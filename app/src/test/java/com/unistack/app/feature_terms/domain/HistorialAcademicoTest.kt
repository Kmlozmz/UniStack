package com.unistack.app.feature_terms.domain

import com.unistack.app.TextosDePrueba
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingCutScheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Las cuentas del histórico.
 *
 * Fijan lo que se enseña como cierto: una final solo existe con todo evaluado, el acumulado es
 * la media de las definitivas de los cerrados —con la repetida contando sus dos intentos—, y el
 * rango del periodo en curso es piso y techo, nunca una proyección.
 */
class HistorialAcademicoTest {

    @Before
    fun instalarTextos() {
        TextosDePrueba.instalar()
    }

    private val hoy = LocalDate.of(2026, 12, 5)
    private val zona = ZoneId.of("UTC")

    private val esquema = GradingCutScheme(
        listOf(
            GradingCut("period-1", "Corte 1", 0.30, 1),
            GradingCut("period-2", "Corte 2", 0.30, 2),
            GradingCut("period-3", "Corte 3", 0.40, 3)
        )
    )

    private fun periodo(id: String, inicio: LocalDate, fin: LocalDate, cerrado: LocalDate?) = AcademicTerm(
        id = id,
        userId = "u",
        name = id,
        type = AcademicTermType.SEMESTER,
        startEpochDay = inicio.toEpochDay(),
        plannedEndEpochDay = fin.toEpochDay(),
        closedEpochDay = cerrado?.toEpochDay(),
        status = if (cerrado != null) AcademicTermStatus.CLOSED else AcademicTermStatus.ACTIVE,
        createdAt = 0,
        updatedAt = 0
    )

    private fun nota(cutId: String, valor: Double) = GradeItem(
        id = "g-$cutId-$valor-${System.nanoTime()}",
        name = "Parcial",
        value = valor,
        percentage = 1.0,
        cutId = cutId
    )

    private fun materia(id: String, termId: String, cortes: List<Double?>, repiteDe: String? = null) = Subject(
        id = id,
        name = id,
        targetAverage = 4.0,
        grades = cortes.mapIndexedNotNull { i, v -> v?.let { nota("period-${i + 1}", it) } },
        cutScheme = esquema,
        termId = termId,
        repeatedFromSubjectId = repiteDe
    )

    private val p1 = periodo("2025-2", LocalDate.of(2025, 7, 28), LocalDate.of(2025, 11, 28), LocalDate.of(2025, 11, 28))
    private val p2 = periodo("2026-1", LocalDate.of(2026, 1, 26), LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 5))
    private val activo = periodo("2026-2", LocalDate.of(2026, 7, 27), LocalDate.of(2026, 11, 27), null)

    private fun construir(
        terms: List<AcademicTerm> = listOf(p1, p2, activo),
        subjects: List<Subject>,
        sessions: List<ClassSession> = emptyList(),
        occurrences: List<ClassOccurrence> = emptyList(),
        tasks: List<StudentTask> = emptyList()
    ) = CalculoDelHistorico.build(
        terms = terms,
        subjects = subjects,
        sessions = sessions,
        occurrences = occurrences,
        breaks = emptyList(),
        tasks = tasks,
        esquemaActual = esquema,
        notaMaxima = 5.0,
        aprobado = 3.0,
        hoy = hoy,
        ahora = hoy.atTime(12, 0),
        zona = zona
    )

    @Test
    fun `la final solo existe con todos los cortes evaluados`() {
        val historial = construir(
            subjects = listOf(
                materia("completa", "2026-2", listOf(4.0, 3.0, 5.0)),
                materia("a medias", "2026-2", listOf(4.0, 3.0, null))
            )
        )
        val materias = historial.activo!!.materias.associateBy { it.id }
        assertEquals(4.1, materias.getValue("completa").final!!, 0.0001)
        assertNull(materias.getValue("a medias").final)
        assertEquals(2, materias.getValue("a medias").primerCorteQueFalta)
    }

    @Test
    fun `el acumulado es la media de las definitivas y la repetida cuenta dos veces`() {
        val historial = construir(
            subjects = listOf(
                materia("calculo-1", "2025-2", listOf(2.5, 2.5, 2.5)),
                materia("fisica", "2025-2", listOf(4.0, 4.0, 4.0)),
                materia("calculo-2", "2026-1", listOf(4.0, 4.0, 4.0), repiteDe = "calculo-1")
            )
        )
        assertEquals((2.5 + 4.0 + 4.0) / 3, historial.acumulado!!, 0.0001)
        assertEquals(3, historial.notasFinales)
        val repetida = historial.materia("calculo-2")!!.second
        assertEquals("2025-2", repetida.intentoAnterior!!.termName)
        assertEquals(2.5, repetida.intentoAnterior!!.final!!, 0.0001)
        assertEquals(listOf("calculo-1"), historial.periodo("2025-2")!!.perdidas.map { it.id })
    }

    @Test
    fun `la serie lleva el acumulado a cada cierre en orden cronologico`() {
        val historial = construir(
            subjects = listOf(
                materia("a", "2025-2", listOf(3.0, 3.0, 3.0)),
                materia("b", "2026-1", listOf(5.0, 5.0, 5.0))
            )
        )
        assertEquals(listOf("2025-2", "2026-1"), historial.serie.map { it.nombre })
        assertEquals(3.0, historial.serie[0].acumulado!!, 0.0001)
        assertEquals(4.0, historial.serie[1].acumulado!!, 0.0001)
    }

    @Test
    fun `el rango del acumulado es piso y techo con lo que falta`() {
        val historial = construir(
            subjects = listOf(
                materia("cerrada", "2026-1", listOf(4.0, 4.0, 4.0)),
                materia("en curso", "2026-2", listOf(5.0, 5.0, null))
            )
        )
        val (piso, techo) = historial.rangoDelAcumulado(historial.activo!!)!!
        // Piso: 5·0,3 + 5·0,3 + 0 = 3,0. Techo: con el 40 % restante en 5, 5,0.
        assertEquals((4.0 + 3.0) / 2, piso, 0.0001)
        assertEquals((4.0 + 5.0) / 2, techo, 0.0001)
    }

    @Test
    fun `las clases cuentan desde el inicio y la racha se corta con una falta`() {
        val sesion = ClassSession(
            id = "s",
            subjectId = "m",
            daysOfWeek = setOf(DayOfWeek.MONDAY.value),
            startMinute = 8 * 60,
            endMinute = 10 * 60,
            createdAt = 0,
            updatedAt = 0
        )
        val lunes = listOf(LocalDate.of(2026, 7, 27), LocalDate.of(2026, 8, 3), LocalDate.of(2026, 8, 10), LocalDate.of(2026, 8, 17))
        val marcas = listOf(ClassAttendanceStatus.ATTENDED, ClassAttendanceStatus.ATTENDED, ClassAttendanceStatus.ABSENT, ClassAttendanceStatus.ATTENDED)
        val ocurrencias = lunes.zip(marcas).map { (dia, estado) ->
            ClassOccurrence(id = "o-$dia", sessionId = "s", dateEpochDay = dia.toEpochDay(), status = estado, updatedAt = 0)
        }
        val historial = construir(
            subjects = listOf(materia("m", "2026-2", listOf(null, null, null))),
            sessions = listOf(sesion),
            occurrences = ocurrencias
        )
        val asistencia = historial.activo!!.materias.single().asistencia
        assertEquals(3, asistencia.asistidas)
        assertEquals(1, asistencia.faltas)
        assertEquals(2, asistencia.racha)
        assertEquals(75.0, asistencia.porcentaje!!, 0.0001)
        // Del 24 de agosto al fin previsto no hay nada marcado: son clases sin marcar, no faltas.
        assertTrue(asistencia.sinMarcar > 0)
        assertFalse(historial.activo!!.materias.single().completa)
    }

    @Test
    fun `los pendientes de cierre van materia por materia y respetan los cortes desconocidos`() {
        val desconocida = materia("desconocida", "2026-2", listOf(null, 4.0, 4.0)).copy(unknownCutIds = setOf("period-1"))
        val sinNota = materia("sin nota", "2026-2", listOf(4.0, 4.0, null))
        val tarea = StudentTask(
            id = "t",
            title = "Proyecto final",
            description = "",
            subjectId = "sin nota",
            type = TaskType.values().first(),
            dueDateMillis = LocalDate.of(2026, 11, 26).atStartOfDay(zona).toInstant().toEpochMilli(),
            difficulty = TaskDifficulty.values().first(),
            estimatedMinutes = 60,
            completed = false,
            createdAt = 0,
            updatedAt = 0
        )
        val historial = construir(subjects = listOf(desconocida, sinNota), tasks = listOf(tarea))
        val pendientes = RevisionDeCierre.pendientes(historial.activo!!, listOf(tarea), hoy, zona = zona)
        assertEquals(2, pendientes.size)
        assertTrue(pendientes[0] is PendienteDeCierre.Nota)
        assertEquals("sin nota", (pendientes[0] as PendienteDeCierre.Nota).materia.id)
        assertTrue(pendientes[1] is PendienteDeCierre.Tarea)
        assertTrue(RevisionDeCierre.pendientes(historial.activo!!, listOf(tarea), hoy, setOf("tarea:t"), zona).none { it is PendienteDeCierre.Tarea })
    }

    @Test
    fun `el periodo siguiente sigue la numeracion y empieza el lunes de la fecha de su tramo`() {
        val propuesta = PropuestaDelSiguientePeriodo.build(listOf(p1, p2, activo.copy(closedEpochDay = LocalDate.of(2026, 11, 27).toEpochDay(), status = AcademicTermStatus.CLOSED)), hoy)
        assertEquals("2027-1", propuesta.nombre)
        // 2026-1 empezó el 26 de enero; en 2027 ese día es martes, así que el lunes de esa semana.
        assertEquals(LocalDate.of(2027, 1, 25), propuesta.inicioSugerido)
        assertEquals(listOf(LocalDate.of(2026, 1, 26)), propuesta.iniciosAnteriores)
        assertEquals(LocalDate.of(2027, 1, 20), propuesta.diaDelAviso(OpcionesDelAviso.ANTES))
        assertEquals(LocalDate.of(2027, 2, 1), propuesta.diaDelAviso(OpcionesDelAviso.DESPUES))
    }

    @Test
    fun `el aviso no se corre con los dias cuando el inicio previsto ya paso`() {
        val tarde = LocalDate.of(2027, 3, 3)
        val propuesta = PropuestaDelSiguientePeriodo.build(listOf(p1, p2.copy()), tarde)
        assertTrue(!propuesta.inicioSugerido.isBefore(tarde))
        assertTrue(propuesta.inicioPrevisto.isBefore(tarde))
        assertEquals(propuesta.inicioPrevisto, propuesta.diaDelAviso(OpcionesDelAviso.EL_DIA))
    }

    @Test
    fun `los repartos de cortes suman siempre la duracion`() {
        assertEquals(listOf(6, 6, 6), RepartoDeCortes.iguales(18, 3))
        assertEquals(listOf(5, 5, 8), RepartoDeCortes.ultimoMasLargo(18, 3))
        assertEquals(listOf(7, 6, 5), RepartoDeCortes.ajustar(listOf(6, 6, 6), 18, 0, 1))
        assertNull(RepartoDeCortes.ajustar(listOf(8, 8, 2), 18, 0, 1))
        val cierres = RepartoDeCortes.cierres(LocalDate.of(2027, 1, 25), listOf(6, 6, 6))
        assertEquals(LocalDate.of(2027, 3, 7).toEpochDay(), cierres[0])
        assertEquals(LocalDate.of(2027, 4, 18).toEpochDay(), cierres[1])
        assertNull(cierres[2])
        assertTrue(GradingCutScheme(esquema.cuts.mapIndexed { i, c -> c.copy(endEpochDay = cierres[i]) }).isValid)
    }

    @Test
    fun `un cerrado con su copia de cortes no se mueve si cambia el esquema de hoy`() {
        val otroEsquema = GradingCutScheme(
            listOf(
                GradingCut("period-1", "Corte 1", 0.50, 1),
                GradingCut("period-2", "Corte 2", 0.25, 2),
                GradingCut("period-3", "Corte 3", 0.25, 3)
            )
        )
        val cerradoConCopia = p2.copy(cutScheme = esquema)
        val historial = CalculoDelHistorico.build(
            terms = listOf(cerradoConCopia),
            subjects = listOf(materia("m", "2026-1", listOf(5.0, 5.0, 3.0))),
            sessions = emptyList(),
            occurrences = emptyList(),
            breaks = emptyList(),
            tasks = emptyList(),
            esquemaActual = otroEsquema,
            notaMaxima = 5.0,
            aprobado = 3.0,
            hoy = hoy,
            ahora = hoy.atTime(12, 0),
            zona = zona
        )
        // Con su copia (30/30/40): 1,5 + 1,5 + 1,2 = 4,2. Con el de hoy (50/25/25) serían 4,5.
        assertEquals(4.2, historial.cerrados.single().materias.single().final!!, 0.0001)
        assertNotNull(historial.acumulado)
    }
}
