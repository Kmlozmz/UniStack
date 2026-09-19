package com.unistack.app.feature_setup.presentation

import com.unistack.app.feature_user.domain.offerableModules
import com.unistack.app.core.design.components.ScaleZoneBar
import com.unistack.app.core.design.components.GradeStepperRow
import com.unistack.app.core.design.components.CutWheelCard
import com.unistack.app.core.design.components.CutBalanceNotice
import com.unistack.app.core.design.components.CutCountSection
import com.unistack.app.core.design.components.SetupEvenSplitAction
import com.unistack.app.core.design.components.gradeValueOf
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniSegmentedControl
import androidx.compose.material3.MotionScheme
import com.unistack.app.core.design.components.UniSwitch
import androidx.compose.ui.draw.alpha
import com.unistack.app.core.design.components.UniStackBrandPill
import com.unistack.app.core.design.components.UniStackBrandMark
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.unistack.app.core.utils.GradingScaleUtils
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.utils.ValidationResult
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.components.expressiveSelection
import com.unistack.app.core.design.components.rememberSelectionShape
import com.unistack.app.core.design.components.floatingOffset
import com.unistack.app.core.design.components.UniStackLogoMark
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import kotlin.math.roundToInt

import com.unistack.app.core.design.theme.LocalIsDarkTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
private object SetupRoutes {
    const val Welcome = "setup_welcome"
    const val Name = "setup_name"
    const val Profile = "setup_profile"
    const val Modules = "setup_modules"
    const val Scale = "setup_scale"
    const val Periods = "setup_periods"
    const val Term = "setup_term"
    const val TermDates = "setup_term_dates"
    const val TermCutDates = "setup_term_cut_dates"
    const val Permissions = "setup_permissions"
    const val Done = "setup_done"
}

/**
 * Numeración de los pasos con indicador. La bienvenida queda fuera: no pide datos y no
 * lleva barra de progreso.
 *
 * Módulos va antes que la escala y los periodos a propósito: si el usuario desactiva el
 * módulo de notas, esos dos pasos dejan de tener sentido y se saltan. Preguntándolos antes
 * se corría el riesgo de pedir datos que luego se descartaban.
 */
internal object SetupSteps {
    const val Name = 1
    const val Profile = 2
    const val Modules = 3
    const val Scale = 4
    const val Periods = 5

    /**
     * El periodo académico: cuándo empieza y cuándo acaba.
     *
     * Va después de los cortes porque se lee como lo que es —primero cómo se reparte la nota,
     * luego en qué tramo de calendario—, y porque las fechas de corte que se piden aquí
     * necesitan saber ya cuántos cortes hay.
     */
    const val Term = 6
    const val TermDates = 7
    const val TermCutDates = 8

    /**
     * Escala, cortes y periodo solo existen con el módulo de notas activo.
     *
     * Y las fechas de corte solo cuando alguien dice que las sabe, así que la longitud del
     * flujo depende de esa respuesta: prometer un paso que luego no llega deja la barra de
     * progreso mintiendo.
     */
    fun permissions(gradesEnabled: Boolean, cutDates: Boolean = false): Int = when {
        gradesEnabled && cutDates -> 9
        gradesEnabled -> 8
        else -> 4
    }

    /** El paso final siempre es el último, tenga el flujo la longitud que tenga. */
    fun done(gradesEnabled: Boolean, permissionsNeeded: Boolean, cutDates: Boolean = false): Int =
        permissions(gradesEnabled, cutDates) + if (permissionsNeeded) 1 else 0

    fun total(gradesEnabled: Boolean, permissionsNeeded: Boolean, cutDates: Boolean = false): Int =
        done(gradesEnabled, permissionsNeeded, cutDates)
}

private const val SETUP_EXIT_MILLIS = 220

@Composable
fun SetupFlow(
    onSetupFinished: (createFirstSubject: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isReplay: Boolean = false,
    onDismissReplay: (() -> Unit)? = null,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    // La longitud del flujo depende de si el usuario conserva el módulo de notas y de si
    // este Android pide el permiso de notificaciones en ejecución.
    val gradesEnabled = AppModule.GRADES in viewModel.enabledModules
    val permissionsNeeded = notificationPermissionRequired()
    val cutDates = viewModel.knowsCutDates == true
    val totalSteps = SetupSteps.total(gradesEnabled, permissionsNeeded, cutDates)
    val afterEvaluation = if (permissionsNeeded) SetupRoutes.Permissions else SetupRoutes.Done

    /*
     * El paso se decide en un solo sitio, y ese sitio es este.
     *
     * Antes lo declaraba cada pantalla al construir su propio encabezado, y dos llegaron a
     * decir que eran la octava: la de permisos calculaba su numero sin contar las fechas de
     * corte mientras el total si las contaba.
     */
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val pasoActual = when (navBackStackEntry?.destination?.route) {
        SetupRoutes.Name -> SetupSteps.Name
        SetupRoutes.Profile -> SetupSteps.Profile
        SetupRoutes.Modules -> SetupSteps.Modules
        SetupRoutes.Scale -> SetupSteps.Scale
        SetupRoutes.Periods -> SetupSteps.Periods
        SetupRoutes.Term -> SetupSteps.Term
        SetupRoutes.TermDates -> SetupSteps.TermDates
        SetupRoutes.TermCutDates -> SetupSteps.TermCutDates
        SetupRoutes.Permissions -> SetupSteps.permissions(gradesEnabled, cutDates)
        SetupRoutes.Done -> SetupSteps.done(gradesEnabled, permissionsNeeded, cutDates)
        // La bienvenida no pide nada, asi que no cuenta como paso ni lleva barra.
        else -> null
    }

    // El cierre del onboarding retira tambien el encabezado, que ya no vive dentro de el.
    var saliendo by remember { mutableStateOf(false) }
    val chromeAlpha by animateFloatAsState(
        targetValue = if (saliendo) 0f else 1f,
        animationSpec = tween(durationMillis = 320),
        label = "setup-chrome-alpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
    SetupProgressHeader(
        step = pasoActual,
        totalSteps = totalSteps,
        onBackClick = {
            if (!navController.navigateUp() && isReplay && onDismissReplay != null) {
                onDismissReplay()
            }
        },
        chromeAlpha = chromeAlpha
    )
    NavHost(
        navController = navController,
        startDestination = SetupRoutes.Welcome,
        modifier = Modifier.weight(1f),
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
            if (isReplay && onDismissReplay != null) {
                BackHandler(onBack = onDismissReplay)
            }
            SetupWelcomeScreen(
                onStartClick = { navController.navigate(SetupRoutes.Name) },
                onDismissClick = if (isReplay) onDismissReplay else null
            )
        }
        composable(SetupRoutes.Name) {
            SetupNameScreen(
                name = viewModel.preferredName,
                nameValidation = viewModel.nameValidation,
                totalSteps = totalSteps,
                onNameChange = viewModel::updatePreferredName,
                onBackClick = {
                    if (!navController.navigateUp() && isReplay && onDismissReplay != null) {
                        onDismissReplay()
                    }
                },
                onContinueClick = { navController.navigate(SetupRoutes.Profile) }
            )
        }
        composable(SetupRoutes.Profile) {
            SetupProfileScreen(
                studyArea = viewModel.studyArea,
                selectedProgram = viewModel.selectedProgram,
                customProgram = viewModel.customProgram,
                canContinue = viewModel.canContinueFromProfile,
                customProgramValidation = viewModel.customProgramValidation,
                totalSteps = totalSteps,
                institutionName = viewModel.institutionName,
                onInstitutionNameChange = viewModel::updateInstitutionName,
                onStudyAreaSelected = viewModel::updateStudyArea,
                onProgramSelected = viewModel::updateSelectedProgram,
                onCustomProgramChange = viewModel::updateCustomProgram,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Modules) },
                onSkipClick = {
                    viewModel.skipAcademicInfo()
                    navController.navigate(SetupRoutes.Modules)
                }
            )
        }
        composable(SetupRoutes.Scale) {
            SetupGradingScaleScreen(
                totalSteps = totalSteps,
                selectedScale = viewModel.gradingScale,
                scaleChosen = viewModel.scaleChosen,
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
            SetupGradingCutsScreen(
                weights = viewModel.gradingCutWeights,
                isValid = viewModel.areGradingCutsValid,
                totalSteps = totalSteps,
                onCountSelected = viewModel::updateGradingCutCount,
                onWeightChange = viewModel::updateGradingCutWeight,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Term) }
            )
        }
        composable(SetupRoutes.Term) {
            SetupTermTypeScreen(
                type = viewModel.termType,
                cutCount = viewModel.termCutCount,
                cutWeights = viewModel.gradingCutWeights,
                totalSteps = totalSteps,
                onTypeSelected = viewModel::updateTermType,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.TermDates) }
            )
        }
        composable(SetupRoutes.TermDates) {
            val tipo = viewModel.termType
            // Sin tipo no se llega aqui por navegacion normal, pero un proceso recreado si
            // puede aterrizar: se vuelve en vez de reventar. La fecha de inicio, en cambio,
            // es nula a proposito hasta que diga si su periodo ya empezo.
            if (tipo == null) {
                LaunchedEffect(Unit) { navController.navigateUp() }
            } else {
                SetupTermDatesScreen(
                    type = tipo,
                    name = viewModel.termName,
                    cutCount = viewModel.termCutCount,
                    start = viewModel.termStart,
                    plannedEnd = viewModel.termPlannedEnd,
                    knowsCutDates = if (viewModel.termCutCount > 1) viewModel.knowsCutDates else false,
                    totalSteps = totalSteps,
                    onStartChange = viewModel::updateTermStart,
                    onPlannedEndChange = viewModel::updateTermPlannedEnd,
                    onKnowsCutDatesChange = viewModel::updateKnowsCutDates,
                    onBackClick = { navController.navigateUp() },
                    onContinueClick = {
                        if (viewModel.knowsCutDates == true) {
                            navController.navigate(SetupRoutes.TermCutDates)
                        } else {
                            navController.navigate(afterEvaluation)
                        }
                    },
                    onSkipClick = {
                        viewModel.skipTermDates()
                        navController.navigate(afterEvaluation)
                    }
                )
            }
        }
        composable(SetupRoutes.TermCutDates) {
            val inicio = viewModel.termStart
            if (inicio == null) {
                LaunchedEffect(Unit) { navController.navigateUp() }
            } else {
                SetupTermCutDatesScreen(
                    start = inicio,
                    plannedEnd = viewModel.termPlannedEnd,
                    cutWeights = viewModel.gradingCutWeights,
                    cutEndDates = viewModel.cutEndDates,
                    isValid = viewModel.isTermValid,
                    totalSteps = totalSteps,
                    onCutDateChange = viewModel::updateCutEndDate,
                    onBackClick = { navController.navigateUp() },
                    onContinueClick = { navController.navigate(afterEvaluation) }
                )
            }
        }
        composable(SetupRoutes.Permissions) {
            SetupPermissionsScreen(
                step = SetupSteps.permissions(gradesEnabled, cutDates),
                totalSteps = totalSteps,
                onBackClick = { navController.navigateUp() },
                onContinueClick = { navController.navigate(SetupRoutes.Done) }
            )
        }
        composable(SetupRoutes.Modules) {
            SetupModulesScreen(
                selectedModules = viewModel.enabledModules,
                totalSteps = totalSteps,
                onToggleModule = viewModel::toggleModule,
                onBackClick = { navController.navigateUp() },
                onContinueClick = {
                    // Sin el módulo de notas, la escala y los periodos no aplican.
                    val next = if (gradesEnabled) SetupRoutes.Scale else afterEvaluation
                    navController.navigate(next)
                }
            )
        }
        composable(SetupRoutes.Done) {
            SetupDoneScreen(
                onExitingChange = { saliendo = it },
                name = viewModel.preferredName,
                studyArea = viewModel.studyArea,
                selectedProgram = viewModel.selectedProgram,
                customProgram = viewModel.customProgram,
                academicInfo = viewModel.academicInfo,
                gradingScale = viewModel.gradingScale,
                customGradeMax = viewModel.customGradeMax,
                passingGrade = viewModel.passingGradeText,
                targetAverage = viewModel.targetAverageText,
                cutWeights = viewModel.gradingCutWeights,
                enabledModules = viewModel.enabledModules,
                gradesEnabled = gradesEnabled,
                permissionsNeeded = permissionsNeeded,
                totalSteps = totalSteps,
                institutionName = viewModel.institutionName,
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
}

@Composable
fun SetupWelcomeScreen(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDismissClick: (() -> Unit)? = null
) {
    SetupScaffold(
        modifier = modifier,
        welcome = false,
        actions = {
            UniStackButton(
                text = stringResource(R.string.setup_btn_start),
                onClick = onStartClick,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
            if (onDismissClick != null) {
                UniStackButton(
                    text = stringResource(R.string.common_cancel),
                    onClick = onDismissClick,
                    variant = UniStackButtonVariant.Outlined
                )
            }
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
            color = MaterialTheme.colorScheme.onSurface,
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
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.primaryContainer)
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.30f else 0.24f))
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
                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.22f else 0.16f))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.setup_welcome_badge),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.setup_welcome_hero_lead))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(stringResource(R.string.setup_welcome_hero_accent))
                    }
                },
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = 30.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.setup_welcome_hero_desc),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
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
    val features = listOf(
        WelcomeFeature(
            icon = Icons.Rounded.School,
            label = stringResource(R.string.setup_welcome_feature1_title),
            description = stringResource(R.string.setup_welcome_feature1_desc)
        ),
        WelcomeFeature(
            icon = Icons.Rounded.CalendarMonth,
            label = stringResource(R.string.setup_welcome_feature2_title),
            description = stringResource(R.string.setup_welcome_feature2_desc)
        ),
        WelcomeFeature(
            icon = Icons.Rounded.Percent,
            label = stringResource(R.string.setup_welcome_feature3_title),
            description = stringResource(R.string.setup_welcome_feature3_desc)
        ),
        WelcomeFeature(
            icon = Icons.Rounded.GridView,
            label = stringResource(R.string.setup_welcome_feature4_title),
            description = stringResource(R.string.setup_welcome_feature4_desc)
        )
    )
    // La selección persiste tras cerrar el diálogo; por eso son dos estados y no uno.
    var selectedIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var dialogIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.setup_welcome_footer),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = colorSpec,
        label = "feature-container"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        animationSpec = colorSpec,
        label = "feature-content"
    )
    val iconBackground by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = colorSpec,
        label = "feature-icon-bg"
    )
    val iconTint by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
        animationSpec = colorSpec,
        label = "feature-icon-tint"
    )
    // Card de Material. El escalado del 4% al seleccionar se fue con el resto: la respuesta
    // al pulsar la pone el MotionScheme del tema, igual que en cualquier otra tarjeta.
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(MaterialTheme.shapes.small)
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
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = feature.label,
                color = MaterialTheme.colorScheme.onSurface,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.setup_welcome_btn_understood),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
fun SetupNameScreen(
    name: String,
    nameValidation: ValidationResult,
    onNameChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 6
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        step = SetupSteps.Name,
        totalSteps = totalSteps,
        modifier = modifier,
        // Aquí el teclado se superpone en vez de encoger la pantalla: subirlo todo dejaba
        // fuera de vista el hero, y con él el nombre escribiéndose en vivo en la tarjeta,
        // que es justo lo que da sentido a este paso mientras se teclea.
        overlayKeyboard = true,
        actions = {
            UniStackButton(
                text = stringResource(R.string.setup_btn_continue),
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
    val glowAlpha = if (LocalIsDarkTheme.current) 0.36f else 0.18f
    val cardColor = if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow

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
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
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
            tint = MaterialTheme.colorScheme.primary,
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
                .clip(MaterialTheme.shapes.medium)
                .background(cardColor)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.58f else 0.32f),
                    shape = MaterialTheme.shapes.medium
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.92f))
                    )
                    Box(
                        modifier = Modifier
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f))
                    )
                }
                Box(
                    modifier = Modifier
                        .width(50.dp)
                        .height(21.dp)
                        .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                        .background(MaterialTheme.colorScheme.primary)
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
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = if (LocalIsDarkTheme.current) 0.45f else 0.72f
                                            )
                                        )
                                )
                                Box(
                                    modifier = Modifier
                                        .width(78.dp)
                                        .height(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.outlineVariant.copy(
                                                alpha = if (LocalIsDarkTheme.current) 0.28f else 0.54f
                                            )
                                        )
                                )
                            }
                        } else {
                            Text(
                                text = badgeName,
                                color = MaterialTheme.colorScheme.onSurface,
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
                append(stringResource(R.string.setup_name_title_lead))
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(R.string.setup_name_title_accent))
                }
            },
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.setup_name_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            text = stringResource(R.string.setup_name_label),
            color = MaterialTheme.colorScheme.primary,
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
                        text = stringResource(R.string.setup_name_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        fontSize = 15.sp
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                },
                supportingText = {
                    if (showError) {
                        Text(nameValidation.errorMessage ?: stringResource(R.string.setup_name_error))
                    }
                },
                shape = MaterialTheme.shapes.medium,
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (LocalIsDarkTheme.current) 0.12f else 0.76f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (LocalIsDarkTheme.current) 0.08f else 0.62f),
                    errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (LocalIsDarkTheme.current) 0.08f else 0.62f)
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
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (LocalIsDarkTheme.current) 0.76f else 0.9f),
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
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(23.dp)
                )
            }
            Text(
                text = stringResource(R.string.setup_name_footer),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Perfil academico: que estudias y donde.
 *
 * Antes empezaba preguntando el nivel —primaria, secundaria, universidad, otro— y de esa
 * respuesta dependia lo que se preguntaba despues. La app es de educacion superior, asi que
 * el nivel dejo de ser una pregunta y este paso quedo en lo unico que siempre se rellenaba:
 * area, carrera e institucion.
 *
 * La institucion es opcional. El area y la carrera no: alimentan el catalogo de materias.
 */
@Composable
fun SetupProfileScreen(
    studyArea: StudyArea?,
    selectedProgram: String?,
    customProgram: String,
    canContinue: Boolean,
    onStudyAreaSelected: (StudyArea) -> Unit,
    onProgramSelected: (String) -> Unit,
    onCustomProgramChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
    customProgramValidation: ValidationResult? = null,
    totalSteps: Int = 6,
    institutionName: String = "",
    onInstitutionNameChange: (String) -> Unit = {}
) {
    var areaExpanded by remember { mutableStateOf(false) }
    var programExpanded by remember { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        step = SetupSteps.Profile,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            /*
             * Los botones salen cuando hay algo que continuar, no antes.
             *
             * Con las tres preguntas a la vista desde el principio, el boton llevaba ahi
             * apagado desde que se abria la pantalla. Apareciendo al elegir carrera, marca el
             * final de la conversacion en lugar de anunciarlo desde el principio.
             */
            if (selectedProgram == null) return@SetupScaffold
            UniStackButton(
                text = stringResource(R.string.setup_btn_continue),
                onClick = onContinueClick,
                enabled = canContinue,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
            UniStackButton(
                text = stringResource(R.string.setup_btn_skip_later),
                onClick = onSkipClick,
                variant = UniStackButtonVariant.Outlined
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

            /*
             * Una sola rama, sin rejilla de niveles delante.
             *
             * Aqui habia cuatro tarjetas —primaria, secundaria, universidad, otro— y tres
             * bloques que aparecian y desaparecian segun cual se tocara. La app es de
             * educacion superior, asi que preguntar el nivel era ofrecer una sola respuesta
             * util y tres caminos muertos. Lo que queda es lo que siempre se rellenaba.
             *
             * Y sin la rejilla delante sobra el rotulo «¿Que estudias?» que separaba una cosa
             * de otra: el titulo de la pantalla ya dice eso mismo, y dos preguntas casi
             * identicas apiladas se leen como un error.
             */
            SetupDropdownField(
                label = stringResource(R.string.setup_career_area_label),
                value = studyArea?.let(::labelFor).orEmpty(),
                options = StudyArea.entries.map(::labelFor),
                enabled = true,
                expanded = areaExpanded,
                leadingIcon = studyArea?.let(::studyAreaIcon) ?: Icons.Rounded.School,
                optionIcon = { option -> studyAreaForLabel(option)?.let(::studyAreaIcon) ?: Icons.Rounded.GridView },
                onExpandedChange = { expanded ->
                    areaExpanded = expanded
                    if (expanded) programExpanded = false
                },
                onOptionSelected = { selectedLabel ->
                    StudyArea.entries.firstOrNull { labelFor(it) == selectedLabel }?.let(onStudyAreaSelected)
                }
            )
            // La carrera solo aparece cuando ya hay area: sin ella su lista estaria vacia,
            // y un desplegable vacio invita a tocarlo para nada.
            Revelado(visible = studyArea != null) {
              SetupDropdownField(
                label = stringResource(R.string.setup_career_program_label),
                value = selectedProgram.orEmpty(),
                options = studyArea?.let(::programsFor).orEmpty(),
                enabled = studyArea != null,
                expanded = programExpanded,
                leadingIcon = Icons.Rounded.School,
                optionIcon = { option -> programIcon(option) },
                onExpandedChange = { expanded ->
                    programExpanded = expanded
                    if (expanded) areaExpanded = false
                },
                onOptionSelected = onProgramSelected
              )
            }
            // El atajo de «no encuentro la mia» acompana a la lista de carreras, asi que
            // aparece con ella.
            Revelado(visible = studyArea != null) {
                AcademicProgramHelpCard(
                    selected = studyArea == StudyArea.OTHER || isOtherOption(selectedProgram),
                    onClick = {
                        onStudyAreaSelected(StudyArea.OTHER)
                        onProgramSelected(otherOptionLabel())
                    }
                )
            }
            if (studyArea == StudyArea.OTHER || isOtherOption(selectedProgram)) {
                SetupCustomProgramField(
                    value = customProgram,
                    validation = customProgramValidation,
                    onValueChange = onCustomProgramChange
                )
            }
            // Y la universidad, que es opcional, solo tras haber contestado lo obligatorio.
            Revelado(visible = selectedProgram != null) {
                InstitutionField(
                    value = institutionName,
                    label = stringResource(R.string.setup_career_university_label),
                    placeholder = stringResource(R.string.setup_career_university_hint),
                    onValueChange = onInstitutionNameChange
                )
            }
        }
    }
}

@Composable
private fun SetupEducationHero() {
    val glowAlpha = if (LocalIsDarkTheme.current) 0.38f else 0.16f
    val heroFloat = floatingOffset(travel = 5f, durationMillis = 3000, label = "education-hero-float")

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
                            MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = heroFloat.dp.toPx() }
                .size(68.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (LocalIsDarkTheme.current) 0.68f else 1f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.72f else 0.34f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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
                append(stringResource(R.string.setup_career_title_lead))
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(R.string.setup_career_title_accent))
                }
            },
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.setup_career_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
        borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (LocalIsDarkTheme.current) 0.78f else 0.9f),
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
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (LocalIsDarkTheme.current) 0.86f else 1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.setup_career_not_found),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.setup_career_add_manually),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 16.sp
                )
            }
            Icon(
                imageVector = if (selected) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(if (selected) 24.dp else 26.dp)
                    .then(
                        if (selected) {
                            Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
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
internal fun SetupCustomProgramField(
    value: String,
    validation: ValidationResult?,
    onValueChange: (String) -> Unit,
    label: String = stringResource(R.string.setup_career_custom_label),
    placeholder: String = stringResource(R.string.setup_career_custom_hint)
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
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal
                )
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )
            },
            shape = MaterialTheme.shapes.medium,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (LocalIsDarkTheme.current) 0.16f else 0.76f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (LocalIsDarkTheme.current) 0.1f else 0.62f),
                errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (LocalIsDarkTheme.current) 0.1f else 0.62f)
            ),
            supportingText = {
                if (showError) {
                    Text(validation?.errorMessage ?: stringResource(R.string.setup_career_custom_error))
                }
            },
            isError = showError
        )
    }
}

internal fun studyAreaForLabel(label: String): StudyArea? =
    StudyArea.entries.firstOrNull { labelFor(it) == label }

internal fun studyAreaIcon(area: StudyArea): ImageVector = when (area) {
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

internal fun programIcon(option: String): ImageVector =
    if (isOtherOption(option)) Icons.Rounded.AutoAwesome else Icons.Rounded.School

@Composable
fun SetupGradingScaleScreen(
    selectedScale: GradingScale,
    /** Si ya ha elegido; con `false` no hay nada marcado y lo de abajo no existe todavia. */
    scaleChosen: Boolean,
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
    modifier: Modifier = Modifier,
    totalSteps: Int = 6
) {
    BackHandler(onBack = onBackClick)
    val selectedChoice = when {
        !scaleChosen -> null
        selectedScale == GradingScale.ZERO_TO_FIVE -> SetupScaleChoice.FIVE
        selectedScale == GradingScale.ZERO_TO_HUNDRED -> SetupScaleChoice.HUNDRED
        else -> SetupScaleChoice.CUSTOM
    }
    SetupScaffold(
        step = SetupSteps.Scale,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = stringResource(R.string.setup_btn_continue),
                onClick = onContinueClick,
                enabled = isValid,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        val scaleMax = if (selectedScale == GradingScale.CUSTOM) {
            customGradeMax
        } else {
            GradingScaleUtils.maxGradeFor(selectedScale)
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            /*
             * Ver la escala, no solo elegirla.
             *
             * Antes se elegía entre tres tarjetas y luego se escribían dos notas a mano en
             * sendos campos con teclado, sin nada que dijera qué significaban esos números
             * dentro del rango. La franja parte la escala en los tres tramos que de verdad
             * importan —lo que reprueba, lo que aprueba y lo que llega a tu meta— y se
             * redibuja al mover cualquiera de las dos cifras, así que la relación entre
             * ellas se ve antes de seguir.
             */
            SetupPlainTitle(
                title = stringResource(R.string.setup_scale_title),
                subtitle = stringResource(R.string.setup_scale_desc)
            )
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
            /*
             * Nada de esto existe hasta que hay escala, y entra escalonado.
             *
             * Salia todo montado con «0 a 5.0» marcada de fabrica, asi que la pantalla se leia
             * como algo ya resuelto en vez de como la pregunta que es. Y esta en concreto no
             * conviene contestarla por inercia: cambiar la escala mas tarde borra las notas.
             */
            val eligiendoRango = selectedScale == GradingScale.CUSTOM && !customGradeRangeConfirmed
            Revelado(visible = scaleChosen && eligiendoRango) {
                CustomGradeRangeSelector(
                    customGradeMax = customGradeMax,
                    onCustomGradeMaxChange = onCustomGradeMaxChange,
                    onConfirmClick = onConfirmCustomGradeRange
                )
            }
            Revelado(visible = scaleChosen && !eligiendoRango, retardoMs = 90) {
                Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                    if (selectedScale == GradingScale.CUSTOM) {
                        ConfirmedScaleRangeRow(
                            customGradeMax = customGradeMax,
                            onEditClick = onEditCustomGradeRange
                        )
                    }
                    ScaleZoneBar(
                        max = scaleMax,
                        passing = gradeValueOf(passingGrade),
                        target = gradeValueOf(targetAverage)
                    )
                }
            }
            Revelado(visible = scaleChosen && !eligiendoRango, retardoMs = 220) {
                GradeStepperRow(
                    label = stringResource(R.string.setup_scale_pass_label),
                    value = passingGrade,
                    max = scaleMax,
                    floorValue = 0.0,
                    ceilingValue = gradeValueOf(targetAverage) ?: scaleMax,
                    filled = false,
                    onValueChange = onPassingGradeChange
                )
            }
            Revelado(visible = scaleChosen && !eligiendoRango, retardoMs = 340) {
                GradeStepperRow(
                    label = stringResource(R.string.setup_scale_goal_label),
                    value = targetAverage,
                    max = scaleMax,
                    floorValue = gradeValueOf(passingGrade) ?: 0.0,
                    ceilingValue = scaleMax,
                    filled = true,
                    onValueChange = onTargetAverageChange
                )
            }
            Revelado(visible = scaleChosen && !eligiendoRango && !isValid) {
                Text(
                    text = stringResource(R.string.setup_scale_error),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** El título de un paso, alineado a la izquierda como en el resto de la app. */
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun SetupPlainTitle(title: String, subtitle: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMediumEmphasized
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun ScaleTypeSection(
    selectedChoice: SetupScaleChoice?,
    onChoiceSelected: (SetupScaleChoice) -> Unit
) {
    // El grupo conectado del resto de la app. Eran tres tarjetas grandes con su punto de
    // radio, su rótulo encima y una línea extra en la personalizada: mucho mueble para
    // elegir entre tres cosas que se nombran solas.
    UniSegmentedControl(
        selected = selectedChoice,
        options = listOf(
            SetupScaleChoice.FIVE to stringResource(R.string.setup_scale_range_0_5),
            SetupScaleChoice.HUNDRED to stringResource(R.string.setup_scale_range_0_100),
            SetupScaleChoice.CUSTOM to stringResource(R.string.setup_scale_range_other)
        ).map { (choice, label) -> UniSegmentedOption<SetupScaleChoice?>(value = choice, label = label) },
        // Nulo es «todavia no ha elegido», y de ahi no se puede volver tocando una opcion.
        onSelected = { valor -> valor?.let(onChoiceSelected) },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ConfirmedScaleRangeRow(
    customGradeMax: Double,
    onEditClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (LocalIsDarkTheme.current) 0.52f else 0.72f),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (LocalIsDarkTheme.current) 0.72f else 0.9f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.setup_scale_confirmed_range, customGradeMax.toInt()),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onEditClick) {
                Text(stringResource(R.string.setup_scale_btn_change), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SetupGradingCutsScreen(
    weights: List<String>,
    isValid: Boolean,
    onCountSelected: (Int) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 6
) {
    val total = weights.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val remaining = 100.0 - total
    var countExpanded by remember { mutableStateOf(false) }
    var customCountSelected by remember { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        step = SetupSteps.Periods,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            GradingCutsBottomActions(
                enabled = isValid,
                onContinueClick = onContinueClick
            )
        }
    ) {
        /*
         * El reparto manda, no los campos.
         *
         * Era una tarjeta con un desplegable de cantidad y una rejilla de campos numéricos,
         * y para saber si cuadraba el 100 % había que leer una cifra al final y fiarse. La
         * rueda lo dice sin sumar: si no está cerrada, falta. Cada peso se mueve con más y
         * menos, así que ninguno puede pasarse ni quedar vacío, y hay un atajo para repartir
         * por igual, que es lo que hace la mayoría.
         */
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SetupPlainTitle(
                title = stringResource(R.string.setup_cuts_title),
                subtitle = stringResource(R.string.setup_cuts_desc, Corte.Plural.lowercase())
            )

            /*
             * Primero cuantos, y lo demas va saliendo debajo.
             *
             * La cantidad estaba debajo de la rueda, asi que elegir un numero hacia aparecer de
             * golpe todo el bloque —rueda, balance y atajo— y ademas encima, empujando hacia
             * abajo el propio boton que acababas de tocar. Con el selector arriba, cada pieza
             * entra escalonada justo bajo la mano.
             */
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                CutCountSection(
                    count = weights.size,
                    onCountSelected = { count ->
                        customCountSelected = false
                        countExpanded = false
                        onCountSelected(count)
                    }
                )
                Revelado(visible = weights.isNotEmpty(), retardoMs = 90) {
                    CutWheelCard(
                        weights = weights,
                        total = total,
                        isValid = isValid,
                        onWeightChange = onWeightChange
                    )
                }
                Revelado(visible = weights.isNotEmpty(), retardoMs = 230) {
                    CutBalanceNotice(total = total, remaining = remaining, isValid = isValid)
                }
                Revelado(visible = weights.size > 1, retardoMs = 360) {
                    SetupEvenSplitAction(
                        count = weights.size,
                        onSplit = { even ->
                            weights.indices.forEach { index -> onWeightChange(index, even[index]) }
                        }
                    )
                }
            }
            }
        }
    }

@Composable
private fun GradingCutsBottomActions(
    enabled: Boolean,
    onContinueClick: () -> Unit
) {
    UniStackButton(
        text = stringResource(R.string.setup_btn_continue),
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = if (enabled) stringResource(R.string.setup_cuts_complete) else stringResource(R.string.setup_cuts_incomplete),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            lineHeight = 12.sp
        )
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
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.setup_scale_custom_max_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    )
                    Text(
                        stringResource(R.string.setup_scale_custom_max_desc),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                UniCard(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    tonalElevation = 4.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        customGradeMax.toInt().toString(),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Text(
                stringResource(R.string.setup_scale_custom_current, customGradeMax.toInt()),
                color = MaterialTheme.colorScheme.onSurface,
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
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    activeTickColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.35f),
                    inactiveTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("1", "25", "50", "75", "100").forEach { mark ->
                    Text(mark, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Normal)
                }
            }
            UniStackButton(text = stringResource(R.string.setup_scale_btn_confirm_range), onClick = onConfirmClick)
        }
    }
}

@Composable
fun SetupModulesScreen(
    totalSteps: Int = 6,
    selectedModules: Set<AppModule>,
    onToggleModule: (AppModule) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        step = SetupSteps.Modules,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = stringResource(R.string.setup_btn_continue),
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
            AnimatedVisibility(
                visible = AppModule.GRADES !in selectedModules,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = stringResource(R.string.setup_modules_skip_warning),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                )
            }
            SetupModulesInfoCard()
        }
    }
}

/**
 * Paso final: celebración, repaso de lo configurado y salida a la app.
 *
 * Antes eran dos pantallas seguidas —«Resumen» y «Listo»— que mostraban exactamente los
 * mismos datos y se confirmaban uno detrás de otro. Se fusionan en una: el repaso sigue
 * estando, pero se confirma una sola vez.
 *
 * Los bloques de escala y evaluación se ocultan si el usuario desactivó el módulo de notas,
 * porque en ese caso esos pasos ni siquiera se le preguntaron.
 */
@Composable
fun SetupDoneScreen(
    name: String,
    /** Avisa al flujo de que empieza el cierre, para que retire el encabezado a la vez. */
    onExitingChange: (Boolean) -> Unit = {},
    studyArea: StudyArea?,
    selectedProgram: String?,
    customProgram: String,
    academicInfo: String,
    gradingScale: GradingScale,
    customGradeMax: Double,
    passingGrade: String,
    targetAverage: String,
    cutWeights: List<String>,
    enabledModules: Set<AppModule>,
    onBackClick: () -> Unit,
    onCreateSubjectClick: () -> Unit,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradesEnabled: Boolean = true,
    permissionsNeeded: Boolean = true,
    totalSteps: Int = 7,
    institutionName: String = ""
) {
    BackHandler(onBack = onBackClick)
    val displayName = name.ifBlank { stringResource(R.string.setup_default_user) }
    val program = resolvedProgram(selectedProgram, customProgram)
    val weights = cutWeights.filter { it.isNotBlank() }

    // La celebración es la misma se pulse el botón que se pulse: lo que se celebra es haber
    // terminado, no a dónde se va. Se recuerda cuál se pulsó y la ruta se resuelve al final.
    var exiting by remember { mutableStateOf(false) }
    var createSubjectOnExit by remember { mutableStateOf(false) }
    val startExit: (Boolean) -> Unit = { createSubject ->
        if (!exiting) {
            createSubjectOnExit = createSubject
            exiting = true
            onExitingChange(true)
        }
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (exiting) 0f else 1f,
        animationSpec = tween(durationMillis = 320),
        label = "done-content-alpha"
    )
    val contentShift by animateFloatAsState(
        targetValue = if (exiting) 24f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "done-content-shift"
    )
    val actionsAlpha by animateFloatAsState(
        targetValue = if (exiting) 0f else 1f,
        animationSpec = tween(durationMillis = 280),
        label = "done-actions-alpha"
    )
    val actionsShift by animateFloatAsState(
        targetValue = if (exiting) 54f else 0f,
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "done-actions-shift"
    )

    Box(modifier = modifier.fillMaxSize()) {
    SetupScaffold(
        step = SetupSteps.done(gradesEnabled, permissionsNeeded),
        totalSteps = totalSteps,
        actions = {
            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = actionsAlpha
                    translationY = actionsShift.dp.toPx()
                },
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (gradesEnabled) {
                    UniStackButton(
                        text = stringResource(R.string.setup_done_btn_first_subject),
                        onClick = { startExit(true) },
                        leadingIcon = Icons.Rounded.Add,
                        trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
                    )
                    UniStackButton(
                        text = stringResource(R.string.setup_done_btn_home),
                        onClick = { startExit(false) },
                        variant = UniStackButtonVariant.Outlined,
                        leadingIcon = Icons.Rounded.Home
                    )
                } else {
                    UniStackButton(
                        text = stringResource(R.string.setup_done_btn_home),
                        onClick = { startExit(false) },
                        leadingIcon = Icons.Rounded.Home,
                        trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
                    )
                }
            }
        }
    ) {
        /*
         * Tres cosas y ninguna más.
         *
         * Aquí había una celebración, una tarjeta con las seis cosas que el usuario acababa
         * de escribir y un aviso debajo. Ninguna de las tres se ganaba el sitio: el resumen
         * le devuelve lo que ya sabe, y el aviso explica algo que ya no puede cambiar sin
         * volver atrás. Después de siete pasos de formulario, lo mejor que puede hacer esta
         * pantalla es apartarse: el sello, el nombre y la puerta.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                // Centrado del todo quedaba alto: el bloque de acciones ocupa el pie, así
                // que el centro real del hueco cae por encima del centro que se percibe.
                .padding(top = 72.dp)
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentShift.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SetupFinishHero(name = displayName)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = if (gradesEnabled) {
                    stringResource(R.string.setup_done_footer_with_subjects)
                } else {
                    stringResource(R.string.setup_done_footer_generic)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }

        if (exiting) {
            SetupFinishTransition(
                onFinished = {
                    if (createSubjectOnExit) onCreateSubjectClick() else onGoHomeClick()
                }
            )
        }
    }
}

@Composable
private fun SetupModulesHero() {
    val float = floatingOffset(travel = 4f, durationMillis = 2900, label = "modules-hero-float")
    val breath = floatingOffset(travel = 0.05f, durationMillis = 3700, label = "modules-hero-breath")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = 1f + breath
                    scaleY = 1f + breath
                }
                .size(66.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.34f else 0.16f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = float.dp.toPx() }
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (LocalIsDarkTheme.current) 0.74f else 1f))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.58f else 0.26f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.GridView,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
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
            text = stringResource(R.string.setup_modules_title),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 23.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.setup_modules_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val activeDesc = stringResource(R.string.setup_modules_status_active)
    val inactiveDesc = stringResource(R.string.setup_modules_status_inactive)
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .alpha(if (selected) 1f else 0.58f)
            .expressiveSelection(selected)
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onValueChange = { onClick() }
            )
            .semantics {
                stateDescription = if (selected) activeDesc else inactiveDesc
            },
        // Un módulo apagado se atenúa entero en vez de cambiar solo de contorno: así la
        // lista se lee de un vistazo, sin comparar bordes fila por fila.
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = rememberSelectionShape(selected),
        tonalElevation = 0.dp,
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = option.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // El interruptor funciona.
            //
            // Estaba puesto a `enabled = false` para que solo se dibujara y dejara pasar el
            // toque a la tarjeta, pero un control desactivado se come el puntero igual: por
            // encima del interruptor no pasaba nada, y solo colaba algún toque en el borde.
            // Ahora responde él, y la tarjeta sigue respondiendo por su cuenta.
            UniSwitch(
                checked = selected,
                onCheckedChange = { onClick() }
            )
        }
    }
}

@Composable
private fun SetupModulesInfoCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.78f),
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
                    append(stringResource(R.string.setup_modules_footer_lead))
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)) {
                        append(stringResource(R.string.setup_modules_footer_accent))
                    }
                    append(stringResource(R.string.setup_modules_footer_tail))
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** El sello del cierre, del mismo ancho que el de la animación que viene detrás. */
private val FinishMarkWidth = 108.dp

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun SetupFinishHero(name: String) {
    // El sello entra con rebote: es el único momento del onboarding que celebra algo, y
    // aparecer ya colocado lo hacía indistinguible de una cabecera cualquiera.
    val motionEnabled = LocalMotionDurationScale.current > 0f
    var appeared by remember { mutableStateOf(!motionEnabled) }
    LaunchedEffect(Unit) { appeared = true }
    val badgeScale by animateFloatAsState(
        targetValue = if (appeared) 1f else 0.55f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "finish-badge-appear"
    )
    val badgeFloat = floatingOffset(travel = 4f, durationMillis = 3200, label = "finish-badge-float")

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .graphicsLayer {
                    scaleX = badgeScale
                    scaleY = badgeScale
                    translationY = badgeFloat.dp.toPx()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.30f else 0.18f),
                                Color.Transparent
                            )
                        )
                    )
            )
            /*
             * La marca de verdad, con sus tres píldoras y sus degradados.
             *
             * Aquí estuvo la silueta blanca del símbolo sobre un cuadrado del acento, y una
             * silueta no es la marca: los colores son justo lo que la identifica. Es además
             * la misma que arranca la app y la misma que se despide un segundo después en
             * la transición de salida, así que las tres puntas del onboarding llevan el
             * mismo sello.
             */
            Box(
                modifier = Modifier.size(
                    width = FinishMarkWidth,
                    height = FinishMarkWidth * UniStackBrandMark.HeightRatio
                )
            ) {
                UniStackBrandMark.Pills.forEach { pill ->
                    UniStackBrandPill(pill = pill, markWidth = FinishMarkWidth)
                }
            }
        }
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.setup_done_lead))
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("$name.")
                }
            },
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMediumEmphasized,
            textAlign = TextAlign.Center
        )
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
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
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
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "i",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = (size.value * 0.48f).sp,
            lineHeight = (size.value * 0.52f).sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun setupModuleOptions(): List<ModuleOption> = allModuleOptions()
    .filter { it.module in offerableModules() }

@Composable
private fun allModuleOptions(): List<ModuleOption> = listOf(
    ModuleOption(
        module = AppModule.GRADES,
        label = stringResource(R.string.settings_module_grades_title),
        description = stringResource(R.string.settings_module_grades_desc),
        icon = Icons.AutoMirrored.Rounded.MenuBook
    ),
    ModuleOption(
        module = AppModule.TASKS,
        label = stringResource(R.string.settings_module_tasks_title),
        description = stringResource(R.string.settings_module_tasks_desc),
        icon = Icons.Rounded.CheckCircle
    ),
    ModuleOption(
        module = AppModule.EXPENSES,
        label = stringResource(R.string.settings_module_expenses_title),
        description = stringResource(R.string.settings_module_expenses_desc),
        icon = Icons.Rounded.AccountBalanceWallet
    ),
    ModuleOption(
        module = AppModule.ACADEMIC_TEMPLATES,
        label = stringResource(R.string.settings_module_templates_title),
        description = stringResource(R.string.setup_module_templates_desc),
        icon = Icons.AutoMirrored.Rounded.Assignment
    )
)

@Composable
internal fun SetupScaffold(
    modifier: Modifier = Modifier,
    /**
     * Solo para separar el contenido: quien dibuja el encabezado es [SetupFlow].
     *
     * Vivia aqui, y por eso la transicion entre pasos se lo llevaba de un lado a otro. Sigue
     * haciendo falta el numero para saber si arriba hay encabezado o no, que es lo que decide
     * cuanto respira el contenido.
     */
    step: Int? = null,
    totalSteps: Int = 6,
    welcome: Boolean = false,
    overlayKeyboard: Boolean = false,
    actions: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // El teclado se esquiva aquí y no en la raíz de la app: así este paso puede optar por
    // que se superponga. Se excluye el inset de la barra de navegación porque el del
    // teclado ya lo incluye y las barras de acciones aplican navigationBarsPadding().
    val imeInsets = WindowInsets.ime.exclude(WindowInsets.navigationBars)
    val density = LocalDensity.current
    // Alto real de la barra de acciones flotante, para reservar sitio al final del scroll.
    var floatingActionsHeight by remember { mutableStateOf(0.dp) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .then(if (overlayKeyboard) Modifier else Modifier.windowInsetsPadding(imeInsets)),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0.dp),
        bottomBar = {
            // Con superposición la barra no va aquí: Scaffold le resta su alto al contenido,
            // así que subirla por el teclado encogería la pantalla igual que antes. Pasa a
            // flotar sobre el contenido, más abajo.
            if (actions != null && !overlayKeyboard) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
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
        // Sin esto no había manera de cerrar el teclado: tocar fuera del campo no quitaba el
        // foco y el único recurso era el botón atrás del sistema. Los hijos se comprueban
        // antes, así que tocar un campo o un botón sigue funcionando igual.
        val focusManager = LocalFocusManager.current
        // Al superponerse, el final del contenido queda debajo del teclado y de la barra
        // flotante. Se reserva ese alto para poder desplazarse hasta él; sin esto, en una
        // pantalla corta el campo quedaría tapado sin manera de sacarlo.
        val overlayBottomRoom = if (overlayKeyboard) {
            floatingActionsHeight + with(density) { WindowInsets.ime.getBottom(density).toDp() }
        } else {
            0.dp
        }

        Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 22.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .then(if (actions == null) Modifier.navigationBarsPadding() else Modifier)
                .verticalScroll(scrollState)
                .padding(
                    top = if (step == null) 10.dp else 7.dp,
                    bottom = 14.dp + overlayBottomRoom
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (welcome) {
                            Modifier
                                .clip(MaterialTheme.shapes.extraLarge)
                                .background(
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = MaterialTheme.shapes.extraLarge
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

            if (actions != null && overlayKeyboard) {
                // La barra se queda anclada abajo y el teclado también la tapa: subirla con
                // el IME la traía encima del contenido, que es lo que se quería evitar. Para
                // pulsarla se cierra antes el teclado tocando fuera del campo.
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onSizeChanged {
                                floatingActionsHeight = with(density) { it.height.toDp() }
                            }
                            .background(MaterialTheme.colorScheme.background)
                            .navigationBarsPadding()
                            .padding(horizontal = 22.dp)
                            .padding(top = 6.dp, bottom = 7.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        content = actions
                    )
                }
            }
        }
    }
}

/**
 * El encabezado del onboarding, por encima de las pantallas y no dentro de ellas.
 *
 * Vivia dentro de cada paso, asi que la transicion de navegacion se lo llevaba: al continuar,
 * una barra salia por un lado y entraba otra por el otro. Eso es lo que hacia que nueve pasos
 * se sintieran como pasar diapositivas. Aqui no se mueve nunca; lo unico que cambia es cuanto
 * mide el relleno, que es exactamente lo que significa avanzar.
 */
@Composable
private fun SetupProgressHeader(
    step: Int?,
    totalSteps: Int,
    onBackClick: () -> Unit,
    chromeAlpha: Float
) {
    // Al retirarse conserva el ultimo paso: sin esto la barra se vaciaria mientras se va.
    var ultimoPaso by remember { mutableStateOf(1) }
    LaunchedEffect(step) { if (step != null) ultimoPaso = step }

    AnimatedVisibility(
        visible = step != null,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ),
        exit = fadeOut(animationSpec = tween(160)) + shrinkVertically(animationSpec = tween(200))
    ) {
        SetupTopBar(
            onBackClick = onBackClick,
            step = ultimoPaso,
            totalSteps = totalSteps,
            modifier = Modifier
                .graphicsLayer { alpha = chromeAlpha }
                .statusBarsPadding()
                .padding(horizontal = 22.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SetupTopBar(
    onBackClick: (() -> Unit)?,
    step: Int?,
    totalSteps: Int,
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
                        contentDescription = stringResource(R.string.setup_btn_back),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (step != null) {
                /*
                 * Quieto, y solo cambia la cifra.
                 *
                 * Se probo animandolo en la direccion de la marcha y quedaba peor: el rotulo
                 * parecia reaparecer en cada paso, que es justo la sensacion que se estaba
                 * intentando quitar. Quien tiene que dar el movimiento es la barra.
                 */
                Text(
                    text = stringResource(R.string.setup_step_counter, step, totalSteps),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
        if (step != null) {
            /*
             * Una barra que se llena, no una fila de pastillas que se encienden.
             *
             * Con una pastilla por paso, avanzar solo pintaba una mas: eso es el punto de una
             * diapositiva, no una sensacion de avance. Una sola barra crece del ancho anterior
             * al nuevo, asi que el movimiento se ve, y al cambiar de paso pega un golpe de alto
             * para que no pase desapercibido.
             *
             * El alto se anima dentro de un contenedor de alto fijo. Animarlo por fuera movia
             * la barra de acciones y el contenido entero de la pantalla a cada paso.
             */
            val avance = step.coerceIn(0, totalSteps).toFloat() / totalSteps.coerceAtLeast(1)
            val relleno by animateFloatAsState(
                targetValue = avance,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "setup-progress"
            )
            val grosor = remember { Animatable(6f) }
            LaunchedEffect(step) {
                grosor.animateTo(10f, tween(140, easing = FastOutSlowInEasing))
                grosor.animateTo(
                    6f,
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 54.dp)
                    .height(11.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(grosor.value.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.outlineVariant
                                .copy(alpha = if (LocalIsDarkTheme.current) 0.5f else 0.72f)
                        )
                )
                Box(
                    modifier = Modifier
                        // Nunca del todo vacia: en el primer paso ya has hecho algo.
                        .fillMaxWidth(relleno.coerceIn(0.05f, 1f))
                        .height(grosor.value.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                )
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

private enum class SetupScaleChoice {
    FIVE,
    HUNDRED,
    CUSTOM
}

/*
 * En primaria y secundaria esto devolvia el curso en lugar de la carrera. Sin esos niveles
 * queda una sola linea: la carrera elegida, o la escrita a mano cuando no estaba en el
 * catalogo.
 */
private fun resolvedProgram(selectedProgram: String?, customProgram: String): String =
    if (isOtherOption(selectedProgram)) customProgram else selectedProgram.orEmpty()

@Composable
internal fun SetupDropdownField(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean,
    expanded: Boolean,
    leadingIcon: ImageVector,
    optionIcon: (String) -> ImageVector,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIconContainerColor: Color? = null,
    leadingIconContentColor: Color? = null
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    val rotation by animateFloatAsState(if (expanded) 180f else 0f, label = "setup-dropdown-arrow")
    var anchorWidth by remember { mutableIntStateOf(0) }
    val shape = MaterialTheme.shapes.medium
    val displayValue = value.ifBlank { stringResource(R.string.setup_btn_select) }
    val isPlaceholder = value.isBlank()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = shape,
                tonalElevation = 0.dp,
                borderColor = when {
                    expanded -> MaterialTheme.colorScheme.primary
                    enabled -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (LocalIsDarkTheme.current) 0.82f else 0.92f)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f)
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
                        modifier = Modifier.size(46.dp),
                        containerColor = leadingIconContainerColor,
                        contentColor = leadingIconContentColor
                    )
                    Text(
                        text = displayValue,
                        color = when {
                            !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                            isPlaceholder -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurface
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
                        tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(25.dp)
                            .rotate(rotation + 90f)
                    )
                }
            }

            MaterialTheme(
                shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.medium),
                colorScheme = MaterialTheme.colorScheme.copy(surface = MaterialTheme.colorScheme.surfaceContainerLow)
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
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            MaterialTheme.shapes.medium
                        )
                ) {
                    options.forEach { option ->
                        val isSelected = option == value
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
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
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (LocalIsDarkTheme.current) 0.58f else 0.9f) else Color.Transparent,
                                    MaterialTheme.shapes.small
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
internal fun DropdownIconBox(
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    iconSize: androidx.compose.ui.unit.Dp = 25.dp,
    // Nulos preservan el aspecto de siempre. El perfil los usa para pedir una caja neutra:
    // ahí ya compiten el avatar y el camino del progreso por el mismo morado, y otra caja más
    // del mismo color no distingue nada, solo repite.
    containerColor: Color? = null,
    contentColor: Color? = null
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                containerColor ?: MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = when {
                        !enabled -> 0.42f
                        LocalIsDarkTheme.current -> 0.9f
                        else -> 1f
                    }
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor ?: if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(iconSize)
        )
    }
}
