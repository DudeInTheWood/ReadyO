package app.readyo.feature.radio.data

import kotlin.test.*

class RadioDirectoryTest {
    @Test
    fun resolvedStreamWinsAndDuplicateIdsAreCollapsed() {
        val rows =
            parseStations(
                """[
          {"stationuuid":"one","name":" Real station ","url":"http://old.example/a","url_resolved":"https://stream.example/live","country":"Thailand","tags":"jazz,pop,rock"},
          {"stationuuid":"one","name":"Duplicate","url":"https://stream.example/other"}
        ]"""
            )
        assertEquals(1, rows.size)
        assertEquals("https://stream.example/live", rows.single().uri)
        assertEquals("Real station", rows.single().title)
        assertEquals("Thailand · jazz · pop", rows.single().subtitle)
    }

    @Test
    fun unusableEntriesAreExcludedAndMissingMetadataStaysEmpty() {
        val rows =
            parseStations(
                """[
          {"stationuuid":"bad","name":"Local path","url":"file:///private/audio"},
          {"stationuuid":"bad2","name":"Invalid protocol","url":"http-invalid"},
          {"stationuuid":"","name":"Missing ID","url":"https://example.com"},
          {"stationuuid":"ok","name":"Minimal","url":"http://example.com/live"}
        ]"""
            )
        assertEquals(listOf("ok"), rows.map { it.id })
        assertEquals("", rows.single().subtitle)
    }
}
