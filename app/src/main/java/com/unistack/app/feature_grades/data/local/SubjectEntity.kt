package com.unistack.app.feature_grades.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "subjects", indices = [Index("termId")])
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
    /*
     * A que periodo academico pertenece esta materia.
     *
     * Nulo significa «de antes de que hubiera periodos», no «sin asignar»: son las materias
     * que ya existian cuando llego este concepto. No se rellenan solas porque inventarles un
     * semestre seria inventarle al usuario un dato que nunca dio.
     */
    val termId: String? = null,
    /** El tope de faltas de la materia. Nulo es «no lo ha dicho», no «no hay». */
    val absenceLimit: Int? = null,
    val createdAt: Long,
    val updatedAt: Long
)
