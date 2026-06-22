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
import com.unistack.app.feature_schedule.data.local.ClassSessionDao
import com.unistack.app.feature_schedule.data.local.ClassSessionEntity
import com.unistack.app.feature_schedule.data.local.ClassOccurrenceDao
import com.unistack.app.feature_schedule.data.local.ClassOccurrenceEntity
import com.unistack.app.feature_schedule.data.local.AgendaEventDao
import com.unistack.app.feature_schedule.data.local.AgendaEventEntity

@Database(
    entities = [
        SubjectEntity::class,
        GradeEntity::class,
        TaskEntity::class,
        ExpenseEntity::class,
        AcademicWorkEntity::class,
        ClassSessionEntity::class,
        ClassOccurrenceEntity::class,
        AgendaEventEntity::class
    ],
    version = 13,
    exportSchema = true
)
abstract class UniStackDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun gradeDao(): GradeDao
    abstract fun taskDao(): TaskDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun academicWorkDao(): AcademicWorkDao
    abstract fun classSessionDao(): ClassSessionDao
    abstract fun classOccurrenceDao(): ClassOccurrenceDao
    abstract fun agendaEventDao(): AgendaEventDao

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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subjects ADD COLUMN customColor INTEGER")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subjects ADD COLUMN periodSchemeJson TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE subjects ADD COLUMN activePeriodId TEXT NOT NULL DEFAULT 'period-1'")
                db.execSQL("ALTER TABLE subjects ADD COLUMN historyPromptStatus TEXT NOT NULL DEFAULT 'NOT_SHOWN'")
                db.execSQL("ALTER TABLE subjects ADD COLUMN unknownPeriodIdsJson TEXT NOT NULL DEFAULT '[]'")

                db.execSQL("ALTER TABLE grades ADD COLUMN source TEXT NOT NULL DEFAULT 'ACTIVITY'")
                db.execSQL("ALTER TABLE grades ADD COLUMN weightStatus TEXT NOT NULL DEFAULT 'KNOWN'")
                db.execSQL("ALTER TABLE grades ADD COLUMN taskId TEXT")
                db.execSQL("ALTER TABLE grades ADD COLUMN recordedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE grades SET recordedAt = createdAt WHERE recordedAt = 0")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_grades_taskId ON grades(taskId)")

                db.execSQL("ALTER TABLE tasks ADD COLUMN periodId TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN gradingStatus TEXT NOT NULL DEFAULT 'UNDECIDED'")
                db.execSQL("ALTER TABLE tasks ADD COLUMN linkedGradeId TEXT")
                db.execSQL("ALTER TABLE tasks ADD COLUMN completedAt INTEGER")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_periodId ON tasks(periodId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tasks_gradingStatus ON tasks(gradingStatus)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS class_sessions (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        subjectId TEXT NOT NULL,
                        daysOfWeekCsv TEXT NOT NULL,
                        startMinute INTEGER NOT NULL,
                        endMinute INTEGER NOT NULL,
                        location TEXT NOT NULL,
                        reminderMinutes INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_sessions_userId ON class_sessions(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_sessions_subjectId ON class_sessions(subjectId)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS class_occurrences (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        sessionId TEXT NOT NULL,
                        dateEpochDay INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        modality TEXT NOT NULL,
                        absenceReason TEXT,
                        note TEXT NOT NULL,
                        overrideStartMinute INTEGER,
                        overrideEndMinute INTEGER,
                        overrideLocation TEXT,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_occurrences_userId ON class_occurrences(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_occurrences_sessionId ON class_occurrences(sessionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_class_occurrences_dateEpochDay ON class_occurrences(dateEpochDay)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE class_sessions ADD COLUMN repeatEveryWeeks INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE class_sessions ADD COLUMN recurrenceStartEpochDay INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS agenda_events (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        kind TEXT NOT NULL,
                        startMillis INTEGER NOT NULL,
                        endMillis INTEGER,
                        allDay INTEGER NOT NULL,
                        location TEXT NOT NULL,
                        reminderMinutes INTEGER NOT NULL,
                        recurrence TEXT NOT NULL,
                        recurrenceEndEpochDay INTEGER,
                        colorArgb INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_agenda_events_userId ON agenda_events(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_agenda_events_startMillis ON agenda_events(startMillis)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_agenda_events_kind ON agenda_events(kind)")
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

        val ALL_MIGRATIONS = arrayOf(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8,
            MIGRATION_8_9,
            MIGRATION_9_10,
            MIGRATION_10_11,
            MIGRATION_11_12,
            MIGRATION_12_13
        )
    }
}
