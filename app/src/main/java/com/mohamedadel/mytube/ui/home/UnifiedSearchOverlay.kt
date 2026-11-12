package com.mohamedadel.mytube.ui.home

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.unified.SearchSource
import com.mohamedadel.mytube.data.unified.SourceResult
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.results.SearchRow

/**
 * Extracted from MyTubeApp (VerifyError fix, 2026-11-12): the whole showSearch
 * overlay lives here so the activity composable stays under ART limits.
 * All routing/prefs logic is internal; the call site passes thin one-liners.
 */
@Composable
fun UnifiedSearchOverlay(
    query: String,
    loading: Boolean,
    scLoading: Boolean,
    unifiedLoading: Boolean,
    unifiedSource: SearchSource?,
    uYT: SourceResult?,
    uSC: SourceResult?,
    uYM: SourceResult?,
    unifiedExpanded: SearchSource?,
    suggestions: List<String>,
    showSuggestions: Boolean,
    recentSearches: List<String>,
    recentMusicSearches: List<String>,
    setRecentSearches: (List<String>) -> Unit,
    setRecentMusicSearches: (List<String>) -> Unit,
    prefs: SharedPreferences,
    playing: VideoItem?,
    isPlaying: Boolean,
    favoriteSet: Set<String>,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float?>,
    onQueryChange: (String) -> Unit,
    onSelectSource: (SearchSource?) -> Unit,
    doSearch: (String) -> Unit,
    doScSearch: (String, Int) -> Unit,
    doUnifiedSearch: (String) -> Unit,
    setShowSearch: (Boolean) -> Unit,
    setShowSC: (Boolean) -> Unit,
    setScQuery: (String) -> Unit,
    setShowSuggestions: (Boolean) -> Unit,
    clearUnifiedResults: () -> Unit,
    retryUnifiedSource: (String, SearchSource) -> Unit,
    onVoice: () -> Unit,
    onPlayItem: (VideoItem) -> Unit,
    onShowOptions: (VideoItem) -> Unit,
    onToggleFav: (VideoItem) -> Unit,
    onExpand: (SearchSource?) -> Unit,
    modifier: Modifier = Modifier
) {
    // home-redesign Phase 1: unified bar (chips + recents per source).
    // All-mode searches run in parallel and render sections below the bar;
    // single-source chips keep today's flows (close + existing search).
    val mainRecents = unifiedSource == null || unifiedSource == SearchSource.YOUTUBE
    val uRecentsBase = if (mainRecents) recentSearches else recentMusicSearches
    val uDropRecents = if (query.isBlank()) uRecentsBase
    else uRecentsBase.filter { r -> r.contains(query, ignoreCase = true) && !suggestions.contains(r) }
    fun routeSearch(q: String, closeAfter: Boolean) {
        when (unifiedSource) {
            null -> { doUnifiedSearch(q); setShowSuggestions(false) }
            SearchSource.YOUTUBE -> { doSearch(q); if (closeAfter) setShowSearch(false) }
            SearchSource.SOUNDCLoud -> { setScQuery(q); doScSearch(q, 0); if (closeAfter) { setShowSearch(false); setShowSC(true) } }
            SearchSource.YTMUSIC -> { setScQuery(q); doScSearch(q, 1); if (closeAfter) { setShowSearch(false); setShowSC(true) } }
        }
    }
    Column(modifier = modifier.fillMaxSize()) {
        UnifiedSearchBar(
            query = query,
            loading = loading || scLoading || unifiedLoading,
            selected = unifiedSource,
            onSelectSource = { onSelectSource(it); clearUnifiedResults() },
            onQueryChange = onQueryChange,
            onSearch = { q -> routeSearch(q, closeAfter = true) },
            onClose = { setShowSearch(false); clearUnifiedResults() },
            onClear = { onQueryChange(""); setShowSuggestions(false); clearUnifiedResults() },
            dropRecents = uDropRecents,
            suggestions = suggestions,
            onPickSuggestion = { q ->
                onQueryChange(q); setShowSuggestions(false)
                routeSearch(q, closeAfter = true)
            },
            onVoice = onVoice,
            showDropdown = showSuggestions,
            onClearRecents = {
                try {
                    if (mainRecents) { setRecentSearches(emptyList()); prefs.edit().remove("recent_searches").apply() }
                    else { setRecentMusicSearches(emptyList()); prefs.edit().remove("recent_music_searches").apply() }
                } catch (_: Exception) { }
            },
            onDeleteRecent = { s ->
                try {
                    if (mainRecents) {
                        val next = recentSearches.filter { it != s }
                        setRecentSearches(next)
                        prefs.edit().putString("recent_searches", next.joinToString("\u001f")).apply()
                    } else {
                        val next = recentMusicSearches.filter { it != s }
                        setRecentMusicSearches(next)
                        prefs.edit().putString("recent_music_searches", next.joinToString("\u001f")).apply()
                    }
                } catch (_: Exception) { }
            }
        )
        // All-mode parallel results (sections) live under the bar; hidden until searched.
        if (unifiedSource == null && (uYT != null || uSC != null || uYM != null || unifiedLoading)) {
            UnifiedResults(
                yt = uYT, sc = uSC, ym = uYM,
                onRetry = { s -> retryUnifiedSource(query, s) },
                expanded = unifiedExpanded,
                onExpand = { onExpand(it) },
                onCollapse = { onExpand(null) },
                onPlay = onPlayItem,
                onLongClick = onShowOptions,
                rowContent = { v ->
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
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
