package com.tasktracker.daily.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.ui.theme.AccentAmber
import com.tasktracker.daily.ui.theme.AccentCoral
import com.tasktracker.daily.ui.theme.AccentSky
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors
import com.tasktracker.daily.ui.theme.PrimaryEmerald
import com.tasktracker.daily.viewmodel.DayStat
import java.time.format.DateTimeFormatter

@Composable
fun WeeklyChart(
    stats: List<DayStat>,
    modifier: Modifier = Modifier
) {
    val extras = LocalGoalieExtraColors.current
    val totalTasks7d = stats.sumOf { it.totalTasks }
    val completedTasks7d = stats.sumOf { it.completedTasks }
    val avgRatePct = if (totalTasks7d > 0) (completedTasks7d.toFloat() / totalTasks7d * 100).toInt() else 0

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Color-accented Stat Cards ──
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StatCard(
                label = "Total Tasks",
                value = totalTasks7d,
                accentColor = AccentSky,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Completed",
                value = completedTasks7d,
                accentColor = PrimaryEmerald,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "7-Day Rate",
                value = avgRatePct,
                suffix = "%",
                accentColor = AccentAmber,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Vertical Bar Chart ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(24.dp))
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                // Chart header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weekly Performance",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = extras.textAlpha100
                    )
                    // Trend pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "↑ ${avgRatePct}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bars area
                val dayFormatter = DateTimeFormatter.ofPattern("EEE")
                val maxBarHeight = 110.dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxBarHeight + 28.dp), // bars + label space
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    stats.forEach { stat ->
                        val rate = stat.completionRate
                        val barColor = when {
                            rate >= 1f -> PrimaryEmerald
                            rate >= 0.75f -> AccentAmber
                            rate >= 0.5f -> AccentAmber
                            rate > 0f -> AccentCoral
                            else -> extras.textAlpha20
                        }

                        // Staggered animation trigger
                        var animationTriggered by remember { mutableStateOf(false) }
                        LaunchedEffect(stat.date) { animationTriggered = true }
                        val animatedHeight by animateFloatAsState(
                            targetValue = if (animationTriggered) rate.coerceAtLeast(0.02f) else 0f,
                            animationSpec = tween(
                                durationMillis = 800,
                                delayMillis = stats.indexOf(stat) * 70
                            ),
                            label = "barHeight"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Bar
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(maxBarHeight * animatedHeight)
                                    .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 2.dp, bottomEnd = 2.dp))
                                    .then(
                                        if (rate >= 1f) {
                                            Modifier.shadow(4.dp, RoundedCornerShape(6.dp), ambientColor = PrimaryEmerald.copy(alpha = 0.3f))
                                        } else Modifier
                                    )
                                    .background(barColor)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Day label
                            Text(
                                text = stat.date.format(dayFormatter).take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = extras.textAlpha40
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Color-accented stat card matching mockup `.stat` design.
 * Solid surface base to eliminate shadow bleed-through, top 3dp colored border,
 * faint tinted gradient overlay, and animated counter.
 */
@Composable
private fun StatCard(
    label: String,
    value: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    suffix: String = ""
) {
    val extras = LocalGoalieExtraColors.current
    val animatedValue = animatedCounterValue(targetValue = value)

    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.15f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )
    ) {
        // Top accent border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentColor)
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 14.dp, start = 8.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$animatedValue$suffix",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = accentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp
                ),
                color = extras.textAlpha40,
                maxLines = 1
            )
        }
    }
}
