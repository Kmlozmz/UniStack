package com.unistack.app.feature_sync.domain

import kotlinx.coroutines.flow.StateFlow

data class CloudBackupState(
    val inProgress: Boolean = false,
    val lastBackupAt: Long? = null,
    val lastRestoreAt: Long? = null,
    val message: String? = null,
    val errorMessage: String? = null
)

interface CloudBackupRepository {
    val state: StateFlow<CloudBackupState>

    /**
     * Si la copia en la nube puede funcionar en esta compilación.
     *
     * Sin `google-services.json` no hay proyecto al que hablar, así que los botones fallaban al
     * pulsarlos con un mensaje de configuración que no le dice nada a quien usa la app. Es mejor
     * decir de entrada que todavía no está disponible.
     */
    val isConfigured: Boolean

    suspend fun backupNow(): Result<Unit>
    suspend fun restoreLatest(): Result<Unit>
}
