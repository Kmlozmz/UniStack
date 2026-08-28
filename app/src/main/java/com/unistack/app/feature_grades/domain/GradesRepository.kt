package com.unistack.app.feature_grades.domain

import kotlinx.coroutines.flow.StateFlow

interface GradesRepository {
    val subjects: StateFlow<List<Subject>>

    fun addSubject(subject: Subject)

    /**
     * Actualiza los campos propios de la materia. **No toca sus notas**, aunque el
     * [Subject] que recibe las lleve dentro: viven en otra tabla y se gestionan con
     * [addGrade], [updateGrade], [deleteGrade] y [clearGrades].
     */
    fun updateSubject(subject: Subject)
    fun deleteSubject(subjectId: String)
    fun addGrade(subjectId: String, grade: GradeItem)
    fun updateGrade(subjectId: String, grade: GradeItem)
    fun deleteGrade(subjectId: String, gradeId: String)

    /** Borra de una vez todas las notas de una materia, dejándola en pie. */
    fun clearGrades(subjectId: String)

    /**
     * Marca con [termId] las materias que todavía no tienen periodo.
     *
     * Se llama al cerrar uno. Son las materias de antes de que existieran los periodos: si se
     * quedaran sin él, el histórico las perdería justo en el momento en que empieza a haber
     * histórico. Las que ya tienen periodo no se tocan.
     */
    suspend fun stampTerm(termId: String)
}
