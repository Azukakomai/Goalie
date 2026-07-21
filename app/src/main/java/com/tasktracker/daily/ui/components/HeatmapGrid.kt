package com.tasktracker.daily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.ui.theme.DarkBorder
import com.tasktracker.daily.ui.theme.DarkSurface
import com.tasktracker.daily.ui.theme.HeatmapLevel0
import com.tasktracker.daily.ui.theme.HeatmapLevel1
import com.tasktracker.daily.ui.theme.HeatmapLevel2
import com.tasktracker.daily.ui.theme.HeatmapLevel3
import com.tasktracker.daily.ui.theme.HeatmapLevel4
import com.tasktracker.daily.ui.theme.TextMuted
import com.tasktracker.daily.ui.theme.TextPrimary
import com.tasktracker.daily.ui.theme.TextSecondary
import com.tasktracker.daily.viewmodel.DayStat
import java.time.format.DateTimeFormatter

@Composable
fun HeatmapGrid(
    stats: List<DayStat>,
    modifier: Modifier = Modifier
) {
    var selectedStat by remember { mutableStateOf<DayStat?>(null) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "3-Month Activity Heatmap",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Text(
                    text = "Scrollable (90 Days)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 90 Days Scrollable Grid (7 columns)
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                userScrollEnabled = true
            ) {
                items(stats) { stat ->
                    val squareColor = getHeatmapColor(stat.level)
                    val isSelected = selectedStat?.epochDay == stat.epochDay

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(squareColor)
                            .border(
                                width = if (isSelected) 2.dp else 0.5.dp,
                                color = if (isSelected) TextPrimary else DarkBorder,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedStat = stat }
                    ) {
                        Text(
                            text = stat.date.dayOfMonth.toString(),
                            fontSize = 10.sp,
                            color = if (stat.level >= 3) DarkSurface else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Day Detail Box
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
                        .background(DarkBorder.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = activeStat.date.format(dateFormatter),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${activeStat.completedTasks}/${activeStat.totalTasks} Done ($pctStr%)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = getHeatmapColor(activeStat.level).takeIf { activeStat.level > 0 } ?: TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // GitHub style Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Less", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Spacer(modifier = Modifier.width(6.dp))

                listOf(0, 1, 2, 3, 4).forEach { level ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(getHeatmapColor(level))
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                }

                Spacer(modifier = Modifier.width(3.dp))
                Text("More", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

fun getHeatmapColor(level: Int): Color {
    return when (level) {
        1 -> HeatmapLevel1
        2 -> HeatmapLevel2
        3 -> HeatmapLevel3
        4 -> HeatmapLevel4
        else -> HeatmapLevel0
    }
}
