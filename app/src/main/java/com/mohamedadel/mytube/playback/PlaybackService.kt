package com.mohamedadel.mytube.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mohamedadel.mytube.MainActivity

@UnstableApi
class PlaybackService : MediaSessionService() {
    companion object {
        var player: ExoPlayer? = null
            private set
        var session: MediaSession? = null
            private set
        const val CHANNEL_ID = "mytube_playback"
        // custom notification / lock-screen actions, routed to the in-app queue by MainActivity
        const val ACTION_PREV = "mytube_prev"
        const val ACTION_NEXT = "mytube_next"
        var onCustomAction: ((String) -> Unit)? = null
    }

    // forwards notification / lock-screen prev+next taps to whoever owns the queue (MainActivity)
    private inner class QueueCallback : MediaSession.Callback {
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            try { onCustomAction?.invoke(customCommand.customAction) } catch (_: Exception) { }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Playback", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Media playback controls"
                setShowBadge(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        val provider = DefaultMediaNotificationProvider.Builder(this)
            .setChannelId(CHANNEL_ID)
            .build()
        setMediaNotificationProvider(provider)
        val p = ExoPlayer.Builder(this).build()
        player = p
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivity = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val sess = MediaSession.Builder(this, p)
            .setSessionActivity(sessionActivity)
            .setCallback(QueueCallback())
            .build()
        // prev/next buttons in the notification and on the lock screen (custom layout renders as notification actions)
        sess.setCustomLayout(
            listOf(
                CommandButton.Builder(CommandButton.ICON_PREVIOUS)
                    .setDisplayName("Previous")
                    .setSessionCommand(SessionCommand(ACTION_PREV, Bundle.EMPTY))
                    .build(),
                CommandButton.Builder(CommandButton.ICON_NEXT)
                    .setDisplayName("Next")
                    .setSessionCommand(SessionCommand(ACTION_NEXT, Bundle.EMPTY))
                    .build()
            )
        )
        session = sess
        addSession(sess)
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = session
    override fun onDestroy() {
        session?.let { removeSession(it) }
        session?.release()
        session = null
        player?.release()
        player = null
        super.onDestroy()
    }
}