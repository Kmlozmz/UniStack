package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.UniStackTheme
import androidx.compose.ui.tooling.preview.Preview
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale

@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onAddGradeClick: (String) -> Unit,
    onEditSubjectClick: (String) -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    onSubjectDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel()
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsState()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val profile by viewModel.userProfile.collectAsState()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    var showSubjectMenu by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }
    var gradeIdPendingDelete by remember { mutableStateOf<String?>(null) }

    if (subject == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(UniStackColors.Background)
                .padding(20.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Text("Materia no encontrada", color = UniStackColors.TextPrimary)
        }
        return
    }

    val average = viewModel.currentAverage(subject)
    val evaluated = viewModel.evaluatedPercentage(subject)
    val needed = viewModel.neededGrade(subject)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    subject.name,
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { showSubjectMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Opciones de materia")
                    }
                    DropdownMenu(
                        expanded = showSubjectMenu,
                        onDismissRequest = { showSubjectMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar materia") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                            onClick = {
                                showSubjectMenu = false
                                onEditSubjectClick(subject.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar materia", color = UniStackColors.Coral) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = UniStackColors.Coral
                                )
                            },
                            onClick = {
                                showSubjectMenu = false
                                showDeleteSubjectDialog = true
                            }
                        )
                    }
                }
            }
        }
        item {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = subjectBackground(subject.visualType),
                shape = AppShapes.LargeCard
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Promedio actual", color = UniStackColors.TextSecondary)
                        Text(GradingScaleUtils.formatGrade(average, scale), color = UniStackColors.TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("${String.format("%.0f", evaluated)}% evaluado", color = subjectAccent(subject.visualType), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        item {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.YellowLight,
                shape = AppShapes.MediumCard
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = UniStackColors.Yellow)
                    Text(
                        text = if (subject.grades.isEmpty()) {
                            "Agrega una nota para calcular cuánto necesitas."
                        } else if (needed == null || needed.isNaN() || needed <= 0.0) {
                            "Ya no queda porcentaje disponible."
                        } else {
                            "Para terminar con ${GradingScaleUtils.formatGrade(subject.targetAverage, scale)} necesitas ${GradingScaleUtils.formatGrade(needed, scale)}."
                        },
                        color = UniStackColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
        }
        if (subject.grades.isEmpty()) {
            item {
                UniCard(modifier = Modifier.fillMaxWidth(), color = UniStackColors.Card, shape = AppShapes.MediumCard) {
                    Text("Agrega tu primera nota para calcular tu promedio.", color = UniStackColors.TextSecondary)
                }
            }
        } else {
            items(subject.grades, key = { it.id }) { grade ->
                UniCard(modifier = Modifier.fillMaxWidth(), color = UniStackColors.Card, shape = AppShapes.MediumCard) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(grade.name, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                            Text("${String.format("%.0f", grade.percentage * 100)}%", color = UniStackColors.TextSecondary)
                        }
                        Text(GradingScaleUtils.formatGrade(grade.value, scale), color = UniStackColors.Primary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        IconButton(onClick = { onEditGradeClick(subject.id, grade.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Editar nota")
                        }
                        IconButton(onClick = { gradeIdPendingDelete = grade.id }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Eliminar nota",
                                tint = UniStackColors.Coral
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = { onAddGradeClick(subject.id) },
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(3.dp))
                Text("Agregar nota")
            }
        }
    }

    if (showDeleteSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            title = { Text("¿Eliminar materia?") },
            text = { Text("También se eliminarán sus notas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteSubjectDialog = false
                        if (viewModel.deleteSubject(subject.id)) {
                            onSubjectDeleted()
                        }
                    }
                ) {
                    Text("Eliminar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = UniStackColors.Card
        )
    }

    gradeIdPendingDelete?.let { gradeId ->
        AlertDialog(
            onDismissRequest = { gradeIdPendingDelete = null },
            title = { Text("¿Eliminar nota?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGrade(subject.id, gradeId)
                        gradeIdPendingDelete = null
                    }
                ) {
                    Text("Eliminar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { gradeIdPendingDelete = null }) {
                    Text("Cancelar")
                }
            },
            containerColor = UniStackColors.Card
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SubjectDetailScreenPreview() {
    UniStackTheme {
        SubjectDetailScreen(
            subjectId = "1",
            onBackClick = {},
            onAddGradeClick = {},
            onEditSubjectClick = {},
            onEditGradeClick = { _, _ -> },
            onSubjectDeleted = {}
        )
    }
}
