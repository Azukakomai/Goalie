package com.tasktracker.daily

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tasktracker.daily.ui.navigation.AppNavigation
import com.tasktracker.daily.ui.screens.SplashScreen
import com.tasktracker.daily.ui.theme.DailyTrackerTheme
import com.tasktracker.daily.ui.theme.ThemePreferences
import com.tasktracker.daily.viewmodel.NutritionViewModel
import com.tasktracker.daily.viewmodel.NutritionViewModelFactory
import com.tasktracker.daily.viewmodel.TaskViewModel
import com.tasktracker.daily.viewmodel.TaskViewModelFactory

class MainActivity : ComponentActivity() {
    private val viewModel: TaskViewModel by viewModels {
        val app = application as DailyTrackerApp
        TaskViewModelFactory(app.database.taskDao())
    }

    private val nutritionViewModel: NutritionViewModel by viewModels {
        val app = application as DailyTrackerApp
        NutritionViewModelFactory(app.database.nutritionDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePrefs = ThemePreferences(this)

        setContent {
            var currentThemeMode by remember { mutableStateOf(themePrefs.themeMode) }

            DailyTrackerTheme(themeMode = currentThemeMode) {
                var isLoading by remember { mutableStateOf(true) }

                if (isLoading) {
                    SplashScreen(onSplashFinished = { isLoading = false })
                } else {
                    AppNavigation(
                        viewModel = viewModel,
                        nutritionViewModel = nutritionViewModel,
                        currentThemeMode = currentThemeMode,
                        onThemeModeChange = { newMode ->
                            themePrefs.themeMode = newMode
                            currentThemeMode = newMode
                        }
                    )
                }
            }
        }
    }
}

