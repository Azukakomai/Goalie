package com.tasktracker.daily.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.data.GoalOperator
import com.tasktracker.daily.data.MealLog
import com.tasktracker.daily.data.NutritionGoal
import com.tasktracker.daily.data.NutritionMetric
import com.tasktracker.daily.ui.components.getHeatmapColor
import com.tasktracker.daily.ui.theme.DarkBackground
import com.tasktracker.daily.ui.theme.DarkBorder
import com.tasktracker.daily.ui.theme.DarkSurface
import com.tasktracker.daily.ui.theme.DarkSurfaceVariant
import com.tasktracker.daily.ui.theme.PrimaryEmerald
import com.tasktracker.daily.ui.theme.TextMuted
import com.tasktracker.daily.ui.theme.TextPrimary
import com.tasktracker.daily.ui.theme.TextSecondary
import com.tasktracker.daily.viewmodel.NutritionDayStat
import com.tasktracker.daily.viewmodel.NutritionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun NutritionScreen(
    viewModel: NutritionViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val summary by viewModel.selectedDayMacroSummary.collectAsState()
    val meals by viewModel.mealsForSelectedDate.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val heatmapStats by viewModel.heatmap90Days.collectAsState()

    var showAddMealDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showManageGoals by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<NutritionGoal?>(null) }

    val today = LocalDate.now()
    val isToday = selectedDate == today
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Title Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nutritional Tracker",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track meals, calories & daily goals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    IconButton(
                        onClick = { showManageGoals = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Manage Goals",
                            tint = PrimaryEmerald
                        )
                    }
                }
            }

            // Date Navigation Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.selectDate(selectedDate.minusDays(1)) }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = TextPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (isToday) "Today" else selectedDate.format(dateFormatter),
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (isToday) {
                                Text(
                                    text = selectedDate.format(dateFormatter),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!isToday) {
                                IconButton(onClick = { viewModel.selectDate(today) }) {
                                    Icon(Icons.Default.Today, contentDescription = "Go to Today", tint = PrimaryEmerald)
                                }
                            }
                            IconButton(onClick = { viewModel.selectDate(selectedDate.plusDays(1)) }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = TextPrimary)
                            }
                        }
                    }
                }
            }

            // Daily Macro Summary Card
            item {
                MacroSummaryCard(summary = summary, goals = goals)
            }

            // Goal accomplishment Heatmap Card
            item {
                NutritionHeatmapCard(
                    stats = heatmapStats,
                    selectedDate = selectedDate,
                    onDateSelected = { date -> viewModel.selectDate(date) }
                )
            }

            // Daily Goals Accomplishment Status Badges
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daily Goal Conditions",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(onClick = { showAddGoalDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Goal", color = PrimaryEmerald)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (goals.isEmpty()) {
                            Text(
                                text = "No goal conditions set. Tap 'Add Goal' to create one (e.g. Protein > 130g).",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                goals.filter { it.isEnabled }.forEach { goal ->
                                    val isMet = goal.isAccomplished(
                                        totalFat = summary.totalFat,
                                        totalCarb = summary.totalCarb,
                                        totalProtein = summary.totalProtein,
                                        totalSugar = summary.totalSugar,
                                        totalKcal = summary.totalKcal
                                    )
                                    GoalConditionBadge(
                                        goal = goal,
                                        isMet = isMet,
                                        summary = summary,
                                        onEditClick = { goalToEdit = goal }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Logged Meals Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Logged Meals",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${meals.size} ${if (meals.size == 1) "meal" else "meals"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            if (meals.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No meals logged for this day yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button to add a meal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                }
            } else {
                items(meals, key = { it.id }) { meal ->
                    MealItemCard(meal = meal, onDelete = { viewModel.deleteMeal(meal) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to Add Meal
        FloatingActionButton(
            onClick = { showAddMealDialog = true },
            containerColor = PrimaryEmerald,
            contentColor = DarkBackground,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Meal")
        }

        // Add Meal Dialog
        if (showAddMealDialog) {
            AddMealDialog(
                onDismiss = { showAddMealDialog = false },
                onAddMeal = { name, fat, carb, protein, sugar, kcal ->
                    viewModel.addMeal(name, fat, carb, protein, sugar, kcal)
                    showAddMealDialog = false
                }
            )
        }

        // Add / Edit Goal Dialog
        if (showAddGoalDialog || goalToEdit != null) {
            AddOrEditGoalDialog(
                goalToEdit = goalToEdit,
                onDismiss = {
                    showAddGoalDialog = false
                    goalToEdit = null
                },
                onSaveGoal = { metric, operator, targetVal ->
                    if (goalToEdit != null) {
                        viewModel.updateGoalDetails(goalToEdit!!, metric, operator, targetVal)
                    } else {
                        viewModel.addGoal(metric, operator, targetVal)
                    }
                    showAddGoalDialog = false
                    goalToEdit = null
                }
            )
        }

        // Manage Goals Sheet/Dialog
        if (showManageGoals) {
            ManageGoalsDialog(
                goals = goals,
                onDismiss = { showManageGoals = false },
                onToggle = { viewModel.toggleGoal(it) },
                onEdit = { goal ->
                    showManageGoals = false
                    goalToEdit = goal
                },
                onDelete = { viewModel.deleteGoal(it) },
                onAddGoalClick = {
                    showManageGoals = false
                    showAddGoalDialog = true
                }
            )
        }
    }
}

@Composable
fun MacroSummaryCard(
    summary: com.tasktracker.daily.viewmodel.DayMacroSummary,
    goals: List<NutritionGoal>
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Daily Intake Summary",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Main Calories Big Display
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Total Calories",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${summary.totalKcal}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = PrimaryEmerald,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "kcal",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4 Macro Meters Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroPill(
                    label = "Protein",
                    valueStr = "${summary.totalProtein.toInt()}g",
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = "Fat",
                    valueStr = "${summary.totalFat.toInt()}g",
                    color = Color(0xFFFFD93D),
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = "Carbs",
                    valueStr = "${summary.totalCarb.toInt()}g",
                    color = Color(0xFF4D96FF),
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = "Sugar",
                    valueStr = "${summary.totalSugar.toInt()}g",
                    color = Color(0xFF6BCB77),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MacroPill(
    label: String,
    valueStr: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valueStr,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun NutritionHeatmapCard(
    stats: List<NutritionDayStat>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder),
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
                Column {
                    Text(
                        text = "3-Month Goal Accomplishment",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Cell shows kcal • Greener = more goals met",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
                Text(
                    text = "Scrollable (90 Days)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 90-day Scrollable Grid (7 columns)
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
                    val isSelected = stat.date == selectedDate
                    val squareColor = getHeatmapColor(stat.level)

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(squareColor)
                            .border(
                                width = if (isSelected) 2.dp else 0.5.dp,
                                color = if (isSelected) TextPrimary else DarkBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onDateSelected(stat.date) }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${stat.date.dayOfMonth}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (stat.level >= 3) DarkBackground else TextPrimary
                            )
                            if (stat.totalKcal > 0) {
                                Text(
                                    text = "${stat.totalKcal}",
                                    fontSize = 8.sp,
                                    color = if (stat.level >= 3) DarkBackground.copy(alpha = 0.8f) else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Cell Summary Banner
            val activeStat = stats.find { it.date == selectedDate } ?: stats.lastOrNull()
            if (activeStat != null) {
                val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM d")
                val goalsStr = if (activeStat.totalGoalsCount > 0) {
                    "${activeStat.goalsAccomplished}/${activeStat.totalGoalsCount} Goals Met"
                } else {
                    "No goals set"
                }

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
                        text = "${activeStat.date.format(dateFormatter)}: ${activeStat.totalKcal} kcal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = goalsStr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (activeStat.goalsAccomplished > 0) PrimaryEmerald else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Heatmap Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("0 Goals", style = MaterialTheme.typography.labelSmall, color = TextMuted)
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
                Text("All Goals Met", style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
        }
    }
}

@Composable
fun GoalConditionBadge(
    goal: NutritionGoal,
    isMet: Boolean,
    summary: com.tasktracker.daily.viewmodel.DayMacroSummary,
    onEditClick: () -> Unit
) {
    val currentVal = when (goal.nutritionMetric) {
        NutritionMetric.PROTEIN -> summary.totalProtein
        NutritionMetric.FAT -> summary.totalFat
        NutritionMetric.CARB -> summary.totalCarb
        NutritionMetric.SUGAR -> summary.totalSugar
        NutritionMetric.KCAL -> summary.totalKcal.toFloat()
    }

    val formatVal = { v: Float -> if (v % 1f == 0f) v.toInt().toString() else String.format("%.1f", v) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isMet) PrimaryEmerald.copy(alpha = 0.12f) else DarkSurfaceVariant)
            .border(1.dp, if (isMet) PrimaryEmerald.copy(alpha = 0.4f) else DarkBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isMet) PrimaryEmerald else TextMuted.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isMet) DarkBackground else TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = goal.getDisplayText(),
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${formatVal(currentVal)}${goal.nutritionMetric.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isMet) PrimaryEmerald else TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Goal",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun MealItemCard(
    meal: MealLog,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.mealName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroTextBadge("P: ${meal.proteinGrams.toInt()}g", Color(0xFFFF6B6B))
                    MacroTextBadge("F: ${meal.fatGrams.toInt()}g", Color(0xFFFFD93D))
                    MacroTextBadge("C: ${meal.carbGrams.toInt()}g", Color(0xFF4D96FF))
                    if (meal.sugarGrams > 0) {
                        MacroTextBadge("S: ${meal.sugarGrams.toInt()}g", Color(0xFF6BCB77))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${meal.kcal} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryEmerald,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Meal",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MacroTextBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onAddMeal: (name: String, fat: Float, carb: Float, protein: Float, sugar: Float, kcal: Int) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var fatStr by remember { mutableStateOf("") }
    var carbStr by remember { mutableStateOf("") }
    var proteinStr by remember { mutableStateOf("") }
    var sugarStr by remember { mutableStateOf("") }
    var kcalStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Add Meal", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Meal Name (e.g., Chicken & Rice)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = PrimaryEmerald,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = proteinStr,
                        onValueChange = { proteinStr = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fatStr,
                        onValueChange = { fatStr = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = carbStr,
                        onValueChange = { carbStr = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sugarStr,
                        onValueChange = { sugarStr = it },
                        label = { Text("Sugar (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryEmerald,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = kcalStr,
                    onValueChange = { kcalStr = it },
                    label = { Text("Calories (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (mealName.isNotBlank()) {
                        val fat = fatStr.toFloatOrNull() ?: 0f
                        val carb = carbStr.toFloatOrNull() ?: 0f
                        val protein = proteinStr.toFloatOrNull() ?: 0f
                        val sugar = sugarStr.toFloatOrNull() ?: 0f
                        val kcal = kcalStr.toIntOrNull() ?: 0
                        onAddMeal(mealName, fat, carb, protein, sugar, kcal)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = DarkBackground)
            ) {
                Text("Save Meal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AddOrEditGoalDialog(
    goalToEdit: NutritionGoal? = null,
    onDismiss: () -> Unit,
    onSaveGoal: (metric: NutritionMetric, operator: GoalOperator, targetVal: Float) -> Unit
) {
    val isEditing = goalToEdit != null
    var selectedMetric by remember { mutableStateOf(goalToEdit?.nutritionMetric ?: NutritionMetric.PROTEIN) }
    var selectedOperator by remember { mutableStateOf(goalToEdit?.goalOperator ?: GoalOperator.GREATER_THAN) }
    var targetValueStr by remember {
        mutableStateOf(
            goalToEdit?.let {
                if (it.targetValue % 1f == 0f) it.targetValue.toInt().toString() else it.targetValue.toString()
            } ?: "130"
        )
    }

    var metricExpanded by remember { mutableStateOf(false) }
    var operatorExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (isEditing) "Edit Goal Condition" else "Add Goal Condition",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Condition example: Protein > 130g or Fat < 15g",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )

                // 1. Select Metric Chip Buttons
                Text(
                    text = "Nutritional Metric",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(NutritionMetric.PROTEIN, NutritionMetric.FAT, NutritionMetric.CARB).forEach { metric ->
                            val isSelected = metric == selectedMetric
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryEmerald else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) PrimaryEmerald else DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedMetric = metric }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${metric.displayName} (${metric.unit})",
                                    fontSize = 11.sp,
                                    color = if (isSelected) DarkBackground else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(NutritionMetric.SUGAR, NutritionMetric.KCAL).forEach { metric ->
                            val isSelected = metric == selectedMetric
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryEmerald else DarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) PrimaryEmerald else DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedMetric = metric }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${metric.displayName} (${metric.unit})",
                                    fontSize = 11.sp,
                                    color = if (isSelected) DarkBackground else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Select Operator Condition Buttons
                Text(
                    text = "Operator Condition",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GoalOperator.values().forEach { op ->
                        val isSelected = op == selectedOperator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryEmerald.copy(alpha = 0.15f) else DarkSurfaceVariant)
                                .border(1.dp, if (isSelected) PrimaryEmerald else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedOperator = op }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${op.symbol}  ${op.description}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) PrimaryEmerald else TextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = PrimaryEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Target Value Input
                OutlinedTextField(
                    value = targetValueStr,
                    onValueChange = { targetValueStr = it },
                    label = { Text("Target Amount (${selectedMetric.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valFloat = targetValueStr.toFloatOrNull()
                    if (valFloat != null) {
                        onSaveGoal(selectedMetric, selectedOperator, valFloat)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = DarkBackground)
            ) {
                Text(if (isEditing) "Save Changes" else "Add Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun ManageGoalsDialog(
    goals: List<NutritionGoal>,
    onDismiss: () -> Unit,
    onToggle: (NutritionGoal) -> Unit,
    onEdit: (NutritionGoal) -> Unit,
    onDelete: (NutritionGoal) -> Unit,
    onAddGoalClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manage Daily Goals", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onAddGoalClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = PrimaryEmerald)
                }
            }
        },
        text = {
            if (goals.isEmpty()) {
                Text("No goals configured yet.", color = TextMuted)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    goals.forEach { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal.getDisplayText(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (goal.isEnabled) TextPrimary else TextMuted
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { onToggle(goal) }) {
                                    Text(if (goal.isEnabled) "Enabled" else "Disabled", color = if (goal.isEnabled) PrimaryEmerald else TextMuted)
                                }
                                IconButton(onClick = { onEdit(goal) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDelete(goal) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = DarkBackground)
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
