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
import com.unistack.app.feature_user.domain.HapticStrength
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.core.utils.HapticRuntime
import com.unistack.app.feature_user.domain.LineHeightStyle
import com.unistack.app.feature_user.domain.ReadingFont
import com.unistack.app.feature_user.domain.ContrastLevel
import com.unistack.app.feature_user.domain.CornerStyle
import com.unistack.app.feature_user.domain.SurfaceStyle
import com.unistack.app.feature_user.domain.AccentIntensity
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
    // La fuerza de vibracion sale de aqui hacia `performSafely`, que se llama desde onClick y
    // no puede leer un CompositionLocal. Con el movimiento apagado del todo, tampoco vibra.
    HapticRuntime.strength = if (maxOf(appearance.motionPreference, accessibility.motionPreference) == MotionPreference.NONE) {
        HapticStrength.NINGUNA
    } else {
        appearance.motion.haptics
    }

    val effectiveAppearance = if (accessibility.reduceTransparency && appearance.surfaceStyle == SurfaceStyle.TRANSLUCENT) {
        appearance.copy(surfaceStyle = SurfaceStyle.OUTLINED)
    } else {
        appearance
    }

    val scheme = expressiveColorScheme(darkTheme = darkTheme, oledTheme = oledTheme, appearance = effectiveAppearance)
        .conContraste(accessibility.contrast, accessibility.highContrastEnabled, darkTheme)
    /*
     * Verde, ambar y rojo, o todo del color de acento.
     *
     * Apagado, la app deja de decir «bien o mal» con el color y lo dice solo con el numero y
     * con la posicion. Es lo que pide quien no distingue ese par: sin esto, media app le
     * cuenta las cosas en un idioma que no lee.
     */
    val sections = SectionColors.forTheme(darkTheme)
        .conPaleta(accessibility.colorBlindPalette)
        .let { base -> if (appearance.sectionColorsEnabled) base else base.sinSemaforo(scheme) }

    /*
     * La letra sale de dos sitios, y Accesibilidad manda.
     *
     * La familia de lectura para dislexia y la negrita son ajustes de accesibilidad, asi que
     * pisan a la familia elegida en Apariencia: quien las necesita las necesita, y no tiene
     * sentido que elegir «Mono» en Tipografia las anule sin decirlo.
     */
    /*
     * El interlineado se aplica **al final**, y ahi esta el arreglo.
     *
     * Se aplicaba dentro de `appearanceTypography`, y justo despues `expressiveTypography`
     * reescribia el `lineHeight` de los diecisiete estilos con su valor fijo en sp: el ajuste
     * se guardaba, se elegia y no cambiaba un renglon. Ahora el orden es al reves —primero la
     * familia y el peso, luego los tamanos de M3E, y el aire al final— asi que ya no hay nada
     * despues que lo pise.
     */
    val typography = expressiveTypography(
        base = appearanceTypography(
            estilo = if (accessibility.readingFont == ReadingFont.DISLEXIA) {
                TypographyStyle.SYSTEM
            } else {
                appearance.typographyStyle
            },
            negrita = accessibility.boldText
        ),
        negrita = accessibility.boldText
    ).conInterlineado(
        if (accessibility.readingFont == ReadingFont.DISLEXIA) {
            // La dislexia pide aire entre renglones ademas de letra abierta: las dos cosas
            // juntas es lo que hace que un parrafo deje de saltar de linea.
            LineHeightStyle.AMPLIO
        } else {
            appearance.lineHeightStyle
        }
    )

    // La preferencia de "texto grande" se aplica sobre el fontScale de la densidad, no
    // sobre los estilos de tipografía. Así la respetan TODAS las medidas en sp de la app,
    // incluidas las que se declaran sueltas en las pantallas; escalando solo la Typography,
    // cualquier `fontSize = 13.sp` se saltaba el ajuste.
    val density = LocalDensity.current
    /*
     * El tamano del texto sale del porcentaje de Apariencia, y la preferencia vieja de
     * Accesibilidad sigue sumando si estaba puesta.
     *
     * Los dos existen porque el porcentaje es nuevo: quien tuviera «grande» guardado de antes
     * lo conserva sin tener que volver a elegirlo, y quien toque el deslizador manda.
     */
    val textScale = (appearance.textScalePercent.coerceIn(85, 135) / 100f) *
        if (accessibility.textScale == TextScalePreference.LARGE) 1.10f else 1f

    CompositionLocalProvider(
        LocalSectionColors provides sections,
        LocalVividAccents provides VividAccents.Default,
        LocalIsDarkTheme provides darkTheme,
        LocalAppearancePreferences provides effectiveAppearance,
        LocalAccessibilityPreferences provides accessibility,
        LocalMotionDurationScale provides accessibility.motionScale(),
        LocalMotion provides appearance.motion,
        // El permiso sale del mas restrictivo de los dos: Accesibilidad puede quitar el
        // movimiento de toda la app aunque Apariencia lo tenga completo, y al reves.
        // maxOf y no minOf: el orden del enum va de completo a nada, asi que el mayor de los
        // dos es el mas restrictivo.
        LocalMotionAllowance provides maxOf(appearance.motionPreference, accessibility.motionPreference),
        LocalInterfaceSpacing provides appearance.interfaceSpacing(),
        LocalDensity provides Density(
            density = density.density,
            fontScale = density.fontScale * textScale
        )
    ) {
        MaterialExpressiveTheme(
            colorScheme = scheme,
            motionScheme = MotionScheme.expressive(),
            shapes = escalaDeFormas(appearance.cornerStyle),
            typography = typography,
            content = content
        )
    }
}

/**
 * La escala de formas, ahora sí atada al ajuste de esquinas.
 *
 * **Estuvo fija.** El comentario que había aquí decía que no depender de un ajuste era la
 * decisión —tres variantes multiplicaban los estados que revisar por pantalla—, pero el ajuste
 * existía igual: se elegía «Rectas» en Apariencia, se guardaba, viajaba en la copia de
 * seguridad y las tarjetas seguían con los mismos 28dp. Un ajuste que no hace nada cuesta más
 * que tres variantes que sí.
 *
 * Los cinco tamaños se mueven juntos y guardando la proporción: si el contenedor grande baja a
 * 14dp y el pequeño se queda en 12dp, un botón dentro de una tarjeta se ve más redondo que la
 * tarjeta que lo contiene.
 */
internal val ExpressiveShapeScale = escalaDeFormas(CornerStyle.BALANCED)

internal fun escalaDeFormas(estilo: CornerStyle): Shapes = when (estilo) {
    CornerStyle.COMPACT -> Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(6.dp),
        medium = RoundedCornerShape(10.dp),
        large = RoundedCornerShape(14.dp),
        extraLarge = RoundedCornerShape(16.dp)
    )
    CornerStyle.BALANCED -> Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(28.dp),
        extraLarge = RoundedCornerShape(32.dp)
    )
    CornerStyle.SOFT -> Shapes(
        extraSmall = RoundedCornerShape(12.dp),
        small = RoundedCornerShape(18.dp),
        medium = RoundedCornerShape(28.dp),
        large = RoundedCornerShape(36.dp),
        extraLarge = RoundedCornerShape(42.dp)
    )
}

/**
 * El esquema que toca: el tema elegido, en la cara que pida el modo.
 *
 * **Aqui estaba el fallo de los modos.** El tema pintaba una sola paleta encima del esquema
 * base, y como veinticuatro de los veintiocho son oscuros, elegir «Claro» daba el tema oscuro
 * igual: el modo parecia no hacer nada. Solo OLED se notaba, porque ese pinta el negro
 * *despues* de todo lo demas. Ahora cada tema tiene su cara clara y su cara oscura, y el modo
 * dice cual de las dos.
 *
 * Monet ya no esta. Se quito entero —no escondido tras un ajuste— porque con veintiocho temas
 * y un acento a medida, tomar prestada la paleta del fondo de pantalla no anade nada y si
 * quitaba: mientras estuvo encendido, los temas quedaban en pausa y la mitad de esta pantalla
 * no pintaba.
 */
@Composable
private fun expressiveColorScheme(
    darkTheme: Boolean,
    oledTheme: Boolean,
    appearance: AppearancePreferences
): ColorScheme {
    val base = if (darkTheme) ExpressiveDarkScheme else ExpressiveLightScheme

    /*
     * El tema elegido se pinta encima del esquema base.
     *
     * El tema decide fondo, tarjetas, tinta y acento a la vez -- que es lo que hace que
     * «Dracula» se vea como Dracula y no como la app de siempre con un morado distinto.
     */
    val conTema = run {
        val tema = AppThemes.byId(appearance.themeId).palette(darkTheme)
        val acento = appearance.accentIntensity.aplicarA(
            appearance.customAccentColor?.let(::Color) ?: tema.accent
        )
        base.copy(
            primary = acento,
            onPrimary = tema.onAccent,
            primaryContainer = acento.copy(alpha = 0.22f).compuestoSobre(tema.background),
            onPrimaryContainer = if (!darkTheme) tema.ink else acento,
            secondary = acento,
            /*
             * La pastilla de la barra de abajo sale de aqui.
             *
             * `ShortNavigationBarItem` pinta su indicador con `secondaryContainer`, y ese se
             * quedaba con el valor del esquema base: la barra marcaba la pestana activa con un
             * color que no era el del tema ni el del acento, y no habia forma de entender de
             * donde salia. Ahora es el acento rebajado sobre el fondo, que es lo que hace que
             * se lea como «lo elegido» en el mismo idioma que el resto de la app.
             */
            secondaryContainer = acento.copy(alpha = 0.26f).compuestoSobre(tema.background),
            onSecondaryContainer = acento,
            tertiary = acento,
            background = tema.background,
            onBackground = tema.ink,
            surface = tema.background,
            onSurface = tema.ink,
            onSurfaceVariant = tema.ink.copy(alpha = 0.66f).compuestoSobre(tema.background),
            surfaceContainerLowest = tema.background.mezclaCon(tema.surface, 0.25f),
            surfaceContainerLow = tema.background.mezclaCon(tema.surface, 0.6f),
            surfaceContainer = tema.surface,
            surfaceContainerHigh = tema.surface.mezclaCon(tema.ink, 0.07f),
            surfaceContainerHighest = tema.surface.mezclaCon(tema.ink, 0.13f),
            surfaceVariant = tema.surface,
            outline = tema.ink.copy(alpha = 0.34f).compuestoSobre(tema.background),
            outlineVariant = tema.ink.copy(alpha = 0.16f).compuestoSobre(tema.background)
        ).conSuperficie(appearance.surfaceStyle, tema)
    }

    // OLED apaga el píxel: el fondo y el contenedor más bajo van a negro puro, y el resto de
    // los niveles se conservan para que la jerarquía de profundidad no se venga abajo.
    // design-tokens-ok-begin: el negro puro ES el modo OLED, no un color de marca
    return if (oledTheme && darkTheme) {
        conTema.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerLowest = Color.Black
        )
    } else {
        conTema
    }
    // design-tokens-ok-end
}

/**
 * Dos colores mezclados, para derivar los niveles de superficie de un tema.
 *
 * Un tema declara solo fondo y tarjeta; Material necesita cinco niveles entre medias. Se
 * interpolan en vez de pedirlos uno a uno: veintiocho temas x cinco niveles serian ciento
 * cuarenta colores escritos a mano, y bastaria con equivocarse en uno para que una pantalla
 * quedara ilegible.
 */
internal fun Color.mezclaCon(otro: Color, fraccion: Float): Color = Color(
    red = red + (otro.red - red) * fraccion,
    green = green + (otro.green - green) * fraccion,
    blue = blue + (otro.blue - blue) * fraccion,
    alpha = 1f
)

/** El mismo color, ya resuelto sobre un fondo opaco: Material no admite transparencias aqui. */
internal fun Color.compuestoSobre(fondo: Color): Color = Color(
    red = fondo.red + (red - fondo.red) * alpha,
    green = fondo.green + (green - fondo.green) * alpha,
    blue = fondo.blue + (blue - fondo.blue) * alpha,
    alpha = 1f
)

/**
 * Lo que hace la intensidad del acento, que hasta ahora no hacia nada.
 *
 * Suave lo acerca al blanco y vivo lo satura acercandolo al negro; equilibrado lo deja como
 * viene. Es un ajuste de un solo color y por eso vive con el color, no en la pantalla.
 */
internal fun AccentIntensity.aplicarA(color: Color): Color = when (this) {
    // Los recorridos son amplios a proposito: con un 15% el cambio existia pero no se veia,
    // que para el caso es lo mismo que no hacer nada.
    AccentIntensity.SOFT -> color.mezclaCon(Color.White, 0.42f)
    AccentIntensity.BALANCED -> color
    AccentIntensity.VIBRANT -> color.saturado(0.55f)
}

/**
 * El mismo color, mas vivo: se aleja de su propio gris manteniendo el tono.
 *
 * Oscurecerlo mezclando con negro apagaba el acento en vez de encenderlo, que es lo contrario
 * de lo que promete «vivo».
 */
internal fun Color.saturado(fuerza: Float): Color {
    val gris = (red + green + blue) / 3f
    return Color(
        red = (gris + (red - gris) * (1f + fuerza)).coerceIn(0f, 1f),
        green = (gris + (green - gris) * (1f + fuerza)).coerceIn(0f, 1f),
        blue = (gris + (blue - gris) * (1f + fuerza)).coerceIn(0f, 1f),
        alpha = 1f
    )
}

/**
 * Lo que hace el estilo de superficie, que hasta ahora **no hacia nada**.
 *
 * Se guardaba, viajaba en la copia de seguridad y salia en Apariencia; elegir «plana» o «con
 * sombra» daba el mismo pixel. Cada estilo cambia de donde sale la separacion entre una
 * tarjeta y el fondo:
 *
 * - **Plana**: el nivel de tarjeta se acerca al fondo y el filete desaparece. Solo el tono.
 * - **Filete**: la tarjeta se funde con el fondo y lo que la delimita es el contorno.
 * - **Sombra**: la tarjeta se aleja del fondo en tono; la sombra la ponen los componentes.
 * - **Cristal**: nivel intermedio y contorno tenue, para que se lea como algo translucido.
 */
internal fun ColorScheme.conSuperficie(estilo: SurfaceStyle, tema: ThemePalette): ColorScheme = when (estilo) {
    SurfaceStyle.FLAT -> copy(
        surfaceContainer = tema.background.mezclaCon(tema.surface, 0.55f),
        surfaceContainerHigh = tema.background.mezclaCon(tema.surface, 0.75f),
        outlineVariant = tema.background.mezclaCon(tema.ink, 0.05f)
    )
    // El grosor del filete no se puede meter en un ColorScheme, asi que lo que se ajusta aqui
    // es su contraste: un filete «grueso» que ademas se ve mas oscuro se lee como mas grueso.
    SurfaceStyle.OUTLINED -> copy(
        surfaceContainer = tema.background.mezclaCon(tema.surface, 0.35f),
        surfaceContainerHigh = tema.background.mezclaCon(tema.surface, 0.6f),
        outlineVariant = tema.ink.copy(alpha = 0.28f).compuestoSobre(tema.background)
    )
    SurfaceStyle.ELEVATED -> copy(
        surfaceContainer = tema.surface.mezclaCon(tema.ink, 0.04f),
        surfaceContainerHigh = tema.surface.mezclaCon(tema.ink, 0.10f),
        outlineVariant = tema.ink.copy(alpha = 0.10f).compuestoSobre(tema.background)
    )
    SurfaceStyle.TRANSLUCENT -> copy(
        surfaceContainer = tema.background.mezclaCon(tema.surface, 0.7f),
        surfaceContainerHigh = tema.background.mezclaCon(tema.surface, 0.85f),
        outlineVariant = tema.ink.copy(alpha = 0.20f).compuestoSobre(tema.background)
    )
}

/**
 * El contraste, subido a lo que se haya pedido.
 *
 * «Alto contraste» era un interruptor que se guardaba y no cambiaba nada, y ahora es el escalon
 * del medio de un ajuste de tres. Lo que sube no es el color de acento sino **la tinta**: el
 * texto se acerca al blanco o al negro puro y los contornos se marcan, que es lo que hace que
 * un parrafo se lea con sol de frente.
 */
internal fun ColorScheme.conContraste(
    nivel: ContrastLevel,
    altoContrasteViejo: Boolean,
    oscuro: Boolean
): ColorScheme {
    // El interruptor de antes sigue contando: quien lo tuviera puesto se queda en «alto» sin
    // tener que volver a elegirlo.
    val efectivo = if (altoContrasteViejo && nivel == ContrastLevel.ESTANDAR) ContrastLevel.ALTO else nivel
    if (efectivo == ContrastLevel.ESTANDAR) return this
    val extremo = if (oscuro) Color.White else Color.Black
    val fuerza = if (efectivo == ContrastLevel.ALTO) 0.45f else 1f
    return copy(
        onSurface = onSurface.mezclaCon(extremo, fuerza),
        onSurfaceVariant = onSurfaceVariant.mezclaCon(extremo, fuerza * 0.8f),
        onBackground = onBackground.mezclaCon(extremo, fuerza),
        outline = outline.mezclaCon(extremo, fuerza * 0.6f),
        outlineVariant = outlineVariant.mezclaCon(extremo, fuerza * 0.6f)
    )
}
