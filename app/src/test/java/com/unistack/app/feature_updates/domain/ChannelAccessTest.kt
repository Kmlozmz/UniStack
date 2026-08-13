package com.unistack.app.feature_updates.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChannelAccessTest {

    private val alpha = ChannelAccess.AccessEntry(
        channel = UpdateChannel.ALPHA,
        fingerprint = ChannelAccess.fingerprint("3KWVQ-B5LQ7-6W8RY-B7NRT")
    )
    private val beta = ChannelAccess.AccessEntry(
        channel = UpdateChannel.BETA,
        fingerprint = ChannelAccess.fingerprint("QTZBF-F9KMG-V54XW-EX2H2")
    )
    private val lista = listOf(alpha, beta)

    @Test
    fun `el codigo de alpha no abre beta`() {
        // Eran una escalera y el de alpha abria los dos. Son publicos distintos: cada codigo
        // abre el suyo y solo el suyo.
        assertEquals(
            UpdateChannel.ALPHA,
            ChannelAccess.channelFor(ChannelAccess.fingerprint("3KWVQ-B5LQ7-6W8RY-B7NRT"), lista)
        )
        assertNull(
            ChannelAccess.channelFor(ChannelAccess.fingerprint("3KWVQ-B5LQ7-6W8RY-B7NRT"), listOf(beta))
        )
    }

    @Test
    fun `un codigo abre el canal que le corresponde`() {
        assertEquals(
            UpdateChannel.ALPHA,
            ChannelAccess.channelFor(ChannelAccess.fingerprint("3KWVQ-B5LQ7-6W8RY-B7NRT"), lista)
        )
        assertEquals(
            UpdateChannel.BETA,
            ChannelAccess.channelFor(ChannelAccess.fingerprint("QTZBF-F9KMG-V54XW-EX2H2"), lista)
        )
    }

    @Test
    fun `da igual como se escriba el codigo`() {
        // Se dicta en voz alta y se copia a mano: ni los guiones ni las mayúsculas deberían
        // decidir si funciona.
        val esperado = ChannelAccess.fingerprint("3KWVQ-B5LQ7-6W8RY-B7NRT")
        assertEquals(esperado, ChannelAccess.fingerprint("3kwvqb5lq76w8ryb7nrt"))
        assertEquals(esperado, ChannelAccess.fingerprint("  3KWVQ-B5LQ7-6W8RY-B7NRT  "))
    }

    @Test
    fun `un codigo que no esta en la lista no abre nada`() {
        assertNull(ChannelAccess.channelFor(ChannelAccess.fingerprint("XXXXX-XXXXX-XXXXX-XXXXX"), lista))
    }

    @Test
    fun `quitar la linea de la lista revoca el acceso`() {
        // Es lo que hace que gestionar el archivo sirva de algo: la app vuelve a contrastar su
        // huella en cada comprobación, y si ya no está, se queda sin el canal.
        val huella = ChannelAccess.fingerprint("3KWVQ-B5LQ7-6W8RY-B7NRT")
        assertEquals(UpdateChannel.ALPHA, ChannelAccess.channelFor(huella, lista))
        assertNull(ChannelAccess.channelFor(huella, lista - alpha))
    }

    @Test
    fun `la lista publicada no permite recuperar el codigo`() {
        // La huella es lo único que se publica, y no se parece al código que la genera.
        val codigo = "3KWVQ-B5LQ7-6W8RY-B7NRT"
        assertNotEquals(codigo, ChannelAccess.fingerprint(codigo))
        assertEquals(64, ChannelAccess.fingerprint(codigo).length)
    }
}
