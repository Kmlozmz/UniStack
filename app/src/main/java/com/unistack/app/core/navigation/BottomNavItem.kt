package com.unistack.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.unistack.app.R
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.IconStyle

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
    const val ThemeSettings = "theme_settings"
    const val SurfaceSettings = "surface_settings"
    const val TypographySettings = "typography_settings"
    const val ComponentSettings = "component_settings"
    const val HomeSettings = "home_settings"
    const val MotionSettings = "motion_settings"
    const val AccessibilitySettings = "accessibility_settings"
    const val Calendar = "calendar"
    const val AcademicSettings = "academic_settings"
    const val AcademicScale = "academic_scale"
    const val AcademicCuts = "academic_cuts"
    const val AcademicAbsence = "academic_absence"
    const val AcademicBreaks = "academic_breaks"
    const val AcademicTerm = "academic_term"
    const val AcademicHistory = "academic_history"
    const val TermClose = "term_close"
    const val NewTerm = "new_term"
    const val ClosedTerm = "closed_term"
    const val TermDetail = "term_detail"
    const val TermSubject = "term_subject"
    const val TermEditGrades = "term_edit_grades"
    const val TermBulletin = "term_bulletin"
    const val ModuleSettings = "module_settings"
    const val NotificationSettings = "notification_settings"
    const val DataSettings = "data_settings"
    const val UpdateSettings = "update_settings"
    const val WhatsNew = "whats_new"
    const val Resources = "resources"
    const val Help = "help"
    const val About = "about"
    const val AcademicWorkArg = "workId"
    const val AcademicWork = "academic_work/{workId}"

    fun academicWork(workId: String): String = "academic_work/" + workId
    const val GpaCalculator = "gpa_calculator"
    const val QuickNotes = "quick_notes"

    /**
     * Escribir una nota, con su ruta propia y a pantalla completa.
     *
     * Sin argumento se abre en blanco; con el identificador, sobre una nota que ya existe.
     * Son dos rutas y no un argumento opcional porque asi la de escribir no hereda el
     * historial de la que se estaba leyendo.
     */
    const val NewNote = "new_note"

    /** Con que arranca la nota nueva: texto, lista, foto, audio o archivo. */
    const val NewNoteStartArg = "start"

    /** Con qué materia sale ya vinculada, si se lanzó desde una en concreto. */
    const val NewNoteSubjectArg = "subject"
    const val NewNoteWithStart = "$NewNote?$NewNoteStartArg={$NewNoteStartArg}&$NewNoteSubjectArg={$NewNoteSubjectArg}"
    const val NoteEditor = "note_editor"
    const val AiAssistant = "ai_assistant"
    const val Labs = "labs"
    const val AcademicTemplates = "academic_templates"
    const val Expenses = "expenses"
    const val ExpenseInsights = "expense_insights"
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
    const val SubjectCutDetail = "subject_period_detail"
    const val SubjectStats = "subject_stats"
    const val PriorHistory = "prior_history"
    const val AddGrade = "add_grade"
    const val AddGradeFromHistory = "add_grade_from_history"
    const val EditGrade = "edit_grade"
    const val AddTask = "add_task"
    const val EditTask = "edit_task"
    const val TaskDetail = "task_detail"
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
    fun noteEditor(noteId: String) = "$NoteEditor/$noteId"
    fun newNote(start: String, subjectId: String? = null) =
        "$NewNote?$NewNoteStartArg=$start".let { if (subjectId == null) it else "$it&$NewNoteSubjectArg=$subjectId" }
    fun termDetail(termId: String) = "$TermDetail/$termId"
    fun termSubject(subjectId: String) = "$TermSubject/$subjectId"
    fun termEditGrades(subjectId: String) = "$TermEditGrades/$subjectId"
    fun termBulletin(termId: String) = "$TermBulletin/$termId"
    fun subjectCutDetail(subjectId: String, cutId: String) = "$SubjectCutDetail/$subjectId/$cutId"
    fun subjectStats(subjectId: String) = "$SubjectStats/$subjectId"
    fun priorHistory(subjectId: String) = "$PriorHistory/$subjectId"
    fun addGrade(subjectId: String, cutId: String? = null) =
        if (cutId == null) "$AddGrade/$subjectId" else "$AddGrade/$subjectId/$cutId"
    fun addGradeFromHistory(subjectId: String, cutId: String) =
        "$AddGradeFromHistory/$subjectId/$cutId"
    fun editSubject(subjectId: String) = "$EditSubject/$subjectId"
    fun editSubjectFromSchedule(subjectId: String) = "$EditSubjectFromSchedule/$subjectId"
    fun editGrade(subjectId: String, gradeId: String) = "$EditGrade/$subjectId/$gradeId"
    fun editTask(taskId: String) = "$EditTask/$taskId"
    fun taskDetail(taskId: String) = "$TaskDetail/$taskId"
    fun editExpense(expenseId: String) = "$EditExpense/$expenseId"
    fun notificationDetail(notificationId: Int) = "$NotificationDetail/$notificationId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelResId: Int = 0
) {
    companion object {
        private val homeItem = BottomNavItem(
            route = AppRoutes.Home,
            label = "Inicio",
            selectedIcon = Icons.Rounded.Home,
            unselectedIcon = Icons.Outlined.Home,
            labelResId = R.string.nav_home
        )
        private val academicItem = BottomNavItem(
            route = AppRoutes.Academic,
            label = "Académico",
            selectedIcon = Icons.AutoMirrored.Rounded.MenuBook,
            unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
            labelResId = R.string.nav_academic
        )
        private val calendarItem = BottomNavItem(
            route = AppRoutes.Calendar,
            label = "Horario",
            selectedIcon = Icons.Rounded.CalendarMonth,
            unselectedIcon = Icons.Outlined.CalendarMonth,
            labelResId = R.string.nav_schedule
        )
        private val expensesItem = BottomNavItem(
            route = AppRoutes.Expenses,
            label = "Gastos",
            selectedIcon = Icons.Rounded.AccountBalanceWallet,
            unselectedIcon = Icons.Outlined.AccountBalanceWallet,
            labelResId = R.string.nav_expenses
        )
        /*
         * La quinta pestaña es la configuración, no el perfil.
         *
         * El perfil era una parada intermedia: se entraba a una tarjeta con el nombre y la
         * foto y desde ahí se tocaba «Configuración» para llegar a lo que se venía a hacer.
         * Ahora la pestaña abre directamente la configuración, como pantalla propia y no como
         * subpantalla de nadie, y el perfil pasa a ser una fila más dentro de ella.
         *
         * El rótulo dice «Ajustes» y no «Configuración» porque en una barra de cinco no cabe:
         * la palabra larga se corta en «Configuraci…». El título de la pantalla sí lo dice
         * entero.
         */
        private val settingsItem = BottomNavItem(
            route = AppRoutes.Settings,
            label = "Ajustes",
            selectedIcon = Icons.Rounded.Settings,
            unselectedIcon = Icons.Outlined.Settings,
            labelResId = R.string.nav_settings
        )

        val items = listOf(homeItem, academicItem, calendarItem, expensesItem, settingsItem)

        fun itemsFor(enabledModules: Set<AppModule>): List<BottomNavItem> {
            return buildList {
                add(homeItem)
                if (AppModule.GRADES in enabledModules || AppModule.TASKS in enabledModules) add(academicItem)
                add(calendarItem)
                if (AppModule.EXPENSES in enabledModules) add(expensesItem)
                add(settingsItem)
            }
        }
    }
}

/**
 * El icono que toca a esta pestana, con el trazo elegido en Componentes.
 *
 * Material trae los juegos ya dibujados: `Rounded` es el redondeado, `Outlined` el lineal.
 * «Relleno» usa el redondeado en los dos estados —activo e inactivo—, que es lo que deja la
 * barra distinguiendose solo por el color, sin cambio de peso.
 */
fun BottomNavItem.iconFor(selected: Boolean, style: IconStyle): ImageVector = when (style) {
    IconStyle.REDONDEADO -> if (selected) selectedIcon else unselectedIcon
    IconStyle.LINEAL -> unselectedIcon
    IconStyle.RELLENO -> selectedIcon
}
