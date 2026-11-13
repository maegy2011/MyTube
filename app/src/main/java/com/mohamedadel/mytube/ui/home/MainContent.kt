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
 * remaining inline bits (h.flags.loading skeleton, empty state) are tiny.
 */
@Composable
fun MainContent(
    h: MainContentHost,
    settingsPane: @Composable () -> Unit,
    playlistsRoute: @Composable () -> Unit
) {
            when {
                // Fix 3: full channel screen with tabs (header + Videos/About)
                h.ch.channelScreenUrl != null -> {
                    com.mohamedadel.mytube.ui.home.ChannelBranch(
                        channelScreenUrl = h.ch.channelScreenUrl, channelDetail = h.ch.channelDetail,
                        channelScreenVideos = h.ch.channelScreenVideos, channelScreenLoading = h.ch.channelScreenLoading,
                        subscriptions = h.ch.subscriptions, ctx = h.ch.ctx, favoriteSet = h.ch.favoriteSet,
                        pinBadges = h.ch.pinBadges, pinningKeys = h.ch.pinningKeys, pinProgress = h.ch.pinProgress,
                        channelShorts = h.ch.channelShorts, channelShortsLoading = h.ch.channelShortsLoading,
                        channelLive = h.ch.channelLive, channelLiveLoading = h.ch.channelLiveLoading,
                        channelPls = h.ch.channelPls, channelPlsLoading = h.ch.channelPlsLoading,
                        onSubscribe = h.ch.onSubscribeChannel,
                        onBack = h.ch.onBack,
                        onPlay = h.ch.onPlayVideo,
                        onShowOptions = h.ch.onShowOptions,
                        onToggleFav = h.ch.onToggleFav,
                        onTabSelect = h.ch.onTabSelect,
                        onOpenRemotePlaylist = h.ch.onOpenRemotePlaylist
                    )
                }
                // h.qu.queue button opens this panel INSTEAD of the suggestions/h.home.results below the video
                h.home.showQueue -> {
                    com.mohamedadel.mytube.ui.home.QueueBranch(
                        queue = h.qu.queue, setQueue = h.qu.setQueue,
                        queueIndex = h.qu.queueIndex, setQueueIndex = h.qu.setQueueIndex,
                        playing = h.qu.playing, pinBadges = h.ch.pinBadges, pinningKeys = h.ch.pinningKeys,
                        pinProgress = h.ch.pinProgress, radioOn = h.qu.radioOn, setRadioOn = h.qu.setRadioOn,
                        setShowQueue = h.qu.setShowQueue, scope = h.qu.scope, dao = h.qu.dao,
                        onPlayFrom = { idx -> if (idx in h.qu.queue.indices) h.ch.onPlayInQueue(h.qu.queue[idx]) },
                        onShowOptions = h.ch.onShowOptions
                    )
                }
                // 2026-11-12 (runtime fix): expanded player fills the screen — plain
                // destinations hide so the player's ScrollableColumn gets a bounded
                // height (h.qu.queue + channel stay visible above for management/back).
                h.home.playerExpanded && !h.home.showQueue && h.ch.channelScreenUrl == null -> { }
                h.flags.showLibrary || h.flags.showNotifications || h.flags.showDownloads || h.flags.showLocal -> {
                    com.mohamedadel.mytube.ui.home.LibrarySetBranch(
                        which = if (h.flags.showLibrary) 0 else if (h.flags.showNotifications) 1 else if (h.flags.showDownloads) 2 else 3,
                        ctx = h.ch.ctx, prefs = h.home.prefs, scope = h.qu.scope, dao = h.qu.dao,
                        groups = h.lib.groups, pins = h.lib.pins, subscriptions = h.ch.subscriptions,
                        feedCounts = h.lib.feedCounts, topPlayedHome = h.lib.topPlayedHome,
                        setShowLibrary = h.lib.setShowLibrary, setFromLibrary = h.lib.setFromLibrary,
                        setShowHistory = h.lib.setShowHistory, setShowDownloads = h.lib.setShowDownloads,
                        setShowGroups = h.lib.setShowGroups, setShowFavorites = h.lib.setShowFavorites,
                        setShowPlaylists = h.lib.setShowPlaylists, setShowLocal = h.lib.setShowLocal,
                        setShowFeed = h.lib.setShowFeed,
                        onOpenLocalAudio = h.lib.onOpenLocalAudio,
                        onOpenFeed = h.lib.onOpenFeed,
                        onUnsubscribe = h.lib.onUnsubscribe,
                        onImportSubs = h.lib.onImportSubs,
                        onOpenChannel = h.lib.onOpenChannel,
                        onPlayTop = h.ch.onPlayVideo,
                        notifications = h.lib.notifications, feedLoading = h.lib.feedLoading, onRefreshFeed = h.lib.onRefreshFeed,
                        onPlayVideo = h.ch.onPlayVideo,
                        onPlayNotif = h.lib.onPlayNotif,
                        pinProgress = h.ch.pinProgress, pinningKeys = h.ch.pinningKeys, history = h.lib.history,
                        onDeletePin = h.lib.onDeletePin, onClearAllPins = h.lib.onClearAllPins,
                        onCancelPin = h.lib.onCancelPin, pausedKeys = h.lib.pausedKeys,
                        onPausePin = h.lib.onPausePin, onResumePin = h.lib.onResumePin,
                        orphanInfo = h.lib.orphanInfo, onCleanOrphans = h.lib.onCleanOrphans,
                        localTracks = h.lib.localTracks, localFolder = h.lib.localFolder,
                        setLocalFolder = h.lib.setLocalFolder, onExcludeFolder = h.lib.onExcludeFolder,
                        localExcl = h.lib.localExcl, onUnexclude = h.lib.onUnexclude,
                        localCompat = h.lib.localCompat, setLocalCompat = h.lib.setLocalCompat,
                        onRefreshLocalAudio = h.lib.onRefreshLocalAudio, localHasPerm = h.lib.localHasPerm,
                        onRequestLocalPerm = h.lib.onRequestLocalPerm,
                        onBack = h.ch.onBack, favoriteSet = h.ch.favoriteSet,
                        onToggleFav = h.ch.onToggleFav, onShowOptions = h.ch.onShowOptions,
                        playingUrl = h.qu.playing?.url, isPlaying = h.lib.isPlaying
                    )
                }
                h.flags.showGroups || h.flags.openGroup != null || h.flags.showFeed -> {
                    com.mohamedadel.mytube.ui.home.SocialFeedBranch(
                        which = if (h.flags.showGroups) 0 else if (h.flags.openGroup != null) 1 else 2,
                        groups = h.lib.groups, groupCounts = h.soc.groupCounts,
                        setOpenGroup = h.soc.setOpenGroup, setShowGroups = h.lib.setShowGroups,
                        onCreateGroup = h.soc.onCreateGroup,
                        onRenameGroup = h.soc.onRenameGroup,
                        onDeleteGroup = h.soc.onDeleteGroup,
                        onBack = h.ch.onBack,
                        openGroup = h.flags.openGroup, groupMembers = h.soc.groupMembers, subscriptions = h.ch.subscriptions,
                        onRemoveFromGroup = h.soc.onRemoveFromGroup,
                        onAddToGroup = h.soc.onAddToGroup,
                        onOpenChannel = h.lib.onOpenChannel,
                        scope = h.qu.scope, dao = h.qu.dao,
                        onOpenGroupFeed = h.soc.onOpenGroupFeed,
                        feedItems = h.soc.feedItems, feedLoading = h.lib.feedLoading, onRefreshFeed = h.lib.onRefreshFeed,
                        feedFilter = h.soc.feedFilter, feedGroupName = h.soc.feedGroupName, feedGroupUrls = h.soc.feedGroupUrls,
                        feedCounts = h.lib.feedCounts,
                        setFeedFilter = h.soc.setFeedFilter,
                        setFeedGroupUrls = h.soc.setFeedGroupUrls,
                        setFeedGroupName = h.soc.setFeedGroupName,
                        pinBadges = h.ch.pinBadges, pinningKeys = h.ch.pinningKeys, pinProgress = h.ch.pinProgress,
                        favoriteSet = h.ch.favoriteSet,
                        onToggleFav = h.ch.onToggleFav, onShowOptions = h.ch.onShowOptions,
                        onPlayVideo = h.ch.onPlayVideo
                    )
                }
                h.flags.showSC -> {
                    com.mohamedadel.mytube.ui.home.MusicBranch(
                        scQuery = h.mus.scQuery, setScQuery = h.mus.setScQuery, scLoading = h.mus.scLoading,
                        recentMusicSearches = h.mus.recentMusicSearches,
                        setRecentMusicSearches = h.mus.setRecentMusicSearches, prefs = h.home.prefs,
                        scSource = h.mus.scSource, setScSource = h.mus.setScSource,
                        scKind = h.mus.scKind, setScKind = h.mus.setScKind,
                        scResults = h.mus.scResults, setScResults = h.mus.setScResults,
                        scError = h.mus.scError, setScError = h.mus.setScError,
                        scSuggestion = h.mus.scSuggestion, setScSuggestion = h.mus.setScSuggestion,
                        scCorrected = h.mus.scCorrected,
                        remotePlaylists = h.home.remotePlaylists, setRemotePlaylists = h.mus.setRemotePlaylists,
                        scHasMore = h.mus.scHasMore, scLoadingMore = h.mus.scLoadingMore,
                        tasteProfile = h.mus.tasteProfile, tasteMix = h.mus.tasteMix,
                        tasteMixTerm = h.mus.tasteMixTerm, tasteMixLoading = h.mus.tasteMixLoading,
                        playingUrl = h.qu.playing?.url, isPlaying = h.lib.isPlaying, favoriteSet = h.ch.favoriteSet,
                        pinBadges = h.ch.pinBadges, pinningKeys = h.ch.pinningKeys, pinProgress = h.ch.pinProgress,
                        scListState = h.mus.scListState,
                        doScSearch = h.mus.doScSearch,
                        doScSearchSrc = h.mus.doScSearchSrc,
                        doScSearchKind = h.mus.doScSearchKind,
                        onClose = h.mus.onCloseMusic, onVoice = h.mus.onVoice, scope = h.qu.scope,
                        onPlayVideo = h.ch.onPlayVideo,
                        onPlayMix = h.mus.onPlayMix,
                        onOpenChannel = h.lib.onOpenChannel,
                        onShowOptions = h.ch.onShowOptions,
                        onToggleFav = h.ch.onToggleFav,
                        onOpenRemotePlaylist = h.ch.onOpenRemotePlaylist,
                        onLoadMore = h.mus.onLoadMoreSc
                    )
                }
                h.home.showSettings -> { settingsPane() }
                h.flags.showHistory || h.flags.showFavorites -> {
                    com.mohamedadel.mytube.ui.home.HistorySetBranch(
                        which = if (h.flags.showHistory) 0 else 1,
                        histFlowAll = h.his.histFlowAll, histFlowPinned = h.his.histFlowPinned,
                        histFlowUnpinned = h.his.histFlowUnpinned, histCount = h.his.histCount,
                        histSearchQuery = h.his.histSearchQuery, onHistSearch = h.his.onHistSearch,
                        histSearchResults = h.his.histSearchResults, pins = h.lib.pins,
                        pinningKeys = h.ch.pinningKeys, pinProgress = h.ch.pinProgress, favoriteSet = h.ch.favoriteSet,
                        scope = h.qu.scope, dao = h.qu.dao, ctx = h.ch.ctx,
                        toast = h.his.toast,
                        onPlayVideo = h.ch.onPlayVideo,
                        onClearHistory = h.his.onClearHistory, onBack = h.ch.onBack,
                        onShowOptions = h.ch.onShowOptions, onToggleFav = h.ch.onToggleFav,
                        favorites = h.his.favorites, favPinBadges = h.ch.pinBadges, remotePinBadges = h.ch.pinBadges,
                        onClearFavorites = h.his.onClearFavorites,
                        setQueue = h.qu.setQueue,
                        onPlayInQueue = h.ch.onPlayInQueue,
                        onStartRadio = h.his.onStartRadio,
                        openRemote = h.flags.openRemote, setOpenRemote = h.his.setOpenRemote,
                        openRemoteLoading = h.his.openRemoteLoading, openRemoteItems = h.his.openRemoteItems,
                        playingUrl = h.qu.playing?.url, isPlaying = h.lib.isPlaying,
                        setResults = h.home.setResults, setCurrentQuery = h.his.setCurrentQuery,
                        onSaveRemoteList = h.his.onSaveRemoteList,
                        openRemoteNext = h.his.openRemoteNext, openRemoteLoadingMore = h.his.openRemoteLoadingMore,
                        onLoadMoreRemote = h.his.onLoadMoreRemote,
                        onQuickPin = h.his.onQuickPin
                    )
                }
                h.flags.showPlaylists -> {
                    playlistsRoute()
                }
                h.flags.openRemote != null -> {
                    com.mohamedadel.mytube.ui.home.HistorySetBranch(
                        which = 2,
                        histFlowAll = h.his.histFlowAll, histFlowPinned = h.his.histFlowPinned,
                        histFlowUnpinned = h.his.histFlowUnpinned, histCount = h.his.histCount,
                        histSearchQuery = h.his.histSearchQuery, onHistSearch = h.his.onHistSearch,
                        histSearchResults = h.his.histSearchResults, pins = h.lib.pins,
                        pinningKeys = h.ch.pinningKeys, pinProgress = h.ch.pinProgress, favoriteSet = h.ch.favoriteSet,
                        scope = h.qu.scope, dao = h.qu.dao, ctx = h.ch.ctx,
                        toast = h.his.toast,
                        onPlayVideo = h.ch.onPlayVideo,
                        onClearHistory = h.his.onClearHistory, onBack = h.ch.onBack,
                        onShowOptions = h.ch.onShowOptions, onToggleFav = h.ch.onToggleFav,
                        favorites = h.his.favorites, favPinBadges = h.ch.pinBadges, remotePinBadges = h.ch.pinBadges,
                        onClearFavorites = h.his.onClearFavorites,
                        setQueue = h.qu.setQueue,
                        onPlayInQueue = h.ch.onPlayInQueue,
                        onStartRadio = h.his.onStartRadio,
                        openRemote = h.flags.openRemote, setOpenRemote = h.his.setOpenRemote,
                        openRemoteLoading = h.his.openRemoteLoading, openRemoteItems = h.his.openRemoteItems,
                        playingUrl = h.qu.playing?.url, isPlaying = h.lib.isPlaying,
                        setResults = h.home.setResults, setCurrentQuery = h.his.setCurrentQuery,
                        onSaveRemoteList = h.his.onSaveRemoteList,
                        openRemoteNext = h.his.openRemoteNext, openRemoteLoadingMore = h.his.openRemoteLoadingMore,
                        onLoadMoreRemote = h.his.onLoadMoreRemote,
                        onQuickPin = h.his.onQuickPin
                    )
                }
                h.flags.loading -> {
                    com.mohamedadel.mytube.ui.results.VideoListSkeleton(count = 5)
                }
                h.home.results.isEmpty() && h.flags.query.isBlank() -> {
                    // VerifyError fix (2026-11-12): home branch lives in ui.home.HomeBranches.
                    com.mohamedadel.mytube.ui.home.HomeBranch(
                        trending = h.home.trending,
                        trendTab = h.home.trendTab,
                        trendingItems = h.home.trendingItems,
                        trendingLoading = h.home.trendingLoading,
                        trendingError = h.home.trendingError,
                        trendingHasMore = h.home.trendingHasMore,
                        trendingLoadingMore = h.home.trendingLoadingMore,
                        countryName = h.home.countryName,
                        sectionTitle = h.home.sectionTitle,
                        showMusicBadge = h.home.showMusicBadge,
                        isSuggestionsTab = h.home.isSuggestionsTab,
                        suggestionChips = h.home.suggestionChips,
                        continueWatching = h.home.continueWatching,
                        continueProgress = h.home.continueProgress,
                        continueResumeMs = h.home.continueResumeMs,
                        trendingCached = h.home.trendingCached,
                        pinBadges = h.ch.pinBadges,
                        pinningKeys = h.ch.pinningKeys,
                        pinProgress = h.ch.pinProgress,
                        favoriteSet = h.ch.favoriteSet,
                        onTrendTab = h.home.onTrendTab,
                        onRetryTrending = h.home.onRetryTrending,
                        onShowMore = h.home.onShowMoreTrending,
                        onSearchQ = h.home.onSearchQ,
                        onSuggestionQ = h.home.onSearchQ,
                        onPlayItem = h.ch.onPlayVideo,
                        onFavToggle = h.ch.onToggleFav,
                        onShowOptions = h.ch.onShowOptions,
                        onResumePlay = h.home.onResumePlay
                    )
                }
                h.home.results.isEmpty() && !h.flags.loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Rounded.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text(
                                run { val se = h.home.searchError; if (se != null) se else stringResource(R.string.no_results) },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                if (h.home.searchError != null) stringResource(R.string.check_connection) else stringResource(R.string.search_try_other),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (h.home.searchError != null && h.home.currentQuery.isNotBlank()) {
                                Spacer(Modifier.height(12.dp))
                                androidx.compose.material3.Button(onClick = { h.home.doSearchText(h.home.currentQuery) }) { Text(stringResource(R.string.retry)) }
                            }
                        }
                    }
                }
                else -> {
                    // VerifyError fix (2026-11-12): h.home.results branch lives in ui.home.HomeBranches.
                    com.mohamedadel.mytube.ui.home.ResultsBranch(
                        results = h.home.results,
                        playing = h.qu.playing,
                        isPlaying = h.lib.isPlaying,
                        favoriteSet = h.ch.favoriteSet,
                        pinBadges = h.ch.pinBadges,
                        pinningKeys = h.ch.pinningKeys,
                        pinProgress = h.ch.pinProgress,
                        prefContentFilter = h.home.prefContentFilter,
                        currentQuery = h.home.currentQuery,
                        searchSuggestion = h.home.searchSuggestion,
                        searchCorrected = h.home.searchCorrected,
                        searchError = h.home.searchError,
                        remotePlaylists = h.home.remotePlaylists,
                        loadingMore = h.home.loadingMore,
                        hasMore = h.home.hasMore,
                        nextPage = h.home.nextPage,
                        prefCountry = h.home.prefCountry,
                        prefs = h.home.prefs,
                        scope = h.qu.scope,
                        listState = h.home.listState,
                        setContentFilter = h.home.setContentFilter,
                        doSearchText = h.home.doSearchText,
                        setResults = h.home.setResults,
                        setNextPage = h.home.setNextPage,
                        setHasMore = h.home.setHasMore,
                        setLoadingMore = h.home.setLoadingMore,
                        onOpenRemotePlaylist = h.ch.onOpenRemotePlaylist,
                        onPlayItem = h.ch.onPlayVideo,
                        onShowOptions = h.ch.onShowOptions,
                        onToggleFav = h.ch.onToggleFav
                    )
                }
            }
}
