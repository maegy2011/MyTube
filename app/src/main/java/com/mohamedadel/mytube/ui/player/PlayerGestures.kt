package com.mohamedadel.mytube.ui.player

import android.app.Activity
import android.media.AudioManager
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * v0.4.5 gesture layer (MX-style, fixed sides even in RTL):
 * right-half vertical = system media volume, left-half vertical = window brightness,
 * horizontal = seek ±10s per 60dp.
 *
 * Phase 1 rebuild (touch fix): manual slop detection — a tap (down+up before the
 * touch slop) is NEVER consumed, so it always reaches the native PlayerView
 * controller. Only a real drag is consumed, from the slop moment onward.
 * (detectDragGestures swallowed taps on some devices, freezing the controller.)
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
    val slop = LocalViewConfiguration.current.touchSlop
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
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var acc = Offset.Zero
                    var mode = -1 // 0 vertical, 1 horizontal (decided at slop)
                    var dragging = false
                    val startRight = down.position.x > size.width / 2
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break // finger up: tap, never consumed
                        if (!dragging) {
                            acc += change.positionChange()
                            if (acc.getDistance() >= slop) {
                                dragging = true
                                mode = if (abs(acc.x) > abs(acc.y)) 1 else 0
                                change.consume()
                                acc = Offset.Zero
                            }
                        } else {
                            change.consume()
                            acc += change.positionChange()
                            acc = applyGestureDrag(mode, startRight, acc, audio, activity, onSeekBy, ::flash)
                        }
                    }
                }
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

// One drag step: volume (right), brightness (left), seek ±10s per 60dp (horizontal).
// Returns the leftover accumulator. Never throws; all hardware calls guarded.
private fun applyGestureDrag(
    mode: Int,
    startRight: Boolean,
    acc: Offset,
    audio: AudioManager?,
    activity: Activity?,
    onSeekBy: (Long) -> Unit,
    flash: (String, Float, Int) -> Unit
): Offset {
    try {
        if (mode == 0) {
            if (startRight) {
                val max = (audio?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15)
                val cur = audio?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
                val step = maxOf(1, max / 15)
                val next = (cur + if (acc.y < 0) step else if (acc.y > 0) -step else 0).coerceIn(0, max)
                if (next != cur) {
                    audio?.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0)
                    flash("$next/$max", next.toFloat() / max.toFloat(), 0)
                }
                return Offset.Zero
            } else {
                val lp = activity?.window?.attributes ?: return Offset.Zero
                var b = lp.screenBrightness
                if (b < 0f) b = 0.5f
                b = (b - acc.y / 1200f).coerceIn(0.05f, 1f)
                lp.screenBrightness = b
                activity.window.attributes = lp
                flash("${(b * 100).toInt()}%", b, 1)
                return Offset.Zero
            }
        } else {
            if (abs(acc.x) > 60f) {
                val delta = if (acc.x > 0) 10000L else -10000L
                onSeekBy(delta)
                flash(if (delta > 0) "+10s" else "-10s", 0.5f, 2)
                return Offset.Zero
            }
            return acc
        }
    } catch (_: Exception) { return Offset.Zero }
}
