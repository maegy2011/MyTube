package com.mohamedadel.mytube.ui.results

import com.mohamedadel.mytube.R
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.player.EqualizerBars
import com.mohamedadel.mytube.util.formatDuration

// YouTube video card (design clone, existing data only):
// full-bleed 16:9 thumbnail + duration badge, avatar circle (channel initial),
// 2-line 14sp title, 12sp gray metadata, ⋮ menu (reuses the existing options dialog).
// onAdd == null hides nothing here: ⋮ always opens the existing long-press menu.

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SearchRow(v: VideoItem, isCurrent: Boolean, isPlaying: Boolean, onClick: () -> Unit, onAdd: ((VideoItem) -> Unit)? = null, onLongClick: (VideoItem) -> Unit = {}, isFav: Boolean = false, onFav: (VideoItem) -> Unit = {}, onRemove: ((VideoItem) -> Unit)? = null) {
    val lctx = LocalContext.current
    val meta = buildString {
        append(v.channel ?: "")
        if (v.viewCount != null && v.viewCount > 0) {
            if (isNotEmpty()) append(" • ")
            append(lctx.formatViews(v.viewCount))
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = { onLongClick(v) })
    ) {
        // 16:9 full-bleed thumbnail (edge-to-edge, no rounding — like YouTube)
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(MaterialTheme.colorScheme.surfaceContainerLow)) {
            AsyncImage(model = v.thumbnail, contentDescription = v.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (isCurrent) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    EqualizerBars(playing = isPlaying)
                }
            }
            v.durationSec?.let { dur ->
                Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp), shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.8f)) {
                    Text(text = formatDuration(dur), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
        // info row: avatar + title/meta + ⋮
        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 12.dp), verticalAlignment = Alignment.Top) {
            // channel avatar: initial letter circle (no new data — initial from the existing channel name)
            val initial = (v.channel?.trim()?.firstOrNull()?.uppercase() ?: "♪").toString()
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(initial, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = v.title ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (meta.isNotEmpty()) {
                    Text(text = meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = { onLongClick(v) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.video_options_title), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
