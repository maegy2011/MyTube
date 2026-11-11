package com.mohamedadel.mytube.ui.playlists

import com.mohamedadel.mytube.R
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.data.local.FavoriteEntity
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.results.SearchRow

// Favorites screen: one-tap hearts land here. Tap = play, heart = remove, trash = clear all.
@Composable
fun FavoritesScreen(
    items: List<FavoriteEntity>,
    onPlay: (VideoItem) -> Unit,
    onToggleFav: (VideoItem) -> Unit,
    onClear: () -> Unit,
    onBack: () -> Unit,
    onLongClick: (VideoItem) -> Unit = {},
    pinBadges: Map<String, String> = emptyMap(),
    pinningKeys: Set<String> = emptySet(),
    progressOf: (String) -> Float? = { null },
    onPlayAll: (() -> Unit)? = null,
    onRadio: (() -> Unit)? = null
) {
    // screens-fix Library P0-L1: destructive clear needs explicit confirmation.
    var confirmClear by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    // screens-fix Library P1-L6: in-memory search + sort (no DAO change; list is already in memory).
    var favQuery by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var favSort by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0) } // 0 newest, 1 oldest, 2 title
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(
                if (items.isNotEmpty()) stringResource(R.string.fav_title_fmt, items.size) else stringResource(R.string.nav_favorites),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (items.isNotEmpty()) {
                if (onRadio != null) {
                    IconButton(onClick = onRadio) {
                        Icon(Icons.Rounded.Radio, contentDescription = stringResource(R.string.start_radio), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                if (onPlayAll != null) {
                    IconButton(onClick = onPlayAll) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.play), tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = { confirmClear = true }) {
                    Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.clear_favorites), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (confirmClear) {
            ConfirmDeleteDialog(
                title = stringResource(R.string.clear_favorites_title),
                message = stringResource(R.string.del_fav_confirm_fmt, items.size),
                onConfirm = { confirmClear = false; onClear() },
                onDismiss = { confirmClear = false }
            )
        }
        Spacer(Modifier.height(8.dp))
        // screens-fix Library P1-L6: search field + sort chips (mirror History screen).
        androidx.compose.material3.OutlinedTextField(
            value = favQuery,
            onValueChange = { favQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.fav_search_hint)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = { if (favQuery.isNotEmpty()) IconButton(onClick = { favQuery = "" }) { Icon(Icons.Rounded.Clear, contentDescription = stringResource(R.string.clear), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) } },
            singleLine = true,
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sorts = listOf(R.string.sort_newest to 0, R.string.sort_oldest to 1, R.string.sort_title to 2)
            sorts.forEach { (label, v) ->
                androidx.compose.material3.FilterChip(selected = favSort == v, onClick = { favSort = v }, label = { Text(stringResource(label)) })
            }
        }
        Spacer(Modifier.height(4.dp))
        val shown = androidx.compose.runtime.remember(items, favQuery, favSort) {
            val q = favQuery.trim().lowercase()
            val filtered = if (q.isBlank()) items else items.filter {
                (it.title ?: "").lowercase().contains(q) || (it.channel ?: "").lowercase().contains(q)
            }
            when (favSort) {
                1 -> filtered.sortedBy { it.addedAt }
                2 -> filtered.sortedBy { (it.title ?: "").lowercase() }
                else -> filtered.sortedByDescending { it.addedAt }
            }
        }
        if (items.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(48.dp))
                Icon(Icons.Rounded.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.fav_empty), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.fav_empty_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (shown.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(48.dp))
                Text(stringResource(R.string.no_results), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(shown, key = { _, f -> f.videoKey }) { _, f ->
                    val v = f.toVideoItem()
                    SearchRow(
                        v = v,
                        isCurrent = false,
                        isPlaying = false,
                        onClick = { onPlay(v) },
                        onLongClick = { onLongClick(it) },
                        isFav = true,
                        onFav = onToggleFav,
                        pinBadge = pinBadges[v.favKey()],
                        pinning = pinningKeys.contains(v.favKey()),
                        pinProgress = progressOf(v.favKey())
                    )
                }
            }
        }
    }
}
