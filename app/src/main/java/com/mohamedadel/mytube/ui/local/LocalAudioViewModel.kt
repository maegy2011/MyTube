package com.mohamedadel.mytube.ui.local

import android.app.Application
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.util.LocalTrack
import com.mohamedadel.mytube.util.scanLocalAudio
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 2.3 slice 10 (2026-11-13): on-device audio library extracted from
 * the God-Activity. The permission launcher itself stays in the activity
 * (composable API) and forwards via onPermissionResult().
 * Every function <= 30 lines.
 */
class LocalAudioViewModel(app: Application) : AndroidViewModel(app) {

    private fun toastMain(resId: Int) {
        try {
            val s = getApplication<Application>().getString(resId)
            viewModelScope.launch(Dispatchers.Main) {
                try { Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
            }
        } catch (_: Exception) { }
    }

    private val _localTracks = MutableStateFlow<List<LocalTrack>>(emptyList())
    val localTracks: StateFlow<List<LocalTrack>> = _localTracks.asStateFlow()
    private val _localFolder = MutableStateFlow<String?>(null)
    val localFolder: StateFlow<String?> = _localFolder.asStateFlow()
    private val _localExcl = MutableStateFlow(setOf<String>())
    val localExcl: StateFlow<Set<String>> = _localExcl.asStateFlow()
    private val _localCompat = MutableStateFlow(false)
    val localCompat: StateFlow<Boolean> = _localCompat.asStateFlow()
    private val _localHasPerm = MutableStateFlow(false)
    val hasPerm: StateFlow<Boolean> = _localHasPerm.asStateFlow()
    private var prefs: SharedPreferences? = null
    fun configure(p: SharedPreferences) {
        if (prefs != null) return
        prefs = p
    }

    fun setLocalFolder(f: String?) { _localFolder.value = f }

    fun setCompat(c: Boolean) {
        _localCompat.value = c
        try {
            val p = prefs ?: getApplication<Application>().getSharedPreferences("mytube_prefs", Context.MODE_PRIVATE)
            p.edit().putBoolean("local_scan_compat", c).apply()
        } catch (_: Exception) { }
        refreshLocalAudio()
    }

    fun localAudioPerm(): String =
        if (android.os.Build.VERSION.SDK_INT >= 33)
            android.Manifest.permission.READ_MEDIA_AUDIO
        else android.Manifest.permission.READ_EXTERNAL_STORAGE

    fun checkLocalPerm() {
        _localHasPerm.value = try {
            getApplication<Application>().checkSelfPermission(localAudioPerm()) == PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) { false }
    }

    fun onPermissionResult(granted: Boolean) {
        _localHasPerm.value = granted
        if (granted) refreshLocalAudio()
    }

    fun refreshLocalAudio() {
        checkLocalPerm()
        if (!_localHasPerm.value) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                val excl = try { prefs?.getStringSet("local_excl", emptySet()) ?: emptySet() } catch (_: Exception) { emptySet() }
                _localExcl.value = excl
                _localCompat.value = try { prefs?.getBoolean("local_scan_compat", false) } catch (_: Exception) { false } ?: false
                val all = try { ctx.scanLocalAudio(_localCompat.value) } catch (_: Exception) { emptyList() }
                _localTracks.value = all.filter { (it.folder ?: "") !in excl }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { toastMain(R.string.toast_local_fail) }
            }
        }
    }

    fun excludeLocalFolder(f: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = prefs ?: getApplication<Application>().getSharedPreferences("mytube_prefs", Context.MODE_PRIVATE)
                val cur = try { prefs.getStringSet("local_excl", emptySet()) } catch (_: Exception) { emptySet() } ?: emptySet()
                val next = (cur + f).take(50).toSet()
                prefs.edit().putStringSet("local_excl", next).apply()
                _localExcl.value = next
                _localTracks.value = _localTracks.value.filter { (it.folder ?: "") !in next }
                if (_localFolder.value == f) _localFolder.value = null
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { toastMain(R.string.toast_lib_fail) }
            }
        }
    }

    fun unexcludeLocalFolder(f: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val prefs = prefs ?: getApplication<Application>().getSharedPreferences("mytube_prefs", Context.MODE_PRIVATE)
                val cur = try { prefs.getStringSet("local_excl", emptySet()) } catch (_: Exception) { emptySet() } ?: emptySet()
                val next = cur - f
                prefs.edit().putStringSet("local_excl", next).apply()
                _localExcl.value = next
                withContext(Dispatchers.Main) { refreshLocalAudio() }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { toastMain(R.string.toast_lib_fail) }
            }
        }
    }
}
