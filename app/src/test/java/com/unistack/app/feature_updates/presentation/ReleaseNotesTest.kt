package com.unistack.app.feature_updates.presentation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prueba el Markdown de las notas de versión sobre un trozo real del CHANGELOG, con lo que de
 * verdad aparece ahí: encabezados, viñetas partidas en dos líneas y negritas.
 */
class ReleaseNotesTest {

    private val notas = """
        Tercera alpha. Casi todo es la pantalla de actualizaciones.

        ### Cambiado

        - **Alpha y beta son ahora canales distintos, no escalones.** Cada uno tiene su propio
          código: el de alpha ya no abre beta.
        - Los códigos anteriores dejan de valer.

        ---
    """.trimIndent()

    private val bloques = parseReleaseNotes(notas)

    @Test
    fun `el encabezado no sale con almohadillas`() {
        val encabezados = bloques.filterIsInstance<NotesBlock.Heading>()
        assertEquals(1, encabezados.size)
        assertEquals("Cambiado", encabezados.first().text)
    }

    @Test
    fun `una vinieta partida en dos lineas es una sola vinieta`() {
        // En el archivo las frases se cortan a los 96 caracteres. Antes cada línea se convertía
        // en una viñeta suelta, así que salían a medio empezar.
        val vinietas = bloques.filterIsInstance<NotesBlock.Bullet>()
        assertEquals(2, vinietas.size)
        assertTrue(
            vinietas.first().text.text,
            vinietas.first().text.text.endsWith("el de alpha ya no abre beta.")
        )
    }

    @Test
    fun `los asteriscos de negrita no se ven`() {
        val texto = bloques.filterIsInstance<NotesBlock.Bullet>().first().text
        assertTrue("quedan asteriscos: ${texto.text}", !texto.text.contains("**"))
        assertTrue("no hay ningún tramo en negrita", texto.spanStyles.isNotEmpty())
        assertEquals(
            "Alpha y beta son ahora canales distintos, no escalones.",
            texto.text.substring(
                texto.spanStyles.first().start,
                texto.spanStyles.first().end
            )
        )
    }

    @Test
    fun `el parrafo de entrada se conserva`() {
        val parrafos = bloques.filterIsInstance<NotesBlock.Paragraph>()
        assertEquals(1, parrafos.size)
        assertTrue(parrafos.first().text.text.startsWith("Tercera alpha."))
    }

    @Test
    fun `las rayas separadoras no se leen como texto`() {
        assertTrue(bloques.none { it is NotesBlock.Paragraph && it.text.text.contains("---") })
    }

    @Test
    fun `una negrita sin cerrar no se come el resto del texto`() {
        val texto = markdownInline("un **aviso a medias que nadie cerró")
        assertEquals("un **aviso a medias que nadie cerró", texto.text)
    }
}
