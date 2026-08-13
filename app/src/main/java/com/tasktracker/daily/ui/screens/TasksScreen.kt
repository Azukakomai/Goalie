package com.tasktracker.daily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
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
    val tasks by viewModel.todayTasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    val extras = LocalGoalieExtraColors.current
    val todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val progressPct = (progress * 100).toInt()

    // Time-of-day greeting
    val greeting = remember {
        val hour = LocalTime.now().hour
        when {
            hour < 12 -> "Good morning! ☀️"
            hour < 17 -> "Good afternoon! 🌤️"
            else -> "Good evening! 🌙"
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

            // ── Date Context ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = todayStr,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    ),
                    color = extras.textAlpha60,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (totalCount > 0) "$completedCount of $totalCount done" else "No tasks",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
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
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (totalCount > 0) "$completedCount of $totalCount tasks completed"
                        else "Add your first task to get started",
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
                            text = "All clear for today!",
                            style = MaterialTheme.typography.titleLarge,
                            color = extras.textAlpha80
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to set your first goal",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extras.textAlpha40
                        )
                    }
                }
            } else {
                // Section label
                Text(
                    text = "TODAY'S TASKS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
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
