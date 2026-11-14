package com.mohamedadel.mytube.playback

import android.content.Context
import android.net.Uri
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheWriter

// v0.4.6 (audio prefetch): CacheWriter downloads the up-next audio stream
// bytes into the shared MediaCache on a background thread. Runs only when
// the caller gates it (pref on + audio-first + Wi-Fi-ok) — never auto-runs.
object AudioPrefetch {
    @Volatile var lastResult: String = "idle"
        private set

    fun cacheAudioBytes(ctx: Context, audioUrl: String) {
        try {
            if (audioUrl.isBlank()) { lastResult = "empty-url"; return }
            val app = ctx.applicationContext
            val cache = MediaCache.get(app)
            // Skip if already fully cached (keyed lookup is best-effort).
            val upstream = DefaultHttpDataSource.Factory().createDataSource()
            val spec = DataSpec(Uri.parse(audioUrl))
            CacheWriter(cache, upstream, spec, null).cache()
            lastResult = "ok"
        } catch (e: Exception) {
            lastResult = "fail:" + (e.message ?: "?").take(60)
        }
    }
}
