package com.unistack.app.feature_notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Buscar como se busca de verdad: sin tildes y sin mayusculas.
 *
 * Nadie escribe «Cálculo» con tilde en el buscador de su propia app. Una busqueda que no
 * encuentra «Cálculo II» al teclear «calculo» se siente rota aunque sea tecnicamente correcta,
 * y es el fallo que se arreglo en el artifact cuando `@Calculo` no encontraba nada.
 */
class NoteSearchTest {

    private fun nota(body: String) = QuickNote(
        id = "n",
        body = body,
        createdAt = 0,
        updatedAt = 0
    )

    @Test
    fun lasTildesNoCuentanEnNingunLado() {
        assertEquals("calculo ii", NoteSearch.normalize("Cálculo II"))
        assertEquals("calculo", NoteSearch.normalize("  CALCULO  "))
        assertEquals("nino", NoteSearch.normalize("niño"))
    }

    @Test
    fun buscarSinTildeEncuentraLoEscritoConTilde() {
        assertTrue(NoteSearch.matches(nota("Parcial de Cálculo el martes"), "calculo"))
    }

    /** Y al reves: quien si pone la tilde tambien tiene que encontrar lo escrito sin ella. */
    @Test
    fun buscarConTildeEncuentraLoEscritoSinElla() {
        assertTrue(NoteSearch.matches(nota("Parcial de Calculo el martes"), "cálculo"))
    }

    /*
     * Se busca en el texto sin marcas: quien teclea «parcial» espera encontrar una nota titulada
     * `## Parcial 2`, y las almohadillas no son parte de lo que escribio.
     */
    @Test
    fun lasMarcasDeMarkdownNoEstorbanNiCuentan() {
        assertTrue(NoteSearch.matches(nota("## Parcial 2"), "parcial"))
        assertFalse(NoteSearch.matches(nota("## Parcial 2"), "##"))
    }

    @Test
    fun sinBusquedaEstanTodas() {
        val notas = listOf(nota("uno"), nota("dos"))
        assertEquals(2, NoteSearch.filter(notas, "").size)
        assertEquals(2, NoteSearch.filter(notas, "   ").size)
    }

    @Test
    fun loQueNoEstaNoAparece() {
        assertTrue(NoteSearch.filter(listOf(nota("Parcial")), "laboratorio").isEmpty())
    }

    /**
     * Al escribir tras la arroba, primero lo que empieza igual.
     *
     * Quien teclea «cal» busca «Cálculo II» antes que «Química Vertical», aunque las dos lleven
     * esas tres letras dentro.
     */
    @Test
    fun lasMateriasQueEmpiezanIgualVanPrimero() {
        val materias = listOf(
            "s1" to "Química Vertical",
            "s2" to "Cálculo II"
        )
        assertEquals(listOf("s2", "s1"), NoteSearch.matchingSubjects(materias, "cal").map { it.first })
    }

    @Test
    fun sinNadaTecleadoSalenTodasLasMaterias() {
        val materias = listOf("s1" to "Física", "s2" to "Cálculo II")
        assertEquals(2, NoteSearch.matchingSubjects(materias, "").size)
    }

    @Test
    fun unaMateriaQueNoEncajaNoSale() {
        val materias = listOf("s1" to "Física")
        assertTrue(NoteSearch.matchingSubjects(materias, "derecho").isEmpty())
    }
}
