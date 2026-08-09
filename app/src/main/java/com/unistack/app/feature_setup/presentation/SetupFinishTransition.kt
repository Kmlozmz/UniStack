package com.unistack.app.feature_setup.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniStackBrandMark
import com.unistack.app.core.design.components.UniStackBrandPill
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.core.design.theme.UniStackColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/** Ancho del símbolo mientras se dispersa. */
private val ExitMarkWidth = 132.dp

/**
 * Cierre del onboarding: el sello de confirmación se deshace en las tres píldoras de la
 * marca, que se muestran un instante formando el logo y salen disparadas a los lados.
 *
 * Se dispersan en el orden inverso al de la animación de arranque, donde las píldoras se
 * juntan para formar el logo. Así el primer arranque queda cerrado por los dos extremos.
 *
 * No hay velo de color al terminar: la pantalla queda en el fondo de la app, que es el
 * mismo del inicio, de modo que el relevo entre una y otra no tiene costura. El inicio se
 * encarga de aparecer con su propio fundido.
 *
 * [onFinished] se invoca cuando ya no queda nada en pantalla, no al pulsar. Quien llama
 * debe confirmar el setup ahí dentro: hacerlo antes cambia la pantalla bajo los pies de
 * esta animación y la corta a la mitad.
 */
@Composable
fun SetupFinishTransition(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestOnFinished by rememberUpdatedState(onFinished)
    val motionEnabled = LocalMotionDurationScale.current > 0f

    val pillOffsets = remember { List(3) { Animatable(0f) } }
    val pillAlphas = remember { List(3) { Animatable(0f) } }
    val pillScales = remember { List(3) { Animatable(0.55f) } }
    val farewellAlpha = remember { Animatable(0f) }
    val farewellRise = remember { Animatable(14f) }
    val waveProgress = remember { Animatable(0f) }

    LaunchedEffect(motionEnabled) {
        if (!motionEnabled) {
            // Sin animaciones no tiene sentido esperar mirando una pantalla quieta.
            latestOnFinished()
            return@LaunchedEffect
        }

        // Deja que la tarjeta y los botones terminen de retirarse antes de empezar.
        delay(380)

        // Las píldoras brotan del sello y componen el logo.
        pillOffsets.indices.forEach { index ->
            launch {
                pillAlphas[index].animateTo(1f, tween(durationMillis = 220, easing = LinearEasing))
            }
            launch {
                pillScales[index].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                )
            }
        }
        delay(220)

        // La despedida sube bajo el logo ya montado.
        launch {
            farewellAlpha.animateTo(1f, tween(durationMillis = 260, easing = LinearEasing))
        }
        launch {
            farewellRise.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }

        // Respiro con todo en pantalla: es lo que se ha estado construyendo, y sin esta
        // pausa pasa tan deprisa que no da tiempo ni a leer la frase ni a reconocer el
        // símbolo de la marca.
        delay(540)

        // La frase se retira antes que las píldoras, para no leerse a medio dispersar.
        launch {
            farewellAlpha.animateTo(0f, tween(durationMillis = 240, easing = LinearEasing))
        }

        // Dispersión, en el orden inverso al de la entrada.
        val directions = listOf(1f, -1f, 1f)
        val durations = listOf(520, 470, 420)
        directions.indices.forEach { index ->
            launch {
                pillOffsets[index].animateTo(
                    targetValue = directions[index],
                    animationSpec = tween(
                        durationMillis = durations[index],
                        easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                pillAlphas[index].animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = durations[index])
                )
            }
            delay(110)
        }

        // La onda arranca antes de que salga la última píldora: encadenadas se leen como un
        // solo gesto —la dispersión libera el color—, mientras que en secuencia limpia
        // parecen dos animaciones pegadas.
        delay(200)
        waveProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 440, easing = FastOutSlowInEasing)
        )
        delay(110)

        // Y se retira encogiéndose, no desvaneciéndose. El color es claro y lo que queda
        // debajo es oscuro: a media opacidad la mezcla da un azul apagado que no es ninguno
        // de los dos y se lee como un destello aparte. Encogiendo no hay mezcla en ningún
        // momento, solo un círculo que se cierra sobre el fondo de la app.
        //
        // Tiene que resolverse aquí dentro: al terminar, esta pantalla desaparece de golpe,
        // y dejarla llena de color habría dado un corte seco contra el fondo del inicio.
        waveProgress.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
        )
        latestOnFinished()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(
                    width = ExitMarkWidth,
                    height = ExitMarkWidth * UniStackBrandMark.HeightRatio
                )
            ) {
                UniStackBrandMark.Pills.forEachIndexed { index, pill ->
                    UniStackBrandPill(
                        pill = pill,
                        markWidth = ExitMarkWidth,
                        modifier = Modifier.graphicsLayer {
                            // La distancia se mide en anchos de la propia píldora, para que
                            // salga de la pantalla en cualquier tamaño de dispositivo.
                            translationX =
                                pillOffsets[index].value * size.width.coerceAtLeast(1f) * 4f
                            alpha = pillAlphas[index].value
                            scaleX = pillScales[index].value
                            scaleY = pillScales[index].value
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                // Sirve para cualquier nivel: primaria, secundaria, universidad y «otro».
                // Hablar de «semestre» habría dejado fuera a media app.
                text = "Mucho éxito en tus estudios",
                color = UniStackColors.TextPrimary,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = farewellAlpha.value
                    translationY = farewellRise.value.dp.toPx()
                }
            )
        }

        SetupFinishWave(progress = waveProgress.value)
    }
}

/**
 * Círculo del color de marca que crece desde el centro hasta cubrir la pantalla.
 *
 * Sale de UniStackColors.Primary, que con el acento dinámico activo lo deriva Monet del
 * fondo de pantalla del usuario, así que el cierre se tiñe de su propio color.
 */
@Composable
private fun SetupFinishWave(progress: Float) {
    if (progress <= 0f) return
    val color = UniStackColors.Primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        // Radio necesario para alcanzar las esquinas desde el centro.
        val farthest = hypot(size.width / 2f, size.height / 2f)
        drawCircle(color = color, radius = farthest * progress, center = center)
    }
}
