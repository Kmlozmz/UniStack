package com.unistack.app.feature_setup.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.utils.ValidationResult
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unistack.app.R
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackLogoMarkWhite
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_user.domain.AcademicPeriodLabel
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import kotlin.math.roundToInt

private object SetupRoutes {
    const val Welcome = "setup_welcome"
    const val Name = "setup_name"
    const val Education = "setup_education"
    const val Academic = "setup_academic"
    const val Scale = "setup_scale"
    const val Periods = "setup_periods"
    const val Modules = "setup_modules"
    const val Summary = "setup_summary"
    const val Finish = "setup_finish"
}

private const val SETUP_TRANSITION_MILLIS = 220
private const val SETUP_EXIT_MILLIS = 150

@Composable
fun SetupFlow(
    onSetupFinished: (createFirstSubject: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SetupRoutes.Welcome,
        modifier = modifier.background(UniStackColors.Background),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / 3 },
                animationSpec = tween(SETUP_TRANSITION_MILLIS, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(110, delayMillis = 25, easing = FastOutSlowInEasing)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 4 },
                animationSpec = tween(SETUP_EXIT_MILLIS, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(SETUP_EXIT_MILLIS, easing = FastOutSlowInEasing)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = tween(SETUP_TRANSITION_MILLIS, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(110, delayMillis = 25, easing = FastOutSlowInEasing)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it / 4 },
                animationSpec = tween(SETUP_EXIT_MILLIS, easing = FastOutSlowInEasing)
            ) + fadeOut(
                animationSpec = tween(SETUP_EXIT_MILLIS, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable(SetupRoutes.Welcome) {
            SetupWelcomeScreen(onStartClick = { navController.navigate(SetupRoutes.Name) })
        }
        composable(SetupRoutes.Name) {
            SetupNameScreen(
                name = viewModel.preferredName,
                nameValidation = viewModel.nameValidation,
                onNameChange = viewModel::updatePreferredName,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Education) }
            )
        }
        composable(SetupRoutes.Education) {
            SetupEducationLevelScreen(
                selected = viewModel.educationLevel,
                onSelected = viewModel::updateEducationLevel,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Academic) }
            )
        }
        composable(SetupRoutes.Academic) {
            SetupAcademicInfoScreen(
                educationLevel = viewModel.educationLevel,
                value = viewModel.academicInfo,
                studyArea = viewModel.studyArea,
                selectedProgram = viewModel.selectedProgram,
                customProgram = viewModel.customProgram,
                isValid = viewModel.isAcademicInfoValid,
                customProgramValidation = viewModel.customProgramValidation,
                onValueChange = viewModel::updateAcademicInfo,
                onStudyAreaSelected = viewModel::updateStudyArea,
                onProgramSelected = viewModel::updateSelectedProgram,
                onCustomProgramChange = viewModel::updateCustomProgram,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Scale) },
                onSkipClick = {
                    viewModel.skipAcademicInfo()
                    navController.navigate(SetupRoutes.Scale)
                }
            )
        }
        composable(SetupRoutes.Scale) {
            SetupGradingScaleScreen(
                selectedScale = viewModel.gradingScale,
                customGradeMax = viewModel.customGradeMax,
                customGradeRangeConfirmed = viewModel.customGradeRangeConfirmed,
                passingGrade = viewModel.passingGradeText,
                targetAverage = viewModel.targetAverageText,
                isValid = viewModel.isGradesValid,
                onScaleSelected = viewModel::updateGradingScale,
                onCustomGradeMaxChange = viewModel::updateCustomGradeMax,
                onConfirmCustomGradeRange = viewModel::confirmCustomGradeRange,
                onEditCustomGradeRange = viewModel::editCustomGradeRange,
                onPassingGradeChange = viewModel::updatePassingGrade,
                onTargetAverageChange = viewModel::updateTargetAverage,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Periods) }
            )
        }
        composable(SetupRoutes.Periods) {
            SetupAcademicPeriodsScreen(
                label = viewModel.academicPeriodLabel,
                weights = viewModel.academicPeriodWeights,
                isValid = viewModel.isAcademicPeriodsValid,
                onLabelSelected = viewModel::updateAcademicPeriodLabel,
                onCountSelected = viewModel::updateAcademicPeriodCount,
                onWeightChange = viewModel::updateAcademicPeriodWeight,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Modules) }
            )
        }
        composable(SetupRoutes.Modules) {
            SetupModulesScreen(
                selectedModules = viewModel.enabledModules,
                onToggleModule = viewModel::toggleModule,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Summary) }
            )
        }
        composable(SetupRoutes.Summary) {
            SetupSummaryScreen(
                name = viewModel.preferredName,
                educationLevel = viewModel.educationLevel,
                studyArea = viewModel.studyArea,
                selectedProgram = viewModel.selectedProgram,
                customProgram = viewModel.customProgram,
                academicInfo = viewModel.academicInfo,
                gradingScale = viewModel.gradingScale,
                customGradeMax = viewModel.customGradeMax,
                passingGrade = viewModel.passingGradeText,
                targetAverage = viewModel.targetAverageText,
                periodLabel = viewModel.academicPeriodLabel,
                periodWeights = viewModel.academicPeriodWeights,
                enabledModules = viewModel.enabledModules,
                onBackClick = { navController.navigateUp() },
                onConfirmClick = { navController.navigate(SetupRoutes.Finish) }
            )
        }
        composable(SetupRoutes.Finish) {
            SetupFinishScreen(
                name = viewModel.preferredName,
                createSubjectEnabled = AppModule.GRADES in viewModel.enabledModules,
                onBackClick = { navController.navigateUp() },
                onCreateSubjectClick = {
                    viewModel.finishSetup()
                    onSetupFinished(true)
                },
                onGoHomeClick = {
                    viewModel.finishSetup()
                    onSetupFinished(false)
                }
            )
        }
    }
}

@Composable
fun SetupWelcomeScreen(onStartClick: () -> Unit, modifier: Modifier = Modifier) {
    SetupScaffold(
        modifier = modifier,
        welcome = true,
        actions = {
            PrimarySetupButton(text = "Empezar", onClick = onStartClick, trailing = true)
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SetupHeroIcon(size = 42.dp)
                Text(
                    "UniStack",
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Configura UniStack según tu semestre",
                style = MaterialTheme.typography.headlineMedium,
                color = UniStackColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 34.sp
            )
            Text(
                "Notas, tareas, gastos y trabajos en un solo lugar.",
                color = UniStackColors.TextSecondary,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 23.sp
            )
            Image(
                painter = painterResource(R.drawable.onboarding_hero),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.08f)
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun SetupNameScreen(
    name: String,
    nameValidation: ValidationResult,
    onNameChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 2,
        modifier = modifier,
        actions = {
            PrimarySetupButton(text = "Continuar", enabled = nameValidation.isValid, onClick = onContinueClick, trailing = true)
        }
    ) {
        SetupStepHeader(
            icon = Icons.Rounded.Person,
            title = "¿Cómo te llamas?",
            subtitle = "Usaremos este nombre para personalizar tu panel."
        )
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(surface = Color.Transparent)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nombre") },
                placeholder = { Text("Ej. Juan") },
                supportingText = {
                    if (name.isNotBlank() && !nameValidation.isValid) {
                        Text(nameValidation.errorMessage ?: "Ingresa un nombre válido")
                    }
                },
                shape = AppShapes.MediumCard,
                isError = name.isNotBlank() && !nameValidation.isValid
            )
        }
    }
}

@Composable
fun SetupEducationLevelScreen(
    selected: EducationLevel,
    onSelected: (EducationLevel) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 3,
        modifier = modifier,
        actions = {
            PrimarySetupButton(text = "Continuar", onClick = onContinueClick, trailing = true)
        }
    ) {
        SetupStepHeader(
            icon = Icons.Rounded.School,
            title = "¿Cuál es tu nivel\nde estudio?",
            subtitle = "Esto nos ayuda a adaptar UniStack\na tu etapa académica."
        )
        OptionGrid(
            options = listOf(
                SetupCardOption(EducationLevel.PRIMARY, "Primaria", Icons.AutoMirrored.Rounded.MenuBook),
                SetupCardOption(EducationLevel.SECONDARY, "Secundaria", Icons.AutoMirrored.Rounded.Assignment),
                SetupCardOption(EducationLevel.UNIVERSITY, "Universidad", Icons.Rounded.School),
                SetupCardOption(EducationLevel.OTHER, "Otro", Icons.Rounded.GridView)
            ),
            selected = selected,
            onSelected = onSelected
        )
    }
}

@Composable
fun SetupAcademicInfoScreen(
    educationLevel: EducationLevel,
    value: String,
    studyArea: StudyArea?,
    selectedProgram: String?,
    customProgram: String,
    isValid: Boolean,
    customProgramValidation: ValidationResult? = null,
    onValueChange: (String) -> Unit,
    onStudyAreaSelected: (StudyArea) -> Unit,
    onProgramSelected: (String) -> Unit,
    onCustomProgramChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var areaExpanded by remember { mutableStateOf(false) }
    var programExpanded by remember { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 4,
        modifier = modifier,
        actions = {
            PrimarySetupButton(
                text = "Continuar",
                enabled = isValid || educationLevel != EducationLevel.UNIVERSITY,
                onClick = onContinueClick,
                trailing = true
            )
        }
    ) {
        SetupStepHeader(
            icon = Icons.Rounded.BusinessCenter,
            title = "¿Cuál es tu carrera\no programa?",
            subtitle = "Primero elige tu área de estudio\ny luego tu carrera."
        )
        SetupDropdownField(
            label = "Área de estudio",
            value = studyArea?.let(::labelFor).orEmpty(),
            options = StudyArea.entries.map(::labelFor),
            enabled = true,
            expanded = areaExpanded,
            onExpandedChange = { expanded ->
                areaExpanded = expanded
                if (expanded) programExpanded = false
            },
            onOptionSelected = { selectedLabel ->
                StudyArea.entries.firstOrNull { labelFor(it) == selectedLabel }?.let(onStudyAreaSelected)
            }
        )

        val area = studyArea
        SetupDropdownField(
            label = "Programa o carrera",
            value = selectedProgram.orEmpty(),
            options = area?.let(::programsFor).orEmpty(),
            enabled = area != null,
            expanded = programExpanded,
            onExpandedChange = { expanded ->
                programExpanded = expanded
                if (expanded) areaExpanded = false
            },
            onOptionSelected = onProgramSelected
        )

        if (area == StudyArea.OTHER || selectedProgram == OTHER_OPTION) {
            OutlinedTextField(
                value = customProgram,
                onValueChange = onCustomProgramChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nombre del programa") },
                placeholder = { Text("Ej: Ingeniería Biomédica") },
                shape = AppShapes.MediumCard,
                isError = customProgram.isNotBlank() && customProgramValidation?.isValid == false,
                supportingText = {
                    if (customProgram.isNotBlank() && customProgramValidation?.isValid == false) {
                        Text(customProgramValidation.errorMessage ?: "Ingresa un programa válido")
                    }
                }
            )
        }

        TextButton(
            onClick = {
                onStudyAreaSelected(StudyArea.OTHER)
                onProgramSelected(OTHER_OPTION)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("No encuentro mi carrera", color = UniStackColors.Primary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SetupGradingScaleScreen(
    selectedScale: GradingScale,
    customGradeMax: Double,
    customGradeRangeConfirmed: Boolean,
    passingGrade: String,
    targetAverage: String,
    isValid: Boolean,
    onScaleSelected: (GradingScale) -> Unit,
    onCustomGradeMaxChange: (Double) -> Unit,
    onConfirmCustomGradeRange: () -> Unit,
    onEditCustomGradeRange: () -> Unit,
    onPassingGradeChange: (String) -> Unit,
    onTargetAverageChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    val selectedChoice = when {
        selectedScale == GradingScale.ZERO_TO_FIVE -> SetupScaleChoice.FIVE
        customGradeMax.toInt() == 100 && customGradeRangeConfirmed -> SetupScaleChoice.HUNDRED
        else -> SetupScaleChoice.CUSTOM
    }
    SetupScaffold(
        onBackClick = onBackClick,
        step = 5,
        modifier = modifier,
        actions = {
            PrimarySetupButton(text = "Continuar", enabled = isValid, onClick = onContinueClick, trailing = true)
        }
    ) {
        SetupStepHeader(
            icon = Icons.Rounded.BarChart,
            title = "¿Cómo es la escala de tus notas?",
            subtitle = "Esto nos permite calcular tus promedios y metas de forma precisa."
        )
        ScaleChoiceRow(
            options = listOf(
                SetupScaleChoice.FIVE to "0.0 a 5.0",
                SetupScaleChoice.HUNDRED to "0 a 100",
                SetupScaleChoice.CUSTOM to "Personalizada"
            ),
            selected = selectedChoice,
            onSelected = { choice ->
                when (choice) {
                    SetupScaleChoice.FIVE -> onScaleSelected(GradingScale.ZERO_TO_FIVE)
                    SetupScaleChoice.HUNDRED -> {
                        onScaleSelected(GradingScale.CUSTOM)
                        onCustomGradeMaxChange(100.0)
                        onConfirmCustomGradeRange()
                    }
                    SetupScaleChoice.CUSTOM -> {
                        onScaleSelected(GradingScale.CUSTOM)
                        onEditCustomGradeRange()
                    }
                }
            }
        )
        if (selectedScale == GradingScale.CUSTOM && !customGradeRangeConfirmed) {
            CustomGradeRangeSelector(
                customGradeMax = customGradeMax,
                onCustomGradeMaxChange = onCustomGradeMaxChange,
                onConfirmClick = onConfirmCustomGradeRange
            )
        } else {
            val gradeRangeLabel = if (selectedScale == GradingScale.CUSTOM) {
                "0 a ${customGradeMax.toInt()}"
            } else {
                "0.0 a 5.0"
            }
            if (selectedScale == GradingScale.CUSTOM) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Rango confirmado: $gradeRangeLabel",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onEditCustomGradeRange) {
                        Text("Cambiar", color = UniStackColors.Primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            OutlinedTextField(
                value = passingGrade,
                onValueChange = onPassingGradeChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Nota mínima para aprobar") },
                shape = AppShapes.MediumCard
            )
            OutlinedTextField(
                value = targetAverage,
                onValueChange = onTargetAverageChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Promedio objetivo") },
                shape = AppShapes.MediumCard
            )
        }
        val waitingForCustomRange = selectedScale == GradingScale.CUSTOM && !customGradeRangeConfirmed
        if (!isValid && !waitingForCustomRange) {
            Text("Revisa que las notas estén dentro de la escala y que el promedio objetivo sea al menos la nota mínima.", color = UniStackColors.Coral, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SetupAcademicPeriodsScreen(
    label: AcademicPeriodLabel,
    weights: List<String>,
    isValid: Boolean,
    onLabelSelected: (AcademicPeriodLabel) -> Unit,
    onCountSelected: (Int) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = weights.sumOf { it.toDoubleOrNull() ?: 0.0 }
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 6,
        modifier = modifier,
        actions = {
            PrimarySetupButton(text = "Continuar", enabled = isValid, onClick = onContinueClick, trailing = true)
        }
    ) {
        SetupStepHeader(
            icon = Icons.Rounded.CalendarMonth,
            title = "¿Cómo se evalúa tu semestre?",
            subtitle = "Elige si usas periodos o cortes, cantidad y porcentajes."
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            AcademicPeriodLabel.entries.forEach { option ->
                UniCard(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = label == option,
                            role = Role.RadioButton,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = { onLabelSelected(option) }
                        ),
                    color = if (label == option) UniStackColors.PrimaryLight else UniStackColors.Card,
                    shape = AppShapes.Pill,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 11.dp)
                ) {
                    Text(
                        option.singular,
                        color = if (label == option) UniStackColors.Primary else UniStackColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Text("Cantidad", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(2, 3, 4).forEach { count ->
                ScaleChip(
                    label = count.toString(),
                    selected = weights.size == count,
                    onClick = { onCountSelected(count) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            weights.forEachIndexed { index, value ->
                OutlinedTextField(
                    value = value,
                    onValueChange = { onWeightChange(index, it) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("${label.singular} ${index + 1} (%)") },
                    shape = AppShapes.MediumCard
                )
            }
        }
        Text(
            "Total: ${String.format(java.util.Locale.US, "%.0f", total)}%",
            color = if (isValid) UniStackColors.Green else UniStackColors.Coral,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
        if (!isValid) {
            Text(
                "Ajusta la distribución para que el total sea exactamente 100%.",
                color = UniStackColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun CustomGradeRangeSelector(
    customGradeMax: Double,
    onCustomGradeMaxChange: (Double) -> Unit,
    onConfirmClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        brush = Brush.linearGradient(listOf(UniStackColors.PrimaryLight, UniStackColors.SurfaceVariant)),
        shape = AppShapes.LargeCard,
        tonalElevation = 6.dp,
        borderColor = UniStackColors.Primary.copy(alpha = 0.18f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Define el máximo",
                        color = UniStackColors.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Text(
                        "Tu escala irá de 0 hasta este valor.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                UniCard(
                    color = UniStackColors.Primary,
                    shape = AppShapes.Pill,
                    tonalElevation = 4.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        customGradeMax.toInt().toString(),
                        color = UniStackColors.Card,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                "Rango actual: 0 a ${customGradeMax.toInt()}",
                color = UniStackColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Slider(
                value = customGradeMax.toFloat(),
                onValueChange = { value ->
                    onCustomGradeMaxChange(value.roundToInt().coerceIn(1, 100).toDouble())
                },
                valueRange = 1f..100f,
                steps = 98,
                colors = SliderDefaults.colors(
                    thumbColor = UniStackColors.Primary,
                    activeTrackColor = UniStackColors.Primary,
                    inactiveTrackColor = UniStackColors.Primary.copy(alpha = 0.18f),
                    activeTickColor = UniStackColors.Card.copy(alpha = 0.35f),
                    inactiveTickColor = UniStackColors.Primary.copy(alpha = 0.22f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("1", "25", "50", "75", "100").forEach { mark ->
                    Text(mark, color = UniStackColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            PrimarySetupButton(text = "Confirmar rango", onClick = onConfirmClick)
        }
    }
}

@Composable
fun SetupModulesScreen(
    selectedModules: Set<AppModule>,
    onToggleModule: (AppModule) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 7,
        modifier = modifier,
        actions = {
            PrimarySetupButton(text = "Continuar", onClick = onContinueClick, trailing = true)
        }
    ) {
        SetupStepHeader(
            icon = Icons.Rounded.GridView,
            title = "¿Qué quieres organizar con UniStack?",
            subtitle = "Puedes activar o desactivar módulos más adelante desde Ajustes."
        )
        ModuleOptionList(
            options = listOf(
                ModuleOption(
                    module = AppModule.GRADES,
                    label = "Notas y materias",
                    description = "Promedios, porcentajes y metas.",
                    icon = Icons.AutoMirrored.Rounded.MenuBook
                ),
                ModuleOption(
                    module = AppModule.TASKS,
                    label = "Tareas",
                    description = "Entregas, fechas y pendientes.",
                    icon = Icons.Rounded.CheckCircle
                ),
                ModuleOption(
                    module = AppModule.EXPENSES,
                    label = "Gastos",
                    description = "Registros rápidos y resumen semanal.",
                    icon = Icons.Rounded.AccountBalanceWallet
                ),
                ModuleOption(
                    module = AppModule.ACADEMIC_TEMPLATES,
                    label = "Trabajos",
                    description = "Checklist, ensayos y formato APA.",
                    icon = Icons.AutoMirrored.Rounded.Assignment
                )
            ),
            selectedValues = selectedModules,
            onToggle = onToggleModule
        )
    }
}

@Composable
fun SetupSummaryScreen(
    name: String,
    educationLevel: EducationLevel,
    studyArea: StudyArea?,
    selectedProgram: String?,
    customProgram: String,
    academicInfo: String,
    gradingScale: GradingScale,
    customGradeMax: Double,
    passingGrade: String,
    targetAverage: String,
    periodLabel: AcademicPeriodLabel,
    periodWeights: List<String>,
    enabledModules: Set<AppModule>,
    onBackClick: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    val program = resolvedProgram(educationLevel, selectedProgram, customProgram, academicInfo)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 8,
        modifier = modifier,
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                SecondarySetupButton(text = "Volver", onClick = onBackClick, modifier = Modifier.weight(0.8f))
                PrimarySetupButton(
                    text = "Confirmar",
                    onClick = onConfirmClick,
                    modifier = Modifier.weight(1.2f),
                    trailing = true
                )
            }
        }
    ) {
        SetupStepHeader(
            icon = Icons.AutoMirrored.Rounded.Assignment,
            title = "Resumen de tu configuración",
            subtitle = "Revisa y confirma que todo esté correcto."
        )
        SummarySection(
            title = "Información académica",
            rows = listOf(
                "Nombre" to name.ifBlank { "Usuario" },
                "Nivel de estudio" to educationLevel.label(),
                "Área de estudio" to (studyArea?.let(::labelFor) ?: "Sin definir"),
                "Carrera" to program.ifBlank { "Sin definir" }
            )
        )
        SummarySection(
            title = "Escala de notas",
            rows = listOf(
                "Escala" to gradingScale.summaryLabel(customGradeMax),
                "Nota mínima" to passingGrade,
                "Promedio objetivo" to targetAverage
            )
        )
        SummarySection(
            title = "Evaluación",
            rows = listOf(
                "Sistema" to periodLabel.singular,
                "Cantidad" to "${periodWeights.size} ${periodLabel.plural.lowercase()}",
                "Distribución" to periodWeights.joinToString(" / ") { "${it.ifBlank { "0" }}%" }
            )
        )
        SummarySection(
            title = "Módulos activos",
            rows = enabledModules.sortedBy { it.ordinal }.map { it.shortLabel() to "Activo" }
        )
    }
}

@Composable
fun SetupFinishScreen(
    name: String,
    createSubjectEnabled: Boolean = true,
    onBackClick: () -> Unit,
    onCreateSubjectClick: () -> Unit,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 9,
        modifier = modifier,
        actions = {
            if (createSubjectEnabled) {
                PrimarySetupButton(text = "Crear mi primera materia", onClick = onCreateSubjectClick, trailing = true)
                SecondarySetupButton(text = "Ir al inicio", onClick = onGoHomeClick)
            } else {
                PrimarySetupButton(text = "Ir al inicio", onClick = onGoHomeClick)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(96.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(UniStackColors.Primary, UniStackColors.PrimaryDark))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(54.dp))
        }
        Text(
            "¡Todo listo, ${name.ifBlank { "Usuario" }}!",
            style = MaterialTheme.typography.headlineMedium,
            color = UniStackColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "UniStack quedó preparado para organizar tu semestre.",
            color = UniStackColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Image(
            painter = painterResource(R.drawable.onboarding_hero),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.1f)
        )
    }
}

@Composable
private fun SetupScaffold(
    onBackClick: (() -> Unit)? = null,
    step: Int? = null,
    welcome: Boolean = false,
    modifier: Modifier = Modifier,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = UniStackColors.Background,
        topBar = {
            if (onBackClick != null || step != null) {
                SetupTopBar(
                    onBackClick = onBackClick,
                    step = step,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 8.dp)
                )
            }
        },
        bottomBar = {
            if (actions != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(UniStackColors.Background)
                        .navigationBarsPadding()
                        .padding(horizontal = 22.dp)
                        .padding(top = 10.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = actions
                )
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .then(if (actions == null) Modifier.navigationBarsPadding() else Modifier)
                .verticalScroll(scrollState)
                .padding(top = if (step == null) 16.dp else 10.dp, bottom = 18.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (welcome) {
                            Modifier
                                .clip(AppShapes.LargeCard)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            UniStackColors.Card,
                                            UniStackColors.SurfaceVariant,
                                            UniStackColors.PrimaryLight.copy(alpha = 0.55f)
                                        )
                                    )
                                )
                                .border(
                                    width = 1.dp,
                                    color = UniStackColors.SoftOutline,
                                    shape = AppShapes.LargeCard
                                )
                                .padding(24.dp)
                        } else {
                            Modifier
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SetupTopBar(
    onBackClick: (() -> Unit)?,
    step: Int?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver",
                        tint = UniStackColors.TextPrimary
                    )
                }
            }
            if (step != null) {
                Text(
                    text = "Paso $step de 9",
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        if (step != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 54.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(8) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(AppShapes.Pill)
                            .background(
                                if (index < (step - 1).coerceIn(1, 8)) {
                                    UniStackColors.Primary
                                } else {
                                    UniStackColors.SoftOutline.copy(alpha = 0.65f)
                                }
                            )
                    )
                }
            }
        }
    }
}

private data class ModuleOption(
    val module: AppModule,
    val label: String,
    val description: String,
    val icon: ImageVector
)

private data class SetupCardOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector
)

private enum class SetupScaleChoice {
    FIVE,
    HUNDRED,
    CUSTOM
}

@Composable
private fun SetupHeroIcon(size: androidx.compose.ui.unit.Dp = 62.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(UniStackColors.Primary, UniStackColors.Blue))),
        contentAlignment = Alignment.Center
    ) {
        UniStackLogoMarkWhite(size = size * 0.56f)
    }
}

@Composable
private fun SetupStepHeader(
    icon: ImageVector,
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(UniStackColors.PrimaryLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = UniStackColors.Primary, modifier = Modifier.size(26.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = UniStackColors.TextPrimary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )
        if (subtitle != null) {
            Text(
                subtitle,
                color = UniStackColors.TextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun PrimarySetupButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    trailing: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = UniStackColors.Primary,
            disabledContainerColor = UniStackColors.Primary.copy(alpha = 0.38f),
            disabledContentColor = Color.White.copy(alpha = 0.72f)
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, modifier = if (trailing) Modifier.weight(1f) else Modifier)
        if (trailing) {
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SecondarySetupButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = UniStackColors.SurfaceVariant,
            contentColor = UniStackColors.TextPrimary
        ),
        border = BorderStroke(1.dp, UniStackColors.SoftOutline),
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun <T> OptionGrid(
    options: List<SetupCardOption<T>>,
    selected: T?,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { option ->
                    SelectableIconCard(
                        label = option.label,
                        icon = option.icon,
                        selected = selected == option.value,
                        onClick = { onSelected(option.value) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ModuleOptionList(
    options: List<ModuleOption>,
    selectedValues: Set<AppModule>,
    onToggle: (AppModule) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { option ->
            ModuleSelectableCard(
                option = option,
                selected = option.module in selectedValues,
                onClick = { onToggle(option.module) }
            )
        }
    }
}

@Composable
private fun ModuleSelectableCard(
    option: ModuleOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onValueChange = { onClick() }
            )
            .semantics {
                stateDescription = if (selected) "Activo" else "Inactivo"
            },
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.Card,
        shape = AppShapes.SmallCard,
        tonalElevation = 0.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline,
        borderWidth = if (selected) 1.2.dp else 1.dp,
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (selected) UniStackColors.Card else UniStackColors.SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = if (selected) UniStackColors.Primary else UniStackColors.TextSecondary,
                    modifier = Modifier.size(21.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = option.label,
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = option.description,
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 14.sp
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SelectableIconCard(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier
            .height(96.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .semantics {
                stateDescription = if (selected) "Seleccionado" else "No seleccionado"
            },
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.Card,
        shape = AppShapes.SmallCard,
        tonalElevation = 0.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline,
        borderWidth = if (selected) 1.3.dp else 1.dp,
        contentPadding = PaddingValues(11.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    label,
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ScaleChoiceRow(
    options: List<Pair<SetupScaleChoice, String>>,
    selected: SetupScaleChoice,
    onSelected: (SetupScaleChoice) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { (scale, label) ->
            SetupChoiceChip(
                label = label,
                selected = selected == scale,
                onClick = { onSelected(scale) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SetupChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier
            .height(46.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.Card,
        shape = AppShapes.SmallCard,
        tonalElevation = 0.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline,
        borderWidth = if (selected) 1.2.dp else 1.dp,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = if (selected) UniStackColors.Primary else UniStackColors.TextPrimary,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SummarySection(
    title: String,
    rows: List<Pair<String, String>>
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.SmallCard,
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline,
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(
                text = title,
                color = UniStackColors.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
            rows.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = label,
                        color = UniStackColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(0.85f)
                    )
                    Text(
                        text = value,
                        color = UniStackColors.TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1.15f)
                    )
                }
            }
        }
    }
}

private fun EducationLevel.label(): String = when (this) {
    EducationLevel.PRIMARY -> "Primaria"
    EducationLevel.SECONDARY -> "Secundaria"
    EducationLevel.UNIVERSITY -> "Universidad"
    EducationLevel.OTHER -> "Otro"
}

private fun resolvedProgram(
    educationLevel: EducationLevel,
    selectedProgram: String?,
    customProgram: String,
    academicInfo: String
): String {
    if (educationLevel == EducationLevel.PRIMARY || educationLevel == EducationLevel.SECONDARY) {
        return academicInfo
    }
    return if (selectedProgram == OTHER_OPTION) customProgram else selectedProgram.orEmpty()
}

private fun GradingScale.summaryLabel(customGradeMax: Double): String = when (this) {
    GradingScale.ZERO_TO_FIVE -> "0.0 a 5.0"
    GradingScale.CUSTOM -> "0 a ${customGradeMax.toInt()}"
}

private fun AppModule.shortLabel(): String = when (this) {
    AppModule.GRADES -> "Notas"
    AppModule.TASKS -> "Tareas"
    AppModule.EXPENSES -> "Gastos"
    AppModule.ACADEMIC_TEMPLATES -> "Trabajos"
}

@Composable
private fun ScaleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier
            .height(42.dp)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.Card,
        shape = AppShapes.SmallCard,
        tonalElevation = 0.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline,
        borderWidth = if (selected) 1.2.dp else 1.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (selected) UniStackColors.Primary else UniStackColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SetupDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "setup-dropdown-arrow")
    var anchorWidth by remember { mutableStateOf(0) }

    val colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = UniStackColors.SoftOutline,
        focusedBorderColor = UniStackColors.Primary,
        disabledBorderColor = UniStackColors.SoftOutline.copy(alpha = 0.5f),
        disabledTextColor = UniStackColors.TextSecondary,
        unfocusedTextColor = UniStackColors.TextPrimary,
        focusedTextColor = UniStackColors.TextPrimary,
        unfocusedContainerColor = UniStackColors.Card,
        focusedContainerColor = UniStackColors.Card,
        disabledContainerColor = UniStackColors.Card,
    )

    Box(modifier = modifier.fillMaxWidth().onGloballyPositioned { anchorWidth = it.size.width }) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(surface = Color.Transparent)
        ) {
            androidx.compose.material3.OutlinedTextField(
                value = value.ifBlank { "Seleccionar" },
                onValueChange = {},
                readOnly = true,
                enabled = enabled,
                label = { Text(label, fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = UniStackColors.TextSecondary,
                        modifier = Modifier.rotate(rotation + 90f)
                    )
                },
                shape = RoundedCornerShape(22.dp),
                colors = colors
            )
            
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(
                        enabled = enabled,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null
                    ) {
                        keyboard?.hide()
                        onExpandedChange(!expanded)
                    }
            )
        }

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(22.dp)),
            colorScheme = MaterialTheme.colorScheme.copy(surface = UniStackColors.Card)
        ) {
            androidx.compose.material3.DropdownMenu(
                expanded = expanded && enabled,
                onDismissRequest = { onExpandedChange(false) },
                offset = androidx.compose.ui.unit.DpOffset(0.dp, 4.dp),
                modifier = Modifier
                    .then(
                        if (anchorWidth > 0) {
                            Modifier.width(with(density) { anchorWidth.toDp() })
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
                    .heightIn(max = 300.dp)
            ) {
            options.forEach { option ->
                val isSelected = option == value
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = if (isSelected) UniStackColors.Primary else UniStackColors.TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 19.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    trailingIcon = {
                        if (isSelected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = UniStackColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .background(
                            if (isSelected) UniStackColors.PrimaryLight else Color.Transparent,
                            RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 4.dp),
                    onClick = {
                        onExpandedChange(false)
                        onOptionSelected(option)
                    }
                )
            }
        }
        }
    }
}
