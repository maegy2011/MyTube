package com.mohamedadel.mytube.ui.playlists

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mohamedadel.mytube.R

// screens-fix Library P0-L1 (2026-11-11): one shared confirm dialog for every
// destructive Library action (was: instant deletes in Favorites/History/Playlists/Downloads).
@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        // screens-fix: the confirm action deletes — label it حذف, not مسح.
    confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
