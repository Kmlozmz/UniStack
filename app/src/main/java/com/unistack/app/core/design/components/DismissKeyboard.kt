package com.unistack.app.core.design.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Cierra el teclado al tocar fuera de un campo.
 *
 * Android no lo hace solo: un campo conserva el foco hasta que otra cosa se lo quita, así que
 * el teclado se quedaba puesto por mucho que se tocara el fondo y tapaba media pantalla. Va en
 * el contenedor del formulario, no en cada campo.
 *
 * Usa `detectTapGestures` y no `awaitFirstDown` a propósito: el primero solo reacciona a los
 * toques que ningún hijo ha consumido, así que tocar dentro de un campo —que sí lo consume— no
 * le quita el foco justo después de dárselo.
 *
 * Y solo escucha toques. Hubo aquí una regla que cerraba el teclado al desplazar la lista, y
 * el desplazamiento no siempre lo pide el dedo: al enfocar un campo, y en cada línea nueva
 * mientras se escribe en uno de varias líneas, Compose desplaza la lista él solo para
 * mantenerlo por encima del teclado. Ese desplazamiento cerraba el teclado en mitad de la
 * frase. Filtrarlo por origen no bastó —llega con el mismo que el del dedo—, así que la regla
 * se fue entera: tocar fuera ya resuelve lo que había que resolver.
 */
fun Modifier.dismissKeyboardOnTapOutside(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    this.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                focusManager.clearFocus()
                keyboard?.hide()
            }
        )
    }
}
