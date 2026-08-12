package com.unistack.app.feature_schedule.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_profile.domain.FeatureGate
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
    val accessibility: AccessibilityPreferences = AccessibilityPreferences()
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val repository: ScheduleRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val scheduleData = combine(
        repository.sessions,
        repository.occurrences,
        repository.agendaEvents
    ) { sessions, occurrences, agendaEvents -> Triple(sessions, occurrences, agendaEvents) }

    val uiState: StateFlow<ScheduleUiState> = combine(
        scheduleData,
        gradesRepository.subjects,
        tasksRepository.tasks,
        userRepository.userProfile
    ) { schedule, subjects, tasks, profile ->
        ScheduleUiState(
            sessions = schedule.first,
            occurrences = schedule.second,
            agendaEvents = schedule.third,
            subjects = subjects,
            tasks = tasks,
            accessibility = profile?.accessibilityPreferences ?: AccessibilityPreferences()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleUiState())

    fun save(
        existing: ClassSession?,
        subjectId: String,
        days: Set<Int>,
        startMinute: Int,
        endMinute: Int,
        location: String,
        reminderMinutes: Int,
        repeatEveryWeeks: Int = existing?.repeatEveryWeeks ?: 1,
        recurrenceStartEpochDay: Long = existing?.recurrenceStartEpochDay ?: 0L
    ): Boolean {
        val now = System.currentTimeMillis()
        val session = ClassSession(
            id = existing?.id ?: UUID.randomUUID().toString(),
            subjectId = subjectId,
            daysOfWeek = days,
            startMinute = startMinute,
            endMinute = endMinute,
            location = location.trim(),
            reminderMinutes = reminderMinutes,
            createdAt = existing?.createdAt ?: now,
            updatedAt = now,
            repeatEveryWeeks = repeatEveryWeeks,
            recurrenceStartEpochDay = recurrenceStartEpochDay
        )
        if (!session.isValid) return false
        repository.saveSession(session)
        return true
    }

    fun saveClassDraft(
        existing: ClassSession?,
        subjectName: String,
        professor: String,
        colorArgb: Int,
        days: Set<Int>,
        startMinute: Int,
        endMinute: Int,
        room: String,
        reminderMinutes: Int,
        repeatEveryWeeks: Int,
        recurrenceStartEpochDay: Long
    ): Boolean {
        val cleanName = subjectName.trim()
        if (cleanName.length !in 2..80 || days.isEmpty() || days.any { it !in 1..7 }) return false
        if (startMinute !in 0 until 24 * 60 || endMinute !in 1..24 * 60 || endMinute <= startMinute) return false
        if (repeatEveryWeeks !in 1..12) return false

        val currentSubject = existing?.let { session ->
            gradesRepository.subjects.value.firstOrNull { it.id == session.subjectId }
        }
        // Crear una clase con un nombre nuevo crea también la materia, así que este
        // camino tiene que respetar el mismo tope que la pantalla académica. Sin esta
        // comprobación, Horario era una puerta abierta para saltarse el límite del plan
        // gratis en cuanto se active PRO_FEATURES_ENABLED.
        if (currentSubject == null &&
            !FeatureGate.canCreateSubject(
                plan = FeatureGate.planFor(isPro = false),
                currentSubjectCount = gradesRepository.subjects.value.size
            )
        ) {
            return false
        }
        val subject = if (currentSubject == null) {
            val profile = userRepository.userProfile.value
            val periodScheme = profile?.academicPeriodScheme
                ?: com.unistack.app.feature_user.domain.AcademicPeriodScheme.default()
            Subject(
                id = UUID.randomUUID().toString(),
                name = cleanName,
                targetAverage = profile?.targetAverage ?: 4.0,
                grades = emptyList(),
                visualType = SubjectVisualType.TEAL,
                customColor = colorArgb,
                periodScheme = periodScheme,
                activePeriodId = periodScheme.periods.first().id
            ).also(gradesRepository::addSubject)
        } else {
            currentSubject.copy(
                name = cleanName,
                customColor = colorArgb
            ).also(gradesRepository::updateSubject)
        }

        val place = "${room.trim()}\u2022${professor.trim()}"
        return save(
            existing = existing,
            subjectId = subject.id,
            days = days,
            startMinute = startMinute,
            endMinute = endMinute,
            location = place,
            reminderMinutes = reminderMinutes,
            repeatEveryWeeks = repeatEveryWeeks,
            recurrenceStartEpochDay = recurrenceStartEpochDay
        )
    }

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
                periodId = subject?.activePeriodId,
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
