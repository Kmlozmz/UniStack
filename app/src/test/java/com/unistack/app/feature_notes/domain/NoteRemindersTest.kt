package com.unistack.app.feature_notes.domain

import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import com.unistack.app.TextosDePrueba
import org.junit.Before

/**
 * Como se lee un recordatorio.
 *
 * Los dos dias de al lado se dicen por su nombre porque son los que se miran de verdad: leer «14
 * de septiembre» obliga a comprobar que dia es hoy antes de saber si eso es manana.
 */
class NoteRemindersTest {

    @Before
    fun instalarTextos() {
        TextosDePrueba.instalar()
    }

    private val zona: ZoneId = ZoneId.of("America/Bogota")
    private val ahora: LocalDateTime = LocalDateTime.of(2026, 8, 29, 10, 30)

    private fun momento(anio: Int, mes: Int, dia: Int, hora: Int, minuto: Int): Long =
        LocalDateTime.of(anio, mes, dia, hora, minuto).atZone(zona).toInstant().toEpochMilli()

    @Test
    fun hoyYMananaSeDicenPorSuNombre() {
        assertEquals("Hoy 18:00", NoteReminders.label(momento(2026, 8, 29, 18, 0), ahora, zona))
        assertEquals("Mañana 7:15", NoteReminders.label(momento(2026, 8, 30, 7, 15), ahora, zona))
        assertEquals("Ayer 9:00", NoteReminders.label(momento(2026, 8, 28, 9, 0), ahora, zona))
    }

    @Test
    fun elRestoLlevaFecha() {
        val etiqueta = NoteReminders.label(momento(2026, 9, 14, 7, 0), ahora, zona)
        assertTrue(etiqueta, etiqueta.contains("14"))
        assertTrue(etiqueta, etiqueta.contains("7:00"))
    }

    /** Del mismo ano no hace falta decir el ano; de otro, si. */
    @Test
    fun elAnoSoloSaleCuandoNoEsElDeHoy() {
        assertFalse(NoteReminders.label(momento(2026, 9, 14, 7, 0), ahora, zona).contains("2026"))
        assertTrue(NoteReminders.label(momento(2027, 1, 5, 7, 0), ahora, zona).contains("2027"))
    }

    @Test
    fun unRecordatorioPasadoEstaVencido() {
        assertTrue(NoteReminders.isDue(1_000L, now = 2_000L))
        assertFalse(NoteReminders.isDue(3_000L, now = 2_000L))
    }

    /** Justo en el minuto tambien cuenta: si no, el aviso de ahora mismo se veria como futuro. */
    @Test
    fun elMinutoExactoYaCuenta() {
        assertTrue(NoteReminders.isDue(2_000L, now = 2_000L))
    }

    @Test
    fun soloSalenLasQueAvisanYLaMasProximaPrimero() {
        val notas = listOf(
            QuickNote(id = "sin", body = "x", createdAt = 0, updatedAt = 0),
            QuickNote(id = "tarde", body = "x", reminderAt = 9_000L, createdAt = 0, updatedAt = 0),
            QuickNote(id = "pronto", body = "x", reminderAt = 3_000L, createdAt = 0, updatedAt = 0)
        )
        assertEquals(listOf("pronto", "tarde"), NoteReminders.upcoming(notas).map { it.id })
    }

    /**
     * El momento de partida es manana a las ocho.
     *
     * Poner la hora actual haria que el aviso saltara mientras se escribe la nota, y «dentro de
     * una hora» obliga a hacer la cuenta para saber que hora es esa.
     */
    @Test
    fun elMomentoDePartidaEsMananaAlasOcho() {
        val destino = NoteReminders.defaultMoment(ahora)
        assertEquals(ahora.toLocalDate().plusDays(1), destino.toLocalDate())
        assertEquals(8, destino.hour)
        assertEquals(0, destino.minute)
    }
}
