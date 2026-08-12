package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Margen para una acción anclada al fondo de la pantalla, que la mantiene por encima tanto
 * de la barra de gestos como del teclado.
 *
 * Se usa **dentro** de la superficie, nunca sobre ella: aplicado por fuera levanta la barra
 * entera y deja una franja transparente contra el borde por la que se ve pasar el contenido
 * al desplazarse.
 *
 * `union` y no los dos márgenes encadenados. El hueco que deja el teclado ya abarca el de
 * la barra de gestos, así que sumarlos dejaría el botón flotando un dedo por encima de donde
 * toca; `union` se queda con el mayor de los dos, que es justo el que hace falta.
 *
 * Hace falta desde targetSdk 36: el modo edge-to-edge es obligatorio y la ventana ya no se
 * redimensiona sola al abrir el teclado, así que cada pantalla tiene que apartarse ella.
 */
@Composable
fun Modifier.bottomActionInsets(): Modifier =
    windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
