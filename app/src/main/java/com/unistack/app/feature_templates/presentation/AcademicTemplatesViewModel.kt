package com.unistack.app.feature_templates.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.core.utils.TextValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_templates.domain.AcademicTemplateLibrary
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

@HiltViewModel
class AcademicTemplatesViewModel @Inject constructor(
    private val worksRepository: AcademicWorksRepository,
    gradesRepository: GradesRepository
) : ViewModel() {
    val works: StateFlow<List<AcademicWork>> = worksRepository.works
    val subjects: StateFlow<List<Subject>> = gradesRepository.subjects

    fun workById(workId: String?): AcademicWork? {
        if (workId == null) return null
        return works.value.firstOrNull { it.id == workId }
    }

    fun createWork(
        templateId: String,
        title: String,
        subjectId: String?,
        dueDateInput: String,
        priority: AcademicWorkPriority
    ): String? {
        val parsed = validateWorkInput(title, dueDateInput) ?: return null
        val template = AcademicTemplateLibrary.essayTemplates.firstOrNull { it.id == templateId }
            ?: AcademicTemplateLibrary.essayTemplates.first()
        val now = System.currentTimeMillis()
        val workId = "work-${UUID.randomUUID()}"
        worksRepository.addWork(
            AcademicWork(
                id = workId,
                templateId = template.id,
                title = TextValidators.normalizeText(title),
                subjectId = subjectId.takeIf { id -> subjects.value.any { it.id == id } },
                dueDateMillis = parsed.dueDateMillis,
                status = AcademicWorkStatus.DRAFT,
                priority = priority,
                completedChecklistIds = emptySet(),
                thesis = template.sections.firstOrNull { it.title.contains("tesis", ignoreCase = true) }?.prompt.orEmpty(),
                outline = template.sections.joinToString(separator = "\n") { "- ${it.title}: ${it.prompt}" },
                sources = "",
                notes = template.description,
                createdAt = now,
                updatedAt = now
            )
        )
        return workId
    }

    fun updateWork(
        workId: String,
        templateId: String,
        title: String,
        subjectId: String?,
        dueDateInput: String,
        status: AcademicWorkStatus,
        priority: AcademicWorkPriority,
        thesis: String,
        outline: String,
        sources: String,
        notes: String
    ): Boolean {
        val existing = workById(workId) ?: return false
        val parsed = validateWorkInput(title, dueDateInput) ?: return false
        worksRepository.updateWork(
            existing.copy(
                templateId = templateId,
                title = TextValidators.normalizeText(title),
                subjectId = subjectId.takeIf { id -> subjects.value.any { it.id == id } },
                dueDateMillis = parsed.dueDateMillis,
                status = status,
                priority = priority,
                thesis = thesis.trim().take(1_500),
                outline = outline.trim().take(2_500),
                sources = sources.trim().take(2_500),
                notes = notes.trim().take(2_500),
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun deleteWork(workId: String): Boolean {
        if (works.value.none { it.id == workId }) return false
        worksRepository.deleteWork(workId)
        return true
    }

    fun toggleChecklist(workId: String, checklistItemId: String, completed: Boolean): Boolean {
        if (works.value.none { it.id == workId }) return false
        worksRepository.setChecklistItem(workId, checklistItemId, completed)
        return true
    }

    fun setStatus(workId: String, status: AcademicWorkStatus): Boolean {
        if (works.value.none { it.id == workId }) return false
        worksRepository.setStatus(workId, status)
        return true
    }

    private fun validateWorkInput(title: String, dueDateInput: String): ParsedWorkInput? {
        if (!TextValidators.validateActivityName(title).isValid) return null
        val dueDateMillis = dueDateInput
            .takeIf { it.isNotBlank() }
            ?.let { TaskDateUtils.parseInput(it) ?: return null }
            ?.let(TaskDateUtils::toMillis)
        return ParsedWorkInput(dueDateMillis = dueDateMillis)
    }
}

private data class ParsedWorkInput(
    val dueDateMillis: Long?
)
