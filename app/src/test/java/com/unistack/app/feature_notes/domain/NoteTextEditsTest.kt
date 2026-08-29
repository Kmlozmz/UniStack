package com.unistack.app.feature_notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Las dos cuentas que permiten esconder las marcas sin perder el cursor.
 *
 * En escritura sencilla lo que se lee tiene menos caracteres que lo que hay guardado, y sin
 * embargo cada toque, cada seleccion y el cursor se dan en el texto de verdad. Compose exige
 * que las dos conversiones devuelvan posiciones dentro del texto y revienta si una se sale, asi
 * que esta prueba las recorre **todas**, una por una, en varios textos con marcas de todo tipo.
 */
class NoteTextEditsTest {

    private val ejemplos = listOf(
        "",
        "sin marcas ninguna",
        "# Parcial 2",
        "- traer calculadora",
        "- [ ] taller 3\n- [x] taller 2",
        "esto es **importante** y esto *no*",
        "mira [el aula](https://x.co) hoy",
        "# Titulo\n- [ ] **algo** con `codigo`\n> cita\n---\n1. uno\n| a | b |",
        "```\nbloque\n```",
        "*",
        "**",
        "- ",
        "#"
    )

    private fun escondido(texto: String): EditedText {
        val markup = NoteMarkdown.parse(texto)
        return NoteTextEdits.apply(texto, NoteTextEdits.hidingEdits(markup))
    }

    @Test
    fun ningunaPosicionSeSaleDelTexto() {
        for (texto in ejemplos) {
            val editado = escondido(texto)
            for (offset in 0..texto.length) {
                val t = editado.toTransformed(offset)
                assertTrue(
                    "«$texto» posicion $offset -> $t fuera de 0..${editado.text.length}",
                    t in 0..editado.text.length
                )
            }
            for (offset in 0..editado.text.length) {
                val o = editado.toOriginal(offset)
                assertTrue(
                    "«$texto» posicion visible $offset -> $o fuera de 0..${texto.length}",
                    o in 0..texto.length
                )
            }
        }
    }

    /**
     * Ir hacia adelante en el texto no puede ir hacia atras en la pantalla.
     *
     * Si una posicion mayor devolviera una menor, la seleccion se daria la vuelta sola y el
     * cursor saltaria al principio al escribir una letra en medio.
     */
    @Test
    fun lasDosCuentasNuncaRetroceden() {
        for (texto in ejemplos) {
            val editado = escondido(texto)
            var previo = 0
            for (offset in 0..texto.length) {
                val t = editado.toTransformed(offset)
                assertTrue("«$texto» retrocede en $offset", t >= previo)
                previo = t
            }
            previo = 0
            for (offset in 0..editado.text.length) {
                val o = editado.toOriginal(offset)
                assertTrue("«$texto» retrocede al volver en $offset", o >= previo)
                previo = o
            }
        }
    }

    /**
     * Los extremos siguen siendo los extremos, con un matiz.
     *
     * Hacia la pantalla es exacto: el principio del texto guardado es el principio de lo que se
     * ve, y el final es el final. Al volver no siempre: en «# Parcial» la posicion 0 de lo que
     * se ve cae **detras** de la almohadilla, y tiene que ser asi. Quien ve «Parcial» y escribe
     * al principio quiere «# xParcial», no «x# Parcial», que dejaria de ser un titulo.
     *
     * Lo que si tiene que cumplirse siempre es que ir y volver no mueva nada.
     */
    @Test
    fun losExtremosSeQuedanEnSuSitio() {
        for (texto in ejemplos) {
            val editado = escondido(texto)
            assertEquals(0, editado.toTransformed(0))
            assertEquals(editado.text.length, editado.toTransformed(texto.length))
            assertEquals(texto.length, editado.toOriginal(editado.text.length))

            val vuelta = editado.toOriginal(0)
            assertTrue("«$texto» vuelve fuera del texto", vuelta in 0..texto.length)
            assertEquals("«$texto» no cuadra al ir y volver", 0, editado.toTransformed(vuelta))
        }
    }

    @Test
    fun elTextoEscondidoEsElMismoQueElDeLaLista() {
        for (texto in ejemplos) {
            assertEquals(texto, NoteMarkdown.strip(texto), escondido(texto).text)
        }
    }

    /** Fuera de rango no es motivo para reventar: se pega al borde mas cercano. */
    @Test
    fun unaPosicionImposibleSePegaAlBorde() {
        val editado = escondido("# Parcial")
        assertEquals(0, editado.toTransformed(-40))
        assertEquals(editado.text.length, editado.toTransformed(9999))
        // Al volver, la posicion 0 cae detras de la almohadilla: es donde se escribe.
        assertEquals(2, editado.toOriginal(-40))
        assertEquals("# Parcial".length, editado.toOriginal(9999))
    }

    @Test
    fun sinTramosQueEsconderElTextoNoCambia() {
        val editado = NoteTextEdits.apply("nada que esconder", emptyList())
        assertEquals("nada que esconder", editado.text)
        assertEquals(7, editado.toTransformed(7))
        assertEquals(7, editado.toOriginal(7))
    }

    /**
     * Dos tramos que se pisan significan que el analisis se equivoco.
     *
     * Recortarlos a medias daria un texto que no es ni el de antes ni el de despues, asi que el
     * segundo se descarta entero y lo que se enseña sigue siendo coherente.
     */
    @Test
    fun losTramosQueSePisanSeDescartan() {
        val editado = NoteTextEdits.apply(
            "abcdefgh",
            listOf(TextEdit(1, 4, ""), TextEdit(2, 6, "X"))
        )
        assertEquals("aefgh", editado.text)
    }
}
