package app.readyo.feature.music.domain

import app.readyo.core.model.AudioItem
import kotlin.random.Random
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable
data class ListeningStats(
    val listenedMillis: Long = 0,
    val completions: Int = 0,
    val earlySkips: Int = 0,
    val plays: Int = 0,
)

data class DailyMixState(
    val tracks: List<AudioItem> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

interface DailyMixRepository {
    val state: StateFlow<DailyMixState>

    suspend fun refresh()
}

/** Seeded selection stays reproducible. Saved queues provide stability as history grows. */
fun generateDailyMix(
    library: List<AudioItem>,
    history: Map<String, ListeningStats>,
    previousIds: Set<String>,
    seed: Int,
): List<AudioItem> {
    val random = Random(seed)
    val candidates = library.filterNot { it.radio }.distinctBy { it.uri }.shuffled(random)
    val count = minOf(25, candidates.size)
    if (count == 0) return emptyList()
    fun score(item: AudioItem): Long {
        val stats = history[item.id] ?: ListeningStats()
        return stats.completions * 4L + stats.plays + stats.listenedMillis / 60_000 -
            stats.earlySkips * 5L -
            if (item.id in previousIds) 4L else 0L
    }
    val selected = mutableListOf<AudioItem>()
    val pool = candidates.toMutableList()
    fun takeFrom(ranked: List<AudioItem>, amount: Int) {
        val picks = ranked.take(amount)
        selected += picks
        val ids = picks.map { it.id }.toSet()
        pool.removeAll { it.id in ids }
    }
    if (history.values.any { it.listenedMillis > 0 }) {
        takeFrom(
            pool
                .filter { (history[it.id]?.listenedMillis ?: 0) > 0 && score(it) > 0 }
                .sortedByDescending { score(it) },
            count * 60 / 100,
        )
        takeFrom(
            pool.sortedWith(
                compareBy<AudioItem> { history[it.id]?.earlySkips ?: 0 }
                    .thenBy { it.id in previousIds }
                    .thenBy { history[it.id]?.plays ?: 0 }
            ),
            count * 30 / 100,
        )
    }
    takeFrom(
        pool
            .shuffled(random)
            .sortedWith(
                compareBy<AudioItem> { history[it.id]?.earlySkips ?: 0 }
                    .thenBy { it.id in previousIds }
            ),
        count - selected.size,
    )
    val remaining = selected.shuffled(random).toMutableList()
    val result = mutableListOf<AudioItem>()
    while (remaining.isNotEmpty()) {
        val lastArtist = result.lastOrNull()?.subtitle?.trim()?.lowercase()
        val next =
            remaining
                .indexOfFirst {
                    it.subtitle.isBlank() || it.subtitle.trim().lowercase() != lastArtist
                }
                .coerceAtLeast(0)
        result += remaining.removeAt(next)
    }
    return result
}
