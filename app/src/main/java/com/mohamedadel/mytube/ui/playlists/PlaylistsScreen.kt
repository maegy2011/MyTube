package com.mohamedadel.mytube.ui.playlists

import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.ui.theme.Scrim
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DriveFileMove
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.local.PlaylistItemEntity
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.data.local.PlaylistWithCount
import com.mohamedadel.mytube.data.youtube.VideoItem

// ============ Playlists (Room) UI ============

private val PLAYLIST_EMOJIS = listOf("🎵", "🎬", "🔥", "❤️", "📚", "🎮", "🎧", "⭐", "🌙", "⚽", "🚗", "🍳")

// View mode by default (tap = play). Edit mode stages changes in memory;
// Save writes name + emoji + order + deletions in one transaction, Cancel discards.
@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistWithCount>,
    openId: Long?,
    items: List<PlaylistItemEntity>,
    onOpen: (Long) -> Unit,
    onCloseItems: () -> Unit,
    onPlayAll: (Long) -> Unit,
    onPlayFrom: (Int) -> Unit,
    onCreate: (String, String) -> Unit,
    onSaveEdits: (Long, String, String, String, List<Long>) -> Unit,
    onDelete: (Long) -> Unit,
    onQueueItems: (List<PlaylistItemEntity>) -> Unit = {},
    onPinItems: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit = {},
    onRemoveItems: (Long, List<Long>) -> Unit = { _, _ -> },
    onExport: (Long) -> Unit = {},
    tabSort: String = "custom",
    onTabSort: (String) -> Unit = {},
    songSort: String = "added",
    onSongSort: (String) -> Unit = {},
    onMoveItem: (Long, Long, Int) -> Unit,
    onDeleteItem: (Long, Long) -> Unit,
    onClearItems: (Long) -> Unit,
    onMoveToPlaylist: (Long, Long, Long) -> Unit = { _, _, _ -> },
    onBack: () -> Unit,
    pinBadges: Map<String, String> = emptyMap(),
    onLongClick: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = {},
    covers: Map<Long, List<String>> = emptyMap()
) {
    var nameDraft by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var createEmoji by remember { mutableStateOf("🎵") }
    var deleteTarget by remember { mutableStateOf<PlaylistWithCount?>(null) }
    // move-item target picker: which item, from the open playlist, goes where
    var moveRow by remember(openId) { mutableStateOf<PlaylistItemEntity?>(null) }
    val openPl = playlists.firstOrNull { it.id == openId }
    val openName = openPl?.name ?: ""
    var editMode by remember(openId) { mutableStateOf(false) }
    var editName by remember(openId) { mutableStateOf(openName) }
    var editEmoji by remember(openId) { mutableStateOf(openPl?.emoji ?: "🎵") }
    var editMode2 by remember(openId) { mutableStateOf(openPl?.mode ?: "music") }
    // multi-select (select several songs: queue / download / remove together)
    var selecting by remember(openId) { mutableStateOf(false) }
    var selected by remember(openId) { mutableStateOf(setOf<Long>()) }
    var showSortSongs by remember { mutableStateOf(false) }
    var showSortTabs by remember { mutableStateOf(false) }
    var staged by remember(openId) { mutableStateOf<List<PlaylistItemEntity>?>(null) }
    val viewRows = remember(items, songSort) {
        when (songSort) {
            "title" -> items.sortedBy { (it.title ?: "").lowercase() }
            "artist" -> items.sortedBy { (it.channel ?: "").lowercase() }
            "duration" -> items.sortedBy { it.durationSec ?: Long.MAX_VALUE }
            else -> items
        }
    }
    val tabList = remember(playlists, tabSort) {
        when (tabSort) {
            "name" -> playlists.sortedBy { it.name.lowercase() }
            "size" -> playlists.sortedByDescending { it.count }
            else -> playlists
        }
    }
    val rows = if (editMode) staged ?: items else viewRows
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (openId != null) onCloseItems() else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(
                    if (openId != null) stringResource(R.string.items_title_fmt, if (editMode) editName else openName) else stringResource(R.string.nav_playlists),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (openId == null) {
                    IconButton(onClick = { showSortTabs = true }) {
                        Icon(Icons.Rounded.Sort, contentDescription = stringResource(R.string.sort_playlists), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showCreate = true; createEmoji = "🎵" }) {
                        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.playlist_new), tint = MaterialTheme.colorScheme.primary)
                    }
                } else if (editMode) {
                    androidx.compose.material3.TextButton(onClick = {
                        val pid = openId
                        val stagedRows = staged ?: items
                        onSaveEdits(pid, editName.trim().ifBlank { openName }, editEmoji, editMode2, stagedRows.map { it.id })
                        editMode = false
                    }) { Text(stringResource(R.string.save)) }
                    androidx.compose.material3.TextButton(onClick = { staged = null; editMode = false }) { Text(stringResource(R.string.cancel)) }
                } else {
                    androidx.compose.material3.TextButton(onClick = {
                        editName = openName
                        editEmoji = openPl?.emoji ?: "🎵"
                        editMode2 = openPl?.mode ?: "music"
                        staged = items
                        editMode = true
                    }) { Text(stringResource(R.string.edit)) }
                    IconButton(onClick = { selecting = !selecting; if (selecting) selected = emptySet() }) {
                        Icon(Icons.Rounded.Checklist, contentDescription = stringResource(R.string.select_items), tint = if (selecting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { showSortSongs = true }) {
                        Icon(Icons.Rounded.Sort, contentDescription = stringResource(R.string.sort_songs), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { openId?.let { onExport(it) } }) {
                        Icon(Icons.Rounded.Share, contentDescription = stringResource(R.string.export_csv), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (openId == null) {
            if (playlists.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.playlists_empty), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.playlists_empty_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(tabList, key = { it.id }) { p ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        PlaylistCover(emoji = p.emoji.ifBlank { "🎵" }, thumbs = covers[p.id] ?: emptyList())
                        Spacer(Modifier.width(8.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f).clickable { onOpen(p.id) }) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(stringResource(R.string.pl_count_fmt, p.count), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onPlayAll(p.id) }) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.cd_play_playlist), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { deleteTarget = p }) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete_playlist), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            if (editMode) {
                item {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, placeholder = { Text(stringResource(R.string.playlist_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(PLAYLIST_EMOJIS) { e ->
                            val sel = e == editEmoji
                            Surface(
                                onClick = { editEmoji = e },
                                shape = MaterialTheme.shapes.small,
                                color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Text(e, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
            if (openId != null && selecting && !editMode) {
                item {
                    val sel = rows.filter { it.id in selected }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.sel_count_fmt, sel.size), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onQueueItems(sel) }, enabled = sel.isNotEmpty()) {
                            Icon(Icons.Rounded.PlaylistAdd, contentDescription = stringResource(R.string.cd_add_queue_end), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { onPinItems(sel.map { it.toVideoItem() }) }, enabled = sel.isNotEmpty()) {
                            Icon(Icons.Rounded.Download, contentDescription = stringResource(R.string.download), tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { openId?.let { onRemoveItems(it, sel.map { r -> r.id }) }; selected = emptySet() }, enabled = sel.isNotEmpty()) {
                            Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete_item), tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { selecting = false; selected = emptySet() }) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            if (rows.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.playlist_empty_items), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(selected = editMode2 == "music", onClick = { editMode2 = "music" }, label = { Text(stringResource(R.string.pl_mode_music)) })
                        FilterChip(selected = editMode2 == "video", onClick = { editMode2 = "video" }, label = { Text(stringResource(R.string.pl_mode_video)) })
                    }
                }
            }
            itemsIndexed(rows, key = { _, r -> r.id }) { idx, r ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(72.dp, 42.dp).clip(MaterialTheme.shapes.extraSmall)) {
                            AsyncImage(model = r.thumbnail, contentDescription = r.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            if ((r.durationSec ?: 0) > 0) {
                                Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(2.dp), shape = MaterialTheme.shapes.extraSmall, color = Scrim) {
                                    Text(text = com.mohamedadel.mytube.util.formatDuration(r.durationSec ?: 0), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        if (selecting && !editMode) {
                            Checkbox(
                                checked = r.id in selected,
                                onCheckedChange = { c -> selected = if (c) selected + r.id else selected - r.id }
                            )
                        }
                        Column(modifier = Modifier.weight(1f).clickable {
                            if (selecting && !editMode) selected = if (r.id in selected) selected - r.id else selected + r.id
                            else if (!editMode) onPlayFrom(idx)
                        }) {
                            Text(r.title ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(r.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            pinBadges[r.toVideoItem().favKey()]?.let { b ->
                                Text(b, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (!editMode) {
                            IconButton(onClick = { onLongClick(r.toVideoItem()) }, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.video_options_title), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            // move to another playlist (copy-then-delete = true move)
                            IconButton(onClick = { moveRow = r }) {
                                Icon(Icons.Rounded.DriveFileMove, contentDescription = stringResource(R.string.cd_move_playlist), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (editMode) {
                            IconButton(onClick = {
                                staged = staged?.toMutableList()?.also { l ->
                                    val j = idx - 1
                                    if (idx in l.indices && j in l.indices) { val t = l[idx]; l[idx] = l[j]; l[j] = t }
                                }
                            }, enabled = idx > 0) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = stringResource(R.string.move_up), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                staged = staged?.toMutableList()?.also { l ->
                                    val j = idx + 1
                                    if (idx in l.indices && j in l.indices) { val t = l[idx]; l[idx] = l[j]; l[j] = t }
                                }
                            }, enabled = idx < rows.size - 1) {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = stringResource(R.string.move_down), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { staged = staged?.filter { it.id != r.id } }) {
                                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete_item), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            if (!editMode && rows.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.rows_count_fmt, rows.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        androidx.compose.material3.TextButton(onClick = { openId?.let { onPlayAll(it) } }) { Text(stringResource(R.string.play_all)) }
                        androidx.compose.material3.TextButton(onClick = { openId?.let { onClearItems(it) } }) { Text(stringResource(R.string.clear_items)) }
                    }
                }
            }
        }
    }
    // create dialog (name + cover emoji)
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text(stringResource(R.string.playlist_new_title)) },
            text = {
                Column {
                    OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text(stringResource(R.string.playlist_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(PLAYLIST_EMOJIS) { e ->
                            Surface(
                                onClick = { createEmoji = e },
                                shape = RoundedCornerShape(12.dp),
                                color = if (e == createEmoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Text(e, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val n = nameDraft.trim()
                    if (n.isNotEmpty()) { onCreate(n, createEmoji); nameDraft = ""; showCreate = false }
                }) { Text(stringResource(R.string.create)) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showCreate = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    // move target picker: other playlists only; picking one performs the true move
    moveRow?.let { r ->
        val targets = playlists.filter { it.id != openId }
        AlertDialog(
            onDismissRequest = { moveRow = null },
            title = { Text(stringResource(R.string.move_to), maxLines = 1, overflow = TextOverflow.Ellipsis) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(r.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(8.dp))
                    if (targets.isEmpty()) {
                        Text(stringResource(R.string.move_no_target), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(targets, key = { it.id }) { p ->
                                Row(modifier = Modifier.fillMaxWidth().clickable { openId?.let { onMoveToPlaylist(it, r.id, p.id) }; moveRow = null }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.emoji.ifBlank { "🎵" }, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(36.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(p.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${p.count}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { moveRow = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    // song sort (remembered per playlist by the caller via prefs)
    if (showSortSongs) {
        AlertDialog(
            onDismissRequest = { showSortSongs = false },
            title = { Text(stringResource(R.string.sort_songs)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    listOf("added", "title", "artist", "duration").forEach { k ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSongSort(k); showSortSongs = false }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(when (k) { "title" -> R.string.sort_title; "artist" -> R.string.sort_artist; "duration" -> R.string.sort_duration; else -> R.string.sort_added }),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (songSort == k) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (songSort == k) Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showSortSongs = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    // playlists-tab sort (name / size / custom creation order; creator is n/a — all local)
    if (showSortTabs) {
        AlertDialog(
            onDismissRequest = { showSortTabs = false },
            title = { Text(stringResource(R.string.sort_playlists)) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    listOf("custom", "name", "size").forEach { k ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onTabSort(k); showSortTabs = false }.padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(when (k) { "name" -> R.string.sort_tab_name; "size" -> R.string.sort_tab_size; else -> R.string.sort_tab_custom }),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (tabSort == k) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (tabSort == k) Text("✓", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showSortTabs = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    // delete confirm
    deleteTarget?.let { p ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete_playlist_title)) },
            text = { Text(stringResource(R.string.del_playlist_confirm_fmt, p.name)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { onDelete(p.id); deleteTarget = null }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

// Pick a playlist for one video (or create a new one on the spot).
@Composable
fun PlaylistPickerDialog(
    video: VideoItem,
    playlists: List<PlaylistWithCount>,
    onCreateAndAdd: (String) -> Unit,
    onAddTo: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var nameDraft by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.picker_title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(video.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                if (playlists.isEmpty()) {
                    Text(stringResource(R.string.picker_empty_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(playlists, key = { it.id }) { p ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onAddTo(p.id) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(p.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${p.count}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text(stringResource(R.string.picker_new_hint)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                val n = nameDraft.trim()
                if (n.isNotEmpty()) { onCreateAndAdd(n); nameDraft = "" }
            }) { Text(stringResource(R.string.create_and_add)) }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

// Playlist cover: 2x2 artwork collage when 4+ thumbs, single artwork when 1-3,
// emoji fallback when empty. Derived live from songs — no stored cover, no migration.
@Composable
private fun PlaylistCover(emoji: String, thumbs: List<String>) {
    if (thumbs.isEmpty()) {
        Text(emoji, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.width(44.dp))
        return
    }
    if (thumbs.size >= 4) {
        Column(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp))) {
            Row(modifier = Modifier.weight(1f)) {
                AsyncImage(model = thumbs[0], contentDescription = null, modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
                AsyncImage(model = thumbs[1], contentDescription = null, modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
            }
            Row(modifier = Modifier.weight(1f)) {
                AsyncImage(model = thumbs[2], contentDescription = null, modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
                AsyncImage(model = thumbs[3], contentDescription = null, modifier = Modifier.weight(1f).fillMaxHeight(), contentScale = ContentScale.Crop)
            }
        }
        return
    }
    AsyncImage(model = thumbs[0], contentDescription = null, modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
}
