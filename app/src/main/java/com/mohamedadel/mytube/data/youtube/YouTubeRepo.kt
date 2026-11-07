package com.mohamedadel.mytube.data.youtube

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem

data class VideoItem(val id: String? = null, val title: String? = null, val channel: String? = null, val thumbnail: String? = null, val durationSec: Long? = null, val viewCount: Long? = null, val url: String? = null)
data class StreamData(val audioLowestUrl: String? = null, val videoUrlByQuality: Map<String, String> = emptyMap(), val title: String? = null)

object YouTubeRepo {
    private val service = ServiceList.YouTube
    init { runCatching { NewPipe.init(NewPipeDownloaderImpl()) } }

    suspend fun suggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        runCatching {
            service.suggestionExtractor.suggestionList(query).getList()
        }.getOrDefault(emptyList())
    }

    suspend fun search(query: String, pageToken: String? = null): Pair<List<VideoItem>, String?> = withContext(Dispatchers.IO) {
        val extractor = service.getSearchExtractor(query)
        extractor.fetchPage()
        val items = extractor.initialPage.items.filterIsInstance<StreamInfoItem>().map {
            VideoItem(it.id, it.name, it.uploaderName, it.thumbnailUrl, it.duration, it.viewCount, it.url)
        }
        Pair(items, null) // MVP: first page only, paging extension next iteration
    }

    suspend fun stream(url: String): StreamData = withContext(Dispatchers.IO) {
        val extractor = service.getStreamExtractor(url)
        extractor.fetchPage()
        val audios = extractor.audioStreams?.sortedBy { it.averageBitrate } ?: emptyList()
        val videos = extractor.videoStreams?.associate { (it.resolution ?: "auto") to it.content } ?: emptyMap()
        StreamData(audios.firstOrNull()?.content, videos, extractor.name)
    }
}
