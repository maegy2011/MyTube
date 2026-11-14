package com.mohamedadel.mytube.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.media3.exoplayer.ExoPlayer
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.local.SubscriptionEntity
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.youtube.VideoItem

/**
 * Phase 3 slice 33 (2026-11-14): PlayerPane extracted from MainActivity.
 * Pure wiring over PlayerArea; all logic lives in host impl (activity).
 */
interface PlayerPaneHost {
    fun streamData(): StreamData?
    fun exo(): ExoPlayer?
    fun quality(): String
    fun qualityList(): List<String>
    fun isPlayingNow(): Boolean
    fun playerLoading(): Boolean
    fun playerPositionMs(): Long
    fun playerDurationMs(): Long
    fun playerBufferedPct(): Int
    fun playerError(): String?
    fun autoAdvance(): Boolean
    fun playMode(): String
    fun modeLabelOf(m: String): String
    fun shuffleOn(): Boolean
    fun isFullscreen(): Boolean
    fun prefShowSubs(): Boolean
    fun prefSubHideAuto(): Boolean
    fun subTrackIdxNow(): Int
    fun audioTrackIdxNow(): Int
    fun castAvailable(): Boolean
    // Phase 1 rebuild: explicit cast trigger (was missing — menu item was dead).
    fun castNowX()
    fun dlnaAvailable(): Boolean
    fun dlnaPlayX(r: com.mohamedadel.mytube.playback.DlnaManager.Renderer)
    fun isCasting(): Boolean
    fun sleepLeft(): String
    fun favoriteSet(): Set<String>
    fun prefShowRelated(): Boolean
    fun prefShowInfo(): Boolean
    fun prefShowComments(): Boolean
    fun subscriptions(): List<SubscriptionEntity>
    fun pinKeys(): Set<String>
    fun pinningKeys(): Set<String>
    fun pinProgressMap(): Map<String, Float>
    fun subStyle(): SubStyle
    fun translatedTitle(): String?
    fun qualityPick(q: String)
    fun togglePlayExo()
    fun seekExo(pos: Long)
    fun retryQuality()
    fun cycleModeX()
    fun toggleShuffleX()
    fun openQueue()
    fun closePlayerX()
    fun collapsePlayerX()
    fun toggleFullscreenX()
    fun setModeX(m: String)
    fun setShuffleX(b: Boolean)
    fun subtitlePick(i: Int)
    fun audioPick(i: Int)
    fun playNextX()
    fun playPrevX()
    fun openInYoutubeX(v: VideoItem)
    fun openSleep()
    fun toggleFavX(v: VideoItem)
    fun savePick(v: VideoItem)
    fun chapterSeekX(pos: Long)
    fun relatedPlayX(v: VideoItem)
    fun onPinX(v: VideoItem)
    fun subscribeX(v: VideoItem)
    fun channelClickX()
    fun commentsClickX()
    fun tagClickX(q: String)
    fun shareX(v: VideoItem)
    // Phase 1 rebuild: plain-link share (chooser in PlayerArea).
    fun sharePlainX(v: VideoItem)
    fun setRelatedOptions(v: VideoItem?)
    fun speedNow(): Float = 1f
    fun cycleSpeedX() {}
    fun audioQuickOn(): Boolean = false
    fun audioQuickX() {}
}

@Composable
fun PlayerPaneHost.PlayerPane(paneMod: Modifier = Modifier, cur: VideoItem, onEnterPip: () -> Unit) {
PlayerArea(
    modifier = paneMod,
    video = cur,
    artworkUrl = streamData()?.artworkUrl,
    player = exo(),
    quality = quality(),
    qualities = qualityList(),
    isPlaying = isPlayingNow(),
    playerLoading = playerLoading(),
    positionMs = playerPositionMs(),
    durationMs = playerDurationMs(),
    bufferedPct = playerBufferedPct(),
    errorMsg = playerError(),
    onQuality = { q -> qualityPick(q) },
    onTogglePlay = { togglePlayExo() },
    onSeek = { pos -> seekExo(pos) },
    onRetry = { retryQuality() },
    queueOn = autoAdvance(),
    playMode = playMode(),
    playModeLabel = modeLabelOf(playMode()),
    shuffleOn = shuffleOn(),
    onCycleMode = { cycleModeX() },
    onToggleShuffle = { toggleShuffleX() },
    onQueue = { openQueue() },
    onClose = { closePlayerX() },
    onCollapse = { collapsePlayerX() },
    isFullscreen = isFullscreen(),
    onToggleFullscreen = { toggleFullscreenX() },
    onSetMode = { setModeX(it) },
    onSetShuffle = { setShuffleX(it) },
    subtitles = if (!prefShowSubs()) emptyList() else (streamData()?.subtitles ?: emptyList()).let { if (prefSubHideAuto()) it.filter { t -> !t.auto } else it },
    subIdx = subTrackIdxNow(),
    onSubtitleTrack = { i -> subtitlePick(i) },
     audioTracks = streamData()?.audioTracks ?: emptyList(),
    audioIdx = audioTrackIdxNow(),
    onAudioTrack = { i -> audioPick(i) },
     onNext = { playNextX() },
    onPrev = { playPrevX() },
    onFallbackYoutube = { openInYoutubeX(cur) },
    onEnterPip = onEnterPip,
    castAvailable = castAvailable(),
    onCast = { castNowX() },
    dlnaAvailable = dlnaAvailable(),
    onDlnaPlay = { r -> dlnaPlayX(r) },
    isCasting = isCasting(),
    sleepLabel = sleepLeft().ifBlank { null },
    onSleepClick = { openSleep() },
    isFav = cur.favKey() in favoriteSet(),
    onToggleFav = { toggleFavX(cur) },
    onSave = { savePick(cur) },
    likeCount = streamData()?.likeCount,
    description = streamData()?.description,
    chapters = streamData()?.chapters ?: emptyList(),
    related = streamData()?.related ?: emptyList(),
    showRelated = prefShowRelated(),
    showInfo = prefShowInfo(),
    showSubs = prefShowSubs(),
    showCommentsPill = prefShowComments(),
    isLive = streamData()?.isLive == true,
    // Fix: chapter tap starts playback too — seeking a never-started player left silence.
    onChapterSeek = { pos -> chapterSeekX(pos) },
    onRelatedPlay = { rv -> relatedPlayX(rv) },
    isSubscribed = streamData()?.channelUrl != null && subscriptions().any { it.channelUrl == streamData()?.channelUrl },
    isPinned = pinKeys().contains(cur.favKey()),
    isPinning = pinningKeys().contains(cur.favKey()),
    pinProgress = pinProgressMap()[cur.favKey()],
    onPin = { onPinX(cur) },
     onSubscribe = { subscribeX(cur) },
    onChannelClick = { channelClickX() },
    onCommentsClick = { commentsClickX() },
     subStyle = subStyle(),
    category = streamData()?.category,
    tags = streamData()?.tags ?: emptyList(),
    titleOverride = translatedTitle(),
    onTagClick = { q -> tagClickX(q) },
    onShare = { shareX(cur) },
    onSharePlain = { sharePlainX(cur) },
    onRelatedOptions = { setRelatedOptions(it) },
    storyboards = streamData()?.storyboards ?: emptyList(),
    playbackSpeed = speedNow(),
    onSpeedCycle = { cycleSpeedX() },
    audioOnlyQuick = audioQuickOn(),
    onAudioOnlyQuick = { audioQuickX() }
)}
