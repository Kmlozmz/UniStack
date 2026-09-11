package com.unistack.app.core.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.feature_user.domain.Corte
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

/**
 * El día en que cierra cada corte, encadenados.
 *
 * Se escribe **solo el último día** de cada uno: el siguiente empieza al día posterior y el
 * último acaba con el periodo. Guardar los dos extremos permitiría escribir huecos y solapes;
 * guardar solo el corte los hace imposibles.
 *
 * Vive aquí, y no dentro del onboarding, porque se pide en dos sitios. El onboarding lo ofrece
 * al configurarse y promete que «en cuanto las sepas las añades en Ajustes › Configuración
 * académica» —una promesa que solo se cumple si es literalmente la misma pieza, con el mismo
 * encadenado y las mismas reglas, en los dos lados.
 *
 * [termStart] y [termPlannedEnd] son del periodo, y pueden faltar: sin ellas los extremos que
 * no se tocan salen vacíos, pero las fechas de los cortes se siguen pudiendo poner.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CutDatesSection(
    termStart: LocalDate?,
    termPlannedEnd: LocalDate?,
    cutWeights: List<String>,
    cutEndDates: List<LocalDate?>,
    onCutDateChange: (Int, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var picking by remember { mutableStateOf<Int?>(null) }
    val total = cutWeights.size

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        cutWeights.forEachIndexed { indice, peso ->
            val ultimo = indice == total - 1
            // El tramo se calcula: empieza donde acabo el anterior, o con el periodo.
            val desde = if (indice == 0) termStart else cutEndDates.getOrNull(indice - 1)?.plusDays(1)
            val hasta = if (ultimo) termPlannedEnd else cutEndDates.getOrNull(indice)

            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${indice + 1}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "${Corte.Singular} ${indice + 1}",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmallEmphasized
                            )
                            Text(
                                text = stringResource(R.string.cut_dates_weight_of_grade, peso),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    /*
                     * Los dos extremos a la vista, aunque solo uno se toque.
                     *
                     * Se pedia el final y el principio habia que deducirlo del corte
                     * anterior. Ensenar los dos hace evidente el encadenado sin permitir
                     * escribirlo: el que no se toca sale apagado.
                     */
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CutDateField(
                            label = stringResource(R.string.setup_term_dates_start),
                            date = desde,
                            vacio = "—",
                            enabled = false,
                            modifier = Modifier.weight(1f),
                            onClick = {}
                        )
                        if (ultimo) {
                            CutDateField(
                                label = stringResource(R.string.cut_dates_ends),
                                date = hasta,
                                vacio = stringResource(R.string.cut_dates_with_period),
                                enabled = false,
                                modifier = Modifier.weight(1f),
                                onClick = {}
                            )
                        } else {
                            CutDateField(
                                label = stringResource(R.string.cut_dates_ends),
                                date = cutEndDates.getOrNull(indice),
                                modifier = Modifier.weight(1f),
                                onClick = { picking = indice }
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = desde != null && hasta != null,
                        enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                        exit = fadeOut(tween(110)) + shrinkVertically(tween(160))
                    ) {
                        if (desde != null && hasta != null) {
                            Text(
                                text = stringResource(R.string.cut_dates_weeks, semanasEntreCortes(desde, hasta)),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    picking?.let { indice ->
        UniDatePickerDialog(
            selectedDate = cutEndDates.getOrNull(indice) ?: termStart,
            onDateSelected = { fecha ->
                onCutDateChange(indice, fecha)
                picking = null
            },
            onDismiss = { picking = null }
        )
    }
}

@Composable
private fun CutDateField(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vacio: String = stringResource(R.string.terms_field_choose),
    enabled: Boolean = true
) {
    UniCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (enabled) {
            MaterialTheme.colorScheme.surfaceContainerHigh
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        onClick = if (enabled) onClick else null
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = date?.let(::fechaLargaDeCorte) ?: vacio,
                color = when {
                    date != null -> MaterialTheme.colorScheme.onSurface
                    enabled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

internal fun fechaLargaDeCorte(date: LocalDate): String =
    "${date.dayOfMonth} ${date.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).trimEnd('.')} ${date.year}"

/** Redondeadas: «5 semanas» informa, «4,7 semanas» no. Nunca menos de una. */
private fun semanasEntreCortes(desde: LocalDate, hasta: LocalDate): Long =
    (ChronoUnit.DAYS.between(desde, hasta) / 7).coerceAtLeast(1)
