package app.readyo.feature.music.domain

import app.readyo.core.model.AudioItem
import kotlinx.serialization.Serializable

/** A provider-independent identity prevents overlapping folders/device scans duplicating a song. */
@Serializable
data class IndexedTrack(
    val item: AudioItem,
    val identity: String,
    val directory: MusicDirectory? = null,
)

fun mergeLibraryTracks(groups: List<List<IndexedTrack>>): List<AudioItem> =
    groups
        .flatten()
        .distinctBy { it.identity }
        .map { it.item }
        .sortedWith(compareBy<AudioItem> { it.title.lowercase() }.thenBy { it.id })

/** Folder filters reference the canonical item even when device and linked scans overlap. */
fun buildFolderCollections(groups: List<List<IndexedTrack>>): List<MusicFolderCollection> {
    val indexedTracks = groups.flatten()
    val canonicalTracks = indexedTracks.distinctBy { it.identity }.associateBy { it.identity }
    val directories = linkedMapOf<String, MusicDirectory>()
    val folderMembers = mutableMapOf<String, MutableSet<String>>()
    for (track in indexedTracks) {
        val directory = track.directory ?: continue
        directories.getOrPut(directory.id) { directory }
        val canonicalId = canonicalTracks.getValue(track.identity).item.id
        folderMembers.getOrPut(directory.id) { mutableSetOf() }.add(canonicalId)
    }
    return directories.values
        .map { directory ->
            MusicFolderCollection(
                directory = directory,
                trackIds = folderMembers.getValue(directory.id).toSet(),
            )
        }
        .sortedWith(
            compareBy<MusicFolderCollection> { it.directory.name.lowercase() }
                .thenBy { it.directory.path.lowercase() }
                .thenBy { it.directory.id }
        )
}
