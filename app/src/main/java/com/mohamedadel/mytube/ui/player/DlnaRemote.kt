package com.mohamedadel.mytube.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.playback.DlnaManager

/**
 * DLNA remote control (2026-11-14): transport + volume for the picked renderer.
 *
 * State is queried once on open ([DlnaManager.queryState]); every control is
 * best-effort AVTransport/RenderingControl over the LAN. Closes only by user.
 */
@Composable
fun DlnaRemoteDialog(
    renderer: DlnaManager.Renderer,
    onDismiss: () -> Unit
) {
    var playing by remember { mutableStateOf(true) }
    var volume by remember { mutableStateOf(-1) }
    var muted by remember { mutableStateOf(false) }
    var posText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(renderer.udn) {
        DlnaManager.queryState(renderer) { st ->
            if (st.volume in 0..100) volume = st.volume
            muted = st.mute
            playing = st.playing
            posText = if (st.posMs >= 0 && st.durMs > 0) fmtClock(st.posMs) + " / " + fmtClock(st.durMs) else null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dlna_remote) + " — " + renderer.name) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (playing) DlnaManager.pause(renderer) { ok -> if (ok) playing = false }
                        else DlnaManager.resume(renderer) { ok -> if (ok) playing = true }
                    }) {
                        Icon(
                            if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(if (playing) R.string.pause else R.string.play),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(onClick = {
                        DlnaManager.stopPlayback(renderer)
                        playing = false
                    }) {
                        Icon(
                            Icons.Rounded.Stop,
                            contentDescription = stringResource(R.string.stop),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = {
                        val next = !muted
                        DlnaManager.setMute(renderer, next) { ok -> if (ok) muted = next }
                    }) {
                        Icon(
                            if (muted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                            contentDescription = stringResource(R.string.dlna_mute),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                if (posText != null) {
                    Text(
                        posText!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                }
                if (volume in 0..100) {
                    Text(
                        stringResource(R.string.dlna_volume) + " " + volume,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = volume.toFloat(),
                        onValueChange = { volume = it.toInt() },
                        onValueChangeFinished = {
                            DlnaManager.setVolume(renderer, volume) { }
                        },
                        valueRange = 0f..100f,
                        steps = 99,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        stringResource(R.string.dlna_volume),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 0) { volume = 0 } }) { Text("0") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 25) { volume = 25 } }) { Text("25") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 50) { volume = 50 } }) { Text("50") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 75) { volume = 75 } }) { Text("75") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 100) { volume = 100 } }) { Text("100") }
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

private fun fmtClock(ms: Long): String {
    val s = (ms.coerceAtLeast(0) / 1000).toInt()
    return "%d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
}
