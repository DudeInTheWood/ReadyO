package app.readyo.feature.player.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.readyo.core.playback.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PlayerViewModel(private val controller: PlayerController) : ViewModel() {
    private val mutableExpanded = MutableStateFlow(false)
    val uiState =
        combine(controller.state, mutableExpanded) { playback, expanded ->
                PlayerUiState(playback, expanded && playback.item != null)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, PlayerUiState(controller.state.value))

    fun onAction(action: PlayerAction) {
        when (action) {
            is PlayerAction.Select -> {
                if (action.index !in action.items.indices) return
                controller.select(action.items, action.index)
                mutableExpanded.value = true
            }
            is PlayerAction.Seek -> controller.seek(action.positionMillis)
            PlayerAction.TogglePlayback -> controller.toggle()
            PlayerAction.Previous -> controller.previous()
            PlayerAction.Next -> controller.next()
            PlayerAction.Expand -> mutableExpanded.value = controller.state.value.item != null
            PlayerAction.Collapse -> mutableExpanded.value = false
        }
    }
}
