package app.readyo.feature.player.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.readyo.core.designsystem.theme.ReadyOCassettePalette
import app.readyo.feature.player.ui.motion.DeckMotion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/** Decorative needles share the retained clock; they do not measure audio amplitude. */
@Composable
fun AnalogVUMeter(motion: DeckMotion) {
    val materials = ReadyOCassettePalette
    Row(
        Modifier.fillMaxWidth().clearAndSetSemantics {
            contentDescription = "Decorative analog VU meters; not measured audio levels"
        },
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(2) { channel ->
            Column(
                Modifier.weight(1f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                materials.cassetteWindow,
                                materials.vuHighlight.copy(alpha = 0.12f),
                            )
                        ),
                        RoundedCornerShape(10.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "−20   −10    0   +3",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    color = materials.labelPaper,
                    maxLines = 1,
                )
                Canvas(Modifier.fillMaxWidth().height(42.dp)) {
                    val center = Offset(size.width / 2, size.height * 1.28f)
                    val radius = min(size.width * 0.46f, size.height * 1.15f)
                    fun point(angle: Float, distance: Float): Offset {
                        val radians = angle * PI.toFloat() / 180
                        return center + Offset(cos(radians), sin(radians)) * distance
                    }
                    drawArc(
                        materials.vuHighlight.copy(alpha = 0.5f),
                        startAngle = 210f,
                        sweepAngle = 120f,
                        useCenter = false,
                        topLeft = center - Offset(radius, radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(1.dp.toPx()),
                    )
                    repeat(13) { tick ->
                        val angle = 210f + tick * 10
                        drawLine(
                            if (tick >= 10) materials.vuHighlight else materials.labelPaper,
                            point(angle, radius * 0.92f),
                            point(angle, radius * if (tick % 3 == 0) 1.1f else 1.03f),
                            strokeWidth = 1.dp.toPx(),
                        )
                    }
                    val wave =
                        0.5f +
                            0.28f * sin(motion.seconds * 2.4f + channel * 0.7f) +
                            0.12f * sin(motion.seconds * 5.1f + channel)
                    drawLine(
                        materials.vuHighlight,
                        center,
                        point(215f + wave * 105f, radius * 1.04f),
                        strokeWidth = 1.5.dp.toPx(),
                    )
                }
                Text(
                    "VU",
                    color = materials.vuHighlight,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}
