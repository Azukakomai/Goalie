package com.tasktracker.daily.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// --- Material Color Schemes ---

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryEmerald,
    onPrimary = DarkBackground,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    // We use tertiary to carry TextMuted
    tertiary = DarkTextMuted,
    onTertiary = DarkBackground
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryEmerald,
    onPrimary = LightBackground,
    primaryContainer = Color(0xFFD4F5DA),
    onPrimaryContainer = Color(0xFF004318),
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    // We use tertiary to carry TextMuted
    tertiary = LightTextMuted,
    onTertiary = LightBackground
)

// --- Custom Extras: heatmap zero-level color (theme-aware) ---

data class GoalieExtraColors(
    val heatmapLevel0: Color,
    val textMuted: Color
)

val LocalGoalieExtraColors = staticCompositionLocalOf {
    GoalieExtraColors(
        heatmapLevel0 = HeatmapLevel0Dark,
        textMuted = DarkTextMuted
    )
}

@Composable
fun DailyTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) {
        GoalieExtraColors(heatmapLevel0 = HeatmapLevel0Dark, textMuted = DarkTextMuted)
    } else {
        GoalieExtraColors(heatmapLevel0 = HeatmapLevel0Light, textMuted = LightTextMuted)
    }

    CompositionLocalProvider(LocalGoalieExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
