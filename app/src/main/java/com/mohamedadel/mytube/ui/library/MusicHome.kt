// Music home (redesigned 11 Nov 2026): YouTube-Music-style shelves instead of a flat
// text list. Segmented source switch + horizontal artwork cards for picks and mix.
package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.VideoItem

// Segmented SoundCloud | YouTube Music switch: one pill, two halves, no chip sprawl.
@Composable
fun MusicSourceSegment(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val labels = listOf(stringResource(R.string.sc_source_sc), stringResource(R.string.sc_source_ytm))
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            labels.forEachIndexed { i, label ->
                val sel = selected == i
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { if (!sel) onSelect(i) }
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (sel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// One artwork card: 140dp thumb + duration badge, title + channel below.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicCard(
    v: VideoItem,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier.width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onPlay, onLongClick = onLongClick)
    ) {
        Box(
            modifier = Modifier.size(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            if (!v.thumbnail.isNullOrBlank()) {
                AsyncImage(model = v.thumbnail, contentDescription = v.title, modifier = Modifier.size(140.dp), contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
            }
            if (isPlaying) {
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                    Text("▶", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            val dur = v.durationSec ?: 0L
            if (dur > 0) {
                Surface(color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.75f), shape = RoundedCornerShape(4.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)) {
                    Text(
                        "%d:%02d".format(dur / 60, dur % 60),
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(v.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (!v.channel.isNullOrBlank()) {
            Text(v.channel ?: "", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// Horizontal shelf: section title + scrollable cards.
@Composable
fun MusicShelf(
    title: String,
    items: List<VideoItem>,
    playingUrl: String?,
    isPlaying: Boolean,
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit
) {
    if (items.isEmpty()) return
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = items, key = { v -> (v.url ?: v.id ?: v.title ?: "?") + "@" + title.hashCode() }) { v ->
            MusicCard(v = v, isPlaying = isPlaying && playingUrl == v.url, onPlay = { onPlay(v) }, onLongClick = { onLongClick(v) })
        }
    }
    Spacer(Modifier.height(8.dp))
}
