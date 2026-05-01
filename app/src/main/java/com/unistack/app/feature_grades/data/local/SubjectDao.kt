package com.unistack.app.feature_grades.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeSubjectsForUser(userId: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    fun getSubjectById(subjectId: String): Flow<SubjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: String)
}
