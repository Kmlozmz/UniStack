@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.feature_support.domain.TicketContext
import com.unistack.app.feature_support.domain.TicketKind

/** Lo mínimo que hay que escribir para que el mensaje diga algo. */
private const val MinimumLength = 15

/**
 * El formulario de contacto: uno solo para fallos, ideas y lo demás.
 *
 * Antes había dos puertas en Ayuda —«Reportar un fallo» y «Sugerir algo»— y cada una abría su
 * propia hoja. Obligaban a clasificar antes de contar, y no dejaban sitio a lo que no era
 * ninguna de las dos. Ahora el motivo es un selector dentro del formulario: se puede cambiar
 * después de escribir, y el texto ya redactado no se pierde al cambiarlo.
 *
 * Va en una hoja inferior y no en un diálogo: el teclado la empuja hacia arriba en vez de
 * taparla, y deja sitio para el texto largo que hace falta al describir un fallo.
 *
 * Dos salidas al pie. **El correo se ve pero está apagado**, con la opacidad de un control
 * deshabilitado: existe como destino previsto y todavía no está montado, y esconderlo hasta
 * entonces haría pensar que Telegram es la única vía que va a haber nunca.
 */
@Composable
internal fun FeedbackSheet(
    initialKind: TicketKind,
    ticketContext: TicketContext,
    onDismiss: () -> Unit,
    onSend: (kind: TicketKind, text: String, contact: String?) -> Unit
) {
    // El motivo se recuerda, pero el texto **no** se ata a él: cambiar de Fallo a Sugerencia a
    // media redacción es justo el caso que este formulario existe para permitir.
    var kind by rememberSaveable { mutableStateOf(initialKind) }
    var text by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    val enoughWritten = text.trim().length >= MinimumLength

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Con el teclado abierto la hoja no cabía y había que desplazarla a mano para
                // llegar a los botones. imePadding la levanta, y el scroll cubre las pantallas
                // bajas o el texto en grande.
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Escríbenos",
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cerrar")
                }
            }

            Text(
                text = "Reporta un fallo, sugiere algo o cuéntanos lo que se te ocurra.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            KindChooser(selected = kind, onSelected = { kind = it })

            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(1500) },
                label = { Text("Mensaje") },
                placeholder = { Text("¿Qué pasó, o qué te gustaría ver?") },
                minLines = 4,
                maxLines = 8,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it.take(120) },
                    label = { Text("Contacto (opcional)") },
                    placeholder = { Text("Correo o usuario") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Solo si quieres que podamos responderte.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            WhatWillBeSent(ticketContext = ticketContext, contact = contact.trim())

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "El grupo es público, así que no escribas nada que no quieras que se lea.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = { onSend(kind, text, contact.trim().takeIf { it.isNotEmpty() }) },
                    enabled = enoughWritten,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Telegram")
                }
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) {
                    Icon(Icons.Rounded.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Correo")
                }
            }

            Text(
                text = "El correo todavía no está disponible.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * El selector de motivo.
 *
 * Es el segmentado de Material, no el `UniSegmentedControl` de las pestañas, que va relleno.
 * Aquí el elegido se marca con un check que entra por la izquierda mientras el rótulo se
 * recoloca, y esa animación —la de serie del componente— es justo la que hace evidente que se
 * está *marcando una casilla*, no cambiando de vista. Son dos gestos distintos y por eso son
 * dos controles distintos.
 */
@Composable
private fun KindChooser(
    selected: TicketKind,
    onSelected: (TicketKind) -> Unit
) {
    val kinds = TicketKind.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        kinds.forEachIndexed { index, kind ->
            SegmentedButton(
                selected = kind == selected,
                onClick = { onSelected(kind) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = kinds.size),
                modifier = Modifier.weight(1f)
            ) {
                // Sin ajuste de línea: mientras el check entra, el rótulo dispone de menos
                // ancho del que mide y `Text` lo partiría en dos a mitad de la animación.
                Text(kind.label, maxLines = 1, softWrap = false)
            }
        }
    }
}

/**
 * Lo que se va a mandar, escrito antes de mandarlo.
 *
 * La app añade sola la versión y el teléfono, y decirlo en una línea pequeña no es lo mismo que
 * enseñarlo: aquí se ve el dato exacto, tal cual va a viajar. Empieza plegado porque con el
 * teclado abierto la hoja ya va justa, y quien quiera comprobarlo lo abre.
 */
@Composable
private fun WhatWillBeSent(
    ticketContext: TicketContext,
    contact: String
) {
    var open by rememberSaveable { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, label = "flecha")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { open = !open }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Qué se va a enviar",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Rounded.ExpandMore,
                contentDescription = if (open) "Ocultar" else "Ver",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(20.dp)
                    .rotate(turn)
            )
        }
        AnimatedVisibility(visible = open) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SentRow("Teléfono", ticketContext.device)
                SentRow("Android", "${ticketContext.androidVersion} (SDK ${ticketContext.androidSdk})")
                SentRow("UniStack", ticketContext.appVersion)
                SentRow("Contacto", contact.ifEmpty { "No lo has puesto" })
            }
        }
    }
}

@Composable
private fun SentRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
