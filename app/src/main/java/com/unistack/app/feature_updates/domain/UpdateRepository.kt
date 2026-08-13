package com.unistack.app.feature_updates.domain

import kotlinx.coroutines.flow.StateFlow

interface UpdateRepository {
    val state: StateFlow<UpdateState>

    /** Hasta qué punto de la escalera se aceptan actualizaciones. */
    val channel: StateFlow<UpdateChannel>
    fun setChannel(channel: UpdateChannel)

    /** El canal más alto que este móvil tiene permitido elegir. */
    val unlockedChannel: StateFlow<UpdateChannel>

    /**
     * Canjea un código de acceso contra la lista publicada.
     *
     * Devuelve el canal que abre, o null si el código no está en la lista o no se pudo
     * consultar. Se distingue un caso del otro por el mensaje del estado.
     */
    suspend fun redeemAccessCode(code: String): UpdateChannel?

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
