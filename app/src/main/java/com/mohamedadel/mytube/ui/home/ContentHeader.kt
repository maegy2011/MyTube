package com.mohamedadel.mytube.ui.home

import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import com.mohamedadel.mytube.data.unified.SearchSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.unified.SourceResult
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.ui.search.MainSearchViewModel
import com.mohamedadel.mytube.ui.search.MusicSearchViewModel
import com.mohamedadel.mytube.ui.search.SearchViewModel
import com.mohamedadel.mytube.ui.search.UnifiedSearchViewModel

/**
 * Phase 3 slice 32 (2026-11-14): ContentHeader extracted from MainActivity.
 * Pure wiring: snapshots + VM calls via host; bodies identical.
 */
interface ContentHeaderHost {
    fun isFullscreen(): Boolean
    fun batteryExempt(): Boolean
    fun requestBatteryExemption()
    fun canNavigateBack(): Boolean
    fun goBack()
    fun openNotifications()
    fun openSearch()
    fun unreadCount(): Int
    fun loading(): Boolean
    fun showSearch(): Boolean
    fun setShowSearch(b: Boolean)
    fun query(): String
    fun onQueryChange(q: String)
    fun scLoading(): Boolean
    fun unifiedLoading(): Boolean
    fun unifiedSource(): SearchSource?
    fun setUnifiedSource(s: SearchSource?)
    fun uYT(): SourceResult?
    fun uSC(): SourceResult?
    fun uYM(): SourceResult?
    fun unifiedExpanded(): SearchSource?
    fun setUnifiedExpanded(s: SearchSource?)
    fun suggestions(): List<String>
    fun suggestionsShown(): Boolean
    fun setSuggestionsShown(b: Boolean)
    fun recentSearches(): List<String>
    fun recentMusicSearches(): List<String>
    fun prefs(): SharedPreferences
    fun playingNow(): VideoItem?
    fun isPlayingNow(): Boolean
    fun favoriteSet(): Set<String>
    fun pinBadges(): Map<String, String>
    fun pinningKeys(): Set<String>
    fun pinProgress(): Map<String, Float>
    fun setShowSC(b: Boolean)
    fun doSearchIn(q: String)
    fun doScSearchIn(q: String, src: Int)
    fun doUnifiedSearchIn(q: String)
    fun retryUnifiedSourceIn(q: String, s: SearchSource)
    fun clearUnified()
    fun onVoiceSearch()
    fun playSearchResult(v: VideoItem)
    fun setOptionsFor(v: VideoItem?)
    fun toggleFav(v: VideoItem)
    fun uniVm(): UnifiedSearchViewModel
    fun mainSearchVm(): MainSearchViewModel
    fun musicSearchVm(): MusicSearchViewModel
    fun svm(): SearchViewModel
    fun countryOrNull(): String?
}

@Composable
fun ContentHeaderHost.ContentHeader() {
    // Battery-exemption banner: visible only while the exemption is missing (never in fullscreen)
    if (!isFullscreen() && !batteryExempt()) {
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
                androidx.compose.material3.Button(onClick = { requestBatteryExemption() }) {
                    Text(stringResource(R.string.allow))
                }
            }
        }
    }
    // Search + suggestions are hidden in fullscreen: they stole vertical space and
    // squeezed the video to "app size" instead of true fullscreen.
    if (!isFullscreen()) {
    // Compact header: nav + logo + search icon only (YouTube-style).
    // Tapping search opens a standalone search screen; the inline field is gone.
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2026-11-12 (runtime fix): hierarchical Up on EVERY non-root screen —
        // pops the nav stack via smartBack(), never jumps home. Root = home feed
        // with nothing playing and no results. Mirrors the BackHandler enable set.
        val canGoBack = canNavigateBack()
        if (canGoBack) {
            IconButton(onClick = { goBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
        }
        Icon(myTubeLogo(), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(30.dp))
        Spacer(Modifier.weight(1f))
        // notif 2026-11-12: bell with unread badge -> Notifications screen.
        androidx.compose.material3.BadgedBox(
            badge = { if (unreadCount > 0) androidx.compose.material3.Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) } }
        ) {
            IconButton(onClick = { openNotifications() }) {
                Icon(Icons.Rounded.Notifications, contentDescription = stringResource(R.string.notif_bell))
            }
        }
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = { openSearch() }) {
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
        setUnifiedExpanded(null)
        uniVm().search(q, countryOrNull()) { mainSearchVm().rememberRecent(it) }
    }
    fun retryUnifiedSource(q: String, s: com.mohamedadel.mytube.data.unified.SearchSource) {
        uniVm().retrySource(q, s, countryOrNull())
    }
    if (showSearch()) {
        // VerifyError fix (2026-11-12): overlay lives in ui.home.UnifiedSearchOverlay.
        UnifiedSearchOverlay(
            query = query(),
            loading = loading(),
            scLoading = scLoading(),
            unifiedLoading = unifiedLoading(),
            unifiedSource = unifiedSource(),
            uYT = uYT(), uSC = uSC(), uYM = uYM(),
            unifiedExpanded = unifiedExpanded(),
            suggestions = suggestions(),
            showSuggestions = suggestionsShown(),
            recentSearches = recentSearches(),
            recentMusicSearches = recentMusicSearches(),
            setRecentSearches = { mainSearchVm().assignRecentSearches(it) },
            setRecentMusicSearches = { musicSearchVm().assignRecentMusic(it) },
            prefs = prefs(),
            playing = playingNow(),
            isPlaying = isPlayingNow(),
            favoriteSet = favoriteSet(),
            pinBadges = pinBadges(),
            pinningKeys = pinningKeys(),
            pinProgress = pinProgress(),
            onQueryChange = { onQueryChange(it) },
            onSelectSource = { setUnifiedSource(it) },
            doSearch = { q -> doSearchIn(q) },
            doScSearch = { q, src -> doScSearchIn(q, src) },
            doUnifiedSearch = { q -> doUnifiedSearchIn(q) },
            setShowSearch = { setShowSearch(it) },
            setShowSC = { setShowSC(it) },
            setScQuery = { musicSearchVm().setScQuery(it) },
            setShowSuggestions = { setSuggestionsShown(it) },
            clearUnifiedResults = { clearUnified() },
            retryUnifiedSource = { q, s -> retryUnifiedSourceIn(q, s) },
            onVoice = { onVoiceSearch() },
            onPlayItem = { v -> playSearchResult(v) },
            onShowOptions = { setOptionsFor(it) },
            onToggleFav = { toggleFav(it) },
            onExpand = { setUnifiedExpanded(it) }
        )
    }

    }}
