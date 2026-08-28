package com.unistack.app.feature_terms.domain

import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_user.domain.GradingCutScheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Algo del periodo que se quedó a medias. */
sealed interface TermGap {
    /** De qué materia es, para agrupar y para nombrarla. */
    val subjectName: String

    /** Un corte que cerró sin una sola nota. Es el que más pesa: son puntos sin registrar. */
    data class MissingCut(
        override val subjectName: String,
        val subjectId: String,
        val cutId: String,
        val cutName: String,
        val weightPercent: Int
    ) : TermGap

    /** Clases que ya pasaron y nadie marcó. La asistencia del periodo queda incompleta. */
    data class UnmarkedClasses(
        override val subjectName: String,
        val subjectId: String,
        val count: Int
    ) : TermGap

    /** Una entrega vencida sin marcar como hecha. */
    data class OverdueTask(
        override val subjectName: String,
        val taskId: String,
        val title: String,
        val dueDate: LocalDate
    ) : TermGap
}

/** Una materia que acabó por debajo del aprobado, con las notas completas. */
data class FailedSubject(
    val subjectId: String,
    val name: String,
    val average: Double
)

/**
 * Lo que la app sabe del periodo justo antes de cerrarlo.
 *
 * No impide cerrar: cerrar es del usuario. Lo que impide es cerrar **a ciegas**, que es lo
 * único malo de una acción irreversible.
 */
data class TermCloseReport(
    val gaps: List<TermGap>,
    val subjectsWithEverything: Int,
    val subjectsTotal: Int,
    val average: Double?,
    val failed: List<FailedSubject>
) {
    /** Sin nada a medias, una confirmación basta: la fricción se gana, no se reparte. */
    val isClean: Boolean get() = gaps.isEmpty()
}

/**
 * La revisión del periodo, que corre siempre y no solo al cerrar.
 *
 * Llegar a diciembre para enterarte de que el Corte 1 lleva tres meses vacío no sirve de nada,
 * así que esto mismo se consulta durante el periodo con los cortes que ya cerraron. Al cerrar
 * el periodo han cerrado todos, y es el mismo cálculo con otro conjunto.
 *
 * Es pura a propósito: recibe listas y devuelve un informe. Ni base de datos, ni Compose, ni
 * reloj propio —el «hoy» se pasa— que es lo que la hace probable.
 */
object TermCloseCheck {
    /**
     * Los cortes que ya cerraron a fecha [date].
     *
     * El último no aparece nunca: acaba con el periodo, y mientras el periodo esté abierto no
     * ha cerrado. Sin fechas de corte esto devuelve vacío, y entonces la revisión solo puede
     * hablar de asistencias y entregas hasta que se cierre el periodo entero.
     */
    fun closedCutsOn(scheme: GradingCutScheme, date: LocalDate): Set<String> {
        if (!scheme.hasDates) return emptySet()
        val dia = date.toEpochDay()
        return scheme.cuts
            .sortedBy { it.order }
            .dropLast(1)
            .filter { corte -> corte.endEpochDay?.let { it < dia } == true }
            .map { it.id }
            .toSet()
    }

    /** Todos, que es lo que vale al cerrar el periodo: ahí ya no queda ninguno por venir. */
    fun allCuts(scheme: GradingCutScheme): Set<String> = scheme.cuts.map { it.id }.toSet()

    fun build(
        subjects: List<Subject>,
        cutScheme: GradingCutScheme,
        closedCutIds: Set<String>,
        unmarkedClasses: Map<String, Int>,
        tasks: List<StudentTask>,
        passingGrade: Double,
        today: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): TermCloseReport {
        val cortes = cutScheme.cuts.sortedBy { it.order }
        val huecos = mutableListOf<TermGap>()

        subjects.forEach { materia ->
            cortes.filter { it.id in closedCutIds }.forEach { corte ->
                /*
                 * «No la tengo» no es lo mismo que «me falta».
                 *
                 * Para eso existe `unknownCutIds`: el usuario ya dijo que ese corte no va a
                 * tener nota nunca —llegó tarde a la materia, o el profesor no la dio— y
                 * sacarlo aquí sería pedirle otra vez algo que ya contestó.
                 */
                if (corte.id in materia.unknownCutIds) return@forEach
                if (materia.grades.none { it.cutId == corte.id }) {
                    huecos += TermGap.MissingCut(
                        subjectName = materia.name,
                        subjectId = materia.id,
                        cutId = corte.id,
                        cutName = corte.name,
                        weightPercent = Math.round(corte.weight * 100.0).toInt()
                    )
                }
            }
        }

        subjects.forEach { materia ->
            val sinMarcar = unmarkedClasses[materia.id] ?: 0
            if (sinMarcar > 0) {
                huecos += TermGap.UnmarkedClasses(materia.name, materia.id, sinMarcar)
            }
        }

        /*
         * Vencida es «de antes de hoy», como en Tareas.
         *
         * Una que vence hoy sale ahí bajo «Hoy» y no bajo «Vencidas», y todavía queda el día
         * para hacerla. Si aquí contara como vencida, la misma entrega tendría dos estados a
         * la vez según la pantalla desde la que se mire.
         */
        tasks.asSequence()
            .filter { !it.completed }
            .map { it to Instant.ofEpochMilli(it.dueDateMillis).atZone(zone).toLocalDate() }
            .filter { (_, vence) -> vence.isBefore(today) }
            .forEach { (tarea, vence) ->
                huecos += TermGap.OverdueTask(
                    subjectName = subjects.firstOrNull { it.id == tarea.subjectId }?.name.orEmpty(),
                    taskId = tarea.id,
                    title = tarea.title,
                    dueDate = vence
                )
            }

        val promedios = subjects.associate { materia ->
            materia.id to GradeCalculator.calculateCurrentAverageByCuts(materia.grades, cortes)
        }
        val evaluadas = promedios.values.filterNotNull()

        /*
         * Perdida solo si las notas están completas.
         *
         * Con un corte sin registrar el promedio está calculado sobre una parte, así que un
         * 2,4 puede ser un 4,0 en cuanto llegue lo que falta. Etiquetar «repitiendo» ahí sería
         * adivinar, y esa etiqueta se arrastra al periodo siguiente.
         */
        val incompletas = huecos.filterIsInstance<TermGap.MissingCut>().map { it.subjectId }.toSet()
        val perdidas = subjects.mapNotNull { materia ->
            if (materia.id in incompletas) return@mapNotNull null
            val promedio = promedios[materia.id] ?: return@mapNotNull null
            if (promedio >= passingGrade) return@mapNotNull null
            FailedSubject(materia.id, materia.name, promedio)
        }

        return TermCloseReport(
            // Primero lo que cuesta puntos, después la asistencia, y al final las entregas.
            gaps = huecos.sortedBy { hueco ->
                when (hueco) {
                    is TermGap.MissingCut -> 0
                    is TermGap.UnmarkedClasses -> 1
                    is TermGap.OverdueTask -> 2
                }
            },
            subjectsWithEverything = subjects.count { it.id !in incompletas },
            subjectsTotal = subjects.size,
            average = evaluadas.takeIf { it.isNotEmpty() }?.let { lista ->
                Math.round(lista.average() * 10.0) / 10.0
            },
            failed = perdidas
        )
    }
}

/** Cómo se dice un hueco en una línea, para la lista de la comprobación. */
fun TermGap.title(): String = when (this) {
    is TermGap.MissingCut -> "$subjectName — falta el $cutName"
    is TermGap.UnmarkedClasses ->
        "$subjectName — $count ${if (count == 1) "clase" else "clases"} sin marcar"
    is TermGap.OverdueTask -> "$title — sin entregar"
}

fun TermGap.detail(): String = when (this) {
    is TermGap.MissingCut -> "$weightPercent% de la nota sin registrar"
    is TermGap.UnmarkedClasses -> "La asistencia queda incompleta"
    is TermGap.OverdueTask -> "Venció el ${dueDate.dayOfMonth} de ${mesLargo(dueDate)}"
}

/** Qué palabra usar para el conjunto, que cambia con el número. */
fun List<TermGap>.summaryLine(): String = when (size) {
    0 -> "No falta nada por terminar"
    1 -> "Hay 1 cosa sin terminar"
    else -> "Hay $size cosas sin terminar"
}

private val MesesLargos = listOf(
    "enero", "febrero", "marzo", "abril", "mayo", "junio",
    "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
)

private fun mesLargo(date: LocalDate): String = MesesLargos[date.monthValue - 1]
