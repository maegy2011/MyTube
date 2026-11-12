package com.mohamedadel.mytube.ui.home

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.PlaylistDao
import com.mohamedadel.mytube.data.local.PlaylistEntity
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toEntity
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.playback.PlaybackService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Extracted from MyTubeApp (VerifyError fix, 2026-11-12): all end-of-screen
 * dialogs live here so the activity composable stays under ART limits.
 * Bulky work (prefs/DB/service/intent) is internal; call-site lambdas stay thin.
 */
@Composable
fun HomeDialogs(
    ctx: Context,
    prefs: SharedPreferences,
    scope: CoroutineScope,
    dao: PlaylistDao,
    toast: (String) -> Unit,
    uiToast: (String) -> Unit,
    // add-to-playlist picker
    pickFor: VideoItem?,
    setPickFor: (VideoItem?) -> Unit,
    playlists: List<com.mohamedadel.mytube.data.local.PlaylistWithCount>,
    // sleep timer
    showSleep: Boolean,
    sleepLeft: String,
    setSleepEndOfTrack: (Boolean) -> Unit,
    setSleepTrackKey: (String?) -> Unit,
    setSleepAtMs: (Long?) -> Unit,
    setSleepLeft: (String) -> Unit,
    setShowSleep: (Boolean) -> Unit,
    playing: VideoItem?,
    // age gate
    agePending: Triple<VideoItem, Boolean, StreamData?>?,
    onAgePlay: (Triple<VideoItem, Boolean, StreamData?>) -> Unit,
    setAgePending: (Triple<VideoItem, Boolean, StreamData?>?) -> Unit,
    // pin confirm
    pinConfirm: com.mohamedadel.mytube.data.local.PinManager.PinRequest?,
    pinSd: com.mohamedadel.mytube.data.youtube.StreamData?,
    pinSelLoc: String,
    pinSelQuality: String,
    pinSelAudio: Int,
    pinSelSubs: Set<Int>,
    pinEst: Long,
    prefAllow4k: Boolean,
    setPinSelLoc: (String) -> Unit,
    setPinSelQuality: (String) -> Unit,
    setPinSelAudio: (Int) -> Unit,
    setPinSelSubs: (Set<Int>) -> Unit,
    setPinEst: (Long) -> Unit,
    setPinConfirm: (com.mohamedadel.mytube.data.local.PinManager.PinRequest?) -> Unit,
    onConfirmPin: () -> Unit,
    // video options
    optionsFor: VideoItem?,
    setOptionsFor: (VideoItem?) -> Unit,
    onPlayNow: (VideoItem) -> Unit,
    onPlayNext: (VideoItem) -> Unit,
    onQueueEnd: (VideoItem) -> Unit,
    onPinToggle: (VideoItem) -> Unit,
    onQuickPin: (VideoItem) -> Unit,
    isPinned: (VideoItem) -> Boolean,
    isPinning: (VideoItem) -> Boolean,
    onSubscribeVideo: (VideoItem) -> Unit,
    onShare: (VideoItem) -> Unit,
    onStartRadio: (VideoItem) -> Unit,
    isSubscribed: (VideoItem) -> Boolean,
    // update dialog
    updateInfo: com.mohamedadel.mytube.data.UpdateCheck.Release?,
    setUpdateInfo: (com.mohamedadel.mytube.data.UpdateCheck.Release?) -> Unit,
    modifier: Modifier = Modifier
) {
    // add-to-playlist picker for a single video
    pickFor?.let { v ->
        com.mohamedadel.mytube.ui.playlists.PlaylistPickerDialog(
            video = v,
            playlists = playlists,
            onCreateAndAdd = { name ->
                scope.launch {
                    try {
                        val pid = dao.insertPlaylist(PlaylistEntity(name = name))
                        dao.insertItems(listOf(v.toEntity(pid, 0)))
                        toast(ctx.getString(R.string.toast_playlist_created))
                    } catch (_: Exception) { toast(ctx.getString(R.string.toast_playlist_create_fail)) }
                    setPickFor(null)
                }
            },
            onAddTo = { pid ->
                scope.launch {
                    // no duplicates (same stable key as favorites): reject LOUDLY, never silently
                    val key = v.favKey()
                    val exists = dao.itemsOnce(pid).any { e ->
                        VideoItem(id = e.videoId, title = e.title, channel = e.channel, thumbnail = e.thumbnail, durationSec = e.durationSec, viewCount = e.viewCount, url = e.url).favKey() == key
                    }
                    if (!exists) {
                        val pos = dao.itemCount(pid)
                        dao.insertItems(listOf(v.toEntity(pid, pos)))
                        toast(ctx.getString(R.string.toast_added_playlist))
                    } else {
                        toast(ctx.getString(R.string.toast_already_queued))
                    }
                    setPickFor(null)
                }
            },
            onDismiss = { setPickFor(null) }
        )
    }
    // sleep timer picker: arms/clears the auto-pause deadline watched by the polling loop
    if (showSleep) {
        com.mohamedadel.mytube.ui.player.SleepTimerDialog(
            currentLabel = sleepLeft.ifBlank { null },
            onPick = { m ->
                setSleepEndOfTrack(false)
                setSleepTrackKey(null) // F12: a fresh timer disarms any track-bound flag
                setSleepAtMs(if (m == null) null else android.os.SystemClock.uptimeMillis() + m * 60000L)
                if (m == null) {
                    setSleepLeft("")
                    try { prefs.edit().remove("sleep_deadline").apply() } catch (_: Exception) { }
                    // F1: cancel the service-side deadline too.
                    try { PlaybackService.cancelSleep() } catch (_: Exception) { }
                } else {
                    try { prefs.edit().putLong("sleep_deadline", android.os.SystemClock.uptimeMillis() + m * 60000L).apply() } catch (_: Exception) { }
                    // F1: service-side deadline survives Activity death (Compose watcher is UI-only).
                    try { PlaybackService.scheduleSleep(m * 60000L) } catch (_: Exception) { }
                }
                setShowSleep(false)
            },
            onEndOfTrack = {
                setSleepAtMs(null)
                try { prefs.edit().remove("sleep_deadline").apply() } catch (_: Exception) { }
                setSleepTrackKey(playing?.favKey()) // F12: bind the flag to this track
                setSleepEndOfTrack(true)
                setSleepLeft(try { ctx.getString(R.string.sleep_track) } catch (_: Exception) { "..." })
                setShowSleep(false)
            },
            onDismiss = { setShowSleep(false) }
        )
    }
    // age-gate warn dialog lives in the library file (Scaffold content is at the 64KB limit)
    agePending?.let { ap ->
        com.mohamedadel.mytube.ui.library.AgeWarnDialog(
            title = ap.first.title,
            onPlay = { onAgePlay(ap); setAgePending(null) },
            onDismiss = { setAgePending(null) }
        )
    }
    // pin confirm: own file (Scaffold content is at the 64KB method limit)
    pinConfirm?.let { pc ->
        com.mohamedadel.mytube.ui.library.PinConfirmDialog(
            title = pc.v.title,
            sd = pinSd,
            selLoc = pinSelLoc,
            selQuality = pinSelQuality,
            selAudio = pinSelAudio,
            selSubs = pinSelSubs,
            maxH = if (prefAllow4k) Int.MAX_VALUE else 1080,
            estBytes = pinEst,
            sizeUnknown = stringResource(R.string.pin_size_unknown),
            sizeFmt = { mb -> ctx.getString(R.string.pin_size_fmt, mb) },
            extOk = try { com.mohamedadel.mytube.data.local.PinFiles.isExternalAvailable(ctx.applicationContext) } catch (_: Exception) { false },
            onLoc = { loc ->
                setPinSelLoc(loc)
                try { prefs.edit().putString("pref_pin_location", loc).apply() } catch (_: Exception) { }
            },
            onQuality = { setPinSelQuality(it) },
            onAudio = { setPinSelAudio(it) },
            onSubs = { setPinSelSubs(it) },
            onEst = { setPinEst(it) },
            onConfirm = onConfirmPin,
            onDismiss = { setPinConfirm(null) }
        )
    }
    // long-press video options: play now / play next / queue end / add to playlist
    optionsFor?.let { v ->
        com.mohamedadel.mytube.ui.queue.VideoOptionsDialog(
            video = v,
            onPlayNow = { onPlayNow(v); setOptionsFor(null) },
            onPlayNext = { onPlayNext(v); setOptionsFor(null) },
            onQueueEnd = { onQueueEnd(v); setOptionsFor(null) },
            onAddToPlaylist = { setPickFor(v); setOptionsFor(null) },
            onPin = { onPinToggle(v); setOptionsFor(null) },
            onQuickPin = { onQuickPin(v); setOptionsFor(null) },
            isPinned = isPinned(v),
            isPinning = isPinning(v),
            onSubscribe = if (v.channelUrl != null) ({ onSubscribeVideo(v); setOptionsFor(null) }) else null,
            onShare = { onShare(v); setOptionsFor(null) },
            onStartRadio = { onStartRadio(v); setOptionsFor(null) },
            isSubscribed = isSubscribed(v),
            onDismiss = { setOptionsFor(null) }
        )
    }
    // Deferred item 6: update dialog — version + changelog + Download (was toast-only).
    updateInfo?.let { rel ->
        AlertDialog(
            onDismissRequest = { setUpdateInfo(null) },
            title = { Text(stringResource(R.string.update_available_fmt, rel.tag, com.mohamedadel.mytube.BuildConfig.VERSION_NAME)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        (rel.body?.take(2000)?.ifBlank { rel.tag } ?: rel.tag),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    try {
                        val url = rel.htmlUrl
                        if (!url.isNullOrBlank()) {
                            val view = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                            view.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            ctx.startActivity(view)
                        } else uiToast(ctx.getString(R.string.update_fail))
                    } catch (_: Exception) { uiToast(ctx.getString(R.string.update_fail)) }
                    setUpdateInfo(null)
                }) { Text(stringResource(R.string.download)) }
            },
            dismissButton = {
                TextButton(onClick = { setUpdateInfo(null) }) { Text(stringResource(R.string.close)) }
            }
        )
    }
}
