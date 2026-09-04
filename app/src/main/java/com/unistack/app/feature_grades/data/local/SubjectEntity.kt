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
    val closedPeriodIdsJson: String = "[]",
    /*
     * A que periodo academico pertenece esta materia.
     *
     * Nulo significa «de antes de que hubiera periodos», no «sin asignar»: son las materias
     * que ya existian cuando llego este concepto. No se rellenan solas porque inventarles un
     * semestre seria inventarle al usuario un dato que nunca dio.
     */
    val termId: String? = null,
    /**
     * Antiguo tope de faltas por materia. **Ya no se usa.**
     *
     * El tope resulto ser uno solo —sale del reglamento de la universidad, no de la
     * asignatura— y vive ahora en el perfil. La columna sigue declarada porque quitarla obliga
     * a rehacer la tabla, y eso no se hace con datos de un semestre en curso encima: se
     * limpiara entre periodos. No la lee nadie.
     */
    val absenceLimit: Int? = null,
    /** La materia de la que viene esta, si se trajo para repetirla. */
    val repeatedFromSubjectId: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
