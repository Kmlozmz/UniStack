package com.unistack.app.feature_grades.domain

import com.unistack.app.feature_user.domain.GradingCutScheme

data class Subject(
    val id: String,
    val name: String,
    val targetAverage: Double,
    val grades: List<GradeItem>,
    val visualType: SubjectVisualType = SubjectVisualType.TEAL,
    val customColor: Int? = null,
    val cutScheme: GradingCutScheme = GradingCutScheme.default(),
    /**
     * El corte en el que va la materia, **vacío mientras el usuario no lo haya elegido**.
     *
     * Antes arrancaba en el primer corte, y la app presentaba esa suposición como un hecho:
     * marcaba «Corte actual» en una materia recién creada y, si el usuario elegía otro corte,
     * reclamaba el historial de unos cortes anteriores que nunca dijo haber cursado. No hay
     * forma de saberlo sin preguntar, así que no se supone. Léelo por [chosenCutId] o
     * [defaultCutId] según lo que necesites.
     */
    val activeCutId: String = "",
    val historyPromptStatus: PriorHistoryPromptStatus = PriorHistoryPromptStatus.NOT_SHOWN,
    val unknownCutIds: Set<String> = emptySet(),
    /** El periodo academico al que pertenece, o nulo si es anterior a que existieran. */
    val termId: String? = null,
    /**
     * Cuántas faltas puedes acumular antes de perder la materia, o nulo si no lo has dicho.
     *
     * Es **el** número que se mira en la universidad, y cambia por materia: un laboratorio
     * perdona menos que una teórica. Nulo no es «no hay tope», es «no lo sé», que es distinto:
     * con nulo la app no puede prometer «te quedan 4», así que enseña lo que sí sabe.
     */
    val absenceLimit: Int? = null
) {
    /** El corte que el usuario eligió, o null si todavía no ha elegido. */
    val chosenCutId: String?
        get() = activeCutId.takeIf { id -> cutScheme.cuts.any { it.id == id } }

    /**
     * Dónde entra lo que se registre ahora: el corte elegido, y mientras no lo haya, el
     * primero. Para rellenar un formulario vale una suposición; para decirle al usuario en
     * qué corte va, no.
     */
    val defaultCutId: String
        get() = chosenCutId ?: cutScheme.cuts.firstOrNull()?.id.orEmpty()
}

enum class PriorHistoryPromptStatus {
    NOT_SHOWN,
    SNOOZED,
    DISMISSED,
    COMPLETED
}
