package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unistack.app.core.design.components.UniStackBrandHeader
import com.unistack.app.core.design.components.UniStackFabMenu
import com.unistack.app.core.design.responsive.UniStackResponsiveMetrics
import com.unistack.app.core.design.responsive.uniStackResponsiveMetrics
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_home.domain.AcademicWorkSummary
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.SubjectRiskSeverity
import com.unistack.app.feature_home.domain.SubjectRiskSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_home.domain.TaskSummary
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale

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
    modifier: Modifier = Modifier
) {
    val summary = uiState.summary
    val enabledModules = summary.enabledModules
    val showGrades = AppModule.GRADES in enabledModules
    val showTasks = AppModule.TASKS in enabledModules
    val showExpenses = AppModule.EXPENSES in enabledModules
    val showTemplates = AppModule.ACADEMIC_TEMPLATES in enabledModules

    val purpleColor = HomePurple
    val blueColor = HomeBlue
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBackgroundBrush)
    ) {
        val metrics = uniStackResponsiveMetrics(maxWidth)
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = purpleColor.copy(alpha = 0.14f),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.78f, size.height * 0.18f)
            )
            drawCircle(
                color = blueColor.copy(alpha = 0.08f),
                radius = size.width * 0.42f,
                center = Offset(size.width * 0.20f, size.height * 0.06f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = metrics.horizontalPadding,
                end = metrics.horizontalPadding,
                top = metrics.itemSpacing + 24.dp,
                bottom = 112.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (metrics.isCompact) 20.dp else 24.dp)
        ) {
            item {
                HomeHeader(
                    photoUrl = summary.avatarPhotoUrl,
                    onProfileClick = onProfileClick
                )
            }
            item { HomeGreeting(name = summary.userName) }
            item {
                HomeHeroCard(
                    summary = summary,
                    showGrades = showGrades,
                    showTasks = showTasks,
                    showExpenses = showExpenses,
                    onSeeTasksClick = onSeeTasksClick,
                    metrics = metrics
                )
            }
            item {
                HomeSummaryMetrics(
                    priorityCount = summary.priorityCount(showGrades, showTasks, showExpenses, showTemplates),
                    subjectsCount = if (showGrades) summary.subjectsCount else 0,
                    weeklyExpenseTotal = if (showExpenses) summary.weeklyExpenseTotal else 0,
                    metrics = metrics
                )
            }
            if (summary.hasActionablePriorities(showGrades, showExpenses)) {
                item {
                    HomePrioritiesSection(
                        summary = summary,
                        showGrades = showGrades,
                        showExpenses = showExpenses,
                        onSubjectClick = onSubjectClick,
                        onSeeExpensesClick = onSeeExpensesClick,
                        metrics = metrics
                    )
                }
            }
            if (showTasks || showExpenses || showTemplates) {
                item {
                    HomeWeekSection(
                        summary = summary,
                        showTasks = showTasks,
                        showExpenses = showExpenses,
                        showTemplates = showTemplates,
                        metrics = metrics
                    )
                }
            }
            if (showGrades) {
                item {
                    HomeSubjectsSection(
                        summary = summary,
                        onSeeAllSubjectsClick = onSeeAllSubjectsClick,
                        onAddSubjectClick = onAddSubjectClick,
                        onSubjectClick = onSubjectClick
                    )
                }
            }
            if (showExpenses) {
                item {
                    HomeExpensesSection(
                        expenses = summary.weeklyExpenses,
                        total = summary.weeklyExpenseTotal,
                        onSeeExpensesClick = onSeeExpensesClick
                    )
                }
            }
            if (showGrades || showTasks || showExpenses) {
                item {
                    HomeQuickActionsSection(
                        showGrades = showGrades,
                        showTasks = showTasks,
                        showExpenses = showExpenses,
                        onAddGradeClick = onAddGradeClick,
                        onAddTaskClick = onAddTaskClick,
                        onAddExpenseClick = onAddExpenseClick,
                        metrics = metrics
                    )
                }
            }
            if (showTemplates) {
                item {
                    HomeEmptyInlineCard(
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        title = "Plantillas académicas",
                        body = summary.nextAcademicWork?.let { "${it.title} vence ${it.dueText}." }
                            ?: "Organiza trabajos, exposiciones y entregas.",
                        accent = HomePurple,
                        onClick = onOpenTemplatesClick
                    )
                }
            }
            item {
                HomeEmptyInlineCard(
                    icon = Icons.Rounded.Person,
                    title = "Tu espacio UniStack",
                    body = "Personaliza tus módulos desde el perfil.",
                    accent = HomeBlue,
                    onClick = onProfileClick
                )
            }
        }

        UniStackFabMenu(
            onAddGradeClick = onAddGradeClick,
            onAddTaskClick = onAddTaskClick,
            onAddExpenseClick = onAddExpenseClick,
            onAddSubjectClick = onAddSubjectClick,
            showAddGrade = showGrades,
            showAddTask = showTasks,
            showAddExpense = showExpenses,
            showAddSubject = false,
            expandedEndPadding = metrics.horizontalPadding,
            expandedBottomPadding = 22.dp
        )
    }
}

@Composable
private fun HomeHeader(
    photoUrl: String?,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Menu,
            contentDescription = "Menú",
            tint = HomeText,
            modifier = Modifier
                .padding(end = 12.dp)
                .size(24.dp)
        )
        UniStackBrandHeader(symbolSize = 38.dp)
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .cleanClickable {}
        ) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = "Notificaciones",
                tint = HomeText,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(22.dp)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-7).dp, y = 7.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(HomePurple)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFFFFD8DE), Color(0xFFE6D9FF), Color(0xFFDDEBFF))))
                .cleanClickable(onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "Perfil",
                    tint = HomePurple,
                    modifier = Modifier.size(22.dp)
                )
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

@Composable
private fun HomeGreeting(name: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = "Hola, $name 👋",
            color = HomeText,
            fontSize = 31.sp,
            lineHeight = 35.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "Organiza tu día y mantén el control",
            color = HomeMuted,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeHeroCard(
    summary: HomeSummary,
    showGrades: Boolean,
    showTasks: Boolean,
    showExpenses: Boolean,
    onSeeTasksClick: () -> Unit,
    metrics: UniStackResponsiveMetrics,
    modifier: Modifier = Modifier
) {
    val purpleColor = HomePurple
    val hasCriticalSubject = showGrades &&
        summary.riskSubject != null &&
        summary.riskSubject.severity != SubjectRiskSeverity.STABLE
    val hasExpenseActivity = showExpenses && summary.weeklyExpenseTotal > 0
    val hasAnyHomeData = summary.subjectsCount > 0 ||
        summary.tasksToday > 0 ||
        summary.nextTask != null ||
        summary.nextAcademicWork != null ||
        summary.weeklyExpenseTotal > 0
    val showHeroAction = hasCriticalSubject ||
        hasExpenseActivity ||
        (showTasks && (summary.tasksToday > 0 || summary.overdueTasks > 0 || summary.nextTask != null || summary.nextAcademicWork != null))
    val heroHeight = when {
        !hasAnyHomeData -> if (metrics.isCompact) 206.dp else 218.dp
        metrics.isCompact -> 224.dp
        metrics.isMedium -> 230.dp
        else -> 238.dp
    }
    val heroPadding = if (metrics.isCompact) 15.dp else 20.dp
    HomeCard(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight),
        shape = RoundedCornerShape(metrics.cardRadius + 4.dp),
        borderColor = purpleColor.copy(alpha = 0.68f),
        contentPadding = PaddingValues(0.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val illustrationWidth = if (metrics.isCompact) {
                (maxWidth * 0.27f).coerceIn(78.dp, 96.dp)
            } else {
                (maxWidth * 0.34f).coerceIn(120.dp, 160.dp)
            }
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = purpleColor.copy(alpha = 0.34f),
                    radius = size.width * 0.40f,
                    center = Offset(size.width * 0.80f, size.height * 0.42f)
                )
                drawCircle(
                    color = purpleColor.copy(alpha = 0.13f),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.95f, size.height * 0.18f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = heroPadding, vertical = heroPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(if (metrics.isCompact) 8.dp else 10.dp)
                ) {
                    Text(
                        text = "Resumen de hoy",
                        color = HomeText,
                        fontSize = if (metrics.isCompact) 22.sp else 28.sp,
                        lineHeight = if (metrics.isCompact) 27.sp else 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Text(
                        text = if (hasAnyHomeData) {
                            buildAnnotatedString {
                                withStyle(SpanStyle(color = HomePurple, fontWeight = FontWeight.Medium)) {
                                    append("${if (hasCriticalSubject) 1 else 0} materia crítica")
                                }
                                append(", ")
                                append("${if (showTasks) summary.tasksToday else 0} tareas y ")
                                withStyle(SpanStyle(color = HomeText)) {
                                    append(CurrencyFormatter.formatCop(if (showExpenses) summary.weeklyExpenseTotal else 0))
                                }
                                append(" registrados esta semana.")
                            }
                        } else {
                            buildAnnotatedString {
                                append("Agrega materias, tareas o gastos para construir tu resumen.")
                            }
                        },
                        color = HomeText,
                        fontSize = if (metrics.isCompact) 15.sp else 17.sp,
                        lineHeight = if (metrics.isCompact) 22.sp else 25.sp,
                        fontWeight = FontWeight.Normal
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (hasCriticalSubject) {
                            HomeStatusChip("Materia crítica", Icons.Rounded.TrackChanges, HomeCoral)
                        }
                        if (showTasks && summary.tasksToday == 0 && summary.overdueTasks == 0) {
                            HomeStatusChip("Sin tareas", Icons.Rounded.CheckCircle, HomeBlue)
                        }
                        if (hasExpenseActivity) {
                            HomeStatusChip("Gastos registrados", Icons.Rounded.BarChart, HomePurple)
                        }
                    }
                    if (showHeroAction) {
                        HomeOutlineButton(
                            text = "Ver pendientes",
                            onClick = onSeeTasksClick
                        )
                    }
                }
                HomeHeroIllustration(
                    modifier = Modifier
                        .width(illustrationWidth)
                        .height(if (metrics.isCompact) 112.dp else 148.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeStatusChip(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(28.dp),
        shape = RoundedCornerShape(14.dp),
        color = HomeElevated.copy(alpha = 0.82f),
        border = BorderStroke(1.dp, HomeBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(label, color = HomeText, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun HomeOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(38.dp)
            .cleanClickable(onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, HomePurple)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text, color = HomePurple, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = HomePurple, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun HomeHeroIllustration(modifier: Modifier = Modifier) {
    val purpleColor = HomePurple
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = purpleColor.copy(alpha = 0.24f),
                radius = size.minDimension * 0.46f,
                center = Offset(size.width * 0.58f, size.height * 0.50f)
            )
            drawCircle(
                color = purpleColor.copy(alpha = 0.12f),
                radius = size.minDimension * 0.31f,
                center = Offset(size.width * 0.66f, size.height * 0.46f)
            )
            drawRoundRect(
                color = if (UniStackColors.IsDarkTheme) Color.White.copy(alpha = 0.82f) else Color(0xFFDDD6FE),
                topLeft = Offset(size.width * 0.58f, size.height * 0.18f),
                size = Size(size.width * 0.035f, size.height * 0.44f),
                cornerRadius = CornerRadius(size.width * 0.03f, size.width * 0.03f)
            )
            drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFFB491FF), Color(0xFF6E43FF))),
                topLeft = Offset(size.width * 0.60f, size.height * 0.20f),
                size = Size(size.width * 0.33f, size.height * 0.18f),
                cornerRadius = CornerRadius(size.height * 0.09f, size.height * 0.09f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF9C7BFF), Color(0xFF6D45F4))),
                topLeft = Offset(size.width * 0.36f, size.height * 0.48f),
                size = Size(size.width * 0.48f, size.height * 0.17f),
                cornerRadius = CornerRadius(size.height * 0.09f, size.height * 0.09f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Color(0xFF8F68FF), Color(0xFF5D3EE8))),
                topLeft = Offset(size.width * 0.30f, size.height * 0.64f),
                size = Size(size.width * 0.58f, size.height * 0.17f),
                cornerRadius = CornerRadius(size.height * 0.09f, size.height * 0.09f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.14f),
                topLeft = Offset(size.width * 0.45f, size.height * 0.54f),
                size = Size(size.width * 0.24f, size.height * 0.045f),
                cornerRadius = CornerRadius(size.height * 0.03f, size.height * 0.03f)
            )
            drawRoundRect(
                color = Color.White.copy(alpha = 0.13f),
                topLeft = Offset(size.width * 0.43f, size.height * 0.70f),
                size = Size(size.width * 0.30f, size.height * 0.045f),
                cornerRadius = CornerRadius(size.height * 0.03f, size.height * 0.03f)
            )
            drawRoundRect(
                brush = Brush.horizontalGradient(listOf(Color(0xFF5B35FF), Color(0xFFA487FF))),
                topLeft = Offset(size.width * 0.22f, size.height * 0.84f),
                size = Size(size.width * 0.70f, size.height * 0.055f),
                cornerRadius = CornerRadius(size.height * 0.04f, size.height * 0.04f)
            )
        }
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = if (UniStackColors.IsDarkTheme) Color.White else Color(0xFF7B5CF5),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 6.dp, y = (-8).dp)
                .size(26.dp)
        )
    }
}

@Composable
private fun HomeSummaryMetrics(
    priorityCount: Int,
    subjectsCount: Int,
    weeklyExpenseTotal: Int,
    metrics: UniStackResponsiveMetrics,
    modifier: Modifier = Modifier
) {
    HomeCard(
        modifier = modifier
            .fillMaxWidth()
            .height(
                when {
                    metrics.isCompact -> 60.dp
                    metrics.isMedium -> 64.dp
                    else -> 68.dp
                }
            ),
        shape = RoundedCornerShape(metrics.cardRadius),
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            HomeMetric(value = "$priorityCount", label = "prioridad", color = HomePurple, modifier = Modifier.weight(1f))
            VerticalDivider()
            HomeMetric(
                value = "$subjectsCount",
                label = if (subjectsCount == 1) "materia" else "materias",
                color = HomePurple,
                modifier = Modifier.weight(1f)
            )
            VerticalDivider()
            HomeMetric(
                value = CurrencyFormatter.formatCop(weeklyExpenseTotal),
                label = "gastos esta semana",
                color = HomeCoral,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeMetric(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(value, color = color, fontSize = 18.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(label, color = HomeMuted, fontSize = 10.sp, lineHeight = 12.sp, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .height(42.dp)
            .width(1.dp)
            .background(HomeBorder)
    )
}

@Composable
private fun HomePrioritiesSection(
    summary: HomeSummary,
    showGrades: Boolean,
    showExpenses: Boolean,
    onSubjectClick: (String) -> Unit,
    onSeeExpensesClick: () -> Unit,
    metrics: UniStackResponsiveMetrics,
    modifier: Modifier = Modifier
) {
    val hasCriticalSubject = showGrades &&
        summary.riskSubject != null &&
        summary.riskSubject.severity != SubjectRiskSeverity.STABLE
    val hasExpenseActivity = showExpenses && summary.weeklyExpenseTotal > 0

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeSectionHeader(title = "Prioridades", actionText = "Ver todas", onActionClick = onSeeExpensesClick)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (hasCriticalSubject) {
                PrioritySubjectCard(summary, showGrades, onSubjectClick)
            }
            if (hasExpenseActivity) {
                PriorityExpenseCard(summary.weeklyExpenseTotal, showExpenses, onSeeExpensesClick)
            }
        }
    }
}

@Composable
private fun PrioritySubjectCard(
    summary: HomeSummary,
    showGrades: Boolean,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val risk = summary.riskSubject
    val accent = if (risk?.severity == SubjectRiskSeverity.STABLE) HomeGreen else HomeCoral
    PriorityCard(
        title = if (risk?.severity == SubjectRiskSeverity.STABLE) "Materia estable" else "Materia crítica",
        body = risk?.let { "${it.subjectName} necesita atención académica." }
            ?: if (showGrades) "Tus materias están bajo control." else "Activa materias para ver alertas.",
        icon = Icons.Rounded.TrackChanges,
        accent = accent,
        actionText = "Abrir",
        onActionClick = {
            if (risk != null) onSubjectClick(risk.subjectId)
        },
        modifier = modifier
    )
}

@Composable
private fun PriorityExpenseCard(
    weeklyExpenseTotal: Int,
    showExpenses: Boolean,
    onSeeExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PriorityCard(
        title = "Gasto semanal",
        body = if (showExpenses) {
            "Has registrado ${CurrencyFormatter.formatCop(weeklyExpenseTotal)} esta semana."
        } else {
            "Activa gastos para ver tu resumen."
        },
        icon = Icons.Rounded.CreditCard,
        accent = HomeCoral,
        actionText = "Ver gastos",
        onActionClick = onSeeExpensesClick,
        modifier = modifier
    )
}

@Composable
private fun PriorityCard(
    title: String,
    body: String,
    icon: ImageVector,
    accent: Color,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeCard(
        modifier = modifier.height(74.dp),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            AccentIcon(icon = icon, accent = accent, size = 36.dp)
            Column(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, color = HomeText, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(body, color = HomeMuted, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            SmallOutlineAction(text = actionText, color = accent, onClick = onActionClick)
        }
    }
}

@Composable
private fun SmallOutlineAction(text: String, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(36.dp)
            .cleanClickable(onClick),
        shape = RoundedCornerShape(15.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, color)
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
            Text(text, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
private fun HomeWeekSection(
    summary: HomeSummary,
    showTasks: Boolean,
    showExpenses: Boolean,
    showTemplates: Boolean,
    metrics: UniStackResponsiveMetrics,
    modifier: Modifier = Modifier
) {
    val cards = listOf(
        WeekItem("Hoy", "${if (showTasks) summary.tasksToday else 0}", "tareas", Icons.Rounded.CheckCircle, HomePurple),
        WeekItem("Tarea", summary.nextTask?.dueText ?: "--", summary.nextTask?.title ?: "sin pendientes", Icons.AutoMirrored.Rounded.Assignment, HomeBlue),
        WeekItem("Trabajo", summary.nextAcademicWork?.dueText ?: "--", if (showTemplates) summary.nextAcademicWork?.title ?: "sin entregas" else "sin entregas", Icons.AutoMirrored.Rounded.MenuBook, HomePurple),
        WeekItem("Gastos", CurrencyFormatter.formatCop(if (showExpenses) summary.weeklyExpenseTotal else 0), "semana actual", Icons.Rounded.AccountBalanceWallet, HomeCoral)
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeSectionHeader(title = "Esta semana")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            cards.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowItems.forEach { item -> WeekMiniCard(item = item, modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

private data class WeekItem(
    val title: String,
    val value: String,
    val detail: String,
    val icon: ImageVector,
    val accent: Color
)

@Composable
private fun WeekMiniCard(item: WeekItem, modifier: Modifier = Modifier) {
        HomeCard(
            modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(11.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(item.icon, contentDescription = null, tint = item.accent, modifier = Modifier.size(18.dp))
                Text(item.title, color = HomeMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(item.value, color = HomeText, fontSize = 17.sp, lineHeight = 19.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.detail, color = HomeMuted, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun HomeSubjectsSection(
    summary: HomeSummary,
    onSeeAllSubjectsClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HomeSectionHeader(title = "Materias", actionText = "Ver todas", onActionClick = onSeeAllSubjectsClick)
        val subject = summary.subjects.firstOrNull()
        if (subject == null) {
            HomeEmptyInlineCard(
                icon = Icons.AutoMirrored.Rounded.MenuBook,
                title = "Aún no tienes materias.",
                body = "Agrega la primera para empezar a calcular tu promedio.",
                accent = HomePurple,
                onClick = onAddSubjectClick
            )
        } else {
            SubjectHighlightCard(
                subject = subject,
                gradingScale = summary.gradingScale,
                onClick = { onSubjectClick(subject.id) }
            )
        }
    }
}

@Composable
private fun SubjectHighlightCard(
    subject: SubjectSummary,
    gradingScale: GradingScale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = subjectStyle(subject.type)
    val averageText = subject.average?.let { GradingScaleUtils.formatGrade(it, gradingScale) } ?: "--"
    val targetText = GradingScaleUtils.formatGrade(subject.targetAverage, gradingScale)
    HomeCard(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp)
            .cleanClickable(onClick),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            if (maxWidth >= 340.dp) {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    AccentIcon(icon = style.icon, accent = style.accent, size = 36.dp)
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .width(118.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(subject.name, color = HomeText, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(averageText, color = style.accent, fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
                            Text(" / $targetText", color = HomeMuted, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                            .background(style.accent.copy(alpha = 0.18f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(subject.progress.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(50))
                                .background(style.accent)
                        )
                    }
                    Text(
                        text = if ((subject.average ?: 0.0) < subject.targetAverage) "Necesita recuperar la meta" else "Va sobre la meta",
                        color = HomeMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(106.dp)
                    )
                }
            } else {
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    AccentIcon(icon = style.icon, accent = style.accent, size = 40.dp)
                    Column(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(subject.name, color = HomeText, fontSize = 15.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    text = if ((subject.average ?: 0.0) < subject.targetAverage) "Necesita recuperar la meta" else "Va sobre la meta",
                                    color = HomeMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                modifier = Modifier.padding(start = 10.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(averageText, color = style.accent, fontSize = 20.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold)
                                Text(" / $targetText", color = HomeMuted, fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                                .background(style.accent.copy(alpha = 0.18f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(subject.progress.coerceIn(0f, 1f))
                                    .clip(RoundedCornerShape(50))
                                    .background(style.accent)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeExpensesSection(
    expenses: ExpenseSummary?,
    total: Int,
    onSeeExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transport = expenses?.transport ?: 0
    val food = expenses?.food ?: 0
    val values = expenses?.chartValues ?: listOf(0, 0, 0, total, 0, 0, 0)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSectionHeader(title = "Gastos de la semana", actionText = "Ver detalles", onActionClick = onSeeExpensesClick)
        HomeCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            shape = RoundedCornerShape(18.dp),
            contentPadding = PaddingValues(11.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val compact = maxWidth < 340.dp
                val chartWidth = if (compact) 68.dp else 88.dp
                val totalWidth = if (compact) 78.dp else 104.dp
                val totalFontSize = if (compact) 17.sp else 20.sp
                Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.width(totalWidth), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Total gastado", color = HomeMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text(CurrencyFormatter.formatCop(total), color = HomeText, fontSize = totalFontSize, lineHeight = 25.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("vs semana anterior", color = HomeMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("↓ 12%", color = HomeGreen, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                        }
                    }
                    VerticalDivider()
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExpenseCategoryLine(icon = Icons.AutoMirrored.Rounded.Assignment, label = "Transporte", amount = transport)
                        ExpenseCategoryLine(icon = Icons.Rounded.AccountBalanceWallet, label = "Comida", amount = food)
                    }
                    VerticalDivider()
                    HomeMiniBars(values = values, color = HomePurple, modifier = Modifier.padding(start = 8.dp).width(chartWidth))
                }
            }
        }
    }
}

@Composable
private fun ExpenseCategoryLine(icon: ImageVector, label: String, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = HomePurple, modifier = Modifier.size(17.dp))
        Text(label, color = HomeMuted, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 6.dp).weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(CurrencyFormatter.formatCop(amount), color = HomePurple, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun HomeQuickActionsSection(
    showGrades: Boolean,
    showTasks: Boolean,
    showExpenses: Boolean,
    onAddGradeClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    metrics: UniStackResponsiveMetrics,
    modifier: Modifier = Modifier
) {
    val actions = buildList {
        if (showGrades) add(QuickAction("Agregar nota", Icons.Rounded.Calculate, HomePurple, onAddGradeClick))
        if (showTasks) add(QuickAction("Nueva tarea", Icons.AutoMirrored.Rounded.Assignment, HomeBlue, onAddTaskClick))
        if (showExpenses) add(QuickAction("Registrar gasto", Icons.Rounded.AccountBalanceWallet, HomeCoral, onAddExpenseClick))
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HomeSectionHeader(title = "Acciones rápidas")
        if (metrics.isCompact) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                actions.forEach { action ->
                    QuickActionCard(action = action, fullWidth = true, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                actions.forEach { action ->
                    QuickActionCard(action = action, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionCard(action: QuickAction, modifier: Modifier = Modifier, fullWidth: Boolean = false) {
    HomeCard(
        modifier = modifier
            .height(if (fullWidth) 78.dp else 72.dp)
            .cleanClickable(action.onClick),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(if (fullWidth) 14.dp else 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            AccentIcon(icon = action.icon, accent = action.accent, size = if (fullWidth) 42.dp else 36.dp)
            Text(
                action.title,
                color = HomeText,
                fontSize = if (fullWidth) 15.sp else 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(start = if (fullWidth) 14.dp else 8.dp)
                    .weight(1f)
            )
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = HomeText, modifier = Modifier.size(if (fullWidth) 18.dp else 15.dp))
        }
    }
}

@Composable
private fun HomeEmptyInlineCard(
    icon: ImageVector,
    title: String,
    body: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    HomeCard(
        modifier = modifier
            .fillMaxWidth()
            .cleanClickable(onClick),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, color = HomeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(body, color = HomeMuted, fontSize = 12.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            color = HomeText,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onActionClick != null) {
            Row(
                modifier = Modifier.cleanClickable(onActionClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(actionText, color = HomePurple, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = HomeText, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun HomeCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderColor: Color = HomeBorder,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(HomeCardBrush)
            .border(1.dp, borderColor, shape)
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
private fun AccentIcon(icon: ImageVector, accent: Color, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.34f))
            .background(accent.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(size * 0.48f))
    }
}

@Composable
private fun HomeMiniBars(values: List<Int>, color: Color, modifier: Modifier = Modifier) {
    val days = listOf("L", "M", "M", "J", "V", "S", "D")
    val normalized = normalizeBars(values)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            val guideColor = if (UniStackColors.IsDarkTheme) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)
            val barWidth = size.width / 18f
            val spacing = (size.width - barWidth * 7) / 6f
            repeat(3) { index ->
                val y = size.height * (0.22f + index * 0.24f)
                drawLine(guideColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.1f)
            }
            normalized.forEachIndexed { index, heightRatio ->
                val barHeight = size.height * heightRatio
                val x = index * (barWidth + spacing)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            days.forEach { day ->
                Text(day, color = HomeMuted, fontSize = 9.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun normalizeBars(values: List<Int>): List<Float> {
    if (values.isEmpty() || values.all { it <= 0 }) return listOf(0.34f, 0.38f, 0.36f, 0.82f, 0.35f, 0.38f, 0.36f)
    val max = values.maxOf { it }.coerceAtLeast(1)
    return values.take(7).map { value -> (0.22f + (value.toFloat() / max) * 0.68f).coerceIn(0.18f, 0.9f) }
}

private data class SubjectVisualStyle(
    val icon: ImageVector,
    val accent: Color
)

private fun subjectStyle(type: SubjectVisualType): SubjectVisualStyle {
    return when (type) {
        SubjectVisualType.TEAL -> SubjectVisualStyle(Icons.AutoMirrored.Rounded.MenuBook, UniStackColors.Teal)
        SubjectVisualType.BLUE -> SubjectVisualStyle(Icons.Rounded.BarChart, UniStackColors.Blue)
        SubjectVisualType.CORAL -> SubjectVisualStyle(Icons.Rounded.CreditCard, UniStackColors.Coral)
        SubjectVisualType.PURPLE -> SubjectVisualStyle(Icons.AutoMirrored.Rounded.MenuBook, UniStackColors.Primary)
        SubjectVisualType.GREEN -> SubjectVisualStyle(Icons.Rounded.CheckCircle, UniStackColors.Green)
        SubjectVisualType.YELLOW -> SubjectVisualStyle(Icons.Rounded.Star, UniStackColors.Yellow)
        SubjectVisualType.ROSE -> SubjectVisualStyle(Icons.AutoMirrored.Rounded.MenuBook, Color(0xFFE84A8A))
        SubjectVisualType.INDIGO -> SubjectVisualStyle(Icons.AutoMirrored.Rounded.MenuBook, Color(0xFF6B72FF))
        SubjectVisualType.ORANGE -> SubjectVisualStyle(Icons.Rounded.Calculate, Color(0xFFF57C00))
        SubjectVisualType.CYAN -> SubjectVisualStyle(Icons.Rounded.BarChart, Color(0xFF00A6D6))
        SubjectVisualType.LIME -> SubjectVisualStyle(Icons.Rounded.CheckCircle, Color(0xFF7CB342))
        SubjectVisualType.SLATE -> SubjectVisualStyle(Icons.AutoMirrored.Rounded.MenuBook, Color(0xFF8AA0AA))
    }
}

private fun HomeSummary.priorityCount(
    showGrades: Boolean,
    showTasks: Boolean,
    showExpenses: Boolean,
    showTemplates: Boolean
): Int {
    var count = 0
    if (showGrades && riskSubject != null && riskSubject.severity != SubjectRiskSeverity.STABLE) count += 1
    if (showTasks && overdueTasks > 0) count += overdueTasks
    if (showTemplates && nextAcademicWork != null) count += 1
    if (showExpenses && weeklyExpenseTotal > 0) count += 1
    return count
}

private fun HomeSummary.hasActionablePriorities(
    showGrades: Boolean,
    showExpenses: Boolean
): Boolean {
    val hasCriticalSubject = showGrades &&
        riskSubject != null &&
        riskSubject.severity != SubjectRiskSeverity.STABLE
    val hasExpenseActivity = showExpenses && weeklyExpenseTotal > 0
    return hasCriticalSubject || hasExpenseActivity
}

private fun Modifier.cleanClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

private val HomeBackground: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF070A12) else Color(0xFFF5F3FA)
private val HomeSurface: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF10131B) else Color(0xFFFFFFFF)
private val HomeElevated: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF151826) else Color(0xFFF0ECF6)
private val HomeText: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFF6F7FB) else Color(0xFF1A1A2E)
private val HomeMuted: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA9ADBD) else Color(0xFF6E6B80)
private val HomeBorder: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)
private val HomePurple: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF8B6BFF) else Color(0xFF7B5CF5)
private val HomeCoral: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFFF6F6A) else Color(0xFFE85D58)
private val HomeBlue: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF5C8DFF) else Color(0xFF4A7AEE)
private val HomeGreen: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF39D98A) else Color(0xFF2BBD76)
private val HomeBackgroundBrush: Brush
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        Brush.radialGradient(
            colors = listOf(Color(0xFF101A32), Color(0xFF070A12)),
            center = Offset(1000f, 0f),
            radius = 1200f
        )
    } else {
        Brush.radialGradient(
            colors = listOf(Color(0xFFEDE7F6), Color(0xFFF5F3FA)),
            center = Offset(1000f, 0f),
            radius = 1200f
        )
    }
private val HomeCardBrush: Brush
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        Brush.linearGradient(listOf(Color(0xFF10131B), Color(0xFF0D111B)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF9F7FE)))
    }

@Preview(name = "Home dark 430", widthDp = 430, heightDp = 932, showBackground = true)
@Composable
private fun HomePreview430() {
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

@Preview(name = "Home dark 360", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun HomePreview360() {
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
    dashboardMessage = "Imprenta necesita atención académica.",
    generalAverage = 3.4,
    subjectsCount = 4,
    tasksToday = 0,
    overdueTasks = 0,
    subjects = listOf(
        SubjectSummary(
            id = "subject-1",
            name = "Imprenta",
            average = 3.4,
            targetAverage = 4.0,
            progress = 0.62f,
            type = SubjectVisualType.ROSE
        )
    ),
    riskSubject = SubjectRiskSummary(
        subjectId = "subject-1",
        subjectName = "Imprenta",
        detail = "Necesita recuperar la meta.",
        severity = SubjectRiskSeverity.CRITICAL
    ),
    neededGrade = null,
    nextTask = null,
    nextAcademicWork = AcademicWorkSummary(
        id = "work-1",
        subjectId = "subject-1",
        title = "Borrador final",
        dueText = "sin entregas",
        progress = 0.2f
    ),
    weeklyExpenses = ExpenseSummary(
        transport = 7_400,
        food = 13_500,
        total = 20_900,
        chartValues = listOf(2_000, 3_200, 3_000, 8_500, 2_000, 1_200, 1_000)
    ),
    weeklyExpenseTotal = 20_900,
    productivitySummary = "Sin tareas todavía.",
    gradingScale = GradingScale.ZERO_TO_FIVE,
    enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES, AppModule.ACADEMIC_TEMPLATES)
)
