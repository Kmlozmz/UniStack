package com.unistack.app.feature_grades.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun observeGradesForSubject(subjectId: String): Flow<List<GradeEntity>>

    @Query("SELECT * FROM grades ORDER BY createdAt DESC")
    fun observeAllGrades(): Flow<List<GradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity)

    @Query("DELETE FROM grades WHERE id = :gradeId")
    suspend fun deleteGradeById(gradeId: String)

    @Query("DELETE FROM grades WHERE subjectId = :subjectId")
    suspend fun deleteGradesForSubject(subjectId: String)
}
