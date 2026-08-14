package com.tasktracker.daily.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tasktracker.daily.ui.theme.AccentAmber
import com.tasktracker.daily.ui.theme.AccentCoral
import com.tasktracker.daily.ui.theme.AccentMint
import com.tasktracker.daily.ui.theme.AccentSky
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors
import java.text.NumberFormat

/**
 * Multi-segment connected donut chart for Nutrition intake matching the mockup's .cal-ring-wrap.
 * Segments connect seamlessly end-to-end and leave the unconsumed portion empty on the background track.
 */
@Composable
fun CalorieRingChart(
    totalCalories: Int,
    proteinGrams: Float,
    fatGrams: Float,
    carbsGrams: Float,
    sugarGrams: Float,
    modifier: Modifier = Modifier,
    calorieGoal: Int = 2000,
    diameter: Dp = 180.dp,
    strokeWidth: Dp = 16.dp,
    trackColor: Color = Color(0xFF21262D)
) {
    val extras = LocalGoalieExtraColors.current

    // Macro kcal values: Protein = 4, Fat = 9, Carbs = 4, Sugar = 4
    val proteinKcal = (proteinGrams * 4f).coerceAtLeast(0f)
    val fatKcal = (fatGrams * 9f).coerceAtLeast(0f)
    val carbsKcal = (carbsGrams * 4f).coerceAtLeast(0f)
    val sugarKcal = (sugarGrams * 4f).coerceAtLeast(0f)

    val totalMacroKcal = (proteinKcal + fatKcal + carbsKcal + sugarKcal).coerceAtLeast(0f)

    // Base total target: use calorie goal, but expand if consumed calories exceed goal
    val maxKcal = maxOf(calorieGoal.toFloat(), totalCalories.toFloat(), totalMacroKcal, 1f)

    // Overall progress angle out of 360 degrees
    val targetAngle = if (totalCalories > 0) {
        (totalCalories.toFloat() / maxKcal).coerceIn(0f, 1f) * 360f
    } else {
        0f
    }

    val animatedTotalAngle by animateFloatAsState(
        targetValue = targetAngle,
        animationSpec = ProgressRingSpring,
        label = "totalCalorieAngle"
    )

    // Proportional sweeps for each connected macro segment
    val pFraction = if (totalMacroKcal > 0) proteinKcal / totalMacroKcal else 0f
    val fFraction = if (totalMacroKcal > 0) fatKcal / totalMacroKcal else 0f
    val cFraction = if (totalMacroKcal > 0) carbsKcal / totalMacroKcal else 0f
    val sFraction = if (totalMacroKcal > 0) sugarKcal / totalMacroKcal else 0f

    val animatedPSweep by animateFloatAsState(
        targetValue = animatedTotalAngle * pFraction,
        animationSpec = ProgressRingSpring,
        label = "pSweep"
    )
    val animatedFSweep by animateFloatAsState(
        targetValue = animatedTotalAngle * fFraction,
        animationSpec = ProgressRingSpring,
        label = "fSweep"
    )
    val animatedCSweep by animateFloatAsState(
        targetValue = animatedTotalAngle * cFraction,
        animationSpec = ProgressRingSpring,
        label = "cSweep"
    )
    val animatedSSweep by animateFloatAsState(
        targetValue = animatedTotalAngle * sFraction,
        animationSpec = ProgressRingSpring,
        label = "sSweep"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(diameter)
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)

            // Background track (shows empty space / kcal left to consume)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            var currentAngle = -90f

            if (totalCalories > 0 && totalMacroKcal > 0) {
                // 1. Protein Arc (Coral)
                if (animatedPSweep > 0f) {
                    drawArc(
                        color = AccentCoral,
                        startAngle = currentAngle,
                        sweepAngle = animatedPSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    currentAngle += animatedPSweep
                }

                // 2. Fat Arc (Amber) - connected directly
                if (animatedFSweep > 0f) {
                    drawArc(
                        color = AccentAmber,
                        startAngle = currentAngle,
                        sweepAngle = animatedFSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    currentAngle += animatedFSweep
                }

                // 3. Carbs Arc (Sky) - connected directly
                if (animatedCSweep > 0f) {
                    drawArc(
                        color = AccentSky,
                        startAngle = currentAngle,
                        sweepAngle = animatedCSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    currentAngle += animatedCSweep
                }

                // 4. Sugar Arc (Mint) - connected directly
                if (animatedSSweep > 0f) {
                    drawArc(
                        color = AccentMint,
                        startAngle = currentAngle,
                        sweepAngle = animatedSSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center Content matching mockup's .cal-center
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Calories",
                style = MaterialTheme.typography.labelSmall,
                color = extras.textAlpha40
            )
            Text(
                text = NumberFormat.getIntegerInstance().format(totalCalories),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "kcal",
                style = MaterialTheme.typography.bodyMedium,
                color = extras.textAlpha60
            )
        }
    }
}
