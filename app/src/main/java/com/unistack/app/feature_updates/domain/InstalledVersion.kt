package com.unistack.app.feature_updates.domain

/** Una versión que ha estado puesta en este teléfono, y desde cuándo. */
data class InstalledVersion(
    val versionName: String,
    val installedAtMillis: Long
)

/**
 * El historial de versiones: solo la que llevas y las anteriores.
 *
 * **Lo nuevo no va aquí** («al ser historial», 19 sep 2026): una versión entra en la lista
 * cuando la app arranca con ella puesta, no cuando se publica ni cuando se descarga. Por eso
 * la lista no sale de GitHub sino de lo que este teléfono ha ido ejecutando.
 *
 * Se guarda de la más nueva a la más vieja, que es como se lee.
 */
object HistorialDeVersiones {

    /**
     * Apunta [version] como la que está puesta ahora, si no era ya la última apuntada.
     *
     * Volver a arrancar con la misma versión no añade nada; arrancar con otra —más nueva o,
     * tras reinstalar, más vieja— la pone en cabeza con la hora en que se instaló.
     */
    fun registrar(historial: List<InstalledVersion>, version: String, instaladaEn: Long): List<InstalledVersion> {
        if (historial.firstOrNull()?.versionName == version) return historial
        return listOf(InstalledVersion(version, instaladaEn)) + historial.filterNot { it.versionName == version }
    }

    /**
     * Cuántos días estuvo puesta la versión en [indice], o nulo si es la de ahora.
     *
     * Una versión dura hasta que se instaló la siguiente (la que va justo delante en la lista);
     * la actual no ha terminado y no se cuenta. Menos de un día se redondea a uno: estuvo.
     */
    fun diasInstalada(historial: List<InstalledVersion>, indice: Int): Int? {
        if (indice <= 0 || indice >= historial.size) return null
        val esta = historial[indice].installedAtMillis
        val siguiente = historial[indice - 1].installedAtMillis
        val dias = (siguiente - esta) / DIA_MILLIS
        return dias.toInt().coerceAtLeast(1)
    }

    private const val DIA_MILLIS = 24 * 60 * 60 * 1000L
}
