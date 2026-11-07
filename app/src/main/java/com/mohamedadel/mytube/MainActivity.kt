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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.activity.compose.BackHandler
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.TopAppBarDefaults
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
    var recentSearches by remember { mutableStateOf(listOf<String>()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var showSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(1800); showSplash = false }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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
    val prefs = remember { ctx.getSharedPreferences("mytube_prefs", android.content.Context.MODE_PRIVATE) }
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
        val url = when {
            q == "audio-only (default)" -> s.audioLowestUrl
            q.startsWith("auto") -> autoUrl(s, ctx.estimatedDownstreamKbps())
            else -> s.videoUrlByQuality[q]
        } ?: return
        val muxedUrls = s.videoUrlByQuality.filter { s.muxedQualities.contains(it.key) }.values.toSet()
        val isMuxed = url in muxedUrls
        val meta = MediaMetadata.Builder()
            .setTitle(playing?.title ?: s.title)
            .setArtist(playing?.channel)
            .setAlbumTitle("MyTube")
            .setDescription("© MyTube")
            .setArtworkUri(playing?.thumbnail?.let { Uri.parse(it) })
            .build()
        val mediaItem = MediaItem.Builder().setUri(Uri.parse(url)).setMediaMetadata(meta).build()
        // Prepare paused; resume only if explicitly requested AND was already playing (quality switch / background switch), or forced (retry).
        val resume = (autoplay && wasPlaying) || forceStart
        p.playWhenReady = false
        if (q == "audio-only (default)" || isMuxed || s.audioLowestUrl == null) {
            p.setMediaItem(mediaItem)
            p.prepare()
        } else {
            val httpFactory = DefaultHttpDataSource.Factory()
            val dataSourceFactory = DefaultDataSource.Factory(ctx, httpFactory)
            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(s.audioLowestUrl)))
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
    fun playVideo(v: VideoItem, queue: Boolean = false) {
        playing = v
        autoAdvance = queue
        queueIndex = results.indexOfFirst { it.id == v.id || (it.url != null && it.url == v.url) }
        playerLoading = true
        playerError = null
        playerPositionMs = 0L; playerDurationMs = 0L; playerBufferedPct = 0
        try { player?.playWhenReady = false; player?.pause() } catch (_: Exception) { }
        scope.launch {
            val s = YouTubeRepo.stream(v.url ?: return@launch)
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
        val next = if (i >= 0) results.getOrNull(i + 1) else null
        if (next != null) playVideo(next, queue = true) else autoAdvance = false
    }
    fun playPrevInQueue() {
        val prev = if (queueIndex > 0) results.getOrNull(queueIndex - 1) else null
        if (prev != null) playVideo(prev, queue = true)
    }
    // playback modes: random pick (shuffle) + wrap-around (repeat all)
    fun playRandomInQueue() {
        if (results.size < 2) { if (results.isNotEmpty()) playVideo(results[0], queue = true); return }
        var n = queueIndex
        while (n == queueIndex) n = (0 until results.size).random()
        playVideo(results[n], queue = true)
    }
    fun playFirstInQueue() {
        if (results.isNotEmpty()) playVideo(results[0], queue = true) else autoAdvance = false
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
                            val nxt = if (queueIndex >= 0) results.getOrNull(queueIndex + 1) else null
                            if (nxt != null) playVideo(nxt, queue = true) else playFirstInQueue()
                        }
                        else -> playNextInQueue()
                    }
                }
            }
            override fun onIsPlayingChanged(playingFlag: Boolean) {
                isPlaying = playingFlag
            }
            override fun onPlayerError(error: PlaybackException) {
                playerError = error.message ?: "Playback error"
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
            }
        }
    }

    // Smart back: player open -> close player (results + scroll kept); results shown -> save scroll, back home. Returns true if consumed.
    fun smartBack(): Boolean {
        if (playing != null) {
            playing = null
            streamData = null
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
    BackHandler(enabled = playing != null || results.isNotEmpty()) { smartBack() }

    if (showSplash) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.ui.res.painterResource(id = com.mohamedadel.mytube.R.drawable.ic_launcher_foreground)?.let { p ->
                    Icon(painter = p, contentDescription = "MyTube", tint = Color.White, modifier = Modifier.size(96.dp))
                    Spacer(Modifier.height(12.dp))
                }
                Text("MyTube", style = MaterialTheme.typography.displayMedium, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Text("v0.1.6", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                Spacer(Modifier.height(24.dp))
                Text("© MyTube. All rights reserved.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                Spacer(Modifier.height(4.dp))
                Text("Not affiliated with YouTube or Google.", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
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
    Scaffold(
        topBar = {
            if (!isInPip) {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(myTubeLogo(), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("MyTube", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    // smart back button: shown whenever back would do something smart; hamburger otherwise
                    if (playing != null || results.isNotEmpty()) {
                        IconButton(onClick = { smartBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        }
                    } else {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Rounded.Menu, contentDescription = "القائمة")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
            }
        },
        bottomBar = {
            // Mini-player: always-visible answer to "which video is playing now" + quick control from anywhere in the list
            if (!isInPip && playing != null) {
                MiniPlayer(
                    video = playing!!,
                    isPlaying = isPlaying,
                    positionMs = playerPositionMs,
                    durationMs = playerDurationMs,
                    onToggle = {
                        playerError = null
                        if (player?.isPlaying == true) player?.pause() else player?.play()
                    },
                    onTap = {
                        // jump the list to the currently playing row
                        scope.launch {
                            val cur = playing ?: return@launch
                            val idx = results.indexOfFirst { it.id == cur.id || (it.url != null && it.url == cur.url) }
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
            // Battery-exemption banner: visible only while the exemption is missing
            if (!batteryExempt) {
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            AnimatedVisibility(visible = showSuggestions && suggestions.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Column {
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

            val cur = playing
            if (cur != null) {
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
                    onNext = { playNextInQueue() },
                    onPrev = { playPrevInQueue() },
                    onFallbackYoutube = { ctx.openInYoutubeCtx(cur) },
                    onEnterPip = onEnterPip
                )
            }

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
                                    results = rows.map { it.toVideoItem() }
                                    currentQuery = ""
                                    playVideo(results[0], queue = true)
                                }
                            }
                        },
                        onPlayFrom = { idx ->
                            val list = openItems.map { it.toVideoItem() }
                            if (idx in list.indices) {
                                showPlaylists = false
                                results = list
                                currentQuery = ""
                                playVideo(list[idx], queue = true)
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
                    HomeEmptyState(recentSearches = recentSearches, trending = trending, onSearch = { q -> query = q; doSearch(q) })
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
                                    if (results.isNotEmpty()) playVideo(results[0], queue = true)
                                }) { Text(if (autoAdvance) "القائمة تعمل" else "تشغيل الكل") }
                            }
                        }
                        itemsIndexed(items = results, key = { idx, v -> "$idx|${v.id ?: v.url ?: v.title ?: "n/a"}" }) { _, v ->
                            SearchRow(
                                v = v,
                                isCurrent = playing?.id == v.id || playing?.url == v.url,
                                isPlaying = isPlaying && (playing?.id == v.id || playing?.url == v.url),
                                onClick = { playVideo(v) },
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
            // queue manager dialog: reorder / remove items of the current list + save it as a playlist
            if (showQueue) {
                QueueDialog(
                    items = results,
                    onMove = { idx, dir ->
                        val j = idx + dir
                        if (idx in results.indices && j in results.indices) {
                            val m = results.toMutableList()
                            val t = m[idx]; m[idx] = m[j]; m[j] = t
                            results = m
                            queueIndex = m.indexOfFirst { it.id == playing?.id || (it.url != null && it.url == playing?.url) }
                        }
                    },
                    onRemove = { idx ->
                        if (idx in results.indices) {
                            results = results.toMutableList().also { it.removeAt(idx) }
                        }
                    },
                    onPlayFrom = { idx -> if (idx in results.indices) playVideo(results[idx], queue = true) },
                    onSave = { name ->
                        scope.launch {
                            val pid = dao.insertPlaylist(PlaylistEntity(name = name))
                            dao.insertItems(results.mapIndexed { i, v -> v.toEntity(pid, i) })
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
fun HomeEmptyState(recentSearches: List<String>, trending: List<String>, onSearch: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (recentSearches.isNotEmpty()) {
            item { Text("عمليات بحث سابقة", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentSearches) { q -> FilterChip(selected = false, onClick = { onSearch(q) }, label = { Text(q) }) }
                }
            }
        }
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

@Composable
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
    onTap: () -> Unit
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
fun SearchRow(v: VideoItem, isCurrent: Boolean, isPlaying: Boolean, onClick: () -> Unit, onAdd: (VideoItem) -> Unit = {}) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
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
    onCycleMode: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onQueue: () -> Unit = {},
    onFallbackYoutube: () -> Unit,
    onEnterPip: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(220.dp).clickable(enabled = errorMsg == null) { onTogglePlay() }
            ) {
                if (quality != "audio-only (default)" && player != null) {
                    AndroidView(
                        factory = { c ->
                            PlayerView(c).apply {
                                this.player = player
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                useController = true
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(model = video.thumbnail, contentDescription = video.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)))))
                }
                // center state: error > loading > big play/pause button
                when {
                    errorMsg != null -> {
                        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.Clear, contentDescription = "خطأ", tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("تعذّر التشغيل", style = MaterialTheme.typography.titleSmall, color = Color.White)
                            Spacer(Modifier.height(2.dp))
                            Text("تحقق من الاتصال ثم حاول مجددًا", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                            Spacer(Modifier.height(8.dp))
                            androidx.compose.material3.Button(onClick = onRetry) { Text("إعادة المحاولة") }
                        }
                    }
                    playerLoading -> {
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
                    quality == "audio-only (default)" || player == null -> {
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
            }
            // seek + time row (when duration known)
            if (durationMs > 0) {
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
                        "▶ القائمة تعمل • $playModeLabel" + if (shuffleOn) " • عشوائي" else "",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        FilterChip(selected = false, onClick = { expanded = true }, label = { Text(quality) })
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            qualities.forEach { q -> DropdownMenuItem(text = { Text(q) }, onClick = { onQuality(q); expanded = false }) }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    IconButton(onClick = onPrev) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "السابق", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "التالي", tint = MaterialTheme.colorScheme.primary)
                    }
                    // playback mode: order -> repeat all -> repeat one (cycles)
                    IconButton(onClick = onCycleMode) {
                        Icon(
                            when (playMode) { "repeatOne" -> Icons.Rounded.RepeatOne; else -> Icons.Rounded.Repeat },
                            contentDescription = "وضع التشغيل: $playModeLabel",
                            tint = if (playMode == "order") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                        )
                    }
                    // shuffle toggle
                    IconButton(onClick = onToggleShuffle) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = if (shuffleOn) "إيقاف العشوائي" else "تشغيل عشوائي", tint = if (shuffleOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    // current queue: manage / reorder / save
                    IconButton(onClick = onQueue) {
                        Icon(Icons.Rounded.QueueMusic, contentDescription = "إدارة القائمة الحالية", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onEnterPip) {
                        Icon(Icons.Rounded.PictureInPicture, contentDescription = "صورة داخل صورة", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onFallbackYoutube) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "فتح في يوتيوب", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
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