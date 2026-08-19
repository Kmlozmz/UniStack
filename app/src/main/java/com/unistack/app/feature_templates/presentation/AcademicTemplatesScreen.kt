package com.unistack.app.feature_templates.presentation

import android.content.ClipData
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.components.UniEmptyStateCard
import com.unistack.app.core.design.components.UniFilterChipRow
import com.unistack.app.core.design.components.UniFilterOption
import com.unistack.app.core.design.components.UniScreenHeader
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_templates.domain.AcademicTemplateLibrary
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_templates.domain.ApaTip
import com.unistack.app.feature_templates.domain.ChecklistItem
import com.unistack.app.feature_templates.domain.EssayTemplate
import com.unistack.app.feature_templates.domain.buildApaReferenceDraft
import com.unistack.app.feature_templates.domain.exportText
import kotlinx.coroutines.launch

import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.runtime.ReadOnlyComposable
@Composable
fun AcademicTemplatesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AcademicTemplatesViewModel = hiltViewModel()
) {

    val works by viewModel.works.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val templates = AcademicTemplateLibrary.essayTemplates
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    fun copyToClipboard(text: String) {
        coroutineScope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("UniStack", text)))
        }
    }

    var selectedTemplateId by rememberSaveable { mutableStateOf(templates.first().id) }
    var selectedWorkId by rememberSaveable { mutableStateOf<String?>(null) }
    var workIdPendingDelete by rememberSaveable { mutableStateOf<String?>(null) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedTemplate = templates.firstOrNull { it.id == selectedTemplateId } ?: templates.first()
    val selectedWork = works.firstOrNull { it.id == selectedWorkId }

    var title by rememberSaveable { mutableStateOf("") }
    var subjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var dueDate by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(AcademicWorkStatus.DRAFT) }
    var priority by rememberSaveable { mutableStateOf(AcademicWorkPriority.MEDIUM) }
    var thesis by rememberSaveable { mutableStateOf("") }
    var outline by rememberSaveable { mutableStateOf("") }
    var sources by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedWork?.id, selectedTemplateId) {
        if (selectedWork != null) {
            selectedTemplateId = selectedWork.templateId
            title = selectedWork.title
            subjectId = selectedWork.subjectId
            dueDate = selectedWork.dueDateMillis?.let { TaskDateUtils.formatInput(TaskDateUtils.fromMillis(it)) }.orEmpty()
            status = selectedWork.status
            priority = selectedWork.priority
            thesis = selectedWork.thesis
            outline = selectedWork.outline
            sources = selectedWork.sources
            notes = selectedWork.notes
        } else {
            title = selectedTemplate.title
            subjectId = null
            dueDate = ""
            status = AcademicWorkStatus.DRAFT
            priority = AcademicWorkPriority.MEDIUM
            thesis = ""
            outline = selectedTemplate.sections.joinToString(separator = "\n") { "- ${it.title}: ${it.prompt}" }
            sources = ""
            notes = selectedTemplate.description
        }
        error = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = scrollBottomRoom),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                }
            }
            item {
                UniScreenHeader(
                    title = "Trabajos académicos",
                    subtitle = "Convierte plantillas en trabajos reales con checklist, materia, fecha y progreso."
                )
            }
            item {
                HeaderCard(
                    workCount = works.size,
                    activeCount = works.count { !it.isFinished }
                )
            }
            item {
                WorkListSection(
                    works = works,
                    subjects = subjects,
                    selectedWorkId = selectedWorkId,
                    onSelectWork = { work ->
                        selectedWorkId = work.id
                        feedback = null
                    },
                    onNewWork = {
                        selectedWorkId = null
                        feedback = null
                    },
                    onMarkSubmitted = { work ->
                        viewModel.setStatus(work.id, AcademicWorkStatus.SUBMITTED)
                    },
                    onDeleteWork = { workIdPendingDelete = it.id }
                )
            }
            item {
                TemplatePicker(
                    templates = templates,
                    selectedTemplateId = selectedTemplateId,
                    enabled = selectedWork == null,
                    onTemplateSelected = {
                        selectedTemplateId = it
                        selectedWorkId = null
                    }
                )
            }
            item {
                WorkEditorCard(
                    isEditing = selectedWork != null,
                    title = title,
                    onTitleChange = {
                        title = it.take(40)
                        error = null
                    },
                    subjectId = subjectId,
                    subjects = subjects,
                    onSubjectSelected = {
                        subjectId = it
                        error = null
                    },
                    dueDate = dueDate,
                    onDueDateChange = {
                        dueDate = it.take(10)
                        error = null
                    },
                    status = status,
                    onStatusSelected = { status = it },
                    priority = priority,
                    onPrioritySelected = { priority = it },
                    thesis = thesis,
                    onThesisChange = { thesis = it.take(1_500) },
                    outline = outline,
                    onOutlineChange = { outline = it.take(2_500) },
                    sources = sources,
                    onSourcesChange = { sources = it.take(2_500) },
                    notes = notes,
                    onNotesChange = { notes = it.take(2_500) },
                    error = error,
                    onSave = {
                        val editingId = selectedWork?.id
                        if (editingId == null) {
                            val createdId = viewModel.createWork(
                                templateId = selectedTemplateId,
                                title = title,
                                subjectId = subjectId,
                                dueDateInput = dueDate,
                                priority = priority
                            )
                            if (createdId == null) {
                                error = "Revisa título y fecha antes de crear el trabajo."
                            } else {
                                selectedWorkId = createdId
                                feedback = "Trabajo creado."
                            }
                        } else {
                            val saved = viewModel.updateWork(
                                workId = editingId,
                                templateId = selectedTemplateId,
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
                            if (saved) {
                                feedback = "Trabajo actualizado."
                            } else {
                                error = "Revisa título y fecha antes de guardar."
                            }
                        }
                    }
                )
            }
            if (selectedWork != null) {
                item {
                    PersistentChecklistCard(
                        work = selectedWork,
                        onToggle = { item, checked ->
                            viewModel.toggleChecklist(selectedWork.id, item.id, checked)
                        }
                    )
                }
                item {
                    CopyTemplateCard(
                        title = "Exportar trabajo",
                        body = "Copia estructura, checklist y notas persistidas para llevarlas a tu editor.",
                        onCopyClick = {
                            copyToClipboard(selectedWork.exportText(subjects))
                            feedback = "Trabajo copiado al portapapeles."
                        }
                    )
                }
            } else {
                item {
                    TemplateDetailCard(template = selectedTemplate)
                }
                item {
                    CopyTemplateCard(
                        title = "Copiar plantilla",
                        body = "Copia la plantilla base y úsala como borrador rápido.",
                        onCopyClick = {
                            copyToClipboard(selectedTemplate.exportText(emptyList()))
                            feedback = "Plantilla copiada al portapapeles."
                        }
                    )
                }
            }
            item {
                Text(
                    text = "APA básico",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            item {
                val apaDraft = selectedWork?.let { work ->
                    buildApaReferenceDraft(work.sources)
                } ?: buildApaReferenceDraft(sources)
                ApaReferenceGeneratorCard(
                    referenceDraft = apaDraft,
                    onCopyClick = {
                        copyToClipboard(apaDraft)
                        feedback = "Referencias APA copiadas."
                    }
                )
            }
            items(AcademicTemplateLibrary.apaTips) { tip ->
                ApaTipCard(tip = tip)
            }
            feedback?.let { message ->
                item {
                    Text(message, color = LocalSectionColors.current.onTrack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    workIdPendingDelete?.let { workId ->
        UniConfirmDeleteDialog(
            title = "¿Eliminar trabajo?",
            body = "Se eliminarán su checklist, notas y progreso guardado.",
            onConfirm = {
                viewModel.deleteWork(workId)
                if (selectedWorkId == workId) selectedWorkId = null
                workIdPendingDelete = null
            },
            onDismiss = { workIdPendingDelete = null }
        )
    }
}

@Composable
private fun HeaderCard(workCount: Int, activeCount: Int) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 6.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(Icons.AutoMirrored.Rounded.Assignment, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            Column(
                modifier = Modifier.padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$activeCount activos",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 22.sp,
                    lineHeight = 25.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (workCount == 1) "1 trabajo guardado" else "$workCount trabajos guardados",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun WorkListSection(
    works: List<AcademicWork>,
    subjects: List<Subject>,
    selectedWorkId: String?,
    onSelectWork: (AcademicWork) -> Unit,
    onNewWork: () -> Unit,
    onMarkSubmitted: (AcademicWork) -> Unit,
    onDeleteWork: (AcademicWork) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Trabajos guardados",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onNewWork,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text("Nuevo", fontWeight = FontWeight.Bold)
            }
        }
        if (works.isEmpty()) {
            UniEmptyStateCard(
                title = "Aún no tienes trabajos guardados.",
                body = "Crea uno desde una plantilla para persistir checklist, fecha, materia y progreso.",
                icon = Icons.AutoMirrored.Rounded.Assignment,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                iconColor = MaterialTheme.colorScheme.primary
            )
        } else {
            works.forEach { work ->
                WorkCard(
                    work = work,
                    subjectName = work.subjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name },
                    selected = selectedWorkId == work.id,
                    onSelect = { onSelectWork(work) },
                    onMarkSubmitted = { onMarkSubmitted(work) },
                    onDelete = { onDeleteWork(work) }
                )
            }
        }
    }
}

@Composable
private fun WorkCard(
    work: AcademicWork,
    subjectName: String?,
    selected: Boolean,
    onSelect: () -> Unit,
    onMarkSubmitted: () -> Unit,
    onDelete: () -> Unit
) {
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onSelect),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        tonalElevation = if (selected) 5.dp else 2.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(
                    icon = Icons.Rounded.Edit,
                    background = work.priority.color().copy(alpha = 0.18f),
                    tint = work.priority.color()
                )
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(work.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                    Text(
                        listOfNotNull(
                            subjectName ?: "General",
                            work.dueDateMillis?.let(TaskDateUtils::dueText),
                            work.status.label(),
                            work.priority.label()
                        ).joinToString(" · "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Eliminar trabajo", tint = MaterialTheme.colorScheme.error)
                }
            }
            LinearProgressIndicator(
                progress = { work.checklistProgress },
                modifier = Modifier.fillMaxWidth(),
                color = work.status.color(),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
            if (!work.isFinished) {
                Button(
                    onClick = onMarkSubmitted,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = LocalSectionColors.current.onTrack),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Marcar entregado")
                }
            }
        }
    }
}

@Composable
private fun TemplatePicker(
    templates: List<EssayTemplate>,
    selectedTemplateId: String,
    enabled: Boolean,
    onTemplateSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Plantillas", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        templates.forEach { template ->
            TemplateOption(
                template = template,
                selected = selectedTemplateId == template.id,
                enabled = enabled,
                onClick = { onTemplateSelected(template.id) }
            )
        }
    }
}

@Composable
private fun TemplateOption(
    template: EssayTemplate,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.bounceClick(onClick) else Modifier),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        tonalElevation = if (selected) 5.dp else 2.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(
                icon = if (selected) Icons.Rounded.Star else Icons.AutoMirrored.Rounded.MenuBook,
                background = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(template.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                Text(template.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun WorkEditorCard(
    isEditing: Boolean,
    title: String,
    onTitleChange: (String) -> Unit,
    subjectId: String?,
    subjects: List<Subject>,
    onSubjectSelected: (String?) -> Unit,
    dueDate: String,
    onDueDateChange: (String) -> Unit,
    status: AcademicWorkStatus,
    onStatusSelected: (AcademicWorkStatus) -> Unit,
    priority: AcademicWorkPriority,
    onPrioritySelected: (AcademicWorkPriority) -> Unit,
    thesis: String,
    onThesisChange: (String) -> Unit,
    outline: String,
    onOutlineChange: (String) -> Unit,
    sources: String,
    onSourcesChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    error: String?,
    onSave: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = if (isEditing) "Editar trabajo" else "Crear trabajo desde plantilla",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
            OutlinedTextField(
                value = dueDate,
                onValueChange = onDueDateChange,
                label = { Text("Fecha de entrega") },
                placeholder = { Text("YYYY-MM-DD") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                isError = dueDate.isNotBlank() && TaskDateUtils.parseInput(dueDate) == null
            )
            Text("Materia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
            UniFilterChipRow(
                options = listOf(UniFilterOption<String?>(null, "General")) +
                    subjects.map { UniFilterOption<String?>(it.id, it.name) },
                selected = subjectId,
                onSelected = onSubjectSelected
            )
            Text("Estado", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
            UniFilterChipRow(
                options = AcademicWorkStatus.entries.map { UniFilterOption(it, it.label()) },
                selected = status,
                onSelected = onStatusSelected
            )
            Text("Prioridad", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
            UniFilterChipRow(
                options = AcademicWorkPriority.entries.map { UniFilterOption(it, it.label()) },
                selected = priority,
                onSelected = onPrioritySelected
            )
            OutlinedTextField(
                value = thesis,
                onValueChange = onThesisChange,
                label = { Text("Tesis u objetivo") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
            OutlinedTextField(
                value = outline,
                onValueChange = onOutlineChange,
                label = { Text("Esquema") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
            OutlinedTextField(
                value = sources,
                onValueChange = onSourcesChange,
                label = { Text("Fuentes") },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("academic-work-sources"),
                shape = MaterialTheme.shapes.large
            )
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notas") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onSave,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Guardar trabajo" else "Crear trabajo")
            }
        }
    }
}

@Composable
private fun PersistentChecklistCard(
    work: AcademicWork,
    onToggle: (ChecklistItem, Boolean) -> Unit
) {
    val items = AcademicTemplateLibrary.checklist
    val completedCount = work.completedChecklistIds.size.coerceAtMost(items.size)

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.CheckCircle, LocalSectionColors.current.onTrackContainer, LocalSectionColors.current.onTrack)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Checklist persistente", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                    Text("$completedCount de ${items.size} pasos listos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            items.forEach { item ->
                ChecklistRow(
                    item = item,
                    checked = item.id in work.completedChecklistIds,
                    onToggle = { onToggle(item, item.id !in work.completedChecklistIds) }
                )
            }
        }
    }
}

@Composable
private fun ChecklistRow(
    item: ChecklistItem,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(if (checked) LocalSectionColors.current.onTrackContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
            .bounceClick(onToggle)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f)) {
            Text(item.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(item.detail, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun TemplateDetailCard(template: EssayTemplate) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(template.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            template.sections.forEachIndexed { index, section ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(
                        modifier = Modifier.padding(start = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(section.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                        Text(section.prompt, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApaReferenceGeneratorCard(
    referenceDraft: String,
    onCopyClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.AutoAwesome, LocalSectionColors.current.onTrackContainer, LocalSectionColors.current.onTrack)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text("Generador de referencias", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                    Text("Autor | Año | Título | Medio", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Text(
                text = referenceDraft,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Button(
                onClick = onCopyClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = LocalSectionColors.current.onTrack),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy-apa-references")
            ) {
                Text("Copiar referencias")
            }
        }
    }
}

@Composable
private fun ApaTipCard(tip: ApaTip) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
        tonalElevation = 3.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(Icons.Rounded.Check, LocalSectionColors.current.scheduleContainer, LocalSectionColors.current.schedule)
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(tip.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                Text(tip.description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
    }
}

@Composable
private fun CopyTemplateCard(
    title: String,
    body: String,
    onCopyClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Rounded.AutoAwesome, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                    Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Button(
                onClick = onCopyClick,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("copy-work-to-clipboard")
            ) {
                Text("Copiar al portapapeles")
            }
        }
    }
}

private fun AcademicWork.exportText(subjects: List<Subject>): String {
    val subjectName = subjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name } ?: "General"
    val checklist = AcademicTemplateLibrary.checklist.joinToString(separator = "\n") { item ->
        val mark = if (item.id in completedChecklistIds) "[x]" else "[ ]"
        "$mark ${item.title}: ${item.detail}"
    }
    val due = dueDateMillis?.let { TaskDateUtils.fromMillis(it).toString() } ?: "Sin fecha"
    val apaReferences = buildApaReferenceDraft(sources)
    return """
        $title
        Materia: $subjectName
        Fecha: $due
        Estado: ${status.label()}
        Prioridad: ${priority.label()}

        Checklist
        $checklist

        Tesis u objetivo
        $thesis

        Esquema
        $outline

        Fuentes
        $sources

        Referencias APA
        $apaReferences

        Notas
        $notes
    """.trimIndent()
}

private fun AcademicWorkStatus.label(): String {
    return when (this) {
        AcademicWorkStatus.IDEA -> "Idea"
        AcademicWorkStatus.DRAFT -> "Borrador"
        AcademicWorkStatus.REVIEW -> "Revisión"
        AcademicWorkStatus.READY -> "Listo"
        AcademicWorkStatus.SUBMITTED -> "Entregado"
    }
}

@Composable
@ReadOnlyComposable
private fun AcademicWorkStatus.color(): Color {
    return when (this) {
        AcademicWorkStatus.IDEA -> LocalSectionColors.current.schedule
        AcademicWorkStatus.DRAFT -> MaterialTheme.colorScheme.primary
        AcademicWorkStatus.REVIEW -> LocalSectionColors.current.atRisk
        AcademicWorkStatus.READY -> LocalSectionColors.current.onTrack
        AcademicWorkStatus.SUBMITTED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun AcademicWorkPriority.label(): String {
    return when (this) {
        AcademicWorkPriority.LOW -> "Baja"
        AcademicWorkPriority.MEDIUM -> "Media"
        AcademicWorkPriority.HIGH -> "Alta"
    }
}

@Composable
@ReadOnlyComposable
private fun AcademicWorkPriority.color(): Color {
    return when (this) {
        AcademicWorkPriority.LOW -> LocalSectionColors.current.onTrack
        AcademicWorkPriority.MEDIUM -> LocalSectionColors.current.atRisk
        AcademicWorkPriority.HIGH -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun IconBadge(
    icon: ImageVector,
    background: Color,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
    }
}
