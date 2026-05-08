package com.unistack.app.core.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unistack.app.core.AppContainer
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_expenses.presentation.AddExpenseScreen
import com.unistack.app.feature_expenses.presentation.ExpensesScreen
import com.unistack.app.feature_grades.presentation.AddGradeScreen
import com.unistack.app.feature_grades.presentation.AddSubjectScreen
import com.unistack.app.feature_grades.presentation.GradeSimulatorScreen
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

private const val MAIN_TRANSITION_MILLIS = 220
private const val MAIN_EXIT_MILLIS = 150

@Composable
fun MainNavGraph(
    modifier: Modifier = Modifier,
    initialRoute: String = AppRoutes.Home,
    launchRoute: String? = null,
    onLaunchRouteConsumed: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val profile by AppContainer.userRepository.userProfile.collectAsState()
    val enabledModules = profile?.enabledModules ?: setOf(AppModule.GRADES, AppModule.TASKS)
    val bottomItems = BottomNavItem.itemsFor(enabledModules)
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: AppRoutes.Home
    val selectedBottomRoute = bottomRouteFor(currentRoute)
    val showBottomBar = selectedBottomRoute != null && bottomItems.any { it.route == selectedBottomRoute }

    LaunchedEffect(launchRoute, enabledModules) {
        launchRoute?.let { route ->
            navController.navigateIfModuleEnabled(route, enabledModules)
            onLaunchRouteConsumed()
        }
    }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UniStackColors.Background,
        bottomBar = {
            if (showBottomBar) {
                UniStackBottomBar(
                    currentRoute = selectedBottomRoute ?: currentRoute,
                    items = bottomItems,
                    onNavigate = { route ->
                        navController.navigateToBottomRoute(
                            currentRoute = currentRoute,
                            targetRoute = route
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
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
                val uiState by viewModel.uiState.collectAsState()
                HomeScreen(
                    uiState = uiState,
                    onAddGradeClick = { navController.navigateIfModuleEnabled(AppRoutes.Grades, enabledModules) },
                    onNewTaskClick = { navController.navigateIfModuleEnabled(AppRoutes.AddTask, enabledModules) },
                    onAddExpenseClick = { navController.navigateIfModuleEnabled(AppRoutes.AddExpense, enabledModules) },
                    onAddSubjectClick = { navController.navigateIfModuleEnabled(AppRoutes.AddSubject, enabledModules) },
                    onSeeAllSubjectsClick = { navController.navigateIfModuleEnabled(AppRoutes.Grades, enabledModules) },
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
                    onOpenSimulatorClick = { navController.navigateIfModuleEnabled(AppRoutes.GradeSimulator, enabledModules) },
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
                AddTaskScreen(onBackClick = { navController.navigateBackOr(AppRoutes.Tasks, enabledModules) })
            }
            composable("${AppRoutes.EditTask}/{taskId}") { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                AddTaskScreen(
                    taskId = taskId,
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
            composable(AppRoutes.GradeSimulator) {
                GradeSimulatorScreen(onBackClick = { navController.navigateBackOr(AppRoutes.Grades, enabledModules) })
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
    }
}

internal fun bottomRouteFor(route: String?): String? {
    return when {
        routeBelongsTo(route, AppRoutes.Home) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Grades) -> AppRoutes.Grades
        routeBelongsTo(route, AppRoutes.AddSubject) -> AppRoutes.Grades
        routeBelongsTo(route, AppRoutes.GradeSimulator) -> AppRoutes.Grades
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
        routeBelongsTo(route, AppRoutes.Profile) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.Pro) -> AppRoutes.Home
        routeBelongsTo(route, AppRoutes.AcademicTemplates) -> AppRoutes.Home
        else -> null
    }
}

internal fun moduleForRoute(route: String?): AppModule? {
    return when {
        routeBelongsTo(route, AppRoutes.Grades) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.AddSubject) -> AppModule.GRADES
        routeBelongsTo(route, AppRoutes.GradeSimulator) -> AppModule.GRADES
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

    if (shouldPopSelectedBottomRoute(currentRoute, targetRoute) && popBackStack(targetRoute, inclusive = false)) {
        return
    }

    val restoreState = shouldRestoreBottomRouteState(currentRoute, targetRoute)
    navigate(targetRoute) {
        popUpTo(graph.findStartDestination().id) {
            saveState = restoreState
        }
        launchSingleTop = true
        this.restoreState = restoreState
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
private fun UniStackBottomBar(
    currentRoute: String,
    items: List<BottomNavItem>,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = AppShapes.BottomBar,
        color = UniStackColors.BottomBar,
        tonalElevation = 4.dp,
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route
                val pillColor by animateColorAsState(if (selected) UniStackColors.BottomBarSelected else Color.Transparent, label = "pill")
                val contentColor by animateColorAsState(if (selected) UniStackColors.Primary else UniStackColors.TextPrimary, label = "content")
                val iconScale by animateFloatAsState(
                    targetValue = if (selected) 1.07f else 1f,
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    label = "bottomIconScale"
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppShapes.Pill)
                        .bounceClick { onNavigate(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(31.dp)
                            .fillMaxWidth(0.68f)
                            .clip(AppShapes.Pill)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                            .background(pillColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = contentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = item.label,
                        color = contentColor,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}
