package com.mohamedadel.mytube.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * DraggableTitle (2026-11-12): single-line title, touch-draggable left/right,
 * keeps its scroll position while held (remember per row, session only),
 * plus a dynamic ▼/▲ button to expand to full text / collapse back.
 * No ellipsis clipping — full text is always reachable.
 */
@Composable
fun DraggableTitle(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val scroll = rememberScrollState()

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(modifier = Modifier.weight(1f)) {
            if (expanded) {
                Text(
                    text = text,
                    style = style,
                    color = color,
                    maxLines = Int.MAX_VALUE,
                    overflow = TextOverflow.Visible
                )
            } else {
                Box {
                    Text(
                        text = text,
                        style = style,
                        color = color,
                        maxLines = 1,
                        overflow = TextOverflow.Visible,
                        softWrap = false,
                        modifier = Modifier.horizontalScroll(scroll)
                    )
                    // fade edge hint: draggable (ج)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    0f to Color.Transparent,
                                    0.85f to Color.Transparent,
                                    1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
                                )
                            )
                    )
                }
            }
        }
        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (expanded) "طي" else "توسيع",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
