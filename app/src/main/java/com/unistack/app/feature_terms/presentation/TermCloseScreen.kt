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

    LargeTitleScaffold(
        title = "Antes de cerrar",
        subtitle = term?.let { "${it.name} · desde el ${it.start.diaMes()}" },
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 10.dp
    ) {
        if (term == null) {
            item { TermEmptyNote("No hay ningún periodo en curso que cerrar.") }
        } else if (report == null) {
            item { TermEmptyNote("Revisando el periodo…") }
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
                                "El periodo está completo. Puedes cerrarlo cuando quieras."
                            } else {
                                "Puedes cerrar igualmente. Esto solo es para que no te enteres después."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            if (report.gaps.isNotEmpty()) {
                item { TermLabel("LO QUE FALTA", Modifier.padding(start = 4.dp, top = 6.dp)) }
                items(report.gaps.size) { indice ->
                    val hueco: TermGap = report.gaps[indice]
                    TermGapRow(title = hueco.title(), detail = hueco.detail())
                }
            }

            item { TermLabel("LO QUE SÍ ESTÁ COMPLETO", Modifier.padding(start = 4.dp, top = 6.dp)) }
            item {
                TermCard {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        TermDoneRow(
                            "${report.subjectsWithEverything} de ${report.subjectsTotal} " +
                                "${if (report.subjectsTotal == 1) "materia" else "materias"} con todas las notas"
                        )
                        report.average?.let { promedio ->
                            TermDoneRow("Promedio del periodo: $promedio")
                        }
                        if (report.failed.isNotEmpty()) {
                            TermDoneRow(
                                "${report.failed.size} " +
                                    "${if (report.failed.size == 1) "materia perdida" else "materias perdidas"}: " +
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
                            text = "Ir a completarlas",
                            onClick = onGoComplete,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    UniStackButton(
                        text = if (report.isClean) "Cerrar el periodo" else "Cerrar de todas formas",
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
        val puedeCerrar = limpio || escrito.trim().equals(PALABRA_DE_CIERRE, ignoreCase = true)
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("Cerrar ${term.name}") },
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
                            "El periodo pasará al histórico y no podrá volver a ser el activo."
                        } else {
                            "Vas a cerrar con ${report?.gaps?.size} cosas sin terminar. El " +
                                "periodo pasará al histórico y no podrá volver a ser el activo."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TermDoneRow("Editar notas desde el histórico")
                        TermDoneRow("Consultar asistencias y promedios")
                        Row {
                            Text(
                                text = "✕  Volver a activarlo — esto no",
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
                            label = { Text("Escribe $PALABRA_DE_CIERRE") },
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
                        "Cerrar el periodo",
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
                    Text("Volver", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}
