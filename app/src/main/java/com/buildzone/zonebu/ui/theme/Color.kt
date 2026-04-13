package com.buildzone.zonebu.ui.theme

import androidx.compose.ui.graphics.Color

// Zone colors
val ColdZone = Color(0xFF1D4ED8)
val CoolZone = Color(0xFF38BDF8)
val NeutralZone = Color(0xFF34D399)
val WarmZone = Color(0xFFF59E0B)
val HotZone = Color(0xFFEF4444)

// Accent colors
val AccentBlue = Color(0xFF3B82F6)
val AccentIndigo = Color(0xFF6366F1)
val AccentCyan = Color(0xFF22D3EE)

// State colors
val ErrorRed = Color(0xFFDC2626)
val WarningYellow = Color(0xFFFACC15)
val SuccessGreen = Color(0xFF22C55E)

// Specific zone colors for map
val MapColdBlue = Color(0xFF1D4ED8)
val MapMoistCyan = Color(0xFF06B6D4)
val MapNormal = Color(0xFF10B981)
val MapWarmOrange = Color(0xFFFB923C)
val MapOverheatRed = Color(0xFFEF4444)

// Light theme
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSecondaryBackground = Color(0xFFEEF2F7)
val LightBorder = Color(0xFFE2E8F0)
val LightOnSurface = Color(0xFF1E293B)
val LightOnBackground = Color(0xFF334155)

// Dark theme
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSecondaryBackground = Color(0xFF162032)
val DarkBorder = Color(0xFF334155)
val DarkOnSurface = Color(0xFFF1F5F9)
val DarkOnBackground = Color(0xFFCBD5E1)

fun temperatureToColor(temp: Float): Color {
    val clampedTemp = temp.coerceIn(-10f, 40f)
    return when {
        clampedTemp < 10f -> ColdZone
        clampedTemp < 16f -> lerpColorCustom(ColdZone, Color(0xFF2563EB), (clampedTemp - 10f) / 6f)
        clampedTemp < 18f -> lerpColorCustom(Color(0xFF2563EB), CoolZone, (clampedTemp - 16f) / 2f)
        clampedTemp < 20f -> lerpColorCustom(CoolZone, NeutralZone, (clampedTemp - 18f) / 2f)
        clampedTemp < 24f -> lerpColorCustom(NeutralZone, WarmZone, (clampedTemp - 20f) / 4f)
        clampedTemp < 28f -> lerpColorCustom(WarmZone, HotZone, (clampedTemp - 24f) / 4f)
        else -> HotZone
    }
}

fun humidityToColor(humidity: Float): Color {
    val clamped = humidity.coerceIn(0f, 100f)
    return when {
        clamped < 30f -> Color(0xFFEF4444)
        clamped < 40f -> lerpColorCustom(Color(0xFFEF4444), Color(0xFFF59E0B), (clamped - 30f) / 10f)
        clamped < 50f -> lerpColorCustom(Color(0xFFF59E0B), Color(0xFF34D399), (clamped - 40f) / 10f)
        clamped < 60f -> lerpColorCustom(Color(0xFF34D399), Color(0xFF38BDF8), (clamped - 50f) / 10f)
        clamped < 70f -> lerpColorCustom(Color(0xFF38BDF8), Color(0xFF3B82F6), (clamped - 60f) / 10f)
        else -> Color(0xFF1D4ED8)
    }
}

fun lerpColorCustom(start: Color, end: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = start.red + (end.red - start.red) * f,
        green = start.green + (end.green - start.green) * f,
        blue = start.blue + (end.blue - start.blue) * f,
        alpha = 1f
    )
}
