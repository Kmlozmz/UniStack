@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_expenses.presentation


import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.theme.scrollBottomRoom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.rememberLeaveGuard
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.formatCurrency
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.annotation.StringRes
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
private val ExpenseFormBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val ExpenseFormBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant
private val ExpenseFormCoral: Color
    @Composable get() = LocalSectionColors.current.expenses
private val ExpenseFormText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val ExpenseFormMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val ExpenseFieldShape: Shape
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes.medium
private val longDateFormatter: DateTimeFormatter
    get() = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())

@Composable
fun AddExpenseScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = hiltViewModel(),
    expenseId: String? = null
) {
    val context = LocalContext.current
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val expense = expenseId?.let { id -> expenses.firstOrNull { it.id == id } }
    val isEditing = expenseId != null

    // Sin categoría de partida. Venía con «Comida» puesta, que ahorra un toque en el caso
    // frecuente pero hace igual de fácil guardar un transporte etiquetado como comida sin
    // darse cuenta. Obligar a elegir una evita el error silencioso.
    var category by rememberSaveable { mutableStateOf<ExpenseCategory?>(null) }
    var amount by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(ExpenseDateUtils.formatInput(ExpenseDateUtils.today())) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var initialized by rememberSaveable(expenseId) { mutableStateOf(false) }

    val hasUnsavedChanges = if (expense == null) {
        category != null || amount.isNotBlank()
    } else {
        category != expense.category ||
            amount != expense.amount.toString() ||
            date != ExpenseDateUtils.formatInput(ExpenseDateUtils.fromMillis(expense.dateMillis))
    }
    val requestLeave = rememberLeaveGuard(
        hasUnsavedChanges = hasUnsavedChanges,
        onLeave = onBackClick,
        message = if (expense == null) {
            stringResource(R.string.expenses_unsaved_leave_create)
        } else {
            stringResource(R.string.expenses_unsaved_leave_edit)
        }
    )

    val parsedAmount = amount.toIntOrNull()
    val parsedDate = ExpenseDateUtils.parseInput(date)
    val enabledCategories = profile?.enabledExpenseCategories ?: ExpenseCategory.entries.toSet()
    val visibleCategories = (enabledCategories + listOfNotNull(expense?.category)).toList().sortedBy { it.ordinal }
    val isValid = (!isEditing || expense != null) &&
        category != null &&
        parsedAmount != null &&
        parsedAmount in 1..99_999_999 &&
        parsedDate != null

    LaunchedEffect(expense?.id, expenseId) {
        if (initialized) return@LaunchedEffect
        if (expense != null) {
            category = expense.category
            amount = expense.amount.toString()
            date = ExpenseDateUtils.formatInput(ExpenseDateUtils.fromMillis(expense.dateMillis))
            initialized = true
        } else if (!isEditing) {
            initialized = true
        }
    }

    AddExpenseContent(
        isEditing = isEditing,
        expenseMissing = isEditing && expense == null,
        amount = amount,
        onAmountChange = {
            amount = it.filter(Char::isDigit).take(8)
            error = null
        },
        date = parsedDate ?: ExpenseDateUtils.today(),
        onDateClick = { showDatePicker = true },
        category = category,
        categories = visibleCategories,
        onCategorySelected = {
            category = it
            error = null
        },
        isValid = isValid,
        error = error,
        onBackClick = requestLeave,
        onSaveClick = {
            val chosenCategory = category ?: run {
                error = context.getString(R.string.expenses_choose_category_error)
                return@AddExpenseContent
            }
            val editingExpenseId = expenseId
            val saved = if (editingExpenseId != null) {
                viewModel.updateExpense(
                    expenseId = editingExpenseId,
                    category = chosenCategory,
                    amountInput = amount,
                    dateInput = date
                )
            } else {
                viewModel.addExpense(
                    category = chosenCategory,
                    amountInput = amount,
                    dateInput = date
                )
            }

            if (saved) {
                onBackClick()
            } else {
                error = context.getString(R.string.expenses_validation_error)
            }
        },
        modifier = modifier
    )

    if (showDatePicker) {
        UniDatePickerDialog(
            selectedDate = parsedDate ?: ExpenseDateUtils.today(),
            onDateSelected = { selected ->
                date = ExpenseDateUtils.formatInput(selected)
                error = null
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun AddExpenseContent(
    isEditing: Boolean,
    expenseMissing: Boolean,
    amount: String,
    onAmountChange: (String) -> Unit,
    date: LocalDate,
    onDateClick: () -> Unit,
    category: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory) -> Unit,
    isValid: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ExpenseFormBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 16.dp, bottom = scrollBottomRoom),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UniBackButton(onClick = onBackClick)

        AddExpenseHeader(isEditing = isEditing)
        ExpenseInfoCard(
            amount = amount,
            onAmountChange = onAmountChange,
            date = date,
            onDateClick = onDateClick,
            expenseMissing = expenseMissing
        )
        ExpenseCategorySection(
            selectedCategory = category,
            categories = categories,
            onCategorySelected = onCategorySelected
        )
        ExpensePreviewCard(
            category = category,
            amount = amount.toIntOrNull() ?: 0,
            date = date
        )
        error?.let {
            Text(
                text = it,
                color = ExpenseFormCoral,
                fontWeight = FontWeight.Bold
            )
        }
        SaveExpenseButton(
            text = if (isEditing) stringResource(R.string.action_save_changes) else stringResource(R.string.expenses_save_expense),
            enabled = isValid,
            onClick = onSaveClick
        )
        Spacer(modifier = Modifier.height(18.dp))
    }
}

@Composable
private fun AddExpenseHeader(isEditing: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (isEditing) stringResource(R.string.expenses_edit_expense) else stringResource(R.string.expenses_add_expense),
            color = ExpenseFormText,
            fontSize = 32.sp,
            lineHeight = 37.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.expenses_add_subtitle),
            color = ExpenseFormMuted,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ExpenseInfoCard(
    amount: String,
    onAmountChange: (String) -> Unit,
    date: LocalDate,
    onDateClick: () -> Unit,
    expenseMissing: Boolean
) {
    FormCard {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = ExpenseFormCoral,
                    modifier = Modifier.size(27.dp)
                )
                Text(
                    text = stringResource(R.string.expenses_info_title),
                    color = ExpenseFormText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (expenseMissing) {
                Text(
                    text = stringResource(R.string.expenses_not_found),
                    color = ExpenseFormMuted,
                    fontWeight = FontWeight.SemiBold
                )
            }
            ExpenseAmountField(
                amount = amount,
                onAmountChange = onAmountChange
            )
            ExpenseDateField(
                date = date,
                onClick = onDateClick
            )
        }
    }
}

@Composable
private fun ExpenseAmountField(
    amount: String,
    onAmountChange: (String) -> Unit
) {
    val currency = LocalAccessibilityPreferences.current.currency
    PremiumFieldContainer(minHeight = 78.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.expenses_amount_label),
                color = ExpenseFormMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${currency.symbol} ",
                    color = ExpenseFormText,
                    fontSize = 25.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (amount.isBlank()) {
                        Text(
                            text = "0",
                            color = ExpenseFormMuted.copy(alpha = 0.5f),
                            fontSize = 25.sp,
                            lineHeight = 29.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    BasicTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        textStyle = TextStyle(
                            color = ExpenseFormText,
                            fontSize = 25.sp,
                            lineHeight = 29.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        cursorBrush = SolidColor(ExpenseFormCoral),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ExpenseFormCoral.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = currency.code,
                        color = ExpenseFormCoral,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseDateField(
    date: LocalDate,
    onClick: () -> Unit
) {
    val dateFormat = LocalAccessibilityPreferences.current.dateFormat
    PremiumFieldContainer(
        minHeight = 70.dp,
        modifier = Modifier.cleanClickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = stringResource(R.string.expenses_date),
                    color = ExpenseFormMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${date.format(DateTimeFormatter.ofPattern(dateFormat.pattern))} (${longDateFormatter.format(date)})",
                    color = ExpenseFormText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = ExpenseFormMuted,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = ExpenseFormMuted,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}

@Composable
private fun ExpenseCategorySection(
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.expenses_category),
            color = ExpenseFormText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        categories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { option ->
                    ExpenseCategoryOption(
                        category = option,
                        selected = selectedCategory == option,
                        onClick = { onCategorySelected(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExpenseCategoryOption(
    category: ExpenseCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // La elegida se rellena con el color de su categoria, el mismo que lleva despues en la
    // lista. Antes eran seis contornos iguales y la marca era un tinte del 10 % que apenas
    // se distinguia de las otras cinco.
    val tone = category.expenseFormTone()
    Surface(
        modifier = modifier
            .height(56.dp)
            .cleanClickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) tone else MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        val content = if (selected) contentColorOn(tone) else MaterialTheme.colorScheme.onSurfaceVariant
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = category.icon(),
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = category.label(),
                color = content,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExpensePreviewCard(
    category: ExpenseCategory?,
    amount: Int,
    date: LocalDate
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = stringResource(R.string.expenses_preview),
            color = ExpenseFormText,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ExpenseFieldShape,
            color = Color.Transparent,
            border = BorderStroke(1.dp, ExpenseFormBorder),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(ExpenseFormCoral.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        // Sin categoría elegida la vista previa no se inventa una: enseña
                        // un hueco, que es exactamente lo que falta por decidir.
                        imageVector = category?.icon() ?: Icons.AutoMirrored.Rounded.HelpOutline,
                        contentDescription = null,
                        tint = if (category != null) ExpenseFormCoral else ExpenseFormMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = category?.label() ?: stringResource(R.string.expenses_choose_category),
                        color = if (category != null) ExpenseFormText else ExpenseFormMuted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${formatCurrency(amount)} · ${ExpenseDateUtils.formatDisplay(ExpenseDateUtils.toMillis(date), LocalAccessibilityPreferences.current.dateFormat)}",
                        color = ExpenseFormMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SaveExpenseButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    UniStackButton(
        text = text,
        onClick = onClick,
        enabled = enabled,
        containerColor = ExpenseFormCoral
    )
}

@Composable
private fun FormCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            content()
        }
    }
}

@Composable
private fun PremiumFieldContainer(
    minHeight: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/** El mismo color que lleva la categoria en la lista de gastos. */
@Composable
private fun ExpenseCategory.expenseFormTone(): Color = when (this) {
    ExpenseCategory.TRANSPORT -> LocalSectionColors.current.schedule
    ExpenseCategory.FOOD -> LocalSectionColors.current.expenses
    ExpenseCategory.COPIES -> LocalSectionColors.current.atRisk
    ExpenseCategory.MATERIALS -> MaterialTheme.colorScheme.tertiary
    ExpenseCategory.OUTINGS -> LocalSectionColors.current.onTrack
    ExpenseCategory.OTHER -> MaterialTheme.colorScheme.outline
}

@Composable
private fun ExpenseCategory.icon(): ImageVector {
    return when (this) {
        ExpenseCategory.TRANSPORT -> Icons.Rounded.DirectionsBus
        ExpenseCategory.FOOD -> Icons.Rounded.Restaurant
        ExpenseCategory.COPIES -> Icons.Rounded.ContentCopy
        ExpenseCategory.MATERIALS -> Icons.AutoMirrored.Rounded.MenuBook
        ExpenseCategory.OUTINGS -> Icons.Rounded.Celebration
        ExpenseCategory.OTHER -> Icons.Rounded.MoreHoriz
    }
}



@Composable
fun ExpenseCategory.label(): String = stringResource(labelRes())

@StringRes
fun ExpenseCategory.labelRes(): Int = when (this) {
    ExpenseCategory.TRANSPORT -> R.string.expense_cat_transport
    ExpenseCategory.FOOD -> R.string.expense_cat_food
    ExpenseCategory.COPIES -> R.string.expense_cat_copies
    ExpenseCategory.MATERIALS -> R.string.expense_cat_materials
    ExpenseCategory.OUTINGS -> R.string.expense_cat_outings
    ExpenseCategory.OTHER -> R.string.expense_cat_other
}

@Preview(name = "Registrar gasto dark", widthDp = 430, heightDp = 932, showBackground = true)
@Composable
private fun AddExpenseScreenPreview() {
    UniStackTheme(darkTheme = true) {
        AddExpenseContent(
            isEditing = false,
            expenseMissing = false,
            amount = "",
            onAmountChange = {},
            date = LocalDate.of(2026, 5, 20),
            onDateClick = {},
            category = ExpenseCategory.FOOD,
            categories = ExpenseCategory.entries,
            onCategorySelected = {},
            isValid = false,
            error = null,
            onBackClick = {},
            onSaveClick = {}
        )
    }
}
