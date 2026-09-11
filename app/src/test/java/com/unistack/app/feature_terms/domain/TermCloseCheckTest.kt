package com.unistack.app.feature_terms.domain

import com.unistack.app.TextosDePrueba
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingCutScheme
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * La comprobación previa al cierre.
 *
 * Cerrar es irreversible, así que lo único que no puede pasar es cerrar sin saber qué se queda
 * a medias. Estas pruebas fijan qué cuenta como «a medias» y, sobre todo, cuándo la app **no**
 * puede afirmar que una materia se perdió.
 */
class TermCloseCheckTest {

    @Before
    fun instalarTextos() {
        TextosDePrueba.instalar()
    }
    private val zona: ZoneId = ZoneId.of("UTC")
    private val hoy: LocalDate = LocalDate.of(2026, 12, 12)

    private fun esquema() = GradingCutScheme(
        listOf(
            GradingCut("period-1", "Corte 1", 0.30, 1, LocalDate.of(2026, 9, 20).toEpochDay()),
            GradingCut("period-2", "Corte 2", 0.40, 2, LocalDate.of(2026, 10, 31).toEpochDay()),
            GradingCut("period-3", "Corte 3", 0.30, 3, null)
        )
    )

    private fun nota(cutId: String, valor: Double, porcentaje: Double = 1.0) = GradeItem(
        id = "g-$cutId-$valor",
        name = "Parcial",
        value = valor,
        percentage = porcentaje,
        cutId = cutId
    )

    private fun materia(
        id: String,
        nombre: String,
        notas: List<GradeItem>,
        desconocidos: Set<String> = emptySet()
    ) = Subject(
        id = id,
        name = nombre,
        targetAverage = 4.0,
        grades = notas,
        cutScheme = esquema(),
        unknownCutIds = desconocidos
    )

    private fun informe(
        materias: List<Subject>,
        cerrados: Set<String> = TermCloseCheck.allCuts(esquema()),
        sinMarcar: Map<String, Int> = emptyMap(),
        tareas: List<StudentTask> = emptyList()
    ) = TermCloseCheck.build(
        subjects = materias,
        cutScheme = esquema(),
        closedCutIds = cerrados,
        unmarkedClasses = sinMarcar,
        tasks = tareas,
        passingGrade = 3.0,
        today = hoy,
        zone = zona
    )

    private fun tarea(id: String, titulo: String, vence: LocalDate, hecha: Boolean = false) = StudentTask(
        id = id,
        title = titulo,
        description = "",
        subjectId = "m1",
        type = TaskType.WORKSHOP,
        dueDateMillis = vence.atStartOfDay(zona).toInstant().toEpochMilli(),
        difficulty = TaskDifficulty.MEDIUM,
        estimatedMinutes = 60,
        completed = hecha,
        createdAt = 0L,
        updatedAt = 0L
    )

    // --- Cortes sin nota ---

    @Test
    fun `un corte cerrado sin una sola nota es un hueco`() {
        val r = informe(listOf(materia("m1", "Cálculo II", listOf(nota("period-1", 4.0)))))
        val faltantes = r.gaps.filterIsInstance<TermGap.MissingCut>()
        assertEquals(2, faltantes.size)
        assertEquals("Corte 2", faltantes[0].cutName)
        assertEquals(40, faltantes[0].weightPercent)
    }

    @Test
    fun `un corte con nota final tambien cuenta como registrado`() {
        val completa = materia(
            "m1", "Estadística",
            listOf(nota("period-1", 4.0), nota("period-2", 3.8), nota("period-3", 4.2))
        )
        assertTrue(informe(listOf(completa)).gaps.isEmpty())
    }

    @Test
    fun `un corte marcado como no lo tengo no se reclama`() {
        val materia = materia(
            "m1", "Física",
            listOf(nota("period-2", 4.0), nota("period-3", 4.0)),
            desconocidos = setOf("period-1")
        )
        assertTrue(informe(listOf(materia)).gaps.isEmpty())
    }

    @Test
    fun `durante el periodo solo se miran los cortes que ya cerraron`() {
        val materia = materia("m1", "Cálculo II", emptyList())
        val cerrados = TermCloseCheck.closedCutsOn(esquema(), LocalDate.of(2026, 10, 1))
        val faltantes = informe(listOf(materia), cerrados = cerrados)
            .gaps.filterIsInstance<TermGap.MissingCut>()
        assertEquals(1, faltantes.size)
        assertEquals("Corte 1", faltantes.single().cutName)
    }

    @Test
    fun `el ultimo corte nunca cierra solo`() {
        // Ni pasado el fin previsto: el periodo sigue abierto hasta que se cierra a mano.
        val cerrados = TermCloseCheck.closedCutsOn(esquema(), LocalDate.of(2027, 3, 1))
        assertEquals(setOf("period-1", "period-2"), cerrados)
    }

    @Test
    fun `sin fechas de corte no se puede decir que ninguno cerro`() {
        assertTrue(TermCloseCheck.closedCutsOn(GradingCutScheme.default(), hoy).isEmpty())
    }

    // --- Asistencias y entregas ---

    @Test
    fun `las clases sin marcar salen con su cuenta`() {
        val r = informe(
            listOf(materia("m1", "Estadística", listOf(nota("period-1", 4.0), nota("period-2", 4.0), nota("period-3", 4.0)))),
            sinMarcar = mapOf("m1" to 4)
        )
        val hueco = r.gaps.filterIsInstance<TermGap.UnmarkedClasses>().single()
        assertEquals(4, hueco.count)
        assertEquals("Estadística — 4 clases sin marcar", hueco.title())
    }

    @Test
    fun `una entrega vencida sin hacer es un hueco`() {
        val completa = materia("m1", "Contabilidad", listOf(nota("period-1", 4.0), nota("period-2", 4.0), nota("period-3", 4.0)))
        val r = informe(listOf(completa), tareas = listOf(tarea("t1", "Informe", LocalDate.of(2026, 12, 2))))
        val hueco = r.gaps.filterIsInstance<TermGap.OverdueTask>().single()
        assertEquals("Informe", hueco.title)
        assertEquals("Venció el 2 de diciembre", hueco.detail())
    }

    @Test
    fun `una entrega ya hecha no molesta`() {
        val completa = materia("m1", "Contabilidad", listOf(nota("period-1", 4.0), nota("period-2", 4.0), nota("period-3", 4.0)))
        val r = informe(listOf(completa), tareas = listOf(tarea("t1", "Informe", LocalDate.of(2026, 12, 2), hecha = true)))
        assertTrue(r.gaps.isEmpty())
    }

    @Test
    fun `una entrega que aun no vence no molesta`() {
        val completa = materia("m1", "Contabilidad", listOf(nota("period-1", 4.0), nota("period-2", 4.0), nota("period-3", 4.0)))
        val r = informe(listOf(completa), tareas = listOf(tarea("t1", "Informe", LocalDate.of(2026, 12, 20))))
        assertTrue(r.gaps.isEmpty())
    }

    @Test
    fun `la que vence hoy todavia no esta vencida`() {
        val completa = materia("m1", "Contabilidad", listOf(nota("period-1", 4.0), nota("period-2", 4.0), nota("period-3", 4.0)))
        val r = informe(listOf(completa), tareas = listOf(tarea("t1", "Informe", hoy)))
        assertTrue(r.gaps.isEmpty())
    }

    // --- Perdidas ---

    @Test
    fun `una materia bajo el aprobado con las notas completas se da por perdida`() {
        val floja = materia("m1", "Cálculo II", listOf(nota("period-1", 2.0), nota("period-2", 2.5), nota("period-3", 2.6)))
        val perdida = informe(listOf(floja)).failed.single()
        assertEquals("Cálculo II", perdida.name)
        assertTrue(perdida.average < 3.0)
    }

    @Test
    fun `con un corte sin registrar no se puede afirmar que se perdio`() {
        // 2,0 sobre el 30% que hay registrado no dice nada del 70% que falta.
        val incompleta = materia("m1", "Cálculo II", listOf(nota("period-1", 2.0)))
        assertTrue(informe(listOf(incompleta)).failed.isEmpty())
    }

    @Test
    fun `una materia sin ninguna nota no es una materia perdida`() {
        assertTrue(informe(listOf(materia("m1", "Electiva", emptyList()))).failed.isEmpty())
    }

    // --- El conjunto ---

    @Test
    fun `sin nada a medias el informe esta limpio`() {
        val completa = materia("m1", "Estadística", listOf(nota("period-1", 4.0), nota("period-2", 4.0), nota("period-3", 4.0)))
        val r = informe(listOf(completa))
        assertTrue(r.isClean)
        assertEquals(1, r.subjectsWithEverything)
        assertEquals(1, r.subjectsTotal)
        assertEquals(4.0, r.average!!, 0.01)
    }

    @Test
    fun `con algo a medias el informe no esta limpio`() {
        val r = informe(listOf(materia("m1", "Cálculo II", emptyList())))
        assertFalse(r.isClean)
        assertEquals(0, r.subjectsWithEverything)
    }

    @Test
    fun `los huecos van primero los que cuestan puntos`() {
        val floja = materia("m1", "Cálculo II", listOf(nota("period-1", 4.0), nota("period-3", 4.0)))
        val r = informe(
            listOf(floja),
            sinMarcar = mapOf("m1" to 2),
            tareas = listOf(tarea("t1", "Informe", LocalDate.of(2026, 12, 2)))
        )
        assertTrue(r.gaps[0] is TermGap.MissingCut)
        assertTrue(r.gaps[1] is TermGap.UnmarkedClasses)
        assertTrue(r.gaps[2] is TermGap.OverdueTask)
    }

    @Test
    fun `el resumen cuenta en singular y en plural`() {
        assertEquals("No falta nada por terminar", emptyList<TermGap>().summaryLine())
        assertEquals(
            "Hay 1 cosa sin terminar",
            listOf(TermGap.UnmarkedClasses("Física", "m1", 1) as TermGap).summaryLine()
        )
    }

    @Test
    fun `sin ninguna materia el promedio no se inventa`() {
        val r = informe(emptyList())
        assertTrue(r.isClean)
        assertEquals(null, r.average)
    }
}
