package com.unistack.app.feature_updates.domain

import kotlinx.coroutines.flow.StateFlow

/**
 * De dónde salen las actualizaciones.
 *
 * **No hay canales.** Los hubo —estable, beta y alpha, cada uno con su código de acceso— y se
 * quitaron enteros: la app toma siempre la última publicación de GitHub, sea preestreno o
 * definitiva. Mantenerlos obligaba a una lista de códigos publicada aparte, a un periodo de
 * gracia para revocarlos y a decidir en cada consulta qué versiones «acepta» tu canal, y a
 * cambio de eso repartía las mismas descargas públicas que cualquiera podía bajar del enlace.
 */
interface UpdateRepository {
    val state: StateFlow<UpdateState>

    /**
     * Las publicaciones recientes, de la más nueva a la más vieja.
     *
     * La pantalla las enseña con sus notas: quien va a instalar quiere ver qué cambió, y si se
     * saltó una versión, qué cambió en la que no llegó a poner.
     */
    val releases: StateFlow<List<UpdateInfo>>

    suspend fun checkForUpdates()
    suspend fun checkForUpdatesIfDue()
    fun downloadUpdate()
    fun installUpdate()

    /** ¿Puede la app instalar APKs, o falta que el usuario la autorice como origen? */
    fun canInstallPackages(): Boolean

    /** Lleva al interruptor de «instalar apps desconocidas» de esta app. */
    fun openInstallPermissionSettings()
    fun clearDownload()
    fun dismiss()

    /** Cuántos APK descargados ocupan espacio ahora mismo. Cambia al descargar y al limpiar. */
    val pendingApks: StateFlow<Int>

    /** Vuelve a mirar el disco, por si algo cambió mientras la app no estaba delante. */
    fun refreshPendingApks()
}
