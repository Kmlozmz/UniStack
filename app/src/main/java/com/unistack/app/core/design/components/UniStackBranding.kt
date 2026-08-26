package com.unistack.app.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
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
val BrandPurple = Color(0xFF8E00FF)

/**
 * El símbolo de la marca.
 *
 * Es el PNG original, no un dibujo en código: el archivo *es* la marca. Las pantallas que la
 * animan —arranque, final del onboarding— la recomponen desde [UniStackBrandMark] únicamente
 * porque una imagen plana no se puede mover por piezas; el símbolo quieto sale siempre de aquí.
 */
@Composable
fun UniStackLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Image(
        painter = painterResource(id = R.drawable.unistack_option_a_symbol),
        contentDescription = "UniStack",
        modifier = modifier.size(size)
    )
}

/** La misma marca recortada en blanco, para fondos de color. */
@Composable
fun UniStackLogoMarkWhite(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    Image(
        painter = painterResource(id = R.drawable.unistack_option_a_symbol_white),
        contentDescription = "UniStack",
        modifier = modifier.size(size)
    )
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

