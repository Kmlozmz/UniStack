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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.MetricCard
import com.unistack.app.core.design.components.MiniBarChart
import com.unistack.app.core.design.components.QuickActionButton
import com.unistack.app.core.design.components.SectionHeader
import com.unistack.app.core.design.components.SubjectCard
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_home.domain.TaskSummary

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onAddGradeClick: () -> Unit,
    onNewTaskClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    onSeeAllSubjectsClick: () -> Unit,
    onSeeExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = uiState.summary

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        item { HomeHeader(photoUrl = summary.avatarPhotoUrl) }
        item { GreetingText(name = summary.userName) }
        item { HeroSummaryCard(summary = summary) }
        item {
            SubjectsSection(
                subjects = summary.subjects,
                onAddSubjectClick = onAddSubjectClick,
                onSeeAllSubjectsClick = onSeeAllSubjectsClick
            )
        }
        summary.neededGrade?.let { neededGrade ->
            item {
                NeededAndNextTaskRow(
                    neededGrade = neededGrade,
                    nextTask = summary.nextTask
                )
            }
        }
        item {
            ExpenseWeeklyCard(
                expenses = summary.weeklyExpenses,
                onSeeExpensesClick = onSeeExpensesClick
            )
        }
        item {
            QuickActionsRow(
                onAddGradeClick = onAddGradeClick,
                onNewTaskClick = onNewTaskClick,
                onAddExpenseClick = onAddExpenseClick
            )
        }
    }
}

@Composable
private fun HomeHeader(
    photoUrl: String?,
    modifier: Modifier = Modifier
) {
    val avatarDescription = if (photoUrl.isNullOrBlank()) "Perfil" else "Foto de perfil"

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniStackLogo(modifier = Modifier.size(34.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "UniStack",
            color = UniStackColors.TextPrimary,
            fontSize = 23.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.Notifications,
            contentDescription = "Notificaciones",
            tint = UniStackColors.TextPrimary,
            modifier = Modifier.size(23.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
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
                ),
            contentAlignment = Alignment.Center
        ) {
            // Future Google Sign-In can replace this placeholder when photoUrl is available.
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = avatarDescription,
                tint = UniStackColors.PrimaryDark,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun UniStackLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        fun stackPath(centerY: Float): Path {
            val w = size.width
            val h = size.height
            return Path().apply {
                moveTo(w * 0.50f, centerY - h * 0.18f)
                lineTo(w * 0.86f, centerY)
                lineTo(w * 0.50f, centerY + h * 0.18f)
                lineTo(w * 0.14f, centerY)
                close()
            }
        }

        drawPath(
            path = stackPath(size.height * 0.34f),
            brush = Brush.linearGradient(listOf(Color(0xFF8D73FF), UniStackColors.Primary))
        )
        drawPath(
            path = stackPath(size.height * 0.52f),
            brush = Brush.linearGradient(listOf(Color(0xFF6F58F5), Color(0xFF4A33D6)))
        )
        drawPath(
            path = stackPath(size.height * 0.70f),
            brush = Brush.linearGradient(listOf(Color(0xFF9584FF), Color(0xFF6B54F6)))
        )
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
            .height(188.dp),
        brush = Brush.linearGradient(
            listOf(
                Color(0xFFF0E7FF),
                Color(0xFFE8E0FF),
                Color(0xFFF8F5FF)
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
                        text = if (summary.subjectsCount == 0) {
                            "Organiza tu semestre ✨"
                        } else {
                            "Vas bien 🎉"
                        },
                        color = UniStackColors.PrimaryDark,
                        fontSize = if (summary.subjectsCount == 0) 24.sp else 29.sp,
                        lineHeight = if (summary.subjectsCount == 0) 27.sp else 32.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (summary.subjectsCount == 0) {
                            "Agrega tus materias para\nempezar a calcular tu promedio."
                        } else {
                            "Sigue así, ¡vas por\nbuen camino!"
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    value = String.format("%.1f", summary.generalAverage),
                    label = "Promedio\ngeneral",
                    icon = Icons.Rounded.Star,
                    iconColor = UniStackColors.Primary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    value = summary.subjectsCount.toString(),
                    label = "materias",
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    iconColor = UniStackColors.Blue,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    value = summary.tasksToday.toString(),
                    label = "tareas hoy",
                    icon = Icons.Rounded.CheckCircle,
                    iconColor = UniStackColors.Teal,
                    modifier = Modifier.weight(1f)
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
    onAddSubjectClick: () -> Unit,
    onSeeAllSubjectsClick: () -> Unit,
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
                        modifier = Modifier.weight(1f)
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
        brush = Brush.linearGradient(listOf(UniStackColors.PrimaryLight, Color.White)),
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
    neededGrade: com.unistack.app.feature_home.domain.NeededGradeSummary,
    nextTask: TaskSummary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CompactInfoCard(
            title = "Nota necesaria",
            icon = Icons.Rounded.TrackChanges,
            iconColor = UniStackColors.Yellow,
            background = Brush.linearGradient(listOf(UniStackColors.YellowLight, Color.White.copy(alpha = 0.88f))),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Para terminar con\n")
                    append(String.format("%.1f", neededGrade.targetAverage))
                    append(" en ")
                    append(neededGrade.subjectName)
                    append("\nnecesitas ")
                    withStyle(SpanStyle(color = UniStackColors.Yellow, fontWeight = FontWeight.ExtraBold)) {
                        append(String.format("%.1f", neededGrade.neededGrade))
                    }
                },
                color = UniStackColors.TextPrimary,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
        CompactInfoCard(
            title = "Próxima tarea",
            icon = Icons.AutoMirrored.Rounded.Assignment,
            iconColor = UniStackColors.Green,
            background = Brush.linearGradient(listOf(UniStackColors.GreenLight, Color.White.copy(alpha = 0.88f))),
            modifier = Modifier.weight(1f)
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
        modifier = modifier.height(94.dp),
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
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
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
    expenses: ExpenseSummary,
    onSeeExpensesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier
            .fillMaxWidth()
            .height(124.dp),
        brush = Brush.linearGradient(
            listOf(
                Color(0xFFF4EDFF),
                Color(0xFFFBF8FF),
                Color.White
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
                        .background(Color(0xFFE0D6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountBalanceWallet,
                        contentDescription = null,
                        tint = UniStackColors.PrimaryDark,
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
private fun QuickActionsRow(
    onAddGradeClick: () -> Unit,
    onNewTaskClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            text = "Nota",
            icon = Icons.Rounded.Add,
            backgroundColor = UniStackColors.PrimaryLight,
            contentColor = UniStackColors.Primary,
            onClick = onAddGradeClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            text = "Tarea",
            icon = Icons.Rounded.Check,
            backgroundColor = UniStackColors.BlueLight,
            contentColor = UniStackColors.Blue,
            onClick = onNewTaskClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            text = "Gasto",
            icon = Icons.Rounded.CreditCard,
            backgroundColor = UniStackColors.CoralLight,
            contentColor = UniStackColors.Coral,
            onClick = onAddExpenseClick,
            modifier = Modifier.weight(1f)
        )
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
            background = Color(0xFFFFE4EF)
        )
        SubjectVisualType.INDIGO -> SubjectVisualStyle(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            accent = Color(0xFF4D5BD7),
            background = Color(0xFFE5E8FF)
        )
        SubjectVisualType.ORANGE -> SubjectVisualStyle(
            icon = Icons.Rounded.Calculate,
            accent = Color(0xFFF57C00),
            background = Color(0xFFFFE8D3)
        )
        SubjectVisualType.CYAN -> SubjectVisualStyle(
            icon = Icons.Rounded.BarChart,
            accent = Color(0xFF00A6D6),
            background = Color(0xFFDDF7FF)
        )
        SubjectVisualType.LIME -> SubjectVisualStyle(
            icon = Icons.Rounded.Check,
            accent = Color(0xFF7CB342),
            background = Color(0xFFEAF7D7)
        )
        SubjectVisualType.SLATE -> SubjectVisualStyle(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            accent = Color(0xFF607D8B),
            background = Color(0xFFE8EEF2)
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenPreview() {
    UniStackTheme {
        HomeScreen(
            uiState = HomeUiState(),
            onAddGradeClick = {},
            onNewTaskClick = {},
            onAddExpenseClick = {},
            onAddSubjectClick = {},
            onSeeAllSubjectsClick = {},
            onSeeExpensesClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun HeroSummaryCardPreview() {
    UniStackTheme {
        Box(
            modifier = Modifier
                .background(UniStackColors.Background)
                .padding(20.dp)
        ) {
            HeroSummaryCard(summary = DemoData.homeSummary)
        }
    }
}

@Preview(showBackground = true, widthDp = 180)
@Composable
fun SubjectCardPreview() {
    UniStackTheme {
        val subject = DemoData.homeSummary.subjects.first()
        val style = subjectStyle(subject.type)
        SubjectCard(
            name = subject.name,
            average = subject.average,
            progress = subject.progress,
            icon = style.icon,
            accentColor = style.accent,
            backgroundColor = style.background,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
fun ExpenseWeeklyCardPreview() {
    UniStackTheme {
        Box(
            modifier = Modifier
                .background(UniStackColors.Background)
                .padding(20.dp)
        ) {
            ExpenseWeeklyCard(
                expenses = DemoData.homeSummary.weeklyExpenses,
                onSeeExpensesClick = {}
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 150)
@Composable
fun QuickActionButtonPreview() {
    UniStackTheme {
        QuickActionButton(
            text = "Agregar nota",
            icon = Icons.Rounded.Add,
            backgroundColor = UniStackColors.PrimaryLight,
            contentColor = UniStackColors.Primary,
            onClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}
