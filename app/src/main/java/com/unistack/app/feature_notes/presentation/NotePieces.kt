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
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteReminders
import com.unistack.app.feature_notes.domain.QuickNote
import java.time.Instant
import java.time.ZoneId

/**
 * El redondeo de una nota.
 *
 * Menos que el `large` del tema, que en una tarjeta pequeña dejaba las esquinas tan blandas que
 * la lista parecía un montón de pastillas. Una nota es papel: se le nota el canto.
 */
private val FormaNota = RoundedCornerShape(12.dp)

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
    onToggleCheck: ((Int) -> Unit)? = null,
    selected: Boolean = false
) {
    val accent = subject?.let { subjectAccent(it) }
    val portada = remember(attachments) {
        attachments.firstOrNull { it.kind == AttachmentKind.IMAGE }
    }
    val audio = remember(attachments) {
        attachments.firstOrNull { it.kind == AttachmentKind.AUDIO }
    }
    val archivo = remember(attachments) {
        attachments.firstOrNull { it.kind == AttachmentKind.FILE }
    }
    val casillasTotal = remember(note.body) { NoteMarkdown.checkboxes(note.body) }
    val casillasHechas = remember(casillasTotal) { casillasTotal.count { it.checked } }
    val fondo = NoteColors.surfaceFor(note.colorArgb)
    val enFondo = NoteColors.contentOn(fondo, MaterialTheme.colorScheme.onSurface)
    val suave = enFondo.copy(alpha = 0.66f)

    /*
     * Una tarjeta de contorno, no de relleno.
     *
     * Antes era una superficie rellena con un filo de color en el canto. Dos cosas peleaban con
     * el texto: el relleno, que hacia que la lista se leyera como un montaje de bloques, y el
     * filo, que convertia la pantalla en un semaforo. Con el fondo de la pantalla y una linea de
     * un punto alrededor, lo que se ve son las notas.
     *
     * La nota con color propio si va rellena: ahi el color es lo que la distingue.
     */
    UniCard(
        color = fondo,
        borderColor = when {
            // Marcada: el borde del acento y mas grueso, que es como se ve en Keep.
            selected -> MaterialTheme.colorScheme.primary
            note.colorArgb == null -> MaterialTheme.colorScheme.outlineVariant
            else -> Color.Transparent
        },
        borderWidth = when {
            selected -> 2.dp
            note.colorArgb == null -> 1.dp
            else -> 0.dp
        },
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 15.dp, end = 15.dp, top = 13.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    NoteTypeRow(
                        casillasTotal = casillasTotal.size,
                        casillasHechas = casillasHechas,
                        audioName = audio?.displayName,
                        reminderAt = note.reminderAt,
                        archivoName = archivo?.displayName,
                        content = enFondo,
                        soft = suave
                    )
                    if (note.title.isNotBlank()) {
                        Text(
                            text = note.title,
                            color = enFondo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    NoteCardBody(
                        note = note,
                        maxLines = if (compact) 4 else 7,
                        conTitulo = note.title.isBlank(),
                        texto = enFondo,
                        suave = suave,
                        onToggleCheck = onToggleCheck,
                        showDoneCount = casillasTotal.isEmpty()
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (subject != null) {
                            NoteSubjectChip(subject, modifier = Modifier.weight(1f, fill = false))
                        }
                        Spacer(Modifier.weight(1f))
                        if (note.pinned) {
                            Icon(
                                Icons.Rounded.PushPin,
                                contentDescription = "Fijada",
                                tint = suave,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = timeLabel,
                            color = suave.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
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
    conTitulo: Boolean,
    texto: Color,
    suave: Color,
    onToggleCheck: ((Int) -> Unit)?,
    showDoneCount: Boolean = true
) {
    val plano = remember(note.body) { NoteMarkdown.strip(note.body).lines() }
    val casillas = remember(note.body) {
        NoteMarkdown.checkboxes(note.body).associateBy { it.lineIndex }
    }
    /*
     * Las marcadas se recogen en una linea, como hace Keep.
     *
     * Una lista de doce pendientes con nueve tachados ocupa la tarjeta entera para decir lo que
     * queda por hacer, que son tres. Lo hecho se cuenta y se aparta; sigue estando dentro.
     */
    val hechas = remember(casillas) { casillas.values.count { it.checked } }
    val visibles = remember(plano, maxLines, casillas) {
        plano.withIndex()
            .filter { it.value.isNotBlank() && casillas[it.index]?.checked != true }
            .take(maxLines)
    }
    val hayMas = remember(plano, visibles, casillas) {
        plano.withIndex()
            .count { it.value.isNotBlank() && casillas[it.index]?.checked != true } > visibles.size
    }

    visibles.forEachIndexed { orden, (indice, linea) ->
        val casilla = casillas[indice]
        when {
            /*
             * En la tarjeta las casillas se ven, pero no se tocan.
             *
             * Se probo tocandolas y era un fastidio: en una tarjeta de mosaico el cuadro mide
             * quince puntos, esta pegado al texto, y acertar sin abrir la nota por error costaba
             * mas que abrirla. Como en Keep: la tarjeta se toca entera y lleva a la nota, que es
             * donde hay sitio para marcar.
             */
            casilla != null -> Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CheckBoxMark(casilla.checked)
                Spacer(Modifier.width(10.dp))
                Text(
                    text = casilla.label,
                    modifier = Modifier.weight(1f),
                    color = if (casilla.checked) suave.copy(alpha = 0.5f) else suave,
                    style = MaterialTheme.typography.bodyMedium,
                    textDecoration = if (casilla.checked) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Sin titulo propio, la primera linea hace de titular: es lo que habia antes de
            // que el titulo fuera un campo, y sigue haciendo falta en las notas viejas.
            conTitulo && orden == 0 -> Text(
                text = linea,
                color = texto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            else -> Text(
                text = linea,
                color = suave,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (hayMas) {
        Text(
            text = "…",
            color = suave.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall
        )
    }
    if (hechas > 0 && showDoneCount) {
        Text(
            text = "+ " + hechas + if (hechas == 1) " marcada" else " marcadas",
            color = suave.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 3.dp)
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

/**
 * Lo que hace especial a esta nota, antes del título: una lista con su avance, un audio, un
 * archivo o un aviso con su fecha en bloque. Una sola señal y en ese orden — es la que manda en
 * la tarjeta, no un resumen de todo lo que lleva.
 */
@Composable
private fun NoteTypeRow(
    casillasTotal: Int,
    casillasHechas: Int,
    audioName: String?,
    reminderAt: Long?,
    archivoName: String?,
    content: Color,
    soft: Color
) {
    if (casillasTotal == 0 && audioName == null && reminderAt == null && archivoName == null) return
    val error = MaterialTheme.colorScheme.error
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        when {
            casillasTotal > 0 -> {
                ChecklistRing(casillasHechas, casillasTotal, content)
                NoteTypeCaption("Lista", "$casillasHechas de $casillasTotal hechas", content, soft)
            }
            audioName != null -> {
                AudioWaveform(content)
                NoteTypeCaption("Audio", "nota de voz", content, soft)
            }
            reminderAt != null -> {
                val vencido = NoteReminders.isDue(reminderAt)
                NoteDateTile(reminderAt, vencido, content, error)
                NoteTypeCaption(
                    if (vencido) "Venció" else "Recordatorio",
                    NoteReminders.label(reminderAt),
                    if (vencido) error else content,
                    soft
                )
            }
            archivoName != null -> {
                Icon(
                    Icons.Rounded.Description,
                    contentDescription = null,
                    tint = soft,
                    modifier = Modifier.size(22.dp)
                )
                NoteTypeCaption("Archivo", archivoName, content, soft)
            }
        }
    }
}

@Composable
private fun NoteTypeCaption(caption: String, detail: String, contentColor: Color, softColor: Color) {
    Column {
        Text(
            text = caption,
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = detail,
            color = softColor,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** El avance de una lista, en un anillo — el mismo dato que ya cuentan las casillas de abajo. */
@Composable
private fun ChecklistRing(hechas: Int, total: Int, content: Color) {
    CircularProgressIndicator(
        progress = { if (total == 0) 0f else hechas.toFloat() / total },
        modifier = Modifier.size(26.dp),
        color = content,
        trackColor = content.copy(alpha = 0.25f),
        strokeWidth = 2.5.dp,
        strokeCap = StrokeCap.Round
    )
}

private val AlturasOnda = listOf(6, 13, 9, 17, 10, 19, 8, 14, 10)

/** Una grabación, antes de abrirla: una forma de onda quieta, no un ícono. */
@Composable
private fun AudioWaveform(tint: Color) {
    Row(
        modifier = Modifier
            .height(24.dp)
            .width(38.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp)
    ) {
        AlturasOnda.forEach { alto ->
            Box(
                Modifier
                    .width(3.dp)
                    .height(alto.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(tint.copy(alpha = 0.7f))
            )
        }
    }
}

private val MesesAbrev = listOf(
    "ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC"
)

/** El recordatorio, como una fecha de calendario: el día grande, el mes debajo. */
@Composable
private fun NoteDateTile(at: Long, vencido: Boolean, content: Color, error: Color) {
    val fecha = remember(at) { Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate() }
    val tono = if (vencido) error else content
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(tono.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = fecha.dayOfMonth.toString(),
                color = tono,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black
            )
            Text(
                text = MesesAbrev[fecha.monthValue - 1],
                color = tono,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                fontWeight = FontWeight.Bold
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
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
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
