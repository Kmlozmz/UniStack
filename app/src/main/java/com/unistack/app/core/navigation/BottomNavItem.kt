package com.unistack.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.unistack.app.feature_user.domain.AppModule

object AppRoutes {
    const val Home = "home"
    const val Grades = "grades"
    const val Tasks = "tasks"
    const val Profile = "profile"
    const val Pro = "pro"
    const val Expenses = "expenses"
    const val AddSubject = "add_subject"
    const val EditSubject = "edit_subject"
    const val SubjectDetail = "subject_detail"
    const val AddGrade = "add_grade"
    const val EditGrade = "edit_grade"
    const val AddTask = "add_task"
    const val EditTask = "edit_task"
    const val AddExpense = "add_expense"
    const val EditExpense = "edit_expense"
    const val GradeSimulator = "grade_simulator"

    fun subjectDetail(subjectId: String) = "$SubjectDetail/$subjectId"
    fun addGrade(subjectId: String) = "$AddGrade/$subjectId"
    fun editSubject(subjectId: String) = "$EditSubject/$subjectId"
    fun editGrade(subjectId: String, gradeId: String) = "$EditGrade/$subjectId/$gradeId"
    fun editTask(taskId: String) = "$EditTask/$taskId"
    fun editExpense(expenseId: String) = "$EditExpense/$expenseId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    companion object {
        private val homeItem = BottomNavItem(AppRoutes.Home, "Inicio", Icons.Rounded.Home)
        private val gradesItem = BottomNavItem(AppRoutes.Grades, "Materias", Icons.AutoMirrored.Rounded.MenuBook)
        private val tasksItem = BottomNavItem(AppRoutes.Tasks, "Tareas", Icons.AutoMirrored.Rounded.Assignment)
        private val profileItem = BottomNavItem(AppRoutes.Profile, "Perfil", Icons.Rounded.Person)

        val items = listOf(homeItem, gradesItem, tasksItem, profileItem)

        fun itemsFor(enabledModules: Set<AppModule>): List<BottomNavItem> {
            return buildList {
                add(homeItem)
                if (AppModule.GRADES in enabledModules) add(gradesItem)
                if (AppModule.TASKS in enabledModules) add(tasksItem)
                add(profileItem)
            }
        }
    }
}
