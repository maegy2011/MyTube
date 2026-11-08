package com.mohamedadel.mytube.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
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
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("ابحث عن فيديو أو قناة…") },
                leadingIcon = {
                    if (loading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!loading && query.isNotEmpty()) {
                            IconButton(onClick = onClear) {
                                Icon(Icons.Rounded.Clear, contentDescription = "مسح البحث")
                            }
                        }
                        IconButton(onClick = { onSearch(query) }) {
                            Icon(Icons.Rounded.Search, contentDescription = "بحث")
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
        Text("اكتب ثم اضغط بحث — الاقتراحات والسجل يظهران أسفل الحقل.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

