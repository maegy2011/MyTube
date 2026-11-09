package com.mohamedadel.mytube.data.youtube

import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.net.URLEncoder

data class VideoItem(val id: String? = null, val title: String? = null, val channel: String? = null, val thumbnail: String? = null, val durationSec: Long? = null, val viewCount: Long? = null, val url: String? = null, val channelAvatar: String? = null, val verified: Boolean = false, val uploadDate: String? = null, val isShort: Boolean = false)
data class SubtitleItem(val url: String, val langTag: String, val label: String, val auto: Boolean, val mime: String)
data class AudioTrackItem(val url: String, val bitrate: Int, val trackId: String?, val name: String?, val localeTag: String?, val type: String?)
data class ChapterItem(val title: String, val startSec: Int)
data class ChannelDetail(val name: String? = null, val avatar: String? = null, val banner: String? = null, val subs: Long? = null, val description: String? = null, val verified: Boolean = false)
data class CommentItem(val author: String? = null, val text: String? = null, val likes: Int = 0, val avatar: String? = null, val date: String? = null)
data class StreamData(val audioLowestUrl: String? = null, val videoUrlByQuality: Map<String, String> = emptyMap(), val muxedQualities: Set<String> = emptySet(), val title: String? = null, val subtitles: List<SubtitleItem> = emptyList(), val audioTracks: List<AudioTrackItem> = emptyList(), val likeCount: Long? = null, val description: String? = null, val uploadDate: String? = null, val category: String? = null, val tags: List<String> = emptyList(), val uploaderVerified: Boolean = false, val subscriberCount: Long? = null, val uploaderAvatar: String? = null, val language: String? = null, val channelUrl: String? = null, val chapters: List<ChapterItem> = emptyList(), val related: List<VideoItem> = emptyList())
data class SearchPage(val items: List<VideoItem> = emptyList(), val nextPage: Page? = null, val hasNext: Boolean = false, val error: String? = null)

object YouTubeRepo {
    private val client = OkHttpClient()
    private fun decodeHtml(s: String): String {
        return try { Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString() } catch (_: Exception) { s }
    }
    private fun service() = NewPipe.getService("YouTube")
    init { runCatching { NewPipe.init(NewPipeDownloaderImpl()) } }

    // Real per-country kiosks (verified against NewPipeExtractor 0.26.5 YoutubeService):
    // default Trending + trending_music + trending_gaming + trending_movies_and_shows
    // + trending_podcasts_episodes + live. "news" has NO kiosk — served as region search, labeled honestly.
    // Any failing kiosk returns empty (caller shows the fallback, never a fake list).
    suspend fun trending(region: String? = null, kioskId: String? = null): List<VideoItem> = withContext(Dispatchers.IO) {
        try {
            applyRegion(region)
            val svc = service()
            val kiosk = try {
                if (kioskId.isNullOrBlank()) svc.kioskList.getDefaultKioskExtractor(null)
                else svc.kioskList.getExtractorById(kioskId, null)
            } catch (_: Exception) { null } ?: return@withContext emptyList()
            kiosk.fetchPage()
            val lp = try { kiosk.initialPage } catch (_: Exception) { return@withContext emptyList() }
            val out = mutableListOf<VideoItem>()
            for (info in lp.items) {
                if (info is StreamInfoItem) {
                    val url: String? = try { info.url } catch (_: Exception) { null }
                    val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { info.name }
                    val up: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { info.uploaderName }
                    var thumb: String? = null
                    try { thumb = info.thumbnails.firstOrNull()?.url } catch (_: Exception) {}
                    var dur: Long? = null
                    try { dur = info.duration } catch (_: Exception) {}
                    var views: Long? = null
                    try { views = info.viewCount } catch (_: Exception) {}
                    var avatar: String? = null
                    try { avatar = info.uploaderAvatars.firstOrNull()?.url } catch (_: Exception) {}
                    var verified = false
                    try { verified = info.isUploaderVerified } catch (_: Exception) {}
                    var upDate: String? = null
                    try { upDate = info.textualUploadDate } catch (_: Exception) {}
                    var isShort = false
                    try { isShort = info.isShortFormContent } catch (_: Exception) {}
                    val id: String? = url?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    out.add(VideoItem(id, name, up, thumb, dur, views, url, avatar, verified, upDate, isShort))
                }
            }
            out
        } catch (_: Exception) { emptyList() }
    }

    suspend fun suggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList<String>()
        // Try NewPipe first
        try {
            val list = service().suggestionExtractor.suggestionList(query)
            if (list.isNotEmpty()) return@withContext list.map { decodeHtml(it) }.take(8)
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
                    if (text.isNotBlank()) out.add(decodeHtml(text))
                }
                out.take(8)
            }
        } catch (_: Exception) { emptyList() }
    }

    // Manual content country (no IP, no permission): drives YouTube search/trending region.
    // "global" = leave NewPipe default; otherwise force Arabic + chosen country.
    fun applyRegion(country: String?) {
        runCatching {
            if (!country.isNullOrBlank() && country != "global")
                NewPipe.setupLocalization(Localization("ar", country))
        }
    }

    // content filter: "all" (default) | "videos" | "channels" | "playlists" (verified via javap:
    // YoutubeSearchQueryHandlerFactory.getAvailableContentFilter; NO sort API exists)
    suspend fun search(query: String, page: Page? = null, region: String? = null, contentFilter: String = "all"): SearchPage = withContext(Dispatchers.IO) {
        try {
            applyRegion(region)
            val ext = try {
                val qh = service().searchQHFactory.fromQuery(query, listOf(contentFilter), "")
                service().getSearchExtractor(qh)
            } catch (_: Exception) { service().getSearchExtractor(query) }
            val lp = if (page == null) { ext.fetchPage(); ext.initialPage } else { ext.getPage(page) }
            val items = lp.items
            val out = mutableListOf<VideoItem>()
            for (info in items) {
                if (info is StreamInfoItem) {
                    val url: String? = try { info.url } catch (_: Exception) { null }
                    val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { info.name }
                    val up: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { info.uploaderName }
                    var thumb: String? = null
                    try { thumb = info.thumbnails.firstOrNull()?.url } catch (_: Exception) {}
                    var dur: Long? = null
                    try { dur = info.duration } catch (_: Exception) {}
                    var views: Long? = null
                    try { views = info.viewCount } catch (_: Exception) {}
                    var avatar: String? = null
                    try { avatar = info.uploaderAvatars.firstOrNull()?.url } catch (_: Exception) {}
                    var verified = false
                    try { verified = info.isUploaderVerified } catch (_: Exception) {}
                    var upDate: String? = null
                    try { upDate = info.textualUploadDate } catch (_: Exception) {}
                    var isShort = false
                    try { isShort = info.isShortFormContent } catch (_: Exception) {}
                    val id: String? = url?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    out.add(VideoItem(id, name, up, thumb, dur, views, url, avatar, verified, upDate, isShort))
                }
            }
            val hasNext = try { lp.hasNextPage() } catch (_: Exception) { false }
            val next = if (hasNext) try { lp.nextPage } catch (_: Exception) { null } else null
            SearchPage(out, next, hasNext)
        } catch (e: Exception) { SearchPage(error = e.message ?: e.javaClass.simpleName) }
    }

    suspend fun stream(url: String): StreamData = withContext(Dispatchers.IO) {
        try {
            val ext = service().getStreamExtractor(url)
            ext.fetchPage()
            val audios = try { ext.audioStreams } catch (_: Exception) { null } ?: emptyList()
            val lowest = audios.sortedBy { a -> try { a.averageBitrate } catch (_: Exception) { Int.MAX_VALUE } }.firstOrNull()
            val audioUrl: String? = try { lowest?.content } catch (_: Exception) { null }
            // multi-language audio tracks (dubbed/original), kept with their locale + type
            val tracks = audios.mapNotNull { a ->
                try {
                    val u = a.content ?: return@mapNotNull null
                    val loc = try { a.audioLocale?.toLanguageTag() } catch (_: Exception) { null }
                    AudioTrackItem(u, try { a.averageBitrate } catch (_: Exception) { 0 }, try { a.audioTrackId } catch (_: Exception) { null }, try { a.audioTrackName } catch (_: Exception) { null }, loc, try { a.audioTrackType?.name } catch (_: Exception) { null })
                } catch (_: Exception) { null }
            }
            // caption/subtitle tracks (manual + auto-generated); default format is VTT
            val subs = try { ext.subtitlesDefault } catch (_: Exception) { null }.orEmpty().mapNotNull { s ->
                try {
                    val u = s.content ?: return@mapNotNull null
                    val ext = try { s.extension } catch (_: Exception) { "vtt" } ?: "vtt"
                    val mime = when (ext.lowercase()) {
                        "ttml" -> "application/ttml+xml"
                        "srt" -> "application/x-subrip"
                        else -> "text/vtt"
                    }
                    val tag = try { s.languageTag } catch (_: Exception) { null } ?: "und"
                    val label = try { s.displayLanguageName } catch (_: Exception) { null } ?: tag
                    SubtitleItem(u, tag, label, try { s.isAutoGenerated } catch (_: Exception) { false }, mime)
                } catch (_: Exception) { null }
            }
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
            var likeCount: Long? = null
            try { likeCount = ext.likeCount.takeIf { it >= 0 } } catch (_: Exception) {}
            var desc: String? = null
            try { desc = ext.description?.content?.take(2000) } catch (_: Exception) {}
            var upDate: String? = null
            try { upDate = ext.textualUploadDate } catch (_: Exception) {}
            var category: String? = null
            try { category = ext.category } catch (_: Exception) {}
            var tags: List<String> = emptyList()
            try { tags = ext.tags?.take(15) ?: emptyList() } catch (_: Exception) {}
            var uVerified = false
            try { uVerified = ext.isUploaderVerified } catch (_: Exception) {}
            var subCount: Long? = null
            try { subCount = ext.uploaderSubscriberCount.takeIf { it >= 0 } } catch (_: Exception) {}
            var uAvatar: String? = null
            try { uAvatar = ext.uploaderAvatars.firstOrNull()?.url } catch (_: Exception) {}
            var lang: String? = null
            try { lang = ext.languageInfo?.toLanguageTag() } catch (_: Exception) {}
            var chUrl: String? = null
            try { chUrl = ext.uploaderUrl } catch (_: Exception) {}
            var chapters: List<ChapterItem> = emptyList()
            try { chapters = ext.streamSegments?.mapNotNull { seg ->
                val t = try { seg.title } catch (_: Exception) { null }
                val st = try { seg.startTimeSeconds } catch (_: Exception) { -1 }
                if (t != null && st >= 0) ChapterItem(t, st) else null
            } ?: emptyList() } catch (_: Exception) {}
            var related: List<VideoItem> = emptyList()
            try { related = ext.relatedStreams?.getItems()?.mapNotNull { info ->
                if (true) {
                    val u: String? = try { info.url } catch (_: Exception) { null }
                    val nm: String? = try { decodeHtml(info.name) } catch (_: Exception) { null }
                    val ch: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { null }
                    var th: String? = null
                    try { th = info.thumbnails.firstOrNull()?.url } catch (_: Exception) {}
                    var d: Long? = null
                    try { d = info.duration } catch (_: Exception) {}
                    var vw: Long? = null
                    try { vw = info.viewCount } catch (_: Exception) {}
                    val rid: String? = u?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    if (u != null) VideoItem(rid, nm, ch, th, d, vw, u) else null
                } else null
            }?.take(20) ?: emptyList() } catch (_: Exception) {}
            StreamData(audioUrl, map, muxed, title, subs, tracks, likeCount, desc, upDate, category, tags, uVerified, subCount, uAvatar, lang, chUrl, chapters, related)
        } catch (_: Exception) { StreamData() }
    }

    suspend fun channelDetail(channelUrl: String?): ChannelDetail = withContext(Dispatchers.IO) {
        if (channelUrl.isNullOrBlank()) return@withContext ChannelDetail()
        try {
            val h = service().channelLHFactory.fromUrl(channelUrl)
            val ext = service().getChannelExtractor(h)
            ext.fetchPage()
            var av: String? = null
            try { av = ext.avatars.firstOrNull()?.url } catch (_: Exception) {}
            var bn: String? = null
            try { bn = ext.banners.firstOrNull()?.url } catch (_: Exception) {}
            var sc: Long? = null
            try { sc = ext.subscriberCount.takeIf { it >= 0 } } catch (_: Exception) {}
            var ds: String? = null
            try { ds = ext.description?.take(1000) } catch (_: Exception) {}
            var vf = false
            try { vf = ext.isVerified } catch (_: Exception) {}
            var nm: String? = null
            try { nm = ext.name } catch (_: Exception) {}
            ChannelDetail(nm, av, bn, sc, ds, vf)
        } catch (_: Exception) { ChannelDetail() }
    }

    suspend fun comments(videoUrl: String?): List<CommentItem> = withContext(Dispatchers.IO) {
        if (videoUrl.isNullOrBlank()) return@withContext emptyList()
        try {
            val h = service().commentsLHFactory.fromUrl(videoUrl)
            val ext = service().getCommentsExtractor(h)
            ext.fetchPage()
            val page = try { ext.initialPage } catch (_: Exception) { return@withContext emptyList() }
            page.items.mapNotNull { c ->
                try {
                    val tx = try { c.commentText?.content?.take(500) } catch (_: Exception) { null }
                    var av: String? = null
                    try { av = c.uploaderAvatars.firstOrNull()?.url } catch (_: Exception) {}
                    CommentItem(
                        try { c.uploaderName } catch (_: Exception) { null },
                        tx,
                        try { c.likeCount } catch (_: Exception) { 0 },
                        av,
                        try { c.textualUploadDate } catch (_: Exception) { null }
                    )
                } catch (_: Exception) { null }
            }.take(30)
        } catch (_: Exception) { emptyList() }
    }
}
