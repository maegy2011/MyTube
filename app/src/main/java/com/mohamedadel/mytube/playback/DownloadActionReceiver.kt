package com.mohamedadel.mytube.playback

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// pause/stop 2026-11-12: download notification actions (Pause / Resume / Stop).
// Same pattern as PipActionReceiver — explicit intents only (exported=false).
// The receiver itself holds no state: MainActivity registers its live pause/resume/stop
// lambdas on DownloadActions during composition (same process, so the static registry
// is valid); taps from a dead process are safely ignored (null handlers = no-op).
object DownloadActions {
    var onPause: ((String) -> Unit)? = null
    var onResume: ((String) -> Unit)? = null
    var onStop: ((String) -> Unit)? = null
}

class DownloadActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val key = intent?.getStringExtra(EXTRA_KEY) ?: return
            when (intent.action) {
                ACTION_DL_PAUSE -> try { DownloadActions.onPause?.invoke(key) } catch (_: Exception) { }
                ACTION_DL_RESUME -> try { DownloadActions.onResume?.invoke(key) } catch (_: Exception) { }
                ACTION_DL_STOP -> try { DownloadActions.onStop?.invoke(key) } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    companion object {
        const val ACTION_DL_PAUSE = "com.mohamedadel.mytube.DL_PAUSE"
        const val ACTION_DL_RESUME = "com.mohamedadel.mytube.DL_RESUME"
        const val ACTION_DL_STOP = "com.mohamedadel.mytube.DL_STOP"
        const val EXTRA_KEY = "key"

        private fun pending(ctx: Context, action: String, key: String, slot: Int): PendingIntent =
            PendingIntent.getBroadcast(
                ctx, key.hashCode() * 31 + slot,
                Intent(ctx, DownloadActionReceiver::class.java).setAction(action).putExtra(EXTRA_KEY, key),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        fun pendingPause(ctx: Context, key: String): PendingIntent = pending(ctx, ACTION_DL_PAUSE, key, 1)
        fun pendingResume(ctx: Context, key: String): PendingIntent = pending(ctx, ACTION_DL_RESUME, key, 2)
        fun pendingStop(ctx: Context, key: String): PendingIntent = pending(ctx, ACTION_DL_STOP, key, 3)
    }
}
