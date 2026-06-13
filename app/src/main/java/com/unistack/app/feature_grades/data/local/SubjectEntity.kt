package com.unistack.app.feature_grades.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val targetAverage: Double,
    val visualType: String,
    val customColor: Int? = null,
    val periodSchemeJson: String = "",
    val activePeriodId: String = "period-1",
    val historyPromptStatus: String = "NOT_SHOWN",
    val unknownPeriodIdsJson: String = "[]",
    val createdAt: Long,
    val updatedAt: Long
)
