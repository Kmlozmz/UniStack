package com.unistack.app.core.design.components

import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

data class UniSegmentedOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector
)

/**
 * Elegir entre dos o tres vistas de lo mismo: Materias o Tareas, Semana o Mes.
 *
 * Lo dibuja [ButtonGroup], el grupo conectado de Material 3 Expressive. La diferencia con lo
 * que había —una fila de cajas dentro de una tarjeta, con el seleccionado pintado de otro
 * color— es que aquí el seleccionado **cambia de forma y de anchura**: se ensancha y sus
 * esquinas se redondean, y los vecinos se estrechan para dejarle sitio. Se nota qué está
 * elegido sin depender de distinguir dos tonos, que es justo lo que falla con poca luz o con
 * daltonismo.
 *
 * El movimiento de ese ensanchado sale del `MotionScheme` del tema, no de una duración escrita
 * aquí.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> UniSegmentedControl(
    selected: T,
    options: List<UniSegmentedOption<T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    ButtonGroup(modifier = modifier) {
        options.forEach { option ->
            toggleableItem(
                checked = selected == option.value,
                label = option.label,
                onCheckedChange = { onSelected(option.value) },
                icon = { Icon(option.icon, contentDescription = null) },
                weight = 1f
            )
        }
    }
}
