package com.unistack.app.feature_grades.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unistack.app.feature_expenses.data.local.ExpenseDao
import com.unistack.app.feature_expenses.data.local.ExpenseEntity
import com.unistack.app.feature_notes.data.local.NoteAttachmentDao
import com.unistack.app.feature_notes.data.local.NoteAttachmentEntity
import com.unistack.app.feature_notes.data.local.NoteDao
import com.unistack.app.feature_notes.data.local.NoteEntity
import com.unistack.app.feature_templates.data.local.AcademicWorkDao
import com.unistack.app.feature_templates.data.local.AcademicWorkEntity
import com.unistack.app.feature_tasks.data.local.TaskDao
import com.unistack.app.feature_tasks.data.local.TaskEntity
import com.unistack.app.feature_tasks.data.local.TaskAttachmentDao
import com.unistack.app.feature_tasks.data.local.TaskAttachmentEntity
import com.unistack.app.feature_schedule.data.local.ClassSessionDao
import com.unistack.app.feature_schedule.data.local.ClassSessionEntity
import com.unistack.app.feature_schedule.data.local.ClassOccurrenceDao
import com.unistack.app.feature_schedule.data.local.ClassOccurrenceEntity
import com.unistack.app.feature_schedule.data.local.AgendaEventDao
import com.unistack.app.feature_terms.data.local.AcademicBreakDao
import com.unistack.app.feature_terms.data.local.AcademicBreakEntity
import com.unistack.app.feature_terms.data.local.AcademicTermDao
import com.unistack.app.feature_terms.data.local.AcademicTermEntity
import com.unistack.app.feature_schedule.data.local.AgendaEventEntity

import com.unistack.app.feature_tasks.data.local.TaskSubtaskEntity

@Database(
    entities = [
        SubjectEntity::class,
        GradeEntity::class,
        TaskEntity::class,
        TaskSubtaskEntity::class,
        ExpenseEntity::class,
        AcademicWorkEntity::class,
        ClassSessionEntity::class,
        ClassOccurrenceEntity::class,
        AgendaEventEntity::class,
        AcademicTermEntity::class,
        AcademicBreakEntity::class,
        NoteEntity::class,
        NoteAttachmentEntity::class,
        TaskAttachmentEntity::class
    ],
    version = 24,
    exportSchema = true
)
abstract class UniStackDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun gradeDao(): GradeDao
    abstract fun taskDao(): TaskDao
    abstract fun taskAttachmentDao(): TaskAttachmentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun academicWorkDao(): AcademicWorkDao
    abstract fun classSessionDao(): ClassSessionDao
    abstract fun classOccurrenceDao(): ClassOccurrenceDao
    abstract fun agendaEventDao(): AgendaEventDao
    abstract fun academicTermDao(): AcademicTermDao
    abstract fun academicBreakDao(): AcademicBreakDao
    abstract fun noteDao(): NoteDao
    abstract fun noteAttachmentDao(): NoteAttachmentDao

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

        /*
         * El periodo academico entra en escena, y las materias aprenden a que ciclo pertenecen.
         *
         * `termId` es lo unico que se anade a `subjects`, y con eso basta: todo lo academico
         * cuelga de la materia —notas, clases, ocurrencias, y las tareas y trabajos que tengan
         * materia—, asi que hereda el periodo por el camino sin tocar sus tablas.
         *
         * Nace en NULL para todas las materias que ya existen. Ese nulo significa «de antes de
         * que hubiera periodos» y no se rellena aqui a la fuerza: inventar una fecha de inicio
         * para un semestre que nadie declaro seria exactamente la suposicion que este trabajo
         * viene a quitar. Quien tenga datos previos elegira, y hasta entonces siguen visibles.
         */
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS academic_terms (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        startEpochDay INTEGER NOT NULL,
                        plannedEndEpochDay INTEGER,
                        closedEpochDay INTEGER,
                        status TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_terms_userId ON academic_terms(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_terms_status ON academic_terms(status)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_terms_startEpochDay ON academic_terms(startEpochDay)")
                db.execSQL("ALTER TABLE subjects ADD COLUMN termId TEXT DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_subjects_termId ON subjects(termId)")
            }
        }

        /**
         * Los dias sin clase y el tope de faltas.
         *
         * Las dos cosas existen por lo mismo: sin ellas la asistencia cuenta mal. El tope
         * entra como columna anulable porque nulo significa «no lo ha dicho» —no hay valor
         * honesto que inventar— y los tramos como tabla propia porque son del calendario y no
         * de una materia: un festivo lo es para todas.
         */
        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS academic_breaks (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        name TEXT NOT NULL,
                        startEpochDay INTEGER NOT NULL,
                        endEpochDay INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_academic_breaks_userId ON academic_breaks(userId)")
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_academic_breaks_startEpochDay ON academic_breaks(startEpochDay)"
                )
                db.execSQL("ALTER TABLE subjects ADD COLUMN absenceLimit INTEGER DEFAULT NULL")
            }
        }

        /**
         * De donde viene una materia que se repite.
         *
         * Sin la columna, «repitiendo» habria que deducirlo buscando por nombre en periodos
         * anteriores, y eso se rompe al renombrar. Nula en todo lo existente: nadie ha
         * repetido nada todavia porque hasta ahora no habia periodos que cerrar.
         */
        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subjects ADD COLUMN repeatedFromSubjectId TEXT DEFAULT NULL")
            }
        }

        /**
         * Las notas dejan de ser una hoja y pasan a ser filas.
         *
         * Hasta aqui las notas rapidas vivian en las preferencias del telefono: un unico texto
         * de cuatro mil caracteres, fuera de la base de datos y por tanto fuera del respaldo.
         * Restaurar una copia no las traia porque nunca estuvieron dentro.
         *
         * `format` nace en PLAIN y no en el formato que el usuario prefiera: la preferencia
         * dice con que nacen las notas nuevas, y lo ya escrito se escribio sin marcas. `pinned`
         * nace apagado y todavia no se puede tocar desde ninguna pantalla; esta la columna
         * puesta desde el principio para no volver a mover la tabla cuando se pueda.
         */
        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS notes (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        body TEXT NOT NULL,
                        subjectId TEXT,
                        format TEXT NOT NULL,
                        pinned INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_userId ON notes(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_subjectId ON notes(subjectId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_updatedAt ON notes(updatedAt)")
            }
        }

        /**
         * Lo que se cuelga de una nota: fotos, archivos y grabaciones.
         *
         * Tabla aparte y no una columna con una lista dentro, porque un adjunto tiene datos
         * propios —tamaño, tipo, duracion— y porque asi borrar uno no reescribe la nota entera.
         *
         * `storedName` es el nombre del archivo **dentro** de la app. Guardar la direccion de la
         * galeria habria sido mas barato y habria dejado las notas en blanco el dia que alguien
         * limpia la galeria, que es exactamente lo que se venia a evitar.
         */
        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS note_attachments (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        noteId TEXT NOT NULL,
                        kind TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        storedName TEXT NOT NULL,
                        mimeType TEXT NOT NULL,
                        sizeBytes INTEGER NOT NULL,
                        durationMillis INTEGER,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_attachments_userId ON note_attachments(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_attachments_noteId ON note_attachments(noteId)")
            }
        }

        /**
         * La nota crece: titulo propio, recordatorio y color.
         *
         * El titulo era la primera linea del cuerpo. Ahorraba una columna y a cambio impedia dos
         * cosas: escribir una nota que empiece por una lista sin que el primer punto hiciera de
         * titulo, y buscar solo por titulos.
         *
         * `reminderAt` es lo que convierte un apunte en algo que vuelve solo. Nulo es «no avisa»,
         * que es lo que le pasa a todo lo que ya estaba escrito.
         */
        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN title TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE notes ADD COLUMN reminderAt INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE notes ADD COLUMN colorArgb INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_reminderAt ON notes(reminderAt)")
            }
        }

        /**
         * Archivar y la papelera.
         *
         * Borrar dejaba de existir la nota en el acto, con sus fotos y sus grabaciones detras.
         * Ahora se mueve: `deletedAt` dice cuando, y de ahi salen las dos cosas que hacen que una
         * papelera sirva —poder deshacer y vaciarse sola a la semana—.
         *
         * `archived` es otra cosa distinta: el apunte de un parcial que ya paso no es basura, es
         * historia. Sin el, la unica forma de no verlo todos los dias era borrarlo.
         */
        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE notes ADD COLUMN archived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE notes ADD COLUMN deletedAt INTEGER DEFAULT NULL")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_deletedAt ON notes(deletedAt)")
            }
        }

        /**
         * El corte cerrado a mano.
         *
         * Hasta ahora un corte con el 100 % repartido se daba por cerrado solo, y por eso el
         * sello no lo veia nadie: pasaba mientras estabas en otra pantalla. El cierre pasa a
         * ser una accion, y una accion hay que guardarla.
         */
        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subjects ADD COLUMN closedPeriodIdsJson TEXT NOT NULL DEFAULT '[]'")
            }
        }

        /**
         * Subtareas de una tarea.
         *
         * Permite desglosar una tarea en pasos interactivos con orden explícito y estado de completitud.
         */
        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS task_subtasks (
                        id TEXT NOT NULL,
                        taskId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        isCompleted INTEGER NOT NULL DEFAULT 0,
                        position INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(id),
                        FOREIGN KEY(taskId) REFERENCES tasks(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_task_subtasks_taskId ON task_subtasks(taskId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_task_subtasks_position ON task_subtasks(position)")
            }
        }

        /**
         * Lo que se cuelga de una tarea: fotos, archivos y grabaciones — el mismo derecho que
         * ya tenían las notas rápidas. Tabla aparte y calcada de `note_attachments`, con
         * `taskId` en vez de `noteId`; misma razón: un adjunto tiene datos propios y borrar uno
         * no debe reescribir la tarea entera.
         */
        /**
         * El periodo cerrado guarda sus cortes.
         *
         * Columna nula y sin valor por defecto: los que ya estaban cerrados no tienen copia, y
         * inventarla con el esquema de hoy sería justo el error que viene a quitar.
         */
        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE academic_terms ADD COLUMN cutSchemeJson TEXT")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS task_attachments (
                        id TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        taskId TEXT NOT NULL,
                        kind TEXT NOT NULL,
                        displayName TEXT NOT NULL,
                        storedName TEXT NOT NULL,
                        mimeType TEXT NOT NULL,
                        sizeBytes INTEGER NOT NULL,
                        durationMillis INTEGER,
                        createdAt INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_task_attachments_userId ON task_attachments(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_task_attachments_taskId ON task_attachments(taskId)")
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
            MIGRATION_12_13,
            MIGRATION_13_14,
            MIGRATION_14_15,
            MIGRATION_15_16,
            MIGRATION_16_17,
            MIGRATION_17_18,
            MIGRATION_18_19,
            MIGRATION_19_20,
            MIGRATION_20_21,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24
        )
    }
}
