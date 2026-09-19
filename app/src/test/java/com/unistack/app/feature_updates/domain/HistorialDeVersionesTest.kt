package com.unistack.app.feature_updates.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * El historial es lo que este teléfono ha ejecutado, la actual primero.
 *
 * Una versión entra al arrancar con ella puesta; volver a arrancar con la misma no añade
 * nada; y cada una dura hasta que se instaló la siguiente.
 */
class HistorialDeVersionesTest {

    private val dia = 24 * 60 * 60 * 1000L

    @Test
    fun `arrancar con una version nueva la pone en cabeza`() {
        val antes = listOf(InstalledVersion("1.0.0", 10 * dia))
        val despues = HistorialDeVersiones.registrar(antes, "1.0.1", 13 * dia)
        assertEquals(listOf("1.0.1", "1.0.0"), despues.map { it.versionName })
    }

    @Test
    fun `arrancar otra vez con la misma version no cambia nada`() {
        val antes = listOf(InstalledVersion("1.0.1", 13 * dia), InstalledVersion("1.0.0", 10 * dia))
        assertSame(antes, HistorialDeVersiones.registrar(antes, "1.0.1", 20 * dia))
    }

    @Test
    fun `reinstalar una vieja la sube sin duplicarla`() {
        val antes = listOf(InstalledVersion("1.0.1", 13 * dia), InstalledVersion("1.0.0", 10 * dia))
        val despues = HistorialDeVersiones.registrar(antes, "1.0.0", 15 * dia)
        assertEquals(listOf("1.0.0", "1.0.1"), despues.map { it.versionName })
        assertEquals(15 * dia, despues.first().installedAtMillis)
    }

    @Test
    fun `cada version dura hasta la siguiente y la actual no se cuenta`() {
        val historial = listOf(
            InstalledVersion("1.0.1", 13 * dia),
            InstalledVersion("1.0.0", 10 * dia),
            InstalledVersion("0.9.1", 3 * dia)
        )
        assertNull(HistorialDeVersiones.diasInstalada(historial, 0))
        assertEquals(3, HistorialDeVersiones.diasInstalada(historial, 1))
        assertEquals(7, HistorialDeVersiones.diasInstalada(historial, 2))
    }

    @Test
    fun `menos de un dia cuenta como uno`() {
        val historial = listOf(InstalledVersion("1.0.1", 10 * dia + 5000L), InstalledVersion("1.0.0", 10 * dia))
        assertEquals(1, HistorialDeVersiones.diasInstalada(historial, 1))
    }
}
