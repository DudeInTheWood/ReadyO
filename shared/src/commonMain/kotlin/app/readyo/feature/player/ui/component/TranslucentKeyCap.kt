package app.readyo.feature.player.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import app.readyo.core.designsystem.theme.ReadyOCassettePalette
import kotlin.math.max

/** Molded plastic over an amber lamp. All illumination stays inside the physical key. */
@Composable
internal fun TranslucentKeyCap(
    illumination: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val materials = ReadyOCassettePalette
    Box(
        modifier.clip(RoundedCornerShape(8.dp)).drawBehind {
            val radius = CornerRadius(8.dp.toPx())
            val top = lerp(materials.keyPlasticTop, materials.keyAmberTop, illumination)
            val middle = lerp(materials.keyPlasticMiddle, materials.keyAmberMiddle, illumination)
            val bottom = lerp(materials.keyPlasticBottom, materials.keyAmberBottom, illumination)
            drawRoundRect(
                Brush.verticalGradient(0f to top, 0.38f to middle, 1f to bottom),
                cornerRadius = radius,
            )
            drawRoundRect(
                Brush.radialGradient(
                    listOf(
                        materials.keyLamp.copy(alpha = illumination * 0.35f),
                        materials.keyLamp.copy(alpha = illumination * 0.08f),
                        Color.Transparent,
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    radius = max(size.width, size.height) * 0.65f,
                ),
                cornerRadius = radius,
            )
            // A broad reflection and dark lower lip give the translucent cap thickness.
            drawRoundRect(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.16f),
                    0.12f to Color.White.copy(alpha = 0.025f),
                    0.56f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.18f),
                ),
                cornerRadius = radius,
            )
            val inset = 1.dp.toPx()
            drawRoundRect(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.27f + illumination * 0.1f),
                        materials.keyLamp.copy(alpha = 0.03f + illumination * 0.22f),
                        Color.Black.copy(alpha = 0.5f),
                    )
                ),
                topLeft = Offset(inset, inset),
                size = Size(size.width - inset * 2, size.height - inset * 2),
                cornerRadius = radius,
                style = Stroke(1.dp.toPx()),
            )
            // Mold ribs at the edges stay subtle and clear of the printed legend.
            repeat(3) { rib ->
                val x = (5f + rib * 2f).dp.toPx()
                for (edge in listOf(x, size.width - x)) {
                    drawLine(
                        Color.White.copy(alpha = 0.025f + illumination * 0.025f),
                        Offset(edge, 25.dp.toPx()),
                        Offset(edge, size.height - 13.dp.toPx()),
                        strokeWidth = 0.6.dp.toPx(),
                    )
                }
            }
            // A small inset lamp window reads as hardware, including in the unlit state.
            val lampWidth = 15.dp.toPx()
            val lampPosition = Offset((size.width - lampWidth) / 2, 10.dp.toPx())
            drawRoundRect(
                Color.Black.copy(alpha = 0.45f),
                lampPosition - Offset(1.dp.toPx(), 1.dp.toPx()),
                Size(lampWidth + 2.dp.toPx(), 4.dp.toPx()),
                CornerRadius(2.dp.toPx()),
            )
            drawRoundRect(
                lerp(materials.oxideTape, materials.keyLamp, illumination),
                lampPosition,
                Size(lampWidth, 2.dp.toPx()),
                CornerRadius(1.dp.toPx()),
            )
        }
    ) {
        content()
    }
}
