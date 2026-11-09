package com.mohamedadel.mytube.ui.settings

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Settings screen extracted from MainActivity (split step 2).
// Pure UI: all state comes in via params, all writes go out via callbacks.

val LANG_OPTIONS = listOf("ar" to "العربية", "en" to "الإنجليزية", "fr" to "الفرنسية", "es" to "الإسبانية", "de" to "الألمانية")

val COUNTRY_OPTIONS = listOf(
    "EG" to "🇪🇬 مصر", "SA" to "🇸🇦 السعودية", "AE" to "🇦🇪 الإمارات",
    "KW" to "🇰🇼 الكويت", "QA" to "🇶🇦 قطر", "MA" to "🇲🇦 المغرب",
    "DZ" to "🇩🇿 الجزائر", "JO" to "🇯🇴 الأردن", "IQ" to "🇮🇶 العراق",
    "US" to "🇺🇸 أمريكا", "GB" to "🇬🇧 بريطانيا", "global" to "🌍 عالمي"
)

fun langLabel(code: String, deviceTag: String?): String = when (code) {
    "auto" -> "تلقائي (لغة الجهاز" + (deviceTag?.let { ": $it" } ?: "") + ")"
    "off" -> "إيقاف"
    "original" -> "الأصلية (لغة الفيديو أيا كانت)"
    else -> LANG_OPTIONS.firstOrNull { it.first == code }?.second ?: code
}

val THEME_OPTIONS = listOf(
    "dark" to "داكن (الافتراضي)",
    "light" to "فاتح (مريح لكبار السن)",
    "system" to "حسب النظام"
)

val QUALITY_OPTIONS = listOf(
    "audio-only" to "صوت فقط (الأخف — الافتراضي)",
    "auto" to "تلقائي حسب السرعة",
    "saver" to "توفير بيانات (أدنى دقة)",
    "high" to "عالية (أعلى دقة)"
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
    onTheme: (String) -> Unit
) {
    val deviceTag = deviceLang?.language ?: "ar"
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("الإعدادات", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface) }
        item {
            Text(
                "لغة الجهاز الحالية: ${deviceLang?.displayLanguage ?: deviceTag} ($deviceTag) — تُستخدم تلقائيًا ما لم تختر غيرها. اختياراتك تُحفظ دائمًا وتُطبق على كل فيديو.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        item {
            Text("لغة الترجمة المفضلة", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
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
                Text(langLabel(code, deviceTag), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("لغة الترجمة البديلة (عند غياب المفضلة)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
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
                Text(langLabel(code, deviceTag), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("لغة الصوت المفضلة", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
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
                Text(langLabel(code, deviceTag), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("لغة الصوت البديلة (عند غياب المفضلة)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
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
                Text(langLabel(code, deviceTag), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("جودة الفيديو الافتراضية (تُطبق على كل فيديو جديد)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
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
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("مظهر التطبيق", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
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
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Spacer(Modifier.height(4.dp))
            Text("بلد المحتوى (يحدد الرائج والبحث — اختيار يدوي فقط، بلا تتبع)", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
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
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        item {
            Text(
                "ملاحظة: الترجمة تُختار تلقائيًا عند توفرها بلغة التفضيل (اليدوية قبل المولّدة آليًا)، والصوت المدبلج/الأصلي يُطابق اللغة نفسها — ويمكن تغييرهما لأي فيديو من أزرار المشغل، فيُحفظ الاختيار الجديد تلقائيًا.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
