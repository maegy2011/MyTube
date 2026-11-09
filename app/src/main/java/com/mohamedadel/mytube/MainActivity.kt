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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.mohamedadel.mytube.ui.player.SleepTimerDialog
import com.mohamedadel.mytube.ui.playlists.FavoritesScreen
import com.mohamedadel.mytube.ui.playlists.HistoryScreen
import com.mohamedadel.mytube.ui.playlists.PlaylistPickerDialog
import com.mohamedadel.mytube.ui.playlists.PlaylistsScreen
import com.mohamedadel.mytube.ui.library.LibraryScreen
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
import com.mohamedadel.mytube.util.smartError
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.playback.PlaybackService
import androidx.room.Room
import com.mohamedadel.mytube.data.local.MIGRATION_1_2
import com.mohamedadel.mytube.data.local.MIGRATION_2_3
import com.mohamedadel.mytube.data.local.MIGRATION_3_4
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
    var playing by remember { mutableStateOf<VideoItem?>(null) }
    var streamData by remember { mutableStateOf<StreamData?>(null) }
    // selected dubbed/original audio track (auto-matched to the audio pref once prefs load below)
    var audioTrackIdx by remember { mutableStateOf(0) }
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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
    var optionsFor by remember { mutableStateOf<VideoItem?>(null) }
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
        } catch (_: Exception) { }
        try {
            val raw = prefs.getString("recent_searches", "") ?: ""
            if (raw.isNotBlank()) recentSearches = raw.split("\u001f").filter { it.isNotBlank() }.take(6)
        } catch (_: Exception) { }
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
    fun pickAudioIdx(): Int {
        val tracks = streamData?.audioTracks ?: return 0
        if (tracks.isEmpty()) return 0
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
                b.setPreferredTextLanguage(resolveLang(prefSubLang))
            }
            p.trackSelectionParameters = b.build()
        } catch (_: Exception) { }
    }
    // auto-match the subtitle chip along the fallback chain (primary -> secondary -> device -> en)
    fun matchSubIdx(): Int {
        if (prefSubLang == "off") return -1
        val subs = streamData?.subtitles ?: return -1
        if (subs.isEmpty()) return -1
        val device = try { java.util.Locale.getDefault().language } catch (_: Exception) { null }
        val ordered = listOf(resolveLang(prefSubLang), resolveLang(prefSubLang2)) + listOf(device, "en")
        for (want in ordered) {
            if (want == null) break // "original": keep the player's own default
            val i = subs.indexOfFirst { it.langTag.startsWith(want, ignoreCase = true) }
            if (i >= 0) return i
        }
        return -1
    }
    LaunchedEffect(streamData, prefAudioLang, prefAudioLang2) {
        // auto-pick the audio track along the fallback chain (primary -> secondary -> device -> en/original)
        audioTrackIdx = pickAudioIdx()
    }
    LaunchedEffect(streamData, prefSubLang, prefSubLang2) {
        // auto-match the subtitle chip (off stays off)
        subTrackIdx = matchSubIdx()
    }
    // (refreshTrending lives after `scope` below — Kotlin locals must precede use.)
    LaunchedEffect(player, prefSubLang, prefAudioLang) { applyLangPrefs() }
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
    // One-tap favorite toggle (needs scope, so it lives here).
    // Short user feedback for every silent success/failure in favorites/queue/playlists.
    fun toast(msg: String) {
        try { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
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
    fun refreshTrending() {
        val tab = TREND_TABS.getOrNull(trendTab)
        val kiosk = tab?.second
        val isSearchTab = tab?.third == true
        trendingLoading = true
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
        try { player?.playWhenReady = false; player?.pause() } catch (_: Exception) { }
        scope.launch {
            val s = YouTubeRepo.stream(v.url ?: return@launch)
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
            val autoKey = autoUrl(s, kbps)?.let { u ->
                val h = s.videoUrlByQuality.entries.firstOrNull { it.value == u }?.key
                    ?.let { qualityHeight(it) }
                if (h != null && h != Int.MAX_VALUE) "auto \u2022 ${h}p" else "auto"
            } ?: "auto"
            val quals = mutableListOf("audio-only (default)", autoKey)
            val sorted = s.videoUrlByQuality.keys.sortedByDescending { qualityHeight(it) ?: 0 }
            quals.addAll(sorted)
            qualityList = quals
            // default quality preference (applies to every new video)
            val startQ = when (prefQuality) {
                "auto" -> autoKey
                "saver" -> sorted.lastOrNull() ?: "audio-only (default)"
                "high" -> sorted.firstOrNull() ?: autoKey
                else -> "audio-only (default)"
            }
            quality = startQ
            applyQuality(startQ, s, player, autoplay = false)
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
                val res = YouTubeRepo.search(q, page, if (prefCountry == "global") null else prefCountry)
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
    fun doSearch(q: String) {
        if (q.isBlank()) return
        currentQuery = q
        loading = true
        searchError = null
        showSuggestions = false
        searchJob?.cancel()
        searchJob = scope.launch {
            try {
                val page = YouTubeRepo.search(q, null, if (prefCountry == "global") null else prefCountry)
                if (page.error != null && page.items.isEmpty()) {
                    searchError = ctx.getString(R.string.err_search)
                    results = emptyList()
                    hasMore = false
                } else {
                    results = page.items
                    nextPage = page.nextPage
                    hasMore = page.hasNext
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
    fun smartBack(): Boolean {
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
    BackHandler(enabled = isFullscreen || playerExpanded || playing != null || showPlaylists || showFavorites || showHistory || showQueue || showSettings || showSearch || showLibrary || results.isNotEmpty()) { smartBack() }

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
            if (showSearch) {
                SearchScreen(
                    query = query,
                    loading = loading,
                    onQueryChange = { query = it; svm.onQueryChange(it); showSuggestions = true },
                    onSearch = { q -> doSearch(q); showSearch = false },
                    onClose = { showSearch = false },
                    onClear = { query = ""; svm.onQueryChange(""); showSuggestions = false }
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
                                Text(stringResource(R.string.recent_searches), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                                androidx.compose.material3.TextButton(onClick = {
                                    recentSearches = emptyList()
                                    try { prefs.edit().remove("recent_searches").apply() } catch (_: Exception) { }
                                }) { Text(stringResource(R.string.clear_all)) }
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
                    subtitles = streamData?.subtitles ?: emptyList(),
                    subIdx = subTrackIdx,
                    onSubtitleTrack = { i ->
                        // manual pick becomes the saved subtitle preference (applies to all future videos)
                        try {
                            if (i < 0) {
                                prefSubLang = "off"
                                prefs.edit().putString("pref_sub_lang", "off").apply()
                                subTrackIdx = -1
                                toast(ctx.getString(R.string.toast_sub_off))
                            } else {
                                val t = streamData?.subtitles?.getOrNull(i)
                                val tag = t?.langTag?.substringBefore("-")?.takeIf { it.isNotBlank() }
                                val code = tag?.let { c -> if (LANG_OPTIONS.any { o -> o.first == c }) c else "auto" } ?: "auto"
                                prefSubLang = code
                                prefs.edit().putString("pref_sub_lang", code).apply()
                                subTrackIdx = i
                                toast(ctx.getString(R.string.sub_toast_fmt, t?.label?.takeIf { it.isNotBlank() } ?: code))
                            }
                        } catch (_: Exception) { toast(ctx.getString(R.string.toast_sub_fail)) }
                        applyLangPrefs()
                    },
                    audioTracks = streamData?.audioTracks ?: emptyList(),
                    audioIdx = audioTrackIdx,
                    onAudioTrack = { i ->
                        audioTrackIdx = i
                        // manual pick becomes the saved preference (applies to all future videos)
                        try {
                            val t = streamData?.audioTracks?.getOrNull(i)
                            val tag = t?.localeTag?.substringBefore("-") ?: t?.name
                            if (!tag.isNullOrBlank()) {
                                prefAudioLang = tag
                                prefs.edit().putString("pref_audio_lang", tag).apply()
                            }
                        } catch (_: Exception) { }
                        streamData?.let { sd -> applyQuality(quality, sd, player, autoplay = true) }
                    },
                    onNext = { playNextInQueue() },
                    onPrev = { playPrevInQueue() },
                    onFallbackYoutube = { ctx.openInYoutubeCtx(cur) },
                    onEnterPip = onEnterPip,
                    sleepLabel = sleepLeft.ifBlank { null },
                    onSleepClick = { showSleep = true },
                    isFav = cur.favKey() in favoriteSet,
                    onToggleFav = { toggleFavorite(cur) }
                )
            }

            if (!isFullscreen) {
            when {
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
                        onPlaylists = { showLibrary = false; fromLibrary = true; showPlaylists = true }
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
                        histCount = history.size,
                        plCount = playlists.size,
                        dbSize = dbSizeLabel(),
                        onClearFavorites = { clearFavorites() },
                        onClearHistory = { clearHistory() },
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
                        items = history,
                        onPlay = { playVideo(it) },
                        onDelete = { scope.launch { try { dao.deleteHistory(it.favKey()) } catch (_: Exception) { toast(ctx.getString(R.string.toast_delete_item_fail)) } } },
                        onClear = { clearHistory() },
                        onBack = { smartBack() }
                    )
                }
                showFavorites -> {
                    FavoritesScreen(
                        items = favorites,
                        onPlay = { playVideo(it) },
                        onToggleFav = { toggleFavorite(it) },
                        onAdd = { enqueueLast(it) },
                        onClear = { clearFavorites() },
                        onBack = { smartBack() }
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
                        onBack = { showPlaylists = false; openPlaylistId = null }
                    )
                }
                loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
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
                        onPlay = { playVideo(it) },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) },
                        onAdd = { enqueueLast(it) }
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
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // results header: count only — "play all" is gone. The working queue is built
                        // explicitly (row + button, long-press) and started from the queue panel.
                        item {
                            Text(stringResource(R.string.results_found_fmt, results.size), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                onFav = { toggleFavorite(it) }
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
                                                val page = YouTubeRepo.search(currentQuery, pg, if (prefCountry == "global") null else prefCountry)
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

// Estimated downstream bandwidth in Kbps from the active network (0 = unknown).

