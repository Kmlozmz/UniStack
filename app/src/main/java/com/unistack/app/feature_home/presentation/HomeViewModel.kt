package com.unistack.app.feature_home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.AppContainer
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_home.domain.NeededGradeSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.GradingScale
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val gradesRepository: GradesRepository = AppContainer.gradesRepository,
    private val userRepository: UserRepository = AppContainer.userRepository
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        gradesRepository.subjects,
        userRepository.userProfile,
        userRepository.currentUser
    ) { subjects, profile, user ->
        HomeUiState(summary = HomeUiState.emptySummary.copyFrom(subjects, profile, user))
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    private fun com.unistack.app.feature_home.domain.HomeSummary.copyFrom(
        subjects: List<Subject>,
        profile: UserProfile?,
        user: AppUser
    ): com.unistack.app.feature_home.domain.HomeSummary {
        val summaries = subjects.take(3).map { subject ->
            val average = GradeCalculator.calculateCurrentAverage(subject.grades)
            val evaluatedPercentage = subject.grades.sumOf { it.percentage }.coerceIn(0.0, 1.0)
            SubjectSummary(
                id = subject.id,
                name = subject.name,
                average = average,
                progress = evaluatedPercentage.toFloat(),
                type = subject.visualType
            )
        }

        val subjectsWithGrades = subjects.filter { it.grades.isNotEmpty() }
        val generalAverage = if (subjectsWithGrades.isEmpty()) {
            null
        } else {
            val validGrades = subjectsWithGrades.mapNotNull { sub ->
                GradeCalculator.calculateCurrentAverage(sub.grades)?.let { avg ->
                    GradeItem(
                        id = sub.id,
                        name = sub.name,
                        value = avg,
                        percentage = 1.0 / subjectsWithGrades.size
                    )
                }
            }
            if (validGrades.isEmpty()) null else GradeCalculator.calculateCurrentAverage(validGrades)
        }

        val focusSubject = subjects.firstOrNull()
        val neededGrade = focusSubject?.let { subject ->
            val currentWeightedPoints = GradeCalculator.calculateWeightedPoints(subject.grades)
            val remainingPercentage = (1.0 - subject.grades.sumOf { it.percentage }).coerceAtLeast(0.0)
            val maxGrade = profile?.let { GradingScaleUtils.maxGradeFor(it.gradingScale) } ?: 5.0
            val needed = GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = currentWeightedPoints,
                remainingPercentage = remainingPercentage,
                targetAverage = subject.targetAverage,
                maxGrade = maxGrade
            )
            if (needed == null || needed.isNaN() || needed <= 0.0) {
                null
            } else {
                NeededGradeSummary(
                    subjectName = subject.name,
                    targetAverage = subject.targetAverage,
                    neededGrade = needed
                )
            }
        }

        return copy(
            userName = profile?.preferredName?.takeIf { it.isNotBlank() }
                ?: user.displayName?.takeIf { it.isNotBlank() }
                ?: "Estudiante",
            avatarPhotoUrl = user.photoUrl,
            generalAverage = generalAverage,
            subjectsCount = subjects.size,
            subjects = summaries,
            neededGrade = neededGrade,
            gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
        )
    }
}
