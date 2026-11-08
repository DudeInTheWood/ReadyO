package app.readyo.feature.music.data

import android.content.Context
import android.util.AtomicFile
import app.readyo.feature.music.domain.IndexedTrack
import java.io.File
import java.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
internal data class CachedScans(
    val device: List<IndexedTrack>? = null,
    val folders: Map<String, FolderScan> = emptyMap(),
)

internal class MusicScanCache(context: Context) {
    private val file = AtomicFile(File(context.cacheDir, "music-scans-v3.json"))

    fun read(): CachedScans =
        try {
            Json.decodeFromString<CachedScans>(file.readFully().decodeToString())
        } catch (_: IOException) {
            CachedScans()
        } catch (_: SerializationException) {
            CachedScans()
        }

    fun write(scans: CachedScans) {
        val output = file.startWrite()
        try {
            output.write(Json.encodeToString(scans).encodeToByteArray())
            file.finishWrite(output)
        } catch (failure: Exception) {
            file.failWrite(output)
            throw failure
        }
    }
}
