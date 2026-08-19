package com.unistack.app.feature_updates.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La regla de «al día».
 *
 * Antes se decía al día siempre que lo publicado no fuera más nuevo, y eso mete en el mismo
 * saco tres cosas distintas: tener justo lo último, ir por delante de lo que ese canal publica,
 * y que el canal no haya publicado nada. Solo la primera es estar al día.
 */
class UpdateOutcomeTest {

    private fun release(version: String) = UpdateInfo(
        versionName = version,
        releaseNotes = "",
        releaseDate = "2026-08-19",
        downloadUrl = "https://example.test/app.apk",
        sizeMb = 11.0
    )

    @Test
    fun `al dia solo si coincide con lo instalado`() {
        assertEquals(
            UpdateState.UpToDate,
            resolveUpdateState(release("1.2.0-alpha.2"), installed = "1.2.0-alpha.2")
        )
    }

    @Test
    fun `una version posterior es una actualizacion`() {
        val resultado = resolveUpdateState(release("1.2.0"), installed = "1.2.0-alpha.2")

        assertTrue(resultado is UpdateState.Available)
        assertEquals("1.2.0", (resultado as UpdateState.Available).info.versionName)
    }

    @Test
    fun `una alpha mirando el canal beta va por delante, no al dia`() {
        // El caso que se veía en el móvil: 1.2.0-alpha.2 puesta, canal beta elegido, y la
        // última beta publicada es la 1.1.0-beta.4.
        val resultado = resolveUpdateState(release("1.1.0-beta.4"), installed = "1.2.0-alpha.2")

        assertTrue(resultado is UpdateState.Ahead)
        assertEquals("1.1.0-beta.4", (resultado as UpdateState.Ahead).info.versionName)
    }

    @Test
    fun `un canal sin publicaciones no es estar al dia`() {
        assertEquals(UpdateState.NoReleases, resolveUpdateState(null, installed = "1.2.0-alpha.2"))
    }

    @Test
    fun `la v de la etiqueta no cuenta como version distinta`() {
        assertEquals(
            UpdateState.UpToDate,
            resolveUpdateState(release("v1.1.0"), installed = "1.1.0")
        )
    }
}
