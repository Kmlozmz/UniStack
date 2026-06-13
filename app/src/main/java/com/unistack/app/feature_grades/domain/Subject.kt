package com.unistack.app.feature_grades.domain

import com.unistack.app.feature_user.domain.AcademicPeriodScheme

data class Subject(
    val id: String,
    val name: String,
    val targetAverage: Double,
    val grades: List<GradeItem>,
    val visualType: SubjectVisualType = SubjectVisualType.TEAL,
    val customColor: Int? = null,
    val periodScheme: AcademicPeriodScheme = AcademicPeriodScheme.default(),
    val activePeriodId: String = periodScheme.periods.firstOrNull()?.id ?: "period-1",
    val historyPromptStatus: PriorHistoryPromptStatus = PriorHistoryPromptStatus.NOT_SHOWN,
    val unknownPeriodIds: Set<String> = emptySet()
)

enum class PriorHistoryPromptStatus {
    NOT_SHOWN,
    SNOOZED,
    DISMISSED,
    COMPLETED
}
