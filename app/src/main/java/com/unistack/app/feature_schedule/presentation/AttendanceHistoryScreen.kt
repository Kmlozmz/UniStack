package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.reacomodoDeLista
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.AttendanceHistoryEntry
import com.unistack.app.feature_schedule.domain.ClassAbsenceReason
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.SubjectAttendanceHistory
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_user.domain.GradingCutScheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * El historial de asistencia de una materia, rediseñado entero.
 *
 * **Cada clase pasada se toca y se corrige.** Antes, tocar una fila cerraba esta pantalla y
 * abría la ficha de la clase en el calendario, que ya no marca asistencia: el historial
 * parecía de solo lectura porque, en la práctica, lo era. Ahora la fila abre [HojaDeClase]
 * aquí mismo, con los cuatro estados y el detalle, y se guarda sin salir.
 *
 * La forma es la del diseño aprobado: un hero tonal del color de cómo vas, el periodo entero
 * de un vistazo con una rueda por clase, y la lista con la fecha en grande. Sin una sola
 * franja de color al costado.
 *
 * Dos decisiones que van con el diseño y no con el usuario:
 * - **Solo lo pasado se edita.** Marcar por adelantado que faltarás es la puerta a un
 *   historial inventado; lo que viene sale, pero no se toca.
 * - **Cancelada y reprogramada no gastan falta.** Es lo que ya hacía el dominio: no fuiste,
 *   pero no fue cosa tuya.
 */
@Composable
internal fun AttendanceHistoryScreen(
    subject: Subject,
    sessions: List<ClassSession>,
    occurrences: List<ClassOccurrence>,
    term: AcademicTerm?,
    cutScheme: GradingCutScheme?,
    /** El tope del reglamento, uno para todas las materias. */
    absenceLimit: Int?,
    breaks: List<AcademicBreak>,
    onDismiss: () -> Unit,
    onSetAbsenceLimit: (Int?) -> Unit,
    onSave: (AttendanceHistoryEntry, ClassAttendanceStatus, ClassModality, ClassAbsenceReason?, String) -> Unit
) {
    val locale = Locale.getDefault()
    val en24 = LocalAccessibilityPreferences.current.use24HourTime
    var pidiendoTope by remember { mutableStateOf(false) }
    var poniendoseAlDia by remember { mutableStateOf(false) }
    var ayudaVisible by remember { mutableStateOf(false) }
    var editando by remember { mutableStateOf<AttendanceHistoryEntry?>(null) }

    val hoy = LocalDate.now()
    val entries = remember(sessions, occurrences, term, breaks) {
        SubjectAttendanceHistory.build(
            sessions = sessions,
            occurrences = occurrences,
            today = LocalDate.now(),
            termStart = term?.start,
            termEnd = term?.plannedEnd,
            breaks = breaks.map { it.range }
        )
    }
    val porClave = remember(occurrences) { occurrences.associateBy { it.sessionId to it.dateEpochDay } }
    fun ocurrenciaDe(e: AttendanceHistoryEntry) = porClave[e.session.id to e.date.toEpochDay()]

    /*
     * El semestre entero, o un corte suelto.
     *
     * Solo se ofrece si el esquema tiene fechas y mas de un corte: sin fechas no hay forma de
     * decir a que corte pertenece una clase.
     */
    val desdeEl = stringResource(R.string.schedule_from_date)
    val hastaEl = stringResource(R.string.schedule_to_date)
    val cortes = remember(cutScheme, term) {
        val esquema = cutScheme?.takeIf { it.hasDates && it.cuts.size > 1 } ?: return@remember emptyList()
        esquema.cuts.sortedBy { it.order }.map { corte ->
            val (desde, hasta) = esquema.rangeFor(corte.id, term?.start, term?.plannedEnd)
            CutScope(
                id = corte.id,
                name = corte.name,
                range = when {
                    desde != null && hasta != null -> desde.diaMes() + " → " + hasta.diaMes()
                    desde != null -> desdeEl + desde.diaMes()
                    hasta != null -> hastaEl + hasta.diaMes()
                    else -> null
                }
            )
        }
    }
    var alcanceElegido by rememberSaveable(subject.id) { mutableStateOf<String?>(null) }
    val alcance = alcanceElegido?.takeIf { id -> cortes.any { it.id == id } }
    val enAlcance = remember(entries, alcance, cutScheme) {
        val id = alcance
        if (id == null || cutScheme == null) entries else entries.filter { cutScheme.cutForDate(it.date)?.id == id }
    }
    // Mirando un corte, el tope del semestre no se aplica: «te quedan 5 de 6» contando solo
    // las faltas de noviembre seria mentira si en septiembre ya gastaste cuatro.
    val summary = remember(enAlcance, absenceLimit, alcance) {
        SubjectAttendanceHistory.summarize(
            entries = enAlcance,
            absenceLimit = if (alcance == null) absenceLimit else null
        )
    }
    val semanas = remember(enAlcance, term) { SubjectAttendanceHistory.byWeek(enAlcance, hoy, term?.start) }
    val porVenir = remember(enAlcance) { SubjectAttendanceHistory.upcoming(enAlcance, hoy) }
    val pasadas = remember(enAlcance) {
        enAlcance.filter { !it.date.isAfter(hoy) }
            .sortedWith(compareByDescending<AttendanceHistoryEntry> { it.date }.thenByDescending { it.session.startMinute })
    }
    val sinMarcar = remember(entries) { SubjectAttendanceHistory.pendingToCatchUp(entries, LocalDateTime.now()) }
    val rango = remember(enAlcance) {
        val fechas = enAlcance.map { it.date }
        val desde = fechas.minOrNull()
        val hasta = fechas.maxOrNull()
        if (desde == null || hasta == null) null else desde.diaMes() + " – " + hasta.diaMes()
    }
    // «Jueves · 15:00 – 20:00»: los dias de la materia y la franja de su primera clase.
    val subtitulo = remember(sessions, en24) {
        val dias = sessions.flatMap { it.daysOfWeek }.distinct().sorted()
            .joinToString(", ") { DayOfWeek.of(it).getDisplayName(TextStyle.FULL, locale).replaceFirstChar { c -> c.titlecase(locale) } }
        val primera = sessions.minByOrNull { it.startMinute }
        listOfNotNull(
            dias.takeIf { it.isNotBlank() },
            primera?.let { formatoDeHora(it.startMinute, en24) + " – " + formatoDeHora(it.endMinute, en24) }
        ).joinToString(" · ")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UniBackButton(onClick = onDismiss)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = subject.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (subtitulo.isNotBlank()) {
                            Text(
                                text = subtitulo,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    // Toda la pantalla en una frase, para quien la abre por primera vez.
                    IconButton(onClick = { ayudaVisible = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = stringResource(R.string.schedule_how_to_read),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (cortes.isNotEmpty()) {
                        item("cortes") {
                            AlcanceDeCortes(
                                cortes = cortes,
                                seleccionado = alcance,
                                onSelect = { elegido -> alcanceElegido = elegido },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                    item("hero") {
                        HeroDeAsistencia(
                            summary = summary,
                            dadas = pasadas.size,
                            sinMarcar = pasadas.count { it.status == ClassAttendanceStatus.PENDING },
                            absenceLimit = absenceLimit,
                            onLimitClick = { pidiendoTope = true },
                            onMarcarLoQueFalta = {
                                // Con varias, la lista de dos botones por fila; con una, su hoja.
                                if (sinMarcar.size > 1) poniendoseAlDia = true else sinMarcar.firstOrNull()?.let { editando = it }
                            }
                        )
                    }
                    if (enAlcance.isEmpty()) {
                        item("vacio") {
                            Text(
                                text = if (alcance == null) {
                                    stringResource(R.string.schedule_history_empty)
                                } else {
                                    stringResource(R.string.schedule_history_empty_scope) + com.unistack.app.feature_user.domain.Corte.Singular.lowercase()
                                },
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        item("mapa-rotulo") {
                            Rotulo(
                                texto = stringResource(R.string.attendance_history_the_period),
                                detalle = rango,
                                modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
                            )
                        }
                        item("mapa") {
                            MapaDelPeriodo(
                                semanas = semanas,
                                porVenir = porVenir,
                                today = hoy,
                                onPick = { editando = it }
                            )
                        }
                        if (pasadas.isNotEmpty()) {
                            item("pasado-rotulo") {
                                // El detalle dice lo que la fila no puede decir sola: que se toca.
                                Rotulo(
                                    texto = stringResource(R.string.attendance_history_past_classes),
                                    detalle = stringResource(R.string.attendance_history_tap_to_fix),
                                    modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
                                )
                            }
                            items(pasadas, key = { "${it.session.id}:${it.date.toEpochDay()}" }) { entrada ->
                                FilaDelHistorial(
                                    entrada = entrada,
                                    occurrence = ocurrenciaDe(entrada),
                                    en24 = en24,
                                    porVenir = false,
                                    onPick = { editando = it },
                                    modifier = reacomodoDeLista()
                                )
                            }
                        }
                        if (porVenir.isNotEmpty()) {
                            item("venir-rotulo") {
                                Rotulo(
                                    texto = stringResource(R.string.attendance_history_upcoming_section),
                                    modifier = Modifier.padding(top = 20.dp, bottom = 4.dp)
                                )
                            }
                            items(porVenir, key = { "f:${it.session.id}:${it.date.toEpochDay()}" }) { entrada ->
                                FilaDelHistorial(
                                    entrada = entrada,
                                    occurrence = null,
                                    en24 = en24,
                                    porVenir = true,
                                    onPick = {}
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    editando?.let { entrada ->
        HojaDeClase(
            entrada = entrada,
            occurrence = ocurrenciaDe(entrada),
            en24 = en24,
            onSave = { estado, modalidad, motivo, nota ->
                onSave(entrada, estado, modalidad, motivo, nota)
                editando = null
            },
            onDismiss = { editando = null }
        )
    }

    if (ayudaVisible) {
        AttendanceHelpDialog(
            hasLimit = absenceLimit != null,
            hasWeekNumbers = semanas.any { it.number != null },
            hasCuts = cortes.isNotEmpty(),
            onDismiss = { ayudaVisible = false }
        )
    }

    if (pidiendoTope) {
        AbsenceLimitDialog(
            actual = absenceLimit,
            onDismiss = { pidiendoTope = false },
            onConfirm = { limite ->
                onSetAbsenceLimit(limite)
                pidiendoTope = false
            }
        )
    }

    if (poniendoseAlDia) {
        CatchUpSheet(
            pending = sinMarcar,
            subjects = listOf(subject),
            onMark = { entrada, estado -> onSave(entrada, estado, ClassModality.IN_PERSON, null, "") },
            onDismiss = { poniendoseAlDia = false }
        )
    }
}

/** «24 ago», para decir de qué clase habla algo sin escribir la fecha entera. */
private fun LocalDate.diaMes(): String = format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
