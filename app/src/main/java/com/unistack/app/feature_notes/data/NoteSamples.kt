package com.unistack.app.feature_notes.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.QuickNote
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Una nota de ejemplo con lo que haga falta colgarle. */
data class SampleNote(val note: QuickNote, val attachments: List<NoteAttachment>)

/**
 * Notas de mentira para poder mirar las de verdad.
 *
 * Existe porque probar esta pantalla vacía no dice nada: hay que escribir ocho notas distintas,
 * con foto, con archivo y con audio, antes de poder juzgar si el mosaico se lee o si el cuaderno
 * respira. Eso es media hora de teclear cada vez que se cambia una separación.
 *
 * Las ocho son distintas a propósito y entre todas tocan **todo**: título, negrita, cursiva,
 * tachado, código en línea y en bloque, cita, línea, lista, lista numerada, casillas marcadas y
 * sin marcar, enlace, tabla, las dos maneras de escribir, con materia y sin ella, fijada, y los
 * tres tipos de adjunto.
 *
 * Los archivos se fabrican aquí: la foto se dibuja, el audio se sintetiza y el documento se
 * escribe. Nada viene de fuera, así que no hay que empaquetar nada en el APK ni pedir permisos.
 *
 * Solo se ofrece en las compilaciones que pueden abrir lo que está a medio hacer.
 */
object NoteSamples {

    private const val DIA = 24L * 60 * 60 * 1000

    fun build(store: NoteAttachmentStore, subjectIds: List<String>, now: Long): List<SampleNote> {
        // Se reparten las materias que haya. Con una sola, se repite; sin ninguna, todas van
        // sueltas, que también es un estado que hay que poder mirar.
        fun materia(indice: Int): String? =
            if (subjectIds.isEmpty()) null else subjectIds[indice % subjectIds.size]

        val muestras = mutableListOf<SampleNote>()

        // 1 · Fijada. Título, negrita, lista y casillas: la nota de estudiar.
        muestras += plano(
            id = "sample-parcial",
            subjectId = materia(0),
            pinned = true,
            updatedAt = now - 40 * 60 * 1000,
            body = """
                # Parcial 2
                **Martes 14, salón 302**, de 7 a 9. Traer calculadora y hoja de fórmulas.

                Temas que entran:
                - Derivadas implícitas
                - Regla de la cadena
                - *Todo* el capítulo 4

                - [x] Repasar los talleres
                - [ ] Hacer el simulacro
                - [ ] Preguntar lo de la página 212
            """.trimIndent()
        )

        // 2 · Con foto. Es la que sostiene el mosaico: la portada dice de qué va sin leer nada.
        val foto = pizarra(store)
        muestras += SampleNote(
            note = nota(
                id = "sample-pizarra",
                subjectId = materia(1),
                body = "Lo que quedó en la pizarra al final de la clase. La parte de abajo no la " +
                    "alcancé a copiar, hay que pedírsela a alguien.",
                format = NoteFormat.MARKDOWN,
                updatedAt = now - 3 * 60 * 60 * 1000
            ),
            attachments = listOfNotNull(
                foto?.let {
                    adjunto(
                        noteId = "sample-pizarra",
                        kind = AttachmentKind.IMAGE,
                        displayName = "Pizarra.jpg",
                        storedName = it.first,
                        mimeType = "image/jpeg",
                        sizeBytes = it.second,
                        createdAt = now - 3 * 60 * 60 * 1000
                    )
                }
            )
        )

        // 3 · Cita, enlace y un archivo colgado.
        val documento = documento(store)
        muestras += SampleNote(
            note = nota(
                id = "sample-taller",
                subjectId = materia(0),
                body = """
                    ## Taller 3
                    > Se entrega en parejas y se sustenta en clase.

                    El enunciado está en [el aula virtual](https://ejemplo.edu/taller3) y también
                    lo dejo aquí adjunto por si se cae la página.
                """.trimIndent(),
                format = NoteFormat.MARKDOWN,
                updatedAt = now - 26 * 60 * 60 * 1000
            ),
            attachments = listOfNotNull(
                documento?.let {
                    adjunto(
                        noteId = "sample-taller",
                        kind = AttachmentKind.FILE,
                        displayName = "Taller_3.txt",
                        storedName = it.first,
                        mimeType = "text/plain",
                        sizeBytes = it.second,
                        createdAt = now - 26 * 60 * 60 * 1000
                    )
                }
            )
        )

        // 4 · Con audio. Lo que se dice en clase y no da tiempo a escribir.
        val audio = grabacion(store)
        muestras += SampleNote(
            note = nota(
                id = "sample-voz",
                subjectId = materia(2),
                body = "Lo que dijo sobre la sustentación. Lo grabé porque hablaba muy rápido.",
                format = NoteFormat.PLAIN,
                updatedAt = now - 27 * 60 * 60 * 1000
            ),
            attachments = listOfNotNull(
                audio?.let {
                    adjunto(
                        noteId = "sample-voz",
                        kind = AttachmentKind.AUDIO,
                        displayName = "Grabación",
                        storedName = it.first,
                        mimeType = "audio/wav",
                        sizeBytes = it.second,
                        durationMillis = it.third,
                        createdAt = now - 27 * 60 * 60 * 1000
                    )
                }
            )
        )

        // 5 · Markdown a fondo: tabla, bloque de código, línea separadora.
        muestras += plano(
            id = "sample-calificacion",
            subjectId = materia(0),
            updatedAt = now - 2 * DIA,
            body = """
                ### Cómo se califica

                | Corte | Peso |
                | --- | --- |
                | Corte 1 | 30% |
                | Corte 2 | 30% |
                | Final | 40% |

                ---

                Para pasar con 3,0 en el final necesito:

                ```
                (3.0 - 0.3*n1 - 0.3*n2) / 0.4
                ```

                Ojo: el `20%` de la nota del corte 2 es asistencia.
            """.trimIndent()
        )

        // 6 · Escritura sencilla. El mismo texto guardado igual, con las marcas escondidas.
        muestras += SampleNote(
            note = nota(
                id = "sample-sencilla",
                subjectId = materia(1),
                body = "Cambió la entrega: ahora es el **viernes**, no el miércoles.\n" +
                    "~~Miércoles 12~~ y el formato es *libre*.",
                format = NoteFormat.PLAIN,
                updatedAt = now - 2 * DIA - 3 * 60 * 60 * 1000
            ),
            attachments = emptyList()
        )

        // 7 · Sin materia y de dos líneas: lo que se apunta de camino a la parada.
        muestras += plano(
            id = "sample-suelta",
            subjectId = null,
            updatedAt = now - 3 * DIA,
            body = "Renovar el carnet antes del 15. Llevar foto y el recibo de matrícula."
        )

        // 8 · Lista numerada, y una que ya no va.
        muestras += plano(
            id = "sample-pendientes",
            subjectId = materia(2),
            updatedAt = now - 6 * DIA,
            body = """
                Antes del lunes

                1. Leer el capítulo 4
                2. Terminar el laboratorio
                3. ~~Comprar el libro~~ me lo prestaron

                - [ ] Mandar el correo al monitor
            """.trimIndent()
        )

        /*
         * Cada adjunto acaba apuntando al identificador de verdad de su nota.
         *
         * Arriba se escribe el nombre base —«sample-pizarra»— porque el identificador real lleva
         * un sufijo al azar que no existe hasta que la nota esta hecha. Sin este paso, los datos
         * que devuelve esta funcion salen con el enlace roto y solo funcionan si quien los usa se
         * acuerda de arreglarlos, que es justo la clase de trampa que acaba costando una tarde.
         */
        return muestras.map { muestra ->
            muestra.copy(
                attachments = muestra.attachments.map { it.copy(noteId = muestra.note.id) }
            )
        }
    }

    private fun plano(
        id: String,
        subjectId: String?,
        body: String,
        updatedAt: Long,
        pinned: Boolean = false
    ) = SampleNote(
        note = nota(id, subjectId, body, NoteFormat.MARKDOWN, updatedAt, pinned),
        attachments = emptyList()
    )

    private fun nota(
        id: String,
        subjectId: String?,
        body: String,
        format: NoteFormat,
        updatedAt: Long,
        pinned: Boolean = false
    ) = QuickNote(
        id = id + "-" + UUID.randomUUID().toString().take(8),
        body = body,
        subjectId = subjectId,
        format = format,
        pinned = pinned,
        createdAt = updatedAt,
        updatedAt = updatedAt
    )

    private fun adjunto(
        noteId: String,
        kind: AttachmentKind,
        displayName: String,
        storedName: String,
        mimeType: String,
        sizeBytes: Long,
        createdAt: Long,
        durationMillis: Long? = null
    ) = NoteAttachment(
        id = "sample-att-" + UUID.randomUUID().toString().take(8),
        noteId = noteId,
        kind = kind,
        displayName = displayName,
        storedName = storedName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        durationMillis = durationMillis,
        createdAt = createdAt
    )

    /**
     * Una pizarra dibujada, y no una foto empaquetada en el APK.
     *
     * Pesa cero en el instalador y sale del mismo tamaño que una foto de verdad, que es lo que
     * hace falta para ver si la portada del mosaico recorta bien.
     */
    private fun pizarra(store: NoteAttachmentStore): Pair<String, Long>? = runCatching {
        val ancho = 1200
        val alto = 800
        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val lienzo = Canvas(bitmap)

        // design-tokens-ok: es una imagen dibujada, no interfaz: los colores son el contenido.
        lienzo.drawColor(android.graphics.Color.rgb(28, 42, 38))

        val tiza = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(226, 232, 226)
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val letra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(226, 232, 226)
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textSize = 66f
        }
        val floja = Paint(letra).apply {
            color = android.graphics.Color.argb(150, 226, 232, 226)
            textSize = 48f
        }

        lienzo.drawText("f(x) = 3x² - 2x + 1", 90f, 170f, letra)
        lienzo.drawLine(90f, 205f, 700f, 205f, tiza)
        lienzo.drawText("f'(x) = 6x - 2", 90f, 300f, letra)
        lienzo.drawText("f'(2) = 10", 90f, 400f, letra)
        lienzo.drawText("(regla de la cadena)", 90f, 480f, floja)
        lienzo.drawText("ejercicio 12 → queda de tarea", 90f, 570f, floja)
        lienzo.drawLine(90f, 640f, 1100f, 640f, tiza)
        lienzo.drawText("parcial: martes 14", 90f, 720f, floja)

        val bytes = ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, it)
        }.toByteArray()
        bitmap.recycle()

        val (nombre, archivo) = store.newFileFor("jpg")
        archivo.writeBytes(bytes)
        nombre to archivo.length()
    }.getOrNull()

    private fun documento(store: NoteAttachmentStore): Pair<String, Long>? = runCatching {
        val (nombre, archivo) = store.newFileFor("txt")
        archivo.writeText(
            """
            TALLER 3 — enunciado
            ====================

            1. Calcular la derivada de las siguientes funciones.
            2. Justificar cada paso.
            3. Entregar en parejas, sustentación en clase.

            Fecha de entrega: viernes.
            """.trimIndent()
        )
        nombre to archivo.length()
    }.getOrNull()

    /**
     * Un audio sintetizado: dos notas que suben y se apagan.
     *
     * Un WAV se escribe a mano en veinte líneas y lo reproduce cualquier cosa, así que sirve
     * para comprobar que el botón de escuchar hace lo que dice sin tener que grabar de verdad.
     */
    private fun grabacion(store: NoteAttachmentStore): Triple<String, Long, Long>? = runCatching {
        val muestreo = 16_000
        val duracionMs = 2_400L
        val total = (muestreo * duracionMs / 1000).toInt()
        val pcm = ByteArray(total * 2)

        for (i in 0 until total) {
            val t = i.toDouble() / muestreo
            val frecuencia = if (t < 1.2) 392.0 else 523.25
            val caida = exp(-1.6 * (t % 1.2))
            val valor = sin(2 * PI * frecuencia * t) * caida * 0.28
            val entero = (valor * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767)
            pcm[i * 2] = (entero and 0xFF).toByte()
            pcm[i * 2 + 1] = ((entero shr 8) and 0xFF).toByte()
        }

        val (nombre, archivo) = store.newFileFor("wav")
        archivo.outputStream().use { salida ->
            salida.write(cabeceraWav(pcm.size, muestreo))
            salida.write(pcm)
        }
        Triple(nombre, archivo.length(), duracionMs)
    }.getOrNull()

    private fun cabeceraWav(datos: Int, muestreo: Int): ByteArray {
        val bytesPorSegundo = muestreo * 2
        val cabecera = ByteArray(44)
        fun texto(pos: Int, valor: String) {
            valor.forEachIndexed { i, c -> cabecera[pos + i] = c.code.toByte() }
        }
        fun entero(pos: Int, valor: Int) {
            cabecera[pos] = (valor and 0xFF).toByte()
            cabecera[pos + 1] = ((valor shr 8) and 0xFF).toByte()
            cabecera[pos + 2] = ((valor shr 16) and 0xFF).toByte()
            cabecera[pos + 3] = ((valor shr 24) and 0xFF).toByte()
        }
        fun corto(pos: Int, valor: Int) {
            cabecera[pos] = (valor and 0xFF).toByte()
            cabecera[pos + 1] = ((valor shr 8) and 0xFF).toByte()
        }

        texto(0, "RIFF")
        entero(4, 36 + datos)
        texto(8, "WAVE")
        texto(12, "fmt ")
        entero(16, 16)
        corto(20, 1)
        corto(22, 1)
        entero(24, muestreo)
        entero(28, bytesPorSegundo)
        corto(32, 2)
        corto(34, 16)
        texto(36, "data")
        entero(40, datos)
        return cabecera
    }

    /** Los archivos que dejó una tanda de ejemplos, para poder borrarlos con ellas. */
    fun storedNamesOf(muestras: List<SampleNote>): List<String> =
        muestras.flatMap { it.attachments }.map { it.storedName }

    /** Si un archivo pertenece a una nota de ejemplo. */
    fun isSample(noteId: String): Boolean = noteId.startsWith("sample-")
}
