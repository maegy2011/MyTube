package com.mohamedadel.mytube.data.unified

import com.mohamedadel.mytube.data.youtube.SearchPage
import com.mohamedadel.mytube.data.youtube.YouTubeRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Unified search sources (home-redesign Phase 0, 2026-11-12).
 * Foundation only — no UI reads this yet; screens keep calling
 * doSearch/doScSearch until Phase 1 wires the unified screen.
 */
enum class SearchSource { YOUTUBE, SOUNDCLoud, YTMUSIC }

/** One source's result: items or a short error; never throws. */
data class SourceResult(
    val source: SearchSource,
    val page: SearchPage = SearchPage(),
    val ms: Long = 0L
) {
    val items get() = page.items
    val error get() = page.error
}

/** All three sources side by side; a failed source keeps its error, others still show. */
data class UnifiedResult(
    val youtube: SourceResult,
    val soundcloud: SourceResult,
    val ytmusic: SourceResult
)

private const val PER_SOURCE_TIMEOUT_MS = 8000L

object UnifiedSearchRepo {

    /** YouTube main search — same call doSearch makes (filter "all" + region). */
    suspend fun searchYouTube(query: String, region: String?): SourceResult =
        runSource(SearchSource.YOUTUBE) {
            YouTubeRepo.search(query, null, region, "all")
        }

    /** SoundCloud track search — same call doScSearch makes (src == 0). */
    suspend fun searchSoundCloud(query: String): SourceResult =
        runSource(SearchSource.SOUNDCLoud) {
            YouTubeRepo.scSearch(query)
        }

    /** YouTube Music songs — same call doScSearch makes (src == 1, songs filter). */
    suspend fun searchYTMusic(query: String, region: String?): SourceResult =
        runSource(SearchSource.YTMUSIC) {
            YouTubeRepo.search(query, null, region, "music_songs")
        }

    /**
     * All three in parallel; each source gets 8s then yields its error.
     * One slow/failed source never blocks the others (supervisorScope).
     */
    suspend fun searchAll(query: String, region: String?): UnifiedResult =
        withContext(Dispatchers.IO) {
            supervisorScope {
                val yt = async { searchYouTube(query, region) }
                val sc = async { searchSoundCloud(query) }
                val ym = async { searchYTMusic(query, region) }
                UnifiedResult(yt.await(), sc.await(), ym.await())
            }
        }

    private suspend fun runSource(
        source: SearchSource,
        block: suspend () -> SearchPage
    ): SourceResult {
        val t0 = System.currentTimeMillis()
        val page = try {
            withTimeoutOrNull(PER_SOURCE_TIMEOUT_MS) { block() }
                ?: SearchPage(error = "timeout")
        } catch (e: Exception) {
            SearchPage(error = (e.javaClass.simpleName + ": " + (e.message?.take(120) ?: "")))
        }
        return SourceResult(source, page, System.currentTimeMillis() - t0)
    }
}
