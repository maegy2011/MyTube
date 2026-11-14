package com.mohamedadel.mytube.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.request.ImageRequest

// v0.4.6 (artwork palette): vibrant color from the video art for mini-player
// accents. Hardware bitmaps disabled (Palette needs pixels). Null = keep theme.
@Composable
fun rememberArtworkVibrant(artUrl: String?, enabled: Boolean): Color? {
    val ctx = LocalContext.current
    val key = if (enabled) artUrl else null
    val out = produceState<Color?>(initialValue = null, key1 = key) {
        value = null
        val u = key
        if (u.isNullOrBlank()) return@produceState
        try {
            val loader = ImageLoader(ctx)
            val req = ImageRequest.Builder(ctx).data(u).allowHardware(false).build()
            val res = loader.execute(req).drawable as? android.graphics.drawable.BitmapDrawable
            val bmp = res?.bitmap
            if (bmp != null) {
                val pal = androidx.palette.graphics.Palette.from(bmp).generate()
                val rgb = pal.getVibrantColor(0)
                value = if (rgb != 0) Color(rgb) else null
            }
            try { loader.shutdown() } catch (_: Exception) { }
        } catch (_: Exception) { value = null }
    }
    return out.value
}
