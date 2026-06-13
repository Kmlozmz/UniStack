package com.unistack.app.feature_grades.data.local

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.AcademicPeriodLabel
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import org.json.JSONArray
import org.json.JSONObject

fun SubjectEntity.toDomain(grades: List<GradeItem>): Subject {
    val type = runCatching { SubjectVisualType.valueOf(visualType) }
        .getOrDefault(SubjectVisualType.TEAL)
    return Subject(
        id = id,
        name = name,
        targetAverage = targetAverage,
        grades = grades,
        visualType = type,
        customColor = customColor,
        periodScheme = periodSchemeJson.toPeriodScheme(),
        activePeriodId = activePeriodId.ifBlank { "period-1" },
        historyPromptStatus = historyPromptStatus.toEnum(PriorHistoryPromptStatus.NOT_SHOWN),
        unknownPeriodIds = unknownPeriodIdsJson.toStringSet()
    )
}

fun Subject.toEntity(userId: String): SubjectEntity {
    val now = System.currentTimeMillis()
    return SubjectEntity(
        id = id,
        userId = userId,
        name = name,
        targetAverage = targetAverage,
        visualType = visualType.name,
        customColor = customColor,
        periodSchemeJson = periodScheme.toJson(),
        activePeriodId = activePeriodId,
        historyPromptStatus = historyPromptStatus.name,
        unknownPeriodIdsJson = JSONArray(unknownPeriodIds.toList()).toString(),
        createdAt = now,
        updatedAt = now
    )
}

fun GradeEntity.toDomain(): GradeItem {
    val gradeType = runCatching { GradeType.valueOf(type) }
        .getOrDefault(GradeType.WORKSHOP)
    return GradeItem(
        id = id,
        name = name,
        value = value,
        percentage = percentage,
        type = gradeType,
        periodId = periodId.ifBlank { "period-1" },
        source = source.toEnum(GradeSource.ACTIVITY),
        weightStatus = weightStatus.toEnum(GradeWeightStatus.KNOWN),
        taskId = taskId,
        recordedAt = recordedAt.takeIf { it > 0L } ?: createdAt
    )
}

fun GradeItem.toEntity(subjectId: String): GradeEntity {
    return GradeEntity(
        id = id,
        subjectId = subjectId,
        name = name,
        value = value,
        percentage = percentage,
        type = type.name,
        periodId = periodId,
        source = source.name,
        weightStatus = weightStatus.name,
        taskId = taskId,
        recordedAt = recordedAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
        createdAt = System.currentTimeMillis()
    )
}

private fun AcademicPeriodScheme.toJson(): String = JSONObject()
    .put("label", label.name)
    .put(
        "periods",
        JSONArray(
            periods.sortedBy { it.order }.map { period ->
                JSONObject()
                    .put("id", period.id)
                    .put("name", period.name)
                    .put("weight", period.weight)
                    .put("order", period.order)
            }
        )
    )
    .toString()

private fun String.toPeriodScheme(): AcademicPeriodScheme {
    if (isBlank()) return AcademicPeriodScheme.default()
    return runCatching {
        val root = JSONObject(this)
        val label = root.optString("label").toEnum(AcademicPeriodLabel.CORTE)
        val array = root.optJSONArray("periods") ?: return@runCatching AcademicPeriodScheme.default()
        val periods = (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val weight = item.optDouble("weight", 0.0)
            if (weight <= 0.0) return@mapNotNull null
            AcademicPeriod(
                id = item.optString("id", "period-${index + 1}"),
                name = item.optString("name", "${label.singular} ${index + 1}"),
                weight = weight,
                order = item.optInt("order", index + 1)
            )
        }.sortedBy { it.order }
        AcademicPeriodScheme(label, periods).takeIf { it.isValid } ?: AcademicPeriodScheme.default()
    }.getOrDefault(AcademicPeriodScheme.default())
}

private fun String.toStringSet(): Set<String> = runCatching {
    val array = JSONArray(this)
    (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }.toSet()
}.getOrDefault(emptySet())

private inline fun <reified T : Enum<T>> String.toEnum(default: T): T =
    runCatching { enumValueOf<T>(this) }.getOrDefault(default)
