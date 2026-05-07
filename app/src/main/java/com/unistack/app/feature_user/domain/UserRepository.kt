package com.unistack.app.feature_user.domain

import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUser: StateFlow<AppUser>
    val userProfile: StateFlow<UserProfile?>

    /** True once the first value has been emitted from the persistent source. */
    val didLoad: Boolean

    fun saveUserProfile(profile: UserProfile)
    fun updatePreferredName(name: String)
    fun markSetupCompleted()
    fun linkAccount(account: LinkedAccount)
    fun unlinkAccount()
}
