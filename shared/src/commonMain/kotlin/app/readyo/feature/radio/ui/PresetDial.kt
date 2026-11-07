package app.readyo.feature.radio.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import app.readyo.core.playback.PlaybackStatus

@Composable
internal fun PresetDial(count: Int, index: Int, status: PlaybackStatus) {
    val colors = MaterialTheme.colorScheme
    val position by
        animateFloatAsState(
            index.coerceAtLeast(0).toFloat() / (count - 1).coerceAtLeast(1),
            label = "Tuning needle",
        )
    Surface(
        color = colors.surfaceContainerHigh,
        shape = RoundedCornerShape(9.dp),
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IndexLabel("STATION INDEX", colors.onSurfaceVariant)
                IndexLabel(
                    if (index < 0) "PRESETS"
                    else
                        (if (status == PlaybackStatus.Playing) "ON AIR"
                        else status.name.uppercase()) + " / " + presetNumber(index),
                    colors.secondary,
                )
            }
            Canvas(Modifier.fillMaxWidth().height(26.dp).padding(horizontal = 4.dp)) {
                for (tick in 0..40) {
                    val x = size.width * tick / 40
                    drawLine(
                        colors.outline,
                        Offset(x, size.height),
                        Offset(x, size.height * if (tick % 8 == 0) .18f else .64f),
                        1.dp.toPx(),
                    )
                }
                if (index >= 0) {
                    val x = size.width * position
                    drawLine(
                        colors.primary.copy(alpha = .16f),
                        Offset(x, 0f),
                        Offset(x, size.height),
                        7.dp.toPx(),
                    )
                    drawLine(colors.primary, Offset(x, 0f), Offset(x, size.height), 2.dp.toPx())
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val marks = count.coerceIn(0, 6)
                repeat(marks) { mark ->
                    IndexLabel(presetNumber(mark * (count - 1) / (marks - 1).coerceAtLeast(1)))
                }
            }
        }
    }
}
