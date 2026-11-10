package com.mohamedadel.mytube.data.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import java.util.concurrent.TimeUnit

// SponsorBlock: community skip segments (sponsor/selfpromo/intro/outro).
// Privacy: enabling this sends watched video IDs to sponsor.ajay.app (third party).
// Default OFF; the settings row says so. All calls best-effort — failure = no skipping.
data class SbSeg(val startMs: Long, val endMs: Long)

object SponsorBlock {
    // Fixed category set (simple on/off — no per-category matrix).
    private const val CATS = "[\"sponsor\",\"selfpromo\",\"intro\",\"outro\"]"
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    private val cache = mutableMapOf<String, List<SbSeg>>()

    suspend fun fetch(videoId: String): List<SbSeg> {
        cache[videoId]?.let { return it }
        val out = mutableListOf<SbSeg>()
        try {
            withContext(Dispatchers.IO) {
                val url = "https://sponsor.ajay.app/api/skipSegments?videoID=" + videoId + "&categories=" +
                    java.net.URLEncoder.encode(CATS, "UTF-8")
                val req = Request.Builder().url(url).header("User-Agent", "MyTube/1.0").build()
                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext
                    val body = resp.body?.string() ?: return@withContext
                    val arr = try { JSONArray(body) } catch (_: Exception) { return@withContext }
                    for (i in 0 until arr.length()) {
                        try {
                            val seg = arr.getJSONObject(i).getJSONArray("segment")
                            val s = (seg.getDouble(0) * 1000).toLong()
                            val e = (seg.getDouble(1) * 1000).toLong()
                            if (e > s && s >= 0) out.add(SbSeg(s, e))
                        } catch (_: Exception) { }
                    }
                }
            }
        } catch (_: Exception) { }
        if (out.isEmpty()) cache.remove(videoId) else cache[videoId] = out
        if (cache.size > 50) cache.clear()
        return out
    }

    fun clear(videoId: String) { cache.remove(videoId) }
}
