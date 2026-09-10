package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.core.design.components.reacomodoDeLista
import kotlinx.coroutines.launch
import com.unistack.app.core.utils.performSafely
import com.unistack.app.core.design.components.RuedaDeAsistencia
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.AttendanceHistoryEntry
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Espanol: Locale = Locale.forLanguageTag("es")
private val DiaCorto = DateTimeFormatter.ofPattern("d MMM", Espanol)

/**
 * Resolver de una vez todo lo que se quedó sin marcar.
 *
 * Los avisos se ignoran —una semana de exámenes y hay seis clases pendientes— y marcarlas una
 * a una, entrando y saliendo de cada panel, es lo que hace que se abandone el registro entero.
 * Aquí cada fila tiene sus dos botones y la lista se vacía sola según se responde.
 *
 * No hay «marcar todas como asistidas»: eso sería la app inventándose el dato, que es justo lo
 * que llevamos toda esta parte quitando.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatchUpSheet(
    pending: List<AttendanceHistoryEntry>,
    subjects: List<Subject>,
    onMark: (AttendanceHistoryEntry, ClassAttendanceStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = if (pending.isEmpty()) {
                        stringResource(R.string.schedule_all_caught_up)
                    } else {
                        stringResource(R.string.catch_up_unmarked_count, pending.size)
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (pending.isEmpty()) {
                        stringResource(R.string.catch_up_empty_title)
                    } else {
                        stringResource(R.string.catch_up_empty_desc)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            /*
             * **La fila se queda el tiempo que dura el gesto.**
             *
             * Aqui se pintaba `pending` directamente, y responder saca la clase de esa
             * lista **en el mismo fotograma**: la fila desaparecia antes de que el gesto
             * tuviera un pixel donde dibujarse. Estaba puesto y no se veia nunca, que es
             * lo mismo que no estar.
             *
             * `mostradas` es lo que hay pintado ahora: entra lo que llega de `pending` y
             * lo respondido se queda 520 ms mas —el gesto dura 250— antes de irse con la
             * salida de la lista.
             */
            val mostradas = remember { mutableStateListOf<AttendanceHistoryEntry>() }
            val respondidas = remember { mutableStateMapOf<String, ClassAttendanceStatus>() }
            LaunchedEffect(pending) {
                pending.forEachIndexed { i, e ->
                    if (mostradas.none { claveDe(it) == claveDe(e) }) {
                        mostradas.add(i.coerceAtMost(mostradas.size), e)
                    }
                }
            }
            val alcance = rememberCoroutineScope()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(mostradas.toList(), key = { claveDe(it) }) { entrada ->
                    val materia = subjects.firstOrNull { it.id == entrada.session.subjectId }
                    FilaPendiente(
                        modifier = reacomodoDeLista(),
                        entrada = entrada,
                        nombre = materia?.name ?: stringResource(R.string.schedule_detail_class),
                        respuesta = respondidas[claveDe(entrada)],
                        onMark = { estado ->
                            haptics.performSafely(HapticFeedbackType.SegmentTick)
                            respondidas[claveDe(entrada)] = estado
                            onMark(entrada, estado)
                            alcance.launch {
                                kotlinx.coroutines.delay(520)
                                mostradas.removeAll { claveDe(it) == claveDe(entrada) }
                                respondidas.remove(claveDe(entrada))
                            }
                        }
                    )
                }
            }
        }
    }
}

/** La clave de una clase de un dia: es lo que la distingue en la lista. */
private fun claveDe(e: AttendanceHistoryEntry): String = "${e.session.id}:${e.date.toEpochDay()}"

/**
 * Una clase sin marcar, con la forma que la vista previa de Movimiento siempre dibujo.
 *
 * **Eran dos botones redondos a la derecha y nada mas.** El preview de «Marcar asistencia»
 * lleva anos ensenando otra cosa: una fila con su rueda, su visto y una chapa que pasa de «Sin
 * marcar» a «Asisti». Eso es lo que hay aqui ahora, asi que lo que se elige en Ajustes es lo
 * que se ve al marcar.
 *
 * El gesto va en la rueda, que es la pieza que cambia de estado. Los dos botones de abajo son
 * la respuesta; la rueda es lo que pasa cuando respondes.
 */
@Composable
private fun FilaPendiente(
    entrada: AttendanceHistoryEntry,
    nombre: String,
    respuesta: ClassAttendanceStatus?,
    onMark: (ClassAttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val verde = ScheduleAccent
    val rojo = MaterialTheme.colorScheme.error
    val asistio = respuesta == ClassAttendanceStatus.ATTENDED
    val tono = if (asistio) verde else rojo

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = nombre,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = entrada.date.format(DiaCorto),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
                // La chapa dice el estado en palabras: la rueda sola no distingue «sin marcar»
                // de «marcada hace un momento».
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (respuesta == null) {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            } else {
                                tono.copy(alpha = 0.22f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = when (respuesta) {
                            ClassAttendanceStatus.ATTENDED -> stringResource(R.string.schedule_status_attended)
                            ClassAttendanceStatus.ABSENT -> stringResource(R.string.schedule_status_absent)
                            else -> stringResource(R.string.catch_up_unmarked_chip)
                        },
                        color = if (respuesta == null) MaterialTheme.colorScheme.onSurfaceVariant else tono,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            // La rueda nace gris y el gesto la llena: verde sobre verde no se veia.
            RuedaDeAsistencia(
                marcada = respuesta != null,
                color = tono,
                icono = if (asistio) Icons.Rounded.Check else Icons.Rounded.Close
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BotonDeRespuesta(
                icono = Icons.Rounded.Check,
                texto = stringResource(R.string.schedule_status_attended),
                tono = verde,
                elegido = respuesta == ClassAttendanceStatus.ATTENDED,
                modifier = Modifier.weight(1f),
                onClick = { onMark(ClassAttendanceStatus.ATTENDED) }
            )
            BotonDeRespuesta(
                icono = Icons.Rounded.Close,
                texto = stringResource(R.string.schedule_status_absent),
                tono = rojo,
                elegido = respuesta == ClassAttendanceStatus.ABSENT,
                modifier = Modifier.weight(1f),
                onClick = { onMark(ClassAttendanceStatus.ABSENT) }
            )
        }
    }
}

/** Una de las dos respuestas. Se apaga cuando la otra queda elegida. */
@Composable
private fun BotonDeRespuesta(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    tono: Color,
    elegido: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(if (elegido) tono else tono.copy(alpha = 0.14f))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (elegido) MaterialTheme.colorScheme.surface else tono,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            color = if (elegido) MaterialTheme.colorScheme.surface else tono,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

