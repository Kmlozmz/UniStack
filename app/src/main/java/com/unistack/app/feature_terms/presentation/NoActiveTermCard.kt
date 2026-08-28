package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
                        text = "Sin periodo activo",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = cerrado
                            ?.let { "Cerraste ${lastClosed.term.name} el ${it.diaMes()}." }
                            ?: "Cerraste ${lastClosed.term.name}.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.5.sp
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    TermStat(label = "Promedio", value = lastClosed.average?.toString())
                    TermStat(label = "Materias", value = lastClosed.subjectCount.toString())
                    TermStat(
                        label = "Asistencia",
                        value = lastClosed.attendanceRate?.let { "$it%" }
                    )
                }
            }
        }

        TermCard {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "¿Empezaste un periodo nuevo?",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    // Lo que se hereda se dice antes de pulsar, para que empezar no dé miedo.
                    text = "Traigo tu configuración de ${lastClosed.term.name}: la escala, la " +
                        "nota de aprobado y tus $cutCount cortes. Solo tendrás que revisar las fechas.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
                UniStackButton(
                    text = "Empezar periodo nuevo",
                    onClick = onStartNewTerm,
                    modifier = Modifier.fillMaxWidth()
                )
                UniStackButton(
                    text = "Ver el histórico",
                    onClick = onOpenHistory,
                    variant = UniStackButtonVariant.Outlined,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
