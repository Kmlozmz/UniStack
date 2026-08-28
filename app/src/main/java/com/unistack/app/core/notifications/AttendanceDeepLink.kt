package com.unistack.app.core.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** La clase por la que preguntaba el aviso que se acaba de tocar. */
data class PendingAttendance(val sessionId: String, val epochDay: Long)

/**
 * El aviso de asistencia abre **esa** clase, no el calendario.
 *
 * Los dos botones del aviso ya guardaban sin abrir nada, pero tocar el cuerpo dejaba en
 * Horario y ahí había que buscar el día, dar con la clase y abrir su panel: tres pasos para
 * responder una pregunta que la propia notificación acababa de hacer.
 *
 * Va por aquí y no por la ruta de navegación a propósito. `calendar` es una pestaña de la barra
 * inferior, y colgarle argumentos obliga a tocar cómo se decide qué pestaña está activa y cómo
 * se vuelve atrás —lo que se gana no compensa lo que se arriesga—. Esto es un dato de un solo
 * uso: la actividad lo deja aquí al recibir el intent y la pantalla lo recoge y lo borra.
 */
object AttendanceDeepLink {
    private val _pending = MutableStateFlow<PendingAttendance?>(null)
    val pending: StateFlow<PendingAttendance?> = _pending

    fun offer(sessionId: String, epochDay: Long) {
        if (sessionId.isBlank() || epochDay < 0L) return
        _pending.value = PendingAttendance(sessionId, epochDay)
    }

    /** Recogido y fuera: si no se borrase, volver a Horario reabriría el panel solo. */
    fun consume() {
        _pending.value = null
    }
}
