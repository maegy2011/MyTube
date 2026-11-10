package com.mohamedadel.mytube.playback

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.mediarouter.media.MediaRouteSelector
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.common.images.WebImage
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener

/**
 * Chromecast sender (runtime layer). Best-effort throughout: devices without
 * Google Play Services get [available] = false and no cast UI at all.
 * Local ExoPlayer stays the owner — MainActivity pauses it when a remote
 * load succeeds and resumes locally when the session ends (user taps play).
 */
object CastManager {
    val available = mutableStateOf(false)
    val connected = mutableStateOf(false)

    /** Fired on the main thread when a Cast session starts/ends. Set by MainActivity. */
    var onSessionStarted: (() -> Unit)? = null
    var onSessionEnded: (() -> Unit)? = null

    private var appCtx: Context? = null

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            connected.value = true
            try { onSessionStarted?.invoke() } catch (_: Exception) { }
        }
        override fun onSessionEnded(session: CastSession, error: Int) {
            connected.value = false
            try { onSessionEnded?.invoke() } catch (_: Exception) { }
        }
        override fun onSessionStartFailed(session: CastSession, error: Int) { }
        override fun onSessionEnding(session: CastSession) { }
        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            connected.value = true
        }
        override fun onSessionResumeFailed(session: CastSession, error: Int) { }
        override fun onSessionSuspended(session: CastSession, reason: Int) {
            connected.value = false
        }
        override fun onSessionStarting(session: CastSession) { }
        override fun onSessionResuming(session: CastSession, sessionId: String) { }
    }

    /** Call once from Application.onCreate. Returns false when GMS/Cast is absent. */
    fun init(app: Context): Boolean {
        return try {
            val cc = CastContext.getSharedInstance(app.applicationContext)
            appCtx = app.applicationContext
            cc.sessionManager.addSessionManagerListener(sessionListener, CastSession::class.java)
            connected.value = try {
                cc.sessionManager.currentCastSession?.isConnected == true
            } catch (_: Exception) { false }
            available.value = true
            true
        } catch (_: Exception) {
            available.value = false
            false
        }
    }

    fun selector(): MediaRouteSelector? {
        return try {
            val c = appCtx ?: return null
            CastContext.getSharedInstance(c).mergedSelector
        } catch (_: Exception) { null }
    }

    /**
     * Load the currently resolved stream URL on the receiver (default media
     * receiver — progressive MP3/AAC/MP4 only, no HLS/DASH, no auth headers).
     * Fire-and-forget IPC; returns false when nothing was sent.
     */
    fun load(url: String?, title: String?, thumb: String?, mime: String, positionMs: Long, autoplay: Boolean): Boolean {
        if (url.isNullOrBlank()) return false
        return try {
            val c = appCtx ?: return false
            val client = CastContext.getSharedInstance(c)
                .sessionManager.currentCastSession?.remoteMediaClient ?: return false
            val md = MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC).apply {
                putString(MediaMetadata.KEY_TITLE, title?.takeIf { it.isNotBlank() } ?: "MyTube")
                try {
                    if (!thumb.isNullOrBlank()) addImage(WebImage(Uri.parse(thumb)))
                } catch (_: Exception) { }
            }
            val info = MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(mime)
                .setMetadata(md)
                .build()
            val req = MediaLoadRequestData.Builder()
                .setMediaInfo(info)
                .setAutoplay(autoplay)
                .setCurrentTime(positionMs)
                .build()
            client.load(req)
            true
        } catch (_: Exception) { false }
    }

    fun endSession() {
        try {
            val c = appCtx ?: return
            CastContext.getSharedInstance(c).sessionManager.endCurrentSession(true)
        } catch (_: Exception) { }
    }
}
