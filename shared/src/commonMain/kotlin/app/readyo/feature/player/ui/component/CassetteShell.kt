package app.readyo.feature.player.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.readyo.feature.player.ui.motion.DeckMotion

@Composable
fun CassetteShell(motion: DeckMotion, modifier: Modifier = Modifier) {
    CassetteReelAnimation(motion, modifier)
}
