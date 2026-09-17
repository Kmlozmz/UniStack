package com.unistack.app.feature_tasks.data

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.Attachments
import java.io.File
import java.util.UUID
import com.unistack.app.core.utils.nombreVisibleDe

/** Lo que queda de un archivo después de copiarlo dentro. Igual que `StoredFile` de Notas. */
data class TaskStoredFile(
    val storedName: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val kind: AttachmentKind
)

/**
 * Los adjuntos de una tarea, copiados dentro de UniStack.
 *
 * Mismo motivo que `NoteAttachmentStore`: una tarea que solo guarda la dirección de la galería
 * se queda con un hueco el día que se limpia la galería. Carpeta propia (`task_attachments`)
 * para no mezclar los archivos de una feature con los de otra.
 */
class TaskAttachmentStore(private val context: Context) {
    private val root: File
        get() = File(context.filesDir, CARPETA).apply { if (!exists()) mkdirs() }

    fun file(storedName: String): File = File(root, storedName)

    fun exists(storedName: String): Boolean = file(storedName).exists()

    fun import(uri: Uri, hintName: String? = null): TaskStoredFile? {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri).orEmpty().ifBlank { "application/octet-stream" }
        val nombre = hintName ?: context.nombreVisibleDe(uri)
        val kind = Attachments.kindFor(mime)
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

        return TaskStoredFile(
            storedName = storedName,
            displayName = nombre?.takeIf { it.isNotBlank() }
                ?: Attachments.fallbackName(kind, System.currentTimeMillis()),
            mimeType = mime,
            sizeBytes = copiados,
            kind = kind
        )
    }

    /** Un archivo vacío dentro de la app, para que la cámara o el grabador escriban ahí. */
    fun newFileFor(extension: String): Pair<String, File> {
        val storedName = newStoredName(extension)
        return storedName to File(root, storedName)
    }

    /** La dirección prestada con la que otra app puede leer un adjunto. */
    fun shareUri(storedName: String): Uri = FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file(storedName)
    )

    fun delete(storedName: String) {
        runCatching { file(storedName).delete() }
    }

    fun deleteAll(storedNames: List<String>) {
        storedNames.forEach(::delete)
    }

    private fun newStoredName(extension: String): String = "task-" + UUID.randomUUID() + "." + extension

    private companion object {
        const val CARPETA = "task_attachments"
    }
}
