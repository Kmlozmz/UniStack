package com.unistack.app.feature_tasks.domain

data class StudentTask(
    val id: String,
    val title: String,
    val description: String,
    val subjectId: String?,
    val type: TaskType,
    val dueDateMillis: Long,
    val difficulty: TaskDifficulty,
    val estimatedMinutes: Int,
    val completed: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val cutId: String? = null,
    val gradingStatus: TaskGradingStatus = TaskGradingStatus.UNDECIDED,
    val linkedGradeId: String? = null,
    val completedAt: Long? = null,
    val subtasks: List<TaskSubtask> = emptyList()
)

data class TaskSubtask(
    val id: String,
    val taskId: String,
    val title: String,
    val isCompleted: Boolean,
    val position: Int
)

enum class TaskGradingStatus {
    UNDECIDED,
    NOT_GRADED,
    AWAITING_GRADE,
    GRADED
}

enum class TaskDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class TaskType {
    WORKSHOP,
    EXAM,
    ESSAY,
    PRESENTATION,
    RESEARCH,
    TEST,
    PRACTICE,
    PROJECT,
    READING,
    OTHER
}

/**
 * Determina si el tipo de tarea habitualmente se evalúa / califica en la universidad.
 * Taller, examen, ensayo, exposición, investigación, quiz y proyecto se califican.
 * Lectura, práctica y otros no preguntan por nota.
 */
fun TaskType.isGradable(): Boolean = when (this) {
    TaskType.WORKSHOP,
    TaskType.EXAM,
    TaskType.ESSAY,
    TaskType.PRESENTATION,
    TaskType.RESEARCH,
    TaskType.TEST,
    TaskType.PROJECT -> true
    TaskType.PRACTICE,
    TaskType.READING,
    TaskType.OTHER -> false
}
