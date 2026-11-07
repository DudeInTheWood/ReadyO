package app.readyo.feature.player.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun CompactTransportButton(
    symbol: TransportSymbol,
    description: String,
    primary: Boolean = false,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by
        animateFloatAsState(
            if (pressed) 0.92f else 1f,
            spring(dampingRatio = 0.7f, stiffness = 700f),
            label = "Compact transport press",
        )
    val colors = MaterialTheme.colorScheme
    val foreground = if (primary) colors.onPrimary else colors.onSurface
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactions,
        modifier =
            Modifier.size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .semantics { contentDescription = description },
        shape = RoundedCornerShape(8.dp),
        color = if (primary) colors.primary else colors.surface,
        contentColor = foreground,
        border = BorderStroke(1.dp, if (primary) colors.primary else colors.outline),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (loading) {
                CircularProgressIndicator(
                    Modifier.size(19.dp),
                    color = foreground,
                    strokeWidth = 2.dp,
                )
            } else {
                TransportGlyph(symbol, foreground.copy(alpha = if (enabled) 1f else 0.3f))
            }
        }
    }
}
