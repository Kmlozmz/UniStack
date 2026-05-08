package com.unistack.app.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun UniStackTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    UniStackColors.applyTheme(darkTheme)

    MaterialTheme(
        colorScheme = if (darkTheme) darkUniStackColorScheme() else lightUniStackColorScheme(),
        typography = UniStackTypography,
        shapes = UniStackShapes,
        content = content
    )
}

private fun lightUniStackColorScheme() = lightColorScheme(
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

private fun darkUniStackColorScheme() = darkColorScheme(
    primary = UniStackColors.Primary,
    onPrimary = Color(0xFF26125F),
    primaryContainer = UniStackColors.PrimaryLight,
    onPrimaryContainer = UniStackColors.PrimaryDark,
    secondary = UniStackColors.Blue,
    onSecondary = Color(0xFF0A2446),
    secondaryContainer = UniStackColors.BlueLight,
    tertiary = UniStackColors.Teal,
    onTertiary = Color(0xFF003735),
    tertiaryContainer = UniStackColors.TealLight,
    background = UniStackColors.Background,
    onBackground = UniStackColors.TextPrimary,
    surface = UniStackColors.Card,
    onSurface = UniStackColors.TextPrimary,
    surfaceVariant = UniStackColors.SurfaceVariant,
    onSurfaceVariant = UniStackColors.TextSecondary,
    outline = UniStackColors.SoftOutline
)
