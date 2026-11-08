package app.readyo.feature.radio.data

import android.content.Context
import android.content.SharedPreferences
import app.readyo.core.model.AudioItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

internal class RecentStationStore(context: Context) {
    private val prefs = context.getSharedPreferences("recent-radio", Context.MODE_PRIVATE)
    val stations: kotlinx.coroutines.flow.Flow<List<AudioItem>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "stations") trySend(read())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(read())
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun record(station: AudioItem) {
        val previous = read()
        if (previous.firstOrNull() == station) return
        val recent = (listOf(station) + previous.filterNot { it.id == station.id }).take(20)
        prefs.edit().putString("stations", Json.encodeToString(recent)).apply()
    }

    private fun read(): List<AudioItem> =
        try {
            Json.decodeFromString(prefs.getString("stations", null) ?: "[]")
        } catch (_: SerializationException) {
            emptyList()
        }
}
