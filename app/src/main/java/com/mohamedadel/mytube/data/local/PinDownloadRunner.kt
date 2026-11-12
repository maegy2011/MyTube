package com.mohamedadel.mytube.data.local

import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import com.mohamedadel.mytube.PinRetry
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.playback.DownloadActionReceiver
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

// pause/stop 2026-11-12 (runtime layer, Android skill): pin/download job execution.
// Lives here — NOT inline in MainActivity.MyTubeApp — because that composable method
// sits at the 64KB codegen limit and the inline body broke compileDebugKotlin with
// MethodTooLargeException. Behavior contract (unchanged):
// - single pinProgress source (written via setProgress) drives Downloads rows,
//   badges and the notification;
// - PAUSE suspends the OkHttp stream, keeps staging (.part byte counts are the
//   persisted resume offsets), returns PinManager.PAUSED, progress stays frozen,
//   job stays tracked, notification flips to Resume + Stop, silent;
// - STOP cancels the job, wipes the partial file + temp meta (staging), clears every
//   tracking key, dismisses the notification, shows ONLY the cancel toast
//   (no false-failure toast — user-cancelled stays silent per standing rule);
// - RESUME re-enters with resume=true so getTo re-sends Range from kept lengths;
// - audio-only default, progressive only; ar+en strings; theme tokens (UI side).
object PinDownloadRunner {

    // Everything the runner needs from the UI layer, supplied per call so the
    // composable stays a thin forwarder. State is touched only through these.
    class Deps(
        val scope: CoroutineScope,
        val act: Context,
        val app: Context,
        val dao: PlaylistDao,
        val prefs: SharedPreferences,
        val pinJobs: ConcurrentHashMap<String, Job>,
        val pinPlans: ConcurrentHashMap<String, Triple<VideoItem, PinManager.PinPlan, PinRetry?>>,
        val pinSelLoc: String,
        val allow4k: Boolean,
        val kbps: () -> Int,
        val uiToast: (String) -> Unit,
        val isPinning: (String) -> Boolean,
        val addPinning: (String) -> Unit,
        val removePinning: (String) -> Unit,
        val isPausedKey: (String) -> Boolean,
        val addPaused: (String) -> Unit,
        val removePaused: (String) -> Unit,
        val setProgress: (String, Float?) -> Unit
    )

    fun notifId(key: String) = 2000 + (key.hashCode() and 0xFFF)

    fun dismiss(app: Context, key: String) {
        try {
            val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            nm.cancel(notifId(key))
        } catch (_: Exception) { }
    }

    private const val DL_CHANNEL = "mytube_downloads"

    private fun ensureChannel(app: Context, act: Context) {
        try {
            val nm = app.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                nm.createNotificationChannel(
                    android.app.NotificationChannel(DL_CHANNEL, act.getString(R.string.dl_channel), android.app.NotificationManager.IMPORTANCE_LOW)
                )
            }
        } catch (_: Exception) { }
    }

    private fun progressNotif(d: Deps, key: String, title: String?, frac: Float): Notification =
        NotificationCompat.Builder(d.app, DL_CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title ?: d.act.getString(R.string.dl_title))
            .setOngoing(true).setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_pause, d.act.getString(R.string.dl_pause), DownloadActionReceiver.pendingPause(d.app, key))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, d.act.getString(R.string.cancel), DownloadActionReceiver.pendingStop(d.app, key))
            .apply {
                if (frac >= 0) setProgress(100, (frac * 100).toInt(), false)
                    .setContentText(d.act.getString(R.string.dl_progress_fmt, (frac * 100).toInt()))
                else setProgress(0, 0, true).setContentText(d.act.getString(R.string.pinning))
            }
            .build()

    private fun pausedNotif(d: Deps, key: String, title: String?): Notification =
        NotificationCompat.Builder(d.app, DL_CHANNEL)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle(title ?: d.act.getString(R.string.dl_title))
            .setContentText(d.act.getString(R.string.pin_paused))
            .setOngoing(true).setOnlyAlertOnce(true)
            .addAction(android.R.drawable.ic_media_play, d.act.getString(R.string.dl_resume), DownloadActionReceiver.pendingResume(d.app, key))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, d.act.getString(R.string.cancel), DownloadActionReceiver.pendingStop(d.app, key))
            .build()

    private fun notify(nm: android.app.NotificationManager?, id: Int, n: Notification) {
        try { nm?.notify(id, n) } catch (_: Exception) { }
    }

    fun run(v: VideoItem, plan: PinManager.PinPlan, key: String, alreadyAdded: Boolean = false, retry: PinRetry? = null, resume: Boolean = false, d: Deps) {
        // F6: callers that added the key optimistically (quickPin) skip the re-add, not the guard.
        // resume re-enters with the key still tracked — bypass the guard.
        if (!resume && d.isPinning(key) && !alreadyAdded) { d.uiToast(d.act.getString(R.string.pinning)); return }
        if (!resume && !alreadyAdded) d.addPinning(key)
        // stored plan = resume source (no re-resolve; Range continues).
        try { d.pinPlans[key] = Triple(v, plan, retry) } catch (_: Exception) { }
        val title = v.title
        // F5: track the job so DownloadsScreen can cancel it mid-flight (box: the launch
        // body references the job itself, which a plain val cannot do in its own initializer).
        val dlJobBox = arrayOfNulls<Job>(1)
        dlJobBox[0] = d.scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Storage-permission fix: app-specific dirs need NO runtime permission, but the
                // external root can be unmounted/missing — fall back to internal LOUDLY, not silently.
                try {
                    val pf = PinFiles
                    if (pf.isCurrentLocBroken(d.app)) {
                        try { d.prefs.edit().putString("pref_pin_location", "internal").apply() } catch (_: Exception) { }
                        d.uiToast(d.act.getString(R.string.pin_ext_fallback))
                    }
                    // Free-space guard: refuse early with a clear message instead of a mid-write fail.
                    val need = PinManager.estimateBytes(listOf(plan.videoUrl, plan.audioUrl, plan.subUrl) + plan.subs.drop(1).map { it.url })
                    // 2026-11-12 (runtime fix): free bytes on the ACTUAL chosen pin root
                    // (pinSelLoc) — default-root query read 0 on fresh installs (missing
                    // travel dir) and cried "no space" though space exists. -1 = unknown = skip.
                    val free = pf.freeBytes(d.app, d.pinSelLoc)
                    if (need > 0 && free >= 0 && need > free) {
                        d.uiToast(d.act.getString(R.string.pin_no_space)); return@launch
                    }
                } catch (_: Exception) { }
                val app = d.app
                val nm = try { app.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager } catch (_: Exception) { null }
                ensureChannel(app, d.act)
                val notifId = notifId(key)
                // notification carries Pause + Stop actions while running,
                // Resume + Stop while paused (explicit intents, same pattern as PiP).
                val progCb: (Long, Long) -> Unit = { done, total ->
                    val frac = if (total > 0) (done.toFloat() / total).coerceIn(0f, 1f) else -1f
                    try { d.setProgress(key, frac) } catch (_: Exception) { }
                    try { notify(nm, notifId, progressNotif(d, key, title, frac)) } catch (_: Exception) { }
                }
                var size = PinManager.downloadPin(app, d.dao, v, plan, progCb, resume)
                // pin-deepfix: one fresh-resolve retry — signed googlevideo URLs expire, so a stale
                // plan fails even on a good network. Rebuild from a fresh resolve with the same choices.
                // never retry a paused or user-cancelled job.
                if (size < 0 && size != PinManager.PAUSED && retry != null && !PinManager.isCancelRequested(key) && !PinManager.isPaused(key)) {
                    try {
                        val fresh = v.url?.let { YouTubeRepo.streamRouted(it) }
                        if (fresh != null && !fresh.audioHlsOnly &&
                            (fresh.audioLowestUrl != null || fresh.videoUrlByQuality.isNotEmpty())) {
                            val rebuilt = PinManager.buildPlan(
                                fresh, retry.quality, retry.audioIdx, retry.subIdxs,
                                d.kbps(), maxH = if (d.allow4k) Int.MAX_VALUE else 1080)
                            if (rebuilt != null) {
                                try { d.pinPlans[key] = Triple(v, rebuilt, retry) } catch (_: Exception) { }
                                size = PinManager.downloadPin(app, d.dao, v, rebuilt, progCb, resume)
                            }
                        }
                    } catch (_: Exception) { }
                }
                // PAUSED = stream suspended, staging kept for Range resume; progress stays
                // frozen, key stays tracked, notification flips to Resume + Stop. No toast.
                if (size == PinManager.PAUSED) {
                    try { d.addPaused(key) } catch (_: Exception) { }
                    try { notify(nm, notifId, pausedNotif(d, key, title)) } catch (_: Exception) { }
                } else {
                    try { d.setProgress(key, null) } catch (_: Exception) { }
                    try { PinManager.clearPause(key) } catch (_: Exception) { }
                    try { d.removePaused(key) } catch (_: Exception) { }
                    try { d.pinPlans.remove(key) } catch (_: Exception) { }
                    try { nm?.cancel(notifId) } catch (_: Exception) { }
                }
                // pin-deepfix: a user-cancelled pin is not a failure — stay silent
                // (stop already toasted). Clear the consumed flag.
                // STOP wipes staging here as well (covers the job-side path).
                if (size == PinManager.PAUSED) { }
                else if (PinManager.isCancelRequested(key)) {
                    try { PinManager.consumeCancel(key) } catch (_: Exception) { }
                    try { PinManager.deleteStaging(app, key) } catch (_: Exception) { }
                }
                else if (size >= 0) {
                    d.uiToast(d.act.getString(R.string.toast_pin_done))
                    // notif 2026-11-12: terminal success notification, tap opens the player.
                    try { NotifCenter.postPinDone(app, key, title) } catch (_: Exception) { }
                    try { PinManager.clearError(key) } catch (_: Exception) { }
                }
                else {
                    d.uiToast(d.act.getString(R.string.toast_pin_fail))
                    // notif 2026-11-12: terminal failure notification with the real reason.
                    try {
                        val code = PinManager.takeError(key)
                        NotifCenter.postPinFail(app, key, title, code)
                    } catch (_: Exception) { }
                }
            } catch (ce: kotlinx.coroutines.CancellationException) {
                // job-cancel path of stop — silent, then propagate.
                try { d.setProgress(key, null) } catch (_: Exception) { }
                try { PinManager.consumeCancel(key) } catch (_: Exception) { }
                throw ce
            } catch (_: Exception) {
                d.uiToast(d.act.getString(R.string.toast_pin_fail))
                try {
                    val ecode = PinManager.takeError(key)
                    NotifCenter.postPinFail(d.app, key, title, ecode)
                } catch (_: Exception) { }
            }
            // a paused job keeps its tracking key so the row + frozen progress stay
            // visible (resume re-enters with resume=true).
            finally { if (!d.isPausedKey(key)) d.removePinning(key); try { val j = dlJobBox[0]; if (j != null) d.pinJobs.remove(key, j) } catch (_: Exception) { } }
        }
        try { dlJobBox[0]?.let { d.pinJobs[key] = it } } catch (_: Exception) { }
    }

    // PAUSE: suspends the OkHttp stream (flag; the loop exits at the next 256KB chunk),
    // keeps staging (.part byte counts = persisted resume offsets) + frozen progress.
    // Cooperative only — no Job.cancel (that path would wipe progress via
    // CancellationException); the OkHttp loop exits at the next chunk.
    fun pause(key: String, d: Deps) {
        if (!d.isPinning(key) || d.isPausedKey(key)) return
        PinManager.requestPause(key)
        try { d.uiToast(d.act.getString(R.string.pin_paused)) } catch (_: Exception) { }
    }

    // RESUME: re-enters run with resume=true — staging kept, Range continues.
    fun resume(key: String, d: Deps) {
        val t = try { d.pinPlans[key] } catch (_: Exception) { null } ?: return
        // paused keys stay tracked — only block a truly running job.
        if (d.isPinning(key) && !d.isPausedKey(key)) return
        PinManager.clearPause(key)
        try { d.removePaused(key) } catch (_: Exception) { }
        run(t.first, t.second, key, retry = t.third, resume = true, d = d)
    }

    // STOP: aborts the job, wipes the partial file + temp meta (staging), clears every
    // tracking key, dismisses the notification — and shows ONLY the cancel toast.
    fun stop(key: String, d: Deps) {
        PinManager.requestCancel(key)
        PinManager.clearPause(key)
        // STOP clears the tracking key immediately too — a paused job already ended,
        // so no finally will do it (running jobs re-remove harmlessly).
        try { d.removePaused(key) } catch (_: Exception) { }
        try { d.removePinning(key) } catch (_: Exception) { }
        try { d.pinPlans.remove(key) } catch (_: Exception) { }
        val j = try { d.pinJobs.remove(key) } catch (_: Exception) { null }
        try { j?.cancel() } catch (_: Exception) { }
        try {
            d.scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try { j?.join() } catch (_: Exception) { }
                try { PinManager.deleteStaging(d.app, key) } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
        try { d.setProgress(key, null) } catch (_: Exception) { }
        try { dismiss(d.app, key) } catch (_: Exception) { }
        try { d.uiToast(d.act.getString(R.string.cancel)) } catch (_: Exception) { }
    }
}
