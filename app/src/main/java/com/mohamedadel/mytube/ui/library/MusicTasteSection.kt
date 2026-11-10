// Idle music-tab recommendations (local taste profile): extracted from MyTubeApp
// to keep the giant when-branch under the 64KB method limit.
package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.results.SearchRow
import com.mohamedadel.mytube.util.TasteProfile
import com.mohamedadel.mytube.data.local.favKey

@Composable
fun MusicTasteSection(
    visible: Boolean,
    profile: TasteProfile?,
    mix: List<VideoItem>,
    mixTitle: String,
    mixLoading: Boolean,
    playingUrl: String?,
    isPlaying: Boolean,
    isFav: (VideoItem) -> Boolean,
    pinBadge: (VideoItem) -> String?,
    pinning: (VideoItem) -> Boolean,
    onPickTerm: (String) -> Unit,
    onPlay: (VideoItem) -> Unit,
    onOpenChannel: (String) -> Unit,
    onLongClick: (VideoItem) -> Unit,
    onFav: (VideoItem) -> Unit
) {
    if (!visible) return
    Text(
        stringResource(R.string.quick_picks),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
    )
    if (profile?.terms?.isNotEmpty() == true) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profile.terms.forEach { t ->
                FilterChip(
                    selected = false,
                    onClick = { onPickTerm(t) },
                    label = { Text(t) }
                )
            }
        }
    }
    (profile?.picks ?: emptyList()).take(5).forEach { v ->
        SearchRow(
            v = v,
            isCurrent = playingUrl == v.url,
            isPlaying = isPlaying && playingUrl == v.url,
            isFav = isFav(v),
            onClick = { onPlay(v) },
            onLongClick = { onLongClick(it) },
            onFav = onFav,
            pinBadge = pinBadge(v),
            pinning = pinning(v)
        )
    }
    if (mixLoading || mix.isNotEmpty()) {
        Text(
            mixTitle,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
        if (mixLoading && mix.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            }
        }
        mix.forEach { v ->
            SearchRow(
                v = v,
                isCurrent = playingUrl == v.url,
                isPlaying = isPlaying && playingUrl == v.url,
                isFav = isFav(v),
                onClick = { if (v.isChannel) v.channelUrl?.let { onOpenChannel(it) } ?: onOpenChannel(v.url ?: "") else onPlay(v) },
                onLongClick = { if (v.isChannel) v.channelUrl?.let { onOpenChannel(it) } else { onLongClick(it) } },
                onFav = onFav,
                pinBadge = pinBadge(v),
                pinning = pinning(v)
            )
        }
    }
}
