package com.unistack.app.feature_terms.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.unistack.app.feature_grades.data.local.UniStackDatabase
import com.unistack.app.feature_terms.data.local.toDomain
import com.unistack.app.feature_terms.data.local.toEntity
import com.unistack.app.feature_terms.domain.AcademicTermStatus
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.data.InMemoryUserRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Lo que el repositorio de periodos no puede dejar pasar.
 *
 * Dos invariantes valen por todo lo demás: **no puede haber dos periodos activos**, y **cerrar
 * es irreversible**. Si alguna se rompe, el histórico deja de significar nada.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class RoomAcademicTermRepositoryTest {
    private lateinit var database: UniStackDatabase
    private lateinit var repository: RoomAcademicTermRepository

    private val inicio = LocalDate.parse("2026-08-10")
    private val finPrevisto = LocalDate.parse("2026-12-12")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UniStackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val userRepository = InMemoryUserRepository().also { it.saveUserProfile(perfil()) }
        repository = RoomAcademicTermRepository(database.academicTermDao(), userRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `crear un periodo lo deja activo y sin fecha de cierre`() = runBlocking {
        val term = repository.create("2026-2", AcademicTermType.SEMESTER, inicio, finPrevisto)
            .getOrThrow()

        assertTrue(term.isActive)
        assertNull(term.closedEpochDay)
        assertEquals(inicio.toEpochDay(), term.startEpochDay)
        assertEquals(finPrevisto.toEpochDay(), term.plannedEndEpochDay)
        assertNotNull(repository.terms.first { it.isNotEmpty() })
    }

    @Test
    fun `no se puede abrir un periodo con otro en curso`() = runBlocking {
        repository.create("2026-2", AcademicTermType.SEMESTER, inicio, finPrevisto).getOrThrow()
        repository.terms.first { it.isNotEmpty() }

        val segundo = repository.create("2027-1", AcademicTermType.SEMESTER, LocalDate.parse("2027-01-20"), null)

        assertTrue(segundo.isFailure)
    }

    @Test
    fun `un periodo no puede acabar antes de empezar`() = runBlocking {
        val malo = repository.create("2026-2", AcademicTermType.SEMESTER, inicio, inicio.minusDays(1))
        assertTrue(malo.isFailure)
    }

    @Test
    fun `un periodo necesita nombre`() = runBlocking {
        assertTrue(repository.create("   ", AcademicTermType.SEMESTER, inicio, finPrevisto).isFailure)
    }

    @Test
    fun `cerrar deja el periodo cerrado y libera el sitio para el siguiente`() = runBlocking {
        val term = repository.create("2026-2", AcademicTermType.SEMESTER, inicio, finPrevisto).getOrThrow()
        repository.terms.first { it.isNotEmpty() }

        repository.close(term.id, LocalDate.parse("2026-12-15")).getOrThrow()

        val guardado = database.academicTermDao().byId(term.id, UserIds.storageIdsFor(UserIds.LOCAL))
        assertEquals(AcademicTermStatus.CLOSED.name, guardado?.status)
        assertEquals(LocalDate.parse("2026-12-15").toEpochDay(), guardado?.closedEpochDay)

        // Con el sitio libre, ya se puede empezar el siguiente.
        val siguiente = repository.create("2027-1", AcademicTermType.SEMESTER, LocalDate.parse("2027-01-20"), null)
        assertTrue(siguiente.isSuccess)
    }

    /**
     * Cerrar dos veces no puede mover la fecha real.
     *
     * Dos toques seguidos, o una pantalla que se recompone, no pueden pisar el día en que de
     * verdad se cerró con otro posterior: el histórico dejaría de decir la verdad.
     */
    @Test
    fun `cerrar un periodo ya cerrado falla y no toca la fecha`() = runBlocking {
        val term = repository.create("2026-2", AcademicTermType.SEMESTER, inicio, finPrevisto).getOrThrow()
        repository.terms.first { it.isNotEmpty() }
        repository.close(term.id, LocalDate.parse("2026-12-15")).getOrThrow()

        val segundo = repository.close(term.id, LocalDate.parse("2027-01-30"))

        assertTrue(segundo.isFailure)
        val guardado = database.academicTermDao().byId(term.id, UserIds.storageIdsFor(UserIds.LOCAL))
        assertEquals(LocalDate.parse("2026-12-15").toEpochDay(), guardado?.closedEpochDay)
    }

    @Test
    fun `no se puede cerrar antes del dia en que empezo`() = runBlocking {
        val term = repository.create("2026-2", AcademicTermType.SEMESTER, inicio, finPrevisto).getOrThrow()
        repository.terms.first { it.isNotEmpty() }

        assertTrue(repository.close(term.id, inicio.minusDays(1)).isFailure)
    }

    @Test
    fun `cerrar un periodo que no existe falla en vez de romper`() = runBlocking {
        assertTrue(repository.close("no-existe", LocalDate.parse("2026-12-15")).isFailure)
    }

    /**
     * El estado se deriva de la fecha de cierre, no de la columna.
     *
     * Son el mismo hecho contado dos veces. Si una escritura a medias las deja en desacuerdo,
     * manda la fecha: así no queda un periodo cerrado que se pueda reactivar.
     */
    @Test
    fun `una fila con fecha de cierre se lee como cerrada aunque diga lo contrario`() = runBlocking {
        val term = repository.create("2026-2", AcademicTermType.SEMESTER, inicio, finPrevisto).getOrThrow()
        repository.terms.first { it.isNotEmpty() }
        // Se escribe a mano una fila contradictoria: con fecha de cierre pero marcada activa.
        database.academicTermDao().upsert(
            term.toEntity().copy(closedEpochDay = finPrevisto.toEpochDay(), status = "ACTIVE")
        )

        val leido = database.academicTermDao()
            .byId(term.id, UserIds.storageIdsFor(UserIds.LOCAL))!!
            .toDomain()

        assertFalse(leido.isActive)
        assertEquals(AcademicTermStatus.CLOSED, leido.status)
    }

    private fun perfil(): UserProfile {
        val now = System.currentTimeMillis()
        return UserProfile(
            userId = UserIds.LOCAL,
            preferredName = "Tester",
            careerOrProgram = "Ingenieria",
            studyArea = null,
            gradingScale = GradingScale.ZERO_TO_FIVE,
            passingGrade = 3.0,
            targetAverage = 4.0,
            enabledModules = setOf(AppModule.GRADES),
            setupCompleted = true,
            createdAt = now,
            updatedAt = now
        )
    }
}
