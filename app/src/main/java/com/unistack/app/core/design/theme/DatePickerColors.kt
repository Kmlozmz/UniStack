package com.unistack.app.core.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Colores del selector de fecha, delegados en [UniStackColors].
 *
 * Antes fijaban sus propios hex (incluido un acento coral que ignoraba el acento elegido
 * por el usuario), lo que dejaba el date picker fuera del tema y del color dinámico.
 */
object UniStackDatePickerColors {
    val Surface: Color
        @Composable get() = UniStackColors.Card

    val DayCell: Color
        @Composable get() = UniStackColors.SurfaceVariant

    val Accent: Color
        @Composable get() = UniStackColors.Primary

    /** Contenido sobre [Accent]; se calcula para no romperse con acentos claros. */
    val OnAccent: Color
        @Composable get() = UniStackColors.OnPrimary

    val Text: Color
        @Composable get() = UniStackColors.TextPrimary

    val Muted: Color
        @Composable get() = UniStackColors.TextSecondary
}
