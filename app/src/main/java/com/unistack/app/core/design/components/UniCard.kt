package com.unistack.app.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalInterfaceSpacing

/**
 * La superficie sobre la que se apoya casi todo en la app.
 *
 * Ahora es un [Surface] de Material y nada más. Antes era una caja que reimplementaba a mano
 * la sombra, el recorte, el borde y el fondo, y que además decidía los cuatro a partir de dos
 * preferencias del usuario —estilo de superficie y estilo de esquinas—, así que cada pantalla
 * nueva había que mirarla en doce combinaciones.
 *
 * Eso se fue con ellas: la forma sale de la escala de formas del tema y la elevación, del
 * propio [Surface], que además tiñe el fondo según la altura como pide Material en lugar de
 * pintar una sombra por debajo.
 */
@Composable
fun UniCard(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    brush: Brush? = null,
    shape: Shape? = null,
    tonalElevation: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    contentPadding: PaddingValues? = null,
    content: @Composable () -> Unit
) {
    val resolvedShape = shape ?: MaterialTheme.shapes.large
    val resolvedContentPadding = contentPadding ?: PaddingValues(LocalInterfaceSpacing.current.cardPadding)
    val border = if (borderWidth > 0.dp) {
        BorderStroke(
            width = borderWidth,
            color = if (borderColor != Color.Transparent) {
                borderColor
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    } else {
        null
    }

    // Con degradado, el Surface va transparente y el pincel se pinta dentro: Surface solo
    // acepta un color liso, y perder el degradado cambiaría lo que dibujan las pantallas
    // que lo piden.
    val painted: @Composable () -> Unit = {
        Box(
            modifier = if (brush != null) Modifier.background(brush) else Modifier
        ) {
            Box(modifier = Modifier.padding(resolvedContentPadding)) { content() }
        }
    }

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = resolvedShape,
            color = if (brush != null) Color.Transparent else color,
            tonalElevation = tonalElevation,
            border = border,
            content = painted
        )
    } else {
        Surface(
            modifier = modifier,
            shape = resolvedShape,
            color = if (brush != null) Color.Transparent else color,
            tonalElevation = tonalElevation,
            border = border,
            content = painted
        )
    }
}
