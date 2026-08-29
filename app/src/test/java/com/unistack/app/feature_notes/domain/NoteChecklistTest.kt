package com.unistack.app.feature_notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cuando una nota es una lista, y que pasa al escribirla.
 *
 * No hay columna que lo diga: se deduce del contenido. Eso es lo que permite que nadie tenga que
 * elegir un tipo al crear la nota, que fue lo que el pidio quitar. Estas pruebas fijan la
 * frontera —que es donde se equivoca— y que al ir y volver el texto sale igual.
 */
class NoteChecklistTest {

    private val n = 10.toChar().toString()

    @Test
    fun todoCasillasEsUnaLista() {
        assertTrue(NoteChecklist.isChecklist("- [ ] uno" + n + "- [x] dos"))
    }

    /** Una sola linea de texto suelta ya la saca de ser lista: ahi hay que escribir seguido. */
    @Test
    fun conUnParrafoDejaDeSerLista() {
        assertFalse(NoteChecklist.isChecklist("Pendientes" + n + "- [ ] uno"))
    }

    @Test
    fun unaNotaVaciaTodaviaNoEsLista() {
        assertFalse(NoteChecklist.isChecklist(""))
        assertFalse(NoteChecklist.isChecklist("   "))
    }

    @Test
    fun lasLineasEnBlancoNoCuentan() {
        assertTrue(NoteChecklist.isChecklist("- [ ] uno" + n + n + "- [ ] dos"))
    }

    @Test
    fun cadaLineaEsUnElementoConSuEstado() {
        val items = NoteChecklist.parse("- [ ] taller 3" + n + "- [x] taller 2")
        assertEquals(2, items.size)
        assertEquals("taller 3", items[0].text)
        assertFalse(items[0].checked)
        assertTrue(items[1].checked)
    }

    /** Ir y volver tiene que dejar el texto igual: es lo que guarda la base. */
    @Test
    fun elTextoSobreviveALaIdaYLaVuelta() {
        val texto = "- [ ] uno" + n + "- [x] dos" + n + "- [ ] tres"
        assertEquals(texto, NoteChecklist.render(NoteChecklist.parse(texto)))
    }

    /**
     * Los renglones vacios se caen al guardar.
     *
     * Es lo que queda cuando alguien borra el texto de un elemento y no se acuerda de quitarlo:
     * una casilla sin nada al lado que no se puede ni marcar ni leer.
     */
    @Test
    fun losElementosVaciosNoSeGuardan() {
        val items = listOf(
            ChecklistItem("uno", false),
            ChecklistItem("   ", false),
            ChecklistItem("dos", true)
        )
        assertEquals("- [ ] uno" + n + "- [x] dos", NoteChecklist.render(items))
    }

    @Test
    fun unaListaVaciaSeGuardaComoNada() {
        assertEquals("", NoteChecklist.render(listOf(ChecklistItem("", false))))
    }

    /*
     * Convertir en lista no puede perder lineas: quien tiene tres pendientes escritos a mano y
     * toca «Casillas» espera encontrarse sus tres, no una lista en blanco.
     */
    @Test
    fun convertirUnTextoRespetaSusLineas() {
        val items = NoteChecklist.from("comprar el libro" + n + "leer el capitulo 4")
        assertEquals(2, items.size)
        assertEquals("comprar el libro", items[0].text)
        assertFalse(items[1].checked)
    }

    @Test
    fun alConvertirLoQueYaEraCasillaConservaSuMarca() {
        val items = NoteChecklist.from("- [x] hecho" + n + "pendiente")
        assertEquals(2, items.size)
        assertTrue(items[0].checked)
        assertFalse(items[1].checked)
    }

    @Test
    fun convertirUnaNotaVaciaDaUnElementoParaEmpezar() {
        assertEquals(1, NoteChecklist.from("").size)
    }

    @Test
    fun laConversionQuitaLasMarcasDelTextoNormal() {
        val items = NoteChecklist.from("# Pendientes")
        assertEquals("Pendientes", items.single().text)
    }

    @Test
    fun moverCambiaElOrdenSinPerderNada() {
        val items = listOf(
            ChecklistItem("a", false),
            ChecklistItem("b", false),
            ChecklistItem("c", false)
        )
        assertEquals(listOf("c", "a", "b"), NoteChecklist.move(items, 2, 0).map { it.text })
        assertEquals(3, NoteChecklist.move(items, 2, 0).size)
    }

    @Test
    fun moverAUnSitioImposibleNoToca() {
        val items = listOf(ChecklistItem("a", false))
        assertEquals(items, NoteChecklist.move(items, 0, 5))
        assertEquals(items, NoteChecklist.move(items, -1, 0))
        assertEquals(items, NoteChecklist.move(items, 0, 0))
    }
}
