package com.unistack.app.feature_user.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Elegir el corte de una nota por su fecha.
 *
 * Los cortes son contiguos por construcción: se guarda solo el día en que cierra cada uno, y
 * por eso los solapes y los huecos no pueden existir. Estas pruebas fijan esa promesa, porque
 * es lo que permite dejar de preguntar el corte al registrar una nota.
 */
class GradingCutSchemeDatesTest {

    private fun dia(texto: String) = LocalDate.parse(texto)

    /** Tres cortes: cierran el 20 sep y el 31 oct; el tercero acaba con el periodo. */
    private fun conFechas() = GradingCutScheme(
        listOf(
            GradingCut("period-1", "Corte 1", 0.30, 1, dia("2026-09-20").toEpochDay()),
            GradingCut("period-2", "Corte 2", 0.40, 2, dia("2026-10-31").toEpochDay()),
            GradingCut("period-3", "Corte 3", 0.30, 3, null)
        )
    )

    private fun sinFechas() = GradingCutScheme.default()

    // --- Elegir por fecha ---

    @Test
    fun `una nota de octubre cae en el corte 2`() {
        assertEquals("period-2", conFechas().cutForDate(dia("2026-10-02"))?.id)
    }

    @Test
    fun `el dia de cierre pertenece al corte que cierra, no al siguiente`() {
        assertEquals("period-1", conFechas().cutForDate(dia("2026-09-20"))?.id)
        assertEquals("period-2", conFechas().cutForDate(dia("2026-09-21"))?.id)
    }

    @Test
    fun `el ultimo corte recoge todo lo que venga despues`() {
        assertEquals("period-3", conFechas().cutForDate(dia("2026-11-20"))?.id)
        // Incluso pasado el fin previsto: el periodo sigue vivo hasta que se cierre.
        assertEquals("period-3", conFechas().cutForDate(dia("2027-02-01"))?.id)
    }

    @Test
    fun `una fecha anterior al primer cierre cae en el primer corte`() {
        assertEquals("period-1", conFechas().cutForDate(dia("2026-08-11"))?.id)
    }

    /**
     * Sin fechas no se adivina.
     *
     * Nulo aquí significa «pregúntaselo», no «no hay corte». El selector manual sigue
     * existiendo y es exactamente lo que aparece cuando esto no puede responder.
     */
    @Test
    fun `un esquema sin fechas no elige nada`() {
        assertNull(sinFechas().cutForDate(dia("2026-10-02")))
        assertFalse(sinFechas().hasDates)
    }

    @Test
    fun `un esquema con fechas se declara capaz de elegir`() {
        assertTrue(conFechas().hasDates)
    }

    @Test
    fun `un corte unico no necesita fechas para elegir`() {
        val uno = GradingCutScheme(listOf(GradingCut("period-1", "Corte 1", 1.0, 1, null)))
        assertTrue(uno.hasDates)
        assertEquals("period-1", uno.cutForDate(dia("2026-10-02"))?.id)
    }

    // --- Validez ---

    @Test
    fun `las fechas tienen que subir`() {
        val alReves = GradingCutScheme(
            listOf(
                GradingCut("period-1", "Corte 1", 0.5, 1, dia("2026-10-31").toEpochDay()),
                GradingCut("period-2", "Corte 2", 0.5, 2, dia("2026-09-20").toEpochDay()),
                GradingCut("period-3", "Corte 3", 0.0, 3, null)
            )
        )
        assertFalse(alReves.datesLookValid())
    }

    /**
     * El último no lleva fecha, y no es un descuido.
     *
     * Acaba cuando acaba el periodo. Dársela propia abriría un hueco entre su cierre y el del
     * periodo, que es justo lo que este diseño hace imposible.
     */
    @Test
    fun `el ultimo corte no puede llevar fecha propia`() {
        val conFechaFinal = GradingCutScheme(
            listOf(
                GradingCut("period-1", "Corte 1", 0.5, 1, dia("2026-09-20").toEpochDay()),
                GradingCut("period-2", "Corte 2", 0.5, 2, dia("2026-12-12").toEpochDay())
            )
        )
        assertFalse(conFechaFinal.isValid)
    }

    @Test
    fun `fechas a medias no valen`() {
        val aMedias = GradingCutScheme(
            listOf(
                GradingCut("period-1", "Corte 1", 0.3, 1, dia("2026-09-20").toEpochDay()),
                GradingCut("period-2", "Corte 2", 0.4, 2, null),
                GradingCut("period-3", "Corte 3", 0.3, 3, null)
            )
        )
        assertFalse(aMedias.isValid)
    }

    @Test
    fun `el esquema por defecto sigue siendo valido sin fechas`() {
        assertTrue(GradingCutScheme.default().isValid)
    }

    @Test
    fun `un esquema con fechas correctas es valido`() {
        assertTrue(conFechas().isValid)
    }

    /** Atajo para probar solo la parte de fechas, sin que los pesos enturbien el resultado. */
    private fun GradingCutScheme.datesLookValid(): Boolean =
        copy(cuts = cuts.mapIndexed { i, c -> c.copy(weight = 1.0 / cuts.size) }).isValid
}
