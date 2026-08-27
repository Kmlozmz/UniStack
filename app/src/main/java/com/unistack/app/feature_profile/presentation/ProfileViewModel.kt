package com.unistack.app.feature_profile.presentation

import android.content.Context
import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.utils.GradingScaleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.BuildConfig
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.BackgroundStyle
import com.unistack.app.feature_user.domain.CustomThemeBase
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_billing.domain.BillingRepository
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_sync.domain.LocalBackupPreview
import com.unistack.app.feature_sync.domain.LocalBackupRepository
import com.unistack.app.feature_user.domain.AccountAuthService
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.VisualPreset
import com.unistack.app.feature_user.domain.VisualPreference
import com.unistack.app.core.utils.GradeCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileActionState(
    val isAccountBusy: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)

/** Lo que se perdería al cambiar de escala de notas. */
data class GradingScaleChangeImpact(
    val gradeCount: Int,
    val subjectCount: Int
) {
    /** Sin notas registradas no hay nada que advertir: el cambio es inofensivo. */
    val isDestructive: Boolean get() = gradeCount > 0

    fun describe(): String {
        val notas = if (gradeCount == 1) "1 nota" else "$gradeCount notas"
        val materias = if (subjectCount == 1) "1 materia" else "$subjectCount materias"
        return "$notas en $materias"
    }
}

/**
 * Cómo va el semestre, para la portada del perfil.
 *
 * El perfil enseñaba el nombre, la carrera y la meta, y nada de si esa meta se está
 * cumpliendo. Los tres datos salen de lo que ya hay registrado; ninguno se inventa cuando no
 * hay notas: [average] en nulo significa «todavía no hay nada evaluado», no un cero.
 */
data class AcademicSnapshot(
    val subjectCount: Int = 0,
    val average: Double? = null,
    val atRisk: Int = 0,
    val passing: Int = 0,
    val gradeCount: Int = 0
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val gradesRepository: GradesRepository,
    private val billingRepository: BillingRepository,
    private val accountAuthService: AccountAuthService,
    private val localBackupRepository: LocalBackupRepository,
    private val cloudBackupRepository: CloudBackupRepository
) : ViewModel() {
    val profile: StateFlow<UserProfile?> = userRepository.userProfile
    val currentUser = userRepository.currentUser
    val billingState = billingRepository.state
    val cloudBackupState = cloudBackupRepository.state

    val academicSnapshot: StateFlow<AcademicSnapshot> = combine(
        gradesRepository.subjects,
        userRepository.userProfile
    ) { subjects, profile ->
        val periods = profile?.academicPeriodScheme?.periods.orEmpty()
        val passing = profile?.passingGrade
        val averages = subjects.map { subject ->
            GradeCalculator.calculateCurrentAverageByPeriods(subject.grades, periods)
        }
        val evaluated = averages.filterNotNull()
        AcademicSnapshot(
            subjectCount = subjects.size,
            average = evaluated.takeIf { it.isNotEmpty() }?.let { list ->
                Math.round(list.average() * 10.0) / 10.0
            },
            atRisk = if (passing == null) 0 else evaluated.count { it < passing },
            passing = if (passing == null) 0 else evaluated.count { it >= passing },
            gradeCount = subjects.sumOf { it.grades.size }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AcademicSnapshot())

    private val _actionState = MutableStateFlow(ProfileActionState())
    val actionState: StateFlow<ProfileActionState> = _actionState

    fun currentPlan() = FeatureGate.planFor(billingState.value.isPro)

    fun refreshBilling() {
        billingRepository.refreshPurchases()
    }

    /**
     * Guarda el retrato elegido, o lo quita.
     *
     * Recibe una ruta a un archivo que ya está dentro de la app: la copia la hace la pantalla,
     * porque el `content://` que devuelve el selector de fotos caduca cuando el proceso muere y
     * el retrato tiene que seguir ahí mañana.
     */
    fun updateLocalPhoto(path: String?) {
        val current = profile.value ?: return
        save(current.copy(localPhotoUri = path))
    }

    fun updatePreferredName(name: String): Boolean {
        val current = profile.value ?: return false
        if (!TextValidators.validateDisplayName(name).isValid) return false
        save(current.copy(preferredName = TextValidators.normalizeText(name)))
        return true
    }

    /**
     * Cuántas notas y en cuántas materias se perderían al cambiar de escala.
     *
     * Una nota es un registro de lo que puso un profesor, no una medida que se pueda
     * reexpresar: convertir 85 sobre 100 en 4.3 sobre 5 inventa un número que nadie dio,
     * y como se guarda con un decimal, ida y vuelta ya no devuelve 85. Por eso el cambio
     * de escala borra en vez de convertir, y por eso hay que decir cuánto se borra.
     */
    fun gradingScaleChangeImpact(): GradingScaleChangeImpact {
        val subjects = gradesRepository.subjects.value
        val affected = subjects.filter { it.grades.isNotEmpty() }
        return GradingScaleChangeImpact(
            gradeCount = affected.sumOf { it.grades.size },
            subjectCount = affected.size
        )
    }

    /**
     * Borra las notas de todas las materias y devuelve sus metas al valor del perfil.
     *
     * Reajustar la meta es tan necesario como borrar: `targetAverage` vive en cada materia
     * y también está expresada en la escala vieja. Si solo se vaciaran las notas, una
     * materia con meta 4.0 quedaría pidiendo un 4 sobre 100.
     */
    private fun wipeGradesForScaleChange(newTargetAverage: Double) {
        gradesRepository.subjects.value.forEach { subject ->
            // Las notas se borran con clearGrades, no pasando un Subject con la lista
            // vacía: updateSubject solo escribe los campos de la materia y las notas
            // viven en su propia tabla, así que copy(grades = emptyList()) no borraba nada.
            if (subject.grades.isNotEmpty()) {
                gradesRepository.clearGrades(subject.id)
            }
            if (subject.targetAverage != newTargetAverage || subject.unknownPeriodIds.isNotEmpty()) {
                gradesRepository.updateSubject(
                    subject.copy(
                        targetAverage = newTargetAverage,
                        unknownPeriodIds = emptySet()
                    )
                )
            }
        }
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

        // Solo se borra si la escala cambia de verdad. Ajustar la mínima o la meta sin
        // tocar la escala deja las notas donde están: siguen midiendo lo mismo.
        if (gradingScale != current.gradingScale) {
            wipeGradesForScaleChange(newTargetAverage = targetAverage)
        }

        save(
            current.copy(
                gradingScale = gradingScale,
                passingGrade = passingGrade,
                targetAverage = targetAverage
            )
        )
        return true
    }

    fun updateAcademicPeriodSettings(weightInputs: List<String>): Boolean {
        val current = profile.value ?: return false
        val weights = weightInputs.map { it.toDoubleOrNull()?.div(100.0) ?: return false }
        if (weights.isEmpty() || weights.any { it <= 0.0 }) return false
        if (kotlin.math.abs(weights.sum() - 1.0) > 0.0001) return false
        val scheme = AcademicPeriodScheme(
            periods = weights.mapIndexed { index, weight ->
                val order = index + 1
                AcademicPeriod(
                    id = "period-$order",
                    name = "${Corte.Singular} $order",
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
        val currentAppearance = current.appearancePreferences
        val appearance = when (preference) {
            VisualPreference.SYSTEM, VisualPreference.LIGHT, VisualPreference.DARK ->
                currentAppearance.copy(
                    backgroundStyle = BackgroundStyle.DEFAULT,
                    visualPreset = VisualPreset.CUSTOM
                )
            VisualPreference.OLED -> currentAppearance.copy(
                backgroundStyle = BackgroundStyle.PURE,
                visualPreset = VisualPreset.CUSTOM
            )
            VisualPreference.CUSTOM -> currentAppearance.copy(
                customThemeBase = when (current.visualPreference) {
                    VisualPreference.LIGHT -> CustomThemeBase.LIGHT
                    VisualPreference.DARK, VisualPreference.OLED -> CustomThemeBase.DARK
                    VisualPreference.SYSTEM, VisualPreference.CUSTOM -> currentAppearance.customThemeBase
                },
                visualPreset = VisualPreset.CUSTOM
            )
        }
        save(current.copy(visualPreference = preference, appearancePreferences = appearance))
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

    /** Enciende o apaga el resumen de la manana, y a que hora sale. */
    fun updateDailyDigest(enabled: Boolean, hour: Int, minute: Int) {
        val current = profile.value ?: return
        save(
            current.copy(
                dailyDigestEnabled = enabled,
                dailyDigestHour = hour.coerceIn(0, 23),
                dailyDigestMinute = minute.coerceIn(0, 59)
            )
        )
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

    /** Si la copia en la nube está disponible en esta compilación. */
    val cloudAvailable: Boolean get() = cloudBackupRepository.isConfigured

    /**
     * Si vincular una cuenta puede llegar a funcionar en esta compilación.
     *
     * Son dos condiciones y no una: el proyecto de Firebase —que es lo que mira
     * [cloudAvailable]— y el identificador de cliente web, que es lo que pide Credential
     * Manager para iniciar sesión. Con el proyecto puesto y el identificador vacío el botón se
     * vería encendido y seguiría fallando al pulsarlo, que es exactamente lo que hacía antes.
     */
    val accountLinkAvailable: Boolean
        get() = cloudAvailable && BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()

    fun exportLocalBackup(): String = localBackupRepository.exportBackupJson()

    /** El PDF del reporte, ya escrito, listo para compartir. Nulo si no se pudo crear. */
    fun academicPdfFile(context: Context): File? =
        localBackupRepository.exportAcademicPdf(context)
            .map { path -> File(path) }
            .onFailure { throwable ->
                _actionState.update { it.copy(message = null, errorMessage = throwable.message ?: "No se pudo crear el PDF.") }
            }
            .getOrNull()

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

    /** Lo que trae un archivo, campo por campo. Nulo si no es una copia válida. */
    fun inspectLocalBackup(json: String): LocalBackupPreview? =
        localBackupRepository.previewBackupJson(json).getOrNull()

    /** Lo que hay ahora mismo en la app, para poder comparar antes de reemplazarlo. */
    fun currentContents(): LocalBackupPreview? = inspectLocalBackup(exportLocalBackup())

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
