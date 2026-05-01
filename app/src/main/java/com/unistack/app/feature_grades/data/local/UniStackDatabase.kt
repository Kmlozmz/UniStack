package com.unistack.app.feature_grades.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SubjectEntity::class, GradeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class UniStackDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun gradeDao(): GradeDao

    companion object {
        @Volatile
        private var INSTANCE: UniStackDatabase? = null

        fun getInstance(context: Context): UniStackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UniStackDatabase::class.java,
                    "unistack.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
