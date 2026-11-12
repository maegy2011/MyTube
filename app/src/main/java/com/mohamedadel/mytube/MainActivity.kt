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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.widget.Toast
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.dash.DashMediaSource
import com.mohamedadel.mytube.util.BROWSER_UA
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
import com.mohamedadel.mytube.ui.library.NotificationsScreen
import com.mohamedadel.mytube.ui.library.FeedScreen
import com.mohamedadel.mytube.ui.library.ChannelScreen
import com.mohamedadel.mytube.ui.library.MusicHomeShelves
import com.mohamedadel.mytube.ui.queue.QueuePanel
import com.mohamedadel.mytube.ui.results.HomeEmptyState
import com.mohamedadel.mytube.ui.results.SearchRow
import com.mohamedadel.mytube.ui.results.TREND_TABS
import com.mohamedadel.mytube.ui.search.SearchScreen
import com.mohamedadel.mytube.ui.queue.VideoOptionsDialog
import com.mohamedadel.mytube.ui.settings.COUNTRY_OPTIONS
import com.mohamedadel.mytube.ui.settings.SettingsScreen
import com.mohamedadel.mytube.util.autoUrl
import com.mohamedadel.mytube.util.batteryExemptionIntent
import com.mohamedadel.mytube.util.estimatedDownstreamKbps
import com.mohamedadel.mytube.util.isBatteryExempt
import com.mohamedadel.mytube.util.isLocal
import com.mohamedadel.mytube.util.isLocalAudioUri
import com.mohamedadel.mytube.util.scanLocalAudio
import com.mohamedadel.mytube.util.openInYoutubeCtx
import com.mohamedadel.mytube.util.shareVideoCtx
import com.mohamedadel.mytube.util.qualityHeight
import androidx.compose.ui.draw.clip
import com.mohamedadel.mytube.util.formatViews
import com.mohamedadel.mytube.util.smartError
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import com.mohamedadel.mytube.playback.CastManager
import com.mohamedadel.mytube.playback.PlaybackService
import androidx.room.Room
import com.mohamedadel.mytube.data.local.MIGRATION_1_2
import com.mohamedadel.mytube.data.local.MIGRATION_2_3
import com.mohamedadel.mytube.data.local.MIGRATION_3_4
import com.mohamedadel.mytube.data.local.MIGRATION_4_5
import com.mohamedadel.mytube.data.local.MIGRATION_5_6
import com.mohamedadel.mytube.data.local.MIGRATION_6_7
import com.mohamedadel.mytube.data.local.MIGRATION_7_8
import com.mohamedadel.mytube.data.local.MIGRATION_8_9
import com.mohamedadel.mytube.data.local.MIGRATION_9_10
import com.mohamedadel.mytube.data.local.toHistory
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toFavorite
import com.mohamedadel.mytube.data.local.MyTubeDb
import com.mohamedadel.mytube.data.local.NotifCenter
import com.mohamedadel.mytube.data.local.NotificationItemEntity
import com.mohamedadel.mytube.data.local.PlaylistEntity
import com.mohamedadel.mytube.data.local.toEntity
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.ui.icons.myTubeLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import com.mohamedadel.mytube.ui.search.SearchViewModel
import com.mohamedadel.mytube.ui.theme.MyTubeTheme
import com.mohamedadel.mytube.ui.player.volumeLive
import com.mohamedadel.mytube.ui.player.mutedLive
import com.mohamedadel.mytube.data.youtube.SbSeg
import com.mohamedadel.mytube.data.youtube.SponsorBlock
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.verticalScroll
import org.schabi.newpipe.extractor.Page

// Appearance live holders (file-level so both the Activity and the top-level
// MyTubeApp composable see them): setContent observes these, so theme/accent
// apply instantly with no recreate — the queue survives a theme switch.
private val themeLive = androidx.compose.runtime.mutableStateOf("dark")
private val accentLive = androidx.compose.runtime.mutableStateOf("red")

// Restart-prompt (11 Nov 2026 — بأمر محمد): full process restart after a
// theme/accent change — relaunch MainActivity with a clean task, then exit.
// API 26+: getLaunchIntentForPackage + CLEAR_TASK works on all supported levels.
private fun restartAppNow(activity: android.app.Activity) {
    try {
        val launch = activity.packageManager.getLaunchIntentForPackage(activity.packageName)?.apply {
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        if (launch != null) activity.startActivity(launch)
    } catch (_: Exception) { }
    try { android.os.Process.killProcess(android.os.Process.myPid()) } catch (_: Exception) { }
    try { kotlin.system.exitProcess(0) } catch (_: Exception) { }
}

// pin-deepfix 2026-11-11 (hoisted 2026-11-12): retry choices for the one fresh-resolve
// retry in downloads. Was a local class inside MyTubeApp; hoisted to top level so
// PinDownloadRunner (data/local) can reference it — the composable method sits at
// the 64KB codegen limit and the download body had to move out (MethodTooLarge).
data class PinRetry(val quality: String, val audioIdx: Int, val subIdxs: Set<Int>)

class MainActivity : ComponentActivity() {
    private var player: ExoPlayer? = null
    val pipMode = androidx.compose.runtime.mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        pendingSharedIntent = intent
        try { if (intent?.getBooleanExtra("mytube_expand_player", false) == true) expandPlayerRequest.value++ } catch (_: Exception) { }
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        // theme + ui-language prefs are read here (before composition) so they survive restarts;
        // theme/accent changes apply live via themeLive/accentLive (no recreate — queue survives).
        // Restart-prompt (11 Nov 2026): darkTheme reads themeLive (was the stale themePref snapshot,
        // so dark/light/system needed a manual restart); values are saved immediately and the settings
        // dialog offers restart-now for full consistency, or later (applies on next launch at latest).
        val themePref = try { getSharedPreferences("mytube_prefs", MODE_PRIVATE).getString("pref_theme", "dark") ?: "dark" } catch (_: Exception) { "dark" }
        val accentPref = try { getSharedPreferences("mytube_prefs", MODE_PRIVATE).getString("pref_accent", "red") ?: "red" } catch (_: Exception) { "red" }
        try { themeLive.value = themePref } catch (_: Exception) { }
        try { accentLive.value = accentPref } catch (_: Exception) { }
        try { volumeLive.value = getSharedPreferences("mytube_prefs", MODE_PRIVATE).getFloat("pref_volume", 1f).coerceIn(0f, 1f) } catch (_: Exception) { }
        try { mutedLive.value = getSharedPreferences("mytube_prefs", MODE_PRIVATE).getBoolean("pref_muted", false) } catch (_: Exception) { }
        val uiLangPref = try { getSharedPreferences("mytube_prefs", MODE_PRIVATE).getString("pref_ui_lang", "ar") ?: "ar" } catch (_: Exception) { "ar" }
        setContent {
            MyTubeTheme(
                darkTheme = when (themeLive.value) {
                    "light" -> false
                    "system" -> androidx.compose.foundation.isSystemInDarkTheme()
                    else -> true
                },
                black = themeLive.value == "black",
                accent = accentLive.value,
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
    companion object {
        // G-A1 (audit P2 2026-11-11): onCreate/onNewIntent park the shared/viewed intent here;
        // the composition consumes it once playVideo/doSearch are in scope.
        var pendingSharedIntent: android.content.Intent? = null
        // 2026-11-12 (runtime fix 3b): media-notification tap = explicit tap → open player.
        // State (not a plain counter) so composition re-fires on every tap.
        var expandPlayerRequest = androidx.compose.runtime.mutableStateOf(0)
    }
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        try { setIntent(intent) } catch (_: Exception) { }
        pendingSharedIntent = intent
        try { if (intent.getBooleanExtra("mytube_expand_player", false)) expandPlayerRequest.value++ } catch (_: Exception) { }
    }
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: android.content.res.Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        pipMode.value = isInPictureInPictureMode
    }
    // L6 (audit P2 2026-11-11): PiP params with play/pause + close RemoteActions (API26+).
    // The toggle icon/label follow the `playing` argument — callers refresh on isPlaying change.
    private fun buildPipParams(autoPip: Boolean, playing: Boolean): PictureInPictureParams {
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
        val b = PictureInPictureParams.Builder()
            .setAspectRatio(ratio)
            .apply { if (android.os.Build.VERSION.SDK_INT >= 31) setAutoEnterEnabled(autoPip) }
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            try {
                val toggleTitle = if (playing) getString(R.string.sleep_pause) else getString(R.string.play)
                val toggleIcon = android.graphics.drawable.Icon.createWithResource(
                    this, if (playing) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                )
                val closeIcon = android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel)
                b.setActions(listOf(
                    android.app.RemoteAction(toggleIcon, toggleTitle, toggleTitle,
                        com.mohamedadel.mytube.playback.PipActionReceiver.pendingToggle(this)),
                    android.app.RemoteAction(closeIcon, getString(R.string.close), getString(R.string.close),
                        com.mohamedadel.mytube.playback.PipActionReceiver.pendingClose(this))
                ))
            } catch (_: Exception) { }
        }
        return b.build()
    }
    private fun enterPip() {
        // L2 (audit P0 2026-11-11): auto-enter follows pref_pip — a stale `true` used to stick after the first manual PiP.
        val autoPip = try { getSharedPreferences("mytube_prefs", MODE_PRIVATE).getBoolean("pref_pip", true) } catch (_: Exception) { true }
        val playingNow = try { player?.isPlaying == true } catch (_: Exception) { false }
        val params = buildPipParams(autoPip, playingNow)
        try { setPictureInPictureParams(params) } catch (_: Exception) { }
        enterPictureInPictureMode(params)
    }

    override fun onUserLeaveHint() {
        val h: Int = try { player?.videoSize?.height ?: 0 } catch (_: Exception) { 0 }
        val playing: Boolean = try { player?.isPlaying == true } catch (_: Exception) { false }
        // Auto-PiP obeys the pref (default on = current behavior); the manual PiP button always works.
        val autoPip = try { getSharedPreferences("mytube_prefs", MODE_PRIVATE).getBoolean("pref_pip", true) } catch (_: Exception) { true }
        if (playing && h > 0 && autoPip) enterPip()
        else if (!autoPip && android.os.Build.VERSION.SDK_INT >= 31) {
            // L2: clear a sticky auto-enter flag when the user turned the pref off.
            try {
                setPictureInPictureParams(PictureInPictureParams.Builder().setAutoEnterEnabled(false).build())
            } catch (_: Exception) { }
        }
        super.onUserLeaveHint()
    }
    // H1: NEVER release here — the ExoPlayer instance is owned by PlaybackService (shared reference).
    // Releasing it killed background audio on activity exit + double-released with the service.
    override fun onDestroy() { player = null; try { PlaybackService.onCustomAction = null } catch (_: Exception) { }; super.onDestroy() }
}

// screens-fix Library P2-L10: single const for the default quality key (was a magic string ×5).
private const val QUALITY_AUDIO_ONLY = "audio-only (default)"

// 2026-11-12 (runtime fix): playback session survives rotation/process death so the
// mini-player, queue position and radio flag are not wiped by a config change.
// VideoItem is not Parcelable, so it is encoded as one \u001F-joined string of
// Uri-encoded fields (Bundle-safe); the queue saver caps at 50 items (Binder ~1MB).
private fun encodeVideoItem(v: VideoItem): String {
    fun s(x: String?): String = try { android.net.Uri.encode(x ?: "") } catch (_: Exception) { "" }
    return listOf(s(v.id), s(v.title), s(v.channel), s(v.thumbnail),
        v.durationSec?.toString() ?: "", v.viewCount?.toString() ?: "",
        s(v.url), s(v.channelAvatar), if (v.verified) "1" else "",
        s(v.uploadDate), if (v.isShort) "1" else "", s(v.channelUrl)).joinToString("\u001F")
}
private fun decodeVideoItem(e: String): VideoItem? = try {
    val p = e.split("\u001F")
    if (p.size < 12) null else {
        fun d(i: Int): String? = try { android.net.Uri.decode(p[i]).takeIf { it.isNotEmpty() } } catch (_: Exception) { p[i].takeIf { it.isNotEmpty() } }
        VideoItem(id = d(0), title = d(1), channel = d(2), thumbnail = d(3),
            durationSec = p[4].toLongOrNull(), viewCount = p[5].toLongOrNull(), url = d(6),
            channelAvatar = d(7), verified = p[8] == "1", uploadDate = d(9),
            isShort = p[10] == "1", channelUrl = d(11))
    }
} catch (_: Exception) { null }
private val VideoItemSaver = Saver<androidx.compose.runtime.MutableState<VideoItem?>, String>(
    save = { it.value?.let(::encodeVideoItem) ?: "" },
    restore = { androidx.compose.runtime.mutableStateOf(if (it.isEmpty()) null else decodeVideoItem(it)) }
)
private val VideoQueueSaver = Saver<androidx.compose.runtime.MutableState<List<VideoItem>>, ArrayList<String>>(
    save = { ArrayList(it.value.take(50).map(::encodeVideoItem)) },
    restore = { list -> androidx.compose.runtime.mutableStateOf(list.mapNotNull(::decodeVideoItem)) }
)

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
    var playing by rememberSaveable(saver = VideoItemSaver) { mutableStateOf<VideoItem?>(null) }
    var streamData by remember { mutableStateOf<StreamData?>(null) }
    // selected dubbed/original audio track (auto-matched to the audio pref once prefs load below)
    var audioTrackIdx by remember { mutableStateOf(0) }
    // Low: plain holder — no composable reads this; state caused pointless recompositions.
    val externalAudioHook = remember { arrayOfNulls<Player.Listener>(1) }
    // selected subtitle track: -1 = off (real disable), else index into streamData.subtitles (auto-matched to sub prefs)
    var subTrackIdx by remember { mutableStateOf(-1) }
    // F8 (audit P1 2026-11-11): which video the explicit pick belongs to — guards the
    // TrackSelectionOverride against stale currentTracks right after a video change.
    var subTrackFor by remember { mutableStateOf<String?>(null) }
    var qualityList by remember { mutableStateOf(listOf(QUALITY_AUDIO_ONLY)) }
    var quality by remember { mutableStateOf(QUALITY_AUDIO_ONLY) }
    var playerLoading by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var playerPositionMs by remember { mutableStateOf(0L) }
    var playerDurationMs by remember { mutableStateOf(0L) }
    var playerBufferedPct by remember { mutableStateOf(0) }
    var playerError by remember { mutableStateOf<String?>(null) }
    // queue (suggestion #3): sequential playback of the result list, started only by explicit user action
    var autoAdvance by rememberSaveable { mutableStateOf(false) }
    // Radio (endless related queue): seeded from StreamData.related, refilled at every
    // queue end from the just-played track's own related list. Local-only, no account.
    var radioOn by rememberSaveable { mutableStateOf(false) }
    var queueIndex by rememberSaveable { mutableStateOf(-1) }
    // full player vs mini player: back collapses (keeps audio), X closes (stops audio)
    var playerExpanded by rememberSaveable { mutableStateOf(false) }
    // 2026-11-12 (runtime fix 3b): media-notification tap = explicit tap → open the player.
    LaunchedEffect(MainActivity.expandPlayerRequest.value) {
        if (MainActivity.expandPlayerRequest.value > 0) {
            playerExpanded = true
            MainActivity.expandPlayerRequest.value = 0
        }
    }
    // fullscreen experiment: native ExoPlayer controls only; follows device rotation via SENSOR
    var isFullscreen by remember { mutableStateOf(false) }
    var recentSearches by remember { mutableStateOf(listOf<String>()) }
    var showSuggestions by remember { mutableStateOf(false) }
    // G-A1: bumped on every resume when a shared/view intent is parked (consumed below after doSearch is in scope).
    var sharedPending by remember { mutableStateOf(0) }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var showSplash by remember { mutableStateOf(true) }
    // Low: splash target time (the wait loop sits after feedItems is declared, below).
    // YouTube-style bottom-nav state: library groups history/favorites/playlists (existing screens)
    var showLibrary by remember { mutableStateOf(false) }
    var fromLibrary by remember { mutableStateOf(false) }
    val ctx = LocalContext.current
    fun toast(msg: String) {
        try { Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
    }
    // S5 (audit P2 2026-11-11): toasts from IO scopes — Toast needs the main Looper, post it.
    fun uiToast(msg: String) {
        try {
            if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) toast(msg)
            else android.os.Handler(android.os.Looper.getMainLooper()).post { try { toast(msg) } catch (_: Exception) { } }
        } catch (_: Exception) { }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    // explicit queue: ONLY what the user queued (play-single => one item; play-all => results copy; long-press => insert/append).
    // Search results are display-only; nothing joins the queue implicitly.
    var queue by rememberSaveable(saver = VideoQueueSaver) { mutableStateOf(emptyList<VideoItem>()) }
    // playlists (Room) + playback modes. Modes persist in prefs; playlists persist in Room.
    val db = remember {
        // Release safety: destructive fallback ONLY in debug. A shipped schema change
        // without a written migration must crash loudly in testing, never wipe user data.
        val builder = Room.databaseBuilder(ctx.applicationContext, MyTubeDb::class.java, "mytube.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
        if (com.mohamedadel.mytube.BuildConfig.DEBUG) builder.fallbackToDestructiveMigration()
        builder.build()
    }
    val dao = remember { db.playlists() }
    val playlists by dao.observePlaylists().collectAsStateWithLifecycle(initialValue = emptyList())
    var showPlaylists by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showFavorites by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    val history by remember { dao.observeHistory() }.collectAsStateWithLifecycle(initialValue = emptyList())

    // Favorites (one-tap heart): keys observed live so every heart reflects instantly.
    val favoriteKeys by remember { dao.observeFavoriteKeys() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val subscriptions by remember { dao.observeSubscriptions() }.collectAsStateWithLifecycle(initialValue = emptyList())
    // Channel groups (Room v8): named buckets; feed-by-group filters the subscription feed.
    val groups by remember { dao.observeGroups() }.collectAsStateWithLifecycle(initialValue = emptyList())
    var showGroups by remember { mutableStateOf(false) }
    var openGroup by remember { mutableStateOf<com.mohamedadel.mytube.data.local.ChannelGroupEntity?>(null) }
    val groupMembers by remember(openGroup) {
        // T5: local val instead of openGroup!!.
        val og = openGroup
        if (og == null) kotlinx.coroutines.flow.flowOf(emptyList<com.mohamedadel.mytube.data.local.ChannelGroupMemberEntity>())
        else dao.observeGroupMembers(og.id)
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    // group member counts refresh alongside pins/history labels
    var groupCounts by remember { mutableStateOf(mapOf<Long, Int>()) }
    var feedGroupUrls by remember { mutableStateOf<Set<String>?>(null) }
    var feedGroupName by remember { mutableStateOf<String?>(null) }
    // Channel extra tabs (lazy per tab)
    var channelShorts by remember { mutableStateOf(emptyList<VideoItem>()) }
    var channelShortsLoading by remember { mutableStateOf(false) }
    var channelLive by remember { mutableStateOf(emptyList<VideoItem>()) }
    var channelLiveLoading by remember { mutableStateOf(false) }
    var channelPls by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.youtube.RemotePlaylist>()) }
    var channelPlsLoading by remember { mutableStateOf(false) }
    var channelTabsLoaded by remember { mutableStateOf(setOf<Int>()) }
    val favoriteSet = remember(favoriteKeys) { favoriteKeys.toSet() }
    val favorites by remember { dao.observeFavorites() }.collectAsStateWithLifecycle(initialValue = emptyList())
    // (toggleFavorite lives after `scope` below — Kotlin locals must precede use.)
    // language prefs: "auto" (device language), "off" (subtitles only), "original" (video's own language),
    // or a language tag (ar/en/…). Secondary = fallback when the primary is unavailable.
    var prefSubLang by remember { mutableStateOf("auto") }
    var prefAudioLang by remember { mutableStateOf("auto") }
    var prefSubLang2 by remember { mutableStateOf("en") }
    var prefAudioLang2 by remember { mutableStateOf("original") }
    // content country (manual only, no IP): drives search/trending region. "global" = YouTube default.
    var prefCountry by remember { mutableStateOf("EG") }
    // Geo-switch cache guard (2026-11-12, runtime layer): the newly picked country
    // waits here until the user confirms the cache-clear dialog; refuse = null
    // (old value kept, no partial state), accept = caches cleared then applied.
    var pendingCountry by remember { mutableStateOf<String?>(null) }
    // app theme: dark (default) | light (large-type friendly) | system
    var prefTheme by remember { mutableStateOf("dark") }
    var prefAccent by remember { mutableStateOf("red") }
    var prefPip by remember { mutableStateOf(true) }
    // 2026-11-12 (runtime layer): mini-player options — master switch (default ON),
    // auto-show on play (default ON), swipe-to-dismiss (default ON). Live-apply, no restart.
    var prefMiniEnabled by remember { mutableStateOf(true) }
    var prefMiniAutoshow by remember { mutableStateOf(true) }
    var prefMiniSwipe by remember { mutableStateOf(true) }
    // Audio focus: "all" (pause on any interruption, ExoPlayer handles) vs "calls"
    // (ignore other apps, pause only for phone calls via telephony callback).
    var prefAf by remember { mutableStateOf("all") }
    var callPaused by remember { mutableStateOf(false) }
    fun applyAudioFocus() {
        try {
            val p = player ?: return
            val attrs = androidx.media3.common.AudioAttributes.Builder()
                .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            p.setAudioAttributes(attrs, prefAf != "calls")
        } catch (_: Exception) { }
    }
    // Download manager: the sd behind the current pinConfirm + live quick-pick selection.
    var pinSd by remember { mutableStateOf<com.mohamedadel.mytube.data.youtube.StreamData?>(null) }
    var pinSelQuality by remember { mutableStateOf(QUALITY_AUDIO_ONLY) }
    var pinSelAudio by remember { mutableStateOf(0) }
    var pinSelSubs by remember { mutableStateOf(emptySet<Int>()) } // empty = none
    var pinSelLoc by remember { mutableStateOf("internal") }
    var pinEst by remember { mutableStateOf(-1L) }
    // SponsorBlock: community skip segments (default OFF — video IDs go to a third party).
    var prefSb by remember { mutableStateOf(false) }
    var sbSegments by remember { mutableStateOf(emptyList<SbSeg>()) }
    // UI language: ar (default, RTL) | en (LTR). Applies via recreate().
    var prefUiLang by remember { mutableStateOf("ar") }
    // default video quality for every new video: audio-only | auto | saver | high
    var prefQuality by remember { mutableStateOf("audio-only") }
    // 2026-11-12 (runtime fix): persisted autoplay toggle, default ON (بأمر محمد) —
    // drives radio/queue auto-advance; OFF stops automatic advance. Declared up here
    // (next to the other prefs) because the prefs-load LaunchedEffect below assigns it.
    var prefAutoplay by remember { mutableStateOf(true) }
    // subtitle studio: hide auto-generated, fg/bg/edge/size/bold (applied via CaptionStyleCompat)
    var prefSubHideAuto by remember { mutableStateOf(false) }
    var prefSubFg by remember { mutableStateOf("white") }
    var prefSubBg by remember { mutableStateOf("transparent") }
    var prefSubEdge by remember { mutableStateOf("outline") }
    var prefSubScale by remember { mutableStateOf(1.0f) }
    var prefSubBgOpacity by remember { mutableStateOf(80) }
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
    // Deferred item 8 (2026-11-11): periodic feed refresh toggle (WorkManager, default OFF).
    var prefFeedAuto by remember { mutableStateOf(false) }
    // M1: next-item stream prefetch (latency only — no media bytes; one extractor fetch).
    // Skipped when offline (pins need no resolve) and capped at 2 entries.
    var prefPreloadNext by remember { mutableStateOf(true) }
    // R5: prefetch on WiFi only (default ON, as promised) — mobile data never prefetches.
    var prefPreloadWifiOnly by remember { mutableStateOf(true) }
    // M3: title-translation toggle (default ON); OFF = original titles always, zero Google calls.
    var prefTitleTranslate by remember { mutableStateOf(true) }
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
    // SoundCloud tab (separate): own query/results/loading, reuses SearchScreen + SearchRow.
    var showSC by remember { mutableStateOf(false) }
    var scQuery by remember { mutableStateOf("") }
    var scResults by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.youtube.VideoItem>()) }
    var scLoading by remember { mutableStateOf(false) }
    var scError by remember { mutableStateOf<String?>(null) }
    // Remote playlists (service playlists, not local Room): cards + openable contents view.
    var remotePlaylists by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.youtube.RemotePlaylist>()) }
    var openRemote by remember { mutableStateOf<com.mohamedadel.mytube.data.youtube.RemotePlaylist?>(null) }
    var openRemoteItems by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.youtube.VideoItem>()) }
    var openRemoteLoading by remember { mutableStateOf(false) }

    // Music-tab source: 0 = SoundCloud, 1 = YouTube Music (native music_songs filter).
    // screens-fix Music P1-M5: survive rotation (was remember).
    var scSource by rememberSaveable { mutableIntStateOf(0) }
    // Music-tab YTM kind filter (SoundCloud source stays unfiltered — its extractor has one list).
    var scKind by rememberSaveable { mutableStateOf("songs") } // songs | albums | artists | playlists
    // YTM tabs paging mirrors the main search list (auto-append near the end); history in prefs (on-device only).
    var scNextPage by remember { mutableStateOf<org.schabi.newpipe.extractor.Page?>(null) }
    var scHasMore by remember { mutableStateOf(false) }
    var scLoadingMore by remember { mutableStateOf(false) }
    // screens-fix Music P1-M7: search-correction mirror of the main search screen.
    var scSuggestion by remember { mutableStateOf<String?>(null) }
    var scCorrected by remember { mutableStateOf(false) }
    // screens-fix Music P1-M5: taste TTL (5 min) — refreshTaste skips network while fresh.
    var tasteFetchedAt by remember { mutableStateOf(0L) }
    // screens-fix Library P2-L12: most-replayed rail data (loaded when the hub opens).
    var topPlayedHome by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.local.PlayedStat>()) }
    val scListState = androidx.compose.foundation.lazy.rememberLazyListState()
    var recentMusicSearches by remember { mutableStateOf(listOf<String>()) }
    // Local-profile recommendations (no account): quick picks + one YTM mix shelf.
    var tasteProfile by remember { mutableStateOf<com.mohamedadel.mytube.util.TasteProfile?>(null) }
    var tasteMix by remember { mutableStateOf(emptyList<VideoItem>()) }
    var tasteMixTerm by remember { mutableStateOf<String?>(null) }
    var tasteMixLoading by remember { mutableStateOf(false) }
    var trendTab by remember { mutableStateOf(0) }
    var trendingItems by remember { mutableStateOf(emptyList<VideoItem>()) }
    var trendingLoading by remember { mutableStateOf(false) }
    // screens-fix 2026-11-11 Home P0-H2: trending error state for retry UI.
    var trendingError by remember { mutableStateOf<String?>(null) }
    // screens-fix 2026-11-11 Home P1-H5: cancellable suggestions job.
    var suggestJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    // Deferred item 1 (2026-11-11): trending pagination — kiosk next-page + news search page.
    var trendingHasMore by remember { mutableStateOf(false) }
    var trendingLoadingMore by remember { mutableStateOf(false) }
    var trendingNewsNext by remember { mutableStateOf<Page?>(null) }
    var trendingNewsQuery by remember { mutableStateOf<String?>(null) }
    var openPlaylistId by remember { mutableStateOf<Long?>(null) }
    val openItems by remember(openPlaylistId) {
        openPlaylistId?.let { dao.observeItems(it) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    // Sleep timer: absolute deadline (uptime ms). The polling loop below watches it;
    // expiry pauses playback and clears itself. Survives nothing — a deliberate session choice.
    var sleepAtMs by remember { mutableStateOf<Long?>(null) }
    var sleepLeft by remember { mutableStateOf("") }
    var showSleep by remember { mutableStateOf(false) }
    // end-of-track mode: stop when the current track ends instead of after N minutes
    var sleepEndOfTrack by remember { mutableStateOf(false) }
    // F12 (audit P2 2026-11-11): the armed track's key — a stale flag must never stop a future track.
    var sleepTrackKey by remember { mutableStateOf<String?>(null) }
    var showQueue by remember { mutableStateOf(false) }
    var pickFor by remember { mutableStateOf<VideoItem?>(null) }
    var channelDetail by remember { mutableStateOf<com.mohamedadel.mytube.data.youtube.ChannelDetail?>(null) }

    var commentList by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.youtube.CommentItem>()) }
    var showFeed by remember { mutableStateOf(false) }
    var feedItems by remember { mutableStateOf(emptyList<VideoItem>()) }
    // Low: splash waits for player readiness or first content (cap 3s), not a fixed timer.
    LaunchedEffect(Unit) {
        val t0 = android.os.SystemClock.uptimeMillis()
        while ((player == null && results.isEmpty() && feedItems.isEmpty()) &&
               android.os.SystemClock.uptimeMillis() - t0 < 3000) {
            kotlinx.coroutines.delay(100)
        }
        showSplash = false
    }
    var feedLoading by remember { mutableStateOf(false) }
    var showComments by remember { mutableStateOf(false) }
    var commentsLoading by remember { mutableStateOf(false) }

    var optionsFor by remember { mutableStateOf<VideoItem?>(null) }
    // Phase 3: offline pins (faithful snapshot: current quality + audio + subtitle)
    val pins by remember { dao.observePins() }.collectAsStateWithLifecycle(initialValue = emptyList())
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
    // F5: in-flight download jobs for cancellation (Job.cancel + PinManager flag).
    // (PinRetry hoisted to top level — see above.)
    val pinJobs = remember { java.util.concurrent.ConcurrentHashMap<String, kotlinx.coroutines.Job>() }
    var pinProgress by remember { mutableStateOf(mapOf<String, Float>()) }
    var showDownloads by remember { mutableStateOf(false) }
    // notif 2026-11-12: bell screen state + live badge count (runtime layer).
    var showNotifications by remember { mutableStateOf(false) }
    val notifications by remember { dao.observeNotifications() }.collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadCount by remember { dao.observeUnreadCount() }.collectAsStateWithLifecycle(initialValue = 0)
    var prefKeepalive by remember { mutableStateOf(false) }
    var prefAgeMode by remember { mutableStateOf("block") } // block | warn | allow
    var prefShowComments by remember { mutableStateOf(true) }
    var prefShowRelated by remember { mutableStateOf(true) }
    var prefShowInfo by remember { mutableStateOf(true) }
    var prefShowSubs by remember { mutableStateOf(true) }
    var prefAllow4k by remember { mutableStateOf(true) }
    // warn-consent carries the resolved stream: confirming resumes playback with ZERO re-extraction.
    var agePending by remember { mutableStateOf<Triple<VideoItem, Boolean, com.mohamedadel.mytube.data.youtube.StreamData?>?>(null) }
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
    // L1 (audit P1 2026-11-11): session-restore targets — declared before the prefs-load
    // effect below (Kotlin locals must precede use); saveSession itself lives near setShuffle.
    var pendingSeekMs by remember { mutableStateOf<Long?>(null) }
    var pendingSeekKey by remember { mutableStateOf<String?>(null) }
    var lastSessSave by remember { mutableStateOf(0L) }
    // Simple equalizer state (framework effect; hidden on unsupported devices).
    var eqEnabled by remember { mutableStateOf(false) }
    var eqPreset by remember { mutableStateOf(0) } // index, or EqManager.CUSTOM
    var eqSupported by remember { mutableStateOf(false) }
    var eqBands by remember { mutableStateOf(0) }
    var eqLabels by remember { mutableStateOf(emptyList<String>()) }
    var eqPresetNames by remember { mutableStateOf(emptyList<String>()) }
    val eqCustom = remember { androidx.compose.runtime.mutableStateListOf<Float>() }
    fun fmtFreq(hz: Int): String = if (hz <= 0) "?" else if (hz < 1000) "$hz Hz" else "${hz / 1000}.${(hz % 1000) / 100} kHz"
    fun applyEqPrefs() {
        try {
            val E = com.mohamedadel.mytube.playback.EqManager
            if (!E.supported) { eqSupported = false; return }
            val n = E.bandCount()
            if (n <= 0) { eqSupported = false; return }
            eqSupported = true
            if (eqBands != n) {
                eqBands = n
                eqLabels = (0 until n).map { fmtFreq(E.centerFreqHz(it)) }
                eqPresetNames = E.presets()
                if (eqCustom.size != n) {
                    val saved = try { prefs.getString("eq_custom", "") ?: "" } catch (_: Exception) { "" }.split(",").mapNotNull { it.toFloatOrNull() }
                    eqCustom.clear()
                    for (b in 0 until n) eqCustom.add(saved.getOrNull(b)?.coerceIn(0f, 1f) ?: E.toSlider(E.bandLevel(b)))
                }
            }
            if (eqPreset >= 0) E.usePreset(eqPreset) else for (b in 0 until n) E.setBand(b, E.toMillibels(eqCustom.getOrNull(b) ?: 0.5f))
            E.setEnabled(eqEnabled)
        } catch (_: Exception) { }
    }



    // persisted settings + recents (survive app restarts)
    LaunchedEffect(Unit) {
        try {
            prefSubLang = prefs.getString("pref_sub_lang", "auto") ?: "auto"
            prefAudioLang = prefs.getString("pref_audio_lang", "auto") ?: "auto"
            prefSubLang2 = prefs.getString("pref_sub_lang2", "en") ?: "en"
            prefAudioLang2 = prefs.getString("pref_audio_lang2", "original") ?: "original"
            prefCountry = prefs.getString("pref_country", "EG") ?: "EG"
            try { com.mohamedadel.mytube.data.youtube.YouTubeRepo.contentLang = prefs.getString("pref_ui_lang", "ar") ?: "ar" } catch (_: Exception) { }
            try { com.mohamedadel.mytube.data.youtube.YouTubeRepo.applyRegion(prefCountry) } catch (_: Exception) { }
            prefTheme = prefs.getString("pref_theme", "dark") ?: "dark"
            prefPip = try { prefs.getBoolean("pref_pip", true) } catch (_: Exception) { true }
            prefMiniEnabled = try { prefs.getBoolean("pref_mini_enabled", true) } catch (_: Exception) { true }
            prefMiniAutoshow = try { prefs.getBoolean("pref_mini_autoshow", true) } catch (_: Exception) { true }
            prefMiniSwipe = try { prefs.getBoolean("pref_mini_swipe", true) } catch (_: Exception) { true }
            prefAf = try { prefs.getString("pref_audiofocus", "all") ?: "all" } catch (_: Exception) { "all" }
            prefSb = try { prefs.getBoolean("pref_sponsorblock", false) } catch (_: Exception) { false }
            prefAccent = prefs.getString("pref_accent", "red") ?: "red"
            eqEnabled = try { prefs.getBoolean("eq_enabled", false) } catch (_: Exception) { false }
            eqPreset = try { prefs.getInt("eq_preset", 0) } catch (_: Exception) { 0 }
            prefUiLang = prefs.getString("pref_ui_lang", "ar") ?: "ar"
            prefQuality = prefs.getString("pref_quality", "audio-only") ?: "audio-only"
            // 2026-11-12 (runtime fix): autoplay persisted, default ON (بأمر محمد).
            prefAutoplay = try { prefs.getBoolean("pref_autoplay", true) } catch (_: Exception) { true }
            prefContentFilter = prefs.getString("pref_content_filter", "all") ?: "all"
            prefSubHideAuto = prefs.getBoolean("pref_sub_hide_auto", false)
            prefSubFg = prefs.getString("pref_sub_fg", "white") ?: "white"
            prefSubBg = prefs.getString("pref_sub_bg", "transparent") ?: "transparent"
            prefSubEdge = prefs.getString("pref_sub_edge", "outline") ?: "outline"
            // size migration: legacy s/m/l/xl codes -> scale float (one-time, then pref_sub_scale rules).
            prefSubScale = try {
                if (prefs.contains("pref_sub_scale")) prefs.getFloat("pref_sub_scale", 1.0f).coerceIn(0.5f, 2.0f)
                else when (prefs.getString("pref_sub_size", "m")) { "s" -> 0.8f; "l" -> 1.25f; "xl" -> 1.5f; else -> 1.0f }
            } catch (_: Exception) { 1.0f }
            prefSubBgOpacity = try { prefs.getInt("pref_sub_bg_opacity", 80).coerceIn(0, 100) } catch (_: Exception) { 80 }
            prefSubBold = prefs.getBoolean("pref_sub_bold", false)
            prefPreferOriginal = prefs.getBoolean("pref_prefer_original", false)
            prefSubRole = prefs.getString("pref_sub_role", "subtitle") ?: "subtitle"
            prefHideForced = prefs.getBoolean("pref_hide_forced", false)
            prefUndetermined = prefs.getBoolean("pref_undetermined", true)
            prefAudioRole = prefs.getString("pref_audio_role", "auto") ?: "auto"
            prefSkipSilence = prefs.getBoolean("pref_skip_silence", false)
            prefCacheMaxMb = prefs.getLong("pref_cache_max_mb", 500L).takeIf { it > 0 } ?: 500L
            prefOffline = prefs.getBoolean("pref_offline", false)
            prefKeepalive = prefs.getBoolean("pref_keepalive", false)
            prefAgeMode = prefs.getString("pref_age_mode", "block") ?: "block"
            prefShowComments = prefs.getBoolean("pref_show_comments", true)
            prefShowRelated = prefs.getBoolean("pref_show_related", true)
            prefShowInfo = prefs.getBoolean("pref_show_info", true)
            prefShowSubs = prefs.getBoolean("pref_show_subtitles", true)
            prefAllow4k = prefs.getBoolean("pref_allow_4k", true)
            try { com.mohamedadel.mytube.playback.KeepAliveService.sync(ctx.applicationContext) } catch (_: Exception) { }
            prefPinWarnMb = prefs.getLong("pref_pin_warn_mb", 1024L)
            prefFeedParallel = prefs.getBoolean("pref_feed_parallel", true)
            prefFeedCache = prefs.getBoolean("pref_feed_cache", true)
            prefFeedAuto = try { prefs.getBoolean("pref_feed_auto", false) } catch (_: Exception) { false }
            prefPreloadNext = prefs.getBoolean("pref_preload_next", true)
            prefPreloadWifiOnly = prefs.getBoolean("pref_preload_wifi_only", true)
            prefTitleTranslate = prefs.getBoolean("pref_title_translate", true)
            // cap only (no disk I/O); the cache itself warms on a background thread after scope exists
            try { com.mohamedadel.mytube.playback.MediaCache.setMax(prefCacheMaxMb * 1024 * 1024) } catch (_: Exception) { }
            prefSpeed = prefs.getFloat("pref_speed", 1.0f)
            prefPitch = prefs.getFloat("pref_pitch", 1.0f)
        } catch (_: Exception) { }
        try {
            val raw = prefs.getString("recent_searches", "") ?: ""
            if (raw.isNotBlank()) recentSearches = raw.split("\u001f").filter { it.isNotBlank() }.take(6)
            // sleep timer is playback-bound and persisted: leaving/killing the app must not cancel it
            try {
                val dl = prefs.getLong("sleep_deadline", 0L)
                if (dl > android.os.SystemClock.uptimeMillis() + 30000L) {
                    sleepAtMs = dl
                    // F1: re-arm the service-side deadline after process restart.
                    try { PlaybackService.scheduleSleep(dl - android.os.SystemClock.uptimeMillis()) } catch (_: Exception) { }
                } else if (dl > 0L) {
                    try { prefs.edit().remove("sleep_deadline").apply() } catch (_: Exception) { }
                }
            } catch (_: Exception) { }
            try { prefs.getString("recent_music_searches", "")?.let { m -> if (m.isNotBlank()) recentMusicSearches = m.split("\u001f").filter { it.isNotBlank() }.take(6) } } catch (_: Exception) { }
            // L1 (audit P1 2026-11-11): restore last session's queue (no autoplay; position seeks on tap-play).
            try {
                val favs = (prefs.getString("sess_favs", "") ?: "").split("\u001f")
                val urls = (prefs.getString("sess_urls", "") ?: "").split("\u001f")
                val titles = (prefs.getString("sess_titles", "") ?: "").split("\u001f")
                val ids = (prefs.getString("sess_ids", "") ?: "").split("\u001f")
                val idx = prefs.getInt("sess_index", -1)
                val pos = prefs.getLong("sess_pos", 0L)
                if (favs.isNotEmpty() && favs.size == urls.size && idx in favs.indices) {
                    queue = favs.indices.map { i -> VideoItem(id = ids.getOrNull(i)?.ifBlank { null }, title = titles.getOrNull(i), url = urls.getOrNull(i)?.ifBlank { null }) }
                    queueIndex = idx
                    if (pos > 5000L) { pendingSeekMs = pos; pendingSeekKey = queue[idx].favKey() }
                    playerExpanded = false
                    try { android.widget.Toast.makeText(ctx, ctx.getString(R.string.sess_restored), android.widget.Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
                }
            } catch (_: Exception) { }
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
            // F8 (audit P1 2026-11-11): the user's explicit per-video pick beats language prefs.
            // Guarded by subTrackFor so a fresh video (auto-matched index) never inherits a stale override.
            if (subTrackIdx >= 0 && prefSubLang != "off" && subTrackFor == playing?.favKey()) {
                try {
                    val groups = p.currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
                    val flat = groups.flatMap { g -> (0 until g.length).map { idx -> g to idx } }
                    flat.getOrNull(subTrackIdx)?.let { (g, idx) ->
                        b.setOverrideForType(TrackSelectionOverride(g.mediaTrackGroup, idx))
                    }
                } catch (_: Exception) { }
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
    // Multi-subtitle default: the primary match PLUS the secondary-language match (Arabic + English
    // out of the box when both exist) — the user can still toggle any track in the dialog.
    fun matchSubIdxsFor(sd: com.mohamedadel.mytube.data.youtube.StreamData?): Set<Int> {
        if (prefSubLang == "off") return emptySet()
        val subs = sd?.subtitles ?: return emptySet()
        if (subs.isEmpty()) return emptySet()
        val device = try { java.util.Locale.getDefault().language } catch (_: Exception) { null }
        val ordered = (listOf(resolveLang(prefSubLang), resolveLang(prefSubLang2)) + listOf(device, "en")).filterNotNull().distinct()
        val pool = if (prefSubHideAuto) subs.filter { !it.auto } else subs
        val out = linkedSetOf<Int>()
        for (want in ordered) {
            if (want == "original") break // "original": keep the player's own default
            var i = pool.indexOfFirst { !it.auto && it.langTag.startsWith(want, ignoreCase = true) }
            if (i < 0) i = pool.indexOfFirst { it.langTag.startsWith(want, ignoreCase = true) }
            if (i >= 0) out.add(subs.indexOf(pool[i]))
            if (out.size >= 2) break
        }
        return out
    }
    LaunchedEffect(streamData, prefAudioLang, prefAudioLang2) {
        // backup only: playVideo sets both indices synchronously after resolve (no race).
        // Pinned playback keeps its packaged tracks — never auto-rematch those.
        if (!playingPinned && audioTrackIdx == 0) audioTrackIdx = pickAudioIdx()
    }
    LaunchedEffect(streamData, prefSubLang, prefSubLang2) {
        // backup only (same reason); off stays off; pinned keeps sub 0.
        if (!playingPinned) subTrackIdx = matchSubIdx()
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
    // L1 (audit P1 2026-11-11): minimal session — queue (favKey+url+title+id) + index + position.
    // Saved throttled while loaded; restored on launch (tap play to resume at position).
    fun saveSession() {
        try {
            val q = queue.take(50)
            if (q.isEmpty() || queueIndex !in q.indices) return
            prefs.edit()
                .putString("sess_favs", q.joinToString("\u001f") { it.favKey() })
                .putString("sess_urls", q.joinToString("\u001f") { it.url ?: "" })
                .putString("sess_titles", q.joinToString("\u001f") { (it.title ?: "").replace("\u001f", " ") })
                .putString("sess_ids", q.joinToString("\u001f") { it.id ?: "" })
                .putInt("sess_index", queueIndex)
                .putLong("sess_pos", playerPositionMs)
                .apply()
        } catch (_: Exception) { }
    }
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
            if (event == Lifecycle.Event.ON_RESUME) { ensurePermissions(); ensureBatteryExemption(); if (!com.mohamedadel.mytube.playback.CastManager.available.value) { try { com.mohamedadel.mytube.playback.CastManager.init(ctx.applicationContext) } catch (_: Exception) { } }
                // L4 (audit P1 2026-11-11): ACTION_STOP writes the pref directly — re-read on resume or the toggle lies.
                try { prefKeepalive = prefs.getBoolean("pref_keepalive", false) } catch (_: Exception) { }
                // G-A1: a parked SEND/VIEW intent (cold start or share-while-alive) gets consumed below.
                try { if (MainActivity.pendingSharedIntent != null) sharedPending++ } catch (_: Exception) { } }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        // no remove needed for composition lifetime
    }
    val scope = rememberCoroutineScope()
    fun openRemotePlaylist(p: com.mohamedadel.mytube.data.youtube.RemotePlaylist) {
        openRemote = p
        openRemoteItems = emptyList()
        openRemoteLoading = true
        scope.launch {
            try {
                openRemoteItems = com.mohamedadel.mytube.data.youtube.YouTubeRepo.fetchPlaylist(p.url ?: return@launch)
            } catch (_: Exception) { openRemoteItems = emptyList() }
            finally { openRemoteLoading = false }
        }
    }
    // Title localization: displayed title follows the video's FIRST subtitle language.
    var translatedTitle by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(playing?.url, streamData?.subtitles, prefTitleTranslate) {
        translatedTitle = null
        // M3: OFF = original titles, no network. ON = translate to first-subtitle language.
        if (prefTitleTranslate) {
            try {
                val sd = streamData
                val target = sd?.subtitles?.firstOrNull()?.langTag
                val t = playing?.title
                if (!t.isNullOrBlank() && !target.isNullOrBlank()) {
                    translatedTitle = YouTubeRepo.localizedTitle(t, target)?.takeIf { it != t }
                }
            } catch (_: Exception) { }
        }
    }
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
    // member counts per group (groups are few; one small query each on change)
    fun refreshGroupCounts(gs: List<com.mohamedadel.mytube.data.local.ChannelGroupEntity>) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val m = gs.associate { g ->
                    g.id to try { dao.memberCount(g.id) } catch (_: Exception) { 0 }
                }
                groupCounts = m
            } catch (_: Exception) { }
        }
    }
    fun createGroup(name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try { dao.insertGroup(com.mohamedadel.mytube.data.local.ChannelGroupEntity(name = n)) } catch (_: Exception) { }
        }
    }
    fun renameGroup(g: com.mohamedadel.mytube.data.local.ChannelGroupEntity, name: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try { dao.renameGroup(g.id, n) } catch (_: Exception) { }
        }
    }
    fun deleteGroup(g: com.mohamedadel.mytube.data.local.ChannelGroupEntity) {
        if (openGroup?.id == g.id) openGroup = null
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try { dao.deleteGroup(g.id) } catch (_: Exception) { }
        }
    }
    fun addToGroup(gid: Long, url: String) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // no UNIQUE(groupId, channelUrl) (would need a v9 migration): check-then-add
                // also kills the rapid-double-tap duplicate.
                val exists = try { dao.memberUrls(gid).contains(url) } catch (_: Exception) { true }
                if (!exists) dao.addMember(com.mohamedadel.mytube.data.local.ChannelGroupMemberEntity(groupId = gid, channelUrl = url))
            } catch (_: Exception) { }
        }
    }
    fun removeFromGroup(gid: Long, url: String) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try { dao.removeMember(gid, url) } catch (_: Exception) { }
        }
    }
    fun loadChannelTab(i: Int, url: String) {
        if (channelTabsLoaded.contains(i)) return
        channelTabsLoaded = channelTabsLoaded + i
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                when (i) {
                    1 -> { channelShortsLoading = true; channelShorts = YouTubeRepo.channelTab(url, listOf("short")) }
                    2 -> { channelLiveLoading = true; channelLive = YouTubeRepo.channelTab(url, listOf("live", "stream")) }
                    3 -> { channelPlsLoading = true; channelPls = YouTubeRepo.channelPlaylists(url) }
                }
            } catch (_: Exception) { } finally {
                channelShortsLoading = false; channelLiveLoading = false; channelPlsLoading = false
            }
        }
    }
    fun openChannel(url: String?) {
        if (url.isNullOrBlank()) return
        try { playerExpanded = false } catch (_: Exception) { }
        channelScreenUrl = url
        channelDetail = null
        channelScreenVideos = emptyList()
        channelScreenLoading = true
        channelShorts = emptyList(); channelLive = emptyList(); channelPls = emptyList()
        channelTabsLoaded = emptySet()
        scope.launch {
            // Gate-3: silent failure left an empty channel screen with no message — now surfaced.
            try { channelDetail = YouTubeRepo.channelDetail(url) } catch (_: Exception) { }
            try { channelScreenVideos = YouTubeRepo.channelVideos(url) } catch (_: Exception) { }
            if (channelDetail == null && channelScreenVideos.isEmpty()) {
                try { android.widget.Toast.makeText(ctx, ctx.getString(R.string.err_channel_load), android.widget.Toast.LENGTH_SHORT).show() } catch (_: Exception) { }
            }
            channelScreenLoading = false
        }
    }
    // SimpleCache init does disk I/O — warm it off the main thread (skill Rule 5)
    LaunchedEffect(Unit) {
        try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { com.mohamedadel.mytube.playback.MediaCache.warmAsync(ctx.applicationContext) } } catch (_: Exception) { }
    }
    androidx.compose.runtime.LaunchedEffect(groups) { refreshGroupCounts(groups) }
    // On-device audio (MediaStore): tracks, folder filter/exclusion, scan mode.
    var showLocal by remember { mutableStateOf(false) }
    var localTracks by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.util.LocalTrack>()) }
    var localFolder by remember { mutableStateOf<String?>(null) }
    var localExcl by remember { mutableStateOf(setOf<String>()) }
    var localCompat by remember { mutableStateOf(false) }
    var localHasPerm by remember { mutableStateOf(false) }
    fun localAudioPerm(): String = if (android.os.Build.VERSION.SDK_INT >= 33)
        android.Manifest.permission.READ_MEDIA_AUDIO else android.Manifest.permission.READ_EXTERNAL_STORAGE
    fun checkLocalPerm() {
        try {
            localHasPerm = ctx.checkSelfPermission(localAudioPerm()) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (_: Exception) { localHasPerm = false }
    }
    fun refreshLocalAudio() {
        checkLocalPerm()
        if (!localHasPerm) return
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val excl = try { prefs.getStringSet("local_excl", emptySet()) ?: emptySet() } catch (_: Exception) { emptySet() }
                localExcl = excl
                localCompat = try { prefs.getBoolean("local_scan_compat", false) } catch (_: Exception) { false }
                val all = try { ctx.scanLocalAudio(localCompat) } catch (_: Exception) { emptyList() }
                localTracks = all.filter { t -> (t.folder ?: "") !in excl }
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_local_fail)) }
        }
    }
    fun excludeLocalFolder(f: String) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val cur = (try { prefs.getStringSet("local_excl", emptySet()) } catch (_: Exception) { emptySet() }) ?: emptySet()
                val next = (cur + f).take(50).toSet()
                prefs.edit().putStringSet("local_excl", next).apply()
                localExcl = next
                localTracks = localTracks.filter { (it.folder ?: "") !in next }
                if (localFolder == f) localFolder = null
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_lib_fail)) }
        }
    }
    fun unexcludeLocalFolder(f: String) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val cur = (try { prefs.getStringSet("local_excl", emptySet()) } catch (_: Exception) { emptySet() }) ?: emptySet()
                val next = cur - f
                prefs.edit().putStringSet("local_excl", next).apply()
                localExcl = next
                refreshLocalAudio()
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_lib_fail)) }
        }
    }
    val localPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        localHasPerm = granted
        if (granted) refreshLocalAudio()
    }
    // playlist artwork covers (first 4 song thumbs per playlist, refreshed on change)
    var plCovers by remember { mutableStateOf(mapOf<Long, List<String>>()) }
    fun refreshPlCovers() {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val m = mutableMapOf<Long, List<String>>()
                for (pl in playlists) {
                    try { m[pl.id] = dao.playlistThumbs(pl.id) } catch (_: Exception) { }
                }
                plCovers = m
            } catch (_: Exception) { }
        }
    }
    androidx.compose.runtime.LaunchedEffect(playlists) { refreshPlCovers() }
    // One-tap favorite toggle (needs scope, so it lives here).
    // Short user feedback for every silent success/failure in favorites/queue/playlists.
    // playlist tools: tab sort + per-playlist song sort (prefs) + video-mode queue flag
    var plTabSort by remember { mutableStateOf("custom") }
    var plSongSort by remember { mutableStateOf("added") }
    var videoModeQueue by remember { mutableStateOf(false) }
    fun loadPlSorts(pid: Long?) {
        try { plTabSort = prefs.getString("pl_tab_sort", "custom") ?: "custom" } catch (_: Exception) { }
        try { plSongSort = if (pid == null) "added" else (prefs.getString("pl_sort_" + pid, "added") ?: "added") } catch (_: Exception) { plSongSort = "added" }
    }
    // S5+S9 (audit P2 2026-11-11): CSV goes through FileProvider + EXTRA_STREAM —
    // a thousand-row list as EXTRA_TEXT would blow the ~1MB Binder budget (TransactionTooLarge).
    fun exportPlaylistCsv(pid: Long) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val rows = dao.itemsOnce(pid)
                if (rows.isEmpty()) { uiToast(ctx.getString(R.string.toast_csv_empty)); return@launch }
                // screens-fix Library P1-L4: video_id column first (round-trips through import).
                val sb = StringBuilder("video_id,title,channel,url,duration_sec\n")
                for (r in rows) {
                    fun q(v: String?): String = "\"" + (v ?: "").replace("\"", "\"\"") + "\""
                    sb.append(q(r.videoId)).append(',').append(q(r.title)).append(',').append(q(r.channel)).append(',')
                        .append(q(r.url)).append(',').append(r.durationSec ?: "").append('\n')
                }
                val dir = java.io.File(ctx.cacheDir, "shared").apply { mkdirs() }
                val f = java.io.File(dir, "mytube_playlist_" + pid + ".csv")
                f.writeText(sb.toString())
                val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "com.mohamedadel.mytube.fileprovider", f)
                val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = android.content.Intent.createChooser(send, try { ctx.getString(R.string.export_csv) } catch (_: Exception) { "CSV" })
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(chooser)
            } catch (_: Exception) {
                // fallback: tiny inline text so an export never hard-fails (may truncate large lists)
                try {
                    val rows = dao.itemsOnce(pid).take(50)
                    val sb = StringBuilder()
                    for (r in rows) sb.append(r.title ?: r.url ?: "").append('\n')
                    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, sb.toString())
                    }
                    val chooser = android.content.Intent.createChooser(send, "CSV")
                    chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    ctx.startActivity(chooser)
                } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_csv_fail)) }
            }
        }
    }
    androidx.compose.runtime.LaunchedEffect(showPlaylists, openPlaylistId) { if (showPlaylists) loadPlSorts(openPlaylistId) }
    // screens-fix Library P1-L5: remote playlist -> local copy (local fun keeps the when-branch small).
    fun saveRemoteList(list: List<VideoItem>) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val rpTitle = try { openRemote?.title } catch (_: Exception) { null }
                val name = ((rpTitle ?: "Remote").take(80) + " (import)")
                val pid = dao.insertPlaylist(com.mohamedadel.mytube.data.local.PlaylistEntity(name = name, emoji = "\uD83C\uDFB5", mode = "music"))
                val rows = list.mapIndexed { i, v ->
                    com.mohamedadel.mytube.data.local.PlaylistItemEntity(
                        playlistId = pid, videoId = v.id, title = v.title, channel = v.channel,
                        thumbnail = v.thumbnail, url = v.url, durationSec = v.durationSec,
                        viewCount = v.viewCount, position = i)
                }
                if (rows.isNotEmpty()) dao.insertItems(rows)
                uiToast(ctx.getString(R.string.toast_csv_imported_fmt, rows.size))
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_playlist_save_fail)) }
        }
    }
    // screens-fix Library P1-L4: CSV import (round-trips our own export; header-aware).
    fun importPlaylistCsv(uri: android.net.Uri) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val text = ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return@launch
                val lines = text.lines().filter { it.isNotBlank() }
                if (lines.size < 2) { uiToast(ctx.getString(R.string.import_invalid)); return@launch }
                fun splitCsv(line: String): List<String> {
                    val out = mutableListOf<String>()
                    val cur = StringBuilder()
                    var inQ = false
                    var i = 0
                    while (i < line.length) {
                        val c = line[i]
                        if (inQ) {
                            if (c == '"') {
                                if (i + 1 < line.length && line[i + 1] == '"') { cur.append('"'); i++ } else inQ = false
                            } else cur.append(c)
                        } else {
                            if (c == '"') inQ = true else if (c == ',') { out.add(cur.toString()); cur.clear() } else cur.append(c)
                        }
                        i++
                    }
                    out.add(cur.toString())
                    return out
                }
                val header = splitCsv(lines[0]).map { it.trim().lowercase() }
                val hasId = header.getOrNull(0) == "video_id"
                val pid = dao.insertPlaylist(
                    com.mohamedadel.mytube.data.local.PlaylistEntity(
                        name = ("CSV " + java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US).format(java.util.Date())).take(80),
                        emoji = "\uD83C\uDFB5", mode = "music")
                )
                var n = 0
                val rows = lines.drop(1).mapNotNull { ln ->
                    try {
                        val c = splitCsv(ln)
                        if (c.all { it.isBlank() }) return@mapNotNull null
                        val (vid, title, channel, url, dur) = if (hasId) {
                            listOf(c.getOrNull(0), c.getOrNull(1), c.getOrNull(2), c.getOrNull(3), c.getOrNull(4))
                        } else {
                            listOf(null, c.getOrNull(0), c.getOrNull(1), c.getOrNull(2), c.getOrNull(3))
                        }
                        if (title.isNullOrBlank() && url.isNullOrBlank()) return@mapNotNull null
                        n++
                        com.mohamedadel.mytube.data.local.PlaylistItemEntity(
                            playlistId = pid, videoId = vid?.ifBlank { null },
                            title = title?.ifBlank { null }, channel = channel?.ifBlank { null },
                            thumbnail = null, url = url?.ifBlank { null },
                            durationSec = dur?.trim()?.toLongOrNull(), viewCount = null, position = n)
                    } catch (_: Exception) { null }
                }
                if (rows.isNotEmpty()) dao.insertItems(rows)
                uiToast(ctx.getString(R.string.toast_csv_imported_fmt, rows.size))
            } catch (_: Exception) { uiToast(ctx.getString(R.string.backup_import_fail)) }
        }
    }
    val csvImportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        importPlaylistCsv(uri)
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
                if (pairs.isEmpty()) {
                    toast(ctx.getString(R.string.import_invalid))
                    return@launch
                }
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
            try { histSearchResults = dao.historySearch("%" + com.mohamedadel.mytube.data.local.escapeLike(q.trim()) + "%") } catch (_: Exception) { }
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
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_hist_fail)) }
        }
    }
    val trending = remember { listOf("music", "tech reviews", "gaming", "news", "podcasts") }
    var suggestionChips by remember { mutableStateOf(mapOf<String, List<String>>()) }
    // Suggestions tab: seeds = your recent searches (else trending fallbacks);
    // chips = real NewPipe suggestionList completions, videos = top hits per seed.
    fun refreshSuggestions(region: String?, myTab: Int) {
        val seeds = (recentSearches.take(3) + trending.take(2)).distinct().take(3)
        if (seeds.isEmpty()) { if (trendTab == myTab) { suggestionChips = emptyMap(); trendingItems = emptyList() }; return }
        suggestJob?.cancel()
        suggestJob = scope.launch {
            try {
                val chips = seeds.associateWith { seed ->
                    try { YouTubeRepo.suggestions(seed).take(4) } catch (_: Exception) { emptyList() }
                }
                val vids = seeds.take(2).flatMap { seed ->
                    try { YouTubeRepo.search(seed, null, region).items.take(6) } catch (_: Exception) { emptyList() }
                }.distinctBy { it.id ?: it.url ?: it.title }.take(18)
                if (trendTab == myTab) { suggestionChips = chips; trendingItems = vids; trendingError = null }
            } catch (_: Exception) { if (trendTab == myTab) { suggestionChips = emptyMap(); trendingItems = emptyList(); trendingError = ctx.getString(R.string.err_kiosk) } }
            finally { if (trendTab == myTab) trendingLoading = false }
        }
    }

    fun refreshTrending() {
        val tab = TREND_TABS.getOrNull(trendTab)
        val kiosk = tab?.second
        val isSearchTab = tab?.third == true
        trendingLoading = true
        // screens-fix Home P1-H5: cancel in-flight suggestions when switching tabs.
        if (kiosk != "suggestions") suggestJob?.cancel()
        trendingError = null
        // Deferred item 1: reset pagination on every fresh load.
        trendingHasMore = false
        trendingNewsNext = null
        trendingNewsQuery = null
        if (kiosk == "suggestions") {
            val region = if (prefCountry == "global") null else prefCountry
            val myTab = trendTab
            refreshSuggestions(region, myTab)
            return
        }
        scope.launch {
            try {
                val region = if (prefCountry == "global") null else prefCountry
                if (isSearchTab) {
                    // "news" has no YouTube kiosk: honest region-scoped search instead of a fake list
                    // Gate-4: news query follows UI language (was hardcoded Arabic for everyone).
                    // Deferred item 1: keep the SearchPage cursor so "show more" pages the search.
                    val nq = if (prefUiLang == "ar") "أخبار" else "news"
                    val page = YouTubeRepo.search(nq, null, region)
                    trendingItems = page.items
                    trendingNewsQuery = nq
                    trendingNewsNext = page.nextPage
                    trendingHasMore = page.hasNext && page.nextPage != null
                } else {
                    trendingItems = YouTubeRepo.trending(region, kiosk)
                    trendingHasMore = trendingItems.isNotEmpty()
                }
                // screens-fix Home P0-H2: empty trending = error surface (was silent).
                trendingError = if (trendingItems.isEmpty()) ctx.getString(R.string.err_kiosk) else null
            } catch (_: Exception) { trendingItems = emptyList(); trendingError = ctx.getString(R.string.err_kiosk) }
            finally { trendingLoading = false }
        }
    }
    // Deferred item 1 (2026-11-11): "show more" — kiosk next-page, or next search page for news.
    fun loadMoreTrending() {
        if (trendingLoadingMore || !trendingHasMore) return
        val tab = TREND_TABS.getOrNull(trendTab)
        val kiosk = tab?.second
        val isSearchTab = tab?.third == true
        trendingLoadingMore = true
        scope.launch {
            try {
                val region = if (prefCountry == "global") null else prefCountry
                if (isSearchTab) {
                    val pg = trendingNewsNext
                    val nq = trendingNewsQuery
                    if (pg == null || nq == null) { trendingHasMore = false; return@launch }
                    val page = YouTubeRepo.search(nq, pg, region)
                    val seen = trendingItems.mapNotNull { it.url ?: it.id }.toSet()
                    trendingItems = trendingItems + page.items.filter { (it.url ?: it.id ?: it.title) !in seen }
                    trendingNewsNext = page.nextPage
                    trendingHasMore = page.hasNext && page.nextPage != null
                } else {
                    val (more, hasMore) = YouTubeRepo.trendingMore(region, kiosk)
                    if (more.isNotEmpty()) {
                        val seen = trendingItems.mapNotNull { it.url ?: it.id }.toSet()
                        trendingItems = trendingItems + more.filter { (it.url ?: it.id ?: it.title) !in seen }
                    }
                    trendingHasMore = hasMore && more.isNotEmpty()
                }
            } catch (_: Exception) { trendingHasMore = false }
            finally { trendingLoadingMore = false }
        }
    }
    LaunchedEffect(prefCountry) { if (results.isEmpty() && query.isBlank()) refreshTrending() }
    // Deferred item 8: apply the periodic worker on startup (toggle changes re-apply it).
    LaunchedEffect(prefFeedAuto) { try { com.mohamedadel.mytube.data.local.FeedRefreshWorker.schedule(ctx.applicationContext, prefFeedAuto) } catch (_: Exception) { } }
    val svm: SearchViewModel = viewModel()
    val suggestions by svm.suggestions.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Policy: default quality is audio-only; NEVER auto-play. Playback starts only via explicit user tap on play.
    // Gate-1: URL resolution extracted from applyQuality — null = unavailable (caller surfaces the error).
    // Returns (playback url, is it a muxed file, user-picked audio url or null).
    fun resolvePlaybackUrl(q: String, s: StreamData): Triple<String, Boolean, String?>? {
        val selAudio = s.audioTracks.getOrNull(audioTrackIdx)?.url ?: s.audioLowestUrl
        val maxH = if (prefAllow4k) Int.MAX_VALUE else 1080
        val capped = if (maxH == Int.MAX_VALUE) s else try { s.copy(videoUrlByQuality = s.videoUrlByQuality.filterKeys { (qualityHeight(it) ?: 0) <= maxH }) } catch (_: Exception) { s }
        val url = when {
            q == QUALITY_AUDIO_ONLY -> selAudio
            q.startsWith("auto") -> autoUrl(capped, ctx.estimatedDownstreamKbps())
            else -> capped.videoUrlByQuality[q]
        } ?: return null
        val muxedUrls = s.videoUrlByQuality.filter { s.muxedQualities.contains(it.key) }.values.toSet()
        return Triple(url, url in muxedUrls, selAudio)
    }

    fun applyQuality(q: String, s: StreamData, p: ExoPlayer?, autoplay: Boolean = false, forceStart: Boolean = false, local: Boolean = false) {
        if (p == null) return
        val wasPlaying = try { p.isPlaying } catch (_: Exception) { false }
        // user-selected dubbed/original audio track, falling back to lowest
        val (url, isMuxed, selAudio) = resolvePlaybackUrl(q, s) ?: run {
            playerError = ctx.getString(R.string.err_quality_unavailable)
            playerLoading = false
            return
        }
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
        // offline fix 2026-11-11: pinned files carry their recorded container (Cause 2);
        // explicit MIME beats extension sniffing. Online streams pass null = unchanged.
        val mediaItem = MediaItem.Builder().setUri(Uri.parse(url)).setMediaMetadata(meta).setSubtitleConfigurations(subConfigs).apply { try { s.videoMime?.takeIf { it.isNotBlank() }?.let { setMimeType(it) } } catch (_: Exception) { } }.build()
        // Prepare paused; resume only if explicitly requested AND was already playing (quality switch / background switch), or forced (retry).
        val resume = (autoplay && wasPlaying) || forceStart
        // 2026-11-12 (runtime fix): HLS-flagged urls (scStream sets audioHlsOnly from
        // the extractor's own DeliveryMethod, never URL sniffing) go through HlsMediaSource
        // explicitly — extensionless HLS pointers are mis-sniffed as Progressive otherwise.
        // Single tap still prepares paused (no-autoplay policy unchanged).
        val hlsOnly = s.audioHlsOnly
        p.playWhenReady = false
        // Fix: muxed files bake their own audio, so a picked track was silently ignored there.
        // Merged rebuild (muxed video + picked audio) + one-shot disable of the baked audio group.
        val needExternalAudio = isMuxed && selAudio != null && audioTrackIdx != 0 && q != QUALITY_AUDIO_ONLY
        // Shared HTTP stack for explicit sources: browser UA (googlevideo/cf-media 403
        // non-browser agents) + cache-first for online, direct for local/pinned files.
        fun explicitDsf(local: Boolean): androidx.media3.datasource.DataSource.Factory {
            val httpFactory = DefaultHttpDataSource.Factory().setUserAgent(BROWSER_UA)
            return if (local) DefaultDataSource.Factory(ctx, httpFactory) else try {
                com.mohamedadel.mytube.playback.MediaCache.factory(ctx, DefaultDataSource.Factory(ctx, httpFactory))
            } catch (_: Exception) { DefaultDataSource.Factory(ctx, httpFactory) }
        }
        // 2026-11-12 (runtime fix 1): livestreams expose NO progressive audio —
        // only a single adaptive URL (NewPipe: getAudioStreams() is empty for lives).
        // Auto-sniffing via setMediaItem misfires on extensionless googlevideo live
        // pointers, so route explicitly: .mpd → DASH, everything else → HLS
        // (NewPipe hands YouTube lives as HLS). Never cached (stale edge), never pinned
        // (requestPin/quickPin refuse with err_pin_live).
        if (s.isLive || q == "live") {
            p.setMediaSource((if (url.lowercase().contains(".mpd")) DashMediaSource.Factory(explicitDsf(true)) else HlsMediaSource.Factory(explicitDsf(true))).createMediaSource(mediaItem))
            p.prepare()
        } else if (hlsOnly && q == QUALITY_AUDIO_ONLY) {
            p.setMediaSource(HlsMediaSource.Factory(explicitDsf(local)).createMediaSource(mediaItem))
            p.prepare()
        } else if (q == QUALITY_AUDIO_ONLY || selAudio == null || (isMuxed && !needExternalAudio)) {
            p.setMediaItem(mediaItem)
            p.prepare()
        } else {
            val httpFactory = DefaultHttpDataSource.Factory().setUserAgent(BROWSER_UA)
            // Phase 1: cache-first media (replay within URL lifetime = zero bytes).
            // Phase 4: pinned files bypass the cache entirely (no duplicate bytes on disk).
            val dataSourceFactory = if (local) DefaultDataSource.Factory(ctx, httpFactory) else try {
                com.mohamedadel.mytube.playback.MediaCache.factory(ctx, DefaultDataSource.Factory(ctx, httpFactory))
            } catch (_: Exception) { DefaultDataSource.Factory(ctx, httpFactory) }
            val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
            val audioItem = MediaItem.Builder().setUri(Uri.parse(selAudio)).apply { try { s.audioMime?.takeIf { it.isNotBlank() }?.let { setMimeType(it) } } catch (_: Exception) { } }.build()
            val audioSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(audioItem)
            val merged = MergingMediaSource(videoSource, audioSource)
            p.setMediaSource(merged)
            p.prepare()
            try { externalAudioHook[0]?.let { p.removeListener(it) } } catch (_: Exception) { }
            externalAudioHook[0] = null
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
                }).also { externalAudioHook[0] = it })
            }
        }
        p.playWhenReady = resume
        applyLangPrefs() // re-assert caps/roles after every media set (quality switch)
    }

    // Background audio saver: dropping the video track stops video chunks with ZERO rebuffer gap
    // (the old approach re-prepared an audio-only item and audibly hiccuped). PiP keeps video.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val p = player ?: return@LifecycleEventObserver
            if (event == Lifecycle.Event.ON_STOP) {
                val activity = ctx as? android.app.Activity
                val hasVideo = try { p.videoFormat != null } catch (_: Exception) { false }
                if (activity?.isInPictureInPictureMode != true && playing != null && hasVideo) {
                    try { p.trackSelectionParameters = p.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, true).build() } catch (_: Exception) { }
                }
            } else if (event == Lifecycle.Event.ON_START) {
                try { p.trackSelectionParameters = p.trackSelectionParameters.buildUpon().setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, false).build() } catch (_: Exception) { }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Open video WITHOUT auto-play: default quality audio-only, prepared paused. User taps play explicitly.
    // queue=true means this playback belongs to the user-started queue ("play all" / next / prev).
    // expand=false keeps the current collapsed/expanded state (queue advancing while browsing).
    // 4K gate: when off, ladders behave as if >1080p never existed (saves data on small screens).
    fun cap4k(sd: com.mohamedadel.mytube.data.youtube.StreamData): com.mohamedadel.mytube.data.youtube.StreamData =
        if (prefAllow4k) sd else try { sd.copy(videoUrlByQuality = sd.videoUrlByQuality.filterKeys { (qualityHeight(it) ?: 0) <= 1080 }) } catch (_: Exception) { sd }
    fun autoKeyFor(sd0: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): String {
        val sd = cap4k(sd0)
        return autoUrl(sd, kbps)?.let { u ->
            val h = sd.videoUrlByQuality.entries.firstOrNull { it.value == u }?.key
                ?.let { qualityHeight(it) }
            if (h != null && h != Int.MAX_VALUE) "auto \u2022 ${h}p" else "auto"
        } ?: "auto"
    }
    fun qualityEntriesFor(sd0: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): List<String> {
        val sd = cap4k(sd0)
        val autoKey = autoKeyFor(sd, kbps)
        val sorted = sd.videoUrlByQuality.keys.sortedByDescending { qualityHeight(it) ?: 0 }
        return mutableListOf(QUALITY_AUDIO_ONLY, autoKey).also { it.addAll(sorted) }
    }
    // default quality preference (applies to every new video AND to list-context pins)
    fun pickStartQuality(sd0: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): String {
        val sd = cap4k(sd0)
        val autoKey = autoKeyFor(sd, kbps)
        val sorted = sd.videoUrlByQuality.keys.sortedByDescending { qualityHeight(it) ?: 0 }
        return when (prefQuality) {
            "auto" -> autoKey
            "saver" -> sorted.lastOrNull() ?: QUALITY_AUDIO_ONLY
            "high" -> sorted.firstOrNull() ?: autoKey
            else -> QUALITY_AUDIO_ONLY
        }
    }
    // Chromecast: guess the receiver content type from the resolved stream URL.
    // Falls back to audio/mp4 + video/mp4 (progressive — what the extractor hands us).
    fun castMimeFor(url: String, audioOnly: Boolean): String {
        return try {
            val q = android.net.Uri.parse(url).getQueryParameter("mime")?.let {
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            if (!q.isNullOrBlank() && q.contains("/")) q else if (audioOnly) "audio/mp4" else "video/mp4"
        } catch (_: Exception) { if (audioOnly) "audio/mp4" else "video/mp4" }
    }

    // Push the current stream to the Cast receiver at the local position, then pause local.
    // Called on session start and whenever a new video starts while connected.
    fun castCurrent(autoplay: Boolean = true) {
        try {
            val s0 = streamData ?: return
            val s = cap4k(s0)
            val triple = try { resolvePlaybackUrl(quality, s) } catch (_: Exception) { null } ?: return
            val url = triple.first
            val mime = castMimeFor(url, quality == QUALITY_AUDIO_ONLY)
            val pos = try { player?.currentPosition ?: 0L } catch (_: Exception) { 0L }
            val ok = CastManager.load(url, playing?.title ?: s.title, playing?.thumbnail, mime, pos, autoplay)
            if (ok) {
                try { player?.pause() } catch (_: Exception) { }
                toast(ctx.getString(R.string.toast_cast_started))
            }
        } catch (_: Exception) { }
    }

    // Chromecast session wiring: push current media on connect, note the handoff on end.
    LaunchedEffect(Unit) {
        CastManager.onSessionStarted = { castCurrent(autoplay = true) }
        CastManager.onSessionEnded = {
            try { toast(ctx.getString(R.string.toast_cast_ended)) } catch (_: Exception) { }
        }
    }
    // Single taps never expand: the mini player appears (SoundCloud/YTM-phone behavior);
    // tap the mini player to expand. Explicit actions (play-now, queue-when-idle) pass
    // expand = true; queue advances preserve the current state.
    fun playVideo(v: VideoItem, inQueue: Boolean = false, expand: Boolean = false, ageOk: Boolean = false, pre: com.mohamedadel.mytube.data.youtube.StreamData? = null, forcePlay: Boolean = false) {
        // 2026-11-12 (runtime layer): mini-player prefs — a FRESH play (nothing was playing)
        // opens the full player when the mini bar is disabled entirely (master OFF) or when
        // auto-show is OFF, so playback stays reachable; track switches inside a live session
        // keep the current expanded/collapsed state (no yanking on auto-advance).
        val hadPlaying = playing != null
        playing = v
        recordHistory(v)
        if (expand) playerExpanded = true
        else if (!hadPlaying && (!prefMiniEnabled || !prefMiniAutoshow)) playerExpanded = true
        if (!inQueue) {
            // single tap: this video alone is the queue (no implicit follow-up)
            queue = listOf(v)
            autoAdvance = false
            radioOn = false
            videoModeQueue = false
        } else {
            autoAdvance = true
        }
        // F11 (audit P2 2026-11-11): canonical key everywhere — id/url matching let dupes slip the position.
        queueIndex = queue.indexOfFirst { it.favKey() == v.favKey() }
        playerLoading = true
        playerError = null
        playerPositionMs = 0L; playerDurationMs = 0L; playerBufferedPct = 0
        // queue continuity: advancing inside a playing queue resumes; a fresh tap stays paused (no-autoplay policy).
        // 2026-11-12 (runtime fix): at STATE_ENDED isPlaying is already false, so the check
        // below was ALWAYS false on auto-advance — the next radio/queue track loaded paused
        // (radio looked dead). forcePlay=true is passed ONLY from the ENDED path, i.e. inside
        // an already user-started session; cold taps keep forcePlay=false (no autoplay).
        val wasPlayingQueue = (inQueue && try { player?.isPlaying == true } catch (_: Exception) { false }) || forcePlay
        if (!inQueue) try { prefetched.clear() } catch (_: Exception) { }
        try { player?.playWhenReady = false; player?.pause() } catch (_: Exception) { }
        scope.launch {
            val key = v.favKey()
            val app = ctx.applicationContext
            val pinned = try { withContext(kotlinx.coroutines.Dispatchers.IO) { com.mohamedadel.mytube.data.local.PinFiles.isPinned(app, key) } } catch (_: Exception) { false }
            if (prefOffline && !pinned) {
                playerLoading = false
                playerError = ctx.getString(R.string.err_pin_first)
                toast(ctx.getString(R.string.err_pin_first))
                return@launch
            }
            playingPinned = false
            // On-device file (MediaStore content:// or file://): zero-network playback,
            // same local path as pins (no cache, no autoplay change).
            // L9 (audit P2 2026-11-11): explicit local type check (id carries the local: prefix).
            if (v.isLocal()) {
                try {
                    audioTrackIdx = 0
                    subTrackIdx = -1
                    val s = com.mohamedadel.mytube.data.youtube.StreamData(
                        audioLowestUrl = v.url,
                        title = v.title,
                        subtitles = emptyList()
                    )
                    streamData = s
                    qualityList = listOf(QUALITY_AUDIO_ONLY)
                    quality = QUALITY_AUDIO_ONLY
                    applyQuality(QUALITY_AUDIO_ONLY, s, player, autoplay = wasPlayingQueue, local = true)
                    if (CastManager.connected.value) castCurrent(autoplay = true)
                    return@launch
                } catch (_: Exception) {
                    playerLoading = false
                    playerError = ctx.getString(R.string.toast_pin_fail)
                    return@launch
                }
            }
            playingPinned = pinned
            if (pinned) {
                // Phase 4: zero-network playback from the faithful snapshot
                // T1 (audit P1 2026-11-11): disk + JSON snapshot on IO; player work stays on Main.
                val snap = withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val d = com.mohamedadel.mytube.data.local.PinFiles.existingDir(app, key)
                    val vf = if (d != null) File(d, "video.mp4") else null
                    val af = if (d != null) File(d, "audio.m4a") else null
                    val sf = if (d != null) File(d, "sub.vtt") else null
                    val m = try { if (d != null) org.json.JSONObject(File(d, "meta.json").readText()) else org.json.JSONObject() } catch (_: Exception) { org.json.JSONObject() }
                    Triple(d, Triple(vf, af, sf), m)
                }
                try {
                    val dir = snap.first ?: throw IllegalStateException("pin missing")
                    val vFile = snap.second.first ?: throw IllegalStateException("pin missing")
                    val aFile = snap.second.second
                    val sFile = snap.second.third
                    if (!vFile.exists()) throw IllegalStateException("pin missing")
                    val meta = snap.third
                    val pinAge = try { meta.optInt("age", 0) } catch (_: Exception) { 0 }
                    if (pinAge >= 18 && !ageOk) {
                        if (prefAgeMode == "block") {
                            playerLoading = false
                            playerError = ctx.getString(R.string.err_age_blocked)
                            toast(ctx.getString(R.string.err_age_blocked))
                            return@launch
                        } else if (prefAgeMode == "warn") {
                            playerLoading = false
                            // L3 (audit P0 2026-11-11): queue auto-advance must never freeze silently — skip with a toast.
                            // Inline (not playNextInQueue()): local funs declared below are out of scope here.
                            if (inQueue) {
                                toast(ctx.getString(R.string.err_age_blocked))
                                val nxt = queue.getOrNull(queueIndex + 1)
                                if (nxt != null) playVideo(nxt, inQueue = true, expand = playerExpanded)
                                else { radioOn = false; autoAdvance = false }
                                return@launch
                            }
                            agePending = Triple(v, inQueue, null)
                            return@launch
                        }
                    }
                    val label = meta.optString("quality", QUALITY_AUDIO_ONLY).takeIf { it.isNotBlank() } ?: QUALITY_AUDIO_ONLY
                    val hasAudio = aFile?.exists() == true
                    val hasSub = sFile?.exists() == true
                    val vUri = android.net.Uri.fromFile(vFile).toString()
                    // multi-sub meta ("subs" array); legacy pins fall back to the single sub.vtt keys
                    val subs = try {
                        val arr = meta.optJSONArray("subs")
                        if (arr != null && arr.length() > 0) {
                            (0 until arr.length()).mapNotNull { i ->
                                val o = try { arr.getJSONObject(i) } catch (_: Exception) { null } ?: return@mapNotNull null
                                val fn = o.optString("file", "")
                                val f = if (fn.isBlank()) null else File(dir, fn)
                                if (f == null || !f.exists()) return@mapNotNull null
                                com.mohamedadel.mytube.data.youtube.SubtitleItem(
                                    url = android.net.Uri.fromFile(f).toString(),
                                    langTag = o.optString("lang", "und").takeIf { it.isNotBlank() } ?: "und",
                                    label = o.optString("label", "").takeIf { it.isNotBlank() } ?: label,
                                    auto = false,
                                    mime = o.optString("mime", "text/vtt").takeIf { it.isNotBlank() } ?: "text/vtt"
                                )
                            }
                        } else null
                    } catch (_: Exception) { null }
                        ?: if (hasSub) listOf(
                            com.mohamedadel.mytube.data.youtube.SubtitleItem(
                                url = android.net.Uri.fromFile(sFile ?: vFile).toString(),
                                langTag = meta.optString("subLang", "und").takeIf { it.isNotBlank() } ?: "und",
                                label = meta.optString("subTag", "").takeIf { it.isNotBlank() } ?: label,
                                auto = false,
                                mime = meta.optString("subMime", "text/vtt").takeIf { it.isNotBlank() } ?: "text/vtt"
                            )
                        ) else emptyList()
                    // offline fix 2026-11-11: recorded containers (Cause 2) — explicit MIME
                    // frees playback from file-extension sniffing (audio-opus-as-.mp4 etc.).
                    // Legacy pins (no mime keys) fall back to null = old sniffing behavior.
                    fun cleanMime(raw: String?): String? {
                        val m = raw?.trim()?.takeIf { it.contains("/") } ?: return null
                        return if (m.startsWith("audio/") || m.startsWith("video/")) m else null
                    }
                    val s = com.mohamedadel.mytube.data.youtube.StreamData(
                        audioLowestUrl = if (hasAudio && aFile != null) android.net.Uri.fromFile(aFile).toString() else vUri,
                        videoUrlByQuality = mapOf(label to vUri),
                        muxedQualities = if (hasAudio) emptySet() else setOf(label),
                        title = v.title,
                        subtitles = subs,
                        channelUrl = v.channelUrl,
                        videoMime = cleanMime(meta.optString("videoMime", "")),
                        audioMime = cleanMime(meta.optString("audioMime", ""))
                    )
                    streamData = s
                    qualityList = listOf(label)
                    quality = label
                    audioTrackIdx = 0
                    subTrackIdx = if (hasSub) 0 else -1
                    subTrackFor = null // F8: packaged pin track, not an explicit pick
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
            var s = pre ?: try { prefetched.remove(key) } catch (_: Exception) { null }
            if (s != null && s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) s = null
            if (s == null) {
                s = try { YouTubeRepo.streamRouted(v.url ?: return@launch) } catch (_: Exception) { null }
                if (s == null) {
                    playerLoading = false
                    playerError = ctx.getString(R.string.err_no_sources)
                    return@launch
                }
            }
            // total extraction failure used to leave the spinner forever with NO error (silent death).
            // Detect it here and report a smart message instead.
            // 2026-11-12: StreamData.error carries the extractor's own reason — show it
            // so a stale-extractor/403 class is diagnosable on device (was: generic text only).
            if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
                playerLoading = false
                playerError = if (!s.error.isNullOrBlank()) ctx.getString(R.string.err_extract_detail, s.error) else ctx.getString(R.string.err_no_sources)
                return@launch
            }
            // Fresh video = fresh indices, set HERE (the LaunchedEffect on streamData races
            // applyQuality and played the previous video's dub/subtitle — major #4/#5).
            audioTrackIdx = pickAudioIdxFor(s)
            subTrackIdx = matchSubIdxFor(s)
            subTrackFor = null // F8: auto-matched, not an explicit pick
            if (s.ageLimit >= 18 && !ageOk) {
                if (prefAgeMode == "block") {
                    playerLoading = false
                    playerError = ctx.getString(R.string.err_age_blocked)
                    toast(ctx.getString(R.string.err_age_blocked))
                    return@launch
                } else if (prefAgeMode == "warn") {
                    playerLoading = false
                    // L3 (audit P0 2026-11-11): queue auto-advance must never freeze silently — skip with a toast.
                    // Inline (not playNextInQueue()): local funs declared below are out of scope here.
                    if (inQueue) {
                        toast(ctx.getString(R.string.err_age_blocked))
                        val nxt = queue.getOrNull(queueIndex + 1)
                        if (nxt != null) playVideo(nxt, inQueue = true, expand = playerExpanded)
                        else { radioOn = false; autoAdvance = false }
                        return@launch
                    }
                    agePending = Triple(v, inQueue, s)
                    return@launch
                }
            }
            if (s.isLive) {
                // live edge: single URL, no quality ladder, no cache (cache would pin a stale edge)
                qualityList = listOf("live")
                quality = "live"
                streamData = s
                applyQuality("live", s, player, autoplay = wasPlayingQueue, local = true)
                if (CastManager.connected.value) castCurrent(autoplay = true)
                return@launch
            }
            streamData = s
            // label the auto entry with the speed-based target so the user sees what auto will pick
            val kbps = ctx.estimatedDownstreamKbps()
            qualityList = qualityEntriesFor(s, kbps)
            val startQ = if (s.videoUrlByQuality.isEmpty()) QUALITY_AUDIO_ONLY else pickStartQuality(s, kbps)
            // video-mode playlist: never start audio-only (keeps videos out of the music flow)
            quality = if (videoModeQueue && startQ == QUALITY_AUDIO_ONLY && s.videoUrlByQuality.isNotEmpty()) {
                qualityList.firstOrNull { it != QUALITY_AUDIO_ONLY } ?: startQ
            } else startQ
            applyQuality(startQ, s, player, autoplay = wasPlayingQueue)
            if (CastManager.connected.value) castCurrent(autoplay = true)
            // M1: prefetch the next queue item's stream (metadata/subs only — zero media bytes)
            try {
                if (prefPreloadNext && !prefOffline && autoAdvance && prefetched.size < 2 && (!prefPreloadWifiOnly || isUnmeteredNet())) {
                    val nxt = queue.getOrNull(queueIndex + 1)
                    if (nxt != null) {
                        val nk = nxt.favKey()
                        val app2 = ctx.applicationContext
                        if (!com.mohamedadel.mytube.data.local.PinFiles.isPinned(app2, nk) && !prefetched.containsKey(nk) && nxt.url != null && nxt.url?.isLocalAudioUri() != true) {
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    val s2 = YouTubeRepo.streamRouted(nxt.url ?: return@launch) ?: return@launch
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
    fun startRadio(v: VideoItem) {
        scope.launch {
            try {
                // screens-fix Music P0-M2: local audio has no related list — refuse early.
                if (v.isLocal()) { toast(ctx.getString(R.string.toast_radio_fail)); return@launch }
                val sdNow = streamData
                // 2026-11-12 (runtime fix): related may be empty for ANY source (YTM items,
                // SC has none by design, age-gated/region-emptied YT) — fall back to a title
                // search for all sources instead of SC-only, and surface the extractor
                // reason instead of swallowing it (was: generic toast on every failure).
                var seedErr: String? = null
                val seed: List<VideoItem> = if (playing?.favKey() == v.favKey() && !sdNow?.related.isNullOrEmpty()) {
                    // T5: local val instead of !! (race-safe snapshot).
                    sdNow.related
                } else {
                    val url = v.url
                    if (url.isNullOrBlank()) { toast(ctx.getString(R.string.toast_radio_fail)); return@launch }
                    var list: List<VideoItem> = emptyList()
                    if (!url.contains("soundcloud.com")) {
                        try {
                            val s = com.mohamedadel.mytube.data.youtube.YouTubeRepo.streamRouted(url)
                            list = s?.related ?: emptyList()
                            if (list.isEmpty()) seedErr = s?.error
                        } catch (e: Exception) { seedErr = e.message?.take(80) }
                    }
                    if (list.isEmpty()) {
                        val q = v.title?.takeIf { it.isNotBlank() }
                        if (q == null) {
                            if (!seedErr.isNullOrBlank()) toast(ctx.getString(R.string.err_extract_detail, seedErr))
                            else toast(ctx.getString(R.string.toast_radio_fail))
                            return@launch
                        }
                        try { list = com.mohamedadel.mytube.data.youtube.YouTubeRepo.search(q, null, null).items }
                        catch (e: Exception) { if (seedErr == null) seedErr = e.message?.take(80) }
                    }
                    list
                }
                val fresh = seed.filter { it.url?.isNotBlank() == true }.distinctBy { it.favKey() }.take(15)
                if (fresh.isEmpty()) {
                    if (!seedErr.isNullOrBlank()) toast(ctx.getString(R.string.err_extract_detail, seedErr))
                    else toast(ctx.getString(R.string.toast_radio_fail))
                    return@launch
                }
                queue = listOf(v) + fresh.filter { it.favKey() != v.favKey() }
                queueIndex = 0
                radioOn = true
                videoModeQueue = false
                toast(ctx.getString(R.string.toast_radio_started))
                playVideo(v, inQueue = true)
            } catch (_: Exception) { toast(ctx.getString(R.string.toast_radio_fail)) }
        }
    }
    // T6 (audit P1 2026-11-11): cancellable fade — a second call cancels the first (no stuck low volume).
    // Plain holder (no state): no composable reads this; state would recompose pointlessly.
    val fadeJob = remember { arrayOfNulls<kotlinx.coroutines.Job>(1) }
    // 5-second fade-out instead of a hard cut; volume is restored after the pause
    fun fadeOutAndPause() {
        try { fadeJob[0]?.cancel() } catch (_: Exception) { }
        fadeJob[0] = scope.launch {
            try {
                val pl = player ?: return@launch
                // T5/T10: safe cast instead of (as Float).
                val base = try { (volumeLive.value as? Float)?.coerceIn(0f, 1f) ?: 1f } catch (_: Exception) { 1f }
                if (base <= 0f) { try { pl.pause() } catch (_: Exception) { }; return@launch }
                repeat(10) { i ->
                    try { pl.volume = base * (1f - (i + 1) / 10f) } catch (_: Exception) { return@repeat }
                    try { kotlinx.coroutines.delay(500) } catch (_: Exception) { return@repeat }
                }
                try { pl.pause() } catch (_: Exception) { }
                try { pl.volume = base } catch (_: Exception) { }
            } catch (_: Exception) { try { player?.pause() } catch (_: Exception) { } }
        }
    }
    fun playPrevInQueue() {
        // T4 (audit P1): pure index math lives in PlayerController.
        val prev = com.mohamedadel.mytube.playback.prevSequentialIndex(queueIndex)?.let { queue.getOrNull(it) }
        if (prev != null) playVideo(prev, inQueue = true, expand = playerExpanded)
    }
    // F7 (audit P1 2026-11-11): shuffled play order as favKeys (head = next to play).
    // Both auto-advance and manual Next consume it; rebuilt when empty/stale so queue
    // changes (radio refill) can't resurrect removed items. No immediate repeat of current.
    var shuffleOrder by remember { mutableStateOf(listOf<String>()) }
    fun ensureShuffleOrder(): List<String> {
        val keys = queue.map { it.favKey() }
        val cur = playing?.favKey()
        val valid = shuffleOrder.filter { it in keys && it != cur }
        if (valid.isNotEmpty()) { shuffleOrder = valid; return valid }
        val fresh = com.mohamedadel.mytube.playback.shuffledFavKeys(keys, cur)
        shuffleOrder = fresh
        return fresh
    }
    // playback modes: random pick (shuffle) + wrap-around (repeat all)
    // 2026-11-12: forcePlay=true only from the STATE_ENDED path (session keeps sounding).
    fun playRandomInQueue(forcePlay: Boolean = false) {
        if (queue.size < 2) { if (queue.isNotEmpty()) playVideo(queue[0], inQueue = true, expand = playerExpanded, forcePlay = forcePlay); return }
        val order = ensureShuffleOrder()
        val nxt = order.firstNotNullOfOrNull { k -> queue.firstOrNull { it.favKey() == k } }
        if (nxt == null) {
            // stale order despite refresh: legacy uniform pick (never strand playback).
            var n = queueIndex
            var guard = 0
            while (n == queueIndex && guard++ < 8) n = (0 until queue.size).random()
            playVideo(queue[n], inQueue = true, expand = playerExpanded, forcePlay = forcePlay)
            return
        }
        shuffleOrder = order.filter { it != nxt.favKey() }
        playVideo(nxt, inQueue = true, expand = playerExpanded, forcePlay = forcePlay)
    }
    fun playNextInQueue(forcePlay: Boolean = false) {
        // F7 (audit P1 2026-11-11): manual Next honors shuffle (was: sequential always).
        // Declared after playRandomInQueue: Kotlin locals must precede use.
        if (shuffleOn && queue.size > 1) { playRandomInQueue(forcePlay); return }
        // T4 (audit P1): pure index math lives in PlayerController.
        val next = com.mohamedadel.mytube.playback.nextSequentialIndex(queue.size, queueIndex)?.let { queue.getOrNull(it) }
        // expand=playerExpanded: advancing from notification/lock while browsing must not yank the full player open
        if (next != null) { playVideo(next, inQueue = true, expand = playerExpanded, forcePlay = forcePlay); return }
        // queue exhausted: radio refills from the just-played track's related list (endless)
        if (radioOn) {
            // screens-fix Music P0-M2: never refill for local tracks (no related list) — stop cleanly.
            if (playing?.isLocal() == true) { radioOn = false; autoAdvance = false; return }
            // F11: one canonical key for refill too (dedupe seeds, not just the queue).
            val cached = (streamData?.related ?: emptyList())
                .filter { r -> (r.url?.isNotBlank() == true) && queue.none { it.favKey() == r.favKey() } }
                .distinctBy { it.favKey() }
                .take(10)
            if (cached.isNotEmpty()) {
                queue = queue + cached
                // F10 (audit P2 2026-11-11): radio grows +10 per refill — trim the consumed head,
                // keeping the last 50 + everything upcoming (queueIndex adjusted).
                val over = queue.size - 60
                if (over > 0 && queueIndex > 0) {
                    val drop = minOf(over, queueIndex)
                    queue = queue.drop(drop)
                    queueIndex -= drop
                }
                playVideo(cached[0], inQueue = true, expand = playerExpanded, forcePlay = true)
                return
            }
            // 2026-11-12 (runtime fix): cached related empty for ANY source (YTM/SC/emptied YT)
            // must not kill the radio silently — refetch + title-search fallback async (all sources),
            // then keep playing; only stop with a reason when that dries up too.
            val cur = playing
            val curUrl = cur?.url
            if (curUrl.isNullOrBlank() && cur?.title.isNullOrBlank()) {
                if (playMode == "repeatAll" && queue.isNotEmpty()) { playVideo(queue[0], inQueue = true, expand = playerExpanded, forcePlay = true); return }
                radioOn = false
                autoAdvance = false
                return
            }
            scope.launch {
                try {
                    var seed: List<VideoItem> = emptyList()
                    var err: String? = null
                    if (!curUrl.isNullOrBlank() && !curUrl.contains("soundcloud.com")) {
                        try {
                            val s = com.mohamedadel.mytube.data.youtube.YouTubeRepo.streamRouted(curUrl)
                            seed = s?.related ?: emptyList()
                            if (seed.isEmpty()) err = s?.error
                        } catch (e: Exception) { err = e.message?.take(80) }
                    }
                    if (seed.isEmpty() && !cur?.title.isNullOrBlank()) {
                        try { seed = com.mohamedadel.mytube.data.youtube.YouTubeRepo.search(cur?.title ?: "", null, null).items }
                        catch (e: Exception) { if (err == null) err = e.message?.take(80) }
                    }
                    val fresh = seed
                        .filter { r -> (r.url?.isNotBlank() == true) && queue.none { it.favKey() == r.favKey() } }
                        .distinctBy { it.favKey() }
                        .take(10)
                    if (fresh.isNotEmpty()) {
                        queue = queue + fresh
                        val over = queue.size - 60
                        if (over > 0 && queueIndex > 0) {
                            val drop = minOf(over, queueIndex)
                            queue = queue.drop(drop)
                            queueIndex -= drop
                        }
                        playVideo(fresh[0], inQueue = true, expand = playerExpanded, forcePlay = true)
                        return@launch
                    }
                    // F9 (audit P2 2026-11-11): repeatAll wraps the live queue when radio dries up
                    // instead of stranding playback at the end.
                    if (playMode == "repeatAll" && queue.isNotEmpty()) { playVideo(queue[0], inQueue = true, expand = playerExpanded, forcePlay = true); return@launch }
                    radioOn = false
                    autoAdvance = false
                    if (!err.isNullOrBlank()) toast(ctx.getString(R.string.err_extract_detail, err))
                } catch (_: Exception) {
                    radioOn = false
                    autoAdvance = false
                    toast(ctx.getString(R.string.toast_radio_fail))
                }
            }
            return
        }
        autoAdvance = false
    }
    fun playFirstInQueue(forcePlay: Boolean = false) {
        if (queue.isNotEmpty()) playVideo(queue[0], inQueue = true, expand = playerExpanded, forcePlay = forcePlay) else autoAdvance = false
    }
    // explicit queue building (long-press menu)
    // 2026-11-12 (runtime fix, بأمر محمد): Play Now ALWAYS enables autoplay —
    // sets + persists prefAutoplay=true, arms radio/auto-advance (related refill
    // continues after this item), and force-starts this item now. Cold taps
    // elsewhere still start silent (unchanged).
    fun playNowAlone(v: VideoItem) {
        prefAutoplay = true
        try { prefs.edit().putBoolean("pref_autoplay", true).apply() } catch (_: Exception) { }
        playVideo(v, inQueue = false, expand = true, forcePlay = true)
        autoAdvance = true
        radioOn = true
    }
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
        if (playing == null) { queueIndex = 0; playVideo(v, inQueue = true, expand = true, forcePlay = true) }
    }
    fun enqueueLast(v: VideoItem) {
        if (isQueued(v)) { toast(ctx.getString(R.string.toast_already_queued)); return }
        queue = queue + v
        toast(ctx.getString(R.string.toast_added_queue))
        if (playing == null) playVideo(v, inQueue = true, expand = true, forcePlay = true)
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
        // H3: bounded wait (~5s) — an unbounded loop hung the app with no message if the service failed to start.
        var tries = 0
        while (p == null && tries < 50) {
            p = PlaybackService.player
            if (p == null) { kotlinx.coroutines.delay(100); tries++ }
        }
        if (p == null) {
            playerError = ctx.getString(R.string.err_player_unavailable)
            toast(ctx.getString(R.string.err_player_unavailable))
            return@LaunchedEffect
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
    // M4: DisposableEffect removes the listener — a recreated player left the old one leaking,
    // and STATE_ENDED fired twice = double queue advance.
    // Equalizer: (re)attach the framework effect whenever the audio session changes,
    // then re-apply the saved preset/custom bands + enabled flag.
    val eqSessionListener = remember {
        object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                try {
                    if (com.mohamedadel.mytube.playback.EqManager.attach(audioSessionId)) {
                        applyEqPrefs()
                    }
                } catch (_: Exception) { }
            }
        }
    }
    val queueListener = remember {
        object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                playerLoading = state == Player.STATE_BUFFERING
                isPlaying = player?.isPlaying == true
                if (state == Player.STATE_READY) playerError = null
                // queue end: repeat one -> replay; shuffle -> random; repeat all -> wrap; order -> next (stops at end)
                if (state == Player.STATE_ENDED && autoAdvance) {
                    if (sleepEndOfTrack) {
                        // F12: honor only for the track that armed it; a stale flag falls through
                        // to the normal advance instead of killing a future track silently.
                        val stale = sleepTrackKey != null && playing?.favKey() != sleepTrackKey
                        sleepEndOfTrack = false
                        sleepTrackKey = null
                        if (!stale) {
                            // stop at end of track: no advance, no repeat, no radio refill
                            autoAdvance = false
                            radioOn = false
                            videoModeQueue = false
                            sleepLeft = ""
                            return
                        }
                    }
                    // 2026-11-12 (runtime fix): autoplay OFF stops automatic advance
                    // (explicit repeatOne replay still honors the user's picked mode).
                    if (!prefAutoplay && playMode != "repeatOne") {
                        radioOn = false
                        autoAdvance = false
                        return
                    }
                    when {
                        playMode == "repeatOne" -> try { player?.seekTo(0L); player?.play() } catch (_: Exception) { }
                        // 2026-11-12: ENDED-path advances force-start (isPlaying is already false
                        // here, so the in-queue resume heuristic alone would load paused).
                        shuffleOn -> playRandomInQueue(forcePlay = true)
                        playMode == "repeatAll" -> {
                            if (radioOn) playNextInQueue(forcePlay = true)
                            else {
                                val nxt = if (queueIndex >= 0) queue.getOrNull(queueIndex + 1) else null
                                if (nxt != null) playVideo(nxt, inQueue = true, forcePlay = true) else playFirstInQueue(forcePlay = true)
                            }
                        }
                        else -> playNextInQueue(forcePlay = true)
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
        }
    }
    androidx.compose.runtime.DisposableEffect(player) {
        try { applyAudioFocus() } catch (_: Exception) { }
        player?.addListener(queueListener)
        player?.addListener(eqSessionListener)
        try {
            val sid = player?.audioSessionId ?: 0
            if (sid != 0 && com.mohamedadel.mytube.playback.EqManager.attach(sid)) applyEqPrefs()
        } catch (_: Exception) { }
        onDispose {
            try { player?.removeListener(queueListener) } catch (_: Exception) { }
            try { player?.removeListener(eqSessionListener) } catch (_: Exception) { }
            // T3 (audit P0 2026-11-11): release the static service callback so it never pins this composition.
            try { PlaybackService.onCustomAction = null } catch (_: Exception) { }
            // T7 (audit P1 2026-11-11): release the native EQ effect with the composition.
            try { com.mohamedadel.mytube.playback.EqManager.release() } catch (_: Exception) { }
        }
    }
    // SponsorBlock segments: fetch on video/toggle change (IO), clear when off.
    androidx.compose.runtime.LaunchedEffect(playing?.id, prefSb) {
        val vid = playing?.id
        if (!prefSb || vid.isNullOrEmpty()) { sbSegments = emptyList(); return@LaunchedEffect }
        try {
            sbSegments = SponsorBlock.fetch(vid)
        } catch (_: Exception) { sbSegments = emptyList() }
    }
    // Calls-only focus mode: pause on ring/offhook, auto-resume on idle (only if we paused).
    androidx.compose.runtime.DisposableEffect(prefAf) {
        var tm: android.telephony.TelephonyManager? = null
        var listener: android.telephony.PhoneStateListener? = null
        var callback: android.telephony.TelephonyCallback? = null
        try {
            if (prefAf == "calls") {
                tm = ctx.getSystemService(android.telephony.TelephonyManager::class.java)
                if (android.os.Build.VERSION.SDK_INT >= 31) {
                    callback = object : android.telephony.TelephonyCallback(), android.telephony.TelephonyCallback.CallStateListener {
                        override fun onCallStateChanged(state: Int) {
                            try {
                                val pl = player
                                if (state == android.telephony.TelephonyManager.CALL_STATE_IDLE) {
                                    if (callPaused) { callPaused = false; try { pl?.play() } catch (_: Exception) { } }
                                } else {
                                    if (pl?.isPlaying == true) { callPaused = true; try { pl?.pause() } catch (_: Exception) { } }
                                }
                            } catch (_: Exception) { }
                        }
                    }
                // T5/T10: local val instead of callback!!.
                val cbReg = callback
                if (cbReg != null) { try { tm?.registerTelephonyCallback(ctx.mainExecutor, cbReg) } catch (_: Exception) { } }
                } else {
                    @Suppress("DEPRECATION")
                    listener = object : android.telephony.PhoneStateListener() {
                        @Suppress("DEPRECATION")
                        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                            try {
                                val pl = player
                                if (state == android.telephony.TelephonyManager.CALL_STATE_IDLE) {
                                    if (callPaused) { callPaused = false; try { pl?.play() } catch (_: Exception) { } }
                                } else {
                                    if (pl?.isPlaying == true) { callPaused = true; try { pl?.pause() } catch (_: Exception) { } }
                                }
                            } catch (_: Exception) { }
                        }
                    }
                    @Suppress("DEPRECATION")
                    tm?.listen(listener, android.telephony.PhoneStateListener.LISTEN_CALL_STATE)
                }
            }
        } catch (_: Exception) { }
        onDispose {
            try {
                if (callback != null && android.os.Build.VERSION.SDK_INT >= 31) { val cb = callback; if (cb != null) try { tm?.unregisterTelephonyCallback(cb) } catch (_: Exception) { } }
                @Suppress("DEPRECATION")
                if (listener != null) tm?.listen(listener, android.telephony.PhoneStateListener.LISTEN_NONE)
            } catch (_: Exception) { }
        }
    }
    // position / duration / buffer polling for the upgraded player UI
    // Position polling runs ONLY while something is loaded; idle = slow heartbeat, no battery burn.
    LaunchedEffect(player, playing) {
        while (true) {
            val p = player
            if (p != null && playing != null) {
                try {
                    playerPositionMs = p.currentPosition.coerceAtLeast(0L)
                    // SponsorBlock: silent seek past community segments (guarded, no toast spam).
                    try {
                        val segs = sbSegments
                        if (prefSb && segs.isNotEmpty() && p.isPlaying && streamData?.isLive != true) {
                            val pos = playerPositionMs
                            for (sg in segs) {
                                if (pos >= sg.startMs && pos < sg.endMs - 300) { p.seekTo(sg.endMs); break }
                            }
                        }
                    } catch (_: Exception) { }
                    playerDurationMs = p.duration.takeIf { it > 0 } ?: 0L
                    playerBufferedPct = p.bufferedPercentage
                    // L1: consume a restored position once the stream reports a duration.
                    try {
                        val ps = pendingSeekMs
                        if (ps != null && ps > 0 && playing?.favKey() == pendingSeekKey && playerDurationMs > 0) {
                            p.seekTo(ps); pendingSeekMs = null; pendingSeekKey = null
                        }
                    } catch (_: Exception) { }
                    // L1: throttled session save (every ~15s while something is loaded).
                    try {
                        val now = android.os.SystemClock.uptimeMillis()
                        if (now - lastSessSave > 15000L) { lastSessSave = now; saveSession() }
                    } catch (_: Exception) { }
                } catch (_: Exception) { }
                // sleep timer watch
                sleepAtMs?.let { deadline ->
                    val left = deadline - android.os.SystemClock.uptimeMillis()
                    if (left <= 0) {
                        fadeOutAndPause()
                        sleepAtMs = null
                        sleepLeft = ""
                        try { prefs.edit().remove("sleep_deadline").apply() } catch (_: Exception) { }
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
    // 2.2 diagnostics: user-triggered report copy (no auto-upload — local-only app, no accounts).
    // Call site: settings button. Includes current visible errors + versions only.
    fun diagnosticsReport(): String {
        return try {
            buildString {
                appendLine("MyTube v" + com.mohamedadel.mytube.BuildConfig.VERSION_NAME + " (vc" + com.mohamedadel.mytube.BuildConfig.VERSION_CODE + ")")
                appendLine("playerError=" + (playerError ?: "none"))
                appendLine("searchError=" + (searchError ?: "none"))
                appendLine("playing=" + (playing?.id ?: "none"))
                appendLine("quality=" + quality)
                appendLine("offline=" + prefOffline + " country=" + prefCountry)
                appendLine("castAvailable=" + com.mohamedadel.mytube.playback.CastManager.available.value + " castConnected=" + com.mohamedadel.mytube.playback.CastManager.connected.value + " castErr=" + (com.mohamedadel.mytube.playback.CastManager.lastError ?: "none"))
            }
        } catch (_: Exception) { "MyTube diagnostics unavailable" }
    }
    // G-A4 (audit P2 2026-11-11): full export/import — every list, fav, history row
    // (capped 500), subscription, group + all prefs in one JSON file shared via FileProvider.
    // Pins are descriptors only: bytes stay on the old device (re-pin after moving).
    // Deferred item 7: last-backup metadata line for the Settings downloads section
    // (null until the first export; survives process death — plain prefs).
    fun backupInfoText(): String? {
        return try {
            val at = try { prefs.getLong("backup_last_at", 0L) } catch (_: Exception) { 0L }
            if (at <= 0L) return null
            val date = try {
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(at))
            } catch (_: Exception) { "" }
            val bytes = try { prefs.getLong("backup_last_bytes", 0L) } catch (_: Exception) { 0L }
            val size = if (bytes >= 1048576L) String.format(java.util.Locale.US, "%.1f MB", bytes / 1048576.0)
            else String.format(java.util.Locale.US, "%d KB", bytes / 1024)
            ctx.getString(
                R.string.backup_last_fmt, date,
                try { prefs.getInt("backup_last_pl", 0) } catch (_: Exception) { 0 },
                try { prefs.getInt("backup_last_fav", 0) } catch (_: Exception) { 0 },
                try { prefs.getInt("backup_last_hist", 0) } catch (_: Exception) { 0 },
                size
            )
        } catch (_: Exception) { null }
    }
    // Deferred item 8: WorkManager scheduler for the periodic feed refresh (see FeedRefreshWorker).
    fun syncFeedWorker(appCtx: android.content.Context, enable: Boolean) {
        try {
            com.mohamedadel.mytube.data.local.FeedRefreshWorker.schedule(appCtx, enable)
        } catch (_: Exception) { }
    }
    fun exportFullBackup() {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val root = org.json.JSONObject()
                root.put("app", "mytube")
                root.put("v", 1)
                root.put("at", System.currentTimeMillis())
                val jpls = org.json.JSONArray()
                for (pw in dao.observePlaylists().first()) {
                    val o = org.json.JSONObject()
                    o.put("name", pw.name); o.put("emoji", pw.emoji); o.put("mode", pw.mode)
                    val ja = org.json.JSONArray()
                    for (it in dao.itemsOnce(pw.id)) {
                        val e = org.json.JSONObject()
                        e.put("videoId", it.videoId); e.put("title", it.title); e.put("channel", it.channel)
                        e.put("thumbnail", it.thumbnail); e.put("url", it.url)
                        e.put("durationSec", it.durationSec ?: -1); e.put("viewCount", it.viewCount ?: -1)
                        e.put("position", it.position)
                        ja.put(e)
                    }
                    o.put("items", ja)
                    jpls.put(o)
                }
                root.put("playlists", jpls)
                fun vrow(videoKey: String, videoId: String?, title: String?, channel: String?, thumb: String?, url: String?, dur: Long?, views: Long?): org.json.JSONObject {
                    val e = org.json.JSONObject()
                    e.put("videoKey", videoKey); e.put("videoId", videoId); e.put("title", title)
                    e.put("channel", channel); e.put("thumbnail", thumb); e.put("url", url)
                    e.put("durationSec", dur ?: -1); e.put("viewCount", views ?: -1)
                    return e
                }
                val jf = org.json.JSONArray()
                for (f in dao.observeFavorites().first()) jf.put(vrow(f.videoKey, f.videoId, f.title, f.channel, f.thumbnail, f.url, f.durationSec, f.viewCount))
                root.put("favorites", jf)
                val jh = org.json.JSONArray()
                for (h in dao.historyPage(500, 0)) jh.put(vrow(h.videoKey, h.videoId, h.title, h.channel, h.thumbnail, h.url, h.durationSec, h.viewCount))
                root.put("history", jh)
                val js = org.json.JSONArray()
                for (x in dao.observeSubscriptions().first()) {
                    val e = org.json.JSONObject()
                    e.put("channelUrl", x.channelUrl); e.put("name", x.name); e.put("avatar", x.avatar)
                    js.put(e)
                }
                root.put("subscriptions", js)
                val jg = org.json.JSONArray()
                for (g in dao.observeGroups().first()) {
                    val o = org.json.JSONObject()
                    o.put("name", g.name)
                    val jm = org.json.JSONArray()
                    for (m in dao.observeGroupMembers(g.id).first()) jm.put(m.channelUrl)
                    o.put("members", jm)
                    jg.put(o)
                }
                root.put("groups", jg)
                val jp = org.json.JSONObject()
                for ((k, v) in prefs.all) {
                    try {
                        when (v) {
                            is Boolean -> jp.put(k, v)
                            is Int -> jp.put(k, v)
                            is Long -> jp.put(k, v)
                            is Float -> jp.put(k, v.toDouble())
                            is String -> jp.put(k, v)
                            is Set<*> -> { val a = org.json.JSONArray(); for (e in v) a.put(e.toString()); jp.put(k, a) }
                        }
                    } catch (_: Exception) { }
                }
                root.put("prefs", jp)
                val dir = java.io.File(ctx.cacheDir, "shared").apply { mkdirs() }
                val f = java.io.File(dir, "mytube_backup_" + java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US).format(java.util.Date()) + ".json")
                f.writeText(root.toString())
                // Deferred item 7 (2026-11-11): backup metadata row — timestamp + size + counts in prefs.
                try {
                    prefs.edit()
                        .putLong("backup_last_at", System.currentTimeMillis())
                        .putLong("backup_last_bytes", f.length())
                        .putInt("backup_last_pl", root.optJSONArray("playlists")?.length() ?: 0)
                        .putInt("backup_last_fav", root.optJSONArray("favorites")?.length() ?: 0)
                        .putInt("backup_last_hist", root.optJSONArray("history")?.length() ?: 0)
                        .apply()
                } catch (_: Exception) { }
                val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "com.mohamedadel.mytube.fileprovider", f)
                val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = android.content.Intent.createChooser(send, try { ctx.getString(R.string.backup_export) } catch (_: Exception) { "backup" })
                chooser.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                ctx.startActivity(chooser)
            } catch (_: Exception) { uiToast(ctx.getString(R.string.backup_export_fail)) }
        }
    }
    fun importFullBackup(uri: android.net.Uri) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val text = ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: return@launch
                val root = org.json.JSONObject(text)
                if (root.optString("app") != "mytube") { uiToast(ctx.getString(R.string.backup_import_fail)); return@launch }
                var nPl = 0; var nFav = 0
                // Deferred item 5 (2026-11-11): import robustness — counters only, restore semantics untouched.
                // - " (import)" suffix applies ONLY on real name clashes (was blind on every import).
                // - rows with neither videoId nor url are invalid and skipped (were inserted as ghosts).
                var nSkippedPlaylists = 0; var nSkippedItems = 0; var nRenamed = 0
                val usedNames = try { dao.observePlaylists().first().map { it.name }.toMutableSet() } catch (_: Exception) { mutableSetOf() }
                val jpls = root.optJSONArray("playlists") ?: org.json.JSONArray()
                for (i in 0 until jpls.length()) {
                    try {
                        val o = jpls.getJSONObject(i)
                        val base = o.optString("name", "import").ifBlank { "import" }.take(80)
                        var finalName = base
                        if (finalName in usedNames) {
                            var n = 1
                            var cand = "$base (import)"
                            while (cand in usedNames) { n++; cand = "$base (import $n)" }
                            finalName = cand.take(80)
                            nRenamed++
                        }
                        usedNames.add(finalName)
                        val pid = dao.insertPlaylist(com.mohamedadel.mytube.data.local.PlaylistEntity(
                            name = finalName,
                            emoji = o.optString("emoji", "\uD83C\uDFB5"), mode = o.optString("mode", "music")))
                        val items = o.optJSONArray("items") ?: org.json.JSONArray()
                        val rows = (0 until items.length()).mapNotNull { j ->
                            try {
                                val e = items.getJSONObject(j)
                                val vid = e.optString("videoId").ifBlank { null }
                                val url = e.optString("url").ifBlank { null }
                                if (vid == null && url == null) { nSkippedItems++; null }
                                else com.mohamedadel.mytube.data.local.PlaylistItemEntity(
                                    playlistId = pid, videoId = vid,
                                    title = e.optString("title").ifBlank { null }, channel = e.optString("channel").ifBlank { null },
                                    thumbnail = e.optString("thumbnail").ifBlank { null }, url = url,
                                    durationSec = e.optLong("durationSec", -1).takeIf { it >= 0 }, viewCount = e.optLong("viewCount", -1).takeIf { it >= 0 },
                                    position = e.optInt("position", j))
                            } catch (_: Exception) { nSkippedItems++; null }
                        }
                        if (rows.isNotEmpty()) dao.insertItems(rows)
                        nPl++
                    } catch (_: Exception) { nSkippedPlaylists++ }
                }
                fun vrow(e: org.json.JSONObject, playedAt: Long): com.mohamedadel.mytube.data.local.HistoryEntity {
                    return com.mohamedadel.mytube.data.local.HistoryEntity(
                        videoKey = e.optString("videoKey"), videoId = e.optString("videoId").ifBlank { null },
                        title = e.optString("title").ifBlank { null }, channel = e.optString("channel").ifBlank { null },
                        thumbnail = e.optString("thumbnail").ifBlank { null }, url = e.optString("url").ifBlank { null },
                        durationSec = e.optLong("durationSec", -1).takeIf { it >= 0 }, viewCount = e.optLong("viewCount", -1).takeIf { it >= 0 },
                        playedAt = playedAt)
                }
                val jf = root.optJSONArray("favorites") ?: org.json.JSONArray()
                for (i in 0 until jf.length()) {
                    try {
                        val h = vrow(jf.getJSONObject(i), System.currentTimeMillis())
                        // Deferred item 5: skip identifier-less rows (no key/id/url = unrestorable ghost).
                        if (h.videoKey.isBlank() && h.videoId.isNullOrBlank() && h.url.isNullOrBlank()) { nSkippedItems++; continue }
                        if (h.videoKey.isNotBlank() && dao.favoriteCount(h.videoKey) == 0) {
                            dao.insertFavorite(com.mohamedadel.mytube.data.local.FavoriteEntity(
                                videoKey = h.videoKey, videoId = h.videoId, title = h.title, channel = h.channel,
                                thumbnail = h.thumbnail, url = h.url, durationSec = h.durationSec, viewCount = h.viewCount))
                            nFav++
                        }
                    } catch (_: Exception) { nSkippedItems++ }
                }
                var nHist = 0
                val jh = root.optJSONArray("history") ?: org.json.JSONArray()
                for (i in 0 until jh.length()) {
                    try {
                        val h = vrow(jh.getJSONObject(i), System.currentTimeMillis())
                        // Deferred item 5: skip identifier-less rows.
                        if (h.videoKey.isBlank() && h.videoId.isNullOrBlank() && h.url.isNullOrBlank()) { nSkippedItems++; continue }
                        dao.upsertHistory(h); nHist++
                    } catch (_: Exception) { nSkippedItems++ }
                }
                val js = root.optJSONArray("subscriptions") ?: org.json.JSONArray()
                for (i in 0 until js.length()) {
                    try {
                        val e = js.getJSONObject(i)
                        val u = e.optString("channelUrl")
                        if (u.isNotBlank() && dao.subscriptionCount(u) == 0)
                            dao.insertSubscription(com.mohamedadel.mytube.data.local.SubscriptionEntity(channelUrl = u, name = e.optString("name").ifBlank { null }, avatar = e.optString("avatar").ifBlank { null }))
                    } catch (_: Exception) { }
                }
                val existingGroups = dao.observeGroups().first().map { it.name }.toSet()
                val jg = root.optJSONArray("groups") ?: org.json.JSONArray()
                for (i in 0 until jg.length()) {
                    try {
                        val o = jg.getJSONObject(i)
                        val name = o.optString("name")
                        if (name.isBlank() || name in existingGroups) continue
                        val gid = dao.insertGroup(com.mohamedadel.mytube.data.local.ChannelGroupEntity(name = name))
                        val jm = o.optJSONArray("members") ?: org.json.JSONArray()
                        for (j in 0 until jm.length()) {
                            try { dao.addMember(com.mohamedadel.mytube.data.local.ChannelGroupMemberEntity(groupId = gid, channelUrl = jm.optString(j))) } catch (_: Exception) { }
                        }
                    } catch (_: Exception) { }
                }
                val jp = root.optJSONObject("prefs")
                if (jp != null) {
                    try {
                        val ed = prefs.edit()
                        val cur = prefs.all
                        val keys = jp.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            try {
                                if (k == "pref_pin_location") continue // device-local paths never roam
                                val old = cur[k]
                                when {
                                    old is Boolean -> ed.putBoolean(k, jp.optBoolean(k, old))
                                    old is Int -> ed.putInt(k, jp.optInt(k, old))
                                    old is Long -> ed.putLong(k, jp.optLong(k, old))
                                    old is Float -> ed.putFloat(k, jp.optDouble(k, old.toDouble()).toFloat())
                                    old is String -> ed.putString(k, jp.optString(k, old))
                                    old is Set<*> -> {
                                        val a = jp.optJSONArray(k)
                                        if (a != null) ed.putStringSet(k, (0 until a.length()).map { a.optString(it) }.toSet())
                                    }
                                    else -> {
                                        // unknown key: restore by JSON type (best effort)
                                        val v: Any? = jp.opt(k)
                                        when (v) {
                                            is Boolean -> ed.putBoolean(k, v)
                                            is Int -> ed.putInt(k, v)
                                            is Long -> ed.putLong(k, v)
                                            is Double -> ed.putFloat(k, v.toFloat())
                                            is String -> ed.putString(k, v)
                                            is org.json.JSONArray -> ed.putStringSet(k, (0 until v.length()).map { v.optString(it) }.toSet())
                                        }
                                    }
                                }
                            } catch (_: Exception) { }
                        }
                        ed.apply()
                    } catch (_: Exception) { }
                }
                uiToast(ctx.getString(R.string.backup_import_ok_fmt, nPl, nFav, nHist))
                // Deferred item 5: report skipped/invalid rows + renames (restore itself unchanged).
                val nSkippedTotal = nSkippedPlaylists + nSkippedItems
                if (nSkippedTotal > 0 || nRenamed > 0) uiToast(ctx.getString(R.string.backup_import_skipped_fmt, nSkippedTotal, nRenamed))
            } catch (_: Exception) { uiToast(ctx.getString(R.string.backup_import_fail)) }
        }
    }
    // Deferred item 6 (2026-11-11): update dialog payload (version + changelog + download URL).
    var updateInfo by remember { mutableStateOf<com.mohamedadel.mytube.data.UpdateCheck.Release?>(null) }
    // G-A2 (audit P2 2026-11-11): compare installed versionName with latest GitHub tag.
    fun checkUpdate() {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val owner = try { ctx.getString(R.string.update_repo_owner) } catch (_: Exception) { "" }
                val repo = try { ctx.getString(R.string.update_repo_name) } catch (_: Exception) { "" }
                if (owner.isBlank() || repo.isBlank()) { uiToast(ctx.getString(R.string.update_not_configured)); return@launch }
                uiToast(ctx.getString(R.string.update_checking))
                val rel = com.mohamedadel.mytube.data.UpdateCheck.latestRelease(owner, repo)
                if (rel == null) { uiToast(ctx.getString(R.string.update_fail)); return@launch }
                val cur = com.mohamedadel.mytube.BuildConfig.VERSION_NAME
                if (!com.mohamedadel.mytube.data.UpdateCheck.isNewer(rel.tag, cur)) uiToast(ctx.getString(R.string.update_latest))
                // Deferred item 6: dialog with changelog + download (was toast-only).
                else updateInfo = rel
            } catch (_: Exception) { uiToast(ctx.getString(R.string.update_fail)) }
        }
    }
    fun copyDiagnostics() {
        try {
            val cm = ctx.getSystemService(android.content.ClipboardManager::class.java) ?: return
            val clip = android.content.ClipData.newPlainText("MyTube diagnostics", diagnosticsReport())
            // S6 (audit P2 2026-11-11): diagnostics carry prefs + player errors — keep them
            // out of the Gboard clipboard history on API33+.
            try {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    clip.description.extras = android.os.PersistableBundle().apply {
                        putBoolean(android.content.ClipDescription.EXTRA_IS_SENSITIVE, true)
                    }
                }
            } catch (_: Exception) { }
            cm.setPrimaryClip(clip)
            toast(ctx.getString(R.string.diag_copied))
        } catch (_: Exception) { toast(ctx.getString(R.string.toast_delete_item_fail)) }
    }

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
                if (prefContentFilter == "playlists") {
                    remotePlaylists = YouTubeRepo.searchPlaylists(q, if (prefCountry == "global") null else prefCountry)
                } else if (prefContentFilter == "albums") {
                    remotePlaylists = YouTubeRepo.searchPlaylists(q, if (prefCountry == "global") null else prefCountry, albums = true)
                } else {
                    remotePlaylists = emptyList()
                }
                // songs = YTM audio search; albums surface as cards only (no video rows)
                val filter = if (prefContentFilter == "songs") "music_songs" else prefContentFilter
                val page = if (prefContentFilter == "albums") com.mohamedadel.mytube.data.youtube.SearchPage(emptyList(), null, false, null)
                else YouTubeRepo.search(q, null, if (prefCountry == "global") null else prefCountry, filter)
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
    // G-A1 (audit P2 2026-11-11): shared / viewed YouTube links land here.
    // Companion-parked by onCreate/onNewIntent; consumed on resume (covers cold start +
    // share-while-alive via singleTop). A bare video URL plays like a tapped result;
    // anything else becomes a search query.
    fun extractYoutubeId(t: String): String? {
        return try {
            val pats = listOf(
                Regex("[?&]v=([A-Za-z0-9_-]{6,})"),
                Regex("youtu\\.be/([A-Za-z0-9_-]{6,})"),
                Regex("/(?:shorts|embed|live)/([A-Za-z0-9_-]{6,})")
            )
            pats.firstNotNullOfOrNull { r -> r.find(t)?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() } }
        } catch (_: Exception) { null }
    }
    fun openPinned(key: String) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val row = try { dao.observePins().first().firstOrNull { it.videoKey == key } } catch (_: Exception) { null }
                if (row == null) { uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch }
                val v = com.mohamedadel.mytube.data.youtube.VideoItem(
                    id = row.videoId, title = row.title, channel = row.channel, url = row.url
                )
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    playVideo(v, inQueue = false, expand = true, forcePlay = true)
                }
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_pin_fail)) }
        }
    }
    fun handleSharedIntent(inc: android.content.Intent) {
        try {
            val action = inc.action
            val type = inc.type
            var text: String? = null
            if (action == android.content.Intent.ACTION_SEND && type == "text/plain")
                text = inc.getStringExtra(android.content.Intent.EXTRA_TEXT)
            else if (action == android.content.Intent.ACTION_VIEW) text = inc.dataString
            text = text?.trim()
            if (text.isNullOrBlank()) return
            val vid = extractYoutubeId(text)
            if (vid != null) {
                val url = "https://www.youtube.com/watch?v=" + vid
                playVideo(VideoItem(id = vid, url = url, title = text.take(120)), inQueue = false, expand = true, forcePlay = true)
            } else {
                doSearch(text.take(200))
            }
        } catch (_: Exception) { }
    }
    // consumed by the ON_RESUME observer above via sharedPending ticks (declared up top).
    androidx.compose.runtime.LaunchedEffect(sharedPending) {
        if (sharedPending > 0) {
            val inc = MainActivity.pendingSharedIntent
            MainActivity.pendingSharedIntent = null
            if (inc != null) {
                val a = inc.action
                if (a == android.content.Intent.ACTION_SEND || a == android.content.Intent.ACTION_VIEW) handleSharedIntent(inc)
                // notif 2026-11-12: download-complete tap deep-links straight to the player.
                else if (a == "com.mohamedadel.mytube.OPEN_PIN") {
                    val k = try { inc.getStringExtra(com.mohamedadel.mytube.data.local.NotifCenter.EXTRA_PIN_KEY) } catch (_: Exception) { null }
                    if (!k.isNullOrBlank()) openPinned(k)
                }
            }
        }
    }
    // L6 (audit P2 2026-11-11): refresh PiP actions with the live play state while in PiP
    // (onEnterPip rebuilds params from the current isPlaying; no state change -> no loop).
    androidx.compose.runtime.LaunchedEffect(pipMode.value, isPlaying) {
        if (pipMode.value) { try { onEnterPip() } catch (_: Exception) { } }
    }
    // SoundCloud search (separate tab, own state; failures surface with retry).
    var scJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    // Music-tab search (SoundCloud / YouTube Music): main track search runs FIRST with one
    // retry; playlist cards resolve independently so a failing playlist endpoint can never
    // blank the track list. Errors carry the real extractor message (truncated) — the old
    // generic toast hid whether this was network, ReCaptcha, or a dead client_id.
    fun doScSearch(q: String, src: Int = scSource, kind: String = scKind) {
        if (q.isBlank()) return
        scQuery = q
        scLoading = true
        scError = null
        scJob?.cancel()
        scJob = scope.launch {
            try {
                val region = if (prefCountry == "global") null else prefCountry
                val musicFilter = when (kind) {
                    "albums" -> "music_albums"
                    "playlists" -> "music_playlists"
                    "artists" -> "music_artists"
                    else -> "music_songs"
                }
                suspend fun mainPage(): com.mohamedadel.mytube.data.youtube.SearchPage {
                    if (src == 1 && kind == "albums")
                        return com.mohamedadel.mytube.data.youtube.SearchPage(emptyList(), null, false, null)
                    var last: com.mohamedadel.mytube.data.youtube.SearchPage? = null
                    repeat(2) {
                        last = if (src == 1)
                            com.mohamedadel.mytube.data.youtube.YouTubeRepo.search(q, null, region, musicFilter)
                        else
                            com.mohamedadel.mytube.data.youtube.YouTubeRepo.scSearch(q)
                        val l = last
                        // T5: null-safe return instead of !! chain.
                        if (l != null && l.error == null && l.items.isNotEmpty()) return l
                    }
                    return last ?: com.mohamedadel.mytube.data.youtube.SearchPage()
                }
                val page = try { mainPage() } catch (e: Exception) {
                    com.mohamedadel.mytube.data.youtube.SearchPage(error = e.javaClass.simpleName + (e.message?.takeIf { it.isNotBlank() }?.let { ": " + it.take(120) } ?: ""))
                }
                if (page.error != null && page.items.isEmpty()) {
                    scError = try { ctx.getString(R.string.err_search_detail_fmt, page.error) } catch (_: Exception) { page.error }
                    scResults = emptyList()
                    remotePlaylists = emptyList()
                    scSuggestion = null
                    scCorrected = false
                } else {
                    scResults = page.items
                    scNextPage = page.nextPage
                    scHasMore = page.hasNext && page.nextPage != null
                    // screens-fix Music P1-M7: mirror main-search correction row.
                    scSuggestion = page.suggestion
                    scCorrected = page.corrected
                    if (!recentMusicSearches.contains(q)) {
                        recentMusicSearches = (listOf(q) + recentMusicSearches).take(6)
                        try { prefs.edit().putString("recent_music_searches", recentMusicSearches.joinToString("\u001f")).apply() } catch (_: Exception) { }
                    }
                    // playlist cards: best-effort, never fatal
                    remotePlaylists = try {
                        if (src == 1 && kind == "albums")
                            com.mohamedadel.mytube.data.youtube.YouTubeRepo.searchPlaylists(q, region, albums = true)
                        else if (src == 1 && kind == "playlists")
                            com.mohamedadel.mytube.data.youtube.YouTubeRepo.searchPlaylists(q, region, music = true)
                        else if (src == 1)
                            emptyList()
                        else
                            com.mohamedadel.mytube.data.youtube.YouTubeRepo.searchPlaylists(q, sc = true)
                    } catch (_: Exception) { emptyList() }
                }
            } catch (e: Exception) {
                val detail = e.javaClass.simpleName + (e.message?.takeIf { it.isNotBlank() }?.let { ": " + it.take(120) } ?: "")
                scError = try { ctx.getString(R.string.err_search_detail_fmt, detail) } catch (_: Exception) { detail }
                scResults = emptyList()
            } finally {
                scLoading = false
            }
        }
    }
    fun refreshTaste() {
        // screens-fix Music P1-M5: TTL 5 min + only when the music tab is actually open.
        val now = System.currentTimeMillis()
        if (tasteProfile != null && now - tasteFetchedAt < 5 * 60 * 1000L) return
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val top = try { dao.topPlayed(20) } catch (_: Exception) { emptyList() }
                val prof = com.mohamedadel.mytube.util.buildTasteProfile(top, favorites, recentMusicSearches, recentSearches)
                tasteProfile = prof
                tasteFetchedAt = System.currentTimeMillis()
                // one network enrichment: YTM songs for the top term (silent fail, tab-local)
                val term = prof.terms.firstOrNull()
                if (!term.isNullOrBlank() && term != tasteMixTerm && !tasteMixLoading) {
                    tasteMixLoading = true
                    try {
                        val page = try {
                            com.mohamedadel.mytube.data.youtube.YouTubeRepo.search(
                                term, null, if (prefCountry == "global") null else prefCountry, "music_songs")
                        } catch (_: Exception) { null }
                        val seen = prof.picks.mapNotNull { it.url ?: it.id }.toSet()
                        tasteMix = (page?.items ?: emptyList()).filter { (it.url ?: it.id ?: "") !in seen }.take(6)
                        tasteMixTerm = term
                    } catch (_: Exception) { }
                    tasteMixLoading = false
                }
            } catch (_: Exception) { }
        }
    }
    fun loadScMore() {
        val q = scQuery
        val pg = scNextPage
        if (q.isBlank() || pg == null || scLoadingMore || scLoading || !scHasMore) return
        scLoadingMore = true
        scope.launch {
            try {
                val region = if (prefCountry == "global") null else prefCountry
                val res = if (scSource == 1) {
                    val mf = when (scKind) {
                        "albums" -> "music_albums"
                        "playlists" -> "music_playlists"
                        "artists" -> "music_artists"
                        else -> "music_songs"
                    }
                    com.mohamedadel.mytube.data.youtube.YouTubeRepo.search(q, pg, region, mf)
                } else {
                    com.mohamedadel.mytube.data.youtube.YouTubeRepo.scSearch(q, pg)
                }
                val seen = scResults.mapNotNull { it.url ?: it.id }.toSet()
                val fresh = res.items.filter { (it.url ?: it.id ?: it.title) !in seen }
                scResults = scResults + fresh
                scNextPage = res.nextPage
                scHasMore = res.hasNext && res.nextPage != null
            } catch (_: Exception) {
                scHasMore = false
            } finally {
                scLoadingMore = false
            }
        }
    }
    // music-tab paging: auto-append when the tail approaches (same pattern as main search)
    val scTail = try {
        val last = scListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        val total = scListState.layoutInfo.totalItemsCount
        total > 0 && last >= total - 3
    } catch (_: Exception) { false }
    LaunchedEffect(showSC) { if (showSC) refreshTaste() }
    LaunchedEffect(scTail, scQuery) {
        if (scTail && scHasMore && !scLoadingMore && !scLoading && scQuery.isNotBlank()) loadScMore()
    }
    // Voice search: permission -> system recognizer -> query + search. Best-effort throughout.
    val voiceResultLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        try {
            val heard = res.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.trim().orEmpty()
            if (heard.isNotEmpty()) {
                if (showSC) { scQuery = heard; doScSearch(heard) }
                else { query = heard; svm.onQueryChange(heard); showSearch = false; doSearch(heard) }
            }
        } catch (_: Exception) { }
    }
    fun launchVoiceRecognizer() {
        try {
            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, if (prefUiLang == "en") "en" else "ar")
                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, ctx.getString(R.string.voice_search))
            }
            voiceResultLauncher.launch(intent)
        } catch (_: Exception) {
            toast(ctx.getString(R.string.voice_unavailable))
        }
    }
    val afPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) {
            try {
                prefAf = "all"
                prefs.edit().putString("pref_audiofocus", "all").apply()
                applyAudioFocus()
                toast(ctx.getString(R.string.af_perm_denied))
            } catch (_: Exception) { }
        }
    }
    val voicePermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) try { launchVoiceRecognizer() } catch (_: Exception) { }
        else toast(ctx.getString(R.string.voice_unavailable))
    }
    fun startVoiceSearch() {
        try {
            val granted = ctx.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            if (granted) launchVoiceRecognizer() else voicePermLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } catch (_: Exception) { toast(ctx.getString(R.string.voice_unavailable)) }
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
        radioOn = false
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
                // screens-fix Home P0-H1: VideoItem has no publishedAt (only uploadDate String),
                // so newest-first sort is unreliable — round-robin interleave avoids channel blocks.
                // TODO(field): verify newest-first across 3+ subs; add publishedAt to VideoItem if needed.
                feedItems = if (all.size <= 1) all else {
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
                    mixed
                }
                // notif 2026-11-12: store newly seen items (Room + read flag; bell badge).
                try { com.mohamedadel.mytube.data.local.NotifCenter.recordFresh(dao, all) } catch (_: Exception) { }
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_feed_fail)) }
            feedLoading = false
        }
    }
    // Geo-switch ACCEPT path (2026-11-12, runtime layer): a plain (non-composable)
    // local fun declared after refreshTrending/refreshFeed (Kotlin locals: no forward
    // refs) and before SettingsPane. Refuse path never calls this — old kept.
    fun confirmCountrySwitch(v: String) {
        pendingCountry = null
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try { com.mohamedadel.mytube.playback.MediaCache.clear(ctx.applicationContext) } catch (_: Exception) { }
            try { com.mohamedadel.mytube.data.youtube.YouTubeRepo.clearCaches() } catch (_: Exception) { }
            try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                prefCountry = v
                try { prefs.edit().putString("pref_country", v).apply() } catch (_: Exception) { }
                try { com.mohamedadel.mytube.data.youtube.YouTubeRepo.applyRegion(v, prefUiLang) } catch (_: Exception) { }
                // L7 (audit P2 2026-11-11): reload both surfaces + announce.
                refreshTrending()
                refreshFeed()
                toast(ctx.getString(R.string.region_changed))
            } } catch (_: Exception) { }
        }
    }
    fun openGroupFeed(g: com.mohamedadel.mytube.data.local.ChannelGroupEntity, urls: Set<String>) {
        fromLibrary = true
        showGroups = false
        openGroup = null
        feedGroupUrls = urls
        feedGroupName = g.name
        feedFilter = null
        showFeed = true
        if (feedItems.isEmpty()) refreshFeed()
    }
    // pin-deepfix 2026-11-11: retry choices for the one fresh-resolve retry in runPinDownload
    // (googlevideo URLs expire — a stale plan fails even on good networks).
    // requestPin: player context passes resolved sd+selection (no re-resolve); list context resolves first.
    fun requestPin(v: VideoItem, sd: com.mohamedadel.mytube.data.youtube.StreamData?, q: String?, aIdx: Int?, sIdxs: Set<Int>?) {
        if ((sd?.isLive == true) || (streamData?.isLive == true && playing?.favKey() == v.favKey())) {
            toast(ctx.getString(R.string.err_pin_live)); return
        }
        val key = v.favKey()
        if (pinKeys.contains(key) || pinningKeys.contains(key)) return
        pinningKeys = pinningKeys + key
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // pin-deepfix: a null URL used to exit silently (no toast at all).
                // pin-runtime 2026-11-11: uiToast (main Looper) — Toast.show() from this
                // IO thread throws and the catch drops it, so every failure was invisible.
                val u = v.url
                if (sd == null && u.isNullOrBlank()) { uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch }
                val s = sd ?: YouTubeRepo.streamRouted(u ?: return@launch) ?: run { uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch }
                // pin-deepfix: streaming-only (HLS) audio cannot be pinned as bytes — say so clearly.
                if (s.audioHlsOnly) { uiToast(ctx.getString(R.string.pin_err_stream)); return@launch }
                if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
                    uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch
                }
                val kbps = ctx.estimatedDownstreamKbps()
                // Audio-only sources (e.g. SoundCloud) have no video map: pin the audio directly.
                val qq = q ?: if (s.videoUrlByQuality.isEmpty()) QUALITY_AUDIO_ONLY else pickStartQuality(s, kbps)
                val ai = aIdx ?: pickAudioIdxFor(s)
                val si = sIdxs ?: matchSubIdxsFor(s)
                val plan = com.mohamedadel.mytube.data.local.PinManager.buildPlan(s, qq, ai, si, kbps, maxH = if (prefAllow4k) Int.MAX_VALUE else 1080)
                if (plan == null) { uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch }
                val est = com.mohamedadel.mytube.data.local.PinManager.estimateBytes(listOf(plan.videoUrl, plan.audioUrl, plan.subUrl))
                // pin-runtime 2026-11-11: dialog state on Main (writes from IO are
                // silently late/lost on some skins) + loc from the centralized default.
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    pinSd = s
                    pinSelQuality = qq
                    pinSelAudio = ai
                    pinSelSubs = si
                    try {
                        pinSelLoc = com.mohamedadel.mytube.data.local.PinFiles.loc(ctx.applicationContext)
                    } catch (_: Exception) { }
                    pinEst = est
                    pinConfirm = com.mohamedadel.mytube.data.local.PinManager.PinRequest(v, plan, est)
                }
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_pin_fail)) }
            finally { pinningKeys = pinningKeys - key }
        }
    }
    // pause/stop 2026-11-12: paused keys (job ended, staging kept for Range resume) +
    // stored plans so resume re-enters without re-resolving (single pinProgress source kept).
    var pausedKeys by remember { mutableStateOf(setOf<String>()) }
    val pinPlans = remember { java.util.concurrent.ConcurrentHashMap<String, Triple<com.mohamedadel.mytube.data.youtube.VideoItem, com.mohamedadel.mytube.data.local.PinManager.PinPlan, PinRetry?>>() }
    // pause/stop 2026-11-12: download-job execution lives in PinDownloadRunner
    // (data/local) — this composable method sits at the 64KB codegen limit and the
    // inline body broke compileDebugKotlin (MethodTooLargeException). Thin
    // forwarders only; every existing call site is untouched.
    fun pinDeps(): com.mohamedadel.mytube.data.local.PinDownloadRunner.Deps =
        com.mohamedadel.mytube.data.local.PinDownloadRunner.Deps(
            scope = scope, act = ctx, app = ctx.applicationContext, dao = dao, prefs = prefs,
            pinJobs = pinJobs, pinPlans = pinPlans,
            pinSelLoc = pinSelLoc, allow4k = prefAllow4k,
            kbps = { ctx.estimatedDownstreamKbps() },
            uiToast = { m -> uiToast(m) },
            isPinning = { k -> pinningKeys.contains(k) },
            addPinning = { k -> pinningKeys = pinningKeys + k },
            removePinning = { k -> pinningKeys = pinningKeys - k },
            isPausedKey = { k -> pausedKeys.contains(k) },
            addPaused = { k -> pausedKeys = pausedKeys + k },
            removePaused = { k -> pausedKeys = pausedKeys - k },
            setProgress = { k, f -> try { pinProgress = if (f == null) pinProgress - k else pinProgress + (k to f) } catch (_: Exception) { } }
        )
    fun runPinDownload(v: VideoItem, plan: com.mohamedadel.mytube.data.local.PinManager.PinPlan, key: String, alreadyAdded: Boolean = false, retry: PinRetry? = null, resume: Boolean = false) =
        com.mohamedadel.mytube.data.local.PinDownloadRunner.run(v, plan, key, alreadyAdded, retry, resume, pinDeps())
    // notif 2026-11-12: POST_NOTIFICATIONS at pin time (API33+) so the terminal
    // download notification is visible even if the launch-time grant was skipped.
    // PAUSE: suspends the OkHttp stream (flag; the loop exits at the next 256KB chunk),
    // keeps staging (.part byte counts = persisted resume offsets) + frozen progress.
    fun pausePinDownload(key: String) =
        com.mohamedadel.mytube.data.local.PinDownloadRunner.pause(key, pinDeps())
    // RESUME: re-enters runPinDownload with resume=true — staging kept, Range continues.
    fun resumePinDownload(key: String) =
        com.mohamedadel.mytube.data.local.PinDownloadRunner.resume(key, pinDeps())
    // STOP: aborts the job, wipes the partial file + temp meta (staging), clears every
    // tracking key, dismisses the notification — and shows ONLY the cancel toast
    // (runPinDownload stays silent on the cancel path, no false-failure toast).
    fun cancelPinDownload(key: String) =
        com.mohamedadel.mytube.data.local.PinDownloadRunner.stop(key, pinDeps())
    // Notification Pause / Resume / Stop taps land here (same process static registry).
    try {
        com.mohamedadel.mytube.playback.DownloadActions.onPause = { k -> pausePinDownload(k) }
        com.mohamedadel.mytube.playback.DownloadActions.onResume = { k -> resumePinDownload(k) }
        com.mohamedadel.mytube.playback.DownloadActions.onStop = { k -> cancelPinDownload(k) }
    } catch (_: Exception) { }
    // Download-manager state: live per-pin progress + manager screen + keepalive toggle.
    fun ensurePinNotifPerm() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } catch (_: Exception) { }
    }
    fun confirmPin() {
        ensurePinNotifPerm()
        val pc = pinConfirm ?: return
        val sd = pinSd
        val key = pc.v.favKey()
        pinConfirm = null
        pinSd = null
        // remember last choices: quick-pin replays them with one tap
        try {
            prefs.edit()
                .putString("pref_pin_last_q", pinSelQuality)
                .putString("pref_pin_last_subs", pinSelSubs.sorted().joinToString(","))
                .apply()
        } catch (_: Exception) { }
        val plan = if (sd != null) {
            com.mohamedadel.mytube.data.local.PinManager.buildPlan(sd, pinSelQuality, pinSelAudio, pinSelSubs, ctx.estimatedDownstreamKbps(), maxH = if (prefAllow4k) Int.MAX_VALUE else 1080)
        } else pc.plan
        if (plan == null) { toast(ctx.getString(R.string.toast_pin_fail)); return }
        runPinDownload(pc.v, plan, key, retry = PinRetry(pinSelQuality, pinSelAudio, pinSelSubs))
    }
    // Shared download execution (dialog confirm + quick pin): progress card + notification + guards.
    // Quick pin: one tap, no dialog. Replays the last confirmed choices (quality + subtitle set
    // + location pref); falls back to the same smart defaults as the dialog.
    fun quickPin(v: VideoItem, sd: com.mohamedadel.mytube.data.youtube.StreamData?) {
        ensurePinNotifPerm()
        if ((sd?.isLive == true) || (streamData?.isLive == true && playing?.favKey() == v.favKey())) {
            toast(ctx.getString(R.string.err_pin_live)); return
        }
        val key = v.favKey()
        if (pinKeys.contains(key) || pinningKeys.contains(key)) return
        // F6: optimistic add on the UI thread (mirrors requestPin) —
        // closes the double-tap race where two launches both passed the check before either added.
        pinningKeys = pinningKeys + key
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // pin-deepfix: a null URL used to exit silently (no toast at all).
                // pin-runtime 2026-11-11: uiToast (main Looper) + key ownership: the F6
                // optimistic add stays until runPinDownload's inner job finishes — the old
                // outer-finally removal ran the moment the inner job was LAUNCHED, hiding
                // in-flight pins from Downloads/cancel and reopening the double-tap race.
                // Early exits below remove the key explicitly; the success path is owned
                // by runPinDownload(alreadyAdded=true) which removes it when done.
                val u = v.url
                if (sd == null && u.isNullOrBlank()) { pinningKeys = pinningKeys - key; uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch }
                val s = sd ?: YouTubeRepo.streamRouted(u ?: run { pinningKeys = pinningKeys - key; return@launch }) ?: run { pinningKeys = pinningKeys - key; uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch }
                // pin-deepfix: streaming-only (HLS) audio cannot be pinned as bytes — say so clearly.
                if (s.audioHlsOnly) { pinningKeys = pinningKeys - key; uiToast(ctx.getString(R.string.pin_err_stream)); return@launch }
                if (s.audioLowestUrl == null && s.videoUrlByQuality.isEmpty()) {
                    pinningKeys = pinningKeys - key; uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch
                }
                val kbps = ctx.estimatedDownstreamKbps()
                val lastQ = try { prefs.getString("pref_pin_last_q", null) } catch (_: Exception) { null }
                val qq = if (s.videoUrlByQuality.isEmpty()) QUALITY_AUDIO_ONLY
                    else if (!lastQ.isNullOrBlank() && (lastQ == QUALITY_AUDIO_ONLY || s.videoUrlByQuality.containsKey(lastQ) || lastQ.startsWith("auto"))) lastQ
                    else pickStartQuality(s, kbps)
                val ai = pickAudioIdxFor(s)
                val lastSubs = try { (prefs.getString("pref_pin_last_subs", "") ?: "").split(",").mapNotNull { it.toIntOrNull() }.filter { s.subtitles.getOrNull(it) != null }.toSet() } catch (_: Exception) { emptySet() }
                val si = if (lastSubs.isNotEmpty()) lastSubs else matchSubIdxsFor(s)
                val plan = com.mohamedadel.mytube.data.local.PinManager.buildPlan(s, qq, ai, si, kbps, maxH = if (prefAllow4k) Int.MAX_VALUE else 1080)
                if (plan == null) { pinningKeys = pinningKeys - key; uiToast(ctx.getString(R.string.toast_pin_fail)); return@launch }
                runPinDownload(v, plan, key, alreadyAdded = true, retry = PinRetry(qq, ai, si))
            } catch (_: Exception) { pinningKeys = pinningKeys - key; uiToast(ctx.getString(R.string.toast_pin_fail)) }
        }
    }
    // external orphan pins (left after switching location to internal)
    var orphanInfo by remember { mutableStateOf(0 to 0L) }
    fun refreshOrphans() {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                orphanInfo = com.mohamedadel.mytube.data.local.PinFiles.externalOrphans(ctx.applicationContext, pinKeys.toList())
            } catch (_: Exception) { }
        }
    }
    fun cleanOrphans() {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val n = com.mohamedadel.mytube.data.local.PinFiles.cleanExternalOrphans(ctx.applicationContext, pinKeys.toList())
                refreshOrphans()
                uiToast(ctx.getString(R.string.pin_orphans_cleaned_fmt, n))
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_pin_fail)) }
        }
    }
    androidx.compose.runtime.LaunchedEffect(showDownloads, pinKeys) { if (showDownloads) refreshOrphans() }
    // notif 2026-11-12: opening the screen marks everything read (badge clears).
    androidx.compose.runtime.LaunchedEffect(showNotifications) {
        if (showNotifications) { try { dao.markAllNotificationsRead() } catch (_: Exception) { } }
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
                uiToast(ctx.getString(R.string.toast_pins_cleared))
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_pin_fail)) }
        }
    }
    fun doUnpin(key: String) {
        // never delete under an in-flight download (it would re-upsert after us)
        if (pinningKeys.contains(key)) { toast(ctx.getString(R.string.pinning)); return }
        try { if (playing?.favKey() == key) closePlayer() } catch (_: Exception) { }
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                com.mohamedadel.mytube.data.local.PinManager.unpin(ctx.applicationContext, dao, key)
                uiToast(ctx.getString(R.string.toast_unpinned))
            } catch (_: Exception) { uiToast(ctx.getString(R.string.toast_pin_fail)) }
        }
    }
    fun smartBack(): Boolean {
        if (showNotifications) {
            showNotifications = false
            return true
        }
        if (openGroup != null) {
            openGroup = null
            showGroups = true
            return true
        }
        if (showGroups) {
            showGroups = false
            if (fromLibrary) { fromLibrary = false; showLibrary = true }
            return true
        }
        if (feedGroupUrls != null && showFeed) {
            feedGroupUrls = null; feedGroupName = null
            if (fromLibrary) { fromLibrary = false; showLibrary = true } else showFeed = false
            return true
        }
        if (showDownloads) {
            showDownloads = false
            if (fromLibrary) { fromLibrary = false; showLibrary = true }
            return true
        }
        if (showLocal) {
            showLocal = false
            if (fromLibrary) { fromLibrary = false; showLibrary = true }
            return true
        }
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
        if (openRemote != null) {
            openRemote = null
            openRemoteItems = emptyList()
            return true
        }
        if (showLibrary) {
            showLibrary = false
            return true
        }
        if (showSC) {
            showSC = false
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
    BackHandler(enabled = showNotifications || showFeed || channelScreenUrl != null || showComments || isFullscreen || playerExpanded || playing != null || showPlaylists || showFavorites || showHistory || showQueue || showSettings || showSearch || showLibrary || showDownloads || showGroups || openGroup != null || showSC || openRemote != null || results.isNotEmpty()) { smartBack() }

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
                Text("v0.3.6", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
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
        showSC = false
        showLibrary = false; fromLibrary = false
        showFavorites = false; showHistory = false; showPlaylists = false
        openPlaylistId = null; showSettings = false; playerExpanded = false
    }
    fun goLibrary() {
        showSC = false
        showFavorites = false; showHistory = false; showPlaylists = false
        openPlaylistId = null; showSettings = false; playerExpanded = false
        fromLibrary = false; showLibrary = true
    }
    fun goSettings() {
        showSC = false
        showLibrary = false; fromLibrary = false
        showFavorites = false; showHistory = false; showPlaylists = false
        openPlaylistId = null; playerExpanded = false
        showSettings = true
    }
    fun goSC() {
        showLibrary = false; fromLibrary = false
        showFavorites = false; showHistory = false; showPlaylists = false
        openPlaylistId = null; showSettings = false; playerExpanded = false
        showSC = true
    }
    val tabLibrary = showLibrary || showFavorites || showHistory || showPlaylists
    // method-size 2026-11-12: header region (battery banner + compact header +
    // standalone search) out of the Scaffold content lambda — same local-fun trick as
    // PlayerPane/SettingsPane. Pure move, zero behavior change.
    @Composable
    fun ContentHeader() {
        // Battery-exemption banner: visible only while the exemption is missing (never in fullscreen)
        if (!isFullscreen && !batteryExempt) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                shape = MaterialTheme.shapes.small,
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
            // 2026-11-12 (runtime fix): hierarchical Up on EVERY non-root screen —
            // pops the nav stack via smartBack(), never jumps home. Root = home feed
            // with nothing playing and no results. Mirrors the BackHandler enable set.
            val canGoBack = showNotifications || playerExpanded || playing != null || showPlaylists || showSettings || showSearch || showLibrary || results.isNotEmpty() || showFeed || showDownloads || showFavorites || showHistory || showSC || channelScreenUrl != null || showQueue || showGroups || openGroup != null || openRemote != null || showLocal || openPlaylistId != null
            if (canGoBack) {
                IconButton(onClick = { smartBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
            Icon(myTubeLogo(), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(30.dp))
            Spacer(Modifier.weight(1f))
            // notif 2026-11-12: bell with unread badge -> Notifications screen.
            androidx.compose.material3.BadgedBox(
                badge = { if (unreadCount > 0) androidx.compose.material3.Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) } }
            ) {
                IconButton(onClick = { showNotifications = true }) {
                    Icon(Icons.Rounded.Notifications, contentDescription = stringResource(R.string.notif_bell))
                }
            }
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
                onVoice = { startVoiceSearch() },
                onClearRecents = {
                    recentSearches = emptyList()
                    try { prefs.edit().remove("recent_searches").apply() } catch (_: Exception) { }
                }
            )
        }

        }
    }

    // 2026-11-12 (compile fix): the Scaffold content lambda breached the 64KB JVM
    // method limit (Method too large). This branch moves to a local @Composable —
    // captures stay implicit, no signature churn, no behavior change.
    @Composable
    fun PlaylistsRoute() {
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
                                        radioOn = false
                                        videoModeQueue = playlists.firstOrNull { it.id == pid }?.mode == "video"
                                        playVideo(list[0], inQueue = true, expand = true, forcePlay = true)
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
                                    radioOn = false
                                    videoModeQueue = playlists.firstOrNull { it.id == openPlaylistId }?.mode == "video"
                                    playVideo(list[idx], inQueue = true, expand = true, forcePlay = true)
                                }
                            },
                            onCreate = { name, emoji ->
                                scope.launch { dao.insertPlaylist(PlaylistEntity(name = name, emoji = emoji)) }
                            },
                            onSaveEdits = { pid, name, emoji, mode, orderedIds ->
                                scope.launch {
                                    try {
                                        dao.savePlaylistEdits(pid, name, emoji, mode, orderedIds)
                                        toast(ctx.getString(R.string.toast_playlist_saved))
                                    } catch (_: Exception) { toast(ctx.getString(R.string.toast_playlist_save_fail)) }
                                }
                            },
                            onDelete = { pid -> scope.launch { dao.deletePlaylist(pid); if (openPlaylistId == pid) openPlaylistId = null } },
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
                            // 2026-11-12 (runtime fix): hierarchical pop via smartBack — the old
                            // inline close dropped fromLibrary and landed on home instead of Library.
                            onBack = { smartBack() },
                            pinBadges = pinBadges,
                            onLongClick = { optionsFor = it },
                            covers = plCovers,
                            onQueueItems = { sel ->
                                val fresh = sel.map { it.toVideoItem() }.filter { !isQueued(it) }
                                if (fresh.isNotEmpty()) {
                                    queue = queue + fresh
                                    if (playing == null) playVideo(fresh[0], inQueue = true)
                                }
                            },
                            onPinItems = { vs -> scope.launch { for (v in vs) { try { quickPin(v, null) } catch (_: Exception) { } } } },
                            onRemoveItems = { pid, ids -> scope.launch { try { dao.deleteItems(pid, ids) } catch (_: Exception) { } } },
                            onExport = { pid -> exportPlaylistCsv(pid) },
                            // screens-fix Library P1-L4: CSV import entry.
                            onImport = { try { csvImportLauncher.launch("text/*") } catch (_: Exception) { } },
                            tabSort = plTabSort,
                            onTabSort = { v -> plTabSort = v; try { prefs.edit().putString("pl_tab_sort", v).apply() } catch (_: Exception) { } },
                            songSort = plSongSort,
                            onSongSort = { v ->
                                plSongSort = v
                                try { openPlaylistId?.let { prefs.edit().putString("pl_sort_" + it, v).apply() } } catch (_: Exception) { }
                            }
        )
    }

    Scaffold(
        topBar = { },
        bottomBar = {
            // Mini-player above the bottom nav (YouTube order): Scaffold layering keeps
            // this bar above ALL destinations except Settings (2026-11-12 runtime fix).
            // Surfaces allowlist (standing rule): Home/Search/Music/Library/Downloads/Queue/
            // Notifications/Player(collapsed)/Channel/Remote/LocalAudio — everything rendered
            // inside this Scaffold's content. Excluded: Settings screen, fullscreen video,
            // expanded player, PiP (system-provided mini UI). Master switch pref_mini_enabled.
            if (!isInPip && !isFullscreen && prefMiniEnabled && playing != null && !playerExpanded && !showSettings) {
                MiniPlayer(
                    video = playing!!,
                    swipeEnabled = prefMiniSwipe,
                    titleOverride = translatedTitle,
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
                    castAvailable = CastManager.available.value,
                    // 2026-11-12 (runtime fix): queue reachable straight from the mini-player.
                    onQueue = { showQueue = true },
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
            // YouTube bottom nav: Home / SC / Library / Settings. Hidden on watch screen, fullscreen, PiP, search.
            // SC stays visible: it is a top-level tab with its own nav item, rendered inside Scaffold content.
            if (!isInPip && !isFullscreen && !playerExpanded && !showSearch) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    NavigationBarItem(
                        selected = !tabLibrary && !showSettings && !showSC,
                        onClick = { goHome() },
                        icon = { Icon(Icons.Rounded.Home, contentDescription = stringResource(R.string.nav_home)) },
                        label = { Text(stringResource(R.string.nav_home)) }
                    )
                    NavigationBarItem(
                        selected = showSC,
                        onClick = { goSC() },
                        icon = { Icon(Icons.Rounded.QueueMusic, contentDescription = stringResource(R.string.sc_title)) },
                        label = { Text(stringResource(R.string.sc_title)) }
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
                if (cur != null && quality != QUALITY_AUDIO_ONLY && player != null) {
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
            ContentHeader()

            val cur = playing
            if (cur != null && (playerExpanded || isFullscreen)) {
    // screens-fix queue (method-size, 2026-11-11): the 130-line PlayerArea call lived inline
    // in MyTubeApp.invoke and helped breach the 64KB limit — same local-fun trick as SettingsPane.
    @Composable
    fun PlayerPane(paneMod: Modifier = Modifier) {
                PlayerArea(
                    modifier = paneMod,
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
                        streamData?.let { applyQuality(q, it, player, autoplay = true, local = playingPinned) }
                    },
                    onTogglePlay = {
                        playerError = null
                        if (player?.isPlaying == true) player?.pause() else player?.play()
                    },
                    onSeek = { pos -> try { player?.seekTo(pos) } catch (_: Exception) { } },
                    onRetry = {
                        playerError = null
                        streamData?.let { applyQuality(quality, it, player, forceStart = true, local = playingPinned) }
                    },
                    queueOn = autoAdvance,
                    playMode = playMode,
                    playModeLabel = modeLabel(playMode),
                    shuffleOn = shuffleOn,
                    onCycleMode = { cycleMode() },
                    onToggleShuffle = { setShuffle(!shuffleOn) },
                    onQueue = { showQueue = true },
                    onClose = { closePlayer() },
                    onCollapse = { playerExpanded = false },
                    isFullscreen = isFullscreen,
                    onToggleFullscreen = { setFullscreen(!isFullscreen) },
                    onSetMode = { setMode(it) },
                    onSetShuffle = { setShuffle(it) },
                    subtitles = if (!prefShowSubs) emptyList() else (streamData?.subtitles ?: emptyList()).let { if (prefSubHideAuto) it.filter { t -> !t.auto } else it },
                    subIdx = subTrackIdx,
                    onSubtitleTrack = { i ->
                        // manual pick becomes the saved subtitle preference (applies to all future videos)
                        try {
                            if (i < 0) {
                                prefSubLang = "off"
                                prefs.edit().putString("pref_sub_lang", "off").apply()
                                subTrackIdx = -1
                                subTrackFor = null // F8: explicit off clears any override owner
                                toast(ctx.getString(R.string.toast_sub_off))
                            } else {
                                val t = streamData?.subtitles?.getOrNull(i)
                                val tag = t?.langTag?.substringBefore("-")?.takeIf { it.isNotBlank() }
                                // keep the raw tag (matching is prefix-based): collapsing rare tags to
                                // "auto" made the setting disagree with the chip the user just picked.
                                val code = tag ?: "auto"
                                prefSubLang = code
                                prefs.edit().putString("pref_sub_lang", code).apply()
                                subTrackIdx = i
                                subTrackFor = playing?.favKey() // F8: explicit pick owns the override
                                toast(ctx.getString(R.string.sub_toast_fmt, t?.label?.takeIf { it.isNotBlank() } ?: code))
                            }
                        } catch (_: Exception) { toast(ctx.getString(R.string.toast_sub_fail)) }
                        applyLangPrefs()
                    },
                    audioTracks = streamData?.audioTracks ?: emptyList(),
                    audioIdx = audioTrackIdx,
                    onAudioTrack = { i ->
                        // F13 (audit P2 2026-11-11, by design): the picked track may be an external
                        // (merged) stream, so the source is rebuilt via applyQuality; in-player
                        // trackSelector overrides are already used where the source is shared
                        // (muxed-audio disable + F8 subtitle override in applyLangPrefs).
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
                    castAvailable = CastManager.available.value,
                    isCasting = CastManager.connected.value,
                    sleepLabel = sleepLeft.ifBlank { null },
                    onSleepClick = { showSleep = true },
                    isFav = cur.favKey() in favoriteSet,
                    onToggleFav = { toggleFavorite(cur) },
                    onSave = { pickFor = cur },
                    likeCount = streamData?.likeCount,
                    description = streamData?.description,
                    chapters = streamData?.chapters ?: emptyList(),
                    related = streamData?.related ?: emptyList(),
                    showRelated = prefShowRelated,
                    showInfo = prefShowInfo,
                    showSubs = prefShowSubs,
                    showCommentsPill = prefShowComments,
                    isLive = streamData?.isLive == true,
                    // Fix: chapter tap starts playback too — seeking a never-started player left silence.
                    onChapterSeek = { pos -> try { player?.seekTo(pos); playerError = null; player?.play() } catch (_: Exception) { } },
                    onRelatedPlay = { rv -> playVideo(rv, forcePlay = true) },
                    isSubscribed = streamData?.channelUrl != null && subscriptions.any { it.channelUrl == streamData?.channelUrl },
                    isPinned = pinKeys.contains(cur.favKey()),
                    isPinning = pinningKeys.contains(cur.favKey()),
                    pinProgress = pinProgress[cur.favKey()],
                    onPin = {
                        if (pinKeys.contains(cur.favKey())) doUnpin(cur.favKey())
                        else requestPin(cur, streamData, quality, audioTrackIdx, ((if (subTrackIdx >= 0) setOf(subTrackIdx) else emptySet()) + matchSubIdxsFor(streamData)))
                    },
                    onSubscribe = { toggleSubscription(streamData?.channelUrl, cur.channel, channelDetail?.avatar) },
                    onChannelClick = { openChannel(streamData?.channelUrl) },
                    onCommentsClick = {
                        showComments = true
                        commentsLoading = true
                        val url = playing?.url
                        scope.launch {
                            try {
                                commentList = YouTubeRepo.comments(url)
                                commentsHasMore = YouTubeRepo.CommentPager.hasMore
                            } catch (_: Exception) { }
                            commentsLoading = false
                        }
                    },
                    subStyle = subStyleFromPrefs(prefSubFg, prefSubBg, prefSubEdge, prefSubScale, prefSubBold, prefSubBgOpacity),
                    category = streamData?.category,
                    tags = streamData?.tags ?: emptyList(),
                    titleOverride = translatedTitle,
                    onTagClick = { q -> try { playerExpanded = false } catch (_: Exception) { }; query = q; doSearch(q) },
                    onShare = { try { ctx.shareVideoCtx(cur) } catch (_: Exception) { } },
                    onRelatedOptions = { optionsFor = it },
                    storyboards = streamData?.storyboards ?: emptyList()
                )
    }
                // 2026-11-12 (runtime fix): bounded height (weight) so the player's
                // internal ScrollableColumn can scroll — unbounded content clipped before.
                if (isFullscreen) PlayerPane()
                else Box(Modifier.weight(1f)) { PlayerPane(paneMod = Modifier.fillMaxSize()) }
                // comments sheet (real CommentsExtractor, pageless first 30)
                if (showComments) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showComments = false },
                        title = { Text(stringResource(R.string.pill_comments)) },
                        text = {
                            if (commentsLoading) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                }
                            } else if (commentList.isEmpty()) {
                                Text(stringResource(R.string.no_results), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(commentList, key = { c -> (c.author ?: "") + "|" + (c.text?.take(24) ?: "") + "|" + (c.date ?: "") }) { c ->
                                        Row(verticalAlignment = Alignment.Top) {
                                            if (!c.avatar.isNullOrBlank()) {
                                                AsyncImage(model = c.avatar, contentDescription = c.author, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                                Spacer(Modifier.width(8.dp))
                                            }
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    (c.author ?: "") + (if (!c.date.isNullOrBlank()) " • " + c.date else "") + (if (c.likes > 0) " • " + ctx.formatViews(c.likes.toLong()) else ""),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(c.text ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                    // page-walk: reaching the end auto-fetches the next comments page
                                    if (commentsHasMore || commentsLoadingMore) {
                                        item {
                                            androidx.compose.runtime.LaunchedEffect(commentList.size) { loadMoreComments() }
                                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center) {
                                                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = { },
                        dismissButton = { androidx.compose.material3.TextButton(onClick = { showComments = false }) { Text(stringResource(R.string.close)) } }
                    )
                }
            }

            if (!isFullscreen) {

            val backupImportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        importFullBackup(uri)
    }
            @Composable
    fun SettingsPane() {
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
                    // Geo-switch cache guard (2026-11-12): NEVER apply directly — stage
                    // the pick and show the mandatory cache-clear dialog instead.
                    onCountry = { v -> if (v != prefCountry) pendingCountry = v },
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
                    // 2026-11-12 (runtime fix): autoplay persisted, default ON (بأمر محمد).
                    autoplay = prefAutoplay,
                    onAutoplay = { v ->
                        prefAutoplay = v
                        try { prefs.edit().putBoolean("pref_autoplay", v).apply() } catch (_: Exception) { }
                    },
                    // 2026-11-12 (runtime layer): mini-player options, live-apply (no restart).
                    miniEnabled = prefMiniEnabled,
                    onMiniEnabled = { v ->
                        prefMiniEnabled = v
                        try { prefs.edit().putBoolean("pref_mini_enabled", v).apply() } catch (_: Exception) { }
                        // turning the bar off while collapsed strands playback behind no UI —
                        // open the full player so it stays reachable (same as autoshow-OFF rule).
                        if (!v && playing != null && !playerExpanded) playerExpanded = true
                    },
                    miniAutoshow = prefMiniAutoshow,
                    onMiniAutoshow = { v ->
                        prefMiniAutoshow = v
                        try { prefs.edit().putBoolean("pref_mini_autoshow", v).apply() } catch (_: Exception) { }
                    },
                    miniSwipe = prefMiniSwipe,
                    onMiniSwipe = { v ->
                        prefMiniSwipe = v
                        try { prefs.edit().putBoolean("pref_mini_swipe", v).apply() } catch (_: Exception) { }
                    },
                    afMode = prefAf,
                    onAf = { v ->
                        prefAf = v
                        try { prefs.edit().putString("pref_audiofocus", v).apply() } catch (_: Exception) { }
                        try {
                            if (v == "calls") {
                                val granted = ctx.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                                if (!granted) afPermLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                            }
                            applyAudioFocus()
                        } catch (_: Exception) { }
                    },
                    sbEnabled = prefSb,
                    onSb = { v ->
                        prefSb = v
                        try { prefs.edit().putBoolean("pref_sponsorblock", v).apply() } catch (_: Exception) { }
                    },
                    pipAuto = prefPip,
                    onPip = { v ->
                        prefPip = v
                        try { prefs.edit().putBoolean("pref_pip", v).apply() } catch (_: Exception) { }
                    },
                    favCount = favoriteKeys.size,
                    histCount = histCount,
                    plCount = playlists.size,
                    dbSize = dbSizeLabel(),
                    onClearFavorites = { clearFavorites() },
                    onClearHistory = { clearHistory() },
                    onCopyDiagnostics = { copyDiagnostics() },
                    onCheckUpdate = { checkUpdate() },
                    onExportFull = { exportFullBackup() },
                    onImportFull = { try { backupImportLauncher.launch("application/json") } catch (_: Exception) { } },
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
                    keepAlive = prefKeepalive,
                    onKeepAlive = { v ->
                        prefKeepalive = v
                        try { prefs.edit().putBoolean("pref_keepalive", v).apply() } catch (_: Exception) { }
                        try { com.mohamedadel.mytube.playback.KeepAliveService.sync(ctx.applicationContext) } catch (_: Exception) { }
                        if (!v) toast(ctx.getString(R.string.keepalive_off))
                    },
                    ageMode = prefAgeMode,
                    onAgeMode = { m ->
                        prefAgeMode = m
                        try { prefs.edit().putString("pref_age_mode", m).apply() } catch (_: Exception) { }
                    },
                    showComments = prefShowComments,
                    onShowComments = { v ->
                        prefShowComments = v
                        try { prefs.edit().putBoolean("pref_show_comments", v).apply() } catch (_: Exception) { }
                    },
                    showRelated = prefShowRelated,
                    onShowRelated = { v ->
                        prefShowRelated = v
                        try { prefs.edit().putBoolean("pref_show_related", v).apply() } catch (_: Exception) { }
                    },
                    showInfo = prefShowInfo,
                    onShowInfo = { v ->
                        prefShowInfo = v
                        try { prefs.edit().putBoolean("pref_show_info", v).apply() } catch (_: Exception) { }
                    },
                    showSubs = prefShowSubs,
                    onShowSubs = { v ->
                        prefShowSubs = v
                        try { prefs.edit().putBoolean("pref_show_subtitles", v).apply() } catch (_: Exception) { }
                    },
                    allow4k = prefAllow4k,
                    onAllow4k = { v ->
                        prefAllow4k = v
                        try { prefs.edit().putBoolean("pref_allow_4k", v).apply() } catch (_: Exception) { }
                    },
                    pinsCount = pinsCount,
                    pinsSize = pinsSizeLabel,
                    pinsBytes = pinsBytes,
                    pinsWarnMb = prefPinWarnMb,
                    pinLoc = pinSelLoc,
                    extAvailable = try { com.mohamedadel.mytube.data.local.PinFiles.isExternalAvailable(ctx.applicationContext) } catch (_: Exception) { false },
                    onPinLoc = { loc ->
                        pinSelLoc = loc
                        try { prefs.edit().putString("pref_pin_location", loc).apply() } catch (_: Exception) { }
                    },
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
                    // Deferred items 7+8: last-backup metadata row + periodic refresh toggle.
                    backupInfo = backupInfoText(),
                    feedAutoRefresh = prefFeedAuto,
                    onFeedAutoRefresh = { v ->
                        prefFeedAuto = v
                        try { prefs.edit().putBoolean("pref_feed_auto", v).apply() } catch (_: Exception) { }
                        try { syncFeedWorker(ctx.applicationContext, v) } catch (_: Exception) { }
                    },
                    preloadNext = prefPreloadNext,
                    onPreloadNext = { v ->
                        prefPreloadNext = v
                        try { prefs.edit().putBoolean("pref_preload_next", v).apply() } catch (_: Exception) { }
                        if (!v) try { prefetched.clear() } catch (_: Exception) { }
                    },
                    preloadWifiOnly = prefPreloadWifiOnly,
                    titleTranslate = prefTitleTranslate,
                    onTitleTranslate = { v ->
                        prefTitleTranslate = v
                        try { prefs.edit().putBoolean("pref_title_translate", v).apply() } catch (_: Exception) { }
                    },
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
                    subScale = prefSubScale,
                    onSubScale = { v -> prefSubScale = v; try { prefs.edit().putFloat("pref_sub_scale", v.coerceIn(0.5f, 2.0f)).apply() } catch (_: Exception) { } },
                    subBgOpacity = prefSubBgOpacity,
                    onSubBgOpacity = { v -> prefSubBgOpacity = v; try { prefs.edit().putInt("pref_sub_bg_opacity", v.coerceIn(0, 100)).apply() } catch (_: Exception) { } },
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
                        // processor chain is fixed at player build: stop the service, WAIT for
                        // death, then recreate — the old fire-and-forget raced a stale player (major #6).
                        try { ctx.stopService(android.content.Intent(ctx, com.mohamedadel.mytube.playback.PlaybackService::class.java)) } catch (_: Exception) { }
                        scope.launch {
                            try { kotlinx.coroutines.delay(900) } catch (_: Exception) { }
                            try { withContext(kotlinx.coroutines.Dispatchers.Main) { (ctx as? android.app.Activity)?.recreate() } } catch (_: Exception) { }
                        }
                    },
                    speed = prefSpeed,
                    onSpeed = { v -> prefSpeed = v; try { prefs.edit().putFloat("pref_speed", v).apply() } catch (_: Exception) { } },
                    pitch = prefPitch,
                    onPitch = { v -> prefPitch = v; try { prefs.edit().putFloat("pref_pitch", v).apply() } catch (_: Exception) { } },
                    theme = prefTheme,
                    onTheme = { v ->
                        prefTheme = v
                        try { prefs.edit().putString("pref_theme", v).apply() } catch (_: Exception) { }
                        try { themeLive.value = v } catch (_: Exception) { }
                    },
                    accent = prefAccent,
                    onAccent = { v ->
                        prefAccent = v
                        try { prefs.edit().putString("pref_accent", v).apply() } catch (_: Exception) { }
                        try { accentLive.value = v } catch (_: Exception) { }
                    },
                    // Restart-prompt (11 Nov 2026 — بأمر محمد): dialog choice after theme/accent change.
                    onRestartNow = { (ctx as? android.app.Activity)?.let { restartAppNow(it) } },
                    volume = volumeLive.value,
                    muted = mutedLive.value,
                    onVolume = { v ->
                        try { volumeLive.value = v.coerceIn(0f, 1f) } catch (_: Exception) { }
                        if (v > 0f) try { mutedLive.value = false } catch (_: Exception) { }
                        try { prefs.edit().putFloat("pref_volume", v.coerceIn(0f, 1f)).apply() } catch (_: Exception) { }
                    },
                    onMuted = { m ->
                        try { mutedLive.value = m } catch (_: Exception) { }
                        try { prefs.edit().putBoolean("pref_muted", m).apply() } catch (_: Exception) { }
                    },
                    eqSupported = eqSupported,
                    eqEnabled = eqEnabled,
                    onEqEnabled = { on ->
                        eqEnabled = on
                        try { prefs.edit().putBoolean("eq_enabled", on).apply() } catch (_: Exception) { }
                        try { applyEqPrefs() } catch (_: Exception) { }
                    },
                    eqPresetNames = eqPresetNames,
                    eqPreset = eqPreset,
                    onEqPreset = { i ->
                        eqPreset = i
                        try { prefs.edit().putInt("eq_preset", i).apply() } catch (_: Exception) { }
                        try { applyEqPrefs() } catch (_: Exception) { }
                    },
                    eqLabels = eqLabels,
                    eqBands = eqCustom.toList(),
                    onEqBand = { b, v ->
                        try {
                            while (eqCustom.size <= b) eqCustom.add(0.5f)
                            eqCustom[b] = v.coerceIn(0f, 1f)
                            if (eqPreset != com.mohamedadel.mytube.playback.EqManager.CUSTOM) {
                                eqPreset = com.mohamedadel.mytube.playback.EqManager.CUSTOM
                                prefs.edit().putInt("eq_preset", eqPreset).apply()
                            }
                            prefs.edit().putString("eq_custom", eqCustom.joinToString(",")).apply()
                            applyEqPrefs()
                        } catch (_: Exception) { }
                    }
                )
                // Geo-switch cache guard dialog (2026-11-12, runtime layer): mandatory
                // confirmation before a content-country switch. REFUSE (dismiss/cancel)
                // keeps the OLD country — prefCountry untouched, no partial state.
                // ACCEPT clears extractor + page caches, then applies + reloads.
                // Extracted to CountryGuardDialog: this host method is at the 64KB limit.
                val pending = pendingCountry
                if (pending != null) {
                    com.mohamedadel.mytube.ui.settings.CountryGuardDialog(
                        pending = pending,
                        pendingName = com.mohamedadel.mytube.ui.settings.COUNTRY_OPTIONS.firstOrNull { it.first == pending }?.second?.let { ctx.getString(it) } ?: pending,
                        onDismiss = { pendingCountry = null },
                        onConfirm = { v -> confirmCountrySwitch(v) }
                    )
                }
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
                        onPlay = { playVideo(it, expand = true, forcePlay = true) },
                        onLongClick = { optionsFor = it },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) },
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        progressOf = { k -> pinProgress[k] },
                        subsLabel = { n -> if (n != null && n > 0) ctx.getString(R.string.subs_count_fmt, n) else "" },
                        shorts = channelShorts,
                        shortsLoading = channelShortsLoading,
                        live = channelLive,
                        liveLoading = channelLiveLoading,
                        channelPlaylists = channelPls,
                        channelPlaylistsLoading = channelPlsLoading,
                        onTabSelect = { i -> channelScreenUrl?.let { loadChannelTab(i, it) } },
                        onOpenPlaylist = { openRemotePlaylist(it) }
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
                                // 2026-11-12 (runtime fix 3): canonical key (F11) — id/url matching
                                // let dupes slip the position after reorder.
                                queueIndex = m.indexOfFirst { it.favKey() == playing?.favKey() }
                            }
                        },
                        onRemove = { idx ->
                            if (idx in queue.indices) {
                                queue = queue.toMutableList().also { it.removeAt(idx) }
                                // 2026-11-12 (runtime fix 3): keep the index glued to the
                                // playing item after a removal ahead of it (was: stale index
                                // → next/prev + prefetch pointed at the wrong item).
                                if (queue.isEmpty()) queueIndex = -1
                                else {
                                    if (idx < queueIndex) queueIndex--
                                    queueIndex = queueIndex.coerceIn(0, queue.size - 1)
                                }
                            }
                        },
                        // Deferred item 3: swipe-to-remove Undo re-inserts at the original index.
                        onInsert = { idx, v ->
                            val at = idx.coerceIn(0, queue.size)
                            queue = queue.toMutableList().also { it.add(at, v) }
                        },
                        onPlayFrom = { idx -> if (idx in queue.indices) playVideo(queue[idx], inQueue = true, expand = true, forcePlay = true) },
                        onOptions = { optionsFor = it },
                        // pin-deepfix 2026-11-11: badges + live % in queue rows.
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        progressOf = { k -> pinProgress[k] },
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
                        ,
                        // screens-fix Music P1-M4: visible radio state + stop.
                        radioOn = radioOn,
                        onStopRadio = { radioOn = false }
                        ,
                        // screens-fix Library P2-L13: clear-all (queue itself is session-only by design).
                        onClearAll = { queue = emptyList(); queueIndex = 0 }
                    )
                }
                // 2026-11-12 (runtime fix): expanded player fills the screen — plain
                // destinations hide so the player's ScrollableColumn gets a bounded
                // height (queue + channel stay visible above for management/back).
                playerExpanded && !showQueue && channelScreenUrl == null -> { }
                showLibrary -> {
                    // screens-fix Library P2-L12: load most-replayed when the hub opens (IO, silent fail).
                    LaunchedEffect(showLibrary) {
                        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try { topPlayedHome = dao.topPlayed(6) } catch (_: Exception) { }
                        }
                    }
                    LibraryScreen(
                        onHistory = { showLibrary = false; fromLibrary = true; showHistory = true },
                        onDownloads = { showLibrary = false; fromLibrary = true; showDownloads = true },
                        onGroups = { showLibrary = false; fromLibrary = true; showGroups = true },
                        groupCount = groups.size,
                        pinCount = pins.size,
                        onFavorites = { showLibrary = false; fromLibrary = true; showFavorites = true },
                        onPlaylists = { showLibrary = false; fromLibrary = true; showPlaylists = true },
                        onLocalAudio = { showLibrary = false; fromLibrary = true; showLocal = true; checkLocalPerm(); refreshLocalAudio() },
                        subscriptions = subscriptions,
                        onOpenFeed = { showLibrary = false; fromLibrary = true; showFeed = true; if (feedItems.isEmpty()) refreshFeed() },
                        onUnsubscribe = { url -> scope.launch { try { dao.deleteSubscription(url) } catch (_: Exception) { } } },
                        onImportSubs = { try { subsImportLauncher.launch("*/*") } catch (_: Exception) { } },
                        feedCounts = feedCounts,
                        onOpenChannel = { url -> openChannel(url) },
                        topPlayed = topPlayedHome,
                        onPlayTop = { v -> playVideo(v, expand = true, forcePlay = true) }
                    )
                }
                showNotifications -> {
                    NotificationsScreen(
                        items = notifications,
                        loading = feedLoading,
                        onRefresh = { refreshFeed() },
                        onPlay = { n ->
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try { dao.markNotificationRead(n.videoKey) } catch (_: Exception) { }
                            }
                            // Tapping a notification IS an explicit tap -> forcePlay allowed.
                            playVideo(with(NotifCenter) { n.toVideoItem() }, inQueue = false, expand = true, forcePlay = true)
                        }
                    )
                }
                showDownloads -> {
                    com.mohamedadel.mytube.ui.library.DownloadsRoute(
                        pins = pins,
                        totalBytes = try { com.mohamedadel.mytube.data.local.PinFiles.totalBytes(ctx.applicationContext) } catch (_: Exception) { 0L },
                        pinningKeys = pinningKeys,
                        progressMap = pinProgress,
                        thumbLookup = { k -> try { history.firstOrNull { it.videoKey == k }?.thumbnail } catch (_: Exception) { null } },
                        onPlay = { v -> showDownloads = false; playVideo(v, expand = true, forcePlay = true) },
                        onDelete = { k -> doUnpin(k) },
                        onClearAll = { clearAllPins() },
                        onCancel = { k -> cancelPinDownload(k) },
                        pausedKeys = pausedKeys,
                        onPause = { k -> pausePinDownload(k) },
                        onResume = { k -> resumePinDownload(k) },
                        titleLookup = { k -> try { history.firstOrNull { it.videoKey == k }?.title } catch (_: Exception) { null } },
                        orphanCount = orphanInfo.first,
                        orphanBytes = orphanInfo.second,
                        onCleanOrphans = { cleanOrphans() }
                    )
                }
                showLocal -> {
                    com.mohamedadel.mytube.ui.library.LocalAudioRoute(
                        tracks = localTracks,
                        folderFilter = localFolder,
                        onFolder = { localFolder = it },
                        onExcludeFolder = { f -> excludeLocalFolder(f) },
                        excluded = localExcl,
                        onUnexclude = { f -> unexcludeLocalFolder(f) },
                        compat = localCompat,
                        onCompat = { c ->
                            localCompat = c
                            try { prefs.edit().putBoolean("local_scan_compat", c).apply() } catch (_: Exception) { }
                            refreshLocalAudio()
                        },
                        needsPermission = !localHasPerm,
                        onRequestPermission = { try { localPermLauncher.launch(localAudioPerm()) } catch (_: Exception) { } },
                        onRescan = { refreshLocalAudio() },
                        onBack = { smartBack() },
                        onPlay = { v -> showLocal = false; fromLibrary = false; playVideo(v, expand = true, forcePlay = true) },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) },
                        onLongClick = { optionsFor = it },
                        // screens-fix Music P1-M8: highlight the playing track.
                        playingUrl = playing?.url,
                        isPlaying = isPlaying
                    )
                }
                showGroups -> {
                    com.mohamedadel.mytube.ui.library.GroupsScreen(
                        groups = groups,
                        counts = groupCounts,
                        onOpen = { openGroup = it; showGroups = false },
                        onCreate = { createGroup(it) },
                        onRename = { g, n -> renameGroup(g, n) },
                        onDelete = { deleteGroup(it) },
                        onBack = { smartBack() }
                    )
                }
                openGroup != null -> {
                    // T5: no !! on delegated state — null falls back to list.
                    val g = openGroup
                    if (g != null) com.mohamedadel.mytube.ui.library.GroupDetailScreen(
                        group = g,
                        members = groupMembers,
                        subscriptions = subscriptions,
                        onRemove = { removeFromGroup(g.id, it) },
                        onAdd = { addToGroup(g.id, it) },
                        onOpenChannel = { openChannel(it) },
                        onOpenFeed = {
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                val urls = try { dao.memberUrls(g.id) } catch (_: Exception) { emptyList() }
                                openGroupFeed(g, urls.toSet())
                            }
                        },
                        onBack = { smartBack() }
                    )
                }
                showFeed -> {
                    FeedScreen(
                        items = feedItems,
                        loading = feedLoading,
                        onRefresh = { refreshFeed() },
                        filterChannelUrl = feedFilter,
                        filterChannelName = feedGroupName ?: feedFilter?.let { u -> subscriptions.firstOrNull { it.channelUrl == u }?.name },
                        onClearFilter = { feedFilter = null; feedGroupUrls = null; feedGroupName = null },
                        filterUrls = feedGroupUrls ?: emptySet(),
                        filterLabel = feedGroupName,
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        progressOf = { k -> pinProgress[k] },
                        onLongClick = { optionsFor = it },
                        onPlay = { playVideo(it, expand = true, forcePlay = true) },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) },
                    )
                }
                showSC -> {
                    com.mohamedadel.mytube.ui.library.MusicTab(
                        scQuery = scQuery,
                        scLoading = scLoading,
                        recentMusicSearches = recentMusicSearches,
                        scSource = scSource,
                        scKind = scKind,
                        scResults = scResults,
                        scError = scError,
                        scSuggestion = scSuggestion,
                        scCorrected = scCorrected,
                        remotePlaylists = remotePlaylists,
                        scHasMore = scHasMore,
                        scLoadingMore = scLoadingMore,
                        tasteProfile = tasteProfile,
                        tasteMix = tasteMix,
                        tasteMixTerm = tasteMixTerm,
                        tasteMixLoading = tasteMixLoading,
                        playingUrl = playing?.url,
                        isPlaying = isPlaying,
                        favoriteSet = favoriteSet,
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        progressOf = { k -> pinProgress[k] },
                        scListState = scListState,
                        onQueryChange = { scQuery = it },
                        onSearch = { q -> doScSearch(q) },
                        onClose = { goHome() },
                        onClear = { scQuery = ""; scResults = emptyList(); scError = null },
                        onPickSuggestion = { picked -> doScSearch(picked) },
                        onClearRecents = { recentMusicSearches = emptyList(); try { prefs.edit().remove("recent_music_searches").apply() } catch (_: Exception) { } },
                        onVoice = { startVoiceSearch() },
                        onSourceSelect = { src -> scSource = src; scResults = emptyList(); scError = null; scSuggestion = null; scope.launch { try { scListState.scrollToItem(0) } catch (_: Exception) { } }; if (scQuery.isNotBlank()) doScSearch(scQuery, src) },
                        onKindSelect = { k -> if (scKind != k) { scKind = k; remotePlaylists = emptyList(); scResults = emptyList(); scError = null; scSuggestion = null; scope.launch { try { scListState.scrollToItem(0) } catch (_: Exception) { } }; if (scQuery.isNotBlank()) doScSearch(scQuery, 1, k) } },
                        onTerm = { t -> scQuery = t; doScSearch(t) },
                        onPlayVideo = { v -> playVideo(v, expand = true, forcePlay = true) },
                        onPlayMix = { v -> if (v.isChannel) openChannel(v.channelUrl ?: v.url) else playVideo(v, expand = true, forcePlay = true) },
                        onOpenChannel = { url -> openChannel(url) },
                        onOptions = { optionsFor = it },
                        onToggleFav = { toggleFavorite(it) },
                        onOpenRemotePlaylist = { p -> openRemotePlaylist(p) },
                        onLoadMore = { loadScMore() }
                    )
                }
                showSettings -> { SettingsPane() }
                showHistory -> {
                    HistoryScreen(
                        flowAll = histFlowAll,
                        flowPinned = histFlowPinned,
                        flowUnpinned = histFlowUnpinned,
                        onPlay = { playVideo(it, expand = true, forcePlay = true) },
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
                        progressOf = { k -> pinProgress[k] },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) }
                    )
                }
                showFavorites -> {
                    FavoritesScreen(
                        items = favorites,
                        onPlay = { playVideo(it, expand = true, forcePlay = true) },
                        onToggleFav = { toggleFavorite(it) },
                        onClear = { clearFavorites() },
                        onBack = { smartBack() },
                        onLongClick = { optionsFor = it },
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        progressOf = { k -> pinProgress[k] },
                        onPlayAll = {
                            val list = favorites.map { it.toVideoItem() }
                            if (list.isNotEmpty()) {
                                queue = list
                                playVideo(list[0], inQueue = true, expand = true, forcePlay = true)
                            }
                        },
                        onRadio = {
                            val first = favorites.firstOrNull()?.toVideoItem()
                            if (first != null) startRadio(first)
                        }
                    )
                }
                showPlaylists -> {
                    PlaylistsRoute()
                }
                openRemote != null -> {
                    // T5: no !! on delegated state — null renders nothing.
                    val rp = openRemote
                    if (rp != null) com.mohamedadel.mytube.ui.results.RemoteRoute(
                        rp = rp,
                        loading = openRemoteLoading,
                        items = openRemoteItems,
                        playingUrl = playing?.url,
                        isPlaying = isPlaying,
                        favSet = favoriteSet,
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        progressOf = { k -> pinProgress[k] },
                        onBack = { openRemote = null; openRemoteItems = emptyList() },
                        onPlayAll = { list ->
                            results = list
                            queue = list
                            currentQuery = ""
                            openRemote = null
                            playVideo(list[0], inQueue = true, expand = true, forcePlay = true)
                        },
                        onPlay = { playVideo(it, expand = true, forcePlay = true) },
                        onLongClick = { optionsFor = it },
                        onFav = { toggleFavorite(it) },
                        // screens-fix Library P1-L5: remote -> local save + pin-all.
                        // screens-fix Library P2 (method-size): body lives in a local fun, not inline.
                        onSave = { list -> saveRemoteList(list) },
                        onPinAll = { list -> scope.launch { for (v in list) { try { quickPin(v, null) } catch (_: Exception) { } } } },
                    ) else { openRemote = null }
                }
                loading -> {
                    com.mohamedadel.mytube.ui.results.VideoListSkeleton(count = 5)
                }
                results.isEmpty() && query.isBlank() -> {
                    HomeEmptyState(
                        trending = trending,
                        trendTab = trendTab,
                        onTrendTab = { i ->
                            // screens-fix Home P1-H5: cancel stale suggestions before switching.
                            suggestJob?.cancel()
                            trendTab = i
                            refreshTrending()
                        },
                        trendingItems = trendingItems,
                        trendingLoading = trendingLoading,
                        trendingError = trendingError,
                        onRetryTrending = { refreshTrending() },
                        // Deferred item 1: pagination + pull-to-refresh wiring.
                        hasMore = trendingHasMore,
                        loadingMore = trendingLoadingMore,
                        onShowMore = { loadMoreTrending() },
                        countryName = COUNTRY_OPTIONS.firstOrNull { it.first == prefCountry }?.second?.let { ctx.getString(it) } ?: prefCountry,
                        // Official section header (2026-11-12): this tab's own label;
                        // music tab also badges every track (surface a).
                        sectionTitle = TREND_TABS.getOrNull(trendTab)?.first?.let { ctx.getString(it) },
                        showMusicBadge = TREND_TABS.getOrNull(trendTab)?.second == "trending_music",
                        onSearch = { q -> query = q; doSearch(q) },
                        isSuggestionsTab = TREND_TABS.getOrNull(trendTab)?.second == "suggestions",
                        suggestionChips = suggestionChips,
                        onSuggestion = { q -> query = q; doSearch(q) },
                        onPlay = { playVideo(it, expand = true, forcePlay = true) },
                        isFav = { v -> v.favKey() in favoriteSet },
                        onFav = { toggleFavorite(it) },
                        onLongClick = { optionsFor = it },
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        progressOf = { k -> pinProgress[k] }
                    )
                }
                results.isEmpty() && !loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                run { val se = searchError; if (se != null) se else stringResource(R.string.no_results) },
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
                                listOf("all", "videos", "songs", "channels", "playlists", "albums").forEach { f ->
                                    androidx.compose.material3.FilterChip(
                                        selected = prefContentFilter == f,
                                        onClick = {
                                            prefContentFilter = f
                                            try { prefs.edit().putString("pref_content_filter", f).apply() } catch (_: Exception) { }
                                            if (currentQuery.isNotBlank()) doSearch(currentQuery)
                                        },
                                        label = { Text(stringResource(when (f) { "videos" -> R.string.filter_videos; "songs" -> R.string.filter_songs; "channels" -> R.string.filter_channels; "playlists" -> R.string.filter_playlists; "albums" -> R.string.filter_albums; else -> R.string.filter_all })) }
                                    )
                                }
                            }
                        }
                        // remote playlists (service playlists for the browsed service)
                        if (remotePlaylists.isNotEmpty()) {
                            item {
                                Text(
                                    stringResource(R.string.remote_playlists_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                            items(items = remotePlaylists, key = { p -> p.url ?: p.title ?: p.hashCode().toString() }) { p ->
                                com.mohamedadel.mytube.ui.results.PlaylistCard(p = p, onClick = { openRemotePlaylist(p) })
                            }
                        }
                        // search correction (own item: never breaks the list)
                        // T5: local val instead of searchSuggestion!!.
                        val ss = searchSuggestion
                        if (ss != null) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { doSearch(ss) }.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        if (searchCorrected) stringResource(R.string.did_you_mean_showing_fmt, ss) else stringResource(R.string.did_you_mean_fmt, ss),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                        }
                        item {
                            Text(stringResource(R.string.results_found_fmt, results.size), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp))
                        }
                        // screens-fix Home P0-H3: stable keys (no index prefix).
                        itemsIndexed(items = results, key = { _, v -> v.id ?: v.url ?: v.title ?: v.hashCode().toString() }) { _, v ->
                            SearchRow(
                                v = v,
                                isCurrent = playing?.id == v.id || playing?.url == v.url,
                                isPlaying = isPlaying && (playing?.id == v.id || playing?.url == v.url),
                                isFav = v.favKey() in favoriteSet,
                                onClick = { playVideo(v, expand = true, forcePlay = true) },
                                onLongClick = { optionsFor = it },
                                onFav = { toggleFavorite(it) },
                                pinBadge = pinBadges[v.favKey()],
                                pinning = pinningKeys.contains(v.favKey()),
                                pinProgress = pinProgress[v.favKey()]
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
                        sleepEndOfTrack = false
                        sleepTrackKey = null // F12: a fresh timer disarms any track-bound flag
                        sleepAtMs = if (m == null) null else android.os.SystemClock.uptimeMillis() + m * 60000L
                        if (m == null) {
                            sleepLeft = ""
                            try { prefs.edit().remove("sleep_deadline").apply() } catch (_: Exception) { }
                            // F1: cancel the service-side deadline too.
                            try { PlaybackService.cancelSleep() } catch (_: Exception) { }
                        } else {
                            try { prefs.edit().putLong("sleep_deadline", sleepAtMs ?: 0L).apply() } catch (_: Exception) { }
                            // F1: service-side deadline survives Activity death (Compose watcher is UI-only).
                            try { PlaybackService.scheduleSleep(m * 60000L) } catch (_: Exception) { }
                        }
                        showSleep = false
                    },
                    onEndOfTrack = {
                        sleepAtMs = null
                        try { prefs.edit().remove("sleep_deadline").apply() } catch (_: Exception) { }
                        sleepTrackKey = playing?.favKey() // F12: bind the flag to this track
                        sleepEndOfTrack = true
                        sleepLeft = try { ctx.getString(R.string.sleep_track) } catch (_: Exception) { "..." }
                        showSleep = false
                    },
                    onDismiss = { showSleep = false }
                )
            }
            // age-gate warn dialog lives in the library file (Scaffold content is at the 64KB limit)
            agePending?.let { ap ->
                com.mohamedadel.mytube.ui.library.AgeWarnDialog(
                    title = ap.first.title,
                    onPlay = { val x = ap; agePending = null; playVideo(x.first, inQueue = x.second, ageOk = true, pre = x.third, forcePlay = true) },
                    onDismiss = { agePending = null }
                )
            }
            // pin confirm: own file (Scaffold content is at the 64KB method limit)
            pinConfirm?.let { pc ->
                com.mohamedadel.mytube.ui.library.PinConfirmDialog(
                    title = pc.v.title,
                    sd = pinSd,
                    selLoc = pinSelLoc,
                    selQuality = pinSelQuality,
                    selAudio = pinSelAudio,
                    selSubs = pinSelSubs,
                    maxH = if (prefAllow4k) Int.MAX_VALUE else 1080,
                    estBytes = pinEst,
                    sizeUnknown = stringResource(R.string.pin_size_unknown),
                    sizeFmt = { mb -> ctx.getString(R.string.pin_size_fmt, mb) },
                    extOk = try { com.mohamedadel.mytube.data.local.PinFiles.isExternalAvailable(ctx.applicationContext) } catch (_: Exception) { false },
                    onLoc = { loc ->
                        pinSelLoc = loc
                        try { prefs.edit().putString("pref_pin_location", loc).apply() } catch (_: Exception) { }
                    },
                    onQuality = { pinSelQuality = it },
                    onAudio = { pinSelAudio = it },
                    onSubs = { pinSelSubs = it },
                    onEst = { pinEst = it },
                    onConfirm = { confirmPin() },
                    onDismiss = { pinConfirm = null }
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
                    onQuickPin = {
                        if (!pinKeys.contains(v.favKey())) { optionsFor = null; quickPin(v, null) }
                    },
                    isPinned = pinKeys.contains(v.favKey()),
                    isPinning = pinningKeys.contains(v.favKey()),
                    onSubscribe = if (v.channelUrl != null) ({ toggleSubscription(v.channelUrl, v.channel, v.channelAvatar); optionsFor = null }) else null,
                    onShare = { try { ctx.shareVideoCtx(v) } catch (_: Exception) { }; optionsFor = null },
                    onStartRadio = { startRadio(v); optionsFor = null },
                    isSubscribed = v.channelUrl != null && subscriptions.any { it.channelUrl == v.channelUrl },
                    onDismiss = { optionsFor = null }
                )
            }
            // Deferred item 6: update dialog — version + changelog + Download (was toast-only).
            updateInfo?.let { rel ->
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { updateInfo = null },
                    title = { Text(stringResource(R.string.update_available_fmt, rel.tag, com.mohamedadel.mytube.BuildConfig.VERSION_NAME)) },
                    text = {
                        androidx.compose.foundation.layout.Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                            Text(
                                (rel.body?.take(2000)?.ifBlank { rel.tag } ?: rel.tag),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(onClick = {
                            try {
                                val url = rel.htmlUrl
                                if (!url.isNullOrBlank()) {
                                    val view = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    view.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    ctx.startActivity(view)
                                } else uiToast(ctx.getString(R.string.update_fail))
                            } catch (_: Exception) { uiToast(ctx.getString(R.string.update_fail)) }
                            updateInfo = null
                        }) { Text(stringResource(R.string.download)) }
                    },
                    dismissButton = {
                        androidx.compose.material3.TextButton(onClick = { updateInfo = null }) { Text(stringResource(R.string.close)) }
                    }
                )
            }
        }
        }
    }
}

// Estimated downstream bandwidth in Kbps from the active network (0 = unknown).

