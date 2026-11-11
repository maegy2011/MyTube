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

data class VideoItem(val id: String? = null, val title: String? = null, val channel: String? = null, val thumbnail: String? = null, val durationSec: Long? = null, val viewCount: Long? = null, val url: String? = null, val channelAvatar: String? = null, val verified: Boolean = false, val uploadDate: String? = null, val isShort: Boolean = false, val channelUrl: String? = null, val ageLimit: Int = 0, val isChannel: Boolean = false)
data class SubtitleItem(val url: String, val langTag: String, val label: String, val auto: Boolean, val mime: String)
data class AudioTrackItem(val url: String, val bitrate: Int, val trackId: String?, val name: String?, val localeTag: String?, val type: String?)
data class ChapterItem(val title: String, val startSec: Int)
data class Storyboard(val urls: List<String>, val total: Int, val perX: Int, val perY: Int, val fw: Int, val fh: Int, val durPerFrame: Int)
data class ChannelDetail(val name: String? = null, val avatar: String? = null, val banner: String? = null, val subs: Long? = null, val description: String? = null, val verified: Boolean = false)
data class CommentItem(val author: String? = null, val text: String? = null, val likes: Int = 0, val avatar: String? = null, val date: String? = null)
data class StreamData(val audioLowestUrl: String? = null, val videoUrlByQuality: Map<String, String> = emptyMap(), val muxedQualities: Set<String> = emptySet(), val title: String? = null, val subtitles: List<SubtitleItem> = emptyList(), val audioTracks: List<AudioTrackItem> = emptyList(), val likeCount: Long? = null, val description: String? = null, val uploadDate: String? = null, val category: String? = null, val tags: List<String> = emptyList(), val uploaderVerified: Boolean = false, val subscriberCount: Long? = null, val uploaderAvatar: String? = null, val language: String? = null, val channelUrl: String? = null, val chapters: List<ChapterItem> = emptyList(), val related: List<VideoItem> = emptyList(), val storyboards: List<Storyboard> = emptyList(), val ageLimit: Int = 0, val isLive: Boolean = false, val audioHlsOnly: Boolean = false, val videoMime: String? = null, val audioMime: String? = null)
data class RemotePlaylist(
    val title: String? = null,
    val uploader: String? = null,
    val thumbnail: String? = null,
    val count: Long = 0,
    val url: String? = null,
    val source: String = "youtube" // youtube | soundcloud
)

data class SearchPage(val items: List<VideoItem> = emptyList(), val nextPage: Page? = null, val hasNext: Boolean = false, val error: String? = null, val suggestion: String? = null, val corrected: Boolean = false)

object YouTubeRepo {
    // H2: bare OkHttpClient hung IO threads forever on stalled connections (pool exhaustion).
    // Same pattern as PinManager: fail fast, surface the error.
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .callTimeout(2, java.util.concurrent.TimeUnit.MINUTES)
            .build()
    }
    private fun decodeHtml(s: String): String {
        return try { Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY).toString() } catch (_: Exception) { s }
    }
    private fun service() = NewPipe.getService("YouTube")
    // SoundCloud by name; index 1 is the documented fallback (YouTube=0, SoundCloud=1, media.ccc.de=2, PeerTube=3, Bandcamp=4).
    private fun scService() = try { NewPipe.getService("SoundCloud") } catch (_: Exception) { NewPipe.getService(1) }
    init { runCatching { NewPipe.init(NewPipeDownloaderImpl()) } }

    // Real per-country kiosks (verified against NewPipeExtractor 0.26.5 YoutubeService):
    // default Trending + trending_music + trending_gaming + trending_movies_and_shows
    // + trending_podcasts_episodes + live. "news" has NO kiosk — served as region search, labeled honestly.
    // Any failing kiosk returns empty (caller shows the fallback, never a fake list).
    // Deferred item 1 (2026-11-11): real kiosk pagination. KioskExtractor IS a ListExtractor
    // (verified in NewPipeExtractor 0.27.1: getPage(Page)/getInitialPage + InfoItemsPage.nextPage),
    // so "show more" pages the same kiosk — the old "extractor lacks nextPage" note was wrong.
    // YouTube kiosks often return everything on page 1 (nextPage null) — then hasMore is false
    // and the UI hides the button honestly instead of faking more content.
    private var kioskKey: String? = null
    private var kioskNext: Page? = null
    private var kioskHasMore: Boolean = false
    private fun mapKioskItems(items: List<org.schabi.newpipe.extractor.InfoItem>): List<VideoItem> {
        val out = mutableListOf<VideoItem>()
        for (info in items) {
            if (info is StreamInfoItem) {
                val url: String? = try { info.url } catch (_: Exception) { null }
                val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { info.name }
                val up: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { info.uploaderName }
                var thumb: String? = null
                try { thumb = bestThumb(info.thumbnails) } catch (_: Exception) {}
                var dur: Long? = null
                try { dur = info.duration } catch (_: Exception) {}
                var views: Long? = null
                try { views = info.viewCount } catch (_: Exception) {}
                var avatar: String? = null
                try { avatar = bestThumb(info.uploaderAvatars) } catch (_: Exception) {}
                var verified = false
                try { verified = info.isUploaderVerified } catch (_: Exception) {}
                var upDate: String? = null
                try { upDate = info.textualUploadDate } catch (_: Exception) {}
                var isShort = false
                try { isShort = info.isShortFormContent } catch (_: Exception) {}
                val id: String? = url?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                var chUrl: String? = null
                try { chUrl = info.uploaderUrl } catch (_: Exception) {}
                out.add(VideoItem(id, name, up, thumb, dur, views, url, avatar, verified, upDate, isShort, chUrl))
            }
        }
        return out
    }
    private fun kioskExtractorFor(kioskId: String?): org.schabi.newpipe.extractor.kiosk.KioskExtractor<*>? {
        return try {
            val svc = service()
            if (kioskId.isNullOrBlank()) svc.kioskList.getDefaultKioskExtractor(null)
            else svc.kioskList.getExtractorById(kioskId, null)
        } catch (_: Exception) { null }
    }
    suspend fun trending(region: String? = null, kioskId: String? = null): List<VideoItem> = withContext(Dispatchers.IO) {
        val key = (region ?: "") + "|" + (kioskId ?: "")
        kioskKey = key
        kioskNext = null
        kioskHasMore = false
        try {
            applyRegion(region)
            val kiosk = kioskExtractorFor(kioskId) ?: return@withContext emptyList()
            kiosk.fetchPage()
            val lp = try { kiosk.initialPage } catch (_: Exception) { return@withContext emptyList() }
            val out = mapKioskItems(lp.items)
            try {
                kioskHasMore = lp.hasNextPage()
                kioskNext = if (kioskHasMore) try { lp.nextPage } catch (_: Exception) { null } else null
                if (kioskNext == null) kioskHasMore = false
            } catch (_: Exception) { kioskHasMore = false; kioskNext = null }
            out
        } catch (_: Exception) { emptyList() }
    }
    // Next kiosk page for the last trending() call (key-guarded: a tab switch invalidates it).
    // Returns (items, hasMoreAfterThis).
    suspend fun trendingMore(region: String? = null, kioskId: String? = null): Pair<List<VideoItem>, Boolean> = withContext(Dispatchers.IO) {
        try {
            val key = (region ?: "") + "|" + (kioskId ?: "")
            val pg = kioskNext
            if (key != kioskKey || pg == null) return@withContext emptyList<VideoItem>() to false
            applyRegion(region)
            val kiosk = kioskExtractorFor(kioskId) ?: return@withContext emptyList<VideoItem>() to false
            val lp = try { kiosk.getPage(pg) } catch (_: Exception) { return@withContext emptyList<VideoItem>() to false }
            val out = mapKioskItems(lp.items)
            try {
                kioskHasMore = lp.hasNextPage()
                kioskNext = if (kioskHasMore) try { lp.nextPage } catch (_: Exception) { null } else null
                if (kioskNext == null) kioskHasMore = false
            } catch (_: Exception) { kioskHasMore = false; kioskNext = null }
            out to kioskHasMore
        } catch (_: Exception) { emptyList<VideoItem>() to false }
    }

    // Display upscale: extractors hand us the smallest variant (YTM InnerTube thumbs are
    // often w60-h60 and YT default.jpg is 120px — blurry on high-density screens). Both CDNs
    // serve bigger renders of the same path, so rewrite the size token instead of refetching.
    // Unknown patterns pass through untouched.
    fun upgradeThumb(url: String?): String? {
        if (url.isNullOrBlank()) return url
        return try {
            var u = url
            if (u.contains("sndcdn.com") && u.contains("-large.")) u = u.replace("-large.", "-t500x500.")
            if (u.contains("googleusercontent.com") && ("=w" in u || "-w" in u)) {
                u = u.replace(Regex("=w\\d+-h\\d+"), "=w540-h540")
                u = u.replace(Regex("-w\\d+-h\\d+"), "-w540-h540")
            }
            if (u.contains("i.ytimg.com") && u.endsWith("/default.jpg")) u = u.replace("/default.jpg", "/hqdefault.jpg")
            if (u.contains("i.ytimg.com") && u.endsWith("/mqdefault.jpg")) u = u.replace("/mqdefault.jpg", "/hqdefault.jpg")
            u
        } catch (_: Exception) { url }
    }
    // Thumbnails: NewPipe lists smallest-first — firstOrNull() gave blurry cards.
    // Pick max known area; unknown sizes (-1) fall back to the last entry (usually largest).
    fun bestThumb(list: List<org.schabi.newpipe.extractor.Image>): String? {
        return try {
            if (list.isEmpty()) return null
            val best = list.maxByOrNull { it.height.coerceAtLeast(0).toLong() * it.width.coerceAtLeast(0).toLong() }
            val url = best?.url
            val area = (best?.height ?: -1).coerceAtLeast(0).toLong() * (best?.width ?: -1).coerceAtLeast(0).toLong()
            upgradeThumb((if (area > 0) url else null) ?: list.lastOrNull()?.url)
        } catch (_: Exception) { try { upgradeThumb(list.lastOrNull()?.url) } catch (_: Exception) { null } }
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
                .header("Accept-Language", if (contentLang == "en") "en,ar;q=0.9" else "ar,en;q=0.9")
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
    // "global" = neutral YouTube default (resets any previous country — never leave stale).
    // Language follows the UI lang (contentLang, synced from MainActivity), never forced.
    var contentLang: String = "ar"
    fun applyRegion(country: String?, lang: String? = null) {
        runCatching {
            val l = (lang ?: contentLang).takeIf { it == "ar" || it == "en" } ?: "en"
            if (country.isNullOrBlank() || country == "global")
                NewPipe.setupLocalization(Localization(l, "US"))
            else
                NewPipe.setupLocalization(Localization(l, country))
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
                if (info is org.schabi.newpipe.extractor.playlist.PlaylistInfoItem) {
                    // playlists surface separately via searchPlaylists(); skipped in the video list
                    continue
                }
                if (info is org.schabi.newpipe.extractor.channel.ChannelInfoItem) {
                    // artists/channels tab: row opens the channel screen (isChannel routes the tap)
                    val url: String? = try { info.url } catch (_: Exception) { null }
                    if (url.isNullOrBlank()) continue
                    val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { info.name }
                    var thumb: String? = null
                    try { thumb = bestThumb(info.thumbnails) } catch (_: Exception) {}
                    var subs: Long? = null
                    try { subs = info.subscriberCount.takeIf { it >= 0 } } catch (_: Exception) {}
                    var verified = false
                    try { verified = info.isVerified } catch (_: Exception) { }
                    var desc: String? = null
                    try { desc = info.description?.take(140) } catch (_: Exception) { }
                    out.add(VideoItem(id = null, title = name, channel = desc, thumbnail = thumb, durationSec = null, viewCount = subs, url = url, channelAvatar = thumb, verified = verified, uploadDate = null, isShort = false, channelUrl = url, ageLimit = 0, isChannel = true))
                    continue
                }
                if (info is StreamInfoItem) {
                    val url: String? = try { info.url } catch (_: Exception) { null }
                    val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { info.name }
                    val up: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { info.uploaderName }
                    var thumb: String? = null
                    try { thumb = bestThumb(info.thumbnails) } catch (_: Exception) {}
                    var dur: Long? = null
                    try { dur = info.duration } catch (_: Exception) {}
                    var views: Long? = null
                    try { views = info.viewCount } catch (_: Exception) {}
                    var avatar: String? = null
                    try { avatar = bestThumb(info.uploaderAvatars) } catch (_: Exception) {}
                    var verified = false
                    try { verified = info.isUploaderVerified } catch (_: Exception) {}
                    var upDate: String? = null
                    try { upDate = info.textualUploadDate } catch (_: Exception) {}
                    var isShort = false
                    try { isShort = info.isShortFormContent } catch (_: Exception) {}
                    val id: String? = url?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    var chUrl: String? = null
                    try { chUrl = info.uploaderUrl } catch (_: Exception) {}
                    out.add(VideoItem(id, name, up, thumb, dur, views, url, avatar, verified, upDate, isShort, chUrl))
                }
            }
            val hasNext = try { lp.hasNextPage() } catch (_: Exception) { false }
            val next = if (hasNext) try { lp.nextPage } catch (_: Exception) { null } else null
            // search correction (own try: must never fail the search itself)
            var sugg: String? = null
            var corr = false
            try { sugg = ext.searchSuggestion?.takeIf { it.isNotBlank() } } catch (_: Exception) { }
            try { corr = ext.isCorrectedSearch } catch (_: Exception) { }
            // hide self-echo suggestion (same as query = no correction)
            if (sugg != null && sugg.equals(query, ignoreCase = true)) sugg = null
            SearchPage(out, next, hasNext, null, sugg, corr)
        } catch (e: Exception) { SearchPage(error = e.message ?: e.javaClass.simpleName) }
    }

    // pin-deepfix 2026-11-11: YTM tracks (music.youtube.com/watch, youtu.be/…) must resolve
    // through the www host — the YouTube extractor does not accept the music host
    // (fetchPlaylist already normalized; this covers direct pin/play of YTM items).
    fun normalizeStreamUrl(url: String): String {
        return try {
            var u = url.replace("music.youtube.com", "www.youtube.com")
            val short = Regex("^https?://youtu\\.be/([^?#/]+)").find(u)
            if (short != null) u = "https://www.youtube.com/watch?v=" + short.groupValues[1]
            u
        } catch (_: Exception) { url }
    }
    suspend fun stream(url: String): StreamData = withContext(Dispatchers.IO) {
        try {
            val ext = service().getStreamExtractor(normalizeStreamUrl(url))
            ext.fetchPage()
            // Livestreams: no progressive/DASH-video map — single HLS (or DASH manifest) URL.
            // ExoPlayer sniffs HLS/DASH automatically once the hls/dash modules are on classpath.
            try {
                if (ext.streamType == org.schabi.newpipe.extractor.stream.StreamType.LIVE_STREAM) {
                    val liveUrl: String? = try { ext.hlsUrl } catch (_: Exception) { null }
                        ?: try { ext.dashMpdUrl } catch (_: Exception) { null }
                    if (!liveUrl.isNullOrBlank()) {
                        return@withContext StreamData(
                            videoUrlByQuality = mapOf("live" to liveUrl),
                            muxedQualities = setOf("live"),
                            title = try { ext.name } catch (_: Exception) { null },
                            channelUrl = try { ext.uploaderUrl } catch (_: Exception) { null },
                            isLive = true,
                            // age gate must cover live too (was reset to 0 — an actual protection hole)
                            ageLimit = try { ext.ageLimit } catch (_: Exception) { 0 }
                        )
                    }
                }
            } catch (_: Exception) { }
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
            try { uAvatar = bestThumb(ext.uploaderAvatars) } catch (_: Exception) {}
            var lang: String? = null
            try { lang = ext.languageInfo?.toLanguageTag() } catch (_: Exception) {}
            var chUrl: String? = null
            try { chUrl = ext.uploaderUrl } catch (_: Exception) {}
            var age = 0
            try { age = ext.ageLimit } catch (_: Exception) {}
            var chapters: List<ChapterItem> = emptyList()
            try { chapters = ext.streamSegments?.mapNotNull { seg ->
                val t = try { seg.title } catch (_: Exception) { null }
                val st = try { seg.startTimeSeconds } catch (_: Exception) { -1 }
                if (t != null && st >= 0) ChapterItem(t, st) else null
            } ?: emptyList() } catch (_: Exception) {}
            var related: List<VideoItem> = emptyList()
            try { related = ext.relatedStreams?.getItems()?.mapNotNull { info ->
                run {
                    val u: String? = try { info.url } catch (_: Exception) { null }
                    val nm: String? = try { decodeHtml(info.name) } catch (_: Exception) { null }
                    val ch: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { null }
                    var th: String? = null
                    try { th = bestThumb(info.thumbnails) } catch (_: Exception) {}
                    var d: Long? = null
                    try { d = info.duration } catch (_: Exception) {}
                    var vw: Long? = null
                    try { vw = info.viewCount } catch (_: Exception) {}
                    val rid: String? = u?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    var rChUrl: String? = null
                    try { rChUrl = info.uploaderUrl } catch (_: Exception) {}
                    if (u != null) VideoItem(rid, nm, ch, th, d, vw, u, channelUrl = rChUrl) else null
                }
            }?.take(20) ?: emptyList() } catch (_: Exception) {}
            var boards: List<Storyboard> = emptyList()
            try { boards = ext.frames?.mapNotNull { f ->
                try { Storyboard(f.urls, f.totalCount, f.framesPerPageX, f.framesPerPageY, f.frameWidth, f.frameHeight, f.durationPerFrame) } catch (_: Exception) { null }
            } ?: emptyList() } catch (_: Exception) {}
            StreamData(audioUrl, map, muxed, title, subs, tracks, likeCount, desc, upDate, category, tags, uVerified, subCount, uAvatar, lang, chUrl, chapters, related, boards, ageLimit = age)
        } catch (_: Exception) { StreamData() }
    }

    // A2 subscriptions: import OPML/JSON/CSV/ZIP (YouTube Takeout) via the extractor's own dispatch.
    // Returns (url, name) pairs; caller persists them (dedupe by URL).
    suspend fun importSubscriptions(input: java.io.InputStream): List<Pair<String, String?>> = withContext(Dispatchers.IO) {
        try {
            service().subscriptionExtractor.fromInputStream(input).mapNotNull { item ->
                val u = try { item.url } catch (_: Exception) { null }
                if (u.isNullOrBlank()) null else Pair(u, try { item.name } catch (_: Exception) { null })
            }
        } catch (_: Exception) { emptyList() }
    }

    // A3 feed: latest videos of one channel. Param is the CHANNEL ID (not URL) —
    // resolve via channelLHFactory.fromUrl(url).getId() first. Per-channel try at call site.
    suspend fun feedFor(channelId: String?): List<VideoItem> = withContext(Dispatchers.IO) {
        if (channelId.isNullOrBlank()) return@withContext emptyList()
        try {
            val ext = service().getFeedExtractor(channelId) ?: return@withContext emptyList()
            ext.fetchPage()
            val lp = try { ext.initialPage } catch (_: Exception) { return@withContext emptyList() }
            lp.items.mapNotNull { info ->
                try {
                    val u: String? = try { info.url } catch (_: Exception) { null }
                    if (u == null) return@mapNotNull null
                    val nm: String? = try { decodeHtml(info.name) } catch (_: Exception) { null }
                    val ch: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { null }
                    var th: String? = null
                    try { th = bestThumb(info.thumbnails) } catch (_: Exception) {}
                    var d: Long? = null
                    try { d = info.duration } catch (_: Exception) {}
                    var vw: Long? = null
                    try { vw = info.viewCount } catch (_: Exception) {}
                    val rid: String? = u.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    var fChUrl: String? = null
                    try { fChUrl = info.uploaderUrl } catch (_: Exception) {}
                    VideoItem(rid, nm, ch, th, d, vw, u, channelUrl = fChUrl)
                } catch (_: Exception) { null }
            }.take(15)
        } catch (_: Exception) { emptyList() }
    }

    // Resolve a channel URL to its stable channel ID (feed key). Null on failure — caller skips.
    // Robust channel-id resolve: LH parse is enough for /channel/UC… links, but
    // /c/, /user/ and /@handle links need a real extractor fetch to yield the UC id.
    suspend fun channelIdFor(channelUrl: String?): String? = withContext(Dispatchers.IO) {
        if (channelUrl.isNullOrBlank()) return@withContext null
        try {
            val lhId = try { service().channelLHFactory.fromUrl(channelUrl).id } catch (_: Exception) { null }
            if (!lhId.isNullOrBlank() && lhId.startsWith("UC")) return@withContext lhId
            val ext = service().getChannelExtractor(service().channelLHFactory.fromUrl(channelUrl))
            ext.fetchPage()
            try { ext.id?.takeIf { it.isNotBlank() } } catch (_: Exception) { lhId }
        } catch (_: Exception) { null }
    }

    // Channel videos tab (javap-verified 0.26.5: getTabs/getChannelTabExtractor/getContentFilters).
    // Picks the "videos" tab, else the first tab; pageless first ~20 items, zero account.
    suspend fun channelVideos(channelUrl: String?, limit: Int = 20): List<VideoItem> = withContext(Dispatchers.IO) {
        if (channelUrl.isNullOrBlank()) return@withContext emptyList()
        try {
            val h = service().channelLHFactory.fromUrl(channelUrl)
            val ext = service().getChannelExtractor(h)
            ext.fetchPage()
            val tabs = try { ext.tabs } catch (_: Exception) { emptyList() }
            if (tabs.isEmpty()) return@withContext emptyList()
            val videosTab = tabs.firstOrNull { t ->
                try { t.contentFilters.any { f -> f.contains("video", ignoreCase = true) } } catch (_: Exception) { false }
            } ?: tabs.first()
            val tabExt = service().getChannelTabExtractor(videosTab)
            tabExt.fetchPage()
            val page = try { tabExt.initialPage } catch (_: Exception) { null } ?: return@withContext emptyList()
            page.items.filterIsInstance<org.schabi.newpipe.extractor.stream.StreamInfoItem>().take(limit).mapNotNull { info ->
                try {
                    val url: String? = try { info.url } catch (_: Exception) { null }
                    val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { null }
                    var thumb: String? = null
                    try { thumb = bestThumb(info.thumbnails) } catch (_: Exception) {}
                    var dur: Long? = null
                    try { dur = info.duration } catch (_: Exception) {}
                    var views: Long? = null
                    try { views = info.viewCount } catch (_: Exception) {}
                    var verified = false
                    try { verified = info.isUploaderVerified } catch (_: Exception) {}
                    var upDate: String? = null
                    try { upDate = info.textualUploadDate } catch (_: Exception) {}
                    var isShort = false
                    try { isShort = info.isShortFormContent } catch (_: Exception) {}
                    val id: String? = url?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    val chName: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { null }
                    var chUrl: String? = null
                    try { chUrl = info.uploaderUrl } catch (_: Exception) {}
                    VideoItem(id, name, chName, thumb, dur, views, url, null, verified, upDate, isShort, chUrl ?: channelUrl)
                } catch (_: Exception) { null }
            }
        } catch (_: Exception) { emptyList() }
    }

    // Channel tabs beyond Videos: kinds match tab contentFilters ("short" | "live"/"stream" | "playlist").
    // Shorts/Live return VideoItems; Playlists need PlaylistInfoItem — use channelPlaylists() for those.
    suspend fun channelTab(channelUrl: String?, kinds: List<String>, limit: Int = 20): List<VideoItem> = withContext(Dispatchers.IO) {
        if (channelUrl.isNullOrBlank()) return@withContext emptyList()
        try {
            val h = service().channelLHFactory.fromUrl(channelUrl)
            val ext = service().getChannelExtractor(h)
            ext.fetchPage()
            val tabs = try { ext.tabs } catch (_: Exception) { emptyList() }
            if (tabs.isEmpty()) return@withContext emptyList()
            val want = tabs.firstOrNull { t ->
                try { t.contentFilters.any { f -> kinds.any { k -> f.contains(k, ignoreCase = true) } } } catch (_: Exception) { false }
            } ?: return@withContext emptyList()
            val tabExt = service().getChannelTabExtractor(want)
            tabExt.fetchPage()
            val page = try { tabExt.initialPage } catch (_: Exception) { null } ?: return@withContext emptyList()
            page.items.filterIsInstance<org.schabi.newpipe.extractor.stream.StreamInfoItem>().take(limit).mapNotNull { info ->
                try {
                    val u: String? = try { info.url } catch (_: Exception) { null }
                    val nm: String? = try { decodeHtml(info.name) } catch (_: Exception) { null }
                    var th: String? = null
                    try { th = bestThumb(info.thumbnails) } catch (_: Exception) {}
                    var d: Long? = null
                    try { d = info.duration } catch (_: Exception) {}
                    var vw: Long? = null
                    try { vw = info.viewCount } catch (_: Exception) {}
                    var upDate: String? = null
                    try { upDate = info.textualUploadDate } catch (_: Exception) {}
                    val id: String? = u?.substringAfter("v=", "")?.substringBefore("&")?.takeIf { it.isNotEmpty() }
                    var chUrl: String? = null
                    try { chUrl = info.uploaderUrl } catch (_: Exception) {}
                    if (u != null) VideoItem(id, nm, null, th, d, vw, u, null, false, upDate, false, chUrl ?: channelUrl) else null
                } catch (_: Exception) { null }
            }
        } catch (_: Exception) { emptyList() }
    }

    // A channel's Playlists tab as openable remote-playlist cards (source youtube).
    suspend fun channelPlaylists(channelUrl: String?): List<RemotePlaylist> = withContext(Dispatchers.IO) {
        if (channelUrl.isNullOrBlank()) return@withContext emptyList()
        try {
            val h = service().channelLHFactory.fromUrl(channelUrl)
            val ext = service().getChannelExtractor(h)
            ext.fetchPage()
            val tabs = try { ext.tabs } catch (_: Exception) { emptyList() }
            if (tabs.isEmpty()) return@withContext emptyList()
            val want = tabs.firstOrNull { t ->
                try { t.contentFilters.any { f -> f.contains("playlist", ignoreCase = true) } } catch (_: Exception) { false }
            } ?: return@withContext emptyList()
            val tabExt = service().getChannelTabExtractor(want)
            tabExt.fetchPage()
            val page = try { tabExt.initialPage } catch (_: Exception) { null } ?: return@withContext emptyList()
            page.items.filterIsInstance<org.schabi.newpipe.extractor.playlist.PlaylistInfoItem>()
                .mapNotNull { p -> playlistInfoToRemote(p, "youtube") }
        } catch (_: Exception) { emptyList() }
    }

    suspend fun channelDetail(channelUrl: String?): ChannelDetail = withContext(Dispatchers.IO) {
        if (channelUrl.isNullOrBlank()) return@withContext ChannelDetail()
        try {
            val h = service().channelLHFactory.fromUrl(channelUrl)
            val ext = service().getChannelExtractor(h)
            ext.fetchPage()
            var av: String? = null
            try { av = bestThumb(ext.avatars) } catch (_: Exception) {}
            var bn: String? = null
            try { bn = bestThumb(ext.banners) } catch (_: Exception) {}
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

    // CommentPager: stateful page-walking over CommentsExtractor (first + getPage chain).
    // Keeps the extractor + next Page between calls; survives rotation? No — reset per open.
    // Low (doc): single-flight by design — one comments sheet at a time. Not safe for concurrent first() calls.
    object CommentPager {
        private var ext: org.schabi.newpipe.extractor.comments.CommentsExtractor? = null
        private var next: org.schabi.newpipe.extractor.Page? = null
        var hasMore: Boolean = false
            private set
        private fun map(items: List<org.schabi.newpipe.extractor.InfoItem>): List<CommentItem> {
            return items.filterIsInstance<org.schabi.newpipe.extractor.comments.CommentsInfoItem>().mapNotNull { c ->
                try {
                    val tx = try { c.commentText?.content?.take(500) } catch (_: Exception) { null }
                    var av: String? = null
                    try { av = bestThumb(c.uploaderAvatars) } catch (_: Exception) {}
                    CommentItem(
                        try { c.uploaderName } catch (_: Exception) { null },
                        tx,
                        try { c.likeCount } catch (_: Exception) { 0 },
                        av,
                        try { c.textualUploadDate } catch (_: Exception) { null }
                    )
                } catch (_: Exception) { null }
            }
        }
        suspend fun first(videoUrl: String?): List<CommentItem> = withContext(Dispatchers.IO) {
            ext = null; next = null; hasMore = false
            if (videoUrl.isNullOrBlank()) return@withContext emptyList()
            try {
                val h = service().commentsLHFactory.fromUrl(videoUrl)
                val e = service().getCommentsExtractor(h)
                e.fetchPage()
                ext = e
                val page = try { e.initialPage } catch (_: Exception) { return@withContext emptyList() }
                next = try { page.nextPage } catch (_: Exception) { null }
                hasMore = next != null && (try { !page.items.isNullOrEmpty() } catch (_: Exception) { false })
                map(page.items).take(30)
            } catch (_: Exception) { emptyList() }
        }
        suspend fun more(): List<CommentItem> = withContext(Dispatchers.IO) {
            val e = ext ?: return@withContext emptyList()
            val p = next ?: run { hasMore = false; return@withContext emptyList() }
            try {
                val page = e.getPage(p)
                next = try { page.nextPage } catch (_: Exception) { null }
                val items = map(page.items)
                hasMore = next != null && items.isNotEmpty()
                if (!hasMore) { ext = null; next = null }
                items
            } catch (_: Exception) { hasMore = false; emptyList() }
        }
    }

    // Title localization: video title rendered in the video's FIRST subtitle language.
    // Uses the free Google Translate endpoint (no key); auto-detects source and returns
    // the original when it already matches the target. In-memory cache, silent on failure.
    // M3: synchronized LRU (cap 200) — check-then-act was racy and eviction wiped everything.
    private val titleCache: MutableMap<String, String> = java.util.Collections.synchronizedMap(
        object : LinkedHashMap<String, String>(64, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean = size > 200
        }
    )
    suspend fun localizedTitle(title: String?, targetLang: String?): String? = withContext(Dispatchers.IO) {
        val t = targetLang?.substringBefore("-")?.substringBefore("_")?.lowercase()?.takeIf { it.length in 2..3 }
        if (title.isNullOrBlank() || t.isNullOrBlank()) return@withContext null
        val key = "$t|$title"
        titleCache[key]?.let { return@withContext it }
        try {
            val q = java.net.URLEncoder.encode(title.take(500), "UTF-8")
            val req = okhttp3.Request.Builder()
                .url("https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=$t&dt=t&q=$q")
                .header("User-Agent", "Mozilla/5.0").build()
            val body = client.newCall(req).execute().use { it.body?.string() } ?: return@withContext null
            val arr = org.json.JSONArray(body)
            val segs = arr.optJSONArray(0) ?: return@withContext null
            val sb = StringBuilder()
            for (i in 0 until segs.length()) sb.append(segs.optJSONArray(i)?.optString(0) ?: "")
            val out = sb.toString().takeIf { it.isNotBlank() } ?: return@withContext null
            val detected = arr.optString(2, "").lowercase()
            val final = if (detected == t || detected.startsWith("$t-")) title else out
            if (titleCache.size > 200) {
                // evict ~a quarter (oldest insertion order) instead of nuking the whole cache
                val drop = titleCache.keys.take(50)
                drop.forEach { titleCache.remove(it) }
            }
            titleCache[key] = final
            final
        } catch (_: Exception) { null }
    }

    suspend fun comments(videoUrl: String?): List<CommentItem> = CommentPager.first(videoUrl)

    // SoundCloud (separate tab): search + audio-stream resolve. Best-effort; failures = empty/error.
    // SC items carry soundcloud.com URLs with null ids (favKey falls back to url).
    suspend fun scSearch(query: String, page: Page? = null): SearchPage = withContext(Dispatchers.IO) {
        try {
            // fresh extractor per call: extractors are single-use cursors, getPage needs its own
            val ext = scService().getSearchExtractor(query)
            val lp = if (page == null) {
                try { ext.fetchPage() } catch (_: Exception) { }
                try { ext.initialPage } catch (_: Exception) { null }
            } else {
                try { ext.getPage(page) } catch (_: Exception) { null }
            } ?: return@withContext SearchPage(emptyList(), null, false, "empty", null, false)
            val out = mutableListOf<VideoItem>()
            for (info in lp.items) {
                if (info is org.schabi.newpipe.extractor.stream.StreamInfoItem) {
                    val url: String? = try { info.url } catch (_: Exception) { null }
                    val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { info.name }
                    val up: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { info.uploaderName }
                    var thumb: String? = null
                    try { thumb = bestThumb(info.thumbnails) } catch (_: Exception) {}
                    var dur: Long? = null
                    try { dur = info.duration } catch (_: Exception) {}
                    out.add(VideoItem(null, name, up, thumb, dur, null, url, null, false, null, false, null))
                } else if (info is org.schabi.newpipe.extractor.channel.ChannelInfoItem) {
                    val url: String? = try { info.url } catch (_: Exception) { null }
                    if (url.isNullOrBlank()) continue
                    val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { info.name }
                    var thumb: String? = null
                    try { thumb = bestThumb(info.thumbnails) } catch (_: Exception) {}
                    out.add(VideoItem(null, name, null, thumb, null, null, url, thumb, false, null, false, url, 0, true))
                }
            }
            val hasNext = try { lp.hasNextPage() } catch (_: Exception) { false }
            val next = if (hasNext) try { lp.nextPage } catch (_: Exception) { null } else null
            SearchPage(out, next, hasNext, null, null, false)
        } catch (e: Exception) {
            SearchPage(emptyList(), null, false, e.message, null, false)
        }
    }

    // Audio-only StreamData for a SoundCloud track (highest-bitrate audio stream).
    // pin-deepfix 2026-11-11: skip HLS (.m3u8) variants — pinning stores raw bytes, so a
    // playlist URL would produce a corrupt audio.m4a. audioHlsOnly lets callers say so clearly.
    suspend fun scStream(url: String): StreamData? = withContext(Dispatchers.IO) {
        try {
            val ext = scService().getStreamExtractor(url)
            ext.fetchPage()
            val audios = try { ext.audioStreams } catch (_: Exception) { null } ?: emptyList()
            val withUrl = audios.mapNotNull { a -> try { a.content?.let { a to it } } catch (_: Exception) { null } }
            if (withUrl.isEmpty()) return@withContext null
            val progressive = withUrl.filter { (_, u) -> !u.contains("m3u8", ignoreCase = true) }
            if (progressive.isEmpty()) return@withContext StreamData(audioHlsOnly = true)
            val best = progressive.sortedByDescending { (a, _) -> try { a.averageBitrate } catch (_: Exception) { 0 } }.firstOrNull()
            val audioUrl: String? = best?.second
            if (audioUrl.isNullOrBlank()) return@withContext null
            StreamData(
                audioLowestUrl = audioUrl,
                videoUrlByQuality = emptyMap(),
                muxedQualities = emptySet(),
                title = try { ext.name } catch (_: Exception) { null },
                subtitles = emptyList()
            )
        } catch (_: Exception) { null }
    }

    // Router: SoundCloud URLs resolve via SC, everything else via YouTube. Call sites use this.
    suspend fun streamRouted(url: String): StreamData? {
        return try {
            if (url.contains("soundcloud.com")) scStream(url) else stream(url)
        } catch (_: Exception) { null }
    }

    // Remote playlists: search + open, per the browsed service (YouTube / YouTube Music / SoundCloud).
    // SearchPage.video list skips PlaylistInfoItem; this surfaces them as openable cards.
    suspend fun searchPlaylists(query: String, region: String? = null, music: Boolean = false, sc: Boolean = false, albums: Boolean = false): List<RemotePlaylist> = withContext(Dispatchers.IO) {
        try {
            if (sc) {
                val ext = scService().getSearchExtractor(query)
                ext.fetchPage()
                return@withContext ext.initialPage.items
                    .filterIsInstance<org.schabi.newpipe.extractor.playlist.PlaylistInfoItem>()
                    .mapNotNull { p -> playlistInfoToRemote(p, "soundcloud") }
            }
            applyRegion(region)
            val filter = if (albums) "music_albums" else if (music) "music_playlists" else "playlists"
            val ext = try {
                val qh = service().searchQHFactory.fromQuery(query, listOf(filter), "")
                service().getSearchExtractor(qh)
            } catch (_: Exception) { service().getSearchExtractor(query) }
            ext.fetchPage()
            ext.initialPage.items
                .filterIsInstance<org.schabi.newpipe.extractor.playlist.PlaylistInfoItem>()
                .mapNotNull { p -> playlistInfoToRemote(p, "youtube") }
        } catch (_: Exception) { emptyList() }
    }

    private fun playlistInfoToRemote(p: org.schabi.newpipe.extractor.playlist.PlaylistInfoItem, source: String): RemotePlaylist? {
        return try {
            val url: String? = try { p.url } catch (_: Exception) { null }
            if (url.isNullOrBlank()) return null
            val name: String? = try { decodeHtml(p.name) } catch (_: Exception) { p.name }
            val up: String? = try { decodeHtml(p.uploaderName) } catch (_: Exception) { p.uploaderName }
            var thumb: String? = null
            try { thumb = bestThumb(p.thumbnails) } catch (_: Exception) { }
            var cnt = 0L
            try { cnt = p.streamCount } catch (_: Exception) { }
            RemotePlaylist(name, up, thumb, cnt, url, source)
        } catch (_: Exception) { null }
    }

    // Open a remote playlist URL (youtube.com / music.youtube.com / soundcloud.com) into playable items.
    suspend fun fetchPlaylist(url: String): List<VideoItem> = withContext(Dispatchers.IO) {
        try {
            // YTM album links (music.youtube.com) resolve through the www host
            val norm = url.replace("music.youtube.com", "www.youtube.com")
            val sc = norm.contains("soundcloud.com")
            val ext = if (sc) scService().getPlaylistExtractor(norm) else service().getPlaylistExtractor(norm)
            ext.fetchPage()
            ext.initialPage.items.mapNotNull { info ->
                try {
                    val u: String? = try { info.url } catch (_: Exception) { null }
                    if (u.isNullOrBlank()) return@mapNotNull null
                    val name: String? = try { decodeHtml(info.name) } catch (_: Exception) { info.name }
                    val up: String? = try { decodeHtml(info.uploaderName) } catch (_: Exception) { info.uploaderName }
                    var thumb: String? = null
                    try { thumb = bestThumb(info.thumbnails) } catch (_: Exception) { }
                    var dur: Long? = null
                    try { dur = info.duration } catch (_: Exception) { }
                    val id: String? = u.substringAfter("v=", "").substringBefore("&").takeIf { it.isNotEmpty() }
                    var chUrl: String? = null
                    try { chUrl = info.uploaderUrl } catch (_: Exception) { }
                    VideoItem(id, name, up, thumb, dur, null, u, null, false, null, false, chUrl)
                } catch (_: Exception) { null }
            }
        } catch (_: Exception) { emptyList() }
    }
}
