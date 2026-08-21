package com.unistack.app.feature_support.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupportTicketTest {

    private val contexto = TicketContext(
        appVersion = "1.1.0-alpha.1",
        androidVersion = "14",
        androidSdk = 34,
        device = "Pixel 8"
    )

    @Test
    fun `el ticket lleva el motivo, el texto y de donde sale`() {
        val ticket = buildTicket(
            TicketKind.BUG,
            "El horario no muestra la clase del sábado.",
            contact = null,
            context = contexto
        )

        assertTrue(ticket.startsWith("🐞 Fallo"))
        assertTrue(ticket.contains("El horario no muestra la clase del sábado."))
        assertTrue(ticket.contains("UniStack 1.1.0-alpha.1"))
        assertTrue(ticket.contains("Android 14 (SDK 34) · Pixel 8"))
    }

    @Test
    fun `los espacios de mas no viajan`() {
        val ticket = buildTicket(
            TicketKind.IDEA,
            "   Poder exportar el horario.\n\n  ",
            contact = null,
            context = contexto
        )

        assertTrue(ticket.contains("Poder exportar el horario."))
        assertTrue("no se cuelan líneas en blanco al final", !ticket.contains("\n\n\n"))
    }

    @Test
    fun `el contacto solo aparece si se escribio`() {
        // Es opcional de verdad: sin él, el ticket no debe llevar una línea vacía esperando
        // a que alguien la rellene a mano.
        val sin = buildTicket(TicketKind.BUG, "algo", contact = null, context = contexto)
        val vacio = buildTicket(TicketKind.BUG, "algo", contact = "   ", context = contexto)
        val con = buildTicket(TicketKind.BUG, "algo", contact = " @kmlo ", context = contexto)

        assertFalse(sin.contains("Contacto"))
        assertFalse(vacio.contains("Contacto"))
        assertTrue(con.contains("Contacto: @kmlo"))
    }

    @Test
    fun `cada motivo abre su propio tema`() {
        assertEquals("https://t.me/unistacksoporte/2", SupportChannel.webUrlFor(TicketKind.BUG))
        assertEquals("https://t.me/unistacksoporte/3", SupportChannel.webUrlFor(TicketKind.IDEA))
    }

    @Test
    fun `lo que no es fallo ni idea cae en el grupo, no en un tema ajeno`() {
        // Mandarlo a Fallos o a Sugerencias ensuciaría las dos listas que existen justamente
        // para separar; sin tema propio, el sitio correcto es el general del grupo.
        assertEquals(null, SupportChannel.topicFor(TicketKind.OTHER))
        assertEquals("https://t.me/unistacksoporte", SupportChannel.webUrlFor(TicketKind.OTHER))
        assertFalse(SupportChannel.appUriFor(TicketKind.OTHER).contains("thread"))
    }

    @Test
    fun `el enlace de la app no pasa por el dominio`() {
        // Es lo que evita el error de DNS con una VPN de por medio: tg:// va a la app
        // instalada sin resolver t.me ni abrir el navegador.
        val uri = SupportChannel.appUriFor(TicketKind.BUG)

        assertTrue(uri.startsWith("tg://"))
        assertTrue(uri.contains("domain=unistacksoporte"))
        assertTrue(uri.contains("thread=2"))
    }

    @Test
    fun `una sugerencia se distingue de un fallo a primera vista`() {
        val fallo = buildTicket(TicketKind.BUG, "algo", contact = null, context = contexto)
        val idea = buildTicket(TicketKind.IDEA, "algo", contact = null, context = contexto)
        val otro = buildTicket(TicketKind.OTHER, "algo", contact = null, context = contexto)

        assertTrue(fallo.lineSequence().first().contains("Fallo"))
        assertTrue(idea.lineSequence().first().contains("Sugerencia"))
        assertTrue(otro.lineSequence().first().contains("Otro"))
    }
}
