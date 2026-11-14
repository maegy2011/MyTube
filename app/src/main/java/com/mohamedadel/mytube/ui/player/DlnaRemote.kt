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
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay10
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * DLNA remote control (2026-11-14, enhanced).
 *
 * Transport (play/pause/stop/±10s/seek) + volume/mute + live position.
 * State refreshes on open, every 5s while open, and after every command.
 * Play falls back: resume → replay (re-send URI) when the renderer lost context.
 * All commands best-effort; failures surface one short inline line, never crash.
 */
@Composable
fun DlnaRemoteDialog(
    renderer: DlnaManager.Renderer,
    onDismiss: () -> Unit
) {
    var playing by remember { mutableStateOf(true) }
    var volume by remember { mutableStateOf(-1) }
    var muted by remember { mutableStateOf(false) }
    var posMs by remember { mutableStateOf(-1L) }
    var durMs by remember { mutableStateOf(-1L) }
    var seekPos by remember { mutableStateOf<Float?>(null) }
    var err by remember { mutableStateOf(false) }

    fun apply(st: DlnaManager.RemoteState) {
        if (st.volume in 0..100) volume = st.volume
        muted = st.mute
        playing = st.playing
        if (st.posMs >= 0) posMs = st.posMs
        if (st.durMs > 0) durMs = st.durMs
    }
    fun refresh() = DlnaManager.queryState(renderer, ::apply)

    LaunchedEffect(renderer.udn) {
        refresh()
        while (isActive) {
            delay(5000)
            refresh()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dlna_remote) + " — " + renderer.name) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (err) {
                    Text(
                        stringResource(R.string.dlna_cmd_failed),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        err = false
                        DlnaManager.seekTo(renderer, (posMs - 10000).coerceAtLeast(0)) { ok ->
                            if (ok) refresh() else err = true
                        }
                    }) {
                        Icon(
                            Icons.Rounded.Replay10, contentDescription = "-10s",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = {
                        err = false
                        if (playing) {
                            DlnaManager.pause(renderer) { ok ->
                                if (ok) { playing = false; refresh() } else err = true
                            }
                        } else {
                            DlnaManager.resume(renderer) { ok ->
                                if (ok) { playing = true; refresh() }
                                else DlnaManager.replay(renderer) { ok2 ->
                                    if (ok2) { playing = true; refresh() } else err = true
                                }
                            }
                        }
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
                        err = false
                        DlnaManager.seekTo(renderer, posMs + 10000) { ok ->
                            if (ok) refresh() else err = true
                        }
                    }) {
                        Icon(
                            Icons.Rounded.Forward10, contentDescription = "+10s",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = {
                        err = false
                        val next = !muted
                        DlnaManager.setMute(renderer, next) { ok ->
                            if (ok) muted = next else err = true
                        }
                    }) {
                        Icon(
                            if (muted) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                            contentDescription = stringResource(R.string.dlna_mute),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                if (durMs > 0 && posMs >= 0) {
                    Text(
                        fmtClock(posMs) + " / " + fmtClock(durMs),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Slider(
                        value = (seekPos ?: posMs.toFloat()).coerceIn(0f, durMs.toFloat()),
                        onValueChange = { seekPos = it },
                        onValueChangeFinished = {
                            val target = (seekPos ?: posMs.toFloat()).toLong()
                            seekPos = null
                            err = false
                            DlnaManager.seekTo(renderer, target) { ok ->
                                if (ok) refresh() else err = true
                            }
                        },
                        valueRange = 0f..durMs.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(4.dp))
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
                            err = false
                            DlnaManager.setVolume(renderer, volume) { ok ->
                                if (ok) refresh() else err = true
                            }
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
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 0) { if (it) volume = 0 } }) { Text("0") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 25) { if (it) volume = 25 } }) { Text("25") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 50) { if (it) volume = 50 } }) { Text("50") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 75) { if (it) volume = 75 } }) { Text("75") }
                        Spacer(Modifier.width(8.dp))
                        TextButton(onClick = { DlnaManager.setVolume(renderer, 100) { if (it) volume = 100 } }) { Text("100") }
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
