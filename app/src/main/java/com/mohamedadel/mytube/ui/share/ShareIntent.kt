package com.mohamedadel.mytube.ui.share

import android.content.Intent
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.util.extractYoutubeId

/**
 * Phase 3 slice 35 (2026-11-14): shared-intent handling extracted from
 * MainActivity. Pure logic; play/search actions via host. Behavior identical.
 */
interface ShareIntentHost {
    fun playSharedVideo(v: VideoItem)
    fun searchSharedText(q: String)
}

fun ShareIntentHost.handleShared(inc: Intent) {
    try {
        val action = inc.action
        val type = inc.type
        var text: String? = null
        if (action == Intent.ACTION_SEND && type == "text/plain")
            text = inc.getStringExtra(Intent.EXTRA_TEXT)
        else if (action == Intent.ACTION_VIEW) text = inc.dataString
        text = text?.trim()
        if (text.isNullOrBlank()) return
        val vid = extractYoutubeId(text)
        if (vid != null) {
            val url = "https://www.youtube.com/watch?v=$vid"
            playSharedVideo(VideoItem(id = vid, url = url, title = text.take(120)))
        } else {
            searchSharedText(text.take(200))
        }
    } catch (_: Exception) { }
}
