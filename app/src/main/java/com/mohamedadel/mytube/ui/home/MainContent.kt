package com.mohamedadel.mytube.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R

/**
 * Extracted from MyTubeApp (VerifyError fix, 2026-11-12): the entire Scaffold
 * content destination-switch lives here so the activity composable stays
 * under JVM/ART method limits. Branch bodies were already extracted;
 * remaining inline bits (loading skeleton, empty state) are tiny.
 */
@Composable
fun MainContent(
    channelScreenUrl: String?,
    channelDetail: com.mohamedadel.mytube.data.youtube.ChannelDetail?,
    channelScreenVideos: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    channelScreenLoading: Boolean,
    subscriptions: List<com.mohamedadel.mytube.data.local.SubscriptionEntity>,
    ctx: android.content.Context,
    favoriteSet: Set<String>,
    pinBadges: Map<String, String>,
    pinningKeys: Set<String>,
    pinProgress: Map<String, Float>,
    channelShorts: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    channelShortsLoading: Boolean,
    channelLive: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    channelLiveLoading: Boolean,
    channelPls: List<com.mohamedadel.mytube.data.youtube.RemotePlaylist>,
    channelPlsLoading: Boolean,
    onSubscribeChannel: () -> Unit,
    onBack: () -> Unit,
    onPlayVideo: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
    onPlayInQueue: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
    onPlayFromQueue: (Int) -> Unit,
    onShowOptions: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
    onToggleFav: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
    onTabSelect: (Int) -> Unit,
    onOpenRemotePlaylist: (com.mohamedadel.mytube.data.youtube.RemotePlaylist) -> Unit,
    queue: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    setQueue: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit,
    queueIndex: Int,
    setQueueIndex: (Int) -> Unit,
    playing: com.mohamedadel.mytube.data.youtube.VideoItem?,
    radioOn: Boolean,
    setRadioOn: (Boolean) -> Unit,
    setShowQueue: (Boolean) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    dao: com.mohamedadel.mytube.data.local.PlaylistDao,
    playerExpanded: Boolean,
    showQueue: Boolean,
    showSettings: Boolean,
    settingsPane: @Composable () -> Unit,
    playlistsRoute: @Composable () -> Unit,
    trending: List<String>,
    trendTab: Int,
    onTrendTab: (Int) -> Unit,
    trendingItems: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    trendingLoading: Boolean,
    trendingError: String?,
    onRetryTrending: () -> Unit,
    trendingHasMore: Boolean,
    trendingLoadingMore: Boolean,
    onShowMoreTrending: () -> Unit,
    countryName: String,
    sectionTitle: String?,
    showMusicBadge: Boolean,
    isSuggestionsTab: Boolean,
    suggestionChips: Map<String, List<String>>,
    onSearchQ: (String) -> Unit,
    onSuggestionQ: (String) -> Unit,
    continueWatching: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    continueProgress: Map<String, Float>,
    continueResumeMs: Map<String, Long>,
    onResumePlay: (com.mohamedadel.mytube.data.youtube.VideoItem, Long) -> Unit,
    trendingCached: Boolean,
    searchError: String?,
    currentQuery: String,
    searchSuggestion: String?,
    searchCorrected: Boolean,
    remotePlaylists: List<com.mohamedadel.mytube.data.youtube.RemotePlaylist>,
    results: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    prefContentFilter: String,
    setContentFilter: (String) -> Unit,
    prefs: android.content.SharedPreferences,
    doSearchText: (String) -> Unit,
    setResults: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit,
    nextPage: org.schabi.newpipe.extractor.Page?,
    setNextPage: (org.schabi.newpipe.extractor.Page?) -> Unit,
    hasMore: Boolean,
    setHasMore: (Boolean) -> Unit,
    loadingMore: Boolean,
    setLoadingMore: (Boolean) -> Unit,
    prefCountry: String,
    showLibrary: Boolean,
    showNotifications: Boolean,
    showDownloads: Boolean,
    showLocal: Boolean,
    showGroups: Boolean,
    openGroup: com.mohamedadel.mytube.data.local.ChannelGroupEntity?,
    showFeed: Boolean,
    showSC: Boolean,
    showHistory: Boolean,
    showFavorites: Boolean,
    showPlaylists: Boolean,
    openRemote: com.mohamedadel.mytube.data.youtube.RemotePlaylist?,
    loading: Boolean,
    query: String,
    // library set
    groups: List<com.mohamedadel.mytube.data.local.ChannelGroupEntity>,
    pins: List<com.mohamedadel.mytube.data.local.PinEntity>,
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
    onPlayTop: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
    notifications: List<com.mohamedadel.mytube.data.local.NotificationItemEntity>,
    feedLoading: Boolean,
    onRefreshFeed: () -> Unit,
    onPlayNotif: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
    history: List<com.mohamedadel.mytube.data.local.HistoryEntity>,
    onDeletePin: (String) -> Unit,
    onClearAllPins: () -> Unit,
    onCancelPin: (String) -> Unit,
    pausedKeys: Set<String>,
    onPausePin: (String) -> Unit,
    onResumePin: (String) -> Unit,
    orphanInfo: Pair<Int, Long>,
    onCleanOrphans: () -> Unit,
    localTracks: List<com.mohamedadel.mytube.util.LocalTrack>,
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
    playingUrl: String?,
    isPlaying: Boolean,
    // social/feed
    groupCounts: Map<Long, Int>,
    setOpenGroup: (com.mohamedadel.mytube.data.local.ChannelGroupEntity?) -> Unit,
    onCreateGroup: (String) -> Unit,
    onRenameGroup: (com.mohamedadel.mytube.data.local.ChannelGroupEntity, String) -> Unit,
    onDeleteGroup: (com.mohamedadel.mytube.data.local.ChannelGroupEntity) -> Unit,
    groupMembers: List<com.mohamedadel.mytube.data.local.ChannelGroupMemberEntity>,
    onRemoveFromGroup: (Long, String) -> Unit,
    onAddToGroup: (Long, String) -> Unit,
    onOpenGroupFeed: (com.mohamedadel.mytube.data.local.ChannelGroupEntity, Set<String>) -> Unit,
    feedItems: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    feedFilter: String?,
    feedGroupName: String?,
    feedGroupUrls: Set<String>?,
    setFeedFilter: (String?) -> Unit,
    setFeedGroupUrls: (Set<String>?) -> Unit,
    setFeedGroupName: (String?) -> Unit,
    // music
    scQuery: String,
    setScQuery: (String) -> Unit,
    scLoading: Boolean,
    recentMusicSearches: List<String>,
    setRecentMusicSearches: (List<String>) -> Unit,
    scSource: Int,
    setScSource: (Int) -> Unit,
    scKind: String,
    setScKind: (String) -> Unit,
    scResults: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    setScResults: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit,
    scError: String?,
    setScError: (String?) -> Unit,
    scSuggestion: String?,
    setScSuggestion: (String?) -> Unit,
    scCorrected: Boolean,
    setRemotePlaylists: (List<com.mohamedadel.mytube.data.youtube.RemotePlaylist>) -> Unit,
    scHasMore: Boolean,
    scLoadingMore: Boolean,
    tasteProfile: com.mohamedadel.mytube.util.TasteProfile?,
    tasteMix: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    tasteMixTerm: String?,
    tasteMixLoading: Boolean,
    scListState: androidx.compose.foundation.lazy.LazyListState,
    doScSearch: (String) -> Unit,
    doScSearchSrc: (String, Int) -> Unit,
    doScSearchKind: (String, Int, String) -> Unit,
    onCloseMusic: () -> Unit,
    onVoice: () -> Unit,
    onPlayMix: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
    onLoadMoreSc: () -> Unit,
    // history set
    histFlowAll: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.mohamedadel.mytube.data.local.HistoryEntity>>,
    histFlowPinned: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.mohamedadel.mytube.data.local.HistoryEntity>>,
    histFlowUnpinned: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.mohamedadel.mytube.data.local.HistoryEntity>>,
    histCount: Int,
    histSearchQuery: String,
    onHistSearch: (String) -> Unit,
    histSearchResults: List<com.mohamedadel.mytube.data.local.HistoryEntity>,
    toast: (String) -> Unit,
    onClearHistory: () -> Unit,
    favorites: List<com.mohamedadel.mytube.data.local.FavoriteEntity>,
    onClearFavorites: () -> Unit,
    onStartRadio: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
    setOpenRemote: (com.mohamedadel.mytube.data.youtube.RemotePlaylist?) -> Unit,
    openRemoteLoading: Boolean,
    openRemoteItems: List<com.mohamedadel.mytube.data.youtube.VideoItem>,
    setCurrentQuery: (String) -> Unit,
    onSaveRemoteList: (List<com.mohamedadel.mytube.data.youtube.VideoItem>) -> Unit,
    openRemoteNext: org.schabi.newpipe.extractor.Page?,
    openRemoteLoadingMore: Boolean,
    onLoadMoreRemote: () -> Unit,
    onQuickPin: (com.mohamedadel.mytube.data.youtube.VideoItem) -> Unit,
) {
            when {
                // Fix 3: full channel screen with tabs (header + Videos/About)
                channelScreenUrl != null -> {
                    com.mohamedadel.mytube.ui.home.ChannelBranch(
                        channelScreenUrl = channelScreenUrl, channelDetail = channelDetail,
                        channelScreenVideos = channelScreenVideos, channelScreenLoading = channelScreenLoading,
                        subscriptions = subscriptions, ctx = ctx, favoriteSet = favoriteSet,
                        pinBadges = pinBadges, pinningKeys = pinningKeys, pinProgress = pinProgress,
                        channelShorts = channelShorts, channelShortsLoading = channelShortsLoading,
                        channelLive = channelLive, channelLiveLoading = channelLiveLoading,
                        channelPls = channelPls, channelPlsLoading = channelPlsLoading,
                        onSubscribe = onSubscribeChannel,
                        onBack = onBack,
                        onPlay = onPlayVideo,
                        onShowOptions = onShowOptions,
                        onToggleFav = onToggleFav,
                        onTabSelect = onTabSelect,
                        onOpenRemotePlaylist = onOpenRemotePlaylist
                    )
                }
                // queue button opens this panel INSTEAD of the suggestions/results below the video
                showQueue -> {
                    com.mohamedadel.mytube.ui.home.QueueBranch(
                        queue = queue, setQueue = setQueue,
                        queueIndex = queueIndex, setQueueIndex = setQueueIndex,
                        playing = playing, pinBadges = pinBadges, pinningKeys = pinningKeys,
                        pinProgress = pinProgress, radioOn = radioOn, setRadioOn = setRadioOn,
                        setShowQueue = setShowQueue, scope = scope, dao = dao,
                        onPlayFrom = { idx -> if (idx in queue.indices) onPlayInQueue(queue[idx]) },
                        onShowOptions = onShowOptions
                    )
                }
                // 2026-11-12 (runtime fix): expanded player fills the screen — plain
                // destinations hide so the player's ScrollableColumn gets a bounded
                // height (queue + channel stay visible above for management/back).
                playerExpanded && !showQueue && channelScreenUrl == null -> { }
                showLibrary || showNotifications || showDownloads || showLocal -> {
                    com.mohamedadel.mytube.ui.home.LibrarySetBranch(
                        which = if (showLibrary) 0 else if (showNotifications) 1 else if (showDownloads) 2 else 3,
                        ctx = ctx, prefs = prefs, scope = scope, dao = dao,
                        groups = groups, pins = pins, subscriptions = subscriptions,
                        feedCounts = feedCounts, topPlayedHome = topPlayedHome,
                        setShowLibrary = setShowLibrary, setFromLibrary = setFromLibrary,
                        setShowHistory = setShowHistory, setShowDownloads = setShowDownloads,
                        setShowGroups = setShowGroups, setShowFavorites = setShowFavorites,
                        setShowPlaylists = setShowPlaylists, setShowLocal = setShowLocal,
                        setShowFeed = setShowFeed,
                        onOpenLocalAudio = onOpenLocalAudio,
                        onOpenFeed = onOpenFeed,
                        onUnsubscribe = onUnsubscribe,
                        onImportSubs = onImportSubs,
                        onOpenChannel = onOpenChannel,
                        onPlayTop = onPlayVideo,
                        notifications = notifications, feedLoading = feedLoading, onRefreshFeed = onRefreshFeed,
                        onPlayVideo = onPlayVideo,
                        onPlayNotif = onPlayNotif,
                        pinProgress = pinProgress, pinningKeys = pinningKeys, history = history,
                        onDeletePin = onDeletePin, onClearAllPins = onClearAllPins,
                        onCancelPin = onCancelPin, pausedKeys = pausedKeys,
                        onPausePin = onPausePin, onResumePin = onResumePin,
                        orphanInfo = orphanInfo, onCleanOrphans = onCleanOrphans,
                        localTracks = localTracks, localFolder = localFolder,
                        setLocalFolder = setLocalFolder, onExcludeFolder = onExcludeFolder,
                        localExcl = localExcl, onUnexclude = onUnexclude,
                        localCompat = localCompat, setLocalCompat = setLocalCompat,
                        onRefreshLocalAudio = onRefreshLocalAudio, localHasPerm = localHasPerm,
                        onRequestLocalPerm = onRequestLocalPerm,
                        onBack = onBack, favoriteSet = favoriteSet,
                        onToggleFav = onToggleFav, onShowOptions = onShowOptions,
                        playingUrl = playing?.url, isPlaying = isPlaying
                    )
                }
                showGroups || openGroup != null || showFeed -> {
                    com.mohamedadel.mytube.ui.home.SocialFeedBranch(
                        which = if (showGroups) 0 else if (openGroup != null) 1 else 2,
                        groups = groups, groupCounts = groupCounts,
                        setOpenGroup = setOpenGroup, setShowGroups = setShowGroups,
                        onCreateGroup = onCreateGroup,
                        onRenameGroup = onRenameGroup,
                        onDeleteGroup = onDeleteGroup,
                        onBack = onBack,
                        openGroup = openGroup, groupMembers = groupMembers, subscriptions = subscriptions,
                        onRemoveFromGroup = onRemoveFromGroup,
                        onAddToGroup = onAddToGroup,
                        onOpenChannel = onOpenChannel,
                        scope = scope, dao = dao,
                        onOpenGroupFeed = onOpenGroupFeed,
                        feedItems = feedItems, feedLoading = feedLoading, onRefreshFeed = onRefreshFeed,
                        feedFilter = feedFilter, feedGroupName = feedGroupName, feedGroupUrls = feedGroupUrls,
                        feedCounts = feedCounts,
                        setFeedFilter = setFeedFilter,
                        setFeedGroupUrls = setFeedGroupUrls,
                        setFeedGroupName = setFeedGroupName,
                        pinBadges = pinBadges, pinningKeys = pinningKeys, pinProgress = pinProgress,
                        favoriteSet = favoriteSet,
                        onToggleFav = onToggleFav, onShowOptions = onShowOptions,
                        onPlayVideo = onPlayVideo
                    )
                }
                showSC -> {
                    com.mohamedadel.mytube.ui.home.MusicBranch(
                        scQuery = scQuery, setScQuery = setScQuery, scLoading = scLoading,
                        recentMusicSearches = recentMusicSearches,
                        setRecentMusicSearches = setRecentMusicSearches, prefs = prefs,
                        scSource = scSource, setScSource = setScSource,
                        scKind = scKind, setScKind = setScKind,
                        scResults = scResults, setScResults = setScResults,
                        scError = scError, setScError = setScError,
                        scSuggestion = scSuggestion, setScSuggestion = setScSuggestion,
                        scCorrected = scCorrected,
                        remotePlaylists = remotePlaylists, setRemotePlaylists = setRemotePlaylists,
                        scHasMore = scHasMore, scLoadingMore = scLoadingMore,
                        tasteProfile = tasteProfile, tasteMix = tasteMix,
                        tasteMixTerm = tasteMixTerm, tasteMixLoading = tasteMixLoading,
                        playingUrl = playing?.url, isPlaying = isPlaying, favoriteSet = favoriteSet,
                        pinBadges = pinBadges, pinningKeys = pinningKeys, pinProgress = pinProgress,
                        scListState = scListState,
                        doScSearch = doScSearch,
                        doScSearchSrc = doScSearchSrc,
                        doScSearchKind = doScSearchKind,
                        onClose = onCloseMusic, onVoice = onVoice, scope = scope,
                        onPlayVideo = onPlayVideo,
                        onPlayMix = onPlayMix,
                        onOpenChannel = onOpenChannel,
                        onShowOptions = onShowOptions,
                        onToggleFav = onToggleFav,
                        onOpenRemotePlaylist = onOpenRemotePlaylist,
                        onLoadMore = onLoadMoreSc
                    )
                }
                showSettings -> { settingsPane() }
                showHistory || showFavorites -> {
                    com.mohamedadel.mytube.ui.home.HistorySetBranch(
                        which = if (showHistory) 0 else 1,
                        histFlowAll = histFlowAll, histFlowPinned = histFlowPinned,
                        histFlowUnpinned = histFlowUnpinned, histCount = histCount,
                        histSearchQuery = histSearchQuery, onHistSearch = onHistSearch,
                        histSearchResults = histSearchResults, pins = pins,
                        pinningKeys = pinningKeys, pinProgress = pinProgress, favoriteSet = favoriteSet,
                        scope = scope, dao = dao, ctx = ctx,
                        toast = toast,
                        onPlayVideo = onPlayVideo,
                        onClearHistory = onClearHistory, onBack = onBack,
                        onShowOptions = onShowOptions, onToggleFav = onToggleFav,
                        favorites = favorites, favPinBadges = pinBadges, remotePinBadges = pinBadges,
                        onClearFavorites = onClearFavorites,
                        setQueue = setQueue,
                        onPlayInQueue = onPlayInQueue,
                        onStartRadio = onStartRadio,
                        openRemote = openRemote, setOpenRemote = setOpenRemote,
                        openRemoteLoading = openRemoteLoading, openRemoteItems = openRemoteItems,
                        playingUrl = playing?.url, isPlaying = isPlaying,
                        setResults = setResults, setCurrentQuery = setCurrentQuery,
                        onSaveRemoteList = onSaveRemoteList,
                        openRemoteNext = openRemoteNext, openRemoteLoadingMore = openRemoteLoadingMore,
                        onLoadMoreRemote = onLoadMoreRemote,
                        onQuickPin = onQuickPin
                    )
                }
                showPlaylists -> {
                    playlistsRoute()
                }
                openRemote != null -> {
                    com.mohamedadel.mytube.ui.home.HistorySetBranch(
                        which = 2,
                        histFlowAll = histFlowAll, histFlowPinned = histFlowPinned,
                        histFlowUnpinned = histFlowUnpinned, histCount = histCount,
                        histSearchQuery = histSearchQuery, onHistSearch = onHistSearch,
                        histSearchResults = histSearchResults, pins = pins,
                        pinningKeys = pinningKeys, pinProgress = pinProgress, favoriteSet = favoriteSet,
                        scope = scope, dao = dao, ctx = ctx,
                        toast = toast,
                        onPlayVideo = onPlayVideo,
                        onClearHistory = onClearHistory, onBack = onBack,
                        onShowOptions = onShowOptions, onToggleFav = onToggleFav,
                        favorites = favorites, favPinBadges = pinBadges, remotePinBadges = pinBadges,
                        onClearFavorites = onClearFavorites,
                        setQueue = setQueue,
                        onPlayInQueue = onPlayInQueue,
                        onStartRadio = onStartRadio,
                        openRemote = openRemote, setOpenRemote = setOpenRemote,
                        openRemoteLoading = openRemoteLoading, openRemoteItems = openRemoteItems,
                        playingUrl = playing?.url, isPlaying = isPlaying,
                        setResults = setResults, setCurrentQuery = setCurrentQuery,
                        onSaveRemoteList = onSaveRemoteList,
                        openRemoteNext = openRemoteNext, openRemoteLoadingMore = openRemoteLoadingMore,
                        onLoadMoreRemote = onLoadMoreRemote,
                        onQuickPin = onQuickPin
                    )
                }
                loading -> {
                    com.mohamedadel.mytube.ui.results.VideoListSkeleton(count = 5)
                }
                results.isEmpty() && query.isBlank() -> {
                    // VerifyError fix (2026-11-12): home branch lives in ui.home.HomeBranches.
                    com.mohamedadel.mytube.ui.home.HomeBranch(
                        trending = trending,
                        trendTab = trendTab,
                        trendingItems = trendingItems,
                        trendingLoading = trendingLoading,
                        trendingError = trendingError,
                        trendingHasMore = trendingHasMore,
                        trendingLoadingMore = trendingLoadingMore,
                        countryName = countryName,
                        sectionTitle = sectionTitle,
                        showMusicBadge = showMusicBadge,
                        isSuggestionsTab = isSuggestionsTab,
                        suggestionChips = suggestionChips,
                        continueWatching = continueWatching,
                        continueProgress = continueProgress,
                        continueResumeMs = continueResumeMs,
                        trendingCached = trendingCached,
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        pinProgress = pinProgress,
                        favoriteSet = favoriteSet,
                        onTrendTab = onTrendTab,
                        onRetryTrending = onRetryTrending,
                        onShowMore = onShowMoreTrending,
                        onSearchQ = onSearchQ,
                        onSuggestionQ = onSearchQ,
                        onPlayItem = onPlayVideo,
                        onFavToggle = onToggleFav,
                        onShowOptions = onShowOptions,
                        onResumePlay = onResumePlay
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
                                androidx.compose.material3.Button(onClick = { doSearchText(currentQuery) }) { Text(stringResource(R.string.retry)) }
                            }
                        }
                    }
                }
                else -> {
                    // VerifyError fix (2026-11-12): results branch lives in ui.home.HomeBranches.
                    com.mohamedadel.mytube.ui.home.ResultsBranch(
                        results = results,
                        playing = playing,
                        isPlaying = isPlaying,
                        favoriteSet = favoriteSet,
                        pinBadges = pinBadges,
                        pinningKeys = pinningKeys,
                        pinProgress = pinProgress,
                        prefContentFilter = prefContentFilter,
                        currentQuery = currentQuery,
                        searchSuggestion = searchSuggestion,
                        searchCorrected = searchCorrected,
                        searchError = searchError,
                        remotePlaylists = remotePlaylists,
                        loadingMore = loadingMore,
                        hasMore = hasMore,
                        nextPage = nextPage,
                        prefCountry = prefCountry,
                        prefs = prefs,
                        scope = scope,
                        listState = listState,
                        setContentFilter = setContentFilter,
                        doSearchText = doSearchText,
                        setResults = setResults,
                        setNextPage = setNextPage,
                        setHasMore = setHasMore,
                        setLoadingMore = setLoadingMore,
                        onOpenRemotePlaylist = onOpenRemotePlaylist,
                        onPlayItem = onPlayVideo,
                        onShowOptions = onShowOptions,
                        onToggleFav = onToggleFav
                    )
                }
            }
}
