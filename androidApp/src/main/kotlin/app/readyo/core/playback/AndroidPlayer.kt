package app.readyo.core.playback

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import app.readyo.core.model.AudioItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AndroidPlayer(context: Context) : PlayerController {
    private val mutableState = MutableStateFlow(PlayerState())
    override val state = mutableState.asStateFlow()
    private var controller: MediaController? = null
    private var playbackContext = emptyList<AudioItem>()
    private var pendingSelection: (() -> Unit)? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val controllerFuture =
        MediaController.Builder(
                context,
                SessionToken(context, ComponentName(context, PlaybackService::class.java)),
            )
            .buildAsync()

    init {
        controllerFuture.addListener(
            {
                try {
                    controller =
                        controllerFuture.get().also { player ->
                            player.addListener(
                                object : Player.Listener {
                                    override fun onEvents(player: Player, events: Player.Events) {
                                        updatePlaybackState()
                                    }

                                    override fun onPlayerError(error: PlaybackException) {
                                        mutableState.value =
                                            mutableState.value.copy(
                                                status = PlaybackStatus.Error,
                                                error =
                                                    "This source couldn't play. Check your connection or try another file or station.",
                                            )
                                    }
                                }
                            )
                        }
                    pendingSelection?.invoke()
                    pendingSelection = null
                    updatePlaybackState()
                } catch (exception: Exception) {
                    mutableState.value =
                        mutableState.value.copy(
                            status = PlaybackStatus.Error,
                            error = "The audio service couldn't start.",
                        )
                }
            },
            ContextCompat.getMainExecutor(context),
        )
        scope.launch {
            while (isActive) {
                updatePlaybackState()
                delay(500)
            }
        }
    }

    private fun updatePlaybackState() {
        val player = controller ?: return
        if (player.mediaItemCount == 0 && pendingSelection == null) {
            mutableState.value = PlayerState()
            return
        }
        if (player.playerError != null) return
        val id = player.currentMediaItem?.mediaId
        val media = player.currentMediaItem
        val item =
            playbackContext.find { it.id == id }
                ?: media?.let {
                    AudioItem(
                        it.mediaId,
                        it.mediaMetadata.title?.toString().orEmpty(),
                        it.mediaMetadata.artist?.toString().orEmpty(),
                        it.localConfiguration?.uri?.toString().orEmpty(),
                        it.mediaMetadata.extras?.getBoolean("radio") ?: false,
                    )
                }
        if (item?.radio == true && playbackContext.isEmpty()) {
            val savedContext = media?.mediaMetadata?.extras?.getString(RADIO_CONTEXT_KEY)
            playbackContext =
                savedContext
                    ?.let { encoded ->
                        runCatching { Json.decodeFromString<List<AudioItem>>(encoded) }.getOrNull()
                    }
                    .orEmpty()
        }
        mutableState.value =
            PlayerState(
                item,
                when {
                    player.playbackState == Player.STATE_BUFFERING -> PlaybackStatus.Buffering
                    player.isPlaying -> PlaybackStatus.Playing
                    player.playbackState == Player.STATE_ENDED -> PlaybackStatus.Ended
                    item != null -> PlaybackStatus.Paused
                    else -> PlaybackStatus.Idle
                },
                player.currentPosition.coerceAtLeast(0),
                player.duration.coerceAtLeast(0),
                canSkipNext =
                    if (item?.radio == true) playbackContext.size > 1
                    else player.hasNextMediaItem(),
            )
    }

    override fun select(items: List<AudioItem>, index: Int) {
        if (items.isEmpty() || index !in items.indices) return
        // One service-owned ExoPlayer replaces its media list atomically on source switch.
        playbackContext = items
        val selected = items[index]
        val radioContext = if (selected.radio) Json.encodeToString(items) else null
        mutableState.value =
            PlayerState(
                selected,
                PlaybackStatus.Connecting,
                canSkipNext = if (selected.radio) items.size > 1 else index < items.lastIndex,
            )
        val action = {
            controller?.run {
                pause()
                val playlist = if (selected.radio) listOf(selected) else items
                setMediaItems(
                    playlist.map { item ->
                        MediaItem.Builder()
                            .setMediaId(item.id)
                            .setUri(item.uri)
                            .setMediaMetadata(
                                MediaMetadata.Builder()
                                    .setTitle(item.title)
                                    .setArtist(item.subtitle)
                                    .setExtras(
                                        Bundle().apply {
                                            putBoolean("radio", item.radio)
                                            if (radioContext != null)
                                                putString(RADIO_CONTEXT_KEY, radioContext)
                                        }
                                    )
                                    .build()
                            )
                            .build()
                    },
                    if (selected.radio) 0 else index,
                    0,
                )
                prepare()
                play()
            }
            Unit
        }
        if (controller == null) pendingSelection = action else action()
    }

    override fun toggle() {
        controller?.run {
            if (playerError != null || playbackState == Player.STATE_IDLE) {
                prepare()
                play()
            } else if (isPlaying) pause()
            else {
                if (playbackState == Player.STATE_ENDED) seekTo(0)
                play()
            }
        }
    }

    override fun seek(position: Long) {
        if (mutableState.value.item?.radio == false) controller?.seekTo(position)
    }

    override fun next() {
        skip(1)
    }

    override fun previous() {
        skip(-1)
    }

    private fun skip(delta: Int) {
        val item = mutableState.value.item ?: return
        if (item.radio) {
            val index = playbackContext.indexOfFirst { it.id == item.id }
            if (playbackContext.isNotEmpty())
                select(
                    playbackContext,
                    (index + delta + playbackContext.size) % playbackContext.size,
                )
        } else
            controller?.run {
                if (delta > 0 && hasNextMediaItem()) seekToNextMediaItem()
                else if (delta < 0) {
                    if (currentPosition > 3000) seekTo(0)
                    else if (hasPreviousMediaItem()) seekToPreviousMediaItem()
                }
            }
    }

    fun close() {
        scope.cancel()
        MediaController.releaseFuture(controllerFuture)
    }

    companion object {
        private const val RADIO_CONTEXT_KEY = "app.readyo.RADIO_CONTEXT"
    }
}
