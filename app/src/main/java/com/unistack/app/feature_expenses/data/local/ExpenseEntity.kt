package com.unistack.app.feature_expenses.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index("userId"),
        Index("category"),
        Index("dateMillis")
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val category: String,
    val amount: Int,
    val dateMillis: Long,
    val createdAt: Long,
    val updatedAt: Long
)
