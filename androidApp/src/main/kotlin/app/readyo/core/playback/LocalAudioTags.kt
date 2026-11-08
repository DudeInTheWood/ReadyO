package app.readyo.core.playback

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal data class LocalAudioTags(
    val title: String = "",
    val credits: String = "",
    val quality: String = "",
)

internal suspend fun readLocalAudioTags(context: Context, uri: String): LocalAudioTags =
    withContext(Dispatchers.IO) {
        val reader = MediaMetadataRetriever()
        try {
            reader.setDataSource(context, Uri.parse(uri))
            fun tag(key: Int) = reader.extractMetadata(key)?.trim()?.takeIf { it.isNotEmpty() }
            fun number(key: Int) = tag(key)?.toIntOrNull()?.takeIf { it > 0 }

            val artist =
                tag(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                    ?: tag(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)
            val album = tag(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val quality = buildList {
                val format =
                    when (tag(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)) {
                        "audio/mpeg" -> "MP3"
                        "audio/mp4",
                        "video/mp4" -> "MP4"
                        "audio/flac",
                        "audio/x-flac" -> "FLAC"
                        "audio/mp4a-latm",
                        "audio/aac",
                        "audio/aac-adts" -> "AAC"
                        "audio/x-wav",
                        "audio/wav" -> "WAV"
                        "audio/ogg",
                        "application/ogg" -> "Ogg"
                        "audio/opus" -> "Opus"
                        else -> null
                    }
                format?.let { add(it) }
                if (Build.VERSION.SDK_INT >= 31) {
                    number(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.let {
                        add(if (it % 1000 == 0) "${it / 1000} kHz" else "${it / 1000.0} kHz")
                    }
                    number(MediaMetadataRetriever.METADATA_KEY_BITS_PER_SAMPLE)?.let {
                        add("$it-bit")
                    }
                }
                number(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.let {
                    add("${(it + 500L) / 1000} kbps")
                }
            }
            LocalAudioTags(
                title = tag(MediaMetadataRetriever.METADATA_KEY_TITLE).orEmpty(),
                credits = listOfNotNull(artist, album).distinct().joinToString(" · "),
                quality = quality.joinToString(" · "),
            )
        } catch (_: RuntimeException) {
            LocalAudioTags()
        } catch (_: java.io.IOException) {
            LocalAudioTags()
        } finally {
            runCatching { reader.release() }
        }
    }
