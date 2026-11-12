package com.mohamedadel.mytube.ui.home

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.unified.SearchSource
import com.mohamedadel.mytube.ui.search.SearchScreen

/**
 * Unified search bar (home-redesign Phase 0, 2026-11-12).
 * Wraps SearchScreen with a source-chip row (All | YouTube | SoundCloud | YT Music).
 * NOT wired into any screen yet — Phase 1 hooks it up; Phase 0 only proves it compiles
 * and behaves like today's bars (recents per source + per-item delete + clear-all).
 */
@Composable
fun UnifiedSearchBar(
    query: String,
    loading: Boolean,
    selected: SearchSource?,
    onSelectSource: (SearchSource?) -> Unit,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClose: () -> Unit,
    onClear: () -> Unit,
    dropRecents: List<String> = emptyList(),
    suggestions: List<String> = emptyList(),
    onPickSuggestion: (String) -> Unit = {},
    onClearRecents: () -> Unit = {},
    onDeleteRecent: (String) -> Unit = {},
    onVoice: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // source chips: null = All sources (Phase 1 parallel mode)
        Row(
            modifier = Modifier.fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            SourceChip(label = "الكل", isSelected = selected == null, onClick = { onSelectSource(null) })
            SourceChip(label = "YouTube", isSelected = selected == SearchSource.YOUTUBE, onClick = { onSelectSource(SearchSource.YOUTUBE) })
            SourceChip(label = "SoundCloud", isSelected = selected == SearchSource.SOUNDCLoud, onClick = { onSelectSource(SearchSource.SOUNDCLoud) })
            SourceChip(label = "YT Music", isSelected = selected == SearchSource.YTMUSIC, onClick = { onSelectSource(SearchSource.YTMUSIC) })
        }
        SearchScreen(
            query = query,
            loading = loading,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onClose = onClose,
            onClear = onClear,
            showDropdown = true,
            dropRecents = dropRecents,
            suggestions = suggestions,
            onPickSuggestion = onPickSuggestion,
            onClearRecents = onClearRecents,
            onDeleteRecent = onDeleteRecent,
            onVoice = onVoice,
            fillMax = false
        )
    }
}

@Composable
private fun SourceChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.padding(end = 8.dp)
    )
}
