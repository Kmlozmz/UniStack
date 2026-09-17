package com.unistack.app.feature_notes.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.unistack.app.TextosDePrueba
import org.junit.Before

/**
 * El reparto de las notas en la lista.
 *
 * Se prueba con una zona horaria fija para que el resultado no dependa de donde se ejecute:
 * una nota escrita a las once y media de la noche cae en un dia u otro segun la zona, y eso es
 * exactamente lo que hay que dejar clavado.
 */
class NoteGroupingTest {
    @Before
    fun instalarTextos() {
        TextosDePrueba.instalar()
    }

    private val zone: ZoneId = ZoneId.of("America/Bogota")
    private val hoy: LocalDate = LocalDate.of(2026, 8, 28)

    private fun nota(
        id: String,
        dia: LocalDate,
        hora: LocalTime = LocalTime.of(10, 0),
        pinned: Boolean = false,
        subjectId: String? = null
    ): QuickNote {
        val millis = dia.atTime(hora).atZone(zone).toInstant().toEpochMilli()
        return QuickNote(
            id = id,
            body = "Nota " + id,
            subjectId = subjectId,
            pinned = pinned,
            createdAt = millis,
            updatedAt = millis
        )
    }

    @Test
    fun losDosDiasQueSeMiranTienenNombrePropio() {
        assertEquals("Hoy", NoteGrouping.dayLabel(hoy, hoy))
        assertEquals("Ayer", NoteGrouping.dayLabel(hoy.minusDays(1), hoy))
    }

    /** Del mismo ano no hace falta decir el ano; de otro, si. */
    @Test
    fun elAnoSoloSeDiceCuandoNoEsElDeHoy() {
        val esteAno = NoteGrouping.dayLabel(LocalDate.of(2026, 3, 4), hoy)
        val otroAno = NoteGrouping.dayLabel(LocalDate.of(2025, 3, 4), hoy)

        assertTrue(esteAno, !esteAno.contains("2026"))
        assertTrue(otroAno, otroAno.contains("2025"))
    }

    @Test
    fun laCabeceraEmpiezaEnMayuscula() {
        val etiqueta = NoteGrouping.dayLabel(LocalDate.of(2026, 3, 4), hoy)
        assertEquals(etiqueta.first().uppercaseChar(), etiqueta.first())
    }

    @Test
    fun laHoraRespetaElFormatoDeVeinticuatro() {
        val tarde = nota("t", hoy, LocalTime.of(19, 5))
        assertEquals("19:05", NoteGrouping.timeLabel(tarde, use24Hour = true, zone = zone))
        assertTrue(NoteGrouping.timeLabel(tarde, use24Hour = false, zone = zone).startsWith("7:05"))
    }
}
