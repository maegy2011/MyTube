package com.mohamedadel.mytube.data.local

import android.content.Context
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.util.qualityHeight
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.util.autoUrl
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

    data class PinSub(val url: String, val mime: String?, val lang: String?, val label: String?)

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
,
        val ageLimit: Int = 0,
        // Multi-subtitle: every selected track downloads (sub.vtt = first for legacy readers).
        val subs: List<PinSub> = emptyList()
    )

    // Same URL math as MainActivity.applyQuality (muxed vs merged, auto mapping).
    // kbps must be the live estimate (not 0): "auto" resolves differently per bandwidth,
    // and pinning must capture exactly what the player would play.
    fun buildPlan(sd: StreamData, quality: String, audioIdx: Int, subIdxs: Set<Int>, kbps: Int = 0, maxH: Int = Int.MAX_VALUE): PinPlan? {
        // Legacy single-index callers pass one element; empty = no subtitles.
        return buildPlanMulti(sd, quality, audioIdx, subIdxs, kbps, maxH)
    }

    fun buildPlanMulti(sd: StreamData, quality: String, audioIdx: Int, subIdxs: Set<Int>, kbps: Int = 0, maxH: Int = Int.MAX_VALUE): PinPlan? {
        val sd = if (maxH == Int.MAX_VALUE) sd else try { sd.copy(videoUrlByQuality = sd.videoUrlByQuality.filterKeys { (qualityHeight(it) ?: 0) <= maxH }) } catch (_: Exception) { sd }
        val selAudio = sd.audioTracks.getOrNull(audioIdx)?.url ?: sd.audioLowestUrl
        val audioTag = sd.audioTracks.getOrNull(audioIdx)?.let { it.localeTag ?: it.name }
        val url = when {
            quality == "audio-only (default)" -> selAudio
            quality.startsWith("auto") -> autoUrl(sd, kbps)
            else -> sd.videoUrlByQuality[quality]
        } ?: return null
        val picked = subIdxs.sorted().mapNotNull { i ->
            sd.subtitles.getOrNull(i)?.let { t -> PinSub(t.url, t.mime, t.langTag, t.label ?: t.langTag) }
        }
        val first = picked.firstOrNull()
        if (quality == "audio-only (default)") {
            return PinPlan(null, selAudio, first?.url, first?.mime, first?.lang, "audio", quality, audioTag, first?.label, ageLimit = sd.ageLimit, subs = picked)
        }
        val muxedUrls = sd.videoUrlByQuality.filter { sd.muxedQualities.contains(it.key) }.values.toSet()
        val isMuxed = url in muxedUrls
        return PinPlan(
            videoUrl = url,
            audioUrl = if (isMuxed) null else selAudio,
            subUrl = first?.url, subMime = first?.mime, subLang = first?.lang,
            kind = "video", quality = quality, audioTag = audioTag, subTag = first?.label,
            ageLimit = sd.ageLimit, subs = picked
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

    // F5 (audit P1 2026-11-11): cooperative cancellation — MainActivity registers a key
    // here and DownloadsScreen offers a cancel button per in-flight pin.
    private val cancelKeys = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    fun requestCancel(key: String) { try { cancelKeys.add(key) } catch (_: Exception) { } }
    fun consumeCancel(key: String): Boolean = try { cancelKeys.remove(key) } catch (_: Exception) { false }

    // F14 (audit P2 2026-11-11): startup sweep — a killed download leaves staging_* orphans
    // (invisible to existingDir/totalBytes). Called once per launch from MyTubeApp on a worker thread.
    fun sweepOnStartup(app: Context) {
        try {
            val root = try { PinFiles.pinDir(app, "__probe__").parentFile } catch (_: Exception) { null }
            if (root != null) sweepStaleStaging(root)
        } catch (_: Exception) { }
    }

    private fun sweepStaleStaging(root: File, maxAgeMs: Long = 24L * 3600 * 1000) {
        // F5 companion: interrupted pins keep .part for resume; abandon them after a day.
        try {
            val now = System.currentTimeMillis()
            root.listFiles()?.forEach { f ->
                try { if (f.isDirectory && f.name.startsWith("staging_") && now - f.lastModified() > maxAgeMs) f.deleteRecursively() } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    private fun getTo(url: String, dest: File, onProgress: ((done: Long, total: Long) -> Unit)? = null, base: Long = 0L, span: Long = 0L, isCancelled: (() -> Boolean)? = null): Boolean {
        return try {
            dest.parentFile?.mkdirs()
            val tmp = File(dest.parentFile, dest.name + ".part")
            // F5 (audit P1 2026-11-11): resume from a previous partial chunk when the host honors Range.
            var done = try { if (tmp.exists()) tmp.length() else 0L } catch (_: Exception) { 0L }
            var respTotal = -1L
            val req = Request.Builder().url(url)
            if (done > 0) req.header("Range", "bytes=$done-")
            http.newCall(req.build()).execute().use { resp ->
                if (done > 0 && resp.code != 206) {
                    // Host ignored Range (200) or rejected it (416/others): restart clean.
                    done = 0
                    try { tmp.delete() } catch (_: Exception) { }
                    if (resp.code != 200) return false
                }
                if (!resp.isSuccessful || resp.body == null) return false
                // F2 (audit P0 2026-11-11): capture the declared size from THIS response —
                // no extra HEAD round-trip; a short read below is rejected as truncated.
                // With 206 the Content-Range carries the FULL length ("bytes 100-199/1000").
                respTotal = if (resp.code == 206) {
                    resp.header("Content-Range")?.substringAfterLast("/")?.toLongOrNull()?.let { it } ?: -1L
                } else {
                    resp.header("Content-Length")?.toLongOrNull() ?: -1L
                }
                val body = resp.body!!
                val append = done > 0 && resp.code == 206
                java.io.FileOutputStream(tmp, append).use { out ->
                    val buf = ByteArray(256 * 1024)
                    var lastReport = 0L
                    val input = body.byteStream()
                    while (true) {
                        if (isCancelled?.invoke() == true) return false
                        val n = input.read(buf)
                        if (n < 0) break
                        out.write(buf, 0, n)
                        done += n
                        // throttle callbacks: every ~256KB to avoid spamming the UI thread
                        if (onProgress != null && done - lastReport >= 262144) {
                            lastReport = done
                            try { onProgress(base + done, span.takeIf { it > 0 } ?: respTotal) } catch (_: Exception) { }
                        }
                    }
                    if (onProgress != null) try { onProgress(base + done, span.takeIf { it > 0 } ?: respTotal) } catch (_: Exception) { }
                }
            }
            if (respTotal > 0 && done != respTotal) {
                // keep .part on disk: a retry resumes via Range instead of restarting.
                return false
            }
            if (dest.exists()) dest.delete()
            tmp.renameTo(dest)
        } catch (_: Exception) { false }
    }

    // Downloads plan into travel/<sha>, writes meta.json, upserts the Room row. All-or-nothing.
    // onProgress(doneBytes, totalBytes): total may be -1 when the host hides sizes.
    // F5 resume: .part chunks survive a failed attempt (same staging dir); retry continues via Range.
    suspend fun downloadPin(ctx: Context, dao: PlaylistDao, v: VideoItem, plan: PinPlan, onProgress: ((Long, Long) -> Unit)? = null): Long {
        val app = ctx.applicationContext
        val key = v.favKey()
        if (consumeCancel(key)) return -1L
        val travelRoot = try { PinFiles.pinDir(app, "__probe__").parentFile } catch (_: Exception) { null }
        if (travelRoot != null) sweepStaleStaging(travelRoot)
        val dir = PinFiles.pinDir(app, key)
        val staging = File(PinFiles.pinDir(app, "__staging_" + PinFiles.sha(key)).let { it.parentFile }, "staging_" + PinFiles.sha(key))
        try { if (staging.exists()) staging.deleteRecursively() } catch (_: Exception) { }
        try {
            staging.mkdirs()
            val vFile = File(staging, "video.mp4")
            val aFile = File(staging, "audio.m4a")
            val sFile = File(staging, "sub.vtt")
            val estTotal = estimateBytes(listOf(plan.videoUrl, plan.audioUrl)).takeIf { it > 0 } ?: 0L
            // single-stream pins span the full estimate (the old /2 showed a stuck 50% bar)
            val videoSpan = if (plan.audioUrl != null) estTotal / 2 else estTotal
            if (plan.videoUrl != null) {
                if (!getTo(plan.videoUrl, vFile, onProgress, 0L, videoSpan) { cancelKeys.contains(key) }) { if (!cancelKeys.contains(key)) staging.deleteRecursively(); return -1L }
            }
            if (plan.audioUrl != null) {
                val base = if (plan.videoUrl != null) videoSpan else 0L
                if (!getTo(plan.audioUrl, aFile, onProgress, base, estTotal - base) { cancelKeys.contains(key) }) { if (!cancelKeys.contains(key)) staging.deleteRecursively(); return -1L }
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
            // extra subtitle tracks: sub2.vtt, sub3.vtt… (sub.vtt stays the first for legacy readers)
            val extraSubs = if (plan.subs.size > 1) plan.subs.drop(1) else emptyList()
            val extraNames = mutableListOf<String>()
            extraSubs.forEachIndexed { i, ps ->
                val f = File(staging, "sub${i + 2}.vtt")
                try { if (getTo(ps.url, f)) extraNames.add(f.name) else try { f.delete() } catch (_: Exception) { } } catch (_: Exception) { }
            }
            val meta = JSONObject()
                .put("kind", plan.kind)
                .put("quality", plan.quality)
                .put("audioTag", plan.audioTag ?: "")
                .put("subTag", plan.subTag ?: "")
                .put("subMime", plan.subMime ?: "")
                .put("subLang", plan.subLang ?: "")
                .put("subs", org.json.JSONArray().apply {
                    plan.subs.forEachIndexed { i, ps ->
                        put(org.json.JSONObject()
                            .put("file", if (i == 0) "sub.vtt" else "sub${i + 1}.vtt")
                            .put("lang", ps.lang ?: "")
                            .put("label", ps.label ?: "")
                            .put("mime", ps.mime ?: "text/vtt"))
                    }
                })
                .put("age", plan.ageLimit)
                .put("videoId", v.id ?: "")
                .put("title", v.title ?: "")
                // F2 (audit P0 2026-11-11): expected bytes for post-download verification on the play path.
                .put("expectedBytes", estTotal)
            File(staging, "meta.json").writeText(meta.toString())
            try { if (dir.exists()) dir.deleteRecursively() } catch (_: Exception) { }
            if (!staging.renameTo(dir)) {
                // rename across volumes can fail: copy fallback
                try {
                    dir.mkdirs()
                    staging.listFiles()?.forEach { f -> f.copyTo(File(dir, f.name), overwrite = true) }
                    staging.deleteRecursively()
                } catch (_: Exception) { staging.deleteRecursively(); return -1L }
                try { if (staging.exists()) staging.deleteRecursively() } catch (_: Exception) { }
            }
            try { cancelKeys.remove(key) } catch (_: Exception) { }
            val size = PinFiles.dirBytes(app, key)
            // F2 (audit P0 2026-11-11): post-download length check — refuse empty/zero-byte snapshots.
            try {
                val vf = File(dir, "video.mp4")
                if (!vf.exists() || vf.length() <= 0L) { try { dir.deleteRecursively() } catch (_: Exception) { }; return -1L }
                if (estTotal > 0 && size > 0 && size < estTotal / 2) { try { dir.deleteRecursively() } catch (_: Exception) { }; return -1L }
            } catch (_: Exception) { }
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
