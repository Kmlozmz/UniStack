package com.unistack.app.feature_updates.domain

import kotlinx.coroutines.flow.StateFlow

interface UpdateRepository {
    val state: StateFlow<UpdateState>

    /** Hasta qué punto de la escalera se aceptan actualizaciones. */
    val channel: StateFlow<UpdateChannel>
    fun setChannel(channel: UpdateChannel)

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
