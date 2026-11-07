package app.readyo.core.playback

import app.readyo.core.model.AudioItem
import kotlinx.coroutines.flow.StateFlow

interface PlayerController {
    val state: StateFlow<PlayerState>

    fun select(items: List<AudioItem>, index: Int = 0)

    fun toggle()

    fun seek(position: Long)

    fun next()

    fun previous()
}
