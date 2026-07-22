package com.tasktracker.daily.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasktracker.daily.data.RecurrenceType
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

    init {
        // Automatically sync recurring tasks for today on init
        syncRecurringTasksForDate(todayEpochDay)
    }

    val todayTasks: StateFlow<List<Task>> = taskDao.getTasksForDay(todayEpochDay)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val start90DaysEpoch = LocalDate.now().minusDays(89).toEpochDay()
    private val endTodayEpoch = LocalDate.now().toEpochDay()

    val allTasks: StateFlow<List<Task>> = taskDao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val all90DaysTasks: StateFlow<List<Task>> = taskDao.getTasksInRange(start90DaysEpoch, endTodayEpoch)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val stats90Days: StateFlow<List<DayStat>> = all90DaysTasks.map { tasks ->
        val tasksByDay = tasks.groupBy { it.dateEpochDay }
        val today = LocalDate.now()

        (89 downTo 0).map { daysAgo ->
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

    val stats30Days: StateFlow<List<DayStat>> = stats90Days.map { list ->
        if (list.size >= 30) list.takeLast(30) else list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val stats7Days: StateFlow<List<DayStat>> = stats90Days.map { list ->
        if (list.size >= 7) list.takeLast(7) else list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addTask(
        title: String,
        recurrenceType: RecurrenceType = RecurrenceType.NONE,
        customIntervalDays: Int = 1
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                dateEpochDay = todayEpochDay,
                recurrenceType = recurrenceType.name,
                customIntervalDays = customIntervalDays.coerceAtLeast(1),
                startDateEpochDay = todayEpochDay
            )
            taskDao.insertTask(task)
            syncRecurringTasksForDate(todayEpochDay)
        }
    }

    fun updateTaskDetails(
        task: Task,
        title: String,
        recurrenceType: RecurrenceType = RecurrenceType.NONE,
        customIntervalDays: Int = 1
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val updated = task.copy(
                title = title.trim(),
                recurrenceType = recurrenceType.name,
                customIntervalDays = customIntervalDays.coerceAtLeast(1)
            )
            taskDao.updateTask(updated)
            syncRecurringTasksForDate(todayEpochDay)
        }
    }

    fun toggleTask(task: Task) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            val targetId = task.parentTaskId ?: task.id
            if (task.recurrenceType != RecurrenceType.NONE.name || task.parentTaskId != null) {
                taskDao.deleteTaskAndInstances(targetId)
            } else {
                taskDao.deleteTask(task)
            }
        }
    }

    fun syncRecurringTasksForDate(targetEpochDay: Long) {
        viewModelScope.launch {
            val targetDate = LocalDate.ofEpochDay(targetEpochDay)
            val templates = taskDao.getRecurringTaskTemplates()

            for (template in templates) {
                if (shouldTaskRunOnDate(template, targetDate)) {
                    if (template.startDateEpochDay == targetEpochDay) {
                        // The template task itself was created today
                        continue
                    }
                    val existing = taskDao.getInstanceForDay(template.id, targetEpochDay)
                    if (existing == null) {
                        taskDao.insertTask(
                            Task(
                                title = template.title,
                                isCompleted = false,
                                dateEpochDay = targetEpochDay,
                                recurrenceType = template.recurrenceType,
                                customIntervalDays = template.customIntervalDays,
                                startDateEpochDay = template.startDateEpochDay,
                                parentTaskId = template.id
                            )
                        )
                    }
                }
            }
        }
    }

    private fun shouldTaskRunOnDate(template: Task, targetDate: LocalDate): Boolean {
        val startDate = LocalDate.ofEpochDay(template.startDateEpochDay)
        if (targetDate.isBefore(startDate)) return false

        val recurrence = RecurrenceType.fromString(template.recurrenceType)
        return when (recurrence) {
            RecurrenceType.NONE -> template.startDateEpochDay == targetDate.toEpochDay()
            RecurrenceType.DAILY -> true
            RecurrenceType.WEEKLY -> (targetDate.toEpochDay() - startDate.toEpochDay()) % 7 == 0L
            RecurrenceType.YEARLY -> targetDate.month == startDate.month && targetDate.dayOfMonth == startDate.dayOfMonth
            RecurrenceType.CUSTOM -> {
                val interval = if (template.customIntervalDays > 0) template.customIntervalDays else 1
                (targetDate.toEpochDay() - startDate.toEpochDay()) % interval == 0L
            }
        }
    }

    fun seedSampleData() {
        viewModelScope.launch {
            val today = LocalDate.now()
            val sampleTasks = mutableListOf<Task>()

            // Seed recurring task templates
            val dailyTask = Task(
                title = "Morning Workout",
                dateEpochDay = today.minusDays(29).toEpochDay(),
                recurrenceType = RecurrenceType.DAILY.name,
                startDateEpochDay = today.minusDays(29).toEpochDay()
            )
            val dailyId = taskDao.insertTask(dailyTask).toInt()

            val weeklyTask = Task(
                title = "Weekly Review & Planning",
                dateEpochDay = today.minusDays(28).toEpochDay(),
                recurrenceType = RecurrenceType.WEEKLY.name,
                startDateEpochDay = today.minusDays(28).toEpochDay()
            )
            val weeklyId = taskDao.insertTask(weeklyTask).toInt()

            val customTask = Task(
                title = "Deep Clean Workspace",
                dateEpochDay = today.minusDays(27).toEpochDay(),
                recurrenceType = RecurrenceType.CUSTOM.name,
                customIntervalDays = 3,
                startDateEpochDay = today.minusDays(27).toEpochDay()
            )
            val customId = taskDao.insertTask(customTask).toInt()

            // Seed sample tasks over the last 30 days
            for (daysAgo in 0..29) {
                val epoch = today.minusDays(daysAgo.toLong()).toEpochDay()
                val targetDate = LocalDate.ofEpochDay(epoch)

                // Add instances for recurring templates
                if (daysAgo > 0) {
                    if (shouldTaskRunOnDate(dailyTask, targetDate)) {
                        sampleTasks.add(
                            Task(
                                title = dailyTask.title,
                                isCompleted = (0..1).random() == 1,
                                dateEpochDay = epoch,
                                recurrenceType = RecurrenceType.DAILY.name,
                                startDateEpochDay = dailyTask.startDateEpochDay,
                                parentTaskId = dailyId
                            )
                        )
                    }
                    if (shouldTaskRunOnDate(weeklyTask, targetDate)) {
                        sampleTasks.add(
                            Task(
                                title = weeklyTask.title,
                                isCompleted = true,
                                dateEpochDay = epoch,
                                recurrenceType = RecurrenceType.WEEKLY.name,
                                startDateEpochDay = weeklyTask.startDateEpochDay,
                                parentTaskId = weeklyId
                            )
                        )
                    }
                    if (shouldTaskRunOnDate(customTask, targetDate)) {
                        sampleTasks.add(
                            Task(
                                title = customTask.title,
                                isCompleted = (0..1).random() == 1,
                                dateEpochDay = epoch,
                                recurrenceType = RecurrenceType.CUSTOM.name,
                                customIntervalDays = 3,
                                startDateEpochDay = customTask.startDateEpochDay,
                                parentTaskId = customId
                            )
                        )
                    }
                }

                // Add random non-recurring tasks
                val count = (1..3).random()
                for (i in 1..count) {
                    sampleTasks.add(
                        Task(
                            title = "Goal $i from ${daysAgo}d ago",
                            isCompleted = (0..1).random() == 1,
                            dateEpochDay = epoch,
                            recurrenceType = RecurrenceType.NONE.name
                        )
                    )
                }
            }
            sampleTasks.forEach { taskDao.insertTask(it) }
            syncRecurringTasksForDate(todayEpochDay)
        }
    }

    fun importTasks(newTasks: List<Task>) {
        viewModelScope.launch {
            taskDao.deleteAllTasks()
            newTasks.forEach { taskDao.insertTask(it) }
            syncRecurringTasksForDate(todayEpochDay)
        }
    }

    fun resetAllTasks() {
        viewModelScope.launch {
            taskDao.deleteAllTasks()
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
