package com.unistack.app.feature_setup.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.domain.Corte
import java.time.LocalDate

/**
 * Paso 1 de 3 del periodo: cómo se organiza el calendario.
 *
 * La cinta va horizontal y con el nombre a secas; el detalle —«2 al año, unas 16 semanas»—
 * sale debajo, y solo el de la elegida. Con cinco tarjetas apiladas explicándose todas a la
 * vez, la pantalla se convertía en un muro y había que desplazarse para llegar a lo siguiente.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupTermTypeScreen(
    type: AcademicTermType?,
    cutCount: Int,
    cutWeights: List<String>,
    onTypeSelected: (AcademicTermType) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 8
) {
    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = SetupSteps.Term,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = type != null,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SetupPlainTitle(
                title = "Tu periodo",
                subtitle = "Cómo se organiza el calendario de tu universidad."
            )

            // Lo que ya quedó resuelto en el paso anterior, para no perder el hilo.
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(LocalSectionColors.current.onTrack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "$cutCount ${if (cutCount == 1) Corte.Singular.lowercase() else Corte.Plural.lowercase()}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmallEmphasized
                        )
                        Text(
                            text = cutWeights.filter { it.isNotBlank() }.joinToString(" · ") { "$it%" },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            SetupSectionLabel("¿Cómo se manejan los periodos académicos en tu universidad?")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AcademicTermType.entries.forEach { opcion ->
                    TermTypeChip(
                        label = opcion.label,
                        selected = type == opcion,
                        onClick = { onTypeSelected(opcion) }
                    )
                }
            }

            if (type != null) {
                UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = type.label,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmallEmphasized
                        )
                        Text(
                            text = type.detail,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Elige una para continuar",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Paso 2 de 3: las fechas.
 *
 * El inicio es el dato que faltaba en toda la app. El fin se pide como **previsión** —en agosto
 * nadie sabe el día exacto— y la pantalla lo dice en voz alta para que nadie espere que el
 * periodo se cierre solo cuando llegue.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupTermDatesScreen(
    type: AcademicTermType,
    name: String,
    cutCount: Int,
    start: LocalDate,
    plannedEnd: LocalDate?,
    knowsCutDates: Boolean?,
    onStartChange: (LocalDate) -> Unit,
    onPlannedEndChange: (LocalDate) -> Unit,
    onKnowsCutDatesChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 8
) {
    var picking by remember { mutableStateOf<TermDateTarget?>(null) }

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = SetupSteps.TermDates,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Continuar",
                onClick = onContinueClick,
                enabled = knowsCutDates != null,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SetupPlainTitle(
                title = "Cuándo empieza",
                subtitle = "Con esto la app sabe qué clases existieron y cuáles no."
            )

            SetupSectionLabel("Tu periodo")
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "${type.label} · $cutCount ${if (cutCount == 1) Corte.Singular.lowercase() else Corte.Plural.lowercase()}",
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmallEmphasized
                        )
                        Text(
                            text = name,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            SetupSectionLabel("Fechas")
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                TermDateField(
                    label = "Empieza",
                    date = start,
                    modifier = Modifier.weight(1f),
                    onClick = { picking = TermDateTarget.START }
                )
                TermDateField(
                    label = "Acaba (previsto)",
                    date = plannedEnd,
                    modifier = Modifier.weight(1f),
                    onClick = { picking = TermDateTarget.PLANNED_END }
                )
            }

            UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 15.sp)
                    Text(
                        text = "La fecha de fin es solo una previsión. El periodo no se cierra hasta que tú lo cierres.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            if (cutCount > 1) {
                SetupSectionLabel("¿Sabes cuándo cierra cada ${Corte.Singular.lowercase()}?")
                UniSegmentedControl(
                    selected = knowsCutDates,
                    options = listOf(
                        UniSegmentedOption<Boolean?>(value = true, label = "Sí, las sé"),
                        UniSegmentedOption<Boolean?>(value = false, label = "Todavía no")
                    ),
                    onSelected = { valor -> valor?.let(onKnowsCutDatesChange) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    picking?.let { target ->
        UniDatePickerDialog(
            selectedDate = if (target == TermDateTarget.START) start else plannedEnd,
            onDateSelected = { fecha ->
                if (target == TermDateTarget.START) onStartChange(fecha) else onPlannedEndChange(fecha)
                picking = null
            },
            onDismiss = { picking = null }
        )
    }
}

/**
 * Paso 3 de 3: dónde se corta.
 *
 * Se pide **solo el día en que acaba cada corte**, no sus dos extremos. El siguiente empieza al
 * día posterior y el último acaba con el periodo, así que los solapes y los huecos no pueden
 * existir: no hay forma de escribirlos, y por tanto no hay nada que validar ni que explicar.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupTermCutDatesScreen(
    start: LocalDate,
    plannedEnd: LocalDate?,
    cutWeights: List<String>,
    cutEndDates: List<LocalDate>,
    isValid: Boolean,
    onCutDateChange: (Int, LocalDate) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 9
) {
    var picking by remember { mutableStateOf<Int?>(null) }
    val total = cutWeights.size

    BackHandler(onBack = onBackClick)
    SetupScaffold(
        onBackClick = onBackClick,
        step = SetupSteps.TermCutDates,
        totalSteps = totalSteps,
        modifier = modifier,
        actions = {
            UniStackButton(
                text = "Terminar",
                onClick = onContinueClick,
                enabled = isValid,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight
            )
            UniStackButton(
                text = "Dejarlo para después",
                onClick = onBackClick,
                variant = UniStackButtonVariant.Outlined
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SetupPlainTitle(
                title = "¿Dónde se corta?",
                subtitle = "Solo el día en que acaba cada ${Corte.Singular.lowercase()}. El siguiente empieza al día siguiente."
            )

            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = "Empieza el periodo",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Text(
                        text = termDateLabel(start),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            cutWeights.forEachIndexed { index, peso ->
                val ultimo = index == total - 1
                UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "${Corte.Singular} ${index + 1}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.titleSmallEmphasized
                                )
                                Text(
                                    text = "$peso% de la nota",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (ultimo) {
                            Text(
                                text = plannedEnd?.let { "Acaba con el periodo, el ${termDateLabel(it)}." }
                                    ?: "Acaba con el periodo.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        } else {
                            TermDateField(
                                label = "Acaba el",
                                date = cutEndDates.getOrNull(index),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { picking = index }
                            )
                        }
                    }
                }
            }

            Text(
                text = "${total - 1} ${if (total - 1 == 1) "fecha" else "fechas"}, no ${total * 2}. Sin huecos ni solapes posibles.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }

    picking?.let { index ->
        UniDatePickerDialog(
            selectedDate = cutEndDates.getOrNull(index) ?: start,
            onDateSelected = { fecha ->
                onCutDateChange(index, fecha)
                picking = null
            },
            onDismiss = { picking = null }
        )
    }
}

/** Cuál de las dos fechas del periodo se está eligiendo. */
private enum class TermDateTarget { START, PLANNED_END }

/** Una ficha de la cinta: el nombre a secas, que el detalle va debajo. */
@Composable
private fun TermTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SetupSectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun TermDateField(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
            Text(
                text = date?.let(::termDateLabel) ?: "Elegir",
                color = if (date != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.primary
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private val MesesCortos =
    listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")

private fun termDateLabel(date: LocalDate): String =
    "${date.dayOfMonth} ${MesesCortos[date.monthValue - 1]} ${date.year}"
