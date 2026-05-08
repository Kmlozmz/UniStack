package com.unistack.app.feature_expenses.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.core.AppContainer
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class ExpensesViewModel(
    private val repository: ExpensesRepository = AppContainer.expensesRepository
) : ViewModel() {
    val expenses: StateFlow<List<Expense>> = repository.expenses

    fun expenseById(expenseId: String): Expense? {
        return expenses.value.firstOrNull { it.id == expenseId }
    }

    fun addExpense(
        category: ExpenseCategory,
        amountInput: String,
        dateInput: String
    ): Boolean {
        val parsed = validatedExpenseInput(amountInput, dateInput) ?: return false
        val now = System.currentTimeMillis()
        repository.addExpense(
            Expense(
                id = "expense-${UUID.randomUUID()}",
                category = category,
                amount = parsed.amount,
                dateMillis = parsed.dateMillis,
                createdAt = now,
                updatedAt = now
            )
        )
        return true
    }

    fun updateExpense(
        expenseId: String,
        category: ExpenseCategory,
        amountInput: String,
        dateInput: String
    ): Boolean {
        val existing = expenseById(expenseId) ?: return false
        val parsed = validatedExpenseInput(amountInput, dateInput) ?: return false
        repository.updateExpense(
            existing.copy(
                category = category,
                amount = parsed.amount,
                dateMillis = parsed.dateMillis,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun deleteExpense(expenseId: String): Boolean {
        val exists = expenses.value.any { it.id == expenseId }
        if (!exists) return false
        repository.deleteExpense(expenseId)
        return true
    }

    fun weeklyExpenses(): List<Expense> {
        return expenses.value.filter { ExpenseDateUtils.isInCurrentWeek(it.dateMillis) }
    }

    fun categoryTotals(expenses: List<Expense>): Map<ExpenseCategory, Int> {
        return expenses
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    fun weeklyChartValues(expenses: List<Expense>): List<Int> {
        val start = ExpenseDateUtils.startOfWeek()
        return (0..6).map { dayOffset ->
            val date = start.plusDays(dayOffset.toLong())
            expenses
                .filter { ExpenseDateUtils.fromMillis(it.dateMillis) == date }
                .sumOf { it.amount }
        }
    }

    private fun validatedExpenseInput(amountInput: String, dateInput: String): ParsedExpenseInput? {
        val amount = amountInput.toIntOrNull() ?: return null
        if (amount !in 1..99_999_999) return null
        val date = ExpenseDateUtils.parseInput(dateInput) ?: return null
        return ParsedExpenseInput(
            amount = amount,
            dateMillis = ExpenseDateUtils.toMillis(date)
        )
    }
}

private data class ParsedExpenseInput(
    val amount: Int,
    val dateMillis: Long
)
