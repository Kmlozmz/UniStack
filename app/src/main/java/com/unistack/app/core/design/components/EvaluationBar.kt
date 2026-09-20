@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
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
 * Recta. Fue ondulada y elegible en Apariencia › Componentes hasta el 20 sep 2026; en un dato
 * que se consulta a diario, la onda distraía más de lo que decía, y la elección era de diseño,
 * no de quien usa la app. El indicador de Material gestiona el punto del final de la pista, que
 * es lo que en su día obligó a pintar esto a mano con dos cajas.
 *
 * Esto es progreso **académico**. El del sistema —descargas, guardado— usa `SystemProgress`,
 * que no se configura.
 */
@Composable
fun EvaluationBar(
    fraction: Double,
    modifier: Modifier = Modifier,
    color: Color? = null,
    /** La pista, para las secciones que tienen la suya. Si es null, la de por defecto. */
    trackColor: Color? = null
) {
    val accent = color ?: MaterialTheme.colorScheme.primary
    val progress = { fraction.coerceIn(0.0, 1.0).toFloat() }
    // La pista tiene que verse. Al 12% sobre fondo oscuro era invisible, así que un
    // 30% evaluado se leía como un trozo de barra suelto flotando a la izquierda en
    // vez de como un tercio de algo.
    val track = trackColor ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)

    // Recta, fijo desde el 20 sep 2026: la onda se elegía en Componentes y era decisión de
    // diseño. Las barras de descarga siguen onduladas, que ese es su gesto.
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier.fillMaxWidth(),
        color = accent,
        trackColor = track
    )
}

/**
 * El mismo progreso académico, en anillo.
 *
 * Existe para que el aro de una materia y su barra vayan iguales: rectos los dos.
 */
@Composable
fun EvaluationRing(
    fraction: Double,
    modifier: Modifier = Modifier,
    color: Color? = null,
    trackColor: Color? = null
) {
    val accent = color ?: MaterialTheme.colorScheme.primary
    val progress = { fraction.coerceIn(0.0, 1.0).toFloat() }
    val track = trackColor ?: MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)

    CircularProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = accent,
        trackColor = track
    )
}

