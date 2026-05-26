package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EventNote
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unistack.app.core.design.components.UniStackFabMenu
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.AcademicWorkSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.SubjectRiskSeverity
import com.unistack.app.feature_home.domain.SubjectRiskSummary
import com.unistack.app.feature_home.domain.SubjectSummary
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
    val displayName = summary.userName.takeIf { it.isNotBlank() } ?: "Pineda"
    val prioritySubject = summary.riskSubject?.subjectName?.takeIf { it.isNotBlank() } ?: "Inglés"
    val heroTitle = "$prioritySubject necesita atención"

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
                    onProfileClick = onProfileClick
                )
            }
            item {
                HomeGreeting(name = displayName, compact = isCompact)
            }
            item {
                PriorityHero(
                    title = heroTitle,
                    compact = isCompact,
                    onOpenClick = {
                        summary.riskSubject?.subjectId?.let(onSubjectClick) ?: onSeeAllSubjectsClick()
                    }
                )
            }
            item {
                QuickModules(
                    subjectsCount = summary.subjectsCount,
                    tasksToday = summary.tasksToday,
                    weeklyExpenseTotal = summary.weeklyExpenseTotal,
                    notesCount = summary.nextAcademicWork?.let { 3 } ?: 0,
                    fourColumns = maxWidth >= 360.dp,
                    compact = isCompact,
                    onSubjectsClick = onSeeAllSubjectsClick,
                    onTasksClick = onSeeTasksClick,
                    onExpensesClick = onSeeExpensesClick,
                    onNotesClick = onOpenTemplatesClick
                )
            }
            item {
                TodayTimeline(onTasksClick = onSeeTasksClick, compact = isCompact)
            }
            item {
                CompanionCard(
                    name = displayName,
                    priorities = summary.priorityCount(),
                    pendingTasks = summary.overdueTasks,
                    compact = isCompact,
                    onProfileClick = onProfileClick
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
    }
}

@Composable
private fun HomeHeader(
    photoUrl: String?,
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
                HeaderIcon(icon = Icons.Rounded.NotificationsNone, contentDescription = "Notificaciones")
                Box(
                    modifier = Modifier
                        .offset(x = (-7).dp, y = 5.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HomePurple)
                )
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
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(38.dp), contentAlignment = Alignment.Center) {
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp)
    ) {
        Text(
            text = "Hola, $name 👋",
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
private fun PriorityHero(
    title: String,
    compact: Boolean,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroHeight = if (compact) 174.dp else 190.dp
    val heroPadding = if (compact) 16.dp else 18.dp
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight),
        shape = RoundedCornerShape(19.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, HomePurple.copy(alpha = 0.42f)),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HeroBrush)
                .padding(heroPadding)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = HomeHeroGlow.copy(alpha = 0.58f),
                    radius = size.minDimension * 0.48f,
                    center = Offset(size.width * 0.74f, size.height * 0.58f)
                )
                drawCircle(
                    color = HomeHeroGlow.copy(alpha = 0.28f),
                    radius = size.minDimension * 0.72f,
                    center = Offset(size.width * 0.78f, size.height * 0.54f)
                )
                drawCircle(
                    color = HomeHeroGlow.copy(alpha = 0.12f),
                    radius = size.minDimension * 0.98f,
                    center = Offset(size.width * 0.84f, size.height * 0.42f)
                )
                drawCircle(
                    color = HomeHeroLight.copy(alpha = 0.05f),
                    radius = size.minDimension * 0.25f,
                    center = Offset(size.width * 0.68f, size.height * 0.24f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.56f)
                    .align(Alignment.CenterStart),
                verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = HomePurple, modifier = Modifier.size(13.dp))
                    Text(
                        text = "PRIORIDAD DE HOY",
                        color = HomePurple,
                        fontSize = 8.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp
                    )
                }
                Text(
                    text = title,
                    color = HomeText,
                    fontSize = if (compact) 20.sp else 22.sp,
                    lineHeight = if (compact) 25.sp else 27.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Tienes clase a las 10:00 AM.\nRevisa tus apuntes antes de entrar.",
                    color = HomeSoftText,
                    fontSize = if (compact) 10.sp else 11.sp,
                    lineHeight = if (compact) 15.sp else 16.sp,
                    fontWeight = FontWeight.Normal
                )
                Button(
                    onClick = onOpenClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HomePurple),
                    contentPadding = PaddingValues(horizontal = 13.dp, vertical = 0.dp),
                    modifier = Modifier.height(if (compact) 32.dp else 34.dp)
                ) {
                    Text("Abrir materia", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }

            NotebookIllustration(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(if (compact) 116.dp else 132.dp)
                    .aspectRatio(0.86f)
                    .graphicsLayer {
                        rotationZ = -5f
                        translationX = 6.dp.toPx()
                    }
            )
        }
    }
}

@Composable
private fun NotebookIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val shadow = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.83f)
            quadraticBezierTo(size.width * 0.58f, size.height * 0.96f, size.width * 0.96f, size.height * 0.80f)
            quadraticBezierTo(size.width * 0.62f, size.height * 0.72f, size.width * 0.22f, size.height * 0.83f)
        }
        drawPath(shadow, HomeShadow.copy(alpha = 0.25f))
        drawNotebook()
        drawPen()
    }
}

private fun DrawScope.drawNotebook() {
    drawRoundRect(
        brush = Brush.linearGradient(listOf(HomeNotebookTop, HomeNotebookBottom)),
        topLeft = Offset(size.width * 0.18f, size.height * 0.12f),
        size = Size(size.width * 0.58f, size.height * 0.62f),
        cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
    )
    drawRoundRect(
        color = HomeShadow.copy(alpha = 0.18f),
        topLeft = Offset(size.width * 0.68f, size.height * 0.16f),
        size = Size(size.width * 0.11f, size.height * 0.58f),
        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
    )
    repeat(4) { index ->
        val y = size.height * (0.22f + index * 0.12f)
        drawRoundRect(
            color = HomeNotebookBinding,
            topLeft = Offset(size.width * 0.12f, y),
            size = Size(size.width * 0.16f, size.height * 0.035f),
            cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
        )
    }
    drawRoundRect(
        color = HomeHeroLight.copy(alpha = 0.20f),
        topLeft = Offset(size.width * 0.44f, size.height * 0.30f),
        size = Size(size.width * 0.25f, size.height * 0.14f),
        cornerRadius = CornerRadius(7.dp.toPx(), 7.dp.toPx())
    )
    drawRoundRect(
        color = HomeHeroLight.copy(alpha = 0.16f),
        topLeft = Offset(size.width * 0.48f, size.height * 0.36f),
        size = Size(size.width * 0.13f, size.height * 0.014f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
}

private fun DrawScope.drawPen() {
    drawRoundRect(
        brush = Brush.linearGradient(listOf(HomePenTop, HomePenBottom)),
        topLeft = Offset(size.width * 0.67f, size.height * 0.45f),
        size = Size(size.width * 0.16f, size.height * 0.36f),
        cornerRadius = CornerRadius(9.dp.toPx(), 9.dp.toPx())
    )
    drawRoundRect(
        color = HomeHeroLight.copy(alpha = 0.28f),
        topLeft = Offset(size.width * 0.68f, size.height * 0.49f),
        size = Size(size.width * 0.13f, size.height * 0.025f),
        cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
    )
    val tip = Path().apply {
        moveTo(size.width * 0.69f, size.height * 0.79f)
        lineTo(size.width * 0.78f, size.height * 0.90f)
        lineTo(size.width * 0.84f, size.height * 0.76f)
        close()
    }
    drawPath(tip, HomePenTip)
}

@Composable
private fun QuickModules(
    subjectsCount: Int,
    tasksToday: Int,
    weeklyExpenseTotal: Int,
    notesCount: Int,
    fourColumns: Boolean,
    compact: Boolean,
    onSubjectsClick: () -> Unit,
    onTasksClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onNotesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Módulos rápidos", color = HomeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("Personaliza tu día", color = HomePurple, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        if (fourColumns) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                QuickModuleCard("Materias", "${subjectsCount.coerceAtLeast(1)} activa", Icons.AutoMirrored.Rounded.MenuBook, HomePurple, onSubjectsClick, Modifier.weight(1f), compact = true)
                QuickModuleCard("Tareas", "$tasksToday hoy", Icons.Rounded.EventNote, HomeTeal, onTasksClick, Modifier.weight(1f), compact = true)
                QuickModuleCard("Gastos", "$${weeklyExpenseTotal / 1000} semana", Icons.Rounded.Wallet, HomeCoral, onExpensesClick, Modifier.weight(1f), compact = true)
                QuickModuleCard("Notas", "$notesCount apuntes", Icons.Rounded.Description, HomeYellow, onNotesClick, Modifier.weight(1f), compact = true)
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickModuleCard("Materias", "${subjectsCount.coerceAtLeast(1)} activa", Icons.AutoMirrored.Rounded.MenuBook, HomePurple, onSubjectsClick, Modifier.weight(1f), compact = compact)
                QuickModuleCard("Tareas", "$tasksToday hoy", Icons.Rounded.EventNote, HomeTeal, onTasksClick, Modifier.weight(1f), compact = compact)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickModuleCard("Gastos", "$${weeklyExpenseTotal / 1000} semana", Icons.Rounded.Wallet, HomeCoral, onExpensesClick, Modifier.weight(1f), compact = compact)
                QuickModuleCard("Notas", "$notesCount apuntes", Icons.Rounded.Description, HomeYellow, onNotesClick, Modifier.weight(1f), compact = compact)
            }
        }
    }
}

@Composable
private fun QuickModuleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean
) {
    Surface(
        modifier = modifier
            .height(if (compact) 76.dp else 92.dp)
            .cleanClickable(onClick),
        shape = RoundedCornerShape(14.dp),
        color = HomeCard,
        border = BorderStroke(1.dp, HomeBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = if (compact) 10.dp else 13.dp, vertical = if (compact) 10.dp else 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(if (compact) 28.dp else 34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(if (compact) 17.dp else 20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        title,
                        color = HomeText,
                        fontSize = if (compact) 10.sp else 13.sp,
                        lineHeight = if (compact) 13.sp else 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = HomeMuted, modifier = Modifier.size(if (compact) 12.dp else 15.dp))
                }
                Text(subtitle, color = HomeMuted, fontSize = if (compact) 9.sp else 11.sp, lineHeight = if (compact) 12.sp else 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun TodayTimeline(
    onTasksClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        TimelineItem("10:00 AM", "Clase de Inglés", "Aula 302", Icons.Rounded.CalendarMonth, HomePurple, true),
        TimelineItem("2:00 PM", "Entrega de proyecto", "Matemáticas", Icons.Rounded.EventNote, HomeTeal, false),
        TimelineItem("8:00 AM", "Examen parcial", "Física", Icons.AutoMirrored.Rounded.Assignment, HomeYellow, false)
    )

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Hoy", color = HomeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Ver todo",
                color = HomePurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.cleanClickable(onTasksClick)
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = HomeCard,
            border = BorderStroke(1.dp, HomeBorder)
        ) {
            Column(modifier = Modifier.padding(vertical = if (compact) 8.dp else 10.dp)) {
                items.forEachIndexed { index, item ->
                    TimelineRow(item = item, showLineTop = index > 0, showLineBottom = index < items.lastIndex, compact = compact)
                }
            }
        }
    }
}

@Composable
private fun TimelineRow(
    item: TimelineItem,
    showLineTop: Boolean,
    showLineBottom: Boolean,
    compact: Boolean
) {
    val inactiveRing = HomeMuted.copy(alpha = 0.75f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 58.dp else 64.dp)
            .padding(horizontal = if (compact) 14.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val rowHeight = if (compact) 58.dp else 64.dp
        Box(modifier = Modifier.size(width = 34.dp, height = rowHeight), contentAlignment = Alignment.Center) {
            if (showLineTop) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(1.dp)
                        .height(if (compact) 19.dp else 22.dp)
                        .background(HomePurple.copy(alpha = 0.36f))
                )
            }
            if (showLineBottom) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .width(1.dp)
                        .height(if (compact) 19.dp else 22.dp)
                        .background(HomePurple.copy(alpha = 0.36f))
                )
            }
            Box(
                modifier = Modifier
                    .size(if (compact) 27.dp else 30.dp)
                    .clip(CircleShape)
                    .background(item.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = null, tint = item.accent, modifier = Modifier.size(if (compact) 15.dp else 17.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(item.time, color = HomeMuted, fontSize = 9.sp, lineHeight = 12.sp, fontWeight = FontWeight.Medium)
            Text(item.title, color = HomeText, fontSize = if (compact) 12.sp else 13.sp, lineHeight = if (compact) 15.sp else 16.sp, fontWeight = FontWeight.SemiBold)
            Text(item.subtitle, color = HomeMuted, fontSize = 10.sp, lineHeight = 13.sp)
        }
        Box(
            modifier = Modifier
                .size(if (compact) 18.dp else 20.dp)
                .clip(CircleShape)
                .background(if (item.active) HomePurple else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (!item.active) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(color = inactiveRing, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.2.dp.toPx()))
                }
            }
        }
    }
}

@Composable
private fun CompanionCard(
    name: String,
    priorities: Int,
    pendingTasks: Int,
    compact: Boolean,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)) {
        Text("Tu acompañamiento", color = HomeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 96.dp else 108.dp)
                .cleanClickable(onProfileClick),
            shape = RoundedCornerShape(16.dp),
            color = HomeCard,
            border = BorderStroke(1.dp, HomeBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(if (compact) 50.dp else 56.dp)
                        .clip(CircleShape)
                        .background(HomePurple.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.FavoriteBorder, contentDescription = null, tint = HomeCompanionHeart, modifier = Modifier.size(31.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("Vas bien, $name.", color = HomeText, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Pequeño progreso cada día,\ngrandes resultados siempre.",
                        color = HomeSoftText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.Start) {
                    CompanionStat(Icons.Rounded.Star, "${priorities.coerceAtLeast(1)} prioridad\nacadémica", HomeYellow)
                    CompanionStat(Icons.Rounded.CheckCircle, "${pendingTasks.coerceAtLeast(0)} tareas\npendientes", HomePurple)
                }
            }
        }
    }
}

@Composable
private fun CompanionStat(icon: ImageVector, text: String, tint: Color) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        Text(text, color = HomeText, fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold)
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

private fun HomeSummary.priorityCount(): Int {
    var count = 0
    if (riskSubject != null && riskSubject.severity != SubjectRiskSeverity.STABLE) count += 1
    if (nextAcademicWork != null) count += 1
    return count.coerceAtLeast(1)
}

private fun Modifier.cleanClickable(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

private val HomeBackgroundBrush: Brush
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        Brush.verticalGradient(listOf(HomeBgTop, HomeBgMid, HomeBgBottom))
    } else {
        Brush.linearGradient(listOf(Color(0xFFF8F6FF), Color(0xFFFFFFFF)))
    }

private val HeroBrush: Brush
    @Composable get() = Brush.linearGradient(
        listOf(
            HomeHeroStart,
            HomeHeroMid,
            HomeHeroEnd
        )
    )

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
private val HomeHeroStart = Color(0xFF070718)
private val HomeHeroMid = Color(0xFF170A39)
private val HomeHeroEnd = Color(0xFF2E0B74)
private val HomeHeroGlow = Color(0xFF751CFF)
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
private val HomeNotebookTop = Color(0xFFB15CFF)
private val HomeNotebookBottom = Color(0xFF6225F6)
private val HomeNotebookBinding = Color(0xFF2B186B)
private val HomePenTop = Color(0xFFBC6CFF)
private val HomePenBottom = Color(0xFF5525D8)
private val HomePenTip = Color(0xFFD7C6FF)
private val HomeCompanionHeart = Color(0xFFC08CFF)

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
    generalAverage = 3.8,
    subjectsCount = 1,
    tasksToday = 0,
    overdueTasks = 0,
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
    weeklyExpenses = ExpenseSummary(
        transport = 0,
        food = 0,
        total = 0,
        chartValues = emptyList()
    ),
    weeklyExpenseTotal = 0,
    productivitySummary = "Vas bien, Pineda.",
    gradingScale = GradingScale.ZERO_TO_FIVE,
    enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES, AppModule.ACADEMIC_TEMPLATES)
)
