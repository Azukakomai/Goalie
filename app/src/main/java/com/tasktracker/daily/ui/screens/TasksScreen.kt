package com.tasktracker.daily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.ui.components.AddOrEditTaskDialog
import com.tasktracker.daily.ui.components.CircularProgressRing
import com.tasktracker.daily.ui.components.TaskItem
import com.tasktracker.daily.ui.components.staggeredAppear
import com.tasktracker.daily.ui.theme.GlowGreen
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors
import com.tasktracker.daily.viewmodel.TaskViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TasksScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val selectedEpochDay by viewModel.selectedDateEpochDay.collectAsState()
    val tasks by viewModel.selectedDateTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    val extras = LocalGoalieExtraColors.current
    val today = remember { LocalDate.now() }
    val todayEpochDay = remember { today.toEpochDay() }
    val selectedDate = remember(selectedEpochDay) { LocalDate.ofEpochDay(selectedEpochDay) }
    val isToday = selectedEpochDay == todayEpochDay
    val isFuture = selectedEpochDay > todayEpochDay

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val progressPct = (progress * 100).toInt()

    // Date context label
    val dateContextStr = remember(selectedDate, today) {
        val offset = (selectedEpochDay - todayEpochDay).toInt()
        when (offset) {
            0 -> "Today"
            1 -> "Tomorrow"
            -1 -> "Yesterday"
            else -> selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
        }
    }

    // Date label above the ring — uses 3-letter month
    val dateLabel = remember(selectedDate) {
        selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }

    // Time-of-day greeting
    val greeting = remember {
        val hour = LocalTime.now().hour
        when {
            hour < 12 -> "Good morning! ☀\uFE0F"
            hour < 17 -> "Good afternoon! 🌤\uFE0F"
            else -> "Good evening! 🌙"
        }
    }

    // Date scroller data: 3 days before + today + 10 days ahead
    val dateChips = remember(today) {
        (-3..10).map { offset ->
            val date = today.plusDays(offset.toLong())
            DateChipData(
                date = date,
                epochDay = date.toEpochDay(),
                offset = offset,
                dayLabel = date.format(DateTimeFormatter.ofPattern("EEE")).take(3).uppercase(),
                dayNum = date.dayOfMonth
            )
        }
    }

    // Auto-scroll to selected chip
    val scrollState = rememberScrollState()
    LaunchedEffect(selectedEpochDay) {
        val selectedIndex = dateChips.indexOfFirst { it.epochDay == selectedEpochDay }
        if (selectedIndex >= 0) {
            // Each chip is ~60dp wide with 8dp gap, scroll to center it
            val chipWidth = 60
            val gap = 8
            val targetScroll = (selectedIndex * (chipWidth + gap) - 120).coerceAtLeast(0)
            scrollState.animateScrollTo(targetScroll)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Greeting Header ──
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge,
                    color = extras.textAlpha100
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep the momentum going",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extras.textAlpha60
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Horizontal Date Carousel ──
            val bgColor = MaterialTheme.colorScheme.background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Left fade gradient
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    bgColor,
                                    Color.Transparent
                                ),
                                startX = 0f,
                                endX = 32.dp.toPx()
                            ),
                            size = androidx.compose.ui.geometry.Size(32.dp.toPx(), size.height)
                        )
                        // Right fade gradient
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    bgColor
                                ),
                                startX = size.width - 32.dp.toPx(),
                                endX = size.width
                            ),
                            topLeft = Offset(size.width - 32.dp.toPx(), 0f),
                            size = androidx.compose.ui.geometry.Size(32.dp.toPx(), size.height)
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .padding(vertical = 8.dp, horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateChips.forEach { chip ->
                        val isSelected = chip.epochDay == selectedEpochDay
                        val chipIsToday = chip.offset == 0
                        val chipIsFuture = chip.offset > 0

                        Column(
                            modifier = Modifier
                                .width(52.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier.shadow(4.dp, RoundedCornerShape(16.dp))
                                    } else Modifier
                                )
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .clickable { viewModel.selectDate(chip.epochDay) }
                                .padding(vertical = 10.dp, horizontal = 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Day abbreviation (MON, TUE, etc.)
                            Text(
                                text = chip.dayLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                    else -> extras.textAlpha40
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Day number
                            Text(
                                text = "${chip.dayNum}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                    chipIsFuture -> extras.textAlpha40
                                    chipIsToday -> extras.textAlpha100
                                    else -> extras.textAlpha80
                                }
                            )

                            // Today dot
                            if (chipIsToday) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Date Context ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = extras.textAlpha60,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isFuture) {
                        "${totalCount} planned"
                    } else if (totalCount > 0) {
                        "$completedCount of $totalCount done"
                    } else {
                        "No tasks"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Progress Ring Section (Hero Card) ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = MaterialTheme.colorScheme.surface,
                        spotColor = MaterialTheme.colorScheme.surface
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .graphicsLayer { alpha = if (isFuture) 0.7f else 1f }
                    .padding(vertical = 32.dp, horizontal = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressRing(
                        progress = progress,
                        diameter = 140.dp,
                        strokeWidth = 12.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        progressColor = MaterialTheme.colorScheme.primary,
                        centerContent = {
                            Text(
                                text = "$completedCount/$totalCount",
                                style = MaterialTheme.typography.headlineLarge,
                                color = extras.textAlpha100
                            )
                            Text(
                                text = "$progressPct%",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = when {
                            totalCount == 0 && isToday -> "Add your first task to get started"
                            totalCount == 0 -> "No tasks for $dateLabel"
                            isFuture -> "$totalCount tasks planned"
                            else -> "$completedCount of $totalCount tasks completed"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = extras.textAlpha60
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Tasks List ──
            if (tasks.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isToday) "All clear for today!" else "No tasks for this day",
                            style = MaterialTheme.typography.titleLarge,
                            color = extras.textAlpha80
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isToday) "Tap + to set your first goal" else "Tap + to add a task",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extras.textAlpha40
                        )
                    }
                }
            } else {
                // Section label
                val sectionLabel = remember(selectedEpochDay, todayEpochDay) {
                    val offset = (selectedEpochDay - todayEpochDay).toInt()
                    when {
                        offset == 0 -> "TODAY'S TASKS"
                        offset == 1 -> "TOMORROW'S TASKS"
                        offset == -1 -> "YESTERDAY'S TASKS"
                        offset > 0 -> "PLANNED TASKS"
                        else -> "PAST TASKS"
                    }
                }

                Text(
                    text = sectionLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = androidx.compose.ui.unit.TextUnit(1f, androidx.compose.ui.unit.TextUnitType.Sp)
                    ),
                    color = extras.textAlpha40,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(tasks, key = { _, task -> task.id }) { index, task ->
                        Box(modifier = Modifier.staggeredAppear(index)) {
                            TaskItem(
                                task = task,
                                onToggle = { viewModel.toggleTask(task) },
                                onEdit = { taskToEdit = task },
                                onDelete = { viewModel.deleteTask(task) }
                            )
                        }
                    }
                }
            }
        }

        // ── Floating Action Button ──
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(28.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = GlowGreen,
                    spotColor = GlowGreen
                )
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
        }

        if (showAddDialog || taskToEdit != null) {
            AddOrEditTaskDialog(
                taskToEdit = taskToEdit,
                initialDate = selectedDate,
                onDismiss = {
                    showAddDialog = false
                    taskToEdit = null
                },
                onConfirm = { title, recurrenceType, customIntervalDays, startDate ->
                    if (taskToEdit != null) {
                        viewModel.updateTaskDetails(taskToEdit!!, title, recurrenceType, customIntervalDays, startDate)
                    } else {
                        viewModel.addTask(title, recurrenceType, customIntervalDays, startDate)
                    }
                    showAddDialog = false
                    taskToEdit = null
                }
            )
        }
    }
}



/**
 * Data class for date carousel chips.
 */
private data class DateChipData(
    val date: LocalDate,
    val epochDay: Long,
    val offset: Int,
    val dayLabel: String,
    val dayNum: Int
)
