package app.readyo.feature.player.ui.motion

import kotlin.test.*

class PlayerGestureTest {
    @Test
    fun shortDragsSnapBackAndLongDragsCommit() {
        assertFalse(shouldSettlePlayer(30f, 0f, 100f))
        assertTrue(shouldSettlePlayer(120f, 0f, 100f))
        assertFalse(shouldSettlePlayer(0f, 0f, 100f))
    }

    @Test
    fun deliberateFlickWinsOverDistanceAndReversedFlickCancels() {
        assertTrue(shouldSettlePlayer(15f, 1100f, 100f))
        assertFalse(shouldSettlePlayer(130f, -1100f, 100f))
        assertFalse(shouldSettlePlayer(-30f, -300f, 100f))
    }
}
