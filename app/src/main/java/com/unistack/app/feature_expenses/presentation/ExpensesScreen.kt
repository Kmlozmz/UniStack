@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_expenses.presentation

import com.unistack.app.core.utils.DayLabels

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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import kotlin.math.roundToInt

import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroup
private val ExpenseBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val ExpenseCard: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
private val ExpenseCardHigh: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
private val ExpenseCoral: Color
    @Composable get() = LocalSectionColors.current.expenses
private val ExpenseCoralDeep: Color
    @Composable get() = LocalSectionColors.current.expenses
private val ExpensePurple: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val ExpenseText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val ExpenseMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val ExpenseTrack: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
// Los filetes salen de SoftOutline, que mezcla la tarjeta con el color de texto y por tanto
// se comporta bien en cualquier tema. Antes en oscuro salían de OnPrimary al 6%, y OnPrimary
// es «contenido sobre el acento»: con un acento claro —el caso normal con Monet en oscuro—
// se resuelve a tinta oscura, y tinta oscura al 6% sobre un fondo oscuro no se ve. De ahí
// que los contornos hubieran desaparecido en esta pantalla.
private val ExpenseBorder: Color
    @Composable get() = if (LocalIsDarkTheme.current) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    }
private val ExpenseDivider: Color
    @Composable get() = if (LocalIsDarkTheme.current) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
private val ExpenseSelectedText: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val ExpenseNeutralIcon: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val ReferenceBars = listOf(28, 55, 35, 78, 32, 52, 40)

@Composable
fun ExpensesScreen(
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var expenseIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var showBudgetSheet by rememberSaveable { mutableStateOf(false) }
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var categoryFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPeriod by rememberSaveable { mutableStateOf(ExpensePeriodFilter.TODAY) }
    var selectedCategory by rememberSaveable { mutableStateOf<ExpenseCategory?>(null) }

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
    val previousTotal = remember(expenses, selectedPeriod) {
        previousTotalForPeriod(expenses, selectedPeriod)
    }
    val chartValues = remember(weeklyExpenses) { viewModel.weeklyChartValues(weeklyExpenses) }
    val periodTotal = selectedPeriodExpenses.sumOf { it.amount }
    val activeBudget = when (selectedPeriod) {
        // Un dia no tiene tope propio, asi que se mide contra el de la semana, que es el
        // marco mas corto que el perfil guarda.
        ExpensePeriodFilter.TODAY -> profile?.weeklyBudget ?: 0
        ExpensePeriodFilter.WEEK -> profile?.weeklyBudget ?: 0
        ExpensePeriodFilter.MONTH -> profile?.monthlyBudget ?: 0
        ExpensePeriodFilter.ALL -> (profile?.monthlyBudget ?: 0).takeIf { it > 0 } ?: (profile?.weeklyBudget ?: 0)
    }
    // Con «Hoy» el tope se compara con lo de la semana entera, no con lo de hoy: un tope
    // semanal contra el gasto de un dia diria que llevas el 3 % usado cada lunes.
    val budgetSpent = if (selectedPeriod == ExpensePeriodFilter.TODAY) {
        expenses.filter { ExpensePeriodFilter.WEEK.matches(it) }.sumOf { it.amount }
    } else {
        periodTotal
    }

    // Un Box normal: medía la pantalla solo para calcular el factor de encogimiento, y ya
    // no hay factor. Medir para nada obliga a una pasada de composición de más.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ExpenseBackground)
    ) {
        ExpensesContent(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = { selectedPeriod = it },
            selectedCategory = selectedCategory,
            categories = filterCategories,
            onCategorySelected = { selectedCategory = it },
            onCategoryClick = {
                categoryFeedback = null
                showCategorySheet = true
            },
            periodTotal = periodTotal,
            recordCount = selectedPeriodExpenses.size,
            previousTotal = previousTotal,
            budget = activeBudget,
            budgetSpent = budgetSpent,
            chartValues = chartValues,
            expenses = filteredExpenses,
            onAddExpenseClick = onAddExpenseClick,
            onEditExpenseClick = onEditExpenseClick,
            onDeleteExpenseClick = { expenseIdPendingDelete = it },
            onBudgetClick = { showBudgetSheet = true },
            bottomPadding = 118.dp
        )
    }

    if (showCategorySheet) {
        ExpenseCategorySheet(
            selectedCategory = selectedCategory,
            enabledCategories = enabledCategories,
            filterCategories = filterCategories,
            feedback = categoryFeedback,
            onFilterSelected = {
                selectedCategory = it
                categoryFeedback = null
                showCategorySheet = false
            },
            onToggleCategory = { category ->
                val updated = viewModel.toggleExpenseCategory(category)
                if (updated) {
                    if (category == selectedCategory && category in enabledCategories) {
                        selectedCategory = null
                    }
                    categoryFeedback = "Categorías actualizadas."
                } else {
                    categoryFeedback = "Debe quedar al menos una categoría activa."
                }
            },
            onDismiss = {
                showCategorySheet = false
                categoryFeedback = null
            }
        )
    }

    val currentProfile = profile
    if (showBudgetSheet && currentProfile != null) {
        ExpenseBudgetSheet(
            weeklyBudget = currentProfile.weeklyBudget,
            monthlyBudget = currentProfile.monthlyBudget,
            onDismiss = { showBudgetSheet = false },
            onSave = { weeklyInput, monthlyInput ->
                if (viewModel.updateBudgetSettings(weeklyInput, monthlyInput)) {
                    showBudgetSheet = false
                }
            }
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
    onCategoryClick: () -> Unit,
    periodTotal: Int,
    recordCount: Int,
    previousTotal: Int,
    budget: Int,
    budgetSpent: Int,
    chartValues: List<Int>,
    expenses: List<Expense>,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    onDeleteExpenseClick: (String) -> Unit,
    onBudgetClick: () -> Unit,
    bottomPadding: Dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = LocalInterfaceSpacing.current.screenHorizontal,
                top = 58.dp,
                end = LocalInterfaceSpacing.current.screenHorizontal,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { ExpensesHeader() }
            item {
                ExpensesHeroCard(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    amount = periodTotal,
                    recordCount = recordCount,
                    trendText = trendText(periodTotal, previousTotal),
                    budget = budget,
                    budgetProgress = if (budget > 0) (budgetSpent / budget.toFloat()).coerceIn(0f, 1f) else 0f,
                    chartValues = chartValues,
                    onBudgetClick = onBudgetClick
                )
            }
            item {
                ExpensesFilters(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = onCategorySelected,
                    onCategoryClick = onCategoryClick
                )
            }
            if (expenses.isEmpty()) {
                item { ExpensesEmptyState(period = selectedPeriod) }
            } else {
                item {
                    Text(
                        text = "Últimos gastos",
                        color = ExpenseText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(expenses, key = { it.id }) { expense ->
                    ExpenseListItem(
                        expense = expense,
                        onEditClick = { onEditExpenseClick(expense.id) },
                        onDeleteClick = { onDeleteExpenseClick(expense.id) }
                    )
                }
            }
        }

        RegisterExpenseButton(
            onClick = onAddExpenseClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Un FAB no es contenido que se desplaza: está anclado, así que la barra
                // flotante lo taparía para siempre. Sube por encima de ella.
                .padding(end = 20.dp, bottom = 20.dp)
        )
    }
}

@Composable
private fun ExpensesHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = "Gastos",
            color = ExpenseText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
        Text(
            text = "Registra gastos personales y académicos.",
            color = ExpenseMuted,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ExpensesHeroCard(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    amount: Int,
    recordCount: Int,
    trendText: String,
    budget: Int,
    budgetProgress: Float,
    chartValues: List<Int>,
    onBudgetClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 250.dp),
        shape = MaterialTheme.shapes.large,
        color = ExpenseCard,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val chartWidth = (maxWidth * 0.30f).coerceIn(88.dp, 104.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AccentCircleIcon(
                                icon = Icons.Rounded.AccountBalanceWallet,
                                iconColor = LocalSectionColors.current.onExpensesContainer,
                                backgroundColor = LocalSectionColors.current.expensesContainer,
                                size = 42.dp,
                                iconSize = 21.dp
                            )
                            // Solo el rotulo del tramo. Aqui habia un segundo selector de
                            // periodo, desplegable, que movia el mismo dato que el grupo de
                            // filtros de debajo: dos mandos para una sola cosa, y el de
                            // arriba tapaba media tarjeta al abrirse.
                            Text(
                                text = selectedPeriod.heroLabel,
                                color = ExpenseText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                        Text(
                            text = CurrencyFormatter.formatCop(amount),
                            color = ExpenseCoral,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "gastados",
                            color = ExpenseMuted,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                        ExpenseTrendLine(
                            recordCount = recordCount,
                            trendText = trendText,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    WeeklyMiniChart(
                        values = chartValues,
                        modifier = Modifier
                            .padding(top = 44.dp)
                            .width(chartWidth)
                            .height(86.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(13.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ExpenseDivider)
            )
            Spacer(modifier = Modifier.height(13.dp))
            BudgetRow(
                budget = budget,
                progress = budgetProgress,
                onClick = onBudgetClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExpenseTrendLine(
    recordCount: Int,
    trendText: String,
    modifier: Modifier = Modifier
) {
    val percent = trendText.substringBefore(" vs")
    val suffix = trendText.removePrefix(percent)
    Text(
        text = buildAnnotatedString {
            append(recordCountLabel(recordCount))
            // Sin tendencia que mostrar tampoco se pinta el separador, o quedaría un
            // «0 registros  •» colgando sin nada detrás.
            if (trendText.isNotBlank()) {
                append("  •  ")
                withStyle(SpanStyle(color = ExpenseCoral, fontWeight = FontWeight.SemiBold)) {
                    append(percent)
                }
                append(suffix)
            }
        },
        color = ExpenseMuted,
        style = MaterialTheme.typography.bodySmall,
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
    modifier: Modifier = Modifier
) {
    val labels = DayLabels.short
    val normalizedValues = values.take(7).let { current ->
        if (current.size == 7) current else current + List(7 - current.size) { 0 }
    }
    val hasData = normalizedValues.any { it > 0 }
    val bars = if (hasData) normalizedValues else List(7) { 1 }
    val max = bars.maxOrNull()?.takeIf { it > 0 } ?: 1

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ExpenseDivider)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { value ->
                    val normalized = if (hasData) value / max.toFloat() else 0.15f
                    Box(
                        modifier = Modifier
                            .width(9.dp)
                            .height((16f + normalized * 42f).dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (hasData) {
                                    Brush.verticalGradient(listOf(ExpenseCoral, ExpenseCoralDeep))
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            ExpenseCoral.copy(alpha = 0.38f),
                                            ExpenseCoralDeep.copy(alpha = 0.28f)
                                        )
                                    )
                                }
                            )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    color = ExpenseMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(9.dp)
                )
            }
        }
    }
}

@Composable
private fun BudgetRow(
    budget: Int,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBudget = budget > 0
    BoxWithConstraints(modifier = modifier) {
        val progressWidth = if (maxWidth < 300.dp) 58.dp else 96.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .cleanClickable(onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccentCircleIcon(
                icon = Icons.Rounded.TrackChanges,
                iconColor = ExpensePurple,
                backgroundColor = ExpensePurple.copy(alpha = 0.18f),
                size = 40.dp,
                iconSize = 21.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (hasBudget) "Presupuesto: ${CurrencyFormatter.formatCop(budget)}" else "Sin presupuesto",
                    color = ExpenseText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = if (hasBudget) "${(progress * 100).roundToInt()}% usado" else "Configurar presupuesto",
                    color = ExpensePurple,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            if (hasBudget) {
                BudgetProgress(
                    progress = progress,
                    modifier = Modifier.width(progressWidth)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = ExpenseText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BudgetProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    LinearWavyProgressIndicator(
        progress = { progress.coerceIn(0f, 1f) },
        modifier = modifier,
        color = ExpensePurple,
        trackColor = ExpenseTrack
    )
}

@Composable
private fun ExpensesFilters(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onCategoryClick: () -> Unit
) {
    // Anchos fijos de 206 y 156 puntos, y una rama aparte para pantallas de menos de 300:
    // el grupo reparte solo y el chip ocupa lo que mide su texto, asi que no hace falta ni
    // medir la pantalla ni escribir dos veces la misma fila.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PeriodSegmentedControl(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected,
            modifier = Modifier.weight(1f)
        )
        CategoryChip(
            selectedCategory = selectedCategory,
            onClick = onCategoryClick
        )
    }
}

@Composable
private fun PeriodSegmentedControl(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    // El mismo grupo conectado que Horario/Calendario y Materias/Tareas. Era una caja con
    // tres cajas dentro y la elegida tenida al 28 %, que se leia mas como un resalte que
    // como una eleccion.
    val options = listOf(
        ExpensePeriodFilter.ALL,
        ExpensePeriodFilter.WEEK,
        ExpensePeriodFilter.MONTH
    )
    ButtonGroup(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, period ->
            val interactionSource = remember { MutableInteractionSource() }
            val selected = selectedPeriod == period
            val shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }
            ToggleButton(
                checked = selected,
                onCheckedChange = { onPeriodSelected(period) },
                shapes = shapes,
                interactionSource = interactionSource,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 44.dp)
                    .animateWidth(interactionSource)
            ) {
                Text(period.label, maxLines = 1, softWrap = false)
            }
        }
    }
}

@Composable
private fun CategoryChip(
    selectedCategory: ExpenseCategory?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Con una categoria puesta el chip se rellena: el filtro activo se ve sin leerlo, que
    // es justo lo que un chip de filtro tiene que hacer.
    val active = selectedCategory != null
    val labelColor = if (active) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        ExpenseMuted
    }
    Surface(
        modifier = modifier
            .height(44.dp)
            .cleanClickable(onClick),
        shape = CircleShape,
        color = if (active) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = selectedCategory?.label() ?: "Categoria",
                color = labelColor,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = labelColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseCategorySheet(
    selectedCategory: ExpenseCategory?,
    enabledCategories: Set<ExpenseCategory>,
    filterCategories: List<ExpenseCategory>,
    feedback: String?,
    onFilterSelected: (ExpenseCategory?) -> Unit,
    onToggleCategory: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ExpenseBackground,
        contentColor = ExpenseText,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Categorías",
                    color = ExpenseText,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Filtra tus gastos o decide qué categorías aparecen al registrar.",
                    color = ExpenseMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Filtro actual",
                    color = ExpenseText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ExpenseCategorySheetOption(
                    label = "Todas las categorías",
                    icon = Icons.Rounded.Check,
                    selected = selectedCategory == null,
                    accent = ExpensePurple,
                    onClick = { onFilterSelected(null) }
                )
                filterCategories.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { category ->
                            ExpenseCategorySheetOption(
                                label = category.label(),
                                icon = category.expenseSheetIcon(),
                                selected = selectedCategory == category,
                                accent = ExpenseCoral,
                                onClick = { onFilterSelected(category) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Administrar categorías",
                    color = ExpenseText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                ExpenseCategory.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { category ->
                            val enabled = category in enabledCategories
                            ExpenseCategorySheetOption(
                                label = category.label(),
                                icon = category.expenseSheetIcon(),
                                selected = enabled,
                                accent = ExpenseCoral,
                                trailingCheck = enabled,
                                onClick = { onToggleCategory(category) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                feedback?.let { message ->
                    Text(
                        text = message,
                        color = if (message.startsWith("Debe")) ExpenseCoral else ExpenseMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ExpenseCategorySheetOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingCheck: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .cleanClickable(onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) accent.copy(alpha = 0.12f) else ExpenseCardHigh,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accent.copy(alpha = 0.62f) else ExpenseBorder
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accent else ExpenseMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = if (selected) ExpenseText else ExpenseMuted,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (trailingCheck) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Un color por categoría, sacado de los tonos del tema.
 *
 * La pantalla pintaba los seis cajones del mismo coral, que es el color de la sección y no
 * el del gasto. Con un tono por categoría la lista se lee de un vistazo.
 */
@Composable
private fun ExpenseCategory.expenseTone(): Color = when (this) {
    ExpenseCategory.TRANSPORT -> LocalSectionColors.current.schedule
    ExpenseCategory.FOOD -> LocalSectionColors.current.expenses
    ExpenseCategory.COPIES -> LocalSectionColors.current.atRisk
    ExpenseCategory.MATERIALS -> MaterialTheme.colorScheme.tertiary
    ExpenseCategory.OUTINGS -> LocalSectionColors.current.onTrack
    ExpenseCategory.OTHER -> MaterialTheme.colorScheme.outline
}

@Composable
private fun ExpenseCategory.expenseSheetIcon(): ImageVector {
    return when (this) {
        ExpenseCategory.TRANSPORT -> Icons.Rounded.DirectionsBus
        ExpenseCategory.FOOD -> Icons.Rounded.Restaurant
        ExpenseCategory.COPIES -> Icons.Rounded.ContentCopy
        ExpenseCategory.MATERIALS -> Icons.AutoMirrored.Rounded.MenuBook
        ExpenseCategory.OUTINGS -> Icons.Rounded.Celebration
        ExpenseCategory.OTHER -> Icons.Rounded.MoreHoriz
    }
}

@Composable
private fun ExpensesEmptyState(
    period: ExpensePeriodFilter
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = MaterialTheme.shapes.large,
        color = ExpenseCard,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ExpenseCardHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Wallet,
                    contentDescription = null,
                    tint = ExpenseNeutralIcon,
                    modifier = Modifier.size(34.dp)
                )
                Box(
                    modifier = Modifier
                        .offset(x = (-7).dp, y = (-22).dp)
                        .size(width = 4.dp, height = 9.dp)
                        .clip(CircleShape)
                        .background(ExpensePurple)
                )
                Box(
                    modifier = Modifier
                        .offset(x = 7.dp, y = (-23).dp)
                        .size(width = 4.dp, height = 9.dp)
                        .clip(CircleShape)
                        .background(ExpensePurple)
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Aún no hay gastos ${period.emptySuffix}",
                color = ExpenseText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Registra tu primer gasto para ver\ntu resumen y categorías.",
                color = ExpenseMuted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RegisterExpenseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .cleanClickable(onClick),
        shape = MaterialTheme.shapes.extraLarge,
        color = ExpenseCoral,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = contentColorOn(ExpenseCoral),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Registrar gasto",
                color = contentColorOn(ExpenseCoral),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseBudgetSheet(
    weeklyBudget: Int,
    monthlyBudget: Int,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var weeklyInput by rememberSaveable(weeklyBudget) {
        mutableStateOf(weeklyBudget.takeIf { it > 0 }?.toString().orEmpty())
    }
    var monthlyInput by rememberSaveable(monthlyBudget) {
        mutableStateOf(monthlyBudget.takeIf { it > 0 }?.toString().orEmpty())
    }
    val weeklyValue = weeklyInput.toIntOrNull() ?: if (weeklyInput.isBlank()) 0 else null
    val monthlyValue = monthlyInput.toIntOrNull() ?: if (monthlyInput.isBlank()) 0 else null
    val isValid = weeklyValue != null &&
        monthlyValue != null &&
        weeklyValue in 0..99_999_999 &&
        monthlyValue in 0..999_999_999

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ExpenseBackground,
        contentColor = ExpenseText,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Configurar presupuesto",
                    color = ExpenseText,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Define límites para que el resumen de Gastos tenga contexto real.",
                    color = ExpenseMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BudgetInputField(
                    label = "Semanal",
                    value = weeklyInput,
                    onValueChange = { weeklyInput = it.filter(Char::isDigit).take(9) },
                    modifier = Modifier.weight(1f)
                )
                BudgetInputField(
                    label = "Mensual",
                    value = monthlyInput,
                    onValueChange = { monthlyInput = it.filter(Char::isDigit).take(9) },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Puedes dejar un campo vacío para no usar presupuesto en ese periodo.",
                color = ExpenseMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    onSave(
                        (weeklyValue ?: 0).toString(),
                        (monthlyValue ?: 0).toString()
                    )
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExpenseCoral,
                    contentColor = contentColorOn(ExpenseCoral),
                    disabledContainerColor = ExpenseTrack,
                    disabledContentColor = ExpenseMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Guardar presupuesto", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun BudgetInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(72.dp),
        shape = MaterialTheme.shapes.medium,
        color = ExpenseCardHigh,
        border = BorderStroke(1.dp, ExpenseDivider),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = ExpenseMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$",
                    color = ExpenseMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = ExpenseText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(ExpensePurple),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (value.isBlank()) {
                            Text("0", color = ExpenseMuted.copy(alpha = 0.65f), fontSize = 17.sp)
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

@Composable
private fun ExpenseListItem(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Editar y borrar se van a la hoja que abre la fila.
    //
    // Eran dos botones de icono en cada fila: con diez gastos, veinte mandos en la lista, y
    // el de borrar a un dedo de distancia del de editar. Ahora la fila se toca entera y las
    // dos acciones salen abajo con su nombre escrito, como en el sheet de clase de Horario.
    var showActions by rememberSaveable(expense.id) { mutableStateOf(false) }

    if (showActions) {
        ExpenseActionsSheet(
            expense = expense,
            onDismiss = { showActions = false },
            onEditClick = {
                showActions = false
                onEditClick()
            },
            onDeleteClick = {
                showActions = false
                onDeleteClick()
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth().cleanClickable { showActions = true },
        shape = MaterialTheme.shapes.medium,
        color = ExpenseCard,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(
                start = 16.dp,
                top = 12.dp,
                end = 8.dp,
                bottom = 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // El icono dice de qué es el gasto. Los seis cajones llevaban el mismo billete
            // en el mismo coral, así que una lista de diez gastos era diez veces el mismo
            // dibujo y había que leer el rótulo de cada fila para distinguirlas.
            AccentCircleIcon(
                icon = expense.category.expenseSheetIcon(),
                iconColor = contentColorOn(expense.category.expenseTone()),
                backgroundColor = expense.category.expenseTone(),
                size = 46.dp,
                iconSize = 22.dp
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = expense.category.label(),
                    color = ExpenseText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ExpenseDateUtils.formatDisplay(expense.dateMillis),
                    color = ExpenseMuted,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
            Text(
                text = CurrencyFormatter.formatCop(expense.amount),
                color = ExpenseCoral,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
        }
    }
}

/** Qué se puede hacer con un gasto, dicho con palabras y no con dos iconos en la fila. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseActionsSheet(
    expense: Expense,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val tone = expense.category.expenseTone()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccentCircleIcon(
                    icon = expense.category.expenseSheetIcon(),
                    iconColor = contentColorOn(tone),
                    backgroundColor = tone,
                    size = 46.dp,
                    iconSize = 22.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = expense.category.label(),
                        color = ExpenseText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = ExpenseDateUtils.formatDisplay(expense.dateMillis) +
                            "  •  " + CurrencyFormatter.formatCop(expense.amount),
                        color = ExpenseMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            ExpenseActionRow(
                icon = Icons.Rounded.Edit,
                tone = MaterialTheme.colorScheme.primary,
                title = "Editar gasto",
                subtitle = "Categoría, monto y fecha",
                onClick = onEditClick
            )
            ExpenseActionRow(
                icon = Icons.Rounded.Delete,
                tone = MaterialTheme.colorScheme.error,
                title = "Eliminar gasto",
                subtitle = "Se borra del historial y de los totales",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun ExpenseActionRow(
    icon: ImageVector,
    tone: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .cleanClickable(onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccentCircleIcon(
            icon = icon,
            iconColor = tone,
            backgroundColor = tone.copy(alpha = 0.16f),
            size = 36.dp,
            iconSize = 19.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = ExpenseMuted,
                style = MaterialTheme.typography.bodySmall
            )
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

@Composable
private fun Modifier.cleanClickable(onClick: () -> Unit): Modifier {
    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

/** Cadena vacía si no hay periodo anterior: sin nada con qué comparar no hay tendencia. */
private fun trendText(total: Int, previousTotal: Int): String {
    // Antes devolvía «+0% vs anterior», que suena a que gastaste lo mismo que el periodo
    // pasado cuando en realidad no hay periodo pasado. Un 0% inventado es peor que
    // no decir nada.
    if (previousTotal <= 0) return ""
    val percent = (((total - previousTotal) / previousTotal.toFloat()) * 100).roundToInt()
    val sign = if (percent >= 0) "+" else ""
    return "$sign$percent% vs anterior"
}

private fun previousTotalForPeriod(
    expenses: List<Expense>,
    period: ExpensePeriodFilter
): Int {
    val today = ExpenseDateUtils.today()
    return when (period) {
        // La tendencia de hoy se mide contra ayer, que es el tramo anterior del mismo largo.
        ExpensePeriodFilter.TODAY -> {
            val yesterday = today.minusDays(1)
            expenses
                .filter { ExpenseDateUtils.fromMillis(it.dateMillis) == yesterday }
                .sumOf { it.amount }
        }
        ExpensePeriodFilter.WEEK -> {
            val currentStart = ExpenseDateUtils.startOfWeek(today)
            val previousStart = currentStart.minusDays(7)
            expenses
                .filter { expense ->
                    val date = ExpenseDateUtils.fromMillis(expense.dateMillis)
                    !date.isBefore(previousStart) && date.isBefore(currentStart)
                }
                .sumOf { it.amount }
        }
        ExpensePeriodFilter.MONTH -> {
            val previousMonth = today.minusMonths(1)
            expenses
                .filter { expense ->
                    val date = ExpenseDateUtils.fromMillis(expense.dateMillis)
                    date.month == previousMonth.month && date.year == previousMonth.year
                }
                .sumOf { it.amount }
        }
        ExpensePeriodFilter.ALL -> 0
    }
}

private fun recordCountLabel(count: Int): String =
    if (count == 1) "1 registro" else "$count registros"

/**
 * El tramo que suman la cifra, el grafico y la lista.
 *
 * «Hoy» es el que sale al abrir, y no «Todo»: lo primero que se viene a mirar es cuanto
 * llevas gastado hoy, no el acumulado historico, que solo crece y nunca dice nada nuevo.
 */
private enum class ExpensePeriodFilter(val label: String, val heroLabel: String, val emptySuffix: String) {
    TODAY("Hoy", "Hoy", "hoy"),
    WEEK("Semana", "Esta semana", "esta semana"),
    MONTH("Mes", "Este mes", "este mes"),
    ALL("Todo", "Todo", "todavía");

    fun matches(expense: Expense): Boolean {
        val today = ExpenseDateUtils.today()
        val date = ExpenseDateUtils.fromMillis(expense.dateMillis)
        return when (this) {
            TODAY -> date == today
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ExpenseBackground)
        ) {
            ExpensesContent(
                selectedPeriod = ExpensePeriodFilter.WEEK,
                onPeriodSelected = {},
                selectedCategory = null,
                categories = ExpenseCategory.entries,
                onCategorySelected = {},
                onCategoryClick = {},
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
                onBudgetClick = {},
                bottomPadding = 150.dp
            )
            ExpensesPreviewBottomNav(
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
    onCategoryClick: () -> Unit,
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
    onBudgetClick: () -> Unit,
    bottomPadding: Dp
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            top = 48.dp,
            end = 24.dp,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { ExpensesHeader() }
        item {
            ExpensesHeroCard(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                amount = periodTotal,
                recordCount = recordCount,
                trendText = trendText(periodTotal, previousTotal),
                budget = budget,
                budgetProgress = budgetProgress,
                chartValues = chartValues,
                onBudgetClick = onBudgetClick
            )
        }
        item {
            ExpensesFilters(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                selectedCategory = selectedCategory,
                categories = categories,
                onCategorySelected = onCategorySelected,
                onCategoryClick = onCategoryClick
            )
        }
        if (expenses.isEmpty()) {
            item { ExpensesEmptyState(period = selectedPeriod) }
        } else {
            item {
                Text(
                    text = "Últimos gastos",
                    color = ExpenseText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(expenses, key = { it.id }) { expense ->
                ExpenseListItem(
                    expense = expense,
                    onEditClick = { onEditExpenseClick(expense.id) },
                    onDeleteClick = { onDeleteExpenseClick(expense.id) }
                )
            }
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                RegisterExpenseButton(
                    onClick = onAddExpenseClick
                )
            }
        }
    }
}

@Composable
private fun ExpensesPreviewBottomNav(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(96.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = ExpenseCard,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PreviewBottomNavItem("Inicio", Icons.Rounded.Home, selected = false, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Materias", Icons.AutoMirrored.Rounded.MenuBook, selected = false, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Tareas", Icons.AutoMirrored.Rounded.Assignment, selected = false, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Gastos", Icons.Rounded.AccountBalanceWallet, selected = true, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Perfil", Icons.Rounded.Person, selected = false, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PreviewBottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
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
            modifier = Modifier.size(25.dp)
        )
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 5.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(width = 19.dp, height = 4.dp)
                .clip(CircleShape)
                .background(if (selected) ExpensePurple else Color.Transparent)
        )
    }
}
