package com.tasktracker.daily.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dateEpochDay = :epochDay ORDER BY id DESC")
    fun getTasksForDay(epochDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dateEpochDay >= :startEpochDay AND dateEpochDay <= :endEpochDay")
    fun getTasksInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks ORDER BY dateEpochDay DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)
}
