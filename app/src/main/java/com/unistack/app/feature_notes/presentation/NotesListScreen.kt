@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.unistack.app.feature_notes.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteDay
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteGrouping
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteSearch
import com.unistack.app.feature_notes.domain.NoteText
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.QuickNote
import java.time.LocalDate

/** Con qué arranca una nota nueva, según por dónde se pidió. */
enum class NewNoteStart(val route: String) {
    TEXTO("texto"),
    LISTA("lista"),
    FOTO("foto"),
    AUDIO("audio"),
    ARCHIVO("archivo");

    companion object {
        fun of(value: String?): NewNoteStart = entries.firstOrNull { it.route == value } ?: TEXTO
    }
}

/**
 * Notas rápidas.
 *
 * La cabecera es la barra de buscar y no un título: buscar entre las notas propias es lo que más
 * se hace con ellas, y esconderlo detrás de una lupa cobraba un toque por algo que debería estar
 * ya puesto. Dentro de la misma barra viven la forma de ver y el filtro, que es donde se buscan.
 *
 * El botón de crear despliega el tipo de nota. Empezar por una foto de la pizarra o por una
 * grabación es tan normal como empezar escribiendo, y con un solo botón había que abrir la nota
 * en blanco y buscar el icono después.
 */
@Composable
fun NotesListScreen(
    onBackClick: () -> Unit,
    onNoteClick: (String) -> Unit,
    onNewNoteClick: (NewNoteStart) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allAttachments by viewModel.attachments.collectAsStateWithLifecycle()

    var filterSubjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var searching by rememberSaveable { mutableStateOf(false) }
    var acting by rememberSaveable { mutableStateOf<String?>(null) }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    var filtering by rememberSaveable { mutableStateOf(false) }

    val layout = profile?.notesLayout ?: NotesLayout.CUADERNO
    val use24Hour = profile?.accessibilityPreferences?.use24HourTime ?: true
    val today = remember { LocalDate.now() }

    // Solo se ofrecen como filtro las materias de las que hay algo escrito: una lista con las
    // nueve del semestre obliga a recorrerlas para descubrir que siete están vacías.
    val subjectsWithNotes = remember(notes, subjects) {
        val used = notes.mapNotNull { it.subjectId }.toSet()
        subjects.filter { it.id in used }
    }
    val activeFilter = filterSubjectId?.takeIf { id -> subjectsWithNotes.any { it.id == id } }

    val visible = remember(notes, activeFilter, query) {
        val porMateria =
            if (activeFilter == null) notes else notes.filter { it.subjectId == activeFilter }
        NoteSearch.filter(porMateria, query)
    }
    val porNota = remember(allAttachments) { allAttachments.groupBy { it.noteId } }
    val pinned = remember(visible) { NoteGrouping.pinned(visible) }
    val days = remember(visible, today) { NoteGrouping.byDay(visible, today) }
    val materiaFiltrada = subjectsWithNotes.firstOrNull { it.id == activeFilter }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                NotesSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    searching = searching,
                    onSearchingChange = {
                        searching = it
                        if (!it) query = ""
                    },
                    layout = layout,
                    onToggleLayout = {
                        viewModel.setLayout(
                            if (layout == NotesLayout.MOSAICO) {
                                NotesLayout.CUADERNO
                            } else {
                                NotesLayout.MOSAICO
                            }
                        )
                    },
                    canFilter = subjectsWithNotes.isNotEmpty(),
                    onFilterClick = { filtering = true },
                    onBackClick = onBackClick,
                    menu = {
                        Box {
                            UniIconButton(
                                icon = Icons.Rounded.MoreVert,
                                contentDescription = "Más opciones",
                                onClick = { menuOpen = true }
                            )
                            NotesOverflowMenu(
                                expanded = menuOpen,
                                onDismiss = { menuOpen = false },
                                defaultFormat = profile?.noteFormatDefault ?: NoteFormat.PLAIN,
                                onDefaultFormat = viewModel::setDefaultFormat,
                                canSeed = viewModel.canSeedSamples,
                                onSeed = viewModel::seedSamples,
                                onRemoveSamples = viewModel::removeSamples
                            )
                        }
                    }
                )
                if (materiaFiltrada != null) {
                    FilterBanner(
                        subject = materiaFiltrada,
                        count = visible.size,
                        onClear = { filterSubjectId = null }
                    )
                }
            }
        },
        floatingActionButton = { NewNoteFab(onPick = onNewNoteClick) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                    onNoteLongClick = { acting = it },
                    onToggleCheck = viewModel::toggleCheck
                )

                else -> NotesNotebook(
                    pinned = pinned,
                    days = days,
                    subjectFor = { viewModel.subjectById(it) },
                    attachmentsFor = { porNota[it].orEmpty() },
                    pathFor = { viewModel.attachmentPath(it) },
                    use24Hour = use24Hour,
                    onNoteClick = onNoteClick,
                    onNoteLongClick = { acting = it },
                    onToggleCheck = viewModel::toggleCheck
                )
            }
        }
    }

    if (filtering) {
        NoteFilterSheet(
            subjects = subjectsWithNotes,
            counts = remember(notes) {
                notes.mapNotNull { it.subjectId }.groupingBy { it }.eachCount()
            },
            total = notes.size,
            selectedSubjectId = activeFilter,
            onSelect = {
                filterSubjectId = it
                filtering = false
            },
            onDismiss = { filtering = false }
        )
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

/**
 * La cabecera: una barra de buscar que además guarda la forma de ver y el filtro.
 *
 * No hay título. A esta pantalla se llega desde un sitio que ya dice a dónde vas, y el nombre
 * repetido arriba solo ocupaba la línea donde ahora se busca.
 */
@Composable
private fun NotesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    searching: Boolean,
    onSearchingChange: (Boolean) -> Unit,
    layout: NotesLayout,
    onToggleLayout: () -> Unit,
    canFilter: Boolean,
    onFilterClick: () -> Unit,
    onBackClick: () -> Unit,
    menu: @Composable () -> Unit
) {
    val foco = remember { FocusRequester() }
    LaunchedEffect(searching) {
        if (searching) runCatching { foco.requestFocus() }
    }
    BackHandler(enabled = searching) { onSearchingChange(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniIconButton(
            icon = if (searching) Icons.Rounded.Close else Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = if (searching) "Cerrar la búsqueda" else "Atrás",
            onClick = { if (searching) onSearchingChange(false) else onBackClick() }
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(start = 15.dp, end = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(Modifier.width(11.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            "Buscar en tus notas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (searching) {
                        BasicTextField(
                            value = query,
                            onValueChange = onQueryChange,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().focusRequester(foco),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        // Sin el campo puesto, todo el hueco del texto es el botón de buscar.
                        Surface(
                            onClick = { onSearchingChange(true) },
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth().height(46.dp)
                        ) {}
                    }
                }
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
                    onClick = onToggleLayout
                )
                if (canFilter) {
                    UniIconButton(
                        icon = Icons.Rounded.FilterList,
                        contentDescription = "Filtrar por materia",
                        onClick = onFilterClick
                    )
                }
            }
        }
        menu()
    }
}

/** Con filtro puesto, una línea que lo dice y lo quita. */
@Composable
private fun FilterBanner(subject: Subject, count: Int, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NoteSubjectChip(subject, modifier = Modifier.weight(1f, fill = false))
        Text(
            count.toString() + if (count == 1) " nota" else " notas",
            color = MaterialTheme.colorScheme.outline,
            style = MaterialTheme.typography.labelSmall
        )
        Spacer(Modifier.weight(1f))
        UniIconButton(
            icon = Icons.Rounded.Close,
            contentDescription = "Quitar el filtro",
            onClick = onClear
        )
    }
}

/**
 * El botón de crear, desplegado por tipo.
 *
 * Empezar por una foto de la pizarra o por una grabación es tan normal como empezar escribiendo.
 * Con un solo botón había que abrir la nota en blanco y buscar el icono después; aquí se elige
 * antes y la nota se abre ya con la cámara o el micrófono delante.
 */
@Composable
private fun NewNoteFab(onPick: (NewNoteStart) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = expanded) { expanded = false }

    val entradas = listOf(
        Triple("Texto", Icons.Rounded.TextFields, NewNoteStart.TEXTO),
        Triple("Lista", Icons.Rounded.CheckBox, NewNoteStart.LISTA),
        Triple("Foto", Icons.Rounded.Image, NewNoteStart.FOTO),
        Triple("Audio", Icons.Rounded.Mic, NewNoteStart.AUDIO),
        Triple("Archivo", Icons.Rounded.AttachFile, NewNoteStart.ARCHIVO)
    )

    FloatingActionButtonMenu(
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it },
                containerSize = ToggleFloatingActionButtonDefaults.containerSize(
                    initialSize = 60.dp,
                    finalSize = 68.dp
                ),
                // Con el menú abierto, el lector de pantalla llega antes al botón que a las
                // opciones: es lo que las cierra.
                modifier = Modifier.semantics { traversalIndex = -1f }
            ) {
                val icono: ImageVector = if (expanded) Icons.Rounded.Close else Icons.Rounded.Add
                Icon(icono, contentDescription = if (expanded) "Cerrar" else "Nueva nota")
            }
        }
    ) {
        entradas.forEach { (label, icono, tipo) ->
            FloatingActionButtonMenuItem(
                onClick = {
                    expanded = false
                    onPick(tipo)
                },
                icon = { Icon(icono, contentDescription = null) },
                text = { Text(label) }
            )
        }
    }
}

@Composable
private fun NotesOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    defaultFormat: NoteFormat,
    onDefaultFormat: (NoteFormat) -> Unit,
    canSeed: Boolean,
    onSeed: () -> Unit,
    onRemoveSamples: () -> Unit
) {
    UniDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        /*
         * El ajuste de formato vive aquí y no dentro del editor.
         *
         * Son dos cosas distintas: el interruptor del editor cambia esa nota, y esto dice con
         * qué nacen las siguientes. Juntarlas haría que cambiar una nota cambiara todas las
         * futuras sin haberlo pedido.
         */
        Text(
            "Notas nuevas",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 2.dp)
        )
        NoteFormat.entries.forEach { opcion ->
            DropdownMenuItem(
                text = { Text(if (opcion == NoteFormat.MARKDOWN) "Markdown" else "Normal") },
                onClick = {
                    onDefaultFormat(opcion)
                    onDismiss()
                },
                trailingIcon = {
                    if (defaultFormat == opcion) {
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
        if (canSeed) {
            /*
             * Notas de mentira para poder mirar las de verdad. Solo en dev, alpha y beta: en una
             * versión publicada, un botón que mete ocho notas falsas entre las de alguien es la
             * forma más rápida de perder su confianza.
             */
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            DropdownMenuItem(
                text = { Text("Crear notas de ejemplo") },
                onClick = {
                    onSeed()
                    onDismiss()
                }
            )
            DropdownMenuItem(
                text = { Text("Quitar las de ejemplo") },
                onClick = {
                    onRemoveSamples()
                    onDismiss()
                }
            )
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
    notes: List<QuickNote>,
    subjectFor: (String?) -> Subject?,
    attachmentsFor: (String) -> List<NoteAttachment>,
    pathFor: (NoteAttachment) -> String,
    use24Hour: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onToggleCheck: (String, Int) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 130.dp),
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
                onLongClick = { onNoteLongClick(note.id) },
                onToggleCheck = { linea -> onToggleCheck(note.id, linea) }
            )
        }
    }
}

/** Cuaderno: una columna por días, con la fecha arriba de cada montón. */
@Composable
private fun NotesNotebook(
    pinned: List<QuickNote>,
    days: List<NoteDay>,
    subjectFor: (String?) -> Subject?,
    attachmentsFor: (String) -> List<NoteAttachment>,
    pathFor: (NoteAttachment) -> String,
    use24Hour: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onToggleCheck: (String, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 130.dp),
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
                    onLongClick = { onNoteLongClick(note.id) },
                    onToggleCheck = { linea -> onToggleCheck(note.id, linea) }
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
                    onLongClick = { onNoteLongClick(note.id) },
                    onToggleCheck = { linea -> onToggleCheck(note.id, linea) }
                )
            }
        }
    }
}

/**
 * Elegir por qué materia se mira la lista.
 *
 * Solo salen las materias que tienen alguna nota, con cuántas: una lista con las nueve del
 * semestre obliga a recorrerlas enteras para descubrir que siete están vacías.
 */
@Composable
private fun NoteFilterSheet(
    subjects: List<Subject>,
    counts: Map<String, Int>,
    total: Int,
    selectedSubjectId: String?,
    onSelect: (String?) -> Unit,
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
                .padding(bottom = 20.dp)
        ) {
            Text(
                "Filtrar por materia",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp)
            )
            FilterRow(
                name = "Todas",
                accent = MaterialTheme.colorScheme.primary,
                count = total,
                selected = selectedSubjectId == null,
                onClick = { onSelect(null) }
            )
            subjects.forEach { subject ->
                FilterRow(
                    name = subject.name,
                    accent = subjectAccent(subject),
                    count = counts[subject.id] ?: 0,
                    selected = selectedSubjectId == subject.id,
                    onClick = { onSelect(subject.id) }
                )
            }
        }
    }
}

@Composable
private fun FilterRow(
    name: String,
    accent: Color,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (selected) accent.copy(alpha = 0.10f) else Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
            Text(
                name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                count.toString(),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelMedium
            )
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
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
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 18.dp)
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
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        contentColor = tint,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
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
