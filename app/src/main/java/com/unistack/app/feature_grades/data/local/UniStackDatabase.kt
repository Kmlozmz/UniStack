package com.unistack.app.feature_grades.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unistack.app.feature_expenses.data.local.ExpenseDao
import com.unistack.app.feature_expenses.data.local.ExpenseEntity
import com.unistack.app.feature_templates.data.local.AcademicWorkDao
import com.unistack.app.feature_templates.data.local.AcademicWorkEntity
import com.unistack.app.feature_tasks.data.local.TaskDao
import com.unistack.app.feature_tasks.data.local.TaskEntity

@Database(
    entities = [SubjectEntity::class, GradeEntity::class, TaskEntity::class, ExpenseEntity::class, AcademicWorkEntity::class],
    version = 7,
    exportSchema = true
)
abstract class UniStackDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun gradeDao(): GradeDao
    abstract fun taskDao(): TaskDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun academicWorkDao(): AcademicWorkDao

    companion object {
        @Volatile
        private var INSTANCE: UniStackDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS expenses (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        category TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        dateMillis INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_userId ON expenses(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_category ON expenses(category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_dateMillis ON expenses(dateMillis)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS academic_works (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        templateId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        subjectId TEXT,
                        dueDateMillis INTEGER,
                        status TEXT NOT NULL,
                        priority TEXT NOT NULL,
                        completedChecklistIdsJson TEXT NOT NULL,
                        thesis TEXT NOT NULL,
                        outline TEXT NOT NULL,
                        sources TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_works_userId ON academic_works(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_works_subjectId ON academic_works(subjectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_works_dueDateMillis ON academic_works(dueDateMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_works_status ON academic_works(status)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN type TEXT NOT NULL DEFAULT 'WORKSHOP'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN description TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE grades ADD COLUMN type TEXT NOT NULL DEFAULT 'WORKSHOP'")
                db.execSQL("ALTER TABLE grades ADD COLUMN periodId TEXT NOT NULL DEFAULT 'period-1'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_grades_periodId ON grades(periodId)")
            }
        }

        fun getInstance(context: Context): UniStackDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    UniStackDatabase::class.java,
                    "unistack.db"
                )
                    .addMigrations(*ALL_MIGRATIONS)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
    }
}
