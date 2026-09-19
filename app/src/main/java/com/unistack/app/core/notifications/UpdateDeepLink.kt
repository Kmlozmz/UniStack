package com.unistack.app.core.notifications

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * El botón «Instalar» del aviso de «lista para instalar».
 *
 * Abre la app en Actualizaciones y, además, el instalador. Un receptor no puede abrir el
 * instalador desde el fondo (Android 10 lo bloquea), así que el botón abre la actividad con
 * este encargo y la pantalla lo recoge, instala y lo borra. Mismo patrón que [AttendanceDeepLink].
 */
object UpdateDeepLink {
    private val _installRequested = MutableStateFlow(false)
    val installRequested: StateFlow<Boolean> = _installRequested

    fun offerInstall() {
        _installRequested.value = true
    }

    fun consume() {
        _installRequested.value = false
    }
}
