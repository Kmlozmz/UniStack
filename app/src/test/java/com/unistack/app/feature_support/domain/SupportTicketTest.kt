package com.unistack.app.feature_support.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportTicketTest {

    private val contexto = TicketContext(
        appVersion = "1.1.0-alpha.1",
        androidVersion = "14",
        device = "Pixel 8"
    )

    @Test
    fun `el ticket lleva el motivo, el texto y de donde sale`() {
        val ticket = buildTicket(TicketKind.BUG, "El horario no muestra la clase del sábado.", contexto)

        assertTrue(ticket.startsWith("🐞 Fallo"))
        assertTrue(ticket.contains("El horario no muestra la clase del sábado."))
        assertTrue(ticket.contains("UniStack 1.1.0-alpha.1"))
        assertTrue(ticket.contains("Android 14 · Pixel 8"))
    }

    @Test
    fun `los espacios de mas no viajan`() {
        val ticket = buildTicket(TicketKind.IDEA, "   Poder exportar el horario.\n\n  ", contexto)

        assertTrue(ticket.contains("Poder exportar el horario."))
        assertTrue("no se cuelan líneas en blanco al final", !ticket.contains("\n\n\n"))
    }

    @Test
    fun `cada motivo abre su propio tema`() {
        assertEquals("https://t.me/unistacksoporte/2", SupportChannel.topicFor(TicketKind.BUG))
        assertEquals("https://t.me/unistacksoporte/3", SupportChannel.topicFor(TicketKind.IDEA))
    }

    @Test
    fun `una sugerencia se distingue de un fallo a primera vista`() {
        val fallo = buildTicket(TicketKind.BUG, "algo", contexto)
        val idea = buildTicket(TicketKind.IDEA, "algo", contexto)

        assertTrue(fallo.lineSequence().first().contains("Fallo"))
        assertTrue(idea.lineSequence().first().contains("Sugerencia"))
    }
}
