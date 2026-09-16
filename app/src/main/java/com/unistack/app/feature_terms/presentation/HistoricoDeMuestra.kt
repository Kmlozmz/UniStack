package com.unistack.app.feature_terms.presentation

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermStatus
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_terms.domain.ClaseDelPeriodo
import com.unistack.app.feature_terms.domain.RepartoDeCortes
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingCutScheme
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Lo que el histórico lee mientras la simulación está puesta, en lugar de los repositorios. */
data class DatosDeMuestra(
    val terms: List<AcademicTerm>,
    val subjects: List<Subject>,
    val sessions: List<ClassSession>,
    val occurrences: List<ClassOccurrence>,
    val tasks: List<StudentTask>,
    /** El esquema «de las preferencias»: el del periodo en curso, con sus fechas. */
    val esquema: GradingCutScheme,
    val hoy: LocalDate,
    val ahora: LocalDateTime
)

/**
 * El histórico de la simulación aprobada, para mirarlo en el teléfono igual que en el artifact.
 *
 * **Vive en memoria y no toca la base de datos.** Sembrar cuatro periodos de verdad no servía:
 * solo puede haber un periodo activo, así que habría que aparcar el real, y las clases de las
 * materias sembradas seguirían sonando como recordatorios en el teléfono de uso diario. Aquí el
 * histórico, Inicio y Configuración académica leen estos datos mientras la simulación está
 * puesta, y todo lo demás de la app sigue con lo real.
 *
 * El «hoy» es el de la simulación, el sábado 5 de diciembre de 2026 a las 10:24, y los datos
 * salen de las mismas tablas y fórmulas que el artifact —hasta el azar de las faltas, con la
 * misma semilla—, así que las cifras coinciden. Cerrar, crear, corregir notas y marcar clases
 * cambian esta copia. Salir, o cerrar la app, la borra.
 */
object HistoricoDeMuestra {

    private val _datos = MutableStateFlow<DatosDeMuestra?>(null)
    val datos: StateFlow<DatosDeMuestra?> = _datos.asStateFlow()

    val enMarcha: Boolean get() = _datos.value != null

    private const val MARCA = "prueba-hist-"
    private val HOY: LocalDate = LocalDate.of(2026, 12, 5)
    private val AHORA: LocalDateTime = HOY.atTime(10, 24)
    private val PESOS = listOf(0.30, 0.30, 0.40)
    private val HORARIOS = listOf(0 to 2, 1 to 3, 2 to 4, 0 to 3, 1 to 4, 0 to 2)

    // ================================================================== saltos

    /** 2026-2 terminado hace 8 días y sin cerrar: el punto de partida del artifact. */
    fun empezar() {
        _datos.value = construir()
    }

    /** 2026-2 ya cerrado hoy: Inicio sin periodo. */
    fun saltarASinPeriodo() {
        val datos = construir()
        _datos.value = datos
        datos.terms.firstOrNull { it.isActive }?.let { cerrar(it.id, datos.esquema) }
    }

    /** 2027-1 creado, con la materia perdida traída: Inicio con el periodo por empezar. */
    fun saltarAPorEmpezar() {
        saltarASinPeriodo()
        val datos = _datos.value ?: return
        val perdida = datos.subjects.firstOrNull { it.termId == "${MARCA}2026-2" && it.name == "Ecuaciones diferenciales" }
        val inicio = LocalDate.of(2027, 1, 25)
        crear(
            nombre = "2027-1",
            tipo = AcademicTermType.SEMESTER,
            inicio = inicio,
            finPrevisto = inicio.plusDays(18 * 7L - 3L),
            cierres = RepartoDeCortes.cierres(inicio, RepartoDeCortes.iguales(18, 3)),
            repetir = setOfNotNull(perdida?.id)
        )
    }

    fun salir() {
        _datos.value = null
    }

    // ================================================================== cambios

    fun cerrar(termId: String, esquema: GradingCutScheme) {
        actualizar { d ->
            d.copy(terms = d.terms.map { term ->
                if (term.id != termId || !term.isActive) term else term.copy(
                    closedEpochDay = d.hoy.toEpochDay(),
                    status = AcademicTermStatus.CLOSED,
                    cutScheme = esquema
                )
            })
        }
    }

    fun reabrir(termId: String) {
        actualizar { d ->
            if (d.terms.any { it.isActive }) return@actualizar d
            d.copy(terms = d.terms.map { term ->
                if (term.id != termId) term else term.copy(closedEpochDay = null, status = AcademicTermStatus.ACTIVE, cutScheme = null)
            })
        }
    }

    fun crear(
        nombre: String,
        tipo: AcademicTermType,
        inicio: LocalDate,
        finPrevisto: LocalDate,
        cierres: List<Long?>?,
        repetir: Set<String>
    ): AcademicTerm? {
        val datos = _datos.value ?: return null
        if (datos.terms.any { it.isActive }) return null
        val term = AcademicTerm(
            id = MARCA + nombre,
            userId = "prueba",
            name = nombre,
            type = tipo,
            startEpochDay = inicio.toEpochDay(),
            plannedEndEpochDay = finPrevisto.toEpochDay(),
            closedEpochDay = null,
            status = AcademicTermStatus.ACTIVE,
            createdAt = 0,
            updatedAt = 0
        )
        val copias = datos.subjects.filter { it.id in repetir }.map { original ->
            original.copy(
                id = MARCA + "mat-" + UUID.randomUUID(),
                grades = emptyList(),
                termId = term.id,
                repeatedFromSubjectId = original.id,
                activeCutId = "",
                unknownCutIds = emptySet(),
                closedCutIds = emptySet(),
                historyPromptStatus = PriorHistoryPromptStatus.NOT_SHOWN
            )
        }
        val cortes = datos.esquema.cuts.sortedBy { it.order }
        _datos.value = datos.copy(
            terms = datos.terms + term,
            subjects = datos.subjects + copias,
            esquema = GradingCutScheme(cortes.mapIndexed { i, corte -> corte.copy(endEpochDay = cierres?.getOrNull(i)) })
        )
        return term
    }

    fun ponerNota(subjectId: String, nota: GradeItem) {
        actualizar { d ->
            d.copy(subjects = d.subjects.map { if (it.id == subjectId) it.copy(grades = it.grades + nota) else it })
        }
    }

    fun cambiarNota(subjectId: String, nota: GradeItem) {
        actualizar { d ->
            d.copy(subjects = d.subjects.map { materia ->
                if (materia.id != subjectId) materia else materia.copy(grades = materia.grades.map { if (it.id == nota.id) nota else it })
            })
        }
    }

    fun marcar(clase: ClaseDelPeriodo, estado: ClassAttendanceStatus) {
        actualizar { d ->
            val dia = clase.date.toEpochDay()
            val sinEsa = d.occurrences.filterNot { it.sessionId == clase.sessionId && it.dateEpochDay == dia }
            d.copy(occurrences = sinEsa + ClassOccurrence(
                id = "${MARCA}o-${clase.sessionId}-$dia",
                sessionId = clase.sessionId,
                dateEpochDay = dia,
                status = estado,
                updatedAt = 0
            ))
        }
    }

    fun completarTarea(taskId: String) {
        actualizar { d ->
            d.copy(tasks = d.tasks.map { if (it.id == taskId) it.copy(completed = true, completedAt = 0L) else it })
        }
    }

    private fun actualizar(cambio: (DatosDeMuestra) -> DatosDeMuestra) {
        val actual = _datos.value ?: return
        _datos.value = cambio(actual)
    }

    // ================================================================== los datos del artifact

    private class Materia(
        val nombre: String,
        val cortes: List<Double?>,
        val faltas: Int,
        val noHubo: Int,
        val tareas: Pair<Int, Int>,
        val pendientes: Int = 0,
        val vencida: String? = null,
        val repiteDe: String? = null
    )

    private class Periodo(
        val id: String,
        val inicio: LocalDate,
        val semanas: Int,
        val cerro: LocalDate?,
        val materias: List<Materia>
    )

    /** `FUENTE` del artifact, tal cual. Inglés IV no es anual: se eligió sin anuales. */
    private val FUENTE = listOf(
        Periodo("2026-2", LocalDate.of(2026, 7, 27), 18, null, listOf(
            Materia("Estructuras de datos", listOf(4.6, 4.8, 4.7), 1, 1, 9 to 10, vencida = "Proyecto final"),
            Materia("Física II", listOf(4.1, 3.8, null), 2, 1, 6 to 6),
            Materia("Probabilidad", listOf(3.9, 3.7, 3.8), 3, 0, 5 to 6, pendientes = 3),
            Materia("Ecuaciones diferenciales", listOf(2.8, 3.0, 2.9), 2, 1, 4 to 6),
            Materia("Cálculo III", listOf(3.6, 4.0, 3.9), 3, 0, 7 to 7),
            Materia("Inglés IV", listOf(4.3, 4.1, 4.4), 2, 2, 8 to 8)
        )),
        Periodo("2026-1", LocalDate.of(2026, 1, 26), 19, LocalDate.of(2026, 6, 5), listOf(
            Materia("Programación II", listOf(4.5, 4.8, 4.5), 2, 1, 10 to 10),
            Materia("Física I", listOf(4.0, 4.5, 4.4), 3, 0, 6 to 7),
            Materia("Inglés III", listOf(4.2, 4.0, 4.3), 4, 2, 8 to 8),
            Materia("Estadística", listOf(3.8, 4.2, 4.0), 3, 1, 5 to 6),
            Materia("Álgebra lineal", listOf(3.6, 4.1, 4.0), 4, 0, 7 to 8),
            Materia("Cálculo II", listOf(3.5, 3.9, 3.9), 4, 1, 8 to 9, repiteDe = "2025-2")
        )),
        Periodo("2025-2", LocalDate.of(2025, 7, 28), 18, LocalDate.of(2025, 11, 28), listOf(
            Materia("Programación I", listOf(4.2, 4.6, 4.4), 3, 0, 9 to 9),
            Materia("Expresión oral", listOf(4.0, 4.1, 3.9), 5, 1, 4 to 4),
            Materia("Química general", listOf(3.2, 3.7, 3.6), 4, 1, 5 to 7),
            Materia("Inglés II", listOf(3.5, 3.3, 3.4), 5, 0, 6 to 8),
            Materia("Cálculo II", listOf(2.9, 2.4, 2.8), 4, 2, 5 to 9)
        )),
        Periodo("2025-1", LocalDate.of(2025, 2, 3), 18, LocalDate.of(2025, 6, 6), listOf(
            Materia("Introducción a la ingeniería", listOf(4.1, 4.3, 4.2), 3, 1, 6 to 6),
            Materia("Inglés I", listOf(3.8, 4.0, 3.9), 4, 0, 7 to 8),
            Materia("Dibujo técnico", listOf(3.5, 3.9, 3.7), 3, 1, 9 to 10),
            Materia("Cálculo I", listOf(3.4, 3.8, 3.6), 4, 1, 6 to 7),
            Materia("Cátedra universitaria", listOf(3.9, 3.4, 3.6), 4, 0, 3 to 3)
        ))
    )

    private fun construir(): DatosDeMuestra {
        val terms = mutableListOf<AcademicTerm>()
        val subjects = mutableListOf<Subject>()
        val sessions = mutableListOf<ClassSession>()
        val occurrences = mutableListOf<ClassOccurrence>()
        val tasks = mutableListOf<StudentTask>()
        var esquemaActivo = esquema(LocalDate.of(2026, 7, 27), 18)

        FUENTE.forEach { periodo ->
            val termId = MARCA + periodo.id
            val cortes = semanasDeCorte(periodo.semanas)
            val esquema = esquema(periodo.inicio, periodo.semanas)
            val fin = periodo.inicio.plusDays(periodo.semanas * 7L - 3L)
            if (periodo.cerro == null) esquemaActivo = esquema
            terms += AcademicTerm(
                id = termId,
                userId = "prueba",
                name = periodo.id,
                type = AcademicTermType.SEMESTER,
                startEpochDay = periodo.inicio.toEpochDay(),
                plannedEndEpochDay = fin.toEpochDay(),
                closedEpochDay = periodo.cerro?.toEpochDay(),
                status = if (periodo.cerro != null) AcademicTermStatus.CLOSED else AcademicTermStatus.ACTIVE,
                createdAt = 0,
                updatedAt = 0,
                cutScheme = if (periodo.cerro != null) esquema else null
            )
            periodo.materias.forEachIndexed { idx, m ->
                val subjectId = "$MARCA${periodo.id}-$idx"
                subjects += Subject(
                    id = subjectId,
                    name = m.nombre,
                    targetAverage = 4.0,
                    grades = m.cortes.flatMapIndexed { k, nota -> evaluaciones(subjectId, m.nombre, k, nota, periodo.inicio, cortes) },
                    cutScheme = esquema,
                    termId = termId,
                    repeatedFromSubjectId = m.repiteDe?.let { anterior ->
                        FUENTE.first { it.id == anterior }.materias.indexOfFirst { it.nombre == m.nombre }
                            .takeIf { it >= 0 }?.let { "$MARCA$anterior-$it" }
                    }
                )
                val (sesion, marcadas) = sesiones(subjectId, periodo.id, m, periodo.inicio, periodo.semanas, idx)
                sessions += sesion
                occurrences += marcadas
                tasks += tareas(subjectId, m, periodo.inicio, periodo.semanas, fin)
            }
        }
        return DatosDeMuestra(terms, subjects, sessions, occurrences, tasks, esquemaActivo, HOY, AHORA)
    }

    /** `semanasDeCorte` del artifact: iguales, y lo que sobre al último. */
    private fun semanasDeCorte(total: Int): List<Int> {
        val b = total / 3
        return listOf(b, b, total - 2 * b)
    }

    /** Los cortes al 30/30/40 con sus fechas: cada uno acaba el viernes de su última semana. */
    private fun esquema(inicio: LocalDate, semanas: Int): GradingCutScheme {
        val cortes = semanasDeCorte(semanas)
        var acumuladas = 0
        return GradingCutScheme(
            cortes.mapIndexed { k, w ->
                acumuladas += w
                GradingCut(
                    id = "period-${k + 1}",
                    name = "${Corte.Singular} ${k + 1}",
                    weight = PESOS[k],
                    order = k + 1,
                    endEpochDay = if (k < cortes.lastIndex) inicio.plusDays(acumuladas * 7L - 3L).toEpochDay() else null
                )
            }
        )
    }

    /** `evaluaciones` del artifact: dos notas por corte que promedian exactamente la del corte. */
    private fun evaluaciones(subjectId: String, nombre: String, k: Int, nota: Double?, inicio: LocalDate, cortes: List<Int>): List<GradeItem> {
        if (nota == null) return emptyList()
        val fin = cortes.take(k + 1).sum()
        val ini = fin - cortes[k]
        val fParcial = inicio.plusDays((fin - 1) * 7L + 2L)
        val fOtra = inicio.plusDays((ini + cortes[k] / 2) * 7L + 3L)
        val otra = if (k == 1) "Quiz ${k + 1}" else "Taller ${k + 1}"
        var d = listOf(0.2, -0.2, 0.4, -0.4)[hashN(nombre + k) % 4]
        if (nota >= 4.6) d = 0.2
        if (nota + d > 5 || nota - 1.5 * d > 5 || nota - 1.5 * d < 0) d = -0.2
        return listOf(
            GradeItem(
                id = "$MARCA$subjectId-g$k-a",
                name = otra,
                value = r1(nota - 1.5 * d),
                percentage = 0.40,
                cutId = "period-${k + 1}",
                recordedAt = milis(fOtra)
            ),
            GradeItem(
                id = "$MARCA$subjectId-g$k-b",
                name = "Parcial ${k + 1}",
                value = r1(nota + d),
                percentage = 0.60,
                cutId = "period-${k + 1}",
                recordedAt = milis(fParcial)
            )
        )
    }

    /**
     * `sesiones` del artifact: dos clases por semana, con las faltas y las que no hubo repartidas
     * por el mismo azar con semilla, y las últimas sin marcar si la materia las tiene.
     */
    private fun sesiones(
        subjectId: String,
        pid: String,
        m: Materia,
        inicio: LocalDate,
        semanas: Int,
        idx: Int
    ): Pair<ClassSession, List<ClassOccurrence>> {
        val total = semanas * 2
        val azar = Azar(pid + m.nombre)
        val estados = Array(total) { "fui" }
        for (i in total - m.pendientes until total) estados[i] = "pendiente"
        fun libre(): Int {
            var i: Int
            var g = 0
            do {
                i = Math.floor(azar.siguiente() * (total - m.pendientes - 2)).toInt()
                g++
            } while (estados[i] != "fui" && g < 500)
            return i
        }
        repeat(m.faltas) { estados[libre()] = "falte" }
        repeat(m.noHubo) { estados[libre()] = "nohubo" }
        val (dia1, dia2) = HORARIOS[idx % HORARIOS.size]
        val sesion = ClassSession(
            id = "${MARCA}s-$subjectId",
            subjectId = subjectId,
            daysOfWeek = setOf(dia1 + 1, dia2 + 1),
            startMinute = 8 * 60,
            endMinute = 10 * 60,
            reminderMinutes = 0,
            createdAt = 0,
            updatedAt = 0,
            recurrenceStartEpochDay = inicio.toEpochDay()
        )
        val marcadas = estados.mapIndexedNotNull { i, estado ->
            val fecha = inicio.plusDays((i / 2) * 7L + if (i % 2 == 0) dia1 else dia2)
            val status = when (estado) {
                "fui" -> ClassAttendanceStatus.ATTENDED
                "falte" -> ClassAttendanceStatus.ABSENT
                "nohubo" -> ClassAttendanceStatus.CANCELLED
                else -> return@mapIndexedNotNull null
            }
            ClassOccurrence(
                id = "${MARCA}o-${sesion.id}-${fecha.toEpochDay()}",
                sessionId = sesion.id,
                dateEpochDay = fecha.toEpochDay(),
                status = status,
                updatedAt = 0
            )
        }
        return sesion to marcadas
    }

    /**
     * Las tareas para que salga «9 de 10». Las que no se hicieron vencen después del periodo,
     * salvo la «Proyecto final» de Estructuras de datos, que venció el día antes del fin.
     */
    private fun tareas(subjectId: String, m: Materia, inicio: LocalDate, semanas: Int, fin: LocalDate): List<StudentTask> {
        val (hechas, total) = m.tareas
        return (0 until total).map { i ->
            val hecha = i < hechas
            val esLaVencida = !hecha && m.vencida != null && i == hechas
            val vence = when {
                esLaVencida -> fin.minusDays(1)
                hecha -> inicio.plusDays(((i + 1) * semanas * 7L) / (total + 1))
                else -> HOY.plusDays(5L + i)
            }
            StudentTask(
                id = "$MARCA$subjectId-t$i",
                title = if (esLaVencida) m.vencida!! else "Entrega ${i + 1}",
                description = "",
                subjectId = subjectId,
                type = if (esLaVencida) TaskType.PROJECT else TaskType.WORKSHOP,
                dueDateMillis = vence.atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                difficulty = TaskDifficulty.MEDIUM,
                estimatedMinutes = 60,
                completed = hecha,
                createdAt = 0,
                updatedAt = 0,
                completedAt = if (hecha) milis(vence) else null
            )
        }
    }

    private fun milis(fecha: LocalDate): Long =
        fecha.atTime(LocalTime.of(10, 0)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun r1(v: Double): Double = Math.round(v * 10.0) / 10.0

    /** `hashN` del artifact. */
    private fun hashN(s: String): Int {
        var h = 7L
        for (c in s) h = (h * 31 + c.code) % 100003
        return h.toInt()
    }

    /** `azar` del artifact: FNV-1a para la semilla y xorshift de 32 bits, como `Math.imul`. */
    private class Azar(texto: String) {
        private var h: Int

        init {
            var x = 2166136261L.toInt()
            for (c in texto) {
                x = x xor c.code
                x *= 16777619
            }
            h = x
        }

        fun siguiente(): Double {
            h = h xor (h shl 13)
            h = h xor (h ushr 17)
            h = h xor (h shl 5)
            return ((h.toLong() and 0xFFFFFFFFL) % 100000) / 100000.0
        }
    }
}
