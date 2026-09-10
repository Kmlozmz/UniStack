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
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.components.reacomodoDeLista
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom

/**
 * Tu carrera, periodo a periodo.
 *
 * Es lo que se gana al cerrar: un promedio acumulado que de verdad acumula, las materias
 * repetidas visibles, y la asistencia de cada periodo en su propio marco temporal en vez de
 * una única cifra que mezcla semestres.
 *
 * El activo sale también, al principio y marcado como tal. Un histórico que solo enseña lo
 * cerrado obliga a llevar la cuenta de dónde estás por tu cuenta.
 */
@Composable
fun AcademicHistoryScreen(
    onBackClick: () -> Unit,
    onTermClick: (String) -> Unit,
    onCloseTermClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current

    LargeTitleScaffold(
        title = stringResource(R.string.terms_history_title),
        subtitle = stringResource(R.string.terms_history_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 10.dp
    ) {
        if (state.loaded && state.summaries.isEmpty()) {
            item {
                TermCard {
                    TermEmptyNote(
                        stringResource(R.string.terms_empty_history)
                    )
                }
            }
        } else {
            /*
             * El acumulado solo cuenta lo cerrado.
             *
             * Meter el periodo en curso lo haría bailar cada vez que se registra una nota, y un
             * promedio de carrera que cambia a diario no es un promedio de carrera.
             */
            item {
                TermCard {
                    Row(verticalAlignment = Alignment.Top) {
                        val closedNote = if (state.closedCount == 0) {
                            stringResource(R.string.terms_cum_average_placeholder)
                        } else {
                            val cCount = if (state.closedCount == 1) stringResource(R.string.terms_closed_count_single) else stringResource(R.string.terms_closed_count_multiple, state.closedCount)
                            val sCount = if (state.subjectsInHistory == 1) stringResource(R.string.terms_subjects_count_single) else stringResource(R.string.terms_subjects_count_multiple, state.subjectsInHistory)
                            "$cCount · $sCount"
                        }
                        TermStat(
                            label = stringResource(R.string.terms_stat_cum_average),
                            value = state.cumulativeAverage?.toString(),
                            modifier = Modifier.weight(1f),
                            note = closedNote
                        )
                    }
                }
            }

            val activos = state.summaries.filter { it.term.isActive }
            val cerrados = state.summaries.filter { !it.term.isActive }

            if (activos.isNotEmpty()) {
                item { TermLabel(stringResource(R.string.terms_active_section), Modifier.padding(start = 4.dp, top = 6.dp)) }
                items(activos, key = { it.term.id }) { resumen ->
                    TermRow(
                        summary = resumen,
                        onClick = { onTermClick(resumen.term.id) },
                        modifier = reacomodoDeLista()
                    )
                }
                item {
                    UniStackButton(
                        text = stringResource(R.string.terms_btn_close_term),
                        onClick = onCloseTermClick,
                        variant = UniStackButtonVariant.Outlined,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                    )
                }
            }

            if (cerrados.isNotEmpty()) {
                item { TermLabel(stringResource(R.string.terms_closed_section), Modifier.padding(start = 4.dp, top = 10.dp)) }
                items(cerrados, key = { it.term.id }) { resumen ->
                    TermRow(
                        summary = resumen,
                        onClick = { onTermClick(resumen.term.id) },
                        modifier = reacomodoDeLista()
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.terms_closed_note),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/** Una fila del histórico: el periodo, sus fechas y las dos cifras que se miran. */
@Composable
private fun TermRow(
    summary: TermSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = LocalSectionColors.current
    TermCard(onClick = onClick, modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = summary.term.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    val inProgressLabel = stringResource(R.string.terms_row_in_progress)
                    val sCountLabel = if (summary.subjectCount == 1) stringResource(R.string.terms_subjects_count_single) else stringResource(R.string.terms_subjects_count_multiple, summary.subjectCount)
                    Text(
                        text = buildString {
                            append(summary.term.start.diaMes())
                            summary.term.closedEpochDay?.let {
                                append(" – ")
                                append(java.time.LocalDate.ofEpochDay(it).diaMesAno())
                            } ?: append(" · $inProgressLabel")
                            append(" · ")
                            append(sCountLabel)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
                TermStateChip(active = summary.term.isActive)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TermStat(label = stringResource(R.string.terms_stat_average), value = summary.average?.toString())
                TermStat(label = stringResource(R.string.terms_stat_attendance), value = summary.attendanceRate?.let { "$it%" })
                if (summary.failedCount > 0) {
                    TermStat(
                        label = if (summary.failedCount == 1) stringResource(R.string.terms_stat_failed_single) else stringResource(R.string.terms_stat_failed_multiple),
                        value = summary.failedCount.toString(),
                        tint = colores.atRisk
                    )
                }
            }
        }
    }
}
