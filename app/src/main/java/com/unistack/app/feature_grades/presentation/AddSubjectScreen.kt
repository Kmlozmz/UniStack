package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.toArgb
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
import kotlin.math.roundToInt

private val SubjectFormCardShape = RoundedCornerShape(10.dp)

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
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val maxGradeLabel = GradingScaleUtils.formatGrade(maxGrade, scale)
    val defaultAverage = profile?.targetAverage ?: 4.0
    val isEditing = subjectId != null
    val subject = subjectId?.let { id -> subjects.firstOrNull { it.id == id } }
    val userPlan = FeatureGate.planFor(isPro = false)
    val freeLimitReached = !isEditing && !FeatureGate.canCreateSubject(userPlan, subjects.size)

    var name by remember { mutableStateOf("") }
    var targetAverage by remember { mutableStateOf("") }
    var visualType by remember { mutableStateOf(SubjectVisualType.TEAL) }
    var customColor by remember { mutableStateOf<Int?>(subjectAccent(SubjectVisualType.TEAL).toArgb()) }
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
            customColor = subject.customColor ?: subjectAccent(subject.visualType).toArgb()
            initialized = true
        } else if (!isEditing) {
            targetAverage = GradingScaleUtils.formatGrade(defaultAverage, scale)
            customColor = subjectAccent(visualType).toArgb()
            initialized = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubjectFormHeader(
                title = if (isEditing) "Editar materia" else "Agregar materia",
                onBackClick = onBackClick
            )
            if (isEditing && subject == null) {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = SubjectFormCardShape,
                    tonalElevation = 0.dp,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                    borderWidth = 0.5.dp
                ) {
                    Text("Materia no encontrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!isEditing && FeatureGate.PRO_FEATURES_ENABLED) {
                PlanBanner(
                    plan = userPlan,
                    currentSubjectCount = subjects.size,
                    onUpgradeClick = onUpgradeClick
                )
            }
            SubjectBasicInfoCard(
                name = name,
                onNameChange = {
                    name = it.take(40)
                    error = null
                },
                nameIsValid = isNameValid,
                nameError = nameValidation.errorMessage,
                targetAverage = targetAverage,
                targetLabel = maxGradeLabel,
                targetHasError = targetAverage.isNotBlank() && (targetValue == null || targetValue !in 0.0..maxGrade),
                onTargetChange = {
                    targetAverage = it
                    error = null
                }
            )
            SubjectColorPicker(
                selectedColor = customColor ?: subjectAccent(visualType).toArgb(),
                onSelected = { color ->
                    customColor = color
                    visualType = closestVisualType(Color(color))
                }
            )
            SubjectPreviewCard(
                name = name,
                targetAverage = targetAverage.ifBlank { GradingScaleUtils.formatGrade(defaultAverage, scale) },
                visualType = visualType,
                customColor = customColor
            )
            error?.let {
                Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
            }
            SaveSubjectButton(
                text = if (isEditing) "Guardar cambios" else "Guardar materia",
                enabled = isValid,
                onClick = {
                    if (freeLimitReached) {
                        error = "Alcanzaste el límite gratis de ${userPlan.maxSubjects} materias."
                        return@SaveSubjectButton
                    }

                    val editingSubjectId = subjectId
                    val savedSubjectId = if (editingSubjectId != null) {
                        val saved = viewModel.updateSubject(
                            subjectId = editingSubjectId,
                            name = TextValidators.normalizeText(name),
                            targetAverage = targetValue ?: defaultAverage,
                            visualType = visualType,
                            customColor = customColor
                        )
                        if (saved) editingSubjectId else null
                    } else {
                        viewModel.addSubject(
                            name = TextValidators.normalizeText(name),
                            targetAverage = targetValue ?: defaultAverage,
                            visualType = visualType,
                            customColor = customColor
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
                modifier = Modifier.fillMaxWidth()
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SubjectFormHeader(
    title: String,
    onBackClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f), AppShapes.Pill)
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
        }
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun PlanBanner(
    plan: UserPlan,
    currentSubjectCount: Int,
    onUpgradeClick: () -> Unit
) {
    val remaining = FeatureGate.remainingSubjects(plan, currentSubjectCount) ?: Int.MAX_VALUE
    val limitReached = remaining == 0

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = SubjectFormCardShape,
        tonalElevation = 0.dp,
        borderColor = if (limitReached) UniStackColors.Coral.copy(alpha = 0.36f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (limitReached) UniStackColors.Coral.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        AppShapes.SmallCard
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = if (limitReached) UniStackColors.Coral else MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (limitReached) "Límite gratis alcanzado" else "Plan ${plan.name}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (limitReached) {
                        "${plan.maxSubjects} materias incluidas. Pro desbloqueará materias ilimitadas."
                    } else {
                        "$currentSubjectCount de ${plan.maxSubjects} materias usadas."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun SubjectBasicInfoCard(
    name: String,
    onNameChange: (String) -> Unit,
    nameIsValid: Boolean,
    nameError: String?,
    targetAverage: String,
    targetLabel: String,
    targetHasError: Boolean,
    onTargetChange: (String) -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = SubjectFormCardShape,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Información básica", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nombre") },
                placeholder = { Text("Ej: Estadística inferencial") },
                singleLine = true,
                shape = AppShapes.SmallCard,
                modifier = Modifier.fillMaxWidth(),
                isError = !nameIsValid,
                supportingText = {
                    if (!nameIsValid) {
                        Text(nameError ?: "Ingresa un nombre de materia válido")
                    }
                }
            )
            OutlinedTextField(
                value = targetAverage,
                onValueChange = onTargetChange,
                label = { Text("Meta de promedio (0 a $targetLabel)") },
                singleLine = true,
                shape = AppShapes.SmallCard,
                modifier = Modifier.fillMaxWidth(),
                isError = targetHasError
            )
        }
    }
}

@Composable
private fun SubjectColorPicker(
    selectedColor: Int,
    onSelected: (Int) -> Unit
) {
    val selected = Color(selectedColor)
    val selectedRgb = selected.toArgb()
    val red = ((selectedRgb shr 16) and 0xFF)
    val green = ((selectedRgb shr 8) and 0xFF)
    val blue = (selectedRgb and 0xFF)

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Color", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(SubjectColorPalette, key = { it.toArgb() }) { color ->
                ColorSwatch(
                    color = color,
                    label = color.accessibilityLabel(),
                    selected = color.toArgb() == selectedColor,
                    onClick = { onSelected(color.toArgb()) }
                )
            }
        }
        UniCard(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
            shape = SubjectFormCardShape,
            tonalElevation = 0.dp,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            borderWidth = 0.5.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(selected)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f),
                                shape = CircleShape
                            )
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Color personalizado",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = selected.accessibilityLabel().uppercase(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                ColorChannelSlider(
                    label = "Rojo",
                    value = red,
                    activeColor = Color(0xFFFF4D5E),
                    onValueChange = { onSelected(rgbColor(it, green, blue)) }
                )
                ColorChannelSlider(
                    label = "Verde",
                    value = green,
                    activeColor = Color(0xFF14D8A6),
                    onValueChange = { onSelected(rgbColor(red, it, blue)) }
                )
                ColorChannelSlider(
                    label = "Azul",
                    value = blue,
                    activeColor = Color(0xFF5EA8FF),
                    onValueChange = { onSelected(rgbColor(red, green, it)) }
                )
            }
        }
    }
}

@Composable
private fun ColorChannelSlider(
    label: String,
    value: Int,
    activeColor: Color,
    onValueChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt().coerceIn(0, 255)) },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor,
                inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            )
        )
    }
}

@Composable
private fun SubjectPreviewCard(
    name: String,
    targetAverage: String,
    visualType: SubjectVisualType,
    customColor: Int?
) {
    val accent = customColor?.let { Color(it) } ?: subjectAccent(visualType)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Vista previa", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold)
        UniCard(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            shape = SubjectFormCardShape,
            tonalElevation = 0.dp,
            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
            borderWidth = 0.5.dp,
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(74.dp)
                        .background(accent)
                )
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(44.dp)
                        .background(accent.copy(alpha = 0.14f), AppShapes.SmallCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.School, contentDescription = null, tint = accent)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = name.ifBlank { "Nombre de la materia" },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Meta objetivo: $targetAverage",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun SaveSubjectButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f)
        ),
        contentPadding = PaddingValues(vertical = 0.dp),
        modifier = modifier.height(56.dp)
    ) {
        Text(text, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = if (selected) 1f else 0.82f))
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) else Color.Transparent,
                shape = CircleShape
            )
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

private val SubjectColorPalette = listOf(
    Color(0xFF10B8AC),
    Color(0xFF58A6FF),
    Color(0xFFFF6B7A),
    Color(0xFF8B5CF6),
    Color(0xFF22C55E),
    Color(0xFFF5C542),
    Color(0xFFE84A8A),
    Color(0xFF6366F1),
    Color(0xFFFF9F43),
    Color(0xFF06B6D4),
    Color(0xFF84CC16),
    Color(0xFF94A3B8),
    Color(0xFFFF4D4D),
    Color(0xFFFF7A1A),
    Color(0xFF00D084),
    Color(0xFF14B8A6),
    Color(0xFF2DD4BF),
    Color(0xFF38BDF8),
    Color(0xFF3B82F6),
    Color(0xFF7C3AED),
    Color(0xFFA855F7),
    Color(0xFFD946EF),
    Color(0xFFF472B6),
    Color(0xFF64748B)
)

private fun Color.accessibilityLabel(): String = "#${toArgb().toUInt().toString(16).takeLast(6)}"

private fun rgbColor(red: Int, green: Int, blue: Int): Int {
    return Color(red, green, blue).toArgb()
}

private fun closestVisualType(color: Color): SubjectVisualType {
    return SubjectVisualType.entries.minBy { type ->
        val candidate = subjectAccent(type)
        val dr = candidate.red - color.red
        val dg = candidate.green - color.green
        val db = candidate.blue - color.blue
        dr * dr + dg * dg + db * db
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
