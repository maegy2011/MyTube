package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.NotificationItemEntity
import com.mohamedadel.mytube.data.local.NotifCenter.toVideoItem
import com.mohamedadel.mytube.ui.results.SearchRow

// Notification bell screen (2026-11-12, runtime layer): new videos from subscribed/followed
// channels (channel groups). Unread rows carry the primary-container tint; tapping an item
// opens the player for it (caller marks read). Manual refresh reuses the feed-refresh path.
@Composable
fun NotificationsScreen(
    items: List<NotificationItemEntity>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onPlay: (NotificationItemEntity) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.notif_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(12.dp))
            } else {
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Rounded.Refresh, contentDescription = stringResource(R.string.notif_refresh))
                }
            }
        }
        if (items.isEmpty() && !loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.NotificationsNone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        stringResource(R.string.notif_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items, key = { it.id }) { n ->
                    val bg = if (!n.read) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.surface
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        color = bg,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        val v = n.toVideoItem()
                        SearchRow(
                            v = v,
                            isCurrent = false,
                            isPlaying = false,
                            onClick = { onPlay(n) }
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}
