@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

import com.unistack.app.core.design.theme.LocalVividAccents
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/**
 * El movimiento de la calculadora, en un sitio.
 *
 * Cada pieza de aquí resuelve un momento en el que algo cambiaba de golpe: el sitio donde
 * escribes, el botón que pasa de avanzar a guardar, la nota que entra en la cuenta, el peso que
 * se reparte y el promedio que se recalcula.
 *
 * Todas usan los muelles del `motionScheme` del tema y no duraciones escritas a mano: si mañana
 * el tema cambia el ritmo de la app, esto lo sigue. Y como el tema respeta la preferencia de
 * «animaciones reducidas», apagarlas en Accesibilidad apaga también esto, sin condiciones
 * repartidas por cada pantalla.
 */

/**
 * Las casillas con un solo foco, que se mueve de una a otra.
 *
 * Antes cada casilla se pintaba activa o inactiva por su cuenta: al pasar de nota a porcentaje
 * una se encendía y la otra se apagaba a la vez, dos cosas sin relación aparente. Con un único
 * rectángulo que se desliza, lo que se lee es que el sitio donde escribes es uno y acaba de
 * cambiar de lugar.
 *
 * El foco se dibuja detrás con `drawBehind` y no como una casilla más: así no entra en la
 * medición —las casillas siguen repartiéndose el ancho a partes iguales— y puede quedarse en
 * cualquier punto intermedio mientras viaja.
 */
@Composable
internal fun SlotRow(
    focused: Int,
    slotCount: Int,
    modifier: Modifier = Modifier,
    gap: Dp = 9.dp,
    trailing: @Composable RowScope.() -> Unit = {},
    slots: @Composable RowScope.() -> Unit
) {
    val position by animateFloatAsState(
        targetValue = focused.coerceIn(0, (slotCount - 1).coerceAtLeast(0)).toFloat(),
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "foco"
    )
    // El foco va en el violeta vivo, no en el contenedor pálido del tema: es lo que marca
    // dónde estás escribiendo y tenía el mismo peso visual que el fondo de al lado.
    val tone = LocalVividAccents.current.violet

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .drawBehind { drawFocus(position, slotCount, gap.toPx(), tone) },
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
            content = slots
        )
        trailing()
    }
}

/** El rectángulo del foco, en el hueco que le toca según lo avanzado que vaya el viaje. */
private fun DrawScope.drawFocus(position: Float, count: Int, gapPx: Float, tone: Color) {
    if (count <= 0) return
    val slotWidth = (size.width - gapPx * (count - 1)) / count
    if (slotWidth <= 0f) return
    drawRoundRect(
        color = tone,
        topLeft = Offset(position * (slotWidth + gapPx), 0f),
        size = Size(slotWidth, size.height),
        cornerRadius = CornerRadius(16.dp.toPx())
    )
}

/**
 * Lo que entra en una lista, entrando.
 *
 * Una nota nueva aparecía sin más: es el único momento en que algo pasa a contar en el promedio,
 * y ocurría sin que nadie se enterase. Baja desde donde estaba la casilla, se pasa un pelo de
 * tamaño y se posa.
 *
 * Se anima solo la primera vez que se compone —el estado arranca en `false` y salta a `true` en
 * el mismo fotograma—, así que borrar otra fila no relanza la animación de las que ya estaban.
 */
@Composable
internal fun EnterOnAppear(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(
        visibleState = state,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
        ) { height -> -height / 2 } +
            scaleIn(
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                initialScale = 0.86f
            ) +
            fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
    ) {
        content()
    }
}

/**
 * El peso repartido, como una barra que se llena.
 *
 * El sitio libre solo se decía con letras, dentro del rótulo de una casilla: «VALE · queda 40 %».
 * Una barra dice de un vistazo cuánto de la materia llevas repartido, y cada nota entra como un
 * tramo que crece desde cero hasta el suyo mientras los de al lado se acomodan.
 *
 * El hueco libre no se pinta: es el fondo de la barra. Así llenarse y quedarse sin sitio son la
 * misma imagen, y no hay dos maneras de decir lo mismo.
 */
@Composable
internal fun WeightBar(
    weights: List<Double>,
    tones: List<Color>,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        var covered = 0f
        weights.forEachIndexed { index, weight ->
            val share = (weight / 100.0).toFloat().coerceIn(0f, 1f)
            covered += share
            /*
             * Cada tramo crece desde cero la primera vez que aparece.
             *
             * `animateFloatAsState` arranca ya en su destino: el tramo nuevo salia a su ancho
             * final de golpe y solo se animaban los cambios posteriores, asi que anadir una nota
             * —que es justo el momento que hay que contar— no se veia. Con un objetivo que
             * empieza en cero y salta a su parte en el efecto siguiente, la primera aparicion
             * tambien es un crecimiento.
             */
            var target by remember { mutableStateOf(0f) }
            LaunchedEffect(share) { target = share }
            val grown by animateFloatAsState(
                targetValue = target,
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "tramo"
            )
            if (grown > 0.0001f) {
                Box(
                    modifier = Modifier
                        .weight(grown)
                        .fillMaxHeight()
                        .background(tones[index % tones.size])
                )
            }
        }
        val free = (1f - covered).coerceAtLeast(0f)
        if (free > 0.0001f) Box(modifier = Modifier.weight(free))
    }
}

/**
 * Cuánto hay que escalar algo porque su valor acaba de cambiar.
 *
 * Devuelve 1 casi siempre y un pico corto justo después de un cambio. Late al cambiar el valor y
 * no al pulsar una tecla: si latiera con cada dígito dejaría de significar nada. Y no se anima el
 * conteo cifra a cifra a propósito —eso lo vuelve un juguete, y mientras corre se lee peor.
 *
 * La primera composición no late: al abrir la pantalla el número no «acaba de cambiar», estaba
 * ahí desde el principio.
 */
@Composable
internal fun bumpScale(value: Any?): Float {
    val scale = remember { Animatable(1f) }
    val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
    var seen by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        if (!seen) {
            seen = true
            return@LaunchedEffect
        }
        scale.snapTo(1.12f)
        scale.animateTo(1f, spec)
    }
    return scale.value
}

/**
 * Los colores de los tramos de la barra de peso.
 *
 * Salían de los tonos de sección, que son colores de *contenido*: en tema oscuro son claros y
 * lavados porque tienen que leerse como texto. Puestos de relleno, la barra entera parecía una
 * caja de acuarelas gastada. Estos seis son las mismas familias con la saturación que un relleno
 * sí puede permitirse.
 *
 * Seis bastan: una materia repartida en más de seis cortes es un caso que no se ha visto, y si
 * llega, se repiten en orden.
 */
@Composable
internal fun weightTones(): List<Color> = LocalVividAccents.current.ordered
