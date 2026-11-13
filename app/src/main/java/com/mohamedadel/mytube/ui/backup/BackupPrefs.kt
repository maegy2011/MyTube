package com.mohamedadel.mytube.ui.backup

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Phase 2.3 slice 12b: prefs JSON helpers for BackupViewModel.
 * device-local pref_pin_location never roams (export skips nothing;
 * import skips it). Every function <= 30 lines.
 */
internal fun buildPrefsJson(prefs: SharedPreferences?): JSONObject {
    val jp = JSONObject()
    for ((k, v) in prefs?.all ?: emptyMap()) {
        try {
            when (v) {
                is Boolean -> jp.put(k, v)
                is Int -> jp.put(k, v)
                is Long -> jp.put(k, v)
                is Float -> jp.put(k, v.toDouble())
                is String -> jp.put(k, v)
                is Set<*> -> { val a = JSONArray(); for (e in v) a.put(e.toString()); jp.put(k, a) }
            }
        } catch (_: Exception) { }
    }
    return jp
}

internal fun importPrefs(prefs: SharedPreferences?, jp: JSONObject?) {
    if (jp == null) return
    try {
        val p = prefs ?: return
        val ed = p.edit()
        val cur = p.all
        val keys = jp.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            try {
                if (k == "pref_pin_location") continue
                applyPrefKey(ed, cur[k], jp, k)
            } catch (_: Exception) { }
        }
        ed.apply()
    } catch (_: Exception) { }
}

private fun applyPrefKey(ed: SharedPreferences.Editor, old: Any?, jp: JSONObject, k: String) {
    when {
        old is Boolean -> ed.putBoolean(k, jp.optBoolean(k, old))
        old is Int -> ed.putInt(k, jp.optInt(k, old))
        old is Long -> ed.putLong(k, jp.optLong(k, old))
        old is Float -> ed.putFloat(k, jp.optDouble(k, old.toDouble()).toFloat())
        old is String -> ed.putString(k, jp.optString(k, old))
        old is Set<*> -> {
            val a = jp.optJSONArray(k)
            if (a != null) ed.putStringSet(k, (0 until a.length()).map { a.optString(it) }.toSet())
        }
        else -> applyUnknownPrefKey(ed, jp, k)
    }
}

private fun applyUnknownPrefKey(ed: SharedPreferences.Editor, jp: JSONObject, k: String) {
    val v: Any? = jp.opt(k)
    when (v) {
        is Boolean -> ed.putBoolean(k, v)
        is Int -> ed.putInt(k, v)
        is Long -> ed.putLong(k, v)
        is Double -> ed.putFloat(k, v.toFloat())
        is String -> ed.putString(k, v)
        is JSONArray -> ed.putStringSet(k, (0 until v.length()).map { v.optString(it) }.toSet())
    }
}
