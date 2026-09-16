// design-tokens-exempt: el boletín es un documento para compartir, con colores de papel fijos que no siguen al tema.
package com.unistack.app.feature_terms.presentation

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas as LienzoAndroid
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.provider.MediaStore
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.unistack.app.R
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_terms.domain.PeriodoDelHistorico
import com.unistack.app.feature_user.domain.GradingScale
import java.io.File
import java.util.Locale

internal object ColoresDelPapel {
    val Papel = Color(0xFFFBFAFF)
    val Tinta = Color(0xFF14131C)
    val Gris = Color(0xFF5F5E6E)
    val Raya = Color(0xFFE4E2EE)
    val Rojo = Color(0xFFB3261E)
    val Marca = Color(0xFF5F56C9)
}

internal data class OpcionesDelBoletin(val cortes: Boolean = true, val asistencia: Boolean = true)

private val AnchoCorte: Dp = 38.dp
private val AnchoAsistencia: Dp = 50.dp
private val AnchoFinal: Dp = 44.dp

/** El boletín tal como sale en la imagen: papel claro también con el tema oscuro. */
@Composable
internal fun PapelDelBoletin(
    periodo: PeriodoDelHistorico,
    opciones: OpcionesDelBoletin,
    escala: GradingScale,
    maxima: Double,
    aprobado: Double
) {
    val cabecera = estilo(10.5f, 14f, FontWeight.Bold, 0.8f)
    val celda = estilo(12.5f, 18f, cifras = true)
    val raya = ColoresDelPapel.Raya
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ColoresDelPapel.Papel)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("UniStack", style = estilo(13f, 18f, FontWeight.Bold, 0.3f), color = ColoresDelPapel.Marca)
            Spacer(Modifier.weight(1f))
            Text(stringResource(R.string.hist_boletin_del_periodo), style = estilo(12f, 16f), color = ColoresDelPapel.Gris)
        }
        Text(periodo.nombre, style = estilo(26f, 32f, FontWeight.ExtraBold), color = ColoresDelPapel.Tinta, modifier = Modifier.padding(top = 6.dp))
        Text(subtituloDelBoletin(periodo, escala, maxima, aprobado), style = estilo(12.5f, 18f), color = ColoresDelPapel.Gris)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp)) {
            Text(stringResource(R.string.hist_boletin_materia).uppercase(Locale.getDefault()), style = cabecera, color = ColoresDelPapel.Gris, modifier = Modifier.weight(1f))
            if (opciones.cortes) {
                periodo.esquema.cuts.indices.forEach { i ->
                    Text("C${i + 1}", style = cabecera, color = ColoresDelPapel.Gris, textAlign = TextAlign.End, modifier = Modifier.width(AnchoCorte))
                }
            }
            if (opciones.asistencia) {
                Text(stringResource(R.string.hist_boletin_asist).uppercase(Locale.getDefault()), style = cabecera, color = ColoresDelPapel.Gris, textAlign = TextAlign.End, modifier = Modifier.width(AnchoAsistencia))
            }
            Text(stringResource(R.string.hist_boletin_final).uppercase(Locale.getDefault()), style = cabecera, color = ColoresDelPapel.Gris, textAlign = TextAlign.End, modifier = Modifier.width(AnchoFinal))
        }
        periodo.materias.forEach { materia ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind { drawLine(raya, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
                    .padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(materia.nombre, style = celda, color = ColoresDelPapel.Tinta, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                if (opciones.cortes) {
                    materia.cortes.forEach { corte ->
                        Text(Formato.nota(corte.nota, escala), style = celda, color = ColoresDelPapel.Tinta, textAlign = TextAlign.End, modifier = Modifier.width(AnchoCorte))
                    }
                }
                if (opciones.asistencia) {
                    Text(Formato.porcentaje(materia.asistencia.porcentaje), style = celda, color = ColoresDelPapel.Tinta, textAlign = TextAlign.End, modifier = Modifier.width(AnchoAsistencia))
                }
                Text(
                    Formato.nota(materia.final, escala),
                    style = celda.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (materia.final != null && materia.final < aprobado - 0.0001) ColoresDelPapel.Rojo else ColoresDelPapel.Tinta,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(AnchoFinal)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .drawBehind { drawLine(raya, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(stringResource(R.string.hist_boletin_promedio), style = estilo(11f, 14f), color = ColoresDelPapel.Gris)
                Text(Formato.promedio(periodo.promedio, maxima), style = Letra.numero(20f), color = ColoresDelPapel.Tinta)
            }
            if (opciones.asistencia) {
                Column {
                    Text(stringResource(R.string.hist_boletin_asistencia), style = estilo(11f, 14f), color = ColoresDelPapel.Gris)
                    Text(Formato.porcentaje(periodo.asistencia), style = Letra.numero(20f), color = ColoresDelPapel.Tinta)
                }
            }
            Spacer(Modifier.weight(1f))
            if (opciones.cortes) {
                Text(
                    stringResource(R.string.hist_boletin_cortes_al, periodo.esquema.cuts.sortedBy { it.order }.joinToString(" / ") { Math.round(it.weight * 100).toString() }),
                    style = estilo(11f, 14f),
                    color = ColoresDelPapel.Gris,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

private fun subtituloDelBoletin(periodo: PeriodoDelHistorico, escala: GradingScale, maxima: Double, aprobado: Double): String =
    Textos.get(
        R.string.hist_boletin_sub,
        Textos.get(R.string.hist_rango_fechas, Formato.diaMes(periodo.inicio), Formato.diaMes(periodo.fin)),
        periodo.fin.year,
        notaMaximaTexto(maxima).removeSuffix(".0"),
        Formato.nota(aprobado, escala)
    )

/**
 * La imagen del boletín, dibujada aparte a 1080 de ancho.
 *
 * Es el mismo papel de la pantalla con sus medidas multiplicadas: se pinta con el lienzo de
 * Android y no capturando la vista, para que salga igual aunque la pantalla esté a medio
 * desplazar o con otra letra.
 */
internal object ImagenDelBoletin {

    fun dibujar(
        periodo: PeriodoDelHistorico,
        opciones: OpcionesDelBoletin,
        escala: GradingScale,
        maxima: Double,
        aprobado: Double
    ): Bitmap {
        val ancho = 1080
        val s = ancho / 350f
        val margen = 18f * s
        val filas = periodo.materias.size
        val altoContenido = margen + 18f * s + 38f * s + 18f * s + 12f * s + 20f * s + filas * 32f * s + 12f * s + 12f * s + 40f * s + margen
        val alto = maxOf(1350, altoContenido.toInt())
        val bitmap = Bitmap.createBitmap(ancho, alto, Bitmap.Config.ARGB_8888)
        val lienzo = LienzoAndroid(bitmap)
        lienzo.drawColor(ColoresDelPapel.Papel.toArgb())

        fun pincel(tamano: Float, color: Color, negrita: Boolean = false, alinear: Paint.Align = Paint.Align.LEFT, espaciado: Float = 0f) =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = tamano * s
                this.color = color.toArgb()
                typeface = Typeface.create(Typeface.DEFAULT, if (negrita) Typeface.BOLD else Typeface.NORMAL)
                textAlign = alinear
                letterSpacing = espaciado
            }

        val raya = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ColoresDelPapel.Raya.toArgb()
            strokeWidth = 1f * s
        }
        val izquierda = margen
        val derecha = ancho - margen
        var y = margen + 14f * s

        lienzo.drawText("UniStack", izquierda, y, pincel(13f, ColoresDelPapel.Marca, negrita = true, espaciado = 0.02f))
        lienzo.drawText(Textos.get(R.string.hist_boletin_del_periodo), derecha, y, pincel(12f, ColoresDelPapel.Gris, alinear = Paint.Align.RIGHT))
        y += 34f * s
        lienzo.drawText(periodo.nombre, izquierda, y, pincel(26f, ColoresDelPapel.Tinta, negrita = true))
        y += 20f * s
        lienzo.drawText(subtituloDelBoletin(periodo, escala, maxima, aprobado), izquierda, y, pincel(12.5f, ColoresDelPapel.Gris))

        // Columnas desde la derecha: final, asistencia y cortes.
        val xFinal = derecha
        val xAsistencia = xFinal - AnchoFinal.value * s
        val cortes = periodo.esquema.cuts.size
        val xCortes = (0 until cortes).map { i ->
            val base = if (opciones.asistencia) xAsistencia - AnchoAsistencia.value * s else xAsistencia
            base - (cortes - 1 - i) * AnchoCorte.value * s
        }
        val limiteNombre = (if (opciones.cortes) xCortes.first() - AnchoCorte.value * s else if (opciones.asistencia) xAsistencia - AnchoAsistencia.value * s else xAsistencia) - 8f * s

        y += 26f * s
        val cabecera = pincel(10.5f, ColoresDelPapel.Gris, negrita = true, espaciado = 0.08f)
        val cabeceraDerecha = pincel(10.5f, ColoresDelPapel.Gris, negrita = true, alinear = Paint.Align.RIGHT, espaciado = 0.08f)
        lienzo.drawText(Textos.get(R.string.hist_boletin_materia).uppercase(Locale.getDefault()), izquierda, y, cabecera)
        if (opciones.cortes) xCortes.forEachIndexed { i, x -> lienzo.drawText("C${i + 1}", x, y, cabeceraDerecha) }
        if (opciones.asistencia) lienzo.drawText(Textos.get(R.string.hist_boletin_asist).uppercase(Locale.getDefault()), xAsistencia, y, cabeceraDerecha)
        lienzo.drawText(Textos.get(R.string.hist_boletin_final).uppercase(Locale.getDefault()), xFinal, y, cabeceraDerecha)
        y += 8f * s

        val celda = pincel(12.5f, ColoresDelPapel.Tinta)
        val celdaDerecha = pincel(12.5f, ColoresDelPapel.Tinta, alinear = Paint.Align.RIGHT)
        periodo.materias.forEach { materia ->
            lienzo.drawLine(izquierda, y, derecha, y, raya)
            val base = y + 21f * s
            val nombre = TextUtils.ellipsize(materia.nombre, celda, limiteNombre - izquierda, TextUtils.TruncateAt.END).toString()
            lienzo.drawText(nombre, izquierda, base, celda)
            if (opciones.cortes) materia.cortes.forEachIndexed { i, corte -> xCortes.getOrNull(i)?.let { lienzo.drawText(Formato.nota(corte.nota, escala), it, base, celdaDerecha) } }
            if (opciones.asistencia) lienzo.drawText(Formato.porcentaje(materia.asistencia.porcentaje), xAsistencia, base, celdaDerecha)
            val perdida = materia.final != null && materia.final < aprobado - 0.0001
            lienzo.drawText(
                Formato.nota(materia.final, escala),
                xFinal,
                base,
                pincel(12.5f, if (perdida) ColoresDelPapel.Rojo else ColoresDelPapel.Tinta, negrita = true, alinear = Paint.Align.RIGHT)
            )
            y += 32f * s
        }
        y += 12f * s
        lienzo.drawLine(izquierda, y, derecha, y, raya)
        y += 12f * s + 11f * s
        val etiqueta = pincel(11f, ColoresDelPapel.Gris)
        val cifra = pincel(20f, ColoresDelPapel.Tinta, negrita = true)
        lienzo.drawText(Textos.get(R.string.hist_boletin_promedio), izquierda, y, etiqueta)
        lienzo.drawText(Formato.promedio(periodo.promedio, maxima), izquierda, y + 26f * s, cifra)
        if (opciones.asistencia) {
            val x = izquierda + 90f * s
            lienzo.drawText(Textos.get(R.string.hist_boletin_asistencia), x, y, etiqueta)
            lienzo.drawText(Formato.porcentaje(periodo.asistencia), x, y + 26f * s, cifra)
        }
        if (opciones.cortes) {
            lienzo.drawText(
                Textos.get(R.string.hist_boletin_cortes_al, periodo.esquema.cuts.sortedBy { it.order }.joinToString(" / ") { Math.round(it.weight * 100).toString() })
                    .replace("\n", " "),
                derecha,
                y + 26f * s,
                pincel(11f, ColoresDelPapel.Gris, alinear = Paint.Align.RIGHT)
            )
        }
        return bitmap
    }

    private fun nombreDeArchivo(periodo: PeriodoDelHistorico): String =
        Textos.get(R.string.hist_boletin_archivo, periodo.nombre.replace(Regex("""[\\/:*?"<>|]"""), "-"))

    fun compartir(context: Context, bitmap: Bitmap, periodo: PeriodoDelHistorico): Result<Unit> = runCatching {
        val archivo = File(context.cacheDir, nombreDeArchivo(periodo))
        archivo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", archivo)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, Textos.get(R.string.hist_compartir_boletin)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    /** En Galería, dentro de Imágenes/UniStack. Antes de Android 10 hace falta permiso, así que se comparte. */
    fun guardar(context: Context, bitmap: Bitmap, periodo: PeriodoDelHistorico): Result<Boolean> = runCatching {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            compartir(context, bitmap, periodo).getOrThrow()
            return@runCatching false
        }
        val valores = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, nombreDeArchivo(periodo))
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/UniStack")
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, valores) ?: error("MediaStore")
        resolver.openOutputStream(uri)?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) } ?: error("MediaStore")
        true
    }
}
