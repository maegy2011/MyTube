package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    isFav: (VideoItem) -> Boolean,
    onFav: (VideoItem) -> Unit,
    onAdd: (VideoItem) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when {
                loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                items.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text(stringResource(R.string.feed_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.feed_refresh), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                else -> LazyColumn(
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
                    itemsIndexed(items = items, key = { idx, v -> "$idx|${v.id ?: v.url ?: v.title ?: "n/a"}" }) { _, v ->
                        SearchRow(v = v, isCurrent = false, isPlaying = false, onClick = { onPlay(v) }, onLongClick = {}, onAdd = onAdd, isFav = isFav(v), onFav = onFav)
                    }
                }
            }
        }
    }
}
