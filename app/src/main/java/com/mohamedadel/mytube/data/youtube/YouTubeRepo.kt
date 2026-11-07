package com.mohamedadel.mytube.data.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.stream.StreamInfoItem

data class VideoItem(val id: String? = null, val title: String? = null, val channel: String? = null, val thumbnail: String? = null, val durationSec: Long? = null, val viewCount: Long? = null, val url: String? = null)
data class StreamData(val audioLowestUrl: String? = null, val videoUrlByQuality: Map<String, String> = emptyMap(), val title: String? = null)

object YouTubeRepo {
    private fun service() = NewPipe.getService("YouTube")
    init { runCatching { NewPipe.init(NewPipeDownloaderImpl()) } }

    suspend fun suggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        runCatching {
            val ext = service().suggestionExtractor
            ext.fetchPage()
            ext.getSuggestions(query).getList()
        }.getOrDefault(emptyList())
    }

    suspend fun search(query: String): Pair<List<VideoItem>, String?> = withContext(Dispatchers.IO) {
        runCatching {
            val ext = service().getSearchExtractor(query)
            ext.fetchPage()
            val page = ext.initialPage
            val items = page.items.filterIsInstance<StreamInfoItem>().map { it ->
                val url = it.url
                val id = url?.substringAfter("v=", missingDelimiterValue = "")?.substringBefore("&")?.takeIf { s -> s.isNotEmpty() }
                VideoItem(id, it.name, it.uploaderName, it.thumbnailUrl, it.duration, it.viewCount, url)
            }
            Pair(items, null)
        }.getOrDefault(Pair(emptyList(), null))
    }

    suspend fun stream(url: String): StreamData = withContext(Dispatchers.IO) {
        runCatching {
            val ext = service().getStreamExtractor(url)
            ext.fetchPage()
            val audios = try { ext.audioStreams?.sortedBy { a -> a.averageBitrate } } catch (_: Exception) { emptyList() } ?: emptyList()
            val videos = try { ext.videoStreams?.associate { v -> (v.resolution ?: "auto") to v.content } } catch (_: Exception) { emptyMap() } ?: emptyMap()
            var title: String? = null
            try { title = ext.name } catch (_: Exception) {}
            StreamData(audios.firstOrNull()?.content, videos, title)
        }.getOrDefault(StreamData())
    }
}
