package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.youtube.RemotePlaylist
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.results.PlaylistCard
import com.mohamedadel.mytube.ui.results.SearchRow
import com.mohamedadel.mytube.ui.search.SearchScreen
import com.mohamedadel.mytube.util.TasteProfile

// screens-fix Library P2 (method-size, 2026-11-11): the whole music-tab branch lived
// inline in MyTubeApp's Scaffold content (one 64KB+ method) — extracted here verbatim.
// All state lives in MainActivity; this takes reads + mutation callbacks.
@Composable
fun MusicTab(
    scQuery: String,
    scLoading: Boolean,
    recentMusicSearches: List<String>,
    scSource: Int,
    scKind: String,
    scResults: List<VideoItem>,
    scError: String?,
    scSuggestion: String?,
    scCorrected: Boolean,
    remotePlaylists: List<RemotePlaylist>,
    scHasMore: Boolean,
    scLoadingMore: Boolean,
    tasteProfile: TasteProfile?,
    tasteMix: List<VideoItem>,
    tasteMixTerm: String?,
    tasteMixLoading: Boolean,
    playingUrl: String?,
    isPlaying: Boolean,
    favoriteSet: Set<String>,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    progressOf: (String) -> Float? = { null },
    scListState: LazyListState,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClose: () -> Unit,
    onClear: () -> Unit,
    onPickSuggestion: (String) -> Unit,
    onClearRecents: () -> Unit,
    onVoice: () -> Unit,
    onSourceSelect: (Int) -> Unit,
    onKindSelect: (String) -> Unit,
    onTerm: (String) -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    onPlayMix: (VideoItem) -> Unit = onPlayVideo,
    onOpenChannel: (String) -> Unit,
    onOptions: (VideoItem) -> Unit,
    onToggleFav: (VideoItem) -> Unit,
    onOpenRemotePlaylist: (RemotePlaylist) -> Unit,
    onLoadMore: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SearchScreen(
            query = scQuery,
            loading = scLoading,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onClose = onClose,
            onClear = onClear,
            showDropdown = true,
            dropRecents = if (scQuery.isBlank()) recentMusicSearches else recentMusicSearches.filter { r -> r.contains(scQuery, ignoreCase = true) },
            onPickSuggestion = onPickSuggestion,
            onClearRecents = onClearRecents,
            onVoice = onVoice,
            fillMax = false
        )
        MusicSourceSegment(
            selected = scSource,
            // screens-fix Music P2-M11: scroll to top on source change (done by caller).
            onSelect = onSourceSelect
        )
        if (scSource == 1) {
            // screens-fix Music P2-M12: albums tab shows cards only by design.
            if (scKind == "albums") {
                ScAlbumsNote()
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("songs", "albums", "artists", "playlists").forEach { k ->
                    FilterChip(
                        selected = scKind == k,
                        // screens-fix Music P2-M11: scroll to top on kind change (done by caller).
                        onClick = { onKindSelect(k) },
                        label = { Text(stringResource(when (k) { "songs" -> R.string.filter_songs; "albums" -> R.string.filter_albums; "artists" -> R.string.filter_artists; else -> R.string.filter_playlists })) }
                    )
                }
            }
        }
        // Single scroll surface: header rows + results share one LazyColumn with the
        // leftover height (weight). Siblings in a plain Column squeezed the list to ~0
        // height — the More button sat off-screen and scrolling died.
        LazyColumn(modifier = Modifier.weight(1f), state = scListState) {
            // Music home: term chips + horizontal shelves (redesigned — shelves, not a flat list).
            if (scQuery.isBlank() && scResults.isEmpty() && scError == null && (tasteProfile?.terms?.isNotEmpty() == true || tasteProfile?.picks?.isNotEmpty() == true)) {
                // screens-fix Library P2 (method-size): shelves live in MusicHome.kt now.
                MusicHomeShelves(
                    profile = tasteProfile,
                    mix = tasteMix,
                    mixTerm = tasteMixTerm,
                    mixLoading = tasteMixLoading,
                    playingUrl = playingUrl,
                    isPlaying = isPlaying,
                    onTerm = onTerm,
                    onPlay = onPlayVideo,
                    onPlayMix = onPlayMix,
                    onLongClick = onOptions,
                    onLongClickMix = { v -> if (v.isChannel) onOpenChannel(v.channelUrl ?: v.url ?: "") else onOptions(v) }
                )
            }
            if (scError != null) {
                item(key = "sc-err") {
                    Text(
                        scError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            // screens-fix Music P1-M7: correction row (mirrors main search).
            val scSugg = scSuggestion
            if (scSugg != null) {
                item(key = "sc-corr") {
                    ScCorrectionRow(scSugg, scCorrected, onSearch)
                }
            }
            if (remotePlaylists.isNotEmpty()) {
                item(key = "sc-pl-title") {
                    Text(
                        stringResource(R.string.remote_playlists_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                // screens-fix Home P0-H3: stable keys (no index prefix).
                itemsIndexed(items = remotePlaylists, key = { _, p -> "pl|" + (p.url ?: p.title ?: p.hashCode().toString()) }) { _, p ->
                    PlaylistCard(p = p, onClick = { onOpenRemotePlaylist(p) })
                }
            }
            itemsIndexed(items = scResults, key = { _, v -> v.id ?: v.url ?: v.title ?: v.hashCode().toString() }) { _, v ->
                SearchRow(
                    v = v,
                    isCurrent = playingUrl == v.url,
                    isPlaying = isPlaying && playingUrl == v.url,
                    isFav = v.favKey() in favoriteSet,
                    // channel/artist rows open the channel screen — never the player
                    onClick = { if (v.isChannel) onOpenChannel(v.channelUrl ?: v.url ?: "") else onPlayVideo(v) },
                    onLongClick = { if (v.isChannel) onOpenChannel(v.channelUrl ?: v.url ?: "") else onOptions(it) },
                    onFav = onToggleFav,
                    pinBadge = pinBadges[v.favKey()],
                    pinning = pinningKeys.contains(v.favKey()),
                    pinProgress = progressOf(v.favKey())
                )
            }
            if (scHasMore || scLoadingMore) {
                item(key = "sc-more") {
                    Box(modifier = Modifier.fillMaxWidth().padding(12.dp), contentAlignment = Alignment.Center) {
                        if (scLoadingMore) CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        else TextButton(onClick = onLoadMore) { Text(stringResource(R.string.load_more)) }
                    }
                }
            }
        }
    }
}
