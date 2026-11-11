// Settings building blocks (redesign 11 Nov 2026): collapsible section cards,
// dropdown rows replacing 20+ radio lists, and switch rows replacing radio toggles.
package com.mohamedadel.mytube.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

// One collapsible card per settings group. All start collapsed — the screen shows
// 7 headers instead of 88 rows.
@Composable
fun SettingsSection(
    title: String,
    expandedDefault: Boolean = false,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(expandedDefault) }
    Card(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    content()
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// Compact picker: one row + dropdown menu. Replaces radio lists of 2–23 options.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropRow(
    label: String,
    entries: List<Pair<String, String>>,
    selected: String,
    onPick: (String) -> Unit,
    enabled: Boolean = true
) {
    var open by remember { mutableStateOf(false) }
    val cur = entries.firstOrNull { it.first == selected }?.second ?: selected
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        ExposedDropdownMenuBox(
            expanded = open && enabled,
            onExpandedChange = { if (enabled) open = !open },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = cur,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                enabled = enabled,
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = open && enabled) },
                modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = open && enabled,
                onDismissRequest = { open = false }
            ) {
                entries.forEach { (code, name) ->
                    DropdownMenuItem(
                        text = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        onClick = { onPick(code); open = false },
                        trailingIcon = {
                            if (code == selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    )
                }
            }
        }
    }
}

// Boolean toggle: label + optional description + switch. Replaces radio-circle rows.
@Composable
fun SwitchRow(
    label: String,
    desc: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().let { m -> if (enabled) m.clickable { onToggle() } else m }.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (desc != null) {
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = { onToggle() }, enabled = enabled)
    }
}
