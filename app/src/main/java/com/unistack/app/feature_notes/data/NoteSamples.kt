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
import com.unistack.app.core.utils.cabeceraWav

/** Una nota de ejemplo con lo que haga falta colgarle. */
data class SampleNote(val note: QuickNote, val attachments: List<NoteAttachment>)

/**
 * Notas de mentira para poder mirar las de verdad.
 *
 * Existe porque probar esta pantalla vacía no dice nada: hay que escribir una docena de notas
 * distintas, con foto, con archivo y con audio, antes de poder juzgar si el mosaico se lee o si
 * el cuaderno respira. Eso es media hora de teclear cada vez que se cambia una separación.
 *
 * **Hay una de cada tipo, y entre todas tocan todo lo que una nota sabe hacer hoy**: título,
 * negrita, cursiva, tachado, código en línea y en bloque, cita, línea, lista, lista numerada,
 * casillas marcadas y sin marcar, enlace, tabla, las dos maneras de escribir, con materia y sin
 * ella, fijada, con color, con aviso por llegar y con aviso ya vencido, los tres tipos de
 * adjunto, **una con cuatro adjuntos a la vez** —que es la única forma de ver el carrusel—, una
 * **archivada** y una **en la papelera**.
 *
 * Los tres últimos estados son de lo añadido después de la primera tanda, y sin ellos había que
 * archivar y borrar a mano cada vez para mirar esas dos pantallas.
 *
 * Los archivos se fabrican aquí: la foto se dibuja, el audio se sintetiza y el documento se
 * escribe. Nada viene de fuera, así que no hay que empaquetar nada en el APK ni pedir permisos.
 *
 * Solo se ofrece en las compilaciones que pueden abrir lo que está a medio hacer.
 */
object NoteSamples {
    private const val DIA = 24L * 60 * 60 * 1000

    private val GALERIA_CUERPO = """
        Todo lo de la clase: las dos pizarras, lo que explico de viva voz y el enunciado que
        paso al final.

        La tasa se calcula sobre el **inductor**, no sobre horas maquina.
    """.trimIndent()

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
            title = "Parcial 2",
            pinned = true,
            reminderAt = now + 20 * 60 * 60 * 1000,
            colorArgb = 0xFF5A2E28.toInt(),
            updatedAt = now - 40 * 60 * 1000,
            body = """
                **Martes 14, salón 302**, de 7 a 9. Traer calculadora y hoja de fórmulas.

                ## Temas que entran
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
                title = "La pizarra del jueves",
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
                title = "Taller 3",
                body = """
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
                title = "Lo que dijo el profe",
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
            title = "Cómo se califica",
            colorArgb = 0xFF24414F.toInt(),
            updatedAt = now - 2 * DIA,
            body = """
                # El reparto

                | Corte | Peso |
                | --- | --- |
                | Corte 1 | 30% |
                | Corte 2 | 30% |
                | Final | 40% |

                ---

                ### La fórmula

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
                title = "Cambió la entrega",
                body = "Ahora es el **viernes**, no el miércoles.\n" +
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
            reminderAt = now + 3 * DIA,
            updatedAt = now - 3 * DIA,
            body = "Renovar el carnet antes del 15. Llevar foto y el recibo de matrícula."
        )

        // 8 · Lista numerada, y una que ya no va.
        muestras += plano(
            id = "sample-pendientes",
            subjectId = materia(2),
            title = "Antes del lunes",
            colorArgb = 0xFF3E4A28.toInt(),
            updatedAt = now - 6 * DIA,
            body = """
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
        // 9 - Una lista pura: todo casillas, para que se abra con el editor de lista.
        muestras += plano(
            id = "sample-mercado",
            subjectId = null,
            title = "Para el laboratorio",
            colorArgb = 0xFF23453C.toInt(),
            updatedAt = now - 5 * 60 * 60 * 1000,
            body = """
                - [ ] Bata
                - [ ] Guantes
                - [x] Gafas de seguridad
                - [ ] Marcador de vidrio
                - [x] Cuaderno de laboratorio
            """.trimIndent()
        )

        // 10 - Varios adjuntos en una nota: dos fotos, el audio y el archivo. Es la que
        // ensena el carrusel de adjuntos, que con uno solo no se despliega.
        // La nota se construye **antes** que sus adjuntos: `nota()` le pone un sufijo al azar
        // al identificador, asi que colgar los adjuntos del nombre base los deja huerfanos.
        val galeria = nota(
            id = "sample-galeria",
            subjectId = materia(1),
            title = "Clase entera del jueves",
            body = GALERIA_CUERPO,
            format = NoteFormat.MARKDOWN,
            updatedAt = now - 2 * 60 * 60 * 1000
        )
        val galeriaId = galeria.id
        val galeriaAdjuntos = mutableListOf<NoteAttachment>()
        pizarra(store, variante = 0)?.let { (nombre, peso) ->
            galeriaAdjuntos += adjunto(
                galeriaId, AttachmentKind.IMAGE, "pizarra-1.jpg", nombre,
                "image/jpeg", peso, now - 2 * 60 * 60 * 1000
            )
        }
        pizarra(store, variante = 1)?.let { (nombre, peso) ->
            galeriaAdjuntos += adjunto(
                galeriaId, AttachmentKind.IMAGE, "pizarra-2.jpg", nombre,
                "image/jpeg", peso, now - 2 * 60 * 60 * 1000
            )
        }
        grabacion(store)?.let { (nombre, peso, duracion) ->
            galeriaAdjuntos += adjunto(
                galeriaId, AttachmentKind.AUDIO, "explicacion.wav", nombre,
                "audio/wav", peso, now - 2 * 60 * 60 * 1000, duracion
            )
        }
        documento(store)?.let { (nombre, peso) ->
            galeriaAdjuntos += adjunto(
                galeriaId, AttachmentKind.FILE, "enunciado.txt", nombre,
                "text/plain", peso, now - 2 * 60 * 60 * 1000
            )
        }
        if (galeriaAdjuntos.isNotEmpty()) {
            muestras += SampleNote(note = galeria, attachments = galeriaAdjuntos)
        }

        // 11 - Con el aviso ya vencido, para ver la ficha de fecha en rojo en la tarjeta.
        muestras += plano(
            id = "sample-vencida",
            subjectId = materia(2),
            title = "Entregar el formato",
            reminderAt = now - 5 * 60 * 60 * 1000,
            updatedAt = now - 2 * DIA,
            body = "Se entregaba ayer en secretaria. Preguntar si todavia lo reciben."
        )

        // 12 - Archivada: guardada, pero fuera de la lista. Solo se ve entrando a Archivo.
        muestras += plano(
            id = "sample-archivada",
            subjectId = materia(0),
            title = "Parcial 1 (ya paso)",
            archived = true,
            updatedAt = now - 21 * DIA,
            body = """
                Lo que entro en el primer parcial, por si sirve para el final.

                - Limites y continuidad
                - Derivadas basicas
            """.trimIndent()
        )

        // 13 - En la papelera, borrada hace dos dias: le quedan cinco antes de irse sola.
        muestras += plano(
            id = "sample-papelera",
            subjectId = null,
            title = "Lista vieja del super",
            deletedAt = now - 2 * DIA,
            updatedAt = now - 2 * DIA,
            body = """
                - [x] Cafe
                - [x] Cuaderno
                - [ ] Marcadores
            """.trimIndent()
        )

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
        title: String = "",
        pinned: Boolean = false,
        reminderAt: Long? = null,
        colorArgb: Int? = null,
        archived: Boolean = false,
        deletedAt: Long? = null
    ) = SampleNote(
        note = nota(
            id = id,
            subjectId = subjectId,
            body = body,
            format = NoteFormat.MARKDOWN,
            updatedAt = updatedAt,
            title = title,
            pinned = pinned,
            reminderAt = reminderAt,
            colorArgb = colorArgb,
            archived = archived,
            deletedAt = deletedAt
        ),
        attachments = emptyList()
    )

    private fun nota(
        id: String,
        subjectId: String?,
        body: String,
        format: NoteFormat,
        updatedAt: Long,
        title: String = "",
        pinned: Boolean = false,
        reminderAt: Long? = null,
        colorArgb: Int? = null,
        archived: Boolean = false,
        deletedAt: Long? = null
    ) = QuickNote(
        id = id + "-" + UUID.randomUUID().toString().take(8),
        title = title,
        body = body,
        subjectId = subjectId,
        format = format,
        pinned = pinned,
        reminderAt = reminderAt,
        colorArgb = colorArgb,
        archived = archived,
        deletedAt = deletedAt,
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
    private fun pizarra(store: NoteAttachmentStore, variante: Int = 0): Pair<String, Long>? = runCatching {
        val ancho = 1200
        val alto = 800
        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val lienzo = Canvas(bitmap)

        // design-tokens-ok: es una imagen dibujada, no interfaz: los colores son el contenido.
        lienzo.drawColor(
            if (variante == 0) {
                android.graphics.Color.rgb(28, 42, 38)
            } else {
                android.graphics.Color.rgb(30, 34, 52)
            }
        )

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

        if (variante == 0) {
            lienzo.drawText("f(x) = 3x² - 2x + 1", 90f, 170f, letra)
            lienzo.drawLine(90f, 205f, 700f, 205f, tiza)
            lienzo.drawText("f'(x) = 6x - 2", 90f, 300f, letra)
            lienzo.drawText("f'(2) = 10", 90f, 400f, letra)
            lienzo.drawText("(regla de la cadena)", 90f, 480f, floja)
            lienzo.drawText("ejercicio 12 → queda de tarea", 90f, 570f, floja)
            lienzo.drawLine(90f, 640f, 1100f, 640f, tiza)
            lienzo.drawText("parcial: martes 14", 90f, 720f, floja)
        } else {
            lienzo.drawText("Costeo por actividades", 90f, 160f, letra)
            lienzo.drawLine(90f, 195f, 820f, 195f, tiza)
            lienzo.drawText("tasa = CIF / inductor", 90f, 290f, letra)
            lienzo.drawText("CIF = 4.800.000", 90f, 380f, floja)
            lienzo.drawText("inductor = 12.000 h", 90f, 450f, floja)
            lienzo.drawText("tasa = 400 / hora", 90f, 545f, letra)
            lienzo.drawLine(90f, 610f, 1100f, 610f, tiza)
            lienzo.drawText("ojo: no son horas máquina", 90f, 700f, floja)
        }

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

    /** Si un archivo pertenece a una nota de ejemplo. */
    fun isSample(noteId: String): Boolean = noteId.startsWith("sample-")
}
