package com.tasktracker.daily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.ui.theme.GlowGreen
import com.tasktracker.daily.ui.theme.GoalieExtraColors
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors
import com.tasktracker.daily.viewmodel.DayStat
import java.time.format.DateTimeFormatter

@Composable
fun HeatmapGrid(
    stats: List<DayStat>,
    modifier: Modifier = Modifier
) {
    val extras = LocalGoalieExtraColors.current
    var selectedStat by remember { mutableStateOf<DayStat?>(null) }

    // Card container — borderless, rounded, shadow
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Header ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "3-Month Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = extras.textAlpha100
                )
                Text(
                    text = "90 Days",
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textAlpha40
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 90 Days Grid (7 columns) ──
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                userScrollEnabled = true
            ) {
                items(stats) { stat ->
                    val squareColor = getHeatmapColor(stat.level, extras)
                    val isSelected = selectedStat?.epochDay == stat.epochDay
                    val hasGlow = stat.level >= 3

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .then(
                                if (hasGlow) {
                                    Modifier.shadow(
                                        4.dp,
                                        RoundedCornerShape(6.dp),
                                        ambientColor = GlowGreen,
                                        spotColor = GlowGreen
                                    )
                                } else Modifier
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .background(squareColor)
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(
                                        0.dp,
                                        RoundedCornerShape(6.dp)
                                    ).background(squareColor)
                                } else Modifier
                            )
                            .clickable { selectedStat = stat }
                    ) {
                        Text(
                            text = stat.date.dayOfMonth.toString(),
                            fontSize = 9.sp,
                            color = if (stat.level >= 3) Color.White else extras.textAlpha40
                        )

                        // Selected outline indicator
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Transparent)
                                    .padding(0.dp)
                                    .then(
                                        Modifier.shadow(0.dp, RoundedCornerShape(6.dp))
                                    )
                            ) {
                                // Canvas-based outline
                                val primaryColor = MaterialTheme.colorScheme.primary
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.matchParentSize()
                                ) {
                                    drawRoundRect(
                                        color = primaryColor,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Selected Day Detail ──
            val activeStat = selectedStat ?: stats.lastOrNull()
            if (activeStat != null) {
                val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
                val pctStr = (activeStat.completionRate * 100).toInt()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = activeStat.date.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = extras.textAlpha80
                    )
                    Text(
                        text = "${activeStat.completedTasks}/${activeStat.totalTasks} Done ($pctStr%)",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = getHeatmapColor(activeStat.level, extras).takeIf { activeStat.level > 0 }
                            ?: extras.textAlpha60
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Legend ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Less",
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textAlpha40
                )
                Spacer(modifier = Modifier.width(6.dp))

                listOf(0, 1, 2, 3, 4).forEach { level ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(getHeatmapColor(level, extras))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }

                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    "More",
                    style = MaterialTheme.typography.labelSmall,
                    color = extras.textAlpha40
                )
            }
        }
    }
}

fun getHeatmapColor(level: Int, extras: GoalieExtraColors): Color {
    return when (level) {
        1 -> extras.heatmapLevel1
        2 -> extras.heatmapLevel2
        3 -> extras.heatmapLevel3
        4 -> extras.heatmapLevel4
        else -> extras.heatmapLevel0
    }
}
