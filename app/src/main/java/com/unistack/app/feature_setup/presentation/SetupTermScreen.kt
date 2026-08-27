package com.unistack.app.feature_setup.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.domain.Corte
import java.time.LocalDate

/**
 * El periodo académico: cuándo empieza y cuándo acaba.
 *
 * Es el dato que faltaba en toda la app. Con él, el historial deja de inventar clases
 * anteriores al semestre, la asistencia puede ser honesta, y hay algo que cerrar al final.
 *
 * Tres bloques, y el orden importa. Primero **cómo se organiza el calendario** de la
 * universidad, porque de ahí salen las fechas sugeridas. Luego **las fechas**: el inicio es
 * obligatorio y el fin es una previsión —en agosto nadie sabe el día exacto—, así que el
 * periodo no se cierra solo cuando llega. Y por último, opcionales, **los días en que cierra
 * cada corte**, que es lo que permite dejar de preguntar el corte al registrar una nota.
 *
 * Va después de los cortes y no antes: las fechas de corte que se piden aquí necesitan saber
 * ya cuántos hay.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupTermScreen(
    type: AcademicTermType?,
    name: String,
    start: LocalDate?,
    plannedEnd: LocalDate?,
    cutEndDates: List<LocalDate>,
    cutCount: Int,
    isValid: Boolean,
    onTypeSelected: (AcademicTermType) -> Unit,
    onNameChange: (String) -> Unit,
    onStartChange: (LocalDate) -> Unit,
    onPlannedEndChange: (LocalDate) -> Unit,
    onSuggestCutDates: () -> Unit,
    onClearCutDates: () -> Unit,
    onCutDateChange: (Int, LocalDate) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 7
) {
    // Cuál de las fechas se está eligiendo. Nulo: ninguna, el diálogo está cerrado.
    var picking by remember { mutableStateOf<TermDateTarget?>(null) }

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = SetupSteps.Term,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = isValid,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
            UniStackButton(
                text = "Prefiero hacerlo después",
                onClick = onSkipClick,
                variant = UniStackButtonVariant.Outlined
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SetupPlainTitle(
                title = "¿Cómo se manejan los periodos en tu universidad?",
                subtitle = "Con las fechas sé qué clases existieron y cuáles no."
            )

            TermTypeRibbon(selected = type, onSelected = onTypeSelected)

            if (type != null && start != null) {
                UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = onNameChange,
                            label = { Text("Nombre del periodo") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                            TermDateField(
                                label = "Empieza",
                                date = start,
                                modifier = Modifier.weight(1f),
                                onClick = { picking = TermDateTarget.START }
                            )
                            TermDateField(
                                label = "Acaba (previsto)",
                                date = plannedEnd,
                                modifier = Modifier.weight(1f),
                                onClick = { picking = TermDateTarget.PLANNED_END }
                            )
                        }
                        Text(
                            text = "La fecha de fin es solo una previsión. El periodo no se cierra hasta que tú lo cierres.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }

                if (cutCount > 1) {
                    TermCutDatesSection(
                        cutCount = cutCount,
                        cutEndDates = cutEndDates,
                        onSuggest = onSuggestCutDates,
                        onClear = onClearCutDates,
                        onPick = { index -> picking = TermDateTarget.cut(index) }
                    )
                }
            }
        }
    }

    picking?.let { target ->
        UniDatePickerDialog(
            selectedDate = when (target) {
                TermDateTarget.START -> start
                TermDateTarget.PLANNED_END -> plannedEnd
                else -> cutEndDates.getOrNull(target.cutIndex)
            },
            onDateSelected = { fecha ->
                when (target) {
                    TermDateTarget.START -> onStartChange(fecha)
                    TermDateTarget.PLANNED_END -> onPlannedEndChange(fecha)
                    else -> onCutDateChange(target.cutIndex, fecha)
                }
                picking = null
            },
            onDismiss = { picking = null }
        )
    }
}

/** Cuál de las fechas del periodo se está eligiendo. */
private data class TermDateTarget(val cutIndex: Int) {
    companion object {
        val START = TermDateTarget(-1)
        val PLANNED_END = TermDateTarget(-2)
        fun cut(index: Int) = TermDateTarget(index)
    }
}

/**
 * Las cinco formas de organizar el calendario, en tarjetas.
 *
 * En tarjetas y no en fila de chips porque cada una lleva su explicación —«2 al año, unas 16
 * semanas»—, que es justo lo que despeja la duda de quien no sabe cómo se llama lo suyo.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TermTypeRibbon(
    selected: AcademicTermType?,
    onSelected: (AcademicTermType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AcademicTermType.entries.forEach { opcion ->
            val elegida = selected == opcion
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                onClick = { onSelected(opcion) },
                color = if (elegida) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                borderColor = if (elegida) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                borderWidth = if (elegida) 1.4.dp else 1.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = opcion.label,
                        color = if (elegida) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.titleSmallEmphasized
                    )
                    Text(
                        text = opcion.detail,
                        color = if (elegida) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TermDateField(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = date?.let(::termDateLabel) ?: "Elegir",
                color = if (date != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.primary
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Los días en que cierra cada corte, que son uno menos que cortes hay.
 *
 * El último no aparece porque acaba con el periodo. Guardar solo el punto de corte —y no los
 * dos extremos de cada uno— hace que los solapes y los huecos no puedan existir: no hay forma
 * de escribirlos, así que no hay nada que validar ni que explicar.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TermCutDatesSection(
    cutCount: Int,
    cutEndDates: List<LocalDate>,
    onSuggest: () -> Unit,
    onClear: () -> Unit,
    onPick: (Int) -> Unit
) {
    val corte = Corte.Singular.lowercase()
    UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "¿Sabes cuándo cierra cada $corte?",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmallEmphasized
            )
            Text(
                text = if (cutEndDates.isEmpty()) {
                    "Es opcional. Si las pones, al registrar una nota no tendrás que elegir el $corte: sale de la fecha."
                } else {
                    "Solo el día en que acaba cada uno. El siguiente empieza al día siguiente."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
            if (cutEndDates.isEmpty()) {
                UniStackButton(
                    text = "Ponerlas",
                    onClick = onSuggest,
                    variant = UniStackButtonVariant.Outlined
                )
            } else {
                cutEndDates.forEachIndexed { index, fecha ->
                    TermDateField(
                        label = "${Corte.Singular} ${index + 1} acaba el",
                        date = fecha,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onPick(index) }
                    )
                }
                Text(
                    text = "${Corte.Singular} $cutCount acaba con el periodo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
                UniStackButton(
                    text = "Dejarlo para después",
                    onClick = onClear,
                    variant = UniStackButtonVariant.Outlined
                )
            }
        }
    }
}

private val MesesCortos =
    listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

private fun termDateLabel(date: LocalDate): String =
    "${date.dayOfMonth} ${MesesCortos[date.monthValue - 1]} ${date.year}"
