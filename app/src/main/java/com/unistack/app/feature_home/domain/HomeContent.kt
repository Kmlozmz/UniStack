package com.unistack.app.feature_home.domain

import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_templates.domain.AcademicWork

data class HomeContent(
    val subjects: List<Subject>,
    val tasks: List<StudentTask>,
    val expenses: List<Expense>,
    val works: List<AcademicWork>,
    val classSessions: List<ClassSession> = emptyList(),
    /** Lo marcado clase a clase, para el bloque de Asistencia. */
    val occurrences: List<ClassOccurrence> = emptyList(),
    /** Las notas rápidas, de las que Inicio solo enseña las fijadas. */
    val notes: List<QuickNote> = emptyList()
)
