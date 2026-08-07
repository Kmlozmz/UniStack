package com.unistack.app.core.design.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/**
 * Desplaza el contenedor con scroll para dejar este elemento a la vista cuando [visible]
 * pasa a true.
 *
 * Pensado para el revelado progresivo: al mostrarse una sección nueva al final de la
 * pantalla, sin esto aparece cortada abajo y el usuario tiene que descubrir que hay más
 * contenido y desplazarse a mano.
 *
 * @param settleMillis espera antes de desplazar. Hace falta porque la animación de
 *   expansión arranca con altura casi cero: traer a la vista en ese instante no serviría
 *   de nada, ya que el destino todavía no tiene su tamaño final.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.revealIntoView(
    visible: Boolean,
    settleMillis: Long = 240
): Modifier {
    val requester = remember { BringIntoViewRequester() }
    LaunchedEffect(visible) {
        if (!visible) return@LaunchedEffect
        delay(settleMillis)
        requester.bringIntoView()
    }
    return this.bringIntoViewRequester(requester)
}
