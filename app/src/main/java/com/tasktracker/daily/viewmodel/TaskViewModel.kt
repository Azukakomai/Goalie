package com.tasktracker.daily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.data.TaskDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DayStat(
    val date: LocalDate,
    val epochDay: Long,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float, // 0.0f to 1.0f
    val level: Int // 0 to 4
)

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    val todayEpochDay: Long = LocalDate.now().toEpochDay()

    val todayTasks: StateFlow<List<Task>> = taskDao.getTasksForDay(todayEpochDay)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val start30DaysEpoch = LocalDate.now().minusDays(29).toEpochDay()
    private val endTodayEpoch = LocalDate.now().toEpochDay()

    val all30DaysTasks: StateFlow<List<Task>> = taskDao.getTasksInRange(start30DaysEpoch, endTodayEpoch)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val stats30Days: StateFlow<List<DayStat>> = all30DaysTasks.map { tasks ->
        val tasksByDay = tasks.groupBy { it.dateEpochDay }
        val today = LocalDate.now()

        (29 downTo 0).map { daysAgo ->
            val date = today.minusDays(daysAgo.toLong())
            val epochDay = date.toEpochDay()
            val dayTasks = tasksByDay[epochDay] ?: emptyList()
            val total = dayTasks.size
            val completed = dayTasks.count { it.isCompleted }
            val rate = if (total > 0) completed.toFloat() / total else 0f

            val level = when {
                total == 0 || completed == 0 -> 0
                rate <= 0.25f -> 1
                rate <= 0.50f -> 2
                rate <= 0.75f -> 3
                else -> 4
            }

            DayStat(
                date = date,
                epochDay = epochDay,
                totalTasks = total,
                completedTasks = completed,
                completionRate = rate,
                level = level
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats7Days: StateFlow<List<DayStat>> = stats30Days.map { list ->
        if (list.size >= 7) list.takeLast(7) else list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskDao.insertTask(Task(title = title.trim(), dateEpochDay = todayEpochDay))
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val sampleTasks = mutableListOf<Task>()
            // Seed sample tasks over the last 30 days for rich demo experience
            for (daysAgo in 0..29) {
                val epoch = today.minusDays(daysAgo.toLong()).toEpochDay()
                val count = (2..5).random()
                val done = (0..count).random()
                for (i in 1..count) {
                    sampleTasks.add(
                        Task(
                            title = "Task $i from ${daysAgo}d ago",
                            isCompleted = i <= done,
                            dateEpochDay = epoch
                        )
                    )
                }
            }
            sampleTasks.forEach { taskDao.insertTask(it) }
        }
    }
}

class TaskViewModelFactory(private val taskDao: TaskDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(taskDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
