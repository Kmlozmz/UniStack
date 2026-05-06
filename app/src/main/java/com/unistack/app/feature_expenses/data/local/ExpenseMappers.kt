package com.unistack.app.feature_expenses.data.local

import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory

fun ExpenseEntity.toDomain(): Expense {
    val parsedCategory = runCatching { ExpenseCategory.valueOf(category) }
        .getOrDefault(ExpenseCategory.OTHER)

    return Expense(
        id = id,
        category = parsedCategory,
        amount = amount,
        dateMillis = dateMillis,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Expense.toEntity(userId: String): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        userId = userId,
        category = category.name,
        amount = amount,
        dateMillis = dateMillis,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
