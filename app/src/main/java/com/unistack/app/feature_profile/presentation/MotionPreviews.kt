@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.unistack.app.core.design.theme.LocalSectionColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Las ciento once variantes, animadas y a la vez.
 *
 * **Esto no es adorno de la pantalla de ajustes: es la pantalla.** Elegir entre «Trazo» y
 * «Barrido» leyendo dos palabras es adivinar; el nombre no dice qué va a pasar cuando marques
 * asistencia. Cada variante se pinta corriendo en bucle, así que se comparan de un vistazo y
 * se elige la que se ve, no la que suena mejor.
 *
 * Todas comparten el mismo lienzo normalizado —cien de ancho por sesenta y cuatro de alto— y
 * de ahí sale que se puedan poner en rejilla sin que unas se vean grandes y otras diminutas.
 * Dentro se dibuja con [DrawScope] y nada más: sin composables anidados, que en una rejilla de
 * noventa cajas animando son noventa recomposiciones por fotograma.
 */

private const val ANCHO = 100f
private const val ALTO = 64f

/**
 * Un reloj de 0 a 1 que vuelve a empezar.
 *
 * Todas las vistas previas cuelgan de él y no de un `animate*AsState` por variante: son
 * ciento once cajas, y ciento once animaciones con su propio arranque no llegarían nunca a
 * verse en fase.
 */
@Composable
internal fun bucle(duracionMs: Int, etiqueta: String = "bucle"): Float {
    val transicion = rememberInfiniteTransition(label = etiqueta)
    val valor by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracionMs, easing = LinearEasing)),
        label = etiqueta
    )
    return valor
}

/** El mismo reloj, de ida y vuelta: sirve para lo que respira o late. */
@Composable
internal fun vaiven(duracionMs: Int, etiqueta: String = "vaiven"): Float {
    val transicion = rememberInfiniteTransition(label = etiqueta)
    val valor by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracionMs, easing = LinearEasing), RepeatMode.Reverse),
        label = etiqueta
    )
    return valor
}

/** Suaviza el arranque y la llegada, que es lo que separa un movimiento de un salto. */
private fun suave(t: Float): Float = t * t * (3f - 2f * t)

/** Un muelle que se pasa de largo y vuelve. */
private fun muelle(t: Float, rebote: Float = 0.35f): Float {
    if (t >= 1f) return 1f
    return 1f - exp(-6f * t) * cos(t * PI.toFloat() * (2f + rebote * 4f))
}

/** El tramo [desde, hasta] del reloj, devuelto otra vez como 0..1. */
private fun tramo(t: Float, desde: Float, hasta: Float): Float =
    ((t - desde) / (hasta - desde)).coerceIn(0f, 1f)

/** Colores del lienzo: los del tema, para que la vista previa no mienta. */
internal data class TintaDemo(
    val fondo: Color,
    val pieza: Color,
    val acento: Color,
    val tinta: Color,
    val verde: Color,
    val rojo: Color,
    val ambar: Color
)

@Composable
internal fun tintaDemo(): TintaDemo {
    val esquema = MaterialTheme.colorScheme
    val secciones = LocalSectionColors.current
    return TintaDemo(
        fondo = esquema.surfaceContainerHighest,
        pieza = esquema.onSurface.copy(alpha = 0.16f),
        acento = esquema.primary,
        tinta = esquema.onSurface,
        verde = secciones.onTrack,
        rojo = secciones.atRisk,
        ambar = secciones.expenses
    )
}

/**
 * La caja de una variante: el lienzo normalizado y el dibujo dentro.
 *
 * @param dibujo recibe el reloj ya en 0..1 y pinta en coordenadas de cien por sesenta y cuatro.
 */
@Composable
internal fun LienzoDemo(
    t: Float,
    modifier: Modifier = Modifier,
    dibujo: DrawScope.(Float) -> Unit
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val escalaX = size.width / ANCHO
        val escalaY = size.height / ALTO
        scale(scaleX = escalaX, scaleY = escalaY, pivot = Offset.Zero) {
            dibujo(t)
        }
    }
}

// ---------------------------------------------------------------------- piezas comunes

/** Una fila de lista: el rectángulo redondeado que hace de contenido en casi todas. */
private fun DrawScope.fila(
    y: Float,
    color: Color,
    x: Float = 12f,
    ancho: Float = 76f,
    alto: Float = 11f,
    alfa: Float = 1f
) {
    drawRoundRect(
        color = color.copy(alpha = color.alpha * alfa),
        topLeft = Offset(x, y),
        size = Size(ancho, alto),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
}

/** Un panel entero, que es lo que se mueve en las transiciones de pantalla. */
private fun DrawScope.panel(x: Float, color: Color, alfa: Float = 1f, escala: Float = 1f) {
    val ancho = 74f * escala
    val alto = 46f * escala
    drawRoundRect(
        color = color.copy(alpha = color.alpha * alfa),
        topLeft = Offset(x + (74f - ancho) / 2f, 9f + (46f - alto) / 2f),
        size = Size(ancho, alto),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
    )
}

/** El visto de asistencia, dibujado por tramos para poder pintarlo a medias. */
private fun DrawScope.visto(centro: Offset, radio: Float, avance: Float, color: Color, grosor: Float = 3f) {
    val puntos = listOf(
        centro + Offset(-radio * 0.5f, 0f),
        centro + Offset(-radio * 0.12f, radio * 0.38f),
        centro + Offset(radio * 0.55f, -radio * 0.42f)
    )
    val largoA = (puntos[1] - puntos[0]).getDistance()
    val largoB = (puntos[2] - puntos[1]).getDistance()
    val total = largoA + largoB
    val recorrido = total * avance.coerceIn(0f, 1f)
    if (recorrido <= 0f) return
    val primero = min(recorrido, largoA)
    drawLine(
        color = color,
        start = puntos[0],
        end = puntos[0] + (puntos[1] - puntos[0]) * (primero / largoA),
        strokeWidth = grosor,
        cap = androidx.compose.ui.graphics.StrokeCap.Round
    )
    if (recorrido > largoA) {
        drawLine(
            color = color,
            start = puntos[1],
            end = puntos[1] + (puntos[2] - puntos[1]) * ((recorrido - largoA) / largoB),
            strokeWidth = grosor,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

/**
 * La onda circular de M3E: un arco cuyo **radio** ondula.
 *
 * No es una línea recta doblada, y por eso hay que calcular el radio punto a punto: doblar una
 * onda ya hecha deja las crestas mirando en la dirección equivocada según por dónde vaya el
 * arco.
 */
private fun ondaCircular(
    centro: Offset,
    radio: Float,
    amplitud: Float,
    lobulos: Int,
    desde: Float,
    hasta: Float,
    fase: Float
): Path {
    val camino = Path()
    val pasos = 80
    for (paso in 0..pasos) {
        val fraccion = paso / pasos.toFloat()
        val angulo = (desde + (hasta - desde) * fraccion) * 2f * PI.toFloat()
        val r = radio + amplitud * sin(lobulos * angulo + fase)
        val punto = Offset(centro.x + r * cos(angulo), centro.y + r * sin(angulo))
        if (paso == 0) camino.moveTo(punto.x, punto.y) else camino.lineTo(punto.x, punto.y)
    }
    return camino
}

/** El polígono de N lóbulos del `LoadingIndicator`, que es lo que le da su forma cambiante. */
private fun formaLobulada(centro: Offset, radio: Float, lobulos: Int, redondez: Float, giro: Float): Path {
    val camino = Path()
    val pasos = 120
    for (paso in 0..pasos) {
        val angulo = paso / pasos.toFloat() * 2f * PI.toFloat() + giro
        val r = radio * (1f - redondez + redondez * abs(cos(lobulos * angulo / 2f)))
        val punto = Offset(centro.x + r * cos(angulo), centro.y + r * sin(angulo))
        if (paso == 0) camino.moveTo(punto.x, punto.y) else camino.lineTo(punto.x, punto.y)
    }
    camino.close()
    return camino
}

/**
 * Qué dibuja cada variante.
 *
 * Es un `when` largo a propósito y no veinte funciones sueltas: leído de arriba abajo, cada
 * gesto queda con todas sus variantes seguidas, que es la única forma de asegurarse de que dos
 * no acaban pareciéndose. Ese fue el problema de la primera versión —«escalonada», «escala» y
 * «resorte» hacían lo mismo— y se arregló mirándolas juntas, no una a una.
 */
internal fun DrawScope.pintarVariante(gesto: String, variante: String, t: Float, tinta: TintaDemo) {
    when (gesto) {
        "velocidad" -> velocidad(variante, t, tinta)
        "rebote" -> rebote(variante, t, tinta)
        "pulsacion" -> pulsacion(variante, t, tinta)
        "carga" -> carga(variante, t, tinta)
        "transicion" -> transicion(variante, t, tinta)
        "listas" -> listas(variante, t, tinta)
        "refresco" -> refresco(variante, t, tinta)
        "asistencia" -> asistencia(variante, t, tinta)
        "notaNueva" -> notaNueva(variante, t, tinta)
        "subeNota" -> subeNota(variante, t, tinta)
        "recupera" -> recupera(variante, t, tinta)
        "sello" -> sello(variante, t, tinta)
        "cierreSem" -> cierreSemestre(variante, t, tinta)
        "celebracion" -> celebracion(variante, t, tinta)
        "tachar" -> tachar(variante, t, tinta)
        "latido" -> latido(variante, t, tinta)
        "deshacer" -> deshacer(variante, t, tinta)
        "guardado" -> guardado(variante, t, tinta)
        "fijar" -> fijar(variante, t, tinta)
        "presupuesto" -> presupuesto(variante, t, tinta)
        "claseAhora" -> claseAhora(variante, t, tinta)
        "errorShake" -> errorAviso(variante, t, tinta)
        "saludo" -> saludo(variante, t, tinta)
        "fabScroll" -> fabScroll(variante, t, tinta)
        "haptica" -> haptica(variante, t, tinta)
    }
}

// ---------------------------------------------------------------------- base

private fun DrawScope.velocidad(v: String, t: Float, c: TintaDemo) {
    val recorrido = 68f
    val avance = when (v) {
        // «Nada» no es lento: es que no hay recorrido. El punto está aquí o está allí.
        "instant" -> if (t < 0.5f) 0f else 1f
        "rapida" -> suave(tramo(t, 0f, 0.3f))
        "lenta" -> suave(tramo(t, 0f, 0.95f))
        else -> suave(tramo(t, 0f, 0.6f))
    }
    drawLine(c.pieza, Offset(16f, 32f), Offset(84f, 32f), strokeWidth = 2f)
    drawCircle(c.acento, radius = 6f, center = Offset(16f + recorrido * avance, 32f))
}

private fun DrawScope.rebote(v: String, t: Float, c: TintaDemo) {
    val fuerza = when (v) {
        "suave" -> 0f
        "vivo" -> 0.9f
        else -> 0.4f
    }
    val avance = if (fuerza == 0f) suave(tramo(t, 0f, 0.55f)) else muelle(tramo(t, 0f, 0.75f), fuerza)
    val y = 14f + 34f * avance
    drawLine(c.pieza, Offset(30f, 48f), Offset(70f, 48f), strokeWidth = 2f)
    drawRoundRect(
        color = c.acento,
        topLeft = Offset(38f, y - 10f),
        size = Size(24f, 20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
}

private fun DrawScope.pulsacion(v: String, t: Float, c: TintaDemo) {
    val golpe = tramo(t, 0.1f, 0.55f)
    val escala = when (v) {
        "hundir" -> 1f - 0.12f * sin(golpe * PI.toFloat())
        "rebote" -> if (golpe < 1f) 0.9f + 0.1f * muelle(golpe, 0.8f) else 1f
        else -> 1f
    }
    scale(escala, pivot = Offset(50f, 32f)) {
        drawRoundRect(
            color = c.acento,
            topLeft = Offset(26f, 22f),
            size = Size(48f, 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
        )
    }
    if (v == "onda" && golpe > 0f && golpe < 1f) {
        clipRect(26f, 22f, 74f, 42f) {
            drawCircle(
                color = c.fondo.copy(alpha = 0.55f * (1f - golpe)),
                radius = 34f * golpe,
                center = Offset(50f, 32f)
            )
        }
    }
}

private fun DrawScope.carga(v: String, t: Float, c: TintaDemo) {
    val centro = Offset(50f, 32f)
    when (v) {
        "circulo" -> {
            drawCircle(c.pieza, radius = 16f, center = centro, style = Stroke(4f))
            drawArc(
                color = c.acento,
                startAngle = t * 360f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centro.x - 16f, centro.y - 16f),
                size = Size(32f, 32f),
                style = Stroke(4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        // La gracia del indicador de M3E es que **cambia de forma mientras gira**. Sin la
        // rotación continua no se distingue de un círculo que late.
        "formas" -> {
            val formas = listOf(9 to 0.10f, 4 to 0.18f, 12 to 0.13f, 6 to 0.20f, 3 to 0.26f)
            val paso = t * formas.size
            val indice = paso.toInt().coerceIn(0, formas.size - 1)
            val siguiente = (indice + 1) % formas.size
            val mezcla = suave(paso - indice)
            val lobulos = if (mezcla < 0.5f) formas[indice].first else formas[siguiente].first
            val redondez = formas[indice].second + (formas[siguiente].second - formas[indice].second) * mezcla
            drawPath(
                path = formaLobulada(centro, 17f, lobulos, redondez, t * 2f * PI.toFloat()),
                color = c.acento
            )
        }
        "onda" -> {
            drawPath(
                path = ondaCircular(centro, 15f, 2.6f, 8, 0f, 0.75f, t * 2f * PI.toFloat()),
                color = c.acento,
                style = Stroke(3.5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        "puntos" -> {
            repeat(3) { indice ->
                val fase = ((t + indice * 0.18f) % 1f)
                val alto = abs(sin(fase * PI.toFloat()))
                drawCircle(
                    color = c.acento,
                    radius = 5f,
                    center = Offset(34f + indice * 16f, 36f - 10f * alto)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------- transiciones

private fun DrawScope.transicion(v: String, t: Float, c: TintaDemo) {
    val avance = suave(tramo(t, 0.15f, 0.7f))
    when (v) {
        "ninguna" -> panel(13f, if (avance < 0.5f) c.pieza else c.acento)
        "fundido" -> {
            panel(13f, c.pieza, alfa = 1f - avance)
            panel(13f, c.acento, alfa = avance)
        }
        "eje" -> {
            panel(13f - 90f * avance, c.pieza)
            panel(13f + 90f * (1f - avance), c.acento)
        }
        "contenedor" -> {
            panel(13f, c.pieza, alfa = 1f - avance)
            panel(13f, c.acento, alfa = avance, escala = 0.55f + 0.45f * avance)
        }
        "abajo" -> {
            panel(13f, c.pieza)
            translate(top = 62f * (1f - avance)) { panel(13f, c.acento) }
        }
        "zoom" -> {
            panel(13f, c.pieza, alfa = 1f - avance, escala = 1f + 0.35f * avance)
            panel(13f, c.acento, alfa = avance, escala = 0.7f + 0.3f * avance)
        }
    }
}

private fun DrawScope.listas(v: String, t: Float, c: TintaDemo) {
    val filas = 3
    repeat(filas) { indice ->
        val y = 12f + indice * 16f
        val retardo = when (v) {
            "escalonada" -> indice * 0.10f
            "cascada" -> indice * 0.20f
            "abanico" -> indice * 0.12f
            else -> 0f
        }
        val bruto = tramo(t, 0.08f + retardo, 0.52f + retardo)
        when (v) {
            "ninguna" -> fila(y, c.acento)
            "fundido" -> fila(y, c.acento, alfa = suave(tramo(t, 0.1f, 0.6f)))
            // Escalonada sube desde abajo, cascada **cae desde arriba** y con el doble de
            // retardo entre filas: puestas una al lado de la otra ya no se confunden.
            "escalonada" -> {
                val p = suave(bruto)
                translate(top = 14f * (1f - p)) { fila(y, c.acento, alfa = p) }
            }
            "cascada" -> {
                val p = suave(bruto)
                translate(top = -18f * (1f - p)) { fila(y, c.acento, alfa = p) }
            }
            // Escala no se desplaza: crece en su sitio, y las tres a la vez.
            "escala" -> {
                val p = suave(tramo(t, 0.1f, 0.55f))
                val ancho = 76f * (0.55f + 0.45f * p)
                fila(y, c.acento, x = 12f + (76f - ancho) / 2f, ancho = ancho, alfa = p)
            }
            // Resorte llega desde muy abajo y se pasa de largo antes de asentarse.
            "resorte" -> {
                val p = muelle(tramo(t, 0.08f, 0.75f), 0.85f)
                translate(top = 30f * (1f - p)) { fila(y, c.acento) }
            }
            "abanico" -> {
                val p = suave(bruto)
                rotate(degrees = -22f * (1f - p), pivot = Offset(12f, y + 5f)) {
                    fila(y, c.acento, alfa = p)
                }
            }
        }
    }
}

private fun DrawScope.refresco(v: String, t: Float, c: TintaDemo) {
    val centro = Offset(50f, 30f)
    when (v) {
        "circulo" -> drawArc(
            color = c.acento,
            startAngle = t * 360f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(centro.x - 14f, centro.y - 14f),
            size = Size(28f, 28f),
            style = Stroke(4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        // La onda **viaja** por el arco y respira: dibujada una vez y rotada, lo que gira es
        // una onda congelada, que es justo lo que no hace el indicador de M3E.
        "ondacirc" -> {
            val amplitud = 2f + 1.6f * sin(t * 4f * PI.toFloat())
            drawPath(
                path = ondaCircular(centro, 14f, amplitud, 9, 0f, 1f, -t * 6f * PI.toFloat()),
                color = c.pieza,
                style = Stroke(3f)
            )
            drawPath(
                path = ondaCircular(centro, 14f, amplitud, 9, t, t + 0.35f, -t * 6f * PI.toFloat()),
                color = c.acento,
                style = Stroke(4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        "formas" -> {
            val formas = listOf(9 to 0.10f, 4 to 0.18f, 12 to 0.13f)
            val paso = t * formas.size
            val indice = paso.toInt().coerceIn(0, formas.size - 1)
            drawPath(
                path = formaLobulada(centro, 15f, formas[indice].first, formas[indice].second, t * 2f * PI.toFloat()),
                color = c.acento
            )
        }
        "elastico" -> {
            val estiron = sin(t * 2f * PI.toFloat())
            drawRoundRect(
                color = c.acento,
                topLeft = Offset(centro.x - 13f, centro.y - 9f - 5f * estiron),
                size = Size(26f, 18f + 10f * estiron),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(11f, 11f)
            )
        }
        "barra" -> {
            drawRoundRect(
                color = c.pieza,
                topLeft = Offset(20f, 28f),
                size = Size(60f, 5f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )
            val ancho = 22f
            val x = 20f + (60f + ancho) * t - ancho
            clipRect(20f, 26f, 80f, 35f) {
                drawRoundRect(
                    color = c.acento,
                    topLeft = Offset(x, 28f),
                    size = Size(ancho, 5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
            }
        }
        "gota" -> {
            val caida = tramo(t, 0f, 0.7f)
            val y = 12f + 26f * suave(caida)
            val achate = 1f + 0.5f * tramo(t, 0.7f, 0.85f) - 0.5f * tramo(t, 0.85f, 1f)
            drawOval(
                color = c.acento,
                topLeft = Offset(centro.x - 8f * achate, y - 8f / achate),
                size = Size(16f * achate, 16f / achate)
            )
        }
    }
}

// ---------------------------------------------------------------------- académico

private fun DrawScope.asistencia(v: String, t: Float, c: TintaDemo) {
    val centro = Offset(50f, 32f)
    val avance = tramo(t, 0.12f, 0.6f)
    when (v) {
        "ninguna" -> {
            drawCircle(c.verde, radius = 16f, center = centro)
            visto(centro, 16f, 1f, c.fondo)
        }
        "trazo" -> {
            drawCircle(c.verde.copy(alpha = 0.25f), radius = 16f, center = centro)
            visto(centro, 16f, suave(avance), c.verde)
        }
        // El relleno sube por dentro del círculo, como un vaso que se llena.
        "relleno" -> {
            drawCircle(c.pieza, radius = 16f, center = centro)
            clipRect(centro.x - 16f, centro.y + 16f - 32f * suave(avance), centro.x + 16f, centro.y + 16f) {
                drawCircle(c.verde, radius = 16f, center = centro)
            }
            visto(centro, 16f, 1f, c.fondo.copy(alpha = suave(avance)))
        }
        "rebote" -> {
            val escala = if (avance < 1f) muelle(avance, 0.9f) else 1f
            scale(escala.coerceAtLeast(0f), pivot = centro) {
                drawCircle(c.verde, radius = 16f, center = centro)
                visto(centro, 16f, 1f, c.fondo)
            }
        }
        // Una franja verde cruza la fila entera; el visto queda detrás y aparece al pasar.
        "barrido" -> {
            fila(26f, c.pieza, x = 10f, ancho = 80f, alto = 12f)
            clipRect(10f, 26f, 10f + 80f * suave(avance), 38f) {
                fila(26f, c.verde, x = 10f, ancho = 80f, alto = 12f)
            }
            visto(Offset(24f, 32f), 9f, if (avance > 0.25f) 1f else 0f, c.fondo, grosor = 2.5f)
        }
    }
}

private fun DrawScope.notaNueva(v: String, t: Float, c: TintaDemo) {
    val avance = suave(tramo(t, 0.12f, 0.62f))
    when (v) {
        "ninguna" -> {
            fila(14f, c.pieza); fila(30f, c.acento); fila(46f, c.pieza)
        }
        // Cae desde arriba y **empuja**: las de abajo bajan con ella, que es lo que la
        // distingue de «se abre hueco», donde el hueco se abre antes de que llegue nada.
        "cae" -> {
            fila(14f, c.pieza)
            translate(top = -22f * (1f - avance)) { fila(30f, c.acento, alfa = avance) }
            translate(top = 16f * avance) { fila(30f, c.pieza) }
        }
        "lateral" -> {
            fila(14f, c.pieza); fila(46f, c.pieza)
            translate(left = 92f * (1f - avance)) { fila(30f, c.acento) }
        }
        "destello" -> {
            fila(14f, c.pieza); fila(46f, c.pieza)
            fila(30f, c.acento)
            fila(30f, c.fondo, alfa = (1f - avance) * 0.85f)
        }
        // Aquí lo que se mueve no es la fila: es el número del promedio contando.
        "contar" -> {
            fila(14f, c.pieza); fila(46f, c.pieza)
            val ancho = 10f + 44f * avance
            fila(30f, c.acento, x = 12f, ancho = ancho)
            drawCircle(c.acento, radius = 3f, center = Offset(12f + ancho + 6f, 35.5f))
        }
        "abre" -> {
            fila(14f, c.pieza)
            translate(top = 16f * avance) { fila(30f, c.pieza) }
            if (avance > 0.55f) fila(30f, c.acento, alfa = tramo(avance, 0.55f, 1f))
        }
    }
}

private fun DrawScope.subeNota(v: String, t: Float, c: TintaDemo) {
    val avance = tramo(t, 0.15f, 0.65f)
    val centro = Offset(50f, 34f)
    drawRoundRect(
        color = c.pieza,
        topLeft = Offset(30f, 22f),
        size = Size(40f, 24f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f)
    )
    when (v) {
        "ninguna" -> drawCircle(c.verde, radius = 7f, center = centro)
        "salto" -> {
            val salto = if (avance < 1f) sin(avance * PI.toFloat()) else 0f
            drawCircle(c.verde, radius = 7f, center = centro - Offset(0f, 13f * salto))
        }
        "flecha" -> {
            drawCircle(c.verde, radius = 7f, center = centro)
            val subida = suave(avance)
            val camino = Path().apply {
                val cima = Offset(76f, 34f - 16f * subida)
                moveTo(cima.x, cima.y)
                lineTo(cima.x - 5f, cima.y + 8f)
                lineTo(cima.x + 5f, cima.y + 8f)
                close()
            }
            drawPath(camino, c.verde.copy(alpha = 1f - 0.6f * subida))
        }
        "brillo" -> {
            drawCircle(c.verde.copy(alpha = 0.35f), radius = 7f + 9f * suave(avance) * (1f - avance), center = centro)
            drawCircle(c.verde, radius = 7f, center = centro)
        }
    }
}

private fun DrawScope.recupera(v: String, t: Float, c: TintaDemo) {
    val avance = tramo(t, 0.15f, 0.7f)
    val y = 26f
    when (v) {
        "seco" -> fila(y, if (avance < 0.5f) c.rojo else c.verde, x = 12f, ancho = 76f, alto = 13f)
        // El color **viaja** por el ámbar: es un degradado en el tiempo, no un corte.
        "viaje" -> {
            val color = if (avance < 0.5f) {
                mezclar(c.rojo, c.ambar, avance * 2f)
            } else {
                mezclar(c.ambar, c.verde, (avance - 0.5f) * 2f)
            }
            fila(y, color, x = 12f, ancho = 76f, alto = 13f)
        }
        "pulso" -> {
            fila(y, c.verde, x = 12f, ancho = 76f, alto = 13f)
            val pulso = abs(sin(t * 2f * PI.toFloat()))
            fila(y - 3f, c.verde.copy(alpha = 0.30f * pulso), x = 9f, ancho = 82f, alto = 19f)
        }
        // Barrido: la franja verde **entra por la izquierda** sobre el rojo, con su borde a la vista.
        "barrido" -> {
            fila(y, c.rojo, x = 12f, ancho = 76f, alto = 13f)
            val borde = 12f + 76f * suave(avance)
            clipRect(12f, y, borde, y + 13f) { fila(y, c.verde, x = 12f, ancho = 76f, alto = 13f) }
            if (avance > 0f && avance < 1f) {
                drawLine(c.fondo, Offset(borde, y), Offset(borde, y + 13f), strokeWidth = 2f)
            }
        }
        // Relevo: el rojo se **encoge** por la derecha mientras el verde crece por la izquierda.
        // Son dos piezas que se ceden el sitio, no una capa encima de otra.
        "relevo" -> {
            val p = suave(avance)
            fila(y, c.rojo, x = 12f + 76f * p, ancho = 76f * (1f - p), alto = 13f)
            fila(y, c.verde, x = 12f, ancho = 76f * p, alto = 13f)
        }
    }
}

private fun DrawScope.sello(v: String, t: Float, c: TintaDemo) {
    val centro = Offset(50f, 32f)
    val golpe = tramo(t, 0.15f, 0.55f)
    fila(14f, c.pieza, x = 16f, ancho = 68f, alto = 8f)
    when (v) {
        "ninguna" -> drawCircle(c.acento, radius = 14f, center = centro, style = Stroke(3f))
        // El sello **baja** desde muy arriba y aterriza de golpe: la escala grande al principio
        // es lo que se lee como profundidad.
        "estampa" -> {
            val escala = 2.4f - 1.4f * suave(golpe)
            val alfa = suave(tramo(t, 0.15f, 0.4f))
            scale(escala, pivot = centro) {
                drawCircle(c.acento.copy(alpha = alfa), radius = 14f, center = centro, style = Stroke(3f))
            }
        }
        "tinta" -> {
            drawCircle(c.acento.copy(alpha = 0.22f), radius = 20f * suave(golpe), center = centro)
            drawCircle(c.acento, radius = 14f, center = centro, style = Stroke(3f))
        }
        // El lacre cae, se aplasta y se recupera un poco: por eso el óvalo cambia de proporción.
        "lacre" -> {
            val caida = suave(tramo(t, 0.1f, 0.45f))
            val aplaste = 1f + 0.35f * tramo(t, 0.45f, 0.58f) - 0.35f * tramo(t, 0.58f, 0.75f)
            val y = 8f + 24f * caida
            drawOval(
                color = c.rojo,
                topLeft = Offset(centro.x - 13f * aplaste, y - 13f / aplaste),
                size = Size(26f * aplaste, 26f / aplaste)
            )
        }
        "cinta" -> {
            val ancho = 92f * suave(golpe)
            rotate(degrees = -12f, pivot = centro) {
                drawRoundRect(
                    color = c.acento,
                    topLeft = Offset(4f, 26f),
                    size = Size(ancho, 13f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }
        }
    }
}

private fun DrawScope.cierreSemestre(v: String, t: Float, c: TintaDemo) {
    val avance = suave(tramo(t, 0.12f, 0.72f))
    val piezas = listOf(
        Rect(14f, 10f, 86f, 22f),
        Rect(14f, 26f, 46f, 54f),
        Rect(52f, 26f, 86f, 38f),
        Rect(52f, 42f, 86f, 54f)
    )
    when (v) {
        "entero" -> piezas.forEach { r -> caja(r, c.acento, avance) }
        // Pieza a pieza: cada trozo tiene su turno, y se ve el orden en que se arma.
        "pieza" -> piezas.forEachIndexed { indice, r ->
            caja(r, c.acento, suave(tramo(t, 0.1f + indice * 0.14f, 0.4f + indice * 0.14f)))
        }
        "cortina" -> {
            clipRect(0f, 0f, ANCHO, ALTO * avance) { piezas.forEach { r -> caja(r, c.acento, 1f) } }
        }
        // Apilado: llegan una sobre otra desde abajo y se reparten al llegar.
        "apilado" -> piezas.forEachIndexed { indice, r ->
            val p = suave(tramo(t, 0.08f + indice * 0.12f, 0.5f + indice * 0.12f))
            translate(top = (ALTO - r.top) * (1f - p)) { caja(r, c.acento, p) }
        }
    }
}

private fun DrawScope.caja(r: Rect, color: Color, alfa: Float) {
    if (alfa <= 0f) return
    drawRoundRect(
        color = color.copy(alpha = color.alpha * alfa),
        topLeft = Offset(r.left, r.top),
        size = Size(r.width, r.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )
}

// ---------------------------------------------------------------------- tareas y notas

private fun DrawScope.celebracion(v: String, t: Float, c: TintaDemo) {
    val centro = Offset(50f, 34f)
    val golpe = tramo(t, 0.1f, 0.85f)
    visto(centro, 12f, 1f, c.pieza, grosor = 4f)
    when (v) {
        "ninguna" -> Unit
        "confeti" -> {
            val colores = listOf(c.acento, c.verde, c.ambar, c.rojo)
            repeat(12) { indice ->
                val angulo = indice / 12f * 2f * PI.toFloat() + 0.4f
                val dist = 34f * suave(golpe)
                val caida = 22f * golpe * golpe
                val punto = Offset(
                    centro.x + cos(angulo) * dist,
                    centro.y + sin(angulo) * dist * 0.7f + caida
                )
                rotate(degrees = indice * 40f + golpe * 260f, pivot = punto) {
                    drawRect(
                        color = colores[indice % colores.size].copy(alpha = 1f - golpe),
                        topLeft = punto - Offset(2.5f, 1.5f),
                        size = Size(5f, 3f)
                    )
                }
            }
        }
        "onda" -> repeat(2) { indice ->
            val p = tramo(t, 0.1f + indice * 0.18f, 0.85f + indice * 0.18f)
            if (p > 0f && p < 1f) {
                drawCircle(
                    color = c.acento.copy(alpha = 0.7f * (1f - p)),
                    radius = 8f + 34f * p,
                    center = centro,
                    style = Stroke(3f)
                )
            }
        }
        "sello" -> {
            val escala = 2.2f - 1.2f * suave(tramo(t, 0.1f, 0.45f))
            scale(escala, pivot = centro) {
                drawCircle(c.verde.copy(alpha = suave(tramo(t, 0.1f, 0.35f))), radius = 18f, center = centro, style = Stroke(3f))
            }
        }
        "destello" -> repeat(8) { indice ->
            val angulo = indice / 8f * 2f * PI.toFloat()
            val dentro = 14f + 8f * suave(golpe)
            val fuera = dentro + 10f * (1f - golpe)
            drawLine(
                color = c.ambar.copy(alpha = 1f - golpe),
                start = centro + Offset(cos(angulo) * dentro, sin(angulo) * dentro),
                end = centro + Offset(cos(angulo) * fuera, sin(angulo) * fuera),
                strokeWidth = 3f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

private fun DrawScope.tachar(v: String, t: Float, c: TintaDemo) {
    val avance = suave(tramo(t, 0.15f, 0.62f))
    val y = 32f
    fila(y - 6f, c.pieza, x = 14f, ancho = 62f, alto = 12f)
    val fin = 14f + 62f * avance
    when (v) {
        "ninguna" -> Unit
        "linea" -> drawLine(c.tinta, Offset(14f, y), Offset(fin, y), strokeWidth = 2.5f)
        // El marcador es una **banda ancha y translúcida**, no una línea: se lee como rotulador.
        "marcador" -> drawRoundRect(
            color = c.ambar.copy(alpha = 0.55f),
            topLeft = Offset(14f, y - 6f),
            size = Size(fin - 14f, 12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        "visto" -> visto(Offset(45f, y), 18f, avance, c.verde, grosor = 4f)
        "doble" -> {
            drawLine(c.tinta, Offset(14f, y - 2.5f), Offset(fin, y - 2.5f), strokeWidth = 2f)
            drawLine(c.tinta, Offset(14f, y + 2.5f), Offset(fin, y + 2.5f), strokeWidth = 2f)
        }
        // La tinta **cala** el texto: la fila se oscurece por donde ya pasó.
        "tinta" -> {
            clipRect(14f, y - 6f, fin, y + 6f) {
                fila(y - 6f, c.tinta.copy(alpha = 0.55f), x = 14f, ancho = 62f, alto = 12f)
            }
            drawLine(c.tinta, Offset(14f, y), Offset(fin, y), strokeWidth = 3f)
        }
    }
}

private fun DrawScope.latido(v: String, t: Float, c: TintaDemo) {
    val centro = Offset(50f, 32f)
    val caja = Rect(20f, 20f, 80f, 44f)
    when (v) {
        "ninguna" -> caja(caja, c.rojo, 1f)
        "pulso" -> {
            val p = abs(sin(t * 2f * PI.toFloat()))
            scale(1f + 0.09f * p, pivot = centro) { caja(caja, c.rojo, 1f) }
        }
        // Respira es lento y va en el color, no en el tamaño.
        "respira" -> caja(caja, c.rojo, 0.45f + 0.55f * (0.5f + 0.5f * sin(t * 2f * PI.toFloat())))
        "borde" -> {
            caja(caja, c.rojo.copy(alpha = 0.25f), 1f)
            drawRoundRect(
                color = c.rojo.copy(alpha = 0.4f + 0.6f * abs(sin(t * 2f * PI.toFloat()))),
                topLeft = Offset(caja.left, caja.top),
                size = Size(caja.width, caja.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                style = Stroke(3f)
            )
        }
        // El tic es **dos golpes secos y una pausa larga**: nada que ver con un latido continuo.
        "tic" -> {
            val golpe = if (t < 0.08f) 1f - t / 0.08f else if (t in 0.16f..0.24f) 1f - (t - 0.16f) / 0.08f else 0f
            scale(1f + 0.12f * golpe, pivot = centro) { caja(caja, c.rojo, 1f) }
        }
    }
}

private fun DrawScope.deshacer(v: String, t: Float, c: TintaDemo) {
    val avance = tramo(t, 0.12f, 0.68f)
    val p = suave(avance)
    fila(12f, c.pieza); fila(44f, c.pieza)
    when (v) {
        "aparece" -> fila(28f, c.acento, alfa = p)
        "vuelve" -> translate(left = 92f * (1f - p)) { fila(28f, c.acento) }
        "cae" -> translate(top = -26f * (1f - p)) { fila(28f, c.acento, alfa = p) }
        // Se despliega: no llega de ningún sitio, **crece de alto** desde una línea.
        "despliega" -> {
            val alto = 11f * p
            fila(28f + (11f - alto) / 2f, c.acento, alto = max(alto, 1f))
        }
        "rebota" -> {
            val m = if (avance < 1f) muelle(avance, 0.9f) else 1f
            translate(left = 92f * (1f - m)) { fila(28f, c.acento) }
        }
        "gira" -> rotate(degrees = -80f * (1f - p), pivot = Offset(12f, 33.5f)) {
            fila(28f, c.acento, alfa = p)
        }
        "destello" -> {
            translate(left = 92f * (1f - p)) { fila(28f, c.acento) }
            if (p > 0.85f) fila(28f, c.fondo, alfa = (1f - tramo(p, 0.85f, 1f)) * 0.8f)
        }
    }
}

private fun DrawScope.guardado(v: String, t: Float, c: TintaDemo) {
    val entra = suave(tramo(t, 0.1f, 0.35f))
    val sale = 1f - suave(tramo(t, 0.75f, 0.95f))
    val alfa = entra * sale
    fila(40f, c.pieza, x = 14f, ancho = 72f, alto = 8f)
    when (v) {
        "ninguna" -> Unit
        "pildora" -> {
            drawRoundRect(
                color = c.acento.copy(alpha = alfa),
                topLeft = Offset(30f, 14f - 6f * (1f - entra)),
                size = Size(40f, 15f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
        }
        "punto" -> drawCircle(c.verde.copy(alpha = alfa), radius = 5f, center = Offset(50f, 22f))
        "visto" -> visto(Offset(50f, 22f), 11f, entra, c.verde.copy(alpha = sale), grosor = 3f)
        // El anillo se **cierra** mientras guarda: la vuelta entera es el guardado terminado.
        "anillo" -> {
            drawCircle(c.pieza, radius = 10f, center = Offset(50f, 22f), style = Stroke(3f))
            drawArc(
                color = c.acento,
                startAngle = -90f,
                sweepAngle = 360f * suave(tramo(t, 0.1f, 0.7f)),
                useCenter = false,
                topLeft = Offset(40f, 12f),
                size = Size(20f, 20f),
                style = Stroke(3f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        "filete" -> {
            val ancho = 100f * suave(tramo(t, 0.1f, 0.6f))
            drawRect(c.acento.copy(alpha = sale), topLeft = Offset(0f, 0f), size = Size(ancho, 3f))
        }
        "nube" -> {
            val y = 24f - 4f * entra
            drawCircle(c.acento.copy(alpha = alfa), radius = 7f, center = Offset(45f, y))
            drawCircle(c.acento.copy(alpha = alfa), radius = 9f, center = Offset(55f, y - 1f))
            drawRoundRect(
                color = c.acento.copy(alpha = alfa),
                topLeft = Offset(38f, y),
                size = Size(24f, 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}

private fun DrawScope.fijar(v: String, t: Float, c: TintaDemo) {
    val avance = tramo(t, 0.12f, 0.68f)
    val p = suave(avance)
    fila(12f, c.pieza)
    fila(44f, c.pieza, alfa = 0.5f)
    val desde = 44f
    val hasta = 12f
    when (v) {
        "seco" -> fila(if (avance < 0.5f) desde else hasta, c.acento)
        "salta" -> {
            val salto = sin(p * PI.toFloat()) * 10f
            fila(desde + (hasta - desde) * p - salto, c.acento)
        }
        // Vuela en arco: se va por la derecha antes de subir, y entra por arriba.
        "vuela" -> {
            val arco = sin(p * PI.toFloat())
            translate(left = 26f * arco) { fila(desde + (hasta - desde) * p, c.acento) }
        }
        // El imán acelera: al principio casi no se mueve y al final llega de golpe.
        "iman" -> {
            val acelera = p * p * p
            fila(desde + (hasta - desde) * acelera, c.acento)
        }
        // Despega: primero se levanta —sombra debajo— y después viaja.
        "despega" -> {
            val alza = suave(tramo(t, 0.12f, 0.3f))
            val viaje = suave(tramo(t, 0.3f, 0.68f))
            val y = desde + (hasta - desde) * viaje
            fila(y + 4f * alza, c.tinta.copy(alpha = 0.18f * alza), x = 12f + 2f, ancho = 76f)
            scale(1f + 0.06f * alza, pivot = Offset(50f, y + 5f)) { fila(y, c.acento) }
        }
        "destello" -> {
            fila(desde + (hasta - desde) * p, c.acento)
            if (p > 0.8f) {
                fila(hasta - 3f, c.acento.copy(alpha = 0.35f * (1f - tramo(p, 0.8f, 1f))), x = 9f, ancho = 82f, alto = 17f)
            }
        }
    }
}

// ---------------------------------------------------------------------- gastos y avisos

private fun DrawScope.presupuesto(v: String, t: Float, c: TintaDemo) {
    val avance = suave(tramo(t, 0.1f, 0.55f))
    val pista = Rect(14f, 30f, 86f, 42f)
    val lleno = 0.72f + 0.38f * avance
    // La pista, siempre: es lo que deja ver que la barra se pasa del final.
    drawRoundRect(
        color = c.pieza,
        topLeft = Offset(pista.left, pista.top),
        size = Size(pista.width, pista.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )
    val temblor = when (v) {
        "sacude" -> sin(t * 26f * PI.toFloat()) * 3.5f * (1f - avance)
        "alerta" -> if (avance > 0.85f) sin(t * 40f * PI.toFloat()) * 2f else 0f
        else -> 0f
    }
    translate(left = temblor) {
        when (v) {
            "desborda" -> {
                // Se pasa del final **de verdad**: el sobrante se sale de la pista y gotea.
                clipRect(pista.left, pista.top - 14f, pista.right + 16f, pista.bottom + 16f) {
                    drawRoundRect(
                        color = c.rojo,
                        topLeft = Offset(pista.left, pista.top),
                        size = Size(pista.width * lleno, pista.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    if (lleno > 1f) {
                        val derrame = (lleno - 1f) * pista.width
                        drawCircle(c.rojo, radius = 3.5f, center = Offset(pista.right + 2f, pista.bottom + derrame * 0.5f))
                        drawCircle(c.rojo, radius = 2.5f, center = Offset(pista.right + 7f, pista.bottom + derrame))
                    }
                }
            }
            "grieta" -> {
                drawRoundRect(
                    color = c.rojo.copy(alpha = 0.2f + 0.8f * avance),
                    topLeft = Offset(pista.left, pista.top),
                    size = Size(pista.width, pista.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }
            "parpadeo" -> {
                val encendido = if ((t * 6f).toInt() % 2 == 0) 1f else 0.25f
                drawRoundRect(
                    color = c.rojo.copy(alpha = encendido),
                    topLeft = Offset(pista.left, pista.top),
                    size = Size(pista.width * min(lleno, 1f), pista.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                )
            }
            else -> drawRoundRect(
                color = c.rojo,
                topLeft = Offset(pista.left, pista.top),
                size = Size(pista.width * min(lleno, 1f), pista.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
    }
    when (v) {
        // La alerta pone el triángulo encima: es lo que la separa de «seco».
        "alerta" -> if (avance > 0.5f) triangulo(Offset(50f, 16f), 9f, c.rojo)
        "banner" -> {
            val baja = suave(tramo(t, 0.1f, 0.45f))
            drawRoundRect(
                color = c.rojo,
                topLeft = Offset(8f, -14f + 20f * baja),
                size = Size(84f, 16f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f)
            )
        }
        else -> Unit
    }
}

private fun DrawScope.triangulo(centro: Offset, radio: Float, color: Color) {
    val camino = Path().apply {
        moveTo(centro.x, centro.y - radio)
        lineTo(centro.x + radio, centro.y + radio * 0.8f)
        lineTo(centro.x - radio, centro.y + radio * 0.8f)
        close()
    }
    drawPath(camino, color)
}

private fun DrawScope.claseAhora(v: String, t: Float, c: TintaDemo) {
    val caja = Rect(14f, 18f, 86f, 46f)
    // Siempre verde: es lo que dice «esto está pasando ahora».
    caja(caja, c.verde.copy(alpha = 0.22f), 1f)
    drawRoundRect(
        color = c.verde,
        topLeft = Offset(caja.left, caja.top),
        size = Size(caja.width, caja.height),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(2f)
    )
    when (v) {
        "quieta" -> Unit
        "respira" -> caja(caja, c.verde.copy(alpha = 0.10f + 0.22f * (0.5f + 0.5f * sin(t * 2f * PI.toFloat()))), 1f)
        "punto" -> {
            val late = 0.5f + 0.5f * sin(t * 4f * PI.toFloat())
            drawCircle(c.verde.copy(alpha = 0.35f), radius = 5f + 4f * late, center = Offset(24f, 32f))
            drawCircle(c.verde, radius = 4f, center = Offset(24f, 32f))
        }
        // Un punto de luz recorre el **perímetro**, esquinas incluidas.
        "recorre" -> {
            val perimetro = 2f * (caja.width + caja.height)
            var d = (t * perimetro) % perimetro
            val punto = when {
                d < caja.width -> Offset(caja.left + d, caja.top)
                d < caja.width + caja.height -> Offset(caja.right, caja.top + (d - caja.width))
                d < 2f * caja.width + caja.height ->
                    Offset(caja.right - (d - caja.width - caja.height), caja.bottom)
                else -> Offset(caja.left, caja.bottom - (d - 2f * caja.width - caja.height))
            }
            drawCircle(c.verde, radius = 3.5f, center = punto)
        }
        // Brillo que barre: una franja clara cruza la tarjeta. Antes no hacía nada porque se
        // pintaba fuera del recorte y la tapaba el borde.
        "barre" -> clipRect(caja.left, caja.top, caja.right, caja.bottom) {
            val x = caja.left - 20f + (caja.width + 40f) * t
            rotate(degrees = 18f, pivot = Offset(x, caja.center.y)) {
                drawRect(
                    color = c.verde.copy(alpha = 0.45f),
                    topLeft = Offset(x - 7f, caja.top - 12f),
                    size = Size(14f, caja.height + 24f)
                )
            }
        }
    }
}

private fun DrawScope.errorAviso(v: String, t: Float, c: TintaDemo) {
    val avance = tramo(t, 0.1f, 0.6f)
    val temblor = if (v == "sacude") sin(t * 24f * PI.toFloat()) * 4f * (1f - avance) else 0f
    val alfa = when (v) {
        "parpadea" -> if ((t * 6f).toInt() % 2 == 0) 1f else 0.3f
        else -> 1f
    }
    translate(left = temblor) {
        drawRoundRect(
            color = c.rojo.copy(alpha = alfa),
            topLeft = Offset(16f, 16f),
            size = Size(68f, 20f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f, 5f),
            style = Stroke(2.5f)
        )
    }
    if (v == "entra") {
        val p = suave(avance)
        translate(top = -8f * (1f - p)) {
            fila(42f, c.rojo, x = 16f, ancho = 44f, alto = 6f, alfa = p)
        }
    }
}

// ---------------------------------------------------------------------- generales

private fun DrawScope.saludo(v: String, t: Float, c: TintaDemo) {
    val rotulo = Rect(14f, 16f, 52f, 22f)
    val nombre = Rect(14f, 28f, 74f, 42f)
    when (v) {
        "golpe" -> {
            val p = if (t > 0.15f) 1f else 0f
            caja(rotulo, c.acento, p); caja(nombre, c.tinta, p)
        }
        // El rótulo entra, y el nombre **después**: dos tiempos, no uno.
        "escalonado" -> {
            caja(rotulo, c.acento, suave(tramo(t, 0.1f, 0.35f)))
            caja(nombre, c.tinta, suave(tramo(t, 0.28f, 0.55f)))
        }
        // La máquina de escribir crece por caracteres y lleva **cursor** al final.
        "maquina" -> {
            caja(rotulo, c.acento, 1f)
            val letras = (suave(tramo(t, 0.15f, 0.7f)) * 8f).toInt()
            val ancho = nombre.width * (letras / 8f)
            caja(Rect(nombre.left, nombre.top, nombre.left + ancho, nombre.bottom), c.tinta, 1f)
            if ((t * 8f).toInt() % 2 == 0) {
                drawRect(c.acento, topLeft = Offset(nombre.left + ancho + 2f, nombre.top), size = Size(2.5f, nombre.height))
            }
        }
        // La cortina revela de arriba abajo: las dos piezas ya están, lo que se mueve es el corte.
        "cortina" -> {
            val corte = ALTO * suave(tramo(t, 0.1f, 0.6f))
            clipRect(0f, 0f, ANCHO, corte) {
                caja(rotulo, c.acento, 1f); caja(nombre, c.tinta, 1f)
            }
        }
        "lateral" -> {
            val p = suave(tramo(t, 0.1f, 0.55f))
            translate(left = -70f * (1f - p)) { caja(rotulo, c.acento, 1f); caja(nombre, c.tinta, 1f) }
        }
        // El desenfoque se finge con capas desplazadas: el efecto real vive en la app.
        "desenfoque" -> {
            val p = suave(tramo(t, 0.1f, 0.6f))
            val borrón = 6f * (1f - p)
            listOf(-borrón, 0f, borrón).forEach { desvio ->
                translate(left = desvio) {
                    caja(rotulo, c.acento.copy(alpha = 0.4f + 0.6f * p), 1f)
                    caja(nombre, c.tinta.copy(alpha = 0.4f + 0.6f * p), 1f)
                }
            }
        }
        // Letra a letra: cada bloque tiene su propio turno, y se ven los huecos entre ellos.
        "letras" -> {
            caja(rotulo, c.acento, 1f)
            repeat(6) { indice ->
                val p = suave(tramo(t, 0.12f + indice * 0.07f, 0.32f + indice * 0.07f))
                val x = nombre.left + indice * 10f
                translate(top = 6f * (1f - p)) {
                    caja(Rect(x, nombre.top, x + 8f, nombre.bottom), c.tinta, p)
                }
            }
        }
    }
}

private fun DrawScope.fabScroll(v: String, t: Float, c: TintaDemo) {
    val recogido = suave(tramo(t, 0.25f, 0.6f))
    fila(12f, c.pieza, x = 10f, ancho = 80f, alto = 7f)
    fila(23f, c.pieza, x = 10f, ancho = 80f, alto = 7f)
    when (v) {
        "fijo" -> boton(52f, 1f, 1f, c.acento)
        "encoge" -> boton(52f, 1f - 0.62f * recogido, 1f, c.acento)
        "baja" -> translate(top = 26f * recogido) { boton(52f, 1f, 1f, c.acento) }
        "desvanece" -> boton(52f, 1f, 1f - recogido, c.acento)
    }
}

private fun DrawScope.boton(y: Float, extension: Float, alfa: Float, color: Color) {
    val ancho = 20f + 34f * extension
    drawRoundRect(
        color = color.copy(alpha = alfa),
        topLeft = Offset(88f - ancho, y - 10f),
        size = Size(ancho, 20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
    )
}

private fun DrawScope.haptica(v: String, t: Float, c: TintaDemo) {
    val fuerza = when (v) {
        "ninguna" -> 0f
        "suave" -> 0.32f
        "fuerte" -> 1f
        else -> 0.62f
    }
    val pulso = exp(-4f * t) * sin(t * 22f * PI.toFloat())
    drawLine(c.pieza, Offset(12f, 32f), Offset(88f, 32f), strokeWidth = 1.5f)
    repeat(19) { indice ->
        val x = 12f + indice * 4f
        val decaimiento = exp(-2.2f * indice / 19f)
        val alto = 22f * fuerza * decaimiento * abs(pulso + sin(indice * 1.7f) * 0.3f)
        if (alto > 0.5f) {
            drawLine(
                color = c.acento,
                start = Offset(x, 32f - alto),
                end = Offset(x, 32f + alto),
                strokeWidth = 2.5f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
    if (fuerza == 0f) drawCircle(c.pieza, radius = 3f, center = Offset(50f, 32f))
}

// ---------------------------------------------------------------------- utilidad

/** Dos colores mezclados, para lo que viaja de un tono a otro. */
private fun mezclar(desde: Color, hasta: Color, fraccion: Float): Color {
    val f = fraccion.coerceIn(0f, 1f)
    return Color(
        red = desde.red + (hasta.red - desde.red) * f,
        green = desde.green + (hasta.green - desde.green) * f,
        blue = desde.blue + (hasta.blue - desde.blue) * f,
        alpha = desde.alpha + (hasta.alpha - desde.alpha) * f
    )
}

/** Sin uso directo aún; queda por si una variante necesita línea discontinua. */
internal val guiones: PathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
