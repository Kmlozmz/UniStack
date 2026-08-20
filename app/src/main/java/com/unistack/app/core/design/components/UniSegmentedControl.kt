package com.unistack.app.core.design.components

import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class UniSegmentedOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector
)

/**
 * Elegir entre dos o tres vistas de lo mismo: Materias o Tareas, Horario o Calendario.
 *
 * Lo dibuja [SingleChoiceSegmentedButtonRow], que es el control de Material para exactamente
 * esto: un grupo conectado donde solo una opción está activa, con su marca de selección y las
 * esquinas redondeadas hacia fuera en los extremos.
 *
 * **Antes estuvo hecho con `ButtonGroup` y no se dibujaba nada.** `ButtonGroup` es para una
 * fila de acciones que puede desbordarse a un menú, no para elegir entre vistas; aquí se
 * quedaba sin medir y la fila desaparecía de la pantalla sin dar error. El control de una sola
 * elección es el que corresponde, y además dice en su propio nombre lo que hace.
 */
@Composable
fun <T> UniSegmentedControl(
    selected: T,
    options: List<UniSegmentedOption<T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = selected == option.value,
                onClick = { onSelected(option.value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                icon = { Icon(option.icon, contentDescription = null) },
                label = { Text(option.label) }
            )
        }
    }
}
