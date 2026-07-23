package com.tasktracker.daily.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.data.RecurrenceType
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.ui.theme.DarkBackground
import com.tasktracker.daily.ui.theme.DarkBorder
import com.tasktracker.daily.ui.theme.DarkSurface
import com.tasktracker.daily.ui.theme.PrimaryEmerald
import com.tasktracker.daily.ui.theme.TextMuted
import com.tasktracker.daily.ui.theme.TextPrimary
import com.tasktracker.daily.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddOrEditTaskDialog(
    taskToEdit: Task? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, recurrenceType: RecurrenceType, customIntervalDays: Int, startDate: LocalDate) -> Unit
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val isEditing = taskToEdit != null

    var taskTitle by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var selectedRecurrence by remember { mutableStateOf(taskToEdit?.let { RecurrenceType.fromString(it.recurrenceType) } ?: RecurrenceType.NONE) }
    var customIntervalText by remember { mutableStateOf(taskToEdit?.customIntervalDays?.toString() ?: "2") }
    var selectedDate by remember {
        mutableStateOf(
            taskToEdit?.let { LocalDate.ofEpochDay(it.startDateEpochDay) } ?: today
        )
    }

    val formattedSelectedDate = remember(selectedDate) {
        val daysDiff = ChronoUnit.DAYS.between(today, selectedDate)
        val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
        when {
            daysDiff == 0L -> "Today ($dateStr)"
            daysDiff == 1L -> "Tomorrow ($dateStr)"
            daysDiff > 1L -> "In $daysDiff days ($dateStr)"
            daysDiff == -1L -> "Yesterday ($dateStr)"
            else -> "$dateStr"
        }
    }

    fun openDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (isEditing) "Edit Task / Goal" else "New Goal / Task",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = if (isEditing) "Modify your task details" else "What do you want to accomplish?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    placeholder = { Text("e.g. Read 20 pages, Morning Run...", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface,
                        focusedBorderColor = PrimaryEmerald,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date Selection Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Start Date",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formattedSelectedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryEmerald,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                val isToday = selectedDate == today
                val isTomorrow = selectedDate == today.plusDays(1)
                val isIn2Days = selectedDate == today.plusDays(2)
                val isCustom = !isToday && !isTomorrow && !isIn2Days

                // Preset Chips Row (3 buttons)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Today chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isToday) PrimaryEmerald else DarkBackground)
                            .border(1.dp, if (isToday) PrimaryEmerald else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedDate = today }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isToday) DarkBackground else TextPrimary,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Tomorrow chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isTomorrow) PrimaryEmerald else DarkBackground)
                            .border(1.dp, if (isTomorrow) PrimaryEmerald else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedDate = today.plusDays(1) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tomorrow",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isTomorrow) DarkBackground else TextPrimary,
                            fontWeight = if (isTomorrow) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // +2 Days chip
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isIn2Days) PrimaryEmerald else DarkBackground)
                            .border(1.dp, if (isIn2Days) PrimaryEmerald else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedDate = today.plusDays(2) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+2 Days",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isIn2Days) DarkBackground else TextPrimary,
                            fontWeight = if (isIn2Days) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Custom Date Picker Bar (Full Width)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCustom) PrimaryEmerald.copy(alpha = 0.15f) else DarkBackground)
                        .border(1.dp, if (isCustom) PrimaryEmerald else DarkBorder, RoundedCornerShape(8.dp))
                        .clickable { openDatePicker() }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pick Custom Date",
                                tint = if (isCustom) PrimaryEmerald else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCustom) "Custom Date: ${selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))}" else "Pick Specific Date...",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCustom) PrimaryEmerald else TextSecondary,
                                fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Text(
                            text = "📅",
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Recurrence",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RecurrenceType.values().forEach { type ->
                        val isSelected = selectedRecurrence == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryEmerald else DarkBackground)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryEmerald else DarkBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedRecurrence = type }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) DarkBackground else TextPrimary
                            )
                        }
                    }
                }

                if (selectedRecurrence == RecurrenceType.CUSTOM) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Repeat every",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = customIntervalText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 3) {
                                    customIntervalText = input
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface,
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "days",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        val interval = customIntervalText.toIntOrNull() ?: 1
                        onConfirm(taskTitle, selectedRecurrence, interval, selectedDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryEmerald,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditing) "Save Changes" else "Add Task", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

// Backwards compatibility alias for AddTaskDialog
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, recurrenceType: RecurrenceType, customIntervalDays: Int, startDate: LocalDate) -> Unit
) {
    AddOrEditTaskDialog(taskToEdit = null, onDismiss = onDismiss, onConfirm = onConfirm)
}
