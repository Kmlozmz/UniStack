package com.unistack.app.feature_expenses.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils

@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = viewModel(),
    expenseId: String? = null
) {
    BackHandler(onBack = onBackClick)

    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val expense = expenseId?.let { id -> expenses.firstOrNull { it.id == id } }
    val isEditing = expenseId != null

    var category by rememberSaveable(expenseId) { mutableStateOf(ExpenseCategory.FOOD) }
    var amount by rememberSaveable(expenseId) { mutableStateOf("") }
    var date by rememberSaveable(expenseId) { mutableStateOf(ExpenseDateUtils.formatInput(ExpenseDateUtils.today())) }
    var initialized by rememberSaveable(expenseId) { mutableStateOf(false) }
    var error by rememberSaveable(expenseId) { mutableStateOf<String?>(null) }

    val parsedAmount = amount.toIntOrNull()
    val parsedDate = ExpenseDateUtils.parseInput(date)
    val isValid = (!isEditing || expense != null) &&
        parsedAmount != null &&
        parsedAmount in 1..99_999_999 &&
        parsedDate != null

    LaunchedEffect(expense?.id, expenseId) {
        if (initialized) return@LaunchedEffect
        if (expense != null) {
            category = expense.category
            amount = expense.amount.toString()
            date = ExpenseDateUtils.formatInput(ExpenseDateUtils.fromMillis(expense.dateMillis))
            initialized = true
        } else if (!isEditing) {
            initialized = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
        }
        Text(
            if (isEditing) "Editar gasto" else "Registrar gasto",
            color = UniStackColors.TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        UniCard(
            modifier = Modifier.fillMaxWidth(),
            color = UniStackColors.CoralLight,
            shape = AppShapes.LargeCard,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.Rounded.CreditCard, contentDescription = null, tint = UniStackColors.Coral)
                if (isEditing && expense == null) {
                    Text("Gasto no encontrado.", color = UniStackColors.TextSecondary)
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() }.take(8)
                        error = null
                    },
                    label = { Text("Valor") },
                    placeholder = { Text("12000") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    isError = amount.isNotBlank() && (parsedAmount == null || parsedAmount !in 1..99_999_999),
                    supportingText = {
                        if (parsedAmount != null && parsedAmount > 0) {
                            Text(CurrencyFormatter.formatCop(parsedAmount))
                        }
                    }
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = {
                        date = it.take(10)
                        error = null
                    },
                    label = { Text("Fecha") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    isError = date.isNotBlank() && parsedDate == null,
                    supportingText = {
                        if (date.isNotBlank() && parsedDate == null) {
                            Text("Usa el formato YYYY-MM-DD")
                        }
                    }
                )
                Text("Categoría", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                ExpenseCategory.values().toList().chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { option ->
                            CategoryChip(
                                category = option,
                                selected = category == option,
                                onClick = {
                                    category = option
                                    error = null
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                error?.let {
                    Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(
            onClick = {
                val saved = if (isEditing && expenseId != null) {
                    viewModel.updateExpense(
                        expenseId = expenseId,
                        category = category,
                        amountInput = amount,
                        dateInput = date
                    )
                } else {
                    viewModel.addExpense(
                        category = category,
                        amountInput = amount,
                        dateInput = date
                    )
                }

                if (saved) {
                    onBackClick()
                } else {
                    error = "Revisa el valor, la fecha y la categoría antes de guardar."
                }
            },
            enabled = isValid,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Coral),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditing) "Guardar cambios" else "Guardar gasto")
        }
    }
}

@Composable
private fun CategoryChip(
    category: ExpenseCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier.bounceClick(onClick),
        color = if (selected) UniStackColors.CoralLight else UniStackColors.Card,
        shape = AppShapes.Pill,
        tonalElevation = if (selected) 5.dp else 1.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = category.label(),
            color = if (selected) UniStackColors.Coral else UniStackColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

fun ExpenseCategory.label(): String {
    return when (this) {
        ExpenseCategory.TRANSPORT -> "Transporte"
        ExpenseCategory.FOOD -> "Comida"
        ExpenseCategory.COPIES -> "Copias"
        ExpenseCategory.MATERIALS -> "Materiales"
        ExpenseCategory.OUTINGS -> "Salidas"
        ExpenseCategory.OTHER -> "Otros"
    }
}
