@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)

package com.unistack.app.feature_support.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.ScaleZoneBar
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_support.domain.CalculatorMath
import com.unistack.app.feature_support.domain.CalculatorMath.Evaluation
import com.unistack.app.feature_support.domain.CalculatorMath.SemesterSubject
import com.unistack.app.feature_user.domain.GradingScale

/** Qué casilla recibe lo que se teclea. */
private enum class Slot { FIRST, SECOND }

internal fun parseTyped(text: String): Double? = text.replace(',', '.').toDoubleOrNull()

private fun percentText(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)

/* ------------------------------------------------------------------ Materia */

@Composable
internal fun SubjectCalculator(
    maxGrade: Double,
    scale: GradingScale,
    toast: String?,
    onToast: (String?) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var grades by rememberSaveable { mutableStateOf(listOf<Double>()) }
    var weights by rememberSaveable { mutableStateOf(listOf<Double>()) }
    var draft by rememberSaveable { mutableStateOf("0") }
    var slot by rememberSaveable { mutableStateOf(Slot.FIRST) }
    var pending by rememberSaveable { mutableStateOf<Double?>(null) }
    var fresh by rememberSaveable { mutableStateOf(true) }

    val entries = grades.zip(weights) { g, w -> Evaluation(g, w) }
    val used = CalculatorMath.usedWeight(entries)
    val free = CalculatorMath.freeWeight(entries)
    val average = CalculatorMath.subjectAverage(entries)
    val complete = free <= 0.0
    val typed = parseTyped(draft) != null && draft != "0"

    fun reset() {
        draft = "0"
        slot = Slot.FIRST
        pending = null
        fresh = true
    }

    CalculatorLayout(
        toast = toast,
        onToastDismiss = { onToast(null) },
        entry = {
            NameField(
                value = name,
                placeholder = "Nombre de la materia (opcional)",
                onValueChange = { name = it }
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                NumberSlot(
                    label = "NOTA",
                    value = if (slot == Slot.FIRST) draft else pending?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "—",
                    active = slot == Slot.FIRST,
                    onClick = {
                        slot = Slot.FIRST
                        draft = "0"
                        fresh = true
                        onToast(null)
                    }
                )
                NumberSlot(
                    label = if (complete) "SIN SITIO" else "VALE · queda ${percentText(free)} %",
                    value = if (slot == Slot.SECOND) "$draft %" else "—",
                    active = slot == Slot.SECOND,
                    enabled = !complete && pending != null,
                    onClick = {
                        slot = Slot.SECOND
                        draft = "0"
                        fresh = true
                        onToast(null)
                    }
                )
                SlotAction(
                    isArrow = slot == Slot.FIRST,
                    enabled = typed && !complete,
                    onClick = {
                        if (complete) {
                            onToast("Ya está repartido el 100 % de la materia. Quita una nota para cambiarla.")
                            return@SlotAction
                        }
                        val value = parseTyped(draft) ?: return@SlotAction
                        if (slot == Slot.FIRST) {
                            pending = value
                            slot = Slot.SECOND
                            draft = "0"
                            fresh = true
                        } else {
                            grades = grades + (pending ?: 0.0)
                            weights = weights + value
                            reset()
                        }
                        onToast(null)
                    }
                )
            }
        },
        keypad = {
            CalculatorKeypad(
                draft = draft,
                max = if (slot == Slot.FIRST) maxGrade else free,
                fresh = fresh,
                blockedMessage = if (slot == Slot.FIRST) {
                    "La escala llega hasta ${GradingScaleUtils.formatGrade(maxGrade, scale)}: no hay notas por encima."
                } else {
                    "Solo queda el ${percentText(free)} % por repartir. Lo que pongas tiene que caber ahí."
                },
                onDraft = {
                    draft = it
                    fresh = false
                    onToast(null)
                },
                onClear = {
                    grades = emptyList()
                    weights = emptyList()
                    reset()
                    onToast(null)
                },
                onBlocked = onToast
            )
        }
    ) {
        item("resultado") {
            ResultCard(
                label = "LLEVAS EN LA MATERIA",
                value = average?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "—",
                suffix = "/ ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                accent = if (average == null) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((used / 100.0).toFloat().coerceIn(0f, 1f))
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .background(
                                if (complete) LocalSectionColors.current.onTrack
                                else MaterialTheme.colorScheme.primary
                            )
                    )
                }
                Text(
                    text = if (complete) {
                        "El 100 % está repartido: esta es la nota final."
                    } else {
                        "Llevas evaluado el ${percentText(used)} %. Queda libre el ${percentText(free)} %."
                    },
                    modifier = Modifier.padding(top = 7.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (complete) LocalSectionColors.current.onTrack else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (complete) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
        if (entries.isNotEmpty()) {
            item("notas") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    entries.forEachIndexed { index, entry ->
                        ValueChip(
                            text = "${GradingScaleUtils.formatGrade(entry.grade, scale)}  ·  ${percentText(entry.weightPercent)} %",
                            onRemove = {
                                grades = grades.filterIndexed { i, _ -> i != index }
                                weights = weights.filterIndexed { i, _ -> i != index }
                                onToast(null)
                            }
                        )
                    }
                }
            }
        } else {
            item("vacio") {
                CalculatorFootnote("Teclea la nota, toca la flecha, escribe cuánto vale y toca el más.")
            }
        }
    }
}

/* ----------------------------------------------------------------- Semestre */

@Composable
internal fun SemesterCalculator(
    maxGrade: Double,
    scale: GradingScale,
    passing: Double,
    target: Double,
    available: List<CalculatorSubject>,
    toast: String?,
    onToast: (String?) -> Unit
) {
    var names by rememberSaveable { mutableStateOf(listOf<String>()) }
    var grades by rememberSaveable { mutableStateOf(listOf<Double>()) }
    var credits by rememberSaveable { mutableStateOf(listOf<Double>()) }
    var fromApp by rememberSaveable { mutableStateOf(listOf<Boolean>()) }
    var name by rememberSaveable { mutableStateOf("") }
    var draft by rememberSaveable { mutableStateOf("0") }
    var slot by rememberSaveable { mutableStateOf(Slot.FIRST) }
    var pending by rememberSaveable { mutableStateOf<Double?>(null) }
    var fresh by rememberSaveable { mutableStateOf(true) }
    var editing by rememberSaveable { mutableStateOf(-1) }
    var pickerOpen by rememberSaveable { mutableStateOf(false) }

    val rows = names.indices.map { i ->
        SemesterSubject(names[i], grades[i], credits[i], fromApp.getOrElse(i) { false })
    }
    val average = CalculatorMath.semesterAverage(rows)
    val totalCredits = CalculatorMath.totalCredits(rows)
    val withoutCredits = CalculatorMath.subjectsWithoutCredits(rows)
    val typed = parseTyped(draft) != null && draft != "0"
    val accent = when {
        average == null -> MaterialTheme.colorScheme.outlineVariant
        average >= target -> LocalSectionColors.current.onTrack
        average >= passing -> LocalSectionColors.current.atRisk
        else -> MaterialTheme.colorScheme.error
    }

    fun reset() {
        draft = "0"
        slot = Slot.FIRST
        pending = null
        fresh = true
        editing = -1
        name = ""
    }

    CalculatorLayout(
        toast = toast,
        onToastDismiss = { onToast(null) },
        entry = {
            NameField(
                value = name,
                placeholder = if (editing >= 0) "Nombre de la materia" else "Materia ${rows.size + 1}",
                onValueChange = { name = it }
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                NumberSlot(
                    label = if (editing >= 0) "CORRIGIENDO" else "NOTA FINAL",
                    value = if (slot == Slot.FIRST) draft else pending?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "—",
                    active = slot == Slot.FIRST,
                    onClick = {
                        slot = Slot.FIRST
                        draft = "0"
                        fresh = true
                        onToast(null)
                    }
                )
                NumberSlot(
                    label = "CRÉDITOS",
                    value = if (slot == Slot.SECOND) draft else "—",
                    active = slot == Slot.SECOND,
                    enabled = pending != null || editing >= 0,
                    onClick = {
                        slot = Slot.SECOND
                        draft = "0"
                        fresh = true
                        onToast(null)
                    }
                )
                SlotAction(
                    isArrow = slot == Slot.FIRST,
                    enabled = typed,
                    onClick = {
                        val value = parseTyped(draft) ?: return@SlotAction
                        if (slot == Slot.FIRST) {
                            pending = value
                            slot = Slot.SECOND
                            draft = "0"
                            fresh = true
                        } else {
                            val finalName = name.ifBlank { "Materia ${rows.size + 1}" }
                            if (editing >= 0) {
                                names = names.mapIndexed { i, old -> if (i == editing) finalName else old }
                                grades = grades.mapIndexed { i, old -> if (i == editing) (pending ?: old) else old }
                                credits = credits.mapIndexed { i, old -> if (i == editing) value else old }
                            } else {
                                names = names + finalName
                                grades = grades + (pending ?: 0.0)
                                credits = credits + value
                                fromApp = fromApp + false
                            }
                            reset()
                        }
                        onToast(null)
                    }
                )
            }
        },
        keypad = {
            CalculatorKeypad(
                draft = draft,
                max = if (slot == Slot.FIRST) maxGrade else 40.0,
                fresh = fresh,
                blockedMessage = if (slot == Slot.FIRST) {
                    "La escala llega hasta ${GradingScaleUtils.formatGrade(maxGrade, scale)}."
                } else {
                    "Como mucho 40 créditos en una materia."
                },
                onDraft = {
                    draft = it
                    fresh = false
                    onToast(null)
                },
                onClear = {
                    names = emptyList()
                    grades = emptyList()
                    credits = emptyList()
                    fromApp = emptyList()
                    reset()
                    onToast(null)
                },
                onBlocked = onToast
            )
        }
    ) {
        item("resultado") {
            ResultCard(
                label = "PROMEDIO DEL SEMESTRE",
                value = average?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "—",
                suffix = "/ ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                accent = accent,
                trailing = if (totalCredits > 0.0) "${percentText(totalCredits)} créditos" else null
            ) {
                if (withoutCredits > 0) {
                    Text(
                        text = if (withoutCredits == 1) {
                            "Una materia no cuenta todavía: le faltan los créditos."
                        } else {
                            "$withoutCredits materias no cuentan todavía: les faltan los créditos."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalSectionColors.current.atRisk,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        item("franja") {
            ScaleZoneBar(max = maxGrade, passing = passing, target = target, marker = average)
        }
        items(rows.size, key = { rows[it].name + it }) { index ->
            SemesterRow(
                subject = rows[index],
                scale = scale,
                editing = editing == index,
                onEdit = {
                    editing = index
                    name = rows[index].name
                    slot = Slot.FIRST
                    draft = GradingScaleUtils.formatGrade(rows[index].grade, scale)
                    pending = rows[index].grade
                    fresh = true
                    onToast(null)
                },
                onRemove = {
                    names = names.filterIndexed { i, _ -> i != index }
                    grades = grades.filterIndexed { i, _ -> i != index }
                    credits = credits.filterIndexed { i, _ -> i != index }
                    fromApp = fromApp.filterIndexed { i, _ -> i != index }
                    if (editing == index) reset()
                    onToast(null)
                }
            )
        }
        item("acciones") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlineChip(
                    text = "Traer mis materias",
                    icon = Icons.Rounded.Add,
                    onClick = { pickerOpen = true }
                )
                if (rows.isNotEmpty()) {
                    OutlineChip(text = "Vaciar", icon = null, onClick = {
                        names = emptyList()
                        grades = emptyList()
                        credits = emptyList()
                        fromApp = emptyList()
                        reset()
                        onToast(null)
                    })
                }
            }
        }
    }

    if (pickerOpen) {
        SubjectPickerSheet(
            subjects = available,
            scale = scale,
            onDismiss = { pickerOpen = false },
            onConfirm = { chosen, withGrades ->
                names = names + chosen.map { it.name }
                grades = grades + chosen.map { if (withGrades) it.average ?: 0.0 else 0.0 }
                credits = credits + chosen.map { 0.0 }
                fromApp = fromApp + chosen.map { true }
                pickerOpen = false
                onToast(null)
            }
        )
    }
}

/* ----------------------------------------------------------------- Me falta */

@Composable
internal fun NeededCalculator(
    maxGrade: Double,
    scale: GradingScale,
    defaultTarget: Double,
    toast: String?,
    onToast: (String?) -> Unit
) {
    var have by rememberSaveable { mutableStateOf("0") }
    var done by rememberSaveable { mutableStateOf("0") }
    var goal by rememberSaveable { mutableStateOf(GradingScaleUtils.formatGrade(defaultTarget, scale)) }
    var field by rememberSaveable { mutableStateOf(0) }
    var fresh by rememberSaveable { mutableStateOf(true) }

    val haveValue = parseTyped(have) ?: 0.0
    val doneValue = (parseTyped(done) ?: 0.0).coerceIn(0.0, 100.0)
    val goalValue = parseTyped(goal) ?: 0.0
    val remaining = 100.0 - doneValue
    val needed = CalculatorMath.neededGrade(haveValue, doneValue, goalValue)
    val impossible = needed != null && needed > maxGrade
    val already = needed != null && needed <= 0.0

    val container = when {
        needed == null -> MaterialTheme.colorScheme.surfaceContainer
        already -> LocalSectionColors.current.onTrackContainer
        impossible -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val current = listOf(have, done, goal)[field]

    CalculatorLayout(
        toast = toast,
        onToastDismiss = { onToast(null) },
        entry = {},
        keypad = {
            CalculatorKeypad(
                draft = current,
                max = if (field == 1) 100.0 else maxGrade,
                fresh = fresh,
                blockedMessage = if (field == 1) {
                    "El curso evaluado va de 0 a 100 %."
                } else {
                    "La escala llega hasta ${GradingScaleUtils.formatGrade(maxGrade, scale)}."
                },
                onDraft = { value ->
                    when (field) {
                        0 -> have = value
                        1 -> done = value
                        else -> goal = value
                    }
                    fresh = false
                    onToast(null)
                },
                onClear = {
                    when (field) {
                        0 -> have = "0"
                        1 -> done = "0"
                        else -> goal = "0"
                    }
                    fresh = true
                    onToast(null)
                },
                onBlocked = onToast
            )
        }
    ) {
        item("campos") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column {
                    NeededField(
                        label = "Mi promedio ahora",
                        hint = "Lo que llevas en la materia",
                        value = have,
                        active = field == 0,
                        onClick = { field = 0; fresh = true; onToast(null) }
                    )
                    NeededField(
                        label = "Del curso ya evaluado",
                        hint = "Cuánto se ha calificado, en porcentaje",
                        value = "$done %",
                        active = field == 1,
                        onClick = { field = 1; fresh = true; onToast(null) }
                    )
                    NeededField(
                        label = "Quiero acabar con",
                        hint = "La nota con la que quieres terminar",
                        value = goal,
                        active = field == 2,
                        onClick = { field = 2; fresh = true; onToast(null) },
                        last = true
                    )
                }
            }
        }
        item("dibujo") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HalfBox(
                    label = "HECHO",
                    value = have,
                    weight = (doneValue / 100.0).toFloat().coerceIn(0.18f, 0.82f),
                    container = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                HalfBox(
                    label = "TE FALTA SACAR",
                    value = when {
                        needed == null -> "—"
                        already -> GradingScaleUtils.formatGrade(0.0, scale)
                        else -> GradingScaleUtils.formatGrade(needed, scale)
                    },
                    weight = 1f,
                    container = container
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CalculatorFootnote("${percentText(doneValue)} % evaluado")
                CalculatorFootnote("${percentText(remaining)} % por evaluar")
            }
        }
        item("veredicto") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = container,
                contentColor = contentColorOn(container)
            ) {
                Text(
                    text = when {
                        needed == null -> "Con el 100 % evaluado ya no queda nota que sacar: lo que llevas es lo que hay."
                        already -> "Ya la tienes: aunque saques cero en lo que falta, acabas con tu meta o por encima."
                        impossible -> "Necesitarías más de ${GradingScaleUtils.formatGrade(maxGrade, scale)}, y eso no existe. " +
                            "Con esa meta ya no da: bájala o cuenta con no llegar."
                        else -> "Necesitas sacar ${GradingScaleUtils.formatGrade(needed, scale)} " +
                            "en el ${percentText(remaining)} % que te queda."
                    },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/* -------------------------------------------------------------- compartidas */

/**
 * El armazón de las tres: lo que se lee arriba y se desplaza, y abajo lo que se toca —el
 * aviso, las casillas y el teclado— siempre en el mismo sitio y sin moverse con el scroll.
 */
@Composable
private fun CalculatorLayout(
    toast: String?,
    onToastDismiss: () -> Unit,
    entry: @Composable () -> Unit,
    keypad: @Composable () -> Unit,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    val spacing = LocalInterfaceSpacing.current
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = 14.dp,
                bottom = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
        Column(
            modifier = Modifier.padding(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                bottom = scrollBottomRoom.coerceAtMost(20.dp)
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (toast != null) {
                CalculatorToast(message = toast, onDismiss = onToastDismiss)
            }
            entry()
            keypad()
        }
    }
}

@Composable
private fun ResultCard(
    label: String,
    value: String,
    suffix: String,
    accent: androidx.compose.ui.graphics.Color,
    trailing: String? = null,
    extra: @Composable () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = label,
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 3.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmallEmphasized,
                    color = accent,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = suffix,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trailing != null) {
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = trailing,
                        modifier = Modifier.padding(bottom = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(modifier = Modifier.padding(top = 12.dp)) { extra() }
        }
    }
}

@Composable
private fun ValueChip(text: String, onRemove: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(start = 13.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Surface(
                onClick = onRemove,
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.14f),
                modifier = Modifier.size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Close, contentDescription = "Quitar", modifier = Modifier.size(13.dp))
                }
            }
        }
    }
}

@Composable
private fun OutlineChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SemesterRow(
    subject: SemesterSubject,
    scale: GradingScale,
    editing: Boolean,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (editing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (editing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(start = 13.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .cleanClickable(onEdit),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(
                            if (subject.fromApp) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
                Text(
                    text = subject.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = GradingScaleUtils.formatGrade(subject.grade, scale),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (subject.credits > 0.0) "${percentText(subject.credits)} cr" else "sin cr",
                    modifier = Modifier.width(46.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (subject.credits > 0.0) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        LocalSectionColors.current.atRisk
                    },
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                onClick = onRemove,
                shape = MaterialTheme.shapes.extraLarge,
                color = Color.Transparent,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Quitar ${subject.name}",
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NeededField(
    label: String,
    hint: String,
    value: String,
    active: Boolean,
    onClick: () -> Unit,
    last: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
                .cleanClickable(onClick)
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ) {
                Text(
                    text = value,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        if (!last) {
            androidx.compose.material3.HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HalfBox(
    label: String,
    value: String,
    weight: Float,
    container: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier
            .weight(weight)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = contentColorOn(container)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = label, style = SectionLabelStyle, maxLines = 1)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

