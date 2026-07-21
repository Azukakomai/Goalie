package com.tasktracker.daily.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tasktracker.daily.data.RecurrenceType
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.ui.theme.DarkBackground
import com.tasktracker.daily.ui.theme.DarkBorder
import com.tasktracker.daily.ui.theme.DarkSurface
import com.tasktracker.daily.ui.theme.PrimaryEmerald
import com.tasktracker.daily.ui.theme.TextMuted
import com.tasktracker.daily.ui.theme.TextPrimary

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (task.isCompleted) DarkSurface.copy(alpha = 0.6f) else DarkSurface,
        label = "taskBgColor"
    )

    val recurrenceType = RecurrenceType.fromString(task.recurrenceType)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryEmerald,
                    uncheckedColor = TextMuted,
                    checkmarkColor = DarkSurface
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) TextMuted else TextPrimary
                )

                if (recurrenceType != RecurrenceType.NONE) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(DarkBackground)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = "Recurring",
                                    tint = PrimaryEmerald,
                                    modifier = Modifier.height(12.dp).width(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val labelText = if (recurrenceType == RecurrenceType.CUSTOM) {
                                    "Every ${task.customIntervalDays} days"
                                } else {
                                    recurrenceType.label
                                }
                                Text(
                                    text = labelText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryEmerald
                                )
                            }
                        }
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = TextMuted
                )
            }
        }
    }
}
