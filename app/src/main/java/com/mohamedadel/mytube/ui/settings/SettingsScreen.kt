package com.mohamedadel.mytube.ui.settings

import com.mohamedadel.mytube.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape

// Settings screen extracted from MainActivity (split step 2).
// Pure UI: all state comes in via params, all writes go out via callbacks.

val LANG_OPTIONS = listOf("ar" to R.string.lang_ar, "en" to R.string.lang_en, "fr" to R.string.lang_fr, "es" to R.string.lang_es, "de" to R.string.lang_de)

val COUNTRY_OPTIONS = listOf(
    "EG" to R.string.country_eg, "SA" to R.string.country_sa, "AE" to R.string.country_ae,
    "KW" to R.string.country_kw, "QA" to R.string.country_qa, "MA" to R.string.country_ma,
    "DZ" to R.string.country_dz, "JO" to R.string.country_jo, "IQ" to R.string.country_iq,
    "US" to R.string.country_us, "GB" to R.string.country_gb, "global" to R.string.country_global
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

// Appearance accents: code to fill color + label. Fills mirror Color.kt AccentFills.
val ACCENT_OPTIONS = listOf(
    "red" to (Color(0xFFD32F2F) to R.string.accent_red),
    "blue" to (Color(0xFF0B57D0) to R.string.accent_blue),
    "green" to (Color(0xFF2E7D32) to R.string.accent_green),
    "purple" to (Color(0xFF7B1FA2) to R.string.accent_purple),
    "orange" to (Color(0xFFB45300) to R.string.accent_orange),
    "teal" to (Color(0xFF00796B) to R.string.accent_teal)
)

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
    uiLang: String,
    onUiLang: (String) -> Unit,
    playMode: String,
    onPlayMode: (String) -> Unit,
    subHideAuto: Boolean = false,
    onSubHideAuto: (Boolean) -> Unit = {},
    subFg: String = "white",
    onSubFg: (String) -> Unit = {},
    subBg: String = "transparent",
    onSubBg: (String) -> Unit = {},
    subEdge: String = "outline",
    onSubEdge: (String) -> Unit = {},
    subSize: String = "m",
    onSubSize: (String) -> Unit = {},
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
    pinsCount: Int = 0,
    pinsSize: String = "—",
    pinsBytes: Long = 0L,
    pinsWarnMb: Long = 1024L,
    onPinsWarn: (Long) -> Unit = {},
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
    onCopyDiagnostics: () -> Unit = {}
) {
    val deviceTag = deviceLang?.language ?: "ar"
    var confirmFav by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var confirmHist by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var confirmCache by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
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
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface) }
        item {
            Text(
                stringResource(R.string.device_line_fmt, deviceLang?.displayLanguage ?: deviceTag, deviceTag),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Text(stringResource(R.string.sub_primary_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(listOf("auto", "off") + LANG_OPTIONS.map { it.first }) { code ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onSubLang(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (subLang == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (subLang == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(langText(code, deviceTag), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.sub_fallback_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(listOf("original") + LANG_OPTIONS.map { it.first }) { code ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onSubLang2(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (subLang2 == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (subLang2 == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(langText(code, deviceTag), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.audio_primary_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(listOf("auto") + LANG_OPTIONS.map { it.first }) { code ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onAudioLang(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (audioLang == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (audioLang == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(langText(code, deviceTag), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.audio_fallback_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(listOf("original") + LANG_OPTIONS.map { it.first }) { code ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onAudioLang2(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (audioLang2 == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (audioLang2 == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(langText(code, deviceTag), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.quality_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(QUALITY_OPTIONS) { (code, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onQuality(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (quality == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (quality == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.ui_lang_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(UI_LANG_OPTIONS) { (code, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onUiLang(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (uiLang == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (uiLang == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.sub_studio_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onSubHideAuto(!subHideAuto) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (subHideAuto) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (subHideAuto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.sub_hide_auto), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        items(listOf("s", "m", "l", "xl")) { code ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onSubSize(code) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (subSize == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (subSize == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(when (code) { "s" -> R.string.sub_size_s; "m" -> R.string.sub_size_m; "l" -> R.string.sub_size_l; else -> R.string.sub_size_xl }), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        items(listOf("white", "yellow", "black", "cyan")) { code ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onSubFg(code) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (subFg == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (subFg == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(when (code) { "yellow" -> R.string.sub_fg_yellow; "black" -> R.string.sub_fg_black; "cyan" -> R.string.sub_fg_cyan; else -> R.string.sub_fg_white }), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        items(listOf("transparent", "black", "white")) { code ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onSubBg(code) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (subBg == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (subBg == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(when (code) { "black" -> R.string.sub_bg_black; "white" -> R.string.sub_bg_white; else -> R.string.sub_bg_trans }), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        items(listOf("outline", "none", "shadow", "raised")) { code ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onSubEdge(code) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (subEdge == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (subEdge == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(code, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onSubBold(!subBold) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (subBold) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (subBold) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.sub_bold), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        items(listOf("subtitle", "caption", "descriptive", "system")) { code ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onSubRole(code) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (subRole == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (subRole == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(when (code) { "caption" -> R.string.sub_role_caption; "descriptive" -> R.string.sub_role_descriptive; "system" -> R.string.sub_role_system; else -> R.string.sub_role_subtitle }), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onHideForced(!hideForced) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (hideForced) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (hideForced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.hide_forced_subs), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onUndetermined(!undetermined) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (undetermined) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (undetermined) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.undetermined_lang), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.audio_studio_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onPreferOriginal(!preferOriginal) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (preferOriginal) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (preferOriginal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.prefer_original), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        items(listOf("auto", "dubbed", "original", "commentary")) { code ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onAudioRole(code) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (audioRole == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (audioRole == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(when (code) { "dubbed" -> R.string.audio_role_dubbed; "original" -> R.string.audio_role_original; "commentary" -> R.string.audio_role_commentary; else -> R.string.audio_role_auto }), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onSkipSilence(!skipSilence) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (skipSilence) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (skipSilence) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.skip_silence), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        items(listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)) { v ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onSpeed(v) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (speed == v) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (speed == v) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(if (v == 1.0f) stringResource(R.string.speed_normal) else "${v}x", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.pitch_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(listOf(0.5f, 0.8f, 1.0f, 1.25f, 1.5f, 2.0f)) { v ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onPitch(v) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (pitch == v) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (pitch == v) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(if (v == 1.0f) stringResource(R.string.speed_normal) else "${v}x", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.playmode_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(PLAYMODE_OPTIONS) { (code, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onPlayMode(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (playMode == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (playMode == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Text(stringResource(R.string.appearance_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.theme_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(THEME_OPTIONS) { (code, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onTheme(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (theme == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (theme == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.accent_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                ACCENT_OPTIONS.forEach { (code, pair) ->
                    val (fill, label) = pair
                    val selected = accent == code
                    Box(
                        modifier = Modifier.size(40.dp)
                            .clip(CircleShape)
                            .background(fill)
                            .border(if (selected) 3.dp else 1.dp, if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable(onClickLabel = stringResource(label)) { onAccent(code) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) Icon(Icons.Rounded.CheckCircle, contentDescription = stringResource(label), tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }
            }
            Text(stringResource(R.string.accent_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.country_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        items(COUNTRY_OPTIONS) { (code, label) ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onCountry(code) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (country == code) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (country == code) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.data_note), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Text(
                stringResource(R.string.settings_data_line, favCount, histCount, plCount, dbSize),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedButton(onClick = { confirmFav = true }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.clear_favorites)) }
                androidx.compose.material3.OutlinedButton(onClick = { confirmHist = true }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.clear_history)) }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.perf_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onFeedParallel(!feedParallel) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (feedParallel) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (feedParallel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.perf_parallel), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.perf_parallel_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onFeedCache(!feedCache) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (feedCache) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (feedCache) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.perf_feedcache), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.perf_feedcache_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onPreloadNext(!preloadNext) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (preloadNext) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (preloadNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.perf_preload), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.perf_preload_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable(enabled = preloadNext, onClick = { onPreloadWifiOnly(!preloadWifiOnly) }).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (preloadWifiOnly) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (preloadNext && preloadWifiOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (preloadNext) 1f else 0.35f), modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.perf_preload_wifi), style = MaterialTheme.typography.bodyMedium, color = if (preloadNext) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.perf_preload_wifi_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onTitleTranslate(!titleTranslate) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (titleTranslate) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (titleTranslate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.title_translate), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.title_translate_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.offline_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().clickable { onOfflineMode(!offlineMode) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (offlineMode) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (offlineMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.offline_mode), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    Text(stringResource(R.string.offline_mode_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.cache_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Text(
                stringResource(R.string.cache_line_fmt, cacheSize, cacheMaxMb),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(listOf(100L, 250L, 500L, 1024L)) { mb ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onCacheMax(mb) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (cacheMaxMb == mb) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (cacheMaxMb == mb) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                Text(if (mb >= 1024) stringResource(R.string.cache_gb) else stringResource(R.string.cache_mb_fmt, mb.toInt()), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedButton(onClick = { confirmCache = true }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.clear_cache)) }
                androidx.compose.material3.OutlinedButton(onClick = onCopyDiagnostics, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.diag_copy)) }
            }
            Text(stringResource(R.string.diag_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.pins_title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
        }
        item {
            Text(
                stringResource(R.string.pins_line_fmt, pinsCount, pinsSize),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(listOf(512L, 1024L, 2048L, 0L)) { mb ->
            Row(modifier = Modifier.fillMaxWidth().clickable { onPinsWarn(mb) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (pinsWarnMb == mb) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked, contentDescription = null, tint = if (pinsWarnMb == mb) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(12.dp))
                val label = when {
                    mb == 0L -> stringResource(R.string.pins_warn_off)
                    mb >= 1024 -> stringResource(R.string.pins_warn_gb_fmt, (mb / 1024).toInt())
                    else -> stringResource(R.string.pins_warn_mb_fmt, mb.toInt())
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        item {
            if (pinsWarnMb > 0 && pinsCount > 0 && pinsBytes > pinsWarnMb * 1024 * 1024) {
                // soft reminder only: honest byte comparison; never blocks pinning
                Text(
                    stringResource(R.string.pins_over_warn),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedButton(onClick = { confirmPins = true }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.clear_pins)) }
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
