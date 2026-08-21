package com.unistack.app.feature_profile.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * El retrato, copiado dentro de la app y recortado a lo que se eligió ver.
 *
 * El selector de fotos devuelve un `content://` con permiso de lectura que dura lo que dure el
 * proceso: guardarlo tal cual deja el retrato en blanco la próxima vez que se abre la app. Y
 * persistir el permiso tampoco basta, porque la foto sigue viviendo en la galería y desaparece
 * de aquí si se borra de allí. Una copia de unos cientos de kilobytes evita las dos cosas.
 *
 * Se guardan dos archivos por retrato: el recorte, que es el que se pinta, y el original tal
 * como se eligió. Sin el original, volver a ajustar el encuadre partiría del recorte anterior y
 * cada retoque perdería un poco más de foto.
 *
 * Cada retrato estrena nombre y borra los anteriores. Reusar el nombre parecía más limpio, pero
 * el cargador de imágenes guarda en caché por ruta: cambiar de foto dejaba la vieja en pantalla
 * hasta reiniciar. Con un nombre nuevo cada vez, la caché no tiene con qué confundirse.
 */
internal object ProfilePhotoFiles {

    private const val CROP = "profile-photo-"
    private const val SOURCE = "profile-source-"
    private const val SUFFIX = ".jpg"

    /** Lo que mide como mucho la copia que se edita, por lado. */
    private const val EDIT_MAX = 1400

    /** Lo que mide el recorte guardado. Un avatar nunca se pinta más grande. */
    private const val OUTPUT = 512

    /**
     * Abre la imagen a un tamaño con el que se pueda trabajar.
     *
     * Las fotos de un móvil actual pasan de los cincuenta megapíxeles: cargarlas enteras para
     * pintar un círculo de trescientos píxeles es la forma más rápida de quedarse sin memoria.
     * `inSampleSize` deja que el decodificador salte píxeles mientras lee, sin materializar
     * nunca la imagen completa.
     */
    fun decodeForEditing(context: Context, source: Uri): Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        val mayor = max(bounds.outWidth, bounds.outHeight)
        if (mayor <= 0) return@runCatching null

        var muestra = 1
        while (mayor / muestra > EDIT_MAX) muestra *= 2

        val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
        context.contentResolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, opciones)
        }
    }.getOrNull()

    /**
     * Guarda el trozo que quedó dentro del círculo.
     *
     * Las cuentas van al revés que en pantalla: allí la imagen se estira para cubrir el cuadro y
     * luego se mueve; aquí se deshace eso para saber qué rectángulo de la imagen original
     * quedaba visible. `base` es el estiramiento que la hace cubrir el cuadro, `mostrado` el que
     * de verdad se aplicó contando el zoom, y el lado visible es el cuadro dividido por él.
     */
    suspend fun saveCrop(
        context: Context,
        source: Uri,
        image: Bitmap,
        frame: Int,
        zoom: Float,
        offset: Offset
    ): String? = withContext(Dispatchers.IO) {
        if (frame <= 0) return@withContext null

        val base = max(frame.toFloat() / image.width, frame.toFloat() / image.height)
        val mostrado = base * zoom
        val lado = (frame / mostrado).roundToInt().coerceAtMost(min(image.width, image.height))
        val centroX = image.width / 2f - offset.x / mostrado
        val centroY = image.height / 2f - offset.y / mostrado
        val izquierda = (centroX - lado / 2f).roundToInt().coerceIn(0, image.width - lado)
        val arriba = (centroY - lado / 2f).roundToInt().coerceIn(0, image.height - lado)

        val recorte = runCatching {
            val trozo = Bitmap.createBitmap(image, izquierda, arriba, lado, lado)
            val destino = Bitmap.createScaledBitmap(trozo, OUTPUT, OUTPUT, true)
            if (trozo != destino) trozo.recycle()
            destino
        }.getOrNull() ?: return@withContext null

        val sello = System.currentTimeMillis()
        val archivo = File(context.filesDir, CROP + sello + SUFFIX)
        val guardado = runCatching {
            archivo.outputStream().use { recorte.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        }.getOrDefault(false)
        recorte.recycle()

        if (!guardado || archivo.length() == 0L) {
            archivo.delete()
            return@withContext null
        }

        // El original, para que volver a encuadrar no parta de un recorte.
        val copia = File(context.filesDir, SOURCE + sello + SUFFIX)
        runCatching {
            context.contentResolver.openInputStream(source)?.use { entrada ->
                copia.outputStream().use { salida -> entrada.copyTo(salida) }
            }
        }

        clear(context, keep = sello)
        Uri.fromFile(archivo).toString()
    }

    /**
     * El original del retrato que se está usando, si sigue guardado.
     *
     * Se devuelve `null` cuando el retrato viene de una versión anterior a los originales, o
     * cuando es la foto de la cuenta de Google: en esos casos se vuelve a pedir una foto en vez
     * de reencuadrar algo que no está.
     */
    fun sourceOf(context: Context, cropUri: String?): Uri? {
        val nombre = cropUri?.substringAfterLast('/') ?: return null
        if (!nombre.startsWith(CROP)) return null
        val sello = nombre.removePrefix(CROP).removeSuffix(SUFFIX)
        val copia = File(context.filesDir, SOURCE + sello + SUFFIX)
        return if (copia.exists()) Uri.fromFile(copia) else null
    }

    /** Borra los retratos guardados, menos el del sello que se acaba de escribir. */
    fun clear(context: Context, keep: Long? = null) {
        val vivos = setOfNotNull(
            keep?.let { CROP + it + SUFFIX },
            keep?.let { SOURCE + it + SUFFIX }
        )
        runCatching {
            context.filesDir.listFiles { file ->
                (file.name.startsWith(CROP) || file.name.startsWith(SOURCE)) &&
                    file.name !in vivos
            }?.forEach { it.delete() }
        }
    }

    private fun min(a: Int, b: Int) = if (a < b) a else b
}
