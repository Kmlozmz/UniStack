package com.unistack.app.feature_profile.presentation

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.content.edit
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

private const val BACKUP_PREFS = "unistack_backup_history"
private const val LAST_BACKUP_AT = "last_backup_at"

/**
 * Guardar y leer copias como archivos.
 *
 * La copia se sacaba copiando un JSON al portapapeles y se restauraba pegándolo en un campo de
 * texto. Eso no es una copia de seguridad: el portapapeles se pierde al copiar cualquier otra
 * cosa, tiene un tope de tamaño que nadie anuncia —y una base entera lo pasa— y no hay dónde
 * dejar el archivo. Con el selector del sistema, la copia acaba donde el usuario quiera y se
 * restaura eligiéndola.
 */
object BackupFiles {

    fun suggestedName(prefix: String, extension: String): String {
        val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm"))
        return "$prefix-$stamp.$extension"
    }

    fun writeText(context: Context, uri: Uri, text: String): Result<Unit> = runCatching {
        // «wt» trunca lo que hubiera: sin la t, sobrescribir un archivo más largo deja la cola
        // del anterior pegada al final y el JSON queda ilegible.
        context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
            output.write(text.toByteArray())
        } ?: error(Textos.get(R.string.backup_write_failed))
    }

    /** Deja el texto en un archivo de la caché y abre el selector para compartirlo. */
    fun shareText(context: Context, fileName: String, mimeType: String, text: String): Result<Unit> =
        runCatching {
            val file = File(context.cacheDir, fileName)
            file.writeText(text)
            shareFile(context, file, mimeType).getOrThrow()
        }

    fun shareFile(context: Context, file: File, mimeType: String): Result<Unit> = runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            // La dirección va también en el ClipData para que la hoja de compartir pueda leer el
            // archivo: solo con el extra sale sin vista previa, y en algunos teléfonos ni se abre.
            // Es lo que le pasaba al boletín del histórico.
            clipData = ClipData.newRawUri(file.name, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, Textos.get(R.string.settings_backup_btn_share))
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    /** El nombre del archivo elegido, para que se vea cuál se va a restaurar. */
    fun displayName(context: Context, uri: Uri): String {
        val fromProvider = runCatching {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
            }
        }.getOrNull()
        return fromProvider ?: uri.lastPathSegment?.substringAfterLast('/') ?: (Textos.get(R.string.backup_archivo_elegido))
    }

    fun rememberBackupDone(context: Context) {
        context.applicationContext
            .getSharedPreferences(BACKUP_PREFS, Context.MODE_PRIVATE)
            .edit { putLong(LAST_BACKUP_AT, System.currentTimeMillis()) }
    }

    fun lastBackupAt(context: Context): Long? {
        val stored = context.applicationContext
            .getSharedPreferences(BACKUP_PREFS, Context.MODE_PRIVATE)
            .getLong(LAST_BACKUP_AT, 0L)
        return stored.takeIf { it > 0L }
    }
}
