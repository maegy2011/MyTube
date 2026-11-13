package com.mohamedadel.mytube.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase 2.3 slice 2 (2026-11-13): all 48 persisted preferences extracted
 * from the God-Activity. State as StateFlows; every setter persists
 * to SharedPreferences immediately (same as before). Every function <= 30 lines.
 */
class SettingsViewModel : ViewModel() {

    private val _prefContentFilter = MutableStateFlow<String>("all")
    private val _prefSubLang = MutableStateFlow<String>("auto")
    private val _prefAudioLang = MutableStateFlow<String>("auto")
    private val _prefSubLang2 = MutableStateFlow<String>("en")
    private val _prefAudioLang2 = MutableStateFlow<String>("original")
    private val _prefCountry = MutableStateFlow<String>("EG")
    private val _prefTheme = MutableStateFlow<String>("dark")
    private val _prefAccent = MutableStateFlow<String>("red")
    private val _prefPip = MutableStateFlow<Boolean>(true)
    private val _prefMiniEnabled = MutableStateFlow<Boolean>(true)
    private val _prefMiniAutoshow = MutableStateFlow<Boolean>(true)
    private val _prefMiniSwipe = MutableStateFlow<Boolean>(true)
    private val _prefAf = MutableStateFlow<String>("all")
    private val _prefSb = MutableStateFlow<Boolean>(false)
    private val _prefUiLang = MutableStateFlow<String>("ar")
    private val _prefQuality = MutableStateFlow<String>("audio-only")
    private val _prefAutoplay = MutableStateFlow<Boolean>(true)
    private val _prefSubHideAuto = MutableStateFlow<Boolean>(false)
    private val _prefSubFg = MutableStateFlow<String>("white")
    private val _prefSubBg = MutableStateFlow<String>("transparent")
    private val _prefSubEdge = MutableStateFlow<String>("outline")
    private val _prefSubScale = MutableStateFlow<Float>(1.0f)
    private val _prefSubBgOpacity = MutableStateFlow<Int>(80)
    private val _prefSubBold = MutableStateFlow<Boolean>(false)
    private val _prefPreferOriginal = MutableStateFlow<Boolean>(false)
    private val _prefSubRole = MutableStateFlow<String>("subtitle")
    private val _prefHideForced = MutableStateFlow<Boolean>(false)
    private val _prefUndetermined = MutableStateFlow<Boolean>(true)
    private val _prefAudioRole = MutableStateFlow<String>("auto")
    private val _prefSkipSilence = MutableStateFlow<Boolean>(false)
    private val _prefCacheMaxMb = MutableStateFlow<Long>(500L)
    private val _prefOffline = MutableStateFlow<Boolean>(false)
    private val _prefPinWarnMb = MutableStateFlow<Long>(1024L)
    private val _prefFeedParallel = MutableStateFlow<Boolean>(true)
    private val _prefFeedCache = MutableStateFlow<Boolean>(true)
    private val _prefFeedAuto = MutableStateFlow<Boolean>(false)
    private val _prefPreloadNext = MutableStateFlow<Boolean>(true)
    private val _prefPreloadWifiOnly = MutableStateFlow<Boolean>(true)
    private val _prefTitleTranslate = MutableStateFlow<Boolean>(true)
    private val _prefSpeed = MutableStateFlow<Float>(1.0f)
    private val _prefPitch = MutableStateFlow<Float>(1.0f)
    private val _prefKeepalive = MutableStateFlow<Boolean>(false)
    private val _prefAgeMode = MutableStateFlow<String>("block")
    private val _prefShowComments = MutableStateFlow<Boolean>(true)
    private val _prefShowRelated = MutableStateFlow<Boolean>(true)
    private val _prefShowInfo = MutableStateFlow<Boolean>(true)
    private val _prefShowSubs = MutableStateFlow<Boolean>(true)
    private val _prefAllow4k = MutableStateFlow<Boolean>(true)
    val prefContentFilter: StateFlow<String> = _prefContentFilter.asStateFlow()
    val prefSubLang: StateFlow<String> = _prefSubLang.asStateFlow()
    val prefAudioLang: StateFlow<String> = _prefAudioLang.asStateFlow()
    val prefSubLang2: StateFlow<String> = _prefSubLang2.asStateFlow()
    val prefAudioLang2: StateFlow<String> = _prefAudioLang2.asStateFlow()
    val prefCountry: StateFlow<String> = _prefCountry.asStateFlow()
    val prefTheme: StateFlow<String> = _prefTheme.asStateFlow()
    val prefAccent: StateFlow<String> = _prefAccent.asStateFlow()
    val prefPip: StateFlow<Boolean> = _prefPip.asStateFlow()
    val prefMiniEnabled: StateFlow<Boolean> = _prefMiniEnabled.asStateFlow()
    val prefMiniAutoshow: StateFlow<Boolean> = _prefMiniAutoshow.asStateFlow()
    val prefMiniSwipe: StateFlow<Boolean> = _prefMiniSwipe.asStateFlow()
    val prefAf: StateFlow<String> = _prefAf.asStateFlow()
    val prefSb: StateFlow<Boolean> = _prefSb.asStateFlow()
    val prefUiLang: StateFlow<String> = _prefUiLang.asStateFlow()
    val prefQuality: StateFlow<String> = _prefQuality.asStateFlow()
    val prefAutoplay: StateFlow<Boolean> = _prefAutoplay.asStateFlow()
    val prefSubHideAuto: StateFlow<Boolean> = _prefSubHideAuto.asStateFlow()
    val prefSubFg: StateFlow<String> = _prefSubFg.asStateFlow()
    val prefSubBg: StateFlow<String> = _prefSubBg.asStateFlow()
    val prefSubEdge: StateFlow<String> = _prefSubEdge.asStateFlow()
    val prefSubScale: StateFlow<Float> = _prefSubScale.asStateFlow()
    val prefSubBgOpacity: StateFlow<Int> = _prefSubBgOpacity.asStateFlow()
    val prefSubBold: StateFlow<Boolean> = _prefSubBold.asStateFlow()
    val prefPreferOriginal: StateFlow<Boolean> = _prefPreferOriginal.asStateFlow()
    val prefSubRole: StateFlow<String> = _prefSubRole.asStateFlow()
    val prefHideForced: StateFlow<Boolean> = _prefHideForced.asStateFlow()
    val prefUndetermined: StateFlow<Boolean> = _prefUndetermined.asStateFlow()
    val prefAudioRole: StateFlow<String> = _prefAudioRole.asStateFlow()
    val prefSkipSilence: StateFlow<Boolean> = _prefSkipSilence.asStateFlow()
    val prefCacheMaxMb: StateFlow<Long> = _prefCacheMaxMb.asStateFlow()
    val prefOffline: StateFlow<Boolean> = _prefOffline.asStateFlow()
    val prefPinWarnMb: StateFlow<Long> = _prefPinWarnMb.asStateFlow()
    val prefFeedParallel: StateFlow<Boolean> = _prefFeedParallel.asStateFlow()
    val prefFeedCache: StateFlow<Boolean> = _prefFeedCache.asStateFlow()
    val prefFeedAuto: StateFlow<Boolean> = _prefFeedAuto.asStateFlow()
    val prefPreloadNext: StateFlow<Boolean> = _prefPreloadNext.asStateFlow()
    val prefPreloadWifiOnly: StateFlow<Boolean> = _prefPreloadWifiOnly.asStateFlow()
    val prefTitleTranslate: StateFlow<Boolean> = _prefTitleTranslate.asStateFlow()
    val prefSpeed: StateFlow<Float> = _prefSpeed.asStateFlow()
    val prefPitch: StateFlow<Float> = _prefPitch.asStateFlow()
    val prefKeepalive: StateFlow<Boolean> = _prefKeepalive.asStateFlow()
    val prefAgeMode: StateFlow<String> = _prefAgeMode.asStateFlow()
    val prefShowComments: StateFlow<Boolean> = _prefShowComments.asStateFlow()
    val prefShowRelated: StateFlow<Boolean> = _prefShowRelated.asStateFlow()
    val prefShowInfo: StateFlow<Boolean> = _prefShowInfo.asStateFlow()
    val prefShowSubs: StateFlow<Boolean> = _prefShowSubs.asStateFlow()
    val prefAllow4k: StateFlow<Boolean> = _prefAllow4k.asStateFlow()

    private var prefs: SharedPreferences? = null

    private fun persistString(f: MutableStateFlow<String>, v: String, key: String) { f.value = v; try { prefs?.edit()?.putString(key, v)?.apply() } catch (_: Exception) { } }
    private fun persistBoolean(f: MutableStateFlow<Boolean>, v: Boolean, key: String) { f.value = v; try { prefs?.edit()?.putBoolean(key, v)?.apply() } catch (_: Exception) { } }
    private fun persistLong(f: MutableStateFlow<Long>, v: Long, key: String) { f.value = v; try { prefs?.edit()?.putLong(key, v)?.apply() } catch (_: Exception) { } }
    private fun persistFloat(f: MutableStateFlow<Float>, v: Float, key: String) { f.value = v; try { prefs?.edit()?.putFloat(key, v)?.apply() } catch (_: Exception) { } }
    private fun persistInt(f: MutableStateFlow<Int>, v: Int, key: String) { f.value = v; try { prefs?.edit()?.putInt(key, v)?.apply() } catch (_: Exception) { } }

    fun configure(p: SharedPreferences) {
        if (prefs != null) return
        prefs = p
        loadStringsA(p)
        loadStringsB(p)
        loadBoolsA(p)
        loadBoolsB(p)
        loadNumbers(p)
    }

    private fun loadStringsA(p: SharedPreferences) {
        try { _prefContentFilter.value = p.getString("pref_content_filter", "all") ?: "all"
        } catch (_: Exception) { }
        try { _prefSubLang.value = p.getString("pref_sub_lang", "auto") ?: "auto"
        } catch (_: Exception) { }
        try { _prefAudioLang.value = p.getString("pref_audio_lang", "auto") ?: "auto"
        } catch (_: Exception) { }
        try { _prefSubLang2.value = p.getString("pref_sub_lang2", "en") ?: "en"
        } catch (_: Exception) { }
        try { _prefAudioLang2.value = p.getString("pref_audio_lang2", "original") ?: "original"
        } catch (_: Exception) { }
        try { _prefCountry.value = p.getString("pref_country", "EG") ?: "EG"
        } catch (_: Exception) { }
        try { _prefTheme.value = p.getString("pref_theme", "dark") ?: "dark"
        } catch (_: Exception) { }
        try { _prefAccent.value = p.getString("pref_accent", "red") ?: "red"
        } catch (_: Exception) { }
        try { _prefAf.value = try { p.getString("pref_audiofocus", "all") ?: "all" } catch (_: Exception) { "all" }
        } catch (_: Exception) { }
    }

    private fun loadStringsB(p: SharedPreferences) {
        try { _prefUiLang.value = p.getString("pref_ui_lang", "ar") ?: "ar"
        } catch (_: Exception) { }
        try { _prefQuality.value = p.getString("pref_quality", "audio-only") ?: "audio-only"
        } catch (_: Exception) { }
        try { _prefSubFg.value = p.getString("pref_sub_fg", "white") ?: "white"
        } catch (_: Exception) { }
        try { _prefSubBg.value = p.getString("pref_sub_bg", "transparent") ?: "transparent"
        } catch (_: Exception) { }
        try { _prefSubEdge.value = p.getString("pref_sub_edge", "outline") ?: "outline"
        } catch (_: Exception) { }
        try { _prefSubRole.value = p.getString("pref_sub_role", "subtitle") ?: "subtitle"
        } catch (_: Exception) { }
        try { _prefAudioRole.value = p.getString("pref_audio_role", "auto") ?: "auto"
        } catch (_: Exception) { }
        try { _prefAgeMode.value = p.getString("pref_age_mode", "block") ?: "block"
        } catch (_: Exception) { }
    }


    private fun loadBoolsA(p: SharedPreferences) {
        try { _prefPip.value = try { p.getBoolean("pref_pip", true) } catch (_: Exception) { true }
        } catch (_: Exception) { }
        try { _prefMiniEnabled.value = try { p.getBoolean("pref_mini_enabled", true) } catch (_: Exception) { true }
        } catch (_: Exception) { }
        try { _prefMiniAutoshow.value = try { p.getBoolean("pref_mini_autoshow", true) } catch (_: Exception) { true }
        } catch (_: Exception) { }
        try { _prefMiniSwipe.value = try { p.getBoolean("pref_mini_swipe", true) } catch (_: Exception) { true }
        } catch (_: Exception) { }
        try { _prefSb.value = try { p.getBoolean("pref_sponsorblock", false) } catch (_: Exception) { false }
        } catch (_: Exception) { }
        try { _prefAutoplay.value = try { p.getBoolean("pref_autoplay", true) } catch (_: Exception) { true }
        } catch (_: Exception) { }
        try { _prefSubHideAuto.value = p.getBoolean("pref_sub_hide_auto", false)
        } catch (_: Exception) { }
        try { _prefSubBold.value = p.getBoolean("pref_sub_bold", false)
        } catch (_: Exception) { }
        try { _prefPreferOriginal.value = p.getBoolean("pref_prefer_original", false)
        } catch (_: Exception) { }
        try { _prefHideForced.value = p.getBoolean("pref_hide_forced", false)
        } catch (_: Exception) { }
        try { _prefUndetermined.value = p.getBoolean("pref_undetermined", true)
        } catch (_: Exception) { }
        try { _prefSkipSilence.value = p.getBoolean("pref_skip_silence", false)
        } catch (_: Exception) { }
    }

    private fun loadBoolsB(p: SharedPreferences) {
        try { _prefOffline.value = p.getBoolean("pref_offline", false)
        } catch (_: Exception) { }
        try { _prefFeedParallel.value = p.getBoolean("pref_feed_parallel", true)
        } catch (_: Exception) { }
        try { _prefFeedCache.value = p.getBoolean("pref_feed_cache", true)
        } catch (_: Exception) { }
        try { _prefFeedAuto.value = try { p.getBoolean("pref_feed_auto", false) } catch (_: Exception) { false }
        } catch (_: Exception) { }
        try { _prefPreloadNext.value = p.getBoolean("pref_preload_next", true)
        } catch (_: Exception) { }
        try { _prefPreloadWifiOnly.value = p.getBoolean("pref_preload_wifi_only", true)
        } catch (_: Exception) { }
        try { _prefTitleTranslate.value = p.getBoolean("pref_title_translate", true)
        } catch (_: Exception) { }
        try { _prefKeepalive.value = p.getBoolean("pref_keepalive", false)
        } catch (_: Exception) { }
        try { _prefShowComments.value = p.getBoolean("pref_show_comments", true)
        } catch (_: Exception) { }
        try { _prefShowRelated.value = p.getBoolean("pref_show_related", true)
        } catch (_: Exception) { }
        try { _prefShowInfo.value = p.getBoolean("pref_show_info", true)
        } catch (_: Exception) { }
        try { _prefShowSubs.value = p.getBoolean("pref_show_subtitles", true)
        } catch (_: Exception) { }
        try { _prefAllow4k.value = p.getBoolean("pref_allow_4k", true)
        } catch (_: Exception) { }
    }

    private fun loadNumbers(p: SharedPreferences) {
        try { _prefCacheMaxMb.value = p.getLong("pref_cache_max_mb", 500L).takeIf { it > 0 } ?: 500L
        } catch (_: Exception) { }
        try { _prefPinWarnMb.value = p.getLong("pref_pin_warn_mb", 1024L)
        } catch (_: Exception) { }
        try { _prefSubScale.value = try {
                        if (p.contains("pref_sub_scale")) p.getFloat("pref_sub_scale", 1.0f).coerceIn(0.5f, 2.0f)
                        else when (p.getString("pref_sub_size", "m")) { "s" -> 0.8f; "l" -> 1.25f; "xl" -> 1.5f; else -> 1.0f }
                    } catch (_: Exception) { 1.0f }
        } catch (_: Exception) { }
        try { _prefSpeed.value = p.getFloat("pref_speed", 1.0f)
        } catch (_: Exception) { }
        try { _prefPitch.value = p.getFloat("pref_pitch", 1.0f)
        } catch (_: Exception) { }
        try { _prefSubBgOpacity.value = try { p.getInt("pref_sub_bg_opacity", 80).coerceIn(0, 100) } catch (_: Exception) { 80 }
        } catch (_: Exception) { }
    }

    fun setPrefContentFilter(v: String) = persistString(_prefContentFilter, v, "pref_content_filter")
    fun setPrefSubLang(v: String) = persistString(_prefSubLang, v, "pref_sub_lang")
    fun setPrefAudioLang(v: String) = persistString(_prefAudioLang, v, "pref_audio_lang")
    fun setPrefSubLang2(v: String) = persistString(_prefSubLang2, v, "pref_sub_lang2")
    fun setPrefAudioLang2(v: String) = persistString(_prefAudioLang2, v, "pref_audio_lang2")
    fun setPrefCountry(v: String) = persistString(_prefCountry, v, "pref_country")
    fun setPrefTheme(v: String) = persistString(_prefTheme, v, "pref_theme")
    fun setPrefAccent(v: String) = persistString(_prefAccent, v, "pref_accent")
    fun setPrefPip(v: Boolean) = persistBoolean(_prefPip, v, "pref_pip")
    fun setPrefMiniEnabled(v: Boolean) = persistBoolean(_prefMiniEnabled, v, "pref_mini_enabled")
    fun setPrefMiniAutoshow(v: Boolean) = persistBoolean(_prefMiniAutoshow, v, "pref_mini_autoshow")
    fun setPrefMiniSwipe(v: Boolean) = persistBoolean(_prefMiniSwipe, v, "pref_mini_swipe")
    fun setPrefAf(v: String) = persistString(_prefAf, v, "pref_audiofocus")
    fun setPrefSb(v: Boolean) = persistBoolean(_prefSb, v, "pref_sponsorblock")
    fun setPrefUiLang(v: String) = persistString(_prefUiLang, v, "pref_ui_lang")
    fun setPrefQuality(v: String) = persistString(_prefQuality, v, "pref_quality")
    fun setPrefAutoplay(v: Boolean) = persistBoolean(_prefAutoplay, v, "pref_autoplay")
    fun setPrefSubHideAuto(v: Boolean) = persistBoolean(_prefSubHideAuto, v, "pref_sub_hide_auto")
    fun setPrefSubFg(v: String) = persistString(_prefSubFg, v, "pref_sub_fg")
    fun setPrefSubBg(v: String) = persistString(_prefSubBg, v, "pref_sub_bg")
    fun setPrefSubEdge(v: String) = persistString(_prefSubEdge, v, "pref_sub_edge")
    fun setPrefSubScale(v: Float) { _prefSubScale.value = v; try { prefs?.edit()?.putFloat("pref_sub_scale", v.coerceIn(0.5f, 2.0f))?.apply() } catch (_: Exception) { } }
    fun setPrefSubBgOpacity(v: Int) { val c = v.coerceIn(0, 100); _prefSubBgOpacity.value = c; try { prefs?.edit()?.putInt("pref_sub_bg_opacity", c)?.apply() } catch (_: Exception) { } }
    fun setPrefSubBold(v: Boolean) = persistBoolean(_prefSubBold, v, "pref_sub_bold")
    fun setPrefPreferOriginal(v: Boolean) = persistBoolean(_prefPreferOriginal, v, "pref_prefer_original")
    fun setPrefSubRole(v: String) = persistString(_prefSubRole, v, "pref_sub_role")
    fun setPrefHideForced(v: Boolean) = persistBoolean(_prefHideForced, v, "pref_hide_forced")
    fun setPrefUndetermined(v: Boolean) = persistBoolean(_prefUndetermined, v, "pref_undetermined")
    fun setPrefAudioRole(v: String) = persistString(_prefAudioRole, v, "pref_audio_role")
    fun setPrefSkipSilence(v: Boolean) = persistBoolean(_prefSkipSilence, v, "pref_skip_silence")
    fun setPrefCacheMaxMb(v: Long) = persistLong(_prefCacheMaxMb, v, "pref_cache_max_mb")
    fun setPrefOffline(v: Boolean) = persistBoolean(_prefOffline, v, "pref_offline")
    fun setPrefPinWarnMb(v: Long) = persistLong(_prefPinWarnMb, v, "pref_pin_warn_mb")
    fun setPrefFeedParallel(v: Boolean) = persistBoolean(_prefFeedParallel, v, "pref_feed_parallel")
    fun setPrefFeedCache(v: Boolean) = persistBoolean(_prefFeedCache, v, "pref_feed_cache")
    fun setPrefFeedAuto(v: Boolean) = persistBoolean(_prefFeedAuto, v, "pref_feed_auto")
    fun setPrefPreloadNext(v: Boolean) = persistBoolean(_prefPreloadNext, v, "pref_preload_next")
    fun setPrefPreloadWifiOnly(v: Boolean) = persistBoolean(_prefPreloadWifiOnly, v, "pref_preload_wifi_only")
    fun setPrefTitleTranslate(v: Boolean) = persistBoolean(_prefTitleTranslate, v, "pref_title_translate")
    fun setPrefSpeed(v: Float) = persistFloat(_prefSpeed, v, "pref_speed")
    fun setPrefPitch(v: Float) = persistFloat(_prefPitch, v, "pref_pitch")
    fun setPrefKeepalive(v: Boolean) = persistBoolean(_prefKeepalive, v, "pref_keepalive")
    fun setPrefAgeMode(v: String) = persistString(_prefAgeMode, v, "pref_age_mode")
    fun setPrefShowComments(v: Boolean) = persistBoolean(_prefShowComments, v, "pref_show_comments")
    fun setPrefShowRelated(v: Boolean) = persistBoolean(_prefShowRelated, v, "pref_show_related")
    fun setPrefShowInfo(v: Boolean) = persistBoolean(_prefShowInfo, v, "pref_show_info")
    fun setPrefShowSubs(v: Boolean) = persistBoolean(_prefShowSubs, v, "pref_show_subtitles")
    fun setPrefAllow4k(v: Boolean) = persistBoolean(_prefAllow4k, v, "pref_allow_4k")
}
