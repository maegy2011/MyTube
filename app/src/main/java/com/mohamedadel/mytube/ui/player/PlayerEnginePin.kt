package com.mohamedadel.mytube.ui.player

import androidx.lifecycle.viewModelScope
import com.mohamedadel.mytube.data.local.PinFiles
import com.mohamedadel.mytube.data.youtube.StreamData
import com.mohamedadel.mytube.data.youtube.VideoItem
import com.mohamedadel.mytube.util.QUALITY_AUDIO_ONLY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Phase 2.3 slice 27b (2026-11-13): pinned-playback branch of the playVideo
 * engine. Reads the faithful snapshot off IO, rebuilds the packaged stream,
 * enforces the age gate. Fns <= 30 lines.
 */

internal data class PinStream(val stream: StreamData, val pinLabel: String, val hasSub: Boolean, val age: Int)

private fun cleanPinMime(raw: String?): String? {
    val m = raw?.trim()?.takeIf { it.contains("/") } ?: return null
    return if (m.startsWith("audio/") || m.startsWith("video/")) m else null
}

internal fun PlayerViewModel.buildPinStream(
    v: VideoItem, dir: java.io.File, vFile: java.io.File, aFile: java.io.File?,
    sFile: java.io.File?, meta: org.json.JSONObject?
): PinStream {
    val label = (try { meta?.optString("quality", QUALITY_AUDIO_ONLY) } catch (_: Exception) { null })
        .takeIf { !it.isNullOrBlank() } ?: QUALITY_AUDIO_ONLY
    val hasAudio = aFile?.exists() == true
    val hasSub = sFile?.exists() == true
    val vUri = android.net.Uri.fromFile(vFile).toString()
    val subs = readPinSubs(meta, dir, sFile, vFile, label)
    val s = StreamData(
        audioLowestUrl = if (hasAudio && aFile != null) android.net.Uri.fromFile(aFile).toString() else null,
        videoUrlByQuality = mapOf(label to vUri),
        muxedQualities = if (hasAudio) emptySet() else setOf(label),
        title = v.title,
        subtitles = subs,
        channelUrl = v.channelUrl,
        videoMime = cleanPinMime(try { meta?.optString("videoMime", "") } catch (_: Exception) { null }),
        audioMime = cleanPinMime(try { meta?.optString("audioMime", "") } catch (_: Exception) { null })
    )
    setQualityList(listOf(label))
    setQuality(label)
    setAudioTrackIdx(0)
    setSubTrackIdx(if (hasSub) 0 else -1)
    setSubTrackFor(null)
    val age = try { meta?.optInt("age", 0) ?: 0 } catch (_: Exception) { 0 }
    return PinStream(s, label, hasSub, age)
}

private fun readPinSubs(
    meta: org.json.JSONObject?, dir: java.io.File, sFile: java.io.File?,
    vFile: java.io.File, label: String
): List<com.mohamedadel.mytube.data.youtube.SubtitleItem> {
    return try {
        val arr = meta?.optJSONArray("subs")
        if (arr != null && arr.length() > 0) {
            (0 until arr.length()).mapNotNull { i ->
                val o = try { arr.getJSONObject(i) } catch (_: Exception) { null } ?: return@mapNotNull null
                val fn = o.optString("file", "")
                val f = if (fn.isBlank()) null else java.io.File(dir, fn)
                if (f == null || !f.exists()) return@mapNotNull null
                com.mohamedadel.mytube.data.youtube.SubtitleItem(
                    url = android.net.Uri.fromFile(f).toString(),
                    langTag = o.optString("lang", "und").takeIf { it.isNotBlank() } ?: "und",
                    label = o.optString("label", "").takeIf { it.isNotBlank() } ?: label,
                    auto = false,
                    mime = o.optString("mime", "text/vtt").takeIf { it.isNotBlank() } ?: "text/vtt")
            }
        } else null
    } catch (_: Exception) { null }
        ?: if (sFile?.exists() == true) listOf(
            com.mohamedadel.mytube.data.youtube.SubtitleItem(
                url = android.net.Uri.fromFile(sFile ?: vFile).toString(),
                langTag = (try { meta?.optString("subLang", "und") } catch (_: Exception) { null }).takeIf { !it.isNullOrBlank() } ?: "und",
                label = (try { meta?.optString("subTag", "") } catch (_: Exception) { null }).takeIf { !it.isNullOrBlank() } ?: label,
                auto = false,
                mime = (try { meta?.optString("subMime", "text/vtt") } catch (_: Exception) { null }).takeIf { !it.isNullOrBlank() } ?: "text/vtt")
        ) else emptyList()
}

internal suspend fun PlayerViewModel.pinAgeBlocked(
    pin: PinStream, v: VideoItem, inQueue: Boolean, ageOk: Boolean, prefs: PlayPrefs, host: PlayEngineHost
): Boolean {
    if (pin.age < 18 || ageOk) return false
    if (prefs.ageMode == "block") {
        setPlayerLoading(false)
        host.ageBlocked()
        return true
    }
    setPlayerLoading(false)
    if (inQueue) {
        host.ageBlocked()
        val nxt = queueNow().getOrNull(queueIndexNow() + 1)
        if (nxt != null) host.play(nxt, true, host.expandedNow(), false)
        else { setRadioOn(false); setAutoAdvance(false) }
        return true
    }
    setAgePending(Triple(v, inQueue, null))
    return true
}
