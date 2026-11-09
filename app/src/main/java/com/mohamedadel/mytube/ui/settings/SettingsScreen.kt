package com.mohamedadel.mytube.ui.settings

import com.mohamedadel.mytube.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp

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
    "system" to R.string.theme_system
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
    speed: Float = 1.0f,
    onSpeed: (Float) -> Unit = {},
    pitch: Float = 1.0f,
    onPitch: (Float) -> Unit = {},
    favCount: Int,
    histCount: Int,
    plCount: Int,
    dbSize: String,
    onClearFavorites: () -> Unit,
    onClearHistory: () -> Unit
) {
    val deviceTag = deviceLang?.language ?: "ar"
    var confirmFav by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var confirmHist by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
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
            Text(
                stringResource(R.string.settings_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
