package com.unistack.app.feature_updates.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseVersionTest {

    @Test
    fun `una alpha es anterior a la version definitiva que la sigue`() {
        assertTrue(ReleaseVersion.isNewer("1.1.0", "1.1.0-alpha.1"))
        assertFalse(ReleaseVersion.isNewer("1.1.0-alpha.1", "1.1.0"))
    }

    @Test
    fun `las alphas se ordenan entre si`() {
        assertTrue(ReleaseVersion.isNewer("1.1.0-alpha.2", "1.1.0-alpha.1"))
        assertTrue(ReleaseVersion.isNewer("1.1.0-beta.1", "1.1.0-alpha.9"))
    }

    @Test
    fun `el sufijo no se cuela en el numero de parche`() {
        // Al partir por puntos y quedarse con lo numérico, «1.0.0-alpha.1» se leía como
        // «1.0.1» y se anunciaba como más nuevo que «1.0.0».
        assertFalse(ReleaseVersion.isNewer("1.0.0-alpha.1", "1.0.0"))
    }

    @Test
    fun `cualquier publicacion supera a una compilacion local`() {
        // Las locales se numeran 0.0.0-dev.<yyMMddHH> justamente para esto: para que la
        // primera versión pública pueda ser la 1.0.0 y no una inventada más arriba.
        assertTrue(ReleaseVersion.isNewer("1.0.0-alpha.1", "0.0.0-dev.26081310"))
        assertTrue(ReleaseVersion.isNewer("1.0.0", "0.0.0-dev.26081310"))
        assertFalse(ReleaseVersion.isNewer("0.0.0-dev.26081310", "1.0.0-alpha.1"))
    }

    @Test
    fun `entre compilaciones locales manda el sello`() {
        assertTrue(ReleaseVersion.isNewer("0.0.0-dev.26081410", "0.0.0-dev.26081310"))
    }

    @Test
    fun `la v inicial de la etiqueta no cuenta`() {
        assertTrue(ReleaseVersion.isNewer("v1.2.0", "1.1.9"))
    }

    @Test
    fun `la misma version no es una actualizacion`() {
        assertFalse(ReleaseVersion.isNewer("1.1.0", "1.1.0"))
        assertFalse(ReleaseVersion.isNewer("1.1.0-alpha.1", "1.1.0-alpha.1"))
    }
}
