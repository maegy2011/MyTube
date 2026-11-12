package com.mohamedadel.mytube.ui.settings

import com.mohamedadel.mytube.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.mohamedadel.mytube.ui.theme.AccentFills
import com.mohamedadel.mytube.ui.theme.AccentNames
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape

// Settings screen extracted from MainActivity (split step 2).
// Pure UI: all state comes in via params, all writes go out via callbacks.

val LANG_OPTIONS = listOf("ar" to R.string.lang_ar, "en" to R.string.lang_en, "fr" to R.string.lang_fr, "es" to R.string.lang_es, "de" to R.string.lang_de)

val COUNTRY_OPTIONS = listOf(
    "EG" to R.string.country_eg, "SA" to R.string.country_sa, "AE" to R.string.country_ae,
    "KW" to R.string.country_kw, "QA" to R.string.country_qa, "BH" to R.string.country_bh,
    "OM" to R.string.country_om, "YE" to R.string.country_ye, "IQ" to R.string.country_iq,
    "JO" to R.string.country_jo, "LB" to R.string.country_lb, "SY" to R.string.country_sy,
    "PS" to R.string.country_ps, "TR" to R.string.country_tr, "IR" to R.string.country_ir,
    "MA" to R.string.country_ma, "DZ" to R.string.country_dz,
    "US" to R.string.country_us, "GB" to R.string.country_gb, "FR" to R.string.country_fr,
    "DE" to R.string.country_de, "global" to R.string.country_global
)

@Composable
fun langText(code: String, deviceTag: String): String = when (code) {
    "auto" -> stringResource(R.string.auto_device_fmt, deviceTag)
    "off" -> stringResource(R.string.stop)
    "original" -> stringResource(R.string.lang_original_opt)
    else -> LANG_OPTIONS.firstOrNull { it.first == code }?.second?.let { stringResource(it) } ?: code
}

val UI_LANG_OPTIONS = listOf(
    "ar" to "العربية",
    "en" to "English"
)

val PLAYMODE_OPTIONS = listOf(
    "order" to R.string.mode_order_desc,
    "repeatAll" to R.string.mode_repeat_all_desc,
    "repeatOne" to R.string.mode_repeat_one_desc
)

val THEME_OPTIONS = listOf(
    "dark" to R.string.theme_dark,
    "light" to R.string.theme_light,
    "black" to R.string.theme_black,
    "system" to R.string.theme_system
)

// Appearance accents: built from Color.kt AccentFills (single source of truth) + local labels.
// D2 (audit P1 2026-11-11): was a hardcoded mirror of AccentFills.
private val ACCENT_LABELS = mapOf(
    "red" to R.string.accent_red,
    "blue" to R.string.accent_blue,
    "green" to R.string.accent_green,
    "purple" to R.string.accent_purple,
    "orange" to R.string.accent_orange,
    "teal" to R.string.accent_teal
)
val ACCENT_OPTIONS = AccentNames.mapNotNull { code ->
    AccentFills[code]?.let { code to (it to (ACCENT_LABELS[code] ?: R.string.accent_red)) }
}

val QUALITY_OPTIONS = listOf(
    "audio-only" to R.string.quality_audio,
    "auto" to R.string.quality_auto,
    "saver" to R.string.quality_saver,
    "high" to R.string.quality_high
)

@Composable
fun SettingsScreen(
    subLang: String,
    audioLang: String,
    subLang2: String,
    audioLang2: String,
    country: String,
    quality: String,
    deviceLang: java.util.Locale?,
    onSubLang: (String) -> Unit,
    onAudioLang: (String) -> Unit,
    onSubLang2: (String) -> Unit,
    onAudioLang2: (String) -> Unit,
    onCountry: (String) -> Unit,
    onQuality: (String) -> Unit,
    theme: String,
    onTheme: (String) -> Unit,
    accent: String = "red",
    onAccent: (String) -> Unit = {},
    // Restart-prompt (11 Nov 2026 — بأمر محمد): "restart now" action of the
    // confirm dialog shown after a theme/accent change.
    onRestartNow: () -> Unit = {},
    volume: Float = 1f,
    muted: Boolean = false,
    onVolume: (Float) -> Unit = {},
    onMuted: (Boolean) -> Unit = {},
    eqSupported: Boolean = false,
    eqEnabled: Boolean = false,
    onEqEnabled: (Boolean) -> Unit = {},
    eqPresetNames: List<String> = emptyList(),
    eqPreset: Int = 0,
    onEqPreset: (Int) -> Unit = {},
    eqLabels: List<String> = emptyList(),
    eqBands: List<Float> = emptyList(),
    onEqBand: (Int, Float) -> Unit = { _, _ -> },
    uiLang: String,
    onUiLang: (String) -> Unit,
    playMode: String,
    pipAuto: Boolean = true,
    onPip: (Boolean) -> Unit = {},
    sbEnabled: Boolean = false,
    onSb: (Boolean) -> Unit = {},
    afMode: String = "all",
    onAf: (String) -> Unit = {},
    onPlayMode: (String) -> Unit,
    // 2026-11-12 (runtime fix): persisted autoplay toggle, default ON — drives radio auto-advance.
    autoplay: Boolean = true,
    onAutoplay: (Boolean) -> Unit = {},
    // 2026-11-12 (runtime layer): mini-player options — master switch, auto-show on play,
    // swipe-to-dismiss. Tap behavior is fixed (tap opens full player) — no pref. Surfaces:
    // allowlist = every destination except Settings (enforced in Scaffold bottomBar).
    miniEnabled: Boolean = true,
    onMiniEnabled: (Boolean) -> Unit = {},
    miniAutoshow: Boolean = true,
    onMiniAutoshow: (Boolean) -> Unit = {},
    miniSwipe: Boolean = true,
    onMiniSwipe: (Boolean) -> Unit = {},
    subHideAuto: Boolean = false,
    onSubHideAuto: (Boolean) -> Unit = {},
    subFg: String = "white",
    onSubFg: (String) -> Unit = {},
    subBg: String = "transparent",
    onSubBg: (String) -> Unit = {},
    subEdge: String = "outline",
    onSubEdge: (String) -> Unit = {},
    subScale: Float = 1.0f,
    onSubScale: (Float) -> Unit = {},
    subBgOpacity: Int = 80,
    onSubBgOpacity: (Int) -> Unit = {},
    subBold: Boolean = false,
    onSubBold: (Boolean) -> Unit = {},
    preferOriginal: Boolean = false,
    onPreferOriginal: (Boolean) -> Unit = {},
    subRole: String = "subtitle",
    onSubRole: (String) -> Unit = {},
    hideForced: Boolean = false,
    onHideForced: (Boolean) -> Unit = {},
    undetermined: Boolean = true,
    onUndetermined: (Boolean) -> Unit = {},
    audioRole: String = "auto",
    onAudioRole: (String) -> Unit = {},
    skipSilence: Boolean = false,
    onSkipSilence: (Boolean) -> Unit = {},
    speed: Float = 1.0f,
    onSpeed: (Float) -> Unit = {},
    pitch: Float = 1.0f,
    onPitch: (Float) -> Unit = {},
    favCount: Int,
    histCount: Int,
    plCount: Int,
    dbSize: String,
    cacheSize: String = "—",
    cacheMaxMb: Long = 500L,
    onCacheMax: (Long) -> Unit = {},
    onClearCache: () -> Unit = {},
    offlineMode: Boolean = false,
    onOfflineMode: (Boolean) -> Unit = {},
    keepAlive: Boolean = false,
    onKeepAlive: (Boolean) -> Unit = {},
    ageMode: String = "block",
    onAgeMode: (String) -> Unit = {},
    showComments: Boolean = true,
    onShowComments: (Boolean) -> Unit = {},
    showRelated: Boolean = true,
    onShowRelated: (Boolean) -> Unit = {},
    showInfo: Boolean = true,
    onShowInfo: (Boolean) -> Unit = {},
    showSubs: Boolean = true,
    onShowSubs: (Boolean) -> Unit = {},
    allow4k: Boolean = true,
    onAllow4k: (Boolean) -> Unit = {},
    pinsCount: Int = 0,
    pinsSize: String = "—",
    pinsBytes: Long = 0L,
    pinsWarnMb: Long = 1024L,
    onPinsWarn: (Long) -> Unit = {},
    pinLoc: String = "internal",
    extAvailable: Boolean = false,
    onPinLoc: (String) -> Unit = {},
    onClearPins: () -> Unit = {},
    feedParallel: Boolean = true,
    onFeedParallel: (Boolean) -> Unit = {},
    feedCache: Boolean = true,
    onFeedCache: (Boolean) -> Unit = {},
    preloadNext: Boolean = true,
    onPreloadNext: (Boolean) -> Unit = {},
    preloadWifiOnly: Boolean = true,
    onPreloadWifiOnly: (Boolean) -> Unit = {},
    titleTranslate: Boolean = true,
    onTitleTranslate: (Boolean) -> Unit = {},
    onClearFavorites: () -> Unit,
    onClearHistory: () -> Unit,
    onCopyDiagnostics: () -> Unit = {},
    onCheckUpdate: () -> Unit = {},
    onExportFull: () -> Unit = {},
    onImportFull: () -> Unit = {},
    // Deferred item 7 (backup metadata row) + item 8 (periodic auto-refresh toggle).
    backupInfo: String? = null,
    feedAutoRefresh: Boolean = false,
    onFeedAutoRefresh: (Boolean) -> Unit = {}
) {
    val deviceTag = deviceLang?.language ?: "ar"
    var confirmFav by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var confirmHist by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var confirmCache by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    // Restart-prompt (11 Nov 2026): shown after a theme/accent change — restart now or later.
    var showRestartPrompt by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    if (showRestartPrompt) {
        AlertDialog(
            onDismissRequest = { showRestartPrompt = false },
            title = { Text(stringResource(R.string.restart_needed_title)) },
            text = { Text(stringResource(R.string.restart_appearance_msg)) },
            confirmButton = { TextButton(onClick = { showRestartPrompt = false; onRestartNow() }) { Text(stringResource(R.string.restart_now)) } },
            dismissButton = { TextButton(onClick = { showRestartPrompt = false }) { Text(stringResource(R.string.later)) } }
        )
    }
    var confirmPins by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    if (confirmFav) {
        AlertDialog(
            onDismissRequest = { confirmFav = false },
            title = { Text(stringResource(R.string.clear_favorites_title)) },
            text = { Text(stringResource(R.string.del_fav_confirm_fmt, favCount)) },
            confirmButton = { TextButton(onClick = { onClearFavorites(); confirmFav = false }) { Text(stringResource(R.string.clear)) } },
            dismissButton = { TextButton(onClick = { confirmFav = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (confirmHist) {
        AlertDialog(
            onDismissRequest = { confirmHist = false },
            title = { Text(stringResource(R.string.clear_history_title)) },
            text = { Text(stringResource(R.string.del_hist_confirm_fmt, histCount)) },
            confirmButton = { TextButton(onClick = { onClearHistory(); confirmHist = false }) { Text(stringResource(R.string.clear)) } },
            dismissButton = { TextButton(onClick = { confirmHist = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (confirmPins) {
        AlertDialog(
            onDismissRequest = { confirmPins = false },
            title = { Text(stringResource(R.string.clear_pins_title)) },
            text = { Text(stringResource(R.string.clear_pins_fmt, pinsCount, pinsSize)) },
            confirmButton = { TextButton(onClick = { onClearPins(); confirmPins = false }) { Text(stringResource(R.string.clear)) } },
            dismissButton = { TextButton(onClick = { confirmPins = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (confirmCache) {
        AlertDialog(
            onDismissRequest = { confirmCache = false },
            title = { Text(stringResource(R.string.clear_cache_title)) },
            text = { Text(stringResource(R.string.clear_cache_fmt, cacheSize)) },
            confirmButton = { TextButton(onClick = { onClearCache(); confirmCache = false }) { Text(stringResource(R.string.clear)) } },
            dismissButton = { TextButton(onClick = { confirmCache = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    // Deferred item 4 (2026-11-11): settings search — one field filters all 7 sections/rows;
    // matching sections auto-expand via forceExpand so results are visible immediately.
    var sQuery by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf("") }
    val sQ = sQuery.trim()
    fun sHit(vararg t: String) = sQ.isEmpty() || t.any { it.contains(sQ, ignoreCase = true) }
    val sLangT = stringResource(R.string.settings_sec_lang)
    val sSubsT = stringResource(R.string.settings_sec_subs)
    val sAudioT = stringResource(R.string.settings_sec_audio)
    val sPlayerT = stringResource(R.string.settings_sec_player)
    val sMiniT = stringResource(R.string.settings_sec_miniplayer)
    val sAppearT = stringResource(R.string.settings_sec_appearance)
    val sDlT = stringResource(R.string.settings_sec_downloads)
    val sDataT = stringResource(R.string.settings_sec_data)
    // Deferred-4 visibility must resolve in composable scope: LazyListScope `if` guards
    // cannot call stringResource (composable-only), so precompute each section here.
    val showLang = sHit(sLangT, stringResource(R.string.ui_lang_title), stringResource(R.string.country_title), stringResource(R.string.title_translate))
    val showSubs = sHit(sSubsT, stringResource(R.string.sub_primary_title), stringResource(R.string.sub_fallback_title), stringResource(R.string.sub_hide_auto), stringResource(R.string.sub_size_title), stringResource(R.string.sub_fg_title), stringResource(R.string.sub_bg_title), stringResource(R.string.sub_edge_title), stringResource(R.string.sub_bold), stringResource(R.string.sub_role_title))
    val showAudio = sHit(sAudioT, stringResource(R.string.audio_primary_title), stringResource(R.string.audio_fallback_title), stringResource(R.string.prefer_original), stringResource(R.string.audio_role_title), stringResource(R.string.skip_silence), stringResource(R.string.speed_title), stringResource(R.string.pitch_title), stringResource(R.string.volume_title), stringResource(R.string.eq_title))
    val showPlayer = sHit(sPlayerT, stringResource(R.string.quality_title), stringResource(R.string.playmode_title), stringResource(R.string.autoplay_title), stringResource(R.string.af_all), stringResource(R.string.sb_title), stringResource(R.string.pip_auto), stringResource(R.string.vis_comments), stringResource(R.string.vis_related), stringResource(R.string.vis_info), stringResource(R.string.vis_subs), stringResource(R.string.vis_4k), stringResource(R.string.keepalive_setting), stringResource(R.string.age_title))
    val showMini = sHit(sMiniT, stringResource(R.string.mini_enabled), stringResource(R.string.mini_autoshow), stringResource(R.string.mini_swipe))
    val showAppear = sHit(sAppearT, stringResource(R.string.theme_title), stringResource(R.string.accent_title))
    val showDl = sHit(sDlT, stringResource(R.string.update_check), stringResource(R.string.backup_export), stringResource(R.string.backup_import), stringResource(R.string.feed_auto_refresh), stringResource(R.string.offline_mode), stringResource(R.string.perf_parallel), stringResource(R.string.perf_feedcache), stringResource(R.string.perf_preload), stringResource(R.string.clear_pins), stringResource(R.string.clear_cache), stringResource(R.string.diag_copy), stringResource(R.string.cache_title), stringResource(R.string.pin_loc_title))
    val showData = sHit(sDataT, stringResource(R.string.clear_favorites), stringResource(R.string.clear_history))
    // Deferred-2 (a11y): semantics{} blocks are not composable — resolve descriptions here.
    val searchHint = stringResource(R.string.settings_search_hint)
    val subSizeDesc = stringResource(R.string.sub_size_title)
    val subBgOpDesc = stringResource(R.string.sub_bg_opacity)
    val volDesc = stringResource(R.string.volume_title)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = sQuery,
                onValueChange = { sQuery = it },
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.settings_search_hint)) },
                modifier = Modifier.fillMaxWidth().semantics { contentDescription = searchHint }
            )
        }
        if (showLang) item {
            SettingsSection(title = sLangT, forceExpand = sQ.isNotEmpty()) {
                Text(
                    stringResource(R.string.device_line_fmt, deviceLang?.displayLanguage ?: deviceTag, deviceTag),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DropRow(
                    label = stringResource(R.string.ui_lang_title),
                    entries = UI_LANG_OPTIONS.map { it.first to it.second },
                    selected = uiLang,
                    onPick = onUiLang
                )
                DropRow(
                    label = stringResource(R.string.country_title),
                    entries = COUNTRY_OPTIONS.map { it.first to stringResource(it.second) },
                    selected = country,
                    onPick = onCountry
                )
                SwitchRow(
                    label = stringResource(R.string.title_translate),
                    desc = stringResource(R.string.title_translate_desc),
                    checked = titleTranslate,
                    onToggle = { onTitleTranslate(!titleTranslate) }
                )
            }
        }
        if (showSubs) item {
            SettingsSection(title = sSubsT, forceExpand = sQ.isNotEmpty()) {
                DropRow(
                    label = stringResource(R.string.sub_primary_title),
                    entries = listOf("auto" to langText("auto", deviceTag), "off" to langText("off", deviceTag)) + LANG_OPTIONS.map { it.first to langText(it.first, deviceTag) },
                    selected = subLang,
                    onPick = onSubLang
                )
                DropRow(
                    label = stringResource(R.string.sub_fallback_title),
                    entries = listOf("original" to langText("original", deviceTag)) + LANG_OPTIONS.map { it.first to langText(it.first, deviceTag) },
                    selected = subLang2,
                    onPick = onSubLang2
                )
                SwitchRow(
                    label = stringResource(R.string.sub_hide_auto),
                    checked = subHideAuto,
                    onToggle = { onSubHideAuto(!subHideAuto) }
                )
                Box(modifier = Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp), contentAlignment = Alignment.Center) {
                    // screens-fix Settings P0-S2: preview now reflects subEdge (same constants as
                    // subStyleFromPrefs: outline/shadow/raised/none). Outline is approximated with a hard
                    // dark edge — true stroked glyphs need a custom text draw, out of scope here.
                    val pvShadow = when (subEdge) {
                        "none" -> null
                        "shadow" -> Shadow(color = Color.Black.copy(alpha = 0.9f), offset = Offset(2f, 2f), blurRadius = 4f)
                        "raised" -> Shadow(color = Color.White.copy(alpha = 0.7f), offset = Offset(-1f, -1f), blurRadius = 1f)
                        else -> Shadow(color = Color.Black, offset = Offset(1f, 1f), blurRadius = 0f)
                    }
                    val pvFg = when (subFg) { "yellow" -> Color.Yellow; "black" -> Color.Black; "cyan" -> Color.Cyan; else -> Color.White }
                    val pvBgBase = when (subBg) { "black" -> Color.Black; "white" -> Color.White; else -> Color.Transparent }
                    val pvBg = if (subBg == "transparent") Color.Transparent else pvBgBase.copy(alpha = subBgOpacity.coerceIn(0, 100) / 100f)
                    Text(
                        stringResource(R.string.sub_preview_text),
                        color = pvFg,
                        fontSize = (16f * subScale.coerceIn(0.5f, 2.0f)).sp,
                        fontWeight = if (subBold) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                        style = TextStyle(shadow = pvShadow ?: Shadow(color = Color.Transparent, offset = Offset.Zero, blurRadius = 0f)),
                        modifier = Modifier.background(pvBg).padding(horizontal = 4.dp)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.sub_size_title), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    // Deferred item 2 (a11y): sliders carry their label for TalkBack.
                    Slider(value = subScale.coerceIn(0.5f, 2.0f), onValueChange = onSubScale, valueRange = 0.5f..2.0f, steps = 14, modifier = Modifier.weight(1f).semantics { contentDescription = subSizeDesc })
                    Spacer(Modifier.width(8.dp))
                    Text(String.format(java.util.Locale.US, "%.1fx", subScale.coerceIn(0.5f, 2.0f)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                DropRow(
                    label = stringResource(R.string.sub_fg_title),
                    entries = listOf("white", "yellow", "black", "cyan").map { it to stringResource(when (it) { "yellow" -> R.string.sub_fg_yellow; "black" -> R.string.sub_fg_black; "cyan" -> R.string.sub_fg_cyan; else -> R.string.sub_fg_white }) },
                    selected = subFg,
                    onPick = onSubFg
                )
                DropRow(
                    label = stringResource(R.string.sub_bg_title),
                    entries = listOf("transparent", "black", "white").map { it to stringResource(when (it) { "black" -> R.string.sub_bg_black; "white" -> R.string.sub_bg_white; else -> R.string.sub_bg_trans }) },
                    selected = subBg,
                    onPick = onSubBg
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.sub_bg_opacity), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.width(8.dp))
                    Slider(value = subBgOpacity.toFloat(), onValueChange = { onSubBgOpacity(it.toInt()) }, valueRange = 0f..100f, steps = 19, modifier = Modifier.weight(1f).semantics { contentDescription = subBgOpDesc }, enabled = subBg != "transparent")
                    Spacer(Modifier.width(8.dp))
                    Text("$subBgOpacity%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                DropRow(
                    label = stringResource(R.string.sub_edge_title),
                    entries = listOf("outline", "none", "shadow", "raised").map { it to stringResource(when (it) { "outline" -> R.string.sub_edge_outline; "none" -> R.string.sub_edge_none; "shadow" -> R.string.sub_edge_shadow; else -> R.string.sub_edge_raised }) },
                    selected = subEdge,
                    onPick = onSubEdge
                )
                SwitchRow(
                    label = stringResource(R.string.sub_bold),
                    checked = subBold,
                    onToggle = { onSubBold(!subBold) }
                )
                DropRow(
                    label = stringResource(R.string.sub_role_title),
                    entries = listOf("subtitle", "caption", "descriptive", "system").map { it to stringResource(when (it) { "caption" -> R.string.sub_role_caption; "descriptive" -> R.string.sub_role_descriptive; "system" -> R.string.sub_role_system; else -> R.string.sub_role_subtitle }) },
                    selected = subRole,
                    onPick = onSubRole
                )
                SwitchRow(
                    label = stringResource(R.string.hide_forced_subs),
                    checked = hideForced,
                    onToggle = { onHideForced(!hideForced) }
                )
                SwitchRow(
                    label = stringResource(R.string.undetermined_lang),
                    checked = undetermined,
                    onToggle = { onUndetermined(!undetermined) }
                )
            }
        }
        if (showAudio) item {
            SettingsSection(title = sAudioT, forceExpand = sQ.isNotEmpty()) {
                DropRow(
                    label = stringResource(R.string.audio_primary_title),
                    entries = listOf("auto" to langText("auto", deviceTag)) + LANG_OPTIONS.map { it.first to langText(it.first, deviceTag) },
                    selected = audioLang,
                    onPick = onAudioLang
                )
                DropRow(
                    label = stringResource(R.string.audio_fallback_title),
                    entries = listOf("original" to langText("original", deviceTag)) + LANG_OPTIONS.map { it.first to langText(it.first, deviceTag) },
                    selected = audioLang2,
                    onPick = onAudioLang2
                )
                SwitchRow(
                    label = stringResource(R.string.prefer_original),
                    checked = preferOriginal,
                    onToggle = { onPreferOriginal(!preferOriginal) }
                )
                DropRow(
                    label = stringResource(R.string.audio_role_title),
                    entries = listOf("auto", "dubbed", "original", "commentary").map { it to stringResource(when (it) { "dubbed" -> R.string.audio_role_dubbed; "original" -> R.string.audio_role_original; "commentary" -> R.string.audio_role_commentary; else -> R.string.audio_role_auto }) },
                    selected = audioRole,
                    onPick = onAudioRole
                )
                SwitchRow(
                    label = stringResource(R.string.skip_silence),
                    checked = skipSilence,
                    onToggle = { onSkipSilence(!skipSilence) }
                )
                DropRow(
                    label = stringResource(R.string.speed_title),
                    entries = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).map { v -> v.toString() to (if (v == 1.0f) stringResource(R.string.speed_normal) else "${v}x") },
                    selected = speed.toString(),
                    onPick = { onSpeed(it.toFloat()) }
                )
                DropRow(
                    label = stringResource(R.string.pitch_title),
                    entries = listOf(0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f).map { v -> v.toString() to (if (v == 1.0f) stringResource(R.string.speed_normal) else "${v}x") },
                    selected = pitch.toString(),
                    onPick = { onPitch(it.toFloat()) }
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (muted || volume == 0f) Icons.AutoMirrored.Filled.VolumeOff else if (volume < 0.5f) Icons.AutoMirrored.Filled.VolumeDown else Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Slider(value = if (muted) 0f else volume, onValueChange = onVolume, valueRange = 0f..1f, modifier = Modifier.weight(1f).semantics { contentDescription = volDesc })
                    Spacer(Modifier.width(8.dp))
                    Text("${(if (muted) 0f else volume * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                // screens-fix Settings P1-S7: stable label (was flipping mute/unmute with state).
                SwitchRow(
                    label = stringResource(R.string.cd_mute),
                    desc = stringResource(if (muted) R.string.state_on else R.string.state_off),
                    checked = muted,
                    onToggle = { onMuted(!muted) }
                )
                if (!eqSupported) {
                    Text(stringResource(R.string.eq_unsupported), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    SwitchRow(
                        label = stringResource(if (eqEnabled) R.string.eq_on else R.string.eq_off),
                        checked = eqEnabled,
                        onToggle = { onEqEnabled(!eqEnabled) }
                    )
                    if (eqEnabled) {
                        DropRow(
                            label = stringResource(R.string.eq_title),
                            entries = eqPresetNames.mapIndexed { i, n -> i.toString() to n } + ("-1" to stringResource(R.string.eq_custom)),
                            selected = eqPreset.toString(),
                            onPick = { onEqPreset(it.toInt()) }
                        )
                        if (eqPreset == -1) {
                            eqLabels.forEachIndexed { b, _ ->
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(eqLabels[b], style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(64.dp))
                                    // Deferred item 2 (a11y): EQ band sliders carry their band label.
                                    Slider(value = eqBands.getOrNull(b) ?: 0.5f, onValueChange = { onEqBand(b, it) }, valueRange = 0f..1f, modifier = Modifier.weight(1f).semantics { contentDescription = eqLabels.getOrElse(b) { "" } })
                                }
                            }
                        }
                    }
                }
            }
        }
        if (showPlayer) item {
            SettingsSection(title = sPlayerT, forceExpand = sQ.isNotEmpty()) {
                DropRow(
                    label = stringResource(R.string.quality_title),
                    entries = QUALITY_OPTIONS.map { it.first to stringResource(it.second) },
                    selected = quality,
                    onPick = onQuality
                )
                DropRow(
                    label = stringResource(R.string.playmode_title),
                    entries = PLAYMODE_OPTIONS.map { it.first to stringResource(it.second) },
                    selected = playMode,
                    onPick = onPlayMode
                )
                SwitchRow(
                    label = stringResource(R.string.autoplay_title),
                    desc = stringResource(R.string.autoplay_desc),
                    checked = autoplay,
                    onToggle = { onAutoplay(!autoplay) }
                )
                listOf("all", "calls").forEach { code ->
                    // screens-fix Settings P1-S4: selectable radio rows (were clickable + icon-mimic).
                    Row(modifier = Modifier.fillMaxWidth().selectable(selected = afMode == code, onClick = { onAf(code) }, role = Role.RadioButton).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(if (afMode == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (afMode == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(stringResource(if (code == "all") R.string.af_all else R.string.af_calls), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            if (code == "calls") Text(stringResource(R.string.af_calls_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                SwitchRow(
                    label = stringResource(R.string.sb_title),
                    desc = stringResource(R.string.sb_desc),
                    checked = sbEnabled,
                    onToggle = { onSb(!sbEnabled) }
                )
                SwitchRow(
                    label = stringResource(R.string.pip_auto),
                    checked = pipAuto,
                    onToggle = { onPip(!pipAuto) }
                )
                SwitchRow(
                    label = stringResource(R.string.vis_comments),
                    desc = stringResource(R.string.vis_comments_desc),
                    checked = showComments,
                    onToggle = { onShowComments(!showComments) }
                )
                SwitchRow(
                    label = stringResource(R.string.vis_related),
                    desc = stringResource(R.string.vis_related_desc),
                    checked = showRelated,
                    onToggle = { onShowRelated(!showRelated) }
                )
                SwitchRow(
                    label = stringResource(R.string.vis_info),
                    desc = stringResource(R.string.vis_info_desc),
                    checked = showInfo,
                    onToggle = { onShowInfo(!showInfo) }
                )
                SwitchRow(
                    label = stringResource(R.string.vis_subs),
                    desc = stringResource(R.string.vis_subs_desc),
                    checked = showSubs,
                    onToggle = { onShowSubs(!showSubs) }
                )
                SwitchRow(
                    label = stringResource(R.string.vis_4k),
                    desc = stringResource(R.string.vis_4k_desc),
                    checked = allow4k,
                    onToggle = { onAllow4k(!allow4k) }
                )
                SwitchRow(
                    label = stringResource(R.string.keepalive_setting),
                    desc = stringResource(R.string.keepalive_setting_desc),
                    checked = keepAlive,
                    onToggle = { onKeepAlive(!keepAlive) }
                )
                Text(stringResource(R.string.age_title), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                AgeModeRow(mode = "block", sel = ageMode, onPick = onAgeMode)
                AgeModeRow(mode = "warn", sel = ageMode, onPick = onAgeMode)
                AgeModeRow(mode = "allow", sel = ageMode, onPick = onAgeMode)
            }
        }
        if (showMini) item {
            SettingsSection(title = sMiniT, forceExpand = sQ.isNotEmpty()) {
                SwitchRow(
                    label = stringResource(R.string.mini_enabled),
                    desc = stringResource(R.string.mini_enabled_desc),
                    checked = miniEnabled,
                    onToggle = { onMiniEnabled(!miniEnabled) }
                )
                SwitchRow(
                    label = stringResource(R.string.mini_autoshow),
                    desc = stringResource(R.string.mini_autoshow_desc),
                    checked = miniAutoshow,
                    enabled = miniEnabled,
                    onToggle = { onMiniAutoshow(!miniAutoshow) }
                )
                SwitchRow(
                    label = stringResource(R.string.mini_swipe),
                    desc = stringResource(R.string.mini_swipe_desc),
                    checked = miniSwipe,
                    enabled = miniEnabled,
                    onToggle = { onMiniSwipe(!miniSwipe) }
                )
            }
        }
        if (showAppear) item {
            SettingsSection(title = sAppearT, forceExpand = sQ.isNotEmpty()) {
                DropRow(
                    label = stringResource(R.string.theme_title),
                    entries = THEME_OPTIONS.map { it.first to stringResource(it.second) },
                    selected = theme,
                    onPick = { if (it != theme) { onTheme(it); showRestartPrompt = true } }
                )
                Text(stringResource(R.string.accent_title), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                // screens-fix Settings P1-S5: 48dp targets + visible labels + radio group (were 40dp color-only).
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    ACCENT_OPTIONS.forEach { (code, pair) ->
                        val (fill, label) = pair
                        val selected = accent == code
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).selectable(selected = selected, onClick = { if (code != accent) { onAccent(code); showRestartPrompt = true } }, role = Role.RadioButton)) {
                            Box(
                                modifier = Modifier.size(48.dp)
                                    .clip(CircleShape)
                                    .background(fill)
                                    .border(if (selected) 3.dp else 1.dp, if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Text(stringResource(label), style = MaterialTheme.typography.labelSmall, color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                }
                Text(stringResource(R.string.accent_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (showDl) item {
            SettingsSection(title = sDlT, forceExpand = sQ.isNotEmpty()) {
                Text(
                    stringResource(R.string.pins_line_fmt, pinsCount, pinsSize),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(stringResource(R.string.pin_loc_title), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                listOf("internal", "external").forEach { loc ->
                    val enabled = loc == "internal" || extAvailable
                    // screens-fix Settings P1-S4: selectable radio rows (were clickable + icon-mimic).
                    Row(
                        modifier = Modifier.fillMaxWidth().let { m -> if (enabled) m.selectable(selected = pinLoc == loc, onClick = { onPinLoc(loc) }, role = Role.RadioButton) else m }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (pinLoc == loc) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (pinLoc == loc) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                stringResource(if (loc == "internal") R.string.pin_loc_internal else R.string.pin_loc_external),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (loc == "external" && !extAvailable) {
                                Text(stringResource(R.string.pin_loc_ext_missing), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                DropRow(
                    label = stringResource(R.string.pins_warn_title),
                    entries = listOf(512L, 1024L, 2048L, 0L).map { mb ->
                        mb.toString() to when {
                            mb == 0L -> stringResource(R.string.pins_warn_off)
                            mb >= 1024 -> stringResource(R.string.pins_warn_gb_fmt, (mb / 1024).toInt())
                            else -> stringResource(R.string.pins_warn_mb_fmt, mb.toInt())
                        }
                    },
                    selected = pinsWarnMb.toString(),
                    onPick = { onPinsWarn(it.toLong()) }
                )
                if (pinsWarnMb > 0 && pinsCount > 0 && pinsBytes > pinsWarnMb * 1024 * 1024) {
                    Text(
                        stringResource(R.string.pins_over_warn),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedButton(onClick = { confirmPins = true }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.clear_pins)) }
                }
                Text(
                    stringResource(R.string.cache_line_fmt, cacheSize, cacheMaxMb),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DropRow(
                    label = stringResource(R.string.cache_title),
                    entries = listOf(100L, 250L, 500L, 1024L).map { mb -> mb.toString() to (if (mb >= 1024) stringResource(R.string.cache_gb) else stringResource(R.string.cache_mb_fmt, mb.toInt())) },
                    selected = cacheMaxMb.toString(),
                    onPick = { onCacheMax(it.toLong()) }
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedButton(onClick = { confirmCache = true }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.clear_cache)) }
                    androidx.compose.material3.OutlinedButton(onClick = onCopyDiagnostics, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.diag_copy)) }
                }
                // G-A2/G-A4 (audit P2 2026-11-11): update check + full backup (shared YouTube links land in G-A1).
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedButton(onClick = onCheckUpdate, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.update_check)) }
                    androidx.compose.material3.OutlinedButton(onClick = onExportFull, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.backup_export)) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedButton(onClick = onImportFull, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.backup_import)) }
                }
                // Deferred item 7: last-backup metadata row (hidden until the first export).
                if (!backupInfo.isNullOrBlank()) {
                    Text(backupInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
                }
                Text(stringResource(R.string.update_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.backup_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(stringResource(R.string.diag_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                SwitchRow(
                    label = stringResource(R.string.offline_mode),
                    desc = stringResource(R.string.offline_mode_desc),
                    checked = offlineMode,
                    onToggle = { onOfflineMode(!offlineMode) }
                )
                SwitchRow(
                    label = stringResource(R.string.perf_parallel),
                    desc = stringResource(R.string.perf_parallel_desc),
                    checked = feedParallel,
                    onToggle = { onFeedParallel(!feedParallel) }
                )
                SwitchRow(
                    label = stringResource(R.string.perf_feedcache),
                    desc = stringResource(R.string.perf_feedcache_desc),
                    checked = feedCache,
                    onToggle = { onFeedCache(!feedCache) }
                )
                SwitchRow(
                    label = stringResource(R.string.perf_preload),
                    desc = stringResource(R.string.perf_preload_desc),
                    checked = preloadNext,
                    onToggle = { onPreloadNext(!preloadNext) }
                )
                SwitchRow(
                    label = stringResource(R.string.perf_preload_wifi),
                    desc = stringResource(R.string.perf_preload_wifi_desc),
                    checked = preloadWifiOnly,
                    enabled = preloadNext,
                    onToggle = { onPreloadWifiOnly(!preloadWifiOnly) }
                )
                // Deferred item 8: periodic feed refresh (WorkManager, 15-min minimum, default OFF).
                SwitchRow(
                    label = stringResource(R.string.feed_auto_refresh),
                    desc = stringResource(R.string.feed_auto_refresh_desc),
                    checked = feedAutoRefresh,
                    onToggle = { onFeedAutoRefresh(!feedAutoRefresh) }
                )
            }
        }
        if (showData) item {
            SettingsSection(title = sDataT, forceExpand = sQ.isNotEmpty()) {
                Text(
                    stringResource(R.string.settings_data_line, favCount, histCount, plCount, dbSize),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.OutlinedButton(onClick = { confirmFav = true }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.clear_favorites)) }
                    androidx.compose.material3.OutlinedButton(onClick = { confirmHist = true }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.clear_history)) }
                }
            }
        }
        item {
            Text(
                stringResource(R.string.settings_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AgeModeRow(mode: String, sel: String, onPick: (String) -> Unit) {
    val label = when (mode) {
        "block" -> stringResource(R.string.age_block)
        "warn" -> stringResource(R.string.age_warn)
        else -> stringResource(R.string.age_allow)
    }
    val desc = when (mode) {
        "block" -> stringResource(R.string.age_block_desc)
        "warn" -> stringResource(R.string.age_warn_desc)
        else -> stringResource(R.string.age_allow_desc)
    }
    // screens-fix Settings P1-S4: selectable radio row (was clickable + icon-mimic).
    Row(modifier = Modifier.fillMaxWidth().selectable(selected = sel == mode, onClick = { onPick(mode) }, role = Role.RadioButton).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (sel == mode) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (sel == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

