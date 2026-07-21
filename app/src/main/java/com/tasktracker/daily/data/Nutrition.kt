package com.tasktracker.daily.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class NutritionMetric(val displayName: String, val unit: String) {
    PROTEIN("Protein", "g"),
    FAT("Fat", "g"),
    CARB("Carbs", "g"),
    SUGAR("Sugar", "g"),
    KCAL("Calories", "kcal");

    companion object {
        fun fromString(value: String): NutritionMetric {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: KCAL
        }
    }
}

enum class GoalOperator(val symbol: String, val description: String) {
    GREATER_THAN(">", "Greater than"),
    GREATER_EQUAL(">=", "At least"),
    LESS_THAN("<", "Less than"),
    LESS_EQUAL("<=", "At most");

    companion object {
        fun fromString(value: String): GoalOperator {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: GREATER_EQUAL
        }
    }
}

@Entity(tableName = "meal_logs")
data class MealLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mealName: String,
    val dateEpochDay: Long = LocalDate.now().toEpochDay(),
    val fatGrams: Float = 0f,
    val carbGrams: Float = 0f,
    val proteinGrams: Float = 0f,
    val sugarGrams: Float = 0f,
    val kcal: Int = 0
)

@Entity(tableName = "nutrition_goals")
data class NutritionGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val metric: String = NutritionMetric.PROTEIN.name,
    val operator: String = GoalOperator.GREATER_EQUAL.name,
    val targetValue: Float = 0f,
    val isEnabled: Boolean = true
) {
    val nutritionMetric: NutritionMetric
        get() = NutritionMetric.fromString(metric)

    val goalOperator: GoalOperator
        get() = GoalOperator.fromString(operator)

    fun isAccomplished(
        totalFat: Float,
        totalCarb: Float,
        totalProtein: Float,
        totalSugar: Float,
        totalKcal: Int
    ): Boolean {
        if (!isEnabled) return true

        val currentValue = when (nutritionMetric) {
            NutritionMetric.PROTEIN -> totalProtein
            NutritionMetric.FAT -> totalFat
            NutritionMetric.CARB -> totalCarb
            NutritionMetric.SUGAR -> totalSugar
            NutritionMetric.KCAL -> totalKcal.toFloat()
        }

        return when (goalOperator) {
            GoalOperator.GREATER_THAN -> currentValue > targetValue
            GoalOperator.GREATER_EQUAL -> currentValue >= targetValue
            GoalOperator.LESS_THAN -> currentValue < targetValue
            GoalOperator.LESS_EQUAL -> currentValue <= targetValue
        }
    }

    fun getDisplayText(): String {
        val formattedVal = if (targetValue % 1f == 0f) targetValue.toInt().toString() else String.format("%.1f", targetValue)
        return "${nutritionMetric.displayName} ${goalOperator.symbol} $formattedVal${nutritionMetric.unit}"
    }
}
