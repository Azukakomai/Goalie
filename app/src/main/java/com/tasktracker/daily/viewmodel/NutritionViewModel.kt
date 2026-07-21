package com.tasktracker.daily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasktracker.daily.data.GoalOperator
import com.tasktracker.daily.data.MealLog
import com.tasktracker.daily.data.NutritionDao
import com.tasktracker.daily.data.NutritionGoal
import com.tasktracker.daily.data.NutritionMetric
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class NutritionDayStat(
    val date: LocalDate,
    val epochDay: Long,
    val totalKcal: Int,
    val totalProtein: Float,
    val totalFat: Float,
    val totalCarb: Float,
    val totalSugar: Float,
    val goalsAccomplished: Int,
    val totalGoalsCount: Int,
    val accomplishmentRate: Float, // 0.0f to 1.0f
    val level: Int // 0 to 4 (for heatmap green shade)
)

data class DayMacroSummary(
    val date: LocalDate,
    val totalKcal: Int = 0,
    val totalProtein: Float = 0f,
    val totalFat: Float = 0f,
    val totalCarb: Float = 0f,
    val totalSugar: Float = 0f
)

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModel(private val nutritionDao: NutritionDao) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    val goals: StateFlow<List<NutritionGoal>> = nutritionDao.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Seed initial default goals if database is empty
        viewModelScope.launch {
            nutritionDao.getAllGoals().collect { list ->
                if (list.isEmpty()) {
                    nutritionDao.insertGoal(
                        NutritionGoal(
                            metric = NutritionMetric.PROTEIN.name,
                            operator = GoalOperator.GREATER_THAN.name,
                            targetValue = 130f,
                            isEnabled = true
                        )
                    )
                    nutritionDao.insertGoal(
                        NutritionGoal(
                            metric = NutritionMetric.FAT.name,
                            operator = GoalOperator.LESS_THAN.name,
                            targetValue = 15f,
                            isEnabled = true
                        )
                    )
                    nutritionDao.insertGoal(
                        NutritionGoal(
                            metric = NutritionMetric.CARB.name,
                            operator = GoalOperator.LESS_EQUAL.name,
                            targetValue = 200f,
                            isEnabled = true
                        )
                    )
                }
            }
        }
    }

    val mealsForSelectedDate: StateFlow<List<MealLog>> = _selectedDate
        .flatMapLatest { date ->
            nutritionDao.getMealsForDay(date.toEpochDay())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedDayMacroSummary: StateFlow<DayMacroSummary> = mealsForSelectedDate.map { meals ->
        val date = _selectedDate.value
        val totalKcal = meals.sumOf { it.kcal }
        val totalProtein = meals.sumOf { it.proteinGrams.toDouble() }.toFloat()
        val totalFat = meals.sumOf { it.fatGrams.toDouble() }.toFloat()
        val totalCarb = meals.sumOf { it.carbGrams.toDouble() }.toFloat()
        val totalSugar = meals.sumOf { it.sugarGrams.toDouble() }.toFloat()

        DayMacroSummary(
            date = date,
            totalKcal = totalKcal,
            totalProtein = totalProtein,
            totalFat = totalFat,
            totalCarb = totalCarb,
            totalSugar = totalSugar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DayMacroSummary(LocalDate.now())
    )

    val allMeals: StateFlow<List<MealLog>> = nutritionDao.getAllMeals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val start90DaysEpoch = LocalDate.now().minusDays(89).toEpochDay()
    private val endTodayEpoch = LocalDate.now().toEpochDay()

    val all90DaysMeals: StateFlow<List<MealLog>> = nutritionDao.getMealsInRange(start90DaysEpoch, endTodayEpoch)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val heatmap90Days: StateFlow<List<NutritionDayStat>> = combine(all90DaysMeals, goals) { meals, activeGoals ->
        val mealsByDay = meals.groupBy { it.dateEpochDay }
        val today = LocalDate.now()
        val enabledGoals = activeGoals.filter { it.isEnabled }

        (89 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val epochDay = date.toEpochDay()
            val dayMeals = mealsByDay[epochDay] ?: emptyList()

            val kcal = dayMeals.sumOf { it.kcal }
            val protein = dayMeals.sumOf { it.proteinGrams.toDouble() }.toFloat()
            val fat = dayMeals.sumOf { it.fatGrams.toDouble() }.toFloat()
            val carb = dayMeals.sumOf { it.carbGrams.toDouble() }.toFloat()
            val sugar = dayMeals.sumOf { it.sugarGrams.toDouble() }.toFloat()

            val totalGoalsCount = enabledGoals.size
            val accomplishedCount = enabledGoals.count { goal ->
                goal.isAccomplished(
                    totalFat = fat,
                    totalCarb = carb,
                    totalProtein = protein,
                    totalSugar = sugar,
                    totalKcal = kcal
                )
            }

            val rate = if (totalGoalsCount > 0 && dayMeals.isNotEmpty()) {
                accomplishedCount.toFloat() / totalGoalsCount
            } else 0f

            // Level mapping (0 to 4 green shade scale)
            val level = when {
                dayMeals.isEmpty() -> 0
                totalGoalsCount == 0 -> if (kcal > 0) 2 else 0
                accomplishedCount == 0 -> 0
                rate <= 0.25f -> 1
                rate <= 0.50f -> 2
                rate <= 0.75f -> 3
                else -> 4
            }

            NutritionDayStat(
                date = date,
                epochDay = epochDay,
                totalKcal = kcal,
                totalProtein = protein,
                totalFat = fat,
                totalCarb = carb,
                totalSugar = sugar,
                goalsAccomplished = accomplishedCount,
                totalGoalsCount = totalGoalsCount,
                accomplishmentRate = rate,
                level = level
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val heatmap30Days: StateFlow<List<NutritionDayStat>> = heatmap90Days.map { list ->
        if (list.size >= 30) list.takeLast(30) else list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addMeal(
        mealName: String,
        fatGrams: Float,
        carbGrams: Float,
        proteinGrams: Float,
        sugarGrams: Float,
        kcal: Int
    ) {
        if (mealName.isBlank()) return
        viewModelScope.launch {
            val meal = MealLog(
                mealName = mealName.trim(),
                dateEpochDay = _selectedDate.value.toEpochDay(),
                fatGrams = fatGrams.coerceAtLeast(0f),
                carbGrams = carbGrams.coerceAtLeast(0f),
                proteinGrams = proteinGrams.coerceAtLeast(0f),
                sugarGrams = sugarGrams.coerceAtLeast(0f),
                kcal = kcal.coerceAtLeast(0)
            )
            nutritionDao.insertMeal(meal)
        }
    }

    fun deleteMeal(mealLog: MealLog) {
        viewModelScope.launch {
            nutritionDao.deleteMeal(mealLog)
        }
    }

    fun addGoal(metric: NutritionMetric, operator: GoalOperator, targetValue: Float) {
        viewModelScope.launch {
            val goal = NutritionGoal(
                metric = metric.name,
                operator = operator.name,
                targetValue = targetValue.coerceAtLeast(0f),
                isEnabled = true
            )
            nutritionDao.insertGoal(goal)
        }
    }

    fun toggleGoal(goal: NutritionGoal) {
        viewModelScope.launch {
            nutritionDao.updateGoal(goal.copy(isEnabled = !goal.isEnabled))
        }
    }

    fun deleteGoal(goal: NutritionGoal) {
        viewModelScope.launch {
            nutritionDao.deleteGoal(goal)
        }
    }

    fun updateGoalDetails(goal: NutritionGoal, metric: NutritionMetric, operator: GoalOperator, targetValue: Float) {
        viewModelScope.launch {
            nutritionDao.updateGoal(
                goal.copy(
                    metric = metric.name,
                    operator = operator.name,
                    targetValue = targetValue.coerceAtLeast(0f)
                )
            )
        }
    }

    fun importNutrition(newMeals: List<MealLog>, newGoals: List<NutritionGoal>) {
        viewModelScope.launch {
            nutritionDao.deleteAllMeals()
            nutritionDao.deleteAllGoals()
            newMeals.forEach { nutritionDao.insertMeal(it) }
            newGoals.forEach { nutritionDao.insertGoal(it) }
        }
    }

    fun resetAllNutrition() {
        viewModelScope.launch {
            nutritionDao.deleteAllMeals()
            nutritionDao.deleteAllGoals()
        }
    }
}

class NutritionViewModelFactory(private val nutritionDao: NutritionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NutritionViewModel(nutritionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
