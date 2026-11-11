package com.mohamedadel.mytube.ui.results

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        onPinAll = onPinAll
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
    onPinAll: ((List<VideoItem>) -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                rp.title ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (items.isNotEmpty()) {
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
            }
        }
    }
}
