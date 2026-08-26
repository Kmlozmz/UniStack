@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * La barra que aparece cuando hay cosas marcadas.
 *
 * Sale de abajo al marcar el primer elemento y se va al desmarcar el último, con lo que se puede
 * hacer sobre lo marcado.
 *
 * **Es una `Surface` con forma de pastilla, no `HorizontalFloatingToolbar`.** Aquel se dibujaba
 * como un círculo enorme y opaco en mitad de la pantalla: mide su contenido de una forma que no
 * casa con ir dentro de un `AnimatedVisibility` suelto en un `Box`, y el resultado tapaba media
 * lista. Una pastilla que se ajusta a lo que lleva dentro hace lo mismo y no puede desbordarse.
 *
 * La cuenta va a la izquierda porque responde a «¿cuántas llevo marcadas?», que es la pregunta
 * que uno se hace justo antes de pulsar borrar.
 */
@Composable
fun UniSelectionToolbar(
    selectedCount: Int,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = selectedCount > 0,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            tonalElevation = 3.dp,
            shadowElevation = 6.dp
        ) {
            Row(
                modifier = Modifier.padding(start = 18.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (selectedCount == 1) "1 marcada" else "$selectedCount marcadas",
                    style = MaterialTheme.typography.labelLargeEmphasized
                )
                actions()
            }
        }
    }
}
