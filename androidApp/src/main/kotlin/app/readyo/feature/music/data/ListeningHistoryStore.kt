package app.readyo.feature.music.data

import android.content.Context
import app.readyo.feature.music.domain.ListeningStats
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class ListeningHistoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("listening-history", Context.MODE_PRIVATE)

    fun read(): Map<String, ListeningStats> =
        try {
            Json.decodeFromString(prefs.getString("tracks", null) ?: "{}")
        } catch (_: SerializationException) {
            emptyMap()
        }

    fun record(id: String, millis: Long, completion: Boolean, skip: Boolean, newPlay: Boolean) {
        if (millis <= 0 && !completion && !skip) return
        val tracks = read().toMutableMap()
        val old = tracks[id] ?: ListeningStats()
        tracks[id] =
            old.copy(
                listenedMillis = old.listenedMillis + millis,
                completions = old.completions + if (completion) 1 else 0,
                earlySkips = old.earlySkips + if (skip) 1 else 0,
                plays = old.plays + if (newPlay) 1 else 0,
            )
        prefs.edit().putString("tracks", Json.encodeToString(tracks)).apply()
    }
}
