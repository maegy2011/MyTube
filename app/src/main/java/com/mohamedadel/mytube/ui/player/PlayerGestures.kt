package com.mohamedadel.mytube.ui.player

import android.app.Activity
import android.media.AudioManager
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrightnessHigh
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * v0.4.5 gesture layer (MX-style, fixed sides even in RTL):
 * right-half vertical = system media volume, left-half vertical = window brightness,
 * horizontal = seek ±10s per 60dp. Single taps pass through to the native controller.
 */
@Composable
fun GestureOverlay(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSeekBy: (Long) -> Unit = {}
) {
    if (!enabled) return
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    val audio = remember(ctx) { ctx.getSystemService(android.content.Context.AUDIO_SERVICE) as? AudioManager }
    var hint by remember { mutableStateOf<String?>(null) }
    var hintFrac by remember { mutableStateOf(0f) }
    var hintKind by remember { mutableStateOf(0) } // 0 vol, 1 bright, 2 seek
    val scope = rememberCoroutineScope()
    fun flash(text: String, frac: Float, kind: Int) {
        hint = text; hintFrac = frac.coerceIn(0f, 1f); hintKind = kind
        scope.launch { delay(900); hint = null }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                var accX = 0f; var accY = 0f; var mode = -1 // 0 vertical, 1 horizontal
                var startRight = true
                detectDragGestures(
                    onDragStart = { off -> accX = 0f; accY = 0f; mode = -1; startRight = off.x > size.width / 2 },
                    onDrag = { change, drag ->
                        change.consume()
                        if (mode == -1) mode = if (kotlin.math.abs(accX + drag.x) > kotlin.math.abs(accY + drag.y)) 1 else 0
                        accX += drag.x; accY += drag.y
                        if (mode == 0) {
                            if (startRight) {
                                // volume: up-drag raises
                                try {
                                    val max = (audio?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15)
                                    val cur = audio?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                                    val step = maxOf(1, max / 15)
                                    val next = (cur + if (accY < 0) step else if (accY > 0) -step else 0).coerceIn(0, max)
                                    if (next != cur) {
                                        audio?.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0)
                                        flash("$next/$max", next.toFloat() / max.toFloat(), 0)
                                        accY = 0f
                                    }
                                } catch (_: Exception) { }
                            } else {
                                try {
                                    val lp = activity?.window?.attributes
                                    var b = lp?.screenBrightness ?: -1f
                                    if (b < 0f) b = 0.5f
                                    b = (b - accY / 1200f).coerceIn(0.05f, 1f)
                                    lp?.let { it.screenBrightness = b; activity?.window?.attributes = it }
                                    flash("${(b * 100).toInt()}%", b, 1)
                                    accY = 0f
                                } catch (_: Exception) { }
                            }
                        } else {
                            if (kotlin.math.abs(accX) > 60f) {
                                val delta = if (accX > 0) 10000L else -10000L
                                onSeekBy(delta)
                                flash(if (delta > 0) "+10s" else "-10s", 0.5f, 2)
                                accX = 0f
                            }
                        }
                    },
                    onDragEnd = { mode = -1 }
                )
            }
    ) {
        hint?.let { t ->
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.Black.copy(alpha = 0.72f)
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        when (hintKind) {
                            1 -> Icons.Rounded.BrightnessHigh
                            else -> Icons.Rounded.VolumeUp
                        },
                        contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(t, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    if (hintKind != 2) {
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { hintFrac },
                            modifier = Modifier.size(width = 120.dp, height = 4.dp),
                            color = Color.White, trackColor = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}
