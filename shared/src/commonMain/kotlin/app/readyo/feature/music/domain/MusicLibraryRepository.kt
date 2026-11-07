package app.readyo.feature.music.domain

import app.readyo.core.model.AudioItem
import kotlinx.coroutines.flow.StateFlow

data class MusicFolder(val id: String, val name: String, val trackCount: Int = 0)

data class MusicDirectory(val id: String, val name: String, val path: String)

data class MusicFolderCollection(val directory: MusicDirectory, val trackIds: Set<String>)

data class MusicLibrarySnapshot(
    val tracks: List<AudioItem> = emptyList(),
    val isLoading: Boolean = true,
    val isImporting: Boolean = false,
    val error: String? = null,
    val folders: List<MusicFolder> = emptyList(),
    val isScanning: Boolean = false,
    val deviceScanEnabled: Boolean = false,
    val collections: List<MusicFolderCollection> = emptyList(),
)

interface MusicLibraryRepository {
    val supportsDeviceScan: Boolean
        get() = false

    val supportsFolderLinking: Boolean
        get() = false

    val library: StateFlow<MusicLibrarySnapshot>

    suspend fun refresh()

    suspend fun importAudio(sources: List<String>)

    suspend fun addFolder(source: String)

    suspend fun removeFolder(id: String)

    suspend fun scanDevice()
}
