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
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.navigation.AppRoutes
import com.unistack.app.feature_tasks.presentation.TasksScreen

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
    initialTab: String? = null
) {
    // La clave es la pestaña pedida: al volver a navegar aquí con otra distinta, el estado se
    // reinicia y manda la nueva. Sin clave, la primera elección quedaba congelada y las
    // llamadas posteriores no tenían efecto.
    var selectedTab by rememberSaveable(initialTab) {
        mutableStateOf(
            when (initialTab) {
                AppRoutes.AcademicTabTasks -> AcademicTab.TASKS
                else -> AcademicTab.SUBJECTS
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Académico",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Materias, notas y entregas en un mismo lugar.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                        }
                    )
                },
                onSelected = { selectedTab = it },
                modifier = Modifier.fillMaxWidth()
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
}
