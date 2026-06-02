package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SleekPrimaryBlueDark,
    secondary = SleekSecondaryContainer,
    tertiary = GoldAccent,
    background = SleekBackgroundDark,
    surface = SleekSurfaceDark,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SleekCardDark,
    onSurfaceVariant = TextSecondaryDark,
    error = ExpenseRed
)

private val LightColorScheme = lightColorScheme(
    primary = SleekPrimaryBlue,
    secondary = SleekSecondaryContainer,
    tertiary = GoldAccent,
    background = SleekBackgroundLight,
    surface = SleekSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color(0xFF191C1E),
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SleekSecondaryContainer.copy(alpha = 0.5f),
    onSurfaceVariant = TextSecondaryLight,
    error = ExpenseRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Sleek Interface style default (Light high-end slate blue)
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
