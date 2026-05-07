package com.unistack.app.feature_user.domain

data class AppUser(
    val userId: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val authProvider: AuthProvider = AuthProvider.LOCAL,
    val providerUserId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) {
    val isLinked: Boolean
        get() = authProvider != AuthProvider.LOCAL && providerUserId != null
}
