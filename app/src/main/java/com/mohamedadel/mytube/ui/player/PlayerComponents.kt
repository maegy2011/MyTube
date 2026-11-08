package com.mohamedadel.mytube.ui.player

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohamedadel.mytube.data.youtube.VideoItem

// Mini player + equalizer bars extracted from MainActivity (split step 5).
// Pure UI: state in via params, actions out via callbacks.

@Composable
fun EqualizerBars(playing: Boolean) {
    // animated sound bars while playing, frozen mid-height while paused
    if (!playing) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(3) {
                Box(modifier = Modifier.width(3.dp).height(8.dp).background(Color.White, RoundedCornerShape(1.dp)))
            }
        }
        return
    }
    val t = rememberInfiniteTransition(label = "eq")
    val h1 by t.animateFloat(4f, 16f, infiniteRepeatable(tween(380), RepeatMode.Reverse), label = "eq1")
    val h2 by t.animateFloat(14f, 5f, infiniteRepeatable(tween(520), RepeatMode.Reverse), label = "eq2")
    val h3 by t.animateFloat(6f, 13f, infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "eq3")
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        listOf(h1, h2, h3).forEach { h ->
            Box(modifier = Modifier.width(3.dp).height(h.dp).background(Color.White, RoundedCornerShape(1.dp)))
        }
    }
}

@Composable
fun MiniPlayer(
    video: VideoItem,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onToggle: () -> Unit,
    onTap: () -> Unit,
    onClose: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onTap).padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(model = video.thumbnail, contentDescription = video.title, modifier = Modifier.size(48.dp, 32.dp).clip(RoundedCornerShape(6.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = video.title ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = video.channel ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                EqualizerBars(playing = isPlaying)
                Spacer(Modifier.width(4.dp))
                IconButton(onClick = onToggle) {
                    Icon(if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Clear, contentDescription = "إغلاق المشغل", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            if (durationMs > 0) {
                LinearProgressIndicator(
                    progress = { (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(2.dp)
                )
            }
        }
    }
}