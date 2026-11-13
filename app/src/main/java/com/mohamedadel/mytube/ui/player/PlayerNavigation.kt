package com.mohamedadel.mytube.ui.player

import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.util.isLocal
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/**
 * Phase 2.3 slice 5 (2026-11-13): the Activity implements this seam.
 * The ViewModel owns navigation LOGIC; the Activity owns the ExoPlayer,
 * network toasts formatting and stream state. No player instance crosses.
 */
interface PlayerHost {
    fun play(v: VideoItem, inQueue: Boolean, expand: Boolean, forcePlay: Boolean)
    fun toast(resId: Int, arg: String? = null)
    fun expandedNow(): Boolean
    fun relatedNow(): List<VideoItem>
    fun enableAutoplayPrefs()
    fun playerVolume(): Float
    fun setPlayerVolume(f: Float)
    fun pausePlayer()
    fun castSnapshot(): CastSnapshot?
    fun onCastStarted()
}

data class CastSnapshot(
    val url: String,
    val mime: String,
    val title: String?,
    val thumb: String?,
    val positionMs: Long,
    val autoplay: Boolean
)

/** Queue navigation + radio, moved from the God-Activity (slice 5). */
fun PlayerViewModel.isQueued(v: VideoItem): Boolean =
    queueNow().any { it.favKey() == v.favKey() }

fun PlayerViewModel.playPrevInQueue(host: PlayerHost) {
    val prev = prevSequential()
    if (prev != null) host.play(prev, true, host.expandedNow(), false)
}

fun PlayerViewModel.playFirstInQueue(forcePlay: Boolean, host: PlayerHost) {
    val q = queueNow()
    if (q.isNotEmpty()) host.play(q[0], true, host.expandedNow(), forcePlay)
    else setAutoAdvance(false)
}

fun PlayerViewModel.playNowAlone(v: VideoItem, host: PlayerHost) {
    host.enableAutoplayPrefs()
    host.play(v, false, true, true)
    setAutoAdvance(true)
    setRadioOn(true)
}

fun PlayerViewModel.enqueueNext(v: VideoItem, host: PlayerHost) {
    if (isQueued(v)) { host.toast(R.string.toast_already_queued); return }
    val q = queueNow().toMutableList()
    val at = if (queueIndexNow() >= 0) queueIndexNow() + 1 else q.size
    q.add(at.coerceIn(0, q.size), v)
    replaceQueue(q, queueIndexNow())
    host.toast(R.string.toast_added_queue)
    if (playingNow() == null) {
        setQueueIndex(0)
        host.play(v, true, true, true)
    }
}

fun PlayerViewModel.enqueueLast(v: VideoItem, host: PlayerHost) {
    if (isQueued(v)) { host.toast(R.string.toast_already_queued); return }
    appendItems(listOf(v))
    host.toast(R.string.toast_added_queue)
    if (playingNow() == null) host.play(v, true, true, true)
}

fun PlayerViewModel.playRandomInQueue(forcePlay: Boolean, host: PlayerHost) {
    val q = queueNow()
    if (q.size < 2) {
        if (q.isNotEmpty()) host.play(q[0], true, host.expandedNow(), forcePlay)
        return
    }
    val nxt = drawShuffled()
    if (nxt == null) {
        var n = queueIndexNow()
        var guard = 0
        while (n == queueIndexNow() && guard++ < 8) n = (0 until q.size).random()
        host.play(q[n], true, host.expandedNow(), forcePlay)
        return
    }
    host.play(nxt, true, host.expandedNow(), forcePlay)
}

fun PlayerViewModel.playNextInQueue(forcePlay: Boolean, host: PlayerHost) {
    if (shuffleOnNow() && queueNow().size > 1) { playRandomInQueue(forcePlay, host); return }
    val next = nextSequential()
    if (next != null) { host.play(next, true, host.expandedNow(), forcePlay); return }
    if (!radioOnNow()) { setAutoAdvance(false); return }
    if (playingNow()?.isLocal() == true) { setRadioOn(false); setAutoAdvance(false); return }
    val cached = host.relatedNow()
        .filter { r -> (r.url?.isNotBlank() == true) && queueNow().none { it.favKey() == r.favKey() } }
        .distinctBy { it.favKey() }
        .take(10)
    if (cached.isNotEmpty()) {
        appendItems(cached)
        trimHead()
        host.play(cached[0], true, host.expandedNow(), true)
        return
    }
    val cur = playingNow()
    val curUrl = cur?.url
    if (curUrl.isNullOrBlank() && cur?.title.isNullOrBlank()) {
        if (playModeNow() == "repeatAll" && queueNow().isNotEmpty()) {
            host.play(queueNow()[0], true, host.expandedNow(), true)
            return
        }
        setRadioOn(false)
        setAutoAdvance(false)
        return
    }
    fetchRefill(host)
}

private fun PlayerViewModel.fetchRefill(host: PlayerHost) {
    val cur = playingNow()
    val curUrl = cur?.url
    val curTitle = cur?.title
    viewModelScope.launch {
        try {
            var seed: List<VideoItem> = emptyList()
            var err: String? = null
            if (!curUrl.isNullOrBlank() && !curUrl.contains("soundcloud.com")) {
                try {
                    val s = YouTubeRepo.streamRouted(curUrl)
                    seed = s?.related ?: emptyList()
                    if (seed.isEmpty()) err = s?.error
                } catch (e: Exception) { err = e.message?.take(80) }
            }
            if (seed.isEmpty() && !curTitle.isNullOrBlank()) {
                try { seed = YouTubeRepo.search(curTitle, null, null).items }
                catch (e: Exception) { if (err == null) err = e.message?.take(80) }
            }
            val fresh = seed
                .filter { r -> (r.url?.isNotBlank() == true) && queueNow().none { it.favKey() == r.favKey() } }
                .distinctBy { it.favKey() }
                .take(10)
            if (fresh.isNotEmpty()) {
                appendItems(fresh)
                trimHead()
                host.play(fresh[0], true, host.expandedNow(), true)
                return@launch
            }
            if (playModeNow() == "repeatAll" && queueNow().isNotEmpty()) {
                host.play(queueNow()[0], true, host.expandedNow(), true)
                return@launch
            }
            setRadioOn(false)
            setAutoAdvance(false)
            if (!err.isNullOrBlank()) host.toast(R.string.err_extract_detail, err)
        } catch (_: Exception) {
            setRadioOn(false)
            setAutoAdvance(false)
            host.toast(R.string.toast_radio_fail)
        }
    }
}

private suspend fun radioSeed(
    v: VideoItem,
    playingKey: String?,
    relatedNow: List<VideoItem>,
    onToast: (Int, String?) -> Unit
): Pair<List<VideoItem>, String?> {
    var seedErr: String? = null
    if (playingKey == v.favKey() && relatedNow.isNotEmpty()) return relatedNow to null
    val url = v.url
    if (url.isNullOrBlank()) return emptyList<VideoItem>() to null
    var list: List<VideoItem> = emptyList()
    if (!url.contains("soundcloud.com")) {
        try {
            val s = YouTubeRepo.streamRouted(url)
            list = s?.related ?: emptyList()
            if (list.isEmpty()) seedErr = s?.error
        } catch (e: Exception) { seedErr = e.message?.take(80) }
    }
    if (list.isEmpty()) {
        val q = v.title?.takeIf { it.isNotBlank() } ?: return emptyList<VideoItem>() to seedErr
        try { list = YouTubeRepo.search(q, null, null).items }
        catch (e: Exception) { if (seedErr == null) seedErr = e.message?.take(80) }
    }
    return list to seedErr
}

fun PlayerViewModel.startRadio(v: VideoItem, host: PlayerHost) {
    if (v.isLocal()) { host.toast(R.string.toast_radio_fail); return }
    viewModelScope.launch {
        try {
            val (seed, seedErr) = radioSeed(v, playingNow()?.favKey(), host.relatedNow()) { id, a -> host.toast(id, a) }
            val fresh = seed.filter { it.url?.isNotBlank() == true }.distinctBy { it.favKey() }.take(15)
            if (fresh.isEmpty()) {
                if (!seedErr.isNullOrBlank()) host.toast(R.string.err_extract_detail, seedErr)
                else host.toast(R.string.toast_radio_fail)
                return@launch
            }
            replaceQueue(listOf(v) + fresh.filter { it.favKey() != v.favKey() }, 0)
            setRadioOn(true)
            setVideoModeQueue(false)
            host.toast(R.string.toast_radio_started)
            host.play(v, true, host.expandedNow(), false)
        } catch (_: Exception) { host.toast(R.string.toast_radio_fail) }
    }
}

private var fadeJob: kotlinx.coroutines.Job? = null

fun PlayerViewModel.fadeOutAndPause(host: PlayerHost) {
    try { fadeJob?.cancel() } catch (_: Exception) { }
    fadeJob = viewModelScope.launch {
        try {
            val base = try { host.playerVolume().coerceIn(0f, 1f) } catch (_: Exception) { 1f }
            if (base <= 0f) { host.pausePlayer(); return@launch }
            repeat(10) { i ->
                try { host.setPlayerVolume(base * (1f - (i + 1) / 10f)) } catch (_: Exception) { return@repeat }
                try { kotlinx.coroutines.delay(500) } catch (_: Exception) { return@repeat }
            }
            host.pausePlayer()
            try { host.setPlayerVolume(base) } catch (_: Exception) { }
        } catch (_: Exception) { try { host.pausePlayer() } catch (_: Exception) { } }
    }
}

fun PlayerViewModel.castCurrent(host: PlayerHost, autoplay: Boolean = true) {
    try {
        val snap = host.castSnapshot() ?: return
        val ok = com.mohamedadel.mytube.playback.CastManager.load(
            snap.url, snap.title, snap.thumb, snap.mime, snap.positionMs, autoplay
        )
        if (ok) host.onCastStarted()
    } catch (_: Exception) { }
}
