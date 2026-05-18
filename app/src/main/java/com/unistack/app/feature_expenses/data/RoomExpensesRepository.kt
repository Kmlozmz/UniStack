package com.unistack.app.feature_expenses.data

import com.unistack.app.feature_expenses.data.local.ExpenseDao
import com.unistack.app.feature_expenses.data.local.toDomain
import com.unistack.app.feature_expenses.data.local.toEntity
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.UserIds
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private val userId: String
        get() = UserIds.normalize(userRepository.currentUser.value.userId)

    private val userIds: List<String>
        get() = UserIds.storageIdsFor(userId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val expenses: StateFlow<List<Expense>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids ->
            expenseDao.observeExpensesForUsers(ids).map { entities ->
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
                userIds = userIds,
                category = expense.category.name,
                amount = expense.amount,
                dateMillis = expense.dateMillis,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun deleteExpense(expenseId: String) {
        scope.launch {
            expenseDao.deleteExpenseById(expenseId, userIds)
        }
    }
}
