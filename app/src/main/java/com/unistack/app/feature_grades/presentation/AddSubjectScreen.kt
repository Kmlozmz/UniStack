package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.domain.UserPlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddSubjectScreen(
    onBackClick: () -> Unit,
    onSubjectSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel(),
    subjectId: String? = null,
    onUpgradeClick: () -> Unit = {}
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let { GradingScaleUtils.maxGradeFor(it.gradingScale) } ?: 5.0
    val maxGradeLabel = GradingScaleUtils.formatGrade(maxGrade, scale)
    val defaultAverage = profile?.targetAverage ?: 4.0
    val isEditing = subjectId != null
    val subject = subjectId?.let { id -> subjects.firstOrNull { it.id == id } }
    val userPlan = FeatureGate.freePlan
    val freeLimitReached = !isEditing && !FeatureGate.canCreateSubject(userPlan, subjects.size)

    var name by remember { mutableStateOf("") }
    var targetAverage by remember { mutableStateOf("") }
    var visualType by remember { mutableStateOf(SubjectVisualType.TEAL) }
    var initialized by remember(subjectId) { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val targetValue = targetAverage.toDoubleOrNull()
    val nameValidation = TextValidators.validateSubjectName(name)
    val isNameValid = name.isBlank() || nameValidation.isValid
    val canEditLoadedSubject = !isEditing || subject != null
    val isValid = canEditLoadedSubject &&
        !freeLimitReached &&
        nameValidation.isValid &&
        targetValue != null &&
        targetValue in 0.0..maxGrade

    LaunchedEffect(subject?.id, defaultAverage, scale, subjectId) {
        if (initialized) return@LaunchedEffect

        if (subject != null) {
            name = subject.name
            targetAverage = GradingScaleUtils.formatGrade(subject.targetAverage, scale)
            visualType = subject.visualType
            initialized = true
        } else if (!isEditing) {
            targetAverage = GradingScaleUtils.formatGrade(defaultAverage, scale)
            initialized = true
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UniStackColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UniStackColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = if (isEditing) "Editar materia" else "Agregar materia",
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            if (isEditing && subject == null) {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = UniStackColors.Card,
                    shape = AppShapes.MediumCard
                ) {
                    Text("Materia no encontrada.", color = UniStackColors.TextSecondary)
                }
            }
            if (!isEditing) {
                SubjectPlanGateCard(
                    plan = userPlan,
                    currentSubjectCount = subjects.size,
                    onUpgradeClick = onUpgradeClick
                )
            }
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.Card,
                shape = AppShapes.LargeCard,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it.take(40)
                            error = null
                        },
                        label = { Text("Nombre") },
                        placeholder = { Text("Cálculo, Derecho civil, Biología...") },
                        singleLine = true,
                        shape = AppShapes.MediumCard,
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isNameValid,
                        supportingText = {
                            if (!isNameValid) {
                                Text(nameValidation.errorMessage ?: "Ingresa un nombre de materia válido")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = targetAverage,
                        onValueChange = {
                            targetAverage = it
                            error = null
                        },
                        label = { Text("Meta de promedio (0 a $maxGradeLabel)") },
                        singleLine = true,
                        shape = AppShapes.MediumCard,
                        modifier = Modifier.fillMaxWidth(),
                        isError = targetAverage.isNotBlank() && (targetValue == null || targetValue !in 0.0..maxGrade)
                    )
                    Text("Color", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                    SubjectVisualType.values().toList().chunked(6).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { type ->
                                ColorSwatch(
                                    color = subjectAccent(type),
                                    label = type.accessibilityLabel(),
                                    selected = visualType == type,
                                    onClick = { visualType = type }
                                )
                            }
                        }
                    }
                    error?.let {
                        Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Button(
                onClick = {
                    if (freeLimitReached) {
                        error = "Alcanzaste el límite gratis de ${userPlan.maxSubjects} materias."
                        return@Button
                    }

                    val savedSubjectId = if (isEditing && subjectId != null) {
                        val saved = viewModel.updateSubject(
                            subjectId = subjectId,
                            name = TextValidators.normalizeText(name),
                            targetAverage = targetValue ?: defaultAverage,
                            visualType = visualType
                        )
                        if (saved) subjectId else null
                    } else {
                        viewModel.addSubject(
                            name = TextValidators.normalizeText(name),
                            targetAverage = targetValue ?: defaultAverage,
                            visualType = visualType
                        )?.id
                    }

                    if (savedSubjectId == null) {
                        error = "Revisa el nombre y la meta antes de guardar."
                    } else {
                        scope.launch {
                            launch {
                                snackbarHostState.showSnackbar(
                                    if (isEditing) "Materia actualizada correctamente" else "Materia creada correctamente"
                                )
                            }
                            delay(650)
                            onSubjectSaved(savedSubjectId)
                        }
                    }
                },
                enabled = isValid,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Guardar cambios" else "Guardar materia")
            }
        }
    }
}

@Composable
private fun SubjectPlanGateCard(
    plan: UserPlan,
    currentSubjectCount: Int,
    onUpgradeClick: () -> Unit
) {
    val remaining = FeatureGate.remainingSubjects(plan, currentSubjectCount) ?: Int.MAX_VALUE
    val limitReached = remaining == 0

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = if (limitReached) UniStackColors.CoralLight else UniStackColors.PrimaryLight,
        shape = AppShapes.MediumCard,
        tonalElevation = if (limitReached) 3.dp else 0.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (limitReached) UniStackColors.Coral else UniStackColors.Primary)
                    .padding(9.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White)
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (limitReached) "Límite gratis alcanzado" else "Plan ${plan.name}",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (limitReached) {
                        "${plan.maxSubjects} materias incluidas. Pro desbloqueará materias ilimitadas."
                    } else {
                        "$currentSubjectCount de ${plan.maxSubjects} materias usadas."
                    },
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            if (limitReached) {
                Button(
                    onClick = onUpgradeClick,
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Ver Pro", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, label: String, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = if (selected) 1f else 0.22f))
            .clickable(
                onClickLabel = "Seleccionar color $label",
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics {
                contentDescription = "Color $label"
                stateDescription = if (selected) "Seleccionado" else "No seleccionado"
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
        }
    }
}

private fun SubjectVisualType.accessibilityLabel(): String = when (this) {
    SubjectVisualType.TEAL -> "turquesa"
    SubjectVisualType.BLUE -> "azul"
    SubjectVisualType.CORAL -> "coral"
    SubjectVisualType.PURPLE -> "morado"
    SubjectVisualType.GREEN -> "verde"
    SubjectVisualType.YELLOW -> "amarillo"
    SubjectVisualType.ROSE -> "rosa"
    SubjectVisualType.INDIGO -> "indigo"
    SubjectVisualType.ORANGE -> "naranja"
    SubjectVisualType.CYAN -> "cian"
    SubjectVisualType.LIME -> "lima"
    SubjectVisualType.SLATE -> "gris azulado"
}
