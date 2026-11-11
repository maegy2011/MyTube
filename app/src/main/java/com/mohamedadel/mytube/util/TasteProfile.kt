// Local taste profile: recommendations without an account.
// Built on-device from play history (weighted most), favorites, and recent searches.
// No network, no login, no API keys — pure local scoring. The one network enrichment
// (a YTM song search for the top term) happens in MainActivity, never here.
package com.mohamedadel.mytube.util

import com.mohamedadel.mytube.data.local.FavoriteEntity
import com.mohamedadel.mytube.data.local.PlayedStat
import com.mohamedadel.mytube.data.youtube.VideoItem

data class TasteProfile(
    val terms: List<String> = emptyList(),
    val picks: List<VideoItem> = emptyList()
)

private val STOP = setOf(
    // English
    "the", "and", "for", "with", "from", "that", "this", "you", "your", "official",
    "video", "audio", "full", "live", "music", "song", "songs", "lyrics", "lyric",
    "cover", "remix", "version", "original", "feat", "ft", "vol", "part", "episode",
    // screens-fix Music P2-M12: extra music-metadata noise tokens.
    "clip", "single", "deluxe", "remaster", "reverb", "slowed", "sped", "nightcore",
    // Arabic
    "من", "في", "على", "إلى", "عن", "مع", "هذا", "هذه", "التي", "الذي",
    "كلمات", "اغنية", "أغنية", "فيديو", "مباشر", "رسمي", "رسمية", "كامل", "كاملة", "جديد", "جديدة",
    "مقطع", "نسخة", "مترجمة", "مترجم", "حصري", "حصريا"
)

private val TOKEN = Regex("[\\p{L}\\p{Nd}]+")

private fun tokensOf(text: String?, weight: Int, out: MutableMap<String, Int>) {
    if (text.isNullOrBlank()) return
    for (m in TOKEN.findAll(text.lowercase())) {
        val t = m.value
        if (t.length < 3 || t.all { it.isDigit() } || t in STOP) continue
        out[t] = (out[t] ?: 0) + weight
    }
}

// Short raw queries ("عمرو دياب", "lofi beats") are taste terms as-is AND tokenized.
private fun queryTerms(q: String, outTerms: MutableSet<String>, outTokens: MutableMap<String, Int>) {
    val t = q.trim()
    if (t.length < 2) return
    if (t.length <= 24) outTerms.add(t)
    tokensOf(t, 2, outTokens)
}

fun buildTasteProfile(
    topPlayed: List<PlayedStat>,
    favorites: List<FavoriteEntity>,
    recentMusic: List<String>,
    recentMain: List<String>
): TasteProfile {
    val tokens = mutableMapOf<String, Int>()
    val rawQueries = linkedSetOf<String>()
    for (p in topPlayed) {
        // repeat plays are the strongest signal: weight by play count (capped)
        val w = 1 + minOf(p.plays, 9).toInt()
        tokensOf(p.title, w, tokens)
        tokensOf(p.channel, w / 2 + 1, tokens)
    }
    for (f in favorites.take(40)) {
        tokensOf(f.title, 2, tokens)
        tokensOf(f.channel, 1, tokens)
    }
    for (q in recentMusic.take(6)) queryTerms(q, rawQueries, tokens)
    for (q in recentMain.take(6)) queryTerms(q, rawQueries, tokens)
    // raw short queries first (exact user intent), then top tokens
    val terms = (rawQueries.toList() +
        tokens.entries.sortedByDescending { it.value }.map { it.key })
        .distinct().take(8)
    val picks = topPlayed.take(8).map { p ->
        VideoItem(
            id = p.videoId, title = p.title, channel = p.channel,
            thumbnail = p.thumbnail, url = p.url
        )
    }
    return TasteProfile(terms, picks)
}
