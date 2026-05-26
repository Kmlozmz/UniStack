package com.unistack.app.feature_sync.data

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_sync.domain.LocalBackupPreview
import com.unistack.app.feature_sync.domain.LocalBackupRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class LocalJsonBackupRepository(
    private val userRepository: UserRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val expensesRepository: ExpensesRepository,
    private val academicWorksRepository: AcademicWorksRepository
) : LocalBackupRepository {

    override fun exportBackupJson(): String {
        return JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("profile", profileJson(userRepository.userProfile.value))
            .put("subjects", JSONArray(gradesRepository.subjects.value.map(::subjectJson)))
            .put("tasks", JSONArray(tasksRepository.tasks.value.map(::taskJson)))
            .put("expenses", JSONArray(expensesRepository.expenses.value.map(::expenseJson)))
            .put("academicWorks", JSONArray(academicWorksRepository.works.value.map(::academicWorkJson)))
            .toString(2)
    }

    override fun previewBackupJson(json: String): Result<LocalBackupPreview> = runCatching {
        val root = JSONObject(json)
        check(root.optInt("schemaVersion") in 1..SCHEMA_VERSION) { "Versión de backup no soportada." }
        val subjects = root.optJSONArray("subjects") ?: JSONArray()
        val grades = (0 until subjects.length()).sumOf { index ->
            subjects.optJSONObject(index)?.optJSONArray("grades")?.length() ?: 0
        }
        LocalBackupPreview(
            schemaVersion = root.optInt("schemaVersion"),
            subjects = subjects.length(),
            grades = grades,
            tasks = root.optJSONArray("tasks")?.length() ?: 0,
            expenses = root.optJSONArray("expenses")?.length() ?: 0,
            academicWorks = root.optJSONArray("academicWorks")?.length() ?: 0
        )
    }

    override fun restoreBackupJson(json: String): Result<LocalBackupPreview> = runCatching {
        val preview = previewBackupJson(json).getOrThrow()
        val root = JSONObject(json)
        restoreProfile(root.optJSONObject("profile"))
        parseSubjects(root.optJSONArray("subjects")).forEach { subject ->
            val existing = gradesRepository.subjects.value.firstOrNull { it.id == subject.id }
            if (existing == null) gradesRepository.addSubject(subject.copy(grades = emptyList())) else gradesRepository.updateSubject(subject.copy(grades = existing.grades))
            subject.grades.forEach { grade ->
                if (existing?.grades?.any { it.id == grade.id } == true) {
                    gradesRepository.updateGrade(subject.id, grade)
                } else {
                    gradesRepository.addGrade(subject.id, grade)
                }
            }
        }
        parseTasks(root.optJSONArray("tasks")).forEach { task ->
            if (tasksRepository.tasks.value.any { it.id == task.id }) tasksRepository.updateTask(task) else tasksRepository.addTask(task)
        }
        parseExpenses(root.optJSONArray("expenses")).forEach { expense ->
            if (expensesRepository.expenses.value.any { it.id == expense.id }) expensesRepository.updateExpense(expense) else expensesRepository.addExpense(expense)
        }
        parseAcademicWorks(root.optJSONArray("academicWorks")).forEach { work ->
            if (academicWorksRepository.works.value.any { it.id == work.id }) academicWorksRepository.updateWork(work) else academicWorksRepository.addWork(work)
        }
        preview
    }

    override fun exportAcademicReport(): String {
        val profile = userRepository.userProfile.value
        val scale = profile?.gradingScale
        return buildString {
            appendLine("Reporte académico UniStack")
            appendLine("Estudiante: ${profile?.preferredName?.takeIf { it.isNotBlank() } ?: "Estudiante"}")
            appendLine()
            gradesRepository.subjects.value.forEach { subject ->
                val average = GradeCalculator.calculateCurrentAverage(subject.grades)
                appendLine("${subject.name} · Promedio ${GradingScaleUtils.formatGrade(average, scale ?: profile?.gradingScale ?: com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE)}")
                subject.grades.forEach { grade ->
                    appendLine("- ${grade.name}: ${grade.value} · ${(grade.percentage * 100).toInt()}%")
                }
                appendLine()
            }
        }.trim()
    }

    override fun exportAcademicPdf(context: Context): Result<String> = runCatching {
        val file = File(context.cacheDir, "unistack-academic-report.pdf")
        val document = PdfDocument()
        val paint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
        }
        val titlePaint = Paint(paint).apply {
            textSize = 18f
            isFakeBoldText = true
        }
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var y = 48f
        page.canvas.drawText("Reporte académico UniStack", 40f, y, titlePaint)
        y += 28f
        exportAcademicReport().lineSequence().forEach { line ->
            if (y > 800f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                y = 48f
            }
            page.canvas.drawText(line.take(92), 40f, y, paint)
            y += 18f
        }
        document.finishPage(page)
        file.outputStream().use { output -> document.writeTo(output) }
        document.close()
        file.absolutePath
    }


    override fun exportTasksCsv(): String {
        return buildCsv(
            header = listOf("id", "title", "description", "subjectId", "type", "dueDate", "difficulty", "estimatedMinutes", "completed"),
            rows = tasksRepository.tasks.value.map { task ->
                listOf(task.id, task.title, task.description, task.subjectId.orEmpty(), task.type.name, TaskDateUtils.fromMillis(task.dueDateMillis).toString(), task.difficulty.name, task.estimatedMinutes.toString(), task.completed.toString())
            }
        )
    }

    override fun exportExpensesCsv(): String {
        return buildCsv(
            header = listOf("id", "category", "amount", "date"),
            rows = expensesRepository.expenses.value.map { expense ->
                listOf(expense.id, expense.category.name, expense.amount.toString(), ExpenseDateUtils.fromMillis(expense.dateMillis).toString())
            }
        )
    }

    private fun profileJson(profile: UserProfile?): JSONObject {
        return JSONObject()
            .put("preferredName", profile?.preferredName)
            .put("customGradeMax", profile?.customGradeMax ?: 100.0)
            .put("weeklyBudget", profile?.weeklyBudget ?: 0)
            .put("monthlyBudget", profile?.monthlyBudget ?: 0)
            .put("expenseAlertThresholdPercent", profile?.expenseAlertThresholdPercent ?: 80)
            .put("enabledExpenseCategories", JSONArray(profile?.enabledExpenseCategories?.map { it.name }.orEmpty()))
            .put("enabledModules", JSONArray(profile?.enabledModules?.map { it.name }.orEmpty()))
    }

    private fun restoreProfile(profileJson: JSONObject?) {
        val current = userRepository.userProfile.value ?: return
        if (profileJson == null) return
        val modules = profileJson.optJSONArray("enabledModules")
            .strings()
            .mapNotNull { name -> runCatching { AppModule.valueOf(name) }.getOrNull() }
            .toSet()
            .ifEmpty { current.enabledModules }
        val expenseCategories = profileJson.optJSONArray("enabledExpenseCategories")
            .strings()
            .mapNotNull { name -> runCatching { ExpenseCategory.valueOf(name) }.getOrNull() }
            .toSet()
            .ifEmpty { current.enabledExpenseCategories }
        userRepository.saveUserProfile(
            current.copy(
                preferredName = profileJson.optString("preferredName", current.preferredName).takeIf { it.isNotBlank() } ?: current.preferredName,
                customGradeMax = profileJson.optDouble("customGradeMax", current.customGradeMax).coerceIn(1.0, 100.0),
                weeklyBudget = profileJson.optInt("weeklyBudget", current.weeklyBudget),
                monthlyBudget = profileJson.optInt("monthlyBudget", current.monthlyBudget),
                expenseAlertThresholdPercent = profileJson.optInt("expenseAlertThresholdPercent", current.expenseAlertThresholdPercent),
                enabledExpenseCategories = expenseCategories,
                enabledModules = modules,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun subjectJson(subject: Subject): JSONObject = JSONObject()
        .put("id", subject.id)
        .put("name", subject.name)
        .put("targetAverage", subject.targetAverage)
        .put("visualType", subject.visualType.name)
        .put("grades", JSONArray(subject.grades.map(::gradeJson)))

    private fun gradeJson(grade: GradeItem): JSONObject = JSONObject()
        .put("id", grade.id)
        .put("name", grade.name)
        .put("value", grade.value)
        .put("percentage", grade.percentage)

    private fun taskJson(task: StudentTask): JSONObject = JSONObject()
        .put("id", task.id)
        .put("title", task.title)
        .put("description", task.description)
        .put("subjectId", task.subjectId)
        .put("type", task.type.name)
        .put("dueDateMillis", task.dueDateMillis)
        .put("difficulty", task.difficulty.name)
        .put("estimatedMinutes", task.estimatedMinutes)
        .put("completed", task.completed)
        .put("createdAt", task.createdAt)
        .put("updatedAt", task.updatedAt)

    private fun expenseJson(expense: Expense): JSONObject = JSONObject()
        .put("id", expense.id)
        .put("category", expense.category.name)
        .put("amount", expense.amount)
        .put("dateMillis", expense.dateMillis)
        .put("createdAt", expense.createdAt)
        .put("updatedAt", expense.updatedAt)

    private fun academicWorkJson(work: AcademicWork): JSONObject = JSONObject()
        .put("id", work.id)
        .put("templateId", work.templateId)
        .put("title", work.title)
        .put("subjectId", work.subjectId)
        .put("dueDateMillis", work.dueDateMillis)
        .put("status", work.status.name)
        .put("priority", work.priority.name)
        .put("completedChecklistIds", JSONArray(work.completedChecklistIds.toList()))
        .put("thesis", work.thesis)
        .put("outline", work.outline)
        .put("sources", work.sources)
        .put("notes", work.notes)
        .put("createdAt", work.createdAt)
        .put("updatedAt", work.updatedAt)

    private fun parseSubjects(array: JSONArray?): List<Subject> = array.objects().mapNotNull { item ->
        Subject(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            name = item.optString("name").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            targetAverage = item.optDouble("targetAverage"),
            visualType = item.optString("visualType").toEnum(SubjectVisualType.TEAL),
            grades = parseGrades(item.optJSONArray("grades"))
        )
    }

    private fun parseGrades(array: JSONArray?): List<GradeItem> = array.objects().mapNotNull { item ->
        GradeItem(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            name = item.optString("name").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            value = item.optDouble("value"),
            percentage = item.optDouble("percentage")
        )
    }

    private fun parseTasks(array: JSONArray?): List<StudentTask> = array.objects().mapNotNull { item ->
        StudentTask(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            title = item.optString("title").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            description = item.optString("description", ""),
            subjectId = item.optNullableString("subjectId"),
            type = item.optString("type").toEnum(TaskType.WORKSHOP),
            dueDateMillis = item.optLong("dueDateMillis"),
            difficulty = item.optString("difficulty").toEnum(TaskDifficulty.MEDIUM),
            estimatedMinutes = item.optInt("estimatedMinutes"),
            completed = item.optBoolean("completed"),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun parseExpenses(array: JSONArray?): List<Expense> = array.objects().mapNotNull { item ->
        Expense(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            category = item.optString("category").toEnum(ExpenseCategory.OTHER),
            amount = item.optInt("amount"),
            dateMillis = item.optLong("dateMillis"),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun parseAcademicWorks(array: JSONArray?): List<AcademicWork> = array.objects().mapNotNull { item ->
        AcademicWork(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            templateId = item.optString("templateId").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            title = item.optString("title").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            subjectId = item.optNullableString("subjectId"),
            dueDateMillis = if (item.isNull("dueDateMillis")) null else item.optLong("dueDateMillis"),
            status = item.optString("status").toEnum(AcademicWorkStatus.DRAFT),
            priority = item.optString("priority").toEnum(AcademicWorkPriority.MEDIUM),
            completedChecklistIds = item.optJSONArray("completedChecklistIds").strings().toSet(),
            thesis = item.optString("thesis"),
            outline = item.optString("outline"),
            sources = item.optString("sources"),
            notes = item.optString("notes"),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun buildCsv(header: List<String>, rows: List<List<String>>): String {
        return (listOf(header) + rows).joinToString("\n") { row -> row.joinToString(",") { it.csvEscape() } }
    }

    private fun JSONArray?.objects(): List<JSONObject> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optJSONObject(index) }
    }

    private fun JSONArray?.strings(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optString(index).takeIf { it.isNotBlank() } }
    }

    private inline fun <reified T : Enum<T>> String.toEnum(default: T): T = runCatching { enumValueOf<T>(this) }.getOrDefault(default)
    private fun JSONObject.optNullableString(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
    private fun String.csvEscape(): String = "\"${replace("\"", "\"\"")}\""

    private companion object {
        const val SCHEMA_VERSION = 1
    }
}
