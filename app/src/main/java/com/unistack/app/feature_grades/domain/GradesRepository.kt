package com.unistack.app.feature_grades.domain

import kotlinx.coroutines.flow.StateFlow

interface GradesRepository {
    val subjects: StateFlow<List<Subject>>

    fun addSubject(subject: Subject)
    fun updateSubject(subject: Subject)
    fun deleteSubject(subjectId: String)
    fun addGrade(subjectId: String, grade: GradeItem)
    fun deleteGrade(subjectId: String, gradeId: String)
}
