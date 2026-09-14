package com.unistack.app.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.unistack.app.feature_grades.data.local.UniStackDatabase
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.unistack.app.TextosDePrueba

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class RoomMigrationTest {
    private lateinit var context: Context
    private lateinit var databaseFile: File

    @Before
    fun setUp() {
        TextosDePrueba.instalar()
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

    @Test
    fun migrationFourToFiveAddsTaskTypeColumn() {
        val database = createDatabaseWithSchema(version = 4)

        UniStackDatabase.MIGRATION_4_5.migrate(database)

        assertTrue(database.hasColumn("tasks", "type"))
        database.close()
    }

    @Test
    fun migrationFiveToSixAddsTaskDescriptionColumn() {
        val database = createDatabaseWithSchema(version = 5)

        UniStackDatabase.MIGRATION_5_6.migrate(database)

        assertTrue(database.hasColumn("tasks", "description"))
        database.close()
    }

    @Test
    fun migrationSixToSevenAddsGradeTypeAndPeriodFields() {
        val database = createDatabaseWithSchema(version = 6)

        UniStackDatabase.MIGRATION_6_7.migrate(database)

        assertTrue(database.hasColumn("grades", "type"))
        assertTrue(database.hasColumn("grades", "periodId"))
        assertTrue(database.hasIndex("index_grades_periodId"))
        database.close()
    }

    @Test
    fun migrationSevenToEightAddsSubjectCustomColorColumn() {
        val database = createDatabaseWithSchema(version = 7)

        UniStackDatabase.MIGRATION_7_8.migrate(database)

        assertTrue(database.hasColumn("subjects", "customColor"))
        database.close()
    }

    @Test
    fun migrationEightToNineAddsAcademicHistoryAndTaskGradeFields() {
        val database = createDatabaseWithSchema(version = 8)
        database.execSQL(
            """
            INSERT INTO subjects (
                id, userId, name, targetAverage, visualType, createdAt, updatedAt, customColor
            ) VALUES ('subject-1', 'local', 'Fisica', 4.0, 'BLUE', 10, 10, NULL)
            """.trimIndent()
        )
        database.execSQL(
            """
            INSERT INTO grades (
                id, subjectId, name, value, percentage, createdAt, type, periodId
            ) VALUES ('grade-1', 'subject-1', 'Parcial', 4.5, 0.5, 1234, 'EXAM', 'period-1')
            """.trimIndent()
        )

        UniStackDatabase.MIGRATION_8_9.migrate(database)

        listOf("periodSchemeJson", "activePeriodId", "historyPromptStatus", "unknownPeriodIdsJson")
            .forEach { assertTrue(database.hasColumn("subjects", it)) }
        listOf("source", "weightStatus", "taskId", "recordedAt")
            .forEach { assertTrue(database.hasColumn("grades", it)) }
        listOf("periodId", "gradingStatus", "linkedGradeId", "completedAt")
            .forEach { assertTrue(database.hasColumn("tasks", it)) }
        assertTrue(database.hasIndex("index_grades_taskId"))
        assertTrue(database.hasIndex("index_tasks_periodId"))
        assertTrue(database.hasIndex("index_tasks_gradingStatus"))
        database.query("SELECT recordedAt FROM grades WHERE id = 'grade-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1234L, cursor.getLong(0))
        }
        database.close()
    }

    @Test
    fun migrationTwelveToThirteenCreatesAgendaEventsTableAndIndexes() {
        val database = createDatabaseWithSchema(version = 12)

        UniStackDatabase.MIGRATION_12_13.migrate(database)

        assertTrue(database.hasTable("agenda_events"))
        assertTrue(database.hasIndex("index_agenda_events_userId"))
        assertTrue(database.hasIndex("index_agenda_events_startMillis"))
        assertTrue(database.hasIndex("index_agenda_events_kind"))
        database.close()
    }

    @Test
    fun migrationThirteenToFourteenCreatesTermsAndLinksSubjects() {
        val database = createDatabaseWithSchema(version = 13)

        UniStackDatabase.MIGRATION_13_14.migrate(database)

        assertTrue(database.hasTable("academic_terms"))
        assertTrue(database.hasIndex("index_academic_terms_userId"))
        assertTrue(database.hasIndex("index_academic_terms_status"))
        assertTrue(database.hasIndex("index_academic_terms_startEpochDay"))
        // La columna que hace que todo lo academico herede el periodo por la materia.
        assertTrue(database.hasColumn("subjects", "termId"))
        assertTrue(database.hasIndex("index_subjects_termId"))
        database.close()
    }

    /**
     * Las materias que ya existian sobreviven, y sin periodo.
     *
     * Nulo significa «de antes de que hubiera periodos». Rellenarlo en la migracion —con el
     * primer periodo, o con uno inventado a partir de la fecha de creacion— seria darle al
     * usuario un semestre que nunca declaro, que es justo lo que este trabajo viene a quitar.
     */
    @Test
    fun migrationThirteenToFourteenLeavesExistingSubjectsWithoutTerm() {
        val database = createDatabaseWithSchema(version = 13)
        database.execSQL(
            """
            INSERT INTO subjects (id, userId, name, targetAverage, visualType, createdAt, updatedAt)
            VALUES ('s1', 'local', 'Calculo II', 4.0, 'TEAL', 0, 0)
            """.trimIndent()
        )

        UniStackDatabase.MIGRATION_13_14.migrate(database)

        database.query("SELECT termId FROM subjects WHERE id = 's1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
        }
        database.close()
    }

    @Test
    fun migrationSixteenToSeventeenCreatesNotesTableAndIndexes() {
        val database = createDatabaseWithSchema(version = 16)

        UniStackDatabase.MIGRATION_16_17.migrate(database)

        assertTrue(database.hasTable("notes"))
        assertTrue(database.hasIndex("index_notes_userId"))
        assertTrue(database.hasIndex("index_notes_subjectId"))
        assertTrue(database.hasIndex("index_notes_updatedAt"))
        // `pinned` y `format` entran ya aunque todavia no se puedan cambiar desde ninguna
        // pantalla: anadirlas mas tarde obligaria a volver a mover la tabla.
        assertTrue(database.hasColumn("notes", "pinned"))
        assertTrue(database.hasColumn("notes", "format"))
        database.close()
    }

    /**
     * La migracion no toca lo que ya habia.
     *
     * Las notas nacen en una tabla nueva y vacia; lo que estuviera escrito en la hoja vieja vive
     * en las preferencias del telefono y lo rescata el repositorio, no SQL.
     */
    @Test
    fun migrationSixteenToSeventeenLeavesTheNotesTableEmpty() {
        val database = createDatabaseWithSchema(version = 16)

        UniStackDatabase.MIGRATION_16_17.migrate(database)

        database.query("SELECT COUNT(*) FROM notes").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        database.close()
    }

    @Test
    fun migrationSeventeenToEighteenCreatesAttachmentsTableAndIndexes() {
        val database = createDatabaseWithSchema(version = 17)

        UniStackDatabase.MIGRATION_17_18.migrate(database)

        assertTrue(database.hasTable("note_attachments"))
        assertTrue(database.hasIndex("index_note_attachments_userId"))
        assertTrue(database.hasIndex("index_note_attachments_noteId"))
        // La duracion solo la tiene el audio, asi que es la unica columna que admite nulo.
        assertTrue(database.hasColumn("note_attachments", "durationMillis"))
        assertTrue(database.hasColumn("note_attachments", "storedName"))
        database.close()
    }

    /** Las notas que ya existian sobreviven a la migracion, y sin nada colgado. */
    @Test
    fun migrationSeventeenToEighteenLeavesExistingNotesAlone() {
        val database = createDatabaseWithSchema(version = 17)
        database.execSQL(
            """
            INSERT INTO notes (id, userId, body, subjectId, format, pinned, createdAt, updatedAt)
            VALUES ('n1', 'local', 'Parcial el martes', NULL, 'PLAIN', 0, 0, 0)
            """.trimIndent()
        )

        UniStackDatabase.MIGRATION_17_18.migrate(database)

        database.query("SELECT body FROM notes WHERE id = 'n1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Parcial el martes", cursor.getString(0))
        }
        database.query("SELECT COUNT(*) FROM note_attachments").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        database.close()
    }

    @Test
    fun migrationEighteenToNineteenAddsTitleReminderAndColour() {
        val database = createDatabaseWithSchema(version = 18)

        UniStackDatabase.MIGRATION_18_19.migrate(database)

        assertTrue(database.hasColumn("notes", "title"))
        assertTrue(database.hasColumn("notes", "reminderAt"))
        assertTrue(database.hasColumn("notes", "colorArgb"))
        assertTrue(database.hasIndex("index_notes_reminderAt"))
        database.close()
    }

    /**
     * Lo que ya estaba escrito sobrevive, sin titulo y sin aviso.
     *
     * El titulo nace vacio porque hasta ahora era la primera linea del cuerpo, y `reminderAt`
     * nulo porque «no avisa» es justo lo que le pasa a todo lo anterior: nadie pudo ponerle hora
     * a una nota antes de que existiera la campana.
     */
    @Test
    fun migrationEighteenToNineteenLeavesOldNotesUntouched() {
        val database = createDatabaseWithSchema(version = 18)
        database.execSQL(
            """
            INSERT INTO notes (id, userId, body, subjectId, format, pinned, createdAt, updatedAt)
            VALUES ('n1', 'local', 'Parcial el martes', NULL, 'PLAIN', 0, 0, 0)
            """.trimIndent()
        )

        UniStackDatabase.MIGRATION_18_19.migrate(database)

        database.query("SELECT title, body, reminderAt, colorArgb FROM notes WHERE id = 'n1'").use {
            assertTrue(it.moveToFirst())
            assertEquals("", it.getString(0))
            assertEquals("Parcial el martes", it.getString(1))
            assertTrue(it.isNull(2))
            assertTrue(it.isNull(3))
        }
        database.close()
    }

    @Test
    fun migrationNineteenToTwentyAddsArchiveAndTrash() {
        val database = createDatabaseWithSchema(version = 19)

        UniStackDatabase.MIGRATION_19_20.migrate(database)

        assertTrue(database.hasColumn("notes", "archived"))
        assertTrue(database.hasColumn("notes", "deletedAt"))
        assertTrue(database.hasIndex("index_notes_deletedAt"))
        database.close()
    }

    /**
     * Lo que ya habia sigue en la lista.
     *
     * Ni archivado ni en la papelera: son estados que nadie pudo pedir antes de que existieran,
     * y darselos a las notas viejas las haria desaparecer de la pantalla al actualizar.
     */
    @Test
    fun migrationNineteenToTwentyLeavesNotesInTheList() {
        val database = createDatabaseWithSchema(version = 19)
        database.execSQL(
            """
            INSERT INTO notes (id, userId, title, body, subjectId, format, pinned, reminderAt,
                colorArgb, createdAt, updatedAt)
            VALUES ('n1', 'local', '', 'Parcial el martes', NULL, 'PLAIN', 0, NULL, NULL, 0, 0)
            """.trimIndent()
        )

        UniStackDatabase.MIGRATION_19_20.migrate(database)

        database.query("SELECT archived, deletedAt FROM notes WHERE id = 'n1'").use {
            assertTrue(it.moveToFirst())
            assertEquals(0, it.getInt(0))
            assertTrue(it.isNull(1))
        }
        database.close()
    }

    @Test
    fun migrationTwentyToTwentyOneAddsClosedPeriodIdsJsonColumn() {
        val database = createDatabaseWithSchema(version = 20)

        UniStackDatabase.MIGRATION_20_21.migrate(database)

        assertTrue(database.hasColumn("subjects", "closedPeriodIdsJson"))
        database.close()
    }

    @Test
    fun migrationTwentyOneToTwentyTwoCreatesTaskSubtasksTableAndIndexes() {
        val database = createDatabaseWithSchema(version = 21)

        UniStackDatabase.MIGRATION_21_22.migrate(database)

        assertTrue(database.hasTable("task_subtasks"))
        assertTrue(database.hasColumn("task_subtasks", "id"))
        assertTrue(database.hasColumn("task_subtasks", "taskId"))
        assertTrue(database.hasColumn("task_subtasks", "title"))
        assertTrue(database.hasColumn("task_subtasks", "isCompleted"))
        assertTrue(database.hasColumn("task_subtasks", "position"))
        assertTrue(database.hasIndex("index_task_subtasks_taskId"))
        assertTrue(database.hasIndex("index_task_subtasks_position"))
        database.close()
    }

    @Test
    fun migrationTwentyTwoToTwentyThreeCreatesTaskAttachmentsTableAndIndexes() {
        val database = createDatabaseWithSchema(version = 22)

        UniStackDatabase.MIGRATION_22_23.migrate(database)

        assertTrue(database.hasTable("task_attachments"))
        assertTrue(database.hasColumn("task_attachments", "id"))
        assertTrue(database.hasColumn("task_attachments", "userId"))
        assertTrue(database.hasColumn("task_attachments", "taskId"))
        assertTrue(database.hasColumn("task_attachments", "kind"))
        assertTrue(database.hasColumn("task_attachments", "displayName"))
        assertTrue(database.hasColumn("task_attachments", "storedName"))
        assertTrue(database.hasColumn("task_attachments", "mimeType"))
        assertTrue(database.hasColumn("task_attachments", "sizeBytes"))
        assertTrue(database.hasColumn("task_attachments", "durationMillis"))
        assertTrue(database.hasColumn("task_attachments", "createdAt"))
        assertTrue(database.hasIndex("index_task_attachments_userId"))
        assertTrue(database.hasIndex("index_task_attachments_taskId"))
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

    private fun createDatabaseWithSchema(version: Int): SupportSQLiteDatabase {
        return createDatabase(version = version) { db ->
            createVersionOneSchema(db)
            applyMigrationsThrough(db, version)
        }
    }

    private fun applyMigrationsThrough(db: SupportSQLiteDatabase, targetVersion: Int) {
        if (targetVersion >= 2) UniStackDatabase.MIGRATION_1_2.migrate(db)
        if (targetVersion >= 3) UniStackDatabase.MIGRATION_2_3.migrate(db)
        if (targetVersion >= 4) UniStackDatabase.MIGRATION_3_4.migrate(db)
        if (targetVersion >= 5) UniStackDatabase.MIGRATION_4_5.migrate(db)
        if (targetVersion >= 6) UniStackDatabase.MIGRATION_5_6.migrate(db)
        if (targetVersion >= 7) UniStackDatabase.MIGRATION_6_7.migrate(db)
        if (targetVersion >= 8) UniStackDatabase.MIGRATION_7_8.migrate(db)
        if (targetVersion >= 9) UniStackDatabase.MIGRATION_8_9.migrate(db)
        if (targetVersion >= 10) UniStackDatabase.MIGRATION_9_10.migrate(db)
        if (targetVersion >= 11) UniStackDatabase.MIGRATION_10_11.migrate(db)
        if (targetVersion >= 12) UniStackDatabase.MIGRATION_11_12.migrate(db)
        if (targetVersion >= 13) UniStackDatabase.MIGRATION_12_13.migrate(db)
        if (targetVersion >= 14) UniStackDatabase.MIGRATION_13_14.migrate(db)
        if (targetVersion >= 15) UniStackDatabase.MIGRATION_14_15.migrate(db)
        if (targetVersion >= 16) UniStackDatabase.MIGRATION_15_16.migrate(db)
        if (targetVersion >= 17) UniStackDatabase.MIGRATION_16_17.migrate(db)
        if (targetVersion >= 18) UniStackDatabase.MIGRATION_17_18.migrate(db)
        if (targetVersion >= 19) UniStackDatabase.MIGRATION_18_19.migrate(db)
        if (targetVersion >= 20) UniStackDatabase.MIGRATION_19_20.migrate(db)
        if (targetVersion >= 21) UniStackDatabase.MIGRATION_20_21.migrate(db)
        if (targetVersion >= 22) UniStackDatabase.MIGRATION_21_22.migrate(db)
        if (targetVersion >= 23) UniStackDatabase.MIGRATION_22_23.migrate(db)
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

    private fun SupportSQLiteDatabase.hasColumn(table: String, column: String): Boolean {
        return query("PRAGMA table_info($table)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            generateSequence { if (cursor.moveToNext()) cursor.getString(nameIndex) else null }
                .any { it == column }
        }
    }
}
