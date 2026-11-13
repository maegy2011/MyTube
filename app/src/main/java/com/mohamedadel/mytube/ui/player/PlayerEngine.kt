package com.mohamedadel.mytube.ui.player

import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.data.local.PinFiles
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.util.QUALITY_AUDIO_ONLY
import com.mohamedadel.mytube.util.favKey
import com.mohamedadel.mytube.util.isLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 2.3 slice 27b (2026-11-13): playVideo engine extracted from the
 * God-Activity. Playback states already live in PlayerViewModel (slice 27a);
 * the Activity keeps ExoPlayer, prefs, toasts and the prefetch map behind
 * PlayEngineHost. Branch semantics identical to the original. Fns <= 30 lines.
 */
data class PlayPrefs(
    val offline: Boolean,
    val ageMode: String,
    val miniEnabled: Boolean,
    val miniAutoshow: Boolean,
    val preloadNext: Boolean,
    val preloadWifiOnly: Boolean,
    val videoMode: Boolean
)

interface PlayEngineHost : PlayerHost {
    fun playPrefs(): PlayPrefs
    fun exo(): androidx.media3.exoplayer.ExoPlayer?
    fun playerWasPlaying(): Boolean
    fun pauseNow()
    fun recordHistory(v: VideoItem)
    fun takePrefetched(key: String): StreamData?
    fun putPrefetched(key: String, s: StreamData)
    fun prefetchCount(): Int
    fun clearPrefetch()
    fun isUnmetered(): Boolean
    fun pickAudio(s: StreamData): Int
    fun pickSubs(s: StreamData): Set<Int>
    fun startQuality(s: StreamData, kbps: Int): String
    fun qualityEntries(s: StreamData, kbps: Int): List<String>
    fun videoModeNow(): Boolean
    fun setExpanded(b: Boolean)
    fun offlinePinFail()
    fun extractFail(detail: String?)
    fun pinFail()
    fun ageBlocked()
    fun pickSub(s: StreamData): Int
}

fun PlayerViewModel.playVideo(
    v: VideoItem,
    inQueue: Boolean = false,
    expand: Boolean = false,
    ageOk: Boolean = false,
    pre: StreamData? = null,
    forcePlay: Boolean = false,
    host: PlayEngineHost
) {
    val prefs = host.playPrefs()
    val hadPlaying = playingNow() != null
    setPlaying(v)
    host.recordHistory(v)
    if (expand) host.setExpanded(true)
    else if (!hadPlaying && (!prefs.miniEnabled || !prefs.miniAutoshow)) host.setExpanded(true)
    if (!inQueue) {
        replaceQueue(listOf(v), 0)
        setAutoAdvance(false)
        setRadioOn(false)
        setVideoModeQueue(false)
    } else {
        setAutoAdvance(true)
    }
    setQueueIndex(indexOfKey(v.favKey()))
    setPlayerLoading(true)
    setPlayerError(null)
    setPlayerPositionMs(0L); setPlayerDurationMs(0L); setPlayerBufferedPct(0)
    val wasPlayingQueue = inQueue && host.playerWasPlaying()
    if (!inQueue) try { host.clearPrefetch() } catch (_: Exception) { }
    try { host.pauseNow() } catch (_: Exception) { }
    viewModelScope.launch {
        resolveAndStart(v, inQueue, ageOk, pre, wasPlayingQueue, forcePlay, prefs, host)
    }
}

private suspend fun PlayerViewModel.resolveAndStart(
    v: VideoItem,
    inQueue: Boolean,
    ageOk: Boolean,
    pre: StreamData?,
    wasPlayingQueue: Boolean,
    forcePlay: Boolean,
    prefs: PlayPrefs,
    host: PlayEngineHost
) {
    val key = v.favKey()
    val pinned = try { withContext(Dispatchers.IO) { PinFiles.isPinned(host.appCtx(), key) } } catch (_: Exception) { false }
    if (prefs.offline && !pinned) {
        setPlayerLoading(false)
        host.offlinePinFail()
        return
    }
    setPlayingPinned(false)
    if (v.isLocal()) { startLocal(v, wasPlayingQueue, forcePlay, host); return }
    setPlayingPinned(pinned)
    if (pinned) { startPinned(v, key, inQueue, ageOk, wasPlayingQueue, forcePlay, prefs, host); return }
    var s = pre ?: try { host.takePrefetched(key) } catch (_: Exception) { null }
    if (s != null && s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) s = null
    if (s == null) {
        s = try { YouTubeRepo.streamRouted(v.url ?: return) } catch (_: Exception) { null }
        if (s == null) {
            setPlayerLoading(false)
            host.extractFail(null)
            return
        }
    }
    if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
        setPlayerLoading(false)
        host.extractFail(s.error)
        return
    }
    setAudioTrackIdx(host.pickAudio(s))
    setSubTrackIdx(host.pickSub(s))
    setSubTrackFor(null)
    if (ageGateOnline(v, inQueue, s, ageOk, prefs, host)) return
    if (s.isLive) {
        setQualityList(listOf("live"))
        setQuality("live")
        setStreamData(s)
        applyQuality("live", s, host.exo(), host, autoplay = wasPlayingQueue, forceStart = forcePlay, local = true)
        if (com.mohamedadel.mytube.playback.CastManager.connected.value) castCurrent(host, true)
        return
    }
    setStreamData(s)
    val kbps = host.kbps()
    setQualityList(host.qualityEntries(s, kbps))
    val startQ = if (s.videoUrlByQuality.isEmpty()) QUALITY_AUDIO_ONLY else host.startQuality(s, kbps)
    setQuality(if (prefs.videoMode && startQ == QUALITY_AUDIO_ONLY && s.videoUrlByQuality.isNotEmpty()) {
        qualityListNow().firstOrNull { it != QUALITY_AUDIO_ONLY } ?: startQ
    } else startQ)
    applyQuality(startQ, s, host.exo(), host, autoplay = wasPlayingQueue, forceStart = forcePlay)
    if (com.mohamedadel.mytube.playback.CastManager.connected.value) castCurrent(host, true)
    prefetchNext(prefs, host)
}

private suspend fun PlayerViewModel.startPinned(
    v: VideoItem, key: String, inQueue: Boolean, ageOk: Boolean,
    wasPlayingQueue: Boolean, forcePlay: Boolean, prefs: PlayPrefs, host: PlayEngineHost
) {
    val snap = withContext(Dispatchers.IO) {
        val app = host.appCtx()
        val d = PinFiles.existingDir(app, key)
        val vf = if (d != null) java.io.File(d, "video.mp4") else null
        val af = if (d != null) java.io.File(d, "audio.m4a") else null
        val sf = if (d != null) java.io.File(d, "sub.vtt") else null
        val m = try { if (d != null) org.json.JSONObject(java.io.File(d, "meta.json").readText()) else null } catch (_: Exception) { null }
        Triple(d, Triple(vf, af, sf), m)
    }
    try {
        val dir = snap.first ?: throw IllegalStateException("pin missing")
        val vFile = snap.second.first ?: throw IllegalStateException("pin missing")
        if (!vFile.exists()) throw IllegalStateException("pin missing")
        val s = buildPinStream(v, dir, vFile, snap.second.second, snap.second.third, snap.third)
        if (pinAgeBlocked(s, v, inQueue, ageOk, prefs, host)) return
        setStreamData(s)
        applyQuality(s.pinLabel, s.stream, host.exo(), host, autoplay = wasPlayingQueue, forceStart = forcePlay, local = true)
        setPlayingPinned(true)
    } catch (_: Exception) {
        setPlayerLoading(false)
        host.pinFail()
    } finally {
        setPlayerLoading(false)
    }
}

private fun PlayerViewModel.startLocal(v: VideoItem, wasPlayingQueue: Boolean, forcePlay: Boolean, host: PlayEngineHost) {
    try {
        setAudioTrackIdx(0)
        setSubTrackIdx(-1)
        val s = StreamData(audioLowestUrl = v.url, title = v.title, subtitles = emptyList())
        setStreamData(s)
        setQualityList(listOf(QUALITY_AUDIO_ONLY))
        setQuality(QUALITY_AUDIO_ONLY)
        applyQuality(QUALITY_AUDIO_ONLY, s, host.exo(), host, autoplay = wasPlayingQueue, forceStart = forcePlay, local = true)
        if (com.mohamedadel.mytube.playback.CastManager.connected.value) castCurrent(host, true)
    } catch (_: Exception) {
        setPlayerLoading(false)
        host.pinFail()
    }
}

private suspend fun PlayerViewModel.ageGateOnline(
    v: VideoItem, inQueue: Boolean, s: StreamData, ageOk: Boolean, prefs: PlayPrefs, host: PlayEngineHost
): Boolean {
    if (s.ageLimit < 18 || ageOk) return false
    if (prefs.ageMode == "block") {
        setPlayerLoading(false)
        host.ageBlocked()
        return true
    }
    setPlayerLoading(false)
    if (inQueue) {
        host.ageBlocked()
        val nxt = queueNow().getOrNull(queueIndexNow() + 1)
        if (nxt != null) host.play(nxt, true, host.expandedNow(), false)
        else { setRadioOn(false); setAutoAdvance(false) }
        return true
    }
    setAgePending(Triple(v, inQueue, s))
    return true
}

private suspend fun PlayerViewModel.prefetchNext(prefs: PlayPrefs, host: PlayEngineHost) {
    try {
        if (prefs.preloadNext && !prefs.offline && autoAdvanceNow() && host.prefetchCount() < 2 &&
            (!prefs.preloadWifiOnly || host.isUnmetered())) {
            val nxt = queueNow().getOrNull(queueIndexNow() + 1) ?: return
            val nk = nxt.favKey()
            if (PinFiles.isPinned(host.appCtx(), nk) || host.prefetchCount() >= 2) return
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val s2 = YouTubeRepo.streamRouted(nxt.url ?: return@launch) ?: return@launch
                    if (s2.audioLowestUrl != null || s2.videoUrlByQuality.isNotEmpty()) {
                        if (host.prefetchCount() < 2) host.putPrefetched(nk, s2)
                    }
                } catch (_: Exception) { }
            }
        }
    } catch (_: Exception) { }
}
