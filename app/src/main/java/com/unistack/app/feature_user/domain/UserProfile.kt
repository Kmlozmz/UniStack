package com.unistack.app.feature_user.domain

import com.unistack.app.feature_expenses.domain.ExpenseCategory

data class UserProfile(
    val userId: String,
    val preferredName: String,
    val accountProvider: AuthProvider = AuthProvider.LOCAL,
    val accountProviderUserId: String? = null,
    val accountEmail: String? = null,
    val accountPhotoUrl: String? = null,
    /**
     * El retrato que eligió quien usa la app, si eligió alguno.
     *
     * Va aparte de [accountPhotoUrl] y manda sobre él. Vincular Google trae la foto de esa
     * cuenta, que es un punto de partida razonable —mejor una foto tuya que una inicial—, pero
     * no puede ser la última palabra: hay quien tiene ahí una foto de hace seis años. Guardando
     * las dos, cambiar el retrato no borra la de la cuenta y desvincular no borra el tuyo.
     *
     * Es un `content://` con permiso de lectura persistido, no una copia del archivo: la app no
     * duplica imágenes que ya están en la galería.
     */
    val localPhotoUri: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val lastSyncAt: Long? = null,
    val educationLevel: EducationLevel,
    val careerOrProgram: String?,
    val studyArea: StudyArea?,
    val gradeLevel: String?,
    /**
     * Nombre de la institución tal y como lo escribió el usuario, sin normalizar.
     *
     * Se guarda en crudo a propósito: si algún día se añade un catálogo canónico, poder
     * mapear el texto original a una entidad es posible; recuperar lo que la persona
     * escribió después de haberlo sobrescrito, no. Es opcional en todos los niveles.
     */
    val institutionName: String? = null,
    val gradingScale: GradingScale,
    val customGradeMax: Double = 100.0,
    val passingGrade: Double,
    val targetAverage: Double,
    val enabledModules: Set<AppModule>,
    val visualPreference: VisualPreference = VisualPreference.SYSTEM,
    val appearancePreferences: AppearancePreferences = AppearancePreferences(),
    val accessibilityPreferences: AccessibilityPreferences = AccessibilityPreferences(),
    val taskRemindersEnabled: Boolean = true,
    val academicWorkRemindersEnabled: Boolean = true,
    val overdueRemindersEnabled: Boolean = true,
    val gradeInsightRemindersEnabled: Boolean = true,
    val pendingGradeRemindersEnabled: Boolean = true,
    val reminderLeadHours: Int = 24,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStartHour: Int? = null,
    val quietHoursEndHour: Int? = null,
    val weeklyBudget: Int = 0,
    val monthlyBudget: Int = 0,
    val expenseAlertThresholdPercent: Int = 80,
    val enabledExpenseCategories: Set<ExpenseCategory> = ExpenseCategory.entries.toSet(),
    val gradeScenarios: List<SavedGradeScenario> = emptyList(),
    val academicPeriodScheme: AcademicPeriodScheme = AcademicPeriodScheme.default(),
    val setupCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class SavedGradeScenario(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val name: String,
    val targetAverage: Double,
    val neededGrade: Double?,
    val createdAt: Long
)

data class AcademicPeriodScheme(
    val label: AcademicPeriodLabel = AcademicPeriodLabel.CORTE,
    val periods: List<AcademicPeriod> = defaultPeriods()
) {
    val totalWeight: Double
        get() = periods.sumOf { it.weight }

    val isValid: Boolean
        get() = periods.isNotEmpty() &&
            periods.all { it.weight > 0.0 } &&
            kotlin.math.abs(totalWeight - 1.0) <= 0.0001

    fun periodName(periodId: String?): String {
        return periods.firstOrNull { it.id == periodId }?.name
            ?: periods.firstOrNull()?.name
            ?: label.singular
    }

    companion object {
        fun default(): AcademicPeriodScheme = AcademicPeriodScheme(
            label = AcademicPeriodLabel.CORTE,
            periods = defaultPeriods()
        )

        fun defaultPeriods(): List<AcademicPeriod> = listOf(
            AcademicPeriod(id = "period-1", name = "Corte 1", weight = 0.30, order = 1),
            AcademicPeriod(id = "period-2", name = "Corte 2", weight = 0.40, order = 2),
            AcademicPeriod(id = "period-3", name = "Corte 3", weight = 0.30, order = 3)
        )
    }
}

data class AcademicPeriod(
    val id: String,
    val name: String,
    val weight: Double,
    val order: Int
)

enum class AcademicPeriodLabel(val singular: String, val plural: String) {
    PERIOD("Periodo", "Periodos"),
    CORTE("Corte", "Cortes")
}

enum class EducationLevel {
    PRIMARY,
    SECONDARY,
    UNIVERSITY,
    OTHER
}

enum class GradingScale {
    ZERO_TO_FIVE,
    ZERO_TO_HUNDRED,
    CUSTOM
}

enum class StudyArea {
    ENGINEERING_TECHNOLOGY,
    ECONOMICS_BUSINESS,
    LAW_POLITICS,
    HEALTH_SCIENCES,
    EDUCATION,
    ARTS_DESIGN,
    SOCIAL_SCIENCES,
    BASIC_SCIENCES,
    OTHER
}

enum class AppModule {
    GRADES,
    TASKS,
    EXPENSES,
    ACADEMIC_TEMPLATES
}

enum class VisualPreference {
    SYSTEM,
    LIGHT,
    DARK,
    OLED,
    CUSTOM
}
