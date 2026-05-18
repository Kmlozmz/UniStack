package com.unistack.app.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.unistack.app.feature_grades.data.local.UniStackDatabase
import java.io.File
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class RoomMigrationTest {
    private lateinit var context: Context
    private lateinit var databaseFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        databaseFile = File(context.filesDir, "unistack-migration-test.db")
        context.deleteDatabase(databaseFile.name)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseFile.name)
    }

    @Test
    fun migrationOneToTwoCreatesTasksTableAndIndexes() {
        val database = createDatabase(version = 1) { db ->
            createVersionOneSchema(db)
        }

        UniStackDatabase.MIGRATION_1_2.migrate(database)

        assertTrue(database.hasTable("tasks"))
        assertTrue(database.hasIndex("index_tasks_userId"))
        assertTrue(database.hasIndex("index_tasks_subjectId"))
        assertTrue(database.hasIndex("index_tasks_dueDateMillis"))
        database.close()
    }

    @Test
    fun migrationTwoToThreeCreatesExpensesTableAndIndexes() {
        val database = createDatabase(version = 2) { db ->
            createVersionOneSchema(db)
            UniStackDatabase.MIGRATION_1_2.migrate(db)
        }

        UniStackDatabase.MIGRATION_2_3.migrate(database)

        assertTrue(database.hasTable("expenses"))
        assertTrue(database.hasIndex("index_expenses_userId"))
        assertTrue(database.hasIndex("index_expenses_category"))
        assertTrue(database.hasIndex("index_expenses_dateMillis"))
        database.close()
    }

    @Test
    fun migrationThreeToFourCreatesAcademicWorksTableAndIndexes() {
        val database = createDatabase(version = 3) { db ->
            createVersionOneSchema(db)
            UniStackDatabase.MIGRATION_1_2.migrate(db)
            UniStackDatabase.MIGRATION_2_3.migrate(db)
        }

        UniStackDatabase.MIGRATION_3_4.migrate(database)

        assertTrue(database.hasTable("academic_works"))
        assertTrue(database.hasIndex("index_academic_works_userId"))
        assertTrue(database.hasIndex("index_academic_works_subjectId"))
        assertTrue(database.hasIndex("index_academic_works_dueDateMillis"))
        assertTrue(database.hasIndex("index_academic_works_status"))
        database.close()
    }

    private fun createDatabase(
        version: Int,
        onCreateSchema: (SupportSQLiteDatabase) -> Unit
    ): SupportSQLiteDatabase {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseFile.name)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(version) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            onCreateSchema(db)
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int
                        ) = Unit
                    }
                )
                .build()
        )
        return helper.writableDatabase
    }

    private fun createVersionOneSchema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS subjects (
                id TEXT NOT NULL,
                userId TEXT NOT NULL,
                name TEXT NOT NULL,
                targetAverage REAL NOT NULL,
                visualType TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS grades (
                id TEXT NOT NULL,
                subjectId TEXT NOT NULL,
                name TEXT NOT NULL,
                value REAL NOT NULL,
                percentage REAL NOT NULL,
                createdAt INTEGER NOT NULL,
                PRIMARY KEY(id),
                FOREIGN KEY(subjectId) REFERENCES subjects(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_grades_subjectId ON grades(subjectId)")
    }

    private fun SupportSQLiteDatabase.hasTable(name: String): Boolean {
        return query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?", arrayOf(name)).use {
            it.moveToFirst()
        }
    }

    private fun SupportSQLiteDatabase.hasIndex(name: String): Boolean {
        return query("SELECT name FROM sqlite_master WHERE type = 'index' AND name = ?", arrayOf(name)).use {
            it.moveToFirst()
        }
    }
}
