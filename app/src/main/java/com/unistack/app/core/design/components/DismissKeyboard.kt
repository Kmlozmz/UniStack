package com.unistack.app.core.design.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Cierra el teclado al tocar fuera de un campo o al desplazar la pantalla.
 *
 * Android no lo hace solo: un campo conserva el foco hasta que otra cosa se lo quita, así que
 * el teclado se quedaba puesto por mucho que se tocara el fondo y tapaba media pantalla. Va en
 * el contenedor del formulario, no en cada campo.
 *
 * Usa `detectTapGestures` y no `awaitFirstDown` a propósito: el primero solo reacciona a los
 * toques que ningún hijo ha consumido, así que tocar dentro de un campo —que sí lo consume— no
 * le quita el foco justo después de dárselo.
 */
fun Modifier.dismissKeyboardOnTapOutside(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    /*
     * Solo el desplazamiento que hace el dedo cierra el teclado.
     *
     * Sin la comprobación de origen valía cualquiera, y al enfocar un campo Compose desplaza la
     * lista él solo para dejarlo por encima del teclado: ese desplazamiento llegaba aquí y
     * cerraba el teclado recién abierto. Se veía en la caja de sugerencia, que está al final de
     * una lista larga y siempre necesita ese ajuste.
     */
    val clearOnScroll = remember(focusManager, keyboard) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) {
                    focusManager.clearFocus()
                    keyboard?.hide()
                }
                return Offset.Zero
            }
        }
    }

    this
        .nestedScroll(clearOnScroll)
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    focusManager.clearFocus()
                    keyboard?.hide()
                }
            )
        }
}
