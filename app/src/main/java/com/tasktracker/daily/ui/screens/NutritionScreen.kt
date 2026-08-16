package com.tasktracker.daily.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.data.GoalOperator
import com.tasktracker.daily.data.MealLog
import com.tasktracker.daily.data.NutritionGoal
import com.tasktracker.daily.data.NutritionMetric
import com.tasktracker.daily.ui.components.CalorieRingChart
import com.tasktracker.daily.ui.theme.AccentAmber
import com.tasktracker.daily.ui.theme.AccentCoral
import com.tasktracker.daily.ui.theme.AccentMint
import com.tasktracker.daily.ui.theme.AccentSky
import com.tasktracker.daily.ui.theme.GlowGreen
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors
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

    val extras = LocalGoalieExtraColors.current
    var showAddMealDialog by remember { mutableStateOf(false) }
    var mealToEdit by remember { mutableStateOf<MealLog?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<NutritionGoal?>(null) }
    var showManageGoals by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val isToday = selectedDate == today
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                            text = "Nutrition",
                            style = MaterialTheme.typography.headlineLarge,
                            color = extras.textAlpha100,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Track your daily intake",
                            style = MaterialTheme.typography.bodyMedium,
                            color = extras.textAlpha60
                        )
                    }
                    IconButton(
                        onClick = { showManageGoals = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Manage Goals",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Date Navigation Ticker
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.selectDate(selectedDate.minusDays(1)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Day",
                            tint = extras.textAlpha60,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isToday) "📅 Today, ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
                            else selectedDate.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isToday) {
                            IconButton(onClick = { viewModel.selectDate(today) }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Today, contentDescription = "Go to Today", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.selectDate(selectedDate.plusDays(1)) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next Day",
                                tint = extras.textAlpha60,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Daily Macro Summary Card with Connected Ring & Macro %
            item {
                MacroSummaryCard(
                    summary = summary,
                    goals = goals
                )
            }

            // Goal Progress Section (Mockup-style flat rows without bulky panels)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GOAL PROGRESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            ),
                            color = extras.textAlpha40
                        )
                        TextButton(onClick = { showAddGoalDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Goal", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val enabledGoals = goals.filter { it.isEnabled }
                    if (enabledGoals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No goal conditions set. Tap 'Add Goal' to create one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extras.textAlpha40
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            enabledGoals.forEachIndexed { index, goal ->
                                val isMet = goal.isAccomplished(
                                    totalFat = summary.totalFat,
                                    totalCarb = summary.totalCarb,
                                    totalProtein = summary.totalProtein,
                                    totalSugar = summary.totalSugar,
                                    totalKcal = summary.totalKcal
                                )
                                GoalRowItem(
                                    goal = goal,
                                    isMet = isMet,
                                    summary = summary,
                                    onEditClick = { goalToEdit = goal },
                                    showDivider = index < enabledGoals.size - 1
                                )
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
                        text = "TODAY'S MEALS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = extras.textAlpha40
                    )
                    Text(
                        text = "${meals.size} ${if (meals.size == 1) "meal" else "meals"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = extras.textAlpha40
                    )
                }
            }

            if (meals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = extras.textAlpha40,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No meals logged for this day yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = extras.textAlpha80
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to add a meal",
                                style = MaterialTheme.typography.bodyMedium,
                                color = extras.textAlpha40
                            )
                        }
                    }
                }
            } else {
                items(meals, key = { it.id }) { meal ->
                    MealItemCard(
                        meal = meal,
                        onEdit = { mealToEdit = meal },
                        onDelete = { viewModel.deleteMeal(meal) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }

        // Floating Action Button to Add Meal
        FloatingActionButton(
            onClick = { showAddMealDialog = true },
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
            Icon(Icons.Default.Add, contentDescription = "Add Meal")
        }

        // Add / Edit Meal Dialog
        if (showAddMealDialog || mealToEdit != null) {
            AddOrEditMealDialog(
                mealToEdit = mealToEdit,
                onDismiss = {
                    showAddMealDialog = false
                    mealToEdit = null
                },
                onSaveMeal = { name, fat, carb, protein, sugar, kcal ->
                    if (mealToEdit != null) {
                        viewModel.updateMealDetails(mealToEdit!!, name, fat, carb, protein, sugar, kcal)
                    } else {
                        viewModel.addMeal(name, fat, carb, protein, sugar, kcal)
                    }
                    showAddMealDialog = false
                    mealToEdit = null
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
    // Calorie goal from goals list or default 2000 kcal
    val calorieGoal = goals.find { it.nutritionMetric == NutritionMetric.KCAL && it.isEnabled }?.targetValue?.toInt() ?: 2000

    // Macro targets for % calculation (from goals or standard defaults: Protein: 130g, Fat: 65g, Carbs: 250g, Sugar: 35g)
    val proteinTarget = goals.find { it.nutritionMetric == NutritionMetric.PROTEIN && it.isEnabled }?.targetValue ?: 130f
    val fatTarget = goals.find { it.nutritionMetric == NutritionMetric.FAT && it.isEnabled }?.targetValue ?: 65f
    val carbsTarget = goals.find { it.nutritionMetric == NutritionMetric.CARB && it.isEnabled }?.targetValue ?: 250f
    val sugarTarget = goals.find { it.nutritionMetric == NutritionMetric.SUGAR && it.isEnabled }?.targetValue ?: 35f

    val proteinPct = if (proteinTarget > 0) ((summary.totalProtein / proteinTarget) * 100).toInt() else 0
    val fatPct = if (fatTarget > 0) ((summary.totalFat / fatTarget) * 100).toInt() else 0
    val carbsPct = if (carbsTarget > 0) ((summary.totalCarb / carbsTarget) * 100).toInt() else 0
    val sugarPct = if (sugarTarget > 0) ((summary.totalSugar / sugarTarget) * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 28.dp, horizontal = 24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Connected Calorie Donut Ring Chart with empty track for remaining kcal
            CalorieRingChart(
                totalCalories = summary.totalKcal,
                proteinGrams = summary.totalProtein,
                fatGrams = summary.totalFat,
                carbsGrams = summary.totalCarb,
                sugarGrams = summary.totalSugar,
                calorieGoal = calorieGoal,
                diameter = 180.dp,
                strokeWidth = 16.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4 Macro Pills Row with % completion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroPill(
                    label = "Protein",
                    valueStr = "${summary.totalProtein.toInt()}g",
                    pctStr = "$proteinPct%",
                    color = AccentCoral,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = "Fat",
                    valueStr = "${summary.totalFat.toInt()}g",
                    pctStr = "$fatPct%",
                    color = AccentAmber,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = "Carbs",
                    valueStr = "${summary.totalCarb.toInt()}g",
                    pctStr = "$carbsPct%",
                    color = AccentSky,
                    modifier = Modifier.weight(1f)
                )
                MacroPill(
                    label = "Sugar",
                    valueStr = "${summary.totalSugar.toInt()}g",
                    pctStr = "$sugarPct%",
                    color = AccentMint,
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
    pctStr: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = valueStr,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = LocalGoalieExtraColors.current.textAlpha100
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pctStr,
                fontSize = 11.sp,
                color = LocalGoalieExtraColors.current.textAlpha40
            )
        }
    }
}

/**
 * Clean flat goal row matching the mockup's .goal-row design.
 */
@Composable
fun GoalRowItem(
    goal: NutritionGoal,
    isMet: Boolean,
    summary: com.tasktracker.daily.viewmodel.DayMacroSummary,
    onEditClick: () -> Unit,
    showDivider: Boolean = true
) {
    val extras = LocalGoalieExtraColors.current
    val currentVal = when (goal.nutritionMetric) {
        NutritionMetric.PROTEIN -> summary.totalProtein
        NutritionMetric.FAT -> summary.totalFat
        NutritionMetric.CARB -> summary.totalCarb
        NutritionMetric.SUGAR -> summary.totalSugar
        NutritionMetric.KCAL -> summary.totalKcal.toFloat()
    }

    val targetVal = goal.targetValue
    val pctFraction = if (targetVal > 0) (currentVal / targetVal).coerceIn(0f, 1f) else 0f
    val pctDisplay = if (targetVal > 0) ((currentVal / targetVal) * 100).toInt() else 0
    val barColor = when (goal.nutritionMetric) {
        NutritionMetric.PROTEIN -> AccentCoral
        NutritionMetric.FAT -> AccentAmber
        NutritionMetric.CARB -> AccentSky
        NutritionMetric.SUGAR -> AccentMint
        NutritionMetric.KCAL -> MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() }
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon in 32dp circle matching mockup .goal-icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isMet) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else AccentCoral.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isMet) "✅" else "❌",
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Body: Goal Title and Progress Bar matching mockup .goal-body
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.getDisplayText(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = extras.textAlpha100
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Slim Progress Bar matching mockup .goal-bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(extras.textAlpha20.copy(alpha = 0.12f))
                ) {
                    if (pctFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(pctFraction)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isMet) barColor else AccentCoral)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Goal percentage matching mockup .goal-pct
            Text(
                text = "$pctDisplay%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = if (isMet) barColor else AccentCoral
            )
        }

        if (showDivider) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(extras.textAlpha20.copy(alpha = 0.08f))
            )
        }
    }
}

@Composable
fun MealItemCard(
    meal: MealLog,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val extras = LocalGoalieExtraColors.current

    // Determine left accent color based on highest macro contribution
    val accentColor = remember(meal) {
        val pKcal = meal.proteinGrams * 4f
        val fKcal = meal.fatGrams * 9f
        val cKcal = meal.carbGrams * 4f
        when {
            pKcal >= fKcal && pKcal >= cKcal -> AccentCoral
            cKcal >= pKcal && cKcal >= fKcal -> AccentSky
            else -> AccentAmber
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                // 3dp left accent bar
                drawRoundRect(
                    color = accentColor,
                    topLeft = Offset(0f, 10.dp.toPx()),
                    size = Size(3.dp.toPx(), size.height - 20.dp.toPx()),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }
            .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.mealName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = extras.textAlpha100
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${meal.kcal} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extras.textAlpha60
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Shows only Pro, Carb, Fat matching mockup chips (no sugar)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroChip("Pro ${meal.proteinGrams.toInt()}g", AccentCoral)
                    MacroChip("Carb ${meal.carbGrams.toInt()}g", AccentSky)
                    MacroChip("Fat ${meal.fatGrams.toInt()}g", AccentAmber)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Meal",
                        tint = extras.textAlpha40,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Meal",
                        tint = extras.textAlpha40,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MacroChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditMealDialog(
    mealToEdit: MealLog? = null,
    onDismiss: () -> Unit,
    onSaveMeal: (name: String, fat: Float, carb: Float, protein: Float, sugar: Float, kcal: Int) -> Unit
) {
    val isEditing = mealToEdit != null
    val extras = LocalGoalieExtraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var mealName by remember { mutableStateOf(mealToEdit?.mealName ?: "") }
    var fatStr by remember {
        mutableStateOf(
            mealToEdit?.let {
                if (it.fatGrams % 1f == 0f) it.fatGrams.toInt().toString() else it.fatGrams.toString()
            } ?: ""
        )
    }
    var carbStr by remember {
        mutableStateOf(
            mealToEdit?.let {
                if (it.carbGrams % 1f == 0f) it.carbGrams.toInt().toString() else it.carbGrams.toString()
            } ?: ""
        )
    }
    var proteinStr by remember {
        mutableStateOf(
            mealToEdit?.let {
                if (it.proteinGrams % 1f == 0f) it.proteinGrams.toInt().toString() else it.proteinGrams.toString()
            } ?: ""
        )
    }
    var sugarStr by remember {
        mutableStateOf(
            mealToEdit?.let {
                if (it.sugarGrams % 1f == 0f) it.sugarGrams.toInt().toString() else it.sugarGrams.toString()
            } ?: ""
        )
    }
    var kcalStr by remember { mutableStateOf(mealToEdit?.kcal?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(extras.textAlpha40.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Meal" else "Log New Meal",
                style = MaterialTheme.typography.headlineMedium,
                color = extras.textAlpha100,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter meal details and macro breakdown",
                style = MaterialTheme.typography.bodyMedium,
                color = extras.textAlpha60
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = mealName,
                onValueChange = { mealName = it },
                placeholder = { Text("Meal Name (e.g. Oatmeal & Berries)", color = extras.textAlpha40) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = extras.textAlpha100,
                    unfocusedTextColor = extras.textAlpha100
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = kcalStr,
                onValueChange = { if (it.all { c -> c.isDigit() }) kcalStr = it },
                label = { Text("Total Calories (kcal)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = extras.textAlpha100,
                    unfocusedTextColor = extras.textAlpha100
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Grid of Macro Inputs
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = proteinStr,
                    onValueChange = { proteinStr = it },
                    label = { Text("Protein (g)", color = AccentCoral) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentCoral,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = extras.textAlpha100,
                        unfocusedTextColor = extras.textAlpha100
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = carbStr,
                    onValueChange = { carbStr = it },
                    label = { Text("Carbs (g)", color = AccentSky) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentSky,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = extras.textAlpha100,
                        unfocusedTextColor = extras.textAlpha100
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fatStr,
                    onValueChange = { fatStr = it },
                    label = { Text("Fat (g)", color = AccentAmber) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentAmber,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = extras.textAlpha100,
                        unfocusedTextColor = extras.textAlpha100
                    ),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = sugarStr,
                    onValueChange = { sugarStr = it },
                    label = { Text("Sugar (g)", color = AccentMint) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentMint,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedTextColor = extras.textAlpha100,
                        unfocusedTextColor = extras.textAlpha100
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (mealName.isNotBlank()) {
                        val fat = fatStr.toFloatOrNull() ?: 0f
                        val carb = carbStr.toFloatOrNull() ?: 0f
                        val protein = proteinStr.toFloatOrNull() ?: 0f
                        val sugar = sugarStr.toFloatOrNull() ?: 0f
                        val kcal = kcalStr.toIntOrNull() ?: ((protein * 4) + (carb * 4) + (fat * 9)).toInt()
                        onSaveMeal(mealName, fat, carb, protein, sugar, kcal)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isEditing) "Save Meal Changes" else "Add Meal Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditGoalDialog(
    goalToEdit: NutritionGoal? = null,
    onDismiss: () -> Unit,
    onSaveGoal: (metric: NutritionMetric, operator: GoalOperator, targetVal: Float) -> Unit
) {
    val isEditing = goalToEdit != null
    val extras = LocalGoalieExtraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedMetric by remember { mutableStateOf(goalToEdit?.nutritionMetric ?: NutritionMetric.PROTEIN) }
    var selectedOperator by remember { mutableStateOf(goalToEdit?.goalOperator ?: GoalOperator.GREATER_EQUAL) }
    var targetValueStr by remember {
        mutableStateOf(
            goalToEdit?.let {
                if (it.targetValue % 1f == 0f) it.targetValue.toInt().toString() else it.targetValue.toString()
            } ?: "130"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(extras.textAlpha40.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Nutrition Goal" else "New Nutrition Goal",
                style = MaterialTheme.typography.headlineMedium,
                color = extras.textAlpha100,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Set daily target threshold for a metric",
                style = MaterialTheme.typography.bodyMedium,
                color = extras.textAlpha60
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Metric Selector Chips
            Text("NUTRIENT METRIC", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = extras.textAlpha40, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                NutritionMetric.values().forEach { metric ->
                    val isSel = selectedMetric == metric
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedMetric = metric }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = metric.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else extras.textAlpha80,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Operator Selector Chips
            Text("TARGET CONDITION", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = extras.textAlpha40, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                GoalOperator.values().forEach { op ->
                    val isSel = selectedOperator == op
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedOperator = op }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = op.symbol,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else extras.textAlpha80,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Target Value Input
            OutlinedTextField(
                value = targetValueStr,
                onValueChange = { targetValueStr = it },
                label = { Text("Target Amount (${selectedMetric.unit})") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = extras.textAlpha100,
                    unfocusedTextColor = extras.textAlpha100
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val targetVal = targetValueStr.toFloatOrNull()
                    if (targetVal != null && targetVal > 0) {
                        onSaveGoal(selectedMetric, selectedOperator, targetVal)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isEditing) "Save Goal Changes" else "Add Nutrition Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGoalsDialog(
    goals: List<NutritionGoal>,
    onDismiss: () -> Unit,
    onToggle: (NutritionGoal) -> Unit,
    onEdit: (NutritionGoal) -> Unit,
    onDelete: (NutritionGoal) -> Unit,
    onAddGoalClick: () -> Unit
) {
    val extras = LocalGoalieExtraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(extras.textAlpha40.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Nutrition Goals",
                    style = MaterialTheme.typography.headlineMedium,
                    color = extras.textAlpha100,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onAddGoalClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Goal",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (goals.isEmpty()) {
                Text(
                    text = "No goal conditions set yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extras.textAlpha40,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    goals.forEach { goal ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = goal.getDisplayText(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = extras.textAlpha100
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onEdit(goal) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Goal", tint = extras.textAlpha40, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { onDelete(goal) }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Goal", tint = extras.textAlpha40, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
