@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_templates.presentation

import androidx.compose.material.icons.rounded.Description
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.LargeTitleScaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.rounded.Add
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import com.unistack.app.core.design.components.EvaluationBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniEmptyStateCard
import com.unistack.app.core.design.components.UniFilterChipRow
import com.unistack.app.core.design.components.UniFilterOption
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_templates.domain.AcademicTemplateLibrary
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_templates.domain.ChecklistItem
import com.unistack.app.feature_templates.domain.EssayTemplate
import com.unistack.app.feature_templates.domain.buildApaReferenceDraft
import com.unistack.app.feature_templates.domain.exportText

import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
/**
 * Trabajos: la lista, y nada más.
 *
 * Venía todo apilado en una sola pantalla —la lista, el catálogo de plantillas, el detalle de
 * la plantilla elegida, el formulario de crear, el checklist del trabajo abierto, el botón de
 * exportar y el generador de referencias—, casi mil líneas de scroll donde la mitad hablaba de
 * un trabajo concreto y la otra mitad de uno que aún no existía.
 *
 * Se parte en tres: aquí qué tienes y cómo vas; en una hoja, de qué tipo va a ser el nuevo; y en
 * su propia pantalla, cada trabajo con lo suyo.
 */
@Composable
fun AcademicTemplatesScreen(
    onBackClick: () -> Unit,
    onWorkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AcademicTemplatesViewModel = hiltViewModel()
) {
    val works by viewModel.works.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    var showFinished by rememberSaveable { mutableStateOf(false) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var workIdPendingDelete by rememberSaveable { mutableStateOf<String?>(null) }

    val open = works.filter { !it.isFinished }
    val finished = works.filter { it.isFinished }
    val visible = if (showFinished) finished else open

    Box(modifier = modifier.fillMaxSize()) {
        LargeTitleScaffold(
            title = "Trabajos",
            subtitle = "Plantilla, checklist y entrega.",
            onBackClick = onBackClick,
            bottomPadding = scrollBottomRoom
        ) {
            item(key = "filtro") {
                UniSegmentedControl(
                    selected = showFinished,
                    options = listOf(
                        UniSegmentedOption(
                            value = false,
                            label = "En curso",
                            icon = Icons.Rounded.Edit,
                            badge = open.size.takeIf { it > 0 }
                        ),
                        UniSegmentedOption(
                            value = true,
                            label = "Entregados",
                            icon = Icons.Rounded.CheckCircle,
                            badge = finished.size.takeIf { it > 0 }
                        )
                    ),
                    onSelected = { showFinished = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (visible.isEmpty()) {
                item(key = "vacio") {
                    UniEmptyStateCard(
                        title = if (showFinished) {
                            "Todavía no has entregado ninguno."
                        } else {
                            "No tienes trabajos en curso."
                        },
                        body = if (showFinished) {
                            "Los que marques como entregados se guardan aquí, con su checklist tal " +
                                "como lo dejaste."
                        } else {
                            "Empieza uno desde una plantilla: trae los apartados y el checklist puestos."
                        },
                        icon = Icons.AutoMirrored.Rounded.Assignment,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        iconColor = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                items(visible, key = { it.id }) { work ->
                    WorkCard(
                        work = work,
                        subjectName = work.subjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name },
                        onSelect = { onWorkClick(work.id) },
                        onDelete = { workIdPendingDelete = work.id }
                    )
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { creating = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text("Nuevo", fontWeight = FontWeight.Bold) }
        )
    }

    if (creating) {
        NewWorkSheet(
            onDismiss = { creating = false },
            onCreate = { template ->
                creating = false
                val id = viewModel.createWork(
                    templateId = template.id,
                    title = template.title,
                    subjectId = null,
                    dueDateInput = "",
                    priority = AcademicWorkPriority.MEDIUM
                )
                if (id != null) onWorkClick(id)
            }
        )
    }

    workIdPendingDelete?.let { pendingId ->
        val name = works.firstOrNull { it.id == pendingId }?.title.orEmpty()
        AlertDialog(
            onDismissRequest = { workIdPendingDelete = null },
            title = { Text("¿Eliminar trabajo?") },
            text = { Text("Se borra «" + name + "» con su checklist y sus notas. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteWork(pendingId)
                    workIdPendingDelete = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { workIdPendingDelete = null }) { Text("Cancelar") }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

/**
 * ¿De qué tipo?, en una hoja.
 *
 * El catálogo de plantillas estaba siempre abierto en medio de la pantalla, aunque no fueras a
 * crear nada: sitio permanente para una decisión que se toma una vez por trabajo. Aquí aparece
 * cuando hace falta y trae lo único que se necesita para elegir: qué apartados da cada una.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewWorkSheet(
    onDismiss: () -> Unit,
    onCreate: (EssayTemplate) -> Unit
) {
    val templates = AcademicTemplateLibrary.essayTemplates
    var chosenId by rememberSaveable { mutableStateOf(templates.first().id) }
    val chosen = templates.firstOrNull { it.id == chosenId } ?: templates.first()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "¿De qué tipo?",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Cada plantilla trae sus apartados y su checklist. Podrás cambiarlo todo después.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            templates.forEach { template ->
                TemplateChoiceCard(
                    template = template,
                    selected = template.id == chosenId,
                    onClick = { chosenId = template.id }
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = { onCreate(chosen) },
                    modifier = Modifier.weight(1.4f)
                ) {
                    Text("Crear", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TemplateChoiceCard(
    template: EssayTemplate,
    selected: Boolean,
    onClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLowest
        },
        shape = MaterialTheme.shapes.extraLarge,
        borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        borderWidth = if (selected) 2.dp else 0.dp,
        onClick = onClick,
        contentPadding = PaddingValues(15.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            IconBadge(
                icon = Icons.Rounded.Description,
                background = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                tint = if (selected) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Column(
                modifier = Modifier.padding(start = 13.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    template.title,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    template.description,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodySmall
                )
                // Los apartados de verdad, no una promesa: es lo que distingue una plantilla de
                // otra, y lo único que hace falta saber para elegir.
                Text(
                    template.sections.joinToString(" · ") { it.title.uppercase() },
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
private fun WorkCard(
    work: AcademicWork,
    subjectName: String?,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth().bounceClick(onSelect),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge,
        contentPadding = PaddingValues(15.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        work.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    // Materia y fecha: lo que sitúa el trabajo. La prioridad y el estado se
                    // dicen con el color de la etiqueta y de la barra, sin repetirlos en texto.
                    Text(
                        listOfNotNull(
                            subjectName ?: "General",
                            work.dueDateMillis?.let(TaskDateUtils::dueText)
                        ).joinToString(" · "),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = work.priority.color().copy(alpha = 0.18f),
                    contentColor = work.priority.color()
                ) {
                    Text(
                        work.status.label().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(IconButtonDefaults.smallContainerSize())) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Eliminar trabajo",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                EvaluationBar(
                    fraction = work.checklistProgress.toDouble(),
                    modifier = Modifier.weight(1f),
                    color = work.status.color(),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                Spacer(Modifier.width(11.dp))
                Text(
                    work.completedChecklistIds.size.toString() + "/" +
                        AcademicTemplateLibrary.checklist.size,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
internal fun WorkEditorCard(
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
                shapes = UniStackButtonDefaults.shapes,
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
            ) {
                Text(if (isEditing) "Guardar trabajo" else "Crear trabajo")
            }
        }
    }
}

@Composable
internal fun PersistentChecklistCard(
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
internal fun ApaReferenceGeneratorCard(
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
                shapes = UniStackButtonDefaults.shapes,
                onClick = onCopyClick,
                colors = ButtonDefaults.buttonColors(containerColor = LocalSectionColors.current.onTrack),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                    .testTag("copy-apa-references")
            ) {
                Text("Copiar referencias")
            }
        }
    }
}

internal fun AcademicWork.exportText(subjects: List<Subject>): String {
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

internal fun AcademicWorkStatus.label(): String {
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
internal fun AcademicWorkStatus.color(): Color {
    return when (this) {
        AcademicWorkStatus.IDEA -> LocalSectionColors.current.schedule
        AcademicWorkStatus.DRAFT -> MaterialTheme.colorScheme.primary
        AcademicWorkStatus.REVIEW -> LocalSectionColors.current.atRisk
        AcademicWorkStatus.READY -> LocalSectionColors.current.onTrack
        AcademicWorkStatus.SUBMITTED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

internal fun AcademicWorkPriority.label(): String {
    return when (this) {
        AcademicWorkPriority.LOW -> "Baja"
        AcademicWorkPriority.MEDIUM -> "Media"
        AcademicWorkPriority.HIGH -> "Alta"
    }
}

@Composable
@ReadOnlyComposable
internal fun AcademicWorkPriority.color(): Color {
    return when (this) {
        AcademicWorkPriority.LOW -> LocalSectionColors.current.onTrack
        AcademicWorkPriority.MEDIUM -> LocalSectionColors.current.atRisk
        AcademicWorkPriority.HIGH -> MaterialTheme.colorScheme.error
    }
}

@Composable
internal fun IconBadge(
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
