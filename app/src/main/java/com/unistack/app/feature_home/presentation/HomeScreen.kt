package com.unistack.app.feature_home.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unistack.app.R
import com.unistack.app.core.design.components.UniStackFabMenu
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.notifications.NotificationHistoryStore
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.GradingScaleUtils
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalTime

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
    onNotificationsClick: () -> Unit = {}
) {
    val summary = uiState.summary
    val context = LocalContext.current
    val notifications by remember(context) {
        NotificationHistoryStore.observe(context)
    }.collectAsStateWithLifecycle()
    val displayName = summary.userName.takeIf { it.isNotBlank() } ?: "Pineda"
    var showPriorityDetails by rememberSaveable { mutableStateOf(false) }
    val priorityActionLabel = summary.priority.action.actionLabel()
    val openPriorityAction = {
        when (summary.priority.action) {
            HomePriorityAction.SUBJECT -> summary.priority.subjectId?.let(onSubjectClick) ?: onSeeAllSubjectsClick()
            HomePriorityAction.SUBJECTS -> onSeeAllSubjectsClick()
            HomePriorityAction.TASKS -> onSeeTasksClick()
            HomePriorityAction.EXPENSES -> onSeeExpensesClick()
            HomePriorityAction.TEMPLATES -> onOpenTemplatesClick()
        }
    }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBackgroundBrush)
    ) {
        val isCompact = maxHeight < 840.dp || maxWidth < 390.dp
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
                    onNotificationsClick = onNotificationsClick,
                    onProfileClick = onProfileClick
                )
            }
            item {
                HomeGreeting(name = displayName, compact = isCompact)
            }
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
            item {
                TodayAgenda(
                    summary = summary,
                    compact = isCompact,
                    onTasksClick = onSeeTasksClick
                )
            }
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

@Composable
private fun HomeHeader(
    photoUrl: String?,
    unreadNotificationCount: Int,
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
            text = "$greeting, $name! \uD83D\uDC4B",
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

@Composable
private fun TodayAgenda(
    summary: HomeSummary,
    compact: Boolean,
    onTasksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timelineItems = summary.todayItems.take(3)
    if (summary.subjectsCount == 0 && timelineItems.isEmpty()) return

    val countText = when {
        timelineItems.isNotEmpty() -> "${timelineItems.size} acción${if (timelineItems.size == 1) "" else "es"}"
        else -> "Sin pendientes"
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Agenda", color = HomeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(countText, color = HomeMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            color = HomeCard,
            border = BorderStroke(1.dp, HomeBorder)
        ) {
            Column(modifier = Modifier.padding(vertical = if (compact) 6.dp else 8.dp)) {
                when {
                    timelineItems.isNotEmpty() -> {
                        timelineItems.forEachIndexed { index, item ->
                            AgendaTimelineRow(item = item.toTimelineItem(), compact = compact, onClick = onTasksClick)
                            if (index < timelineItems.lastIndex) AgendaDivider()
                        }
                    }
                    else -> TimelineEmptyRow(compact = compact)
                }
            }
        }
    }
}

@Composable
private fun AgendaTimelineRow(
    item: TimelineItem,
    compact: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable(onClick)
            .padding(
                horizontal = if (compact) 14.dp else 16.dp,
                vertical = if (compact) 10.dp else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgendaIcon(icon = item.icon, accent = item.accent, compact = compact)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.time, color = item.accent, fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                item.title,
                color = HomeText,
                fontSize = if (compact) 13.sp else 14.sp,
                lineHeight = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.subtitle,
                color = HomeMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = HomeMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AgendaFocusRow(
    item: DailyFocusItem,
    compact: Boolean,
    onClick: () -> Unit
) {
    val accent = item.action.agendaAccent()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable(onClick)
            .padding(
                horizontal = if (compact) 14.dp else 16.dp,
                vertical = if (compact) 10.dp else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgendaIcon(icon = item.action.focusIcon(), accent = accent, compact = compact)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.slotLabel, color = accent, fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                item.title,
                color = HomeText,
                fontSize = if (compact) 13.sp else 14.sp,
                lineHeight = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.detail,
                color = HomeMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(item.minutesText, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AgendaIcon(
    icon: ImageVector,
    accent: Color,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .size(if (compact) 34.dp else 38.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(if (compact) 18.dp else 20.dp))
    }
}

@Composable
private fun AgendaDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 63.dp, end = 14.dp)
            .height(1.dp)
            .background(HomeBorder)
    )
}

@Composable
private fun SemesterSnapshot(
    summary: HomeSummary,
    compact: Boolean,
    onSubjectsClick: () -> Unit,
    onTasksClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onWorksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val averageText = summary.generalAverage
        ?.let { GradingScaleUtils.formatGrade(it, summary.gradingScale) }
        ?: "Sin notas"
    val pendingText = when {
        summary.overdueTasks > 0 -> "${summary.overdueTasks} vencida${if (summary.overdueTasks == 1) "" else "s"}"
        summary.tasksToday > 0 -> "${summary.tasksToday} hoy"
        else -> "${summary.pendingTasks} pendiente${if (summary.pendingTasks == 1) "" else "s"}"
    }
    val worksText = when {
        summary.openAcademicWorks > 0 -> "${summary.openAcademicWorks} trabajo${if (summary.openAcademicWorks == 1) "" else "s"}"
        summary.nextAcademicWork != null -> "1 proximo"
        else -> "Sin trabajos"
    }
    val moneyText = when {
        AppModule.EXPENSES !in summary.enabledModules -> "Inactivo"
        summary.weeklyExpenseTotal > 0 -> CurrencyFormatter.formatCop(summary.weeklyExpenseTotal)
        else -> "Sin gastos"
    }
    val showWorks = AppModule.ACADEMIC_TEMPLATES in summary.enabledModules &&
        (summary.openAcademicWorks > 0 || summary.nextAcademicWork != null)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Tu tablero", color = HomeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("Actualizado", color = HomeMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            color = HomeCard,
            border = BorderStroke(1.dp, HomeBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = if (compact) 13.dp else 15.dp, vertical = if (compact) 13.dp else 15.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    SnapshotMetric(
                        label = "Materias",
                        value = summary.subjectsCount.toString(),
                        detail = "registradas",
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        accent = HomePurple,
                        compact = compact,
                        onClick = onSubjectsClick,
                        modifier = Modifier.weight(1f)
                    )
                    SnapshotMetric(
                        label = "Promedio",
                        value = averageText,
                        detail = summary.neededGrade?.let { "meta ${GradingScaleUtils.formatGrade(it.targetAverage, summary.gradingScale)}" }
                            ?: "general",
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        accent = HomeTeal,
                        compact = compact,
                        onClick = onSubjectsClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    SnapshotMetric(
                        label = "Hoy",
                        value = pendingText,
                        detail = "tareas",
                        icon = Icons.AutoMirrored.Rounded.EventNote,
                        accent = HomeYellow,
                        compact = compact,
                        onClick = onTasksClick,
                        modifier = Modifier.weight(1f)
                    )
                    SnapshotMetric(
                        label = if (showWorks) "Trabajos" else "Gastos",
                        value = if (showWorks) worksText else moneyText,
                        detail = if (showWorks) "academicos" else "semana",
                        icon = if (showWorks) Icons.Rounded.Description else Icons.Rounded.Wallet,
                        accent = HomeCoral,
                        compact = compact,
                        onClick = if (showWorks) onWorksClick else onExpensesClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SnapshotMetric(
    label: String,
    value: String,
    detail: String,
    icon: ImageVector,
    accent: Color,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = if (UniStackColors.IsDarkTheme) 0.08f else 0.07f))
            .cleanClickable(onClick)
            .padding(horizontal = if (compact) 11.dp else 12.dp, vertical = if (compact) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 30.dp else 34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.17f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(if (compact) 16.dp else 18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, color = HomeMuted, fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                value,
                color = HomeText,
                fontSize = if (compact) 13.sp else 14.sp,
                lineHeight = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(detail, color = HomeSoftText, fontSize = 9.sp, lineHeight = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PriorityHero(
    title: String,
    description: String,
    actionLabel: String,
    compact: Boolean,
    onOpenClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroLabel = title.heroLabel()
    val heroHeight = if (compact) 174.dp else 190.dp
    val heroPadding = if (compact) 16.dp else 18.dp
    val isDarkTheme = UniStackColors.IsDarkTheme
    val heroStar = HomeHeroStar
    val heroStarSoft = HomeHeroStarSoft
    val heroAssetShadow = HomeHeroAssetShadow
    val sparkleMotion = rememberInfiniteTransition(label = "heroSparkleMotion")
    val sparkleOneFloat by sparkleMotion.animateFloat(
        initialValue = 2f,
        targetValue = -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleOneFloat"
    )
    val sparkleTwoFloat by sparkleMotion.animateFloat(
        initialValue = -1f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleTwoFloat"
    )
    val sparkleThreeFloat by sparkleMotion.animateFloat(
        initialValue = 1f,
        targetValue = -2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleThreeFloat"
    )
    val sparkleOneAlpha by sparkleMotion.animateFloat(
        initialValue = 0.78f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleOneAlpha"
    )
    val sparkleTwoAlpha by sparkleMotion.animateFloat(
        initialValue = 1f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleTwoAlpha"
    )
    val sparkleThreeAlpha by sparkleMotion.animateFloat(
        initialValue = 0.70f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleThreeAlpha"
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight)
            .cleanClickable(onDetailsClick),
        shape = RoundedCornerShape(19.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, HomeHeroStroke),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HeroBrush)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                HomeHeroLightViolet.copy(alpha = 0.24f),
                                HomeHeroVioletWash.copy(alpha = 0.20f),
                                HomeHeroVioletDepth.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                HomeHeroLightModeGlow.copy(alpha = 0.70f),
                                HomeHeroLightModeAccent.copy(alpha = 0.34f),
                                Color.Transparent
                            )
                        },
                        center = Offset(size.width * 0.79f, size.height * 0.52f),
                        radius = size.width * if (isDarkTheme) 0.42f else 0.52f
                    )
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                Color.Transparent,
                                HomeHeroTransition.copy(alpha = 0.10f),
                                HomeHeroVioletDepth.copy(alpha = 0.18f)
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                HomeHeroLightModeAccent.copy(alpha = 0.24f),
                                HomeHeroLightModeDepth.copy(alpha = 0.18f)
                            )
                        },
                        start = Offset(size.width * 0.36f, size.height * 0.16f),
                        end = Offset(size.width * 1.04f, size.height * 0.88f)
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                HomeHeroLight.copy(alpha = 0.020f),
                                Color.Transparent,
                                HomeShadow.copy(alpha = 0.24f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.64f),
                                Color.Transparent,
                                HomeHeroLightModeDepth.copy(alpha = 0.12f)
                            )
                        }
                    )
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                HomeShadow.copy(alpha = 0.30f),
                                Color.Transparent,
                                HomeShadow.copy(alpha = 0.18f)
                            )
                        } else {
                            listOf(
                                HomeHeroLightModeDepth.copy(alpha = 0.08f),
                                Color.Transparent,
                                HomeHeroLightModeAccent.copy(alpha = 0.12f)
                            )
                        }
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            HomeShadow.copy(alpha = if (isDarkTheme) 0.08f else 0.02f),
                            Color.Transparent,
                            HomeShadow.copy(alpha = if (isDarkTheme) 0.22f else 0.04f)
                        )
                    )
                )
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            heroAssetShadow.copy(alpha = if (isDarkTheme) 0.46f else 0.22f),
                            HomeHeroVioletDepth.copy(alpha = if (isDarkTheme) 0.16f else 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.80f, size.height * 0.77f),
                        radius = size.width * 0.24f
                    ),
                    topLeft = Offset(size.width * 0.64f, size.height * 0.68f),
                    size = Size(size.width * 0.32f, size.height * 0.18f)
                )
                drawSoftSparkle(
                    center = Offset(size.width * 0.55f, size.height * 0.37f + sparkleOneFloat.dp.toPx()),
                    radius = size.minDimension * 0.022f,
                    color = heroStar,
                    alpha = (if (isDarkTheme) 0.54f else 0.40f) * sparkleOneAlpha
                )
                drawSoftSparkle(
                    center = Offset(size.width * 0.88f, size.height * 0.27f + sparkleTwoFloat.dp.toPx()),
                    radius = size.minDimension * 0.030f,
                    color = heroStarSoft,
                    alpha = (if (isDarkTheme) 0.48f else 0.34f) * sparkleTwoAlpha
                )
                drawSoftSparkle(
                    center = Offset(size.width * 0.68f, size.height * 0.24f + sparkleThreeFloat.dp.toPx()),
                    radius = size.minDimension * 0.014f,
                    color = heroStar,
                    alpha = (if (isDarkTheme) 0.38f else 0.26f) * sparkleThreeAlpha
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(heroPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.56f)
                        .align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PremiumSparkle(tint = HomeHeroStar, modifier = Modifier.size(13.dp))
                        Text(
                            text = heroLabel,
                            color = HomeHeroLabel,
                            fontSize = 8.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        )
                    }
                    Text(
                        text = title,
                        color = HomeHeroTitle,
                        fontSize = if (compact) 20.sp else 22.sp,
                        lineHeight = if (compact) 26.sp else 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = description,
                        color = HomeHeroSecondary,
                        fontSize = if (compact) 10.sp else 11.sp,
                        lineHeight = if (compact) 15.sp else 16.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.cleanClickable(onDetailsClick)
                    )
                    Box(
                        modifier = Modifier
                            .width(if (compact) 128.dp else 138.dp)
                            .height(if (compact) 32.dp else 34.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(listOf(HomeHeroButtonStart, HomeHeroButtonEnd)))
                            .cleanClickable(onOpenClick)
                            .padding(horizontal = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                actionLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                Image(
                    painter = painterResource(R.drawable.hero_notebook_pen),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(if (compact) 144.dp else 160.dp)
                        .height(if (compact) 154.dp else 176.dp)
                        .offset(x = if (compact) 20.dp else 24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityContextSheet(
    priority: HomePrioritySummary,
    actionLabel: String,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val bodyParagraphs = priority.sheetBodyParagraphs()
    val suggestionText = priority.sheetSuggestionText()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = HomePrioritySheetSurface,
        contentColor = HomePrioritySheetText,
        scrimColor = Color.Black.copy(alpha = 0.64f),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 42.dp, height = 4.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(100.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 30.dp, end = 30.dp, bottom = 42.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrioritySunBadge()
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = priority.title,
                        color = HomePrioritySheetText,
                        fontSize = 25.sp,
                        lineHeight = 29.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "✦ Tu prioridad de hoy",
                        color = HomePrioritySheetAccentSoft,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                bodyParagraphs.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        color = HomePrioritySheetBody,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HomePrioritySheetSuggestion, RoundedCornerShape(18.dp))
                    .border(
                        width = 0.7.dp,
                        color = HomePrioritySheetCardBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                PriorityBulbBadge()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Sugerencia",
                        color = HomePrioritySheetAccentSoft,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = suggestionText,
                        color = HomePrioritySheetBody,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePrioritySheetSecondaryButton,
                        contentColor = HomePrioritySheetText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Cerrar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePurple,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(actionLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PrioritySunBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(HomePrioritySheetIconCircle, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(32.dp)) {
            val center = this.center
            val rayStart = size.minDimension * 0.35f
            val rayEnd = size.minDimension * 0.48f
            drawCircle(
                color = HomePrioritySheetSun,
                radius = size.minDimension * 0.20f,
                center = center
            )
            repeat(8) { index ->
                val angle = Math.toRadians((index * 45).toDouble())
                val start = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * rayStart,
                    y = center.y + kotlin.math.sin(angle).toFloat() * rayStart
                )
                val end = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * rayEnd,
                    y = center.y + kotlin.math.sin(angle).toFloat() * rayEnd
                )
                drawLine(
                    color = HomePrioritySheetSun,
                    start = start,
                    end = end,
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun PriorityBulbBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .background(HomePrioritySheetIconCircle, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Lightbulb,
            contentDescription = null,
            tint = HomePrioritySheetAccentSoft,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun TimelineEmptyRow(compact: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 76.dp else 86.dp)
            .padding(horizontal = if (compact) 14.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 30.dp else 34.dp)
                .clip(CircleShape)
                .background(HomePurple.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = HomePurple, modifier = Modifier.size(if (compact) 16.dp else 18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Día despejado", color = HomeText, fontSize = if (compact) 12.sp else 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Sin vencimientos cercanos por ahora", color = HomeMuted, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

private data class TimelineItem(
    val time: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val active: Boolean
)

@Composable
private fun HomeTimelineSummary.toTimelineItem(): TimelineItem {
    return TimelineItem(
        time = timeText,
        title = title,
        subtitle = subtitle,
        icon = when (kind) {
            HomeTimelineKind.CLASS -> Icons.Rounded.CalendarMonth
            HomeTimelineKind.TASK -> Icons.AutoMirrored.Rounded.EventNote
            HomeTimelineKind.WORK -> Icons.Rounded.Description
            HomeTimelineKind.EXAM -> Icons.AutoMirrored.Rounded.Assignment
            HomeTimelineKind.FOCUS -> Icons.Rounded.Star
        },
        accent = when (kind) {
            HomeTimelineKind.CLASS -> HomePurple
            HomeTimelineKind.TASK -> HomeTeal
            HomeTimelineKind.WORK -> HomeYellow
            HomeTimelineKind.EXAM -> HomeCoral
            HomeTimelineKind.FOCUS -> HomePurple
        },
        active = state == HomeTimelineState.CURRENT
    )
}

private fun Modifier.cleanClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@Composable
private fun PremiumSparkle(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawSoftSparkle(
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.minDimension * 0.42f,
            color = tint,
            alpha = 0.92f
        )
    }
}

private fun DrawScope.drawSoftSparkle(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.22f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 2.25f
        ),
        radius = radius * 2.25f,
        center = center
    )

    val sparkle = Path().apply {
        moveTo(center.x, center.y - radius)
        cubicTo(
            center.x + radius * 0.14f,
            center.y - radius * 0.28f,
            center.x + radius * 0.28f,
            center.y - radius * 0.14f,
            center.x + radius,
            center.y
        )
        cubicTo(
            center.x + radius * 0.28f,
            center.y + radius * 0.14f,
            center.x + radius * 0.14f,
            center.y + radius * 0.28f,
            center.x,
            center.y + radius
        )
        cubicTo(
            center.x - radius * 0.14f,
            center.y + radius * 0.28f,
            center.x - radius * 0.28f,
            center.y + radius * 0.14f,
            center.x - radius,
            center.y
        )
        cubicTo(
            center.x - radius * 0.28f,
            center.y - radius * 0.14f,
            center.x - radius * 0.14f,
            center.y - radius * 0.28f,
            center.x,
            center.y - radius
        )
        close()
    }
    drawPath(sparkle, color.copy(alpha = alpha))
    drawCircle(color = Color.White.copy(alpha = alpha * 0.18f), radius = radius * 0.16f, center = center)
}

private val HomeBackgroundBrush: Brush
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        androidx.compose.ui.graphics.SolidColor(UniStackColors.Background)
    } else {
        Brush.linearGradient(listOf(Color(0xFFF8F6FF), Color(0xFFFFFFFF)))
    }

private val HeroBrush: Brush
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        Brush.linearGradient(
            listOf(
                HomeHeroStart,
                HomeHeroMid,
                HomeHeroTransition,
                HomeHeroEnd
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                HomeHeroLightModeStart,
                HomeHeroLightModeMid,
                HomeHeroLightModeTransition,
                HomeHeroLightModeEnd
            )
        )
    }

private val HomeCard: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeCardDark.copy(alpha = 0.96f) else Color.White
private val HomeText: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeTextPrimary else Color(0xFF171427)
private val HomeSoftText: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeTextSoft else Color(0xFF575269)
private val HomeMuted: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeTextMuted else Color(0xFF6B6578)
private val HomeBorder: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeStroke else Color.Black.copy(alpha = 0.07f)
private val HomePurple: Color
    @Composable get() = HomeAccentPurple
private val HomeTeal: Color
    @Composable get() = HomeAccentTeal
private val HomeCoral: Color
    @Composable get() = HomeAccentCoral
private val HomeYellow: Color
    @Composable get() = HomeAccentYellow

private val HomeBgTop = Color(0xFF01040B)
private val HomeBgMid = Color(0xFF01040B)
private val HomeBgBottom = Color(0xFF000309)
private val HomeCardDark = Color(0xFF080D17)
private val HomeHeroStart = Color(0xFF09051A)
private val HomeHeroMid = Color(0xFF0E0228)
private val HomeHeroEnd = Color(0xFF06041C)
private val HomeHeroTransition = Color(0xFF1A0A48)
private val HomeHeroVioletDepth = Color(0xFF2A0E72)
private val HomeHeroVioletWash = Color(0xFF6D28FF)
private val HomeHeroLightViolet = Color(0xFF8A5FFF)
private val HomeHeroTitle: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFF4F3FF) else Color(0xFF1C1530)
private val HomeHeroSecondary: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFB8BDD0) else Color(0xFF5F5B73)
private val HomeHeroLabel: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA78BFA) else Color(0xFF7C3AED)
private val HomeHeroStar: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA78BFA) else Color(0xFF7C3AED)
private val HomeHeroStarSoft: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFC4B5FD) else Color(0xFF9B6CFF)
private val HomeHeroStroke: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        Color(0xFFA78BFA).copy(alpha = 0.13f)
    } else {
        Color(0xFFBDA8FF).copy(alpha = 0.54f)
    }
private val HomeHeroAssetShadow: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF090018) else Color(0xFF7655D8)
private val HomeHeroButtonStart = Color(0xFF581DD6)
private val HomeHeroButtonEnd = Color(0xFF8A5FFF)
private val HomeAccentPurple = Color(0xFF8F35FF)
private val HomeAvatarPurpleTop = Color(0xFF9A42FF)
private val HomeAvatarPurpleBottom = Color(0xFF6E22FF)
private val HomeAccentTeal = Color(0xFF00E0B8)
private val HomeAccentCoral = Color(0xFFFF3348)
private val HomeAccentYellow = Color(0xFFFFB800)
private val HomeTextPrimary = Color(0xFFF8F4FF)
private val HomeTextSoft = Color(0xFFD3D0E0)
private val HomeTextMuted = Color(0xFFA7ADBE)
private val HomeStroke = Color(0xFF1A2230)
private val HomeHeroLight = Color(0xFFFFFFFF)
private val HomeShadow = Color(0xFF000000)
private val HomeHeroLightModeStart = Color(0xFFFFFEFF)
private val HomeHeroLightModeMid = Color(0xFFF6F0FF)
private val HomeHeroLightModeTransition = Color(0xFFEDE3FF)
private val HomeHeroLightModeEnd = Color(0xFFF8F4FF)
private val HomeHeroLightModeGlow = Color(0xFFD9C7FF)
private val HomeHeroLightModeAccent = Color(0xFFB892FF)
private val HomeHeroLightModeDepth = Color(0xFF8E6AE8)
private val HomeCompanionHeart = Color(0xFFC08CFF)
private val HomePrioritySheetSurface: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) UniStackColors.Background else Color(0xFFFBFAFF)
private val HomePrioritySheetSuggestion: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF15182B) else Color(0xFFF2ECFF)
private val HomePrioritySheetText: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFF4F3FF) else Color(0xFF171427)
private val HomePrioritySheetBody: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFCDD2E3) else Color(0xFF555267)
private val HomePrioritySheetMuted: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF9EA6BA) else Color(0xFF747186)
private val HomePrioritySheetSecondaryButton: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF202232) else Color(0xFFECEAF4)
private val HomePrioritySheetIconCircle: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF201044) else Color(0xFFEDE4FF)
private val HomePrioritySheetCardBorder: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color.White.copy(alpha = 0.07f) else Color(0xFF7C3AED).copy(alpha = 0.14f)
private val HomePrioritySheetSun = Color(0xFFFFD21F)
private val HomePrioritySheetAccentSoft: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA855F7) else Color(0xFF8B35E8)

private fun HomePriorityAction.actionLabel(): String {
    return when (this) {
        HomePriorityAction.SUBJECT -> "Abrir materia"
        HomePriorityAction.SUBJECTS -> "Ver materias"
        HomePriorityAction.TASKS -> "Ver mis tareas"
        HomePriorityAction.EXPENSES -> "Ver gastos"
        HomePriorityAction.TEMPLATES -> "Ver trabajos"
    }
}

private fun currentHomeGreeting(): String {
    return when (LocalTime.now().hour) {
        in 5..11 -> "Buenos días"
        in 12..18 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}

private fun String.heroLabel(): String {
    val normalized = lowercase()
    return when {
        "espera su nota" in normalized || "resultados esperan" in normalized -> "RESULTADO PENDIENTE"
        "historial" in normalized -> "DATOS POR COMPLETAR"
        "ajusta" in normalized -> "PROYECCIÓN"
        "venc" in normalized || "necesita" in normalized || "sobre el límite" in normalized -> "ALERTA"
        "cerca" in normalized || "atención" in normalized || "limite" in normalized -> "ENFOQUE"
        "gasto" in normalized -> "FINANZAS"
        "primera" in normalized || "materia" in normalized || "nota" in normalized || "semestre" in normalized -> "PRÓXIMO PASO"
        else -> "PULSO DE HOY"
    }
}

private fun HomePriorityAction.focusIcon(): ImageVector {
    return when (this) {
        HomePriorityAction.SUBJECT,
        HomePriorityAction.SUBJECTS -> Icons.Rounded.School
        HomePriorityAction.TASKS -> Icons.AutoMirrored.Rounded.EventNote
        HomePriorityAction.EXPENSES -> Icons.Rounded.Wallet
        HomePriorityAction.TEMPLATES -> Icons.Rounded.Description
    }
}

@Composable
private fun HomePriorityAction.agendaAccent(): Color {
    return when (this) {
        HomePriorityAction.SUBJECT,
        HomePriorityAction.SUBJECTS -> HomePurple
        HomePriorityAction.TASKS -> HomeTeal
        HomePriorityAction.EXPENSES -> HomeCoral
        HomePriorityAction.TEMPLATES -> HomeYellow
    }
}

private fun HomePrioritySummary.sheetBodyParagraphs(): List<String> {
    if (title == "Día despejado") {
        return listOf(
            "No tienes vencimientos cercanos por ahora.",
            "Es un buen momento para repasar, avanzar en tus materias y dejar listas tus próximas actividades."
        )
    }

    val sentences = fullDescription
        .split(". ")
        .mapIndexed { index, part ->
            val trimmed = part.trim()
            if (trimmed.endsWith(".") || index == fullDescription.split(". ").lastIndex) trimmed else "$trimmed."
        }
        .filter { it.isNotBlank() }

    return when {
        sentences.size >= 2 -> listOf(sentences.first(), sentences.drop(1).joinToString(" "))
        sentences.size == 1 -> listOf(sentences.first())
        else -> listOf(shortDescription)
    }
}

private fun HomePrioritySummary.sheetSuggestionText(): String {
    if (title == "Día despejado") {
        return "Dedica al menos 15 minutos a repasar hoy para mantener el ritmo."
    }
    return suggestion.substringAfter(": ", suggestion)
        .replaceFirstChar { char -> char.uppercase() }
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
