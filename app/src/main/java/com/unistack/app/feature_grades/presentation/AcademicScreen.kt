@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.navigation.AppRoutes
import com.unistack.app.feature_tasks.presentation.TasksScreen
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.Alignment
import com.unistack.app.core.design.components.UniStackFabMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.feature_tasks.presentation.TasksViewModel

private enum class AcademicTab(val label: String) {
    SUBJECTS("Materias"),
    TASKS("Tareas")
}

/**
 * @param initialTab pestaña con la que abrir, tal y como venga en la ruta. Quien navega
 *   decide: la casilla «Pendientes» de Inicio pide Tareas y la de «Materias» pide Materias.
 *   Cambiarla a mano después sigue funcionando; el valor de la ruta solo fija el arranque.
 */
@Composable
fun AcademicScreen(
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onNewTaskClick: () -> Unit,
    onEditTaskClick: (String) -> Unit,
    onCompleteHistoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialTab: String? = null,
    tasksViewModel: TasksViewModel = hiltViewModel()
) {
    // La clave es la pestaña pedida: al volver a navegar aquí con otra distinta, el estado se
    // reinicia y manda la nueva. Sin clave, la primera elección quedaba congelada y las
    // llamadas posteriores no tenían efecto.
    // Cuántas tareas quedan sin hacer, para la insignia del selector.
    val tasks by tasksViewModel.tasks.collectAsStateWithLifecycle()
    val pendingTasks = tasks.count { !it.completed }

    var selectedTab by rememberSaveable(initialTab) {
        mutableStateOf(
            when (initialTab) {
                AppRoutes.AcademicTabTasks -> AcademicTab.TASKS
                else -> AcademicTab.SUBJECTS
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(start = 20.dp, top = 18.dp, end = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Académico",
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { /* pendiente: buscar entre materias y tareas */ }) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                UniSegmentedControl(
                    selected = selectedTab,
                    options = AcademicTab.entries.map { tab ->
                        UniSegmentedOption(
                            value = tab,
                            label = tab.label,
                            icon = if (tab == AcademicTab.SUBJECTS) {
                                Icons.AutoMirrored.Rounded.MenuBook
                            } else {
                                Icons.AutoMirrored.Rounded.Assignment
                            },
                            badge = if (tab == AcademicTab.TASKS) pendingTasks else null
                        )
                    },
                    onSelected = { selectedTab = it },
                    modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    AcademicTab.SUBJECTS -> GradesScreen(
                        onAddSubjectClick = onAddSubjectClick,
                        onSubjectClick = onSubjectClick,
                        embedded = true
                    )
                    AcademicTab.TASKS -> TasksScreen(
                        onNewTaskClick = onNewTaskClick,
                        onEditTaskClick = onEditTaskClick,
                        onCompleteHistoryClick = onCompleteHistoryClick,
                        embedded = true
                    )
                }
            }
        }

        /*
         * El botón de crear, aquí a modo de prueba.
         *
         * Cada pestaña ya trae su propia acción anclada —«Agregar materia», «Nueva tarea»—, así
         * que este menú repite lo que ya hay abajo y además lo tapa. Se deja puesto para verlo
         * en el móvil y decidir; si se queda, lo que sobra es el botón anclado de cada pestaña.
         */
        UniStackFabMenu(
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp),
            onAddGradeClick = onAddSubjectClick,
            onAddTaskClick = onNewTaskClick,
            onAddExpenseClick = {},
            onAddSubjectClick = onAddSubjectClick,
            showAddGrade = false,
            showAddTask = true,
            showAddExpense = false,
            showAddSubject = true
        )
    }
}
