package com.mohamedadel.mytube.data.local

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mohamedadel.mytube.MainActivity
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.VideoItem

// Notification bell + download-complete notifications (2026-11-12, runtime layer).
// - recordFresh(): shared by FeedRefreshWorker and foreground refreshFeed — stores newly
//   seen feed items in Room with timestamp + read flag. First-ever run seeds the baseline
//   as read (no badge flood for the existing backlog); later runs mark novelties unread.
// - Download terminal notifications live here too: one immutable channel created once,
//   tap = PendingIntent (FLAG_IMMUTABLE) opening the player for that pin.
object NotifCenter {
    const val DONE_CHANNEL = "mytube_pin_done"
    const val EXTRA_PIN_KEY = "mytube_pin_key"

    suspend fun recordFresh(dao: PlaylistDao, items: List<VideoItem>) {
        try {
            if (items.isEmpty()) return
            val baseline = try { dao.notificationCount() == 0 } catch (_: Exception) { false }
            val rows = items.map { v ->
                NotificationItemEntity(
                    videoKey = v.favKey(),
                    videoId = v.id,
                    title = v.title,
                    channel = v.channel,
                    thumbnail = v.thumbnail,
                    url = v.url,
                    durationSec = v.durationSec,
                    viewCount = v.viewCount,
                    read = baseline
                )
            }
            try { dao.insertNotifications(rows) } catch (_: Exception) { }
            try { dao.trimNotifications() } catch (_: Exception) { }
        } catch (_: Exception) { }
    }

    fun NotificationItemEntity.toVideoItem(): VideoItem = VideoItem(
        id = videoId, title = title, channel = channel, thumbnail = thumbnail,
        durationSec = durationSec, viewCount = viewCount, url = url
    )

    private fun ensureDoneChannel(ctx: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
                // Immutable after creation (except by the user): create once, never update.
                if (nm.getNotificationChannel(DONE_CHANNEL) == null) {
                    nm.createNotificationChannel(
                        android.app.NotificationChannel(
                            DONE_CHANNEL,
                            ctx.getString(R.string.pin_done_channel),
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                    )
                }
            }
        } catch (_: Exception) { }
    }

    fun playerIntent(app: Context, key: String): PendingIntent {
        val i = Intent(app, MainActivity::class.java)
            .setAction("com.mohamedadel.mytube.OPEN_PIN")
            .putExtra(EXTRA_PIN_KEY, key)
        return PendingIntent.getActivity(
            app, key.hashCode(),
            i, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun postPinDone(app: Context, key: String, title: String?) {
        try {
            ensureDoneChannel(app)
            val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val b = NotificationCompat.Builder(app, DONE_CHANNEL)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(app.getString(R.string.pin_done_title))
                .setContentText(title ?: app.getString(R.string.pin_done_title))
                .setContentIntent(playerIntent(app, key))
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
            nm.notify(3000 + (key.hashCode() and 0xFFF), b.build())
        } catch (_: Exception) { }
    }

    // Real error reason for the failure notification (no silent failures).
    // Codes come from PinManager.takeError(); raw "ex:" detail passes through.
    fun reasonText(app: Context, code: String?): String = when {
        code.isNullOrBlank() || code == "plan" -> app.getString(R.string.pin_err_unknown)
        code == "short" -> app.getString(R.string.pin_err_short)
        code == "empty" -> app.getString(R.string.pin_err_empty)
        code == "io" -> app.getString(R.string.pin_err_io)
        code.startsWith("http") -> app.getString(R.string.pin_err_http, code.removePrefix("http"))
        code.startsWith("ex:") -> code.removePrefix("ex:")
        else -> code
    }

    fun postPinFail(app: Context, key: String, title: String?, reason: String?) {
        try {
            ensureDoneChannel(app)
            val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val b = NotificationCompat.Builder(app, DONE_CHANNEL)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(app.getString(R.string.pin_fail_title))
                .setContentText(
                    if (reason.isNullOrBlank()) title ?: app.getString(R.string.pin_fail_title)
                    else app.getString(R.string.pin_fail_fmt, reasonText(app, reason))
                )
                .setContentIntent(playerIntent(app, key))
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
            nm.notify(4000 + (key.hashCode() and 0xFFF), b.build())
        } catch (_: Exception) { }
    }
}
