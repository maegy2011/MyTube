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

    fun closePlayer(stopAndClear: () -> Unit) {
        try { stopAndClear() } catch (_: Exception) { }
        setPlaying(null)
        setAutoAdvance(false)
        setRadioOn(false)
        setQueueIndex(-1)
    }

    // slice 27a: player playback states (stream/quality/loading/position/pin/age)
    private val _streamData = MutableStateFlow<com.mohamedadel.mytube.data.youtube.StreamData?>(null)
    val streamData: StateFlow<com.mohamedadel.mytube.data.youtube.StreamData?> = _streamData.asStateFlow()
    private val _qualityList = MutableStateFlow(listOf(com.mohamedadel.mytube.util.QUALITY_AUDIO_ONLY))
    val qualityList: StateFlow<List<String>> = _qualityList.asStateFlow()
    private val _quality = MutableStateFlow(com.mohamedadel.mytube.util.QUALITY_AUDIO_ONLY)
    val quality: StateFlow<String> = _quality.asStateFlow()
    private val _audioTrackIdx = MutableStateFlow(0)
    val audioTrackIdx: StateFlow<Int> = _audioTrackIdx.asStateFlow()
    private val _subTrackIdx = MutableStateFlow(-1)
    val subTrackIdx: StateFlow<Int> = _subTrackIdx.asStateFlow()
    private val _subTrackFor = MutableStateFlow<String?>(null)
    val subTrackFor: StateFlow<String?> = _subTrackFor.asStateFlow()
    private val _playerLoading = MutableStateFlow(false)
    val playerLoading: StateFlow<Boolean> = _playerLoading.asStateFlow()
    private val _playerError = MutableStateFlow<String?>(null)
    val playerError: StateFlow<String?> = _playerError.asStateFlow()
    private val _playerPositionMs = MutableStateFlow(0L)
    val playerPositionMs: StateFlow<Long> = _playerPositionMs.asStateFlow()
    private val _playerDurationMs = MutableStateFlow(0L)
    val playerDurationMs: StateFlow<Long> = _playerDurationMs.asStateFlow()
    private val _playerBufferedPct = MutableStateFlow(0)
    val playerBufferedPct: StateFlow<Int> = _playerBufferedPct.asStateFlow()
    private val _playingPinned = MutableStateFlow(false)
    val playingPinned: StateFlow<Boolean> = _playingPinned.asStateFlow()
    private val _agePending = MutableStateFlow<Triple<com.mohamedadel.mytube.data.youtube.VideoItem, Boolean, com.mohamedadel.mytube.data.youtube.StreamData?>?>(null)
    val agePending: StateFlow<Triple<com.mohamedadel.mytube.data.youtube.VideoItem, Boolean, com.mohamedadel.mytube.data.youtube.StreamData?>?> = _agePending.asStateFlow()

    fun setStreamData(s: com.mohamedadel.mytube.data.youtube.StreamData?) { _streamData.value = s }
    fun streamDataNow(): com.mohamedadel.mytube.data.youtube.StreamData? = _streamData.value
    fun setQualityList(l: List<String>) { _qualityList.value = l }
    fun setQuality(q: String) { _quality.value = q }
    fun qualityListNow(): List<String> = _qualityList.value
    fun autoAdvanceNow(): Boolean = _autoAdvance.value
    fun qualityNow(): String = _quality.value
    fun setAudioTrackIdx(i: Int) { _audioTrackIdx.value = i }
    fun audioTrackIdxNow(): Int = _audioTrackIdx.value
    fun setSubTrackIdx(i: Int) { _subTrackIdx.value = i }
    fun subTrackIdxNow(): Int = _subTrackIdx.value
    fun setSubTrackFor(k: String?) { _subTrackFor.value = k }
    fun subTrackForNow(): String? = _subTrackFor.value
    fun setPlayerLoading(b: Boolean) { _playerLoading.value = b }
    fun setPlayerError(s: String?) { _playerError.value = s }
    fun playerErrorNow(): String? = _playerError.value
    fun setPlayerPositionMs(v: Long) { _playerPositionMs.value = v }
    fun setPlayerDurationMs(v: Long) { _playerDurationMs.value = v }
    fun setPlayerBufferedPct(v: Int) { _playerBufferedPct.value = v }
    fun setPlayingPinned(b: Boolean) { _playingPinned.value = b }
    fun playingPinnedNow(): Boolean = _playingPinned.value
    fun setAgePending(t: Triple<com.mohamedadel.mytube.data.youtube.VideoItem, Boolean, com.mohamedadel.mytube.data.youtube.StreamData?>?) { _agePending.value = t }

    fun saveSession(prefs: android.content.SharedPreferences, positionMs: Long) {
        try {
            val q = queueNow().take(50)
            if (q.isEmpty() || queueIndexNow() !in q.indices) return
            prefs.edit()
                .putString("sess_favs", q.joinToString("\u001f") { it.favKey() })
                .putString("sess_urls", q.joinToString("\u001f") { it.url ?: "" })
                .putString("sess_titles", q.joinToString("\u001f") { (it.title ?: "").replace("\u001f", " ") })
                .putString("sess_ids", q.joinToString("\u001f") { it.id ?: "" })
                .putInt("sess_index", queueIndexNow())
                .putLong("sess_pos", positionMs)
                .apply()
        } catch (_: Exception) { }
    }
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
