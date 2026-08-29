@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.unistack.app.feature_notes.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteGrouping
import com.unistack.app.feature_notes.domain.NotesLayout
import java.time.LocalDate

/**
 * Notas rápidas: una lista de notas sueltas.
 *
 * Era una hoja única. Una sola, de cuatro mil caracteres, que se iba llenando por arriba hasta
 * que encontrar lo del martes pasado costaba más que volver a preguntarlo. Ahora cada apunte es
 * suyo, con su hora y su materia, y se puede mirar solo lo de una asignatura.
 *
 * Dos formas de verlas, porque en el simulador solo dos aguantaron con veinte notas dentro:
 * mosaico —dos columnas, para quien fotografía la pizarra— y cuaderno —una columna por días,
 * para quien escribe—. Se elige aquí mismo y se recuerda.
 */
@Composable
fun NotesListScreen(
    onBackClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onNewNoteClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allAttachments by viewModel.attachments.collectAsStateWithLifecycle()

    var filterSubjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    val layout = profile?.notesLayout ?: NotesLayout.MOSAICO
    val use24Hour = profile?.accessibilityPreferences?.use24HourTime ?: true
    val today = remember { LocalDate.now() }

    // Solo se ofrecen como filtro las materias de las que hay algo escrito: una fila con las
    // nueve del semestre obliga a recorrerlas para descubrir que siete están vacías.
    val subjectsWithNotes = remember(notes, subjects) {
        val used = notes.mapNotNull { it.subjectId }.toSet()
        subjects.filter { it.id in used }
    }

    // Si la materia por la que se filtraba se queda sin notas, el filtro se cae solo; si no, la
    // pantalla se queda vacía con una pastilla marcada que ya no existe en la fila.
    val activeFilter = filterSubjectId?.takeIf { id -> subjectsWithNotes.any { it.id == id } }

    val visible = remember(notes, activeFilter) {
        if (activeFilter == null) notes else notes.filter { it.subjectId == activeFilter }
    }
    val porNota = remember(allAttachments) { allAttachments.groupBy { it.noteId } }
    val pinned = remember(visible) { NoteGrouping.pinned(visible) }
    val days = remember(visible, today) { NoteGrouping.byDay(visible, today) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notas",
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    UniIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        onClick = onBackClick
                    )
                },
                actions = {
                    if (notes.isNotEmpty()) {
                        UniIconButton(
                            icon = if (layout == NotesLayout.MOSAICO) {
                                Icons.AutoMirrored.Rounded.ViewList
                            } else {
                                Icons.Rounded.GridView
                            },
                            contentDescription = if (layout == NotesLayout.MOSAICO) {
                                "Ver como cuaderno"
                            } else {
                                "Ver como mosaico"
                            },
                            onClick = {
                                viewModel.setLayout(
                                    if (layout == NotesLayout.MOSAICO) {
                                        NotesLayout.CUADERNO
                                    } else {
                                        NotesLayout.MOSAICO
                                    }
                                )
                            }
                        )
                    }
                    Box {
                        UniIconButton(
                            icon = Icons.Rounded.MoreVert,
                            contentDescription = "Más opciones",
                            onClick = { menuOpen = true }
                        )
                        /*
                         * El ajuste de formato vive aquí y no dentro del editor.
                         *
                         * Son dos cosas distintas: el interruptor del editor cambia esa nota, y
                         * esto dice con qué nacen las siguientes. Ponerlas juntas haría que
                         * cambiar una nota cambiara todas las futuras sin haberlo pedido.
                         */
                        UniDropdownMenu(
                            expanded = menuOpen,
                            onDismissRequest = { menuOpen = false }
                        ) {
                            Text(
                                "Las notas nuevas nacen en",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp)
                            )
                            NoteFormat.entries.forEach { opcion ->
                                DropdownMenuItem(
                                    text = { Text(if (opcion == NoteFormat.MARKDOWN) "Markdown" else "Sencillo") },
                                    onClick = {
                                        viewModel.setDefaultFormat(opcion)
                                        menuOpen = false
                                    },
                                    trailingIcon = {
                                        if (profile?.noteFormatDefault == opcion) {
                                            Icon(
                                                Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewNoteClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Rounded.EditNote, contentDescription = "Escribir una nota")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (subjectsWithNotes.isNotEmpty()) {
                NoteSubjectFilters(
                    subjects = subjectsWithNotes,
                    selectedSubjectId = activeFilter,
                    onSelect = { filterSubjectId = it },
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 8.dp)
                )
            }

            when {
                notes.isEmpty() -> EmptyNotes(
                    headline = "Todavía no hay nada apuntado",
                    body = "Lo que se dijo en clase, la fecha del parcial, el salón. " +
                        "Escríbelo aquí y luego dile de qué materia es."
                )

                visible.isEmpty() -> EmptyNotes(
                    headline = "Nada de esta materia",
                    body = "No has apuntado nada en esta asignatura todavía."
                )

                layout == NotesLayout.MOSAICO -> NotesMosaic(
                    notes = visible,
                    subjectFor = { viewModel.subjectById(it) },
                    attachmentsFor = { porNota[it].orEmpty() },
                    pathFor = { viewModel.attachmentPath(it) },
                    use24Hour = use24Hour,
                    onNoteClick = onNoteClick
                )

                else -> NotesNotebook(
                    pinned = pinned,
                    days = days,
                    subjectFor = { viewModel.subjectById(it) },
                    attachmentsFor = { porNota[it].orEmpty() },
                    pathFor = { viewModel.attachmentPath(it) },
                    use24Hour = use24Hour,
                    onNoteClick = onNoteClick
                )
            }
        }
    }
}

/**
 * Mosaico: dos columnas de altura libre.
 *
 * Sin cabeceras de día a propósito. La rejilla reparte las notas por altura y no por orden, así
 * que un rótulo «Ayer» encima de una columna estaría mintiendo sobre la otra.
 */
@Composable
private fun NotesMosaic(
    notes: List<com.unistack.app.feature_notes.domain.QuickNote>,
    subjectFor: (String?) -> com.unistack.app.feature_grades.domain.Subject?,
    attachmentsFor: (String) -> List<NoteAttachment>,
    pathFor: (NoteAttachment) -> String,
    use24Hour: Boolean,
    onNoteClick: (String) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(
                note = note,
                subject = subjectFor(note.subjectId),
                timeLabel = NoteGrouping.timeLabel(note, use24Hour),
                onClick = { onNoteClick(note.id) },
                compact = true,
                attachments = attachmentsFor(note.id),
                pathFor = pathFor
            )
        }
    }
}

/** Cuaderno: una columna por días, con la fecha arriba de cada montón. */
@Composable
private fun NotesNotebook(
    pinned: List<com.unistack.app.feature_notes.domain.QuickNote>,
    days: List<com.unistack.app.feature_notes.domain.NoteDay>,
    subjectFor: (String?) -> com.unistack.app.feature_grades.domain.Subject?,
    attachmentsFor: (String) -> List<NoteAttachment>,
    pathFor: (NoteAttachment) -> String,
    use24Hour: Boolean,
    onNoteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (pinned.isNotEmpty()) {
            item(key = "fijadas") { NoteDayHeader("Fijadas", pinned.size) }
            items(pinned, key = { "pin-" + it.id }) { note ->
                NoteCard(
                    note = note,
                    subject = subjectFor(note.subjectId),
                    timeLabel = NoteGrouping.timeLabel(note, use24Hour),
                    onClick = { onNoteClick(note.id) },
                    attachments = attachmentsFor(note.id),
                    pathFor = pathFor
                )
            }
        }
        days.forEach { day ->
            item(key = "dia-" + day.date) { NoteDayHeader(day.label, day.notes.size) }
            items(day.notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    subject = subjectFor(note.subjectId),
                    timeLabel = NoteGrouping.timeLabel(note, use24Hour),
                    onClick = { onNoteClick(note.id) },
                    attachments = attachmentsFor(note.id),
                    pathFor = pathFor
                )
            }
        }
    }
}

@Composable
private fun EmptyNotes(headline: String, body: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(horizontal = 36.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Rounded.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                headline,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}
