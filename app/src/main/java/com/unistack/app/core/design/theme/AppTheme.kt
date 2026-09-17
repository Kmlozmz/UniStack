package com.unistack.app.core.design.theme

import androidx.compose.ui.graphics.Color
import androidx.annotation.StringRes
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/**
 * Los cinco colores de una cara del tema.
 *
 * Un tema tiene dos: la clara y la oscura. **Esto es lo que estaba roto:** el tema guardaba una
 * sola paleta y la pintaba encima del esquema base, así que elegir «Claro» con Dracula puesto
 * daba Dracula igual —oscuro— y el modo parecía no funcionar. Solo OLED se notaba, porque ese
 * pinta el negro *después*.
 */
data class ThemePalette(
    val accent: Color,
    val background: Color,
    /** El nivel de tarjeta. Los demás niveles se derivan de este y del fondo. */
    val surface: Color,
    val ink: Color,
    /** Lo que se escribe **encima** del acento. */
    val onAccent: Color
)

/**
 * Un tema completo: fondo, tarjetas, tinta y acento, decididos juntos, en claro y en oscuro.
 *
 * Hasta ahora lo único que se podía cambiar era el acento, y ni eso: `AccentStyle` guardaba
 * cinco colores de los que **solo funcionaba «del fondo»** —los demás se elegían, se
 * guardaban, viajaban en la copia de seguridad y no pintaban nada. Un tema no es un color
 * suelto: es el conjunto, y por eso vive aquí entero.
 *
 * Las paletas conocidas llevan sus valores publicados, no aproximaciones a ojo: quien elige
 * «Catppuccin Mocha» espera el Mocha de verdad, y una versión parecida se nota. Cuando la
 * familia publica su propia cara clara —Latte para Catppuccin, Dawn para Rosé Pine, Day para
 * Tokyo Night— se usa esa y no una inventada.
 */
data class AppTheme(
    val id: String,
    /** El nombre fijo: las paletas publicadas se llaman igual en todos los idiomas. */
    private val fixedName: String,
    /** De dónde viene, para leerlo en la tarjeta. Vacío en los propios de la app. */
    val family: String = "",
    val dark: ThemePalette,
    val light: ThemePalette,
    /** El nombre traducible de los temas propios; cero en las paletas publicadas. */
    @param:StringRes private val nameRes: Int = 0
) {
    val name: String get() = if (nameRes != 0) Textos.get(nameRes) else fixedName

    fun palette(oscuro: Boolean): ThemePalette = if (oscuro) dark else light
}

private fun paleta(accent: Long, background: Long, surface: Long, ink: Long, onAccent: Long) =
    ThemePalette(Color(accent), Color(background), Color(surface), Color(ink), Color(onAccent))

object AppThemes {
    /** El de siempre, y el que se usa cuando el guardado ya no existe. */
    const val DEFAULT_ID = "unistack"

    val catalog: List<AppTheme> = listOf(
        // ---------------------------------------------------------------- propios
        AppTheme(
            "unistack", "UniStack", "",
            dark = paleta(0xFF7F77DD, 0xFF0A0C11, 0xFF181C27, 0xFFF4F2FA, 0xFF171040),
            light = paleta(0xFF5F56C9, 0xFFF4F3FA, 0xFFFFFFFF, 0xFF14131C, 0xFFFFFFFF)
        ),
        AppTheme(
            "medianoche", "Medianoche", "",
            dark = paleta(0xFF3F8FE0, 0xFF05070D, 0xFF111827, 0xFFEAF0FA, 0xFF04203F),
            light = paleta(0xFF1565C0, 0xFFEFF3FA, 0xFFFFFFFF, 0xFF0B1220, 0xFFFFFFFF),
            nameRes = R.string.theme_medianoche
        ),
        AppTheme(
            "bosque", "Bosque", "",
            dark = paleta(0xFF4FBFA6, 0xFF070F0D, 0xFF122019, 0xFFE9F5F0, 0xFF052620),
            light = paleta(0xFF10796A, 0xFFEEF6F3, 0xFFFFFFFF, 0xFF0B1A16, 0xFFFFFFFF),
            nameRes = R.string.theme_bosque
        ),
        AppTheme(
            "atardecer", "Atardecer", "",
            dark = paleta(0xFFE8693A, 0xFF120A08, 0xFF221410, 0xFFF8EDE7, 0xFF2A0F04),
            light = paleta(0xFFC24A1C, 0xFFFBF2ED, 0xFFFFFFFF, 0xFF23130D, 0xFFFFFFFF),
            nameRes = R.string.theme_atardecer
        ),
        AppTheme(
            "carbon", "Carbón", "",
            dark = paleta(0xFF8C93A8, 0xFF0B0D11, 0xFF171A21, 0xFFEDEEF2, 0xFF0B0D11),
            light = paleta(0xFF515869, 0xFFF2F3F6, 0xFFFFFFFF, 0xFF14161C, 0xFFFFFFFF),
            nameRes = R.string.theme_carbon
        ),
        AppTheme(
            "cereza", "Cereza", "",
            dark = paleta(0xFFE062A8, 0xFF120810, 0xFF241320, 0xFFFAECF4, 0xFF2E0A1E),
            light = paleta(0xFFBE3C82, 0xFFFBEFF5, 0xFFFFFFFF, 0xFF23101B, 0xFFFFFFFF),
            nameRes = R.string.theme_cereza
        ),
        AppTheme(
            "oceano", "Océano", "",
            dark = paleta(0xFF2FB4C9, 0xFF04101A, 0xFF0F2231, 0xFFE6F4FA, 0xFF032430),
            light = paleta(0xFF0B7A8C, 0xFFEDF5F9, 0xFFFFFFFF, 0xFF07171F, 0xFFFFFFFF),
            nameRes = R.string.theme_oceano
        ),
        AppTheme(
            "ambar", "Ámbar", "",
            dark = paleta(0xFFE0A63C, 0xFF100D05, 0xFF1F1A0D, 0xFFF9F2E2, 0xFF241A03),
            light = paleta(0xFF9A6C00, 0xFFFAF5EA, 0xFFFFFFFF, 0xFF1F1A0C, 0xFFFFFFFF),
            nameRes = R.string.theme_ambar
        ),
        AppTheme(
            "lavanda", "Lavanda", "",
            dark = paleta(0xFF9B8CF0, 0xFF0C0A14, 0xFF191428, 0xFFF0EDFC, 0xFF1B1240),
            light = paleta(0xFF6A57D4, 0xFFF3F1FD, 0xFFFFFFFF, 0xFF16122A, 0xFFFFFFFF),
            nameRes = R.string.theme_lavanda
        ),
        AppTheme(
            "vino", "Vino", "",
            dark = paleta(0xFFC8506A, 0xFF100608, 0xFF231015, 0xFFF9E9ED, 0xFF2C070F),
            light = paleta(0xFFA02A46, 0xFFFAEDF0, 0xFFFFFFFF, 0xFF200E13, 0xFFFFFFFF),
            nameRes = R.string.theme_vino
        ),

        // ---------------------------------------------------------------- propios, de nacer claros
        AppTheme(
            "papel", "Papel", "",
            dark = paleta(0xFF9A92F0, 0xFF14131A, 0xFF201F2A, 0xFFF0EEF6, 0xFF14122E),
            light = paleta(0xFF5F56C9, 0xFFF4F2ED, 0xFFFFFFFF, 0xFF1B1A22, 0xFFFFFFFF),
            nameRes = R.string.theme_papel
        ),
        AppTheme(
            "nieve", "Nieve", "",
            dark = paleta(0xFF6FB2F5, 0xFF0D131C, 0xFF17202D, 0xFFEDF2F8, 0xFF06213C),
            light = paleta(0xFF1F6FC4, 0xFFEEF2F7, 0xFFFFFFFF, 0xFF101725, 0xFFFFFFFF),
            nameRes = R.string.theme_nieve
        ),
        AppTheme(
            "menta", "Menta", "",
            dark = paleta(0xFF5FC98F, 0xFF0A140F, 0xFF14231B, 0xFFE9F6EE, 0xFF06301D),
            light = paleta(0xFF1F8B58, 0xFFF1F7F3, 0xFFFFFFFF, 0xFF10231A, 0xFFFFFFFF),
            nameRes = R.string.theme_menta
        ),
        AppTheme(
            "arena", "Arena", "",
            dark = paleta(0xFFD8A464, 0xFF14110C, 0xFF231E16, 0xFFF6F0E6, 0xFF261A08),
            light = paleta(0xFF8F5F1E, 0xFFF7F3EC, 0xFFFFFFFF, 0xFF24201A, 0xFFFFFFFF),
            nameRes = R.string.theme_arena
        ),

        // ---------------------------------------------------------------- catppuccin
        // Las tres oscuras comparten Latte como cara clara: es la que publica la paleta, y
        // inventarle una a cada una daría tres claros parecidos que no son Catppuccin.
        AppTheme(
            "mocha", "Mocha", "Catppuccin",
            dark = paleta(0xFFCBA6F7, 0xFF1E1E2E, 0xFF313244, 0xFFCDD6F4, 0xFF1E1E2E),
            light = paleta(0xFF8839EF, 0xFFEFF1F5, 0xFFFFFFFF, 0xFF4C4F69, 0xFFFFFFFF)
        ),
        AppTheme(
            "macchiato", "Macchiato", "Catppuccin",
            dark = paleta(0xFFC6A0F6, 0xFF24273A, 0xFF363A4F, 0xFFCAD3F5, 0xFF24273A),
            light = paleta(0xFF8839EF, 0xFFEFF1F5, 0xFFFFFFFF, 0xFF4C4F69, 0xFFFFFFFF)
        ),
        AppTheme(
            "frappe", "Frappé", "Catppuccin",
            dark = paleta(0xFFCA9EE6, 0xFF303446, 0xFF414559, 0xFFC6D0F5, 0xFF303446),
            light = paleta(0xFF8839EF, 0xFFEFF1F5, 0xFFFFFFFF, 0xFF4C4F69, 0xFFFFFFFF)
        ),
        AppTheme(
            "latte", "Latte", "Catppuccin",
            dark = paleta(0xFFCBA6F7, 0xFF1E1E2E, 0xFF313244, 0xFFCDD6F4, 0xFF1E1E2E),
            light = paleta(0xFF8839EF, 0xFFEFF1F5, 0xFFFFFFFF, 0xFF4C4F69, 0xFFFFFFFF)
        ),

        // ---------------------------------------------------------------- las conocidas
        AppTheme(
            "tokyo", "Tokyo Night", "Tokyo",
            dark = paleta(0xFF7AA2F7, 0xFF1A1B26, 0xFF292E42, 0xFFC0CAF5, 0xFF1A1B26),
            // Tokyo Night Day, la cara clara que publica la propia paleta.
            light = paleta(0xFF2E7DE9, 0xFFE1E2E7, 0xFFF0F0F4, 0xFF3760BF, 0xFFFFFFFF)
        ),
        AppTheme(
            "tokyostorm", "Storm", "Tokyo",
            dark = paleta(0xFFBB9AF7, 0xFF24283B, 0xFF2F3549, 0xFFC0CAF5, 0xFF24283B),
            light = paleta(0xFF7847BD, 0xFFE1E2E7, 0xFFF0F0F4, 0xFF3760BF, 0xFFFFFFFF)
        ),
        AppTheme(
            "dracula", "Dracula", "Dracula",
            dark = paleta(0xFFBD93F9, 0xFF282A36, 0xFF44475A, 0xFFF8F8F2, 0xFF282A36),
            // Alucard, la cara clara oficial de Dracula.
            light = paleta(0xFF644AC9, 0xFFF8F8F2, 0xFFFFFFFF, 0xFF282A36, 0xFFFFFFFF)
        ),
        AppTheme(
            "nord", "Nord", "Nord",
            dark = paleta(0xFF88C0D0, 0xFF2E3440, 0xFF3B4252, 0xFFD8DEE9, 0xFF2E3440),
            // Snow Storm, los tres claros que Nord publica junto a los cuatro oscuros.
            light = paleta(0xFF5E81AC, 0xFFECEFF4, 0xFFFFFFFF, 0xFF2E3440, 0xFFFFFFFF)
        ),
        AppTheme(
            "gruvbox", "Gruvbox", "Gruvbox",
            dark = paleta(0xFFFE8019, 0xFF282828, 0xFF3C3836, 0xFFEBDBB2, 0xFF282828),
            light = paleta(0xFFAF3A03, 0xFFFBF1C7, 0xFFF2E5BC, 0xFF3C3836, 0xFFFFFFFF)
        ),
        AppTheme(
            "solarized", "Solarized", "Solarized",
            dark = paleta(0xFF268BD2, 0xFF002B36, 0xFF073642, 0xFF93A1A1, 0xFF002B36),
            light = paleta(0xFF268BD2, 0xFFFDF6E3, 0xFFEEE8D5, 0xFF586E75, 0xFFFFFFFF)
        ),
        AppTheme(
            "rosepine", "Rosé Pine", "Rosé Pine",
            dark = paleta(0xFFC4A7E7, 0xFF191724, 0xFF1F1D2E, 0xFFE0DEF4, 0xFF191724),
            // Dawn, la cara clara de la familia.
            light = paleta(0xFF907AA9, 0xFFFAF4ED, 0xFFFFFAF3, 0xFF575279, 0xFFFFFFFF)
        ),
        AppTheme(
            "onedark", "One Dark", "One Dark",
            dark = paleta(0xFF61AFEF, 0xFF282C34, 0xFF21252B, 0xFFABB2BF, 0xFF282C34),
            light = paleta(0xFF4078F2, 0xFFFAFAFA, 0xFFFFFFFF, 0xFF383A42, 0xFFFFFFFF)
        ),
        AppTheme(
            "everforest", "Everforest", "Everforest",
            dark = paleta(0xFFA7C080, 0xFF2D353B, 0xFF343F44, 0xFFD3C6AA, 0xFF2D353B),
            light = paleta(0xFF8DA101, 0xFFFDF6E3, 0xFFF4F0D9, 0xFF5C6A72, 0xFFFFFFFF)
        ),
        AppTheme(
            "monokai", "Monokai", "Monokai",
            dark = paleta(0xFFA6E22E, 0xFF272822, 0xFF3E3D32, 0xFFF8F8F2, 0xFF272822),
            light = paleta(0xFF6A8F1F, 0xFFFAFAFA, 0xFFF0F0EA, 0xFF272822, 0xFFFFFFFF)
        )
    )

    fun byId(id: String?): AppTheme =
        catalog.firstOrNull { it.id == id } ?: catalog.first { it.id == DEFAULT_ID }

    /** Dónde está un tema en el catálogo, para abrir el carrusel justo ahí. */
    fun indexOf(id: String?): Int = catalog.indexOfFirst { it.id == id }.coerceAtLeast(0)
}
