package com.unistack.app.feature_updates.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** El «6 s» junto al porcentaje: se mide a un segundo vista, no a cada sondeo. */
class RitmoDeDescargaTest {

    @Test
    fun `sin un segundo de muestra no dice nada`() {
        val ritmo = RitmoDeDescarga()
        assertNull(ritmo.segundosQueFaltan(ahora = 0L, bajados = 0L, total = 1_000_000L))
        assertNull(ritmo.segundosQueFaltan(ahora = 200L, bajados = 100_000L, total = 1_000_000L))
    }

    @Test
    fun `al segundo calcula lo que falta a ese ritmo`() {
        val ritmo = RitmoDeDescarga()
        ritmo.segundosQueFaltan(ahora = 0L, bajados = 0L, total = 1_000_000L)
        // 200 000 bytes por segundo, faltan 800 000: cuatro segundos.
        assertEquals(4, ritmo.segundosQueFaltan(ahora = 1000L, bajados = 200_000L, total = 1_000_000L))
    }

    @Test
    fun `sin peso total no hay estimacion`() {
        val ritmo = RitmoDeDescarga()
        ritmo.segundosQueFaltan(ahora = 0L, bajados = 0L, total = -1L)
        assertNull(ritmo.segundosQueFaltan(ahora = 1000L, bajados = 200_000L, total = -1L))
    }
}
