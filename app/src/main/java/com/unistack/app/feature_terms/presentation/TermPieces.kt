package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.LocalSectionColors
import java.time.LocalDate

private val MesesCortos =
    listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

internal fun LocalDate.diaMes(): String = "$dayOfMonth ${MesesCortos[monthValue - 1]}"

internal fun LocalDate.diaMesAno(): String = "$dayOfMonth ${MesesCortos[monthValue - 1]} $year"

/** El rótulo pequeño en versales que separa bloques, igual que en el historial de asistencia. */
@Composable
internal fun TermLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.outline,
        fontWeight = FontWeight.Black,
        fontSize = 10.sp,
        letterSpacing = 0.13.em
    )
}

/**
 * Una cifra con su nombre, de las tres que resumen un periodo.
 *
 * [value] llega ya escrito porque quien lo calcula sabe si hay dato: aquí un nulo se pinta
 * «—» y no un cero, que diría algo que nadie ha medido.
 */
@Composable
internal fun TermStat(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    note: String? = null
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Text(
            text = value ?: "—",
            color = tint ?: MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.02).em
        )
        if (note != null) {
            Text(
                text = note,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 10.sp
            )
        }
    }
}

/** Una fila de la comprobación: lo que falta, con su porqué debajo. */
@Composable
internal fun TermGapRow(title: String, detail: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(LocalSectionColors.current.atRisk),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.PriorityHigh,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(14.dp)
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.5.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp,
                lineHeight = 15.sp
            )
        }
    }
}

/** Una línea de lo que sí quedó terminado, para que la lista no sea solo reproches. */
@Composable
internal fun TermDoneRow(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Check,
            contentDescription = null,
            tint = LocalSectionColors.current.onTrack,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.5.sp,
            lineHeight = 16.sp
        )
    }
}

/** La tarjeta que envuelve un bloque del periodo, con el borde fino de siempre. */
@Composable
internal fun TermCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val forma = RoundedCornerShape(20.dp)
    val color = MaterialTheme.colorScheme.surfaceContainerHigh
    val borde = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier.fillMaxWidth(), shape = forma, color = color, border = borde) {
            Box(Modifier.padding(14.dp)) { content() }
        }
    } else {
        Surface(modifier = modifier.fillMaxWidth(), shape = forma, color = color, border = borde) {
            Box(Modifier.padding(14.dp)) { content() }
        }
    }
}

/** La insignia que distingue el periodo en curso del que ya se cerró. */
@Composable
internal fun TermStateChip(active: Boolean) {
    val colores = LocalSectionColors.current
    Surface(
        shape = CircleShape,
        color = if (active) colores.onTrack.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Text(
            text = if (active) "En curso" else "Cerrado",
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
            color = if (active) colores.onTrack else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** El texto centrado de cuando no hay nada que enseñar todavía. */
@Composable
internal fun TermEmptyNote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth().padding(24.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
}
