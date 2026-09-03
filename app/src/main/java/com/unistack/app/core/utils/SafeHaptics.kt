package com.unistack.app.core.utils

import androidx.compose.ui.hapticfeedback.HapticFeedback
import com.unistack.app.feature_user.domain.HapticStrength
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
    /*
     * La fuerza elegida en Movimiento manda, y se aplica **aqui**.
     *
     * `haptics` se guardaba y solo vibraba al elegirlo en su propia pantalla: en el resto de la
     * app cada toque avisaba igual, eligiera lo que eligiera. Este es el punto por el que pasan
     * los doce sitios que vibran, asi que aplicarlo aqui vale para todos sin tocar ninguno.
     *
     * No puede ser un `CompositionLocal` porque casi todas las llamadas salen de un `onClick`,
     * que ya no esta en composicion. Es el mismo patron que `AppearanceRuntime.cornerStyle`.
     */
    val ajustada = when (HapticRuntime.strength) {
        HapticStrength.NINGUNA -> return
        // El aviso mas leve que da Android, para quien quiere notarlo sin que le sobresalte.
        HapticStrength.SUAVE -> HapticFeedbackType.TextHandleMove
        HapticStrength.MEDIA -> type
        HapticStrength.FUERTE -> HapticFeedbackType.LongPress
    }
    try {
        performHapticFeedback(ajustada)
        // Dos seguidos es lo mas cerca que se puede estar de subir la intensidad sin pedir
        // permiso de vibrador.
        if (HapticRuntime.strength == HapticStrength.FUERTE) performHapticFeedback(ajustada)
    } catch (_: SecurityException) {
        // Sin vibración; la acción que la acompañaba sigue su curso.
    }
}

/**
 * La fuerza de vibracion que hay puesta, fuera de la composicion.
 *
 * La escribe el tema en cada recomposicion y la lee [performSafely], que se llama desde
 * `onClick` y por tanto no puede leer un `CompositionLocal`.
 */
object HapticRuntime {
    var strength: HapticStrength = HapticStrength.MEDIA
        internal set
}