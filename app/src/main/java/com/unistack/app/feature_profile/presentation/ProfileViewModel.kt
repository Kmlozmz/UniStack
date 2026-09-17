package com.unistack.app.feature_profile.presentation

import android.content.Context
import android.net.Uri
import java.io.File
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.utils.GradingScaleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.BuildConfig
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.BackgroundStyle
import com.unistack.app.feature_user.domain.CustomThemeBase
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicBreakRepository
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_terms.domain.AcademicTermType
import java.time.LocalDate
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_sync.domain.LocalBackupPreview
import com.unistack.app.feature_sync.domain.LocalBackupRepository
import com.unistack.app.feature_user.domain.AccountAuthService
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.VisualPreset
import com.unistack.app.feature_user.domain.VisualPreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

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
        val notas = if (gradeCount == 1) Textos.get(R.string.count_grade_one) else Textos.get(R.string.count_grade_many, gradeCount)
        val materias = if (subjectCount == 1) Textos.get(R.string.count_subject_one) else Textos.get(R.string.count_subject_many, subjectCount)
        return Textos.get(R.string.profile_en, notas, materias)
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val gradesRepository: GradesRepository,
    private val accountAuthService: AccountAuthService,
    private val localBackupRepository: LocalBackupRepository,
    private val cloudBackupRepository: CloudBackupRepository,
    private val breakRepository: AcademicBreakRepository,
    private val termRepository: AcademicTermRepository
) : ViewModel() {
    /**
     * El periodo que se está cursando, o nulo entre uno y otro.
     *
     * Aquí sirve para una sola cosa: dibujar los tramos de los cortes. El primero empieza
     * cuando empieza el periodo y el último acaba con él, así que sin esto las dos fechas de
     * los extremos saldrían vacías. Nulo no bloquea nada: las fechas de corte se pueden poner
     * igual, solo que sin los extremos escritos.
     */
    val activeTerm: StateFlow<AcademicTerm?> = termRepository.activeTerm

    /**
     * Los días en que la universidad estuvo cerrada.
     *
     * Se editan aquí y no en el horario porque no son de una materia: un festivo lo es para
     * todas, igual que la escala de notas o el reparto de los cortes.
     */
    val academicBreaks: StateFlow<List<AcademicBreak>> = breakRepository.breaks

    fun saveAcademicBreak(id: String?, name: String, start: LocalDate, end: LocalDate) {
        viewModelScope.launch { breakRepository.save(id, name, start, end) }
    }

    fun deleteAcademicBreak(breakId: String) {
        viewModelScope.launch { breakRepository.delete(breakId) }
    }

    val profile: StateFlow<UserProfile?> = userRepository.userProfile
    val currentUser = userRepository.currentUser
    val cloudBackupState = cloudBackupRepository.state

    private val _actionState = MutableStateFlow(ProfileActionState())
    val actionState: StateFlow<ProfileActionState> = _actionState

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
     * Cambia el área de estudio.
     *
     * La carrera solo sobrevive si el área no cambió de verdad: viene de un catálogo distinto
     * por área, así que una carrera de Ingeniería no tiene sentido bajo Salud. El nombre
     * escrito a mano para «Otra» se conserva igual —por si se vuelve a elegir— y se limpia
     * en cuanto el área deja de ser «Otra».
     */
    fun updateStudyArea(area: StudyArea): Boolean {
        val current = profile.value ?: return false
        val keepProgram = current.studyArea == area
        save(
            current.copy(
                studyArea = area,
                careerOrProgram = if (keepProgram) current.careerOrProgram else null,
                customStudyArea = if (area == StudyArea.OTHER) current.customStudyArea else null
            )
        )
        return true
    }

    fun updateCustomStudyArea(text: String): Boolean {
        val current = profile.value ?: return false
        save(current.copy(customStudyArea = text))
        return true
    }

    /** Sirve igual para una carrera del catálogo que para una escrita a mano. */
    fun updateCareerOrProgram(text: String): Boolean {
        val current = profile.value ?: return false
        save(current.copy(careerOrProgram = text))
        return true
    }

    fun updateInstitutionName(text: String): Boolean {
        val current = profile.value ?: return false
        save(current.copy(institutionName = text))
        return true
    }

    /**
     * En qué semestre vas, de cuántos.
     *
     * Van juntos porque uno solo no dice nada: «semestre 5» sin el total no dibuja el camino,
     * y el total sin el actual no dice dónde estás parado.
     */
    fun updateSemesterProgress(currentSemester: Int, totalSemesters: Int): Boolean {
        val current = profile.value ?: return false
        if (totalSemesters !in 1..30) return false
        if (currentSemester !in 1..totalSemesters) return false
        save(current.copy(currentSemester = currentSemester, totalSemesters = totalSemesters))
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
            if (subject.targetAverage != newTargetAverage || subject.unknownCutIds.isNotEmpty()) {
                gradesRepository.updateSubject(
                    subject.copy(
                        targetAverage = newTargetAverage,
                        unknownCutIds = emptySet()
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

    /**
     * Guarda el reparto de los cortes **y sus fechas**.
     *
     * Antes esto solo recibía los pesos y construía los cortes de cero, así que cada vez que
     * alguien entraba aquí y tocaba «Guardar cortes» se llevaba por delante las fechas puestas
     * en el onboarding sin decir nada. No era que no se pudieran editar: es que se borraban.
     *
     * [cutEndDates] va en paralelo a [weightInputs] y solo cuentan las posiciones anteriores a
     * la última: el último corte acaba con el periodo y por eso nunca lleva fecha propia.
     */
    fun updateGradingCutSettings(
        weightInputs: List<String>,
        cutEndDates: List<LocalDate?> = emptyList()
    ): Boolean {
        val current = profile.value ?: return false
        val weights = weightInputs.map { it.toDoubleOrNull()?.div(100.0) ?: return false }
        if (weights.isEmpty() || weights.any { it <= 0.0 }) return false
        if (kotlin.math.abs(weights.sum() - 1.0) > 0.0001) return false
        val scheme = GradingCutScheme(
            cuts = weights.mapIndexed { index, weight ->
                val order = index + 1
                GradingCut(
                    id = "period-$order",
                    name = "${Corte.Singular} $order",
                    weight = weight,
                    order = order,
                    // El ultimo acaba con el periodo, asi que su fecha no existe.
                    endEpochDay = if (index == weights.lastIndex) {
                        null
                    } else {
                        cutEndDates.getOrNull(index)?.toEpochDay()
                    }
                )
            }
        )
        // Fechas que no suben, o puestas a medias, no se guardan: el esquema entero deja de
        // poder decidir el corte de una nota y es peor que no tener fechas.
        if (!scheme.isValid) return false
        save(current.copy(gradingCutScheme = scheme))
        return true
    }

    /**
     * Corrige el periodo activo: nombre, forma, o sus dos fechas.
     *
     * El onboarding las pregunta una vez y `NewTermScreen` promete que «se cambia en Ajustes
     * › Configuración académica, cuando quieras» — una promesa que hasta ahora no tenía dónde
     * cumplirse: el repositorio ya sabía actualizar un periodo, solo faltaba quien lo llamara
     * desde aquí. Cerrar el periodo sigue siendo la única vía para cambiar sus fechas *reales*
     * de cierre; esto solo toca lo que se puso al abrirlo.
     */
    fun updateActiveTerm(
        name: String,
        type: AcademicTermType,
        start: LocalDate,
        plannedEnd: LocalDate?
    ): Boolean {
        val term = activeTerm.value ?: return false
        if (name.isBlank()) return false
        if (plannedEnd != null && !plannedEnd.isAfter(start)) return false
        viewModelScope.launch {
            termRepository.update(
                term.copy(
                    name = name.trim(),
                    type = type,
                    startEpochDay = start.toEpochDay(),
                    plannedEndEpochDay = plannedEnd?.toEpochDay(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return true
    }

    /**
     * Cuántas faltas admite tu reglamento, o nulo para quitar el tope.
     *
     * Uno para todas las materias: el número sale del reglamento de la universidad, no de la
     * asignatura. Estuvo colgando de cada materia y obligaba a escribir siete veces lo mismo.
     */
    fun setAbsenceLimit(limit: Int?): Boolean {
        val current = profile.value ?: return false
        save(current.copy(absenceLimit = limit?.coerceIn(1, 40)))
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
                            message = Textos.get(R.string.profile_cuenta_de_google_conectada),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(
                            isAccountBusy = false,
                            message = null,
                            errorMessage = throwable.message ?: Textos.get(R.string.profile_no_se_pudo_conectar_con_google)
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
                        message = Textos.get(R.string.profile_cuenta_desvinculada),
                        errorMessage = null
                    )
                }
            } else {
                _actionState.update {
                    it.copy(
                        isAccountBusy = false,
                        message = null,
                        errorMessage = Textos.get(R.string.profile_no_hay_cuenta_vinculada)
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
                _actionState.update {
                    it.copy(
                        message = null,
                        errorMessage = throwable.message ?: Textos.get(R.string.profile_no_se_pudo_crear_el_pdf)
                    )
                }
            }
            .getOrNull()

    fun exportAcademicReport(): String = localBackupRepository.exportAcademicReport()

    fun exportAcademicPdf(context: Context): Boolean {
        return localBackupRepository.exportAcademicPdf(context)
            .onSuccess { path ->
                _actionState.update {
                    it.copy(
                        message = Textos.get(R.string.profile_pdf_academico_creado, path),
                        errorMessage = null
                    )
                }
            }
            .onFailure { throwable ->
                _actionState.update {
                    it.copy(
                        message = null,
                        errorMessage = throwable.message ?: (Textos.get(R.string.profile_no_se_pudo_crear_el_pdf))
                    )
                }
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
                onFailure = {
                    it.message ?: Textos.get(R.string.profile_backup_invalido)
                }
            )
    }

    /**
     * Restaura la copia elegida: primero los archivos que traiga dentro, luego los datos.
     *
     * Los archivos van antes para que, cuando aparezcan las notas, sus fotos ya estén en su
     * sitio. Todo ocurre fuera del hilo de la pantalla y el resultado llega por [onDone]: la
     * copia restaurada, o nulo si no se pudo.
     */
    fun restoreLocalBackup(
        context: Context,
        archivo: Uri,
        json: String,
        onDone: (LocalBackupPreview?) -> Unit
    ) {
        val app = context.applicationContext
        viewModelScope.launch {
            val resultado = withContext(Dispatchers.IO) {
                val retrato = BackupArchive.restoreFiles(app, archivo, json)
                localBackupRepository.restoreBackupJson(json, localPhotoUri = retrato)
            }
            resultado
                .onSuccess { preview ->
                    _actionState.update {
                        it.copy(
                            message = Textos.get(R.string.profile_backup_local_restaurado, preview.summary()),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(
                            message = null,
                            errorMessage = throwable.message ?: (Textos.get(R.string.profile_no_se_pudo_restaurar_el_backup))
                        )
                    }
                }
            onDone(resultado.getOrNull())
        }
    }

    private fun save(profile: UserProfile) {
        userRepository.saveUserProfile(
            profile.copy(updatedAt = System.currentTimeMillis())
        )
    }
}
