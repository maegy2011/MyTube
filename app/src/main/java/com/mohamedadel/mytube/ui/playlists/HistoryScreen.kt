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
    onBack: () -> Unit
) {
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
        if (items.isEmpty()) {
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
                itemsIndexed(items, key = { _, h -> h.videoKey }) { _, h ->
                    val v = h.toVideoItem()
                    SearchRow(
                        v = v,
                        isCurrent = false,
                        isPlaying = false,
                        onClick = { onPlay(v) },
                        onLongClick = {},
                        onRemove = onDelete
                    )
                }
            }
        }
        Spacer(Modifier.width(0.dp))
    }
}
