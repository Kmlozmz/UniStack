package com.unistack.app.feature_grades.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "grades",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId"), Index("periodId"), Index("taskId")]
)
data class GradeEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val name: String,
    val value: Double,
    val percentage: Double,
    val type: String = "WORKSHOP",
    val periodId: String = "period-1",
    val source: String = "ACTIVITY",
    val weightStatus: String = "KNOWN",
    val taskId: String? = null,
    val recordedAt: Long = 0L,
    val createdAt: Long
)
