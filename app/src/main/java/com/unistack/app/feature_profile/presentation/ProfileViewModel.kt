package com.unistack.app.feature_profile.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.AppContainer
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.AcademicPeriodLabel
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.VisualPreset
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
    private val localBackupRepository = AppContainer.localBackupRepository
    private val cloudBackupRepository = AppContainer.cloudBackupRepository
    val cloudBackupState = cloudBackupRepository.state

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
        val maxGrade = if (gradingScale == GradingScale.CUSTOM) {
            current.customGradeMax
        } else {
            GradingScaleUtils.maxGradeFor(gradingScale)
        }
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

    fun updateAcademicPeriodSettings(
        label: AcademicPeriodLabel,
        weightInputs: List<String>
    ): Boolean {
        val current = profile.value ?: return false
        val weights = weightInputs.map { it.toDoubleOrNull()?.div(100.0) ?: return false }
        if (weights.isEmpty() || weights.any { it <= 0.0 }) return false
        if (kotlin.math.abs(weights.sum() - 1.0) > 0.0001) return false
        val scheme = AcademicPeriodScheme(
            label = label,
            periods = weights.mapIndexed { index, weight ->
                val order = index + 1
                AcademicPeriod(
                    id = "period-$order",
                    name = "${label.singular} $order",
                    weight = weight,
                    order = order
                )
            }
        )
        save(current.copy(academicPeriodScheme = scheme))
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

    fun updateAppearance(transform: (AppearancePreferences) -> AppearancePreferences): Boolean {
        val current = profile.value ?: return false
        val updated = transform(current.appearancePreferences)
            .normalized()
            .copy(visualPreset = VisualPreset.CUSTOM)
        save(current.copy(appearancePreferences = updated))
        return true
    }

    fun updateAccessibility(transform: (AccessibilityPreferences) -> AccessibilityPreferences): Boolean {
        val current = profile.value ?: return false
        save(current.copy(accessibilityPreferences = transform(current.accessibilityPreferences)))
        return true
    }

    fun applyVisualPreset(preset: VisualPreset): Boolean {
        val current = profile.value ?: return false
        val appearance = AppearancePreferences.preset(preset)
        val visualPreference = when (preset) {
            VisualPreset.OLED -> VisualPreference.OLED
            VisualPreset.DEFAULT -> VisualPreference.SYSTEM
            else -> current.visualPreference.takeUnless { it == VisualPreference.OLED } ?: VisualPreference.DARK
        }
        save(
            current.copy(
                visualPreference = visualPreference,
                appearancePreferences = appearance
            )
        )
        return true
    }

    fun resetAppearance(): Boolean {
        val current = profile.value ?: return false
        save(
            current.copy(
                visualPreference = VisualPreference.SYSTEM,
                appearancePreferences = AppearancePreferences.defaults()
            )
        )
        return true
    }

    fun updateReminderSettings(
        taskRemindersEnabled: Boolean,
        academicWorkRemindersEnabled: Boolean,
        overdueRemindersEnabled: Boolean,
        reminderLeadHours: Int
    ): Boolean {
        val current = profile.value ?: return false
        if (reminderLeadHours !in 1..168) return false
        save(
            current.copy(
                taskRemindersEnabled = taskRemindersEnabled,
                academicWorkRemindersEnabled = academicWorkRemindersEnabled,
                overdueRemindersEnabled = overdueRemindersEnabled,
                reminderLeadHours = reminderLeadHours
            )
        )
        return true
    }

    fun updateAcademicReminderSettings(
        gradeInsightRemindersEnabled: Boolean,
        pendingGradeRemindersEnabled: Boolean
    ): Boolean {
        val current = profile.value ?: return false
        save(
            current.copy(
                gradeInsightRemindersEnabled = gradeInsightRemindersEnabled,
                pendingGradeRemindersEnabled = pendingGradeRemindersEnabled
            )
        )
        return true
    }

    fun updateQuietHours(
        enabled: Boolean,
        startHour: Int?,
        endHour: Int?
    ): Boolean {
        val current = profile.value ?: return false
        if (startHour != null && startHour !in 0..23) return false
        if (endHour != null && endHour !in 0..23) return false
        if (enabled && (startHour == null || endHour == null || startHour == endHour)) return false
        save(
            current.copy(
                quietHoursEnabled = enabled,
                quietHoursStartHour = startHour,
                quietHoursEndHour = endHour
            )
        )
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

    fun backupToCloud() {
        viewModelScope.launch {
            cloudBackupRepository.backupNow()
        }
    }

    fun restoreFromCloud() {
        viewModelScope.launch {
            cloudBackupRepository.restoreLatest()
        }
    }

    fun exportLocalBackup(): String = localBackupRepository.exportBackupJson()

    fun exportAcademicReport(): String = localBackupRepository.exportAcademicReport()

    fun exportAcademicPdf(context: Context): Boolean {
        return localBackupRepository.exportAcademicPdf(context)
            .onSuccess { path ->
                _actionState.update { it.copy(message = "PDF académico creado: $path", errorMessage = null) }
            }
            .onFailure { throwable ->
                _actionState.update { it.copy(message = null, errorMessage = throwable.message ?: "No se pudo crear el PDF.") }
            }
            .isSuccess
    }

    fun exportTasksCsv(): String = localBackupRepository.exportTasksCsv()

    fun exportExpensesCsv(): String = localBackupRepository.exportExpensesCsv()

    fun localDataSummary(): String = previewLocalBackup(exportLocalBackup())

    fun previewLocalBackup(json: String): String {
        return localBackupRepository.previewBackupJson(json)
            .fold(
                onSuccess = { it.summary() },
                onFailure = { it.message ?: "Backup inválido." }
            )
    }

    fun restoreLocalBackup(json: String): Boolean {
        return localBackupRepository.restoreBackupJson(json)
            .onSuccess { preview ->
                _actionState.update {
                    it.copy(message = "Backup local restaurado: ${preview.summary()}", errorMessage = null)
                }
            }
            .onFailure { throwable ->
                _actionState.update {
                    it.copy(message = null, errorMessage = throwable.message ?: "No se pudo restaurar el backup.")
                }
            }
            .isSuccess
    }

    private fun save(profile: UserProfile) {
        userRepository.saveUserProfile(
            profile.copy(updatedAt = System.currentTimeMillis())
        )
    }
}
