package com.unistack.app.core.design.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.zIndex
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/**
 * Arrastrar una fila para cambiarla de sitio.
 *
 * **El gesto vive en el asa, no en la fila.** Se probó al revés —mantener pulsado en cualquier
 * parte levantaba la fila, y soltarla sin moverla la marcaba— y no hay forma de adivinarlo: dos
 * cosas distintas detrás del mismo gesto, y cuál de las dos ibas a obtener dependía de si tu
 * dedo se movía o no. Con un asa a la vista, mantener pulsada la fila marca y mantener pulsada
 * el asa mueve, y cada gesto tiene su sitio.
 *
 * **Esto estaba escrito dos veces y mal las dos.** La copia de Apariencia guardaba una sola
 * altura de fila para toda la lista: `onSizeChanged` la pisaba con la de cada fila que se
 * dibujaba, así que la cuenta de cuándo permutar se hacía con la altura de otra. Con filas de
 * distinto alto —y las de Apariencia lo son— unas saltaban antes de tiempo y otras se quedaban
 * pegadas, dejando un hueco. Aquí cada fila apunta la suya.
 */
@Stable
class UniReorderState internal constructor() {
    internal var draggedKey by mutableStateOf<Any?>(null)
    internal var offset by mutableFloatStateOf(0f)
    internal var heldHandleKey by mutableStateOf<Any?>(null)
    private val heights = mutableStateMapOf<Any, Int>()

    /** Si hay algo levantado ahora mismo. Sirve para apagar el toque mientras dura. */
    val isDragging: Boolean get() = draggedKey != null

    /**
     * Si el dedo está sobre el asa de esta fila, **desde que la toca** y no desde que la
     * levanta.
     *
     * Existe porque [isDragging] llega tarde. Una fila que se puede mantener pulsada para
     * marcarla y que lleva un asa dentro tiene dos detectores de pulsación larga corriendo a la
     * vez sobre el mismo dedo, y los dos arrancan su cuenta atrás en el mismo instante. Cuál de
     * los dos despierta primero al cumplirse el plazo no está decidido por nada: son dos
     * corrutinas esperando el mismo tiempo. Se intentó preguntar por [isDragging] dentro del
     * `onLongClick` de la fila, y falla justo la mitad de las veces —cuando gana la fila, el
     * arrastre aún no ha empezado y la marca se quita—.
     *
     * El evento de bajar el dedo sí está ordenado: ocurre un plazo entero antes que cualquiera
     * de las dos pulsaciones largas. Preguntando por esto, la respuesta ya no depende de quién
     * gane la carrera.
     */
    fun isHandleHeld(key: Any): Boolean = heldHandleKey == key

    internal fun measure(key: Any, height: Int) {
        if (height > 0) heights[key] = height
    }

    internal fun heightOf(key: Any): Int = heights[key] ?: 0

    internal fun release() {
        draggedKey = null
        offset = 0f
    }
}

@Composable
fun rememberUniReorderState(): UniReorderState = remember { UniReorderState() }

/**
 * Lo que le pasa a la fila mientras se arrastra: se levanta por encima del resto y sigue al dedo.
 *
 * Va en la fila entera. El gesto que la mueve va aparte, en [uniReorderHandle].
 */
fun Modifier.uniReorderableItem(
    state: UniReorderState,
    key: Any
): Modifier = composed {
    val dragging = state.draggedKey == key
    this
        .zIndex(if (dragging) 1f else 0f)
        .graphicsLayer { translationY = if (dragging) state.offset else 0f }
        .onSizeChanged { state.measure(key, it.height) }
}

/**
 * El asa: mantenerla pulsada levanta la fila y moverla la cambia de sitio.
 *
 * @param key algo estable que identifique la fila. **No sirve la posición**: el detector se
 *   reinicia cuando su clave cambia, y como la posición cambia en la primera permuta, el dedo se
 *   quedaría a medias con la fila pegada al sitio nuevo.
 * @param index dónde está la fila ahora. Es una función y no un número porque se lee en cada
 *   movimiento del dedo, ya cambiada por las permutas anteriores.
 * @param onMove mueve la fila de una posición a otra en tu lista.
 * @param onSettle se llama al soltar. Es donde se guarda.
 */
fun Modifier.uniReorderHandle(
    state: UniReorderState,
    key: Any,
    index: () -> Int,
    itemCount: () -> Int,
    onMove: (from: Int, to: Int) -> Unit,
    onSettle: () -> Unit = {}
): Modifier = composed {
    val move by rememberUpdatedState(onMove)
    val settle by rememberUpdatedState(onSettle)
    val at by rememberUpdatedState(index)
    val total by rememberUpdatedState(itemCount)

    this
        /*
         * Apuntar el dedo en cuanto baja, antes de que nadie decida nada.
         *
         * No consume el evento a propósito: la fila de debajo sigue recibiéndolo y sigue
         * pudiendo abrirse con un toque corto. Lo único que hace es dejar constancia de dónde
         * empezó el gesto, para que quien tenga que decidir pueda mirarlo.
         */
        .pointerInput(key) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                state.heldHandleKey = key
                try {
                    // Hasta que se levanten todos los dedos. Se espera el evento en vez de
                    // `waitForUpOrCancellation` porque esa se rinde en cuanto el arrastre
                    // consume el movimiento, que es precisamente cuando hay que seguir.
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                } finally {
                    if (state.heldHandleKey == key) state.heldHandleKey = null
                }
            }
        }
        .pointerInput(key) {
        detectDragGesturesAfterLongPress(
            onDragStart = {
                state.draggedKey = key
                state.offset = 0f
            },
            onDragEnd = {
                settle()
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
                    // Al permutar, la fila ya salta un hueco entero por sí sola: se le
                    // descuenta esa altura al arrastre para que siga bajo el dedo en vez de
                    // adelantarse.
                    state.offset -= step * height
                    move(from, from + step)
                }
            }
        )
        }
}
