package com.mohamedadel.mytube.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.unified.SearchSource
import com.mohamedadel.mytube.data.unified.SourceResult
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.common.DraggableTitle
import com.mohamedadel.mytube.util.formatDuration

/**
 * Unified results (home-redesign Phase 1, 2026-11-12): Netflix-style sections.
 * Each source renders independently: loading shimmer while pending, retry on error,
 * horizontal cards on success. "Show more" expands one source to a full vertical list.
 */

// 200dp horizontal card: 16:9 thumb + draggable title + channel line.
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SectionCard(
    v: VideoItem,
    onClick: () -> Unit,
    onLongClick: (VideoItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(200.dp)
            .combinedClickable(onClick = onClick, onLongClick = { onLongClick(v) })
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            AsyncImage(
                model = v.thumbnail, contentDescription = v.title,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
            )
            v.durationSec?.takeIf { it > 0 }?.let { dur ->
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = com.mohamedadel.mytube.ui.theme.Scrim
                ) {
                    Text(
                        text = formatDuration(dur),
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        DraggableTitle(
            text = v.title ?: "",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        )
        if (!v.channel.isNullOrBlank()) {
            Text(
                text = v.channel ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun SectionLoading() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier.width(200.dp).aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SourceSection(
    title: String,
    res: SourceResult?,
    onRetry: () -> Unit,
    onShowMore: () -> Unit,
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit
) {
    val items = res?.items ?: emptyList()
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (res != null) "$title • ${items.size}" else title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (items.size > 5) {
                TextButton(onClick = onShowMore) { Text(stringResource(R.string.show_more)) }
            }
        }
        Spacer(Modifier.height(6.dp))
        when {
            res == null -> SectionLoading()
            res.error != null && items.isEmpty() -> Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = res.error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
            }
            items.isEmpty() -> Text(
                text = "—",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            else -> LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items.take(10), key = { it.id ?: it.url ?: it.title ?: it.hashCode().toString() }) { v ->
                    SectionCard(v = v, onClick = { onPlay(v) }, onLongClick = onLongClick)
                }
            }
        }
    }
}

/**
 * Full unified result: 3 sections, or one expanded source as a vertical list
 * (rows rendered by the caller's rowContent, i.e. the standard SearchRow wiring).
 */
@Composable
fun UnifiedResults(
    yt: SourceResult?,
    sc: SourceResult?,
    ym: SourceResult?,
    onRetry: (SearchSource) -> Unit,
    expanded: SearchSource?,
    onExpand: (SearchSource) -> Unit,
    onCollapse: () -> Unit,
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit,
    rowContent: @Composable (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (expanded != null) {
        val res = when (expanded) {
            SearchSource.YOUTUBE -> yt
            SearchSource.SOUNDCLoud -> sc
            SearchSource.YTMUSIC -> ym
        }
        val title = when (expanded) {
            SearchSource.YOUTUBE -> stringResource(R.string.unified_src_youtube)
            SearchSource.SOUNDCLoud -> stringResource(R.string.unified_src_sc)
            SearchSource.YTMUSIC -> stringResource(R.string.unified_src_ytm)
        }
        Column(modifier = modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCollapse) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            val items = res?.items ?: emptyList()
            if (res == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id ?: it.url ?: it.title ?: it.hashCode().toString() }) { v ->
                        rowContent(v)
                    }
                }
            }
        }
        return
    }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item {
            SourceSection(
                title = stringResource(R.string.unified_src_youtube), res = yt,
                onRetry = { onRetry(SearchSource.YOUTUBE) },
                onShowMore = { onExpand(SearchSource.YOUTUBE) },
                onPlay = onPlay, onLongClick = onLongClick
            )
        }
        item {
            SourceSection(
                title = stringResource(R.string.unified_src_sc), res = sc,
                onRetry = { onRetry(SearchSource.SOUNDCLoud) },
                onShowMore = { onExpand(SearchSource.SOUNDCLoud) },
                onPlay = onPlay, onLongClick = onLongClick
            )
        }
        item {
            SourceSection(
                title = stringResource(R.string.unified_src_ytm), res = ym,
                onRetry = { onRetry(SearchSource.YTMUSIC) },
                onShowMore = { onExpand(SearchSource.YTMUSIC) },
                onPlay = onPlay, onLongClick = onLongClick
            )
        }
    }
}
