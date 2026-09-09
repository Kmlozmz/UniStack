@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_user.domain.Corte
import java.time.LocalDate

/** Cuál de las dos fechas se está eligiendo. */
private enum class QueFecha { EMPIEZA, ACABA }

/**
 * El periodo nuevo: se hereda lo que no cambia y se pregunta lo que sí.
 *
 * De un semestre a otro no cambian la escala, la nota de aprobado ni el reparto de los cortes,
 * así que no se vuelven a preguntar: se enseñan para que se puedan corregir si acaso. Lo que
 * cambia siempre son las fechas, y eso es lo único obligatorio aquí.
 *
 * Abajo aparecen las materias que se perdieron, para traerlas. Se crean **vacías**: repetir es
 * cursarla otra vez, no arrastrar las notas ni el horario de la vez que salió mal.
 */
@Composable
fun NewTermScreen(
    onBackClick: () -> Unit,
    onCreated: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val colores = LocalSectionColors.current
    val herencia = state.inheritance
    val perdidas = remember(state.lastClosed) {
        state.lastClosed?.subjects?.filter { it.passed == false }.orEmpty()
    }

    var inicio by rememberSaveable(stateSaver = FechaSaver) { mutableStateOf<LocalDate?>(null) }
    var fin by rememberSaveable(stateSaver = FechaSaver) { mutableStateOf<LocalDate?>(null) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var eligiendo by remember { mutableStateOf<QueFecha?>(null) }
    var aRepetir by rememberSaveable { mutableStateOf(setOf<String>()) }

    val nombreFinal = nombre.trim().ifBlank {
        val tipo = herencia?.type
        if (tipo != null && inicio != null) AcademicTerm.suggestedName(tipo, inicio!!) else ""
    }
    val valido = inicio != null && nombreFinal.isNotBlank() &&
        (fin == null || fin!!.isAfter(inicio))

    LargeTitleScaffold(
        title = stringResource(R.string.terms_new_title),
        subtitle = state.lastClosed?.let { stringResource(R.string.terms_new_subtitle_inherit, it.term.name) }
            ?: stringResource(R.string.terms_new_subtitle_fresh),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 10.dp
    ) {

        if (herencia != null) {
            item { TermLabel(stringResource(R.string.terms_section_retained), Modifier.padding(start = 4.dp, top = 4.dp)) }
            item {
                TermCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TermDoneRow(stringResource(R.string.terms_retain_scale, herencia.scaleLabel, herencia.passingLabel))
                        TermDoneRow(
                            "${herencia.cutCount} ${Corte.Plural.lowercase()} · " +
                                herencia.cutWeights.joinToString(" · ") { "$it%" }
                        )
                        TermDoneRow(herencia.type.label)
                        Text(
                            text = stringResource(R.string.terms_retain_setting_hint),
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item { TermLabel(stringResource(R.string.terms_section_required), Modifier.padding(start = 4.dp, top = 6.dp)) }
        item {
            TermCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FechaDelPeriodo(
                            label = stringResource(R.string.terms_field_starts),
                            date = inicio,
                            vacio = stringResource(R.string.terms_field_choose),
                            modifier = Modifier.weight(1f),
                            onClick = { eligiendo = QueFecha.EMPIEZA }
                        )
                        FechaDelPeriodo(
                            label = stringResource(R.string.terms_field_ends_planned),
                            date = fin,
                            vacio = stringResource(R.string.terms_field_choose),
                            modifier = Modifier.weight(1f),
                            onClick = { eligiendo = QueFecha.ACABA }
                        )
                    }
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it.take(40) },
                        label = { Text(stringResource(R.string.terms_field_name)) },
                        placeholder = { Text(nombreFinal.ifBlank { "2026-2" }) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        // El fin es una prevision, y decirlo evita que parezca un compromiso.
                        text = stringResource(R.string.terms_planned_end_hint),
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        if (perdidas.isNotEmpty()) {
            item { TermLabel(stringResource(R.string.terms_failed_subjects_section), Modifier.padding(start = 4.dp, top = 6.dp)) }
            items(perdidas, key = { it.id }) { materia ->
                val elegida = materia.id in aRepetir
                TermCard(
                    onClick = {
                        aRepetir = if (elegida) aRepetir - materia.id else aRepetir + materia.id
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = materia.name,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.terms_failed_closed_at, materia.average ?: "—"),
                                color = colores.atRisk,
                                fontSize = 11.5.sp
                            )
                        }
                        Text(
                            text = if (elegida) stringResource(R.string.terms_btn_remove) else stringResource(R.string.terms_btn_bring),
                            color = if (elegida) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            item {
                Text(
                    text = stringResource(R.string.terms_failed_empty_note),
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }
        }

        item {
            UniStackButton(
                text = stringResource(R.string.terms_btn_create),
                onClick = {
                    viewModel.startTerm(
                        name = nombreFinal,
                        type = herencia?.type ?: com.unistack.app.feature_terms.domain.AcademicTermType.SEMESTER,
                        start = inicio ?: return@UniStackButton,
                        plannedEnd = fin,
                        repeatSubjectIds = aRepetir,
                        onDone = onCreated
                    )
                },
                enabled = valido,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }
    }

    eligiendo?.let { cual ->
        UniDatePickerDialog(
            selectedDate = if (cual == QueFecha.EMPIEZA) inicio else fin,
            onDateSelected = { fecha ->
                if (cual == QueFecha.EMPIEZA) {
                    inicio = fecha
                    // Mover el inicio deja el fin previsto sin sentido si quedaba antes.
                    if (fin != null && !fin!!.isAfter(fecha)) fin = null
                } else {
                    fin = fecha
                }
                eligiendo = null
            },
            onDismiss = { eligiendo = null }
        )
    }
}

@Composable
private fun FechaDelPeriodo(
    label: String,
    date: LocalDate?,
    vacio: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        onClick = onClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            Text(
                text = date?.diaMesAno() ?: vacio,
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

/** Las fechas sobreviven a un giro de pantalla; el `Bundle` solo entiende texto. */
private val FechaSaver = androidx.compose.runtime.saveable.Saver<LocalDate?, String>(
    save = { it?.toString() ?: "" },
    restore = { texto -> texto.takeIf { it.isNotEmpty() }?.let(LocalDate::parse) }
)
