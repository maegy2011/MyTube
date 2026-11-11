package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

// Deferred item 1 (2026-11-11): channel-header rows for the feed (file-private: local sealed types are illegal).
sealed interface FeedRow {
    data class Header(val key: String, val name: String, val count: Int) : FeedRow
    data class Video(val v: VideoItem) : FeedRow
}
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.results.SearchRow

// A3 subscription feed: merged latest videos of all subscribed channels (manual refresh,
// each channel fetched in its own try — one dead channel never kills the feed).
@Composable
fun FeedScreen(
    items: List<VideoItem>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit = {},
    isFav: (VideoItem) -> Boolean,
    onFav: (VideoItem) -> Unit,
    // R3: channel filter (tap a library row); R4: manual pull-to-refresh (no PullToRefreshBox in M3 1.3.0)
    filterChannelUrl: String? = null,
    filterChannelName: String? = null,
    onClearFilter: () -> Unit = {},
    filterUrls: Set<String> = emptySet(),
    filterLabel: String? = null,
    pinBadges: Map<String, String> = emptyMap(),
    pinningKeys: Set<String> = emptySet()
) {
    val shown = remember(items, filterChannelUrl, filterUrls) {
        if (filterUrls.isNotEmpty()) items.filter { it.channelUrl in filterUrls }
        else if (filterChannelUrl == null) items else items.filter { it.channelUrl == filterChannelUrl }
    }
    // Deferred item 1 (2026-11-11): channel headers. The feed stays round-robin interleaved
    // (P0-H1: no publishedAt in VideoItem, so grouping would rebuild channel blocks) — instead a
    // compact header precedes each channel's FIRST visible video and names its video count,
    // YouTube-"latest from X" style. Filtered views skip headers (single channel already known).
    val rows: List<FeedRow> = remember(shown, filterChannelUrl, filterUrls) {
        if (filterChannelUrl != null || filterUrls.isNotEmpty()) return@remember shown.map { FeedRow.Video(it) }
        val counts = shown.groupingBy { it.channelUrl ?: it.channel ?: "?" }.eachCount()
        val seen = mutableSetOf<String>()
        buildList {
            for (v in shown) {
                val ck = v.channelUrl ?: v.channel ?: "?"
                if (seen.add(ck)) add(FeedRow.Header("h-$ck", v.channel ?: ck, counts[ck] ?: 1))
                add(FeedRow.Video(v))
            }
        }
    }
    // R4: nested-scroll pull gesture — only when the list rests at the very top.
    var pullPx by remember { mutableStateOf(0f) }
    var pullFired by remember { mutableStateOf(false) }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val pullConn = remember(loading) {
        object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
            override fun onPreScroll(available: androidx.compose.ui.geometry.Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): androidx.compose.ui.geometry.Offset {
                if (loading) return androidx.compose.ui.geometry.Offset.Zero
                val atTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
                if (available.y > 0 && atTop && !pullFired) {
                    pullPx += available.y
                    if (pullPx >= 360f) { pullFired = true; onRefresh() }
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
    LaunchedEffect(loading) { if (!loading) { pullPx = 0f; pullFired = false } }
    Column(modifier = Modifier.fillMaxSize().nestedScroll(pullConn)) {
        if (pullPx > 24f && !loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            }
        }
        if (filterChannelUrl != null || filterUrls.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onClearFilter() }.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.feed_filter_fmt, filterLabel ?: filterChannelName ?: filterChannelUrl ?: ""),
                    style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                )
            }
        }
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when {
                loading -> com.mohamedadel.mytube.ui.results.VideoListSkeleton(count = 4)
                shown.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        // screens-fix Library P1-L8: group feeds get their own empty copy (was generic).
                        Text(
                            if (filterLabel != null) stringResource(R.string.feed_empty_group_fmt, filterLabel) else stringResource(R.string.feed_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.feed_refresh), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.feed_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            IconButton(onClick = onRefresh) {
                                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.feed_refresh), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    // screens-fix Home P0-H3: stable keys (no index prefix).
                    items(items = rows, key = { r -> when (r) { is FeedRow.Header -> r.key; is FeedRow.Video -> r.v.id ?: r.v.url ?: r.v.title ?: r.v.hashCode().toString() } }) { r ->
                        when (r) {
                            is FeedRow.Header -> Text(
                                stringResource(R.string.feed_channel_fmt, r.name, r.count),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
                                    .semantics { heading() }
                            )
                            is FeedRow.Video -> {
                                val v = r.v
                                SearchRow(v = v, isCurrent = false, isPlaying = false, onClick = { onPlay(v) }, onLongClick = { onLongClick(v) }, isFav = isFav(v), onFav = onFav, pinBadge = pinBadges[v.favKey()], pinning = pinningKeys.contains(v.favKey()))
                            }
                        }
                    }
                }
            }
        }
    }
}
