@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath

/**
 * La galleta de doce lóbulos que asoma por la esquina de las tarjetas con color.
 *
 * Va como modificador y no como hijo de un `Box`. Siendo un hijo, el lienzo de 130 dp **contaba
 * para medir la caja**: la tarjeta crecía hasta la altura de un adorno que ni siquiera se ve
 * entero, y debajo del contenido quedaba un hueco vacío del alto de la galleta. Se veía rota
 * porque lo estaba. Pintado detrás, ocupa cero y la tarjeta mide lo que mida su contenido.
 *
 * **El recorte lo pone este modificador, y hay que pasarle la forma.** Durante un tiempo esto
 * decía que lo ponía la propia `Surface`, y era falso: una `Surface` recorta su *contenido*, no
 * lo que se pinta desde un modificador aplicado desde fuera. Con el desplazamiento por defecto
 * la galleta cae casi toda fuera del lienzo y no se notaba; en la tarjeta de copias, con 250 dp
 * de desplazamiento, se salía por el borde derecho y se veía como un pegote gris cortado contra
 * el canto de la pantalla.
 */
fun Modifier.cookieCorner(
    color: Color,
    shape: Shape,
    size: Dp = 130.dp,
    offsetX: Dp = 250.dp,
    offsetY: Dp = (-42).dp,
    alpha: Float = 0.18f
): Modifier = this.drawWithCache {
    val recorte = Path().apply {
        addOutline(shape.createOutline(this@drawWithCache.size, layoutDirection, this@drawWithCache))
    }
    val lado = size.toPx()
    val dx = offsetX.toPx()
    val dy = offsetY.toPx()
    onDrawBehind {
        clipPath(recorte) {
            translate(left = dx, top = dy) {
                withTransform({ scale(lado, lado, pivot = Offset.Zero) }) {
                    drawPath(CookiePath, color.copy(alpha = alpha))
                }
            }
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

// El `toPath()` de material3 es @Composable y no vale para un valor de fichero; el del
// motor de polígonos devuelve un trazado de Android que se convierte una sola vez.
private val CookiePath = CookiePolygon.toPath().asComposePath()
