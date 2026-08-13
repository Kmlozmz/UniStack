package com.unistack.app.feature_updates.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChannelAccessTest {

    private val alpha = ChannelAccess.AccessEntry(
        channel = UpdateChannel.ALPHA,
        fingerprint = ChannelAccess.fingerprint("BB2CM-KZK22-AKCN2-2WGC9")
    )
    private val beta = ChannelAccess.AccessEntry(
        channel = UpdateChannel.BETA,
        fingerprint = ChannelAccess.fingerprint("LAB46-52J77-XFXTU-TM6FS")
    )
    private val lista = listOf(alpha, beta)

    @Test
    fun `un codigo abre el canal que le corresponde`() {
        assertEquals(
            UpdateChannel.ALPHA,
            ChannelAccess.channelFor(ChannelAccess.fingerprint("BB2CM-KZK22-AKCN2-2WGC9"), lista)
        )
        assertEquals(
            UpdateChannel.BETA,
            ChannelAccess.channelFor(ChannelAccess.fingerprint("LAB46-52J77-XFXTU-TM6FS"), lista)
        )
    }

    @Test
    fun `da igual como se escriba el codigo`() {
        // Se dicta en voz alta y se copia a mano: ni los guiones ni las mayúsculas deberían
        // decidir si funciona.
        val esperado = ChannelAccess.fingerprint("BB2CM-KZK22-AKCN2-2WGC9")
        assertEquals(esperado, ChannelAccess.fingerprint("bb2cmkzk22akcn22wgc9"))
        assertEquals(esperado, ChannelAccess.fingerprint("  BB2CM-KZK22-AKCN2-2WGC9  "))
    }

    @Test
    fun `un codigo que no esta en la lista no abre nada`() {
        assertNull(ChannelAccess.channelFor(ChannelAccess.fingerprint("XXXXX-XXXXX-XXXXX-XXXXX"), lista))
    }

    @Test
    fun `quitar la linea de la lista revoca el acceso`() {
        // Es lo que hace que gestionar el archivo sirva de algo: la app vuelve a contrastar su
        // huella en cada comprobación, y si ya no está, se queda sin el canal.
        val huella = ChannelAccess.fingerprint("BB2CM-KZK22-AKCN2-2WGC9")
        assertEquals(UpdateChannel.ALPHA, ChannelAccess.channelFor(huella, lista))
        assertNull(ChannelAccess.channelFor(huella, lista - alpha))
    }

    @Test
    fun `la lista publicada no permite recuperar el codigo`() {
        // La huella es lo único que se publica, y no se parece al código que la genera.
        val codigo = "BB2CM-KZK22-AKCN2-2WGC9"
        assertNotEquals(codigo, ChannelAccess.fingerprint(codigo))
        assertEquals(64, ChannelAccess.fingerprint(codigo).length)
    }
}
