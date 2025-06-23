package com.psy.dear.presentation.chat

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Small triangular tail used for chat message bubbles.
 * Flipped horizontally when [isUser] is true so the tail can
 * appear on either the left or right side of the bubble.
 */
@Composable
fun ChatBubbleTail(color: Color, isUser: Boolean) {
    Canvas(
        modifier = Modifier
            .size(width = 8.dp, height = 10.dp)
            .graphicsLayer { if (isUser) scaleX = -1f }
    ) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, size.height / 2)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}

