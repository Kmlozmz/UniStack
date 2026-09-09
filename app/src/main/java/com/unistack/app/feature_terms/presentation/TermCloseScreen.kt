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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_terms.domain.TermGap
import com.unistack.app.feature_terms.domain.detail
import com.unistack.app.feature_terms.domain.summaryLine
import com.unistack.app.feature_terms.domain.title
import java.time.LocalDate

/** La palabra que hay que escribir cuando quedan cosas a medias. */
private const val PALABRA_DE_CIERRE = "CERRAR"

/**
 * Antes de cerrar: lo que el periodo deja a medias.
 *
 * No impide cerrar —cerrar es del usuario— sino cerrar **a ciegas**, que es lo único malo de
 * una acción irreversible. Y la fricción se gana: con todo en orden basta una confirmación;
 * con cosas sin terminar hay que escribir la palabra, porque un botón rojo se pulsa sin leer
 * y lo que hay que hacer aquí es mirar la lista.
 */
@Composable
fun TermCloseScreen(
    onBackClick: () -> Unit,
    onGoComplete: () -> Unit,
    onClosed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val colores = LocalSectionColors.current
    val term = state.activeTerm
    val report = state.report

    var confirmando by rememberSaveable { mutableStateOf(false) }
    var escrito by rememberSaveable { mutableStateOf("") }

    val isEn = java.util.Locale.getDefault().language == "en"
    LargeTitleScaffold(
        title = stringResource(R.string.terms_close_screen_title),
        subtitle = term?.let { "${it.name} · " + stringResource(R.string.terms_attendance_since, it.start.diaMes()) },
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 10.dp
    ) {
        if (term == null) {
            item { TermEmptyNote(stringResource(R.string.terms_close_empty_term)) }
        } else if (report == null) {
            item { TermEmptyNote(stringResource(R.string.terms_close_reviewing)) }
        } else {
            item {
                TermCard {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = report.gaps.summaryLine(),
                            color = if (report.isClean) colores.onTrack else MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = if (report.isClean) {
                                stringResource(R.string.terms_close_clean_msg)
                            } else {
                                stringResource(R.string.terms_close_unclean_msg)
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            if (report.gaps.isNotEmpty()) {
                item { TermLabel(stringResource(R.string.terms_section_missing), Modifier.padding(start = 4.dp, top = 6.dp)) }
                items(report.gaps.size) { indice ->
                    val hueco: TermGap = report.gaps[indice]
                    TermGapRow(title = hueco.title(), detail = hueco.detail())
                }
            }

            item { TermLabel(stringResource(R.string.terms_section_complete), Modifier.padding(start = 4.dp, top = 6.dp)) }
            item {
                TermCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val subText = if (report.subjectsTotal == 1) stringResource(R.string.terms_subjects_count_single) else stringResource(R.string.terms_subjects_count_multiple, report.subjectsTotal)
                        TermDoneRow(
                            stringResource(R.string.terms_done_subjects_notes, report.subjectsWithEverything, report.subjectsTotal, subText)
                        )
                        report.average?.let { promedio ->
                            TermDoneRow(stringResource(R.string.terms_done_average, promedio))
                        }
                        if (report.failed.isNotEmpty()) {
                            val failLabel = if (report.failed.size == 1) stringResource(R.string.terms_stat_failed_single) else stringResource(R.string.terms_stat_failed_multiple)
                            TermDoneRow(
                                "${report.failed.size} $failLabel: " +
                                    report.failed.joinToString { it.name }
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (report.gaps.isNotEmpty()) {
                        UniStackButton(
                            text = stringResource(R.string.terms_btn_go_complete),
                            onClick = onGoComplete,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    UniStackButton(
                        text = if (report.isClean) stringResource(R.string.terms_btn_close_term) else stringResource(R.string.terms_btn_close_anyway),
                        onClick = {
                            escrito = ""
                            confirmando = true
                        },
                        // Con cosas a medias es la accion secundaria: la principal es ir a
                        // terminarlas, y el relleno se lo lleva esa.
                        variant = if (report.isClean) {
                            UniStackButtonVariant.Filled
                        } else {
                            UniStackButtonVariant.Outlined
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (confirmando && term != null) {
        val limpio = report?.isClean != false
        val closeTargetWord = if (isEn) "CLOSE" else PALABRA_DE_CIERRE
        val puedeCerrar = limpio || escrito.trim().equals(PALABRA_DE_CIERRE, ignoreCase = true) || escrito.trim().equals("CLOSE", ignoreCase = true)
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text(stringResource(R.string.terms_dialog_close_title, term.name)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    /*
                     * Lo que se pierde es exactamente una cosa, y hay que decir cuál.
                     *
                     * «Irreversible» a secas se lee como «pierdes los datos», y no es eso: las
                     * notas siguen editándose desde el histórico porque llegan tarde y los
                     * profesores corrigen. Lo que no vuelve es que sea el periodo activo.
                     */
                    Text(
                        text = if (limpio) {
                            stringResource(R.string.terms_dialog_clean_desc)
                        } else {
                            stringResource(R.string.terms_dialog_unclean_desc, report?.gaps?.size ?: 0)
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TermDoneRow(stringResource(R.string.terms_dialog_row_edit_grades))
                        TermDoneRow(stringResource(R.string.terms_dialog_row_view_stats))
                        Row {
                            Text(
                                text = stringResource(R.string.terms_dialog_row_cannot_reactivate),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (!limpio) {
                        OutlinedTextField(
                            value = escrito,
                            onValueChange = { escrito = it.take(10) },
                            label = { Text(stringResource(R.string.terms_dialog_type_prompt, closeTargetWord)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmando = false
                        viewModel.closeActiveTerm(LocalDate.now(), onDone = onClosed)
                    },
                    enabled = puedeCerrar
                ) {
                    Text(
                        stringResource(R.string.terms_btn_close_term),
                        color = if (puedeCerrar) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmando = false }) {
                    Text(stringResource(R.string.common_back), fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}
