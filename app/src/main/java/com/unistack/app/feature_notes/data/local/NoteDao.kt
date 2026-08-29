package com.unistack.app.feature_notes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query(
        """
        SELECT * FROM notes
        WHERE userId IN (:userIds)
        ORDER BY pinned DESC, updatedAt DESC
        """
    )
    fun observeNotesForUsers(userIds: List<String>): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    /*
     * El cuerpo, la materia y el formato se escriben aquí y en ningún otro sitio.
     *
     * La lección viene de `updateSubjectFields`, que durante meses se dejó dos columnas fuera:
     * la app enseñaba el dato guardado en memoria y al releer de la base había desaparecido.
     * Si se añade una columna a `notes`, se añade también a esta consulta.
     */
    @Query(
        """
        UPDATE notes
        SET title = :title,
            body = :body,
            subjectId = :subjectId,
            format = :format,
            pinned = :pinned,
            reminderAt = :reminderAt,
            colorArgb = :colorArgb,
            updatedAt = :updatedAt
        WHERE id = :noteId AND userId IN (:userIds)
        """
    )
    suspend fun updateNoteFields(
        noteId: String,
        userIds: List<String>,
        title: String,
        body: String,
        subjectId: String?,
        format: String,
        pinned: Boolean,
        reminderAt: Long?,
        colorArgb: Int?,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE notes
        SET pinned = :pinned,
            updatedAt = :updatedAt
        WHERE id = :noteId AND userId IN (:userIds)
        """
    )
    suspend fun updateNotePinned(
        noteId: String,
        userIds: List<String>,
        pinned: Boolean,
        updatedAt: Long
    )

    @Query("DELETE FROM notes WHERE id = :noteId AND userId IN (:userIds)")
    suspend fun deleteNoteById(noteId: String, userIds: List<String>)

    @Query("SELECT COUNT(*) FROM notes WHERE userId IN (:userIds)")
    suspend fun countNotesForUsers(userIds: List<String>): Int
}
