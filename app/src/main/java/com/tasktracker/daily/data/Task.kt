package com.tasktracker.daily.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class RecurrenceType(val label: String) {
    NONE("One-time"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    YEARLY("Yearly"),
    CUSTOM("Custom");

    companion object {
        fun fromString(type: String): RecurrenceType {
            return values().find { it.name.equals(type, ignoreCase = true) } ?: NONE
        }
    }
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val dateEpochDay: Long = LocalDate.now().toEpochDay(),
    val recurrenceType: String = RecurrenceType.NONE.name,
    val customIntervalDays: Int = 1,
    val startDateEpochDay: Long = dateEpochDay,
    val parentTaskId: Int? = null
)
