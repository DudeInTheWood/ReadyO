package app.readyo.core.playback

import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player

/** System Stop ends listening; Pause remains resumable. */
class SessionPlayer(player: Player) : ForwardingPlayer(player) {
    override fun stop() {
        super.stop()
        super.clearMediaItems()
    }
}
