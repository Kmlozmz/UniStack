package com.unistack.app.feature_notes.domain

import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/** Lo que puede hacer la barra flotante del modo sencillo. */
enum class NoteAction(val labelRes: Int) {
    NEGRITA(R.string.note_action_bold),
    CURSIVA(R.string.note_action_italic),
    TACHADO(R.string.note_action_strike),
    VINETA(R.string.note_action_list),
    NUMERADA(R.string.note_action_numbered),
    CASILLA(R.string.note_action_checkbox),
    TITULO1(R.string.note_action_title),
    TITULO2(R.string.note_action_subtitle),
    NORMAL(R.string.note_action_normal),
    LIMPIAR(R.string.note_action_clear),
    TABLA(R.string.note_action_table);

    /** El nombre en el idioma de la app. */
    val label: String get() = Textos.get(labelRes)
}

/** Un texto y donde queda la seleccion despues de tocarlo. */
data class TextChange(val text: String, val selectionStart: Int, val selectionEnd: Int)

/**
 * Poner y quitar formato desde la barra, sin escribir marcas a mano.
 *
 * Las dos maneras de escribir una nota guardan lo mismo —Markdown— y solo cambia si las marcas
 * se ven o no. Por eso esta barra no es un segundo formato: escribe los mismos asteriscos que
 * escribiria alguien a mano, y quien luego cambie esa nota a Markdown se encuentra su texto
 * entero, sin conversion y sin nada que se pierda por el camino.
 *
 * Cada boton es un interruptor: sobre un texto que ya esta en negrita, quita la negrita. Sin eso
 * la unica forma de deshacer seria borrar unos caracteres que en este modo ni siquiera se ven.
 */
object NoteFormatting {

    private const val NEGRITA = "**"
    private const val CURSIVA = "*"
    private const val TACHADO = "~~"

    fun apply(action: NoteAction, text: String, selectionStart: Int, selectionEnd: Int): TextChange {
        val inicio = minOf(selectionStart, selectionEnd).coerceIn(0, text.length)
        val fin = maxOf(selectionStart, selectionEnd).coerceIn(0, text.length)
        return when (action) {
            NoteAction.NEGRITA -> envolver(text, inicio, fin, NEGRITA)
            NoteAction.CURSIVA -> envolver(text, inicio, fin, CURSIVA)
            NoteAction.TACHADO -> envolver(text, inicio, fin, TACHADO)
            NoteAction.VINETA -> prefijar(text, inicio, fin) { "- " }
            NoteAction.CASILLA -> prefijar(text, inicio, fin) { "- [ ] " }
            NoteAction.NUMERADA -> prefijar(text, inicio, fin) { indice -> "${indice + 1}. " }
            NoteAction.TITULO1 -> titular(text, inicio, fin, "# ")
            NoteAction.TITULO2 -> titular(text, inicio, fin, "## ")
            NoteAction.NORMAL -> titular(text, inicio, fin, "")
            NoteAction.LIMPIAR -> limpiar(text, inicio, fin)
            NoteAction.TABLA -> insertarTabla(text, inicio)
        }
    }

    private val TITULO_RE = Regex("""^#{1,6}[ @T]+""".replace("@T", "\t"))

    /**
     * Poner, cambiar o quitar el nivel de título de una línea.
     *
     * Es un cambio y no un interruptor: pedir «Título» sobre una línea que ya es subtítulo la
     * convierte en título, no le añade otra almohadilla delante. Con marca vacía, la deja lisa.
     */
    private fun titular(text: String, inicio: Int, fin: Int, marca: String): TextChange {
        val lineaInicio = text.lastIndexOf(10.toChar(), (inicio - 1).coerceAtLeast(0))
            .let { if (it == -1 || inicio == 0) 0 else it + 1 }
        val lineaFin = text.indexOf(10.toChar(), fin).let { if (it == -1) text.length else it }

        val linea = text.substring(lineaInicio, lineaFin)
        val sangria = linea.takeWhile { it == ' ' }.length
        val resto = linea.substring(sangria)
        val actual = TITULO_RE.find(resto)?.value.orEmpty()
        val yaEsta = actual == marca && marca.isNotEmpty()

        val nueva = linea.substring(0, sangria) +
            (if (yaEsta) "" else marca) +
            resto.removePrefix(actual)

        val nuevo = text.replaceRange(lineaInicio, lineaFin, nueva)
        val desplazamiento = nueva.length - linea.length
        return TextChange(
            text = nuevo,
            selectionStart = (inicio + desplazamiento).coerceIn(0, nuevo.length),
            selectionEnd = (fin + desplazamiento).coerceIn(0, nuevo.length)
        )
    }

    /**
     * Dejar el trozo seleccionado en texto pelado.
     *
     * Es el botón que hace falta cuando algo se puso con la mano y no se sabe qué marca lleva:
     * en vez de adivinar cuál quitar, se quitan todas las de dentro.
     */
    private fun limpiar(text: String, inicio: Int, fin: Int): TextChange {
        if (inicio == fin) return TextChange(text, inicio, fin)
        val trozo = text.substring(inicio, fin)
        val limpio = NoteMarkdown.strip(trozo)
        val nuevo = text.replaceRange(inicio, fin, limpio)
        return TextChange(nuevo, inicio, (inicio + limpio.length).coerceAtMost(nuevo.length))
    }

    /**
     * Una tabla ya hecha, para no tener que acordarse de los guiones.
     *
     * Escribir la línea de separación a mano es lo que hace que casi nadie use tablas en
     * Markdown. Sale con dos columnas y una fila: es más fácil añadir que recortar.
     */
    private fun insertarTabla(text: String, cursor: Int): TextChange {
        val salto = 10.toChar()
        val delante = if (cursor > 0 && text.getOrNull(cursor - 1) != salto) salto.toString() else ""
        val tabla = delante +
            "| Columna | Columna |" + salto +
            "| --- | --- |" + salto +
            "|  |  |" + salto
        val nuevo = text.replaceRange(cursor, cursor, tabla)
        val destino = cursor + delante.length + 2
        return TextChange(nuevo, destino, destino)
    }

    private fun envolver(text: String, inicio: Int, fin: Int, marca: String): TextChange {
        val yaPuesta = inicio >= marca.length &&
            fin + marca.length <= text.length &&
            text.regionMatches(inicio - marca.length, marca, 0, marca.length) &&
            text.regionMatches(fin, marca, 0, marca.length)

        if (yaPuesta) {
            val nuevo = text.removeRange(fin, fin + marca.length)
                .removeRange(inicio - marca.length, inicio)
            return TextChange(nuevo, inicio - marca.length, fin - marca.length)
        }

        val nuevo = buildString {
            append(text, 0, inicio)
            append(marca)
            append(text, inicio, fin)
            append(marca)
            append(text, fin, text.length)
        }
        // Sin nada seleccionado, el cursor se queda en medio de las dos marcas listo para
        // escribir; con texto seleccionado, la seleccion sigue siendo la misma de antes.
        return TextChange(nuevo, inicio + marca.length, fin + marca.length)
    }

    private fun prefijar(
        text: String,
        inicio: Int,
        fin: Int,
        prefijo: (Int) -> String
    ): TextChange {
        val lineaInicio = text.lastIndexOf('\n', (inicio - 1).coerceAtLeast(0))
            .let { if (it == -1 || inicio == 0) 0 else it + 1 }
        val lineaFin = text.indexOf('\n', fin).let { if (it == -1) text.length else it }

        val bloque = text.substring(lineaInicio, lineaFin)
        val lineas = bloque.split("\n")

        val puestos = lineas.mapIndexed { indice, linea -> prefijo(indice) }
        val todasLoTienen = lineas.withIndex().all { (indice, linea) ->
            linea.isBlank() || linea.startsWith(puestos[indice]) || tienePrefijoEquivalente(linea, puestos[indice])
        }

        val nuevoBloque = lineas.mapIndexed { indice, linea ->
            if (todasLoTienen) quitarPrefijo(linea, puestos[indice]) else puestos[indice] + linea
        }.joinToString("\n")

        val nuevo = text.replaceRange(lineaInicio, lineaFin, nuevoBloque)
        val desplazamiento = nuevoBloque.length - bloque.length
        return TextChange(
            text = nuevo,
            selectionStart = inicio.coerceAtMost(nuevo.length),
            selectionEnd = (fin + desplazamiento).coerceIn(0, nuevo.length)
        )
    }

    /*
     * Una lista numerada renumerada no lleva el mismo prefijo en cada linea, asi que «ya lo
     * tiene» no puede compararse letra a letra: basta con que la linea empiece por un numero
     * seguido de punto.
     */
    private fun tienePrefijoEquivalente(linea: String, prefijo: String): Boolean {
        if (!prefijo.firstOrNull()?.isDigit().orFalse()) return false
        return Regex("""^\d{1,3}[.)][ \t]+""").containsMatchIn(linea)
    }

    private fun quitarPrefijo(linea: String, prefijo: String): String {
        if (linea.startsWith(prefijo)) return linea.removePrefix(prefijo)
        val numerado = Regex("""^\d{1,3}[.)][ \t]+""").find(linea)
        if (numerado != null) return linea.removeRange(numerado.range)
        return linea
    }

    private fun Boolean?.orFalse(): Boolean = this ?: false
}
