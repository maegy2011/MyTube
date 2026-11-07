package com.mohamedadel.mytube.data.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.net.URLEncoder

data class VideoItem(val id: String? = null, val title: String? = null, val channel: String? = null, val thumbnail: String? = null, val durationSec: Long? = null, val viewCount: Long? = null, val url: String? = null)
data class StreamData(val audioLowestUrl: String? = null, val videoUrlByQuality: Map<String, String> = emptyMap(), val muxedQualities: Set<String> = emptySet(), val title: String? = null)
data class SearchPage(val items: List<VideoItem> = emptyList(), val nextPage: Page? = null, val hasNext: Boolean = false)

object YouTubeRepo {
    private val client = OkHttpClient()
    private fun service() = NewPipe.getService("YouTube")
    init { runCatching { NewPipe.init(NewPipeDownloaderImpl()) } }

    suspend fun suggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList<String>()
        // Try NewPipe first
        try {
            val list = service().suggestionExtractor.suggestionList(query)
            if (list.isNotEmpty()) return@withContext list.take(8)
        } catch (_: Exception) { }
        // Fallback: Google YouTube suggest (works for Arabic and other scripts)
        try {
            val url = "https://suggestqueries.google.com/complete/search?client=firefox&ds=yt&q=" +
                URLEncoder.encode(query, "UTF-8")
            val req = Request.Builder().url(url)
                .header("Accept-Language", "ar,en;q=0.9")
                .header("User-Agent", "Mozilla/5.0").build()
            client.newCall(req).execute().use { r ->
                val body = r.body?.string() ?: ""
                val arr = JSONArray(body)
                val sugg = arr.optJSONArray(1) ?: return@withContext emptyList()
                val out = mutableListOf<String>()
                for (i in 0 until sugg.length()) {
                    val item = sugg.get(i)
                    val text = if (item is JSONArray) item.optString(0) else item.toString()
                    if (text.isNotBlank()) out.add(text)
                }
                out.take(8)
            }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun search(query: String, page: Page? = null): SearchPage = withContext(Dispatchers.IO) {
        try {
            val ext = service().getSearchExtractor(query)
            val lp = if (page == null) { ext.fetchPage(); ext.initialPage } else { ext.getPage(page) }
            val items = lp.items
            val out = mutableListOf<VideoItem>()
            for (info in items) {
                if (info is StreamInfoItem) {
                    val url: String? = try { info.url } catch (_: Exception) { null }
                    val name: String? = try { info.name } catch (_: Exception) { null }
                    val up: String? = try { info.uploaderName } catch (_: Exception) { null }
                    var thumb: String? = null
                    try { thumb = info.thumbnails.firstOrNull()?.url } catch (_: Exception) {}
                    var dur: Long? = null
                    try { dur = info.duration } catch (_: Exception) {}
                    var views: Long? = null
                    try { views = info.viewCount } catch (_: Exception) {}
                    val id: String? = url?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    out.add(VideoItem(id, name, up, thumb, dur, views, url))
                }
            }
            val hasNext = try { lp.hasNextPage() } catch (_: Exception) { false }
            val next = if (hasNext) try { lp.nextPage } catch (_: Exception) { null } else null
            SearchPage(out, next, hasNext)
        } catch (_: Exception) { SearchPage() }
    }

    suspend fun stream(url: String): StreamData = withContext(Dispatchers.IO) {
        try {
            val ext = service().getStreamExtractor(url)
            ext.fetchPage()
            val audios = try { ext.audioStreams } catch (_: Exception) { null } ?: emptyList()
            val lowest = audios.sortedBy { a -> try { a.averageBitrate } catch (_: Exception) { Int.MAX_VALUE } }.firstOrNull()
            val audioUrl: String? = try { lowest?.content } catch (_: Exception) { null }
            val map = mutableMapOf<String, String>()
            val muxed = mutableSetOf<String>()
            // Combined (audio+video) streams
            val combined = try { ext.videoStreams } catch (_: Exception) { null } ?: emptyList()
            for (v in combined) {
                try {
                    val key = (v.resolution ?: "").ifBlank { "auto" }
                    if (!map.containsKey(key)) {
                        map[key] = v.content
                        muxed.add(key)
                    }
                } catch (_: Exception) {}
            }
            // Video-only (DASH) streams for higher qualities
            val videoOnly = try { ext.videoOnlyStreams } catch (_: Exception) { null } ?: emptyList()
            var idx = 0
            for (v in videoOnly) {
                try {
                    val base = (v.resolution ?: "").ifBlank { "video$idx" }
                    if (!map.containsKey(base)) {
                        map[base] = v.content
                    }
                    idx++
                } catch (_: Exception) {}
            }
            // ensure uniqueness: if duplicate keys slipped, re-key
            var title: String? = null
            try { title = ext.name } catch (_: Exception) {}
            StreamData(audioUrl, map, muxed, title)
        } catch (_: Exception) { StreamData() }
    }
}
