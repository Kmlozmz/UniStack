package com.unistack.app.feature_schedule.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.utils.performSafely
import com.unistack.app.feature_schedule.domain.ClassAbsenceReason
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality

internal fun ClassAbsenceReason.label(): String = when (this) {
    ClassAbsenceReason.HEALTH -> "Salud"
    ClassAbsenceReason.TRANSPORT -> "Transporte"
    ClassAbsenceReason.PERSONAL -> "Personal"
    ClassAbsenceReason.ACADEMIC_CONFLICT -> "Otra clase"
    ClassAbsenceReason.OTHER -> "Otro"
}

internal fun ClassModality.label(): String = when (this) {
    ClassModality.IN_PERSON -> "Presencial"
    ClassModality.VIRTUAL -> "Virtual"
    ClassModality.HYBRID -> "Híbrida"
}

/**
 * Lo que se pregunta **después** de marcar, y solo si te apetece contestarlo.
 *
 * El modelo guardaba modalidad, motivo de falta y nota desde siempre, y los tres estaban
 * muertos porque nunca se preguntaban. Preguntarlos antes de guardar habría sido peor: cada
 * respuesta obligatoria es una razón más para no marcar nada.
 *
 * Por eso aparece cuando el estado **ya está guardado**. Cerrar el panel sin tocar esto no
 * pierde nada, y lo que se conteste se guarda al momento, sin botón de confirmar.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AttendanceDetail(
    status: ClassAttendanceStatus,
    modality: ClassModality,
    absenceReason: ClassAbsenceReason?,
    note: String,
    onModalityChange: (ClassModality) -> Unit,
    onReasonChange: (ClassAbsenceReason?) -> Unit,
    onNoteChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    // Ni una cancelada ni una pendiente tienen detalle que dar: no fuiste porque no la hubo.
    val visible = status == ClassAttendanceStatus.ATTENDED || status == ClassAttendanceStatus.ABSENT

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + expandVertically(
            spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)
        ),
        exit = fadeOut(tween(110)) + shrinkVertically(tween(160))
    ) {
        Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(
                text = when (status) {
                    ClassAttendanceStatus.ABSENT -> "Guardada. ¿Por qué faltaste?"
                    else -> "Guardada. ¿Cómo fue la clase?"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (status == ClassAttendanceStatus.ABSENT) {
                    ClassAbsenceReason.entries.forEach { motivo ->
                        val elegido = absenceReason == motivo
                        FilterChip(
                            selected = elegido,
                            onClick = {
                                if (!elegido) haptics.performSafely(HapticFeedbackType.SegmentTick)
                                // Volver a tocarlo lo quita: equivocarse no puede ser definitivo.
                                onReasonChange(if (elegido) null else motivo)
                            },
                            label = { Text(motivo.label(), fontSize = 12.sp) },
                            shape = FilterChipDefaults.shape
                        )
                    }
                } else {
                    ClassModality.entries.forEach { forma ->
                        val elegido = modality == forma
                        FilterChip(
                            selected = elegido,
                            onClick = {
                                if (!elegido) haptics.performSafely(HapticFeedbackType.SegmentTick)
                                onModalityChange(forma)
                            },
                            label = { Text(forma.label(), fontSize = 12.sp) },
                            shape = FilterChipDefaults.shape
                        )
                    }
                }
            }

            OutlinedTextField(
                value = note,
                onValueChange = { onNoteChange(it.take(140)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Una nota, si hace falta", fontSize = 13.sp) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                textStyle = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** Cómo se resume el detalle en una línea, cuando ya está puesto. */
internal fun resumenDeDetalle(
    status: ClassAttendanceStatus,
    modality: ClassModality,
    absenceReason: ClassAbsenceReason?,
    note: String
): String? {
    val piezas = buildList {
        when (status) {
            ClassAttendanceStatus.ABSENT -> absenceReason?.let { add(it.label()) }
            ClassAttendanceStatus.ATTENDED ->
                if (modality != ClassModality.IN_PERSON) add(modality.label())
            else -> Unit
        }
        if (note.isNotBlank()) add(note)
    }
    return piezas.takeIf { it.isNotEmpty() }?.joinToString(" · ")
}
