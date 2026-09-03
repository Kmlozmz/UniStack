package com.unistack.app.core.design.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Un tema completo: fondo, tarjetas, tinta y acento, decididos juntos.
 *
 * Hasta ahora lo único que se podía cambiar era el acento, y ni eso: `AccentStyle` guardaba
 * cinco colores de los que **solo funcionaba «del fondo»** —los demás se elegían, se
 * guardaban, viajaban en la copia de seguridad y no pintaban nada. Un tema no es un color
 * suelto: es el conjunto, y por eso vive aquí entero.
 *
 * Las paletas conocidas llevan sus valores publicados, no aproximaciones a ojo: quien elige
 * «Catppuccin Mocha» espera el Mocha de verdad, y una versión parecida se nota.
 */
data class AppTheme(
    val id: String,
    val name: String,
    /** De dónde viene, para agrupar en el carrusel. Vacío en los propios de la app. */
    val family: String = "",
    val accent: Color,
    val background: Color,
    /** El nivel de tarjeta. Los demás niveles se derivan de este y del fondo. */
    val surface: Color,
    val ink: Color,
    /** Lo que se escribe **encima** del acento. */
    val onAccent: Color
) {
    /** Si el tema es claro, que decide contrastes, sombras y qué tinta va sobre el acento. */
    val isLight: Boolean get() = background.luminance() > 0.5f
}

object AppThemes {

    /** El de siempre, y el que se usa cuando el guardado ya no existe. */
    const val DEFAULT_ID = "unistack"

    val catalog: List<AppTheme> = listOf(
        // ---------------------------------------------------------------- propios
        AppTheme("unistack", "UniStack", "", Color(0xFF7F77DD), Color(0xFF0A0C11), Color(0xFF181C27), Color(0xFFF4F2FA), Color(0xFF171040)),
        AppTheme("medianoche", "Medianoche", "", Color(0xFF3F8FE0), Color(0xFF05070D), Color(0xFF111827), Color(0xFFEAF0FA), Color(0xFF04203F)),
        AppTheme("bosque", "Bosque", "", Color(0xFF4FBFA6), Color(0xFF070F0D), Color(0xFF122019), Color(0xFFE9F5F0), Color(0xFF052620)),
        AppTheme("atardecer", "Atardecer", "", Color(0xFFE8693A), Color(0xFF120A08), Color(0xFF221410), Color(0xFFF8EDE7), Color(0xFF2A0F04)),
        AppTheme("carbon", "Carbón", "", Color(0xFF8C93A8), Color(0xFF0B0D11), Color(0xFF171A21), Color(0xFFEDEEF2), Color(0xFF0B0D11)),
        AppTheme("cereza", "Cereza", "", Color(0xFFE062A8), Color(0xFF120810), Color(0xFF241320), Color(0xFFFAECF4), Color(0xFF2E0A1E)),
        AppTheme("oceano", "Océano", "", Color(0xFF2FB4C9), Color(0xFF04101A), Color(0xFF0F2231), Color(0xFFE6F4FA), Color(0xFF032430)),
        AppTheme("ambar", "Ámbar", "", Color(0xFFE0A63C), Color(0xFF100D05), Color(0xFF1F1A0D), Color(0xFFF9F2E2), Color(0xFF241A03)),
        AppTheme("lavanda", "Lavanda", "", Color(0xFF9B8CF0), Color(0xFF0C0A14), Color(0xFF191428), Color(0xFFF0EDFC), Color(0xFF1B1240)),
        AppTheme("vino", "Vino", "", Color(0xFFC8506A), Color(0xFF100608), Color(0xFF231015), Color(0xFFF9E9ED), Color(0xFF2C070F)),

        // ---------------------------------------------------------------- propios, claros
        AppTheme("papel", "Papel", "", Color(0xFF5F56C9), Color(0xFFF4F2ED), Color(0xFFFFFFFF), Color(0xFF1B1A22), Color(0xFFFFFFFF)),
        AppTheme("nieve", "Nieve", "", Color(0xFF3F8FE0), Color(0xFFEEF2F7), Color(0xFFFFFFFF), Color(0xFF101725), Color(0xFFFFFFFF)),
        AppTheme("menta", "Menta", "", Color(0xFF5FC98F), Color(0xFFF1F7F3), Color(0xFFFFFFFF), Color(0xFF10231A), Color(0xFF08301E)),
        AppTheme("arena", "Arena", "", Color(0xFFC08A4A), Color(0xFFF7F3EC), Color(0xFFFFFFFF), Color(0xFF24201A), Color(0xFFFFFFFF)),

        // ---------------------------------------------------------------- catppuccin
        AppTheme("mocha", "Mocha", "Catppuccin", Color(0xFFCBA6F7), Color(0xFF1E1E2E), Color(0xFF313244), Color(0xFFCDD6F4), Color(0xFF1E1E2E)),
        AppTheme("macchiato", "Macchiato", "Catppuccin", Color(0xFFC6A0F6), Color(0xFF24273A), Color(0xFF363A4F), Color(0xFFCAD3F5), Color(0xFF24273A)),
        AppTheme("frappe", "Frappé", "Catppuccin", Color(0xFFCA9EE6), Color(0xFF303446), Color(0xFF414559), Color(0xFFC6D0F5), Color(0xFF303446)),
        AppTheme("latte", "Latte", "Catppuccin", Color(0xFF8839EF), Color(0xFFEFF1F5), Color(0xFFFFFFFF), Color(0xFF4C4F69), Color(0xFFFFFFFF)),

        // ---------------------------------------------------------------- otras conocidas
        AppTheme("tokyo", "Tokyo Night", "Tokyo", Color(0xFF7AA2F7), Color(0xFF1A1B26), Color(0xFF292E42), Color(0xFFC0CAF5), Color(0xFF1A1B26)),
        AppTheme("tokyostorm", "Storm", "Tokyo", Color(0xFFBB9AF7), Color(0xFF24283B), Color(0xFF2F3549), Color(0xFFC0CAF5), Color(0xFF24283B)),
        AppTheme("dracula", "Dracula", "Dracula", Color(0xFFBD93F9), Color(0xFF282A36), Color(0xFF44475A), Color(0xFFF8F8F2), Color(0xFF282A36)),
        AppTheme("nord", "Nord", "Nord", Color(0xFF88C0D0), Color(0xFF2E3440), Color(0xFF3B4252), Color(0xFFD8DEE9), Color(0xFF2E3440)),
        AppTheme("gruvbox", "Gruvbox", "Gruvbox", Color(0xFFFE8019), Color(0xFF282828), Color(0xFF3C3836), Color(0xFFEBDBB2), Color(0xFF282828)),
        AppTheme("solarized", "Solarized", "Solarized", Color(0xFF268BD2), Color(0xFF002B36), Color(0xFF073642), Color(0xFF93A1A1), Color(0xFF002B36)),
        AppTheme("rosepine", "Rosé Pine", "Rosé Pine", Color(0xFFC4A7E7), Color(0xFF191724), Color(0xFF1F1D2E), Color(0xFFE0DEF4), Color(0xFF191724)),
        AppTheme("onedark", "One Dark", "One Dark", Color(0xFF61AFEF), Color(0xFF282C34), Color(0xFF21252B), Color(0xFFABB2BF), Color(0xFF282C34)),
        AppTheme("everforest", "Everforest", "Everforest", Color(0xFFA7C080), Color(0xFF2D353B), Color(0xFF343F44), Color(0xFFD3C6AA), Color(0xFF2D353B)),
        AppTheme("monokai", "Monokai", "Monokai", Color(0xFFA6E22E), Color(0xFF272822), Color(0xFF3E3D32), Color(0xFFF8F8F2), Color(0xFF272822))
    )

    fun byId(id: String?): AppTheme =
        catalog.firstOrNull { it.id == id } ?: catalog.first { it.id == DEFAULT_ID }

    /** Agrupados como salen en el carrusel: primero los de la app, luego cada familia junta. */
    fun grouped(): List<Pair<String, List<AppTheme>>> =
        catalog.groupBy { it.family }
            .toList()
            .sortedBy { (familia, _) -> if (familia.isEmpty()) "" else "z$familia" }
            .map { (familia, temas) -> (familia.ifEmpty { "UniStack" }) to temas }
}
