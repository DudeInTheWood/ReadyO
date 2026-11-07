package app.readyo.feature.music.data

import android.content.Context
import android.util.AtomicFile
import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable data class SavedMusicFolder(val uri: String, val name: String)

@Serializable
data class MusicSources(
    val deviceScanEnabled: Boolean = false,
    val folders: List<SavedMusicFolder> = emptyList(),
)

class MusicSourceStore(context: Context) {
    private val file = AtomicFile(File(context.filesDir, "music-sources.json"))

    fun read(): MusicSources =
        if (file.baseFile.exists()) Json.decodeFromString(file.readFully().decodeToString())
        else MusicSources()

    fun write(sources: MusicSources) {
        val output = file.startWrite()
        try {
            output.write(Json.encodeToString(sources).encodeToByteArray())
            file.finishWrite(output)
        } catch (exception: Exception) {
            file.failWrite(output)
            throw exception
        }
    }
}
