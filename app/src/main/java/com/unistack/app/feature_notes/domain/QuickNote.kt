package com.unistack.app.feature_notes.domain

/**
 * Con qué reglas se lee el texto de una nota.
 *
 * Va guardado en cada nota y no solo en los ajustes porque el ajuste dice con qué **nacen** las
 * notas nuevas, no con qué se escribieron las viejas: cambiar la preferencia no puede reescribir
 * lo que ya está en el papel.
 */
enum class NoteFormat {
    /** Texto tal cual, sin marcas. Es con lo que nacen las notas. */
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
/** Por qué fecha se ordenan las notas. */
enum class NotesSort {
    /** Lo último que se tocó, arriba. Es lo que se espera de un cuaderno. */
    MODIFICADA,

    /** Por cuándo se escribieron, para leer las cosas en el orden en que pasaron. */
    CREADA
}

enum class NotesLayout {
    /** Una columna por días, con la fecha pegada arriba mientras se recorre. */
    CUADERNO,

    /** Dos columnas de altura libre. */
    MOSAICO
}

data class QuickNote(
    val id: String,
    /**
     * El título, aparte del cuerpo.
     *
     * Antes el título era la primera línea del texto, que ahorra un campo y a cambio impide dos
     * cosas: escribir una nota que empiece por una lista sin que el primer punto haga de título,
     * y buscar solo por títulos. Es opcional: una nota sin título sigue siendo una nota.
     */
    val title: String = "",
    val body: String,
    val subjectId: String? = null,
    val format: NoteFormat = NoteFormat.PLAIN,
    /** Arriba del todo, por encima de la fecha. Se guarda desde ya aunque aún no se pueda tocar. */
    val pinned: Boolean = false,
    /**
     * Cuándo tiene que avisar esta nota, si tiene que avisar.
     *
     * Nulo es «no avisa». Es lo que convierte un apunte en algo que vuelve solo: «traer la
     * calculadora» no sirve de nada guardado si nadie lo lee la mañana del parcial.
     */
    val reminderAt: Long? = null,
    /** El color con el que se pinta la nota, o nulo para el de siempre. */
    val colorArgb: Int? = null,
    /**
     * Guardada, pero fuera de la lista.
     *
     * Es lo que hace falta para no tener que elegir entre borrar algo que ya no sirve y verlo
     * todos los días. El apunte de un parcial que ya pasó no es basura: es historia.
     */
    val archived: Boolean = false,
    /**
     * Cuándo se mandó a la papelera, o nulo si no está en ella.
     *
     * Borrar no borra: mueve. La fecha sirve para dos cosas —decir cuánto le queda y vaciarla
     * sola pasada una semana— y es lo que convierte un toque equivocado en algo reversible.
     */
    val deletedAt: Long? = null,
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
