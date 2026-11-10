package com.mohamedadel.mytube.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mohamedadel.mytube.MainActivity
import com.mohamedadel.mytube.R

// Keep-alive guard against aggressive OEM battery killers (Oppo/Vivo/Xiaomi/Samsung/Huawei).
// A lightweight foreground service holding a LOW-importance ongoing notification: the process
// stays in the foreground bucket, so the OEM task killer skips it while audio plays in background.
// Default OFF (settings toggle pref_keepalive) — it costs a persistent icon by design.
// No media, no network, no location: just presence. Stops itself when toggled off.
class KeepAliveService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, getString(R.string.keepalive_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.keepalive_channel_desc)
                setShowBadge(false)
            }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            try {
                getSharedPreferences("mytube_prefs", MODE_PRIVATE).edit()
                    .putBoolean("pref_keepalive", false).apply()
            } catch (_: Exception) { }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        val openIntent = Intent(this, MainActivity::class.java)
        openIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val open = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = PendingIntent.getService(
            this, 1, Intent(this, KeepAliveService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notif: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.keepalive_title))
            .setContentText(getString(R.string.keepalive_text))
            .setContentIntent(open)
            .addAction(0, getString(R.string.keepalive_stop), stopIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
        startForeground(NOTIF_ID, notif)
        return START_STICKY
    }

    companion object {
        const val CHANNEL_ID = "mytube_keepalive"
        const val NOTIF_ID = 1001
        const val ACTION_STOP = "com.mohamedadel.mytube.STOP_KEEPALIVE"

        fun start(ctx: Context) {
            try {
                val i = Intent(ctx.applicationContext, KeepAliveService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ctx.applicationContext.startForegroundService(i)
                } else {
                    ctx.applicationContext.startService(i)
                }
            } catch (_: Exception) { }
        }

        fun stop(ctx: Context) {
            try { ctx.applicationContext.stopService(Intent(ctx.applicationContext, KeepAliveService::class.java)) } catch (_: Exception) { }
        }

        fun sync(ctx: Context) {
            val on = try {
                ctx.getSharedPreferences("mytube_prefs", MODE_PRIVATE).getBoolean("pref_keepalive", false)
            } catch (_: Exception) { false }
            if (on) start(ctx) else stop(ctx)
        }
    }
}
