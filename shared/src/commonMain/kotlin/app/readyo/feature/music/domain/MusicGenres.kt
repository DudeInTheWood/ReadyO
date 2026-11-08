package app.readyo.feature.music.domain

import app.readyo.core.model.AudioItem

data class MusicGenre(val id: String, val name: String, val trackIds: Set<String>)

fun buildMusicGenres(tracks: List<AudioItem>): List<MusicGenre> {
    val names = linkedMapOf<String, String>()
    val members = linkedMapOf<String, MutableSet<String>>()
    for (track in tracks) {
        val tags =
            track.genre
                .orEmpty()
                .split(';', '\u0000')
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.equals("<unknown>", true) }
                .ifEmpty { listOf("Unknown genre") }
        for (tag in tags) {
            val id = tag.lowercase()
            names.getOrPut(id) { tag }
            members.getOrPut(id) { mutableSetOf() }.add(track.id)
        }
    }
    return names
        .map { (id, name) -> MusicGenre(id, name, members.getValue(id)) }
        .sortedWith(compareBy<MusicGenre> { it.id == "unknown genre" }.thenBy { it.id })
}
