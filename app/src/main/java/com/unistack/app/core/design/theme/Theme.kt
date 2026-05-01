package com.unistack.app.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = UniStackColors.Primary,
    onPrimary = Color.White,
    primaryContainer = UniStackColors.PrimaryLight,
    onPrimaryContainer = UniStackColors.PrimaryDark,
    secondary = UniStackColors.Blue,
    onSecondary = Color.White,
    secondaryContainer = UniStackColors.BlueLight,
    tertiary = UniStackColors.Teal,
    onTertiary = Color.White,
    tertiaryContainer = UniStackColors.TealLight,
    background = UniStackColors.Background,
    onBackground = UniStackColors.TextPrimary,
    surface = UniStackColors.Card,
    onSurface = UniStackColors.TextPrimary,
    surfaceVariant = UniStackColors.SurfaceVariant,
    onSurfaceVariant = UniStackColors.TextSecondary,
    outline = UniStackColors.SoftOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCFC2FF),
    onPrimary = Color(0xFF26125F),
    primaryContainer = UniStackColors.PrimaryDark,
    onPrimaryContainer = UniStackColors.PrimaryLight,
    secondary = Color(0xFFA9C8FF),
    tertiary = Color(0xFF80DDD6),
    background = Color(0xFF111018),
    onBackground = Color(0xFFF7F3FF),
    surface = Color(0xFF1B1924),
    onSurface = Color(0xFFF7F3FF),
    surfaceVariant = Color(0xFF282432),
    onSurfaceVariant = Color(0xFFD2CADC)
)

@Composable
fun UniStackTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = UniStackTypography,
        shapes = UniStackShapes,
        content = content
    )
}
