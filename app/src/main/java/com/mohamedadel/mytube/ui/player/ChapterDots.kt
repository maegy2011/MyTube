package com.mohamedadel.mytube.ui.player

import androidx.media3.ui.PlayerView
import androidx.media3.ui.TimeBar
import com.mohamedadel.mytube.R
import com.mohamedadel.mytube.data.youtube.ChapterItem

// v0.4.6 (chapter dots): repurpose the native TimeBar ad-group markers as
// chapter dots — no custom seek bar, the ExoPlayer controller stays stock.
// Played/unplayed state refreshes from position on every controller update.
fun applyChapterDots(
    view: PlayerView,
    chapters: List<ChapterItem>,
    durationMs: Long,
    positionMs: Long
) {
    try {
        val tb = view.findViewById<android.view.View>(androidx.media3.ui.R.id.exo_progress) as? TimeBar ?: return
        val key = chapters.size.toString() + "@" + durationMs.toString()
        if (view.getTag(R.id.tag_chapter_dots) == key && chapters.size <= 1) return
        if (chapters.size <= 1 || durationMs <= 0L) {
            tb.setAdGroupTimesMs(longArrayOf(), booleanArrayOf(), 0)
            view.setTag(R.id.tag_chapter_dots, "empty")
            return
        }
        val times = chapters.mapNotNull { c ->
            val ms = c.startSec * 1000L
            if (ms in 1L until durationMs) ms else null
        }.toLongArray()
        if (times.isEmpty()) return
        val played = BooleanArray(times.size) { i -> positionMs >= times[i] }
        tb.setAdGroupTimesMs(times, played, times.size)
        view.setTag(R.id.tag_chapter_dots, key)
    } catch (_: Exception) { }
}
