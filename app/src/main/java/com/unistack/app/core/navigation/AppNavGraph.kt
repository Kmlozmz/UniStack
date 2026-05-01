package com.unistack.app.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
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
import com.unistack.app.feature_tasks.presentation.AddTaskScreen
import com.unistack.app.feature_tasks.presentation.TasksScreen

@Composable
fun MainNavGraph(
    modifier: Modifier = Modifier,
    initialRoute: String = AppRoutes.Home,
    launchRoute: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route ?: AppRoutes.Home
    val showBottomBar = BottomNavItem.items.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }

    LaunchedEffect(launchRoute) {
        launchRoute?.let { route ->
            navController.navigate(route)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UniStackColors.Background,
        bottomBar = {
            if (showBottomBar) {
                UniStackBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
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
                val isTab = BottomNavItem.items.any { it.route == targetState.destination.route }
                if (isTab) {
                    fadeIn(tween(300))
                } else {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeIn(tween(300))
                }
            },
            exitTransition = {
                val isTab = BottomNavItem.items.any { it.route == initialState.destination.route }
                if (isTab) {
                    fadeOut(tween(300))
                } else {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) + fadeOut(tween(300))
                }
            },
            popEnterTransition = {
                val isTab = BottomNavItem.items.any { it.route == targetState.destination.route }
                if (isTab) {
                    fadeIn(tween(300))
                } else {
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeIn(tween(300))
                }
            },
            popExitTransition = {
                val isTab = BottomNavItem.items.any { it.route == initialState.destination.route }
                if (isTab) {
                    fadeOut(tween(300))
                } else {
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) + fadeOut(tween(300))
                }
            }
        ) {
            composable(AppRoutes.Home) {
                val viewModel: HomeViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()
                HomeScreen(
                    uiState = uiState,
                    onAddGradeClick = { navController.navigate(AppRoutes.Grades) },
                    onNewTaskClick = { navController.navigate(AppRoutes.AddTask) },
                    onAddExpenseClick = { navController.navigate(AppRoutes.AddExpense) },
                    onAddSubjectClick = { navController.navigate(AppRoutes.AddSubject) },
                    onSeeAllSubjectsClick = { navController.navigate(AppRoutes.Grades) },
                    onSeeExpensesClick = { navController.navigate(AppRoutes.Expenses) },
                    onSubjectClick = { subjectId -> navController.navigate(AppRoutes.subjectDetail(subjectId)) }
                )
            }
            composable(AppRoutes.Grades) {
                GradesScreen(
                    onAddSubjectClick = { navController.navigate(AppRoutes.AddSubject) },
                    onOpenSimulatorClick = { navController.navigate(AppRoutes.GradeSimulator) },
                    onSubjectClick = { subjectId -> navController.navigate(AppRoutes.subjectDetail(subjectId)) }
                )
            }
            composable(AppRoutes.Tasks) {
                TasksScreen(onNewTaskClick = { navController.navigate(AppRoutes.AddTask) })
            }
            composable(AppRoutes.Profile) {
                ProfileScreen()
            }
            composable(AppRoutes.Expenses) {
                ExpensesScreen(onAddExpenseClick = { navController.navigate(AppRoutes.AddExpense) })
            }
            composable(AppRoutes.AddSubject) {
                AddSubjectScreen(
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.navigate(AppRoutes.Home)
                        }
                    },
                    onSubjectCreated = { subjectId ->
                        navController.navigate(AppRoutes.subjectDetail(subjectId)) {
                            popUpTo(AppRoutes.AddSubject) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable("${AppRoutes.SubjectDetail}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                SubjectDetailScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.navigate(AppRoutes.Grades)
                        }
                    },
                    onAddGradeClick = { id -> navController.navigate(AppRoutes.addGrade(id)) }
                )
            }
            composable("${AppRoutes.AddGrade}/{subjectId}") { backStackEntry ->
                val subjectId = backStackEntry.arguments?.getString("subjectId").orEmpty()
                AddGradeScreen(
                    subjectId = subjectId,
                    onBackClick = {
                        if (!navController.navigateUp()) {
                            navController.navigate(AppRoutes.subjectDetail(subjectId))
                        }
                    }
                )
            }
            composable(AppRoutes.AddTask) {
                AddTaskScreen(onBackClick = { navController.navigateUp() })
            }
            composable(AppRoutes.AddExpense) {
                AddExpenseScreen(onBackClick = { navController.navigateUp() })
            }
            composable(AppRoutes.GradeSimulator) {
                GradeSimulatorScreen(onBackClick = { navController.navigateUp() })
            }
        }
    }
}

@Composable
private fun UniStackBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        shape = AppShapes.BottomBar,
        color = Color(0xFFFFFCFF),
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
            BottomNavItem.items.forEach { item ->
                val selected = currentRoute == item.route
                val pillColor by animateColorAsState(if (selected) Color(0xFFF0EAFF) else Color.Transparent, label = "pill")
                val contentColor by animateColorAsState(if (selected) UniStackColors.Primary else UniStackColors.TextPrimary, label = "content")
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(AppShapes.Pill)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.route) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .height(31.dp)
                            .fillMaxWidth(0.68f)
                            .clip(AppShapes.Pill)
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
