package com.unistack.app.feature_support.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogTest {

    private val archivo = """
        # Cambios

        Cabecera con instrucciones para quien edita el archivo.

        ---

        ## [Sin publicar]

        ### Añadido

        - Algo que todavía no ha salido.

        ---

        ## [1.1.0] — 2026-09-01

        ### Añadido

        - La función grande de la 1.1.0.

        ---

        ## [1.1.0-beta.1] — 2026-08-25

        ### Corregido

        - Un arreglo que vio la beta.

        ---

        ## [1.1.0-alpha.2] — 2026-08-20

        ### Corregido

        - Un arreglo que solo vio la alpha.

        ---

        ## [1.1.0-alpha.1] — 2026-08-18

        ### Añadido

        - El primer intento de la función.

        ---

        ## [1.0.0] — 2026-08-10

        ### Añadido

        - La primera versión pública.
    """.trimIndent()

    @Test
    fun `lo que no se ha publicado no se ensena`() {
        val versiones = parseChangelog(archivo).map { it.version }

        assertTrue("«Sin publicar» no puede salir", versiones.none { it.contains("Sin publicar") })
        assertEquals("1.1.0", versiones.first())
    }

    @Test
    fun `una definitiva recoge todo desde la definitiva anterior`() {
        val visible = changelogFor(archivo, "1.1.0").map { it.version }

        assertEquals(listOf("1.1.0", "1.1.0-beta.1", "1.1.0-alpha.2", "1.1.0-alpha.1"), visible)
    }

    @Test
    fun `una beta recoge desde la beta anterior, con las alphas de en medio`() {
        val visible = changelogFor(archivo, "1.1.0-beta.1").map { it.version }

        assertEquals(listOf("1.1.0-beta.1", "1.1.0-alpha.2", "1.1.0-alpha.1"), visible)
    }

    @Test
    fun `una alpha solo cuenta lo suyo desde la alpha anterior`() {
        val visible = changelogFor(archivo, "1.1.0-alpha.2").map { it.version }

        assertEquals(listOf("1.1.0-alpha.2"), visible)
    }

    @Test
    fun `nadie ve versiones posteriores a la que tiene instalada`() {
        val visible = changelogFor(archivo, "1.0.0").map { it.version }

        assertEquals(listOf("1.0.0"), visible)
        assertTrue("la 1.1.0 no puede asomar", visible.none { it.startsWith("1.1.0") })
    }

    @Test
    fun `una compilacion de trabajo ve lo ultimo publicado`() {
        val visible = changelogFor(archivo, "0.0.0-dev.26081317").map { it.version }

        assertEquals("1.1.0", visible.first())
    }

    @Test
    fun `cada seccion conserva su fecha y su contenido`() {
        val beta = changelogFor(archivo, "1.1.0-beta.1").first()

        assertEquals("2026-08-25", beta.date)
        assertTrue(beta.body.contains("Un arreglo que vio la beta"))
        assertTrue("el separador no es contenido", !beta.body.endsWith("---"))
    }

    @Test
    fun `un archivo sin versiones publicadas no rompe nada`() {
        assertEquals(emptyList<ChangelogSection>(), changelogFor("## [Sin publicar]\n\n- nada", "1.0.0"))
    }
}
