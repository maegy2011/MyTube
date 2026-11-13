package com.mohamedadel.mytube.ui.trending

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.ui.results.TREND_TABS
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.Page

/**
 * Phase 2.3 slice 7 (2026-11-13): trending kiosks + suggestions + news
 * pagination extracted from the God-Activity. Prefs arrive as call params.
 * Every function <= 30 lines.
 */
class TrendingViewModel(app: Application) : AndroidViewModel(app) {

    private fun errKiosk(): String =
        try { getApplication<Application>().getString(R.string.err_kiosk) } catch (_: Exception) { "" }

    private val seeds = listOf("music", "tech reviews", "gaming", "news", "podcasts")

    private val _trendTab = MutableStateFlow(0)
    val trendTab: StateFlow<Int> = _trendTab.asStateFlow()
    private val _trendingItems = MutableStateFlow<List<VideoItem>>(emptyList())
    val trendingItems: StateFlow<List<VideoItem>> = _trendingItems.asStateFlow()
    private val _trendingLoading = MutableStateFlow(false)
    val trendingLoading: StateFlow<Boolean> = _trendingLoading.asStateFlow()
    private val _trendingLoadingMore = MutableStateFlow(false)
    val trendingLoadingMore: StateFlow<Boolean> = _trendingLoadingMore.asStateFlow()
    private val _trendingError = MutableStateFlow<String?>(null)
    val trendingError: StateFlow<String?> = _trendingError.asStateFlow()
    private val _trendingHasMore = MutableStateFlow(false)
    val trendingHasMore: StateFlow<Boolean> = _trendingHasMore.asStateFlow()
    private val _trendingCached = MutableStateFlow(false)
    val trendingCached: StateFlow<Boolean> = _trendingCached.asStateFlow()
    private val _trendingNewsNext = MutableStateFlow<Page?>(null)
    private val _trendingNewsQuery = MutableStateFlow<String?>(null)
    private val _suggestionChips = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val suggestionChips: StateFlow<Map<String, List<String>>> = _suggestionChips.asStateFlow()

    private var cache: Map<String, Pair<Long, List<VideoItem>>> = emptyMap()
    private var suggestJob: Job? = null

    fun setTrendTab(i: Int) { _trendTab.value = i }
    fun cancelSuggestions() { try { suggestJob?.cancel() } catch (_: Exception) { } }

    fun refreshTrending(force: Boolean, tab: Int, country: String, uiLang: String, recent: List<String>) {
        val kiosk = TREND_TABS.getOrNull(tab)?.second
        val isSearchTab = TREND_TABS.getOrNull(tab)?.third == true
        val cacheKey = "$tab|$country"
        val region = if (country == "global") null else country
        if (!force && kiosk != "suggestions") {
            val hit = cache[cacheKey]
            if (hit != null && System.currentTimeMillis() - hit.first < 30 * 60 * 1000L && hit.second.isNotEmpty()) {
                applyItems(hit.second, cached = true, hasMore = false)
                return
            }
        }
        _trendingCached.value = false
        _trendingLoading.value = true
        if (kiosk != "suggestions") cancelSuggestions()
        _trendingError.value = null
        _trendingHasMore.value = false
        _trendingNewsNext.value = null
        _trendingNewsQuery.value = null
        if (kiosk == "suggestions") {
            refreshSuggestions(region, tab, recent)
            return
        }
        viewModelScope.launch {
            try {
                if (isSearchTab) loadNewsTab(region, uiLang, cacheKey) else loadKioskTab(region, kiosk, cacheKey)
            } catch (_: Exception) {
                fallbackStale(cacheKey)
            } finally {
                _trendingLoading.value = false
            }
        }
    }

    private fun applyItems(items: List<VideoItem>, cached: Boolean, hasMore: Boolean) {
        _trendingItems.value = items
        _trendingHasMore.value = hasMore
        _trendingNewsNext.value = null
        _trendingNewsQuery.value = null
        _trendingError.value = null
        _trendingCached.value = cached
        _trendingLoading.value = false
    }

    private suspend fun loadNewsTab(region: String?, uiLang: String, cacheKey: String) {
        val nq = if (uiLang == "ar") "أخبار" else "news"
        val page = YouTubeRepo.search(nq, null, region)
        _trendingItems.value = page.items
        _trendingNewsQuery.value = nq
        _trendingNewsNext.value = page.nextPage
        _trendingHasMore.value = page.hasNext && page.nextPage != null
        finishTabLoad(cacheKey)
    }

    private suspend fun loadKioskTab(region: String?, kiosk: String?, cacheKey: String) {
        _trendingItems.value = YouTubeRepo.trending(region, kiosk)
        _trendingHasMore.value = _trendingItems.value.isNotEmpty()
        finishTabLoad(cacheKey)
    }

    private fun finishTabLoad(cacheKey: String) {
        if (_trendingItems.value.isEmpty()) {
            val stale = cache[cacheKey]?.second.orEmpty()
            if (stale.isNotEmpty()) {
                _trendingItems.value = stale
                _trendingCached.value = true
                _trendingError.value = null
            } else {
                _trendingError.value = errKiosk()
            }
        } else {
            _trendingError.value = null
            cache = cache + (cacheKey to (System.currentTimeMillis() to _trendingItems.value))
        }
    }

    private fun fallbackStale(cacheKey: String) {
        val stale = cache[cacheKey]?.second.orEmpty()
        if (stale.isNotEmpty()) {
            _trendingItems.value = stale
            _trendingCached.value = true
            _trendingError.value = null
        } else {
            _trendingItems.value = emptyList()
            _trendingError.value = errKiosk()
        }
    }

    fun refreshSuggestions(region: String?, myTab: Int, recent: List<String>) {
        val seedList = (recent.take(3) + seeds.take(2)).distinct().take(3)
        if (seedList.isEmpty()) {
            if (_trendTab.value == myTab) {
                _suggestionChips.value = emptyMap()
                _trendingItems.value = emptyList()
            }
            return
        }
        cancelSuggestions()
        suggestJob = viewModelScope.launch {
            try {
                val chips = seedList.associateWith { seed ->
                    try { YouTubeRepo.suggestions(seed).take(4) } catch (_: Exception) { emptyList() }
                }
                val vids = seedList.take(2).flatMap { seed ->
                    try { YouTubeRepo.search(seed, null, region).items.take(6) } catch (_: Exception) { emptyList() }
                }.distinctBy { it.id ?: it.url ?: it.title }.take(18)
                if (_trendTab.value == myTab) {
                    _suggestionChips.value = chips
                    _trendingItems.value = vids
                    _trendingError.value = null
                }
            } catch (_: Exception) {
                if (_trendTab.value == myTab) {
                    _suggestionChips.value = emptyMap()
                    _trendingItems.value = emptyList()
                    _trendingError.value = errKiosk()
                }
            } finally {
                if (_trendTab.value == myTab) _trendingLoading.value = false
            }
        }
    }

    fun loadMoreTrending(tab: Int, country: String) {
        if (_trendingLoadingMore.value || !_trendingHasMore.value) return
        val kiosk = TREND_TABS.getOrNull(tab)?.second
        val isSearchTab = TREND_TABS.getOrNull(tab)?.third == true
        _trendingLoadingMore.value = true
        viewModelScope.launch {
            try {
                val region = if (country == "global") null else country
                if (isSearchTab) loadMoreNews(region) else loadMoreKiosk(region, kiosk)
            } catch (_: Exception) {
                _trendingHasMore.value = false
            } finally {
                _trendingLoadingMore.value = false
            }
        }
    }

    private suspend fun loadMoreNews(region: String?) {
        val pg = _trendingNewsNext.value
        val nq = _trendingNewsQuery.value
        if (pg == null || nq == null) { _trendingHasMore.value = false; return }
        val page = YouTubeRepo.search(nq, pg, region)
        val seen = _trendingItems.value.mapNotNull { it.url ?: it.id }.toSet()
        _trendingItems.value = _trendingItems.value + page.items.filter { (it.url ?: it.id ?: it.title) !in seen }
        _trendingNewsNext.value = page.nextPage
        _trendingHasMore.value = page.hasNext && page.nextPage != null
    }

    private suspend fun loadMoreKiosk(region: String?, kiosk: String?) {
        val (more, hasMore) = YouTubeRepo.trendingMore(region, kiosk)
        if (more.isNotEmpty()) {
            val seen = _trendingItems.value.mapNotNull { it.url ?: it.id }.toSet()
            _trendingItems.value = _trendingItems.value + more.filter { (it.url ?: it.id ?: it.title) !in seen }
        }
        _trendingHasMore.value = hasMore && more.isNotEmpty()
    }
}
