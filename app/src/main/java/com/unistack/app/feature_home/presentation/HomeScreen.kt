package com.unistack.app.feature_home.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_home.domain.AcademicWorkSummary
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
    var quickModuleKeys by rememberSaveable {
        mutableStateOf(QuickModuleType.entries.map { it.key })
    }
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
                    compact = isCompact,
                    onOpenClick = openPriorityAction,
                    onDetailsClick = { showPriorityDetails = true }
                )
            }
            item {
                QuickModules(
                    subjectsCount = summary.subjectsCount,
                    tasksToday = summary.tasksToday,
                    overdueTasks = summary.overdueTasks,
                    weeklyExpenseTotal = summary.weeklyExpenseTotal,
                    notesCount = summary.openAcademicWorks,
                    compact = isCompact,
                    onSubjectsClick = onSeeAllSubjectsClick,
                    onTasksClick = onSeeTasksClick,
                    onExpensesClick = onSeeExpensesClick,
                    onNotesClick = onOpenTemplatesClick,
                    enabledModules = summary.enabledModules,
                    selectedModuleKeys = quickModuleKeys,
                    onSelectedModuleKeysChange = { quickModuleKeys = it }
                )
            }
            item {
                TodayTimeline(items = summary.todayItems, onTasksClick = onSeeTasksClick, compact = isCompact)
            }
            item {
                CompanionCard(
                    name = displayName,
                    insight = summary.companionInsight,
                    priorities = summary.priorityCount(),
                    pendingTasks = summary.pendingTasks,
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
    description: String,
    compact: Boolean,
    onOpenClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                            text = "PRIORIDAD DE HOY",
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
                            .height(if (compact) 32.dp else 34.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(listOf(HomeHeroButtonStart, HomeHeroButtonEnd)))
                            .cleanClickable(onOpenClick)
                            .padding(horizontal = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Abrir materia", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
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
                        color = Color.White.copy(alpha = 0.07f),
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
private fun QuickModules(
    subjectsCount: Int,
    tasksToday: Int,
    overdueTasks: Int,
    weeklyExpenseTotal: Int,
    notesCount: Int,
    compact: Boolean,
    onSubjectsClick: () -> Unit,
    onTasksClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onNotesClick: () -> Unit,
    enabledModules: Set<AppModule>,
    selectedModuleKeys: List<String>,
    onSelectedModuleKeysChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCustomizeDialog by rememberSaveable { mutableStateOf(false) }
    val subjectsSubtitle = when (subjectsCount) {
        0 -> "Crear primera"
        1 -> "1 activa"
        else -> "$subjectsCount activas"
    }
    val tasksSubtitle = when {
        overdueTasks > 0 -> "$overdueTasks vencida${if (overdueTasks == 1) "" else "s"}"
        tasksToday > 0 -> "$tasksToday hoy"
        else -> "Al día"
    }
    val expensesSubtitle = if (weeklyExpenseTotal > 0) "$${weeklyExpenseTotal / 1000} semana" else "Sin gastos"
    val notesSubtitle = if (notesCount > 0) "$notesCount activo${if (notesCount == 1) "" else "s"}" else "Sin apuntes"
    val availableTypes = remember(enabledModules) {
        QuickModuleType.entries.filter { type -> type.appModule == null || type.appModule in enabledModules }
    }
    val visibleTypes = remember(availableTypes, selectedModuleKeys) {
        availableTypes.filter { it.key in selectedModuleKeys }.ifEmpty { availableTypes.take(1) }
    }
    val cards = visibleTypes.map { type ->
        when (type) {
            QuickModuleType.SUBJECTS -> QuickModuleCardData("Materias", subjectsSubtitle, Icons.AutoMirrored.Rounded.MenuBook, HomePurple, onSubjectsClick)
            QuickModuleType.TASKS -> QuickModuleCardData("Tareas", tasksSubtitle, Icons.AutoMirrored.Rounded.EventNote, HomeTeal, onTasksClick)
            QuickModuleType.EXPENSES -> QuickModuleCardData("Gastos", expensesSubtitle, Icons.Rounded.Wallet, HomeCoral, onExpensesClick)
            QuickModuleType.NOTES -> QuickModuleCardData("Notas", notesSubtitle, Icons.Rounded.Description, HomeYellow, onNotesClick)
        }
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Módulos rápidos", color = HomeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Personaliza tu día",
                color = HomePurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.cleanClickable { showCustomizeDialog = true }
            )
        }
        if (cards.size > 3) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                items(cards, key = { it.title }) { card ->
                    QuickModuleCard(
                        title = card.title,
                        subtitle = card.subtitle,
                        icon = card.icon,
                        accent = card.accent,
                        onClick = card.onClick,
                        modifier = Modifier.width(if (compact) 112.dp else 124.dp),
                        compact = true
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(if (cards.size == 1) 0.dp else 12.dp), modifier = Modifier.fillMaxWidth()) {
                cards.forEach { card ->
                    QuickModuleCard(
                        title = card.title,
                        subtitle = card.subtitle,
                        icon = card.icon,
                        accent = card.accent,
                        onClick = card.onClick,
                        modifier = Modifier.weight(1f),
                        compact = compact
                    )
                }
            }
        }
    }

    if (showCustomizeDialog) {
        QuickModulesDialog(
            availableTypes = availableTypes,
            selectedKeys = selectedModuleKeys,
            onDismiss = { showCustomizeDialog = false },
            onToggle = { type ->
                val selectedAvailable = availableTypes.filter { it.key in selectedModuleKeys }
                val next = if (type.key in selectedModuleKeys) {
                    if (selectedAvailable.size <= 1) selectedModuleKeys else selectedModuleKeys - type.key
                } else {
                    (selectedModuleKeys + type.key).distinct()
                }
                onSelectedModuleKeysChange(next)
            }
        )
    }
}

@Composable
private fun QuickModulesDialog(
    availableTypes: List<QuickModuleType>,
    selectedKeys: List<String>,
    onDismiss: () -> Unit,
    onToggle: (QuickModuleType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Personaliza tu día", color = HomeText, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Elige qué accesos quieres ver en módulos rápidos.",
                    color = HomeMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                availableTypes.forEach { type ->
                    val checked = type.key in selectedKeys
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .cleanClickable { onToggle(type) }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = checked, onCheckedChange = { onToggle(type) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(type.title, color = HomeText, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(type.description, color = HomeMuted, fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Listo", color = HomePurple, fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = HomeCard,
        titleContentColor = HomeText,
        textContentColor = HomeMuted
    )
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
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "quickModulePressScale"
    )
    Surface(
        modifier = modifier
            .height(if (compact) 76.dp else 92.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
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

private data class QuickModuleCardData(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val onClick: () -> Unit
)

private enum class QuickModuleType(
    val key: String,
    val title: String,
    val description: String,
    val appModule: AppModule?
) {
    SUBJECTS("subjects", "Materias", "Promedios, notas y metas.", AppModule.GRADES),
    TASKS("tasks", "Tareas", "Pendientes y entregas cercanas.", AppModule.TASKS),
    EXPENSES("expenses", "Gastos", "Resumen de tu semana.", AppModule.EXPENSES),
    NOTES("notes", "Notas", "Trabajos y apuntes académicos.", AppModule.ACADEMIC_TEMPLATES)
}

@Composable
private fun TodayTimeline(
    items: List<HomeTimelineSummary>,
    onTasksClick: () -> Unit,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
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
                if (items.isEmpty()) {
                    TimelineEmptyRow(compact = compact)
                } else {
                    items.forEachIndexed { index, item ->
                        TimelineRow(item = item.toTimelineItem(), showLineTop = index > 0, showLineBottom = index < items.lastIndex, compact = compact)
                    }
                }
            }
        }
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
    insight: String,
    priorities: Int,
    pendingTasks: Int,
    compact: Boolean,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "companionPressScale"
    )
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)) {
        Text("Tu acompañamiento", color = HomeText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 96.dp else 108.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onProfileClick
                ),
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
                        insight,
                        color = HomeSoftText,
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
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
        Brush.verticalGradient(listOf(HomeBgTop, HomeBgMid, HomeBgBottom))
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
    @Composable get() = UniStackColors.Background
private val HomePrioritySheetSuggestion = Color(0xFF15182B)
private val HomePrioritySheetText = Color(0xFFF4F3FF)
private val HomePrioritySheetBody = Color(0xFFCDD2E3)
private val HomePrioritySheetMuted = Color(0xFF9EA6BA)
private val HomePrioritySheetSecondaryButton = Color(0xFF202232)
private val HomePrioritySheetIconCircle = Color(0xFF201044)
private val HomePrioritySheetSun = Color(0xFFFFD21F)
private val HomePrioritySheetAccentSoft = Color(0xFFA855F7)

private fun HomePriorityAction.actionLabel(): String {
    return when (this) {
        HomePriorityAction.SUBJECT -> "Abrir materia"
        HomePriorityAction.SUBJECTS -> "Ver materias"
        HomePriorityAction.TASKS -> "Ver mis tareas"
        HomePriorityAction.EXPENSES -> "Ver gastos"
        HomePriorityAction.TEMPLATES -> "Ver trabajos"
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
