package com.tasktracker.daily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasktracker.daily.ui.navigation.AppNavigation
import com.tasktracker.daily.ui.theme.DailyTrackerTheme
import com.tasktracker.daily.viewmodel.TaskViewModel
import com.tasktracker.daily.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels {
        val app = application as DailyTrackerApp
        TaskViewModelFactory(app.database.taskDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyTrackerTheme {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
