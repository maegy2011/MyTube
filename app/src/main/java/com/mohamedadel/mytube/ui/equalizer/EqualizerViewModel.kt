package com.mohamedadel.mytube.ui.equalizer

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.mohamedadel.mytube.playback.EqManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase 2.3 slice 13 (2026-11-13): equalizer state extracted from the
 * God-Activity. EqManager is a process-static; prefs injected via
 * configure(). Player-attach and track-select call sites forward to
 * applyEqPrefs(). Every function <= 30 lines.
 */
class EqualizerViewModel : ViewModel() {

    private var prefs: SharedPreferences? = null
    fun configure(p: SharedPreferences) {
        if (prefs != null) return
        prefs = p
    }

    private val _eqSupported = MutableStateFlow(false)
    val eqSupported: StateFlow<Boolean> = _eqSupported.asStateFlow()
    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()
    private val _eqPreset = MutableStateFlow(0)
    val eqPreset: StateFlow<Int> = _eqPreset.asStateFlow()
    private val _eqBands = MutableStateFlow(0)
    private val _eqLabels = MutableStateFlow<List<String>>(emptyList())
    val eqLabels: StateFlow<List<String>> = _eqLabels.asStateFlow()
    private val _eqPresetNames = MutableStateFlow<List<String>>(emptyList())
    val eqPresetNames: StateFlow<List<String>> = _eqPresetNames.asStateFlow()
    private val _eqCustom = MutableStateFlow<List<Float>>(emptyList())
    val eqCustom: StateFlow<List<Float>> = _eqCustom.asStateFlow()

    private fun fmtFreq(hz: Int): String =
        if (hz <= 0) "?" else if (hz < 1000) "$hz Hz" else "${hz / 1000}.${(hz % 1000) / 100} kHz"

    fun loadFromPrefs() {
        _eqEnabled.value = try { prefs?.getBoolean("eq_enabled", false) } catch (_: Exception) { false } ?: false
        _eqPreset.value = try { prefs?.getInt("eq_preset", 0) } catch (_: Exception) { 0 } ?: 0
    }

    fun applyEqPrefs() {
        try {
            val e = EqManager
            if (!e.supported) { _eqSupported.value = false; return }
            val n = e.bandCount()
            if (n <= 0) { _eqSupported.value = false; return }
            _eqSupported.value = true
            if (_eqBands.value != n) discoverBands(e, n)
            applyBands(e, n)
            e.setEnabled(_eqEnabled.value)
        } catch (_: Exception) { }
    }

    private fun discoverBands(e: EqManager, n: Int) {
        _eqBands.value = n
        _eqLabels.value = (0 until n).map { fmtFreq(e.centerFreqHz(it)) }
        _eqPresetNames.value = e.presets()
        if (_eqCustom.value.size != n) {
            val saved = try { prefs?.getString("eq_custom", "") ?: "" } catch (_: Exception) { "" }
                .split(",").mapNotNull { it.toFloatOrNull() }
            _eqCustom.value = (0 until n).map { b ->
                saved.getOrNull(b)?.coerceIn(0f, 1f) ?: e.toSlider(e.bandLevel(b))
            }
        }
    }

    private fun applyBands(e: EqManager, n: Int) {
        val preset = _eqPreset.value
        if (preset >= 0) e.usePreset(preset)
        else {
            val custom = _eqCustom.value
            for (b in 0 until n) e.setBand(b, e.toMillibels(custom.getOrNull(b) ?: 0.5f))
        }
    }

    fun setEnabled(on: Boolean) {
        _eqEnabled.value = on
        try { prefs?.edit()?.putBoolean("eq_enabled", on)?.apply() } catch (_: Exception) { }
        try { applyEqPrefs() } catch (_: Exception) { }
    }

    fun setPreset(i: Int) {
        _eqPreset.value = i
        try { prefs?.edit()?.putInt("eq_preset", i)?.apply() } catch (_: Exception) { }
        try { applyEqPrefs() } catch (_: Exception) { }
    }

    fun setBand(b: Int, v: Float) {
        try {
            val cur = _eqCustom.value.toMutableList()
            while (cur.size <= b) cur.add(0.5f)
            cur[b] = v.coerceIn(0f, 1f)
            _eqCustom.value = cur
            if (_eqPreset.value != EqManager.CUSTOM) {
                _eqPreset.value = EqManager.CUSTOM
                prefs?.edit()?.putInt("eq_preset", EqManager.CUSTOM)?.apply()
            }
            prefs?.edit()?.putString("eq_custom", cur.joinToString(","))?.apply()
            applyEqPrefs()
        } catch (_: Exception) { }
    }
}
