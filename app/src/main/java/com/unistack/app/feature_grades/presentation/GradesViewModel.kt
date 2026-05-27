package com.unistack.app.feature_grades.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.core.AppContainer
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_templates.domain.AcademicWork
import kotlinx.coroutines.flow.StateFlow
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import java.util.UUID

class GradesViewModel(
    private val repository: GradesRepository = AppContainer.gradesRepository,
    private val userRepository: UserRepository = AppContainer.userRepository
) : ViewModel() {
    val subjects: StateFlow<List<Subject>> = repository.subjects
    val userProfile: StateFlow<UserProfile?> = userRepository.userProfile
    val billingState = AppContainer.billingRepository.state
    val academicWorks: StateFlow<List<AcademicWork>> = AppContainer.academicWorksRepository.works

    private fun getMaxGrade(): Double {
        val profile = userProfile.value ?: return 5.0
        return GradingScaleUtils.maxGradeFor(profile)
    }

    fun currentPlan() = FeatureGate.planFor(billingState.value.isPro)

    fun addSubject(
        name: String,
        targetAverage: Double,
        visualType: SubjectVisualType,
        customColor: Int? = null
    ): Subject? {
        if (!FeatureGate.canCreateSubject(currentPlan(), subjects.value.size)) return null
        if (!TextValidators.validateSubjectName(name).isValid) return null
        if (targetAverage !in 0.0..getMaxGrade()) return null
        val subject = Subject(
            id = "subject-${UUID.randomUUID()}",
            name = TextValidators.normalizeText(name),
            targetAverage = targetAverage,
            grades = emptyList(),
            visualType = visualType,
            customColor = customColor
        )
        repository.addSubject(subject)
        return subject
    }

    fun updateSubject(
        subjectId: String,
        name: String,
        targetAverage: Double,
        visualType: SubjectVisualType,
        customColor: Int? = null
    ): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        if (!TextValidators.validateSubjectName(name).isValid) return false
        if (targetAverage !in 0.0..getMaxGrade()) return false

        repository.updateSubject(
            subject.copy(
                name = TextValidators.normalizeText(name),
                targetAverage = targetAverage,
                visualType = visualType,
                customColor = customColor
            )
        )
        return true
    }

    fun deleteSubject(subjectId: String): Boolean {
        val exists = subjects.value.any { it.id == subjectId }
        if (!exists) return false
        repository.deleteSubject(subjectId)
        return true
    }

    fun addGrade(
        subjectId: String,
        name: String,
        value: Double,
        percentageInput: Double,
        type: GradeType = GradeType.WORKSHOP,
        periodId: String = "period-1"
    ): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        if (!TextValidators.validateActivityName(name).isValid) return false
        val percentage = percentageInput / 100.0
        val total = subject.grades.filter { it.periodId == periodId }.sumOf { it.percentage } + percentage
        
        // Get scale to validate grade value
        val maxGrade = getMaxGrade()
        if (value !in 0.0..maxGrade || percentage <= 0.0 || total > 1.00001) return false

        repository.addGrade(
            subjectId = subjectId,
            grade = GradeItem(
                id = "grade-${UUID.randomUUID()}",
                name = TextValidators.normalizeText(name),
                value = value,
                percentage = percentage,
                type = type,
                periodId = periodId
            )
        )
        return true
    }

    fun updateGrade(
        subjectId: String,
        gradeId: String,
        name: String,
        value: Double,
        percentageInput: Double,
        type: GradeType = GradeType.WORKSHOP,
        periodId: String = "period-1"
    ): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        val existingGrade = subject.grades.firstOrNull { it.id == gradeId } ?: return false
        if (!TextValidators.validateActivityName(name).isValid) return false

        val percentage = percentageInput / 100.0
        val total = subject.grades
            .filterNot { it.id == gradeId }
            .filter { it.periodId == periodId }
            .sumOf { it.percentage } + percentage
        val maxGrade = getMaxGrade()
        if (value !in 0.0..maxGrade || percentage <= 0.0 || total > 1.00001) return false

        repository.updateGrade(
            subjectId = subjectId,
            grade = existingGrade.copy(
                name = TextValidators.normalizeText(name),
                value = value,
                percentage = percentage,
                type = type,
                periodId = periodId
            )
        )
        return true
    }

    fun deleteGrade(subjectId: String, gradeId: String): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        val exists = subject.grades.any { it.id == gradeId }
        if (!exists) return false
        repository.deleteGrade(subjectId, gradeId)
        return true
    }

    fun subjectById(subjectId: String): Subject? {
        return subjects.value.firstOrNull { it.id == subjectId }
    }

    fun currentAverage(subject: Subject): Double? = GradeCalculator.calculateCurrentAverage(subject.grades)

    fun evaluatedPercentage(subject: Subject): Double = GradeCalculator.calculateEvaluatedPercentage(subject.grades)

    fun neededGrade(subject: Subject): Double? {
        if (subject.grades.isEmpty()) return null
        return GradeCalculator.calculateNeededGrade(
            currentWeightedPoints = GradeCalculator.calculateWeightedPoints(subject.grades),
            remainingPercentage = (1.0 - subject.grades.sumOf { it.percentage }).coerceAtLeast(0.0),
            targetAverage = subject.targetAverage,
            maxGrade = getMaxGrade()
        )
    }

}
