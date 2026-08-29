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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

/**
 * Una lista de verdad, no un texto con casillas.
 *
 * Cada renglón es su propio campo: se escribe, se marca tocando el cuadro y se cambia de sitio
 * arrastrando el asa de la izquierda. Dentro de un solo campo de texto había que colocar el
 * cursor a mano en cada línea y no había forma de reordenar nada.
 *
 * Las marcadas se recogen abajo, plegadas, como en Keep. Una lista de doce con nueve tachadas
 * gasta la pantalla entera en decir lo que ya está hecho.
 *
 * Guarda lo mismo de siempre: `- [ ] algo`. Por eso la tarjeta, el buscador y el respaldo no se
 * enteran de que esto existe.
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
    var focoEn by remember { mutableStateOf<Int?>(null) }
    val focos = remember { mutableMapOf<Int, FocusRequester>() }

    val sinMarcar = items.withIndex().filter { !it.value.checked }
    val marcadas = items.withIndex().filter { it.value.checked }

    LaunchedEffect(focoEn) {
        val destino = focoEn ?: return@LaunchedEffect
        runCatching { focos[destino]?.requestFocus() }
        focoEn = null
    }

    Column(modifier = modifier.fillMaxWidth()) {
        sinMarcar.forEach { (indice, item) ->
            ChecklistRow(
                item = item,
                indice = indice,
                total = items.size,
                reorder = reorder,
                texto = texto,
                suave = suave,
                foco = focos.getOrPut(indice) { FocusRequester() },
                onText = { nuevo ->
                    onChange(items.toMutableList().also { it[indice] = item.copy(text = nuevo) })
                },
                onToggle = {
                    onChange(
                        items.toMutableList().also { it[indice] = item.copy(checked = !item.checked) }
                    )
                },
                onRemove = {
                    onChange(items.toMutableList().also { it.removeAt(indice) })
                    focoEn = (indice - 1).coerceAtLeast(0)
                },
                onEnter = {
                    val nuevos = items.toMutableList()
                    nuevos.add(indice + 1, ChecklistItem("", false))
                    onChange(nuevos)
                    focoEn = indice + 1
                },
                onMove = { desde, hasta ->
                    onChange(com.unistack.app.feature_notes.domain.NoteChecklist.move(items, desde, hasta))
                }
            )
        }

        // «Elemento de lista»: la fila que crea la siguiente sin tener que buscar un botón.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    onChange(items + ChecklistItem("", false))
                    focoEn = items.size
                }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(26.dp))
            Icon(
                Icons.Rounded.Add,
                contentDescription = null,
                tint = suave,
                modifier = Modifier.size(19.dp)
            )
            Spacer(Modifier.width(13.dp))
            Text("Elemento", color = suave, style = MaterialTheme.typography.bodyLarge)
        }

        if (marcadas.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandidas = !expandidas }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (expandidas) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = suave,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(19.dp))
                Text(
                    marcadas.size.toString() +
                        if (marcadas.size == 1) " marcada" else " marcadas",
                    color = suave,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (expandidas) {
                marcadas.forEach { (indice, item) ->
                    ChecklistRow(
                        item = item,
                        indice = indice,
                        total = items.size,
                        reorder = reorder,
                        texto = texto,
                        suave = suave,
                        foco = focos.getOrPut(indice) { FocusRequester() },
                        conAsa = false,
                        onText = { nuevo ->
                            onChange(
                                items.toMutableList().also { it[indice] = item.copy(text = nuevo) }
                            )
                        },
                        onToggle = {
                            onChange(
                                items.toMutableList().also {
                                    it[indice] = item.copy(checked = !item.checked)
                                }
                            )
                        },
                        onRemove = {
                            onChange(items.toMutableList().also { it.removeAt(indice) })
                        },
                        onEnter = {},
                        onMove = { _, _ -> }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    indice: Int,
    total: Int,
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
            .uniReorderableItem(reorder, "item-$indice")
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (conAsa) {
            Icon(
                Icons.Rounded.DragIndicator,
                contentDescription = "Mover de sitio",
                tint = suave.copy(alpha = 0.55f),
                modifier = Modifier
                    .size(20.dp)
                    .uniReorderHandle(
                        state = reorder,
                        key = "item-$indice",
                        index = { indice },
                        itemCount = { total },
                        onMove = onMove
                    )
            )
            Spacer(Modifier.width(6.dp))
        } else {
            Spacer(Modifier.width(26.dp))
        }

        ChecklistBox(checked = item.checked, tint = texto, onClick = onToggle)
        Spacer(Modifier.width(13.dp))

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
            contentDescription = "Quitar",
            tint = suave.copy(alpha = 0.6f),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onRemove)
                .padding(5.dp)
                .size(16.dp)
        )
    }
}

/** El cuadro de una fila: vacío con borde, y relleno con la marca cuando está hecho. */
@Composable
private fun ChecklistBox(checked: Boolean, tint: Color, onClick: () -> Unit) {
    val forma = RoundedCornerShape(4.dp)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick)
            .padding(3.dp)
            .size(19.dp)
            .clip(forma)
            .then(
                if (checked) {
                    Modifier.background(tint.copy(alpha = 0.85f))
                } else {
                    Modifier.border(1.6.dp, tint.copy(alpha = 0.55f), forma)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
