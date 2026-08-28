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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsHeader
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

    if (resumen == null) {
        Column(modifier.fillMaxSize().statusBarsPadding()) {
            SettingsHeader(title = "Periodo", subtitle = "Histórico", onBackClick = onBackClick)
            TermEmptyNote(
                if (state.loaded) "Este periodo ya no existe." else "Cargando el periodo…"
            )
        }
        return
    }

    val term = resumen.term
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
                title = term.name,
                subtitle = buildString {
                    append(term.start.diaMes())
                    append(" – ")
                    append(
                        term.closedEpochDay
                            ?.let { LocalDate.ofEpochDay(it).diaMesAno() }
                            ?: "en curso"
                    )
                    append(" · ")
                    append(term.type.label)
                },
                onBackClick = onBackClick
            )
        }

        item {
            TermCard {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    TermStat(
                        label = "Promedio",
                        value = resumen.average?.toString(),
                        modifier = Modifier.weight(1f)
                    )
                    TermStat(
                        label = "Asistencia",
                        value = resumen.attendanceRate?.let { "$it%" },
                        modifier = Modifier.weight(1f),
                        // El alcance va pegado a la cifra: sin él, un 91% no dice de qué.
                        note = resumen.attendanceSince?.let { "desde el ${it.diaMes()}" }
                    )
                    TermStat(
                        label = if (resumen.failedCount == 1) "Perdida" else "Perdidas",
                        value = resumen.failedCount.toString(),
                        modifier = Modifier.weight(1f),
                        tint = if (resumen.failedCount > 0) colores.atRisk else null
                    )
                }
            }
        }

        item { TermLabel("MATERIAS", Modifier.padding(start = 4.dp, top = 6.dp)) }

        if (resumen.subjects.isEmpty()) {
            item { TermCard { TermEmptyNote("Este periodo no tiene materias registradas.") } }
        } else {
            items(resumen.subjects, key = { it.id }) { materia ->
                SubjectRowInTerm(subject = materia, onClick = { onSubjectClick(materia.id) })
            }
        }

        item {
            Text(
                text = if (term.isActive) {
                    "Es el periodo en curso: lo que ves aquí sigue cambiando."
                } else {
                    "Puedes editar las notas de este periodo desde cada materia. Está cerrado, " +
                        "pero cerrado no quiere decir bloqueado."
                },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )
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
                Text(
                    text = subject.attendanceRate?.let { "Asistencia $it%" } ?: "Sin clases marcadas",
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
                            text = "Perdida",
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
