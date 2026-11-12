package com.mohamedadel.mytube.ui.results

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.theme.ScrimSoft
import androidx.compose.ui.graphics.Color

// Music badge (runtime layer, 2026-11-12): a small ♪ pill marking music items on
// three surfaces only — (a) the official YouTube Music trending tab, (b) YouTube
// Music service items (music_songs/music_playlists filters, music.youtube.com links),
// (c) SoundCloud tracks. Theme-token colors, 48dp-safe (decorative row, TalkBack
// announces the badge label once via contentDescription).

enum class MusicSurface { YT_MUSIC_TAB, YT_MUSIC_SERVICE, SOUNDCLOUD }

// SACRED-CONTENT EXCLUSION (hard rule, never violated): Quran recitations, religious
// sermons/lessons, podcasts and similar MUST NEVER carry the music badge. Matching is
// a keyword denylist over title + channel (+ description when available, list rows
// only carry title/channel). Arabic is normalized before matching (أإآ→ا, ة→ه, ى→ي)
// and both Arabic + English keywords are covered:
//   ar: قرآن/قران، تلاوة، تجويد، ترتيل، خطبة/خطب، درس ديني/دروس دينية، شيخ، أذان/اذان،
//       إقامة، دعاء/أدعية، حديث نبوي، السيرة النبوية، تفسير، فقه، توحيد، عقيدة،
//       إنشاد/أناشيد دينية، مديح نبوي، ابتهالات، رقية شرعية، صحيح البخاري/مسلم،
//       صلاة، الجمعة، بودكاست
//   en: quran, koran, recitation, tajweed, sermon, khutbah/khutba, adhan/azan,
//       nasheed (religious or ambiguous — secular use is unclear, so suppress),
//       dua, hadith, seerah, tafsir/tafseer, fiqh, salah, prayer, jumuah,
//       bible, gospel, pastor, church, podcast
// WHEN IN DOUBT → NO badge (blank title = suppress; any keyword hit = suppress).
private val SACRED_AR = listOf(
    "قران", "تلاوة", "تجويد", "ترتيل", "خطبة", "خطب", "درس ديني", "دروس دينية",
    "شيخ", "اذان", "اقامة", "دعاء", "ادعية", "حديث نبوي", "السيرة النبوية",
    "تفسير", "فقه", "توحيد", "عقيدة", "انشاد", "اناشيد دينية", "مديح نبوي",
    "ابتهال", "رقية", "البخاري", "مسلم", "صلاة", "الجمعة", "بودكاست"
)
private val SACRED_EN = listOf(
    "quran", "koran", "recitation", "tajweed", "sermon", "khutbah", "khutba",
    "adhan", "azan", "nasheed", "dua", "hadith", "seerah", "tafsir", "tafseer",
    "fiqh", "salah", "jumuah", "bible", "gospel", "pastor", "church", "podcast"
)

private fun normAr(s: String): String {
    var o = s.lowercase()
    o = o.replace('أ', 'ا').replace('إ', 'ا').replace('آ', 'ا')
    o = o.replace('ة', 'ه').replace('ى', 'ي')
    return o
}

fun isSacredContent(title: String?, channel: String?, description: String? = null): Boolean {
    if (title.isNullOrBlank() && channel.isNullOrBlank()) return true // doubt → suppress
    val hayAr = normAr(listOfNotNull(title, channel, description).joinToString(" "))
    if (SACRED_AR.any { k -> hayAr.contains(normAr(k)) }) return true
    val hayEn = listOfNotNull(title, channel, description).joinToString(" ").lowercase()
    // word-ish match for short en tokens (dua, fiqh…) to avoid false hits inside words
    if (SACRED_EN.any { k ->
        if (k.length <= 4) Regex("\\b$k\\b").containsMatchIn(hayEn) else hayEn.contains(k)
    }) return true
    return false
}

fun shouldShowMusicBadge(item: VideoItem, surface: MusicSurface, description: String? = null): Boolean {
    if (item.isChannel) return false
    return when (surface) {
        // (a) official YouTube Music trending kiosk: every track is music by construction.
        MusicSurface.YT_MUSIC_TAB -> true
        // (b) YouTube Music service items: the caller only passes items from a YouTube
        // Music context (music_songs/music_playlists filters, music.youtube.com links,
        // taste-mix shelf) — the context itself is the music signal, so badge unless sacred.
        MusicSurface.YT_MUSIC_SERVICE -> true
        // (c) SoundCloud tracks: soundcloud.com audio URLs only.
        MusicSurface.SOUNDCLOUD -> (item.url ?: "").contains("soundcloud.com")
    }.let { isMusic ->
        if (!isMusic) false
        else !isSacredContent(item.title, item.channel, description) // sacred → never
    }
}

@Composable
fun MusicBadge(modifier: Modifier = Modifier) {
    val cd = stringResource(R.string.music_badge_desc)
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = modifier.semantics { contentDescription = cd }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                stringResource(R.string.music_badge_label),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 1
            )
        }
    }
}

// Thumbnail-corner variant for artwork cards (MusicCard shelves).
@Composable
fun MusicBadgeOverlay(modifier: Modifier = Modifier) {
    val cd = stringResource(R.string.music_badge_desc)
    Surface(
        color = ScrimSoft,
        shape = MaterialTheme.shapes.extraSmall,
        modifier = modifier.semantics { contentDescription = cd }
    ) {
        Icon(
            Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp).size(14.dp)
        )
    }
}
