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

private val DarkColorScheme =
  darkColorScheme(
    primary = CalmingTeal,
    secondary = SoothingIndigo,
    tertiary = CalmSage,
    background = DeepSlateBlue,
    surface = MutedNavy,
    onPrimary = DeepSlateBlue,
    onSecondary = CaringIvory,
    onBackground = CaringIvory,
    onSurface = CaringIvory,
    error = WarningCoral
  )

private val LightColorScheme =
  lightColorScheme(
    primary = SoothingIndigo,
    secondary = CalmingTeal,
    tertiary = CalmSage,
    background = GentleLinen,
    surface = ElegantCardWhite,
    onPrimary = ElegantCardWhite,
    onSecondary = ElegantCardWhite,
    onBackground = QuietCharcoal,
    onSurface = QuietCharcoal,
    error = WarningCoral
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Elegant Dark theme
  dynamicColor: Boolean = false, // Disable dynamic colors to keep Elegant Dark intact
  content: @Composable () -> Unit,
) {
  // Always use our custom DarkColorScheme for MindBridge Elegant Dark feel
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
