package io.github.amarthyasg.airstix.ui.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun HUDViewfinder(modifier: Modifier = Modifier) {
    val tintColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    Canvas(modifier = modifier.fillMaxSize()) {
        val sizePx = 14.dp.toPx()
        val strokeWidth = 1.dp.toPx()

        // Top-Left L Bracket
        drawLine(
            color = tintColor,
            start = Offset(0f, 0f),
            end = Offset(sizePx, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tintColor,
            start = Offset(0f, 0f),
            end = Offset(0f, sizePx),
            strokeWidth = strokeWidth
        )

        // Top-Right L Bracket
        drawLine(
            color = tintColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width - sizePx, 0f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tintColor,
            start = Offset(size.width, 0f),
            end = Offset(size.width, sizePx),
            strokeWidth = strokeWidth
        )

        // Bottom-Left L Bracket
        drawLine(
            color = tintColor,
            start = Offset(0f, size.height),
            end = Offset(sizePx, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tintColor,
            start = Offset(0f, size.height),
            end = Offset(0f, size.height - sizePx),
            strokeWidth = strokeWidth
        )

        // Bottom-Right L Bracket
        drawLine(
            color = tintColor,
            start = Offset(size.width, size.height),
            end = Offset(size.width - sizePx, size.height),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tintColor,
            start = Offset(size.width, size.height),
            end = Offset(size.width, size.height - sizePx),
            strokeWidth = strokeWidth
        )
    }
}
