package com.mohamedadel.mytube.ui.playlists

import com.mohamedadel.mytube.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.data.local.HistoryEntity
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.results.SearchRow

// Listening history: newest first (H1: Paging 3, page 30, cap 1000 rows). Tap = replay, ✕ = remove, trash = clear all.
@Composable
fun HistoryScreen(
    flowAll: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<HistoryEntity>>,
    flowPinned: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<HistoryEntity>>,
    flowUnpinned: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<HistoryEntity>>,
    onPlay: (VideoItem) -> Unit,
    onDelete: (VideoItem) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onLongClick: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = {},
    // Phase 5: offline-pin badges (zero network)
    pinInfo: Map<String, com.mohamedadel.mytube.data.local.PinEntity> = emptyMap(),
    pinningKeys: Set<String> = emptySet(),
    isFav: (VideoItem) -> Boolean = { false },
    onFav: (VideoItem) -> Unit = {},
    totalCount: Int = 0,
    // R2: local search (blank = paged tabs). Results grouped by recency buckets; zero network.
    searchQuery: String = "",
    onSearchQuery: (String) -> Unit = {},
    searchResults: List<HistoryEntity> = emptyList()
) {
    // screens-fix Library P0-L1: destructive clear needs explicit confirmation.
    var confirmClear by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    // screens-fix Library P0-L1 (row-delete): single history-item delete also confirms.
    var confirmRow by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<VideoItem?>(null) }
    val lctx = androidx.compose.ui.platform.LocalContext.current
    var tab by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) }
    fun sizeLabel(bytes: Long): String = try {
        if (bytes < 1048576) lctx.getString(com.mohamedadel.mytube.R.string.db_kb, (bytes / 1024).toInt())
        else lctx.getString(com.mohamedadel.mytube.R.string.db_mb, bytes / 1048576.0)
    } catch (_: Exception) { "" }
    // Tabs switch the Paging query itself (Room-side filter) — the list stays lazy at any depth.
    val activeFlow = when (tab) { 1 -> flowPinned 2 -> flowUnpinned else -> flowAll }
    val paged = remember(activeFlow) { activeFlow }.collectAsLazyPagingItems()
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                if (totalCount > 0) stringResource(R.string.hist_title_fmt, totalCount) else stringResource(R.string.nav_history),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (totalCount > 0) {
                IconButton(onClick = { confirmClear = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.clear_history), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (confirmClear) {
            ConfirmDeleteDialog(
                title = stringResource(R.string.clear_history_title),
                message = stringResource(R.string.del_hist_confirm_fmt, totalCount),
                onConfirm = { confirmClear = false; onClear() },
                onDismiss = { confirmClear = false }
            )
        }
        if (confirmRow != null) {
            val rv = confirmRow
            ConfirmDeleteDialog(
                title = stringResource(R.string.delete),
                message = stringResource(R.string.del_hist_item_fmt, rv?.title ?: ""),
                onConfirm = { confirmRow = null; if (rv != null) onDelete(rv) },
                onDismiss = { confirmRow = null }
            )
        }
        Spacer(Modifier.height(8.dp))
        // local search field (title/channel)
        androidx.compose.material3.OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.hist_search_hint)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { onSearchQuery("") }) { Icon(Icons.Rounded.Clear, contentDescription = stringResource(R.string.clear), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) } },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (searchQuery.isNotBlank()) {
            // bucketed search results (in-memory grouping over the capped Room query)
            val now = System.currentTimeMillis()
            val day = 24 * 3600 * 1000L
            val startToday = now - (now % day)
            var lastBucket = -1
            // screens-fix Library P1-L7: the Room query caps at 200 — say so.
            if (searchResults.size >= 200) {
                Text(
                    stringResource(R.string.hist_search_cap_fmt, 200),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                searchResults.forEach { h ->
                    val bucket = when {
                        h.playedAt >= startToday -> 0
                        h.playedAt >= startToday - day -> 1
                        h.playedAt >= startToday - 7 * day -> 2
                        else -> 3
                    }
                    if (bucket != lastBucket) {
                        lastBucket = bucket
                        item(key = "bucket-$bucket") {
                            Text(
                                stringResource(
                                    when (bucket) {
                                        0 -> R.string.hist_bucket_today
                                        1 -> R.string.hist_bucket_yesterday
                                        2 -> R.string.hist_bucket_week
                                        else -> R.string.hist_bucket_older
                                    }
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    item(key = h.videoKey) {
                        val v = h.toVideoItem()
                        val pin = pinInfo[h.videoKey]
                        SearchRow(
                            v = v, isCurrent = false, isPlaying = false,
                            onClick = { onPlay(v) },
                            onLongClick = { onLongClick(it) },
                        isFav = isFav(v),
                        onFav = onFav,
                            onRemove = { confirmRow = it },
                            pinBadge = pin?.let { pp -> "📌 " + (pp.quality ?: "") + (if (pp.sizeBytes > 0) " • " + sizeLabel(pp.sizeBytes) else "") },
                            pinning = pinningKeys.contains(h.videoKey)
                        )
                    }
                }
            }
            return@Column
        }
        // filter tabs: all / pinned / unpinned
        androidx.compose.material3.TabRow(selectedTabIndex = tab) {
            androidx.compose.material3.Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(com.mohamedadel.mytube.R.string.hist_tab_all)) })
            androidx.compose.material3.Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(com.mohamedadel.mytube.R.string.hist_tab_pinned)) })
            androidx.compose.material3.Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text(stringResource(com.mohamedadel.mytube.R.string.hist_tab_unpinned)) })
        }
        Spacer(Modifier.height(8.dp))
        val isEmpty = paged.itemCount == 0 && paged.loadState.refresh !is androidx.paging.LoadState.Loading
        if (paged.loadState.refresh is androidx.paging.LoadState.Loading && paged.itemCount == 0) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(48.dp))
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (paged.loadState.refresh is androidx.paging.LoadState.Error && paged.itemCount == 0) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(48.dp))
                Text(stringResource(com.mohamedadel.mytube.R.string.hist_load_fail), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedButton(onClick = { paged.retry() }) { Text(stringResource(com.mohamedadel.mytube.R.string.retry)) }
            }
        } else if (isEmpty) {
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
                items(paged.itemCount, key = { i -> paged.peek(i)?.videoKey ?: "idx-$i" }) { i ->
                    val h = paged[i] ?: return@items
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
                        isFav = isFav(v),
                        onFav = onFav,
                        onRemove = { confirmRow = it },
                        pinBadge = badge,
                        pinning = pinningKeys.contains(h.videoKey)
                    )
                }
                if (paged.loadState.append is androidx.paging.LoadState.Loading) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(8.dp))
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
