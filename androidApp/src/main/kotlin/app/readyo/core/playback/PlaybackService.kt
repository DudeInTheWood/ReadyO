package app.readyo.core.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import app.readyo.MainActivity
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

@androidx.annotation.OptIn(UnstableApi::class)
class PlaybackService : MediaSessionService() {
    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        // Do not retain foreground-service status for a paused player.
        setForegroundServiceTimeoutMs(0)
        val player =
            ExoPlayer.Builder(this).build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true,
                )
                setHandleAudioBecomingNoisy(true)
            }
        val sessionPlayer = SessionPlayer(player)
        sessionPlayer.addListener(
            object : Player.Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    if (player.mediaItemCount == 0 && player.playbackState == Player.STATE_IDLE) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
        )
        val activity =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        session =
            MediaSession.Builder(this, sessionPlayer)
                .setSessionActivity(activity)
                .setCallback(
                    object : MediaSession.Callback {
                        override fun onConnect(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo,
                        ): MediaSession.ConnectionResult {
                            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                                .setAvailableSessionCommands(
                                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                                        .buildUpon()
                                        .add(STOP_SESSION)
                                        .build()
                                )
                                .build()
                        }

                        override fun onCustomCommand(
                            session: MediaSession,
                            controller: MediaSession.ControllerInfo,
                            customCommand: SessionCommand,
                            args: Bundle,
                        ): ListenableFuture<SessionResult> {
                            if (customCommand.customAction == STOP_SESSION.customAction) {
                                session.player.stop()
                                stopForeground(STOP_FOREGROUND_REMOVE)
                                stopSelf()
                                return Futures.immediateFuture(
                                    SessionResult(SessionResult.RESULT_SUCCESS)
                                )
                            }
                            return super.onCustomCommand(session, controller, customCommand, args)
                        }
                    }
                )
                .build()
                .also { mediaSession ->
                    mediaSession.setMediaButtonPreferences(
                        listOf(
                            CommandButton.Builder(CommandButton.ICON_STOP)
                                .setDisplayName("Stop and dismiss")
                                .setSessionCommand(STOP_SESSION)
                                .build()
                        )
                    )
                }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val result = super.onStartCommand(intent, flags, startId)
        if (
            intent?.getBooleanExtra(MediaNotification.NOTIFICATION_DISMISSED_EVENT_KEY, false) ==
                true
        ) {
            // Dismissal ends the session's content; closing the Activity does not.
            session?.player?.run {
                stop()
                clearMediaItems()
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return result
    }

    override fun onDestroy() {
        session?.run {
            player.release()
            release()
        }
        session = null
        super.onDestroy()
    }

    companion object {
        private val STOP_SESSION = SessionCommand("app.readyo.STOP_AND_DISMISS", Bundle.EMPTY)
    }
}
