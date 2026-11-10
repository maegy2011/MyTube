package com.mohamedadel.mytube.playback

import android.media.audiofx.Equalizer

// Simple equalizer bound to the player's audio session (framework effect, no libraries).
// Every call is best-effort: devices without the effect report supported=false
// and the settings section hides itself. Must be re-attached whenever the
// audio session changes (player recreation) — MainActivity owns that via
// Player.Listener.onAudioSessionIdChanged.
object EqManager {
    private var eq: Equalizer? = null
    var supported: Boolean = false
        private set

    // Preset index saved in prefs; CUSTOM = -1.
    const val CUSTOM = -1

    fun attach(sessionId: Int): Boolean {
        release()
        if (sessionId == 0) return false
        return try {
            eq = Equalizer(0, sessionId)
            supported = (eq?.numberOfBands ?: 0) > 0
            if (!supported) release()
            supported
        } catch (_: Exception) {
            release()
            false
        }
    }

    fun release() {
        try { eq?.release() } catch (_: Exception) { }
        eq = null
        supported = false
    }

    fun setEnabled(on: Boolean) {
        try { eq?.enabled = on } catch (_: Exception) { }
    }

    fun presets(): List<String> {
        return try {
            val e = eq ?: return emptyList()
            val n = e.numberOfPresets.toInt()
            (0 until n).mapNotNull { try { e.getPresetName(it.toShort()) } catch (_: Exception) { null } }
        } catch (_: Exception) { emptyList() }
    }

    fun bandCount(): Int = try { eq?.numberOfBands?.toInt() ?: 0 } catch (_: Exception) { 0 }

    // Level range in millibels, e.g. -1500..1500.
    fun levelRange(): IntRange {
        return try {
            val r = eq?.bandLevelRange ?: return -1500..1500
            r[0].toInt()..r[1].toInt()
        } catch (_: Exception) { -1500..1500 }
    }

    fun centerFreqHz(band: Int): Int {
        return try { (eq?.getCenterFreq(band.toShort()) ?: 0) / 1000 } catch (_: Exception) { 0 }
    }

    fun usePreset(idx: Int) {
        try { eq?.usePreset(idx.toShort()) } catch (_: Exception) { }
    }

    fun setBand(band: Int, millibels: Int) {
        try { eq?.setBandLevel(band.toShort(), millibels.toShort()) } catch (_: Exception) { }
    }

    fun bandLevel(band: Int): Int {
        return try { eq?.getBandLevel(band.toShort())?.toInt() ?: 0 } catch (_: Exception) { 0 }
    }

    // 0..1 slider <-> millibels helpers (shared with Settings UI).
    fun toSlider(mb: Int): Float {
        val r = levelRange()
        return ((mb - r.first).toFloat() / (r.last - r.first).toFloat()).coerceIn(0f, 1f)
    }

    fun toMillibels(v: Float): Int {
        val r = levelRange()
        return (r.first + v * (r.last - r.first)).toInt()
    }
}
