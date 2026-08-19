package com.unistack.app.feature_updates.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateChannelTest {

    @Test
    fun `el canal estable solo acepta versiones terminadas`() {
        assertTrue(UpdateChannel.STABLE.accepts("1.0.0"))
        assertFalse(UpdateChannel.STABLE.accepts("1.0.0-alpha.1"))
        assertFalse(UpdateChannel.STABLE.accepts("1.0.0-beta.1"))
        assertFalse(UpdateChannel.STABLE.accepts("1.0.0-rc.1"))
    }

    @Test
    fun `alpha y beta son publicos distintos, no escalones`() {
        // Era una escalera, y por eso un solo codigo abria los dos canales.
        assertFalse(UpdateChannel.ALPHA.accepts("1.0.0-beta.1"))
        assertFalse(UpdateChannel.BETA.accepts("1.0.0-alpha.1"))
    }

    @Test
    fun `cada canal recibe lo suyo`() {
        assertTrue(UpdateChannel.ALPHA.accepts("1.0.0-alpha.3"))
        assertTrue(UpdateChannel.BETA.accepts("1.0.0-beta.1"))
        assertTrue(UpdateChannel.BETA.accepts("1.0.0-rc.1"))
    }

    @Test
    fun `la version definitiva llega a todos los canales`() {
        // Es la salida de cualquier preestreno: sin esto, quien prueba una alpha se queda
        // anclado en ella para siempre.
        UpdateChannel.entries.forEach { canal ->
            assertTrue(canal.name, canal.accepts("1.0.0"))
        }
    }

    @Test
    fun `la version instalada abre su propio canal`() {
        // Quien ya lleva una beta no tiene que pedir permiso para recibir betas: el codigo es
        // para entrar, y ya esta dentro.
        assertEquals(UpdateChannel.BETA, UpdateChannel.ofInstalled("1.2.0-beta.1"))
        assertEquals(UpdateChannel.ALPHA, UpdateChannel.ofInstalled("1.2.0-alpha.3"))
    }

    @Test
    fun `una candidata cuenta como beta tambien para abrir el canal`() {
        assertEquals(UpdateChannel.BETA, UpdateChannel.ofInstalled("1.2.0-rc.1"))
    }

    @Test
    fun `una definitiva no abre ningun canal`() {
        // No hace falta: la definitiva la recibe todo el mundo desde el canal estable.
        assertNull(UpdateChannel.ofInstalled("1.2.0"))
    }

    @Test
    fun `una compilacion local abre el canal alpha`() {
        // «dev» no es un canal, y su sufijo cae del lado del circulo mas pequeno.
        assertEquals(UpdateChannel.ALPHA, UpdateChannel.ofInstalled("0.0.0-dev.42"))
    }

    @Test
    fun `un sufijo desconocido se trata como alpha`() {
        // El circulo mas pequeno, para no colarlo donde hay mas gente.
        assertTrue(UpdateChannel.ALPHA.accepts("1.0.0-loquesea"))
        assertFalse(UpdateChannel.BETA.accepts("1.0.0-loquesea"))
        assertFalse(UpdateChannel.STABLE.accepts("1.0.0-loquesea"))
    }
}
