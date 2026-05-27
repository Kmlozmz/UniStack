package com.unistack.app.feature_sync.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_sync.domain.CloudBackupState
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class FirebaseCloudBackupRepository(
    private val context: Context,
    private val userRepository: UserRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val expensesRepository: ExpensesRepository,
    private val academicWorksRepository: AcademicWorksRepository
) : CloudBackupRepository {

    private val _state = MutableStateFlow(CloudBackupState())
    override val state: StateFlow<CloudBackupState> = _state

    override suspend fun backupNow(): Result<Unit> = runCatching {
        _state.update { it.copy(inProgress = true, message = null, errorMessage = null) }
        val userId = linkedUserId()
        ensureFirebaseConfigured()

        val now = System.currentTimeMillis()
        val payload = mapOf(
            "schemaVersion" to 1,
            "updatedAt" to now,
            "profile" to profileMap(),
            "subjects" to gradesRepository.subjects.value.map(::subjectMap),
            "tasks" to tasksRepository.tasks.value.map(::taskMap),
            "expenses" to expensesRepository.expenses.value.map(::expenseMap),
            "academicWorks" to academicWorksRepository.works.value.map(::academicWorkMap)
        )

        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("backups")
            .document("current")
            .set(payload)
            .await()

        _state.update {
            it.copy(
                inProgress = false,
                lastBackupAt = now,
                message = "Backup cloud actualizado.",
                errorMessage = null
            )
        }
    }.onFailure { throwable ->
        _state.update {
            it.copy(
                inProgress = false,
                message = null,
                errorMessage = throwable.message ?: "No se pudo actualizar el backup."
            )
        }
    }

    override suspend fun restoreLatest(): Result<Unit> = runCatching {
        _state.update { it.copy(inProgress = true, message = null, errorMessage = null) }
        val userId = linkedUserId()
        ensureFirebaseConfigured()

        val snapshot = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("backups")
            .document("current")
            .get()
            .await()

        check(snapshot.exists()) { "No hay backup cloud para restaurar." }
        val data = snapshot.data.orEmpty()

        parseSubjects(data["subjects"]).forEach { subject ->
            gradesRepository.addSubject(subject.copy(grades = emptyList()))
            subject.grades.forEach { grade ->
                gradesRepository.addGrade(subject.id, grade)
            }
        }
        parseTasks(data["tasks"]).forEach(tasksRepository::addTask)
        parseExpenses(data["expenses"]).forEach(expensesRepository::addExpense)
        parseAcademicWorks(data["academicWorks"]).forEach(academicWorksRepository::addWork)

        val now = System.currentTimeMillis()
        _state.update {
            it.copy(
                inProgress = false,
                lastRestoreAt = now,
                message = "Backup cloud restaurado en este dispositivo.",
                errorMessage = null
            )
        }
    }.onFailure { throwable ->
        _state.update {
            it.copy(
                inProgress = false,
                message = null,
                errorMessage = throwable.message ?: "No se pudo restaurar el backup."
            )
        }
    }

    private fun linkedUserId(): String {
        val user = userRepository.currentUser.value
        val providerUserId = user.providerUserId
        check(user.isLinked && !providerUserId.isNullOrBlank()) {
            "Conecta una cuenta de Google antes de usar backup cloud."
        }
        return providerUserId
    }

    private fun ensureFirebaseConfigured() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        check(FirebaseApp.getApps(context).isNotEmpty()) {
            "Falta app/google-services.json para inicializar Firebase."
        }
    }

    private fun profileMap(): Map<String, Any?> {
        val profile = userRepository.userProfile.value ?: return emptyMap()
        return mapOf(
            "preferredName" to profile.preferredName,
            "educationLevel" to profile.educationLevel.name,
            "careerOrProgram" to profile.careerOrProgram,
            "studyArea" to profile.studyArea?.name,
            "gradeLevel" to profile.gradeLevel,
            "gradingScale" to profile.gradingScale.name,
            "customGradeMax" to profile.customGradeMax,
            "passingGrade" to profile.passingGrade,
            "targetAverage" to profile.targetAverage,
            "enabledModules" to profile.enabledModules.map { it.name },
            "visualPreference" to profile.visualPreference.name,
            "taskRemindersEnabled" to profile.taskRemindersEnabled,
            "academicWorkRemindersEnabled" to profile.academicWorkRemindersEnabled,
            "overdueRemindersEnabled" to profile.overdueRemindersEnabled,
            "reminderLeadHours" to profile.reminderLeadHours,
            "weeklyBudget" to profile.weeklyBudget,
            "monthlyBudget" to profile.monthlyBudget,
            "expenseAlertThresholdPercent" to profile.expenseAlertThresholdPercent,
            "enabledExpenseCategories" to profile.enabledExpenseCategories.map { it.name },
            "gradeScenarios" to profile.gradeScenarios.map { scenario ->
                mapOf(
                    "id" to scenario.id,
                    "subjectId" to scenario.subjectId,
                    "subjectName" to scenario.subjectName,
                    "name" to scenario.name,
                    "targetAverage" to scenario.targetAverage,
                    "neededGrade" to scenario.neededGrade,
                    "createdAt" to scenario.createdAt
                )
            }
        )
    }

    private fun subjectMap(subject: Subject): Map<String, Any?> = mapOf(
        "id" to subject.id,
        "name" to subject.name,
        "targetAverage" to subject.targetAverage,
        "visualType" to subject.visualType.name,
        "grades" to subject.grades.map(::gradeMap)
    )

    private fun gradeMap(grade: GradeItem): Map<String, Any?> = mapOf(
        "id" to grade.id,
        "name" to grade.name,
        "value" to grade.value,
        "percentage" to grade.percentage,
        "type" to grade.type.name,
        "periodId" to grade.periodId
    )

    private fun taskMap(task: StudentTask): Map<String, Any?> = mapOf(
        "id" to task.id,
        "title" to task.title,
        "description" to task.description,
        "subjectId" to task.subjectId,
        "type" to task.type.name,
        "dueDateMillis" to task.dueDateMillis,
        "difficulty" to task.difficulty.name,
        "estimatedMinutes" to task.estimatedMinutes,
        "completed" to task.completed,
        "createdAt" to task.createdAt,
        "updatedAt" to task.updatedAt
    )

    private fun expenseMap(expense: Expense): Map<String, Any?> = mapOf(
        "id" to expense.id,
        "category" to expense.category.name,
        "amount" to expense.amount,
        "dateMillis" to expense.dateMillis,
        "createdAt" to expense.createdAt,
        "updatedAt" to expense.updatedAt
    )

    private fun academicWorkMap(work: AcademicWork): Map<String, Any?> = mapOf(
        "id" to work.id,
        "templateId" to work.templateId,
        "title" to work.title,
        "subjectId" to work.subjectId,
        "dueDateMillis" to work.dueDateMillis,
        "status" to work.status.name,
        "priority" to work.priority.name,
        "completedChecklistIds" to work.completedChecklistIds.toList(),
        "thesis" to work.thesis,
        "outline" to work.outline,
        "sources" to work.sources,
        "notes" to work.notes,
        "createdAt" to work.createdAt,
        "updatedAt" to work.updatedAt
    )

    private fun parseSubjects(value: Any?): List<Subject> {
        return asMapList(value).mapNotNull { map ->
            val id = map.string("id") ?: return@mapNotNull null
            val name = map.string("name") ?: return@mapNotNull null
            val targetAverage = map.double("targetAverage") ?: return@mapNotNull null
            val visualType = map.string("visualType")
                ?.let { runCatching { SubjectVisualType.valueOf(it) }.getOrNull() }
                ?: SubjectVisualType.TEAL
            val grades = parseGrades(map["grades"])
            Subject(id = id, name = name, targetAverage = targetAverage, visualType = visualType, grades = grades)
        }
    }

    private fun parseGrades(value: Any?): List<GradeItem> {
        return asMapList(value).mapNotNull { map ->
            GradeItem(
                id = map.string("id") ?: return@mapNotNull null,
                name = map.string("name") ?: return@mapNotNull null,
                value = map.double("value") ?: return@mapNotNull null,
                percentage = map.double("percentage") ?: return@mapNotNull null,
                type = map.string("type")
                    ?.let { runCatching { GradeType.valueOf(it) }.getOrNull() }
                    ?: GradeType.WORKSHOP,
                periodId = map.string("periodId") ?: "period-1"
            )
        }
    }

    private fun parseTasks(value: Any?): List<StudentTask> {
        return asMapList(value).mapNotNull { map ->
            val difficulty = map.string("difficulty")
                ?.let { runCatching { TaskDifficulty.valueOf(it) }.getOrNull() }
                ?: TaskDifficulty.MEDIUM
            StudentTask(
                id = map.string("id") ?: return@mapNotNull null,
                title = map.string("title") ?: return@mapNotNull null,
                description = map.string("description") ?: "",
                subjectId = map.string("subjectId"),
                type = map.string("type")
                    ?.let { runCatching { TaskType.valueOf(it) }.getOrNull() }
                    ?: TaskType.WORKSHOP,
                dueDateMillis = map.long("dueDateMillis") ?: return@mapNotNull null,
                difficulty = difficulty,
                estimatedMinutes = map.int("estimatedMinutes") ?: return@mapNotNull null,
                completed = map.boolean("completed") ?: false,
                createdAt = map.long("createdAt") ?: System.currentTimeMillis(),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }

    private fun parseExpenses(value: Any?): List<Expense> {
        return asMapList(value).mapNotNull { map ->
            val category = map.string("category")
                ?.let { runCatching { ExpenseCategory.valueOf(it) }.getOrNull() }
                ?: ExpenseCategory.OTHER
            Expense(
                id = map.string("id") ?: return@mapNotNull null,
                category = category,
                amount = map.int("amount") ?: return@mapNotNull null,
                dateMillis = map.long("dateMillis") ?: return@mapNotNull null,
                createdAt = map.long("createdAt") ?: System.currentTimeMillis(),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }

    private fun parseAcademicWorks(value: Any?): List<AcademicWork> {
        return asMapList(value).mapNotNull { map ->
            val status = map.string("status")
                ?.let { runCatching { AcademicWorkStatus.valueOf(it) }.getOrNull() }
                ?: AcademicWorkStatus.DRAFT
            val priority = map.string("priority")
                ?.let { runCatching { AcademicWorkPriority.valueOf(it) }.getOrNull() }
                ?: AcademicWorkPriority.MEDIUM
            AcademicWork(
                id = map.string("id") ?: return@mapNotNull null,
                templateId = map.string("templateId") ?: return@mapNotNull null,
                title = map.string("title") ?: return@mapNotNull null,
                subjectId = map.string("subjectId"),
                dueDateMillis = map.long("dueDateMillis"),
                status = status,
                priority = priority,
                completedChecklistIds = (map["completedChecklistIds"] as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toSet()
                    .orEmpty(),
                thesis = map.string("thesis").orEmpty(),
                outline = map.string("outline").orEmpty(),
                sources = map.string("sources").orEmpty(),
                notes = map.string("notes").orEmpty(),
                createdAt = map.long("createdAt") ?: System.currentTimeMillis(),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }

    private fun asMapList(value: Any?): List<Map<String, Any?>> {
        return (value as? List<*>)
            ?.mapNotNull { item ->
                @Suppress("UNCHECKED_CAST")
                item as? Map<String, Any?>
            }
            .orEmpty()
    }

    private fun Map<String, Any?>.string(key: String): String? = this[key] as? String
    private fun Map<String, Any?>.boolean(key: String): Boolean? = this[key] as? Boolean
    private fun Map<String, Any?>.double(key: String): Double? = (this[key] as? Number)?.toDouble()
    private fun Map<String, Any?>.long(key: String): Long? = (this[key] as? Number)?.toLong()
    private fun Map<String, Any?>.int(key: String): Int? = (this[key] as? Number)?.toInt()
}
