package com.mohamedadel.mytube.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.common.DraggableTitle

/**
 * Netflix-style home (home-redesign Phase 2, 2026-11-12):
 * Hero spotlight + horizontal rows + continue-watching. Tabs/filter UI stays
 * in HomeEmptyState; this file owns only the success-branch content layout.
 */

// Spotlight card: wide art + gradient + title + Play / Details.
@Composable
fun HeroCard(
    v: VideoItem,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        AsyncImage(
            model = v.thumbnail, contentDescription = v.title,
            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                )
            )
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            DraggableTitle(
                text = v.title ?: "",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold, color = Color.White
                )
            )
            if (!v.channel.isNullOrBlank()) {
                Text(
                    text = v.channel ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.play))
                }
                OutlinedButton(
                    onClick = onDetails,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.hero_details))
                }
            }
        }
    }
}

@Composable
private fun RowHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

// Success-branch content: Hero + continue-watching + horizontal rows + paging.
@Composable
fun HomeNetflixLayout(
    items: List<VideoItem>,
    continueItems: List<VideoItem> = emptyList(),
    onPlay: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit,
    hasMore: Boolean = false,
    loadingMore: Boolean = false,
    onShowMore: () -> Unit = {},
    // hoisted list state so pull-to-refresh keeps working in both layouts.
    listState: LazyListState? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState ?: rememberLazyListState(),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (continueItems.isNotEmpty()) {
            item(key = "cw-header") { RowHeader(stringResource(R.string.continue_watching)) }
            item(key = "cw-row") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        continueItems,
                        key = { it.id ?: it.url ?: it.title ?: it.hashCode().toString() }
                    ) { v ->
                        SectionCard(v = v, onClick = { onPlay(v) }, onLongClick = onLongClick)
                    }
                }
            }
        }
        val hero = items.firstOrNull()
        if (hero != null) {
            item(key = "hero") {
                HeroCard(
                    v = hero,
                    onPlay = { onPlay(hero) },
                    onDetails = { onLongClick(hero) }
                )
            }
        }
        val rest = if (hero != null) items.drop(1) else items
        if (rest.isNotEmpty()) {
            item(key = "rows") {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        rest,
                        key = { it.id ?: it.url ?: it.title ?: it.hashCode().toString() }
                    ) { v ->
                        SectionCard(v = v, onClick = { onPlay(v) }, onLongClick = onLongClick)
                    }
                }
            }
        }
        if (hasMore || loadingMore) {
            item(key = "home-more") {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    if (loadingMore) CircularProgressIndicator(modifier = Modifier.size(28.dp))
                    else Button(onClick = onShowMore) { Text(stringResource(R.string.show_more)) }
                }
            }
        }
    }
}
