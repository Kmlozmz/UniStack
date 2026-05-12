package com.unistack.app.feature_sync.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_sync.domain.CloudBackupState
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_user.domain.AppUser
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
    private val expensesRepository: ExpensesRepository
) : CloudBackupRepository {

    private val _state = MutableStateFlow(CloudBackupState())
    override val state: StateFlow<CloudBackupState> = _state

    override suspend fun backupNow(): Result<Unit> = runCatching {
        _state.update { it.copy(inProgress = true, message = null, errorMessage = null) }
        val user = linkedUser()
        ensureFirebaseConfigured()

        val now = System.currentTimeMillis()
        val payload = mapOf(
            "schemaVersion" to 1,
            "updatedAt" to now,
            "profile" to profileMap(),
            "subjects" to gradesRepository.subjects.value.map(::subjectMap),
            "tasks" to tasksRepository.tasks.value.map(::taskMap),
            "expenses" to expensesRepository.expenses.value.map(::expenseMap)
        )

        Firebase.firestore
            .collection("users")
            .document(user.providerUserId!!)
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
        val user = linkedUser()
        ensureFirebaseConfigured()

        val snapshot = Firebase.firestore
            .collection("users")
            .document(user.providerUserId!!)
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

    private fun linkedUser(): AppUser {
        val user = userRepository.currentUser.value
        check(user.isLinked && !user.providerUserId.isNullOrBlank()) {
            "Conecta una cuenta de Google antes de usar backup cloud."
        }
        return user
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
            "passingGrade" to profile.passingGrade,
            "targetAverage" to profile.targetAverage,
            "enabledModules" to profile.enabledModules.map { it.name },
            "visualPreference" to profile.visualPreference.name
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
        "percentage" to grade.percentage
    )

    private fun taskMap(task: StudentTask): Map<String, Any?> = mapOf(
        "id" to task.id,
        "title" to task.title,
        "subjectId" to task.subjectId,
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
                percentage = map.double("percentage") ?: return@mapNotNull null
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
                subjectId = map.string("subjectId"),
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
