package com.mohamedadel.mytube.ui.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onSaveEdits: (Long, String, String, List<Long>) -> Unit,
    onDelete: (Long) -> Unit,
    onMoveItem: (Long, Long, Int) -> Unit,
    onDeleteItem: (Long, Long) -> Unit,
    onClearItems: (Long) -> Unit,
    onBack: () -> Unit
) {
    var nameDraft by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var createEmoji by remember { mutableStateOf("🎵") }
    var deleteTarget by remember { mutableStateOf<PlaylistWithCount?>(null) }
    val openPl = playlists.firstOrNull { it.id == openId }
    val openName = openPl?.name ?: ""
    var editMode by remember(openId) { mutableStateOf(false) }
    var editName by remember(openId) { mutableStateOf(openName) }
    var editEmoji by remember(openId) { mutableStateOf(openPl?.emoji ?: "🎵") }
    var staged by remember(openId) { mutableStateOf<List<PlaylistItemEntity>?>(null) }
    val rows = if (editMode) staged ?: items else items
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (openId != null) onCloseItems() else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Text(
                    if (openId != null) "بنود: ${if (editMode) editName else openName}" else "قوائم التشغيل",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (openId == null) {
                    IconButton(onClick = { showCreate = true; createEmoji = "🎵" }) {
                        Icon(Icons.Rounded.Add, contentDescription = "قائمة جديدة", tint = MaterialTheme.colorScheme.primary)
                    }
                } else if (editMode) {
                    androidx.compose.material3.TextButton(onClick = {
                        val pid = openId
                        val stagedRows = staged ?: items
                        onSaveEdits(pid, editName.trim().ifBlank { openName }, editEmoji, stagedRows.map { it.id })
                        editMode = false
                    }) { Text("حفظ") }
                    androidx.compose.material3.TextButton(onClick = { staged = null; editMode = false }) { Text("إلغاء") }
                } else {
                    androidx.compose.material3.TextButton(onClick = {
                        editName = openName
                        editEmoji = openPl?.emoji ?: "🎵"
                        staged = items
                        editMode = true
                    }) { Text("تعديل") }
                }
            }
        }
        if (openId == null) {
            if (playlists.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("لا توجد قوائم بعد", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("احفظ نتائج البحث أو أي فيديو في قائمة تشغيل", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(playlists, key = { it.id }) { p ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(p.emoji.ifBlank { "🎵" }, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.width(44.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f).clickable { onOpen(p.id) }) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${p.count} عنصر", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onPlayAll(p.id) }) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "تشغيل القائمة", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { deleteTarget = p }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "حذف القائمة", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            if (editMode) {
                item {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, placeholder = { Text("اسم القائمة") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        items(PLAYLIST_EMOJIS) { e ->
                            val sel = e == editEmoji
                            Surface(
                                onClick = { editEmoji = e },
                                shape = RoundedCornerShape(12.dp),
                                color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                            ) {
                                Text(e, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
            if (rows.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("القائمة فارغة — أضف فيديوهات من نتائج البحث", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        AsyncImage(model = r.thumbnail, contentDescription = r.title, modifier = Modifier.size(72.dp, 42.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f).clickable { if (!editMode) onPlayFrom(idx) }) {
                            Text(r.title ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(r.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (editMode) {
                            IconButton(onClick = {
                                staged = staged?.toMutableList()?.also { l ->
                                    val j = idx - 1
                                    if (idx in l.indices && j in l.indices) { val t = l[idx]; l[idx] = l[j]; l[j] = t }
                                }
                            }, enabled = idx > 0) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = "تحريك لأعلى", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = {
                                staged = staged?.toMutableList()?.also { l ->
                                    val j = idx + 1
                                    if (idx in l.indices && j in l.indices) { val t = l[idx]; l[idx] = l[j]; l[j] = t }
                                }
                            }, enabled = idx < rows.size - 1) {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = "تحريك لأسفل", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { staged = staged?.filter { it.id != r.id } }) {
                                Icon(Icons.Rounded.Delete, contentDescription = "حذف البند", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            if (!editMode && rows.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${rows.size} عنصر", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        androidx.compose.material3.TextButton(onClick = { openId?.let { onPlayAll(it) } }) { Text("تشغيل الكل") }
                        androidx.compose.material3.TextButton(onClick = { openId?.let { onClearItems(it) } }) { Text("تفريغ") }
                    }
                }
            }
        }
    }
    // create dialog (name + cover emoji)
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("قائمة تشغيل جديدة") },
            text = {
                Column {
                    OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text("اسم القائمة") }, singleLine = true, modifier = Modifier.fillMaxWidth())
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
                }) { Text("إنشاء") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showCreate = false }) { Text("إلغاء") } }
        )
    }
    // delete confirm
    deleteTarget?.let { p ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("حذف القائمة؟") },
            text = { Text("سيتم حذف \"${p.name}\" نهائيًا مع كل بنودها.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { onDelete(p.id); deleteTarget = null }) { Text("حذف") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { deleteTarget = null }) { Text("إلغاء") } }
        )
    }
}

// Current-queue manager: reorder / remove / play-from / save-as-playlist.
// Inline queue panel: rendered IN PLACE of the suggestions/results below the video
// (no popup dialog). Same powers: reorder, remove, play-from, save as playlist, close.
@Composable
fun QueuePanel(
    items: List<VideoItem>,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onPlayFrom: (Int) -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameDraft by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("القائمة الحالية (${items.size})", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(onClick = onDismiss) { Text("إغلاق") }
            }
            Spacer(Modifier.height(4.dp))
            if (items.isEmpty()) {
                Text("لا توجد عناصر في القائمة الحالية", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    itemsIndexed(items, key = { idx, v -> "$idx|${v.id ?: v.url ?: v.title ?: "n/a"}" }) { idx, v ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("${idx + 1}.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                            Text(v.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).clickable { onPlayFrom(idx) })
                            // explicit "play from here": sequential playback starts ONLY from this button
                            IconButton(onClick = { onPlayFrom(idx) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = "تشغيل من هنا", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onMove(idx, -1) }, enabled = idx > 0, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = "لأعلى", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onMove(idx, 1) }, enabled = idx < items.size - 1, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = "لأسفل", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onRemove(idx) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text("احفظ القائمة باسم…") }, singleLine = true, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.Button(onClick = {
                    val n = nameDraft.trim()
                    if (n.isNotEmpty() && items.isNotEmpty()) { onSave(n); nameDraft = "" }
                }) { Text("حفظ") }
            }
        }
    }
}

// Long-press menu for a video row.
@Composable
fun VideoOptionsDialog(
    video: VideoItem,
    onPlayNow: () -> Unit,
    onPlayNext: () -> Unit,
    onQueueEnd: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("خيارات الفيديو", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(video.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                val items = listOf(
                    "تشغيل الآن" to onPlayNow,
                    "تشغيل التالي" to onPlayNext,
                    "إضافة لآخر القائمة الحالية" to onQueueEnd,
                    "إضافة إلى قائمة تشغيل…" to onAddToPlaylist
                )
                items.forEach { (label, action) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (label) {
                            "تشغيل الآن" -> Icons.Rounded.PlayArrow
                            "تشغيل التالي" -> Icons.Rounded.SkipNext
                            "إضافة لآخر القائمة الحالية" -> Icons.Rounded.QueueMusic
                            else -> Icons.Rounded.PlaylistAdd
                        }
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
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
        title = { Text("إضافة إلى قائمة تشغيل", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(video.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                if (playlists.isEmpty()) {
                    Text("لا توجد قوائم بعد — أنشئ واحدة بالاسم أدناه", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text("أو قائمة جديدة باسم…") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                val n = nameDraft.trim()
                if (n.isNotEmpty()) { onCreateAndAdd(n); nameDraft = "" }
            }) { Text("إنشاء وإضافة") }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
