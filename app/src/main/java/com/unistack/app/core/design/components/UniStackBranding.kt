package com.unistack.app.core.design.components

import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
/**
 * El morado de la marca, fijo.
 *
 * «Stack» heredaba `colorScheme.primary`, asi que el nombre de la app cambiaba de color cada
 * vez que se tocaba el acento del tema —y con Material You lo decidia el fondo de pantalla del
 * telefono. Una marca que cambia de color deja de ser una marca: este es el unico color de la
 * app que no responde al tema.
 */
// design-tokens-ok: es la definicion del color de marca, no un color suelto en una pantalla
private val BrandPurple = Color(0xFF8E00FF)

/**
 * El simbolo, dibujado desde su definicion y no desde un PNG.
 *
 * Era una imagen con los degradados cocidos dentro, asi que cambiar el color de la marca movia
 * la pantalla de arranque —que si lo dibuja— y dejaba el simbolo de Acerca de con los colores
 * viejos. Dos marcas distintas segun donde mires. Con las tres barras salidas de
 * [UniStackBrandMark.Pills], el color se define una vez y lo siguen las dos.
 */
@Composable
fun UniStackLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Canvas(
        modifier = modifier.size(width = size, height = size * UniStackBrandMark.HeightRatio)
    ) {
        UniStackBrandMark.Pills.forEach { pill ->
            val ancho = pill.width * this.size.width
            val alto = pill.height * this.size.height
            translate(left = pill.left * this.size.width, top = pill.top * this.size.height) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(pill.gradientStart, pill.gradientEnd),
                        startX = 0f,
                        endX = ancho
                    ),
                    size = Size(ancho, alto),
                    cornerRadius = CornerRadius(alto / 2f)
                )
            }
        }
    }
}

/**
 * El nombre de la marca: «Uni» en el color del texto y «Stack» en el morado de la app.
 *
 * Vive aquí y no en cada pantalla para que el reparto de colores sea uno solo. El primer
 * tramo usa `TextPrimary` y no blanco fijo: en el tema claro, blanco sobre fondo claro no se
 * lee, y la marca tiene que verse en los dos.
 */
@Composable
fun UniStackWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) { append("Uni") }
            withStyle(SpanStyle(color = BrandPurple)) { append("Stack") }
        },
        modifier = modifier,
        fontSize = fontSize,
        lineHeight = fontSize * 1.22f,
        fontWeight = fontWeight,
        letterSpacing = 0.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

