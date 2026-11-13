package com.mohamedadel.mytube.ui.search

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.data.youtube.RemotePlaylist
import com.mohamedadel.mytube.data.youtube.SearchPage
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Phase 2.2 (2026-11-13): music-search + remote-playlist domains extracted
 * from the God-Activity. State as StateFlows; network in viewModelScope.
 * Named MusicSearchViewModel: ui.search.SearchViewModel already exists
 * (unified-search suggestions). Every function <= 30 lines.
 */
class MusicSearchViewModel : ViewModel() {

    private val _scQuery = MutableStateFlow("")
    val scQuery: StateFlow<String> = _scQuery.asStateFlow()
    private val _scResults = MutableStateFlow<List<VideoItem>>(emptyList())
    val scResults: StateFlow<List<VideoItem>> = _scResults.asStateFlow()
    private val _scLoading = MutableStateFlow(false)
    val scLoading: StateFlow<Boolean> = _scLoading.asStateFlow()
    private val _scError = MutableStateFlow<String?>(null)
    val scError: StateFlow<String?> = _scError.asStateFlow()
    private val _scHasMore = MutableStateFlow(false)
    val scHasMore: StateFlow<Boolean> = _scHasMore.asStateFlow()
    private val _scLoadingMore = MutableStateFlow(false)
    val scLoadingMore: StateFlow<Boolean> = _scLoadingMore.asStateFlow()
    private val _scSuggestion = MutableStateFlow<String?>(null)
    val scSuggestion: StateFlow<String?> = _scSuggestion.asStateFlow()
    private val _scCorrected = MutableStateFlow(false)
    val scCorrected: StateFlow<Boolean> = _scCorrected.asStateFlow()
    private val _scSource = MutableStateFlow(0)
    val scSource: StateFlow<Int> = _scSource.asStateFlow()
    private val _scKind = MutableStateFlow("songs")
    val scKind: StateFlow<String> = _scKind.asStateFlow()
    private val _scNextPage = MutableStateFlow<org.schabi.newpipe.extractor.Page?>(null)
    private val _recentMusic = MutableStateFlow<List<String>>(emptyList())
    val recentMusicSearches: StateFlow<List<String>> = _recentMusic.asStateFlow()
    private val _remotePlaylists = MutableStateFlow<List<RemotePlaylist>>(emptyList())
    val remotePlaylists: StateFlow<List<RemotePlaylist>> = _remotePlaylists.asStateFlow()

    private val _openRemote = MutableStateFlow<RemotePlaylist?>(null)
    val openRemote: StateFlow<RemotePlaylist?> = _openRemote.asStateFlow()
    private val _openRemoteItems = MutableStateFlow<List<VideoItem>>(emptyList())
    val openRemoteItems: StateFlow<List<VideoItem>> = _openRemoteItems.asStateFlow()
    private val _openRemoteLoading = MutableStateFlow(false)
    val openRemoteLoading: StateFlow<Boolean> = _openRemoteLoading.asStateFlow()
    private val _openRemoteNext = MutableStateFlow<org.schabi.newpipe.extractor.Page?>(null)
    val openRemoteNext: StateFlow<org.schabi.newpipe.extractor.Page?> = _openRemoteNext.asStateFlow()
    private val _openRemoteLoadingMore = MutableStateFlow(false)
    val openRemoteLoadingMore: StateFlow<Boolean> = _openRemoteLoadingMore.asStateFlow()

    private var prefs: SharedPreferences? = null
    private var errDetail: (String) -> String = { it }
    private var searchJob: Job? = null

    fun configure(p: SharedPreferences, errFmt: (String) -> String) {
        if (prefs != null) return
        prefs = p
        errDetail = errFmt
        try {
            p.getString("recent_music_searches", "")?.let { m ->
                if (m.isNotBlank()) _recentMusic.value = m.split("").filter { it.isNotBlank() }.take(6)
            }
        } catch (_: Exception) { }
    }

    fun setScQuery(v: String) { _scQuery.value = v }
    fun assignScResults(v: List<VideoItem>) { _scResults.value = v }
    fun assignScError(v: String?) { _scError.value = v }
    fun assignScSuggestion(v: String?) { _scSuggestion.value = v }
    fun assignRecentMusic(v: List<String>) { _recentMusic.value = v }
    fun setScSource(v: Int) { _scSource.value = v }
    fun setScKind(v: String) { _scKind.value = v }
    fun setOpenRemote(v: RemotePlaylist?) { _openRemote.value = v }
    fun clearOpenRemote() {
        _openRemote.value = null
        _openRemoteItems.value = emptyList()
        _openRemoteNext.value = null
    }

    /** Main-search playlist cards write here; songs/albums branches stay in activity. */
    fun assignRemotePlaylists(v: List<RemotePlaylist>) { _remotePlaylists.value = v }

    private fun musicFilter(kind: String): String = when (kind) {
        "albums" -> "music_albums"
        "playlists" -> "music_playlists"
        "artists" -> "music_artists"
        else -> "music_songs"
    }

    private suspend fun mainPage(q: String, src: Int, kind: String, region: String?): SearchPage {
        if (src == 1 && kind == "albums") return SearchPage(emptyList(), null, false, null)
        var last: SearchPage? = null
        repeat(2) {
            last = if (src == 1) YouTubeRepo.search(q, null, region, musicFilter(kind))
            else YouTubeRepo.scSearch(q)
            val l = last
            if (l != null && l.error == null && l.items.isNotEmpty()) return l
        }
        return last ?: SearchPage()
    }

    private suspend fun fetchPlaylists(q: String, src: Int, kind: String, region: String?): List<RemotePlaylist> =
        try {
            if (src == 1 && kind == "albums") YouTubeRepo.searchPlaylists(q, region, albums = true)
            else if (src == 1 && kind == "playlists") YouTubeRepo.searchPlaylists(q, region, music = true)
            else if (src == 1) emptyList()
            else YouTubeRepo.searchPlaylists(q, sc = true)
        } catch (_: Exception) { emptyList() }

    private fun rememberMusicQuery(q: String) {
        if (_recentMusic.value.contains(q)) return
        val updated = (listOf(q) + _recentMusic.value).take(6)
        _recentMusic.value = updated
        try { prefs?.edit()?.putString("recent_music_searches", updated.joinToString(""))?.apply() } catch (_: Exception) { }
    }

    fun search(q: String, src: Int, kind: String, region: String?) {
        if (q.isBlank()) return
        _scQuery.value = q
        _scLoading.value = true
        _scError.value = null
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                val page = try { mainPage(q, src, kind, region) } catch (e: Exception) {
                    SearchPage(error = e.javaClass.simpleName + (e.message?.takeIf { it.isNotBlank() }?.let { ": " + it.take(120) } ?: ""))
                }
                if (page.error != null && page.items.isEmpty()) {
                    failSearch(page.error)
                } else {
                    _scResults.value = page.items
                    _scNextPage.value = page.nextPage
                    _scHasMore.value = page.hasNext && page.nextPage != null
                    _scSuggestion.value = page.suggestion
                    _scCorrected.value = page.corrected
                    rememberMusicQuery(q)
                    _remotePlaylists.value = fetchPlaylists(q, src, kind, region)
                }
            } catch (e: Exception) {
                failSearch(e.javaClass.simpleName)
            } finally {
                _scLoading.value = false
            }
        }
    }

    private fun failSearch(raw: String?) {
        _scError.value = try { errDetail(raw ?: "") } catch (_: Exception) { raw }
        _scResults.value = emptyList()
        _remotePlaylists.value = emptyList()
        _scSuggestion.value = null
        _scCorrected.value = false
    }

    fun loadMore(region: String?) {
        val q = _scQuery.value
        val pg = _scNextPage.value
        if (q.isBlank() || pg == null || _scLoadingMore.value || _scLoading.value || !_scHasMore.value) return
        _scLoadingMore.value = true
        viewModelScope.launch {
            try {
                val res = if (_scSource.value == 1) YouTubeRepo.search(q, pg, region, musicFilter(_scKind.value))
                else YouTubeRepo.scSearch(q, pg)
                val seen = _scResults.value.mapNotNull { it.url ?: it.id }.toSet()
                _scResults.value = _scResults.value + res.items.filter { (it.url ?: it.id ?: it.title) !in seen }
                _scNextPage.value = res.nextPage
                _scHasMore.value = res.hasNext && res.nextPage != null
            } catch (_: Exception) {
                _scHasMore.value = false
            } finally {
                _scLoadingMore.value = false
            }
        }
    }

    fun openPlaylist(p: RemotePlaylist) {
        _openRemote.value = p
        _openRemoteItems.value = emptyList()
        _openRemoteNext.value = null
        _openRemoteLoadingMore.value = false
        _openRemoteLoading.value = true
        viewModelScope.launch {
            try {
                val (first, next) = YouTubeRepo.fetchPlaylist(p.url ?: return@launch)
                _openRemoteItems.value = first
                _openRemoteNext.value = next
            } catch (_: Exception) {
                _openRemoteItems.value = emptyList()
                _openRemoteNext.value = null
            } finally {
                _openRemoteLoading.value = false
            }
        }
    }

    fun loadMoreRemote() {
        val rp = _openRemote.value ?: return
        val nxt = _openRemoteNext.value ?: return
        if (_openRemoteLoading.value || _openRemoteLoadingMore.value) return
        _openRemoteLoadingMore.value = true
        viewModelScope.launch {
            try {
                val (more, next) = YouTubeRepo.fetchPlaylistPage(rp.url ?: return@launch, nxt)
                if (more.isNotEmpty()) {
                    val seen = _openRemoteItems.value.mapNotNull { it.url ?: it.id }.toSet()
                    _openRemoteItems.value = _openRemoteItems.value + more.filter { (it.url ?: it.id ?: it.title) !in seen }
                }
                _openRemoteNext.value = next
            } catch (_: Exception) { }
            finally { _openRemoteLoadingMore.value = false }
        }
    }
}
