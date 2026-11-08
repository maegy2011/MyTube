package com.mohamedadel.mytube.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.data.youtube.VideoItem

// Home (trending tabs + country badge) extracted from MainActivity (split step 6, final).

// Real YouTube kiosk sections (null = default Trending). "news" has no kiosk: region search, labeled.
val TREND_TABS = listOf(
    Triple("عام", null, false),
    Triple("موسيقى", "trending_music", false),
    Triple("ألعاب", "trending_gaming", false),
    Triple("أفلام", "trending_movies_and_shows", false),
    Triple("بودكاست", "trending_podcasts_episodes", false),
    Triple("مباشر", "live", false),
    Triple("أخبار", "news", true)
)

// Real per-country trending (kiosk) in "عام"; category tabs search within the same country.
// A small badge names the country so a non-local list is never passed off as local.
// Standalone search screen (YouTube-style): back + full-width field + hint. Opens over content.
@Composable
fun HomeEmptyState(
    trending: List<String>,
    trendTab: Int,
    onTrendTab: (Int) -> Unit,
    trendingItems: List<VideoItem>,
    trendingLoading: Boolean,
    countryName: String,
    onSearch: (String) -> Unit,
    onPlay: (VideoItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = trendTab, edgePadding = 8.dp) {
            TREND_TABS.forEachIndexed { i, (label, _, _) ->
                Tab(selected = trendTab == i, onClick = { onTrendTab(i) }, text = { Text(label) })
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("رائج الآن", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(countryName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
        }
        // every tab is a real kiosk list (or labeled region-search for news); empty = honest fallback chips
        if (trendingLoading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
        } else if (trendingItems.isEmpty()) {
            Text(
                if (TREND_TABS.getOrNull(trendTab)?.third == true) "لا توجد نتائج إخبارية لهذا البلد — جرّب بلدًا آخر من الإعدادات."
                else "تعذّر جلب هذا القسم لبلدك حاليًا — جرّب لاحقًا أو غيّر البلد من الإعدادات.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                items(trending) { q ->
                    FilterChip(selected = false, onClick = { onSearch(q) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }, label = { Text(q) })
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trendingItems, key = { v -> v.id ?: v.url ?: v.title ?: v.hashCode().toString() }) { v ->
                    SearchRow(v = v, isCurrent = false, isPlaying = false, onClick = { onPlay(v) }, onLongClick = {}, onAdd = {})
                }
            }
        }
    }
}


