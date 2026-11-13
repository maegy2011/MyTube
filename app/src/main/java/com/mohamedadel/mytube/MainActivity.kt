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
import com.mohamedadel.mytube.util.QUALITY_AUDIO_ONLY
import com.mohamedadel.mytube.util.autoKeyFor
import com.mohamedadel.mytube.util.cap4k
import com.mohamedadel.mytube.util.matchSubIdxsFor
import com.mohamedadel.mytube.util.pickAudioIdxFor
import com.mohamedadel.mytube.util.pickStartQuality
import com.mohamedadel.mytube.util.resolveLang
import com.mohamedadel.mytube.util.resolvePlaybackUrl
import com.mohamedadel.mytube.ui.player.MiniPlayer
import com.mohamedadel.mytube.ui.player.PlayerHost
import com.mohamedadel.mytube.ui.player.enqueueLast
import com.mohamedadel.mytube.ui.player.enqueueNext
import com.mohamedadel.mytube.ui.player.isQueued
import com.mohamedadel.mytube.ui.player.playFirstInQueue
import com.mohamedadel.mytube.ui.player.playNextInQueue
import com.mohamedadel.mytube.ui.player.playNowAlone
import com.mohamedadel.mytube.ui.player.playPrevInQueue
import com.mohamedadel.mytube.ui.player.playRandomInQueue
import com.mohamedadel.mytube.ui.player.startRadio
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
import com.mohamedadel.mytube.data.local.MIGRATION_10_11
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
    val msearchVm: com.mohamedadel.mytube.ui.search.MainSearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(key = "main")
    val currentQuery by msearchVm.currentQuery.collectAsStateWithLifecycle()
    val results by msearchVm.results.collectAsStateWithLifecycle()
    val loading by msearchVm.loading.collectAsStateWithLifecycle()
    val loadingMore by msearchVm.loadingMore.collectAsStateWithLifecycle()
    val hasMore by msearchVm.hasMore.collectAsStateWithLifecycle()
    val nextPage by msearchVm.nextPage.collectAsStateWithLifecycle()
    // search content filter: all | videos | channels | playlists (persisted; no sort API exists)
    val settingsVm: com.mohamedadel.mytube.ui.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(key = "settings")
    val prefContentFilter by settingsVm.prefContentFilter.collectAsStateWithLifecycle()
    val playerVm: com.mohamedadel.mytube.ui.player.PlayerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val playing by playerVm.playing.collectAsStateWithLifecycle()
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
    val autoAdvance by playerVm.autoAdvance.collectAsStateWithLifecycle()
    // Radio (endless related queue): seeded from StreamData.related, refilled at every
    // queue end from the just-played track's own related list. Local-only, no account.
    val radioOn by playerVm.radioOn.collectAsStateWithLifecycle()
    val queueIndex by playerVm.queueIndex.collectAsStateWithLifecycle()
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
    val recentSearches by msearchVm.recentSearches.collectAsStateWithLifecycle()
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
    val queue by playerVm.queue.collectAsStateWithLifecycle()
    // playlists (Room) + playback modes. Modes persist in prefs; playlists persist in Room.
    val db = remember {
        // Release safety: destructive fallback ONLY in debug. A shipped schema change
        // without a written migration must crash loudly in testing, never wipe user data.
        val builder = Room.databaseBuilder(ctx.applicationContext, MyTubeDb::class.java, "mytube.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
        if (com.mohamedadel.mytube.BuildConfig.DEBUG) builder.fallbackToDestructiveMigration()
        builder.build()
    }
    val dao = remember { db.playlists() }
    val csvVm: com.mohamedadel.mytube.ui.library.PlaylistCsvViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val backupVm: com.mohamedadel.mytube.ui.backup.BackupViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val sysVm: com.mohamedadel.mytube.ui.system.SystemViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val libVm: com.mohamedadel.mytube.ui.library.LibraryViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val feedVm: com.mohamedadel.mytube.ui.feed.FeedViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    androidx.compose.runtime.LaunchedEffect(Unit) { feedVm.configure(dao) }
    androidx.compose.runtime.LaunchedEffect(Unit) { libVm.configure(dao) }
    androidx.compose.runtime.LaunchedEffect(Unit) { csvVm.configure(dao) }
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
    val groupCounts by libVm.groupCounts.collectAsStateWithLifecycle()
    var feedGroupUrls by remember { mutableStateOf<Set<String>?>(null) }
    var feedGroupName by remember { mutableStateOf<String?>(null) }
    // Channel extra tabs (lazy per tab)
    val chVm: com.mohamedadel.mytube.ui.channel.ChannelViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val channelShorts by chVm.channelShorts.collectAsStateWithLifecycle()
    val channelShortsLoading by chVm.channelShortsLoading.collectAsStateWithLifecycle()
    val channelLive by chVm.channelLive.collectAsStateWithLifecycle()
    val channelLiveLoading by chVm.channelLiveLoading.collectAsStateWithLifecycle()
    val channelPls by chVm.channelPls.collectAsStateWithLifecycle()
    val channelPlsLoading by chVm.channelPlsLoading.collectAsStateWithLifecycle()
    val favoriteSet = remember(favoriteKeys) { favoriteKeys.toSet() }
    val favorites by remember { dao.observeFavorites() }.collectAsStateWithLifecycle(initialValue = emptyList())
    // (toggleFavorite lives after `scope` below — Kotlin locals must precede use.)
    // language prefs: "auto" (device language), "off" (subtitles only), "original" (video's own language),
    // or a language tag (ar/en/…). Secondary = fallback when the primary is unavailable.
    val prefSubLang by settingsVm.prefSubLang.collectAsStateWithLifecycle()
    val prefAudioLang by settingsVm.prefAudioLang.collectAsStateWithLifecycle()
    val prefSubLang2 by settingsVm.prefSubLang2.collectAsStateWithLifecycle()
    val prefAudioLang2 by settingsVm.prefAudioLang2.collectAsStateWithLifecycle()
    // content country (manual only, no IP): drives search/trending region. "global" = YouTube default.
    val prefCountry by settingsVm.prefCountry.collectAsStateWithLifecycle()
    // Geo-switch cache guard (2026-11-12, runtime layer): the newly picked country
    // waits here until the user confirms the cache-clear dialog; refuse = null
    // (old value kept, no partial state), accept = caches cleared then applied.
    var pendingCountry by remember { mutableStateOf<String?>(null) }
    // app theme: dark (default) | light (large-type friendly) | system
    val prefTheme by settingsVm.prefTheme.collectAsStateWithLifecycle()
    val prefAccent by settingsVm.prefAccent.collectAsStateWithLifecycle()
    val prefPip by settingsVm.prefPip.collectAsStateWithLifecycle()
    // 2026-11-12 (runtime layer): mini-player options — master switch (default ON),
    // auto-show on play (default ON), swipe-to-dismiss (default ON). Live-apply, no restart.
    val prefMiniEnabled by settingsVm.prefMiniEnabled.collectAsStateWithLifecycle()
    val prefMiniAutoshow by settingsVm.prefMiniAutoshow.collectAsStateWithLifecycle()
    val prefMiniSwipe by settingsVm.prefMiniSwipe.collectAsStateWithLifecycle()
    // Audio focus: "all" (pause on any interruption, ExoPlayer handles) vs "calls"
    // (ignore other apps, pause only for phone calls via telephony callback).
    val prefAf by settingsVm.prefAf.collectAsStateWithLifecycle()
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
    val prefSb by settingsVm.prefSb.collectAsStateWithLifecycle()
    var sbSegments by remember { mutableStateOf(emptyList<SbSeg>()) }
    // UI language: ar (default, RTL) | en (LTR). Applies via recreate().
    val prefUiLang by settingsVm.prefUiLang.collectAsStateWithLifecycle()
    // default video quality for every new video: audio-only | auto | saver | high
    val prefQuality by settingsVm.prefQuality.collectAsStateWithLifecycle()
    // 2026-11-12 (runtime fix): persisted autoplay toggle, default ON (بأمر محمد) —
    // drives radio/queue auto-advance; OFF stops automatic advance. Declared up here
    // (next to the other prefs) because the prefs-load LaunchedEffect below assigns it.
    val prefAutoplay by settingsVm.prefAutoplay.collectAsStateWithLifecycle()
    // subtitle studio: hide auto-generated, fg/bg/edge/size/bold (applied via CaptionStyleCompat)
    val prefSubHideAuto by settingsVm.prefSubHideAuto.collectAsStateWithLifecycle()
    val prefSubFg by settingsVm.prefSubFg.collectAsStateWithLifecycle()
    val prefSubBg by settingsVm.prefSubBg.collectAsStateWithLifecycle()
    val prefSubEdge by settingsVm.prefSubEdge.collectAsStateWithLifecycle()
    val prefSubScale by settingsVm.prefSubScale.collectAsStateWithLifecycle()
    val prefSubBgOpacity by settingsVm.prefSubBgOpacity.collectAsStateWithLifecycle()
    val prefSubBold by settingsVm.prefSubBold.collectAsStateWithLifecycle()
    // audio studio: prefer original + playback speed/pitch
    val prefPreferOriginal by settingsVm.prefPreferOriginal.collectAsStateWithLifecycle()
    // B2 precise track roles: sub role (subtitle/caption/descriptive/system) + forced-sub kill + undetermined
    val prefSubRole by settingsVm.prefSubRole.collectAsStateWithLifecycle()
    val prefHideForced by settingsVm.prefHideForced.collectAsStateWithLifecycle()
    val prefUndetermined by settingsVm.prefUndetermined.collectAsStateWithLifecycle()
    val prefAudioRole by settingsVm.prefAudioRole.collectAsStateWithLifecycle()
    // B3 silence skipping (ExoPlayer SilenceSkippingAudioProcessor via custom RenderersFactory)
    val prefSkipSilence by settingsVm.prefSkipSilence.collectAsStateWithLifecycle()
    // Phase 1: transient media-cache cap (MB) + live size label
    val prefCacheMaxMb by settingsVm.prefCacheMaxMb.collectAsStateWithLifecycle()
    // Phase 4: offline mode — pinned items play from disk, unpinned show "pin first"
    val prefOffline by settingsVm.prefOffline.collectAsStateWithLifecycle()
    // Phase 4 fix: quality-switch/retry must reuse the local path for pinned playback,
    // otherwise file:// bytes get needlessly duplicated into the transient cache.
    var playingPinned by remember { mutableStateOf(false) }
    // Phase 6: storage section — pins totals + soft-limit (reminder only, never blocks)
    val prefPinWarnMb by settingsVm.prefPinWarnMb.collectAsStateWithLifecycle()
    // Perf toggles (settings): feed parallelism + feed cache; lazy lists stay lazy (no toggle needed)
    val prefFeedParallel by settingsVm.prefFeedParallel.collectAsStateWithLifecycle()
    val prefFeedCache by settingsVm.prefFeedCache.collectAsStateWithLifecycle()
    // Deferred item 8 (2026-11-11): periodic feed refresh toggle (WorkManager, default OFF).
    val prefFeedAuto by settingsVm.prefFeedAuto.collectAsStateWithLifecycle()
    // M1: next-item stream prefetch (latency only — no media bytes; one extractor fetch).
    // Skipped when offline (pins need no resolve) and capped at 2 entries.
    val prefPreloadNext by settingsVm.prefPreloadNext.collectAsStateWithLifecycle()
    // R5: prefetch on WiFi only (default ON, as promised) — mobile data never prefetches.
    val prefPreloadWifiOnly by settingsVm.prefPreloadWifiOnly.collectAsStateWithLifecycle()
    // M3: title-translation toggle (default ON); OFF = original titles always, zero Google calls.
    val prefTitleTranslate by settingsVm.prefTitleTranslate.collectAsStateWithLifecycle()
    fun isUnmeteredNet(): Boolean {
        return try {
            val cm = ctx.getSystemService(android.net.ConnectivityManager::class.java) ?: return false
            val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
            caps.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        } catch (_: Exception) { false }
    }
    val prefetched = remember { java.util.concurrent.ConcurrentHashMap<String, com.mohamedadel.mytube.data.youtube.StreamData>() }
    val pinsCount by libVm.pinsCount.collectAsStateWithLifecycle()
    val pinsSizeLabel by libVm.pinsSizeLabel.collectAsStateWithLifecycle()
    val pinsBytes by libVm.pinsBytes.collectAsStateWithLifecycle()
    val histCount by libVm.histCount.collectAsStateWithLifecycle()

    val cacheSizeLabel by libVm.cacheSizeLabel.collectAsStateWithLifecycle()
    val prefSpeed by settingsVm.prefSpeed.collectAsStateWithLifecycle()
    val prefPitch by settingsVm.prefPitch.collectAsStateWithLifecycle()
    // standalone search screen (YouTube-style) + trending tabs
    var showSearch by remember { mutableStateOf(false) }
    // home-redesign Phase 1 (2026-11-12): unified search state — source chips + parallel results.
    var unifiedSource by remember { mutableStateOf<com.mohamedadel.mytube.data.unified.SearchSource?>(null) }
    var uYT by remember { mutableStateOf<com.mohamedadel.mytube.data.unified.SourceResult?>(null) }
    var uSC by remember { mutableStateOf<com.mohamedadel.mytube.data.unified.SourceResult?>(null) }
    var uYM by remember { mutableStateOf<com.mohamedadel.mytube.data.unified.SourceResult?>(null) }
    var unifiedLoading by remember { mutableStateOf(false) }
    var unifiedExpanded by remember { mutableStateOf<com.mohamedadel.mytube.data.unified.SearchSource?>(null) }
    var unifiedJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    // SoundCloud tab (separate): own query/results/loading, reuses SearchScreen + SearchRow.
    var showSC by remember { mutableStateOf(false) }
    val searchVm: com.mohamedadel.mytube.ui.search.MusicSearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(key = "music")
    val scQuery by searchVm.scQuery.collectAsStateWithLifecycle()
    val scResults by searchVm.scResults.collectAsStateWithLifecycle()
    val scLoading by searchVm.scLoading.collectAsStateWithLifecycle()
    val scError by searchVm.scError.collectAsStateWithLifecycle()
    // Remote playlists (service playlists, not local Room): cards + openable contents view.
    val remotePlaylists by searchVm.remotePlaylists.collectAsStateWithLifecycle()
    val openRemote by searchVm.openRemote.collectAsStateWithLifecycle()
    val openRemoteItems by searchVm.openRemoteItems.collectAsStateWithLifecycle()
    val openRemoteLoading by searchVm.openRemoteLoading.collectAsStateWithLifecycle()
    // remote-playlist paging (SoundCloud sets run long): next page + single-flight guard.
    val openRemoteNext by searchVm.openRemoteNext.collectAsStateWithLifecycle()
    val openRemoteLoadingMore by searchVm.openRemoteLoadingMore.collectAsStateWithLifecycle()

    // Music-tab source: 0 = SoundCloud, 1 = YouTube Music (native music_songs filter).
    // screens-fix Music P1-M5: survive rotation (was remember).
    val scSource by searchVm.scSource.collectAsStateWithLifecycle()
    // Music-tab YTM kind filter (SoundCloud source stays unfiltered — its extractor has one list).
    val scKind by searchVm.scKind.collectAsStateWithLifecycle()
    // YTM tabs paging mirrors the main search list (auto-append near the end); history in prefs (on-device only).
    // scNextPage lives in SearchViewModel now
    val scHasMore by searchVm.scHasMore.collectAsStateWithLifecycle()
    val scLoadingMore by searchVm.scLoadingMore.collectAsStateWithLifecycle()
    // screens-fix Music P1-M7: search-correction mirror of the main search screen.
    val scSuggestion by searchVm.scSuggestion.collectAsStateWithLifecycle()
    val scCorrected by searchVm.scCorrected.collectAsStateWithLifecycle()
    // screens-fix Music P1-M5: taste TTL (5 min) — refreshTaste skips network while fresh.
    // screens-fix Library P2-L12: most-replayed rail data (loaded when the hub opens).
    var topPlayedHome by remember { mutableStateOf(emptyList<com.mohamedadel.mytube.data.local.PlayedStat>()) }
    // home-redesign Phase 2 (2026-11-12): recent history for the continue-watching row.
    var continueWatching by remember { mutableStateOf(emptyList<VideoItem>()) }
    // watch-position resume (2026-11-12): favKey -> 0..1 progress + resume ms.
    var continueProgress by remember { mutableStateOf(mapOf<String, Float>()) }
    var continueResumeMs by remember { mutableStateOf(mapOf<String, Long>()) }
    val scListState = androidx.compose.foundation.lazy.rememberLazyListState()
    val recentMusicSearches by searchVm.recentMusicSearches.collectAsStateWithLifecycle()
    // Local-profile recommendations (no account): quick picks + one YTM mix shelf.
    val tasteVm: com.mohamedadel.mytube.ui.search.TasteViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val tasteProfile by tasteVm.tasteProfile.collectAsStateWithLifecycle()
    val tasteMix by tasteVm.tasteMix.collectAsStateWithLifecycle()
    val tasteMixTerm by tasteVm.tasteMixTerm.collectAsStateWithLifecycle()
    val tasteMixLoading by tasteVm.tasteMixLoading.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { tasteVm.configure(dao) }
    val trendVm: com.mohamedadel.mytube.ui.trending.TrendingViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val trendTab by trendVm.trendTab.collectAsStateWithLifecycle()
    val trendingItems by trendVm.trendingItems.collectAsStateWithLifecycle()
    val trendingLoading by trendVm.trendingLoading.collectAsStateWithLifecycle()
    // screens-fix 2026-11-11 Home P0-H2: trending error state for retry UI.
    val trendingError by trendVm.trendingError.collectAsStateWithLifecycle()
    // home-redesign Phase 3 (2026-11-12): 30-min in-memory cache per tab+country.
    // First page only; paging always goes to network. Serving cache sets trendingCached (badge).
    val trendingCached by trendVm.trendingCached.collectAsStateWithLifecycle()
    // screens-fix 2026-11-11 Home P1-H5: cancellable suggestions job.
    // Deferred item 1 (2026-11-11): trending pagination — kiosk next-page + news search page.
    val trendingHasMore by trendVm.trendingHasMore.collectAsStateWithLifecycle()
    val trendingLoadingMore by trendVm.trendingLoadingMore.collectAsStateWithLifecycle()
    var openPlaylistId by remember { mutableStateOf<Long?>(null) }
    val openItems by remember(openPlaylistId) {
        openPlaylistId?.let { dao.observeItems(it) } ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    // Sleep timer: absolute deadline (uptime ms). The polling loop below watches it;
    // expiry pauses playback and clears itself. Survives nothing — a deliberate session choice.
    val sleepAtMs by playerVm.sleepAtMs.collectAsStateWithLifecycle()
    var sleepLeft by remember { mutableStateOf("") }
    var showSleep by remember { mutableStateOf(false) }
    // end-of-track mode: stop when the current track ends instead of after N minutes
    var sleepEndOfTrack by remember { mutableStateOf(false) }
    // F12 (audit P2 2026-11-11): the armed track's key — a stale flag must never stop a future track.
    var sleepTrackKey by remember { mutableStateOf<String?>(null) }
    var showQueue by remember { mutableStateOf(false) }
    var pickFor by remember { mutableStateOf<VideoItem?>(null) }
    val channelDetail by chVm.channelDetail.collectAsStateWithLifecycle()

    val cmtVm: com.mohamedadel.mytube.ui.trending.CommentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val commentList by cmtVm.commentList.collectAsStateWithLifecycle()
    var showFeed by remember { mutableStateOf(false) }
    val feedItems by feedVm.feedItems.collectAsStateWithLifecycle()
    // Low: splash waits for player readiness or first content (cap 3s), not a fixed timer.
    LaunchedEffect(Unit) {
        val t0 = android.os.SystemClock.uptimeMillis()
        while ((player == null && results.isEmpty() && feedItems.isEmpty()) &&
               android.os.SystemClock.uptimeMillis() - t0 < 3000) {
            kotlinx.coroutines.delay(100)
        }
        showSplash = false
    }
    val feedLoading by feedVm.feedLoading.collectAsStateWithLifecycle()
    var showComments by remember { mutableStateOf(false) }
    val commentsLoading by cmtVm.commentsLoading.collectAsStateWithLifecycle()

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
    val prefKeepalive by settingsVm.prefKeepalive.collectAsStateWithLifecycle()
    val prefAgeMode by settingsVm.prefAgeMode.collectAsStateWithLifecycle()
    val prefShowComments by settingsVm.prefShowComments.collectAsStateWithLifecycle()
    val prefShowRelated by settingsVm.prefShowRelated.collectAsStateWithLifecycle()
    val prefShowInfo by settingsVm.prefShowInfo.collectAsStateWithLifecycle()
    val prefShowSubs by settingsVm.prefShowSubs.collectAsStateWithLifecycle()
    val prefAllow4k by settingsVm.prefAllow4k.collectAsStateWithLifecycle()
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
    androidx.compose.runtime.LaunchedEffect(Unit) { searchVm.configure(prefs) { e -> try { ctx.getString(R.string.err_search_detail_fmt, e) } catch (_: Exception) { e } } }
    androidx.compose.runtime.LaunchedEffect(Unit) { settingsVm.configure(prefs) }
    androidx.compose.runtime.LaunchedEffect(Unit) { backupVm.configure(dao, prefs) }
    androidx.compose.runtime.LaunchedEffect(Unit) { msearchVm.configure(prefs) { try { ctx.getString(R.string.err_search) } catch (_: Exception) { "search error" } } }
    // L1 (audit P1 2026-11-11): session-restore targets — declared before the prefs-load
    // effect below (Kotlin locals must precede use); saveSession itself lives near setShuffle.
    var pendingSeekMs by remember { mutableStateOf<Long?>(null) }
    var pendingSeekKey by remember { mutableStateOf<String?>(null) }
    var lastSessSave by remember { mutableStateOf(0L) }
    // watch-position resume (2026-11-12): throttle twin of lastSessSave.
    var lastPosSave by remember { mutableStateOf(0L) }
    // Simple equalizer state (framework effect; hidden on unsupported devices).
    val eqVm: com.mohamedadel.mytube.ui.equalizer.EqualizerViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val eqEnabled by eqVm.eqEnabled.collectAsStateWithLifecycle()
    val eqPreset by eqVm.eqPreset.collectAsStateWithLifecycle()
    val eqSupported by eqVm.eqSupported.collectAsStateWithLifecycle()
    val eqLabels by eqVm.eqLabels.collectAsStateWithLifecycle()
    val eqPresetNames by eqVm.eqPresetNames.collectAsStateWithLifecycle()
    val eqCustom by eqVm.eqCustom.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { eqVm.configure(prefs) }
    fun applyEqPrefs() {
        eqVm.applyEqPrefs()
    }



    // persisted settings + recents (survive app restarts)
    LaunchedEffect(Unit) {
        try {
            try { com.mohamedadel.mytube.data.youtube.YouTubeRepo.contentLang = prefs.getString("pref_ui_lang", "ar") ?: "ar" } catch (_: Exception) { }
            try { com.mohamedadel.mytube.data.youtube.YouTubeRepo.applyRegion(prefCountry) } catch (_: Exception) { }
            eqVm.loadFromPrefs()
            // 2026-11-12 (runtime fix): autoplay persisted, default ON (بأمر محمد).
            // size migration: legacy s/m/l/xl codes -> scale float (one-time, then pref_sub_scale rules).
            try { com.mohamedadel.mytube.playback.KeepAliveService.sync(ctx.applicationContext) } catch (_: Exception) { }
            // cap only (no disk I/O); the cache itself warms on a background thread after scope exists
            try { com.mohamedadel.mytube.playback.MediaCache.setMax(prefCacheMaxMb * 1024 * 1024) } catch (_: Exception) { }
        } catch (_: Exception) { }
        try {
            val raw = prefs.getString("recent_searches", "") ?: ""
            // sleep timer is playback-bound and persisted: leaving/killing the app must not cancel it
            try {
                val dl = prefs.getLong("sleep_deadline", 0L)
                if (dl > android.os.SystemClock.uptimeMillis() + 30000L) {
                    playerVm.setSleepAtMs(dl)
                    // F1: re-arm the service-side deadline after process restart.
                    try { PlaybackService.scheduleSleep(dl - android.os.SystemClock.uptimeMillis()) } catch (_: Exception) { }
                } else if (dl > 0L) {
                    try { prefs.edit().remove("sleep_deadline").apply() } catch (_: Exception) { }
                }
            } catch (_: Exception) { }
                        // L1 (audit P1 2026-11-11): restore last session's queue (no autoplay; position seeks on tap-play).
            try {
                val favs = (prefs.getString("sess_favs", "") ?: "").split("\u001f")
                val urls = (prefs.getString("sess_urls", "") ?: "").split("\u001f")
                val titles = (prefs.getString("sess_titles", "") ?: "").split("\u001f")
                val ids = (prefs.getString("sess_ids", "") ?: "").split("\u001f")
                val idx = prefs.getInt("sess_index", -1)
                val pos = prefs.getLong("sess_pos", 0L)
                if (favs.isNotEmpty() && favs.size == urls.size && idx in favs.indices) {
                    playerVm.replaceQueue(favs.indices.map { i -> VideoItem(id = ids.getOrNull(i)?.ifBlank { null }, title = titles.getOrNull(i), url = urls.getOrNull(i)?.ifBlank { null }) }, idx)
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
    // fallback chain: primary -> secondary -> device -> English/original. Null = no forcing.
    fun pickAudioIdxFor(sd: com.mohamedadel.mytube.data.youtube.StreamData?): Int =
        pickAudioIdxFor(sd, prefPreferOriginal, prefAudioLang, prefAudioLang2)
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
    fun matchSubIdxsFor(sd: com.mohamedadel.mytube.data.youtube.StreamData?): Set<Int> =
        matchSubIdxsFor(sd, prefSubLang, prefSubLang2, prefSubHideAuto)
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
    val playMode by playerVm.playMode.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(Unit) { playerVm.initModes(prefs.getString("play_mode", "order") ?: "order", prefs.getBoolean("shuffle", false), false) }
    val shuffleOn by playerVm.shuffleOn.collectAsStateWithLifecycle()
    fun setMode(m: String) { playerVm.setPlayMode(m); try { prefs.edit().putString("play_mode", m).apply() } catch (_: Exception) { } }
    fun setShuffle(b: Boolean) { playerVm.setShuffleOn(b); try { prefs.edit().putBoolean("shuffle", b).apply() } catch (_: Exception) { } }
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
                try { settingsVm.setPrefKeepalive(prefs.getBoolean("pref_keepalive", false)) } catch (_: Exception) { }
                // G-A1: a parked SEND/VIEW intent (cold start or share-while-alive) gets consumed below.
                try { if (MainActivity.pendingSharedIntent != null) sharedPending++ } catch (_: Exception) { } }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        // no remove needed for composition lifetime
    }
    val scope = rememberCoroutineScope()
    fun openRemotePlaylist(p: com.mohamedadel.mytube.data.youtube.RemotePlaylist) {
        searchVm.openPlaylist(p)
    }
    // page-walk for the opened remote playlist (dedup by canonical key).
    fun loadMoreRemote() {
        searchVm.loadMoreRemote()
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
    val commentsHasMore by cmtVm.commentsHasMore.collectAsStateWithLifecycle()
    val commentsLoadingMore by cmtVm.commentsLoadingMore.collectAsStateWithLifecycle()
    fun loadMoreComments() {
        cmtVm.loadMoreComments()
    }
    // Fix 3: full channel screen with tabs (replaces the dialog sheet)
    val channelScreenUrl by chVm.channelScreenUrl.collectAsStateWithLifecycle()
    val channelScreenVideos by chVm.channelScreenVideos.collectAsStateWithLifecycle()
    val channelScreenLoading by chVm.channelScreenLoading.collectAsStateWithLifecycle()
    // member counts per group (groups are few; one small query each on change)
    fun refreshGroupCounts(gs: List<com.mohamedadel.mytube.data.local.ChannelGroupEntity>) {
        libVm.refreshGroupCounts(gs)
    }
    fun createGroup(name: String) {
        libVm.createGroup(name)
    }
    fun renameGroup(g: com.mohamedadel.mytube.data.local.ChannelGroupEntity, name: String) {
        libVm.renameGroup(g, name)
    }
    fun deleteGroup(g: com.mohamedadel.mytube.data.local.ChannelGroupEntity) {
        if (openGroup?.id == g.id) openGroup = null
        libVm.deleteGroup(g.id)
    }
    fun addToGroup(gid: Long, url: String) {
        libVm.addToGroup(gid, url)
    }
    fun removeFromGroup(gid: Long, url: String) {
        libVm.removeFromGroup(gid, url)
    }
    fun loadChannelTab(i: Int, url: String) {
        chVm.loadChannelTab(i, url)
    }
    fun openChannel(url: String?) {
        if (url.isNullOrBlank()) return
        try { playerExpanded = false } catch (_: Exception) { }
        chVm.openChannel(url)
    }
    // SimpleCache init does disk I/O — warm it off the main thread (skill Rule 5)
    LaunchedEffect(Unit) {
        try { kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { com.mohamedadel.mytube.playback.MediaCache.warmAsync(ctx.applicationContext) } } catch (_: Exception) { }
    }
    androidx.compose.runtime.LaunchedEffect(groups) { refreshGroupCounts(groups) }
    // On-device audio (MediaStore): tracks, folder filter/exclusion, scan mode.
    var showLocal by remember { mutableStateOf(false) }
    val localVm: com.mohamedadel.mytube.ui.local.LocalAudioViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val localTracks by localVm.localTracks.collectAsStateWithLifecycle()
    val localFolder by localVm.localFolder.collectAsStateWithLifecycle()
    val localHasPerm by localVm.hasPerm.collectAsStateWithLifecycle()
    val localExcl by localVm.localExcl.collectAsStateWithLifecycle()
    val localCompat by localVm.localCompat.collectAsStateWithLifecycle()
    fun localAudioPerm(): String = localVm.localAudioPerm()
    fun checkLocalPerm() {
        localVm.checkLocalPerm()
    }
    fun refreshLocalAudio() {
        localVm.refreshLocalAudio()
    }
    fun excludeLocalFolder(f: String) {
        localVm.excludeLocalFolder(f)
    }
    fun unexcludeLocalFolder(f: String) {
        localVm.unexcludeLocalFolder(f)
    }
    val localPermLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> localVm.onPermissionResult(granted) }
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
    val videoModeQueue by playerVm.videoModeQueue.collectAsStateWithLifecycle()
    fun loadPlSorts(pid: Long?) {
        try { plTabSort = prefs.getString("pl_tab_sort", "custom") ?: "custom" } catch (_: Exception) { }
        try { plSongSort = if (pid == null) "added" else (prefs.getString("pl_sort_" + pid, "added") ?: "added") } catch (_: Exception) { plSongSort = "added" }
    }
    // S5+S9 (audit P2 2026-11-11): CSV goes through FileProvider + EXTRA_STREAM —
    // a thousand-row list as EXTRA_TEXT would blow the ~1MB Binder budget (TransactionTooLarge).
    fun exportPlaylistCsv(pid: Long) {
        csvVm.exportPlaylistCsv(pid)
    }
    androidx.compose.runtime.LaunchedEffect(showPlaylists, openPlaylistId) { if (showPlaylists) loadPlSorts(openPlaylistId) }
    // screens-fix Library P1-L5: remote playlist -> local copy (local fun keeps the when-branch small).
    fun saveRemoteList(list: List<VideoItem>) {
        val title = try { openRemote?.title } catch (_: Exception) { null }
        libVm.saveRemoteList(list, title)
    }
    // screens-fix Library P1-L4: CSV import (round-trips our own export; header-aware).
    fun importPlaylistCsv(uri: android.net.Uri) {
        csvVm.importPlaylistCsv(uri)
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
        libVm.clearFavorites()
    }
    fun clearHistory() {
        libVm.clearHistory()
    }
    fun refreshPinsLabel() {
        libVm.refreshPinsLabel(pins.size)
    }

    // R2: history local search (Room LIKE, capped 200, off-main)
    val histSearchQuery by libVm.histSearchQuery.collectAsStateWithLifecycle()
    // R3: per-channel feed filter (tap a library row). Counts derive from live feedItems.
    var feedFilter by remember { mutableStateOf<String?>(null) }
    val feedCounts = remember(feedItems) { feedItems.groupingBy { it.channelUrl ?: "" }.eachCount().filterKeys { it.isNotEmpty() } }
    val histSearchResults by libVm.histSearchResults.collectAsStateWithLifecycle()
    fun runHistSearch(q: String) {
        libVm.runHistSearch(q)
    }
    fun refreshCacheLabel() {
        libVm.refreshCacheLabel()
    }
    fun clearMediaCache() {
        libVm.clearMediaCache()
    }
    LaunchedEffect(showSettings) {
        if (showSettings) {
            refreshCacheLabel(); refreshPinsLabel()
            libVm.loadHistCount()
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
        libVm.toggleFavorite(v)
    }
    // Listening history: every explicit play lands here (dedupe by key, capped at 100).
    fun recordHistory(v: VideoItem) {
        libVm.recordHistory(v)
    }
    val trending = remember { listOf("music", "tech reviews", "gaming", "news", "podcasts") }
    val suggestionChips by trendVm.suggestionChips.collectAsStateWithLifecycle()
    // Suggestions tab: seeds = your recent searches (else trending fallbacks);
    // chips = real NewPipe suggestionList completions, videos = top hits per seed.
    fun refreshSuggestions(region: String?, myTab: Int) {
        trendVm.refreshSuggestions(region, myTab, recentSearches)
    }

    fun refreshTrending(force: Boolean = false) {
        trendVm.refreshTrending(force, trendTab, prefCountry, prefUiLang, recentSearches)
    }
    // Deferred item 1 (2026-11-11): "show more" — kiosk next-page, or next search page for news.
    fun loadMoreTrending() {
        trendVm.loadMoreTrending(trendTab, prefCountry)
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
    fun resolvePlaybackUrl(q: String, s: StreamData): Triple<String, Boolean, String?>? =
        resolvePlaybackUrl(q, s, audioTrackIdx, prefAllow4k, ctx.estimatedDownstreamKbps())

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
            .setArtworkUri((playing?.thumbnail ?: s.artworkUrl ?: playing?.channelAvatar)?.let { Uri.parse(it) })
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
        cap4k(sd, prefAllow4k)
    fun autoKeyFor(sd0: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): String =
        autoKeyFor(sd0, kbps, prefAllow4k)
    fun qualityEntriesFor(sd0: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): List<String> {
        val sd = cap4k(sd0, prefAllow4k)
        val autoKey = autoKeyFor(sd, kbps, prefAllow4k)
        val sorted = sd.videoUrlByQuality.keys.sortedByDescending { com.mohamedadel.mytube.util.qualityHeight(it) ?: 0 }
        return mutableListOf(QUALITY_AUDIO_ONLY, autoKey).also { it.addAll(sorted) }
    }
    // default quality preference (applies to every new video AND to list-context pins)
    fun pickStartQuality(sd0: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): String =
        pickStartQuality(sd0, kbps, prefQuality, prefAllow4k)
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
        playerVm.setPlaying(v)
        recordHistory(v)
        if (expand) playerExpanded = true
        else if (!hadPlaying && (!prefMiniEnabled || !prefMiniAutoshow)) playerExpanded = true
        if (!inQueue) {
            // single tap: this video alone is the queue (no implicit follow-up)
            playerVm.replaceQueue(listOf(v), 0)
            playerVm.setAutoAdvance(false)
            playerVm.setRadioOn(false)
            playerVm.setVideoModeQueue(false)
        } else {
            playerVm.setAutoAdvance(true)
        }
        // F11 (audit P2 2026-11-11): canonical key everywhere — id/url matching let dupes slip the position.
        playerVm.setQueueIndex(playerVm.indexOfKey(v.favKey()))
        playerLoading = true
        playerError = null
        playerPositionMs = 0L; playerDurationMs = 0L; playerBufferedPct = 0
        // queue continuity: advancing inside a playing queue resumes; an explicit play-button
        // tap (forcePlay) force-starts even when the player was idle (wasPlaying is false after
        // the pause above, so autoplay alone could never resume a fresh tap). Plain prepares
        // without forcePlay keep the old paused behavior.
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
                    applyQuality(QUALITY_AUDIO_ONLY, s, player, autoplay = wasPlayingQueue, forceStart = forcePlay, local = true)
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
                                else { playerVm.setRadioOn(false); playerVm.setAutoAdvance(false) }
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
                    applyQuality(label, s, player, autoplay = wasPlayingQueue, forceStart = forcePlay, local = true)
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
                        else { playerVm.setRadioOn(false); playerVm.setAutoAdvance(false) }
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
                applyQuality("live", s, player, autoplay = wasPlayingQueue, forceStart = forcePlay, local = true)
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
            applyQuality(startQ, s, player, autoplay = wasPlayingQueue, forceStart = forcePlay)
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
    // Phase 2.3 slice 5: PlayerHost seam — VM owns navigation logic, activity owns player/toasts/streams.
    val playerHost = remember {
        val showToast: (String) -> Unit = { s -> toast(s) }
        object : com.mohamedadel.mytube.ui.player.PlayerHost {
            override fun play(v: VideoItem, inQueue: Boolean, expand: Boolean, forcePlay: Boolean) {
                playVideo(v, inQueue, expand, forcePlay = forcePlay)
            }
            override fun toast(resId: Int, arg: String?) {
                try { showToast(if (arg == null) ctx.getString(resId) else ctx.getString(resId, arg)) } catch (_: Exception) { }
            }
            override fun expandedNow(): Boolean = playerExpanded
            override fun relatedNow(): List<VideoItem> =
                try { streamData?.related ?: emptyList() } catch (_: Exception) { emptyList() }
            override fun enableAutoplayPrefs() { settingsVm.setPrefAutoplay(true) }
        }
    }
    fun startRadio(v: VideoItem) {
        playerVm.startRadio(v, playerHost)
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
        playerVm.playPrevInQueue(playerHost)
    }
    // F7 (audit P1 2026-11-11): shuffled play order as favKeys (head = next to play).
    // Both auto-advance and manual Next consume it; rebuilt when empty/stale so queue
    // changes (radio refill) can't resurrect removed items. No immediate repeat of current.
    fun ensureShuffleOrder(): List<String> {
        playerVm.drawShuffled()
        return emptyList()
    }
    // playback modes: random pick (shuffle) + wrap-around (repeat all)
    // 2026-11-12: forcePlay=true only from the STATE_ENDED path (session keeps sounding).
    fun playRandomInQueue(forcePlay: Boolean = false) {
        playerVm.playRandomInQueue(forcePlay, playerHost)
    }
    fun playNextInQueue(forcePlay: Boolean = false) {
        playerVm.playNextInQueue(forcePlay, playerHost)
    }
    fun playFirstInQueue(forcePlay: Boolean = false) {
        playerVm.playFirstInQueue(forcePlay, playerHost)
    }
    // explicit queue building (long-press menu)
    // 2026-11-12 (runtime fix, بأمر محمد): Play Now ALWAYS enables autoplay —
    // sets + persists prefAutoplay=true, arms radio/auto-advance (related refill
    // continues after this item), and force-starts this item now. Cold taps
    // elsewhere still start silent (unchanged).
    fun playNowAlone(v: VideoItem) {
        playerVm.playNowAlone(v, playerHost)
    }
    // explicit queue building only: duplicates are refused with a toast (no silent dupes)
    fun isQueued(v: VideoItem): Boolean =
        playerVm.isQueued(v)
    fun enqueueNext(v: VideoItem) {
        playerVm.enqueueNext(v, playerHost)
    }
    fun enqueueLast(v: VideoItem) {
        playerVm.enqueueLast(v, playerHost)
    }
    fun modeLabel(m: String): String = when (m) {
        "repeatAll" -> ctx.getString(R.string.mode_repeat_all)
        "repeatOne" -> ctx.getString(R.string.mode_repeat_one)
        else -> ctx.getString(R.string.mode_order)
    }
    fun cycleMode() = setMode(playerVm.cycleMode())

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
            msearchVm.loadMore(if (prefCountry == "global") null else prefCountry, prefContentFilter)
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
                            playerVm.setAutoAdvance(false)
                            playerVm.setRadioOn(false)
                            playerVm.setVideoModeQueue(false)
                            sleepLeft = ""
                            return
                        }
                    }
                    // 2026-11-12 (runtime fix): autoplay OFF stops automatic advance
                    // (explicit repeatOne replay still honors the user's picked mode).
                    if (!prefAutoplay && playMode != "repeatOne") {
                        playerVm.setRadioOn(false)
                        playerVm.setAutoAdvance(false)
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
                    // watch-position resume (2026-11-12): persist the resume point on the same
                    // 15s tick while actually playing; near-end clears it (finished = restart).
                    try {
                        val pl = playing
                        val tick = android.os.SystemClock.uptimeMillis()
                        if (pl != null && isPlaying && tick - lastPosSave > 15000L) {
                            lastPosSave = tick
                            val pos = playerPositionMs
                            val dur = playerDurationMs
                            val key = pl.favKey()
                            scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                try {
                                    if (dur > 0 && pos >= dur - 10000L) dao.updatePosition(key, 0L)
                                    else if (pos > 5000L) dao.updatePosition(key, pos)
                                } catch (_: Exception) { }
                            }
                        }
                    } catch (_: Exception) { }
                } catch (_: Exception) { }
                // sleep timer watch
                sleepAtMs?.let { deadline ->
                    val left = deadline - android.os.SystemClock.uptimeMillis()
                    if (left <= 0) {
                        fadeOutAndPause()
                        playerVm.setSleepAtMs(null)
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
    // scroll restore after VM search completes (needs activity listState + savedScroll)
    LaunchedEffect(Unit) {
        msearchVm.searchDone.collect { q ->
            savedScroll[q]?.let { (idx, off) ->
                try { listState.scrollToItem(idx, off) } catch (_: Exception) { }
            }
        }
    }
    // Search single-flight with cancellation: a new query kills the previous request,
    // so late responses can never overwrite fresh results. Failures surface with retry.
    val searchError by msearchVm.searchError.collectAsStateWithLifecycle()
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
    fun backupInfoText(): String? = backupVm.backupInfoText()
    // Deferred item 8: WorkManager scheduler for the periodic feed refresh (see FeedRefreshWorker).
    fun syncFeedWorker(appCtx: android.content.Context, enable: Boolean) {
        sysVm.syncFeedWorker(enable)
    }
    fun exportFullBackup() {
        backupVm.exportFullBackup()
    }
    fun importFullBackup(uri: android.net.Uri) {
        backupVm.importFullBackup(uri)
    }
    // Deferred item 6 (2026-11-11): update dialog payload (version + changelog + download URL).
    val updateInfo by sysVm.updateInfo.collectAsStateWithLifecycle()
    // G-A2 (audit P2 2026-11-11): compare installed versionName with latest GitHub tag.
    fun checkUpdate() {
        sysVm.checkUpdate()
    }
    fun copyDiagnostics() {
        sysVm.copyDiagnostics(diagnosticsReport())
    }

    val searchSuggestion by msearchVm.searchSuggestion.collectAsStateWithLifecycle()
    val searchCorrected by msearchVm.searchCorrected.collectAsStateWithLifecycle()
    fun doSearch(q: String) {
        if (q.isBlank()) return
        // navigating to results collapses the full player — audio continues in the mini bar.
        playerExpanded = false
        showSuggestions = false
        msearchVm.search(q, prefContentFilter, if (prefCountry == "global") null else prefCountry) { cards -> searchVm.assignRemotePlaylists(cards) }
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
    // home-redesign Phase 2 (2026-11-12): load recent history when the home screen shows.
    // No watch-position column exists yet (HistoryEntity has duration but no position),
    // so this row replays from the start — position-resume is a later item.
    androidx.compose.runtime.LaunchedEffect(results.isEmpty() && query.isBlank() && !showSearch) {
        if (results.isEmpty() && query.isBlank() && !showSearch) {
            try {
                val rows = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { dao.historyPage(10, 0) }
                val items = mutableListOf<VideoItem>()
                val prog = mutableMapOf<String, Float>()
                val resume = mutableMapOf<String, Long>()
                for (h in rows) {
                    val url = h.url ?: continue
                    val pos = h.positionMs
                    val durMs = (h.durationSec ?: 0L) * 1000L
                    // only genuinely in-progress: started (>5s) and not finished (10s margin).
                    if (pos <= 5000L) continue
                    if (durMs > 0 && pos >= durMs - 10000L) continue
                    val v = VideoItem(id = h.videoId, title = h.title, channel = h.channel, thumbnail = h.thumbnail, url = url, durationSec = h.durationSec, viewCount = h.viewCount)
                    items.add(v)
                    if (durMs > 0) prog[v.favKey()] = (pos.toFloat() / durMs).coerceIn(0f, 1f)
                    resume[v.favKey()] = pos
                    if (items.size >= 10) break
                }
                continueWatching = items
                continueProgress = prog
                continueResumeMs = resume
            } catch (_: Exception) { }
        }
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
    // Music-tab search (SoundCloud / YouTube Music): main track search runs FIRST with one
    // retry; playlist cards resolve independently so a failing playlist endpoint can never
    // blank the track list. Errors carry the real extractor message (truncated) — the old
    // generic toast hid whether this was network, ReCaptcha, or a dead client_id.
    fun doScSearch(q: String, src: Int = scSource, kind: String = scKind) {
        if (q.isBlank()) return
        searchVm.search(q, src, kind, if (prefCountry == "global") null else prefCountry)
    }
    fun refreshTaste() {
        tasteVm.refreshTaste(favorites, recentMusicSearches, recentSearches, prefCountry)
    }
    fun loadScMore() {
        searchVm.loadMore(if (prefCountry == "global") null else prefCountry)
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
                if (showSC) { searchVm.setScQuery(heard); doScSearch(heard) }
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
                settingsVm.setPrefAf("all")
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
        playerVm.setPlaying(null)
        streamData = null
        playerExpanded = false
        playerVm.setAutoAdvance(false)
        playerVm.setRadioOn(false)
        playerVm.setQueueIndex(-1)
    }
    // Smart back: exit fullscreen first; collapse full player (audio keeps playing, mini player appears); results cleared next; playlists navigated. Never stops audio.
    fun toggleSubscription(url: String?, name: String?, avatar: String?) {
        libVm.toggleSubscription(url, name, avatar)
    }
    // A3 feed: per-channel fetch with own try (one dead channel never kills the feed), merged newest-first
    // L1+L2: parallel fetch (cap 3) + 6h Room cache. DAO stays off-main; per-channel try (one dead channel never kills the feed).
    fun refreshFeed() {
        feedVm.refreshFeed(subscriptions.toList(), prefFeedParallel, prefFeedCache)
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
                settingsVm.setPrefCountry(v)
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
                // SoundCloud HLS transcodings ARE pinnable: downloadPin records the m3u8 to a file.
                if (s.audioHlsOnly && !(u?.contains("soundcloud.com") == true)) { uiToast(ctx.getString(R.string.pin_err_stream)); return@launch }
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
                // SoundCloud HLS transcodings record to file (see downloadHlsTo) — only refuse non-SC streaming audio.
                if (s.audioHlsOnly && !(u?.contains("soundcloud.com") == true)) { pinningKeys = pinningKeys - key; uiToast(ctx.getString(R.string.pin_err_stream)); return@launch }
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
            chVm.closeChannel()
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
            searchVm.clearOpenRemote()
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
            msearchVm.clearResults()
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
                Text("v0.4.0", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
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
                IconButton(onClick = { playerExpanded = false; showNotifications = true }) {
                    Icon(Icons.Rounded.Notifications, contentDescription = stringResource(R.string.notif_bell))
                }
            }
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { playerExpanded = false; showSearch = true; showSuggestions = true }) {
                Icon(Icons.Rounded.Search, contentDescription = stringResource(R.string.search_action))
            }
        }
        // Standalone search screen: full search UI lives here, results stay in the main list.
        // The suggestions dropdown lives INSIDE it (it used to sit below this fillMaxSize
        // column, invisible while typing, flashing only after submit).
        // home-redesign Phase 1 (2026-11-12): unified parallel search (All mode).
        // One supervisor job, three child launches — each source lands independently,
        // so a slow/failed source never blocks the others. Same repo calls as doSearch/doScSearch.
        fun doUnifiedSearch(q: String) {
            if (q.isBlank()) return
            unifiedJob?.cancel()
            uYT = null; uSC = null; uYM = null
            unifiedExpanded = null
            unifiedLoading = true
            msearchVm.rememberRecent(q)
            unifiedJob = scope.launch {
                try {
                    val region = if (prefCountry == "global") null else prefCountry
                    kotlinx.coroutines.supervisorScope {
                        launch { try { uYT = com.mohamedadel.mytube.data.unified.UnifiedSearchRepo.searchYouTube(q, region) } catch (_: Exception) { } }
                        launch { try { uSC = com.mohamedadel.mytube.data.unified.UnifiedSearchRepo.searchSoundCloud(q) } catch (_: Exception) { } }
                        launch { try { uYM = com.mohamedadel.mytube.data.unified.UnifiedSearchRepo.searchYTMusic(q, region) } catch (_: Exception) { } }
                    }
                } finally { unifiedLoading = false }
            }
        }
        fun retryUnifiedSource(q: String, s: com.mohamedadel.mytube.data.unified.SearchSource) {
            if (q.isBlank()) return
            scope.launch {
                try {
                    val region = if (prefCountry == "global") null else prefCountry
                    when (s) {
                        com.mohamedadel.mytube.data.unified.SearchSource.YOUTUBE -> uYT = com.mohamedadel.mytube.data.unified.UnifiedSearchRepo.searchYouTube(q, region)
                        com.mohamedadel.mytube.data.unified.SearchSource.SOUNDCLoud -> uSC = com.mohamedadel.mytube.data.unified.UnifiedSearchRepo.searchSoundCloud(q)
                        com.mohamedadel.mytube.data.unified.SearchSource.YTMUSIC -> uYM = com.mohamedadel.mytube.data.unified.UnifiedSearchRepo.searchYTMusic(q, region)
                    }
                } catch (_: Exception) { }
            }
        }
        if (showSearch) {
            // VerifyError fix (2026-11-12): overlay lives in ui.home.UnifiedSearchOverlay.
            com.mohamedadel.mytube.ui.home.UnifiedSearchOverlay(
                query = query,
                loading = loading,
                scLoading = scLoading,
                unifiedLoading = unifiedLoading,
                unifiedSource = unifiedSource,
                uYT = uYT, uSC = uSC, uYM = uYM,
                unifiedExpanded = unifiedExpanded,
                suggestions = suggestions,
                showSuggestions = showSuggestions,
                recentSearches = recentSearches,
                recentMusicSearches = recentMusicSearches,
                setRecentSearches = { msearchVm.assignRecentSearches(it) },
                setRecentMusicSearches = { searchVm.assignRecentMusic(it) },
                prefs = prefs,
                playing = playing,
                isPlaying = isPlaying,
                favoriteSet = favoriteSet,
                pinBadges = pinBadges,
                pinningKeys = pinningKeys,
                pinProgress = pinProgress,
                onQueryChange = { query = it; svm.onQueryChange(it); showSuggestions = true },
                onSelectSource = { unifiedSource = it },
                doSearch = { q -> doSearch(q) },
                doScSearch = { q, src -> doScSearch(q, src) },
                doUnifiedSearch = { q -> doUnifiedSearch(q) },
                setShowSearch = { showSearch = it },
                setShowSC = { showSC = it },
                setScQuery = { searchVm.setScQuery(it) },
                setShowSuggestions = { showSuggestions = it },
                clearUnifiedResults = { uYT = null; uSC = null; uYM = null; unifiedExpanded = null },
                retryUnifiedSource = { q, s -> retryUnifiedSource(q, s) },
                onVoice = { startVoiceSearch() },
                onPlayItem = { v -> showSearch = false; playVideo(v, expand = true, forcePlay = true) },
                onShowOptions = { optionsFor = it },
                onToggleFav = { toggleFavorite(it) },
                onExpand = { unifiedExpanded = it }
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
                                        msearchVm.showPlaylistResults(list)
                                        playerVm.replaceQueue(list, 0)
                                        playerVm.setRadioOn(false)
                                        playerVm.setVideoModeQueue(playlists.firstOrNull { it.id == pid }?.mode == "video")
                                        playVideo(list[0], inQueue = true, expand = true, forcePlay = true)
                                    }
                                }
                            },
                            onPlayFrom = { idx ->
                                val list = openItems.map { it.toVideoItem() }
                                if (idx in list.indices) {
                                    showPlaylists = false
                                    msearchVm.showPlaylistResults(list)
                                    playerVm.replaceQueue(list, idx)
                                    playerVm.setRadioOn(false)
                                    playerVm.setVideoModeQueue(playlists.firstOrNull { it.id == openPlaylistId }?.mode == "video")
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
                                    playerVm.appendItems(fresh)
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

            val mc_onSubscribeChannel: () -> Unit = { toggleSubscription(channelScreenUrl, channelDetail?.name, channelDetail?.avatar) }
            val mc_onBack: () -> Unit = { smartBack() }
            val mc_onPlayVideo: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { v -> playVideo(v, expand = true, forcePlay = true) }
            val mc_onPlayInQueue: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { v -> playVideo(v, inQueue = true, expand = true, forcePlay = true) }
            val mc_onPlayFromQueue: (Int) -> Unit = { idx -> if (idx in queue.indices) playVideo(queue[idx], inQueue = true, expand = true, forcePlay = true) }
            val mc_onShowOptions: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { optionsFor = it }
            val mc_onToggleFav: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { toggleFavorite(it) }
            val mc_onTabSelect: (Int) -> Unit = { i -> channelScreenUrl?.let { loadChannelTab(i, it) } }
            val mc_onOpenRemotePlaylist: (com.mohamedadel.mytube.data.youtube.RemotePlaylist) -> Unit = { p -> openRemotePlaylist(p) }
            val mc_setQueue: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit = { playerVm.assignQueue(it) }
            val mc_setQueueIndex: (Int) -> Unit = { playerVm.setQueueIndex(it) }
            val mc_setRadioOn: (Boolean) -> Unit = { playerVm.setRadioOn(it) }
            val mc_setShowQueue: (Boolean) -> Unit = { showQueue = it }
                        val mc_playlistsRoute: @Composable () -> Unit = { PlaylistsRoute() }
            val mc_onTrendTab: (Int) -> Unit = { i -> trendVm.cancelSuggestions(); trendVm.setTrendTab(i); refreshTrending() }
            val mc_onRetryTrending: () -> Unit = { refreshTrending(force = true) }
            val mc_onShowMoreTrending: () -> Unit = { loadMoreTrending() }
            val mc_countryName = COUNTRY_OPTIONS.firstOrNull { it.first == prefCountry }?.second?.let { ctx.getString(it) } ?: prefCountry
            val mc_sectionTitle = TREND_TABS.getOrNull(trendTab)?.first?.let { ctx.getString(it) }
            val mc_showMusicBadge = TREND_TABS.getOrNull(trendTab)?.second == "trending_music"
            val mc_isSuggestionsTab = TREND_TABS.getOrNull(trendTab)?.second == "suggestions"
            val mc_onSearchQ: (String) -> Unit = { q -> query = q; doSearch(q) }
            val mc_onSuggestionQ: (String) -> Unit = { q -> query = q; doSearch(q) }
            val mc_onResumePlay: (com.mohamedadel.mytube.data.youtube.VideoItem, Long) -> Unit = { v, ms -> pendingSeekMs = ms; pendingSeekKey = v.favKey(); playVideo(v, expand = true, forcePlay = true) }
            val mc_setContentFilter: (String) -> Unit = { settingsVm.setPrefContentFilter(it) }
            val mc_doSearchText: (String) -> Unit = { q -> doSearch(q) }
            val mc_setResults: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit = { msearchVm.assignResults(it) }
            val mc_setNextPage: (org.schabi.newpipe.extractor.Page?) -> Unit = { msearchVm.assignNextPage(it) }
            val mc_setHasMore: (Boolean) -> Unit = { msearchVm.assignHasMore(it) }
            val mc_setLoadingMore: (Boolean) -> Unit = { msearchVm.assignLoadingMore(it) }
            val mc_setShowLibrary: (Boolean) -> Unit = { showLibrary = it }
            val mc_setFromLibrary: (Boolean) -> Unit = { fromLibrary = it }
            val mc_setShowHistory: (Boolean) -> Unit = { showHistory = it }
            val mc_setShowDownloads: (Boolean) -> Unit = { showDownloads = it }
            val mc_setShowGroups: (Boolean) -> Unit = { showGroups = it }
            val mc_setShowFavorites: (Boolean) -> Unit = { showFavorites = it }
            val mc_setShowPlaylists: (Boolean) -> Unit = { showPlaylists = it }
            val mc_setShowLocal: (Boolean) -> Unit = { showLocal = it }
            val mc_setShowFeed: (Boolean) -> Unit = { showFeed = it }
            val mc_onOpenLocalAudio: () -> Unit = { showLibrary = false; fromLibrary = true; showLocal = true; checkLocalPerm(); refreshLocalAudio() }
            val mc_onOpenFeed: () -> Unit = { showLibrary = false; fromLibrary = true; showFeed = true; if (feedItems.isEmpty()) refreshFeed() }
            val mc_onUnsubscribe: (String) -> Unit = { url -> scope.launch { try { dao.deleteSubscription(url) } catch (_: Exception) { } } }
            val mc_onImportSubs: () -> Unit = { try { subsImportLauncher.launch("*/*") } catch (_: Exception) { } }
            val mc_onOpenChannel: (String) -> Unit = { url -> openChannel(url) }
            val mc_onPlayTop: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { v -> playVideo(v, expand = true, forcePlay = true) }
            val mc_onRefreshFeed: () -> Unit = { refreshFeed() }
            val mc_onPlayNotif: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { v -> playVideo(v, inQueue = false, expand = true, forcePlay = true) }
            val mc_onDeletePin: (String) -> Unit = { k -> doUnpin(k) }
            val mc_onClearAllPins: () -> Unit = { clearAllPins() }
            val mc_onCancelPin: (String) -> Unit = { k -> cancelPinDownload(k) }
            val mc_onPausePin: (String) -> Unit = { k -> pausePinDownload(k) }
            val mc_onResumePin: (String) -> Unit = { k -> resumePinDownload(k) }
            val mc_onCleanOrphans: () -> Unit = { cleanOrphans() }
            val mc_setLocalFolder: (String?) -> Unit = { localVm.setLocalFolder(it) }
            val mc_onExcludeFolder: (String) -> Unit = { f -> excludeLocalFolder(f) }
            val mc_onUnexclude: (String) -> Unit = { f -> unexcludeLocalFolder(f) }
            val mc_setLocalCompat: (Boolean) -> Unit = { localVm.setCompat(it) }
            val mc_onRefreshLocalAudio: () -> Unit = { refreshLocalAudio() }
            val mc_onRequestLocalPerm: () -> Unit = { try { localPermLauncher.launch(localAudioPerm()) } catch (_: Exception) { } }
            val mc_playingUrl = playing?.url
            val mc_setOpenGroup: (com.mohamedadel.mytube.data.local.ChannelGroupEntity?) -> Unit = { openGroup = it }
            val mc_onCreateGroup: (String) -> Unit = { createGroup(it) }
            val mc_onRenameGroup: (com.mohamedadel.mytube.data.local.ChannelGroupEntity, String) -> Unit = { g, n -> renameGroup(g, n) }
            val mc_onDeleteGroup: (com.mohamedadel.mytube.data.local.ChannelGroupEntity) -> Unit = { deleteGroup(it) }
            val mc_onRemoveFromGroup: (Long, String) -> Unit = { gid, url -> removeFromGroup(gid, url) }
            val mc_onAddToGroup: (Long, String) -> Unit = { gid, url -> addToGroup(gid, url) }
            val mc_onOpenGroupFeed: (com.mohamedadel.mytube.data.local.ChannelGroupEntity, Set<String>) -> Unit = { g, urls -> openGroupFeed(g, urls) }
            val mc_setFeedFilter: (String?) -> Unit = { feedFilter = it }
            val mc_setFeedGroupUrls: (Set<String>?) -> Unit = { feedGroupUrls = it }
            val mc_setFeedGroupName: (String?) -> Unit = { feedGroupName = it }
            val mc_setScQuery: (String) -> Unit = { searchVm.setScQuery(it) }
            val mc_setRecentMusicSearches: (List<String>) -> Unit = { searchVm.assignRecentMusic(it) }
            val mc_setScSource: (Int) -> Unit = { searchVm.setScSource(it) }
            val mc_setScKind: (String) -> Unit = { searchVm.setScKind(it) }
            val mc_setScResults: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit = { searchVm.assignScResults(it) }
            val mc_setScError: (String?) -> Unit = { searchVm.assignScError(it) }
            val mc_setScSuggestion: (String?) -> Unit = { searchVm.assignScSuggestion(it) }
            val mc_setRemotePlaylists: (List<com.mohamedadel.mytube.data.youtube.RemotePlaylist>) -> Unit = { searchVm.assignRemotePlaylists(it) }
            val mc_doScSearch: (String) -> Unit = { q -> doScSearch(q) }
            val mc_doScSearchSrc: (String, Int) -> Unit = { q, src -> doScSearch(q, src) }
            val mc_doScSearchKind: (String, Int, String) -> Unit = { q, src, k -> doScSearch(q, src, k) }
            val mc_onCloseMusic: () -> Unit = { goHome() }
            val mc_onVoice: () -> Unit = { startVoiceSearch() }
            val mc_onPlayMix: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { v -> if (v.isChannel) openChannel(v.channelUrl ?: v.url) else playVideo(v, expand = true, forcePlay = true) }
            val mc_onLoadMoreSc: () -> Unit = { loadScMore() }
            val mc_onHistSearch: (String) -> Unit = { runHistSearch(it) }
            val mc_toast: (String) -> Unit = { toast(it) }
            val mc_onClearHistory: () -> Unit = { clearHistory() }
            val mc_onClearFavorites: () -> Unit = { clearFavorites() }
            val mc_onStartRadio: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { v -> startRadio(v) }
            val mc_setOpenRemote: (com.mohamedadel.mytube.data.youtube.RemotePlaylist?) -> Unit = { searchVm.setOpenRemote(it) }
            val mc_setCurrentQuery: (String) -> Unit = { msearchVm.assignCurrentQuery(it) }
            val mc_onSaveRemoteList: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit = { list -> saveRemoteList(list) }
            val mc_onLoadMoreRemote: () -> Unit = { loadMoreRemote() }
            val mc_onQuickPin: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit = { v -> quickPin(v, null) }
            val hCh = com.mohamedadel.mytube.ui.home.ChDeps(
                channelScreenUrl = channelScreenUrl,
                channelDetail = channelDetail,
                channelScreenVideos = channelScreenVideos,
                channelScreenLoading = channelScreenLoading,
                subscriptions = subscriptions,
                ctx = ctx,
                favoriteSet = favoriteSet,
                pinBadges = pinBadges,
                pinningKeys = pinningKeys,
                pinProgress = pinProgress,
                channelShorts = channelShorts,
                channelShortsLoading = channelShortsLoading,
                channelLive = channelLive,
                channelLiveLoading = channelLiveLoading,
                channelPls = channelPls,
                channelPlsLoading = channelPlsLoading,
                onSubscribeChannel = mc_onSubscribeChannel,
                onBack = mc_onBack,
                onPlayVideo = mc_onPlayVideo,
                onPlayInQueue = mc_onPlayInQueue,
                onPlayFromQueue = mc_onPlayFromQueue,
                onShowOptions = mc_onShowOptions,
                onToggleFav = mc_onToggleFav,
                onTabSelect = mc_onTabSelect,
                onOpenRemotePlaylist = mc_onOpenRemotePlaylist,
            )
            val hQu = com.mohamedadel.mytube.ui.home.QuDeps(
                queue = queue,
                setQueue = mc_setQueue,
                queueIndex = queueIndex,
                setQueueIndex = mc_setQueueIndex,
                playing = playing,
                radioOn = radioOn,
                setRadioOn = mc_setRadioOn,
                setShowQueue = mc_setShowQueue,
                scope = scope,
                dao = dao,
            )
            val hHome = com.mohamedadel.mytube.ui.home.HomeDeps(
                playerExpanded = playerExpanded,
                showQueue = showQueue,
                showSettings = showSettings,
                trending = trending,
                trendTab = trendTab,
                onTrendTab = mc_onTrendTab,
                trendingItems = trendingItems,
                trendingLoading = trendingLoading,
                trendingError = trendingError,
                onRetryTrending = mc_onRetryTrending,
                trendingHasMore = trendingHasMore,
                trendingLoadingMore = trendingLoadingMore,
                onShowMoreTrending = mc_onShowMoreTrending,
                countryName = mc_countryName,
                sectionTitle = mc_sectionTitle,
                showMusicBadge = mc_showMusicBadge,
                isSuggestionsTab = mc_isSuggestionsTab,
                suggestionChips = suggestionChips,
                onSearchQ = mc_onSearchQ,
                onSuggestionQ = mc_onSuggestionQ,
                continueWatching = continueWatching,
                continueProgress = continueProgress,
                continueResumeMs = continueResumeMs,
                onResumePlay = mc_onResumePlay,
                trendingCached = trendingCached,
                searchError = searchError,
                currentQuery = currentQuery,
                searchSuggestion = searchSuggestion,
                searchCorrected = searchCorrected,
                remotePlaylists = remotePlaylists,
                results = results,
                listState = listState,
                prefContentFilter = prefContentFilter,
                setContentFilter = mc_setContentFilter,
                prefs = prefs,
                doSearchText = mc_doSearchText,
                setResults = mc_setResults,
                nextPage = nextPage,
                setNextPage = mc_setNextPage,
                hasMore = hasMore,
                setHasMore = mc_setHasMore,
                loadingMore = loadingMore,
                setLoadingMore = mc_setLoadingMore,
                prefCountry = prefCountry,
            )
            val hFlags = com.mohamedadel.mytube.ui.home.FlagDeps(
                showLibrary = showLibrary,
                showNotifications = showNotifications,
                showDownloads = showDownloads,
                showLocal = showLocal,
                showGroups = showGroups,
                openGroup = openGroup,
                showFeed = showFeed,
                showSC = showSC,
                showHistory = showHistory,
                showFavorites = showFavorites,
                showPlaylists = showPlaylists,
                openRemote = openRemote,
                loading = loading,
                query = query,
            )
            val hLib = com.mohamedadel.mytube.ui.home.LibDeps(
                groups = groups,
                pins = pins,
                feedCounts = feedCounts,
                topPlayedHome = topPlayedHome,
                setShowLibrary = mc_setShowLibrary,
                setFromLibrary = mc_setFromLibrary,
                setShowHistory = mc_setShowHistory,
                setShowDownloads = mc_setShowDownloads,
                setShowGroups = mc_setShowGroups,
                setShowFavorites = mc_setShowFavorites,
                setShowPlaylists = mc_setShowPlaylists,
                setShowLocal = mc_setShowLocal,
                setShowFeed = mc_setShowFeed,
                onOpenLocalAudio = mc_onOpenLocalAudio,
                onOpenFeed = mc_onOpenFeed,
                onUnsubscribe = mc_onUnsubscribe,
                onImportSubs = mc_onImportSubs,
                onOpenChannel = mc_onOpenChannel,
                onPlayTop = mc_onPlayTop,
                notifications = notifications,
                feedLoading = feedLoading,
                onRefreshFeed = mc_onRefreshFeed,
                onPlayNotif = mc_onPlayNotif,
                history = history,
                onDeletePin = mc_onDeletePin,
                onClearAllPins = mc_onClearAllPins,
                onCancelPin = mc_onCancelPin,
                pausedKeys = pausedKeys,
                onPausePin = mc_onPausePin,
                onResumePin = mc_onResumePin,
                orphanInfo = orphanInfo,
                onCleanOrphans = mc_onCleanOrphans,
                localTracks = localTracks,
                localFolder = localFolder,
                setLocalFolder = mc_setLocalFolder,
                onExcludeFolder = mc_onExcludeFolder,
                localExcl = localExcl,
                onUnexclude = mc_onUnexclude,
                localCompat = localCompat,
                setLocalCompat = mc_setLocalCompat,
                onRefreshLocalAudio = mc_onRefreshLocalAudio,
                localHasPerm = localHasPerm,
                onRequestLocalPerm = mc_onRequestLocalPerm,
                playingUrl = mc_playingUrl,
                isPlaying = isPlaying,
            )
            val hSoc = com.mohamedadel.mytube.ui.home.SocDeps(
                groupCounts = groupCounts,
                setOpenGroup = mc_setOpenGroup,
                onCreateGroup = mc_onCreateGroup,
                onRenameGroup = mc_onRenameGroup,
                onDeleteGroup = mc_onDeleteGroup,
                groupMembers = groupMembers,
                onRemoveFromGroup = mc_onRemoveFromGroup,
                onAddToGroup = mc_onAddToGroup,
                onOpenGroupFeed = mc_onOpenGroupFeed,
                feedItems = feedItems,
                feedFilter = feedFilter,
                feedGroupName = feedGroupName,
                feedGroupUrls = feedGroupUrls,
                setFeedFilter = mc_setFeedFilter,
                setFeedGroupUrls = mc_setFeedGroupUrls,
                setFeedGroupName = mc_setFeedGroupName,
            )
            val hMus = com.mohamedadel.mytube.ui.home.MusDeps(
                scQuery = scQuery,
                setScQuery = mc_setScQuery,
                scLoading = scLoading,
                recentMusicSearches = recentMusicSearches,
                setRecentMusicSearches = mc_setRecentMusicSearches,
                scSource = scSource,
                setScSource = mc_setScSource,
                scKind = scKind,
                setScKind = mc_setScKind,
                scResults = scResults,
                setScResults = mc_setScResults,
                scError = scError,
                setScError = mc_setScError,
                scSuggestion = scSuggestion,
                setScSuggestion = mc_setScSuggestion,
                scCorrected = scCorrected,
                setRemotePlaylists = mc_setRemotePlaylists,
                scHasMore = scHasMore,
                scLoadingMore = scLoadingMore,
                tasteProfile = tasteProfile,
                tasteMix = tasteMix,
                tasteMixTerm = tasteMixTerm,
                tasteMixLoading = tasteMixLoading,
                scListState = scListState,
                doScSearch = mc_doScSearch,
                doScSearchSrc = mc_doScSearchSrc,
                doScSearchKind = mc_doScSearchKind,
                onCloseMusic = mc_onCloseMusic,
                onVoice = mc_onVoice,
                onPlayMix = mc_onPlayMix,
                onLoadMoreSc = mc_onLoadMoreSc,
            )
            val hHis = com.mohamedadel.mytube.ui.home.HisDeps(
                histFlowAll = histFlowAll,
                histFlowPinned = histFlowPinned,
                histFlowUnpinned = histFlowUnpinned,
                histCount = histCount,
                histSearchQuery = histSearchQuery,
                onHistSearch = mc_onHistSearch,
                histSearchResults = histSearchResults,
                toast = mc_toast,
                onClearHistory = mc_onClearHistory,
                favorites = favorites,
                onClearFavorites = mc_onClearFavorites,
                onStartRadio = mc_onStartRadio,
                setOpenRemote = mc_setOpenRemote,
                openRemoteLoading = openRemoteLoading,
                openRemoteItems = openRemoteItems,
                setCurrentQuery = mc_setCurrentQuery,
                onSaveRemoteList = mc_onSaveRemoteList,
                openRemoteNext = openRemoteNext,
                openRemoteLoadingMore = openRemoteLoadingMore,
                onLoadMoreRemote = mc_onLoadMoreRemote,
                onQuickPin = mc_onQuickPin,
            )
            val mcHost = com.mohamedadel.mytube.ui.home.MainContentHost(
                ch = hCh,
                qu = hQu,
                home = hHome,
                flags = hFlags,
                lib = hLib,
                soc = hSoc,
                mus = hMus,
                his = hHis,
            )
    Scaffold(
        topBar = { },
        bottomBar = {
            // 2026-11-12 (mini-over-nav fix): Column wrapper so MiniPlayer stacks ABOVE
            // the NavigationBar instead of overlapping it (siblings in bottomBar overlap).
            Column {
            // Mini-player above the bottom nav (YouTube order): Scaffold layering keeps
            // this bar above ALL destinations except Settings (2026-11-12 runtime fix).
            // Surfaces allowlist (standing rule): Home/Search/Music/Library/Downloads/Queue/
            // Notifications/Player(collapsed)/Channel/Remote/LocalAudio — everything rendered
            // inside this Scaffold's content. Excluded: Settings screen, fullscreen video,
            // expanded player, PiP (system-provided mini UI). Master switch pref_mini_enabled.
            if (!isInPip && !isFullscreen && prefMiniEnabled && playing != null && !playerExpanded && !showSettings) {
                MiniPlayer(
                    // display-only art backfill (queue keeps the original): resolver artwork
                    // then channel avatar, so audio-only/SC/YTM never show a blank thumb.
                    video = playing!!.let { v -> if (v.thumbnail == null) v.copy(thumbnail = streamData?.artworkUrl ?: v.channelAvatar) else v },
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
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { pad ->
    // screens-fix queue (method-size, 2026-11-11): the 130-line PlayerArea call lived inline
    // in MyTubeApp.invoke and helped breach the 64KB limit — same local-fun trick as SettingsPane.
    @Composable
    fun PlayerPane(paneMod: Modifier = Modifier, cur: VideoItem) {
                PlayerArea(
                    modifier = paneMod,
                    video = cur,
                    artworkUrl = streamData?.artworkUrl,
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
                                settingsVm.setPrefSubLang("off")
                                subTrackIdx = -1
                                subTrackFor = null // F8: explicit off clears any override owner
                                toast(ctx.getString(R.string.toast_sub_off))
                            } else {
                                val t = streamData?.subtitles?.getOrNull(i)
                                val tag = t?.langTag?.substringBefore("-")?.takeIf { it.isNotBlank() }
                                // keep the raw tag (matching is prefix-based): collapsing rare tags to
                                // "auto" made the setting disagree with the chip the user just picked.
                                val code = tag ?: "auto"
                                settingsVm.setPrefSubLang(code)
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
                                settingsVm.setPrefAudioLang(tag)
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
                        cmtVm.loadComments(playing?.url)
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
        if (isInPip) {
            Box(Modifier.padding(pad).fillMaxSize()) {
                val pipCur = playing
                if (pipCur != null && quality != QUALITY_AUDIO_ONLY && player != null) {
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
                    AsyncImage(model = pipCur?.thumbnail, contentDescription = pipCur?.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
            }
        } else {
        Column(Modifier.padding(pad).fillMaxSize()) {
            ContentHeader()
            com.mohamedadel.mytube.ui.home.PlayerChrome(
                cur = playing,
                playerExpanded = playerExpanded,
                isFullscreen = isFullscreen,
                playerPane = { paneMod -> val c = playing; if (c != null) PlayerPane(paneMod, c) },
                showComments = showComments,
                setShowComments = { showComments = it },
                commentsLoading = commentsLoading,
                commentList = commentList,
                commentsHasMore = commentsHasMore,
                commentsLoadingMore = commentsLoadingMore,
                onLoadMoreComments = { loadMoreComments() },
                ctx = ctx
            )

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
                        settingsVm.setPrefSubLang(v)
                        applyLangPrefs()
                    },
                    onAudioLang = { v ->
                        settingsVm.setPrefAudioLang(v)
                        applyLangPrefs()
                    },
                    onSubLang2 = { v ->
                        settingsVm.setPrefSubLang2(v)
                    },
                    onAudioLang2 = { v ->
                        settingsVm.setPrefAudioLang2(v)
                        audioTrackIdx = pickAudioIdx()
                    },
                    // Geo-switch cache guard (2026-11-12): NEVER apply directly — stage
                    // the pick and show the mandatory cache-clear dialog instead.
                    onCountry = { v -> if (v != prefCountry) pendingCountry = v },
                    onQuality = { v ->
                        settingsVm.setPrefQuality(v)
                        toast(ctx.getString(R.string.toast_quality_next))
                    },
                    uiLang = prefUiLang,
                    onUiLang = { v ->
                        settingsVm.setPrefUiLang(v)
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
                        settingsVm.setPrefAutoplay(v)
                    },
                    // 2026-11-12 (runtime layer): mini-player options, live-apply (no restart).
                    miniEnabled = prefMiniEnabled,
                    onMiniEnabled = { v ->
                        settingsVm.setPrefMiniEnabled(v)
                        // turning the bar off while collapsed strands playback behind no UI —
                        // open the full player so it stays reachable (same as autoshow-OFF rule).
                        if (!v && playing != null && !playerExpanded) playerExpanded = true
                    },
                    miniAutoshow = prefMiniAutoshow,
                    onMiniAutoshow = { v ->
                        settingsVm.setPrefMiniAutoshow(v)
                    },
                    miniSwipe = prefMiniSwipe,
                    onMiniSwipe = { v ->
                        settingsVm.setPrefMiniSwipe(v)
                    },
                    afMode = prefAf,
                    onAf = { v ->
                        settingsVm.setPrefAf(v)
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
                        settingsVm.setPrefSb(v)
                    },
                    pipAuto = prefPip,
                    onPip = { v ->
                        settingsVm.setPrefPip(v)
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
                        settingsVm.setPrefCacheMaxMb(mb)
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
                        settingsVm.setPrefOffline(v)
                    },
                    keepAlive = prefKeepalive,
                    onKeepAlive = { v ->
                        settingsVm.setPrefKeepalive(v)
                        try { com.mohamedadel.mytube.playback.KeepAliveService.sync(ctx.applicationContext) } catch (_: Exception) { }
                        if (!v) toast(ctx.getString(R.string.keepalive_off))
                    },
                    ageMode = prefAgeMode,
                    onAgeMode = { m ->
                        settingsVm.setPrefAgeMode(m)
                    },
                    showComments = prefShowComments,
                    onShowComments = { v ->
                        settingsVm.setPrefShowComments(v)
                    },
                    showRelated = prefShowRelated,
                    onShowRelated = { v ->
                        settingsVm.setPrefShowRelated(v)
                    },
                    showInfo = prefShowInfo,
                    onShowInfo = { v ->
                        settingsVm.setPrefShowInfo(v)
                    },
                    showSubs = prefShowSubs,
                    onShowSubs = { v ->
                        settingsVm.setPrefShowSubs(v)
                    },
                    allow4k = prefAllow4k,
                    onAllow4k = { v ->
                        settingsVm.setPrefAllow4k(v)
                    },
                    pinsCount = pinsCount,
                    pinsSize = pinsSizeLabel,
                    pinsBytes = pinsBytes,
                    pinsWarnMb = prefPinWarnMb,
                    pinLoc = pinSelLoc,
                    extAvailable = try { com.mohamedadel.mytube.data.local.PinFiles.isExternalAvailable(ctx.applicationContext) } catch (_: Exception) { false },
                    onPinLoc = { loc ->
                        pinSelLoc = loc
                    },
                    onPinsWarn = { mb ->
                        settingsVm.setPrefPinWarnMb(mb)
                    },
                    onClearPins = { clearAllPins() },
                    feedParallel = prefFeedParallel,
                    onFeedParallel = { v ->
                        settingsVm.setPrefFeedParallel(v)
                    },
                    feedCache = prefFeedCache,
                    onFeedCache = { v ->
                        settingsVm.setPrefFeedCache(v)
                    },
                    // Deferred items 7+8: last-backup metadata row + periodic refresh toggle.
                    backupInfo = backupInfoText(),
                    feedAutoRefresh = prefFeedAuto,
                    onFeedAutoRefresh = { v ->
                        settingsVm.setPrefFeedAuto(v)
                        try { syncFeedWorker(ctx.applicationContext, v) } catch (_: Exception) { }
                    },
                    preloadNext = prefPreloadNext,
                    onPreloadNext = { v ->
                        settingsVm.setPrefPreloadNext(v)
                        if (!v) try { prefetched.clear() } catch (_: Exception) { }
                    },
                    preloadWifiOnly = prefPreloadWifiOnly,
                    titleTranslate = prefTitleTranslate,
                    onTitleTranslate = { v ->
                        settingsVm.setPrefTitleTranslate(v)
                    },
                    onPreloadWifiOnly = { v ->
                        settingsVm.setPrefPreloadWifiOnly(v)
                    },
                    subHideAuto = prefSubHideAuto,
                    onSubHideAuto = { v -> settingsVm.setPrefSubHideAuto(v) },
                    subFg = prefSubFg,
                    onSubFg = { v -> settingsVm.setPrefSubFg(v) },
                    subBg = prefSubBg,
                    onSubBg = { v -> settingsVm.setPrefSubBg(v) },
                    subEdge = prefSubEdge,
                    onSubEdge = { v -> settingsVm.setPrefSubEdge(v) },
                    subScale = prefSubScale,
                    onSubScale = { v -> settingsVm.setPrefSubScale(v) },
                    subBgOpacity = prefSubBgOpacity,
                    onSubBgOpacity = { v -> settingsVm.setPrefSubBgOpacity(v) },
                    subBold = prefSubBold,
                    onSubBold = { v -> settingsVm.setPrefSubBold(v) },
                    preferOriginal = prefPreferOriginal,
                    onPreferOriginal = { v -> settingsVm.setPrefPreferOriginal(v); audioTrackIdx = pickAudioIdx() },
                    subRole = prefSubRole,
                    onSubRole = { v -> settingsVm.setPrefSubRole(v) },
                    hideForced = prefHideForced,
                    onHideForced = { v -> settingsVm.setPrefHideForced(v) },
                    undetermined = prefUndetermined,
                    onUndetermined = { v -> settingsVm.setPrefUndetermined(v) },
                    audioRole = prefAudioRole,
                    onAudioRole = { v -> settingsVm.setPrefAudioRole(v) },
                    skipSilence = prefSkipSilence,
                    onSkipSilence = { v ->
                        settingsVm.setPrefSkipSilence(v)
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
                    onSpeed = { v -> settingsVm.setPrefSpeed(v) },
                    pitch = prefPitch,
                    onPitch = { v -> settingsVm.setPrefPitch(v) },
                    theme = prefTheme,
                    onTheme = { v ->
                        settingsVm.setPrefTheme(v)
                        try { themeLive.value = v } catch (_: Exception) { }
                    },
                    accent = prefAccent,
                    onAccent = { v ->
                        settingsVm.setPrefAccent(v)
                        try { accentLive.value = v } catch (_: Exception) { }
                    },
                    // Restart-prompt (11 Nov 2026 — بأمر محمد): dialog choice after theme/accent change.
                    onRestartNow = { (ctx as? android.app.Activity)?.let { restartAppNow(it) } },
                    volume = volumeLive.value,
                    muted = mutedLive.value,
                    onVolume = { v ->
                        try { volumeLive.value = v.coerceIn(0f, 1f) } catch (_: Exception) { }
                        if (v > 0f) try { mutedLive.value = false } catch (_: Exception) { }
                    },
                    onMuted = { m ->
                        try { mutedLive.value = m } catch (_: Exception) { }
                    },
                    eqSupported = eqSupported,
                    eqEnabled = eqEnabled,
                    onEqEnabled = { on -> eqVm.setEnabled(on) },
                    eqPresetNames = eqPresetNames,
                    eqPreset = eqPreset,
                    onEqPreset = { i -> eqVm.setPreset(i) },
                    eqLabels = eqLabels,
                    eqBands = eqCustom,
                    onEqBand = { b, v -> eqVm.setBand(b, v) },
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

                com.mohamedadel.mytube.ui.home.MainContent(h = mcHost, settingsPane = { SettingsPane() }, playlistsRoute = { PlaylistsRoute() })
            }
            // VerifyError fix (2026-11-12): end-of-screen dialogs live in ui.home.HomeDialogs.
            com.mohamedadel.mytube.ui.home.HomeDialogs(
                ctx = ctx,
                prefs = prefs,
                scope = scope,
                dao = dao,
                toast = { toast(it) },
                uiToast = { uiToast(it) },
                pickFor = pickFor,
                setPickFor = { pickFor = it },
                playlists = playlists,
                showSleep = showSleep,
                sleepLeft = sleepLeft,
                setSleepEndOfTrack = { sleepEndOfTrack = it },
                setSleepTrackKey = { sleepTrackKey = it },
                setSleepAtMs = { playerVm.setSleepAtMs(it) },
                setSleepLeft = { sleepLeft = it },
                setShowSleep = { showSleep = it },
                playing = playing,
                agePending = agePending,
                onAgePlay = { x -> agePending = null; playVideo(x.first, inQueue = x.second, ageOk = true, pre = x.third, forcePlay = true) },
                setAgePending = { agePending = it },
                pinConfirm = pinConfirm,
                pinSd = pinSd,
                pinSelLoc = pinSelLoc,
                pinSelQuality = pinSelQuality,
                pinSelAudio = pinSelAudio,
                pinSelSubs = pinSelSubs,
                pinEst = pinEst,
                prefAllow4k = prefAllow4k,
                setPinSelLoc = { pinSelLoc = it },
                setPinSelQuality = { pinSelQuality = it },
                setPinSelAudio = { pinSelAudio = it },
                setPinSelSubs = { pinSelSubs = it },
                setPinEst = { pinEst = it },
                setPinConfirm = { pinConfirm = it },
                onConfirmPin = { confirmPin() },
                optionsFor = optionsFor,
                setOptionsFor = { optionsFor = it },
                onPlayNow = { playNowAlone(it) },
                onPlayNext = { enqueueNext(it) },
                onQueueEnd = { enqueueLast(it) },
                onPinToggle = { v -> if (pinKeys.contains(v.favKey())) doUnpin(v.favKey()) else requestPin(v, null, null, null, null) },
                onQuickPin = { v -> if (!pinKeys.contains(v.favKey())) quickPin(v, null) },
                isPinned = { v -> pinKeys.contains(v.favKey()) },
                isPinning = { v -> pinningKeys.contains(v.favKey()) },
                onSubscribeVideo = { v -> toggleSubscription(v.channelUrl, v.channel, v.channelAvatar) },
                onShare = { v -> try { ctx.shareVideoCtx(v) } catch (_: Exception) { } },
                onStartRadio = { startRadio(it) },
                isSubscribed = { v -> v.channelUrl != null && subscriptions.any { it.channelUrl == v.channelUrl } },
                updateInfo = updateInfo,
                setUpdateInfo = { sysVm.setUpdateInfo(it) }
            )
        }
        }
        }
    }

// Estimated downstream bandwidth in Kbps from the active network (0 = unknown).

