package com.unistack.app.feature_terms.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * El periodo académico en disco.
 *
 * Las fechas se guardan como día epoch y no como milisegundos: un periodo empieza un día, no
 * a una hora, y guardar el instante obligaría a arrastrar la zona horaria para poder comparar
 * «¿esta clase cae dentro?» sin que cambiar de país mueva la respuesta.
 */
@Entity(
    tableName = "academic_terms",
    indices = [Index("userId"), Index("status"), Index("startEpochDay")]
)
data class AcademicTermEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val type: String,
    val startEpochDay: Long,
    val plannedEndEpochDay: Long?,
    val closedEpochDay: Long?,
    val status: String,
    val createdAt: Long,
    val updatedAt: Long
)
