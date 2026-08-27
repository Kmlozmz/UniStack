package com.unistack.app.feature_terms.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Las reglas del periodo académico.
 *
 * Las fechas son el dato que faltaba en toda la app, así que su comportamiento se fija aquí
 * antes de que nada se apoye en él.
 */
class AcademicTermTest {

    private fun dia(texto: String) = LocalDate.parse(texto)

    private fun periodo(
        start: String = "2026-08-10",
        plannedEnd: String? = "2026-12-12",
        closed: String? = null
    ) = AcademicTerm(
        id = "t1",
        userId = "local",
        name = "2026-2",
        type = AcademicTermType.SEMESTER,
        startEpochDay = dia(start).toEpochDay(),
        plannedEndEpochDay = plannedEnd?.let { dia(it).toEpochDay() },
        closedEpochDay = closed?.let { dia(it).toEpochDay() },
        status = if (closed == null) AcademicTermStatus.ACTIVE else AcademicTermStatus.CLOSED,
        createdAt = 0L,
        updatedAt = 0L
    )

    // --- Hasta dónde llega el periodo ---

    @Test
    fun `el periodo activo llega hasta hoy, no hasta la fecha prevista`() {
        val hoy = dia("2026-10-01")
        assertEquals(hoy, periodo().endFor(hoy))
    }

    /**
     * El fin previsto no cierra nada.
     *
     * Se pide en agosto como estimación, y las fechas se mueven. Si el periodo llegara a su
     * fin el día previsto, las clases de la semana siguiente quedarían fuera de todo periodo
     * sin que nadie lo hubiera decidido.
     */
    @Test
    fun `pasado el fin previsto sin cerrar, el periodo sigue corriendo`() {
        val hoy = dia("2026-12-20")
        val p = periodo(plannedEnd = "2026-12-12")
        assertEquals(hoy, p.endFor(hoy))
        assertTrue(p.contains(dia("2026-12-18"), hoy))
    }

    @Test
    fun `un periodo cerrado llega hasta el dia en que se cerro`() {
        val hoy = dia("2027-03-01")
        val p = periodo(closed = "2026-12-15")
        assertEquals(dia("2026-12-15"), p.endFor(hoy))
        assertFalse(p.contains(dia("2027-01-10"), hoy))
    }

    // --- Qué cae dentro ---

    @Test
    fun `una fecha anterior al inicio queda fuera`() {
        val hoy = dia("2026-10-01")
        // Justo el problema que motivo todo: las clases de junio no existieron.
        assertFalse(periodo().contains(dia("2026-06-29"), hoy))
        assertTrue(periodo().contains(dia("2026-08-10"), hoy))
    }

    @Test
    fun `los dos extremos cuentan como dentro`() {
        val hoy = dia("2026-12-15")
        val p = periodo(closed = "2026-12-12")
        assertTrue(p.contains(dia("2026-08-10"), hoy))
        assertTrue(p.contains(dia("2026-12-12"), hoy))
    }

    // --- El aviso del dia de fin ---

    @Test
    fun `el aviso salta el dia previsto y no antes`() {
        val p = periodo(plannedEnd = "2026-12-12")
        assertFalse(p.isPastPlannedEnd(dia("2026-12-11")))
        assertTrue(p.isPastPlannedEnd(dia("2026-12-12")))
        assertTrue(p.isPastPlannedEnd(dia("2026-12-30")))
    }

    @Test
    fun `un periodo cerrado ya no avisa de nada`() {
        assertFalse(periodo(closed = "2026-12-12").isPastPlannedEnd(dia("2027-01-01")))
    }

    @Test
    fun `sin fin previsto no hay aviso`() {
        assertFalse(periodo(plannedEnd = null).isPastPlannedEnd(dia("2030-01-01")))
    }

    // --- Validez ---

    @Test
    fun `un periodo que acaba antes de empezar no vale`() {
        val malo = periodo().copy(plannedEndEpochDay = dia("2026-08-01").toEpochDay())
        assertFalse(malo.isValid)
    }

    @Test
    fun `cerrado sin fecha de cierre no vale, y activo con ella tampoco`() {
        assertFalse(periodo().copy(status = AcademicTermStatus.CLOSED).isValid)
        assertFalse(periodo(closed = "2026-12-12").copy(status = AcademicTermStatus.ACTIVE).isValid)
    }

    @Test
    fun `sin fin previsto sigue siendo valido`() {
        assertTrue(periodo(plannedEnd = null).isValid)
    }

    // --- Sugerencias ---

    @Test
    fun `el nombre sugerido sale del año y de la mitad en que empieza`() {
        assertEquals("2026-2", AcademicTerm.suggestedName(AcademicTermType.SEMESTER, dia("2026-08-10")))
        assertEquals("2026-1", AcademicTerm.suggestedName(AcademicTermType.SEMESTER, dia("2026-02-01")))
        // El anual no lleva ordinal: solo hay uno.
        assertEquals("2026", AcademicTerm.suggestedName(AcademicTermType.ANNUAL, dia("2026-08-10")))
    }

    @Test
    fun `el fin sugerido cuenta las semanas de esa forma de periodo`() {
        val inicio = dia("2026-08-10")
        assertEquals(inicio.plusWeeks(16), AcademicTerm.suggestedPlannedEnd(AcademicTermType.SEMESTER, inicio))
        assertEquals(inicio.plusWeeks(11), AcademicTerm.suggestedPlannedEnd(AcademicTermType.TRIMESTER, inicio))
    }

    @Test
    fun `un periodo recien creado no tiene fecha de cierre`() {
        assertNull(periodo().closedEpochDay)
        assertTrue(periodo().isActive)
    }
}
