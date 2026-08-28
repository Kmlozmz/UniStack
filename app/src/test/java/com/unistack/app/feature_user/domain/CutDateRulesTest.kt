package com.unistack.app.feature_user.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Las reglas de las fechas de corte, que ahora se aplican en dos pantallas.
 *
 * Estaban escritas dentro del onboarding y al hacerlas editables desde Ajustes habrían tenido
 * que existir dos veces. Estas pruebas fijan el comportamiento que ya tenía el onboarding, para
 * que compartirlas no lo cambie.
 */
class CutDateRulesTest {
    private val inicio = LocalDate.of(2026, 8, 24)
    private val fin = LocalDate.of(2026, 12, 12)

    @Test
    fun `sin fechas puestas no hay problema`() {
        assertNull(CutDateRules.problemFor(listOf(null, null), cutCount = 3, termStart = inicio, termPlannedEnd = fin))
    }

    @Test
    fun `un solo corte no tiene fechas que poner`() {
        assertNull(CutDateRules.problemFor(emptyList(), cutCount = 1))
        assertTrue(CutDateRules.areSound(emptyList(), cutCount = 1))
    }

    @Test
    fun `fechas completas y en orden se pueden guardar`() {
        val fechas = listOf(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 11, 6))
        assertTrue(CutDateRules.areSound(fechas, cutCount = 3, termStart = inicio, termPlannedEnd = fin))
    }

    @Test
    fun `media tabla no vale`() {
        val fechas = listOf(LocalDate.of(2026, 9, 30), null)
        assertEquals(
            CutDateProblem.INCOMPLETAS,
            CutDateRules.problemFor(fechas, cutCount = 3, termStart = inicio, termPlannedEnd = fin)
        )
    }

    @Test
    fun `una fecha que no avanza deja al corte sin dias`() {
        val fechas = listOf(LocalDate.of(2026, 11, 6), LocalDate.of(2026, 9, 30))
        assertEquals(
            CutDateProblem.DESORDENADAS,
            CutDateRules.problemFor(fechas, cutCount = 3, termStart = inicio, termPlannedEnd = fin)
        )
    }

    @Test
    fun `dos cortes que acaban el mismo dia tampoco`() {
        val mismo = LocalDate.of(2026, 9, 30)
        assertEquals(
            CutDateProblem.DESORDENADAS,
            CutDateRules.problemFor(listOf(mismo, mismo), cutCount = 3, termStart = inicio, termPlannedEnd = fin)
        )
    }

    @Test
    fun `la primera no puede caer antes del inicio del periodo`() {
        val fechas = listOf(inicio.minusDays(1), LocalDate.of(2026, 11, 6))
        assertEquals(
            CutDateProblem.ANTES_DEL_INICIO,
            CutDateRules.problemFor(fechas, cutCount = 3, termStart = inicio, termPlannedEnd = fin)
        )
    }

    @Test
    fun `la ultima tiene que dejar sitio al corte final`() {
        val fechas = listOf(LocalDate.of(2026, 9, 30), fin)
        assertEquals(
            CutDateProblem.DESPUES_DEL_FINAL,
            CutDateRules.problemFor(fechas, cutCount = 3, termStart = inicio, termPlannedEnd = fin)
        )
    }

    @Test
    fun `sin periodo declarado solo se comprueba el orden`() {
        val fechas = listOf(LocalDate.of(2020, 1, 1), LocalDate.of(2030, 1, 1))
        assertTrue(CutDateRules.areSound(fechas, cutCount = 3))
    }

    @Test
    fun `las posiciones que sobran no se miran`() {
        // La cuarta fecha es basura de un esquema anterior: con tres cortes solo cuentan dos.
        val fechas = listOf(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 11, 6), LocalDate.of(2020, 1, 1))
        assertTrue(CutDateRules.areSound(fechas, cutCount = 3, termStart = inicio, termPlannedEnd = fin))
    }

    @Test
    fun `subir de cortes conserva las fechas ya puestas`() {
        val fechas = listOf(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 11, 6))
        val ampliada = CutDateRules.resize(fechas, cutCount = 4)
        assertEquals(3, ampliada.size)
        assertEquals(fechas[0], ampliada[0])
        assertEquals(fechas[1], ampliada[1])
        assertNull(ampliada[2])
    }

    @Test
    fun `bajar de cortes recorta por el final`() {
        val fechas = listOf(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 11, 6))
        val recortada = CutDateRules.resize(fechas, cutCount = 2)
        assertEquals(1, recortada.size)
        assertEquals(fechas[0], recortada[0])
    }

    @Test
    fun `un solo corte se queda sin casillas`() {
        val fechas = listOf(LocalDate.of(2026, 9, 30))
        assertTrue(CutDateRules.resize(fechas, cutCount = 1).isEmpty())
    }

    @Test
    fun `el esquema con fechas a medias no es valido`() {
        val esquema = GradingCutScheme(
            cuts = listOf(
                GradingCut("period-1", "Corte 1", 0.3, 1, LocalDate.of(2026, 9, 30).toEpochDay()),
                GradingCut("period-2", "Corte 2", 0.4, 2, null),
                GradingCut("period-3", "Corte 3", 0.3, 3, null)
            )
        )
        assertFalse(esquema.isValid)
    }

    @Test
    fun `el esquema con todas las fechas puestas es valido`() {
        val esquema = GradingCutScheme(
            cuts = listOf(
                GradingCut("period-1", "Corte 1", 0.3, 1, LocalDate.of(2026, 9, 30).toEpochDay()),
                GradingCut("period-2", "Corte 2", 0.4, 2, LocalDate.of(2026, 11, 6).toEpochDay()),
                GradingCut("period-3", "Corte 3", 0.3, 3, null)
            )
        )
        assertTrue(esquema.isValid)
    }
}
