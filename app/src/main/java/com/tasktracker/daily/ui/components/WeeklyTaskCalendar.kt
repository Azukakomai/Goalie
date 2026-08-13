package com.tasktracker.daily.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors
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
    val extras = LocalGoalieExtraColors.current
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }
    var weekOffset by remember { mutableStateOf(0L) }

    val currentWeekMonday = remember(weekOffset) {
        today.plusWeeks(weekOffset).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    val weekDates = remember(currentWeekMonday) {
        (0..6).map { currentWeekMonday.plusDays(it.toLong()) }
    }

    val tasksByDay = remember(allTasks) {
        allTasks.groupBy { it.dateEpochDay }
    }

    val selectedDayTasks = remember(selectedDate, tasksByDay) {
        tasksByDay[selectedDate.toEpochDay()] ?: emptyList()
    }

    val weekTitle = remember(weekDates) {
        "Week of ${weekDates.first().format(DateTimeFormatter.ofPattern("MMM d"))}"
    }

    // Card container — no border, rounded, shadow
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = weekTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = extras.textAlpha100
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (weekOffset != 0L) {
                        IconButton(
                            onClick = {
                                weekOffset = 0L
                                selectedDate = today
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Today,
                                contentDescription = "Go to Today",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    // Navigation arrows
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { weekOffset -= 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Week",
                            tint = extras.textAlpha60,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { weekOffset += 1 },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Week",
                            tint = extras.textAlpha60,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── 7-Day Strip with Mini Progress Rings ──
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
                    val completionRate = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount else 0f

                    // Bounce animation on selection
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "weekCellScale"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer { scaleX = scale; scaleY = scale }
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    else -> Color.Transparent
                                }
                            )
                            .then(
                                if (isSelected) {
                                    Modifier.shadow(4.dp, RoundedCornerShape(16.dp),
                                        ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                } else Modifier
                            )
                            .clickable { selectedDate = date }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Day abbreviation
                        Text(
                            text = date.format(DateTimeFormatter.ofPattern("EEE")).take(3),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
                            else extras.textAlpha40
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Day number
                        Text(
                            text = "${date.dayOfMonth}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else extras.textAlpha80
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Mini progress ring (20dp)
                        MiniProgressRing(
                            progress = completionRate,
                            isSelected = isSelected,
                            accentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.primary
                        )

                        // Today dot
                        if (isToday) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Stacked Tasks Section ──
            val selectedDateStr = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stacked Tasks ($selectedDateStr)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = extras.textAlpha100
                )
                Text(
                    text = "${selectedDayTasks.count { it.isCompleted }}/${selectedDayTasks.size} done",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedDayTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks scheduled for ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extras.textAlpha40
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

/**
 * Mini progress ring (20dp) for weekly calendar cells.
 * Matches mockup's `.mini-r` SVG ring design.
 */
@Composable
private fun MiniProgressRing(
    progress: Float,
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val trackColor = if (isSelected) {
        Color(0x40000000) // dark semi-transparent on selected
    } else {
        Color(0x0FF0F6FC) // very faint white on normal
    }

    Canvas(modifier = modifier.size(20.dp)) {
        val strokeWidth = 3.dp.toPx()
        val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
        val offset = Offset(strokeWidth / 2f, strokeWidth / 2f)

        // Background track
        drawArc(
            color = trackColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = offset,
            size = arcSize,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        if (progress > 0f) {
            drawArc(
                color = accentColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                topLeft = offset,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
