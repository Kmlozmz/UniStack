package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.feature_schedule.domain.AttendanceHistoryEntry
import com.unistack.app.feature_schedule.domain.AttendanceSummary
import com.unistack.app.feature_schedule.domain.AttendanceWeek
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Espanol: Locale = Locale.forLanguageTag("es")
private val DiaMes = DateTimeFormatter.ofPattern("d MMM", Espanol)
private val SoloDia = DateTimeFormatter.ofPattern("d", Espanol)
private val Mes = DateTimeFormatter.ofPattern("MMMM", Espanol)

/**
 * El color con que se pinta cada estado en la tira y en las semanas.
 *
 * Sale de [attendanceColor] y no del tema: ver [AttendanceAttended]. Lo pendiente sí toma el
 * gris de la superficie, porque no es un hecho sino la falta de uno.
 */
@Composable
private fun ClassAttendanceStatus.cuadro(): Color =
    attendanceColor() ?: MaterialTheme.colorScheme.surfaceContainerHighest

/**
 * La cabecera del historial: faltas que quedan, y las clases dibujadas.
 *
 * Antes era un porcentaje enorme con su anillo, y con una sola clase marcada anunciaba
 * «100 %» —un dato sostenido por nada, presentado como titular—. Lo que de verdad se mira en
 * la universidad es cuántas faltas caben todavía.
 *
 * La tira de abajo cuenta **dos cosas con un solo gráfico**: los cuadros rojos son las faltas
 * gastadas, y los verdes seguidos del final son la racha. Así la racha no es un número que
 * haya que creerse, se ve.
 *
 * Sin tope de faltas puesto no se promete ninguno: se enseña el porcentaje diciendo sobre
 * cuántas clases se calcula, que es lo único honesto mientras falte el dato.
 */
@Composable
internal fun AttendanceSummaryCard(
    summary: AttendanceSummary,
    entries: List<AttendanceHistoryEntry>,
    today: LocalDate,
    onLimitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val restantes = summary.remainingAbsences
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = if (restantes != null) "Te quedan" else "Asistencia",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = when {
                                restantes != null -> "$restantes"
                                summary.rate != null -> "${summary.rate}%"
                                else -> "—"
                            },
                            letterSpacing = (-0.02).em,
                            color = if (summary.atLimit) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            fontSize = 33.sp,
                            fontWeight = FontWeight.Black
                        )
                        if (restantes != null) {
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "de ${summary.absenceLimit} faltas",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = leyendaDeApoyo(summary),
                        color = if (summary.atLimit || summary.oneLeft) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                if (summary.streak > 1) {
                    RachaChip(summary.streak)
                }
            }

            /*
             * El tope se pone desde aqui, que es donde se echa en falta.
             *
             * Sin el, la cifra grande no puede ser «te quedan N» y cae al porcentaje. Pedirlo
             * en el formulario de la materia lo habria escondido en un sitio al que solo se
             * entra a cambiar el nombre.
             */
            Text(
                text = if (summary.absenceLimit == null) {
                    "Poner un tope de faltas"
                } else {
                    "Cambiar el tope (${summary.absenceLimit})"
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onLimitClick)
                    .padding(vertical = 2.dp),
                color = ScheduleAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            if (entries.any { !it.date.isAfter(today) }) {
                Spacer(Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outlineVariant))
                TiraDeClases(entries = entries, today = today)
            }
        }
    }
}

/** La frase de apoyo bajo la cifra, que cambia según lo que se pueda afirmar. */
private fun leyendaDeApoyo(summary: AttendanceSummary): String = when {
    summary.decided == 0 -> "Marca tus clases y aparece aquí."
    summary.atLimit -> "Ya no te queda ninguna. ${summary.absent} faltas de ${summary.absenceLimit}."
    summary.oneLeft -> "Te queda una. ${summary.absent} faltas de ${summary.absenceLimit}."
    summary.absenceLimit != null ->
        "${summary.absent} faltas · ${summary.attended} asistencias · ${summary.decided} clases dadas"
    summary.tooFewToTrust ->
        "Sobre ${summary.decided} ${if (summary.decided == 1) "clase" else "clases"}. Aún son pocas para fiarse."
    else -> "${summary.attended} asistencias · ${summary.absent} faltas"
}

@Composable
private fun RachaChip(racha: Int) {
    Surface(
        shape = CircleShape,
        color = ScheduleAccent.copy(alpha = 0.16f)
    ) {
        Text(
            text = "🔥 $racha seguidas",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = ScheduleAccent,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Una fila de cuadros, uno por clase dada, del principio al final del periodo.
 *
 * Solo las pasadas: un cuadro vacío al final se leería como una clase sin marcar cuando en
 * realidad todavía no ha llegado.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TiraDeClases(entries: List<AttendanceHistoryEntry>, today: LocalDate) {
    val pasadas = entries.filter { !it.date.isAfter(today) }.sortedBy { it.date }
    // Con un semestre entero la tira no cabe; las últimas veinte cuentan la historia igual.
    val visibles = pasadas.takeLast(20)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Rotulo(
            text = if (visibles.size < pasadas.size) {
                "Tus últimas ${visibles.size} clases"
            } else {
                "Tus ${pasadas.size} ${if (pasadas.size == 1) "clase" else "clases"}"
            }
        )
        /*
         * Cuadros de tamaño fijo, no repartidos por el ancho.
         *
         * Con `weight` una sola clase ocupaba la fila entera y se leía como una barra de
         * progreso llena: exactamente el «100 %» sin fundamento que esta cabecera venía a
         * quitar. Fijos, una clase es un cuadro y once son once.
         */
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            visibles.forEach { entrada ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(entrada.status.cuadro())
                )
            }
        }
        Leyenda(visibles)
    }
}

/** El rótulo pequeño en versales que separa los bloques, como en el diseño aprobado. */
@Composable
private fun Rotulo(text: String) {
    Text(
        text = text.uppercase(Espanol),
        // `outline` y no `onSurfaceVariant`: es un rotulo de seccion, el escalon mas tenue.
        color = MaterialTheme.colorScheme.outline,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 0.13.em
    )
}

/**
 * Qué significa cada color, y solo de los que salen.
 *
 * Sin leyenda la tira es decorativa: se ve que hay rojos, pero no que son las faltas, que es
 * justo la mitad de lo que este gráfico cuenta.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Leyenda(visibles: List<AttendanceHistoryEntry>) {
    val presentes = visibles.map { it.status }.toSet()
    val orden = listOf(
        ClassAttendanceStatus.ATTENDED,
        ClassAttendanceStatus.ABSENT,
        ClassAttendanceStatus.CANCELLED,
        ClassAttendanceStatus.RESCHEDULED,
        ClassAttendanceStatus.PENDING
    ).filter { it in presentes }
    if (orden.size < 2) return

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(13.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        orden.forEach { estado ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(estado.cuadro())
                )
                Text(
                    text = estado.legendName(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.5.sp
                )
            }
        }
    }
}

/**
 * El historial: lo que viene primero, y luego una fila por semana.
 *
 * Cada semana enseña sus clases como cuadros, así que se ve un periodo entero sin desplazarse
 * y sin perder las fechas —que era lo que fallaba de una cuadrícula suelta: un cuadro no dice
 * de qué día es—. Una semana de receso no aparece, y eso es exactamente lo que fue.
 */
@Composable
internal fun AttendanceWeekList(
    weeks: List<AttendanceWeek>,
    upcoming: List<AttendanceHistoryEntry>,
    today: LocalDate,
    onPick: (AttendanceHistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        if (upcoming.isNotEmpty()) {
            EtiquetaDeGrupo("Próxima")
            upcoming.take(2).forEach { entrada ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable { onPick(entrada) }
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entrada.date.format(DiaMes),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = formatoDeHora(entrada.session.startMinute),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
            }
        }

        var mesAnterior: String? = null
        weeks.forEach { semana ->
            val mes = semana.start.format(Mes).replaceFirstChar { it.titlecase(Espanol) }
            if (mes != mesAnterior) {
                EtiquetaDeGrupo(mes)
                mesAnterior = mes
            }
            FilaDeSemana(semana = semana, today = today, onPick = onPick)
        }
    }
}

@Composable
private fun EtiquetaDeGrupo(texto: String) {
    Box(modifier = Modifier.padding(start = 11.dp, top = 4.dp)) {
        Rotulo(texto)
    }
}

@Composable
private fun FilaDeSemana(
    semana: AttendanceWeek,
    today: LocalDate,
    onPick: (AttendanceHistoryEntry) -> Unit
) {
    val esLaDeHoy = semana.contains(today)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Text(
            text = "${semana.start.format(SoloDia)}–${semana.end.format(SoloDia)}",
            modifier = Modifier.width(46.dp),
            color = if (esLaDeHoy) ScheduleAccent else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.5.sp,
            fontWeight = if (esLaDeHoy) FontWeight.Bold else FontWeight.Normal
        )
        /*
         * Cuadros limpios, sin la fecha escrita dentro.
         *
         * El numero del dia no cabe con dignidad en algo de este tamaño y competia con el
         * color, que es lo que de verdad se lee de un vistazo. La semana ya esta a la
         * izquierda, y tocar un cuadro abre su clase con la fecha entera.
         */
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            semana.entries.forEach { entrada ->
                val esHoy = entrada.date == today
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(entrada.status.cuadro())
                        .then(
                            if (esHoy) {
                                Modifier.border(1.5.dp, ScheduleAccent, RoundedCornerShape(5.dp))
                            } else {
                                Modifier
                            }
                        )
                        .clickable { onPick(entrada) }
                )
            }
        }
    }
}

private fun formatoDeHora(minuto: Int): String = "%02d:%02d".format(minuto / 60, minuto % 60)
