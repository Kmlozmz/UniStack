package com.unistack.app.feature_support.domain

/**
 * Las tres cuentas de la calculadora.
 *
 * Están aquí y no dentro de la pantalla porque son lo único de esa pantalla que se puede
 * comprobar sin dibujar nada, y porque son tres cuentas distintas que se confundían en una:
 * la calculadora vieja pedía créditos para sacar el promedio de **una** materia, que es una
 * pregunta donde los créditos no pintan nada.
 *
 * - [subjectAverage] — dentro de una materia, cada nota vale un porcentaje del curso.
 * - [semesterAverage] — entre materias, cada una pesa según sus créditos.
 * - [neededGrade] — la inversa: qué hace falta sacar en lo que queda para llegar a una meta.
 */
object CalculatorMath {

    /** Una nota con el porcentaje del curso que vale. */
    data class Evaluation(val grade: Double, val weightPercent: Double)

    /** Una materia con su nota final y sus créditos. */
    data class SemesterSubject(
        val name: String,
        val grade: Double,
        val credits: Double,
        /** Si vino de las materias registradas o la escribió quien calcula. */
        val fromApp: Boolean = false
    )

    /**
     * El promedio dentro de una materia, y cuánto del curso lleva evaluado.
     *
     * Se divide entre el peso puesto, no entre 100: con el 50 % evaluado, un 4,0 y un 3,0 dan
     * 3,5 —lo que llevas—, no 1,75 —lo que llevarías si el resto fuera cero—. Son dos números
     * distintos y el que se viene a ver aquí es el primero.
     */
    fun subjectAverage(evaluations: List<Evaluation>): Double? {
        val weight = evaluations.sumOf { it.weightPercent }
        if (weight <= 0.0) return null
        return evaluations.sumOf { it.grade * it.weightPercent } / weight
    }

    /** Cuánto del curso está repartido ya, en porcentaje. */
    fun usedWeight(evaluations: List<Evaluation>): Double =
        evaluations.sumOf { it.weightPercent }

    /**
     * Lo que queda por repartir. Nunca negativo: si se pasara de 100 no habría hueco, y el
     * hueco es lo que decide si se puede añadir otra nota.
     */
    fun freeWeight(evaluations: List<Evaluation>): Double =
        (100.0 - usedWeight(evaluations)).coerceAtLeast(0.0)

    /**
     * El promedio del semestre, pesando cada materia por sus créditos.
     *
     * Las materias sin créditos puestos **no cuentan**: con peso cero no aportan, y darles un
     * uno por defecto sería inventarse que todas valen lo mismo justo en la pantalla que
     * existe para que no valgan lo mismo.
     */
    fun semesterAverage(subjects: List<SemesterSubject>): Double? {
        val credits = subjects.sumOf { it.credits }
        if (credits <= 0.0) return null
        return subjects.sumOf { it.grade * it.credits } / credits
    }

    fun totalCredits(subjects: List<SemesterSubject>): Double =
        subjects.sumOf { it.credits }

    /** Cuántas materias quedan fuera del promedio por no tener créditos. */
    fun subjectsWithoutCredits(subjects: List<SemesterSubject>): Int =
        subjects.count { it.credits <= 0.0 }

    /**
     * Qué nota hace falta en lo que queda por evaluar para acabar con [target].
     *
     * `null` cuando ya no queda nada por evaluar: con el 100 % hecho no hay nota que sacar, y
     * devolver un número ahí sería responder a una pregunta que nadie hizo.
     *
     * Puede salir **por encima del máximo de la escala**, y esa es la respuesta más útil que
     * da esta cuenta: significa que la meta ya no se alcanza. Se devuelve tal cual en vez de
     * recortarla al máximo, porque un «5,0» recortado se lee como «justo lo justo» cuando lo
     * que pasa es que no da.
     */
    fun neededGrade(current: Double, evaluatedPercent: Double, target: Double): Double? {
        val done = evaluatedPercent.coerceIn(0.0, 100.0)
        val remaining = 100.0 - done
        if (remaining <= 0.0) return null
        return (target * 100.0 - current * done) / remaining
    }
}
