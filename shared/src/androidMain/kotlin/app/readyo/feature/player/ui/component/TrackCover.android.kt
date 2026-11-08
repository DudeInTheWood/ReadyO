package app.readyo.feature.player.ui.component

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import app.readyo.core.model.AudioItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val covers =
    object : LruCache<String, ImageBitmap>(4 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ImageBitmap) = value.width * value.height * 4
    }

@Composable
internal actual fun rememberTrackCover(item: AudioItem?): ImageBitmap? {
    if (item == null || item.radio) return null
    val context = LocalContext.current.applicationContext
    val uri = item.uri
    return key(uri) {
        produceState(covers.get(uri)) {
                if (value == null)
                    value =
                        withContext(Dispatchers.IO) {
                            embeddedCover(context, uri)?.also { covers.put(uri, it) }
                        }
            }
            .value
    }
}

private fun embeddedCover(context: Context, uri: String): ImageBitmap? {
    val reader = MediaMetadataRetriever()
    try {
        reader.setDataSource(context, Uri.parse(uri))
        val bytes = reader.embeddedPicture ?: return null
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        options.inJustDecodeBounds = false
        while (
            maxOf(options.outWidth, options.outHeight) / options.inSampleSize.coerceAtLeast(1) >
                1024
        ) {
            options.inSampleSize = options.inSampleSize.coerceAtLeast(1) * 2
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)?.asImageBitmap()
    } catch (_: RuntimeException) {
        return null
    } catch (_: java.io.IOException) {
        return null
    } finally {
        runCatching { reader.release() }
    }
}
