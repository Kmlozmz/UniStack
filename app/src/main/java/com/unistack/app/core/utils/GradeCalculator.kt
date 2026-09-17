package com.unistack.app.core.utils

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_user.domain.GradingCut
import kotlin.math.round

data class CutGradeCalculation(
    val average: Double?,
    val evaluatedFraction: Double,
    val weightedPoints: Double,
    val usesOfficialResult: Boolean,
    val unknownWeightCount: Int,
    val recordedGradeCount: Int,
    /**
     * Suma real de los pesos, sin recortar a 1.0. Solo difiere de [evaluatedFraction]
     * cuando el corte está sobreasignado: sus pesos suman más del 100 %.
     */
    val allocatedFraction: Double = evaluatedFraction
) {
    val isComplete: Boolean
        get() = evaluatedFraction >= 0.9999

    /**
     * Lo que este corte aporta a la nota final, ya escalado a la fracción que cubre.
     *
     * Con datos sanos es exactamente [weightedPoints]. Difiere solo cuando el corte está
     * sobreasignado, y ahí está el motivo de que exista: [average] ya se calcula dividiendo
     * por la suma real de pesos, pero [weightedPoints] no, así que sumarlo tal cual a nivel
     * de materia devolvía el número inflado que el corte ya había corregido. Tres actividades
     * del 50% con 5.0 daban 5.0 en la tarjeta del corte y 7.5 en la de la materia.
     */
    val normalizedPoints: Double
        get() = if (allocatedFraction > 1.0) weightedPoints / allocatedFraction else weightedPoints
}

/**
 * Cómo va la meta de la materia, mirando dónde se puede acabar y no solo dónde se está.
 *
 * Sale de comparar la meta con el suelo y el techo —lo que se saca haciendo 0 en lo que falta
 * y lo que se saca haciéndolo perfecto—, así que son estados comprobables y no una suposición
 * sobre el futuro.
 */
enum class TargetOutlook {
    /** Nada evaluado todavía: no hay nada que decir sin inventarlo. */
    NO_DATA,

    /** El suelo ya llega a la meta: no se puede perder ni sacando 0 en todo lo que falta. */
    SECURED,

    /** El promedio va por encima de la meta, pero todavía se puede caer. */
    ON_TRACK,

    /** Se llega, pero hay que sacar más de lo que se lleva. */
    AT_RISK,

    /** Ni con el máximo en todo lo que falta se alcanza la meta. */
    UNREACHABLE
}

/**
 * Cuánto de grave es ir por debajo de la meta, mirando la **nota de aprobación** y no solo la meta.
 *
 * [TargetOutlook] compara siempre contra la meta, que casi nadie pone en el aprobado raspado:
 * con la meta en 4.0 y el aprobado en 3.0, un 3.9 salía como «en riesgo» igual que un 1.2. Las
 * dos cosas no se parecen —una es ir algo corto, la otra es perder la materia— y decirlas con
 * la misma palabra y el mismo rojo hace que el aviso deje de significar nada.
 */
enum class GradeAlertLevel {
    /** Va bien: por encima de la meta, o todavía sin datos. */
    NONE,

    /** Por debajo de la meta, pero aprobando y con el aprobado a salvo. Es un aviso, no una alarma. */
    BEHIND,

    /**
     * El aprobado está en juego de verdad: o ya no se alcanza, o se va por debajo de él y
     * queda menos de medio semestre para arreglarlo.
     */
    CRITICAL
}

data class SubjectGradeCalculation(
    /** Promedio de lo evaluado hasta ahora. No es una previsión del final. */
    val currentAverage: Double?,
    /** Puntos ya asegurados sobre la nota final. Es el suelo. */
    val confirmedWeightedPoints: Double,
    val evaluatedSemesterFraction: Double,
    val remainingSemesterFraction: Double,
    val neededForTarget: Double?,
    val unknownWeightCount: Int,
    val hasIncompleteData: Boolean,
    val targetIsReachable: Boolean?,
    /** Nota final si se saca 0 en todo lo que falta. Null si no hay nada evaluado. */
    val guaranteedMinimum: Double?,
    /** Nota final si se saca el máximo en todo lo que falta. Null si no hay nada evaluado. */
    val bestPossible: Double?,
    /** No queda nada por evaluar: el suelo y el techo son la misma nota, la definitiva. */
    val isFinished: Boolean,
    val outlook: TargetOutlook,
    /**
     * Qué tan grave es el estado, midiendo contra el aprobado.
     *
     * Sale de [TargetOutlook] y de la nota de aprobación: es lo que separa «voy corto para la
     * meta» de «voy a perder la materia», que es lo que decide el color y la palabra en pantalla.
     */
    val alertLevel: GradeAlertLevel = GradeAlertLevel.NONE
)

object GradeCalculator {
    fun calculateCurrentAverage(grades: List<GradeItem>): Double? {
        return calculateCut(grades).average
    }

    fun calculateCutAverage(grades: List<GradeItem>): Double? = calculateCut(grades).average

    fun calculateCut(grades: List<GradeItem>): CutGradeCalculation {
        val officialResult = grades
            .filter { it.source == GradeSource.PERIOD_FINAL }
            .maxByOrNull { it.recordedAt }
        if (officialResult != null) {
            return CutGradeCalculation(
                average = roundToOneDecimal(officialResult.value),
                evaluatedFraction = 1.0,
                weightedPoints = officialResult.value,
                usesOfficialResult = true,
                unknownWeightCount = grades.count {
                    it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.UNKNOWN
                },
                recordedGradeCount = grades.size
            )
        }

        val activities = grades.filter { it.source == GradeSource.ACTIVITY }
        val knownActivities = activities.filter {
            it.weightStatus == GradeWeightStatus.KNOWN && it.percentage > 0.0
        }
        // Se divide por lo que realmente se sumó, no por la fracción recortada. Si los
        // pesos pasan del 100% —dato que puede llegar de un respaldo restaurado o de un
        // esquema de cortes editado después— recortar solo el denominador inflaba el
        // resultado: tres actividades del 50% con 5.0 daban 7.5 en una escala de 0 a 5.
        // Dividiendo por la suma real sigue siendo una media ponderada de verdad, así que
        // nunca puede superar la nota más alta que haya entre los datos.
        val allocatedFraction = knownActivities.sumOf { it.percentage }
        val evaluatedFraction = allocatedFraction.coerceIn(0.0, 1.0)
        val weightedPoints = knownActivities.sumOf { it.value * it.percentage }
        val average = if (allocatedFraction <= 0.0) {
            null
        } else {
            roundToOneDecimal(weightedPoints / allocatedFraction)
        }
        return CutGradeCalculation(
            average = average,
            evaluatedFraction = evaluatedFraction,
            weightedPoints = weightedPoints,
            usesOfficialResult = false,
            unknownWeightCount = activities.count { it.weightStatus == GradeWeightStatus.UNKNOWN },
            recordedGradeCount = activities.size,
            allocatedFraction = allocatedFraction
        )
    }

    fun calculateWeightedPointsByCuts(
        grades: List<GradeItem>,
        cuts: List<GradingCut>
    ): Double {
        return cuts.sumOf { cut ->
            calculateCut(grades.filter { it.cutId == cut.id }).normalizedPoints * cut.weight
        }
    }

    /**
     * Promedio de lo que ya está evaluado.
     *
     * Se llamaba «projected» y no proyecta nada: divide los puntos confirmados entre la
     * fracción evaluada, así que responde «cómo voy», no «cómo voy a acabar». El nombre
     * antiguo invitaba a colocar al lado una segunda proyección calculada de otra forma, que
     * es exactamente lo que había en el detalle de materia dando un número distinto.
     */
    fun calculateCurrentAverageByCuts(
        grades: List<GradeItem>,
        cuts: List<GradingCut>
    ): Double? {
        val evaluatedWeight = evaluatedSemesterFraction(grades, cuts)
        if (evaluatedWeight <= 0.0) return null
        return roundToOneDecimal(calculateWeightedPointsByCuts(grades, cuts) / evaluatedWeight)
    }

    fun calculateEvaluatedSemesterPercentage(
        grades: List<GradeItem>,
        cuts: List<GradingCut>
    ): Double {
        return roundToOneDecimal(evaluatedSemesterFraction(grades, cuts) * 100.0)
    }

    fun calculateSubject(
        grades: List<GradeItem>,
        cuts: List<GradingCut>,
        targetAverage: Double,
        maxGrade: Double,
        /**
         * La nota con la que se aprueba. Sin ella no se puede distinguir ir corto de perder la
         * materia, así que se cae a la meta y el aviso queda como estaba: todo lo que no llega
         * a la meta es grave.
         */
        passingGrade: Double? = null
    ): SubjectGradeCalculation {
        val evaluated = evaluatedSemesterFraction(grades, cuts)
        val remaining = (1.0 - evaluated).coerceAtLeast(0.0)
        val weightedPoints = calculateWeightedPointsByCuts(grades, cuts)
        val current = if (evaluated > 0.0) roundToOneDecimal(weightedPoints / evaluated) else null
        val needed = calculateNeededGrade(weightedPoints, remaining, targetAverage, maxGrade)
        val unknownWeights = grades.count {
            it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.UNKNOWN
        }
        // El suelo y el techo: qué nota final queda si en lo que falta se saca 0, y qué queda
        // si se saca el máximo. Son los dos extremos reales, sin suponer nada sobre cómo irá
        // lo que todavía no se ha hecho.
        val floor = if (evaluated > 0.0) roundToOneDecimal(weightedPoints) else null
        val ceiling = if (evaluated > 0.0) roundToOneDecimal(weightedPoints + remaining * maxGrade) else null
        val outlook = when {
            floor == null || ceiling == null -> TargetOutlook.NO_DATA
            floor >= targetAverage - 0.0001 -> TargetOutlook.SECURED
            ceiling < targetAverage - 0.0001 -> TargetOutlook.UNREACHABLE
            current != null && current >= targetAverage - 0.0001 -> TargetOutlook.ON_TRACK
            else -> TargetOutlook.AT_RISK
        }
        /*
         * El aviso se gradúa aquí, con el aprobado delante y el calendario detrás.
         *
         * Rojo cuando el aprobado está en juego y ámbar cuando lo único que falta es la meta.
         * Es la diferencia entre «apura» y «esto se pierde», y es la que faltaba en la lista
         * de materias.
         *
         * **Ir por debajo del aprobado no basta por sí solo.** La primera nota de un semestre
         * pesa poco y baja el promedio entero: un 2.0 con el 20 % evaluado deja el techo en
         * 4.4 —la materia no está ni cerca de perderse— y aun así el promedio de hoy está
         * bajo el aprobado. Pintar eso de rojo es la misma sobrealarma que este nivel vino a
         * quitar, corrida al principio del semestre.
         *
         * Así que el rojo por promedio pide además que quede **menos de medio semestre**: con
         * más por delante hay sitio de sobra para darle la vuelta y el aviso se queda en
         * ámbar. Lo que no espera a nadie es el techo: si el aprobado ya no se alcanza, es
         * rojo el primer día igual que el último.
         */
        val passing = passingGrade ?: targetAverage
        val quedaPoco = remaining < 0.5 - 0.0001
        val alertLevel = when {
            outlook == TargetOutlook.NO_DATA ||
                outlook == TargetOutlook.SECURED ||
                outlook == TargetOutlook.ON_TRACK -> GradeAlertLevel.NONE
            ceiling != null && ceiling < passing - 0.0001 -> GradeAlertLevel.CRITICAL
            current != null && current < passing - 0.0001 && quedaPoco -> GradeAlertLevel.CRITICAL
            else -> GradeAlertLevel.BEHIND
        }
        return SubjectGradeCalculation(
            currentAverage = current,
            confirmedWeightedPoints = weightedPoints,
            evaluatedSemesterFraction = evaluated,
            remainingSemesterFraction = remaining,
            neededForTarget = needed,
            unknownWeightCount = unknownWeights,
            hasIncompleteData = unknownWeights > 0 || remaining > 0.0001,
            targetIsReachable = needed?.let { it <= maxGrade },
            guaranteedMinimum = floor,
            bestPossible = ceiling,
            isFinished = remaining <= 0.0001,
            outlook = outlook,
            alertLevel = alertLevel
        )
    }

    fun calculateNeededGrade(
        currentWeightedPoints: Double,
        remainingPercentage: Double,
        targetAverage: Double,
        maxGrade: Double = 5.0
    ): Double? {
        if (remainingPercentage <= 0.0 || maxGrade <= 0.0) return null
        val needed = (targetAverage - currentWeightedPoints) / remainingPercentage
        if (needed.isNaN() || needed.isInfinite()) return null
        return roundToOneDecimal(needed)
    }

    private fun evaluatedSemesterFraction(
        grades: List<GradeItem>,
        cuts: List<GradingCut>
    ): Double {
        return cuts.sumOf { cut ->
            calculateCut(grades.filter { it.cutId == cut.id }).evaluatedFraction * cut.weight
        }.coerceIn(0.0, 1.0)
    }

    private fun roundToOneDecimal(value: Double): Double = round(value * 10.0) / 10.0
}
