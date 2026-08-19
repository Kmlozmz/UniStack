package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Cuánto se ha evaluado ya de una materia o de un corte.
 *
 * El relleno lleva un color de identidad —el acento de la app, o el de la propia materia si
 * quien la dibuja lo tiene a mano—, nunca uno de rendimiento. Ese es el matiz: la longitud
 * mide avance, así que pintarla de rojo por ir mal hacía que dijera dos cosas a la vez; pero
 * en gris parecía apagada, como si no avanzara. El rendimiento lo lleva la cifra.
 *
 * La dibuja [LinearWavyProgressIndicator], que es la barra de progreso de Material 3
 * Expressive: la parte recorrida ondula y la que falta queda plana. Antes esto estaba pintado
 * a mano con dos cajas, porque el `LinearProgressIndicator` de Material 3 1.3 dejaba un punto
 * flotando al final de la pista y con la barra a cero se leía como un dato suelto. El
 * indicador ondulado sí gestiona ese punto, así que el motivo para no usar Material desapareció.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EvaluationBar(
    fraction: Double,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val accent = color ?: MaterialTheme.colorScheme.primary
    LinearWavyProgressIndicator(
        progress = { fraction.coerceIn(0.0, 1.0).toFloat() },
        modifier = modifier.fillMaxWidth(),
        color = accent,
        // La pista tiene que verse. Al 12% sobre fondo oscuro era invisible, así que un
        // 30% evaluado se leía como un trozo de barra suelto flotando a la izquierda en
        // vez de como un tercio de algo.
        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    )
}
