package com.unistack.app.core.design.theme

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_user.domain.AccentStyle
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.TextScalePreference
import com.unistack.app.feature_user.domain.TypographyStyle

/**
 * La identidad cromática de la sección en la que se está.
 *
 * Se provee aquí y no se importa suelta porque depende del tema: los mismos roles tienen dos
 * juegos de valores, claro y oscuro.
 */
val LocalSectionColors = staticCompositionLocalOf { SectionColors.Light }

/**
 * El tema de la app: Material 3 Expressive.
 *
 * Lo que provee [MaterialExpressiveTheme] y no proveía el `MaterialTheme` anterior es el
 * **esquema de movimiento**. A partir de aquí, los componentes de Material animan con muelles
 * —`spatial` con rebote para lo que se mueve, `effects` sin rebote para color y opacidad— en
 * lugar de con duraciones fijas. Un muelle interrumpido a mitad de camino sale de donde está;
 * un `tween` salta al principio de la curva nueva. Es exactamente lo que se veía al cambiar de
 * pestaña a golpes.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UniStackTheme(
    darkTheme: Boolean = false,
    oledTheme: Boolean = false,
    appearance: AppearancePreferences = AppearancePreferences.defaults(),
    accessibility: AccessibilityPreferences = AccessibilityPreferences(),
    content: @Composable () -> Unit
) {
    AppearanceRuntime.cornerStyle = appearance.cornerStyle

    val scheme = expressiveColorScheme(darkTheme = darkTheme, oledTheme = oledTheme, appearance = appearance)
    val sections = SectionColors.forTheme(darkTheme)

    val typography = expressiveTypography(
        appearanceTypography(useSystemFont = appearance.typographyStyle == TypographyStyle.SYSTEM)
    )

    // La preferencia de "texto grande" se aplica sobre el fontScale de la densidad, no
    // sobre los estilos de tipografía. Así la respetan TODAS las medidas en sp de la app,
    // incluidas las que se declaran sueltas en las pantallas; escalando solo la Typography,
    // cualquier `fontSize = 13.sp` se saltaba el ajuste.
    val density = LocalDensity.current
    val textScale = if (accessibility.textScale == TextScalePreference.LARGE) 1.10f else 1f

    CompositionLocalProvider(
        LocalSectionColors provides sections,
        LocalIsDarkTheme provides darkTheme,
        LocalAppearancePreferences provides appearance,
        LocalAccessibilityPreferences provides accessibility,
        LocalMotionDurationScale provides accessibility.motionScale(),
        LocalInterfaceSpacing provides appearance.interfaceSpacing(),
        LocalDensity provides Density(
            density = density.density,
            fontScale = density.fontScale * textScale
        )
    ) {
        MaterialExpressiveTheme(
            colorScheme = scheme,
            motionScheme = MotionScheme.expressive(),
            shapes = ExpressiveShapeScale,
            typography = typography,
            content = content
        )
    }
}

/**
 * La escala de formas, una sola y sin preferencia de esquinas.
 *
 * Los cinco tamaños de Material, con los valores de la maqueta. Que no dependa de un ajuste es
 * la decisión: cada variante de esquina multiplicaba por tres los estados que había que mirar
 * en cada pantalla nueva, a cambio de una diferencia que casi nadie tocaba.
 */
internal val ExpressiveShapeScale = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * El esquema que toca: el de la marca, o el del fondo de pantalla si se ha pedido.
 *
 * El violeta manda por defecto. Monet queda a un toque de distancia en Apariencia, pero
 * dejarlo de serie hacía que la app se viera del color del fondo de pantalla de cada quien:
 * UniStack no tenía identidad propia en su propia app.
 */
@Composable
private fun expressiveColorScheme(
    darkTheme: Boolean,
    oledTheme: Boolean,
    appearance: AppearancePreferences
): ColorScheme {
    val base = when {
        appearance.accentStyle == AccentStyle.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ExpressiveDarkScheme
        else -> ExpressiveLightScheme
    }

    // OLED apaga el píxel: el fondo y el contenedor más bajo van a negro puro, y el resto de
    // los niveles se conservan para que la jerarquía de profundidad no se venga abajo.
    // design-tokens-ok-begin: el negro puro ES el modo OLED, no un color de marca
    return if (oledTheme && darkTheme) {
        base.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerLowest = Color.Black
        )
    } else {
        base
    }
    // design-tokens-ok-end
}
