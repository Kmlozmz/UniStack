package com.unistack.app.feature_expenses.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.components.UniEmptyStateCard
import com.unistack.app.core.design.components.UniFilterChipRow
import com.unistack.app.core.design.components.UniFilterOption
import com.unistack.app.core.design.components.UniScreenHeader
import com.unistack.app.core.design.components.MiniBarChart
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils

@Composable
fun ExpensesScreen(
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = viewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val weeklyExpenses = remember(expenses) { viewModel.weeklyExpenses() }
    val monthlyExpenses = remember(expenses) { viewModel.monthlyExpenses() }
    val previousWeekTotal = remember(expenses) { viewModel.previousWeekTotal() }
    val chartValues = remember(weeklyExpenses) { viewModel.weeklyChartValues(weeklyExpenses) }
    var expenseIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var selectedPeriod by remember { mutableStateOf(ExpensePeriodFilter.WEEK) }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    val enabledCategories = profile?.enabledExpenseCategories ?: ExpenseCategory.entries.toSet()
    val filterCategories = remember(expenses, enabledCategories) {
        (enabledCategories + expenses.map { it.category }).toList().sortedBy { it.ordinal }
    }
    val filteredExpenses = remember(expenses, selectedPeriod, selectedCategory) {
        expenses
            .filter { expense -> selectedPeriod.matches(expense) }
            .filter { expense -> selectedCategory == null || expense.category == selectedCategory }
    }
    val categoryTotals = remember(filteredExpenses) { viewModel.categoryTotals(filteredExpenses) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            UniScreenHeader(
                title = "Gastos",
                subtitle = "Registra gastos personales y académicos."
            )
        }
        item {
            WeeklyExpenseSummaryCard(
                weeklyExpenses = weeklyExpenses,
                monthlyExpenses = monthlyExpenses,
                previousWeekTotal = previousWeekTotal,
                weeklyBudget = profile?.weeklyBudget ?: 0,
                monthlyBudget = profile?.monthlyBudget ?: 0,
                alertThresholdPercent = profile?.expenseAlertThresholdPercent ?: 80,
                chartValues = chartValues
            )
        }
        item {
            UniFilterChipRow(
                options = ExpensePeriodFilter.entries.map { UniFilterOption(it, it.label) },
                selected = selectedPeriod,
                onSelected = { selectedPeriod = it }
            )
        }
        item {
            UniFilterChipRow(
                options = listOf(UniFilterOption<ExpenseCategory?>(null, "Todas")) +
                    filterCategories.map { UniFilterOption<ExpenseCategory?>(it, it.label()) },
                selected = selectedCategory,
                onSelected = { selectedCategory = it }
            )
        }
        if (categoryTotals.isNotEmpty()) {
            item {
                CategorySummaryCard(categoryTotals = categoryTotals)
            }
        }
        if (expenses.isEmpty()) {
            item {
                UniEmptyStateCard(
                    title = "Aún no tienes gastos reales.",
                    body = "Registra tu primer gasto para ver el resumen semanal y categorías.",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    actionText = "Registrar gasto",
                    onActionClick = onAddExpenseClick,
                    iconColor = UniStackColors.Coral
                )
            }
        } else if (filteredExpenses.isEmpty()) {
            item {
                UniEmptyStateCard(
                    title = "No hay gastos con este filtro.",
                    body = "Cambia el periodo o la categoría para ver otros registros.",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    iconColor = UniStackColors.Coral
                )
            }
        } else {
            item {
                Text(
                    "Registros",
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(filteredExpenses, key = { it.id }) { expense ->
                ExpenseListItem(
                    expense = expense,
                    onEditClick = { onEditExpenseClick(expense.id) },
                    onDeleteClick = { expenseIdPendingDelete = expense.id }
                )
            }
        }
        item {
            Button(
                onClick = onAddExpenseClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Coral),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AddCard, contentDescription = null)
                Spacer(modifier = Modifier.padding(3.dp))
                Text("Registrar gasto")
            }
        }
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
private fun WeeklyExpenseSummaryCard(
    weeklyExpenses: List<Expense>,
    monthlyExpenses: List<Expense>,
    previousWeekTotal: Int,
    weeklyBudget: Int,
    monthlyBudget: Int,
    alertThresholdPercent: Int,
    chartValues: List<Int>
) {
    val total = weeklyExpenses.sumOf { it.amount }
    val monthlyTotal = monthlyExpenses.sumOf { it.amount }
    val weeklyProgress = if (weeklyBudget > 0) total.toFloat() / weeklyBudget else 0f
    val monthlyProgress = if (monthlyBudget > 0) monthlyTotal.toFloat() / monthlyBudget else 0f
    val threshold = alertThresholdPercent / 100f
    val trendText = when {
        previousWeekTotal <= 0 && total > 0 -> "Primera semana con datos recientes."
        previousWeekTotal <= 0 -> "Sin tendencia suficiente todavía."
        total > previousWeekTotal -> "Subió ${CurrencyFormatter.formatCop(total - previousWeekTotal)} frente a la semana pasada."
        total < previousWeekTotal -> "Bajó ${CurrencyFormatter.formatCop(previousWeekTotal - total)} frente a la semana pasada."
        else -> "Gasto estable frente a la semana pasada."
    }
    val alertText = when {
        weeklyBudget > 0 && weeklyProgress >= 1f -> "Superaste el presupuesto semanal."
        weeklyBudget > 0 && weeklyProgress >= threshold -> "Estás cerca del límite semanal."
        monthlyBudget > 0 && monthlyProgress >= 1f -> "Superaste el presupuesto mensual."
        monthlyBudget > 0 && monthlyProgress >= threshold -> "Estás cerca del límite mensual."
        weeklyBudget > 0 || monthlyBudget > 0 -> "Presupuesto bajo control."
        else -> "Configura un presupuesto en Perfil para activar alertas."
    }

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.CoralLight,
        shape = AppShapes.LargeCard,
        contentPadding = PaddingValues(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.AccountBalanceWallet, contentDescription = null, tint = UniStackColors.Coral)
                Text("Esta semana", color = UniStackColors.TextSecondary)
                Text(
                    CurrencyFormatter.formatCop(total),
                    color = UniStackColors.TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(recordCountLabel(weeklyExpenses.size), color = UniStackColors.TextSecondary, fontSize = 13.sp)
                Text(trendText, color = UniStackColors.TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                Text(alertText, color = budgetAlertColor(alertText), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            MiniBarChart(
                values = chartValues,
                modifier = Modifier.width(132.dp)
            )
        }
    }
}

private fun budgetAlertColor(text: String) =
    if (text.contains("Superaste") || text.contains("cerca")) UniStackColors.Coral else UniStackColors.Green

private fun recordCountLabel(count: Int): String =
    if (count == 1) "1 registro" else "$count registros"

private enum class ExpensePeriodFilter(val label: String) {
    WEEK("Semana"),
    MONTH("Mes"),
    ALL("Todo");

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

@Composable
private fun CategorySummaryCard(categoryTotals: Map<ExpenseCategory, Int>) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Por categoría", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            categoryTotals.entries
                .sortedByDescending { it.value }
                .forEach { entry ->
                    CategoryLine(category = entry.key, amount = entry.value)
                }
        }
    }
}

@Composable
private fun CategoryLine(category: ExpenseCategory, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            category.label(),
            color = UniStackColors.TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            CurrencyFormatter.formatCop(amount),
            color = UniStackColors.Coral,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun EmptyExpensesCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Aún no tienes gastos reales.", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text("Registra tu primer gasto para ver el resumen semanal y categorías.", color = UniStackColors.TextSecondary)
        }
    }
}

@Composable
private fun ExpenseListItem(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(expense.category.label(), color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text(
                    ExpenseDateUtils.formatDisplay(expense.dateMillis),
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
            Text(
                CurrencyFormatter.formatCop(expense.amount),
                color = UniStackColors.Coral,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            IconButton(onClick = onEditClick) {
                Icon(Icons.Rounded.Edit, contentDescription = "Editar gasto")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Rounded.Delete, contentDescription = "Eliminar gasto", tint = UniStackColors.Coral)
            }
        }
    }
}
