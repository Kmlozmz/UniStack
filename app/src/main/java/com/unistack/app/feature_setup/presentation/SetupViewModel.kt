package com.unistack.app.feature_setup.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.unistack.app.core.AppContainer
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.VisualPreference
import com.unistack.app.core.utils.ValidationResult

class SetupViewModel(
    private val userRepository: UserRepository = AppContainer.userRepository
) : ViewModel() {
    var preferredName by mutableStateOf("")
        private set
    var educationLevel by mutableStateOf(EducationLevel.UNIVERSITY)
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
    var passingGradeText by mutableStateOf("3.0")
        private set
    var targetAverageText by mutableStateOf("4.0")
        private set
    var enabledModules by mutableStateOf(AppModule.values().toSet())
        private set

    val nameValidation: ValidationResult
        get() = TextValidators.validateDisplayName(preferredName)

    val customProgramValidation: ValidationResult
        get() = TextValidators.validateCustomCareer(customProgram)

    val isNameValid: Boolean
        get() = nameValidation.isValid

    val isAcademicInfoValid: Boolean
        get() {
            if (educationLevel == EducationLevel.UNIVERSITY || educationLevel == EducationLevel.TECHNICAL) {
                val area = studyArea ?: return false
                val program = selectedProgram ?: return false
                if (area == StudyArea.OTHER || program == OTHER_OPTION) {
                    return customProgramValidation.isValid
                }
                return true
            }

            val value = academicInfo.trim()
            return value.isEmpty() || TextValidators.validateCustomCareer(value).isValid
        }

    val isGradesValid: Boolean
        get() {
            val passing = passingGradeText.toDoubleOrNull() ?: return false
            val target = targetAverageText.toDoubleOrNull() ?: return false
            val max = gradingScale.maxNumericValue ?: 100.0
            return passing in 0.0..max &&
                target in 0.0..max &&
                target >= passing
        }

    fun updatePreferredName(value: String) {
        preferredName = value.take(30)
    }

    fun updateEducationLevel(value: EducationLevel) {
        educationLevel = value
        academicInfo = ""
        studyArea = null
        selectedProgram = null
        customProgram = ""
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
        if (value == GradingScale.ZERO_TO_FIVE) {
            passingGradeText = "3.0"
            targetAverageText = "4.0"
        } else if (value == GradingScale.ZERO_TO_TEN) {
            passingGradeText = "6.0"
            targetAverageText = "8.0"
        } else if (value == GradingScale.ZERO_TO_ONE_HUNDRED) {
            passingGradeText = "60"
            targetAverageText = "80"
        }
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

    fun finishSetup() {
        val now = System.currentTimeMillis()
        val info = academicInfoValue()
        val profile = UserProfile(
            userId = UserIds.LOCAL,
            preferredName = TextValidators.normalizeText(preferredName),
            educationLevel = educationLevel,
            careerOrProgram = if (educationLevel == EducationLevel.SCHOOL) null else info,
            studyArea = studyArea,
            gradeLevel = if (educationLevel == EducationLevel.SCHOOL) info else null,
            gradingScale = gradingScale,
            passingGrade = passingGradeText.toDoubleOrNull() ?: gradingScale.defaultPassingGrade,
            targetAverage = targetAverageText.toDoubleOrNull() ?: gradingScale.defaultTargetAverage,
            enabledModules = enabledModules,
            visualPreference = VisualPreference.SYSTEM,
            setupCompleted = true,
            createdAt = now,
            updatedAt = now
        )
        userRepository.saveUserProfile(profile)
    }

    private fun academicInfoValue(): String? {
        if (educationLevel == EducationLevel.UNIVERSITY || educationLevel == EducationLevel.TECHNICAL) {
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
}

const val OTHER_OPTION = "Otra"

fun labelFor(area: StudyArea): String = when (area) {
    StudyArea.ENGINEERING_TECHNOLOGY -> "Ingeniería y tecnología"
    StudyArea.ECONOMICS_BUSINESS -> "Ciencias económicas y administrativas"
    StudyArea.LAW_POLITICS -> "Ciencias jurídicas"
    StudyArea.HEALTH_SCIENCES -> "Ciencias de la salud"
    StudyArea.EDUCATION -> "Educación"
    StudyArea.ARTS_DESIGN -> "Artes y diseño"
    StudyArea.SOCIAL_SCIENCES -> "Ciencias sociales"
    StudyArea.BASIC_SCIENCES -> "Ciencias básicas"
    StudyArea.OTHER -> "Otra"
}

fun programsFor(area: StudyArea): List<String> = when (area) {
    StudyArea.ENGINEERING_TECHNOLOGY -> listOf(
        "Ingeniería de Sistemas",
        "Ingeniería Industrial",
        "Ingeniería Civil",
        "Ingeniería Mecánica",
        "Ingeniería Electrónica",
        "Ingeniería Ambiental",
        "Ingeniería de Software",
        OTHER_OPTION
    )
    StudyArea.ECONOMICS_BUSINESS -> listOf(
        "Contaduría Pública",
        "Administración de Empresas",
        "Economía",
        "Finanzas",
        "Mercadeo",
        "Negocios Internacionales",
        OTHER_OPTION
    )
    StudyArea.LAW_POLITICS -> listOf("Derecho", "Ciencias Políticas", "Criminalística", OTHER_OPTION)
    StudyArea.HEALTH_SCIENCES -> listOf("Medicina", "Enfermería", "Odontología", "Fisioterapia", "Nutrición", "Bacteriología", OTHER_OPTION)
    StudyArea.EDUCATION -> listOf(
        "Licenciatura en Educación Infantil",
        "Licenciatura en Matemáticas",
        "Licenciatura en Lenguas",
        "Licenciatura en Ciencias Sociales",
        "Licenciatura en Educación Física",
        OTHER_OPTION
    )
    StudyArea.ARTS_DESIGN -> listOf("Diseño Gráfico", "Diseño Industrial", "Diseño de Modas", "Artes Visuales", "Música", OTHER_OPTION)
    StudyArea.SOCIAL_SCIENCES -> listOf("Psicología", "Trabajo Social", "Comunicación Social", "Sociología", "Antropología", OTHER_OPTION)
    StudyArea.BASIC_SCIENCES -> listOf("Matemáticas", "Física", "Química", "Biología", "Estadística", OTHER_OPTION)
    StudyArea.OTHER -> listOf(OTHER_OPTION)
}

val GradingScale.maxNumericValue: Double?
    get() = when (this) {
        GradingScale.ZERO_TO_FIVE -> 5.0
        GradingScale.ZERO_TO_TEN -> 10.0
        GradingScale.ZERO_TO_ONE_HUNDRED -> 100.0
        GradingScale.LETTERS,
        GradingScale.CUSTOM -> null
    }

val GradingScale.defaultPassingGrade: Double
    get() = when (this) {
        GradingScale.ZERO_TO_FIVE -> 3.0
        GradingScale.ZERO_TO_TEN -> 6.0
        GradingScale.ZERO_TO_ONE_HUNDRED -> 60.0
        GradingScale.LETTERS,
        GradingScale.CUSTOM -> 3.0
    }

val GradingScale.defaultTargetAverage: Double
    get() = when (this) {
        GradingScale.ZERO_TO_FIVE -> 4.0
        GradingScale.ZERO_TO_TEN -> 8.0
        GradingScale.ZERO_TO_ONE_HUNDRED -> 80.0
        GradingScale.LETTERS,
        GradingScale.CUSTOM -> 4.0
    }
