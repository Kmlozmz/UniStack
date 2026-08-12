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
}
