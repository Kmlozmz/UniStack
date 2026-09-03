package com.unistack.app.core.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.core.design.theme.muelleDeMovimiento
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
 * modificador tenía `indication = null` escrito a mano. Ahora las cuatro opciones pintan:
 *
 * - **Nada**: como estaba, sin respuesta visual.
 * - **Onda**: la de Material, que es la que trae el tema.
 * - **Hundir**: el elemento se encoge un 4% mientras el dedo está encima.
 * - **Rebote**: se encoge y al soltar se pasa de largo con muelle.
 *
 * Vivía copiado, palabra por palabra, en Gastos, en el formulario de gasto, en Materias y en
 * Tareas: cuatro copias idénticas que nadie iba a mantener a la vez.
 */
@Composable
fun Modifier.cleanClickable(onClick: () -> Unit): Modifier {
    val efecto = motionActual().press
    val fuente = remember { MutableInteractionSource() }
    val pulsado by fuente.collectIsPressedAsState()

    val escala by animateFloatAsState(
        targetValue = when {
            !pulsado || !hayMovimiento() -> 1f
            efecto == PressEffect.HUNDIR || efecto == PressEffect.REBOTE -> 0.96f
            else -> 1f
        },
        // El rebote usa muelle y el hundido no: es lo unico que los separa, y sin ello las dos
        // opciones se sentian iguales bajo el dedo.
        animationSpec = if (efecto == PressEffect.REBOTE) muelleDeMovimiento() else tweenDeMovimiento(baseMs = 110),
        label = "pulsacion"
    )

    val conEscala = if (efecto == PressEffect.HUNDIR || efecto == PressEffect.REBOTE) {
        graphicsLayer {
            scaleX = escala
            scaleY = escala
        }
    } else {
        Modifier
    }

    return this
        .then(conEscala)
        .clickable(
            interactionSource = fuente,
            // La onda sale de `LocalIndication`, que es la del tema: asi respeta el color de
            // acento sin que haya que pasarselo.
            indication = if (efecto == PressEffect.ONDA) LocalIndication.current else null,
            onClick = onClick
        )
}
