@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.unistack.app.feature_notes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.rounded.Check
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.QuickNote

/**
 * El redondeo de una nota.
 *
 * Menos que el `large` del tema, que en una tarjeta pequeña dejaba las esquinas tan blandas que
 * la lista parecía un montón de pastillas. Una nota es papel: se le nota el canto.
 */
private val FormaNota = RoundedCornerShape(14.dp)

/**
 * La materia de una nota: un punto de su color y el nombre, sin más.
 *
 * Antes era una píldora con fondo teñido, y con nombres como «SISTEMAS DE ACUMULACIÓN DE COSTOS»
 * se comía la mitad de la tarjeta y competía con el texto de la nota, que es lo que se viene a
 * leer. El punto se ve igual de rápido y ocupa lo que ocupa una letra.
 */
@Composable
fun NoteSubjectChip(
    subject: Subject,
    modifier: Modifier = Modifier
) {
    val accent = subjectAccent(subject)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
        Text(
            text = subject.name,
            color = accent,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Una nota en la lista.
 *
 * No hay campo de título: el título es la primera línea, en negrita, y debajo va lo que siga.
 * El texto se pinta línea a línea y no de una pieza, y por un motivo concreto: así **las
 * casillas se pueden marcar desde aquí**, sin abrir la nota. Era como estaba en el diseño y se
 * había quedado fuera; una lista de pendientes que no se puede tachar no es una lista.
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
    pathFor: (NoteAttachment) -> String = { "" },
    onLongClick: (() -> Unit)? = null,
    onToggleCheck: ((Int) -> Unit)? = null
) {
    val accent = subject?.let { subjectAccent(it) }
    val portada = remember(attachments) {
        attachments.firstOrNull { it.kind == AttachmentKind.IMAGE }
    }

    UniCard(
        modifier = if (onLongClick == null) {
            modifier.fillMaxWidth()
        } else {
            modifier
                .fillMaxWidth()
                .clip(FormaNota)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
        },
        onClick = if (onLongClick == null) onClick else null,
        shape = FormaNota,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (portada != null) NoteCardThumbnail(pathFor(portada), compact)
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                /*
                 * El filo de color, y no un borde entero.
                 *
                 * Teñir la tarjeta entera del color de la materia convierte la lista en un
                 * semáforo y pone el texto a pelear con el fondo. Un filo de tres puntos en el
                 * canto dice lo mismo y deja que la nota siga siendo papel.
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
                            start = if (accent != null) 12.dp else 15.dp,
                            end = 15.dp,
                            top = 13.dp,
                            bottom = 12.dp
                        ),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    NoteCardBody(
                        note = note,
                        maxLines = if (compact) 5 else 9,
                        onToggleCheck = onToggleCheck
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (note.pinned) {
                            Icon(
                                Icons.Rounded.PushPin,
                                contentDescription = "Fijada",
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        if (subject != null) {
                            NoteSubjectChip(subject, modifier = Modifier.weight(1f, fill = false))
                        }
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

/**
 * El texto de la nota en la tarjeta, línea a línea.
 *
 * Se pinta sobre el texto **sin marcas**, que tiene exactamente las mismas líneas que el
 * original —esconder una marca nunca quita un salto de línea—, así que el número de línea sirve
 * para las dos cosas: para saber qué enseñar y para saber qué casilla se acaba de tocar.
 */
@Composable
private fun NoteCardBody(
    note: QuickNote,
    maxLines: Int,
    onToggleCheck: ((Int) -> Unit)?
) {
    val plano = remember(note.body) { NoteMarkdown.strip(note.body).lines() }
    val casillas = remember(note.body) {
        NoteMarkdown.checkboxes(note.body).associateBy { it.lineIndex }
    }
    val visibles = remember(plano, maxLines) {
        plano.withIndex().filter { it.value.isNotBlank() }.take(maxLines)
    }
    val hayMas = remember(plano, visibles) {
        plano.withIndex().count { it.value.isNotBlank() } > visibles.size
    }

    visibles.forEachIndexed { orden, (indice, linea) ->
        val casilla = casillas[indice]
        when {
            casilla != null -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (onToggleCheck == null) {
                            Modifier
                        } else {
                            Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .combinedClickable(onClick = { onToggleCheck(indice) })
                        }
                    )
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CheckBoxMark(casilla.checked)
                Text(
                    text = casilla.label,
                    color = if (casilla.checked) {
                        MaterialTheme.colorScheme.outline
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = if (casilla.checked) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            orden == 0 -> Text(
                text = linea,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            else -> Text(
                text = linea,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (hayMas) {
        Text(
            text = "…",
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/** La casilla dibujada: un cuadro con el canto suave, y una marca dentro cuando está hecha. */
@Composable
private fun CheckBoxMark(checked: Boolean) {
    val forma = RoundedCornerShape(4.dp)
    val accent = MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(15.dp)
            .clip(forma)
            .then(
                if (checked) {
                    Modifier.background(accent)
                } else {
                    Modifier.border(1.4.dp, MaterialTheme.colorScheme.outline, forma)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(11.dp)
            )
        }
    }
}

/** La cabecera de un día en el cuaderno. */
@Composable
fun NoteDayHeader(label: String, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 12.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = androidx.compose.ui.unit.TextUnit(0.08f, androidx.compose.ui.unit.TextUnitType.Em),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

/**
 * La fila de filtros: «Todas» y una pastilla por materia.
 *
 * Solo salen las materias que tienen alguna nota. Una fila con las nueve del semestre obliga a
 * recorrerlas enteras para descubrir que siete están vacías.
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
        horizontalArrangement = Arrangement.spacedBy(6.dp),
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
        shape = RoundedCornerShape(9.dp),
        color = if (selected) accent.copy(alpha = 0.14f) else Color.Transparent,
        contentColor = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (selected) {
            null
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}
