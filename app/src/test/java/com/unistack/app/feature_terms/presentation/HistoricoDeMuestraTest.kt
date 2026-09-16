package com.unistack.app.feature_terms.presentation

import com.unistack.app.TextosDePrueba
import com.unistack.app.feature_terms.domain.CalculoDelHistorico
import com.unistack.app.feature_terms.domain.HistorialAcademico
import com.unistack.app.feature_terms.domain.PendienteDeCierre
import com.unistack.app.feature_terms.domain.PropuestaDelSiguientePeriodo
import com.unistack.app.feature_terms.domain.RevisionDeCierre
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * La simulación del banco de pruebas da las mismas cifras que el artifact.
 *
 * Los valores de referencia se sacaron ejecutando el JavaScript de la simulación aprobada, con
 * las materias anuales apagadas como se eligió. Si una de estas cuentas cambia, el histórico de
 * muestra deja de parecerse a lo que se aprobó.
 */
class HistoricoDeMuestraTest {

    @Before
    fun instalarTextos() {
        TextosDePrueba.instalar()
    }

    @After
    fun salir() {
        HistoricoDeMuestra.salir()
    }

    private fun historial(): HistorialAcademico {
        val datos = HistoricoDeMuestra.datos.value!!
        return CalculoDelHistorico.build(
            terms = datos.terms,
            subjects = datos.subjects,
            sessions = datos.sessions,
            occurrences = datos.occurrences,
            breaks = emptyList(),
            tasks = datos.tasks,
            esquemaActual = datos.esquema,
            notaMaxima = 5.0,
            aprobado = 3.0,
            hoy = datos.hoy,
            ahora = datos.ahora
        )
    }

    /** Nombre, final, asistencia y racha de cada materia, como las calcula el artifact. */
    private val referencia = mapOf(
        "2026-2" to listOf(
            Fila("Estructuras de datos", 4.7, 97.143, 28),
            Fila("Física II", null, 94.286, 29),
            Fila("Probabilidad", 3.8, 90.909, 15),
            Fila("Ecuaciones diferenciales", 2.9, 94.286, 25),
            Fila("Cálculo III", 3.8, 91.667, 14),
            Fila("Inglés IV", 4.3, 94.118, 16)
        ),
        "2026-1" to listOf(
            Fila("Programación II", 4.6, 94.595, 22),
            Fila("Física I", 4.3, 92.105, 19),
            Fila("Inglés III", 4.2, 88.889, 20),
            Fila("Estadística", 4.0, 91.892, 15),
            Fila("Álgebra lineal", 3.9, 89.474, 16),
            Fila("Cálculo II", 3.8, 89.189, 14)
        ),
        "2025-2" to listOf(
            Fila("Programación I", 4.4, 91.667, 14),
            Fila("Expresión oral", 4.0, 85.714, 11),
            Fila("Química general", 3.5, 88.571, 18),
            Fila("Inglés II", 3.4, 86.111, 13),
            Fila("Cálculo II", 2.7, 88.235, 10)
        ),
        "2025-1" to listOf(
            Fila("Introducción a la ingeniería", 4.2, 91.429, 11),
            Fila("Inglés I", 3.9, 88.889, 14),
            Fila("Dibujo técnico", 3.7, 91.429, 16),
            Fila("Cálculo I", 3.6, 88.571, 11),
            Fila("Cátedra universitaria", 3.6, 88.889, 19)
        )
    )

    private data class Fila(val nombre: String, val final: Double?, val asistencia: Double, val racha: Int)

    @Test
    fun `cada materia tiene la final, la asistencia y la racha del artifact`() {
        HistoricoDeMuestra.empezar()
        val historial = historial()
        referencia.forEach { (periodoId, filas) ->
            val periodo = historial.periodos.single { it.nombre == periodoId }
            assertEquals(periodoId, filas.map { it.nombre }, periodo.materias.map { it.nombre })
            filas.zip(periodo.materias).forEach { (fila, materia) ->
                if (fila.final == null) assertNull(materia.nombre, materia.final) else assertEquals(materia.nombre, fila.final, materia.final!!, 0.0001)
                assertEquals(materia.nombre, fila.asistencia, materia.asistencia.porcentaje!!, 0.0006)
                assertEquals(materia.nombre, fila.racha, materia.asistencia.racha)
            }
        }
    }

    @Test
    fun `los promedios, el acumulado y el rango son los del artifact`() {
        HistoricoDeMuestra.empezar()
        val historial = historial()
        val promedios = historial.periodos.associate { it.nombre to it.promedio!! }
        assertEquals(3.9, promedios.getValue("2026-2"), 0.0001)
        assertEquals(4.1333, promedios.getValue("2026-1"), 0.0001)
        assertEquals(3.6, promedios.getValue("2025-2"), 0.0001)
        assertEquals(3.8, promedios.getValue("2025-1"), 0.0001)
        assertEquals(93.7346, historial.periodo(historial.activo!!.id)!!.asistencia!!, 0.0001)
        assertEquals(3.8625, historial.acumulado!!, 0.00001)
        assertEquals(16, historial.notasFinales)
        assertEquals(3.8714285714285714, historial.acumuladoAlCerrar(historial.activo!!)!!, 0.00001)
        val (piso, techo) = historial.rangoDelAcumulado(historial.activo!!)!!
        assertEquals(3.804090909090909, piso, 0.00001)
        assertEquals(3.895, techo, 0.00001)
        assertEquals(4, historial.activo!!.completas)
        assertEquals(listOf("Ecuaciones diferenciales"), historial.activo!!.perdidas.map { it.nombre })
        assertEquals(listOf("Cálculo II"), historial.periodos.single { it.nombre == "2025-2" }.perdidas.map { it.nombre })
    }

    @Test
    fun `las notas de cada corte y lo que falta al cerrar salen igual`() {
        HistoricoDeMuestra.empezar()
        val historial = historial()
        val activo = historial.activo!!
        val estructuras = activo.materias.single { it.nombre == "Estructuras de datos" }
        assertEquals(listOf(4.3, 4.8, 4.5, 5.0, 4.4, 4.9), estructuras.cortes.flatMap { c -> c.notas.map { it.value } })
        val fisica = activo.materias.single { it.nombre == "Física II" }
        assertEquals(listOf(4.7, 3.7, 3.5, 4.0), fisica.cortes.flatMap { c -> c.notas.map { it.value } })

        val pendientes = RevisionDeCierre.pendientes(activo, HistoricoDeMuestra.datos.value!!.tasks, historial.hoy)
        assertEquals(3, pendientes.size)
        val tarea = pendientes[0] as PendienteDeCierre.Tarea
        assertEquals("Proyecto final", tarea.tarea.title)
        assertEquals(LocalDate.of(2026, 11, 26), tarea.vence)
        assertEquals("Física II", (pendientes[1] as PendienteDeCierre.Nota).materia.nombre)
        val clases = pendientes[2] as PendienteDeCierre.Clases
        assertEquals("Probabilidad", clases.materia.nombre)
        assertEquals(3, clases.clases.size)
    }

    @Test
    fun `cerrar y empezar el siguiente dejan lo mismo que el artifact`() {
        HistoricoDeMuestra.saltarASinPeriodo()
        val sinPeriodo = historial()
        assertNull(sinPeriodo.activo)
        assertEquals(LocalDate.of(2026, 12, 5), sinPeriodo.cerrados.first().term.closedEpochDay?.let(LocalDate::ofEpochDay))
        val propuesta = PropuestaDelSiguientePeriodo.build(HistoricoDeMuestra.datos.value!!.terms, sinPeriodo.hoy)
        assertEquals("2027-1", propuesta.nombre)
        assertEquals(LocalDate.of(2027, 1, 25), propuesta.inicioSugerido)
        assertEquals(listOf(LocalDate.of(2026, 1, 26), LocalDate.of(2025, 2, 3)), propuesta.iniciosAnteriores)
        assertEquals(listOf(16, 18, 20), propuesta.opcionesDeSemanas)

        HistoricoDeMuestra.saltarAPorEmpezar()
        val porEmpezar = historial()
        val nuevo = porEmpezar.activo!!
        assertTrue(nuevo.porEmpezar)
        assertEquals("2027-1", nuevo.nombre)
        assertEquals(listOf("Ecuaciones diferenciales"), nuevo.materias.map { it.nombre })
        assertTrue(nuevo.materias.single().esRepetida)
    }
}
