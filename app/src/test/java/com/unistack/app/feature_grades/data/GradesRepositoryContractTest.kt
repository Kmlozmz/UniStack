package com.unistack.app.feature_grades.data

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Fija el contrato de [com.unistack.app.feature_grades.domain.GradesRepository] que las dos
 * implementaciones tienen que compartir.
 *
 * Existe por un fallo concreto: cambiar la escala de notas debía borrarlas y no borraba nada.
 * El código hacía `updateSubject(subject.copy(grades = emptyList()))`, que funcionaba contra
 * este doble en memoria —reemplazaba el objeto entero— pero no contra Room, que solo escribe
 * los campos de la materia porque las notas viven en su propia tabla. El doble era más
 * permisivo que la implementación real, así que ningún test podía ver el problema.
 */
class GradesRepositoryContractTest {

    private fun subjectWithGrades(): Subject = Subject(
        id = "materia-1",
        name = "Cálculo III",
        targetAverage = 4.0,
        grades = listOf(
            GradeItem(id = "n1", name = "Parcial", value = 4.0, percentage = 0.5),
            GradeItem(id = "n2", name = "Quiz", value = 3.0, percentage = 0.25)
        )
    )

    @Test
    fun `updateSubject changes subject fields without touching its grades`() {
        val repository = InMemoryGradesRepository()
        val subject = subjectWithGrades()
        repository.addSubject(subject)

        repository.updateSubject(subject.copy(name = "Cálculo IV", grades = emptyList()))

        val stored = repository.subjects.value.single()
        assertEquals("el campo sí se actualiza", "Cálculo IV", stored.name)
        assertEquals("las notas no se gestionan por aquí", 2, stored.grades.size)
    }

    @Test
    fun `clearGrades empties the subject but keeps it alive`() {
        val repository = InMemoryGradesRepository()
        repository.addSubject(subjectWithGrades())

        repository.clearGrades("materia-1")

        val stored = repository.subjects.value.single()
        assertTrue("las notas se van", stored.grades.isEmpty())
        assertEquals("la materia se queda", "Cálculo III", stored.name)
    }

    @Test
    fun `clearGrades only touches the subject it was given`() {
        val repository = InMemoryGradesRepository()
        repository.addSubject(subjectWithGrades())
        repository.addSubject(subjectWithGrades().copy(id = "materia-2", name = "Estadística"))

        repository.clearGrades("materia-1")

        val otra = repository.subjects.value.single { it.id == "materia-2" }
        assertEquals(2, otra.grades.size)
    }
}
