package com.mohamedadel.mytube.data.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.stream.StreamInfoItem

data class VideoItem(val id: String? = null, val title: String? = null, val channel: String? = null, val thumbnail: String? = null, val durationSec: Long? = null, val viewCount: Long? = null, val url: String? = null)
data class StreamData(val audioLowestUrl: String? = null, val videoUrlByQuality: Map<String, String> = emptyMap(), val title: String? = null)
data class SearchPage(val items: List<VideoItem> = emptyList(), val nextPage: Page? = null, val hasNext: Boolean = false)

object YouTubeRepo {
    private fun service() = NewPipe.getService("YouTube")
    init { runCatching { NewPipe.init(NewPipeDownloaderImpl()) } }

    suspend fun suggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList<String>()
        try {
            service().suggestionExtractor.suggestionList(query)
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
            val vids = try { ext.videoStreams } catch (_: Exception) { null } ?: emptyList()
            val map = mutableMapOf<String, String>()
            for (v in vids) {
                try { map[v.resolution ?: "auto"] = v.content } catch (_: Exception) {}
            }
            var title: String? = null
            try { title = ext.name } catch (_: Exception) {}
            StreamData(audioUrl, map, title)
        } catch (_: Exception) { StreamData() }
    }
}
