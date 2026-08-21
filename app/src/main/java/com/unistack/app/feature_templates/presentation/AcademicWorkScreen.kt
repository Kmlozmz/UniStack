@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_templates.presentation

import com.unistack.app.feature_templates.domain.buildApaReferenceDraft
import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_templates.domain.AcademicTemplateLibrary
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import kotlinx.coroutines.launch

/**
 * Un trabajo, con lo que le pertenece.
 *
 * Antes esto vivía al final de la lista: el checklist, el generador de referencias y el botón de
 * exportar se pintaban debajo de todos los trabajos, aunque hablaran de uno solo. El generador
 * de APA en particular quedaba al fondo de la pantalla, tan lejos del trabajo del que sacaba las
 * fuentes que parecía una herramienta suelta.
 *
 * Aquí cada cosa está donde se usa, y la lista vuelve a ser una lista.
 */
@Composable
fun AcademicWorkScreen(
    workId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AcademicTemplatesViewModel = hiltViewModel()
) {
    val works by viewModel.works.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val work = works.firstOrNull { it.id == workId }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by rememberSaveable { mutableStateOf(false) }

    fun copyToClipboard(text: String) {
        scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("UniStack", text)))
        }
    }

    if (work == null) {
        // El trabajo se borró desde otro sitio mientras esta pantalla estaba abierta: se vuelve
        // en vez de quedarse pintando un hueco.
        androidx.compose.runtime.LaunchedEffect(workId) { onBackClick() }
        return
    }

    val steps = AcademicTemplateLibrary.checklist
    val done = work.completedChecklistIds.count { id -> steps.any { it.id == id } }
    val subjectName = work.subjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name }
    val template = AcademicTemplateLibrary.essayTemplates.firstOrNull { it.id == work.templateId }

    LargeTitleScaffold(
        title = work.title,
        onBackClick = onBackClick,
        modifier = modifier,
        bottomPadding = scrollBottomRoom,
        actions = {
            IconButton(onClick = { editing = !editing }) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = if (editing) "Cerrar la edición" else "Editar trabajo"
                )
            }
        }
    ) {
        item(key = "chips") {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                template?.let { WorkChip(it.title, LocalSectionColors.current.schedule) }
                WorkChip(subjectName ?: "General", MaterialTheme.colorScheme.onSurfaceVariant)
                work.dueDateMillis?.let {
                    WorkChip(TaskDateUtils.dueText(it), LocalSectionColors.current.atRisk)
                }
            }
        }
        item(key = "avance") {
            /*
             * El avance, en grande y con su anillo.
             *
             * La lista ya dice «cuánto llevas» con una barra fina; aquí dentro, que es donde se
             * marcan los pasos, el número tiene que moverse a la vista para que marcar uno se
             * sienta como avanzar y no como tocar una casilla.
             */
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentPadding = PaddingValues(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "$done de ${steps.size}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineMediumEmphasized,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "pasos del checklist",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    CircularWavyProgressIndicator(
                        progress = { work.checklistProgress },
                        modifier = Modifier.size(58.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.24f)
                    )
                }
            }
        }
        if (editing) {
            item(key = "editor") {
                WorkEditorSection(
                    work = work,
                    subjects = subjects,
                    viewModel = viewModel,
                    onSaved = {
                        editing = false
                        feedback = "Trabajo actualizado."
                    }
                )
            }
        }
        item(key = "checklist") {
            PersistentChecklistCard(
                work = work,
                onToggle = { item, checked -> viewModel.toggleChecklist(work.id, item.id, checked) }
            )
        }
        item(key = "apa") {
            val draft = remember(work.sources) { buildApaReferenceDraft(work.sources) }
            ApaReferenceGeneratorCard(
                referenceDraft = draft,
                onCopyClick = {
                    copyToClipboard(draft)
                    feedback = "Referencias APA copiadas."
                }
            )
        }
        item(key = "exportar") {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    Text(
                        "Llevarlo a tu editor",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "Copia la estructura, el checklist y las notas para pegarlas donde vayas a escribir.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp
                    )
                    Button(
                        shapes = UniStackButtonDefaults.shapes,
                        onClick = {
                            copyToClipboard(work.exportText(subjects))
                            feedback = "Trabajo copiado al portapapeles."
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("  Copiar el trabajo", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item(key = "entrega") {
            val delivered = work.isFinished
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    viewModel.setStatus(
                        work.id,
                        if (delivered) AcademicWorkStatus.DRAFT else AcademicWorkStatus.SUBMITTED
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (delivered) {
                        MaterialTheme.colorScheme.surfaceContainerHigh
                    } else {
                        LocalSectionColors.current.onTrack
                    },
                    contentColor = if (delivered) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        LocalSectionColors.current.onOnTrack
                    }
                )
            ) {
                if (!delivered) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text(
                    if (delivered) "  Volver a ponerlo en curso" else "  Marcar entregado",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        feedback?.let { message ->
            item(key = "aviso") {
                Text(
                    message,
                    color = LocalSectionColors.current.onTrack,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun WorkChip(text: String, tone: androidx.compose.ui.graphics.Color) {
    Surface(
        shape = CircleShape,
        color = tone.copy(alpha = 0.15f),
        contentColor = tone
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
        )
    }
}

/**
 * El formulario, solo cuando se pide.
 *
 * Estaba siempre desplegado en medio de la lista, con nueve campos abiertos aunque no fueras a
 * cambiar nada. Aquí se abre desde el lápiz de la cabecera y se cierra al guardar: editar es lo
 * que se hace de vez en cuando, no lo que se ve todo el rato.
 */
@Composable
private fun WorkEditorSection(
    work: com.unistack.app.feature_templates.domain.AcademicWork,
    subjects: List<com.unistack.app.feature_grades.domain.Subject>,
    viewModel: AcademicTemplatesViewModel,
    onSaved: () -> Unit
) {
    var title by rememberSaveable(work.id) { mutableStateOf(work.title) }
    var subjectId by rememberSaveable(work.id) { mutableStateOf(work.subjectId) }
    var dueDate by rememberSaveable(work.id) {
        mutableStateOf(
            work.dueDateMillis?.let { TaskDateUtils.formatInput(TaskDateUtils.fromMillis(it)) }.orEmpty()
        )
    }
    var status by rememberSaveable(work.id) { mutableStateOf(work.status) }
    var priority by rememberSaveable(work.id) { mutableStateOf(work.priority) }
    var thesis by rememberSaveable(work.id) { mutableStateOf(work.thesis) }
    var outline by rememberSaveable(work.id) { mutableStateOf(work.outline) }
    var sources by rememberSaveable(work.id) { mutableStateOf(work.sources) }
    var notes by rememberSaveable(work.id) { mutableStateOf(work.notes) }
    var error by rememberSaveable(work.id) { mutableStateOf<String?>(null) }

    WorkEditorCard(
        isEditing = true,
        title = title,
        onTitleChange = { title = it; error = null },
        subjectId = subjectId,
        subjects = subjects,
        onSubjectSelected = { subjectId = it },
        dueDate = dueDate,
        onDueDateChange = { dueDate = it; error = null },
        status = status,
        onStatusSelected = { status = it },
        priority = priority,
        onPrioritySelected = { priority = it },
        thesis = thesis,
        onThesisChange = { thesis = it },
        outline = outline,
        onOutlineChange = { outline = it },
        sources = sources,
        onSourcesChange = { sources = it },
        notes = notes,
        onNotesChange = { notes = it },
        error = error,
        onSave = {
            val saved = viewModel.updateWork(
                workId = work.id,
                templateId = work.templateId,
                title = title,
                subjectId = subjectId,
                dueDateInput = dueDate,
                status = status,
                priority = priority,
                thesis = thesis,
                outline = outline,
                sources = sources,
                notes = notes
            )
            if (saved) onSaved() else error = "Revisa el título y la fecha antes de guardar."
        }
    )
}
