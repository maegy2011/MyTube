package com.mohamedadel.mytube.ui.feed

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.FeedCacheEntity
import com.mohamedadel.mytube.data.local.FeedJson
import com.mohamedadel.mytube.data.local.NotifCenter
import com.mohamedadel.mytube.data.local.PlaylistDao
import com.mohamedadel.mytube.data.local.SubscriptionEntity
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext

/**
 * Phase 2.3 slice 6 (2026-11-13): subscriptions feed extracted from the
 * God-Activity. DAO injected via configure(); prefs arrive as call params.
 * Every function <= 30 lines.
 */
class FeedViewModel(app: Application) : AndroidViewModel(app) {

    private var dao: PlaylistDao? = null
    fun configure(d: PlaylistDao) {
        if (dao != null) return
        dao = d
    }

    private val _feedItems = MutableStateFlow<List<VideoItem>>(emptyList())
    val feedItems: StateFlow<List<VideoItem>> = _feedItems.asStateFlow()
    private val _feedLoading = MutableStateFlow(false)
    val feedLoading: StateFlow<Boolean> = _feedLoading.asStateFlow()

    private suspend fun toastMain(s: String) = withContext(Dispatchers.Main) {
        try { Toast.makeText(getApplication(), s, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }

    fun refreshFeed(subs: List<SubscriptionEntity>, useParallel: Boolean, useCache: Boolean) {
        _feedLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val all = if (useParallel) fetchParallel(subs, useCache) else fetchSerial(subs, useCache)
                _feedItems.value = interleave(all)
                try { NotifCenter.recordFresh(dao ?: return@launch, all) } catch (_: Exception) { }
            } catch (_: Exception) {
                toastMain(try { getApplication<Application>().getString(R.string.toast_feed_fail) } catch (_: Exception) { "" })
            }
            _feedLoading.value = false
        }
    }

    private suspend fun oneChannel(channelUrl: String?, useCache: Boolean): List<VideoItem> {
        return try {
            val id = YouTubeRepo.channelIdFor(channelUrl) ?: return emptyList()
            if (useCache) {
                val hit = try { dao?.getFeedCache(id) } catch (_: Exception) { null }
                if (hit != null && System.currentTimeMillis() - hit.fetchedAt < 6 * 3600 * 1000L) {
                    val items = FeedJson.fromJson(hit.itemsJson)
                    if (items.isNotEmpty()) return items
                }
            }
            val rss = YouTubeRepo.feedFor(id)
            val fresh = if (rss.isNotEmpty()) rss else YouTubeRepo.channelVideos(channelUrl).take(15)
            if (useCache && fresh.isNotEmpty()) {
                try { dao?.upsertFeedCache(FeedCacheEntity(id, System.currentTimeMillis(), FeedJson.toJson(fresh))) } catch (_: Exception) { }
            }
            fresh
        } catch (_: Exception) { emptyList() }
    }

    private suspend fun CoroutineScope.fetchParallel(
        subs: List<SubscriptionEntity>,
        useCache: Boolean
    ): List<VideoItem> {
        val sem = Semaphore(3)
        return subs.map { sub ->
            async {
                sem.acquire()
                try { oneChannel(sub.channelUrl, useCache) } finally { sem.release() }
            }
        }.awaitAll().flatten()
    }

    private suspend fun fetchSerial(
        subs: List<SubscriptionEntity>,
        useCache: Boolean
    ): List<VideoItem> {
        return subs.flatMap { oneChannel(it.channelUrl, useCache) }
    }

    private fun interleave(all: List<VideoItem>): List<VideoItem> {
        if (all.size <= 1) return all
        val perChannel = all.groupBy { it.channelUrl ?: it.channel ?: "?" }.values.toList()
        val mixed = mutableListOf<VideoItem>()
        var idx = 0
        var added: Boolean
        do {
            added = false
            for (bucket in perChannel) {
                if (idx < bucket.size) { mixed += bucket[idx]; added = true }
            }
            idx++
        } while (added)
        return mixed
    }
}
