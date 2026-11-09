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
    fun buildPlan(sd: StreamData, quality: String, audioIdx: Int, subIdx: Int): PinPlan? {
        val selAudio = sd.audioTracks.getOrNull(audioIdx)?.url ?: sd.audioLowestUrl
        val audioTag = sd.audioTracks.getOrNull(audioIdx)?.let { it.localeTag ?: it.name }
        val url = when {
            quality == "audio-only (default)" -> selAudio
            quality.startsWith("auto") -> autoUrl(sd, 0)
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

    // Approximate total via HEAD content-length; -1 when any URL hides it.
    fun estimateBytes(urls: List<String?>): Long {
        var total = 0L
        for (u in urls) {
            if (u.isNullOrBlank()) continue
            try {
                val req = Request.Builder().url(u).head().build()
                http.newCall(req).execute().use { resp ->
                    val len = resp.header("Content-Length")?.toLongOrNull() ?: return -1L
                    total += len
                }
            } catch (_: Exception) { return -1L }
        }
        return total
    }

    private fun getTo(url: String, dest: File): Boolean {
        return try {
            dest.parentFile?.mkdirs()
            val tmp = File(dest.parentFile, dest.name + ".part")
            http.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                if (!resp.isSuccessful || resp.body == null) return false
                tmp.outputStream().use { out -> resp.body!!.byteStream().copyTo(out) }
            }
            if (dest.exists()) dest.delete()
            tmp.renameTo(dest)
        } catch (_: Exception) { false }
    }

    // Downloads plan into travel/<sha>, writes meta.json, upserts the Room row. All-or-nothing.
    suspend fun downloadPin(ctx: Context, dao: PlaylistDao, v: VideoItem, plan: PinPlan): Long {
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
            if (plan.videoUrl != null) {
                if (!getTo(plan.videoUrl, vFile)) { staging.deleteRecursively(); return -1L }
            }
            if (plan.audioUrl != null) {
                if (!getTo(plan.audioUrl, aFile)) { staging.deleteRecursively(); return -1L }
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
