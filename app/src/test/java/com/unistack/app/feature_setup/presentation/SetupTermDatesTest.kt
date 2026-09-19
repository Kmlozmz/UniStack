package com.unistack.app.feature_setup.presentation

import com.unistack.app.core.MainDispatcherRule
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.data.InMemoryUserRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/**
 * Las fechas de corte del onboarding.
 *
 * Existe por un fallo que no se veía: la lista de fechas nacía vacía y escribir en ella era un
 * `mapIndexed` sobre cero elementos, así que elegir una fecha en el calendario no guardaba nada
 * y la pantalla tampoco lo decía. Las pruebas de aquí fijan que la lista tenga siempre una
 * casilla por corte y que lo escrito se quede.
 */
class SetupTermDatesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SetupViewModel

    private val inicio: LocalDate = LocalDate.of(2026, 8, 24)
    private val fin: LocalDate = LocalDate.of(2026, 12, 12)

    @Before
    fun setUp() {
        viewModel = SetupViewModel(InMemoryUserRepository(), FakeAcademicTermRepository())
    }

    /** Deja el periodo listo con [cortes] cortes y fechas de inicio y fin puestas. */
    private fun periodoCon(cortes: Int) {
        viewModel.updateGradingCutCount(cortes)
        viewModel.updateTermType(AcademicTermType.SEMESTER)
        viewModel.updateTermStart(inicio)
        viewModel.updateTermPlannedEnd(fin)
    }

    @Test
    fun `hay una casilla por corte menos el ultimo`() {
        periodoCon(3)

        assertEquals(2, viewModel.cutEndDates.size)
        assertTrue(viewModel.cutEndDates.all { it == null })
    }

    @Test
    fun `con un solo corte no hay ninguna casilla`() {
        periodoCon(1)

        assertTrue(viewModel.cutEndDates.isEmpty())
    }

    @Test
    fun `la fecha elegida se guarda`() {
        periodoCon(3)

        val corte = LocalDate.of(2026, 9, 30)
        viewModel.updateCutEndDate(0, corte)

        assertEquals(corte, viewModel.cutEndDates[0])
        assertNull(viewModel.cutEndDates[1])
    }

    @Test
    fun `escribir fuera de rango no rompe ni inventa casillas`() {
        periodoCon(3)

        viewModel.updateCutEndDate(7, LocalDate.of(2026, 9, 30))

        assertEquals(2, viewModel.cutEndDates.size)
        assertTrue(viewModel.cutEndDates.all { it == null })
    }

    @Test
    fun `cambiar cuantos cortes hay reajusta las casillas`() {
        periodoCon(3)
        viewModel.updateCutEndDate(0, LocalDate.of(2026, 9, 30))

        viewModel.updateGradingCutCount(2)

        assertEquals(1, viewModel.cutEndDates.size)
        assertNull(viewModel.cutEndDates[0])
    }

    @Test
    fun `mover el inicio del periodo vacia los cortes`() {
        periodoCon(3)
        viewModel.updateCutEndDate(0, LocalDate.of(2026, 9, 30))

        viewModel.updateTermStart(LocalDate.of(2026, 8, 31))

        assertTrue(viewModel.cutEndDates.all { it == null })
    }

    @Test
    fun `sin ninguna fecha de corte el periodo vale`() {
        periodoCon(3)

        assertTrue(viewModel.isTermValid)
    }

    @Test
    fun `con las fechas a medias el periodo no vale`() {
        periodoCon(3)

        viewModel.updateCutEndDate(0, LocalDate.of(2026, 9, 30))

        assertFalse(viewModel.isTermValid)
    }

    @Test
    fun `con todas las fechas en orden el periodo vale`() {
        periodoCon(3)

        viewModel.updateCutEndDate(0, LocalDate.of(2026, 9, 30))
        viewModel.updateCutEndDate(1, LocalDate.of(2026, 11, 6))

        assertTrue(viewModel.isTermValid)
    }

    @Test
    fun `un corte que cierra despues del siguiente no vale`() {
        periodoCon(3)

        viewModel.updateCutEndDate(0, LocalDate.of(2026, 11, 6))
        viewModel.updateCutEndDate(1, LocalDate.of(2026, 9, 30))

        assertFalse(viewModel.isTermValid)
    }

    @Test
    fun `un corte anterior al inicio del periodo no vale`() {
        periodoCon(2)

        viewModel.updateCutEndDate(0, inicio.minusDays(1))

        assertFalse(viewModel.isTermValid)
    }

    @Test
    fun `el ultimo corte necesita dias despues del anterior`() {
        periodoCon(2)

        // Cerrar el primero el mismo dia que acaba el periodo deja al segundo sin dias.
        viewModel.updateCutEndDate(0, fin)

        assertFalse(viewModel.isTermValid)
    }

    @Test
    fun `decir que las pondras luego deja las casillas vacias`() {
        periodoCon(3)
        viewModel.updateCutEndDate(0, LocalDate.of(2026, 9, 30))

        viewModel.updateKnowsCutDates(false)

        assertEquals(2, viewModel.cutEndDates.size)
        assertTrue(viewModel.cutEndDates.all { it == null })
    }

    @Test
    fun `saltar fechas deja inicio y fin nulos y limpia los cortes`() {
        periodoCon(3)
        viewModel.skipTermDates()

        assertNull(viewModel.termStart)
        assertNull(viewModel.termPlannedEnd)
        assertEquals(false, viewModel.knowsCutDates)
        assertEquals(2, viewModel.cutEndDates.size)
        assertTrue(viewModel.cutEndDates.all { it == null })
    }

    @Test
    fun `reiniciar onboarding activa isReplayingSetup y se puede finalizar`() {
        val userRepo = InMemoryUserRepository()
        assertFalse(userRepo.isReplayingSetup.value)

        userRepo.startReplayingSetup()
        assertTrue(userRepo.isReplayingSetup.value)

        userRepo.finishReplayingSetup()
        assertFalse(userRepo.isReplayingSetup.value)
    }

    @Test
    fun `init precarga los datos del perfil existente cuando setupCompleted es true`() {
        val userRepo = InMemoryUserRepository()
        userRepo.saveUserProfile(
            testProfile().copy(
                preferredName = "Carlos",
                careerOrProgram = "Ingeniería de Software",
                studyArea = StudyArea.ENGINEERING_TECHNOLOGY,
                gradingScale = GradingScale.ZERO_TO_FIVE,
                passingGrade = 3.0,
                targetAverage = 4.2
            )
        )

        val vm = SetupViewModel(userRepo, FakeAcademicTermRepository())

        assertEquals("Carlos", vm.preferredName)
        assertEquals(StudyArea.ENGINEERING_TECHNOLOGY, vm.studyArea)
        assertEquals(GradingScale.ZERO_TO_FIVE, vm.gradingScale)
        assertTrue(vm.scaleChosen)
        assertEquals("3.0", vm.passingGradeText)
        assertEquals("4.2", vm.targetAverageText)
    }

    @Test
    fun `en modo repeticion finishSetup no modifica el perfil ni crea periodos`() {
        val userRepo = InMemoryUserRepository()
        userRepo.saveUserProfile(
            testProfile().copy(
                preferredName = "Carlos Original",
                gradingScale = GradingScale.ZERO_TO_FIVE
            )
        )
        userRepo.startReplayingSetup()

        val termRepo = FakeAcademicTermRepository()
        val vm = SetupViewModel(userRepo, termRepo)

        // El usuario altera cosas en la pantalla por curiosidad
        vm.updatePreferredName("Nombre Alterado")
        vm.updateGradingScale(GradingScale.ZERO_TO_HUNDRED)
        vm.updateTermType(AcademicTermType.SEMESTER)
        vm.updateTermStart(LocalDate.of(2026, 8, 24))

        // Al finalizar en modo repetición
        vm.finishSetup()

        // El perfil persistido se conserva intacto
        assertEquals("Carlos Original", userRepo.userProfile.value?.preferredName)
        assertEquals(GradingScale.ZERO_TO_FIVE, userRepo.userProfile.value?.gradingScale)

        // Ningún periodo fue creado
        assertTrue(termRepo.terms.value.isEmpty())
    }

    private fun testProfile() = UserProfile(
        userId = "local",
        preferredName = "Estudiante",
        careerOrProgram = "Ingeniería",
        studyArea = StudyArea.ENGINEERING_TECHNOLOGY,
        gradingScale = GradingScale.ZERO_TO_FIVE,
        passingGrade = 3.0,
        targetAverage = 4.0,
        enabledModules = setOf(AppModule.GRADES, AppModule.TASKS),
        setupCompleted = true,
        createdAt = 1000L,
        updatedAt = 1000L
    )
}

private class FakeAcademicTermRepository : AcademicTermRepository {
    private val state = MutableStateFlow<List<AcademicTerm>>(emptyList())
    private val active = MutableStateFlow<AcademicTerm?>(null)

    override val terms: StateFlow<List<AcademicTerm>> = state.asStateFlow()
    override val activeTerm: StateFlow<AcademicTerm?> = active.asStateFlow()

    override suspend fun create(
        name: String,
        type: AcademicTermType,
        start: LocalDate,
        plannedEnd: LocalDate?
    ): Result<AcademicTerm> {
        val now = System.currentTimeMillis()
        val term = AcademicTerm(
            id = "term-${state.value.size + 1}",
            userId = "local",
            name = name,
            type = type,
            startEpochDay = start.toEpochDay(),
            plannedEndEpochDay = plannedEnd?.toEpochDay(),
            closedEpochDay = null,
            status = com.unistack.app.feature_terms.domain.AcademicTermStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )
        state.value = listOf(term) + state.value
        active.value = term
        return Result.success(term)
    }

    override suspend fun update(term: AcademicTerm): Result<Unit> = Result.success(Unit)

    override suspend fun reopen(termId: String): Result<Unit> = Result.success(Unit)

    override suspend fun close(termId: String, closedOn: LocalDate, cutScheme: com.unistack.app.feature_user.domain.GradingCutScheme?): Result<Unit> {
        active.value = null
        return Result.success(Unit)
    }

    override suspend fun delete(termId: String): Result<Unit> = Result.success(Unit)
}
