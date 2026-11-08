package app.readyo.feature.music.data

import android.content.Context
import app.readyo.feature.music.domain.*
import java.time.LocalDate
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
private data class SavedDailyMix(
    val day: String = "",
    val revision: Int = 0,
    val ids: List<String> = emptyList(),
    val previousIds: Set<String> = emptySet(),
)

/** Only date changes and explicit refreshes replace a saved selection. */
class AndroidDailyMixRepository(context: Context, private val library: MusicLibraryRepository) :
    DailyMixRepository {
    private val prefs = context.getSharedPreferences("daily-mix", Context.MODE_PRIVATE)
    private val history = ListeningHistoryStore(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(DailyMixState())
    override val state = mutableState.asStateFlow()

    init {
        scope.launch { library.library.collect { update(false) } }
        scope.launch {
            while (isActive) {
                delay(60_000)
                update(false)
            }
        }
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) { update(true) }

    private suspend fun update(refresh: Boolean) = mutex.withLock {
        val snapshot = library.library.value
        if (snapshot.isLoading || snapshot.isScanning || snapshot.isImporting) return@withLock
        val tracks = snapshot.tracks.filterNot { it.radio }
        if (tracks.isEmpty()) {
            mutableState.value = DailyMixState()
            return@withLock
        }
        mutableState.update { it.copy(isRefreshing = refresh, error = null) }
        try {
            var saved =
                try {
                    Json.decodeFromString<SavedDailyMix>(prefs.getString("mix", null) ?: "{}")
                } catch (_: SerializationException) {
                    SavedDailyMix()
                }
            val today = LocalDate.now().toString()
            val currentIds = tracks.map { it.id }.toSet()
            if (refresh || saved.day != today || saved.ids.none { it in currentIds }) {
                val revision = if (saved.day == today) saved.revision + 1 else 0
                val previous = if (saved.day == today) saved.previousIds else saved.ids.toSet()
                var mix =
                    generateDailyMix(tracks, history.read(), previous, today.hashCode() + revision)
                if (refresh && mix.size > 1 && mix.map { it.id } == saved.ids)
                    mix = mix.drop(1) + mix.first()
                saved = SavedDailyMix(today, revision, mix.map { it.id }, previous)
                check(prefs.edit().putString("mix", Json.encodeToString(saved)).commit())
            }
            val byId = tracks.associateBy { it.id }
            mutableState.value = DailyMixState(saved.ids.mapNotNull { byId[it] })
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            mutableState.update {
                it.copy(isRefreshing = false, error = "Daily Mix couldn't be saved. Try again.")
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
