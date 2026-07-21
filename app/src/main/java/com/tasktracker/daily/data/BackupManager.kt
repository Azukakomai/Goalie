package com.tasktracker.daily.data

import org.json.JSONArray
import org.json.JSONObject

data class GoalieBackupData(
    val tasks: List<Task>,
    val meals: List<MealLog>,
    val goals: List<NutritionGoal>
)

object BackupManager {
    fun exportToJson(
        tasks: List<Task>,
        meals: List<MealLog>,
        goals: List<NutritionGoal>
    ): String {
        val root = JSONObject()
        root.put("app", "Goalie")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val tasksArray = JSONArray()
        tasks.forEach { task ->
            val obj = JSONObject()
            obj.put("id", task.id)
            obj.put("title", task.title)
            obj.put("isCompleted", task.isCompleted)
            obj.put("dateEpochDay", task.dateEpochDay)
            obj.put("recurrenceType", task.recurrenceType)
            obj.put("customIntervalDays", task.customIntervalDays)
            obj.put("startDateEpochDay", task.startDateEpochDay)
            if (task.parentTaskId != null) {
                obj.put("parentTaskId", task.parentTaskId)
            }
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        val mealsArray = JSONArray()
        meals.forEach { meal ->
            val obj = JSONObject()
            obj.put("id", meal.id)
            obj.put("mealName", meal.mealName)
            obj.put("dateEpochDay", meal.dateEpochDay)
            obj.put("fatGrams", meal.fatGrams.toDouble())
            obj.put("carbGrams", meal.carbGrams.toDouble())
            obj.put("proteinGrams", meal.proteinGrams.toDouble())
            obj.put("sugarGrams", meal.sugarGrams.toDouble())
            obj.put("kcal", meal.kcal)
            mealsArray.put(obj)
        }
        root.put("meals", mealsArray)

        val goalsArray = JSONArray()
        goals.forEach { goal ->
            val obj = JSONObject()
            obj.put("id", goal.id)
            obj.put("metric", goal.metric)
            obj.put("operator", goal.operator)
            obj.put("targetValue", goal.targetValue.toDouble())
            obj.put("isEnabled", goal.isEnabled)
            goalsArray.put(obj)
        }
        root.put("goals", goalsArray)

        return root.toString(2)
    }

    fun parseFromJson(jsonStr: String): GoalieBackupData {
        val root = JSONObject(jsonStr)
        val parsedTasks = mutableListOf<Task>()
        val parsedMeals = mutableListOf<MealLog>()
        val parsedGoals = mutableListOf<NutritionGoal>()

        if (root.has("tasks")) {
            val tasksArr = root.getJSONArray("tasks")
            for (i in 0 until tasksArr.length()) {
                val obj = tasksArr.getJSONObject(i)
                parsedTasks.add(
                    Task(
                        id = obj.optInt("id", 0),
                        title = obj.getString("title"),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        dateEpochDay = obj.optLong("dateEpochDay", 0L),
                        recurrenceType = obj.optString("recurrenceType", RecurrenceType.NONE.name),
                        customIntervalDays = obj.optInt("customIntervalDays", 1),
                        startDateEpochDay = obj.optLong("startDateEpochDay", 0L),
                        parentTaskId = if (obj.has("parentTaskId") && !obj.isNull("parentTaskId")) obj.getInt("parentTaskId") else null
                    )
                )
            }
        }

        if (root.has("meals")) {
            val mealsArr = root.getJSONArray("meals")
            for (i in 0 until mealsArr.length()) {
                val obj = mealsArr.getJSONObject(i)
                parsedMeals.add(
                    MealLog(
                        id = obj.optInt("id", 0),
                        mealName = obj.getString("mealName"),
                        dateEpochDay = obj.optLong("dateEpochDay", 0L),
                        fatGrams = obj.optDouble("fatGrams", 0.0).toFloat(),
                        carbGrams = obj.optDouble("carbGrams", 0.0).toFloat(),
                        proteinGrams = obj.optDouble("proteinGrams", 0.0).toFloat(),
                        sugarGrams = obj.optDouble("sugarGrams", 0.0).toFloat(),
                        kcal = obj.optInt("kcal", 0)
                    )
                )
            }
        }

        if (root.has("goals")) {
            val goalsArr = root.getJSONArray("goals")
            for (i in 0 until goalsArr.length()) {
                val obj = goalsArr.getJSONObject(i)
                parsedGoals.add(
                    NutritionGoal(
                        id = obj.optInt("id", 0),
                        metric = obj.optString("metric", NutritionMetric.PROTEIN.name),
                        operator = obj.optString("operator", GoalOperator.GREATER_EQUAL.name),
                        targetValue = obj.optDouble("targetValue", 0.0).toFloat(),
                        isEnabled = obj.optBoolean("isEnabled", true)
                    )
                )
            }
        }

        return GoalieBackupData(tasks = parsedTasks, meals = parsedMeals, goals = parsedGoals)
    }
}
