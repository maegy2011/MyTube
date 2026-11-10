package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.PinManager
import com.mohamedadel.mytube.data.youtube.StreamData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Pin-confirm sheet: location + format + quality + audio + subtitle + live size.
// Extracted from MyTubeApp — the Scaffold content method is at the 64KB JVM limit,
// so this 90-line dialog must live in its own file, not inline.
@Composable
fun PinConfirmDialog(
    title: String?,
    sd: StreamData?,
    selLoc: String,
    selQuality: String,
    selAudio: Int,
    selSub: Int,
    estBytes: Long,
    sizeUnknown: String,
    sizeFmt: (Double) -> String,
    extOk: Boolean,
    onLoc: (String) -> Unit,
    onQuality: (String) -> Unit,
    onAudio: (Int) -> Unit,
    onSub: (Int) -> Unit,
    onEst: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pin_title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Text(title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                if (sd != null) {
                    Text(stringResource(R.string.dl_location), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    listOf("internal", "external").forEach { loc ->
                        val disabled = loc == "external" && !extOk
                        Row(modifier = Modifier.fillMaxWidth().clickable(enabled = !disabled) {
                            onLoc(loc)
                        }.padding(vertical = 4.dp).alpha(if (disabled) 0.4f else 1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (selLoc == loc) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (selLoc == loc) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(if (loc == "internal") R.string.dl_internal else R.string.dl_external), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Text(stringResource(R.string.dl_format), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    listOf("video", "audio").forEach { kind ->
                        val sel = (kind == "audio") == (selQuality == "audio-only (default)")
                        Row(modifier = Modifier.fillMaxWidth().clickable {
                            onQuality(if (kind == "audio") "audio-only (default)" else (sd.videoUrlByQuality.keys.firstOrNull() ?: selQuality))
                        }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (sel) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(if (kind == "audio") R.string.quality_audio else R.string.dl_video), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    if (selQuality != "audio-only (default)") {
                        Text(stringResource(R.string.dl_quality), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        sd.videoUrlByQuality.keys.forEach { q ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onQuality(q) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (selQuality == q) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (selQuality == q) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(q, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    if (sd.audioTracks.isNotEmpty()) {
                        Text(stringResource(R.string.dl_audio), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        sd.audioTracks.forEachIndexed { i, t ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onAudio(i) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (selAudio == i) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (selAudio == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(t.name ?: t.localeTag ?: ("#" + (i + 1)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    if (sd.subtitles.isNotEmpty()) {
                        Text(stringResource(R.string.dl_captions), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSub(-1) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (selSub == -1) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (selSub == -1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.dl_none), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        sd.subtitles.forEachIndexed { i, st ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onSub(i) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(if (selSub == i) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (selSub == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(st.label ?: st.langTag ?: ("#" + (i + 1)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                    // Live size estimate for the current selection (IO, debounced by composition).
                    LaunchedEffect(selQuality, selAudio, selSub) {
                        onEst(-1L)
                        try {
                            val p = withContext(Dispatchers.IO) {
                                PinManager.buildPlan(sd, selQuality, selAudio, selSub, 0)
                            }
                            onEst(if (p == null) -1L else withContext(Dispatchers.IO) {
                                PinManager.estimateBytes(listOf(p.videoUrl, p.audioUrl, p.subUrl))
                            })
                        } catch (_: Exception) { onEst(-1L) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                val sizeTxt = if (estBytes >= 0) sizeFmt(estBytes / 1048576.0) else sizeUnknown
                Text(
                    stringResource(R.string.pin_confirm_fmt, selQuality, sizeTxt),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.pin_action)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

// Age-gate consent: shown once per video in "warn" mode, never remembered.
@Composable
fun AgeWarnDialog(
    title: String?,
    onPlay: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.age_warn_title), maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.age_warn_text), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        },
        confirmButton = { TextButton(onClick = onPlay) { Text(stringResource(R.string.age_warn_play)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
