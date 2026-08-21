package com.unistack.app.feature_profile.presentation

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * El retrato, copiado dentro de la app.
 *
 * El selector de fotos devuelve un `content://` con permiso de lectura que dura lo que dure el
 * proceso: guardarlo tal cual deja el retrato en blanco la próxima vez que se abre la app. Y
 * persistir el permiso tampoco basta, porque la foto sigue viviendo en la galería y desaparece
 * de aquí si se borra de allí. Una copia de unos cientos de kilobytes evita las dos cosas.
 *
 * Cada copia estrena nombre y borra las anteriores. Reusar el nombre parecía más limpio, pero
 * el cargador de imágenes guarda en caché por ruta: cambiar de foto dejaba la vieja en pantalla
 * hasta reiniciar. Con un nombre nuevo cada vez, la caché no tiene con qué confundirse.
 */
internal object ProfilePhotoFiles {

    private const val PREFIX = "profile-photo-"
    private const val SUFFIX = ".jpg"

    /**
     * Copia la imagen elegida y devuelve su ruta como URI de archivo.
     *
     * Devuelve `null` si no se pudo leer el origen: no hay nada que decirle a nadie más allá de
     * que el retrato no cambió, y dejar a medias el archivo sería peor que no tenerlo.
     */
    suspend fun copyIn(context: Context, source: Uri): String? = withContext(Dispatchers.IO) {
        val destination = File(context.filesDir, PREFIX + System.currentTimeMillis() + SUFFIX)
        val copied = runCatching {
            context.contentResolver.openInputStream(source)?.use { input ->
                destination.outputStream().use { output -> input.copyTo(output) }
            } ?: return@runCatching false
            true
        }.getOrDefault(false)

        if (!copied || destination.length() == 0L) {
            destination.delete()
            return@withContext null
        }
        clear(context, keep = destination.name)
        Uri.fromFile(destination).toString()
    }

    /** Borra las copias guardadas, menos la que se acaba de escribir. */
    fun clear(context: Context, keep: String? = null) {
        runCatching {
            context.filesDir.listFiles { file ->
                file.name.startsWith(PREFIX) && file.name != keep
            }?.forEach { it.delete() }
        }
    }
}
