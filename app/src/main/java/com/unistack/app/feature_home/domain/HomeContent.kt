package com.unistack.app.feature_home.domain

import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_templates.domain.AcademicWork

data class HomeContent(
    val subjects: List<Subject>,
    val tasks: List<StudentTask>,
    val expenses: List<Expense>,
    val works: List<AcademicWork>,
    val classSessions: List<ClassSession> = emptyList()
)
