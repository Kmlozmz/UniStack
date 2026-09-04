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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.unistack.app.feature_user.domain.OverBudgetMotion
import com.unistack.app.feature_user.domain.PinMotion
import com.unistack.app.feature_user.domain.RecoveryMotion
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

    return when (estilo) {
        AttendanceMotion.REBOTE -> this.scale(avance.coerceAtLeast(0.01f))
        // El relleno sube por dentro, como un vaso que se llena.
        AttendanceMotion.RELLENO -> this.drawBehind {
            drawRect(
                color = color,
                topLeft = Offset(0f, size.height * (1f - avance)),
                size = Size(size.width, size.height * avance)
            )
        }
        // Una franja cruza de izquierda a derecha por detrás del contenido.
        AttendanceMotion.BARRIDO -> this.drawBehind {
            drawRoundRect(
                color = color.copy(alpha = 0.22f),
                size = Size(size.width * avance, size.height),
                cornerRadius = CornerRadius(size.height * 0.3f)
            )
        }
        else -> this
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
        ClassNowMotion.RESPIRA -> this.drawBehind {
            drawRoundRect(
                color = verde.copy(alpha = 0.08f + 0.16f * (0.5f + 0.5f * sin(t * 2f * PI.toFloat()))),
                cornerRadius = CornerRadius(size.height * 0.2f)
            )
        }
        ClassNowMotion.PUNTO -> this.drawWithContent {
            drawContent()
            val late = 0.5f + 0.5f * sin(t * 4f * PI.toFloat())
            val centro = Offset(size.width - 14f, size.height / 2f)
            drawCircle(verde.copy(alpha = 0.35f), radius = 6f + 6f * late, center = centro)
            drawCircle(verde, radius = 5f, center = centro)
        }
        // Un punto de luz recorre el perímetro, esquinas incluidas.
        ClassNowMotion.RECORRE -> this.drawWithContent {
            drawContent()
            val perimetro = 2f * (size.width + size.height)
            val d = (t * perimetro) % perimetro
            val punto = when {
                d < size.width -> Offset(d, 0f)
                d < size.width + size.height -> Offset(size.width, d - size.width)
                d < 2f * size.width + size.height -> Offset(size.width - (d - size.width - size.height), size.height)
                else -> Offset(0f, size.height - (d - 2f * size.width - size.height))
            }
            drawCircle(verde, radius = 5f, center = punto)
        }
        ClassNowMotion.BARRE -> this.drawWithContent {
            drawContent()
            val x = -size.width * 0.2f + size.width * 1.4f * t
            rotate(degrees = 18f, pivot = Offset(x, size.height / 2f)) {
                drawRect(
                    color = verde.copy(alpha = 0.20f),
                    topLeft = Offset(x - 24f, -size.height),
                    size = Size(48f, size.height * 3f)
                )
            }
        }
        ClassNowMotion.QUIETA -> this
    }
}
