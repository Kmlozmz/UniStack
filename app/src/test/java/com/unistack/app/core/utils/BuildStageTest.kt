package com.unistack.app.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildStageTest {

    @Test
    fun `cada sufijo cae en su peldano`() {
        assertEquals(BuildStage.DEV, BuildStage.of("0.0.0-dev.26081317"))
        assertEquals(BuildStage.ALPHA, BuildStage.of("1.0.0-alpha.5"))
        assertEquals(BuildStage.BETA, BuildStage.of("1.0.0-beta.1"))
        assertEquals(BuildStage.RC, BuildStage.of("1.0.0-rc.1"))
        assertEquals(BuildStage.STABLE, BuildStage.of("1.0.0"))
    }

    @Test
    fun `solo dev y alpha abren lo que esta a medias`() {
        assertTrue(BuildStage.of("0.0.0-dev.1").allowsUnfinished)
        assertTrue(BuildStage.of("1.0.0-alpha.1").allowsUnfinished)
        assertFalse(BuildStage.of("1.0.0-beta.1").allowsUnfinished)
        assertFalse(BuildStage.of("1.0.0-rc.1").allowsUnfinished)
        assertFalse(BuildStage.of("1.0.0").allowsUnfinished)
    }

    @Test
    fun `un sufijo desconocido se trata como definitiva`() {
        // Ante la duda, lo cerrado: una etiqueta que nadie previó no puede abrir de par en par
        // lo que está a medio hacer.
        assertEquals(BuildStage.STABLE, BuildStage.of("1.0.0-canary.3"))
        assertFalse(BuildStage.of("1.0.0-canary.3").allowsUnfinished)
    }

    @Test
    fun `las mayusculas dan igual`() {
        assertEquals(BuildStage.ALPHA, BuildStage.of("1.0.0-ALPHA.2"))
    }
}
