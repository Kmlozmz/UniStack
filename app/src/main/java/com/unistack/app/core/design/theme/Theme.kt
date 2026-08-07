package com.unistack.app.core.design.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
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
        highContrast = accessibility.highContrastEnabled,
        dynamicAccent = dynamicAccent(darkTheme)
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

/**
 * Roles tonales extraídos del fondo de pantalla del sistema (Material You / Monet).
 * Devuelve null en API < 31, donde el llamador cae al violeta de marca.
 *
 * Tomamos el esquema que corresponde al modo actual —incluido el oscuro, con su `primary`
 * pastel de tono 80—. Es seguro porque el contenido encima se resuelve con
 * [UniStackColors.contentColorOn] en vez de asumir blanco.
 */
@Composable
private fun dynamicAccent(darkTheme: Boolean): DynamicAccent? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val context = LocalContext.current
    val scheme = if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    return DynamicAccent(
        primary = scheme.primary,
        primaryContainer = scheme.primaryContainer,
        onPrimaryContainer = scheme.onPrimaryContainer
    )
}

private fun lightUniStackColorScheme() = lightColorScheme(
    primary = UniStackColors.Primary,
    onPrimary = UniStackColors.OnPrimary,
    primaryContainer = UniStackColors.PrimaryLight,
    onPrimaryContainer = UniStackColors.OnPrimaryContainer,
    secondary = UniStackColors.Blue,
    onSecondary = UniStackColors.contentColorOn(UniStackColors.Blue),
    secondaryContainer = UniStackColors.BlueLight,
    tertiary = UniStackColors.Teal,
    onTertiary = UniStackColors.contentColorOn(UniStackColors.Teal),
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
    onPrimary = UniStackColors.OnPrimary,
    primaryContainer = UniStackColors.PrimaryLight,
    onPrimaryContainer = UniStackColors.OnPrimaryContainer,
    secondary = UniStackColors.Blue,
    onSecondary = UniStackColors.contentColorOn(UniStackColors.Blue),
    secondaryContainer = UniStackColors.BlueLight,
    tertiary = UniStackColors.Teal,
    onTertiary = UniStackColors.contentColorOn(UniStackColors.Teal),
    tertiaryContainer = UniStackColors.TealLight,
    background = UniStackColors.Background,
    onBackground = UniStackColors.TextPrimary,
    surface = UniStackColors.Card,
    onSurface = UniStackColors.TextPrimary,
    surfaceVariant = UniStackColors.SurfaceVariant,
    onSurfaceVariant = UniStackColors.TextSecondary,
    outline = UniStackColors.SoftOutline
)
