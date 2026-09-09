@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)

package com.unistack.app.feature_support.presentation

import com.unistack.app.core.design.theme.LocalVividAccents
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.key
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

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
    val allDistributedMsg = stringResource(R.string.calculator_all_distributed)

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
            SlotRow(
                focused = if (slot == Slot.FIRST) 0 else 1,
                slotCount = 2,
                trailing = {
                    SlotAction(
                        isArrow = slot == Slot.FIRST,
                        enabled = typed && !complete,
                        onClick = {
                            if (complete) {
                                onToast(allDistributedMsg)
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
            ) {
                NumberSlot(
                    label = stringResource(R.string.calculator_label_grade),
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
                    label = if (complete) (if (java.util.Locale.getDefault().language == "en") "NO SPACE" else "SIN SITIO") else (if (java.util.Locale.getDefault().language == "en") "WORTH · ${percentText(free)}% left" else "VALE · queda ${percentText(free)} %"),
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
            }
        },
        keypad = {
            CalculatorKeypad(
                draft = draft,
                max = if (slot == Slot.FIRST) maxGrade else free,
                fresh = fresh,
                blockedMessage = if (slot == Slot.FIRST) {
                    stringResource(R.string.calculator_max_scale_limit, GradingScaleUtils.formatGrade(maxGrade, scale))
                } else {
                    stringResource(R.string.calculator_free_percent_left, percentText(free))
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
                label = stringResource(R.string.calculator_label_course_progress),
                // Sin notas se enseña un cero apagado y no un guion: a ese tamaño y con ese
                // grosor, un «—» se lee como una barra gris rota, no como «todavía nada».
                value = GradingScaleUtils.formatGrade(average ?: 0.0, scale),
                suffix = "/ ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                accent = if (average == null) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface
            ) {
                /*
                 * Un tramo por nota, no una barra de un color.
                 *
                 * Era una franja lisa que salía saltando a su nuevo largo: decía cuánto llevas
                 * repartido, pero no de qué está hecho ese reparto. Partida en tramos, cada nota
                 * ocupa el suyo y se ve de un vistazo si la materia va en tres cortes grandes o
                 * en ocho pedazos. El tramo nuevo crece desde cero mientras los otros se acomodan.
                 */
                WeightBar(
                    weights = entries.map { it.weightPercent },
                    tones = weightTones(),
                    height = 10.dp
                )
                Text(
                    text = if (complete) {
                        stringResource(R.string.calculator_all_distributed_final)
                    } else {
                        stringResource(R.string.calculator_progress_summary, percentText(used), percentText(free))
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
                /*
                 * Una fila por nota, no una pastilla.
                 *
                 * Iban como etiquetas pequenas en una fila que se doblaba: cabian muchas, si,
                 * pero la nota y su peso quedaban del tamano de un pie de foto en el unico sitio
                 * donde hay que poder repasarlos. Y al entrar, una pastilla de ese tamano no da
                 * de si para que se note que acaba de entrar.
                 *
                 * Cada fila lleva su peso en un cuadro del color de su tramo en la barra: se
                 * sabe cual de los trozos es suyo sin contar de izquierda a derecha.
                 */
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    /*
                     * Sin `key`: la posicion en la lista es la identidad.
                     *
                     * Con `key(index, ...)`, borrar la primera nota cambiaba la clave de todas
                     * las de abajo —la que era 1 pasa a ser 0—, asi que Compose las daba por
                     * nuevas y todas volvian a entrar a la vez. Parecia que la lista se
                     * recargara entera por quitar una.
                     *
                     * Sin clave, cada hueco conserva su estado: al borrar desaparece el ultimo
                     * hueco y los demas siguen donde estaban, quietos. Y al anadir se estrena
                     * uno al final, que es el unico que debe animarse.
                     */
                    entries.forEachIndexed { index, entry ->
                        run {
                            EnterOnAppear {
                                GradeRow(
                                    grade = GradingScaleUtils.formatGrade(entry.grade, scale),
                                    weight = percentText(entry.weightPercent),
                                    tone = weightTones()[index % weightTones().size],
                                    onRemove = {
                                        grades = grades.filterIndexed { i, _ -> i != index }
                                        weights = weights.filterIndexed { i, _ -> i != index }
                                        onToast(null)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            item("vacio") {
                CalculatorFootnote(stringResource(R.string.calculator_instructions_course))
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
    }

    CalculatorLayout(
        toast = toast,
        onToastDismiss = { onToast(null) },
        entry = {
            SlotRow(
                focused = if (slot == Slot.FIRST) 0 else 1,
                slotCount = 2,
                trailing = {
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
                            // Las tecleadas a mano se numeran solas. Un campo de nombre
                            // suelto encima del teclado no llegaba a ser de nadie, y lo que
                            // hace falta para no perderse es el orden, no el rótulo.
                            if (editing >= 0) {
                                grades = grades.mapIndexed { i, old -> if (i == editing) (pending ?: old) else old }
                                credits = credits.mapIndexed { i, old -> if (i == editing) value else old }
                            } else {
                                names = names + (if (java.util.Locale.getDefault().language == "en") "Course ${rows.count { !it.fromApp } + 1}" else "Materia ${rows.count { !it.fromApp } + 1}") 
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
            ) {
                NumberSlot(
                    label = if (editing >= 0) (if (java.util.Locale.getDefault().language == "en") "EDITING" else "CORRIGIENDO") else stringResource(R.string.calculator_label_final_grade),
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
                    label = stringResource(R.string.calculator_label_credits),
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
            }
        },
        keypad = {
            CalculatorKeypad(
                draft = draft,
                max = if (slot == Slot.FIRST) maxGrade else 40.0,
                fresh = fresh,
                blockedMessage = if (slot == Slot.FIRST) {
                    stringResource(R.string.calculator_max_scale_limit, GradingScaleUtils.formatGrade(maxGrade, scale))
                } else {
                    stringResource(R.string.calculator_max_credits_limit)
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
                label = stringResource(R.string.calculator_label_semester_gpa),
                value = GradingScaleUtils.formatGrade(average ?: 0.0, scale),
                suffix = "/ ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                accent = accent,
                trailing = if (totalCredits > 0.0) stringResource(R.string.calculator_total_credits, percentText(totalCredits)) else null
            ) {
                if (withoutCredits > 0) {
                    Text(
                        text = if (withoutCredits == 1) {
                            stringResource(R.string.calculator_one_missing_credits)
                        } else {
                            stringResource(R.string.calculator_multi_missing_credits, withoutCredits)
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
            EnterOnAppear {
            SemesterRow(
                subject = rows[index],
                scale = scale,
                editing = editing == index,
                onEdit = {
                    editing = index
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
        }
        item("acciones") {
            val isEn = java.util.Locale.getDefault().language == "en"
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlineChip(
                    text = if (isEn) "Import my courses" else "Traer mis materias",
                    icon = Icons.Rounded.Add,
                    onClick = { pickerOpen = true }
                )
                if (rows.isNotEmpty()) {
                    OutlineChip(text = if (isEn) "Clear all" else "Vaciar", icon = null, onClick = {
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
    // Las tres arrancan en blanco.
    //
    // Traían un cero y la meta del perfil ya puestos, y con eso la tarjeta de abajo daba una
    // respuesta antes de que nadie hubiera escrito nada: un número inventado que parecía tuyo.
    var have by rememberSaveable { mutableStateOf("") }
    var done by rememberSaveable { mutableStateOf("") }
    var goal by rememberSaveable { mutableStateOf("") }
    var field by rememberSaveable { mutableStateOf(0) }
    var fresh by rememberSaveable { mutableStateOf(true) }

    val haveValue = parseTyped(have)
    val doneValue = parseTyped(done)?.coerceIn(0.0, 100.0)
    val goalValue = parseTyped(goal)
    val ready = haveValue != null && doneValue != null && goalValue != null
    val remaining = 100.0 - (doneValue ?: 0.0)
    val needed = if (ready) {
        CalculatorMath.neededGrade(haveValue, doneValue, goalValue)
    } else {
        null
    }
    val impossible = needed != null && needed > maxGrade
    val already = needed != null && needed <= 0.0

    val container = when {
        !ready || needed == null -> MaterialTheme.colorScheme.surfaceContainer
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
                    stringResource(R.string.calculator_course_percent_range)
                } else {
                    stringResource(R.string.calculator_max_scale_limit, GradingScaleUtils.formatGrade(maxGrade, scale))
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
                        0 -> have = ""
                        1 -> done = ""
                        else -> goal = ""
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
                        label = stringResource(R.string.calculator_target_current_gpa),
                        hint = stringResource(R.string.calculator_target_current_desc),
                        value = have,
                        suffix = "",
                        active = field == 0,
                        onClick = { field = 0; fresh = true; onToast(null) }
                    )
                    NeededField(
                        label = stringResource(R.string.calculator_target_evaluated),
                        hint = stringResource(R.string.calculator_target_evaluated_desc),
                        value = done,
                        suffix = " %",
                        active = field == 1,
                        onClick = { field = 1; fresh = true; onToast(null) }
                    )
                    NeededField(
                        label = stringResource(R.string.calculator_target_desired),
                        hint = stringResource(R.string.calculator_target_desired_desc),
                        value = goal,
                        suffix = "",
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
                    label = if (java.util.Locale.getDefault().language == "en") "DONE" else "HECHO",
                    value = have.ifBlank { "—" },
                    // El mínimo sube a un tercio: con el 18 % la caja medía 60 dp y el
                    // rótulo salía cortado como «HEC».
                    weight = ((doneValue ?: 50.0) / 100.0).toFloat().coerceIn(0.34f, 0.72f),
                    container = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                HalfBox(
                    label = if (java.util.Locale.getDefault().language == "en") "SCORE NEEDED" else "TE FALTA SACAR",
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
                CalculatorFootnote(
                    if (doneValue == null) (if (java.util.Locale.getDefault().language == "en") "not evaluated" else "sin evaluar") else (if (java.util.Locale.getDefault().language == "en") "${percentText(doneValue)}% evaluated" else "${percentText(doneValue)} % evaluado")
                )
                CalculatorFootnote(
                    if (doneValue == null) (if (java.util.Locale.getDefault().language == "en") "unfilled" else "sin rellenar") else stringResource(R.string.calculator_remaining_to_eval, percentText(remaining))
                )
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
                        !ready -> stringResource(R.string.calculator_target_fill_fields)
                        needed == null -> stringResource(R.string.calculator_target_all_evaluated)
                        already -> stringResource(R.string.calculator_target_already_achieved)
                        impossible -> stringResource(R.string.calculator_target_impossible, GradingScaleUtils.formatGrade(maxGrade, scale))
                        else -> if (java.util.Locale.getDefault().language == "en") {
                            "You need to score ${GradingScaleUtils.formatGrade(needed, scale)} on the remaining ${percentText(remaining)}%."
                        } else {
                            "Necesitas sacar ${GradingScaleUtils.formatGrade(needed, scale)} en el ${percentText(remaining)} % que te queda."
                        }
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
                // Late al cambiar el valor, no al pulsar una tecla: es la respuesta a lo que
                // acabas de hacer, y si latiera con cada digito dejaria de significar nada.
                val beat = bumpScale(value)
                Text(
                    text = value,
                    modifier = Modifier.graphicsLayer {
                        scaleX = beat
                        scaleY = beat
                        // Crece desde la esquina de abajo a la izquierda: el numero esta alineado
                        // ahi con su sufijo, y escalar desde el centro lo separaria de el.
                        transformOrigin = TransformOrigin(0f, 1f)
                    },
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
private fun GradeRow(
    grade: String,
    weight: String,
    tone: androidx.compose.ui.graphics.Color,
    onRemove: () -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 11.dp, end = 11.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // El cuadro va relleno, no teñido al 18 %: es la mancha que empareja la fila con
            // su tramo de la barra, y un lavado de color no empareja con nada.
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(tone),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = weight,
                    color = LocalVividAccents.current.inkOn(tone),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grade,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = stringResource(R.string.calculator_weight_badge, weight),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Surface(
                onClick = onRemove,
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = if (java.util.Locale.getDefault().language == "en") "Remove" else "Quitar",
                        modifier = Modifier.size(15.dp)
                    )
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
                    .cleanClickable(onClick = onEdit),
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
                    text = if (subject.credits > 0.0) "${percentText(subject.credits)} cr" else (if (java.util.Locale.getDefault().language == "en") "no cr" else "sin cr"),
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
                        contentDescription = (if (java.util.Locale.getDefault().language == "en") "Remove " else "Quitar ") + subject.name,
                        modifier = Modifier.size(15.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Una de las tres cosas que hay que rellenar.
 *
 * La caja de la derecha lleva contorno y, vacía, un guion en gris: sin eso, tres números
 * puestos parecían un resumen de algo y no tres huecos que rellenar. La activa se rellena y
 * saca un cursor, que es lo que dice dónde va a caer lo que se teclee abajo.
 */
@Composable
private fun NeededField(
    label: String,
    hint: String,
    value: String,
    suffix: String,
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
                .cleanClickable(onClick = onClick)
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
            // El relleno del campo activo entra y sale con el mismo ritmo que el foco de las
            // otras dos pestanas: aqui no hay un rectangulo que viaje —los tres campos estan en
            // filas distintas— pero el cambio de sitio se sigue contando, no se salta.
            val fill by animateColorAsState(
                targetValue = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "relleno del campo"
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = fill,
                contentColor = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                border = if (active) {
                    null
                } else {
                    androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                }
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(min = 78.dp)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = if (value.isBlank()) "—" else value + suffix,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            active -> MaterialTheme.colorScheme.onPrimary
                            value.isBlank() -> MaterialTheme.colorScheme.outline
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (active) {
                        Spacer(Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .size(width = 2.dp, height = 19.dp)
                                .background(MaterialTheme.colorScheme.onPrimary)
                        )
                    }
                }
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
    /*
     * El reparto se desliza mientras escribes.
     *
     * Las dos cajas dibujan cuanto del curso llevas evaluado y cuanto falta. Saltaban a su nuevo
     * tamano en cuanto cambiaba un digito, asi que teclear un porcentaje daba una sacudida por
     * cifra. Animado, el reparto se acomoda una vez y se entiende que las dos partes son la
     * misma tarta.
     */
    val share by animateFloatAsState(
        targetValue = weight,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "reparto"
    )
    val tone by animateColorAsState(
        targetValue = container,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "color del veredicto"
    )
    Surface(
        modifier = Modifier
            .weight(share)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = tone,
        contentColor = contentColorOn(tone)
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

