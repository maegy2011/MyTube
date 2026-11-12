package com.mohamedadel.mytube.ui.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mohamedadel.mytube.R

// Geo-switch cache-guard dialog (2026-11-12, runtime layer): extracted from
// MainActivity's SettingsPane — the host method is at the 64KB JVM limit
// (MethodTooLargeException), so this lives here as its own method.
@Composable
fun CountryGuardDialog(
    pending: String,
    pendingName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.country_guard_title)) },
        text = { Text(stringResource(R.string.country_guard_msg, pendingName)) },
        confirmButton = {
            TextButton(onClick = { onConfirm(pending) }) { Text(stringResource(R.string.country_guard_apply)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
