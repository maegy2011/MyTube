package com.mohamedadel.mytube.ui.results

import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.data.youtube.VideoItem

// Home (trending tabs + country badge) extracted from MainActivity (split step 6, final).

// Real YouTube kiosk sections (null = default Trending). "news" has no kiosk: region search, labeled.
val TREND_TABS = listOf(
    Triple(R.string.tab_general, null, false),
    Triple(R.string.tab_music, "trending_music", false),
    Triple(R.string.tab_gaming, "trending_gaming", false),
    Triple(R.string.tab_movies, "trending_movies_and_shows", false),
    Triple(R.string.tab_podcasts, "trending_podcasts_episodes", false),
    Triple(R.string.tab_live, "live", false),
    Triple(R.string.tab_news, "news", true),
    Triple(R.string.tab_suggestions, "suggestions", false)
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
    isSuggestionsTab: Boolean = false,
    suggestionChips: Map<String, List<String>> = emptyMap(),
    onSuggestion: (String) -> Unit = {},
    onPlay: (VideoItem) -> Unit,
    isFav: (VideoItem) -> Boolean = { false },
    onFav: (VideoItem) -> Unit = {},
    onLongClick: (VideoItem) -> Unit = {},
    pinBadges: Map<String, String> = emptyMap(),
    pinningKeys: Set<String> = emptySet(),
    progressOf: (String) -> Float? = { null },
    // Official section header (2026-11-12): the extractor kiosk's own section name
    // for this tab (announced as a heading); null = tabs alone (legacy).
    sectionTitle: String? = null,
    // Music surface (a): true only on the official YouTube Music tab.
    showMusicBadge: Boolean = false,
    // screens-fix Home P0-H2: error surface + retry.
    trendingError: String? = null,
    onRetryTrending: () -> Unit = {},
    // Deferred item 1 (2026-11-11): real kiosk/search pagination + pull-to-refresh on Home.
    hasMore: Boolean = false,
    loadingMore: Boolean = false,
    onShowMore: () -> Unit = {},
    // home-redesign Phase 2 (2026-11-12): recent history for the continue-watching row.
    continueItems: List<VideoItem> = emptyList()
) {
    // Deferred item 1: hoisted scroll state (was recreated per composition inside the else branch).
    val trendListState = androidx.compose.foundation.lazy.rememberLazyListState()
    // Deferred item 1: custom pull-to-refresh for Home (same 360px pattern as FeedScreen,
    // with a visible release hint so it is discoverable + TalkBack-announced).
    var pullPx by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0f) }
    var pullFired by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val pullConn = androidx.compose.runtime.remember(trendingLoading) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (trendingLoading) return androidx.compose.ui.geometry.Offset.Zero
                val atTop = trendListState.firstVisibleItemIndex == 0 && trendListState.firstVisibleItemScrollOffset == 0
                if (available.y > 0 && atTop && !pullFired) {
                    pullPx += available.y
                    if (pullPx >= 360f) { pullFired = true; onRetryTrending() }
                    return available
                }
                if (available.y < 0) pullPx = 0f
                return androidx.compose.ui.geometry.Offset.Zero
            }
            override suspend fun onPostFling(consumed: androidx.compose.ui.unit.Velocity, available: androidx.compose.ui.unit.Velocity): androidx.compose.ui.unit.Velocity {
                pullPx = 0f; pullFired = false
                return super.onPostFling(consumed, available)
            }
        }
    }
    androidx.compose.runtime.LaunchedEffect(trendingLoading) { if (!trendingLoading) { pullPx = 0f; pullFired = false } }
    Column(modifier = Modifier.fillMaxSize().nestedScroll(pullConn)) {
        ScrollableTabRow(selectedTabIndex = trendTab, edgePadding = 8.dp) {
            TREND_TABS.forEachIndexed { i, (label, _, _) ->
                Tab(selected = trendTab == i, onClick = { onTrendTab(i) }, text = { Text(stringResource(label)) })
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.trending_now), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            if (!sectionTitle.isNullOrBlank()) {
                Spacer(Modifier.width(8.dp))
                Text(
                    sectionTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }
                )
            }
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                Text(countryName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
            }
            // Deferred item 1: explicit refresh affordance next to the country badge.
            androidx.compose.material3.IconButton(onClick = onRetryTrending, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                androidx.compose.material3.Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.feed_refresh), tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (pullPx > 24f && !trendingLoading) {
            // Pull hint: visible release cue (was silent in FeedScreen's version).
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                if (pullFired) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text(stringResource(R.string.pull_to_refresh), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        // every tab is a real kiosk list (or labeled region-search for news); empty = honest fallback chips
        if (trendingLoading) {
            VideoListSkeleton(count = 4)
        } else if (trendingError != null && trendingItems.isEmpty()) {
            // screens-fix Home P0-H2: explicit error + retry (was silent chips).
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    trendingError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                androidx.compose.material3.Button(onClick = onRetryTrending) {
                    Text(stringResource(R.string.retry))
                }
            }
        } else if (trendingItems.isEmpty()) {
            Text(
                if (TREND_TABS.getOrNull(trendTab)?.third == true) stringResource(R.string.news_empty)
                else stringResource(R.string.err_kiosk),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
                items(trending) { q ->
                    FilterChip(selected = false, onClick = { onSearch(q) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }, label = { Text(q) })
                }
            }
        } else if (!isSuggestionsTab) {
            // home-redesign Phase 2 (2026-11-12): Netflix layout (Hero + rows + continue-watching);
            // the suggestions tab keeps the classic vertical list below.
            com.mohamedadel.mytube.ui.home.HomeNetflixLayout(
                items = trendingItems,
                continueItems = continueItems,
                onPlay = onPlay,
                onLongClick = onLongClick,
                hasMore = hasMore,
                loadingMore = loadingMore,
                onShowMore = onShowMore,
                listState = trendListState,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                state = trendListState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (isSuggestionsTab && suggestionChips.isNotEmpty()) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                stringResource(R.string.sugg_based),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            suggestionChips.forEach { (seed, chips) ->
                                if (chips.isNotEmpty()) {
                                    Text(
                                        seed,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    @OptIn(ExperimentalLayoutApi::class)
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(0.dp)
                                    ) {
                                        chips.forEach { c ->
                                            FilterChip(selected = false, onClick = { onSuggestion(c) }, label = { Text(c) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                items(trendingItems, key = { v -> v.id ?: v.url ?: v.title ?: v.hashCode().toString() }) { v ->
                    SearchRow(v = v, isCurrent = false, isPlaying = false, onClick = { onPlay(v) }, onLongClick = { onLongClick(v) }, isFav = isFav(v), onFav = onFav, pinBadge = pinBadges[v.favKey()], pinning = pinningKeys.contains(v.favKey()), pinProgress = progressOf(v.favKey()), showMusicBadge = showMusicBadge && shouldShowMusicBadge(v, MusicSurface.YT_MUSIC_TAB))
                }
                // Deferred item 1: honest pagination — hidden when the kiosk/search has no next page.
                if (!isSuggestionsTab && (hasMore || loadingMore)) {
                    item(key = "trend-more") {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            if (loadingMore) CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            else androidx.compose.material3.Button(onClick = onShowMore) { Text(stringResource(R.string.show_more)) }
                        }
                    }
                }
            }
        }
    }
}


