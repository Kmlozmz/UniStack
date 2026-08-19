package com.unistack.app.core.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.material3.MaterialTheme
/**
 * Colores del selector de fecha, tomados del esquema de Material.
 *
 * Antes fijaban sus propios hex (incluido un acento coral que ignoraba el acento elegido
 * por el usuario), lo que dejaba el date picker fuera del tema y del color dinámico.
 */
object UniStackDatePickerColors {
    val Surface: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow

    val DayCell: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

    val Accent: Color
        @Composable get() = MaterialTheme.colorScheme.primary

    /** Contenido sobre [Accent]; se calcula para no romperse con acentos claros. */
    val OnAccent: Color
        @Composable get() = MaterialTheme.colorScheme.onPrimary

    val Text: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface

    val Muted: Color
        @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
}
