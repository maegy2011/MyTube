package com.mohamedadel.mytube.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.data.unified.SearchSource
import com.mohamedadel.mytube.data.unified.SourceResult
import com.mohamedadel.mytube.data.unified.UnifiedSearchRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

/**
 * Phase 2.3 slice 21 (2026-11-13): unified parallel search extracted from
 * the God-Activity. One supervisor job, three child launches — a slow or
 * failed source never blocks the others. Recent-query recording arrives
 * as a callback (owned by MainSearchViewModel). Every fn <= 30 lines.
 */
class UnifiedSearchViewModel : ViewModel() {

    private val _uYT = MutableStateFlow<SourceResult?>(null)
    val uYT: StateFlow<SourceResult?> = _uYT.asStateFlow()
    private val _uSC = MutableStateFlow<SourceResult?>(null)
    val uSC: StateFlow<SourceResult?> = _uSC.asStateFlow()
    private val _uYM = MutableStateFlow<SourceResult?>(null)
    val uYM: StateFlow<SourceResult?> = _uYM.asStateFlow()
    private val _unifiedLoading = MutableStateFlow(false)
    val unifiedLoading: StateFlow<Boolean> = _unifiedLoading.asStateFlow()

    private var unifiedJob: Job? = null
    fun cancel() { try { unifiedJob?.cancel() } catch (_: Exception) { } }

    fun clear() {
        cancel()
        _uYT.value = null; _uSC.value = null; _uYM.value = null
    }

    fun search(q: String, region: String?, onRecent: (String) -> Unit) {
        if (q.isBlank()) return
        cancel()
        _uYT.value = null; _uSC.value = null; _uYM.value = null
        _unifiedLoading.value = true
        onRecent(q)
        unifiedJob = viewModelScope.launch {
            try {
                supervisorScope {
                    launch { try { _uYT.value = UnifiedSearchRepo.searchYouTube(q, region) } catch (_: Exception) { } }
                    launch { try { _uSC.value = UnifiedSearchRepo.searchSoundCloud(q) } catch (_: Exception) { } }
                    launch { try { _uYM.value = UnifiedSearchRepo.searchYTMusic(q, region) } catch (_: Exception) { } }
                }
            } finally { _unifiedLoading.value = false }
        }
    }

    fun retrySource(q: String, s: SearchSource, region: String?) {
        if (q.isBlank()) return
        viewModelScope.launch {
            try {
                when (s) {
                    SearchSource.YOUTUBE -> _uYT.value = UnifiedSearchRepo.searchYouTube(q, region)
                    SearchSource.SOUNDCLoud -> _uSC.value = UnifiedSearchRepo.searchSoundCloud(q)
                    SearchSource.YTMUSIC -> _uYM.value = UnifiedSearchRepo.searchYTMusic(q, region)
                }
            } catch (_: Exception) { }
        }
    }
}
