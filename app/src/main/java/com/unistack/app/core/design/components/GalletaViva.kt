@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.graphics.shapes.Morph
import com.unistack.app.core.design.theme.duracion
import com.unistack.app.core.design.theme.hayMovimiento

/**
 * El fondo de galleta de M3E, vivo: gira despacio y respira entre nueve y doce lóbulos.
 *
 * Se pidió el 16 sep 2026, mirando el promedio del cierre: «estaría bueno que estas formas se
 * fuesen moviendo». Solo se mueve el fondo; lo que va encima —la nota, el total, el visto— se
 * queda quieto para poder leerlo.
 *
 * El giro es lento a propósito, una vuelta cada 24 s: se nota que está vivo sin que haya que
 * seguirlo con la vista. Sin movimiento es la galleta de nueve lados de siempre, quieta.
 */
@Composable
fun Modifier.galletaViva(color: Color): Modifier {
    val vueltaMs = duracion(24_000)
    val respiroMs = duracion(3_200)
    if (!hayMovimiento() || vueltaMs <= 0 || respiroMs <= 0) {
        return this.background(color, MaterialShapes.Cookie9Sided.toShape())
    }
    val ciclo = rememberInfiniteTransition(label = "galleta viva")
    val giro = ciclo.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(vueltaMs, easing = LinearEasing)),
        label = "giro"
    )
    val respiro = ciclo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(respiroMs, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "respiro"
    )
    val forma = remember { Morph(MaterialShapes.Cookie9Sided, MaterialShapes.Cookie12Sided) }
    // Los valores se leen al dibujar y no al componer: así cada fotograma solo repinta.
    return this.drawWithCache {
        val camino = Path()
        val escala = Matrix()
        onDrawBehind {
            camino.rewind()
            forma.toPath(progress = respiro.value, path = camino)
            escala.reset()
            escala.scale(size.width, size.height)
            camino.transform(escala)
            rotate(giro.value) { drawPath(camino, color) }
        }
    }
}
