package com.unistack.app.feature_support.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Una materia registrada, tal y como la ve la calculadora.
 *
 * Trae el nombre y el promedio, y **no trae créditos**: la materia no los guarda —el modelo
 * tiene nombre, notas, color y corte— así que los pone quien calcula. Es la clase de detalle
 * que hay que decir en la pantalla en vez de dejar que se descubra con una casilla vacía.
 */
data class CalculatorSubject(
    val id: String,
    val name: String,
    /** El promedio de lo evaluado, o `null` si la materia todavía no tiene notas. */
    val average: Double?
)

@HiltViewModel
class GpaCalculatorViewModel @Inject constructor(
    gradesRepository: GradesRepository,
    userRepository: UserRepository
) : ViewModel() {

    val profile = userRepository.userProfile

    /**
     * Las materias que se pueden traer a la cuenta.
     *
     * Entran **todas**, también las que aún no tienen ninguna nota. Antes se filtraban las
     * vacías, y con eso desaparecía el caso más interesante: poner a mano la nota que esperas
     * sacar en la materia que todavía no te han calificado.
     */
    val subjects: StateFlow<List<CalculatorSubject>> = combine(
        gradesRepository.subjects,
        userRepository.userProfile
    ) { subjects, profile ->
        val cuts = profile?.gradingCutScheme?.cuts.orEmpty()
        subjects.map { subject ->
            CalculatorSubject(
                id = subject.id,
                name = subject.name,
                average = GradeCalculator.calculateCurrentAverageByCuts(subject.grades, cuts)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
