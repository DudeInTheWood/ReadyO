package app.readyo.feature.music.domain

import app.readyo.core.model.AudioItem
import kotlin.test.*

class DailyMixTest {
    private fun songs(count: Int) =
        (0 until count).map {
            AudioItem("$it", "Song $it", "Artist ${it % 5}", "file:///$it.flac", false)
        }

    @Test
    fun deterministicUniqueQueueAndSmallLibraries() {
        val library = songs(60)
        val first = generateDailyMix(library, emptyMap(), emptySet(), 10)
        assertEquals(25, first.size)
        assertEquals(25, first.map { it.uri }.toSet().size)
        assertEquals(first, generateDailyMix(library, emptyMap(), emptySet(), 10))
        assertNotEquals(first, generateDailyMix(library, emptyMap(), emptySet(), 11))
        assertEquals(2, generateDailyMix(songs(2), emptyMap(), emptySet(), 10).size)
        assertTrue(generateDailyMix(emptyList(), emptyMap(), emptySet(), 10).isEmpty())
    }

    @Test
    fun familiarQuotaExplorationAndArtistSpacing() {
        val library = songs(100)
        val history = library.take(20).associate { it.id to ListeningStats(300_000, 3, 0, 3) }
        val queue = generateDailyMix(library, history, emptySet(), 10)
        assertEquals(15, queue.count { it.id in history })
        assertTrue(queue.zipWithNext().all { (a, b) -> a.subtitle != b.subtitle })
    }

    @Test
    fun previousDayAndEarlySkipsReduceSelectionWhenAlternativesExist() {
        val library = songs(100)
        val skipped = library.take(20).associate { it.id to ListeningStats(1_000, 0, 8, 8) }
        val previous = library.drop(20).take(25).map { it.id }.toSet()
        val queue = generateDailyMix(library, skipped, previous, 20)
        assertTrue(queue.count { it.id in skipped } < 15)
        assertTrue(queue.any { it.id !in skipped && it.id !in previous })
    }
}
