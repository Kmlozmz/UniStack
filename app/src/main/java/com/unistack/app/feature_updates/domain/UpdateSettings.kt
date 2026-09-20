package com.unistack.app.feature_updates.domain

/**
 * Cada cuánto mira la app si hay algo nuevo por su cuenta.
 *
 * Tres valores y no un número (los eligió él el 19 sep 2026): a diario, cada semana, o nunca
 * —«manual»: deberás comprobar tú desde Actualizaciones—. Un deslizador de horas no lo pediría
 * nadie.
 */
enum class CheckInterval {
    DAILY,
    WEEKLY,
    MANUAL
}

/**
 * Lo que decidió el 19 sep 2026 que viviera en el menú ⋮ de Actualizaciones.
 *
 * Los tres vienen encendidos: la app descarga sola con Wi-Fi, avisa de la versión nueva y mira
 * a diario. Quien no quiera que haga nada por su cuenta lo apaga desde el mismo menú.
 */
data class UpdateSettings(
    /** Con datos móviles no se descarga sola; a mano, pregunta antes de gastar datos. */
    val autoDownloadOnWifi: Boolean = true,
    /** «Notificar nueva versión»: todos los avisos de actualización, del primero al «lista». */
    val notifyNewVersion: Boolean = true,
    val checkInterval: CheckInterval = CheckInterval.DAILY
)
