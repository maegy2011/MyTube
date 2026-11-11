package com.mohamedadel.mytube.ui.results

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

// Card state machine: Skeleton (loading) mirrors SearchRow geometry
// (16:9 thumb + avatar + 2 text lines) so content swaps in without layout jump.
// Hydrated = SearchRow itself; Error/Empty stay as the existing honest text rows.

// Shared pulse: boxes breathe 0.35↔0.7 alpha on the surface-variant color.
@Composable
private fun pulseAlpha(): Float {
    val t = rememberInfiniteTransition(label = "skel")
    val a by t.animateFloat(0.35f, 0.7f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "skelA")
    return a
}

@Composable
fun VideoCardSkeleton() {
    val a = pulseAlpha()
    val wash = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = a)
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(MaterialTheme.colorScheme.surfaceContainerLow)) {
            Box(modifier = Modifier.fillMaxWidth().height(10.dp).align(Alignment.Center).padding(horizontal = 48.dp).clip(RoundedCornerShape(5.dp)).background(wash))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.Top) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(wash))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.9f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(wash))
                Box(modifier = Modifier.fillMaxWidth(0.55f).height(12.dp).clip(RoundedCornerShape(4.dp)).background(wash))
            }
        }
    }
}

// Full-list loading state: N skeletons with the same spacing as real rows.
@Composable
fun VideoListSkeleton(count: Int = 4) {
    // screens-fix Home P2-H9: Column (not LazyColumn) so it nests safely inside scroll hosts.
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        repeat(count) { VideoCardSkeleton() }
    }
}
