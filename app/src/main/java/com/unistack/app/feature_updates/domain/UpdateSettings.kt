package com.unistack.app.feature_updates.domain

/**
 * Cada cuánto mira la app si hay algo nuevo, aunque esté cerrada.
 *
 * Tres valores y no un número: «cada 2 horas» es lo de siempre, «cada día» para quien no quiere
 * que la app hable, y «solo a mano» para quien prefiere entrar a mirar. Un deslizador de minutos
 * no lo pediría nadie.
 */
enum class CheckInterval {
    EVERY_2H,
    DAILY,
    MANUAL;

    /** El siguiente en la rueda: el chip del menú va pasando de uno a otro con cada toque. */
    fun next(): CheckInterval = entries[(ordinal + 1) % entries.size]
}

/**
 * Lo que decidió el 19 sep 2026 que viviera en el menú ⋮ de Actualizaciones.
 *
 * Los tres vienen encendidos: la app descarga sola con Wi-Fi, avisa sin sonido cuando ya está y
 * mira cada dos horas. Quien no quiera que haga nada por su cuenta lo apaga desde el mismo menú.
 */
data class UpdateSettings(
    /** Con datos móviles no se descarga sola; a mano, pregunta antes de gastar datos. */
    val autoDownloadOnWifi: Boolean = true,
    /** «Notificar nueva versión»: todos los avisos de actualización, del primero al «lista». */
    val notifyNewVersion: Boolean = true,
    val checkInterval: CheckInterval = CheckInterval.EVERY_2H
)
