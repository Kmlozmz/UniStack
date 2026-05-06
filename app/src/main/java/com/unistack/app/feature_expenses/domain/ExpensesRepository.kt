package com.unistack.app.feature_expenses.domain

import kotlinx.coroutines.flow.StateFlow

interface ExpensesRepository {
    val expenses: StateFlow<List<Expense>>

    fun addExpense(expense: Expense)
    fun updateExpense(expense: Expense)
    fun deleteExpense(expenseId: String)
}
