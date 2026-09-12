@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_schedule.presentation

import com.unistack.app.core.design.components.UniBackButton
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
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.core.notifications.AttendanceDeepLink
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
import com.unistack.app.core.utils.Textos
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

private enum class CalendarMode(val labelRes: Int) {
    MONTH(R.string.schedule_tab_month),
    AGENDA(R.string.schedule_tab_agenda),
    LIST(R.string.schedule_tab_list);

    val label: String
        @Composable get() = stringResource(labelRes)
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
    // La clase que el historial tiene que abrir al entrar: solo la pone el aviso.
    var historyOpenClass by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var showFullSchedule by rememberSaveable { mutableStateOf(false) }
    var showAgendaMenu by rememberSaveable { mutableStateOf(false) }
    var showAddClassSheet by rememberSaveable { mutableStateOf(false) }
    var agendaCreateKind by remember { mutableStateOf<AgendaCreateKind?>(null) }
    var editingAgendaEvent by remember { mutableStateOf<com.unistack.app.feature_schedule.domain.AgendaEvent?>(null) }

    /*
     * El aviso de asistencia abre la clase por la que pregunta.
     *
     * Se espera a que los datos hayan llegado: en el primer fotograma la lista de clases esta
     * vacia y buscar en ella daria «no existe» para una clase que si existe. Si de verdad ya
     * no esta —se borro la materia entre el aviso y el toque— el dato se descarta igual, para
     * que no quede esperando a reabrirse solo la proxima vez que se entre en Horario.
     */
    val avisoDeClase by AttendanceDeepLink.pending.collectAsStateWithLifecycle()
    LaunchedEffect(avisoDeClase, state.loaded) {
        val aviso = avisoDeClase ?: return@LaunchedEffect
        if (!state.loaded) return@LaunchedEffect
        /*
         * Al historial, no a la ficha de la clase: la ficha ya no marca asistencia, asi que
         * llegar ahi era llegar a un sitio donde no se podia contestar lo preguntado.
         */
        state.sessions.firstOrNull { it.id == aviso.sessionId }?.let { sesion ->
            historyOpenClass = sesion.id to aviso.epochDay
            historySubjectId = sesion.subjectId
        }
        AttendanceDeepLink.consume()
    }

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
            // Se corrige dentro del historial: tocar una clase ya no cierra esto para
            // abrir la ficha del calendario, que no marca asistencia.
            AttendanceHistoryScreen(
                subject = subject,
                sessions = state.sessions.filter { it.subjectId == subjectId },
                occurrences = state.occurrences,
                term = state.activeTerm,
                cutScheme = state.cutScheme,
                absenceLimit = state.absenceLimit,
                breaks = state.breaks,
                onDismiss = { historySubjectId = null; historyOpenClass = null },
                onSetAbsenceLimit = { limite -> viewModel.setAbsenceLimit(limite) },
                abrirClase = historyOpenClass,
                onSave = { entrada, estado, modalidad, motivo, nota ->
                    viewModel.saveOccurrence(
                        sessionId = entrada.session.id,
                        dateEpochDay = entrada.date.toEpochDay(),
                        status = estado,
                        modality = modalidad,
                        absenceReason = motivo,
                        note = nota
                    )
                }
            )
        }
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
                        subject?.name ?: stringResource(R.string.schedule_detail_class),
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
                        ClassInfo(Icons.Rounded.Schedule, stringResource(R.string.schedule_detail_time), "${formatMinute(session.startMinute, use24Hour)} - ${formatMinute(session.endMinute, use24Hour)}"),
                        ClassInfo(Icons.Rounded.HourglassBottom, stringResource(R.string.schedule_detail_duration), durationLabel(session.endMinute - session.startMinute)),
                        ClassInfo(Icons.Rounded.Place, stringResource(R.string.schedule_detail_room), session.place.room.ifBlank { stringResource(R.string.schedule_detail_no_room) }),
                        ClassInfo(Icons.Rounded.Person, stringResource(R.string.schedule_detail_professor), session.place.professor.ifBlank { stringResource(R.string.schedule_detail_no_professor) }),
                        ClassInfo(Icons.Rounded.Repeat, stringResource(R.string.schedule_detail_repetition), repeatLabel(session.repeatEveryWeeks)),
                        ClassInfo(Icons.Rounded.NotificationsNone, stringResource(R.string.schedule_detail_reminder), reminderLabel(session.reminderMinutes))
                    )
                )
            }

            /*
             * **Aqui ya no se marca asistencia.**
             *
             * Se marcaba en dos sitios con dos formas distintas: este panel, con un grupo
             * segmentado de tres estados y su detalle debajo, y «Ponerse al dia», con dos
             * botones planos y sin gesto ninguno. Dos caminos para lo mismo, uno de ellos a
             * medio hacer, y encima este obligaba a abrir la clase para responder por ella.
             *
             * Ahora el panel es lo que dice su titulo: la ficha de la clase. Marcar —una o
             * catorce— vive entero en «Ponerse al dia», que es la pantalla que existe para eso.
             */
            fichas()

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            SheetActionRow(
                icon = Icons.Rounded.CalendarMonth,
                tone = LocalSectionColors.current.schedule,
                title = stringResource(R.string.schedule_view_history),
                subtitle = stringResource(R.string.schedule_view_history_desc),
                onClick = onHistory
            )
            SheetActionRow(
                icon = Icons.Rounded.Edit,
                tone = MaterialTheme.colorScheme.tertiary,
                title = stringResource(R.string.schedule_edit_class),
                subtitle = stringResource(R.string.schedule_edit_class_desc),
                onClick = onEdit
            )
            SheetActionRow(
                icon = Icons.Rounded.DeleteOutline,
                tone = MaterialTheme.colorScheme.error,
                title = stringResource(R.string.schedule_delete_class),
                subtitle = stringResource(R.string.schedule_delete_class_desc),
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
                        stringResource(R.string.schedule_add_class),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        stringResource(R.string.schedule_add_class_subtitle),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            SheetGroupLabel(stringResource(R.string.schedule_section_no_schedule_yet))
            pendingSubjects.forEach { subject ->
                val tone = subject.scheduleColor()
                SheetActionRow(
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    tone = tone,
                    title = subject.name,
                    subtitle = stringResource(R.string.schedule_no_schedule_yet_desc),
                    onClick = { onPickSubject(subject.id) }
                )
            }

            SheetGroupLabel(stringResource(R.string.schedule_section_start_scratch))
            SheetActionRow(
                icon = Icons.Rounded.Add,
                tone = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.schedule_new_subject),
                subtitle = stringResource(R.string.schedule_new_subject_desc),
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
            status.label().uppercase(AppLocale),
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

private fun repeatLabel(everyWeeks: Int): String {
    return if (everyWeeks <= 1) {
        Textos.get(R.string.schedule_repeat_every_week)
    } else {
        Textos.get(R.string.schedule_cada_semanas, everyWeeks)
    }
}

private fun reminderLabel(minutes: Int): String {
    return when {
        minutes <= 0 -> Textos.get(R.string.schedule_reminder_none)
        minutes % 60 == 0 -> Textos.get(R.string.schedule_h_antes, minutes / 60)
        else -> Textos.get(R.string.schedule_min_antes, minutes)
    }
}

private fun ClassAttendanceStatus.icon(): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    ClassAttendanceStatus.ATTENDED -> Icons.Rounded.Check
    ClassAttendanceStatus.ABSENT -> Icons.Rounded.Close
    ClassAttendanceStatus.CANCELLED -> Icons.Rounded.EventBusy
    ClassAttendanceStatus.RESCHEDULED -> Icons.Rounded.Schedule
    ClassAttendanceStatus.PENDING -> Icons.Rounded.Schedule
}

private val AppLocale: Locale get() = Locale.getDefault()

@Composable
@ReadOnlyComposable
private fun Subject?.scheduleColor(): Color = this?.customColor?.let(::Color) ?: this?.let { subject -> subjectAccent(subject) } ?: ScheduleAccent

/** «24 ago», para decir de qué clase habla un botón sin escribir la fecha entera. */
private fun LocalDate.dayMonth(): String =
    format(DateTimeFormatter.ofPattern("d MMM", AppLocale))

private fun LocalDate.longTitle(): String = format(DateTimeFormatter.ofPattern(if (AppLocale.language == "en") "EEEE, MMMM d" else "EEEE, d 'de' MMMM", AppLocale)).capitalized()

private fun String.capitalized(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(AppLocale) else it.toString() }

private fun formatMinute(value: Int, use24Hour: Boolean): String {
    val hour = value / 60
    val minute = value % 60
    if (use24Hour) return "%02d:%02d".format(hour, minute)
    val displayHour = (hour % 12).takeIf { it != 0 } ?: 12
    return "%d:%02d %s".format(displayHour, minute, if (hour < 12) (Textos.get(R.string.schedule_a_m)) else (Textos.get(R.string.schedule_p_m)))
}

private fun ClassAttendanceStatus.label(): String {
    return when (this) {
        ClassAttendanceStatus.PENDING -> Textos.get(R.string.subject_status_pending)
        ClassAttendanceStatus.ATTENDED -> Textos.get(R.string.schedule_asist_u00ed)
        ClassAttendanceStatus.ABSENT -> Textos.get(R.string.schedule_status_absent)
        ClassAttendanceStatus.CANCELLED -> Textos.get(R.string.schedule_status_canceled)
        ClassAttendanceStatus.RESCHEDULED -> Textos.get(R.string.schedule_status_rescheduled)
    }
}

@Composable
private fun ClassAttendanceStatus.color(): Color =
    // Los mismos tres colores que la tira del historial: ver `AttendanceColors`.
    attendanceColor() ?: MaterialTheme.colorScheme.onSurfaceVariant
