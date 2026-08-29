package com.unistack.app.feature_notes.domain

/** Una línea de una lista: lo que dice y si está marcada. */
data class ChecklistItem(val text: String, val checked: Boolean)

/**
 * Una nota que es una lista, y no un texto con casillas dentro.
 *
 * La diferencia importa porque se editan distinto. Un texto con una casilla suelta se escribe
 * seguido; una lista de doce pendientes se escribe línea a línea, se reordena y se tacha, y
 * hacerlo dentro de un campo de texto obliga a colocar el cursor a mano en cada renglón.
 *
 * No hay columna que lo diga: se deduce del contenido. Una nota es una lista cuando **todo** lo
 * que tiene escrito son casillas. Así nadie tiene que elegir un tipo al crearla —que es lo que
 * él pidió quitar— y una nota deja de ser lista en cuanto se le escribe un párrafo.
 *
 * Por dentro sigue siendo el mismo Markdown de siempre: `- [ ] algo`. Eso es lo que permite que
 * la tarjeta de la lista, el buscador y el respaldo no se enteren de nada.
 */
object NoteChecklist {

    private const val SIN_MARCAR = "- [ ] "
    private const val MARCADA = "- [x] "

    /** Si esta nota se edita como lista. Una nota vacía todavía no lo es. */
    fun isChecklist(body: String): Boolean {
        val lineas = body.lines().filter { it.isNotBlank() }
        if (lineas.isEmpty()) return false
        return lineas.size == NoteMarkdown.checkboxes(body).size
    }

    /** Las líneas de la nota como elementos, en su orden. */
    fun parse(body: String): List<ChecklistItem> =
        NoteMarkdown.checkboxes(body).map { ChecklistItem(it.label, it.checked) }

    /**
     * De vuelta a texto.
     *
     * Los elementos vacíos se caen: una lista con renglones en blanco entre medias es lo que
     * queda cuando alguien borra el texto de uno y no se acuerda de quitarlo.
     */
    fun render(items: List<ChecklistItem>): String = items
        .filter { it.text.isNotBlank() }
        .joinToString(separator = "\n") { (if (it.checked) MARCADA else SIN_MARCAR) + it.text.trim() }

    /**
     * Convertir una nota corriente en lista.
     *
     * Cada línea con algo escrito pasa a ser un elemento; lo que ya era casilla se queda como
     * estaba, marcada o no.
     */
    fun from(body: String): List<ChecklistItem> {
        if (body.isBlank()) return listOf(ChecklistItem("", false))
        val casillas = NoteMarkdown.checkboxes(body).associateBy { it.lineIndex }
        val plano = NoteMarkdown.strip(body).lines()
        return body.lines().mapIndexedNotNull { indice, _ ->
            val casilla = casillas[indice]
            if (casilla != null) return@mapIndexedNotNull ChecklistItem(casilla.label, casilla.checked)
            val texto = plano.getOrNull(indice)?.trim().orEmpty()
            if (texto.isBlank()) null else ChecklistItem(texto, false)
        }.ifEmpty { listOf(ChecklistItem("", false)) }
    }

    /** Mueve un elemento de sitio, dejando la lista con los mismos que tenía. */
    fun move(items: List<ChecklistItem>, from: Int, to: Int): List<ChecklistItem> {
        if (from !in items.indices || to !in items.indices || from == to) return items
        return items.toMutableList().also { it.add(to, it.removeAt(from)) }
    }
}
