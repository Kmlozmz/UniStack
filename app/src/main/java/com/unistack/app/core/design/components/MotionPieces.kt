@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridItemScope
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalSectionColors
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
 * Cómo se recoloca una fila cuando otra se va.
 *
 * **Es la otra mitad de [entradaDeLista], que hasta ahora no existía.** Toda lista de la app
 * tenía una entrada cuidada y después nada: borrabas una tarea y las cinco de abajo saltaban
 * a su nuevo sitio en el mismo fotograma, la fila borrada desaparecía sin irse, y quien
 * marcaba una casilla veía media pantalla cambiar de golpe. Justo el corte que se notó en
 * Académico, pero repetido en Tareas, Gastos, Notas, Materias y Plantillas.
 *
 * Se pone en la fila, dentro del `items` de la lista, y hace falta que la lista tenga `key`:
 * sin clave no hay forma de saber que la fila tercera de ahora es la cuarta de antes.
 *
 * La entrada la sigue poniendo [entradaDeLista], así que aquí no se pide ninguna —dos
 * animaciones de aparición a la vez se pelean—; lo que se añade es la salida y el
 * desplazamiento de las vecinas.
 */
@Composable
fun LazyItemScope.reacomodoDeLista(): Modifier {
    if (!hayMovimiento()) return Modifier
    return Modifier.animateItem(
        fadeInSpec = null,
        placementSpec = tweenDeMovimiento(340),
        fadeOutSpec = tweenDeMovimiento(180)
    )
}

/** La misma pieza para el mosaico de Notas, que es una rejilla y no una lista. */
@Composable
fun LazyStaggeredGridItemScope.reacomodoDeLista(): Modifier {
    if (!hayMovimiento()) return Modifier
    return Modifier.animateItem(
        fadeInSpec = null,
        placementSpec = tweenDeMovimiento(340),
        fadeOutSpec = tweenDeMovimiento(180)
    )
}

/**
 * El latido de lo que lleva días vencido.
 *
 * Cuatro variantes, mas la de no moverse, que se distinguen por **qué** se mueve y no por a
 * qué ritmo: la fila entera, su opacidad, un cerco que sale de ella o solo la franja roja del
 * borde.
 *
 * Cada una lleva además su propio compás, el del prototipo, y no uno compartido. Un cerco que
 * crece se lee de un vistazo y puede ir más rápido; una opacidad que baja necesita más tiempo
 * o parece un parpadeo. Con los cuatro a la misma velocidad, dos de ellos quedaban mal.
 */
@Composable
fun Modifier.latidoDeVencido(activo: Boolean): Modifier {
    val estilo = motionActual().overdueBeat
    if (!activo || estilo == OverdueBeat.NINGUNA || !hayMovimiento()) return this

    val periodo = when (estilo) {
        OverdueBeat.PULSO -> 1900
        OverdueBeat.RESPIRA -> 2400
        OverdueBeat.HALO -> 1800
        OverdueBeat.FRANJA -> 1700
        OverdueBeat.NINGUNA -> 2000
    }
    /*
     * **Un solo valor de ida y vuelta, no una rampa con senos encima.**
     *
     * Antes el ciclo subía de 0 a 1 en línea recta y cada variante le aplicaba su propio seno
     * para volver: cuatro fórmulas distintas para el mismo vaivén, y ninguna con la curva
     * suave de los extremos. Con `Reverse` el valor va y vuelve solo, con la misma curva que
     * el resto de la app, y lo que queda en cada variante es únicamente qué pinta.
     */
    val transicion = rememberInfiniteTransition(label = "vencido")
    val onda by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(duracion(periodo), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vencido"
    )
    // El rojo de la app, que cambia con el tema. Antes era el rojo puro del sistema: el mismo
    // #FF0000 en claro y en oscuro, y en oscuro chillaba al lado de todo lo demás.
    val rojo = LocalSectionColors.current.expenses

    return when (estilo) {
        OverdueBeat.PULSO -> this.scale(1f + 0.028f * onda)
        OverdueBeat.RESPIRA -> this.alpha(1f - 0.38f * onda)
        /*
         * **Un cerco que sale de la fila, no un contorno pegado a ella.**
         *
         * Tenía dos aros: uno fuera y otro de 2 píxeles justo en el borde, que es exactamente
         * el contorno que esta variante no quiere ser. Y medía en píxeles crudos, así que en un
         * móvil de 3x el halo entero ocupaba dos décimas de milímetro y no se veía.
         *
         * Ahora es un solo anillo que crece de 0 a 3 dp hacia fuera mientras aparece: lo mismo
         * que hacía la sombra del prototipo, medido en dp y con el rojo del tema.
         */
        OverdueBeat.HALO -> this.drawWithContent {
            drawContent()
            val grosor = 3.dp.toPx() * onda
            if (grosor > 0.4f) {
                val radio = minOf(12.dp.toPx(), size.minDimension * 0.2f)
                drawRoundRect(
                    color = rojo.copy(alpha = 0.35f * onda),
                    topLeft = Offset(-grosor / 2f, -grosor / 2f),
                    size = Size(size.width + grosor, size.height + grosor),
                    cornerRadius = CornerRadius(radio + grosor / 2f),
                    style = Stroke(width = grosor)
                )
            }
        }
        /*
         * **La fila se queda quieta y late lo único que ya decía «esto está vencido».**
         *
         * La franja se dibuja entera y se apaga a la mitad, en vez de encogerse hasta poco más
         * de la mitad de la fila: encogiendo tanto dejaba de parecer un borde y pasaba a
         * parecer una barra de progreso a medias.
         *
         * Se recorta por arriba y por abajo lo que miden las esquinas redondeadas de la
         * tarjeta, porque justo ahí el borde izquierdo ya no es recto y la franja se salía.
         */
        OverdueBeat.FRANJA -> this.drawWithContent {
            drawContent()
            val radio = minOf(12.dp.toPx(), size.minDimension * 0.2f)
            val ancho = 3.dp.toPx()
            val alto = (size.height - radio * 2f).coerceAtLeast(0f) * (1f - 0.18f * onda)
            drawRoundRect(
                color = rojo.copy(alpha = 1f - 0.65f * onda),
                topLeft = Offset(0f, (size.height - alto) / 2f),
                size = Size(ancho, alto),
                cornerRadius = CornerRadius(ancho / 2f)
            )
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
            /*
             * **No se veia nada**, y el motivo era un `fillMaxWidth` de mas.
             *
             * El `Canvas` estaba dentro de una caja de ocho puntos pidiendo solo el ancho, asi
             * que se quedaba con alto cero y no pintaba: los tres puntos estaban ahi, midiendo
             * ocho por cero. Con `background` no hace falta lienzo ninguno.
             */
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .graphicsLayer { translationY = -10f * salto }
                    .background(color, androidx.compose.foundation.shape.CircleShape)
            )
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

/**
 * El aviso de un campo mal rellenado, con la variante elegida.
 *
 * Se aplica al `Modifier` del campo y ya esta: el rojo lo pone el propio `isError` de Material,
 * y esto anade lo que el rojo no dice —que **acaba** de pasar algo—. Es la diferencia entre un
 * campo que estaba mal desde el principio y uno que se acaba de equivocar.
 *
 * La sacudida y el parpadeo se disparan una sola vez al pasar a error y no mientras dure: un
 * campo temblando sin parar mientras se escribe es peor que uno quieto en rojo.
 */
@Composable
fun Modifier.avisoDeError(hayError: Boolean): Modifier {
    if (!hayMovimiento()) return this

    // Se reinicia cada vez que se entra en error, y no cada recomposicion: escribiendo, el
    // campo recompone en cada tecla y el aviso no puede repetirse en cada una.
    var golpe by remember { mutableStateOf(0f) }
    LaunchedEffect(hayError) { golpe = if (hayError) 1f else 0f }
    val avance by animateFloatAsState(
        targetValue = golpe,
        animationSpec = tweenDeMovimiento(baseMs = 480),
        label = "error"
    )
    if (avance <= 0.01f) return this

    // Tres idas y venidas que se apagan: un temblor que no decae se lee como un fallo.
    return this.graphicsLayer {
        translationX = sin(avance * 6f * Math.PI.toFloat()) * 9f * (1f - avance)
    }
}
