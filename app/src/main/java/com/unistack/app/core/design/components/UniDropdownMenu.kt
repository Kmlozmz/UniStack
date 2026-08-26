package com.unistack.app.core.design.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

/**
 * El menú desplegable de la app.
 *
 * **Existe por la forma.** Material dibuja los menús con su esquina más pequeña —cuatro puntos,
 * prácticamente recta— y en una app cuyas tarjetas, botones y hojas van todos redondeados, el
 * menu era la unica pieza con pinta de cuadrado. Se notaba sobre todo al abrirlo encima de una
 * tarjeta: un rectángulo duro flotando sobre esquinas suaves.
 *
 * Doce puntos es lo que usan las hojas pequeñas del sistema, así que el menú ya no desentona
 * con lo que tiene detrás.
 */
@Composable
fun UniDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    /** Fondo propio, para las pantallas con identidad cromática como Notificaciones. */
    containerColor: Color? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        scrollState = scrollState,
        shape = MaterialTheme.shapes.medium,
        containerColor = containerColor ?: MenuDefaults.containerColor,
        content = content
    )
}
