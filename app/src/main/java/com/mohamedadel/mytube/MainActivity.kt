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
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.widget.Toast
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.activity.compose.BackHandler
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.player.MiniPlayer
import com.mohamedadel.mytube.ui.player.PlayerArea
import com.mohamedadel.mytube.ui.player.subStyleFromPrefs
import com.mohamedadel.mytube.ui.player.SleepTimerDialog
import com.mohamedadel.mytube.ui.playlists.FavoritesScreen
import com.mohamedadel.mytube.ui.playlists.HistoryScreen
import com.mohamedadel.mytube.ui.playlists.PlaylistPickerDialog
import com.mohamedadel.mytube.ui.playlists.PlaylistsScreen
import com.mohamedadel.mytube.ui.library.LibraryScreen
import com.mohamedadel.mytube.ui.library.FeedScreen
import com.mohamedadel.mytube.ui.library.ChannelScreen
import com.mohamedadel.mytube.ui.queue.QueuePanel
import com.mohamedadel.mytube.ui.results.HomeEmptyState
import com.mohamedadel.mytube.ui.results.SearchRow
import com.mohamedadel.mytube.ui.results.TREND_TABS
import com.mohamedadel.mytube.ui.search.SearchScreen
import com.mohamedadel.mytube.ui.queue.VideoOptionsDialog
import com.mohamedadel.mytube.ui.settings.COUNTRY_OPTIONS
import com.mohamedadel.mytube.ui.settings.LANG_OPTIONS
import com.mohamedadel.mytube.ui.settings.SettingsScreen
import com.mohamedadel.mytube.util.autoUrl
import com.mohamedadel.mytube.util.batteryExemptionIntent
import com.mohamedadel.mytube.util.estimatedDownstreamKbps
import com.mohamedadel.mytube.util.isBatteryExempt
import com.mohamedadel.mytube.util.openInYoutubeCtx
import com.mohamedadel.mytube.util.qualityHeight
import androidx.compose.ui.draw.clip
import com.mohamedadel.mytube.util.formatViews
import com.mohamedadel.mytube.util.smartError
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.playback.PlaybackService
import androidx.room.Room
import com.mohamedadel.mytube.data.local.MIGRATION_1_2
import com.mohamedadel.mytube.data.local.MIGRATION_2_3
import com.mohamedadel.mytube.data.local.MIGRATION_3_4
import com.mohamedadel.mytube.data.local.MIGRATION_4_5
import com.mohamedadel.mytube.data.local.MIGRATION_5_6
import com.mohamedadel.mytube.data.local.MIGRATION_6_7
import com.mohamedadel.mytube.data.local.toHistory
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toFavorite
import com.mohamedadel.mytube.data.local.MyTubeDb
import com.mohamedadel.mytube.data.local.PlaylistEntity
import com.mohamedadel.mytube.data.local.toEntity
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.ui.icons.myTubeLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import com.mohamedadel.mytube.ui.search.SearchViewModel
import com.mohamedadel.mytube.ui.theme.MyTubeTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import org.schabi.newpipe.extractor.Page

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    val pipMode = androidx.compose.runtime.mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // theme + ui-language prefs are read here (before composition) so they survive restarts;
        // changes apply via activity.recreate() from the settings screen.
        val themePref = try { getSharedPreferences("mytube_prefs", MODE_PRIVATE).getString("pref_theme", "dark") ?: "dark" } catch (_: Exception) { "dark" }
        val uiLangPref = try { getSharedPreferences("mytube_prefs", MODE_PRIVATE).getString("pref_ui_lang", "ar") ?: "ar" } catch (_: Exception) { "ar" }
        setContent {
            MyTubeTheme(
                darkTheme = when (themePref) {
                    "light" -> false
                    "system" -> androidx.compose.foundation.isSystemInDarkTheme()
                    else -> true
                },
                rtl = uiLangPref != "en"
            ) { MyTubeApp(onEnterPip = { enterPip() }, onPlayerReady = { player = it }, pipMode = pipMode) }
        }
    }
    // apply the saved UI language before anything inflates (needs no extra dependency)
    override fun attachBaseContext(newBase: Context) {
        val lang = try { newBase.getSharedPreferences("mytube_prefs", MODE_PRIVATE).getString("pref_ui_lang", "ar") ?: "ar" } catch (_: Exception) { "ar" }
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.setLocale(java.util.Locale(lang))
        super.attachBaseContext(newBase.createConfigurationContext(config))
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
    // Survive rotation: the query text and current query live in saved state,
    // results refetch via LaunchedEffect below (ViewModel owns suggestions already).
    var query by rememberSaveable { mutableStateOf("") }
    var currentQuery by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<VideoItem>()) }
    var loading by remember { mutableStateOf(false) }
    var loadingMore by remember { mutableStateOf(false) }
    var hasMore by remember { mutableStateOf(false) }
    var nextPage by remember { mutableStateOf<Page?>(null) }
    // search content filter: all | videos | channels | playlists (persisted; no sort API exists)
    var prefContentFilter by remember { mutableStateOf("all") }
    var playing by remember { mutableStateOf<VideoItem?>(null) }
    var streamData by remember { mutableStateOf<StreamData?>(null) }
    // selected dubbed/original audio track (auto-matched to the audio pref once prefs load below)
    var audioTrackIdx by remember { mutableStateOf(0) }
    var externalAudioHook by remember { mutableStateOf<Player.Listener?>(null) }
    // selected subtitle track: -1 = off (real disable), else index into streamData.subtitles (auto-matched to sub prefs)
    var subTrackIdx by remember { mutableStateOf(-1) }
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
    // YouTube-style bottom-nav state: library groups history/favorites/playlists (existing screens)
    var showLibrary by remember { mutableStateOf(false) }
    var fromLibrary by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    // explicit queue: ONLY what the user queued (play-single => one item; play-all => results copy; long-press => insert/append).
    // Search results are display-only; nothing joins the queue implicitly.
    var queue by remember { mutableStateOf(emptyList<VideoItem>()) }
    // playlists (Room) + playback modes. Modes persist in prefs; playlists persist in Room.
    val db = remember {
        // Release safety: destructive fallback ONLY in debug. A shipped schema change
        // without a written migration must crash loudly in testing, never wipe user data.
        val builder = Room.databaseBuilder(ctx.applicationContext, MyTubeDb::class.java, "mytube.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
        if (com.mohamedadel.mytube.BuildConfig.DEBUG) builder.fallbackToDestructiveMigration()
        builder.build()
    }
    val dao = remember { db.playlists() }
    val playlists by dao.observePlaylists().collectAsState(initial = emptyList())
    var showPlaylists by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    val history by remember { dao.observeHistory() }.collectAsState(initial = emptyList())

    // Favorites (one-tap heart): keys observed live so every heart reflects instantly.
    val favoriteKeys by remember { dao.observeFavoriteKeys() }.collectAsState(initial = emptyList())
    val subscriptions by remember { dao.observeSubscriptions() }.collectAsState(initial = emptyList())
    val favoriteSet = remember(favoriteKeys) { favoriteKeys.toSet() }
    val favorites by remember { dao.observeFavorites() }.collectAsState(initial = emptyList())
    // (toggleFavorite lives after `scope` below — Kotlin locals must precede use.)
    // language prefs: "auto" (device language), "off" (subtitles only), "original" (video's own language),
    // or a language tag (ar/en/…). Secondary = fallback when the primary is unavailable.
    var prefSubLang by remember { mutableStateOf("auto") }
    var prefAudioLang by remember { mutableStateOf("auto") }
    var prefSubLang2 by remember { mutableStateOf("en") }
    var prefAudioLang2 by remember { mutableStateOf("original") }
    // content country (manual only, no IP): drives search/trending region. "global" = YouTube default.
    var prefCountry by remember { mutableStateOf("EG") }
    // app theme: dark (default) | light (large-type friendly) | system
    var prefTheme by remember { mutableStateOf("dark") }
    // UI language: ar (default, RTL) | en (LTR). Applies via recreate().
    var prefUiLang by remember { mutableStateOf("ar") }
    // default video quality for every new video: audio-only | auto | saver | high
    var prefQuality by remember { mutableStateOf("audio-only") }
    // subtitle studio: hide auto-generated, fg/bg/edge/size/bold (applied via CaptionStyleCompat)
    var prefSubHideAuto by remember { mutableStateOf(false) }
    var prefSubFg by remember { mutableStateOf("white") }
    var prefSubBg by remember { mutableStateOf("transparent") }
    var prefSubEdge by remember { mutableStateOf("outline") }
    var prefSubSize by remember { mutableStateOf("m") }
    var prefSubBold by remember { mutableStateOf(false) }
    // audio studio: prefer original + playback speed/pitch
    var prefPreferOriginal by remember { mutableStateOf(false) }
    // B2 precise track roles: sub role (subtitle/caption/descriptive/system) + forced-sub kill + undetermined
    var prefSubRole by remember { mutableStateOf("subtitle") }
    var prefHideForced by remember { mutableStateOf(false) }
    var prefUndetermined by remember { mutableStateOf(true) }
    var prefAudioRole by remember { mutableStateOf("auto") }
    // B3 silence skipping (ExoPlayer SilenceSkippingAudioProcessor via custom RenderersFactory)
    var prefSkipSilence by remember { mutableStateOf(false) }
    // Phase 1: transient media-cache cap (MB) + live size label
    var prefCacheMaxMb by remember { mutableStateOf(500L) }
    // Phase 4: offline mode — pinned items play from disk, unpinned show "pin first"
    var prefOffline by remember { mutableStateOf(false) }
    // Phase 4 fix: quality-switch/retry must reuse the local path for pinned playback,
    // otherwise file:// bytes get needlessly duplicated into the transient cache.
    var playingPinned by remember { mutableStateOf(false) }
    // Phase 6: storage section — pins totals + soft-limit (reminder only, never blocks)
    var prefPinWarnMb by remember { mutableStateOf(1024L) }
    // Perf toggles (settings): feed parallelism + feed cache; lazy lists stay lazy (no toggle needed)
    var prefFeedParallel by remember { mutableStateOf(true) }
    var prefFeedCache by remember { mutableStateOf(true) }
    // M1: next-item stream prefetch (latency only — no media bytes; one extractor fetch).
    // Skipped when offline (pins need no resolve) and capped at 2 entries.
    var prefPreloadNext by remember { mutableStateOf(true) }
    // R5: prefetch on WiFi only (default ON, as promised) — mobile data never prefetches.
    var prefPreloadWifiOnly by remember { mutableStateOf(true) }
    fun isUnmeteredNet(): Boolean {
        return try {
            val cm = ctx.getSystemService(android.net.ConnectivityManager::class.java) ?: return false
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } catch (_: Exception) { false }
    }
    val prefetched = remember { java.util.concurrent.ConcurrentHashMap<String, com.mohamedadel.mytube.data.youtube.StreamData>() }
    var pinsCount by remember { mutableStateOf(0) }
    var pinsSizeLabel by remember { mutableStateOf("—") }
    var pinsBytes by remember { mutableStateOf(0L) }
    var histCount by remember { mutableStateOf(0) }

    var cacheSizeLabel by remember { mutableStateOf("—") }
    var prefSpeed by remember { mutableStateOf(1.0f) }
    var prefPitch by remember { mutableStateOf(1.0f) }
    // standalone search screen (YouTube-style) + trending tabs
    var showSearch by remember { mutableStateOf(false) }
    var trendTab by remember { mutableStateOf(0) }
    var trendingItems by remember { mutableStateOf(emptyList<VideoItem>()) }
    var trendingLoading by remember { mutableStateOf(false) }
    var openPlaylistId by remember { mutableStateOf<Long?>(null) }
    val openItems by remember(openPlaylistId) {
        openPlaylistId?.let { dao.observeItems(it) } ?: flowOf(emptyList())
    }.collectAsState(initial = emptyList())
    // Sleep timer: absolute deadline (uptime ms). The polling loop below watches it;
    // expiry pauses playback and clears itself. Survives nothing — a deliberate session choice.
    var sleepAtMs by remember { mutableStateOf<Long?>(null) }
    var sleepLeft by remember { mutableStateOf("") }
    var showSleep by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var pickFor by remember { mutableStateOf<VideoItem?>(null) }
    var channelDetail by remember { mutableStateOf<com.mohamedadel.mytube.data.youtube.ChannelDetail?>(null) }

    var commentList by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.youtube.CommentItem>()) }
    var showFeed by remember { mutableStateOf(false) }
    var feedItems by remember { mutableStateOf(emptyList<VideoItem>()) }
    var feedLoading by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var commentsLoading by remember { mutableStateOf(false) }

    var optionsFor by remember { mutableStateOf<VideoItem?>(null) }
    // Phase 3: offline pins (faithful snapshot: current quality + audio + subtitle)
    val pins by remember { dao.observePins() }.collectAsState(initial = emptyList())
    val pinKeys = remember(pins) { pins.map { it.videoKey }.toSet() }
    // B: precomputed pin badge texts shared by every list (history builds its own in-screen)
    val pinBadges = remember(pins) {
        pins.associate { p ->
            p.videoKey to ("📌 " + (p.quality ?: "") + (if (p.sizeBytes > 0) {
                val sz = if (p.sizeBytes < 1048576) ctx.getString(R.string.db_kb, (p.sizeBytes / 1024).toInt())
                else ctx.getString(R.string.db_mb, p.sizeBytes / 1048576.0)
                " • $sz"
            } else ""))
        }
    }
    var pinningKeys by remember { mutableStateOf(setOf<String>()) }
    var pinConfirm by remember { mutableStateOf<com.mohamedadel.mytube.data.local.PinManager.PinRequest?>(null) }
    val histFlowAll = remember { androidx.paging.Pager(androidx.paging.PagingConfig(pageSize = 30)) { com.mohamedadel.mytube.data.local.HistoryPaging(dao, 0, emptyList()) }.flow }
    val histFlowPinned = remember(pinKeys) {
        val k = pinKeys.toList().ifEmpty { listOf("__none__") }
        androidx.paging.Pager(androidx.paging.PagingConfig(pageSize = 30)) { com.mohamedadel.mytube.data.local.HistoryPaging(dao, 1, k) }.flow
    }
    val histFlowUnpinned = remember(pinKeys) {
        val k = pinKeys.toList().ifEmpty { listOf("__none__") }
        androidx.paging.Pager(androidx.paging.PagingConfig(pageSize = 30)) { com.mohamedadel.mytube.data.local.HistoryPaging(dao, 2, k) }.flow
    }
    val prefs = remember { ctx.getSharedPreferences("mytube_prefs", android.content.Context.MODE_PRIVATE) }
    // persisted settings + recents (survive app restarts)
    LaunchedEffect(Unit) {
        try {
            prefSubLang = prefs.getString("pref_sub_lang", "auto") ?: "auto"
            prefAudioLang = prefs.getString("pref_audio_lang", "auto") ?: "auto"
            prefSubLang2 = prefs.getString("pref_sub_lang2", "en") ?: "en"
            prefAudioLang2 = prefs.getString("pref_audio_lang2", "original") ?: "original"
            prefCountry = prefs.getString("pref_country", "EG") ?: "EG"
            prefTheme = prefs.getString("pref_theme", "dark") ?: "dark"
            prefUiLang = prefs.getString("pref_ui_lang", "ar") ?: "ar"
            prefQuality = prefs.getString("pref_quality", "audio-only") ?: "audio-only"
            prefContentFilter = prefs.getString("pref_content_filter", "all") ?: "all"
            prefSubHideAuto = prefs.getBoolean("pref_sub_hide_auto", false)
            prefSubFg = prefs.getString("pref_sub_fg", "white") ?: "white"
            prefSubBg = prefs.getString("pref_sub_bg", "transparent") ?: "transparent"
            prefSubEdge = prefs.getString("pref_sub_edge", "outline") ?: "outline"
            prefSubSize = prefs.getString("pref_sub_size", "m") ?: "m"
            prefSubBold = prefs.getBoolean("pref_sub_bold", false)
            prefPreferOriginal = prefs.getBoolean("pref_prefer_original", false)
            prefSubRole = prefs.getString("pref_sub_role", "subtitle") ?: "subtitle"
            prefHideForced = prefs.getBoolean("pref_hide_forced", false)
            prefUndetermined = prefs.getBoolean("pref_undetermined", true)
            prefAudioRole = prefs.getString("pref_audio_role", "auto") ?: "auto"
            prefSkipSilence = prefs.getBoolean("pref_skip_silence", false)
            prefCacheMaxMb = prefs.getLong("pref_cache_max_mb", 500L).takeIf { it > 0 } ?: 500L
            prefOffline = prefs.getBoolean("pref_offline", false)
            prefPinWarnMb = prefs.getLong("pref_pin_warn_mb", 1024L)
            prefFeedParallel = prefs.getBoolean("pref_feed_parallel", true)
            prefFeedCache = prefs.getBoolean("pref_feed_cache", true)
            prefPreloadNext = prefs.getBoolean("pref_preload_next", true)
            prefPreloadWifiOnly = prefs.getBoolean("pref_preload_wifi_only", true)
            // cap only (no disk I/O); the cache itself warms on a background thread after scope exists
            try { com.mohamedadel.mytube.playback.MediaCache.setMax(prefCacheMaxMb * 1024 * 1024) } catch (_: Exception) { }
            prefSpeed = prefs.getFloat("pref_speed", 1.0f)
            prefPitch = prefs.getFloat("pref_pitch", 1.0f)
        } catch (_: Exception) { }
        try {
            val raw = prefs.getString("recent_searches", "") ?: ""
            if (raw.isNotBlank()) recentSearches = raw.split("\u001f").filter { it.isNotBlank() }.take(6)
        } catch (_: Exception) { }
    }
    // playback speed/pitch (ExoPlayer PlaybackParameters): applied live, persisted
    LaunchedEffect(player, prefSpeed, prefPitch) {
        try { player?.playbackParameters = androidx.media3.common.PlaybackParameters(prefSpeed.coerceIn(0.25f, 3f), prefPitch.coerceIn(0.5f, 2f)) } catch (_: Exception) { }
    }
    // language helpers: resolve "auto" to the device language; push prefs into the player.
    // Placed AFTER prefs/player/streamData declarations (Kotlin locals must precede use).
    fun resolveLang(pref: String): String? = when (pref) {
        "off" -> null
        "original" -> null
        "auto" -> try { java.util.Locale.getDefault().language } catch (_: Exception) { "ar" }
        else -> pref
    }
    // fallback chain: primary -> secondary -> device -> English/original. Null = no forcing.
    fun pickAudioIdxFor(sd: com.mohamedadel.mytube.data.youtube.StreamData?): Int {
        val tracks = sd?.audioTracks ?: return 0
        if (tracks.isEmpty()) return 0
        if (prefPreferOriginal) {
            val i = tracks.indexOfFirst { (it.type ?: "").equals("ORIGINAL", ignoreCase = true) }
            if (i >= 0) return i
        }
        val ordered = listOf(resolveLang(prefAudioLang), resolveLang(prefAudioLang2)) +
            listOf(try { java.util.Locale.getDefault().language } catch (_: Exception) { null }, "en")
        for (want in ordered) {
            if (want == null) return 0 // "original" or "off": keep the video's own first track
            val i = tracks.indexOfFirst { t ->
                val tag = t.localeTag ?: t.name ?: ""
                tag.startsWith(want, ignoreCase = true)
            }
            if (i >= 0) return i
        }
        return 0
    }
    fun pickAudioIdx(): Int = pickAudioIdxFor(streamData)
    // Trending loader: defined as a suspend-friendly launcher; the actual scope is
    // passed at call sites (Kotlin locals must precede use, scope lives below).
    fun applyLangPrefs() {
        try {
            val p = player ?: return
            val b = p.trackSelectionParameters.buildUpon()
                .setPreferredAudioLanguage(resolveLang(prefAudioLang))
            if (prefSubLang == "off") {
                // REAL off: disable the text renderer (null preference alone still allows auto captions)
                b.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                b.setPreferredTextLanguage(null)
            } else {
                b.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                if (prefSubRole == "system") {
                    b.setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings(ctx)
                } else {
                    b.setPreferredTextLanguage(resolveLang(prefSubLang))
                    b.setPreferredTextRoleFlags(when (prefSubRole) {
                        "caption" -> C.ROLE_FLAG_CAPTION
                        "descriptive" -> C.ROLE_FLAG_DESCRIBES_VIDEO
                        else -> C.ROLE_FLAG_SUBTITLE
                    })
                }
            }
            if (prefHideForced) b.setIgnoredTextSelectionFlags(C.SELECTION_FLAG_FORCED)
            b.setSelectUndeterminedTextLanguage(prefUndetermined)
            b.setPreferredAudioRoleFlags(when (prefAudioRole) {
                "dubbed" -> C.ROLE_FLAG_DUB
                "original" -> C.ROLE_FLAG_MAIN
                "commentary" -> C.ROLE_FLAG_COMMENTARY
                else -> 0
            })
            // B1 real data cap: saver = 480p + bitrate ceilings (applies to adaptive + fixed picks)
            if (prefQuality == "saver") {
                b.setMaxVideoSize(854, 480)
                b.setMaxVideoBitrate(1_000_000)
                b.setMaxAudioBitrate(64_000)
            } else {
                b.setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
                b.setMaxVideoBitrate(Int.MAX_VALUE)
                b.setMaxAudioBitrate(Int.MAX_VALUE)
            }
            p.trackSelectionParameters = b.build()
        } catch (_: Exception) { }
    }
    // auto-match the subtitle chip along the fallback chain (primary -> secondary -> device -> en)
    fun matchSubIdxFor(sd: com.mohamedadel.mytube.data.youtube.StreamData?): Int {
        if (prefSubLang == "off") return -1
        val subs = sd?.subtitles ?: return -1
        if (subs.isEmpty()) return -1
        val device = try { java.util.Locale.getDefault().language } catch (_: Exception) { null }
        val ordered = listOf(resolveLang(prefSubLang), resolveLang(prefSubLang2)) + listOf(device, "en")
        val pool = if (prefSubHideAuto) subs.filter { !it.auto } else subs
        for (want in ordered) {
            if (want == null) break // "original": keep the player's own default
            var i = pool.indexOfFirst { !it.auto && it.langTag.startsWith(want, ignoreCase = true) }
            if (i >= 0) return subs.indexOf(pool[i])
            i = pool.indexOfFirst { it.langTag.startsWith(want, ignoreCase = true) }
            if (i >= 0) return subs.indexOf(pool[i])
        }
        return -1
    }
    fun matchSubIdx(): Int = matchSubIdxFor(streamData)
    LaunchedEffect(streamData, prefAudioLang, prefAudioLang2) {
        // auto-pick the audio track along the fallback chain (primary -> secondary -> device -> en/original)
        audioTrackIdx = pickAudioIdx()
    }
    LaunchedEffect(streamData, prefSubLang, prefSubLang2) {
        // auto-match the subtitle chip (off stays off)
        subTrackIdx = matchSubIdx()
    }
    // (refreshTrending lives after `scope` below — Kotlin locals must precede use.)
    LaunchedEffect(player, prefSubLang, prefAudioLang, prefQuality, prefSubRole, prefHideForced, prefUndetermined, prefAudioRole) { applyLangPrefs() }
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
    // comments pagination: first page + page-walk with single-flight guard
    var commentsHasMore by remember { mutableStateOf(false) }
    var commentsLoadingMore by remember { mutableStateOf(false) }
    fun loadMoreComments() {
        if (commentsLoadingMore || !commentsHasMore) return
        commentsLoadingMore = true
        scope.launch {
            try {
                val more = YouTubeRepo.CommentPager.more()
                if (more.isNotEmpty()) commentList = commentList + more
                commentsHasMore = YouTubeRepo.CommentPager.hasMore
            } catch (_: Exception) { commentsHasMore = false }
            finally { commentsLoadingMore = false }
        }
    }
    // Fix 3: full channel screen with tabs (replaces the dialog sheet)
    var channelScreenUrl by remember { mutableStateOf<String?>(null) }
    var channelScreenVideos by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.youtube.VideoItem>()) }
    var channelScreenLoading by remember { mutableStateOf(false) }
    fun openChannel(url: String?) {
        if (url.isNullOrBlank()) return
        try { playerExpanded = false } catch (_: Exception) { }
        channelScreenUrl = url
        channelDetail = null
        channelScreenVideos = emptyList()
        channelScreenLoading = true
        scope.launch {
            try { channelDetail = YouTubeRepo.channelDetail(url) } catch (_: Exception) { }
            try { channelScreenVideos = YouTubeRepo.channelVideos(url) } catch (_: Exception) { }
            channelScreenLoading = false
        }
    }
    // SimpleCache init does disk I/O — warm it off the main thread (skill Rule 5)
    LaunchedEffect(Unit) {
        try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { com.mohamedadel.mytube.playback.MediaCache.warmAsync(ctx.applicationContext) } } catch (_: Exception) { }
    }
    // One-tap favorite toggle (needs scope, so it lives here).
    // Short user feedback for every silent success/failure in favorites/queue/playlists.
    fun toast(msg: String) {
        try { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }
    val subsImportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val input = ctx.contentResolver.openInputStream(uri) ?: return@launch
                val pairs = YouTubeRepo.importSubscriptions(input)
                try { input.close() } catch (_: Exception) { }
                var n = 0
                for ((u, name) in pairs) {
                    try {
                        if (dao.subscriptionCount(u) == 0) {
                            dao.insertSubscription(com.mohamedadel.mytube.data.local.SubscriptionEntity(channelUrl = u, name = name))
                            n++
                        }
                    } catch (_: Exception) { }
                }
                toast(ctx.getString(R.string.imported_fmt, n))
            } catch (_: Exception) { toast(ctx.getString(R.string.toast_delete_item_fail)) }
        }
    }
    // data management (reused by the favorites/history screens AND the settings screen)
    fun clearFavorites() {
        scope.launch {
            try { dao.clearFavorites(); toast(ctx.getString(R.string.toast_fav_cleared)) }
            catch (_: Exception) { toast(ctx.getString(R.string.toast_fav_clear_fail)) }
        }
    }
    fun clearHistory() {
        scope.launch {
            try { dao.clearHistory(); toast(ctx.getString(R.string.toast_hist_cleared)) }
            catch (_: Exception) { toast(ctx.getString(R.string.toast_hist_clear_fail)) }
        }
    }
    fun refreshPinsLabel() {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val n = try { pins.size } catch (_: Exception) { 0 }
            val bytes = try { dao.pinsTotalSize() } catch (_: Exception) { 0L }
            val label = try {
                if (bytes < 1048576) ctx.getString(R.string.db_kb, (bytes / 1024).toInt())
                else ctx.getString(R.string.db_mb, bytes / 1048576.0)
            } catch (_: Exception) { "—" }
            pinsCount = n
            pinsSizeLabel = label
            pinsBytes = bytes
        }
    }

    // R2: history local search (Room LIKE, capped 200, off-main)
    var histSearchQuery by remember { mutableStateOf("") }
    // R3: per-channel feed filter (tap a library row). Counts derive from live feedItems.
    var feedFilter by remember { mutableStateOf<String?>(null) }
    val feedCounts = remember(feedItems) { feedItems.groupingBy { it.channelUrl ?: "" }.eachCount().filterKeys { it.isNotEmpty() } }
    var histSearchResults by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.local.HistoryEntity>()) }
    fun runHistSearch(q: String) {
        histSearchQuery = q
        if (q.isBlank()) { histSearchResults = emptyList(); return }
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try { histSearchResults = dao.historySearch(q.trim()) } catch (_: Exception) { }
        }
    }
    fun refreshCacheLabel() {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val bytes = try { com.mohamedadel.mytube.playback.MediaCache.bytesUsed(ctx.applicationContext) } catch (_: Exception) { 0L }
            val label = try {
                if (bytes < 1048576) ctx.getString(R.string.db_kb, (bytes / 1024).toInt())
                else ctx.getString(R.string.db_mb, bytes / 1048576.0)
            } catch (_: Exception) { "—" }
            cacheSizeLabel = label
        }
    }
    fun clearMediaCache() {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try { com.mohamedadel.mytube.playback.MediaCache.clear(ctx.applicationContext) } catch (_: Exception) { }
            refreshCacheLabel()
            toast(ctx.getString(R.string.toast_cache_cleared))
        }
    }
    LaunchedEffect(showSettings) {
        if (showSettings) {
            refreshCacheLabel(); refreshPinsLabel()
            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try { histCount = dao.historyCount() } catch (_: Exception) { }
            }
        }
    }

    fun dbSizeLabel(): String {
        return try {
            val f = ctx.getDatabasePath("mytube.db")
            if (!f.exists()) "—"
            else if (f.length() < 1048576) ctx.getString(R.string.db_kb, (f.length() / 1024).toInt())
            else ctx.getString(R.string.db_mb, f.length() / 1048576.0)
        } catch (_: Exception) { "—" }
    }
    fun toggleFavorite(v: VideoItem) {
        scope.launch {
            try {
                val key = v.favKey()
                if (dao.favoriteCount(key) > 0) {
                    dao.deleteFavorite(key)
                    toast(ctx.getString(R.string.toast_fav_removed))
                } else {
                    dao.insertFavorite(v.toFavorite())
                    toast(ctx.getString(R.string.toast_fav_added))
                }
            } catch (_: Exception) { toast(ctx.getString(R.string.toast_fav_fail)) }
        }
    }
    // Listening history: every explicit play lands here (dedupe by key, capped at 100).
    fun recordHistory(v: VideoItem) {
        scope.launch {
            try {
                dao.upsertHistory(v.toHistory())
                dao.trimHistory()
            } catch (_: Exception) { }
        }
    }
    val trending = remember { listOf("music", "tech reviews", "gaming", "news", "podcasts") }
    var suggestionChips by remember { mutableStateOf(mapOf<String, List<String>>()) }
    // Suggestions tab: seeds = your recent searches (else trending fallbacks);
    // chips = real NewPipe suggestionList completions, videos = top hits per seed.
    fun refreshSuggestions(region: String?, myTab: Int) {
        val seeds = (recentSearches.take(3) + trending.take(2)).distinct().take(3)
        if (seeds.isEmpty()) { if (trendTab == myTab) { suggestionChips = emptyMap(); trendingItems = emptyList() }; return }
        scope.launch {
            try {
                val chips = seeds.associateWith { seed ->
                    try { YouTubeRepo.suggestions(seed).take(4) } catch (_: Exception) { emptyList() }
                }
                val vids = seeds.take(2).flatMap { seed ->
                    try { YouTubeRepo.search(seed, null, region).items.take(6) } catch (_: Exception) { emptyList() }
                }.distinctBy { it.id ?: it.url ?: it.title }.take(18)
                if (trendTab == myTab) { suggestionChips = chips; trendingItems = vids }
            } catch (_: Exception) { if (trendTab == myTab) { suggestionChips = emptyMap(); trendingItems = emptyList() } }
            finally { if (trendTab == myTab) trendingLoading = false }
        }
    }

    fun refreshTrending() {
        val tab = TREND_TABS.getOrNull(trendTab)
        val kiosk = tab?.second
        val isSearchTab = tab?.third == true
        trendingLoading = true
        if (kiosk == "suggestions") {
            val region = if (prefCountry == "global") null else prefCountry
            val myTab = trendTab
            refreshSuggestions(region, myTab)
            return
        }
        scope.launch {
            try {
                val region = if (prefCountry == "global") null else prefCountry
                trendingItems = if (isSearchTab) {
                    // "news" has no YouTube kiosk: honest region-scoped search instead of a fake list
                    YouTubeRepo.search("أخبار", null, region).items
                } else {
                    YouTubeRepo.trending(region, kiosk)
                }
            } catch (_: Exception) { trendingItems = emptyList() }
            finally { trendingLoading = false }
        }
    }
    LaunchedEffect(prefCountry) { if (results.isEmpty() && query.isBlank()) refreshTrending() }
    val svm: SearchViewModel = viewModel()
    val suggestions by svm.suggestions.collectAsState()
    val listState = rememberLazyListState()

    // Policy: default quality is audio-only; NEVER auto-play. Playback starts only via explicit user tap on play.
    fun applyQuality(q: String, s: StreamData, p: ExoPlayer?, autoplay: Boolean = false, forceStart: Boolean = false, local: Boolean = false) {
        if (p == null) return
        val wasPlaying = try { p.isPlaying } catch (_: Exception) { false }
        // user-selected dubbed/original audio track, falling back to lowest
        val selAudio = s.audioTracks.getOrNull(audioTrackIdx)?.url ?: s.audioLowestUrl
        val url = when {
            q == "audio-only (default)" -> selAudio
            q.startsWith("auto") -> autoUrl(s, ctx.estimatedDownstreamKbps())
            else -> s.videoUrlByQuality[q]
        } ?: run {
            playerError = ctx.getString(R.string.err_quality_unavailable)
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
        // Fix: muxed files bake their own audio, so a picked track was silently ignored there.
        // Merged rebuild (muxed video + picked audio) + one-shot disable of the baked audio group.
        val needExternalAudio = isMuxed && selAudio != null && audioTrackIdx != 0 && q != "audio-only (default)"
        if (q == "audio-only (default)" || selAudio == null || (isMuxed && !needExternalAudio)) {
            p.setMediaItem(mediaItem)
            p.prepare()
        } else {
            val httpFactory = DefaultHttpDataSource.Factory()
            // Phase 1: cache-first media (replay within URL lifetime = zero bytes).
            // Phase 4: pinned files bypass the cache entirely (no duplicate bytes on disk).
            val dataSourceFactory = if (local) DefaultDataSource.Factory(ctx, httpFactory) else try {
                com.mohamedadel.mytube.playback.MediaCache.factory(ctx, DefaultDataSource.Factory(ctx, httpFactory))
            } catch (_: Exception) { DefaultDataSource.Factory(ctx, httpFactory) }
            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(selAudio)))
            val merged = MergingMediaSource(videoSource, audioSource)
            p.setMediaSource(merged)
            p.prepare()
            try { externalAudioHook?.let { p.removeListener(it) } } catch (_: Exception) { }
            externalAudioHook = null
            if (needExternalAudio) {
                // MergingMediaSource concatenates groups in source order: [video, muxed-audio, picked-audio].
                // Disabling the muxed-audio group leaves exactly the picked track playing.
                p.addListener((object : Player.Listener {
                    override fun onTracksChanged(tracks: Tracks) {
                        try { p.removeListener(this) } catch (_: Exception) { }
                        try {
                            val audioGroups = tracks.groups.filter { it.type == C.TRACK_TYPE_AUDIO }
                            if (audioGroups.size >= 2) {
                                val ts = p.trackSelector as? DefaultTrackSelector ?: return
                                ts.setParameters(ts.buildUponParameters().setOverrideForType(
                                    TrackSelectionOverride(audioGroups[0].mediaTrackGroup, emptyList())
                                ))
                            }
                        } catch (_: Exception) { }
                    }
                }).also { externalAudioHook = it })
            }
        }
        p.playWhenReady = resume
        applyLangPrefs() // re-assert caps/roles after every media set (quality switch)
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
    fun autoKeyFor(sd: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): String {
        return autoUrl(sd, kbps)?.let { u ->
            val h = sd.videoUrlByQuality.entries.firstOrNull { it.value == u }?.key
                ?.let { qualityHeight(it) }
            if (h != null && h != Int.MAX_VALUE) "auto \u2022 ${h}p" else "auto"
        } ?: "auto"
    }
    fun qualityEntriesFor(sd: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): List<String> {
        val autoKey = autoKeyFor(sd, kbps)
        val sorted = sd.videoUrlByQuality.keys.sortedByDescending { qualityHeight(it) ?: 0 }
        return mutableListOf("audio-only (default)", autoKey).also { it.addAll(sorted) }
    }
    // default quality preference (applies to every new video AND to list-context pins)
    fun pickStartQuality(sd: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): String {
        val autoKey = autoKeyFor(sd, kbps)
        val sorted = sd.videoUrlByQuality.keys.sortedByDescending { qualityHeight(it) ?: 0 }
        return when (prefQuality) {
            "auto" -> autoKey
            "saver" -> sorted.lastOrNull() ?: "audio-only (default)"
            "high" -> sorted.firstOrNull() ?: autoKey
            else -> "audio-only (default)"
        }
    }
    fun playVideo(v: VideoItem, inQueue: Boolean = false, expand: Boolean = true) {
        playing = v
        recordHistory(v)
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
        // queue continuity: advancing inside a playing queue resumes; a fresh tap stays paused (no-autoplay policy)
        val wasPlayingQueue = inQueue && try { player?.isPlaying == true } catch (_: Exception) { false }
        if (!inQueue) try { prefetched.clear() } catch (_: Exception) { }
        try { player?.playWhenReady = false; player?.pause() } catch (_: Exception) { }
        scope.launch {
            val key = v.favKey()
            val app = ctx.applicationContext
            val pinned = try { com.mohamedadel.mytube.data.local.PinFiles.isPinned(app, key) } catch (_: Exception) { false }
            if (prefOffline && !pinned) {
                playerLoading = false
                playerError = ctx.getString(R.string.err_pin_first)
                toast(ctx.getString(R.string.err_pin_first))
                return@launch
            }
            playingPinned = pinned
            if (pinned) {
                // Phase 4: zero-network playback from the faithful snapshot
                try {
                    val dir = com.mohamedadel.mytube.data.local.PinFiles.pinDir(app, key)
                    val vFile = File(dir, "video.mp4")
                    val aFile = File(dir, "audio.m4a")
                    val sFile = File(dir, "sub.vtt")
                    if (!vFile.exists()) throw IllegalStateException("pin missing")
                    val meta = try { org.json.JSONObject(File(dir, "meta.json").readText()) } catch (_: Exception) { org.json.JSONObject() }
                    val label = meta.optString("quality", "audio-only (default)").takeIf { it.isNotBlank() } ?: "audio-only (default)"
                    val hasAudio = aFile.exists()
                    val hasSub = sFile.exists()
                    val vUri = android.net.Uri.fromFile(vFile).toString()
                    val subs = if (hasSub) listOf(
                        com.mohamedadel.mytube.data.youtube.SubtitleItem(
                            url = android.net.Uri.fromFile(sFile).toString(),
                            langTag = meta.optString("subLang", "und").takeIf { it.isNotBlank() } ?: "und",
                            label = meta.optString("subTag", "").takeIf { it.isNotBlank() } ?: label,
                            auto = false,
                            mime = meta.optString("subMime", "text/vtt").takeIf { it.isNotBlank() } ?: "text/vtt"
                        )
                    ) else emptyList()
                    val s = com.mohamedadel.mytube.data.youtube.StreamData(
                        audioLowestUrl = if (hasAudio) android.net.Uri.fromFile(aFile).toString() else if (!hasAudio) vUri else null,
                        videoUrlByQuality = mapOf(label to vUri),
                        muxedQualities = if (hasAudio) emptySet() else setOf(label),
                        title = v.title,
                        subtitles = subs,
                        channelUrl = v.channelUrl
                    )
                    streamData = s
                    qualityList = listOf(label)
                    quality = label
                    audioTrackIdx = 0
                    subTrackIdx = if (hasSub) 0 else -1
                    applyQuality(label, s, player, autoplay = wasPlayingQueue, local = true)
                    playingPinned = true
                } catch (_: Exception) {
                    playerLoading = false
                    playerError = ctx.getString(R.string.toast_pin_fail)
                    toast(ctx.getString(R.string.toast_pin_fail))
                } finally {
                    playerLoading = false
                }
                return@launch
            }
            // M1: consume a prefetched next-item stream when it matches (skips the 1-3s extraction spinner)
            var s = try { prefetched.remove(key) } catch (_: Exception) { null }
            if (s != null && s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) s = null
            if (s == null) {
                s = try { YouTubeRepo.stream(v.url ?: return@launch) } catch (_: Exception) { null }
                if (s == null) {
                    playerLoading = false
                    playerError = ctx.getString(R.string.err_no_sources)
                    return@launch
                }
            }
            // total extraction failure used to leave the spinner forever with NO error (silent death).
            // Detect it here and report a smart message instead.
            if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
                playerLoading = false
                playerError = ctx.getString(R.string.err_no_sources)
                return@launch
            }
            streamData = s
            // label the auto entry with the speed-based target so the user sees what auto will pick
            val kbps = ctx.estimatedDownstreamKbps()
            qualityList = qualityEntriesFor(s, kbps)
            // default quality preference (applies to every new video)
            val startQ = pickStartQuality(s, kbps)
            quality = startQ
            applyQuality(startQ, s, player, autoplay = wasPlayingQueue)
            // M1: prefetch the next queue item's stream (metadata/subs only — zero media bytes)
            try {
                if (prefPreloadNext && !prefOffline && autoAdvance && prefetched.size < 2 && (!prefPreloadWifiOnly || isUnmeteredNet())) {
                    val nxt = queue.getOrNull(queueIndex + 1)
                    if (nxt != null) {
                        val nk = nxt.favKey()
                        val app2 = ctx.applicationContext
                        if (!com.mohamedadel.mytube.data.local.PinFiles.isPinned(app2, nk) && !prefetched.containsKey(nk) && nxt.url != null) {
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    val s2 = YouTubeRepo.stream(nxt.url ?: return@launch)
                                    if (s2.audioLowestUrl != null || s2.videoUrlByQuality.isNotEmpty()) {
                                        if (prefetched.size < 2) prefetched[nk] = s2
                                    }
                                } catch (_: Exception) { }
                            }
                        }
                    }
                }
            } catch (_: Exception) { }
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
    // explicit queue building only: duplicates are refused with a toast (no silent dupes)
    fun isQueued(v: VideoItem): Boolean =
        queue.any { it.favKey() == v.favKey() }
    fun enqueueNext(v: VideoItem) {
        if (isQueued(v)) { toast(ctx.getString(R.string.toast_already_queued)); return }
        val q = queue.toMutableList()
        val at = if (queueIndex >= 0) queueIndex + 1 else q.size
        q.add(at.coerceIn(0, q.size), v)
        queue = q
        toast(ctx.getString(R.string.toast_added_queue))
        if (playing == null) { queueIndex = 0; playVideo(v, inQueue = true, expand = true) }
    }
    fun enqueueLast(v: VideoItem) {
        if (isQueued(v)) { toast(ctx.getString(R.string.toast_already_queued)); return }
        queue = queue + v
        toast(ctx.getString(R.string.toast_added_queue))
        if (playing == null) playVideo(v, inQueue = true, expand = true)
    }
    fun modeLabel(m: String): String = when (m) {
        "repeatAll" -> ctx.getString(R.string.mode_repeat_all)
        "repeatOne" -> ctx.getString(R.string.mode_repeat_one)
        else -> ctx.getString(R.string.mode_order)
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


    LaunchedEffect(prefCountry) { if (results.isEmpty() && query.isBlank()) refreshTrending() }
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
                val res = YouTubeRepo.search(q, page, if (prefCountry == "global") null else prefCountry, prefContentFilter)
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
                playerError = ctx.smartError(error)
                playerLoading = false
                isPlaying = false
            }
        })
    }
    // position / duration / buffer polling for the upgraded player UI
    // Position polling runs ONLY while something is loaded; idle = slow heartbeat, no battery burn.
    LaunchedEffect(player, playing) {
        while (true) {
            val p = player
            if (p != null && playing != null) {
                try {
                    playerPositionMs = p.currentPosition.coerceAtLeast(0L)
                    playerDurationMs = p.duration.takeIf { it > 0 } ?: 0L
                    playerBufferedPct = p.bufferedPercentage
                } catch (_: Exception) { }
                // sleep timer watch
                sleepAtMs?.let { deadline ->
                    val left = deadline - android.os.SystemClock.uptimeMillis()
                    if (left <= 0) {
                        try { p.pause() } catch (_: Exception) { }
                        sleepAtMs = null
                        sleepLeft = ""
                    } else {
                        val m = (left / 60000).toInt()
                        sleepLeft = if (m >= 1) ctx.getString(R.string.sleep_left_min, m) else ctx.getString(R.string.sleep_left_sec, (left / 1000).toInt())
                    }
                }
                kotlinx.coroutines.delay(500)
            } else {
                playerPositionMs = 0L; playerDurationMs = 0L; playerBufferedPct = 0
                kotlinx.coroutines.delay(2000)
            }
        }
    }

    // saved scroll position per query: firstVisibleIndex + offset, restored when the same query is searched again
    val savedScroll = remember { mutableMapOf<String, Pair<Int, Int>>() }
    // Search single-flight with cancellation: a new query kills the previous request,
    // so late responses can never overwrite fresh results. Failures surface with retry.
    var searchJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }
    var searchSuggestion by remember { mutableStateOf<String?>(null) }
    var searchCorrected by remember { mutableStateOf(false) }
    fun doSearch(q: String) {
        if (q.isBlank()) return
        currentQuery = q
        loading = true
        searchError = null
        showSuggestions = false
        searchJob?.cancel()
        searchJob = scope.launch {
            try {
                val page = YouTubeRepo.search(q, null, if (prefCountry == "global") null else prefCountry, prefContentFilter)
                if (page.error != null && page.items.isEmpty()) {
                    searchError = ctx.getString(R.string.err_search)
                    results = emptyList()
                    hasMore = false
                } else {
                    results = page.items
                    nextPage = page.nextPage
                    hasMore = page.hasNext
                    searchSuggestion = page.suggestion
                    searchCorrected = page.corrected
                }
            } catch (_: Exception) {
                searchError = ctx.getString(R.string.err_search)
                results = emptyList()
                hasMore = false
            } finally {
                loading = false
            }
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
    fun toggleSubscription(url: String?, name: String?, avatar: String?) {
        if (url.isNullOrBlank()) return
        scope.launch {
            try {
                if (dao.subscriptionCount(url) > 0) {
                    dao.deleteSubscription(url)
                    toast(ctx.getString(R.string.sub_removed))
                } else {
                    dao.insertSubscription(com.mohamedadel.mytube.data.local.SubscriptionEntity(channelUrl = url, name = name, avatar = avatar))
                    toast(ctx.getString(R.string.sub_added))
                }
            } catch (_: Exception) { toast(ctx.getString(R.string.toast_delete_item_fail)) }
        }
    }
    // A3 feed: per-channel fetch with own try (one dead channel never kills the feed), merged newest-first
    // L1+L2: parallel fetch (cap 3) + 6h Room cache. DAO stays off-main; per-channel try (one dead channel never kills the feed).
    fun refreshFeed() {
        feedLoading = true
        val subs = subscriptions.toList()
        val useParallel = prefFeedParallel
        val useCache = prefFeedCache
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                suspend fun oneChannel(channelUrl: String?): List<VideoItem> {
                    return try {
                        val id = YouTubeRepo.channelIdFor(channelUrl) ?: return emptyList()
                        if (useCache) {
                            val hit = try { dao.getFeedCache(id) } catch (_: Exception) { null }
                            if (hit != null && System.currentTimeMillis() - hit.fetchedAt < 6 * 3600 * 1000L) {
                                val items = com.mohamedadel.mytube.data.local.FeedJson.fromJson(hit.itemsJson)
                                if (items.isNotEmpty()) return items
                            }
                        }
                        // RSS feed first (cheap, chronological); videos-tab fallback when the
                        // feed endpoint is blocked/empty — recent uploads are the same content.
                        val rss = YouTubeRepo.feedFor(id)
                        val fresh = if (rss.isNotEmpty()) rss else YouTubeRepo.channelVideos(channelUrl).take(15)
                        if (useCache && fresh.isNotEmpty()) {
                            try { dao.upsertFeedCache(com.mohamedadel.mytube.data.local.FeedCacheEntity(id, System.currentTimeMillis(), com.mohamedadel.mytube.data.local.FeedJson.toJson(fresh))) } catch (_: Exception) { }
                        }
                        fresh
                    } catch (_: Exception) { emptyList() }
                }
                val all = if (useParallel) {
                    val sem = kotlinx.coroutines.sync.Semaphore(3)
                    subs.map { sub ->
                        async {
                            sem.acquire(); try { oneChannel(sub.channelUrl) } finally { sem.release() }
                        }
                    }.awaitAll().flatten()
                } else {
                    subs.flatMap { oneChannel(it.channelUrl) }
                }
                feedItems = all
            } catch (_: Exception) { }
            feedLoading = false
        }
    }
    // requestPin: player context passes resolved sd+selection (no re-resolve); list context resolves first.
    fun requestPin(v: VideoItem, sd: com.mohamedadel.mytube.data.youtube.StreamData?, q: String?, aIdx: Int?, sIdx: Int?) {
        val key = v.favKey()
        if (pinKeys.contains(key) || pinningKeys.contains(key)) return
        pinningKeys = pinningKeys + key
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val s = sd ?: YouTubeRepo.stream(v.url ?: return@launch)
                if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
                    toast(ctx.getString(R.string.toast_pin_fail)); return@launch
                }
                val kbps = ctx.estimatedDownstreamKbps()
                val qq = q ?: pickStartQuality(s, kbps)
                val ai = aIdx ?: pickAudioIdxFor(s)
                val si = sIdx ?: matchSubIdxFor(s)
                val plan = com.mohamedadel.mytube.data.local.PinManager.buildPlan(s, qq, ai, si, kbps)
                if (plan == null) { toast(ctx.getString(R.string.toast_pin_fail)); return@launch }
                val est = com.mohamedadel.mytube.data.local.PinManager.estimateBytes(listOf(plan.videoUrl, plan.audioUrl, plan.subUrl))
                pinConfirm = com.mohamedadel.mytube.data.local.PinManager.PinRequest(v, plan, est)
            } catch (_: Exception) { toast(ctx.getString(R.string.toast_pin_fail)) }
            finally { pinningKeys = pinningKeys - key }
        }
    }
    fun confirmPin() {
        val pc = pinConfirm ?: return
        pinConfirm = null
        val key = pc.v.favKey()
        pinningKeys = pinningKeys + key
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val size = com.mohamedadel.mytube.data.local.PinManager.downloadPin(ctx.applicationContext, dao, pc.v, pc.plan)
                if (size >= 0) toast(ctx.getString(R.string.toast_pin_done))
                else toast(ctx.getString(R.string.toast_pin_fail))
            } catch (_: Exception) { toast(ctx.getString(R.string.toast_pin_fail)) }
            finally { pinningKeys = pinningKeys - key }
        }
    }
    fun clearAllPins() {
        try {
            val cur = playing?.favKey()
            if (cur != null && com.mohamedadel.mytube.data.local.PinFiles.isPinned(ctx.applicationContext, cur)) closePlayer()
        } catch (_: Exception) { }
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                com.mohamedadel.mytube.data.local.PinFiles.clearAll(ctx.applicationContext)
                try { dao.clearPins() } catch (_: Exception) { }
                refreshPinsLabel()
                toast(ctx.getString(R.string.toast_pins_cleared))
            } catch (_: Exception) { toast(ctx.getString(R.string.toast_pin_fail)) }
        }
    }
    fun doUnpin(key: String) {
        // never delete under an in-flight download (it would re-upsert after us)
        if (pinningKeys.contains(key)) { toast(ctx.getString(R.string.pinning)); return }
        try { if (playing?.favKey() == key) closePlayer() } catch (_: Exception) { }
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                com.mohamedadel.mytube.data.local.PinManager.unpin(ctx.applicationContext, dao, key)
                toast(ctx.getString(R.string.toast_unpinned))
            } catch (_: Exception) { toast(ctx.getString(R.string.toast_pin_fail)) }
        }
    }
    fun smartBack(): Boolean {
        if (showFeed) {
            showFeed = false
            if (fromLibrary) { fromLibrary = false; showLibrary = true }
            return true
        }
        if (channelScreenUrl != null) {
            channelScreenUrl = null
            return true
        }
        if (showComments) {
            showComments = false
            return true
        }
        if (isFullscreen) {
            setFullscreen(false)
            return true
        }
        if (playerExpanded) {
            playerExpanded = false
            return true
        }
        if (showHistory) {
            showHistory = false
            if (fromLibrary) { fromLibrary = false; showLibrary = true }
            return true
        }
        if (showPlaylists) {
            if (openPlaylistId != null) openPlaylistId = null
            else {
                showPlaylists = false
                if (fromLibrary) { fromLibrary = false; showLibrary = true }
            }
            return true
        }
        if (showFavorites) {
            showFavorites = false
            if (fromLibrary) { fromLibrary = false; showLibrary = true }
            return true
        }
        if (showQueue) {
            showQueue = false
            return true
        }
        if (showSearch) {
            showSearch = false
            return true
        }
        if (showSettings) {
            showSettings = false
            return true
        }
        if (showLibrary) {
            showLibrary = false
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
    BackHandler(enabled = showFeed || channelScreenUrl != null || showComments || isFullscreen || playerExpanded || playing != null || showPlaylists || showFavorites || showHistory || showQueue || showSettings || showSearch || showLibrary || results.isNotEmpty()) { smartBack() }

    if (showSplash) {
        // Full-screen solid red splash (same red as the system splash background).
        // Legal lines + version live at the very bottom in small type.
        Box(Modifier.fillMaxSize().background(com.mohamedadel.mytube.ui.theme.SplashScrim), contentAlignment = Alignment.Center) {
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
                Text("v0.2.1", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(4.dp))
                Text("© MyTube. All rights reserved.", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.height(2.dp))
                Text("Not affiliated with YouTube or Google.", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
        return
    }
    val isInPip = pipMode.value
    // No top app bar: logo + nav live INSIDE the search row (saves a full 64dp bar).
    // YouTube tab routing (all destinations are existing screens — design only, no new features)
    fun goHome() {
        showLibrary = false; fromLibrary = false
        showFavorites = false; showHistory = false; showPlaylists = false
        openPlaylistId = null; showSettings = false; playerExpanded = false
    }
    fun goLibrary() {
        showFavorites = false; showHistory = false; showPlaylists = false
        openPlaylistId = null; showSettings = false; playerExpanded = false
        fromLibrary = false; showLibrary = true
    }
    fun goSettings() {
        showLibrary = false; fromLibrary = false
        showFavorites = false; showHistory = false; showPlaylists = false
        openPlaylistId = null; playerExpanded = false
        showSettings = true
    }
    val tabLibrary = showLibrary || showFavorites || showHistory || showPlaylists
    Scaffold(
        topBar = { },
        bottomBar = {
            // Mini-player above the bottom nav (YouTube order): visible whenever audio plays while collapsed.
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
                    onNext = { playNextInQueue() },
                    onPrev = { playPrevInQueue() },
                    hasNext = queueIndex >= 0 && queueIndex < queue.size - 1,
                    hasPrev = queueIndex > 0,
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
            // YouTube bottom nav: Home / Library / Settings. Hidden on watch screen, fullscreen, PiP, search.
            if (!isInPip && !isFullscreen && !playerExpanded && !showSearch) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    NavigationBarItem(
                        selected = !tabLibrary && !showSettings,
                        onClick = { goHome() },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = tabLibrary,
                        onClick = { goLibrary() },
                        icon = { Icon(Icons.Rounded.VideoLibrary, contentDescription = stringResource(R.string.lib_title)) },
                        label = { Text(stringResource(R.string.lib_title)) }
                    )
                    NavigationBarItem(
                        selected = showSettings,
                        onClick = { goSettings() },
                        icon = { Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings_title)) },
                        label = { Text(stringResource(R.string.settings_title)) }
                    )
                }
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
                            stringResource(R.string.battery_banner),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        androidx.compose.material3.Button(onClick = { ensureBatteryExemption() }) {
                            Text(stringResource(R.string.allow))
                        }
                    }
                }
            }
            // Search + suggestions are hidden in fullscreen: they stole vertical space and
            // squeezed the video to "app size" instead of true fullscreen.
            if (!isFullscreen) {
            // Compact header: nav + logo + search icon only (YouTube-style).
            // Tapping search opens a standalone search screen; the inline field is gone.
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (playerExpanded || playing != null || showPlaylists || showSettings || showSearch || showLibrary || results.isNotEmpty()) {
                    IconButton(onClick = { smartBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
                Icon(myTubeLogo(), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(30.dp))
                Spacer(Modifier.weight(1f))
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = { showSearch = true; showSuggestions = true }) {
                    Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search_action))
                }
            }
            // Standalone search screen: full search UI lives here, results stay in the main list.
            // The suggestions dropdown lives INSIDE it (it used to sit below this fillMaxSize
            // column, invisible while typing, flashing only after submit).
            val dropRecents = if (query.isBlank()) recentSearches else recentSearches.filter { r -> r.contains(query, ignoreCase = true) && !suggestions.contains(r) }
            if (showSearch) {
                SearchScreen(
                    query = query,
                    loading = loading,
                    onQueryChange = { query = it; svm.onQueryChange(it); showSuggestions = true },
                    onSearch = { q -> doSearch(q); showSearch = false },
                    onClose = { showSearch = false },
                    onClear = { query = ""; svm.onQueryChange(""); showSuggestions = false },
                    showDropdown = showSuggestions,
                    dropRecents = dropRecents,
                    suggestions = suggestions,
                    onPickSuggestion = { q -> query = q; svm.onQueryChange(q); showSearch = false; doSearch(q) },
                    onClearRecents = {
                        recentSearches = emptyList()
                        try { prefs.edit().remove("recent_searches").apply() } catch (_: Exception) { }
                    }
                )
            }

            when {
                // Fix 3: full channel screen with tabs (header + Videos/About)
                channelScreenUrl != null -> {
                    val chUrl = channelScreenUrl
                    ChannelScreen(
                        detail = channelDetail,
                        videos = channelScreenVideos,
                        videosLoading = channelScreenLoading,
                        isSubscribed = chUrl != null && subscriptions.any { it.channelUrl == chUrl },
                        onSubscribe = { toggleSubscription(chUrl, channelDetail?.name, channelDetail?.avatar) },
                        onBack = { channelScreenUrl = null },
                        onPlay = { playVideo(it) },
                        onLongClick = { optionsFor = it },
                        onAdd = { enqueueLast(it) },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) },
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        subsLabel = { n -> if (n != null && n > 0) ctx.getString(R.string.subs_count_fmt, n) else "" }
                    )
                }
                // queue button opens this panel INSTEAD of the suggestions/results below the video
                showQueue -> {
                    QueuePanel(
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
                                // new playlist: dedupe the working queue before saving
                                val seen = mutableSetOf<String>()
                                val fresh = queue.filter { w -> seen.add(w.id ?: w.url ?: w.title ?: ("#" + w.hashCode())) }
                                val pid = dao.insertPlaylist(PlaylistEntity(name = name))
                                dao.insertItems(fresh.mapIndexed { i, w -> w.toEntity(pid, i) })
                                showQueue = false
                            }
                        },
                        onDismiss = { showQueue = false }
                    )
                }
                showLibrary -> {
                    LibraryScreen(
                        onHistory = { showLibrary = false; fromLibrary = true; showHistory = true },
                        onFavorites = { showLibrary = false; fromLibrary = true; showFavorites = true },
                        onPlaylists = { showLibrary = false; fromLibrary = true; showPlaylists = true },
                        subscriptions = subscriptions,
                        onOpenFeed = { showLibrary = false; fromLibrary = true; showFeed = true; if (feedItems.isEmpty()) refreshFeed() },
                        onUnsubscribe = { url -> scope.launch { try { dao.deleteSubscription(url) } catch (_: Exception) { } } },
                        onImportSubs = { try { subsImportLauncher.launch("*/*") } catch (_: Exception) { } },
                        feedCounts = feedCounts,
                        onOpenChannel = { url -> openChannel(url) }
                    )
                }
                showFeed -> {
                    FeedScreen(
                        items = feedItems,
                        loading = feedLoading,
                        onRefresh = { refreshFeed() },
                        filterChannelUrl = feedFilter,
                        filterChannelName = feedFilter?.let { u -> subscriptions.firstOrNull { it.channelUrl == u }?.name },
                        onClearFilter = { feedFilter = null },
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        onLongClick = { optionsFor = it },
                        onPlay = { playVideo(it) },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) },
                        onAdd = { enqueueLast(it) }
                    )
                }
                showSettings -> {
                    SettingsScreen(
                        subLang = prefSubLang,
                        audioLang = prefAudioLang,
                        subLang2 = prefSubLang2,
                        audioLang2 = prefAudioLang2,
                        country = prefCountry,
                        quality = prefQuality,
                        deviceLang = try { java.util.Locale.getDefault() } catch (_: Exception) { null },
                        onSubLang = { v ->
                            prefSubLang = v
                            try { prefs.edit().putString("pref_sub_lang", v).apply() } catch (_: Exception) { }
                            applyLangPrefs()
                        },
                        onAudioLang = { v ->
                            prefAudioLang = v
                            try { prefs.edit().putString("pref_audio_lang", v).apply() } catch (_: Exception) { }
                            applyLangPrefs()
                        },
                        onSubLang2 = { v ->
                            prefSubLang2 = v
                            try { prefs.edit().putString("pref_sub_lang2", v).apply() } catch (_: Exception) { }
                        },
                        onAudioLang2 = { v ->
                            prefAudioLang2 = v
                            try { prefs.edit().putString("pref_audio_lang2", v).apply() } catch (_: Exception) { }
                            audioTrackIdx = pickAudioIdx()
                        },
                        onCountry = { v ->
                            prefCountry = v
                            try { prefs.edit().putString("pref_country", v).apply() } catch (_: Exception) { }
                            refreshTrending()
                        },
                        onQuality = { v ->
                            prefQuality = v
                            try { prefs.edit().putString("pref_quality", v).apply() } catch (_: Exception) { }
                            toast(ctx.getString(R.string.toast_quality_next))
                        },
                        uiLang = prefUiLang,
                        onUiLang = { v ->
                            prefUiLang = v
                            try { prefs.edit().putString("pref_ui_lang", v).apply() } catch (_: Exception) { }
                            toast(ctx.getString(R.string.toast_lang_restart))
                            try { (ctx as? android.app.Activity)?.recreate() } catch (_: Exception) { }
                        },
                        playMode = playMode,
                        onPlayMode = { v ->
                            setMode(v)
                            toast(ctx.getString(R.string.playmode_toast_fmt, modeLabel(v)))
                        },
                        favCount = favoriteKeys.size,
                        histCount = histCount,
                        plCount = playlists.size,
                        dbSize = dbSizeLabel(),
                        onClearFavorites = { clearFavorites() },
                        onClearHistory = { clearHistory() },
                        cacheSize = cacheSizeLabel,
                        cacheMaxMb = prefCacheMaxMb,
                        onCacheMax = { mb ->
                            prefCacheMaxMb = mb
                            try { prefs.edit().putLong("pref_cache_max_mb", mb).apply() } catch (_: Exception) { }
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    com.mohamedadel.mytube.playback.MediaCache.setMax(mb * 1024 * 1024)
                                    com.mohamedadel.mytube.playback.MediaCache.warmAsync(ctx.applicationContext)
                                } catch (_: Exception) { }
                                refreshCacheLabel()
                            }
                            toast(ctx.getString(R.string.toast_cache_max))
                        },
                        onClearCache = { clearMediaCache() },
                        offlineMode = prefOffline,
                        onOfflineMode = { v ->
                            prefOffline = v
                            try { prefs.edit().putBoolean("pref_offline", v).apply() } catch (_: Exception) { }
                        },
                        pinsCount = pinsCount,
                        pinsSize = pinsSizeLabel,
                        pinsBytes = pinsBytes,
                        pinsWarnMb = prefPinWarnMb,
                        onPinsWarn = { mb ->
                            prefPinWarnMb = mb
                            try { prefs.edit().putLong("pref_pin_warn_mb", mb).apply() } catch (_: Exception) { }
                        },
                        onClearPins = { clearAllPins() },
                        feedParallel = prefFeedParallel,
                        onFeedParallel = { v ->
                            prefFeedParallel = v
                            try { prefs.edit().putBoolean("pref_feed_parallel", v).apply() } catch (_: Exception) { }
                        },
                        feedCache = prefFeedCache,
                        onFeedCache = { v ->
                            prefFeedCache = v
                            try { prefs.edit().putBoolean("pref_feed_cache", v).apply() } catch (_: Exception) { }
                        },
                        preloadNext = prefPreloadNext,
                        onPreloadNext = { v ->
                            prefPreloadNext = v
                            try { prefs.edit().putBoolean("pref_preload_next", v).apply() } catch (_: Exception) { }
                            if (!v) try { prefetched.clear() } catch (_: Exception) { }
                        },
                        preloadWifiOnly = prefPreloadWifiOnly,
                        onPreloadWifiOnly = { v ->
                            prefPreloadWifiOnly = v
                            try { prefs.edit().putBoolean("pref_preload_wifi_only", v).apply() } catch (_: Exception) { }
                        },
                        subHideAuto = prefSubHideAuto,
                        onSubHideAuto = { v -> prefSubHideAuto = v; try { prefs.edit().putBoolean("pref_sub_hide_auto", v).apply() } catch (_: Exception) { } },
                        subFg = prefSubFg,
                        onSubFg = { v -> prefSubFg = v; try { prefs.edit().putString("pref_sub_fg", v).apply() } catch (_: Exception) { } },
                        subBg = prefSubBg,
                        onSubBg = { v -> prefSubBg = v; try { prefs.edit().putString("pref_sub_bg", v).apply() } catch (_: Exception) { } },
                        subEdge = prefSubEdge,
                        onSubEdge = { v -> prefSubEdge = v; try { prefs.edit().putString("pref_sub_edge", v).apply() } catch (_: Exception) { } },
                        subSize = prefSubSize,
                        onSubSize = { v -> prefSubSize = v; try { prefs.edit().putString("pref_sub_size", v).apply() } catch (_: Exception) { } },
                        subBold = prefSubBold,
                        onSubBold = { v -> prefSubBold = v; try { prefs.edit().putBoolean("pref_sub_bold", v).apply() } catch (_: Exception) { } },
                        preferOriginal = prefPreferOriginal,
                        onPreferOriginal = { v -> prefPreferOriginal = v; try { prefs.edit().putBoolean("pref_prefer_original", v).apply() } catch (_: Exception) { }; audioTrackIdx = pickAudioIdx() },
                        subRole = prefSubRole,
                        onSubRole = { v -> prefSubRole = v; try { prefs.edit().putString("pref_sub_role", v).apply() } catch (_: Exception) { } },
                        hideForced = prefHideForced,
                        onHideForced = { v -> prefHideForced = v; try { prefs.edit().putBoolean("pref_hide_forced", v).apply() } catch (_: Exception) { } },
                        undetermined = prefUndetermined,
                        onUndetermined = { v -> prefUndetermined = v; try { prefs.edit().putBoolean("pref_undetermined", v).apply() } catch (_: Exception) { } },
                        audioRole = prefAudioRole,
                        onAudioRole = { v -> prefAudioRole = v; try { prefs.edit().putString("pref_audio_role", v).apply() } catch (_: Exception) { } },
                        skipSilence = prefSkipSilence,
                        onSkipSilence = { v ->
                            prefSkipSilence = v
                            try { prefs.edit().putBoolean("pref_skip_silence", v).apply() } catch (_: Exception) { }
                            toast(ctx.getString(R.string.toast_skip_restart))
                            try {
                                // processor chain is fixed at player build: restart service so the new
                                // RenderersFactory takes effect, then recreate to rebind the new player
                                ctx.stopService(android.content.Intent(ctx, com.mohamedadel.mytube.playback.PlaybackService::class.java))
                            } catch (_: Exception) { }
                            try { (ctx as? android.app.Activity)?.recreate() } catch (_: Exception) { }
                        },
                        speed = prefSpeed,
                        onSpeed = { v -> prefSpeed = v; try { prefs.edit().putFloat("pref_speed", v).apply() } catch (_: Exception) { } },
                        pitch = prefPitch,
                        onPitch = { v -> prefPitch = v; try { prefs.edit().putFloat("pref_pitch", v).apply() } catch (_: Exception) { } },
                        theme = prefTheme,
                        onTheme = { v ->
                            prefTheme = v
                            try { prefs.edit().putString("pref_theme", v).apply() } catch (_: Exception) { }
                            toast(ctx.getString(R.string.toast_theme_restart))
                            try { (ctx as? android.app.Activity)?.recreate() } catch (_: Exception) { }
                        }
                    )
                }
                showHistory -> {
                    HistoryScreen(
                        flowAll = histFlowAll,
                        flowPinned = histFlowPinned,
                        flowUnpinned = histFlowUnpinned,
                        onPlay = { playVideo(it) },
                        onDelete = { scope.launch { try { dao.deleteHistory(it.favKey()) } catch (_: Exception) { toast(ctx.getString(R.string.toast_delete_item_fail)) } } },
                        onClear = { clearHistory() },
                        onBack = { smartBack() },
                        onLongClick = { optionsFor = it },
                        totalCount = histCount,
                        searchQuery = histSearchQuery,
                        onSearchQuery = { runHistSearch(it) },
                        searchResults = histSearchResults,
                        pinInfo = remember(pins) { pins.associateBy { it.videoKey } },
                        pinningKeys = pinningKeys,
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) }
                    )
                }
                showFavorites -> {
                    FavoritesScreen(
                        items = favorites,
                        onPlay = { playVideo(it) },
                        onToggleFav = { toggleFavorite(it) },
                        onAdd = { enqueueLast(it) },
                        onClear = { clearFavorites() },
                        onBack = { smartBack() },
                        onLongClick = { optionsFor = it },
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys
                    )
                }
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
                        onCreate = { name, emoji ->
                            scope.launch { dao.insertPlaylist(PlaylistEntity(name = name, emoji = emoji)) }
                        },
                        onSaveEdits = { pid, name, emoji, orderedIds ->
                            scope.launch {
                                try {
                                    dao.savePlaylistEdits(pid, name, emoji, orderedIds)
                                    toast(ctx.getString(R.string.toast_playlist_saved))
                                } catch (_: Exception) { toast(ctx.getString(R.string.toast_playlist_save_fail)) }
                            }
                        },
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
                        onMoveToPlaylist = { pid, iid, targetPid ->
                            scope.launch {
                                try {
                                    val row = dao.itemsOnce(pid).firstOrNull { it.id == iid }
                                    if (row == null) { toast(ctx.getString(R.string.toast_move_fail)); return@launch }
                                    val v = VideoItem(id = row.videoId, title = row.title, channel = row.channel, thumbnail = row.thumbnail, durationSec = row.durationSec, viewCount = row.viewCount, url = row.url)
                                    val key = v.favKey()
                                    val exists = dao.itemsOnce(targetPid).any { e ->
                                        VideoItem(id = e.videoId, title = e.title, channel = e.channel, thumbnail = e.thumbnail, durationSec = e.durationSec, viewCount = e.viewCount, url = e.url).favKey() == key
                                    }
                                    if (exists) { toast(ctx.getString(R.string.toast_already_target)) }
                                    else {
                                        dao.insertItems(listOf(v.toEntity(targetPid, dao.itemCount(targetPid))))
                                        dao.deleteItem(pid, iid)
                                        toast(ctx.getString(R.string.toast_moved))
                                    }
                                } catch (_: Exception) { toast(ctx.getString(R.string.toast_move_fail)) }
                            }
                        },
                        onBack = { showPlaylists = false; openPlaylistId = null },
                        pinBadges = pinBadges
                    )
                }
                loading -> {
                    com.mohamedadel.mytube.ui.results.VideoListSkeleton(count = 5)
                }
                results.isEmpty() && query.isBlank() -> {
                    HomeEmptyState(
                        trending = trending,
                        trendTab = trendTab,
                        onTrendTab = { i ->
                            trendTab = i
                            refreshTrending()
                        },
                        trendingItems = trendingItems,
                        trendingLoading = trendingLoading,
                        countryName = COUNTRY_OPTIONS.firstOrNull { it.first == prefCountry }?.second?.let { ctx.getString(it) } ?: prefCountry,
                        onSearch = { q -> query = q; doSearch(q) },
                        isSuggestionsTab = TREND_TABS.getOrNull(trendTab)?.second == "suggestions",
                        suggestionChips = suggestionChips,
                        onSuggestion = { q -> query = q; doSearch(q) },
                        onPlay = { playVideo(it) },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) },
                        onAdd = { enqueueLast(it) },
                        onLongClick = { optionsFor = it },
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys
                    )
                }
                results.isEmpty() && !loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (searchError != null) searchError!! else stringResource(R.string.no_results),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (searchError != null) stringResource(R.string.check_connection) else stringResource(R.string.search_try_other),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (searchError != null && currentQuery.isNotBlank()) {
                                Spacer(Modifier.height(12.dp))
                                androidx.compose.material3.Button(onClick = { doSearch(currentQuery) }) { Text(stringResource(R.string.retry)) }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
                    ) {
                        // content filter chips (all/videos/channels/playlists) + count
                        item {
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("all", "videos", "channels", "playlists").forEach { f ->
                                    androidx.compose.material3.FilterChip(
                                        selected = prefContentFilter == f,
                                        onClick = {
                                            prefContentFilter = f
                                            try { prefs.edit().putString("pref_content_filter", f).apply() } catch (_: Exception) { }
                                            if (currentQuery.isNotBlank()) doSearch(currentQuery)
                                        },
                                        label = { Text(stringResource(when (f) { "videos" -> R.string.filter_videos; "channels" -> R.string.filter_channels; "playlists" -> R.string.filter_playlists; else -> R.string.filter_all })) }
                                    )
                                }
                            }
                        }
                        // search correction (own item: never breaks the list)
                        if (searchSuggestion != null) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { doSearch(searchSuggestion!!) }.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (searchCorrected) stringResource(R.string.did_you_mean_showing_fmt, searchSuggestion!!) else stringResource(R.string.did_you_mean_fmt, searchSuggestion!!),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                        item {
                            Text(stringResource(R.string.results_found_fmt, results.size), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp))
                        }
                        itemsIndexed(items = results, key = { idx, v -> "$idx|${v.id ?: v.url ?: v.title ?: "n/a"}" }) { _, v ->
                            SearchRow(
                                v = v,
                                isCurrent = playing?.id == v.id || playing?.url == v.url,
                                isPlaying = isPlaying && (playing?.id == v.id || playing?.url == v.url),
                                isFav = v.favKey() in favoriteSet,
                                onClick = { playVideo(v) },
                                onLongClick = { optionsFor = it },
                                onAdd = { enqueueLast(it) },
                                onFav = { toggleFavorite(it) },
                                pinBadge = pinBadges[v.favKey()],
                                pinning = pinningKeys.contains(v.favKey())
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
                                                val page = YouTubeRepo.search(currentQuery, pg, if (prefCountry == "global") null else prefCountry, prefContentFilter)
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
                                    }) { Text(stringResource(R.string.show_more)) }
                                }
                            }
                        }
                    }
                }
            }
            }
            // add-to-playlist picker for a single video
            pickFor?.let { v ->
                PlaylistPickerDialog(
                    video = v,
                    playlists = playlists,
                    onCreateAndAdd = { name ->
                        scope.launch {
                            try {
                                val pid = dao.insertPlaylist(PlaylistEntity(name = name))
                                dao.insertItems(listOf(v.toEntity(pid, 0)))
                                toast(ctx.getString(R.string.toast_playlist_created))
                            } catch (_: Exception) { toast(ctx.getString(R.string.toast_playlist_create_fail)) }
                            pickFor = null
                        }
                    },
                    onAddTo = { pid ->
                        scope.launch {
                            // no duplicates (same stable key as favorites): reject LOUDLY, never silently
                            val key = v.favKey()
                            val exists = dao.itemsOnce(pid).any { e ->
                                VideoItem(id = e.videoId, title = e.title, channel = e.channel, thumbnail = e.thumbnail, durationSec = e.durationSec, viewCount = e.viewCount, url = e.url).favKey() == key
                            }
                            if (!exists) {
                                val pos = dao.itemCount(pid)
                                dao.insertItems(listOf(v.toEntity(pid, pos)))
                                toast(ctx.getString(R.string.toast_added_playlist))
                            } else {
                                toast(ctx.getString(R.string.toast_already_queued))
                            }
                            pickFor = null
                        }
                    },
                    onDismiss = { pickFor = null }
                )
            }
            // sleep timer picker: arms/clears the auto-pause deadline watched by the polling loop
            if (showSleep) {
                SleepTimerDialog(
                    currentLabel = sleepLeft.ifBlank { null },
                    onPick = { m ->
                        sleepAtMs = if (m == null) null else android.os.SystemClock.uptimeMillis() + m * 60000L
                        if (m == null) sleepLeft = ""
                        showSleep = false
                    },
                    onDismiss = { showSleep = false }
                )
            }
            // pin confirm: faithful snapshot (quality + audio + subtitle) with approximate size
            pinConfirm?.let { pc ->
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { pinConfirm = null },
                    title = { Text(stringResource(R.string.pin_title), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(pc.v.title ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.height(8.dp))
                            val sizeTxt = if (pc.est >= 0) ctx.getString(R.string.pin_size_fmt, pc.est / 1048576.0) else stringResource(R.string.pin_size_unknown)
                            Text(
                                stringResource(R.string.pin_confirm_fmt, pc.plan.quality, sizeTxt),
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    confirmButton = { androidx.compose.material3.TextButton(onClick = { confirmPin() }) { Text(stringResource(R.string.pin_action)) } },
                    dismissButton = { androidx.compose.material3.TextButton(onClick = { pinConfirm = null }) { Text(stringResource(R.string.cancel)) } }
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
                    onPin = {
                        if (pinKeys.contains(v.favKey())) { doUnpin(v.favKey()); optionsFor = null }
                        else { optionsFor = null; requestPin(v, null, null, null, null) }
                    },
                    isPinned = pinKeys.contains(v.favKey()),
                    isPinning = pinningKeys.contains(v.favKey()),
                    onSubscribe = if (v.channelUrl != null) ({ toggleSubscription(v.channelUrl, v.channel, v.channelAvatar); optionsFor = null }) else null,
                    isSubscribed = v.channelUrl != null && subscriptions.any { it.channelUrl == v.channelUrl },
                    onDismiss = { optionsFor = null }
                )
            }
        }
        }
    }
}

// Estimated downstream bandwidth in Kbps from the active network (0 = unknown).

