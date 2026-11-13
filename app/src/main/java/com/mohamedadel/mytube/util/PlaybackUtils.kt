package com.mohamedadel.mytube.util

import com.mohamedadel.mytube.R
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.media3.common.PlaybackException
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem

// Pure playback/format/battery helpers extracted from MainActivity (split step 1).
// No Compose, no Activity state — safe to unit-test and reuse.

// Runtime-layer shared UA (2026-11-12 fix): googlevideo/cf-media now 403s
// non-browser agents on the player stack too — same class as the pin-side
// 403 fixed with PIN_UA in PinManager. One constant for both stacks.
const val BROWSER_UA = "Mozilla/5.0"
const val QUALITY_AUDIO_ONLY = "audio-only (default)"

fun Context.estimatedDownstreamKbps(): Int {
    return try {
        val cm = getSystemService(android.net.ConnectivityManager::class.java) ?: return 0
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return 0
        caps.linkDownstreamBandwidthKbps
    } catch (_: Exception) { 0 }
}

// Auto quality mapped to the user's actual connection speed:
// <1Mbps→144p, <2.5Mbps→360p, <5Mbps→480p, <10Mbps→720p, <20Mbps→1080p, else highest. Unknown speed → old mid default (720/480).
fun autoUrl(s: StreamData, kbps: Int = 0): String? {
    val heights = s.videoUrlByQuality.keys.mapNotNull { k ->
        qualityHeight(k)?.let { h -> h to k }
    }
    // 2026-11-12: audio-only sources (e.g. SC HLS) carry no video map — fall back
    // to the resolved audio URL instead of returning null (null = err_quality_unavailable).
    if (heights.isEmpty()) return s.videoUrlByQuality.values.firstOrNull() ?: s.audioLowestUrl
    if (kbps <= 0) {
        val target = heights.filter { it.first == 720 }.firstOrNull()
            ?: heights.filter { it.first == 480 }.firstOrNull()
            ?: heights.maxByOrNull { it.first }
        return target?.let { s.videoUrlByQuality[it.second] }
    }
    val targetH = when {
        kbps < 1000 -> 144
        kbps < 2500 -> 360
        kbps < 5000 -> 480
        kbps < 10000 -> 720
        kbps < 20000 -> 1080
        else -> Int.MAX_VALUE
    }
    val atOrBelow = heights.filter { it.first <= targetH }.maxByOrNull { it.first }
    val best = atOrBelow ?: heights.minByOrNull { it.first }
    return best?.let { s.videoUrlByQuality[it.second] }
}

fun qualityHeight(label: String): Int? {
    val l = label.lowercase()
    if (l.contains("2160") || l.contains("4k")) return 2160
    if (l.contains("1440")) return 1440
    if (l.contains("1080")) return 1080
    if (l.contains("720")) return 720
    if (l.contains("480")) return 480
    if (l.contains("360")) return 360
    if (l.contains("240")) return 240
    if (l.contains("144")) return 144
    return l.filter { it.isDigit() }.toIntOrNull()
}

fun Context.shortQuality(q: String): String {
    return when {
        q.startsWith("audio-only") -> getString(R.string.sq_audio)
        q.startsWith("auto") -> getString(R.string.sq_auto)
        else -> q.substringBefore(" • ").ifBlank { q }
    }
}

// Smart Arabic error messages: raw ExoPlayer/NewPipe errors are English codes —
// map them to something the user can act on (retry vs. open in YouTube).
fun Context.smartError(error: PlaybackException?): String {
    if (error == null) return getString(R.string.err_unknown)
    return when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        PlaybackException.ERROR_CODE_TIMEOUT -> getString(R.string.err_io)
        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> getString(R.string.err_source_gone)
        PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> getString(R.string.err_live_ended)
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED -> getString(R.string.err_decoder)
        PlaybackException.ERROR_CODE_DRM_SCHEME_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DRM_UNSPECIFIED -> getString(R.string.err_drm)
        PlaybackException.ERROR_CODE_REMOTE_ERROR -> getString(R.string.err_remote)
        else -> getString(R.string.err_playback_code, error.errorCode)
    }
}

fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%d:%02d", m, s)
}

fun formatDuration(sec: Long): String {
    val safe = sec.coerceAtLeast(0)
    val m = safe / 60
    val s = safe % 60
    return if (m >= 60) {
        val h = m / 60
        String.format("%d:%02d:%02d", h, m % 60, s)
    } else {
        String.format("%d:%02d", m, s)
    }
}

fun Context.formatViews(n: Long): String {
    return when {
        n >= 1_000_000 -> getString(R.string.views_m, n / 1_000_000.0)
        n >= 1_000 -> getString(R.string.views_k, n / 1_000.0)
        else -> getString(R.string.views_count, n)
    }
}

// Battery-optimization exemption helpers (checked on every app start/resume).
fun Context.isBatteryExempt(): Boolean {
    return try {
        val pm = getSystemService(android.os.PowerManager::class.java) ?: return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) pm.isIgnoringBatteryOptimizations(packageName) else true
    } catch (_: Exception) { true }
}

fun Context.batteryExemptionIntent(): Intent {
    return try {
        Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
    } catch (_: Exception) {
        Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    }
}

// Fix: system share sheet for a video (title + link, works for any app).
fun Context.shareVideoCtx(v: VideoItem) {
    try {
        val url = v.url ?: v.id?.let { "https://www.youtube.com/watch?v=$it" } ?: return
        val text = ((v.title?.takeIf { it.isNotBlank() } ?: "MyTube") + "\n" + url)
        startActivity(android.content.Intent.createChooser(
            android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(android.content.Intent.EXTRA_TEXT, text)
            }, getString(com.mohamedadel.mytube.R.string.share)))
    } catch (_: Exception) { }
}

// Universal YouTube ID extractor: watch?v=, /shorts/, /live/, /embed/, youtu.be/,
// music.youtube.com, /v/ — returns null only when no 11-char ID is present.
fun youtubeIdFrom(link: String?): String? {
    if (link.isNullOrBlank()) return null
    val l = link.trim()
    // triple-quoted: regex backslashes need no Kotlin escaping
    val pats = listOf(
        Regex("""[?&]v=([A-Za-z0-9_-]{11})"""),
        Regex("""youtu\.be/([A-Za-z0-9_-]{11})"""),
        Regex("""/shorts/([A-Za-z0-9_-]{11})"""),
        Regex("""/live/([A-Za-z0-9_-]{11})"""),
        Regex("""/embed/([A-Za-z0-9_-]{11})"""),
        Regex("""/v/([A-Za-z0-9_-]{11})""")
    )
    for (p in pats) {
        try { p.find(l)?.groupValues?.getOrNull(1)?.let { return it } } catch (_: Exception) { }
    }
    return null
}

fun Context.openInYoutubeCtx(v: VideoItem) {
    val id = v.id ?: youtubeIdFrom(v.url)
    if (id == null) {
        // no parseable ID: open the raw URL (or toast when there is none)
        val raw = v.url
        if (raw.isNullOrBlank()) {
            try { android.widget.Toast.makeText(this, getString(com.mohamedadel.mytube.R.string.err_no_sources), android.widget.Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
            return
        }
        // S4 (audit P1 2026-11-11): feed data must never reach intent resolution
        // with a non-web scheme (intent://, file://, custom) — http(s) only.
        val scheme = try { Uri.parse(raw).scheme?.lowercase() } catch (_: Exception) { null }
        if (scheme != "http" && scheme != "https") {
            try { android.widget.Toast.makeText(this, getString(com.mohamedadel.mytube.R.string.err_no_sources), android.widget.Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
            return
        }
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(raw))) } catch (_: Exception) { }
        return
    }
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id")).apply { `package` = "com.google.android.youtube" })
    } catch (_: Exception) {
        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$id"))) } catch (_: Exception) { }
    }
}

// Phase 2.3 slice 4 (2026-11-13): pure track/quality selectors moved out of the
// God-Activity. All inputs explicit (no prefs reads) — same logic, testable.

fun resolveLang(pref: String): String? = when (pref) {
    "off" -> null
    "original" -> null
    "auto" -> try { java.util.Locale.getDefault().language } catch (_: Exception) { "ar" }
    else -> pref
}

fun cap4k(sd: StreamData, allow4k: Boolean): StreamData =
    if (allow4k) sd else try { sd.copy(videoUrlByQuality = sd.videoUrlByQuality.filterKeys { (qualityHeight(it) ?: 0) <= 1080 }) } catch (_: Exception) { sd }

fun autoKeyFor(sd0: StreamData, kbps: Int, allow4k: Boolean): String {
    val sd = cap4k(sd0, allow4k)
    return autoUrl(sd, kbps)?.let { u ->
        val h = sd.videoUrlByQuality.entries.firstOrNull { it.value == u }?.key
            ?.let { qualityHeight(it) }
        if (h != null && h != Int.MAX_VALUE) "auto \u2022 ${h}p" else "auto"
    } ?: "auto"
}

fun uriEncode(x: String?): String = try { android.net.Uri.encode(x ?: "") } catch (_: Exception) { "" }

fun castMimeFor(url: String, audioOnly: Boolean): String {
    return try {
        val q = android.net.Uri.parse(url).getQueryParameter("mime")?.let {
            java.net.URLDecoder.decode(it, "UTF-8")
        }
        if (!q.isNullOrBlank() && q.contains("/")) q else if (audioOnly) "audio/mp4" else "video/mp4"
    } catch (_: Exception) { if (audioOnly) "audio/mp4" else "video/mp4" }
}

fun resolvePlaybackUrl(q: String, s: StreamData, audioTrackIdx: Int, allow4k: Boolean, kbps: Int): Triple<String, Boolean, String?>? {
    val selAudio = s.audioTracks.getOrNull(audioTrackIdx)?.url ?: s.audioLowestUrl
    val capped = cap4k(s, allow4k)
    val url = when {
        q == QUALITY_AUDIO_ONLY -> selAudio
        q.startsWith("auto") -> autoUrl(capped, kbps)
        else -> capped.videoUrlByQuality[q]
    } ?: return null
    val muxedUrls = s.videoUrlByQuality.filter { s.muxedQualities.contains(it.key) }.values.toSet()
    return Triple(url, url in muxedUrls, selAudio)
}

fun pickStartQuality(sd0: StreamData, kbps: Int, qualityPref: String, allow4k: Boolean): String {
    val sd = cap4k(sd0, allow4k)
    val autoKey = autoKeyFor(sd, kbps, allow4k)
    val sorted = sd.videoUrlByQuality.keys.sortedByDescending { qualityHeight(it) ?: 0 }
    return when (qualityPref) {
        "auto" -> autoKey
        "saver" -> sorted.lastOrNull() ?: QUALITY_AUDIO_ONLY
        "high" -> sorted.firstOrNull() ?: autoKey
        else -> QUALITY_AUDIO_ONLY
    }
}

fun pickAudioIdxFor(sd: StreamData?, preferOriginal: Boolean, lang1: String, lang2: String): Int {
    val tracks = sd?.audioTracks ?: return 0
    if (tracks.isEmpty()) return 0
    if (preferOriginal) {
        val i = tracks.indexOfFirst { (it.type ?: "").equals("ORIGINAL", ignoreCase = true) }
        if (i >= 0) return i
    }
    val ordered = listOf(resolveLang(lang1), resolveLang(lang2)) +
        listOf(try { java.util.Locale.getDefault().language } catch (_: Exception) { null }, "en")
    for (want in ordered) {
        if (want == null) return 0
        val i = tracks.indexOfFirst { t ->
            val tag = t.localeTag ?: t.name ?: ""
            tag.startsWith(want, ignoreCase = true)
        }
        if (i >= 0) return i
    }
    return 0
}

fun matchSubIdxFor(sd: StreamData?, subLang: String, subLang2: String, subHideAuto: Boolean): Int {
    if (subLang == "off") return -1
    val subs = sd?.subtitles ?: return -1
    if (subs.isEmpty()) return -1
    val device = try { java.util.Locale.getDefault().language } catch (_: Exception) { null }
    val ordered = listOf(resolveLang(subLang), resolveLang(subLang2)) + listOf(device, "en")
    val pool = if (subHideAuto) subs.filter { !it.auto } else subs
    for (want in ordered) {
        if (want == null) break
        var i = pool.indexOfFirst { !it.auto && it.langTag.startsWith(want, ignoreCase = true) }
        if (i >= 0) return subs.indexOf(pool[i])
        i = pool.indexOfFirst { it.langTag.startsWith(want, ignoreCase = true) }
        if (i >= 0) return subs.indexOf(pool[i])
    }
    return -1
}

fun matchSubIdxsFor(sd: StreamData?, subLang: String, subLang2: String, subHideAuto: Boolean): Set<Int> {
    if (subLang == "off") return emptySet()
    val subs = sd?.subtitles ?: return emptySet()
    if (subs.isEmpty()) return emptySet()
    val device = try { java.util.Locale.getDefault().language } catch (_: Exception) { null }
    val ordered = (listOf(resolveLang(subLang), resolveLang(subLang2)) + listOf(device, "en")).filterNotNull().distinct()
    val pool = if (subHideAuto) subs.filter { !it.auto } else subs
    val out = linkedSetOf<Int>()
    for (want in ordered) {
        if (want == "original") break
        var i = pool.indexOfFirst { !it.auto && it.langTag.startsWith(want, ignoreCase = true) }
        if (i < 0) i = pool.indexOfFirst { it.langTag.startsWith(want, ignoreCase = true) }
        if (i >= 0) out.add(subs.indexOf(pool[i]))
        if (out.size >= 2) break
    }
    return out
}
