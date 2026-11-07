package app.readyo.feature.player.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Fine brushed texture stays local to the physical deck, behind all content. */
@Composable
internal fun DeckPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .clip(shape)
            .background(Brush.verticalGradient(listOf(colors.surfaceVariant, colors.surface)))
            .drawBehind {
                val spacing = 3.dp.toPx()
                repeat((size.height / spacing).toInt()) { row ->
                    drawLine(
                        Color.White.copy(alpha = if (row % 3 == 0) 0.025f else 0.01f),
                        Offset(0f, row * spacing),
                        Offset(size.width, row * spacing),
                        strokeWidth = 0.5.dp.toPx(),
                    )
                }
            }
            .border(1.dp, colors.outline.copy(alpha = 0.7f), shape)
    ) {
        content()
    }
}
