package app.readyo.feature.player.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.component.MicroLabel
import app.readyo.core.playback.PlayerState
import app.readyo.feature.player.presentation.PlayerAction
import app.readyo.feature.player.ui.component.CassetteArtwork
import app.readyo.feature.player.ui.motion.DeckMotion

@Composable
fun NowPlayingScreen(state: PlayerState, motion: DeckMotion, onAction: (PlayerAction) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
        val useTwoColumns = maxWidth >= 700.dp && maxHeight >= 420.dp
        val showCompactMeters = state.item?.radio != false || maxHeight >= 1000.dp
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            PlayerHeader(state.item?.radio == true) { onAction(PlayerAction.Collapse) }
            if (useTwoColumns) {
                Row(
                    Modifier.widthIn(max = 1120.dp)
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    CassetteArtwork(state, motion, Modifier.weight(1.1f))
                    PlayerDetails(
                        state,
                        motion,
                        onAction,
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    )
                }
            } else {
                Column(
                    Modifier.weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CassetteArtwork(state, motion, Modifier.widthIn(max = 460.dp).fillMaxWidth())
                    PlayerDetails(
                        state,
                        motion,
                        onAction,
                        Modifier.widthIn(max = 560.dp).fillMaxWidth(),
                        showMeters = showCompactMeters,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerHeader(radio: Boolean, onClose: () -> Unit) {
    Column(Modifier.widthIn(max = 1120.dp).fillMaxWidth().padding(bottom = 20.dp)) {
        Box(Modifier.fillMaxWidth().height(18.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.width(36.dp)
                    .height(3.dp)
                    .background(MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onClose) { Text("⌄  Close") }
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Text(
                    "ReadyO",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(
            Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "Now playing.",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 38.sp,
            )
            MicroLabel(if (radio) "ARCHIVE EDITION / SIDE A" else "ARCHIVE EDITION / SIDE B")
        }
    }
}
