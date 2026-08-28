package com.unistack.app.feature_schedule.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Las cuentas que abren el historial, y los días que no cuentan.
 *
 * Dos cosas se prueban aquí. Una: que un festivo no acabe apuntado como clase perdida, que es
 * el único sitio donde la app llegaba a registrar algo directamente falso. Y otra: que el
 * porcentaje y las faltas restantes digan «no sé» en vez de inventarse un número.
 */
class AttendanceSummaryTest {

    private val hoy: LocalDate = LocalDate.of(2026, 8, 27)
    private val martes = sesion(DayOfWeek.TUESDAY)

    private fun sesion(vararg dias: DayOfWeek) = ClassSession(
        id = "s-1",
        subjectId = "mat-1",
        daysOfWeek = dias.map { it.value }.toSet(),
        startMinute = 8 * 60,
        endMinute = 10 * 60,
        createdAt = 0L,
        updatedAt = 0L
    )

    private fun marcada(fecha: LocalDate, estado: ClassAttendanceStatus) = ClassOccurrence(
        id = "o-${fecha.toEpochDay()}",
        sessionId = martes.id,
        dateEpochDay = fecha.toEpochDay(),
        status = estado,
        updatedAt = 0L
    )

    private fun construir(
        occurrences: List<ClassOccurrence> = emptyList(),
        breaks: List<ClosedRange<LocalDate>> = emptyList()
    ) = SubjectAttendanceHistory.build(
        sessions = listOf(martes),
        occurrences = occurrences,
        today = hoy,
        termStart = hoy.minusDays(60),
        breaks = breaks
    )

    // ---------- días sin clase ----------

    @Test
    fun `un festivo no aparece como clase`() {
        val festivo = hoy.minusDays(2)
        assertEquals(DayOfWeek.TUESDAY, festivo.dayOfWeek)

        val entradas = construir(breaks = listOf(festivo..festivo))

        assertTrue(entradas.none { it.date == festivo })
    }

    @Test
    fun `una semana de receso se lleva todas sus clases`() {
        val receso = hoy.minusDays(16)..hoy.minusDays(10)

        val entradas = construir(breaks = listOf(receso))

        assertTrue(entradas.none { it.date in receso })
        // Y no se lleva por delante las de alrededor.
        assertTrue(entradas.any { it.date.isBefore(receso.start) })
        assertTrue(entradas.any { it.date.isAfter(receso.endInclusive) })
    }

    @Test
    fun `dos tramos solapados no rompen nada`() {
        val receso = hoy.minusDays(16)..hoy.minusDays(10)
        val festivoDentro = hoy.minusDays(14)..hoy.minusDays(14)

        val entradas = construir(breaks = listOf(receso, festivoDentro))

        assertTrue(entradas.none { it.date in receso })
    }

    // ---------- el resumen ----------

    @Test
    fun `sin nada marcado no hay porcentaje`() {
        val resumen = SubjectAttendanceHistory.summarize(construir())

        assertNull(resumen.rate)
        assertEquals(0, resumen.decided)
    }

    @Test
    fun `el porcentaje sale de lo decidido, no de todo`() {
        val entradas = construir(
            occurrences = listOf(
                marcada(hoy.minusDays(2), ClassAttendanceStatus.ATTENDED),
                marcada(hoy.minusDays(9), ClassAttendanceStatus.ATTENDED),
                marcada(hoy.minusDays(16), ClassAttendanceStatus.ABSENT),
                marcada(hoy.minusDays(23), ClassAttendanceStatus.ATTENDED)
            )
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas)

        assertEquals(3, resumen.attended)
        assertEquals(1, resumen.absent)
        assertEquals(4, resumen.decided)
        assertEquals(75, resumen.rate)
    }

    @Test
    fun `una cancelada no cuenta ni a favor ni en contra`() {
        val entradas = construir(
            occurrences = listOf(
                marcada(hoy.minusDays(2), ClassAttendanceStatus.ATTENDED),
                marcada(hoy.minusDays(9), ClassAttendanceStatus.CANCELLED)
            )
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas)

        assertEquals(1, resumen.decided)
        assertEquals(100, resumen.rate)
    }

    @Test
    fun `un porcentaje sobre pocas clases se marca como poco fiable`() {
        val entradas = construir(
            occurrences = listOf(marcada(hoy.minusDays(2), ClassAttendanceStatus.ATTENDED))
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas)

        assertEquals(100, resumen.rate)
        assertTrue(resumen.tooFewToTrust)
    }

    // ---------- el tope de faltas ----------

    @Test
    fun `sin tope no se promete cuantas quedan`() {
        val resumen = SubjectAttendanceHistory.summarize(construir(), absenceLimit = null)

        assertNull(resumen.remainingAbsences)
        assertFalse(resumen.atLimit)
    }

    @Test
    fun `las faltas restantes se descuentan del tope`() {
        val entradas = construir(
            occurrences = listOf(
                marcada(hoy.minusDays(2), ClassAttendanceStatus.ABSENT),
                marcada(hoy.minusDays(9), ClassAttendanceStatus.ABSENT)
            )
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas, absenceLimit = 6)

        assertEquals(4, resumen.remainingAbsences)
        assertFalse(resumen.atLimit)
    }

    @Test
    fun `pasarse del tope no da un numero negativo`() {
        val entradas = construir(
            occurrences = listOf(
                marcada(hoy.minusDays(2), ClassAttendanceStatus.ABSENT),
                marcada(hoy.minusDays(9), ClassAttendanceStatus.ABSENT),
                marcada(hoy.minusDays(16), ClassAttendanceStatus.ABSENT)
            )
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas, absenceLimit = 2)

        assertEquals(0, resumen.remainingAbsences)
        assertTrue(resumen.atLimit)
    }

    @Test
    fun `queda una falta y se sabe`() {
        val entradas = construir(
            occurrences = listOf(marcada(hoy.minusDays(2), ClassAttendanceStatus.ABSENT))
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas, absenceLimit = 2)

        assertTrue(resumen.oneLeft)
    }

    // ---------- la racha ----------

    @Test
    fun `la racha cuenta las asistidas seguidas mas recientes`() {
        val entradas = construir(
            occurrences = listOf(
                marcada(hoy.minusDays(2), ClassAttendanceStatus.ATTENDED),
                marcada(hoy.minusDays(9), ClassAttendanceStatus.ATTENDED),
                marcada(hoy.minusDays(16), ClassAttendanceStatus.ABSENT),
                marcada(hoy.minusDays(23), ClassAttendanceStatus.ATTENDED)
            )
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas)

        assertEquals(2, resumen.streak)
    }

    @Test
    fun `una falta reciente deja la racha en cero`() {
        val entradas = construir(
            occurrences = listOf(
                marcada(hoy.minusDays(2), ClassAttendanceStatus.ABSENT),
                marcada(hoy.minusDays(9), ClassAttendanceStatus.ATTENDED)
            )
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas)

        assertEquals(0, resumen.streak)
    }

    @Test
    fun `una cancelada por medio no corta la racha`() {
        val entradas = construir(
            occurrences = listOf(
                marcada(hoy.minusDays(2), ClassAttendanceStatus.ATTENDED),
                marcada(hoy.minusDays(9), ClassAttendanceStatus.CANCELLED),
                marcada(hoy.minusDays(16), ClassAttendanceStatus.ATTENDED)
            )
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas)

        assertEquals(2, resumen.streak)
    }

    @Test
    fun `una clase pasada sin marcar si corta la racha`() {
        val entradas = construir(
            occurrences = listOf(
                marcada(hoy.minusDays(2), ClassAttendanceStatus.ATTENDED),
                // La del medio se queda sin marcar: no sabemos qué pasó.
                marcada(hoy.minusDays(16), ClassAttendanceStatus.ATTENDED)
            )
        )

        val resumen = SubjectAttendanceHistory.summarize(entradas)

        assertEquals(1, resumen.streak)
    }
}
