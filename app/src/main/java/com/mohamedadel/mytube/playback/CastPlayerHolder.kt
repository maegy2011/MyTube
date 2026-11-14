package com.mohamedadel.mytube.playback

import android.content.Context
import android.net.Uri
import androidx.media3.cast.CastPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.google.android.gms.cast.framework.CastContext

/**
 * ext-cast integration (2026-11-14): official `androidx.media3:media3-cast`
 * (same 1.4.1 line as the rest of our Media3 stack).
 *
 * CastPlayer implements the standard Player API backed by the Cast session:
 * position/state tracking comes free, and manifest URLs (HLS/DASH) are handed
 * to the receiver directly instead of resolved progressive bytes. Falls back
 * to the raw RemoteMediaClient path in CastManager when unavailable.
 */
object CastPlayerHolder {
    private var player: CastPlayer? = null

    /** Create the CastPlayer once Cast services are up. Idempotent. */
    fun ensure(app: Context): Boolean {
        if (player != null) return true
        return try {
            val cc = CastContext.getSharedInstance(app.applicationContext)
            player = CastPlayer(cc)
            true
        } catch (_: Exception) {
            player = null
            false
        }
    }

    /**
     * Load a snapshot URL on the receiver through the Player API.
     * Returns false when there is no player (caller falls back to CastManager.load).
     */
    fun playUrl(
        url: String,
        mime: String,
        title: String?,
        thumb: String?,
        positionMs: Long,
        autoplay: Boolean
    ): Boolean {
        val p = player ?: return false
        if (url.isBlank()) return false
        return try {
            val md = MediaMetadata.Builder()
                .setTitle(title?.takeIf { it.isNotBlank() } ?: "MyTube")
                .setArtworkUri(thumb?.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })
                .build()
            val item = MediaItem.Builder()
                .setUri(Uri.parse(url))
                .setMimeType(mime.takeIf { it.isNotBlank() })
                .setMediaMetadata(md)
                .build()
            p.setMediaItem(item)
            p.prepare()
            if (positionMs > 0) {
                try { p.seekTo(positionMs) } catch (_: Exception) { }
            }
            if (autoplay) p.play() else p.pause()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun stop() {
        try { player?.stop() } catch (_: Exception) { }
    }

    fun release() {
        try { player?.release() } catch (_: Exception) { }
        player = null
    }
}
