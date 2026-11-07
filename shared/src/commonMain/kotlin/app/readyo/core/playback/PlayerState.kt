package app.readyo.core.playback

import app.readyo.core.model.AudioItem

enum class PlaybackStatus {
    Idle,
    Connecting,
    Buffering,
    Playing,
    Paused,
    Ended,
    Error,
}

data class PlayerState(
    val item: AudioItem? = null,
    val status: PlaybackStatus = PlaybackStatus.Idle,
    val position: Long = 0,
    val duration: Long = 0,
    val error: String? = null,
    val canSkipNext: Boolean = false,
)
