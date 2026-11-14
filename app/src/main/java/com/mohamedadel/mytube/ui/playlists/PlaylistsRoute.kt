package com.mohamedadel.mytube.ui.playlists

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import com.mohamedadel.mytube.data.local.PlaylistItemEntity
import com.mohamedadel.mytube.data.local.PlaylistWithCount
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.library.LibraryViewModel
import com.mohamedadel.mytube.ui.library.PlaylistCsvViewModel
import com.mohamedadel.mytube.ui.library.clearPlaylist
import com.mohamedadel.mytube.ui.library.createPlaylist
import com.mohamedadel.mytube.ui.library.deletePlaylist
import com.mohamedadel.mytube.ui.library.loadPlaylistItems
import com.mohamedadel.mytube.ui.library.moveItem
import com.mohamedadel.mytube.ui.library.removeItems
import com.mohamedadel.mytube.ui.library.saveEdits
import com.mohamedadel.mytube.ui.player.PlayerViewModel
import com.mohamedadel.mytube.ui.search.MainSearchViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Phase 3 slice 31 (2026-11-14): PlaylistsRoute extracted from MainActivity.
 * Pure wiring: snapshots + VM calls via host; bodies identical to the
 * in-activity version (slice 30 simplified them first).
 */
interface PlaylistsRouteHost {
    fun playlistsSnap(): List<PlaylistWithCount>
    fun openPlaylistIdSnap(): Long?
    fun setOpenPlaylistId(id: Long?)
    fun openItemsSnap(): List<PlaylistItemEntity>
    fun pinBadgesSnap(): Map<String, String>
    fun coversSnap(): Map<Long, List<String>>
    fun tabSortSnap(): String
    fun songSortSnap(): String
    fun playingSnap(): VideoItem?
    fun routeScope(): CoroutineScope
    fun libVm(): LibraryViewModel
    fun playerVm(): PlayerViewModel
    fun searchVm(): MainSearchViewModel
    fun csvVm(): PlaylistCsvViewModel
    fun routePrefs(): SharedPreferences
    fun playRouteVideo(v: VideoItem, inQueue: Boolean = false, expand: Boolean = false, forcePlay: Boolean = false)
    fun routeIsQueued(v: VideoItem): Boolean
    fun routeQuickPin(v: VideoItem)
    fun routeSmartBack()
    fun setRouteOptionsFor(v: VideoItem?)
    fun setRouteShowPlaylists(b: Boolean)
    fun routeCsvImport()
}

@Composable
fun PlaylistsRouteHost.PlaylistsRoute() {
    val lib = libVm()
    val pvm = playerVm()
    PlaylistsScreen(
        playlists = playlistsSnap(),
        openId = openPlaylistIdSnap(),
        items = openItemsSnap(),
        onOpen = { setOpenPlaylistId(it) },
        onCloseItems = { setOpenPlaylistId(null) },
        onPlayAll = { pid ->
            routeScope().launch {
                val list = lib.loadPlaylistItems(pid)
                if (list.isNotEmpty()) {
                    setRouteShowPlaylists(false)
                    setOpenPlaylistId(null)
                    searchVm().showPlaylistResults(list)
                    pvm.replaceQueue(list, 0)
                    pvm.setRadioOn(false)
                    pvm.setVideoModeQueue(playlistsSnap().firstOrNull { it.id == pid }?.mode == "video")
                    playRouteVideo(list[0], inQueue = true, expand = true, forcePlay = true)
                }
            }
        },
        onPlayFrom = { idx ->
            val list = openItemsSnap().map { it.toVideoItem() }
            if (idx in list.indices) {
                setRouteShowPlaylists(false)
                searchVm().showPlaylistResults(list)
                pvm.replaceQueue(list, idx)
                pvm.setRadioOn(false)
                pvm.setVideoModeQueue(playlistsSnap().firstOrNull { it.id == openPlaylistIdSnap() }?.mode == "video")
                playRouteVideo(list[idx], inQueue = true, expand = true, forcePlay = true)
            }
        },
        onCreate = { name, emoji ->
            routeScope().launch { lib.createPlaylist(name, emoji) }
        },
        onSaveEdits = { pid, name, emoji, mode, orderedIds ->
            routeScope().launch {
                lib.saveEdits(pid, name, emoji, mode, orderedIds)
            }
        },
        onDelete = { pid -> routeScope().launch { lib.deletePlaylist(pid); if (openPlaylistIdSnap() == pid) setOpenPlaylistId(null) } },
        onClearItems = { pid -> routeScope().launch { lib.clearPlaylist(pid) } },
        onMoveToPlaylist = { pid, iid, targetPid ->
            routeScope().launch { lib.moveItem(pid, iid, targetPid) }
        },
        // 2026-11-12 (runtime fix): hierarchical pop via smartBack — the old
        // inline close dropped fromLibrary and landed on home instead of Library.
        onBack = { routeSmartBack() },
        pinBadges = pinBadgesSnap(),
        onLongClick = { setRouteOptionsFor(it) },
        covers = coversSnap(),
        onQueueItems = { sel ->
            val fresh = sel.map { it.toVideoItem() }.filter { !routeIsQueued(it) }
            if (fresh.isNotEmpty()) {
                pvm.appendItems(fresh)
                if (playingSnap() == null) playRouteVideo(fresh[0], inQueue = true)
            }
        },
        onPinItems = { vs -> routeScope().launch { for (v in vs) { try { routeQuickPin(v) } catch (_: Exception) { } } } },
        onRemoveItems = { pid, ids -> routeScope().launch { lib.removeItems(pid, ids) } },
        onExport = { pid -> csvVm().exportPlaylistCsv(pid) },
        // screens-fix Library P1-L4: CSV import entry point.
        onImport = { try { routeCsvImport() } catch (_: Exception) { } },
        tabSort = tabSortSnap(),
        onTabSort = { v -> lib.setPlTabSort(v, routePrefs()) },
        songSort = songSortSnap(),
        onSongSort = { v -> lib.setPlSongSort(v, openPlaylistIdSnap(), routePrefs()) }
    )
}
