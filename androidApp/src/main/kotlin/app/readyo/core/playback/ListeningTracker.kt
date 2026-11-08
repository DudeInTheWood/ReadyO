package app.readyo.core.playback

import android.content.Context
import android.os.SystemClock
import androidx.media3.common.C
import androidx.media3.common.Player
import app.readyo.feature.music.data.ListeningHistoryStore
import kotlinx.coroutines.*

/** Count audible wall time, so seeking never manufactures listening or completions. */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
internal class ListeningTracker(context: Context, private val player: Player) : Player.Listener {
    private val store = ListeningHistoryStore(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var id: String? = null
    private var lastTick = SystemClock.elapsedRealtime()
    private var wasPlaying = false
    private var elapsed = 0L
    private var pending = 0L
    private var countedPlay = false
    private var completed = false
    private var duration = 0L
    private var automaticTransition = false

    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
        automaticTransition =
            reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO ||
                reason == Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
    }

    init {
        player.addListener(this)
        scope.launch {
            while (isActive) {
                delay(5_000)
                update()
                flush()
            }
        }
    }

    private fun update() {
        val now = SystemClock.elapsedRealtime()
        if (wasPlaying && id != null) {
            val delta = (now - lastTick).coerceAtLeast(0)
            elapsed += delta
            pending += delta
        }
        lastTick = now
        if (player.currentMediaItem?.mediaId == id) duration = player.duration.coerceAtLeast(0)
        wasPlaying = player.isPlaying && id != null
    }

    private fun flush() {
        val track = id ?: return
        val finish = !completed && duration > 0 && elapsed >= duration * 9 / 10
        if (pending > 0 || finish) {
            store.record(track, pending, finish, false, !countedPlay)
            countedPlay = true
            completed = completed || finish
            pending = 0
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        update()
        val item = player.currentMediaItem
        val nextId =
            item?.takeUnless { it.mediaMetadata.extras?.getBoolean("radio") == true }?.mediaId
        if (nextId != id || events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION)) {
            flush()
            if (
                id != null &&
                    elapsed in 1..29_999 &&
                    !completed &&
                    events.contains(Player.EVENT_MEDIA_ITEM_TRANSITION) &&
                    player.playbackState != Player.STATE_ENDED &&
                    !automaticTransition
            ) {
                store.record(id!!, 0, false, true, false)
            }
            id = nextId
            elapsed = 0
            pending = 0
            countedPlay = false
            completed = false
            duration = player.duration.takeIf { it != C.TIME_UNSET }?.coerceAtLeast(0) ?: 0
        }
        wasPlaying = player.isPlaying && id != null
        automaticTransition = false
        if (!wasPlaying) flush()
    }

    fun close() {
        update()
        flush()
        player.removeListener(this)
        scope.cancel()
    }
}
