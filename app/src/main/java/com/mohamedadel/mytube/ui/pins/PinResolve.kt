package com.mohamedadel.mytube.ui.pins

import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.PinFiles
import com.mohamedadel.mytube.data.local.PinManager
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.util.QUALITY_AUDIO_ONLY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.mohamedadel.mytube.data.local.favKey

/**
 * Phase 2.3 slice 16 (2026-11-13): pin resolve-and-plan extracted from the
 * God-Activity as PinViewModel extensions. Player/picker reads arrive via
 * PinHost; resolve + plan + dialog state live here. Every fn <= 30 lines.
 */
interface PinResolveHost {
    fun currentStreamData(): StreamData?
    fun playingFavKey(): String?
    fun pinKeysContains(key: String): Boolean
    fun ensurePinNotifPerm()
    fun playPinned(v: VideoItem)
    fun pickStartQuality(s: StreamData, kbps: Int): String
    fun pickAudioIdxFor(s: StreamData): Int
    fun matchSubIdxsFor(s: StreamData): Set<Int>
}

private suspend fun PinViewModel.fail(host: PinHost, resId: Int) {
    withContext(Dispatchers.Main) { host.uiToast(appStr(resId)) }
}

private fun PinViewModel.appStr(resId: Int, vararg args: Any): String =
    try { getApp().getString(resId, *args) } catch (_: Exception) { "" }

fun PinViewModel.requestPin(
    v: VideoItem,
    sd: StreamData?,
    q: String?,
    aIdx: Int?,
    sIdxs: Set<Int>?,
    host: PinHost,
    rh: PinResolveHost,
    pinKeys: Set<String>
) {
    if ((sd?.isLive == true) || (rh.currentStreamData()?.isLive == true && rh.playingFavKey() == v.favKey())) {
        host.uiToast(appStr(R.string.err_pin_live)); return
    }
    val key = v.favKey()
    if (pinKeys.contains(key) || pinningKeysNow().contains(key)) return
    setPinning(key, true)
    ioScope().launch(Dispatchers.IO) {
        try {
            val u = v.url
            if (sd == null && u.isNullOrBlank()) { setPinning(key, false); fail(host, R.string.toast_pin_fail); return@launch }
            val s = sd ?: YouTubeRepo.streamRouted(u ?: return@launch)
                ?: run { fail(host, R.string.toast_pin_fail); return@launch }
            if (s.audioHlsOnly && !(u?.contains("soundcloud.com") == true)) { fail(host, R.string.pin_err_stream); return@launch }
            if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
                fail(host, R.string.toast_pin_fail); return@launch
            }
            val kbps = host.kbps()
            val qq = q ?: if (s.videoUrlByQuality.isEmpty()) QUALITY_AUDIO_ONLY else rh.pickStartQuality(s, kbps)
            val ai = aIdx ?: rh.pickAudioIdxFor(s)
            val si = sIdxs ?: rh.matchSubIdxsFor(s)
            val plan = PinManager.buildPlan(s, qq, ai, si, kbps, maxH = if (host.allow4k()) Int.MAX_VALUE else 1080)
            if (plan == null) { fail(host, R.string.toast_pin_fail); return@launch }
            val est = PinManager.estimateBytes(listOf(plan.videoUrl, plan.audioUrl, plan.subUrl))
            withContext(Dispatchers.Main) {
                val loc = try { PinFiles.loc(getApp()) } catch (_: Exception) { "internal" }
                setConfirm(PinManager.PinRequest(v, plan, est), s, qq, ai, si, loc, est)
            }
        } catch (_: Exception) { fail(host, R.string.toast_pin_fail) }
        finally { setPinning(key, false) }
    }
}

fun PinViewModel.quickPin(v: VideoItem, sd: StreamData?, host: PinHost, rh: PinResolveHost, pinKeys: Set<String>) {
    rh.ensurePinNotifPerm()
    if ((sd?.isLive == true) || (rh.currentStreamData()?.isLive == true && rh.playingFavKey() == v.favKey())) {
        host.uiToast(appStr(R.string.err_pin_live)); return
    }
    val key = v.favKey()
    if (pinKeys.contains(key) || pinningKeysNow().contains(key)) return
    setPinning(key, true)
    ioScope().launch(Dispatchers.IO) {
        try {
            val s = resolveForQuick(v, sd, key, host, rh) ?: return@launch
            val kbps = host.kbps()
            val qq = quickQuality(s, host, rh, kbps)
            val ai = rh.pickAudioIdxFor(s)
            val si = quickSubs(host, rh, s)
            val plan = PinManager.buildPlan(s, qq, ai, si, kbps, maxH = if (host.allow4k()) Int.MAX_VALUE else 1080)
            if (plan == null) { setPinning(key, false); fail(host, R.string.toast_pin_fail); return@launch }
            runPinDownload(v, plan, key, host, alreadyAdded = true, retry = com.mohamedadel.mytube.PinRetry(qq, ai, si))
        } catch (_: Exception) { setPinning(key, false); fail(host, R.string.toast_pin_fail) }
    }
}

private suspend fun PinViewModel.resolveForQuick(
    v: VideoItem, sd: StreamData?, key: String, host: PinHost, rh: PinResolveHost
): StreamData? {
    val u = v.url
    if (sd == null && u.isNullOrBlank()) { setPinning(key, false); fail(host, R.string.toast_pin_fail); return null }
    val s = sd ?: YouTubeRepo.streamRouted(u ?: run { setPinning(key, false); return null })
        ?: run { setPinning(key, false); fail(host, R.string.toast_pin_fail); return null }
    if (s.audioHlsOnly && !(u?.contains("soundcloud.com") == true)) {
        setPinning(key, false); fail(host, R.string.pin_err_stream); return null
    }
    if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
        setPinning(key, false); fail(host, R.string.toast_pin_fail); return null
    }
    return s
}

private fun PinViewModel.quickQuality(s: StreamData, host: PinHost, rh: PinResolveHost, kbps: Int): String {
    val lastQ = host.lastPinQuality()
    return if (s.videoUrlByQuality.isEmpty()) QUALITY_AUDIO_ONLY
    else if (!lastQ.isNullOrBlank() && (lastQ == QUALITY_AUDIO_ONLY || s.videoUrlByQuality.containsKey(lastQ))) lastQ
    else rh.pickStartQuality(s, kbps)
}

private fun quickSubs(host: PinHost, rh: PinResolveHost, s: StreamData): Set<Int> {
    val lastSubs = host.lastPinSubs()
    return if (lastSubs.isNotEmpty()) lastSubs else rh.matchSubIdxsFor(s)
}

fun PinViewModel.confirmPin(host: PinHost, rh: PinResolveHost) {
    rh.ensurePinNotifPerm()
    val pc = pinConfirmNow() ?: return
    val sd = pinSdNow()
    val key = pc.v.favKey()
    clearConfirm()
    host.rememberPinChoices(pinSelQualityNow(), pinSelSubsNow())
    val plan = if (sd != null) {
        PinManager.buildPlan(sd, pinSelQualityNow(), pinSelAudioNow(), pinSelSubsNow(), host.kbps(), maxH = if (host.allow4k()) Int.MAX_VALUE else 1080)
    } else pc.plan
    if (plan == null) { host.uiToast(appStr(R.string.toast_pin_fail)); return }
    runPinDownload(pc.v, plan, key, host, retry = com.mohamedadel.mytube.PinRetry(pinSelQualityNow(), pinSelAudioNow(), pinSelSubsNow()))
}

fun PinViewModel.openPinned(key: String, host: PinHost, rh: PinResolveHost) {
    ioScope().launch(Dispatchers.IO) {
        try {
            val d = daoNow() ?: return@launch
            val row = try { d.observePins().first().firstOrNull { it.videoKey == key } } catch (_: Exception) { null }
            if (row == null) { fail(host, R.string.toast_pin_fail); return@launch }
            val v = VideoItem(id = row.videoId, title = row.title, channel = row.channel, url = row.url)
            withContext(Dispatchers.Main) { rh.playPinned(v) }
        } catch (_: Exception) { fail(host, R.string.toast_pin_fail) }
    }
}
