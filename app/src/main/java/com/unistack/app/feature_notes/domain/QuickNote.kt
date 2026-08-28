package com.unistack.app.feature_notes.domain

/**
 * Con qué reglas se lee el texto de una nota.
 *
 * Va guardado en cada nota y no solo en los ajustes porque el ajuste dice con qué **nacen** las
 * notas nuevas, no con qué se escribieron las viejas: cambiar la preferencia no puede reescribir
 * lo que ya está en el papel.
 */
enum class NoteFormat {
    /** Texto tal cual, sin marcas. */
    PLAIN,

    /** Markdown de apuntes, formateado al vuelo. */
    MARKDOWN
}

/**
 * Una nota suelta.
 *
 * Antes era una hoja única de cuatro mil caracteres guardada en las preferencias del teléfono,
 * fuera de la base de datos y fuera del respaldo. Ahora cada nota es una fila con su hora, su
 * materia opcional y su formato, y viaja en la copia de seguridad como todo lo demás.
 *
 * No hay campo de título: el título de una nota es su primera línea. Pedir uno aparte obliga a
 * bautizar cada apunte antes de escribirlo, que es justo el peaje que hace que no se apunte nada.
 */
/**
 * Con qué forma se enseña la lista de notas.
 *
 * Se probaron seis en un simulador y solo dos aguantaron con veinte notas dentro. Mosaico es el
 * de partida porque una nota suele empezar por una foto de la pizarra, y ahí la foto manda;
 * Cuaderno es para quien escribe más de lo que fotografía y quiere leer sin abrir.
 */
enum class NotesLayout {
    /** Dos columnas de altura libre. */
    MOSAICO,

    /** Una columna por días, con la fecha pegada arriba mientras se recorre. */
    CUADERNO
}

data class QuickNote(
    val id: String,
    val body: String,
    val subjectId: String? = null,
    val format: NoteFormat = NoteFormat.PLAIN,
    /** Arriba del todo, por encima de la fecha. Se guarda desde ya aunque aún no se pueda tocar. */
    val pinned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Lo que se puede decir de una nota sin abrirla.
 *
 * Vive aparte del modelo y sin dependencias de Android para que la lista, el diálogo de borrado
 * y las pruebas saquen el mismo texto de las mismas reglas.
 */
object NoteText {

    /** Tope por nota. Generoso para un apunte de clase, pero acotado: una fila no es un archivo. */
    const val MAX_LENGTH = 20_000

    private const val TITLE_MAX = 70

    /** La primera línea con algo escrito, que es como se llama una nota. */
    fun title(body: String): String {
        val first = body.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
        return if (first.length <= TITLE_MAX) first else first.take(TITLE_MAX).trimEnd() + "…"
    }

    /** Lo que va debajo del título en la tarjeta: el resto, sin líneas en blanco de más. */
    fun preview(body: String, maxLines: Int = 4): String {
        val lines = body.lines()
        val firstIndex = lines.indexOfFirst { it.isNotBlank() }
        if (firstIndex == -1) return ""
        return lines.drop(firstIndex + 1)
            .dropWhile { it.isBlank() }
            .take(maxLines)
            .joinToString(separator = "\n")
            .trimEnd()
    }

    /** Una nota sin nada escrito no se guarda: se descarta al salir. */
    fun isEmpty(body: String): Boolean = body.isBlank()

    /** Cómo se nombra una nota cuando hay que preguntar por ella («¿Borrar…?»). */
    fun label(body: String): String = title(body).ifBlank { "esta nota" }
}
