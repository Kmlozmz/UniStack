package com.unistack.app.core.utils

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * Un toque háptico que no puede tumbar la app.
 *
 * El arreglo de verdad es declarar `VIBRATE` en el manifest, y está declarado. Esto es el
 * cinturón: en la ruta normal de Android vibra el sistema por cuenta de la app, pero algunas
 * capas de fabricante —MIUI/HyperOS entre ellas— la sustituyen por una llamada a `Vibrator`
 * dentro del proceso, y ahí cualquier diferencia de permisos entre versiones de la capa vuelve
 * a lanzar `SecurityException`. Un tic decorativo al pulsar un botón no puede ser el motivo de
 * que alguien pierda lo que estaba escribiendo.
 *
 * Solo se traga `SecurityException`: si el fallo es otro, que se vea.
 */
fun HapticFeedback.performSafely(type: HapticFeedbackType) {
    try {
        performHapticFeedback(type)
    } catch (_: SecurityException) {
        // Sin vibración; la acción que la acompañaba sigue su curso.
    }
}
