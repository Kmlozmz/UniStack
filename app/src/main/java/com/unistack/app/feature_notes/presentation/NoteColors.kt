package com.unistack.app.feature_notes.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Los colores con los que se puede pintar una nota.
 *
 * Son doce y se guardan como el entero de siempre, igual que el color propio de una materia. No
 * salen del tema a propósito: el color de una nota es contenido —«esta es la de urgente»— y tiene
 * que seguir siendo el mismo cuando alguien cambie el acento de la app o se pase a claro.
 *
 * Están elegidos oscuros y desaturados para que el texto blanco encima siga leyéndose, que es lo
 * que se rompe en cuanto se meten colores de saturación alta.
 */
object NoteColors {

    // design-tokens-ok-begin: la paleta de las notas es contenido, no interfaz; el usuario
    // elige uno y ese color viaja con la nota aunque cambie el tema de la app.
    val palette: List<Color> = listOf(
        Color(0xFF4A2A3A),
        Color(0xFF5A2E28),
        Color(0xFF5A4423),
        Color(0xFF3E4A28),
        Color(0xFF23453C),
        Color(0xFF24414F),
        Color(0xFF2A3556),
        Color(0xFF3B2F58),
        Color(0xFF4E2C4B),
        Color(0xFF3A3A3E),
        Color(0xFF2E2E33),
        Color(0xFF4A3B2A)
    )
    // design-tokens-ok-end

    /** El fondo de una nota: el suyo si lo tiene, y si no el de la app. */
    @Composable
    @ReadOnlyComposable
    fun surfaceFor(colorArgb: Int?): Color =
        colorArgb?.let { Color(it) } ?: MaterialTheme.colorScheme.surfaceContainerLow

    /**
     * El color del texto que va encima.
     *
     * Se decide por la luminosidad del fondo y no por el tema: una nota amarilla clara en modo
     * oscuro seguiría poniendo el texto en blanco, que es exactamente lo que no se lee.
     */
    fun contentOn(background: Color, fallback: Color): Color {
        if (background.alpha == 0f) return fallback
        // design-tokens-ok: matemática de contraste, no un color de interfaz.
        return if (background.luminance() > 0.45f) Color(0xFF14141A) else Color(0xFFF2F1F6)
    }
}
