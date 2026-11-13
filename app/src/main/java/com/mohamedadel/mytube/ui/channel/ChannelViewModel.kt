package com.mohamedadel.mytube.ui.channel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.ChannelDetail
import com.mohamedadel.mytube.data.youtube.RemotePlaylist
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 2.3 slice 8 (2026-11-13): channel screen data extracted from the
 * God-Activity. Pure network+state; player collapse stays with the caller.
 * Every function <= 30 lines.
 */
class ChannelViewModel(app: Application) : AndroidViewModel(app) {

    private fun errChannel(): String =
        try { getApplication<Application>().getString(R.string.err_channel_load) } catch (_: Exception) { "" }

    private suspend fun toastMain(s: String) = withContext(Dispatchers.Main) {
        try { Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }

    private val _channelScreenUrl = MutableStateFlow<String?>(null)
    val channelScreenUrl: StateFlow<String?> = _channelScreenUrl.asStateFlow()
    private val _channelScreenVideos = MutableStateFlow<List<VideoItem>>(emptyList())
    val channelScreenVideos: StateFlow<List<VideoItem>> = _channelScreenVideos.asStateFlow()
    private val _channelScreenLoading = MutableStateFlow(false)
    val channelScreenLoading: StateFlow<Boolean> = _channelScreenLoading.asStateFlow()
    private val _channelDetail = MutableStateFlow<ChannelDetail?>(null)
    val channelDetail: StateFlow<ChannelDetail?> = _channelDetail.asStateFlow()
    private val _channelTabsLoaded = MutableStateFlow(setOf<Int>())
    private val _channelShorts = MutableStateFlow<List<VideoItem>>(emptyList())
    val channelShorts: StateFlow<List<VideoItem>> = _channelShorts.asStateFlow()
    private val _channelShortsLoading = MutableStateFlow(false)
    val channelShortsLoading: StateFlow<Boolean> = _channelShortsLoading.asStateFlow()
    private val _channelLive = MutableStateFlow<List<VideoItem>>(emptyList())
    val channelLive: StateFlow<List<VideoItem>> = _channelLive.asStateFlow()
    private val _channelLiveLoading = MutableStateFlow(false)
    val channelLiveLoading: StateFlow<Boolean> = _channelLiveLoading.asStateFlow()
    private val _channelPls = MutableStateFlow<List<RemotePlaylist>>(emptyList())
    val channelPls: StateFlow<List<RemotePlaylist>> = _channelPls.asStateFlow()
    private val _channelPlsLoading = MutableStateFlow(false)
    val channelPlsLoading: StateFlow<Boolean> = _channelPlsLoading.asStateFlow()

    fun openChannel(url: String?) {
        if (url.isNullOrBlank()) return
        _channelScreenUrl.value = url
        _channelDetail.value = null
        _channelScreenVideos.value = emptyList()
        _channelScreenLoading.value = true
        _channelShorts.value = emptyList()
        _channelLive.value = emptyList()
        _channelPls.value = emptyList()
        _channelTabsLoaded.value = emptySet()
        viewModelScope.launch {
            try { _channelDetail.value = YouTubeRepo.channelDetail(url) } catch (_: Exception) { }
            try { _channelScreenVideos.value = YouTubeRepo.channelVideos(url) } catch (_: Exception) { }
            if (_channelDetail.value == null && _channelScreenVideos.value.isEmpty()) {
                toastMain(errChannel())
            }
            _channelScreenLoading.value = false
        }
    }

    fun closeChannel() { _channelScreenUrl.value = null }

    fun loadChannelTab(i: Int, url: String) {
        if (_channelTabsLoaded.value.contains(i)) return
        _channelTabsLoaded.value = _channelTabsLoaded.value + i
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (i) {
                    1 -> { setShortsLoading(true); _channelShorts.value = YouTubeRepo.channelTab(url, listOf("short")) }
                    2 -> { setLiveLoading(true); _channelLive.value = YouTubeRepo.channelTab(url, listOf("live", "stream")) }
                    3 -> { setPlsLoading(true); _channelPls.value = YouTubeRepo.channelPlaylists(url) }
                }
            } catch (_: Exception) { } finally {
                setShortsLoading(false); setLiveLoading(false); setPlsLoading(false)
            }
        }
    }

    private fun setShortsLoading(v: Boolean) { _channelShortsLoading.value = v }
    private fun setLiveLoading(v: Boolean) { _channelLiveLoading.value = v }
    private fun setPlsLoading(v: Boolean) { _channelPlsLoading.value = v }
}
