@file:OptIn(
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.NotificationAdd
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.TextFormat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import com.unistack.app.core.design.components.AvisoDeGuardado
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.design.components.MantenerPantallaEncendida
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniTimePickerDialog
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.Attachments
import com.unistack.app.feature_notes.domain.NoteAction
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteCheckbox
import com.unistack.app.feature_notes.domain.NoteChecklist
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NoteFormatting
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteMention
import com.unistack.app.feature_notes.domain.NoteMoment
import com.unistack.app.feature_notes.domain.NoteReminders
import com.unistack.app.feature_notes.domain.NoteSearch
import com.unistack.app.feature_notes.domain.NoteText
import com.unistack.app.feature_notes.domain.NoteTextEdits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Escribir una nota.
 *
 * Título arriba y cuerpo debajo, como en cualquier libreta. El título era la primera línea del
 * texto, que ahorra un campo y a cambio impide empezar una nota por una lista sin que el primer
 * punto haga de titular.
 *
 * **No hace falta saber Markdown.** Todas las notas son lo mismo por dentro: los botones de la
 * barra de formato escriben las marcas y la app las esconde. Quien quiera escribirlas a mano lo
 * enciende una vez en el menú de la lista y no se le vuelve a preguntar.
 */
@Composable
fun NoteEditorScreen(
    noteId: String?,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    start: NewNoteStart = NewNoteStart.TEXTO,
    initialSubjectId: String? = null,
    viewModel: NotesViewModel = hiltViewModel()
) {
    // Aqui y no en Inicio: «mantener la pantalla encendida» se pidio para estudiar con una
    // nota abierta, y dejar la bandera puesta en toda la app mantendria el telefono despierto
    // mirando el saludo.
    MantenerPantallaEncendida(LocalAccessibilityPreferences.current.keepScreenOn)

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allAttachments by viewModel.attachments.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var currentId by rememberSaveable { mutableStateOf(noteId) }
    val existing = remember(notes, currentId) { viewModel.noteById(currentId) }

    var loaded by rememberSaveable { mutableStateOf(noteId == null) }
    var title by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var subjectId by rememberSaveable { mutableStateOf(initialSubjectId) }
    var reminderAt by rememberSaveable { mutableStateOf<Long?>(null) }
    var colorArgb by rememberSaveable { mutableStateOf<Int?>(null) }

    var pickingSubject by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    var inserting by rememberSaveable { mutableStateOf(false) }
    var picking by rememberSaveable { mutableStateOf(false) }
    var formatting by rememberSaveable { mutableStateOf(false) }
    var recording by rememberSaveable { mutableStateOf(false) }
    var pendingPhoto by rememberSaveable { mutableStateOf<String?>(null) }
    var attachError by rememberSaveable { mutableStateOf<String?>(null) }
    var dismissedSuggestion by rememberSaveable { mutableStateOf<String?>(null) }
    var pickingDate by rememberSaveable { mutableStateOf(false) }
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    val portapapeles = LocalClipboardManager.current
    val avisos = remember { SnackbarHostState() }
    val alcance = rememberCoroutineScope()
    var pendingDate by rememberSaveable { mutableStateOf<Long?>(null) }

    val body = value.text
    // Las marcas se ven solo si alguien lo pidió en el menú. Es una preferencia de cómo se
    // escribe, no una propiedad de cada nota: por dentro todas son iguales.
    val activeFormat = if (profile?.noteFormatDefault == NoteFormat.MARKDOWN) {
        NoteFormat.MARKDOWN
    } else {
        NoteFormat.PLAIN
    }

    /*
     * La lista manda aquí, y no el texto.
     *
     * Se probó al revés —leer los elementos del cuerpo en cada pintada— y rompía las dos cosas
     * que más se hacen: un elemento recién añadido está vacío, así que al releer el cuerpo
     * desaparecía y la nota volvía a ser un texto normal; y las filas se identificaban por su
     * posición, que cambia al mover una, de modo que el arrastre soltaba la fila a medio camino.
     *
     * El cuerpo sigue siendo lo que se guarda: cada cambio en la lista lo reescribe. Lo que ya no
     * hace es mandar mientras se edita.
     */
    var modoLista by rememberSaveable { mutableStateOf(false) }
    var elementos by remember { mutableStateOf(emptyList<com.unistack.app.feature_notes.domain.ChecklistItem>()) }

    val ponerLista: (List<com.unistack.app.feature_notes.domain.ChecklistItem>) -> Unit = { nuevos ->
        elementos = nuevos
        value = value.copy(text = NoteChecklist.render(nuevos))
    }

    LaunchedEffect(existing?.id) {
        val note = existing
        if (!loaded && note != null) {
            title = TextFieldValue(note.title)
            value = TextFieldValue(note.body)
            if (NoteChecklist.isChecklist(note.body)) {
                modoLista = true
                elementos = NoteChecklist.parse(note.body)
            }
            subjectId = note.subjectId
            reminderAt = note.reminderAt
            colorArgb = note.colorArgb
            loaded = true
        }
    }

    val draft = NoteDraft(
        title = title.text,
        body = body,
        subjectId = subjectId,
        reminderAt = reminderAt,
        colorArgb = colorArgb
    )

    val dirty = loaded && (
        title.text.trim() != existing?.title.orEmpty() ||
            body.trimEnd() != existing?.body.orEmpty() ||
            subjectId != existing?.subjectId ||
            reminderAt != existing?.reminderAt ||
            colorArgb != existing?.colorArgb
        )

    /*
     * El contador de guardados, para el aviso de Movimiento.
     *
     * Es un numero y no un booleano porque dos guardados seguidos tienen que dar dos avisos: con
     * un booleano, el segundo no cambia nada y el aviso no vuelve a salir.
     */
    var guardadosHechos by remember { mutableIntStateOf(0) }

    LaunchedEffect(draft, loaded) {
        if (!loaded || !dirty) return@LaunchedEffect
        delay(600)
        val id = viewModel.saveNote(currentId, draft)
        if (id != null) currentId = id
        guardadosHechos++
    }

    /*
     * Salir guarda, pero solo si la nota llegó a leerse.
     *
     * Sin la condición, abrir una nota y volver atrás antes de que la base conteste guardaría un
     * campo vacío sobre ella, y guardar vacío es borrar.
     */
    val leave: () -> Unit = {
        if (loaded) {
            val id = viewModel.saveNote(currentId, draft)
            if (id != null) currentId = id
        }
        onBackClick()
    }

    BackHandler(enabled = true) { if (formatting) formatting = false else leave() }

    val focusRequester = remember { FocusRequester() }
    val misAdjuntos = remember(allAttachments, currentId) {
        allAttachments.filter { it.noteId == currentId }
    }

    val asegurarNota: () -> String = {
        val id = viewModel.ensureNoteId(currentId, draft)
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
        if (!viewModel.attachStoredFile(id, guardado, "Foto", "image/jpeg", AttachmentKind.IMAGE)) {
            attachError = "No se pudo guardar la foto."
        }
    }

    val abrirCamara: () -> Unit = {
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

    val aplicarFormato: (NoteAction) -> Unit = { accion ->
        val cambio = NoteFormatting.apply(
            action = accion,
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

    /** Mandar la nota a donde sea: el texto sin marcas, con el título delante si lo tiene. */
    val compartir: () -> Unit = {
        val plano = NoteMarkdown.strip(body)
        val texto = listOf(title.text.trim(), plano).filter { it.isNotBlank() }.joinToString("\n\n")
        if (texto.isNotBlank()) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, texto)
                title.text.trim().takeIf { it.isNotBlank() }
                    ?.let { putExtra(Intent.EXTRA_SUBJECT, it) }
            }
            runCatching { context.startActivity(Intent.createChooser(intent, "Compartir la nota")) }
        }
    }

    val abrirAdjunto: (NoteAttachment) -> Unit = { adjunto ->
        val uri = viewModel.attachmentUri(adjunto)
        if (uri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, adjunto.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            runCatching { context.startActivity(intent) }
                .onFailure { attachError = "No hay ninguna app en el teléfono que abra esto." }
        }
    }

    /*
     * La nota se abre haciendo aquello por lo que se abrió.
     *
     * Quien toca «Foto» en el botón de crear no quiere una hoja en blanco con un icono de cámara
     * al fondo: quiere la cámara.
     */
    var arrancada by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (noteId != null || arrancada) return@LaunchedEffect
        arrancada = true
        when (start) {
            NewNoteStart.TEXTO -> runCatching { focusRequester.requestFocus() }
            NewNoteStart.LISTA -> {
                modoLista = true
                elementos = listOf(com.unistack.app.feature_notes.domain.ChecklistItem("", false))
            }
            NewNoteStart.FOTO -> {
                asegurarNota()
                elegirFoto.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
            NewNoteStart.AUDIO -> {
                asegurarNota()
                recording = true
            }
            NewNoteStart.ARCHIVO -> {
                asegurarNota()
                elegirArchivo.launch(arrayOf("*/*"))
            }
        }
    }

    // El reloj de la sugerencia: se mira al entrar y una vez por minuto.
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

    val mencion = remember(value.text, value.selection) {
        if (value.selection.collapsed) NoteMention.at(value.text, value.selection.start) else null
    }
    val candidatas = remember(mencion, subjects) {
        val consulta = mencion ?: return@remember emptyList()
        NoteSearch.matchingSubjects(subjects.map { it.id to it.name }, consulta.token).take(4)
    }

    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    val escondido = remember(body, activeFormat) {
        if (activeFormat == NoteFormat.PLAIN) {
            NoteTextEdits.apply(body, NoteTextEdits.hidingEdits(NoteMarkdown.parse(body)))
        } else {
            null
        }
    }
    val aVisible: (Int) -> Int = { offset -> escondido?.toTransformed(offset) ?: offset }
    val textoVisible = escondido?.text ?: body

    val casillas = remember(body) { NoteMarkdown.checkboxes(body) }
    val iniciosDeLinea = remember(body) {
        var acumulado = 0
        body.split("\n").map { linea ->
            val inicio = acumulado
            acumulado += linea.length + 1
            inicio
        }
    }

    val subject = subjects.firstOrNull { it.id == subjectId }
    val saved = loaded && !dirty && (body.isNotBlank() || title.text.isNotBlank())
    val fondo = NoteColors.surfaceFor(colorArgb)
    val enFondo = NoteColors.contentOn(fondo, MaterialTheme.colorScheme.onSurface)
    val suave = enFondo.copy(alpha = 0.62f)
    val palette = rememberNotePalette().copy(texto = enFondo, suave = suave)
    val transformation = remember(activeFormat, palette) {
        NoteVisualTransformation(activeFormat, palette)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = fondo,
        /*
         * El aviso de guardado va **encima del host de avisos**, no en su lugar.
         *
         * Un `Snackbar` es para lo que hay que leer y quiza deshacer; el guardado automatico es
         * lo contrario —una senal de que todo va bien— y ocupando la barra de abajo taparia
         * los avisos que si importan. Aqui flota, se apaga solo y no pide nada.
         */
        snackbarHost = {
            Box(contentAlignment = Alignment.BottomCenter) {
                SnackbarHost(avisos)
                AvisoDeGuardado(marca = guardadosHechos, modifier = Modifier.padding(bottom = 8.dp))
            }
        },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    Box(modifier = Modifier.padding(start = 6.dp)) {
                        RoundIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Atrás", leave, enFondo)
                    }
                },
                actions = {
                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentId != null) {
                            val fijada = existing?.pinned == true
                            RoundIconButton(
                                if (fijada) Icons.Rounded.PushPin else Icons.Outlined.PushPin,
                                if (fijada) "Quitar de fijadas" else "Fijar arriba",
                                { currentId?.let { viewModel.setPinned(it, !fijada) } },
                                enFondo
                            )
                        }
                        RoundIconButton(
                            if (reminderAt == null) {
                                Icons.Rounded.NotificationAdd
                            } else {
                                Icons.Rounded.Notifications
                            },
                            "Recordatorio",
                            { pickingDate = true },
                            enFondo
                        )
                        if (currentId != null) {
                            val archivada = existing?.archived == true
                            RoundIconButton(
                                if (archivada) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                                if (archivada) "Sacar del archivo" else "Archivar",
                                {
                                    currentId?.let { viewModel.archive(it, !archivada) }
                                    if (!archivada) leave()
                                },
                                enFondo
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = fondo)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                if (formatting) {
                    NoteFormatToolbar(
                        onAction = aplicarFormato,
                        onClose = { formatting = false }
                    )
                } else {
                    /*
                     * El pie, sin línea y con los botones redondos.
                     *
                     * Planos y con una raya encima se leían como el borde de la pantalla, no como
                     * cosas que se tocan. Con su círculo de fondo, y el resto del pie del mismo
                     * color que la nota, el pie deja de ser una barra y pasa a ser el papel.
                     */
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FooterButton(Icons.Rounded.Add, "Añadir a la nota", enFondo) {
                            inserting = true
                        }
                        FooterButton(Icons.Rounded.Palette, "Color de la nota", enFondo) {
                            picking = true
                        }
                        FooterButton(Icons.Rounded.TextFormat, "Formato del texto", enFondo) {
                            formatting = true
                        }
                        Spacer(Modifier.weight(1f))
                        if (body.length > NoteText.MAX_LENGTH - 500) {
                            Text(
                                "${body.length} de ${NoteText.MAX_LENGTH}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Box {
                            FooterButton(Icons.Rounded.MoreVert, "Más opciones", enFondo) {
                                menuOpen = true
                            }
                            UniDropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false }
                            ) {
                                /*
                                 * Lo que de verdad se hace con una nota de clase.
                                 *
                                 * «Duplicar» y «Compartir» a secas eran las de cualquier app de
                                 * notas. Aquí lo que falta es el puente con lo demás: un apunte
                                 * que dice «entregar el taller el viernes» quiere ser una tarea.
                                 */
                                DropdownMenuItem(
                                    text = { Text("Crear una tarea con esto") },
                                    onClick = {
                                        menuOpen = false
                                        val hecha = viewModel.taskFromNote(
                                            title.text,
                                            body,
                                            subjectId,
                                            reminderAt
                                        )
                                        alcance.launch {
                                            avisos.showSnackbar(
                                                if (hecha) {
                                                    "Tarea creada en Académico"
                                                } else {
                                                    "Escribe algo antes de crear la tarea"
                                                }
                                            )
                                        }
                                    }
                                )
                                /*
                                 * «Copiar», a secas.
                                 *
                                 * Decia «Copiar el texto sin marcas», y eso era hablarle al
                                 * usuario de algo que no ve: las marcas estan escondidas justo
                                 * para que no tenga que saber que existen. Lo que hace si es
                                 * quitarlas —pegar `**importante**` en otra app seria pegar los
                                 * asteriscos— pero eso es cosa del boton, no suya.
                                 */
                                DropdownMenuItem(
                                    text = { Text("Copiar") },
                                    onClick = {
                                        menuOpen = false
                                        portapapeles.setText(AnnotatedString(NoteMarkdown.strip(body)))
                                        alcance.launch {
                                            avisos.showSnackbar("Copiado")
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Compartir") },
                                    onClick = {
                                        menuOpen = false
                                        compartir()
                                    }
                                )
                                if (currentId != null) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Mover a la papelera",
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            menuOpen = false
                                            confirmingDelete = true
                                        }
                                    )
                                }
                            }
                        }
                    }
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
                    accent = sugerida?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.primary,
                    onLink = {
                        subjectId = enClase
                        dismissedSuggestion = null
                    },
                    onDismiss = { dismissedSuggestion = enClase },
                    modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 10.dp)
                )
            }

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                if (title.text.isEmpty()) {
                    Text(
                        "Título",
                        color = suave,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                BasicTextField(
                    value = title,
                    onValueChange = { nuevo ->
                        title = nuevo.copy(text = nuevo.text.take(120).replace("\n", " "))
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = enFondo,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            }

            /*
             * Lo que la nota es, debajo de su título.
             *
             * La materia estaba arriba, en la cabecera, y con nombres como «SISTEMAS DE
             * ACUMULACIÓN DE COSTOS» se comía la fila entera de botones. Aquí abajo es un dato
             * más de la nota, al lado del recordatorio, que es lo que es.
             */
            if (subject != null || reminderAt != null) {
                /*
                 * Envuelve, no se sale.
                 *
                 * Con «SISTEMAS DE ACUMULACION DE COSTOS» y una fecha al lado no caben los dos
                 * en una linea, y en una `Row` normal el segundo se iba por el borde derecho
                 * con el aspa cortada. Aqui baja a la linea siguiente.
                 */
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 22.dp, end = 22.dp, top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (subject != null) {
                        Surface(
                            onClick = { pickingSubject = true },
                            shape = RoundedCornerShape(8.dp),
                            color = subjectAccent(subject).copy(alpha = 0.16f),
                            contentColor = subjectAccent(subject),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(subjectAccent(subject))
                                )
                                Text(
                                    subject.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    if (reminderAt != null) {
                        ReminderChip(
                            at = reminderAt!!,
                            tint = enFondo,
                            onClear = { reminderAt = null },
                            onClick = { pickingDate = true }
                        )
                    }
                }
            }

            Spacer(Modifier.size(10.dp))

            if (modoLista) {
                NoteChecklistEditor(
                    items = elementos,
                    onChange = ponerLista,
                    texto = enFondo,
                    suave = suave,
                    modifier = Modifier.padding(start = 14.dp, end = 10.dp, bottom = 14.dp)
                )
            } else Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 22.dp, end = 22.dp, bottom = 14.dp)
                    /*
                     * Tocar una casilla la marca, sin salir de la nota.
                     *
                     * El campo de texto se queda con todos los toques, así que hay que mirarlos
                     * antes que él: en la pasada inicial se comprueba si el dedo cayó encima de
                     * una casilla y solo entonces se consume.
                     */
                    .pointerInput(casillas, layout, textoVisible) {
                        awaitPointerEventScope {
                            while (true) {
                                val evento = awaitPointerEvent(PointerEventPass.Initial)
                                val toque = evento.changes.firstOrNull() ?: continue
                                if (!toque.pressed || toque.isConsumed) continue
                                val medido = layout ?: continue
                                val linea = casillaEn(
                                    posicion = toque.position,
                                    casillas = casillas,
                                    iniciosDeLinea = iniciosDeLinea,
                                    aVisible = aVisible,
                                    visibleLength = textoVisible.length,
                                    marcaLarga = activeFormat == NoteFormat.MARKDOWN,
                                    layout = medido
                                )
                                if (linea != null) {
                                    toque.consume()
                                    val nuevo = NoteMarkdown.toggleCheckbox(body, linea)
                                    if (nuevo != null) value = value.copy(text = nuevo)
                                }
                            }
                        }
                    }
            ) {
                if (body.isEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text("Nota", color = suave, style = MaterialTheme.typography.bodyLarge)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = enFondo.copy(alpha = 0.10f),
                                contentColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    "@",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                "para vincularla a una materia",
                                color = suave,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
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
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = enFondo,
                        lineHeight = 25.sp
                    ),
                    visualTransformation = transformation,
                    onTextLayout = { layout = it },
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )

                val medido = layout
                if (candidatas.isNotEmpty() && mencion != null && medido != null) {
                    val cursor = remember(medido, value.selection, escondido) {
                        val visible = aVisible(mencion.start).coerceIn(0, textoVisible.length)
                        runCatching { medido.getCursorRect(visible) }.getOrNull()
                    }
                    if (cursor != null) {
                        MentionPopup(
                            candidatas = candidatas,
                            subjects = subjects,
                            offsetX = cursor.left.toInt(),
                            offsetY = cursor.bottom.toInt() + 8,
                            onPick = { id ->
                                val cambio = NoteMention.accept(value.text, mencion)
                                value = TextFieldValue(
                                    text = cambio.text.take(NoteText.MAX_LENGTH),
                                    selection = TextRange(
                                        cambio.selectionStart.coerceIn(0, cambio.text.length)
                                    )
                                )
                                subjectId = id
                            }
                        )
                    }
                }
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

    if (inserting) {
        NoteInsertSheet(
            onDismiss = { inserting = false },
            onPick = { tipo ->
                inserting = false
                when (tipo) {
                    NoteInsert.CAMARA -> abrirCamara()
                    NoteInsert.GALERIA -> {
                        asegurarNota()
                        elegirFoto.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    NoteInsert.GRABACION -> {
                        asegurarNota()
                        recording = true
                    }
                    NoteInsert.ARCHIVO -> {
                        asegurarNota()
                        elegirArchivo.launch(arrayOf("*/*"))
                    }
                    NoteInsert.CASILLAS -> {
                        // Con la nota en blanco, «Casillas» crea una lista; con algo escrito,
                        // convierte lo que hay en elementos sin perder ni una linea.
                        modoLista = true
                        ponerLista(NoteChecklist.from(body))
                    }
                    NoteInsert.TABLA -> aplicarFormato(NoteAction.TABLA)
                }
            }
        )
    }

    if (picking) {
        NoteColorSheet(
            selected = colorArgb,
            onPick = {
                colorArgb = it
                picking = false
            },
            onDismiss = { picking = false }
        )
    }

    if (pickingDate) {
        NoteReminderPicker(
            current = reminderAt,
            onPicked = {
                reminderAt = it
                pickingDate = false
            },
            onDismiss = { pickingDate = false }
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
                    displayName = "Grabación",
                    mimeType = "audio/mp4",
                    kind = AttachmentKind.AUDIO,
                    durationMillis = duracion
                )
                if (!puesta) {
                    viewModel.discardStoredFile(nombre)
                    attachError = "No se pudo guardar la grabación."
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
        /*
         * Borrar es mover, y se dice.
         *
         * «No se puede deshacer» era verdad y daba miedo; ahora no lo es, así que el aviso dice
         * a dónde va y cuánto tiempo se queda ahí. Es lo que convierte un toque equivocado en
         * algo sin consecuencias.
         */
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("¿Mover esta nota a la papelera?") },
            text = {
                Text(
                    "Se queda en la papelera " + viewModel.diasEnPapelera +
                        " días por si te arrepientes, y luego se borra sola."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    currentId?.let { viewModel.moveToTrash(it) }
                    confirmingDelete = false
                    onBackClick()
                }) {
                    Text(
                        "Mover a la papelera",
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

/** El recordatorio puesto, con su fecha en cristiano y un aspa para quitarlo. */
@Composable
private fun ReminderChip(
    at: Long,
    tint: Color,
    onClear: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*
     * Del mismo tamano que el chip de materia, y ni un punto mas.
     *
     * Salia con el doble de alto: el aspa llevaba su propio relleno dentro del chip, asi que el
     * chip crecia para caberla. Ahora el aspa es un icono a secas y la zona de toque la pone el
     * propio chip, que ya es tocable.
     */
    val color = if (NoteReminders.isDue(at)) MaterialTheme.colorScheme.error else tint
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.14f),
        contentColor = color,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Rounded.Notifications,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Text(
                NoteReminders.label(at),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Icon(
                Icons.Rounded.Close,
                contentDescription = "Quitar el recordatorio",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onClear)
                    .size(13.dp)
            )
        }
    }
}

@Composable
private fun FooterButton(
    icon: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = tint.copy(alpha = 0.07f),
        contentColor = tint.copy(alpha = 0.9f)
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.padding(11.dp).size(21.dp))
    }
}

/**
 * La casilla que hay bajo un toque, si la hay.
 *
 * Se mira línea por línea porque son pocas y el cálculo es barato. El ancho de la zona sensible
 * es el de la marca: con las marcas a la vista ocupa `- [ ] ` y escondidas, el cuadro solo.
 */
private fun casillaEn(
    posicion: Offset,
    casillas: List<NoteCheckbox>,
    iniciosDeLinea: List<Int>,
    aVisible: (Int) -> Int,
    visibleLength: Int,
    marcaLarga: Boolean,
    layout: TextLayoutResult
): Int? {
    val ancho = if (marcaLarga) 6 else 2
    for (casilla in casillas) {
        val inicio = iniciosDeLinea.getOrNull(casilla.lineIndex) ?: continue
        val desde = aVisible(inicio).coerceIn(0, (visibleLength - 1).coerceAtLeast(0))
        if (desde >= visibleLength) continue
        val hasta = (desde + ancho - 1).coerceIn(desde, visibleLength - 1)
        val caja = runCatching { layout.getBoundingBox(desde) }.getOrNull() ?: continue
        val fin = runCatching { layout.getBoundingBox(hasta) }.getOrNull() ?: caja
        val dentro = posicion.x >= caja.left - 8f &&
            posicion.x <= fin.right + 8f &&
            posicion.y >= caja.top &&
            posicion.y <= caja.bottom
        if (dentro) return casilla.lineIndex
    }
    return null
}

/** La lista de materias, flotando justo debajo de la arroba que se está escribiendo. */
@Composable
private fun MentionPopup(
    candidatas: List<Pair<String, String>>,
    subjects: List<Subject>,
    offsetX: Int,
    offsetY: Int,
    onPick: (String) -> Unit
) {
    /*
     * Aparece creciendo desde donde está la arroba.
     *
     * Salía de golpe, entero y a tamaño completo, y una lista que se materializa encima de lo que
     * estás escribiendo se lee como un fallo. Naciendo pequeña desde su esquina se entiende de
     * dónde viene.
     */
    val entrada = remember { Animatable(0f) }
    LaunchedEffect(Unit) { entrada.animateTo(1f, tween(160)) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .offset { IntOffset(offsetX, offsetY) }
            .graphicsLayer {
                alpha = entrada.value
                scaleX = 0.88f + 0.12f * entrada.value
                scaleY = 0.88f + 0.12f * entrada.value
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .widthIn(max = 260.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            candidatas.forEach { (id, nombre) ->
                val materia = subjects.firstOrNull { it.id == id }
                val color = materia?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.primary
                Surface(
                    onClick = { onPick(id) },
                    color = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                        Text(
                            nombre,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
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
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
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
        shape = RoundedCornerShape(12.dp),
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
