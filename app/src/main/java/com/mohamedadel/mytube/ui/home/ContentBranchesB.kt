package com.mohamedadel.mytube.ui.home

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.local.ChannelGroupEntity
import com.mohamedadel.mytube.data.local.ChannelGroupMemberEntity
import com.mohamedadel.mytube.data.local.FavoriteEntity
import com.mohamedadel.mytube.data.local.PlaylistDao
import com.mohamedadel.mytube.data.local.SubscriptionEntity
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.local.toVideoItem
import com.mohamedadel.mytube.data.youtube.RemotePlaylist
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.util.TasteProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData
import com.mohamedadel.mytube.data.local.HistoryEntity
import kotlinx.coroutines.launch

/**
 * Extracted from MyTubeApp (VerifyError fix, 2026-11-12): social/feed/music/
 * history-set branches. Call-site lambdas stay thin; bulky work is internal.
 */

@Composable
fun SocialFeedBranch(
    which: Int, // 0 = showGroups, 1 = openGroup, 2 = showFeed
    groups: List<ChannelGroupEntity>,
    groupCounts: Map<Long, Int>,
    setOpenGroup: (ChannelGroupEntity?) -> Unit,
    setShowGroups: (Boolean) -> Unit,
    onCreateGroup: (String) -> Unit,
    onRenameGroup: (ChannelGroupEntity, String) -> Unit,
    onDeleteGroup: (ChannelGroupEntity) -> Unit,
    onBack: () -> Unit,
    openGroup: ChannelGroupEntity?,
    groupMembers: List<ChannelGroupMemberEntity>,
    subscriptions: List<SubscriptionEntity>,
    onRemoveFromGroup: (Long, String) -> Unit,
    onAddToGroup: (Long, String) -> Unit,
    onOpenChannel: (String) -> Unit,
    scope: CoroutineScope,
    dao: PlaylistDao,
    onOpenGroupFeed: (ChannelGroupEntity, Set<String>) -> Unit,
    feedItems: List<VideoItem>,
    feedLoading: Boolean,
    onRefreshFeed: () -> Unit,
    feedFilter: String?,
    feedGroupName: String?,
    feedGroupUrls: Set<String>?,
    feedCounts: Map<String, Int>,
    setFeedFilter: (String?) -> Unit,
    setFeedGroupUrls: (Set<String>?) -> Unit,
    setFeedGroupName: (String?) -> Unit,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float>,
    favoriteSet: Set<String>,
    onToggleFav: (VideoItem) -> Unit,
    onShowOptions: (VideoItem) -> Unit,
    onPlayVideo: (VideoItem) -> Unit
) {
    when (which) {
        0 -> com.mohamedadel.mytube.ui.library.GroupsScreen(
            groups = groups,
            counts = groupCounts,
            onOpen = { setOpenGroup(it); setShowGroups(false) },
            onCreate = onCreateGroup,
            onRename = onRenameGroup,
            onDelete = onDeleteGroup,
            onBack = onBack
        )
        1 -> {
            // T5: no !! on delegated state — null falls back to list.
            val g = openGroup
            if (g != null) com.mohamedadel.mytube.ui.library.GroupDetailScreen(
                group = g,
                members = groupMembers,
                subscriptions = subscriptions,
                onRemove = { onRemoveFromGroup(g.id, it) },
                onAdd = { onAddToGroup(g.id, it) },
                onOpenChannel = onOpenChannel,
                onOpenFeed = {
                    scope.launch(kotlinx.coroutines.Dispatchers.IO) {
                        val urls = try { dao.memberUrls(g.id) } catch (_: Exception) { emptyList() }
                        onOpenGroupFeed(g, urls.toSet())
                    }
                },
                onBack = onBack
            )
        }
        else -> com.mohamedadel.mytube.ui.library.FeedScreen(
            items = feedItems,
            loading = feedLoading,
            onRefresh = onRefreshFeed,
            filterChannelUrl = feedFilter,
            filterChannelName = feedGroupName ?: feedFilter?.let { u -> subscriptions.firstOrNull { it.channelUrl == u }?.name },
            onClearFilter = { setFeedFilter(null); setFeedGroupUrls(null); setFeedGroupName(null) },
            filterUrls = feedGroupUrls ?: emptySet(),
            filterLabel = feedGroupName,
            pinBadges = pinBadges,
            pinningKeys = pinningKeys,
            progressOf = { k -> pinProgress[k] },
            onLongClick = onShowOptions,
            onPlay = onPlayVideo,
            isFav = { v -> v.favKey() in favoriteSet },
            onFav = onToggleFav,
        )
    }
}

@Composable
fun MusicBranch(
    scQuery: String,
    setScQuery: (String) -> Unit,
    scLoading: Boolean,
    recentMusicSearches: List<String>,
    setRecentMusicSearches: (List<String>) -> Unit,
    prefs: SharedPreferences,
    scSource: Int,
    setScSource: (Int) -> Unit,
    scKind: String,
    setScKind: (String) -> Unit,
    scResults: List<VideoItem>,
    setScResults: (List<VideoItem>) -> Unit,
    scError: String?,
    setScError: (String?) -> Unit,
    scSuggestion: String?,
    setScSuggestion: (String?) -> Unit,
    scCorrected: Boolean,
    remotePlaylists: List<RemotePlaylist>,
    setRemotePlaylists: (List<RemotePlaylist>) -> Unit,
    scHasMore: Boolean,
    scLoadingMore: Boolean,
    tasteProfile: TasteProfile?,
    tasteMix: List<VideoItem>,
    tasteMixTerm: String?,
    tasteMixLoading: Boolean,
    playingUrl: String?,
    isPlaying: Boolean,
    favoriteSet: Set<String>,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float>,
    scListState: LazyListState,
    doScSearch: (String) -> Unit,
    doScSearchSrc: (String, Int) -> Unit,
    doScSearchKind: (String, Int, String) -> Unit,
    onClose: () -> Unit,
    onVoice: () -> Unit,
    scope: CoroutineScope,
    onPlayVideo: (VideoItem) -> Unit,
    onPlayMix: (VideoItem) -> Unit,
    onOpenChannel: (String) -> Unit,
    onShowOptions: (VideoItem) -> Unit,
    onToggleFav: (VideoItem) -> Unit,
    onOpenRemotePlaylist: (RemotePlaylist) -> Unit,
    onLoadMore: () -> Unit
) {
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
        playingUrl = playingUrl,
        isPlaying = isPlaying,
        favoriteSet = favoriteSet,
        pinBadges = pinBadges,
        pinningKeys = pinningKeys,
        progressOf = { k -> pinProgress[k] },
        scListState = scListState,
        onQueryChange = setScQuery,
        onSearch = doScSearch,
        onClose = onClose,
        onClear = { setScQuery(""); setScResults(emptyList()); setScError(null) },
        onPickSuggestion = doScSearch,
        onClearRecents = { setRecentMusicSearches(emptyList()); try { prefs.edit().remove("recent_music_searches").apply() } catch (_: Exception) { } },
        onDeleteRecent = { s ->
            val next = recentMusicSearches.filter { it != s }
            setRecentMusicSearches(next)
            try { prefs.edit().putString("recent_music_searches", next.joinToString("\u001f")).apply() } catch (_: Exception) { }
        },
        onVoice = onVoice,
        onSourceSelect = { src -> setScSource(src); setScResults(emptyList()); setScError(null); setScSuggestion(null); scope.launch { try { scListState.scrollToItem(0) } catch (_: Exception) { } }; if (scQuery.isNotBlank()) doScSearchSrc(scQuery, src) },
        onKindSelect = { k -> if (scKind != k) { setScKind(k); setRemotePlaylists(emptyList()); setScResults(emptyList()); setScError(null); setScSuggestion(null); scope.launch { try { scListState.scrollToItem(0) } catch (_: Exception) { } }; if (scQuery.isNotBlank()) doScSearchKind(scQuery, 1, k) } },
        onTerm = { t -> setScQuery(t); doScSearch(t) },
        onPlayVideo = onPlayVideo,
        onPlayMix = onPlayMix,
        onOpenChannel = onOpenChannel,
        onOptions = onShowOptions,
        onToggleFav = onToggleFav,
        onOpenRemotePlaylist = onOpenRemotePlaylist,
        onLoadMore = onLoadMore
    )
}

@Composable
fun HistorySetBranch(
    which: Int, // 0 = showHistory, 1 = showFavorites, 2 = openRemote
    histFlowAll: Flow<PagingData<HistoryEntity>>,
    histFlowPinned: Flow<PagingData<HistoryEntity>>,
    histFlowUnpinned: Flow<PagingData<HistoryEntity>>,
    histCount: Int,
    histSearchQuery: String,
    onHistSearch: (String) -> Unit,
    histSearchResults: List<HistoryEntity>,
    pins: List<com.mohamedadel.mytube.data.local.PinEntity>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float>,
    favoriteSet: Set<String>,
    scope: CoroutineScope,
    dao: PlaylistDao,
    ctx: Context,
    toast: (String) -> Unit,
    onPlayVideo: (VideoItem) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
    onShowOptions: (VideoItem) -> Unit,
    onToggleFav: (VideoItem) -> Unit,
    favorites: List<FavoriteEntity>,
    favPinBadges: Map<String, String>,
    remotePinBadges: Map<String, String>,
    onClearFavorites: () -> Unit,
    setQueue: (List<VideoItem>) -> Unit,
    onPlayInQueue: (VideoItem) -> Unit,
    onStartRadio: (VideoItem) -> Unit,
    openRemote: RemotePlaylist?,
    setOpenRemote: (RemotePlaylist?) -> Unit,
    openRemoteLoading: Boolean,
    openRemoteItems: List<VideoItem>,
    playingUrl: String?,
    isPlaying: Boolean,
    setResults: (List<VideoItem>) -> Unit,
    setCurrentQuery: (String) -> Unit,
    onSaveRemoteList: (List<VideoItem>) -> Unit,
    openRemoteNext: org.schabi.newpipe.extractor.Page?,
    openRemoteLoadingMore: Boolean,
    onLoadMoreRemote: () -> Unit,
    onQuickPin: (VideoItem) -> Unit
) {
    when (which) {
        0 -> com.mohamedadel.mytube.ui.playlists.HistoryScreen(
            flowAll = histFlowAll,
            flowPinned = histFlowPinned,
            flowUnpinned = histFlowUnpinned,
            onPlay = onPlayVideo,
            onDelete = { scope.launch { try { dao.deleteHistory(it.favKey()) } catch (_: Exception) { toast(ctx.getString(R.string.toast_delete_item_fail)) } } },
            onClear = onClearHistory,
            onBack = onBack,
            onLongClick = onShowOptions,
            totalCount = histCount,
            searchQuery = histSearchQuery,
            onSearchQuery = onHistSearch,
            searchResults = histSearchResults,
            pinInfo = remember(pins) { pins.associateBy { it.videoKey } },
            pinningKeys = pinningKeys,
            progressOf = { k -> pinProgress[k] },
            isFav = { v -> v.favKey() in favoriteSet },
            onFav = onToggleFav
        )
        1 -> com.mohamedadel.mytube.ui.playlists.FavoritesScreen(
            items = favorites,
            onPlay = onPlayVideo,
            onToggleFav = onToggleFav,
            onClear = onClearFavorites,
            onBack = onBack,
            onLongClick = onShowOptions,
            pinBadges = favPinBadges,
            pinningKeys = pinningKeys,
            progressOf = { k -> pinProgress[k] },
            onPlayAll = {
                val list = favorites.map { it.toVideoItem() }
                if (list.isNotEmpty()) {
                    setQueue(list)
                    onPlayInQueue(list[0])
                }
            },
            onRadio = {
                val first = favorites.firstOrNull()?.toVideoItem()
                if (first != null) onStartRadio(first)
            }
        )
        else -> {
            // T5: no !! on delegated state — null renders nothing.
            val rp = openRemote
            if (rp != null) com.mohamedadel.mytube.ui.results.RemoteRoute(
                rp = rp,
                loading = openRemoteLoading,
                items = openRemoteItems,
                playingUrl = playingUrl,
                isPlaying = isPlaying,
                favSet = favoriteSet,
                pinBadges = remotePinBadges,
                pinningKeys = pinningKeys,
                progressOf = { k -> pinProgress[k] },
                onBack = onBack,
                onPlayAll = { list ->
                    setResults(list)
                    setQueue(list)
                    setCurrentQuery("")
                    setOpenRemote(null)
                    onPlayInQueue(list[0])
                },
                onPlay = onPlayVideo,
                onLongClick = onShowOptions,
                onFav = onToggleFav,
                // screens-fix Library P1-L5: remote -> local save + pin-all.
                // screens-fix Library P2 (method-size): body lives in a local fun, not inline.
                onSave = onSaveRemoteList,
                // SC sets run long: page-walk the opened playlist instead of first-page-only.
                hasMore = openRemoteNext != null,
                loadingMore = openRemoteLoadingMore,
                onShowMore = onLoadMoreRemote,
                onPinAll = { list -> scope.launch { for (v in list) { try { onQuickPin(v) } catch (_: Exception) { } } } },
            ) else { setOpenRemote(null) }
        }
    }
}
