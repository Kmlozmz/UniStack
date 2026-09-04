package com.unistack.app.core.design.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.core.design.theme.tweenDeMovimiento
import com.unistack.app.feature_user.domain.PressEffect

/**
 * Un clic con **el gesto que se haya elegido en Movimiento**.
 *
 * Se usa donde el propio elemento ya responde al toque —una tarjeta que se rellena, un chip que
 * cambia de color— y por eso nació sin la onda de Material: la onda encima era una segunda
 * respuesta a la misma acción.
 *
 * **«Al pulsar» era el ajuste más visible de los veinticinco y no hacía nada.** Se elegía entre
 * onda, hundir y rebote, se guardaba, y cada toque de la app seguía sin responder porque este
 * modificador tenía `indication = null` escrito a mano.
 *
 * **El orden de dentro es lo que hace que funcione**, y costó dos intentos:
 *
 * 1. El `graphicsLayer` del hundido va **antes** que nada. Un `graphicsLayer` solo transforma
 *    lo que viene después en la cadena, así que puesto al final dejaba el fondo sin mover y
 *    solo se hundía el texto.
 * 2. El recorte va **antes** del `clickable`. La onda de Material se pinta en el nodo del
 *    clic, así que sin recorte por delante se derrama fuera del botón —el cerco gris que se
 *    salía por los lados—.
 *
 * @param shape la forma con la que recortar la onda. Sin ella se recorta al rectángulo, que es
 *   lo correcto para una fila; un botón redondo tiene que pasar la suya o la onda le asoma por
 *   las esquinas.
 */
@Composable
fun Modifier.cleanClickable(shape: Shape = RectangleShape, onClick: () -> Unit): Modifier {
    val efecto = motionActual().press
    val fuente = remember { MutableInteractionSource() }
    val pulsado by fuente.collectIsPressedAsState()
    val enMovimiento = hayMovimiento()

    /*
     * Hundir y rebote **no pueden llegar al mismo sitio**, o se sienten iguales.
     *
     * Los dos bajaban al 96% y solo cambiaba la curva, que bajo el dedo no se distingue.
     * Ahora hundir baja más y vuelve sin pasarse —es un botón que cede—, y rebote baja menos
     * pero vuelve con un muelle muy suelto que **se pasa de 1**: al soltar da el saltito, que
     * es de donde le viene el nombre.
     */
    val destino = when {
        !pulsado || !enMovimiento -> 1f
        efecto == PressEffect.HUNDIR -> 0.90f
        efecto == PressEffect.REBOTE -> 0.94f
        else -> 1f
    }
    val escala by animateFloatAsState(
        targetValue = destino,
        animationSpec = if (efecto == PressEffect.REBOTE) {
            spring(dampingRatio = 0.32f, stiffness = Spring.StiffnessMedium)
        } else {
            tweenDeMovimiento(baseMs = 90)
        },
        label = "pulsacion"
    )

    val conEscala = if (efecto == PressEffect.HUNDIR || efecto == PressEffect.REBOTE) {
        Modifier.graphicsLayer {
            scaleX = escala
            scaleY = escala
        }
    } else {
        Modifier
    }

    return this
        .then(conEscala)
        .clip(shape)
        .clickable(
            interactionSource = fuente,
            // La onda sale de `LocalIndication`, que es la del tema: así respeta el color de
            // acento sin que haya que pasárselo.
            indication = if (efecto == PressEffect.ONDA && enMovimiento) LocalIndication.current else null,
            onClick = onClick
        )
}
