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
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
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
import com.unistack.app.core.design.components.UniSearchField
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteGrouping
import com.unistack.app.feature_notes.domain.NoteSearch
import com.unistack.app.feature_notes.domain.NoteText
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.QuickNote
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
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var acting by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }

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

    val visible = remember(notes, activeFilter, query) {
        val porMateria =
            if (activeFilter == null) notes else notes.filter { it.subjectId == activeFilter }
        NoteSearch.filter(porMateria, query)
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
                        /*
                         * Buscar se despliega, no ocupa sitio siempre.
                         *
                         * Filtros, buscador y fijadas apilados se comen media pantalla antes de
                         * la primera nota. Detras de la lupa, la lista empieza arriba.
                         */
                        UniIconButton(
                            icon = Icons.Rounded.Search,
                            contentDescription = if (searching) "Cerrar la búsqueda" else "Buscar",
                            onClick = {
                                searching = !searching
                                if (!searching) query = ""
                            }
                        )
                    }
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
                            /*
                              * El rotulo va corto porque el menu mide lo que mide su texto.
                              *
                              * Con «Las notas nuevas nacen en» entero, el menu salia mas ancho
                              * que el hueco que le queda a la derecha y se cortaba contra el
                              * borde de la pantalla.
                              */
                            Text(
                                "Notas nuevas",
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
                            if (viewModel.canSeedSamples) {
                                /*
                                 * Notas de mentira para poder mirar las de verdad.
                                 *
                                 * Solo en dev, alpha y beta: en una version publicada, un boton
                                 * que mete ocho notas falsas entre las notas de alguien es la
                                 * forma mas rapida de perder su confianza.
                                 */
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                DropdownMenuItem(
                                    text = { Text("Crear notas de ejemplo") },
                                    onClick = {
                                        viewModel.seedSamples()
                                        menuOpen = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Quitar las de ejemplo") },
                                    onClick = {
                                        viewModel.removeSamples()
                                        menuOpen = false
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
            if (searching) {
                UniSearchField(
                    query = query,
                    onQueryChange = { query = it },
                    placeholder = "Buscar en tus notas…",
                    modifier = Modifier.padding(start = 18.dp, end = 18.dp, bottom = 10.dp)
                )
            }
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

                visible.isEmpty() && query.isNotBlank() -> EmptyNotes(
                    headline = "Nada con eso",
                    body = "No hay ninguna nota que diga «" + query.trim() + "»."
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
                    onNoteClick = onNoteClick,
                    onNoteLongClick = { acting = it }
                )

                else -> NotesNotebook(
                    pinned = pinned,
                    days = days,
                    subjectFor = { viewModel.subjectById(it) },
                    attachmentsFor = { porNota[it].orEmpty() },
                    pathFor = { viewModel.attachmentPath(it) },
                    use24Hour = use24Hour,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = { acting = it }
                )
            }
        }
    }

    acting?.let { noteId ->
        val nota = notes.firstOrNull { it.id == noteId }
        NoteActionsSheet(
            pinned = nota?.pinned == true,
            onPin = {
                nota?.let { viewModel.setPinned(it.id, !it.pinned) }
                acting = null
            },
            onDelete = {
                acting = null
                deleting = noteId
            },
            onDismiss = { acting = null }
        )
    }

    deleting?.let { noteId ->
        val nota = notes.firstOrNull { it.id == noteId }
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("¿Borrar la nota?") },
            text = {
                Text(
                    "Se borra «" + NoteText.label(NoteMarkdown.strip(nota?.body.orEmpty())) +
                        "» con lo que lleve dentro, y no se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteNote(noteId)
                    deleting = null
                }) {
                    Text(
                        "Borrar",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

/** Lo que hay detrás de una nota al dejar el dedo puesto. */
@Composable
private fun NoteActionsSheet(
    pinned: Boolean,
    onPin: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionRow(
                icon = Icons.Rounded.PushPin,
                label = if (pinned) "Quitar de fijadas" else "Fijar arriba",
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onPin
            )
            ActionRow(
                icon = Icons.Rounded.DeleteOutline,
                label = "Borrar",
                tint = MaterialTheme.colorScheme.error,
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = tint,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(13.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit
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
                pathFor = pathFor,
                onLongClick = { onNoteLongClick(note.id) }
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
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit
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
                    pathFor = pathFor,
                    onLongClick = { onNoteLongClick(note.id) }
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
                    pathFor = pathFor,
                    onLongClick = { onNoteLongClick(note.id) }
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
