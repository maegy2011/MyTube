package com.mohamedadel.mytube.playback

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions

/**
 * Default media receiver (no Cast console registration needed).
 * Plays progressive streams — exactly what the extractor hands us.
 */
class CastOptionsProvider : OptionsProvider {
    override fun getCastOptions(appContext: Context): CastOptions {
        val notif = NotificationOptions.Builder()
            .setTargetActivityClassName("com.mohamedadel.mytube.MainActivity")
            .build()
        val media = CastMediaOptions.Builder()
            .setNotificationOptions(notif)
            // Expanded controller off: the route-button dialog already offers stop/disconnect.
            .setExpandedControllerActivityClassName("com.mohamedadel.mytube.MainActivity")
            .build()
        return CastOptions.Builder()
            .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            .setCastMediaOptions(media)
            .build()
    }

    override fun getAdditionalSessionProviders(appContext: Context): List<SessionProvider>? = null
}
