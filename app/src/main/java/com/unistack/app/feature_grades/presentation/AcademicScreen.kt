package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_tasks.presentation.TasksScreen

private enum class AcademicTab(val label: String) {
    SUBJECTS("Materias"),
    TASKS("Tareas")
}

@Composable
fun AcademicScreen(
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onNewTaskClick: () -> Unit,
    onEditTaskClick: (String) -> Unit,
    onCompleteHistoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(AcademicTab.SUBJECTS) }

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
                color = UniStackColors.TextSecondary
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.MediumCard,
                color = UniStackColors.SurfaceVariant
            ) {
                Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    AcademicTab.entries.forEach { tab ->
                        val selected = selectedTab == tab
                        Surface(
                            onClick = { selectedTab = tab },
                            modifier = Modifier.weight(1f),
                            shape = AppShapes.SmallCard,
                            color = if (selected) UniStackColors.Card else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (tab == AcademicTab.SUBJECTS) {
                                        Icons.AutoMirrored.Rounded.MenuBook
                                    } else {
                                        Icons.AutoMirrored.Rounded.Assignment
                                    },
                                    contentDescription = null,
                                    tint = if (selected) UniStackColors.Primary else UniStackColors.TextSecondary
                                )
                                Text(
                                    text = tab.label,
                                    modifier = Modifier.padding(start = 8.dp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (selected) UniStackColors.TextPrimary else UniStackColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
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
