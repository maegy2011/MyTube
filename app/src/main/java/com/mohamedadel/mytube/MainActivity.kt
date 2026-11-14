package com.mohamedadel.mytube

import android.app.PictureInPictureParams
import com.mohamedadel.mytube.ui.pins.PinResolveHost
import com.mohamedadel.mytube.ui.pins.confirmPin
import com.mohamedadel.mytube.ui.pins.openPinned
import com.mohamedadel.mytube.ui.pins.quickPin
import com.mohamedadel.mytube.ui.pins.requestPin
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
import androidx.fragment.app.FragmentActivity
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
import com.mohamedadel.mytube.ui.player.castCurrent
import com.mohamedadel.mytube.ui.player.dlnaCurrent
import com.mohamedadel.mytube.ui.player.fadeOutAndPause
import com.mohamedadel.mytube.ui.player.applyQuality
import com.mohamedadel.mytube.ui.player.playVideo
import com.mohamedadel.mytube.ui.player.applyLangPrefs
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
import com.mohamedadel.mytube.ui.playlists.PlaylistsRoute
import com.mohamedadel.mytube.ui.home.ContentHeader
import com.mohamedadel.mytube.ui.player.PlayerPane
import com.mohamedadel.mytube.ui.settings.SettingsPane
import com.mohamedadel.mytube.ui.share.handleShared
import com.mohamedadel.mytube.ui.playlists.PlaylistsScreen
import com.mohamedadel.mytube.ui.library.LibraryScreen
import com.mohamedadel.mytube.ui.library.NotificationsScreen
import com.mohamedadel.mytube.ui.library.FeedScreen
import com.mohamedadel.mytube.ui.library.ChannelScreen
import com.mohamedadel.mytube.ui.library.MusicHomeShelves
import com.mohamedadel.mytube.ui.library.clearPlaylist
import com.mohamedadel.mytube.ui.library.createPlaylist
import com.mohamedadel.mytube.ui.library.deletePlaylist
import com.mohamedadel.mytube.ui.library.loadPlaylistItems
import com.mohamedadel.mytube.ui.library.moveItem
import com.mohamedadel.mytube.ui.library.removeItems
import com.mohamedadel.mytube.ui.library.saveEdits
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

// 2026-11-14 (cast fix): FragmentActivity — MediaRouteButton dialogs require it.
class MainActivity : FragmentActivity() {
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
    fun s(x: String?): String = com.mohamedadel.mytube.util.uriEncode(x)
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
    val playerHostHolder = remember { arrayOfNulls<com.mohamedadel.mytube.ui.player.PlayerHost>(1) }
    val streamData by playerVm.streamData.collectAsStateWithLifecycle()
    // selected dubbed/original audio track (auto-matched to the audio pref once prefs load below)
    val audioTrackIdx by playerVm.audioTrackIdx.collectAsStateWithLifecycle()
    // Low: plain holder — no composable reads this; state caused pointless recompositions.
    val externalAudioHook = remember { arrayOfNulls<Player.Listener>(1) }
    // selected subtitle track: -1 = off (real disable), else index into streamData.subtitles (auto-matched to sub prefs)
    val subTrackIdx by playerVm.subTrackIdx.collectAsStateWithLifecycle()
    // F8 (audit P1 2026-11-11): which video the explicit pick belongs to — guards the
    // TrackSelectionOverride against stale currentTracks right after a video change.
    val subTrackFor by playerVm.subTrackFor.collectAsStateWithLifecycle()
    val qualityList by playerVm.qualityList.collectAsStateWithLifecycle()
    val quality by playerVm.quality.collectAsStateWithLifecycle()
    val playerLoading by playerVm.playerLoading.collectAsStateWithLifecycle()
    var isPlaying by remember { mutableStateOf(false) }
    val playerPositionMs by playerVm.playerPositionMs.collectAsStateWithLifecycle()
    val playerDurationMs by playerVm.playerDurationMs.collectAsStateWithLifecycle()
    val playerBufferedPct by playerVm.playerBufferedPct.collectAsStateWithLifecycle()
    val playerError by playerVm.playerError.collectAsStateWithLifecycle()
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
    val pinVm: com.mohamedadel.mytube.ui.pins.PinViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
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
    // v0.4.5 #30: smart landscape — rotate to landscape enters fullscreen while a
    // video plays (sensor), back to portrait exits. Best-effort, never fights the user:
    // only auto-enters, and only when the player is expanded with video content.
    androidx.compose.runtime.DisposableEffect(playerExpanded) {
        var listener: android.view.OrientationEventListener? = null
        if (playerExpanded) try {
            listener = object : android.view.OrientationEventListener(ctx) {
                var lastLand = false
                override fun onOrientationChanged(deg: Int) {
                    if (deg == ORIENTATION_UNKNOWN) return
                    try {
                        val land = (deg in 60..120) || (deg in 240..300)
                        if (land == lastLand) return
                        lastLand = land
                        val videoMode = quality != QUALITY_AUDIO_ONLY
                        if (land && !isFullscreen && videoMode && playerExpanded) setFullscreen(true)
                        else if (!land && isFullscreen) setFullscreen(false)
                    } catch (_: Exception) { }
                }
            }
            if (listener?.canDetectOrientation() == true) listener?.enable()
        } catch (_: Exception) { }
        onDispose { try { listener?.disable() } catch (_: Exception) { } }
    }
    // Download manager: the sd behind the current pinConfirm + live quick-pick selection.
    val pinSelQuality by pinVm.pinSelQuality.collectAsStateWithLifecycle()
    val pinSelAudio by pinVm.pinSelAudio.collectAsStateWithLifecycle()
    val pinSelSubs by pinVm.pinSelSubs.collectAsStateWithLifecycle()
    val pinSelLoc by pinVm.pinSelLoc.collectAsStateWithLifecycle()
    val pinSd by pinVm.pinSd.collectAsStateWithLifecycle()
    val pinEst by pinVm.pinEst.collectAsStateWithLifecycle()
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
    val playingPinned by playerVm.playingPinned.collectAsStateWithLifecycle()
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
    val uniVm: com.mohamedadel.mytube.ui.search.UnifiedSearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val uYT by uniVm.uYT.collectAsStateWithLifecycle()
    val uSC by uniVm.uSC.collectAsStateWithLifecycle()
    val uYM by uniVm.uYM.collectAsStateWithLifecycle()
    val unifiedLoading by uniVm.unifiedLoading.collectAsStateWithLifecycle()
    var unifiedExpanded by remember { mutableStateOf<com.mohamedadel.mytube.data.unified.SearchSource?>(null) }
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
    val pinningKeys by pinVm.pinningKeys.collectAsStateWithLifecycle()
    // F5: in-flight download jobs for cancellation (Job.cancel + PinManager flag).
    // (PinRetry hoisted to top level — see above.)
    val pinProgress by pinVm.pinProgress.collectAsStateWithLifecycle()
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
    val agePending by playerVm.agePending.collectAsStateWithLifecycle()
    val pinConfirm by pinVm.pinConfirm.collectAsStateWithLifecycle()
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
    androidx.compose.runtime.LaunchedEffect(Unit) { pinVm.configure(dao, prefs) }
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
        try { (playerHostHolder[0] as? com.mohamedadel.mytube.ui.player.LangHost)?.let { playerVm.applyLangPrefs(it) } } catch (_: Exception) { }
    }
    // auto-match the subtitle chip along the fallback chain (primary -> secondary -> device -> en)
    fun matchSubIdxFor(sd: com.mohamedadel.mytube.data.youtube.StreamData?): Int =
        com.mohamedadel.mytube.util.matchSubIdxFor(sd, prefSubLang, prefSubLang2, prefSubHideAuto)
    fun matchSubIdx(): Int = matchSubIdxFor(streamData)
    // Multi-subtitle default: the primary match PLUS the secondary-language match (Arabic + English
    // out of the box when both exist) — the user can still toggle any track in the dialog.
    fun matchSubIdxsFor(sd: com.mohamedadel.mytube.data.youtube.StreamData?): Set<Int> =
        matchSubIdxsFor(sd, prefSubLang, prefSubLang2, prefSubHideAuto)
    LaunchedEffect(streamData, prefAudioLang, prefAudioLang2) {
        // backup only: playVideo sets both indices synchronously after resolve (no race).
        // Pinned playback keeps its packaged tracks — never auto-rematch those.
        if (!playingPinned && audioTrackIdx == 0) playerVm.setAudioTrackIdx(pickAudioIdx())
    }
    LaunchedEffect(streamData, prefSubLang, prefSubLang2) {
        // backup only (same reason); off stays off; pinned keeps sub 0.
        if (!playingPinned) playerVm.setSubTrackIdx(matchSubIdx())
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
        playerVm.saveSession(prefs, playerPositionMs)
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
                try { com.mohamedadel.mytube.playback.DlnaManager.start(ctx.applicationContext) } catch (_: Exception) { }
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
    val plCovers by libVm.plCovers.collectAsStateWithLifecycle()
    fun refreshPlCovers() {
        libVm.refreshPlCovers(playlists)
    }
    androidx.compose.runtime.LaunchedEffect(playlists) { refreshPlCovers() }
    // One-tap favorite toggle (needs scope, so it lives here).
    // Short user feedback for every silent success/failure in favorites/queue/playlists.
    // playlist tools: tab sort + per-playlist song sort (prefs) + video-mode queue flag
    val plTabSort by libVm.plTabSort.collectAsStateWithLifecycle()
    val plSongSort by libVm.plSongSort.collectAsStateWithLifecycle()
    val videoModeQueue by playerVm.videoModeQueue.collectAsStateWithLifecycle()
    fun loadPlSorts(pid: Long?) {
        libVm.loadPlSorts(pid, prefs)
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
        try { playerHostHolder[0]?.let { playerVm.applyQuality(q, s, p, it, autoplay, forceStart, local) } } catch (_: Exception) { }
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
    fun castMimeFor(url: String, audioOnly: Boolean): String =
        com.mohamedadel.mytube.util.castMimeFor(url, audioOnly)

    // Push the current stream to the Cast receiver at the local position, then pause local.
    // Called on session start and whenever a new video starts while connected.

    fun castCurrent(autoplay: Boolean = true) {
        try { playerHostHolder[0]?.let { playerVm.castCurrent(it, autoplay) } } catch (_: Exception) { }
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
        try { (playerHostHolder[0] as? com.mohamedadel.mytube.ui.player.PlayEngineHost)?.let { playerVm.playVideo(v, inQueue, expand, ageOk, pre, forcePlay, it) } } catch (_: Exception) { }
    }

    // Queue navigation (suggestion #3): next/previous within the result list.
    val playerHost = remember {
        val showToast: (String) -> Unit = { s -> toast(s) }
        object : com.mohamedadel.mytube.ui.player.LangHost {
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
            override fun playerVolume(): Float = try { player?.volume ?: 1f } catch (_: Exception) { 1f }
            override fun setPlayerVolume(f: Float) { try { player?.volume = f } catch (_: Exception) { } }
            override fun pausePlayer() { try { player?.pause() } catch (_: Exception) { } }
            override fun castSnapshot(): com.mohamedadel.mytube.ui.player.CastSnapshot? {
                return try {
                    val s0 = streamData ?: return null
                    val s = cap4k(s0)
                    val triple = try { resolvePlaybackUrl(quality, s) } catch (_: Exception) { null } ?: return null
                    val pos = try { player?.currentPosition ?: 0L } catch (_: Exception) { 0L }
                    com.mohamedadel.mytube.ui.player.CastSnapshot(
                        triple.first, com.mohamedadel.mytube.util.castMimeFor(triple.first, quality == com.mohamedadel.mytube.util.QUALITY_AUDIO_ONLY),
                        playing?.title ?: s.title, playing?.thumbnail, pos, true
                    )
                } catch (_: Exception) { null }
            }
            override fun appCtx(): android.content.Context = ctx
            override fun audioTrackIdxNow(): Int = audioTrackIdx
            override fun playingMeta(): VideoItem? = playing
            override fun onQualityUnavailable() {
                playerVm.setPlayerError(ctx.getString(R.string.err_quality_unavailable))
                playerVm.setPlayerLoading(false)
            }
            override fun reassertLang() { applyLangPrefs() }
            override fun hookSlot(): Array<androidx.media3.common.Player.Listener?> = externalAudioHook
            override fun allow4kNow(): Boolean = prefAllow4k
            override fun kbpsNow(): Int = ctx.estimatedDownstreamKbps()
            override fun playPrefs(): com.mohamedadel.mytube.ui.player.PlayPrefs =
                com.mohamedadel.mytube.ui.player.PlayPrefs(
                    offline = prefOffline, ageMode = prefAgeMode,
                    miniEnabled = prefMiniEnabled, miniAutoshow = prefMiniAutoshow,
                    preloadNext = prefPreloadNext, preloadWifiOnly = prefPreloadWifiOnly,
                    videoMode = videoModeQueue
                )
            override fun exo(): ExoPlayer? = player
            override fun playerWasPlaying(): Boolean = try { player?.isPlaying == true } catch (_: Exception) { false }
            override fun pauseNow() { try { player?.playWhenReady = false; player?.pause() } catch (_: Exception) { } }
            override fun recordHistory(v: VideoItem) { libVm.recordHistory(v) }
            override fun takePrefetched(key: String): com.mohamedadel.mytube.data.youtube.StreamData? =
                try { prefetched.remove(key) } catch (_: Exception) { null }
            override fun putPrefetched(key: String, s: com.mohamedadel.mytube.data.youtube.StreamData) {
                try { prefetched[key] = s } catch (_: Exception) { }
            }
            override fun prefetchCount(): Int = try { prefetched.size } catch (_: Exception) { 0 }
            override fun clearPrefetch() { try { prefetched.clear() } catch (_: Exception) { } }
            override fun isUnmetered(): Boolean = isUnmeteredNet()
            override fun pickAudio(s: com.mohamedadel.mytube.data.youtube.StreamData): Int = pickAudioIdxFor(s)
            override fun pickSub(s: com.mohamedadel.mytube.data.youtube.StreamData): Int = matchSubIdxFor(s)
            override fun startQuality(s: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): String = pickStartQuality(s, kbps)
            override fun qualityEntries(s: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): List<String> = qualityEntriesFor(s, kbps)
            override fun setExpanded(b: Boolean) { playerExpanded = b }
            override fun offlinePinFail() {
                playerVm.setPlayerLoading(false)
                playerVm.setPlayerError(ctx.getString(R.string.err_pin_first))
                toast(ctx.getString(R.string.err_pin_first))
            }
            override fun extractFail(detail: String?) {
                playerVm.setPlayerLoading(false)
                playerVm.setPlayerError(if (!detail.isNullOrBlank()) ctx.getString(R.string.err_extract_detail, detail) else ctx.getString(R.string.err_no_sources))
            }
            override fun pinFail() {
                playerVm.setPlayerError(ctx.getString(R.string.toast_pin_fail))
                toast(ctx.getString(R.string.toast_pin_fail))
            }
            override fun ageBlocked() { toast(ctx.getString(R.string.err_age_blocked)) }
            override fun langPrefs(): com.mohamedadel.mytube.ui.player.LangPrefs =
                com.mohamedadel.mytube.ui.player.LangPrefs(
                    audioLang = prefAudioLang, subLang = prefSubLang, subRole = prefSubRole,
                    hideForced = prefHideForced, undetermined = prefUndetermined,
                    audioRole = prefAudioRole, saver = prefQuality == "saver"
                )
            override fun resolveLang(code: String): String? = com.mohamedadel.mytube.util.resolveLang(code)
            override fun textGroups(): List<com.mohamedadel.mytube.ui.player.TrackGroupInfo> {
                return try {
                    val p = player ?: return emptyList()
                    p.currentTracks.groups.filter { it.type == C.TRACK_TYPE_TEXT }
                        .map { g -> com.mohamedadel.mytube.ui.player.TrackGroupInfo(g.mediaTrackGroup, g.length) }
                } catch (_: Exception) { emptyList() }
            }
            override fun applyDecision(d: com.mohamedadel.mytube.ui.player.LangDecision) {
                val p = player ?: return
                val b = p.trackSelectionParameters.buildUpon()
                    .setPreferredAudioLanguage(d.audioLang)
                if (d.subOff) {
                    b.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, true)
                    b.setPreferredTextLanguage(null)
                } else {
                    b.setTrackTypeDisabled(C.TRACK_TYPE_TEXT, false)
                    if (d.useCaptioningManager) {
                        b.setPreferredTextLanguageAndRoleFlagsToCaptioningManagerSettings(ctx)
                    } else {
                        b.setPreferredTextLanguage(d.subLang)
                        b.setPreferredTextRoleFlags(d.subRoleFlag)
                    }
                }
                if (d.hideForced) b.setIgnoredTextSelectionFlags(C.SELECTION_FLAG_FORCED)
                b.setSelectUndeterminedTextLanguage(d.undetermined)
                b.setPreferredAudioRoleFlags(d.audioRoleFlag)
                if (d.saver) {
                    b.setMaxVideoSize(854, 480)
                    b.setMaxVideoBitrate(1_000_000)
                    b.setMaxAudioBitrate(64_000)
                } else {
                    b.setMaxVideoSize(Int.MAX_VALUE, Int.MAX_VALUE)
                    b.setMaxVideoBitrate(Int.MAX_VALUE)
                    b.setMaxAudioBitrate(Int.MAX_VALUE)
                }
                d.pinIdx?.let { idx ->
                    try {
                        val flat = textGroups().flatMap { g -> (0 until g.length).map { i -> g to i } }
                        flat.getOrNull(idx)?.let { (g, i) ->
                            b.setOverrideForType(TrackSelectionOverride(g.token as androidx.media3.common.TrackGroup, i))
                        }
                    } catch (_: Exception) { }
                }
                p.trackSelectionParameters = b.build()
            }
            override fun onCastStarted() {
                try { player?.pause() } catch (_: Exception) { }
                toast(ctx.getString(R.string.toast_cast_started))
            }
        }
    }
    try { playerHostHolder[0] = playerHost } catch (_: Exception) { }
    // Phase 2.3 slice 5: PlayerHost seam — VM owns navigation logic, activity owns player/toasts/streams.
    fun startRadio(v: VideoItem) {
        playerVm.startRadio(v, playerHost)
    }
    // T6 (audit P1 2026-11-11): cancellable fade — a second call cancels the first (no stuck low volume).
    // Plain holder (no state): no composable reads this; state would recompose pointlessly.
    val fadeJob = remember { arrayOfNulls<kotlinx.coroutines.Job>(1) }
    // 5-second fade-out instead of a hard cut; volume is restored after the pause
    fun fadeOutAndPause() {
        playerVm.fadeOutAndPause(playerHost)
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
    fun modeLabel(m: String): String = sysVm.modeLabel(m)
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
            playerVm.setPlayerError(ctx.getString(R.string.err_player_unavailable))
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
                playerVm.setPlayerLoading(state == Player.STATE_BUFFERING)
                isPlaying = player?.isPlaying == true
                if (state == Player.STATE_READY) playerVm.setPlayerError(null)
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
                playerVm.setPlayerError(ctx.smartError(error))
                playerVm.setPlayerLoading(false)
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
                    playerVm.setPlayerPositionMs(p.currentPosition.coerceAtLeast(0L))
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
                    playerVm.setPlayerDurationMs(p.duration.takeIf { it > 0 } ?: 0L)
                    playerVm.setPlayerBufferedPct(p.bufferedPercentage)
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
                playerVm.setPlayerPositionMs(0L); playerVm.setPlayerDurationMs(0L); playerVm.setPlayerBufferedPct(0)
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
    fun diagnosticsReport(): String = sysVm.diagnosticsReport(
        playerError, searchError, playing?.id, quality, prefOffline, prefCountry
    )
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
    fun extractYoutubeId(t: String): String? = com.mohamedadel.mytube.util.extractYoutubeId(t)
    // Phase 2.3 slice 15: PinHost seam — VM owns registry/runner, activity owns player/toasts/net.
    val pinHost = remember {
        object : com.mohamedadel.mytube.ui.pins.PinHost {
            override fun uiToast(s: String) { uiToast(s) }
            override fun kbps(): Int = ctx.estimatedDownstreamKbps()
            override fun allow4k(): Boolean = prefAllow4k
            override fun playingKey(): String? = try { playing?.favKey() } catch (_: Exception) { null }
            override fun closePlayer() { closePlayer() }
            override fun refreshPinsLabel() { refreshPinsLabel() }
            override fun lastPinQuality(): String? = try { prefs.getString("pref_pin_last_q", null) } catch (_: Exception) { null }
            override fun lastPinSubs(): Set<Int> = try { (prefs.getString("pref_pin_last_subs", "") ?: "").split(",").mapNotNull { it.toIntOrNull() }.toSet() } catch (_: Exception) { emptySet() }
            override fun rememberPinChoices(q: String, subs: Set<Int>) {
                try { prefs.edit().putString("pref_pin_last_q", q).putString("pref_pin_last_subs", subs.sorted().joinToString(",")).apply() } catch (_: Exception) { }
            }
        }
    }
    // slice 16: resolve-host (player/picker reads for requestPin/quickPin)
    val pinResolveHost = remember {
            object : com.mohamedadel.mytube.ui.pins.PinResolveHost {
                override fun currentStreamData() = streamData
                override fun playingFavKey(): String? = try { playing?.favKey() } catch (_: Exception) { null }
                override fun pinKeysContains(key: String): Boolean = pinKeys.contains(key)
                override fun ensurePinNotifPerm() { ensurePinNotifPerm() }
                override fun playPinned(v: VideoItem) { playVideo(v, inQueue = false, expand = true, forcePlay = true) }
                override fun pickStartQuality(s: com.mohamedadel.mytube.data.youtube.StreamData, kbps: Int): String = pickStartQuality(s, kbps)
                override fun pickAudioIdxFor(s: com.mohamedadel.mytube.data.youtube.StreamData): Int = pickAudioIdxFor(s)
                override fun matchSubIdxsFor(s: com.mohamedadel.mytube.data.youtube.StreamData): Set<Int> = matchSubIdxsFor(s)
            }
    }
    fun openPinned(key: String) {
        pinVm.openPinned(key, pinHost, pinResolveHost)
    }
    fun handleSharedIntent(inc: android.content.Intent) {
        val host = object : com.mohamedadel.mytube.ui.share.ShareIntentHost {
            override fun playSharedVideo(v: VideoItem) { playVideo(v, inQueue = false, expand = true, forcePlay = true) }
            override fun searchSharedText(q: String) { doSearch(q) }
        }
        host.handleShared(inc)
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
            voiceResultLauncher.launch(sysVm.voiceIntentLang(prefUiLang))
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
            if (sysVm.hasRecordAudio()) launchVoiceRecognizer() else voicePermLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } catch (_: Exception) { toast(ctx.getString(R.string.voice_unavailable)) }
    }


    // Explicit close: stops audio and clears the player (X button only).
    fun closePlayer() {
        playerVm.closePlayer({ try { player?.stop() } catch (_: Exception) { }; try { player?.clearMediaItems() } catch (_: Exception) { } })
        playerVm.setStreamData(null)
        playerExpanded = false
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
        sysVm.confirmCountrySwitch(v, prefUiLang,
            clearCaches = {
                com.mohamedadel.mytube.playback.MediaCache.clear(ctx.applicationContext)
                com.mohamedadel.mytube.data.youtube.YouTubeRepo.clearCaches()
            },
            setCountry = { settingsVm.setPrefCountry(it) },
            applyRegion = { c, l -> com.mohamedadel.mytube.data.youtube.YouTubeRepo.applyRegion(c, l) },
            refreshAll = { refreshTrending(); refreshFeed() },
            announce = { toast(ctx.getString(R.string.region_changed)) }
        )
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
        pinVm.requestPin(v, sd, q, aIdx, sIdxs, pinHost, pinResolveHost, pinKeys)
    }
    // pause/stop 2026-11-12: paused keys (job ended, staging kept for Range resume) +
    // stored plans so resume re-enters without re-resolving (single pinProgress source kept).
    // pause/stop 2026-11-12: download-job execution lives in PinDownloadRunner
    // (data/local) — this composable method sits at the 64KB codegen limit and the
    // inline body broke compileDebugKotlin (MethodTooLargeException). Thin
    // forwarders only; every existing call site is untouched.

    fun runPinDownload(v: VideoItem, plan: com.mohamedadel.mytube.data.local.PinManager.PinPlan, key: String, alreadyAdded: Boolean = false, retry: PinRetry? = null, resume: Boolean = false) =
        pinVm.runPinDownload(v, plan, key, pinHost, alreadyAdded, retry, resume)
    // notif 2026-11-12: POST_NOTIFICATIONS at pin time (API33+) so the terminal
    // download notification is visible even if the launch-time grant was skipped.
    // PAUSE: suspends the OkHttp stream (flag; the loop exits at the next 256KB chunk),
    // keeps staging (.part byte counts = persisted resume offsets) + frozen progress.
    fun pausePinDownload(key: String) =
        pinVm.pausePinDownload(key, pinHost)
    // RESUME: re-enters runPinDownload with resume=true — staging kept, Range continues.
    fun resumePinDownload(key: String) =
        pinVm.resumePinDownload(key, pinHost)
    // STOP: aborts the job, wipes the partial file + temp meta (staging), clears every
    // tracking key, dismisses the notification — and shows ONLY the cancel toast
    // (runPinDownload stays silent on the cancel path, no false-failure toast).
    fun cancelPinDownload(key: String) =
        pinVm.cancelPinDownload(key, pinHost)
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
        pinVm.confirmPin(pinHost, pinResolveHost)
    }
    // Shared download execution (dialog confirm + quick pin): progress card + notification + guards.
    // Quick pin: one tap, no dialog. Replays the last confirmed choices (quality + subtitle set
    // + location pref); falls back to the same smart defaults as the dialog.
    fun quickPin(v: VideoItem, sd: com.mohamedadel.mytube.data.youtube.StreamData?) {
        pinVm.quickPin(v, sd, pinHost, pinResolveHost, pinKeys)
    }
    // external orphan pins (left after switching location to internal)
    val orphanInfo by pinVm.orphanInfo.collectAsStateWithLifecycle()
    fun refreshOrphans() {
        pinVm.refreshOrphans(pinKeys.toList())
    }
    fun cleanOrphans() {
        pinVm.cleanOrphans(pinHost, pinKeys.toList())
    }
    androidx.compose.runtime.LaunchedEffect(showDownloads, pinKeys) { if (showDownloads) refreshOrphans() }
    // notif 2026-11-12: opening the screen marks everything read (badge clears).
    androidx.compose.runtime.LaunchedEffect(showNotifications) {
        if (showNotifications) { try { dao.markAllNotificationsRead() } catch (_: Exception) { } }
    }
    fun clearAllPins() {
        pinVm.clearAllPins(pinHost)
    }
    fun doUnpin(key: String) {
        pinVm.doUnpin(key, pinHost)
    }
    fun backSnapshot(): com.mohamedadel.mytube.ui.navigation.NavState =
        com.mohamedadel.mytube.ui.navigation.NavState(
            showNotifications = showNotifications, openGroup = openGroup != null,
            showGroups = showGroups, fromLibrary = fromLibrary,
            feedGroupOpen = feedGroupUrls != null, showDownloads = showDownloads,
            showLocal = showLocal, showFeed = showFeed, channelOpen = channelScreenUrl != null,
            showComments = showComments, isFullscreen = isFullscreen,
            playerExpanded = playerExpanded, showHistory = showHistory,
            showPlaylists = showPlaylists, openPlaylist = openPlaylistId != null,
            showFavorites = showFavorites, showQueue = showQueue, showSearch = showSearch,
            showSettings = showSettings, openRemote = openRemote != null,
            showLibrary = showLibrary, showSC = showSC, hasResults = results.isNotEmpty()
        )

    fun backToLibrary() {
        if (fromLibrary) { fromLibrary = false; showLibrary = true }
    }

    fun applyNavLibrary(a: com.mohamedadel.mytube.ui.navigation.NavAction): Boolean {
        when (a) {
            is com.mohamedadel.mytube.ui.navigation.NavAction.GroupToGroups -> { openGroup = null; showGroups = true }
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseGroups -> { showGroups = false; backToLibrary() }
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseGroupFeed -> { feedGroupUrls = null; feedGroupName = null; showFeed = false; backToLibrary() }
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseDownloads -> { showDownloads = false; backToLibrary() }
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseLocal -> { showLocal = false; backToLibrary() }
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseFeed -> { showFeed = false; backToLibrary() }
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseHistory -> { showHistory = false; backToLibrary() }
            is com.mohamedadel.mytube.ui.navigation.NavAction.ClosePlaylistDetail -> openPlaylistId = null
            is com.mohamedadel.mytube.ui.navigation.NavAction.ClosePlaylists -> { showPlaylists = false; backToLibrary() }
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseFavorites -> { showFavorites = false; backToLibrary() }
            else -> return false
        }
        return true
    }

    fun applyNavPlayer(a: com.mohamedadel.mytube.ui.navigation.NavAction): Boolean {
        when (a) {
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseChannel -> chVm.closeChannel()
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseComments -> showComments = false
            is com.mohamedadel.mytube.ui.navigation.NavAction.ExitFullscreen -> setFullscreen(false)
            is com.mohamedadel.mytube.ui.navigation.NavAction.CollapsePlayer -> playerExpanded = false
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseQueue -> showQueue = false
            else -> return false
        }
        return true
    }

    fun applyNavRoot(a: com.mohamedadel.mytube.ui.navigation.NavAction): Boolean {
        when (a) {
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseNotifications -> showNotifications = false
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseSearch -> showSearch = false
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseSettings -> showSettings = false
            is com.mohamedadel.mytube.ui.navigation.NavAction.ClearOpenRemote -> searchVm.clearOpenRemote()
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseLibrary -> showLibrary = false
            is com.mohamedadel.mytube.ui.navigation.NavAction.CloseSC -> showSC = false
            is com.mohamedadel.mytube.ui.navigation.NavAction.GoHome -> { showSC = false; showLibrary = false; fromLibrary = false; showFavorites = false; showHistory = false; showPlaylists = false; openPlaylistId = null; showSettings = false; playerExpanded = false }
            is com.mohamedadel.mytube.ui.navigation.NavAction.GoLibrary -> { showSC = false; showFavorites = false; showHistory = false; showPlaylists = false; openPlaylistId = null; showSettings = false; playerExpanded = false; fromLibrary = false; showLibrary = true }
            is com.mohamedadel.mytube.ui.navigation.NavAction.GoSettings -> { showSC = false; showLibrary = false; fromLibrary = false; showFavorites = false; showHistory = false; showPlaylists = false; openPlaylistId = null; playerExpanded = false; showSettings = true }
            is com.mohamedadel.mytube.ui.navigation.NavAction.GoSC -> { showLibrary = false; fromLibrary = false; showFavorites = false; showHistory = false; showPlaylists = false; openPlaylistId = null; showSettings = false; playerExpanded = false; showSC = true }
            is com.mohamedadel.mytube.ui.navigation.NavAction.ClearResults -> {
                try {
                    savedScroll[currentQuery] = listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
                } catch (_: Exception) { }
                msearchVm.clearResults()
                query = ""
            }
            else -> return false
        }
        return true
    }

    fun applyNavAction(a: com.mohamedadel.mytube.ui.navigation.NavAction): Boolean {
        if (a is com.mohamedadel.mytube.ui.navigation.NavAction.None) return false
        if (applyNavLibrary(a)) return true
        if (applyNavPlayer(a)) return true
        if (applyNavRoot(a)) return true
        return false
    }

    fun smartBack(): Boolean = applyNavAction(
        com.mohamedadel.mytube.ui.navigation.NavStack.decide(backSnapshot())
    )
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
                Text("v0.4.4", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.85f))
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
    fun goHome() = applyNavAction(com.mohamedadel.mytube.ui.navigation.NavAction.GoHome)
    fun goLibrary() = applyNavAction(com.mohamedadel.mytube.ui.navigation.NavAction.GoLibrary)
    fun goSettings() = applyNavAction(com.mohamedadel.mytube.ui.navigation.NavAction.GoSettings)
    fun goSC() = applyNavAction(com.mohamedadel.mytube.ui.navigation.NavAction.GoSC)
    val tabLibrary = showLibrary || showFavorites || showHistory || showPlaylists
    // method-size 2026-11-12: header region (battery banner + compact header +
    // standalone search) out of the Scaffold content lambda — same local-fun trick as
    // PlayerPane/SettingsPane. Pure move, zero behavior change.
    @Composable
    fun ContentHeader() {
        val host = remember {
            object : com.mohamedadel.mytube.ui.home.ContentHeaderHost {
                override fun isFullscreen() = isFullscreen
                override fun batteryExempt() = batteryExempt
                override fun requestBatteryExemption() { ensureBatteryExemption() }
                override fun canNavigateBack() = showNotifications || playerExpanded || playing != null || showPlaylists || showSettings || showSearch || showLibrary || results.isNotEmpty() || showFeed || showDownloads || showFavorites || showHistory || showSC || channelScreenUrl != null || showQueue || showGroups || openGroup != null || openRemote != null || showLocal || openPlaylistId != null
                override fun goBack() { smartBack() }
                override fun openNotifications() { playerExpanded = false; showNotifications = true }
                override fun openSearch() { playerExpanded = false; showSearch = true; showSuggestions = true }
                override fun unreadCount() = unreadCount
                override fun loading() = loading
                override fun showSearch() = showSearch
                override fun setShowSearch(b: Boolean) { showSearch = b }
                override fun query() = query
                override fun onQueryChange(q: String) { query = q; svm.onQueryChange(q); showSuggestions = true }
                override fun scLoading() = scLoading
                override fun unifiedLoading() = unifiedLoading
                override fun unifiedSource() = unifiedSource
                override fun setUnifiedSource(s: com.mohamedadel.mytube.data.unified.SearchSource?) { unifiedSource = s }
                override fun uYT() = uYT
                override fun uSC() = uSC
                override fun uYM() = uYM
                override fun unifiedExpanded() = unifiedExpanded
                override fun setUnifiedExpanded(s: com.mohamedadel.mytube.data.unified.SearchSource?) { unifiedExpanded = s }
                override fun suggestions() = suggestions
                override fun suggestionsShown() = showSuggestions
                override fun setSuggestionsShown(b: Boolean) { showSuggestions = b }
                override fun recentSearches() = recentSearches
                override fun recentMusicSearches() = recentMusicSearches
                override fun prefs() = prefs
                override fun playingNow() = playing
                override fun isPlayingNow() = isPlaying
                override fun favoriteSet() = favoriteSet
                override fun pinBadges() = pinBadges
                override fun pinningKeys() = pinningKeys
                override fun pinProgress() = pinProgress
                override fun setShowSC(b: Boolean) { showSC = b }
                override fun doSearchIn(q: String) { doSearch(q) }
                override fun doScSearchIn(q: String, src: Int) { doScSearch(q, src) }
                override fun doUnifiedSearchIn(q: String) { if (q.isBlank()) return; unifiedExpanded = null; uniVm.search(q, if (prefCountry == "global") null else prefCountry) { msearchVm.rememberRecent(it) } }
                override fun retryUnifiedSourceIn(q: String, s: com.mohamedadel.mytube.data.unified.SearchSource) { uniVm.retrySource(q, s, if (prefCountry == "global") null else prefCountry) }
                override fun clearUnified() { uniVm.clear(); unifiedExpanded = null }
                override fun onVoiceSearch() { startVoiceSearch() }
                override fun playSearchResult(v: VideoItem) { showSearch = false; playVideo(v, expand = true, forcePlay = true) }
                override fun setOptionsFor(v: VideoItem?) { optionsFor = v }
                override fun toggleFav(v: VideoItem) { toggleFavorite(v) }
                override fun uniVm() = uniVm
                override fun mainSearchVm() = msearchVm
                override fun musicSearchVm() = searchVm
                override fun svm() = svm
                override fun countryOrNull() = if (prefCountry == "global") null else prefCountry
            }
        }
        host.ContentHeader()
    }

    // 2026-11-12 (compile fix): the Scaffold content lambda breached the 64KB JVM
    // method limit (Method too large). This branch moves to a local @Composable —
    // captures stay implicit, no signature churn, no behavior change.
    @Composable
    fun PlaylistsRoute() {
        val host = remember {
            object : com.mohamedadel.mytube.ui.playlists.PlaylistsRouteHost {
                override fun playlistsSnap() = playlists
                override fun openPlaylistIdSnap() = openPlaylistId
                override fun setOpenPlaylistId(id: Long?) { openPlaylistId = id }
                override fun openItemsSnap() = openItems
                override fun pinBadgesSnap() = pinBadges
                override fun coversSnap() = plCovers
                override fun tabSortSnap() = plTabSort
                override fun songSortSnap() = plSongSort
                override fun playingSnap() = playing
                override fun routeScope() = scope
                override fun libVm() = libVm
                override fun playerVm() = playerVm
                override fun searchVm() = msearchVm
                override fun csvVm() = csvVm
                override fun routePrefs() = prefs
                override fun playRouteVideo(v: VideoItem, inQueue: Boolean, expand: Boolean, forcePlay: Boolean) { playVideo(v, inQueue = inQueue, expand = expand, forcePlay = forcePlay) }
                override fun routeIsQueued(v: VideoItem) = isQueued(v)
                override fun routeQuickPin(v: VideoItem) { quickPin(v, null) }
                override fun routeSmartBack() { smartBack() }
                override fun setRouteOptionsFor(v: VideoItem?) { optionsFor = v }
                override fun setRouteShowPlaylists(b: Boolean) { showPlaylists = b }
                override fun routeCsvImport() { try { csvImportLauncher.launch("text/*") } catch (_: Exception) { } }
            }
        }
        host.PlaylistsRoute()
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
            val mc_onTrendTab: (Int) -> Unit = { i -> trendVm.cancelSuggestions(); trendVm.setTrendTab(i); trendVm.refreshTrending(false, i, prefCountry, prefUiLang, recentSearches) }
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
                pausedKeys = pinVm.pausedKeys.collectAsStateWithLifecycle().value,
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
                        playerVm.setPlayerError(null)
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
        val host = remember {
            object : com.mohamedadel.mytube.ui.player.PlayerPaneHost {
                override fun streamData() = streamData
                override fun exo() = player
                override fun quality() = quality
                override fun qualityList() = qualityList
                override fun isPlayingNow() = isPlaying
                override fun playerLoading() = playerLoading
                override fun playerPositionMs() = playerPositionMs
                override fun playerDurationMs() = playerDurationMs
                override fun playerBufferedPct() = playerBufferedPct
                override fun playerError() = playerError
                override fun autoAdvance() = autoAdvance
                override fun playMode() = playMode
                override fun modeLabelOf(m: String) = modeLabel(m)
                override fun shuffleOn() = shuffleOn
                override fun isFullscreen() = isFullscreen
                override fun prefShowSubs() = prefShowSubs
                override fun prefSubHideAuto() = prefSubHideAuto
                override fun subTrackIdxNow() = subTrackIdx
                override fun audioTrackIdxNow() = audioTrackIdx
                override fun castAvailable() = CastManager.available.value
                override fun dlnaAvailable() = com.mohamedadel.mytube.playback.DlnaManager.available.value
                override fun dlnaPlayX(r: com.mohamedadel.mytube.playback.DlnaManager.Renderer) {
                    try { playerHostHolder[0]?.let { playerVm.dlnaCurrent(it, r) { } } } catch (_: Exception) { }
                }
                override fun isCasting() = CastManager.connected.value
                override fun sleepLeft() = sleepLeft
                override fun favoriteSet() = favoriteSet
                override fun prefShowRelated() = prefShowRelated
                override fun prefShowInfo() = prefShowInfo
                override fun prefShowComments() = prefShowComments
                override fun subscriptions() = subscriptions
                override fun pinKeys() = pinKeys
                override fun pinningKeys() = pinningKeys
                override fun pinProgressMap() = pinProgress
                override fun subStyle() = subStyleFromPrefs(prefSubFg, prefSubBg, prefSubEdge, prefSubScale, prefSubBold, prefSubBgOpacity)
                override fun translatedTitle() = translatedTitle
                override fun qualityPick(q: String) {
                    playerVm.setQuality(q)
                    streamData?.let { applyQuality(q, it, player, autoplay = true, local = playingPinned) }
                }
                override fun togglePlayExo() {
                    playerVm.setPlayerError(null)
                    if (player?.isPlaying == true) player?.pause() else player?.play()
                }
                override fun seekExo(pos: Long) { try { player?.seekTo(pos) } catch (_: Exception) { } }
                override fun retryQuality() {
                    playerVm.setPlayerError(null)
                    streamData?.let { applyQuality(quality, it, player, forceStart = true, local = playingPinned) }
                }
                override fun cycleModeX() { cycleMode() }
                override fun toggleShuffleX() { setShuffle(!shuffleOn) }
                override fun openQueue() { showQueue = true }
                override fun closePlayerX() { closePlayer() }
                override fun collapsePlayerX() { playerExpanded = false }
                override fun toggleFullscreenX() { setFullscreen(!isFullscreen) }
                override fun setModeX(m: String) { setMode(m) }
                override fun setShuffleX(b: Boolean) { setShuffle(b) }
                override fun subtitlePick(i: Int) {
                    try {
                        if (i < 0) {
                            settingsVm.setPrefSubLang("off")
                            playerVm.setSubTrackIdx(-1)
                            playerVm.setSubTrackFor(null)
                            toast(ctx.getString(R.string.toast_sub_off))
                        } else {
                            val t = streamData?.subtitles?.getOrNull(i)
                            val tag = t?.langTag?.substringBefore("-")?.takeIf { it.isNotBlank() }
                            val code = tag ?: "auto"
                            settingsVm.setPrefSubLang(code)
                            playerVm.setSubTrackIdx(i)
                            playerVm.setSubTrackFor(playing?.favKey())
                            toast(ctx.getString(R.string.sub_toast_fmt, t?.label?.takeIf { it.isNotBlank() } ?: code))
                        }
                    } catch (_: Exception) { toast(ctx.getString(R.string.toast_sub_fail)) }
                    applyLangPrefs()
                }
                override fun audioPick(i: Int) {
                    playerVm.setAudioTrackIdx(i)
                    try {
                        val t = streamData?.audioTracks?.getOrNull(i)
                        val tag = t?.localeTag?.substringBefore("-") ?: t?.name
                        if (!tag.isNullOrBlank()) settingsVm.setPrefAudioLang(tag)
                    } catch (_: Exception) { }
                    streamData?.let { sd -> applyQuality(quality, sd, player, autoplay = true) }
                }
                override fun playNextX() { playNextInQueue() }
                override fun playPrevX() { playPrevInQueue() }
                override fun openInYoutubeX(v: VideoItem) { ctx.openInYoutubeCtx(v) }
                override fun openSleep() { showSleep = true }
                override fun toggleFavX(v: VideoItem) { toggleFavorite(v) }
                override fun savePick(v: VideoItem) { pickFor = v }
                override fun chapterSeekX(pos: Long) { try { player?.seekTo(pos); playerVm.setPlayerError(null); player?.play() } catch (_: Exception) { } }
                override fun relatedPlayX(v: VideoItem) { playVideo(v, forcePlay = true) }
                override fun onPinX(v: VideoItem) {
                    if (pinKeys.contains(v.favKey())) doUnpin(v.favKey())
                    else requestPin(v, streamData, quality, audioTrackIdx, ((if (subTrackIdx >= 0) setOf(subTrackIdx) else emptySet()) + matchSubIdxsFor(streamData)))
                }
                override fun subscribeX(v: VideoItem) { toggleSubscription(streamData?.channelUrl, v.channel, channelDetail?.avatar) }
                override fun channelClickX() { openChannel(streamData?.channelUrl) }
                override fun commentsClickX() { showComments = true; cmtVm.loadComments(playing?.url) }
                override fun tagClickX(q: String) { try { playerExpanded = false } catch (_: Exception) { }; query = q; doSearch(q) }
                override fun shareX(v: VideoItem) { try { ctx.shareVideoCtx(v, try { playerPositionMs } catch (_: Exception) { 0L }) } catch (_: Exception) { } }
                override fun setRelatedOptions(v: VideoItem?) { optionsFor = v }
                override fun speedNow() = try { prefSpeed } catch (_: Exception) { 1f }
                override fun cycleSpeedX() {
                    try {
                        val steps = listOf(1f, 1.25f, 1.5f, 1.75f, 2f)
                        val cur = try { prefSpeed } catch (_: Exception) { 1f }
                        val next = steps.firstOrNull { it > cur + 0.01f } ?: 1f
                        settingsVm.setPrefSpeed(next)
                    } catch (_: Exception) { }
                }
                private var prevQBeforeAudio = "auto"
                override fun audioQuickOn() = quality == QUALITY_AUDIO_ONLY
                override fun audioQuickX() {
                    try {
                        if (quality == QUALITY_AUDIO_ONLY) qualityPick(prevQBeforeAudio)
                        else { prevQBeforeAudio = quality; qualityPick(QUALITY_AUDIO_ONLY) }
                    } catch (_: Exception) { }
                }
            }
        }
        host.PlayerPane(paneMod, cur, onEnterPip)
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
        val host = remember {
            object : com.mohamedadel.mytube.ui.settings.SettingsPaneHost {
                override fun settingsVm() = settingsVm
                override fun libVm() = libVm
                override fun pinVm() = pinVm
                override fun eqVm() = eqVm
                override fun playerVm() = playerVm
                override fun favoriteKeys() = favoriteKeys
                override fun playlistsCount() = playlists.size
                override fun prefetchedMap() = prefetched
                override fun pickAudioIdxNow() = pickAudioIdx()
                override fun dbSizeLabel() = dbSizeLabel()
                override fun backupInfoText() = backupInfoText()
                override fun extAvailable() = try { com.mohamedadel.mytube.data.local.PinFiles.isExternalAvailable(ctx.applicationContext) } catch (_: Exception) { false }
                override fun strOf(id: Int) = try { ctx.getString(id) } catch (_: Exception) { "" }
                override fun subLangX(v: String) { settingsVm.setPrefSubLang(v); applyLangPrefs() }
                override fun audioLangX(v: String) { settingsVm.setPrefAudioLang(v); applyLangPrefs() }
                override fun stageCountry(v: String) { pendingCountry = v }
                override fun qualityX(v: String) { settingsVm.setPrefQuality(v); toast(ctx.getString(R.string.toast_quality_next)) }
                override fun uiLangX(v: String) { settingsVm.setPrefUiLang(v); toast(ctx.getString(R.string.toast_lang_restart)); try { (ctx as? android.app.Activity)?.recreate() } catch (_: Exception) { } }
                override fun playModeX(v: String) { setMode(v); toast(ctx.getString(R.string.playmode_toast_fmt, modeLabel(v))) }
                override fun miniEnabledX(v: Boolean) { settingsVm.setPrefMiniEnabled(v); if (!v && playing != null && !playerExpanded) playerExpanded = true }
                override fun afX(v: String) {
                    settingsVm.setPrefAf(v)
                    try {
                        if (v == "calls") {
                            val granted = ctx.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
                            if (!granted) afPermLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                        }
                        applyAudioFocus()
                    } catch (_: Exception) { }
                }
                override fun clearFavoritesX() { clearFavorites() }
                override fun clearHistoryX() { clearHistory() }
                override fun copyDiagnosticsX() { copyDiagnostics() }
                override fun checkUpdateX() { checkUpdate() }
                override fun exportFullX() { exportFullBackup() }
                override fun importFullX() { try { backupImportLauncher.launch("application/json") } catch (_: Exception) { } }
                override fun cacheMaxX(mb: Long) {
                    settingsVm.setPrefCacheMaxMb(mb)
                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        try {
                            com.mohamedadel.mytube.playback.MediaCache.setMax(mb * 1024 * 1024)
                            com.mohamedadel.mytube.playback.MediaCache.warmAsync(ctx.applicationContext)
                        } catch (_: Exception) { }
                        refreshCacheLabel()
                    }
                    toast(ctx.getString(R.string.toast_cache_max))
                }
                override fun clearCacheX() { clearMediaCache() }
                override fun keepAliveX(v: Boolean) { settingsVm.setPrefKeepalive(v); try { com.mohamedadel.mytube.playback.KeepAliveService.sync(ctx.applicationContext) } catch (_: Exception) { }; if (!v) toast(ctx.getString(R.string.keepalive_off)) }
                override fun clearPinsX() { clearAllPins() }
                override fun feedAutoX(v: Boolean) { settingsVm.setPrefFeedAuto(v); try { syncFeedWorker(ctx.applicationContext, v) } catch (_: Exception) { } }
                override fun preloadNextX(v: Boolean) { settingsVm.setPrefPreloadNext(v); if (!v) try { prefetched.clear() } catch (_: Exception) { } }
                override fun preferOriginalX(v: Boolean) { settingsVm.setPrefPreferOriginal(v); playerVm.setAudioTrackIdx(pickAudioIdx()) }
                override fun skipSilenceX(v: Boolean) {
                    settingsVm.setPrefSkipSilence(v)
                    toast(ctx.getString(R.string.toast_skip_restart))
                    try { ctx.stopService(android.content.Intent(ctx, com.mohamedadel.mytube.playback.PlaybackService::class.java)) } catch (_: Exception) { }
                    scope.launch {
                        try { kotlinx.coroutines.delay(900) } catch (_: Exception) { }
                        try { withContext(kotlinx.coroutines.Dispatchers.Main) { (ctx as? android.app.Activity)?.recreate() } } catch (_: Exception) { }
                    }
                }
                override fun themeX(v: String) { settingsVm.setPrefTheme(v); try { themeLive.value = v } catch (_: Exception) { } }
                override fun accentX(v: String) { settingsVm.setPrefAccent(v); try { accentLive.value = v } catch (_: Exception) { } }
                override fun volumeX(v: Float) { try { volumeLive.value = v.coerceIn(0f, 1f) } catch (_: Exception) { }; if (v > 0f) try { mutedLive.value = false } catch (_: Exception) { } }
                override fun mutedX(m: Boolean) { try { mutedLive.value = m } catch (_: Exception) { } }
                override fun restartNowX() { (ctx as? android.app.Activity)?.let { restartAppNow(it) } }
                override fun pendingCountry() = pendingCountry
                override fun clearPending() { pendingCountry = null }
                override fun confirmCountryX(v: String) { confirmCountrySwitch(v) }
            }
        }
        host.SettingsPane()
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
                onAgePlay = { x -> playerVm.setAgePending(null); playVideo(x.first, inQueue = x.second, ageOk = true, pre = x.third, forcePlay = true) },
                setAgePending = { playerVm.setAgePending(it) },
                pinConfirm = pinConfirm,
                pinSd = pinSd,
                pinSelLoc = pinSelLoc,
                pinSelQuality = pinSelQuality,
                pinSelAudio = pinSelAudio,
                pinSelSubs = pinSelSubs,
                pinEst = pinEst,
                prefAllow4k = prefAllow4k,
                setPinSelLoc = { pinVm.setPinSelLoc(it) },
                setPinSelQuality = { pinVm.setPinSelQuality(it) },
                setPinSelAudio = { pinVm.setPinSelAudio(it) },
                setPinSelSubs = { pinVm.setPinSelSubs(it) },
                setPinEst = { pinVm.setPinEst(it) },
                setPinConfirm = { pinVm.setPinConfirm(it) },
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

