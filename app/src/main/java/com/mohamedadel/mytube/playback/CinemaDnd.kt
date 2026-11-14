package com.mohamedadel.mytube.playback

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

// v0.4.6 (cinema DND): silence notifications while fullscreen video plays,
// restore on exit. Needs Notification-Policy access (granted by the user in
// system settings); without it this is a silent no-op — never crashes.
object CinemaDnd {
    fun isGranted(ctx: Context): Boolean {
        return try {
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.isNotificationPolicyAccessGranted == true
        } catch (_: Exception) { false }
    }

    fun accessIntent(): Intent {
        return try {
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        } catch (_: Exception) {
            Intent(Settings.ACTION_SETTINGS)
        }
    }

    fun enter(ctx: Context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (!nm.isNotificationPolicyAccessGranted) return
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        } catch (_: Exception) { }
    }

    fun exit(ctx: Context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
            val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            if (!nm.isNotificationPolicyAccessGranted) return
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        } catch (_: Exception) { }
    }

    // Single entry point for fullscreen transitions (called from setFullscreen).
    fun onFullscreen(full: Boolean, ctx: Context, enabled: Boolean) {
        try {
            if (!enabled) return
            if (full) enter(ctx) else exit(ctx)
        } catch (_: Exception) { }
    }
}
