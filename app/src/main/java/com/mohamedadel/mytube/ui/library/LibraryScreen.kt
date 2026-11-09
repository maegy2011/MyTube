package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R

// YouTube "Library" tab: groups the three existing collections (design only —
// history, favorites and playlists screens are reused unchanged).
@Composable
fun LibraryScreen(
    onHistory: () -> Unit,
    onFavorites: () -> Unit,
    onPlaylists: () -> Unit,
    subscriptions: List<com.mohamedadel.mytube.data.local.SubscriptionEntity> = emptyList(),
    onOpenFeed: () -> Unit = {},
    onUnsubscribe: (String) -> Unit = {},
    onImportSubs: () -> Unit = {},
    // R3: per-channel counts from the live in-memory feed (zero DB/network) + tap-to-filter.
    feedCounts: Map<String, Int> = emptyMap(),
    onOpenChannel: (String) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        item {
            Text(
                stringResource(R.string.lib_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        item {
            LibraryRow(
                icon = { Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) },
                label = stringResource(R.string.nav_history),
                onClick = onHistory
            )
        }
        item {
            LibraryRow(
                icon = { Icon(Icons.Rounded.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) },
                label = stringResource(R.string.nav_favorites),
                onClick = onFavorites
            )
        }
        item {
            LibraryRow(
                icon = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) },
                label = stringResource(R.string.nav_playlists),
                onClick = onPlaylists
            )
        }
        // subscriptions (local-only): channel rows + import + feed entry
        item {
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.subscriptions),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        item {
            LibraryRow(
                icon = { Icon(Icons.Rounded.RssFeed, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) },
                label = stringResource(R.string.feed_title) + " (${subscriptions.size})",
                onClick = onOpenFeed
            )
        }
        item {
            LibraryRow(
                icon = { Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) },
                label = stringResource(R.string.import_subs),
                onClick = onImportSubs
            )
        }
        items(subscriptions, key = { it.channelUrl }) { sub ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onOpenChannel(sub.channelUrl) }.padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!sub.avatar.isNullOrBlank()) {
                    coil.compose.AsyncImage(model = sub.avatar, contentDescription = sub.name, modifier = Modifier.size(36.dp).clip(androidx.compose.foundation.shape.CircleShape), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(12.dp))
                androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                    Text(sub.name ?: sub.channelUrl, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.sub_badge), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 1)
                    }
                    val n = feedCounts[sub.channelUrl]
                    Text(
                        if (n != null) stringResource(R.string.lib_channel_count_fmt, n) else stringResource(R.string.lib_channel_count_none),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1
                    )
                }
                androidx.compose.material3.TextButton(onClick = { onUnsubscribe(sub.channelUrl) }) { Text(stringResource(R.string.unsubscribe)) }
            }
        }
    }
}

@Composable
private fun LibraryRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        }
    }
}
