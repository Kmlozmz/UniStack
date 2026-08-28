package com.unistack.app.feature_setup.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.domain.Corte
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Paso 1 de 3 del periodo: cómo se organiza el calendario.
 *
 * La cinta va horizontal y con el nombre a secas. Debajo, en vez de repetir lo que acabas de
 * tocar, se enseña **cómo queda tu año**: una barra partida en tantos tramos como periodos
 * caben, con el primero destacado.
 *
 * Ese cambio es la diferencia entre una pantalla viva y una apagada. Antes la tarjeta decía
 * «Semestral» debajo de una ficha que ya decía «Semestral», y ocupaba el sitio de más peso
 * visual con algo que el usuario acababa de leer. Lo que le faltaba era información, no
 * adorno: una barra que cambia al tocar hace que la elección tenga efecto visible.
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

            ResueltoAntes(
                titulo = "$cutCount ${if (cutCount == 1) Corte.Singular.lowercase() else Corte.Plural.lowercase()}",
                detalle = cutWeights.filter { it.isNotBlank() }.joinToString(" · ") { "$it%" }
            )

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

            if (type != null) AnoPartido(type) else EsperandoEleccion()
        }
    }
}

/**
 * Cómo queda el año con el tipo elegido.
 *
 * Los tramos salen de dividir el año entre las semanas típicas de ese tipo, así que anual
 * pinta uno solo y por bloques pinta seis. Es una ilustración, no un calendario: sirve para
 * reconocer «sí, así es lo mío» de un vistazo.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AnoPartido(type: AcademicTermType) {
    val cuantos = (52 / type.weeks).coerceIn(1, 6)
    UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Así queda tu año:",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp)),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(cuantos) { indice ->
                    val primero = indice == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(
                                if (primero) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHighest
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${indice + 1}",
                            color = if (primero) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("enero", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                Text("diciembre", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
            Text(
                text = if (cuantos == 1) {
                    "Uno al año, de unas ${type.weeks} semanas."
                } else {
                    "$cuantos al año, de unas ${type.weeks} semanas cada uno. El primero es el que vas a configurar."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Paso 2 de 3: cuándo empieza.
 *
 * Primero se pregunta si ya empezó, que es algo que cualquiera contesta sin mirar un
 * calendario; de ahí sale una fecha propuesta que sigue siendo editable. Antes venía puesta
 * con la de hoy, así que la pantalla afirmaba en lugar de preguntar, y casi nadie configura
 * la app el día exacto en que arranca su semestre.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupTermDatesScreen(
    type: AcademicTermType,
    name: String,
    cutCount: Int,
    alreadyStarted: Boolean?,
    start: LocalDate?,
    plannedEnd: LocalDate?,
    knowsCutDates: Boolean?,
    onAlreadyStartedChange: (Boolean) -> Unit,
    onStartChange: (LocalDate) -> Unit,
    onPlannedEndChange: (LocalDate) -> Unit,
    onKnowsCutDatesChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    totalSteps: Int = 8
) {
    var picking by remember { mutableStateOf<TermDateTarget?>(null) }
    val listo = start != null && (cutCount <= 1 || knowsCutDates != null)

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
                enabled = listo,
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
                subtitle = "Con esto podremos llevar un mejor orden de tus fechas."
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
                        if (name.isNotBlank()) {
                            Text(
                                text = name,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            SetupSectionLabel("¿Tu periodo ya empezó?")
            UniSegmentedControl(
                selected = alreadyStarted,
                options = listOf(
                    UniSegmentedOption<Boolean?>(value = true, label = "Ya empezó"),
                    UniSegmentedOption<Boolean?>(value = false, label = "Empieza pronto")
                ),
                onSelected = { valor -> valor?.let(onAlreadyStartedChange) },
                modifier = Modifier.fillMaxWidth()
            )

            if (start != null) {
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
                            text = buildString {
                                if (plannedEnd != null) {
                                    append("Son ${semanasEntre(start, plannedEnd)} semanas. ")
                                }
                                append("La fecha de fin es una previsión: el periodo no se cierra hasta que tú lo cierres.")
                            },
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
 * Sigues escribiendo **el día en que acaba** cada corte —eso es lo que hace imposibles los
 * huecos y los solapes, porque el siguiente empieza al día posterior— pero debajo se lee el
 * tramo entero: «27 ago → 3 oct · 5 semanas».
 *
 * Ahí estaba la confusión: la pantalla pedía finales y el usuario piensa en tramos, así que
 * tenía que calcularlos de cabeza. Con el tramo delante, que el corte 2 empiece justo donde
 * acaba el 1 deja de ser una promesa del texto y pasa a verse.
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
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SetupPlainTitle(
                title = "¿Dónde se corta?",
                subtitle = plannedEnd?.let {
                    "Tu periodo va del ${fechaCorta(start)} al ${fechaCorta(it)}. Marca dónde termina cada ${Corte.Singular.lowercase()}."
                } ?: "Marca dónde termina cada ${Corte.Singular.lowercase()}."
            )

            cutWeights.forEachIndexed { indice, peso ->
                val ultimo = indice == total - 1
                // El tramo se calcula: empieza donde acabo el anterior, o con el periodo.
                val desde = if (indice == 0) start else cutEndDates.getOrNull(indice - 1)?.plusDays(1)
                val hasta = if (ultimo) plannedEnd else cutEndDates.getOrNull(indice)

                UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${indice + 1}",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                            }
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "${Corte.Singular} ${indice + 1}",
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

                        if (desde != null && hasta != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Text(
                                    text = fechaCorta(desde),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "→",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = fechaCorta(hasta),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "· ${semanasEntre(desde, hasta)} semanas",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        if (ultimo) {
                            Text(
                                text = "Acaba con el periodo.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        } else {
                            TermDateField(
                                label = "Acaba el",
                                date = cutEndDates.getOrNull(indice),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { picking = indice }
                            )
                        }
                    }
                }
            }
        }
    }

    picking?.let { indice ->
        UniDatePickerDialog(
            selectedDate = cutEndDates.getOrNull(indice) ?: start,
            onDateSelected = { fecha ->
                onCutDateChange(indice, fecha)
                picking = null
            },
            onDismiss = { picking = null }
        )
    }
}

/** Lo que ya quedó resuelto en el paso anterior, para no perder el hilo. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ResueltoAntes(titulo: String, detalle: String) {
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
                    text = titulo,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmallEmphasized
                )
                Text(
                    text = detalle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EsperandoEleccion() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Elige una para ver cómo queda tu año",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
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
internal fun SetupSectionLabel(text: String) {
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
                text = date?.let(::fechaLarga) ?: "Elegir",
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

private fun fechaLarga(date: LocalDate): String =
    "${date.dayOfMonth} ${MesesCortos[date.monthValue - 1]} ${date.year}"

/** Sin el año: en un tramo del mismo periodo, repetirlo tres veces es ruido. */
private fun fechaCorta(date: LocalDate): String =
    "${date.dayOfMonth} ${MesesCortos[date.monthValue - 1]}"

/** Redondeadas: «5 semanas» informa, «4,7 semanas» no. Nunca menos de una. */
private fun semanasEntre(desde: LocalDate, hasta: LocalDate): Long =
    (ChronoUnit.DAYS.between(desde, hasta) / 7).coerceAtLeast(1)
