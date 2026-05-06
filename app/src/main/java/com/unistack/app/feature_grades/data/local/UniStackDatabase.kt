package com.unistack.app.feature_grades.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unistack.app.feature_tasks.data.local.TaskDao
import com.unistack.app.feature_tasks.data.local.TaskEntity

@Database(
    entities = [SubjectEntity::class, GradeEntity::class, TaskEntity::class],
    version = 2,
    exportSchema = false
)
abstract class UniStackDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun gradeDao(): GradeDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: UniStackDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tasks (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        subjectId TEXT,
                        dueDateMillis INTEGER NOT NULL,
                        difficulty TEXT NOT NULL,
                        estimatedMinutes INTEGER NOT NULL,
                        completed INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_userId ON tasks(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_subjectId ON tasks(subjectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_dueDateMillis ON tasks(dueDateMillis)")
            }
        }

        fun getInstance(context: Context): UniStackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UniStackDatabase::class.java,
                    "unistack.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
