package com.tasktracker.daily.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.tasktracker.daily.data.RecurrenceType
import com.tasktracker.daily.data.Task
import com.tasktracker.daily.ui.theme.AccentAmber
import com.tasktracker.daily.ui.theme.AccentCoral
import com.tasktracker.daily.ui.theme.AccentMint
import com.tasktracker.daily.ui.theme.AccentPurple
import com.tasktracker.daily.ui.theme.AccentSky
import com.tasktracker.daily.ui.theme.LocalGoalieExtraColors

import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = LocalGoalieExtraColors.current
    val isCompleted = task.isCompleted

    // Background color animation
    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "taskBgColor"
    )

    // Left accent bar color (only visible when completed)
    val accentBarColor = MaterialTheme.colorScheme.primary

    // Checkbox bounce animation
    val checkScale = remember { Animatable(1f) }
    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            checkScale.snapTo(0.7f)
            checkScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f)
            )
        }
    }

    val recurrenceType = RecurrenceType.fromString(task.recurrenceType)
    val startDate = remember(task.startDateEpochDay) { LocalDate.ofEpochDay(task.startDateEpochDay) }
    val today = remember { LocalDate.now() }
    val isFutureStart = startDate.isAfter(today)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .then(
                if (isCompleted) {
                    Modifier.drawBehind {
                        // 3dp left accent bar
                        drawRoundRect(
                            color = accentBarColor,
                            topLeft = Offset(0f, 8.dp.toPx()),
                            size = Size(3.dp.toPx(), size.height - 16.dp.toPx()),
                            cornerRadius = CornerRadius(3.dp.toPx())
                        )
                    }
                } else Modifier
            )
            .clickable { onToggle() }
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Custom circle checkbox
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        scaleX = checkScale.value
                        scaleY = checkScale.value
                    }
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                    )
                    .drawBehind {
                        if (!isCompleted) {
                            drawCircle(
                                color = accentBarColor.copy(alpha = 0.2f),
                                radius = size.minDimension / 2f,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                        }
                    }
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Task body
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (isCompleted) extras.textAlpha40 else extras.textAlpha100
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isFutureStart) {
                        TagPill(
                            text = "Starts ${startDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                            color = AccentSky
                        )
                    }

                    if (recurrenceType != RecurrenceType.NONE) {
                        val labelText = if (recurrenceType == RecurrenceType.CUSTOM) {
                            "Every ${task.customIntervalDays} days"
                        } else {
                            recurrenceType.label
                        }
                        val tagColor = when (recurrenceType) {
                            RecurrenceType.DAILY -> AccentMint
                            RecurrenceType.WEEKLY -> AccentAmber
                            RecurrenceType.YEARLY -> AccentSky
                            RecurrenceType.CUSTOM -> AccentCoral
                            else -> AccentMint
                        }
                        TagPill(text = labelText, color = tagColor)
                    } else if (!isFutureStart) {
                        // One-time task for today — purple "Today only" tag
                        TagPill(text = "Today only", color = AccentPurple)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Delete Task Icon Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = extras.textAlpha40,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Colored tag pill matching the mockup's `.task-tag` design.
 * Uses category-based colors with faint tinted backgrounds.
 */
@Composable
private fun TagPill(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
            ),
            color = color
        )
    }
}
