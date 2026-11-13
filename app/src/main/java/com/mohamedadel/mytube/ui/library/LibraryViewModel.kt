package com.mohamedadel.mytube.ui.library

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.ChannelGroupEntity
import com.mohamedadel.mytube.data.local.ChannelGroupMemberEntity
import com.mohamedadel.mytube.data.local.HistoryEntity
import com.mohamedadel.mytube.data.local.PlaylistEntity
import com.mohamedadel.mytube.data.local.PlaylistItemEntity
import com.mohamedadel.mytube.data.local.PlaylistDao
import com.mohamedadel.mytube.data.local.SubscriptionEntity
import com.mohamedadel.mytube.data.local.escapeLike
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toFavorite
import com.mohamedadel.mytube.data.local.toHistory
import com.mohamedadel.mytube.data.youtube.VideoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 2.3 slice 3 (2026-11-13): library actions extracted from the
 * God-Activity. DAO injected via configure() (single Room instance stays
 * in the activity). Toasts use application context. Every function <= 30 lines.
 * Player-entangled pin flows (requestPin/quickPin/doUnpin) stay in activity.
 */
class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private var dao: PlaylistDao? = null
    private fun ctx() = getApplication<Application>()
    private fun str(id: Int, vararg a: Any): String =
        try { ctx().getString(id, *a) } catch (_: Exception) { "" }

    private suspend fun toastMain(s: String) = withContext(Dispatchers.Main) {
        try { Toast.makeText(ctx(), s, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }

    fun configure(d: PlaylistDao) {
        if (dao != null) return
        dao = d
    }

    private val _histSearchQuery = MutableStateFlow("")
    val histSearchQuery: StateFlow<String> = _histSearchQuery.asStateFlow()
    private val _histSearchResults = MutableStateFlow<List<HistoryEntity>>(emptyList())
    val histSearchResults: StateFlow<List<HistoryEntity>> = _histSearchResults.asStateFlow()
    private val _pinsCount = MutableStateFlow(0)
    val pinsCount: StateFlow<Int> = _pinsCount.asStateFlow()
    private val _pinsSizeLabel = MutableStateFlow("—")
    val pinsSizeLabel: StateFlow<String> = _pinsSizeLabel.asStateFlow()
    private val _pinsBytes = MutableStateFlow(0L)
    val pinsBytes: StateFlow<Long> = _pinsBytes.asStateFlow()
    private val _histCount = MutableStateFlow(0)
    val histCount: StateFlow<Int> = _histCount.asStateFlow()

    private val _plCovers = MutableStateFlow(mapOf<Long, List<String>>())
    val plCovers: StateFlow<Map<Long, List<String>>> = _plCovers.asStateFlow()
    private val _plTabSort = MutableStateFlow("custom")
    val plTabSort: StateFlow<String> = _plTabSort.asStateFlow()
    private val _plSongSort = MutableStateFlow("added")
    val plSongSort: StateFlow<String> = _plSongSort.asStateFlow()

    fun refreshPlCovers(playlists: List<com.mohamedadel.mytube.data.local.PlaylistWithCount>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val d = dao ?: return@launch
                val m = mutableMapOf<Long, List<String>>()
                for (pl in playlists) {
                    try { m[pl.id] = d.playlistThumbs(pl.id) } catch (_: Exception) { }
                }
                _plCovers.value = m
            } catch (_: Exception) { }
        }
    }

    fun setPlTabSort(v: String, prefs: android.content.SharedPreferences) {
        _plTabSort.value = v
        try { prefs.edit().putString("pl_tab_sort", v).apply() } catch (_: Exception) { }
    }

    fun setPlSongSort(v: String, pid: Long?, prefs: android.content.SharedPreferences) {
        _plSongSort.value = v
        try { pid?.let { prefs.edit().putString("pl_sort_" + it, v).apply() } } catch (_: Exception) { }
    }

    fun loadPlSorts(pid: Long?, prefs: android.content.SharedPreferences) {
        try { _plTabSort.value = prefs.getString("pl_tab_sort", "custom") ?: "custom" } catch (_: Exception) { }
        try { _plSongSort.value = if (pid == null) "added" else (prefs.getString("pl_sort_" + pid, "added") ?: "added") } catch (_: Exception) { }
    }
    private val _cacheSizeLabel = MutableStateFlow("—")
    val cacheSizeLabel: StateFlow<String> = _cacheSizeLabel.asStateFlow()
    private val _groupCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val groupCounts: StateFlow<Map<Long, Int>> = _groupCounts.asStateFlow()

    fun toggleFavorite(v: VideoItem) {
        viewModelScope.launch {
            try {
                val d = dao ?: return@launch
                val key = v.favKey()
                if (d.favoriteCount(key) > 0) {
                    d.deleteFavorite(key)
                    toastMain(str(R.string.toast_fav_removed))
                } else {
                    d.insertFavorite(v.toFavorite())
                    toastMain(str(R.string.toast_fav_added))
                }
            } catch (_: Exception) { toastMain(str(R.string.toast_fav_fail)) }
        }
    }

    fun recordHistory(v: VideoItem) {
        viewModelScope.launch {
            try {
                val d = dao ?: return@launch
                d.upsertHistory(v.toHistory())
                d.trimHistory()
            } catch (_: Exception) { toastMain(str(R.string.toast_hist_fail)) }
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            try { dao?.clearFavorites(); toastMain(str(R.string.toast_fav_cleared)) }
            catch (_: Exception) { toastMain(str(R.string.toast_fav_clear_fail)) }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try { dao?.clearHistory(); toastMain(str(R.string.toast_hist_cleared)) }
            catch (_: Exception) { toastMain(str(R.string.toast_hist_clear_fail)) }
        }
    }

    fun toggleSubscription(url: String?, name: String?, avatar: String?) {
        if (url.isNullOrBlank()) return
        viewModelScope.launch {
            try {
                val d = dao ?: return@launch
                if (d.subscriptionCount(url) > 0) {
                    d.deleteSubscription(url)
                    toastMain(str(R.string.sub_removed))
                } else {
                    d.insertSubscription(SubscriptionEntity(channelUrl = url, name = name, avatar = avatar))
                    toastMain(str(R.string.sub_added))
                }
            } catch (_: Exception) { }
        }
    }

    fun runHistSearch(q: String) {
        _histSearchQuery.value = q
        if (q.isBlank()) { _histSearchResults.value = emptyList(); return }
        viewModelScope.launch(Dispatchers.IO) {
            try { _histSearchResults.value = dao?.historySearch("%" + escapeLike(q.trim()) + "%") ?: emptyList() } catch (_: Exception) { }
        }
    }

    fun refreshPinsLabel(pinsSize: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = try { dao?.pinsTotalSize() ?: 0L } catch (_: Exception) { 0L }
            val label = try {
                if (bytes < 1048576) str(R.string.db_kb, (bytes / 1024).toInt())
                else str(R.string.db_mb, bytes / 1048576.0)
            } catch (_: Exception) { "—" }
            _pinsCount.value = pinsSize
            _pinsSizeLabel.value = label
            _pinsBytes.value = bytes
        }
    }

    fun refreshCacheLabel() {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = try { com.mohamedadel.mytube.playback.MediaCache.bytesUsed(ctx()) } catch (_: Exception) { 0L }
            _cacheSizeLabel.value = try {
                if (bytes < 1048576) str(R.string.db_kb, (bytes / 1024).toInt())
                else str(R.string.db_mb, bytes / 1048576.0)
            } catch (_: Exception) { "—" }
        }
    }

    fun clearMediaCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try { com.mohamedadel.mytube.playback.MediaCache.clear(ctx()) } catch (_: Exception) { }
            refreshCacheLabel()
            toastMain(str(R.string.toast_cache_cleared))
        }
    }

    fun loadHistCount() {
        viewModelScope.launch(Dispatchers.IO) {
            try { _histCount.value = dao?.historyCount() ?: 0 } catch (_: Exception) { }
        }
    }

    fun refreshGroupCounts(gs: List<ChannelGroupEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _groupCounts.value = gs.associate { g ->
                    g.id to try { dao?.memberCount(g.id) ?: 0 } catch (_: Exception) { 0 }
                }
            } catch (_: Exception) { }
        }
    }

    fun createGroup(name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try { dao?.insertGroup(ChannelGroupEntity(name = n)) } catch (_: Exception) { }
        }
    }

    fun renameGroup(g: ChannelGroupEntity, name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try { dao?.renameGroup(g.id, n) } catch (_: Exception) { }
        }
    }

    fun deleteGroup(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try { dao?.deleteGroup(id) } catch (_: Exception) { }
        }
    }

    fun addToGroup(gid: Long, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exists = try { dao?.memberUrls(gid)?.contains(url) ?: true } catch (_: Exception) { true }
                if (!exists) dao?.addMember(ChannelGroupMemberEntity(groupId = gid, channelUrl = url))
            } catch (_: Exception) { }
        }
    }

    fun removeFromGroup(gid: Long, url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try { dao?.removeMember(gid, url) } catch (_: Exception) { }
        }
    }

    fun saveRemoteList(list: List<VideoItem>, remoteTitle: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val d = dao ?: return@launch
                val name = ((remoteTitle ?: "Remote").take(80) + " (import)")
                val pid = d.insertPlaylist(PlaylistEntity(name = name, emoji = "🎵", mode = "music"))
                val rows = list.mapIndexed { i, v ->
                    PlaylistItemEntity(
                        playlistId = pid, videoId = v.id, title = v.title, channel = v.channel,
                        thumbnail = v.thumbnail, url = v.url, durationSec = v.durationSec,
                        viewCount = v.viewCount, position = i)
                }
                if (rows.isNotEmpty()) d.insertItems(rows)
                toastMain(str(R.string.toast_csv_imported_fmt, rows.size))
            } catch (_: Exception) { toastMain(str(R.string.toast_playlist_save_fail)) }
        }
    }
}
