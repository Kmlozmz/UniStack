@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.unistack.app.feature_notes.presentation

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Image
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.Attachments
import com.unistack.app.feature_notes.domain.NoteAction
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteFormatting
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteMention
import com.unistack.app.feature_notes.domain.NoteMoment
import com.unistack.app.feature_notes.domain.NoteSearch
import com.unistack.app.feature_notes.domain.NoteText
import kotlinx.coroutines.delay
import java.time.LocalDateTime

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
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allAttachments by viewModel.attachments.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // El identificador vive en el estado porque una nota nueva todavía no tiene: nace en el
    // primer guardado y a partir de ahí los siguientes tienen que actualizar, no insertar.
    var currentId by rememberSaveable { mutableStateOf(noteId) }
    val existing = remember(notes, currentId) { viewModel.noteById(currentId) }

    var loaded by rememberSaveable { mutableStateOf(noteId == null) }
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var subjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var format by rememberSaveable { mutableStateOf<NoteFormat?>(null) }
    var pickingSubject by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    var showingHelp by rememberSaveable { mutableStateOf(false) }
    var warningAboutSimple by rememberSaveable { mutableStateOf(false) }
    var recording by rememberSaveable { mutableStateOf(false) }
    var pendingPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var attachError by rememberSaveable { mutableStateOf<String?>(null) }
    var dismissedSuggestion by rememberSaveable { mutableStateOf<String?>(null) }

    val body = value.text
    // Hasta que el perfil carga no hay ajuste que leer, y una nota nueva no puede nacer con un
    // formato inventado: mientras tanto vale el de partida, que es el mismo que trae el perfil.
    val activeFormat = format ?: existing?.format ?: profile?.noteFormatDefault ?: NoteFormat.MARKDOWN

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
            value = TextFieldValue(note.body)
            subjectId = note.subjectId
            format = note.format
            loaded = true
        }
    }

    val dirty = loaded && (
        body.trimEnd() != existing?.body.orEmpty() ||
            subjectId != existing?.subjectId ||
            (existing != null && activeFormat != existing.format)
        )

    LaunchedEffect(body, subjectId, activeFormat, loaded) {
        if (!loaded || !dirty) return@LaunchedEffect
        delay(600)
        val id = viewModel.saveNote(currentId, body, subjectId, activeFormat)
        if (id != null) currentId = id
    }

    /*
     * Salir guarda, pero solo si la nota llegó a leerse.
     *
     * Sin la condición, abrir una nota y volver atrás antes de que la base conteste guardaría un
     * campo vacío sobre ella, y guardar vacío es borrar: se perdería la nota por el simple hecho
     * de haberla abierto.
     */
    val leave: () -> Unit = {
        if (loaded) {
            val id = viewModel.saveNote(currentId, body, subjectId, activeFormat)
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

    val misAdjuntos = remember(allAttachments, currentId) {
        allAttachments.filter { it.noteId == currentId }
    }

    /*
     * Colgar algo necesita una nota a la que colgarlo.
     *
     * En una nota nueva el primer gesto puede ser perfectamente la foto y no la primera letra,
     * asi que aqui se crea la fila antes de abrir el selector. Si luego se sale sin escribir
     * nada, la nota se queda: tiene una foto dentro, que ya es contenido.
     */
    val asegurarNota: () -> String = {
        val id = viewModel.ensureNoteId(currentId, body, subjectId, activeFormat)
        currentId = id
        loaded = true
        id
    }

    val elegirFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val id = currentId
        if (uri != null && id != null && !viewModel.attach(id, uri)) {
            attachError = "No se pudo guardar la foto. Puede que pase de " +
                Attachments.formatSize(Attachments.MAX_BYTES) + "."
        }
    }

    val elegirArchivo = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        val id = currentId
        if (uri != null && id != null && !viewModel.attach(id, uri)) {
            attachError = "No se pudo guardar el archivo. Puede que pase de " +
                Attachments.formatSize(Attachments.MAX_BYTES) + "."
        }
    }

    val hacerFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { hecha ->
        val guardado = pendingPhoto
        pendingPhoto = null
        val id = currentId
        if (guardado == null) return@rememberLauncherForActivityResult
        if (!hecha || id == null) {
            viewModel.discardStoredFile(guardado)
            return@rememberLauncherForActivityResult
        }
        val puesta = viewModel.attachStoredFile(
            noteId = id,
            storedName = guardado,
            displayName = "Foto",
            mimeType = "image/jpeg",
            kind = AttachmentKind.IMAGE
        )
        if (!puesta) attachError = "No se pudo guardar la foto."
    }

    val abrirAdjunto: (com.unistack.app.feature_notes.domain.NoteAttachment) -> Unit = { adjunto ->
        val uri = viewModel.attachmentUri(adjunto)
        if (uri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, adjunto.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Si no hay ninguna app que abra ese tipo, se dice en vez de cerrarse.
            runCatching { context.startActivity(intent) }
                .onFailure { attachError = "No hay ninguna app en el teléfono que abra esto." }
        }
    }

    /*
     * El reloj de la sugerencia.
     *
     * Se mira al entrar y una vez por minuto. Sin el latido, quien abre el editor un minuto
     * antes de que empiece la clase no ve la sugerencia aparecer nunca, y quien lo deja abierto
     * hasta que acaba la clase la sigue viendo media hora despues.
     */
    var ahora by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            ahora = LocalDateTime.now()
        }
    }

    val enClase = remember(sessions, ahora) { NoteMoment.subjectInClassNow(sessions, ahora) }
    val sugerida = subjects.firstOrNull { it.id == enClase }
    val haySugerencia = sugerida != null &&
        NoteMoment.shouldSuggest(enClase, subjectId, dismissedSuggestion)

    /*
     * La mencion con arroba, mientras se escribe.
     *
     * Solo cuando el cursor esta suelto: con texto seleccionado no se esta escribiendo un
     * nombre de materia, se esta a punto de darle formato.
     */
    val mencion = remember(value.text, value.selection) {
        if (value.selection.collapsed) NoteMention.at(value.text, value.selection.start) else null
    }
    val candidatas = remember(mencion, subjects) {
        val consulta = mencion ?: return@remember emptyList()
        NoteSearch.matchingSubjects(subjects.map { it.id to it.name }, consulta.token).take(4)
    }

    val subject = subjects.firstOrNull { it.id == subjectId }
    val saved = loaded && !dirty && body.isNotBlank()
    val palette = rememberNotePalette()
    val transformation = remember(activeFormat, palette) {
        NoteVisualTransformation(activeFormat, palette)
    }

    val cambiarFormato: (NoteFormat) -> Unit = { destino ->
        when {
            destino == activeFormat -> Unit
            destino == NoteFormat.PLAIN && NoteMarkdown.hasRichBlocks(body) -> warningAboutSimple = true
            else -> format = destino
        }
    }

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
                        val fijada = existing?.pinned == true
                        UniIconButton(
                            icon = if (fijada) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                            contentDescription = if (fijada) "Quitar de fijadas" else "Fijar arriba",
                            onClick = { currentId?.let { viewModel.setPinned(it, !fijada) } }
                        )
                    }
                    UniIconButton(
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        contentDescription = "Qué se puede escribir",
                        onClick = { showingHelp = true }
                    )
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
                /*
                 * La barra de formato sale al seleccionar, y solo en sencillo.
                 *
                 * En Markdown estorbaría: ahí las marcas se escriben a mano y se ven, así que un
                 * botón de negrita sería una segunda forma de hacer lo mismo. Aquí es la única.
                 */
                /*
                 * Las materias que encajan con lo tecleado tras la arroba.
                 *
                 * Van aqui abajo, pegadas al teclado, y no flotando sobre el texto: una lista
                 * encima de lo que se escribe tapa justo la linea que se esta escribiendo.
                 */
                AnimatedVisibility(
                    visible = candidatas.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        candidatas.forEach { (id, nombre) ->
                            val materia = subjects.firstOrNull { it.id == id }
                            Surface(
                                onClick = {
                                    val consulta = mencion ?: return@Surface
                                    val cambio = NoteMention.accept(value.text, consulta, nombre)
                                    value = TextFieldValue(
                                        text = cambio.text.take(NoteText.MAX_LENGTH),
                                        selection = TextRange(
                                            cambio.selectionStart.coerceIn(0, cambio.text.length)
                                        )
                                    )
                                    subjectId = id
                                },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                contentColor = materia?.let { subjectAccent(it) }
                                    ?: MaterialTheme.colorScheme.onSurface
                            ) {
                                Text(
                                    nombre,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = activeFormat == NoteFormat.PLAIN && !value.selection.collapsed,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    NoteFormatBar(
                        onAction = { action ->
                            val cambio = NoteFormatting.apply(
                                action = action,
                                text = value.text,
                                selectionStart = value.selection.start,
                                selectionEnd = value.selection.end
                            )
                            value = TextFieldValue(
                                text = cambio.text.take(NoteText.MAX_LENGTH),
                                selection = TextRange(
                                    cambio.selectionStart.coerceIn(0, cambio.text.length),
                                    cambio.selectionEnd.coerceIn(0, cambio.text.length)
                                )
                            )
                        }
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cuatro botones, la materia y el interruptor no caben en un telefono
                    // estrecho: lo de la izquierda se desplaza y el interruptor no se mueve.
                    Row(
                        modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                    AttachButton(Icons.Rounded.Image, "Añadir una foto") {
                        asegurarNota()
                        elegirFoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    AttachButton(Icons.Rounded.PhotoCamera, "Hacer una foto") {
                        asegurarNota()
                        val (nombre, archivo) = viewModel.newAttachmentFile("jpg")
                        runCatching { archivo.createNewFile() }
                        val uri = runCatching {
                            androidx.core.content.FileProvider.getUriForFile(
                                context,
                                context.packageName + ".provider",
                                archivo
                            )
                        }.getOrNull()
                        if (uri == null) {
                            viewModel.discardStoredFile(nombre)
                            attachError = "No se pudo preparar la cámara."
                        } else {
                            pendingPhoto = nombre
                            hacerFoto.launch(uri)
                        }
                    }
                    AttachButton(Icons.Rounded.AttachFile, "Adjuntar un archivo") {
                        asegurarNota()
                        elegirArchivo.launch(arrayOf("*/*"))
                    }
                    AttachButton(Icons.Rounded.Mic, "Grabar audio") {
                        asegurarNota()
                        recording = true
                    }
                    SubjectButton(
                        subject = subject,
                        enabled = subjects.isNotEmpty(),
                        onClick = { pickingSubject = true }
                    )
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
                    FormatSwitch(format = activeFormat, onChange = cambiarFormato)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(
                visible = haySugerencia,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                NoteSuggestionPill(
                    subjectName = sugerida?.name.orEmpty(),
                    accent = sugerida?.let { subjectAccent(it) }
                        ?: MaterialTheme.colorScheme.primary,
                    onLink = {
                        subjectId = enClase
                        dismissedSuggestion = null
                    },
                    onDismiss = { dismissedSuggestion = enClase },
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 12.dp)
                )
            }
            Box(modifier = Modifier.fillMaxWidth()) {
            if (body.isEmpty()) {
                Text(
                    "Escribe aquí…",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = { nuevo ->
                    value = if (nuevo.text.length <= NoteText.MAX_LENGTH) {
                        nuevo
                    } else {
                        nuevo.copy(text = nuevo.text.take(NoteText.MAX_LENGTH))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(start = 22.dp, end = 22.dp, bottom = 26.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 25.sp
                ),
                visualTransformation = transformation,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )
            }
            NoteAttachmentStrip(
                attachments = misAdjuntos,
                pathFor = { viewModel.attachmentPath(it) },
                existsFor = { viewModel.attachmentFileExists(it) },
                onOpen = abrirAdjunto,
                onRemove = { viewModel.removeAttachment(it.id) },
                modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 26.dp)
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

    if (recording) {
        NoteRecorderSheet(
            createFile = { extension -> viewModel.newAttachmentFile(extension) },
            onDiscard = { nombre -> viewModel.discardStoredFile(nombre) },
            onSaved = { nombre, duracion ->
                val id = currentId
                val puesta = id != null && viewModel.attachStoredFile(
                    noteId = id,
                    storedName = nombre,
                    displayName = "Grabacion",
                    mimeType = "audio/mp4",
                    kind = AttachmentKind.AUDIO,
                    durationMillis = duracion
                )
                if (!puesta) {
                    viewModel.discardStoredFile(nombre)
                    attachError = "No se pudo guardar la grabacion."
                }
            },
            onDismiss = { recording = false }
        )
    }

    attachError?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { attachError = null },
            title = { Text("No se pudo adjuntar") },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = { attachError = null }) { Text("Entendido") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    if (showingHelp) {
        NoteFormatHelpSheet(format = activeFormat, onDismiss = { showingHelp = false })
    }

    if (warningAboutSimple) {
        /*
         * Cambiar a sencillo no convierte ni pierde nada, pero deja cosas sin botón.
         *
         * Las dos maneras guardan el mismo texto: lo único que cambia es si las marcas se ven.
         * Por eso el aviso no habla de perder, que sería mentira, sino de lo que de verdad pasa:
         * los títulos y las tablas se seguirán viendo y no habrá con qué quitarlos desde ahí.
         */
        AlertDialog(
            onDismissRequest = { warningAboutSimple = false },
            title = { Text("Esta nota tiene títulos o tablas") },
            text = {
                Text(
                    "No se pierde nada: es el mismo texto con las marcas escondidas. Pero la barra " +
                        "de escritura sencilla no tiene botón para títulos, tablas ni bloques de " +
                        "código, así que se seguirán viendo y no podrás quitarlos hasta que vuelvas " +
                        "a Markdown."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    format = NoteFormat.PLAIN
                    warningAboutSimple = false
                }) {
                    Text("Cambiar igual", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { warningAboutSimple = false }) { Text("Quedarme en Markdown") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("¿Borrar la nota?") },
            text = {
                Text("Se borra «${NoteText.label(NoteMarkdown.strip(body))}» y no se puede deshacer.")
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

/** Un boton redondo de la fila de adjuntar. */
@Composable
private fun AttachButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Icon(
            icon,
            contentDescription = description,
            modifier = Modifier.padding(9.dp).size(19.dp)
        )
    }
}

/**
 * El interruptor de las dos maneras de escribir.
 *
 * Cambia **esta** nota y no el ajuste: el ajuste dice con qué nacen las nuevas y vive en el menú
 * de la lista. Fue lo que él eligió —«un ajuste por defecto, y se puede cambiar en una nota»—
 * porque las dos cosas por separado son lo que hace falta: quien escribe casi todo en Markdown
 * no quiere tocar el ajuste para un apunte suelto de dos líneas.
 */
@Composable
private fun FormatSwitch(format: NoteFormat, onChange: (NoteFormat) -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            FormatSwitchOption(
                label = "MD",
                description = "Escribir en Markdown",
                selected = format == NoteFormat.MARKDOWN,
                monospace = true,
                onClick = { onChange(NoteFormat.MARKDOWN) }
            )
            FormatSwitchOption(
                label = "Aa",
                description = "Escribir sin marcas",
                selected = format == NoteFormat.PLAIN,
                monospace = false,
                onClick = { onChange(NoteFormat.PLAIN) }
            )
        }
    }
}

@Composable
private fun FormatSwitchOption(
    label: String,
    description: String,
    selected: Boolean,
    monospace: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        contentColor = if (selected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontFamily = if (monospace) FontFamily.Monospace else FontFamily.Default,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 11.dp, vertical = 6.dp)
                .semanticsDescription(description)
        )
    }
}

private fun Modifier.semanticsDescription(description: String): Modifier =
    this.then(
        Modifier.semantics { contentDescription = description }
    )

/**
 * La barra que sale al seleccionar en escritura sencilla.
 *
 * No hay subrayado y sí tachado. Las dos maneras guardan Markdown —eso es lo que permite pasar
 * de una a la otra sin convertir ni perder—, y en Markdown el subrayado no existe: ponerlo aquí
 * obligaría a inventar una marca propia que luego nadie más sabría leer.
 */
@Composable
private fun NoteFormatBar(onAction: (NoteAction) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FormatBarButton("B", NoteAction.NEGRITA, onAction) { FontWeight.ExtraBold }
        FormatBarButton("I", NoteAction.CURSIVA, onAction, italic = true) { FontWeight.Medium }
        FormatBarButton("S", NoteAction.TACHADO, onAction, strike = true) { FontWeight.Medium }
        FormatBarButton("•", NoteAction.VINETA, onAction) { FontWeight.Bold }
        FormatBarButton("1.", NoteAction.NUMERADA, onAction) { FontWeight.Bold }
        FormatBarButton("☐", NoteAction.CASILLA, onAction) { FontWeight.Bold }
    }
}

@Composable
private fun FormatBarButton(
    label: String,
    action: NoteAction,
    onAction: (NoteAction) -> Unit,
    italic: Boolean = false,
    strike: Boolean = false,
    weight: () -> FontWeight
) {
    Surface(
        onClick = { onAction(action) },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = weight(),
            fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = if (strike) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier
                .padding(horizontal = 13.dp, vertical = 6.dp)
                .semanticsDescription(action.label)
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
    accent: Color,
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
