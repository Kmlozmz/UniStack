package com.unistack.app.feature_expenses.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * Las tres lecturas de los gastos, con fechas fijas.
 *
 * Se prueban aqui y no mirando la pantalla porque las tres dependen del dia en que se miren:
 * una proyeccion hecha el dia 11 de un mes de 30 no se puede juzgar el dia 25.
 */
class ExpenseInsightsTest {

    private val lunes = LocalDate.of(2026, 9, 7)

    private fun gasto(fecha: LocalDate, monto: Int, categoria: ExpenseCategory = ExpenseCategory.FOOD) =
        Expense(
            id = "e-" + fecha + "-" + monto + "-" + categoria,
            category = categoria,
            amount = monto,
            dateMillis = ExpenseDateUtils.toMillis(fecha),
            createdAt = 0L,
            updatedAt = 0L
        )

    // ------------------------------------------------------------- comparar

    @Test
    fun comparaLaSemanaConLaAnterior() {
        val gastos = listOf(
            gasto(lunes, 10_000),
            gasto(lunes.plusDays(2), 5_000, ExpenseCategory.TRANSPORT),
            gasto(lunes.minusDays(7), 20_000),
            gasto(lunes.minusDays(5), 10_000, ExpenseCategory.TRANSPORT)
        )

        val c = ExpenseInsights.compararSemanas(gastos, today = lunes.plusDays(3))

        assertEquals(15_000, c.actual)
        assertEquals(30_000, c.anterior)
        assertEquals(-15_000, c.diferencia)
        assertEquals(-50, c.cambioPorcentual)
    }

    @Test
    fun senalaLaCategoriaQueMasExplicaElCambio() {
        val gastos = listOf(
            gasto(lunes, 4_000),
            gasto(lunes, 1_000, ExpenseCategory.TRANSPORT),
            gasto(lunes.minusDays(7), 24_000),
            gasto(lunes.minusDays(7), 2_000, ExpenseCategory.TRANSPORT)
        )

        val c = ExpenseInsights.compararSemanas(gastos, today = lunes)

        assertEquals(ExpenseCategory.FOOD, c.categoriaQueMasCambia?.category)
        assertEquals(-20_000, c.categoriaQueMasCambia?.diferencia)
    }

    /** Sin semana anterior no hay porcentaje: dividir entre cero seria inventarse un dato. */
    @Test
    fun sinSemanaAnteriorNoHayPorcentaje() {
        val c = ExpenseInsights.compararSemanas(listOf(gasto(lunes, 9_000)), today = lunes)
        assertEquals(9_000, c.actual)
        assertEquals(0, c.anterior)
        assertNull(c.cambioPorcentual)
    }

    /** Las categorias sin nada en ninguna de las dos semanas no salen. */
    @Test
    fun soloSalenLasCategoriasConAlgo() {
        val c = ExpenseInsights.compararSemanas(listOf(gasto(lunes, 9_000)), today = lunes)
        assertEquals(listOf(ExpenseCategory.FOOD), c.categorias.map { it.category })
    }

    // ------------------------------------------------------------- ritmo

    @Test
    fun proyectaElMesConReglaDeTres() {
        val dia10 = LocalDate.of(2026, 9, 10)
        val gastos = (1..10).map { gasto(LocalDate.of(2026, 9, it), 2_000) }

        val r = ExpenseInsights.ritmoDelMes(gastos, presupuestoMensual = 50_000, today = dia10)

        assertEquals(20_000, r.gastado)
        assertEquals(30, r.diasDelMes)
        // 20.000 en 10 dias de 30 -> 60.000 al acabar.
        assertEquals(60_000, r.proyeccion)
        assertTrue(r.seVaAPasar)
    }

    @Test
    fun repartePorLoQueQuedaDeMes() {
        val dia10 = LocalDate.of(2026, 9, 10)
        val gastos = listOf(gasto(LocalDate.of(2026, 9, 5), 20_000))

        val r = ExpenseInsights.ritmoDelMes(gastos, presupuestoMensual = 60_000, today = dia10)

        // Quedan 40.000 y 20 dias.
        assertEquals(2_000, r.diarioRestante)
        assertEquals(20, r.diasRestantes)
    }

    @Test
    fun sinPresupuestoNoHayCuotaDiaria() {
        val r = ExpenseInsights.ritmoDelMes(
            listOf(gasto(LocalDate.of(2026, 9, 5), 20_000)),
            presupuestoMensual = 0,
            today = LocalDate.of(2026, 9, 10)
        )
        assertNull(r.diarioRestante)
        assertTrue(!r.seVaAPasar)
    }

    @Test
    fun pasadoElPresupuestoLaCuotaEsCero() {
        val r = ExpenseInsights.ritmoDelMes(
            listOf(gasto(LocalDate.of(2026, 9, 5), 90_000)),
            presupuestoMensual = 60_000,
            today = LocalDate.of(2026, 9, 10)
        )
        assertEquals(0, r.diarioRestante)
    }

    /** La linea es acumulada: nunca baja, y acaba en lo gastado. */
    @Test
    fun laLineaEsAcumuladaYAcabaEnElTotal() {
        val dia5 = LocalDate.of(2026, 9, 5)
        val gastos = listOf(
            gasto(LocalDate.of(2026, 9, 1), 1_000),
            gasto(LocalDate.of(2026, 9, 3), 4_000)
        )

        val r = ExpenseInsights.ritmoDelMes(gastos, presupuestoMensual = 0, today = dia5)

        assertEquals(listOf(1_000, 1_000, 5_000, 5_000, 5_000), r.acumuladoPorDia)
        assertEquals(5_000, r.gastado)
    }

    // ------------------------------------------------------------- calendario

    @Test
    fun elCalendarioTieneUnDiaPorCadaDiaDelMes() {
        val c = ExpenseInsights.calendarioDelMes(emptyList(), YearMonth.of(2026, 9))
        assertEquals(30, c.dias.size)
        assertEquals(0, c.total)
    }

    /** Septiembre de 2026 empieza en martes: un hueco antes del dia 1. */
    @Test
    fun losHuecosColocanElDiaUnoBajoSuNombre() {
        val c = ExpenseInsights.calendarioDelMes(emptyList(), YearMonth.of(2026, 9))
        assertEquals(1, c.huecosIniciales)
    }

    @Test
    fun cadaDiaLlevaSusGastosOrdenadosPorImporte() {
        val dia = LocalDate.of(2026, 9, 4)
        val c = ExpenseInsights.calendarioDelMes(
            listOf(gasto(dia, 3_000), gasto(dia, 9_000, ExpenseCategory.TRANSPORT)),
            YearMonth.of(2026, 9)
        )

        val cuarto = c.dias.first { it.fecha == dia }
        assertEquals(12_000, cuarto.total)
        assertEquals(listOf(9_000, 3_000), cuarto.gastos.map { it.amount })
        assertEquals(dia, c.diaMasCaro?.fecha)
    }

    @Test
    fun losGastosDeOtroMesNoEntran() {
        val c = ExpenseInsights.calendarioDelMes(
            listOf(gasto(LocalDate.of(2026, 8, 20), 50_000)),
            YearMonth.of(2026, 9)
        )
        assertEquals(0, c.total)
    }
}
