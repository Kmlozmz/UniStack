package com.unistack.app.feature_profile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.AppContainer
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.VisualPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileActionState(
    val isAccountBusy: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

class ProfileViewModel(
    private val userRepository: UserRepository = AppContainer.userRepository
) : ViewModel() {
    val profile: StateFlow<UserProfile?> = userRepository.userProfile
    val currentUser = userRepository.currentUser
    val billingState = AppContainer.billingRepository.state
    private val accountAuthService = AppContainer.accountAuthService
    private val billingRepository = AppContainer.billingRepository

    private val _actionState = MutableStateFlow(ProfileActionState())
    val actionState: StateFlow<ProfileActionState> = _actionState

    fun currentPlan() = FeatureGate.planFor(billingState.value.isPro)

    fun refreshBilling() {
        billingRepository.refreshPurchases()
    }

    fun updatePreferredName(name: String): Boolean {
        val current = profile.value ?: return false
        if (!TextValidators.validateDisplayName(name).isValid) return false
        save(current.copy(preferredName = TextValidators.normalizeText(name)))
        return true
    }

    fun updateGradingSettings(
        gradingScale: GradingScale,
        passingGradeInput: String,
        targetAverageInput: String
    ): Boolean {
        val current = profile.value ?: return false
        val maxGrade = GradingScaleUtils.maxGradeFor(gradingScale)
        val passingGrade = passingGradeInput.toDoubleOrNull() ?: return false
        val targetAverage = targetAverageInput.toDoubleOrNull() ?: return false

        if (passingGrade !in 0.0..maxGrade) return false
        if (targetAverage !in 0.0..maxGrade) return false
        if (targetAverage < passingGrade) return false

        save(
            current.copy(
                gradingScale = gradingScale,
                passingGrade = passingGrade,
                targetAverage = targetAverage
            )
        )
        return true
    }

    fun toggleModule(module: AppModule): Boolean {
        val current = profile.value ?: return false
        val nextModules = if (module in current.enabledModules) {
            current.enabledModules - module
        } else {
            current.enabledModules + module
        }
        if (nextModules.isEmpty()) return false
        save(current.copy(enabledModules = nextModules))
        return true
    }

    fun updateVisualPreference(preference: VisualPreference): Boolean {
        val current = profile.value ?: return false
        save(current.copy(visualPreference = preference))
        return true
    }

    fun restartOnboarding(): Boolean {
        val current = profile.value ?: return false
        save(current.copy(setupCompleted = false))
        return true
    }

    fun connectGoogle(context: Context) {
        if (_actionState.value.isAccountBusy) return
        viewModelScope.launch {
            _actionState.update { it.copy(isAccountBusy = true, message = null, errorMessage = null) }
            accountAuthService.signInWithGoogle(context)
                .onSuccess { account ->
                    userRepository.linkAccount(account)
                    _actionState.update {
                        it.copy(
                            isAccountBusy = false,
                            message = "Cuenta de Google conectada.",
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(
                            isAccountBusy = false,
                            message = null,
                            errorMessage = throwable.message ?: "No se pudo conectar con Google."
                        )
                    }
                }
        }
    }

    fun unlinkAccount() {
        if (_actionState.value.isAccountBusy) return
        viewModelScope.launch {
            _actionState.update { it.copy(isAccountBusy = true, message = null, errorMessage = null) }
            accountAuthService.signOut()
            if (currentUser.value.isLinked) {
                userRepository.unlinkAccount()
                _actionState.update {
                    it.copy(
                        isAccountBusy = false,
                        message = "Cuenta desvinculada.",
                        errorMessage = null
                    )
                }
            } else {
                _actionState.update {
                    it.copy(
                        isAccountBusy = false,
                        message = null,
                        errorMessage = "No hay cuenta vinculada."
                    )
                }
            }
        }
    }

    private fun save(profile: UserProfile) {
        userRepository.saveUserProfile(
            profile.copy(updatedAt = System.currentTimeMillis())
        )
    }
}
