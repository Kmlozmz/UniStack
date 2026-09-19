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

    /** Cuántos APK descargados ocupan espacio ahora mismo. Cambia al descargar y al limpiar. */
    val pendingApks: StateFlow<Int>

    /** Vuelve a mirar el disco, por si algo cambió mientras la app no estaba delante. */
    fun refreshPendingApks()

    /**
     * Cuándo se miró por última vez, en milisegundos; cero si nunca.
     *
     * Es lo que la tarjeta de estado pone debajo del título («hace 2 min»): una pantalla que
     * dice «estás al día» no aclara nada si no dice desde cuándo.
     */
    val lastCheckedAt: StateFlow<Long>

    /** Lo del menú ⋮: descargar sola con Wi-Fi, avisar al estar lista, cada cuánto mirar. */
    val settings: StateFlow<UpdateSettings>
    fun updateSettings(transform: (UpdateSettings) -> UpdateSettings)

    /** Las versiones que han estado puestas en este teléfono, la actual primero. */
    val installHistory: StateFlow<List<InstalledVersion>>

    /**
     * La última versión para la que ya se enseñó la hoja de «Actualización disponible».
     *
     * La hoja sale una vez por versión: cerrarla deslizando la apunta aquí y no vuelve a salir
     * hasta que haya otra versión. Antes cerrarla ponía el estado en reposo y se llevaba con
     * ella la propia actualización de la pantalla.
     */
    val sheetSeenVersion: StateFlow<String?>
    fun markSheetSeen(versionName: String)

    /**
     * Fuerza un estado, para el banco de pruebas.
     *
     * Ver el flujo entero exige que GitHub tenga publicada una versión más nueva que la que
     * llevas, y eso casi nunca pasa mientras se desarrolla. Con `null` vuelve a lo real.
     */
    fun simulate(state: UpdateState?)
}
