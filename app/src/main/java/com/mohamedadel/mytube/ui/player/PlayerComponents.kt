package com.mohamedadel.mytube.ui.player

import com.mohamedadel.mytube.R
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.youtube.VideoItem

// Mini player + equalizer bars extracted from MainActivity (split step 5).
// Pure UI: state in via params, actions out via callbacks.

@Composable
fun EqualizerBars(playing: Boolean) {
    // animated sound bars while playing, frozen mid-height while paused
    if (!playing) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(3) {
                Box(modifier = Modifier.width(3.dp).height(8.dp).background(Color.White, RoundedCornerShape(1.dp)))
            }
        }
        return
    }
    val t = rememberInfiniteTransition(label = "eq")
    val h1 by t.animateFloat(4f, 16f, infiniteRepeatable(tween(380), RepeatMode.Reverse), label = "eq1")
    val h2 by t.animateFloat(14f, 5f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "eq2")
    val h3 by t.animateFloat(6f, 13f, infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "eq3")
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(h1, h2, h3).forEach { h ->
            Box(modifier = Modifier.width(3.dp).height(h.dp).background(Color.White, RoundedCornerShape(1.dp)))
        }
    }
}

// R1: queue navigation (next/prev, disabled at the ends) + swipe-to-dismiss (same closePlayer()).
@Composable
fun MiniPlayer(
    video: VideoItem,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onToggle: () -> Unit,
    onTap: () -> Unit,
    onClose: () -> Unit = {},
    onNext: (() -> Unit)? = null,
    onPrev: (() -> Unit)? = null,
    titleOverride: String? = null,
    hasNext: Boolean = false,
    hasPrev: Boolean = false,
    castAvailable: Boolean = false,
    // 2026-11-12 (runtime fix): queue reachable straight from the mini-player
    // (was only inside the full player) — remove/reorder/clear live in QueuePanel.
    onQueue: (() -> Unit)? = null
) {
    val dismissState = androidx.compose.material3.rememberSwipeToDismissBoxState()
    val dismissed = dismissState.currentValue != androidx.compose.material3.SwipeToDismissBoxValue.Settled
    androidx.compose.runtime.LaunchedEffect(dismissed) { if (dismissed) onClose() }
    androidx.compose.material3.SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Rounded.Clear, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 16.dp))
            }
        },
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true
    ) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onTap).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(model = video.thumbnail, contentDescription = video.title, modifier = Modifier.size(48.dp, 32.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = titleOverride ?: video.title ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = video.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                EqualizerBars(playing = isPlaying)
                Spacer(Modifier.width(4.dp))
                if (onQueue != null) {
                    IconButton(onClick = onQueue) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = stringResource(R.string.cd_manage_queue), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    }
                }
                if (onPrev != null) {
                    IconButton(onClick = onPrev, enabled = hasPrev) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = stringResource(R.string.prev), tint = if (hasPrev) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(22.dp))
                    }
                }
                IconButton(onClick = onToggle) {
                    Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (isPlaying) stringResource(R.string.sleep_pause) else stringResource(R.string.play), tint = MaterialTheme.colorScheme.primary)
                }
                if (onNext != null) {
                    IconButton(onClick = onNext, enabled = hasNext) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = stringResource(R.string.next), tint = if (hasNext) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f), modifier = Modifier.size(22.dp))
                    }
                }
                if (castAvailable) {
                    androidx.compose.ui.viewinterop.AndroidView(
                        factory = { c ->
                            androidx.mediarouter.app.MediaRouteButton(c).apply {
                                try { com.mohamedadel.mytube.playback.CastManager.selector()?.let { routeSelector = it } } catch (_: Exception) { }
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Clear, contentDescription = stringResource(R.string.cd_close_player), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            if (durationMs > 0) {
                LinearProgressIndicator(
                    progress = { (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }
        }
    }
    }
}
// Sleep timer picker: arms playback auto-pause after N minutes (or disarms).
@Composable
fun SleepTimerDialog(
    currentLabel: String?,
    onPick: (Int?) -> Unit,
    onDismiss: () -> Unit,
    onEndOfTrack: (() -> Unit)? = null
) {
    val options = listOf(5, 10, 15, 30, 45, 60)
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (currentLabel != null) stringResource(R.string.sleep_running_fmt, currentLabel) else stringResource(R.string.sleep_timer), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.sleep_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                options.forEach { m ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(role = androidx.compose.ui.semantics.Role.Button) { onPick(m) }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.sleep_in_fmt, m), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                if (onEndOfTrack != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(role = androidx.compose.ui.semantics.Role.Button) { onEndOfTrack() }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.sleep_track), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                if (currentLabel != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(role = androidx.compose.ui.semantics.Role.Button) { onPick(null) }.padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Clear, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.sleep_stop), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}
