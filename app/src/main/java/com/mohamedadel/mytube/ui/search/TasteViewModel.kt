package com.mohamedadel.mytube.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.data.local.FavoriteEntity
import com.mohamedadel.mytube.data.local.PlaylistDao
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.util.TasteProfile
import com.mohamedadel.mytube.util.buildTasteProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Phase 2.3 slice 9 (2026-11-13): music-taste profile extracted from the
 * God-Activity. DAO injected via configure(); favorites/recents arrive as
 * call params (owned by Library/MainSearch VMs + activity flow).
 * Every function <= 30 lines.
 */
class TasteViewModel(app: Application) : AndroidViewModel(app) {

    private var dao: PlaylistDao? = null
    fun configure(d: PlaylistDao) {
        if (dao != null) return
        dao = d
    }

    private val _tasteProfile = MutableStateFlow<TasteProfile?>(null)
    val tasteProfile: StateFlow<TasteProfile?> = _tasteProfile.asStateFlow()
    private val _tasteMix = MutableStateFlow<List<VideoItem>>(emptyList())
    val tasteMix: StateFlow<List<VideoItem>> = _tasteMix.asStateFlow()
    private val _tasteMixTerm = MutableStateFlow<String?>(null)
    val tasteMixTerm: StateFlow<String?> = _tasteMixTerm.asStateFlow()
    private val _tasteMixLoading = MutableStateFlow(false)
    val tasteMixLoading: StateFlow<Boolean> = _tasteMixLoading.asStateFlow()
    private var tasteFetchedAt = 0L

    fun refreshTaste(
        favorites: List<FavoriteEntity>,
        recentMusic: List<String>,
        recentMain: List<String>,
        country: String
    ) {
        val now = System.currentTimeMillis()
        if (_tasteProfile.value != null && now - tasteFetchedAt < 5 * 60 * 1000L) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                buildProfile(favorites, recentMusic, recentMain)
                enrichMix(country)
            } catch (_: Exception) { }
        }
    }

    private suspend fun buildProfile(
        favorites: List<FavoriteEntity>,
        recentMusic: List<String>,
        recentMain: List<String>
    ) {
        val top = try { dao?.topPlayed(20) } catch (_: Exception) { null } ?: emptyList()
        _tasteProfile.value = buildTasteProfile(top, favorites, recentMusic, recentMain)
        tasteFetchedAt = System.currentTimeMillis()
    }

    private suspend fun enrichMix(country: String) {
        val prof = _tasteProfile.value ?: return
        val term = prof.terms.firstOrNull()
        if (term.isNullOrBlank() || term == _tasteMixTerm.value || _tasteMixLoading.value) return
        _tasteMixLoading.value = true
        try {
            val page = try {
                YouTubeRepo.search(term, null, if (country == "global") null else country, "music_songs")
            } catch (_: Exception) { null }
            val seen = prof.picks.mapNotNull { it.url ?: it.id }.toSet()
            _tasteMix.value = (page?.items ?: emptyList()).filter { (it.url ?: it.id ?: "") !in seen }.take(6)
            _tasteMixTerm.value = term
        } catch (_: Exception) { }
        _tasteMixLoading.value = false
    }
}
