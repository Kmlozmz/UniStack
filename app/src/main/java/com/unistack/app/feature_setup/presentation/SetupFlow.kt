package com.unistack.app.feature_setup.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.utils.ValidationResult
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackLogoMarkWhite
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.UniStackTheme
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
    const val Modules = "setup_modules"
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
                onContinueClick = { navController.navigate(SetupRoutes.Modules) }
            )
        }
        composable(SetupRoutes.Modules) {
            SetupModulesScreen(
                selectedModules = viewModel.enabledModules,
                onToggleModule = viewModel::toggleModule,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Finish) }
            )
        }
        composable(SetupRoutes.Finish) {
            SetupFinishScreen(
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
    SetupScaffold(modifier = modifier) {
        SetupHeroIcon()
        Text("Bienvenido a UniStack", style = MaterialTheme.typography.headlineLarge, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
        Text(
            "Organiza tus notas, tareas, gastos y entregas académicas desde un solo lugar.",
            color = UniStackColors.TextSecondary,
            style = MaterialTheme.typography.bodyLarge
        )
        PrimarySetupButton(text = "Empezar", onClick = onStartClick)
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
    SetupScaffold(onBackClick = onBackClick, modifier = modifier) {
        SetupStepTitle("¿Cómo quieres que te llamemos?")
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Nombre preferido") },
            placeholder = { Text("Escribe tu nombre o apodo") },
            supportingText = {
                if (name.isNotBlank() && !nameValidation.isValid) {
                    Text(nameValidation.errorMessage ?: "Ingresa un nombre válido")
                }
            },
            shape = AppShapes.MediumCard,
            isError = name.isNotBlank() && !nameValidation.isValid
        )
        PrimarySetupButton(text = "Continuar", enabled = nameValidation.isValid, onClick = onContinueClick)
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
    SetupScaffold(onBackClick = onBackClick, modifier = modifier) {
        SetupStepTitle("¿Dónde estudias actualmente?")
        OptionList(
            options = listOf(
                EducationLevel.PRIMARY to "Primaria",
                EducationLevel.SECONDARY to "Secundaria",
                EducationLevel.UNIVERSITY to "Universidad",
                EducationLevel.OTHER to "Otro"
            ),
            selected = selected,
            onSelected = onSelected
        )
        PrimarySetupButton(text = "Continuar", onClick = onContinueClick)
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
    var schoolExpanded by remember { mutableStateOf(false) }
    var customSchoolGradeSelected by rememberSaveable(educationLevel) { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)
    SetupScaffold(onBackClick = onBackClick, modifier = modifier) {
        if (educationLevel == EducationLevel.UNIVERSITY) {
            SetupStepTitle("Tu carrera o programa")
            
            UniStackDropdown(
                label = "Área de estudio",
                options = StudyArea.entries.map { it to labelFor(it) },
                selected = studyArea,
                onSelected = onStudyAreaSelected,
                expanded = areaExpanded,
                onExpandedChange = { areaExpanded = it }
            )
            
            if (studyArea != null) {
                val area = studyArea
                Spacer(modifier = Modifier.height(12.dp))
                UniStackDropdown(
                    label = "Programa o carrera",
                    options = programsFor(area).map { it to it },
                    selected = selectedProgram,
                    onSelected = onProgramSelected,
                    expanded = programExpanded,
                    onExpandedChange = { programExpanded = it }
                )
                
                if (area == StudyArea.OTHER || selectedProgram == OTHER_OPTION) {
                    Spacer(modifier = Modifier.height(12.dp))
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
            }
        } else if (educationLevel == EducationLevel.PRIMARY || educationLevel == EducationLevel.SECONDARY) {
            SetupStepTitle("¿En qué grado estás?")
            
            val standardGrades = if (educationLevel == EducationLevel.PRIMARY) {
                listOf("1°", "2°", "3°", "4°", "5°")
            } else {
                listOf("6°", "7°", "8°", "9°", "10°", "11°")
            }
            val schoolOptions = standardGrades + OTHER_OPTION
            val isCustomSchoolGrade = customSchoolGradeSelected || (value.isNotBlank() && value !in standardGrades)
            val selectedSchool = when {
                isCustomSchoolGrade -> OTHER_OPTION
                value in standardGrades -> value
                else -> null
            }
            
            UniStackDropdown(
                label = "Grado escolar",
                options = schoolOptions.map { it to it },
                selected = selectedSchool,
                onSelected = {
                    customSchoolGradeSelected = it == OTHER_OPTION
                    if (it == OTHER_OPTION) onValueChange("") else onValueChange(it)
                },
                expanded = schoolExpanded,
                onExpandedChange = { schoolExpanded = it }
            )
            
            if (isCustomSchoolGrade) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Escribe tu grado") },
                    placeholder = { Text("Bachillerato, aceleración...") },
                    shape = AppShapes.MediumCard,
                    isError = value.isNotBlank() && !isValid
                )
            }
        } else {
            SetupStepTitle("Cuéntanos qué estudias")
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Información académica") },
                placeholder = { Text("Opcional") },
                shape = AppShapes.MediumCard,
                isError = value.isNotBlank() && !isValid
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onSkipClick, modifier = Modifier.weight(1f)) {
                Text("Omitir", color = UniStackColors.Primary, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onContinueClick,
                enabled = isValid || educationLevel == EducationLevel.OTHER,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Continuar", fontWeight = FontWeight.ExtraBold)
            }
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
    SetupScaffold(onBackClick = onBackClick, modifier = modifier) {
        SetupStepTitle("¿Qué escala de notas usas?")
        FlowOptions(
            options = listOf(
                GradingScale.ZERO_TO_FIVE to "0.0 a 5.0",
                GradingScale.CUSTOM to "Personalizada"
            ),
            selected = selectedScale,
            onSelected = onScaleSelected
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
                label = { Text("Nota mínima para aprobar ($gradeRangeLabel)") },
                shape = AppShapes.MediumCard
            )
            OutlinedTextField(
                value = targetAverage,
                onValueChange = onTargetAverageChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Promedio objetivo ($gradeRangeLabel)") },
                shape = AppShapes.MediumCard
            )
        }
        val waitingForCustomRange = selectedScale == GradingScale.CUSTOM && !customGradeRangeConfirmed
        if (!isValid && !waitingForCustomRange) {
            Text("Revisa que las notas estén dentro de la escala y que el promedio objetivo sea al menos la nota mínima.", color = UniStackColors.Coral, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        PrimarySetupButton(text = "Continuar", enabled = isValid, onClick = onContinueClick)
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
    SetupScaffold(onBackClick = onBackClick, modifier = modifier) {
        SetupStepTitle("¿Qué quieres organizar primero?")
        ModuleOptionList(
            options = listOf(
                ModuleOption(
                    module = AppModule.GRADES,
                    label = "Notas y materias",
                    description = "Promedios, porcentajes y simulador.",
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
        PrimarySetupButton(text = "Continuar", onClick = onContinueClick)
    }
}

@Composable
fun SetupFinishScreen(
    createSubjectEnabled: Boolean = true,
    onBackClick: () -> Unit,
    onCreateSubjectClick: () -> Unit,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(onBackClick = onBackClick, modifier = modifier) {
        SetupHeroIcon()
        Text("Todo listo", style = MaterialTheme.typography.headlineLarge, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
        Text("Ahora puedes empezar a organizar tu semestre.", color = UniStackColors.TextSecondary)
        if (createSubjectEnabled) {
            PrimarySetupButton(text = "Crear mi primera materia", onClick = onCreateSubjectClick)
            TextButton(onClick = onGoHomeClick) {
                Text("Ir al inicio", color = UniStackColors.Primary, fontWeight = FontWeight.Bold)
            }
        } else {
            PrimarySetupButton(text = "Ir al inicio", onClick = onGoHomeClick)
        }
    }
}

@Composable
private fun SetupScaffold(
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    tint = UniStackColors.TextPrimary
                )
            }
        }
        UniCard(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            brush = Brush.linearGradient(listOf(UniStackColors.Card, UniStackColors.SurfaceVariant)),
            shape = AppShapes.LargeCard,
            tonalElevation = 6.dp,
            borderColor = UniStackColors.SoftOutline,
            borderWidth = 1.4.dp,
            contentPadding = PaddingValues(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp), content = content)
        }
    }
}

private data class ModuleOption(
    val module: AppModule,
    val label: String,
    val description: String,
    val icon: ImageVector
)

@Composable
private fun SetupHeroIcon() {
    Box(
        modifier = Modifier
            .size(62.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(UniStackColors.Primary, UniStackColors.Blue))),
        contentAlignment = Alignment.Center
    ) {
        UniStackLogoMarkWhite(size = 34.dp)
    }
}

@Composable
private fun SetupStepTitle(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Rounded.School, contentDescription = null, tint = UniStackColors.Primary)
        Text(text, style = MaterialTheme.typography.headlineSmall, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun PrimarySetupButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.Pill,
        colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun <T> OptionList(
    options: List<Pair<T, String>>,
    selected: T?,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { (value, label) ->
            SelectableCard(label = label, selected = selected == value, onClick = { onSelected(value) })
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
    onClick: () -> Unit
) {
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                onValueChange = { onClick() }
            )
            .semantics {
                stateDescription = if (selected) "Activo" else "Inactivo"
            },
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = if (selected) 4.dp else 1.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline,
        borderWidth = if (selected) 1.6.dp else 1.2.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
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
                    tint = UniStackColors.Primary
                )
            }
        }
    }
}

@Composable
private fun SelectableCard(label: String, selected: Boolean, onClick: () -> Unit) {
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics {
                stateDescription = if (selected) "Seleccionado" else "No seleccionado"
            },
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = 2.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline,
        borderWidth = if (selected) 1.5.dp else 1.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (selected) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = UniStackColors.Primary)
            }
        }
    }
}

@Composable
private fun FlowOptions(
    options: List<Pair<GradingScale, String>>,
    selected: GradingScale,
    onSelected: (GradingScale) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        options.forEach { (scale, label) ->
            ScaleChip(
                label = label,
                selected = selected == scale,
                onClick = { onSelected(scale) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScaleChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        modifier = modifier
    )
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun SetupWelcomeScreenPreview() {
    UniStackTheme {
        SetupWelcomeScreen(onStartClick = {})
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun SetupNameScreenPreview() {
    UniStackTheme {
        SetupNameScreen(name = "Pineda", nameValidation = ValidationResult(true), onNameChange = {}, onBackClick = {}, onContinueClick = {})
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun SetupFinishScreenPreview() {
    UniStackTheme {
        SetupFinishScreen(onBackClick = {}, onCreateSubjectClick = {}, onGoHomeClick = {})
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> UniStackDropdown(
    label: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelected: (T) -> Unit,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = options.find { it.first == selected }?.second ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = UniStackColors.Primary,
                focusedLabelColor = UniStackColors.Primary
            ),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = AppShapes.MediumCard
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(UniStackColors.Card)
        ) {
            options.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onExpandedChange(false)
                        onSelected(value)
                    }
                )
            }
        }
    }
}
