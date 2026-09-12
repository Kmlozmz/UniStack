@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_notes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniReorderState
import com.unistack.app.core.design.components.rememberUniReorderState
import com.unistack.app.core.design.components.uniReorderHandle
import com.unistack.app.core.design.components.uniReorderableItem
import com.unistack.app.feature_notes.domain.ChecklistItem
import com.unistack.app.feature_notes.domain.NoteChecklist

/**
 * Una lista de verdad, no un texto con casillas.
 *
 * Cada renglón es su propio campo: se escribe, se marca tocando el cuadro y se cambia de sitio
 * arrastrando el asa de la izquierda. Dentro de un solo campo de texto había que colocar el
 * cursor a mano en cada línea y no había forma de reordenar nada.
 *
 * **La lista manda aquí y no en el texto.** Se probó al revés —leer los elementos del cuerpo en
 * cada pintada— y fallaba en las dos cosas que más se hacen: el elemento recién añadido está
 * vacío, así que al releer el cuerpo desaparecía y la nota volvía a ser un texto normal; y las
 * filas se identificaban por su posición, que cambia en cuanto se mueve una, de modo que el
 * arrastre soltaba la fila y agarraba la de al lado. Cada elemento lleva ahora su identidad.
 */
@Composable
fun NoteChecklistEditor(
    items: List<ChecklistItem>,
    onChange: (List<ChecklistItem>) -> Unit,
    texto: Color,
    suave: Color,
    modifier: Modifier = Modifier
) {
    val reorder: UniReorderState = rememberUniReorderState()
    var expandidas by remember { mutableStateOf(false) }
    var focoEn by remember { mutableStateOf<String?>(null) }
    val focos = remember { mutableMapOf<String, FocusRequester>() }

    val sinMarcar = items.filter { !it.checked }
    val marcadas = items.filter { it.checked }

    LaunchedEffect(focoEn) {
        val destino = focoEn ?: return@LaunchedEffect
        runCatching { focos[destino]?.requestFocus() }
        focoEn = null
    }

    fun cambiar(item: ChecklistItem, nuevo: ChecklistItem) {
        onChange(items.map { if (it.id == item.id) nuevo else it })
    }

    fun moverEnGrupo(grupo: List<ChecklistItem>, desde: Int, hasta: Int) {
        val fromItem = grupo.getOrNull(desde) ?: return
        val toItem = grupo.getOrNull(hasta) ?: return
        val from = items.indexOfFirst { it.id == fromItem.id }
        val to = items.indexOfFirst { it.id == toItem.id }
        if (from >= 0 && to >= 0) onChange(NoteChecklist.move(items, from, to))
    }

    Column(modifier = modifier.fillMaxWidth()) {
        sinMarcar.forEachIndexed { visibleIndex, item ->
            // La lista se pinta por grupos (pendientes / hechas), así que el índice que recibe
            // el asa también debe ser el del grupo visible. Usar el índice de `items` hacía que
            // el primer salto funcionara y el siguiente apuntara a una fila que no estaba en
            // pantalla cuando había elementos marcados.
            key(item.id) {
                ChecklistRow(
                    item = item,
                    indice = { visibleIndex },
                    total = { sinMarcar.size },
                    reorder = reorder,
                    texto = texto,
                    suave = suave,
                    foco = focos.getOrPut(item.id) { FocusRequester() },
                    onText = { nuevo -> cambiar(item, item.copy(text = nuevo)) },
                    onToggle = { cambiar(item, item.copy(checked = !item.checked)) },
                    onRemove = { onChange(items.filterNot { it.id == item.id }) },
                    onEnter = {
                        val nuevo = ChecklistItem("", false)
                        val donde = items.indexOfFirst { it.id == item.id } + 1
                        onChange(items.toMutableList().also { it.add(donde, nuevo) })
                        focoEn = nuevo.id
                    },
                    onMove = { desde, hasta -> moverEnGrupo(sinMarcar, desde, hasta) }
                )
            }
        }

        // «Elemento»: la fila que crea la siguiente sin tener que buscar un botón.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    val nuevo = ChecklistItem("", false)
                    onChange(items + nuevo)
                    focoEn = nuevo.id
                }
                .padding(vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(30.dp))
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                tint = suave,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(16.dp))
            Text(stringResource(R.string.notes_checklist_item), color = suave, style = MaterialTheme.typography.bodyLarge)
        }

        if (marcadas.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { expandidas = !expandidas }
                    .padding(vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (expandidas) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = suave,
                    modifier = Modifier.size(23.dp)
                )
                Spacer(Modifier.width(25.dp))
                val marcadasLabel = "${marcadas.size} " + (if (marcadas.size == 1) stringResource(R.string.notes_checklist_marked_singular) else stringResource(R.string.notes_checklist_marked_plural))
                Text(
                    marcadasLabel,
                    color = suave,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            if (expandidas) {
                marcadas.forEachIndexed { visibleIndex, item ->
                    key(item.id) {
                        ChecklistRow(
                            item = item,
                            indice = { visibleIndex },
                            total = { marcadas.size },
                            reorder = reorder,
                            texto = texto,
                            suave = suave,
                            foco = focos.getOrPut(item.id) { FocusRequester() },
                            onText = { nuevo -> cambiar(item, item.copy(text = nuevo)) },
                            onToggle = { cambiar(item, item.copy(checked = !item.checked)) },
                            onRemove = { onChange(items.filterNot { it.id == item.id }) },
                            onEnter = {},
                            onMove = { desde, hasta -> moverEnGrupo(marcadas, desde, hasta) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    indice: () -> Int,
    total: () -> Int,
    reorder: UniReorderState,
    texto: Color,
    suave: Color,
    foco: FocusRequester,
    onText: (String) -> Unit,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
    onEnter: () -> Unit,
    onMove: (Int, Int) -> Unit,
    conAsa: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .uniReorderableItem(reorder, item.id)
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conAsa) {
            Icon(
                Icons.Rounded.DragIndicator,
                contentDescription = stringResource(R.string.notes_checklist_move),
                tint = suave.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(24.dp)
                    .uniReorderHandle(
                        state = reorder,
                        key = item.id,
                        index = indice,
                        itemCount = total,
                        onMove = onMove
                    )
            )
            Spacer(Modifier.width(6.dp))
        } else {
            Spacer(Modifier.width(30.dp))
        }

        ChecklistBox(checked = item.checked, tint = texto, onClick = onToggle)
        Spacer(Modifier.width(14.dp))

        BasicTextField(
            value = item.text,
            onValueChange = onText,
            singleLine = true,
            modifier = Modifier.weight(1f).focusRequester(foco),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (item.checked) suave else texto,
                textDecoration = if (item.checked) TextDecoration.LineThrough else null
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { onEnter() }),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
        )

        Icon(
            Icons.Rounded.Close,
            contentDescription = stringResource(R.string.notes_checklist_remove),
            tint = suave.copy(alpha = 0.6f),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onRemove)
                .padding(6.dp)
                .size(18.dp)
        )
    }
}

/** El cuadro de una fila: vacío con borde, y relleno con la marca cuando está hecho. */
@Composable
private fun ChecklistBox(checked: Boolean, tint: Color, onClick: () -> Unit) {
    val forma = RoundedCornerShape(5.dp)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
            .size(22.dp)
            .clip(forma)
            .then(
                if (checked) {
                    Modifier.background(tint.copy(alpha = 0.85f))
                } else {
                    Modifier.border(1.8.dp, tint.copy(alpha = 0.55f), forma)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
