package com.unistack.app.feature_grades.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.core.utils.TextValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_billing.domain.BillingRepository
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_schedule.domain.SubjectScheduleDraft
import kotlinx.coroutines.flow.StateFlow
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import java.util.UUID

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val repository: GradesRepository,
    private val userRepository: UserRepository,
    private val tasksRepository: TasksRepository,
    private val scheduleRepository: ScheduleRepository,
    private val billingRepository: BillingRepository,
    private val academicWorksRepository: AcademicWorksRepository
) : ViewModel() {
    val subjects: StateFlow<List<Subject>> = repository.subjects
    val userProfile: StateFlow<UserProfile?> = userRepository.userProfile
    val billingState = billingRepository.state
    val academicWorks: StateFlow<List<AcademicWork>> = academicWorksRepository.works
    val classSessions: StateFlow<List<ClassSession>> = scheduleRepository.sessions

    private fun getMaxGrade(): Double {
        val profile = userProfile.value ?: return 5.0
        return GradingScaleUtils.maxGradeFor(profile)
    }

    fun currentPlan() = FeatureGate.planFor(billingState.value.isPro)

    fun addSubject(
        name: String,
        targetAverage: Double,
        visualType: SubjectVisualType,
        customColor: Int? = null,
        activePeriodId: String? = null
    ): Subject? {
        if (!FeatureGate.canCreateSubject(currentPlan(), subjects.value.size)) return null
        if (!TextValidators.validateSubjectName(name).isValid) return null
        if (targetAverage !in 0.0..getMaxGrade()) return null
        val periodScheme = userProfile.value?.academicPeriodScheme
            ?: com.unistack.app.feature_user.domain.AcademicPeriodScheme.default()
        val subject = Subject(
            id = "subject-${UUID.randomUUID()}",
            name = TextValidators.normalizeText(name),
            targetAverage = targetAverage,
            grades = emptyList(),
            visualType = visualType,
            customColor = customColor,
            periodScheme = periodScheme,
            activePeriodId = activePeriodId
                ?.takeIf { id -> periodScheme.periods.any { it.id == id } }
                ?: periodScheme.periods.first().id
        )
        repository.addSubject(subject)
        return subject
    }

    fun updateSubject(
        subjectId: String,
        name: String,
        targetAverage: Double,
        visualType: SubjectVisualType,
        customColor: Int? = null,
        activePeriodId: String? = null
    ): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        if (!TextValidators.validateSubjectName(name).isValid) return false
        if (targetAverage !in 0.0..getMaxGrade()) return false

        repository.updateSubject(
            subject.copy(
                name = TextValidators.normalizeText(name),
                targetAverage = targetAverage,
                visualType = visualType,
                customColor = customColor,
                activePeriodId = activePeriodId
                    ?.takeIf { id -> subject.periodScheme.periods.any { it.id == id } }
                    ?: subject.activePeriodId
            )
        )
        return true
    }

    fun saveSubjectSchedule(subjectId: String, draft: SubjectScheduleDraft): Boolean {
        if (subjectId.isBlank() || !draft.isValid) return false
        val existing = classSessions.value.filter { it.subjectId == subjectId }
        if (!draft.enabled) {
            existing.forEach { scheduleRepository.deleteSession(it.id) }
            return true
        }

        val now = System.currentTimeMillis()
        val primary = existing.firstOrNull()
        scheduleRepository.saveSession(
            ClassSession(
                id = primary?.id ?: "class-${UUID.randomUUID()}",
                subjectId = subjectId,
                daysOfWeek = draft.daysOfWeek,
                startMinute = draft.startMinute,
                endMinute = draft.endMinute,
                location = draft.location,
                reminderMinutes = draft.reminderMinutes,
                createdAt = primary?.createdAt ?: now,
                updatedAt = now,
                repeatEveryWeeks = draft.repeatEveryWeeks,
                recurrenceStartEpochDay = draft.recurrenceStartEpochDay
            )
        )
        existing.drop(1).forEach { scheduleRepository.deleteSession(it.id) }
        return true
    }

    fun deleteSubject(subjectId: String): Boolean {
        val exists = subjects.value.any { it.id == subjectId }
        if (!exists) return false
        tasksRepository.tasks.value
            .filter { it.subjectId == subjectId }
            .forEach { task ->
                tasksRepository.updateTask(
                    task.copy(
                        subjectId = null,
                        periodId = null,
                        gradingStatus = if (task.linkedGradeId != null) {
                            TaskGradingStatus.NOT_GRADED
                        } else {
                            task.gradingStatus
                        },
                        linkedGradeId = null,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        classSessions.value
            .filter { it.subjectId == subjectId }
            .forEach { scheduleRepository.deleteSession(it.id) }
        repository.deleteSubject(subjectId)
        return true
    }

    fun addGrade(
        subjectId: String,
        name: String,
        value: Double,
        percentageInput: Double,
        type: GradeType = GradeType.WORKSHOP,
        periodId: String = "period-1",
        source: GradeSource = GradeSource.ACTIVITY,
        weightStatus: GradeWeightStatus = GradeWeightStatus.KNOWN,
        taskId: String? = null
    ): Boolean {
        return saveGrade(
            subjectId = subjectId,
            name = name,
            value = value,
            percentageInput = percentageInput,
            type = type,
            periodId = periodId,
            source = source,
            weightStatus = weightStatus,
            taskId = taskId
        ).saved
    }

    fun saveGrade(
        subjectId: String,
        name: String,
        value: Double,
        percentageInput: Double,
        type: GradeType = GradeType.WORKSHOP,
        periodId: String = "period-1",
        source: GradeSource = GradeSource.ACTIVITY,
        weightStatus: GradeWeightStatus = GradeWeightStatus.KNOWN,
        taskId: String? = null
    ): GradeSaveOutcome {
        val subject = subjects.value.firstOrNull { it.id == subjectId }
            ?: return GradeSaveOutcome(false)
        if (!TextValidators.validateActivityName(name).isValid) return GradeSaveOutcome(false)
        val percentage = when {
            source == GradeSource.PERIOD_FINAL -> 1.0
            weightStatus == GradeWeightStatus.UNKNOWN -> 0.0
            else -> percentageInput / 100.0
        }
        val existingKnownWeight = subject.grades
            .filter { it.periodId == periodId }
            .filter { it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.KNOWN }
            .sumOf { it.percentage }
        val total = existingKnownWeight +
            if (source == GradeSource.ACTIVITY && weightStatus == GradeWeightStatus.KNOWN) percentage else 0.0
        val maxGrade = getMaxGrade()
        val validWeight = source == GradeSource.PERIOD_FINAL ||
            weightStatus == GradeWeightStatus.UNKNOWN ||
            percentage > 0.0
        if (value !in 0.0..maxGrade || !validWeight || total > 1.00001) {
            return GradeSaveOutcome(false)
        }

        if (source == GradeSource.PERIOD_FINAL) {
            subject.grades
                .filter { it.periodId == periodId && it.source == GradeSource.PERIOD_FINAL }
                .forEach { repository.deleteGrade(subjectId, it.id) }
        }

        repository.addGrade(
            subjectId = subjectId,
            grade = GradeItem(
                id = "grade-${UUID.randomUUID()}",
                name = TextValidators.normalizeText(name),
                value = value,
                percentage = percentage,
                type = type,
                periodId = periodId,
                source = source,
                weightStatus = weightStatus,
                taskId = taskId,
                recordedAt = System.currentTimeMillis()
            )
        )
        val shouldSuggestHistory = subject.grades.isEmpty() &&
            subject.historyPromptStatus == PriorHistoryPromptStatus.NOT_SHOWN &&
            subject.periodScheme.periods.firstOrNull { it.id == periodId }?.order?.let { it > 1 } == true
        if (periodId != subject.activePeriodId) {
            repository.updateSubject(subject.copy(activePeriodId = periodId))
        }
        return GradeSaveOutcome(saved = true, suggestPriorHistory = shouldSuggestHistory)
    }

    fun updateGrade(
        subjectId: String,
        gradeId: String,
        name: String,
        value: Double,
        percentageInput: Double,
        type: GradeType = GradeType.WORKSHOP,
        periodId: String = "period-1",
        source: GradeSource = GradeSource.ACTIVITY,
        weightStatus: GradeWeightStatus = GradeWeightStatus.KNOWN
    ): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        val existingGrade = subject.grades.firstOrNull { it.id == gradeId } ?: return false
        if (!TextValidators.validateActivityName(name).isValid) return false

        val percentage = when {
            source == GradeSource.PERIOD_FINAL -> 1.0
            weightStatus == GradeWeightStatus.UNKNOWN -> 0.0
            else -> percentageInput / 100.0
        }
        val existingKnownWeight = subject.grades
            .filterNot { it.id == gradeId }
            .filter { it.periodId == periodId }
            .filter { it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.KNOWN }
            .sumOf { it.percentage }
        val total = existingKnownWeight +
            if (source == GradeSource.ACTIVITY && weightStatus == GradeWeightStatus.KNOWN) percentage else 0.0
        val maxGrade = getMaxGrade()
        val validWeight = source == GradeSource.PERIOD_FINAL ||
            weightStatus == GradeWeightStatus.UNKNOWN ||
            percentage > 0.0
        if (value !in 0.0..maxGrade || !validWeight || total > 1.00001) return false

        if (source == GradeSource.PERIOD_FINAL) {
            subject.grades
                .filter {
                    it.id != gradeId &&
                        it.periodId == periodId &&
                        it.source == GradeSource.PERIOD_FINAL
                }
                .forEach { repository.deleteGrade(subjectId, it.id) }
        }

        repository.updateGrade(
            subjectId = subjectId,
            grade = existingGrade.copy(
                name = TextValidators.normalizeText(name),
                value = value,
                percentage = percentage,
                type = type,
                periodId = periodId,
                source = source,
                weightStatus = weightStatus,
                recordedAt = System.currentTimeMillis()
            )
        )
        existingGrade.taskId?.let { taskId ->
            tasksRepository.tasks.value.firstOrNull { it.id == taskId }?.let { task ->
                tasksRepository.updateTask(
                    task.copy(
                        periodId = periodId,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        return true
    }

    fun deleteGrade(subjectId: String, gradeId: String): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        val grade = subject.grades.firstOrNull { it.id == gradeId } ?: return false
        repository.deleteGrade(subjectId, gradeId)
        val linkedTask = tasksRepository.tasks.value.firstOrNull {
            it.linkedGradeId == gradeId || it.id == grade.taskId
        }
        linkedTask?.let { task ->
            tasksRepository.updateTask(
                task.copy(
                    gradingStatus = TaskGradingStatus.AWAITING_GRADE,
                    linkedGradeId = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return true
    }

    fun subjectById(subjectId: String): Subject? {
        return subjects.value.firstOrNull { it.id == subjectId }
    }

    fun currentAverage(subject: Subject): Double? {
        return GradeCalculator.calculateProjectedAverageByPeriods(subject.grades, subject.periodScheme.periods)
    }

    fun evaluatedPercentage(subject: Subject): Double {
        return GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, subject.periodScheme.periods)
    }

    fun neededGrade(subject: Subject): Double? {
        if (subject.grades.isEmpty()) return null
        val periods = subject.periodScheme.periods
        val currentWeightedPoints = GradeCalculator.calculateWeightedPointsByPeriods(subject.grades, periods)
        val remainingPercentage =
            (1.0 - GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, periods) / 100.0)
                .coerceAtLeast(0.0)
        return GradeCalculator.calculateNeededGrade(
            currentWeightedPoints = currentWeightedPoints,
            remainingPercentage = remainingPercentage,
            targetAverage = subject.targetAverage,
            maxGrade = getMaxGrade()
        )
    }

    fun setActivePeriod(subjectId: String, periodId: String): Boolean {
        val subject = subjectById(subjectId) ?: return false
        if (subject.periodScheme.periods.none { it.id == periodId }) return false
        repository.updateSubject(subject.copy(activePeriodId = periodId))
        return true
    }

    fun updateHistoryPromptStatus(
        subjectId: String,
        status: PriorHistoryPromptStatus
    ): Boolean {
        val subject = subjectById(subjectId) ?: return false
        repository.updateSubject(subject.copy(historyPromptStatus = status))
        return true
    }

    fun markPeriodUnknown(subjectId: String, periodId: String): Boolean {
        val subject = subjectById(subjectId) ?: return false
        if (subject.periodScheme.periods.none { it.id == periodId }) return false
        val unknown = subject.unknownPeriodIds + periodId
        val previousPeriods = subject.previousPeriods()
        val status = if (previousPeriods.all { period ->
                period.id in unknown || subject.grades.any { it.periodId == period.id }
            }
        ) PriorHistoryPromptStatus.COMPLETED else PriorHistoryPromptStatus.SNOOZED
        repository.updateSubject(
            subject.copy(
                unknownPeriodIds = unknown,
                historyPromptStatus = status
            )
        )
        return true
    }

    fun clearPeriodUnknown(subjectId: String, periodId: String): Boolean {
        val subject = subjectById(subjectId) ?: return false
        repository.updateSubject(subject.copy(unknownPeriodIds = subject.unknownPeriodIds - periodId))
        return true
    }

    fun refreshHistoryCompletion(subjectId: String) {
        val subject = subjectById(subjectId) ?: return
        val previousPeriods = subject.previousPeriods()
        if (previousPeriods.isNotEmpty() && previousPeriods.all { period ->
                period.id in subject.unknownPeriodIds || subject.grades.any { it.periodId == period.id }
            }
        ) {
            repository.updateSubject(subject.copy(historyPromptStatus = PriorHistoryPromptStatus.COMPLETED))
        }
    }

    private fun Subject.previousPeriods() = periodScheme.periods
        .filter { period ->
            val activeOrder = periodScheme.periods.firstOrNull { it.id == activePeriodId }?.order ?: 1
            period.order < activeOrder
        }
}

data class GradeSaveOutcome(
    val saved: Boolean,
    val suggestPriorHistory: Boolean = false
)
