package com.unistack.app.core.design.components

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import com.unistack.app.core.design.theme.UniStackColors

/**
 * Avisa antes de abandonar un formulario a medio llenar.
 *
 * Los diez formularios de crear y editar esconden la barra inferior por esto mismo: sin aviso,
 * un toque en cualquier pestaña se llevaba por delante lo escrito sin decir nada, y esconder la
 * barra era el único parche posible. Con el aviso puesto, salir cuesta una confirmación y la
 * pérdida deja de ser silenciosa.
 *
 * Devuelve la función con la que pedir salida: se usa igual en el botón de atrás de la barra
 * superior que en el gesto del sistema, que es lo que hace que las dos puertas se comporten
 * igual. Sin cambios pendientes no pregunta nada y sale directo.
 */
@Composable
fun rememberLeaveGuard(
    hasUnsavedChanges: Boolean,
    onLeave: () -> Unit,
    title: String = "¿Salir sin guardar?",
    message: String = "Lo que escribiste se va a perder."
): () -> Unit {
    var asking by remember { mutableStateOf(false) }
    val currentHasChanges by rememberUpdatedState(hasUnsavedChanges)
    val currentOnLeave by rememberUpdatedState(onLeave)

    val requestLeave: () -> Unit = remember {
        {
            if (currentHasChanges) asking = true else currentOnLeave()
        }
    }

    // El gesto de atrás pasa por la misma puerta. Mientras el diálogo está abierto lo captura
    // él, para que un segundo atrás lo cierre en vez de salir por debajo.
    BackHandler(enabled = true) {
        if (asking) asking = false else requestLeave()
    }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            title = { Text(title) },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        asking = false
                        currentOnLeave()
                    }
                ) {
                    Text("Salir", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { asking = false }) { Text("Seguir aquí") }
            },
            containerColor = UniStackColors.Background
        )
    }

    return requestLeave
}
