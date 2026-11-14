package com.mohamedadel.mytube.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.playback.DlnaManager

/**
 * DLNA renderer picker (2026-11-14).
 *
 * Lists discovered MediaRenderer devices (smart TVs, receivers); tapping one
 * plays the current snapshot on it via AVTransport. Empty list shows a hint
 * instead of an empty box (same pattern as other pickers in this file).
 */
@Composable
fun DlnaPickerDialog(
    onPick: (DlnaManager.Renderer) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("DLNA") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (DlnaManager.renderers.isEmpty()) {
                    Text(
                        stringResource(R.string.dlna_none),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    DlnaManager.renderers.forEach { r ->
                        TextButton(onClick = { onPick(r) }, modifier = Modifier.fillMaxWidth()) {
                            Text(r.name, maxLines = 1)
                        }
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
