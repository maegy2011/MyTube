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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TopAppBar
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
                // queue: when a queued track ends, advance to the next result (user started the queue explicitly)
                if (state == Player.STATE_ENDED && autoAdvance) playNextInQueue()
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
                Text("v0.1.5", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
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
                NavigationDrawerItem(label = { Text("Home") }, selected = true, onClick = { scope.launch { drawerState.close() } }, icon = { Icon(Icons.Filled.Home, null) })
            }
        }
    ) {
    val isInPip = pipMode.value
    Scaffold(
        topBar = {
            if (!isInPip) {
            TopAppBar(
                title = {
                    Text("MyTube", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                },
                navigationIcon = {
                    // smart back button: shown whenever back would do something smart; hamburger otherwise
                    if (playing != null || results.isNotEmpty()) {
                        IconButton(onClick = { smartBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
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
                placeholder = { Text("Search videos, channels…") },
                leadingIcon = {
                    if (loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!loading && query.isNotEmpty()) {
                            IconButton(onClick = { query = ""; svm.onQueryChange(""); showSuggestions = false }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                        IconButton(onClick = { doSearch(query) }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
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
                                Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
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
                    onNext = { playNextInQueue() },
                    onPrev = { playPrevInQueue() },
                    onFallbackYoutube = { ctx.openInYoutubeCtx(cur) },
                    onEnterPip = onEnterPip
                )
            }

            when {
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
                            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No results found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Text("Try a different search term", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Text("${results.size} نتيجة", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                onClick = { playVideo(v) }
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
                                    }) { Text("Load more") }
                                }
                            }
                        }
                    }
                }
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
            item { Text("Recent searches", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentSearches) { q -> FilterChip(selected = false, onClick = { onSearch(q) }, label = { Text(q) }) }
                }
            }
        }
        item { Text("Trending", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) }
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
                Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(12.dp))
                Text("Discover videos", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(4.dp))
                Text("Search above to find anything on YouTube", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
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
    onTap: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
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
                    Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = if (isPlaying) "Pause" else "Play", tint = MaterialTheme.colorScheme.primary)
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
fun SearchRow(v: VideoItem, isCurrent: Boolean, isPlaying: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play",
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
            IconButton(onClick = onClick) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = MaterialTheme.colorScheme.primary)
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
    onFallbackYoutube: () -> Unit,
    onEnterPip: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                            Icon(Icons.Filled.Clear, contentDescription = "Error", tint = Color.White, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("تعذر التشغيل", style = MaterialTheme.typography.titleSmall, color = Color.White)
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
                                if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
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
                    Text("▶ تشغيل بالتتابع من نتائج البحث", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
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
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onNext) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onEnterPip) {
                        Icon(Icons.Filled.PictureInPicture, contentDescription = "PiP", tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onFallbackYoutube) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Open in YouTube", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        n >= 1_000_000 -> String.format("%.1fM views", n / 1_000_000.0)
        n >= 1_000 -> String.format("%.1fK views", n / 1_000.0)
        else -> "$n views"
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