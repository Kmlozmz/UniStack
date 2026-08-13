package com.unistack.app.feature_updates.domain

import org.junit.Assert.assertFalse
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
    fun `beta acepta candidatas y definitivas, no alphas`() {
        assertTrue(UpdateChannel.BETA.accepts("1.0.0-beta.1"))
        assertTrue(UpdateChannel.BETA.accepts("1.0.0-rc.1"))
        assertTrue(UpdateChannel.BETA.accepts("1.0.0"))
        assertFalse(UpdateChannel.BETA.accepts("1.0.0-alpha.1"))
    }

    @Test
    fun `alpha acepta todo`() {
        UpdateChannel.entries.forEach { _ ->
            assertTrue(UpdateChannel.ALPHA.accepts("1.0.0-alpha.1"))
            assertTrue(UpdateChannel.ALPHA.accepts("1.0.0-beta.1"))
            assertTrue(UpdateChannel.ALPHA.accepts("1.0.0"))
        }
    }

    @Test
    fun `un canal es un suelo, no un filtro exclusivo`() {
        // Quien prueba alphas también tiene que recibir la definitiva que las sustituye: es la
        // versión buena de lo que estaba probando.
        assertTrue(UpdateChannel.ALPHA.accepts("1.0.0"))
        assertTrue(UpdateChannel.BETA.accepts("1.0.0"))
    }

    @Test
    fun `un sufijo desconocido se trata como lo mas inestable`() {
        // «dev» no se publica, pero si algo raro llegara a publicarse no debe colarse en el
        // canal tranquilo por no reconocerlo.
        assertFalse(UpdateChannel.STABLE.accepts("1.0.0-dev.26081311"))
        assertFalse(UpdateChannel.BETA.accepts("1.0.0-loquesea"))
        assertTrue(UpdateChannel.ALPHA.accepts("1.0.0-loquesea"))
    }
}
