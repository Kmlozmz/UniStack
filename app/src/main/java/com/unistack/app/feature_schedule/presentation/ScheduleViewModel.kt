package com.unistack.app.feature_schedule.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.core.AppContainer
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ClassAbsenceReason
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope

data class ScheduleUiState(
    val sessions: List<ClassSession> = emptyList(),
    val occurrences: List<ClassOccurrence> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val tasks: List<StudentTask> = emptyList(),
    val accessibility: AccessibilityPreferences = AccessibilityPreferences()
)

class ScheduleViewModel : ViewModel() {
    private val repository = AppContainer.scheduleRepository

    val uiState: StateFlow<ScheduleUiState> = combine(
        repository.sessions,
        repository.occurrences,
        AppContainer.gradesRepository.subjects,
        AppContainer.tasksRepository.tasks,
        AppContainer.userRepository.userProfile
    ) { sessions, occurrences, subjects, tasks, profile ->
        ScheduleUiState(
            sessions = sessions,
            occurrences = occurrences,
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

        val gradesRepository = AppContainer.gradesRepository
        val currentSubject = existing?.let { session ->
            gradesRepository.subjects.value.firstOrNull { it.id == session.subjectId }
        }
        val subject = if (currentSubject == null) {
            val profile = AppContainer.userRepository.userProfile.value
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
