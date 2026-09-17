package com.unistack.app.feature_tasks.data

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_tasks.domain.TaskAttachment
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import com.unistack.app.core.utils.cabeceraWav

/**
 * Adjuntos de mentira para el banco de pruebas, calcados de `NoteSamples`.
 *
 * Misma razón: una imagen dibujada pesa cero en el instalador y sale del tamaño real de una
 * foto, así que sirve para ver la tira de adjuntos, el carrusel y la vista a pantalla completa
 * sin tener que fotografiar nada a mano cada vez que se prueba la pantalla.
 */
object TaskAttachmentSamples {
    fun pizarra(store: TaskAttachmentStore, taskId: String, variante: Int = 0): TaskAttachment? = runCatching {
        val ancho = 1200
        val alto = 800
        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val lienzo = Canvas(bitmap)

        // design-tokens-ok: es una imagen dibujada, no interfaz: los colores son el contenido.
        lienzo.drawColor(
            if (variante == 0) android.graphics.Color.rgb(30, 34, 52) else android.graphics.Color.rgb(28, 42, 38)
        )

        val tiza = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(226, 232, 226)
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val letra = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.rgb(226, 232, 226)
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            textSize = 62f
        }
        val floja = Paint(letra).apply {
            color = android.graphics.Color.argb(150, 226, 232, 226)
            textSize = 46f
        }

        if (variante == 0) {
            lienzo.drawText("∬ (x² + y²) dA", 90f, 170f, letra)
            lienzo.drawLine(90f, 205f, 700f, 205f, tiza)
            lienzo.drawText("región tipo I: 0 ≤ x ≤ 2", 90f, 300f, floja)
            lienzo.drawText("cambio de orden → tipo II", 90f, 380f, letra)
            lienzo.drawText("revisar límites antes de integrar", 90f, 460f, floja)
            lienzo.drawLine(90f, 620f, 1100f, 620f, tiza)
            lienzo.drawText("ejercicios 19-28: cambio de orden", 90f, 700f, floja)
        } else {
            lienzo.drawText("Ley de Gauss", 90f, 160f, letra)
            lienzo.drawLine(90f, 195f, 700f, 195f, tiza)
            lienzo.drawText("Φ = Q_enc / ε₀", 90f, 290f, letra)
            lienzo.drawText("simetría esférica / cilíndrica / plana", 90f, 370f, floja)
            lienzo.drawText("capacitancia: C = Q / V", 90f, 460f, letra)
            lienzo.drawLine(90f, 610f, 1100f, 610f, tiza)
            lienzo.drawText("traer calculadora no programable", 90f, 700f, floja)
        }

        val bytes = ByteArrayOutputStream().also {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 82, it)
        }.toByteArray()
        bitmap.recycle()

        val (nombre, archivo) = store.newFileFor("jpg")
        archivo.writeBytes(bytes)
        adjunto(
            taskId = taskId,
            kind = AttachmentKind.IMAGE,
            displayName = "apunte-$variante.jpg",
            storedName = nombre,
            mimeType = "image/jpeg",
            sizeBytes = archivo.length()
        )
    }.getOrNull()

    fun documento(store: TaskAttachmentStore, taskId: String, nombreVisible: String, contenido: String): TaskAttachment? = runCatching {
        val (nombre, archivo) = store.newFileFor("txt")
        archivo.writeText(contenido.trimIndent())
        adjunto(
            taskId = taskId,
            kind = AttachmentKind.FILE,
            displayName = nombreVisible,
            storedName = nombre,
            mimeType = "text/plain",
            sizeBytes = archivo.length()
        )
    }.getOrNull()

    /** Un audio sintetizado: dos notas que suben y se apagan, igual que en Notas. */
    fun grabacion(store: TaskAttachmentStore, taskId: String): TaskAttachment? = runCatching {
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
        adjunto(
            taskId = taskId,
            kind = AttachmentKind.AUDIO,
            displayName = "nota de voz.wav",
            storedName = nombre,
            mimeType = "audio/wav",
            sizeBytes = archivo.length(),
            durationMillis = duracionMs
        )
    }.getOrNull()

    private fun adjunto(
        taskId: String,
        kind: AttachmentKind,
        displayName: String,
        storedName: String,
        mimeType: String,
        sizeBytes: Long,
        durationMillis: Long? = null
    ) = TaskAttachment(
        id = "prueba-att-" + UUID.randomUUID().toString().take(8),
        taskId = taskId,
        kind = kind,
        displayName = displayName,
        storedName = storedName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        durationMillis = durationMillis,
        createdAt = System.currentTimeMillis()
    )
}
