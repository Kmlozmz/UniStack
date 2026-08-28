@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.unistack.app.feature_notes.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_notes.domain.NoteText
import kotlinx.coroutines.delay

/**
 * Escribir una nota, a pantalla completa.
 *
 * Fue una corrección suya y tiene motivo: en un recuadro dentro de la lista no caben el
 * interruptor de formato, la ayuda ni los adjuntos, y sobre todo no cabe escribir. Una nota de
 * clase son quince líneas, no dos.
 *
 * Se guarda sola —600 ms después de la última tecla, como hacía la hoja de antes— y también al
 * salir. Si al salir no hay nada escrito, no se guarda nada: abrir el editor y arrepentirse no
 * puede dejar papeles en blanco por la lista.
 */
@Composable
fun NoteEditorScreen(
    noteId: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()

    // El identificador vive en el estado porque una nota nueva todavía no tiene: nace en el
    // primer guardado y a partir de ahí los siguientes tienen que actualizar, no insertar.
    var currentId by rememberSaveable { mutableStateOf(noteId) }
    val existing = remember(notes, currentId) { viewModel.noteById(currentId) }

    var loaded by rememberSaveable { mutableStateOf(noteId == null) }
    var body by rememberSaveable { mutableStateOf("") }
    var subjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingSubject by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }

    /*
     * La nota se lee una sola vez.
     *
     * Sin el cerrojo, cada guardado devuelve la nota por el flujo y el efecto reescribiría el
     * campo con lo que hay en la base: quien siguiera escribiendo durante ese viaje vería
     * desaparecer las letras de en medio.
     */
    LaunchedEffect(existing?.id) {
        val note = existing
        if (!loaded && note != null) {
            body = note.body
            subjectId = note.subjectId
            loaded = true
        }
    }

    val dirty = loaded && (body.trimEnd() != existing?.body.orEmpty() || subjectId != existing?.subjectId)

    LaunchedEffect(body, subjectId, loaded) {
        if (!loaded || !dirty) return@LaunchedEffect
        delay(600)
        val id = viewModel.saveNote(currentId, body, subjectId)
        if (id != null) currentId = id
    }

    /*
     * Salir guarda, pero solo si la nota llego a leerse.
     *
     * Sin la condicion, abrir una nota y volver atras antes de que la base conteste guardaria un
     * campo vacio sobre ella, y guardar vacio es borrar: se perderia la nota por el simple hecho
     * de haberla abierto.
     */
    val leave: () -> Unit = {
        if (loaded) {
            val id = viewModel.saveNote(currentId, body, subjectId)
            if (id != null) currentId = id
        }
        onBackClick()
    }

    BackHandler(enabled = true) { leave() }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (noteId == null) {
            // Nota nueva: el teclado sube solo. Se viene a escribir, no a mirar una hoja.
            runCatching { focusRequester.requestFocus() }
        }
    }

    val subject = subjects.firstOrNull { it.id == subjectId }
    val saved = loaded && !dirty && body.isNotBlank()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    UniIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Atrás",
                        onClick = leave
                    )
                },
                actions = {
                    AnimatedVisibility(visible = saved) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    "Guardado",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (currentId != null) {
                        UniIconButton(
                            icon = Icons.Rounded.DeleteOutline,
                            contentDescription = "Borrar la nota",
                            onClick = { confirmingDelete = true }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubjectButton(
                        subject = subject,
                        enabled = subjects.isNotEmpty(),
                        onClick = { pickingSubject = true }
                    )
                    Spacer(Modifier.weight(1f))
                    // El contador solo aparece cuando queda poco. Enseñar «31 de 20000» desde la
                    // primera letra es poner un límite delante de quien viene a escribir.
                    if (body.length > NoteText.MAX_LENGTH - 500) {
                        Text(
                            "${body.length} de ${NoteText.MAX_LENGTH}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            if (body.isEmpty()) {
                Text(
                    "Escribe aquí…",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp)
                )
            }
            BasicTextField(
                value = body,
                onValueChange = { body = it.take(NoteText.MAX_LENGTH) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(start = 22.dp, end = 22.dp, bottom = 26.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 25.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
        }
    }

    if (pickingSubject) {
        NoteSubjectSheet(
            subjects = subjects,
            selectedSubjectId = subjectId,
            onDismiss = { pickingSubject = false },
            onSelected = {
                subjectId = it
                pickingSubject = false
            }
        )
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("¿Borrar la nota?") },
            text = {
                Text("Se borra «${NoteText.label(body)}» y no se puede deshacer.")
            },
            confirmButton = {
                TextButton(onClick = {
                    currentId?.let(viewModel::deleteNote)
                    confirmingDelete = false
                    onBackClick()
                }) {
                    Text(
                        "Borrar",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

/**
 * El botón de materia del pie del editor.
 *
 * Sin materia dice «Sin materia» en gris y con ella se tiñe de su color, porque vincular es lo
 * único que esta pantalla pide además de escribir y tiene que verse hecho de un vistazo.
 */
@Composable
private fun SubjectButton(
    subject: Subject?,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = subject?.let { subjectAccent(it) }
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = accent?.copy(alpha = 0.14f) ?: MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = accent ?: MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            if (accent != null) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(accent))
            } else {
                Icon(
                    Icons.Rounded.School,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = subject?.name ?: if (enabled) "Sin materia" else "No hay materias",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun NoteSubjectSheet(
    subjects: List<Subject>,
    selectedSubjectId: String?,
    onDismiss: () -> Unit,
    onSelected: (String?) -> Unit
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "¿De qué materia es?",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Vincularla te deja filtrar después por asignatura. Es opcional.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item(key = "sin-materia") {
                    SubjectRow(
                        name = "Sin materia",
                        accent = MaterialTheme.colorScheme.outline,
                        selected = selectedSubjectId == null,
                        onClick = { onSelected(null) }
                    )
                }
                items(subjects, key = { it.id }) { subject ->
                    SubjectRow(
                        name = subject.name,
                        accent = subjectAccent(subject),
                        selected = selectedSubjectId == subject.id,
                        onClick = { onSelected(subject.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectRow(
    name: String,
    accent: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = if (selected) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerLow,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
            Spacer(Modifier.width(11.dp))
            Text(
                name,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}
