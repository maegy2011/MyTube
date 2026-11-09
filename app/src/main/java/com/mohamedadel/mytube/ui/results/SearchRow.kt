package com.mohamedadel.mytube.ui.results

import com.mohamedadel.mytube.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.player.EqualizerBars
import com.mohamedadel.mytube.util.formatDuration
import com.mohamedadel.mytube.util.formatViews

// Result row extracted from MainActivity (split step 6, final).

@Composable
@OptIn(ExperimentalFoundationApi::class)
// onAdd == null hides the + button entirely: no dead button that promises and does nothing.
fun SearchRow(v: VideoItem, isCurrent: Boolean, isPlaying: Boolean, onClick: () -> Unit, onAdd: ((VideoItem) -> Unit)? = null, onLongClick: (VideoItem) -> Unit = {}, isFav: Boolean = false, onFav: (VideoItem) -> Unit = {}, onRemove: ((VideoItem) -> Unit)? = null) {
    val lctx = LocalContext.current
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = { onLongClick(v) }),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(132.dp, 74.dp)) {
                AsyncImage(model = v.thumbnail, contentDescription = v.title, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                // current row: animated equalizer (dances while playing, frozen while paused); others: plain play icon
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrent) {
                        EqualizerBars(playing = isPlaying)
                    } else {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(R.string.play),
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                v.durationSec?.let { dur ->
                    Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp), shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.75f)) {
                        Text(text = formatDuration(dur), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = v.title ?: "", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(text = v.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (v.viewCount != null && v.viewCount > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = lctx.formatViews(v.viewCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onClick) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.cd_play_video), tint = MaterialTheme.colorScheme.primary)
                }
                if (onRemove != null || onAdd != null) {
                    IconButton(onClick = { if (onRemove != null) onRemove(v) else onAdd?.invoke(v) }) {
                        Icon(
                            if (onRemove != null) Icons.Rounded.Clear else Icons.Rounded.Add,
                            contentDescription = if (onRemove != null) stringResource(R.string.cd_remove_history) else stringResource(R.string.cd_add_queue_end),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                IconButton(onClick = { onFav(v) }) {
                    Icon(
                        if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFav) stringResource(R.string.cd_fav_remove) else stringResource(R.string.cd_fav_add),
                        tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
