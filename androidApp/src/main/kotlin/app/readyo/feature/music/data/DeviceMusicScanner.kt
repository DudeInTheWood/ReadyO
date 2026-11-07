package app.readyo.feature.music.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import app.readyo.core.model.AudioItem
import app.readyo.feature.music.domain.IndexedTrack
import app.readyo.feature.music.domain.MusicDirectory
import java.io.File
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class DeviceMusicScanner(private val context: Context) {
    val hasPermission: Boolean
        get() =
            ContextCompat.checkSelfPermission(context, audioPermission) ==
                PackageManager.PERMISSION_GRANTED

    suspend fun scan(): List<IndexedTrack> {
        check(hasPermission) {
            "Music access is needed to scan the device. Choose a folder instead, or allow audio access."
        }
        val collection =
            if (Build.VERSION.SDK_INT >= 29) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            if (Build.VERSION.SDK_INT >= 29) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
                add(MediaStore.Audio.Media.VOLUME_NAME)
            } else {
                @Suppress("DEPRECATION") add(MediaStore.Audio.Media.DATA)
            }
        }
        val tracks = mutableListOf<IndexedTrack>()
        context.contentResolver
            .query(
                collection,
                projection.toTypedArray(),
                "${MediaStore.Audio.Media.IS_MUSIC} != 0",
                null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC",
            )
            ?.use { cursor ->
                while (cursor.moveToNext()) {
                    currentCoroutineContext().ensureActive()
                    val id = cursor.getLong(0)
                    val fileName = cursor.getString(3).orEmpty()
                    val title =
                        cursor.getString(1).orEmpty().ifBlank { fileName.substringBeforeLast('.') }
                    val artist =
                        cursor.getString(2).orEmpty().takeUnless { it == "<unknown>" }.orEmpty()
                    val uri = ContentUris.withAppendedId(collection, id).toString()
                    val directory =
                        if (Build.VERSION.SDK_INT >= 29) {
                            val volume =
                                cursor.getString(5).orEmpty().let {
                                    if (it == "external_primary") "primary" else it
                                }
                            storageDirectory("$volume:${cursor.getString(4).orEmpty()}")
                        } else {
                            cursor
                                .getString(4)
                                ?.takeIf { it.isNotBlank() }
                                ?.let { path ->
                                    File(path).parentFile?.let { parent ->
                                        MusicDirectory(
                                            "device-folder:${parent.path}",
                                            parent.name,
                                            parent.path,
                                        )
                                    }
                                }
                        }
                    val identity =
                        if (Build.VERSION.SDK_INT >= 29) {
                            val volume =
                                cursor.getString(5).orEmpty().let {
                                    if (it == "external_primary") "primary" else it
                                }
                            storageIdentity("$volume:${cursor.getString(4).orEmpty()}$fileName")
                        } else uri
                    tracks +=
                        IndexedTrack(
                            AudioItem("device:$id", title, artist, uri, radio = false),
                            identity,
                            directory,
                        )
                }
            } ?: error("Device music couldn't be read. Please try again.")
        return tracks
    }

    companion object {
        val audioPermission: String
            get() =
                if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
                else Manifest.permission.READ_EXTERNAL_STORAGE
    }
}

internal fun storageIdentity(documentId: String): String {
    val volume = documentId.substringBefore(':').lowercase()
    val path = documentId.substringAfter(':', "")
    return "storage:$volume:$path"
}
