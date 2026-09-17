// design-tokens-exempt: este archivo ES la definición del lenguaje de diseño.
// Aquí viven los esquemas de color, la escala tipográfica, la escala de formas y la paleta
// categórica de secciones. El resto de la app consume estos tokens; nadie más declara colores.
package com.unistack.app.core.design.theme

import androidx.compose.material3.ColorScheme
import com.unistack.app.feature_user.domain.ColorBlindPalette
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
    primary = Color(0xFF6C4DFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE6DFFF),
    onPrimaryContainer = Color(0xFF1C0090),

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

    background = Color(0xFFFBFAFF),
    onBackground = Color(0xFF1A1A21),
    surface = Color(0xFFFBFAFF),
    onSurface = Color(0xFF1A1A21),
    surfaceVariant = Color(0xFFE3E1EE),
    onSurfaceVariant = Color(0xFF46454F),

    // Los cinco niveles se recalculan desde el fondo nuevo: son escalones de profundidad, y
    // si el fondo se mueve y ellos no, el de más abajo deja de estar por debajo de nada.
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F4FC),
    surfaceContainer = Color(0xFFEFEEF8),
    surfaceContainerHigh = Color(0xFFE9E7F3),
    surfaceContainerHighest = Color(0xFFE3E1EE),

    outline = Color(0xFF78757F),
    outlineVariant = Color(0xFFCAC7D6),
    scrim = Color(0xFF000000)
)

/**
 * Esquema oscuro.
 *
 * El fondo dejó de ser un negro casi puro (#131218) y pasó a un gris azulado oscuro (#1E1D2B),
 * elegido a mano el 21 ago 2026. Contradice la nota anterior —que defendía un negro con sesgo
 * violeta para que los subtonos no pelearan con el acento morado— y se deja escrito el cambio
 * en vez de borrarlo: el argumento no era falso, pero un fondo más alto separa mejor las
 * tarjetas del suelo, y eso pesa más.
 *
 * Quien quiera el negro de antes tiene el modo OLED, que lleva el fondo a negro puro.
 */
internal val ExpressiveDarkScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFCFC2FF),
    onPrimary = Color(0xFF33208A),
    primaryContainer = Color(0xFF4B36A8),
    onPrimaryContainer = Color(0xFFE9E2FF),

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

    /*
     * El suelo del modo oscuro, con sesgo azul y no morado.
     *
     * Estaba en #1E1D2B, un gris tirando a violeta que tenia bastante color propio: sobre el,
     * cualquier acento se leia como una variacion del fondo en vez de destacar sobre el. Este
     * es casi negro y su tinte va hacia el azul, que es el que ya usan los acentos de la app.
     *
     * Los cinco escalones bajan con el suelo. Si se quedaran donde estaban, tres de los cinco
     * caerian por encima de lo que les toca y las tarjetas se verian flotando.
     */
    background = Color(0xFF0A0C11),
    onBackground = Color(0xFFE8EBF3),
    surface = Color(0xFF0A0C11),
    onSurface = Color(0xFFE8EBF3),
    surfaceVariant = Color(0xFF262E3B),
    onSurfaceVariant = Color(0xFF98A2B7),

    surfaceContainerLowest = Color(0xFF07090E),
    surfaceContainerLow = Color(0xFF12161D),
    surfaceContainer = Color(0xFF171C24),
    surfaceContainerHigh = Color(0xFF1C222D),
    surfaceContainerHighest = Color(0xFF232B37),

    outline = Color(0xFF6C7689),
    outlineVariant = Color(0xFF262E3B),
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
            // Los cuatro tonos elegidos a mano se ven en tema oscuro, que es donde se
            // juzgaron. Aquí van sus equivalentes oscurecidos: el mismo tono, la luz que un
            // texto necesita sobre fondo claro. #4797FF sobre blanco da 3,1 de contraste y
            // #11C045 da 2,4 — por debajo del 4,5 que pide leerse sin esfuerzo.
            schedule = Color(0xFF0A63D6),
            onSchedule = Color(0xFFFFFFFF),
            scheduleContainer = Color(0xFFD9E5FF),
            onScheduleContainer = Color(0xFF001A47),

            expenses = Color(0xFFD81C00),
            onExpenses = Color(0xFFFFFFFF),
            expensesContainer = Color(0xFFFFDCD5),
            onExpensesContainer = Color(0xFF410800),

            onTrack = Color(0xFF0A8A31),
            onOnTrack = Color(0xFFFFFFFF),
            onTrackContainer = Color(0xFFB4F2C4),
            onOnTrackContainer = Color(0xFF00230D),

            atRisk = Color(0xFF8C6800),
            onAtRisk = Color(0xFFFFFFFF),
            atRiskContainer = Color(0xFFFFE29E),
            onAtRiskContainer = Color(0xFF2A1D00)
        )

        val Dark = SectionColors(
            // Aqui van los tonos elegidos a mano, que es el tema en el que se juzgaron.
            //
            // Dos excepciones, y se dicen para que se puedan revertir a sabiendas. El rojo de
            // Gastos se eligio en #FF2200, que sobre este fondo da 3,5 de contraste: se aclara
            // a #FF5340 para llegar al 4,5 que necesita leerse como texto. Y el ambar se eligio
            // en #8C6800, que es un tono de tema claro —sobre fondo oscuro da 2,4—, asi que
            // aqui va su version clara con el mismo tono.
            schedule = Color(0xFF4797FF),
            onSchedule = Color(0xFF002A5E),
            scheduleContainer = Color(0xFF0B4699),
            onScheduleContainer = Color(0xFFD9E5FF),

            expenses = Color(0xFFFF5340),
            onExpenses = Color(0xFF5A0F00),
            expensesContainer = Color(0xFF8C1F0A),
            onExpensesContainer = Color(0xFFFFDCD5),

            onTrack = Color(0xFF11C045),
            onOnTrack = Color(0xFF00320F),
            onTrackContainer = Color(0xFF0A5C23),
            onOnTrackContainer = Color(0xFFB4F2C4),

            atRisk = Color(0xFFE0A400),
            onAtRisk = Color(0xFF3A2900),
            atRiskContainer = Color(0xFF6B4E00),
            onAtRiskContainer = Color(0xFFFFE29E)
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
internal fun expressiveTypography(
    base: Typography = Typography(),
    negrita: Boolean = false
): Typography {
    val normalWeight = if (negrita) FontWeight.SemiBold else FontWeight.Normal
    val mediumWeight = if (negrita) FontWeight.Bold else FontWeight.Medium
    val boldWeight = if (negrita) FontWeight.ExtraBold else FontWeight.Bold

    return base.copy(
        displayLarge = base.displayLarge.copy(fontSize = 57.sp, lineHeight = 64.sp, fontWeight = normalWeight),
        displayLargeEmphasized = base.displayLarge.copy(fontSize = 57.sp, lineHeight = 64.sp, fontWeight = boldWeight),

        displayMedium = base.displayMedium.copy(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = normalWeight),
        displayMediumEmphasized = base.displayMedium.copy(fontSize = 45.sp, lineHeight = 52.sp, fontWeight = boldWeight),

        displaySmall = base.displaySmall.copy(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = normalWeight),
        displaySmallEmphasized = base.displaySmall.copy(fontSize = 36.sp, lineHeight = 44.sp, fontWeight = boldWeight),

        headlineLarge = base.headlineLarge.copy(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = normalWeight),
        headlineLargeEmphasized = base.headlineLarge.copy(fontSize = 32.sp, lineHeight = 40.sp, fontWeight = boldWeight),

        headlineMedium = base.headlineMedium.copy(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = normalWeight),
        headlineMediumEmphasized = base.headlineMedium.copy(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = boldWeight),

        headlineSmall = base.headlineSmall.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = normalWeight),
        headlineSmallEmphasized = base.headlineSmall.copy(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = boldWeight),

        titleLarge = base.titleLarge.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = normalWeight),
        titleLargeEmphasized = base.titleLarge.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = mediumWeight),

        titleMedium = base.titleMedium.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = mediumWeight),
        titleMediumEmphasized = base.titleMedium.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = boldWeight),

        titleSmall = base.titleSmall.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = mediumWeight),
        titleSmallEmphasized = base.titleSmall.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = boldWeight),

        bodyLarge = base.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = normalWeight),
        bodyLargeEmphasized = base.bodyLarge.copy(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = mediumWeight),

        bodyMedium = base.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = normalWeight),
        bodyMediumEmphasized = base.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = mediumWeight),

        bodySmall = base.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = normalWeight),
        bodySmallEmphasized = base.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = mediumWeight),

        labelLarge = base.labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = mediumWeight),
        labelLargeEmphasized = base.labelLarge.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = boldWeight),

        labelMedium = base.labelMedium.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = mediumWeight),
        labelMediumEmphasized = base.labelMedium.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = boldWeight),

        labelSmall = base.labelSmall.copy(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = mediumWeight),
        labelSmallEmphasized = base.labelSmall.copy(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = boldWeight)
    )
}

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

/**
 * Los seis acentos vivos, para rellenos que no llevan texto encima.
 *
 * Los tonos de sección son colores de *contenido*: tienen que leerse como texto sobre el fondo
 * del tema, así que en oscuro son claros y lavados y en claro son oscuros y sordos. Eso está
 * bien para una palabra o un icono, y mal para un tramo de barra o un cuadro de color, donde lo
 * único que hacen es parecer apagados —que es exactamente la queja.
 *
 * Estos seis son las mismas familias de tono con la saturación que un relleno sí puede
 * permitirse, y son los mismos en tema claro y oscuro: un relleno de color no cambia de identidad
 * porque cambie el fondo, y así una barra se lee igual en los dos.
 *
 * **No se pone texto pequeño encima de ellos.** Para un número corto y en negrita sobre el
 * relleno, [inkOn] tiene contraste de sobra; para un párrafo, no los uses.
 */
@androidx.compose.runtime.Immutable
data class VividAccents(
    val violet: Color,
    val blue: Color,
    val green: Color,
    val amber: Color,
    val coral: Color,
    val pink: Color
) {
    /**
     * La tinta que se lee sobre un relleno concreto.
     *
     * Era un blanco fijo, y funcionaba mientras los seis fueron oscuros. Con el ambar elegido
     * a mano —#F39912— el blanco encima da 2,2 de contraste: el numero del cuadro de peso se
     * volvia ilegible justo en uno de los seis. [contentColorOn] elige tinta clara u oscura por
     * el contraste real de cada uno, asi que la paleta puede cambiar sin arrastrar este problema.
     */
    fun inkOn(fill: Color): Color = contentColorOn(fill)

    /** En orden, para lo que necesite repartir colores sin elegirlos: tramos, cuadros, chips. */
    val ordered: List<Color> get() = listOf(violet, blue, green, amber, coral, pink)

    companion object {
        val Default = VividAccents(
            violet = Color(0xFF6A45E8),
            blue = Color(0xFF1F6FE0),
            green = Color(0xFF0DBA41),
            amber = Color(0xFFF39912),
            coral = Color(0xFFF3311B),
            pink = Color(0xFFD62884)
        )
    }
}

val LocalVividAccents = androidx.compose.runtime.staticCompositionLocalOf { VividAccents.Default }

/**
 * Los colores de los iconos de Ajustes, cuando van de colores: uno por lo que hace cada puerta.
 *
 * Se pidieron el 16 sep 2026 «diferentes cada cosa, alusivos a lo que hacen»: hasta entonces
 * salían de los colores de sección y del acento, y de ocho filas del centro cinco eran morado o
 * verde. Aquí cada tono tiene un significado y se reparte por él —la campana en ámbar, la nube
 * en cian, lo que borra en rojo—, no por turno.
 *
 * Van en dos juegos porque el icono se pinta con el color del fondo encima del relleno: en
 * oscuro el relleno es claro y el icono oscuro, y en claro al revés. Por eso los de la cara
 * clara son tonos hondos y los de la oscura, tonos altos del mismo color.
 */
@androidx.compose.runtime.Immutable
data class TonosDeAjustes(
    val rojo: Color,
    val naranja: Color,
    val ambar: Color,
    val verde: Color,
    val turquesa: Color,
    val cian: Color,
    val azul: Color,
    val indigo: Color,
    val violeta: Color,
    val rosa: Color,
    val gris: Color
) {
    companion object {
        val Oscuro = TonosDeAjustes(
            rojo = Color(0xFFFF6B5E),
            naranja = Color(0xFFFF9A4D),
            ambar = Color(0xFFF2B632),
            verde = Color(0xFF3FD17A),
            turquesa = Color(0xFF2FD1BE),
            cian = Color(0xFF3CC8F0),
            azul = Color(0xFF5B9DFF),
            indigo = Color(0xFF8A93FF),
            violeta = Color(0xFFB18CFF),
            rosa = Color(0xFFF27CC0),
            gris = Color(0xFFA3AEC2)
        )

        val Claro = TonosDeAjustes(
            rojo = Color(0xFFC8321F),
            naranja = Color(0xFFB85A00),
            ambar = Color(0xFF8C6800),
            verde = Color(0xFF0A8A31),
            turquesa = Color(0xFF00897B),
            cian = Color(0xFF00799C),
            azul = Color(0xFF0A63D6),
            indigo = Color(0xFF4553C8),
            violeta = Color(0xFF7446D6),
            rosa = Color(0xFFB8307A),
            gris = Color(0xFF5B6678)
        )

        fun para(oscuro: Boolean): TonosDeAjustes = if (oscuro) Oscuro else Claro
    }
}

/** Los tonos de Ajustes de la cara en curso, clara u oscura. */
val tonosDeAjustes: TonosDeAjustes
    @androidx.compose.runtime.Composable
    @androidx.compose.runtime.ReadOnlyComposable
    get() = TonosDeAjustes.para(LocalIsDarkTheme.current)

/**
 * Los mismos colores de seccion, sin el verde y el rojo.
 *
 * Horario y Gastos se quedan como estan —son identidad de seccion, no un juicio— y lo que se
 * apaga es el par que dice «bien o mal»: al dia y en riesgo pasan al acento y a un tono mas
 * apagado del mismo, que se distinguen por claridad y no por tono.
 */
internal fun SectionColors.sinSemaforo(esquema: ColorScheme): SectionColors = copy(
    onTrack = esquema.primary,
    onOnTrack = esquema.onPrimary,
    onTrackContainer = esquema.primaryContainer,
    onOnTrackContainer = esquema.onPrimaryContainer,
    atRisk = esquema.tertiary,
    onAtRisk = esquema.onTertiary,
    atRiskContainer = esquema.tertiaryContainer,
    onAtRiskContainer = esquema.onTertiaryContainer
)

/**
 * El par «al dia / en riesgo», en una paleta que no dependa del verde y el rojo.
 *
 * Es el unico sitio donde hay que cambiarlo: los dos colores viajan por toda la app desde
 * [SectionColors], asi que cambiarlos aqui los cambia en las once pantallas que dicen «bien o
 * mal» sin tocar ninguna.
 */
internal fun SectionColors.conPaleta(paleta: ColorBlindPalette): SectionColors = when (paleta) {
    ColorBlindPalette.NINGUNA -> this
    // Sin canal verde, el par que mas se separa es azul-naranja.
    ColorBlindPalette.DEUTERANOPIA -> copy(
        onTrack = Color(0xFF3A7DE0),
        onTrackContainer = Color(0xFF12233F),
        onOnTrackContainer = Color(0xFFBBD4FF),
        atRisk = Color(0xFFE0A63C),
        atRiskContainer = Color(0xFF3A2A08),
        onAtRiskContainer = Color(0xFFFFE2AC)
    )
    // Sin canal azul, el verde se mueve hacia el magenta y el rojo se queda donde esta.
    ColorBlindPalette.TRITANOPIA -> copy(
        onTrack = Color(0xFF12B0A0),
        onTrackContainer = Color(0xFF00312C),
        onOnTrackContainer = Color(0xFFA8F0E7),
        atRisk = Color(0xFFE0567F),
        atRiskContainer = Color(0xFF3D0A1C),
        onAtRiskContainer = Color(0xFFFFD9E2)
    )
}
