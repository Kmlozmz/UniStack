package com.unistack.app.feature_expenses.presentation

import com.unistack.app.core.MainDispatcherRule
import com.unistack.app.feature_expenses.data.InMemoryExpensesRepository
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_user.data.InMemoryUserRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.VisualPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ExpensesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var expensesRepo: InMemoryExpensesRepository
    private lateinit var userRepo: InMemoryUserRepository
    private lateinit var viewModel: ExpensesViewModel

    private val todayInput: String = LocalDate.now().toString()

    @Before
    fun setUp() {
        expensesRepo = InMemoryExpensesRepository()
        userRepo = InMemoryUserRepository()
        viewModel = ExpensesViewModel(
            repository = expensesRepo,
            userRepository = userRepo
        )
    }

    @Test
    fun `addExpense con datos validos agrega el gasto`() {
        val added = viewModel.addExpense(
            category = ExpenseCategory.FOOD,
            amountInput = "15000",
            dateInput = todayInput
        )

        assertTrue(added)
        assertEquals(1, viewModel.expenses.value.size)
        assertEquals(15000, viewModel.expenses.value.first().amount)
    }

    @Test
    fun `addExpense con monto cero retorna false`() {
        val added = viewModel.addExpense(
            category = ExpenseCategory.TRANSPORT,
            amountInput = "0",
            dateInput = todayInput
        )

        assertFalse(added)
        assertTrue(viewModel.expenses.value.isEmpty())
    }

    @Test
    fun `addExpense con texto en monto retorna false`() {
        val added = viewModel.addExpense(
            category = ExpenseCategory.COPIES,
            amountInput = "no-es-número",
            dateInput = todayInput
        )

        assertFalse(added)
    }

    @Test
    fun `addExpense con fecha inválida retorna false`() {
        val added = viewModel.addExpense(
            category = ExpenseCategory.FOOD,
            amountInput = "5000",
            dateInput = "fecha-mala"
        )

        assertFalse(added)
    }

    @Test
    fun `updateExpense actualiza el monto`() {
        viewModel.addExpense(
            category = ExpenseCategory.MATERIALS,
            amountInput = "8000",
            dateInput = todayInput
        )
        val expenseId = viewModel.expenses.value.first().id

        val updated = viewModel.updateExpense(
            expenseId = expenseId,
            category = ExpenseCategory.OTHER,
            amountInput = "12000",
            dateInput = todayInput
        )

        assertTrue(updated)
        val stored = viewModel.expenses.value.first { it.id == expenseId }
        assertEquals(12000, stored.amount)
        assertEquals(ExpenseCategory.OTHER, stored.category)
    }

    @Test
    fun `deleteExpense elimina el gasto`() {
        viewModel.addExpense(
            category = ExpenseCategory.OUTINGS,
            amountInput = "30000",
            dateInput = todayInput
        )
        val expenseId = viewModel.expenses.value.first().id

        val deleted = viewModel.deleteExpense(expenseId)

        assertTrue(deleted)
        assertTrue(viewModel.expenses.value.isEmpty())
    }

    @Test
    fun `deleteExpense retorna false para id inexistente`() {
        val deleted = viewModel.deleteExpense("no-existe")

        assertFalse(deleted)
    }

    @Test
    fun `updateBudgetSettings sin perfil retorna false`() {
        val result = viewModel.updateBudgetSettings(
            weeklyBudgetInput = "50000",
            monthlyBudgetInput = "200000"
        )

        assertFalse(result)
    }

    @Test
    fun `updateBudgetSettings con perfil guarda los presupuestos`() {
        userRepo.saveUserProfile(testProfile())

        val result = viewModel.updateBudgetSettings(
            weeklyBudgetInput = "80000",
            monthlyBudgetInput = "300000"
        )

        assertTrue(result)
        val profile = userRepo.userProfile.value
        assertEquals(80000, profile!!.weeklyBudget)
        assertEquals(300000, profile.monthlyBudget)
    }

    @Test
    fun `toggleExpenseCategory agrega categoría cuando no estaba habilitada`() {
        val profile = testProfile().copy(enabledExpenseCategories = setOf(ExpenseCategory.FOOD))
        userRepo.saveUserProfile(profile)

        val result = viewModel.toggleExpenseCategory(ExpenseCategory.TRANSPORT)

        assertTrue(result)
        val stored = userRepo.userProfile.value!!
        assertTrue(ExpenseCategory.TRANSPORT in stored.enabledExpenseCategories)
    }

    @Test
    fun `toggleExpenseCategory no permite deshabilitar la última categoría`() {
        val profile = testProfile().copy(enabledExpenseCategories = setOf(ExpenseCategory.FOOD))
        userRepo.saveUserProfile(profile)

        val result = viewModel.toggleExpenseCategory(ExpenseCategory.FOOD)

        assertFalse(result)
        val stored = userRepo.userProfile.value!!
        assertTrue(ExpenseCategory.FOOD in stored.enabledExpenseCategories)
    }

    private fun testProfile() = UserProfile(
        userId = "local-user",
        preferredName = "Estudiante",
        educationLevel = EducationLevel.UNIVERSITY,
        careerOrProgram = "Ingeniería",
        studyArea = StudyArea.ENGINEERING_TECHNOLOGY,
        gradeLevel = null,
        gradingScale = GradingScale.ZERO_TO_FIVE,
        passingGrade = 3.0,
        targetAverage = 4.0,
        enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES),
        visualPreference = VisualPreference.SYSTEM,
        setupCompleted = true,
        createdAt = 1L,
        updatedAt = 1L
    )
}
