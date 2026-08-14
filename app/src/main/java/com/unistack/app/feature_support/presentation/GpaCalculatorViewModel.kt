package com.unistack.app.feature_support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_support.domain.GpaRow
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GpaCalculatorViewModel @Inject constructor(
    gradesRepository: GradesRepository,
    userRepository: UserRepository
) : ViewModel() {

    val profile = userRepository.userProfile

    /**
     * Las materias, listas para caer en la tabla.
     *
     * Solo entran las que ya tienen algo evaluado: una materia sin notas se traduciría en una
     * fila con la nota en blanco, que no aporta nada y hay que borrar a mano.
     */
    val subjectRows: StateFlow<List<GpaRow>> = combine(
        gradesRepository.subjects,
        userRepository.userProfile
    ) { subjects, profile ->
        val periods = profile?.academicPeriodScheme?.periods.orEmpty()
        val scale = profile?.gradingScale
        subjects.mapNotNull { subject ->
            val average = GradeCalculator.calculateCurrentAverageByPeriods(subject.grades, periods)
                ?: return@mapNotNull null
            GpaRow(
                id = subject.id,
                name = subject.name,
                grade = scale?.let { GradingScaleUtils.formatGrade(average, it) }
                    ?: average.toString(),
                credits = ""
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
