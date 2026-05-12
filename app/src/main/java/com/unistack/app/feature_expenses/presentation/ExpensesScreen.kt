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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    val weeklyExpenses = remember(expenses) { viewModel.weeklyExpenses() }
    val categoryTotals = remember(weeklyExpenses) { viewModel.categoryTotals(weeklyExpenses) }
    val chartValues = remember(weeklyExpenses) { viewModel.weeklyChartValues(weeklyExpenses) }
    var expenseIdPendingDelete by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Gastos", color = UniStackColors.TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Registra gastos personales y académicos.", color = UniStackColors.TextSecondary)
            }
        }
        item {
            WeeklyExpenseSummaryCard(
                weeklyExpenses = weeklyExpenses,
                chartValues = chartValues
            )
        }
        if (categoryTotals.isNotEmpty()) {
            item {
                CategorySummaryCard(categoryTotals = categoryTotals)
            }
        }
        if (expenses.isEmpty()) {
            item {
                EmptyExpensesCard()
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
            items(expenses, key = { it.id }) { expense ->
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
        AlertDialog(
            onDismissRequest = { expenseIdPendingDelete = null },
            title = { Text("¿Eliminar gasto?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expenseId)
                        expenseIdPendingDelete = null
                    }
                ) {
                    Text("Eliminar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseIdPendingDelete = null }) {
                    Text("Cancelar")
                }
            },
            containerColor = UniStackColors.Card
        )
    }
}

@Composable
private fun WeeklyExpenseSummaryCard(
    weeklyExpenses: List<Expense>,
    chartValues: List<Int>
) {
    val total = weeklyExpenses.sumOf { it.amount }

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
            }
            MiniBarChart(
                values = chartValues,
                modifier = Modifier.width(132.dp)
            )
        }
    }
}

private fun recordCountLabel(count: Int): String =
    if (count == 1) "1 registro" else "$count registros"

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
