@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.unistack.app.core.design.components.EvaluationRing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.SectionLabelStyle
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_schedule.domain.ClassAbsenceReason
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.AttendanceHistoryEntry
import com.unistack.app.feature_schedule.domain.SubjectAttendanceHistory
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_schedule.domain.ClassSession
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
/* Estos nombres describían un color («Green», «Purple») pero devolvían un rol del tema,
   así que mentían en cuanto el acento dejaba de ser verde —es decir, siempre—. Ahora
   nombran el papel que cumplen. Se cayeron dos: SchedulePurple, que era un duplicado
   literal de ScheduleAccent, y SchedulePink, que solo alimentaba la lista de muestras. */
internal val ScheduleAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary
internal val ScheduleRescheduled: Color
    @Composable get() = LocalSectionColors.current.schedule
internal val ScheduleCancelled: Color
    @Composable get() = LocalSectionColors.current.atRisk
internal val ScheduleShape: Shape
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes.medium

private enum class CalendarMode(val label: String) {
    MONTH("Mes"),
    AGENDA("Agenda"),
    LIST("Lista")
}

private enum class ScheduleView {
    TIMETABLE,
    CALENDAR,
    DAY
}

@Composable
fun CalendarScheduleScreen(
    onTaskClick: (String) -> Unit,
    // Crear y editar una clase abren el formulario de materia, que es la misma pantalla que
    // usa Académico. Antes Horario tenía su propio diálogo para la misma entidad y los dos
    // formularios divergían en todo lo que nadie sincronizaba a mano.
    onAddClassClick: () -> Unit,
    onEditSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var identityView by rememberSaveable { mutableStateOf(IdentityScheduleView.TIMETABLE) }
    var selectedEpochDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var selectedSession by remember { mutableStateOf<ClassSession?>(null) }
    var historySubjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var showFullSchedule by rememberSaveable { mutableStateOf(false) }
    var showAgendaMenu by rememberSaveable { mutableStateOf(false) }
    var showAddClassSheet by rememberSaveable { mutableStateOf(false) }
    var agendaCreateKind by remember { mutableStateOf<AgendaCreateKind?>(null) }
    var editingAgendaEvent by remember { mutableStateOf<com.unistack.app.feature_schedule.domain.AgendaEvent?>(null) }

    val selectedDate = LocalDate.ofEpochDay(selectedEpochDay)
    val dayOccurrences = state.occurrences.filter { it.dateEpochDay == selectedEpochDay }

    /*
     * Lo que se quedo sin marcar, de todas las materias a la vez.
     *
     * Se calcula con las mismas reglas que el historial de una —periodo, dias sin clase y el
     * tope de la ventana—, asi que un festivo tampoco cuenta aqui como clase perdida.
     */
    val hoyMismo = LocalDate.now()
    val ahora = LocalDateTime.now()
    val sinMarcarTodas = remember(
        state.sessions, state.occurrences, state.activeTerm, state.breaks
    ) {
        SubjectAttendanceHistory.pendingToCatchUp(
            SubjectAttendanceHistory.build(
                sessions = state.sessions,
                occurrences = state.occurrences,
                today = hoyMismo,
                termStart = state.activeTerm?.start,
                termEnd = state.activeTerm?.plannedEnd,
                breaks = state.breaks.map { it.range }
            ),
            ahora
        )
    }
    var poniendoseAlDiaTodas by remember { mutableStateOf(false) }

    // Materias que existen y no tienen ni un solo bloque en el horario. Si no hay ninguna,
    // «Agregar clase» va directo al formulario y nadie ve un paso de más.
    val subjectsWithoutSchedule = state.subjects.filter { subject ->
        state.sessions.none { it.subjectId == subject.id }
    }
    val startAddClass = {
        if (subjectsWithoutSchedule.isEmpty()) onAddClassClick() else showAddClassSheet = true
    }

    /*
     * Nada hasta que haya datos.
     *
     * El estado inicial de un `stateIn` es una copia vacía, y la pantalla la pintaba como si
     * fuera la respuesta: al entrar en Horario se veía un fotograma de «no hay clases» y acto
     * seguido aparecía todo. Un hueco del color del fondo durante ese fotograma no lo nota
     * nadie; una pantalla que dice que no tienes nada, sí.
     */
    if (!state.loaded) {
        Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    ScheduleIdentityContent(
        view = identityView,
        selectedDate = selectedDate,
        uiState = state,
        onViewChange = { identityView = it },
        onDateSelected = { selectedEpochDay = it.toEpochDay() },
        onSessionClick = { date, session ->
            selectedEpochDay = date.toEpochDay()
            selectedSession = session
        },
        onTaskClick = onTaskClick,
        onAgendaEventClick = { event ->
            editingAgendaEvent = event
            agendaCreateKind = when (event.kind) {
                com.unistack.app.feature_schedule.domain.AgendaEventKind.REMINDER -> AgendaCreateKind.REMINDER
                com.unistack.app.feature_schedule.domain.AgendaEventKind.PERSONAL,
                com.unistack.app.feature_schedule.domain.AgendaEventKind.MEETING -> AgendaCreateKind.PERSONAL
                com.unistack.app.feature_schedule.domain.AgendaEventKind.CUSTOM -> AgendaCreateKind.CUSTOM
            }
        },
        onAddClass = startAddClass,
        onAddEvent = { showAgendaMenu = true },
        onOpenFullSchedule = { showFullSchedule = true },
        pendingCount = sinMarcarTodas.size,
        onCatchUp = { poniendoseAlDiaTodas = true },
        modifier = modifier
    )
    if (showAgendaMenu) {
        AgendaCreateMenuSheet(
            onDismiss = { showAgendaMenu = false },
            onSelect = { kind ->
                showAgendaMenu = false
                editingAgendaEvent = null
                agendaCreateKind = kind
            }
        )
    }
    if (showAddClassSheet) {
        AddClassSheet(
            pendingSubjects = subjectsWithoutSchedule,
            onDismiss = { showAddClassSheet = false },
            onPickSubject = { subjectId ->
                showAddClassSheet = false
                onEditSubjectClick(subjectId)
            },
            onNewSubject = {
                showAddClassSheet = false
                onAddClassClick()
            }
        )
    }
    agendaCreateKind?.let { kind ->
        AgendaComposerSheet(
            kind = kind,
            initialDate = selectedDate,
            subjects = state.subjects,
            existingEvent = editingAgendaEvent,
            onDismiss = {
                agendaCreateKind = null
                editingAgendaEvent = null
            },
            onSaveAcademic = viewModel::saveAcademicAgendaItem,
            onSaveEvent = viewModel::saveAgendaEvent,
            onDeleteEvent = viewModel::deleteAgendaEvent
        )
    }
    if (showFullSchedule) {
        FullScheduleDialog(
            selectedDate = selectedDate,
            sessions = state.sessions,
            subjects = state.subjects,
            use24Hour = state.accessibility.use24HourTime,
            onDismiss = { showFullSchedule = false },
            onWeekChange = { selectedEpochDay = it.toEpochDay() },
            onSessionClick = { date, session ->
                selectedEpochDay = date.toEpochDay()
                showFullSchedule = false
                selectedSession = session
            }
        )
    }
    selectedSession?.let { session ->
        ClassDetailsSheet(
            session = session,
            subject = state.subjects.firstOrNull { it.id == session.subjectId },
            date = selectedDate,
            occurrence = dayOccurrences.firstOrNull { it.sessionId == session.id },
            use24Hour = state.accessibility.use24HourTime,
            onDismiss = { selectedSession = null },
            onEdit = {
                selectedSession = null
                onEditSubjectClick(session.subjectId)
            },
            onHistory = {
                historySubjectId = session.subjectId
                selectedSession = null
            },
            onDelete = {
                viewModel.delete(session.id)
                selectedSession = null
            },
            /*
             * Guardar ya no cierra el panel.
             *
             * Lo cerraba, y con el se iba la unica oportunidad de decir por que faltaste. El
             * detalle se pregunta despues de guardar, asi que el panel tiene que seguir ahi.
             */
            onStatus = { status, modality, absenceReason, note ->
                viewModel.saveOccurrence(
                    sessionId = session.id,
                    dateEpochDay = selectedEpochDay,
                    status = status,
                    modality = modality,
                    absenceReason = absenceReason,
                    note = note
                )
            }
        )
    }

    if (poniendoseAlDiaTodas) {
        CatchUpSheet(
            pending = sinMarcarTodas,
            subjects = state.subjects,
            onMark = { entrada, estado ->
                viewModel.saveOccurrence(
                    sessionId = entrada.session.id,
                    dateEpochDay = entrada.date.toEpochDay(),
                    status = estado,
                    modality = ClassModality.IN_PERSON,
                    absenceReason = null,
                    note = ""
                )
            },
            onDismiss = { poniendoseAlDiaTodas = false }
        )
    }

    historySubjectId?.let { subjectId ->
        val subject = state.subjects.firstOrNull { it.id == subjectId }
        if (subject != null) {
            SubjectHistoryDialog(
                subject = subject,
                sessions = state.sessions.filter { it.subjectId == subjectId },
                occurrences = state.occurrences,
                term = state.activeTerm,
                breaks = state.breaks,
                onDismiss = { historySubjectId = null },
                onSetAbsenceLimit = { limite -> viewModel.setAbsenceLimit(subject, limite) },
                onCatchUp = { entrada, estado ->
                    viewModel.saveOccurrence(
                        sessionId = entrada.session.id,
                        dateEpochDay = entrada.date.toEpochDay(),
                        status = estado,
                        modality = ClassModality.IN_PERSON,
                        absenceReason = null,
                        note = ""
                    )
                },
                onMarkAttendance = { date, session ->
                    historySubjectId = null
                    selectedEpochDay = date.toEpochDay()
                    selectedSession = session
                }
            )
        }
    }

}

@Composable
private fun SubjectHistoryDialog(
    subject: Subject,
    sessions: List<ClassSession>,
    occurrences: List<ClassOccurrence>,
    term: AcademicTerm?,
    breaks: List<AcademicBreak>,
    onDismiss: () -> Unit,
    onSetAbsenceLimit: (Int?) -> Unit,
    onCatchUp: (AttendanceHistoryEntry, ClassAttendanceStatus) -> Unit,
    onMarkAttendance: (LocalDate, ClassSession) -> Unit
) {
    var pidiendoTope by remember { mutableStateOf(false) }
    var poniendoseAlDia by remember { mutableStateOf(false) }
    var ayudaVisible by remember { mutableStateOf(false) }
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
    /*
     * Las cuentas se piden, no se hacen aqui.
     *
     * Estaban escritas en la pantalla, y por eso el porcentaje podia anunciarse sin decir
     * sobre cuantas clases se calculaba. En `AttendanceSummary` las dos cifras viajan juntas.
     */
    val hoy = LocalDate.now()
    val summary = remember(entries, subject.absenceLimit) {
        SubjectAttendanceHistory.summarize(entries, subject.absenceLimit)
    }
    val weeks = remember(entries, term) {
        SubjectAttendanceHistory.byWeek(entries, hoy, term?.start)
    }
    val upcoming = remember(entries) { SubjectAttendanceHistory.upcoming(entries, hoy) }
    val sinMarcar = remember(entries) {
        SubjectAttendanceHistory.pendingToCatchUp(entries, LocalDateTime.now())
    }
    val pending = sinMarcar.firstOrNull()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                /*
                 * Una barra fina, no una banda de color.
                 *
                 * Habia 88 dp del acento de la seccion con un medallon encima, y eso metia un
                 * bloque saturado justo antes de la unica tarjeta que aqui importa: la vista
                 * empezaba por el adorno. Con el nombre al lado de la flecha, la pantalla
                 * arranca en el dato.
                 */
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = subject.name,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.01).em,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Toda la pantalla en una frase, para quien la abre por primera vez.
                    IconButton(onClick = { ayudaVisible = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = "Cómo se lee esta pantalla",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                AttendanceSummaryCard(
                    summary = summary,
                    entries = entries,
                    weeks = weeks,
                    today = hoy,
                    onLimitClick = { pidiendoTope = true },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = "HISTORIAL",
                    modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    letterSpacing = 0.13.em
                )
                /*
                 * La tarjeta mide lo que mide su contenido.
                 *
                 * Tenia `weight(1f)`, asi que con tres clases se estiraba hasta el boton y
                 * dejaba media pantalla de tarjeta vacia: parecia que faltaba algo por cargar.
                 * Ahora rueda la columna entera y la tarjeta acaba donde acaba la lista.
                 */
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            if (entries.isEmpty()) {
                                Text(
                                    "A\u00fan no hay clases en el historial",
                                    Modifier.fillMaxWidth().padding(24.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                AttendanceWeekList(
                                    weeks = weeks,
                                    upcoming = upcoming,
                                    today = hoy,
                                    onPick = { entrada -> onMarkAttendance(entrada.date, entrada.session) },
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = {
                        // Con varias sin marcar, entrar en cada una es lo que hace que se
                        // abandone el registro: se abre la lista con dos botones por fila.
                        if (sinMarcar.size > 1) {
                            poniendoseAlDia = true
                        } else {
                            pending?.let { onMarkAttendance(it.date, it.session) }
                        }
                    },
                    enabled = pending != null,
                    modifier = Modifier.fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight).padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScheduleAccent),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = when {
                            sinMarcar.size > 1 -> "Ponerse al d\u00eda \u00b7 ${sinMarcar.size} sin marcar"
                            pending != null -> "Marcar la del ${pending.date.dayMonth()}"
                            else -> "Todo al d\u00eda"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (ayudaVisible) {
        AttendanceHelpDialog(
            hasLimit = subject.absenceLimit != null,
            hasWeekNumbers = weeks.any { it.number != null },
            onDismiss = { ayudaVisible = false }
        )
    }

    if (pidiendoTope) {
        AbsenceLimitDialog(
            actual = subject.absenceLimit,
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
            onMark = { entrada, estado -> onCatchUp(entrada, estado) },
            onDismiss = { poniendoseAlDia = false }
        )
    }
}

@Composable
private fun HistoryRow(entry: AttendanceHistoryEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            entry.date.format(DateTimeFormatter.ofPattern("d MMM (EEE)", SpanishLocale)),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp
        )
        Box(Modifier.size(7.dp).clip(CircleShape).background(entry.status.color()))
        Spacer(Modifier.width(7.dp))
        Text(entry.status.label(), color = entry.status.color(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ClassDetailsSheet(
    session: ClassSession,
    subject: Subject?,
    date: LocalDate,
    occurrence: ClassOccurrence?,
    use24Hour: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onHistory: () -> Unit,
    onDelete: () -> Unit,
    onStatus: (ClassAttendanceStatus, ClassModality, ClassAbsenceReason?, String) -> Unit
) {
    // Abierto del todo desde el principio. Con la altura a medias —lo que hace un
    // ModalBottomSheet por defecto— las acciones del final quedaban fuera de la pantalla y
    // había que arrastrar el sheet hacia arriba para descubrir que estaban ahí.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accent = subject.scheduleColor()
    val status = occurrence?.status ?: ClassAttendanceStatus.PENDING
    /*
     * Si la clase ya paso y no esta marcada, la pregunta va antes que los datos.
     *
     * Habia seis fichas —horario, duracion, aula, profesor, repeticion, recordatorio— y
     * debajo de todas ellas los botones: lo que casi siempre vienes a hacer quedaba lo mas
     * lejos de la mano. Una clase que todavia no ha ocurrido no pregunta nada, asi que ahi
     * el orden de siempre sigue siendo el bueno.
     */
    val preguntaPrimero = !date.isAfter(LocalDate.now()) && status == ClassAttendanceStatus.PENDING

    // El detalle vive aqui y no en el modelo guardado porque se escribe despues de guardar.
    val modalidad = occurrence?.modality ?: ClassModality.IN_PERSON
    val motivo = occurrence?.absenceReason
    val nota = occurrence?.note.orEmpty()
    val statusOptions = listOf(
        ClassAttendanceStatus.ATTENDED,
        ClassAttendanceStatus.ABSENT,
        ClassAttendanceStatus.CANCELLED
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
                // Y aun así el contenido rueda: en una pantalla baja, o con la letra del
                // sistema en grande, el sheet completo tampoco da para todo.
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(16.dp)).background(accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null,
                        tint = contentColorOn(accent),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        subject?.name ?: "Clase",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        date.longTitle(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.width(10.dp))
                StatusPill(status)
            }

            // Los datos, en rejilla de dos. Antes iban en una línea de texto pegados con
            // puntos: si faltaba el aula y el profesor, la línea quedaba vacía y el hueco
            // parecía un fallo de la app en vez de un dato que nadie había rellenado.
            val fichas: @Composable () -> Unit = {
                ClassInfoGrid(
                    listOf(
                        ClassInfo(Icons.Rounded.Schedule, "Horario", "${formatMinute(session.startMinute, use24Hour)} - ${formatMinute(session.endMinute, use24Hour)}"),
                        ClassInfo(Icons.Rounded.HourglassBottom, "Duración", durationLabel(session.endMinute - session.startMinute)),
                        ClassInfo(Icons.Rounded.Place, "Aula", session.place.room.ifBlank { "Sin aula" }),
                        ClassInfo(Icons.Rounded.Person, "Profesor", session.place.professor.ifBlank { "Sin profesor" }),
                        ClassInfo(Icons.Rounded.Repeat, "Repetición", repeatLabel(session.repeatEveryWeeks)),
                        ClassInfo(Icons.Rounded.NotificationsNone, "Recordatorio", reminderLabel(session.reminderMinutes))
                    )
                )
            }

            if (!preguntaPrimero) fichas()

            if (preguntaPrimero) {
                // Lo imprescindible en una linea, para no perder el contexto al subir la pregunta.
                Text(
                    text = listOf(
                        "${formatMinute(session.startMinute, use24Hour)} - ${formatMinute(session.endMinute, use24Hour)}",
                        session.place.room.takeIf { it.isNotBlank() }
                    ).filterNotNull().joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            SheetGroupLabel("REGISTRAR ASISTENCIA")
            // Un grupo conectado, como el de Horario y Calendario arriba: tres piezas que se
            // tocan y una sola elegida. Eran tres rectángulos sueltos con borde, que es la
            // forma que tenía la app antes de este diseño.
            // La sobrecarga obsoleta, por el mismo motivo que en `UniSegmentedControl`: la
            // nueva cambia el contenido a un ámbito con `customItem` y resuelve un
            // desbordamiento que aquí, con tres estados, no ocurre.
            @Suppress("DEPRECATION")
            ButtonGroup(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                statusOptions.forEachIndexed { index, option ->
                    val interactionSource = remember { MutableInteractionSource() }
                    val selected = occurrence?.status == option
                    val shapes = when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        statusOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    }
                    ToggleButton(
                        // Volver a tocar el estado marcado lo deshace: si te equivocas de
                        // botón, antes no había forma de volver a «pendiente».
                        checked = selected,
                        onCheckedChange = {
                            onStatus(
                                if (selected) ClassAttendanceStatus.PENDING else option,
                                modalidad,
                                // Cambiar de estado tira el motivo: el de una falta no vale
                                // para una asistencia, y arrastrarlo guardaria una mentira.
                                if (option == ClassAttendanceStatus.ABSENT) motivo else null,
                                nota
                            )
                        },
                        shapes = shapes,
                        colors = ToggleButtonDefaults.toggleButtonColors(
                            checkedContainerColor = option.color(),
                            checkedContentColor = contentColorOn(option.color())
                        ),
                        interactionSource = interactionSource,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 58.dp)
                            .animateWidth(interactionSource)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(option.icon(), contentDescription = null, modifier = Modifier.size(18.dp))
                            // Sin ajuste de línea: mientras el vecino se ensancha, «Cancelada»
                            // cabría en menos de lo que mide y se partiría en dos.
                            Text(
                                option.label(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            AttendanceDetail(
                status = status,
                modality = modalidad,
                absenceReason = motivo,
                note = nota,
                onModalityChange = { onStatus(status, it, motivo, nota) },
                onReasonChange = { onStatus(status, modalidad, it, nota) },
                onNoteChange = { onStatus(status, modalidad, motivo, it) }
            )

            if (preguntaPrimero) fichas()

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            SheetActionRow(
                icon = Icons.Rounded.CalendarMonth,
                tone = LocalSectionColors.current.schedule,
                title = "Ver historial",
                subtitle = "Las asistencias que llevas de esta materia",
                onClick = onHistory
            )
            SheetActionRow(
                icon = Icons.Rounded.Edit,
                tone = MaterialTheme.colorScheme.tertiary,
                title = "Editar clase",
                subtitle = "Días, hora, aula y profesor",
                onClick = onEdit
            )
            SheetActionRow(
                icon = Icons.Rounded.DeleteOutline,
                tone = MaterialTheme.colorScheme.error,
                title = "Eliminar clase",
                // Dicho aquí porque es justo lo que confunde: esto vacía el horario de la
                // materia, no borra la materia ni sus notas.
                subtitle = "Se quita del horario; la materia sigue en Académico",
                onClick = onDelete,
                titleColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

/** El rótulo de un grupo dentro de un sheet: pequeño, en versales y del color del acento. */
@Composable
private fun SheetGroupLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = SectionLabelStyle,
        modifier = Modifier.padding(start = 4.dp)
    )
}

/** Una acción del sheet: el icono en su cuadrado de color, qué hace y qué significa. */
@Composable
private fun SheetActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tone: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(13.dp)).background(tone.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tone, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                color = titleColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Agregar clase, sabiendo lo que ya tienes.
 *
 * Borrar una clase del horario deja la materia viva en Académico —eso es lo que tiene que
 * pasar—, pero volver a ponerle horario obligaba a salir a Académico, buscarla y editarla, o
 * a crearla otra vez y acabar con la materia repetida. Aquí se ofrecen primero las materias
 * que existen y no tienen horario puesto; crear una nueva sigue estando, abajo.
 *
 * Si no hay ninguna materia suelta, este sheet no llega a aparecer: se va derecho al
 * formulario, que es lo que hacía antes el botón.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AddClassSheet(
    pendingSubjects: List<Subject>,
    onDismiss: () -> Unit,
    onPickSubject: (String) -> Unit,
    onNewSubject: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "Agregar clase",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Ponle horario a una materia que ya tienes, o crea una nueva.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            SheetGroupLabel("SIN HORARIO TODAVÍA")
            pendingSubjects.forEach { subject ->
                val tone = subject.scheduleColor()
                SheetActionRow(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    tone = tone,
                    title = subject.name,
                    subtitle = "Ya está en Académico; le faltan los días y la hora",
                    onClick = { onPickSubject(subject.id) }
                )
            }

            SheetGroupLabel("O EMPEZAR DE CERO")
            SheetActionRow(
                icon = Icons.Rounded.Add,
                tone = MaterialTheme.colorScheme.primary,
                title = "Materia nueva",
                subtitle = "Crea la materia y su horario a la vez",
                onClick = onNewSubject
            )
        }
    }
}

/**
 * El estado de hoy, al lado del nombre: es lo primero que se viene a mirar.
 *
 * La misma etiqueta que llevan las filas del calendario —versales pequeñas sobre el color
 * relleno—, para que «PENDIENTE» aquí y «EXAMEN» allí se lean como la misma clase de cosa.
 */
@Composable
private fun StatusPill(status: ClassAttendanceStatus) {
    val tone = status.color()
    Box(
        Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(tone)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            status.label().uppercase(SpanishLocale),
            color = contentColorOn(tone),
            style = SectionLabelStyle.copy(fontSize = 9.sp, lineHeight = 12.sp, letterSpacing = 0.5.sp)
        )
    }
}

private data class ClassInfo(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val value: String
)

@Composable
private fun ClassInfoGrid(items: List<ClassInfo>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { info ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = ScheduleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(
                            Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(info.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(info.label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                info.value,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun durationLabel(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "$rest min"
        rest == 0 -> "$hours h"
        else -> "$hours h $rest min"
    }
}

private fun repeatLabel(everyWeeks: Int): String =
    if (everyWeeks <= 1) "Cada semana" else "Cada $everyWeeks semanas"

private fun reminderLabel(minutes: Int): String = when {
    minutes <= 0 -> "Sin recordatorio"
    minutes % 60 == 0 -> "${minutes / 60} h antes"
    else -> "$minutes min antes"
}

private fun ClassAttendanceStatus.icon(): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    ClassAttendanceStatus.ATTENDED -> Icons.Rounded.Check
    ClassAttendanceStatus.ABSENT -> Icons.Rounded.Close
    ClassAttendanceStatus.CANCELLED -> Icons.Rounded.EventBusy
    ClassAttendanceStatus.RESCHEDULED -> Icons.Rounded.Schedule
    ClassAttendanceStatus.PENDING -> Icons.Rounded.Schedule
}

private val SpanishLocale: Locale = Locale.forLanguageTag("es")

@Composable
@ReadOnlyComposable
private fun Subject?.scheduleColor(): Color = this?.customColor?.let(::Color) ?: this?.let { subject -> subjectAccent(subject) } ?: ScheduleAccent

/** «24 ago», para decir de qué clase habla un botón sin escribir la fecha entera. */
private fun LocalDate.dayMonth(): String =
    format(DateTimeFormatter.ofPattern("d MMM", SpanishLocale))

private fun LocalDate.longTitle(): String = format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", SpanishLocale)).capitalized()

private fun String.capitalized(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(SpanishLocale) else it.toString() }

private fun formatMinute(value: Int, use24Hour: Boolean): String {
    val hour = value / 60
    val minute = value % 60
    if (use24Hour) return "%02d:%02d".format(hour, minute)
    val displayHour = (hour % 12).takeIf { it != 0 } ?: 12
    return "%d:%02d %s".format(displayHour, minute, if (hour < 12) "a. m." else "p. m.")
}

private fun ClassAttendanceStatus.label(): String = when (this) {
    ClassAttendanceStatus.PENDING -> "Pendiente"
    ClassAttendanceStatus.ATTENDED -> "Asist\u00ed"
    ClassAttendanceStatus.ABSENT -> "Falta"
    ClassAttendanceStatus.CANCELLED -> "Cancelada"
    ClassAttendanceStatus.RESCHEDULED -> "Reprogramada"
}

@Composable
private fun ClassAttendanceStatus.color(): Color =
    // Los mismos tres colores que la tira del historial: ver `AttendanceColors`.
    attendanceColor() ?: MaterialTheme.colorScheme.onSurfaceVariant
