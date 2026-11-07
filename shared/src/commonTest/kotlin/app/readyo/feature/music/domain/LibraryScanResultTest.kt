package app.readyo.feature.music.domain

import app.readyo.core.model.AudioItem
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryScanResultTest {
    @Test
    fun overlappingScansKeepCanonicalTrackIdsInTheFolder() {
        val directory = MusicDirectory("primary:Music", "Music", "Internal storage / Music")
        val device = IndexedTrack(track("device", "Song"), "primary:Music/song.mp3", directory)
        val linked = IndexedTrack(track("linked", "Song"), device.identity, directory)
        val groups = listOf(listOf(device), listOf(linked))
        assertEquals(
            setOf(mergeLibraryTracks(groups).single().id),
            buildFolderCollections(groups).single().trackIds,
        )
        assertEquals(setOf("device"), buildFolderCollections(groups).single().trackIds)
    }

    @Test
    fun foldersWithMatchingNamesAndNestedDirectoriesRemainSeparate() {
        val first = MusicDirectory("music:mixes", "Mixes", "Music/Mixes")
        val second = MusicDirectory("downloads:mixes", "Mixes", "Downloads/Mixes")
        val nested = MusicDirectory("music:mixes:live", "Live", "Music/Mixes/Live")
        val groups =
            listOf(
                listOf(
                    IndexedTrack(track("one", "One"), "one", first),
                    IndexedTrack(track("two", "Two"), "two", second),
                    IndexedTrack(track("three", "Three"), "three", nested),
                )
            )
        val collections =
            buildFolderCollections(groups).associate { it.directory.id to it.trackIds }
        assertEquals(
            mapOf(first.id to setOf("one"), second.id to setOf("two"), nested.id to setOf("three")),
            collections,
        )
        assertEquals(emptyList(), buildFolderCollections(emptyList()))
    }

    @Test
    fun overlappingFoldersAndDeviceScanProduceOneTrackPerFile() {
        val folder = IndexedTrack(track("folder", "Song"), "primary:Music/song.mp3")
        val device = IndexedTrack(track("device", "Song"), "primary:Music/song.mp3")
        assertEquals(
            listOf(folder.item),
            mergeLibraryTracks(listOf(listOf(folder), listOf(device, folder))),
        )
    }

    @Test
    fun sameTitlesInDifferentFilesArePreservedAndSortedDeterministically() {
        val second = IndexedTrack(track("b", "Song"), "primary:Music/b.mp3")
        val first = IndexedTrack(track("a", "Song"), "primary:Music/a.mp3")
        val intro = IndexedTrack(track("intro", "Alpha"), "primary:Music/intro.mp3")
        assertEquals(
            listOf(intro.item, first.item, second.item),
            mergeLibraryTracks(listOf(listOf(second, first, intro))),
        )
        assertEquals(emptyList(), mergeLibraryTracks(emptyList()))
    }

    private fun track(id: String, title: String) =
        AudioItem(id, title, "", "content://audio/$id", false)
}
