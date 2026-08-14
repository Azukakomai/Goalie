package com.tasktracker.daily.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A Canvas-based circular progress ring matching the mockup's `.ring-wrap` design.
 *
 * @param progress Value between 0f and 1f
 * @param diameter Overall size of the ring
 * @param strokeWidth Thickness of the arc
 * @param trackColor Background ring color
 * @param progressColor Foreground arc color
 * @param centerContent Content slot for the center (e.g., "3/5" text)
 */
@Composable
fun CircularProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 140.dp,
    strokeWidth: Dp = 12.dp,
    trackColor: Color = Color(0xFF21262D),  // --surface-hover
    progressColor: Color = Color(0xFF39D353), // --accent emerald
    centerContent: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = ProgressRingSpring,
        label = "ringProgress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(diameter)
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Progress arc
            if (animatedProgress > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        // Center content (text, etc.)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            centerContent()
        }
    }
}
