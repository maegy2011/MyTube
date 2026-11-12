package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import com.mohamedadel.mytube.ui.theme.LiveBadge
import com.mohamedadel.mytube.ui.theme.OnLiveBadge
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.youtube.ChannelDetail
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.RemotePlaylist
import com.mohamedadel.mytube.ui.results.PlaylistCard
import com.mohamedadel.mytube.ui.results.SearchRow
import com.mohamedadel.mytube.ui.results.VideoListSkeleton

// Full channel screen (header + Videos/About tabs). All data from existing
// channelDetail/channelVideos calls — no account, no subscription feed changes.
@Composable
fun ChannelScreen(
    detail: ChannelDetail?,
    videos: List<VideoItem>,
    videosLoading: Boolean,
    isSubscribed: Boolean,
    onSubscribe: () -> Unit,
    onBack: () -> Unit,
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit = {},
    onAdd: ((VideoItem) -> Unit)? = null,
    isFav: (VideoItem) -> Boolean = { false },
    onFav: (VideoItem) -> Unit = {},
    pinBadges: Map<String, String> = emptyMap(),
    pinningKeys: Set<String> = emptySet(),
    progressOf: (String) -> Float? = { null },
    subsLabel: (Long?) -> String = { "" },
    // Extra tabs (lazy-loaded by the caller via onTabSelect): Shorts / Live / Playlists.
    shorts: List<VideoItem> = emptyList(),
    shortsLoading: Boolean = false,
    live: List<VideoItem> = emptyList(),
    liveLoading: Boolean = false,
    channelPlaylists: List<RemotePlaylist> = emptyList(),
    channelPlaylistsLoading: Boolean = false,
    onTabSelect: (Int) -> Unit = {},
    onOpenPlaylist: (RemotePlaylist) -> Unit = {}
) {
    var tab by remember { mutableStateOf(0) }
    var descOpen by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(
                detail?.name ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
        if (!detail?.banner.isNullOrBlank()) {
            AsyncImage(model = detail?.banner, contentDescription = null, modifier = Modifier.fillMaxWidth().height(110.dp), contentScale = ContentScale.Crop)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!detail?.avatar.isNullOrBlank()) {
                AsyncImage(model = detail?.avatar, contentDescription = detail?.name, modifier = Modifier.size(56.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(detail?.name ?: "", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (detail?.verified == true) Text(" ✓", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
                }
                val sl = subsLabel(detail?.subs)
                if (sl.isNotEmpty()) Text(sl, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Spacer(Modifier.width(8.dp))
            if (isSubscribed) {
                OutlinedButton(onClick = onSubscribe) { Text(stringResource(R.string.unsubscribe)) }
            } else {
                Button(onClick = onSubscribe) { Text(stringResource(R.string.subscribe)) }
            }
        }
        val tabTitles = listOf(
            stringResource(R.string.channel_tab_videos),
            stringResource(R.string.channel_tab_shorts),
            stringResource(R.string.channel_tab_live),
            stringResource(R.string.channel_tab_playlists),
            stringResource(R.string.channel_tab_about)
        )
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 16.dp) {
            tabTitles.forEachIndexed { i, t ->
                Tab(selected = tab == i, onClick = { tab = i; onTabSelect(i) }, text = { Text(t) })
            }
        }
        if (tab == 0) {
            if (videosLoading) {
                VideoListSkeleton(count = 4)
            } else if (videos.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(32.dp))
                    Text(stringResource(R.string.feed_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    items(videos, key = { v -> v.id ?: v.url ?: v.title ?: v.hashCode().toString() }) { v ->
                        SearchRow(
                            v = v, isCurrent = false, isPlaying = false,
                            onClick = { onPlay(v) },
                            onLongClick = { onLongClick(v) },
                            isFav = isFav(v),
                            onFav = onFav,
                            pinBadge = pinBadges[v.favKey()],
                            pinning = pinningKeys.contains(v.favKey()),
                            pinProgress = progressOf(v.favKey())
                        )
                    }
                }
            }
        } else if (tab == 1) {
            ChannelVideoList(modifier = Modifier.fillMaxWidth().weight(1f), videos = shorts, loading = shortsLoading, onPlay = onPlay, onLongClick = onLongClick, onAdd = onAdd, isFav = isFav, onFav = onFav, pinBadges = pinBadges, pinningKeys = pinningKeys, progressOf = progressOf)
        } else if (tab == 2) {
            ChannelVideoList(modifier = Modifier.fillMaxWidth().weight(1f), videos = live, badge = stringResource(R.string.live_badge), loading = liveLoading, onPlay = onPlay, onLongClick = onLongClick, onAdd = onAdd, isFav = isFav, onFav = onFav, pinBadges = pinBadges, pinningKeys = pinningKeys, progressOf = progressOf)
        } else if (tab == 3) {
            if (channelPlaylistsLoading) {
                VideoListSkeleton(count = 4)
            } else if (channelPlaylists.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(32.dp))
                    Text(stringResource(R.string.feed_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(channelPlaylists, key = { it.url ?: it.title ?: it.hashCode().toString() }) { pl ->
                        PlaylistCard(p = pl, onClick = { onOpenPlaylist(pl) })
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        val d = detail?.description ?: ""
                        if (d.isBlank()) {
                            Text(stringResource(R.string.channel_no_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text(
                                if (!descOpen && d.length > 220) d.take(220) + "…" else d,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (d.length > 220) {
                                Spacer(Modifier.height(4.dp))
                                androidx.compose.material3.TextButton(onClick = { descOpen = !descOpen }) {
                                    Text(stringResource(if (descOpen) R.string.show_less else R.string.show_more))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChannelVideoList(
    modifier: Modifier = Modifier,
    videos: List<VideoItem>,
    badge: String? = null,
    loading: Boolean,
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit,
    onAdd: ((VideoItem) -> Unit)?,
    isFav: (VideoItem) -> Boolean,
    onFav: (VideoItem) -> Unit,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    progressOf: (String) -> Float? = { null }
) {
    Column(modifier = modifier) {
        if (badge != null && !loading && videos.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                // D3 (audit P1 2026-11-11): shared LiveBadge + theme shape (was raw 0xFFB71C1C).
                Surface(shape = MaterialTheme.shapes.extraSmall, color = LiveBadge) {
                    Text(text = badge, style = MaterialTheme.typography.labelSmall, color = OnLiveBadge, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
        if (loading) {
            VideoListSkeleton(count = 4)
        } else if (videos.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(32.dp))
                Text(stringResource(R.string.feed_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                items(videos, key = { v -> v.id ?: v.url ?: v.title ?: v.hashCode().toString() }) { v ->
                    SearchRow(
                        v = v, isCurrent = false, isPlaying = false,
                        onClick = { onPlay(v) },
                        onLongClick = { onLongClick(v) },
                        isFav = isFav(v),
                        onFav = onFav,
                        pinBadge = pinBadges[v.favKey()],
                        pinning = pinningKeys.contains(v.favKey()),
                        pinProgress = progressOf(v.favKey())
                    )
                }
            }
        }
    }
}
