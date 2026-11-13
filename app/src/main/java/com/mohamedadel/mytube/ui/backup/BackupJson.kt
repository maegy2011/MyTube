package com.mohamedadel.mytube.ui.backup

import com.mohamedadel.mytube.data.local.HistoryEntity
import com.mohamedadel.mytube.data.local.PlaylistItemEntity
import org.json.JSONObject

/**
 * Phase 2.3 slice 12c: row JSON helpers for BackupViewModel.
 * Every function <= 30 lines.
 */

internal class Counts {
    var nPl = 0; var nFav = 0; var nHist = 0
    var nSkippedPlaylists = 0; var nSkippedItems = 0; var nRenamed = 0
}

internal fun histFromJson(e: JSONObject, playedAt: Long): HistoryEntity {
    return HistoryEntity(
        videoKey = e.optString("videoKey"), videoId = e.optString("videoId").ifBlank { null },
        title = e.optString("title").ifBlank { null }, channel = e.optString("channel").ifBlank { null },
        thumbnail = e.optString("thumbnail").ifBlank { null }, url = e.optString("url").ifBlank { null },
        durationSec = e.optLong("durationSec", -1).takeIf { it >= 0 }, viewCount = e.optLong("viewCount", -1).takeIf { it >= 0 },
        playedAt = playedAt)
}

internal fun parseImportItem(e: JSONObject, pid: Long, c: Counts): PlaylistItemEntity? {
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

internal fun vrow(
    videoKey: String, videoId: String?, title: String?, channel: String?,
    thumb: String?, url: String?, dur: Long?, views: Long?
): JSONObject {
    val e = JSONObject()
    e.put("videoKey", videoKey); e.put("videoId", videoId); e.put("title", title)
    e.put("channel", channel); e.put("thumbnail", thumb); e.put("url", url)
    e.put("durationSec", dur ?: -1); e.put("viewCount", views ?: -1)
    return e
}
