@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_notes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteText
import com.unistack.app.feature_notes.domain.QuickNote

/**
 * La etiqueta de materia de una nota: un punto de su color y el nombre.
 *
 * El punto no es adorno. En una lista de veinte notas el nombre se lee al detenerse en una; el
 * color se ve de un vistazo, y es lo que permite saber de quien es cada nota sin leer ninguna.
 */
@Composable
fun NoteSubjectChip(
    subject: Subject,
    modifier: Modifier = Modifier
) {
    val accent = subjectAccent(subject)
    Surface(
        shape = CircleShape,
        color = accent.copy(alpha = 0.14f),
        contentColor = accent,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
            Text(
                text = subject.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Una nota en la lista.
 *
 * No hay campo de titulo: el titulo es la primera linea, en negrita, y debajo va lo que siga.
 * Con [compact] —el mosaico— el texto se recorta a cuatro lineas para que ninguna nota larga se
 * coma la pantalla; en cuaderno se deja respirar hasta ocho.
 */
@Composable
fun NoteCard(
    note: QuickNote,
    subject: Subject?,
    timeLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    attachments: List<NoteAttachment> = emptyList(),
    pathFor: (NoteAttachment) -> String = { "" }
) {
    val accent = subject?.let { subjectAccent(it) }
    /*
     * La foto manda, y por eso va arriba del todo.
     *
     * Es lo que sostiene el mosaico: una nota de clase suele empezar por una foto de la
     * pizarra, y en dos columnas es la foto la que dice de que va cada tarjeta antes de leer
     * una sola palabra. Solo la primera; con dos, la tarjeta deja de ser una tarjeta.
     */
    val portada = remember(attachments) {
        attachments.firstOrNull { it.kind == AttachmentKind.IMAGE }
    }
    /*
     * En la tarjeta el texto va sin marcas.
     *
     * Una nota que empieza por `## Parcial 2` delataria el formato justo donde menos importa:
     * quien mira la lista viene a acordarse de que hay parcial, no a leer Markdown. Se limpia
     * tambien en las notas sencillas porque las dos maneras guardan lo mismo.
     */
    val plano = remember(note.body) { NoteMarkdown.strip(note.body) }
    val title = NoteText.title(plano)
    val preview = NoteText.preview(plano, maxLines = if (compact) 4 else 8)

    UniCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
        if (portada != null) NoteCardThumbnail(pathFor(portada), compact)
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            /*
             * El filo de color, y no un borde entero.
             *
             * Tenir la tarjeta entera del color de la materia convierte la lista en un semaforo
             * y pone el texto a pelear con el fondo. Un filo de tres puntos en el canto dice lo
             * mismo y deja que la nota siga siendo papel.
             */
            if (accent != null) {
                Box(
                    Modifier
                        .width(3.dp)
                        .fillMaxHeight()
                        .background(accent)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = if (accent != null) 11.dp else 14.dp,
                        end = 14.dp,
                        top = 12.dp,
                        bottom = 11.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (title.isNotBlank()) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (preview.isNotBlank()) {
                    Text(
                        text = preview,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = if (compact) 4 else 8,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    if (note.pinned) {
                        Icon(
                            Icons.Rounded.PushPin,
                            contentDescription = "Fijada",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    if (subject != null) NoteSubjectChip(subject)
                    Text(
                        text = timeLabel,
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                    NoteAttachmentSummary(attachments)
                }
            }
        }
        }
    }
}

/** La cabecera de un dia en el cuaderno. */
@Composable
fun NoteDayHeader(label: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * La fila de filtros: «Todas» y una pastilla por materia.
 *
 * Solo salen las materias que tienen alguna nota. Una fila con las nueve del semestre obliga a
 * recorrerlas enteras para descubrir que siete estan vacias.
 */
@Composable
fun NoteSubjectFilters(
    subjects: List<Subject>,
    selectedSubjectId: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill(
            label = "Todas",
            selected = selectedSubjectId == null,
            accent = MaterialTheme.colorScheme.primary,
            onClick = { onSelect(null) }
        )
        subjects.forEach { subject ->
            FilterPill(
                label = subject.name,
                selected = selectedSubjectId == subject.id,
                accent = subjectAccent(subject),
                onClick = { onSelect(subject.id) }
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) accent.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 7.dp)
        )
    }
}
