package app.readyo.feature.player.presentation

import app.readyo.core.model.AudioItem
import app.readyo.core.playback.PlayerState

sealed interface PlayerAction {
    data class Select(val items: List<AudioItem>, val index: Int) : PlayerAction

    data class Seek(val positionMillis: Long) : PlayerAction

    data object TogglePlayback : PlayerAction

    data object Previous : PlayerAction

    data object Next : PlayerAction

    data object Expand : PlayerAction

    data object Collapse : PlayerAction
}

data class PlayerUiState(val playback: PlayerState = PlayerState(), val isExpanded: Boolean = false)
