package com.unistack.app.feature_terms.domain

import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_tasks.domain.StudentTask
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Lo que está a medias en el periodo que se va a cerrar.
 *
 * Son las tres cosas que el cierre deja arreglar sin salir: una nota de corte que falta, clases
 * sin marcar y una tarea vencida. Ninguna impide cerrar; se enseñan para que no se pierdan.
 */
sealed interface PendienteDeCierre {
    val clave: String

    data class Nota(val materia: MateriaDelPeriodo, val corte: CorteDeMateria) : PendienteDeCierre {
        override val clave: String get() = "nota:${materia.id}:${corte.cut.id}"
    }

    data class Clases(val materia: MateriaDelPeriodo, val clases: List<ClaseDelPeriodo>) : PendienteDeCierre {
        override val clave: String get() = "clases:${materia.id}"
    }

    data class Tarea(val materia: MateriaDelPeriodo?, val tarea: StudentTask, val vence: LocalDate) : PendienteDeCierre {
        override val clave: String get() = "tarea:${tarea.id}"
    }
}

object RevisionDeCierre {

    /**
     * Materia por materia, en el orden de la lista: sus notas, sus clases y sus entregas.
     *
     * Es el orden de la simulación aprobada, y lo que se revisa de una materia queda junto.
     * Un corte que el usuario dio por desconocido no se reclama: ya dijo que esa nota no la
     * tiene. Las tareas que cuentan son las de sus materias que vencieron dentro del periodo;
     * una de hace dos semestres no tiene nada que ver con este cierre.
     */
    fun pendientes(
        periodo: PeriodoDelHistorico,
        tasks: List<StudentTask>,
        hoy: LocalDate,
        ignoradas: Set<String> = emptySet(),
        zona: ZoneId = ZoneId.systemDefault()
    ): List<PendienteDeCierre> = periodo.materias.flatMap { materia ->
        val notas = materia.cortes
            .filter { !it.completo && it.cut.id !in materia.subject.unknownCutIds }
            .map { PendienteDeCierre.Nota(materia, it) }
        val clases = materia.asistencia.clases
            .filter { it.status == ClassAttendanceStatus.PENDING }
            .takeIf { it.isNotEmpty() }
            ?.let { listOf(PendienteDeCierre.Clases(materia, it)) }
            .orEmpty()
        val tareas = tasks.asSequence()
            .filter { !it.completed && it.subjectId == materia.id }
            .map { it to Instant.ofEpochMilli(it.dueDateMillis).atZone(zona).toLocalDate() }
            .filter { (_, vence) -> vence.isBefore(hoy) && !vence.isBefore(periodo.inicio) }
            .sortedBy { (_, vence) -> vence }
            .map { (tarea, vence) -> PendienteDeCierre.Tarea(materia, tarea, vence) }
            .toList()
        notas + clases + tareas
    }.filter { it.clave !in ignoradas }
}
