package com.mohamedadel.mytube.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.PinEntity
import com.mohamedadel.mytube.data.youtube.VideoItem

// Downloads manager: every pinned clip in one place with size, progress and actions.
// Data comes from the Room pin index (dao.observePins) — never scans the FS directly.
@Composable
fun DownloadsScreen(
    pins: List<PinEntity>,
    totalBytes: Long,
    pinningKeys: Set<String>,
    progressOf: (String) -> Float?,
    thumbOf: (PinEntity) -> String?,
    titleOf: (String) -> String? = { null },
    onPlay: (PinEntity) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit,
    // F5 (audit P1 2026-11-11): cancel an in-flight download (cooperative; resumes on retry).
    onCancel: (String) -> Unit = {},
    orphanCount: Int = 0,
    orphanBytes: Long = 0L,
    onCleanOrphans: () -> Unit = {}
) {
    // screens-fix Library P0-L1: destructive clear-all needs explicit confirmation.
    var confirmClearAll by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.DownloadDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            if (orphanCount > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onCleanOrphans() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CleaningServices, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.pin_orphans_fmt, orphanCount, String.format("%.1f MB", orphanBytes / 1048576.0)),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary, maxLines = 2
                    )
                }
                Spacer(Modifier.height(4.dp))
            }
            Text(
                stringResource(R.string.dl_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (pins.isNotEmpty()) {
                androidx.compose.material3.TextButton(onClick = { confirmClearAll = true }) {
                    Text(stringResource(R.string.dl_clear_all))
                }
            }
        }
        if (confirmClearAll) {
            com.mohamedadel.mytube.ui.playlists.ConfirmDeleteDialog(
                title = stringResource(R.string.dl_clear_all_title),
                message = stringResource(R.string.dl_clear_all_fmt, pins.size),
                onConfirm = { confirmClearAll = false; onClearAll() },
                onDismiss = { confirmClearAll = false }
            )
        }
        Text(
            stringResource(R.string.dl_total_fmt, totalBytes / 1048576.0),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(4.dp))
        if (pins.isEmpty() && pinningKeys.isEmpty()) {
            // screens-fix Library P0-L3: real empty-state (icon + hint + how-to) instead of bare text.
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                Icon(Icons.Rounded.DownloadDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.dl_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.dl_empty_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // F5: in-flight pins have no Room row yet — render them first so cancel is reachable.
                val doneKeys = pins.map { it.videoKey }.toSet()
                val pending = pinningKeys.filter { it !in doneKeys }
                items(pending, key = { "pending:$it" }) { k ->
                    val prog = progressOf(k)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    titleOf(k) ?: stringResource(R.string.pinning),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(4.dp))
                                if (prog != null && prog >= 0) {
                                    LinearProgressIndicator(
                                        progress = { prog },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(MaterialTheme.shapes.extraSmall)
                                    )
                                    Text(
                                        stringResource(R.string.dl_progress_fmt, (prog * 100).toInt()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                }
                            }
                            IconButton(onClick = { onCancel(k) }) {
                                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
                items(pins, key = { it.videoKey }) { p ->
                    val prog = progressOf(p.videoKey)
                    val inFlight = pinningKeys.contains(p.videoKey)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            val thumb = thumbOf(p)
                            if (!thumb.isNullOrBlank()) {
                                AsyncImage(
                                    model = thumb,
                                    contentDescription = null,
                                    modifier = Modifier.width(112.dp).height(63.dp).clip(MaterialTheme.shapes.extraSmall),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    p.title ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    (p.quality ?: "") + " • " + String.format("%.1f MB", (p.sizeBytes ?: 0L) / 1048576.0),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                                if (inFlight && prog != null) {
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { prog },
                                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(MaterialTheme.shapes.extraSmall)
                                    )
                                    Text(
                                        stringResource(R.string.dl_progress_fmt, (prog * 100).toInt()),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(onClick = { onPlay(p) }, enabled = !inFlight) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = stringResource(R.string.play), tint = MaterialTheme.colorScheme.primary)
                            }
                            // F5: cancel replaces delete while in flight (delete is disabled then).
                            if (inFlight) {
                                IconButton(onClick = { onCancel(p.videoKey) }) {
                                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.cancel), tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            IconButton(onClick = { onDelete(p.videoKey) }, enabled = !inFlight) {
                                Icon(Icons.Rounded.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Thin adapter: PinEntity back to a playable VideoItem (player resolves from disk via isPinned).
fun PinEntity.toVideoItem(): VideoItem = VideoItem(
    id = videoId, title = title, channel = channel, url = url
)

// Route wrapper: keeps the giant MyTubeApp when-branch to a single call (the 64KB
// method limit is real — MyTubeApp$27 already overflows without this indirection).
@Composable
fun DownloadsRoute(
    pins: List<PinEntity>,
    totalBytes: Long,
    pinningKeys: Set<String>,
    progressMap: Map<String, Float>,
    thumbLookup: (String) -> String?,
    titleLookup: (String) -> String? = { null },
    onPlay: (VideoItem) -> Unit,
    onDelete: (String) -> Unit,
    onClearAll: () -> Unit,
    onCancel: (String) -> Unit = {},
    orphanCount: Int = 0,
    orphanBytes: Long = 0L,
    onCleanOrphans: () -> Unit = {}
) {
    DownloadsScreen(
        pins = pins,
        totalBytes = totalBytes,
        pinningKeys = pinningKeys,
        progressOf = { k -> progressMap[k] },
        thumbOf = { pe -> try { thumbLookup(pe.videoKey) } catch (_: Exception) { null } },
        onPlay = { pe -> onPlay(pe.toVideoItem()) },
        onDelete = onDelete,
        onClearAll = onClearAll,
        onCancel = onCancel,
        titleOf = { k -> try { titleLookup(k) } catch (_: Exception) { null } },
        orphanCount = orphanCount,
        orphanBytes = orphanBytes,
        onCleanOrphans = onCleanOrphans
    )
}
