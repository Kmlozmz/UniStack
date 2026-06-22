package com.unistack.app.core.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.TextScalePreference
import com.unistack.app.feature_user.domain.TypographyStyle

@Composable
fun UniStackTheme(
    darkTheme: Boolean = false,
    oledTheme: Boolean = false,
    appearance: AppearancePreferences = AppearancePreferences.defaults(),
    accessibility: AccessibilityPreferences = AccessibilityPreferences(),
    content: @Composable () -> Unit
) {
    AppearanceRuntime.cornerStyle = appearance.cornerStyle
    UniStackColors.applyTheme(
        darkTheme = darkTheme,
        oledTheme = oledTheme,
        appearance = appearance,
        highContrast = accessibility.highContrastEnabled
    )
    val radius = appearance.cornerStyle.cardRadius()
    val shapes = UniStackShapes.copy(
        extraSmall = RoundedCornerShape((radius.value * 0.55f).coerceAtLeast(4f).dp),
        small = RoundedCornerShape((radius.value * 0.75f).coerceAtLeast(6f).dp),
        medium = RoundedCornerShape(radius),
        large = RoundedCornerShape(radius + 4.dp),
        extraLarge = RoundedCornerShape(radius + 10.dp)
    )
    val typography = appearanceTypography(
        scale = if (accessibility.textScale == TextScalePreference.LARGE) 1.10f else 1f,
        useSystemFont = appearance.typographyStyle == TypographyStyle.SYSTEM
    )

    CompositionLocalProvider(
        LocalAppearancePreferences provides appearance,
        LocalAccessibilityPreferences provides accessibility,
        LocalMotionDurationScale provides accessibility.motionScale(),
        LocalInterfaceSpacing provides appearance.interfaceSpacing()
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkUniStackColorScheme() else lightUniStackColorScheme(),
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

private fun lightUniStackColorScheme() = lightColorScheme(
    primary = UniStackColors.Primary,
    onPrimary = contrastingText(UniStackColors.Primary),
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
    onPrimary = contrastingText(UniStackColors.Primary),
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

private fun contrastingText(background: Color): Color {
    val luminance = 0.299f * background.red + 0.587f * background.green + 0.114f * background.blue
    return if (luminance > 0.58f) Color(0xFF171427) else Color.White
}
