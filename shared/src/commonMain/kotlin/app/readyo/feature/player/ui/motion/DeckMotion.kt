package app.readyo.feature.player.ui.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * Retained phase means pause/resume never resets the spindle angle. Read in draw, not composition.
 */
@Stable
class DeckMotion {
    var seconds by mutableFloatStateOf(0f)
        internal set
}

@Composable
fun rememberDeckMotion(playing: Boolean, enabled: Boolean): DeckMotion {
    val motion = remember { DeckMotion() }
    val window = LocalWindowInfo.current
    val scale = rememberCoroutineScope().coroutineContext[MotionDurationScale]
    LaunchedEffect(playing, enabled, window.isWindowFocused) {
        if (playing && enabled && window.isWindowFocused) {
            var last = 0L
            while (true) withFrameNanos { now ->
                if (last != 0L && (scale?.scaleFactor ?: 1f) > 0f)
                    motion.seconds += (now - last).coerceAtMost(50_000_000L) / 1_000_000_000f
                last = now
            }
        }
    }
    return motion
}
