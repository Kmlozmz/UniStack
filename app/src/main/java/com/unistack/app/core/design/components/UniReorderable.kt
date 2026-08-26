package com.unistack.app.core.design.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex

/**
 * Arrastrar una fila para cambiarla de sitio.
 *
 * **Esto estaba escrito dos veces y mal las dos.** La primera copia, en Apariencia, guardaba una
 * sola altura de fila para toda la lista: `onSizeChanged` la pisaba con la de cada fila que se
 * dibujaba, así que la cuenta de cuando permutar se hacía con la altura de otra fila. Con filas
 * de distinto alto —y las de Apariencia lo son— el resultado era que unas saltaban antes de
 * tiempo y otras se quedaban pegadas, dejando un hueco en la lista.
 *
 * Aquí cada fila apunta la suya, que es la única forma de que la cuenta salga con filas
 * desiguales.
 *
 * El gesto es el de Telegram: mantener pulsado levanta la fila, moverla la cambia de sitio, y
 * **soltar sin haberla movido** es un toque largo normal. Eso último permite que el mismo gesto
 * sirva para marcar y para ordenar, que es justo lo que hace falta en una lista con selección.
 */
@Stable
class UniReorderState internal constructor() {
    internal var draggedKey by mutableStateOf<Any?>(null)
    internal var offset by mutableFloatStateOf(0f)
    internal var moved = false
    private val heights = mutableStateMapOf<Any, Int>()

    internal fun remember(key: Any, height: Int) {
        if (height > 0) heights[key] = height
    }

    /** El alto de la fila que se arrastra; sin él, no hay con qué comparar el desplazamiento. */
    internal fun heightOf(key: Any): Int = heights[key] ?: 0

    internal fun release() {
        draggedKey = null
        offset = 0f
    }
}

@Composable
fun rememberUniReorderState(): UniReorderState = remember { UniReorderState() }

/**
 * Hace que una fila se pueda levantar y mover.
 *
 * @param key algo estable que identifique la fila. **No sirve la posición**: el detector se
 *   reinicia cuando su clave cambia, y como la posición cambia en la primera permuta, el dedo
 *   se quedaria a medias con la fila pegada al sitio nuevo.
 * @param index de dónde está la fila ahora. Es una función y no un número porque se lee en cada
 *   movimiento del dedo, ya cambiado por las permutas anteriores.
 * @param onMove mueve la fila de una posición a otra en tu lista.
 * @param onSettle se llama al soltar, y solo si la fila llegó a moverse. Es donde se guarda.
 * @param onLongPressWithoutMove se llama al soltar sin haber movido nada.
 */
fun Modifier.uniReorderable(
    state: UniReorderState,
    key: Any,
    index: () -> Int,
    itemCount: () -> Int,
    enabled: Boolean = true,
    onMove: (from: Int, to: Int) -> Unit,
    onSettle: () -> Unit = {},
    onLongPressWithoutMove: (() -> Unit)? = null
): Modifier = composed {
    val move by rememberUpdatedState(onMove)
    val settle by rememberUpdatedState(onSettle)
    val longPress by rememberUpdatedState(onLongPressWithoutMove)
    val at by rememberUpdatedState(index)
    val total by rememberUpdatedState(itemCount)
    val dragging = state.draggedKey == key

    this
        .zIndex(if (dragging) 1f else 0f)
        .graphicsLayer { translationY = if (dragging) state.offset else 0f }
        .onSizeChanged { state.remember(key, it.height) }
        .then(
            if (!enabled) {
                Modifier
            } else {
                Modifier.pointerInput(key) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            state.draggedKey = key
                            state.offset = 0f
                            state.moved = false
                        },
                        onDragEnd = {
                            if (state.moved) settle() else longPress?.invoke()
                            state.release()
                        },
                        onDragCancel = { state.release() },
                        onDrag = { change, amount ->
                            change.consume()
                            state.offset += amount.y
                            val height = state.heightOf(key)
                            if (height <= 0) return@detectDragGesturesAfterLongPress
                            val from = at()
                            if (from < 0) return@detectDragGesturesAfterLongPress
                            val step = when {
                                state.offset > height / 2f && from < total() - 1 -> 1
                                state.offset < -height / 2f && from > 0 -> -1
                                else -> 0
                            }
                            if (step != 0) {
                                state.moved = true
                                // Al permutar, la fila ya salta un hueco entero por sí sola:
                                // se le descuenta esa altura al arrastre para que siga bajo el
                                // dedo en vez de adelantarse.
                                state.offset -= step * height
                                move(from, from + step)
                            }
                        }
                    )
                }
            }
        )
}
