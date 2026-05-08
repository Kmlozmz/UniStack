package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale

@Composable
fun AddGradeScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel(),
    gradeId: String? = null
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val grade = gradeId?.let { id -> subject?.grades?.firstOrNull { it.id == id } }
    val isEditing = gradeId != null
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf("") }
    var initialized by remember(subjectId, gradeId) { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = GradingScaleUtils.maxGradeFor(scale)
    val maxGradeLabel = GradingScaleUtils.formatGrade(maxGrade, scale)

    val gradeValue = value.toDoubleOrNull()
    val percentageValue = percentage.toDoubleOrNull()
    val currentPercentage = subject?.grades
        ?.filterNot { it.id == gradeId }
        ?.sumOf { it.percentage } ?: 0.0
    val totalPercentage = currentPercentage + (percentageValue ?: 0.0) / 100.0

    val nameValidation = TextValidators.validateActivityName(name)
    val isNameValid = name.isBlank() || nameValidation.isValid
    val isGradeValid = gradeValue != null && gradeValue in 0.0..maxGrade
    val isPercentageValid = percentageValue != null && percentageValue > 0.0 && totalPercentage <= 1.00001

    val isValid = subject != null &&
        (!isEditing || grade != null) &&
        nameValidation.isValid &&
        isGradeValid &&
        isPercentageValid

    LaunchedEffect(grade?.id, subjectId, gradeId) {
        if (initialized) return@LaunchedEffect
        if (grade != null) {
            name = grade.name
            value = GradingScaleUtils.formatGrade(grade.value, scale)
            percentage = String.format(java.util.Locale.US, "%.0f", grade.percentage * 100)
            initialized = true
        } else if (!isEditing) {
            initialized = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
        }
        Text(
            text = if (isEditing) "Editar nota" else "Agregar nota",
            color = UniStackColors.TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        UniCard(
            modifier = Modifier.fillMaxWidth(),
            color = UniStackColors.PrimaryLight,
            shape = AppShapes.LargeCard,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Rounded.EditNote, contentDescription = null, tint = UniStackColors.Primary)
                Text(subject?.name ?: "Materia", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                if (isEditing && grade == null) {
                    Text("Nota no encontrada.", color = UniStackColors.TextSecondary)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(50)
                        error = null
                    },
                    label = { Text("Actividad") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    isError = !isNameValid,
                    supportingText = {
                        if (!isNameValid) {
                            Text(nameValidation.errorMessage ?: "Ingresa un nombre de actividad válido")
                        }
                    }
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        error = null
                    },
                    label = { Text("Nota 0 a $maxGradeLabel") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    isError = value.isNotBlank() && !isGradeValid
                )
                OutlinedTextField(
                    value = percentage,
                    onValueChange = {
                        percentage = it
                        error = null
                    },
                    label = { Text("Porcentaje") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    isError = percentage.isNotBlank() && !isPercentageValid
                )
                error?.let {
                    Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(
            onClick = {
                val saved = if (isEditing && gradeId != null) {
                    viewModel.updateGrade(
                        subjectId = subjectId,
                        gradeId = gradeId,
                        name = TextValidators.normalizeText(name),
                        value = gradeValue ?: 0.0,
                        percentageInput = percentageValue ?: 0.0
                    )
                } else {
                    viewModel.addGrade(
                        subjectId = subjectId,
                        name = TextValidators.normalizeText(name),
                        value = gradeValue ?: 0.0,
                        percentageInput = percentageValue ?: 0.0
                    )
                }
                if (saved) {
                    onBackClick()
                } else {
                    error = "Revisa que la nota esté entre 0 y $maxGradeLabel y que el porcentaje acumulado no supere 100%."
                }
            },
            enabled = isValid,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary)
        ) {
            Text(if (isEditing) "Guardar cambios" else "Guardar nota")
        }
    }
}
