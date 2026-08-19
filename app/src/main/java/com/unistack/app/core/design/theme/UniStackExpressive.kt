// design-tokens-exempt: este archivo ES la definición del lenguaje de diseño.
// Aquí viven los esquemas de color, la escala tipográfica, la escala de formas y la paleta
// categórica de secciones. El resto de la app consume estos tokens; nadie más declara colores.
package com.unistack.app.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.pow

/**
 * Material 3 Expressive, tal cual, como identidad de UniStack.
 *
 * Este archivo es **el único sitio de la app que nombra a Material**. Esa es la condición que
 * hace tolerable depender de un canal alpha: cuando `material3` renombre algo entre versiones
 * —`TonalToggleButton` pasó a `FilledTonalToggleButton` en alpha25—, se arregla aquí y en
 * ningún otro lado.
 *
 * Lo que antes se derivaba en tiempo de ejecución mezclando colores (`applyTheme()` calculaba
 * contenedores, superficies y contornos a base de interpolar) ahora está escrito: un esquema
 * tonal completo por tema. Derivar a mano producía grises donde Material produce pasteles, y
 * obligaba a mantener la matemática de contraste por nuestra cuenta.
 */

// ---------------------------------------------------------------------------------------------
// Color
// ---------------------------------------------------------------------------------------------

/**
 * Esquema claro, generado desde la semilla violeta de la marca (`#5B46E0`).
 *
 * Los roles neutros tiran a violeta en vez de a gris puro: es lo que hace que la app se lea
 * como una sola pieza y no como un acento de color sobre una base ajena.
 */
internal val ExpressiveLightScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF5645D6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4DEFF),
    onPrimaryContainer = Color(0xFF1A0080),

    // El secundario es familia del primario, no un azul con significado propio.
    //
    // Esto deshace una decisión vieja: el indicador de la barra de navegación usaba el
    // contenedor del acento porque `secondary` era un azul suelto y dejaba una píldora azul
    // bajo un acento violeta. Con un esquema generado desde la semilla, `secondaryContainer`
    // ya es el token correcto y se puede seguir el spec a la letra.
    secondary = Color(0xFF605A70),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE7DEF8),
    onSecondaryContainer = Color(0xFF1C1929),

    tertiary = Color(0xFF7C5264),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E7),
    onTertiaryContainer = Color(0xFF301120),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    background = Color(0xFFFCF8FF),
    onBackground = Color(0xFF1B1B21),
    surface = Color(0xFFFCF8FF),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE5DFEB),
    onSurfaceVariant = Color(0xFF48454E),

    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF7F2FC),
    surfaceContainer = Color(0xFFF1ECF7),
    surfaceContainerHigh = Color(0xFFEBE5F1),
    surfaceContainerHighest = Color(0xFFE5DFEB),

    outline = Color(0xFF79767F),
    outlineVariant = Color(0xFFCBC5D0),
    scrim = Color(0xFF000000)
)

/**
 * Esquema oscuro.
 *
 * El fondo es un negro con sesgo violeta, no azulado: bajo un acento morado, un fondo que tira
 * al azul hace que los subtonos peleen entre sí. Se conserva de la paleta anterior porque esa
 * observación sigue siendo cierta.
 */
internal val ExpressiveDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFC6C0FF),
    onPrimary = Color(0xFF2A1878),
    primaryContainer = Color(0xFF3F2F92),
    onPrimaryContainer = Color(0xFFE4DEFF),

    secondary = Color(0xFFCAC3DB),
    onSecondary = Color(0xFF322C40),
    secondaryContainer = Color(0xFF484257),
    onSecondaryContainer = Color(0xFFE7DEF8),

    tertiary = Color(0xFFEFB8CE),
    onTertiary = Color(0xFF4A2536),
    tertiaryContainer = Color(0xFF623B4C),
    onTertiaryContainer = Color(0xFFFFD8E7),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    background = Color(0xFF131218),
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF131218),
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    surfaceContainerLowest = Color(0xFF0D0C12),
    surfaceContainerLow = Color(0xFF1B1A21),
    surfaceContainer = Color(0xFF1F1E25),
    surfaceContainerHigh = Color(0xFF2A2830),
    surfaceContainerHighest = Color(0xFF35323B),

    outline = Color(0xFF948F99),
    outlineVariant = Color(0xFF49454F),
    scrim = Color(0xFF000000)
)

/**
 * La identidad cromática de cada sección, y los estados de rendimiento.
 *
 * **Vive fuera del `ColorScheme` a propósito.** Un `ColorScheme` describe jerarquía —qué es
 * acción principal, qué es superficie, qué es error—; esto describe *de qué se está hablando*.
 * Meterlos en el mismo saco es lo que hacía que un solo color, `Coral`, significara a la vez
 * «Gastos» (35 usos) y «error» (23 archivos), y que no se pudiera afinar uno sin mover el otro.
 *
 * Aquí son dos entradas distintas con dos valores distintos: [expenses] y el `error` del
 * esquema. Ya se pueden tocar por separado.
 */
data class SectionColors(
    /** Horario. */
    val schedule: Color,
    val onSchedule: Color,
    val scheduleContainer: Color,
    val onScheduleContainer: Color,

    /** Gastos. Rojo por identidad, no por alarma. */
    val expenses: Color,
    val onExpenses: Color,
    val expensesContainer: Color,
    val onExpensesContainer: Color,

    /** Meta asegurada, materia al día. */
    val onTrack: Color,
    val onOnTrack: Color,
    val onTrackContainer: Color,
    val onOnTrackContainer: Color,

    /** En riesgo, vence hoy. */
    val atRisk: Color,
    val onAtRisk: Color,
    val atRiskContainer: Color,
    val onAtRiskContainer: Color
) {
    companion object {
        val Light = SectionColors(
            schedule = Color(0xFF2A62CE),
            onSchedule = Color(0xFFFFFFFF),
            scheduleContainer = Color(0xFFDCE3FF),
            onScheduleContainer = Color(0xFF001849),

            expenses = Color(0xFFB03A2E),
            onExpenses = Color(0xFFFFFFFF),
            expensesContainer = Color(0xFFFFDBD3),
            onExpensesContainer = Color(0xFF3F0A03),

            onTrack = Color(0xFF2C6C46),
            onOnTrack = Color(0xFFFFFFFF),
            onTrackContainer = Color(0xFFB6F0C7),
            onOnTrackContainer = Color(0xFF00210F),

            atRisk = Color(0xFF7A5900),
            onAtRisk = Color(0xFFFFFFFF),
            atRiskContainer = Color(0xFFFFDF9B),
            onAtRiskContainer = Color(0xFF261A00)
        )

        val Dark = SectionColors(
            schedule = Color(0xFFB3C5FF),
            onSchedule = Color(0xFF002D7A),
            scheduleContainer = Color(0xFF0E409F),
            onScheduleContainer = Color(0xFFDCE3FF),

            expenses = Color(0xFFFFB4A6),
            onExpenses = Color(0xFF5F1508),
            expensesContainer = Color(0xFF7A2618),
            onExpensesContainer = Color(0xFFFFDBD3),

            onTrack = Color(0xFF9BD4AC),
            onOnTrack = Color(0xFF00391E),
            onTrackContainer = Color(0xFF0E5132),
            onOnTrackContainer = Color(0xFFB6F0C7),

            atRisk = Color(0xFFF2C048),
            onAtRisk = Color(0xFF412D00),
            atRiskContainer = Color(0xFF5C4300),
            onAtRiskContainer = Color(0xFFFFDF9B)
        )

        fun forTheme(darkTheme: Boolean): SectionColors = if (darkTheme) Dark else Light
    }
}

// ---------------------------------------------------------------------------------------------
// Tipografía
// ---------------------------------------------------------------------------------------------

/**
 * La escala doble de Material 3 Expressive.
 *
 * Cada estilo tiene una gemela `…Emphasized`: mismo tamaño y mismo interlineado, más peso. El
 * énfasis deja de ser un `fontWeight = FontWeight.Bold` suelto escrito en cada pantalla y pasa
 * a ser una elección de estilo con nombre, que se cambia en un sitio.
 *
 * Los tamaños son los de la escala de Material. Lo que no es de Material es la elección de
 * *cuándo* usar la variante enfatizada, y eso vive en las pantallas.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun expressiveTypography(base: Typography = Typography()): Typography = base.copy(
    displayLarge = base.displayLarge.copy(fontSize = 57.sp, lineHeight = 64.sp, fontWeight = FontWeight.Normal),
    displayLargeEmphasized = base.displayLarge.copy(fontSize = 57.sp, lineHeight = 64.sp, fontWeight = FontWeight.Bold),

    displayMedium = base.displayMedium.copy(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = FontWeight.Normal),
    displayMediumEmphasized = base.displayMedium.copy(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = FontWeight.Bold),

    displaySmall = base.displaySmall.copy(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Normal),
    displaySmallEmphasized = base.displaySmall.copy(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = FontWeight.Bold),

    headlineLarge = base.headlineLarge.copy(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Normal),
    headlineLargeEmphasized = base.headlineLarge.copy(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),

    headlineMedium = base.headlineMedium.copy(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Normal),
    headlineMediumEmphasized = base.headlineMedium.copy(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),

    headlineSmall = base.headlineSmall.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Normal),
    headlineSmallEmphasized = base.headlineSmall.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),

    titleLarge = base.titleLarge.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Normal),
    titleLargeEmphasized = base.titleLarge.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium),

    titleMedium = base.titleMedium.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),
    titleMediumEmphasized = base.titleMedium.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold),

    titleSmall = base.titleSmall.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    titleSmallEmphasized = base.titleSmall.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),

    bodyLarge = base.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
    bodyLargeEmphasized = base.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Medium),

    bodyMedium = base.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
    bodyMediumEmphasized = base.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),

    bodySmall = base.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    bodySmallEmphasized = base.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),

    labelLarge = base.labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelLargeEmphasized = base.labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold),

    labelMedium = base.labelMedium.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelMediumEmphasized = base.labelMedium.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold),

    labelSmall = base.labelSmall.copy(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    labelSmallEmphasized = base.labelSmall.copy(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
)

/**
 * Estilo para rótulos de sección: versalitas espaciadas, como en las maquetas.
 *
 * No es un estilo de Material —Material no tiene un rol para esto— así que se declara aquí en
 * vez de repetir `letterSpacing` a ojo en cada pantalla.
 */
val SectionLabelStyle: TextStyle = TextStyle(
    fontSize = 11.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Bold,
    letterSpacing = 1.0.sp
)

// ---------------------------------------------------------------------------------------------
// Utilidades del tema
// ---------------------------------------------------------------------------------------------

/**
 * Si el tema en curso es el oscuro.
 *
 * No es `isSystemInDarkTheme()`: el usuario puede forzar claro u oscuro desde Apariencia, y
 * entonces el sistema y la app no coinciden. Lo provee [UniStackTheme].
 */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/** Tinta oscura para contenido sobre superficies claras: más suave que el negro puro. */
private val DarkInk = Color(0xFF1B1B21)
private const val DarkInkLuminance = 0.0136f

/**
 * Color de contenido legible sobre un fondo **arbitrario**, eligiendo entre tinta clara y
 * oscura por ratio de contraste WCAG.
 *
 * Sigue haciendo falta con Material: el esquema trae un `onX` por cada rol suyo, pero no por
 * los colores que elige el usuario —el color de una materia, un acento de Monet—. Sobre esos,
 * `Color.White` fijo deja de ser legible en cuanto el color es claro.
 */
fun contentColorOn(background: Color): Color {
    val luminance = relativeLuminance(background)
    val contrastWithLight = 1.05f / (luminance + 0.05f)
    val contrastWithDark = (luminance + 0.05f) / (DarkInkLuminance + 0.05f)
    return if (contrastWithLight >= contrastWithDark) Color.White else DarkInk
}

private fun relativeLuminance(color: Color): Float {
    fun channel(value: Float): Float =
        if (value <= 0.03928f) value / 12.92f else ((value + 0.055f) / 1.055f).pow(2.4f)
    return 0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)
}
