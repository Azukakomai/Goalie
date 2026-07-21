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

    @Query("SELECT * FROM tasks WHERE recurrenceType != 'NONE' AND parentTaskId IS NULL")
    suspend fun getRecurringTaskTemplates(): List<Task>

    @Query("SELECT * FROM tasks WHERE (parentTaskId = :parentId OR id = :parentId) AND dateEpochDay = :epochDay LIMIT 1")
    suspend fun getInstanceForDay(parentId: Int, epochDay: Long): Task?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks WHERE id = :taskId OR parentTaskId = :taskId")
    suspend fun deleteTaskAndInstances(taskId: Int)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
