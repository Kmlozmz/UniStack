package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

/**
 * El campo de búsqueda de la app.
 *
 * **Uno solo, porque había dos y no se parecían.** El de Tareas medía 56dp de alto, teñía la
 * lupa y ponía el texto de ejemplo en seminegrita; el de Materias no fijaba altura, dejaba la
 * lupa del color por defecto y el texto normal. Son la misma cosa en dos pestañas de la misma
 * pantalla, y se notaba al pasar de una a otra.
 *
 * Lleva la tecla de buscar en el teclado, y una cruz para vaciar el texto que solo aparece
 * cuando hay algo escrito: una cruz permanente sobre un campo vacío invita a pulsarla para
 * cerrar la búsqueda, que no es lo que hace.
 *
 * Es **docked**: un campo dentro de la pantalla, no una capa que la tape. La búsqueda a
 * pantalla completa de Material tiene sentido cuando hay historial y sugerencias detrás; aquí
 * lo que hay es filtrar una lista que ya está a la vista, y taparla para filtrarla es perder
 * de vista justo lo que estás filtrando.
 */
@Composable
fun UniSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearchDone: () -> Unit = {}
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchDone() },
            onDone = { onSearchDone() }
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.common_clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            null
        },
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
