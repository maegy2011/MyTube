package com.mohamedadel.mytube.ui.library

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.PlaylistDao
import com.mohamedadel.mytube.data.local.PlaylistEntity
import com.mohamedadel.mytube.data.local.PlaylistItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Phase 2.3 slice 11 (2026-11-13): playlist CSV export/import extracted
 * from the God-Activity. DAO injected via configure(). The GetContent
 * launcher stays in the activity and forwards the Uri.
 * Every function <= 30 lines.
 */
class PlaylistCsvViewModel(app: Application) : AndroidViewModel(app) {

    private var dao: PlaylistDao? = null
    fun configure(d: PlaylistDao) {
        if (dao != null) return
        dao = d
    }

    private fun str(resId: Int, vararg args: Any): String =
        try { getApplication<Application>().getString(resId, *args) } catch (_: Exception) { "" }

    private suspend fun toastMain(s: String) = withContext(Dispatchers.Main) {
        try { Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }

    fun exportPlaylistCsv(pid: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rows = dao?.itemsOnce(pid) ?: emptyList()
                if (rows.isEmpty()) { toastMain(str(R.string.toast_csv_empty)); return@launch }
                shareCsv(buildCsv(rows), pid, "text/csv", "mytube_playlist_$pid.csv")
            } catch (_: Exception) {
                exportFallback(pid)
            }
        }
    }

    private fun buildCsv(rows: List<PlaylistItemEntity>): String {
        val sb = StringBuilder("video_id,title,channel,url,duration_sec\n")
        for (r in rows) {
            sb.append(q(r.videoId)).append(',').append(q(r.title)).append(',').append(q(r.channel)).append(',')
                .append(q(r.url)).append(',').append(r.durationSec ?: "").append('\n')
        }
        return sb.toString()
    }

    private fun q(v: String?): String = "\"" + (v ?: "").replace("\"", "\"\"") + "\""

    private suspend fun shareCsv(text: String, pid: Long, mime: String, fileName: String) {
        val ctx = getApplication<Application>()
        val dir = File(ctx.cacheDir, "shared").apply { mkdirs() }
        val f = File(dir, fileName)
        f.writeText(text)
        val uri = FileProvider.getUriForFile(ctx, "com.mohamedadel.mytube.fileprovider", f)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, str(R.string.export_csv).ifBlank { "CSV" })
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(chooser)
    }

    private suspend fun exportFallback(pid: Long) {
        try {
            val rows = (dao?.itemsOnce(pid) ?: emptyList()).take(50)
            val sb = StringBuilder()
            for (r in rows) sb.append(r.title ?: r.url ?: "").append('\n')
            val ctx = getApplication<Application>()
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, sb.toString())
            }
            val chooser = Intent.createChooser(send, "CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(chooser)
        } catch (_: Exception) { toastMain(str(R.string.toast_csv_fail)) }
    }

    fun importPlaylistCsv(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val ctx = getApplication<Application>()
                val text = ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: return@launch
                val lines = text.lines().filter { it.isNotBlank() }
                if (lines.size < 2) { toastMain(str(R.string.import_invalid)); return@launch }
                val rows = parseRows(lines)
                if (rows.second.isNotEmpty()) dao?.insertItems(rows.second)
                toastMain(str(R.string.toast_csv_imported_fmt, rows.second.size))
            } catch (_: Exception) { toastMain(str(R.string.backup_import_fail)) }
        }
    }

    private suspend fun parseRows(lines: List<String>): Pair<Long, List<PlaylistItemEntity>> {
        val header = splitCsv(lines[0]).map { it.trim().lowercase() }
        val hasId = header.getOrNull(0) == "video_id"
        val pid = dao?.insertPlaylist(
            PlaylistEntity(
                name = ("CSV " + java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US).format(java.util.Date())).take(80),
                emoji = "\uD83C\uDFB5", mode = "music")
        ) ?: 0L
        var n = 0
        val rows = lines.drop(1).mapNotNull { ln ->
            try {
                val c = splitCsv(ln)
                if (c.all { it.isBlank() }) return@mapNotNull null
                val (vid, title, channel, url, dur) = if (hasId) {
                    listOf(c.getOrNull(0), c.getOrNull(1), c.getOrNull(2), c.getOrNull(3), c.getOrNull(4))
                } else {
                    listOf(null, c.getOrNull(0), c.getOrNull(1), c.getOrNull(2), c.getOrNull(3))
                }
                if (title.isNullOrBlank() && url.isNullOrBlank()) return@mapNotNull null
                n++
                PlaylistItemEntity(
                    playlistId = pid, videoId = vid?.ifBlank { null },
                    title = title?.ifBlank { null }, channel = channel?.ifBlank { null },
                    thumbnail = null, url = url?.ifBlank { null },
                    durationSec = dur?.trim()?.toLongOrNull(), viewCount = null, position = n)
            } catch (_: Exception) { null }
        }
        return pid to rows
    }

    private fun splitCsv(line: String): List<String> {
        val out = mutableListOf<String>()
        val cur = StringBuilder()
        var inQ = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (inQ) {
                if (c == '"') {
                    if (i + 1 < line.length && line[i + 1] == '"') { cur.append('"'); i++ } else inQ = false
                } else cur.append(c)
            } else {
                if (c == '"') inQ = true else if (c == ',') { out.add(cur.toString()); cur.clear() } else cur.append(c)
            }
            i++
        }
        out.add(cur.toString())
        return out
    }
}
