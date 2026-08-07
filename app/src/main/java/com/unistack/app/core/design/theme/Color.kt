// design-tokens-exempt: este archivo ES la definición del sistema de color.
// Aquí viven las paletas base y la matemática de contraste y mezcla; el resto de la app
// debe consumir estos tokens en lugar de declarar colores propios.
package com.unistack.app.core.design.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import com.unistack.app.feature_user.domain.AccentIntensity
import com.unistack.app.feature_user.domain.AccentStyle
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.BackgroundStyle
import com.unistack.app.feature_user.domain.SurfaceStyle

/**
 * Roles tonales tomados del esquema dinámico del sistema (Material You / Monet).
 *
 * Se leen tal cual del [androidx.compose.material3.ColorScheme] en vez de derivarlos con
 * mezclas: fabricar el "container" mezclando el acento hacia el fondo daba un tono
 * grisáceo, no el pastel característico de Monet.
 */
data class DynamicAccent(
    val primary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color
)

object UniStackColors {
    /** Tinta oscura para contenido sobre superficies claras (más suave que el negro puro). */
    private val DarkInk = Color(0xFF171427)
    private const val DARK_INK_LUMINANCE = 0.0136f

    private val lightPalette = UniStackColorPalette(
        primary = Color(0xFF6750F5),
        primaryDark = Color(0xFF2E1A78),
        primaryLight = Color(0xFFE9DDFF),
        blue = Color(0xFF1E7BEA),
        blueLight = Color(0xFFDDEBFF),
        teal = Color(0xFF00AFA5),
        tealLight = Color(0xFFE0F8F5),
        green = Color(0xFF4CAF50),
        greenLight = Color(0xFFE8F6E8),
        coral = Color(0xFFEF5B4D),
        coralLight = Color(0xFFFFE1DC),
        yellow = Color(0xFFE2A900),
        yellowLight = Color(0xFFFFF4CC),
        background = Color(0xFFFCFBFF),
        card = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF4F0FA),
        textPrimary = Color(0xFF171427),
        textSecondary = Color(0xFF5F5B6B),
        softOutline = Color(0xFFDCD2EA),
        gradientEnd = Color(0xFFFFFFFF),
        bottomBar = Color(0xFFFFFCFF),
        bottomBarSelected = Color(0xFFF0EAFF)
    )

    private val darkPalette = UniStackColorPalette(
        primary = Color(0xFF8F35FF),
        primaryDark = Color(0xFFF1E7FF),
        primaryLight = Color(0xFF24105C),
        blue = Color(0xFF8F35FF),
        blueLight = Color(0xFF1D1048),
        teal = Color(0xFF00E0B8),
        tealLight = Color(0xFF063D35),
        green = Color(0xFF74C981),
        greenLight = Color(0xFF193420),
        coral = Color(0xFFFF3348),
        coralLight = Color(0xFF421522),
        yellow = Color(0xFFFFB800),
        yellowLight = Color(0xFF4A3308),
        background = Color(0xFF070B14),
        card = Color(0xFF080D17),
        surfaceVariant = Color(0xFF0B111D),
        textPrimary = Color(0xFFF8F4FF),
        textSecondary = Color(0xFFD3D0E0),
        softOutline = Color(0xFF1A2230),
        gradientEnd = Color(0xFF000309),
        bottomBar = Color(0xFF050913),
        bottomBarSelected = Color(0xFF24105C)
    )

    private var appliedSignature: Int? = null

    var IsDarkTheme by mutableStateOf(false)
        private set
    var Primary by mutableStateOf(lightPalette.primary)
        private set
    var PrimaryDark by mutableStateOf(lightPalette.primaryDark)
        private set
    var PrimaryLight by mutableStateOf(lightPalette.primaryLight)
        private set
    var Blue by mutableStateOf(lightPalette.blue)
        private set
    var BlueLight by mutableStateOf(lightPalette.blueLight)
        private set
    var Teal by mutableStateOf(lightPalette.teal)
        private set
    var TealLight by mutableStateOf(lightPalette.tealLight)
        private set
    var Green by mutableStateOf(lightPalette.green)
        private set
    var GreenLight by mutableStateOf(lightPalette.greenLight)
        private set
    var Coral by mutableStateOf(lightPalette.coral)
        private set
    var CoralLight by mutableStateOf(lightPalette.coralLight)
        private set
    var Yellow by mutableStateOf(lightPalette.yellow)
        private set
    var YellowLight by mutableStateOf(lightPalette.yellowLight)
        private set
    var Background by mutableStateOf(lightPalette.background)
        private set
    var Card by mutableStateOf(lightPalette.card)
        private set
    var SurfaceVariant by mutableStateOf(lightPalette.surfaceVariant)
        private set
    var TextPrimary by mutableStateOf(lightPalette.textPrimary)
        private set
    var TextSecondary by mutableStateOf(lightPalette.textSecondary)
        private set
    var SoftOutline by mutableStateOf(lightPalette.softOutline)
        private set
    var GradientEnd by mutableStateOf(lightPalette.gradientEnd)
        private set
    var BottomBar by mutableStateOf(lightPalette.bottomBar)
        private set
    var BottomBarSelected by mutableStateOf(lightPalette.bottomBarSelected)
        private set

    /** Contenido legible sobre [Primary]. Nunca asumas blanco: con Monet puede ser tinta oscura. */
    var OnPrimary by mutableStateOf(Color.White)
        private set

    /** Contenido legible sobre [PrimaryLight]. */
    var OnPrimaryContainer by mutableStateOf(lightPalette.primaryDark)
        private set

    /**
     * Base para los velos de diálogos y hojas modales. Aplica la opacidad en el punto de
     * uso: `UniStackColors.Scrim.copy(alpha = 0.62f)`.
     *
     * Es negro en ambos temas a propósito: un velo oscurece lo que hay detrás, y aclararlo
     * en tema oscuro rompería la jerarquía de profundidad.
     */
    val Scrim: Color = Color.Black

    /**
     * Color de contenido legible sobre [background], eligiendo entre tinta clara y oscura
     * por ratio de contraste WCAG.
     *
     * Úsalo en lugar de `Color.White` sobre cualquier superficie de color: el blanco fijo
     * deja de ser legible en cuanto el acento es claro (por ejemplo el primary pastel que
     * Monet entrega en modo oscuro, o un color de materia elegido por el usuario).
     */
    fun contentColorOn(background: Color): Color {
        val luminance = relativeLuminance(background)
        val contrastWithLight = 1.05f / (luminance + 0.05f)
        val contrastWithDark = (luminance + 0.05f) / (DARK_INK_LUMINANCE + 0.05f)
        return if (contrastWithLight >= contrastWithDark) Color.White else DarkInk
    }

    internal fun applyTheme(
        darkTheme: Boolean,
        oledTheme: Boolean,
        appearance: AppearancePreferences,
        highContrast: Boolean = false,
        dynamicAccent: DynamicAccent? = null
    ) {
        val normalized = appearance.normalized()
        val signature = 31 * (31 * (31 * (31 * darkTheme.hashCode() + oledTheme.hashCode()) + normalized.hashCode()) +
            highContrast.hashCode()) + dynamicAccent.hashCode()
        if (appliedSignature == signature) return

        // Los tonos de Monet solo se adoptan si el usuario tiene el acento en "del sistema";
        // con un acento fijo seguimos derivándolos del color elegido.
        val dynamicTones = dynamicAccent?.takeIf { normalized.accentStyle == AccentStyle.DYNAMIC }
        val base = if (darkTheme) darkPalette else lightPalette
        val background = resolveBackground(base, darkTheme, oledTheme, normalized)
        val primary = resolveAccent(base, background, darkTheme, normalized, dynamicTones?.primary)
        val card = resolveCard(background, darkTheme, normalized.surfaceStyle)
        val surfaceVariant = mix(card, if (darkTheme) Color.White else Color.Black, if (darkTheme) 0.045f else 0.035f)
        val primaryLight = dynamicTones?.primaryContainer
            ?: mix(primary, background, if (darkTheme) 0.72f else 0.84f)

        appliedSignature = signature
        IsDarkTheme = darkTheme
        Primary = primary
        PrimaryDark = dynamicTones?.onPrimaryContainer
            ?: if (darkTheme) mix(primary, Color.White, 0.72f) else mix(primary, Color.Black, 0.45f)
        PrimaryLight = primaryLight
        OnPrimary = contentColorOn(primary)
        OnPrimaryContainer = dynamicTones?.onPrimaryContainer ?: contentColorOn(primaryLight)
        Blue = base.blue
        BlueLight = base.blueLight
        Teal = base.teal
        TealLight = base.tealLight
        Green = base.green
        GreenLight = base.greenLight
        Coral = base.coral
        CoralLight = base.coralLight
        Yellow = base.yellow
        YellowLight = base.yellowLight
        Background = background
        Card = card
        SurfaceVariant = surfaceVariant
        TextPrimary = if (darkTheme) Color(0xFFF8F4FF) else Color(0xFF171427)
        TextSecondary = when {
            highContrast && darkTheme -> Color(0xFFECE9F4)
            highContrast -> Color(0xFF393442)
            darkTheme -> Color(0xFFD3D0E0)
            else -> Color(0xFF5F5B6B)
        }
        SoftOutline = mix(
            card,
            TextPrimary,
            if (highContrast) 0.28f else if (darkTheme) 0.12f else 0.14f
        )
        GradientEnd = mix(background, if (darkTheme) Color.Black else Color.White, 0.24f)
        BottomBar = resolveCard(background, darkTheme, SurfaceStyle.ELEVATED)
        BottomBarSelected = primaryLight
    }

    private fun resolveBackground(
        base: UniStackColorPalette,
        darkTheme: Boolean,
        oledTheme: Boolean,
        appearance: AppearancePreferences
    ): Color {
        if (oledTheme) return Color.Black
        return when (appearance.backgroundStyle) {
            BackgroundStyle.DEFAULT -> base.background
            BackgroundStyle.PURE -> if (darkTheme) Color.Black else Color.White
            BackgroundStyle.COOL -> if (darkTheme) Color(0xFF050A13) else Color(0xFFF5F7FC)
            BackgroundStyle.VIOLET -> if (darkTheme) Color(0xFF0D0818) else Color(0xFFFAF7FF)
            BackgroundStyle.CUSTOM -> appearance.customBackgroundColor?.let(::Color) ?: base.background
        }
    }

    private fun resolveAccent(
        base: UniStackColorPalette,
        background: Color,
        darkTheme: Boolean,
        appearance: AppearancePreferences,
        dynamicAccent: Color?
    ): Color {
        val selected = when (appearance.accentStyle) {
            // Si el dispositivo no expone Monet (API < 31) caemos al violeta de marca.
            AccentStyle.DYNAMIC -> dynamicAccent
                ?: if (darkTheme) Color(0xFF9A4DFF) else Color(0xFF6750F5)
            AccentStyle.VIOLET -> if (darkTheme) Color(0xFF9A4DFF) else Color(0xFF6750F5)
            AccentStyle.BLUE -> if (darkTheme) Color(0xFF65A7FF) else Color(0xFF1E7BEA)
            AccentStyle.TEAL -> if (darkTheme) Color(0xFF21D6BF) else Color(0xFF008F87)
            AccentStyle.GREEN -> if (darkTheme) Color(0xFF74D88B) else Color(0xFF2F9E50)
            AccentStyle.PINK -> if (darkTheme) Color(0xFFFF6CB4) else Color(0xFFD83D87)
            AccentStyle.CUSTOM -> appearance.customAccentColor?.let(::Color) ?: base.primary
        }
        return when (appearance.accentIntensity) {
            AccentIntensity.SOFT -> mix(selected, background, 0.22f)
            AccentIntensity.BALANCED -> selected
            AccentIntensity.VIBRANT -> mix(selected, if (darkTheme) Color.White else Color.Black, 0.08f)
        }
    }

    private fun resolveCard(background: Color, darkTheme: Boolean, style: SurfaceStyle): Color {
        val contrast = if (darkTheme) Color.White else Color.Black
        return when (style) {
            SurfaceStyle.FLAT -> background
            SurfaceStyle.OUTLINED -> mix(background, contrast, if (darkTheme) 0.025f else 0.018f)
            SurfaceStyle.ELEVATED -> mix(background, contrast, if (darkTheme) 0.065f else 0.035f)
            SurfaceStyle.TRANSLUCENT -> mix(background, contrast, if (darkTheme) 0.045f else 0.025f)
        }
    }

    /** Luminancia relativa WCAG 2.1 (con corrección gamma sRGB). */
    private fun relativeLuminance(color: Color): Float {
        fun channel(value: Float): Float =
            if (value <= 0.03928f) value / 12.92f else ((value + 0.055f) / 1.055f).pow(2.4f)
        return 0.2126f * channel(color.red) + 0.7152f * channel(color.green) + 0.0722f * channel(color.blue)
    }

    private fun mix(first: Color, second: Color, amount: Float): Color {
        val ratio = amount.coerceIn(0f, 1f)
        return Color(
            red = first.red + (second.red - first.red) * ratio,
            green = first.green + (second.green - first.green) * ratio,
            blue = first.blue + (second.blue - first.blue) * ratio,
            alpha = 1f
        )
    }
}

private data class UniStackColorPalette(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val blue: Color,
    val blueLight: Color,
    val teal: Color,
    val tealLight: Color,
    val green: Color,
    val greenLight: Color,
    val coral: Color,
    val coralLight: Color,
    val yellow: Color,
    val yellowLight: Color,
    val background: Color,
    val card: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val softOutline: Color,
    val gradientEnd: Color,
    val bottomBar: Color,
    val bottomBarSelected: Color
)
