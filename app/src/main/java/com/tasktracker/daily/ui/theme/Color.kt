package com.tasktracker.daily.ui.theme

import androidx.compose.ui.graphics.Color

// --- Dark Theme Colors ---
val DarkBackground = Color(0xFF0D1117)
val DarkSurface = Color(0xFF161B22)
val DarkSurfaceVariant = Color(0xFF21262D)
val DarkBorder = Color(0xFF30363D)

val DarkTextPrimary = Color(0xFFF0F6FC)
val DarkTextSecondary = Color(0xFF8B949E)
val DarkTextMuted = Color(0xFF484F58)

// --- Light Theme Colors ---
val LightBackground = Color(0xFFF6F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEAECF0)
val LightBorder = Color(0xFFD0D7DE)

val LightTextPrimary = Color(0xFF1F2328)
val LightTextSecondary = Color(0xFF636C76)
val LightTextMuted = Color(0xFFB0B7C3)

// --- Shared / Brand Colors ---
val PrimaryEmerald = Color(0xFF39D353)
val PrimaryContainer = Color(0xFF0E4429)
val OnPrimaryContainer = Color(0xFF7EE787)

// --- Accent Colors (Semantic, used sparingly — the 10% in 60/30/10) ---
val AccentCoral = Color(0xFFFF6B6B)    // Protein, destructive actions
val AccentAmber = Color(0xFFFFD93D)    // Fat macro, warnings
val AccentSky = Color(0xFF4D96FF)      // Carbs macro, informational
val AccentMint = Color(0xFF6BCB77)     // Sugar macro, secondary green
val AccentPurple = Color(0xFFB388FF)   // Today-only / special tasks

// --- Gradient & Glow ---
val GradientStart = Color(0x1A39D353)  // Emerald @ 10% — gradient overlays on hero cards
val GradientEnd = Color(0x0039D353)    // Emerald @ 0% — gradient fade-out
val GlowGreen = Color(0x3339D353)      // Emerald @ 20% — glow/shadow around selected items

// --- Elevated Surface (slightly lighter than DarkSurface) ---
val CardElevatedDark = Color(0xFF1C2128)
val CardElevatedLight = Color(0xFFFFFFFF)

// --- Text Opacity Variants (matching mockup CSS text hierarchy) ---
val TextAlpha100 = Color(0xFFF0F6FC)          // --text-100: headings
val TextAlpha80 = Color(0xCCF0F6FC)           // --text-80: body
val TextAlpha60 = Color(0x99F0F6FC)           // --text-60: secondary
val TextAlpha40 = Color(0x66F0F6FC)           // --text-40: muted/hint
val TextAlpha20 = Color(0x33F0F6FC)           // --text-20: disabled

// GitHub contribution levels (0 to 4)
val HeatmapLevel0Dark = Color(0xFF161B22) // Empty / dark
val HeatmapLevel0Light = Color(0xFFEAECF0) // Empty / light
val HeatmapLevel1 = Color(0xFF0E4429)     // Darkest green (low completion)
val HeatmapLevel2 = Color(0xFF006D32)     // Medium-dark green
val HeatmapLevel3 = Color(0xFF26A641)     // Medium-bright green
val HeatmapLevel4 = Color(0xFF39D353)     // Lightest / brightest green (high completion)
