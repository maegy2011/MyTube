package com.mohamedadel.mytube.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mohamedadel.mytube.ui.equalizer.EqualizerViewModel
import com.mohamedadel.mytube.ui.library.LibraryViewModel
import com.mohamedadel.mytube.ui.library.PlaylistCsvViewModel
import com.mohamedadel.mytube.ui.player.PlayerViewModel
import com.mohamedadel.mytube.ui.player.mutedLive
import com.mohamedadel.mytube.ui.player.volumeLive
import com.mohamedadel.mytube.ui.search.MusicSearchViewModel

/**
 * Phase 3 slice 34 (2026-11-14): SettingsPane extracted from MainActivity.
 * Snapshots delegate to VM StateFlows; side-effecting actions via host.
 */
interface SettingsPaneHost {
    fun settingsVm(): SettingsViewModel
    fun libVm(): LibraryViewModel
    fun pinVm(): com.mohamedadel.mytube.ui.pins.PinViewModel
    fun eqVm(): EqualizerViewModel
    fun playerVm(): PlayerViewModel
    fun favoriteKeys(): List<String>
    fun playlistsCount(): Int
    fun prefetchedMap(): java.util.concurrent.ConcurrentHashMap<String, com.mohamedadel.mytube.data.youtube.StreamData>
    fun pickAudioIdxNow(): Int
    fun dbSizeLabel(): String
    fun backupInfoText(): String?
    fun extAvailable(): Boolean
    fun strOf(id: Int): String
    fun subLangX(v: String)
    fun audioLangX(v: String)
    fun stageCountry(v: String)
    fun qualityX(v: String)
    fun uiLangX(v: String)
    fun playModeX(v: String)
    fun miniEnabledX(v: Boolean)
    fun afX(v: String)
    fun clearFavoritesX()
    fun clearHistoryX()
    fun copyDiagnosticsX()
    fun checkUpdateX()
    fun exportFullX()
    fun importFullX()
    fun cacheMaxX(mb: Long)
    fun clearCacheX()
    fun keepAliveX(v: Boolean)
    fun clearPinsX()
    fun feedAutoX(v: Boolean)
    fun preloadNextX(v: Boolean)
    fun preferOriginalX(v: Boolean)
    fun skipSilenceX(v: Boolean)
    fun themeX(v: String)
    fun accentX(v: String)
    fun volumeX(v: Float)
    fun mutedX(m: Boolean)
    fun restartNowX()
    fun pendingCountry(): String?
    fun clearPending()
    fun confirmCountryX(v: String)
}

@Composable
fun SettingsPaneHost.SettingsPane() {
    val settings = settingsVm()
    val prefAccent by settings.prefAccent.collectAsStateWithLifecycle()
    val prefAf by settings.prefAf.collectAsStateWithLifecycle()
    val prefAgeMode by settings.prefAgeMode.collectAsStateWithLifecycle()
    val prefAllow4k by settings.prefAllow4k.collectAsStateWithLifecycle()
    val prefAudioLang by settings.prefAudioLang.collectAsStateWithLifecycle()
    val prefAudioLang2 by settings.prefAudioLang2.collectAsStateWithLifecycle()
    val prefAudioRole by settings.prefAudioRole.collectAsStateWithLifecycle()
    val prefAutoplay by settings.prefAutoplay.collectAsStateWithLifecycle()
    val prefCacheMaxMb by settings.prefCacheMaxMb.collectAsStateWithLifecycle()
    val prefCountry by settings.prefCountry.collectAsStateWithLifecycle()
    val prefFeedAuto by settings.prefFeedAuto.collectAsStateWithLifecycle()
    val prefFeedCache by settings.prefFeedCache.collectAsStateWithLifecycle()
    val prefFeedParallel by settings.prefFeedParallel.collectAsStateWithLifecycle()
    val prefHideForced by settings.prefHideForced.collectAsStateWithLifecycle()
    val prefKeepalive by settings.prefKeepalive.collectAsStateWithLifecycle()
    val prefMiniAutoshow by settings.prefMiniAutoshow.collectAsStateWithLifecycle()
    val prefMiniEnabled by settings.prefMiniEnabled.collectAsStateWithLifecycle()
    val prefMiniSwipe by settings.prefMiniSwipe.collectAsStateWithLifecycle()
    val prefOffline by settings.prefOffline.collectAsStateWithLifecycle()
    val prefPinWarnMb by settings.prefPinWarnMb.collectAsStateWithLifecycle()
    val prefPip by settings.prefPip.collectAsStateWithLifecycle()
    val prefPitch by settings.prefPitch.collectAsStateWithLifecycle()
    val prefPreferOriginal by settings.prefPreferOriginal.collectAsStateWithLifecycle()
    val prefPreloadNext by settings.prefPreloadNext.collectAsStateWithLifecycle()
    val prefPreloadWifiOnly by settings.prefPreloadWifiOnly.collectAsStateWithLifecycle()
    val prefQuality by settings.prefQuality.collectAsStateWithLifecycle()
    val prefSb by settings.prefSb.collectAsStateWithLifecycle()
    val prefShowComments by settings.prefShowComments.collectAsStateWithLifecycle()
    val prefShowInfo by settings.prefShowInfo.collectAsStateWithLifecycle()
    val prefShowRelated by settings.prefShowRelated.collectAsStateWithLifecycle()
    val prefShowSubs by settings.prefShowSubs.collectAsStateWithLifecycle()
    val prefSkipSilence by settings.prefSkipSilence.collectAsStateWithLifecycle()
    val prefSpeed by settings.prefSpeed.collectAsStateWithLifecycle()
    val prefSubBg by settings.prefSubBg.collectAsStateWithLifecycle()
    val prefSubBgOpacity by settings.prefSubBgOpacity.collectAsStateWithLifecycle()
    val prefSubBold by settings.prefSubBold.collectAsStateWithLifecycle()
    val prefSubEdge by settings.prefSubEdge.collectAsStateWithLifecycle()
    val prefSubFg by settings.prefSubFg.collectAsStateWithLifecycle()
    val prefSubHideAuto by settings.prefSubHideAuto.collectAsStateWithLifecycle()
    val prefSubLang by settings.prefSubLang.collectAsStateWithLifecycle()
    val prefSubLang2 by settings.prefSubLang2.collectAsStateWithLifecycle()
    val prefSubRole by settings.prefSubRole.collectAsStateWithLifecycle()
    val prefSubScale by settings.prefSubScale.collectAsStateWithLifecycle()
    val prefTheme by settings.prefTheme.collectAsStateWithLifecycle()
    val prefTitleTranslate by settings.prefTitleTranslate.collectAsStateWithLifecycle()
    val prefUiLang by settings.prefUiLang.collectAsStateWithLifecycle()
    val prefUndetermined by settings.prefUndetermined.collectAsStateWithLifecycle()
    val lib = libVm()
    val histCount by lib.histCount.collectAsStateWithLifecycle()
    val pinsCount by lib.pinsCount.collectAsStateWithLifecycle()
    val pinsSizeLabel by lib.pinsSizeLabel.collectAsStateWithLifecycle()
    val pinsBytes by lib.pinsBytes.collectAsStateWithLifecycle()
    val cacheSizeLabel by lib.cacheSizeLabel.collectAsStateWithLifecycle()
    val pinSelLoc by pinVm().pinSelLoc.collectAsStateWithLifecycle()
    val eq = eqVm()
    val eqSupported by eq.eqSupported.collectAsStateWithLifecycle()
    val eqEnabled by eq.eqEnabled.collectAsStateWithLifecycle()
    val eqPresetNames by eq.eqPresetNames.collectAsStateWithLifecycle()
    val eqPreset by eq.eqPreset.collectAsStateWithLifecycle()
    val eqLabels by eq.eqLabels.collectAsStateWithLifecycle()
    val eqCustom by eq.eqCustom.collectAsStateWithLifecycle()
    val playMode by playerVm().playMode.collectAsStateWithLifecycle()
SettingsScreen(
    subLang = prefSubLang,
    audioLang = prefAudioLang,
    subLang2 = prefSubLang2,
    audioLang2 = prefAudioLang2,
    country = prefCountry,
    quality = prefQuality,
    deviceLang = try { java.util.Locale.getDefault() } catch (_: Exception) { null },
    onSubLang = { v -> subLangX(v) },
    onAudioLang = { v -> audioLangX(v) },
    onSubLang2 = { v -> settingsVm().setPrefSubLang2(v) },
    onAudioLang2 = { v -> settingsVm().setPrefAudioLang2(v); playerVm().setAudioTrackIdx(pickAudioIdxNow()) },
    // Geo-switch cache guard (2026-11-12): NEVER apply directly — stage
    // the pick and show the mandatory cache-clear dialog instead.
    onCountry = { v -> if (v != prefCountry) stageCountry(v) },
    onQuality = { v -> qualityX(v) },
    uiLang = prefUiLang,
    onUiLang = { v -> uiLangX(v) },
    playMode = playMode,
    onPlayMode = { v -> playModeX(v) },
    // 2026-11-12 (runtime fix): autoplay persisted, default ON (بأمر محمد).
    autoplay = prefAutoplay,
    onAutoplay = { v -> settingsVm().setPrefAutoplay(v) },
    // 2026-11-12 (runtime layer): mini-player options, live-apply (no restart).
    miniEnabled = prefMiniEnabled,
    onMiniEnabled = { v -> miniEnabledX(v) },
    miniAutoshow = prefMiniAutoshow,
    onMiniAutoshow = { v -> settingsVm().setPrefMiniAutoshow(v) },
    miniSwipe = prefMiniSwipe,
    onMiniSwipe = { v -> settingsVm().setPrefMiniSwipe(v) },
    afMode = prefAf,
    onAf = { v -> afX(v) },
    sbEnabled = prefSb,
    onSb = { v -> settingsVm().setPrefSb(v) },
    pipAuto = prefPip,
    onPip = { v -> settingsVm().setPrefPip(v) },
    favCount = favoriteKeys().size,
    histCount = histCount,
    plCount = playlistsCount(),
    dbSize = dbSizeLabel(),
    onClearFavorites = { clearFavoritesX() },
    onClearHistory = { clearHistoryX() },
    onCopyDiagnostics = { copyDiagnosticsX() },
    onCheckUpdate = { checkUpdateX() },
    onExportFull = { exportFullX() },
    onImportFull = { importFullX() },
    cacheSize = cacheSizeLabel,
    cacheMaxMb = prefCacheMaxMb,
    onCacheMax = { mb -> cacheMaxX(mb) },
    onClearCache = { clearCacheX() },
    offlineMode = prefOffline,
    onOfflineMode = { v -> settingsVm().setPrefOffline(v) },
    keepAlive = prefKeepalive,
    onKeepAlive = { v -> keepAliveX(v) },
    ageMode = prefAgeMode,
    onAgeMode = { m -> settingsVm().setPrefAgeMode(m) },
    showComments = prefShowComments,
    onShowComments = { v -> settingsVm().setPrefShowComments(v) },
    showRelated = prefShowRelated,
    onShowRelated = { v -> settingsVm().setPrefShowRelated(v) },
    showInfo = prefShowInfo,
    onShowInfo = { v -> settingsVm().setPrefShowInfo(v) },
    showSubs = prefShowSubs,
    onShowSubs = { v -> settingsVm().setPrefShowSubs(v) },
    allow4k = prefAllow4k,
    onAllow4k = { v -> settingsVm().setPrefAllow4k(v) },
    pinsCount = pinsCount,
    pinsSize = pinsSizeLabel,
    pinsBytes = pinsBytes,
    pinsWarnMb = prefPinWarnMb,
    pinLoc = pinSelLoc,
    extAvailable = extAvailable(),
    onPinLoc = { loc -> pinVm().setPinSelLoc(loc) },
    onPinsWarn = { mb -> settingsVm().setPrefPinWarnMb(mb) },
    onClearPins = { clearPinsX() },
    feedParallel = prefFeedParallel,
    onFeedParallel = { v -> settingsVm().setPrefFeedParallel(v) },
    feedCache = prefFeedCache,
    onFeedCache = { v -> settingsVm().setPrefFeedCache(v) },
    // Deferred items 7+8: last-backup metadata row + periodic refresh toggle.
    backupInfo = backupInfoText(),
    feedAutoRefresh = prefFeedAuto,
    onFeedAutoRefresh = { v -> feedAutoX(v) },
    preloadNext = prefPreloadNext,
    onPreloadNext = { v -> preloadNextX(v) },
    preloadWifiOnly = prefPreloadWifiOnly,
    titleTranslate = prefTitleTranslate,
    onTitleTranslate = { v -> settingsVm().setPrefTitleTranslate(v) },
    onPreloadWifiOnly = { v -> settingsVm().setPrefPreloadWifiOnly(v) },
    subHideAuto = prefSubHideAuto,
    onSubHideAuto = { v -> settingsVm().setPrefSubHideAuto(v) },
    subFg = prefSubFg,
    onSubFg = { v -> settingsVm().setPrefSubFg(v) },
    subBg = prefSubBg,
    onSubBg = { v -> settingsVm().setPrefSubBg(v) },
    subEdge = prefSubEdge,
    onSubEdge = { v -> settingsVm().setPrefSubEdge(v) },
    subScale = prefSubScale,
    onSubScale = { v -> settingsVm().setPrefSubScale(v) },
    subBgOpacity = prefSubBgOpacity,
    onSubBgOpacity = { v -> settingsVm().setPrefSubBgOpacity(v) },
    subBold = prefSubBold,
    onSubBold = { v -> settingsVm().setPrefSubBold(v) },
    preferOriginal = prefPreferOriginal,
    onPreferOriginal = { v -> preferOriginalX(v) },
    subRole = prefSubRole,
    onSubRole = { v -> settingsVm().setPrefSubRole(v) },
    hideForced = prefHideForced,
    onHideForced = { v -> settingsVm().setPrefHideForced(v) },
    undetermined = prefUndetermined,
    onUndetermined = { v -> settingsVm().setPrefUndetermined(v) },
    audioRole = prefAudioRole,
    onAudioRole = { v -> settingsVm().setPrefAudioRole(v) },
    skipSilence = prefSkipSilence,
    onSkipSilence = { v -> skipSilenceX(v) },
    speed = prefSpeed,
    onSpeed = { v -> settingsVm().setPrefSpeed(v) },
    pitch = prefPitch,
    onPitch = { v -> settingsVm().setPrefPitch(v) },
    theme = prefTheme,
    onTheme = { v -> themeX(v) },
    accent = prefAccent,
    onAccent = { v -> accentX(v) },
    // Restart-prompt (11 Nov 2026 — بأمر محمد): dialog choice after theme/accent change.
    onRestartNow = { restartNowX() },
    volume = volumeLive.value,
    muted = mutedLive.value,
    onVolume = { v ->
        try { volumeLive.value = v.coerceIn(0f, 1f) } catch (_: Exception) { }
        if (v > 0f) try { mutedLive.value = false } catch (_: Exception) { }
    },
    onMuted = { m ->
        try { mutedLive.value = m } catch (_: Exception) { }
    },
    eqSupported = eqSupported,
    eqEnabled = eqEnabled,
    onEqEnabled = { on -> eqVm().setEnabled(on) },
    eqPresetNames = eqPresetNames,
    eqPreset = eqPreset,
    onEqPreset = { i -> eqVm().setPreset(i) },
    eqLabels = eqLabels,
    eqBands = eqCustom,
    onEqBand = { b, v -> eqVm().setBand(b, v) },
)
// Geo-switch cache guard dialog (2026-11-12, runtime layer): mandatory
// confirmation before a content-country switch. REFUSE (dismiss/cancel)
// keeps the OLD country — prefCountry untouched, no partial state.
// ACCEPT clears extractor + page caches, then applies + reloads.
// Extracted to CountryGuardDialog: this host method is at the 64KB limit.
val pending = pendingCountry()
if (pending != null) {
    CountryGuardDialog(
        pending = pending,
        pendingName = COUNTRY_OPTIONS.firstOrNull { it.first == pending }?.second?.let { strOf(it) } ?: pending,
        onDismiss = { clearPending() },
        onConfirm = { v -> confirmCountryX(v) }
    )
}}
