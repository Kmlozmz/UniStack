package com.unistack.app.feature_terms.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.R
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_notes.domain.NotesRepository
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicBreakRepository
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_terms.domain.CalculoDelHistorico
import com.unistack.app.feature_terms.domain.ClaseDelPeriodo
import com.unistack.app.feature_terms.domain.HistorialAcademico
import com.unistack.app.feature_terms.domain.PendienteDeCierre
import com.unistack.app.feature_terms.domain.PeriodoDelHistorico
import com.unistack.app.feature_terms.domain.PropuestaDePeriodo
import com.unistack.app.feature_terms.domain.PropuestaDelSiguientePeriodo
import com.unistack.app.feature_terms.domain.RepartoDeCortes
import com.unistack.app.feature_terms.domain.RevisionDeCierre
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TermsUiState(
    val loaded: Boolean = false,
    val historial: HistorialAcademico? = null,
    val propuesta: PropuestaDePeriodo? = null,
    val profile: UserProfile? = null,
    val tasks: List<StudentTask> = emptyList(),
    /** Lo que sigue funcionando sin periodo: tareas por hacer, notas y gastos del mes. */
    val tareasPendientes: Int = 0,
    val notasRapidas: Int = 0,
    val gastosDelMes: Int = 0
) {
    val activeTerm: AcademicTerm? get() = historial?.activo?.term

    /** El último cerrado, que es lo que resume Inicio mientras no hay periodo. */
    val lastClosed: PeriodoDelHistorico? get() = historial?.cerrados?.firstOrNull()
}

/** Una línea de «Resuelto ahora» en el cierre. */
data class ResueltoEnElCierre(val titulo: String, val detalle: String)

data class VistaPreviaDeNotas(
    val cortes: List<Double?>,
    val final: Double?,
    val promedioAntes: Double?,
    val promedioDespues: Double?
)

@HiltViewModel
class TermsViewModel @Inject constructor(
    private val termRepository: AcademicTermRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val scheduleRepository: ScheduleRepository,
    private val breakRepository: AcademicBreakRepository,
    private val userRepository: UserRepository,
    private val notesRepository: NotesRepository,
    private val expensesRepository: ExpensesRepository
) : ViewModel() {

    private data class Base(
        val terms: List<AcademicTerm>,
        val subjects: List<Subject>,
        val tasks: List<StudentTask>,
        val profile: UserProfile?
    )

    private data class Clases(
        val sessions: List<ClassSession>,
        val occurrences: List<ClassOccurrence>,
        val breaks: List<AcademicBreak>
    )

    private val base = combine(
        termRepository.terms,
        gradesRepository.subjects,
        tasksRepository.tasks,
        userRepository.userProfile
    ) { terms, subjects, tasks, profile -> Base(terms, subjects, tasks, profile) }

    private val clases = combine(
        scheduleRepository.sessions,
        scheduleRepository.occurrences,
        breakRepository.breaks
    ) { sessions, occurrences, breaks -> Clases(sessions, occurrences, breaks) }

    private val extras = combine(notesRepository.notes, expensesRepository.expenses) { notes, expenses -> notes to expenses }

    val uiState: StateFlow<TermsUiState> = combine(base, clases, extras, HistoricoDeMuestra.datos) { b, c, e, muestra ->
        if (muestra != null) construirDeMuestra(muestra, b.profile) else construir(b, c, e.first, e.second)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TermsUiState())

    private val _resueltos = MutableStateFlow<List<ResueltoEnElCierre>>(emptyList())
    val resueltos: StateFlow<List<ResueltoEnElCierre>> = _resueltos.asStateFlow()

    /** Lo que se contestó en el cierre sin cambiar ningún dato: «No la entregué». */
    private val ignoradas = MutableStateFlow<Set<String>>(emptySet())

    val pendientes: StateFlow<List<PendienteDeCierre>> = combine(uiState, ignoradas) { estado, fuera ->
        val historial = estado.historial ?: return@combine emptyList<PendienteDeCierre>()
        val activo = historial.activo ?: return@combine emptyList<PendienteDeCierre>()
        RevisionDeCierre.pendientes(activo, estado.tasks, historial.hoy, fuera)
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun construir(b: Base, c: Clases, notes: List<QuickNote>, expenses: List<Expense>): TermsUiState {
        val hoy = LocalDate.now()
        val perfil = b.profile
        val maxima = perfil?.let { GradingScaleUtils.maxGradeFor(it) } ?: 5.0
        val historial = CalculoDelHistorico.build(
            terms = b.terms,
            subjects = b.subjects,
            sessions = c.sessions,
            occurrences = c.occurrences,
            breaks = c.breaks.map { it.range },
            tasks = b.tasks,
            esquemaActual = perfil?.gradingCutScheme ?: GradingCutScheme.default(),
            notaMaxima = maxima,
            aprobado = perfil?.passingGrade ?: maxima * 0.6,
            hoy = hoy,
            ahora = LocalDateTime.now()
        )
        val zona = ZoneId.systemDefault()
        return TermsUiState(
            loaded = true,
            historial = historial,
            propuesta = PropuestaDelSiguientePeriodo.build(b.terms, hoy),
            profile = perfil,
            tasks = b.tasks,
            tareasPendientes = b.tasks.count { !it.completed },
            notasRapidas = notes.count { !it.archived && it.deletedAt == null },
            gastosDelMes = expenses
                .filter {
                    val dia = Instant.ofEpochMilli(it.dateMillis).atZone(zona).toLocalDate()
                    dia.year == hoy.year && dia.month == hoy.month
                }
                .sumOf { it.amount }
        )
    }

    /**
     * El estado con la simulación del banco de pruebas puesta.
     *
     * Se quedan del perfil real los avisos y los módulos; lo académico es el de la simulación
     * —escala de 0 a 5, aprobado en 3.0, cortes al 30/30/40 y el tope de faltas en 8, que es lo
     * que daba «7 permitidas» sobre 36 clases— para que las cifras sean las mismas. Las tres
     * casillas de «Mientras tanto» también son las suyas.
     */
    private fun construirDeMuestra(muestra: DatosDeMuestra, perfilReal: UserProfile?): TermsUiState {
        val historial = CalculoDelHistorico.build(
            terms = muestra.terms,
            subjects = muestra.subjects,
            sessions = muestra.sessions,
            occurrences = muestra.occurrences,
            breaks = emptyList(),
            tasks = muestra.tasks,
            esquemaActual = muestra.esquema,
            notaMaxima = 5.0,
            aprobado = 3.0,
            hoy = muestra.hoy,
            ahora = muestra.ahora
        )
        return TermsUiState(
            loaded = true,
            historial = historial,
            propuesta = PropuestaDelSiguientePeriodo.build(muestra.terms, muestra.hoy),
            profile = perfilReal?.copy(
                gradingScale = GradingScale.ZERO_TO_FIVE,
                passingGrade = 3.0,
                targetAverage = 4.0,
                absenceLimit = 8,
                gradingCutScheme = muestra.esquema,
                enabledModules = perfilReal.enabledModules + AppModule.TASKS + AppModule.EXPENSES
            ),
            tasks = muestra.tasks,
            tareasPendientes = 2,
            notasRapidas = 14,
            gastosDelMes = 214_000
        )
    }

    /** Las materias de donde se esté leyendo: la simulación si está puesta, si no las reales. */
    private fun materias(): List<Subject> = HistoricoDeMuestra.datos.value?.subjects ?: gradesRepository.subjects.value

    // ================================================================== cerrar

    /** Empieza una revisión de cierre desde cero. */
    fun empezarRevision() {
        _resueltos.value = emptyList()
        ignoradas.value = emptySet()
    }

    /**
     * Cierra el periodo activo y guarda con él sus cortes.
     *
     * Antes estampa el periodo en las materias que no lo llevan: son las de antes de que
     * existieran los periodos, y si se quedaran sin él el histórico las perdería justo cuando
     * empieza a haber histórico.
     */
    fun cerrarPeriodo(onDone: (termId: String, nombre: String, acumulado: Double?) -> Unit) {
        val estado = uiState.value
        val historial = estado.historial ?: return
        val activo = historial.activo ?: return
        val acumulado = historial.acumuladoAlCerrar(activo)
        val esquema = estado.profile?.gradingCutScheme ?: GradingCutScheme.default()
        if (HistoricoDeMuestra.enMarcha) {
            HistoricoDeMuestra.cerrar(activo.id, esquema)
            onDone(activo.id, activo.nombre, acumulado)
            return
        }
        viewModelScope.launch {
            gradesRepository.stampTerm(activo.id)
            termRepository.close(activo.id, LocalDate.now(), esquema)
                .onSuccess { onDone(activo.id, activo.nombre, acumulado) }
                .onFailure { error ->
                    MensajesDelHistorico.publicar(error.message ?: Textos.get(R.string.terms_close_failed))
                }
        }
    }

    fun deshacerCierre(termId: String, nombre: String) {
        if (HistoricoDeMuestra.enMarcha) {
            HistoricoDeMuestra.reabrir(termId)
            MensajesDelHistorico.publicar(Textos.get(R.string.hist_vuelve_en_curso, nombre))
            return
        }
        viewModelScope.launch {
            termRepository.reopen(termId)
                .onSuccess { MensajesDelHistorico.publicar(Textos.get(R.string.hist_vuelve_en_curso, nombre)) }
                .onFailure { error ->
                    MensajesDelHistorico.publicar(error.message ?: Textos.get(R.string.terms_close_failed))
                }
        }
    }

    /** La nota de un corte que faltaba, como resultado oficial del corte. */
    fun ponerNotaDeCorte(subjectId: String, cutId: String, valor: Double) {
        val materia = materias().firstOrNull { it.id == subjectId } ?: return
        val esquema = uiState.value.profile?.gradingCutScheme ?: GradingCutScheme.default()
        val corte = esquema.cuts.firstOrNull { it.id == cutId } ?: return
        val nota = notaOficial(corte.name, cutId, valor)
        if (HistoricoDeMuestra.enMarcha) HistoricoDeMuestra.ponerNota(subjectId, nota) else gradesRepository.addGrade(subjectId, nota)
        val final = finalCon(materia.copy(grades = materia.grades + nota))
        val numero = esquema.cuts.sortedBy { it.order }.indexOfFirst { it.id == cutId } + 1
        _resueltos.value = _resueltos.value + ResueltoEnElCierre(
            titulo = materia.name,
            detalle = Textos.get(R.string.hist_resuelto_corte, numero, formatoNota(valor), formatoNota(final))
        )
        MensajesDelHistorico.publicar(Textos.get(R.string.hist_mensaje_final, materia.name, formatoNota(final)))
    }

    fun marcarClases(subjectId: String, marcas: Map<ClaseDelPeriodo, ClassAttendanceStatus>) {
        val materia = materias().firstOrNull { it.id == subjectId } ?: return
        val ahora = System.currentTimeMillis()
        marcas.forEach { (clase, estado) ->
            if (HistoricoDeMuestra.enMarcha) {
                HistoricoDeMuestra.marcar(clase, estado)
                return@forEach
            }
            val dia = clase.date.toEpochDay()
            val existente = scheduleRepository.occurrences.value.firstOrNull {
                it.sessionId == clase.sessionId && it.dateEpochDay == dia
            }
            scheduleRepository.saveOccurrence(
                (existente ?: ClassOccurrence(
                    id = "occurrence-${clase.sessionId}-$dia",
                    sessionId = clase.sessionId,
                    dateEpochDay = dia,
                    updatedAt = ahora
                )).copy(status = estado, updatedAt = ahora)
            )
        }
        val fui = marcas.values.count { it == ClassAttendanceStatus.ATTENDED }
        val periodo = uiState.value.historial?.materia(subjectId)?.second
        val clasesDespues = periodo?.asistencia?.clases?.map { clase -> marcas[clase]?.let { clase.copy(status = it) } ?: clase }
        val porcentaje = clasesDespues?.let { CalculoDelHistorico.asistencia(it).porcentaje }
        _resueltos.value = _resueltos.value + ResueltoEnElCierre(
            titulo = materia.name,
            detalle = Textos.get(R.string.hist_resuelto_clases, fui, marcas.size, formatoPorcentaje(porcentaje))
        )
    }

    fun resolverTarea(pendiente: PendienteDeCierre.Tarea, entregada: Boolean) {
        if (entregada) {
            if (HistoricoDeMuestra.enMarcha) {
                HistoricoDeMuestra.completarTarea(pendiente.tarea.id)
            } else {
                tasksRepository.setTaskCompleted(pendiente.tarea.id, true)
            }
        } else {
            ignoradas.value = ignoradas.value + pendiente.clave
        }
        _resueltos.value = _resueltos.value + ResueltoEnElCierre(
            titulo = pendiente.materia?.nombre ?: pendiente.tarea.title,
            detalle = Textos.get(
                if (entregada) R.string.hist_resuelto_tarea_si else R.string.hist_resuelto_tarea_no,
                pendiente.tarea.title
            )
        )
    }

    // ================================================================== notas

    /**
     * Guarda lo corregido de una materia: notas que cambian y notas de corte nuevas.
     *
     * Devuelve el texto del aviso con cuánto se movió el promedio del periodo, calculado con
     * las notas ya puestas y no esperando a que la base conteste.
     */
    fun guardarNotas(subjectId: String, valores: Map<String, Double>, nuevasDeCorte: Map<String, Double>) {
        val estado = uiState.value
        val (periodo, materia) = estado.historial?.materia(subjectId) ?: return
        val esquema = periodo.esquema
        val nuevas = mutableListOf<GradeItem>()
        materia.subject.grades.forEach { nota ->
            val valor = valores[nota.id] ?: return@forEach
            if (valor != nota.value) {
                val cambiada = nota.copy(value = valor)
                if (HistoricoDeMuestra.enMarcha) {
                    HistoricoDeMuestra.cambiarNota(subjectId, cambiada)
                } else {
                    gradesRepository.updateGrade(subjectId, cambiada)
                }
                nuevas += cambiada
            } else {
                nuevas += nota
            }
        }
        val sinCambiar = materia.subject.grades.filter { it.id !in valores.keys }
        nuevasDeCorte.forEach { (cutId, valor) ->
            val corte = esquema.cuts.firstOrNull { it.id == cutId } ?: return@forEach
            val nota = notaOficial(corte.name, cutId, valor)
            if (HistoricoDeMuestra.enMarcha) HistoricoDeMuestra.ponerNota(subjectId, nota) else gradesRepository.addGrade(subjectId, nota)
            nuevas += nota
        }
        val finalAntes = materia.final
        val finalDespues = finalCon(materia.subject.copy(grades = sinCambiar + nuevas), esquema)
        val antes = periodo.promedio
        val despues = periodo.materias
            .mapNotNull { if (it.id == subjectId) finalDespues else it.final }
            .takeIf { it.isNotEmpty() }
            ?.average()
        val mensaje = if (antes != null && despues != null && kotlin.math.abs(despues - antes) >= 0.0005) {
            val detalle = if (finalAntes != finalDespues) {
                Textos.get(R.string.hist_guardado_detalle, materia.nombre, formatoNota(finalAntes), formatoNota(finalDespues))
            } else {
                ""
            }
            Textos.get(R.string.hist_guardado_movio, periodo.nombre, formatoPromedio(antes), formatoPromedio(despues)) + detalle
        } else {
            Textos.get(R.string.hist_guardado_igual, materia.nombre, formatoNota(finalDespues))
        }
        MensajesDelHistorico.publicar(mensaje)
    }

    /**
     * Cómo quedarían los cortes, la final y el promedio del periodo con lo que se está escribiendo.
     *
     * Es lo que el formulario enseña mientras se edita; no guarda nada.
     */
    fun vistaPrevia(subjectId: String, valores: Map<String, Double>, nuevasDeCorte: Map<String, Double>): VistaPreviaDeNotas? {
        val (periodo, materia) = uiState.value.historial?.materia(subjectId) ?: return null
        val esquema = periodo.esquema
        val notas = materia.subject.grades.map { nota -> valores[nota.id]?.let { nota.copy(value = it) } ?: nota } +
            nuevasDeCorte.mapNotNull { (cutId, valor) ->
                esquema.cuts.firstOrNull { it.id == cutId }?.let { notaOficial(it.name, cutId, valor) }
            }
        val final = finalCon(materia.subject.copy(grades = notas), esquema)
        return VistaPreviaDeNotas(
            cortes = esquema.cuts.sortedBy { it.order }.map { corte ->
                GradeCalculator.calculateCut(notas.filter { it.cutId == corte.id }).let { if (it.isComplete) it.average else null }
            },
            final = final,
            promedioAntes = periodo.promedio,
            promedioDespues = periodo.materias
                .mapNotNull { if (it.id == subjectId) final else it.final }
                .takeIf { it.isNotEmpty() }
                ?.average()
        )
    }

    // ================================================================== periodo nuevo

    /**
     * Crea el periodo con lo elegido y le pone las fechas de corte.
     *
     * Las materias perdidas se traen **vacías**: sin notas y sin el horario del anterior.
     * Repetir es cursarla otra vez, no arrastrar lo que salió mal.
     */
    fun crearPeriodo(
        nombre: String,
        inicio: LocalDate,
        semanas: Int,
        semanasPorCorte: List<Int>?,
        repetir: Set<String>,
        onDone: (AcademicTerm) -> Unit
    ) {
        val estado = uiState.value
        val propuesta = estado.propuesta ?: return
        if (HistoricoDeMuestra.enMarcha) {
            val cortes = estado.profile?.gradingCutScheme?.cuts?.size ?: 3
            HistoricoDeMuestra.crear(
                nombre = nombre.trim(),
                tipo = propuesta.tipo,
                inicio = inicio,
                finPrevisto = propuesta.finPara(inicio, semanas),
                cierres = semanasPorCorte?.takeIf { it.size == cortes }?.let { RepartoDeCortes.cierres(inicio, it) },
                repetir = repetir
            )?.let(onDone)
            return
        }
        viewModelScope.launch {
            termRepository.create(nombre.trim(), propuesta.tipo, inicio, propuesta.finPara(inicio, semanas))
                .onSuccess { periodo ->
                    estado.profile?.let { perfil ->
                        val cortes = perfil.gradingCutScheme.cuts.sortedBy { it.order }
                        val cierres = semanasPorCorte
                            ?.takeIf { it.size == cortes.size }
                            ?.let { RepartoDeCortes.cierres(inicio, it) }
                        val esquema = GradingCutScheme(cortes.mapIndexed { i, corte -> corte.copy(endEpochDay = cierres?.get(i)) })
                        if (esquema.isValid && esquema != perfil.gradingCutScheme) {
                            userRepository.saveUserProfile(
                                perfil.copy(gradingCutScheme = esquema, updatedAt = System.currentTimeMillis())
                            )
                        }
                    }
                    repetirMaterias(repetir, periodo.id)
                    onDone(periodo)
                }
                .onFailure { error ->
                    MensajesDelHistorico.publicar(error.message ?: Textos.get(R.string.terms_create_failed))
                }
        }
    }

    private fun repetirMaterias(ids: Set<String>, termId: String) {
        if (ids.isEmpty()) return
        val existentes = gradesRepository.subjects.value
        ids.forEach { id ->
            val original = existentes.firstOrNull { it.id == id } ?: return@forEach
            gradesRepository.addSubject(
                original.copy(
                    id = "subject-" + UUID.randomUUID().toString(),
                    grades = emptyList(),
                    termId = termId,
                    repeatedFromSubjectId = original.id,
                    // El corte elegido y lo que se dio por perdido eran del intento anterior.
                    activeCutId = "",
                    unknownCutIds = emptySet(),
                    closedCutIds = emptySet(),
                    historyPromptStatus = PriorHistoryPromptStatus.NOT_SHOWN
                )
            )
        }
    }

    // ================================================================== avisos

    fun setAvisoAlAcabar(activo: Boolean) = guardarPerfil { it.copy(termEndReminderEnabled = activo) }

    fun setAvisoParaEmpezar(activo: Boolean) = guardarPerfil { it.copy(nextTermReminderEnabled = activo) }

    fun setDiaDelAviso(opcion: Int) = guardarPerfil { it.copy(nextTermReminderOffset = opcion.coerceIn(0, 2)) }

    private fun guardarPerfil(cambio: (UserProfile) -> UserProfile) {
        val perfil = userRepository.userProfile.value ?: return
        userRepository.saveUserProfile(cambio(perfil).copy(updatedAt = System.currentTimeMillis()))
    }

    // ================================================================== ayudas

    private fun notaOficial(nombreDelCorte: String, cutId: String, valor: Double) = GradeItem(
        id = "grade-${UUID.randomUUID()}",
        name = Textos.get(R.string.grade_final_result, nombreDelCorte),
        value = valor,
        percentage = 1.0,
        cutId = cutId,
        source = GradeSource.PERIOD_FINAL,
        recordedAt = System.currentTimeMillis()
    )

    private fun finalCon(subject: Subject, esquema: GradingCutScheme? = null): Double? {
        val perfil = uiState.value.profile
        val maxima = perfil?.let { GradingScaleUtils.maxGradeFor(it) } ?: 5.0
        val cortes = (esquema ?: perfil?.gradingCutScheme ?: GradingCutScheme.default()).cuts.sortedBy { it.order }
        val calculo = GradeCalculator.calculateSubject(
            grades = subject.grades,
            cuts = cortes,
            targetAverage = subject.targetAverage,
            maxGrade = maxima,
            passingGrade = perfil?.passingGrade
        )
        return if (calculo.isFinished) calculo.guaranteedMinimum else null
    }

    private fun formatoNota(valor: Double?): String =
        GradingScaleUtils.formatGrade(valor, uiState.value.profile?.gradingScale ?: com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE)

    private fun formatoPromedio(valor: Double?): String =
        formatoDePromedio(valor, uiState.value.historial?.notaMaxima ?: 5.0)

    private fun formatoPorcentaje(valor: Double?): String =
        valor?.let { "${Math.round(it)}%" } ?: com.unistack.app.core.utils.NO_DATA
}

/**
 * Los avisos del histórico, de una pantalla a otra.
 *
 * Cada destino tiene su propio ViewModel, así que el que guarda las notas o cierra el periodo ya
 * no existe cuando se enseña el aviso. El mensaje espera aquí a la pantalla que lo pinte.
 */
object MensajesDelHistorico {
    data class Mensaje(
        val texto: String,
        /** Si trae «Deshacer», el periodo que se acaba de cerrar. */
        val deshacerCierre: Pair<String, String>? = null,
        val id: Long = System.nanoTime()
    )

    private val _mensaje = MutableStateFlow<Mensaje?>(null)
    val mensaje: StateFlow<Mensaje?> = _mensaje.asStateFlow()

    fun publicar(texto: String, deshacerCierre: Pair<String, String>? = null) {
        _mensaje.value = Mensaje(texto, deshacerCierre)
    }

    fun consumir(id: Long) {
        if (_mensaje.value?.id == id) _mensaje.value = null
    }
}

/** Dos decimales en la escala de 0 a 5 y uno en las de cien, que es lo que se lee. */
internal fun formatoDePromedio(valor: Double?, notaMaxima: Double): String {
    if (valor == null) return com.unistack.app.core.utils.NO_DATA
    return String.format(java.util.Locale.US, if (notaMaxima <= 10.0) "%.2f" else "%.1f", valor)
}
