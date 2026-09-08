@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import com.unistack.app.feature_user.domain.CutSealMotion
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.duracion
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.core.design.theme.muelleDeMovimiento
import com.unistack.app.core.design.theme.tweenDeMovimiento
import com.unistack.app.feature_user.domain.AttendanceMotion
import com.unistack.app.feature_user.domain.AutosaveMotion
import com.unistack.app.feature_user.domain.CelebrationMotion
import com.unistack.app.feature_user.domain.ClassNowMotion
import com.unistack.app.feature_user.domain.FabScrollMotion
import com.unistack.app.feature_user.domain.NewGradeMotion
import com.unistack.app.feature_user.domain.GradeUpMotion
import com.unistack.app.feature_user.domain.OverBudgetMotion
import com.unistack.app.feature_user.domain.PinMotion
import com.unistack.app.feature_user.domain.RecoveryMotion
import com.unistack.app.feature_user.domain.TermCloseMotion
import com.unistack.app.feature_user.domain.UndoMotion
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Los momentos de la app en los que un gesto de Movimiento tiene que ocurrir.
 *
 * [MotionPieces] tiene lo que se aplica a un `Modifier` —una entrada de lista, un tachado, un
 * latido—. Aquí van los que **son un momento**: algo pasa una vez, se celebra o se avisa, y
 * después la pantalla vuelve a estar quieta. Por eso casi todos reciben un disparador en vez
 * de un estado permanente.
 *
 * La regla es la misma de siempre: quien quiera el gesto llama a la pieza, no lee la
 * preferencia. Así, apagar el movimiento apaga los veinticinco de golpe y añadir una variante
 * es tocar un solo sitio.
 */

// ------------------------------------------------------------------ el corte que se cierra

/**
 * El sello de «cerrado» sobre el corte que se acaba de completar.
 *
 * **«Sello al cerrar un corte» era el único gesto del catálogo que no ocurría nunca.** Estaba
 * dibujado en su vista previa, se guardaba y se leía de las preferencias, y ninguna pantalla lo
 * llamaba: la tarjeta del corte pasaba a completada sin más.
 *
 * El disparo no puede vivir dentro de la tarjeta. Al completarse, el corte **cambia de lista**
 * —sale de los abiertos y entra en los cerrados— y con eso su tarjeta se compone de cero: mire
 * lo que mire, para ella el corte siempre ha estado cerrado. Por eso quien lo dispara es la
 * pantalla, que es la única que ve las dos listas a la vez, igual que hace la celebración del
 * día con la última pendiente.
 */
@Composable
fun Modifier.selloDeCorte(disparado: Boolean, onTerminado: () -> Unit): Modifier {
    val estilo = motionActual().cutSeal
    if (estilo == CutSealMotion.NINGUNA || !hayMovimiento()) {
        LaunchedEffect(disparado) { if (disparado) onTerminado() }
        return this
    }

    val avance by animateFloatAsState(
        targetValue = if (disparado) 1f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 1200),
        label = "sello",
        finishedListener = { if (it >= 1f) onTerminado() }
    )
    if (avance <= 0f) return this

    return this.drawWithContent {
        drawContent()
        val centro = Offset(size.width / 2f, size.height / 2f)
        val corto = size.minDimension
        // Se apaga en el último cuarto: el sello marca el momento y despues deja ver la
        // tarjeta, que es lo que se ha venido a consultar.
        val vida = if (avance < 0.75f) 1f else 1f - (avance - 0.75f) / 0.25f
        val verde = Color(0xFF11C045)

        when (estilo) {
            CutSealMotion.ESTAMPA, CutSealMotion.TINTA -> {
                val posado = (avance / 0.35f).coerceAtMost(1f)
                if (estilo == CutSealMotion.TINTA) {
                    drawCircle(
                        color = verde.copy(alpha = 0.16f * vida),
                        radius = corto * 1.1f * posado,
                        center = centro
                    )
                }
                val escala = 2.2f - 1.2f * posado
                rotate(degrees = -14f, pivot = centro) {
                    drawRoundRect(
                        color = verde.copy(alpha = vida),
                        topLeft = Offset(
                            centro.x - corto * 0.9f * escala,
                            centro.y - corto * 0.30f * escala
                        ),
                        size = Size(corto * 1.8f * escala, corto * 0.60f * escala),
                        cornerRadius = CornerRadius(corto * 0.14f, corto * 0.14f),
                        style = Stroke(corto * 0.06f)
                    )
                }
            }
            // El lacre cae, se aplasta al llegar y se recupera.
            CutSealMotion.LACRE -> {
                val caida = (avance / 0.45f).coerceAtMost(1f)
                val aplaste = 1f + 0.3f * ((avance - 0.45f) / 0.13f).coerceIn(0f, 1f) -
                    0.3f * ((avance - 0.58f) / 0.17f).coerceIn(0f, 1f)
                val y = size.height * (0.1f + 0.4f * caida)
                val r = corto * 0.32f
                drawOval(
                    color = Color(0xFFD81C00).copy(alpha = vida),
                    topLeft = Offset(size.width * 0.78f - r * aplaste, y - r / aplaste),
                    size = Size(r * 2f * aplaste, r * 2f / aplaste)
                )
            }
            // La cinta cruza la tarjeta entera, como el precinto de una caja.
            CutSealMotion.CINTA -> {
                val largo = size.width * 1.3f * (avance / 0.55f).coerceAtMost(1f)
                rotate(degrees = -10f, pivot = centro) {
                    drawRect(
                        color = verde.copy(alpha = 0.85f * vida),
                        topLeft = Offset(-size.width * 0.15f, centro.y - corto * 0.22f),
                        size = Size(largo, corto * 0.44f)
                    )
                }
            }
            CutSealMotion.NINGUNA -> Unit
        }
    }
}

// ------------------------------------------------------------------ celebrar al terminar

/**
 * La celebración de haber cerrado la última pendiente del día.
 *
 * Se pinta **encima** de lo que envuelva, sin ocupar sitio, para que la lista no dé un salto al
 * aparecer. Dura lo suyo y se apaga sola: [visible] vuelve a falso cuando termina.
 */
@Composable
fun Modifier.celebracionDelDia(disparada: Boolean, onTerminada: () -> Unit): Modifier {
    val estilo = motionActual().celebration
    if (estilo == CelebrationMotion.NINGUNA || !hayMovimiento()) {
        // Sin celebración, el aviso se cierra al momento: si no, quien la apagó se queda con
        // un estado encendido para siempre.
        LaunchedEffect(disparada) { if (disparada) onTerminada() }
        return this
    }

    val avance by animateFloatAsState(
        targetValue = if (disparada) 1f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 1100),
        label = "celebracion",
        finishedListener = { if (it >= 1f) onTerminada() }
    )
    if (avance <= 0f) return this

    // Las partículas se sortean una sola vez: con un sorteo por fotograma, el confeti tiembla
    // en vez de volar.
    val semillas = remember { List(16) { Random(it * 7919).nextFloat() } }

    return this.drawWithContent {
        drawContent()
        val centro = Offset(size.width / 2f, size.height / 2f)
        when (estilo) {
            CelebrationMotion.CONFETI -> semillas.forEachIndexed { indice, semilla ->
                val angulo = indice / semillas.size.toFloat() * 2f * PI.toFloat() + semilla
                val dist = size.minDimension * 0.9f * avance
                val punto = Offset(
                    centro.x + cos(angulo) * dist,
                    centro.y + sin(angulo) * dist * 0.55f + size.height * 0.5f * avance * avance
                )
                rotate(degrees = semilla * 360f + avance * 300f, pivot = punto) {
                    drawRect(
                        color = paletaDeConfeti[indice % paletaDeConfeti.size].copy(alpha = 1f - avance),
                        topLeft = punto - Offset(5f, 2.5f),
                        size = Size(10f, 5f)
                    )
                }
            }
            CelebrationMotion.ONDA -> repeat(2) { indice ->
                val p = ((avance - indice * 0.18f) / 0.82f).coerceIn(0f, 1f)
                if (p > 0f && p < 1f) {
                    drawCircle(
                        color = Color(0xFF7F77DD).copy(alpha = 0.6f * (1f - p)),
                        radius = size.minDimension * (0.2f + 0.9f * p),
                        center = centro,
                        style = Stroke(4f)
                    )
                }
            }
            CelebrationMotion.SELLO -> {
                val escala = 2.2f - 1.2f * minOf(avance * 2.5f, 1f)
                drawCircle(
                    color = Color(0xFF11C045).copy(alpha = (1f - avance) * 0.9f),
                    radius = size.minDimension * 0.32f * escala,
                    center = centro,
                    style = Stroke(4f)
                )
            }
            CelebrationMotion.DESTELLO -> repeat(10) { indice ->
                val angulo = indice / 10f * 2f * PI.toFloat()
                val dentro = size.minDimension * (0.3f + 0.25f * avance)
                val fuera = dentro + size.minDimension * 0.2f * (1f - avance)
                drawLine(
                    color = Color(0xFFE0A400).copy(alpha = 1f - avance),
                    start = centro + Offset(cos(angulo) * dentro, sin(angulo) * dentro * 0.6f),
                    end = centro + Offset(cos(angulo) * fuera, sin(angulo) * fuera * 0.6f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
            CelebrationMotion.NINGUNA -> Unit
        }
    }
}

/** Los colores del confeti. Fijos a propósito: una celebración no cambia con el tema. */
private val paletaDeConfeti = listOf(
    Color(0xFF7F77DD), Color(0xFFE8693A), Color(0xFF5FC96E), Color(0xFFE0A63C), Color(0xFF3FC7B4)
)

// ------------------------------------------------------------------ marcar asistencia

/**
 * Lo que hace la marca de asistencia al confirmarse.
 *
 * Se aplica al círculo o a la fila que se marca; el trazo del visto lo sigue dibujando quien lo
 * pinte, y esto pone lo que rodea: el rebote, el relleno que sube o el barrido.
 */
@Composable
fun Modifier.marcaDeAsistencia(marcada: Boolean, color: Color): Modifier {
    val estilo = motionActual().attendance
    if (estilo == AttendanceMotion.NINGUNA || !hayMovimiento()) return this

    val avance by animateFloatAsState(
        targetValue = if (marcada) 1f else 0f,
        animationSpec = if (estilo == AttendanceMotion.REBOTE) {
            muelleDeMovimiento()
        } else {
            tweenDeMovimiento(baseMs = 420)
        },
        label = "asistencia"
    )
    if (avance <= 0f) return this

    /*
     * **Las cuatro se pintan por delante, y «trazo» existe.**
     *
     * Aqui habia dos fallos que dejaban el gesto en nada. «Trazo», que es el que viene puesto
     * de fabrica, caia en un `else` vacio: marcar asistencia no hacia absolutamente nada para
     * quien no hubiera cambiado el ajuste. Y «relleno» y «barrido» pintaban con `drawBehind`,
     * o sea **por detras** del boton, que cuando queda elegido es opaco: el color se dibujaba
     * donde no se puede ver.
     *
     * Ahora las tres que pintan lo hacen sobre el contenido y con transparencia, que deja leer
     * el rotulo por debajo, y «trazo» dibuja el contorno del boton recorriendolo: es la unica
     * de las cuatro que no tapa nada, y por eso es la de casa.
     */
    return when (estilo) {
        AttendanceMotion.REBOTE -> this.scale(avance.coerceAtLeast(0.01f))

        // El trazo recorre el contorno hasta cerrarlo.
        AttendanceMotion.TRAZO -> this.drawWithContent {
            drawContent()
            val radio = size.height / 2f
            val camino = Path().apply {
                addRoundRect(
                    androidx.compose.ui.geometry.RoundRect(
                        rect = androidx.compose.ui.geometry.Rect(
                            Offset(1.5f, 1.5f),
                            Size(size.width - 3f, size.height - 3f)
                        ),
                        cornerRadius = CornerRadius(radio, radio)
                    )
                )
            }
            val medida = PathMeasure().apply { setPath(camino, false) }
            val trozo = Path()
            medida.getSegment(0f, medida.length * avance, trozo, true)
            drawPath(trozo, color = color, style = Stroke(3f, cap = StrokeCap.Round))
        }

        // El relleno sube por dentro, como un vaso que se llena.
        AttendanceMotion.RELLENO -> this.drawWithContent {
            drawContent()
            drawRect(
                color = color.copy(alpha = 0.30f),
                topLeft = Offset(0f, size.height * (1f - avance)),
                size = Size(size.width, size.height * avance)
            )
        }

        // Una franja cruza de izquierda a derecha.
        AttendanceMotion.BARRIDO -> this.drawWithContent {
            drawContent()
            drawRoundRect(
                color = color.copy(alpha = 0.26f),
                size = Size(size.width * avance, size.height),
                cornerRadius = CornerRadius(size.height * 0.5f, size.height * 0.5f)
            )
        }

        AttendanceMotion.NINGUNA -> this
    }
}

// ------------------------------------------------------------------ materia que se recupera

/**
 * El color de una materia que acaba de salir del rojo.
 *
 * Devuelve el color que toca pintar **ahora mismo**: con «viaje» pasa por el ámbar antes de
 * llegar al verde, y con «seco» cambia de golpe. Quien lo llama pinta con lo que reciba y no
 * tiene que saber qué variante hay puesta.
 */
@Composable
fun colorDeRecuperacion(recuperada: Boolean, riesgo: Color, aviso: Color, alDia: Color): Color {
    val estilo = motionActual().recovery
    if (estilo == RecoveryMotion.SECO || !hayMovimiento()) {
        return if (recuperada) alDia else riesgo
    }
    val avance by animateFloatAsState(
        targetValue = if (recuperada) 1f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 700),
        label = "recuperacion"
    )
    // El viaje pasa por el ámbar: es un degradado en el tiempo, no un corte.
    return if (estilo == RecoveryMotion.VIAJE) {
        if (avance < 0.5f) mezclarColor(riesgo, aviso, avance * 2f) else mezclarColor(aviso, alDia, (avance - 0.5f) * 2f)
    } else {
        mezclarColor(riesgo, alDia, avance)
    }
}

private fun mezclarColor(desde: Color, hasta: Color, f: Float): Color {
    val p = f.coerceIn(0f, 1f)
    return Color(
        red = desde.red + (hasta.red - desde.red) * p,
        green = desde.green + (hasta.green - desde.green) * p,
        blue = desde.blue + (hasta.blue - desde.blue) * p,
        alpha = desde.alpha + (hasta.alpha - desde.alpha) * p
    )
}

// ------------------------------------------------------------------ fijar una nota

/** Cómo se mueve una nota al subir a las fijadas, o al bajar de ellas. */
@Composable
fun Modifier.notaFijada(fijada: Boolean): Modifier {
    val estilo = motionActual().pinNote
    if (estilo == PinMotion.SECO || !hayMovimiento()) return this

    val avance by animateFloatAsState(
        targetValue = if (fijada) 1f else 0f,
        animationSpec = when (estilo) {
            PinMotion.SALTA, PinMotion.DESPEGA -> muelleDeMovimiento()
            else -> tweenDeMovimiento(baseMs = 460)
        },
        label = "fijada"
    )
    // El gesto es de ida: pasado el pico, la fila vuelve a su sitio y no se queda torcida.
    val pico = abs(sin(avance * PI.toFloat()))
    if (pico <= 0.01f) return this

    return when (estilo) {
        PinMotion.SALTA -> this.graphicsLayer { translationY = -26f * pico }
        PinMotion.VUELA -> this.graphicsLayer {
            translationY = -26f * pico
            translationX = 30f * pico
        }
        PinMotion.IMAN -> this.graphicsLayer { translationY = -18f * pico * pico }
        PinMotion.DESPEGA -> this.graphicsLayer {
            scaleX = 1f + 0.06f * pico
            scaleY = 1f + 0.06f * pico
            shadowElevation = 14f * pico
        }
        PinMotion.DESTELLO -> this.drawWithContent {
            drawContent()
            drawRoundRect(
                color = Color.White.copy(alpha = 0.28f * pico),
                cornerRadius = CornerRadius(size.height * 0.25f)
            )
        }
        PinMotion.SECO -> this
    }
}

// ------------------------------------------------------------------ guardado automático

/**
 * El aviso de que la nota quedó guardada, en la forma elegida.
 *
 * Va dentro de un `Box` sobre el contenido y se apaga solo. Recibe un contador y no un booleano
 * porque dos guardados seguidos tienen que dar dos avisos: con un booleano, el segundo no
 * cambia nada y el aviso no vuelve a salir.
 */
@Composable
fun AvisoDeGuardado(marca: Int, modifier: Modifier = Modifier) {
    val estilo = motionActual().autosave
    if (estilo == AutosaveMotion.NINGUNA || !hayMovimiento() || marca == 0) return

    var visible by remember(marca) { mutableStateOf(true) }
    LaunchedEffect(marca) {
        visible = true
        kotlinx.coroutines.delay(1600)
        visible = false
    }
    val alfa by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 260),
        label = "guardado"
    )
    if (alfa <= 0.01f) return

    val esquema = MaterialTheme.colorScheme
    when (estilo) {
        AutosaveMotion.FILETE -> Box(
            modifier = modifier
                .fillMaxSize()
                .drawWithContent {
                    drawRect(
                        color = esquema.primary.copy(alpha = alfa),
                        size = Size(size.width, 3.dp.toPx())
                    )
                }
        )
        AutosaveMotion.PUNTO -> Box(
            modifier = modifier
                .alpha(alfa)
                .padding(6.dp)
                .background(esquema.primary, RoundedCornerShape(50))
                .padding(4.dp)
        )
        else -> Box(
            modifier = modifier.alpha(alfa),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(esquema.primary, RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (estilo == AutosaveMotion.VISTO) "✓" else "Guardado",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = esquema.onPrimary
                )
            }
        }
    }
}

// ------------------------------------------------------------------ pasarse del presupuesto

/**
 * Lo que hace la barra de presupuesto al cruzar el límite.
 *
 * `pasado` es la condición, no un disparo: mientras se esté por encima, el aviso sigue puesto.
 * Un aviso de dinero que se apaga solo deja de avisar justo cuando más falta hace.
 */
@Composable
fun Modifier.avisoDePresupuesto(pasado: Boolean): Modifier {
    val estilo = motionActual().overBudget
    if (!pasado || estilo == OverBudgetMotion.SECO || !hayMovimiento()) return this

    val ciclo = rememberInfiniteTransition(label = "presupuesto")
    val t by ciclo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracion(1800), easing = LinearEasing)),
        label = "presupuesto"
    )

    return when (estilo) {
        // Sacude una vez cada vuelta y se queda quieta: temblar sin parar cansa y deja de avisar.
        OverBudgetMotion.SACUDE -> this.graphicsLayer {
            translationX = if (t < 0.2f) sin(t * 30f * PI.toFloat()) * 6f * (1f - t / 0.2f) else 0f
        }
        OverBudgetMotion.PARPADEO -> this.alpha(if ((t * 6f).toInt() % 2 == 0) 1f else 0.45f)
        OverBudgetMotion.ALERTA -> this.graphicsLayer {
            scaleX = 1f + 0.015f * abs(sin(t * 2f * PI.toFloat()))
            scaleY = 1f + 0.03f * abs(sin(t * 2f * PI.toFloat()))
        }
        else -> this
    }
}

// ------------------------------------------------------------------ clase en curso

/**
 * La clase que está pasando ahora mismo.
 *
 * **En verde siempre**, que es lo que dice «activo»: en el acento competía con todo lo demás
 * que va del acento y no se leía como un estado.
 */
@Composable
fun Modifier.claseEnCurso(enCurso: Boolean, verde: Color): Modifier {
    val estilo = motionActual().classNow
    if (!enCurso || estilo == ClassNowMotion.QUIETA || !hayMovimiento()) return this

    val ciclo = rememberInfiniteTransition(label = "claseAhora")
    val t by ciclo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracion(2400), easing = LinearEasing), RepeatMode.Restart),
        label = "claseAhora"
    )

    return when (estilo) {
        ClassNowMotion.RESPIRA -> this.clipToBounds().drawBehind {
            val p = 0.5f + 0.5f * sin(t * 2f * PI.toFloat())
            drawRoundRect(
                color = verde.copy(alpha = 0.04f + 0.12f * p),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )
        }
        ClassNowMotion.PUNTO -> this.clipToBounds().drawWithContent {
            drawContent()
            val late = 0.5f + 0.5f * sin(t * 4f * PI.toFloat())
            val r = 3.dp.toPx()
            val centro = Offset(8.dp.toPx(), size.height / 2f)
            drawCircle(verde.copy(alpha = 0.35f * late), radius = r * 2f, center = centro)
            drawCircle(verde, radius = r, center = centro)
        }
        // Un punto de luz recorre el perímetro interior, esquinas incluidas e inset para no salirse.
        ClassNowMotion.RECORRE -> this.clipToBounds().drawWithContent {
            drawContent()
            clipRect(0f, 0f, size.width, size.height) {
                val r = 2.5.dp.toPx()
                val inset = 3.dp.toPx()
                val w = (size.width - 2 * inset).coerceAtLeast(1f)
                val h = (size.height - 2 * inset).coerceAtLeast(1f)
                val perimetro = 2f * (w + h)
                val d = (t * perimetro) % perimetro
                val punto = when {
                    d < w -> Offset(inset + d, inset)
                    d < w + h -> Offset(size.width - inset, inset + (d - w))
                    d < 2f * w + h -> Offset(size.width - inset - (d - w - h), size.height - inset)
                    else -> Offset(inset, size.height - inset - (d - 2f * w - h))
                }
                drawCircle(verde.copy(alpha = 0.35f), radius = r * 1.8f, center = punto)
                drawCircle(verde, radius = r, center = punto)
            }
        }
        ClassNowMotion.BARRE -> this.clipToBounds().drawWithContent {
            drawContent()
            clipRect(0f, 0f, size.width, size.height) {
                val x = -size.width * 0.3f + size.width * 1.6f * t
                rotate(degrees = 15f, pivot = Offset(x, size.height / 2f)) {
                    drawRect(
                        color = verde.copy(alpha = 0.16f),
                        topLeft = Offset(x - 20.dp.toPx(), -size.height),
                        size = Size(40.dp.toPx(), size.height * 3f)
                    )
                }
            }
        }
        ClassNowMotion.QUIETA -> this
    }
}

// ------------------------------------------------------------------ botón que se recoge

/**
 * El botón de crear, recogiéndose al bajar por una lista larga.
 *
 * `recogido` es «se está bajando ahora mismo», no «se ha bajado mucho»: lo que molesta es el
 * botón tapando contenido **mientras se busca algo**, y en cuanto se para de bajar vuelve.
 *
 * «Encoge» pierde primero el ancho y después el resto, que es lo que hace que se lea como un
 * botón que se recoge y no como uno que se estruja: en un botón sin rótulo —el de notas es un
 * más— la escala es lo único que queda, y por eso baja al 72% en vez de al 88%.
 */
@Composable
fun Modifier.botonQueSeRecoge(recogido: Boolean): Modifier {
    val estilo = motionActual().fabOnScroll
    if (estilo == FabScrollMotion.FIJO || !hayMovimiento()) return this

    val avance by animateFloatAsState(
        targetValue = if (recogido) 1f else 0f,
        animationSpec = muelleDeMovimiento(),
        label = "fab"
    )
    if (avance <= 0.01f) return this

    return when (estilo) {
        FabScrollMotion.ENCOGE -> this.graphicsLayer {
            scaleX = 1f - 0.28f * avance
            scaleY = 1f - 0.28f * avance
        }
        // Baja lo suyo y algo mas: tiene que salirse de la pantalla, no quedarse a medias.
        FabScrollMotion.BAJA -> this.graphicsLayer { translationY = 190f * avance }
        FabScrollMotion.DESVANECE -> this.graphicsLayer { alpha = 1f - avance }
        FabScrollMotion.FIJO -> this
    }
}

/**
 * Si se esta bajando ahora mismo por la pantalla.
 *
 * Se mira el **sentido** del gesto y no la posicion: escondido por posicion, el boton se
 * quedaria fuera al final de la lista, que es justo donde hace falta para crear algo nuevo.
 *
 * Escucha el scroll anidado en vez de un `LazyListState` a proposito. Notas tiene dos
 * disposiciones —rejilla y cuaderno— con dos tipos de estado distintos, y la conexion sirve
 * para las dos, para las que vengan, y para pantallas que ni siquiera usen una lista perezosa.
 *
 * Devuelve el estado y la conexion: el estado lo lee el boton, y la conexion se aplica al
 * contenedor que se desplaza.
 */
class DesplazamientoDeLista internal constructor() {
    var bajando by mutableStateOf(false)
        internal set

    val conexion: NestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Un umbral pequeno: sin el, el temblor del dedo al parar cambia el sentido y el
            // boton parpadea entre recogido y entero.
            if (available.y < -1.5f) bajando = true
            if (available.y > 1.5f) bajando = false
            return Offset.Zero
        }
    }
}

@Composable
fun rememberDesplazamientoDeLista(): DesplazamientoDeLista = remember { DesplazamientoDeLista() }

// ------------------------------------------------------------------ cierre de semestre

/**
 * Cómo aparece el resumen de un periodo cerrado.
 *
 * Recibe el índice de la pieza para poder escalonarlas: «pieza a pieza» y «apilado» necesitan
 * saber cuál va antes, y las otras dos lo ignoran. Se dispara una vez al abrir la pantalla,
 * porque un resumen que se rearma al desplazarse cansa a la tercera vez.
 */
@Composable
fun Modifier.resumenDePeriodo(indice: Int): Modifier {
    val estilo = motionActual().termClose
    if (!hayMovimiento()) return this

    var dentro by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { dentro = true }
    val retardo = when (estilo) {
        TermCloseMotion.PIEZA -> indice * 90
        TermCloseMotion.APILADO -> indice * 70
        else -> 0
    }
    val avance by animateFloatAsState(
        targetValue = if (dentro) 1f else 0f,
        animationSpec = if (estilo == TermCloseMotion.APILADO) {
            muelleDeMovimiento()
        } else {
            tweenDeMovimiento(baseMs = 420, retrasoMs = retardo)
        },
        label = "periodo$indice"
    )

    return when (estilo) {
        TermCloseMotion.ENTERO -> this.alpha(avance)
        TermCloseMotion.PIEZA -> this.graphicsLayer {
            alpha = avance
            translationY = 18f * (1f - avance)
        }
        // La cortina revela de arriba abajo: nada se mueve, lo que baja es el corte.
        TermCloseMotion.CORTINA -> this.graphicsLayer {
            alpha = if (avance > 0f) 1f else 0f
            clip = true
            scaleY = avance
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
        }
        // Apilado: llegan desde abajo, una sobre otra, y se reparten al llegar.
        TermCloseMotion.APILADO -> this.graphicsLayer {
            alpha = avance
            translationY = 90f * (1f - avance) * (indice + 1)
        }
    }
}

// ------------------------------------------------------------------ registrar una nota

/**
 * Cómo entra una nota recién registrada en la lista de su corte.
 *
 * `esNueva` lo decide quien pinta la lista comparando con lo que había: una nota es nueva
 * mientras sea la última añadida y la pantalla no se haya vuelto a abrir. Sin esa condición,
 * todas las notas entrarían animadas cada vez que se abre la materia, y entonces la entrada
 * deja de significar «acaba de pasar algo».
 */
@Composable
fun Modifier.notaRecienRegistrada(esNueva: Boolean): Modifier {
    val estilo = motionActual().newGrade
    if (!esNueva || estilo == NewGradeMotion.NINGUNA || !hayMovimiento()) return this

    var dentro by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { dentro = true }
    val avance by animateFloatAsState(
        targetValue = if (dentro) 1f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 460),
        label = "notaNueva"
    )

    return when (estilo) {
        // Cae desde arriba y empuja: las de abajo se apartan con ella.
        NewGradeMotion.CAE -> this.graphicsLayer {
            alpha = avance
            translationY = -34f * (1f - avance)
        }
        NewGradeMotion.LATERAL -> this.graphicsLayer { translationX = 160f * (1f - avance) }
        // Un destello que se apaga: la fila ya esta puesta y lo que pasa es que se enciende.
        NewGradeMotion.DESTELLO -> this.drawWithContent {
            drawContent()
            drawRect(color = Color.White.copy(alpha = 0.35f * (1f - avance)))
        }
        // El hueco se abre primero y la fila llega despues a ocuparlo.
        NewGradeMotion.ABRE -> this.graphicsLayer {
            alpha = ((avance - 0.45f) / 0.55f).coerceIn(0f, 1f)
            scaleY = avance
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
        }
        // Aqui lo que se mueve no es la fila: es el promedio, y de eso se encarga
        // `numeroQueCuenta` en la cabecera.
        NewGradeMotion.CONTAR -> this.alpha(avance)
        NewGradeMotion.NINGUNA -> this
    }
}

// ------------------------------------------------------------------ nota que sube

/**
 * El promedio cuando mejora.
 *
 * Solo cuando **sube**: bajar no es un logro y marcarlo con un salto seria celebrarlo. Se
 * compara con el valor anterior de la propia composicion, asi que basta con pasarle la cifra.
 */
@Composable
fun Modifier.promedioQueSube(promedio: Double?): Modifier {
    val estilo = motionActual().gradeUp
    if (promedio == null || estilo == GradeUpMotion.NINGUNA || !hayMovimiento()) return this

    var anterior by remember { mutableStateOf(promedio) }
    var subio by remember { mutableStateOf(false) }
    LaunchedEffect(promedio) {
        // Solo si hay un valor previo distinto: al abrir la pantalla no hay «antes» con el que
        // comparar, y animar ahi seria inventarse una mejora.
        if (promedio > anterior) {
            subio = true
            kotlinx.coroutines.delay(900)
            subio = false
        }
        anterior = promedio
    }

    val avance by animateFloatAsState(
        targetValue = if (subio) 1f else 0f,
        animationSpec = tweenDeMovimiento(baseMs = 420),
        label = "sube"
    )
    val pico = abs(sin(avance * PI.toFloat()))
    if (pico <= 0.01f) return this

    return when (estilo) {
        GradeUpMotion.SALTO -> this.graphicsLayer { translationY = -14f * pico }
        GradeUpMotion.FLECHA -> this.drawWithContent {
            drawContent()
            // Una flecha que sube y se apaga, a la derecha de la cifra.
            val x = size.width + 10f
            val y = size.height / 2f - 16f * pico
            drawPath(
                androidx.compose.ui.graphics.Path().apply {
                    moveTo(x, y - 7f)
                    lineTo(x - 6f, y + 4f)
                    lineTo(x + 6f, y + 4f)
                    close()
                },
                Color(0xFF11C045).copy(alpha = pico)
            )
        }
        GradeUpMotion.BRILLO -> this.drawBehind {
            drawCircle(
                color = Color(0xFF11C045).copy(alpha = 0.30f * pico),
                radius = size.maxDimension * (0.5f + 0.4f * pico)
            )
        }
        GradeUpMotion.NINGUNA -> this
    }
}

// ------------------------------------------------------------------ deshacer un borrado

/**
 * Cómo vuelve a su sitio una fila que se acaba de recuperar.
 *
 * `restaurada` es un disparo y no un estado: la fila **está** en la lista desde el momento en
 * que se deshace el borrado, y lo que hay que animar es su llegada. Por eso quien lo use pasa
 * el identificador de lo último restaurado y lo limpia al terminar.
 */
@Composable
fun Modifier.filaRestaurada(restaurada: Boolean): Modifier {
    val estilo = motionActual().undo
    if (!restaurada || !hayMovimiento()) return this

    var dentro by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { dentro = true }
    val avance by animateFloatAsState(
        targetValue = if (dentro) 1f else 0f,
        animationSpec = if (estilo == UndoMotion.REBOTA) {
            muelleDeMovimiento()
        } else {
            tweenDeMovimiento(baseMs = 460)
        },
        label = "restaurada"
    )

    return when (estilo) {
        UndoMotion.APARECE -> this.alpha(avance)
        UndoMotion.VUELVE, UndoMotion.REBOTA -> this.graphicsLayer { translationX = 200f * (1f - avance) }
        UndoMotion.CAE -> this.graphicsLayer {
            alpha = avance
            translationY = -50f * (1f - avance)
        }
        // Se despliega: no llega de ningun sitio, crece de alto desde una linea.
        UndoMotion.DESPLIEGA -> this.graphicsLayer {
            clip = true
            scaleY = avance
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
        }
        UndoMotion.GIRA -> this.graphicsLayer {
            alpha = avance
            rotationZ = -80f * (1f - avance)
            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
        }
        UndoMotion.DESTELLO -> this
            .graphicsLayer { translationX = 200f * (1f - avance) }
            .drawWithContent {
                drawContent()
                if (avance > 0.8f) {
                    drawRect(color = Color.White.copy(alpha = 0.4f * (1f - (avance - 0.8f) / 0.2f)))
                }
            }
    }
}

/**
 * Que fila acaba de volver de la papelera.
 *
 * Es un `CompositionLocal` y no un parametro a proposito: entre la pantalla que deshace el
 * borrado y la tarjeta que tiene que animarse hay cuatro capas —dos disposiciones distintas y
 * un envoltorio por cada una—, y ninguna de ellas tiene nada que ver con deshacer. Pasarlo a
 * mano obligaria a que las cuatro conocieran un asunto que no es suyo.
 *
 * Es estado ambiental y momentaneo: exactamente para lo que sirve un local.
 */
val LocalFilaRestaurada = androidx.compose.runtime.compositionLocalOf<String?> { null }
