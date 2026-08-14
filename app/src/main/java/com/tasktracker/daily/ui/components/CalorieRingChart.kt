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
 * Multi-segment donut chart for Nutrition macros intake and total calories.
 */
@Composable
fun CalorieRingChart(
    totalCalories: Int,
    proteinGrams: Float,
    fatGrams: Float,
    carbsGrams: Float,
    sugarGrams: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 180.dp,
    strokeWidth: Dp = 16.dp,
    trackColor: Color = Color(0xFF21262D)
) {
    val extras = LocalGoalieExtraColors.current

    // Convert macro grams to calorie estimation or proportional representation
    // Protein = 4 kcal/g, Fat = 9 kcal/g, Carbs = 4 kcal/g, Sugar is part of carbs (4 kcal/g)
    val proteinKcal = proteinGrams * 4f
    val fatKcal = fatGrams * 9f
    val carbsKcal = carbsGrams * 4f
    val sugarKcal = sugarGrams * 4f

    val totalMacroKcal = (proteinKcal + fatKcal + carbsKcal + sugarKcal).coerceAtLeast(1f)

    val animatedProteinPct by animateFloatAsState(
        targetValue = proteinKcal / totalMacroKcal,
        animationSpec = ProgressRingSpring,
        label = "proteinPct"
    )
    val animatedFatPct by animateFloatAsState(
        targetValue = fatKcal / totalMacroKcal,
        animationSpec = ProgressRingSpring,
        label = "fatPct"
    )
    val animatedCarbsPct by animateFloatAsState(
        targetValue = carbsKcal / totalMacroKcal,
        animationSpec = ProgressRingSpring,
        label = "carbsPct"
    )
    val animatedSugarPct by animateFloatAsState(
        targetValue = sugarKcal / totalMacroKcal,
        animationSpec = ProgressRingSpring,
        label = "sugarPct"
    )

    val gapDegrees = 6f
    val activeMacroCount = listOf(proteinGrams, fatGrams, carbsGrams, sugarGrams).count { it > 0 }
    val totalGapDegrees = gapDegrees * activeMacroCount
    val availableDegrees = (360f - totalGapDegrees).coerceAtLeast(0f)

    val pSweep = availableDegrees * animatedProteinPct
    val fSweep = availableDegrees * animatedFatPct
    val cSweep = availableDegrees * animatedCarbsPct
    val sSweep = availableDegrees * animatedSugarPct

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

            var currentAngle = -90f

            if (totalCalories > 0 && activeMacroCount > 0) {
                // Protein Arc
                if (proteinGrams > 0) {
                    drawArc(
                        color = AccentCoral,
                        startAngle = currentAngle,
                        sweepAngle = pSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    currentAngle += pSweep + gapDegrees
                }

                // Fat Arc
                if (fatGrams > 0) {
                    drawArc(
                        color = AccentAmber,
                        startAngle = currentAngle,
                        sweepAngle = fSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    currentAngle += fSweep + gapDegrees
                }

                // Carbs Arc
                if (carbsGrams > 0) {
                    drawArc(
                        color = AccentSky,
                        startAngle = currentAngle,
                        sweepAngle = cSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    currentAngle += cSweep + gapDegrees
                }

                // Sugar Arc
                if (sugarGrams > 0) {
                    drawArc(
                        color = AccentMint,
                        startAngle = currentAngle,
                        sweepAngle = sSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }
            }
        }

        // Center Content
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
