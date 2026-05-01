package com.unistack.app.feature_expenses.domain

data class Expense(
    val id: String,
    val category: ExpenseCategory,
    val amount: Int,
    val dateText: String
)

enum class ExpenseCategory {
    TRANSPORT,
    FOOD,
    COPIES,
    MATERIALS,
    OUTINGS,
    OTHER
}
