package com.mohamedadel.mytube.ui.library

import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toEntity
import com.mohamedadel.mytube.data.local.toVideoItem

/**
 * Phase 2.3 slice 29 (2026-11-13): playlist IO actions as LibraryViewModel
 * extensions (moved from PlaylistsRoute bodies). Fns <= 30 lines.
 */

    // slice 29: playlist IO actions (moved from PlaylistsRoute bodies).
suspend fun LibraryViewModel.loadPlaylistItems(pid: Long): List<com.mohamedadel.mytube.data.youtube.VideoItem> =
    try { (dao?.itemsOnce(pid) ?: emptyList()).map { it.toVideoItem() } } catch (_: Exception) { emptyList() }

suspend fun LibraryViewModel.createPlaylist(name: String, emoji: String) {
    try { dao?.insertPlaylist(com.mohamedadel.mytube.data.local.PlaylistEntity(name = name, emoji = emoji)) } catch (_: Exception) { }
}

suspend fun LibraryViewModel.saveEdits(pid: Long, name: String, emoji: String, mode: String, orderedIds: List<Long>): Boolean {
    return try {
        dao?.savePlaylistEdits(pid, name, emoji, mode, orderedIds)
        toastMain(str(com.mohamedadel.mytube.R.string.toast_playlist_saved))
        true
    } catch (_: Exception) { toastMain(str(com.mohamedadel.mytube.R.string.toast_playlist_save_fail)); false }
}

suspend fun LibraryViewModel.deletePlaylist(pid: Long) {
    try { dao?.deletePlaylist(pid) } catch (_: Exception) { }
}

suspend fun LibraryViewModel.clearPlaylist(pid: Long) {
    try { dao?.clearPlaylist(pid) } catch (_: Exception) { }
}

suspend fun LibraryViewModel.removeItems(pid: Long, ids: List<Long>) {
    try { dao?.deleteItems(pid, ids) } catch (_: Exception) { }
}

suspend fun LibraryViewModel.moveItem(pid: Long, iid: Long, targetPid: Long): Boolean {
    return try {
        val d = dao ?: return false
        val row = d.itemsOnce(pid).firstOrNull { it.id == iid }
        if (row == null) { toastMain(str(com.mohamedadel.mytube.R.string.toast_move_fail)); return false }
        val v = com.mohamedadel.mytube.data.youtube.VideoItem(id = row.videoId, title = row.title, channel = row.channel, channelUrl = row.channelUrl, thumbnail = row.thumbnail, durationSec = row.durationSec, viewCount = row.viewCount, url = row.url)
        val exists = d.itemsOnce(targetPid).any { e ->
            com.mohamedadel.mytube.data.youtube.VideoItem(id = e.videoId, title = e.title, channel = e.channel, channelUrl = e.channelUrl, thumbnail = e.thumbnail, durationSec = e.durationSec, viewCount = e.viewCount, url = e.url).favKey() == v.favKey()
        }
        if (exists) { toastMain(str(com.mohamedadel.mytube.R.string.toast_already_target)); return false }
        d.insertItems(listOf(v.toEntity(targetPid, d.itemCount(targetPid))))
        d.deleteItem(pid, iid)
        toastMain(str(com.mohamedadel.mytube.R.string.toast_moved))
        true
    } catch (_: Exception) { toastMain(str(com.mohamedadel.mytube.R.string.toast_move_fail)); false }
}

