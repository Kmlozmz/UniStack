@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.SectionHeader
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniSearchField
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.navigation.AppRoutes
import com.unistack.app.feature_tasks.presentation.TasksScreen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.ui.Alignment
import com.unistack.app.core.design.components.UniStackFabMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.feature_tasks.presentation.TasksViewModel
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

private enum class AcademicTab(@StringRes val labelRes: Int) {
    SUBJECTS(R.string.academic_tab_subjects),
    TASKS(R.string.academic_tab_tasks);

    val label: String
        @Composable get() = stringResource(labelRes)
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
    onEditSubjectClick: (String) -> Unit,
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
    // Cuántas materias hay marcadas abajo. Con la barra de selección puesta, el botón de crear
    // se retira: los dos ocupan la misma esquina y crear no es lo que vas a hacer mientras
    // tienes materias marcadas.
    var markedSubjects by remember { mutableIntStateOf(0) }
    val pendingTasks = tasks.count { !it.completed }

    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }

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
                modifier = Modifier.padding(
                    start = LocalInterfaceSpacing.current.screenHorizontal,
                    top = 18.dp,
                    end = 8.dp,
                    bottom = 8.dp
                ),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                SectionHeader(
                    title = stringResource(R.string.academic_header_title),
                    subtitle = stringResource(R.string.academic_header_subtitle),
                    modifier = Modifier.padding(end = 12.dp),
                    action = {
                        /*
                         * La lupa solo en Materias.
                         *
                         * Tareas trae su propio buscador dentro, así que aquí era un icono que
                         * duplicaba uno y, en la otra pestaña, uno que no hacía nada.
                         */
                        if (selectedTab == AcademicTab.SUBJECTS) {
                            UniIconButton(
                                icon = if (searching) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = if (searching) stringResource(R.string.academic_search_close) else stringResource(R.string.academic_search_open),
                                onClick = {
                                    searching = !searching
                                    if (!searching) query = ""
                                }
                            )
                        }
                    }
                )
                // El buscador va debajo del apoyo, no en su sitio: sustituirlo cambiaba la
                // altura de la cabecera y empujaba la pantalla entera al abrir la lupa.
                AnimatedVisibility(visible = searching && selectedTab == AcademicTab.SUBJECTS) {
                    UniSearchField(
                        query = query,
                        onQueryChange = { query = it },
                        placeholder = stringResource(R.string.academic_search_placeholder),
                        modifier = Modifier.padding(top = 8.dp, end = 12.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
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
                        onEditSubjectClick = onEditSubjectClick,
                        onSelectionChange = { markedSubjects = it },
                        embedded = true,
                        nameQuery = query
                    )
                    AcademicTab.TASKS -> TasksScreen(
                        onNewTaskClick = onNewTaskClick,
                        onEditTaskClick = onEditTaskClick,
                        onCompleteHistoryClick = onCompleteHistoryClick,
                        onSubjectClick = onSubjectClick,
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
        if (markedSubjects == 0) {
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
}
