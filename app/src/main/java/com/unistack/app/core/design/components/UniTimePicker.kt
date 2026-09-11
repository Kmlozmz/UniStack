@file:OptIn(ExperimentalMaterial3Api::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

/**
 * Poner una hora, escribiéndola.
 *
 * **Abre por teclado y no por reloj, y esa es la decisión.** El reloj de Material está pensado
 * para elegir una hora aproximada arrastrando la aguja; aquí la hora casi siempre se sabe ya
 * —«la entrega es a las 23:59», «la clase empieza a las 7:00»— y con el reloj hay que arrastrar
 * dos veces para poner algo que se teclea en cuatro pulsaciones. Los minutos exactos son
 * especialmente malos con la aguja: acertar el 59 sin pasarse cuesta más que escribirlo.
 *
 * El reloj sigue estando, a un toque del botón de abajo a la izquierda, para quien no tenga la
 * hora pensada y prefiera buscarla.
 *
 * Unifica dos diálogos que preguntaban lo mismo de formas distintas: el de la hora límite de una
 * tarea y el de la hora de una clase.
 */
@Composable
fun UniTimePickerDialog(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
    title: String = "Hora",
    /** Un tercer botón, para las pantallas donde la hora se puede quitar. */
    extraAction: (@Composable () -> Unit)? = null
) {
    val initial = selectedTime ?: LocalTime.now().withSecond(0).withNano(0)
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true
    )
    var typing by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (typing) TimeInput(state = state) else TimePicker(state = state)
                extraAction?.invoke()
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onTimeSelected(LocalTime.of(state.hour, state.minute))
                onDismiss()
            }) { Text(stringResource(R.string.action_accept)) }
        },
        dismissButton = {
            // El cambio de modo va aquí, en el hueco que Material reserva a la izquierda de
            // los botones: es donde lo pone el propio sistema de diseño, y así no compite por
            // sitio con aceptar y cancelar.
            TextButton(onClick = { typing = !typing }) {
                Icon(
                    imageVector = if (typing) Icons.Rounded.Schedule else Icons.Rounded.Keyboard,
                    contentDescription = null
                )
                Text(
                    text = " " + if (typing) stringResource(R.string.time_picker_clock) else stringResource(R.string.time_picker_keyboard),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
}
