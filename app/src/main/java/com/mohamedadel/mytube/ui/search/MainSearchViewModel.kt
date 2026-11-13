package com.mohamedadel.mytube.ui.search

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.data.youtube.SearchPage
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Phase 2.3 slice 1 (2026-11-13): main-search domain extracted from the
 * God-Activity. State as StateFlows; completion via SharedFlow (scroll
 * restore stays in activity — needs the LazyListState). Every function
 * <= 30 lines.
 */
class MainSearchViewModel : ViewModel() {

    private val _currentQuery = MutableStateFlow("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()
    private val _results = MutableStateFlow<List<VideoItem>>(emptyList())
    val results: StateFlow<List<VideoItem>> = _results.asStateFlow()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()
    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    private val _nextPage = MutableStateFlow<org.schabi.newpipe.extractor.Page?>(null)
    val nextPage: StateFlow<org.schabi.newpipe.extractor.Page?> = _nextPage.asStateFlow()
    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()
    private val _suggestion = MutableStateFlow<String?>(null)
    val searchSuggestion: StateFlow<String?> = _suggestion.asStateFlow()
    private val _corrected = MutableStateFlow(false)
    val searchCorrected: StateFlow<Boolean> = _corrected.asStateFlow()
    private val _recent = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recent.asStateFlow()

    private val _done = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val searchDone: SharedFlow<String> = _done

    private var prefs: SharedPreferences? = null
    private var errSearch: () -> String = { "" }
    private var searchJob: Job? = null

    fun configure(p: SharedPreferences, err: () -> String) {
        if (prefs != null) return
        prefs = p
        errSearch = err
        try {
            p.getString("recent_searches", "")?.let { raw ->
                if (raw.isNotBlank()) _recent.value = raw.split("").filter { it.isNotBlank() }.take(6)
            }
        } catch (_: Exception) { }
    }

    fun assignRecentSearches(v: List<String>) { _recent.value = v }
    fun assignResults(v: List<VideoItem>) { _results.value = v }
    fun assignNextPage(v: org.schabi.newpipe.extractor.Page?) { _nextPage.value = v }
    fun assignHasMore(v: Boolean) { _hasMore.value = v }
    fun assignLoadingMore(v: Boolean) { _loadingMore.value = v }
    fun assignCurrentQuery(v: String) { _currentQuery.value = v }

    fun rememberRecent(q: String) {
        if (_recent.value.contains(q)) return
        val updated = (listOf(q) + _recent.value).take(6)
        _recent.value = updated
        try { prefs?.edit()?.putString("recent_searches", updated.joinToString(""))?.apply() } catch (_: Exception) { }
    }

    /** Playlist play-all/from replaces results without a network search. */
    fun showPlaylistResults(list: List<VideoItem>) {
        _results.value = list
        _currentQuery.value = ""
        _hasMore.value = false
        _nextPage.value = null
    }

    fun clearResults() {
        _results.value = emptyList()
    }

    fun search(q: String, contentFilter: String, region: String?, onPlaylists: (List<com.mohamedadel.mytube.data.youtube.RemotePlaylist>) -> Unit) {
        if (q.isBlank()) return
        _currentQuery.value = q
        _loading.value = true
        _searchError.value = null
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                onPlaylists(fetchCards(q, contentFilter, region))
                val filter = if (contentFilter == "songs") "music_songs" else contentFilter
                val page = if (contentFilter == "albums") SearchPage(emptyList(), null, false, null)
                else YouTubeRepo.search(q, null, region, filter)
                if (page.error != null && page.items.isEmpty()) {
                    failSearch()
                } else {
                    _results.value = page.items
                    _nextPage.value = page.nextPage
                    _hasMore.value = page.hasNext
                    _suggestion.value = page.suggestion
                    _corrected.value = page.corrected
                }
            } catch (_: Exception) {
                failSearch()
            } finally {
                _loading.value = false
            }
            rememberRecent(q)
            _done.tryEmit(q)
        }
    }

    private suspend fun fetchCards(q: String, contentFilter: String, region: String?): List<com.mohamedadel.mytube.data.youtube.RemotePlaylist> =
        try {
            if (contentFilter == "playlists") YouTubeRepo.searchPlaylists(q, region)
            else if (contentFilter == "albums") YouTubeRepo.searchPlaylists(q, region, albums = true)
            else emptyList()
        } catch (_: Exception) { emptyList() }

    private fun failSearch() {
        _searchError.value = try { errSearch() } catch (_: Exception) { "error" }
        _results.value = emptyList()
        _hasMore.value = false
    }

    fun loadMore(region: String?, contentFilter: String) {
        val q = _currentQuery.value
        val pg = _nextPage.value
        if (q.isBlank() || pg == null || _loadingMore.value || _loading.value || !_hasMore.value) return
        _loadingMore.value = true
        viewModelScope.launch {
            try {
                val res = YouTubeRepo.search(q, pg, region, contentFilter)
                val seen = _results.value.mapNotNull { it.url ?: it.id }.toSet()
                _results.value = _results.value + res.items.filter { (it.url ?: it.id ?: it.title) !in seen }
                _nextPage.value = res.nextPage
                _hasMore.value = res.hasNext && res.nextPage != null
            } catch (_: Exception) {
                _hasMore.value = false
            } finally {
                _loadingMore.value = false
            }
        }
    }
}
