package com.tasktracker.daily.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

// ═══════════════════════════════════════════════════════
// SHARED ANIMATION SPECS
// ═══════════════════════════════════════════════════════

/** Bouncy spring for checkbox pop animation */
val CheckboxSpring: SpringSpec<Float> = spring(
    dampingRatio = 0.5f,
    stiffness = 300f
)

/** Spring for FAB press scale */
val FabPressSpring: SpringSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)

/** Spring for card press scale */
val CardPressSpring: SpringSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = Spring.StiffnessHigh
)

/** Spring for progress ring animation */
val ProgressRingSpring: SpringSpec<Float> = spring(
    dampingRatio = 0.7f,
    stiffness = Spring.StiffnessLow
)

// ═══════════════════════════════════════════════════════
// STAGGERED ANIMATION HELPER
// ═══════════════════════════════════════════════════════

/**
 * Creates a stagger-delayed fade+slide animation modifier.
 * Each item appears [delayPerItemMs] * [index] ms after the first.
 */
@Composable
fun Modifier.staggeredAppear(
    index: Int,
    delayPerItemMs: Long = 40L
): Modifier {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(16f) }

    LaunchedEffect(index) {
        delay(index * delayPerItemMs)
        alpha.animateTo(1f, animationSpec = tween(300))
    }
    LaunchedEffect(index) {
        delay(index * delayPerItemMs)
        offsetY.animateTo(0f, animationSpec = tween(300))
    }

    return this.graphicsLayer {
        this.alpha = alpha.value
        this.translationY = offsetY.value
    }
}

// ═══════════════════════════════════════════════════════
// ANIMATED COUNTER
// ═══════════════════════════════════════════════════════

/**
 * Animates an integer from 0 to [targetValue] over [durationMs].
 * Returns the current animated value.
 */
@Composable
fun animatedCounterValue(
    targetValue: Int,
    durationMs: Long = 800L
): Int {
    var currentValue by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetValue) {
        if (targetValue <= 0) {
            currentValue = 0
            return@LaunchedEffect
        }

        currentValue = 0
        val steps = 30
        val stepDelay = durationMs / steps
        val increment = (targetValue.toFloat() / steps).coerceAtLeast(1f)

        var accumulated = 0f
        for (i in 1..steps) {
            delay(stepDelay)
            accumulated += increment
            currentValue = accumulated.toInt().coerceAtMost(targetValue)
        }
        currentValue = targetValue
    }

    return currentValue
}
