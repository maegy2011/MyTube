package com.mohamedadel.mytube.playback

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// L6 (audit P2 2026-11-11): PiP play/pause + close actions. Explicit intents only
// (exported=false); touches nothing but the service-owned ExoPlayer, so there is no
// Activity-lifecycle coupling. The Action list itself is (re)built by MainActivity with
// the live isPlaying state; this receiver only executes the tap.
class PipActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val p = PlaybackService.player ?: return
            when (intent?.action) {
                ACTION_PIP_TOGGLE -> if (p.isPlaying) p.pause() else p.play()
                // Close = stop playback; the user swipes the PiP window away. No Activity poke.
                ACTION_PIP_CLOSE -> { try { p.pause() } catch (_: Exception) { }; try { p.stop() } catch (_: Exception) { } }
            }
        } catch (_: Exception) { }
    }

    companion object {
        const val ACTION_PIP_TOGGLE = "com.mohamedadel.mytube.PIP_TOGGLE"
        const val ACTION_PIP_CLOSE = "com.mohamedadel.mytube.PIP_CLOSE"

        fun pendingToggle(ctx: Context): PendingIntent =
            PendingIntent.getBroadcast(
                ctx, 0, Intent(ctx, PipActionReceiver::class.java).setAction(ACTION_PIP_TOGGLE),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun pendingClose(ctx: Context): PendingIntent =
            PendingIntent.getBroadcast(
                ctx, 1, Intent(ctx, PipActionReceiver::class.java).setAction(ACTION_PIP_CLOSE),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
    }
}
