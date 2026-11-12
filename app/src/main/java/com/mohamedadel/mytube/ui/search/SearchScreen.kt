package com.mohamedadel.mytube.ui.search

import com.mohamedadel.mytube.R
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.rounded.History
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

// Standalone search screen extracted from MainActivity (split step 6, final).
// Pure UI: query in via params, actions out via callbacks.

@Composable
fun SearchScreen(
    query: String,
    loading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClose: () -> Unit,
    onClear: () -> Unit,
    showDropdown: Boolean = false,
    dropRecents: List<String> = emptyList(),
    suggestions: List<String> = emptyList(),
    onPickSuggestion: (String) -> Unit = {},
    onClearRecents: () -> Unit = {},
    onDeleteRecent: (String) -> Unit = {},
    onVoice: () -> Unit = {},
    // embedded header (music tab): wrap content so the source chips + results below stay visible.
    // Standalone (main search): fill the whole screen as before.
    fillMax: Boolean = true
) {
    Column(modifier = (if (fillMax) Modifier.fillMaxSize() else Modifier.fillMaxWidth()).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Standalone (fillMax) has no back button: the header Up button (smartBack)
            // already covers navigation. Embedded keeps its close button.
            if (!fillMax) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onVoice) {
                            Icon(Icons.Rounded.Mic, contentDescription = stringResource(R.string.voice_search))
                        }
                        if (!loading && query.isNotEmpty()) {
                            IconButton(onClick = onClear) {
                                Icon(Icons.Rounded.Clear, contentDescription = stringResource(R.string.search_clear))
                            }
                        }
                        IconButton(onClick = { onSearch(query) }) {
                            Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search_action))
                        }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Content),
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { onSearch(query) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        // Fix: the dropdown used to live outside this screen while this Column is fillMaxSize,
        // so suggestions were pushed off-screen while typing and only flashed after submit.
        // Now they render inside the search screen, directly under the field.
        if (showDropdown && (suggestions.isNotEmpty() || dropRecents.isNotEmpty())) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Column {
                    if (query.isBlank() && dropRecents.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.recent_searches), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            androidx.compose.material3.TextButton(onClick = onClearRecents) { Text(stringResource(R.string.clear_all)) }
                        }
                    }
                    dropRecents.forEach { s ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onPickSuggestion(s) }.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(s, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                            IconButton(onClick = { onDeleteRecent(s) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.Clear, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    suggestions.take(8).forEach { s ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onPickSuggestion(s) }.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(s, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        } else {
            Text(stringResource(R.string.search_guide), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

