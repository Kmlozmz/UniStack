package com.unistack.app.feature_notes.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * El Markdown de apuntes: que reconozca lo que tiene que reconocer, y nada mas.
 *
 * Lo que se comprueba aqui no es como se ve —eso es del tema— sino **donde** empieza y acaba
 * cada cosa. Una posicion mal calculada no se nota mirando la pantalla: se nota cuando el
 * cursor salta o cuando media palabra se queda en negrita.
 */
class NoteMarkdownTest {

    private fun estilos(text: String): List<NoteStyle> =
        NoteMarkdown.parse(text).spans.map { it.style }

    private fun span(text: String, style: NoteStyle): String? =
        NoteMarkdown.parse(text).spans.firstOrNull { it.style == style }
            ?.let { text.substring(it.start, it.end) }

    @Test
    fun losTitulosLleganHastaElTercerNivel() {
        assertEquals(listOf(NoteStyle.TITULO1), estilos("# Parcial"))
        assertEquals(listOf(NoteStyle.TITULO2), estilos("## Temas"))
        assertEquals(listOf(NoteStyle.TITULO3), estilos("### Capitulo"))
        // Mas alla del tercero no hay tamaño que lo distinga del texto normal, asi que se trata
        // como el tercero en vez de inventar dos niveles que se verian iguales.
        assertEquals(listOf(NoteStyle.TITULO3), estilos("##### Muy hondo"))
    }

    @Test
    fun laAlmohadillaSinEspacioNoEsUnTitulo() {
        assertTrue(estilos("#etiqueta").isEmpty())
    }

    @Test
    fun elTituloEsLoQueVaDespuesDeLaMarca() {
        assertEquals("Parcial 2", span("## Parcial 2", NoteStyle.TITULO2))
    }

    @Test
    fun laNegritaGanaALaCursivaAunqueCompartanElAsterisco() {
        assertEquals("importante", span("esto es **importante** hoy", NoteStyle.NEGRITA))
        assertFalse(NoteStyle.CURSIVA in estilos("esto es **importante** hoy"))
    }

    @Test
    fun cursivaTachadoYCodigoSeReconocenPorSuMarca() {
        assertEquals("matiz", span("un *matiz* aqui", NoteStyle.CURSIVA))
        assertEquals("ya no", span("~~ya no~~ va", NoteStyle.TACHADO))
        assertEquals("x = 2", span("la formula `x = 2` sale", NoteStyle.CODIGO))
    }

    /**
     * Dentro de `codigo` no manda nadie mas.
     *
     * Quien pega una linea de codigo con asteriscos no quiere que la mitad se le ponga en
     * negrita; y en un apunte de programacion eso pasa constantemente.
     */
    @Test
    fun dentroDelCodigoNoSeAplicaNadaMas() {
        val estilos = estilos("mira `a ** b ** c` y ya")
        assertTrue(NoteStyle.CODIGO in estilos)
        assertFalse(NoteStyle.NEGRITA in estilos)
    }

    @Test
    fun elBloqueDeCodigoTapaLoDeDentro() {
        val texto = "```\n# no es titulo\n**ni negrita**\n```"
        val estilos = estilos(texto)
        assertTrue(NoteStyle.BLOQUE_CODIGO in estilos)
        assertFalse(NoteStyle.TITULO1 in estilos)
        assertFalse(NoteStyle.NEGRITA in estilos)
    }

    @Test
    fun lasListasYLasCasillasSeDistinguenEntreSi() {
        assertEquals(listOf(NoteStyle.VINETA), estilos("- traer calculadora"))
        assertEquals(listOf(NoteStyle.NUMERO), estilos("1. leer el capitulo"))
        assertEquals(listOf(NoteStyle.CASILLA), estilos("- [ ] taller 3"))
        assertEquals(listOf(NoteStyle.CASILLA_HECHA), estilos("- [x] taller 2"))
    }

    @Test
    fun laCasillaGanaALaVinetaAunqueEmpiecenIgual() {
        assertFalse(NoteStyle.VINETA in estilos("- [ ] taller 3"))
    }

    @Test
    fun laCitaYLaLineaSeparadoraTienenSuPropiaMarca() {
        assertEquals("lo que dijo", span("> lo que dijo", NoteStyle.CITA))
        assertEquals(listOf(NoteStyle.LINEA), estilos("---"))
    }

    @Test
    fun elEnlaceSoloPintaSuTexto() {
        assertEquals("el aula", span("mira [el aula](https://x.co) hoy", NoteStyle.ENLACE))
    }

    /*
     * Quitar las marcas es lo que hace la lista, y tiene que dejar el texto legible: sin
     * almohadillas, sin asteriscos, y con la vineta convertida en un punto de verdad.
     */
    @Test
    fun quitarLasMarcasDejaElTextoComoSeLee() {
        assertEquals("Parcial 2", NoteMarkdown.strip("## Parcial 2"))
        assertEquals("esto es importante", NoteMarkdown.strip("esto es **importante**"))
        assertEquals("• traer calculadora", NoteMarkdown.strip("- traer calculadora"))
        assertEquals("☐ taller 3", NoteMarkdown.strip("- [ ] taller 3"))
        assertEquals("☑ taller 2", NoteMarkdown.strip("- [x] taller 2"))
        assertEquals("lo que dijo", NoteMarkdown.strip("> lo que dijo"))
        assertEquals("el aula", NoteMarkdown.strip("[el aula](https://x.co)"))
    }

    @Test
    fun laListaNumeradaConservaSuNumeroAlQuitarLasMarcas() {
        assertEquals("1. leer el capitulo", NoteMarkdown.strip("1. leer el capitulo"))
    }

    @Test
    fun unTextoSinMarcasSaleIgual() {
        val texto = "Martes 14, salon 302. Traer calculadora."
        assertEquals(texto, NoteMarkdown.strip(texto))
    }

    @Test
    fun quitarLasMarcasNoPierdeLosSaltosDeLinea() {
        assertEquals("Parcial 2\n• martes\n• jueves", NoteMarkdown.strip("# Parcial 2\n- martes\n- jueves"))
    }

    /** El aviso al pasar a sencillo se dispara con lo que la barra no sabe poner ni quitar. */
    @Test
    fun soloAvisaCuandoHayTitulosTablasOBloques() {
        assertTrue(NoteMarkdown.hasRichBlocks("## Parcial"))
        assertTrue(NoteMarkdown.hasRichBlocks("| Corte | Peso |\n| 1 | 30 |"))
        assertTrue(NoteMarkdown.hasRichBlocks("```\ncodigo\n```"))
        assertFalse(NoteMarkdown.hasRichBlocks("**negrita** y - lista"))
        assertFalse(NoteMarkdown.hasRichBlocks("- [ ] taller"))
    }

    @Test
    fun unTextoVacioNoTieneNadaQuePintar() {
        val markup = NoteMarkdown.parse("")
        assertTrue(markup.spans.isEmpty())
        assertTrue(markup.markers.isEmpty())
    }

    /** Ninguna posicion puede caerse fuera del texto: es lo que revienta el editor. */
    @Test
    fun todasLasPosicionesCaenDentroDelTexto() {
        val texto = "# Titulo\n- [ ] **algo** con `codigo`\n> cita\n[x](y)\n| a | b |\n---\n1. uno"
        val markup = NoteMarkdown.parse(texto)
        markup.spans.forEach { span ->
            assertTrue(span.toString(), span.start in 0..texto.length)
            assertTrue(span.toString(), span.end in span.start..texto.length)
        }
        markup.markers.forEach { marca ->
            assertTrue(marca.toString(), marca.start in 0..texto.length)
            assertTrue(marca.toString(), marca.end in marca.start..texto.length)
        }
    }

    /*
     * Las casillas se marcan desde la lista, sin abrir la nota. Lo que se cambia es un solo
     * caracter dentro de los corchetes: reescribir la linea entera perderia la sangria.
     */
    @Test
    fun lasCasillasSeEncuentranConSuLineaYSuEstado() {
        val texto = "Pendientes\n- [ ] taller 3\ntexto suelto\n- [x] taller 2"
        val casillas = NoteMarkdown.checkboxes(texto)

        assertEquals(2, casillas.size)
        assertEquals(1, casillas[0].lineIndex)
        assertFalse(casillas[0].checked)
        assertEquals("taller 3", casillas[0].label)
        assertEquals(3, casillas[1].lineIndex)
        assertTrue(casillas[1].checked)
    }

    @Test
    fun marcarUnaCasillaSoloTocaSuCorchete() {
        val texto = "Pendientes\n- [ ] taller 3"
        assertEquals("Pendientes\n- [x] taller 3", NoteMarkdown.toggleCheckbox(texto, 1))
    }

    @Test
    fun volverATocarlaLaDesmarca() {
        assertEquals("- [ ] taller", NoteMarkdown.toggleCheckbox("- [x] taller", 0))
    }

    @Test
    fun laSangriaYElRestoDeLaNotaNoSeMueven() {
        val texto = "# Titulo\n\n    - [ ] con sangria\n\nfinal"
        val nuevo = NoteMarkdown.toggleCheckbox(texto, 2)
        assertEquals("# Titulo\n\n    - [x] con sangria\n\nfinal", nuevo)
    }

    @Test
    fun unaLineaQueNoEsCasillaNoSeToca() {
        assertNull(NoteMarkdown.toggleCheckbox("- solo una vineta", 0))
        assertNull(NoteMarkdown.toggleCheckbox("- [ ] taller", 5))
        assertNull(NoteMarkdown.toggleCheckbox("- [ ] taller", -1))
    }

    /** Esconder una marca nunca quita un salto de linea: la cuenta de lineas no cambia. */
    @Test
    fun quitarLasMarcasNoCambiaElNumeroDeLineas() {
        val texto = "# Titulo\n- [ ] uno\n> cita\n---\n**negrita**"
        assertEquals(texto.split("\n").size, NoteMarkdown.strip(texto).split("\n").size)
    }
}
