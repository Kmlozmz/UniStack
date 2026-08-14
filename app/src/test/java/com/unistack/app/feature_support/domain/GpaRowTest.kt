package com.unistack.app.feature_support.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GpaRowTest {

    @Test
    fun `sin notas no hay promedio`() {
        assertNull(weightedAverage(listOf(GpaRow(), GpaRow())))
    }

    @Test
    fun `sin creditos todas las materias pesan igual`() {
        val average = weightedAverage(
            listOf(GpaRow(grade = "3"), GpaRow(grade = "5"))
        )

        assertEquals(4.0, average!!, 0.001)
    }

    @Test
    fun `los creditos mandan sobre el promedio`() {
        // Cuatro créditos de 5 y uno de 2: el promedio simple daría 3,5.
        val average = weightedAverage(
            listOf(GpaRow(grade = "5", credits = "4"), GpaRow(grade = "2", credits = "1"))
        )

        assertEquals(4.4, average!!, 0.001)
    }

    @Test
    fun `la coma decimal cuenta igual que el punto`() {
        val average = weightedAverage(listOf(GpaRow(grade = "3,5")))

        assertEquals(3.5, average!!, 0.001)
    }

    @Test
    fun `una fila vacia no cuenta y una a medio escribir cuenta por lo que lleva`() {
        // «3,» ya es un 3: el promedio se mueve mientras se teclea, en vez de esperar a que la
        // cifra esté terminada.
        val average = weightedAverage(
            listOf(GpaRow(grade = "4"), GpaRow(grade = ""), GpaRow(grade = "3,"))
        )

        assertEquals(3.5, average!!, 0.001)
    }

    @Test
    fun `cero creditos no anula la fila`() {
        // Escribir un 0 en créditos no puede dejar el promedio sin peso: la fila cuenta como una.
        val average = weightedAverage(listOf(GpaRow(grade = "4", credits = "0")))

        assertEquals(4.0, average!!, 0.001)
    }
}
