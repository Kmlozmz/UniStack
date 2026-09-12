package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.feature_terms.domain.AcademicBreak
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Espanol: Locale = Locale.forLanguageTag("es")
private val Corta = DateTimeFormatter.ofPattern("d MMM yyyy", Espanol)

/**
 * Los días en que no hubo clase para nadie.
 *
 * Están aquí y no en el horario porque no son de una materia: un festivo lo es para todas,
 * igual que la escala o el reparto de los cortes. Sin ellos la app apunta faltas los días que
 * la universidad estaba cerrada y no hay forma de distinguirlas de las de verdad.
 */
@Composable
internal fun AcademicBreaksSection(
    breaks: List<AcademicBreak>,
    onSave: (id: String?, name: String, start: LocalDate, end: LocalDate) -> Unit,
    onDelete: (String) -> Unit
) {
    var editando by remember { mutableStateOf<AcademicBreak?>(null) }
    var creando by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            text = stringResource(R.string.settings_breaks_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )

        breaks.forEach { tramo ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .clickable { editando = tramo }
                    .padding(horizontal = 13.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = tramo.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (tramo.days == 1) {
                            tramo.start.format(Corta)
                        } else {
                            stringResource(R.string.settings_breaks_days_count, tramo.days).let { "${tramo.start.format(Corta)} → ${tramo.end.format(Corta)} · $it" }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onDelete(tramo.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.settings_breaks_remove, tramo.name),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable { creando = true }
                .padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = if (breaks.isEmpty()) stringResource(R.string.settings_breaks_add_first) else stringResource(R.string.settings_breaks_add_another),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (creando || editando != null) {
        BreakEditorDialog(
            existente = editando,
            onDismiss = {
                creando = false
                editando = null
            },
            onConfirm = { nombre, desde, hasta ->
                onSave(editando?.id, nombre, desde, hasta)
                creando = false
                editando = null
            }
        )
    }
}

/** Qué extremo del tramo se está eligiendo. */
private enum class Extremo { DESDE, HASTA }

@Composable
private fun BreakEditorDialog(
    existente: AcademicBreak?,
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, LocalDate) -> Unit
) {
    var nombre by remember { mutableStateOf(existente?.name.orEmpty()) }
    var desde by remember { mutableStateOf(existente?.start) }
    var hasta by remember { mutableStateOf(existente?.end) }
    var eligiendo by remember { mutableStateOf<Extremo?>(null) }

    // Un día suelto es lo más común —un festivo—, así que basta con poner el inicio.
    val finReal = hasta ?: desde
    val valido = nombre.isNotBlank() && desde != null && finReal != null && !finReal.isBefore(desde)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existente == null) stringResource(R.string.settings_breaks_dialog_title_new) else stringResource(R.string.settings_breaks_dialog_title_edit)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it.take(60) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.settings_breaks_field_what)) },
                    placeholder = { Text(stringResource(R.string.settings_breaks_field_hint)) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    CampoDeFecha(
                        etiqueta = stringResource(R.string.settings_breaks_start),
                        fecha = desde,
                        modifier = Modifier.weight(1f),
                        onClick = { eligiendo = Extremo.DESDE }
                    )
                    CampoDeFecha(
                        etiqueta = stringResource(R.string.settings_breaks_end),
                        fecha = hasta,
                        vacio = stringResource(R.string.settings_breaks_same_day),
                        modifier = Modifier.weight(1f),
                        onClick = { eligiendo = Extremo.HASTA }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val d = desde ?: return@TextButton
                    onConfirm(nombre, d, finReal ?: d)
                },
                enabled = valido
            ) {
                Text(stringResource(R.string.common_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )

    eligiendo?.let { extremo ->
        UniDatePickerDialog(
            selectedDate = if (extremo == Extremo.DESDE) desde else hasta,
            onDateSelected = { fecha ->
                if (extremo == Extremo.DESDE) desde = fecha else hasta = fecha
                eligiendo = null
            },
            onDismiss = { eligiendo = null }
        )
    }
}

@Composable
private fun CampoDeFecha(
    etiqueta: String,
    fecha: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    vacio: String = stringResource(R.string.terms_field_choose)
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(etiqueta, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.5.sp)
        Text(
            text = fecha?.format(Corta) ?: vacio,
            color = if (fecha != null) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.primary
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
