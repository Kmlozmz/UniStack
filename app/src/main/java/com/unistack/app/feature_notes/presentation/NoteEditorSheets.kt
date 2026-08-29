@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.unistack.app.feature_notes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatClear
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.FormatListNumbered
import androidx.compose.material.icons.rounded.FormatStrikethrough
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_notes.domain.NoteAction

/** Lo que se puede meter en una nota, desde el «+» de abajo. */
enum class NoteInsert { CAMARA, GALERIA, GRABACION, CASILLAS, TABLA, ARCHIVO }

/**
 * El «+» del pie: lo que se le puede añadir a la nota.
 *
 * Es la lista de Keep con lo que esta app puede hacer de verdad: no hay dibujo porque no hay
 * lienzo, y sí hay tabla y archivo, que aquí sirven para un taller y un PDF del aula virtual.
 */
@Composable
fun NoteInsertSheet(onPick: (NoteInsert) -> Unit, onDismiss: () -> Unit) {
    val opciones = listOf(
        Triple(NoteInsert.CAMARA, "Hacer foto", Icons.Rounded.PhotoCamera),
        Triple(NoteInsert.GALERIA, "Añadir imagen", Icons.Rounded.Image),
        Triple(NoteInsert.GRABACION, "Grabación", Icons.Rounded.Mic),
        Triple(NoteInsert.CASILLAS, "Casillas", Icons.Rounded.CheckBox),
        Triple(NoteInsert.TABLA, "Tabla", Icons.Rounded.TableChart),
        Triple(NoteInsert.ARCHIVO, "Archivo", Icons.Rounded.AttachFile)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 18.dp)
        ) {
            opciones.forEach { (tipo, label, icono) ->
                SheetRow(icono, label) { onPick(tipo) }
            }
        }
    }
}

/**
 * El color de la nota.
 *
 * Keep tiene además fondos ilustrados. Aquí solo hay color: las ilustraciones son dibujo, y uno
 * inventado a medias se nota más que no tenerlo.
 */
@Composable
fun NoteColorSheet(
    selected: Int?,
    onPick: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Color",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorDot(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    selected = selected == null,
                    sinColor = true,
                    onClick = { onPick(null) }
                )
                NoteColors.palette.forEach { color ->
                    ColorDot(
                        color = color,
                        selected = selected == color.toArgb(),
                        sinColor = false,
                        onClick = { onPick(color.toArgb()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorDot(
    color: Color,
    selected: Boolean,
    sinColor: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            selected -> Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = NoteColors.contentOn(color, MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.size(20.dp)
            )
            sinColor -> Icon(
                Icons.Rounded.Close,
                contentDescription = "Sin color",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

/**
 * La barra de formato, la de la «A» subrayada.
 *
 * Es lo que sustituye a tener que saber Markdown: los botones escriben las marcas y la app las
 * esconde. Fue lo que él pidió —«del markdown que se encargue la app»—, y por eso ya no hay que
 * elegir tipo de nota al crearla: todas son la misma cosa.
 */
@Composable
fun NoteFormatToolbar(
    onAction: (NoteAction) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        TextButtonSmall("H1", NoteAction.TITULO1, onAction)
        TextButtonSmall("H2", NoteAction.TITULO2, onAction)
        TextButtonSmall("Aa", NoteAction.NORMAL, onAction)
        Divider()
        IconButtonSmall(Icons.Rounded.FormatBold, NoteAction.NEGRITA, onAction)
        IconButtonSmall(Icons.Rounded.FormatItalic, NoteAction.CURSIVA, onAction)
        IconButtonSmall(Icons.Rounded.FormatStrikethrough, NoteAction.TACHADO, onAction)
        Divider()
        IconButtonSmall(Icons.Rounded.FormatListBulleted, NoteAction.VINETA, onAction)
        IconButtonSmall(Icons.Rounded.FormatListNumbered, NoteAction.NUMERADA, onAction)
        IconButtonSmall(Icons.Rounded.CheckBox, NoteAction.CASILLA, onAction)
        Divider()
        IconButtonSmall(Icons.Rounded.FormatClear, NoteAction.LIMPIAR, onAction)
        Surface(
            onClick = onClose,
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Cerrar el formato",
                modifier = Modifier.padding(9.dp).size(19.dp)
            )
        }
    }
}

@Composable
private fun Divider() {
    Box(
        Modifier
            .padding(horizontal = 5.dp)
            .size(width = 1.dp, height = 20.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun TextButtonSmall(label: String, action: NoteAction, onAction: (NoteAction) -> Unit) {
    Surface(
        onClick = { onAction(action) },
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun IconButtonSmall(icon: ImageVector, action: NoteAction, onAction: (NoteAction) -> Unit) {
    Surface(
        onClick = { onAction(action) },
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Icon(
            icon,
            contentDescription = action.label,
            modifier = Modifier.padding(9.dp).size(19.dp)
        )
    }
}

@Composable
private fun SheetRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(21.dp))
            Spacer(Modifier.width(16.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
