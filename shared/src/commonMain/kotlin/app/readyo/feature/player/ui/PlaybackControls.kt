package app.readyo.feature.player.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.readyo.core.playback.PlaybackStatus
import app.readyo.feature.player.presentation.PlayerAction
import app.readyo.feature.player.ui.component.DeckPanel
import app.readyo.feature.player.ui.component.TransportKey
import app.readyo.feature.player.ui.component.TransportSymbol

@Composable
fun PlaybackControls(
    status: PlaybackStatus,
    canSkipNext: Boolean,
    onAction: (PlayerAction) -> Unit,
) {
    val playing = status == PlaybackStatus.Playing
    val loading = status == PlaybackStatus.Connecting || status == PlaybackStatus.Buffering
    DeckPanel(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TransportKey("Previous", TransportSymbol.Previous, modifier = Modifier.weight(1f)) {
                onAction(PlayerAction.Previous)
            }
            TransportKey(
                if (playing) "Pause" else "Play",
                if (playing) TransportSymbol.Pause else TransportSymbol.Play,
                illuminated = playing,
                enabled = !loading,
                modifier = Modifier.weight(1f),
            ) {
                onAction(PlayerAction.TogglePlayback)
            }
            TransportKey(
                "Next",
                TransportSymbol.Next,
                enabled = canSkipNext,
                modifier = Modifier.weight(1f),
            ) {
                onAction(PlayerAction.Next)
            }
        }
    }
}
