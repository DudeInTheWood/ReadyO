package app.readyo.feature.music.domain

import app.readyo.core.model.AudioItem
import kotlin.test.*

class MusicGenresTest {
    @Test
    fun mergesCaseAndWhitespaceAndKeepsUntaggedMusic() {
        val songs =
            listOf(
                AudioItem("a", "One", "", "a", false, genre = " Jazz "),
                AudioItem("b", "Two", "", "b", false, genre = "jazz;Soul"),
                AudioItem("c", "Three", "", "c", false),
            )
        val groups = buildMusicGenres(songs)
        assertEquals(listOf("jazz", "soul", "unknown genre"), groups.map { it.id })
        assertEquals(setOf("a", "b"), groups.first().trackIds)
        assertEquals(setOf("c"), groups.last().trackIds)
    }
}
