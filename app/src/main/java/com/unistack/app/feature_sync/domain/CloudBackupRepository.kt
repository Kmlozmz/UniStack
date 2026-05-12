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

    suspend fun backupNow(): Result<Unit>
    suspend fun restoreLatest(): Result<Unit>
}
