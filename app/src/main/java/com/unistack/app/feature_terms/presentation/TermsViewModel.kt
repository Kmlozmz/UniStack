package com.unistack.app.feature_terms.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_schedule.domain.SubjectAttendanceHistory
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicBreakRepository
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_terms.domain.TermCloseCheck
import com.unistack.app.feature_terms.domain.TermCloseReport
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Un periodo con lo que se puede decir de él sin abrirlo.
 *
 * [average] en nulo es «no hay nada evaluado», no un cero, y [attendanceRate] en nulo es «no
 * hay clases marcadas». Ninguno de los dos se rellena con un número inventado: un periodo
 * cerrado sin datos es un hecho, y decir 0% sería otra cosa.
 */
data class TermSummary(
    val term: AcademicTerm,
    val subjectCount: Int,
    val average: Double?,
    val failedCount: Int,
    val attendanceRate: Int?,
    val subjects: List<SubjectInTerm> = emptyList(),
    /**
     * Desde cuándo cuenta la asistencia de este periodo.
     *
     * Si el periodo empezó el 1 de agosto y la app se instaló el 20 de septiembre, hay siete
     * semanas que nadie va a marcar hacia atrás. Contarlas daría un porcentaje bajo para
     * siempre e ignorarlas en silencio sería otra cifra inventada: se dice el alcance.
     */
    val attendanceSince: LocalDate? = null
)

/** Una materia dentro de un periodo, con lo que se enseña de ella en el histórico. */
data class SubjectInTerm(
    val id: String,
    val name: String,
    val average: Double?,
    val attendanceRate: Int?,
    /** Nulo mientras no haya notas suficientes para afirmar nada. */
    val passed: Boolean?
)

data class TermsUiState(
    val activeTerm: AcademicTerm? = null,
    val summaries: List<TermSummary> = emptyList(),
    /** Lo que le falta al periodo activo, o nulo si no hay periodo. */
    val report: TermCloseReport? = null,
    val cumulativeAverage: Double? = null,
    val closedCount: Int = 0,
    val subjectsInHistory: Int = 0,
    val loaded: Boolean = false
) {
    /** El último que se cerró, que es el que se resume cuando no hay ninguno activo. */
    val lastClosed: TermSummary?
        get() = summaries.firstOrNull { !it.term.isActive }
}

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val termRepository: AcademicTermRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val scheduleRepository: ScheduleRepository,
    private val breakRepository: AcademicBreakRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    /** Los datos del horario van juntos porque `combine` no da para tantos por separado. */
    private data class DatosDeClase(
        val sessions: List<ClassSession>,
        val occurrences: List<ClassOccurrence>,
        val breaks: List<AcademicBreak>
    )

    private val datosDeClase = combine(
        scheduleRepository.sessions,
        scheduleRepository.occurrences,
        breakRepository.breaks
    ) { sessions, occurrences, breaks -> DatosDeClase(sessions, occurrences, breaks) }

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun consumeMessage() {
        _message.value = null
    }

    val uiState: StateFlow<TermsUiState> = combine(
        termRepository.terms,
        gradesRepository.subjects,
        tasksRepository.tasks,
        userRepository.userProfile,
        datosDeClase
    ) { terms, subjects, tasks, profile, clases ->
        construir(terms, subjects, tasks, profile, clases)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TermsUiState())

    private fun construir(
        terms: List<AcademicTerm>,
        subjects: List<Subject>,
        tasks: List<StudentTask>,
        profile: UserProfile?,
        clases: DatosDeClase
    ): TermsUiState {
        val hoy = LocalDate.now()
        val activo = terms.firstOrNull { it.isActive }
        val esquema = profile?.gradingCutScheme ?: GradingCutScheme.default()

        /*
         * Cada periodo se resume con sus propias materias.
         *
         * Las que no tienen periodo —de antes de que existieran— se cuentan en el activo: es
         * donde el usuario las está viendo, y dejarlas fuera haría que el resumen del periodo
         * en curso no cuadrara con la lista de materias que tiene delante.
         */
        val resumenes = terms
            .sortedByDescending { it.startEpochDay }
            .map { periodo ->
                val suyas = subjects.filter { materia ->
                    materia.termId == periodo.id || (materia.termId == null && periodo.id == activo?.id)
                }
                resumir(periodo, suyas, esquema, profile, clases, hoy)
            }

        val cerrados = resumenes.filter { !it.term.isActive }
        val promediosCerrados = cerrados.mapNotNull { it.average }

        return TermsUiState(
            activeTerm = activo,
            summaries = resumenes,
            report = activo?.let {
                val suyas = subjects.filter { materia ->
                    materia.termId == it.id || materia.termId == null
                }
                TermCloseCheck.build(
                    subjects = suyas,
                    cutScheme = esquema,
                    closedCutIds = TermCloseCheck.allCuts(esquema),
                    unmarkedClasses = sinMarcarPorMateria(suyas, clases, it, hoy),
                    tasks = tasks,
                    passingGrade = profile?.passingGrade ?: 3.0,
                    today = hoy
                )
            },
            cumulativeAverage = promediosCerrados.takeIf { it.isNotEmpty() }?.let { lista ->
                Math.round(lista.average() * 10.0) / 10.0
            },
            closedCount = cerrados.size,
            subjectsInHistory = cerrados.sumOf { it.subjectCount },
            loaded = true
        )
    }

    private fun resumir(
        term: AcademicTerm,
        suyas: List<Subject>,
        esquema: GradingCutScheme,
        profile: UserProfile?,
        clases: DatosDeClase,
        hoy: LocalDate
    ): TermSummary {
        val cortes = esquema.cuts.sortedBy { it.order }
        val promedios = suyas.mapNotNull {
            GradeCalculator.calculateCurrentAverageByCuts(it.grades, cortes)
        }
        val aprobado = profile?.passingGrade ?: 3.0
        val hasta = if (term.isActive) hoy else term.endFor(hoy)
        val entradas = SubjectAttendanceHistory.build(
            sessions = clases.sessions.filter { sesion -> suyas.any { it.id == sesion.subjectId } },
            occurrences = clases.occurrences,
            today = hasta,
            termStart = term.start,
            termEnd = term.plannedEnd,
            breaks = clases.breaks.map { it.range }
        )
        val marcadas = entradas.filter { it.status != ClassAttendanceStatus.PENDING }
        return TermSummary(
            term = term,
            subjectCount = suyas.size,
            average = promedios.takeIf { it.isNotEmpty() }?.let { lista ->
                Math.round(lista.average() * 10.0) / 10.0
            },
            failedCount = promedios.count { it < aprobado },
            attendanceRate = SubjectAttendanceHistory.summarize(entradas).rate,
            subjects = suyas.map { materia ->
                val propias = SubjectAttendanceHistory.build(
                    sessions = clases.sessions.filter { it.subjectId == materia.id },
                    occurrences = clases.occurrences,
                    today = hasta,
                    termStart = term.start,
                    termEnd = term.plannedEnd,
                    breaks = clases.breaks.map { it.range }
                )
                val promedio = GradeCalculator.calculateCurrentAverageByCuts(materia.grades, cortes)
                SubjectInTerm(
                    id = materia.id,
                    name = materia.name,
                    average = promedio,
                    attendanceRate = SubjectAttendanceHistory.summarize(propias).rate,
                    passed = promedio?.let { it >= aprobado }
                )
            },
            // La primera clase marcada es desde cuándo la cifra tiene apoyo real.
            attendanceSince = marcadas.minByOrNull { it.date }?.date
        )
    }

    /**
     * Cuántas clases se quedaron sin marcar, materia a materia.
     *
     * Con las mismas reglas del historial —periodo, días sin clase y la ventana— para que un
     * festivo no aparezca aquí como una clase que alguien olvidó.
     */
    private fun sinMarcarPorMateria(
        subjects: List<Subject>,
        clases: DatosDeClase,
        term: AcademicTerm,
        hoy: LocalDate
    ): Map<String, Int> {
        val ahora = LocalDateTime.now()
        return subjects.associate { materia ->
            val suyas = clases.sessions.filter { it.subjectId == materia.id }
            val entradas = SubjectAttendanceHistory.build(
                sessions = suyas,
                occurrences = clases.occurrences,
                today = hoy,
                termStart = term.start,
                termEnd = term.plannedEnd,
                breaks = clases.breaks.map { it.range }
            )
            // Sin tope: aquí interesa cuántas son, no cuántas caben en una lista.
            materia.id to SubjectAttendanceHistory.pendingToCatchUp(
                entries = entradas,
                now = ahora,
                limit = Int.MAX_VALUE
            ).size
        }.filterValues { it > 0 }
    }

    /**
     * Cierra el periodo activo.
     *
     * Antes estampa el periodo en las materias que no lo llevan: son las de antes de que
     * existieran los periodos, y si se quedaran sin él el histórico las perdería justo en el
     * momento en que empieza a haber histórico.
     */
    fun closeActiveTerm(closedOn: LocalDate = LocalDate.now(), onDone: () -> Unit = {}) {
        val activo = uiState.value.activeTerm ?: return
        viewModelScope.launch {
            gradesRepository.stampTerm(activo.id)
            termRepository.close(activo.id, closedOn)
                .onSuccess {
                    _message.value = "Cerraste ${activo.name}."
                    onDone()
                }
                .onFailure { error ->
                    _message.value = error.message ?: "No se pudo cerrar el periodo."
                }
        }
    }

    /** Empieza el siguiente, heredando de lo anterior lo que no cambia. */
    fun startTerm(
        name: String,
        type: AcademicTermType,
        start: LocalDate,
        plannedEnd: LocalDate?,
        onDone: () -> Unit = {}
    ) {
        viewModelScope.launch {
            termRepository.create(name, type, start, plannedEnd)
                .onSuccess {
                    _message.value = "Empezaste ${it.name}."
                    onDone()
                }
                .onFailure { error ->
                    _message.value = error.message ?: "No se pudo crear el periodo."
                }
        }
    }
}
