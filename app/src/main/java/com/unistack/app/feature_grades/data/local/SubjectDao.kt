package com.unistack.app.feature_grades.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE userId IN (:userIds) ORDER BY createdAt DESC")
    fun observeSubjectsForUsers(userIds: List<String>): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    fun getSubjectById(subjectId: String): Flow<SubjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    /*
      * Los campos que se escriben al guardar una materia.
      *
      * `absenceLimit` y `termId` faltaban, y no era inocuo: la columna existe, el mapeador la
      * lee y la escribe, pero esta consulta es el unico camino de `updateSubject`, asi que
      * poner un tope de faltas no llegaba a la base y desaparecia en cuanto el flujo volvia a
      * leer de ella. Al anadir una columna a `subjects` hay que anadirla tambien aqui.
      */
    @Query(
        """
        UPDATE subjects
        SET name = :name,
            targetAverage = :targetAverage,
            visualType = :visualType,
            customColor = :customColor,
            periodSchemeJson = :periodSchemeJson,
            activePeriodId = :activeCutId,
            historyPromptStatus = :historyPromptStatus,
            unknownPeriodIdsJson = :unknownPeriodIdsJson,
            termId = :termId,
            absenceLimit = :absenceLimit,
            updatedAt = :updatedAt
        WHERE id = :subjectId AND userId IN (:userIds)
        """
    )
    suspend fun updateSubjectFields(
        subjectId: String,
        userIds: List<String>,
        name: String,
        targetAverage: Double,
        visualType: String,
        customColor: Int?,
        periodSchemeJson: String,
        activeCutId: String,
        historyPromptStatus: String,
        unknownPeriodIdsJson: String,
        termId: String?,
        absenceLimit: Int?,
        updatedAt: Long
    )

    /** Estampa el periodo en las materias que todavia no lo tienen, al cerrarlo. */
    @Query(
        """
        UPDATE subjects SET termId = :termId, updatedAt = :updatedAt
        WHERE termId IS NULL AND userId IN (:userIds)
        """
    )
    suspend fun stampMissingTerm(termId: String, updatedAt: Long, userIds: List<String>): Int

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: String)
}
