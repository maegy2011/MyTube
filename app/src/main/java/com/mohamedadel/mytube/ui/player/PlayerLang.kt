package com.mohamedadel.mytube.ui.player

import androidx.media3.common.C
import com.mohamedadel.mytube.data.local.favKey

/**
 * Phase 2.3 slice 28 (2026-11-13): language/track preference application.
 * The ViewModel owns the decision (prefs + playback states); the Activity
 * owns the ExoPlayer mutation behind LangHost. Fns <= 30 lines.
 */
data class LangPrefs(
    val audioLang: String,
    val subLang: String,
    val subRole: String,
    val hideForced: Boolean,
    val undetermined: Boolean,
    val audioRole: String,
    val saver: Boolean
)

data class LangDecision(
    val audioLang: String?,
    val subOff: Boolean,
    val subLang: String?,
    val subRoleFlag: Int,
    val useCaptioningManager: Boolean,
    val hideForced: Boolean,
    val undetermined: Boolean,
    val audioRoleFlag: Int,
    val saver: Boolean,
    val pinIdx: Int?
)

data class TrackGroupInfo(val token: Any, val length: Int)

interface LangHost : PlayEngineHost {
    fun langPrefs(): LangPrefs
    fun resolveLang(code: String): String?
    fun textGroups(): List<TrackGroupInfo>
    fun applyDecision(d: LangDecision)
}

fun PlayerViewModel.buildLangDecision(host: LangHost): LangDecision {
    val p = host.langPrefs()
    val audioLang = host.resolveLang(p.audioLang)
    val subOff = p.subLang == "off"
    var subLang: String? = null
    var roleFlag = C.ROLE_FLAG_SUBTITLE
    var useCm = false
    if (!subOff) {
        if (p.subRole == "system") useCm = true
        else {
            subLang = host.resolveLang(p.subLang)
            roleFlag = when (p.subRole) {
                "caption" -> C.ROLE_FLAG_CAPTION
                "descriptive" -> C.ROLE_FLAG_DESCRIBES_VIDEO
                else -> C.ROLE_FLAG_SUBTITLE
            }
        }
    }
    val audioRoleFlag = when (p.audioRole) {
        "dubbed" -> C.ROLE_FLAG_DUB
        "original" -> C.ROLE_FLAG_MAIN
        "commentary" -> C.ROLE_FLAG_COMMENTARY
        else -> 0
    }
    val pinIdx = if (subTrackIdxNow() >= 0 && !subOff && subTrackForNow() == playingNow()?.favKey()) subTrackIdxNow() else null
    return LangDecision(audioLang, subOff, subLang, roleFlag, useCm, p.hideForced, p.undetermined, audioRoleFlag, p.saver, pinIdx)
}

fun PlayerViewModel.applyLangPrefs(host: LangHost) {
    try {
        host.applyDecision(buildLangDecision(host))
    } catch (_: Exception) { }
}
