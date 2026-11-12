package com.mohamedadel.mytube.ui.queue

import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.ui.common.DraggableTitle
import com.mohamedadel.mytube.data.local.favKey
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.PersonRemove
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.data.youtube.VideoItem

// Current-queue manager: reorder / remove / play-from / save-as-playlist.
// Inline queue panel: rendered IN PLACE of the suggestions/results below the video
// (no popup dialog). Same powers: reorder, remove, play-from, save as playlist, close.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueuePanel(
    items: List<VideoItem>,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onPlayFrom: (Int) -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    onOptions: (VideoItem) -> Unit = {},
    pinBadges: Map<String, String> = emptyMap(),
    pinningKeys: Set<String> = emptySet(),
    progressOf: (String) -> Float? = { null },
    // screens-fix Music P1-M4: radio state badge + stop.
    radioOn: Boolean = false,
    onStopRadio: () -> Unit = {}
    ,
    // screens-fix Library P2-L13: clear-all (swipe gestures skipped — needs gesture design).
    onClearAll: (() -> Unit)? = null,
    // Deferred item 3 (2026-11-11): swipe-to-remove re-inserts here for Undo.
    onInsert: ((Int, VideoItem) -> Unit)? = null
) {
    var nameDraft by remember { mutableStateOf("") }
    val snack = remember { SnackbarHostState() }
    val qscope = rememberCoroutineScope()
    val undoLabel = stringResource(R.string.undo)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Deferred item 2 (a11y): queue title is a heading for screen-reader navigation.
                Text(stringResource(R.string.queue_title_fmt, items.size), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f).semantics { heading() })
                // screens-fix Library P2-L13: clear-all next to dismiss.
                if (onClearAll != null && items.isNotEmpty()) {
                    androidx.compose.material3.TextButton(onClick = onClearAll) { Text(stringResource(R.string.clear)) }
                }
                androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            }
            if (radioOn) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.radio_on_badge),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.TextButton(onClick = onStopRadio) { Text(stringResource(R.string.radio_stop)) }
                }
            }
            Spacer(Modifier.height(4.dp))
            if (items.isEmpty()) {
                Text(stringResource(R.string.queue_empty), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // screens-fix appendix-4: stable keys (no index prefix).
                    // Deferred item 3: swipe-to-remove with Undo (Material3 SwipeToDismissBox).
                    itemsIndexed(items, key = { _, v -> v.favKey() }) { idx, v ->
                        // Composable context: pre-format the undo message (closures below are not composable).
                        val removedMsg = stringResource(R.string.queue_removed_fmt, v.title ?: "")
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { target ->
                                if (target != SwipeToDismissBoxValue.Settled) {
                                    val at = idx
                                    val item = v
                                    onRemove(at)
                                    qscope.launch {
                                        val r = snack.showSnackbar(
                                            message = removedMsg,
                                            actionLabel = undoLabel,
                                            withDismissAction = true
                                        )
                                        if (r == SnackbarResult.ActionPerformed) onInsert?.invoke(at, item)
                                    }
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = true,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 16.dp),
                                    contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
                                ) {
                                    Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        ) {
                        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant), verticalAlignment = Alignment.CenterVertically) {
                            Text("${idx + 1}.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                            // pin-deepfix 2026-11-11: distinctive pinned badge + live % in queue rows.
                            val qKey = v.favKey()
                            val qProg = progressOf(qKey)
                            val qBadgeText = pinBadges[qKey]
                            if (pinningKeys.contains(qKey) && qProg != null && qProg >= 0f) {
                                Text(stringResource(R.string.dl_progress_fmt, (qProg * 100).toInt()), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 4.dp))
                            } else if (qBadgeText != null) {
                                Icon(Icons.Rounded.PushPin, contentDescription = stringResource(R.string.cd_pinned_fmt, qBadgeText), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            }
                            Text("${idx + 1}.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                            DraggableTitle(text = v.title ?: "", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            // explicit "play from here": sequential playback starts ONLY from this button
                            IconButton(onClick = { onPlayFrom(idx) }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.play_from_here), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = { onMove(idx, -1) }, enabled = idx > 0, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                                Icon(Icons.Rounded.ArrowUpward, contentDescription = stringResource(R.string.move_up_short), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onMove(idx, 1) }, enabled = idx < items.size - 1, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                                Icon(Icons.Rounded.ArrowDownward, contentDescription = stringResource(R.string.move_down_short), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onRemove(idx) }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onOptions(v) }, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
                                Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.video_options_title), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                        }
                    }
                }
            }
            // Deferred item 3: undo snackbar host for swipe-to-remove.
            SnackbarHost(hostState = snack, modifier = Modifier.fillMaxWidth())
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
    onPin: (() -> Unit)? = null,
    isPinned: Boolean = false,
    isPinning: Boolean = false,
    onQuickPin: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    onStartRadio: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.video_options_title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                DraggableTitle(text = video.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                // (icon, label, action): icons bind by position, never by translated label
                val items = listOf(
                    Triple(Icons.Rounded.PlayArrow, stringResource(R.string.play_now), onPlayNow),
                    Triple(Icons.Rounded.SkipNext, stringResource(R.string.play_next_opt), onPlayNext),
                    Triple(Icons.Rounded.QueueMusic, stringResource(R.string.cd_add_queue_end), onQueueEnd),
                    Triple(Icons.AutoMirrored.Rounded.PlaylistAdd, stringResource(R.string.video_options_add_playlist), onAddToPlaylist)
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
                if (onStartRadio != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onStartRadio).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Radio, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.start_radio), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                if (onShare != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onShare).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.share), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                if (onQuickPin != null && !isPinned && !isPinning) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !isPinning, onClick = onQuickPin).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            stringResource(R.string.pin_quick),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (onPin != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !isPinning, onClick = onPin).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.PushPin,
                            contentDescription = null,
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            when {
                                isPinning -> stringResource(R.string.pinning)
                                isPinned -> stringResource(R.string.unpin_action)
                                else -> stringResource(R.string.pin_action)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items.forEach { (icon, label, action) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
