package app.readyo.feature.player.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.readyo.core.designsystem.component.MicroLabel
import app.readyo.core.playback.PlayerState
import app.readyo.feature.player.ui.motion.DeckMotion

/** The printed title follows the cassette geometry; scalable metadata remains below the artwork. */
@Composable
fun CassetteArtwork(state: PlayerState, motion: DeckMotion, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DeckPanel(Modifier.fillMaxWidth()) {
            BoxWithConstraints(Modifier.fillMaxWidth().padding(12.dp)) {
                CassetteShell(motion)
                CassetteTitleLabel(
                    title = state.item?.title.orEmpty(),
                    modifier =
                        Modifier.offset(x = maxWidth * (145f / 510f), y = maxWidth * (59f / 510f))
                            .width(maxWidth * (240f / 510f))
                            .height(maxWidth * (54f / 510f)),
                )
            }
        }
        MicroLabel(
            if (state.item?.radio == true) "SIDE A / RADIO RECEIVER"
            else "SIDE B / PERSONAL MIXTAPE"
        )
    }
}
