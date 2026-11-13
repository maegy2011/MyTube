package com.mohamedadel.mytube.ui.backup

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.ChannelGroupEntity
import com.mohamedadel.mytube.data.local.ChannelGroupMemberEntity
import com.mohamedadel.mytube.data.local.FavoriteEntity
import com.mohamedadel.mytube.data.local.HistoryEntity
import com.mohamedadel.mytube.data.local.PlaylistDao
import com.mohamedadel.mytube.data.local.PlaylistEntity
import com.mohamedadel.mytube.data.local.PlaylistItemEntity
import com.mohamedadel.mytube.data.local.SubscriptionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Phase 2.3 slice 12 (2026-11-13): full JSON backup export/import extracted
 * from the God-Activity. DAO + prefs injected via configure(). Launchers
 * stay in the activity and forward Uris. Every function <= 30 lines.
 */
class BackupViewModel(app: Application) : AndroidViewModel(app) {

    private var dao: PlaylistDao? = null
    private var prefs: SharedPreferences? = null
    fun configure(d: PlaylistDao, p: SharedPreferences) {
        if (dao != null) return
        dao = d
        prefs = p
    }

    private fun str(resId: Int, vararg args: Any): String =
        try { getApplication<Application>().getString(resId, *args) } catch (_: Exception) { "" }

    private suspend fun toastMain(s: String) = withContext(Dispatchers.Main) {
        try { Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }

    private class Counts {
        var nPl = 0; var nFav = 0; var nHist = 0
        var nSkippedPlaylists = 0; var nSkippedItems = 0; var nRenamed = 0
    }

    fun backupInfoText(): String? {
        return try {
            val p = prefs ?: return null
            val at = try { p.getLong("backup_last_at", 0L) } catch (_: Exception) { 0L }
            val date = try {
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(at))
            } catch (_: Exception) { "" }
            val bytes = try { p.getLong("backup_last_bytes", 0L) } catch (_: Exception) { 0L }
            val size = if (bytes >= 1048576L) String.format(java.util.Locale.US, "%.1f MB", bytes / 1048576.0)
            else String.format(java.util.Locale.US, "%d KB", bytes / 1024)
            str(
                R.string.backup_last_fmt, date,
                try { p.getInt("backup_last_pl", 0) } catch (_: Exception) { 0 },
                try { p.getInt("backup_last_fav", 0) } catch (_: Exception) { 0 },
                try { p.getInt("backup_last_hist", 0) } catch (_: Exception) { 0 },
                size
            )
        } catch (_: Exception) { null }
    }

    fun exportFullBackup() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val root = buildBackupJson()
                shareBackup(root)
            } catch (_: Exception) { toastMain(str(R.string.backup_export_fail)) }
        }
    }

    private suspend fun buildBackupJson(): JSONObject {
        val d = dao ?: throw IllegalStateException("no dao")
        val root = JSONObject()
        root.put("app", "mytube")
        root.put("v", 1)
        root.put("at", System.currentTimeMillis())
        root.put("playlists", buildPlaylistsJson(d))
        root.put("favorites", buildFavHistJson(d.observeFavorites().first(), isFav = true))
        root.put("history", buildFavHistJson(d.historyPage(500, 0), isFav = false))
        root.put("subscriptions", buildSubsJson(d))
        root.put("groups", buildGroupsJson(d))
        root.put("prefs", buildPrefsJson())
        return root
    }

    private suspend fun buildPlaylistsJson(d: PlaylistDao): JSONArray {
        val jpls = JSONArray()
        for (pw in d.observePlaylists().first()) {
            val o = JSONObject()
            o.put("name", pw.name); o.put("emoji", pw.emoji); o.put("mode", pw.mode)
            val ja = JSONArray()
            for (it in d.itemsOnce(pw.id)) {
                val e = JSONObject()
                e.put("videoId", it.videoId); e.put("title", it.title); e.put("channel", it.channel)
                e.put("thumbnail", it.thumbnail); e.put("url", it.url)
                e.put("durationSec", it.durationSec ?: -1); e.put("viewCount", it.viewCount ?: -1)
                e.put("position", it.position)
                ja.put(e)
            }
            o.put("items", ja)
            jpls.put(o)
        }
        return jpls
    }

    private fun vrow(
        videoKey: String, videoId: String?, title: String?, channel: String?,
        thumb: String?, url: String?, dur: Long?, views: Long?
    ): JSONObject {
        val e = JSONObject()
        e.put("videoKey", videoKey); e.put("videoId", videoId); e.put("title", title)
        e.put("channel", channel); e.put("thumbnail", thumb); e.put("url", url)
        e.put("durationSec", dur ?: -1); e.put("viewCount", views ?: -1)
        return e
    }

    private fun buildFavHistJson(rows: List<Any>, isFav: Boolean): JSONArray {
        val j = JSONArray()
        for (r in rows) {
            if (isFav && r is com.mohamedadel.mytube.data.local.FavoriteEntity)
                j.put(vrow(r.videoKey, r.videoId, r.title, r.channel, r.thumbnail, r.url, r.durationSec, r.viewCount))
            else if (!isFav && r is com.mohamedadel.mytube.data.local.HistoryEntity)
                j.put(vrow(r.videoKey, r.videoId, r.title, r.channel, r.thumbnail, r.url, r.durationSec, r.viewCount))
        }
        return j
    }

    private suspend fun buildSubsJson(d: PlaylistDao): JSONArray {
        val js = JSONArray()
        for (x in d.observeSubscriptions().first()) {
            val e = JSONObject()
            e.put("channelUrl", x.channelUrl); e.put("name", x.name); e.put("avatar", x.avatar)
            js.put(e)
        }
        return js
    }

    private suspend fun buildGroupsJson(d: PlaylistDao): JSONArray {
        val jg = JSONArray()
        for (g in d.observeGroups().first()) {
            val o = JSONObject()
            o.put("name", g.name)
            val jm = JSONArray()
            for (m in d.observeGroupMembers(g.id).first()) jm.put(m.channelUrl)
            o.put("members", jm)
            jg.put(o)
        }
        return jg
    }

    private fun buildPrefsJson(): JSONObject {
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

    private suspend fun shareBackup(root: JSONObject) {
        val ctx = getApplication<Application>()
        val dir = java.io.File(ctx.cacheDir, "shared").apply { mkdirs() }
        val f = java.io.File(dir, "mytube_backup_" + java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US).format(java.util.Date()) + ".json")
        f.writeText(root.toString())
        try {
            prefs?.edit()
                ?.putLong("backup_last_at", System.currentTimeMillis())
                ?.putLong("backup_last_bytes", f.length())
                ?.putInt("backup_last_pl", root.optJSONArray("playlists")?.length() ?: 0)
                ?.putInt("backup_last_fav", root.optJSONArray("favorites")?.length() ?: 0)
                ?.putInt("backup_last_hist", root.optJSONArray("history")?.length() ?: 0)
                ?.apply()
        } catch (_: Exception) { }
        val uri = FileProvider.getUriForFile(ctx, "com.mohamedadel.mytube.fileprovider", f)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, str(R.string.backup_export).ifBlank { "backup" })
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
    }

    fun importFullBackup(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                val text = ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: return@launch
                val root = JSONObject(text)
                if (root.optString("app") != "mytube") { toastMain(str(R.string.backup_import_fail)); return@launch }
                val c = Counts()
                importPlaylists(root, c)
                importFavHist(root, c)
                importSubsGroups(root)
                importPrefs(root.optJSONObject("prefs"))
                toastMain(str(R.string.backup_import_ok_fmt, c.nPl, c.nFav, c.nHist))
                val skipped = c.nSkippedItems + c.nSkippedPlaylists
                if (skipped > 0 || c.nRenamed > 0)
                    toastMain(str(R.string.backup_import_skipped_fmt, skipped, c.nRenamed))
            } catch (_: Exception) { toastMain(str(R.string.backup_import_fail)) }
        }
    }

    private suspend fun importPlaylists(root: JSONObject, c: Counts) {
        val d = dao ?: return
        val usedNames = try { d.observePlaylists().first().map { it.name }.toMutableSet() } catch (_: Exception) { mutableSetOf() }
        val jpls = root.optJSONArray("playlists") ?: JSONArray()
        for (i in 0 until jpls.length()) {
            try {
                val o = jpls.getJSONObject(i)
                val base = o.optString("name", "import").ifBlank { "import" }.take(80)
                var finalName = base
                if (finalName in usedNames) {
                    var n = 1
                    var cand = "$base (import)"
                    while (cand in usedNames) { n++; cand = "$base (import $n)" }
                    finalName = cand.take(80)
                    c.nRenamed++
                }
                usedNames.add(finalName)
                val pid = d.insertPlaylist(
                    PlaylistEntity(
                        name = finalName,
                        emoji = o.optString("emoji", "\uD83C\uDFB5"), mode = o.optString("mode", "music")))
                val items = o.optJSONArray("items") ?: JSONArray()
                val rows = (0 until items.length()).mapNotNull { j ->
                    try {
                        parseImportItem(items.getJSONObject(j), pid, c)
                    } catch (_: Exception) { c.nSkippedItems++; null }
                }
                if (rows.isNotEmpty()) d.insertItems(rows)
                c.nPl++
            } catch (_: Exception) { c.nSkippedPlaylists++ }
        }
    }

    private fun parseImportItem(e: JSONObject, pid: Long, c: Counts): PlaylistItemEntity? {
        val vid = e.optString("videoId").ifBlank { null }
        val url = e.optString("url").ifBlank { null }
        if (vid == null && url == null) { c.nSkippedItems++; return null }
        return PlaylistItemEntity(
            playlistId = pid, videoId = vid,
            title = e.optString("title").ifBlank { null }, channel = e.optString("channel").ifBlank { null },
            thumbnail = e.optString("thumbnail").ifBlank { null }, url = url,
            durationSec = e.optLong("durationSec", -1).takeIf { it >= 0 }, viewCount = e.optLong("viewCount", -1).takeIf { it >= 0 },
            position = e.optInt("position", 0))
    }

    private fun histFromJson(e: JSONObject, playedAt: Long): HistoryEntity {
        return HistoryEntity(
            videoKey = e.optString("videoKey"), videoId = e.optString("videoId").ifBlank { null },
            title = e.optString("title").ifBlank { null }, channel = e.optString("channel").ifBlank { null },
            thumbnail = e.optString("thumbnail").ifBlank { null }, url = e.optString("url").ifBlank { null },
            durationSec = e.optLong("durationSec", -1).takeIf { it >= 0 }, viewCount = e.optLong("viewCount", -1).takeIf { it >= 0 },
            playedAt = playedAt)
    }

    private suspend fun importFavHist(root: JSONObject, c: Counts) {
        val d = dao ?: return
        val jf = root.optJSONArray("favorites") ?: JSONArray()
        for (i in 0 until jf.length()) {
            try {
                val h = histFromJson(jf.getJSONObject(i), System.currentTimeMillis())
                if (h.videoKey.isBlank() && h.videoId.isNullOrBlank() && h.url.isNullOrBlank()) { c.nSkippedItems++; continue }
                if (h.videoKey.isNotBlank() && d.favoriteCount(h.videoKey) == 0) {
                    d.insertFavorite(
                        FavoriteEntity(
                            videoKey = h.videoKey, videoId = h.videoId, title = h.title, channel = h.channel,
                            thumbnail = h.thumbnail, url = h.url, durationSec = h.durationSec, viewCount = h.viewCount))
                    c.nFav++
                }
            } catch (_: Exception) { c.nSkippedItems++ }
        }
        val jh = root.optJSONArray("history") ?: JSONArray()
        for (i in 0 until jh.length()) {
            try {
                val h = histFromJson(jh.getJSONObject(i), System.currentTimeMillis())
                if (h.videoKey.isBlank() && h.videoId.isNullOrBlank() && h.url.isNullOrBlank()) { c.nSkippedItems++; continue }
                d.upsertHistory(h); c.nHist++
            } catch (_: Exception) { c.nSkippedItems++ }
        }
    }

    private suspend fun importSubsGroups(root: JSONObject) {
        val d = dao ?: return
        val js = root.optJSONArray("subscriptions") ?: JSONArray()
        for (i in 0 until js.length()) {
            try {
                val e = js.getJSONObject(i)
                val u = e.optString("channelUrl")
                if (u.isNotBlank() && d.subscriptionCount(u) == 0)
                    d.insertSubscription(SubscriptionEntity(channelUrl = u, name = e.optString("name").ifBlank { null }, avatar = e.optString("avatar").ifBlank { null }))
            } catch (_: Exception) { }
        }
        val existingGroups = d.observeGroups().first().map { it.name }.toSet()
        val jg = root.optJSONArray("groups") ?: JSONArray()
        for (i in 0 until jg.length()) {
            try {
                val o = jg.getJSONObject(i)
                val name = o.optString("name")
                if (name.isBlank() || name in existingGroups) continue
                val gid = d.insertGroup(ChannelGroupEntity(name = name))
                val jm = o.optJSONArray("members") ?: JSONArray()
                for (j in 0 until jm.length()) {
                    try { d.addMember(ChannelGroupMemberEntity(groupId = gid, channelUrl = jm.optString(j))) }
                    catch (_: Exception) { }
                }
            } catch (_: Exception) { }
        }
    }

    private fun importPrefs(jp: JSONObject?) {
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
            else -> {
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
        }
    }
}
