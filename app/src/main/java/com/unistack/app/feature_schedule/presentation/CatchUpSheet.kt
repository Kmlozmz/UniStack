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
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.core.design.components.reacomodoDeLista
import kotlinx.coroutines.launch
import com.unistack.app.core.utils.performSafely
import com.unistack.app.core.design.components.RuedaDeAsistencia
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.AttendanceHistoryEntry
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * La fecha entera, con su dia de la semana: es lo que hay que leer para acordarse de si
 * fuiste. Un «2 sept» en gris de once puntos no le dice nada a nadie.
 */
private val DiaEntero: DateTimeFormatter
    get() = if (Locale.getDefault().language == "en") {
        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)
    } else {
        DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale.forLanguageTag("es"))
    }

private fun LocalDate.diaEntero(): String = format(DiaEntero).replaceFirstChar { it.titlecase(Locale.getDefault()) }

private fun hora(minuto: Int, en24: Boolean): String {
    val h = minuto / 60
    val m = minuto % 60
    if (en24) return "%02d:%02d".format(h, m)
    val h12 = (h % 12).takeIf { it != 0 } ?: 12
    val sufijo = if (Locale.getDefault().language == "en") {
        if (h < 12) "AM" else "PM"
    } else {
        if (h < 12) "a. m." else "p. m."
    }
    return "%d:%02d %s".format(h12, m, sufijo)
}

/**
 * Resolver de una vez todo lo que se quedó sin marcar.
 *
 * Los avisos se ignoran —una semana de exámenes y hay seis clases pendientes— y marcarlas una
 * a una, entrando y saliendo de cada panel, es lo que hace que se abandone el registro entero.
 * Aquí cada fila tiene sus dos botones y la lista se vacía sola según se responde.
 *
 * **Agrupada por dia, y el dia en grande.** Era una pila de tarjetas iguales con el nombre
 * de la materia como titulo y la fecha en gris pequeno debajo: siete veces «Sexo anal liko
 * liko» seguidas y habia que buscar la fecha con lupa para saber de que clase se hablaba. Lo
 * que uno recuerda es **el dia** —«el lunes estaba enfermo»—, asi que el dia manda: es la
 * cabecera de cada grupo, y dentro cada clase dice su hora, su aula y lleva el color de la
 * materia, que es lo que la distingue de un vistazo.
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

            val en24 = LocalAccessibilityPreferences.current.use24HourTime
            val hoy = LocalDate.now()
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                // Se agrupa lo pintado, no lo pendiente: asi la cabecera de un dia se va
                // con su ultima fila y no un segundo antes.
                mostradas.toList().groupBy { it.date }.forEach { (dia, clases) ->
                    item(key = "dia:${dia.toEpochDay()}") {
                        CabeceraDeDia(dia = dia, hoy = hoy, modifier = reacomodoDeLista())
                    }
                    items(clases, key = { claveDe(it) }) { entrada ->
                        val materia = subjects.firstOrNull { it.id == entrada.session.subjectId }
                        FilaPendiente(
                            modifier = reacomodoDeLista(),
                            entrada = entrada,
                            nombre = materia?.name ?: stringResource(R.string.schedule_detail_class),
                            color = materia.scheduleBlockColor(ScheduleAccent),
                            en24 = en24,
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
}

/** La clave de una clase de un dia: es lo que la distingue en la lista. */
private fun claveDe(e: AttendanceHistoryEntry): String = "${e.session.id}:${e.date.toEpochDay()}"

/**
 * El dia, como cabecera del grupo: nombre y fecha en grande, y a la derecha cuanto hace.
 *
 * «Hace 4 dias» es lo que de verdad ayuda a acordarse; la fecha exacta es para confirmarlo.
 */
@Composable
private fun CabeceraDeDia(dia: LocalDate, hoy: LocalDate, modifier: Modifier = Modifier) {
    val hace = ChronoUnit.DAYS.between(dia, hoy).toInt()
    Row(
        modifier = modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dia.diaEntero(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = when (hace) {
                0 -> stringResource(R.string.date_today)
                1 -> stringResource(R.string.date_yesterday)
                else -> stringResource(R.string.catch_up_days_ago, hace)
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Una clase sin marcar de ese dia: la materia con su color, la hora y el aula, y la rueda.
 *
 * Sin la chapa «Sin marcar» de antes: en una lista donde **todas** estan sin marcar, repetirlo en
 * cada fila era ruido. El estado lo dicen la rueda y los botones cuando respondes.
 *
 * El gesto va en la rueda, que es la pieza que cambia de estado. Los dos botones de abajo son
 * la respuesta; la rueda es lo que pasa cuando respondes.
 */
@Composable
private fun FilaPendiente(
    entrada: AttendanceHistoryEntry,
    nombre: String,
    color: Color,
    en24: Boolean,
    respuesta: ClassAttendanceStatus?,
    onMark: (ClassAttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    // Los colores fijos de la asistencia, no el acento: con el tema lila, el visto salia lila
    // y «asisti» y «falta» dejaban de distinguirse de un vistazo.
    val verde = AttendanceAttended
    val rojo = AttendanceAbsent
    val asistio = respuesta == ClassAttendanceStatus.ATTENDED
    val tono = if (asistio) verde else rojo
    val aula = entrada.session.location.split('•', limit = 2).first().trim()
        .ifBlank { stringResource(R.string.schedule_detail_no_room) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 11.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // La franja del color de la materia, como en el horario: es lo que la distingue.
            Box(
                Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = nombre,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = hora(entrada.session.startMinute, en24) + " – " +
                        hora(entrada.session.endMinute, en24) + "  ·  " + aula,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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

