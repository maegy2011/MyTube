package com.mohamedadel.mytube.ui.queue

import com.mohamedadel.mytube.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.data.youtube.VideoItem

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
                Text(stringResource(R.string.queue_title_fmt, items.size), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
            Spacer(Modifier.height(4.dp))
            if (items.isEmpty()) {
                Text(stringResource(R.string.queue_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    itemsIndexed(items, key = { idx, v -> "$idx|${v.id ?: v.url ?: v.title ?: "n/a"}" }) { idx, v ->
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("${idx + 1}.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                            Text(v.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).clickable { onPlayFrom(idx) })
                            // explicit "play from here": sequential playback starts ONLY from this button
                            IconButton(onClick = { onPlayFrom(idx) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.play_from_here), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onMove(idx, -1) }, enabled = idx > 0, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = stringResource(R.string.move_up_short), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onMove(idx, 1) }, enabled = idx < items.size - 1, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = stringResource(R.string.move_down_short), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onRemove(idx) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text(stringResource(R.string.queue_save_hint)) }, singleLine = true, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(8.dp))
                androidx.compose.material3.Button(onClick = {
                    val n = nameDraft.trim()
                    if (n.isNotEmpty() && items.isNotEmpty()) { onSave(n); nameDraft = "" }
                }) { Text(stringResource(R.string.save)) }
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
    onSubscribe: (() -> Unit)? = null,
    isSubscribed: Boolean = false,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.video_options_title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(video.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                val items = listOf(
                    stringResource(R.string.play_now) to onPlayNow,
                    stringResource(R.string.play_next_opt) to onPlayNext,
                    stringResource(R.string.cd_add_queue_end) to onQueueEnd,
                    stringResource(R.string.video_options_add_playlist) to onAddToPlaylist
                )
                if (onSubscribe != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onSubscribe).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isSubscribed) Icons.Rounded.PersonRemove else Icons.Rounded.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(if (isSubscribed) R.string.unsubscribe else R.string.subscribe),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items.forEach { (label, action) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (label) {
                            stringResource(R.string.play_now) -> Icons.Rounded.PlayArrow
                            stringResource(R.string.play_next_opt) -> Icons.Rounded.SkipNext
                            stringResource(R.string.cd_add_queue_end) -> Icons.Rounded.QueueMusic
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
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}
