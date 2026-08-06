package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.unistack.app.core.design.components.UniStackFabMenu
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.notifications.NotificationHistoryStore
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_home.domain.AcademicWorkSummary
import com.unistack.app.feature_home.domain.DailyFocusItem
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomePrioritySummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.HomeTimelineKind
import com.unistack.app.feature_home.domain.HomeTimelineState
import com.unistack.app.feature_home.domain.HomeTimelineSummary
import com.unistack.app.feature_home.domain.SubjectRiskSeverity
import com.unistack.app.feature_home.domain.SubjectRiskSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.HomeSection
import com.unistack.app.feature_user.domain.InterfaceDensity
import java.time.LocalTime
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddSubjectClick: () -> Unit,
    onSeeAllSubjectsClick: () -> Unit,
    onSeeTasksClick: () -> Unit,
    onSeeExpensesClick: () -> Unit,
    onOpenTemplatesClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onAddGradeClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCalendarClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onDataClick: () -> Unit = {},
    onDrawerOpenChange: (Boolean) -> Unit = {}
) {
    val summary = uiState.summary
    val appearance = LocalAppearancePreferences.current
    val context = LocalContext.current
    val notifications by remember(context) {
        NotificationHistoryStore.observe(context)
    }.collectAsStateWithLifecycle()
    val displayName = summary.userName.takeIf { it.isNotBlank() } ?: "Pineda"
    var showPriorityDetails by rememberSaveable { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()
    val closeDrawer: () -> Unit = {
        drawerScope.launch {
            drawerState.close()
            onDrawerOpenChange(false)
        }
        Unit
    }
    val closeDrawerAndRun: (() -> Unit) -> Unit = { action ->
        onDrawerOpenChange(false)
        drawerScope.launch { drawerState.close() }
        action()
    }
    val openDrawer: () -> Unit = {
        onDrawerOpenChange(true)
        drawerScope.launch { drawerState.open() }
        Unit
    }
    LaunchedEffect(drawerState.isOpen) {
        onDrawerOpenChange(drawerState.isOpen)
    }
    val priorityActionLabel = summary.priority.action.actionLabel()
    val openPriorityAction = {
        when (summary.priority.action) {
            HomePriorityAction.SUBJECT -> summary.priority.subjectId?.let(onSubjectClick) ?: onSeeAllSubjectsClick()
            HomePriorityAction.SUBJECTS -> onSeeAllSubjectsClick()
            HomePriorityAction.TASKS -> onSeeTasksClick()
            HomePriorityAction.EXPENSES -> onSeeExpensesClick()
            HomePriorityAction.TEMPLATES -> onOpenTemplatesClick()
            HomePriorityAction.SCHEDULE -> onCalendarClick()
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeNavigationPanel(
                displayName = displayName,
                subjectsCount = summary.subjectsCount,
                onClose = closeDrawer,
                onSemesterClick = { closeDrawerAndRun(onSeeAllSubjectsClick) },
                onWorksClick = { closeDrawerAndRun(onOpenTemplatesClick) },
                onTasksClick = { closeDrawerAndRun(onSeeTasksClick) },
                onNotificationsClick = { closeDrawerAndRun(onNotificationsClick) },
                onDataClick = { closeDrawerAndRun(onDataClick) },
                onSettingsClick = { closeDrawerAndRun(onSettingsClick) },
                onProfileClick = { closeDrawerAndRun(onProfileClick) }
            )
        }
    ) {
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(HomeBackgroundBrush)
        ) {
            val viewportIsCompact = maxHeight < 840.dp || maxWidth < 390.dp
            val isCompact = when (appearance.interfaceDensity) {
                InterfaceDensity.COMPACT -> true
                InterfaceDensity.BALANCED -> viewportIsCompact
                InterfaceDensity.COMFORTABLE -> maxHeight < 760.dp || maxWidth < 350.dp
            }
            val sidePadding = if (isCompact) 20.dp else 22.dp
            val sectionSpacing = if (isCompact) 15.dp else 18.dp

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(
                    start = sidePadding,
                    end = sidePadding,
                    top = if (isCompact) 6.dp else 8.dp,
                    bottom = 126.dp
                ),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing)
            ) {
                item {
                    HomeHeader(
                        photoUrl = summary.avatarPhotoUrl,
                        unreadNotificationCount = notifications.count { !it.read },
                        onMenuClick = openDrawer,
                        onCalendarClick = onCalendarClick,
                        onNotificationsClick = onNotificationsClick,
                        onProfileClick = onProfileClick
                    )
                }
                if (appearance.showHomeGreeting) {
                    item {
                        HomeGreeting(name = displayName, compact = isCompact)
                    }
                }
                appearance.homeSectionOrder.forEach { section ->
                    when (section) {
                        HomeSection.HERO -> if (appearance.showHomeHero) {
                            item {
                                PriorityHero(
                                    title = summary.priority.title,
                                    description = summary.priority.shortDescription,
                                    actionLabel = priorityActionLabel,
                                    compact = isCompact,
                                    onOpenClick = openPriorityAction,
                                    onDetailsClick = { showPriorityDetails = true }
                                )
                            }
                        }
                        HomeSection.AGENDA -> if (appearance.showHomeAgenda) {
                            item {
                                TodayAgenda(
                                    summary = summary,
                                    compact = isCompact,
                                    onTasksClick = onSeeTasksClick
                                )
                            }
                        }
                        HomeSection.SNAPSHOT -> if (appearance.showHomeSnapshot) {
                            item {
                                SemesterSnapshot(
                                    summary = summary,
                                    compact = isCompact,
                                    onSubjectsClick = onSeeAllSubjectsClick,
                                    onTasksClick = onSeeTasksClick,
                                    onExpensesClick = onSeeExpensesClick,
                                    onWorksClick = onOpenTemplatesClick
                                )
                            }
                        }
                    }
                }
            }

            UniStackFabMenu(
                onAddGradeClick = onAddGradeClick,
                onAddTaskClick = onAddTaskClick,
                onAddExpenseClick = onAddExpenseClick,
                onAddSubjectClick = onAddSubjectClick,
                showAddGrade = true,
                showAddTask = true,
                showAddExpense = true,
                showAddSubject = false,
                expandedEndPadding = 24.dp,
                expandedBottomPadding = 12.dp
            )

            if (showPriorityDetails) {
                PriorityContextSheet(
                    priority = summary.priority,
                    actionLabel = priorityActionLabel,
                    onActionClick = {
                        showPriorityDetails = false
                        openPriorityAction()
                    },
                    onDismiss = { showPriorityDetails = false }
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    photoUrl: String?,
    unreadNotificationCount: Int,
    onMenuClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        HeaderIcon(
            icon = Icons.Rounded.Menu,
            onClick = onMenuClick,
            contentDescription = "Menú",
            modifier = Modifier.align(Alignment.CenterStart)
        )
        Text(
            text = "UniStack",
            color = HomePurple,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            modifier = Modifier.align(Alignment.Center)
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                HeaderIcon(
                    icon = Icons.Rounded.NotificationsNone,
                    contentDescription = "Notificaciones",
                    onClick = onNotificationsClick
                )
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .offset(x = (-5).dp, y = 4.dp)
                            .size(if (unreadNotificationCount > 9) 17.dp else 14.dp)
                            .clip(CircleShape)
                            .background(HomePurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadNotificationCount.coerceAtMost(9).toString(),
                            color = Color.White,
                            fontSize = 8.sp,
                            lineHeight = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(HomeAvatarPurpleTop, HomeAvatarPurpleBottom)))
                    .cleanClickable(onProfileClick),
                contentAlignment = Alignment.Center
            ) {
                if (photoUrl.isNullOrBlank()) {
                    Text("P", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                } else {
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val clickModifier = if (onClick != null) modifier.cleanClickable(onClick) else modifier
    Box(modifier = clickModifier.size(38.dp), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = HomeText,
            modifier = Modifier.size(23.dp)
        )
    }
}

@Composable
private fun HomeGreeting(
    name: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val greeting = remember { currentHomeGreeting() }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp)
    ) {
        Text(
            text = "$greeting, $name! 👋",
            color = HomeText,
            fontSize = if (compact) 28.sp else 30.sp,
            lineHeight = if (compact) 32.sp else 35.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "¿Qué vamos a lograr hoy?",
            color = HomeMuted,
            fontSize = if (compact) 14.sp else 15.sp,
            lineHeight = if (compact) 18.sp else 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun currentHomeGreeting(): String {
    return when (LocalTime.now().hour) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

internal fun Modifier.cleanClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Preview(name = "Home Android modern", widthDp = 412, heightDp = 892, showBackground = true)
@Composable
private fun HomePreview412() {
    UniStackTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeUiState(summary = previewHomeSummary),
            onAddSubjectClick = {},
            onSeeAllSubjectsClick = {},
            onSeeTasksClick = {},
            onSeeExpensesClick = {},
            onOpenTemplatesClick = {},
            onSubjectClick = {},
            onProfileClick = {},
            onAddGradeClick = {},
            onAddTaskClick = {},
            onAddExpenseClick = {}
        )
    }
}

private val previewHomeSummary = HomeSummary(
    userName = "Pineda",
    avatarPhotoUrl = null,
    dashboardMessage = "Inglés necesita atención.",
    priority = HomePrioritySummary(
        title = "Inglés necesita atención",
        shortDescription = "Repasa esta materia antes de abrir más frentes.",
        fullDescription = "Inglés necesita atención académica. Revisa tus apuntes antes de entrar y prioriza lo que más peso tenga en la materia.",
        suggestion = "Siguiente paso: repasar Inglés 15 minutos y dejar lista una nota corta.",
        action = HomePriorityAction.SUBJECT,
        subjectId = "english"
    ),
    dailyFocusItems = listOf(
        DailyFocusItem(
            slotLabel = "Ahora",
            title = "Inglés necesita atención",
            detail = "Repasa esta materia antes de abrir más frentes.",
            minutesText = "15 min",
            actionLabel = "Abrir",
            action = HomePriorityAction.SUBJECT,
            subjectId = "english"
        ),
        DailyFocusItem(
            slotLabel = "Luego",
            title = "Entrega de proyecto",
            detail = "Avanza un paso del checklist.",
            minutesText = "20 min",
            actionLabel = "Trabajos",
            action = HomePriorityAction.TEMPLATES
        )
    ),
    generalAverage = 3.8,
    subjectsCount = 1,
    tasksToday = 0,
    overdueTasks = 0,
    pendingTasks = 0,
    openAcademicWorks = 1,
    subjects = listOf(
        SubjectSummary(
            id = "english",
            name = "Inglés",
            average = 3.7,
            targetAverage = 4.2,
            progress = 0.64f,
            type = SubjectVisualType.PURPLE
        )
    ),
    riskSubject = SubjectRiskSummary(
        subjectId = "english",
        subjectName = "Inglés",
        detail = "Revisa tus apuntes antes de entrar.",
        severity = SubjectRiskSeverity.ATTENTION
    ),
    neededGrade = null,
    nextTask = null,
    nextAcademicWork = AcademicWorkSummary(
        id = "project",
        subjectId = "math",
        title = "Entrega de proyecto",
        dueText = "hoy",
        progress = 0.4f
    ),
    todayItems = listOf(
        HomeTimelineSummary("Hoy", "Clase de Inglés", "Revisa apuntes antes de entrar", HomeTimelineKind.CLASS, HomeTimelineState.CURRENT),
        HomeTimelineSummary("Hoy", "Entrega de proyecto", "Matemáticas · 40% listo", HomeTimelineKind.WORK, HomeTimelineState.PENDING),
        HomeTimelineSummary("Mañana", "Examen parcial", "Física · 1 h", HomeTimelineKind.EXAM, HomeTimelineState.PENDING)
    ),
    weeklyExpenses = ExpenseSummary(
        transport = 0,
        food = 0,
        total = 0,
        chartValues = emptyList()
    ),
    weeklyExpenseTotal = 0,
    productivitySummary = "Vas bien, Pineda.",
    companionInsight = "Hoy conviene enfocarte en Inglés antes de abrir más frentes.",
    gradingScale = GradingScale.ZERO_TO_FIVE,
    enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES, AppModule.ACADEMIC_TEMPLATES)
)
