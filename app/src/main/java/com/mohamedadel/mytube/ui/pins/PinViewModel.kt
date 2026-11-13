package com.mohamedadel.mytube.ui.pins

import android.app.Application
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.PinDownloadRunner
import com.mohamedadel.mytube.data.local.PinFiles
import com.mohamedadel.mytube.data.local.PinManager
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.PinRetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Phase 2.3 slice 15 (2026-11-13): offline-pin registry + runner wiring
 * extracted from the God-Activity. The resolve-and-plan fns
 * (requestPin/quickPin/confirmPin/openPinned) stay with the caller until
 * slice 16 — they read live player/picker state. Host carries the
 * activity-owned callbacks (toasts, net, player, labels).
 * Every function <= 30 lines.
 */
interface PinHost {
    fun uiToast(s: String)
    fun kbps(): Int
    fun allow4k(): Boolean
    fun playingKey(): String?
    fun closePlayer()
    fun refreshPinsLabel()
    fun lastPinQuality(): String?
    fun lastPinSubs(): Set<Int>
    fun rememberPinChoices(q: String, subs: Set<Int>)
}

class PinViewModel(app: Application) : AndroidViewModel(app) {

    private var dao: com.mohamedadel.mytube.data.local.PlaylistDao? = null
    private var prefs: SharedPreferences? = null
    fun configure(d: com.mohamedadel.mytube.data.local.PlaylistDao, p: SharedPreferences) {
        if (dao != null) return
        dao = d
        prefs = p
    }

    private fun str(resId: Int, vararg args: Any): String =
        try { getApplication<Application>().getString(resId, *args) } catch (_: Exception) { "" }

    private suspend fun toastMain(s: String) = withContext(Dispatchers.Main) {
        try { Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }

    private val _pinningKeys = MutableStateFlow(setOf<String>())
    val pinningKeys: StateFlow<Set<String>> = _pinningKeys.asStateFlow()
    private val _pausedKeys = MutableStateFlow(setOf<String>())
    val pausedKeys: StateFlow<Set<String>> = _pausedKeys.asStateFlow()
    val pinProgress = MutableStateFlow(mapOf<String, Float>())
    private val _pinConfirm = MutableStateFlow<PinManager.PinRequest?>(null)
    val pinConfirm: StateFlow<PinManager.PinRequest?> = _pinConfirm.asStateFlow()
    private val _pinSd = MutableStateFlow<StreamData?>(null)
    val pinSd: StateFlow<StreamData?> = _pinSd.asStateFlow()
    private val _pinSelQuality = MutableStateFlow("")
    val pinSelQuality: StateFlow<String> = _pinSelQuality.asStateFlow()
    private val _pinSelAudio = MutableStateFlow(0)
    val pinSelAudio: StateFlow<Int> = _pinSelAudio.asStateFlow()
    private val _pinSelSubs = MutableStateFlow(setOf<Int>())
    val pinSelSubs: StateFlow<Set<Int>> = _pinSelSubs.asStateFlow()
    private val _pinSelLoc = MutableStateFlow("internal")
    val pinSelLoc: StateFlow<String> = _pinSelLoc.asStateFlow()
    private val _pinEst = MutableStateFlow(-1L)
    val pinEst: StateFlow<Long> = _pinEst.asStateFlow()
    private val _orphanInfo = MutableStateFlow(0 to 0L)
    val orphanInfo: StateFlow<Pair<Int, Long>> = _orphanInfo.asStateFlow()

    val pinJobs = ConcurrentHashMap<String, Job>()
    val pinPlans = ConcurrentHashMap<String, Triple<VideoItem, PinManager.PinPlan, PinRetry?>>()

    fun pinSdNow(): StreamData? = _pinSd.value

    fun setPinning(k: String, on: Boolean) {
        _pinningKeys.value = if (on) _pinningKeys.value + k else _pinningKeys.value - k
    }

    fun setConfirm(req: PinManager.PinRequest?, sd: StreamData?, q: String, a: Int, s: Set<Int>, loc: String, est: Long) {
        _pinConfirm.value = req
        _pinSd.value = sd
        _pinSelQuality.value = q
        _pinSelAudio.value = a
        _pinSelSubs.value = s
        _pinSelLoc.value = loc
        _pinEst.value = est
    }

    fun clearConfirm() {
        _pinConfirm.value = null
        _pinSd.value = null
    }

    fun setPinSelLoc(v: String) { _pinSelLoc.value = v }
    fun setPinSelQuality(v: String) { _pinSelQuality.value = v }
    fun setPinSelAudio(v: Int) { _pinSelAudio.value = v }
    fun setPinSelSubs(v: Set<Int>) { _pinSelSubs.value = v }
    fun setPinEst(v: Long) { _pinEst.value = v }
    fun setPinConfirm(v: PinManager.PinRequest?) { _pinConfirm.value = v }

    fun setProgress(k: String, f: Float?) {
        pinProgress.value = if (f == null) pinProgress.value - k else pinProgress.value + (k to f)
    }

    private fun deps(host: PinHost): PinDownloadRunner.Deps =
        PinDownloadRunner.Deps(
            scope = viewModelScope, act = getApplication(), app = getApplication(), dao = dao!!, prefs = prefs!!,
            pinJobs = pinJobs, pinPlans = pinPlans,
            pinSelLoc = _pinSelLoc.value, allow4k = host.allow4k(),
            kbps = { host.kbps() },
            uiToast = { m -> host.uiToast(m) },
            isPinning = { k -> _pinningKeys.value.contains(k) },
            addPinning = { k -> setPinning(k, true) },
            removePinning = { k -> setPinning(k, false) },
            isPausedKey = { k -> _pausedKeys.value.contains(k) },
            addPaused = { k -> _pausedKeys.value = _pausedKeys.value + k },
            removePaused = { k -> _pausedKeys.value = _pausedKeys.value - k },
            setProgress = { k, f -> setProgress(k, f) }
        )

    fun runPinDownload(v: VideoItem, plan: PinManager.PinPlan, key: String, host: PinHost, alreadyAdded: Boolean = false, retry: PinRetry? = null, resume: Boolean = false) =
        PinDownloadRunner.run(v, plan, key, alreadyAdded, retry, resume, deps(host))

    fun pausePinDownload(key: String, host: PinHost) =
        PinDownloadRunner.pause(key, deps(host))

    fun resumePinDownload(key: String, host: PinHost) =
        PinDownloadRunner.resume(key, deps(host))

    fun cancelPinDownload(key: String, host: PinHost) =
        PinDownloadRunner.stop(key, deps(host))

    fun refreshOrphans(knownKeys: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _orphanInfo.value = PinFiles.externalOrphans(getApplication(), knownKeys)
            } catch (_: Exception) { }
        }
    }

    fun cleanOrphans(host: PinHost, knownKeys: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val n = PinFiles.cleanExternalOrphans(getApplication(), knownKeys)
                refreshOrphans(knownKeys)
                host.uiToast(str(R.string.pin_orphans_cleaned_fmt, n))
            } catch (_: Exception) { host.uiToast(str(R.string.toast_pin_fail)) }
        }
    }

    fun clearAllPins(host: PinHost) {
        try {
            val cur = host.playingKey()
            if (cur != null && PinFiles.isPinned(getApplication(), cur)) host.closePlayer()
        } catch (_: Exception) { }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                PinFiles.clearAll(getApplication())
                try { dao?.clearPins() } catch (_: Exception) { }
                host.refreshPinsLabel()
                host.uiToast(str(R.string.toast_pins_cleared))
            } catch (_: Exception) { host.uiToast(str(R.string.toast_pin_fail)) }
        }
    }

    fun doUnpin(key: String, host: PinHost) {
        if (_pinningKeys.value.contains(key)) { host.uiToast(str(R.string.pinning)); return }
        try { if (host.playingKey() == key) host.closePlayer() } catch (_: Exception) { }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val d = dao ?: return@launch
                PinManager.unpin(getApplication(), d, key)
                host.uiToast(str(R.string.toast_unpinned))
            } catch (_: Exception) { host.uiToast(str(R.string.toast_pin_fail)) }
        }
    }
}
