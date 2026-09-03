@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.duracion
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.core.design.theme.muelleDeMovimiento
import com.unistack.app.core.design.theme.tweenDeMovimiento
import com.unistack.app.feature_user.domain.ListEntry
import com.unistack.app.feature_user.domain.LoadingStyle
import com.unistack.app.feature_user.domain.OverdueBeat
import com.unistack.app.feature_user.domain.StrikeMotion
import kotlin.math.abs
import kotlin.math.sin

/**
 * Las piezas que convierten una elección de Movimiento en píxeles.
 *
 * Cada una es la única forma de hacer su gesto en toda la app: si mañana «Tachar al completar»
 * gana una variante, se añade aquí y aparece en las cinco listas que tachan cosas, no en la
 * que alguien se acuerde de tocar.
 */

/**
 * Cómo entra la fila número [indice] de una lista.
 *
 * Se aplica al `Modifier` de la fila y ya está: el escalonado, la cascada y el abanico salen
 * del índice, y las tres que no dependen de él lo ignoran. Con el movimiento apagado devuelve
 * el modificador tal cual, sin animación que arrancar ni estado que recordar.
 */
@Composable
fun Modifier.entradaDeLista(indice: Int): Modifier {
    val estilo = motionActual().listEntry
    if (estilo == ListEntry.NINGUNA || !hayMovimiento()) return this

    val retardoMs = when (estilo) {
        ListEntry.ESCALONADA -> 38
        ListEntry.CASCADA -> 70
        ListEntry.ABANICO -> 45
        else -> 0
        // Un tope: en una lista de cuarenta materias, el retardo por índice dejaría la última
        // entrando segundo y medio después de abrir la pantalla.
    } * indice.coerceAtMost(8)

    val avance by animateFloatAsState(
        targetValue = 1f,
        animationSpec = when (estilo) {
            ListEntry.RESORTE -> muelleDeMovimiento()
            else -> tweenDeMovimiento(baseMs = 300, retrasoMs = retardoMs)
        },
        label = "entrada"
    )

    return when (estilo) {
        ListEntry.FUNDIDO -> this.alpha(avance)
        ListEntry.ESCALONADA -> this.graphicsLayer {
            alpha = avance
            translationY = 26f * (1f - avance)
        }
        ListEntry.CASCADA -> this.graphicsLayer {
            alpha = avance
            translationY = -34f * (1f - avance)
        }
        // Escala crece en su sitio y no se desplaza: es lo que la separa de las dos de arriba.
        ListEntry.ESCALA -> this.graphicsLayer {
            alpha = avance
            scaleX = 0.9f + 0.1f * avance
            scaleY = 0.9f + 0.1f * avance
        }
        // Resorte llega desde más abajo y con muelle: se pasa de largo antes de asentarse.
        ListEntry.RESORTE -> this.graphicsLayer { translationY = 70f * (1f - avance) }
        ListEntry.ABANICO -> this.graphicsLayer {
            alpha = avance
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
            rotationZ = -14f * (1f - avance)
        }
        ListEntry.NINGUNA -> this
    }
}

/**
 * El latido de lo que lleva días vencido.
 *
 * Cinco variantes que van de no moverse a marcar dos golpes secos con pausa larga; el pulso
 * continuo cansa en una lista con seis tareas atrasadas, y el tic no.
 */
@Composable
fun Modifier.latidoDeVencido(activo: Boolean): Modifier {
    val estilo = motionActual().overdueBeat
    if (!activo || estilo == OverdueBeat.NINGUNA || !hayMovimiento()) return this

    val transicion = rememberInfiniteTransition(label = "vencido")
    val ciclo by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracion(2200), easing = LinearEasing)),
        label = "vencido"
    )

    return when (estilo) {
        OverdueBeat.PULSO -> this.scale(1f + 0.03f * abs(sin(ciclo * Math.PI.toFloat())))
        OverdueBeat.RESPIRA -> this.alpha(0.68f + 0.32f * (0.5f + 0.5f * sin(ciclo * 2f * Math.PI.toFloat())))
        OverdueBeat.BORDE -> this.drawWithContent {
            drawContent()
            drawRoundRect(
                color = Color.Red.copy(alpha = 0.35f + 0.45f * abs(sin(ciclo * Math.PI.toFloat()))),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.minDimension * 0.14f),
                style = Stroke(width = 3f)
            )
        }
        // Dos golpes al principio del ciclo y silencio el resto: lo que hace un reloj, no un
        // corazón.
        OverdueBeat.TIC -> {
            val golpe = when {
                ciclo < 0.06f -> 1f - ciclo / 0.06f
                ciclo in 0.12f..0.18f -> 1f - (ciclo - 0.12f) / 0.06f
                else -> 0f
            }
            this.scale(1f + 0.045f * golpe)
        }
        OverdueBeat.NINGUNA -> this
    }
}

/**
 * Lo tachado, con la variante elegida.
 *
 * Se pinta encima del contenido y no como decoración del texto porque el marcador, el visto y
 * la tinta no son subrayados: son capas sobre la fila entera.
 */
@Composable
fun Modifier.tachadoDe(completado: Boolean, color: Color): Modifier {
    val estilo = motionActual().strikeThrough
    val avance by animateFloatAsState(
        targetValue = if (completado) 1f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 340),
        label = "tachado"
    )
    if (avance <= 0f || estilo == StrikeMotion.NINGUNA) return this

    return this.drawWithContent {
        drawContent()
        val medio = size.height / 2f
        val fin = size.width * avance
        when (estilo) {
            StrikeMotion.LINEA -> drawLine(
                color = color, start = Offset(0f, medio), end = Offset(fin, medio), strokeWidth = 2.5f
            )
            StrikeMotion.MARCADOR -> drawRoundRect(
                color = color.copy(alpha = 0.30f),
                topLeft = Offset(0f, medio - size.height * 0.34f),
                size = Size(fin, size.height * 0.68f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f)
            )
            StrikeMotion.VISTO -> {
                val r = size.height * 0.34f
                val centro = Offset(size.width * 0.5f, medio)
                val puntos = listOf(
                    centro + Offset(-r, 0f),
                    centro + Offset(-r * 0.25f, r * 0.7f),
                    centro + Offset(r, -r * 0.8f)
                )
                drawLine(color, puntos[0], puntos[0] + (puntos[1] - puntos[0]) * minOf(avance * 2f, 1f), 3f, StrokeCap.Round)
                if (avance > 0.5f) {
                    drawLine(color, puntos[1], puntos[1] + (puntos[2] - puntos[1]) * ((avance - 0.5f) * 2f), 3f, StrokeCap.Round)
                }
            }
            StrikeMotion.DOBLE -> {
                drawLine(color, Offset(0f, medio - 3f), Offset(fin, medio - 3f), 2f)
                drawLine(color, Offset(0f, medio + 3f), Offset(fin, medio + 3f), 2f)
            }
            // La tinta cala: por donde ya pasó, la fila queda velada.
            StrikeMotion.TINTA -> {
                drawRect(color = color.copy(alpha = 0.22f), size = Size(fin, size.height))
                drawLine(color, Offset(0f, medio), Offset(fin, medio), 3f)
            }
            StrikeMotion.NINGUNA -> Unit
        }
    }
}

/**
 * El indicador de espera, en la forma elegida.
 *
 * Cuatro variantes y una regla: la de M3E que cambia de forma es la de por defecto porque es la
 * única que se distingue de una barra de progreso atascada.
 */
@Composable
fun UniLoading(modifier: Modifier = Modifier) {
    when (motionActual().loading) {
        LoadingStyle.CIRCULO -> CircularProgressIndicator(modifier = modifier)
        LoadingStyle.FORMAS -> LoadingIndicator(modifier = modifier)
        LoadingStyle.ONDA -> CircularWavyProgressIndicator(modifier = modifier)
        LoadingStyle.PUNTOS -> PuntosQueSaltan(modifier = modifier)
    }
}

/** Tres puntos saltando por turnos: la espera más callada de las cuatro. */
@Composable
private fun PuntosQueSaltan(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "puntos")
    val color = MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(5.dp)
    ) {
        repeat(3) { indice ->
            val salto by transicion.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(duracion(420), delayMillis = indice * 110),
                    RepeatMode.Reverse
                ),
                label = "punto$indice"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .graphicsLayer { translationY = -10f * salto }
                    .then(Modifier)
            ) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxWidth()) {
                    drawCircle(color = color, radius = size.minDimension / 2f)
                }
            }
        }
    }
}

/**
 * Un número que cuenta desde donde estaba hasta donde va.
 *
 * Es el interruptor «Números que cuentan» de Movimiento: apagado, devuelve el valor de golpe
 * en vez de animar una décima invisible.
 */
@Composable
fun numeroQueCuenta(objetivo: Float, etiqueta: String = "numero"): Float {
    if (!motionActual().countingNumbers || !hayMovimiento()) return objetivo
    /*
     * Arranca en cero la primera vez y despues persigue el valor nuevo.
     *
     * `arrancado` es lo que separa «contar al aparecer» de «saltar al cambiar»: sin el, la
     * primera composicion ya tendria el objetivo puesto y no habria nada que contar; con el
     * puesto para siempre, cada cambio posterior contaria otra vez desde cero, que en un gasto
     * que sube de 61.000 a 61.400 seria absurdo.
     */
    var arrancado by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { arrancado = true }
    val valor by animateFloatAsState(
        targetValue = if (arrancado) objetivo else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 720),
        label = etiqueta
    )
    return valor
}
