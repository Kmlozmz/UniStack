package com.unistack.app.feature_notes.domain

/** Lo que puede hacer la barra flotante del modo sencillo. */
enum class NoteAction(val label: String) {
    NEGRITA("Negrita"),
    CURSIVA("Cursiva"),
    TACHADO("Tachado"),
    VINETA("Lista"),
    NUMERADA("Lista numerada"),
    CASILLA("Casilla")
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
        }
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
