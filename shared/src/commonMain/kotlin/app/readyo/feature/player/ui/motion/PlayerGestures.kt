package app.readyo.feature.player.ui.motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.max

internal fun shouldSettlePlayer(distance: Float, velocity: Float, threshold: Float): Boolean =
    if (abs(velocity) > 900f) velocity > 0f else distance > threshold

/** Downward unconsumed list scroll dismisses only once the child reaches its top. */
@Composable
fun PlayerDragSurface(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val threshold = with(density) { 100.dp.toPx() }
    var drag by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val offset by
        animateFloatAsState(
            drag,
            if (dragging) snap() else spring(dampingRatio = .78f, stiffness = 350f),
            label = "Player drag",
        )
    val dismiss by rememberUpdatedState(onDismiss)
    fun settle(velocity: Float) {
        dragging = false
        if (shouldSettlePlayer(drag, velocity / density.density, threshold)) dismiss()
        else drag = 0f
    }
    val nested =
        remember(threshold) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (drag > 0 && available.y < 0) {
                        val used = max(available.y, -drag)
                        drag += used
                        return Offset(0f, used)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (source == NestedScrollSource.UserInput && available.y > 0) {
                        dragging = true
                        drag += available.y
                        return Offset(0f, available.y)
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (drag > 0) {
                        settle(available.y)
                        return available
                    }
                    return Velocity.Zero
                }
            }
        }
    Box(
        Modifier.fillMaxSize()
            .graphicsLayer {
                translationY = offset
                val shrink = (offset / size.height).coerceIn(0f, .08f)
                scaleX = 1f - shrink
                scaleY = 1f - shrink
                shape = RoundedCornerShape(if (offset > 0) 22.dp else 0.dp)
                clip = true
            }
            .nestedScroll(nested)
            .draggable(
                rememberDraggableState { delta ->
                    dragging = true
                    drag = (drag + delta).coerceAtLeast(0f)
                },
                Orientation.Vertical,
                onDragStarted = { dragging = true },
                onDragStopped = { settle(it) },
            )
    ) {
        content()
    }
}

@Composable
fun Modifier.swipeToOpenPlayer(open: () -> Unit): Modifier {
    var distance by remember { mutableFloatStateOf(0f) }
    val threshold = with(LocalDensity.current) { 36.dp.toPx() }
    val density = LocalDensity.current.density
    val latestOpen by rememberUpdatedState(open)
    val offset by
        animateFloatAsState(distance, spring(stiffness = 500f), label = "Mini player lift")
    return graphicsLayer { translationY = offset.coerceAtLeast(-threshold) }
        .draggable(
            rememberDraggableState { distance = (distance + it).coerceAtMost(0f) },
            Orientation.Vertical,
            onDragStopped = { velocity ->
                if (shouldSettlePlayer(-distance, -velocity / density, threshold)) latestOpen()
                distance = 0f
            },
        )
}
