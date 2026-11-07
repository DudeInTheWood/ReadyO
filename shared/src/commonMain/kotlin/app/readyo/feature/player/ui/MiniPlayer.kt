package app.readyo.feature.player.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.playback.PlaybackStatus
import app.readyo.core.playback.PlayerState
import app.readyo.feature.player.presentation.PlayerAction
import app.readyo.feature.player.ui.component.CassetteShell
import app.readyo.feature.player.ui.component.CompactTransportButton
import app.readyo.feature.player.ui.component.TransportSymbol
import app.readyo.feature.player.ui.motion.DeckMotion
import app.readyo.feature.player.ui.motion.swipeToOpenPlayer

/** Persistent listening strip. Playback actions stay separate from tap/swipe expansion. */
@Composable
fun MiniPlayer(state: PlayerState, motion: DeckMotion, onAction: (PlayerAction) -> Unit) {
    val item = state.item ?: return
    val expand = { onAction(PlayerAction.Expand) }
    val isPlaying = state.status == PlaybackStatus.Playing
    val isLoading =
        state.status == PlaybackStatus.Connecting || state.status == PlaybackStatus.Buffering

    Box(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = expand,
            modifier = Modifier.widthIn(max = 760.dp).fillMaxWidth().swipeToOpenPlayer(expand),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
            shadowElevation = 3.dp,
        ) {
            BoxWithConstraints {
                val showArtwork = maxWidth >= 340.dp && LocalDensity.current.fontScale <= 1.4f
                val showSubtitle = maxWidth >= 380.dp
                Column {
                    Box(
                        Modifier.fillMaxWidth().height(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            Modifier.width(24.dp)
                                .height(2.dp)
                                .background(
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                    CircleShape,
                                )
                        )
                    }
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .heightIn(min = 68.dp)
                                .padding(start = 12.dp, end = 10.dp, bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (showArtwork) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                            ) {
                                Box(Modifier.width(52.dp).padding(3.dp)) {
                                    CassetteShell(motion)
                                }
                            }
                        }
                        Column(
                            Modifier.weight(1f).semantics {
                                contentDescription =
                                    "${item.title}. Swipe up or tap to open Now Playing."
                            },
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                            ) {
                                Box(
                                    Modifier.size(5.dp)
                                        .background(
                                            if (isPlaying) MaterialTheme.colorScheme.secondary
                                            else MaterialTheme.colorScheme.outline,
                                            CircleShape,
                                        )
                                )
                                Text(
                                    text = miniSourceLabel(state),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    letterSpacing = 0.7.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(
                                item.title,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                lineHeight = 21.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (item.subtitle.isNotBlank() && showSubtitle) {
                                Text(
                                    item.subtitle,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CompactTransportButton(
                                symbol =
                                    if (isPlaying) TransportSymbol.Pause else TransportSymbol.Play,
                                description =
                                    if (isLoading) "Connecting"
                                    else if (isPlaying) "Pause" else "Play",
                                primary = true,
                                loading = isLoading,
                                enabled = !isLoading,
                            ) {
                                onAction(PlayerAction.TogglePlayback)
                            }
                            CompactTransportButton(
                                symbol = TransportSymbol.Next,
                                description = if (item.radio) "Next station" else "Next track",
                                enabled = state.canSkipNext,
                            ) {
                                onAction(PlayerAction.Next)
                            }
                        }
                    }
                    if (!item.radio && state.duration > 0) {
                        LinearProgressIndicator(
                            progress = {
                                (state.position.toFloat() / state.duration).coerceIn(0f, 1f)
                            },
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            drawStopIndicator = {},
                        )
                    }
                }
            }
        }
    }
}

private fun miniSourceLabel(state: PlayerState): String {
    val source = if (state.item?.radio == true) "RADIO" else "LOCAL"
    val status =
        when (state.status) {
            PlaybackStatus.Playing -> if (state.item?.radio == true) "LIVE" else "PLAYING"
            PlaybackStatus.Connecting,
            PlaybackStatus.Buffering -> "CONNECTING"
            PlaybackStatus.Error -> "UNAVAILABLE"
            PlaybackStatus.Paused -> "PAUSED"
            PlaybackStatus.Ended -> "ENDED"
            PlaybackStatus.Idle -> "READY"
        }
    return "$source / $status"
}
