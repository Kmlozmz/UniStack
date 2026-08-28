package com.unistack.app.feature_notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Como se lee una nota sin abrirla.
 *
 * No hay campo de titulo: el titulo es la primera linea con algo escrito. Estas pruebas fijan
 * ese «algo escrito», que es donde estan los casos raros —empezar con dos saltos de linea,
 * escribir una sola palabra, pegar un parrafo de trescientas letras sin cortes—.
 */
class NoteTextTest {

    @Test
    fun elTituloEsLaPrimeraLineaConAlgoEscrito() {
        val body = "Parcial 2\nMartes 14, salon 302"
        assertEquals("Parcial 2", NoteText.title(body))
    }

    @Test
    fun elTituloSaltaLasLineasEnBlancoDelPrincipio() {
        val body = "\n\n   \nLo del laboratorio\nTraer bata"
        assertEquals("Lo del laboratorio", NoteText.title(body))
    }

    @Test
    fun unaNotaEnBlancoNoTieneTitulo() {
        assertEquals("", NoteText.title("   \n\n  "))
    }

    /**
     * Un parrafo pegado de golpe no puede ocupar la tarjeta entera.
     *
     * Quien copia un enunciado del aula virtual lo pega sin saltos de linea, asi que la primera
     * linea puede tener seiscientos caracteres. Se corta con puntos suspensivos.
     */
    @Test
    fun elTituloLargoSeCortaConPuntosSuspensivos() {
        val largo = "a".repeat(200)
        val titulo = NoteText.title(largo)
        assertTrue(titulo.length < largo.length)
        assertTrue(titulo.endsWith("…"))
    }

    @Test
    fun laVistaPreviaEsLoQueVaDespuesDelTitulo() {
        val body = "Parcial 2\nMartes 14\nSalon 302"
        assertEquals("Martes 14\nSalon 302", NoteText.preview(body))
    }

    @Test
    fun laVistaPreviaSeQuedaEnLasLineasQueSePiden() {
        val body = "Titulo\nuno\ndos\ntres\ncuatro\ncinco"
        assertEquals("uno\ndos", NoteText.preview(body, maxLines = 2))
    }

    /** Un titulo suelto no arrastra una vista previa vacia debajo. */
    @Test
    fun sinMasLineasLaVistaPreviaEstaVacia() {
        assertEquals("", NoteText.preview("Solo esto"))
        assertEquals("", NoteText.preview("Solo esto\n\n\n"))
    }

    @Test
    fun laVistaPreviaSaltaElHuecoEntreTituloYCuerpo() {
        assertEquals("El cuerpo", NoteText.preview("Titulo\n\n\nEl cuerpo"))
    }

    @Test
    fun soloEspaciosCuentaComoVacio() {
        assertTrue(NoteText.isEmpty("   \n\t\n "))
        assertFalse(NoteText.isEmpty("  x  "))
    }

    /** Al preguntar «¿borrar esto?» hay que poder nombrarlo aunque no tenga primera linea. */
    @Test
    fun unaNotaSinTituloTieneNombreParaElDialogo() {
        assertEquals("esta nota", NoteText.label("   "))
        assertEquals("Parcial 2", NoteText.label("Parcial 2\ny mas"))
    }
}
