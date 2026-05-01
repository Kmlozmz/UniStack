package com.unistack.app.feature_grades.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.core.AppContainer
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_user.domain.GradingScale
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GradesViewModel(
    private val repository: GradesRepository = AppContainer.gradesRepository
) : ViewModel() {
    val subjects: StateFlow<List<Subject>> = repository.subjects

    fun addSubject(name: String, targetAverage: Double, visualType: SubjectVisualType): Subject? {
        if (!TextValidators.isValidAcademicName(name)) return null
        val subject = Subject(
            id = "subject-${System.currentTimeMillis()}",
            name = TextValidators.normalizeText(name),
            targetAverage = targetAverage,
            grades = emptyList(),
            visualType = visualType
        )
        repository.addSubject(subject)
        return subject
    }

    fun addGrade(subjectId: String, name: String, value: Double, percentageInput: Double): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        if (!TextValidators.isValidAcademicName(name)) return false
        val percentage = percentageInput / 100.0
        val total = subject.grades.sumOf { it.percentage } + percentage
        
        // Get scale to validate grade value
        val profile = runBlocking { AppContainer.userRepository.userProfile.first() }
        val maxGrade = when (profile?.gradingScale) {
            GradingScale.ZERO_TO_TEN -> 10.0
            GradingScale.ZERO_TO_ONE_HUNDRED -> 100.0
            else -> 5.0
        }

        if (value !in 0.0..maxGrade || percentage <= 0.0 || total > 1.00001) return false

        repository.addGrade(
            subjectId = subjectId,
            grade = GradeItem(
                id = "grade-${System.currentTimeMillis()}",
                name = TextValidators.normalizeText(name),
                value = value,
                percentage = percentage
            )
        )
        return true
    }

    fun subjectById(subjectId: String): Subject? {
        return subjects.value.firstOrNull { it.id == subjectId }
    }

    fun currentAverage(subject: Subject): Double = GradeCalculator.calculateCurrentAverage(subject.grades)

    fun evaluatedPercentage(subject: Subject): Double = GradeCalculator.calculateEvaluatedPercentage(subject.grades)

    fun neededGrade(subject: Subject): Double {
        return GradeCalculator.calculateNeededGrade(
            currentWeightedPoints = GradeCalculator.calculateWeightedPoints(subject.grades),
            remainingPercentage = (1.0 - subject.grades.sumOf { it.percentage }).coerceAtLeast(0.0),
            targetAverage = subject.targetAverage
        )
    }
}
