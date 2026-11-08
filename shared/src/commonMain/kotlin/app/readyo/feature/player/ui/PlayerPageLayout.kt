package app.readyo.feature.player.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Measures metadata and controls first, then fits the cassette into the remaining height. Each
 * child is measured once. Short screens retain a usable cassette and scroll naturally.
 */
@Composable
internal fun PlayerPageLayout(
    viewportHeight: Dp,
    modifier: Modifier = Modifier,
    cassette: @Composable () -> Unit,
    details: @Composable () -> Unit,
) {
    Layout(
        content = {
            cassette()
            details()
        },
        modifier = modifier,
    ) { children, constraints ->
        val width = constraints.maxWidth.coerceAtMost(560.dp.roundToPx())
        val padding = 16.dp.roundToPx()
        val gap = 20.dp.roundToPx()
        val info = children[1].measure(Constraints.fixedWidth(width))
        val artworkSpace = viewportHeight.roundToPx() - info.height - padding * 2 - gap
        // Artwork has a 510×362 canvas inside 12dp of panel padding on each edge.
        val panelInset = 24.dp.roundToPx()
        val fittedWidth = ((artworkSpace - panelInset) * (510f / 362f) + panelInset).toInt()
        val minWidth = 200.dp.roundToPx().coerceAtMost(width)
        val artWidth = fittedWidth.coerceIn(minWidth, width.coerceAtMost(460.dp.roundToPx()))
        val art = children[0].measure(Constraints.fixedWidth(artWidth))
        val contentHeight = art.height + info.height + gap + padding * 2
        val height = maxOf(viewportHeight.roundToPx(), contentHeight)
        val breathingRoom = (height - contentHeight) / 3
        layout(constraints.maxWidth, height) {
            art.placeRelative((constraints.maxWidth - art.width) / 2, padding + breathingRoom)
            info.placeRelative(
                (constraints.maxWidth - info.width) / 2,
                padding + breathingRoom * 2 + art.height + gap,
            )
        }
    }
}
