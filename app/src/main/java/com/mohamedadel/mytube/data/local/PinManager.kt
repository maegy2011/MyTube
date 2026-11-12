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

    // pin-runtime 2026-11-11: browser UA on every pin request. Playback streams via
    // ExoPlayer (browser UA + full header set) while these bare OkHttp calls used to send
    // the default "okhttp/…" UA — hardened videoplayback/cf-media hosts answer that
    // with 403, so EVERY pin failed at estimate/download while playback kept working.
    // Same "Mozilla/5.0" the extractor/search paths already send (YouTubeRepo).
    private const val PIN_UA = "Mozilla/5.0"

    // Approximate total via HEAD content-length, falling back to a 0-byte Range GET
    // (some hosts reject HEAD); -1 when a URL hides its size either way.
    fun estimateBytes(urls: List<String?>): Long {
        var total = 0L
        for (u in urls) {
            if (u.isNullOrBlank()) continue
            var len: Long? = null
            try {
                http.newCall(Request.Builder().url(u).header("User-Agent", PIN_UA).head().build()).execute().use { resp ->
                    len = resp.header("Content-Length")?.toLongOrNull()
                }
            } catch (_: Exception) { }
            if (len == null) {
                try {
                    http.newCall(Request.Builder().url(u).header("User-Agent", PIN_UA).header("Range", "bytes=0-0").build()).execute().use { resp ->
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
    // pin-deepfix 2026-11-11: non-consuming peek — lets runPinDownload tell
    // "user cancelled" apart from "download failed" for the right toast.
    fun isCancelRequested(key: String): Boolean = try { cancelKeys.contains(key) } catch (_: Exception) { false }

    // pause/stop 2026-11-12: PAUSE suspends the OkHttp stream mid-chunk and keeps
    // staging_* (.part + meta) on disk; resume re-enters with Range from the kept
    // byte count (see getTo/downloadPin). STOP = cancel + staging wipe (no resume).
    const val PAUSED = -2L
    private val pauseKeys = java.util.concurrent.ConcurrentHashMap.newKeySet<String>()
    fun requestPause(key: String) { try { pauseKeys.add(key) } catch (_: Exception) { } }
    fun clearPause(key: String) { try { pauseKeys.remove(key) } catch (_: Exception) { } }
    fun isPaused(key: String): Boolean = try { pauseKeys.contains(key) } catch (_: Exception) { false }

    // notif 2026-11-12: per-key failure reason for the download-complete/failure
    // notification (no silent failures). takeError consumes it once.
    private val errByKey = java.util.concurrent.ConcurrentHashMap<String, String>()
    fun takeError(key: String): String? = try { errByKey.remove(key) } catch (_: Exception) { null }
    private fun noteErr(key: String, code: String) { try { errByKey[key] = code } catch (_: Exception) { } }
    fun clearError(key: String) { try { errByKey.remove(key) } catch (_: Exception) { } }

    fun stagingDir(app: Context, key: String): java.io.File {
        val root = try { PinFiles.pinDir(app, "__probe__").parentFile } catch (_: Exception) { null }
        return java.io.File(root, "staging_" + PinFiles.sha(key))
    }
    // STOP companion: deletes the partial file + temp meta; the tracking key itself
    // (pinProgress/pinningKeys) is cleared by the caller on the UI layer.
    fun deleteStaging(app: Context, key: String) {
        try { stagingDir(app, key).deleteRecursively() } catch (_: Exception) { }
    }

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

    // offline fix 2026-11-11: mimeOut receives the response Content-Type so the pin
    // descriptor records the real container (bytes are not always MP4 despite fixed names).
    // pause/stop 2026-11-12: returns 0 = bytes complete, 1 = failed/cancelled,
    // 2 = paused (stream suspended, .part kept for Range resume).
    private fun getTo(url: String, dest: File, onProgress: ((done: Long, total: Long) -> Unit)? = null, base: Long = 0L, span: Long = 0L, isCancelled: (() -> Boolean)? = null, isPaused: (() -> Boolean)? = null, mimeOut: ((String?) -> Unit)? = null, onErr: ((String) -> Unit)? = null): Int {
        return try {
            dest.parentFile?.mkdirs()
            val tmp = File(dest.parentFile, dest.name + ".part")
            // F5 (audit P1 2026-11-11): resume from a previous partial chunk when the host honors Range.
            var done = try { if (tmp.exists()) tmp.length() else 0L } catch (_: Exception) { 0L }
            var respTotal = -1L
            val req = Request.Builder().url(url).header("User-Agent", PIN_UA)
            if (done > 0) req.header("Range", "bytes=$done-")
            http.newCall(req.build()).execute().use { resp ->
                if (done > 0 && resp.code != 206) {
                    // Host ignored Range (200) or rejected it (416/others): restart clean.
                    done = 0
                    try { tmp.delete() } catch (_: Exception) { }
                    if (resp.code != 200) return 1
                }
                if (!resp.isSuccessful || resp.body == null) { try { onErr?.invoke("http" + resp.code) } catch (_: Exception) { }; return 1 }
                try { mimeOut?.invoke(resp.header("Content-Type")?.substringBefore(";")?.trim()) } catch (_: Exception) { }
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
                        // pause/stop 2026-11-12: pause suspends the stream (keeps .part +
                        // bytes-downloaded so resume continues via Range); cancel aborts.
                        if (isPaused?.invoke() == true) return 2
                        if (isCancelled?.invoke() == true) return 1
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
                try { onErr?.invoke("short") } catch (_: Exception) { }
                return 1
            }
            if (dest.exists()) dest.delete()
            tmp.renameTo(dest)
            return 0
        } catch (_: Exception) { try { onErr?.invoke("io") } catch (_: Exception) { }; 1 }
    }

    // SoundCloud HLS pins: VOD m3u8 (fMP4 or TS segments) recorded to one file —
    // init map + segments concatenated (valid MP4 for fMP4; TS concat plays with broken
    // seeking). AES-128 segments decrypted via javax.crypto (key URI from the playlist).
    // Same 0/1/2 codes as getTo. Resume restarts the file (segments re-fetched).
    // Media-sequence is assumed to start at 0 (true for VOD single playlists).
    private fun downloadHlsTo(url: String, dest: File, onProgress: ((done: Long, total: Long) -> Unit)? = null, base: Long = 0L, isCancelled: (() -> Boolean)? = null, isPaused: (() -> Boolean)? = null, mimeOut: ((String?) -> Unit)? = null, onErr: ((String) -> Unit)? = null): Int {
        return try {
            dest.parentFile?.mkdirs()
            val tmp = java.io.File(dest.parentFile, dest.name + ".part")
            try { if (tmp.exists()) tmp.delete() } catch (_: Exception) { }
            fun attr(line: String, name: String): String? = try {
                val m = Regex(name + "=(\"([^\"]*)\"|([^,]*))").find(line) ?: return null
                (m.groups[2]?.value ?: m.groups[3]?.value)?.takeIf { it.isNotEmpty() }
            } catch (_: Exception) { null }
            fun getBytes(u: String): ByteArray? = try {
                http.newCall(Request.Builder().url(u).header("User-Agent", PIN_UA).build()).execute().use { r ->
                    if (!r.isSuccessful || r.body == null) null else r.body!!.bytes()
                }
            } catch (_: Exception) { null }
            val playlistBytes = getBytes(url) ?: run { try { onErr?.invoke("hls-pl") } catch (_: Exception) { }; return 1 }
            val playlist = try { String(playlistBytes, Charsets.UTF_8) } catch (_: Exception) { try { onErr?.invoke("hls-pl") } catch (_: Exception) { }; return 1 }
            var initUri: String? = null
            var keyMethod = "NONE"
            var keyUri: String? = null
            var keyIv: ByteArray? = null
            val segs = mutableListOf<String>()
            for (raw in playlist.lines()) {
                val line = raw.trim()
                if (line.isEmpty()) continue
                when {
                    line.startsWith("#EXT-X-MAP:") -> initUri = attr(line, "URI")
                    line.startsWith("#EXT-X-KEY:") -> {
                        keyMethod = attr(line, "METHOD") ?: "NONE"
                        keyUri = attr(line, "URI")
                        keyIv = attr(line, "IV")?.let { hex ->
                            try {
                                val h = hex.removePrefix("0x").removePrefix("0X")
                                ByteArray(h.length / 2) { i -> h.substring(i * 2, i * 2 + 2).toInt(16).toByte() }
                            } catch (_: Exception) { null }
                        }
                    }
                    !line.startsWith("#") -> segs.add(line)
                }
            }
            if (segs.isEmpty()) { try { onErr?.invoke("hls-empty") } catch (_: Exception) { }; return 1 }
            fun resolve(rel: String): String = try { java.net.URL(java.net.URL(url), rel).toString() } catch (_: Exception) { rel }
            var keyBytes: ByteArray? = null
            if (keyMethod.equals("AES-128", ignoreCase = true)) {
                val ku = keyUri ?: run { try { onErr?.invoke("hls-key") } catch (_: Exception) { }; return 1 }
                keyBytes = getBytes(resolve(ku))
                if (keyBytes == null || keyBytes.size != 16) { try { onErr?.invoke("hls-key") } catch (_: Exception) { }; return 1 }
            }
            val cipher = if (keyBytes != null) try { javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding") } catch (_: Exception) { try { onErr?.invoke("hls-cipher") } catch (_: Exception) { }; return 1 } else null
            val keySpec = if (keyBytes != null) try { javax.crypto.spec.SecretKeySpec(keyBytes, "AES") } catch (_: Exception) { try { onErr?.invoke("hls-cipher") } catch (_: Exception) { }; return 1 } else null
            try { mimeOut?.invoke("audio/mp4") } catch (_: Exception) { }
            var done = 0L
            var lastReport = 0L
            var seq = 0L
            java.io.FileOutputStream(tmp).use { out ->
                if (initUri != null) {
                    val b = getBytes(resolve(initUri!!)) ?: run { try { onErr?.invoke("hls-init") } catch (_: Exception) { }; return 1 }
                    out.write(b); done += b.size
                }
                for (s in segs) {
                    if (isPaused?.invoke() == true) return 2
                    if (isCancelled?.invoke() == true) return 1
                    var b = getBytes(resolve(s)) ?: run { try { onErr?.invoke("hls-seg") } catch (_: Exception) { }; return 1 }
                    if (cipher != null && keySpec != null) {
                        val iv = keyIv ?: ByteArray(16).also { bb -> for (i in 0..7) bb[15 - i] = ((seq shr (i * 8)) and 0xFF).toByte() }
                        try {
                            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, javax.crypto.spec.IvParameterSpec(iv))
                            b = cipher.doFinal(b)
                        } catch (_: Exception) { try { onErr?.invoke("hls-dec") } catch (_: Exception) { }; return 1 }
                    }
                    out.write(b); done += b.size; seq++
                    if (onProgress != null && done - lastReport >= 262144) {
                        lastReport = done
                        try { onProgress(base + done, -1) } catch (_: Exception) { }
                    }
                }
            }
            if (onProgress != null) try { onProgress(base + done, -1) } catch (_: Exception) { }
            if (dest.exists()) dest.delete()
            tmp.renameTo(dest)
            return 0
        } catch (_: Exception) { try { onErr?.invoke("hls-io") } catch (_: Exception) { }; 1 }
    }

    // Downloads plan into travel/<sha>, writes meta.json, upserts the Room row. All-or-nothing.
    // onProgress(doneBytes, totalBytes): total may be -1 when the host hides sizes.
    // F5 resume: .part chunks survive a failed attempt (same staging dir); retry continues via Range.
    suspend fun downloadPin(ctx: Context, dao: PlaylistDao, v: VideoItem, plan: PinPlan, onProgress: ((Long, Long) -> Unit)? = null, resume: Boolean = false): Long {
        val app = ctx.applicationContext
        val key = v.favKey()
        if (consumeCancel(key)) return -1L
        val travelRoot = try { PinFiles.pinDir(app, "__probe__").parentFile } catch (_: Exception) { null }
        if (travelRoot != null) sweepStaleStaging(travelRoot)
        val dir = PinFiles.pinDir(app, key)
        val staging = stagingDir(app, key)
        // pause/stop 2026-11-12: resume keeps staging (.part byte counts are the persisted
        // resume offsets — getTo re-sends Range from the kept length); fresh starts wipe.
        if (!(resume && staging.exists())) {
            try { if (staging.exists()) staging.deleteRecursively() } catch (_: Exception) { }
        }
        try {
            staging.mkdirs()
            val vFile = File(staging, "video.mp4")
            val aFile = File(staging, "audio.m4a")
            val sFile = File(staging, "sub.vtt")
            val estTotal = estimateBytes(listOf(plan.videoUrl, plan.audioUrl)).takeIf { it > 0 } ?: 0L
            // single-stream pins span the full estimate (the old /2 showed a stuck 50% bar)
            val videoSpan = if (plan.audioUrl != null) estTotal / 2 else estTotal
            var videoMime: String? = null
            var audioMime: String? = null
            // pause/stop 2026-11-12: rc 2 = paused (keep staging for Range resume),
            // rc 1 + cancel flag = STOP (wipe partial + temp meta, stay silent upstream).
            if (plan.videoUrl != null) {
                val rc = getTo(plan.videoUrl, vFile, onProgress, 0L, videoSpan, { cancelKeys.contains(key) }, { pauseKeys.contains(key) }, { m -> videoMime = m }, { e -> noteErr(key, e) })
                if (rc != 0) { if (rc == 1 && !cancelKeys.contains(key)) staging.deleteRecursively(); return if (rc == 2) PAUSED else -1L }
            }
            if (plan.audioUrl != null) {
                val base = if (plan.videoUrl != null) videoSpan else 0L
                // HLS media (SoundCloud transcodings) is a playlist, not bytes — record it.
                val aHls = plan.audioUrl.contains("m3u8", ignoreCase = true)
                val rc = if (aHls) downloadHlsTo(plan.audioUrl, aFile, onProgress, base, { cancelKeys.contains(key) }, { pauseKeys.contains(key) }, { m -> audioMime = m }, { e -> noteErr(key, e) })
                else getTo(plan.audioUrl, aFile, onProgress, base, estTotal - base, { cancelKeys.contains(key) }, { pauseKeys.contains(key) }, { m -> audioMime = m }, { e -> noteErr(key, e) })
                if (rc != 0) { if (rc == 1 && !cancelKeys.contains(key)) staging.deleteRecursively(); return if (rc == 2) PAUSED else -1L }
            } else if (plan.videoUrl == null && plan.audioUrl == null) {
                noteErr(key, "plan"); staging.deleteRecursively(); return -1L
            }
            // audio-only pins store the single stream as video.mp4 (player reads one file either way)
            if (plan.videoUrl == null && plan.audioUrl != null) {
                val only = File(staging, "audio.m4a")
                if (!only.renameTo(vFile)) { staging.deleteRecursively(); noteErr(key, "io"); return -1L }
            }
            if (!plan.subUrl.isNullOrBlank()) {
                try { getTo(plan.subUrl, sFile) } catch (_: Exception) { try { sFile.delete() } catch (_: Exception) { } }
            }
            // extra subtitle tracks: sub2.vtt, sub3.vtt… (sub.vtt stays the first for legacy readers)
            val extraSubs = if (plan.subs.size > 1) plan.subs.drop(1) else emptyList()
            val extraNames = mutableListOf<String>()
            extraSubs.forEachIndexed { i, ps ->
                val f = File(staging, "sub${i + 2}.vtt")
                try { if (getTo(ps.url, f) == 0) extraNames.add(f.name) else try { f.delete() } catch (_: Exception) { } } catch (_: Exception) { }
            }
            // audio-only pins store the single stream as video.mp4: the mime still
            // describes the AUDIO bytes, so playback tags the item correctly.
            if (plan.videoUrl == null && plan.audioUrl != null) videoMime = audioMime
            val meta = JSONObject()
                .put("videoMime", videoMime ?: "")
                .put("audioMime", audioMime ?: "")
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
                } catch (_: Exception) { staging.deleteRecursively(); noteErr(key, "io"); return -1L }
                try { if (staging.exists()) staging.deleteRecursively() } catch (_: Exception) { }
            }
            try { cancelKeys.remove(key) } catch (_: Exception) { }
            try { errByKey.remove(key) } catch (_: Exception) { }
            val size = PinFiles.dirBytes(app, key)
            // F2 (audit P0 2026-11-11): post-download length check — refuse empty/zero-byte snapshots.
            try {
                val vf = File(dir, "video.mp4")
                if (!vf.exists() || vf.length() <= 0L) { try { dir.deleteRecursively() } catch (_: Exception) { }; noteErr(key, "empty"); return -1L }
                if (estTotal > 0 && size > 0 && size < estTotal / 2) { try { dir.deleteRecursively() } catch (_: Exception) { }; noteErr(key, "short"); return -1L }
            } catch (_: Exception) { }
            dao.upsertPin(
                PinEntity(
                    videoKey = key, videoId = v.id, title = v.title, channel = v.channel, url = v.url,
                    kind = plan.kind, quality = plan.quality, audioTag = plan.audioTag, subTag = plan.subTag,
                    sizeBytes = size
                )
            )
            return size
        } catch (e: Exception) {
            try { staging.deleteRecursively() } catch (_: Exception) { }
            try { noteErr(key, "ex:" + (e.message?.take(80) ?: "?")) } catch (_: Exception) { }
            return -1L
        }
    }

    suspend fun unpin(ctx: Context, dao: PlaylistDao, key: String) {
        try { PinFiles.delete(ctx.applicationContext, key) } catch (_: Exception) { }
        try { dao.deletePin(key) } catch (_: Exception) { }
    }
}
