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
    /**
     * El corte en el que va la materia, **vacío mientras el usuario no lo haya elegido**.
     *
     * Antes arrancaba en el primer corte, y la app presentaba esa suposición como un hecho:
     * marcaba «Corte actual» en una materia recién creada y, si el usuario elegía otro corte,
     * reclamaba el historial de unos cortes anteriores que nunca dijo haber cursado. No hay
     * forma de saberlo sin preguntar, así que no se supone. Léelo por [chosenPeriodId] o
     * [defaultPeriodId] según lo que necesites.
     */
    val activePeriodId: String = "",
    val historyPromptStatus: PriorHistoryPromptStatus = PriorHistoryPromptStatus.NOT_SHOWN,
    val unknownPeriodIds: Set<String> = emptySet()
) {
    /** El corte que el usuario eligió, o null si todavía no ha elegido. */
    val chosenPeriodId: String?
        get() = activePeriodId.takeIf { id -> periodScheme.periods.any { it.id == id } }

    /**
     * Dónde entra lo que se registre ahora: el corte elegido, y mientras no lo haya, el
     * primero. Para rellenar un formulario vale una suposición; para decirle al usuario en
     * qué corte va, no.
     */
    val defaultPeriodId: String
        get() = chosenPeriodId ?: periodScheme.periods.firstOrNull()?.id.orEmpty()
}

enum class PriorHistoryPromptStatus {
    NOT_SHOWN,
    SNOOZED,
    DISMISSED,
    COMPLETED
}
