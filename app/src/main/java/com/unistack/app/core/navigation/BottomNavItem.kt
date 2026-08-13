package com.unistack.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.unistack.app.feature_user.domain.AppModule

object AppRoutes {
    const val Home = "home"
    const val Notifications = "notifications"
    const val NotificationDetail = "notification_detail"
    const val Grades = "grades"
    const val Tasks = "tasks"
    const val Academic = "academic"
    const val Profile = "profile"
    const val Settings = "settings"
    const val AppearanceSettings = "appearance_settings"
    const val AccessibilitySettings = "accessibility_settings"
    const val Calendar = "calendar"
    const val AcademicSettings = "academic_settings"
    const val ModuleSettings = "module_settings"
    const val NotificationSettings = "notification_settings"
    const val DataSettings = "data_settings"
    const val UpdateSettings = "update_settings"
    const val Pro = "pro"
    const val AcademicTemplates = "academic_templates"
    const val Expenses = "expenses"
    const val AddSubject = "add_subject"
    const val AddSubjectFromTask = "add_subject_from_task"
    const val EditSubject = "edit_subject"

    /**
     * El mismo formulario de materia, abierto desde Horario.
     *
     * Tiene ruta propia y no un argumento de [AddSubject] porque de la ruta dependen tres
     * cosas distintas: a dónde vuelve el botón atrás (Horario y no Académico), qué bloque
     * llega desplegado, y que el módulo de notas pueda estar apagado sin que Horario se
     * quede sin poder crear clases.
     */
    const val AddSubjectFromSchedule = "add_subject_from_schedule"
    const val EditSubjectFromSchedule = "edit_subject_from_schedule"
    const val SubjectDetail = "subject_detail"
    const val SubjectPeriodDetail = "subject_period_detail"
    const val PriorHistory = "prior_history"
    const val AddGrade = "add_grade"
    const val AddGradeFromHistory = "add_grade_from_history"
    const val EditGrade = "edit_grade"
    const val AddTask = "add_task"
    const val EditTask = "edit_task"
    const val AddExpense = "add_expense"
    const val EditExpense = "edit_expense"

    /**
     * Pestaña con la que abrir Académico.
     *
     * Va en la ruta y no en el estado interno de la pantalla porque quien navega es quien
     * sabe a qué viene: la casilla «Pendientes» de Inicio quiere Tareas, y el ajuste de
     * pantalla inicial quiere la que el usuario eligió. Mientras fue estado privado no había
     * forma de decírselo, y por eso elegir «Tareas» como pantalla de arranque abría Materias.
     */
    const val AcademicTabArg = "tab"
    const val AcademicTabSubjects = "subjects"
    const val AcademicTabTasks = "tasks"

    /** Con argumento opcional: navegar a [Academic] a secas sigue siendo válido. */
    const val AcademicWithTab = "$Academic?$AcademicTabArg={$AcademicTabArg}"

    fun academic(tab: String) = "$Academic?$AcademicTabArg=$tab"

    fun subjectDetail(subjectId: String) = "$SubjectDetail/$subjectId"
    fun subjectPeriodDetail(subjectId: String, periodId: String) = "$SubjectPeriodDetail/$subjectId/$periodId"
    fun priorHistory(subjectId: String) = "$PriorHistory/$subjectId"
    fun addGrade(subjectId: String, periodId: String? = null) =
        if (periodId == null) "$AddGrade/$subjectId" else "$AddGrade/$subjectId/$periodId"
    fun addGradeFromHistory(subjectId: String, periodId: String) =
        "$AddGradeFromHistory/$subjectId/$periodId"
    fun editSubject(subjectId: String) = "$EditSubject/$subjectId"
    fun editSubjectFromSchedule(subjectId: String) = "$EditSubjectFromSchedule/$subjectId"
    fun editGrade(subjectId: String, gradeId: String) = "$EditGrade/$subjectId/$gradeId"
    fun editTask(taskId: String) = "$EditTask/$taskId"
    fun editExpense(expenseId: String) = "$EditExpense/$expenseId"
    fun notificationDetail(notificationId: Int) = "$NotificationDetail/$notificationId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    companion object {
        private val homeItem = BottomNavItem(
            route = AppRoutes.Home,
            label = "Inicio",
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home
        )
        private val academicItem = BottomNavItem(
            route = AppRoutes.Academic,
            label = "Académico",
            selectedIcon = Icons.AutoMirrored.Rounded.MenuBook,
            unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook
        )
        private val calendarItem = BottomNavItem(
            route = AppRoutes.Calendar,
            label = "Horario",
            selectedIcon = Icons.Rounded.CalendarMonth,
            unselectedIcon = Icons.Outlined.CalendarMonth
        )
        private val expensesItem = BottomNavItem(
            route = AppRoutes.Expenses,
            label = "Gastos",
            selectedIcon = Icons.Rounded.AccountBalanceWallet,
            unselectedIcon = Icons.Outlined.AccountBalanceWallet
        )
        private val profileItem = BottomNavItem(
            route = AppRoutes.Profile,
            label = "Perfil",
            selectedIcon = Icons.Rounded.Person,
            unselectedIcon = Icons.Outlined.Person
        )

        val items = listOf(homeItem, academicItem, calendarItem, expensesItem, profileItem)

        fun itemsFor(enabledModules: Set<AppModule>): List<BottomNavItem> {
            return buildList {
                add(homeItem)
                if (AppModule.GRADES in enabledModules || AppModule.TASKS in enabledModules) add(academicItem)
                add(calendarItem)
                if (AppModule.EXPENSES in enabledModules) add(expensesItem)
                add(profileItem)
            }
        }
    }
}
