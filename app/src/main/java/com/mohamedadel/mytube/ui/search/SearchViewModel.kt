package com.mohamedadel.mytube.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel : ViewModel() {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()
    val suggestions = _query.debounce(300).distinctUntilChanged().mapLatest { q ->
        if (q.isBlank()) emptyList() else YouTubeRepo.suggestions(q).take(8)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun onQueryChange(q: String) { _query.value = q }
}
