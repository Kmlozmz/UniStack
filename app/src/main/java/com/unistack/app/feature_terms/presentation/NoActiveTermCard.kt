package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import java.time.LocalDate

/**
 * Entre un periodo y el siguiente, la app no se queda en blanco.
 *
 * Cerrar no vacía nada: cambia a un estado que tiene contenido propio —el resumen de lo que
 * acabas de cerrar y el histórico— y **una sola acción clara**. Abrir la app y encontrarla
 * vacía sin saber qué hacer era justo lo que había que evitar.
 *
 * Aparece solo si hay algo cerrado detrás. Quien todavía no ha configurado nada no está «entre
 * periodos», está empezando, y esa pantalla es otra.
 */
@Composable
fun NoActiveTermCard(
    lastClosed: TermSummary,
    cutCount: Int,
    onStartNewTerm: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cerrado = lastClosed.term.closedEpochDay?.let { LocalDate.ofEpochDay(it) }
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TermCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.terms_no_active_term),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = cerrado
                            ?.let { stringResource(R.string.terms_closed_on_date, lastClosed.term.name, it.diaMes()) }
                            ?: stringResource(R.string.terms_closed_fallback, lastClosed.term.name),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    TermStat(label = stringResource(R.string.terms_stat_average), value = lastClosed.average?.toString())
                    TermStat(label = stringResource(R.string.terms_stat_subjects), value = lastClosed.subjectCount.toString())
                    TermStat(
                        label = stringResource(R.string.terms_stat_attendance),
                        value = lastClosed.attendanceRate?.let { "$it%" }
                    )
                }
            }
        }

        TermCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.terms_started_new_term_q),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    // Lo que se hereda se dice antes de pulsar, para que empezar no dé miedo.
                    text = stringResource(R.string.terms_inherit_config_desc, lastClosed.term.name, cutCount),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
                UniStackButton(
                    text = stringResource(R.string.terms_btn_start_new),
                    onClick = onStartNewTerm,
                    modifier = Modifier.fillMaxWidth()
                )
                UniStackButton(
                    text = stringResource(R.string.terms_btn_view_history),
                    onClick = onOpenHistory,
                    variant = UniStackButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
