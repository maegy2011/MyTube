package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.ChannelGroupEntity
import com.mohamedadel.mytube.data.local.ChannelGroupMemberEntity
import com.mohamedadel.mytube.data.local.SubscriptionEntity

// Channel groups: named buckets of subscribed channels. A group's "feed" is the
// existing subscription feed filtered to its members — no new network path.
@Composable
fun GroupsScreen(
    groups: List<ChannelGroupEntity>,
    counts: Map<Long, Int>,
    onOpen: (ChannelGroupEntity) -> Unit,
    onCreate: (String) -> Unit,
    onRename: (ChannelGroupEntity, String) -> Unit,
    onDelete: (ChannelGroupEntity) -> Unit,
    onBack: () -> Unit
) {
    var createOpen by remember { mutableStateOf(false) }
    var createName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<ChannelGroupEntity?>(null) }
    var renameName by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<ChannelGroupEntity?>(null) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(stringResource(R.string.groups_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            IconButton(onClick = { createName = ""; createOpen = true }) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.group_create), tint = MaterialTheme.colorScheme.primary)
            }
        }
        if (groups.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(32.dp))
                Text(stringResource(R.string.groups_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groups, key = { it.id }) { g ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable { onOpen(g) }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(g.name, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(stringResource(R.string.groups_count_fmt, counts[g.id] ?: 0), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onOpen(g) }) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.group_open_feed), tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { renameName = g.name; renameTarget = g }) {
                                Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.rename), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { deleteTarget = g }) {
                                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
    if (createOpen) {
        NameDialog(
            title = stringResource(R.string.group_create),
            initial = createName,
            onValue = { createName = it },
            onConfirm = { val n = createName.trim(); if (n.isNotEmpty()) onCreate(n); createOpen = false },
            onDismiss = { createOpen = false }
        )
    }
    renameTarget?.let { t ->
        NameDialog(
            title = stringResource(R.string.rename),
            initial = renameName,
            onValue = { renameName = it },
            onConfirm = { val n = renameName.trim(); if (n.isNotEmpty()) onRename(t, n); renameTarget = null },
            onDismiss = { renameTarget = null }
        )
    }
    deleteTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.group_delete_title)) },
            text = { Text(stringResource(R.string.group_delete_text, t.name)) },
            confirmButton = { TextButton(onClick = { onDelete(t); deleteTarget = null }) { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun NameDialog(title: String, initial: String, onValue: (String) -> Unit, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var v by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            OutlinedTextField(
                value = v,
                onValueChange = { v = it; onValue(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = { TextButton(onClick = onConfirm, enabled = v.trim().isNotEmpty()) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

// Group detail: member channels (remove / open / feed) + add-from-subscriptions sheet.
@Composable
fun GroupDetailScreen(
    group: ChannelGroupEntity,
    members: List<ChannelGroupMemberEntity>,
    subscriptions: List<SubscriptionEntity>,
    onRemove: (String) -> Unit,
    onAdd: (String) -> Unit,
    onOpenChannel: (String) -> Unit,
    onOpenFeed: () -> Unit,
    onBack: () -> Unit
) {
    var addOpen by remember { mutableStateOf(false) }
    val memberUrls = remember(members) { members.map { it.channelUrl }.toSet() }
    val subByUrl = remember(subscriptions) { subscriptions.associateBy { it.channelUrl } }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(group.name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            IconButton(onClick = { addOpen = true }) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.group_add), tint = MaterialTheme.colorScheme.primary)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
            androidx.compose.material3.Button(onClick = onOpenFeed, enabled = members.isNotEmpty()) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.group_open_feed))
            }
        }
        if (members.isEmpty()) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.group_no_members), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(members, key = { it.id }) { m ->
                    val sub = subByUrl[m.channelUrl]
                    Row(modifier = Modifier.fillMaxWidth().clickable { onOpenChannel(m.channelUrl) }.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (!sub?.avatar.isNullOrBlank()) {
                            AsyncImage(model = sub?.avatar, contentDescription = sub?.name, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(sub?.name ?: m.channelUrl, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (sub == null) Text(stringResource(R.string.group_unsubscribed), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { onRemove(m.channelUrl) }) {
                            Icon(Icons.Rounded.RemoveCircle, contentDescription = stringResource(R.string.remove), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
    if (addOpen) {
        val candidates = remember(subscriptions, memberUrls) { subscriptions.filter { it.channelUrl !in memberUrls } }
        AlertDialog(
            onDismissRequest = { addOpen = false },
            title = { Text(stringResource(R.string.group_add), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                if (candidates.isEmpty()) {
                    Text(stringResource(R.string.group_all_added), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
                        items(candidates, key = { it.channelUrl }) { c ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onAdd(c.channelUrl) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                if (!c.avatar.isNullOrBlank()) {
                                    AsyncImage(model = c.avatar, contentDescription = c.name, modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                    Spacer(Modifier.width(10.dp))
                                }
                                Text(c.name ?: c.channelUrl, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { addOpen = false }) { Text(stringResource(R.string.done)) } },
            dismissButton = null
        )
    }
}
