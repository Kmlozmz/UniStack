package com.unistack.app.feature_setup.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.unistack.app.core.utils.TextValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.AcademicPeriodLabel
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.VisualPreference
import com.unistack.app.core.utils.ValidationResult

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    var preferredName by mutableStateOf("")
        private set
    /**
     * Sin elegir hasta que el usuario elige. Antes venía preseleccionado a universidad, lo
     * que además de decidir por él dejaba pasar el paso sin haberlo mirado.
     */
    var educationLevel by mutableStateOf<EducationLevel?>(null)
        private set
    var academicInfo by mutableStateOf("")
        private set
    var studyArea by mutableStateOf<StudyArea?>(null)
        private set
    var selectedProgram by mutableStateOf<String?>(null)
        private set
    var customProgram by mutableStateOf("")
        private set
    var gradingScale by mutableStateOf(GradingScale.ZERO_TO_FIVE)
        private set
    var customGradeMax by mutableDoubleStateOf(100.0)
        private set
    var customGradeRangeConfirmed by mutableStateOf(false)
        private set
    var passingGradeText by mutableStateOf("3.0")
        private set
    var targetAverageText by mutableStateOf("4.0")
        private set
    /** Texto crudo de la institución, tal y como lo escribe el usuario. Opcional. */
    var institutionName by mutableStateOf("")
        private set

    var enabledModules by mutableStateOf(AppModule.entries.toSet())
        private set
    var academicPeriodLabel by mutableStateOf(AcademicPeriodLabel.CORTE)
        private set
    var academicPeriodWeights by mutableStateOf(emptyList<String>())
        private set

    val nameValidation: ValidationResult
        get() = TextValidators.validateDisplayName(preferredName)

    val customProgramValidation: ValidationResult
        get() = TextValidators.validateCustomCareer(customProgram)

    val isNameValid: Boolean
        get() = nameValidation.isValid

    /** En primaria y secundaria no se pregunta carrera, sino grado o curso. */
    val isSchoolLevel: Boolean
        get() = educationLevel?.isSchoolLevel() == true

    /** Grados disponibles para el nivel escolar actual. */
    val gradeOptions: List<String>
        get() = educationLevel?.standardGradeOptions().orEmpty()

    /** El grado se guarda en el mismo campo que el resto de información académica. */
    val selectedGrade: String
        get() = academicInfo

    val isAcademicInfoValid: Boolean
        get() {
            val level = educationLevel ?: return false

            if (level == EducationLevel.UNIVERSITY) {
                val area = studyArea ?: return false
                val program = selectedProgram ?: return false
                if (area == StudyArea.OTHER || program == OTHER_OPTION) {
                    return customProgramValidation.isValid
                }
                return true
            }

            if (level.isSchoolLevel()) {
                val value = academicInfo.trim()
                if (value in level.standardGradeOptions()) return true
                return value.isEmpty() || TextValidators.validateCustomCareer(value).isValid
            }

            val value = academicInfo.trim()
            return value.isEmpty() || TextValidators.validateCustomCareer(value).isValid
        }

    /**
     * Elegir nivel es obligatorio; el resto del paso solo lo es en universidad, donde el
     * área y la carrera dan de comer al catálogo de materias.
     */
    val canContinueFromProfile: Boolean
        get() {
            val level = educationLevel ?: return false
            return level != EducationLevel.UNIVERSITY || isAcademicInfoValid
        }

    val isGradesValid: Boolean
        get() {
            if (gradingScale == GradingScale.CUSTOM && !customGradeRangeConfirmed) return false
            val passing = passingGradeText.toDoubleOrNull() ?: return false
            val target = targetAverageText.toDoubleOrNull() ?: return false
            val max = if (gradingScale == GradingScale.CUSTOM) customGradeMax else gradingScale.maxNumericValue
            return passing in 0.0..max &&
                target in 0.0..max &&
                target >= passing
        }

    val isAcademicPeriodsValid: Boolean
        get() = buildAcademicPeriodSchemeOrNull() != null

    fun updatePreferredName(value: String) {
        preferredName = value.take(30)
    }

    /**
     * Alterna el nivel: volver a tocar el ya elegido lo deselecciona. Sin esto, el primer
     * toque era irreversible y no había forma de volver al estado inicial.
     */
    fun updateEducationLevel(value: EducationLevel) {
        educationLevel = if (educationLevel == value) null else value
        academicInfo = ""
        studyArea = null
        selectedProgram = null
        customProgram = ""
        // La institución se conserva: cambiar de nivel por error no debe borrar lo escrito.
    }

    fun updateInstitutionName(value: String) {
        institutionName = value.take(80)
    }

    fun updateGradeLevel(value: String) {
        academicInfo = value
    }

    fun updateAcademicInfo(value: String) {
        academicInfo = value
    }

    fun updateStudyArea(value: StudyArea) {
        studyArea = value
        selectedProgram = null
        customProgram = ""
    }

    fun updateSelectedProgram(value: String) {
        selectedProgram = value
        if (value != OTHER_OPTION) {
            customProgram = ""
        }
    }

    fun updateCustomProgram(value: String) {
        customProgram = value.take(60)
    }

    fun skipAcademicInfo() {
        academicInfo = ""
        studyArea = null
        selectedProgram = null
        customProgram = ""
    }

    fun updateGradingScale(value: GradingScale) {
        gradingScale = value
        customGradeRangeConfirmed = value != GradingScale.CUSTOM
        passingGradeText = value.defaultPassingGradeText
        targetAverageText = value.defaultTargetAverageText
    }

    fun updateCustomGradeMax(value: Double) {
        customGradeMax = value.coerceIn(1.0, 100.0)
        customGradeRangeConfirmed = false
        passingGradeText = customDefaultPassingGradeText()
        targetAverageText = customDefaultTargetAverageText()
    }

    fun confirmCustomGradeRange() {
        customGradeRangeConfirmed = true
        passingGradeText = customDefaultPassingGradeText()
        targetAverageText = customDefaultTargetAverageText()
    }

    fun editCustomGradeRange() {
        customGradeRangeConfirmed = false
    }

    fun updatePassingGrade(value: String) {
        passingGradeText = value
    }

    fun updateTargetAverage(value: String) {
        targetAverageText = value
    }

    fun toggleModule(module: AppModule) {
        enabledModules = if (module in enabledModules) {
            (enabledModules - module).takeIf { it.isNotEmpty() } ?: enabledModules
        } else {
            enabledModules + module
        }
    }

    fun updateAcademicPeriodLabel(label: AcademicPeriodLabel) {
        academicPeriodLabel = label
    }

    fun updateAcademicPeriodCount(count: Int) {
        val safeCount = count.coerceIn(0, 6)
        academicPeriodWeights = suggestedAcademicWeights(safeCount)
    }

    fun updateAcademicPeriodWeight(index: Int, value: String) {
        academicPeriodWeights = academicPeriodWeights.mapIndexed { currentIndex, currentValue ->
            if (currentIndex == index) value.filter { it.isDigit() || it == '.' }.take(5) else currentValue
        }
    }

    fun finishSetup() {
        val now = System.currentTimeMillis()
        val info = academicInfoValue()
        // No se puede pasar del paso de perfil sin elegir nivel, así que aquí siempre hay
        // uno. El repliegue existe solo para no arrastrar nulabilidad hasta el perfil
        // guardado, donde el nivel es obligatorio.
        val level = educationLevel ?: EducationLevel.OTHER
        val profile = UserProfile(
            userId = UserIds.LOCAL,
            preferredName = TextValidators.normalizeText(preferredName),
            educationLevel = level,
            careerOrProgram = if (level.isSchoolLevel()) null else info,
            studyArea = studyArea,
            gradeLevel = if (level.isSchoolLevel()) info else null,
            // Se guarda sin normalizar: conservar el original permite mapearlo a un
            // catálogo canónico más adelante.
            institutionName = institutionName.trim().takeIf { it.isNotEmpty() },
            gradingScale = gradingScale,
            customGradeMax = customGradeMax.coerceIn(1.0, 100.0),
            passingGrade = passingGradeText.toDoubleOrNull() ?: gradingScale.defaultPassingGrade,
            targetAverage = targetAverageText.toDoubleOrNull() ?: gradingScale.defaultTargetAverage,
            enabledModules = enabledModules,
            academicPeriodScheme = buildAcademicPeriodSchemeOrNull() ?: AcademicPeriodScheme.default(),
            visualPreference = VisualPreference.SYSTEM,
            setupCompleted = true,
            createdAt = now,
            updatedAt = now
        )
        userRepository.saveUserProfile(profile)
    }

    private fun academicInfoValue(): String? {
        if (educationLevel == EducationLevel.UNIVERSITY) {
            val area = studyArea ?: return null
            val program = selectedProgram ?: return null
            return if (area == StudyArea.OTHER || program == OTHER_OPTION) {
                TextValidators.normalizeText(customProgram).takeIf { TextValidators.validateCustomCareer(it).isValid }
            } else {
                program
            }
        }
        return TextValidators.normalizeText(academicInfo).takeIf { it.isNotEmpty() }
    }

    private fun buildAcademicPeriodSchemeOrNull(): AcademicPeriodScheme? {
        val weights = academicPeriodWeights.map { it.toDoubleOrNull()?.div(100.0) ?: return null }
        if (weights.any { it <= 0.0 }) return null
        if (kotlin.math.abs(weights.sum() - 1.0) > 0.0001) return null
        return AcademicPeriodScheme(
            label = academicPeriodLabel,
            periods = weights.mapIndexed { index, weight ->
                val order = index + 1
                AcademicPeriod(
                    id = "period-$order",
                    name = "${academicPeriodLabel.singular} $order",
                    weight = weight,
                    order = order
                )
            }
        )
    }

    private fun EducationLevel.isSchoolLevel(): Boolean = this == EducationLevel.PRIMARY || this == EducationLevel.SECONDARY

    private fun EducationLevel.standardGradeOptions(): List<String> {
        return if (this == EducationLevel.PRIMARY) {
            listOf("1°", "2°", "3°", "4°", "5°")
        } else {
            listOf("6°", "7°", "8°", "9°", "10°", "11°")
        }
    }
}

private fun suggestedAcademicWeights(count: Int): List<String> {
    if (count <= 0) return emptyList()
    if (count == 2) return listOf("50", "50")
    if (count == 3) return listOf("30", "40", "30")

    val base = 100 / count
    val remainder = 100 % count
    return List(count) { index ->
        (base + if (index < remainder) 1 else 0).toString()
    }
}

val GradingScale.maxNumericValue: Double
    get() = when (this) {
        GradingScale.ZERO_TO_FIVE -> 5.0
        GradingScale.ZERO_TO_HUNDRED -> 100.0
        GradingScale.CUSTOM -> 100.0
    }

val GradingScale.defaultPassingGrade: Double
    get() = when (this) {
        GradingScale.ZERO_TO_FIVE -> 3.0
        GradingScale.ZERO_TO_HUNDRED -> 60.0
        GradingScale.CUSTOM -> 60.0
    }

val GradingScale.defaultTargetAverage: Double
    get() = when (this) {
        GradingScale.ZERO_TO_FIVE -> 4.0
        GradingScale.ZERO_TO_HUNDRED -> 80.0
        GradingScale.CUSTOM -> 80.0
    }

private val GradingScale.defaultPassingGradeText: String
    get() = when (this) {
        GradingScale.CUSTOM -> defaultPassingGrade.toInt().toString()
        GradingScale.ZERO_TO_HUNDRED -> defaultPassingGrade.toInt().toString()
        GradingScale.ZERO_TO_FIVE -> defaultPassingGrade.toString()
    }

private fun SetupViewModel.customDefaultPassingGradeText(): String {
    return (customGradeMax * 0.6).roundGradeInput()
}

private fun SetupViewModel.customDefaultTargetAverageText(): String {
    return (customGradeMax * 0.8).roundGradeInput()
}

private fun Double.roundGradeInput(): String {
    return if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", this)
    }
}

private val GradingScale.defaultTargetAverageText: String
    get() = when (this) {
        GradingScale.CUSTOM -> defaultTargetAverage.toInt().toString()
        GradingScale.ZERO_TO_HUNDRED -> defaultTargetAverage.toInt().toString()
        GradingScale.ZERO_TO_FIVE -> defaultTargetAverage.toString()
    }
