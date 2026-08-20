package com.unistack.app.core.design.components

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.material3.ToggleFloatingActionButtonDefaults

/**
 * El botón de crear, con sus opciones.
 *
 * Lo dibuja [FloatingActionButtonMenu], el menú de acciones de Material 3 Expressive. Antes
 * eran trescientas sesenta líneas que reimplementaban lo mismo: el escalado del botón, el
 * escalonado de la entrada de cada opción, el velo de fondo, el gesto de atrás y el orden de
 * lectura para accesibilidad. Todo eso lo trae el componente, y su movimiento sale del
 * `MotionScheme` del tema en lugar de las duraciones que había escritas a mano.
 *
 * El botón cambia de forma al abrirse —de cuadrado redondeado a círculo— además de girar el
 * icono. Es la señal que antes daba solo el giro.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UniStackFabMenu(
    onAddGradeClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier,
    showAddGrade: Boolean = true,
    showAddTask: Boolean = true,
    showAddExpense: Boolean = true,
    showAddSubject: Boolean = false
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    // Abierto, atrás cierra el menú en vez de salir de la pantalla.
    BackHandler(enabled = expanded) { expanded = false }

    val entries = buildList {
        if (showAddGrade) add(Triple("Nota", Icons.Rounded.EditNote, onAddGradeClick))
        if (showAddTask) add(Triple("Tarea", Icons.Rounded.TaskAlt, onAddTaskClick))
        if (showAddExpense) add(Triple("Gasto", Icons.Rounded.AccountBalanceWallet, onAddExpenseClick))
        if (showAddSubject) add(Triple("Materia", Icons.Rounded.MenuBook, onAddSubjectClick))
    }

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it },
                // El tamaño mediano de Material. El pequeño es el de una acción secundaria, y
                // este botón es la única forma de crear algo desde Inicio: se veía menor de lo
                // que pesa.
                containerSize = ToggleFloatingActionButtonDefaults.containerSizeMedium(),
                // Con el menú abierto, el lector de pantalla debe llegar antes al botón que
                // a las opciones: es lo que las cierra.
                modifier = Modifier.semantics { traversalIndex = -1f }
            ) {
                val icon: ImageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.Add
                Icon(icon, contentDescription = if (expanded) "Cerrar menú" else "Crear")
            }
        }
    ) {
        entries.forEach { (label, icon, action) ->
            FloatingActionButtonMenuItem(
                onClick = {
                    expanded = false
                    action()
                },
                icon = { Icon(icon, contentDescription = null) },
                text = { Text(label) }
            )
        }
    }
}
