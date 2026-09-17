package com.unistack.app.feature_notes.domain

/** Cada cosa que el texto de una nota puede llegar a ser. */
enum class NoteStyle {
    TITULO1,
    TITULO2,
    TITULO3,
    NEGRITA,
    CURSIVA,
    TACHADO,
    CODIGO,
    BLOQUE_CODIGO,
    CITA,
    VINETA,
    NUMERO,
    CASILLA,
    CASILLA_HECHA,
    LINEA,
    ENLACE,
    TABLA
}

/** Una casilla dentro de una nota: en que linea esta, si esta marcada y que dice. */
data class NoteCheckbox(val lineIndex: Int, val checked: Boolean, val label: String)

/** Un tramo del texto que se pinta de una manera. */
data class NoteSpan(val style: NoteStyle, val start: Int, val end: Int)

/**
 * Un tramo que es marca y no contenido: los asteriscos, las almohadillas, el guion de la lista.
 *
 * [replacement] es lo que se enseña en su lugar cuando las marcas se esconden. Vacio significa
 * que desaparece; para la vineta y la casilla hay un simbolo, porque una lista sin nada delante
 * deja de leerse como lista.
 */
data class NoteMarker(val start: Int, val end: Int, val replacement: String = "")

/** Todo lo que hace falta saber del texto de una nota para pintarlo. */
data class NoteMarkup(
    val spans: List<NoteSpan> = emptyList(),
    val markers: List<NoteMarker> = emptyList()
)

/**
 * El Markdown de apuntes, leido una sola vez para los tres sitios que lo necesitan.
 *
 * El editor lo usa para formatear al vuelo mientras se escribe, la lista para enseñar la nota
 * sin las marcas, y el modo sencillo para esconderlas del todo. Vive en `domain` y sin nada de
 * Compose a proposito: asi se puede probar caracter a caracter sin encender un telefono, que es
 * justo lo que hace falta cuando lo que se calcula son posiciones dentro de un texto.
 *
 * Es el subconjunto que se decidio: titulos hasta tres, negrita, cursiva, tachado, codigo en
 * linea y en bloque, citas, lineas separadoras, listas, listas numeradas, casillas, enlaces y
 * tablas. Fuera quedan las imagenes por URL, el HTML crudo y las notas al pie.
 */
object NoteMarkdown {
    private val TITULO = Regex("""^(#{1,6})[ \t]+""")
    private val CITA = Regex("""^>[ \t]?""")
    private val CASILLA_RE = Regex("""^[-*+][ \t]+\[([ xX])][ \t]+""")
    private val VINETA_RE = Regex("""^[-*+][ \t]+""")
    private val NUMERO_RE = Regex("""^(\d{1,3})[.)][ \t]+""")
    private val LINEA_RE = Regex("""^(-{3,}|\*{3,}|_{3,})[ \t]*$""")
    private val VALLA = Regex("""^```""")

    private val CODIGO_RE = Regex("""`([^`\n]+)`""")
    private val NEGRITA_RE = Regex("""\*\*([^*\n]+)\*\*|__([^_\n]+)__""")
    private val CURSIVA_RE = Regex("""\*([^*\n]+)\*|_([^_\n]+)_""")
    private val TACHADO_RE = Regex("""~~([^~\n]+)~~""")
    private val ENLACE_RE = Regex("""\[([^\]\n]*)]\(([^)\s]+)\)""")

    /** El simbolo que sustituye al guion de una lista cuando las marcas se esconden. */
    const val VINETA_SIMBOLO = "• "
    const val CASILLA_SIMBOLO = "☐ "
    const val CASILLA_HECHA_SIMBOLO = "☑ "
    const val LINEA_SIMBOLO = "──────────"

    fun parse(text: String): NoteMarkup {
        val spans = mutableListOf<NoteSpan>()
        val markers = mutableListOf<NoteMarker>()
        val tomado = BooleanArray(text.length)

        var offset = 0
        var dentroDeValla = false

        for (linea in text.split("\n")) {
            val inicio = offset
            offset += linea.length + 1

            val sangria = linea.takeWhile { it == ' ' || it == '\t' }.length
            val resto = linea.substring(sangria)
            val restoInicio = inicio + sangria
            val finLinea = inicio + linea.length

            /*
             * Las vallas de codigo mandan sobre todo lo demas.
             *
             * Dentro de un bloque, un asterisco es un asterisco y una almohadilla es una
             * almohadilla: quien pega ahi un trozo de codigo no quiere que la mitad se le ponga
             * en negrita.
             */
            if (VALLA.containsMatchIn(resto)) {
                markers += NoteMarker(restoInicio, finLinea)
                dentroDeValla = !dentroDeValla
                continue
            }
            if (dentroDeValla) {
                if (finLinea > inicio) spans += NoteSpan(NoteStyle.BLOQUE_CODIGO, inicio, finLinea)
                continue
            }

            if (LINEA_RE.matches(resto)) {
                spans += NoteSpan(NoteStyle.LINEA, restoInicio, finLinea)
                markers += NoteMarker(restoInicio, finLinea, LINEA_SIMBOLO)
                continue
            }

            var contenidoInicio = restoInicio

            val titulo = TITULO.find(resto)
            val casilla = CASILLA_RE.find(resto)
            val vineta = if (casilla == null) VINETA_RE.find(resto) else null
            val numero = NUMERO_RE.find(resto)
            val cita = CITA.find(resto)

            when {
                titulo != null -> {
                    val nivel = titulo.groupValues[1].length
                    val estilo = when (nivel) {
                        1 -> NoteStyle.TITULO1
                        2 -> NoteStyle.TITULO2
                        else -> NoteStyle.TITULO3
                    }
                    contenidoInicio = restoInicio + titulo.value.length
                    markers += NoteMarker(restoInicio, contenidoInicio)
                    spans += NoteSpan(estilo, contenidoInicio, finLinea)
                }

                casilla != null -> {
                    val hecha = casilla.groupValues[1].lowercase() == "x"
                    contenidoInicio = restoInicio + casilla.value.length
                    markers += NoteMarker(
                        restoInicio,
                        contenidoInicio,
                        if (hecha) CASILLA_HECHA_SIMBOLO else CASILLA_SIMBOLO
                    )
                    spans += NoteSpan(
                        if (hecha) NoteStyle.CASILLA_HECHA else NoteStyle.CASILLA,
                        contenidoInicio,
                        finLinea
                    )
                }

                vineta != null -> {
                    contenidoInicio = restoInicio + vineta.value.length
                    markers += NoteMarker(restoInicio, contenidoInicio, VINETA_SIMBOLO)
                    spans += NoteSpan(NoteStyle.VINETA, contenidoInicio, finLinea)
                }

                numero != null -> {
                    // El numero se queda a la vista tambien en sencillo: quitarlo dejaria una
                    // lista numerada sin numeros, que es una lista a secas.
                    contenidoInicio = restoInicio + numero.value.length
                    spans += NoteSpan(NoteStyle.NUMERO, restoInicio, contenidoInicio)
                }

                cita != null -> {
                    contenidoInicio = restoInicio + cita.value.length
                    markers += NoteMarker(restoInicio, contenidoInicio)
                    spans += NoteSpan(NoteStyle.CITA, contenidoInicio, finLinea)
                }

                resto.startsWith("|") && resto.count { it == '|' } >= 2 -> {
                    spans += NoteSpan(NoteStyle.TABLA, restoInicio, finLinea)
                }
            }

            if (contenidoInicio < finLinea) {
                parseInline(text, contenidoInicio, finLinea, tomado, spans, markers)
            }
        }

        return NoteMarkup(
            spans = spans.sortedBy { it.start },
            markers = markers.sortedBy { it.start }
        )
    }

    /**
     * Las marcas de dentro de una linea, en el orden que evita que se pisen.
     *
     * El codigo va primero porque dentro de un `trozo asi` nada mas cuenta, y la negrita antes
     * que la cursiva porque `**` empieza por `*`: al reves, un texto en negrita saldria en
     * cursiva con dos asteriscos sueltos a los lados.
     */
    private fun parseInline(
        text: String,
        inicio: Int,
        fin: Int,
        tomado: BooleanArray,
        spans: MutableList<NoteSpan>,
        markers: MutableList<NoteMarker>
    ) {
        val trozo = text.substring(inicio, fin)

        fun libre(desde: Int, hasta: Int): Boolean =
            (desde until hasta).none { tomado[it] }

        fun ocupar(desde: Int, hasta: Int) {
            for (i in desde until hasta) tomado[i] = true
        }

        fun aplicar(regex: Regex, estilo: NoteStyle, marca: Int) {
            for (m in regex.findAll(trozo)) {
                val desde = inicio + m.range.first
                val hasta = inicio + m.range.last + 1
                if (!libre(desde, hasta)) continue
                ocupar(desde, hasta)
                spans += NoteSpan(estilo, desde + marca, hasta - marca)
                markers += NoteMarker(desde, desde + marca)
                markers += NoteMarker(hasta - marca, hasta)
            }
        }

        aplicar(CODIGO_RE, NoteStyle.CODIGO, marca = 1)

        for (m in ENLACE_RE.findAll(trozo)) {
            val desde = inicio + m.range.first
            val hasta = inicio + m.range.last + 1
            if (!libre(desde, hasta)) continue
            ocupar(desde, hasta)
            val textoInicio = desde + 1
            val textoFin = textoInicio + m.groupValues[1].length
            spans += NoteSpan(NoteStyle.ENLACE, textoInicio, textoFin)
            markers += NoteMarker(desde, textoInicio)
            markers += NoteMarker(textoFin, hasta)
        }

        aplicar(NEGRITA_RE, NoteStyle.NEGRITA, marca = 2)
        aplicar(TACHADO_RE, NoteStyle.TACHADO, marca = 2)
        aplicar(CURSIVA_RE, NoteStyle.CURSIVA, marca = 1)
    }

    /**
     * Las casillas de una nota, en el orden en que aparecen.
     *
     * Devuelve por cada una en que linea esta y si esta marcada. La linea es lo que hace falta
     * para poder cambiarla: el indice de la casilla dentro de la lista se mueve si alguien
     * escribe otra por encima, el numero de linea no.
     */
    fun checkboxes(text: String): List<NoteCheckbox> {
        val salida = mutableListOf<NoteCheckbox>()
        text.split("\n").forEachIndexed { indice, linea ->
            val sangria = linea.takeWhile { it == ' ' || it == '\t' }.length
            val casilla = CASILLA_RE.find(linea.substring(sangria)) ?: return@forEachIndexed
            salida += NoteCheckbox(
                lineIndex = indice,
                checked = casilla.groupValues[1].lowercase() == "x",
                label = linea.substring(sangria + casilla.value.length)
            )
        }
        return salida
    }

    /**
     * Marca o desmarca la casilla de una linea.
     *
     * Devuelve el texto nuevo, o nulo si esa linea no era una casilla. Se cambia **solo** el
     * caracter de dentro de los corchetes: reescribir la linea entera perderia la sangria y
     * cualquier espacio que alguien hubiera puesto a mano.
     */
    fun toggleCheckbox(text: String, lineIndex: Int): String? {
        val lineas = text.split("\n")
        if (lineIndex !in lineas.indices) return null
        val linea = lineas[lineIndex]
        val sangria = linea.takeWhile { it == ' ' || it == '\t' }.length
        val casilla = CASILLA_RE.find(linea.substring(sangria)) ?: return null

        val dentro = sangria + casilla.value.indexOf('[') + 1
        val marcada = casilla.groupValues[1].lowercase() == "x"
        val nuevaLinea = linea.substring(0, dentro) +
            (if (marcada) " " else "x") +
            linea.substring(dentro + 1)

        return lineas.toMutableList().also { it[lineIndex] = nuevaLinea }.joinToString("\n")
    }

    /**
     * El texto sin marcas, que es lo que se enseña en la lista.
     *
     * Una tarjeta con `## Parcial 2` escrito tal cual delata el formato en el sitio donde menos
     * importa: quien mira la lista viene a acordarse de que hay parcial, no a leer Markdown.
     */
    fun strip(text: String): String {
        val markers = parse(text).markers
        if (markers.isEmpty()) return text
        val salida = StringBuilder(text.length)
        var cursor = 0
        for (marker in markers) {
            if (marker.start < cursor) continue
            salida.append(text, cursor, marker.start)
            salida.append(marker.replacement)
            cursor = marker.end
        }
        salida.append(text, cursor, text.length)
        return salida.toString()
    }
}
