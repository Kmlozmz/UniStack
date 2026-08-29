package com.unistack.app.feature_notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La barra de la escritura sencilla.
 *
 * Escribe las mismas marcas que escribiria alguien a mano, y por eso cambiar una nota de una
 * manera a la otra no convierte nada. Lo que se prueba aqui es que cada boton sea un
 * interruptor: en sencillo las marcas no se ven, asi que si poner negrita no la quitara tambien,
 * la unica forma de deshacerla seria borrar unos caracteres invisibles.
 */
class NoteFormattingTest {

    private fun aplicar(action: NoteAction, texto: String, desde: Int, hasta: Int) =
        NoteFormatting.apply(action, texto, desde, hasta)

    @Test
    fun laNegritaEnvuelveLoSeleccionado() {
        val r = aplicar(NoteAction.NEGRITA, "esto es importante hoy", 8, 18)
        assertEquals("esto es **importante** hoy", r.text)
        assertEquals("importante", r.text.substring(r.selectionStart, r.selectionEnd))
    }

    @Test
    fun volverATocarlaLaQuita() {
        val r = aplicar(NoteAction.NEGRITA, "esto es **importante** hoy", 10, 20)
        assertEquals("esto es importante hoy", r.text)
        assertEquals("importante", r.text.substring(r.selectionStart, r.selectionEnd))
    }

    @Test
    fun sinNadaSeleccionadoElCursorSeQuedaEnMedio() {
        val r = aplicar(NoteAction.NEGRITA, "hola ", 5, 5)
        assertEquals("hola ****", r.text)
        assertEquals(7, r.selectionStart)
        assertEquals(7, r.selectionEnd)
    }

    @Test
    fun laCursivaYElTachadoUsanSusPropiasMarcas() {
        assertEquals("*matiz*", aplicar(NoteAction.CURSIVA, "matiz", 0, 5).text)
        assertEquals("~~ya no~~", aplicar(NoteAction.TACHADO, "ya no", 0, 5).text)
    }

    @Test
    fun laVinetaSePoneEnLaLineaEntera() {
        val r = aplicar(NoteAction.VINETA, "traer calculadora", 6, 6)
        assertEquals("- traer calculadora", r.text)
    }

    @Test
    fun laVinetaSeQuitaSiYaEstaba() {
        val r = aplicar(NoteAction.VINETA, "- traer calculadora", 8, 8)
        assertEquals("traer calculadora", r.text)
    }

    @Test
    fun laVinetaAlcanzaATodasLasLineasSeleccionadas() {
        val r = aplicar(NoteAction.VINETA, "uno\ndos\ntres", 1, 10)
        assertEquals("- uno\n- dos\n- tres", r.text)
    }

    /** Una lista numerada cuenta desde uno en el bloque que se selecciona. */
    @Test
    fun laListaNumeradaNumeraCadaLinea() {
        val r = aplicar(NoteAction.NUMERADA, "uno\ndos\ntres", 0, 12)
        assertEquals("1. uno\n2. dos\n3. tres", r.text)
    }

    @Test
    fun laListaNumeradaSeQuitaAunqueCadaLineaLlevaraUnNumeroDistinto() {
        val r = aplicar(NoteAction.NUMERADA, "1. uno\n2. dos\n3. tres", 0, 21)
        assertEquals("uno\ndos\ntres", r.text)
    }

    @Test
    fun laCasillaPoneLaMarcaCompleta() {
        assertEquals("- [ ] taller 3", aplicar(NoteAction.CASILLA, "taller 3", 0, 0).text)
        assertEquals("taller 3", aplicar(NoteAction.CASILLA, "- [ ] taller 3", 8, 8).text)
    }

    /*
     * Lo que escribe la barra tiene que ser exactamente lo que el motor sabe leer: si no, en
     * sencillo se veria la marca en crudo justo despues de haberla puesto desde un boton.
     */
    @Test
    fun loQueEscribeLaBarraLoEntiendeElMotor() {
        val negrita = aplicar(NoteAction.NEGRITA, "importante", 0, 10).text
        assertEquals("importante", NoteMarkdown.strip(negrita))

        val casilla = aplicar(NoteAction.CASILLA, "taller", 0, 0).text
        assertEquals("☐ taller", NoteMarkdown.strip(casilla))

        val vineta = aplicar(NoteAction.VINETA, "punto", 0, 0).text
        assertEquals("• punto", NoteMarkdown.strip(vineta))
    }

    @Test
    fun unaSeleccionAlRevesTambienVale() {
        val r = aplicar(NoteAction.NEGRITA, "importante", 10, 0)
        assertEquals("**importante**", r.text)
    }

    /*
     * Los titulos son un cambio y no un interruptor: pedir «Titulo» sobre una linea que ya es
     * subtitulo la convierte, no le anade otra almohadilla delante.
     */
    @Test
    fun elTituloCambiaDeNivelEnVezDeAcumularse() {
        assertEquals("# Parcial", aplicar(NoteAction.TITULO1, "Parcial", 0, 0).text)
        assertEquals("# Parcial", aplicar(NoteAction.TITULO1, "## Parcial", 3, 3).text)
        assertEquals("## Parcial", aplicar(NoteAction.TITULO2, "# Parcial", 2, 2).text)
    }

    @Test
    fun volverAPedirElMismoNivelLoQuita() {
        assertEquals("Parcial", aplicar(NoteAction.TITULO1, "# Parcial", 2, 2).text)
    }

    @Test
    fun textoNormalDejaLaLineaLisa() {
        assertEquals("Parcial", aplicar(NoteAction.NORMAL, "### Parcial", 4, 4).text)
        assertEquals("Parcial", aplicar(NoteAction.NORMAL, "Parcial", 0, 0).text)
    }

    /** Quitar el formato deja el trozo en texto pelado, sea cual sea la marca que llevara. */
    @Test
    fun limpiarQuitaTodasLasMarcasDeLoSeleccionado() {
        val texto = "esto es **muy** importante"
        val r = aplicar(NoteAction.LIMPIAR, texto, 8, 15)
        assertEquals("esto es muy importante", r.text)
    }

    @Test
    fun limpiarSinNadaSeleccionadoNoToca() {
        assertEquals("**algo**", aplicar(NoteAction.LIMPIAR, "**algo**", 3, 3).text)
    }

    /*
     * La tabla sale hecha porque escribir la linea de guiones a mano es lo que hace que casi
     * nadie use tablas en Markdown.
     */
    @Test
    fun laTablaSaleConSuLineaDeSeparacion() {
        val r = aplicar(NoteAction.TABLA, "", 0, 0)
        assertTrue(r.text, r.text.contains("| Columna | Columna |"))
        assertTrue(r.text, r.text.contains("| --- | --- |"))
        assertEquals(NoteStyle.TABLA, NoteMarkdown.parse(r.text).spans.first().style)
    }

    @Test
    fun laTablaSeSeparaDeLoQueYaHabiaEscrito() {
        val r = aplicar(NoteAction.TABLA, "una frase", 9, 9)
        assertTrue(r.text, r.text.startsWith("una frase\n|"))
    }

    @Test
    fun unaPosicionFueraDeRangoNoRompe() {
        val r = aplicar(NoteAction.NEGRITA, "hola", -5, 99)
        assertEquals("**hola**", r.text)
    }
}
