package com.unistack.app.core.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import com.unistack.app.core.design.shapes.PolygonShape
import com.unistack.app.core.design.shapes.UniStackShapesCatalog
import com.unistack.app.core.design.shapes.morphToPath
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import androidx.compose.material3.MaterialTheme
/**
 * Indicador de carga expresivo: un polígono que gira mientras muta hacia la siguiente
 * forma de la secuencia, encadenando el ciclo indefinidamente.
 *
 * Sustituye al spinner circular en pantallas de carga. Si el usuario desactivó las
 * animaciones se dibuja una forma quieta, sin girar.
 *
 * @param periodMillis duración de cada transición de forma (una "vuelta").
 */
@Composable
fun UniStackLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    periodMillis: Int = 900
) {
    val shapes = remember { UniStackShapesCatalog.loadingSequence }
    // Encadena forma[i] -> forma[i+1] y cierra el ciclo volviendo a la primera.
    val morphs = remember(shapes) {
        List(shapes.size) { index ->
            Morph(start = shapes[index], end = shapes[(index + 1) % shapes.size])
        }
    }
    val motionScale = LocalMotionDurationScale.current

    if (motionScale <= 0f) {
        Box(
            modifier = modifier
                .size(size)
                .background(color, PolygonShape(UniStackShapesCatalog.cookie))
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "loading-indicator")
    // Un único float 0..morphs.size recorre toda la secuencia: la parte entera elige el
    // morph activo y la fraccionaria es el progreso dentro de él.
    val cycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = morphs.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (periodMillis * morphs.size / motionScale).toInt().coerceAtLeast(200),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading-cycle"
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (periodMillis * 2 / motionScale).toInt().coerceAtLeast(200),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "loading-spin"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val index = cycle.toInt().coerceIn(0, morphs.lastIndex)
            drawPath(
                path = morphToPath(
                    morph = morphs[index],
                    progress = cycle - index,
                    size = this.size,
                    rotationDegrees = spin
                ),
                color = color
            )
        }
    }
}
