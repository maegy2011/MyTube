package com.mohamedadel.mytube.playback

import android.content.Intent
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    companion object {
        var player: ExoPlayer? = null
            private set
        var session: MediaSession? = null
            private set
    }
    override fun onCreate() {
        super.onCreate()
        val p = ExoPlayer.Builder(this).build()
        player = p
        session = MediaSession.Builder(this, p).build()
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo) = session
    override fun onDestroy() {
        session?.release()
        session = null
        player?.release()
        player = null
        super.onDestroy()
    }
}