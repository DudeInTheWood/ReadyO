package app.readyo.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun ReadyOBottomNavigation(radio: Boolean, onSelect: (Boolean) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val itemColors =
        NavigationBarItemDefaults.colors(
            selectedIconColor = colors.primary,
            selectedTextColor = colors.primary,
            indicatorColor = Color.Transparent,
            unselectedIconColor = colors.onSurfaceVariant,
            unselectedTextColor = colors.onSurfaceVariant,
        )
    NavigationBar(
        modifier =
            Modifier.drawBehind {
                drawLine(colors.outlineVariant, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx())
            },
        containerColor = colors.surface,
        tonalElevation = 0.dp,
    ) {
        NavigationBarItem(
            selected = radio,
            onClick = { onSelect(true) },
            icon = { NavigationMark(radio = true, selected = radio) },
            label = { Text("Radio") },
            colors = itemColors,
        )
        NavigationBarItem(
            selected = !radio,
            onClick = { onSelect(false) },
            icon = { NavigationMark(radio = false, selected = !radio) },
            label = { Text("My Music") },
            colors = itemColors,
        )
    }
}

@Composable
private fun NavigationMark(radio: Boolean, selected: Boolean) {
    val colors = MaterialTheme.colorScheme
    val ink = if (selected) colors.primary else colors.onSurfaceVariant
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Canvas(Modifier.size(25.dp)) {
            val stroke = Stroke(1.7.dp.toPx())
            if (radio) {
                drawRoundRect(
                    ink,
                    Offset(size.width * 0.08f, size.height * 0.3f),
                    Size(size.width * 0.84f, size.height * 0.6f),
                    CornerRadius(3.dp.toPx()),
                    style = stroke,
                )
                drawLine(
                    ink,
                    Offset(size.width * 0.22f, size.height * 0.3f),
                    Offset(size.width * 0.7f, size.height * 0.06f),
                    1.7.dp.toPx(),
                )
                drawCircle(
                    ink,
                    size.width * 0.12f,
                    Offset(size.width * 0.34f, size.height * 0.6f),
                    style = stroke,
                )
                drawLine(
                    ink,
                    Offset(size.width * 0.64f, size.height * 0.52f),
                    Offset(size.width * 0.78f, size.height * 0.52f),
                    1.7.dp.toPx(),
                )
            } else {
                drawLine(
                    ink,
                    Offset(size.width * 0.38f, size.height * 0.7f),
                    Offset(size.width * 0.38f, size.height * 0.22f),
                    1.7.dp.toPx(),
                )
                drawLine(
                    ink,
                    Offset(size.width * 0.8f, size.height * 0.6f),
                    Offset(size.width * 0.8f, size.height * 0.12f),
                    1.7.dp.toPx(),
                )
                drawLine(
                    ink,
                    Offset(size.width * 0.38f, size.height * 0.22f),
                    Offset(size.width * 0.8f, size.height * 0.12f),
                    2.5.dp.toPx(),
                )
                drawOval(
                    ink,
                    Offset(size.width * 0.12f, size.height * 0.62f),
                    Size(size.width * 0.28f, size.height * 0.22f),
                )
                drawOval(
                    ink,
                    Offset(size.width * 0.54f, size.height * 0.52f),
                    Size(size.width * 0.28f, size.height * 0.22f),
                )
            }
        }
        Box(
            Modifier.width(26.dp)
                .height(2.dp)
                .background(if (selected) colors.primary else Color.Transparent)
        )
    }
}
