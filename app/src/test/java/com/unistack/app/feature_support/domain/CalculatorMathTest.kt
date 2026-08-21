package com.unistack.app.feature_support.domain

import com.unistack.app.feature_support.domain.CalculatorMath.Evaluation
import com.unistack.app.feature_support.domain.CalculatorMath.SemesterSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorMathTest {

    // ---------------------------------------------------------------- Materia

    @Test
    fun `el promedio de la materia se divide entre lo evaluado, no entre cien`() {
        // Un 4 que vale el 30 % y un 3 que vale el 20 % son 180 puntos sobre 50 de peso: 3,6
        // de lo que llevas. Dividir entre 100 daría 1,8, que es la nota final si el resto
        // fuera cero: otra pregunta.
        val media = CalculatorMath.subjectAverage(
            listOf(Evaluation(4.0, 30.0), Evaluation(3.0, 20.0))
        )
        assertEquals(3.6, media!!, 0.001)
    }

    @Test
    fun `sin notas puestas no hay promedio`() {
        assertNull(CalculatorMath.subjectAverage(emptyList()))
    }

    @Test
    fun `una nota que no vale nada no puede decidir el promedio`() {
        assertNull(CalculatorMath.subjectAverage(listOf(Evaluation(5.0, 0.0))))
    }

    @Test
    fun `el hueco libre nunca es negativo`() {
        val pasado = listOf(Evaluation(4.0, 70.0), Evaluation(4.0, 70.0))
        assertEquals(0.0, CalculatorMath.freeWeight(pasado), 0.001)
        assertEquals(140.0, CalculatorMath.usedWeight(pasado), 0.001)
    }

    @Test
    fun `con el cien por cien repartido no queda hueco`() {
        val completo = listOf(Evaluation(4.0, 60.0), Evaluation(3.0, 40.0))
        assertEquals(0.0, CalculatorMath.freeWeight(completo), 0.001)
        assertEquals(3.6, CalculatorMath.subjectAverage(completo)!!, 0.001)
    }

    // --------------------------------------------------------------- Semestre

    @Test
    fun `el semestre pesa cada materia por sus creditos`() {
        val media = CalculatorMath.semesterAverage(
            listOf(
                SemesterSubject("Cálculo", 5.0, 4.0),
                SemesterSubject("Álgebra", 2.0, 1.0)
            )
        )
        // (5*4 + 2*1) / 5 = 4,4. Sin créditos saldría 3,5.
        assertEquals(4.4, media!!, 0.001)
    }

    @Test
    fun `una materia sin creditos no entra en el promedio`() {
        val subjects = listOf(
            SemesterSubject("Con créditos", 5.0, 2.0),
            SemesterSubject("Sin créditos", 1.0, 0.0)
        )
        assertEquals(5.0, CalculatorMath.semesterAverage(subjects)!!, 0.001)
        assertEquals(1, CalculatorMath.subjectsWithoutCredits(subjects))
    }

    @Test
    fun `sin ningun credito puesto no hay promedio del semestre`() {
        assertNull(
            CalculatorMath.semesterAverage(listOf(SemesterSubject("Una", 4.0, 0.0)))
        )
    }

    // --------------------------------------------------------------- Me falta

    @Test
    fun `lo que falta sale de lo que llevas y de lo que queda`() {
        // 3,5 con el 60 % hecho, meta 4,0: (400 - 210) / 40 = 4,75.
        val falta = CalculatorMath.neededGrade(current = 3.5, evaluatedPercent = 60.0, target = 4.0)
        assertEquals(4.75, falta!!, 0.001)
    }

    @Test
    fun `una meta inalcanzable se dice tal cual, sin recortarla a la escala`() {
        // Recortarlo a 5,0 se leería como «justo lo justo» cuando lo que pasa es que no da.
        val falta = CalculatorMath.neededGrade(current = 3.5, evaluatedPercent = 60.0, target = 4.5)
        assertTrue("tiene que salirse de la escala", falta!! > 5.0)
        assertEquals(6.0, falta, 0.001)
    }

    @Test
    fun `una meta ya alcanzada no pide nada`() {
        val falta = CalculatorMath.neededGrade(current = 4.6, evaluatedPercent = 90.0, target = 4.0)
        assertTrue("no hace falta sacar nada", falta!! <= 0.0)
    }

    @Test
    fun `con el curso entero evaluado ya no hay nota que sacar`() {
        assertNull(CalculatorMath.neededGrade(current = 3.0, evaluatedPercent = 100.0, target = 4.0))
    }

    @Test
    fun `un porcentaje imposible se recorta antes de calcular`() {
        // 120 % evaluado no existe: se trata como el curso terminado.
        assertNull(CalculatorMath.neededGrade(current = 3.0, evaluatedPercent = 120.0, target = 4.0))
    }
}
