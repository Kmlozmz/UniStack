package com.unistack.app.feature_terms.domain

import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingCutScheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Una clase que tocaba dentro del periodo, marcada o no.
 *
 * Solo existen las que ya ocurrieron: el historial de un periodo cuenta lo que pasó, y una clase
 * de la semana que viene no es una falta ni una asistencia todavía.
 */
data class ClaseDelPeriodo(
    val date: LocalDate,
    val sessionId: String,
    val startMinute: Int,
    val endMinute: Int,
    val status: ClassAttendanceStatus
)

/**
 * La asistencia de una materia en su periodo entero.
 *
 * No sale de `SubjectAttendanceHistory.build`, que corta en las últimas veintidós clases porque
 * es lo que cabe en su pantalla: el mapa por semanas y el porcentaje de un semestre cerrado
 * necesitan todas, desde el primer día.
 */
data class AsistenciaDelPeriodo(
    val clases: List<ClaseDelPeriodo>,
    val asistidas: Int,
    val faltas: Int,
    val noHubo: Int,
    val sinMarcar: Int,
    /** Nulo sin ninguna clase decidida: «—», no un cero. */
    val porcentaje: Double?,
    /** La tira más larga de clases seguidas a las que fuiste; una falta la corta. */
    val racha: Int
) {
    /** Las que contaban: todas menos las que no hubo. */
    val programadas: Int get() = asistidas + faltas + sinMarcar
}

/** Un corte de una materia, con sus notas y lo que vale. */
data class CorteDeMateria(
    val cut: GradingCut,
    /** Desde 1, que es como se nombra en pantalla: «C1». */
    val numero: Int,
    val pesoPorcentaje: Int,
    /** Lo registrado en el corte, de lo más antiguo a lo más nuevo. */
    val notas: List<GradeItem>,
    /** La nota del corte, **solo si está completo**. A medias no hay nota que afirmar. */
    val nota: Double?,
    /** Qué parte del corte está evaluada, de 0 a 1. */
    val evaluado: Double,
    val desde: LocalDate?,
    val hasta: LocalDate?
) {
    val completo: Boolean get() = nota != null
}

/** El intento anterior de una materia repetida. */
data class IntentoAnterior(
    val subjectId: String,
    val termId: String,
    val termName: String,
    val final: Double?
)

data class MateriaDelPeriodo(
    val subject: Subject,
    val cortes: List<CorteDeMateria>,
    /** La definitiva: existe cuando no queda nada por evaluar. */
    val final: Double?,
    /** Lo que queda sacando cero en lo que falta. Nulo si no hay nada evaluado. */
    val piso: Double?,
    /** Lo que queda sacando el máximo en lo que falta. Nulo si no hay nada evaluado. */
    val techo: Double?,
    val aprobada: Boolean?,
    val asistencia: AsistenciaDelPeriodo,
    val tareasHechas: Int,
    val tareasTotal: Int,
    val intentoAnterior: IntentoAnterior? = null
) {
    val id: String get() = subject.id
    val nombre: String get() = subject.name
    val esRepetida: Boolean get() = subject.repeatedFromSubjectId != null

    /** El primer corte sin nota, para decir «Falta C3» en vez de una cifra a medias. */
    val primerCorteQueFalta: Int?
        get() = cortes.indexOfFirst { !it.completo }.takeIf { it >= 0 }

    /** Notas de todos los cortes y ninguna clase sin marcar. */
    val completa: Boolean
        get() = cortes.all { it.completo } && asistencia.sinMarcar == 0
}

data class PeriodoDelHistorico(
    val term: AcademicTerm,
    val esquema: GradingCutScheme,
    /** Si las fechas de corte son de este periodo; las de otro no se enseñan. */
    val tieneFechasDeCorte: Boolean,
    val materias: List<MateriaDelPeriodo>,
    /** La media de las definitivas. Las que no tienen definitiva no cuentan. */
    val promedio: Double?,
    val asistencia: Double?,
    val perdidas: List<MateriaDelPeriodo>,
    val completas: Int,
    val fin: LocalDate,
    val semanas: Int,
    /** En curso, con su fin previsto ya pasado. */
    val terminado: Boolean,
    /** Creado, pero todavía no ha empezado. */
    val porEmpezar: Boolean
) {
    val id: String get() = term.id
    val nombre: String get() = term.name
    val cerrado: Boolean get() = !term.isActive
    val inicio: LocalDate get() = term.start
}

/** Un punto de la gráfica del histórico: el periodo y cómo iba el acumulado al cerrarlo. */
data class PuntoDelHistorico(
    val termId: String,
    val nombre: String,
    val promedio: Double?,
    val acumulado: Double?
)

/**
 * Todo lo que se puede decir de los periodos, contado en un solo sitio.
 *
 * Las pantallas del histórico, Inicio sin periodo, el cierre y el periodo nuevo leen de aquí.
 * Antes cada una sacaba sus cifras por su lado, y un acumulado calculado dos veces acaba
 * diciendo dos cosas.
 */
data class HistorialAcademico(
    val hoy: LocalDate,
    /** Del más reciente al más antiguo. */
    val periodos: List<PeriodoDelHistorico>,
    val activo: PeriodoDelHistorico?,
    /** Del más reciente al más antiguo. */
    val cerrados: List<PeriodoDelHistorico>,
    /**
     * La media de todas las definitivas de los periodos cerrados.
     *
     * Una materia repetida cuenta las dos veces: la perdida conserva su nota y la nueva empieza
     * de cero. No hay opción para cambiarlo; es lo que hace repetir.
     */
    val acumulado: Double?,
    val notasFinales: Int,
    /** Los cerrados, del más antiguo al más reciente. */
    val serie: List<PuntoDelHistorico>,
    val notaMaxima: Double,
    val aprobado: Double
) {
    fun periodo(id: String): PeriodoDelHistorico? = periodos.firstOrNull { it.id == id }

    fun materia(subjectId: String): Pair<PeriodoDelHistorico, MateriaDelPeriodo>? {
        periodos.forEach { periodo ->
            periodo.materias.firstOrNull { it.id == subjectId }?.let { return periodo to it }
        }
        return null
    }

    fun anteriorA(periodo: PeriodoDelHistorico): PeriodoDelHistorico? =
        cerrados.filter { it.term.startEpochDay < periodo.term.startEpochDay }
            .maxByOrNull { it.term.startEpochDay }

    /** Cómo quedaría el acumulado si [periodo] se cerrara ahora, con lo que tiene. */
    fun acumuladoAlCerrar(periodo: PeriodoDelHistorico): Double? {
        val notas = cerrados.filter { it.id != periodo.id }.flatMap { p -> p.materias.mapNotNull { it.final } } +
            periodo.materias.mapNotNull { it.final }
        return notas.media()
    }

    /** Entre qué valores puede acabar el promedio del periodo. Sin proyecciones: piso y techo. */
    fun rangoDelPeriodo(periodo: PeriodoDelHistorico): Pair<Double, Double>? {
        if (periodo.materias.isEmpty()) return null
        val piso = periodo.materias.map { it.final ?: it.piso ?: 0.0 }.media() ?: return null
        val techo = periodo.materias.map { it.final ?: it.techo ?: notaMaxima }.media() ?: return null
        return piso to techo
    }

    /** Entre qué valores puede quedar el acumulado al cerrar [periodo]. */
    fun rangoDelAcumulado(periodo: PeriodoDelHistorico): Pair<Double, Double>? {
        val base = cerrados.filter { it.id != periodo.id }.flatMap { p -> p.materias.mapNotNull { it.final } }
        val n = base.size + periodo.materias.size
        if (n == 0) return null
        val suma = base.sum()
        val piso = periodo.materias.sumOf { it.final ?: it.piso ?: 0.0 }
        val techo = periodo.materias.sumOf { it.final ?: it.techo ?: notaMaxima }
        return (suma + piso) / n to (suma + techo) / n
    }

    /** El acumulado tras cada cierre, para la línea pequeña de Inicio. */
    val evolucion: List<Double>
        get() = serie.mapNotNull { it.acumulado }
}

object CalculoDelHistorico {

    fun build(
        terms: List<AcademicTerm>,
        subjects: List<Subject>,
        sessions: List<ClassSession>,
        occurrences: List<ClassOccurrence>,
        breaks: List<ClosedRange<LocalDate>>,
        tasks: List<StudentTask>,
        esquemaActual: GradingCutScheme,
        notaMaxima: Double,
        aprobado: Double,
        hoy: LocalDate,
        ahora: LocalDateTime,
        zona: ZoneId = ZoneId.systemDefault()
    ): HistorialAcademico {
        val porClave = occurrences.associateBy { it.sessionId to it.dateEpochDay }
        val ordenados = terms.sortedByDescending { it.startEpochDay }

        val sinIntentos = ordenados.map { term ->
            periodo(
                term = term,
                suyas = subjects.filter { it.termId == term.id || (it.termId == null && term.isActive) },
                sessions = sessions,
                porClave = porClave,
                breaks = breaks,
                tasks = tasks,
                esquemaActual = esquemaActual,
                notaMaxima = notaMaxima,
                aprobado = aprobado,
                hoy = hoy,
                ahora = ahora,
                zona = zona
            )
        }

        // Segunda pasada: una repetida enlaza con su intento anterior, que es de otro periodo.
        val todas = sinIntentos.flatMap { p -> p.materias.map { it to p } }
        val periodos = sinIntentos.map { p ->
            p.copy(
                materias = p.materias.map { materia ->
                    val origen = materia.subject.repeatedFromSubjectId ?: return@map materia
                    val (anterior, suPeriodo) = todas.firstOrNull { it.first.id == origen } ?: return@map materia
                    materia.copy(
                        intentoAnterior = IntentoAnterior(
                            subjectId = anterior.id,
                            termId = suPeriodo.id,
                            termName = suPeriodo.nombre,
                            final = anterior.final
                        )
                    )
                }
            ).let { actualizado ->
                actualizado.copy(perdidas = actualizado.materias.filter { m -> m.final != null && m.final < aprobado - 0.0001 })
            }
        }

        val cerrados = periodos.filter { it.cerrado }
        val cronologicos = cerrados.sortedBy { it.term.startEpochDay }
        val acumuladas = mutableListOf<Double>()
        val serie = cronologicos.map { p ->
            acumuladas += p.materias.mapNotNull { it.final }
            PuntoDelHistorico(p.id, p.nombre, p.promedio, acumuladas.media())
        }
        val finales = cerrados.flatMap { p -> p.materias.mapNotNull { it.final } }

        return HistorialAcademico(
            hoy = hoy,
            periodos = periodos,
            activo = periodos.firstOrNull { !it.cerrado },
            cerrados = cerrados,
            acumulado = finales.media(),
            notasFinales = finales.size,
            serie = serie,
            notaMaxima = notaMaxima,
            aprobado = aprobado
        )
    }

    private fun periodo(
        term: AcademicTerm,
        suyas: List<Subject>,
        sessions: List<ClassSession>,
        porClave: Map<Pair<String, Long>, ClassOccurrence>,
        breaks: List<ClosedRange<LocalDate>>,
        tasks: List<StudentTask>,
        esquemaActual: GradingCutScheme,
        notaMaxima: Double,
        aprobado: Double,
        hoy: LocalDate,
        ahora: LocalDateTime,
        zona: ZoneId
    ): PeriodoDelHistorico {
        /*
         * El esquema con el que se cerró, si se guardó al cerrar.
         *
         * Las fechas de corte se reescriben con cada periodo nuevo, así que las de hoy no son
         * las de un semestre de hace un año: sin la copia se enseña el reparto, pero no las
         * fechas.
         */
        val esquema = term.cutScheme ?: esquemaActual
        val tieneFechas = (term.isActive || term.cutScheme != null) && esquema.hasDates && esquema.cuts.size > 1
        val fin = term.plannedEnd ?: term.closedEpochDay?.let(LocalDate::ofEpochDay) ?: hoy
        val semanas = ((ChronoUnit.DAYS.between(term.start, fin) + 1) / 7.0).roundToInt().coerceAtLeast(1)

        // Las clases cuentan hasta hoy y nunca más allá del fin previsto ni del cierre.
        val hasta = listOfNotNull(hoy, term.plannedEnd, term.closedEpochDay?.let(LocalDate::ofEpochDay)).min()
        val cortes = esquema.cuts.sortedBy { it.order }

        val materias = suyas.map { subject ->
            materia(
                subject = subject,
                cortes = cortes,
                esquema = esquema,
                tieneFechas = tieneFechas,
                term = term,
                fin = fin,
                clases = clasesDe(subject, sessions, porClave, breaks, term.start, hasta, ahora),
                tasks = tasks,
                notaMaxima = notaMaxima,
                aprobado = aprobado,
                zona = zona
            )
        }
        val finales = materias.mapNotNull { it.final }

        return PeriodoDelHistorico(
            term = term,
            esquema = esquema,
            tieneFechasDeCorte = tieneFechas,
            materias = materias,
            promedio = finales.media(),
            asistencia = materias.mapNotNull { it.asistencia.porcentaje }.media(),
            perdidas = materias.filter { it.final != null && it.final < aprobado - 0.0001 },
            completas = materias.count { it.completa },
            fin = fin,
            semanas = semanas,
            terminado = term.isPastPlannedEnd(hoy),
            porEmpezar = term.isActive && term.start.isAfter(hoy)
        )
    }

    private fun materia(
        subject: Subject,
        cortes: List<GradingCut>,
        esquema: GradingCutScheme,
        tieneFechas: Boolean,
        term: AcademicTerm,
        fin: LocalDate,
        clases: List<ClaseDelPeriodo>,
        tasks: List<StudentTask>,
        notaMaxima: Double,
        aprobado: Double,
        zona: ZoneId
    ): MateriaDelPeriodo {
        val calculo = GradeCalculator.calculateSubject(
            grades = subject.grades,
            cuts = cortes,
            targetAverage = subject.targetAverage,
            maxGrade = notaMaxima,
            passingGrade = aprobado
        )
        val final = if (calculo.isFinished) calculo.guaranteedMinimum else null
        val suyasTareas = tasks.filter { it.subjectId == subject.id }

        return MateriaDelPeriodo(
            subject = subject,
            cortes = cortes.mapIndexed { indice, corte ->
                val delCorte = subject.grades.filter { it.cutId == corte.id }
                val calculoCorte = GradeCalculator.calculateCut(delCorte)
                val (desde, hasta) = if (tieneFechas) esquema.rangeFor(corte.id, term.start, fin) else null to null
                CorteDeMateria(
                    cut = corte,
                    numero = indice + 1,
                    pesoPorcentaje = (corte.weight * 100.0).roundToInt(),
                    notas = delCorte.sortedWith(compareBy({ it.source == GradeSource.PERIOD_FINAL }, { it.recordedAt })),
                    nota = if (calculoCorte.isComplete) calculoCorte.average else null,
                    evaluado = calculoCorte.evaluatedFraction,
                    desde = desde,
                    hasta = hasta
                )
            },
            final = final,
            piso = calculo.guaranteedMinimum,
            techo = calculo.bestPossible,
            aprobada = final?.let { it >= aprobado - 0.0001 },
            asistencia = asistencia(clases),
            tareasHechas = suyasTareas.count { it.completed },
            tareasTotal = suyasTareas.size
        )
    }

    /**
     * Todas las clases de la materia desde que empezó el periodo hasta [hasta].
     *
     * Con los días sin clase fuera, igual que el historial: un festivo no es una clase que
     * alguien olvidó marcar. Las de hoy entran cuando ya acabaron, o si ya se marcaron.
     */
    fun clasesDe(
        subject: Subject,
        sessions: List<ClassSession>,
        porClave: Map<Pair<String, Long>, ClassOccurrence>,
        breaks: List<ClosedRange<LocalDate>>,
        desde: LocalDate,
        hasta: LocalDate,
        ahora: LocalDateTime
    ): List<ClaseDelPeriodo> {
        val suyas = sessions.filter { it.subjectId == subject.id }
        if (suyas.isEmpty() || desde.isAfter(hasta)) return emptyList()
        val clases = mutableListOf<ClaseDelPeriodo>()
        var dia = desde
        while (!dia.isAfter(hasta)) {
            val fecha = dia
            if (breaks.none { fecha in it }) {
                suyas.filter { it.occursOn(fecha.toEpochDay(), fecha.dayOfWeek.value) }
                    .sortedBy { it.startMinute }
                    .forEach { sesion ->
                        val estado = porClave[sesion.id to fecha.toEpochDay()]?.status ?: ClassAttendanceStatus.PENDING
                        val acabo = !fecha.atStartOfDay().plusMinutes(sesion.endMinute.toLong()).isAfter(ahora)
                        if (acabo || estado != ClassAttendanceStatus.PENDING) {
                            clases += ClaseDelPeriodo(fecha, sesion.id, sesion.startMinute, sesion.endMinute, estado)
                        }
                    }
            }
            dia = dia.plusDays(1)
        }
        return clases
    }

    fun asistencia(clases: List<ClaseDelPeriodo>): AsistenciaDelPeriodo {
        val asistidas = clases.count { it.status == ClassAttendanceStatus.ATTENDED }
        val faltas = clases.count { it.status == ClassAttendanceStatus.ABSENT }
        var racha = 0
        var actual = 0
        clases.forEach { clase ->
            when (clase.status) {
                ClassAttendanceStatus.ATTENDED -> {
                    actual++
                    racha = maxOf(racha, actual)
                }
                ClassAttendanceStatus.ABSENT -> actual = 0
                else -> Unit
            }
        }
        return AsistenciaDelPeriodo(
            clases = clases,
            asistidas = asistidas,
            faltas = faltas,
            noHubo = clases.count {
                it.status == ClassAttendanceStatus.CANCELLED || it.status == ClassAttendanceStatus.RESCHEDULED
            },
            sinMarcar = clases.count { it.status == ClassAttendanceStatus.PENDING },
            porcentaje = if (asistidas + faltas > 0) asistidas * 100.0 / (asistidas + faltas) else null,
            racha = racha
        )
    }

    /**
     * La asistencia acumulada al final de cada semana del periodo.
     *
     * Antes de la primera clase decidida la línea está arriba, en el cien: no se ha faltado a
     * nada todavía.
     */
    fun asistenciaPorSemana(materia: MateriaDelPeriodo, inicio: LocalDate, semanas: Int): List<Double> {
        var asistidas = 0
        var faltas = 0
        val clases = materia.asistencia.clases
        return (0 until semanas).map { semana ->
            clases.filter { ChronoUnit.DAYS.between(inicio, it.date) / 7 == semana.toLong() }.forEach {
                if (it.status == ClassAttendanceStatus.ATTENDED) asistidas++
                if (it.status == ClassAttendanceStatus.ABSENT) faltas++
            }
            if (asistidas + faltas > 0) asistidas * 100.0 / (asistidas + faltas) else 100.0
        }
    }

    /** Las notas con valor, para la gráfica y la tabla de una materia, por fecha. */
    fun evaluaciones(materia: MateriaDelPeriodo, zona: ZoneId = ZoneId.systemDefault()): List<EvaluacionDeMateria> =
        materia.cortes.flatMap { corte ->
            corte.notas.filter {
                it.source == GradeSource.PERIOD_FINAL ||
                    (it.weightStatus == GradeWeightStatus.KNOWN && it.percentage > 0.0)
            }.map { nota ->
                EvaluacionDeMateria(
                    nota = nota,
                    corte = corte,
                    fecha = nota.recordedAt.takeIf { it > 0L }?.let { Instant.ofEpochMilli(it).atZone(zona).toLocalDate() },
                    pesoEnLaFinal = if (nota.source == GradeSource.PERIOD_FINAL) {
                        corte.cut.weight * 100.0
                    } else {
                        nota.percentage * corte.cut.weight * 100.0
                    }
                )
            }
        }.sortedWith(compareBy<EvaluacionDeMateria, LocalDate?>(nullsLast()) { it.fecha })

    /** Cuánto se separan las notas de su media: cuanto más pequeño, más parejo. */
    fun constancia(valores: List<Double>): Double? {
        val media = valores.media() ?: return null
        return sqrt(valores.sumOf { (it - media) * (it - media) } / valores.size)
    }
}

data class EvaluacionDeMateria(
    val nota: GradeItem,
    val corte: CorteDeMateria,
    val fecha: LocalDate?,
    /** Qué parte de la nota final es, en porcentaje. */
    val pesoEnLaFinal: Double
) {
    /** Lo que aportó a la final, en puntos de la escala. */
    val aporte: Double get() = nota.value * pesoEnLaFinal / 100.0
}

internal fun List<Double>.media(): Double? = if (isEmpty()) null else sum() / size
