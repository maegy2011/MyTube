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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.ui.theme.ScrimSoft
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.youtube.VideoItem

// screens-fix Music P1-M7/P2-M12 (2026-11-11): extracted from MyTubeApp to keep the
// giant when-branch under the 64KB method limit — correction row + albums note.
@Composable
fun ScCorrectionRow(sugg: String, corrected: Boolean, onPick: (String) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth().clickable { onPick(sugg) }.padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            if (corrected) stringResource(R.string.did_you_mean_showing_fmt, sugg) else stringResource(R.string.did_you_mean_fmt, sugg),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ScAlbumsNote() {
    Text(
        stringResource(R.string.sc_albums_note),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
}

// screens-fix Library P2 (method-size, 2026-11-11): the music home shelves lived inline
// in MyTubeApp's giant when-branch — moved here as a LazyListScope extension.
// NOTE: intentionally NOT @Composable — LazyColumn content scope is a plain DSL
// (only item{} bodies are composable). The annotation broke the call site.
fun androidx.compose.foundation.lazy.LazyListScope.MusicHomeShelves(
    profile: com.mohamedadel.mytube.util.TasteProfile?,
    mix: List<VideoItem>,
    mixTerm: String?,
    mixLoading: Boolean,
    playingUrl: String?,
    isPlaying: Boolean,
    onTerm: (String) -> Unit,
    onPlay: (VideoItem) -> Unit,
    onPlayMix: (VideoItem) -> Unit,
    onLongClick: (VideoItem) -> Unit,
    onLongClickMix: (VideoItem) -> Unit
) {
    if (profile?.terms?.isNotEmpty() == true) {
        item(key = "sc-terms") {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                profile.terms.forEach { t ->
                    androidx.compose.material3.FilterChip(
                        selected = false,
                        onClick = { onTerm(t) },
                        label = { Text(t) }
                    )
                }
            }
        }
    }
    if (profile?.picks?.isNotEmpty() == true) {
        item(key = "sc-picks") {
            MusicShelf(
                title = stringResource(R.string.quick_picks),
                // screens-fix Music P1-M6: dedupe by canonical key.
                items = (profile.picks).distinctBy { it.favKey() }.take(8),
                playingUrl = playingUrl,
                isPlaying = isPlaying,
                onPlay = onPlay,
                onLongClick = onLongClick
            )
        }
    }
    if (mixLoading || mix.isNotEmpty()) {
        item(key = "sc-mix") {
            if (mixLoading && mix.isEmpty()) {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            } else {
                val lctx = androidx.compose.ui.platform.LocalContext.current
                val mixTitle = try {
                    lctx.getString(R.string.taste_mix_fmt, mixTerm ?: "")
                } catch (_: Exception) { mixTerm ?: "" }
                MusicShelf(
                    title = mixTitle,
                    items = mix,
                    playingUrl = playingUrl,
                    isPlaying = isPlaying,
                    onPlay = onPlayMix,
                    onLongClick = onLongClickMix
                )
            }
        }
    }
}

// Segmented SoundCloud | YouTube Music switch: one pill, two halves, no chip sprawl.
@Composable
fun MusicSourceSegment(
    selected: Int,
    onSelect: (Int) -> Unit
) {
    val labels = listOf(stringResource(R.string.sc_source_sc), stringResource(R.string.sc_source_ytm))
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
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
                        // screens-fix Music P2-M10: tab semantics (explicit receiver: param shadows name).
                        .semantics { this.selected = sel },
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
        // screens-fix Music P2-M10: card semantics for TalkBack.
        modifier = Modifier.width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onPlay, onLongClick = onLongClick, onClickLabel = v.title ?: "")
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
                // screens-fix Music P2-M10: Icon instead of literal "▶" (was read aloud).
                Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopStart).padding(6.dp)) {
                    // Decorative: the card already carries onClickLabel=title, so the badge stays silent.
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp).size(16.dp))
                }
            }
            val dur = v.durationSec ?: 0L
            if (dur > 0) {
                Surface(color = ScrimSoft, shape = MaterialTheme.shapes.extraSmall, modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)) {
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
        // screens-fix Music P1-M6: stable canonical key (was title-hash-suffixed per shelf).
        items(items = items, key = { v -> v.favKey() }) { v ->
            MusicCard(v = v, isPlaying = isPlaying && playingUrl == v.url, onPlay = { onPlay(v) }, onLongClick = { onLongClick(v) })
        }
    }
    Spacer(Modifier.height(8.dp))
}
