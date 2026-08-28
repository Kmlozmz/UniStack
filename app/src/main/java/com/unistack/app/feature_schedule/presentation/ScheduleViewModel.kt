package com.unistack.app.feature_schedule.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ClassAbsenceReason
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.AgendaEventKind
import com.unistack.app.feature_schedule.domain.AgendaRecurrence
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.UserRepository
import java.util.UUID
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicBreakRepository
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

data class ScheduleUiState(
    val sessions: List<ClassSession> = emptyList(),
    val occurrences: List<ClassOccurrence> = emptyList(),
    val agendaEvents: List<AgendaEvent> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val tasks: List<StudentTask> = emptyList(),
    val accessibility: AccessibilityPreferences = AccessibilityPreferences(),
    /**
     * El periodo que se esta cursando, o nulo si no hay ninguno configurado.
     *
     * Lo necesita la asistencia: sin el, el historial de una materia se saca de la regla de
     * repeticion y no hay forma de distinguir una clase anterior al periodo de una que se
     * olvido marcar.
     */
    val activeTerm: AcademicTerm? = null,
    /** Festivos, paros y recesos: sus días no cuentan como clase perdida. */
    val breaks: List<AcademicBreak> = emptyList(),
    /**
     * Si los datos ya llegaron.
     *
     * El valor inicial de un `stateIn` es un estado vacío, y la pantalla lo pintaba como si
     * fuera la respuesta: al entrar en Horario se veía un instante «no hay clases» y acto
     * seguido aparecía todo. Con esto la pantalla sabe distinguir «todavía no sé» de «no hay».
     */
    val loaded: Boolean = false
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val userRepository: UserRepository,
    private val termRepository: AcademicTermRepository,
    private val breakRepository: AcademicBreakRepository
) : ViewModel() {

    /*
     * Los cuatro flujos del calendario en uno.
     *
     * Van juntos porque `combine` acepta cinco argumentos con tipos, y el estado ya gasta
     * cuatro. Agrupar aqui deja sitio para el periodo sin perder los tipos por el camino.
     */
    private data class DatosCalendario(
        val sessions: List<ClassSession>,
        val occurrences: List<ClassOccurrence>,
        val agendaEvents: List<AgendaEvent>,
        val breaks: List<AcademicBreak>
    )

    private val scheduleData = combine(
        repository.sessions,
        repository.occurrences,
        repository.agendaEvents,
        breakRepository.breaks
    ) { sessions, occurrences, agendaEvents, breaks ->
        DatosCalendario(sessions, occurrences, agendaEvents, breaks)
    }

    val uiState: StateFlow<ScheduleUiState> = combine(
        scheduleData,
        gradesRepository.subjects,
        tasksRepository.tasks,
        userRepository.userProfile,
        termRepository.activeTerm
    ) { schedule, subjects, tasks, profile, term ->
        ScheduleUiState(
            sessions = schedule.sessions,
            occurrences = schedule.occurrences,
            agendaEvents = schedule.agendaEvents,
            breaks = schedule.breaks,
            subjects = subjects,
            tasks = tasks,
            accessibility = profile?.accessibilityPreferences ?: AccessibilityPreferences(),
            activeTerm = term,
            loaded = true
        )
        // Eagerly y no WhileSubscribed: con la suscripción caducando a los cinco segundos,
        // salir de Horario y volver reiniciaba el flujo y repetía el mismo parpadeo.
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ScheduleUiState())

    fun delete(sessionId: String) = repository.deleteSession(sessionId)

    fun saveAgendaEvent(
        existing: AgendaEvent?,
        title: String,
        notes: String,
        kind: AgendaEventKind,
        date: LocalDate,
        startMinute: Int?,
        endMinute: Int?,
        location: String,
        reminderMinutes: Int,
        recurrence: AgendaRecurrence
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.length !in 2..100 || reminderMinutes !in 0..10_080) return false
        val zone = ZoneId.systemDefault()
        val allDay = startMinute == null
        val resolvedStart = startMinute ?: 9 * 60
        val startMillis = date.atStartOfDay(zone).plusMinutes(resolvedStart.toLong()).toInstant().toEpochMilli()
        val endMillis = endMinute?.let { minute ->
            if (minute <= resolvedStart) return false
            date.atStartOfDay(zone).plusMinutes(minute.toLong()).toInstant().toEpochMilli()
        }
        val now = System.currentTimeMillis()
        val event = AgendaEvent(
            id = existing?.id ?: "agenda-${UUID.randomUUID()}",
            title = cleanTitle,
            notes = notes.trim(),
            kind = kind,
            startMillis = startMillis,
            endMillis = endMillis,
            allDay = allDay,
            location = location.trim(),
            reminderMinutes = reminderMinutes,
            recurrence = recurrence,
            recurrenceEndEpochDay = existing?.recurrenceEndEpochDay,
            colorArgb = existing?.colorArgb,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        if (!event.isValid) return false
        repository.saveAgendaEvent(event)
        return true
    }

    fun deleteAgendaEvent(eventId: String) = repository.deleteAgendaEvent(eventId)

    fun saveAcademicAgendaItem(
        title: String,
        notes: String,
        subjectId: String?,
        type: TaskType,
        date: LocalDate,
        minute: Int?,
        generatesGrade: Boolean
    ): Boolean {
        val cleanTitle = title.trim()
        if (cleanTitle.length !in 2..100) return false
        val resolvedSubject = subjectId?.takeIf { id ->
            gradesRepository.subjects.value.any { it.id == id }
        }
        if (generatesGrade && resolvedSubject == null) return false
        val zone = ZoneId.systemDefault()
        val dueMillis = date.atStartOfDay(zone)
            .plusMinutes((minute ?: 23 * 60 + 59).toLong())
            .toInstant()
            .toEpochMilli()
        val subject = gradesRepository.subjects.value.firstOrNull { it.id == resolvedSubject }
        val now = System.currentTimeMillis()
        tasksRepository.addTask(
            StudentTask(
                id = "task-${UUID.randomUUID()}",
                title = cleanTitle,
                description = notes.trim(),
                subjectId = resolvedSubject,
                type = type,
                dueDateMillis = dueMillis,
                difficulty = TaskDifficulty.MEDIUM,
                estimatedMinutes = 60,
                completed = false,
                createdAt = now,
                updatedAt = now,
                cutId = subject?.chosenCutId,
                gradingStatus = if (generatesGrade) TaskGradingStatus.UNDECIDED else TaskGradingStatus.NOT_GRADED
            )
        )
        return true
    }

    fun saveOccurrence(
        sessionId: String,
        dateEpochDay: Long,
        status: ClassAttendanceStatus,
        modality: ClassModality,
        absenceReason: ClassAbsenceReason?,
        note: String,
        overrideStartMinute: Int? = null,
        overrideEndMinute: Int? = null,
        overrideLocation: String? = null
    ) {
        repository.saveOccurrence(
            ClassOccurrence(
                id = ClassOccurrence.idFor(sessionId, dateEpochDay),
                sessionId = sessionId,
                dateEpochDay = dateEpochDay,
                status = status,
                modality = modality,
                absenceReason = absenceReason,
                note = note.trim(),
                overrideStartMinute = overrideStartMinute,
                overrideEndMinute = overrideEndMinute,
                overrideLocation = overrideLocation?.trim(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}
