package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5), // Indigo 600
    onPrimary = Color.White,
    secondary = Color(0xFF1D4ED8), // Blue 700
    onSecondary = Color.White,
    tertiary = Color(0xFF10B981), // Emerald 500
    background = Color(0xFFF3F4F9), // Slate light
    onBackground = Color(0xFF0F172A), // Slate 900
    surface = Color(0xCCFFFFFF), // Translucent Glass White (80% / 0xCC for readability)
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0x66FFFFFF), // High Translucency Glass (40% / 0x66)
    onSurfaceVariant = Color(0xFF475569), // Muted slate gray
    outline = Color(0x22FFFFFF), // Very subtle transparent border
    outlineVariant = Color(0x1F475569) // Muted separator line
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8), // Indigo 400
    onPrimary = Color(0xFF0F172A),
    secondary = Color(0xFF60A5FA), // Blue 400
    onSecondary = Color(0xFF0F172A),
    tertiary = Color(0xFF34D399),
    background = Color(0xFF0B1120), // Deep blue-gray space
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xCC1E293B), // Translucent Dark Glass
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0x66334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0x22FFFFFF),
    outlineVariant = Color(0x1F94A3B8)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set default dynamicColor to false to fully display the high-fidelity Frosted Glass theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
