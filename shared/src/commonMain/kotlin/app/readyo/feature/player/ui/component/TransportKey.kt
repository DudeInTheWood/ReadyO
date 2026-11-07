package app.readyo.feature.player.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.theme.ReadyOCassettePalette
import kotlinx.coroutines.delay

@Composable
fun TransportKey(
    label: String,
    symbol: TransportSymbol,
    illuminated: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    action: () -> Unit,
) {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    var clickSequence by remember { mutableIntStateOf(0) }
    var flashing by remember { mutableStateOf(false) }
    LaunchedEffect(clickSequence) {
        if (clickSequence > 0) {
            flashing = true
            delay(260)
            flashing = false
        }
    }

    val lit = pressed || flashing || illuminated
    val illumination by
        animateFloatAsState(
            if (lit) 1f else 0f,
            tween(if (lit) 100 else 420, easing = FastOutSlowInEasing),
            label = "Incandescent key light",
        )
    val travel by
        animateFloatAsState(
            if (pressed) 3f else if (illuminated) 1f else 0f,
            spring(dampingRatio = 0.75f, stiffness = 750f),
            label = "Radio key travel",
        )
    val scale by
        animateFloatAsState(
            if (pressed) 0.98f else 1f,
            spring(dampingRatio = 0.7f, stiffness = 700f),
            label = "Radio key return",
        )
    val materials = ReadyOCassettePalette
    val legend =
        lerp(materials.keyLegend, materials.keyLegendLit, illumination)
            .copy(alpha = if (enabled) 1f else 0.4f)

    Surface(
        onClick = {
            clickSequence++
            action()
        },
        enabled = enabled,
        modifier = modifier.semantics { contentDescription = label },
        shape = RoundedCornerShape(11.dp),
        color = materials.cassetteWindow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)),
        interactionSource = interactions,
    ) {
        TranslucentKeyCap(
            illumination = illumination,
            modifier =
                Modifier.padding(start = 3.dp, end = 3.dp, top = 3.dp, bottom = 6.dp)
                    .graphicsLayer {
                        translationY = travel * density
                        scaleX = scale
                        scaleY = scale
                        shadowElevation = (4f - travel).coerceAtLeast(0f) * density
                        shape = RoundedCornerShape(8.dp)
                    },
        ) {
            Column(
                Modifier.fillMaxWidth()
                    .heightIn(min = 84.dp)
                    .padding(start = 10.dp, end = 10.dp, top = 22.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                TransportGlyph(symbol, legend)
                Text(
                    label.uppercase(),
                    color = legend,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    letterSpacing = 0.6.sp,
                )
            }
        }
    }
}
