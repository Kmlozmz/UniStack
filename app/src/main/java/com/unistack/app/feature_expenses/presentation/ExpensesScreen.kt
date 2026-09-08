@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_expenses.presentation

import com.unistack.app.core.design.components.FilaDeslizable
import com.unistack.app.core.design.components.avisoDePresupuesto
import com.unistack.app.core.design.components.UniDivider
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.utils.DayLabels
import com.unistack.app.core.utils.desde
import com.unistack.app.core.design.theme.LocalAppearancePreferences

import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.toShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.unistack.app.core.design.components.UniChoiceRow
import com.unistack.app.feature_user.domain.ExpenseChartStyle
import com.unistack.app.core.design.theme.SectionLabelStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unistack.app.core.design.components.SectionHeader
import com.unistack.app.core.design.components.cleanClickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.formatCurrency
import com.unistack.app.feature_user.domain.CurrencyPreference
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import kotlin.math.roundToInt

import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.components.EvaluationBar
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
private val ExpenseBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val ExpenseCard: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
private val ExpenseCardHigh: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
private val ExpenseCoral: Color
    @Composable get() = LocalSectionColors.current.expenses
private val ExpenseCoralDeep: Color
    @Composable get() = LocalSectionColors.current.expenses
private val ExpensePurple: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val ExpenseText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val ExpenseMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val ExpenseTrack: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
// Los filetes salen de SoftOutline, que mezcla la tarjeta con el color de texto y por tanto
// se comporta bien en cualquier tema. Antes en oscuro salían de OnPrimary al 6%, y OnPrimary
// es «contenido sobre el acento»: con un acento claro —el caso normal con Monet en oscuro—
// se resuelve a tinta oscura, y tinta oscura al 6% sobre un fondo oscuro no se ve. De ahí
// que los contornos hubieran desaparecido en esta pantalla.
private val ExpenseBorder: Color
    @Composable get() = if (LocalIsDarkTheme.current) {
        MaterialTheme.colorScheme.outlineVariant
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)
    }
private val ExpenseDivider: Color
    @Composable get() = if (LocalIsDarkTheme.current) {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    }
private val ExpenseNeutralIcon: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val ReferenceBars = listOf(28, 55, 35, 78, 32, 52, 40)

@Composable
fun ExpensesScreen(
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    onInsightsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    var expenseIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var showBudgetSheet by rememberSaveable { mutableStateOf(false) }
    var showCategorySheet by rememberSaveable { mutableStateOf(false) }
    var categoryFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPeriod by rememberSaveable { mutableStateOf(ExpensePeriodFilter.ALL) }
    var selectedCategory by rememberSaveable { mutableStateOf<ExpenseCategory?>(null) }

    val enabledCategories = profile?.enabledExpenseCategories ?: ExpenseCategory.entries.toSet()
    val filterCategories = remember(expenses, enabledCategories) {
        (enabledCategories + expenses.map { it.category }).toList().sortedBy { it.ordinal }
    }
    val selectedPeriodExpenses = remember(expenses, selectedPeriod) {
        expenses.filter { selectedPeriod.matches(it) }
    }
    val filteredExpenses = remember(selectedPeriodExpenses, selectedCategory) {
        selectedPeriodExpenses.filter { expense ->
            selectedCategory == null || expense.category == selectedCategory
        }
    }
    val weeklyExpenses = remember(expenses) { viewModel.weeklyExpenses() }
    val previousTotal = remember(expenses, selectedPeriod) {
        previousTotalForPeriod(expenses, selectedPeriod)
    }
    val chartValues = remember(weeklyExpenses) { viewModel.weeklyChartValues(weeklyExpenses) }
    // Lo que pinta el anillo: cuanto va en cada categoria dentro del tramo elegido, de mayor a
    // menor y sin las que estan a cero, que solo meterian arcos invisibles.
    val categoryTotals = remember(selectedPeriodExpenses) {
        selectedPeriodExpenses
            .groupBy { it.category }
            .map { (categoria, lista) -> categoria to lista.sumOf { it.amount } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
    }
    val periodTotal = selectedPeriodExpenses.sumOf { it.amount }
    val activeBudget = when (selectedPeriod) {
        ExpensePeriodFilter.TODAY,
        ExpensePeriodFilter.WEEK -> profile?.weeklyBudget ?: 0
        ExpensePeriodFilter.MONTH -> profile?.monthlyBudget ?: 0
        ExpensePeriodFilter.ALL -> (profile?.monthlyBudget ?: 0).takeIf { it > 0 } ?: (profile?.weeklyBudget ?: 0)
    }
    // Con «Hoy» el tope se compara con lo de la semana entera, no con lo de hoy: un tope
    // semanal contra el gasto de un dia diria que llevas el 3 % usado cada lunes.
    val budgetSpent = if (selectedPeriod == ExpensePeriodFilter.TODAY) {
        expenses.filter { ExpensePeriodFilter.WEEK.matches(it) }.sumOf { it.amount }
    } else {
        periodTotal
    }

    // Un Box normal: medía la pantalla solo para calcular el factor de encogimiento, y ya
    // no hay factor. Medir para nada obliga a una pasada de composición de más.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ExpenseBackground)
    ) {
        ExpensesContent(
            selectedPeriod = selectedPeriod,
            // Sin «deseleccionar» aquí, y es a propósito.
            //
            // Lo tuvo un rato: volver a tocar el tramo activo lo devolvía a «Todo». Pero «Todo»
            // es una de las tres opciones, así que deshacer era en realidad elegir otra cosa, y
            // sobre «Todo» no hacía nada. Un gesto que unas veces hace algo y otras no es peor
            // que no tenerlo.
            //
            // Aquí siempre hay un tramo elegido —«Todo» es el neutro— y lo que se quita y se
            // pone son los dos chips de abajo, que para eso llevan su cruz.
            onPeriodSelected = { selectedPeriod = it },
            selectedCategory = selectedCategory,
            categories = filterCategories,
            onCategorySelected = { selectedCategory = it },
            onCategoryClick = {
                categoryFeedback = null
                showCategorySheet = true
            },
            periodTotal = periodTotal,
            recordCount = selectedPeriodExpenses.size,
            previousTotal = previousTotal,
            budget = activeBudget,
            budgetSpent = budgetSpent,
            chartValues = chartValues,
            chartStyle = profile?.expenseChartStyle ?: ExpenseChartStyle.BARS,
            onChartStyleChange = viewModel::setChartStyle,
            categoryTotals = categoryTotals,
            onChartClick = onInsightsClick,
            expenses = filteredExpenses,
            onAddExpenseClick = onAddExpenseClick,
            onEditExpenseClick = onEditExpenseClick,
            onDeleteExpenseClick = { expenseIdPendingDelete = it },
            onBudgetClick = { showBudgetSheet = true },
            bottomPadding = 118.dp
        )
    }

    if (showCategorySheet) {
        ExpenseCategorySheet(
            selectedCategory = selectedCategory,
            enabledCategories = enabledCategories,
            filterCategories = filterCategories,
            feedback = categoryFeedback,
            onFilterSelected = {
                selectedCategory = it
                categoryFeedback = null
                showCategorySheet = false
            },
            onToggleCategory = { category ->
                val updated = viewModel.toggleExpenseCategory(category)
                if (updated) {
                    if (category == selectedCategory && category in enabledCategories) {
                        selectedCategory = null
                    }
                    categoryFeedback = "Categorías actualizadas."
                } else {
                    categoryFeedback = "Debe quedar al menos una categoría activa."
                }
            },
            onDismiss = {
                showCategorySheet = false
                categoryFeedback = null
            }
        )
    }

    val currentProfile = profile
    if (showBudgetSheet && currentProfile != null) {
        ExpenseBudgetSheet(
            weeklyBudget = currentProfile.weeklyBudget,
            monthlyBudget = currentProfile.monthlyBudget,
            onDismiss = { showBudgetSheet = false },
            onSave = { weeklyInput, monthlyInput ->
                if (viewModel.updateBudgetSettings(weeklyInput, monthlyInput)) {
                    showBudgetSheet = false
                }
            }
        )
    }

    expenseIdPendingDelete?.let { expenseId ->
        UniConfirmDeleteDialog(
            title = "¿Eliminar gasto?",
            body = "Esta acción no se puede deshacer.",
            onConfirm = {
                viewModel.deleteExpense(expenseId)
                expenseIdPendingDelete = null
            },
            onDismiss = { expenseIdPendingDelete = null }
        )
    }
}

@Composable
private fun ExpensesContent(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onCategoryClick: () -> Unit,
    periodTotal: Int,
    recordCount: Int,
    previousTotal: Int,
    budget: Int,
    budgetSpent: Int,
    chartValues: List<Int>,
    chartStyle: ExpenseChartStyle,
    onChartStyleChange: (ExpenseChartStyle) -> Unit,
    categoryTotals: List<Pair<ExpenseCategory, Int>>,
    onChartClick: () -> Unit,
    expenses: List<Expense>,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    onDeleteExpenseClick: (String) -> Unit,
    onBudgetClick: () -> Unit,
    bottomPadding: Dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = LocalInterfaceSpacing.current.screenHorizontal,
                top = 58.dp,
                end = LocalInterfaceSpacing.current.screenHorizontal,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { ExpensesHeader() }
            item {
                ExpensesHeroCard(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    amount = periodTotal,
                    recordCount = recordCount,
                    trendText = trendText(periodTotal, previousTotal),
                    budget = budget,
                    budgetProgress = if (budget > 0) (budgetSpent / budget.toFloat()).coerceIn(0f, 1f) else 0f,
                    chartValues = chartValues,
                    chartStyle = chartStyle,
                    onChartStyleChange = onChartStyleChange,
                    categoryTotals = categoryTotals,
                    onChartClick = onChartClick,
                    onBudgetClick = onBudgetClick
                )
            }
            item {
                ExpensesFilters(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                    selectedCategory = selectedCategory,
                    categories = categories,
                    onCategorySelected = onCategorySelected,
                    onCategoryClick = onCategoryClick
                )
            }
            if (expenses.isEmpty()) {
                item { ExpensesEmptyState(period = selectedPeriod) }
            } else {
                item {
                    Text(
                        text = "Últimos gastos",
                        color = ExpenseText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                /*
                 * Un encabezado por día, y debajo lo que se gastó ese día.
                 *
                 * Antes cada fila repetía su propia fecha: con seis gastos del martes, la
                 * palabra «24 ago» salía seis veces y aún así había que leerlas todas para
                 * saber dónde acababa un día y empezaba el siguiente. Agrupando, la fecha se
                 * dice una vez y lo de cada día se ve como un bloque.
                 *
                 * Cada día lleva su total al lado: es la pregunta que se hace mirando un día
                 * suelto, y sumarla a ojo era el motivo para abrir la calculadora.
                 */
                expenses
                    .sortedByDescending { it.dateMillis }
                    .groupBy { ExpenseDateUtils.fromMillis(it.dateMillis) }
                    .forEach { (day, ofTheDay) ->
                        item(key = "dia-" + day.toString()) {
                            ExpenseDayGroup(
                                day = day,
                                expenses = ofTheDay,
                                onEditClick = onEditExpenseClick,
                                onDeleteClick = onDeleteExpenseClick
                            )
                        }
                    }
            }
        }

        RegisterExpenseButton(
            onClick = onAddExpenseClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Un FAB no es contenido que se desplaza: está anclado, así que la barra
                // flotante lo taparía para siempre. Sube por encima de ella.
                .padding(end = 20.dp, bottom = 20.dp)
        )
    }
}

@Composable
private fun ExpensesHeader() {
    SectionHeader(
        title = "Gastos",
        subtitle = "Registra gastos personales y académicos.",
        color = ExpenseText,
        supportColor = ExpenseMuted
    )
}

@Composable
private fun ExpensesHeroCard(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    amount: Int,
    recordCount: Int,
    trendText: String,
    budget: Int,
    budgetProgress: Float,
    chartValues: List<Int>,
    chartStyle: ExpenseChartStyle,
    onChartStyleChange: (ExpenseChartStyle) -> Unit,
    categoryTotals: List<Pair<ExpenseCategory, Int>>,
    onChartClick: () -> Unit,
    onBudgetClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 250.dp),
        shape = MaterialTheme.shapes.large,
        color = ExpenseCard,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            /*
             * La cifra manda a lo ancho, y el grafico va debajo.
             *
             * Estuvo a la derecha, en una columna de 100dp: siete barras en ese hueco salian
             * como palillos y el dia se distinguia por el rotulo, no por la barra. A lo ancho
             * la barra se toca con el dedo, que es lo que hace falta para poder elegir un dia.
             */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AccentCircleIcon(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    iconColor = LocalSectionColors.current.onExpensesContainer,
                    backgroundColor = LocalSectionColors.current.expensesContainer,
                    size = 42.dp,
                    iconSize = 21.dp
                )
                Text(
                    text = selectedPeriod.heroLabel,
                    color = ExpenseText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.weight(1f)
                )
                // La puerta a las tres lecturas, escrita.
                //
                // Estaba en el grafico entero, y con las barras tocables no habia forma de
                // acertar: se elegia un dia sin querer y habia que ir buscando un hueco muerto
                // entre barras para entrar. Un boton dice a donde va y no compite con nada.
                Surface(
                    onClick = onChartClick,
                    shape = CircleShape,
                    color = ExpenseCardHigh,
                    contentColor = ExpenseCoral
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp)
                    ) {
                        Text(
                            text = "Ver más",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = formatCurrency(amount),
                color = ExpenseCoral,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "gastados",
                color = ExpenseMuted,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(3.dp))
            ExpenseTrendLine(
                recordCount = recordCount,
                trendText = trendText,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            when (chartStyle) {
                ExpenseChartStyle.BARS -> WeeklyMiniChart(
                    values = chartValues,
                    modifier = Modifier.fillMaxWidth().height(96.dp)
                )

                ExpenseChartStyle.RING -> CategoryRingChart(
                    totals = categoryTotals,
                    modifier = Modifier.fillMaxWidth().height(96.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            ChartStylePicker(selected = chartStyle, onSelected = onChartStyleChange)

            Spacer(modifier = Modifier.height(13.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ExpenseDivider)
            )
            Spacer(modifier = Modifier.height(13.dp))
            /*
             * El aviso de haberse pasado, con la variante elegida en Movimiento.
             *
             * Va sobre la fila entera y no solo sobre la barra: pasarse del presupuesto es algo
             * que le pasa **al presupuesto**, y lo que hay que mirar es la cifra junto a la
             * barra. Es condicion y no disparo —mientras se este por encima, el aviso sigue—
             * porque un aviso de dinero que se apaga solo deja de avisar justo cuando mas
             * falta hace.
             */
            BudgetRow(
                budget = budget,
                progress = budgetProgress,
                onClick = onBudgetClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .avisoDePresupuesto(pasado = budget > 0 && budgetProgress >= 1f)
            )
        }
    }
}

@Composable
private fun ExpenseTrendLine(
    recordCount: Int,
    trendText: String,
    modifier: Modifier = Modifier
) {
    val percent = trendText.substringBefore(" vs")
    val suffix = trendText.removePrefix(percent)
    Text(
        text = buildAnnotatedString {
            append(recordCountLabel(recordCount))
            // Sin tendencia que mostrar tampoco se pinta el separador, o quedaría un
            // «0 registros  •» colgando sin nada detrás.
            if (trendText.isNotBlank()) {
                append("  •  ")
                withStyle(SpanStyle(color = ExpenseCoral, fontWeight = FontWeight.SemiBold)) {
                    append(percent)
                }
                append(suffix)
            }
        },
        color = ExpenseMuted,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        softWrap = true,
        overflow = TextOverflow.Clip,
        modifier = modifier
    )
}

@Composable
private fun WeeklyMiniChart(
    values: List<Int>,
    modifier: Modifier = Modifier
) {
    /*
     * Los rotulos y las barras se giran **a la vez**.
     *
     * `values` llega siempre empezando en lunes, que es como lo arma el dominio. Girar solo los
     * rotulos dejaria el gasto del lunes bajo la letra del domingo, que es peor que no ofrecer
     * el ajuste: seguiria pareciendo correcto.
     */
    val corte = LocalAppearancePreferences.current.firstDayOfWeek.isoDay - 1
    val labels = DayLabels.desde(java.time.DayOfWeek.of(corte + 1))
    // Cual esta elegido, o -1. La eleccion vive en la barra y no en el modelo: es una mirada,
    // no un ajuste, y no tiene por que sobrevivir a salir de la pantalla.
    var elegido by remember { mutableIntStateOf(-1) }
    val normalizedValues = values.take(7).let { current ->
        if (current.size == 7) current else current + List(7 - current.size) { 0 }
    }.let { semana -> semana.drop(corte) + semana.take(corte) }
    val hasData = normalizedValues.any { it > 0 }
    val bars = if (hasData) normalizedValues else List(7) { 1 }
    val max = bars.maxOrNull()?.takeIf { it > 0 } ?: 1

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(ExpenseDivider)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEachIndexed { indice, value ->
                    val normalized = if (hasData) value / max.toFloat() else 0.15f
                    /*
                     * Cada barra entra con el muelle espacial del tema, escalonada por su
                     * posicion: crecen de lunes a domingo con un rebote corto en lugar de
                     * aparecer ya puestas. El retraso es lo que hace que se lea como un gesto
                     * y no como siete animaciones a la vez.
                     */
                    var visible by remember(values) { mutableStateOf(false) }
                    LaunchedEffect(values) {
                        kotlinx.coroutines.delay(40L * indice)
                        visible = true
                    }
                    val alto by animateDpAsState(
                        targetValue = if (visible) (16f + normalized * 42f).dp else 4.dp,
                        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                        label = "alto de la barra"
                    )
                    val destacada = elegido == indice
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 3.dp)
                            .height(alto)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                when {
                                    !hasData -> Brush.verticalGradient(
                                        listOf(
                                            ExpenseCoral.copy(alpha = 0.38f),
                                            ExpenseCoralDeep.copy(alpha = 0.28f)
                                        )
                                    )
                                    elegido == -1 || destacada ->
                                        Brush.verticalGradient(listOf(ExpenseCoral, ExpenseCoralDeep))
                                    else -> Brush.verticalGradient(
                                        listOf(
                                            ExpenseCoral.copy(alpha = 0.34f),
                                            ExpenseCoralDeep.copy(alpha = 0.26f)
                                        )
                                    )
                                }
                            )
                            .cleanClickable { elegido = if (destacada) -1 else indice }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEachIndexed { indice, label ->
                Text(
                    text = label,
                    color = if (elegido == indice) ExpenseCoral else ExpenseMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (elegido == indice) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        // Con un dia elegido, el pie deja de ser una fila de letras y dice cuanto fue.
        AnimatedVisibility(visible = elegido >= 0) {
            Text(
                text = normalizedValues.getOrElse(elegido.coerceAtLeast(0)) { 0 }
                    .takeIf { it > 0 }
                    ?.let { formatCurrency(it) }
                    ?: "sin gastos",
                color = ExpenseCoral,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            )
        }
    }
}

/**
 * El anillo por categoria, con la galleta de [MaterialShapes] detras.
 *
 * Las barras dicen **cuando** gastaste y este dice **en que**: son dos preguntas distintas y
 * por eso conviven en vez de sustituirse. Los arcos entran uno detras de otro, en orden de
 * tamaño, para que el reparto se lea mientras se dibuja.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoryRingChart(
    totals: List<Pair<ExpenseCategory, Int>>,
    modifier: Modifier = Modifier
) {
    val total = totals.sumOf { it.second }
    val galleta = MaterialShapes.Cookie9Sided.toShape()
    // Los colores se leen aqui: dentro del Canvas no hay tema al que preguntarle.
    val arcos = totals.map { (categoria, monto) -> categoria.expenseTone() to monto }
    val arcosConNombre = totals
    val pista = ExpenseTrack
    var visible by remember(totals) { mutableStateOf(false) }
    LaunchedEffect(totals) { visible = true }
    val barrido by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "barrido del anillo"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
    Box(modifier = Modifier.size(96.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .background(ExpenseCoral.copy(alpha = 0.16f), galleta)
        )
        // El total, dentro de la galleta.
        //
        // Sin el, el centro es una mancha oscura y el anillo no dice de que es el reparto:
        // hay que sumar los tres numeros de la leyenda para saber el total. La forma existe
        // para sostener esta cifra, no como adorno.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val currency = LocalAccessibilityPreferences.current.currency
            Text(
                text = compactAmount(total, currency),
                color = ExpenseText,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = "GASTADO",
                color = ExpenseMuted,
                style = SectionLabelStyle,
                maxLines = 1
            )
        }
        Canvas(modifier = Modifier.size(92.dp)) {
            val grosor = 11.dp.toPx()
            val radio = (size.minDimension - grosor) / 2f
            val esquina = Offset((size.width - radio * 2) / 2f, (size.height - radio * 2) / 2f)
            val lado = androidx.compose.ui.geometry.Size(radio * 2, radio * 2)

            if (total <= 0) {
                drawArc(
                    color = pista,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = esquina,
                    size = lado,
                    style = Stroke(width = grosor, cap = StrokeCap.Round)
                )
                return@Canvas
            }

            var angulo = -90f
            arcos.forEach { (tono, monto) ->
                val porcion = monto * 360f / total
                drawArc(
                    color = tono,
                    startAngle = angulo,
                    // Cada arco deja un hueco de dos grados para que se distingan sin filete.
                    sweepAngle = (porcion - 2f).coerceAtLeast(0f) * barrido,
                    useCenter = false,
                    topLeft = esquina,
                    size = lado,
                    style = Stroke(width = grosor, cap = StrokeCap.Round)
                )
                angulo += porcion
            }
        }
    }
        // La leyenda: sin ella el anillo es bonito y no dice nada, porque los colores de las
        // categorias no se saben de memoria.
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            arcosConNombre.take(4).forEach { (categoria, monto) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(categoria.expenseTone())
                    )
                    Text(
                        text = categoria.label(),
                        color = ExpenseText,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp).weight(1f)
                    )
                    Text(
                        text = formatCurrency(monto),
                        color = ExpenseMuted,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Elegir entre barras y anillo, aqui mismo.
 *
 * Estuvo pensado para Ajustes › Apariencia y no encajaba: aquello cambia el aspecto de la app
 * entera, y esto solo el de una tarjeta. Puesto donde se ve el resultado, elegir cuesta un
 * toque y se juzga en el momento, en vez de ir a otra pantalla a decidir a ciegas.
 */
@Composable
private fun ChartStylePicker(
    selected: ExpenseChartStyle,
    onSelected: (ExpenseChartStyle) -> Unit
) {
    UniChoiceRow(
        selected = selected,
        options = listOf(
            UniSegmentedOption(value = ExpenseChartStyle.BARS, label = "Por día"),
            UniSegmentedOption(value = ExpenseChartStyle.RING, label = "Por categoría")
        ),
        onSelected = onSelected
    )
}

@Composable
private fun BudgetRow(
    budget: Int,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasBudget = budget > 0
    BoxWithConstraints(modifier = modifier) {
        val progressWidth = if (maxWidth < 300.dp) 58.dp else 96.dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .cleanClickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccentCircleIcon(
                icon = Icons.Rounded.TrackChanges,
                iconColor = ExpensePurple,
                backgroundColor = ExpensePurple.copy(alpha = 0.18f),
                size = 40.dp,
                iconSize = 21.dp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (hasBudget) "Presupuesto: ${formatCurrency(budget)}" else "Sin presupuesto",
                    color = ExpenseText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                Text(
                    text = if (hasBudget) "${(progress * 100).roundToInt()}% usado" else "Configurar presupuesto",
                    color = ExpensePurple,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            if (hasBudget) {
                BudgetProgress(
                    progress = progress,
                    modifier = Modifier.width(progressWidth)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = ExpenseText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BudgetProgress(
    progress: Float,
    modifier: Modifier = Modifier
) {
    EvaluationBar(
        fraction = progress.toDouble(),
        modifier = modifier,
        color = ExpensePurple,
        trackColor = ExpenseTrack
    )
}

@Composable
private fun ExpensesFilters(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onCategoryClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PeriodSegmentedControl(
            selectedPeriod = selectedPeriod,
            onPeriodSelected = onPeriodSelected,
            modifier = Modifier.fillMaxWidth()
        )
        CategoryFilterMenu(
            selectedCategory = selectedCategory,
            categories = categories,
            onCategorySelected = onCategorySelected,
            onManageClick = onCategoryClick
        )
    }
}

/**
 * El filtro de categoría, en un desplegable.
 *
 * **La flecha de al lado ahora significa algo.** Era una pastilla con una flecha hacia abajo que
 * abría una hoja a pantalla completa: la flecha prometía una lista corta debajo y lo que salía
 * era otra pantalla. Un menú es lo que la flecha dice, y elegir una categoría pasa de tres
 * toques a dos.
 *
 * Administrar cuáles aparecen al registrar sigue estando, al final del menú y detrás de una
 * línea: es lo único de aquí que no filtra, y mezclarlo con las categorías hacía que tocar la
 * fila equivocada cambiara algo que no querías cambiar.
 */
@Composable
private fun CategoryFilterMenu(
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onManageClick: () -> Unit
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val active = selectedCategory != null
    val turn by animateFloatAsState(
        targetValue = if (open) 180f else 0f,
        label = "flecha de la categoría"
    )

    Box {
        FilterChip(
            selected = active,
            onClick = { open = true },
            modifier = Modifier.height(44.dp),
            shape = CircleShape,
            label = {
                Text(
                    text = selectedCategory?.label() ?: "Categoría",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(turn)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                labelColor = ExpenseMuted,
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            border = null
        )

        UniDropdownMenu(
            expanded = open,
            onDismissRequest = { open = false }
        ) {
            DropdownMenuItem(
                text = { Text("Todas las categorías") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = if (selectedCategory == null) ExpensePurple else ExpenseMuted
                    )
                },
                onClick = {
                    onCategorySelected(null)
                    open = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.label()) },
                    leadingIcon = {
                        Icon(
                            imageVector = category.expenseSheetIcon(),
                            contentDescription = null,
                            tint = category.expenseTone()
                        )
                    },
                    trailingIcon = {
                        if (selectedCategory == category) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = ExpensePurple
                            )
                        }
                    },
                    onClick = {
                        onCategorySelected(category)
                        open = false
                    }
                )
            }
            UniDivider(Modifier.padding(vertical = 4.dp))
            DropdownMenuItem(
                text = { Text("Administrar categorías") },
                leadingIcon = {
                    Icon(Icons.Rounded.Tune, contentDescription = null, tint = ExpenseMuted)
                },
                onClick = {
                    open = false
                    onManageClick()
                }
            )
        }
    }
}


@Composable
private fun PeriodSegmentedControl(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    // El mismo grupo conectado que Horario/Calendario y Materias/Tareas. Era una caja con
    // tres cajas dentro y la elegida tenida al 28 %, que se leia mas como un resalte que
    // como una eleccion.
    UniSegmentedControl(
        selected = selectedPeriod,
        // Tres, no cuatro. Con «Periodo» dentro, los cuatro rótulos se quedaban en «Tod»,
        // «Sem», «Mes» y «Period»: un filtro que hay que adivinar no es un filtro. El periodo
        // a medida baja a la fila de chips, donde tiene sitio para decir las fechas.
        // Todo primero, y es tambien el que sale al abrir. Semana fue el que abria antes,
        // razonando que «casi siempre tiene algo que enseñar» -- pero un lunes, o cualquier
        // dia antes de registrar el primer gasto de la semana, «casi siempre» falla, y una
        // pantalla vacia nada mas entrar parece rota antes que vacia. Todo casi nunca esta
        // vacio salvo la primera vez de verdad.
        options = listOf(
            ExpensePeriodFilter.ALL,
            ExpensePeriodFilter.WEEK,
            ExpensePeriodFilter.MONTH
        ).map { UniSegmentedOption(value = it, label = it.label) },
        onSelected = onPeriodSelected,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseCategorySheet(
    selectedCategory: ExpenseCategory?,
    enabledCategories: Set<ExpenseCategory>,
    filterCategories: List<ExpenseCategory>,
    feedback: String?,
    onFilterSelected: (ExpenseCategory?) -> Unit,
    onToggleCategory: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit
) {
    /*
     * Solo administrar, ya no filtrar.
     *
     * Filtrar se hace desde el desplegable del chip, que es un toque y no cambia de pantalla.
     * Aquí queda lo otro: decidir cuáles de las seis categorías salen al registrar un gasto.
     * Tener las dos cosas en la misma hoja, con cajas idénticas, era lo que hacía tocar la
     * equivocada.
     */
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ExpenseBackground,
        contentColor = ExpenseText,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Administrar categorías",
                    color = ExpenseText,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Apaga las que no uses y dejarán de salir al registrar un gasto. " +
                        "Los gastos que ya tengas guardados no se tocan.",
                    color = ExpenseMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExpenseCategory.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { category ->
                            val enabled = category in enabledCategories
                            ExpenseCategorySheetOption(
                                label = category.label(),
                                icon = category.expenseSheetIcon(),
                                selected = enabled,
                                accent = ExpenseCoral,
                                trailingCheck = enabled,
                                onClick = { onToggleCategory(category) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            feedback?.let { message ->
                Text(
                    text = message,
                    color = if (message.startsWith("Debe")) ExpenseCoral else ExpenseMuted,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ExpenseCategorySheetOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingCheck: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(50.dp)
            .cleanClickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) accent.copy(alpha = 0.12f) else ExpenseCardHigh,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) accent.copy(alpha = 0.62f) else ExpenseBorder
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) accent else ExpenseMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                color = if (selected) ExpenseText else ExpenseMuted,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (trailingCheck) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}



/**
 * Un color por categoría, sacado de los tonos del tema.
 *
 * La pantalla pintaba los seis cajones del mismo coral, que es el color de la sección y no
 * el del gasto. Con un tono por categoría la lista se lee de un vistazo.
 */
@Composable
internal fun ExpenseCategory.expenseTone(): Color = when (this) {
    ExpenseCategory.TRANSPORT -> LocalSectionColors.current.schedule
    ExpenseCategory.FOOD -> LocalSectionColors.current.expenses
    ExpenseCategory.COPIES -> LocalSectionColors.current.atRisk
    ExpenseCategory.MATERIALS -> MaterialTheme.colorScheme.tertiary
    ExpenseCategory.OUTINGS -> LocalSectionColors.current.onTrack
    ExpenseCategory.OTHER -> MaterialTheme.colorScheme.outline
}

@Composable
internal fun ExpenseCategory.expenseSheetIcon(): ImageVector {
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
private fun ExpensesEmptyState(
    period: ExpensePeriodFilter
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        shape = MaterialTheme.shapes.large,
        color = ExpenseCard,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(ExpenseCardHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Wallet,
                    contentDescription = null,
                    tint = ExpenseNeutralIcon,
                    modifier = Modifier.size(34.dp)
                )
                Box(
                    modifier = Modifier
                        .offset(x = (-7).dp, y = (-22).dp)
                        .size(width = 4.dp, height = 9.dp)
                        .clip(CircleShape)
                        .background(ExpensePurple)
                )
                Box(
                    modifier = Modifier
                        .offset(x = 7.dp, y = (-23).dp)
                        .size(width = 4.dp, height = 9.dp)
                        .clip(CircleShape)
                        .background(ExpensePurple)
                )
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Aún no hay gastos ${period.emptySuffix}",
                color = ExpenseText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Registra tu primer gasto para ver\ntu resumen y categorías.",
                color = ExpenseMuted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RegisterExpenseButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*
     * Botón de acción con morphing de Material 3 Expressive.
     * En reposo usa forma cuadrada/redondeada y al pulsar aprieta las esquinas.
     */
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shapes = UniStackButtonDefaults.shapes,
        colors = ButtonDefaults.buttonColors(
            containerColor = ExpenseCoral,
            contentColor = contentColorOn(ExpenseCoral)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = contentColorOn(ExpenseCoral),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Registrar gasto",
                color = contentColorOn(ExpenseCoral),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseBudgetSheet(
    weeklyBudget: Int,
    monthlyBudget: Int,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var weeklyInput by rememberSaveable(weeklyBudget) {
        mutableStateOf(weeklyBudget.takeIf { it > 0 }?.toString().orEmpty())
    }
    var monthlyInput by rememberSaveable(monthlyBudget) {
        mutableStateOf(monthlyBudget.takeIf { it > 0 }?.toString().orEmpty())
    }
    val weeklyValue = weeklyInput.toIntOrNull() ?: if (weeklyInput.isBlank()) 0 else null
    val monthlyValue = monthlyInput.toIntOrNull() ?: if (monthlyInput.isBlank()) 0 else null
    val isValid = weeklyValue != null &&
        monthlyValue != null &&
        weeklyValue in 0..99_999_999 &&
        monthlyValue in 0..999_999_999

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ExpenseBackground,
        contentColor = ExpenseText,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Configurar presupuesto",
                    color = ExpenseText,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Define límites para que el resumen de Gastos tenga contexto real.",
                    color = ExpenseMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                BudgetInputField(
                    label = "Semanal",
                    value = weeklyInput,
                    onValueChange = { weeklyInput = it.filter(Char::isDigit).take(9) },
                    modifier = Modifier.weight(1f)
                )
                BudgetInputField(
                    label = "Mensual",
                    value = monthlyInput,
                    onValueChange = { monthlyInput = it.filter(Char::isDigit).take(9) },
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "Puedes dejar un campo vacío para no usar presupuesto en ese periodo.",
                color = ExpenseMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    onSave(
                        (weeklyValue ?: 0).toString(),
                        (monthlyValue ?: 0).toString()
                    )
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ExpenseCoral,
                    contentColor = contentColorOn(ExpenseCoral),
                    disabledContainerColor = ExpenseTrack,
                    disabledContentColor = ExpenseMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Guardar presupuesto", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun BudgetInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(72.dp),
        shape = MaterialTheme.shapes.medium,
        color = ExpenseCardHigh,
        border = BorderStroke(1.dp, ExpenseDivider),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                color = ExpenseMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$",
                    color = ExpenseMuted,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(6.dp))
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = ExpenseText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(ExpensePurple),
                    modifier = Modifier.weight(1f),
                    decorationBox = { innerTextField ->
                        if (value.isBlank()) {
                            Text("0", color = ExpenseMuted.copy(alpha = 0.65f), fontSize = 17.sp)
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}

/**
 * El día y lo que se gastó en él.
 *
 * «Hoy» y «Ayer» en vez de la fecha para los dos días que se miran de verdad: quien abre Gastos
 * a las once de la noche está mirando hoy, y leer «26 ago» obliga a comprobar qué día es hoy
 * antes de saber si eso es lo de hoy.
 */
/**
 * Un día, como una sola tarjeta.
 *
 * Antes el encabezado del día y cada gasto eran piezas sueltas -- una fila plana y una
 * tarjeta por gasto -- separadas todas por el mismo hueco de 20dp que separaba un día del
 * siguiente. Un martes con seis gastos se leía como seis filas cualquiera, sin nada que dijera
 * dónde acababa el bloque. Aquí el día entero es un solo contenedor: el total va dentro de su
 * propio encabezado y cada gasto es una fila interna, separada de la siguiente por un filete,
 * no por una tarjeta propia.
 */
@Composable
private fun ExpenseDayGroup(
    day: java.time.LocalDate,
    expenses: List<Expense>,
    onEditClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    val today = ExpenseDateUtils.today()
    val label = when (day) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> day.format(
            java.time.format.DateTimeFormatter.ofPattern(
                if (day.year == today.year) "EEEE, d 'de' MMMM" else "d 'de' MMMM 'de' yyyy",
                ExpenseChipLocale
            )
        ).replaceFirstChar { it.uppercase(ExpenseChipLocale) }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        // extraLarge (32dp) se veía demasiado redondeado en un bloque alto de varias filas --
        // el radio grande está pensado para tarjetas cortas. medium (20dp) es el mismo que
        // llevaba cada gasto por separado antes de agruparlos.
        shape = MaterialTheme.shapes.medium,
        color = ExpenseCard,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    color = ExpenseText,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatCurrency(expenses.sumOf { it.amount }),
                    color = ExpenseMuted,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            expenses.forEachIndexed { index, expense ->
                if (index > 0) {
                    HorizontalDivider(color = ExpenseDivider, thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                }
                ExpenseRow(
                    expense = expense,
                    onEditClick = { onEditClick(expense.id) },
                    onDeleteClick = { onDeleteClick(expense.id) }
                )
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/** El gasto, con el arrastre para borrar por delante. */
@Composable
private fun ExpenseRow(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    FilaDeslizable(
        onBorrar = onDeleteClick,
        containerColor = ExpenseCard
    ) {
        FilaDeGasto(expense = expense, onEditClick = onEditClick, onDeleteClick = onDeleteClick)
    }
}

@Composable
private fun FilaDeGasto(
    expense: Expense,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Editar y borrar se van a la hoja que abre la fila.
    //
    // Eran dos botones de icono en cada fila: con diez gastos, veinte mandos en la lista, y
    // el de borrar a un dedo de distancia del de editar. Ahora la fila se toca entera y las
    // dos acciones salen abajo con su nombre escrito, como en el sheet de clase de Horario.
    var showActions by rememberSaveable(expense.id) { mutableStateOf(false) }

    if (showActions) {
        ExpenseActionsSheet(
            expense = expense,
            onDismiss = { showActions = false },
            onEditClick = {
                showActions = false
                onEditClick()
            },
            onDeleteClick = {
                showActions = false
                onDeleteClick()
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable { showActions = true }
            // Los 8dp de antes en el borde derecho eran del tiempo en que cada gasto era su
            // propia tarjeta, pensados para dejar sitio a un mando que ya no está. Agrupadas
            // en la tarjeta del día, esa asimetría dejaba el importe pegado a la esquina
            // mientras el encabezado respiraba 20dp de los dos lados.
            .padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // El icono dice de qué es el gasto. Los seis cajones llevaban el mismo billete
        // en el mismo coral, así que una lista de diez gastos era diez veces el mismo
        // dibujo y había que leer el rótulo de cada fila para distinguirlas.
        AccentCircleIcon(
            icon = expense.category.expenseSheetIcon(),
            iconColor = contentColorOn(expense.category.expenseTone()),
            backgroundColor = expense.category.expenseTone(),
            size = 42.dp,
            iconSize = 20.dp
        )
        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = expense.category.label(),
                color = ExpenseText,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Sin segunda línea: la fecha la dice ahora el encabezado del día, y un gasto
            // no guarda nada más que su categoría y su importe.
        }
        Text(
            text = formatCurrency(expense.amount),
            color = ExpenseCoral,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
    }
}

/** Qué se puede hacer con un gasto, dicho con palabras y no con dos iconos en la fila. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseActionsSheet(
    expense: Expense,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val tone = expense.category.expenseTone()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccentCircleIcon(
                    icon = expense.category.expenseSheetIcon(),
                    iconColor = contentColorOn(tone),
                    backgroundColor = tone,
                    size = 46.dp,
                    iconSize = 22.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = expense.category.label(),
                        color = ExpenseText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = ExpenseDateUtils.formatDisplay(expense.dateMillis, LocalAccessibilityPreferences.current.dateFormat) +
                            "  •  " + formatCurrency(expense.amount),
                        color = ExpenseMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            ExpenseActionRow(
                icon = Icons.Rounded.Edit,
                tone = MaterialTheme.colorScheme.primary,
                title = "Editar gasto",
                subtitle = "Categoría, monto y fecha",
                onClick = onEditClick
            )
            ExpenseActionRow(
                icon = Icons.Rounded.Delete,
                tone = MaterialTheme.colorScheme.error,
                title = "Eliminar gasto",
                subtitle = "Se borra del historial y de los totales",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun ExpenseActionRow(
    icon: ImageVector,
    tone: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .cleanClickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccentCircleIcon(
            icon = icon,
            iconColor = tone,
            backgroundColor = tone.copy(alpha = 0.16f),
            size = 36.dp,
            iconSize = 19.dp
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = ExpenseMuted,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AccentCircleIcon(
    icon: ImageVector,
    iconColor: Color,
    backgroundColor: Color,
    size: Dp,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

/** Cadena vacía si no hay periodo anterior: sin nada con qué comparar no hay tendencia. */
private fun trendText(total: Int, previousTotal: Int): String {
    // Antes devolvía «+0% vs anterior», que suena a que gastaste lo mismo que el periodo
    // pasado cuando en realidad no hay periodo pasado. Un 0% inventado es peor que
    // no decir nada.
    if (previousTotal <= 0) return ""
    val percent = (((total - previousTotal) / previousTotal.toFloat()) * 100).roundToInt()
    val sign = if (percent >= 0) "+" else ""
    return "$sign$percent% vs anterior"
}

private fun previousTotalForPeriod(
    expenses: List<Expense>,
    period: ExpensePeriodFilter
): Int {
    val today = ExpenseDateUtils.today()
    return when (period) {
        // La tendencia de hoy se mide contra ayer, que es el tramo anterior del mismo largo.
        ExpensePeriodFilter.TODAY -> {
            val yesterday = today.minusDays(1)
            expenses
                .filter { ExpenseDateUtils.fromMillis(it.dateMillis) == yesterday }
                .sumOf { it.amount }
        }
        ExpensePeriodFilter.WEEK -> {
            val currentStart = ExpenseDateUtils.startOfWeek(today)
            val previousStart = currentStart.minusDays(7)
            expenses
                .filter { expense ->
                    val date = ExpenseDateUtils.fromMillis(expense.dateMillis)
                    !date.isBefore(previousStart) && date.isBefore(currentStart)
                }
                .sumOf { it.amount }
        }
        ExpensePeriodFilter.MONTH -> {
            val previousMonth = today.minusMonths(1)
            expenses
                .filter { expense ->
                    val date = ExpenseDateUtils.fromMillis(expense.dateMillis)
                    date.month == previousMonth.month && date.year == previousMonth.year
                }
                .sumOf { it.amount }
        }
        // «Todo» no tiene tramo anterior con el que compararse: ya los incluye todos. Sin
        // comparación, no se enseña tendencia.
        ExpensePeriodFilter.ALL -> 0
    }
}

/**
 * La cifra abreviada que cabe dentro del anillo.
 *
 * «$60.500» a tamaño legible no entra en 62dp, y encogerlo hasta que quepa lo dejaria
 * ilegible: dentro de la galleta se dice «$60,5k» y el importe exacto vive en la cifra grande
 * de arriba, a dos centimetros.
 */
private fun compactAmount(monto: Int, currency: CurrencyPreference = CurrencyPreference.COP): String = when {
    monto >= 1_000_000 -> "${currency.symbol}" + String.format(java.util.Locale.forLanguageTag("es"), "%.1f", monto / 1_000_000.0) + "M"
    monto >= 10_000 -> "${currency.symbol}" + String.format(java.util.Locale.forLanguageTag("es"), "%.1f", monto / 1_000.0) + "k"
    monto >= 1_000 -> "${currency.symbol}" + String.format(java.util.Locale.forLanguageTag("es"), "%.1f", monto / 1_000.0) + "k"
    else -> CurrencyFormatter.format(monto, currency)
}

private fun compactCop(monto: Int): String = compactAmount(monto, CurrencyPreference.COP)

private fun recordCountLabel(count: Int): String =
    if (count == 1) "1 registro" else "$count registros"

/**
 * El tramo que suman la cifra, el grafico y la lista.
 *
 * «Hoy» es el que sale al abrir, y no «Todo»: lo primero que se viene a mirar es cuanto
 * llevas gastado hoy, no el acumulado historico, que solo crece y nunca dice nada nuevo.
 */
private val ExpenseChipLocale: java.util.Locale = java.util.Locale.forLanguageTag("es")

private enum class ExpensePeriodFilter(val label: String, val heroLabel: String, val emptySuffix: String) {
    TODAY("Hoy", "Hoy", "hoy"),
    WEEK("Semana", "Esta semana", "esta semana"),
    MONTH("Mes", "Este mes", "este mes"),
    ALL("Todo", "Todo", "todavía");

    fun matches(expense: Expense): Boolean {
        val today = ExpenseDateUtils.today()
        val date = ExpenseDateUtils.fromMillis(expense.dateMillis)
        return when (this) {
            TODAY -> date == today
            WEEK -> ExpenseDateUtils.isInCurrentWeek(expense.dateMillis, today)
            MONTH -> date.month == today.month && date.year == today.year
            ALL -> true
        }
    }
}

@Preview(name = "Gastos mock 430", widthDp = 430, heightDp = 932, showBackground = true)
@Composable
private fun ExpensesScreenReferencePreview430() {
    ExpensesReferencePreview(widthDp = 430)
}

@Preview(name = "Gastos mock 360", widthDp = 360, heightDp = 800, showBackground = true)
@Composable
private fun ExpensesScreenReferencePreview360() {
    ExpensesReferencePreview(widthDp = 360)
}

@Composable
private fun ExpensesReferencePreview(widthDp: Int) {
    UniStackTheme(darkTheme = true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ExpenseBackground)
        ) {
            ExpensesContent(
                selectedPeriod = ExpensePeriodFilter.WEEK,
                onPeriodSelected = {},
                selectedCategory = null,
                categories = ExpenseCategory.entries,
                onCategorySelected = {},
                onCategoryClick = {},
                periodTotal = 48_500,
                recordCount = 6,
                previousTotal = 43_300,
                budget = 80_000,
                budgetProgress = 0.61f,
                chartValues = ReferenceBars,
            chartStyle = ExpenseChartStyle.BARS,
                expenses = emptyList(),
                onAddExpenseClick = {},
                onEditExpenseClick = {},
                onDeleteExpenseClick = {},
                onBudgetClick = {},
                bottomPadding = 150.dp
            )
            ExpensesPreviewBottomNav(
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun ExpensesContent(
    selectedPeriod: ExpensePeriodFilter,
    onPeriodSelected: (ExpensePeriodFilter) -> Unit,
    selectedCategory: ExpenseCategory?,
    categories: List<ExpenseCategory>,
    onCategorySelected: (ExpenseCategory?) -> Unit,
    onCategoryClick: () -> Unit,
    periodTotal: Int,
    recordCount: Int,
    previousTotal: Int,
    budget: Int,
    budgetProgress: Float,
    chartValues: List<Int>,
    chartStyle: ExpenseChartStyle = ExpenseChartStyle.BARS,
    expenses: List<Expense>,
    onAddExpenseClick: () -> Unit,
    onEditExpenseClick: (String) -> Unit,
    onDeleteExpenseClick: (String) -> Unit,
    onBudgetClick: () -> Unit,
    bottomPadding: Dp
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp,
            top = 48.dp,
            end = 24.dp,
            bottom = bottomPadding
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { ExpensesHeader() }
        item {
            ExpensesHeroCard(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                amount = periodTotal,
                recordCount = recordCount,
                trendText = trendText(periodTotal, previousTotal),
                budget = budget,
                budgetProgress = budgetProgress,
                chartValues = chartValues,
                chartStyle = chartStyle,
                onChartStyleChange = {},
                categoryTotals = emptyList(),
                onChartClick = {},
                onBudgetClick = onBudgetClick
            )
        }
        item {
            ExpensesFilters(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = onPeriodSelected,
                selectedCategory = selectedCategory,
                categories = categories,
                onCategorySelected = onCategorySelected,
                onCategoryClick = onCategoryClick
            )
        }
        if (expenses.isEmpty()) {
            item { ExpensesEmptyState(period = selectedPeriod) }
        } else {
            item {
                Text(
                    text = "Últimos gastos",
                    color = ExpenseText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(expenses, key = { it.id }) { expense ->
                ExpenseRow(
                    expense = expense,
                    onEditClick = { onEditExpenseClick(expense.id) },
                    onDeleteClick = { onDeleteExpenseClick(expense.id) }
                )
            }
        }
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                RegisterExpenseButton(
                    onClick = onAddExpenseClick
                )
            }
        }
    }
}

@Composable
private fun ExpensesPreviewBottomNav(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .height(96.dp),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = ExpenseCard,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PreviewBottomNavItem("Inicio", Icons.Rounded.Home, selected = false, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Materias", Icons.AutoMirrored.Rounded.MenuBook, selected = false, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Tareas", Icons.AutoMirrored.Rounded.Assignment, selected = false, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Gastos", Icons.Rounded.AccountBalanceWallet, selected = true, modifier = Modifier.weight(1f))
            PreviewBottomNavItem("Perfil", Icons.Rounded.Person, selected = false, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun PreviewBottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (selected) ExpensePurple else ExpenseMuted
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(25.dp)
        )
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(top = 5.dp)
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(width = 19.dp, height = 4.dp)
                .clip(CircleShape)
                .background(if (selected) ExpensePurple else Color.Transparent)
        )
    }
}
