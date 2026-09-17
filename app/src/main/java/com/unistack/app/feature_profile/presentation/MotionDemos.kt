@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniLoading
import com.unistack.app.feature_user.domain.MotionPreferences
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
        PieDeDemo(
            texto = if (motion.speed.factor == 0f) {
                stringResource(R.string.motion_demo_speed_none)
            } else {
                "${(420 * motion.speed.factor).toInt()} ms · ${motion.speed.displayLabel.lowercase()}"
            }
        )
    }
}

// ------------------------------------------------------------------ los comparativos
