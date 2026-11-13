package com.mohamedadel.mytube.ui.navigation

/**
 * Phase 2.3 slice 20 (2026-11-13): back-stack decision extracted from the
 * God-Activity as a pure function. No Compose, no Android — unit-testable.
 * The activity builds the snapshot from its flags and applies the action
 * (flag writes + the three VM calls + scroll save stay caller-side).
 * Every function <= 30 lines.
 */
data class NavState(
    val showNotifications: Boolean,
    val openGroup: Boolean,
    val showGroups: Boolean,
    val fromLibrary: Boolean,
    val feedGroupOpen: Boolean,
    val showDownloads: Boolean,
    val showLocal: Boolean,
    val showFeed: Boolean,
    val channelOpen: Boolean,
    val showComments: Boolean,
    val isFullscreen: Boolean,
    val playerExpanded: Boolean,
    val showHistory: Boolean,
    val showPlaylists: Boolean,
    val openPlaylist: Boolean,
    val showFavorites: Boolean,
    val showQueue: Boolean,
    val showSearch: Boolean,
    val showSettings: Boolean,
    val openRemote: Boolean,
    val showLibrary: Boolean,
    val showSC: Boolean,
    val hasResults: Boolean
)

sealed interface NavAction {
    data object None : NavAction
    data object CloseNotifications : NavAction
    data object GroupToGroups : NavAction
    data object CloseGroups : NavAction
    data object CloseGroupFeed : NavAction
    data object CloseDownloads : NavAction
    data object CloseLocal : NavAction
    data object CloseFeed : NavAction
    data object CloseChannel : NavAction
    data object CloseComments : NavAction
    data object ExitFullscreen : NavAction
    data object CollapsePlayer : NavAction
    data object CloseHistory : NavAction
    data object ClosePlaylistDetail : NavAction
    data object ClosePlaylists : NavAction
    data object CloseFavorites : NavAction
    data object CloseQueue : NavAction
    data object CloseSearch : NavAction
    data object CloseSettings : NavAction
    data object ClearOpenRemote : NavAction
    data object CloseLibrary : NavAction
    data object CloseSC : NavAction
    data object ClearResults : NavAction
}

object NavStack {
    fun decide(s: NavState): NavAction {
        if (s.showNotifications) return NavAction.CloseNotifications
        if (s.openGroup) return NavAction.GroupToGroups
        if (s.showGroups) return NavAction.CloseGroups
        if (s.feedGroupOpen && s.showFeed) return NavAction.CloseGroupFeed
        if (s.showDownloads) return NavAction.CloseDownloads
        if (s.showLocal) return NavAction.CloseLocal
        if (s.showFeed) return NavAction.CloseFeed
        if (s.channelOpen) return NavAction.CloseChannel
        if (s.showComments) return NavAction.CloseComments
        if (s.isFullscreen) return NavAction.ExitFullscreen
        if (s.playerExpanded) return NavAction.CollapsePlayer
        if (s.showHistory) return NavAction.CloseHistory
        if (s.showPlaylists) return closePlaylists(s)
        if (s.showFavorites) return NavAction.CloseFavorites
        if (s.showQueue) return NavAction.CloseQueue
        if (s.showSearch) return NavAction.CloseSearch
        if (s.showSettings) return NavAction.CloseSettings
        if (s.openRemote) return NavAction.ClearOpenRemote
        if (s.showLibrary) return NavAction.CloseLibrary
        if (s.showSC) return NavAction.CloseSC
        if (s.hasResults) return NavAction.ClearResults
        return NavAction.None
    }

    private fun closePlaylists(s: NavState): NavAction =
        if (s.openPlaylist) NavAction.ClosePlaylistDetail else NavAction.ClosePlaylists
}
