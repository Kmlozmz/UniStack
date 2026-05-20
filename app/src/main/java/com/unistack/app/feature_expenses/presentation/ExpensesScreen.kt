package com.unistack.app.feature_expenses.presentation

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import kotlin.math.roundToInt

private val ExpenseBackground = Color(0xFF080B13)
private val ExpenseCard = Color(0xFF15141E)
private val ExpenseCardHigh = Color(0xFF1B1824)
private val ExpenseCoral = Color(0xFFFF746D)
private val ExpenseCoralDeep = Color(0xFFFF5F66)
private val ExpensePurple = Color(0xFF8B5CF6)
private val ExpenseText = Color(0xFFF8F7FC)
private val ExpenseMuted = Color(0xFFA9A7B7)
private val ExpenseTrack = Color(0xFF292633)
private val ReferenceBars = listOf(28, 55, 35, 78, 32, 52, 40)

@Composable
fun ExpensesScreen(
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = viewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var expenseIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var selectedPeriod by remember { mutableStateOf(ExpensePeriodFilter.WEEK) }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }

    val enabledCategories = profile?.enabledExpenseCategories ?: ExpenseCategory.entries.toSet()
    val filterCategories = remember(expenses, enabledCategories) {
        (enabledCategories + expenses.map { it.category }).toList().sortedBy { it.ordinal }
    }
    val selectedPeriodExpenses = remember(expenses, selectedPeriod) {
        expenses.filter { selectedPeriod.matches(it) }
    }
    val filteredExpenses = remember(selectedPeriodExpenses, selectedCategory) {
        selectedPeriodExpenses.filter { expense ->
            selectedCategory == null || expense.category == selectedCategory
        }
    }
    val weeklyExpenses = remember(expenses) { viewModel.weeklyExpenses() }
    val previousWeekTotal = remember(expenses) { viewModel.previousWeekTotal() }
    val chartValues = remember(weeklyExpenses) { viewModel.weeklyChartValues(weeklyExpenses) }
    val periodTotal = selectedPeriodExpenses.sumOf { it.amount }
    val activeBudget = when (selectedPeriod) {
        ExpensePeriodFilter.WEEK -> profile?.weeklyBudget ?: 0
        ExpensePeriodFilter.MONTH -> profile?.monthlyBudget ?: 0
        ExpensePeriodFilter.ALL -> (profile?.monthlyBudget ?: 0).takeIf { it > 0 } ?: (profile?.weeklyBudget ?: 0)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ExpenseBackground)
    ) {
        val scale = expenseScale(maxWidth)
        ExpensesContent(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { selectedPeriod = it },
            selectedCategory = selectedCategory,
            categories = filterCategories,
            onCategorySelected = { selectedCategory = it },
            periodTotal = periodTotal,
            recordCount = selectedPeriodExpenses.size,
            previousTotal = previousWeekTotal,
            budget = activeBudget,
            chartValues = chartValues,
            expenses = filteredExpenses,
            onAddExpenseClick = onAddExpenseClick,
            onEditExpenseClick = onEditExpenseClick,
            onDeleteExpenseClick = { expenseIdPendingDelete = it },
            scale = scale,
            bottomPadding = scaledDp(40f, scale)
        )
    }

    expenseIdPendingDelete?.let { expenseId ->
        UniConfirmDeleteDialog(
            title = "¿Eliminar gasto?",
            body = "Esta acción no se puede deshacer.",
            onConfirm = {
                viewModel.deleteExpense(expenseId)
                expenseIdPendingDelete = null
            },
            onDismiss = { expenseIdPendingDelete = null }
        )
    }
}

@Composable
private fun ExpensesContent(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    periodTotal: Int,
    recordCount: Int,
    previousTotal: Int,
    budget: Int,
    chartValues: List<Int>,
    expenses: List<Expense>,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    onDeleteExpenseClick: (String) -> Unit,
    scale: Float,
    bottomPadding: Dp
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = scaledDp(24f, scale),
            top = scaledDp(48f, scale),
            end = scaledDp(24f, scale),
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(scaledDp(20f, scale))
    ) {
        item { ExpensesHeader(scale = scale) }
        item {
            ExpensesHeroCard(
                periodLabel = selectedPeriod.heroLabel,
                amount = periodTotal,
                recordCount = recordCount,
                trendText = trendText(periodTotal, previousTotal),
                budget = budget,
                budgetProgress = if (budget > 0) (periodTotal / budget.toFloat()).coerceIn(0f, 1f) else 0f,
                chartValues = chartValues,
                scale = scale
            )
        }
        item {
            ExpensesFilters(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                selectedCategory = selectedCategory,
                categories = categories,
                onCategorySelected = onCategorySelected,
                scale = scale
            )
        }
        if (expenses.isEmpty()) {
            item { ExpensesEmptyState(period = selectedPeriod, scale = scale) }
        } else {
            item {
                Text(
                    text = "Últimos gastos",
                    color = ExpenseText,
                    fontSize = scaledSp(20f, scale),
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(expenses, key = { it.id }) { expense ->
                ExpenseListItem(
                    expense = expense,
                    onEditClick = { onEditExpenseClick(expense.id) },
                    onDeleteClick = { onDeleteExpenseClick(expense.id) },
                    scale = scale
                )
            }
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                RegisterExpenseButton(
                    onClick = onAddExpenseClick,
                    scale = scale
                )
            }
        }
    }
}

@Composable
private fun ExpensesHeader(scale: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(scaledDp(8f, scale))) {
        Text(
            text = "Gastos",
            color = ExpenseText,
            fontSize = scaledSp(34f, scale),
            lineHeight = scaledSp(38f, scale),
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = "Registra gastos personales y académicos.",
            color = ExpenseMuted,
            fontSize = scaledSp(16f, scale),
            lineHeight = scaledSp(22f, scale),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ExpensesHeroCard(
    periodLabel: String,
    amount: Int,
    recordCount: Int,
    trendText: String,
    budget: Int,
    budgetProgress: Float,
    chartValues: List<Int>,
    scale: Float
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = scaledDp(250f, scale)),
        shape = RoundedCornerShape(scaledDp(18f, scale)),
        color = ExpenseCard,
        border = BorderStroke(scaledDp(1f, scale), Color.White.copy(alpha = 0.06f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(scaledDp(20f, scale))
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val chartWidth = (maxWidth * 0.34f).coerceIn(105.dp, 115.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(scaledDp(12f, scale)),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(scaledDp(7f, scale))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(scaledDp(12f, scale))
                        ) {
                            AccentCircleIcon(
                                icon = Icons.Rounded.AccountBalanceWallet,
                                iconColor = ExpenseCoral,
                                backgroundColor = ExpenseCoral.copy(alpha = 0.16f),
                                size = scaledDp(42f, scale),
                                iconSize = scaledDp(21f, scale)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = periodLabel,
                                    color = ExpenseText,
                                    fontSize = scaledSp(18f, scale),
                                    lineHeight = scaledSp(22f, scale),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = ExpenseMuted,
                                    modifier = Modifier.size(scaledDp(18f, scale))
                                )
                            }
                        }
                        Text(
                            text = CurrencyFormatter.formatCop(amount),
                            color = ExpenseCoral,
                            fontSize = scaledSp(40f, scale),
                            lineHeight = scaledSp(42f, scale),
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            softWrap = false
                        )
                        Text(
                            text = "gastados",
                            color = ExpenseMuted,
                            fontSize = scaledSp(18f, scale),
                            lineHeight = scaledSp(21f, scale),
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                        ExpenseTrendLine(
                            recordCount = recordCount,
                            trendText = trendText,
                            scale = scale,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    WeeklyMiniChart(
                        values = chartValues,
                        scale = scale,
                        modifier = Modifier
                            .padding(top = scaledDp(44f, scale))
                            .width(chartWidth)
                            .height(scaledDp(86f, scale))
                    )
                }
            }

            Spacer(modifier = Modifier.height(scaledDp(13f, scale)))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(scaledDp(1f, scale))
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Spacer(modifier = Modifier.height(scaledDp(13f, scale)))
            BudgetRow(
                budget = budget,
                progress = budgetProgress,
                scale = scale,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExpenseTrendLine(
    recordCount: Int,
    trendText: String,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val percent = trendText.substringBefore(" vs")
    val suffix = trendText.removePrefix(percent)
    Text(
        text = buildAnnotatedString {
            append(recordCountLabel(recordCount))
            append("  •  ")
            withStyle(SpanStyle(color = ExpenseCoral, fontWeight = FontWeight.ExtraBold)) {
                append(percent)
            }
            append(suffix)
        },
        color = ExpenseMuted,
        fontSize = scaledSp(14f, scale),
        lineHeight = scaledSp(18f, scale),
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        softWrap = true,
        overflow = TextOverflow.Clip,
        modifier = modifier
    )
}

@Composable
private fun WeeklyMiniChart(
    values: List<Int>,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val labels = listOf("L", "M", "M", "J", "V", "S", "D")
    val normalizedValues = values.take(7).let { current ->
        if (current.size == 7) current else current + List(7 - current.size) { 0 }
    }
    val bars = if (normalizedValues.all { it == 0 }) ReferenceBars else normalizedValues
    val max = bars.maxOrNull()?.takeIf { it > 0 } ?: 1

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(scaledDp(66f, scale))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(scaledDp(1f, scale))
                            .background(Color.White.copy(alpha = 0.09f))
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = scaledDp(2f, scale)),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { value ->
                    val normalized = value / max.toFloat()
                    Box(
                        modifier = Modifier
                            .width(scaledDp(9f, scale))
                            .height(scaledDp(16f + normalized * 42f, scale))
                            .clip(RoundedCornerShape(scaledDp(5f, scale)))
                            .background(
                                Brush.verticalGradient(
                                    listOf(ExpenseCoral, ExpenseCoralDeep)
                                )
                            )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(scaledDp(6f, scale)))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = ExpenseMuted,
                    fontSize = scaledSp(11f, scale),
                    lineHeight = scaledSp(13f, scale),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(scaledDp(9f, scale))
                )
            }
        }
    }
}

@Composable
private fun BudgetRow(
    budget: Int,
    progress: Float,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val hasBudget = budget > 0
    BoxWithConstraints(modifier = modifier) {
        val progressWidth = if (maxWidth < 300.dp) scaledDp(58f, scale) else scaledDp(96f, scale)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(scaledDp(12f, scale))
        ) {
            AccentCircleIcon(
                icon = Icons.Rounded.TrackChanges,
                iconColor = ExpensePurple,
                backgroundColor = ExpensePurple.copy(alpha = 0.18f),
                size = scaledDp(40f, scale),
                iconSize = scaledDp(21f, scale)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(scaledDp(4f, scale))
            ) {
                Text(
                    text = if (hasBudget) "Presupuesto: ${CurrencyFormatter.formatCop(budget)}" else "Sin presupuesto",
                    color = ExpenseText,
                    fontSize = scaledSp(15f, scale),
                    lineHeight = scaledSp(18f, scale),
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = if (hasBudget) "${(progress * 100).roundToInt()}% usado" else "Configúralo en Perfil",
                    color = ExpensePurple,
                    fontSize = scaledSp(13f, scale),
                    lineHeight = scaledSp(16f, scale),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            if (hasBudget) {
                BudgetProgress(
                    progress = progress,
                    scale = scale,
                    modifier = Modifier.width(progressWidth)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = ExpenseText,
                modifier = Modifier.size(scaledDp(20f, scale))
            )
        }
    }
}

@Composable
private fun BudgetProgress(
    progress: Float,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(scaledDp(6f, scale))
            .clip(AppShapes.Pill)
            .background(ExpenseTrack)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0.03f, 1f))
                .clip(AppShapes.Pill)
                .background(
                    Brush.horizontalGradient(
                        listOf(ExpensePurple, ExpensePurple.copy(alpha = 0.74f))
                    )
                )
        )
    }
}

@Composable
private fun ExpensesFilters(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    scale: Float
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val segmentedWidth = scaledDp(206f, scale)
        val categoryWidth = scaledDp(156f, scale)

        if (maxWidth < 300.dp) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(scaledDp(12f, scale)),
                horizontalAlignment = Alignment.End
            ) {
                PeriodSegmentedControl(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    scale = scale,
                    modifier = Modifier.width(segmentedWidth)
                )
                CategoryChip(
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = onCategorySelected,
                    scale = scale,
                    modifier = Modifier.width(categoryWidth)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PeriodSegmentedControl(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    scale = scale,
                    modifier = Modifier.width(segmentedWidth)
                )
                CategoryChip(
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = onCategorySelected,
                    scale = scale,
                    modifier = Modifier.width(categoryWidth)
                )
            }
        }
    }
}

@Composable
private fun PeriodSegmentedControl(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(scaledDp(42f, scale)),
        shape = RoundedCornerShape(scaledDp(14f, scale)),
        color = ExpenseCard,
        border = BorderStroke(scaledDp(1f, scale), Color.White.copy(alpha = 0.06f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(modifier = Modifier.padding(scaledDp(3f, scale))) {
            listOf(
                ExpensePeriodFilter.ALL,
                ExpensePeriodFilter.WEEK,
                ExpensePeriodFilter.MONTH
            ).forEach { period ->
                val selected = selectedPeriod == period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(scaledDp(12f, scale)))
                        .background(if (selected) ExpensePurple.copy(alpha = 0.28f) else Color.Transparent)
                        .cleanClickable { onPeriodSelected(period) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.label,
                        color = if (selected) Color(0xFFA78BFA) else ExpenseMuted,
                        fontSize = scaledSp(13f, scale),
                        lineHeight = scaledSp(15f, scale),
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    scale: Float,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(scaledDp(42f, scale))
                .cleanClickable { expanded = true },
            shape = RoundedCornerShape(scaledDp(14f, scale)),
            color = ExpenseCard,
            border = BorderStroke(scaledDp(1f, scale), Color.White.copy(alpha = 0.06f)),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = scaledDp(12f, scale)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(scaledDp(6f, scale))
            ) {
                Text(
                    text = "Categoría: ${selectedCategory?.label() ?: "Todas"}",
                    color = ExpenseMuted,
                    fontSize = scaledSp(13f, scale),
                    lineHeight = scaledSp(15f, scale),
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = ExpenseMuted,
                    modifier = Modifier.size(scaledDp(14f, scale))
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ExpenseCardHigh)
        ) {
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.label()) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ExpensesEmptyState(
    period: ExpensePeriodFilter,
    scale: Float
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(scaledDp(250f, scale)),
        shape = RoundedCornerShape(scaledDp(18f, scale)),
        color = ExpenseCard,
        border = BorderStroke(scaledDp(1f, scale), Color.White.copy(alpha = 0.06f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = scaledDp(22f, scale)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(scaledDp(70f, scale))
                    .clip(RoundedCornerShape(scaledDp(18f, scale)))
                    .background(ExpenseCardHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Wallet,
                    contentDescription = null,
                    tint = Color(0xFFD8D6E3),
                    modifier = Modifier.size(scaledDp(34f, scale))
                )
                Box(
                    modifier = Modifier
                        .offset(x = scaledDp(-7f, scale), y = scaledDp(-22f, scale))
                        .size(width = scaledDp(4f, scale), height = scaledDp(9f, scale))
                        .clip(AppShapes.Pill)
                        .background(ExpensePurple)
                )
                Box(
                    modifier = Modifier
                        .offset(x = scaledDp(7f, scale), y = scaledDp(-23f, scale))
                        .size(width = scaledDp(4f, scale), height = scaledDp(9f, scale))
                        .clip(AppShapes.Pill)
                        .background(ExpensePurple)
                )
            }
            Spacer(modifier = Modifier.height(scaledDp(22f, scale)))
            Text(
                text = "Aún no hay gastos ${period.emptySuffix}",
                color = ExpenseText,
                fontSize = scaledSp(19f, scale),
                lineHeight = scaledSp(24f, scale),
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(scaledDp(10f, scale)))
            Text(
                text = "Registra tu primer gasto para ver\ntu resumen y categorías.",
                color = ExpenseMuted,
                fontSize = scaledSp(15f, scale),
                lineHeight = scaledSp(22f, scale),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RegisterExpenseButton(
    onClick: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(scaledDp(240f, scale))
            .height(scaledDp(50f, scale))
            .cleanClickable(onClick),
        shape = RoundedCornerShape(scaledDp(16f, scale)),
        color = ExpenseCoral,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = scaledDp(16f, scale)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(scaledDp(10f, scale), Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .size(scaledDp(24f, scale))
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Color(0xFF15131D),
                    modifier = Modifier.size(scaledDp(18f, scale))
                )
            }
            Text(
                text = "Registrar gasto",
                color = Color(0xFF15131D),
                fontSize = scaledSp(14f, scale),
                lineHeight = scaledSp(18f, scale),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
private fun ExpenseListItem(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    scale: Float
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(scaledDp(24f, scale)),
        color = ExpenseCard,
        border = BorderStroke(scaledDp(1f, scale), Color.White.copy(alpha = 0.06f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                start = scaledDp(16f, scale),
                top = scaledDp(12f, scale),
                end = scaledDp(8f, scale),
                bottom = scaledDp(12f, scale)
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AccentCircleIcon(
                icon = Icons.Rounded.Payments,
                iconColor = ExpenseCoral,
                backgroundColor = ExpenseCoral.copy(alpha = 0.16f),
                size = scaledDp(46f, scale),
                iconSize = scaledDp(23f, scale)
            )
            Column(
                modifier = Modifier
                    .padding(start = scaledDp(12f, scale))
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(scaledDp(3f, scale))
            ) {
                Text(
                    text = expense.category.label(),
                    color = ExpenseText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = scaledSp(16f, scale),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ExpenseDateUtils.formatDisplay(expense.dateMillis),
                    color = ExpenseMuted,
                    fontSize = scaledSp(13f, scale),
                    maxLines = 1
                )
            }
            Text(
                text = CurrencyFormatter.formatCop(expense.amount),
                color = ExpenseCoral,
                fontWeight = FontWeight.ExtraBold,
                fontSize = scaledSp(17f, scale),
                maxLines = 1
            )
            IconButton(onClick = onEditClick) {
                Icon(Icons.Rounded.Edit, contentDescription = "Editar gasto", tint = ExpenseMuted)
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Rounded.Delete, contentDescription = "Eliminar gasto", tint = UniStackColors.Coral)
            }
        }
    }
}

@Composable
private fun AccentCircleIcon(
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    size: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

private fun expenseScale(maxWidth: Dp): Float =
    (maxWidth.value / 430f).coerceIn(0.82f, 1f)

private fun scaledDp(value: Float, scale: Float): Dp = (value * scale).dp

private fun scaledSp(value: Float, scale: Float) = (value * scale).sp

@Composable
private fun Modifier.cleanClickable(onClick: () -> Unit): Modifier {
    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

private fun trendText(total: Int, previousTotal: Int): String {
    if (previousTotal <= 0) return "+0% vs anterior"
    val percent = (((total - previousTotal) / previousTotal.toFloat()) * 100).roundToInt()
    val sign = if (percent >= 0) "+" else ""
    return "$sign$percent% vs anterior"
}

private fun recordCountLabel(count: Int): String =
    if (count == 1) "1 registro" else "$count registros"

private enum class ExpensePeriodFilter(val label: String, val heroLabel: String, val emptySuffix: String) {
    WEEK("Semana", "Esta semana", "esta semana"),
    MONTH("Mes", "Este mes", "este mes"),
    ALL("Todo", "Todo", "todavía");

    fun matches(expense: Expense): Boolean {
        val today = ExpenseDateUtils.today()
        val date = ExpenseDateUtils.fromMillis(expense.dateMillis)
        return when (this) {
            WEEK -> ExpenseDateUtils.isInCurrentWeek(expense.dateMillis, today)
            MONTH -> date.month == today.month && date.year == today.year
            ALL -> true
        }
    }
}

@Preview(name = "Gastos mock 430", widthDp = 430, heightDp = 932, showBackground = true)
@Composable
private fun ExpensesScreenReferencePreview430() {
    ExpensesReferencePreview(widthDp = 430)
}

@Preview(name = "Gastos mock 360", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ExpensesScreenReferencePreview360() {
    ExpensesReferencePreview(widthDp = 360)
}

@Composable
private fun ExpensesReferencePreview(widthDp: Int) {
    UniStackTheme(darkTheme = true) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(ExpenseBackground)
        ) {
            val scale = (widthDp / 430f).coerceIn(0.82f, 1f)
            ExpensesContent(
                selectedPeriod = ExpensePeriodFilter.WEEK,
                onPeriodSelected = {},
                selectedCategory = null,
                categories = ExpenseCategory.entries,
                onCategorySelected = {},
                periodTotal = 48_500,
                recordCount = 6,
                previousTotal = 43_300,
                budget = 80_000,
                budgetProgress = 0.61f,
                chartValues = ReferenceBars,
                expenses = emptyList(),
                onAddExpenseClick = {},
                onEditExpenseClick = {},
                onDeleteExpenseClick = {},
                scale = scale,
                bottomPadding = scaledDp(150f, scale)
            )
            ExpensesPreviewBottomNav(
                scale = scale,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ExpensesContent(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    periodTotal: Int,
    recordCount: Int,
    previousTotal: Int,
    budget: Int,
    budgetProgress: Float,
    chartValues: List<Int>,
    expenses: List<Expense>,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    onDeleteExpenseClick: (String) -> Unit,
    scale: Float,
    bottomPadding: Dp
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = scaledDp(24f, scale),
            top = scaledDp(48f, scale),
            end = scaledDp(24f, scale),
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(scaledDp(20f, scale))
    ) {
        item { ExpensesHeader(scale = scale) }
        item {
            ExpensesHeroCard(
                periodLabel = selectedPeriod.heroLabel,
                amount = periodTotal,
                recordCount = recordCount,
                trendText = trendText(periodTotal, previousTotal),
                budget = budget,
                budgetProgress = budgetProgress,
                chartValues = chartValues,
                scale = scale
            )
        }
        item {
            ExpensesFilters(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                selectedCategory = selectedCategory,
                categories = categories,
                onCategorySelected = onCategorySelected,
                scale = scale
            )
        }
        if (expenses.isEmpty()) {
            item { ExpensesEmptyState(period = selectedPeriod, scale = scale) }
        } else {
            item {
                Text(
                    text = "Últimos gastos",
                    color = ExpenseText,
                    fontSize = scaledSp(20f, scale),
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(expenses, key = { it.id }) { expense ->
                ExpenseListItem(
                    expense = expense,
                    onEditClick = { onEditExpenseClick(expense.id) },
                    onDeleteClick = { onDeleteExpenseClick(expense.id) },
                    scale = scale
                )
            }
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                RegisterExpenseButton(
                    onClick = onAddExpenseClick,
                    scale = scale
                )
            }
        }
    }
}

@Composable
private fun ExpensesPreviewBottomNav(
    scale: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(scaledDp(96f, scale)),
        shape = RoundedCornerShape(topStart = scaledDp(28f, scale), topEnd = scaledDp(28f, scale)),
        color = Color(0xFF151320),
        border = BorderStroke(scaledDp(1f, scale), Color.White.copy(alpha = 0.06f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = scaledDp(10f, scale), vertical = scaledDp(10f, scale)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PreviewBottomNavItem("Inicio", Icons.Rounded.Home, selected = false, scale = scale, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Materias", Icons.AutoMirrored.Rounded.MenuBook, selected = false, scale = scale, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Tareas", Icons.AutoMirrored.Rounded.Assignment, selected = false, scale = scale, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Gastos", Icons.Rounded.AccountBalanceWallet, selected = true, scale = scale, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Perfil", Icons.Rounded.Person, selected = false, scale = scale, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PreviewBottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val color = if (selected) ExpensePurple else ExpenseMuted
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(scaledDp(25f, scale))
        )
        Text(
            text = label,
            color = color,
            fontSize = scaledSp(12f, scale),
            lineHeight = scaledSp(14f, scale),
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = scaledDp(5f, scale))
        )
        Box(
            modifier = Modifier
                .padding(top = scaledDp(6f, scale))
                .size(width = scaledDp(19f, scale), height = scaledDp(4f, scale))
                .clip(AppShapes.Pill)
                .background(if (selected) ExpensePurple else Color.Transparent)
        )
    }
}
