package com.mohamedadel.mytube.ui.player

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.ForwardingPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.youtube.AudioTrackItem
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.util.formatDuration
import com.mohamedadel.mytube.util.formatMs
import com.mohamedadel.mytube.util.formatViews
import com.mohamedadel.mytube.util.shortQuality
import kotlinx.coroutines.delay

// Full player extracted from MainActivity (split step 5).
// Pure UI: every behavior arrives via callbacks; the wrapped native controller
// drives queue/modes through onNext/onPrev/onSetMode/onSetShuffle.

@Composable
fun PlayerArea(
    video: VideoItem,
    player: ExoPlayer?,
    quality: String,
    qualities: List<String>,
    isPlaying: Boolean,
    playerLoading: Boolean,
    positionMs: Long = 0L,
    durationMs: Long = 0L,
    bufferedPct: Int = 0,
    errorMsg: String? = null,
    onQuality: (String) -> Unit,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit = {},
    onRetry: () -> Unit = {},
    queueOn: Boolean = false,
    onNext: () -> Unit = {},
    onPrev: () -> Unit = {},
    playMode: String = "order",
    playModeLabel: String = "بالترتيب",
    shuffleOn: Boolean = false,
    onSetMode: (String) -> Unit = {},
    onSetShuffle: (Boolean) -> Unit = {},
    onCycleMode: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onQueue: () -> Unit = {},
    onClose: () -> Unit = {},
    audioTracks: List<AudioTrackItem> = emptyList(),
    audioIdx: Int = 0,
    onAudioTrack: (Int) -> Unit = {},
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onFallbackYoutube: () -> Unit,
    onEnterPip: () -> Unit,
    sleepLabel: String? = null,
    onSleepClick: () -> Unit = {},
    isFav: Boolean = false,
    onToggleFav: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var langExpanded by remember { mutableStateOf(false) }
    // TEMPORARY EXPERIMENT (native-controls test): hide all our custom bars, use the full native ExoPlayer controller.
    // Media3 ships Arabic strings (values-ar), so the native buttons appear Arabic on an Arabic device.
    // To revert: set nativeOnly = false.
    val nativeOnly = true
    // professional controls: visible on tap, auto-hide after 3s while playing
    var controlsVisible by remember { mutableStateOf(true) }
    // in-player volume (0..1) + mute; applied to ExoPlayer, remembered per session
    var volume by remember { mutableStateOf(1f) }
    var muted by remember { mutableStateOf(false) }
    LaunchedEffect(volume, muted, player) {
        try { player?.volume = if (muted) 0f else volume.coerceIn(0f, 1f) } catch (_: Exception) { }
    }
    LaunchedEffect(controlsVisible, isPlaying, playerLoading, errorMsg) {
        if (controlsVisible && isPlaying && !playerLoading && errorMsg == null) {
            delay(3000)
            controlsVisible = false
        }
    }
    ElevatedCard(
        modifier = if (isFullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().padding(16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column {
            Box(
                // nativeOnly: NO tap interceptor — every touch must reach PlayerView so its controller shows.
                // (the old custom-bar toggle stole taps and the controller never appeared)
                modifier = if (isFullscreen) Modifier.fillMaxSize()
                    else if (!nativeOnly) Modifier.fillMaxWidth().height(220.dp).clickable(enabled = errorMsg == null) { controlsVisible = !controlsVisible }
                    else Modifier.fillMaxWidth().height(220.dp)
            ) {
                if (quality != "audio-only (default)" && player != null) {
                    AndroidView(
                        factory = { c ->
                            PlayerView(c).apply {
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                // full native controller (Arabic on Arabic devices via media3 values-ar);
                                // the fullscreen button appears because we set its click listener below.
                                // Wrapped player: native prev/next/shuffle/repeat drive OUR queue/modes
                                // (single-item timeline would leave them dead otherwise).
                                val wrapped = object : ForwardingPlayer(player!!) {
                                    override fun seekToNext() { onNext() }
                                    override fun seekToPrevious() { onPrev() }
                                    override fun setRepeatMode(m: Int) {
                                        onSetMode(when (m) {
                                            Player.REPEAT_MODE_ONE -> "repeatOne"
                                            Player.REPEAT_MODE_ALL -> "repeatAll"
                                            else -> "order"
                                        })
                                    }
                                    override fun getRepeatMode(): Int = when (playMode) {
                                        "repeatOne" -> Player.REPEAT_MODE_ONE
                                        "repeatAll" -> Player.REPEAT_MODE_ALL
                                        else -> Player.REPEAT_MODE_OFF
                                    }
                                    override fun setShuffleModeEnabled(b: Boolean) { onSetShuffle(b) }
                                    override fun getShuffleModeEnabled(): Boolean = shuffleOn
                                    override fun getAvailableCommands(): Player.Commands =
                                        super.getAvailableCommands().buildUpon()
                                            .add(Player.COMMAND_SEEK_TO_NEXT)
                                            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                                            .build()
                                }
                                this.player = wrapped
                                useController = true
                                setShowPreviousButton(true)
                                setShowNextButton(true)
                                setShowRewindButton(true)
                                setShowFastForwardButton(true)
                                setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE or RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL)
                                setShowShuffleButton(true)
                                setShowSubtitleButton(true)
                                setShowVrButton(true)
                            }
                        },
                        update = { v ->
                            v.setFullscreenButtonClickListener { onToggleFullscreen() }
                            v.controllerShowTimeoutMs = 5000
                            v.showController()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (nativeOnly && player != null) {
                    // audio-only WITH native controls: PlayerView shows the artwork (from MediaMetadata)
                    // + full controller (play/pause, seek bar, CC, speed) over it. No video surface needed.
                    AndroidView(
                        factory = { c ->
                            PlayerView(c).apply {
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                val wrapped = object : ForwardingPlayer(player!!) {
                                    override fun seekToNext() { onNext() }
                                    override fun seekToPrevious() { onPrev() }
                                    override fun setRepeatMode(m: Int) {
                                        onSetMode(when (m) {
                                            Player.REPEAT_MODE_ONE -> "repeatOne"
                                            Player.REPEAT_MODE_ALL -> "repeatAll"
                                            else -> "order"
                                        })
                                    }
                                    override fun getRepeatMode(): Int = when (playMode) {
                                        "repeatOne" -> Player.REPEAT_MODE_ONE
                                        "repeatAll" -> Player.REPEAT_MODE_ALL
                                        else -> Player.REPEAT_MODE_OFF
                                    }
                                    override fun setShuffleModeEnabled(b: Boolean) { onSetShuffle(b) }
                                    override fun getShuffleModeEnabled(): Boolean = shuffleOn
                                    override fun getAvailableCommands(): Player.Commands =
                                        super.getAvailableCommands().buildUpon()
                                            .add(Player.COMMAND_SEEK_TO_NEXT)
                                            .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                                            .build()
                                }
                                this.player = wrapped
                                useController = true
                                setUseArtwork(true)
                                setArtworkDisplayMode(PlayerView.ARTWORK_DISPLAY_MODE_FILL)
                                setShowPreviousButton(true)
                                setShowNextButton(true)
                                setShowRewindButton(true)
                                setShowFastForwardButton(true)
                                setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE or RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL)
                                setShowShuffleButton(true)
                                setShowSubtitleButton(true)
                            }
                        },
                        update = { v ->
                            v.setFullscreenButtonClickListener { onToggleFullscreen() }
                            v.controllerShowTimeoutMs = 5000
                            v.showController()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(model = video.thumbnail, contentDescription = video.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
                }
                // Thumbnail while the stream loads (video mode): the PlayerView surface is black
                // until the first frame decodes — show the cover instead of a black box.
                // Drawn only during loading (transient; taps do nothing useful yet anyway).
                if (playerLoading && quality != "audio-only (default)" && errorMsg == null && video.thumbnail != null) {
                    AsyncImage(model = video.thumbnail, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                // center state: error > loading > big play/pause button.
                // nativeOnly: NO overlay (not even loading) — anything drawn over
                // PlayerView eats its taps; the native spinner + controller handle this state.
                val videoNative = nativeOnly && player != null
                when {
                    errorMsg != null -> {
                        Column(modifier = Modifier.align(Alignment.Center).padding(horizontal = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Clear, contentDescription = "خطأ", tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("تعذّر التشغيل", style = MaterialTheme.typography.titleSmall, color = Color.White)
                            Spacer(Modifier.height(2.dp))
                            Text(errorMsg, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.85f))
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                androidx.compose.material3.Button(onClick = onRetry) { Text("إعادة المحاولة") }
                                androidx.compose.material3.OutlinedButton(onClick = onFallbackYoutube) { Text("يوتيوب", color = Color.White) }
                            }
                        }
                    }
                    playerLoading && !videoNative -> {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp, modifier = Modifier.size(56.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (bufferedPct > 0) "تحميل… $bufferedPct%" else "تحميل…",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                    !nativeOnly && (quality == "audio-only (default)" || player == null) -> {
                        IconButton(
                            onClick = onTogglePlay,
                            modifier = Modifier.align(Alignment.Center)
                                .size(76.dp)
                                .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(38.dp))
                        ) {
                            Icon(
                                if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                }
                // live badge while playing audio-only
                if (isPlaying && quality == "audio-only (default)" && errorMsg == null && !playerLoading) {
                    Surface(modifier = Modifier.align(Alignment.TopStart).padding(10.dp), shape = RoundedCornerShape(8.dp), color = Color.Black.copy(alpha = 0.65f)) {
                        Text("▶ يعمل الآن", style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                // bottom control bar: play/pause + time + volume/mute — fades in on tap, auto-hides while playing
                // bottom control bar (plain conditional: avoids AnimatedVisibility scope-overload quirk)
                // hidden while the nativeOnly experiment runs
                if (!nativeOnly && controlsVisible && errorMsg == null && !playerLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { controlsVisible = true; onTogglePlay() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            if (durationMs > 0) {
                                Text(
                                    "${formatMs(positionMs)} / ${formatMs(durationMs)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            // mute toggle
                            IconButton(
                                onClick = { controlsVisible = true; muted = !muted },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (muted || volume == 0f) Icons.AutoMirrored.Rounded.VolumeOff
                                    else if (volume < 0.5f) Icons.AutoMirrored.Rounded.VolumeDown
                                    else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = if (muted) "إلغاء الكتم" else "كتم الصوت",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            // volume slider
                            Slider(
                                value = if (muted) 0f else volume,
                                onValueChange = { volume = it; if (it > 0f) muted = false; controlsVisible = true },
                                valueRange = 0f..1f,
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }
                }
            }
            // seek + time row (when duration known) — hidden while the nativeOnly experiment runs
            if (!nativeOnly && durationMs > 0) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    Slider(
                        value = positionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..durationMs.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(formatMs(positionMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        Text(formatMs(durationMs), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            // title/meta/buttons: hidden in fullscreen (pure video); outer buttons hidden while nativeOnly runs
            if (!isFullscreen) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp)) {
                Text(text = video.title ?: "", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(text = video.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val meta = buildString {
                    video.viewCount?.takeIf { it > 0 }?.let { append(formatViews(it)) }
                    video.durationSec?.let { if (isNotEmpty()) append(" • "); append(formatDuration(it)) }
                }
                if (meta.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(12.dp))
                if (queueOn) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "▶ تعمل • $playModeLabel" + if (shuffleOn) " • عشوائي" else "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                // outer row is always visible: quality, working queue nav, queue manager, volume, PiP, close.
                // Repeat/shuffle live ONLY on the native controller (single source of truth via the wrapped player).
                // Horizontally scrollable + compact: NOTHING goes off-screen on narrow devices.
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        FilterChip(selected = false, onClick = { expanded = true }, label = { Text(shortQuality(quality)) })
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            qualities.forEach { q -> DropdownMenuItem(text = { Text(q) }, onClick = { onQuality(q); expanded = false }) }
                        }
                    }
                    // dubbed/original audio language (only when the video offers several)
                    if (audioTracks.size > 1) {
                        Spacer(Modifier.width(8.dp))
                        Box {
                            val cur = audioTracks.getOrNull(audioIdx)
                            FilterChip(selected = false, onClick = { langExpanded = true }, label = { Text(cur?.name?.takeIf { it.isNotBlank() } ?: cur?.localeTag ?: "الصوت") })
                            DropdownMenu(expanded = langExpanded, onDismissRequest = { langExpanded = false }) {
                                audioTracks.forEachIndexed { i, t ->
                                    val lbl = listOfNotNull(t.name?.takeIf { it.isNotBlank() }, t.localeTag, t.type?.let { if (it == "DUBBED") "مدبلج" else if (it == "ORIGINAL") "أصلي" else it }, t.bitrate.takeIf { it > 0 }?.let { "${it / 1000}k" }).joinToString(" • ")
                                    DropdownMenuItem(text = { Text(if (i == audioIdx) "✓ $lbl" else lbl) }, onClick = { onAudioTrack(i); langExpanded = false })
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onPrev) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "السابق", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "التالي", tint = MaterialTheme.colorScheme.primary)
                    }
                    // current queue: manage / reorder / save
                    IconButton(onClick = onQueue) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = "إدارة القائمة الحالية", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onEnterPip) {
                        Icon(Icons.Rounded.PictureInPicture, contentDescription = "صورة داخل صورة", tint = MaterialTheme.colorScheme.primary)
                    }
                    // favorite: heart the now-playing video without leaving the player
                    IconButton(onClick = onToggleFav) {
                        Icon(
                            if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isFav) "إزالة من المفضلة" else "إضافة للمفضلة",
                            tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary
                        )
                    }
                    // sleep timer: shows remaining time when armed, opens the picker otherwise
                    IconButton(onClick = onSleepClick) {
                        if (sleepLabel != null) {
                            Text(sleepLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        } else {
                            Icon(Icons.Rounded.Timer, contentDescription = "مؤقت النوم", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    // in-player volume (the native controller has no volume button)
                    IconButton(onClick = { muted = !muted }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            if (muted || volume == 0f) Icons.AutoMirrored.Rounded.VolumeOff
                            else if (volume < 0.5f) Icons.AutoMirrored.Rounded.VolumeDown
                            else Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = if (muted) "إلغاء الكتم" else "كتم الصوت",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Slider(
                        value = if (muted) 0f else volume,
                        onValueChange = { volume = it; if (it > 0f) muted = false },
                        valueRange = 0f..1f,
                        modifier = Modifier.width(90.dp)
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onFallbackYoutube) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "فتح في يوتيوب", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Rounded.Clear, contentDescription = "إغلاق المشغل", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            }
        }
    }
}