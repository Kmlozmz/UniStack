package com.unistack.app.feature_user.data

import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.LinkedAccount
import com.unistack.app.feature_user.domain.SyncStatus
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.UserIds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryUserRepository : UserRepository {
    override val didLoad: Boolean = true

    private val anonymousUser = AppUser(
        userId = UserIds.LOCAL,
        displayName = null,
        email = null,
        photoUrl = null
    )

    private val _currentUser = MutableStateFlow(anonymousUser)
    override val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    override fun saveUserProfile(profile: UserProfile) {
        _userProfile.value = profile.copy(userId = UserIds.normalize(profile.userId))
        _currentUser.value = _currentUser.value.copy(
            userId = UserIds.normalize(profile.userId),
            displayName = profile.preferredName
        )
    }

    override fun updatePreferredName(name: String) {
        val now = System.currentTimeMillis()
        _userProfile.update { profile ->
            profile?.copy(preferredName = name.trim(), updatedAt = now)
        }
        _currentUser.update { user -> user.copy(displayName = name.trim()) }
    }

    override fun markSetupCompleted() {
        val now = System.currentTimeMillis()
        _userProfile.update { profile ->
            profile?.copy(setupCompleted = true, updatedAt = now)
        }
    }

    override fun linkAccount(account: LinkedAccount) {
        val now = System.currentTimeMillis()
        _userProfile.update { profile ->
            profile?.copy(
                preferredName = account.displayName?.takeIf { it.isNotBlank() } ?: profile.preferredName,
                accountProvider = account.provider,
                accountProviderUserId = account.providerUserId,
                accountEmail = account.email,
                accountPhotoUrl = account.photoUrl,
                syncStatus = SyncStatus.READY_FOR_BACKUP,
                updatedAt = now
            )
        }
        _currentUser.update { user ->
            user.copy(
                displayName = account.displayName?.takeIf { it.isNotBlank() } ?: user.displayName,
                email = account.email,
                photoUrl = account.photoUrl,
                authProvider = account.provider,
                providerUserId = account.providerUserId,
                syncStatus = SyncStatus.READY_FOR_BACKUP
            )
        }
    }

    override fun unlinkAccount() {
        val now = System.currentTimeMillis()
        _userProfile.update { profile ->
            profile?.copy(
                accountProvider = AuthProvider.LOCAL,
                accountProviderUserId = null,
                accountEmail = null,
                accountPhotoUrl = null,
                syncStatus = SyncStatus.LOCAL_ONLY,
                lastSyncAt = null,
                updatedAt = now
            )
        }
        _currentUser.update { user ->
            user.copy(
                email = null,
                photoUrl = null,
                authProvider = AuthProvider.LOCAL,
                providerUserId = null,
                syncStatus = SyncStatus.LOCAL_ONLY
            )
        }
    }
}
