package com.tasktracker.daily.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.ui.theme.DarkBackground
import com.tasktracker.daily.ui.theme.DarkBorder
import com.tasktracker.daily.ui.theme.DarkSurface
import com.tasktracker.daily.ui.theme.DarkSurfaceVariant
import com.tasktracker.daily.ui.theme.PrimaryEmerald
import com.tasktracker.daily.ui.theme.TextMuted
import com.tasktracker.daily.ui.theme.TextPrimary
import com.tasktracker.daily.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun WeeklyTaskCalendar(
    allTasks: List<Task>,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var weekOffset by remember { mutableStateOf(0L) } // 0 = current week, -1 = last week, +1 = next week

    // Calculate current week's Monday
    val currentWeekMonday = remember(weekOffset) {
        today.plusWeeks(weekOffset).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    // List of 7 dates for the displayed week (Monday to Sunday)
    val weekDates = remember(currentWeekMonday) {
        (0..6).map { currentWeekMonday.plusDays(it.toLong()) }
    }

    // Group tasks by dateEpochDay
    val tasksByDay = remember(allTasks) {
        allTasks.groupBy { it.dateEpochDay }
    }

    // Selected day tasks
    val selectedDayTasks = remember(selectedDate, tasksByDay) {
        tasksByDay[selectedDate.toEpochDay()] ?: emptyList()
    }

    val weekRangeTitle = remember(weekDates) {
        val start = weekDates.first().format(DateTimeFormatter.ofPattern("MMM d"))
        val end = weekDates.last().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        "$start - $end"
    }

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
            // Header Row: Title & Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weekRangeTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (weekOffset != 0L) {
                        IconButton(
                            onClick = {
                                weekOffset = 0L
                                selectedDate = today
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Go to Today",
                                tint = PrimaryEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { weekOffset -= 1 },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Week",
                            tint = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = { weekOffset += 1 },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Week",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7-Day Horizontal Calendar Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weekDates.forEach { date ->
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val dayTasks = tasksByDay[date.toEpochDay()] ?: emptyList()
                    val totalTasksCount = dayTasks.size
                    val completedTasksCount = dayTasks.count { it.isCompleted }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isSelected -> PrimaryEmerald
                                    isToday -> DarkSurfaceVariant
                                    else -> DarkBackground
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    isSelected -> PrimaryEmerald
                                    isToday -> PrimaryEmerald.copy(alpha = 0.5f)
                                    else -> DarkBorder
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedDate = date }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Day Abbreviation (e.g. MON)
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("EEE")).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) DarkBackground else TextMuted
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Day of Month number (e.g. 22)
                        Text(
                            text = "${date.dayOfMonth}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) DarkBackground else TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Task indicator dot/count badge
                        if (totalTasksCount > 0) {
                            val dotColor = when {
                                isSelected -> DarkBackground
                                completedTasksCount == totalTasksCount -> PrimaryEmerald
                                else -> PrimaryEmerald.copy(alpha = 0.7f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stacked Tasks Section Header for Selected Day
            val selectedDateStr = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stacked Tasks ($selectedDateStr)",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${selectedDayTasks.count { it.isCompleted }}/${selectedDayTasks.size} done",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryEmerald
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Stacked Task Cards for Selected Day
            if (selectedDayTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks scheduled for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    selectedDayTasks.forEach { task ->
                        TaskItem(
                            task = task,
                            onToggle = { onToggleTask(task) },
                            onEdit = {},
                            onDelete = { onDeleteTask(task) }
                        )
                    }
                }
            }
        }
    }
}
