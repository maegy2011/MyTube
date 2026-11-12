package com.mohamedadel.mytube.ui.home

import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
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
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.youtube.RemotePlaylist
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.ui.results.HomeEmptyState
import com.mohamedadel.mytube.ui.results.SearchRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Extracted from MyTubeApp (VerifyError fix, 2026-11-12): the home + results
 * branches live here so the activity composable stays under ART limits.
 * Call-site lambdas are thin one-liners; bulky work is internal.
 */

// Home (trending) branch — one HomeEmptyState call.
@Composable
fun HomeBranch(
    trending: List<String>,
    trendTab: Int,
    trendingItems: List<VideoItem>,
    trendingLoading: Boolean,
    trendingError: String?,
    trendingHasMore: Boolean,
    trendingLoadingMore: Boolean,
    countryName: String,
    sectionTitle: String?,
    showMusicBadge: Boolean,
    isSuggestionsTab: Boolean,
    suggestionChips: Map<String, List<String>>,
    continueWatching: List<VideoItem>,
    continueProgress: Map<String, Float>,
    continueResumeMs: Map<String, Long>,
    trendingCached: Boolean,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float?>,
    favoriteSet: Set<String>,
    onTrendTab: (Int) -> Unit,
    onRetryTrending: () -> Unit,
    onShowMore: () -> Unit,
    onSearchQ: (String) -> Unit,
    onSuggestionQ: (String) -> Unit,
    onPlayItem: (VideoItem) -> Unit,
    onFavToggle: (VideoItem) -> Unit,
    onShowOptions: (VideoItem) -> Unit,
    onResumePlay: (VideoItem, Long) -> Unit,
    modifier: Modifier = Modifier
) {
    HomeEmptyState(
        trending = trending,
        trendTab = trendTab,
        onTrendTab = onTrendTab,
        trendingItems = trendingItems,
        trendingLoading = trendingLoading,
        trendingError = trendingError,
        onRetryTrending = onRetryTrending,
        hasMore = trendingHasMore,
        loadingMore = trendingLoadingMore,
        onShowMore = onShowMore,
        countryName = countryName,
        sectionTitle = sectionTitle,
        showMusicBadge = showMusicBadge,
        onSearch = onSearchQ,
        isSuggestionsTab = isSuggestionsTab,
        suggestionChips = suggestionChips,
        onSuggestion = onSuggestionQ,
        onPlay = onPlayItem,
        isFav = { v -> v.favKey() in favoriteSet },
        onFav = onFavToggle,
        onLongClick = onShowOptions,
        pinBadges = pinBadges,
        pinningKeys = pinningKeys,
        progressOf = { k -> pinProgress[k] },
        continueItems = continueWatching,
        continueProgress = { v -> continueProgress[v.favKey()] },
        continueResumeMs = { v -> continueResumeMs[v.favKey()] },
        onResumePlay = onResumePlay,
        isCached = trendingCached
    )
}

// Results-list branch — filter chips + playlists + correction + rows + paging.
@Composable
fun ResultsBranch(
    results: List<VideoItem>,
    playing: VideoItem?,
    isPlaying: Boolean,
    favoriteSet: Set<String>,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float?>,
    prefContentFilter: String,
    currentQuery: String,
    searchSuggestion: String?,
    searchCorrected: Boolean,
    searchError: String?,
    remotePlaylists: List<RemotePlaylist>,
    loadingMore: Boolean,
    hasMore: Boolean,
    nextPage: org.schabi.newpipe.extractor.Page?,
    prefCountry: String,
    prefs: SharedPreferences,
    scope: CoroutineScope,
    listState: LazyListState,
    setContentFilter: (String) -> Unit,
    doSearchText: (String) -> Unit,
    setResults: (List<VideoItem>) -> Unit,
    setNextPage: (org.schabi.newpipe.extractor.Page?) -> Unit,
    setHasMore: (Boolean) -> Unit,
    setLoadingMore: (Boolean) -> Unit,
    onOpenRemotePlaylist: (RemotePlaylist) -> Unit,
    onPlayItem: (VideoItem) -> Unit,
    onShowOptions: (VideoItem) -> Unit,
    onToggleFav: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
    ) {
        // content filter chips (all/videos/channels/playlists) + count
        item {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("all", "videos", "songs", "channels", "playlists", "albums").forEach { f ->
                    FilterChip(
                        selected = prefContentFilter == f,
                        onClick = {
                            setContentFilter(f)
                            try { prefs.edit().putString("pref_content_filter", f).apply() } catch (_: Exception) { }
                            if (currentQuery.isNotBlank()) doSearchText(currentQuery)
                        },
                        label = { Text(stringResource(when (f) { "videos" -> R.string.filter_videos; "songs" -> R.string.filter_songs; "channels" -> R.string.filter_channels; "playlists" -> R.string.filter_playlists; "albums" -> R.string.filter_albums; else -> R.string.filter_all })) }
                    )
                }
            }
        }
        // remote playlists (service playlists for the browsed service)
        if (remotePlaylists.isNotEmpty()) {
            item {
                Text(
                    stringResource(R.string.remote_playlists_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
            items(items = remotePlaylists, key = { p -> p.url ?: p.title ?: p.hashCode().toString() }) { p ->
                com.mohamedadel.mytube.ui.results.PlaylistCard(p = p, onClick = { onOpenRemotePlaylist(p) })
            }
        }
        // search correction (own item: never breaks the list)
        val ss = searchSuggestion
        if (ss != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { doSearchText(ss) }.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (searchCorrected) stringResource(R.string.did_you_mean_showing_fmt, ss) else stringResource(R.string.did_you_mean_fmt, ss),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        item {
            Text(stringResource(R.string.results_found_fmt, results.size), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp))
        }
        // screens-fix Home P0-H3: stable keys (no index prefix).
        itemsIndexed(items = results, key = { _, v -> v.id ?: v.url ?: v.title ?: v.hashCode().toString() }) { _, v ->
            SearchRow(
                v = v,
                isCurrent = playing?.id == v.id || playing?.url == v.url,
                isPlaying = isPlaying && (playing?.id == v.id || playing?.url == v.url),
                isFav = v.favKey() in favoriteSet,
                onClick = { onPlayItem(v) },
                onLongClick = onShowOptions,
                onFav = onToggleFav,
                pinBadge = pinBadges[v.favKey()],
                pinning = pinningKeys.contains(v.favKey()),
                pinProgress = pinProgress[v.favKey()]
            )
        }
        if (loadingMore) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
        } else if (hasMore) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    Button(onClick = {
                        if (loadingMore) return@Button
                        val pg = nextPage ?: return@Button
                        setLoadingMore(true)
                        scope.launch {
                            try {
                                val page = YouTubeRepo.search(currentQuery, pg, if (prefCountry == "global") null else prefCountry, prefContentFilter)
                                val seen = results.mapNotNull { it.url ?: it.id }.toSet()
                                val fresh = page.items.filter { (it.url ?: it.id ?: it.title) !in seen }
                                setResults(results + fresh)
                                setNextPage(page.nextPage)
                                setHasMore(page.hasNext && page.nextPage != null)
                            } catch (_: Exception) {
                                setHasMore(false)
                            } finally {
                                setLoadingMore(false)
                            }
                        }
                    }) { Text(stringResource(R.string.show_more)) }
                }
            }
        }
    }
}
