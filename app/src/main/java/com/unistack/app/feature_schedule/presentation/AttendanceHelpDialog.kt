package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus

/**
 * Cómo se lee el historial.
 *
 * La pantalla usa color como dato —verde es que fuiste, rojo que no— y eso hay que decirlo una
 * vez. La leyenda de la tira lo cubre a medias, pero solo enseña los estados que ya han
 * ocurrido, así que quien empieza no ve el rojo hasta que falta por primera vez.
 *
 * Se abre a mano desde la interrogación y no sale sola: nadie quiere un tutorial cada vez que
 * mira sus faltas.
 */
@Composable
internal fun AttendanceHelpDialog(
    hasLimit: Boolean,
    hasWeekNumbers: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cómo leer esto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Parrafo(
                    if (hasLimit) {
                        "Arriba, cuántas faltas te quedan de las que admite la materia. " +
                            "Ese tope lo pones tú, y sale de tu reglamento."
                    } else {
                        "Arriba, tu porcentaje de asistencia y sobre cuántas clases se " +
                            "calcula. Si pones un tope de faltas, pasa a decirte cuántas te " +
                            "quedan, que suele ser lo que importa."
                    }
                )

                Parrafo(
                    "Debajo, una fila por clase dada. El color dice cómo quedó:"
                )

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Fila(ClassAttendanceStatus.ATTENDED, "Fuiste.")
                    Fila(ClassAttendanceStatus.ABSENT, "No fuiste, y cuenta para el tope.")
                    Fila(ClassAttendanceStatus.CANCELLED, "No la hubo. No cuenta ni a favor ni en contra.")
                    Fila(ClassAttendanceStatus.PENDING, "Ya pasó y no la marcaste.")
                }

                if (hasWeekNumbers) {
                    Parrafo(
                        "Las semanas se cuentan desde que empezó tu periodo, así que " +
                            "«Semana 8» es la octava del semestre."
                    )
                }

                Parrafo(
                    "Lo que aún no ha pasado sale arriba, en «Próxima», y no lleva estado: " +
                        "no hay nada que decir de una clase que no ha ocurrido."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun Parrafo(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
}

@Composable
private fun Fila(status: ClassAttendanceStatus, que: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(
                    status.attendanceColor() ?: MaterialTheme.colorScheme.surfaceContainerHighest
                )
        )
        Text(
            text = status.legendName().replaceFirstChar { it.uppercase() } + " — $que",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.5.sp,
            lineHeight = 16.sp
        )
    }
}
