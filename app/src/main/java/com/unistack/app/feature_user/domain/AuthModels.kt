package com.unistack.app.feature_user.domain

enum class AuthProvider {
    LOCAL,
    GOOGLE
}

enum class SyncStatus {
    LOCAL_ONLY,
    READY_FOR_BACKUP,
    SYNC_PENDING,
    SYNCED,
    SYNC_ERROR
}

data class LinkedAccount(
    val provider: AuthProvider,
    val providerUserId: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?
)
