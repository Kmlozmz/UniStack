package com.unistack.app.feature_updates.domain

import kotlinx.coroutines.flow.StateFlow

interface UpdateRepository {
    val state: StateFlow<UpdateState>

    /** Hasta qué punto de la escalera se aceptan actualizaciones. */
    val channel: StateFlow<UpdateChannel>
    fun setChannel(channel: UpdateChannel)

    /**
     * Los canales que este móvil puede elegir. Siempre incluye [UpdateChannel.STABLE], que no
     * necesita permiso; cada código añade el suyo, y tener el de alpha no da el de beta.
     */
    val unlockedChannels: StateFlow<Set<UpdateChannel>>

    /**
     * Canjea un código para el canal que se está intentando abrir.
     *
     * Devuelve el canal si el código es **el de ese canal**; null en cualquier otro caso: que no
     * exista, que no se pudiera consultar la lista, o que sea el código de otro canal. Los tres
     * se responden igual a propósito, para no ir diciendo a qué canal pertenece un código que
     * alguien acaba de probar.
     */
    suspend fun redeemAccessCode(code: String, channel: UpdateChannel): UpdateChannel?

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
    fun hasPendingDownload(): Boolean
}
