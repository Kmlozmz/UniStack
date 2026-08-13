package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent

/** Alto a partir del cual el nombre puede ocupar dos líneas. */
private val TwoLinesFitFrom = 34.dp

/** Alto a partir del cual cabe el aula debajo del nombre. */
private val RoomFitsFrom = 44.dp

/** Alto a partir del cual cabe además la hora de inicio. */
private val TimeFitsFrom = 62.dp

private val BlockShape = RoundedCornerShape(9.dp)

/** Lo justo para teñir el bloque sin que compita con el resto de la pantalla. */
private const val TintAlpha = 0.17f

/**
 * Una clase dentro de la rejilla del horario.
 *
 * Antes cada rejilla dibujaba lo suyo: el bloque se rellenaba entero del color de la materia y
 * apilaba tres textos —la hora de inicio y la de fin en dos líneas, el nombre y el aula— en una
 * columna de sesenta y pico dp. Ocupaba mucho para lo que decía, y buena parte de lo que decía
 * ya estaba en el eje de horas de al lado.
 *
 * Ahora el color va en una franja y en un fondo tenue, el nombre manda, y lo demás aparece solo
 * si el bloque es lo bastante alto: una clase de media hora enseña el nombre y ya está.
 */
@Composable
internal fun ClassBlock(
    modifier: Modifier,
    height: Dp,
    color: Color,
    name: String,
    room: String,
    startLabel: String,
    onClick: () -> Unit
) {
    val showRoom = height >= RoomFitsFrom && room.isNotBlank()
    val showTime = height >= TimeFitsFrom
    val single = !showRoom && !showTime

    Row(
        modifier = modifier
            .clip(BlockShape)
            .background(color.copy(alpha = TintAlpha))
            .clickable(onClick = onClick)
    ) {
        Box(
            Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(color)
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                // Un bloque muy corto no puede gastar ocho dp en aire: se queda con dos.
                .padding(horizontal = 5.dp, vertical = if (height < TwoLinesFitFrom) 2.dp else 4.dp),
            // En un bloque de una sola línea el texto va centrado; con más líneas, arriba,
            // para que el nombre quede a la altura del inicio de la clase.
            verticalArrangement = if (single) Arrangement.Center else Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = name,
                color = UniStackColors.TextPrimary,
                fontSize = 10.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = when {
                    height < TwoLinesFitFrom -> 1
                    single -> 2
                    else -> 3
                },
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            if (showTime) {
                Text(
                    text = startLabel,
                    color = UniStackColors.TextSecondary,
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (showRoom) {
                Text(
                    text = room,
                    color = UniStackColors.TextSecondary,
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * El color con el que se pinta una materia en el horario.
 *
 * El color elegido a mano manda sobre el que la app deriva del nombre. Estaba resuelto así en
 * la vista previa y en el detalle, pero el horario completo llamaba directo a [subjectAccent] y
 * se saltaba el color personalizado: la misma materia salía de dos colores según la pantalla.
 */
internal fun Subject?.scheduleBlockColor(fallback: Color): Color =
    this?.customColor?.let(::Color) ?: this?.let(::subjectAccent) ?: fallback
