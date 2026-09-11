@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import com.unistack.app.feature_user.domain.CutSealMotion
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.unistack.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.utils.performSafely
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.duracion
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.core.design.theme.muelleDeMovimiento
import com.unistack.app.core.design.theme.tweenDeMovimiento
import com.unistack.app.feature_user.domain.AutosaveMotion
import com.unistack.app.feature_user.domain.CelebrationMotion
import com.unistack.app.feature_user.domain.MotionSpeed
import com.unistack.app.feature_user.domain.ClassNowMotion
import com.unistack.app.feature_user.domain.FabScrollMotion
import com.unistack.app.feature_user.domain.NewGradeMotion
import com.unistack.app.feature_user.domain.PinMotion
import com.unistack.app.feature_user.domain.TermCloseMotion
import com.unistack.app.feature_user.domain.UndoMotion
import kotlinx.coroutines.delay
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
fun Modifier.selloDeCorte(
    disparado: Boolean,
    onTerminado: () -> Unit,
    /** Si el corte **esta** cerrado, no si acaba de cerrarse: eso decide la marca de agua. */
    cerrado: Boolean = false
): Modifier {
    val estilo = motionActual().cutSeal
    if (estilo == CutSealMotion.NINGUNA || !hayMovimiento()) {
        LaunchedEffect(disparado) { if (disparado) onTerminado() }
        return this
    }

    /*
     * **Sobre la tarjeta, no sobre la pantalla.**
     *
     * Estuvo un rato como capa a pantalla completa con el fondo atenuado, y ahi el sello
     * salia y se iba sin dejar nada: un destello sobre un velo negro. Dentro de la tarjeta
     * aterriza sobre lo que sella y se queda de marca de agua mientras el corte siga
     * cerrado, que es lo que hace que el corte **se vea** cerrado al volver a entrar.
     *
     * **Y con golpe.** La version anterior era un tween de 2200 ms con la curva de siempre:
     * el sello bajaba flotando, sin peso, y se iba apagando. Un sello no flota: cae rapido,
     * **pega**, y la tarjeta lo acusa. Por eso el reloj es lineal y cada variante reparte
     * sus fases a mano —caida, golpe, onda, asentado—, y por eso la tarjeta entera baja tres
     * puntos en el golpe y la vibracion va con el, no con el aviso de que algo va a pasar.
     */
    val haptica = LocalHapticFeedback.current
    // Cuando pega cada una, en fraccion del reloj: la estampa nada mas caer, la tinta al tocar
    // la gota, la cinta cuando queda tirante.
    val golpeEn = when (estilo) {
        CutSealMotion.ESTAMPA -> 0.16f
        CutSealMotion.TINTA -> 0.12f
        CutSealMotion.CINTA -> 0.45f
        CutSealMotion.NINGUNA -> 0f
    }
    val total = duracion(2200)
    LaunchedEffect(disparado) {
        if (!disparado) return@LaunchedEffect
        delay((total * golpeEn).toLong())
        haptica.performSafely(HapticFeedbackType.LongPress)
    }

    val avance by animateFloatAsState(
        targetValue = if (disparado) 1f else 0f,
        // Lineal a proposito: las curvas van dentro de cada fase. Y la vuelta es un salto,
        // que animarla al reves repetiria la entrada hacia atras.
        animationSpec = if (disparado) tween(total.coerceAtLeast(1), easing = LinearEasing) else snap(),
        label = "sello",
        finishedListener = { if (it >= 1f) onTerminado() }
    )
    if (avance <= 0f && !cerrado) return this

    val medidor = rememberTextMeasurer()
    val leyenda = stringResource(R.string.subject_cut_seal)
    val encima = MaterialTheme.colorScheme.surface
    // Parado, el sello esta terminado: `t` vale 1 y cada figura se dibuja como queda al
    // final, no como empieza.
    val t = if (avance > 0f) avance else 1f
    // El golpe, como impulso: sube y baja alrededor del instante en que pega.
    val golpe = if (avance > 0f) impulso(t, golpeEn, 0.14f) else 0f

    return this
        .graphicsLayer {
            // La tarjeta acusa el golpe: baja y se encoge un pelo, y vuelve.
            translationY = 3.dp.toPx() * golpe
            val encogida = 1f - 0.012f * golpe
            scaleX = encogida
            scaleY = encogida
        }
        .drawWithContent {
        drawContent()
        val centro = Offset(size.width / 2f, size.height / 2f)
        /*
         * **La unidad no puede ser el lado corto.**
         *
         * Era `size.minDimension`, y en una tarjeta ancha y baja el lado corto es el **ancho**:
         * multiplicarlo por 1,8 y luego por una escala de 2,2 daba un sello casi cuatro veces
         * mas ancho que lo que venia a sellar. Ahora cada figura se mide contra la tarjeta y
         * se le pone tope con la otra dimension, asi que cabe sea cual sea la forma del sitio
         * donde se estampe.
         */
        val corto = size.minDimension
        // Dos tercios del ancho, con tope por si la tarjeta es mas baja que ancha.
        val ancho = minOf(size.width * 0.66f, size.height * 2f)
        val alto = ancho * 0.34f
        /*
         * Aterriza entero y se asienta en marca de agua: baja hasta un 22 % y ahi se queda
         * mientras el corte siga cerrado, que es lo suficiente para leerse a traves de el y
         * no tan poco como para no verlo.
         */
        val asentado = EaseInOutCubic.transform(((t - 0.62f) / 0.38f).coerceIn(0f, 1f))
        val vida = 1f - 0.78f * asentado
        val verde = Color(0xFF11C045)

        fun palabra(tono: Color, tamano: Float, espacio: Float): TextLayoutResult = medidor.measure(
            leyenda,
            TextStyle(
                color = tono,
                fontSize = tamano.toSp(),
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = espacio.toSp()
            )
        )

        fun DrawScope.centrada(medida: TextLayoutResult) = drawText(
            textLayoutResult = medida,
            topLeft = Offset(centro.x - medida.size.width / 2f, centro.y - medida.size.height / 2f)
        )

        /*
         * Y se recorta a la tarjeta.
         *
         * `drawWithContent` no recorta nada: lo que se salga de la tarjeta se pinta sobre la
         * pantalla, sobre la barra de abajo y sobre lo que haya. Un sello que se sale deja de
         * ser un sello, asi que aunque la figura sobre por un lado —la cinta lo hace a
         * proposito, como el precinto de una caja— lo que se ve queda dentro.
         */
        clipRect {
            when (estilo) {
                /*
                 * **Estampado: cae, pega, y la onda se va.**
                 *
                 * Cae acelerando —como algo que pesa— de casi el doble a su tamano en el
                 * primer 16 %, girando de -22 a -14 grados para asentarse. Al pegar, el
                 * hueco del sello se ilumina un instante y una onda con su misma forma se
                 * expande y se apaga: es lo que dice que ha golpeado algo. El marco es
                 * doble, grueso fuera y fino dentro, como un sello de goma de verdad.
                 */
                CutSealMotion.ESTAMPA -> {
                    val caida = (t / 0.16f).coerceAtMost(1f)
                    val cae = EaseInCubic.transform(caida)
                    val escala = 1.9f - 0.9f * cae
                    val giro = -22f + 8f * cae
                    val alfa = (caida / 0.35f).coerceAtMost(1f) * vida
                    val onda = ((t - 0.16f) / 0.30f).coerceIn(0f, 1f)

                    if (onda > 0f && onda < 1f) {
                        val e = EaseOutCubic.transform(onda)
                        rotate(degrees = -14f, pivot = centro) {
                            // El destello del hueco.
                            drawRoundRect(
                                color = verde.copy(alpha = 0.28f * (1f - e)),
                                topLeft = Offset(centro.x - ancho / 2f, centro.y - alto / 2f),
                                size = Size(ancho, alto),
                                cornerRadius = CornerRadius(alto * 0.28f, alto * 0.28f)
                            )
                            // La onda que se expande.
                            val a = ancho * (1f + 0.42f * e)
                            val h = alto * (1f + 0.42f * e)
                            drawRoundRect(
                                color = verde.copy(alpha = 0.55f * (1f - e)),
                                topLeft = Offset(centro.x - a / 2f, centro.y - h / 2f),
                                size = Size(a, h),
                                cornerRadius = CornerRadius(h * 0.28f, h * 0.28f),
                                style = Stroke(1f + h * 0.05f * (1f - e))
                            )
                        }
                    }
                    rotate(degrees = giro, pivot = centro) {
                        val a = ancho * escala
                        val h = alto * escala
                        val esquina = Offset(centro.x - a / 2f, centro.y - h / 2f)
                        drawRoundRect(
                            color = verde.copy(alpha = alfa),
                            topLeft = esquina,
                            size = Size(a, h),
                            cornerRadius = CornerRadius(h * 0.28f, h * 0.28f),
                            style = Stroke(h * 0.10f)
                        )
                        val margen = h * 0.17f
                        drawRoundRect(
                            color = verde.copy(alpha = alfa * 0.8f),
                            topLeft = esquina + Offset(margen, margen),
                            size = Size(a - 2f * margen, h - 2f * margen),
                            cornerRadius = CornerRadius(h * 0.14f, h * 0.14f),
                            style = Stroke(h * 0.03f)
                        )
                        centrada(palabra(verde.copy(alpha = alfa), h * 0.36f, h * 0.08f))
                    }
                }

                /*
                 * **Tinta: una gota que se corre y de la que sube la palabra.**
                 *
                 * Cae una gota oscura al centro, la tinta se extiende desde ahi en una
                 * mancha de bordes suaves —cinco circulos fijos, que un solo circulo es una
                 * moneda y no una mancha— y la palabra emerge del charco como bloque
                 * relleno, que es lo que hace la tinta de verdad al empaparse.
                 */
                CutSealMotion.TINTA -> {
                    val gota = EaseOutCubic.transform((t / 0.12f).coerceAtMost(1f))
                    val mancha = EaseOutExpo.transform(((t - 0.10f) / 0.45f).coerceIn(0f, 1f))
                    val sube = EaseOutCubic.transform(((t - 0.22f) / 0.38f).coerceIn(0f, 1f))

                    manchaDeTinta.forEach { (desplaza, radio) ->
                        val c = centro + Offset(desplaza.x * corto, desplaza.y * corto)
                        val rr = radio * corto * (0.12f + 0.88f * mancha)
                        if (rr > 0f) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    0f to verde.copy(alpha = 0.20f * vida),
                                    0.7f to verde.copy(alpha = 0.12f * vida),
                                    1f to verde.copy(alpha = 0f),
                                    center = c,
                                    radius = rr
                                ),
                                radius = rr,
                                center = c
                            )
                        }
                    }
                    // La gota: oscura al caer, y se difumina segun la tinta se corre.
                    drawCircle(
                        color = verde.copy(alpha = 0.9f * gota * (1f - mancha)),
                        radius = corto * 0.06f * gota,
                        center = centro
                    )
                    if (sube > 0f) {
                        rotate(degrees = -8f, pivot = centro) {
                            scale(scale = 0.9f + 0.1f * sube, pivot = centro) {
                                drawRoundRect(
                                    color = verde.copy(alpha = sube * vida),
                                    topLeft = Offset(centro.x - ancho / 2f, centro.y - alto / 2f),
                                    size = Size(ancho, alto),
                                    cornerRadius = CornerRadius(alto * 0.28f, alto * 0.28f)
                                )
                                centrada(palabra(encima.copy(alpha = sube * (0.45f + 0.55f * vida)), alto * 0.40f, alto * 0.07f))
                            }
                        }
                    }
                }

                /*
                 * **Cinta: se desenrolla, queda tirante y destapa la palabra.**
                 *
                 * Cruza la tarjeta de izquierda a derecha con un poco de rebote al final
                 * —el tiron de dejarla tirante—, con las puntas cortadas en diagonal y una
                 * veta de luz a lo largo: eso es lo que la hace cinta y no una franja. La
                 * palabra no aparece: se **destapa**, solo lo que la cinta ya cubre.
                 */
                CutSealMotion.CINTA -> {
                    val tira = EaseOutBack.transform((t / 0.45f).coerceAtMost(1f))
                    val grosor = corto * 0.22f
                    val largo = size.width * 1.3f * tira
                    rotate(degrees = -10f, pivot = centro) {
                        val x0 = -size.width * 0.15f
                        val y0 = centro.y - grosor / 2f
                        val corte = grosor * 0.35f
                        val tirita = Path().apply {
                            moveTo(x0, y0)
                            lineTo(x0 + largo, y0)
                            lineTo(x0 + largo - corte, y0 + grosor)
                            lineTo(x0 + corte, y0 + grosor)
                            close()
                        }
                        drawPath(tirita, color = verde.copy(alpha = 0.88f * vida))
                        if (largo > corte) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.22f * vida),
                                start = Offset(x0 + corte * 0.6f, y0 + grosor * 0.27f),
                                end = Offset(x0 + largo - corte * 0.6f, y0 + grosor * 0.27f),
                                strokeWidth = grosor * 0.06f,
                                cap = StrokeCap.Round
                            )
                        }
                        clipRect(right = x0 + largo - corte) {
                            centrada(palabra(Color.White.copy(alpha = vida), grosor * 0.42f, grosor * 0.07f))
                        }
                    }
                }
                CutSealMotion.NINGUNA -> Unit
            }
        }
    }
}

/**
 * Un impulso alrededor de [centro]: sube de 0 a 1 y vuelve a 0 en [ancho] de reloj.
 *
 * Es la forma del golpe: la tarjeta que baja y vuelve, el punto de vibracion. Fuera del tramo
 * vale cero, asi que quien lo use no tiene que preguntar si ya paso.
 */
private fun impulso(t: Float, centro: Float, ancho: Float): Float {
    val p = (t - centro) / ancho
    if (p <= 0f || p >= 1f) return 0f
    return sin(p * PI.toFloat())
}

/**
 * La mancha de tinta: cinco circulos con desplazamientos y radios fijos, en fraccion del lado
 * corto. Fijos a proposito: sorteados, la mancha cambiaria de forma cada vez que se sella.
 */
private val manchaDeTinta = listOf(
    Offset(0f, 0f) to 0.52f,
    Offset(-0.26f, -0.11f) to 0.30f,
    Offset(0.29f, 0.07f) to 0.34f,
    Offset(-0.09f, 0.27f) to 0.28f,
    Offset(0.17f, -0.27f) to 0.26f
)

// ------------------------------------------------------------------ celebrar al terminar

/**
 * La celebración de haber cerrado la última pendiente del día, o un corte.
 *
 * Se pinta **encima** de lo que envuelva, sin ocupar sitio, para que la lista no dé un salto al
 * aparecer. Dura lo suyo y se apaga sola: [disparada] vuelve a falso cuando termina.
 *
 * **Tiene que notarse.** La primera version eran dieciseis rectangulos de diez **pixeles**
 * volando 1100 ms desde el centro: en una pantalla de 3x eso son tres puntos, y a esa
 * velocidad ni se veian. Un confeti asi no transmite nada. Ahora todo se mide en dp, hay cien
 * particulas con peso —suben, giran y caen—, dura 2200 ms y ocupa la pantalla entera; y las
 * otras tres variantes crecen hasta las esquinas en vez de quedarse en un circulo del centro.
 *
 * [mensaje] es lo que se celebra, en palabras —«¡Todo hecho!», «¡Corte cerrado!»—, y lo
 * llevan las cuatro: una figura sola, sea confeti, rayos o un aro, no dice que se celebra.
 */
@Composable
fun Modifier.celebracionDelDia(
    disparada: Boolean,
    onTerminada: () -> Unit,
    mensaje: String? = null
): Modifier {
    val estilo = motionActual().celebration
    if (estilo == CelebrationMotion.NINGUNA || !hayMovimiento()) {
        // Sin celebración, el aviso se cierra al momento: si no, quien la apagó se queda con
        // un estado encendido para siempre.
        LaunchedEffect(disparada) { if (disparada) onTerminada() }
        return this
    }

    val haptica = LocalHapticFeedback.current
    LaunchedEffect(disparada) {
        if (disparada) haptica.performSafely(HapticFeedbackType.LongPress)
    }

    val avance by animateFloatAsState(
        targetValue = if (disparada) 1f else 0f,
        /*
         * **La vuelta es un salto.** Al terminar, `onTerminada` apaga `disparada` y el
         * objetivo vuelve a cero: con el mismo tween, el confeti volaba **hacia atras** y
         * las particulas volvian a recogerse en el centro, como un video en reversa. Lo
         * que se apaga tiene que apagarse, no rebobinarse.
         */
        animationSpec = if (disparada) tween(duracion(2200).coerceAtLeast(1), easing = LinearEasing) else snap(),
        label = "celebracion",
        finishedListener = { if (it >= 1f) onTerminada() }
    )
    if (avance <= 0f) return this

    // Las partículas se sortean una sola vez: con un sorteo por fotograma, el confeti tiembla
    // en vez de volar.
    val confeti = remember { List(100) { indice -> Particula.sortear(indice) } }
    val chispas = remember { List(34) { indice -> Chispa.sortear(indice) } }
    val acento = MaterialTheme.colorScheme.primary
    val verde = Color(0xFF11C045)
    val ambar = Color(0xFFE0A400)
    val fondo = MaterialTheme.colorScheme.surface
    val medidor = rememberTextMeasurer()
    val tipoDelMensaje = TextStyle(
        color = acento,
        fontSize = 34.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.02).em
    )

    return this.drawWithContent {
        drawContent()
        val t = avance
        val w = size.width
        val h = size.height
        val centro = Offset(w / 2f, h / 2f)
        val alcance = kotlin.math.hypot(w, h) / 2f
        when (estilo) {
            /*
             * **Dos cañones en las esquinas de abajo.** Cada particula sale con su angulo, su
             * velocidad y su retraso, sube frenando, cae y gira; el `cos` sobre el ancho la hace
             * voltear como un papel. Se apagan en el ultimo tercio, cuando ya van cayendo.
             */
            CelebrationMotion.CONFETI -> {
                // El fogonazo de salida, en los dos cañones.
                val fogonazo = (1f - t / 0.18f).coerceIn(0f, 1f)
                if (fogonazo > 0f) {
                    listOf(Offset(w * 0.08f, h), Offset(w * 0.92f, h)).forEach { boca ->
                        drawCircle(
                            brush = Brush.radialGradient(
                                0f to Color.White.copy(alpha = 0.35f * fogonazo),
                                1f to Color.White.copy(alpha = 0f),
                                center = boca,
                                radius = 140.dp.toPx()
                            ),
                            radius = 140.dp.toPx(),
                            center = boca
                        )
                    }
                }
                confeti.forEach { p ->
                    val tau = ((t - p.retraso) / (1f - p.retraso)).coerceIn(0f, 1f)
                    if (tau <= 0f || tau >= 1f) return@forEach
                    val boca = if (p.izquierda) Offset(w * 0.08f, h + 8.dp.toPx()) else Offset(w * 0.92f, h + 8.dp.toPx())
                    // Sube y cae: la altura alcanza su maximo hacia los dos tercios del vuelo.
                    val subida = h * p.empuje * (2.2f * tau - 1.7f * tau * tau)
                    val deriva = w * p.lateral * tau + sin(tau * 9f + p.fase) * 10.dp.toPx()
                    val punto = Offset(boca.x + deriva, boca.y - subida)
                    val alfa = if (tau < 0.66f) 1f else 1f - (tau - 0.66f) / 0.34f
                    val ancho = p.tamano.dp.toPx()
                    val alto = ancho * 0.55f
                    rotate(degrees = p.giro + p.vueltas * 360f * tau, pivot = punto) {
                        // El volteo: el ancho se estrecha y se ensancha como un papel girando.
                        val volteo = abs(cos(tau * p.volteo + p.fase))
                        val a = ancho * (0.25f + 0.75f * volteo)
                        if (p.redonda) {
                            drawOval(
                                color = p.color.copy(alpha = alfa),
                                topLeft = Offset(punto.x - a / 2f, punto.y - alto / 2f),
                                size = Size(a, alto)
                            )
                        } else {
                            drawRect(
                                color = p.color.copy(alpha = alfa),
                                topLeft = Offset(punto.x - a / 2f, punto.y - alto / 2f),
                                size = Size(a, alto)
                            )
                        }
                    }
                }
            }

            // Cuatro ondas del centro a las esquinas, con un resplandor que llena la pantalla
            // al arrancar y se va apagando.
            CelebrationMotion.ONDA -> {
                val resplandor = (1f - t / 0.5f).coerceIn(0f, 1f)
                if (resplandor > 0f) {
                    drawRect(
                        brush = Brush.radialGradient(
                            0f to acento.copy(alpha = 0.30f * resplandor),
                            1f to acento.copy(alpha = 0f),
                            center = centro,
                            radius = alcance
                        )
                    )
                }
                repeat(4) { indice ->
                    val p = ((t - indice * 0.13f) / 0.62f).coerceIn(0f, 1f)
                    if (p > 0f && p < 1f) {
                        val e = EaseOutCubic.transform(p)
                        drawCircle(
                            color = acento.copy(alpha = 0.7f * (1f - e)),
                            radius = 30.dp.toPx() + (alcance - 30.dp.toPx()) * e,
                            center = centro,
                            style = Stroke(width = 12.dp.toPx() * (1f - 0.7f * e) + 2.dp.toPx())
                        )
                    }
                }
            }

            /*
             * Un sello grande en el centro: el aro cae de fuera hacia dentro, pega —con su
             * onda—, y dentro se dibuja el visto de un trazo. Se queda un momento y se va.
             */
            CelebrationMotion.SELLO -> {
                val lado = minOf(w, h)
                val caida = EaseInCubic.transform((t / 0.28f).coerceAtMost(1f))
                val radio = lado * (0.62f - 0.34f * caida)
                val onda = ((t - 0.28f) / 0.32f).coerceIn(0f, 1f)
                val trazo = EaseOutCubic.transform(((t - 0.30f) / 0.25f).coerceIn(0f, 1f))
                val vida = 1f - EaseInOutCubic.transform(((t - 0.72f) / 0.28f).coerceIn(0f, 1f))
                val alfa = (caida / 0.4f).coerceAtMost(1f) * vida
                if (onda > 0f && onda < 1f) {
                    val e = EaseOutCubic.transform(onda)
                    drawCircle(
                        color = verde.copy(alpha = 0.55f * (1f - e)),
                        radius = lado * 0.28f + alcance * 0.9f * e,
                        center = centro,
                        style = Stroke(width = 10.dp.toPx() * (1f - e) + 1f)
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to verde.copy(alpha = 0.28f * (1f - e)),
                            1f to verde.copy(alpha = 0f),
                            center = centro,
                            radius = lado * 0.5f
                        ),
                        radius = lado * 0.5f,
                        center = centro
                    )
                }
                drawCircle(
                    color = verde.copy(alpha = alfa),
                    radius = radio,
                    center = centro,
                    style = Stroke(width = 16.dp.toPx())
                )
                if (trazo > 0f) {
                    // El visto, de un trazo: primero el palo corto, luego el largo.
                    val a = centro + Offset(-radio * 0.42f, radio * 0.02f)
                    val b = centro + Offset(-radio * 0.12f, radio * 0.32f)
                    val c = centro + Offset(radio * 0.46f, -radio * 0.30f)
                    val primero = (trazo / 0.4f).coerceAtMost(1f)
                    val segundo = ((trazo - 0.4f) / 0.6f).coerceIn(0f, 1f)
                    drawLine(verde.copy(alpha = alfa), a, a + (b - a) * primero, 16.dp.toPx(), StrokeCap.Round)
                    if (segundo > 0f) {
                        drawLine(verde.copy(alpha = alfa), b, b + (c - b) * segundo, 16.dp.toPx(), StrokeCap.Round)
                    }
                }
            }

            /*
             * Rayos del centro a las esquinas girando despacio, y chispas de cuatro puntas que
             * saltan por toda la pantalla, cada una en su momento.
             */
            CelebrationMotion.DESTELLO -> {
                val rayos = EaseOutCubic.transform((t / 0.6f).coerceAtMost(1f))
                val vida = 1f - EaseInOutCubic.transform(((t - 0.55f) / 0.45f).coerceIn(0f, 1f))
                rotate(degrees = t * 24f, pivot = centro) {
                    repeat(12) { indice ->
                        val angulo = indice / 12f * 2f * PI.toFloat()
                        val dentro = 24.dp.toPx() + alcance * 0.35f * rayos
                        val fuera = dentro + alcance * 0.75f * rayos
                        drawLine(
                            color = ambar.copy(alpha = 0.55f * vida),
                            start = centro + Offset(cos(angulo) * dentro, sin(angulo) * dentro),
                            end = centro + Offset(cos(angulo) * fuera, sin(angulo) * fuera),
                            strokeWidth = 5.dp.toPx() * (1f - 0.6f * rayos) + 1f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                chispas.forEach { ch ->
                    val p = ((t - ch.retraso) / 0.38f).coerceIn(0f, 1f)
                    if (p <= 0f || p >= 1f) return@forEach
                    val vivo = sin(p * PI.toFloat())
                    val r = ch.tamano.dp.toPx() * vivo
                    val punto = Offset(w * ch.x, h * ch.y)
                    rotate(degrees = ch.giro + p * 90f, pivot = punto) {
                        val estrella = Path().apply {
                            moveTo(punto.x, punto.y - r)
                            quadraticTo(punto.x, punto.y, punto.x + r, punto.y)
                            quadraticTo(punto.x, punto.y, punto.x, punto.y + r)
                            quadraticTo(punto.x, punto.y, punto.x - r, punto.y)
                            quadraticTo(punto.x, punto.y, punto.x, punto.y - r)
                            close()
                        }
                        drawPath(estrella, color = ch.color.copy(alpha = vivo))
                    }
                }
            }
            CelebrationMotion.NINGUNA -> Unit
        }

        /*
         * **El mensaje, en las cuatro.** Entra con un pequeno rebote, se queda mientras dura
         * la figura y se va con ella. Cada variante lo pone donde no estorba a lo suyo: en el
         * centro, o debajo del aro del sello, que ya lleva el visto dentro; y del color que
         * lleva la figura. Sin palabras, un confeti o unos rayos no decian que se celebraba.
         */
        if (mensaje != null && estilo != CelebrationMotion.NINGUNA) {
            val (desde, color, y) = when (estilo) {
                CelebrationMotion.SELLO -> Triple(0.30f, verde, centro.y + minOf(w, h) * 0.28f + 46.dp.toPx())
                CelebrationMotion.DESTELLO -> Triple(0f, ambar, centro.y)
                else -> Triple(0f, acento, centro.y)
            }
            val entra = EaseOutBack.transform(((t - desde) / 0.22f).coerceIn(0f, 1f))
            val vida = 1f - EaseInOutCubic.transform(((t - 0.72f) / 0.28f).coerceIn(0f, 1f))
            if (entra > 0f && vida > 0f) {
                val medida = medidor.measure(
                    mensaje,
                    tipoDelMensaje.copy(color = color.copy(alpha = vida * entra.coerceAtMost(1f)))
                )
                val punto = Offset(centro.x, y)
                scale(scale = 0.8f + 0.2f * entra, pivot = punto) {
                    // Un halo del fondo detras de la letra, para que se lea sobre el confeti
                    // y los rayos sin taparlos.
                    drawRoundRect(
                        color = fondo.copy(alpha = 0.55f * vida * entra.coerceAtMost(1f)),
                        topLeft = Offset(punto.x - medida.size.width / 2f - 18.dp.toPx(), punto.y - medida.size.height / 2f - 8.dp.toPx()),
                        size = Size(medida.size.width + 36.dp.toPx(), medida.size.height + 16.dp.toPx()),
                        cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx())
                    )
                    drawText(
                        textLayoutResult = medida,
                        topLeft = Offset(punto.x - medida.size.width / 2f, punto.y - medida.size.height / 2f)
                    )
                }
            }
        }
    }
}

/** Un papelito del confeti, con todo lo suyo sorteado una vez. */
private class Particula(
    val izquierda: Boolean,
    val empuje: Float,
    val lateral: Float,
    val retraso: Float,
    val tamano: Float,
    val giro: Float,
    val vueltas: Float,
    val volteo: Float,
    val fase: Float,
    val redonda: Boolean,
    val color: Color
) {
    companion object {
        fun sortear(indice: Int): Particula {
            val azar = Random(indice * 7919 + 13)
            val izquierda = indice % 2 == 0
            return Particula(
                izquierda = izquierda,
                empuje = 0.45f + azar.nextFloat() * 0.5f,
                // Hacia el lado contrario del cañon, abriendo en abanico.
                lateral = (0.25f + azar.nextFloat() * 0.75f) * (if (izquierda) 1f else -1f),
                retraso = azar.nextFloat() * 0.16f,
                tamano = 7f + azar.nextFloat() * 7f,
                giro = azar.nextFloat() * 360f,
                vueltas = 1f + azar.nextFloat() * 2.5f,
                volteo = 6f + azar.nextFloat() * 8f,
                fase = azar.nextFloat() * 6.28f,
                redonda = azar.nextFloat() < 0.3f,
                color = paletaDeConfeti[indice % paletaDeConfeti.size]
            )
        }
    }
}

/** Una chispa del destello: donde salta, cuando, y como de grande. */
private class Chispa(
    val x: Float,
    val y: Float,
    val retraso: Float,
    val tamano: Float,
    val giro: Float,
    val color: Color
) {
    companion object {
        fun sortear(indice: Int): Chispa {
            val azar = Random(indice * 4241 + 7)
            return Chispa(
                x = 0.06f + azar.nextFloat() * 0.88f,
                y = 0.08f + azar.nextFloat() * 0.84f,
                retraso = azar.nextFloat() * 0.6f,
                tamano = 10f + azar.nextFloat() * 16f,
                giro = azar.nextFloat() * 90f,
                color = if (azar.nextFloat() < 0.7f) Color(0xFFFFD34D) else Color.White
            )
        }
    }
}

/** Los colores del confeti. Fijos a propósito: una celebración no cambia con el tema. */
private val paletaDeConfeti = listOf(
    Color(0xFF7F77DD), Color(0xFFE8693A), Color(0xFF5FC96E), Color(0xFFE0A63C), Color(0xFF3FC7B4)
)

// ------------------------------------------------------------------ marcar asistencia

// ------------------------------------------------------------------ materia que se recupera

/**
 * La materia que acaba de salir del rojo, barriendo la barra del hero.
 *
 * **Antes tenia un rotulo de once pixeles en la lista de materias.** Cinco variantes para
 * cambiar el color de una palabra: no habia forma de distinguirlas ni de llegar a verlas, y
 * tres de las cinco compartian la misma linea de codigo.
 *
 * Ahora vive donde el viaje ya estaba dibujado: la barra del hero va de rojo a verde sobre la
 * escala entera, con el punto en donde vas. Recuperarse **es** ese punto cruzando hacia la
 * derecha, y el barrido lo acompana. De cinco variantes queda una, asi que el gesto sale de
 * Movimiento: con una sola forma no hay nada que elegir.
 *
 * @param recuperada si ahora mismo se aprueba. El barrido salta al **cruzar**, no cada vez que
 *   sube una decima: recuperarse es salir del rojo, no mejorar.
 */
@Composable
fun Modifier.barridoDeRecuperacion(recuperada: Boolean): Modifier {
    if (!hayMovimiento()) return this

    /*
     * `visto` arranca en nulo para que entrar a una materia que **ya** estaba aprobada no
     * barra nada: no acaba de pasar. Es el mismo guardian que el sello del corte.
     */
    var visto by remember { mutableStateOf<Boolean?>(null) }
    var corre by remember { mutableStateOf(false) }
    LaunchedEffect(recuperada) {
        if (visto == false && recuperada) corre = true
        visto = recuperada
    }

    val avance by animateFloatAsState(
        targetValue = if (corre) 1f else 0f,
        // La vuelta es un salto: animarla al reves repetiria el barrido de derecha a izquierda.
        animationSpec = if (corre) tweenDeMovimiento(baseMs = 1275) else snap(),
        label = "recuperacion",
        finishedListener = { if (it >= 1f) corre = false }
    )
    if (avance <= 0f || avance >= 1f) return this

    return this.drawWithContent {
        drawContent()
        clipRect(0f, 0f, size.width, size.height) {
            val ancho = size.width * 0.44f
            val x = -ancho + (size.width + ancho * 2f) * avance
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.75f),
                        Color.Transparent
                    ),
                    startX = x,
                    endX = x + ancho
                ),
                topLeft = Offset(x, 0f),
                size = Size(ancho, size.height)
            )
        }
    }
}

// ------------------------------------------------------------------ fijar una nota

/**
 * El aviso de haberse pasado del presupuesto, arriba de la pantalla.
 *
 * **Es la unica forma que queda de avisar, y va siempre.** Hubo cinco variantes —contorno
 * rojo, sacudida, parpadeo, seco y esta— y mirandolas en Gastos las cuatro de la fila se
 * veian iguales: un halo rojo alrededor del presupuesto que no decia nada que esta franja
 * no dijera mejor y antes. Se quitaron con su apartado de Movimiento; lo que hay es esto.
 *
 * Se queda mientras siga pasado: un aviso de dinero que se apaga solo deja de avisar justo
 * cuando mas falta hace. Y al cruzar —no al abrir la pantalla ya pasado— vibra fuerte una
 * vez: es de las pocas cosas de la app que conviene notar sin estar mirando.
 */
@Composable
fun AvisoDePresupuestoArriba(pasado: Boolean, modifier: Modifier = Modifier) {
    val haptica = LocalHapticFeedback.current
    // Lo que habia en la composicion anterior: nulo al entrar, asi que abrir Gastos ya
    // pasado no vibra; solo vibra el momento de cruzar.
    var antes by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(pasado) {
        if (antes == false && pasado) haptica.performSafely(HapticFeedbackType.LongPress)
        antes = pasado
    }
    if (!pasado) return
    val rojo = LocalSectionColors.current.expenses

    AnimatedVisibility(
        visible = true,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = tweenDeMovimiento(baseMs = 225)
        ) { -it } + fadeIn(animationSpec = tweenDeMovimiento(baseMs = 225))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(rojo)
                .padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = stringResource(R.string.expenses_over_budget_banner),
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * La rueda del visto al marcar asistencia.
 *
 * **Nace neutra y el gesto la llena de color**: verde sobre verde no se veia, que fue lo que
 * tuvo el gesto invisible durante meses. Y el gesto es uno solo, el rebote: se miraron las
 * cuatro variantes en «Ponerse al dia» y el rebote era la unica que se sentia como marcar
 * algo. Las otras tres se quitaron con su apartado de Movimiento.
 *
 * **El rebote se pasa de largo por fuera de la rueda.** La escala va antes del recorte de
 * 44 dp, asi que el muelle la lleva hasta un 20 % mas grande y la vuelve: lo que se ve es la
 * rueda saltando. Con el movimiento apagado el color aparece de golpe.
 */
@Composable
fun RuedaDeAsistencia(
    marcada: Boolean,
    color: Color,
    icono: ImageVector,
    modifier: Modifier = Modifier
) {
    val neutro = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
    val encima = MaterialTheme.colorScheme.surface
    val conMovimiento = hayMovimiento() && motionActual().speed != MotionSpeed.INSTANTANEA

    val avance by animateFloatAsState(
        targetValue = if (marcada) 1f else 0f,
        // Muelle propio, no el general: aqui el rebote **es** el gesto, y con el amortiguado
        // de «Suave» no rebotaria nada. Rigido para que sea rapido.
        animationSpec = if (conMovimiento && marcada) {
            spring(dampingRatio = 0.28f, stiffness = 900f / motionActual().speed.factor.coerceAtLeast(0.2f))
        } else {
            snap()
        },
        label = "rueda"
    )
    val p = if (conMovimiento) avance else (if (marcada) 1f else 0f)

    Box(
        modifier = modifier
            .size(44.dp)
            // Arranca desde un 40 % —una rueda que nace de dentro— y el muelle la lleva mas
            // alla del 100 % antes de asentarla.
            .graphicsLayer {
                val escala = if (conMovimiento && marcada) 0.4f + 0.6f * p else 1f
                scaleX = escala
                scaleY = escala
            }
            .clip(CircleShape)
            .background(if (marcada) color else neutro),
        contentAlignment = Alignment.Center
    ) {
        if (marcada && p > 0.15f) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = encima,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * La píldora de «clase en curso», con su luz.
 *
 * **La fila no decía que la clase estaba pasando: lo insinuaba con un borde verde.** Quien no
 * hubiera aprendido que el verde significa eso veía una fila de otro color y ya. La píldora lo
 * dice con palabras, y la luz de al lado es lo que lo hace presente.
 *
 * Aquí vive la variante «Punto que late» de [claseEnCurso]: latir **esta** luz es el gesto. Se
 * dibujaba pegado al borde izquierdo de la fila, donde no significaba nada y además chocaba con
 * la franja de color de la materia.
 */
@Composable
fun PildoraEnCurso(modifier: Modifier = Modifier) {
    val verde = LocalSectionColors.current.onTrack
    val late = motionActual().classNow == ClassNowMotion.PUNTO && hayMovimiento()

    val ciclo = rememberInfiniteTransition(label = "pildora")
    val t by ciclo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracion(1950), easing = LinearEasing)),
        label = "pildora"
    )
    // Crece y se apaga en su sitio: es un latido, no un punto que aparece y desaparece.
    val escala = if (late) 1f + 0.9f * abs(sin(t * PI.toFloat())) else 1f
    val opacidad = if (late) 1f - 0.55f * abs(sin(t * PI.toFloat())) else 1f

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(verde.copy(alpha = 0.16f))
            .padding(horizontal = 9.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                    alpha = opacidad
                }
                .clip(CircleShape)
                .background(verde)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = stringResource(R.string.schedule_class_now),
            color = verde,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.08.em
        )
    }
}

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
 * El pincel del cometa que da la vuelta a un contorno.
 *
 * Es un degradado en abanico con el frente en `t` (fraccion de vuelta, 0 a 1) y una cola de
 * un 40 % de circunferencia que se apaga hacia atras. Se construye por muestras y no con tres
 * paradas porque las paradas de un `sweepGradient` tienen que ir de 0 a 1 en orden: un frente
 * que cruza el cero partiria el degradado en dos y se veria un corte.
 */
fun pincelDeCometa(color: Color, t: Float, centro: Offset, cola: Float = 0.40f): Brush {
    val muestras = 36
    val paradas = Array(muestras + 1) { i ->
        val pos = i / muestras.toFloat()
        // Cuanto queda por detras del frente, en fraccion de vuelta.
        val detras = ((t - pos) % 1f + 1f) % 1f
        val fuerza = if (detras < cola) 1f - detras / cola else 0f
        pos to color.copy(alpha = color.alpha * fuerza * fuerza)
    }
    return Brush.sweepGradient(colorStops = paradas, center = centro)
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

    /*
     * **Cada variante con su compas, elegido mirandolas juntas.**
     *
     * Las cuatro iban a 2400 ms y no les venia bien el mismo: un halo que respira necesita
     * mas aire que una luz que da la vuelta. Van anclados al gesto y no se ajustan.
     */
    val periodo = when (estilo) {
        ClassNowMotion.RESPIRA -> 3600
        ClassNowMotion.PUNTO -> 1950
        ClassNowMotion.RECORRE -> 2340
        ClassNowMotion.BARRE -> 3360
        ClassNowMotion.QUIETA -> 2400
    }
    val ciclo = rememberInfiniteTransition(label = "claseAhora")
    val t by ciclo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(duracion(periodo), easing = LinearEasing), RepeatMode.Restart),
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
        /*
         * **El punto que late es la luz de la pildora, y la pinta ella.**
         *
         * Aqui se dibujaba pegado al borde izquierdo de la fila, donde no significaba nada
         * y competia con la franja de color de la materia. La pildora «CLASE EN CURSO» ya
         * tiene una luz que dice exactamente eso: latir esa es el gesto.
         */
        ClassNowMotion.PUNTO -> this
        /*
         * **Una luz que da la vuelta al borde**, no un punto por dentro.
         *
         * Era un punto de 2,5 dp recorriendo un rectangulo metido 3 dp hacia dentro: se
         * veia pequeno, descentrado del borde verde de la fila y sin relacion con el. Lo
         * que se pidio es el efecto de los bordes que «recorren» —esa luz que da la vuelta a
         * un marco—, y eso se pinta con un degradado en abanico girando sobre el propio
         * contorno de la fila: un frente brillante con una cola que se apaga.
         *
         * El contorno sale de la forma del tema (`shapes.medium`), asi que sigue a las
         * esquinas que el usuario haya elegido y se pinta justo encima del borde verde
         * fijo, que es donde tiene que ir.
         */
        ClassNowMotion.RECORRE -> {
            val forma = MaterialTheme.shapes.medium
            this.clipToBounds().drawWithContent {
                drawContent()
                val grosor = 2.dp.toPx()
                val pincel = pincelDeCometa(verde, t, Offset(size.width / 2f, size.height / 2f))
                // Metido medio trazo: un trazo centrado en el borde pierde la mitad fuera.
                translate(grosor / 2f, grosor / 2f) {
                    val contorno = forma.createOutline(
                        Size(size.width - grosor, size.height - grosor),
                        layoutDirection,
                        this
                    )
                    // El halo ancho y tenue debajo, y el filo encima: es lo que lo hace luz.
                    drawOutline(contorno, pincel, style = Stroke(grosor * 3f), alpha = 0.35f)
                    drawOutline(contorno, pincel, style = Stroke(grosor))
                }
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
