package com.unistack.app.feature_expenses.data

import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryExpensesRepository : ExpensesRepository {
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    override val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    override fun addExpense(expense: Expense) {
        _expenses.update { current ->
            if (current.any { it.id == expense.id }) current else current + expense
        }
    }

    override fun updateExpense(expense: Expense) {
        _expenses.update { current ->
            current.map { existing -> if (existing.id == expense.id) expense else existing }
        }
    }

    override fun deleteExpense(expenseId: String) {
        _expenses.update { current -> current.filterNot { it.id == expenseId } }
    }
}
