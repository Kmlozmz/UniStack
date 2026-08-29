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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
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
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.NotificationAdd
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Unarchive
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniTimePickerDialog
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteDay
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteGrouping
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteReminders
import com.unistack.app.feature_notes.domain.NoteSearch
import com.unistack.app.feature_notes.domain.NoteText
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.NotesSort
import com.unistack.app.feature_notes.domain.QuickNote
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Los tres montones de notas que hay. */
enum class NotesView(val title: String) {
    NOTAS("Notas"),
    /** Lo que avisa, de lo más próximo a lo más lejano. */
    RECORDATORIOS("Recordatorios"),
    ARCHIVO("Archivo"),
    PAPELERA("Papelera")
}

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
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    var filtering by rememberSaveable { mutableStateOf(false) }
    var view by rememberSaveable { mutableStateOf(NotesView.NOTAS) }
    var confirmingTrash by rememberSaveable { mutableStateOf<String?>(null) }
    var seleccion by rememberSaveable { mutableStateOf(setOf<String>()) }
    var trashingSelection by rememberSaveable { mutableStateOf(false) }
    var coloringSelection by rememberSaveable { mutableStateOf(false) }
    var reminderFor by rememberSaveable { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()

    // Al abrir, lo que lleve mas de una semana en la papelera se va de verdad. Una papelera que
    // no se vacia sola deja de ser una papelera y pasa a ser un almacen.
    LaunchedEffect(Unit) { viewModel.purgeOldTrash() }

    BackHandler(enabled = seleccion.isNotEmpty()) { seleccion = emptySet() }
    BackHandler(enabled = seleccion.isEmpty() && view != NotesView.NOTAS) { view = NotesView.NOTAS }

    // Al cambiar de monton, lo marcado deja de tener sentido: son notas que ya no se ven.
    LaunchedEffect(view) { seleccion = emptySet() }

    val seleccionadas = remember(seleccion, notes) {
        notes.filter { it.id in seleccion }
    }

    val layout = profile?.notesLayout ?: NotesLayout.CUADERNO
    val use24Hour = profile?.accessibilityPreferences?.use24HourTime ?: true
    val today = remember { LocalDate.now() }

    /*
     * Cada monton es el mismo listado con otro filtro.
     *
     * Archivo y papelera no son pantallas aparte: son la misma, con las mismas formas de ver y
     * el mismo buscador. Duplicarlas habria significado mantener tres listas iguales.
     */
    val delMonton = remember(notes, view) {
        when (view) {
            NotesView.NOTAS -> notes.filter { it.deletedAt == null && !it.archived }
            NotesView.RECORDATORIOS -> NoteReminders.upcoming(
                notes.filter { it.deletedAt == null }
            )
            NotesView.ARCHIVO -> notes.filter { it.deletedAt == null && it.archived }
            NotesView.PAPELERA -> notes.filter { it.deletedAt != null }
        }
    }

    // Solo se ofrecen como filtro las materias de las que hay algo escrito: una lista con las
    // nueve del semestre obliga a recorrerlas para descubrir que siete están vacías.
    val subjectsWithNotes = remember(delMonton, subjects) {
        val used = delMonton.mapNotNull { it.subjectId }.toSet()
        subjects.filter { it.id in used }
    }
    val activeFilter = filterSubjectId?.takeIf { id -> subjectsWithNotes.any { it.id == id } }

    val visible = remember(delMonton, activeFilter, query) {
        val porMateria = if (activeFilter == null) {
            delMonton
        } else {
            delMonton.filter { it.subjectId == activeFilter }
        }
        NoteSearch.filter(porMateria, query)
    }
    val sort = profile?.notesSort ?: NotesSort.MODIFICADA
    val porNota = remember(allAttachments) { allAttachments.groupBy { it.noteId } }
    val pinned = remember(visible, sort) { NoteGrouping.pinned(visible, sort) }
    val otras = remember(visible, sort) { NoteGrouping.others(visible, sort) }
    val materiaFiltrada = subjectsWithNotes.firstOrNull { it.id == activeFilter }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                if (seleccion.isNotEmpty()) {
                    NoteSelectionBar(
                        count = seleccion.size,
                        pinned = seleccionadas.all { it.pinned },
                        archived = seleccionadas.all { it.archived },
                        enPapelera = view == NotesView.PAPELERA,
                        onClose = { seleccion = emptySet() },
                        onPin = {
                            val destino = !seleccionadas.all { it.pinned }
                            seleccionadas.forEach { viewModel.setPinned(it.id, destino) }
                            seleccion = emptySet()
                        },
                        onReminder = { reminderFor = seleccion.first() },
                        onColor = { coloringSelection = true },
                        onArchive = {
                            val destino = !seleccionadas.all { it.archived }
                            seleccionadas.forEach { viewModel.archive(it.id, destino) }
                            seleccion = emptySet()
                        },
                        onRestore = {
                            seleccionadas.forEach { viewModel.restore(it.id) }
                            seleccion = emptySet()
                        },
                        onDelete = { trashingSelection = true }
                    )
                } else NotesSearchBar(
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
                    onBackClick = { if (view != NotesView.NOTAS) view = NotesView.NOTAS else onBackClick() },
                    view = view,
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
                                view = view,
                                onView = { view = it },
                                trashCount = notes.count { it.deletedAt != null },
                                onEmptyTrash = viewModel::emptyTrash,
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
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            // En archivo y papelera no se crea nada: el boton llevaria a escribir una nota que
            // aparecería en otro sitio.
            if (view == NotesView.NOTAS) NewNoteFab(onPick = onNewNoteClick)
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                delMonton.isEmpty() && view == NotesView.ARCHIVO -> EmptyNotes(
                    headline = "El archivo está vacío",
                    body = "Aquí van las notas que ya no usas pero no quieres perder. " +
                        "Se archivan desde la propia nota."
                )

                delMonton.isEmpty() && view == NotesView.PAPELERA -> EmptyNotes(
                    headline = "La papelera está vacía",
                    body = "Lo que borres se queda aquí " + viewModel.diasEnPapelera +
                        " días antes de irse del todo."
                )

                delMonton.isEmpty() -> EmptyNotes(
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
                    pinned = pinned,
                    others = otras,
                    subjectFor = { viewModel.subjectById(it) },
                    attachmentsFor = { porNota[it].orEmpty() },
                    pathFor = { viewModel.attachmentPath(it) },
                    use24Hour = use24Hour,
                    selected = seleccion,
                    onNoteClick = { id ->
                        if (seleccion.isEmpty()) {
                            onNoteClick(id)
                        } else {
                            seleccion = if (id in seleccion) seleccion - id else seleccion + id
                        }
                    },
                    onNoteLongClick = { id -> seleccion = seleccion + id },
                    onToggleCheck = viewModel::toggleCheck
                )

                else -> NotesNotebook(
                    pinned = pinned,
                    others = otras,
                    subjectFor = { viewModel.subjectById(it) },
                    attachmentsFor = { porNota[it].orEmpty() },
                    pathFor = { viewModel.attachmentPath(it) },
                    use24Hour = use24Hour,
                    selected = seleccion,
                    onNoteClick = { id ->
                        if (seleccion.isEmpty()) {
                            onNoteClick(id)
                        } else {
                            seleccion = if (id in seleccion) seleccion - id else seleccion + id
                        }
                    },
                    onNoteLongClick = { id -> seleccion = seleccion + id },
                    onToggleCheck = viewModel::toggleCheck
                )
            }
        }
    }

    if (filtering) {
        NoteFilterSheet(
            sort = sort,
            onSort = viewModel::setSort,
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

    confirmingTrash?.let { noteId ->
        /*
         * Borrar es mover, y se dice.
         *
         * «No se puede deshacer» era verdad y daba miedo; ahora no lo es. El aviso dice a donde
         * va y cuanto se queda ahi, y al hacerlo sale un aviso abajo con el boton de deshacer:
         * la confirmacion protege del descuido y el deshacer, del arrepentimiento.
         */
        AlertDialog(
            onDismissRequest = { confirmingTrash = null },
            title = { Text("¿Mover esta nota a la papelera?") },
            text = {
                Text(
                    "Se queda en la papelera " + viewModel.diasEnPapelera +
                        " días por si te arrepientes, y luego se borra sola."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val antes = viewModel.moveToTrash(noteId)
                    confirmingTrash = null
                    if (antes != null) {
                        alcance.launch {
                            val respuesta = snackbar.showSnackbar(
                                message = "Movida a la papelera",
                                actionLabel = "Deshacer",
                                withDismissAction = true
                            )
                            if (respuesta == SnackbarResult.ActionPerformed) {
                                viewModel.undoTrash(antes)
                            }
                        }
                    }
                }) {
                    Text(
                        "Mover a la papelera",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingTrash = null }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    if (trashingSelection) {
        val cuantas = seleccion.size
        AlertDialog(
            onDismissRequest = { trashingSelection = false },
            title = {
                Text(
                    if (cuantas == 1) {
                        "¿Mover esta nota a la papelera?"
                    } else {
                        "¿Mover estas " + cuantas + " notas a la papelera?"
                    }
                )
            },
            text = {
                Text(
                    "Se quedan en la papelera " + viewModel.diasEnPapelera +
                        " días por si te arrepientes, y luego se borran solas."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val antes = seleccionadas
                    antes.forEach { viewModel.moveToTrash(it.id) }
                    trashingSelection = false
                    seleccion = emptySet()
                    alcance.launch {
                        val respuesta = snackbar.showSnackbar(
                            message = if (antes.size == 1) {
                                "Movida a la papelera"
                            } else {
                                antes.size.toString() + " notas movidas a la papelera"
                            },
                            actionLabel = "Deshacer",
                            withDismissAction = true
                        )
                        if (respuesta == SnackbarResult.ActionPerformed) {
                            antes.forEach { viewModel.undoTrash(it) }
                        }
                    }
                }) {
                    Text(
                        "Mover a la papelera",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { trashingSelection = false }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    if (coloringSelection) {
        NoteColorSheet(
            selected = seleccionadas.firstOrNull()?.colorArgb,
            onPick = { color ->
                seleccionadas.forEach { viewModel.setColor(it.id, color) }
                coloringSelection = false
                seleccion = emptySet()
            },
            onDismiss = { coloringSelection = false }
        )
    }

    reminderFor?.let { noteId ->
        NoteReminderPicker(
            current = notes.firstOrNull { it.id == noteId }?.reminderAt,
            onPicked = { cuando ->
                viewModel.setReminder(noteId, cuando)
                reminderFor = null
                seleccion = emptySet()
            },
            onDismiss = { reminderFor = null }
        )
    }

    deleting?.let { noteId ->
        val nota = notes.firstOrNull { it.id == noteId }
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("¿Borrar del todo?") },
            text = {
                Text(
                    "Se borra «" +
                        nota?.title?.trim()?.ifBlank { NoteText.label(NoteMarkdown.strip(nota.body)) } +
                        "» con lo que lleve dentro, y esta vez no se puede deshacer."
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
    view: NotesView,
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
                            if (view == NotesView.NOTAS) {
                                "Buscar en tus notas"
                            } else {
                                "Buscar en " + view.title.lowercase()
                            },
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

/**
 * La barra de cuando hay notas marcadas.
 *
 * Sustituye a la cabecera entera, como en Keep, en vez de abrir una hoja por abajo. La hoja
 * obligaba a levantar el dedo, mirar al otro extremo de la pantalla y elegir; aquí las acciones
 * salen donde estaba la barra y sirven para una nota o para doce.
 *
 * El recordatorio solo aparece con una marcada. Ponerle la misma hora a seis notas a la vez no
 * es lo que nadie quiere y no hay forma de deshacerlo de una pasada.
 */
@Composable
private fun NoteSelectionBar(
    count: Int,
    pinned: Boolean,
    archived: Boolean,
    enPapelera: Boolean,
    onClose: () -> Unit,
    onPin: () -> Unit,
    onReminder: () -> Unit,
    onColor: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 6.dp, end = 6.dp, top = 6.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RoundIconButton(Icons.Rounded.Close, "Quitar la selección", onClose)
        Spacer(Modifier.width(10.dp))
        Text(
            count.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        if (enPapelera) {
            RoundIconButton(Icons.Rounded.Restore, "Restaurar", onRestore)
            RoundIconButton(Icons.Rounded.DeleteForever, "Borrar del todo", onDelete)
        } else {
            RoundIconButton(
                if (pinned) Icons.Outlined.PushPin else Icons.Rounded.PushPin,
                if (pinned) "Quitar de fijadas" else "Fijar arriba",
                onPin
            )
            if (count == 1) {
                RoundIconButton(Icons.Rounded.NotificationAdd, "Recordatorio", onReminder)
            }
            RoundIconButton(Icons.Rounded.Palette, "Color", onColor)
            RoundIconButton(
                if (archived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                if (archived) "Sacar del archivo" else "Archivar",
                onArchive
            )
            RoundIconButton(Icons.Rounded.DeleteOutline, "Mover a la papelera", onDelete)
        }
    }
}

/** Un icono con su círculo, como los del pie del editor. */
@Composable
internal fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color? = null
) {
    val color = tint ?: MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = color.copy(alpha = 0.08f),
        contentColor = color
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.padding(9.dp).size(20.dp))
    }
}

/**
 * Poner o quitar la hora de un recordatorio: primero el día, luego la hora.
 *
 * Vive aparte porque hace falta en dos sitios —dentro de la nota y con una marcada en la lista—
 * y son exactamente los mismos dos pasos.
 */
@Composable
internal fun NoteReminderPicker(
    current: Long?,
    onPicked: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    var dia by rememberSaveable { mutableStateOf<Long?>(null) }
    val zona = ZoneId.systemDefault()

    if (dia == null) {
        UniDatePickerDialog(
            selectedDate = current?.let { Instant.ofEpochMilli(it).atZone(zona).toLocalDate() }
                ?: NoteReminders.defaultMoment().toLocalDate(),
            onDateSelected = { fecha -> dia = fecha.toEpochDay() },
            onDismiss = onDismiss
        )
    } else {
        UniTimePickerDialog(
            selectedTime = current?.let { Instant.ofEpochMilli(it).atZone(zona).toLocalTime() }
                ?: LocalTime.of(8, 0),
            title = "¿A qué hora?",
            onTimeSelected = { hora ->
                onPicked(
                    LocalDate.ofEpochDay(dia!!).atTime(hora).atZone(zona).toInstant().toEpochMilli()
                )
                dia = null
            },
            onDismiss = {
                dia = null
                onDismiss()
            },
            extraAction = if (current != null) {
                {
                    TextButton(onClick = {
                        onPicked(null)
                        dia = null
                    }) {
                        Text("Quitar", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                null
            }
        )
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
    view: NotesView,
    onView: (NotesView) -> Unit,
    trashCount: Int,
    onEmptyTrash: () -> Unit,
    canSeed: Boolean,
    onSeed: () -> Unit,
    onRemoveSamples: () -> Unit
) {
    UniDropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        /*
         * Los tres montones, sin barra lateral.
         *
         * Keep los pone en un cajon que se abre desde la izquierda. Aqui esta pantalla se abre
         * desde otro sitio y ya tiene su boton de atras: un cajon encima seria una segunda forma
         * de navegar para tres entradas.
         */
        NotesView.entries.forEach { opcion ->
            DropdownMenuItem(
                text = { Text(opcion.title) },
                leadingIcon = {
                    Icon(
                        when (opcion) {
                            NotesView.NOTAS -> Icons.Rounded.EditNote
                            NotesView.RECORDATORIOS -> Icons.Rounded.Notifications
                            NotesView.ARCHIVO -> Icons.Rounded.Archive
                            NotesView.PAPELERA -> Icons.Rounded.DeleteOutline
                        },
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                },
                trailingIcon = {
                    if (view == opcion) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                onClick = {
                    onView(opcion)
                    onDismiss()
                }
            )
        }
        if (view == NotesView.PAPELERA && trashCount > 0) {
            DropdownMenuItem(
                text = { Text("Vaciar la papelera", color = MaterialTheme.colorScheme.error) },
                onClick = {
                    onEmptyTrash()
                    onDismiss()
                }
            )
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )
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
    pinned: List<QuickNote>,
    others: List<QuickNote>,
    subjectFor: (String?) -> Subject?,
    attachmentsFor: (String) -> List<NoteAttachment>,
    pathFor: (NoteAttachment) -> String,
    use24Hour: Boolean,
    selected: Set<String>,
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
        if (pinned.isNotEmpty()) {
            item(key = "h-fijadas", span = StaggeredGridItemSpan.FullLine) {
                NoteDayHeader("Fijadas", pinned.size)
            }
            items(pinned, key = { "pin-" + it.id }) { note ->
                MosaicCard(note, subjectFor, attachmentsFor, pathFor, use24Hour, note.id in selected, onNoteClick, onNoteLongClick, onToggleCheck)
            }
            if (others.isNotEmpty()) {
                item(key = "h-otras", span = StaggeredGridItemSpan.FullLine) {
                    NoteDayHeader("Otras", others.size)
                }
            }
        }
        items(others, key = { it.id }) { note ->
            MosaicCard(note, subjectFor, attachmentsFor, pathFor, use24Hour, note.id in selected, onNoteClick, onNoteLongClick, onToggleCheck)
        }
    }
}

@Composable
private fun MosaicCard(
    note: QuickNote,
    subjectFor: (String?) -> Subject?,
    attachmentsFor: (String) -> List<NoteAttachment>,
    pathFor: (NoteAttachment) -> String,
    use24Hour: Boolean,
    selected: Boolean,
    onNoteClick: (String) -> Unit,
    onNoteLongClick: (String) -> Unit,
    onToggleCheck: (String, Int) -> Unit
) {
    NoteCard(
        note = note,
        subject = subjectFor(note.subjectId),
        timeLabel = NoteGrouping.timeLabel(note, use24Hour),
        onClick = { onNoteClick(note.id) },
        compact = true,
        selected = selected,
        attachments = attachmentsFor(note.id),
        pathFor = pathFor,
        onLongClick = { onNoteLongClick(note.id) },
        onToggleCheck = { linea -> onToggleCheck(note.id, linea) }
    )
}

/** Cuaderno: una columna por días, con la fecha arriba de cada montón. */
@Composable
private fun NotesNotebook(
    pinned: List<QuickNote>,
    others: List<QuickNote>,
    subjectFor: (String?) -> Subject?,
    attachmentsFor: (String) -> List<NoteAttachment>,
    pathFor: (NoteAttachment) -> String,
    use24Hour: Boolean,
    selected: Set<String>,
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
                    selected = note.id in selected,
                    attachments = attachmentsFor(note.id),
                    pathFor = pathFor,
                    onLongClick = { onNoteLongClick(note.id) },
                    onToggleCheck = { linea -> onToggleCheck(note.id, linea) }
                )
            }
            if (others.isNotEmpty()) {
                item(key = "otras") { NoteDayHeader("Otras", others.size) }
            }
        }
        items(others, key = { it.id }) { note ->
            NoteCard(
                note = note,
                subject = subjectFor(note.subjectId),
                timeLabel = NoteGrouping.timeLabel(note, use24Hour),
                onClick = { onNoteClick(note.id) },
                selected = note.id in selected,
                attachments = attachmentsFor(note.id),
                pathFor = pathFor,
                onLongClick = { onNoteLongClick(note.id) },
                onToggleCheck = { linea -> onToggleCheck(note.id, linea) }
            )
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
    sort: NotesSort,
    onSort: (NotesSort) -> Unit,
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
                "Ordenar por",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 4.dp, bottom = 6.dp)
            )
            SortRow("Última modificación", sort == NotesSort.MODIFICADA) {
                onSort(NotesSort.MODIFICADA)
            }
            SortRow("Fecha de creación", sort == NotesSort.CREADA) {
                onSort(NotesSort.CREADA)
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Text(
                "Filtrar por materia",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 6.dp)
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
private fun SortRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        } else {
            Color.Transparent
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
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
    enPapelera: Boolean,
    pinned: Boolean,
    archived: Boolean,
    onPin: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
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
            if (enPapelera) {
                ActionRow(
                    icon = Icons.Rounded.Restore,
                    label = "Restaurar",
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onRestore
                )
                ActionRow(
                    icon = Icons.Rounded.DeleteForever,
                    label = "Borrar del todo",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onDelete
                )
            } else {
                ActionRow(
                    icon = Icons.Rounded.PushPin,
                    label = if (pinned) "Quitar de fijadas" else "Fijar arriba",
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onPin
                )
                ActionRow(
                    icon = if (archived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                    label = if (archived) "Sacar del archivo" else "Archivar",
                    tint = MaterialTheme.colorScheme.onSurface,
                    onClick = onArchive
                )
                ActionRow(
                    icon = Icons.Rounded.DeleteOutline,
                    label = "Mover a la papelera",
                    tint = MaterialTheme.colorScheme.error,
                    onClick = onDelete
                )
            }
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
