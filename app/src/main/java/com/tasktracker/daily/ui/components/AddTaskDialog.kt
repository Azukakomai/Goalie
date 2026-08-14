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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasktracker.daily.data.RecurrenceType
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddOrEditTaskDialog(
    taskToEdit: Task? = null,
    initialDate: LocalDate = LocalDate.now(),
    onDismiss: () -> Unit,
    onConfirm: (title: String, recurrenceType: RecurrenceType, customIntervalDays: Int, startDate: LocalDate) -> Unit
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now() }
    val isEditing = taskToEdit != null
    val extras = LocalGoalieExtraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var taskTitle by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var selectedRecurrence by remember { mutableStateOf(taskToEdit?.let { RecurrenceType.fromString(it.recurrenceType) } ?: RecurrenceType.NONE) }
    var customIntervalText by remember { mutableStateOf(taskToEdit?.customIntervalDays?.toString() ?: "2") }
    var selectedDate by remember {
        mutableStateOf(
            taskToEdit?.let { LocalDate.ofEpochDay(it.startDateEpochDay) } ?: initialDate
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(extras.textAlpha40.copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Goal / Task" else "New Goal / Task",
                style = MaterialTheme.typography.headlineMedium,
                color = extras.textAlpha100,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isEditing) "Modify your task details" else "What do you want to accomplish?",
                style = MaterialTheme.typography.bodyMedium,
                color = extras.textAlpha60
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Task title input
            OutlinedTextField(
                value = taskTitle,
                onValueChange = { taskTitle = it },
                placeholder = { Text("e.g. Read 20 pages, Morning Run...", color = extras.textAlpha40) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = extras.textAlpha100,
                    unfocusedTextColor = extras.textAlpha100
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Date Selection Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "START DATE",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                    color = extras.textAlpha40,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedSelectedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            val isToday = selectedDate == today
            val isTomorrow = selectedDate == today.plusDays(1)
            val isIn2Days = selectedDate == today.plusDays(2)
            val isCustom = !isToday && !isTomorrow && !isIn2Days

            // Preset Chips Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple("Today", today, isToday),
                    Triple("Tomorrow", today.plusDays(1), isTomorrow),
                    Triple("+2 Days", today.plusDays(2), isIn2Days)
                ).forEach { (label, dateVal, active) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (active) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedDate = dateVal }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (active) MaterialTheme.colorScheme.onPrimary else extras.textAlpha80,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Date Picker Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isCustom) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { openDatePicker() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                            tint = if (isCustom) MaterialTheme.colorScheme.primary else extras.textAlpha40,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isCustom) "Custom: ${selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))}" else "Pick Specific Date...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCustom) MaterialTheme.colorScheme.primary else extras.textAlpha60,
                            fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Text(text = "📅", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "RECURRENCE",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = extras.textAlpha40,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                RecurrenceType.values().forEach { type ->
                    val isSelected = selectedRecurrence == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedRecurrence = type }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = type.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else extras.textAlpha80,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (selectedRecurrence == RecurrenceType.CUSTOM) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Repeat every",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extras.textAlpha60,
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
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = extras.textAlpha100,
                            unfocusedTextColor = extras.textAlpha100
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = extras.textAlpha60,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Full-width Confirm Button
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        val interval = customIntervalText.toIntOrNull() ?: 1
                        onConfirm(taskTitle, selectedRecurrence, interval, selectedDate)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (isEditing) "Save Changes" else "Add Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Backwards compatibility alias for AddTaskDialog
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, recurrenceType: RecurrenceType, customIntervalDays: Int, startDate: LocalDate) -> Unit
) {
    AddOrEditTaskDialog(taskToEdit = null, onDismiss = onDismiss, onConfirm = onConfirm)
}
