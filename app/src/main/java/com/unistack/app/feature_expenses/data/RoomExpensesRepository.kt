package com.unistack.app.feature_expenses.data

import com.unistack.app.feature_expenses.data.local.ExpenseDao
import com.unistack.app.feature_expenses.data.local.toDomain
import com.unistack.app.feature_expenses.data.local.toEntity
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomExpensesRepository(
    private val expenseDao: ExpenseDao,
    private val userRepository: UserRepository
) : ExpensesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val userId: String
        get() = userRepository.currentUser.value.userId.ifBlank { "local_user" }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val expenses: StateFlow<List<Expense>> = userRepository.currentUser
        .map { it.userId.ifBlank { "local_user" } }
        .flatMapLatest { uid ->
            expenseDao.observeExpensesForUser(uid).map { entities ->
                entities.map { it.toDomain() }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override fun addExpense(expense: Expense) {
        scope.launch {
            expenseDao.insertExpense(expense.toEntity(userId))
        }
    }

    override fun updateExpense(expense: Expense) {
        scope.launch {
            expenseDao.updateExpenseFields(
                expenseId = expense.id,
                userId = userId,
                category = expense.category.name,
                amount = expense.amount,
                dateMillis = expense.dateMillis,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun deleteExpense(expenseId: String) {
        scope.launch {
            expenseDao.deleteExpenseById(expenseId, userId)
        }
    }
}
