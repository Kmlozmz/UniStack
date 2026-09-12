package com.unistack.app.feature_notes.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.unistack.app.feature_grades.data.local.UniStackDatabase
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_user.data.InMemoryUserRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserProfile
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.unistack.app.TextosDePrueba

/**
 * Las notas, por el mismo camino que las usa la app.
 *
 * Pasa por el repositorio y no por el DAO a proposito: el fallo que costo meses en las materias
 * —una consulta de actualizacion que se dejaba dos columnas fuera— era invisible desde arriba
 * porque el objeto en memoria si tenia el dato. Solo se veia releyendo de la base.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class RoomNotesRepositoryTest {

    private lateinit var database: UniStackDatabase
    private lateinit var userRepository: InMemoryUserRepository

    @Before
    fun setUp() {
        TextosDePrueba.instalar()
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UniStackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userRepository = InMemoryUserRepository().also { it.saveUserProfile(testProfile()) }
    }

    @After
    fun tearDown() {
        database.close()
    }

    /** Los nombres de archivo que el repositorio ha mandado borrar. */
    private val borrados = mutableListOf<String>()

    private fun repository(legacy: String? = null) = RoomNotesRepository(
        noteDao = database.noteDao(),
        attachmentDao = database.noteAttachmentDao(),
        userRepository = userRepository,
        legacySheet = { legacy },
        fileVault = { nombres -> borrados += nombres }
    )

    private fun adjunto(id: String = "att-1", noteId: String = "note-1") = NoteAttachment(
        id = id,
        noteId = noteId,
        kind = AttachmentKind.IMAGE,
        displayName = "Pizarra.jpg",
        storedName = "note-" + id + ".jpg",
        mimeType = "image/jpeg",
        sizeBytes = 2048,
        durationMillis = null,
        createdAt = 1_000L
    )

    private fun nota(id: String = "note-1") = QuickNote(
        id = id,
        body = "Parcial 2 el martes",
        subjectId = null,
        format = NoteFormat.PLAIN,
        pinned = false,
        createdAt = 1_000L,
        updatedAt = 1_000L
    )

    @Test
    fun laNotaSeGuardaYSeVuelveALeer() {
        runBlocking {
            val repository = repository()
            repository.addNote(nota())

            val guardada = repository.notes.awaitValue { it.isNotEmpty() }.single()
            assertEquals("Parcial 2 el martes", guardada.body)
            assertNull(guardada.subjectId)
        }
    }

    /**
     * Todo lo que se edita tiene que llegar a la base.
     *
     * Es la prueba que le faltaba a `updateSubjectFields`: alli la materia guardaba su tope de
     * faltas y su periodo en memoria, la pantalla los enseñaba, y al releer habian desaparecido
     * porque la consulta no los escribia. Aqui se comprueba campo por campo.
     */
    @Test
    fun editarUnaNotaGuardaLaMateriaElFormatoYElTexto() {
        runBlocking {
            val repository = repository()
            repository.addNote(nota())
            val original = repository.notes.awaitValue { it.isNotEmpty() }.single()

            repository.updateNote(
                original.copy(
                    body = "Parcial 2 el miercoles, salon 302",
                    subjectId = "subject-calculo",
                    format = NoteFormat.MARKDOWN,
                    pinned = true
                )
            )

            val releida = repository.notes.awaitValue { notas ->
                notas.single().body.contains("miercoles")
            }.single()

            assertEquals("Parcial 2 el miercoles, salon 302", releida.body)
            assertEquals("subject-calculo", releida.subjectId)
            assertEquals(NoteFormat.MARKDOWN, releida.format)
            assertTrue(releida.pinned)
        }
    }

    /** Desvincular la materia es un valor, no un olvido: el nulo tambien tiene que escribirse. */
    @Test
    fun quitarLaMateriaDejaLaNotaSinMateria() {
        runBlocking {
            val repository = repository()
            repository.addNote(nota().copy(subjectId = "subject-calculo"))
            val conMateria = repository.notes.awaitValue { it.isNotEmpty() }.single()

            repository.updateNote(conMateria.copy(subjectId = null))

            val sinMateria = repository.notes
                .awaitValue { notas -> notas.single().subjectId == null }
                .single()
            assertNull(sinMateria.subjectId)
        }
    }

    @Test
    fun lasFijadasVanArribaAunqueSeanMasViejas() {
        runBlocking {
            val repository = repository()
            repository.addNote(nota("vieja").copy(pinned = true, updatedAt = 1_000L))
            repository.addNote(nota("nueva").copy(updatedAt = 9_000L))

            val orden = repository.notes.awaitValue { it.size == 2 }.map { it.id }
            assertEquals(listOf("vieja", "nueva"), orden)
        }
    }

    @Test
    fun borrarUnaNotaLaQuitaDeLaLista() {
        runBlocking {
            val repository = repository()
            repository.addNote(nota())
            repository.notes.awaitValue { it.isNotEmpty() }

            repository.deleteNote("note-1")

            repository.notes.awaitValue { it.isEmpty() }
        }
    }

    @Test
    fun loQueSeCuelgaDeUnaNotaSeGuardaEntero() {
        runBlocking {
            val repository = repository()
            repository.addNote(nota())
            repository.addAttachment(adjunto())

            val guardado = repository.attachments.awaitValue { it.isNotEmpty() }.single()
            assertEquals("note-1", guardado.noteId)
            assertEquals(AttachmentKind.IMAGE, guardado.kind)
            assertEquals("Pizarra.jpg", guardado.displayName)
            assertEquals("note-att-1.jpg", guardado.storedName)
            assertEquals(2048L, guardado.sizeBytes)
        }
    }

    /**
     * Borrar una nota se lleva sus archivos, y no solo sus filas.
     *
     * Al reves quedarian copias huerfanas ocupando sitio en el telefono sin que nada en la app
     * supiera de ellas: nadie las veria y nadie podria borrarlas.
     */
    @Test
    fun borrarLaNotaSeLlevaSusAdjuntosYSusArchivos() {
        runBlocking {
            val repository = repository()
            repository.addNote(nota())
            repository.addAttachment(adjunto())
            repository.attachments.awaitValue { it.isNotEmpty() }

            repository.deleteNote("note-1")

            repository.attachments.awaitValue { it.isEmpty() }
            repository.notes.awaitValue { it.isEmpty() }
            assertEquals(listOf("note-att-1.jpg"), borrados)
        }
    }

    @Test
    fun quitarUnAdjuntoSueltoTambienBorraSuArchivo() {
        runBlocking {
            val repository = repository()
            repository.addNote(nota())
            repository.addAttachment(adjunto())
            repository.attachments.awaitValue { it.isNotEmpty() }

            repository.deleteAttachment("att-1")

            repository.attachments.awaitValue { it.isEmpty() }
            assertEquals(listOf("note-att-1.jpg"), borrados)
            // La nota sigue: se quito la foto, no el apunte.
            assertEquals(1, repository.notes.awaitValue { it.isNotEmpty() }.size)
        }
    }

    /**
     * La hoja de antes no se pierde.
     *
     * Era un unico texto en las preferencias del telefono. Al pasar a notas sueltas se convierte
     * en la primera nota; si se perdiera, la mejora empezaria por borrar lo que habia.
     */
    @Test
    fun laHojaViejaSeConvierteEnLaPrimeraNota() {
        runBlocking {
            val repository = repository(legacy = "Lo que habia escrito antes")

            val rescatada = repository.notes.awaitValue { it.isNotEmpty() }.single()
            assertEquals("Lo que habia escrito antes", rescatada.body)
            assertEquals(NoteFormat.PLAIN, rescatada.format)
            assertNull(rescatada.subjectId)
        }
    }

    @Test
    fun sinHojaViejaNoSeInventaNingunaNota() {
        runBlocking {
            val repository = repository(legacy = null)
            repository.addNote(nota())

            val notas = repository.notes.awaitValue { it.isNotEmpty() }
            assertEquals(1, notas.size)
            assertEquals("note-1", notas.single().id)
        }
    }

    private suspend fun <T> StateFlow<T>.awaitValue(predicate: (T) -> Boolean): T {
        if (predicate(value)) return value
        return withTimeout(3_000) { filter(predicate).first() }
    }

    private fun testProfile(): UserProfile {
        val now = System.currentTimeMillis()
        return UserProfile(
            userId = UserIds.LOCAL,
            preferredName = "Tester",
            careerOrProgram = "Ingenieria",
            studyArea = null,
            gradingScale = GradingScale.ZERO_TO_FIVE,
            passingGrade = 3.0,
            targetAverage = 4.0,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS),
            setupCompleted = true,
            createdAt = now,
            updatedAt = now
        )
    }
}
