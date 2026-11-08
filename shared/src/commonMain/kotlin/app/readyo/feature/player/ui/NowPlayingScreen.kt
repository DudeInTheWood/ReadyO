package app.readyo.feature.player.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.playback.PlayerState
import app.readyo.feature.player.presentation.PlayerAction
import app.readyo.feature.player.ui.component.CassetteArtwork
import app.readyo.feature.player.ui.motion.DeckMotion
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(state: PlayerState, motion: DeckMotion, onAction: (PlayerAction) -> Unit) {
    val turn = remember { Animatable(0f) }
    var previewStarted by rememberSaveable { mutableStateOf(false) }
    var manualSideB by rememberSaveable { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(manualSideB) {
        val sideB = manualSideB
        if (sideB != null) {
            turn.animateTo(if (sideB) 180f else 0f, tween(720))
        } else if (!previewStarted) {
            previewStarted = true
            delay(400)
            turn.animateTo(180f, tween(720))
            delay(2000)
            turn.animateTo(0f, tween(720))
        }
    }
    val onFlip = { manualSideB = !(manualSideB ?: (turn.value >= 90f)) }
    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
        val useTwoColumns = maxWidth >= 700.dp && maxHeight >= 420.dp
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            PlayerHeader { onAction(PlayerAction.Collapse) }
            if (useTwoColumns) {
                Row(
                    Modifier.widthIn(max = 1120.dp)
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                ) {
                    CassetteArtwork(state, motion, turn.asState(), onFlip, Modifier.weight(1.1f))
                    PlayerDetails(
                        state,
                        motion,
                        onAction,
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    )
                }
            } else {
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                    PlayerPageLayout(
                        viewportHeight = maxHeight,
                        modifier =
                            Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp),
                        cassette = {
                            CassetteArtwork(
                                state,
                                motion,
                                turn.asState(),
                                onFlip,
                                Modifier.fillMaxWidth(),
                            )
                        },
                        details = {
                            PlayerDetails(
                                state,
                                motion,
                                onAction,
                                Modifier.fillMaxWidth(),
                                showMeters = false,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerHeader(onClose: () -> Unit) {
    Column(Modifier.widthIn(max = 1120.dp).fillMaxWidth()) {
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
    }
}
