package com.tasktracker.daily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tasktracker.daily.ui.components.HeatmapGrid
import com.tasktracker.daily.ui.components.WeeklyChart
import com.tasktracker.daily.ui.theme.DarkBackground
import com.tasktracker.daily.ui.theme.TextPrimary
import com.tasktracker.daily.ui.theme.TextSecondary
import com.tasktracker.daily.viewmodel.TaskViewModel

@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    modifier: Modifier = Modifier
) {
    val stats30Days by viewModel.stats30Days.collectAsState()
    val stats7Days by viewModel.stats7Days.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Analytics Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = "Track your productivity & daily consistency",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 7-Day Analytics Chart
        WeeklyChart(stats = stats7Days)

        Spacer(modifier = Modifier.height(24.dp))

        // 30-Day Contribution Heatmap Grid
        HeatmapGrid(stats = stats30Days)

        Spacer(modifier = Modifier.height(80.dp))
    }
}
