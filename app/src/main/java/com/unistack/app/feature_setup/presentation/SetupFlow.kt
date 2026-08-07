package com.unistack.app.feature_setup.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.utils.ValidationResult
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unistack.app.R
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.components.UniStackLogoMark
import com.unistack.app.core.design.components.UniStackLogoMarkWhite
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.LocalMotionDurationScale
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

private const val SETUP_EXIT_MILLIS = 220

/**
 * Oscilación infinita entre -[travel] y +[travel] para elementos decorativos
 * (blobs, gafete, destellos). El recorrido se atenúa con la preferencia de movimiento
 * del usuario y se anula por completo si eligió "sin animaciones".
 */
@Composable
private fun floatingOffset(travel: Float, durationMillis: Int, label: String): Float {
    val motionScale = LocalMotionDurationScale.current
    if (motionScale <= 0f) return 0f
    val transition = rememberInfiniteTransition(label = label)
    val animated by transition.animateFloat(
        initialValue = -travel,
        targetValue = travel,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = label
    )
    return animated * motionScale
}

@Composable
fun SetupFlow(
    onSetupFinished: (createFirstSubject: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SetupRoutes.Welcome,
        modifier = modifier.background(UniStackColors.Background),
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it / 2 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(
                animationSpec = tween(190, delayMillis = 45, easing = FastOutSlowInEasing)
            ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 5 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut(
                animationSpec = tween(SETUP_EXIT_MILLIS, easing = FastOutSlowInEasing)
            ) + scaleOut(
                targetScale = 0.985f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 2 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeIn(
                animationSpec = tween(190, delayMillis = 45, easing = FastOutSlowInEasing)
            ) + scaleIn(
                initialScale = 0.96f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it / 5 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
            ) + fadeOut(
                animationSpec = tween(SETUP_EXIT_MILLIS, easing = FastOutSlowInEasing)
            ) + scaleOut(
                targetScale = 0.985f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
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
                educationLevel = viewModel.educationLevel,
                studyArea = viewModel.studyArea,
                selectedProgram = viewModel.selectedProgram,
                customProgram = viewModel.customProgram,
                academicInfo = viewModel.academicInfo,
                gradingScale = viewModel.gradingScale,
                customGradeMax = viewModel.customGradeMax,
                periodLabel = viewModel.academicPeriodLabel,
                periodWeights = viewModel.academicPeriodWeights,
                enabledModules = viewModel.enabledModules,
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
        welcome = false,
        actions = {
            UniStackButton(
                text = "Comenzar configuración",
                onClick = onStartClick,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
            WelcomeTimeHint()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 2.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            WelcomeBrand()
            WelcomeHeroCard()
            WelcomeFeaturesGrid()
        }
    }
}

@Composable
private fun WelcomeBrand() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UniStackLogoMark(size = 38.dp)
        Text(
            text = "UniStack",
            color = UniStackColors.TextPrimary,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun WelcomeHeroCard() {
    // Duraciones distintas para que los dos blobs nunca queden sincronizados.
    val blobOneDrift = floatingOffset(travel = 9f, durationMillis = 3200, label = "hero-blob-one")
    val blobOneBreath = floatingOffset(travel = 0.05f, durationMillis = 4300, label = "hero-blob-one-breath")
    val blobTwoDrift = floatingOffset(travel = 13f, durationMillis = 4100, label = "hero-blob-two")
    val blobTwoBreath = floatingOffset(travel = 0.07f, durationMillis = 3500, label = "hero-blob-two-breath")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.LargeCard)
            .background(UniStackColors.PrimaryLight)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 22.dp, y = (-22).dp)
                .graphicsLayer {
                    translationY = blobOneDrift.dp.toPx()
                    translationX = (blobOneDrift * 0.45f).dp.toPx()
                    scaleX = 1f + blobOneBreath
                    scaleY = 1f + blobOneBreath
                }
                .size(112.dp)
                .clip(CircleShape)
                .background(UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.30f else 0.24f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-26).dp, y = 32.dp)
                .graphicsLayer {
                    translationY = (-blobTwoDrift).dp.toPx()
                    translationX = (blobTwoDrift * 0.3f).dp.toPx()
                    scaleX = 1f + blobTwoBreath
                    scaleY = 1f + blobTwoBreath
                }
                .size(82.dp)
                .clip(CircleShape)
                .background(UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.22f else 0.16f))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "BIENVENIDO",
                color = UniStackColors.Primary,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = buildAnnotatedString {
                    append("Tu semestre,\n")
                    withStyle(SpanStyle(color = UniStackColors.Primary)) {
                        append("a tu medida")
                    }
                },
                color = UniStackColors.PrimaryDark,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Unas preguntas rápidas y UniStack estará listo desde el primer día.",
                color = UniStackColors.PrimaryDark.copy(alpha = 0.82f),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.fillMaxWidth(0.82f)
            )
        }
    }
}

private data class WelcomeFeature(
    val icon: ImageVector,
    val label: String,
    val description: String
)

@Composable
private fun WelcomeFeaturesGrid() {
    val features = remember {
        listOf(
            WelcomeFeature(
                icon = Icons.Rounded.School,
                label = "Organizar materias",
                description = "Registra cada materia con su color, su docente y su horario. " +
                    "Es la base sobre la que UniStack arma tus notas, tus tareas y tus recordatorios."
            ),
            WelcomeFeature(
                icon = Icons.Rounded.CalendarMonth,
                label = "Gestionar tareas",
                description = "Crea tareas con fecha de entrega y prioridad. " +
                    "Las que vencen pronto aparecen destacadas en tu panel de inicio."
            ),
            WelcomeFeature(
                icon = Icons.Rounded.Percent,
                label = "Seguir tus notas",
                description = "Anota tus calificaciones por corte y UniStack calcula tu promedio " +
                    "y cuánto necesitas en lo que falta para llegar a tu meta."
            ),
            WelcomeFeature(
                icon = Icons.Rounded.GridView,
                label = "Configurar módulos",
                description = "Activa solo lo que vayas a usar: notas, tareas, gastos u horario. " +
                    "Puedes cambiarlo cuando quieras desde Ajustes."
            )
        )
    }
    // La selección persiste tras cerrar el diálogo; por eso son dos estados y no uno.
    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var dialogIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Con tu configuración podrás",
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp
        )
        features.withIndex().chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (index, feature) ->
                    WelcomeFeatureCard(
                        feature = feature,
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            dialogIndex = index
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    dialogIndex?.let { index ->
        WelcomeFeatureDialog(
            feature = features[index],
            onDismiss = { dialogIndex = null }
        )
    }
}

@Composable
private fun WelcomeFeatureCard(
    feature: WelcomeFeature,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorSpec = spring<Color>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    val containerColor by animateColorAsState(
        targetValue = if (selected) UniStackColors.Primary else UniStackColors.SurfaceVariant,
        animationSpec = colorSpec,
        label = "feature-container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) UniStackColors.OnPrimary else UniStackColors.TextPrimary,
        animationSpec = colorSpec,
        label = "feature-content"
    )
    val iconBackground by animateColorAsState(
        targetValue = if (selected) UniStackColors.OnPrimary.copy(alpha = 0.18f) else UniStackColors.PrimaryLight,
        animationSpec = colorSpec,
        label = "feature-icon-bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) UniStackColors.OnPrimary else UniStackColors.Primary,
        animationSpec = colorSpec,
        label = "feature-icon-tint"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "feature-scale"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(AppShapes.MediumCard)
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = feature.icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )
        }
        Text(
            text = feature.label,
            color = contentColor,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun WelcomeFeatureDialog(
    feature: WelcomeFeature,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = feature.label,
                color = UniStackColors.TextPrimary,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = feature.description,
                color = UniStackColors.TextSecondary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Entendido",
                    color = UniStackColors.Primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = UniStackColors.Card,
        shape = AppShapes.LargeCard
    )
}

@Composable
private fun WelcomeTimeHint() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Schedule,
            contentDescription = null,
            tint = UniStackColors.Primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Tardarás menos de 2 minutos.",
            color = UniStackColors.TextSecondary,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
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
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = nameValidation.isValid,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SetupNameHero(name = name)
            SetupNameTitle()
            SetupNameInput(
                name = name,
                nameValidation = nameValidation,
                onNameChange = onNameChange
            )
            SetupNameInfoCard()
        }
    }
}

@Composable
private fun SetupNameHero(name: String) {
    val glowAlpha = if (UniStackColors.IsDarkTheme) 0.36f else 0.18f
    val cardColor = if (UniStackColors.IsDarkTheme) UniStackColors.SurfaceVariant else UniStackColors.Card

    val badgeFloat = floatingOffset(travel = 7f, durationMillis = 2800, label = "name-badge-float")
    val badgeTilt = floatingOffset(travel = 2.5f, durationMillis = 3600, label = "name-badge-tilt")
    val sparkleBig = floatingOffset(travel = 0.22f, durationMillis = 1500, label = "name-sparkle-big")
    val sparkleSmall = floatingOffset(travel = 0.28f, durationMillis = 1900, label = "name-sparkle-small")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(156.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            UniStackColors.Primary.copy(alpha = glowAlpha),
                            UniStackColors.Primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = UniStackColors.Primary,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-88).dp, y = (-48).dp)
                .graphicsLayer {
                    scaleX = 1f + sparkleBig
                    scaleY = 1f + sparkleBig
                    rotationZ = sparkleBig * 45f
                    alpha = 0.72f + sparkleBig
                }
                .size(23.dp)
        )
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = UniStackColors.Primary,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-102).dp, y = (-22).dp)
                .graphicsLayer {
                    scaleX = 1f + sparkleSmall
                    scaleY = 1f + sparkleSmall
                    rotationZ = -sparkleSmall * 55f
                    alpha = 0.66f + sparkleSmall
                }
                .size(10.dp)
        )
        Box(
            modifier = Modifier
                .size(width = 104.dp, height = 112.dp)
                .graphicsLayer {
                    translationY = badgeFloat.dp.toPx()
                    rotationZ = 12f + badgeTilt
                }
                .clip(RoundedCornerShape(16.dp))
                .background(cardColor)
                .border(
                    width = 1.dp,
                    color = UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.58f else 0.32f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(29.dp)
                            .clip(CircleShape)
                            .background(UniStackColors.Primary.copy(alpha = 0.92f))
                    )
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(UniStackColors.OnPrimary.copy(alpha = 0.18f))
                    )
                }
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(21.dp)
                        .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                        .background(UniStackColors.Primary)
                )
                // El nombre se va escribiendo aquí en vivo; sin nombre aún, barras de relleno.
                val badgeName = name.trim()
                Box(
                    modifier = Modifier
                        .width(84.dp)
                        .height(19.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(targetState = badgeName.isEmpty(), label = "name-badge-content") { isEmpty ->
                        if (isEmpty) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(64.dp)
                                        .height(6.dp)
                                        .clip(AppShapes.Pill)
                                        .background(
                                            UniStackColors.SoftOutline.copy(
                                                alpha = if (UniStackColors.IsDarkTheme) 0.45f else 0.72f
                                            )
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .width(78.dp)
                                        .height(6.dp)
                                        .clip(AppShapes.Pill)
                                        .background(
                                            UniStackColors.SoftOutline.copy(
                                                alpha = if (UniStackColors.IsDarkTheme) 0.28f else 0.54f
                                            )
                                        )
                                )
                            }
                        } else {
                            Text(
                                text = badgeName,
                                color = UniStackColors.TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SetupNameTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append("¿Cómo ")
                withStyle(SpanStyle(color = UniStackColors.Primary)) {
                    append("te llamas?")
                }
            },
            color = UniStackColors.TextPrimary,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Usaremos tu nombre para saludarte\ny personalizar tu panel.",
            color = UniStackColors.TextSecondary,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SetupNameInput(
    name: String,
    nameValidation: ValidationResult,
    onNameChange: (String) -> Unit
) {
    val showError = name.isNotBlank() && !nameValidation.isValid
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "Nombre",
            color = UniStackColors.Primary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal
        )
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(surface = Color.Transparent)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Escribe tu nombre",
                        color = UniStackColors.TextSecondary.copy(alpha = 0.72f),
                        fontSize = 15.sp
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = UniStackColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingText = {
                    if (showError) {
                        Text(nameValidation.errorMessage ?: "Ingresa un nombre válido")
                    }
                },
                shape = RoundedCornerShape(17.dp),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = UniStackColors.Primary,
                    unfocusedBorderColor = UniStackColors.Primary,
                    errorBorderColor = UniStackColors.Coral,
                    focusedTextColor = UniStackColors.TextPrimary,
                    unfocusedTextColor = UniStackColors.TextPrimary,
                    cursorColor = UniStackColors.Primary,
                    focusedContainerColor = UniStackColors.Card.copy(alpha = if (UniStackColors.IsDarkTheme) 0.12f else 0.76f),
                    unfocusedContainerColor = UniStackColors.Card.copy(alpha = if (UniStackColors.IsDarkTheme) 0.08f else 0.62f),
                    errorContainerColor = UniStackColors.Card.copy(alpha = if (UniStackColors.IsDarkTheme) 0.08f else 0.62f)
                ),
                isError = showError
            )
        }
    }
}

@Composable
private fun SetupNameInfoCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.76f else 0.9f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.PrimaryLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(23.dp)
                )
            }
            Text(
                text = "Podrás cambiarlo en cualquier momento\ndesde tu perfil.",
                color = UniStackColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f)
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
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SetupEducationHero()
            SetupEducationTitle()
            Text(
                text = "Selecciona una opción",
                color = UniStackColors.Primary,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
            EducationLevelGrid(
                selected = selected,
                onSelected = onSelected
            )
        }
    }
}

@Composable
private fun SetupEducationHero() {
    val glowAlpha = if (UniStackColors.IsDarkTheme) 0.38f else 0.16f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            UniStackColors.Primary.copy(alpha = glowAlpha),
                            UniStackColors.Primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.68f else 1f))
                .border(
                    width = 1.dp,
                    color = UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.72f else 0.34f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.School,
                contentDescription = null,
                tint = UniStackColors.Primary,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

@Composable
private fun SetupEducationTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append("¿Cuál es tu\n")
                withStyle(SpanStyle(color = UniStackColors.Primary)) {
                    append("nivel de estudio?")
                }
            },
            color = UniStackColors.TextPrimary,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Esto nos ayuda a adaptar UniStack\na tu etapa académica.",
            color = UniStackColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EducationLevelGrid(
    selected: EducationLevel,
    onSelected: (EducationLevel) -> Unit
) {
    val options = listOf(
        EducationLevel.PRIMARY to EducationLevelCardContent(
            title = "Primaria",
            caption = "Etapa básica",
            icon = Icons.AutoMirrored.Rounded.MenuBook
        ),
        EducationLevel.SECONDARY to EducationLevelCardContent(
            title = "Secundaria",
            caption = "Colegio",
            icon = Icons.AutoMirrored.Rounded.Assignment
        ),
        EducationLevel.UNIVERSITY to EducationLevelCardContent(
            title = "Universidad",
            caption = "Educación superior",
            icon = Icons.Rounded.School
        ),
        EducationLevel.OTHER to EducationLevelCardContent(
            title = "Otro",
            caption = "Personalizado",
            icon = Icons.Rounded.GridView
        )
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (level, content) ->
                    EducationLevelCard(
                        content = content,
                        selected = selected == level,
                        onClick = { onSelected(level) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EducationLevelCard(
    content: EducationLevelCardContent,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(15.dp)
    val cardColor = if (selected) {
        UniStackColors.PrimaryLight
    } else {
        UniStackColors.Card
    }

    UniCard(
        modifier = modifier
            .height(136.dp)
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
        color = cardColor,
        shape = shape,
        tonalElevation = 0.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.78f else 0.9f),
        borderWidth = if (selected) 1.4.dp else 1.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(UniStackColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = UniStackColors.OnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = content.icon,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(34.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = content.title,
                        color = UniStackColors.TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = content.caption,
                        color = UniStackColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private data class EducationLevelCardContent(
    val title: String,
    val caption: String,
    val icon: ImageVector
)

@Composable
fun SetupAcademicInfoScreen(
    educationLevel: EducationLevel,
    value: String,
    studyArea: StudyArea?,
    selectedProgram: String?,
    customProgram: String,
    isValid: Boolean,
    modifier: Modifier = Modifier,
    customProgramValidation: ValidationResult? = null,
    onValueChange: (String) -> Unit,
    onStudyAreaSelected: (StudyArea) -> Unit,
    onProgramSelected: (String) -> Unit,
    onCustomProgramChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    var areaExpanded by remember { mutableStateOf(false) }
    var programExpanded by remember { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 4,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = isValid || educationLevel != EducationLevel.UNIVERSITY,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        val area = studyArea
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SetupAcademicHero()
            SetupAcademicTitle()
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                SetupDropdownField(
                    label = "Área de estudio",
                    value = area?.let(::labelFor).orEmpty(),
                    options = StudyArea.entries.map(::labelFor),
                    enabled = true,
                    expanded = areaExpanded,
                    leadingIcon = area?.let(::studyAreaIcon) ?: Icons.Rounded.School,
                    optionIcon = { option -> studyAreaForLabel(option)?.let(::studyAreaIcon) ?: Icons.Rounded.GridView },
                    onExpandedChange = { expanded ->
                        areaExpanded = expanded
                        if (expanded) programExpanded = false
                    },
                    onOptionSelected = { selectedLabel ->
                        StudyArea.entries.firstOrNull { labelFor(it) == selectedLabel }?.let(onStudyAreaSelected)
                    }
                )

                SetupDropdownField(
                    label = "Programa o carrera",
                    value = selectedProgram.orEmpty(),
                    options = area?.let(::programsFor).orEmpty(),
                    enabled = area != null,
                    expanded = programExpanded,
                    leadingIcon = Icons.Rounded.School,
                    optionIcon = { option -> programIcon(option) },
                    onExpandedChange = { expanded ->
                        programExpanded = expanded
                        if (expanded) areaExpanded = false
                    },
                    onOptionSelected = onProgramSelected
                )

                AcademicProgramHelpCard(
                    selected = area == StudyArea.OTHER || selectedProgram == OTHER_OPTION,
                    onClick = {
                        onStudyAreaSelected(StudyArea.OTHER)
                        onProgramSelected(OTHER_OPTION)
                    }
                )

                if (area == StudyArea.OTHER || selectedProgram == OTHER_OPTION) {
                    SetupCustomProgramField(
                        value = customProgram,
                        validation = customProgramValidation,
                        onValueChange = onCustomProgramChange
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupAcademicHero() {
    val glowAlpha = if (UniStackColors.IsDarkTheme) 0.42f else 0.16f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            UniStackColors.Primary.copy(alpha = glowAlpha),
                            UniStackColors.Primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.7f else 1f))
                .border(
                    width = 1.dp,
                    color = UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.72f else 0.34f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.BusinessCenter,
                contentDescription = null,
                tint = UniStackColors.Primary,
                modifier = Modifier.size(39.dp)
            )
        }
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = UniStackColors.Primary.copy(alpha = 0.86f),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 72.dp, y = 28.dp)
                .size(8.dp)
        )
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = UniStackColors.Primary.copy(alpha = 0.72f),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-70).dp, y = (-18).dp)
                .size(7.dp)
        )
    }
}

@Composable
private fun SetupAcademicTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append("¿Cuál es tu ")
                withStyle(SpanStyle(color = UniStackColors.Primary)) {
                    append("carrera")
                }
                append("\no ")
                withStyle(SpanStyle(color = UniStackColors.Primary)) {
                    append("programa?")
                }
            },
            color = UniStackColors.TextPrimary,
            fontSize = 27.sp,
            lineHeight = 31.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Primero elige tu área de estudio\ny luego tu carrera.",
            color = UniStackColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AcademicProgramHelpCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.SurfaceVariant,
        shape = RoundedCornerShape(15.dp),
        tonalElevation = 0.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.78f else 0.9f),
        borderWidth = if (selected) 1.4.dp else 1.dp,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.86f else 1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "No encuentras tu carrera?",
                    color = UniStackColors.TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Podrás agregarla manualmente.",
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = if (selected) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = if (selected) UniStackColors.OnPrimary else UniStackColors.Primary,
                modifier = Modifier
                    .size(if (selected) 24.dp else 26.dp)
                    .then(
                        if (selected) {
                            Modifier
                                .clip(CircleShape)
                                .background(UniStackColors.Primary)
                                .padding(4.dp)
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
private fun SetupCustomProgramField(
    value: String,
    validation: ValidationResult?,
    onValueChange: (String) -> Unit
) {
    val showError = value.isNotBlank() && validation?.isValid == false
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(surface = Color.Transparent)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(
                    text = "Nombre del programa",
                    color = UniStackColors.TextSecondary,
                    fontWeight = FontWeight.Normal
                )
            },
            placeholder = {
                Text(
                    text = "Ej: Ingeniería Biomédica",
                    color = UniStackColors.TextSecondary.copy(alpha = 0.72f)
                )
            },
            shape = RoundedCornerShape(17.dp),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = UniStackColors.Primary,
                unfocusedBorderColor = UniStackColors.SoftOutline,
                errorBorderColor = UniStackColors.Coral,
                focusedTextColor = UniStackColors.TextPrimary,
                unfocusedTextColor = UniStackColors.TextPrimary,
                cursorColor = UniStackColors.Primary,
                focusedContainerColor = UniStackColors.Card.copy(alpha = if (UniStackColors.IsDarkTheme) 0.16f else 0.76f),
                unfocusedContainerColor = UniStackColors.Card.copy(alpha = if (UniStackColors.IsDarkTheme) 0.1f else 0.62f),
                errorContainerColor = UniStackColors.Card.copy(alpha = if (UniStackColors.IsDarkTheme) 0.1f else 0.62f)
            ),
            supportingText = {
                if (showError) {
                    Text(validation?.errorMessage ?: "Ingresa un programa válido")
                }
            },
            isError = showError
        )
    }
}

private fun studyAreaForLabel(label: String): StudyArea? =
    StudyArea.entries.firstOrNull { labelFor(it) == label }

private fun studyAreaIcon(area: StudyArea): ImageVector = when (area) {
    StudyArea.ENGINEERING_TECHNOLOGY -> Icons.Rounded.GridView
    StudyArea.ECONOMICS_BUSINESS -> Icons.Rounded.BarChart
    StudyArea.LAW_POLITICS -> Icons.Rounded.BusinessCenter
    StudyArea.HEALTH_SCIENCES -> Icons.Rounded.CheckCircle
    StudyArea.EDUCATION -> Icons.AutoMirrored.Rounded.MenuBook
    StudyArea.ARTS_DESIGN -> Icons.Rounded.AutoAwesome
    StudyArea.SOCIAL_SCIENCES -> Icons.Rounded.Person
    StudyArea.BASIC_SCIENCES -> Icons.Rounded.GridView
    StudyArea.OTHER -> Icons.Rounded.AutoAwesome
}

private fun programIcon(option: String): ImageVector =
    if (option == OTHER_OPTION) Icons.Rounded.AutoAwesome else Icons.Rounded.School

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
        selectedScale == GradingScale.ZERO_TO_HUNDRED -> SetupScaleChoice.HUNDRED
        else -> SetupScaleChoice.CUSTOM
    }
    SetupScaffold(
        onBackClick = onBackClick,
        step = 5,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = isValid,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            SetupScaleHero()
            SetupScaleTitle()
            ScaleTypeSection(
                selectedChoice = selectedChoice,
                onChoiceSelected = { choice ->
                    when (choice) {
                        SetupScaleChoice.FIVE -> onScaleSelected(GradingScale.ZERO_TO_FIVE)
                        SetupScaleChoice.HUNDRED -> {
                            onScaleSelected(GradingScale.ZERO_TO_HUNDRED)
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
                if (selectedScale == GradingScale.CUSTOM) {
                    ConfirmedScaleRangeRow(
                        customGradeMax = customGradeMax,
                        onEditClick = onEditCustomGradeRange
                    )
                }
                GradeGoalInputRow(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Nota mínima para aprobar",
                    subtitle = "Ingresa la nota mínima aprobatoria.",
                    value = passingGrade,
                    onValueChange = onPassingGradeChange
                )
                GradeGoalInputRow(
                    icon = Icons.Rounded.BarChart,
                    title = "Promedio objetivo",
                    subtitle = "¿Qué promedio quieres alcanzar?",
                    value = targetAverage,
                    onValueChange = onTargetAverageChange
                )
                ScaleInfoCard()
                val waitingForCustomRange = selectedScale == GradingScale.CUSTOM && !customGradeRangeConfirmed
                if (!isValid && !waitingForCustomRange) {
                    Text(
                        text = "Revisa que las notas estén dentro de la escala y que el promedio objetivo sea al menos la nota mínima.",
                        color = UniStackColors.Coral,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupScaleHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.5f else 0.24f),
                            UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.8f else 1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.BarChart,
                contentDescription = null,
                tint = UniStackColors.Primary,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun SetupScaleTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "¿Cómo es la escala de tus notas?",
            color = UniStackColors.TextPrimary,
            fontSize = 22.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Esto nos permite calcular tus promedios y metas\nde forma precisa.",
            color = UniStackColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ScaleTypeSection(
    selectedChoice: SetupScaleChoice,
    onChoiceSelected: (SetupScaleChoice) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "Tipo de escala",
            color = UniStackColors.TextPrimary,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ScaleTypeCard(
                option = ScaleTypeOption(
                    choice = SetupScaleChoice.FIVE,
                    badge = "0.0 - 5.0",
                    title = "0.0 a 5.0",
                    subtitle = "Escala de 0.0\nhasta 5.0",
                    icon = null
                ),
                selected = selectedChoice == SetupScaleChoice.FIVE,
                onClick = { onChoiceSelected(SetupScaleChoice.FIVE) },
                modifier = Modifier.weight(1f)
            )
            ScaleTypeCard(
                option = ScaleTypeOption(
                    choice = SetupScaleChoice.HUNDRED,
                    badge = "0 - 100",
                    title = "0 a 100",
                    subtitle = "Escala de 0\nhasta 100",
                    icon = null
                ),
                selected = selectedChoice == SetupScaleChoice.HUNDRED,
                onClick = { onChoiceSelected(SetupScaleChoice.HUNDRED) },
                modifier = Modifier.weight(1f)
            )
            ScaleTypeCard(
                option = ScaleTypeOption(
                    choice = SetupScaleChoice.CUSTOM,
                    badge = null,
                    title = "Personalizada",
                    subtitle = "Definir mi propia\nescala",
                    icon = Icons.Rounded.AutoAwesome
                ),
                selected = selectedChoice == SetupScaleChoice.CUSTOM,
                onClick = { onChoiceSelected(SetupScaleChoice.CUSTOM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ScaleTypeCard(
    option: ScaleTypeOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(15.dp)
    UniCard(
        modifier = modifier
            .height(120.dp)
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
        shape = shape,
        tonalElevation = 0.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.78f else 0.9f),
        borderWidth = if (selected) 1.4.dp else 1.dp,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 9.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScaleRadioDot(
                selected = selected,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 1.dp, y = 1.dp)
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ScaleBadge(
                    text = option.badge,
                    icon = option.icon,
                    selected = selected
                )
                Text(
                    text = option.title,
                    color = UniStackColors.TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = option.subtitle,
                    color = UniStackColors.TextSecondary,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ScaleRadioDot(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(18.dp)
            .clip(CircleShape)
            .border(
                width = 1.6.dp,
                color = if (selected) UniStackColors.Primary else UniStackColors.TextSecondary.copy(alpha = 0.72f),
                shape = CircleShape
            )
            .background(if (selected) UniStackColors.PrimaryLight else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.Primary)
            )
        }
    }
}

@Composable
private fun ScaleBadge(
    text: String?,
    icon: ImageVector?,
    selected: Boolean
) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(
                if (selected) UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.88f else 1f)
                else UniStackColors.SurfaceVariant.copy(alpha = if (UniStackColors.IsDarkTheme) 0.82f else 0.92f)
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = UniStackColors.TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = text.orEmpty(),
                color = if (selected) UniStackColors.Primary else UniStackColors.TextPrimary,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ConfirmedScaleRangeRow(
    customGradeMax: Double,
    onEditClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.SurfaceVariant.copy(alpha = if (UniStackColors.IsDarkTheme) 0.52f else 0.72f),
        shape = RoundedCornerShape(13.dp),
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.72f else 0.9f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Rango confirmado: 0 a ${customGradeMax.toInt()}",
                color = UniStackColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onEditClick) {
                Text("Cambiar", color = UniStackColors.Primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun GradeGoalInputRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.SurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        borderColor = Color.Transparent,
        borderWidth = 0.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.86f else 1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(21.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    color = UniStackColors.TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = UniStackColors.TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
            GradeValueField(
                value = value,
                onValueChange = onValueChange
            )
        }
    }
}

@Composable
private fun GradeValueField(
    value: String,
    onValueChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(11.dp)
    Box(
        modifier = Modifier
            .width(92.dp)
            .height(44.dp)
            .clip(shape)
            .border(1.dp, UniStackColors.SoftOutline, shape)
            .background(Color.Transparent)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = UniStackColors.TextPrimary,
                fontSize = 16.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(UniStackColors.Primary),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
    }
}

@Composable
private fun ScaleInfoCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.SurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        borderColor = Color.Transparent,
        borderWidth = 0.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "i",
                    color = UniStackColors.OnPrimary,
                    fontSize = 19.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "Puedes cambiar esta configuración en cualquier momento\ndesde Ajustes.",
                color = UniStackColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private data class ScaleTypeOption(
    val choice: SetupScaleChoice,
    val badge: String?,
    val title: String,
    val subtitle: String,
    val icon: ImageVector?
)

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
    val remaining = 100.0 - total
    var countExpanded by remember { mutableStateOf(false) }
    var customCountSelected by remember { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = 6,
        modifier = modifier,
        actions = {
            AcademicPeriodsBottomActions(
                enabled = isValid,
                onContinueClick = onContinueClick
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AcademicPeriodsTitle()
            AcademicDistributionCard(
                label = label,
                weights = weights,
                total = total,
                remaining = remaining,
                isValid = isValid,
                countExpanded = countExpanded,
                customCountSelected = customCountSelected,
                onCountExpandedChange = { countExpanded = it },
                onLabelSelected = onLabelSelected,
                onCountSelected = { count, isCustom ->
                    customCountSelected = isCustom
                    countExpanded = false
                    onCountSelected(count)
                },
                onWeightChange = onWeightChange
            )
        }
    }
}

@Composable
private fun AcademicPeriodsTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "¿Cómo se divide tu nota final?",
            color = UniStackColors.TextPrimary,
            fontSize = 24.sp,
            lineHeight = 27.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Elige el sistema de evaluación y distribuye\nel 100% de tu nota final.",
            color = UniStackColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AcademicDistributionCard(
    label: AcademicPeriodLabel,
    weights: List<String>,
    total: Double,
    remaining: Double,
    isValid: Boolean,
    countExpanded: Boolean,
    customCountSelected: Boolean,
    onCountExpandedChange: (Boolean) -> Unit,
    onLabelSelected: (AcademicPeriodLabel) -> Unit,
    onCountSelected: (Int, Boolean) -> Unit,
    onWeightChange: (Int, String) -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.78f else 0.9f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Tipo de evaluación",
                color = UniStackColors.TextPrimary,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            EvaluationTypeSegmentedControl(
                selected = label,
                onSelected = onLabelSelected
            )
            Text(
                text = "Momentos de evaluación dentro del semestre.",
                color = UniStackColors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            SetupDivider()
            Text(
                text = "Cantidad de ${label.plural.lowercase()}",
                color = UniStackColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
            PeriodCountDropdown(
                label = label,
                count = weights.size,
                expanded = countExpanded,
                customCountSelected = customCountSelected,
                onExpandedChange = onCountExpandedChange,
                onCountSelected = onCountSelected
            )
            if (weights.isNotEmpty()) {
                SetupDivider()
                PeriodDistributionSection(
                    label = label,
                    weights = weights,
                    total = total,
                    isValid = isValid,
                    onWeightChange = onWeightChange
                )
            }
            PeriodSummaryCard(
                total = total,
                remaining = remaining,
                isValid = isValid
            )
        }
    }
}

@Composable
private fun EvaluationTypeSegmentedControl(
    selected: AcademicPeriodLabel,
    onSelected: (AcademicPeriodLabel) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(UniStackColors.SurfaceVariant.copy(alpha = if (UniStackColors.IsDarkTheme) 0.62f else 0.82f))
            .border(
                width = 1.dp,
                color = UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.74f else 0.9f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AcademicPeriodLabel.entries.forEach { option ->
            EvaluationSegment(
                label = periodLabelTitle(option),
                selected = selected == option,
                onClick = { onSelected(option) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EvaluationSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) UniStackColors.Primary else Color.Transparent)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = if (selected) UniStackColors.OnPrimary else UniStackColors.TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (selected) UniStackColors.OnPrimary else UniStackColors.TextPrimary,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PeriodCountDropdown(
    label: AcademicPeriodLabel,
    count: Int,
    expanded: Boolean,
    customCountSelected: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCountSelected: (Int, Boolean) -> Unit
) {
    val density = LocalDensity.current
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "period-count-dropdown-arrow")
    var anchorWidth by remember { mutableIntStateOf(0) }
    val shape = RoundedCornerShape(12.dp)
    val countLabel = when {
        count <= 0 -> "Selecciona una cantidad"
        customCountSelected && count == 6 -> "Otro (personalizado)"
        else -> "$count ${label.plural.lowercase()}"
    }
    val options = buildList {
        (2..6).forEach { add(PeriodCountOption(it, "$it ${label.plural.lowercase()}")) }
        add(PeriodCountOption(null, "Otro (personalizado)"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { anchorWidth = it.size.width }
    ) {
        UniCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            color = UniStackColors.SurfaceVariant,
            shape = shape,
            tonalElevation = 0.dp,
            borderColor = if (expanded) UniStackColors.Primary else UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.82f else 0.92f),
            borderWidth = if (expanded) 1.4.dp else 1.dp,
            onClick = { onExpandedChange(!expanded) },
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = countLabel,
                    color = if (count <= 0) UniStackColors.TextSecondary else UniStackColors.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = if (count <= 0) FontWeight.Normal else FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = UniStackColors.TextPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .padding(1.dp)
                        .rotate(rotation + 90f)
                )
            }
        }

        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(12.dp)),
            colorScheme = MaterialTheme.colorScheme.copy(surface = UniStackColors.Card)
        ) {
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                offset = androidx.compose.ui.unit.DpOffset(0.dp, 6.dp),
                modifier = Modifier
                    .then(
                        if (anchorWidth > 0) Modifier.width(with(density) { anchorWidth.toDp() }) else Modifier.fillMaxWidth()
                    )
                    .background(
                        UniStackColors.SurfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = UniStackColors.TextPrimary,
                                fontSize = 15.sp,
                                lineHeight = 19.sp
                            )
                        },
                        onClick = {
                            onCountSelected(option.count ?: 6, option.count == null)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodDistributionSection(
    label: AcademicPeriodLabel,
    weights: List<String>,
    total: Double,
    isValid: Boolean,
    onWeightChange: (Int, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Distribución de la nota",
                color = UniStackColors.TextPrimary,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Total asignado: ${formatSetupPercent(total)}%",
                    color = if (isValid) UniStackColors.Green else UniStackColors.TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    fontWeight = if (isValid) FontWeight.SemiBold else FontWeight.Normal
                )
                Icon(
                    imageVector = if (isValid) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (isValid) UniStackColors.Green else UniStackColors.TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        UniCard(
            modifier = Modifier.fillMaxWidth(),
            color = UniStackColors.SurfaceVariant.copy(alpha = if (UniStackColors.IsDarkTheme) 0.46f else 0.72f),
            shape = RoundedCornerShape(12.dp),
            tonalElevation = 0.dp,
            borderColor = Color.Transparent,
            borderWidth = 0.dp,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                weights.forEachIndexed { index, value ->
                    PeriodWeightRow(
                        label = "${label.singular} ${index + 1}",
                        index = index,
                        value = value,
                        onValueChange = { onWeightChange(index, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodWeightRow(
    label: String,
    index: Int,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(UniStackColors.Primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = (index + 1).toString(),
                color = UniStackColors.OnPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = label,
            color = UniStackColors.TextPrimary,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
        CompactPercentField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(88.dp)
        )
    }
}

@Composable
private fun CompactPercentField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(9.dp)
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .border(1.dp, UniStackColors.SoftOutline, shape)
            .background(Color.Transparent)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = UniStackColors.TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(UniStackColors.Primary),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
        Text(
            text = "%",
            color = UniStackColors.TextPrimary,
            fontSize = 12.sp,
            lineHeight = 14.sp
        )
    }
}

@Composable
private fun PeriodSummaryCard(
    total: Double,
    remaining: Double,
    isValid: Boolean
) {
    val accent = if (isValid) UniStackColors.Green else UniStackColors.Primary
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = if (isValid) UniStackColors.GreenLight else UniStackColors.SurfaceVariant,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        borderColor = Color.Transparent,
        borderWidth = 0.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = if (UniStackColors.IsDarkTheme) 0.18f else 0.14f))
                    .border(2.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Percent,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp)
                )
            }
            PeriodSummaryMetric(
                title = "Total asignado",
                value = "${formatSetupPercent(total)}%",
                color = accent,
                modifier = Modifier.weight(1f)
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(34.dp)
                    .background(UniStackColors.SoftOutline.copy(alpha = 0.72f))
            )
            PeriodSummaryMetric(
                title = "Restante",
                value = "${formatSetupPercent(remaining)}%",
                color = when {
                    isValid -> UniStackColors.Green
                    remaining < 0 -> UniStackColors.Coral
                    else -> UniStackColors.Primary
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PeriodSummaryMetric(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            color = UniStackColors.TextSecondary,
            fontSize = 11.sp,
            lineHeight = 13.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 20.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AcademicPeriodsBottomActions(
    enabled: Boolean,
    onContinueClick: () -> Unit
) {
    UniStackButton(
        text = "Continuar",
        onClick = onContinueClick,
        enabled = enabled,
        trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = UniStackColors.TextSecondary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (enabled) "Tu distribución está completa." else "Completa el 100% para continuar.",
            color = UniStackColors.TextSecondary,
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
    }
}

private data class PeriodCountOption(
    val count: Int?,
    val label: String
)

private fun periodLabelTitle(label: AcademicPeriodLabel): String = when (label) {
    AcademicPeriodLabel.PERIOD -> "Períodos"
    AcademicPeriodLabel.CORTE -> "Cortes"
}

private fun formatSetupPercent(value: Double): String {
    val normalized = if (kotlin.math.abs(value) < 0.0001) 0.0 else value
    return if (kotlin.math.abs(normalized - normalized.roundToInt()) < 0.0001) {
        normalized.roundToInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", normalized)
    }
}

@Composable
private fun SetupDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.72f else 0.86f))
    )
}

@Composable
private fun CustomGradeRangeSelector(
    customGradeMax: Double,
    onCustomGradeMaxChange: (Double) -> Unit,
    onConfirmClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.PrimaryLight,
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
                fontWeight = FontWeight.Normal
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
                    Text(mark, color = UniStackColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Normal)
                }
            }
            UniStackButton(text = "Confirmar rango", onClick = onConfirmClick)
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
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SetupModulesHero()
            SetupModulesTitle()
            SetupModuleSelectionList(
                selectedValues = selectedModules,
                onToggle = onToggleModule
            )
            SetupModulesInfoCard()
        }
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
    val weights = periodWeights.filter { it.isNotBlank() }
    SetupScaffold(
        onBackClick = onBackClick,
        step = 8,
        modifier = modifier,
        actions = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                UniStackButton(
                    text = "Volver",
                    onClick = onBackClick,
                    variant = UniStackButtonVariant.Outlined,
                    modifier = Modifier.weight(0.9f)
                )
                UniStackButton(
                    text = "Confirmar",
                    onClick = onConfirmClick,
                    trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            SetupSummaryHeader()
            SummaryInfoCard(
                icon = Icons.Rounded.School,
                title = "Información académica"
            ) {
                SummaryKeyValueRow("Nombre", name.ifBlank { "Usuario" })
                SummaryKeyValueRow("Nivel de estudio", educationLevel.label())
                SummaryKeyValueRow("Área de estudio", studyArea?.let(::labelFor) ?: "Sin definir")
                SummaryKeyValueRow("Carrera", program.ifBlank { "Sin definir" })
            }
            SummaryInfoCard(
                icon = Icons.Rounded.BarChart,
                title = "Escala de notas"
            ) {
                SummaryKeyValueRow("Escala", gradingScale.summaryLabel(customGradeMax))
                SummaryKeyValueRow("Nota mínima", passingGrade)
                SummaryKeyValueRow("Promedio objetivo", targetAverage)
            }
            SummaryInfoCard(
                icon = Icons.Rounded.Percent,
                title = "Evaluación"
            ) {
                SummaryKeyValueRow("Sistema", periodLabel.singular)
                SummaryKeyValueRow(
                    "Cantidad",
                    if (weights.isEmpty()) "Sin definir" else "${weights.size} ${periodLabel.plural.lowercase()}"
                )
                SummaryDistributionRow(weights = weights)
            }
            SummaryInfoCard(
                icon = Icons.Rounded.GridView,
                title = "Módulos activos"
            ) {
                SummaryModulesList(enabledModules = enabledModules)
            }
            SetupSummaryNoticeCard()
        }
    }
}

@Composable
fun SetupFinishScreen(
    name: String,
    educationLevel: EducationLevel,
    studyArea: StudyArea?,
    selectedProgram: String?,
    customProgram: String,
    academicInfo: String,
    gradingScale: GradingScale,
    customGradeMax: Double,
    periodLabel: AcademicPeriodLabel,
    periodWeights: List<String>,
    enabledModules: Set<AppModule>,
    modifier: Modifier = Modifier,
    createSubjectEnabled: Boolean = true,
    onBackClick: () -> Unit,
    onCreateSubjectClick: () -> Unit,
    onGoHomeClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)
    val displayName = name.ifBlank { "Usuario" }
    val program = resolvedProgram(educationLevel, selectedProgram, customProgram, academicInfo).ifBlank { "Sin definir" }
    val moduleText = enabledModules.sortedBy { it.ordinal }.joinToString(" · ") { it.shortLabel() }
    val periodCount = if (periodWeights.isEmpty()) {
        "Sin definir"
    } else {
        "${periodWeights.size} ${periodLabel.plural.lowercase()}"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UniStackColors.Background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(top = 0.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SetupFinishTopBar(onBackClick = onBackClick)
            SetupFinishHero(name = displayName)
            FinishSemesterCard(
                education = educationLevel.label(),
                program = program,
                scale = gradingScale.summaryLabel(customGradeMax),
                periodLabel = "${periodLabel.plural} académicos",
                periodCount = periodCount,
                modules = moduleText.ifBlank { "Notas" }
            )
            FinishRecommendedCard()
            FinishActionButtons(
                createSubjectEnabled = createSubjectEnabled,
                onCreateSubjectClick = onCreateSubjectClick,
                onGoHomeClick = onGoHomeClick
            )
        }
    }
}

@Composable
private fun SetupModulesHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.34f else 0.16f),
                            UniStackColors.Primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.74f else 1f))
                .border(
                    width = 1.dp,
                    color = UniStackColors.Primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.58f else 0.26f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.GridView,
                contentDescription = null,
                tint = UniStackColors.Primary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun SetupModulesTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = "¿Qué quieres organizar con UniStack?",
            color = UniStackColors.TextPrimary,
            fontSize = 23.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Activa los módulos que necesitas.\nPuedes cambiar esta configuración en Ajustes.",
            color = UniStackColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SetupModuleSelectionList(
    selectedValues: Set<AppModule>,
    onToggle: (AppModule) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        setupModuleOptions().forEach { option ->
            SetupModuleSelectionCard(
                option = option,
                selected = option.module in selectedValues,
                onClick = { onToggle(option.module) }
            )
        }
    }
}

@Composable
private fun SetupModuleSelectionCard(
    option: ModuleOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
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
        shape = RoundedCornerShape(15.dp),
        tonalElevation = 0.dp,
        borderColor = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline.copy(alpha = 0.9f),
        borderWidth = if (selected) 1.3.dp else 1.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            SetupPurpleIconBox(icon = option.icon, size = 42.dp, iconSize = 23.dp)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = option.label,
                    color = UniStackColors.TextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = option.description,
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SetupModuleCheckBox(selected = selected)
        }
    }
}

@Composable
private fun SetupModuleCheckBox(selected: Boolean) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(shape)
            .background(if (selected) UniStackColors.Primary else Color.Transparent)
            .border(
                width = if (selected) 0.dp else 2.dp,
                color = if (selected) Color.Transparent else UniStackColors.TextSecondary.copy(alpha = 0.38f),
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = UniStackColors.OnPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun SetupModulesInfoCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.SurfaceVariant,
        shape = RoundedCornerShape(15.dp),
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline.copy(alpha = 0.78f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            SetupInfoDot(size = 38.dp)
            Text(
                text = buildAnnotatedString {
                    append("Siempre puedes activar o desactivar módulos\n")
                    append("desde ")
                    withStyle(SpanStyle(color = UniStackColors.Primary, fontWeight = FontWeight.SemiBold)) {
                        append("Ajustes")
                    }
                    append(" más adelante.")
                },
                color = UniStackColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SetupSummaryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            SetupPurpleIconBox(icon = Icons.AutoMirrored.Rounded.Assignment, size = 60.dp, iconSize = 30.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = "Resumen de tu\nconfiguración",
                color = UniStackColors.TextPrimary,
                fontSize = 23.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Revisa y confirma que todo esté correcto.",
                color = UniStackColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun SummaryInfoCard(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline.copy(alpha = 0.84f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SetupPurpleIconBox(icon = icon, size = 34.dp, iconSize = 19.dp)
                Text(
                    text = title,
                    color = UniStackColors.TextPrimary,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = UniStackColors.TextSecondary,
                    modifier = Modifier.size(21.dp)
                )
            }
            Column(
                modifier = Modifier.padding(start = 44.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SummaryKeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            modifier = Modifier.weight(0.85f)
        )
        Text(
            text = value,
            color = UniStackColors.TextPrimary,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.15f)
        )
    }
}

@Composable
private fun SummaryDistributionRow(weights: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Distribución",
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            modifier = Modifier.weight(0.85f)
        )
        if (weights.isEmpty()) {
            Text(
                text = "Sin definir",
                color = UniStackColors.TextPrimary,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.15f)
            )
        } else {
            Row(
                modifier = Modifier.weight(1.15f),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                weights.forEach { weight ->
                    SummaryPercentChip(label = "${weight.ifBlank { "0" }}%")
                }
            }
        }
    }
}

@Composable
private fun SummaryPercentChip(label: String) {
    Box(
        modifier = Modifier
            .height(21.dp)
            .clip(AppShapes.Pill)
            .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.88f else 1f))
            .padding(horizontal = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = UniStackColors.Primary,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SummaryModulesList(enabledModules: Set<AppModule>) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        enabledModules.sortedBy { it.ordinal }.forEach { module ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(UniStackColors.Primary)
                )
                Text(
                    text = module.shortLabel(),
                    color = UniStackColors.TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .height(20.dp)
                        .clip(AppShapes.Pill)
                        .background(UniStackColors.GreenLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.68f else 1f))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Activo",
                        color = UniStackColors.Green,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupSummaryNoticeCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.PrimaryLight,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        borderColor = Color.Transparent,
        borderWidth = 0.dp,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SetupInfoDot(size = 32.dp)
            Text(
                text = buildAnnotatedString {
                    append("Puedes cambiar cualquier configuración más\n")
                    append("adelante desde ")
                    withStyle(SpanStyle(color = UniStackColors.Primary, fontWeight = FontWeight.SemiBold)) {
                        append("Ajustes")
                    }
                    append(".")
                },
                color = UniStackColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SetupFinishTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = UniStackColors.TextPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
        Box(
            modifier = Modifier
                .clip(AppShapes.Pill)
                .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.38f else 0.88f))
                .border(1.dp, UniStackColors.SoftOutline.copy(alpha = 0.72f), AppShapes.Pill)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "9 de 9",
                color = UniStackColors.Primary,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SetupFinishHero(name: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(78.dp)
                .padding(start = 7.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = UniStackColors.OnPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Text(
            text = buildAnnotatedString {
                append("Todo listo,\n")
                withStyle(SpanStyle(color = UniStackColors.Primary)) {
                    append("$name.")
                }
            },
            color = UniStackColors.TextPrimary,
            fontSize = 31.sp,
            lineHeight = 33.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "UniStack ya está listo para\nacompañarte este semestre.",
            color = UniStackColors.TextSecondary,
            fontSize = 15.sp,
            lineHeight = 21.sp
        )
    }
}

@Composable
private fun FinishSemesterCard(
    education: String,
    program: String,
    scale: String,
    periodLabel: String,
    periodCount: String,
    modules: String
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = RoundedCornerShape(14.dp),
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline.copy(alpha = 0.86f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Tu semestre",
                    color = UniStackColors.TextPrimary,
                    fontSize = 17.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null,
                    tint = UniStackColors.TextSecondary,
                    modifier = Modifier.size(21.dp)
                )
            }
            SetupDivider()
            FinishSummaryWideRow(
                icon = Icons.Rounded.School,
                label = education,
                value = program
            )
            SetupDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FinishSummaryMiniBlock(
                    icon = Icons.Rounded.Percent,
                    label = "Sistema de notas",
                    value = scale,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(42.dp)
                        .background(UniStackColors.SoftOutline.copy(alpha = 0.62f))
                )
                FinishSummaryMiniBlock(
                    icon = Icons.Rounded.CalendarMonth,
                    label = periodLabel,
                    value = periodCount,
                    modifier = Modifier.weight(1f)
                )
            }
            SetupDivider()
            FinishSummaryWideRow(
                icon = Icons.Rounded.GridView,
                label = "Módulos activos",
                value = modules
            )
        }
    }
}

@Composable
private fun FinishSummaryWideRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        SetupPurpleIconBox(icon = icon, size = 38.dp, iconSize = 21.dp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                color = UniStackColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
            Text(
                text = value,
                color = UniStackColors.TextPrimary,
                fontSize = 14.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FinishSummaryMiniBlock(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        SetupPurpleIconBox(icon = icon, size = 36.dp, iconSize = 20.dp)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = UniStackColors.TextSecondary,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                color = UniStackColors.TextPrimary,
                fontSize = 13.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FinishRecommendedCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = RoundedCornerShape(15.dp),
        tonalElevation = 0.dp,
        borderColor = UniStackColors.Primary.copy(alpha = 0.86f),
        borderWidth = 1.2.dp,
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 13.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.68f else 1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(AppShapes.Pill)
                        .background(UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.84f else 1f))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PRIMER PASO RECOMENDADO",
                        color = UniStackColors.Primary,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Text(
                    text = "Crea tu primera materia",
                    color = UniStackColors.TextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "para empezar a organizar notas,\ntareas y gastos.",
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun FinishActionButtons(
    createSubjectEnabled: Boolean,
    onCreateSubjectClick: () -> Unit,
    onGoHomeClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        if (createSubjectEnabled) {
            UniStackButton(
                text = "Crear mi primera materia",
                onClick = onCreateSubjectClick,
                leadingIcon = Icons.Rounded.Add,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
            UniStackButton(
                text = "Ir al inicio",
                onClick = onGoHomeClick,
                variant = UniStackButtonVariant.Outlined,
                leadingIcon = Icons.Rounded.Home
            )
        } else {
            UniStackButton(
                text = "Ir al inicio",
                onClick = onGoHomeClick,
                leadingIcon = Icons.Rounded.Home,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    }
}

@Composable
private fun SetupPurpleIconBox(
    icon: ImageVector,
    size: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape((size.value * 0.28f).dp))
            .background(UniStackColors.PrimaryLight),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = UniStackColors.Primary,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
private fun SetupInfoDot(size: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(UniStackColors.Primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "i",
            color = UniStackColors.OnPrimary,
            fontSize = (size.value * 0.48f).sp,
            lineHeight = (size.value * 0.52f).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun setupModuleOptions(): List<ModuleOption> = listOf(
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
)

@Composable
private fun SetupScaffold(
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    step: Int? = null,
    welcome: Boolean = false,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize(),
        containerColor = UniStackColors.Background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp),
        topBar = {
            if (onBackClick != null || step != null) {
                SetupTopBar(
                    onBackClick = onBackClick,
                    step = step,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 22.dp, vertical = 5.dp)
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
                        .padding(top = 6.dp, bottom = 7.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
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
                .padding(top = if (step == null) 10.dp else 7.dp, bottom = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (welcome) {
                            Modifier
                                .clip(AppShapes.LargeCard)
                                .background(
                                    UniStackColors.Card
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
                verticalArrangement = Arrangement.spacedBy(14.dp),
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    color = UniStackColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        if (step != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 54.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                repeat(8) { index ->
                    val isActive = index < step.coerceIn(1, 8)
                    val barColor by animateColorAsState(
                        targetValue = if (isActive) {
                            UniStackColors.Primary
                        } else {
                            UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.5f else 0.72f)
                        },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "setup-progress-color"
                    )
                    val barHeight by animateDpAsState(
                        targetValue = if (isActive) 6.dp else 5.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "setup-progress-height"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(barHeight)
                            .clip(AppShapes.Pill)
                            .background(barColor)
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
            .background(UniStackColors.Primary),
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
                    fontWeight = FontWeight.SemiBold
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
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
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
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
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
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.weight(0.85f)
                    )
                    Text(
                        text = value,
                        color = UniStackColors.TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.SemiBold,
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
    GradingScale.ZERO_TO_HUNDRED -> "0 a 100"
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
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
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
    leadingIcon: ImageVector,
    optionIcon: (String) -> ImageVector,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "setup-dropdown-arrow")
    var anchorWidth by remember { mutableIntStateOf(0) }
    val shape = RoundedCornerShape(17.dp)
    val displayValue = value.ifBlank { "Seleccionar" }
    val isPlaceholder = value.isBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = label,
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 15.sp
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { anchorWidth = it.size.width }
        ) {
            UniCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                color = UniStackColors.Card,
                shape = shape,
                tonalElevation = 0.dp,
                borderColor = when {
                    expanded -> UniStackColors.Primary
                    enabled -> UniStackColors.SoftOutline.copy(alpha = if (UniStackColors.IsDarkTheme) 0.82f else 0.92f)
                    else -> UniStackColors.SoftOutline.copy(alpha = 0.48f)
                },
                borderWidth = if (expanded) 1.4.dp else 1.dp,
                enabled = enabled,
                onClick = {
                    keyboard?.hide()
                    onExpandedChange(!expanded)
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    DropdownIconBox(
                        icon = leadingIcon,
                        enabled = enabled,
                        modifier = Modifier.size(46.dp)
                    )
                    Text(
                        text = displayValue,
                        color = when {
                            !enabled -> UniStackColors.TextSecondary.copy(alpha = 0.58f)
                            isPlaceholder -> UniStackColors.TextPrimary
                            else -> UniStackColors.TextPrimary
                        },
                        fontSize = 17.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = if (enabled) UniStackColors.Primary else UniStackColors.TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(25.dp)
                            .rotate(rotation + 90f)
                    )
                }
            }

            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(17.dp)),
                colorScheme = MaterialTheme.colorScheme.copy(surface = UniStackColors.Card)
            ) {
                androidx.compose.material3.DropdownMenu(
                    expanded = expanded && enabled,
                    onDismissRequest = { onExpandedChange(false) },
                    offset = androidx.compose.ui.unit.DpOffset(0.dp, 8.dp),
                    modifier = Modifier
                        .then(
                            if (anchorWidth > 0) {
                                Modifier.width(with(density) { anchorWidth.toDp() })
                            } else {
                                Modifier.fillMaxWidth()
                            }
                        )
                        .heightIn(max = 300.dp)
                        .background(
                            UniStackColors.SurfaceVariant,
                            RoundedCornerShape(17.dp)
                        )
                ) {
                    options.forEach { option ->
                        val isSelected = option == value
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    color = if (isSelected) UniStackColors.Primary else UniStackColors.TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 17.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                DropdownIconBox(
                                    icon = optionIcon(option),
                                    enabled = true,
                                    modifier = Modifier.size(32.dp),
                                    iconSize = 17.dp
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
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .background(
                                    if (isSelected) UniStackColors.PrimaryLight.copy(alpha = if (UniStackColors.IsDarkTheme) 0.58f else 0.9f) else Color.Transparent,
                                    RoundedCornerShape(13.dp)
                                ),
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
}

@Composable
private fun DropdownIconBox(
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 25.dp
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(
                UniStackColors.PrimaryLight.copy(
                    alpha = when {
                        !enabled -> 0.42f
                        UniStackColors.IsDarkTheme -> 0.9f
                        else -> 1f
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) UniStackColors.Primary else UniStackColors.TextSecondary,
            modifier = Modifier.size(iconSize)
        )
    }
}
