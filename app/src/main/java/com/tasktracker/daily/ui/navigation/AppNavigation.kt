package com.tasktracker.daily.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Restaurant
import com.tasktracker.daily.ui.screens.DashboardScreen
import com.tasktracker.daily.ui.screens.NutritionScreen
import com.tasktracker.daily.ui.screens.TasksScreen
import com.tasktracker.daily.ui.theme.DarkBackground
import com.tasktracker.daily.ui.theme.DarkBorder
import com.tasktracker.daily.ui.theme.DarkSurface
import com.tasktracker.daily.ui.theme.PrimaryEmerald
import com.tasktracker.daily.ui.theme.TextMuted
import com.tasktracker.daily.ui.theme.TextPrimary
import com.tasktracker.daily.viewmodel.NutritionViewModel
import com.tasktracker.daily.viewmodel.TaskViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Tasks : Screen("tasks", "Tasks", Icons.Default.Checklist)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.Restaurant)
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.BarChart)
}

@Composable
fun AppNavigation(
    viewModel: TaskViewModel,
    nutritionViewModel: NutritionViewModel
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Tasks, Screen.Nutrition, Screen.Dashboard)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary,
                tonalElevation = androidx.compose.ui.unit.Dp(0f)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = PrimaryEmerald,
                            indicatorColor = PrimaryEmerald,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        ),
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tasks.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Tasks.route) {
                TasksScreen(viewModel = viewModel)
            }
            composable(Screen.Nutrition.route) {
                NutritionScreen(viewModel = nutritionViewModel)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(viewModel = viewModel)
            }
        }
    }
}
