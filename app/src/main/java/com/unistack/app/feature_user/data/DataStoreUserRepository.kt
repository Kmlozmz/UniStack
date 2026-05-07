package com.unistack.app.feature_user.data

import com.unistack.app.core.datastore.UserPreferencesDataSource
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.LinkedAccount
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DataStoreUserRepository(
    private val dataSource: UserPreferencesDataSource
) : UserRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    override var didLoad: Boolean = false
        private set

    override val userProfile: StateFlow<UserProfile?> = dataSource.userProfileFlow
        .onEach { didLoad = true }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    override val currentUser: StateFlow<AppUser> = dataSource.currentUserFlow.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AppUser(
            userId = "local_user",
            displayName = null,
            email = null,
            photoUrl = null
        )
    )

    override fun saveUserProfile(profile: UserProfile) {
        scope.launch { dataSource.saveUserProfile(profile) }
    }

    override fun updatePreferredName(name: String) {
        scope.launch { dataSource.updatePreferredName(name) }
    }

    override fun markSetupCompleted() {
        scope.launch { dataSource.markSetupCompleted() }
    }

    override fun linkAccount(account: LinkedAccount) {
        scope.launch { dataSource.linkAccount(account) }
    }

    override fun unlinkAccount() {
        scope.launch { dataSource.unlinkAccount() }
    }
}
