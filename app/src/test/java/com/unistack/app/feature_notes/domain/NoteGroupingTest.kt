package com.unistack.app.feature_notes.domain

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El reparto de las notas en la lista.
 *
 * Se prueba con una zona horaria fija para que el resultado no dependa de donde se ejecute:
 * una nota escrita a las once y media de la noche cae en un dia u otro segun la zona, y eso es
 * exactamente lo que hay que dejar clavado.
 */
class NoteGroupingTest {

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
    fun cadaDiaJuntaLoSuyoYLosDiasVanDelMasNuevoAlMasViejo() {
        val notas = listOf(
            nota("a", hoy),
            nota("b", hoy.minusDays(1)),
            nota("c", hoy),
            nota("d", hoy.minusDays(4))
        )

        val dias = NoteGrouping.byDay(notas, hoy, zone)

        assertEquals(3, dias.size)
        assertEquals(hoy, dias[0].date)
        assertEquals(2, dias[0].notes.size)
        assertEquals(hoy.minusDays(1), dias[1].date)
        assertEquals(hoy.minusDays(4), dias[2].date)
    }

    /**
     * Dentro de un dia, lo ultimo que se toco va arriba.
     *
     * Es lo que hace que volver a una nota para anadirle algo la devuelva al sitio donde se
     * espera encontrarla, en vez de dejarla donde estaba cuando se escribio.
     */
    @Test
    fun dentroDeUnDiaMandaLaHoraDelUltimoCambio() {
        val notas = listOf(
            nota("manana", hoy, LocalTime.of(8, 0)),
            nota("tarde", hoy, LocalTime.of(19, 30))
        )

        val dia = NoteGrouping.byDay(notas, hoy, zone).single()

        assertEquals(listOf("tarde", "manana"), dia.notes.map { it.id })
    }

    /**
     * Una nota fijada se sale del tiempo.
     *
     * Si se quedara en su dia, al cabo de una semana estaria enterrada bajo lo nuevo, que es lo
     * contrario de lo que se pide al fijarla.
     */
    @Test
    fun lasFijadasSalenAparteYNoRepitenEnSuDia() {
        val notas = listOf(
            nota("fija", hoy.minusDays(9), pinned = true),
            nota("suelta", hoy)
        )

        assertEquals(listOf("fija"), NoteGrouping.pinned(notas).map { it.id })
        val dias = NoteGrouping.byDay(notas, hoy, zone)
        assertEquals(1, dias.size)
        assertEquals(listOf("suelta"), dias.single().notes.map { it.id })
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

    @Test
    fun sinNotasNoHayDias() {
        assertTrue(NoteGrouping.byDay(emptyList(), hoy, zone).isEmpty())
    }
}
