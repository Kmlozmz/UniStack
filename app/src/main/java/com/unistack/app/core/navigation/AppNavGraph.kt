package com.unistack.app.core.navigation

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.ime
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationItemIconPosition
import androidx.compose.material3.ShortNavigationBarArrangement
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.unistack.app.core.utils.performSafely
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unistack.app.core.di.rememberUniStackEntryPoint
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.feature_expenses.presentation.AddExpenseScreen
import com.unistack.app.feature_expenses.presentation.ExpensesScreen
import com.unistack.app.feature_grades.presentation.AddGradeScreen
import com.unistack.app.feature_grades.presentation.AcademicScreen
import com.unistack.app.feature_grades.presentation.PriorHistoryScreen
import com.unistack.app.feature_grades.presentation.SubjectDetailScreen
import com.unistack.app.feature_grades.presentation.SubjectFormMode
import com.unistack.app.feature_grades.presentation.SubjectFormScreen
import com.unistack.app.feature_grades.presentation.SubjectCutDetailScreen
import com.unistack.app.feature_home.presentation.HomeScreen
import com.unistack.app.feature_home.presentation.HomeViewModel
import com.unistack.app.feature_notifications.presentation.NotificationDetailScreen
import com.unistack.app.feature_notifications.presentation.NotificationHistoryScreen
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.presentation.ProScreen
import com.unistack.app.feature_profile.presentation.AppearanceSettingsScreen
import com.unistack.app.feature_terms.presentation.NewTermScreen
import com.unistack.app.feature_terms.presentation.TermsViewModel
import com.unistack.app.feature_terms.presentation.AcademicHistoryScreen
import com.unistack.app.feature_terms.presentation.ClosedTermDetailScreen
import com.unistack.app.feature_terms.presentation.TermCloseScreen
import com.unistack.app.feature_profile.presentation.AcademicSettingsScreen
import com.unistack.app.feature_profile.presentation.AccountSettingsScreen
import com.unistack.app.feature_profile.presentation.ModuleSettingsScreen
import com.unistack.app.feature_profile.presentation.NotificationSettingsScreen
import com.unistack.app.feature_profile.presentation.DataSettingsScreen
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
import com.unistack.app.feature_templates.presentation.AcademicTemplatesScreen
import com.unistack.app.feature_templates.presentation.AcademicWorkScreen
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
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

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
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            /*
             * La barra no se mueve nunca: vive pegada al borde de la pantalla.
             *
             * Con `adjustResize` la ventana encogía al abrirse el teclado y el `Scaffold`
             * recolocaba su barra **encima** del teclado. Esconderla tampoco valía: una barra
             * de pestañas que aparece y desaparece deja de ser un punto fijo.
             *
             * La ventana ya no encoge (`adjustNothing` en el manifiesto): el teclado se pone
             * por delante, la barra se queda donde estaba —tapada mientras escribes— y lo que
             * se aparta es el contenido, con el hueco que se calcula abajo.
             */
            if (showBottomBar) {
                UniStackBottomBar(
                    navController = navController,
                    items = bottomItems
                )
            }
        }
    ) { innerPadding ->
        /*
         * Abajo se reserva lo que tape más: la barra o el teclado.
         *
         * No se suman. Con el teclado arriba la barra queda por detrás de él, así que contar
         * las dos alturas dejaría el contenido flotando ochenta píxeles más arriba de donde
         * empieza el teclado.
         *
         * Y se lee el inset crudo, que es la altura real del teclado en pantalla: aquí abajo
         * nadie lo ha consumido todavía, porque el hueco se abre justo en la línea siguiente.
         */
        val keyboardBottom = with(LocalDensity.current) {
            WindowInsets.ime.getBottom(this).toDp()
        }
        val contentPadding = PaddingValues(
            start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
            top = innerPadding.calculateTopPadding(),
            end = innerPadding.calculateEndPadding(LocalLayoutDirection.current),
            bottom = maxOf(innerPadding.calculateBottomPadding(), keyboardBottom)
        )
        Box(modifier = Modifier.fillMaxSize()) {
            /*
             * El empuje: la que entra llega desde el borde, la que se va se aparta un tercio.
             *
             * Se apilaban unas sobre otras al cambiar rapido, y el motivo no era la curva:
             * era que ningun destino pintaba fondo propio, asi que durante el deslizamiento
             * se veian las dos a la vez. Cada uno va ahora dentro de una superficie opaca
             * -ver screen()-, y con eso el problema no puede darse: lo de arriba tapa.
             *
             * Sin fundido. Apagarse es volverse transparente, que es exactamente lo que hay
             * que evitar aqui. Y los muelles salen del tema, no de duraciones escritas.
             */
            val motionEnabled = LocalMotionDurationScale.current > 0f
            val slide = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()

            NavHost(
                navController = navController,
                startDestination = resolvedInitialRoute,
                modifier = Modifier
                    .fillMaxSize()
                    /*
                     * El teclado se esquiva aquí dentro, y una sola vez.
                     *
                     * Se declara consumido para que el imePadding() que aplican algunas
                     * pantallas por su cuenta no vuelva a apartarlas: el hueco de arriba ya
                     * las ha subido, y sumar el segundo las dejaba con el contenido a media
                     * pantalla. Consumir es lo que hacía la raíz antes de que el teclado
                     * pasara a resolverse dentro del Scaffold, y por eso aquellas pantallas
                     * nunca contaron doble.
                     */
                    .consumeWindowInsets(WindowInsets.ime)
                    .padding(contentPadding),
                enterTransition = {
                    if (!motionEnabled || isTabSwitch(initialState, targetState)) EnterTransition.None
                    else slideInHorizontally(animationSpec = slide) { width -> width }
                },
                exitTransition = {
                    if (!motionEnabled || isTabSwitch(initialState, targetState)) ExitTransition.None
                    else slideOutHorizontally(animationSpec = slide) { width -> -width / 3 }
                },
                popEnterTransition = {
                    if (!motionEnabled || isTabSwitch(initialState, targetState)) EnterTransition.None
                    else slideInHorizontally(animationSpec = slide) { width -> -width / 3 }
                },
                popExitTransition = {
                    if (!motionEnabled || isTabSwitch(initialState, targetState)) ExitTransition.None
                    else slideOutHorizontally(animationSpec = slide) { width -> width }
                }
            ) {
                composable(AppRoutes.Home) {
                    val viewModel: HomeViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                    // Para saber si hay periodo en curso. Inicio no puede quedarse en blanco
                    // entre un semestre y el siguiente.
                    val termsViewModel: TermsViewModel = hiltViewModel()
                    val termsState by termsViewModel.uiState.collectAsStateWithLifecycle()

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
                            noActiveTerm = if (termsState.activeTerm == null) {
                                termsState.lastClosed
                            } else {
                                null
                            },
                            inheritedCutCount = termsState.inheritance?.cutCount ?: 0,
                            onStartNewTermClick = { navController.go(AppRoutes.NewTerm) },
                            onOpenHistoryClick = { navController.go(AppRoutes.AcademicHistory) },
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
            screen(AppRoutes.Notifications) {
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
            screen("${AppRoutes.NotificationDetail}/{notificationId}") { backStackEntry ->
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
            screen(AppRoutes.Grades) {
                RedirectToAcademic(navController, AppRoutes.AcademicTabSubjects)
            }
            screen(AppRoutes.Tasks) {
                RedirectToAcademic(navController, AppRoutes.AcademicTabTasks)
            }
            screen(AppRoutes.Profile) {
                AccountSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    },
                    onOpenProClick = { navController.go(AppRoutes.Pro) }
                )
            }
            screen(
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
                    onEditSubjectClick = { subjectId ->
                        navController.navigateIfModuleEnabled(AppRoutes.editSubject(subjectId), enabledModules)
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
            screen(AppRoutes.Settings) {
                SettingsHubScreen(
                    // Sin flecha: es la raíz de su pestaña, no se vuelve de ella a ningún
                    // sitio. Se llega tocando «Ajustes» abajo y se sale igual.
                    onBackClick = null,
                    onAppearanceClick = { navController.go(AppRoutes.AppearanceSettings) },
                    onAccessibilityClick = { navController.go(AppRoutes.AccessibilitySettings) },
                    onProfileClick = {
                        navController.navigate(AppRoutes.Profile) {
                            launchSingleTop = true
                        }
                    },
                    onAcademicClick = { navController.go(AppRoutes.AcademicSettings) },
                    onAcademicHistoryClick = { navController.go(AppRoutes.AcademicHistory) },
                    onModulesClick = { navController.go(AppRoutes.ModuleSettings) },
                    onNotificationsClick = { navController.go(AppRoutes.NotificationSettings) },
                    onDataClick = { navController.go(AppRoutes.DataSettings) },
                    onUpdatesClick = { navController.go(AppRoutes.UpdateSettings) }
                )
            }
            screen(AppRoutes.UpdateSettings) {
                UpdateSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            screen(AppRoutes.AppearanceSettings) {
                AppearanceSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            screen(AppRoutes.AccessibilitySettings) {
                AccessibilitySettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            screen(AppRoutes.Calendar) {
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
            screen(AppRoutes.AcademicSettings) {
                AcademicSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            screen(AppRoutes.NewTerm) {
                NewTermScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Home)
                        }
                    },
                    onCreated = { navController.go(AppRoutes.Home) }
                )
            }
            screen(AppRoutes.AcademicHistory) {
                AcademicHistoryScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    },
                    onTermClick = { termId ->
                        navController.navigate(AppRoutes.closedTerm(termId)) {
                            launchSingleTop = true
                        }
                    },
                    onCloseTermClick = {
                        navController.navigate(AppRoutes.TermClose) { launchSingleTop = true }
                    }
                )
            }
            screen(AppRoutes.TermClose) {
                TermCloseScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.AcademicHistory)
                        }
                    },
                    // Lo que falta se completa en Académico, que es donde estan las notas.
                    onGoComplete = { navController.go(AppRoutes.Academic) },
                    // Cerrado el periodo, esta pantalla ya no tiene nada que ensenar.
                    onClosed = { navController.go(AppRoutes.Home) }
                )
            }
            screen("${AppRoutes.ClosedTerm}/{termId}") { backStackEntry ->
                ClosedTermDetailScreen(
                    termId = backStackEntry.arguments?.getString("termId").orEmpty(),
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.AcademicHistory)
                        }
                    },
                    onSubjectClick = { subjectId ->
                        navController.navigateIfModuleEnabled(
                            AppRoutes.subjectDetail(subjectId),
                            enabledModules
                        )
                    }
                )
            }
            screen(AppRoutes.ModuleSettings) {
                ModuleSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            screen(AppRoutes.NotificationSettings) {
                NotificationSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            screen(AppRoutes.DataSettings) {
                DataSettingsScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Settings)
                        }
                    }
                )
            }
            screen(AppRoutes.WhatsNew) {
                WhatsNewScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            screen(AppRoutes.Resources) {
                ResourcesScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            screen(AppRoutes.Help) {
                HelpScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            screen(AppRoutes.About) {
                AboutScreen(
                    onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) }
                )
            }
            screen(AppRoutes.GpaCalculator) {
                GpaCalculatorScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            screen(AppRoutes.QuickNotes) {
                QuickNotesScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            screen(AppRoutes.AiAssistant) {
                AiAssistantScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            screen(AppRoutes.Labs) {
                LabsScreen(onBackClick = { if (!navController.navigateUp()) navController.go(AppRoutes.Home) })
            }
            /*
             * Pro solo se registra si está encendido.
             *
             * Apagado, la app va sin límites y no hay nada que vender: una ruta viva a una
             * pantalla de compra que nadie puede alcanzar es una puerta que solo se abre por
             * error —una notificación vieja, un enlace guardado— y aterriza en una oferta que
             * no existe.
             */
            if (FeatureGate.PRO_FEATURES_ENABLED) {
            screen(AppRoutes.Pro) {
                ProScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Profile)
                        }
                    }
                )
            }
            }
            screen(AppRoutes.Expenses) {
                ExpensesScreen(
                    onAddExpenseClick = { navController.navigateIfModuleEnabled(AppRoutes.AddExpense, enabledModules) },
                    onEditExpenseClick = { expenseId -> navController.navigateIfModuleEnabled(AppRoutes.editExpense(expenseId), enabledModules) }
                )
            }
            screen(AppRoutes.AddSubject) {
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
            screen(AppRoutes.AddSubjectFromSchedule) {
                SubjectFormScreen(
                    mode = SubjectFormMode.SCHEDULE,
                    onBackClick = { navController.navigateBackOr(AppRoutes.Calendar, enabledModules) },
                    onSubjectSaved = { navController.navigateBackOr(AppRoutes.Calendar, enabledModules) },
                    onUpgradeClick = { navController.go(AppRoutes.Pro) }
                )
            }
            screen("${AppRoutes.EditSubjectFromSchedule}/{subjectId}") { backStackEntry ->
                SubjectFormScreen(
                    subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty(),
                    mode = SubjectFormMode.SCHEDULE,
                    onBackClick = { navController.navigateBackOr(AppRoutes.Calendar, enabledModules) },
                    onSubjectSaved = { navController.navigateBackOr(AppRoutes.Calendar, enabledModules) }
                )
            }
            screen(AppRoutes.AddSubjectFromTask) {
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
            screen("${AppRoutes.SubjectDetail}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                SubjectDetailScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.academic(AppRoutes.AcademicTabSubjects), enabledModules)
                    },
                    onAddGradeClick = { id, cutId -> navController.navigateIfModuleEnabled(AppRoutes.addGrade(id, cutId), enabledModules) },
                    onCutClick = { id, cutId -> navController.navigateIfModuleEnabled(AppRoutes.subjectCutDetail(id, cutId), enabledModules) },
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
            screen("${AppRoutes.PriorHistory}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                PriorHistoryScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectDetail(subjectId), enabledModules)
                    },
                    onAddActivitiesClick = { id, cutId ->
                        navController.navigateIfModuleEnabled(
                            AppRoutes.addGradeFromHistory(id, cutId),
                            enabledModules
                        )
                    }
                )
            }
            screen("${AppRoutes.SubjectCutDetail}/{subjectId}/{periodId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                val cutId = backStackEntry.arguments?.getString("periodId").orEmpty()
                SubjectCutDetailScreen(
                    subjectId = subjectId,
                    cutId = cutId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectDetail(subjectId), enabledModules)
                    },
                    onAddGradeClick = { id, selectedCutId -> navController.navigateIfModuleEnabled(AppRoutes.addGrade(id, selectedCutId), enabledModules) },
                    onEditGradeClick = { id, gradeId -> navController.navigateIfModuleEnabled(AppRoutes.editGrade(id, gradeId), enabledModules) }
                )
            }
            screen("${AppRoutes.EditSubject}/{subjectId}") { backStackEntry ->
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
            screen("${AppRoutes.AddGrade}/{subjectId}") { backStackEntry ->
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
            screen("${AppRoutes.AddGrade}/{subjectId}/{periodId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                val cutId = backStackEntry.arguments?.getString("periodId").orEmpty()
                AddGradeScreen(
                    subjectId = subjectId,
                    initialCutId = cutId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.subjectCutDetail(subjectId, cutId), enabledModules)
                    },
                    onCompleteHistoryClick = { id ->
                        navController.go(AppRoutes.priorHistory(id))
                    }
                )
            }
            screen("${AppRoutes.AddGradeFromHistory}/{subjectId}/{periodId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                val cutId = backStackEntry.arguments?.getString("periodId").orEmpty()
                AddGradeScreen(
                    subjectId = subjectId,
                    initialCutId = cutId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.priorHistory(subjectId), enabledModules)
                    }
                )
            }
            screen("${AppRoutes.EditGrade}/{subjectId}/{gradeId}") { backStackEntry ->
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
            screen(AppRoutes.AddTask) {
                AddTaskScreen(
                    onBackClick = { navController.navigateBackOr(AppRoutes.academic(AppRoutes.AcademicTabTasks), enabledModules) },
                    onCreateSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubjectFromTask, enabledModules) }
                )
            }
            screen("${AppRoutes.EditTask}/{taskId}") { backStackEntry ->
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
            screen(AppRoutes.AddExpense) {
                AddExpenseScreen(onBackClick = { navController.navigateBackOr(AppRoutes.Expenses, enabledModules) })
            }
            screen("${AppRoutes.EditExpense}/{expenseId}") { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getString("expenseId").orEmpty()
                AddExpenseScreen(
                    expenseId = expenseId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.Expenses, enabledModules)
                    }
                )
            }
            screen(AppRoutes.AcademicTemplates) {
                AcademicTemplatesScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.Home)
                        }
                    },
                    onWorkClick = { workId -> navController.go(AppRoutes.academicWork(workId)) }
                )
            }
            screen(
                route = AppRoutes.AcademicWork,
                arguments = listOf(navArgument(AppRoutes.AcademicWorkArg) { type = NavType.StringType })
            ) { entry ->
                AcademicWorkScreen(
                    workId = entry.arguments?.getString(AppRoutes.AcademicWorkArg).orEmpty(),
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.go(AppRoutes.AcademicTemplates)
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

/**
 * Si el cambio es entre dos pestañas de la barra de abajo.
 *
 * Esas no se empujan: la barra no es una pila, es un conmutador. Deslizar entre ellas cuenta
 * un viaje que no ocurre —Gastos no está «a la derecha» de Inicio— y al pulsar rápido convierte
 * la barra en un carrusel. El empuje se reserva para entrar y salir de una pantalla, que sí es
 * ir hacia dentro y volver.
 */
private fun isTabSwitch(
    initial: androidx.navigation.NavBackStackEntry,
    target: androidx.navigation.NavBackStackEntry
): Boolean {
    return isBottomRoot(initial.destination.route) && isBottomRoot(target.destination.route)
}

/**
 * Si una ruta es la raíz de una pestaña y no algo abierto dentro de ella.
 *
 * Se compara sin los argumentos: Académico está registrado como `academic?tab={tab}`, así que
 * comparar la ruta entera nunca coincidía con su propia raíz y el cambio de pestaña se seguía
 * empujando.
 */
private fun isBottomRoot(route: String?): Boolean {
    if (route == null) return false
    val base = route.substringBefore('?')
    return bottomRouteFor(base) == base
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
        routeBelongsTo(route, AppRoutes.SubjectCutDetail) -> AppRoutes.Academic
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
        routeBelongsTo(route, AppRoutes.Profile) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.Settings) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.AppearanceSettings) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.AccessibilitySettings) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.Calendar) -> AppRoutes.Calendar
        // El formulario de materia abierto desde Horario pertenece a Horario, que es a donde
        // vuelve al guardar. Sin esto se quedaría sin pestaña y la transición entraría por el
        // lado que no toca.
        routeBelongsTo(route, AppRoutes.AddSubjectFromSchedule) -> AppRoutes.Calendar
        routeBelongsTo(route, AppRoutes.EditSubjectFromSchedule) -> AppRoutes.Calendar
        routeBelongsTo(route, AppRoutes.AcademicSettings) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.AcademicHistory) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.ClosedTerm) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.TermClose) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.NewTerm) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.ModuleSettings) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.NotificationSettings) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.DataSettings) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.UpdateSettings) -> AppRoutes.Settings
        routeBelongsTo(route, AppRoutes.WhatsNew) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Resources) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Help) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.About) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.GpaCalculator) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.QuickNotes) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.AiAssistant) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Labs) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Pro) -> AppRoutes.Settings
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
        routeBelongsTo(route, AppRoutes.SubjectCutDetail) -> AppModule.GRADES
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

/**
 * Un destino del grafo, dibujado en su capa.
 *
 * La pantalla que entra se dibujaba **por detrás** de la que sale, así que el empuje se veía al
 * revés de lo que cuenta: en vez de una hoja nueva tapando a la anterior, parecía que la
 * anterior se apartaba para dejar ver algo que ya estaba puesto debajo.
 *
 * El orden lo decide la profundidad de la ruta: lo que está más adentro se pinta por encima de
 * lo que está más afuera. Al entrar a un detalle, el detalle tapa; al volver, el detalle sigue
 * encima mientras se va, que es lo que hace que se lea como retirar una hoja.
 */
private fun NavGraphBuilder.screen(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(route = route, arguments = arguments) { entry ->
        // Fondo propio y opaco. Es la condicion para que el empuje se lea: sin el, durante
        // el deslizamiento se ve la pantalla de debajo a traves de la de encima.
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content(entry)
        }
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
        /*
         * Cambiar de pestaña te deja en su pantalla principal, no donde lo dejaste.
         *
         * Con `restoreState` puesto, la barra devolvía la pila entera de la pestaña: si salías
         * de Ajustes estando dentro de Apariencia, al volver a Ajustes aparecía Apariencia. No
         * es lo que promete un botón que dice «Ajustes» y lleva el icono de Ajustes, y además
         * la transición se hacía entre dos pantallas que no están al mismo nivel, así que se
         * veía como si la app se hubiera saltado un paso.
         *
         * Volver donde lo dejaste sigue funcionando dentro de la propia pestaña: tocar la
         * pestaña en la que ya estás sube a su raíz, y el botón de atrás deshace el camino.
         */
        restoreState = false
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
    val haptics = LocalHapticFeedback.current
    val showLabels = appearance.bottomBarStyle == BottomBarStyle.LABELED && items.size <= 5

    /*
     * La barra de navegación de Material 3 Expressive.
     *
     * Antes era una `Surface` con una fila dentro y un item escrito a mano por cada pestaña:
     * la pastilla del seleccionado, su animación de entrada y la altura de la barra estaban
     * calculadas aquí a base de números —80dp con etiqueta, 64dp sin ella—. `ShortNavigationBar`
     * trae todo eso, además del comportamiento con lector de pantalla y del hueco de la barra
     * del sistema, que también se restaba a mano.
     *
     * Sigue acoplada al borde y a todo lo ancho: hubo una variante flotante y se retiró porque
     * en UniStack quedaba como un elemento suelto encima de la app.
     *
     * **Icono arriba y reparto a partes iguales.** Con el icono al lado del rótulo, cinco
     * pestañas no caben en un teléfono estrecho sin cortar «Académico»; en columna, cada una
     * ocupa su quinto y el rótulo cabe entero.
     */
    ShortNavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        arrangement = ShortNavigationBarArrangement.EqualWeight
    ) {
        items.forEach { item ->
            val selected = selectedRoute == item.route
            ShortNavigationBarItem(
                selected = selected,
                onClick = {
                    /*
                     * Un toque, un golpecito.
                     *
                     * Cambiar de sección es de lo poco que se hace sin mirar, con el pulgar y
                     * de memoria: el aviso al tacto confirma que se dio en el sitio sin tener
                     * que comprobarlo con la vista. Volver a tocar la sección en la que ya
                     * estás no vibra, porque ahí no ha pasado nada que confirmar.
                     */
                    if (!selected) haptics.performSafely(HapticFeedbackType.ContextClick)
                    onNavigate(item.route)
                },
                icon = {
                    /*
                     * El icono de la sección en la que estás sube un poco y crece un pelo.
                     *
                     * La pastilla de Material ya dice cuál está elegida, pero es un cambio de
                     * color y el ojo lo pierde cuando la barra entera es del mismo tono. El
                     * desplazamiento no compite con el color: se nota con el rabillo del ojo
                     * aunque no estés mirando la barra.
                     *
                     * El muelle sale del `motionScheme` del tema, así que respeta el ajuste de
                     * movimiento reducido: con las animaciones bajadas, salta sin rebotar.
                     */
                    val lift by animateDpAsState(
                        targetValue = if (selected) (-2).dp else 0.dp,
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                        label = "elevación del icono"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.12f else 1f,
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                        label = "tamaño del icono"
                    )
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        modifier = Modifier
                            .offset(y = lift)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                },
                label = if (showLabels) {
                    { Text(item.label, maxLines = 1) }
                } else {
                    null
                },
                iconPosition = NavigationItemIconPosition.Top
            )
        }
    }
}
