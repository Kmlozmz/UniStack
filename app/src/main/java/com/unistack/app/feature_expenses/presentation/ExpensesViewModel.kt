package com.unistack.app.feature_expenses.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.unistack.app.feature_user.domain.ExpenseChartStyle
import javax.inject.Inject
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: ExpensesRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    val expenses: StateFlow<List<Expense>> = repository.expenses
    val userProfile = userRepository.userProfile

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

    fun updateBudgetSettings(
        weeklyBudgetInput: String,
        monthlyBudgetInput: String
    ): Boolean {
        val current = userRepository.userProfile.value ?: return false
        val weeklyBudget = weeklyBudgetInput.toIntOrNull() ?: return false
        val monthlyBudget = monthlyBudgetInput.toIntOrNull() ?: return false
        if (weeklyBudget !in 0..99_999_999) return false
        if (monthlyBudget !in 0..999_999_999) return false
        userRepository.saveUserProfile(
            current.copy(
                weeklyBudget = weeklyBudget,
                monthlyBudget = monthlyBudget
            )
        )
        return true
    }

    fun toggleExpenseCategory(category: ExpenseCategory): Boolean {
        val current = userRepository.userProfile.value ?: return false
        val next = if (category in current.enabledExpenseCategories) {
            current.enabledExpenseCategories - category
        } else {
            current.enabledExpenseCategories + category
        }
        if (next.isEmpty()) return false
        userRepository.saveUserProfile(
            current.copy(enabledExpenseCategories = next)
        )
        return true
    }

    /**
     * Con que forma se dibuja el grafico de la tarjeta.
     *
     * Se guarda en el perfil como el presupuesto o las categorias, y se elige en la propia
     * pantalla de Gastos: es un ajuste de esta tarjeta, no del aspecto de la app.
     */
    fun setChartStyle(style: ExpenseChartStyle): Boolean {
        val current = userProfile.value ?: return false
        if (current.expenseChartStyle == style) return true
        userRepository.saveUserProfile(current.copy(expenseChartStyle = style))
        return true
    }

    fun previousWeekTotal(): Int {
        val start = ExpenseDateUtils.startOfWeek()
        val previousStart = start.minusDays(7)
        return expenses.value
            .filter { expense ->
                val date = ExpenseDateUtils.fromMillis(expense.dateMillis)
                !date.isBefore(previousStart) && date.isBefore(start)
            }
            .sumOf { it.amount }
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
