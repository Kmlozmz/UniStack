package com.unistack.app.feature_user.data

import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryUserRepository : UserRepository {
    override val didLoad: Boolean = true

    private val anonymousUser = AppUser(
        userId = "local-user",
        displayName = null,
        email = null,
        photoUrl = null
    )

    private val _currentUser = MutableStateFlow(anonymousUser)
    override val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    override val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    override fun saveUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        _currentUser.value = _currentUser.value.copy(
            userId = profile.userId,
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
}
