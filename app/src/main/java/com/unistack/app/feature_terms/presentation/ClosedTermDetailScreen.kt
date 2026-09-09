@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.itemsIndexed
import com.unistack.app.core.design.components.resumenDePeriodo
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import java.time.LocalDate

/**
 * Dentro de un periodo: todo lo relevante, no solo el promedio.
 *
 * Promedio y materias, sí. Pero también la asistencia real de aquel periodo **con su alcance**
 * —«desde el 20 sept»— y las que se perdieron. Un histórico que solo enseña un número es
 * decorativo, y con un número sin alcance es además engañoso: si la app se instaló a mitad de
 * semestre, las semanas anteriores no las marcó nadie.
 */
@Composable
fun ClosedTermDetailScreen(
    termId: String,
    onBackClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val colores = LocalSectionColors.current
    val resumen = state.summaries.firstOrNull { it.term.id == termId }

    val title = resumen?.term?.name ?: stringResource(R.string.terms_detail_default_title)
    val inProgLabel = stringResource(R.string.terms_row_in_progress)
    val subtitle = resumen?.let { r ->
        buildString {
            append(r.term.start.diaMes())
            append(" – ")
            append(
                r.term.closedEpochDay
                    ?.let { LocalDate.ofEpochDay(it).diaMesAno() }
                    ?: inProgLabel
            )
            append(" · ")
            append(r.term.type.label)
        }
    } ?: stringResource(R.string.terms_detail_default_subtitle)

    LargeTitleScaffold(
        title = title,
        subtitle = subtitle,
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 10.dp
    ) {
        if (resumen == null) {
            item {
                TermEmptyNote(
                    if (state.loaded) stringResource(R.string.terms_term_not_found) else stringResource(R.string.terms_loading)
                )
            }
        } else {
            val term = resumen.term
            item {
                TermCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        TermStat(
                            label = stringResource(R.string.terms_stat_average),
                            value = resumen.average?.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        TermStat(
                            label = stringResource(R.string.terms_stat_attendance),
                            value = resumen.attendanceRate?.let { "$it%" },
                            modifier = Modifier.weight(1f),
                            // El alcance va pegado a la cifra: sin él, un 91% no dice de qué.
                            note = resumen.attendanceSince?.let { stringResource(R.string.terms_attendance_since, it.diaMes()) }
                        )
                        TermStat(
                            label = if (resumen.failedCount == 1) stringResource(R.string.terms_stat_failed_single) else stringResource(R.string.terms_stat_failed_multiple),
                            value = resumen.failedCount.toString(),
                            modifier = Modifier.weight(1f),
                            tint = if (resumen.failedCount > 0) colores.atRisk else null
                        )
                    }
                }
            }

            if (resumen.subjects.isNotEmpty()) {
                item {
                    val headerLabel = if (resumen.subjects.size == 1) stringResource(R.string.terms_subjects_header_single) else stringResource(R.string.terms_subjects_header_multiple, resumen.subjects.size)
                    TermLabel(
                        headerLabel,
                        Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }
                itemsIndexed(resumen.subjects, key = { _, it -> it.id }) { indice, materia ->
                    Box(modifier = Modifier.resumenDePeriodo(indice)) {
                        SubjectRowInTerm(subject = materia, onClick = { onSubjectClick(materia.id) })
                    }
                }
            }

            item {
                Text(
                    text = if (term.isActive) {
                        stringResource(R.string.terms_active_explainer)
                    } else {
                        stringResource(R.string.terms_closed_explainer)
                    },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SubjectRowInTerm(subject: SubjectInTerm, onClick: () -> Unit) {
    val colores = LocalSectionColors.current
    TermCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subject.name.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Column(
                Modifier.weight(1f).padding(horizontal = 11.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = subject.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                val attRateText = subject.attendanceRate?.let { stringResource(R.string.terms_attendance_rate, it) } ?: stringResource(R.string.terms_no_classes_marked)
                Text(
                    text = attRateText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = subject.average?.toString() ?: "—",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black
                )
                if (subject.passed == false) {
                    Surface(shape = CircleShape, color = colores.atRisk.copy(alpha = 0.18f)) {
                        Text(
                            text = stringResource(R.string.terms_stat_failed_single),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = colores.atRisk,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
