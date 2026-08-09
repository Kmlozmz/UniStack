package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * El símbolo de marca descompuesto en sus tres píldoras, para poder animarlas por separado.
 *
 * Las medidas están sacadas del PNG original (unistack_option_a_symbol.png, 1200x1120)
 * escaneando su canal alfa, no aproximadas a ojo: dibujarlas a mano dejaba un logo con
 * proporciones distintas a las de la imagen que se ve en el resto de la app.
 *
 * Del escaneo salieron números exactos que conviene conocer al tocar esto: las tres
 * píldoras son idénticas —768x288— y sus filas están separadas siempre 304px, así que lo
 * único que las distingue es el desplazamiento horizontal y el degradado. Si alguna de las
 * tres deja de medir lo mismo que las otras, es que se ha colado un error.
 *
 * Todo se guarda en fracciones del rectángulo del símbolo para poder pintarlo a cualquier
 * tamaño sin repetir el cálculo.
 */
object UniStackBrandMark {

    /** El símbolo no es cuadrado: alto = ancho * esto. */
    const val HeightRatio = 1120f / 1200f

    data class Pill(
        val left: Float,
        val top: Float,
        val width: Float,
        val height: Float,
        val gradientStart: Color,
        val gradientEnd: Color
    )

    private const val PillWidth = 768f / 1200f
    private const val PillHeight = 288f / 1120f

    /**
     * De arriba abajo, en el mismo orden en que se apilan. Los degradados son los colores
     * reales muestreados en los extremos de cada píldora; la variación vertical del original
     * es despreciable, por eso se reproduce como degradado horizontal.
     *
     * design-tokens-ok-begin: colores de marca, deben verse igual en claro, oscuro y OLED
     */
    val Pills = listOf(
        Pill(
            left = 328f / 1200f,
            top = 112f / 1120f,
            width = PillWidth,
            height = PillHeight,
            gradientStart = Color(0xFF7456F7),
            gradientEnd = Color(0xFFC0AFFE)
        ),
        Pill(
            left = 104f / 1200f,
            top = 416f / 1120f,
            width = PillWidth,
            height = PillHeight,
            gradientStart = Color(0xFF3E219F),
            gradientEnd = Color(0xFF684AEF)
        ),
        Pill(
            left = 256f / 1200f,
            top = 720f / 1120f,
            width = PillWidth,
            height = PillHeight,
            gradientStart = Color(0xFF5C87F0),
            gradientEnd = Color(0xFF6B52F5)
        )
    )
    // design-tokens-ok-end
}

/**
 * Pinta una píldora dentro de un contenedor del tamaño del símbolo.
 *
 * [modifier] se aplica entre la colocación y el pintado, que es donde tiene que ir la
 * animación de cada píldora para que la mueva sin descolocar a las demás.
 */
@Composable
fun UniStackBrandPill(
    pill: UniStackBrandMark.Pill,
    markWidth: Dp,
    modifier: Modifier = Modifier
) {
    val markHeight = markWidth * UniStackBrandMark.HeightRatio
    Box(
        modifier = Modifier
            .offset(x = markWidth * pill.left, y = markHeight * pill.top)
            .size(width = markWidth * pill.width, height = markHeight * pill.height)
            .then(modifier)
            .clip(RoundedCornerShape(percent = 50))
            .background(Brush.horizontalGradient(listOf(pill.gradientStart, pill.gradientEnd)))
    )
}
