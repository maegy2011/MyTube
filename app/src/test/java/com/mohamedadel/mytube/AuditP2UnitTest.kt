package com.mohamedadel.mytube

import com.mohamedadel.mytube.data.UpdateCheck
import com.mohamedadel.mytube.data.local.favKey
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.playback.nextSequentialIndex
import com.mohamedadel.mytube.playback.prevSequentialIndex
import com.mohamedadel.mytube.playback.shuffledFavKeys
import org.junit.Assert.*
import org.junit.Test

// P2 item 30 (audit 2026-11-11): unit skeleton — favKey stability/dedupe + queue math.
// Run online once (`./gradlew :app:testDebugUnitTest`); needs junit from the network.
class AuditP2UnitTest {

    // F11: one canonical key — id wins, then url, then content triple, then degenerate.
    @Test fun favKey_prefersId() {
        val v = VideoItem(id = "abc", title = "t", url = "https://x")
        assertEquals("abc", v.favKey())
    }

    @Test fun favKey_fallsBackToUrl() {
        val v = VideoItem(title = "t", url = "https://x/v")
        assertEquals("https://x/v", v.favKey())
    }

    @Test fun favKey_localPrefixSurvives() {
        val v = VideoItem(id = "local:42", title = "song", url = "content://media/1")
        assertEquals("local:42", v.favKey())
    }

    // F11: dedupe by favKey (radio seeds must not repeat the queue).
    @Test fun dedupe_byFavKey() {
        val q = listOf(
            VideoItem(id = "a", url = "u1"),
            VideoItem(id = "a", url = "u1"),
            VideoItem(id = "b", url = "u2")
        )
        assertEquals(listOf("a", "b"), q.map { it.favKey() }.distinct())
    }

    // F9/F10: sequential math — next null at end (caller wraps), prev null at head.
    @Test fun queueIndex_nextPrev() {
        assertEquals(1, nextSequentialIndex(3, 0))
        assertNull(nextSequentialIndex(3, 2))
        assertNull(nextSequentialIndex(0, -1))
        assertEquals(0, prevSequentialIndex(1))
        assertNull(prevSequentialIndex(0))
    }

    // F7: shuffle excludes the current track and keeps the full pool otherwise.
    @Test fun shuffle_excludesCurrent() {
        val order = shuffledFavKeys(listOf("a", "b", "c"), "b", seed = 1L)
        assertFalse(order.contains("b"))
        assertEquals(setOf("a", "c"), order.toSet())
    }

    // screens-fix Settings P0-S1: semver update check (was plain string inequality).
    @Test fun update_newerPatch() {
        assertTrue(UpdateCheck.isNewer("v0.3.3", "v0.3.2"))
    }
    @Test fun update_sameTagNotNewer() {
        assertFalse(UpdateCheck.isNewer("v0.3.2", "v0.3.2"))
    }
    @Test fun update_olderTagNotNewer() {
        assertFalse(UpdateCheck.isNewer("v0.3.1", "v0.3.2"))
    }
    @Test fun update_numericNotLexicographic() {
        assertTrue(UpdateCheck.isNewer("v0.10.0", "v0.9.9"))
    }
    @Test fun update_releaseBeatsPrerelease() {
        assertTrue(UpdateCheck.isNewer("1.2.3", "1.2.3-beta"))
        assertFalse(UpdateCheck.isNewer("1.2.3-beta", "1.2.3"))
    }
    @Test fun update_garbageNotNewer() {
        assertFalse(UpdateCheck.isNewer("", "v0.3.2"))
        assertFalse(UpdateCheck.isNewer("abc", "xyz"))
    }
}
