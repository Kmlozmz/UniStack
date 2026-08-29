package com.unistack.app.feature_notes.domain

/** Lo que se ha tecleado tras una arroba, y dónde está en el texto. */
data class MentionQuery(val start: Int, val end: Int, val token: String)

/**
 * Vincular una materia escribiendo `@`, sin soltar el teclado.
 *
 * Era `#` en la primera vuelta y hubo que cambiarlo: `#` es un título en Markdown, así que
 * escribir `#Calculo` al principio de una línea creaba un titular en vez de vincular nada. La
 * arroba no choca con ninguna marca.
 *
 * Al aceptar, la arroba y lo tecleado **se van**. La materia queda vinculada y se ve en su sitio,
 * así que escribir el nombre dentro del texto lo diría dos veces. Se probó al revés y molestaba:
 * el nombre aparecía a mitad de frase sin haberlo pedido.
 *
 * Al quitarla se limpia el espacio que deja: si no, cada arroba iría dejando un hueco doble.
 */
object NoteMention {

    /** Hasta dónde se busca hacia atrás. Una materia no se llama con cuarenta letras. */
    private const val MAX_TOKEN = 40

    /**
     * La mención que se está escribiendo justo donde está el cursor, si la hay.
     *
     * Solo cuenta si la arroba está pegada al principio de una palabra: un correo escrito dentro
     * de una nota —«profe@uni.edu»— no puede abrir el selector de materias a media palabra.
     */
    fun at(text: String, cursor: Int): MentionQuery? {
        if (cursor !in 0..text.length) return null

        var i = cursor - 1
        var recorrido = 0
        while (i >= 0 && recorrido <= MAX_TOKEN) {
            val c = text[i]
            if (c == '@') {
                val anterior = if (i == 0) null else text[i - 1]
                if (anterior != null && !anterior.isWhitespace()) return null
                return MentionQuery(start = i, end = cursor, token = text.substring(i + 1, cursor))
            }
            if (c.isWhitespace()) return null
            i--
            recorrido++
        }
        return null
    }

    /** El texto sin la mención, y dónde queda el cursor. */
    fun accept(text: String, mention: MentionQuery): TextChange {
        val inicio = mention.start.coerceIn(0, text.length)
        var fin = mention.end.coerceIn(inicio, text.length)

        // El espacio que queda detrás se va con ella, y si no había nada delante, también el de
        // antes: quitar «@cal» de «en @cal vimos» tiene que dejar «en vimos», no «en  vimos».
        if (fin < text.length && text[fin] == ' ') fin += 1
        var desde = inicio
        if (fin >= text.length && desde > 0 && text[desde - 1] == ' ') desde -= 1

        val nuevo = text.removeRange(desde, fin)
        return TextChange(nuevo, desde, desde)
    }
}
