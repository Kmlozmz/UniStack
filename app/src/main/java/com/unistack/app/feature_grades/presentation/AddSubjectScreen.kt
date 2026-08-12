package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.SubjectColorPalette
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_schedule.domain.SubjectScheduleDraft
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.domain.UserPlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

private val SubjectFormCardShape
    get() = AppShapes.MediumCard

@Composable
fun AddSubjectScreen(
    onBackClick: () -> Unit,
    onSubjectSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel(),
    subjectId: String? = null,
    onUpgradeClick: () -> Unit = {}
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val classSessions by viewModel.classSessions.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val maxGradeLabel = GradingScaleUtils.formatGrade(maxGrade, scale)
    val defaultAverage = profile?.targetAverage ?: 4.0
    val isEditing = subjectId != null
    val subject = subjectId?.let { id -> subjects.firstOrNull { it.id == id } }
    val subjectSchedule = subjectId?.let { id -> classSessions.firstOrNull { it.subjectId == id } }
    val defaultPeriodScheme = subject?.periodScheme ?: profile?.academicPeriodScheme ?: AcademicPeriodScheme.default()
    val userPlan = FeatureGate.planFor(isPro = false)
    val freeLimitReached = !isEditing && !FeatureGate.canCreateSubject(userPlan, subjects.size)

    var name by remember { mutableStateOf("") }
    var targetAverage by remember { mutableStateOf("") }
    var visualType by remember { mutableStateOf(SubjectVisualType.TEAL) }
    var customColor by remember { mutableStateOf<Int?>(subjectAccent(SubjectVisualType.TEAL).toArgb()) }
    var activePeriodId by remember { mutableStateOf(defaultPeriodScheme.periods.firstOrNull()?.id.orEmpty()) }
    var scheduleDraft by remember(subjectId) { mutableStateOf(defaultSubjectScheduleDraft()) }
    var scheduleInitialized by remember(subjectId) { mutableStateOf(false) }
    var initialized by remember(subjectId) { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val formScrollState = rememberScrollState()
    val targetValue = targetAverage.toDoubleOrNull()
    val nameValidation = TextValidators.validateSubjectName(name)
    val isNameValid = name.isBlank() || nameValidation.isValid
    val canEditLoadedSubject = !isEditing || subject != null
    val isValid = canEditLoadedSubject &&
        !freeLimitReached &&
        nameValidation.isValid &&
        targetValue != null &&
        targetValue in 0.0..maxGrade &&
        scheduleDraft.isValid

    LaunchedEffect(subject?.id, defaultAverage, scale, subjectId) {
        if (initialized) return@LaunchedEffect

        if (subject != null) {
            name = subject.name
            targetAverage = GradingScaleUtils.formatGrade(subject.targetAverage, scale)
            visualType = subject.visualType
            customColor = subject.customColor ?: subjectAccent(subject.visualType).toArgb()
            activePeriodId = subject.activePeriodId
            initialized = true
        } else if (!isEditing) {
            targetAverage = GradingScaleUtils.formatGrade(defaultAverage, scale)
            customColor = subjectAccent(visualType).toArgb()
            activePeriodId = defaultPeriodScheme.periods.firstOrNull()?.id.orEmpty()
            initialized = true
        }
    }

    LaunchedEffect(subject?.id, subjectSchedule?.id, subjectId) {
        when {
            subjectSchedule != null -> {
                scheduleDraft = subjectSchedule.toSubjectScheduleDraft()
                scheduleInitialized = true
            }
            !scheduleInitialized && isEditing && subject != null -> {
                scheduleDraft = defaultSubjectScheduleDraft().copy(enabled = false)
                scheduleInitialized = true
            }
            !scheduleInitialized && !isEditing -> {
                scheduleDraft = defaultSubjectScheduleDraft()
                scheduleInitialized = true
            }
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
                .verticalScroll(formScrollState)
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubjectFormHeader(
                title = if (isEditing) "Editar materia" else "Agregar materia",
                subjectName = name,
                accent = customColor?.let(::Color) ?: subjectAccent(visualType),
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
                },
                periodScheme = defaultPeriodScheme,
                activePeriodId = activePeriodId,
                onActivePeriodSelected = { activePeriodId = it }
            )
            SubjectColorPicker(
                selectedColor = customColor ?: subjectAccent(visualType).toArgb(),
                onSelected = { color ->
                    customColor = color
                    visualType = closestVisualType(Color(color))
                }
            )
            SubjectScheduleSection(
                draft = scheduleDraft,
                onDraftChange = {
                    scheduleDraft = it
                    error = null
                }
            )
            error?.let {
                Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
            }
        }
        // La barra llega hasta el borde inferior de la pantalla y el margen del sistema va
        // por dentro. Con navigationBarsPadding() por fuera se levantaba entera y dejaba
        // una franja transparente debajo por la que se veía pasar el formulario al
        // desplazarse: eso era lo que se veía cortado.
        //
        // union() en vez de encadenar los dos márgenes: el hueco del teclado ya incluye el
        // de la barra de gestos, así que sumarlos dejaría el botón flotando de más.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            // Opaca del todo. Al 98% el contenido se traslucía por debajo y parecía que la
            // barra estaba superpuesta sobre todo.
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
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
                            customColor = customColor,
                            activePeriodId = activePeriodId
                        )
                        if (saved) editingSubjectId else null
                    } else {
                        viewModel.addSubject(
                            name = TextValidators.normalizeText(name),
                            targetAverage = targetValue ?: defaultAverage,
                            visualType = visualType,
                            customColor = customColor,
                            activePeriodId = activePeriodId
                        )?.id
                    }

                    val scheduleSaved = savedSubjectId?.let { id ->
                        viewModel.saveSubjectSchedule(
                            subjectId = id,
                            draft = scheduleDraft.copy(
                                recurrenceStartEpochDay = scheduleDraft.recurrenceStartEpochDay
                                    .takeIf { it > 0L }
                                    ?: LocalDate.now().toEpochDay()
                            )
                        )
                    } ?: false

                    when {
                        savedSubjectId == null -> {
                            error = "Revisa el nombre y la meta antes de guardar."
                        }
                        !scheduleSaved -> {
                            error = "La materia se guardó, pero revisa la configuración del horario."
                        }
                        else -> {
                            scope.launch {
                                launch {
                                    snackbarHostState.showSnackbar(
                                        if (isEditing) "Materia y horario actualizados" else "Materia y horario creados"
                                    )
                                }
                                delay(650)
                                onSubjectSaved(savedSubjectId)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .bottomActionInsets()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 84.dp)
        )
    }
}

@Composable
private fun SubjectFormHeader(
    title: String,
    subjectName: String,
    accent: Color,
    onBackClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f), AppShapes.Pill)
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accent.copy(alpha = 0.14f), AppShapes.SmallCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.School, contentDescription = null, tint = accent)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subjectName.ifBlank { "Configura tu materia" },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
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
    onTargetChange: (String) -> Unit,
    periodScheme: AcademicPeriodScheme,
    activePeriodId: String,
    onActivePeriodSelected: (String) -> Unit
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
            if (periodScheme.periods.size > 1) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Corte actual",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(periodScheme.periods.sortedBy { it.order }, key = { it.id }) { period ->
                            val selected = activePeriodId == period.id
                            Surface(
                                onClick = { onActivePeriodSelected(period.id) },
                                shape = AppShapes.Pill,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                                )
                            ) {
                                Text(
                                    "Corte ${period.order}",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    if (activePeriodId != periodScheme.periods.firstOrNull()?.id) {
                        Text(
                            "Las nuevas notas y tareas usarán este corte por defecto.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectColorPicker(
    selectedColor: Int,
    onSelected: (Int) -> Unit
) {
    val selected = Color(selectedColor)
    var showEditor by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Apariencia", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold)
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
        Surface(
            onClick = { showEditor = true },
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.MediumCard,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(selected.copy(alpha = 0.16f), AppShapes.SmallCard),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.ColorLens, contentDescription = null, tint = selected)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Color personalizado",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        selected.toHexString(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Abrir editor de color")
            }
        }
    }

    if (showEditor) {
        CustomSubjectColorDialog(
            initialColor = selectedColor,
            onDismiss = { showEditor = false },
            onApply = {
                onSelected(it)
                showEditor = false
            }
        )
    }
}

@Composable
private fun CustomSubjectColorDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit
) {
    var workingColor by remember(initialColor) { mutableStateOf(initialColor) }
    val selected = Color(workingColor)
    val hsv = remember(workingColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(workingColor, it) }
    }
    var hexInput by remember(initialColor) { mutableStateOf(selected.toHexString()) }

    LaunchedEffect(workingColor) {
        hexInput = selected.toHexString()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Cancelar")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Color personalizado",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Arrastra el selector para ajustar el tono con precisión.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = AppShapes.MediumCard,
                    color = selected
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            selected.toHexString(),
                            color = UniStackColors.contentColorOn(selected),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                SaturationValuePicker(
                    hue = hsv[0],
                    saturation = hsv[1],
                    value = hsv[2],
                    onSelected = { saturation, value ->
                        workingColor = android.graphics.Color.HSVToColor(
                            floatArrayOf(hsv[0], saturation, value)
                        )
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Tono",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = hsv[0],
                        onValueChange = { hue ->
                            workingColor = android.graphics.Color.HSVToColor(
                                floatArrayOf(hue, hsv[1], hsv[2])
                            )
                        },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = selected,
                            activeTrackColor = selected,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                        )
                    )
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val normalized = input.uppercase()
                            .filter { it == '#' || it in '0'..'9' || it in 'A'..'F' }
                            .take(7)
                        hexInput = normalized
                        normalized.toColorIntOrNull()?.let { workingColor = it }
                    },
                    label = { Text("Hexadecimal") },
                    placeholder = { Text("#6750F5") },
                    singleLine = true,
                    shape = AppShapes.SmallCard,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { onApply(workingColor) },
                        colors = ButtonDefaults.buttonColors(containerColor = selected),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Aplicar",
                            color = UniStackColors.contentColorOn(selected)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaturationValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onSelected: (Float, Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(AppShapes.SmallCard)
            .pointerInput(hue) {
                fun update(offset: Offset) {
                    onSelected(
                        (offset.x / size.width).coerceIn(0f, 1f),
                        (1f - offset.y / size.height).coerceIn(0f, 1f)
                    )
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    update(down.position)
                    down.consume()

                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                update(change.position)
                            }
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        // design-tokens-ok-begin: lienzo saturación/valor del selector HSV. El blanco y el
        // negro son los ejes del espacio de color, no decisiones de marca; el cursor va en
        // blanco con contorno oscuro para verse sobre cualquier punto del lienzo.
        drawRect(
            Brush.horizontalGradient(
                listOf(Color.White, Color.hsv(hue, 1f, 1f))
            )
        )
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        val center = Offset(saturation * size.width, (1f - value) * size.height)
        drawCircle(Color.White, radius = 7.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
        drawCircle(Color.Black.copy(alpha = 0.45f), radius = 9.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
        // design-tokens-ok-end
    }
}

private fun Color.toHexString(): String = "#%06X".format(toArgb() and 0xFFFFFF)

private fun String.toColorIntOrNull(): Int? {
    val raw = removePrefix("#")
    if (raw.length != 6) return null
    val rgb = raw.toLongOrNull(16) ?: return null
    return (0xFF000000L or rgb).toInt()
}

@Composable
private fun SaveSubjectButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniStackButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    )
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
            Icon(Icons.Rounded.Check, contentDescription = null, tint = UniStackColors.contentColorOn(color))
        }
    }
}


private fun Color.accessibilityLabel(): String = "#${toArgb().toUInt().toString(16).takeLast(6)}"

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
