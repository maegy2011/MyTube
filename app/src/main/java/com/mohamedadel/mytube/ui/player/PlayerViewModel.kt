package com.mohamedadel.mytube.ui.player

import androidx.lifecycle.ViewModel
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.playback.nextSequentialIndex
import com.mohamedadel.mytube.playback.prevSequentialIndex
import com.mohamedadel.mytube.playback.shuffledFavKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase 2.1 (2026-11-13): queue domain extracted from the God-Activity.
 * Owns queue/playing/mode state as StateFlows; ExoPlayer wiring
 * (playVideo/applyQuality) stays in MainActivity until slice 2.
 * Every function <= 30 lines; file <= 300 lines (plan constraint).
 */
class PlayerViewModel : ViewModel() {

    private val _queue = MutableStateFlow<List<VideoItem>>(emptyList())
    val queue: StateFlow<List<VideoItem>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _playing = MutableStateFlow<VideoItem?>(null)
    val playing: StateFlow<VideoItem?> = _playing.asStateFlow()

    private val _radioOn = MutableStateFlow(false)
    val radioOn: StateFlow<Boolean> = _radioOn.asStateFlow()

    private val _playMode = MutableStateFlow("order")
    val playMode: StateFlow<String> = _playMode.asStateFlow()

    private val _shuffleOn = MutableStateFlow(false)
    val shuffleOn: StateFlow<Boolean> = _shuffleOn.asStateFlow()

    private val _shuffleOrder = MutableStateFlow<List<String>>(emptyList())
    private val _sleepAtMs = MutableStateFlow<Long?>(null)
    val sleepAtMs: StateFlow<Long?> = _sleepAtMs.asStateFlow()
    fun setSleepAtMs(v: Long?) { _sleepAtMs.value = v }

    // ---- slice 5: navigation state + read accessors for PlayerNavigation ----
    private val _autoAdvance = MutableStateFlow(false)
    val autoAdvance: StateFlow<Boolean> = _autoAdvance.asStateFlow()
    fun setAutoAdvance(v: Boolean) { _autoAdvance.value = v }
    private val _videoModeQueue = MutableStateFlow(false)
    val videoModeQueue: StateFlow<Boolean> = _videoModeQueue.asStateFlow()
    fun setVideoModeQueue(v: Boolean) { _videoModeQueue.value = v }
    fun queueNow(): List<VideoItem> = _queue.value
    fun queueIndexNow(): Int = _queueIndex.value
    fun playingNow(): VideoItem? = _playing.value
    fun radioOnNow(): Boolean = _radioOn.value
    fun playModeNow(): String = _playMode.value
    fun shuffleOnNow(): Boolean = _shuffleOn.value

    fun initModes(mode: String, shuffle: Boolean, radio: Boolean) {
        _playMode.value = mode
        _shuffleOn.value = shuffle
        _radioOn.value = radio
    }

    fun setPlaying(v: VideoItem?) {
        _playing.value = v
    }

    fun setRadioOn(v: Boolean) {
        _radioOn.value = v
    }

    fun setShuffleOn(v: Boolean) {
        _shuffleOn.value = v
    }

    fun assignQueue(items: List<VideoItem>) {
        _queue.value = items
    }

    fun replaceQueue(items: List<VideoItem>, index: Int) {
        _queue.value = items
        _queueIndex.value = index
        _shuffleOrder.value = emptyList()
    }

    fun setQueueIndex(i: Int) {
        _queueIndex.value = i
    }

    fun appendItems(items: List<VideoItem>) {
        if (items.isEmpty()) return
        _queue.value = _queue.value + items
    }

    /** Radio refill trim: drop consumed head, keep last 50 + upcoming. */
    fun trimHead(maxSize: Int = 60) {
        val q = _queue.value
        val over = q.size - maxSize
        val idx = _queueIndex.value
        if (over > 0 && idx > 0) {
            val drop = minOf(over, idx)
            _queue.value = q.drop(drop)
            _queueIndex.value = idx - drop
        }
    }

    fun indexOfKey(key: String): Int =
        _queue.value.indexOfFirst { it.favKey() == key }

    fun itemAt(i: Int): VideoItem? = _queue.value.getOrNull(i)

    fun nextSequential(): VideoItem? =
        nextSequentialIndex(_queue.value.size, _queueIndex.value)
            ?.let { _queue.value.getOrNull(it) }

    fun prevSequential(): VideoItem? =
        prevSequentialIndex(_queueIndex.value)
            ?.let { _queue.value.getOrNull(it) }

    /** Draw next shuffled item; rebuilds order when empty/stale. */
    fun drawShuffled(): VideoItem? {
        val q = _queue.value
        if (q.isEmpty()) return null
        if (q.size == 1) return q[0]
        val cur = _playing.value?.favKey()
        val valid = _shuffleOrder.value.filter { k ->
            k != cur && q.any { it.favKey() == k }
        }
        val order = valid.ifEmpty {
            shuffledFavKeys(q.map { it.favKey() }, cur)
        }
        val nxt = order.firstNotNullOfOrNull { k ->
            q.firstOrNull { it.favKey() == k }
        } ?: return null
        _shuffleOrder.value = order.filter { it != nxt.favKey() }
        return nxt
    }

    fun setPlayMode(m: String) {
        _playMode.value = m
    }

    fun cycleMode(): String {
        val next = when (_playMode.value) {
            "order" -> "repeatAll"
            "repeatAll" -> "repeatOne"
            else -> "order"
        }
        _playMode.value = next
        return next
    }

    fun modeLabel(m: String): String = when (m) {
        "repeatAll" -> "Repeat all"
        "repeatOne" -> "Repeat one"
        else -> "In order"
    }
}
