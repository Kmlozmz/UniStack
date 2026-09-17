package com.unistack.app.feature_profile.presentation

import android.content.Context
import android.net.Uri
import com.unistack.app.R
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_notes.data.NoteAttachmentStore
import com.unistack.app.feature_notes.domain.Attachments
import com.unistack.app.feature_tasks.data.TaskAttachmentStore
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * La copia completa en un solo archivo: los datos y los archivos que cuelgan de ellos.
 *
 * El JSON solo lleva la ficha de cada adjunto. Guardado suelto, reinstalar la app —que borra
 * todo lo que tiene dentro— dejaba las notas con el nombre de su foto y sin la foto, y el perfil
 * sin retrato. Así que la copia es un zip: el JSON de siempre y, al lado, los adjuntos de notas
 * y tareas y el retrato.
 *
 * Restaurar sigue aceptando los JSON sueltos de antes: se distinguen por los cuatro primeros
 * bytes, que en un zip son siempre los mismos.
 */
internal object BackupArchive {
    const val MIME = "application/zip"
    const val EXTENSION = "zip"
    const val PREFIX = "unistack-copia"

    private const val DATOS = "unistack-copia.json"
    private const val NOTAS = "adjuntos/notas/"
    private const val TAREAS = "adjuntos/tareas/"
    private const val RETRATO = "retrato/"

    fun write(context: Context, uri: Uri, json: String): Result<Unit> = runCatching {
        // «wt» trunca lo que hubiera: sin la t, sobrescribir un archivo más largo deja la cola
        // del anterior pegada al final y el zip queda ilegible.
        context.contentResolver.openOutputStream(uri, "wt")?.use { output -> write(context, output, json) }
            ?: error(Textos.get(R.string.backup_write_failed))
    }

    /**
     * Deja la copia en la caché, lista para compartir.
     *
     * Antes se borran las que quedaron de otras veces: con las fotos dentro, cada una puede
     * pesar varios megas, y la caché no se vacía sola hasta que el sistema anda justo de sitio.
     */
    fun writeForSharing(context: Context, json: String): Result<File> = runCatching {
        context.cacheDir.listFiles { file -> file.name.startsWith(PREFIX) }?.forEach { it.delete() }
        val file = File(context.cacheDir, BackupFiles.suggestedName(PREFIX, EXTENSION))
        file.outputStream().use { output -> write(context, output, json) }
        file
    }

    fun write(context: Context, output: OutputStream, json: String) {
        val root = JSONObject(json)
        val notas = NoteAttachmentStore(context)
        val tareas = TaskAttachmentStore(context)
        ZipOutputStream(output.buffered()).use { zip ->
            zip.putNextEntry(ZipEntry(DATOS))
            zip.write(json.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            storedNames(root.optJSONArray("notes")).forEach { nombre ->
                zip.addFile(NOTAS + nombre, notas.file(nombre))
            }
            storedNames(root.optJSONArray("tasks")).forEach { nombre ->
                zip.addFile(TAREAS + nombre, tareas.file(nombre))
            }
            photoName(root)?.let { nombre -> zip.addFile(RETRATO + nombre, File(context.filesDir, nombre)) }
        }
    }

    /** El JSON de la copia, venga dentro de un zip o suelto como los de antes. */
    fun readJson(context: Context, uri: Uri): Result<String> = runCatching {
        context.contentResolver.openInputStream(uri)?.use(::readJson)
            ?: error(Textos.get(R.string.backup_read_failed))
    }

    fun readJson(input: InputStream): String {
        val entrada = BufferedInputStream(input)
        if (!entrada.startsLikeZip()) return entrada.bufferedReader().readText()
        ZipInputStream(entrada).use { zip ->
            while (true) {
                val item = zip.nextEntry ?: break
                if (item.name == DATOS) return zip.readBytes().toString(Charsets.UTF_8)
            }
        }
        error(Textos.get(R.string.backup_read_failed))
    }

    /**
     * Devuelve a su sitio los archivos que la copia nombra, y el retrato si traía uno.
     *
     * Solo se escribe lo que el JSON de la copia nombra, y sin carpetas en el nombre: un «../»
     * no puede sacar nada de donde viven los adjuntos. Lo que ya está no se toca, porque cada
     * nombre lleva un UUID y el mismo nombre es el mismo archivo.
     *
     * @return la dirección del retrato devuelto, para ponerlo en el perfil; nulo si no había.
     */
    fun restoreFiles(context: Context, uri: Uri, json: String): String? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { input -> restoreFiles(context, input, json) }
    }.getOrNull()

    fun restoreFiles(context: Context, input: InputStream, json: String): String? {
        val entrada = BufferedInputStream(input)
        if (!entrada.startsLikeZip()) return null
        val root = JSONObject(json)
        val deNotas = storedNames(root.optJSONArray("notes"))
        val deTareas = storedNames(root.optJSONArray("tasks"))
        val retrato = photoName(root)
        val notas = NoteAttachmentStore(context)
        val tareas = TaskAttachmentStore(context)
        var retratoDevuelto: String? = null

        ZipInputStream(entrada).use { zip ->
            while (true) {
                val item = zip.nextEntry ?: break
                val nombre = item.name
                when {
                    nombre.startsWith(NOTAS) -> nombre.removePrefix(NOTAS)
                        .takeIf { it in deNotas }
                        ?.let { zip.extractTo(notas.file(it)) }
                    nombre.startsWith(TAREAS) -> nombre.removePrefix(TAREAS)
                        .takeIf { it in deTareas }
                        ?.let { zip.extractTo(tareas.file(it)) }
                    retrato != null && nombre == RETRATO + retrato -> {
                        val destino = File(context.filesDir, retrato)
                        if (zip.extractTo(destino)) retratoDevuelto = Uri.fromFile(destino).toString()
                    }
                }
            }
        }
        return retratoDevuelto
    }

    /** Los nombres guardados de los adjuntos de un bloque: notas o tareas. */
    private fun storedNames(array: JSONArray?): Set<String> {
        if (array == null) return emptySet()
        return (0 until array.length())
            .mapNotNull { array.optJSONObject(it)?.optJSONArray("attachments") }
            .flatMap { adjuntos -> (0 until adjuntos.length()).mapNotNull { adjuntos.optJSONObject(it) } }
            .map { it.optString("storedName") }
            .filter(::isPlainName)
            .toSet()
    }

    private fun photoName(root: JSONObject): String? {
        val perfil = root.optJSONObject("profile") ?: return null
        if (perfil.isNull("photoFile")) return null
        return perfil.optString("photoFile").takeIf { isPlainName(it) && ProfilePhotoFiles.isCropName(it) }
    }

    /** Un nombre de archivo a secas: sin carpetas y sin puntos que suban de nivel. */
    private fun isPlainName(name: String): Boolean =
        name.isNotBlank() && '/' !in name && '\\' !in name && name != "." && name != ".."

    private fun ZipOutputStream.addFile(entryName: String, file: File) {
        if (!file.isFile) return
        putNextEntry(ZipEntry(entryName))
        file.inputStream().use { it.copyTo(this) }
        closeEntry()
    }

    /**
     * Copia la entrada actual a [destino], si no estaba ya.
     *
     * Se escribe a un archivo aparte y se renombra al final: una copia cortada a medias —sin
     * sitio, o un zip dañado— no puede dejar un adjunto partido con el nombre del bueno.
     */
    private fun ZipInputStream.extractTo(destino: File): Boolean {
        if (destino.exists()) return true
        val parte = File(destino.parentFile, destino.name + ".parte")
        val completa = runCatching {
            parte.outputStream().use { salida ->
                var total = 0L
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (true) {
                    val leidos = read(buffer)
                    if (leidos <= 0) break
                    total += leidos
                    if (total > Attachments.MAX_BYTES) return@use false
                    salida.write(buffer, 0, leidos)
                }
                true
            }
        }.getOrDefault(false)
        if (!completa || !parte.renameTo(destino)) {
            parte.delete()
            return false
        }
        return true
    }

    private fun BufferedInputStream.startsLikeZip(): Boolean {
        mark(ZIP_MAGIC.size)
        val cabecera = ByteArray(ZIP_MAGIC.size)
        var leidos = 0
        while (leidos < cabecera.size) {
            val n = read(cabecera, leidos, cabecera.size - leidos)
            if (n <= 0) break
            leidos += n
        }
        reset()
        return leidos == ZIP_MAGIC.size && cabecera.contentEquals(ZIP_MAGIC)
    }

    private val ZIP_MAGIC = byteArrayOf(0x50, 0x4B, 0x03, 0x04)
}
