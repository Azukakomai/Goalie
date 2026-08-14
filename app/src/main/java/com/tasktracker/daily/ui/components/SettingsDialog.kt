package com.tasktracker.daily.ui.components

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.tasktracker.daily.data.BackupManager
import com.tasktracker.daily.data.MealLog
import com.tasktracker.daily.data.NutritionGoal
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.notifications.NotificationHelper
import com.tasktracker.daily.notifications.NotificationPreferences
import com.tasktracker.daily.notifications.NotificationScheduler
import com.tasktracker.daily.ui.theme.AccentCoral
import com.tasktracker.daily.ui.theme.AccentSky
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    tasks: List<Task>,
    meals: List<MealLog>,
    goals: List<NutritionGoal>,
    onImportData: (List<Task>, List<MealLog>, List<NutritionGoal>) -> Unit,
    onResetData: () -> Unit
) {
    val context = LocalContext.current
    val extras = LocalGoalieExtraColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    // Notification Preferences State
    val prefs = remember { NotificationPreferences(context) }
    var isNotificationsEnabled by remember { mutableStateOf(prefs.isNotificationsEnabled) }
    var reminderHour by remember { mutableStateOf(prefs.reminderHour) }
    var reminderMinute by remember { mutableStateOf(prefs.reminderMinute) }

    // Launcher for Android 13+ Notification Permission
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isNotificationsEnabled = true
            prefs.isNotificationsEnabled = true
            NotificationScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
            Toast.makeText(
                context,
                String.format("Daily reminder set for %02d:%02d", reminderHour, reminderMinute),
                Toast.LENGTH_LONG
            ).show()
        } else {
            isNotificationsEnabled = false
            prefs.isNotificationsEnabled = false
            Toast.makeText(context, "Notification permission is required for daily reminders.", Toast.LENGTH_LONG).show()
        }
    }

    fun requestPermissionAndEnableNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                isNotificationsEnabled = true
                prefs.isNotificationsEnabled = true
                NotificationScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
                Toast.makeText(
                    context,
                    String.format("Daily reminder set for %02d:%02d", reminderHour, reminderMinute),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            isNotificationsEnabled = true
            prefs.isNotificationsEnabled = true
            NotificationScheduler.scheduleDailyReminder(context, reminderHour, reminderMinute)
            Toast.makeText(
                context,
                String.format("Daily reminder set for %02d:%02d", reminderHour, reminderMinute),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Launcher for exporting JSON file
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val jsonStr = BackupManager.exportToJson(tasks, meals, goals)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(jsonStr.toByteArray())
                }
                Toast.makeText(context, "Data exported successfully!", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Launcher for importing JSON file
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val jsonStr = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (jsonStr.isNotBlank()) {
                    val backupData = BackupManager.parseFromJson(jsonStr)
                    onImportData(backupData.tasks, backupData.meals, backupData.goals)
                    Toast.makeText(
                        context,
                        "Imported ${backupData.tasks.size} tasks, ${backupData.meals.size} meals & ${backupData.goals.size} goals!",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Import failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Goalie Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    color = extras.textAlpha100,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Manage notifications, data backups, or restore previous entries.",
                style = MaterialTheme.typography.bodyMedium,
                color = extras.textAlpha60
            )

            // --- Section: Reminders ---
            Text(
                text = "NOTIFICATIONS",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = extras.textAlpha40,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Daily Goal Reminders",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium,
                                color = extras.textAlpha100
                            )
                            Text(
                                text = "\"Have you checked your goals today?\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = extras.textAlpha60
                            )
                        }
                    }
                    Switch(
                        checked = isNotificationsEnabled,
                        onCheckedChange = { checked ->
                            if (checked) {
                                requestPermissionAndEnableNotifications()
                            } else {
                                isNotificationsEnabled = false
                                prefs.isNotificationsEnabled = false
                                NotificationScheduler.cancelDailyReminder(context)
                                Toast.makeText(context, "Daily reminders disabled", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                if (isNotificationsEnabled) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = extras.textAlpha20.copy(alpha = 0.1f)
                    )

                    OutlinedButton(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    reminderHour = hourOfDay
                                    reminderMinute = minute
                                    prefs.reminderHour = hourOfDay
                                    prefs.reminderMinute = minute
                                    NotificationScheduler.scheduleDailyReminder(context, hourOfDay, minute)
                                    Toast.makeText(
                                        context,
                                        String.format("Reminder updated to %02d:%02d", hourOfDay, minute),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                reminderHour,
                                reminderMinute,
                                false
                            ).show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            val amPm = if (reminderHour >= 12) "PM" else "AM"
                            val displayHour = if (reminderHour % 12 == 0) 12 else reminderHour % 12
                            Text(
                                text = String.format("Reminder Time: %d:%02d %s", displayHour, reminderMinute, amPm),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                            ) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                NotificationHelper.sendGoalReminderNotification(
                                    context = context,
                                    title = "Goalie 🎯",
                                    message = "Have you checked your goals today?"
                                )
                                Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Test Notification", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // --- Section: Backup & Restore ---
            Text(
                text = "DATA & BACKUP",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = extras.textAlpha40,
                fontWeight = FontWeight.Bold
            )

            // Export Data Button
            Button(
                onClick = { exportLauncher.launch("goalie_backup.json") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = extras.textAlpha100
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Export Data", fontWeight = FontWeight.SemiBold, color = extras.textAlpha100)
                        Text("Save tasks, meals & goals to JSON file", style = MaterialTheme.typography.labelSmall, color = extras.textAlpha60)
                    }
                }
            }

            // Import Data Button
            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = extras.textAlpha100
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = AccentSky)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Import Data", fontWeight = FontWeight.SemiBold, color = extras.textAlpha100)
                        Text("Restore data from a JSON backup file", style = MaterialTheme.typography.labelSmall, color = extras.textAlpha60)
                    }
                }
            }

            // Reset Current Data Danger Button
            OutlinedButton(
                onClick = { showResetConfirmDialog = true },
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentCoral),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCoral),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = AccentCoral)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Reset Current Data", fontWeight = FontWeight.Bold, color = AccentCoral)
                        Text("Wipe all tasks, meals & goals", style = MaterialTheme.typography.labelSmall, color = AccentCoral.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }

    // Confirmation modal for resetting current data
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Reset All Data?", style = MaterialTheme.typography.titleLarge, color = AccentCoral, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all tasks, recurring items, meal logs, and daily nutrition goals? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = extras.textAlpha80
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetData()
                        showResetConfirmDialog = false
                        onDismiss()
                        Toast.makeText(context, "All app data has been reset.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCoral, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Yes, Reset Data", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = extras.textAlpha60)
                }
            }
        )
    }
}
