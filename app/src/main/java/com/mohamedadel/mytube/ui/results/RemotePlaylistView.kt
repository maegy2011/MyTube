package com.mohamedadel.mytube.ui.results

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.RemotePlaylist
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.local.favKey

// Route wrapper: keeps the giant MyTubeApp when-branch to a single call (the 64KB
// method limit is real — this branch alone carries 8 lambdas).
@Composable
fun RemoteRoute(
    rp: RemotePlaylist,
    loading: Boolean,
    items: List<VideoItem>,
    playingUrl: String?,
    isPlaying: Boolean,
    favSet: Set<String>,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    progressOf: (String) -> Float? = { null },
    onBack: () -> Unit,
    onPlayAll: (List<VideoItem>) -> Unit,
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit,
    onFav: (VideoItem) -> Unit,
    // screens-fix Library P1-L5: remote playlists were read-only — save + pin-all entry points.
    onSave: ((List<VideoItem>) -> Unit)? = null,
    onPinAll: ((List<VideoItem>) -> Unit)? = null,
    // long sets (SoundCloud): page-walk instead of first-page-only.
    hasMore: Boolean = false,
    loadingMore: Boolean = false,
    onShowMore: () -> Unit = {},
) {
    RemotePlaylistView(
        rp = rp,
        loading = loading,
        items = items,
        playingUrl = playingUrl,
        isPlaying = isPlaying,
        isFav = { v -> v.favKey() in favSet },
        onBack = onBack,
        onPlayAll = onPlayAll,
        onPlay = onPlay,
        onLongClick = onLongClick,
        onFav = onFav,
        pinBadgeOf = { v -> pinBadges[v.favKey()] },
        pinningOf = { v -> pinningKeys.contains(v.favKey()) },
        progressOf = { v -> progressOf(v.favKey()) },
        onSave = onSave,
        onPinAll = onPinAll,
        hasMore = hasMore,
        loadingMore = loadingMore,
        onShowMore = onShowMore
    )
}

/**
 * Remote (service) playlist contents: title header + Play-all + playable rows.
 * Same SearchRow as everywhere else — play/fav/queue/pin all work via callbacks.
 */
@Composable
fun RemotePlaylistView(
    rp: RemotePlaylist,
    loading: Boolean,
    items: List<VideoItem>,
    playingUrl: String?,
    isPlaying: Boolean,
    isFav: (VideoItem) -> Boolean,
    onBack: () -> Unit,
    onPlayAll: (List<VideoItem>) -> Unit,
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit,
    onFav: (VideoItem) -> Unit,
    pinBadgeOf: (VideoItem) -> String?,
    pinningOf: (VideoItem) -> Boolean,
    progressOf: (VideoItem) -> Float? = { null },
    onSave: ((List<VideoItem>) -> Unit)? = null,
    onPinAll: ((List<VideoItem>) -> Unit)? = null,
    hasMore: Boolean = false,
    loadingMore: Boolean = false,
    onShowMore: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // rich header: cover art + title + uploader/count (long SoundCloud sets show what they hold).
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
            if (!rp.thumbnail.isNullOrBlank()) {
                AsyncImage(model = rp.thumbnail, contentDescription = rp.title, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    rp.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val meta = buildString {
                    if (!rp.uploader.isNullOrBlank()) append(rp.uploader)
                    if (rp.count > 0) { if (isNotEmpty()) append(" • "); append(rp.count) }
                }
                if (meta.isNotEmpty()) {
                    Text(meta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        if (items.isNotEmpty()) {
            Row(modifier = Modifier.padding(horizontal = 4.dp)) {
                TextButton(onClick = { onPlayAll(items) }) { Text(stringResource(R.string.remote_play_all)) }
                if (onSave != null) {
                    TextButton(onClick = { onSave(items) }) { Text(stringResource(R.string.remote_save)) }
                }
                if (onPinAll != null) {
                    TextButton(onClick = { onPinAll(items) }) { Text(stringResource(R.string.remote_pin_all)) }
                }
            }
        }
        if (loading) {
            VideoListSkeleton(count = 5)
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.remote_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // screens-fix appendix-4: stable keys (no index prefix).
                itemsIndexed(items = items, key = { _, v -> v.id ?: v.url ?: v.title ?: v.hashCode().toString() }) { _, v ->
                    SearchRow(
                        v = v,
                        isCurrent = playingUrl == v.url,
                        isPlaying = isPlaying && playingUrl == v.url,
                        onClick = { onPlay(v) },
                        onLongClick = onLongClick,
                        isFav = isFav(v),
                        onFav = onFav,
                        pinBadge = pinBadgeOf(v),
                        pinning = pinningOf(v),
                        pinProgress = progressOf(v)
                    )
                }
                // long sets: honest pagination instead of a first-page wall.
                if (hasMore || loadingMore) {
                    item(key = "remote-more") {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            if (loadingMore) androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            else androidx.compose.material3.Button(onClick = onShowMore) { Text(stringResource(R.string.show_more)) }
                        }
                    }
                }
            }
        }
    }
}
