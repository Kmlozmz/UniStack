package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * La cabecera de una sección: Académico, Horario, Gastos.
 *
 * **Una sola, porque eran tres.** Estaban escritas por separado en cada pantalla y habían
 * derivado: distinto espacio entre el título y el apoyo, distinto relleno alrededor, y en
 * Académico el título llegó a ir cuatro puntos más grande que en las otras dos. Cambiar de
 * pestaña movía el texto de sitio, que es lo que se nota aunque no se sepa decir por qué.
 *
 * El apoyo **siempre está**, aunque la pantalla tenga buscador. Antes el campo de búsqueda
 * sustituía a la línea de apoyo, y como mide el triple, abrir la lupa empujaba todo lo de
 * abajo; cerrarla lo devolvía de un salto. Ahora el buscador va debajo y la cabecera mide
 * siempre lo mismo.
 *
 * @param color y [supportColor] para las secciónes con identidad cromática propia, como el
 *   rojo de Gastos. Sin ellos, los del tema.
 */
@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    color: Color? = null,
    supportColor: Color? = null,
    /** Lo que va a la derecha del título: la lupa, un botón de recargar. */
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                color = color ?: MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.weight(1f)
            )
            action?.invoke()
        }
        Text(
            text = subtitle,
            color = supportColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
