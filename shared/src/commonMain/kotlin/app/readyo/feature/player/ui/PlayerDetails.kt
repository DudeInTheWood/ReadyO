package app.readyo.feature.player.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.component.MicroLabel
import app.readyo.core.designsystem.component.ReadyOErrorState
import app.readyo.core.playback.PlaybackStatus
import app.readyo.core.playback.PlayerState
import app.readyo.feature.player.presentation.PlayerAction
import app.readyo.feature.player.ui.component.AnalogVUMeter
import app.readyo.feature.player.ui.component.DeckPanel
import app.readyo.feature.player.ui.motion.DeckMotion

@Composable
internal fun PlayerDetails(
    state: PlayerState,
    motion: DeckMotion,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier,
    showMeters: Boolean = true,
) {
    Column(modifier.padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimatedContent(
                targetState = state.item?.title.orEmpty(),
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically { it / 4 }).togetherWith(
                        fadeOut(tween(150))
                    )
                },
                label = "Track metadata",
            ) { title ->
                Text(title, fontSize = 26.sp, lineHeight = 33.sp, fontWeight = FontWeight.SemiBold)
            }
            if (!state.item?.subtitle.isNullOrBlank()) {
                Text(
                    state.item.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                )
            }
            Text(
                playbackLabel(state),
                modifier =
                    Modifier.background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp),
                        )
                        .padding(horizontal = 9.dp, vertical = 5.dp),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 15.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            if (state.item?.radio == false && state.duration > 0) PlaybackTimeline(state, onAction)
        }
        state.error?.let { message ->
            ReadyOErrorState(message) { onAction(PlayerAction.TogglePlayback) }
        }
        if (showMeters) {
            DeckPanel(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AnalogVUMeter(motion)
                    Text(
                        "STEREO / VU",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        lineHeight = 15.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        PlaybackControls(state.status, state.canSkipNext, onAction)
        MicroLabel(
            if (state.item?.radio == true) "INTERNET RADIO / ARCHIVE EDITION"
            else "LOCAL AUDIO / ARCHIVE EDITION"
        )
    }
}

private fun playbackLabel(state: PlayerState): String =
    when (state.status) {
        PlaybackStatus.Playing ->
            if (state.item?.radio == true) "● ON AIR / LIVE RADIO" else "● PLAYING / LOCAL AUDIO"
        PlaybackStatus.Connecting,
        PlaybackStatus.Buffering -> "CONNECTING / PLEASE WAIT"
        PlaybackStatus.Paused -> "PAUSED"
        PlaybackStatus.Error -> "SOURCE UNAVAILABLE"
        PlaybackStatus.Ended -> "PLAYBACK COMPLETE"
        PlaybackStatus.Idle -> "READY"
    }

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PlaybackTimeline(state: PlayerState, onAction: (PlayerAction) -> Unit) {
    var seekPosition by remember(state.item?.id) { mutableStateOf<Float?>(null) }
    val position = seekPosition ?: state.position.toFloat().coerceIn(0f, state.duration.toFloat())
    val colors = MaterialTheme.colorScheme
    Slider(
        value = position,
        onValueChange = { seekPosition = it },
        onValueChangeFinished = {
            seekPosition?.let { onAction(PlayerAction.Seek(it.toLong())) }
            seekPosition = null
        },
        valueRange = 0f..state.duration.toFloat(),
        thumb = {
            Box(Modifier.size(12.dp).background(colors.primary, CircleShape))
        },
        track = {
            Box(
                Modifier.fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(colors.outlineVariant)
            ) {
                Box(
                    Modifier.fillMaxWidth((position / state.duration).coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(colors.primary)
                )
            }
        },
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            formatPlaybackTime(seekPosition?.toLong() ?: state.position),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(formatPlaybackTime(state.duration), color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal fun formatPlaybackTime(milliseconds: Long): String {
    val minutes = milliseconds / 60_000
    val seconds = (milliseconds / 1_000 % 60).toString().padStart(2, '0')
    return "$minutes:$seconds"
}
