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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.feature_schedule.domain.AttendanceHistoryEntry
import com.unistack.app.feature_schedule.domain.AttendanceSummary
import com.unistack.app.feature_schedule.domain.AttendanceWeek
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val AppLocale: Locale get() = Locale.getDefault()
private val DiaMes: DateTimeFormatter get() = DateTimeFormatter.ofPattern("d MMM", AppLocale)
private val SoloDia: DateTimeFormatter get() = DateTimeFormatter.ofPattern("d", AppLocale)
private val Mes: DateTimeFormatter get() = DateTimeFormatter.ofPattern("MMMM", AppLocale)
private val DiaSemanaYfecha: DateTimeFormatter get() = DateTimeFormatter.ofPattern(
    if (AppLocale.language == "en") "EEE, MMMM d" else "EEE d 'de' MMMM",
    AppLocale
)

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
    weeks: List<AttendanceWeek>,
    today: LocalDate,
    onLimitClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** Cómo se llama lo que se está mirando: «el periodo», «el Corte 2». */
    scopeName: String = stringResource(R.string.attendance_history_term),
    /**
     * El tope de la materia, que existe aunque no se esté aplicando.
     *
     * Mirando un corte suelto, [summary] llega sin tope a propósito —contar las faltas del
     * corte contra un tope del semestre diría «te quedan 5» cuando llevas 6 gastadas— pero el
     * botón sigue teniendo que decir la verdad: el tope está puesto y se puede cambiar.
     */
    absenceLimit: Int? = summary.absenceLimit
) {
    val restantes = summary.remainingAbsences
    /*
     * El mapa esta siempre, pero pequeño.
     *
     * Enseñarlo desplegado obliga a pasar por encima de el cada vez que vienes al detalle, y
     * esconderlo del todo lo hace facil de no descubrir nunca. La tira es el mismo mapa en su
     * forma corta: ocupa una linea y se abre a la cuadricula por semanas cuando la tocas.
     */
    var mapaAbierto by rememberSaveable { mutableStateOf(false) }
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
                        text = if (restantes != null) stringResource(R.string.attendance_history_you_have_left) else stringResource(R.string.attendance_history_attendance),
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
                                text = stringResource(R.string.attendance_history_of_absences, summary.absenceLimit ?: 0),
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
            /*
             * Un boton pequeño, no un texto de color.
             *
             * Pintarlo con el acento lo hacia el unico morado de la tarjeta y se llevaba la
             * vista antes que la cifra, que es lo que se viene a leer. Con fondo propio se
             * nota que se toca sin necesidad de gritar.
             */
            Surface(
                onClick = onLimitClick,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest
            ) {
                Text(
                    text = if (absenceLimit == null) {
                        stringResource(R.string.attendance_history_set_limit)
                    } else {
                        stringResource(R.string.attendance_history_change_limit, absenceLimit.toString())
                    },
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val pasadas = entries.filter { !it.date.isAfter(today) }
            if (pasadas.isNotEmpty()) {
                Spacer(Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outlineVariant))
                if (mapaAbierto) {
                    AlternaMapa(
                        texto = stringResource(R.string.attendance_history_by_week, scopeName.replaceFirstChar { it.uppercase() }),
                        abierto = true,
                        onClick = { mapaAbierto = false }
                    )
                    RejillaPorSemanas(weeks = weeks, today = today)
                    Leyenda(pasadas)
                } else {
                    TiraDeClases(
                        pasadas = pasadas,
                        onClick = { mapaAbierto = true }
                    )
                    AlternaMapa(
                        texto = stringResource(R.string.attendance_history_view_by_weeks, scopeName),
                        abierto = false,
                        onClick = { mapaAbierto = true }
                    )
                }
            }
        }
    }
}

/**
 * Mirar el semestre entero, o un corte suelto.
 *
 * En la universidad el reglamento cuenta faltas por corte tan a menudo como por semestre, y
 * hasta ahora la pantalla solo sabía sumar el periodo completo: al llegar a noviembre, dos
 * faltas de septiembre ya perdonadas seguían pesando en la única cifra que había.
 *
 * Solo aparece cuando el esquema tiene fechas y más de un corte: sin eso no hay forma de decir
 * a qué corte pertenece una clase, y una fila de botones que no puede cumplir es peor que nada.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AlcanceDeCortes(
    cortes: List<CutScope>,
    seleccionado: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FichaDeAlcance(
                texto = stringResource(R.string.attendance_history_whole_term),
                activa = seleccionado == null,
                onClick = { onSelect(null) }
            )
            cortes.forEach { corte ->
                FichaDeAlcance(
                    texto = corte.name,
                    activa = seleccionado == corte.id,
                    onClick = { onSelect(corte.id) }
                )
            }
        }
        // El tramo debajo, y solo del elegido: con los cuatro escritos a la vez la fila se
        // convierte en un calendario y deja de leerse como un interruptor.
        cortes.firstOrNull { it.id == seleccionado }?.range?.let { tramo ->
            Text(
                text = tramo,
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.sp
            )
        }
    }
}

/** Un corte, tal y como lo necesita la fila: nombre, identidad y tramo ya escrito. */
internal data class CutScope(val id: String, val name: String, val range: String?)

@Composable
private fun FichaDeAlcance(texto: String, activa: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (activa) ScheduleAccent else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = if (activa) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = if (activa) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/** La frase de apoyo bajo la cifra, que cambia según lo que se pueda afirmar. */
@Composable
private fun leyendaDeApoyo(summary: AttendanceSummary): String = when {
    summary.decided == 0 -> stringResource(R.string.attendance_history_empty)
    summary.atLimit -> stringResource(R.string.attendance_history_none_left, summary.absent, summary.absenceLimit ?: 0)
    summary.oneLeft -> stringResource(R.string.attendance_history_one_left, summary.absent, summary.absenceLimit ?: 0)
    summary.absenceLimit != null ->
        stringResource(R.string.attendance_history_held_summary, summary.absent, summary.attended, summary.decided)
    summary.tooFewToTrust -> {
        val classWord = if (Locale.getDefault().language == "en") {
            if (summary.decided == 1) "class" else "classes"
        } else {
            if (summary.decided == 1) "clase" else "clases"
        }
        stringResource(R.string.attendance_history_too_few, summary.decided, classWord)
    }
    else -> stringResource(R.string.attendance_history_simple_summary, summary.attended, summary.absent)
}

@Composable
private fun RachaChip(racha: Int) {
    Surface(
        shape = CircleShape,
        color = ScheduleAccent.copy(alpha = 0.16f)
    ) {
        Text(
            text = if (Locale.getDefault().language == "en") "🔥 $racha in a row" else "🔥 $racha seguidas",
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
private fun TiraDeClases(pasadas: List<AttendanceHistoryEntry>, onClick: () -> Unit) {
    val ordenadas = pasadas.sortedBy { it.date }
    // Con un semestre entero la tira no cabe; las últimas veinte cuentan la historia igual.
    val visibles = ordenadas.takeLast(20)
    /*
     * El relleno no sobra: sin el, el cuadro sale cortado.
     *
     * La columna entera se recorta con esquinas de 10 dp para que el toque tenga forma, y con
     * una sola clase el cuadro cae justo en la esquina de abajo a la izquierda: la curva le
     * mordia un pico. Separandolo del borde, la esquina redondea aire en vez de dato.
     */
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        /*
         * «Tus 1 clase» no lo dice nadie.
         *
         * El rotulo se componia metiendo el numero delante siempre, asi que el singular salia
         * con el uno pegado. Cuando es una sola no hace falta contarla: se nombra.
         */
        Rotulo(
            text = when {
                visibles.size < ordenadas.size -> if (Locale.getDefault().language == "en") "Your last ${visibles.size} classes" else "Tus últimas ${visibles.size} clases"
                ordenadas.size == 1 -> if (Locale.getDefault().language == "en") "Your first class" else "Tu primera clase"
                else -> if (Locale.getDefault().language == "en") "Your ${ordenadas.size} classes" else "Tus ${ordenadas.size} clases"
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

/**
 * El periodo entero, una fila por semana.
 *
 * Es el mismo dato que la tira, ordenado: cada fila es una semana y su número la sitúa en el
 * semestre. Aquí los cuadros no llevan fecha escrita a propósito —no cabe— y para eso está el
 * detalle de abajo, que es donde se va a mirar cuál fue cuál.
 */
@Composable
private fun RejillaPorSemanas(weeks: List<AttendanceWeek>, today: LocalDate) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        weeks.forEach { semana ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Text(
                    text = semana.number?.toString() ?: semana.start.format(SoloDia),
                    modifier = Modifier.width(24.dp),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 9.5.sp,
                    textAlign = TextAlign.End
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    semana.entries.forEach { entrada ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(entrada.status.cuadro())
                                .then(
                                    if (entrada.date == today) {
                                        Modifier.border(
                                            1.5.dp,
                                            ScheduleAccent,
                                            RoundedCornerShape(4.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

/** La línea que abre y cierra el mapa, con su flecha. */
@Composable
private fun AlternaMapa(texto: String, abierto: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = if (abierto) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
            contentDescription = if (abierto) (if (Locale.getDefault().language == "en") "Close map" else "Cerrar el mapa") else (if (Locale.getDefault().language == "en") "Open map" else "Abrir el mapa"),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(17.dp)
        )
    }
}

/** El rótulo pequeño en versales que separa los bloques, como en el diseño aprobado. */
@Composable
private fun Rotulo(text: String) {
    Text(
        text = text.uppercase(AppLocale),
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (upcoming.isNotEmpty()) {
            EtiquetaDeGrupo(if (Locale.getDefault().language == "en") "Upcoming" else "Próxima")
            upcoming.take(2).forEach { entrada ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .clickable { onPick(entrada) }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
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

        weeks.forEach { semana ->
            /*
             * «Semana 8 · 24–30 ago», y no solo el rango.
             *
             * Un rango suelto obliga a situarlo tu: hay que acordarse de en que mes estabas y
             * de por donde va el semestre. El numero de semana es la unidad en la que se
             * piensa un periodo, y la fecha al lado quita cualquier duda de cual es.
             */
            EtiquetaDeGrupo(tituloDeSemana(semana))
            semana.entries.forEach { entrada ->
                FilaDeClase(
                    entrada = entrada,
                    esHoy = entrada.date == today,
                    onPick = onPick
                )
            }
        }
    }
}

@Composable
private fun EtiquetaDeGrupo(texto: String) {
    // El sangrado horizontal lo pone la lista entera; aquí solo el aire de arriba.
    Box(modifier = Modifier.padding(top = 5.dp, bottom = 1.dp)) {
        Rotulo(texto)
    }
}

/**
 * Una clase, con su fecha y su estado escritos.
 *
 * Antes cada semana era una fila y sus clases unos cuadros de color. Con eso, «24–30» obligaba
 * a deducir de que dia era cada cuadro, y con dos clases en la misma semana no habia forma de
 * saber cual era cual. El mapa denso ya esta arriba, en la tira: aqui abajo lo que hace falta
 * es el detalle, y el detalle es la fecha.
 */
@Composable
private fun FilaDeClase(
    entrada: AttendanceHistoryEntry,
    esHoy: Boolean,
    onPick: (AttendanceHistoryEntry) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable { onPick(entrada) }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(entrada.status.cuadro())
        )
        Text(
            text = entrada.date.format(DiaSemanaYfecha).replaceFirstChar { it.titlecase(AppLocale) },
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = if (esHoy) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = entrada.status.legendName().replaceFirstChar { it.titlecase(AppLocale) },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.5.sp
        )
    }
}

private fun formatoDeHora(minuto: Int): String = "%02d:%02d".format(minuto / 60, minuto % 60)

/** «Semana 8 · 24–30 ago», o solo el rango si no hay periodo del que contar semanas. */
private fun tituloDeSemana(semana: AttendanceWeek): String {
    val rango = "${semana.start.format(SoloDia)}–${semana.end.format(DiaMes)}"
    return semana.number?.let { if (Locale.getDefault().language == "en") "Week $it · $rango" else "Semana $it · $rango" } ?: rango
}
