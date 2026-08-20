@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.toPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star

/**
 * La galleta de doce lóbulos que asoma por la esquina de las tarjetas con color.
 *
 * Se dibuja con [Canvas] y no recortando una caja con la forma: recortar no llega a aplicarse
 * y lo que sale es el rectángulo entero, como ya pasó una vez en el hero de Inicio y otra en
 * las marcas de materia.
 *
 * Es de la misma familia que las marcas de las materias —el mismo motor de polígonos, distinto
 * número de lóbulos—, así que las superficies con color de la app se reconocen entre sí.
 */
@Composable
fun CookieCorner(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 130.dp,
    offsetX: Dp = 250.dp,
    offsetY: Dp = (-42).dp,
    alpha: Float = 0.18f
) {
    val path = CookiePolygon.toPath()
    Canvas(
        modifier = modifier
            .size(size)
            .offset(x = offsetX, y = offsetY)
    ) {
        withTransform({ scale(this.size.width, this.size.height, pivot = Offset.Zero) }) {
            drawPath(path, color.copy(alpha = alpha))
        }
    }
}

private val CookiePolygon: RoundedPolygon = RoundedPolygon.star(
    numVerticesPerRadius = 12,
    radius = 0.5f,
    innerRadius = 0.5f * 0.86f,
    rounding = CornerRounding(0.2f),
    centerX = 0.5f,
    centerY = 0.5f
)
