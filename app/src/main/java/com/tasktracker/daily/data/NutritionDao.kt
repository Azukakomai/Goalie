package com.tasktracker.daily.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionDao {
    @Query("SELECT * FROM meal_logs WHERE dateEpochDay = :epochDay ORDER BY id DESC")
    fun getMealsForDay(epochDay: Long): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay ORDER BY id DESC")
    fun getMealsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<MealLog>>

    @Query("SELECT * FROM meal_logs ORDER BY dateEpochDay DESC")
    fun getAllMeals(): Flow<List<MealLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(mealLog: MealLog): Long

    @Update
    suspend fun updateMeal(mealLog: MealLog)

    @Delete
    suspend fun deleteMeal(mealLog: MealLog)

    @Query("SELECT * FROM nutrition_goals ORDER BY id ASC")
    fun getAllGoals(): Flow<List<NutritionGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: NutritionGoal): Long

    @Update
    suspend fun updateGoal(goal: NutritionGoal)

    @Delete
    suspend fun deleteGoal(goal: NutritionGoal)
}
