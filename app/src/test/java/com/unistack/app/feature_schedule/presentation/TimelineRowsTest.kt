package com.unistack.app.feature_schedule.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimelineRowsTest {

    private fun clase(desde: String, hasta: String): IntRange {
        fun minutos(hhmm: String): Int {
            val (h, m) = hhmm.split(":").map(String::toInt)
            return h * 60 + m
        }
        return minutos(desde)..minutos(hasta)
    }

    private fun horas(rows: List<TimelineRow>) =
        rows.filterIsInstance<TimelineRow.Hour>().map { it.hour }

    @Test
    fun `un dia repartido cabe entero`() {
        // El caso que motivó todo esto: con una ventana de siete horas, la clase de la tarde no
        // se veía por ninguna parte.
        val rows = buildTimelineRows(listOf(clase("6:30", "9:30"), clase("15:30", "18:30")))

        assertEquals(listOf(6, 7, 8, 9, 15, 16, 17, 18), horas(rows))
        assertEquals(1, rows.filterIsInstance<TimelineRow.Break>().size)
    }

    @Test
    fun `una clase de madrugada ya no esconde las demas`() {
        val rows = buildTimelineRows(listOf(clase("1:00", "3:00"), clase("10:00", "12:00")))

        assertTrue("la de madrugada se ve", horas(rows).containsAll(listOf(1, 2)))
        assertTrue("y la de la mañana también", horas(rows).containsAll(listOf(10, 11)))
    }

    @Test
    fun `un dia seguido no se pliega`() {
        val rows = buildTimelineRows(listOf(clase("8:00", "10:00"), clase("10:00", "13:00")))

        assertEquals(emptyList<TimelineRow.Break>(), rows.filterIsInstance<TimelineRow.Break>())
        assertEquals(listOf(8, 9, 10, 11, 12), horas(rows))
    }

    @Test
    fun `un hueco de una sola hora se deja tal cual`() {
        // Plegar ahí no ahorra sitio y rompe el eje sin motivo.
        val rows = buildTimelineRows(listOf(clase("8:00", "9:00"), clase("10:00", "11:00")))

        assertEquals(emptyList<TimelineRow.Break>(), rows.filterIsInstance<TimelineRow.Break>())
        assertTrue("la hora vacía sigue ahí", 9 in horas(rows))
    }

    @Test
    fun `una clase suelta no deja la rejilla en una tira`() {
        val rows = buildTimelineRows(listOf(clase("8:00", "9:00")))

        assertTrue("hay aire alrededor", horas(rows).size >= 5)
        assertTrue("y la clase está dentro", 8 in horas(rows))
    }

    @Test
    fun `sin clases se enseña la franja de siempre`() {
        val rows = buildTimelineRows(emptyList())

        assertEquals(6, horas(rows).first())
        assertTrue(horas(rows).size >= 5)
    }

    @Test
    fun `una clase que acaba en punto no ocupa la hora siguiente`() {
        val rows = buildTimelineRows(listOf(clase("8:00", "9:00")))

        assertTrue("las 8 sí", 8 in horas(rows))
        // Las 9 pueden aparecer como aire, pero no porque la clase las ocupe.
        assertEquals(8, horas(rows).first())
    }

    @Test
    fun `la altura de un minuto salta los tramos plegados`() {
        val rows = buildTimelineRows(listOf(clase("6:00", "7:00"), clase("15:00", "16:00")))
        val hourHeight = 36f
        val breakHeight = 14f

        val inicioManana = offsetForMinute(rows, 6 * 60, hourHeight, breakHeight)
        val inicioTarde = offsetForMinute(rows, 15 * 60, hourHeight, breakHeight)

        assertNotNull(inicioManana)
        assertNotNull(inicioTarde)
        // Sin plegar, entre las 6:00 y las 15:00 habría nueve horas de separación.
        assertTrue(
            "el corte tiene que acortar la distancia",
            inicioTarde!! - inicioManana!! < 9 * hourHeight
        )
    }

    @Test
    fun `un minuto dentro de un tramo plegado no tiene altura`() {
        val rows = buildTimelineRows(listOf(clase("6:00", "7:00"), clase("15:00", "16:00")))

        assertNull(offsetForMinute(rows, 11 * 60, 36f, 14f))
    }

    @Test
    fun `el alto de una clase sigue siendo proporcional a su duracion`() {
        val rows = buildTimelineRows(listOf(clase("8:00", "11:00")))
        val inicio = offsetForMinute(rows, 8 * 60, 36f, 14f)!!
        val fin = offsetForMinute(rows, 11 * 60, 36f, 14f)!!

        assertEquals(3 * 36f, fin - inicio, 0.01f)
    }

    @Test
    fun `la madrugada se considera hora inusual y el resto del dia no`() {
        assertTrue(com.unistack.app.feature_grades.presentation.isUnusualClassHour(60))
        assertTrue(com.unistack.app.feature_grades.presentation.isUnusualClassHour(5 * 60 + 59))
        assertFalse(com.unistack.app.feature_grades.presentation.isUnusualClassHour(6 * 60))
        assertFalse(com.unistack.app.feature_grades.presentation.isUnusualClassHour(13 * 60))
        assertFalse(com.unistack.app.feature_grades.presentation.isUnusualClassHour(22 * 60))
    }
}
