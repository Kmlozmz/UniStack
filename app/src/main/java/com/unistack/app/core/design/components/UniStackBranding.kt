package com.unistack.app.core.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Stack") }
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

