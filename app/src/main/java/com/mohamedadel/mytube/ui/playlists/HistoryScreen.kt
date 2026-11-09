package com.mohamedadel.mytube.ui.playlists

import com.mohamedadel.mytube.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.data.local.HistoryEntity
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.results.SearchRow

// Listening history: newest first (capped at 100). Tap = replay, ✕ = remove, trash = clear all.
@Composable
fun HistoryScreen(
    items: List<HistoryEntity>,
    onPlay: (VideoItem) -> Unit,
    onDelete: (VideoItem) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onLongClick: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = {},
    // Phase 5: offline-pin badges + filter tabs (in-memory filter, zero network)
    pinInfo: Map<String, com.mohamedadel.mytube.data.local.PinEntity> = emptyMap(),
    pinningKeys: Set<String> = emptySet()
) {
    val lctx = androidx.compose.ui.platform.LocalContext.current
    var tab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    fun sizeLabel(bytes: Long): String = try {
        if (bytes < 1048576) lctx.getString(com.mohamedadel.mytube.R.string.db_kb, (bytes / 1024).toInt())
        else lctx.getString(com.mohamedadel.mytube.R.string.db_mb, bytes / 1048576.0)
    } catch (_: Exception) { "" }
    val shown = remember(items, tab, pinInfo) {
        when (tab) {
            1 -> items.filter { h -> pinInfo.containsKey(h.videoKey) }
            2 -> items.filter { h -> !pinInfo.containsKey(h.videoKey) }
            else -> items
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                if (items.isNotEmpty()) stringResource(R.string.hist_title_fmt, items.size) else stringResource(R.string.nav_history),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (items.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.clear_history), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        // filter tabs: all / pinned / unpinned
        androidx.compose.material3.TabRow(selectedTabIndex = tab) {
            androidx.compose.material3.Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(com.mohamedadel.mytube.R.string.hist_tab_all)) })
            androidx.compose.material3.Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(com.mohamedadel.mytube.R.string.hist_tab_pinned)) })
            androidx.compose.material3.Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(com.mohamedadel.mytube.R.string.hist_tab_unpinned)) })
        }
        Spacer(Modifier.height(8.dp))
        if (shown.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(48.dp))
                Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.hist_empty), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.hist_empty_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(shown, key = { _, h -> h.videoKey }) { _, h ->
                    val v = h.toVideoItem()
                    val pin = pinInfo[h.videoKey]
                    val badge = pin?.let { p ->
                        "📌 " + (p.quality ?: "") + (if (p.sizeBytes > 0) " • " + sizeLabel(p.sizeBytes) else "")
                    }
                    SearchRow(
                        v = v,
                        isCurrent = false,
                        isPlaying = false,
                        onClick = { onPlay(v) },
                        onLongClick = { onLongClick(it) },
                        onRemove = onDelete,
                        pinBadge = badge,
                        pinning = pinningKeys.contains(h.videoKey)
                    )
                }
            }
        }
        Spacer(Modifier.width(0.dp))
    }
}
