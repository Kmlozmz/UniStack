package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.MiniBarChart
import com.unistack.app.core.design.components.SectionHeader
import com.unistack.app.core.design.components.SubjectCard
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackBrandHeader
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_home.domain.TaskSummary
import com.unistack.app.feature_user.domain.AppModule
import coil.compose.AsyncImage

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
    modifier: Modifier = Modifier
) {
    val summary = uiState.summary
    val enabledModules = summary.enabledModules
    val showGrades = AppModule.GRADES in enabledModules
    val showTasks = AppModule.TASKS in enabledModules
    val showExpenses = AppModule.EXPENSES in enabledModules
    val showTemplates = AppModule.ACADEMIC_TEMPLATES in enabledModules
    val neededGrade = summary.neededGrade.takeIf { showGrades }
    val nextTask = summary.nextTask.takeIf { showTasks }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item { HomeHeader(photoUrl = summary.avatarPhotoUrl, onProfileClick = onProfileClick) }
        item { GreetingText(name = summary.userName) }
        item { HeroSummaryCard(summary = summary) }
        item {
            PriorityDashboardSection(
                summary = summary,
                showGrades = showGrades,
                showTasks = showTasks,
                showExpenses = showExpenses,
                showTemplates = showTemplates,
                onSubjectClick = onSubjectClick,
                onSeeTasksClick = onSeeTasksClick,
                onSeeExpensesClick = onSeeExpensesClick,
                onOpenTemplatesClick = onOpenTemplatesClick
            )
        }
        if (showTasks || showExpenses || showTemplates) {
            item {
                WeekOverviewSection(
                    summary = summary,
                    showTasks = showTasks,
                    showExpenses = showExpenses,
                    showTemplates = showTemplates
                )
            }
        }
        if (showGrades) {
            item {
                SubjectsSection(
                    subjects = summary.subjects,
                    gradingScale = summary.gradingScale,
                    onAddSubjectClick = onAddSubjectClick,
                    onSeeAllSubjectsClick = onSeeAllSubjectsClick,
                    onSubjectClick = onSubjectClick
                )
            }
        }
        if (neededGrade != null || nextTask != null) {
            item {
                NeededAndNextTaskRow(
                    neededGrade = neededGrade,
                    nextTask = nextTask,
                    gradingScale = summary.gradingScale
                )
            }
        }
        if (showExpenses) {
            item {
                ExpenseWeeklyCard(
                    expenses = summary.weeklyExpenses,
                    onSeeExpensesClick = onSeeExpensesClick
                )
            }
        }
        if (showTemplates) {
            item {
                AcademicTemplatesCard(onOpenTemplatesClick = onOpenTemplatesClick)
            }
        }
    }
}

@Composable
private fun HomeHeader(
    photoUrl: String?,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarDescription = if (photoUrl.isNullOrBlank()) "Perfil" else "Foto de perfil"

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniStackBrandHeader(symbolSize = 34.dp)
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFFD8C8),
                            Color(0xFFE6E0FF),
                            Color(0xFFDDEBFF)
                        )
                    )
                )
                .bounceClick(onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl.isNullOrBlank()) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = avatarDescription,
                    tint = UniStackColors.PrimaryDark,
                    modifier = Modifier.size(23.dp)
                )
            } else {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = avatarDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun GreetingText(name: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Hola, ",
            color = UniStackColors.TextPrimary,
            fontSize = 20.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = "$name 👋",
            color = UniStackColors.TextPrimary,
            fontSize = 20.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun HeroSummaryCard(summary: HomeSummary, modifier: Modifier = Modifier) {
    UniCard(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp),
        brush = Brush.linearGradient(
            listOf(
                UniStackColors.PrimaryLight,
                UniStackColors.SurfaceVariant,
                UniStackColors.Card
            )
        ),
        shape = AppShapes.LargeCard,
        tonalElevation = 7.dp,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = if (summary.generalAverage == null) {
                            "Organiza tu semestre"
                        } else {
                            "Panel actualizado"
                        },
                        color = UniStackColors.PrimaryDark,
                        fontSize = if (summary.generalAverage == null) 24.sp else 29.sp,
                        lineHeight = if (summary.generalAverage == null) 27.sp else 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (summary.generalAverage == null) {
                            "Agrega tus notas para\nempezar a ver tu progreso."
                        } else {
                            "Revisa tus prioridades\nantes de seguir."
                        },
                        color = UniStackColors.TextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                HeroIllustration(
                    modifier = Modifier
                        .width(128.dp)
                        .height(92.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroIllustration(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawOval(
                color = Color(0xFF9B7DFF).copy(alpha = 0.18f),
                topLeft = Offset(size.width * 0.04f, size.height * 0.20f),
                size = Size(size.width * 0.96f, size.height * 0.75f)
            )
            drawOval(
                color = Color(0xFF6B54F6).copy(alpha = 0.12f),
                topLeft = Offset(size.width * 0.35f, size.height * 0.08f),
                size = Size(size.width * 0.58f, size.height * 0.82f)
            )
            drawOval(
                color = UniStackColors.PrimaryDark.copy(alpha = 0.10f),
                topLeft = Offset(size.width * 0.42f, size.height * 0.76f),
                size = Size(size.width * 0.54f, size.height * 0.16f)
            )
        }
        Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 6.dp, y = (-8).dp)
                .size(31.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 11.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.86f))
            )
            Box(
                modifier = Modifier
                    .offset(x = 16.dp, y = (-30).dp)
                    .size(width = 34.dp, height = 20.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomEnd = 12.dp, bottomStart = 4.dp))
                    .background(Brush.linearGradient(listOf(UniStackColors.Primary, Color(0xFF8D73FF))))
                    .graphicsLayer(rotationZ = -5f)
            )
            StackBlock(width = 62.dp, height = 27.dp, colorTop = Color(0xFF6C50E8), colorBottom = Color(0xFF9C84FF))
            StackBlock(width = 76.dp, height = 27.dp, colorTop = Color(0xFF7658E8), colorBottom = Color(0xFFB6A6FF))
            StackBlock(width = 90.dp, height = 29.dp, colorTop = Color(0xFF1E95D9), colorBottom = Color(0xFF63D2EA))
        }
    }
}

@Composable
private fun StackBlock(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    colorTop: Color,
    colorBottom: Color
) {
    Box(
        modifier = Modifier
            .padding(top = 1.dp)
            .width(width)
            .height(height)
            .shadow(
                elevation = 9.dp,
                shape = RoundedCornerShape(50),
                ambientColor = Color(0x22000000),
                spotColor = Color(0x17000000)
            )
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(colorTop, colorBottom)))
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .fillMaxWidth(0.58f)
                .height(height * 0.24f)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.22f))
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.90f)
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.08f))
        )
    }
}

@Composable
private fun SubjectsSection(
    subjects: List<SubjectSummary>,
    gradingScale: com.unistack.app.feature_user.domain.GradingScale,
    onAddSubjectClick: () -> Unit,
    onSeeAllSubjectsClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = "Materias",
            actionText = "Ver todas",
            onActionClick = onSeeAllSubjectsClick
        )
        if (subjects.isEmpty()) {
            EmptySubjectsCard(onAddSubjectClick = onAddSubjectClick)
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                subjects.take(3).forEach { subject ->
                    val style = subjectStyle(subject.type)
                    SubjectCard(
                        name = subjectDisplayName(subject),
                        average = subject.average,
                        progress = subject.progress,
                        icon = style.icon,
                        accentColor = style.accent,
                        backgroundColor = style.background,
                        gradingScale = gradingScale,
                        modifier = Modifier.weight(1f),
                        onClick = { onSubjectClick(subject.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySubjectsCard(
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier.fillMaxWidth(),
        brush = Brush.linearGradient(listOf(UniStackColors.PrimaryLight, UniStackColors.GradientEnd)),
        shape = AppShapes.MediumCard,
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Aún no tienes materias.",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Crea tu primera materia para empezar.",
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = onAddSubjectClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Agregar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NeededAndNextTaskRow(
    neededGrade: com.unistack.app.feature_home.domain.NeededGradeSummary?,
    nextTask: com.unistack.app.feature_home.domain.TaskSummary?,
    gradingScale: com.unistack.app.feature_user.domain.GradingScale,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val singleCard = neededGrade == null || nextTask == null
        if (neededGrade != null) {
            CompactInfoCard(
                title = "Nota necesaria",
                icon = Icons.Rounded.TrackChanges,
                iconColor = UniStackColors.Yellow,
                background = Brush.linearGradient(listOf(UniStackColors.YellowLight, UniStackColors.GradientEnd)),
                modifier = if (singleCard) Modifier.fillMaxWidth() else Modifier.weight(1f)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Para terminar con\n")
                        append(GradingScaleUtils.formatGrade(neededGrade.targetAverage, gradingScale))
                        append(" en ")
                        append(neededGrade.subjectName)
                        append("\nnecesitas ")
                        withStyle(SpanStyle(color = UniStackColors.Yellow, fontWeight = FontWeight.ExtraBold)) {
                            append(GradingScaleUtils.formatGrade(neededGrade.neededGrade, gradingScale))
                        }
                    },
                    color = UniStackColors.TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (nextTask != null) {
            CompactInfoCard(
                title = "Próxima tarea",
                icon = Icons.AutoMirrored.Rounded.Assignment,
                iconColor = UniStackColors.Green,
                background = Brush.linearGradient(listOf(UniStackColors.GreenLight, UniStackColors.GradientEnd)),
                modifier = if (singleCard) Modifier.fillMaxWidth() else Modifier.weight(1f)
            ) {
                Text(
                    text = "${nextTask.title}\n${nextTask.dueText} · ${nextTask.estimatedTimeText}",
                    color = UniStackColors.TextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CompactInfoCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    background: Brush,
    modifier: Modifier = Modifier,
    body: @Composable () -> Unit
) {
    UniCard(
        modifier = modifier.height(124.dp),
        brush = background,
        shape = AppShapes.MediumCard,
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = UniStackColors.TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = UniStackColors.TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))
                body()
            }
        }
    }
}

@Composable
fun ExpenseWeeklyCard(
    expenses: ExpenseSummary?,
    onSeeExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp),
        brush = Brush.linearGradient(
            listOf(
                UniStackColors.PrimaryLight,
                UniStackColors.SurfaceVariant,
                UniStackColors.Card
            )
        ),
        shape = AppShapes.MediumCard,
        tonalElevation = 5.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(UniStackColors.PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = UniStackColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = "Gastos de la semana",
                    color = UniStackColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier.clickable(onClick = onSeeExpensesClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver detalles",
                        color = UniStackColors.Primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = UniStackColors.Primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
            if (expenses != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ExpenseLine("Transporte", expenses.transport)
                        ExpenseLine("Comida", expenses.food)
                    }
                    MiniBarChart(
                        values = expenses.chartValues,
                        modifier = Modifier.width(124.dp)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Aún no registras gastos esta semana.",
                        color = UniStackColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Registra un gasto para ver tu resumen.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseLine(label: String, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = UniStackColors.TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(82.dp)
        )
        Text(
            text = CurrencyFormatter.formatCop(amount),
            color = UniStackColors.Primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun AcademicTemplatesCard(
    onOpenTemplatesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier
            .fillMaxWidth()
            .height(118.dp),
        brush = Brush.linearGradient(
            listOf(
                UniStackColors.GreenLight,
                UniStackColors.GradientEnd
            )
        ),
        shape = AppShapes.MediumCard,
        tonalElevation = 5.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.Green),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Assignment,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Trabajos académicos",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Checklist, plantillas de ensayo y formato APA.",
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Row(
                modifier = Modifier.clickable(onClick = onOpenTemplatesClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Abrir", color = UniStackColors.Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = UniStackColors.Green, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private data class SubjectVisualStyle(
    val icon: ImageVector,
    val accent: Color,
    val background: Color
)

private fun subjectDisplayName(subject: SubjectSummary): String {
    return subject.name
}

private fun subjectStyle(type: SubjectVisualType): SubjectVisualStyle {
    return when (type) {
        SubjectVisualType.TEAL -> SubjectVisualStyle(
            icon = Icons.Rounded.Calculate,
            accent = UniStackColors.Teal,
            background = UniStackColors.TealLight
        )
        SubjectVisualType.BLUE -> SubjectVisualStyle(
            icon = Icons.Rounded.BarChart,
            accent = UniStackColors.Blue,
            background = UniStackColors.BlueLight
        )
        SubjectVisualType.CORAL -> SubjectVisualStyle(
            icon = Icons.Rounded.ChatBubble,
            accent = UniStackColors.Coral,
            background = UniStackColors.CoralLight
        )
        SubjectVisualType.PURPLE -> SubjectVisualStyle(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            accent = UniStackColors.Primary,
            background = UniStackColors.PrimaryLight
        )
        SubjectVisualType.GREEN -> SubjectVisualStyle(
            icon = Icons.Rounded.CheckCircle,
            accent = UniStackColors.Green,
            background = UniStackColors.GreenLight
        )
        SubjectVisualType.YELLOW -> SubjectVisualStyle(
            icon = Icons.Rounded.Star,
            accent = UniStackColors.Yellow,
            background = UniStackColors.YellowLight
        )
        SubjectVisualType.ROSE -> SubjectVisualStyle(
            icon = Icons.Rounded.ChatBubble,
            accent = Color(0xFFE84A8A),
            background = if (UniStackColors.IsDarkTheme) Color(0xFF3B1F2D) else Color(0xFFFFE4EF)
        )
        SubjectVisualType.INDIGO -> SubjectVisualStyle(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            accent = Color(0xFF4D5BD7),
            background = if (UniStackColors.IsDarkTheme) Color(0xFF20274A) else Color(0xFFE5E8FF)
        )
        SubjectVisualType.ORANGE -> SubjectVisualStyle(
            icon = Icons.Rounded.Calculate,
            accent = Color(0xFFF57C00),
            background = if (UniStackColors.IsDarkTheme) Color(0xFF3D2817) else Color(0xFFFFE8D3)
        )
        SubjectVisualType.CYAN -> SubjectVisualStyle(
            icon = Icons.Rounded.BarChart,
            accent = Color(0xFF00A6D6),
            background = if (UniStackColors.IsDarkTheme) Color(0xFF123444) else Color(0xFFDDF7FF)
        )
        SubjectVisualType.LIME -> SubjectVisualStyle(
            icon = Icons.Rounded.Check,
            accent = Color(0xFF7CB342),
            background = if (UniStackColors.IsDarkTheme) Color(0xFF243719) else Color(0xFFEAF7D7)
        )
        SubjectVisualType.SLATE -> SubjectVisualStyle(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            accent = Color(0xFF607D8B),
            background = if (UniStackColors.IsDarkTheme) Color(0xFF25313A) else Color(0xFFE8EEF2)
        )
    }
}
