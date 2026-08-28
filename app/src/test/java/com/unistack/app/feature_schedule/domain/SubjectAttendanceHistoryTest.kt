package com.unistack.app.feature_schedule.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * El historial de asistencia contado desde el periodo.
 *
 * Lo que se prueba aquí es sobre todo lo que **no** debe aparecer: la lista se construye a
 * partir de la regla de repetición, así que sin un corte por fecha una materia recién creada
 * salía con meses de clases «pendientes» que nunca se dieron.
 */
class SubjectAttendanceHistoryTest {

    // Un jueves, para que las cuentas de días de la semana se lean solas.
    private val hoy: LocalDate = LocalDate.of(2026, 8, 27)

    private val martes = sesion("s-martes", DayOfWeek.TUESDAY)

    private fun sesion(id: String, vararg dias: DayOfWeek) = ClassSession(
        id = id,
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
        inicio: LocalDate? = null,
        fin: LocalDate? = null,
        occurrences: List<ClassOccurrence> = emptyList()
    ) = SubjectAttendanceHistory.build(
        sessions = listOf(martes),
        occurrences = occurrences,
        today = hoy,
        termStart = inicio,
        termEnd = fin
    )

    @Test
    fun `sin periodo se mantiene la ventana de siempre`() {
        val entradas = construir()

        assertTrue(entradas.isNotEmpty())
        // Nunca antes del tope de seguridad, aunque no haya periodo que acote.
        assertTrue(entradas.all { !it.date.isBefore(hoy.minusDays(SubjectAttendanceHistory.LOOKBACK_DAYS)) })
    }

    @Test
    fun `no hay clases anteriores al inicio del periodo`() {
        val inicio = hoy.minusDays(9)

        val entradas = construir(inicio = inicio)

        assertTrue(entradas.isNotEmpty())
        assertTrue(entradas.all { !it.date.isBefore(inicio) })
    }

    @Test
    fun `una materia de un periodo que arranca hoy no acumula faltas`() {
        val entradas = construir(inicio = hoy)

        val pasadas = entradas.filter { !it.date.isAfter(hoy) }
        assertTrue(pasadas.isEmpty())
    }

    @Test
    fun `un periodo que aun no ha empezado no tiene ni una clase`() {
        val entradas = construir(inicio = hoy.plusDays(60), fin = hoy.plusDays(160))

        assertTrue(entradas.isEmpty())
    }

    @Test
    fun `no se anuncian clases posteriores al fin del periodo`() {
        val fin = hoy.plusDays(6)

        val entradas = construir(inicio = hoy.minusDays(30), fin = fin)

        assertTrue(entradas.all { !it.date.isAfter(fin) })
    }

    @Test
    fun `lo marcado conserva su estado`() {
        val inicio = hoy.minusDays(30)
        val unMartes = hoy.minusDays(2)
        assertEquals(DayOfWeek.TUESDAY, unMartes.dayOfWeek)

        val entradas = construir(
            inicio = inicio,
            occurrences = listOf(marcada(unMartes, ClassAttendanceStatus.ABSENT))
        )

        val entrada = entradas.first { it.date == unMartes }
        assertEquals(ClassAttendanceStatus.ABSENT, entrada.status)
    }

    @Test
    fun `lo que nadie marco queda pendiente`() {
        val entradas = construir(inicio = hoy.minusDays(30))

        val anterior = entradas.first { it.date.isBefore(hoy) }
        assertEquals(ClassAttendanceStatus.PENDING, anterior.status)
    }

    @Test
    fun `se anuncian como mucho dos clases futuras`() {
        val entradas = construir(inicio = hoy.minusDays(60))

        assertEquals(
            SubjectAttendanceHistory.UPCOMING_LIMIT,
            entradas.count { it.date.isAfter(hoy) }
        )
    }

    @Test
    fun `el historial pasado no crece sin limite`() {
        // Tres dias por semana: en la ventana de 120 dias caben mas clases que el tope.
        val casiDiaria = sesion("s-3", DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY)

        val entradas = SubjectAttendanceHistory.build(
            sessions = listOf(casiDiaria),
            occurrences = emptyList(),
            today = hoy,
            termStart = hoy.minusDays(360)
        )

        assertEquals(
            SubjectAttendanceHistory.RECENT_LIMIT,
            entradas.count { !it.date.isAfter(hoy) }
        )
    }

    @Test
    fun `la lista va de lo mas reciente a lo mas antiguo`() {
        val entradas = construir(inicio = hoy.minusDays(60))

        val fechas = entradas.map { it.date }
        assertEquals(fechas.sortedDescending(), fechas)
    }

    @Test
    fun `solo salen los dias en que hay clase`() {
        val entradas = construir(inicio = hoy.minusDays(60))

        assertTrue(entradas.all { it.date.dayOfWeek == DayOfWeek.TUESDAY })
    }

    @Test
    fun `una clase cada dos semanas no se cuenta todas las semanas`() {
        val quincenal = martes.copy(
            repeatEveryWeeks = 2,
            recurrenceStartEpochDay = hoy.minusDays(28).toEpochDay()
        )

        val entradas = SubjectAttendanceHistory.build(
            sessions = listOf(quincenal),
            occurrences = emptyList(),
            today = hoy,
            termStart = hoy.minusDays(28)
        )

        val pasadas = entradas.filter { !it.date.isAfter(hoy) }.map { it.date }
        assertFalse(pasadas.contains(hoy.minusDays(9)))
        assertTrue(pasadas.contains(hoy.minusDays(2)))
    }
}
