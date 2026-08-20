@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_schedule.presentation

import com.unistack.app.core.utils.DayLabels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.SessionPlace
import com.unistack.app.feature_tasks.domain.StudentTask
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.foundation.layout.heightIn
/* Estos nombres describían un color («Green», «Purple») pero devolvían un rol del tema,
   así que mentían en cuanto el acento dejaba de ser verde —es decir, siempre—. Ahora
   nombran el papel que cumplen. Se cayeron dos: SchedulePurple, que era un duplicado
   literal de ScheduleAccent, y SchedulePink, que solo alimentaba la lista de muestras. */
private val ScheduleAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary
private val ScheduleRescheduled: Color
    @Composable get() = LocalSectionColors.current.schedule
private val ScheduleCancelled: Color
    @Composable get() = LocalSectionColors.current.atRisk
private val ScheduleShape: Shape
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
        modifier = modifier
    )
    if (showAgendaMenu) {
        AgendaCreateMenuSheet(
            onDismiss = { showAgendaMenu = false },
            onSelect = { kind ->
                showAgendaMenu = false
                editingAgendaEvent = null
                agendaCreateKind = kind
            },
            onAddClass = {
                showAgendaMenu = false
                startAddClass()
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
            onStatus = { status ->
                viewModel.saveOccurrence(
                    sessionId = session.id,
                    dateEpochDay = selectedEpochDay,
                    status = status,
                    modality = ClassModality.IN_PERSON,
                    absenceReason = null,
                    note = ""
                )
                selectedSession = null
            }
        )
    }

    historySubjectId?.let { subjectId ->
        val subject = state.subjects.firstOrNull { it.id == subjectId }
        if (subject != null) {
            SubjectHistoryDialog(
                subject = subject,
                sessions = state.sessions.filter { it.subjectId == subjectId },
                occurrences = state.occurrences,
                onDismiss = { historySubjectId = null },
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
    onDismiss: () -> Unit,
    onMarkAttendance: (LocalDate, ClassSession) -> Unit
) {
    val entries = remember(sessions, occurrences) {
        buildSubjectHistory(sessions, occurrences, LocalDate.now())
    }
    val attended = entries.count { it.status == ClassAttendanceStatus.ATTENDED }
    val absent = entries.count { it.status == ClassAttendanceStatus.ABSENT }
    val decided = attended + absent
    val rate = if (decided == 0) 0 else (attended.toFloat() / decided * 100).roundToInt()
    val pending = entries
        .filter { it.status == ClassAttendanceStatus.PENDING && !it.date.isAfter(LocalDate.now()) }
        .maxByOrNull(HistoryEntry::date)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(88.dp).background(ScheduleAccent)
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter).offset(y = 22.dp).size(50.dp)
                            .clip(CircleShape).background(MaterialTheme.colorScheme.background)
                            .border(1.dp, ScheduleAccent.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = ScheduleAccent)
                    }
                }
                Spacer(Modifier.height(30.dp))
                Text(
                    subject.name,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = ScheduleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Asistencia general", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            Text("$rate%", color = MaterialTheme.colorScheme.onSurface, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                            Text("$attended asistencias  \u2022  $absent faltas", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        }
                        Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                            CircularWavyProgressIndicator(
                                progress = { rate / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = ScheduleAccent,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                            Text("$rate%", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    "Historial",
                    modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 7.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                    shape = ScheduleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    LazyColumn(contentPadding = PaddingValues(vertical = 5.dp)) {
                        if (entries.isEmpty()) {
                            item {
                                Text("A\u00fan no hay clases en el historial", Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        items(entries, key = { "${it.session.id}:${it.date.toEpochDay()}" }) { entry ->
                            HistoryRow(entry)
                        }
                    }
                }
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = { pending?.let { onMarkAttendance(it.date, it.session) } },
                    enabled = pending != null,
                    modifier = Modifier.fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight).padding(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ScheduleAccent),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Marcar asistencia", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
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
    onStatus: (ClassAttendanceStatus) -> Unit
) {
    // Abierto del todo desde el principio. Con la altura a medias —lo que hace un
    // ModalBottomSheet por defecto— las acciones del final quedaban fuera de la pantalla y
    // había que arrastrar el sheet hacia arriba para descubrir que estaban ahí.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accent = subject.scheduleColor()
    val status = occurrence?.status ?: ClassAttendanceStatus.PENDING
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

            SheetGroupLabel("REGISTRAR ASISTENCIA")
            // Un grupo conectado, como el de Horario y Calendario arriba: tres piezas que se
            // tocan y una sola elegida. Eran tres rectángulos sueltos con borde, que es la
            // forma que tenía la app antes de este diseño.
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
                        onCheckedChange = { onStatus(if (selected) ClassAttendanceStatus.PENDING else option) },
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

@Composable
private fun DetailActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(ScheduleShape).clickable(onClick = onClick).padding(vertical = 9.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(11.dp))
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}



private data class HistoryEntry(
    val date: LocalDate,
    val session: ClassSession,
    val status: ClassAttendanceStatus
)

private val SpanishLocale: Locale = Locale.forLanguageTag("es")

@Composable
@ReadOnlyComposable
private fun Subject?.scheduleColor(): Color = this?.customColor?.let(::Color) ?: this?.let { subject -> subjectAccent(subject) } ?: ScheduleAccent

private fun LocalDate.weekStart(): LocalDate = minusDays((dayOfWeek.value - 1).toLong())

private fun Long.asLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.weekdayName(): String = format(DateTimeFormatter.ofPattern("EEEE", SpanishLocale)).capitalized()

private fun LocalDate.longTitle(): String = format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", SpanishLocale)).capitalized()

private fun LocalDate.shortDate(): String = format(DateTimeFormatter.ofPattern("d MMM yyyy", SpanishLocale))

private fun String.capitalized(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(SpanishLocale) else it.toString() }

private fun String.shortName(): String = split(' ').filter(String::isNotBlank).take(2).joinToString(" ") { word ->
    if (word.length <= 5) word else word.take(5) + "."
}

private fun dayLetter(day: DayOfWeek): String = DayLabels.short[day.value - 1]

private fun formatMinute(value: Int, use24Hour: Boolean): String {
    val hour = value / 60
    val minute = value % 60
    if (use24Hour) return "%02d:%02d".format(hour, minute)
    val displayHour = (hour % 12).takeIf { it != 0 } ?: 12
    return "%d:%02d %s".format(displayHour, minute, if (hour < 12) "a. m." else "p. m.")
}

private fun findNextSession(fromDate: LocalDate, sessions: List<ClassSession>): Pair<LocalDate, ClassSession>? {
    if (sessions.isEmpty()) return null
    return (0L..13L).asSequence().mapNotNull { offset ->
        val date = fromDate.plusDays(offset)
        sessions.filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
            .minByOrNull(ClassSession::startMinute)
            ?.let { date to it }
    }.firstOrNull()
}

private fun monthScheduleDates(
    month: YearMonth,
    sessions: List<ClassSession>
): List<Pair<LocalDate, List<ClassSession>>> = (1..month.lengthOfMonth()).mapNotNull { day ->
    val date = month.atDay(day)
    val dateSessions = sessions
        .filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
        .sortedBy(ClassSession::startMinute)
    if (dateSessions.isNotEmpty()) date to dateSessions else null
}

private fun buildSubjectHistory(
    sessions: List<ClassSession>,
    occurrences: List<ClassOccurrence>,
    today: LocalDate
): List<HistoryEntry> {
    val occurrenceByKey = occurrences.associateBy { it.sessionId to it.dateEpochDay }
    val allEntries = (-120L..30L).flatMap { offset ->
        val date = today.plusDays(offset)
        sessions.filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }.map { session ->
            HistoryEntry(
                date = date,
                session = session,
                status = occurrenceByKey[session.id to date.toEpochDay()]?.status ?: ClassAttendanceStatus.PENDING
            )
        }
    }
    val future = allEntries.filter { it.date.isAfter(today) }
        .sortedBy(HistoryEntry::date)
        .take(2)
    val recent = allEntries.filter { !it.date.isAfter(today) }
        .sortedWith(compareByDescending<HistoryEntry> { it.date }.thenByDescending { it.session.startMinute })
        .take(22)
    return (future + recent).sortedWith(
        compareByDescending<HistoryEntry> { it.date }.thenByDescending { it.session.startMinute }
    )
}

private fun ClassAttendanceStatus.label(): String = when (this) {
    ClassAttendanceStatus.PENDING -> "Pendiente"
    ClassAttendanceStatus.ATTENDED -> "Asist\u00ed"
    ClassAttendanceStatus.ABSENT -> "Falta"
    ClassAttendanceStatus.CANCELLED -> "Cancelada"
    ClassAttendanceStatus.RESCHEDULED -> "Reprogramada"
}

@Composable
private fun ClassAttendanceStatus.color(): Color = when (this) {
    ClassAttendanceStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    ClassAttendanceStatus.ATTENDED -> ScheduleAccent
    ClassAttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
    ClassAttendanceStatus.CANCELLED -> ScheduleCancelled
    ClassAttendanceStatus.RESCHEDULED -> ScheduleRescheduled
}
