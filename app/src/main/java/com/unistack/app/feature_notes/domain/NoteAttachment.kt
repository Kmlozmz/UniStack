package com.unistack.app.feature_notes.domain

import java.util.Locale
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/** Las tres cosas que se pueden colgar de una nota. */
enum class AttachmentKind {
    /** Una foto: de la galería o hecha en el momento. */
    IMAGE,

    /** Un PDF, un documento, lo que sea que llegue del selector de archivos. */
    FILE,

    /** Una grabación hecha desde la propia nota. */
    AUDIO
}

/**
 * Algo colgado de una nota.
 *
 * [storedName] es el nombre del archivo **dentro** de UniStack, y no la dirección de donde vino.
 * Es lo que se decidió y tiene una razón concreta: una nota que apunta a la galería se queda en
 * blanco el día que se limpia la galería, y eso pasa. Copiar ocupa más y sobrevive.
 *
 * [displayName] es el nombre que se enseña, que sí es el original: «Taller_3.pdf» dice más que
 * el nombre interno, que es un identificador y no se le enseña a nadie.
 */
data class NoteAttachment(
    val id: String,
    val noteId: String,
    val kind: AttachmentKind,
    val displayName: String,
    val storedName: String,
    val mimeType: String,
    val sizeBytes: Long,
    /** Solo el audio la tiene. */
    val durationMillis: Long? = null,
    val createdAt: Long
)

object Attachments {

    /** Lo que aguanta un adjunto antes de que copiarlo deje de tener sentido. */
    const val MAX_BYTES = 25L * 1024 * 1024

    /** Cuántos caben en una nota. No es un límite técnico: es que la tarjeta deja de leerse. */
    const val MAX_PER_NOTE = 12

    /**
     * De qué tipo es lo que llega, mirando su tipo MIME.
     *
     * Se decide aquí y no en quien lo elige porque el selector de archivos también devuelve
     * imágenes: quien busca «Taller_3.pdf» y de paso toca una foto espera que se vea como foto.
     */
    fun kindFor(mimeType: String?): AttachmentKind {
        val mime = mimeType.orEmpty().lowercase(Locale.ROOT)
        return when {
            mime.startsWith("image/") -> AttachmentKind.IMAGE
            mime.startsWith("audio/") -> AttachmentKind.AUDIO
            else -> AttachmentKind.FILE
        }
    }

    /**
     * El tamaño en cristiano.
     *
     * En kilobytes hasta que deja de caber y en megabytes a partir de ahí, con un decimal: «1,4
     * MB» se entiende de un vistazo y «1468006 bytes» no dice nada.
     */
    fun formatSize(bytes: Long): String {
        val kb = 1024.0
        val mb = kb * 1024
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < mb -> "${(bytes / kb).toInt()} KB"
            else -> String.format(Locale("es"), "%.1f MB", bytes / mb)
        }
    }

    /** La duración de una grabación, en minutos y segundos. */
    fun formatDuration(millis: Long): String {
        val total = (millis / 1000).coerceAtLeast(0)
        val minutos = total / 60
        val segundos = total % 60
        return String.format(Locale("es"), "%d:%02d", minutos, segundos)
    }

    /**
     * El nombre con el que se enseña algo que llega sin nombre.
     *
     * La cámara y el grabador no dan ninguno, y «content://media/external/…» no es un nombre.
     */
    fun fallbackName(kind: AttachmentKind, createdAt: Long): String = when (kind) {
        AttachmentKind.IMAGE -> Textos.get(R.string.attachment_photo)
        AttachmentKind.AUDIO -> Textos.get(R.string.attachment_recording)
        AttachmentKind.FILE -> Textos.get(R.string.attachment_file)
    }

    /** La extensión que le toca a lo que se guarda, para que el sistema sepa abrirlo luego. */
    fun extensionFor(mimeType: String?, displayName: String?): String {
        val delNombre = displayName?.substringAfterLast('.', "")?.takeIf { it.isNotBlank() && it.length <= 5 }
        if (delNombre != null) return delNombre.lowercase(Locale.ROOT)
        return when (mimeType?.lowercase(Locale.ROOT)) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            "audio/mp4", "audio/m4a" -> "m4a"
            "application/pdf" -> "pdf"
            else -> "bin"
        }
    }
}
