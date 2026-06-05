package com.unistack.app.feature_grades.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.GradingScale

private val FormCardShape = RoundedCornerShape(14.dp)
private val FormFieldColor: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) UniStackColors.SurfaceVariant else UniStackColors.Card

private val DisabledButtonColor: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF1A2230) else UniStackColors.SurfaceVariant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGradeScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel(),
    gradeId: String? = null,
    initialPeriodId: String? = null
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val grade = gradeId?.let { id -> subject?.grades?.firstOrNull { it.id == id } }
    val isEditing = gradeId != null
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(GradeType.WORKSHOP) }
    var selectedPeriodId by remember { mutableStateOf(initialPeriodId ?: "period-1") }
    var initialized by remember(subjectId, gradeId) { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: GradingScaleUtils.maxGradeFor(scale)
    val maxGradeLabel = GradingScaleUtils.formatGrade(maxGrade, scale)
    val periodScheme = profile?.academicPeriodScheme ?: com.unistack.app.feature_user.domain.AcademicPeriodScheme.default()
    val lockedPeriod = initialPeriodId?.let { id -> periodScheme.periods.firstOrNull { it.id == id } }
    val selectedPeriod = periodScheme.periods.firstOrNull { it.id == selectedPeriodId }
        ?: lockedPeriod
        ?: periodScheme.periods.first()

    val gradeValue = value.toDoubleOrNull()
    val percentageValue = percentage.toDoubleOrNull()
    val currentPercentage = subject?.grades
        ?.filterNot { it.id == gradeId }
        ?.filter { it.periodId == selectedPeriod.id }
        ?.sumOf { it.percentage } ?: 0.0
    val totalPercentage = currentPercentage + (percentageValue ?: 0.0) / 100.0

    val nameValidation = TextValidators.validateActivityName(name)
    val isNameValid = name.isBlank() || nameValidation.isValid
    val isGradeValid = gradeValue != null && gradeValue in 0.0..maxGrade
    val isPercentageValid = percentageValue != null && percentageValue > 0.0 && totalPercentage <= 1.00001

    val isValid = subject != null &&
        (!isEditing || grade != null) &&
        name.isNotBlank() &&
        nameValidation.isValid &&
        isGradeValid &&
        isPercentageValid

    LaunchedEffect(grade?.id, subjectId, gradeId) {
        if (initialized) return@LaunchedEffect
        if (grade != null) {
            name = grade.name
            value = GradingScaleUtils.formatGrade(grade.value, scale)
            percentage = String.format(java.util.Locale.US, "%.0f", grade.percentage * 100)
            selectedType = grade.type
            selectedPeriodId = grade.periodId
            initialized = true
        } else if (!isEditing) {
            lockedPeriod?.let { selectedPeriodId = it.id }
            initialized = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.Start)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    tint = UniStackColors.TextPrimary
                )
            }

            Text(
                text = if (isEditing) "Editar nota" else "Nueva nota",
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "${periodDisplayName(selectedPeriod)} · ${formatPercent(selectedPeriod.weight * 100)}% de la materia",
                color = UniStackColors.Green,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Activity Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Actividad",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(50)
                        error = null
                    },
                    placeholder = { Text("Ej. Taller, Exposición, Parcial...", color = UniStackColors.TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormCardShape,
                    isError = !isNameValid,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FormFieldColor,
                        unfocusedContainerColor = FormFieldColor,
                        disabledContainerColor = FormFieldColor,
                        focusedBorderColor = UniStackColors.Primary,
                        unfocusedBorderColor = UniStackColors.SoftOutline,
                        errorBorderColor = UniStackColors.Coral,
                        focusedTextColor = UniStackColors.TextPrimary,
                        unfocusedTextColor = UniStackColors.TextPrimary,
                        errorTextColor = UniStackColors.TextPrimary,
                        focusedLabelColor = UniStackColors.Primary,
                        unfocusedLabelColor = UniStackColors.TextSecondary,
                        focusedPlaceholderColor = UniStackColors.TextSecondary,
                        unfocusedPlaceholderColor = UniStackColors.TextSecondary
                    ),
                    supportingText = {
                        if (!isNameValid) {
                            Text(nameValidation.errorMessage ?: "Ingresa un nombre de actividad válido", color = UniStackColors.Coral)
                        }
                    }
                )
            }

            // Grade Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Nota obtenida",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        error = null
                    },
                    placeholder = { Text("0.0", color = UniStackColors.TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormCardShape,
                    isError = value.isNotBlank() && !isGradeValid,
                    trailingIcon = {
                        Text(
                            text = "/ $maxGradeLabel",
                            color = UniStackColors.TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FormFieldColor,
                        unfocusedContainerColor = FormFieldColor,
                        disabledContainerColor = FormFieldColor,
                        focusedBorderColor = UniStackColors.Primary,
                        unfocusedBorderColor = UniStackColors.SoftOutline,
                        errorBorderColor = UniStackColors.Coral,
                        focusedTextColor = UniStackColors.TextPrimary,
                        unfocusedTextColor = UniStackColors.TextPrimary,
                        errorTextColor = UniStackColors.TextPrimary,
                        focusedPlaceholderColor = UniStackColors.TextSecondary,
                        unfocusedPlaceholderColor = UniStackColors.TextSecondary
                    )
                )
            }

            // Weight Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Peso dentro del corte (%)",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                OutlinedTextField(
                    value = percentage,
                    onValueChange = {
                        percentage = it
                        error = null
                    },
                    placeholder = { Text("0", color = UniStackColors.TextSecondary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormCardShape,
                    isError = percentage.isNotBlank() && !isPercentageValid,
                    trailingIcon = {
                        Text(
                            text = "%",
                            color = UniStackColors.TextSecondary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    supportingText = {
                        Text(
                            text = "La suma de pesos debe ser 100%.",
                            color = if (percentage.isNotBlank() && !isPercentageValid) UniStackColors.Coral else UniStackColors.TextSecondary
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = FormFieldColor,
                        unfocusedContainerColor = FormFieldColor,
                        disabledContainerColor = FormFieldColor,
                        focusedBorderColor = UniStackColors.Primary,
                        unfocusedBorderColor = UniStackColors.SoftOutline,
                        errorBorderColor = UniStackColors.Coral,
                        focusedTextColor = UniStackColors.TextPrimary,
                        unfocusedTextColor = UniStackColors.TextPrimary,
                        errorTextColor = UniStackColors.TextPrimary,
                        focusedPlaceholderColor = UniStackColors.TextSecondary,
                        unfocusedPlaceholderColor = UniStackColors.TextSecondary
                    )
                )
            }

            // Activity type chips
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Tipo de actividad (opcional)",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                val chips = listOf(
                    "Taller" to GradeType.WORKSHOP,
                    "Exposición" to GradeType.PRESENTATION,
                    "Quiz" to GradeType.QUIZ,
                    "Parcial" to GradeType.EXAM,
                    "Proyecto" to GradeType.PROJECT,
                    "Investigación" to GradeType.RESEARCH,
                    "Práctica" to GradeType.PRACTICE,
                    "Otra" to GradeType.OTHER
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chips.forEach { (label, type) ->
                        ActivityChip(
                            label = label,
                            isSelected = selectedType == type,
                            onClick = {
                                selectedType = type
                                name = label
                                error = null
                            }
                        )
                    }
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = UniStackColors.Coral,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val editingGradeId = gradeId
                    val saved = if (editingGradeId != null) {
                        viewModel.updateGrade(
                            subjectId = subjectId,
                            gradeId = editingGradeId,
                            name = TextValidators.normalizeText(name),
                            value = gradeValue ?: 0.0,
                            percentageInput = percentageValue ?: 0.0,
                            type = selectedType,
                            periodId = selectedPeriod.id
                        )
                    } else {
                        viewModel.addGrade(
                            subjectId = subjectId,
                            name = TextValidators.normalizeText(name),
                            value = gradeValue ?: 0.0,
                            percentageInput = percentageValue ?: 0.0,
                            type = selectedType,
                            periodId = selectedPeriod.id
                        )
                    }
                    if (saved) {
                        onBackClick()
                    } else {
                        error = "Revisa que la nota esté entre 0 y $maxGradeLabel y que el porcentaje acumulado no supere 100% en ${periodDisplayName(selectedPeriod)}."
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UniStackColors.Primary,
                    contentColor = Color.White,
                    disabledContainerColor = DisabledButtonColor,
                    disabledContentColor = UniStackColors.TextSecondary
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isEditing) "Guardar cambios" else "Guardar nota",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isValid) Color.White else UniStackColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ActivityChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .bounceClick(onClick)
            .background(
                color = if (isSelected) UniStackColors.Primary else FormFieldColor,
                shape = FormCardShape
            )
            .border(
                width = 1.dp,
                color = if (isSelected) UniStackColors.Primary else UniStackColors.SoftOutline,
                shape = FormCardShape
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else UniStackColors.TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun periodDisplayName(period: AcademicPeriod): String = "Corte ${period.order}"

private fun formatPercent(value: Double): String = String.format(Locale.US, "%.0f", value)
