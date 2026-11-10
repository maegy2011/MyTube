package com.mohamedadel.mytube.ui.player

import android.graphics.Color as AColor
import android.graphics.Typeface
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView

// Subtitle studio: style model + applicator (verified against media3-ui 1.4.1 via javap).
// CaptionStyleCompat(fg, bg, window, edgeType, edgeColor, typeface); SubtitleView.setStyle +
// setFractionalTextSize + setApplyEmbeddedStyles(false) so OUR style always wins over VTT/TTML.
// fractional sizes are relative to SubtitleView default (fraction of viewport height).
data class SubStyle(
    val fg: Int = AColor.WHITE,
    val bg: Int = AColor.TRANSPARENT,
    val edge: Int = CaptionStyleCompat.EDGE_TYPE_OUTLINE,
    val sizeFrac: Float = 1.0f,
    val bold: Boolean = false
)

fun applySubStyle(view: PlayerView, st: SubStyle) {
    try {
        val sv = view.subtitleView ?: return
        val tf = if (st.bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        sv.setStyle(CaptionStyleCompat(st.fg, st.bg, AColor.TRANSPARENT, st.edge, AColor.BLACK, tf))
        sv.setFractionalTextSize(st.sizeFrac.coerceIn(0.5f, 2.0f) * 0.0533f)
        sv.setApplyEmbeddedStyles(false)
        sv.setApplyEmbeddedFontSizes(false)
    } catch (_: Exception) { }
}

// scale: 0.5..2.0 multiplier (1.0 = 1x); bgOpacity: 0..100 (ignored when bg is transparent).
fun subStyleFromPrefs(fgName: String, bgName: String, edgeName: String, sizeScale: Float, bold: Boolean, bgOpacity: Int = 80): SubStyle {
    val fg = when (fgName) {
        "yellow" -> AColor.YELLOW
        "black" -> AColor.BLACK
        "cyan" -> AColor.CYAN
        else -> AColor.WHITE
    }
    val alpha = (bgOpacity.coerceIn(0, 100) * 255 / 100)
    val bg = when (bgName) {
        "black" -> AColor.argb(alpha, 0, 0, 0)
        "white" -> AColor.argb(alpha, 255, 255, 255)
        else -> AColor.TRANSPARENT // "transparent" + unknown: opacity slider does not apply
    }
    val edge = when (edgeName) {
        "none" -> CaptionStyleCompat.EDGE_TYPE_NONE
        "shadow" -> CaptionStyleCompat.EDGE_TYPE_DROP_SHADOW
        "raised" -> CaptionStyleCompat.EDGE_TYPE_RAISED
        else -> CaptionStyleCompat.EDGE_TYPE_OUTLINE
    }
    return SubStyle(fg, bg, edge, sizeScale.coerceIn(0.5f, 2.0f), bold)
}
