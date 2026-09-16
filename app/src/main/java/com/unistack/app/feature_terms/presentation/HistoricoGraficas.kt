package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.feature_schedule.presentation.AttendanceAbsent
import com.unistack.app.feature_schedule.presentation.AttendanceAttended
import com.unistack.app.feature_schedule.presentation.AttendanceCancelled
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_terms.domain.ClaseDelPeriodo
import com.unistack.app.feature_terms.domain.PuntoDelHistorico
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/*
 * Las gráficas del histórico.
 *
 * Cada una es la de la simulación con sus mismas coordenadas: se dibujaron en una caja de 314 de
 * ancho, que es lo que mide una tarjeta en un teléfono de 390 dp, y aquí se reescalan al ancho que
 * haya. Lo que la maqueta daba por hecho —escala de 0 a 5, tres cortes— se generaliza a cualquier
 * escala y a cualquier número de cortes.
 */

private enum class Ancla { Inicio, Centro, Fin }

private fun DrawScope.escribir(
    medidor: TextMeasurer,
    texto: String,
    x: Float,
    base: Float,
    estilo: TextStyle,
    ancla: Ancla = Ancla.Inicio
) {
    val medida = medidor.measure(texto, estilo)
    val izquierda = when (ancla) {
        Ancla.Inicio -> x
        Ancla.Centro -> x - medida.size.width / 2f
        Ancla.Fin -> x - medida.size.width
    }
    drawText(medida, topLeft = Offset(izquierda, base - medida.firstBaseline))
}

private fun estiloDeGrafica(tamano: Float, color: Color, peso: FontWeight = FontWeight.Normal) =
    TextStyle(fontSize = tamano.sp, color = color, fontWeight = peso, fontFeatureSettings = "tnum")

private val raya33 = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))

private fun rayas(u: Float, a: Float, b: Float) = PathEffect.dashPathEffect(floatArrayOf(a * u, b * u))

/** Divisiones redondas entre dos valores: medios puntos en la escala de 5, decenas en la de 100. */
private fun divisiones(valores: List<Double>, aprobado: Double, maxima: Double): List<Double> {
    var paso = maxima / 10.0
    val menor = valores.minOrNull() ?: aprobado
    val mayor = valores.maxOrNull() ?: aprobado
    var desde = min(aprobado, floor(menor / paso) * paso).coerceAtLeast(0.0)
    var hasta = max(aprobado + 3 * paso, ceil(mayor / paso) * paso).coerceAtMost(maxima)
    // Un aprobado en el máximo dejaría el eje sin altura, y todas las cuentas dividen por ella.
    if (hasta - desde < paso) desde = (hasta - paso).coerceAtLeast(0.0)
    if (hasta - desde < paso) hasta = desde + paso
    while ((hasta - desde) / paso > 4.0001) {
        paso *= 2
        desde = floor(desde / paso) * paso
        hasta = ceil(hasta / paso) * paso
    }
    val lista = mutableListOf<Double>()
    var v = desde
    while (v <= hasta + paso / 1000) {
        lista += v
        v += paso
    }
    return lista
}

private fun etiquetaDeEje(valor: Double, maxima: Double): String =
    if (maxima <= 10.0) String.format(Locale.US, "%.1f", valor) else String.format(Locale.US, "%.0f", valor)

// ================================================================== el histórico

/**
 * El promedio de cada periodo y el acumulado, con la franja de lo que puede quedar el que está
 * en curso. Tocar un punto abre ese periodo.
 */
@Composable
internal fun GraficaDelHistorico(
    serie: List<PuntoDelHistorico>,
    nombreActivo: String?,
    rangoActivo: Pair<Double, Double>?,
    aprobado: Double,
    maxima: Double,
    onPunto: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val medidor = rememberTextMeasurer()
    val acento = Paleta.acento
    val contenedor = Paleta.acentoContenedor
    val apagado = Paleta.apagado
    val linea = Paleta.linea
    val tinta = Paleta.tinta
    val rojo = AttendanceAbsent
    val abrir by rememberUpdatedState(onPunto)
    val descripcion = stringResource(R.string.hist_grafica_descripcion)

    val n = serie.size + if (nombreActivo != null) 1 else 0
    val valores = serie.mapNotNull { it.promedio } + serie.mapNotNull { it.acumulado } +
        listOfNotNull(rangoActivo?.first, rangoActivo?.second)
    val marcas = divisiones(valores, aprobado, maxima)
    val bajo = marcas.first()
    val altoEje = marcas.last()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(314f / 140f)
            .semantics { contentDescription = descripcion }
            .pointerInput(serie) {
                detectTapGestures { toque ->
                    val u = size.width / 314f
                    val dx = (314f - 44f - 22f) / max(1, n - 1)
                    val alto = 92f
                    serie.forEachIndexed { i, punto ->
                        val v = punto.promedio ?: return@forEachIndexed
                        val x = (44f + i * dx) * u
                        val y = (16f + ((altoEje - v.coerceIn(bajo, altoEje)) / (altoEje - bajo)).toFloat() * alto) * u
                        if (hypot(toque.x - x, toque.y - y) <= 14f * u * 1.6f) {
                            abrir(punto.termId)
                            return@detectTapGestures
                        }
                    }
                }
            }
    ) {
        val u = size.width / 314f
        val x0 = 44f
        val dx = (314f - x0 - 22f) / max(1, n - 1)
        val top = 16f
        val alto = 92f
        fun y(v: Double) = (top + ((altoEje - v.coerceIn(bajo, altoEje)) / (altoEje - bajo)).toFloat() * alto) * u

        marcas.forEach { v ->
            val esAprobado = kotlin.math.abs(v - aprobado) < 0.0001
            drawLine(
                color = if (esAprobado) rojo else linea,
                start = Offset(34f * u, y(v)),
                end = Offset((314f - 4f) * u, y(v)),
                strokeWidth = 1.dp.toPx(),
                pathEffect = if (esAprobado) rayas(u, 3f, 3f) else null
            )
            escribir(medidor, etiquetaDeEje(v, maxima), 28f * u, y(v) + 4f * u, estiloDeGrafica(10f, apagado), Ancla.Fin)
        }

        val acumulados = serie.mapIndexedNotNull { i, p -> p.acumulado?.let { Offset((x0 + i * dx) * u, y(it)) } }
        if (acumulados.size > 1) {
            drawPath(
                path = Path().apply { acumulados.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } },
                color = apagado,
                style = Stroke(width = 1.5f * u, pathEffect = rayas(u, 4f, 3f))
            )
        }
        val puntos = serie.mapIndexedNotNull { i, p -> p.promedio?.let { Triple(Offset((x0 + i * dx) * u, y(it)), it, p) } }
        if (puntos.size > 1) {
            drawPath(
                path = Path().apply { puntos.forEachIndexed { i, (o) -> if (i == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y) } },
                color = acento,
                style = Stroke(width = 2.5f * u, join = StrokeJoin.Round)
            )
        }
        puntos.forEach { (o, v) ->
            drawCircle(contenedor, radius = 7f * u, center = o)
            drawCircle(acento, radius = 5f * u, center = o)
            escribir(medidor, formatoDePromedio(v, maxima), o.x, o.y - 11f * u, estiloDeGrafica(11f, tinta, FontWeight.Bold), Ancla.Centro)
        }
        if (nombreActivo != null && rangoActivo != null) {
            val x = (x0 + serie.size * dx) * u
            drawLine(
                color = acento.copy(alpha = 0.45f),
                start = Offset(x, y(rangoActivo.second)),
                end = Offset(x, y(rangoActivo.first)),
                strokeWidth = 7f * u,
                cap = StrokeCap.Round
            )
        }
        (serie.map { it.nombre } + listOfNotNull(nombreActivo)).forEachIndexed { i, nombre ->
            escribir(medidor, nombre, (x0 + i * dx) * u, (140f - 4f) * u, estiloDeGrafica(10.5f, apagado), Ancla.Centro)
        }
    }
}

/** La línea pequeña del acumulado, para la tarjeta de «Tu recorrido». */
@Composable
internal fun Chispa(valores: List<Double>, modifier: Modifier = Modifier) {
    if (valores.size < 2) return
    val acento = Paleta.acento
    Canvas(modifier = modifier.size(96.dp, 40.dp)) {
        val u = size.width / 96f
        val margen = (valores.max() - valores.min()).coerceAtLeast(0.1) * 0.25
        val bajo = valores.min() - margen
        val alto = valores.max() + margen
        val puntos = valores.mapIndexed { i, v ->
            Offset((4f + i * (96f - 8f) / (valores.size - 1)) * u, (40f - 4f - ((v - bajo) / (alto - bajo)).toFloat() * (40f - 8f)) * u)
        }
        drawPath(
            path = Path().apply { puntos.forEachIndexed { i, p -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } },
            color = acento,
            style = Stroke(width = 2.5f * u, join = StrokeJoin.Round, cap = StrokeCap.Round)
        )
        drawCircle(acento, radius = 4f * u, center = puntos.last())
    }
}

// ================================================================== una materia

/** Un punto de la gráfica de notas: cuándo, cuánto y de qué corte. */
internal data class NotaEnElTiempo(val fecha: LocalDate, val valor: Double, val corte: Int)

@Composable
internal fun GraficaDeNotasEnElTiempo(
    notas: List<NotaEnElTiempo>,
    inicio: LocalDate,
    fin: LocalDate,
    final: Double?,
    aprobado: Double,
    maxima: Double,
    modifier: Modifier = Modifier
) {
    val medidor = rememberTextMeasurer()
    val apagado = Paleta.apagado
    val linea = Paleta.linea
    val acento = Paleta.acento
    val fondoPunto = Paleta.bajo
    val rojo = AttendanceAbsent
    val colores = (0 until 6).map { colorDeCorte(it) }
    val textoFinal = stringResource(R.string.hist_final_corto)
    val meses = stringResource(R.string.hist_patron_mes_corto)

    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(314f / 170f)) {
        val u = size.width / 314f
        val l = 26f
        val r = 8f
        val t = 14f
        val b = 24f
        val h = 170f
        val bajo = (aprobado - maxima * 0.2).coerceAtLeast(0.0)
        val paso = maxima / 5.0
        val dias = ChronoUnit.DAYS.between(inicio, fin).coerceAtLeast(1)
        fun x(d: LocalDate) = (l + ChronoUnit.DAYS.between(inicio, d).toFloat() / dias * (314f - l - r)) * u
        fun y(v: Double) = (t + ((maxima - v.coerceIn(bajo, maxima)) / (maxima - bajo)).toFloat() * (h - t - b)) * u

        var v = bajo
        while (v <= maxima + 0.0001) {
            val esAprobado = kotlin.math.abs(v - aprobado) < 0.0001
            drawLine(if (esAprobado) rojo else linea, Offset(l * u, y(v)), Offset((314f - r) * u, y(v)), 1.dp.toPx(), pathEffect = if (esAprobado) rayas(u, 3f, 3f) else null)
            escribir(medidor, etiquetaDeEje(v, maxima).removeSuffix(".0"), (l - 7f) * u, y(v) + 4f * u, estiloDeGrafica(10f, apagado), Ancla.Fin)
            v += paso
        }
        if (final != null) {
            drawLine(acento.copy(alpha = 0.75f), Offset(l * u, y(final)), Offset((314f - r) * u, y(final)), 1.5f * u, pathEffect = rayas(u, 6f, 4f))
            escribir(medidor, "$textoFinal ${Formato.nota(final, if (maxima <= 10.0) com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE else com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_HUNDRED)}", (314f - r) * u, y(final) - 6f * u, estiloDeGrafica(10.5f, acento, FontWeight.Bold), Ancla.Fin)
        }
        val puntos = notas.sortedBy { it.fecha }.map { Offset(x(it.fecha), y(it.valor)) to it }
        if (puntos.size > 1) {
            drawPath(
                path = Path().apply { puntos.forEachIndexed { i, (p) -> if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y) } },
                color = apagado,
                style = Stroke(width = 1.5f * u, join = StrokeJoin.Round)
            )
        }
        puntos.forEach { (p, nota) ->
            drawCircle(fondoPunto, radius = 7f * u, center = p)
            drawCircle(colores[nota.corte.coerceIn(0, colores.lastIndex)], radius = 5f * u, center = p)
        }
        var mes = inicio.withDayOfMonth(1).plusMonths(1)
        while (mes.isBefore(fin)) {
            val etiqueta = java.time.format.DateTimeFormatter.ofPattern(meses, Locale.getDefault()).format(mes).replace(".", "")
            escribir(medidor, etiqueta, x(mes), (h - 6f) * u, estiloDeGrafica(10f, apagado), Ancla.Centro)
            mes = mes.plusMonths(1)
        }
    }
}

/** Tu nota de cada corte en color, y en gris la media de tus materias en ese corte. */
@Composable
internal fun GraficaDeCortesFrenteAlPeriodo(
    mias: List<Double?>,
    medias: List<Double?>,
    pesos: List<Int>,
    aprobado: Double,
    maxima: Double,
    etiquetaCorte: (Int, Int) -> String,
    formato: (Double?) -> String,
    modifier: Modifier = Modifier
) {
    val medidor = rememberTextMeasurer()
    val apagado = Paleta.apagado
    val tinta = Paleta.tinta
    val gris = Paleta.tope
    val rojo = AttendanceAbsent
    val colores = (0 until 6).map { colorDeCorte(it) }

    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(314f / 150f)) {
        val u = size.width / 314f
        val base = 120f
        val alto = 96f
        val n = mias.size.coerceAtLeast(1)
        val grupo = (314f - 20f) / n
        val ancho = min(30f, grupo / 2f - 6f)
        fun a(v: Double?) = if (v == null) 0f else (v / maxima).toFloat() * alto

        val yAprobado = (base - (aprobado / maxima).toFloat() * alto) * u
        drawLine(rojo, Offset(10f * u, yAprobado), Offset((314f - 10f) * u, yAprobado), 1.dp.toPx(), pathEffect = rayas(u, 3f, 3f))
        mias.forEachIndexed { k, mia ->
            val cx = 10f + grupo * k + grupo / 2f
            val media = medias.getOrNull(k)
            drawRoundRect(colores[k.coerceAtMost(colores.lastIndex)], Offset((cx - ancho - 2f) * u, (base - a(mia)) * u), Size(ancho * u, a(mia) * u), CornerRadius(6f * u))
            drawRoundRect(gris, Offset((cx + 2f) * u, (base - a(media)) * u), Size(ancho * u, a(media) * u), CornerRadius(6f * u))
            escribir(medidor, formato(mia), (cx - ancho / 2f - 2f) * u, (base - a(mia) - 6f) * u, estiloDeGrafica(11f, tinta, FontWeight.ExtraBold), Ancla.Centro)
            escribir(medidor, formato(media), (cx + ancho / 2f + 2f) * u, (base - a(media) - 6f) * u, estiloDeGrafica(11f, apagado), Ancla.Centro)
            escribir(medidor, etiquetaCorte(k + 1, pesos.getOrElse(k) { 0 }), cx * u, (150f - 10f) * u, estiloDeGrafica(11f, apagado), Ancla.Centro)
        }
    }
}

/** Lo que aportó cada corte, uno detrás de otro sobre la escala, con la raya del aprobado. */
@Composable
internal fun GraficaDeAporte(
    aportes: List<Double?>,
    aprobado: Double,
    maxima: Double,
    textoAprobado: String,
    modifier: Modifier = Modifier
) {
    val medidor = rememberTextMeasurer()
    val tinta = Paleta.tinta
    val rojo = AttendanceAbsent
    val colores = (0 until 6).map { colorDeCorte(it) }

    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(314f / 66f)) {
        val u = size.width / 314f
        val escala = 314f / maxima.toFloat()
        var x = 0f
        aportes.forEachIndexed { k, aporte ->
            if (aporte == null) return@forEachIndexed
            val w = aporte.toFloat() * escala
            val izquierda = x + if (k > 0) 1.5f else 0f
            drawRoundRect(
                colores[k.coerceAtMost(colores.lastIndex)],
                Offset(izquierda * u, 8f * u),
                Size((w - if (k > 0) 1.5f else 0f).coerceAtLeast(0f) * u, 26f * u),
                CornerRadius(6f * u)
            )
            val texto = "+" + if (maxima <= 10.0) String.format(Locale.US, "%.2f", aporte) else String.format(Locale.US, "%.1f", aporte)
            escribir(medidor, texto, (x + w / 2f) * u, 50f * u, estiloDeGrafica(10.5f, tinta), Ancla.Centro)
            x += w
        }
        val xa = aprobado.toFloat() * escala * u
        drawLine(rojo, Offset(xa, 2f * u), Offset(xa, 38f * u), 2f * u)
        escribir(medidor, textoAprobado, xa.coerceIn(24f * u, size.width - 24f * u), 63f * u, estiloDeGrafica(10f, rojo), Ancla.Centro)
    }
}

/** La asistencia acumulada semana a semana, con el mínimo que deja el tope de faltas. */
@Composable
internal fun GraficaDeAsistenciaAcumulada(
    puntos: List<Double>,
    minimo: Double?,
    textoMinimo: String?,
    textoSemanaUno: String,
    textoUltimaSemana: String,
    modifier: Modifier = Modifier
) {
    if (puntos.isEmpty()) return
    val medidor = rememberTextMeasurer()
    val acento = Paleta.acento
    val apagado = Paleta.apagado
    val linea = Paleta.linea
    val ambar = AttendanceCancelled

    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(314f / 132f)) {
        val u = size.width / 314f
        val l = 36f
        val r = 8f
        val t = 10f
        val b = 22f
        val h = 132f
        val semanas = puntos.size
        fun x(w: Int) = (l + (if (semanas > 1) w.toFloat() / (semanas - 1) else 0f) * (314f - l - r)) * u
        fun y(v: Double) = (t + ((100.0 - max(50.0, v)) / 50.0).toFloat() * (h - t - b)) * u

        listOf(50.0, 75.0, 100.0).forEach { v ->
            drawLine(linea, Offset(l * u, y(v)), Offset((314f - r) * u, y(v)), 1.dp.toPx())
            escribir(medidor, "${v.toInt()}%", (l - 6f) * u, y(v) + 4f * u, estiloDeGrafica(10f, apagado), Ancla.Fin)
        }
        val area = Path().apply {
            moveTo(x(0), (h - b) * u)
            puntos.forEachIndexed { w, v -> lineTo(x(w), y(v)) }
            lineTo(x(semanas - 1), (h - b) * u)
            close()
        }
        drawPath(area, acento.copy(alpha = 0.16f))
        if (minimo != null && textoMinimo != null) {
            drawLine(ambar, Offset(l * u, y(minimo)), Offset((314f - r) * u, y(minimo)), 1.5f * u, pathEffect = rayas(u, 4f, 3f))
            escribir(medidor, textoMinimo, (314f - r) * u, y(minimo) + 14f * u, estiloDeGrafica(10f, ambar), Ancla.Fin)
        }
        drawPath(
            path = Path().apply { puntos.forEachIndexed { w, v -> if (w == 0) moveTo(x(w), y(v)) else lineTo(x(w), y(v)) } },
            color = acento,
            style = Stroke(width = 2.5f * u, join = StrokeJoin.Round)
        )
        drawCircle(acento, radius = 4.5f * u, center = Offset(x(semanas - 1), y(puntos.last())))
        escribir(medidor, textoSemanaUno, l * u, (h - 5f) * u, estiloDeGrafica(10f, apagado))
        escribir(medidor, textoUltimaSemana, (314f - r) * u, (h - 5f) * u, estiloDeGrafica(10f, apagado), Ancla.Fin)
    }
}

/**
 * El mapa de clases: una columna por semana y una casilla por clase.
 *
 * Verde fuiste, rojo faltaste, ámbar no hubo y un aro lo que falta marcar. Si el periodo tiene
 * más semanas de las que caben, las casillas encogen en vez de salirse.
 */
@Composable
internal fun MapaDeSemanas(clases: List<ClaseDelPeriodo>, inicio: LocalDate, semanas: Int, modifier: Modifier = Modifier) {
    val vacia = Paleta.alto
    val aro = Paleta.tenue
    val porSemana = (0 until semanas.coerceAtLeast(1)).map { w ->
        clases.filter { ChronoUnit.DAYS.between(inicio, it.date) / 7 == w.toLong() }
    }
    val filas = porSemana.maxOfOrNull { it.size }?.coerceIn(1, 4) ?: 1
    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(314f / (filas * 15f - 3f))) {
        val u = size.width / 314f
        val paso = min(15f, 314f / porSemana.size.coerceAtLeast(1))
        val casilla = paso - 3f
        porSemana.forEachIndexed { w, suyas ->
            suyas.take(filas).forEachIndexed { f, clase ->
                val o = Offset(w * paso * u, f * 15f * u)
                val s = Size(casilla * u, 12f * u)
                when (clase.status) {
                    ClassAttendanceStatus.ATTENDED -> drawRoundRect(AttendanceAttended, o, s, CornerRadius(3f * u))
                    ClassAttendanceStatus.ABSENT -> drawRoundRect(AttendanceAbsent, o, s, CornerRadius(3f * u))
                    ClassAttendanceStatus.CANCELLED, ClassAttendanceStatus.RESCHEDULED -> drawRoundRect(AttendanceCancelled, o, s, CornerRadius(3f * u))
                    ClassAttendanceStatus.PENDING -> {
                        drawRoundRect(vacia, o, s, CornerRadius(3f * u))
                        drawRoundRect(aro, o + Offset(u, u), Size(s.width - 2f * u, s.height - 2f * u), CornerRadius(2.5f * u), style = Stroke(2f * u))
                    }
                }
            }
        }
    }
}

// ================================================================== periodo nuevo

/** Así queda tu año: el periodo anterior, las semanas libres, el nuevo y hoy. */
@Composable
internal fun LineaDelAnio(
    previoInicio: LocalDate,
    previoFin: LocalDate,
    previoNombre: String,
    nuevoInicio: LocalDate,
    nuevoFin: LocalDate,
    nuevoNombre: String,
    hoy: LocalDate,
    textoLibres: (Int) -> String,
    textoHoy: String,
    modifier: Modifier = Modifier
) {
    val medidor = rememberTextMeasurer()
    val tope = Paleta.tope
    val apagado = Paleta.apagado
    val tenue = Paleta.tenue
    val acento = Paleta.acento
    val sobreAcento = Paleta.sobreAcento
    val tinta = Paleta.tinta
    val patronMes = stringResource(R.string.hist_patron_mes_corto)

    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(314f / 92f)) {
        val u = size.width / 314f
        val desde = previoInicio.withDayOfMonth(1)
        val hasta = maxOf(nuevoFin, hoy).withDayOfMonth(1).plusMonths(2).minusDays(1)
        val total = ChronoUnit.DAYS.between(desde, hasta).coerceAtLeast(1).toFloat()
        fun x(d: LocalDate) = (6f + ChronoUnit.DAYS.between(desde, d) / total * (314f - 12f)) * u

        drawRoundRect(tope, Offset(x(previoInicio), 30f * u), Size(x(previoFin) - x(previoInicio), 24f * u), CornerRadius(9f * u))
        escribir(medidor, previoNombre, (x(previoInicio) + x(previoFin)) / 2f, 46f * u, estiloDeGrafica(11f, apagado, FontWeight.Bold), Ancla.Centro)
        if (x(nuevoInicio) - x(previoFin) > 12f * u) {
            drawLine(tenue, Offset(x(previoFin) + 5f * u, 42f * u), Offset(x(nuevoInicio) - 5f * u, 42f * u), 2f * u, pathEffect = rayas(u, 2f, 4f))
        }
        val libres = (ChronoUnit.DAYS.between(previoFin, nuevoInicio) / 7.0).let { Math.round(it).toInt() }
        if (libres > 0) {
            escribir(medidor, textoLibres(libres), (x(previoFin) + x(nuevoInicio)) / 2f, 20f * u, estiloDeGrafica(10.5f, apagado), Ancla.Centro)
        }
        drawRoundRect(acento, Offset(x(nuevoInicio), 30f * u), Size(x(nuevoFin) - x(nuevoInicio), 24f * u), CornerRadius(9f * u))
        escribir(medidor, nuevoNombre, (x(nuevoInicio) + x(nuevoFin)) / 2f, 46f * u, estiloDeGrafica(11f, sobreAcento, FontWeight.ExtraBold), Ancla.Centro)
        if (!hoy.isBefore(desde) && !hoy.isAfter(hasta)) {
            drawLine(tinta, Offset(x(hoy), 26f * u), Offset(x(hoy), 58f * u), 1.5f * u)
            escribir(medidor, textoHoy, x(hoy), 70f * u, estiloDeGrafica(10f, tinta), Ancla.Centro)
        }
        var mes = desde
        val formato = java.time.format.DateTimeFormatter.ofPattern(patronMes, Locale.getDefault())
        while (!mes.isAfter(hasta)) {
            val xm = x(mes).coerceIn(10f * u, size.width - 10f * u)
            escribir(medidor, formato.format(mes).replace(".", ""), xm, (92f - 3f) * u, estiloDeGrafica(10f, apagado), Ancla.Centro)
            mes = mes.plusMonths(2)
        }
    }
}

/** Los cortes sobre la duración del periodo, con su peso, sus semanas y sus fechas. */
@Composable
internal fun BarraDeCortes(
    semanasPorCorte: List<Int>,
    pesos: List<Int>,
    inicio: LocalDate,
    conFechas: Boolean,
    textoSemanas: (Int, Boolean) -> String,
    etiquetaCorte: (Int, Int) -> String,
    modifier: Modifier = Modifier
) {
    val medidor = rememberTextMeasurer()
    val apagado = Paleta.apagado
    val tinta = Paleta.tinta
    val colores = (0 until 6).map { colorDeCorte(it) }
    val semanas = semanasPorCorte.sum().coerceAtLeast(1)

    Canvas(modifier = modifier.fillMaxWidth().aspectRatio(314f / 80f)) {
        val u = size.width / 314f
        var x = 0f
        var acumuladas = 0
        semanasPorCorte.forEachIndexed { k, w ->
            val ancho = w.toFloat() / semanas * 314f
            val izquierda = x + if (k > 0) 2f else 0f
            val derecha = x + ancho - if (k < semanasPorCorte.lastIndex) 2f else 0f
            val color = colores[k.coerceAtMost(colores.lastIndex)]
            drawRoundRect(color, Offset(izquierda * u, 18f * u), Size((derecha - izquierda) * u, 30f * u), CornerRadius(9f * u))
            val corto = ancho < 70f
            escribir(medidor, etiquetaCorte(k + 1, pesos.getOrElse(k) { 0 }), (x + ancho / 2f) * u, 38f * u, estiloDeGrafica(11.5f, contentColorOn(color), FontWeight.ExtraBold), Ancla.Centro)
            escribir(medidor, textoSemanas(w, corto), (x + ancho / 2f) * u, 12f * u, estiloDeGrafica(10.5f, apagado), Ancla.Centro)
            if (conFechas) {
                escribir(
                    medidor,
                    Formato.diaMes(inicio.plusDays(acumuladas * 7L)),
                    if (k > 0) x * u else 0f,
                    66f * u,
                    estiloDeGrafica(10.5f, tinta),
                    if (k > 0) Ancla.Centro else Ancla.Inicio
                )
            }
            x += ancho
            acumuladas += w
        }
        if (conFechas) {
            escribir(medidor, Formato.diaMes(inicio.plusDays(semanas * 7L - 3L)), 314f * u, 66f * u, estiloDeGrafica(10.5f, tinta), Ancla.Fin)
        }
    }
}

// ================================================================== cierre

/** El anillo de «4/6 materias completas»: verde cuando están todas. */
@Composable
internal fun AnilloDeProgreso(hechas: Int, total: Int, modifier: Modifier = Modifier) {
    val medidor = rememberTextMeasurer()
    val pista = Paleta.acentoSuave
    val acento = Paleta.acento
    val tinta = Paleta.tinta
    val completo = total > 0 && hechas >= total
    Canvas(modifier = modifier.size(76.dp)) {
        val u = size.width / 76f
        val trazo = 8f * u
        val radio = 31f * u
        val esquina = Offset(size.width / 2f - radio, size.height / 2f - radio)
        drawCircle(pista, radius = radio, style = Stroke(trazo))
        if (total > 0 && hechas > 0) {
            drawArc(
                color = if (completo) AttendanceAttended else acento,
                startAngle = -90f,
                sweepAngle = 360f * hechas / total,
                useCenter = false,
                topLeft = esquina,
                size = Size(radio * 2f, radio * 2f),
                style = Stroke(trazo, cap = StrokeCap.Round)
            )
        }
        escribir(medidor, "$hechas/$total", size.width / 2f, size.height / 2f + 6f * u, estiloDeGrafica(18f, tinta, FontWeight.ExtraBold), Ancla.Centro)
    }
}
