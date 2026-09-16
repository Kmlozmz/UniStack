package com.unistack.app.core.fallos

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El recorte del informe, que es lo único que aquí tiene lógica.
 *
 * Se prueba porque la pantalla **le promete un número al usuario**: «se quitaron 3 datos
 * tuyos». Si ese número se separa de lo que de verdad se reemplazó, la frase pasa de
 * informar a mentir, y es de las que se leen una vez y se creen para siempre.
 */
class InformeDeFalloTest {

    @Test
    fun `quita el correo y lo cuenta`() {
        val (texto, correos, textos) = InformeDeFallo.limpiar(
            "falló al sincronizar usuario@correo.edu.co"
        )

        assertFalse(texto.contains("usuario@correo.edu.co"))
        assertTrue(texto.contains("[correo quitado]"))
        assertEquals(1, correos)
        // Cero, no uno: la marca que deja la primera pasada no la puede volver a contar la
        // segunda. Con comillas angulares de marca pasaba justo eso.
        assertEquals(0, textos)
    }

    @Test
    fun `quita lo entrecomillado con comillas rectas y angulares`() {
        val (texto, _, textos) = InformeDeFallo.limpiar(
            """no existe la materia "Álgebra Lineal" para la tarea «Taller 4»"""
        )

        assertFalse(texto.contains("Álgebra Lineal"))
        assertFalse(texto.contains("Taller 4"))
        assertEquals(2, textos)
    }

    @Test
    fun `un correo entrecomillado cuenta como correo y como texto`() {
        // El orden de los dos reemplazos es lo que se está comprobando: si el entrecomillado
        // corriera primero, el correo desaparecería dentro de él y nunca se contaría como tal.
        val (texto, correos, textos) = InformeDeFallo.limpiar("""cuenta "usuario@correo.edu.co"""")

        assertFalse(texto.contains("usuario@correo.edu.co"))
        assertEquals(1, correos)
        assertEquals(1, textos)
    }

    @Test
    fun `sin nada del usuario deja el texto igual`() {
        val original = "java.lang.IllegalStateException en TaskListViewModel.kt:214"
        val (texto, correos, textos) = InformeDeFallo.limpiar(original)

        assertEquals(original, texto)
        assertEquals(0, correos)
        assertEquals(0, textos)
    }

    @Test
    fun `no toca unas comillas que no envuelven nada`() {
        // Un par vacío sale del propio código —un valor en blanco al formatear— y borrarlo
        // sólo serviría para inflar la cuenta que se le enseña al usuario.
        val original = """el valor "" no vale"""
        val (texto, _, textos) = InformeDeFallo.limpiar(original)

        assertEquals(original, texto)
        assertEquals(0, textos)
    }

    @Test
    fun `con comillas sueltas recorta de mas, que es el lado seguro`() {
        // El emparejado es ingenuo: con un número impar de comillas, lo que queda entre la
        // segunda y la tercera también se va. Se deja así a propósito — equivocarse quitando
        // texto de la traza cuesta una línea de contexto; equivocarse dejándolo cuesta el
        // nombre de una materia en un archivo que se va a enviar.
        val (texto, _, textos) = InformeDeFallo.limpiar("""abre "Álgebra y sigue "Taller 4"""")

        assertFalse(texto.contains("Álgebra"))
        assertTrue(textos >= 1)
    }
}
