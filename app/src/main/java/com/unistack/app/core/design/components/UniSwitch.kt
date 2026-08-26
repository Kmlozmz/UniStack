package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.feature_user.domain.SwitchIconStyle

/**
 * El interruptor de la app, con el icono dentro del pulgar.
 *
 * **Es el único sitio que llama a [Switch] de Material.** Antes cada pantalla lo llamaba pelado
 * —Ajustes por su cuenta, y sueltos en Tareas, Materias, Horario y la calculadora—, así que no
 * había forma de cambiar el aspecto de un interruptor sin ir pantalla por pantalla.
 *
 * El icono es lo que permite leer el estado **sin depender solo del color**. Encendido y apagado
 * se distinguían por un violeta y un gris; quien no separa bien esos dos tonos tenía que fiarse
 * de la posición del pulgar, que es una diferencia de doce píxeles. Un visto y un aspa no se
 * confunden.
 *
 * Cuál de las tres formas se usa sale de Apariencia ([SwitchIconStyle]), no de quien lo llama:
 * un ajuste que cambia según la pantalla en la que estés no es un ajuste.
 *
 * El icono no lleva `contentDescription`: el propio [Switch] ya anuncia su estado por el rol de
 * interruptor, y describirlo otra vez lo diría dos veces.
 */
@Composable
fun UniSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val showIcon = when (LocalAppearancePreferences.current.switchIconStyle) {
        SwitchIconStyle.BOTH -> true
        SwitchIconStyle.CHECKED_ONLY -> checked
        SwitchIconStyle.NONE -> false
    }

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        thumbContent = if (showIcon) {
            {
                Icon(
                    imageVector = if (checked) Icons.Rounded.Check else Icons.Rounded.Close,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        } else {
            null
        }
    )
}
