package com.unistack.app.feature_expenses.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE userId IN (:userIds) ORDER BY dateMillis DESC, createdAt DESC")
    fun observeExpensesForUsers(userIds: List<String>): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query(
        """
        UPDATE expenses
        SET category = :category,
            amount = :amount,
            dateMillis = :dateMillis,
            updatedAt = :updatedAt
        WHERE id = :expenseId AND userId IN (:userIds)
        """
    )
    suspend fun updateExpenseFields(
        expenseId: String,
        userIds: List<String>,
        category: String,
        amount: Int,
        dateMillis: Long,
        updatedAt: Long
    )

    @Query("DELETE FROM expenses WHERE id = :expenseId AND userId IN (:userIds)")
    suspend fun deleteExpenseById(expenseId: String, userIds: List<String>)
}
