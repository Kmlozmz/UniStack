package com.unistack.app.feature_schedule.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * El historial agrupado por semanas, con lo que viene aparte.
 *
 * El reparto importa por una razón concreta: las clases futuras salían en la misma lista
 * marcadas «Pendiente», que es la etiqueta de las que se te olvidaron. Aquí se comprueba que
 * no se mezclen y que las semanas vacías no se inventen.
 */
class AttendanceWeekTest {

    // Jueves 27 de agosto de 2026; su lunes es el 24.
    private val hoy: LocalDate = LocalDate.of(2026, 8, 27)
    private val lunesDeHoy: LocalDate = LocalDate.of(2026, 8, 24)

    private val lunesYmiercoles = ClassSession(
        id = "s-1",
        subjectId = "mat-1",
        daysOfWeek = setOf(DayOfWeek.MONDAY.value, DayOfWeek.WEDNESDAY.value),
        startMinute = 8 * 60,
        endMinute = 10 * 60,
        createdAt = 0L,
        updatedAt = 0L
    )

    private fun construir(
        inicio: LocalDate = hoy.minusDays(30),
        breaks: List<ClosedRange<LocalDate>> = emptyList()
    ) = SubjectAttendanceHistory.build(
        sessions = listOf(lunesYmiercoles),
        occurrences = emptyList(),
        today = hoy,
        termStart = inicio,
        breaks = breaks
    )

    @Test
    fun `lo que viene no entra en las semanas`() {
        val entradas = construir()

        val semanas = SubjectAttendanceHistory.byWeek(entradas, hoy)

        assertTrue(semanas.flatMap { it.entries }.none { it.date.isAfter(hoy) })
    }

    @Test
    fun `lo que viene sale en su propia lista y ordenado`() {
        val entradas = construir()

        val proximas = SubjectAttendanceHistory.upcoming(entradas, hoy)

        assertTrue(proximas.isNotEmpty())
        assertTrue(proximas.all { it.date.isAfter(hoy) })
        assertEquals(proximas.map { it.date }.sorted(), proximas.map { it.date })
    }

    @Test
    fun `cada semana empieza en lunes`() {
        val semanas = SubjectAttendanceHistory.byWeek(construir(), hoy)

        assertTrue(semanas.all { it.start.dayOfWeek == DayOfWeek.MONDAY })
    }

    @Test
    fun `la semana de hoy va primero`() {
        val semanas = SubjectAttendanceHistory.byWeek(construir(), hoy)

        assertEquals(lunesDeHoy, semanas.first().start)
        assertTrue(semanas.first().contains(hoy))
    }

    @Test
    fun `las semanas van de la mas reciente a la mas antigua`() {
        val semanas = SubjectAttendanceHistory.byWeek(construir(), hoy)

        val inicios = semanas.map { it.start }
        assertEquals(inicios.sortedDescending(), inicios)
    }

    @Test
    fun `dentro de una semana las clases van en orden`() {
        val semanas = SubjectAttendanceHistory.byWeek(construir(), hoy)

        val unaLlena = semanas.first { it.entries.size > 1 }
        assertEquals(unaLlena.entries.map { it.date }.sorted(), unaLlena.entries.map { it.date })
    }

    @Test
    fun `una semana de receso no aparece en la lista`() {
        val receso = hoy.minusDays(16)..hoy.minusDays(10)

        val semanas = SubjectAttendanceHistory.byWeek(construir(breaks = listOf(receso)), hoy)

        // No es que salga vacía: es que no está, porque no hubo clases que agrupar.
        assertTrue(semanas.none { semana -> semana.entries.any { it.date in receso } })
    }

    @Test
    fun `ninguna semana sale sin clases`() {
        val semanas = SubjectAttendanceHistory.byWeek(construir(), hoy)

        assertTrue(semanas.all { it.entries.isNotEmpty() })
    }

    // ---------- ponerse al dia ----------

    @Test
    fun `ponerse al dia solo trae lo pasado y sin marcar`() {
        val pendientes = SubjectAttendanceHistory.pendingToCatchUp(construir(), hoy)

        assertTrue(pendientes.isNotEmpty())
        assertTrue(pendientes.all { it.status == ClassAttendanceStatus.PENDING })
        assertTrue(pendientes.all { !it.date.isAfter(hoy) })
    }

    @Test
    fun `ponerse al dia empieza por lo mas reciente`() {
        val pendientes = SubjectAttendanceHistory.pendingToCatchUp(construir(), hoy)

        val fechas = pendientes.map { it.date }
        assertEquals(fechas.sortedDescending(), fechas)
    }

    @Test
    fun `una lista larga se corta para que se pueda terminar`() {
        val pendientes = SubjectAttendanceHistory.pendingToCatchUp(
            construir(inicio = hoy.minusDays(90)),
            hoy
        )

        assertTrue(pendientes.size <= SubjectAttendanceHistory.CATCH_UP_LIMIT)
    }

    @Test
    fun `lo ya marcado no vuelve a preguntarse`() {
        val unLunes = hoy.minusDays(3)
        assertEquals(DayOfWeek.MONDAY, unLunes.dayOfWeek)
        val entradas = SubjectAttendanceHistory.build(
            sessions = listOf(lunesYmiercoles),
            occurrences = listOf(
                ClassOccurrence(
                    id = "o-1",
                    sessionId = lunesYmiercoles.id,
                    dateEpochDay = unLunes.toEpochDay(),
                    status = ClassAttendanceStatus.ATTENDED,
                    updatedAt = 0L
                )
            ),
            today = hoy,
            termStart = hoy.minusDays(30)
        )

        val pendientes = SubjectAttendanceHistory.pendingToCatchUp(entradas, hoy)

        assertTrue(pendientes.none { it.date == unLunes })
    }

    @Test
    fun `sin nada que agrupar no hay semanas`() {
        val semanas = SubjectAttendanceHistory.byWeek(emptyList(), hoy)

        assertTrue(semanas.isEmpty())
    }
}
