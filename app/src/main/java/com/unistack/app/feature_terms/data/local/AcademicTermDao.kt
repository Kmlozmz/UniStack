package com.unistack.app.feature_terms.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicTermDao {
    /** Del más reciente al más antiguo: el histórico se lee empezando por lo último. */
    @Query("SELECT * FROM academic_terms WHERE userId IN (:userIds) ORDER BY startEpochDay DESC")
    fun observeForUsers(userIds: List<String>): Flow<List<AcademicTermEntity>>

    @Query("SELECT * FROM academic_terms WHERE id = :termId AND userId IN (:userIds)")
    suspend fun byId(termId: String, userIds: List<String>): AcademicTermEntity?

    /**
     * El periodo en curso, preguntado a la base y no a un flujo en cache.
     *
     * `terms` tarda en refrescarse tras un cambio, asi que cerrar un periodo y crear el
     * siguiente seguido encontraba el anterior todavia activo y se negaba. Un guardia que mira
     * una copia desactualizada no es un guardia.
     */
    @Query("SELECT * FROM academic_terms WHERE userId IN (:userIds) AND status = 'ACTIVE' LIMIT 1")
    suspend fun activeFor(userIds: List<String>): AcademicTermEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(term: AcademicTermEntity)

    /**
     * Cierra un periodo, y solo si estaba activo.
     *
     * La condición del `WHERE` no sobra: cerrar es irreversible, así que un segundo cierre
     * —dos toques seguidos, una pantalla que se recompone— no puede pisar la fecha real con
     * otra posterior.
     */
    @Query(
        """
        UPDATE academic_terms
        SET status = 'CLOSED', closedEpochDay = :closedEpochDay, updatedAt = :updatedAt,
            cutSchemeJson = :cutSchemeJson
        WHERE id = :termId AND userId IN (:userIds) AND status = 'ACTIVE'
        """
    )
    suspend fun close(
        termId: String,
        closedEpochDay: Long,
        updatedAt: Long,
        cutSchemeJson: String?,
        userIds: List<String>
    ): Int

    /**
     * Vuelve a poner en curso un periodo recién cerrado: es el «Deshacer» del aviso.
     *
     * Solo si está cerrado y no hay otro activo, en la misma sentencia. La copia del esquema se
     * borra porque el periodo vuelve a leer el de las preferencias.
     */
    @Query(
        """
        UPDATE academic_terms
        SET status = 'ACTIVE', closedEpochDay = NULL, cutSchemeJson = NULL, updatedAt = :updatedAt
        WHERE id = :termId AND userId IN (:userIds) AND status = 'CLOSED'
          AND NOT EXISTS (
            SELECT 1 FROM academic_terms WHERE userId IN (:userIds) AND status = 'ACTIVE'
          )
        """
    )
    suspend fun reopen(termId: String, updatedAt: Long, userIds: List<String>): Int

    @Query("DELETE FROM academic_terms WHERE id = :termId AND userId IN (:userIds)")
    suspend fun delete(termId: String, userIds: List<String>)
}
