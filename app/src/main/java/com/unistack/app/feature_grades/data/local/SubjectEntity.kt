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
    val createdAt: Long,
    val updatedAt: Long
)
