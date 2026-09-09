@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_expenses.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.formatCurrency
import com.unistack.app.core.utils.DayLabels
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_expenses.domain.ExpenseInsights
import java.time.LocalDate
import java.time.YearMonth
import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

/** Las tres maneras de leer lo mismo. */
private enum class InsightView(@StringRes val labelRes: Int) {
    COMPARAR(R.string.insights_tab_compare),
    RITMO(R.string.insights_tab_pace),
    CALENDARIO(R.string.insights_tab_calendar);

    val label: String
        @Composable get() = stringResource(labelRes)
}

/**
 * Tus gastos, leídos de tres maneras.
 *
 * No son tres pestañas de un tablero: son **tres preguntas distintas** sobre los mismos datos,
 * y por eso conviven en vez de fundirse en una pantalla larga.
 *
 * - **Comparado**: ¿voy peor que antes? Enfrenta esta semana con la anterior y dice de qué
 *   categoría sale la diferencia.
 * - **Ritmo**: ¿voy a llegar? Proyecta el cierre del mes y lo traduce a cuánto puedes gastar
 *   al día sin pasarte, que es la única cifra con la que se puede hacer algo hoy.
 * - **Calendario**: ¿qué pasó ese día? El mes entero teñido por gasto, y al tocar un día se
 *   abre lo que se fue en él.
 *
 * Las cuentas viven en [ExpenseInsights], sin nada de Android, para poder probarlas con fechas
 * fijas: una proyección hecha el día 11 no se puede juzgar mirando la pantalla el día 25.
 */
@Composable
fun ExpenseInsightsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    var vista by rememberSaveable { mutableStateOf(InsightView.COMPARAR) }

    LargeTitleScaffold(
        title = stringResource(R.string.insights_title),
        subtitle = stringResource(R.string.insights_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 14.dp
    ) {
        item {
            UniSegmentedControl(
                selected = vista,
                options = InsightView.entries.map { UniSegmentedOption(value = it, label = it.label) },
                onSelected = { vista = it }
            )
        }
        when (vista) {
            InsightView.COMPARAR -> comparado(expenses)
            InsightView.RITMO -> ritmo(expenses, profile?.monthlyBudget ?: 0)
            InsightView.CALENDARIO -> calendario(expenses)
        }
    }
}

// ------------------------------------------------------------------ comparado

private fun androidx.compose.foundation.lazy.LazyListScope.comparado(
    expenses: List<com.unistack.app.feature_expenses.domain.Expense>
) {
    item {
        val c = remember(expenses) { ExpenseInsights.compararSemanas(expenses) }
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            UniCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.insights_this_week), style = SectionLabelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatCurrency(c.actual),
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = LocalSectionColors.current.expenses,
                            maxLines = 1
                        )
                    }
                    c.cambioPorcentual?.let { cambio ->
                        val baja = cambio <= 0
                        val tono = if (baja) LocalSectionColors.current.onTrack else MaterialTheme.colorScheme.error
                        // La diferencia va dentro de la galleta de M3E y no de un rectangulo:
                        // es el dato que se viene a mirar, y la forma lo separa de las dos
                        // cifras que compara.
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .background(tono.copy(alpha = 0.18f), MaterialShapes.Cookie9Sided.toShape()),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (if (baja) "" else "+") + cambio + "%",
                                color = tono,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        Text(stringResource(R.string.insights_previous_week), style = SectionLabelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatCurrency(c.anterior),
                            style = MaterialTheme.typography.headlineSmallEmphasized,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }

            if (c.categorias.isEmpty()) {
                Text(
                    stringResource(R.string.insights_not_enough_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(stringResource(R.string.insights_where_difference), style = SectionLabelStyle, color = MaterialTheme.colorScheme.primary)
                c.categorias.forEach { cat -> CategoriaComparadaRow(cat) }
                c.categoriaQueMasCambia?.let { mayor ->
                    UniCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(15.dp)) {
                      // UniCard mete su contenido en un Box: sin columna, los dos textos se
                      // pintan uno encima del otro.
                      Column {
                        val baja = c.diferencia <= 0
                        Text(
                            text = if (baja) {
                                stringResource(R.string.insights_spent_less, formatCurrency(-c.diferencia))
                            } else {
                                stringResource(R.string.insights_spent_more, formatCurrency(c.diferencia))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (mayor.diferencia < 0) {
                                stringResource(R.string.insights_diff_source_down, mayor.category.label().lowercase(), formatCurrency(kotlin.math.abs(mayor.diferencia)))
                            } else {
                                stringResource(R.string.insights_diff_source_up, mayor.category.label().lowercase(), formatCurrency(kotlin.math.abs(mayor.diferencia)))
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                      }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoriaComparadaRow(cat: ExpenseInsights.CategoriaComparada) {
    val tono = cat.category.expenseTone()
    val mayor = maxOf(cat.actual, cat.anterior).coerceAtLeast(1)
    var visible by remember(cat) { mutableStateOf(false) }
    LaunchedEffect(cat) { visible = true }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(tono))
            Text(
                cat.category.label(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            cat.cambioPorcentual?.let { cambio ->
                val baja = cambio <= 0
                Text(
                    text = (if (baja) "" else "+") + cambio + "%",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (baja) LocalSectionColors.current.onTrack else MaterialTheme.colorScheme.error
                )
            }
        }
        ComparaBarra(stringResource(R.string.insights_now), cat.actual, mayor, tono, visible)
        ComparaBarra(stringResource(R.string.insights_before), cat.anterior, mayor, MaterialTheme.colorScheme.outline, visible)
    }
}

@Composable
private fun ComparaBarra(rotulo: String, valor: Int, mayor: Int, color: Color, visible: Boolean) {
    val fraccion by animateFloatAsState(
        targetValue = if (visible) valor.toFloat() / mayor else 0f,
        animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
        label = "barra comparada"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 5.dp)
    ) {
        Text(
            rotulo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(42.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Text(
            formatCurrency(valor),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.padding(start = 10.dp).widthIn(min = 78.dp)
        )
    }
}

// ------------------------------------------------------------------ ritmo

private fun androidx.compose.foundation.lazy.LazyListScope.ritmo(
    expenses: List<com.unistack.app.feature_expenses.domain.Expense>,
    presupuesto: Int
) {
    item {
        val r = remember(expenses, presupuesto) { ExpenseInsights.ritmoDelMes(expenses, presupuesto) }
        val sections = LocalSectionColors.current
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            UniCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(18.dp)) {
              Column {
                Text(
                    if (presupuesto > 0) stringResource(R.string.insights_projected_end) else stringResource(R.string.insights_projected_month_end),
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    formatCurrency(r.proyeccion),
                    style = MaterialTheme.typography.displaySmallEmphasized,
                    color = if (r.seVaAPasar) MaterialTheme.colorScheme.error else sections.expenses,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = when {
                        presupuesto <= 0 -> stringResource(R.string.insights_pace_no_budget, formatCurrency(r.gastado), r.diaDelMes)
                        r.seVaAPasar -> stringResource(R.string.insights_pace_over_budget, formatCurrency(r.proyeccion - presupuesto))
                        else -> stringResource(R.string.insights_pace_within_budget, formatCurrency(presupuesto))
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (r.seVaAPasar) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 3.dp)
                )
                RitmoLinea(
                    acumulado = r.acumuladoPorDia,
                    proyeccion = r.proyeccion,
                    diasDelMes = r.diasDelMes,
                    presupuesto = presupuesto,
                    modifier = Modifier.fillMaxWidth().height(96.dp).padding(top = 14.dp)
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
                    Text(stringResource(R.string.insights_pace_day_one), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        stringResource(R.string.insights_pace_today, r.diaDelMes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(stringResource(R.string.insights_pace_day_n, r.diasDelMes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }

            r.diarioRestante?.let { diario ->
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = sections.onTrackContainer,
                    contentPadding = PaddingValues(18.dp)
                ) {
                  Column {
                    Text(stringResource(R.string.insights_to_not_exceed), style = SectionLabelStyle, color = sections.onOnTrackContainer)
                    Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            formatCurrency(diario),
                            style = MaterialTheme.typography.headlineMediumEmphasized,
                            color = sections.onOnTrackContainer,
                            maxLines = 1
                        )
                        Text(
                            " " + stringResource(R.string.insights_per_day_suffix),
                            style = MaterialTheme.typography.titleSmall,
                            color = sections.onOnTrackContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.insights_days_budget_left, r.diasRestantes, formatCurrency((presupuesto - r.gastado).coerceAtLeast(0))),
                        style = MaterialTheme.typography.bodySmall,
                        color = sections.onOnTrackContainer.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                  }
                }
            }
        }
    }
}

/**
 * Lo acumulado hasta hoy, y por dónde va si nada cambia.
 *
 * La línea sólida es lo que llevas gastado; la de puntos, la proyección hasta fin de mes. La
 * raya horizontal es el presupuesto: verla cruzada por la proyección es la manera más rápida de
 * entender que no vas a llegar.
 */
@Composable
private fun RitmoLinea(
    acumulado: List<Int>,
    proyeccion: Int,
    diasDelMes: Int,
    presupuesto: Int,
    modifier: Modifier = Modifier
) {
    val sections = LocalSectionColors.current
    val trazo = sections.expenses
    val techo = maxOf(proyeccion, presupuesto, acumulado.lastOrNull() ?: 0).coerceAtLeast(1)
    var visible by remember(acumulado) { mutableStateOf(false) }
    LaunchedEffect(acumulado) { visible = true }
    val avance by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MaterialTheme.motionScheme.slowEffectsSpec(),
        label = "trazado del ritmo"
    )
    val pista = MaterialTheme.colorScheme.outline
    val alerta = MaterialTheme.colorScheme.error

    Canvas(modifier = modifier) {
        if (acumulado.isEmpty()) return@Canvas
        val ancho = size.width
        val alto = size.height
        fun x(dia: Int) = ancho * dia / diasDelMes.toFloat()
        fun y(valor: Int) = alto - alto * (valor / techo.toFloat()) * 0.92f

        if (presupuesto > 0) {
            drawLine(
                color = pista,
                start = Offset(0f, y(presupuesto)),
                end = Offset(ancho, y(presupuesto)),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(9f, 9f))
            )
        }

        val hoyX = x(acumulado.size)
        val hoyY = y(acumulado.last())

        // La proyeccion, punteada, desde hoy hasta el final del mes.
        drawLine(
            color = if (proyeccion > presupuesto && presupuesto > 0) alerta else pista,
            start = Offset(hoyX, hoyY),
            end = Offset(x(diasDelMes), y(proyeccion)),
            strokeWidth = 2.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f)),
            cap = StrokeCap.Round
        )

        // Lo real, dibujandose de izquierda a derecha.
        val camino = Path()
        val hasta = (acumulado.size * avance).toInt().coerceAtLeast(1)
        acumulado.take(hasta).forEachIndexed { indice, valor ->
            val px = x(indice + 1)
            val py = y(valor)
            if (indice == 0) camino.moveTo(px, py) else camino.lineTo(px, py)
        }
        drawPath(camino, color = trazo, style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round))

        if (avance > 0.98f) {
            drawCircle(color = trazo, radius = 5.dp.toPx(), center = Offset(hoyX, hoyY))
        }
    }
}

// ------------------------------------------------------------------ calendario

private fun androidx.compose.foundation.lazy.LazyListScope.calendario(
    expenses: List<com.unistack.app.feature_expenses.domain.Expense>
) {
    item {
        val hoy = ExpenseDateUtils.today()
        val cal = remember(expenses) { ExpenseInsights.calendarioDelMes(expenses, YearMonth.from(hoy)) }
        var elegido by rememberSaveable { mutableStateOf(hoy.dayOfMonth) }
        val sections = LocalSectionColors.current
        val maximo = cal.dias.maxOfOrNull { it.total }?.coerceAtLeast(1) ?: 1

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.insights_in_month), style = SectionLabelStyle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        formatCurrency(cal.total),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
                        color = sections.expenses,
                        maxLines = 1
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                DayLabels.short.forEach { dia ->
                    Text(
                        dia,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // La rejilla se arma a mano en filas de siete: una LazyVerticalGrid dentro de una
            // LazyColumn no se puede medir, y para 30 casillas no hace falta.
            val casillas = List(cal.huecosIniciales) { null } + cal.dias
            casillas.chunked(7).forEach { semana ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    semana.forEach { dia ->
                        if (dia == null) {
                            Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            DiaDelCalendario(
                                dia = dia,
                                maximo = maximo,
                                esHoy = dia.fecha == hoy,
                                elegido = dia.fecha.dayOfMonth == elegido,
                                onClick = { elegido = dia.fecha.dayOfMonth },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    repeat(7 - semana.size) {
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }

            cal.dias.firstOrNull { it.fecha.dayOfMonth == elegido }?.let { dia ->
                DiaDetalle(dia)
            }
        }
    }
}

@Composable
private fun DiaDelCalendario(
    dia: ExpenseInsights.DiaDelMes,
    maximo: Int,
    esHoy: Boolean,
    elegido: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sections = LocalSectionColors.current
    // La intensidad dice cuanto se gasto ese dia. Arranca en 0,20 y no en 0 para que un dia con
    // gasto pequeño se distinga de uno sin nada.
    val intensidad = if (dia.total <= 0) 0f else 0.20f + 0.80f * (dia.total.toFloat() / maximo)
    val fondo = if (dia.total <= 0) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        sections.expenses.copy(alpha = intensidad)
    }
    var visible by remember(dia) { mutableStateOf(false) }
    LaunchedEffect(dia) { visible = true }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0.7f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "entrada del día"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.small,
        color = fondo,
        border = when {
            elegido -> androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface)
            esHoy -> androidx.compose.foundation.BorderStroke(2.dp, sections.expenses)
            else -> null
        }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                dia.fecha.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (esHoy || elegido) FontWeight.Bold else FontWeight.Medium,
                color = if (intensidad > 0.55f) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.scale(escala)
            )
        }
    }
}

@Composable
private fun DiaDetalle(dia: ExpenseInsights.DiaDelMes) {
    val sections = LocalSectionColors.current
    UniCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
      Column {
        Text(
            text = DayLabels.medium(dia.fecha.dayOfWeek).uppercase() + " " + dia.fecha.dayOfMonth,
            style = SectionLabelStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (dia.total <= 0) {
            Text(
                stringResource(R.string.insights_no_expenses_day),
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else {
            Text(
                formatCurrency(dia.total),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = sections.expenses,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
            dia.gastos.forEach { gasto ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(gasto.category.expenseTone())
                    )
                    Text(
                        gasto.category.label(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 11.dp).weight(1f)
                    )
                    Text(
                        formatCurrency(gasto.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = sections.expenses
                    )
                }
            }
        }
      }
    }
}
