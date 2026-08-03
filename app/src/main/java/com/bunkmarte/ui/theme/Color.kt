package com.bunkmarte.ui.theme

import androidx.compose.ui.graphics.Color
import com.bunkmarte.data.model.AttendanceStatus

// ── Dark Theme Core ──
val PureBlack = Color(0xFF000000)
val DeepDark = Color(0xFF0A0A0A)
val CardDark = Color(0xFF1A1A2E)              // Slightly elevated dark with purple undertone
val CardDarkVariant = Color(0xFF16213E)       // Alternative card surface
val ElevatedSurface = Color(0xFF1E1E32)       // For nav bars, top bars

// ── Primary Accent: Neon Purple ──
val NeonPurple = Color(0xFFBB86FC)            // Vibrant main accent
val DeepViolet = Color(0xFF7C4DFF)            // Deeper purple for emphasis
val PurpleGlow = Color(0xFF9C27B0)            // Rich purple for active states
val PurpleContainer = Color(0xFF2D1B69)       // Muted purple container
val OnPurpleContainer = Color(0xFFE8DAFF)     // Text on purple containers

// ── Text ──
val PureWhite = Color(0xFFFFFFFF)
val LightGrayText = Color(0xFFB0B0B0)         // Secondary text
val SubtleGray = Color(0xFF6B6B7B)            // Tertiary / hint text
val DimGray = Color(0xFF2A2A3A)               // Borders, dividers

// ── Error ──
val NeonRed = Color(0xFFFF5252)

// ── Attendance status colors — Neon/Pastel variants for dark mode ──
val NeonGreen = Color(0xFF69F0AE)             // Present — Neon mint green
val NeonCoral = Color(0xFFFF5252)             // Absent — Neon coral red
val NeonOrange = Color(0xFFFFAB40)            // Bunk — Neon amber orange
val NeonMagenta = Color(0xFFFF4081)           // Mass Bunk — Neon hot pink
val NeonCyan = Color(0xFF18FFFF)              // Proxy/Freebie — Neon cyan
val NeonSilver = Color(0xFF90A4AE)            // Cancelled — Pastel silver blue
val NeonIndigo = Color(0xFF8C9EFF)            // Exam Day — Pastel indigo
val NeonLavender = Color(0xFFCE93D8)          // Holiday — Soft lavender

// ── Warning zone colors (for Case 1 percentage) — Neon variants ──
val SafeGreen = Color(0xFF69F0AE)             // ≥ 75% — Neon green
val DangerAmber = Color(0xFFFFD740)           // 60–75% — Neon gold
val CriticalRed = Color(0xFFFF5252)           // < 60% — Neon red

/**
 * Extension property mapping each AttendanceStatus to its neon display color.
 * Designed for maximum readability against dark card surfaces.
 */
val AttendanceStatus.statusColor: Color
    get() = when (this) {
        AttendanceStatus.PRESENT -> NeonGreen
        AttendanceStatus.ABSENT -> NeonCoral
        AttendanceStatus.BUNK -> NeonOrange
        AttendanceStatus.MASS_BUNK -> NeonMagenta
        AttendanceStatus.PROXY -> NeonCyan
        AttendanceStatus.CANCELLED -> NeonSilver
        AttendanceStatus.EXAM_DAY -> NeonIndigo
        AttendanceStatus.HOLIDAY -> NeonLavender
    }
