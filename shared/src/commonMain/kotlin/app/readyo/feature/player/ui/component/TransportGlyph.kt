package app.readyo.feature.player.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

enum class TransportSymbol {
    Previous,
    Play,
    Pause,
    Next,
}

@Composable
internal fun TransportGlyph(symbol: TransportSymbol, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.size(22.dp)) {
        val width = size.width
        val height = size.height
        when (symbol) {
            TransportSymbol.Play,
            TransportSymbol.Next,
            TransportSymbol.Previous -> {
                val backwards = symbol == TransportSymbol.Previous
                val triangle =
                    Path().apply {
                        moveTo(width * if (backwards) 0.76f else 0.24f, height * 0.18f)
                        lineTo(width * if (backwards) 0.24f else 0.76f, height * 0.5f)
                        lineTo(width * if (backwards) 0.76f else 0.24f, height * 0.82f)
                        close()
                    }
                drawPath(triangle, color)
                if (symbol != TransportSymbol.Play) {
                    drawRect(
                        color,
                        Offset(width * if (backwards) 0.1f else 0.78f, height * 0.18f),
                        Size(width * 0.12f, height * 0.64f),
                    )
                }
            }
            TransportSymbol.Pause -> {
                drawRect(
                    color,
                    Offset(width * 0.24f, height * 0.18f),
                    Size(width * 0.18f, height * 0.64f),
                )
                drawRect(
                    color,
                    Offset(width * 0.58f, height * 0.18f),
                    Size(width * 0.18f, height * 0.64f),
                )
            }
        }
    }
}
