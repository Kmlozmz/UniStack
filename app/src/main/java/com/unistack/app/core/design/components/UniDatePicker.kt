@file:OptIn(ExperimentalMaterial3Api::class)

package com.unistack.app.core.design.components

import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Convierte a la fecha local que el usuario ve.
 *
 * El selector de Material trabaja en milisegundos UTC. Pasarlos por la zona horaria del
 * teléfono es lo que hacía que elegir el día 1 devolviera el 31 del mes anterior en cualquier
 * huso al oeste de Greenwich —Colombia, sin ir más lejos—, así que la cuenta se hace siempre
 * en UTC y solo al final se toma la parte de fecha.
 */
private fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

private fun LocalDate.toUtcMillis(): Long =
    this.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/**
 * Elegir un día, con el calendario de Material.
 *
 * **Sustituye a dos calendarios dibujados a mano** —uno en Gastos y otro en Tareas— que eran
 * una rejilla de siete columnas construida con cajas. Funcionaban, pero cada uno tenía su
 * propia idea de cómo se ve un día seleccionado, ninguno dejaba escribir la fecha en vez de
 * buscarla, y para saltar de agosto a diciembre había que pulsar la flecha cuatro veces.
 *
 * El de Material trae el salto de año, la entrada por teclado y la accesibilidad hecha. Lo que
 * pone la app es el color: sale del esquema, así que sigue el acento que cada quien elija.
 */
@Composable
fun UniDatePickerDialog(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = (selectedDate ?: LocalDate.now()).toUtcMillis()
    )

    /*
     * Ventana propia y a pantalla completa, con la tarjeta animando su alto por dentro.
     *
     * Este componente ha tenido dos problemas seguidos, y los dos venían del mismo sitio.
     *
     * Con `DatePickerDialog`, cambiar entre calendario y teclado cambiaba el alto de la
     * **ventana**, y una ventana de Android que se redimensiona no lo hace de golpe: se
     * repinta por tramos, que es ese efecto de verla dibujarse franja a franja.
     *
     * Fijar una altura mínima lo quitaba, pero dejaba el modo de teclado —que ocupa la
     * cuarta parte— dentro de una caja del alto del calendario, con un vacío enorme debajo.
     *
     * Así que la ventana pasa a ocupar la pantalla entera y no cambia nunca de tamaño; lo que
     * crece y encoge es la tarjeta de dentro, y eso lo anima Compose con `animateContentSize`,
     * que sí es fluido porque no hay ninguna ventana que reajustar.
     */
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.animateContentSize(
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
                ),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 6.dp
            ) {
                Column {
                    // Sin título propio: le pasaba uno por el hueco `title` y se dibujaba
                    // fuera de la hoja. El titular ya dice qué fecha hay elegida.
                    DatePicker(
                        state = state,
                        showModeToggle = true
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancelar") }
                        TextButton(
                            onClick = {
                                state.selectedDateMillis?.let { onDateSelected(it.toLocalDate()) }
                                onDismiss()
                            },
                            // Sin fecha marcada no hay nada que aceptar, y un botón que no
                            // hace nada se pulsa igual y deja pensando que se ha colgado.
                            enabled = state.selectedDateMillis != null
                        ) { Text("Aceptar") }
                    }
                }
            }
        }
    }
}

/**
 * Elegir dos días: el desde y el hasta.
 *
 * Lo pide el filtro de Gastos. «Esta semana» y «Este mes» cubren la pregunta de todos los días,
 * pero no la del corte de un semestre o la de un viaje concreto, y para eso no había forma de
 * preguntar.
 */
@Composable
fun UniDateRangePickerDialog(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onRangeSelected: (start: LocalDate, end: LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate?.toUtcMillis(),
        initialSelectedEndDateMillis = endDate?.toUtcMillis()
    )

    /*
     * A pantalla completa, no en un diálogo.
     *
     * `DateRangePicker` no enseña un mes: enseña todos, uno debajo de otro, para poder arrastrar
     * de agosto a diciembre sin cambiar de vista. Metido en un `DatePickerDialog` eso no cabe:
     * la lista se comía los botones, el titular quedaba cortado por arriba y «Aceptar» se salía
     * por abajo. Material lo dibuja a pantalla completa por este motivo, y aquí se hace igual.
     */
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // El titular se metía debajo del reloj y la señal: a pantalla completa no
                    // hay diálogo que aparte el contenido de la barra de estado, hay que
                    // apartarlo aquí.
                    .statusBarsPadding()
            ) {
                DateRangePicker(
                    state = state,
                    modifier = Modifier.weight(1f),
                    showModeToggle = true
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    TextButton(
                        onClick = {
                            val from = state.selectedStartDateMillis
                            val to = state.selectedEndDateMillis
                            if (from != null && to != null) {
                                onRangeSelected(from.toLocalDate(), to.toLocalDate())
                            }
                            onDismiss()
                        },
                        // Con un solo extremo marcado el periodo no existe todavía.
                        enabled = state.selectedStartDateMillis != null &&
                            state.selectedEndDateMillis != null
                    ) { Text("Aceptar") }
                }
            }
        }
    }
}
