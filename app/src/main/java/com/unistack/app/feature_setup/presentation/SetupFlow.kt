package com.unistack.app.feature_setup.presentation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private object SetupRoutes {
    const val Welcome = "setup_welcome"
    const val Name = "setup_name"
    const val Education = "setup_education"
    const val Academic = "setup_academic"
    const val Scale = "setup_scale"
    const val Modules = "setup_modules"
    const val Finish = "setup_finish"
}

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
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeIn(animationSpec = tween(350))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(350)
            ) + fadeOut(animationSpec = tween(350))
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
                passingGrade = viewModel.passingGradeText,
                targetAverage = viewModel.targetAverageText,
                isValid = viewModel.isGradesValid,
                onScaleSelected = viewModel::updateGradingScale,
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
                EducationLevel.SCHOOL to "Colegio",
                EducationLevel.UNIVERSITY to "Universidad",
                EducationLevel.TECHNICAL to "Técnico / Tecnólogo",
                EducationLevel.INDEPENDENT_COURSE to "Curso independiente",
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

    BackHandler(onBack = onBackClick)
    SetupScaffold(onBackClick = onBackClick, modifier = modifier) {
        if (educationLevel == EducationLevel.UNIVERSITY || educationLevel == EducationLevel.TECHNICAL) {
            SetupStepTitle("Tu carrera o programa")
            
            UniStackDropdown(
                label = "Área de estudio",
                options = StudyArea.values().map { it to labelFor(it) },
                selected = studyArea,
                onSelected = onStudyAreaSelected,
                expanded = areaExpanded,
                onExpandedChange = { areaExpanded = it }
            )
            
            if (studyArea != null) {
                val area = studyArea!!
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
        } else if (educationLevel == EducationLevel.SCHOOL) {
            SetupStepTitle("¿En qué grado estás?")
            
            val schoolOptions = listOf("6°", "7°", "8°", "9°", "10°", "11°", OTHER_OPTION)
            val selectedSchool = value.takeIf { it in schoolOptions } ?: if (value.isNotEmpty()) OTHER_OPTION else null
            
            UniStackDropdown(
                label = "Grado escolar",
                options = schoolOptions.map { it to it },
                selected = selectedSchool,
                onSelected = { 
                    if (it == OTHER_OPTION) onValueChange("") else onValueChange(it)
                },
                expanded = schoolExpanded,
                onExpandedChange = { schoolExpanded = it }
            )
            
            // Re-evaluating the SCHOOL logic to be cleaner
            val isStandardGrade = value in listOf("6°", "7°", "8°", "9°", "10°", "11°")
            if (!isStandardGrade) {
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
            val question = when (educationLevel) {
                EducationLevel.INDEPENDENT_COURSE -> "¿Qué estás aprendiendo?"
                EducationLevel.OTHER -> "Cuéntanos qué estudias"
                else -> "Información académica"
            }
            SetupStepTitle(question)
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
                enabled = isValid || educationLevel == EducationLevel.OTHER || educationLevel == EducationLevel.INDEPENDENT_COURSE,
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
    passingGrade: String,
    targetAverage: String,
    isValid: Boolean,
    onScaleSelected: (GradingScale) -> Unit,
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
                GradingScale.ZERO_TO_TEN to "0 a 10",
                GradingScale.ZERO_TO_ONE_HUNDRED to "0 a 100",
                GradingScale.LETTERS to "Letras",
                GradingScale.CUSTOM to "Personalizada"
            ),
            selected = selectedScale,
            onSelected = onScaleSelected
        )
        OutlinedTextField(
            value = passingGrade,
            onValueChange = onPassingGradeChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("¿Cuál es la nota mínima para aprobar?") },
            shape = AppShapes.MediumCard
        )
        OutlinedTextField(
            value = targetAverage,
            onValueChange = onTargetAverageChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("¿Cuál es tu promedio objetivo?") },
            shape = AppShapes.MediumCard
        )
        if (!isValid) {
            Text("Revisa que las notas estén dentro de la escala y que el promedio objetivo sea al menos la nota mínima.", color = UniStackColors.Coral, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        PrimarySetupButton(text = "Continuar", enabled = isValid, onClick = onContinueClick)
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
        OptionList(
            options = listOf(
                AppModule.GRADES to "Mis notas",
                AppModule.TASKS to "Mis tareas",
                AppModule.EXPENSES to "Mis gastos",
                AppModule.ACADEMIC_TEMPLATES to "Mis trabajos"
            ),
            selectedValues = selectedModules,
            onToggle = onToggleModule
        )
        PrimarySetupButton(text = "Continuar", onClick = onContinueClick)
    }
}

@Composable
fun SetupFinishScreen(
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
        PrimarySetupButton(text = "Crear mi primera materia", onClick = onCreateSubjectClick)
        TextButton(onClick = onGoHomeClick) {
            Text("Ir al inicio", color = UniStackColors.Primary, fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(22.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp), content = content)
        }
    }
}

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
private fun OptionList(
    options: List<Pair<AppModule, String>>,
    selectedValues: Set<AppModule>,
    onToggle: (AppModule) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        options.forEach { (value, label) ->
            SelectableCard(label = label, selected = value in selectedValues, onClick = { onToggle(value) })
        }
    }
}

@Composable
private fun SelectableCard(label: String, selected: Boolean, onClick: () -> Unit) {
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = 2.dp,
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            options.take(3).forEach { (scale, label) ->
                ScaleChip(label, selected == scale) { onSelected(scale) }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            options.drop(3).forEach { (scale, label) ->
                ScaleChip(label, selected == scale) { onSelected(scale) }
            }
        }
    }
}

@Composable
private fun ScaleChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontWeight = FontWeight.Bold) },
        modifier = Modifier.fillMaxWidth()
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
                        onSelected(value)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}
