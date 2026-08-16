package com.tasktracker.daily.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
    onPrimary = DarkBackground,
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

// --- Custom Shape Scale (matching mockup design system) ---

val GoalieShapes = Shapes(
    small = RoundedCornerShape(8.dp),    // Chips, badges, inline tags
    medium = RoundedCornerShape(16.dp),  // Buttons, text fields
    large = RoundedCornerShape(20.dp),   // Cards, dialogs
    extraLarge = RoundedCornerShape(24.dp) // Bottom sheet, hero cards
)

// --- Custom Extras: expanded color palette beyond Material scheme ---

@Immutable
data class GoalieExtraColors(
    val heatmapLevel0: Color,
    val heatmapLevel1: Color,
    val heatmapLevel2: Color,
    val heatmapLevel3: Color,
    val heatmapLevel4: Color,
    val textMuted: Color,
    // Accent colors
    val accentCoral: Color,
    val accentAmber: Color,
    val accentSky: Color,
    val accentMint: Color,
    // Glow & gradient
    val glowGreen: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    // Elevated surface
    val cardElevated: Color,
    // Text hierarchy (opacity variants)
    val textAlpha100: Color,
    val textAlpha80: Color,
    val textAlpha60: Color,
    val textAlpha40: Color,
    val textAlpha20: Color
)

val LocalGoalieExtraColors = staticCompositionLocalOf {
    GoalieExtraColors(
        heatmapLevel0 = HeatmapLevel0Dark,
        heatmapLevel1 = HeatmapLevel1Dark,
        heatmapLevel2 = HeatmapLevel2Dark,
        heatmapLevel3 = HeatmapLevel3Dark,
        heatmapLevel4 = HeatmapLevel4Dark,
        textMuted = DarkTextMuted,
        accentCoral = AccentCoral,
        accentAmber = AccentAmber,
        accentSky = AccentSky,
        accentMint = AccentMint,
        glowGreen = GlowGreen,
        gradientStart = GradientStart,
        gradientEnd = GradientEnd,
        cardElevated = CardElevatedDark,
        textAlpha100 = TextAlpha100,
        textAlpha80 = TextAlpha80,
        textAlpha60 = TextAlpha60,
        textAlpha40 = TextAlpha40,
        textAlpha20 = TextAlpha20
    )
}

@Composable
fun DailyTrackerTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extraColors = if (darkTheme) {
        GoalieExtraColors(
            heatmapLevel0 = HeatmapLevel0Dark,
            heatmapLevel1 = HeatmapLevel1Dark,
            heatmapLevel2 = HeatmapLevel2Dark,
            heatmapLevel3 = HeatmapLevel3Dark,
            heatmapLevel4 = HeatmapLevel4Dark,
            textMuted = DarkTextMuted,
            accentCoral = AccentCoral,
            accentAmber = AccentAmber,
            accentSky = AccentSky,
            accentMint = AccentMint,
            glowGreen = GlowGreen,
            gradientStart = GradientStart,
            gradientEnd = GradientEnd,
            cardElevated = CardElevatedDark,
            textAlpha100 = TextAlpha100,
            textAlpha80 = TextAlpha80,
            textAlpha60 = TextAlpha60,
            textAlpha40 = TextAlpha40,
            textAlpha20 = TextAlpha20
        )
    } else {
        GoalieExtraColors(
            heatmapLevel0 = HeatmapLevel0Light,
            heatmapLevel1 = HeatmapLevel1Light,
            heatmapLevel2 = HeatmapLevel2Light,
            heatmapLevel3 = HeatmapLevel3Light,
            heatmapLevel4 = HeatmapLevel4Light,
            textMuted = LightTextMuted,
            accentCoral = AccentCoral,
            accentAmber = AccentAmber,
            accentSky = AccentSky,
            accentMint = AccentMint,
            glowGreen = Color(0x2639D353),
            gradientStart = GradientStart,
            gradientEnd = GradientEnd,
            cardElevated = CardElevatedLight,
            textAlpha100 = LightTextPrimary,
            textAlpha80 = LightTextPrimary.copy(alpha = 0.8f),
            textAlpha60 = LightTextSecondary,
            textAlpha40 = LightTextMuted,
            textAlpha20 = LightTextMuted.copy(alpha = 0.5f)
        )
    }

    CompositionLocalProvider(LocalGoalieExtraColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = GoalieShapes,
            content = content
        )
    }
}
