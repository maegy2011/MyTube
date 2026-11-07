package com.mohamedadel.mytube

import android.app.PictureInPictureParams
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.net.Uri
import android.util.Rational
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.PictureInPicture
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.util.RepeatModeUtil
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.activity.compose.BackHandler
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.compose.material3.Slider
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.youtube.AudioTrackItem
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.playback.PlaybackService
import androidx.room.Room
import com.mohamedadel.mytube.data.local.MyTubeDb
import com.mohamedadel.mytube.data.local.PlaylistEntity
import com.mohamedadel.mytube.data.local.PlaylistItemEntity
import com.mohamedadel.mytube.data.local.PlaylistWithCount
import com.mohamedadel.mytube.data.local.toEntity
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.ui.icons.myTubeLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import com.mohamedadel.mytube.ui.search.SearchViewModel
import com.mohamedadel.mytube.ui.theme.MyTubeTheme
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.Page

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    val pipMode = androidx.compose.runtime.mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent { MyTubeTheme { MyTubeApp(onEnterPip = { enterPip() }, onPlayerReady = { player = it }, pipMode = pipMode) } }
    }
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipMode.value = isInPictureInPictureMode
    }
    private fun enterPip() {
        val p = player
        val ratio = run {
            val w = p?.videoSize?.width ?: 0
            val h = p?.videoSize?.height ?: 0
            if (w > 0 && h > 0) {
                val r = w.toFloat() / h.toFloat()
                // clamp to PiP allowed range [0.418..2.39]
                val clamped = r.coerceIn(0.418f, 2.39f)
                Rational((clamped * 1000).toInt(), 1000)
            } else {
                Rational(16, 9)
            }
        }
        val params = PictureInPictureParams.Builder()
            .setAspectRatio(ratio)
            .setAutoEnterEnabled(true)
            .build()
        try { setPictureInPictureParams(params) } catch (_: Exception) { }
        enterPictureInPictureMode(params)
    }

    override fun onUserLeaveHint() {
        val h: Int = try { player?.videoSize?.height ?: 0 } catch (_: Exception) { 0 }
        val playing: Boolean = try { player?.isPlaying == true } catch (_: Exception) { false }
        if (playing && h > 0) enterPip()
        super.onUserLeaveHint()
    }
    override fun onDestroy() { player?.release(); player = null; super.onDestroy() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTubeApp(onEnterPip: () -> Unit, onPlayerReady: (ExoPlayer?) -> Unit = {}, pipMode: androidx.compose.runtime.MutableState<Boolean> = androidx.compose.runtime.mutableStateOf(false)) {
    var query by remember { mutableStateOf("") }
    var currentQuery by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<VideoItem>()) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(false) }
    var nextPage by remember { mutableStateOf<Page?>(null) }
    var playing by remember { mutableStateOf<VideoItem?>(null) }
    var streamData by remember { mutableStateOf<StreamData?>(null) }
    // selected dubbed/original audio track (resets per video); index into streamData.audioTracks
    var audioTrackIdx by remember { mutableStateOf(0) }
    LaunchedEffect(streamData) { audioTrackIdx = 0 }
    var qualityList by remember { mutableStateOf(listOf("audio-only (default)")) }
    var quality by remember { mutableStateOf("audio-only (default)") }
    var playerLoading by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerPositionMs by remember { mutableStateOf(0L) }
    var playerDurationMs by remember { mutableStateOf(0L) }
    var playerBufferedPct by remember { mutableStateOf(0) }
    var playerError by remember { mutableStateOf<String?>(null) }
    // queue (suggestion #3): sequential playback of the result list, started only by explicit user action
    var autoAdvance by remember { mutableStateOf(false) }
    var queueIndex by remember { mutableStateOf(-1) }
    // full player vs mini player: back collapses (keeps audio), X closes (stops audio)
    var playerExpanded by remember { mutableStateOf(false) }
    // fullscreen experiment: native ExoPlayer controls only; follows device rotation via SENSOR
    var isFullscreen by remember { mutableStateOf(false) }
    var recentSearches by remember { mutableStateOf(listOf<String>()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(1800); showSplash = false }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // explicit queue: ONLY what the user queued (play-single => one item; play-all => results copy; long-press => insert/append).
    // Search results are display-only; nothing joins the queue implicitly.
    var queue by remember { mutableStateOf(emptyList<VideoItem>()) }
    // playlists (Room) + playback modes. Modes persist in prefs; playlists persist in Room.
    val db = remember {
        Room.databaseBuilder(ctx.applicationContext, MyTubeDb::class.java, "mytube.db")
            .fallbackToDestructiveMigration().build()
    }
    val dao = remember { db.playlists() }
    val playlists by dao.observePlaylists().collectAsState(initial = emptyList())
    var showPlaylists by remember { mutableStateOf(false) }
    var openPlaylistId by remember { mutableStateOf<Long?>(null) }
    val openItems by remember(openPlaylistId) {
        openPlaylistId?.let { dao.observeItems(it) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    var showQueue by remember { mutableStateOf(false) }
    var pickFor by remember { mutableStateOf<VideoItem?>(null) }
    var optionsFor by remember { mutableStateOf<VideoItem?>(null) }
    val prefs = remember { ctx.getSharedPreferences("mytube_prefs", android.content.Context.MODE_PRIVATE) }
    // persisted recent searches (survive app restarts; shown ONLY in the search dropdown)
    LaunchedEffect(Unit) {
        try {
            val raw = prefs.getString("recent_searches", "") ?: ""
            if (raw.isNotBlank()) recentSearches = raw.split("\u001f").filter { it.isNotBlank() }.take(6)
        } catch (_: Exception) { }
    }
    // TRUE fullscreen: immersive mode (hide status+navigation, transient reveal by swipe)
    // + edge-to-edge window so the video fills the DEVICE screen, not the app content area.
    fun setFullscreen(full: Boolean) {
        isFullscreen = full
        try {
            val act = ctx as? android.app.Activity ?: return
            val win = act.window
            if (full) {
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(win, false)
                val ctl = androidx.core.view.WindowInsetsControllerCompat(win, win.decorView)
                ctl.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                ctl.systemBarsBehavior =
                    androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                act.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else {
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(win, true)
                androidx.core.view.WindowInsetsControllerCompat(win, win.decorView)
                    .show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
                act.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        } catch (_: Exception) { }
    }
    var playMode by remember { mutableStateOf(prefs.getString("play_mode", "order") ?: "order") } // order | repeatAll | repeatOne
    var shuffleOn by remember { mutableStateOf(prefs.getBoolean("shuffle", false)) }
    fun setMode(m: String) { playMode = m; try { prefs.edit().putString("play_mode", m).apply() } catch (_: Exception) { } }
    fun setShuffle(b: Boolean) { shuffleOn = b; try { prefs.edit().putBoolean("shuffle", b).apply() } catch (_: Exception) { } }
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var batteryExempt by remember { mutableStateOf(true) }
    // result of battery-exemption request: re-check right after user returns
    val batteryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { batteryExempt = ctx.isBatteryExempt() }
    fun ensurePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
    // Battery-optimization exemption: required for uninterrupted background audio. Checked on every launch/resume; requested if missing.
    fun ensureBatteryExemption() {
        val ok = ctx.isBatteryExempt()
        batteryExempt = ok
        if (!ok) {
            try {
                batteryLauncher.launch(ctx.batteryExemptionIntent())
            } catch (_: Exception) { }
        }
    }
    LaunchedEffect(Unit) { ensurePermissions(); ensureBatteryExemption() }
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) { ensurePermissions(); ensureBatteryExemption() }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        // no remove needed for composition lifetime
    }
    val scope = rememberCoroutineScope()
    val svm: SearchViewModel = viewModel()
    val suggestions by svm.suggestions.collectAsState()
    val listState = rememberLazyListState()

    // Policy: default quality is audio-only; NEVER auto-play. Playback starts only via explicit user tap on play.
    fun applyQuality(q: String, s: StreamData, p: ExoPlayer?, autoplay: Boolean = false, forceStart: Boolean = false) {
        if (p == null) return
        val wasPlaying = try { p.isPlaying } catch (_: Exception) { false }
        // user-selected dubbed/original audio track, falling back to lowest
        val selAudio = s.audioTracks.getOrNull(audioTrackIdx)?.url ?: s.audioLowestUrl
        val url = when {
            q == "audio-only (default)" -> selAudio
            q.startsWith("auto") -> autoUrl(s, ctx.estimatedDownstreamKbps())
            else -> s.videoUrlByQuality[q]
        } ?: run {
            playerError = "الجودة المطلوبة غير متاحة لهذا الفيديو — اختر جودة أخرى أو شاهده في يوتيوب."
            playerLoading = false
            return
        }
        val muxedUrls = s.videoUrlByQuality.filter { s.muxedQualities.contains(it.key) }.values.toSet()
        val isMuxed = url in muxedUrls
        val meta = MediaMetadata.Builder()
            .setTitle(playing?.title ?: s.title)
            .setArtist(playing?.channel)
            .setAlbumTitle("MyTube")
            .setDescription("© MyTube")
            .setArtworkUri(playing?.thumbnail?.let { Uri.parse(it) })
            .build()
        // side-loaded captions: the native CC button lists them, user switches language there
        val subConfigs = s.subtitles.mapNotNull { cap ->
            try {
                MediaItem.SubtitleConfiguration.Builder(Uri.parse(cap.url))
                    .setMimeType(cap.mime).setLanguage(cap.langTag).setLabel(cap.label).build()
            } catch (_: Exception) { null }
        }
        val mediaItem = MediaItem.Builder().setUri(Uri.parse(url)).setMediaMetadata(meta).setSubtitleConfigurations(subConfigs).build()
        // Prepare paused; resume only if explicitly requested AND was already playing (quality switch / background switch), or forced (retry).
        val resume = (autoplay && wasPlaying) || forceStart
        p.playWhenReady = false
        if (q == "audio-only (default)" || isMuxed || selAudio == null) {
            p.setMediaItem(mediaItem)
            p.prepare()
        } else {
            val httpFactory = DefaultHttpDataSource.Factory()
            val dataSourceFactory = DefaultDataSource.Factory(ctx, httpFactory)
            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(selAudio)))
            val merged = MergingMediaSource(videoSource, audioSource)
            p.setMediaSource(merged)
            p.prepare()
        }
        p.playWhenReady = resume
    }

    // Auto-switch to audio-only (lowest quality) when app goes to background / screen locked
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val activity = ctx as? android.app.Activity
                if (activity?.isInPictureInPictureMode != true && playing != null && quality != "audio-only (default)") {
                    quality = "audio-only (default)"
                    // preserve play state: autoplay=true resumes only if it was playing
                    streamData?.let { applyQuality("audio-only (default)", it, player, autoplay = true) }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Open video WITHOUT auto-play: default quality audio-only, prepared paused. User taps play explicitly.
    // queue=true means this playback belongs to the user-started queue ("play all" / next / prev).
    // expand=false keeps the current collapsed/expanded state (queue advancing while browsing).
    fun playVideo(v: VideoItem, inQueue: Boolean = false, expand: Boolean = true) {
        playing = v
        if (expand) playerExpanded = true
        if (!inQueue) {
            // single tap: this video alone is the queue (no implicit follow-up)
            queue = listOf(v)
            autoAdvance = false
        } else {
            autoAdvance = true
        }
        queueIndex = queue.indexOfFirst { it.id == v.id || (it.url != null && it.url == v.url) }
        playerLoading = true
        playerError = null
        playerPositionMs = 0L; playerDurationMs = 0L; playerBufferedPct = 0
        try { player?.playWhenReady = false; player?.pause() } catch (_: Exception) { }
        scope.launch {
            val s = YouTubeRepo.stream(v.url ?: return@launch)
            // total extraction failure used to leave the spinner forever with NO error (silent death).
            // Detect it here and report a smart message instead.
            if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
                playerLoading = false
                playerError = "تعذّر جلب مصادر هذا الفيديو — قد يكون محذوفًا أو مقيّدًا أو محميًا. جرّب مجددًا أو شاهده في يوتيوب."
                return@launch
            }
            streamData = s
            // label the auto entry with the speed-based target so the user sees what auto will pick
            val kbps = ctx.estimatedDownstreamKbps()
            val autoKey = autoUrl(s, kbps)?.let { u ->
                val h = s.videoUrlByQuality.entries.firstOrNull { it.value == u }?.key
                    ?.let { qualityHeight(it) }
                if (h != null && h != Int.MAX_VALUE) "auto \u2022 ${h}p" else "auto"
            } ?: "auto"
            val quals = mutableListOf("audio-only (default)", autoKey)
            val sorted = s.videoUrlByQuality.keys.sortedByDescending { qualityHeight(it) ?: 0 }
            quals.addAll(sorted)
            qualityList = quals
            quality = "audio-only (default)"
            applyQuality("audio-only (default)", s, player, autoplay = false)
        }
    }

    // Queue navigation (suggestion #3): next/previous within the result list.
    fun playNextInQueue() {
        val i = queueIndex
        val next = if (i >= 0) queue.getOrNull(i + 1) else null
        // expand=playerExpanded: advancing from notification/lock while browsing must not yank the full player open
        if (next != null) playVideo(next, inQueue = true, expand = playerExpanded) else autoAdvance = false
    }
    fun playPrevInQueue() {
        val prev = if (queueIndex > 0) queue.getOrNull(queueIndex - 1) else null
        if (prev != null) playVideo(prev, inQueue = true, expand = playerExpanded)
    }
    // playback modes: random pick (shuffle) + wrap-around (repeat all)
    fun playRandomInQueue() {
        if (queue.size < 2) { if (queue.isNotEmpty()) playVideo(queue[0], inQueue = true, expand = playerExpanded); return }
        var n = queueIndex
        while (n == queueIndex) n = (0 until queue.size).random()
        playVideo(queue[n], inQueue = true, expand = playerExpanded)
    }
    fun playFirstInQueue() {
        if (queue.isNotEmpty()) playVideo(queue[0], inQueue = true, expand = playerExpanded) else autoAdvance = false
    }
    // explicit queue building (long-press menu)
    fun playNowAlone(v: VideoItem) { playVideo(v, inQueue = false, expand = true) }
    fun enqueueNext(v: VideoItem) {
        val q = queue.toMutableList()
        val at = if (queueIndex >= 0) queueIndex + 1 else q.size
        q.add(at.coerceIn(0, q.size), v)
        queue = q
        if (playing == null) { queueIndex = 0; playVideo(v, inQueue = true, expand = true) }
    }
    fun enqueueLast(v: VideoItem) {
        queue = queue + v
        if (playing == null) playVideo(v, inQueue = true, expand = true)
    }
    fun modeLabel(m: String): String = when (m) {
        "repeatAll" -> "تكرار الكل"
        "repeatOne" -> "تكرار واحد"
        else -> "بالترتيب"
    }
    fun cycleMode() = setMode(when (playMode) { "order" -> "repeatAll"; "repeatAll" -> "repeatOne"; else -> "order" })

    // Connect to PlaybackService for notification/lock-screen controls
    LaunchedEffect(Unit) {
        try {
            ctx.startService(Intent(ctx, PlaybackService::class.java))
        } catch (_: Exception) { }
        var p: ExoPlayer? = null
        while (p == null) {
            p = PlaybackService.player
            if (p == null) kotlinx.coroutines.delay(100)
        }
        player = p
        onPlayerReady(p)
        // route notification / lock-screen prev+next buttons to the in-app queue
        PlaybackService.onCustomAction = { action ->
            try {
                when (action) {
                    PlaybackService.ACTION_NEXT -> playNextInQueue()
                    PlaybackService.ACTION_PREV -> playPrevInQueue()
                }
            } catch (_: Exception) { }
        }
    }

    val trending = remember { listOf("music", "tech reviews", "gaming", "news", "podcasts") }

    // auto-load more when scrolled to bottom (guarded: single-flight + null page + error-safe)
    val shouldLoadMore by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            total > 0 && last >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore, currentQuery) {
        if (shouldLoadMore && hasMore && !loadingMore && !loading && currentQuery.isNotBlank()) {
            val q = currentQuery
            val page = nextPage ?: return@LaunchedEffect
            loadingMore = true
            try {
                val res = YouTubeRepo.search(q, page)
                // drop duplicates already in list (same url/id) to avoid key crash
                val seen = results.mapNotNull { it.url ?: it.id }.toSet()
                val fresh = res.items.filter { (it.url ?: it.id ?: it.title) !in seen }
                results = results + fresh
                nextPage = res.nextPage
                hasMore = res.hasNext && res.nextPage != null
            } catch (_: Exception) {
                hasMore = false
            } finally {
                loadingMore = false
            }
        }
    }

    // player listener for buffering state + errors
    LaunchedEffect(player) {
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                playerLoading = state == Player.STATE_BUFFERING
                isPlaying = player?.isPlaying == true
                if (state == Player.STATE_READY) playerError = null
                // queue end: repeat one -> replay; shuffle -> random; repeat all -> wrap; order -> next (stops at end)
                if (state == Player.STATE_ENDED && autoAdvance) {
                    when {
                        playMode == "repeatOne" -> try { player?.seekTo(0L); player?.play() } catch (_: Exception) { }
                        shuffleOn -> playRandomInQueue()
                        playMode == "repeatAll" -> {
                            val nxt = if (queueIndex >= 0) queue.getOrNull(queueIndex + 1) else null
                            if (nxt != null) playVideo(nxt, inQueue = true) else playFirstInQueue()
                        }
                        else -> playNextInQueue()
                    }
                }
            }
            override fun onIsPlayingChanged(playingFlag: Boolean) {
                isPlaying = playingFlag
            }
            override fun onPlayerError(error: PlaybackException) {
                playerError = smartError(error)
                playerLoading = false
                isPlaying = false
            }
        })
    }
    // position / duration / buffer polling for the upgraded player UI
    LaunchedEffect(player, playing) {
        while (true) {
            val p = player
            if (p != null) {
                try {
                    playerPositionMs = p.currentPosition.coerceAtLeast(0L)
                    playerDurationMs = p.duration.takeIf { it > 0 } ?: 0L
                    playerBufferedPct = p.bufferedPercentage
                } catch (_: Exception) { }
            } else {
                playerPositionMs = 0L; playerDurationMs = 0L; playerBufferedPct = 0
            }
            kotlinx.coroutines.delay(500)
        }
    }

    // saved scroll position per query: firstVisibleIndex + offset, restored when the same query is searched again
    val savedScroll = remember { mutableMapOf<String, Pair<Int, Int>>() }
    fun doSearch(q: String) {
        if (q.isBlank()) return
        currentQuery = q
        loading = true
        showSuggestions = false
        scope.launch {
            val page = YouTubeRepo.search(q, null)
            results = page.items
            nextPage = page.nextPage
            hasMore = page.hasNext
            loading = false
            // restore saved scroll position for repeated queries
            savedScroll[q]?.let { (idx, off) ->
                try { listState.scrollToItem(idx, off) } catch (_: Exception) { }
            }
            if (!recentSearches.contains(q)) {
                recentSearches = (listOf(q) + recentSearches).take(6)
                try { prefs.edit().putString("recent_searches", recentSearches.joinToString("\u001f")).apply() } catch (_: Exception) { }
            }
        }
    }

    // Explicit close: stops audio and clears the player (X button only).
    fun closePlayer() {
        // stop + drop the loaded item: otherwise the MediaSession notification keeps
        // showing the last video's data after close (stale notification bug).
        try { player?.stop() } catch (_: Exception) { }
        try { player?.clearMediaItems() } catch (_: Exception) { }
        playing = null
        streamData = null
        playerExpanded = false
        autoAdvance = false
        queueIndex = -1
    }
    // Smart back: exit fullscreen first; collapse full player (audio keeps playing, mini player appears); results cleared next; playlists navigated. Never stops audio.
    fun smartBack(): Boolean {
        if (isFullscreen) {
            setFullscreen(false)
            return true
        }
        if (playerExpanded) {
            playerExpanded = false
            return true
        }
        if (showPlaylists) {
            if (openPlaylistId != null) openPlaylistId = null else showPlaylists = false
            return true
        }
        if (results.isNotEmpty()) {
            try {
                savedScroll[currentQuery] = listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            } catch (_: Exception) { }
            results = emptyList()
            query = ""
            return true
        }
        return false
    }
    BackHandler(enabled = isFullscreen || playerExpanded || playing != null || showPlaylists || results.isNotEmpty()) { smartBack() }

    if (showSplash) {
        // Full-screen solid red splash (same red as the system splash background).
        // Legal lines + version live at the very bottom in small type.
        Box(Modifier.fillMaxSize().background(Color(0xFFD5392E)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.ui.res.painterResource(id = com.mohamedadel.mytube.R.drawable.ic_launcher_foreground)?.let { p ->
                    Icon(painter = p, contentDescription = "MyTube", tint = Color.White, modifier = Modifier.size(96.dp))
                    Spacer(Modifier.height(12.dp))
                }
                Text("MyTube", style = MaterialTheme.typography.displayMedium, color = Color.White)
            }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("v0.1.9", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(4.dp))
                Text("© MyTube. All rights reserved.", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.height(2.dp))
                Text("Not affiliated with YouTube or Google.", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
        return
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("MyTube", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                NavigationDrawerItem(label = { Text("الرئيسية") }, selected = !showPlaylists, onClick = { showPlaylists = false; openPlaylistId = null; scope.launch { drawerState.close() } }, icon = { Icon(Icons.Rounded.Home, null) })
                NavigationDrawerItem(label = { Text(if (playlists.isNotEmpty()) "قوائم التشغيل (${playlists.size})" else "قوائم التشغيل") }, selected = showPlaylists, onClick = { showPlaylists = true; scope.launch { drawerState.close() } }, icon = { Icon(Icons.Rounded.QueueMusic, null) })
            }
        }
    ) {
    val isInPip = pipMode.value
    // No top app bar: logo + nav live INSIDE the search row (saves a full 64dp bar).
    Scaffold(
        topBar = { },
        bottomBar = {
            // Mini-player: visible whenever audio plays while the full player is collapsed (home, results, playlists). Never shown together with the full player.
            if (!isInPip && !isFullscreen && playing != null && !playerExpanded) {
                MiniPlayer(
                    video = playing!!,
                    isPlaying = isPlaying,
                    positionMs = playerPositionMs,
                    durationMs = playerDurationMs,
                    onToggle = {
                        playerError = null
                        if (player?.isPlaying == true) player?.pause() else player?.play()
                    },
                    onClose = { closePlayer() },
                    onTap = {
                        // reopen the full player + jump the queue to the currently playing row
                        playerExpanded = true
                        scope.launch {
                            val cur = playing ?: return@launch
                            val idx = queue.indexOfFirst { it.id == cur.id || (it.url != null && it.url == cur.url) }
                            if (idx >= 0) try { listState.scrollToItem(idx) } catch (_: Exception) { }
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
        if (isInPip) {
            Box(Modifier.padding(pad).fillMaxSize()) {
                val cur = playing
                if (cur != null && quality != "audio-only (default)" && player != null) {
                    AndroidView(
                        factory = { c ->
                            PlayerView(c).apply {
                                this.player = player
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                useController = false
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(model = cur?.thumbnail, contentDescription = cur?.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
        } else {
        Column(Modifier.padding(pad).fillMaxSize()) {
            // Battery-exemption banner: visible only while the exemption is missing (never in fullscreen)
            if (!isFullscreen && !batteryExempt) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "السماح بتشغيل الصوت في الخلفية بلا انقطاع",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.Button(onClick = { ensureBatteryExemption() }) {
                            Text("سماح")
                        }
                    }
                }
            }
            // Search + suggestions are hidden in fullscreen: they stole vertical space and
            // squeezed the video to "app size" instead of true fullscreen.
            if (!isFullscreen) {
            // Unified header row: nav + logo + search (the old 64dp top bar is gone).
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (playerExpanded || playing != null || showPlaylists || results.isNotEmpty()) {
                    IconButton(onClick = { smartBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                } else {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Rounded.Menu, contentDescription = "القائمة")
                    }
                }
                Icon(myTubeLogo(), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(30.dp))
                Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; svm.onQueryChange(it); showSuggestions = true },
                placeholder = { Text("ابحث عن فيديو أو قناة…") },
                leadingIcon = {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!loading && query.isNotEmpty()) {
                            IconButton(onClick = { query = ""; svm.onQueryChange(""); showSuggestions = false }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "مسح البحث")
                            }
                        }
                        IconButton(onClick = { doSearch(query) }) {
                            Icon(Icons.Rounded.Search, contentDescription = "بحث")
                        }
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(textDirection = TextDirection.Content),
                shape = RoundedCornerShape(28.dp),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { doSearch(query) }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f)
            )
            }

            // Dropdown = recent searches (history icon) + live network suggestions, merged and deduped.
            // Blank query -> recents only. Typing -> matching recents first, then network.
            val dropRecents = if (query.isBlank()) recentSearches else recentSearches.filter { r -> r.contains(query, ignoreCase = true) && !suggestions.contains(r) }
            AnimatedVisibility(visible = showSuggestions && (suggestions.isNotEmpty() || dropRecents.isNotEmpty()), enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Column {
                        if (query.isBlank() && dropRecents.isNotEmpty()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("عمليات بحث سابقة", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                androidx.compose.material3.TextButton(onClick = {
                                    recentSearches = emptyList()
                                    try { prefs.edit().remove("recent_searches").apply() } catch (_: Exception) { }
                                }) { Text("مسح الكل") }
                            }
                        }
                        dropRecents.forEach { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    query = s; svm.onQueryChange(s); doSearch(s)
                                }.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(s, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        suggestions.take(8).forEach { s ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    query = s; svm.onQueryChange(s); doSearch(s)
                                }.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(s, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
            }

            val cur = playing
            if (cur != null && (playerExpanded || isFullscreen)) {
                PlayerArea(
                    video = cur,
                    player = player,
                    quality = quality,
                    qualities = qualityList,
                    isPlaying = isPlaying,
                    playerLoading = playerLoading,
                    positionMs = playerPositionMs,
                    durationMs = playerDurationMs,
                    bufferedPct = playerBufferedPct,
                    errorMsg = playerError,
                    onQuality = { q ->
                        quality = q
                        // user explicitly picked quality: resume only if it was already playing
                        streamData?.let { applyQuality(q, it, player, autoplay = true) }
                    },
                    onTogglePlay = {
                        playerError = null
                        if (player?.isPlaying == true) player?.pause() else player?.play()
                    },
                    onSeek = { pos -> try { player?.seekTo(pos) } catch (_: Exception) { } },
                    onRetry = {
                        playerError = null
                        streamData?.let { applyQuality(quality, it, player, forceStart = true) }
                    },
                    queueOn = autoAdvance,
                    playMode = playMode,
                    playModeLabel = modeLabel(playMode),
                    shuffleOn = shuffleOn,
                    onCycleMode = { cycleMode() },
                    onToggleShuffle = { setShuffle(!shuffleOn) },
                    onQueue = { showQueue = true },
                    onClose = { closePlayer() },
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = { setFullscreen(!isFullscreen) },
                    onSetMode = { setMode(it) },
                    onSetShuffle = { setShuffle(it) },
                    audioTracks = streamData?.audioTracks ?: emptyList(),
                    audioIdx = audioTrackIdx,
                    onAudioTrack = { i ->
                        audioTrackIdx = i
                        streamData?.let { sd -> applyQuality(quality, sd, player, autoplay = true) }
                    },
                    onNext = { playNextInQueue() },
                    onPrev = { playPrevInQueue() },
                    onFallbackYoutube = { ctx.openInYoutubeCtx(cur) },
                    onEnterPip = onEnterPip
                )
            }

            if (!isFullscreen) {
            when {
                showPlaylists -> {
                    PlaylistsScreen(
                        playlists = playlists,
                        openId = openPlaylistId,
                        items = openItems,
                        onOpen = { openPlaylistId = it },
                        onCloseItems = { openPlaylistId = null },
                        onPlayAll = { pid ->
                            scope.launch {
                                val rows = dao.itemsOnce(pid)
                                if (rows.isNotEmpty()) {
                                    showPlaylists = false
                                    openPlaylistId = null
                                    val list = rows.map { it.toVideoItem() }
                                    results = list
                                    queue = list
                                    currentQuery = ""
                                    playVideo(list[0], inQueue = true)
                                }
                            }
                        },
                        onPlayFrom = { idx ->
                            val list = openItems.map { it.toVideoItem() }
                            if (idx in list.indices) {
                                showPlaylists = false
                                results = list
                                queue = list
                                currentQuery = ""
                                playVideo(list[idx], inQueue = true)
                            }
                        },
                        onCreate = { name ->
                            scope.launch { dao.insertPlaylist(PlaylistEntity(name = name)) }
                        },
                        onRename = { pid, name -> scope.launch { dao.rename(pid, name) } },
                        onDelete = { pid -> scope.launch { dao.deletePlaylist(pid); if (openPlaylistId == pid) openPlaylistId = null } },
                        onMoveItem = { pid, iid, dir ->
                            scope.launch {
                                val rows = dao.itemsOnce(pid).toMutableList()
                                val i = rows.indexOfFirst { it.id == iid }
                                val j = i + dir
                                if (i >= 0 && j in rows.indices) {
                                    val tmp = rows[i]; rows[i] = rows[j]; rows[j] = tmp
                                    dao.updateItems(rows.mapIndexed { k, r -> r.copy(position = k) })
                                }
                            }
                        },
                        onDeleteItem = { pid, iid -> scope.launch { dao.deleteItem(pid, iid) } },
                        onClearItems = { pid -> scope.launch { dao.clearPlaylist(pid) } },
                        onBack = { showPlaylists = false; openPlaylistId = null }
                    )
                }
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                results.isEmpty() && query.isBlank() -> {
                    HomeEmptyState(trending = trending, onSearch = { q -> query = q; doSearch(q) })
                }
                results.isEmpty() && !loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("لا توجد نتائج", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Text("جرّب كلمة مختلفة أو تحقق من الاتصال بالإنترنت", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // queue entry point (suggestion #3): play the whole result list sequentially, audio-only, no autoplay until tapped
                        item {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("وجدنا ${results.size} نتيجة", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.weight(1f))
                                androidx.compose.material3.Button(onClick = {
                                    // explicit choice: the whole result list becomes the queue
                                    if (results.isNotEmpty()) { queue = results; playVideo(results[0], inQueue = true) }
                                }) { Text(if (autoAdvance) "القائمة تعمل (${queue.size})" else "تشغيل الكل") }
                            }
                        }
                        itemsIndexed(items = results, key = { idx, v -> "$idx|${v.id ?: v.url ?: v.title ?: "n/a"}" }) { _, v ->
                            SearchRow(
                                v = v,
                                isCurrent = playing?.id == v.id || playing?.url == v.url,
                                isPlaying = isPlaying && (playing?.id == v.id || playing?.url == v.url),
                                onClick = { playVideo(v) },
                                onLongClick = { optionsFor = it },
                                onAdd = { pickFor = v }
                            )
                        }
                        if (loadingMore) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                                }
                            }
                        } else if (hasMore) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    androidx.compose.material3.Button(onClick = {
                                        if (loadingMore) return@Button
                                        val pg = nextPage ?: return@Button
                                        loadingMore = true
                                        scope.launch {
                                            try {
                                                val page = YouTubeRepo.search(currentQuery, pg)
                                                val seen = results.mapNotNull { it.url ?: it.id }.toSet()
                                                val fresh = page.items.filter { (it.url ?: it.id ?: it.title) !in seen }
                                                results = results + fresh
                                                nextPage = page.nextPage
                                                hasMore = page.hasNext && page.nextPage != null
                                            } catch (_: Exception) {
                                                hasMore = false
                                            } finally {
                                                loadingMore = false
                                            }
                                        }
                                    }) { Text("عرض المزيد") }
                                }
                            }
                        }
                    }
                }
            }
            }
            // queue manager dialog: reorder / remove items of the current list + save it as a playlist
            if (showQueue) {
                QueueDialog(
                    items = queue,
                    onMove = { idx, dir ->
                        val j = idx + dir
                        if (idx in queue.indices && j in queue.indices) {
                            val m = queue.toMutableList()
                            val t = m[idx]; m[idx] = m[j]; m[j] = t
                            queue = m
                            queueIndex = m.indexOfFirst { it.id == playing?.id || (it.url != null && it.url == playing?.url) }
                        }
                    },
                    onRemove = { idx ->
                        if (idx in queue.indices) {
                            queue = queue.toMutableList().also { it.removeAt(idx) }
                        }
                    },
                    onPlayFrom = { idx -> if (idx in queue.indices) playVideo(queue[idx], inQueue = true) },
                    onSave = { name ->
                        scope.launch {
                            val pid = dao.insertPlaylist(PlaylistEntity(name = name))
                            dao.insertItems(queue.mapIndexed { i, v -> v.toEntity(pid, i) })
                        }
                    },
                    onDismiss = { showQueue = false }
                )
            }
            // add-to-playlist picker for a single video
            pickFor?.let { v ->
                PlaylistPickerDialog(
                    video = v,
                    playlists = playlists,
                    onCreateAndAdd = { name ->
                        scope.launch {
                            val pid = dao.insertPlaylist(PlaylistEntity(name = name))
                            dao.insertItems(listOf(v.toEntity(pid, 0)))
                            pickFor = null
                        }
                    },
                    onAddTo = { pid ->
                        scope.launch {
                            val pos = dao.itemCount(pid)
                            dao.insertItems(listOf(v.toEntity(pid, pos)))
                            pickFor = null
                        }
                    },
                    onDismiss = { pickFor = null }
                )
            }
            // long-press video options: play now / play next / queue end / add to playlist
            optionsFor?.let { v ->
                VideoOptionsDialog(
                    video = v,
                    onPlayNow = { playNowAlone(v); optionsFor = null },
                    onPlayNext = { enqueueNext(v); optionsFor = null },
                    onQueueEnd = { enqueueLast(v); optionsFor = null },
                    onAddToPlaylist = { pickFor = v; optionsFor = null },
                    onDismiss = { optionsFor = null }
                )
            }
        }
        }
    }
    }
}

// Estimated downstream bandwidth in Kbps from the active network (0 = unknown).
fun Context.estimatedDownstreamKbps(): Int {
    return try {
        val cm = getSystemService(android.net.ConnectivityManager::class.java) ?: return 0
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return 0
        caps.linkDownstreamBandwidthKbps
    } catch (_: Exception) { 0 }
}

// Auto quality mapped to the user's actual connection speed:
// <1Mbps→144p, <2.5Mbps→360p, <5Mbps→480p, <10Mbps→720p, <20Mbps→1080p, else highest. Unknown speed → old mid default (720/480).
fun autoUrl(s: StreamData, kbps: Int = 0): String? {
    val heights = s.videoUrlByQuality.keys.mapNotNull { k ->
        qualityHeight(k)?.let { h -> h to k }
    }
    if (heights.isEmpty()) return s.videoUrlByQuality.values.firstOrNull()
    if (kbps <= 0) {
        val target = heights.filter { it.first == 720 }.firstOrNull()
            ?: heights.filter { it.first == 480 }.firstOrNull()
            ?: heights.maxByOrNull { it.first }
        return target?.let { s.videoUrlByQuality[it.second] }
    }
    val targetH = when {
        kbps < 1000 -> 144
        kbps < 2500 -> 360
        kbps < 5000 -> 480
        kbps < 10000 -> 720
        kbps < 20000 -> 1080
        else -> Int.MAX_VALUE
    }
    val atOrBelow = heights.filter { it.first <= targetH }.maxByOrNull { it.first }
    val best = atOrBelow ?: heights.minByOrNull { it.first }
    return best?.let { s.videoUrlByQuality[it.second] }
}

fun qualityHeight(label: String): Int? {
    val l = label.lowercase()
    if (l.contains("2160") || l.contains("4k")) return 2160
    if (l.contains("1440")) return 1440
    if (l.contains("1080")) return 1080
    if (l.contains("720")) return 720
    if (l.contains("480")) return 480
    if (l.contains("360")) return 360
    if (l.contains("240")) return 240
    if (l.contains("144")) return 144
    return l.filter { it.isDigit() }.toIntOrNull()
}

@Composable
fun HomeEmptyState(trending: List<String>, onSearch: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("رائج الآن", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(trending) { q ->
                    FilterChip(selected = false, onClick = { onSearch(q) }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp)) }, label = { Text(q) })
                }
            }
        }
        item {
            Spacer(Modifier.height(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(myTubeLogo(), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(12.dp))
                Text("اكتشف الفيديوهات", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("ابحث أعلاه عن أي فيديو أو قناة على يوتيوب", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ============ Playlists (Room) UI ============

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistWithCount>,
    openId: Long?,
    items: List<PlaylistItemEntity>,
    onOpen: (Long) -> Unit,
    onCloseItems: () -> Unit,
    onPlayAll: (Long) -> Unit,
    onPlayFrom: (Int) -> Unit,
    onCreate: (String) -> Unit,
    onRename: (Long, String) -> Unit,
    onDelete: (Long) -> Unit,
    onMoveItem: (Long, Long, Int) -> Unit,
    onDeleteItem: (Long, Long) -> Unit,
    onClearItems: (Long) -> Unit,
    onBack: () -> Unit
) {
    var nameDraft by remember { mutableStateOf("") }
    var showCreate by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<PlaylistWithCount?>(null) }
    var deleteTarget by remember { mutableStateOf<PlaylistWithCount?>(null) }
    val openName = playlists.firstOrNull { it.id == openId }?.name ?: ""
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (openId != null) onCloseItems() else onBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
                Text(
                    if (openId != null) "بنود: $openName" else "قوائم التشغيل",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.weight(1f))
                if (openId == null) {
                    IconButton(onClick = { showCreate = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = "قائمة جديدة", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
        if (openId == null) {
            if (playlists.isEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("لا توجد قوائم بعد", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("احفظ نتائج البحث أو أي فيديو في قائمة تشغيل", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(playlists, key = { it.id }) { p ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f).clickable { onOpen(p.id) }) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("${p.count} عنصر", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onPlayAll(p.id) }) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = "تشغيل القائمة", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { renameTarget = p }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "إعادة تسمية", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { deleteTarget = p }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "حذف القائمة", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        } else {
            if (items.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Text("القائمة فارغة — أضف فيديوهات من نتائج البحث", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            itemsIndexed(items, key = { _, r -> r.id }) { idx, r ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(model = r.thumbnail, contentDescription = r.title, modifier = Modifier.size(72.dp, 42.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f).clickable { onPlayFrom(idx) }) {
                            Text(r.title ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(r.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        IconButton(onClick = { openId?.let { onMoveItem(it, r.id, -1) } }, enabled = idx > 0) {
                            Icon(Icons.Rounded.ArrowUpward, contentDescription = "تحريك لأعلى", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { openId?.let { onMoveItem(it, r.id, 1) } }, enabled = idx < items.size - 1) {
                            Icon(Icons.Rounded.ArrowDownward, contentDescription = "تحريك لأسفل", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { openId?.let { onDeleteItem(it, r.id) } }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "حذف البند", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            if (items.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("${items.size} عنصر", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        androidx.compose.material3.TextButton(onClick = { openId?.let { onPlayAll(it) } }) { Text("تشغيل الكل") }
                        androidx.compose.material3.TextButton(onClick = { openId?.let { onClearItems(it) } }) { Text("تفريغ") }
                    }
                }
            }
        }
    }
    // create dialog
    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("قائمة تشغيل جديدة") },
            text = {
                OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text("اسم القائمة") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val n = nameDraft.trim()
                    if (n.isNotEmpty()) { onCreate(n); nameDraft = ""; showCreate = false }
                }) { Text("إنشاء") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { showCreate = false }) { Text("إلغاء") } }
        )
    }
    // rename dialog
    renameTarget?.let { p ->
        var draft by remember(p.id) { mutableStateOf(p.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("إعادة تسمية القائمة") },
            text = {
                OutlinedTextField(value = draft, onValueChange = { draft = it }, singleLine = true, modifier = Modifier.fillMaxWidth())
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val n = draft.trim()
                    if (n.isNotEmpty()) onRename(p.id, n)
                    renameTarget = null
                }) { Text("حفظ") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { renameTarget = null }) { Text("إلغاء") } }
        )
    }
    // delete confirm
    deleteTarget?.let { p ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("حذف القائمة؟") },
            text = { Text("سيتم حذف \"${p.name}\" نهائيًا مع كل بنودها.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { onDelete(p.id); deleteTarget = null }) { Text("حذف") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { deleteTarget = null }) { Text("إلغاء") } }
        )
    }
}

// Current-queue manager: reorder / remove / play-from / save-as-playlist.
@Composable
fun QueueDialog(
    items: List<VideoItem>,
    onMove: (Int, Int) -> Unit,
    onRemove: (Int) -> Unit,
    onPlayFrom: (Int) -> Unit,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var nameDraft by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("القائمة الحالية (${items.size})") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (items.isEmpty()) {
                    Text("لا توجد عناصر في القائمة الحالية", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        itemsIndexed(items, key = { idx, v -> "$idx|${v.id ?: v.url ?: v.title ?: "n/a"}" }) { idx, v ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("${idx + 1}.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))
                                Text(v.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).clickable { onPlayFrom(idx) })
                                IconButton(onClick = { onMove(idx, -1) }, enabled = idx > 0, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "لأعلى", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onMove(idx, 1) }, enabled = idx < items.size - 1, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.ArrowDownward, contentDescription = "لأسفل", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onRemove(idx) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text("احفظ القائمة باسم…") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                val n = nameDraft.trim()
                if (n.isNotEmpty() && items.isNotEmpty()) { onSave(n); nameDraft = "" }
            }) { Text("حفظ القائمة") }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

// Long-press menu for a video row.
@Composable
fun VideoOptionsDialog(
    video: VideoItem,
    onPlayNow: () -> Unit,
    onPlayNext: () -> Unit,
    onQueueEnd: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("خيارات الفيديو", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(video.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                val items = listOf(
                    "تشغيل الآن" to onPlayNow,
                    "تشغيل التالي" to onPlayNext,
                    "إضافة لآخر القائمة الحالية" to onQueueEnd,
                    "إضافة إلى قائمة تشغيل…" to onAddToPlaylist
                )
                items.forEach { (label, action) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = action).padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val icon = when (label) {
                            "تشغيل الآن" -> Icons.Rounded.PlayArrow
                            "تشغيل التالي" -> Icons.Rounded.SkipNext
                            "إضافة لآخر القائمة الحالية" -> Icons.Rounded.QueueMusic
                            else -> Icons.Rounded.PlaylistAdd
                        }
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}

// Pick a playlist for one video (or create a new one on the spot).
@Composable
fun PlaylistPickerDialog(
    video: VideoItem,
    playlists: List<PlaylistWithCount>,
    onCreateAndAdd: (String) -> Unit,
    onAddTo: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var nameDraft by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة إلى قائمة تشغيل", maxLines = 1, overflow = TextOverflow.Ellipsis) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(video.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(8.dp))
                if (playlists.isEmpty()) {
                    Text("لا توجد قوائم بعد — أنشئ واحدة بالاسم أدناه", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(playlists, key = { it.id }) { p ->
                            Row(modifier = Modifier.fillMaxWidth().clickable { onAddTo(p.id) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.PlaylistPlay, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(p.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${p.count}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = nameDraft, onValueChange = { nameDraft = it }, placeholder = { Text("أو قائمة جديدة باسم…") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = {
                val n = nameDraft.trim()
                if (n.isNotEmpty()) { onCreateAndAdd(n); nameDraft = "" }
            }) { Text("إنشاء وإضافة") }
        },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
fun EqualizerBars(playing: Boolean) {
    // animated sound bars while playing, frozen mid-height while paused
    if (!playing) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(3) {
                Box(modifier = Modifier.width(3.dp).height(8.dp).background(Color.White, RoundedCornerShape(1.dp)))
            }
        }
        return
    }
    val t = rememberInfiniteTransition(label = "eq")
    val h1 by t.animateFloat(4f, 16f, infiniteRepeatable(tween(380), RepeatMode.Reverse), label = "eq1")
    val h2 by t.animateFloat(14f, 5f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "eq2")
    val h3 by t.animateFloat(6f, 13f, infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "eq3")
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(h1, h2, h3).forEach { h ->
            Box(modifier = Modifier.width(3.dp).height(h.dp).background(Color.White, RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
fun MiniPlayer(
    video: VideoItem,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onToggle: () -> Unit,
    onTap: () -> Unit,
    onClose: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onTap).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(model = video.thumbnail, contentDescription = video.title, modifier = Modifier.size(48.dp, 32.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = video.title ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = video.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                EqualizerBars(playing = isPlaying)
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onToggle) {
                    Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Clear, contentDescription = "إغلاق المشغل", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            if (durationMs > 0) {
                LinearProgressIndicator(
                    progress = { (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SearchRow(v: VideoItem, isCurrent: Boolean, isPlaying: Boolean, onClick: () -> Unit, onAdd: (VideoItem) -> Unit = {}, onLongClick: (VideoItem) -> Unit = {}) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = { onLongClick(v) }),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(132.dp, 74.dp)) {
                AsyncImage(model = v.thumbnail, contentDescription = v.title, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                // current row: animated equalizer (dances while playing, frozen while paused); others: plain play icon
                Box(
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCurrent) {
                        EqualizerBars(playing = isPlaying)
                    } else {
                        Icon(
                            Icons.Rounded.PlayArrow,
                            contentDescription = "تشغيل",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                v.durationSec?.let { dur ->
                    Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp), shape = RoundedCornerShape(6.dp), color = Color.Black.copy(alpha = 0.75f)) {
                        Text(text = formatDuration(dur), style = MaterialTheme.typography.labelSmall, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = v.title ?: "", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(text = v.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (v.viewCount != null && v.viewCount > 0) {
                    Spacer(Modifier.height(2.dp))
                    Text(text = formatViews(v.viewCount), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = onClick) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = "تشغيل الفيديو", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { onAdd(v) }) {
                    Icon(Icons.Rounded.PlaylistAdd, contentDescription = "إضافة إلى قائمة تشغيل", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

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
    onEnterPip: () -> Unit
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

// Short chip labels: the raw quality strings are long ("audio-only (default)", "auto • 720p")
// and pushed buttons off-screen. Display stays compact; the dropdown keeps full names.
private fun shortQuality(q: String): String {
    return when {
        q.startsWith("audio-only") -> "🔊 صوت"
        q.startsWith("auto") -> "⚡ تلقائي"
        else -> q.substringBefore(" • ").ifBlank { q }
    }
}

// Smart Arabic error messages: raw ExoPlayer/NewPipe errors are English codes —
// map them to something the user can act on (retry vs. open in YouTube).
private fun smartError(error: PlaybackException?): String {
    if (error == null) return "حدث خطأ غير معروف — جرّب مجددًا أو شاهده في يوتيوب."
    return when (error.errorCode) {
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
        PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT,
        PlaybackException.ERROR_CODE_TIMEOUT -> "انقطع الاتصال أثناء التحميل — تحقق من الإنترنت ثم أعد المحاولة."
        PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
        PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "المصدر غير متاح (رُبما حُذف الفيديو أو قيّده الناشر) — جرّب مجددًا أو شاهده في يوتيوب."
        PlaybackException.ERROR_CODE_BEHIND_LIVE_WINDOW -> "هذا بث مباشر انتهت نافذته — شاهده في يوتيوب."
        PlaybackException.ERROR_CODE_DECODER_INIT_FAILED,
        PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED -> "جهازك تعذّر عليه فك تشفير هذا الفيديو — جرّب جودة أقل أو شاهده في يوتيوب."
        PlaybackException.ERROR_CODE_DRM_SCHEME_UNSUPPORTED,
        PlaybackException.ERROR_CODE_DRM_UNSPECIFIED -> "محتوى محمي لا يمكن تشغيله هنا — شاهده في يوتيوب."
        PlaybackException.ERROR_CODE_REMOTE_ERROR -> "خطأ من جهة الخادم — أعد المحاولة بعد قليل أو شاهده في يوتيوب."
        else -> "تعذّر التشغيل (خطأ ${error.errorCode}) — أعد المحاولة أو شاهده في يوتيوب."
    }
}

private fun formatMs(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%d:%02d", m, s)
}

private fun formatDuration(sec: Long): String {
    val m = sec / 60
    val s = sec % 60
    return if (m >= 60) {
        val h = m / 60
        String.format("%d:%02d:%02d", h, m % 60, s)
    } else {
        String.format("%d:%02d", m, s)
    }
}

private fun formatViews(n: Long): String {
    return when {
        n >= 1_000_000 -> String.format("%.1f مليون مشاهدة", n / 1_000_000.0)
        n >= 1_000 -> String.format("%.1f ألف مشاهدة", n / 1_000.0)
        else -> "$n مشاهدة"
    }
}

// Battery-optimization exemption helpers (checked on every app start/resume).
fun Context.isBatteryExempt(): Boolean {
    return try {
        val pm = getSystemService(android.os.PowerManager::class.java) ?: return true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) pm.isIgnoringBatteryOptimizations(packageName) else true
    } catch (_: Exception) { true }
}

fun Context.batteryExemptionIntent(): Intent {
    return try {
        Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:$packageName")
        }
    } catch (_: Exception) {
        Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
    }
}

fun Context.openInYoutubeCtx(v: VideoItem) {
    val id = v.id ?: return
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$id")).apply { `package` = "com.google.android.youtube" })
    } catch (_: Exception) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$id")))
    }
}