package com.mohamedadel.mytube.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.media3.common.PlaybackException
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem

// Pure playback/format/battery helpers extracted from MainActivity (split step 1).
// No Compose, no Activity state — safe to unit-test and reuse.

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
    if (heights.isEmpty()) return s.videoUrlByQuality.values.firstOrNull()
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

fun shortQuality(q: String): String {
    return when {
        q.startsWith("audio-only") -> "🔊 صوت"
        q.startsWith("auto") -> "⚡ تلقائي"
        else -> q.substringBefore(" • ").ifBlank { q }
    }
}

// Smart Arabic error messages: raw ExoPlayer/NewPipe errors are English codes —
// map them to something the user can act on (retry vs. open in YouTube).
fun smartError(error: PlaybackException?): String {
    if (error == null) return "حدث خطأ غير معروف — جرّب مجددًا أو شاهده في يوتيوب."
    return when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        PlaybackException.ERROR_CODE_TIMEOUT -> "انقطع الاتصال أثناء التحميل — تحقق من الإنترنت ثم أعد المحاولة."
        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "المصدر غير متاح (رُبما حُذف الفيديو أو قيّده الناشر) — جرّب مجددًا أو شاهده في يوتيوب."
        PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> "هذا بث مباشر انتهت نافذته — شاهده في يوتيوب."
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED -> "جهازك تعذّر عليه فك تشفير هذا الفيديو — جرّب جودة أقل أو شاهده في يوتيوب."
        PlaybackException.ERROR_CODE_DRM_SCHEME_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DRM_UNSPECIFIED -> "محتوى محمي لا يمكن تشغيله هنا — شاهده في يوتيوب."
        PlaybackException.ERROR_CODE_REMOTE_ERROR -> "خطأ من جهة الخادم — أعد المحاولة بعد قليل أو شاهده في يوتيوب."
        else -> "تعذّر التشغيل (خطأ ${error.errorCode}) — أعد المحاولة أو شاهده في يوتيوب."
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
    val m = sec / 60
    val s = sec % 60
    return if (m >= 60) {
        val h = m / 60
        String.format("%d:%02d:%02d", h, m % 60, s)
    } else {
        String.format("%d:%02d", m, s)
    }
}

fun formatViews(n: Long): String {
    return when {
        n >= 1_000_000 -> String.format("%.1f مليون مشاهدة", n / 1_000_000.0)
        n >= 1_000 -> String.format("%.1f ألف مشاهدة", n / 1_000.0)
        else -> "$n مشاهدة"
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

fun Context.openInYoutubeCtx(v: VideoItem) {
    val id = v.id ?: return
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id")).apply { `package` = "com.google.android.youtube" })
    } catch (_: Exception) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$id")))
    }
}
