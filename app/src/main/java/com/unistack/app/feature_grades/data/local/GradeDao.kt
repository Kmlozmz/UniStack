package com.unistack.app.feature_grades.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
    @Query("SELECT * FROM grades ORDER BY createdAt DESC")
    fun observeAllGrades(): Flow<List<GradeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeEntity)

    @Query(
        """
        UPDATE grades
        SET name = :name,
            value = :value,
            percentage = :percentage,
            type = :type,
            periodId = :cutId,
            source = :source,
            weightStatus = :weightStatus,
            taskId = :taskId,
            recordedAt = :recordedAt
        WHERE id = :gradeId AND subjectId = :subjectId
        """
    )
    suspend fun updateGradeFields(
        subjectId: String,
        gradeId: String,
        name: String,
        value: Double,
        percentage: Double,
        type: String,
        cutId: String,
        source: String,
        weightStatus: String,
        taskId: String?,
        recordedAt: Long
    )

    @Query("DELETE FROM grades WHERE id = :gradeId")
    suspend fun deleteGradeById(gradeId: String)

    @Query("DELETE FROM grades WHERE subjectId = :subjectId")
    suspend fun deleteGradesForSubject(subjectId: String)
}
