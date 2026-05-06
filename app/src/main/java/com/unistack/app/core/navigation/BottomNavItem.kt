package com.unistack.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector

object AppRoutes {
    const val Home = "home"
    const val Grades = "grades"
    const val Tasks = "tasks"
    const val Profile = "profile"
    const val Expenses = "expenses"
    const val AddSubject = "add_subject"
    const val EditSubject = "edit_subject"
    const val SubjectDetail = "subject_detail"
    const val AddGrade = "add_grade"
    const val EditGrade = "edit_grade"
    const val AddTask = "add_task"
    const val EditTask = "edit_task"
    const val AddExpense = "add_expense"
    const val GradeSimulator = "grade_simulator"

    fun subjectDetail(subjectId: String) = "$SubjectDetail/$subjectId"
    fun addGrade(subjectId: String) = "$AddGrade/$subjectId"
    fun editSubject(subjectId: String) = "$EditSubject/$subjectId"
    fun editGrade(subjectId: String, gradeId: String) = "$EditGrade/$subjectId/$gradeId"
    fun editTask(taskId: String) = "$EditTask/$taskId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    companion object {
        val items = listOf(
            BottomNavItem(AppRoutes.Home, "Inicio", Icons.Rounded.Home),
            BottomNavItem(AppRoutes.Grades, "Materias", Icons.AutoMirrored.Rounded.MenuBook),
            BottomNavItem(AppRoutes.Tasks, "Tareas", Icons.AutoMirrored.Rounded.Assignment),
            BottomNavItem(AppRoutes.Profile, "Perfil", Icons.Rounded.Person)
        )
    }
}
