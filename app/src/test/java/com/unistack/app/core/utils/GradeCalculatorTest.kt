package com.unistack.app.core.utils

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeCalculatorTest {
    @Test
    fun `format grade follows active numeric scale`() {
        assertEquals("4.3", GradingScaleUtils.formatGrade(4.25, GradingScale.ZERO_TO_FIVE))
        assertEquals("89", GradingScaleUtils.formatGrade(88.6, GradingScale.ZERO_TO_HUNDRED))
        assertEquals("89", GradingScaleUtils.formatGrade(88.6, GradingScale.CUSTOM))
        assertEquals(NO_DATA, GradingScaleUtils.formatGrade(null, GradingScale.ZERO_TO_FIVE))
    }

    @Test
    fun `max grade matches supported numeric scales`() {
        assertEquals(5.0, GradingScaleUtils.maxGradeFor(GradingScale.ZERO_TO_FIVE), 0.0)
        assertEquals(100.0, GradingScaleUtils.maxGradeFor(GradingScale.ZERO_TO_HUNDRED), 0.0)
        assertEquals(100.0, GradingScaleUtils.maxGradeFor(GradingScale.CUSTOM), 0.0)
    }

    @Test
    fun `current average is weighted by evaluated percentage`() {
        val grades = listOf(
            GradeItem(id = "1", name = "Parcial", value = 4.0, percentage = 0.5),
            GradeItem(id = "2", name = "Quiz", value = 3.0, percentage = 0.25)
        )

        assertEquals(3.7, GradeCalculator.calculateCurrentAverage(grades)!!, 0.0)
    }

    @Test
    fun `cut projection uses internal grade weights and cut weights`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.30, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.40, order = 2),
            GradingCut(id = "period-3", name = "Corte 3", weight = 0.30, order = 3)
        )
        val grades = listOf(
            GradeItem(id = "1", name = "Taller", value = 5.0, percentage = 0.20, cutId = "period-1"),
            GradeItem(id = "2", name = "Exposición", value = 4.0, percentage = 0.20, cutId = "period-1"),
            GradeItem(id = "3", name = "Parcial", value = 4.5, percentage = 0.60, cutId = "period-1")
        )

        assertEquals(4.5, GradeCalculator.calculateCutAverage(grades)!!, 0.0)
        assertEquals(1.35, GradeCalculator.calculateWeightedPointsByCuts(grades, cuts), 0.0001)
        assertEquals(30.0, GradeCalculator.calculateEvaluatedSemesterPercentage(grades, cuts), 0.0)
        assertEquals(4.5, GradeCalculator.calculateCurrentAverageByCuts(grades, cuts)!!, 0.0)
    }

    @Test
    fun `official cut result overrides incomplete activity detail`() {
        val grades = listOf(
            GradeItem(id = "1", name = "Quiz", value = 3.0, percentage = 0.20),
            GradeItem(id = "2", name = "Nota final del corte", value = 4.1, percentage = 1.0, source = GradeSource.PERIOD_FINAL)
        )

        val result = GradeCalculator.calculateCut(grades)

        assertEquals(4.1, result.average!!, 0.0)
        assertEquals(1.0, result.evaluatedFraction, 0.0)
        assertEquals(4.1, result.weightedPoints, 0.0)
        assertTrue(result.usesOfficialResult)
    }

    @Test
    fun `activity with unknown weight is preserved but excluded from projection`() {
        val grades = listOf(
            GradeItem(id = "1", name = "Parcial", value = 4.0, percentage = 0.50),
            GradeItem(
                id = "2",
                name = "Taller",
                value = 5.0,
                percentage = 0.0,
                weightStatus = GradeWeightStatus.UNKNOWN
            )
        )

        val result = GradeCalculator.calculateCut(grades)

        assertEquals(4.0, result.average!!, 0.0)
        assertEquals(0.50, result.evaluatedFraction, 0.0)
        assertEquals(1, result.unknownWeightCount)
    }

    @Test
    fun `late start with prior official result calculates remaining target correctly`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.30, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.40, order = 2),
            GradingCut(id = "period-3", name = "Corte 3", weight = 0.30, order = 3)
        )
        val grades = listOf(
            GradeItem(
                id = "1",
                name = "Resultado Corte 1",
                value = 4.1,
                percentage = 1.0,
                cutId = "period-1",
                source = GradeSource.PERIOD_FINAL
            )
        )

        val result = GradeCalculator.calculateSubject(
            grades = grades,
            cuts = cuts,
            targetAverage = 4.5,
            maxGrade = 5.0
        )

        assertEquals(4.1, result.currentAverage!!, 0.0)
        assertEquals(0.30, result.evaluatedSemesterFraction, 0.0001)
        assertEquals(4.7, result.neededForTarget!!, 0.0)
        assertTrue(result.targetIsReachable == true)
    }

    @Test
    fun `needed grade is calculated for each numeric scale`() {
        assertEquals(
            4.7,
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 2.6,
                remainingPercentage = 0.3,
                targetAverage = 4.0,
                maxGrade = 5.0
            )!!,
            0.0
        )
        assertEquals(
            9.3,
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 5.2,
                remainingPercentage = 0.3,
                targetAverage = 8.0,
                maxGrade = 10.0
            )!!,
            0.0
        )
        assertEquals(
            93.3,
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 52.0,
                remainingPercentage = 0.3,
                targetAverage = 80.0,
                maxGrade = 100.0
            )!!,
            0.0
        )
    }

    @Test
    fun `needed grade returns impossible value uncapped for caller messaging`() {
        assertEquals(
            12.0,
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 4.4,
                remainingPercentage = 0.3,
                targetAverage = 8.0,
                maxGrade = 10.0
            )!!,
            0.0
        )
    }

    @Test
    fun `needed grade is null when no percentage remains`() {
        assertNull(
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 4.0,
                remainingPercentage = 0.0,
                targetAverage = 4.0,
                maxGrade = 5.0
            )
        )
    }

    @Test
    fun `average never exceeds the scale when weights add past 100 percent`() {
        // Tres actividades del 50% suman 150%. Antes se dividía por la fracción recortada
        // a 1.0 y el promedio salía 7.5 en una escala de 0 a 5.
        val grades = listOf(
            GradeItem(id = "1", name = "Parcial", value = 5.0, percentage = 0.5),
            GradeItem(id = "2", name = "Taller", value = 5.0, percentage = 0.5),
            GradeItem(id = "3", name = "Quiz", value = 5.0, percentage = 0.5)
        )

        val calculation = GradeCalculator.calculateCut(grades)

        assertEquals(5.0, calculation.average!!, 0.0001)
        assertEquals("el progreso mostrado sigue tope 100%", 1.0, calculation.evaluatedFraction, 0.0)
        assertEquals(1.5, calculation.allocatedFraction, 0.0001)
    }

    @Test
    fun `weights past 100 percent still produce a real weighted average`() {
        val grades = listOf(
            GradeItem(id = "1", name = "Parcial", value = 4.0, percentage = 0.6),
            GradeItem(id = "2", name = "Taller", value = 2.0, percentage = 0.6)
        )

        // (4.0*0.6 + 2.0*0.6) / 1.2 = 3.0
        assertEquals(3.0, GradeCalculator.calculateCut(grades).average!!, 0.0001)
    }

    @Test
    fun `the subject does not inflate what an over allocated cut already corrected`() {
        // Tres actividades del 50% con 5.0: el corte ya devolvía 5.0, pero la materia sumaba
        // los puntos crudos (7.5) y los dividía entre la fracción recortada a 1.0, así que la
        // tarjeta del corte decía 5.0 y la de la materia 7.5 con los mismos datos.
        val cuts = listOf(GradingCut(id = "period-1", name = "Corte 1", weight = 1.0, order = 1))
        val grades = listOf(
            GradeItem(id = "1", name = "Parcial", value = 5.0, percentage = 0.5),
            GradeItem(id = "2", name = "Taller", value = 5.0, percentage = 0.5),
            GradeItem(id = "3", name = "Quiz", value = 5.0, percentage = 0.5)
        )

        assertEquals(5.0, GradeCalculator.calculateCurrentAverageByCuts(grades, cuts)!!, 0.0001)
        assertEquals(5.0, GradeCalculator.calculateWeightedPointsByCuts(grades, cuts), 0.0001)
    }

    @Test
    fun `floor and ceiling frame the target without guessing the future`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.30, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.40, order = 2),
            GradingCut(id = "period-3", name = "Corte 3", weight = 0.30, order = 3)
        )
        // Corte 1 cerrado en 60 sobre 100. Queda el 70% del semestre.
        val grades = listOf(
            GradeItem(id = "1", name = "Parcial", value = 60.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(grades, cuts, targetAverage = 80.0, maxGrade = 100.0)

        assertEquals("promedio de lo evaluado", 60.0, result.currentAverage!!, 0.0001)
        assertEquals("si saca 0 en lo que falta", 18.0, result.guaranteedMinimum!!, 0.0001)
        assertEquals("si lo borda en lo que falta", 88.0, result.bestPossible!!, 0.0001)
        assertEquals(TargetOutlook.AT_RISK, result.outlook)
        assertFalse(result.isFinished)
    }

    @Test
    fun `a barely started cut no longer swings the subject number`() {
        // Este era el caso que rompía el detalle de materia: contaba el Corte 2 entero al
        // ritmo de su única nota, así que un quiz del 5% sacado en 100 disparaba la cifra.
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.30, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.40, order = 2),
            GradingCut(id = "period-3", name = "Corte 3", weight = 0.30, order = 3)
        )
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 60.0, percentage = 1.0, cutId = "period-1"),
            GradeItem(id = "2", name = "Quiz", value = 100.0, percentage = 0.05, cutId = "period-2")
        )

        val result = GradeCalculator.calculateSubject(grades, cuts, targetAverage = 80.0, maxGrade = 100.0)

        // (60*0.30 + 100*0.05*0.40) / (0.30 + 0.05*0.40) = 20 / 0.32 = 62.5
        assertEquals(62.5, result.currentAverage!!, 0.0001)
    }

    @Test
    fun `a finished subject reports its final grade instead of asking for more`() {
        val cuts = listOf(GradingCut(id = "period-1", name = "Corte 1", weight = 1.0, order = 1))
        val grades = listOf(
            GradeItem(id = "1", name = "Final", value = 84.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(grades, cuts, targetAverage = 80.0, maxGrade = 100.0)

        assertTrue(result.isFinished)
        assertEquals(84.0, result.guaranteedMinimum!!, 0.0001)
        assertEquals("el suelo y el techo coinciden cuando ya no queda nada", 84.0, result.bestPossible!!, 0.0001)
        assertEquals(TargetOutlook.SECURED, result.outlook)
        assertNull("no queda nada que pedir", result.neededForTarget)
    }

    @Test
    fun `an unreachable target is called unreachable and not just hard`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.70, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.30, order = 2)
        )
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 40.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(grades, cuts, targetAverage = 80.0, maxGrade = 100.0)

        // 28 + 0.30*100 = 58, por debajo de 80 aunque saque 100 en todo lo que queda.
        assertEquals(58.0, result.bestPossible!!, 0.0001)
        assertEquals(TargetOutlook.UNREACHABLE, result.outlook)
    }

    @Test
    fun `well formed cuts are not flagged as over allocated`() {
        val grades = listOf(
            GradeItem(id = "1", name = "Parcial", value = 4.0, percentage = 0.5),
            GradeItem(id = "2", name = "Quiz", value = 3.0, percentage = 0.25)
        )

        val calculation = GradeCalculator.calculateCut(grades)

        assertEquals(0.75, calculation.allocatedFraction, 0.0001)
    }

    @Test
    fun `going short of the goal is not the same alarm as failing the subject`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.50, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.50, order = 2)
        )
        // 78 de 100 con la meta en 80 y el aprobado en 60: va corto, no va perdiendo.
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 78.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(
            grades = grades,
            cuts = cuts,
            targetAverage = 80.0,
            maxGrade = 100.0,
            passingGrade = 60.0
        )

        assertEquals(TargetOutlook.AT_RISK, result.outlook)
        assertEquals(GradeAlertLevel.BEHIND, result.alertLevel)
    }

    @Test
    fun `being below the passing grade early on is a warning, not an alarm`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.20, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.80, order = 2)
        )
        // 40 de 100 con el 20 % evaluado: el promedio de hoy no aprueba, pero el techo es
        // 8 + 80 = 88 y queda el 80 % del semestre por delante. Es un aviso, no una alarma.
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 40.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(
            grades = grades,
            cuts = cuts,
            targetAverage = 80.0,
            maxGrade = 100.0,
            passingGrade = 60.0
        )

        assertEquals(88.0, result.bestPossible!!, 0.0001)
        assertEquals(GradeAlertLevel.BEHIND, result.alertLevel)
    }

    @Test
    fun `being below the passing grade early on is still critical if it can no longer be passed`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.20, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.80, order = 2)
        )
        // Mismo 20 % evaluado, pero con el aprobado en 95: ni sacando todo lo que queda se llega.
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 40.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(
            grades = grades,
            cuts = cuts,
            targetAverage = 98.0,
            maxGrade = 100.0,
            passingGrade = 95.0
        )

        assertEquals(GradeAlertLevel.CRITICAL, result.alertLevel)
    }

    @Test
    fun `being below the passing grade late in the term is critical`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.70, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.30, order = 2)
        )
        // 70 % evaluado: queda menos de medio semestre para levantar un 40.
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 40.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(
            grades = grades,
            cuts = cuts,
            targetAverage = 80.0,
            maxGrade = 100.0,
            passingGrade = 60.0
        )

        assertEquals(GradeAlertLevel.CRITICAL, result.alertLevel)
    }

    @Test
    fun `a subject that can no longer be passed is critical`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.80, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.20, order = 2)
        )
        // El techo ya no llega al aprobado, aunque quede un 20% del semestre por delante.
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 50.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(
            grades = grades,
            cuts = cuts,
            targetAverage = 80.0,
            maxGrade = 100.0,
            passingGrade = 70.0
        )

        // 40 + 0.20*100 = 60, por debajo del 70 con el que se aprueba.
        assertEquals(60.0, result.bestPossible!!, 0.0001)
        assertEquals(GradeAlertLevel.CRITICAL, result.alertLevel)
    }

    @Test
    fun `without a passing grade the goal takes its place`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.70, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.30, order = 2)
        )
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 78.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(grades, cuts, targetAverage = 80.0, maxGrade = 100.0)

        assertEquals(GradeAlertLevel.CRITICAL, result.alertLevel)
    }

    @Test
    fun `a subject on track carries no alert at all`() {
        val cuts = listOf(
            GradingCut(id = "period-1", name = "Corte 1", weight = 0.50, order = 1),
            GradingCut(id = "period-2", name = "Corte 2", weight = 0.50, order = 2)
        )
        val grades = listOf(
            GradeItem(id = "1", name = "Corte 1", value = 90.0, percentage = 1.0, cutId = "period-1")
        )

        val result = GradeCalculator.calculateSubject(
            grades = grades,
            cuts = cuts,
            targetAverage = 80.0,
            maxGrade = 100.0,
            passingGrade = 60.0
        )

        assertEquals(GradeAlertLevel.NONE, result.alertLevel)
    }
}
