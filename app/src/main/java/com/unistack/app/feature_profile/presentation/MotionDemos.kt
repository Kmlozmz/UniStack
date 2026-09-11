@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniLoading
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.formatCurrency
import com.unistack.app.feature_user.domain.MotionPreferences
import com.unistack.app.feature_user.domain.PressEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

/**
 * Los demos de Movimiento, **hechos de verdad** y no de miniaturas ampliadas.
 *
 * Aquí estaba el fallo que se veía comparando con el diseño: los tres demos de la sección base
 * pintaban el mismo dibujo diminuto de la rejilla de variantes, escalado a noventa píxeles. Y
 * esos dibujos no son demos: el de «Al pulsar» era una pastilla quieta cuando lo que hace falta
 * es **un botón que se pueda pulsar**, y el de «Rebote» era un punto en una línea cuando lo que
 * enseña el rebote son cinco barras subiendo a alturas distintas con el mismo muelle.
 *
 * Cada uno es una pieza real, del tamaño real, con su pie de texto diciendo qué valores tiene
 * puestos ahora mismo. El pie no es adorno: es lo que deja comprobar que la elección llegó,
 * porque dice en números lo que la animación dice en movimiento.
 */

/** El pie de un demo: 9,5 puntos, atenuado, como en el diseño. */
@Composable
private fun PieDeDemo(texto: String, centrado: Boolean = false) {
    Text(
        text = texto,
        fontSize = 9.5.sp,
        color = MaterialTheme.colorScheme.outline,
        textAlign = if (centrado) TextAlign.Center else TextAlign.Start,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 7.dp)
    )
}

/** El rótulo de una de las dos columnas de un demo comparativo. */
@Composable
private fun RotuloDeColumna(texto: String) {
    Text(
        text = texto,
        fontSize = 8.5.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        color = MaterialTheme.colorScheme.outline,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}

/**
 * **Rebote**: cinco barras subiendo a alturas distintas con el muelle elegido.
 *
 * Cinco y no una: el rebote es lo que hace la barra **al llegar** a su altura, y con una sola
 * barra no hay con qué comparar el momento en que se pasa de largo. El retardo escalonado
 * entre ellas es lo que deja verlo cinco veces seguidas en un solo ciclo.
 */
@Composable
fun DemoDeRebote(motion: MotionPreferences) {
    val alturas = listOf(24.dp, 48.dp, 32.dp, 58.dp, 38.dp)
    var crecido by remember { mutableStateOf(false) }
    // Se repite en bucle: si solo creciera una vez, para volver a verlo habria que cerrar y
    // abrir el demo.
    LaunchedEffect(motion.bounce, motion.speed) {
        while (true) {
            crecido = false
            delay(500)
            crecido = true
            delay(2200)
        }
    }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(58.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            alturas.forEachIndexed { indice, alto ->
                val objetivo by animateDpAsState(
                    targetValue = if (crecido) alto else 5.dp,
                    animationSpec = spring(
                        dampingRatio = motion.bounce.damping,
                        stiffness = Spring.StiffnessMediumLow / motion.speed.factor.coerceAtLeast(0.2f)
                    ),
                    label = "barra$indice"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(objetivo)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
        PieDeDemo("${(420 * motion.speed.factor).toInt()} ms · ${motion.bounce.label.lowercase()}")
    }
}

/**
 * **Al pulsar**: un botón de verdad, para tocarlo las veces que haga falta.
 *
 * Es el único demo de la pantalla que se opera en vez de mirarse, y tiene que serlo: lo que se
 * está eligiendo es qué pasa **bajo el dedo**, y eso no se juzga viendo un bucle. Usa el mismo
 * `cleanClickable` que el resto de la app, así que lo que se siente aquí es exactamente lo que
 * se va a sentir en cualquier tarjeta.
 */
@Composable
fun DemoDePulsacion(motion: MotionPreferences) {
    Column {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                /*
                 * `cleanClickable` **antes** del fondo, y ese es el arreglo.
                 *
                 * El efecto de pulsacion se aplica con un `graphicsLayer`, y un
                 * `graphicsLayer` solo transforma lo que viene **despues** de el en la cadena.
                 * Estando detras del `background`, el fondo ya se habia pintado sin
                 * transformar: se hundia el texto y el boton se quedaba quieto, que es
                 * exactamente lo que se veia.
                 */
                modifier = Modifier
                    // La forma va como parametro: sin ella la onda se recorta al rectangulo y
                    // asoma por las esquinas del boton redondo.
                    .cleanClickable(shape = RoundedCornerShape(20.dp)) { }
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 26.dp, vertical = 12.dp)
            ) {
                val isEn = java.util.Locale.getDefault().language == "en"
                Text(
                    stringResource(R.string.settings_motion_press_me),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        PieDeDemo(
            texto = run {
                val isEn = java.util.Locale.getDefault().language == "en"
                when (motion.press) {
                    PressEffect.NINGUNA -> stringResource(R.string.settings_motion_press_none)
                    else -> "${motion.press.displayLabel.lowercase()} · ${stringResource(R.string.motion_toca_varias_veces)}"
                }
            },
            centrado = true
        )
    }
}

/**
 * **Indicador de carga**: el de verdad, el mismo que sale al esperar en la app.
 *
 * No es un dibujo suyo: es [UniLoading], el componente que lee el ajuste. Si mañana cambia,
 * este demo cambia con él y no se queda contando una versión vieja.
 */
@Composable
fun DemoDeCarga(motion: MotionPreferences) {
    Column {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            UniLoading(modifier = Modifier.size(46.dp))
        }
        PieDeDemo(
            texto = run {
                val isEn = java.util.Locale.getDefault().language == "en"
                when (motion.loading.id) {
                    "formas" -> stringResource(R.string.motion_gira_y_cambia_de_forma)
                    "onda" -> stringResource(R.string.motion_la_onda_de_m3e_girando)
                    "puntos" -> stringResource(R.string.motion_tres_puntos_por_turnos)
                    else -> stringResource(R.string.motion_el_circulo_de_siempre)
                }
            },
            centrado = true
        )
    }
}

/**
 * **Velocidad**: una fila entrando a la velocidad elegida, con su duración en números.
 *
 * El diseño no traía demo para este, pero la velocidad es lo único que multiplica a los otros
 * veinticuatro y merece verse sola: con «Nada» la fila aparece puesta, y ahí se entiende de
 * golpe que ese ajuste apaga y no ralentiza.
 */
@Composable
fun DemoDeVelocidad(motion: MotionPreferences) {
    var dentro by remember { mutableStateOf(false) }
    LaunchedEffect(motion.speed) {
        while (true) {
            dentro = false
            delay(420)
            dentro = true
            delay(1600)
        }
    }
    val avance by animateFloatAsState(
        targetValue = if (dentro) 1f else 0f,
        animationSpec = tween((420 * motion.speed.factor).toInt().coerceAtLeast(1)),
        label = "velocidad"
    )
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(26.dp)
                    .align(Alignment.CenterStart)
                    .graphicsLayer {
                        alpha = avance
                        translationX = 60f * (1f - avance)
                    }
                    .padding(start = 10.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        val isEnSpeed = java.util.Locale.getDefault().language == "en"
        PieDeDemo(
            texto = if (motion.speed.factor == 0f) {
                if (isEnSpeed) "none · row appears instantly without transition" else "nada · la fila aparece puesta, sin recorrido"
            } else {
                "${(420 * motion.speed.factor).toInt()} ms · ${motion.speed.displayLabel.lowercase()}"
            }
        )
    }
}

// ------------------------------------------------------------------ los comparativos

/**
 * Dos columnas, con y sin, para los interruptores.
 *
 * Un interruptor no tiene variantes que comparar: tiene tenerlo o no tenerlo, y eso solo se
 * juzga viendo las dos caras a la vez. La que está puesta se marca en el pie.
 */
@Composable
private fun Comparativo(
    izquierda: String,
    derecha: String,
    pie: String,
    contenido: @Composable (encendido: Boolean) -> Unit
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(izquierda to true, derecha to false).forEach { (rotulo, encendido) ->
                Column(modifier = Modifier.weight(1f)) {
                    RotuloDeColumna(rotulo)
                    contenido(encendido)
                }
            }
        }
        PieDeDemo(pie, centrado = true)
    }
}

/** **Barra inferior animada**: la pastilla viaja, o salta. */
@Composable
fun DemoDeBarra(motion: MotionPreferences) {
    var pestana by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(760)
            pestana = (pestana + 1) % 3
        }
    }
    val isEnBar = java.util.Locale.getDefault().language == "en"
    Comparativo(
        izquierda = if (isEnBar) "WITH ANIMATION" else "CON ANIMACIÓN",
        derecha = if (isEnBar) "WITHOUT ANIMATION" else "SIN ANIMACIÓN",
        pie = (if (isEnBar) "current setting: " else "ajuste actual: ") +
            (if (motion.animatedBottomBar) (if (isEnBar) "with animation" else "con animación") else (if (isEnBar) "instant jump" else "salta"))
    ) { animada ->
        val esquema = MaterialTheme.colorScheme
        val destino by animateFloatAsState(
            targetValue = pestana.toFloat(),
            animationSpec = if (animada) {
                spring(dampingRatio = motion.bounce.damping, stiffness = Spring.StiffnessMediumLow)
            } else {
                tween(1)
            },
            label = "pastilla"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(esquema.surfaceContainerHighest)
                .padding(5.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                repeat(3) { indice ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            listOf("A", "B", "C")[indice],
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (indice == pestana) esquema.onPrimary else esquema.onSurfaceVariant
                        )
                    }
                }
            }
            // La pastilla va encima del texto y con el mismo peso: es lo que hace que se lea
            // como que viaja por debajo y no como un rectangulo aparte.
            Box(
                modifier = Modifier
                    .fillMaxWidth(1f / 3f)
                    .fillMaxSize()
                    .graphicsLayer { translationX = destino * (size.width) }
                    .clip(CircleShape)
                    .background(esquema.primary.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    listOf("A", "B", "C")[pestana],
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = esquema.onPrimary
                )
            }
        }
    }
}

/** **Números que cuentan**: la cifra sube desde cero, o está puesta. */
@Composable
fun DemoDeNumeros(motion: MotionPreferences) {
    var arrancado by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            arrancado = false
            delay(400)
            arrancado = true
            delay(2400)
        }
    }
    val valor by animateFloatAsState(
        targetValue = if (arrancado) 61000f else 0f,
        animationSpec = tween((720 * motion.speed.factor).toInt().coerceAtLeast(1)),
        label = "cuenta"
    )
    val isEnCount = java.util.Locale.getDefault().language == "en"
    Comparativo(
        izquierda = if (isEnCount) "COUNTING" else "CONTANDO",
        derecha = if (isEnCount) "DIRECT" else "DIRECTO",
        pie = (if (isEnCount) "current setting: " else "ajuste actual: ") +
            (if (motion.countingNumbers) (if (isEnCount) "counting" else "contando") else (if (isEnCount) "direct" else "directo"))
    ) { contando ->
        Text(
            text = formatCurrency(if (contando) valor.toInt() else 61000),
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (contando) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** **Deslizar en las listas**: la fila se aparta y deja ver el fondo de borrar, o no se mueve. */
@Composable
fun DemoDeGesto(motion: MotionPreferences) {
    val ciclo = rememberInfiniteTransition(label = "gesto")
    val t by ciclo.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing)),
        label = "gesto"
    )
    val isEnSwipe = java.util.Locale.getDefault().language == "en"
    Comparativo(
        izquierda = if (isEnSwipe) "WITH GESTURE" else "CON GESTO",
        derecha = if (isEnSwipe) "WITHOUT IT" else "SIN ÉL",
        pie = (if (isEnSwipe) "current setting: " else "ajuste actual: ") +
            (if (motion.swipeGestures) (if (isEnSwipe) "swipe enabled" else "se puede deslizar") else (if (isEnSwipe) "swipe disabled" else "no se desliza"))
    ) { conGesto ->
        val esquema = MaterialTheme.colorScheme
        val avance = if (!conGesto) 0f else {
            val ida = ((t - 0.15f) / 0.35f).coerceIn(0f, 1f)
            val vuelta = ((t - 0.62f) / 0.28f).coerceIn(0f, 1f)
            ida - vuelta
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(esquema.errorContainer)
        ) {
            val isEnDel = java.util.Locale.getDefault().language == "en"
            Text(
                if (isEnDel) "Delete" else "Borrar",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = esquema.onErrorContainer,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = -avance * 62f }
                    .clip(RoundedCornerShape(12.dp))
                    .background(esquema.surfaceContainerHighest),
                contentAlignment = Alignment.CenterStart
            ) {
                val isEnTask = java.util.Locale.getDefault().language == "en"
                Text(
                    if (isEnTask) "Workshop 2" else "Taller 2",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = esquema.onSurface,
                    modifier = Modifier.padding(start = 11.dp)
                )
            }
        }
    }
}
