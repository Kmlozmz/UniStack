package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.AppShapes

/**
 * Cuánto se ha evaluado ya de una materia o de un corte.
 *
 * El relleno lleva un color de identidad —el acento de la app, o el de la propia materia si
 * quien la dibuja lo tiene a mano—, nunca uno de rendimiento. Ese es el matiz: la longitud
 * mide avance, así que pintarla de rojo por ir mal hacía que dijera dos cosas a la vez; pero
 * en gris parecía apagada, como si no avanzara. El rendimiento lo lleva la cifra.
 *
 * No usa `LinearProgressIndicator` a propósito. Desde Material 3 1.3 dibuja un punto al final
 * de la pista, y con la barra a cero ese punto queda flotando solo al otro extremo: en las
 * pantallas de cortes se leía como un dato suelto que nadie había puesto ahí.
 */
@Composable
fun EvaluationBar(
    fraction: Double,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    color: Color? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(AppShapes.Pill)
            // La pista tiene que verse. Al 12% sobre fondo oscuro era invisible, así que un
            // 30% evaluado se leía como un trozo de barra suelto flotando a la izquierda en
            // vez de como un tercio de algo.
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0.0, 1.0).toFloat())
                .fillMaxHeight()
                .clip(AppShapes.Pill)
                .background(color ?: MaterialTheme.colorScheme.primary)
        )
    }
}
