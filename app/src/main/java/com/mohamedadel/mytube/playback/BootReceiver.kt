package com.mohamedadel.mytube.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// L5 (audit P2 2026-11-11): re-arm KeepAlive after a reboot — but only when the user left
// the toggle ON. sync() is a no-op otherwise, so a reboot never resurrects a guard the
// user turned off. start() failures (background restrictions) are swallowed inside sync().
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        try { KeepAliveService.sync(context.applicationContext) } catch (_: Exception) { }
    }
}
