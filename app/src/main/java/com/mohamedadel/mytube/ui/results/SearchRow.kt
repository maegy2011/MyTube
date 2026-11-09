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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
import com.mohamedadel.mytube.util.formatViews

// YouTube video card (design clone, existing data only):
// full-bleed 16:9 thumbnail + duration badge, avatar circle (channel initial),
// 2-line 14sp title, 12sp gray metadata, ⋮ menu (reuses the existing options dialog).
// onAdd == null hides nothing here: ⋮ always opens the existing long-press menu.

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SearchRow(v: VideoItem, isCurrent: Boolean, isPlaying: Boolean, onClick: () -> Unit, onAdd: ((VideoItem) -> Unit)? = null, onLongClick: (VideoItem) -> Unit = {}, isFav: Boolean = false, onFav: (VideoItem) -> Unit = {}, onRemove: ((VideoItem) -> Unit)? = null, pinBadge: String? = null, pinning: Boolean = false) {
    val lctx = LocalContext.current
    val metaHasCheck = v.verified
    val meta = buildString {
        append(v.channel ?: "")
        if (v.viewCount != null && v.viewCount > 0) {
            if (isNotEmpty()) append(" • ")
            append(v.viewCount.let { lctx.formatViews(it) })
        }
        if (!v.uploadDate.isNullOrBlank()) {
            if (isNotEmpty()) append(" • ")
            append(v.uploadDate)
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
            if (v.isShort) {
                Surface(modifier = Modifier.align(Alignment.BottomStart).padding(8.dp), shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.8f)) {
                    Text(text = "Shorts", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            v.durationSec?.takeIf { it > 0 }?.let { dur ->
                Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp), shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.8f)) {
                    Text(text = formatDuration(dur), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
        // info row: avatar + title/meta + ⋮
        Row(modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 12.dp), verticalAlignment = Alignment.Top) {
            // channel avatar: real uploader image when available, initial-letter fallback
            if (!v.channelAvatar.isNullOrBlank()) {
                AsyncImage(model = v.channelAvatar, contentDescription = v.channel, modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            } else {
                val initial = (v.channel?.trim()?.firstOrNull()?.uppercase() ?: "♪").toString()
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initial, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, maxLines = 1)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = v.title ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (meta.isNotEmpty() || metaHasCheck) {
                    Text(
                        text = buildAnnotatedString {
                            append(meta)
                            if (metaHasCheck) {
                                if (length > 0) append(" ")
                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) { append("✓") }
                            }
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Phase 5: offline-pin badge (📌 quality • size) or in-progress state
                if (pinning) {
                    Spacer(Modifier.height(4.dp))
                    Text(text = stringResource(R.string.pinning), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary, maxLines = 1)
                } else if (!pinBadge.isNullOrBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(text = pinBadge, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            IconButton(onClick = { onFav(v) }, modifier = Modifier.size(36.dp)) {
                Icon(
                    if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = stringResource(if (isFav) R.string.cd_fav_remove else R.string.cd_fav_add),
                    tint = if (isFav) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onLongClick(v) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.video_options_title), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
