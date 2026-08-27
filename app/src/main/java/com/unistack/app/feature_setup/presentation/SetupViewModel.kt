package com.unistack.app.feature_setup.presentation

import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.ViewModel
import com.unistack.app.core.utils.TextValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermType
import java.time.LocalDate
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.VisualPreference
import com.unistack.app.core.utils.ValidationResult
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val termRepository: AcademicTermRepository
) : ViewModel() {
    var preferredName by mutableStateOf("")
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
    /**
     * Sin elegir de partida: preseleccionar «Cortes» daba por hecho una nomenclatura que no
     * es la de todo el mundo, y al venir ya marcada era fácil pasar de largo sin leerla.
     */
    var gradingCutWeights by mutableStateOf(emptyList<String>())
        private set

    val nameValidation: ValidationResult
        get() = TextValidators.validateDisplayName(preferredName)

    val customProgramValidation: ValidationResult
        get() = TextValidators.validateCustomCareer(customProgram)

    val isNameValid: Boolean
        get() = nameValidation.isValid

    /**
     * El area y la carrera son obligatorias: dan de comer al catalogo de materias.
     *
     * Antes esto tenia tres ramas —universidad, escolar y el resto—, porque el paso anterior
     * preguntaba el nivel de estudios. Al centrarse la app en educacion superior queda una
     * sola, y con ella se va la nulabilidad que arrastraba todo el flujo.
     */
    val isAcademicInfoValid: Boolean
        get() {
            val area = studyArea ?: return false
            val program = selectedProgram ?: return false
            if (area == StudyArea.OTHER || program == OTHER_OPTION) {
                return customProgramValidation.isValid
            }
            return true
        }

    val canContinueFromProfile: Boolean
        get() = isAcademicInfoValid

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

    val areGradingCutsValid: Boolean
        get() = buildGradingCutSchemeOrNull() != null

    // ---- El periodo academico ----

    var termType by mutableStateOf<AcademicTermType?>(null)
        private set
    var termStart by mutableStateOf<LocalDate?>(null)
        private set
    var termPlannedEnd by mutableStateOf<LocalDate?>(null)
        private set
    var termName by mutableStateOf("")
        private set

    /**
     * Los dias en que cierra cada corte, uno menos que cortes hay.
     *
     * Vacio significa «todavia no las se», que es una respuesta valida: sin ellas el corte de
     * cada nota se sigue eligiendo a mano. El ultimo corte no aparece porque acaba con el
     * periodo, y por eso guardar solo los cortes hace imposibles los solapes y los huecos.
     */
    var cutEndDates by mutableStateOf<List<LocalDate>>(emptyList())
        private set

    /** Si dijo que sabe las fechas de corte. Nulo: todavia no ha contestado. */
    var knowsCutDates by mutableStateOf<Boolean?>(null)
        private set

    fun updateKnowsCutDates(value: Boolean) {
        knowsCutDates = value
        if (value) suggestCutEndDates() else clearCutEndDates()
    }

    val termCutCount: Int get() = gradingCutWeights.size

    /** Elegir tipo es lo unico obligatorio aqui; las fechas vienen sugeridas y editables. */
    val isTermValid: Boolean
        get() {
            val tipo = termType ?: return false
            val inicio = termStart ?: return false
            val fin = termPlannedEnd
            if (fin != null && !fin.isAfter(inicio)) return false
            if (termName.isBlank()) return false
            // O estan todas las fechas de corte, o ninguna.
            if (cutEndDates.isNotEmpty()) {
                if (cutEndDates.size != (termCutCount - 1).coerceAtLeast(0)) return false
                if (cutEndDates.zipWithNext().any { (a, b) -> !b.isAfter(a) }) return false
                if (cutEndDates.first().isBefore(inicio)) return false
                if (fin != null && cutEndDates.last().isAfter(fin)) return false
            }
            @Suppress("UNUSED_EXPRESSION") tipo
            return true
        }

    fun updateTermType(value: AcademicTermType) {
        termType = value
        knowsCutDates = null
        // Al elegir tipo se proponen fechas y nombre; siguen siendo editables.
        val inicio = termStart ?: LocalDate.now()
        termStart = inicio
        termPlannedEnd = AcademicTerm.suggestedPlannedEnd(value, inicio)
        termName = AcademicTerm.suggestedName(value, inicio)
        cutEndDates = emptyList()
    }

    fun updateTermStart(value: LocalDate) {
        termStart = value
        val tipo = termType ?: return
        termPlannedEnd = AcademicTerm.suggestedPlannedEnd(tipo, value)
        termName = AcademicTerm.suggestedName(tipo, value)
        cutEndDates = emptyList()
    }

    fun updateTermPlannedEnd(value: LocalDate) {
        termPlannedEnd = value
        cutEndDates = emptyList()
    }

    fun updateTermName(value: String) {
        termName = value.take(40)
    }

    /**
     * Reparte los cortes por igual entre el inicio y el fin previsto, como punto de partida.
     *
     * Es una sugerencia, no una imposicion: cada fecha se mueve despues. Sin fin previsto no
     * hay tramo que repartir, asi que no se propone nada.
     */
    fun suggestCutEndDates() {
        val inicio = termStart ?: return
        val fin = termPlannedEnd ?: return
        val tramos = termCutCount
        if (tramos < 2) return
        val dias = java.time.temporal.ChronoUnit.DAYS.between(inicio, fin)
        if (dias < tramos) return
        cutEndDates = (1 until tramos).map { i -> inicio.plusDays(dias * i / tramos) }
    }

    fun clearCutEndDates() {
        cutEndDates = emptyList()
    }

    fun updateCutEndDate(index: Int, value: LocalDate) {
        cutEndDates = cutEndDates.mapIndexed { i, actual -> if (i == index) value else actual }
    }

    fun updatePreferredName(value: String) {
        preferredName = value.take(30)
    }

    fun updateInstitutionName(value: String) {
        institutionName = value.take(80)
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

    fun updateGradingCutCount(count: Int) {
        val safeCount = count.coerceIn(0, 6)
        gradingCutWeights = suggestedAcademicWeights(safeCount)
    }

    fun updateGradingCutWeight(index: Int, value: String) {
        gradingCutWeights = gradingCutWeights.mapIndexed { currentIndex, currentValue ->
            if (currentIndex == index) value.filter { it.isDigit() || it == '.' }.take(5) else currentValue
        }
    }

    fun finishSetup() {
        crearPeriodoSiSeConfiguro()
        val now = System.currentTimeMillis()
        val info = academicInfoValue()
        /*
         * Se parte de lo que ya había, no de un perfil en blanco.
         *
         * Construir uno nuevo desde cero funciona la primera vez, cuando no hay nada que
         * conservar. Pero «Repetir configuración inicial» vuelve a pasar por aquí prometiendo
         * que tus datos se quedan, y lo que salía era un perfil con todos los valores de
         * fábrica: se perdían el retrato, la cuenta de Google vinculada, el tema y los acentos
         * elegidos, los ajustes de accesibilidad y los recordatorios. Nada de eso lo pregunta el
         * onboarding, así que nada de eso debería tocarlo.
         */
        val previous = userRepository.userProfile.value
        val fresh = UserProfile(
            userId = UserIds.LOCAL,
            preferredName = TextValidators.normalizeText(preferredName),
            careerOrProgram = info,
            studyArea = studyArea,
            // Se guarda sin normalizar: conservar el original permite mapearlo a un
            // catálogo canónico más adelante.
            institutionName = institutionName.trim().takeIf { it.isNotEmpty() },
            gradingScale = gradingScale,
            customGradeMax = customGradeMax.coerceIn(1.0, 100.0),
            passingGrade = passingGradeText.toDoubleOrNull() ?: gradingScale.defaultPassingGrade,
            targetAverage = targetAverageText.toDoubleOrNull() ?: gradingScale.defaultTargetAverage,
            enabledModules = enabledModules,
            gradingCutScheme = buildGradingCutSchemeOrNull() ?: GradingCutScheme.default(),
            visualPreference = previous?.visualPreference ?: VisualPreference.SYSTEM,
            setupCompleted = true,
            createdAt = previous?.createdAt ?: now,
            updatedAt = now
        )
        // Sobre el perfil que ya estaba se pisa solo lo que el onboarding pregunta, campo por
        // campo. Enumerarlos cuesta unas lineas y evita el fallo contrario: que anadir manana un
        // campo nuevo al perfil lo borre en silencio cada vez que alguien repita la configuracion.
        val profile = previous?.copy(
            preferredName = fresh.preferredName,
            careerOrProgram = fresh.careerOrProgram,
            studyArea = fresh.studyArea,
            institutionName = fresh.institutionName,
            gradingScale = fresh.gradingScale,
            customGradeMax = fresh.customGradeMax,
            passingGrade = fresh.passingGrade,
            targetAverage = fresh.targetAverage,
            enabledModules = fresh.enabledModules,
            gradingCutScheme = fresh.gradingCutScheme,
            setupCompleted = true,
            updatedAt = now
        ) ?: fresh
        userRepository.saveUserProfile(profile)
    }

    /**
     * Deja creado el periodo con el que arranca la app.
     *
     * Si el usuario se salto el paso —o desactivo el modulo de notas— no se inventa ninguno:
     * un periodo sin fechas declaradas seria exactamente la suposicion que este trabajo viene
     * a quitar, y la app sabe vivir sin periodo activo.
     *
     * El fallo tampoco se propaga: quedarse sin periodo es recuperable desde Ajustes, y tumbar
     * el final del onboarding por ello seria peor que seguir.
     */
    private fun crearPeriodoSiSeConfiguro() {
        val tipo = termType ?: return
        val inicio = termStart ?: return
        viewModelScope.launch {
            termRepository.create(
                name = termName.trim().ifBlank { AcademicTerm.suggestedName(tipo, inicio) },
                type = tipo,
                start = inicio,
                plannedEnd = termPlannedEnd
            )
        }
    }

    private fun academicInfoValue(): String? {
        val area = studyArea ?: return null
        val program = selectedProgram ?: return null
        return if (area == StudyArea.OTHER || program == OTHER_OPTION) {
            TextValidators.normalizeText(customProgram).takeIf { TextValidators.validateCustomCareer(it).isValid }
        } else {
            program
        }
    }

    private fun buildGradingCutSchemeOrNull(): GradingCutScheme? {
        /*
         * Sin pesos no hay esquema, y el boton se queda bloqueado.
         *
         * Antes lo primero que se miraba era el tipo elegido —«Corte» o «Periodo»—, que hacia
         * de centinela ademas de dar nombre a los tramos. Ya no se pregunta, asi que el
         * centinela es la lista de pesos y el nombre sale de la constante.
         */
        val weights = gradingCutWeights.map { it.toDoubleOrNull()?.div(100.0) ?: return null }
        if (weights.isEmpty()) return null
        if (weights.any { it <= 0.0 }) return null
        if (kotlin.math.abs(weights.sum() - 1.0) > 0.0001) return null
        return GradingCutScheme(
            cuts = weights.mapIndexed { index, weight ->
                val order = index + 1
                GradingCut(
                    id = "period-$order",
                    name = "${Corte.Singular} $order",
                    weight = weight,
                    order = order,
                    // El ultimo no lleva fecha: acaba cuando acaba el periodo.
                    endEpochDay = cutEndDates.getOrNull(index)?.toEpochDay()
                )
            }
        )
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
