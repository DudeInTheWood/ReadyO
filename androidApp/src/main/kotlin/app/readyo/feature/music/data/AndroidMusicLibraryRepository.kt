package app.readyo.feature.music.data

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.AtomicFile
import app.readyo.core.model.AudioItem
import app.readyo.core.playback.localTrackTitle
import app.readyo.feature.music.domain.IndexedTrack
import app.readyo.feature.music.domain.MusicFolder
import app.readyo.feature.music.domain.MusicLibraryRepository
import app.readyo.feature.music.domain.MusicLibrarySnapshot
import app.readyo.feature.music.domain.buildFolderCollections
import app.readyo.feature.music.domain.mergeLibraryTracks
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class AndroidMusicLibraryRepository(context: Context) : MusicLibraryRepository {
    override val supportsDeviceScan = true
    override val supportsFolderLinking = true
    private val context = context.applicationContext
    private val indexFile = AtomicFile(File(context.filesDir, "library.json"))
    private val audioDirectory = File(context.filesDir, "audio")
    private val mutationLock = Mutex()
    private val mutableLibrary = MutableStateFlow(MusicLibrarySnapshot())
    override val library = mutableLibrary.asStateFlow()
    private val sourceStore = MusicSourceStore(context)
    private val deviceScanner = DeviceMusicScanner(context)
    private val folderScanner = MusicFolderScanner(context)
    private val scanCache = MusicScanCache(context)

    override suspend fun load() = updateSources { it }

    override suspend fun refresh() = updateSources(refreshAll = true) { it }

    override suspend fun scanDevice() =
        updateSources(refreshDevice = true) { sources ->
            check(deviceScanner.hasPermission) {
                "Allow music access to scan this device, or choose a music folder instead."
            }
            sources.copy(deviceScanEnabled = true)
        }

    override suspend fun addFolder(source: String) =
        updateSources(refreshFolder = source) { sources ->
            val uri = Uri.parse(source)
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            sources.copy(
                folders =
                    (sources.folders + SavedMusicFolder(source, "Music folder")).distinctBy {
                        it.uri
                    }
            )
        }

    override suspend fun removeFolder(id: String) = updateSources { sources ->
        runCatching {
            context.contentResolver.releasePersistableUriPermission(
                Uri.parse(id),
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        }
        sources.copy(folders = sources.folders.filterNot { it.uri == id })
    }

    private suspend fun updateSources(
        refreshAll: Boolean = false,
        refreshDevice: Boolean = false,
        refreshFolder: String? = null,
        transform: (MusicSources) -> MusicSources,
    ) =
        withContext(Dispatchers.IO) {
            mutationLock.withLock {
                mutableLibrary.update { it.copy(isScanning = true, error = null) }
                try {
                    val previous = sourceStore.read()
                    val sources = transform(previous)
                    if (sources != previous) sourceStore.write(sources)
                    rebuildLibrary(sources, refreshAll, refreshDevice, refreshFolder)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    mutableLibrary.update {
                        it.copy(
                            isLoading = false,
                            error =
                                if (exception is IllegalStateException) exception.message
                                else
                                    "Your music couldn't be read. Choose the folder again or rescan after allowing music access.",
                        )
                    }
                } finally {
                    mutableLibrary.update { it.copy(isScanning = false, isLoading = false) }
                }
            }
        }

    private suspend fun rebuildLibrary(
        sources: MusicSources,
        refreshAll: Boolean = false,
        refreshDevice: Boolean = false,
        refreshFolder: String? = null,
    ) {
        val cached = scanCache.read()
        var deviceTracks: List<IndexedTrack>? = null
        val folderScans = mutableMapOf<String, FolderScan>()
        val groups =
            mutableListOf(
                readImportedTracks().map { IndexedTrack(it, it.uri, importedFilesDirectory) }
            )
        val folders = mutableListOf<MusicFolder>()
        val savedFolders = mutableListOf<SavedMusicFolder>()
        val failures = mutableListOf<String>()
        if (sources.deviceScanEnabled) {
            try {
                check(deviceScanner.hasPermission)
                deviceTracks =
                    if (refreshAll || refreshDevice) deviceScanner.scan()
                    else cached.device ?: deviceScanner.scan()
                groups += deviceTracks
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                failures +=
                    "Device music needs access. Tap Scan device music to allow it, or choose a folder."
            }
        }
        for (folder in sources.folders) {
            try {
                val uri = Uri.parse(folder.uri)
                check(
                    context.contentResolver.persistedUriPermissions.any {
                        it.uri == uri && it.isReadPermission
                    }
                )
                val result =
                    if (refreshAll || refreshFolder == folder.uri) folderScanner.scan(uri)
                    else cached.folders[folder.uri] ?: folderScanner.scan(uri)
                folderScans[folder.uri] = result
                groups += result.tracks
                folders += MusicFolder(folder.uri, result.name, result.tracks.size)
                savedFolders += folder.copy(name = result.name)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                folders += MusicFolder(folder.uri, folder.name)
                savedFolders += folder
                failures += "${folder.name} couldn't be read. Choose it again to restore access."
            }
        }
        if (savedFolders != sources.folders) sourceStore.write(sources.copy(folders = savedFolders))
        val updated = CachedScans(deviceTracks, folderScans)
        if (updated != cached) {
            try {
                scanCache.write(updated)
            } catch (_: java.io.IOException) {
                // A full cache volume must not prevent browsing a successful scan.
            }
        }
        mutableLibrary.update { current ->
            current.copy(
                tracks = mergeLibraryTracks(groups),
                collections = buildFolderCollections(groups),
                folders = folders,
                deviceScanEnabled = sources.deviceScanEnabled,
                isLoading = false,
                error = failures.takeIf { it.isNotEmpty() }?.joinToString("\n"),
            )
        }
    }

    private fun readImportedTracks(): List<AudioItem> =
        if (indexFile.baseFile.exists()) {
            val stored =
                Json.decodeFromString<List<AudioItem>>(indexFile.readFully().decodeToString())
                    .filter {
                        File(Uri.parse(it.uri).path.orEmpty()).exists()
                    }
            val upgraded = stored.map { item ->
                if (item.genre != null) item
                else {
                    val reader = MediaMetadataRetriever()
                    try {
                        reader.setDataSource(context, Uri.parse(item.uri))
                        item.copy(
                            genre =
                                reader
                                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                                    .orEmpty()
                        )
                    } catch (_: Exception) {
                        item.copy(genre = "")
                    } finally {
                        runCatching { reader.release() }
                    }
                }
            }
            if (upgraded != stored) saveIndex(upgraded)
            upgraded
        } else emptyList()

    override suspend fun importAudio(sources: List<String>) =
        withContext(Dispatchers.IO) {
            if (sources.isEmpty()) return@withContext
            mutationLock.withLock {
                mutableLibrary.update { it.copy(isImporting = true, error = null) }
                try {
                    val imports = sources.map { source ->
                        runCatching { copyAudio(Uri.parse(source)) }
                    }
                    val importedTracks = imports.mapNotNull { it.getOrNull() }
                    val tracks =
                        (readImportedTracks() + importedTracks).sortedBy {
                            it.title.lowercase()
                        }
                    saveIndex(tracks)
                    rebuildLibrary(sourceStore.read())
                    if (imports.any { it.isFailure })
                        mutableLibrary.update {
                            it.copy(
                                error =
                                    "Some files couldn't be imported. Choose a supported audio file and try again."
                            )
                        }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    mutableLibrary.update {
                        it.copy(
                            error =
                                "There wasn't enough access or storage to save your import. Please try again."
                        )
                    }
                } finally {
                    mutableLibrary.update { it.copy(isImporting = false) }
                }
            }
        }

    private fun copyAudio(source: Uri): AudioItem {
        val displayName = readDisplayName(source)
        val id = UUID.randomUUID().toString()
        audioDirectory.mkdirs()
        val extension =
            displayName.substringAfterLast('.', "audio").take(8).filter { it.isLetterOrDigit() }
        val destination = File(audioDirectory, "$id.$extension")
        try {
            val input =
                context.contentResolver.openInputStream(source) ?: error("Audio unavailable")
            input.use { stream -> destination.outputStream().use(stream::copyTo) }
            return readMetadata(destination, id, displayName)
        } catch (exception: Exception) {
            destination.delete()
            throw exception
        }
    }

    private fun readDisplayName(source: Uri): String {
        context.contentResolver
            .query(source, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) return cursor.getString(0) ?: "Imported audio"
            }
        return "Imported audio"
    }

    private fun readMetadata(file: File, id: String, displayName: String): AudioItem {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val title =
                localTrackTitle(
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    displayName.substringBeforeLast('.'),
                )
            val artist =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: "Imported audio"
            return AudioItem(
                id,
                title,
                artist,
                Uri.fromFile(file).toString(),
                radio = false,
                genre =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE).orEmpty(),
            )
        } finally {
            retriever.release()
        }
    }

    private fun saveIndex(tracks: List<AudioItem>) {
        val output = indexFile.startWrite()
        try {
            output.write(Json.encodeToString(tracks).encodeToByteArray())
            indexFile.finishWrite(output)
        } catch (exception: Exception) {
            indexFile.failWrite(output)
            throw exception
        }
    }
}
