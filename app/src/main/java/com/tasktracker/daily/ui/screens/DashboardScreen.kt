package com.tasktracker.daily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasktracker.daily.ui.components.HeatmapGrid
import com.tasktracker.daily.ui.components.SettingsDialog
import com.tasktracker.daily.ui.components.WeeklyChart
import com.tasktracker.daily.ui.components.WeeklyTaskCalendar
import com.tasktracker.daily.viewmodel.NutritionViewModel
import com.tasktracker.daily.viewmodel.TaskViewModel

@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    nutritionViewModel: NutritionViewModel,
    modifier: Modifier = Modifier
) {
    val stats90Days by viewModel.stats90Days.collectAsState()
    val stats7Days by viewModel.stats7Days.collectAsState()

    val tasks by viewModel.allTasks.collectAsState()
    val meals by nutritionViewModel.allMeals.collectAsState()
    val goals by nutritionViewModel.goals.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Top Header Row with Title and Settings Gear Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Analytics Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track your productivity & daily consistency",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 7-Day Analytics Chart
        WeeklyChart(stats = stats7Days)

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Schedule Calendar & Stacked Day Tasks
        WeeklyTaskCalendar(
            allTasks = tasks,
            onToggleTask = { viewModel.toggleTask(it) },
            onDeleteTask = { viewModel.deleteTask(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3-Month Scrollable Contribution Heatmap Grid
        HeatmapGrid(stats = stats90Days)

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Settings Modal Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false },
            tasks = tasks,
            meals = meals,
            goals = goals,
            onImportData = { importedTasks, importedMeals, importedGoals ->
                viewModel.importTasks(importedTasks)
                nutritionViewModel.importNutrition(importedMeals, importedGoals)
            },
            onResetData = {
                viewModel.resetAllTasks()
                nutritionViewModel.resetAllNutrition()
            }
        )
    }
}
