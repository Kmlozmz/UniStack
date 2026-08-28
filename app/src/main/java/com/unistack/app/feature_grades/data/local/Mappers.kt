package com.unistack.app.feature_grades.data.local

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.GradingCutScheme
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
        cutScheme = periodSchemeJson.toCutScheme(),
        // Sin `ifBlank`: el vacío es un valor con significado —«el usuario no ha elegido
        // corte»— y convertirlo aquí en «cut-1» era justo lo que hacía que la app diera
        // por elegido el primer corte de toda materia nueva.
        activeCutId = activePeriodId,
        historyPromptStatus = historyPromptStatus.toEnum(PriorHistoryPromptStatus.NOT_SHOWN),
        unknownCutIds = unknownPeriodIdsJson.toStringSet(),
        termId = termId,
        absenceLimit = absenceLimit
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
        periodSchemeJson = cutScheme.toJson(),
        activePeriodId = activeCutId,
        historyPromptStatus = historyPromptStatus.name,
        unknownPeriodIdsJson = JSONArray(unknownCutIds.toList()).toString(),
        termId = termId,
        absenceLimit = absenceLimit,
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
        cutId = periodId.ifBlank { "period-1" },
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
        periodId = cutId,
        source = source.name,
        weightStatus = weightStatus.name,
        taskId = taskId,
        recordedAt = recordedAt.takeIf { it > 0L } ?: System.currentTimeMillis(),
        createdAt = System.currentTimeMillis()
    )
}

private fun GradingCutScheme.toJson(): String = JSONObject()
    .put(
        "periods",
        JSONArray(
            cuts.sortedBy { it.order }.map { cut ->
                JSONObject()
                    .put("id", cut.id)
                    .put("name", cut.name)
                    .put("weight", cut.weight)
                    .put("order", cut.order)
                    .put("endEpochDay", cut.endEpochDay)
            }
        )
    )
    .toString()

private fun String.toCutScheme(): GradingCutScheme {
    if (isBlank()) return GradingCutScheme.default()
    return runCatching {
        val root = JSONObject(this)
        val array = root.optJSONArray("periods") ?: return@runCatching GradingCutScheme.default()
        val cuts = (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            val weight = item.optDouble("weight", 0.0)
            if (weight <= 0.0) return@mapNotNull null
            GradingCut(
                id = item.optString("id", "period-${index + 1}"),
                name = item.optString("name", "${Corte.Singular} ${index + 1}"),
                weight = weight,
                order = item.optInt("order", index + 1),
                endEpochDay = if (item.has("endEpochDay") && !item.isNull("endEpochDay")) {
                    item.optLong("endEpochDay")
                } else {
                    null
                }
            )
        }.sortedBy { it.order }
        GradingCutScheme(cuts).takeIf { it.isValid } ?: GradingCutScheme.default()
    }.getOrDefault(GradingCutScheme.default())
}

private fun String.toStringSet(): Set<String> = runCatching {
    val array = JSONArray(this)
    (0 until array.length()).mapNotNull { array.optString(it).takeIf(String::isNotBlank) }.toSet()
}.getOrDefault(emptySet())

private inline fun <reified T : Enum<T>> String.toEnum(default: T): T =
    runCatching { enumValueOf<T>(this) }.getOrDefault(default)
