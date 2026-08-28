package com.unistack.app.feature_terms.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.unistack.app.feature_terms.domain.AcademicBreak

/**
 * Un tramo sin clase en disco.
 *
 * Días epoch por el mismo motivo que el periodo: un festivo es un día, no un instante, y
 * guardarlo como milisegundos obligaría a arrastrar la zona horaria para poder preguntar «¿esta
 * clase cae dentro?» sin que cambiar de país mueva la respuesta.
 */
@Entity(
    tableName = "academic_breaks",
    indices = [Index("userId"), Index("startEpochDay")]
)
data class AcademicBreakEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val createdAt: Long,
    val updatedAt: Long
)

fun AcademicBreakEntity.toDomain(): AcademicBreak = AcademicBreak(
    id = id,
    userId = userId,
    name = name,
    startEpochDay = startEpochDay,
    endEpochDay = endEpochDay,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AcademicBreak.toEntity(): AcademicBreakEntity = AcademicBreakEntity(
    id = id,
    userId = userId,
    name = name,
    startEpochDay = startEpochDay,
    endEpochDay = endEpochDay,
    createdAt = createdAt,
    updatedAt = updatedAt
)
