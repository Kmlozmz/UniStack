package com.unistack.app.feature_notes.domain

import com.unistack.app.feature_schedule.domain.ClassSession
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * «Estas en Calculo II»: de donde sale eso.
 *
 * La app sabe a que hora tienes clase, no sabe si fuiste. Por eso el resultado se ofrece con un
 * atajo y un aspa en vez de aplicarse solo, y por eso estas pruebas fijan cuando **no** hay que
 * sugerir, que es la mitad que hace que la sugerencia no estorbe.
 */
class NoteMomentTest {

    // Viernes 28 de agosto de 2026.
    private val viernes = LocalDateTime.of(2026, 8, 28, 9, 30)

    private fun sesion(
        id: String,
        subjectId: String,
        dias: Set<Int>,
        desde: Int,
        hasta: Int,
        cadaSemanas: Int = 1,
        anclaje: Long = 0L
    ) = ClassSession(
        id = id,
        subjectId = subjectId,
        daysOfWeek = dias,
        startMinute = desde,
        endMinute = hasta,
        createdAt = 0,
        updatedAt = 0,
        repeatEveryWeeks = cadaSemanas,
        recurrenceStartEpochDay = anclaje
    )

    @Test
    fun laClaseQueEstaOcurriendoEsLaQueSeSugiere() {
        val sesiones = listOf(sesion("c1", "calculo", setOf(5), 8 * 60, 10 * 60))
        assertEquals("calculo", NoteMoment.subjectInClassNow(sesiones, viernes))
    }

    @Test
    fun fueraDeHoraNoHayNadaQueSugerir() {
        val sesiones = listOf(sesion("c1", "calculo", setOf(5), 8 * 60, 9 * 60))
        assertNull(NoteMoment.subjectInClassNow(sesiones, viernes))
    }

    @Test
    fun otroDiaDeLaSemanaTampocoCuenta() {
        val sesiones = listOf(sesion("c1", "calculo", setOf(1, 3), 8 * 60, 10 * 60))
        assertNull(NoteMoment.subjectInClassNow(sesiones, viernes))
    }

    /** El minuto en que acaba la clase ya es «despues»: a las 10:00 en punto se sale. */
    @Test
    fun elMinutoDelFinalYaEsDespues() {
        val sesiones = listOf(sesion("c1", "calculo", setOf(5), 8 * 60, 9 * 60 + 30))
        assertNull(NoteMoment.subjectInClassNow(sesiones, viernes))
    }

    /**
     * Con dos clases solapadas gana la que empezo mas tarde.
     *
     * Si acabas de entrar a la segunda, es de esa de la que estas tomando apuntes.
     */
    @Test
    fun entreDosSolapadasGanaLaQueEmpezoDespues() {
        val sesiones = listOf(
            sesion("c1", "vieja", setOf(5), 8 * 60, 11 * 60),
            sesion("c2", "nueva", setOf(5), 9 * 60, 11 * 60)
        )
        assertEquals("nueva", NoteMoment.subjectInClassNow(sesiones, viernes))
    }

    @Test
    fun sinHorarioNoSeSugiereNada() {
        assertNull(NoteMoment.subjectInClassNow(emptyList(), viernes))
    }

    @Test
    fun noSeSugiereLoQueYaEstaPuesto() {
        assertFalse(NoteMoment.shouldSuggest("calculo", currentSubjectId = "calculo", dismissedSubjectId = null))
    }

    /** Insistir con algo que acaban de quitar es la forma mas rapida de que estorbe. */
    @Test
    fun noSeInsisteConLoQueSeAcabaDeQuitar() {
        assertFalse(NoteMoment.shouldSuggest("calculo", currentSubjectId = null, dismissedSubjectId = "calculo"))
    }

    @Test
    fun siHayClaseYNoEstaPuestaSeSugiere() {
        assertTrue(NoteMoment.shouldSuggest("calculo", currentSubjectId = null, dismissedSubjectId = null))
        assertTrue(NoteMoment.shouldSuggest("calculo", currentSubjectId = "fisica", dismissedSubjectId = "otra"))
    }

    @Test
    fun sinClaseNoSeSugiereAunqueNoHayaMateriaPuesta() {
        assertFalse(NoteMoment.shouldSuggest(null, currentSubjectId = null, dismissedSubjectId = null))
    }
}
