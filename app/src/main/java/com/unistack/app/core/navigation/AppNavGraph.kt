package com.unistack.app.core.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unistack.app.core.AppContainer
import com.unistack.app.core.design.components.UniStackFabMenu
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_expenses.presentation.AddExpenseScreen
import com.unistack.app.feature_expenses.presentation.ExpensesScreen
import com.unistack.app.feature_grades.presentation.AddGradeScreen
import com.unistack.app.feature_grades.presentation.AddSubjectScreen
import com.unistack.app.feature_grades.presentation.GradesScreen
import com.unistack.app.feature_grades.presentation.SubjectDetailScreen
import com.unistack.app.feature_home.presentation.HomeScreen
import com.unistack.app.feature_home.presentation.HomeViewModel
import com.unistack.app.feature_profile.presentation.ProfileScreen
import com.unistack.app.feature_profile.presentation.ProScreen
import com.unistack.app.feature_tasks.presentation.AddTaskScreen
import com.unistack.app.feature_tasks.presentation.TasksScreen
import com.unistack.app.feature_templates.presentation.AcademicTemplatesScreen
import com.unistack.app.feature_user.domain.AppModule
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

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
    val enabledModules by remember {
        AppContainer.userRepository.userProfile
            .map { it?.enabledModules ?: DefaultEnabledModules }
            .distinctUntilChanged()
    }.collectAsStateWithLifecycle(
        initialValue = AppContainer.userRepository.userProfile.value?.enabledModules ?: DefaultEnabledModules
    )
    val bottomItems = remember(enabledModules) { BottomNavItem.itemsFor(enabledModules) }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.Home
    val showHomeFab = routeBelongsTo(currentRoute, AppRoutes.Home)
    val showBottomBar = currentRoute in setOf(
        AppRoutes.Home,
        AppRoutes.Grades,
        AppRoutes.Tasks,
        AppRoutes.Expenses,
        AppRoutes.Profile
    )

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
        bottomBar = {
            if (showBottomBar) {
                UniStackBottomBar(
                    navController = navController,
                    items = bottomItems
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = initialRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                enterTransition = {
                    mainSlideIn(fromRight = isForwardNavigation(initialState.destination.route, targetState.destination.route))
                },
                exitTransition = {
                    mainSlideOut(toLeft = isForwardNavigation(initialState.destination.route, targetState.destination.route))
                },
                popEnterTransition = {
                    mainSlideIn(fromRight = false)
                },
                popExitTransition = {
                    mainSlideOut(toLeft = false)
                }
            ) {
                composable(AppRoutes.Home) {
                    val viewModel: HomeViewModel = viewModel()
                    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                    HomeScreen(
                        uiState = uiState,
                        onAddSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubject, enabledModules) },
                        onSeeAllSubjectsClick = { navController.navigateIfModuleEnabled(AppRoutes.Grades, enabledModules) },
                        onSeeTasksClick = { navController.navigateIfModuleEnabled(AppRoutes.Tasks, enabledModules) },
                        onSeeExpensesClick = { navController.navigateIfModuleEnabled(AppRoutes.Expenses, enabledModules) },
                        onOpenTemplatesClick = { navController.navigateIfModuleEnabled(AppRoutes.AcademicTemplates, enabledModules) },
                        onSubjectClick = { subjectId -> navController.navigateIfModuleEnabled(AppRoutes.subjectDetail(subjectId), enabledModules) },
                        onProfileClick = {
                            navController.navigate(AppRoutes.Profile) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
            composable(AppRoutes.Grades) {
                GradesScreen(
                    onAddSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubject, enabledModules) },
                    onSubjectClick = { subjectId -> navController.navigateIfModuleEnabled(AppRoutes.subjectDetail(subjectId), enabledModules) }
                )
            }
            composable(AppRoutes.Tasks) {
                TasksScreen(
                    onNewTaskClick = { navController.navigateIfModuleEnabled(AppRoutes.AddTask, enabledModules) },
                    onEditTaskClick = { taskId -> navController.navigateIfModuleEnabled(AppRoutes.editTask(taskId), enabledModules) }
                )
            }
            composable(AppRoutes.Profile) {
                ProfileScreen(
                    onOpenProClick = { navController.navigate(AppRoutes.Pro) }
                )
            }
            composable(AppRoutes.Pro) {
                ProScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.navigate(AppRoutes.Profile)
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
                AddSubjectScreen(
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.Grades, enabledModules)
                    },
                    onSubjectSaved = { subjectId ->
                        navController.navigate(AppRoutes.subjectDetail(subjectId)) {
                            popUpTo(AppRoutes.AddSubject) {
                                inclusive = true
                            }
                        }
                    },
                    onUpgradeClick = { navController.navigate(AppRoutes.Pro) }
                )
            }
            composable(AppRoutes.AddSubjectFromTask) {
                AddSubjectScreen(
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
                    onUpgradeClick = { navController.navigate(AppRoutes.Pro) }
                )
            }
            composable("${AppRoutes.SubjectDetail}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                SubjectDetailScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.Grades, enabledModules)
                    },
                    onAddGradeClick = { id -> navController.navigateIfModuleEnabled(AppRoutes.addGrade(id), enabledModules) },
                    onEditSubjectClick = { id -> navController.navigateIfModuleEnabled(AppRoutes.editSubject(id), enabledModules) },
                    onEditGradeClick = { id, gradeId -> navController.navigateIfModuleEnabled(AppRoutes.editGrade(id, gradeId), enabledModules) },
                    onSubjectDeleted = {
                        if (!navController.popBackStack(AppRoutes.Grades, inclusive = false)) {
                            navController.navigate(AppRoutes.Grades) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
            composable("${AppRoutes.EditSubject}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                AddSubjectScreen(
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
                    onBackClick = { navController.navigateBackOr(AppRoutes.Tasks, enabledModules) },
                    onCreateSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubjectFromTask, enabledModules) }
                )
            }
            composable("${AppRoutes.EditTask}/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                AddTaskScreen(
                    taskId = taskId,
                    onCreateSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubjectFromTask, enabledModules) },
                    onBackClick = {
                        navController.navigateBackOr(AppRoutes.Tasks, enabledModules)
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
                            navController.navigate(AppRoutes.Home)
                        }
                    }
                )
            }
            }

            if (showHomeFab) {
                UniStackFabMenu(
                    onAddGradeClick = { navController.navigateIfModuleEnabled(AppRoutes.Grades, enabledModules) },
                    onAddTaskClick = { navController.navigateIfModuleEnabled(AppRoutes.AddTask, enabledModules) },
                    onAddExpenseClick = { navController.navigateIfModuleEnabled(AppRoutes.AddExpense, enabledModules) },
                    onAddSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubject, enabledModules) },
                    showAddGrade = AppModule.GRADES in enabledModules,
                    showAddTask = AppModule.TASKS in enabledModules,
                    showAddExpense = AppModule.EXPENSES in enabledModules,
                    showAddSubject = AppModule.GRADES in enabledModules,
                    expandedBottomPadding = 104.dp,
                    expandedEndPadding = 20.dp
                )
            }
        }
    }
}

@Composable
private fun ModuleAccessGuard(
    navController: NavHostController,
    enabledModules: Set<AppModule>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.Home

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

internal fun bottomRouteFor(route: String?): String? {
    return when {
        routeBelongsTo(route, AppRoutes.Home) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Grades) -> AppRoutes.Grades
        routeBelongsTo(route, AppRoutes.AddSubject) -> AppRoutes.Grades
        routeBelongsTo(route, AppRoutes.AddSubjectFromTask) -> AppRoutes.Tasks
        routeBelongsTo(route, AppRoutes.SubjectDetail) -> AppRoutes.Grades
        routeBelongsTo(route, AppRoutes.EditSubject) -> AppRoutes.Grades
        routeBelongsTo(route, AppRoutes.AddGrade) -> AppRoutes.Grades
        routeBelongsTo(route, AppRoutes.EditGrade) -> AppRoutes.Grades
        routeBelongsTo(route, AppRoutes.Tasks) -> AppRoutes.Tasks
        routeBelongsTo(route, AppRoutes.AddTask) -> AppRoutes.Tasks
        routeBelongsTo(route, AppRoutes.EditTask) -> AppRoutes.Tasks
        routeBelongsTo(route, AppRoutes.Expenses) -> AppRoutes.Expenses
        routeBelongsTo(route, AppRoutes.AddExpense) -> AppRoutes.Expenses
        routeBelongsTo(route, AppRoutes.EditExpense) -> AppRoutes.Expenses
        routeBelongsTo(route, AppRoutes.Profile) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.Pro) -> AppRoutes.Profile
        routeBelongsTo(route, AppRoutes.AcademicTemplates) -> AppRoutes.Home
        else -> null
    }
}

internal fun moduleForRoute(route: String?): AppModule? {
    return when {
        routeBelongsTo(route, AppRoutes.Grades) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.AddSubject) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.AddSubjectFromTask) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.SubjectDetail) -> AppModule.GRADES
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

private fun mainSlideIn(fromRight: Boolean) =
    slideInHorizontally(
        initialOffsetX = { width -> if (fromRight) width / 3 else -width / 3 },
        animationSpec = tween(MAIN_TRANSITION_MILLIS, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(110, delayMillis = 25, easing = FastOutSlowInEasing)
    )

private fun mainSlideOut(toLeft: Boolean) =
    slideOutHorizontally(
        targetOffsetX = { width -> if (toLeft) -width / 4 else width / 4 },
        animationSpec = tween(MAIN_EXIT_MILLIS, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(MAIN_EXIT_MILLIS, easing = FastOutSlowInEasing)
    )

private fun routeRank(route: String?): Int {
    return when (bottomRouteFor(route)) {
        AppRoutes.Home -> 0
        AppRoutes.Grades -> 1
        AppRoutes.Tasks -> 2
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
    navigate(if (module == null || module in enabledModules) route else AppRoutes.Home) {
        launchSingleTop = true
    }
}

private fun NavHostController.navigateBackOr(
    fallbackRoute: String,
    enabledModules: Set<AppModule>
) {
    if (!navigateUp()) {
        navigateIfModuleEnabled(fallbackRoute, enabledModules)
    }
}

private fun routeBelongsTo(route: String?, baseRoute: String): Boolean {
    return route == baseRoute || route?.startsWith("$baseRoute/") == true
}

@Composable
fun UniStackBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    items: List<BottomNavItem> = BottomNavItem.items
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppRoutes.Home
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
    val barColor = MaterialTheme.colorScheme.surface
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f)
    val borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    val density = LocalDensity.current
    val navigationBarBottom = with(density) { WindowInsets.navigationBars.getBottom(this).toDp() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(82.dp + navigationBarBottom),
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        color = barColor,
        tonalElevation = 1.dp,
        shadowElevation = 0.dp,
        border = BorderStroke(
            width = 0.5.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 12.dp,
                    top = 8.dp,
                    end = 12.dp,
                    bottom = 8.dp + navigationBarBottom
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = selectedRoute == item.route
                UniStackBottomBarItem(
                    item = item,
                    selected = selected,
                    inactiveColor = inactiveColor,
                    onClick = { onNavigate(item.route) },
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
    inactiveColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            inactiveColor
        },
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "bottomItemColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "bottomItemIconScale"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "bottomItemIndicatorAlpha"
    )
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(top = 7.dp, bottom = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = contentColor,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                }
        )
        Text(
            text = item.label,
            color = contentColor,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            modifier = Modifier.padding(top = 4.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(width = 18.dp, height = 3.dp)
                .graphicsLayer { alpha = indicatorAlpha },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {}
        }
    }
}
