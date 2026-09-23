package com.unistack.app.feature_rooms.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.unistack.app.core.utils.nombreVisibleDe
import com.unistack.app.feature_notes.domain.Attachments
import java.io.File
import java.util.UUID

/** Un archivo de una sala ya copiado dentro de la app. */
data class RoomStoredFile(val storedName: String, val displayName: String, val mimeType: String, val sizeBytes: Long)

/**
 * Los archivos de Trabajos (partes subidas, aportes, material, fotos del chat), copiados dentro.
 *
 * Igual que los adjuntos de tareas: guardar sólo la dirección de la galería deja un hueco el día
 * que se limpia. Carpeta propia (`room_files`) y, cuando llegue la nube, esto es lo que se sube.
 */
class RoomFileStore(private val context: Context) {
    private val root: File
        get() = File(context.filesDir, CARPETA).apply { if (!exists()) mkdirs() }

    fun file(storedName: String): File = File(root, storedName)

    fun import(uri: Uri): RoomStoredFile? {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri).orEmpty().ifBlank { "application/octet-stream" }
        val nombre = context.nombreVisibleDe(uri)
        val storedName = newStoredName(Attachments.extensionFor(mime, nombre))
        val destino = File(root, storedName)
        val copiados = runCatching {
            resolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida ->
                    var total = 0L
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val leidos = entrada.read(buffer)
                        if (leidos <= 0) break
                        total += leidos
                        if (total > Attachments.MAX_BYTES) return@use -1L
                        salida.write(buffer, 0, leidos)
                    }
                    total
                }
            }
        }.getOrNull()
        if (copiados == null || copiados < 0) {
            destino.delete()
            return null
        }
        return RoomStoredFile(storedName, nombre?.takeIf { it.isNotBlank() } ?: storedName, mime, copiados)
    }

    /** Un archivo vacío para que la cámara o el grabador escriban ahí. */
    fun newFileFor(extension: String): Pair<String, File> {
        val storedName = newStoredName(extension)
        return storedName to File(root, storedName)
    }

    fun shareUri(storedName: String): Uri? = runCatching {
        FileProvider.getUriForFile(context, context.packageName + ".provider", file(storedName))
    }.getOrNull()

    /** Lee el texto de un .txt/.md; de un Word o PDF no se puede sin librerías, así que null. */
    fun readText(storedName: String, mime: String): String? =
        if (mime.startsWith("text/")) runCatching { file(storedName).readText().take(60_000) }.getOrNull() else null

    /** Cuántas diapositivas trae: las cuenta dentro del .pptx o por páginas en un PDF. */
    fun countSlides(storedName: String, mime: String): Int = runCatching {
        val f = file(storedName)
        when {
            mime.contains("presentation") || f.name.endsWith(".pptx") -> java.util.zip.ZipFile(f).use { z ->
                z.entries().asSequence().count { Regex("""ppt/slides/slide\d+\.xml""").matches(it.name) }
            }
            mime.contains("pdf") -> android.os.ParcelFileDescriptor.open(f, android.os.ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
                android.graphics.pdf.PdfRenderer(fd).use { it.pageCount }
            }
            else -> 0
        }
    }.getOrDefault(0)

    fun delete(storedName: String) {
        runCatching { file(storedName).delete() }
    }

    private fun newStoredName(extension: String) = "room-" + UUID.randomUUID() + "." + extension

    private companion object {
        const val CARPETA = "room_files"
    }
}
