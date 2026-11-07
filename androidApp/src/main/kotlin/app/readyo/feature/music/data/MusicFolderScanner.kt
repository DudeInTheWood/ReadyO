package app.readyo.feature.music.data

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import app.readyo.core.model.AudioItem
import app.readyo.feature.music.domain.IndexedTrack
import app.readyo.feature.music.domain.MusicDirectory
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

data class FolderScan(val name: String, val tracks: List<IndexedTrack>)

class MusicFolderScanner(private val context: Context) {
    suspend fun scan(root: Uri): FolderScan {
        val rootId = DocumentsContract.getTreeDocumentId(root)
        val rootDocument = DocumentsContract.buildDocumentUriUsingTree(root, rootId)
        val name =
            context.contentResolver
                .query(
                    rootDocument,
                    arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
                    null,
                    null,
                    null,
                )
                ?.use {
                    if (it.moveToFirst()) it.getString(0) else null
                } ?: "Music folder"
        val pending =
            ArrayDeque<MusicDirectory>().apply {
                add(MusicDirectory(rootId, name, name))
            }
        val visited = mutableSetOf<String>()
        val tracks = mutableListOf<IndexedTrack>()
        while (pending.isNotEmpty()) {
            currentCoroutineContext().ensureActive()
            val parent = pending.removeFirst()
            val parentId = parent.id
            if (!visited.add(parentId)) continue
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(root, parentId)
            context.contentResolver.query(children, CHILD_PROJECTION, null, null, null)?.use {
                cursor ->
                while (cursor.moveToNext()) {
                    currentCoroutineContext().ensureActive()
                    val id = cursor.getString(0)
                    val fileName = cursor.getString(1).orEmpty()
                    val mimeType = cursor.getString(2).orEmpty()
                    if (mimeType == DocumentsContract.Document.MIME_TYPE_DIR) {
                        pending.add(MusicDirectory(id, fileName, "${parent.path}/$fileName"))
                    } else if (
                        mimeType.startsWith("audio/") ||
                            fileName.substringAfterLast('.').lowercase() in AUDIO_EXTENSIONS
                    ) {
                        val uri = DocumentsContract.buildDocumentUriUsingTree(root, id)
                        val identity =
                            if (root.authority == "com.android.externalstorage.documents")
                                storageIdentity(id)
                            else "${root.authority}:$id"
                        val directory =
                            if (root.authority == "com.android.externalstorage.documents")
                                storageDirectory(parentId)
                            else parent.copy(id = "${root.authority}:$parentId")
                        tracks +=
                            IndexedTrack(readMetadata(uri, identity, fileName), identity, directory)
                    }
                }
            } ?: error("The folder couldn't be read. Choose it again to restore access.")
        }
        return FolderScan(name, tracks)
    }

    private fun readMetadata(uri: Uri, identity: String, fileName: String): AudioItem {
        var title = fileName.substringBeforeLast('.')
        var artist = ""
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            title =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.takeIf {
                    it.isNotBlank()
                } ?: title
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).orEmpty()
        } catch (_: Exception) {
            // Unrecognized metadata should not prevent browsing a folder; playback reports codec
            // errors.
        } finally {
            retriever.release()
        }
        return AudioItem(identity, title, artist, uri.toString(), radio = false)
    }

    companion object {
        private val CHILD_PROJECTION =
            arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            )
        private val AUDIO_EXTENSIONS =
            setOf("mp3", "m4a", "aac", "flac", "wav", "ogg", "opus", "aiff", "aif", "wma")
    }
}
