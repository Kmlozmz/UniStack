package com.unistack.app.feature_templates.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "academic_works",
    indices = [
        Index("userId"),
        Index("subjectId"),
        Index("dueDateMillis"),
        Index("status")
    ]
)
data class AcademicWorkEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val templateId: String,
    val title: String,
    val subjectId: String?,
    val dueDateMillis: Long?,
    val status: String,
    val priority: String,
    val completedChecklistIdsJson: String,
    val thesis: String,
    val outline: String,
    val sources: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)
