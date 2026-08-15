package com.unistack.app.core.navigation

import com.unistack.app.core.design.theme.AppShapes

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.unistack.app.core.design.components.squishOnPress
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unistack.app.core.di.rememberUniStackEntryPoint
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.feature_expenses.presentation.AddExpenseScreen
import com.unistack.app.feature_expenses.presentation.ExpensesScreen
import com.unistack.app.feature_grades.presentation.AddGradeScreen
import com.unistack.app.feature_grades.presentation.AcademicScreen
import com.unistack.app.feature_grades.presentation.GradesScreen
import com.unistack.app.feature_grades.presentation.PriorHistoryScreen
import com.unistack.app.feature_grades.presentation.SubjectDetailScreen
import com.unistack.app.feature_grades.presentation.SubjectFormMode
import com.unistack.app.feature_grades.presentation.SubjectFormScreen
import com.unistack.app.feature_grades.presentation.SubjectPeriodDetailScreen
import com.unistack.app.feature_home.presentation.HomeScreen
import com.unistack.app.feature_home.presentation.HomeViewModel
import com.unistack.app.feature_notifications.presentation.NotificationDetailScreen
import com.unistack.app.feature_notifications.presentation.NotificationHistoryScreen
import com.unistack.app.feature_profile.presentation.ProfileScreen
import com.unistack.app.feature_profile.presentation.ProfileScreenMode
import com.unistack.app.feature_profile.presentation.ProScreen
import com.unistack.app.feature_profile.presentation.AppearanceSettingsScreen
import com.unistack.app.feature_profile.presentation.AccessibilitySettingsScreen
import com.unistack.app.feature_profile.presentation.SettingsHubScreen
import com.unistack.app.feature_schedule.presentation.CalendarScheduleScreen
import com.unistack.app.feature_support.presentation.AboutScreen
import com.unistack.app.feature_support.presentation.AiAssistantScreen
import com.unistack.app.feature_support.presentation.GpaCalculatorScreen
import com.unistack.app.feature_support.presentation.LabsScreen
import com.unistack.app.feature_support.presentation.QuickNotesScreen
import com.unistack.app.feature_support.presentation.HelpScreen
import com.unistack.app.feature_support.presentation.ResourcesScreen
import com.unistack.app.feature_support.presentation.WhatsNewScreen
import com.unistack.app.feature_tasks.presentation.AddTaskScreen
import com.unistack.app.feature_tasks.presentation.TasksScreen
import com.unistack.app.feature_templates.presentation.AcademicTemplatesScreen
import com.unistack.app.feature_updates.domain.UpdateState
import com.unistack.app.feature_updates.presentation.UpdateAvailableBanner
import com.unistack.app.feature_updates.presentation.UpdateDetailSheet
import com.unistack.app.feature_updates.presentation.UpdateSettingsScreen
import com.unistack.app.feature_updates.presentation.UpdateViewModel
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.BottomBarStyle
import com.unistack.app.feature_user.domain.InitialTab
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

private const val MAIN_TRANSITION_MILLIS = 220
private const val MAIN_EXIT_MILLIS = 150
private val DefaultEnabledModules = setOf(AppModule.GRADES, AppModule.TASKS)

@Composable
fun MainNavGraph(
    modifier: Modifier = Modifier,
    initialRoute: String = AppRoutes.Home,
    launchRoute: String? = null,
    onLaunchRouteConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val motionScale = LocalMotionDurationScale.current
    val appearance = LocalAppearancePreferences.current
    val userRepository = rememberUniStackEntryPoint().userRepository()
    val enabledModules by remember {
        userRepository.userProfile
            .map { it?.enabledModules ?: DefaultEnabledModules }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(
        initialValue = userRepository.userProfile.value?.enabledModules ?: DefaultEnabledModules
    )
    val bottomItems = remember(enabledModules) { BottomNavItem.itemsFor(enabledModules) }
    val resolvedInitialRoute = remember(initialRoute, appearance.initialTab, enabledModules) {
        if (initialRoute != AppRoutes.Home) {
            initialRoute
        } else {
            // Con la pestaña en la ruta: «Académico» a secas abre la primera, que es Materias,
            // así que elegir Tareas como pantalla de arranque abría Materias.
            when (appearance.initialTab) {
                InitialTab.HOME -> AppRoutes.Home
                InitialTab.GRADES -> if (AppModule.GRADES in enabledModules) {
                    AppRoutes.academic(AppRoutes.AcademicTabSubjects)
                } else {
                    AppRoutes.Home
                }
                InitialTab.TASKS -> if (AppModule.TASKS in enabledModules) {
                    AppRoutes.academic(AppRoutes.AcademicTabTasks)
                } else {
                    AppRoutes.Home
                }
                InitialTab.EXPENSES -> if (AppModule.EXPENSES in enabledModules) AppRoutes.Expenses else AppRoutes.Home
            }
        }
    }
    val currentRoute = navController.currentRouteAsState() ?: resolvedInitialRoute
    var homeDrawerOpen by remember { mutableStateOf(false) }
    LaunchedEffect(currentRoute) {
        if (currentRoute != AppRoutes.Home) homeDrawerOpen = false
    }
    val showBottomBar = !homeDrawerOpen && routeShowsBottomBar(currentRoute)

    LaunchedEffect(launchRoute, enabledModules) {
        launchRoute?.let { route ->
            navController.navigateIfModuleEnabled(route, enabledModules)
            onLaunchRouteConsumed()
        }
    }

    ModuleAccessGuard(
        navController = navController,
        enabledModules = enabledModules
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UniStackColors.Background,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            if (showBottomBar) {
                UniStackBottomBar(
                    navController = navController,
                    items = bottomItems
                )
            }
        }
    ) { innerPadding ->
        val contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
            top = innerPadding.calculateTopPadding(),
            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
            bottom = innerPadding.calculateBottomPadding()
        )
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = resolvedInitialRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                /*
                 * Un fundido, y punto.
                 *
                 * Se probaron dos versiones con deslizamiento —un tercio de pantalla, y luego
                 * el ancho completo con la anterior apartándose— y las dos se sentían ajenas a
                 * la app. El fundido no compite con nada de lo que hay en pantalla y deja que
                 * lo que se mueva sea el contenido, no el marco.
                 */
                enterTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route
                    screenEnter(
                        style = appearance.screenTransition,
                        motionScale = motionScale,
                        fromRight = isForwardNavigation(from, to),
                        lateral = isLateralNavigation(from, to)
                    )
                },
                exitTransition = {
                    val from = initialState.destination.route
                    val to = targetState.destination.route
                    screenExit(
                        style = appearance.screenTransition,
                        motionScale = motionScale,
                        toLeft = isForwardNavigation(from, to),
                        lateral = isLateralNavigation(from, to)
                    )
                },
                popEnterTransition = {
                    screenEnter(appearance.screenTransition, motionScale, fromRight = false)
                },
                popExitTransition = {
                    screenExit(appearance.screenTransition, motionScale, toLeft = false)
                }
            ) {
                composable(AppRoutes.Home) {
                    val viewModel: HomeViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    val updateViewModel: UpdateViewModel = hiltViewModel()
                    val updateState by updateViewModel.state.collectAsStateWithLifecycle()
                    val updateInfo = when (val current = updateState) {
                        is UpdateState.Available -> current.info
                        is UpdateState.Downloading -> current.info
                        is UpdateState.ReadyToInstall -> current.info
                        else -> null
                    }

                    val onNavigateToUpdates = {
                        navController.navigate(AppRoutes.Settings) {
                            launchSingleTop = true
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        if (updateInfo != null && updateState is UpdateState.Available) {
                            UpdateAvailableBanner(
                                versionName = updateInfo.versionName,
                                onTap = onNavigateToUpdates
                            )
                        }

                        HomeScreen(
                            uiState = uiState,
                            onAddSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubject, enabledModules) },
                            onSeeAllSubjectsClick = { navController.navigateIfModuleEnabled(AppRoutes.academic(AppRoutes.AcademicTabSubjects), enabledModules) },
                            onSeeTasksClick = { navController.navigateIfModuleEnabled(AppRoutes.academic(AppRoutes.AcademicTabTasks), enabledModules) },
                            onSeeExpensesClick = { navController.navigateIfModuleEnabled(AppRoutes.Expenses, enabledModules) },
                            onOpenTemplatesClick = { navController.navigateIfModuleEnabled(AppRoutes.AcademicTemplates, enabledModules) },
                            onCalendarClick = { navController.go(AppRoutes.Calendar) },
                            onSubjectClick = { subjectId -> navController.navigateIfModuleEnabled(AppRoutes.subjectDetail(subjectId), enabledModules) },
                            onNotificationsClick = {
                                navController.navigate(AppRoutes.Notifications) {
                                    launchSingleTop = true
                                }
                            },
                            onSettingsClick = {
                                navController.navigate(AppRoutes.Settings) {
                                    launchSingleTop = true
                                }
                            },
                            onDataClick = {
                                navController.navigate(AppRoutes.DataSettings) {
                                    launchSingleTop = true
                                }
                            },
                            onWhatsNewClick = { navController.go(AppRoutes.WhatsNew) },
                            onResourcesClick = { navController.go(AppRoutes.Resources) },
                            onHelpClick = { navController.go(AppRoutes.Help) },
                            onAboutClick = { navController.go(AppRoutes.About) },
                            onGpaClick = { navController.go(AppRoutes.GpaCalculator) },
                            onQuickNotesClick = { navController.go(AppRoutes.QuickNotes) },
                            onAiClick = { navController.go(AppRoutes.AiAssistant) },
                            onLabsClick = { navController.go(AppRoutes.Labs) },
                            onProfileClick = {
                                navController.navigate(AppRoutes.Profile) {
                                    launchSingleTop = true
                                }
                            },
                            onAddGradeClick = { subjectId ->
                                navController.navigateIfModuleEnabled(AppRoutes.addGrade(subjectId), enabledModules)
                            },
                            onAddTaskClick = { navController.navigateIfModuleEnabled(AppRoutes.AddTask, enabledModules) },
                            onAddExpenseClick = { navController.navigateIfModuleEnabled(AppRoutes.AddExpense, enabledModules) },
                            onDrawerOpenChange = { open -> homeDrawerOpen = open },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (updateInfo != null) {
                        UpdateDetailSheet(
                            info = updateInfo,
                            state = updateState,
                            onDownloadClick = updateViewModel::downloadUpdate,
                            onInstallClick = updateViewModel::installUpdate,
                            onDismiss = updateViewModel::dismiss
                        )
                    }
                }
            composable(AppRoutes.Notifications) {
                NotificationHistoryScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Home)
                        }
                    },
                    onNotificationClick = { notificationId ->
                        navController.go(AppRoutes.notificationDetail(notificationId))
                    },
                    onSettingsClick = {
                        navController.navigate(AppRoutes.NotificationSettings) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("${AppRoutes.NotificationDetail}/{notificationId}") { backStackEntry ->
                val notificationId = backStackEntry.arguments
                    ?.getString("notificationId")
                    ?.toIntOrNull()
                    ?: -1
                NotificationDetailScreen(
                    notificationId = notificationId,
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Notifications)
                        }
                    },
                    onOpenRelated = { route ->
                        navController.navigateIfModuleEnabled(route, enabledModules)
                    }
                )
            }
            // Materias y Tareas ya no son pantallas propias: viven como pestañas de
            // Académico, que es donde llega la barra inferior. Aquí solo quedan como
            // redirección porque hay recordatorios ya programados con la cadena "tasks"
            // guardada dentro; borrarlas dejaría esas notificaciones apuntando a la nada.
            composable(AppRoutes.Grades) {
                RedirectToAcademic(navController, AppRoutes.AcademicTabSubjects)
            }
            composable(AppRoutes.Tasks) {
                RedirectToAcademic(navController, AppRoutes.AcademicTabTasks)
            }
            composable(AppRoutes.Profile) {
                ProfileScreen(
                    onOpenProClick = { navController.go(AppRoutes.Pro) },
                    onOpenSettingsClick = { navController.go(AppRoutes.Settings) },
                    onOpenAcademicClick = { navController.go(AppRoutes.AcademicSettings) },
                    onOpenNotificationsClick = { navController.go(AppRoutes.NotificationSettings) },
                    onOpenModulesClick = { navController.go(AppRoutes.ModuleSettings) },
                    onOpenAppearanceClick = { navController.go(AppRoutes.AppearanceSettings) },
                    onOpenDataClick = { navController.go(AppRoutes.DataSettings) },
                    onOpenUpdatesClick = { navController.go(AppRoutes.UpdateSettings) }
                )
            }
            composable(
                route = AppRoutes.AcademicWithTab,
                arguments = listOf(
                    navArgument(AppRoutes.AcademicTabArg) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                AcademicScreen(
                    initialTab = entry.arguments?.getString(AppRoutes.AcademicTabArg),
                    onAddSubjectClick = {
                        navController.navigateIfModuleEnabled(AppRoutes.AddSubject, enabledModules)
                    },
                    onSubjectClick = { subjectId ->
                        navController.navigateIfModuleEnabled(AppRoutes.subjectDetail(subjectId), enabledModules)
                    },
                    onNewTaskClick = {
                        navController.navigateIfModuleEnabled(AppRoutes.AddTask, enabledModules)
                    },
                    onEditTaskClick = { taskId ->
                        navController.navigateIfModuleEnabled(AppRoutes.editTask(taskId), enabledModules)
                    },
                    onCompleteHistoryClick = { subjectId ->
                        navController.navigateIfModuleEnabled(AppRoutes.priorHistory(subjectId), enabledModules)
                    }
                )
            }
            composable(AppRoutes.Settings) {
                SettingsHubScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Profile)
                        }
                    },
                    onAppearanceClick = { navController.go(AppRoutes.AppearanceSettings) },
                    onAccessibilityClick = { navController.go(AppRoutes.AccessibilitySettings) },
                    onProfileClick = {
                        navController.navigate(AppRoutes.Profile) {
                            launchSingleTop = true
                        }
                    },
                    onAcademicClick = { navController.go(AppRoutes.AcademicSettings) },
                    onModulesClick = { navController.go(AppRoutes.ModuleSettings) },
                    onNotificationsClick = { navController.go(AppRoutes.NotificationSettings) },
                    onDataClick = { navController.go(AppRoutes.DataSettings) },
                    onUpdatesClick = { navController.go(AppRoutes.UpdateSettings) }
                )
            }
            composable(AppRoutes.UpdateSettings) {
                UpdateSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            composable(AppRoutes.AppearanceSettings) {
                AppearanceSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            composable(AppRoutes.AccessibilitySettings) {
                AccessibilitySettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            composable(AppRoutes.Calendar) {
                CalendarScheduleScreen(
                    onTaskClick = { taskId ->
                        navController.navigateIfModuleEnabled(AppRoutes.editTask(taskId), enabledModules)
                    },
                    // Sin navigateIfModuleEnabled: crear una clase no depende del módulo de
                    // notas. Horario es pestaña fija aunque Académico esté apagado, y con la
                    // comprobación puesta el botón «Añadir clase» llevaría a Inicio.
                    onAddClassClick = {
                        navController.navigate(AppRoutes.AddSubjectFromSchedule) { launchSingleTop = true }
                    },
                    onEditSubjectClick = { subjectId ->
                        navController.navigate(AppRoutes.editSubjectFromSchedule(subjectId)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(AppRoutes.AcademicSettings) {
                ProfileScreen(
                    mode = ProfileScreenMode.ACADEMIC,
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            composable(AppRoutes.ModuleSettings) {
                ProfileScreen(
                    mode = ProfileScreenMode.MODULES,
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            composable(AppRoutes.NotificationSettings) {
                ProfileScreen(
                    mode = ProfileScreenMode.NOTIFICATIONS,
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            composable(AppRoutes.DataSettings) {
                ProfileScreen(
                    mode = ProfileScreenMode.DATA,
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            composable(AppRoutes.WhatsNew) {
                WhatsNewScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            composable(AppRoutes.Resources) {
                ResourcesScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            composable(AppRoutes.Help) {
                HelpScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            composable(AppRoutes.About) {
                AboutScreen(
                    onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) },
                    onWhatsNewClick = { navController.go(AppRoutes.WhatsNew) },
                    onUpdatesClick = { navController.go(AppRoutes.UpdateSettings) }
                )
            }
            composable(AppRoutes.GpaCalculator) {
                GpaCalculatorScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            composable(AppRoutes.QuickNotes) {
                QuickNotesScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            composable(AppRoutes.AiAssistant) {
                AiAssistantScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            composable(AppRoutes.Labs) {
                LabsScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            composable(AppRoutes.Pro) {
                ProScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Profile)
                        }
                    }
                )
            }
            composable(AppRoutes.Expenses) {
                ExpensesScreen(
                    onAddExpenseClick = { navController.navigateIfModuleEnabled(AppRoutes.AddExpense, enabledModules) },
                    onEditExpenseClick = { expenseId -> navController.navigateIfModuleEnabled(AppRoutes.editExpense(expenseId), enabledModules) }
                )
            }
            composable(AppRoutes.AddSubject) {
                SubjectFormScreen(
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.academic(AppRoutes.AcademicTabSubjects), enabledModules)
                    },
                    onSubjectSaved = { subjectId ->
                        navController.navigate(AppRoutes.subjectDetail(subjectId)) {
                            popUpTo(AppRoutes.AddSubject) {
                                inclusive = true
                            }
                        }
                    },
                    onUpgradeClick = { navController.go(AppRoutes.Pro) }
                )
            }
            // Las dos rutas de Horario: el mismo formulario, con el bloque académico plegado
            // y volviendo al calendario en vez de al detalle de la materia.
            composable(AppRoutes.AddSubjectFromSchedule) {
                SubjectFormScreen(
                    mode = SubjectFormMode.SCHEDULE,
                    onBackClick = { navController.navigateBackOr(AppRoutes.Calendar, enabledModules) },
                    onSubjectSaved = { navController.navigateBackOr(AppRoutes.Calendar, enabledModules) },
                    onUpgradeClick = { navController.go(AppRoutes.Pro) }
                )
            }
            composable("${AppRoutes.EditSubjectFromSchedule}/{subjectId}") { backStackEntry ->
                SubjectFormScreen(
                    subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty(),
                    mode = SubjectFormMode.SCHEDULE,
                    onBackClick = { navController.navigateBackOr(AppRoutes.Calendar, enabledModules) },
                    onSubjectSaved = { navController.navigateBackOr(AppRoutes.Calendar, enabledModules) }
                )
            }
            composable(AppRoutes.AddSubjectFromTask) {
                SubjectFormScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.navigateIfModuleEnabled(AppRoutes.AddTask, enabledModules)
                        }
                    },
                    onSubjectSaved = {
                        if (!navController.navigateUp()) {
                            navController.navigateIfModuleEnabled(AppRoutes.AddTask, enabledModules)
                        }
                    },
                    onUpgradeClick = { navController.go(AppRoutes.Pro) }
                )
            }
            composable("${AppRoutes.SubjectDetail}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                SubjectDetailScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.academic(AppRoutes.AcademicTabSubjects), enabledModules)
                    },
                    onAddGradeClick = { id, periodId -> navController.navigateIfModuleEnabled(AppRoutes.addGrade(id, periodId), enabledModules) },
                    onPeriodClick = { id, periodId -> navController.navigateIfModuleEnabled(AppRoutes.subjectPeriodDetail(id, periodId), enabledModules) },
                    onEditSubjectClick = { id -> navController.navigateIfModuleEnabled(AppRoutes.editSubject(id), enabledModules) },
                    onEditGradeClick = { id, gradeId -> navController.navigateIfModuleEnabled(AppRoutes.editGrade(id, gradeId), enabledModules) },
                    onCompleteHistoryClick = { id ->
                        navController.navigateIfModuleEnabled(AppRoutes.priorHistory(id), enabledModules)
                    },
                    onSubjectDeleted = {
                        if (!navController.popBackStack(AppRoutes.Academic, inclusive = false)) {
                            navController.navigate(AppRoutes.academic(AppRoutes.AcademicTabSubjects)) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            composable("${AppRoutes.PriorHistory}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                PriorHistoryScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectDetail(subjectId), enabledModules)
                    },
                    onAddActivitiesClick = { id, periodId ->
                        navController.navigateIfModuleEnabled(
                            AppRoutes.addGradeFromHistory(id, periodId),
                            enabledModules
                        )
                    }
                )
            }
            composable("${AppRoutes.SubjectPeriodDetail}/{subjectId}/{periodId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                val periodId = backStackEntry.arguments?.getString("periodId").orEmpty()
                SubjectPeriodDetailScreen(
                    subjectId = subjectId,
                    periodId = periodId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectDetail(subjectId), enabledModules)
                    },
                    onAddGradeClick = { id, selectedPeriodId -> navController.navigateIfModuleEnabled(AppRoutes.addGrade(id, selectedPeriodId), enabledModules) },
                    onEditGradeClick = { id, gradeId -> navController.navigateIfModuleEnabled(AppRoutes.editGrade(id, gradeId), enabledModules) }
                )
            }
            composable("${AppRoutes.EditSubject}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                SubjectFormScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectDetail(subjectId), enabledModules)
                    },
                    onSubjectSaved = { id ->
                        if (!navController.navigateUp()) {
                            navController.navigateIfModuleEnabled(AppRoutes.subjectDetail(id), enabledModules)
                        }
                    }
                )
            }
            composable("${AppRoutes.AddGrade}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                AddGradeScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectDetail(subjectId), enabledModules)
                    },
                    onCompleteHistoryClick = { id ->
                        navController.navigate(AppRoutes.priorHistory(id)) {
                            popUpTo(AppRoutes.AddGrade) { inclusive = true }
                        }
                    }
                )
            }
            composable("${AppRoutes.AddGrade}/{subjectId}/{periodId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                val periodId = backStackEntry.arguments?.getString("periodId").orEmpty()
                AddGradeScreen(
                    subjectId = subjectId,
                    initialPeriodId = periodId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectPeriodDetail(subjectId, periodId), enabledModules)
                    },
                    onCompleteHistoryClick = { id ->
                        navController.go(AppRoutes.priorHistory(id))
                    }
                )
            }
            composable("${AppRoutes.AddGradeFromHistory}/{subjectId}/{periodId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                val periodId = backStackEntry.arguments?.getString("periodId").orEmpty()
                AddGradeScreen(
                    subjectId = subjectId,
                    initialPeriodId = periodId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.priorHistory(subjectId), enabledModules)
                    }
                )
            }
            composable("${AppRoutes.EditGrade}/{subjectId}/{gradeId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                val gradeId = backStackEntry.arguments?.getString("gradeId").orEmpty()
                AddGradeScreen(
                    subjectId = subjectId,
                    gradeId = gradeId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectDetail(subjectId), enabledModules)
                    }
                )
            }
            composable(AppRoutes.AddTask) {
                AddTaskScreen(
                    onBackClick = { navController.navigateBackOr(AppRoutes.academic(AppRoutes.AcademicTabTasks), enabledModules) },
                    onCreateSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubjectFromTask, enabledModules) }
                )
            }
            composable("${AppRoutes.EditTask}/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                AddTaskScreen(
                    taskId = taskId,
                    onCreateSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubjectFromTask, enabledModules) },
                    onEditLinkedGrade = { subjectId, gradeId ->
                        navController.navigateIfModuleEnabled(
                            AppRoutes.editGrade(subjectId, gradeId),
                            enabledModules
                        )
                    },
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.academic(AppRoutes.AcademicTabTasks), enabledModules)
                    }
                )
            }
            composable(AppRoutes.AddExpense) {
                AddExpenseScreen(onBackClick = { navController.navigateBackOr(AppRoutes.Expenses, enabledModules) })
            }
            composable("${AppRoutes.EditExpense}/{expenseId}") { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId").orEmpty()
                AddExpenseScreen(
                    expenseId = expenseId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.Expenses, enabledModules)
                    }
                )
            }
            composable(AppRoutes.AcademicTemplates) {
                AcademicTemplatesScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Home)
                        }
                    }
                )
            }
        }
        }
    }
}

/**
 * La ruta en la que está la app ahora mismo.
 *
 * `currentBackStackEntryAsState` arranca en `null`: empieza a recoger el flujo de destinos
 * *después* de componer, así que en el primer fotograma no hay entrada todavía. Quien caía de
 * ahí directo a Inicio pintaba Inicio durante ese fotograma y saltaba a la pestaña de verdad en
 * el siguiente, con la animación del indicador de por medio.
 *
 * Se notaba al salir de un formulario: esas rutas esconden la barra, así que al volver la barra
 * se compone desde cero y su primer fotograma decía Inicio. El indicador cruzaba de Inicio a la
 * sección delante del usuario, como si la app hubiera pasado por la pantalla de inicio.
 *
 * El destino del propio NavController sí se puede leer en el acto, y es el mismo dato.
 */
@Composable
private fun NavHostController.currentRouteAsState(): String? {
    val entry by currentBackStackEntryAsState()
    return entry?.destination?.route ?: currentDestination?.route
}

@Composable
private fun ModuleAccessGuard(
    navController: NavHostController,
    enabledModules: Set<AppModule>
) {
    val currentRoute = navController.currentRouteAsState() ?: AppRoutes.Home

    LaunchedEffect(currentRoute, enabledModules) {
        val module = moduleForRoute(currentRoute)
        if (module != null && module !in enabledModules) {
            navController.navigate(AppRoutes.Home) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = false
                }
                launchSingleTop = true
            }
        }
    }
}

/**
 * Rutas que se comportan como una tarea y no como un destino: crear y editar.
 *
 * Son las únicas que ocultan la barra inferior. Fuera de aquí, cualquier pantalla que
 * pertenezca a una sección la conserva, para que se vea dónde estás y se pueda cambiar de
 * sección sin tener que desandar el camino.
 *
 * El motivo de esconderla aquí no es estético: con la barra puesta, un toque en cualquier
 * pestaña abandona un formulario a medio llenar sin decir nada. Mientras no haya un aviso
 * antes de descartar, la salida de un formulario se queda en atrás o guardar.
 */
/**
 * Las pantallas que se abren desde el panel lateral.
 *
 * Van a pantalla completa, sin barra inferior. No son secciones de la app sino sitios a los que
 * se entra y de los que se sale por donde se vino: dejarles la barra invita a saltar a otra
 * pestaña a medio leer, y sobre todo las achata —una pantalla que ocupa todo se lee como un
 * sitio propio y no como una capa encima de Inicio.
 */
private val ImmersiveRoutes = setOf(
    AppRoutes.WhatsNew,
    AppRoutes.Resources,
    AppRoutes.Help,
    AppRoutes.About,
    AppRoutes.GpaCalculator,
    AppRoutes.QuickNotes,
    AppRoutes.AiAssistant,
    AppRoutes.Labs
)

private val ModalRoutes = setOf(
    AppRoutes.AddSubject,
    AppRoutes.AddSubjectFromTask,
    AppRoutes.AddSubjectFromSchedule,
    AppRoutes.EditSubject,
    AppRoutes.EditSubjectFromSchedule,
    AppRoutes.AddGrade,
    AppRoutes.AddGradeFromHistory,
    AppRoutes.EditGrade,
    AppRoutes.AddTask,
    AppRoutes.EditTask,
    AppRoutes.AddExpense,
    AppRoutes.EditExpense
)

/**
 * Si una ruta muestra la barra inferior.
 *
 * Se deriva de [bottomRouteFor] en vez de mantener una lista aparte. Antes eran dos fuentes
 * de verdad que no se hablaban: el mapa sabía que «agregar nota» pertenece a Académico, pero
 * la lista blanca de rutas con barra se escribía a mano y solo cubría siete. De las 29 rutas
 * mapeadas, veintidós calculaban su pestaña para nada.
 *
 * De paso corrige el salto que eso producía: el detalle de una materia es una pantalla de
 * consulta igual que su lista, y se quedaba sin barra mientras la lista la tenía.
 */
internal fun routeShowsBottomBar(route: String?): Boolean {
    if (ModalRoutes.any { routeBelongsTo(route, it) }) return false
    if (ImmersiveRoutes.any { routeBelongsTo(route, it) }) return false
    return bottomRouteFor(route) != null
}

internal fun bottomRouteFor(route: String?): String? {
    return when {
        routeBelongsTo(route, AppRoutes.Home) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Notifications) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.NotificationDetail) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Academic) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.Grades) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.AddSubject) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.AddSubjectFromTask) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.SubjectDetail) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.SubjectPeriodDetail) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.EditSubject) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.AddGrade) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.AddGradeFromHistory) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.EditGrade) -> AppRoutes.Academic
        // Faltaban las dos del historial. Sin mapear, la de consulta se quedaba sin barra
        // por omisión y no por decisión, que es justo lo que este cambio viene a corregir.
        routeBelongsTo(route, AppRoutes.PriorHistory) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.Tasks) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.AddTask) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.EditTask) -> AppRoutes.Academic
        routeBelongsTo(route, AppRoutes.Expenses) -> AppRoutes.Expenses
        routeBelongsTo(route, AppRoutes.AddExpense) -> AppRoutes.Expenses
        routeBelongsTo(route, AppRoutes.EditExpense) -> AppRoutes.Expenses
        routeBelongsTo(route, AppRoutes.Profile) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.Settings) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.AppearanceSettings) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.AccessibilitySettings) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.Calendar) -> AppRoutes.Calendar
        // El formulario de materia abierto desde Horario pertenece a Horario, que es a donde
        // vuelve al guardar. Sin esto se quedaría sin pestaña y la transición entraría por el
        // lado que no toca.
        routeBelongsTo(route, AppRoutes.AddSubjectFromSchedule) -> AppRoutes.Calendar
        routeBelongsTo(route, AppRoutes.EditSubjectFromSchedule) -> AppRoutes.Calendar
        routeBelongsTo(route, AppRoutes.AcademicSettings) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.ModuleSettings) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.NotificationSettings) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.DataSettings) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.UpdateSettings) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.WhatsNew) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Resources) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Help) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.About) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.GpaCalculator) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.QuickNotes) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.AiAssistant) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Labs) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Pro) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.AcademicTemplates) -> AppRoutes.Home
        else -> null
    }
}

/**
 * Módulo del que depende una ruta, para no dejar accesible lo que el usuario apagó.
 *
 * Las dos rutas de materia que salen de Horario quedan fuera a propósito. Crear una clase
 * crea una materia por debajo, pero Horario es una pestaña fija que sigue estando cuando
 * Académico está apagado: atarlas a [AppModule.GRADES] convertiría «Añadir clase» en un
 * botón que lleva a Inicio.
 */
internal fun moduleForRoute(route: String?): AppModule? {
    return when {
        routeBelongsTo(route, AppRoutes.Grades) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.AddSubject) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.AddSubjectFromTask) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.SubjectDetail) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.SubjectPeriodDetail) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.EditSubject) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.AddGrade) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.EditGrade) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.Tasks) -> AppModule.TASKS
        routeBelongsTo(route, AppRoutes.AddTask) -> AppModule.TASKS
        routeBelongsTo(route, AppRoutes.EditTask) -> AppModule.TASKS
        routeBelongsTo(route, AppRoutes.Expenses) -> AppModule.EXPENSES
        routeBelongsTo(route, AppRoutes.AddExpense) -> AppModule.EXPENSES
        routeBelongsTo(route, AppRoutes.EditExpense) -> AppModule.EXPENSES
        routeBelongsTo(route, AppRoutes.AcademicTemplates) -> AppModule.ACADEMIC_TEMPLATES
        else -> null
    }
}

internal fun shouldRestoreBottomRouteState(currentRoute: String?, targetRoute: String): Boolean {
    return targetRoute != AppRoutes.Home && bottomRouteFor(currentRoute) != targetRoute
}

internal fun shouldPopSelectedBottomRoute(currentRoute: String?, targetRoute: String): Boolean {
    return currentRoute != targetRoute && bottomRouteFor(currentRoute) == targetRoute
}

internal fun isForwardNavigation(initialRoute: String?, targetRoute: String?): Boolean {
    val initialRank = routeRank(initialRoute)
    val targetRank = routeRank(targetRoute)
    if (initialRank != targetRank) return targetRank > initialRank
    return routeDepth(targetRoute) >= routeDepth(initialRoute)
}


private fun routeRank(route: String?): Int {
    return when (bottomRouteFor(route)) {
        AppRoutes.Home -> 0
        AppRoutes.Academic -> 1
        AppRoutes.Calendar -> 2
        AppRoutes.Expenses -> 3
        AppRoutes.Profile -> 4
        else -> 0
    }
}

private fun routeDepth(route: String?): Int {
    val bottomRoute = bottomRouteFor(route)
    return if (route != null && bottomRoute != null && route != bottomRoute) 1 else 0
}

private fun NavHostController.navigateToBottomRoute(
    currentRoute: String?,
    targetRoute: String
) {
    if (currentRoute == targetRoute) return

    val targetIsCurrentSection = bottomRouteFor(currentRoute) == targetRoute
    if (targetIsCurrentSection && popBackStack(targetRoute, inclusive = false)) {
        return
    }

    if (targetRoute == AppRoutes.Home && popBackStack(AppRoutes.Home, inclusive = false)) {
        return
    }

    if (targetRoute != AppRoutes.Home && popBackStack(targetRoute, inclusive = false)) {
        return
    }

    navigate(targetRoute) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = targetRoute != AppRoutes.Home
    }
}

private fun NavHostController.navigateIfModuleEnabled(
    route: String,
    enabledModules: Set<AppModule>
) {
    val module = moduleForRoute(route)
    val blocked = (module != null && module !in enabledModules) || !routeIsBuilt(route)
    navigate(if (blocked) AppRoutes.Home else route) {
        launchSingleTop = true
    }
}

/**
 * Rutas que todavía no están terminadas.
 *
 * Fuera de dev y alpha no se entra a ninguna, venga el toque de donde venga: el panel las pinta
 * apagadas, pero una notificación con ruta guardada o un enlace de arranque también llegan
 * aquí. La puerta se cierra en un solo sitio.
 */
private val UnfinishedRoutes = setOf(
    AppRoutes.AcademicTemplates,
    AppRoutes.AiAssistant,
    AppRoutes.Labs
)

internal fun routeIsBuilt(route: String?): Boolean {
    if (BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished) return true
    return UnfinishedRoutes.none { routeBelongsTo(route, it) }
}

/**
 * Navega a una pantalla sin apilarla dos veces.
 *
 * Sin `launchSingleTop`, tocar dos veces la misma fila —de ajustes, del panel, de donde sea—
 * mete dos copias en la pila, y para salir hay que dar atrás tantas veces como toques se
 * dieron. No hay ningún sitio de la app donde apilar la misma pantalla sobre sí misma
 * signifique algo.
 */
private fun NavHostController.go(route: String) {
    navigate(route) { launchSingleTop = true }
}

private fun NavHostController.navigateBackOr(
    fallbackRoute: String,
    enabledModules: Set<AppModule>
) {
    if (!navigateUp()) {
        navigateIfModuleEnabled(fallbackRoute, enabledModules)
    }
}

/**
 * Reenvía a Académico en la pestaña indicada y se quita del historial.
 *
 * Sirve a las rutas antiguas de Materias y Tareas, que ya no tienen pantalla propia pero
 * siguen llegando desde recordatorios agendados antes de este cambio. Se sustituye a sí misma
 * en la pila para que el botón atrás no devuelva a una pantalla que solo redirige.
 */
@Composable
private fun RedirectToAcademic(navController: NavHostController, tab: String) {
    LaunchedEffect(tab) {
        navController.navigate(AppRoutes.academic(tab)) {
            popUpTo(AppRoutes.Home)
            launchSingleTop = true
        }
    }
}

private fun routeBelongsTo(route: String?, baseRoute: String): Boolean {
    return route == baseRoute ||
        route?.startsWith("$baseRoute/") == true ||
        // Rutas con argumento opcional: el patrón que informa el destino es
        // «academic?tab={tab}», que no es igual a «academic» ni empieza por «academic/».
        // Sin esta rama, una ruta así deja de pertenecer a su pestaña y pierde la barra.
        route?.startsWith("$baseRoute?") == true
}

@Composable
fun UniStackBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = BottomNavItem.items
) {
    val currentRoute = navController.currentRouteAsState() ?: AppRoutes.Home
    val selectedBottomRoute = bottomRouteFor(currentRoute)
    val showBottomBar = selectedBottomRoute != null && items.any { it.route == selectedBottomRoute }

    if (showBottomBar) {
        UniStackBottomBarContent(
            selectedRoute = selectedBottomRoute ?: currentRoute,
            items = items,
            onNavigate = { route ->
                navController.navigateToBottomRoute(
                    currentRoute = currentRoute,
                    targetRoute = route
                )
            },
            modifier = modifier
        )
    }
}

@Composable
private fun UniStackBottomBarContent(
    selectedRoute: String,
    items: List<BottomNavItem>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val appearance = LocalAppearancePreferences.current
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val navigationBarBottom = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }
    val showLabels = appearance.bottomBarStyle == BottomBarStyle.LABELED && items.size <= 5
    /*
     * Una sola barra: acoplada al borde, a todo lo ancho y con las esquinas rectas.
     *
     * Hubo una variante flotante —píldora separada de los bordes, con sombra y el contenido
     * pasando por debajo— y se retiró: en UniStack quedaba como un elemento suelto encima de
     * la app, y obligaba a que cada pantalla reservara a mano el hueco que tapaba.
     *
     * La altura sale del spec: 80dp con etiqueta, 64dp solo con iconos.
     */
    val barHeight = if (showLabels) 80.dp else 64.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight + navigationBarBottom),
        shape = RectangleShape,
        // Flotando hace falta más contraste y una sombra de verdad. El color de barra sale
        // de una superficie elevada, apenas un par de tonos por encima del fondo: acoplada
        // basta, porque el borde de la pantalla ya la separa, pero suspendida sobre el
        // contenido se confundía con lo que pasaba por detrás y se veía sucia.
        color = UniStackColors.BottomBar,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        // Sin contorno propio: el borde de la pantalla ya la delimita.
        border = null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp)
                .padding(bottom = navigationBarBottom),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                /*
                 * Un toque, un golpecito.
                 *
                 * Cambiar de sección es de lo poco que se hace sin mirar, con el pulgar y de
                 * memoria: el aviso al tacto confirma que se dio en el sitio sin tener que
                 * comprobarlo con la vista. Volver a tocar la sección en la que ya estás no
                 * vibra, porque ahí no ha pasado nada que confirmar.
                 */
                val selected = selectedRoute == item.route
                UniStackBottomBarItem(
                    item = item,
                    selected = selected,
                    showLabel = showLabels,
                    onClick = {
                        if (!selected) haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        onNavigate(item.route)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun UniStackBottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    showLabel: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motionDuration = (180 * LocalMotionDurationScale.current).roundToInt().coerceAtLeast(0)

    /* Un solo indicador, la píldora de 64x32 del spec. Antes había dos a la vez: un
       rectángulo redondeado detrás del icono y además un subrayado suelto debajo de la
       etiqueta, así que el elemento activo se marcaba por duplicado.

       El contenedor sale del acento y no de secondaryContainer, que es lo que pide
       Material: en esta app "secondary" es un azul con identidad propia, no una variante
       tonal del primario, y usarlo dejaría el indicador azul bajo un acento violeta. */
    val indicatorColor by animateColorAsState(
        targetValue = if (selected) UniStackColors.PrimaryLight else Color.Transparent,
        animationSpec = tween(motionDuration, easing = FastOutSlowInEasing),
        label = "bottomItemIndicator"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) {
            UniStackColors.OnPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(motionDuration, easing = FastOutSlowInEasing),
        label = "bottomItemIcon"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) {
            UniStackColors.TextPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(motionDuration, easing = FastOutSlowInEasing),
        label = "bottomItemLabel"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            // Un poco menos de compresión que en un botón: el destino se aprieta lo justo
            // para notarse bajo el dedo sin saltar dentro de una barra tan compacta.
            .squishOnPress(interactionSource, scale = 0.90f)
            .clip(RoundedCornerShape(percent = 50))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                // Máximo, no medida fija: flotando, cada casilla baja de 64dp y un ancho
                // rígido desbordaba la columna.
                //
                // Y por debajo del máximo del spec. Con 64 de ancho y 32 de alto la
                // píldora es el doble de larga que alta, y como el icono ocupa 24dp
                // quedan 20 de relleno a cada lado: en una barra de cinco destinos eso
                // se lee como una mancha estirada en vez de un indicador.
                .widthIn(max = 56.dp)
                .fillMaxWidth()
                .height(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(indicatorColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        if (showLabel) {
            Text(
                text = item.label,
                color = labelColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                // Antes cortaba en seco a media palabra. Si no cabe, que al menos se vea
                // que falta texto.
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, start = 2.dp, end = 2.dp)
            )
        }
    }
}
