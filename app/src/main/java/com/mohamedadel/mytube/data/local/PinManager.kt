package com.mohamedadel.mytube.data.local

import android.content.Context
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.util.autoUrl
import com.mohamedadel.mytube.util.qualityHeight
import java.io.File
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

// Phase 3 — faithful offline pins ("as configured": current quality + audio + subtitle).
// YouTube serves DASH (video-only + audio-only), so a full pin = video.mp4 [+ audio.m4a]
// + sub.vtt + meta.json under travel/<sha256>. Muxed qualities collapse to one file.
// Playback-from-pin is Phase 4; this file only stages bytes + index row, never touches the player.
object PinManager {

    data class PinRequest(val v: VideoItem, val plan: PinPlan, val est: Long)

    data class PinPlan(
        val videoUrl: String?,
        val audioUrl: String?,
        val subUrl: String?,
        val subMime: String?,
        val subLang: String?,
        val kind: String, // "audio" | "video"
        val quality: String,
        val audioTag: String?,
        val subTag: String?
    )

    // Same URL math as MainActivity.applyQuality (muxed vs merged, auto mapping).
    // kbps must be the live estimate (not 0): "auto" resolves differently per bandwidth,
    // and pinning must capture exactly what the player would play.
    fun buildPlan(sd: StreamData, quality: String, audioIdx: Int, subIdx: Int, kbps: Int = 0): PinPlan? {
        val selAudio = sd.audioTracks.getOrNull(audioIdx)?.url ?: sd.audioLowestUrl
        val audioTag = sd.audioTracks.getOrNull(audioIdx)?.let { it.localeTag ?: it.name }
        val url = when {
            quality == "audio-only (default)" -> selAudio
            quality.startsWith("auto") -> autoUrl(sd, kbps)
            else -> sd.videoUrlByQuality[quality]
        } ?: return null
        if (quality == "audio-only (default)") {
            val sub = sd.subtitles.getOrNull(subIdx)
            return PinPlan(null, selAudio, sub?.url, sub?.mime, sub?.langTag, "audio", quality, audioTag, sub?.label ?: sub?.langTag)
        }
        val muxedUrls = sd.videoUrlByQuality.filter { sd.muxedQualities.contains(it.key) }.values.toSet()
        val isMuxed = url in muxedUrls
        val sub = sd.subtitles.getOrNull(subIdx)
        return PinPlan(
            videoUrl = url,
            audioUrl = if (isMuxed) null else selAudio,
            subUrl = sub?.url, subMime = sub?.mime, subLang = sub?.langTag,
            kind = "video", quality = quality, audioTag = audioTag, subTag = sub?.label ?: sub?.langTag
        )
    }

    private val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(10, TimeUnit.MINUTES)
            .build()
    }

    // Approximate total via HEAD content-length, falling back to a 0-byte Range GET
    // (some hosts reject HEAD); -1 when a URL hides its size either way.
    fun estimateBytes(urls: List<String?>): Long {
        var total = 0L
        for (u in urls) {
            if (u.isNullOrBlank()) continue
            var len: Long? = null
            try {
                http.newCall(Request.Builder().url(u).head().build()).execute().use { resp ->
                    len = resp.header("Content-Length")?.toLongOrNull()
                }
            } catch (_: Exception) { }
            if (len == null) {
                try {
                    http.newCall(Request.Builder().url(u).header("Range", "bytes=0-0").build()).execute().use { resp ->
                        val cr = resp.header("Content-Range") // "bytes 0-0/12345"
                        len = cr?.substringAfterLast("/")?.toLongOrNull()
                            ?: resp.header("Content-Length")?.toLongOrNull()
                    }
                } catch (_: Exception) { }
            }
            if (len == null) return -1L
            total += len!!
        }
        return total
    }

    private fun getTo(url: String, dest: File, onProgress: ((done: Long, total: Long) -> Unit)? = null, base: Long = 0L, span: Long = 0L): Boolean {
        return try {
            dest.parentFile?.mkdirs()
            val tmp = File(dest.parentFile, dest.name + ".part")
            http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                if (!resp.isSuccessful || resp.body == null) return false
                val total = resp.header("Content-Length")?.toLongOrNull() ?: -1L
                val body = resp.body!!
                tmp.outputStream().use { out ->
                    val buf = ByteArray(256 * 1024)
                    var done = 0L
                    var lastReport = 0L
                    while (true) {
                        val n = body.byteStream().read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        // throttle callbacks: every ~256KB to avoid spamming the UI thread
                        if (onProgress != null && done - lastReport >= 262144) {
                            lastReport = done
                            try { onProgress(base + done, span.takeIf { it > 0 } ?: total) } catch (_: Exception) { }
                        }
                    }
                    if (onProgress != null) try { onProgress(base + done, span.takeIf { it > 0 } ?: total) } catch (_: Exception) { }
                }
            }
            if (dest.exists()) dest.delete()
            tmp.renameTo(dest)
        } catch (_: Exception) { false }
    }

    // Downloads plan into travel/<sha>, writes meta.json, upserts the Room row. All-or-nothing.
    // onProgress(doneBytes, totalBytes): total may be -1 when the host hides sizes.
    suspend fun downloadPin(ctx: Context, dao: PlaylistDao, v: VideoItem, plan: PinPlan, onProgress: ((Long, Long) -> Unit)? = null): Long {
        val app = ctx.applicationContext
        val key = v.favKey()
        val dir = PinFiles.pinDir(app, key)
        val staging = File(PinFiles.pinDir(app, "__staging_" + PinFiles.sha(key)).let { it.parentFile }, "staging_" + PinFiles.sha(key))
        try { if (staging.exists()) staging.deleteRecursively() } catch (_: Exception) { }
        try {
            staging.mkdirs()
            val vFile = File(staging, "video.mp4")
            val aFile = File(staging, "audio.m4a")
            val sFile = File(staging, "sub.vtt")
            val estTotal = estimateBytes(listOf(plan.videoUrl, plan.audioUrl)).takeIf { it > 0 } ?: 0L
            if (plan.videoUrl != null) {
                if (!getTo(plan.videoUrl, vFile, onProgress, 0L, estTotal / 2)) { staging.deleteRecursively(); return -1L }
            }
            if (plan.audioUrl != null) {
                val base = if (plan.videoUrl != null) estTotal / 2 else 0L
                if (!getTo(plan.audioUrl, aFile, onProgress, base, estTotal - base)) { staging.deleteRecursively(); return -1L }
            } else if (plan.videoUrl == null && plan.audioUrl == null) {
                staging.deleteRecursively(); return -1L
            }
            // audio-only pins store the single stream as video.mp4 (player reads one file either way)
            if (plan.videoUrl == null && plan.audioUrl != null) {
                val only = File(staging, "audio.m4a")
                if (!only.renameTo(vFile)) { staging.deleteRecursively(); return -1L }
            }
            if (!plan.subUrl.isNullOrBlank()) {
                try { getTo(plan.subUrl, sFile) } catch (_: Exception) { try { sFile.delete() } catch (_: Exception) { } }
            }
            val meta = JSONObject()
                .put("kind", plan.kind)
                .put("quality", plan.quality)
                .put("audioTag", plan.audioTag ?: "")
                .put("subTag", plan.subTag ?: "")
                .put("subMime", plan.subMime ?: "")
                .put("subLang", plan.subLang ?: "")
                .put("videoId", v.id ?: "")
                .put("title", v.title ?: "")
            File(staging, "meta.json").writeText(meta.toString())
            try { if (dir.exists()) dir.deleteRecursively() } catch (_: Exception) { }
            if (!staging.renameTo(dir)) {
                // rename across volumes can fail: copy fallback
                try {
                    dir.mkdirs()
                    staging.listFiles()?.forEach { f -> f.copyTo(File(dir, f.name), overwrite = true) }
                    staging.deleteRecursively()
                } catch (_: Exception) { staging.deleteRecursively(); return -1L }
            }
            val size = PinFiles.dirBytes(app, key)
            dao.upsertPin(
                PinEntity(
                    videoKey = key, videoId = v.id, title = v.title, channel = v.channel, url = v.url,
                    kind = plan.kind, quality = plan.quality, audioTag = plan.audioTag, subTag = plan.subTag,
                    sizeBytes = size
                )
            )
            return size
        } catch (_: Exception) {
            try { staging.deleteRecursively() } catch (_: Exception) { }
            return -1L
        }
    }

    suspend fun unpin(ctx: Context, dao: PlaylistDao, key: String) {
        try { PinFiles.delete(ctx.applicationContext, key) } catch (_: Exception) { }
        try { dao.deletePin(key) } catch (_: Exception) { }
    }
}
