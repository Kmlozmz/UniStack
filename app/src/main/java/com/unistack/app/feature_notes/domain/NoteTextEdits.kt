package com.unistack.app.feature_notes.domain

/** Un tramo del original que se enseña de otra manera, o no se enseña. */
data class TextEdit(val start: Int, val end: Int, val replacement: String)

/**
 * El texto tal y como se ve, y las dos cuentas para ir de una version a la otra.
 *
 * Existe porque el modo sencillo esconde las marcas: lo que se lee tiene menos caracteres que lo
 * que hay guardado, y sin embargo el cursor, la seleccion y cada toque en la pantalla se dan en
 * el texto de verdad. Si las dos cuentas no cuadran, el cursor salta solo —o peor: Compose
 * exige que las posiciones caigan dentro del texto y revienta si una se sale—.
 *
 * Por eso viven aqui, sin Compose delante, y con una prueba que recorre **todas** las posiciones
 * de un texto de ejemplo comprobando que ninguna se sale y que ninguna retrocede.
 */
class EditedText(
    val text: String,
    private val originalLength: Int,
    private val edits: List<TextEdit>
) {
    fun toTransformed(offset: Int): Int {
        val o = offset.coerceIn(0, originalLength)
        var delta = 0
        for (edit in edits) {
            if (edit.end <= o) {
                delta += edit.replacement.length - (edit.end - edit.start)
                continue
            }
            // Dentro de un tramo escondido no hay sitio donde poner el cursor: se va al final
            // de lo que se enseña en su lugar, que es donde la persona espera seguir escribiendo.
            if (edit.start < o) return (edit.start + delta + edit.replacement.length).coerceIn(0, text.length)
            break
        }
        return (o + delta).coerceIn(0, text.length)
    }

    fun toOriginal(offset: Int): Int {
        val t = offset.coerceIn(0, text.length)
        var delta = 0
        for (edit in edits) {
            val inicio = edit.start + delta
            val fin = inicio + edit.replacement.length
            if (fin <= t) {
                delta += edit.replacement.length - (edit.end - edit.start)
                continue
            }
            if (inicio < t) return edit.end.coerceIn(0, originalLength)
            break
        }
        return (t - delta).coerceIn(0, originalLength)
    }
}

object NoteTextEdits {

    /**
     * Aplica los tramos y devuelve el texto resultante con sus dos cuentas.
     *
     * Los tramos que se pisan se descartan en vez de recortarse: un solapamiento significa que
     * el analisis se equivoco, y recortarlo a medias produciria un texto que no es ni el de
     * antes ni el de despues.
     */
    fun apply(text: String, edits: List<TextEdit>): EditedText {
        val limpios = mutableListOf<TextEdit>()
        var ultimoFin = 0
        for (edit in edits.sortedBy { it.start }) {
            if (edit.start < ultimoFin) continue
            if (edit.start < 0 || edit.end > text.length || edit.start > edit.end) continue
            limpios += edit
            ultimoFin = edit.end
        }

        if (limpios.isEmpty()) return EditedText(text, text.length, emptyList())

        val salida = StringBuilder(text.length)
        var cursor = 0
        for (edit in limpios) {
            salida.append(text, cursor, edit.start)
            salida.append(edit.replacement)
            cursor = edit.end
        }
        salida.append(text, cursor, text.length)

        return EditedText(salida.toString(), text.length, limpios)
    }

    /** Los tramos que esconde el modo sencillo: las marcas, con su simbolo si lo tienen. */
    fun hidingEdits(markup: NoteMarkup): List<TextEdit> =
        markup.markers.map { TextEdit(it.start, it.end, it.replacement) }
}
