package com.unistack.app.feature_notes.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.Attachments
import java.io.File
import java.util.UUID

/** Lo que queda de un archivo después de copiarlo dentro. */
data class StoredFile(
    val storedName: String,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val kind: AttachmentKind
)

/**
 * Los adjuntos, copiados dentro de UniStack.
 *
 * Guardar la dirección de la galería habría sido gratis, y por eso casi todo el mundo lo hace.
 * Aquí no: una nota que apunta a la galería se queda en blanco el día que se limpia la galería,
 * o el día que se revoca el permiso, o cuando la foto viene de una app que ya se desinstaló. Una
 * copia ocupa lo que ocupa y sigue estando.
 *
 * Viven en el almacenamiento privado de la app, así que nadie más las ve y desinstalar se las
 * lleva. Para abrir una con otra app se presta por el proveedor, con permiso de solo lectura y
 * por el rato que dure.
 */
class NoteAttachmentStore(private val context: Context) {

    private val root: File
        get() = File(context.filesDir, CARPETA).apply { if (!exists()) mkdirs() }

    fun file(storedName: String): File = File(root, storedName)

    fun exists(storedName: String): Boolean = file(storedName).exists()

    /**
     * Copia lo que hay en [uri] dentro de la app.
     *
     * Devuelve nulo si no se puede leer o si pasa del tamaño máximo. Lo segundo se comprueba
     * mientras se copia y no antes: el tamaño que declara el proveedor no siempre está, y una
     * copia a medias de un archivo de doscientos megas hay que poder cortarla.
     */
    fun import(uri: Uri, hintName: String? = null): StoredFile? {
        val resolver = context.contentResolver
        val mime = resolver.getType(uri).orEmpty().ifBlank { "application/octet-stream" }
        val nombre = hintName ?: queryDisplayName(uri)
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

        return StoredFile(
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

    private fun newStoredName(extension: String): String = "note-" + UUID.randomUUID() + "." + extension

    private fun queryDisplayName(uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val indice = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (indice >= 0 && cursor.moveToFirst()) cursor.getString(indice) else null
            }
    }.getOrNull()

    private companion object {
        const val CARPETA = "note_attachments"
    }
}
