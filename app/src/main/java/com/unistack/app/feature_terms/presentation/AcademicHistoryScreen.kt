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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
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

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsHeader(
                title = "Histórico académico",
                subtitle = "Tus periodos, uno a uno",
                onBackClick = onBackClick
            )
        }

        if (state.loaded && state.summaries.isEmpty()) {
            item {
                TermCard {
                    TermEmptyNote(
                        "Todavía no hay periodos. El primero se crea al configurar la app, y " +
                            "aparece aquí en cuanto exista."
                    )
                }
            }
            return@LazyColumn
        }

        /*
         * El acumulado solo cuenta lo cerrado.
         *
         * Meter el periodo en curso lo haría bailar cada vez que se registra una nota, y un
         * promedio de carrera que cambia a diario no es un promedio de carrera.
         */
        item {
            TermCard {
                Row(verticalAlignment = Alignment.Top) {
                    TermStat(
                        label = "Promedio acumulado",
                        value = state.cumulativeAverage?.toString(),
                        modifier = Modifier.weight(1f),
                        note = if (state.closedCount == 0) {
                            "Aparece al cerrar tu primer periodo"
                        } else {
                            "${state.closedCount} ${if (state.closedCount == 1) "periodo cerrado" else "periodos cerrados"} · " +
                                "${state.subjectsInHistory} ${if (state.subjectsInHistory == 1) "materia" else "materias"}"
                        }
                    )
                }
            }
        }

        val activos = state.summaries.filter { it.term.isActive }
        val cerrados = state.summaries.filter { !it.term.isActive }

        if (activos.isNotEmpty()) {
            item { TermLabel("EN CURSO", Modifier.padding(start = 4.dp, top = 6.dp)) }
            items(activos, key = { it.term.id }) { resumen ->
                TermRow(summary = resumen, onClick = { onTermClick(resumen.term.id) })
            }
            item {
                UniStackButton(
                    text = "Cerrar el periodo",
                    onClick = onCloseTermClick,
                    variant = UniStackButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                )
            }
        }

        if (cerrados.isNotEmpty()) {
            item { TermLabel("CERRADOS", Modifier.padding(start = 4.dp, top = 10.dp)) }
            items(cerrados, key = { it.term.id }) { resumen ->
                TermRow(summary = resumen, onClick = { onTermClick(resumen.term.id) })
            }
        }

        item {
            Text(
                text = "Un periodo cerrado sigue siendo editable: las notas llegan tarde y los " +
                    "profesores corrigen. Lo que no vuelve es a ser el periodo activo.",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}

/** Una fila del histórico: el periodo, sus fechas y las dos cifras que se miran. */
@Composable
private fun TermRow(summary: TermSummary, onClick: () -> Unit) {
    val colores = LocalSectionColors.current
    TermCard(onClick = onClick) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = summary.term.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = buildString {
                            append(summary.term.start.diaMes())
                            summary.term.closedEpochDay?.let {
                                append(" – ")
                                append(java.time.LocalDate.ofEpochDay(it).diaMesAno())
                            } ?: append(" · en curso")
                            append(" · ")
                            append(summary.subjectCount)
                            append(if (summary.subjectCount == 1) " materia" else " materias")
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp
                    )
                }
                TermStateChip(active = summary.term.isActive)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TermStat(label = "Promedio", value = summary.average?.toString())
                TermStat(label = "Asistencia", value = summary.attendanceRate?.let { "$it%" })
                if (summary.failedCount > 0) {
                    TermStat(
                        label = if (summary.failedCount == 1) "Perdida" else "Perdidas",
                        value = summary.failedCount.toString(),
                        tint = colores.atRisk
                    )
                }
            }
        }
    }
}
