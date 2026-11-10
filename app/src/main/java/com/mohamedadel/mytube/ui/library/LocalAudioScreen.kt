// On-device audio screen: folder filter chips (long-press excludes a folder),
// strict/compat scan toggle, excluded-folder restore, tap = play the file.
package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Audiotrack
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material3.FilterChip
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
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.results.SearchRow
import com.mohamedadel.mytube.util.LocalTrack
import com.mohamedadel.mytube.util.toVideoItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalAudioScreen(
    tracks: List<LocalTrack>,
    folders: List<String>,
    folderFilter: String?,
    onFolder: (String?) -> Unit,
    onExcludeFolder: (String) -> Unit,
    excluded: Set<String>,
    onUnexclude: (String) -> Unit,
    compat: Boolean,
    onCompat: (Boolean) -> Unit,
    needsPermission: Boolean,
    onRequestPermission: () -> Unit,
    onRescan: () -> Unit,
    onBack: () -> Unit,
    onPlay: (VideoItem) -> Unit,
    isFav: (VideoItem) -> Boolean,
    onFav: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                stringResource(R.string.local_audio_title, tracks.size),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onRescan) {
                Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.rescan), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (needsPermission) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Audiotrack, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(8.dp))
                Text(stringResource(R.string.local_perm_hint), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onRequestPermission) { Text(stringResource(R.string.local_perm_grant)) }
            }
            return
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(selected = folderFilter == null, onClick = { onFolder(null) }, label = { Text(stringResource(R.string.local_all)) })
            folders.forEach { f ->
                FilterChip(
                    selected = folderFilter == f,
                    onClick = { onFolder(if (folderFilter == f) null else f) },
                    label = { Text(f) },
                    modifier = Modifier.combinedClickable(
                        onClick = { onFolder(if (folderFilter == f) null else f) },
                        onLongClick = { onExcludeFolder(f) }
                    )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(if (compat) R.string.local_scan_compat else R.string.local_scan_strict),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { onCompat(!compat) }) {
                Text(stringResource(if (compat) R.string.local_use_strict else R.string.local_use_compat))
            }
        }
        if (excluded.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.local_excluded), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                excluded.forEach { f ->
                    FilterChip(selected = false, onClick = { onUnexclude(f) }, label = { Text(f) }, trailingIcon = { Icon(Icons.Rounded.Undo, contentDescription = null) })
                }
            }
        }
        if (tracks.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.local_empty), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(stringResource(R.string.local_empty_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                itemsIndexed(items = tracks, key = { _, t -> t.uri }) { _, t ->
                    val v = t.let { it.toVideoItem() }
                    SearchRow(
                        v = v,
                        isCurrent = false,
                        isPlaying = false,
                        onClick = { onPlay(v) },
                        onLongClick = { onLongClick(v) },
                        isFav = isFav(v),
                        onFav = onFav,
                        pinBadge = null,
                        pinning = false
                    )
                }
            }
        }
    }
}

// Route wrapper: keeps the giant MyTubeApp when-branch to a single call (the 64KB
// method limit is real — same reason RemoteRoute/DownloadsRoute exist).
@Composable
fun LocalAudioRoute(
    tracks: List<LocalTrack>,
    folderFilter: String?,
    onFolder: (String?) -> Unit,
    onExcludeFolder: (String) -> Unit,
    excluded: Set<String>,
    onUnexclude: (String) -> Unit,
    compat: Boolean,
    onCompat: (Boolean) -> Unit,
    needsPermission: Boolean,
    onRequestPermission: () -> Unit,
    onRescan: () -> Unit,
    onBack: () -> Unit,
    onPlay: (VideoItem) -> Unit,
    isFav: (VideoItem) -> Boolean,
    onFav: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit = {}
) {
    val folders = androidx.compose.runtime.remember(tracks) { tracks.mapNotNull { it.folder }.distinct().sorted() }
    val shown = androidx.compose.runtime.remember(tracks, folderFilter) {
        if (folderFilter == null) tracks else tracks.filter { it.folder == folderFilter }
    }
    LocalAudioScreen(
        tracks = shown,
        folders = folders,
        folderFilter = folderFilter,
        onFolder = onFolder,
        onExcludeFolder = onExcludeFolder,
        excluded = excluded,
        onUnexclude = onUnexclude,
        compat = compat,
        onCompat = onCompat,
        needsPermission = needsPermission,
        onRequestPermission = onRequestPermission,
        onRescan = onRescan,
        onBack = onBack,
        onPlay = onPlay,
        isFav = isFav,
        onFav = onFav,
        onLongClick = onLongClick
    )
}
