@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_schedule.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EventRepeat
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.utils.performSafely
import com.unistack.app.feature_schedule.domain.AttendanceHistoryEntry
import com.unistack.app.feature_schedule.domain.AttendanceSummary
import com.unistack.app.feature_schedule.domain.AttendanceWeek
import com.unistack.app.feature_schedule.domain.ClassAbsenceReason
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.unistack.app.core.utils.Textos

private val AppLocale: Locale get() = Locale.getDefault()
private val DiaMes: DateTimeFormatter get() = DateTimeFormatter.ofPattern("d MMM", AppLocale)
private val DiaSemanaYfecha: DateTimeFormatter get() = DateTimeFormatter.ofPattern(
    if (AppLocale.language == "en") "EEE, MMMM d" else "EEE d 'de' MMMM",
    AppLocale
)
private val DiaEntero: DateTimeFormatter get() = DateTimeFormatter.ofPattern(
    if (AppLocale.language == "en") "EEEE, MMMM d" else "EEEE d 'de' MMMM",
    AppLocale
)

private fun String.conMayuscula(): String = replaceFirstChar { it.titlecase(AppLocale) }

internal fun formatoDeHora(minuto: Int, en24: Boolean): String {
    val h = minuto / 60
    val m = minuto % 60
    if (en24) return "%02d:%02d".format(h, m)
    val h12 = (h % 12).takeIf { it != 0 } ?: 12
    val sufijo = if (AppLocale.language == "en") {
        if (h < 12) "AM" else "PM"
    } else {
        if (h < 12) "a. m." else "p. m."
    }
    return "%d:%02d %s".format(h12, m, sufijo)
}

/** «15:00 – 20:00», la franja de una clase. */
internal fun AttendanceHistoryEntry.franja(en24: Boolean): String =
    formatoDeHora(session.startMinute, en24) + " – " + formatoDeHora(session.endMinute, en24)

// ================================================================== la rueda

/**
 * La rueda de un estado: **la misma que llena el gesto en «Ponerse al dia»**.
 *
 * El primer rediseño codificaba el estado en siluetas —trebol, rafaga, pildora, rombo— y se
 * veia raro: cuatro formas que habia que aprender para una cosa que la app ya dice con un
 * visto y una equis desde que se marca. Asi que aqui va lo mismo que alli: verde con visto,
 * rojo con equis, ambar con guion para la cancelada, azul con la flecha para la reprogramada.
 * Lo que no esta marcado es un aro vacio, y lo que no ha llegado, un aro punteado.
 */
@Composable
internal fun RuedaDeEstado(
    status: ClassAttendanceStatus,
    tamano: Dp,
    modifier: Modifier = Modifier,
    /** Todavia no ha pasado: aro punteado, sin estado que dar. */
    porVenir: Boolean = false
) {
    val color = status.attendanceColor()
    val aro = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
    val trazo = (tamano.value * 0.075f).coerceAtLeast(1.5f).dp
    when {
        porVenir && status == ClassAttendanceStatus.PENDING -> Box(
            modifier = modifier
                .size(tamano)
                .drawBehind {
                    drawCircle(
                        color = aro.copy(alpha = 0.22f),
                        radius = (size.minDimension - trazo.toPx()) / 2f,
                        style = Stroke(
                            width = trazo.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(trazo.toPx() * 2.2f, trazo.toPx() * 1.8f))
                        )
                    )
                }
        )
        color == null -> Box(
            modifier = modifier
                .size(tamano)
                .clip(CircleShape)
                .border(trazo, aro, CircleShape)
        )
        else -> Box(
            modifier = modifier
                .size(tamano)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (status) {
                    ClassAttendanceStatus.ATTENDED -> Icons.Rounded.Check
                    ClassAttendanceStatus.ABSENT -> Icons.Rounded.Close
                    ClassAttendanceStatus.CANCELLED -> Icons.Rounded.Remove
                    else -> Icons.Rounded.EventRepeat
                },
                contentDescription = status.legendName(),
                tint = contentColorOn(color),
                modifier = Modifier.size(tamano * 0.52f)
            )
        }
    }
}

// ================================================================== el hero

/**
 * La cabecera del historial: un contenedor tonal entero, del color de como vas.
 *
 * **El color rellena, no bordea.** Habia una tarjeta neutra con borde y una cifra; ahora el
 * bloque entero es verde, ambar o rojo segun lo que quede, y la cifra vive dentro. A la
 * derecha, las faltas gastadas en el anillo ondulado de M3E, que ondula mas cuanto mas cerca
 * del tope. Debajo, las cuentas en pildoras y los dos mandos en un grupo conectado.
 *
 * Sin tope de faltas no se promete ninguno: la cifra es el porcentaje y el anillo, las
 * asistidas sobre las decididas.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HeroDeAsistencia(
    summary: AttendanceSummary,
    /** Clases que ya pasaron, marcadas o no: lo que se «dio». */
    dadas: Int,
    sinMarcar: Int,
    absenceLimit: Int?,
    onLimitClick: () -> Unit,
    onMarcarLoQueFalta: () -> Unit,
    modifier: Modifier = Modifier
) {
    val restantes = summary.remainingAbsences
    val oscuro = LocalIsDarkTheme.current
    // Verde, ambar o rojo segun como vayas; sin ningun dato, la superficie neutra.
    val tono: TonoDeAsistencia? = when {
        restantes != null -> when {
            summary.atLimit -> tonoDeAsistencia(RojoProfundo, RojoPalido, oscuro)
            summary.oneLeft -> tonoDeAsistencia(AmbarProfundo, AmbarPalido, oscuro)
            else -> tonoDeAsistencia(VerdeProfundo, VerdePalido, oscuro)
        }
        summary.rate == null -> null
        summary.rate >= 80 -> tonoDeAsistencia(VerdeProfundo, VerdePalido, oscuro)
        summary.rate >= 60 -> tonoDeAsistencia(AmbarProfundo, AmbarPalido, oscuro)
        else -> tonoDeAsistencia(RojoProfundo, RojoPalido, oscuro)
    }
    val contenedor = tono?.contenedor ?: MaterialTheme.colorScheme.surfaceContainerHigh
    val tinta = tono?.sobre ?: MaterialTheme.colorScheme.onSurface
    val fraccion = when {
        summary.absenceLimit != null && summary.absenceLimit > 0 ->
            (summary.absent.toFloat() / summary.absenceLimit).coerceIn(0f, 1f)
        summary.rate != null -> summary.rate / 100f
        else -> 0f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(contenedor)
            .padding(start = 22.dp, end = 22.dp, top = 22.dp, bottom = 18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (restantes != null) {
                        stringResource(R.string.attendance_history_you_have_left)
                    } else {
                        stringResource(R.string.attendance_history_attendance)
                    }.uppercase(AppLocale),
                    color = tinta.copy(alpha = 0.78f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.14.em
                )
                Text(
                    text = when {
                        restantes != null -> "$restantes"
                        summary.rate != null -> "${summary.rate}%"
                        else -> "—"
                    },
                    color = tinta,
                    fontSize = 58.sp,
                    lineHeight = 58.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.04).em,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = when {
                        restantes != null -> stringResource(R.string.attendance_history_of_absences, summary.absenceLimit ?: 0)
                        summary.decided > 0 -> stringResource(R.string.attendance_history_over_classes, summary.decided)
                        else -> stringResource(R.string.attendance_history_empty)
                    },
                    color = tinta.copy(alpha = 0.78f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            AnilloDeFaltas(
                fraccion = fraccion,
                color = tinta,
                tinta = tinta,
                arriba = if (summary.absenceLimit != null) {
                    "${summary.absent}/${summary.absenceLimit}"
                } else {
                    "${summary.attended}/${summary.decided}"
                },
                abajo = if (summary.absenceLimit != null) {
                    stringResource(R.string.attendance_history_ring_spent)
                } else {
                    stringResource(R.string.attendance_history_ring_attended)
                }
            )
        }

        FlowRow(
            modifier = Modifier.padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isEn = AppLocale.language == "en"
            fun plural(n: Int, uno: String, varios: String) = "$n " + if (n == 1) uno else varios
            Pildora(plural(summary.absent, Textos.get(R.string.schedule_falta_2), Textos.get(R.string.schedule_faltas)), tinta)
            Pildora(plural(summary.attended, Textos.get(R.string.schedule_asistencia), Textos.get(R.string.schedule_asistencias)), tinta)
            Pildora(plural(dadas, Textos.get(R.string.schedule_clase_dada), Textos.get(R.string.schedule_clases_dadas)), tinta)
            if (sinMarcar > 0) {
                Pildora(plural(sinMarcar, Textos.get(R.string.schedule_sin_marcar), Textos.get(R.string.schedule_sin_marcar)), tinta)
            }
            if (summary.streak > 1) {
                Pildora(Textos.get(R.string.schedule_seguidas, summary.streak), tinta)
            }
        }

        /*
         * Los dos mandos en un grupo conectado de M3E: se tocan, y el pulsado se ensancha
         * mientras el vecino cede. Es la misma interaccion que la app ya usa en Materias.
         */
        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            val mandos = buildList {
                add(
                    (if (absenceLimit == null) {
                        stringResource(R.string.attendance_history_set_limit)
                    } else {
                        stringResource(R.string.attendance_history_change_limit_short)
                    }) to onLimitClick
                )
                if (sinMarcar == 1) add(stringResource(R.string.attendance_history_mark_pending_one) to onMarcarLoQueFalta)
                if (sinMarcar > 1) add(stringResource(R.string.attendance_history_mark_pending_many, sinMarcar) to onMarcarLoQueFalta)
            }
            mandos.forEachIndexed { indice, (rotulo, accion) ->
                val interaccion = remember { MutableInteractionSource() }
                ToggleButton(
                    checked = false,
                    onCheckedChange = { accion() },
                    shapes = when {
                        mandos.size == 1 -> ButtonGroupDefaults.connectedLeadingButtonShapes(
                            shape = CircleShape,
                            pressedShape = CircleShape,
                            checkedShape = CircleShape
                        )
                        indice == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        else -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    },
                    colors = ToggleButtonDefaults.toggleButtonColors(
                        containerColor = tinta.copy(alpha = 0.16f),
                        contentColor = tinta
                    ),
                    interactionSource = interaccion,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    modifier = Modifier.weight(1f).animateWidth(interaccion)
                ) {
                    Text(rotulo, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
private fun Pildora(texto: String, tinta: Color) {
    Text(
        text = texto,
        color = tinta,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(CircleShape)
            .background(tinta.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

/**
 * Las faltas gastadas, en el indicador circular ondulado de M3E.
 *
 * La onda crece con lo gastado: cerca del tope se agita, y con cero es casi un aro liso.
 */
@Composable
private fun AnilloDeFaltas(fraccion: Float, color: Color, tinta: Color, arriba: String, abajo: String) {
    val grosor = with(LocalDensity.current) { Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round) }
    Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        CircularWavyProgressIndicator(
            progress = { fraccion },
            modifier = Modifier.size(96.dp),
            color = color,
            trackColor = tinta.copy(alpha = 0.22f),
            stroke = grosor,
            trackStroke = grosor,
            amplitude = { 0.25f + 0.75f * fraccion },
            wavelength = 16.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(arriba, color = tinta, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 15.sp)
            Text(
                abajo.uppercase(AppLocale),
                color = tinta.copy(alpha = 0.75f),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.1.em,
                lineHeight = 11.sp
            )
        }
    }
}

// ================================================================== el mapa

/** El rótulo pequeño en versales que separa los bloques. */
@Composable
internal fun Rotulo(texto: String, detalle: String? = null, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        Text(
            text = texto.uppercase(AppLocale),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.14.em,
            modifier = Modifier.weight(1f)
        )
        if (detalle != null) {
            Text(
                text = detalle,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * El periodo entero de un vistazo: una fila por semana, una rueda por clase.
 *
 * Las semanas van de la primera a la ultima, como se lee un semestre. Las que aun no han
 * llegado van con el aro punteado, para que se vea cuanto queda sin prometer nada de ello.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MapaDelPeriodo(
    semanas: List<AttendanceWeek>,
    porVenir: List<AttendanceHistoryEntry>,
    today: LocalDate,
    onPick: (AttendanceHistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val futurasPorSemana = porVenir.groupBy { it.date.with(java.time.DayOfWeek.MONDAY) }
        .map { (lunes, dentro) -> AttendanceWeek(start = lunes, entries = dentro.sortedBy { it.date }) }
    val filas = (semanas.sortedBy { it.start } + futurasPorSemana.sortedBy { it.start })
        .groupBy { it.start }
        .map { (lunes, iguales) -> AttendanceWeek(lunes, iguales.flatMap { it.entries }.sortedBy { it.date }, iguales.firstNotNullOfOrNull { it.number }) }
        .sortedBy { it.start }
    val primera = filas.firstOrNull()?.number

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        filas.forEachIndexed { indice, semana ->
            val numero = semana.number ?: (primera ?: 1) + indice
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "S$numero",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(26.dp)
                )
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    semana.entries.forEach { entrada ->
                        val futura = entrada.date.isAfter(today)
                        RuedaDeEstado(
                            status = entrada.status,
                            tamano = 26.dp,
                            porVenir = futura,
                            modifier = if (futura) Modifier else Modifier.clip(CircleShape).clickable { onPick(entrada) }
                        )
                    }
                }
                Text(
                    text = semana.entries.first().date.format(DiaMes),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Spacer(Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.outlineVariant))
        FlowRow(
            modifier = Modifier.padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                ClassAttendanceStatus.ATTENDED,
                ClassAttendanceStatus.ABSENT,
                ClassAttendanceStatus.CANCELLED,
                ClassAttendanceStatus.RESCHEDULED,
                ClassAttendanceStatus.PENDING
            ).forEach { estado ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RuedaDeEstado(status = estado, tamano = 14.dp)
                    Text(
                        text = estado.legendName().conMayuscula(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RuedaDeEstado(status = ClassAttendanceStatus.PENDING, tamano = 14.dp, porVenir = true)
                Text(
                    text = stringResource(R.string.attendance_history_upcoming),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ================================================================== las filas

/**
 * Una clase del historial: la rueda, la fecha en grande y lo que se anoto de ella.
 *
 * Se toca y se corrige: es lo que no se podia hacer. Las que aun no han pasado no se tocan
 * —marcar por adelantado es la puerta a un historial inventado— y van atenuadas.
 */
@Composable
internal fun FilaDelHistorial(
    entrada: AttendanceHistoryEntry,
    occurrence: ClassOccurrence?,
    en24: Boolean,
    porVenir: Boolean,
    onPick: (AttendanceHistoryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val extras = buildList {
        add(entrada.franja(en24))
        if (occurrence != null) {
            resumenDeDetalle(occurrence.status, occurrence.modality, occurrence.absenceReason, "")?.let { add(it) }
            if (occurrence.note.isNotBlank()) add(stringResource(R.string.attendance_history_with_note))
        }
    }
    val tono = entrada.status.attendanceColor() ?: MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(if (porVenir) Modifier.alpha(0.72f) else Modifier.clickable { onPick(entrada) })
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        RuedaDeEstado(status = entrada.status, tamano = 34.dp, porVenir = porVenir)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entrada.date.format(DiaSemanaYfecha).conMayuscula(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = extras.joinToString(" · "),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = if (porVenir) {
                stringResource(R.string.attendance_history_upcoming)
            } else {
                entrada.status.legendName().conMayuscula()
            },
            color = if (porVenir) MaterialTheme.colorScheme.onSurfaceVariant else tono,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.02.em
        )
    }
}

// ================================================================== la hoja

/**
 * La hoja que corrige una clase: que paso, y si quieres, el detalle.
 *
 * Los cuatro estados en un grupo conectado de M3E, con su rueda encima del nombre; el
 * pulsado se ensancha y toma su color. El detalle —motivo si faltaste, modalidad si fuiste, y
 * la nota— es el mismo [AttendanceDetail] de siempre y aparece solo cuando hay un estado:
 * con el estado basta, y cada pregunta obligatoria es una razon mas para no marcar nada.
 */
@Composable
internal fun HojaDeClase(
    entrada: AttendanceHistoryEntry,
    occurrence: ClassOccurrence?,
    en24: Boolean,
    onSave: (ClassAttendanceStatus, ClassModality, ClassAbsenceReason?, String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = LocalHapticFeedback.current
    var estado by remember { mutableStateOf(occurrence?.status ?: entrada.status) }
    var modalidad by remember { mutableStateOf(occurrence?.modality ?: ClassModality.IN_PERSON) }
    var motivo by remember { mutableStateOf(occurrence?.absenceReason) }
    var nota by remember { mutableStateOf(occurrence?.note.orEmpty()) }
    val aula = entrada.session.location.split('•', limit = 2).first().trim()
        .ifBlank { stringResource(R.string.schedule_detail_no_room) }
    val estados = listOf(
        ClassAttendanceStatus.ATTENDED,
        ClassAttendanceStatus.ABSENT,
        ClassAttendanceStatus.CANCELLED,
        ClassAttendanceStatus.RESCHEDULED
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                RuedaDeEstado(status = estado, tamano = 44.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = entrada.date.format(DiaEntero).conMayuscula(),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.01).em
                    )
                    Text(
                        text = entrada.franja(en24) + " · " + aula,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Rotulo(stringResource(R.string.attendance_history_what_happened))
                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    estados.forEachIndexed { indice, opcion ->
                        val interaccion = remember { MutableInteractionSource() }
                        val elegido = estado == opcion
                        val color = opcion.attendanceColor() ?: MaterialTheme.colorScheme.primary
                        ToggleButton(
                            checked = elegido,
                            onCheckedChange = {
                                haptics.performSafely(HapticFeedbackType.SegmentTick)
                                // Volver a tocar el elegido lo deshace: equivocarse no es definitivo.
                                estado = if (elegido) ClassAttendanceStatus.PENDING else opcion
                                if (estado != ClassAttendanceStatus.ABSENT) motivo = null
                            },
                            shapes = when (indice) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                estados.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            },
                            colors = ToggleButtonDefaults.toggleButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                checkedContainerColor = color,
                                checkedContentColor = contentColorOn(color)
                            ),
                            interactionSource = interaccion,
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 72.dp)
                                .animateWidth(interaccion)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = when (opcion) {
                                        ClassAttendanceStatus.ATTENDED -> Icons.Rounded.Check
                                        ClassAttendanceStatus.ABSENT -> Icons.Rounded.Close
                                        ClassAttendanceStatus.CANCELLED -> Icons.Rounded.Remove
                                        else -> Icons.Rounded.EventRepeat
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = opcion.legendName().conMayuscula(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            AttendanceDetail(
                status = estado,
                modality = modalidad,
                absenceReason = motivo,
                note = nota,
                onModalityChange = { modalidad = it },
                onReasonChange = { motivo = it },
                onNoteChange = { nota = it }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = {
                        haptics.performSafely(HapticFeedbackType.Confirm)
                        onSave(estado, modalidad, motivo, nota.trim())
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = UniStackButtonDefaults.PrimaryHeight),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScheduleAccent,
                        contentColor = contentColorOn(ScheduleAccent)
                    )
                ) {
                    Text(stringResource(R.string.action_save), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
                Text(
                    text = stringResource(R.string.attendance_history_state_is_enough),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ================================================================== los cortes

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
