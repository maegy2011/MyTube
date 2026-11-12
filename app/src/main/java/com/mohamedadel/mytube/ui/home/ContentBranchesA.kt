package com.mohamedadel.mytube.ui.home

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import coil.compose.AsyncImage
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.util.formatViews
import com.mohamedadel.mytube.data.local.ChannelGroupEntity
import com.mohamedadel.mytube.data.local.HistoryEntity
import com.mohamedadel.mytube.data.local.NotificationItemEntity
import com.mohamedadel.mytube.data.local.PinEntity
import com.mohamedadel.mytube.data.local.PlaylistDao
import com.mohamedadel.mytube.data.local.PlaylistEntity
import com.mohamedadel.mytube.data.local.SubscriptionEntity
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toEntity
import com.mohamedadel.mytube.data.youtube.ChannelDetail
import com.mohamedadel.mytube.data.youtube.CommentItem
import com.mohamedadel.mytube.data.youtube.RemotePlaylist
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.util.LocalTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Extracted from MyTubeApp (VerifyError fix, 2026-11-12): expanded-player
 * block + comments dialog. playerPane bridges the local PlayerPane fun.
 */
@Composable
fun PlayerChrome(
    cur: VideoItem?,
    playerExpanded: Boolean,
    isFullscreen: Boolean,
    playerPane: @Composable (Modifier) -> Unit,
    showComments: Boolean,
    setShowComments: (Boolean) -> Unit,
    commentsLoading: Boolean,
    commentList: List<CommentItem>,
    commentsHasMore: Boolean,
    commentsLoadingMore: Boolean,
    onLoadMoreComments: () -> Unit,
    ctx: Context,
    modifier: Modifier = Modifier
) {
    if (cur != null && (playerExpanded || isFullscreen)) {
        // 2026-11-12 (runtime fix): bounded height (weight) so the player's
        // internal ScrollableColumn can scroll — unbounded content clipped before.
        // Column wrapper preserves the original ColumnScope.weight semantics.
        androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        if (isFullscreen) playerPane(Modifier)
        else androidx.compose.foundation.layout.Box(Modifier.weight(1f)) { playerPane(Modifier.fillMaxSize()) }
        // comments sheet (real CommentsExtractor, pageless first 30)
        if (showComments) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { setShowComments(false) },
                title = { androidx.compose.material3.Text(stringResource(R.string.pill_comments)) },
                text = {
                    if (commentsLoading) {
                        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    } else if (commentList.isEmpty()) {
                        androidx.compose.material3.Text(stringResource(R.string.no_results), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                            items(commentList, key = { c -> (c.author ?: "") + "|" + (c.text?.take(24) ?: "") + "|" + (c.date ?: "") }) { c ->
                                androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.Top) {
                                    if (!c.avatar.isNullOrBlank()) {
                                        AsyncImage(model = c.avatar, contentDescription = c.author, modifier = Modifier.size(32.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                        androidx.compose.foundation.layout.Spacer(Modifier.width(8.dp))
                                    }
                                    androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                                        androidx.compose.material3.Text(
                                            (c.author ?: "") + (if (!c.date.isNullOrBlank()) " • " + c.date else "") + (if (c.likes > 0) " • " + ctx.formatViews(c.likes.toLong()) else ""),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        androidx.compose.material3.Text(c.text ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                            // page-walk: reaching the end auto-fetches the next comments page
                            if (commentsHasMore || commentsLoadingMore) {
                                item {
                                    androidx.compose.runtime.LaunchedEffect(commentList.size) { onLoadMoreComments() }
                                    androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center) {
                                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = { },
                dismissButton = { androidx.compose.material3.TextButton(onClick = { setShowComments(false) }) { androidx.compose.material3.Text(stringResource(R.string.close)) } }
            )
        }
        } // Column wrapper (weight scope)
    }
}

/**
 * Extracted from MyTubeApp (VerifyError fix, 2026-11-12): channel + queue +
 * library-set branches. Call-site lambdas stay thin; bulky work is internal.
 */

@Composable
fun ChannelBranch(
    channelScreenUrl: String?,
    channelDetail: ChannelDetail?,
    channelScreenVideos: List<VideoItem>,
    channelScreenLoading: Boolean,
    subscriptions: List<SubscriptionEntity>,
    ctx: Context,
    favoriteSet: Set<String>,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float>,
    channelShorts: List<VideoItem>,
    channelShortsLoading: Boolean,
    channelLive: List<VideoItem>,
    channelLiveLoading: Boolean,
    channelPls: List<RemotePlaylist>,
    channelPlsLoading: Boolean,
    onSubscribe: () -> Unit,
    onBack: () -> Unit,
    onPlay: (VideoItem) -> Unit,
    onShowOptions: (VideoItem) -> Unit,
    onToggleFav: (VideoItem) -> Unit,
    onTabSelect: (Int) -> Unit,
    onOpenRemotePlaylist: (RemotePlaylist) -> Unit
) {
    val chUrl = channelScreenUrl
    com.mohamedadel.mytube.ui.library.ChannelScreen(
        detail = channelDetail,
        videos = channelScreenVideos,
        videosLoading = channelScreenLoading,
        isSubscribed = chUrl != null && subscriptions.any { it.channelUrl == chUrl },
        onSubscribe = onSubscribe,
        onBack = onBack,
        onPlay = onPlay,
        onLongClick = onShowOptions,
        isFav = { v -> v.favKey() in favoriteSet },
        onFav = onToggleFav,
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
        onTabSelect = onTabSelect,
        onOpenPlaylist = onOpenRemotePlaylist
    )
}

@Composable
fun QueueBranch(
    queue: List<VideoItem>,
    setQueue: (List<VideoItem>) -> Unit,
    queueIndex: Int,
    setQueueIndex: (Int) -> Unit,
    playing: VideoItem?,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float>,
    radioOn: Boolean,
    setRadioOn: (Boolean) -> Unit,
    setShowQueue: (Boolean) -> Unit,
    scope: CoroutineScope,
    dao: PlaylistDao,
    onPlayFrom: (Int) -> Unit,
    onShowOptions: (VideoItem) -> Unit
) {
    com.mohamedadel.mytube.ui.queue.QueuePanel(
        items = queue,
        onMove = { idx, dir ->
            val j = idx + dir
            if (idx in queue.indices && j in queue.indices) {
                val m = queue.toMutableList()
                val t = m[idx]; m[idx] = m[j]; m[j] = t
                setQueue(m)
                // 2026-11-12 (runtime fix 3): canonical key (F11) — id/url matching
                // let dupes slip the position after reorder.
                setQueueIndex(m.indexOfFirst { it.favKey() == playing?.favKey() })
            }
        },
        onRemove = { idx ->
            if (idx in queue.indices) {
                setQueue(queue.toMutableList().also { it.removeAt(idx) })
                // 2026-11-12 (runtime fix 3): keep the index glued to the
                // playing item after a removal ahead of it (was: stale index
                // → next/prev + prefetch pointed at the wrong item).
                if (queue.isEmpty()) setQueueIndex(-1)
                else {
                    if (idx < queueIndex) setQueueIndex(queueIndex - 1)
                    setQueueIndex(queueIndex.coerceIn(0, queue.size - 1))
                }
            }
        },
        // Deferred item 3: swipe-to-remove Undo re-inserts at the original index.
        onInsert = { idx, v ->
            val at = idx.coerceIn(0, queue.size)
            setQueue(queue.toMutableList().also { it.add(at, v) })
        },
        onPlayFrom = onPlayFrom,
        onOptions = onShowOptions,
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
                setShowQueue(false)
            }
        },
        onDismiss = { setShowQueue(false) },
        // screens-fix Music P1-M4: visible radio state + stop.
        radioOn = radioOn,
        onStopRadio = { setRadioOn(false) },
        // screens-fix Library P2-L13: clear-all (queue itself is session-only by design).
        onClearAll = { setQueue(emptyList()); setQueueIndex(0) }
    )
}

@Composable
fun LibrarySetBranch(
    which: Int, // 0 = showLibrary, 1 = showNotifications, 2 = showDownloads, 3 = showLocal
    ctx: Context,
    prefs: SharedPreferences,
    scope: CoroutineScope,
    dao: PlaylistDao,
    // library hub
    groups: List<ChannelGroupEntity>,
    pins: List<PinEntity>,
    subscriptions: List<SubscriptionEntity>,
    feedCounts: Map<String, Int>,
    topPlayedHome: List<com.mohamedadel.mytube.data.local.PlayedStat>,
    setShowLibrary: (Boolean) -> Unit,
    setFromLibrary: (Boolean) -> Unit,
    setShowHistory: (Boolean) -> Unit,
    setShowDownloads: (Boolean) -> Unit,
    setShowGroups: (Boolean) -> Unit,
    setShowFavorites: (Boolean) -> Unit,
    setShowPlaylists: (Boolean) -> Unit,
    setShowLocal: (Boolean) -> Unit,
    setShowFeed: (Boolean) -> Unit,
    onOpenLocalAudio: () -> Unit,
    onOpenFeed: () -> Unit,
    onUnsubscribe: (String) -> Unit,
    onImportSubs: () -> Unit,
    onOpenChannel: (String) -> Unit,
    onPlayTop: (VideoItem) -> Unit,
    // notifications
    notifications: List<NotificationItemEntity>,
    feedLoading: Boolean,
    onRefreshFeed: () -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    // notifications (3-arg explicit-tap play)
    onPlayNotif: (VideoItem) -> Unit = onPlayVideo,
    // downloads
    pinProgress: Map<String, Float>,
    pinningKeys: Set<String>,
    history: List<HistoryEntity>,
    onDeletePin: (String) -> Unit,
    onClearAllPins: () -> Unit,
    onCancelPin: (String) -> Unit,
    pausedKeys: Set<String>,
    onPausePin: (String) -> Unit,
    onResumePin: (String) -> Unit,
    orphanInfo: Pair<Int, Long>,
    onCleanOrphans: () -> Unit,
    // local audio
    localTracks: List<LocalTrack>,
    localFolder: String?,
    setLocalFolder: (String?) -> Unit,
    onExcludeFolder: (String) -> Unit,
    localExcl: Set<String>,
    onUnexclude: (String) -> Unit,
    localCompat: Boolean,
    setLocalCompat: (Boolean) -> Unit,
    onRefreshLocalAudio: () -> Unit,
    localHasPerm: Boolean,
    onRequestLocalPerm: () -> Unit,
    onBack: () -> Unit,
    favoriteSet: Set<String>,
    onToggleFav: (VideoItem) -> Unit,
    onShowOptions: (VideoItem) -> Unit,
    playingUrl: String?,
    isPlaying: Boolean
) {
    when (which) {
        0 -> com.mohamedadel.mytube.ui.library.LibraryScreen(
            onHistory = { setShowLibrary(false); setFromLibrary(true); setShowHistory(true) },
            onDownloads = { setShowLibrary(false); setFromLibrary(true); setShowDownloads(true) },
            onGroups = { setShowLibrary(false); setFromLibrary(true); setShowGroups(true) },
            groupCount = groups.size,
            pinCount = pins.size,
            onFavorites = { setShowLibrary(false); setFromLibrary(true); setShowFavorites(true) },
            onPlaylists = { setShowLibrary(false); setFromLibrary(true); setShowPlaylists(true) },
            onLocalAudio = onOpenLocalAudio,
            subscriptions = subscriptions,
            onOpenFeed = onOpenFeed,
            onUnsubscribe = onUnsubscribe,
            onImportSubs = onImportSubs,
            feedCounts = feedCounts,
            onOpenChannel = onOpenChannel,
            topPlayed = topPlayedHome,
            onPlayTop = onPlayTop
        )
        1 -> com.mohamedadel.mytube.ui.library.NotificationsScreen(
            items = notifications,
            loading = feedLoading,
            onRefresh = onRefreshFeed,
            onPlay = { n ->
                scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try { dao.markNotificationRead(n.videoKey) } catch (_: Exception) { }
                }
                // Tapping a notification IS an explicit tap -> forcePlay allowed.
                onPlayNotif(with(com.mohamedadel.mytube.data.local.NotifCenter) { n.toVideoItem() })
            }
        )
        2 -> com.mohamedadel.mytube.ui.library.DownloadsRoute(
            pins = pins,
            totalBytes = try { com.mohamedadel.mytube.data.local.PinFiles.totalBytes(ctx.applicationContext) } catch (_: Exception) { 0L },
            pinningKeys = pinningKeys,
            progressMap = pinProgress,
            thumbLookup = { k -> try { history.firstOrNull { it.videoKey == k }?.thumbnail } catch (_: Exception) { null } },
            onPlay = { v -> setShowDownloads(false); onPlayVideo(v) },
            onDelete = onDeletePin,
            onClearAll = onClearAllPins,
            onCancel = onCancelPin,
            pausedKeys = pausedKeys,
            onPause = onPausePin,
            onResume = onResumePin,
            titleLookup = { k -> try { history.firstOrNull { it.videoKey == k }?.title } catch (_: Exception) { null } },
            orphanCount = orphanInfo.first,
            orphanBytes = orphanInfo.second,
            onCleanOrphans = onCleanOrphans
        )
        else -> com.mohamedadel.mytube.ui.library.LocalAudioRoute(
            tracks = localTracks,
            folderFilter = localFolder,
            onFolder = setLocalFolder,
            onExcludeFolder = onExcludeFolder,
            excluded = localExcl,
            onUnexclude = onUnexclude,
            compat = localCompat,
            onCompat = { c ->
                setLocalCompat(c)
                try { prefs.edit().putBoolean("local_scan_compat", c).apply() } catch (_: Exception) { }
                onRefreshLocalAudio()
            },
            needsPermission = !localHasPerm,
            onRequestPermission = onRequestLocalPerm,
            onRescan = onRefreshLocalAudio,
            onBack = onBack,
            onPlay = { v -> setShowLocal(false); setFromLibrary(false); onPlayVideo(v) },
            isFav = { v -> v.favKey() in favoriteSet },
            onFav = onToggleFav,
            onLongClick = onShowOptions,
            playingUrl = playingUrl,
            isPlaying = isPlaying
        )
    }
}
